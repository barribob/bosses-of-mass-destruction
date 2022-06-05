package net.barribob.boss.block.structure_repair

import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.gen.structure.StructureType

interface StructureRepair {
    fun associatedStructure(): RegistryKey<StructureType>
    fun repairStructure(world: ServerWorld, structureStart: StructureStart)
    fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart): Boolean
}