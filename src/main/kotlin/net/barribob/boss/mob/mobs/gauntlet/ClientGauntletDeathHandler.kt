package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.mobs.gauntlet.ServerGauntletDeathHandler.Companion.deathAnimationTime
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity

class ClientGauntletDeathHandler(val entity: GauntletEntity): IEntityTick<ClientWorld> {
    override fun tick(world: ClientWorld) {
        ++entity.deathTime
        if (entity.deathTime == deathAnimationTime) {
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }
}