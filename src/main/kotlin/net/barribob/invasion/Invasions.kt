package net.barribob.invasion

import net.barribob.invasion.mob.MaelstromScoutEntity
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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Invasions {
    const val MODID = "maelstrom_invasions"

    val LOGGER: Logger = LogManager.getLogger()
}

object Entities {
    val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(Registry.ENTITY_TYPE,
            Identifier(Invasions.MODID, "maelstrom_scout"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity).dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build())
}

@Suppress("unused")
fun init() {
    MaelstromMod.aiManager.addGoalInjection(EntityType.getId(Entities.MAELSTROM_SCOUT).toString()) { entity -> Pair(2, JumpToTargetGoal(entity)) }

    Invasions.LOGGER.info(MaelstromMod.hoconConfigManager.handleConfigLoad(Invasions.MODID, "test").getString("test"))
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    registerModRenderer(
        Entities.MAELSTROM_SCOUT,
        ModelMaelstromScout(), Invasions.MODID, "maelstrom_scout.png")
}