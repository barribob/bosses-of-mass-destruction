package net.barribob.boss.utils

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.gen.structure.Structure

class StructureRegister(structureIdentifier: Identifier) {
    val configuredStructureKey: RegistryKey<Structure> = createConfigureStructureKey(structureIdentifier)
    private fun createConfigureStructureKey(identifier: Identifier) = RegistryKey.of(RegistryKeys.STRUCTURE,  identifier)
}