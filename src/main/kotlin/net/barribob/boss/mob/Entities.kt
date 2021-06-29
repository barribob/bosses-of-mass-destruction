package net.barribob.boss.mob

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.animation.IAnimationTimer
import net.barribob.boss.animation.PauseAnimationTimer
import net.barribob.boss.config.ModConfig
import net.barribob.boss.item.SoulStarEntity
import net.barribob.boss.mob.mobs.gauntlet.*
import net.barribob.boss.mob.mobs.lich.*
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithArmorRenderer
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithBoneLight
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithEntity
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomCodeAnimations
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.SimpleLivingGeoRenderer
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.boss.projectile.comet.CometCodeAnimations
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.render.*
import net.barribob.maelstrom.general.data.WeakHashPredicate
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.util.GlfwUtil
import net.minecraft.entity.*
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object Entities {
    private val mobConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val LICH: EntityType<LichEntity> = registerConfiguredMob("lich",
        { type, world -> LichEntity(type, world, mobConfig.lichConfig) })
    { it.dimensions(EntityDimensions.fixed(1.8f, 3.0f)) }

    val MAGIC_MISSILE: EntityType<MagicMissileProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Mod.identifier("blue_fireball"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::MagicMissileProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val COMET: EntityType<CometProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Mod.identifier("comet"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::CometProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val SOUL_STAR: EntityType<SoulStarEntity> = Registry.register(
        Registry.ENTITY_TYPE,
        Mod.identifier("soul_star"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::SoulStarEntity)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val OBSIDILITH: EntityType<ObsidilithEntity> = registerConfiguredMob("obsidilith",
        { type, world -> ObsidilithEntity(type, world, mobConfig.obsidilithConfig) },
        { it.fireImmune().dimensions(EntityDimensions.fixed(2.0f, 4.4f)) })

    val GAUNTLET: EntityType<GauntletEntity> = registerConfiguredMob("gauntlet",
        { type, world -> GauntletEntity(type, world, mobConfig.gauntletConfig) },
        { it.fireImmune().dimensions(EntityDimensions.fixed(5.0f, 4.0f)) })

    private val VOID_BLOSSOM: EntityType<VoidBlossomEntity> = registerConfiguredMob("void_blossom",
        { type, world -> VoidBlossomEntity(type, world) },
        { it.dimensions(EntityDimensions.fixed(2.0f, 4.4f)) })

    private val killCounter = LichKillCounter(mobConfig.lichConfig.summonMechanic)

    private fun <T : Entity> registerConfiguredMob(
        name: String,
        factory: (EntityType<T>, World) -> T,
        augment: (FabricEntityTypeBuilder<T>) -> FabricEntityTypeBuilder<T>,
    ): EntityType<T> {
        val builder = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER)
        { type: EntityType<T>, world -> factory(type, world) }
        return Registry.register(Registry.ENTITY_TYPE,  Mod.identifier(name), augment(builder).build())
    }

    fun init() {
        if(mobConfig.lichConfig.summonMechanic.isEnabled) {
            ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(killCounter)
        }

        FabricDefaultAttributeRegistry.register(LICH,
            HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 6.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, mobConfig.lichConfig.health)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, mobConfig.lichConfig.missile.damage)
        )

        FabricDefaultAttributeRegistry.register(OBSIDILITH,
            HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, mobConfig.obsidilithConfig.health)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, mobConfig.obsidilithConfig.attack)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 10.0)
                .add(EntityAttributes.GENERIC_ARMOR, mobConfig.obsidilithConfig.armor)
        )

        FabricDefaultAttributeRegistry.register(GAUNTLET, HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_FLYING_SPEED, 4.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
            .add(EntityAttributes.GENERIC_MAX_HEALTH, mobConfig.gauntletConfig.health)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 10.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, mobConfig.gauntletConfig.attack)
            .add(EntityAttributes.GENERIC_ARMOR, mobConfig.gauntletConfig.armor))

        FabricDefaultAttributeRegistry.register(VOID_BLOSSOM,
            HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 10.0)
                .add(EntityAttributes.GENERIC_ARMOR, .0)
        )
    }

    fun clientInit(animationTimer: IAnimationTimer) {
        val pauseSecondTimer = PauseAnimationTimer({ GlfwUtil.getTime() }, { MinecraftClient.getInstance().isPaused })

        EntityRendererRegistry.INSTANCE.register(LICH) { context ->
            val texture = Mod.identifier("textures/entity/lich.png")
            SimpleLivingGeoRenderer(
                context, GeoModel(
                    { Mod.identifier("geo/lich.geo.json") },
                    { texture },
                    Mod.identifier("animations/lich.animation.json"),
                    animationTimer,
                    LichCodeAnimations()
                ),
                BoundedLighting(5),
                LichBoneLight(),
                EternalNightRenderer(),
                renderLayer = RenderLayer.getEntityCutoutNoCull(texture)
            )
        }

        EntityRendererRegistry.INSTANCE.register(OBSIDILITH) { context ->
            val runeColorHandler = ObsidilithBoneLight()
            val modelProvider = GeoModel<ObsidilithEntity>(
                { Mod.identifier("geo/obsidilith.geo.json") },
                { Mod.identifier("textures/entity/obsidilith.png") },
                Mod.identifier("animations/obsidilith.animation.json"),
                animationTimer,
            )
            val armorRenderer = ObsidilithArmorRenderer(modelProvider)
            val obsidilithRenderer = SimpleLivingGeoRenderer(
                context,
                modelProvider,
                renderer = CompositeRenderer(armorRenderer, runeColorHandler),
                renderWithModel = armorRenderer,
                iBoneLight = runeColorHandler,
                deathRotation = false
            )
            obsidilithRenderer
        }

        val missileTexture = Mod.identifier("textures/entity/blue_magic_missile.png")
        val magicMissileRenderLayer = RenderLayer.getEntityCutoutNoCull(missileTexture)
        EntityRendererRegistry.INSTANCE.register(MAGIC_MISSILE) { context ->
            SimpleEntityRenderer(
                context,
                CompositeRenderer(
                    BillboardRenderer(context.renderDispatcher, magicMissileRenderLayer) { 0.5f },
                    ConditionalRenderer(
                        WeakHashPredicate<MagicMissileProjectile> { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LerpedPosRenderer {
                            ParticleFactories.soulFlame().build(it.add(RandomUtils.randVec().multiply(0.25)))
                        })
                ),
                { missileTexture },
                FullRenderLight()
            )
        }

        EntityRendererRegistry.INSTANCE.register(SOUL_STAR) { context ->
            FlyingItemEntityRenderer(context, 1.0f, true)
        }

        EntityRendererRegistry.INSTANCE.register(COMET) { context ->
            ModGeoRenderer(context, GeoModel(
                { Mod.identifier("geo/comet.geo.json") },
                { Mod.identifier("textures/entity/comet.png") },
                Mod.identifier("animations/comet.animation.json"),
                animationTimer,
                CometCodeAnimations()
            ),
                ConditionalRenderer(
                    WeakHashPredicate { FrameLimiter(60f, pauseSecondTimer)::canDoFrame },
                    LerpedPosRenderer {
                        ParticleFactories.cometTrail().build(it.add(RandomUtils.randVec().multiply(0.5)))
                    }),
                FullRenderLight()
            )
        }

        EntityRendererRegistry.INSTANCE.register(GAUNTLET) { context ->
            val modelProvider = GeoModel(
                { Mod.identifier("geo/gauntlet.geo.json") },
                GauntletTextureProvider(),
                Mod.identifier("animations/gauntlet.animation.json"),
                animationTimer,
                GauntletCodeAnimations()
            )
            val energyRenderer = GauntletEnergyRenderer(modelProvider)
            val overlayOverride = GauntletOverlay()
            SimpleLivingGeoRenderer(
                context, modelProvider,
                renderer = CompositeRenderer(
                    GauntletLaserRenderer(),
                    ConditionalRenderer(
                        WeakHashPredicate { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LaserParticleRenderer()
                    ),
                    energyRenderer,
                    overlayOverride
                ),
                renderWithModel = energyRenderer,
                deathRotation = false,
                overlayOverride = overlayOverride
            )
        }

        EntityRendererRegistry.INSTANCE.register(VOID_BLOSSOM) { context ->
            val texture = Mod.identifier("textures/entity/void_blossom.png")
            val modelProvider = GeoModel(
                { Mod.identifier("geo/void_blossom.geo.json") },
                { texture },
                Mod.identifier("animations/void_blossom.animation.json"),
                animationTimer,
                VoidBlossomCodeAnimations()
            )
            SimpleLivingGeoRenderer(context, modelProvider, deathRotation = false)
        }
    }
}