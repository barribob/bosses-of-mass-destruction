package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.render.IRenderLight
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

class BoundedLighting<T : Entity>(private val minimumValue: Int) : IRenderLight<T> {
    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return VanillaCopies.getBlockLight(entity, blockPos).coerceAtLeast(minimumValue)
    }
}