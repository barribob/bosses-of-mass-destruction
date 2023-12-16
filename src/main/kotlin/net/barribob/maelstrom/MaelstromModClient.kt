package net.barribob.maelstrom

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

@Environment(EnvType.CLIENT)
fun clientInit() {
    if(MaelstromMod.isDevelopmentEnvironment){
        clientInitDev()
    }
}

@Environment(EnvType.CLIENT)
private fun clientInitDev() {
    ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { MaelstromMod.clientEventScheduler.updateEvents() })
    ClientPlayNetworking.registerGlobalReceiver(MaelstromMod.DRAW_POINTS_PACKET_ID) { client, _, buf, _ ->
        MaelstromMod.debugPoints.drawDebugPointsClient(client, buf)
    }
}