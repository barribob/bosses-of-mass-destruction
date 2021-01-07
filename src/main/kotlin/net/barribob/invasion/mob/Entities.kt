package net.barribob.invasion.mob

import net.barribob.invasion.Invasions
import net.barribob.invasion.animation.IAnimationTimer
import net.barribob.invasion.animation.PauseAnimationTimer
import net.barribob.invasion.mob.mobs.lich.*
import net.barribob.invasion.mob.utils.SimpleLivingGeoRenderer
import net.barribob.invasion.particle.ParticleFactories
import net.barribob.invasion.projectile.MagicMissileProjectile
import net.barribob.invasion.projectile.comet.CometCodeAnimations
import net.barribob.invasion.projectile.comet.CometProjectile
import net.barribob.invasion.render.*
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.WeakHashPredicate
import net.barribob.maelstrom.general.io.config.IConfig
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.GlfwUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object Entities {
    val LICH: EntityType<LichEntity> = registerConfiguredMob("lich", ::LichEntity)
    { it.dimensions(EntityDimensions.fixed(1.8f, 3.0f)) }

    val MAGIC_MISSILE: EntityType<MagicMissileProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Invasions.identifier("magic_missile"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::MagicMissileProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val COMET: EntityType<CometProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Invasions.identifier("comet"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::CometProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    private fun <T : Entity> registerConfiguredMob(
        name: String,
        factory: (EntityType<T>, World, IConfig) -> T,
        augment: (FabricEntityTypeBuilder<T>) -> FabricEntityTypeBuilder<T>,
    ): EntityType<T> {
        val identifier = Invasions.identifier(name)
        val mobsConfig = MaelstromMod.configRegistry.getConfig(Identifier(Invasions.MODID, "mobs"))
        if (!mobsConfig.hasPath(identifier.path)) {
            throw Exception("The mob config is missing an entry for ${identifier.path}")
        }
        val builder = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER)
        { type: EntityType<T>, world -> factory(type, world, mobsConfig.getConfig(identifier.path)) }
        return Registry.register(Registry.ENTITY_TYPE, identifier, augment(builder).build())
    }

    fun init() {
        FabricDefaultAttributeRegistry.register(LICH,
            HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FLYING_SPEED, 4.0))
    }

    fun clientInit(animationTimer: IAnimationTimer) {
        val pauseSecondTimer = PauseAnimationTimer({ GlfwUtil.getTime() }, { MinecraftClient.getInstance().isPaused })

        EntityRendererRegistry.INSTANCE.register(LICH) { entityRenderDispatcher, _ ->
            SimpleLivingGeoRenderer(
                entityRenderDispatcher, GeoModel(
                    Invasions.identifier("geo/lich.geo.json"),
                    Invasions.identifier("textures/entity/lich.png"),
                    Invasions.identifier("animations/lich.animation.json"),
                    animationTimer,
                    LichCodeAnimations()
                ),
                BoundedLighting(7),
                LichBoneLight(),
                EternalNightRenderer()
            )
        }

        val missileTexture = Invasions.identifier("textures/entity/blue_magic_missile.png")
        val magicMissileRenderLayer = RenderLayer.getEntityCutoutNoCull(missileTexture)
        EntityRendererRegistry.INSTANCE.register(MAGIC_MISSILE) { entityRenderDispatcher, _ ->
            SimpleEntityRenderer(
                entityRenderDispatcher,
                CompositeRenderer(listOf(
                    BillboardRenderer(entityRenderDispatcher, magicMissileRenderLayer) { 0.5f },
                    ConditionalRenderer(
                        WeakHashPredicate<MagicMissileProjectile> { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LerpedPosRenderer {
                            ParticleFactories.soulFlame().build(it.add(RandomUtils.randVec().multiply(0.25)))
                        })
                )),
                { missileTexture },
                FullRenderLight()
            )
        }

        EntityRendererRegistry.INSTANCE.register(COMET) { entityRenderDispatcher, _ ->
            ModGeoRenderer(entityRenderDispatcher, GeoModel(
                Invasions.identifier("geo/comet.geo.json"),
                Invasions.identifier("textures/entity/comet.png"),
                Invasions.identifier("animations/comet.animation.json"),
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
    }
}