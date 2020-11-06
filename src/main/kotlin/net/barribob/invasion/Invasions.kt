package net.barribob.invasion

import net.barribob.invasion.mob.MaelstromScoutEntity
import net.barribob.invasion.model.model.ModelMaelstromScout
import net.barribob.invasion.static_utilities.Animations
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.barribob.maelstrom.mob.ai.JumpToTargetGoal
import net.barribob.maelstrom.static_utilities.registerModRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager

object Invasions {
    const val MODID = "maelstrom_invasions"

    val START_ANIMATION_PACKET_ID = Identifier(MaelstromMod.MODID, "start_animation")

    val LOGGER = ConsoleLogger(LogManager.getLogger())
}

object Entities {
    val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(Registry.ENTITY_TYPE,
            Identifier(Invasions.MODID, "maelstrom_scout"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity).dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build())
}

@Suppress("unused")
fun init() {
    MaelstromMod.aiManager.addGoalInjection(EntityType.getId(Entities.MAELSTROM_SCOUT).toString()) { entity -> Pair(2, JumpToTargetGoal(entity)) }
    FabricDefaultAttributeRegistry.register(Entities.MAELSTROM_SCOUT, HostileEntity.createHostileAttributes())
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    ClientSidePacketRegistry.INSTANCE.register(Invasions.START_ANIMATION_PACKET_ID) { packetContext, packetData ->
        Animations.startAnimationClient(
            packetContext,
            packetData
        )
    }

    registerModRenderer(
        Entities.MAELSTROM_SCOUT,
        ModelMaelstromScout(), Invasions.MODID, "maelstrom_scout.png")
}