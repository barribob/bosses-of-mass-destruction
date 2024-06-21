package net.barribob.boss.utils

import net.barribob.boss.block.VoidBlossomBlock
import net.barribob.boss.block.VoidLilyBlockEntity
import net.barribob.boss.block.structure_repair.ObsidilithStructureRepair
import net.barribob.boss.block.structure_repair.VoidBlossomStructureRepair
import net.barribob.boss.item.ChargedEnderPearlEntity
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.packets.*
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class NetworkUtils {
    companion object {
        fun LivingEntity.sendVelocity(velocity: Vec3d) {
            this.velocity = velocity
            if (this is ServerPlayerEntity) {
                ServerPlayNetworking.send(this, PlayerVelocityPacket(velocity))
            }
        }

        fun GauntletEntity.changeHitbox(open: Boolean) {
            val gauntletChangeHitboxPacket = GauntletChangeHitboxPacket(this.id, open)
            PlayerLookup.tracking(this).forEach {
                ServerPlayNetworking.send(it, gauntletChangeHitboxPacket)
            }
        }

        fun GauntletEntity.sendBlindnessPacket(players: List<PlayerEntity>) {
            PlayerLookup.tracking(this).forEach { entity ->
                ServerPlayNetworking.send(entity, GauntletBlindnessPacket(this.id, players.map { it.id }))
            }
        }

        fun VoidBlossomEntity.sendSpikePacket(spikesPositions: List<BlockPos>) {
            for (spikePos in spikesPositions) {
                val packet = VoidBlossomSpikesPacket(this.id, spikePos)
                for (player in PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(player, packet)
                }
            }
        }

        fun VoidBlossomEntity.sendHealPacket(source: Vec3d, dest: Vec3d) {
            val voidBlossomHealPacket = VoidBlossomHealPacket(source, dest)
            for (player in PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, voidBlossomHealPacket)
            }
        }

        fun VoidBlossomEntity.sendPlacePacket(pos: Vec3d) {
            val voidBlossomPlacePacket = VoidBlossomPlacePacket(pos)
            for (player in PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, voidBlossomPlacePacket)
            }
        }

        fun sendVoidBlossomRevivePacket(world: ServerWorld, pos: Vec3d) {
            val packet = VoidBlossomRevivePacket(pos)
            for (player in PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
                ServerPlayNetworking.send(player, packet)
            }
        }

        fun sendObsidilithRevivePacket(world: ServerWorld, pos: Vec3d) {
            val packet = ObsidilithRevivePacket(pos)
            for (player in PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
                ServerPlayNetworking.send(player, packet)
            }
        }


        fun sendParticlePacket(world: ServerWorld, pos: BlockPos, dir: Vec3d) {
            val packet = VoidLilyParticlePacket(pos.asVec3d(), dir)
            for (player in PlayerLookup.tracking(world, pos)) {
                ServerPlayNetworking.send(player, packet)
            }
        }

        fun sendImpactPacket(world: ServerWorld, pos: Vec3d) {
            for (player in PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
                ServerPlayNetworking.send(player, ChargedEnderPearlImpactPacket(pos))
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(PlayerVelocityPacket.ID) { packet, context ->
            handlePlayerVelocity(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(GauntletChangeHitboxPacket.ID) { packet, context ->
            handleChangeHitbox(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(GauntletBlindnessPacket.ID) { packet, context ->
            handleGauntletBlindness(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(VoidBlossomSpikesPacket.ID) { packet, context ->
            handleVoidBlossomSpikes(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(VoidBlossomHealPacket.ID) { packet, context ->
            handleVoidBlossomHeal(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(VoidBlossomPlacePacket.ID) { packet, context ->
            handleVoidBlossomPlace(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(VoidLilyParticlePacket.ID) { packet, context ->
            handleVoidLilyParticles(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(ChargedEnderPearlImpactPacket.ID) { packet, context -> 
            handlePearlImpact(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(VoidBlossomRevivePacket.ID) { packet, context ->
            handleVoidBlossomRevivePacket(context.client(), packet)
        }
        ClientPlayNetworking.registerGlobalReceiver(ObsidilithRevivePacket.ID) { packet, context ->
            handleObsidilithRevivePacket(context.client(), packet)
        }
    }

    fun registerHandlers(){
        PayloadTypeRegistry.playS2C().register(ChargedEnderPearlImpactPacket.ID, ChargedEnderPearlImpactPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerVelocityPacket.ID, PlayerVelocityPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GauntletChangeHitboxPacket.ID, GauntletChangeHitboxPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GauntletBlindnessPacket.ID, GauntletBlindnessPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidBlossomSpikesPacket.ID, VoidBlossomSpikesPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidBlossomHealPacket.ID, VoidBlossomHealPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidBlossomPlacePacket.ID, VoidBlossomPlacePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidBlossomRevivePacket.ID, VoidBlossomRevivePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ObsidilithRevivePacket.ID, ObsidilithRevivePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidLilyParticlePacket.ID, VoidLilyParticlePacket.CODEC);
    }

    @Environment(EnvType.CLIENT)
    private fun handlePlayerVelocity(client: MinecraftClient, packet: PlayerVelocityPacket) {
        val velocity = packet.velocity
        client.execute {
            client.player?.velocity = velocity
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleChangeHitbox(client: MinecraftClient, packet: GauntletChangeHitboxPacket) {
        val entityId = packet.entityId
        val open = packet.open

        client.execute {
            val entity = client.world?.getEntityById(entityId)
            if (entity is GauntletEntity) {
                if (open) entity.hitboxHelper.setOpenHandHitbox() else entity.hitboxHelper.setClosedFistHitbox()
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleGauntletBlindness(client: MinecraftClient, packet: GauntletBlindnessPacket) {
        val entityId = packet.id
        val playerIds = packet.players

        client.execute {
            val entity = client.world?.getEntityById(entityId)
            val players: List<PlayerEntity> = playerIds.map { client.world?.getEntityById(it) }.filterIsInstance<PlayerEntity>()
            if(entity is GauntletEntity) {
                entity.clientBlindnessHandler.handlePlayerEffects(players)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidBlossomSpikes(client: MinecraftClient, packet: VoidBlossomSpikesPacket) {
        val entityId = packet.id
        val spikePos = packet.pos

        client.execute {
            val entity = client.world?.getEntityById(entityId)

            if (entity is VoidBlossomEntity) {
                entity.clientSpikeHandler.addSpike(spikePos)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidBlossomHeal(client: MinecraftClient, packet: VoidBlossomHealPacket) {
        val source = packet.source
        val dest = packet.dest

        client.execute {
            val world = client.world
            if (world != null) {
                VoidBlossomBlock.handleVoidBlossomHeal(world, source, dest)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidBlossomPlace(client: MinecraftClient, packet: VoidBlossomPlacePacket) {
        val pos = packet.pos

        client.execute {
            VoidBlossomBlock.handleVoidBlossomPlace(pos)
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidLilyParticles(client: MinecraftClient, particlePacket: VoidLilyParticlePacket) {
        val pos = particlePacket.pos
        val dir = particlePacket.dir

        client.execute {
            val world = client.world
            if(world != null) {
                VoidLilyBlockEntity.spawnVoidLilyParticles(world, pos, dir)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handlePearlImpact(client: MinecraftClient, packet: ChargedEnderPearlImpactPacket) {
        client.execute {
            val world = client.world
            if(world != null) {
                ChargedEnderPearlEntity.handlePearlImpact(packet.pos)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidBlossomRevivePacket(client: MinecraftClient, packet: VoidBlossomRevivePacket) {
        val pos = packet.pos

        client.execute {
            val world = client.world
            if(world != null) {
                VoidBlossomStructureRepair.handleVoidBlossomRevivePacket(pos, world)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleObsidilithRevivePacket(client: MinecraftClient, packet: ObsidilithRevivePacket) {
        val pos = packet.pos

        client.execute {
            val world = client.world
            if(world != null) {
                ObsidilithStructureRepair.handleObsidilithRevivePacket(pos, world)
            }
        }
    }
}