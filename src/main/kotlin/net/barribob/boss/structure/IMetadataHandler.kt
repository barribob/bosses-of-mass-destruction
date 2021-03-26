package net.barribob.boss.structure

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ServerWorldAccess
import java.util.*

fun interface IMetadataHandler {
    fun handleMetadata(
        metadata: String, pos: BlockPos, serverWorldAccess: ServerWorldAccess, random: Random, boundingBox: BlockBox
    )
}