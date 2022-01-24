package net.barribob.boss.cardinalComponents

import net.barribob.boss.Mod
import net.barribob.maelstrom.general.event.Event
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

class PlayerChargedEnderPearlCooldownComponent(private val player: ServerPlayerEntity) : IPlayerChargedEnderPearlCooldownComponent {
    private val cooldownNbtKey = "chargedEnderPearlCooldown"
    private val chargedEnderPearl = Mod.items.chargedEnderPearl

    override fun readFromNbt(tag: NbtCompound) {
        if(tag.contains(cooldownNbtKey)) {
            val cooldown = tag.getFloat(cooldownNbtKey).toInt()
            ModComponents.getWorldEventScheduler(player.world).addEvent(Event(::whenNetworkReady, {
                player.itemCooldownManager[chargedEnderPearl] = cooldown
            }, ::whenNetworkReady))
        }
    }

    private fun whenNetworkReady() = player.networkHandler != null

    override fun writeToNbt(tag: NbtCompound) {
        if(player.itemCooldownManager.isCoolingDown(chargedEnderPearl)) {
            tag.putFloat(cooldownNbtKey, player.itemCooldownManager.getCooldownProgress(chargedEnderPearl, 0f) * chargedEnderPearl.itemCooldown)
        }
    }
}