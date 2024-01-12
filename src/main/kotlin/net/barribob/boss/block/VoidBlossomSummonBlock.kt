package net.barribob.boss.block

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.lang.UnsupportedOperationException

class VoidBlossomSummonBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<VoidBlossomSummonBlockEntity>)?, settings: Settings?) : BlockWithEntity(settings) {
    override fun getCodec(): MapCodec<out BlockWithEntity> {
        throw UnsupportedOperationException()
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? = factory?.create(pos, state)
    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return validateTicker(type, ModBlocks.voidBlossomSummonBlockEntityType, VoidBlossomSummonBlockEntity::tick)
    }
}