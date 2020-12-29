package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.render.IRenderLight
import net.barribob.invasion.utils.VanillaCopies
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

class BoundedLighting<T : Entity>(private val minimumValue: Int) : IRenderLight<T> {
    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return VanillaCopies.getBlockLight(entity, blockPos).coerceAtLeast(minimumValue)
    }
}