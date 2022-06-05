package net.barribob.boss.utils

import com.mojang.serialization.Codec
import net.barribob.boss.mixin.DataFixMixin
import net.minecraft.datafixer.fix.StructuresToConfiguredStructuresFix
import net.minecraft.structure.StructureSets
import net.minecraft.util.Identifier
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.gen.chunk.placement.StructurePlacement
import net.minecraft.world.gen.structure.StructureType

class StructureRegister<S : StructureType>(private val structureIdentifier: Identifier, codec: Codec<S>) {
    val structureTypeKey: net.minecraft.structure.StructureType<S> = Registry.register(Registry.STRUCTURE_TYPE, structureIdentifier.toString(), net.minecraft.structure.StructureType { codec })
    val configuredStructureKey: RegistryKey<StructureType> = createConfigureStructureKey(structureIdentifier)

    fun register(configuredStructure: StructureType, structurePlacement: StructurePlacement) {
        val configuredStructureEntry = registerConfiguredStructure(configuredStructureKey, configuredStructure)
        val structureSetKey = RegistryKey.of(Registry.STRUCTURE_SET_KEY, structureIdentifier)
        StructureSets.register(structureSetKey, configuredStructureEntry, structurePlacement)

        DataFixMixin.bossesOfMassDestruction_setStructures(DataFixMixin.bossesOfMassDestruction_getStructures() +
                mapOf(Pair(structureIdentifier.toString(), StructuresToConfiguredStructuresFix.Mapping.create(structureIdentifier.toString()))))
    }

    private fun createConfigureStructureKey(identifier: Identifier) = RegistryKey.of(Registry.STRUCTURE_KEY,  identifier)
    private fun registerConfiguredStructure(key: RegistryKey<StructureType>, configuredStructure: StructureType): RegistryEntry<StructureType> {
        return BuiltinRegistries.add(BuiltinRegistries.STRUCTURE, key.value, configuredStructure)
    }
}