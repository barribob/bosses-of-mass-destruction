package net.barribob.maelstrom

import net.barribob.maelstrom.animation.client.ClientAnimationWatcher
import net.barribob.maelstrom.animation.server.ServerAnimationWatcher
import net.barribob.maelstrom.general.EventScheduler
import net.barribob.maelstrom.mob.MaelstromScoutEntity
import net.barribob.maelstrom.model.ModelMaelstromScout
import net.barribob.maelstrom.registry.registerModRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.event.client.ClientTickCallback
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object MaelstromMod {
    const val MODID = "maelstrom"
    val START_ANIMATION_PACKET_ID = Identifier(MODID, "start_animation")

    @Environment(EnvType.SERVER)
    val serverEventScheduler = EventScheduler()

    @Environment(EnvType.CLIENT)
    val clientAnimationWatcher = ClientAnimationWatcher()

    @Environment(EnvType.SERVER)
    val serverAnimationWatcher = ServerAnimationWatcher()

    val LOGGER: Logger = LogManager.getLogger()
}

@Suppress("unused")
fun init() {
    ServerTickCallback.EVENT.register(ServerTickCallback { MaelstromMod.serverEventScheduler.updateEvents() })
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    registerModRenderer(Entities.MAELSTROM_SCOUT, ModelMaelstromScout(), "maelstrom_scout.png")

    ClientSidePacketRegistry.INSTANCE.register(MaelstromMod.START_ANIMATION_PACKET_ID) { packetContext, packetData ->

        val entityId = packetData.readInt()
        val animId = packetData.readString()

        packetContext.taskQueue.execute {
        val entity = MinecraftClient.getInstance().world?.getEntityById(entityId)

        if (entity != null) {
            MaelstromMod.clientAnimationWatcher.startAnimation(entity, animId)
        }
    }}

    ClientTickCallback.EVENT.register( ClientTickCallback { MaelstromMod.clientAnimationWatcher.tick() } )

}

object Entities {
    val MAELSTROM_SCOUT: EntityType<MaelstromScoutEntity> = Registry.register(Registry.ENTITY_TYPE,
            Identifier(MaelstromMod.MODID, "maelstrom_scout"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::MaelstromScoutEntity).dimensions(EntityDimensions.fixed(0.9F, 1.8F)).build())
}