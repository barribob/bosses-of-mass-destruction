package net.barribob.boss.render

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

class FullRenderLight<T : Entity> : IRenderLight<T> {
    override fun getBlockLight(entity: T, blockPos: BlockPos): Int = 15
}