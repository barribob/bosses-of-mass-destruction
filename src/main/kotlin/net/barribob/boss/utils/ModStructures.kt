package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomArenaStructureFeature
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomCavernPieceGenerator
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.structure.StructurePieceType
import net.minecraft.util.Identifier
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType


class ModStructures {
    val obsidilithStructureRegistry = StructureRegister(Mod.identifier("obsidilith_arena"))
    val gauntletStructureRegistry = StructureRegister(Mod.identifier("gauntlet_arena"))
    val lichStructureRegistry = StructureRegister(Mod.identifier("lich_tower"))

    val soulStarStructureKey: TagKey<Structure> = TagKey.of(RegistryKeys.STRUCTURE, Mod.identifier("soul_star_target"))
    val voidLilyStructureKey: TagKey<Structure> = TagKey.of(RegistryKeys.STRUCTURE, Mod.identifier("void_lily_target"))
    
    val voidBlossomCavernPiece: StructurePieceType = createStructurePiece(Mod.identifier("void_blossom_piece"), StructureFactories.voidBlossom)
    val voidBlossomStructureType: StructureType<VoidBlossomArenaStructureFeature> = Registry.register(Registries.STRUCTURE_TYPE, Mod.identifier("void_blossom"), StructureType{VoidBlossomArenaStructureFeature.CODEC})
    val voidBlossomStructureRegistry = StructureRegister(Mod.identifier("void_blossom"))

    private fun createStructurePiece(identifier: Identifier, structurePieceType: StructurePieceType) = Registry.register(Registries.STRUCTURE_PIECE, identifier, structurePieceType)

    private object StructureFactories {
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(Mod.structures.voidBlossomCavernPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}