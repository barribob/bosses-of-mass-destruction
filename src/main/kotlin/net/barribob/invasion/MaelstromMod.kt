package net.barribob.invasion

import net.barribob.maelstrom.adapters.GoalAdapter
import net.barribob.maelstrom.mob.MaelstromScoutEntity
import net.barribob.maelstrom.mob.server.ai.JumpToTargetGoal
import net.barribob.invasion.model.model.ModelMaelstromScout
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.registry.registerModRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Invasions {
    const val MODID = "maelstrom_invasions"
}

object Entities {
    val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(Registry.ENTITY_TYPE,
            Identifier(Invasions.MODID, "maelstrom_scout"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity).dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build())
}

@Suppress("unused")
fun init() {
    MaelstromMod.aiManager.addGoalInjection(Entities.MAELSTROM_SCOUT) { entity -> Pair(2, GoalAdapter(JumpToTargetGoal(entity))) }
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    registerModRenderer(
        Entities.MAELSTROM_SCOUT,
        ModelMaelstromScout(), "maelstrom_scout.png")
}