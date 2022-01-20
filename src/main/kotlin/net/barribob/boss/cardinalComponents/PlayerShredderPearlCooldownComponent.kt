package net.barribob.boss.cardinalComponents

import net.barribob.boss.Mod
import net.barribob.maelstrom.general.event.Event
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

class PlayerShredderPearlCooldownComponent(private val player: ServerPlayerEntity) : IPlayerShredderPearlCooldownComponent {
    private val cooldownNbtKey = "shredderPearlCooldown"
    private val shredderPearl = Mod.items.shredderPearl

    override fun readFromNbt(tag: NbtCompound) {
        if(tag.contains(cooldownNbtKey)) {
            val cooldown = tag.getFloat(cooldownNbtKey).toInt()
            ModComponents.getWorldEventScheduler(player.world).addEvent(Event(::whenNetworkReady, {
                player.itemCooldownManager[shredderPearl] = cooldown
            }, ::whenNetworkReady))
        }
    }

    private fun whenNetworkReady() = player.networkHandler != null

    override fun writeToNbt(tag: NbtCompound) {
        if(player.itemCooldownManager.isCoolingDown(shredderPearl)) {
            tag.putFloat(cooldownNbtKey, player.itemCooldownManager.getCooldownProgress(shredderPearl, 0f) * shredderPearl.itemCooldown)
        }
    }
}