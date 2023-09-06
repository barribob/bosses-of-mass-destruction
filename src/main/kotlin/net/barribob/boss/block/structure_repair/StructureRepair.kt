package net.barribob.boss.block.structure_repair

import net.minecraft.registry.RegistryKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.world.gen.structure.Structure

interface StructureRepair {
    fun associatedStructure(): RegistryKey<Structure>
    fun repairStructure(world: ServerWorld, structureStart: StructureStart)
    fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart): Boolean
}