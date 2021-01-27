package net.barribob.boss.cardinalComponents

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag

class LichSummonCounter(val entity: PlayerEntity): ILichSummonCounterComponent {
    var summonCounter = 0

    override fun getValue(): Int = summonCounter

    override fun increment() {
        summonCounter+=1
    }

    override fun readFromNbt(tag: CompoundTag) {
        summonCounter = tag.getInt(::summonCounter.name)
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putInt(::summonCounter.name, summonCounter)
    }
}