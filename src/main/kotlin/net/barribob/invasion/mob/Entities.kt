package net.barribob.invasion.mob

import net.barribob.invasion.Invasions
import net.barribob.invasion.animation.IAnimationTimer
import net.barribob.invasion.mob.mobs.lich.LichCodeAnimations
import net.barribob.invasion.mob.mobs.lich.LichEntity
import net.barribob.invasion.mob.utils.SimpleLivingGeoRenderer
import net.barribob.invasion.particle.ClientParticleBuilder
import net.barribob.invasion.particle.Particles
import net.barribob.invasion.projectile.SmallThrownItemEntity
import net.barribob.invasion.projectile.comet.CometCodeAnimations
import net.barribob.invasion.projectile.comet.CometProjectile
import net.barribob.invasion.render.FrameLimitedRenderer
import net.barribob.invasion.render.FullRenderLight
import net.barribob.invasion.render.LerpedPosRenderer
import net.barribob.invasion.render.ModGeoRenderer
import net.barribob.invasion.utils.ModColors
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.mob.ai.JumpToTargetGoal
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Entities {
    private val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(
        Registry.ENTITY_TYPE,
        Identifier(Invasions.MODID, "maelstrom_scout"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity)
            .dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build()
    )

    private val LICH: EntityType<LichEntity> = Registry.register(
        Registry.ENTITY_TYPE,
        Invasions.identifier("lich"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::LichEntity)
            .dimensions(EntityDimensions.fixed(1.0f, 3.0f)).build()
    )

    val TEST_PROJECTILE: EntityType<SmallThrownItemEntity> = Registry.register(
        Registry.ENTITY_TYPE,
        Invasions.identifier("small_magic_missile"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::SmallThrownItemEntity)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    val COMET: EntityType<CometProjectile> = Registry.register(
        Registry.ENTITY_TYPE,
        Invasions.identifier("comet"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::CometProjectile)
            .dimensions(EntityDimensions.fixed(0.25f, 0.25f)).build()
    )

    fun init() {
        MaelstromMod.aiManager.addGoalInjection(EntityType.getId(MAELSTROM_SCOUT).toString()) { entity ->
            Pair(
                2,
                JumpToTargetGoal(entity)
            )
        }
        FabricDefaultAttributeRegistry.register(MAELSTROM_SCOUT, HostileEntity.createHostileAttributes())
        FabricDefaultAttributeRegistry.register(LICH, HostileEntity.createHostileAttributes())
    }

    fun clientInit(animationTimer: IAnimationTimer) {
        EntityRendererRegistry.INSTANCE.register(MAELSTROM_SCOUT) { entityRenderDispatcher, _ ->
            SimpleLivingGeoRenderer<MaelstromScoutEntity>(
                entityRenderDispatcher, GeoModel(
                    Identifier(Invasions.MODID, "geo/maelstrom_scout.geo.json"),
                    Identifier(Invasions.MODID, "textures/entity/maelstrom_scout.png"),
                    Identifier(Invasions.MODID, "animations/scout.animation.json"),
                    animationTimer
                )
            )
        }
        EntityRendererRegistry.INSTANCE.register(LICH) { entityRenderDispatcher, _ ->
            SimpleLivingGeoRenderer(
                entityRenderDispatcher, GeoModel(
                    Invasions.identifier("geo/lich.geo.json"),
                    Invasions.identifier("textures/entity/lich.png"),
                    Invasions.identifier("animations/lich.animation.json"),
                    animationTimer,
                    LichCodeAnimations()
                )
            )
        }
        EntityRendererRegistry.INSTANCE.register(TEST_PROJECTILE) { entityRenderDispatcher, context ->
            FlyingItemEntityRenderer(entityRenderDispatcher, context.itemRenderer)
        }

        val cometTrailParticleFactory = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
            .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
            .brightness { Particles.FULL_BRIGHT }
            .scale { 0.5f + it * 0.3f }
        EntityRendererRegistry.INSTANCE.register(COMET) { entityRenderDispatcher, _ ->
            ModGeoRenderer(entityRenderDispatcher, GeoModel(
                Invasions.identifier("geo/comet.geo.json"),
                Invasions.identifier("textures/entity/comet.png"),
                Invasions.identifier("animations/comet.animation.json"),
                animationTimer,
                CometCodeAnimations()
            ),
                FrameLimitedRenderer(3f,
                    LerpedPosRenderer { cometTrailParticleFactory.build(it.add(RandomUtils.randVec().multiply(0.5))) }),
                FullRenderLight()
            )
        }
    }
}