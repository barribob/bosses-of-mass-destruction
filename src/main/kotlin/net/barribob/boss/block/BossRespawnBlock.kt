package net.barribob.boss.block

import net.barribob.boss.block.structure_repair.StructureRepair
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BossRespawnBlock(settings: Settings, private val structureRepairs: List<StructureRepair>) : Block(settings) {
    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if(world is ServerWorld) {
            for (structureRepair in structureRepairs) {
                val structureStart = world.structureAccessor.getStructureAt(pos, structureRepair.associatedStructure())
                if(structureStart.hasChildren() && structureRepair.shouldRepairStructure(world, structureStart)) {
                    world.breakBlock(pos, false)
                    structureRepair.repairStructure(world, structureStart)
                }
            }
        }
    }
}