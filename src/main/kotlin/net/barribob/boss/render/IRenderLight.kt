package net.barribob.boss.render

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

interface IRenderLight<T : Entity> {
    fun getBlockLight(entity: T, blockPos: BlockPos): Int
}