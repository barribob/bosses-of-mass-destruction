package net.barribob.invasion.mob

import net.barribob.invasion.Invasions
import net.barribob.invasion.animation.IAnimationTimer
import net.barribob.invasion.mob.mobs.LichEntity
import net.barribob.invasion.mob.utils.ModGeoRenderer
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.mob.ai.JumpToTargetGoal
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
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
            ModGeoRenderer<MaelstromScoutEntity>(
                entityRenderDispatcher, GeoModel(
                    Identifier(Invasions.MODID, "geo/maelstrom_scout.geo.json"),
                    Identifier(Invasions.MODID, "textures/entity/maelstrom_scout.png"),
                    Identifier(Invasions.MODID, "animations/scout.animation.json"),
                    animationTimer
                )
            )
        }
        EntityRendererRegistry.INSTANCE.register(LICH) { entityRenderDispatcher, _ ->
            ModGeoRenderer<LichEntity>(
                entityRenderDispatcher, GeoModel(
                    Invasions.identifier("geo/lich.geo.json"),
                    Invasions.identifier("textures/entity/lich.png"),
                    Invasions.identifier("animations/lich.animation.json"),
                    animationTimer
                )
            )
        }
    }
}