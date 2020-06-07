package net.barribob.maelstrom

import net.barribob.maelstrom.general.EventScheduler
import net.barribob.maelstrom.mob.MaelstromScoutEntity
import net.barribob.maelstrom.model.ModelMaelstromScout
import net.barribob.maelstrom.registry.registerModRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object MaelstromMod {
    const val MODID = "maelstrom"

    @Environment(EnvType.SERVER)
    val serverEventScheduler = EventScheduler()
}

@Suppress("unused")
fun init() {
    ServerTickCallback.EVENT.register(ServerTickCallback { MaelstromMod.serverEventScheduler.updateEvents() })
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    registerModRenderer(Entities.MAELSTROM_SCOUT, ModelMaelstromScout(), "maelstrom_scout.png")
}

object Entities {
    val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(Registry.ENTITY_TYPE,
            Identifier(MaelstromMod.MODID, "maelstrom_scout"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity).dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build())
}