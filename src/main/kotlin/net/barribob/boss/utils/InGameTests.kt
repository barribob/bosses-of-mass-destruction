package net.barribob.boss.utils

import io.netty.buffer.Unpooled
import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.BurstAction
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.mob.mobs.obsidilith.PillarAction
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.DebugPointsNetworkHandler
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import kotlin.random.Random

class InGameTests(private val debugPoints: DebugPointsNetworkHandler) {
    private val clientTestPacketId = Mod.identifier("client_test")

    fun provideGear(source: ServerCommandSource) {
        val entity = source.player
        val armor = listOf(ItemStack(Items.NETHERITE_HELMET), ItemStack(Items.NETHERITE_CHESTPLATE), ItemStack(Items.NETHERITE_LEGGINGS), ItemStack(Items.NETHERITE_BOOTS))
        armor.forEach {
            it.addEnchantment(Enchantments.PROTECTION, 3)
            it.addEnchantment(Enchantments.UNBREAKING, 3)
        }
        val sword = ItemStack(Items.NETHERITE_SWORD)
        sword.addEnchantment(Enchantments.SHARPNESS, 4)
        sword.addEnchantment(Enchantments.UNBREAKING, 3)
        val bow = ItemStack(Items.BOW)
        bow.addEnchantment(Enchantments.POWER, 4)
        bow.addEnchantment(Enchantments.INFINITY, 1)
        bow.addEnchantment(Enchantments.UNBREAKING, 3)
        val arrow = ItemStack(Items.ARROW)
        val apples = ItemStack(Items.GOLDEN_APPLE, 8)
        val food = ItemStack(Items.COOKED_PORKCHOP, 64)
        val shield = ItemStack(Items.SHIELD)
        shield.addEnchantment(Enchantments.UNBREAKING, 3)
        listOf(sword, bow, apples, food, arrow, shield).forEach { entity.giveItemStack(it) }
        armor.forEach { entity.giveItemStack(it) }
    }

    fun throwProjectile(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val projectile = MagicMissileProjectile(entity, entity.world, {}, listOf(EntityType.ZOMBIE))
            projectile.setProperties(entity, entity.pitch, entity.yaw, 0f, 1.5f, 1.0f)
            entity.world.spawnEntity(projectile)
        }
    }

    fun axisOffset(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val points = mutableListOf<Vec3d>()
            for (vec in listOf(VecUtils.xAxis, VecUtils.yAxis, VecUtils.zAxis)) {
                val offset = MathUtils.axisOffset(entity.rotationVector, vec)
                val pos = entity.getCameraPosVec(0f)
                MathUtils.lineCallback(pos, offset.add(pos), 30) { v, _ -> points.add(v) }
            }
            debugPoints.drawDebugPoints(points, 1, entity.pos, source.world)
        }
    }

    fun spawnEntity(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val serverWorld = entity.world as ServerWorld
        val compoundTag = CompoundTag()
        compoundTag.putString("id", Registry.ENTITY_TYPE.getId(EntityType.PHANTOM).toString())

        val spawner = MobPlacementLogic(
            RangedSpawnPosition(entity.pos, 3.0, 6.0, ModRandom()),
            CompoundTagEntityProvider(compoundTag, serverWorld, Mod.LOGGER),
            MobEntitySpawnPredicate(entity.world),
            SimpleMobSpawner(serverWorld)
        )
        spawner.tryPlacement(10)
    }

    fun lichSummon(source: ServerCommandSource) {
        Entities.killCounter.trySummonLich(source.player, source.world)
    }

    var calls = 0
    fun lichCounter(source: ServerCommandSource) {
        Entities.killCounter.onEntitiesKilledUpdate(calls, source.player, source.world)
        calls++
    }

    fun burstAction(source: ServerCommandSource) {
        BurstAction(source.player).perform()
    }

    fun playerPosition(source: ServerCommandSource) {
        val points = ModComponents.getPlayerPositions(source.player)
        debugPoints.drawDebugPoints(points, 1, source.position, source.world)
        debugPoints.drawDebugPoints(
            listOf(ObsidilithUtils.approximatePlayerNextPosition(points, source.player.pos)),
            1,
            source.position,
            source.world,
            listOf(1f, 0f, 1f, 1f)
        )
    }

    fun placePillars(source: ServerCommandSource) {
        val entity = source.player
        PillarAction(entity).perform()
    }

    fun placeObsidian(source: ServerCommandSource){
        val entity = source.player
        ObsidilithUtils.placeObsidianBelow(entity)
    }

    fun obsidilithDeath(source: ServerCommandSource){
        val entity = source.player
        ObsidilithUtils.onDeath(entity, 100)
    }

    @Environment(EnvType.CLIENT)
    fun registerClientHandlers(){
        ClientPlayNetworking.registerGlobalReceiver(clientTestPacketId) { client, _, _, _ ->
            testClientCallback(client)
        }
    }

    fun testClient(source: ServerCommandSource) {
        val packetData = PacketByteBuf(Unpooled.buffer())
        PlayerLookup.around(source.world, source.position, 100.0).forEach {
            ServerPlayNetworking.send(it, clientTestPacketId, packetData)
        }
    }

    @Environment(EnvType.CLIENT)
    private fun testClientCallback(client: MinecraftClient) {
        val player = client.player ?: return
        for(i in 0..10) {
            val startingRotation = Random.nextInt(360)
            ClientParticleBuilder(Particles.EYE)
                .continuousPosition { player.pos.add(VecUtils.yAxis.multiply(i * 0.2)).add(VecUtils.xAxis.rotateY(Math.toRadians((it.getAge() * 2 + startingRotation).toDouble()).toFloat())) }
                .build(player.pos.add(VecUtils.yAxis.multiply(i * 0.2)).add(VecUtils.xAxis.rotateY(Math.toRadians(startingRotation.toDouble()).toFloat())))
        }
    }
}