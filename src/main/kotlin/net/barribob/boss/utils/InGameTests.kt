package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.block.MonolithBlock
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.item.SoulStarItem
import net.barribob.boss.item.WallTeleport
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.BurstAction
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.mob.mobs.obsidilith.PillarAction
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.packets.TestPacket
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.projectile.util.ExemptEntities
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.setPos
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryEntryLookup.RegistryLookup
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.random.Random

class InGameTests() {
    fun provideGear(source: ServerCommandSource) {
        val entity = source.playerOrThrow
        val armor = listOf(ItemStack(Items.NETHERITE_HELMET), ItemStack(Items.NETHERITE_CHESTPLATE), ItemStack(Items.NETHERITE_LEGGINGS), ItemStack(Items.NETHERITE_BOOTS))
        val registry = source.world.registryManager.get(RegistryKeys.ENCHANTMENT)
        armor.forEach {
            it.addEnchantment(registry.entryOf(Enchantments.PROTECTION), 3)
            it.addEnchantment(registry.entryOf(Enchantments.UNBREAKING), 3)
        }
        val sword = ItemStack(Items.NETHERITE_SWORD)
        sword.addEnchantment(registry.entryOf(Enchantments.SHARPNESS), 4)
        sword.addEnchantment(registry.entryOf(Enchantments.UNBREAKING), 3)
        val bow = ItemStack(Items.BOW)
        bow.addEnchantment(registry.entryOf(Enchantments.POWER), 4)
        bow.addEnchantment(registry.entryOf(Enchantments.INFINITY), 1)
        bow.addEnchantment(registry.entryOf(Enchantments.UNBREAKING), 3)
        val arrow = ItemStack(Items.ARROW)
        val apples = ItemStack(Items.GOLDEN_APPLE, 8)
        val food = ItemStack(Items.COOKED_PORKCHOP, 64)
        val shield = ItemStack(Items.SHIELD)
        shield.addEnchantment(registry.entryOf(Enchantments.UNBREAKING), 3)
        val pickaxe = ItemStack(Items.NETHERITE_PICKAXE)
        pickaxe.addEnchantment(registry.entryOf(Enchantments.EFFICIENCY), 4)
        val blocks = ItemStack(Items.COBBLESTONE, 64)
        listOf(sword, bow, apples, food, blocks, shield, pickaxe, arrow).forEach { entity.giveItemStack(it) }
        armor.forEach { entity.giveItemStack(it) }
    }

    fun throwProjectile(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val projectile = SporeBallProjectile(entity, entity.world, ExemptEntities(listOf()))
            projectile.setVelocity(entity, entity.pitch, entity.yaw, 0f, 1.5f, 1.0f)
            entity.world.spawnEntity(projectile)
        }
    }

    fun spawnEntity(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val serverWorld = entity.world as ServerWorld
        val compoundTag = NbtCompound()
        compoundTag.putString("id", Registries.ENTITY_TYPE.getId(EntityType.PHANTOM).toString())

        val spawner = MobPlacementLogic(
            RangedSpawnPosition(entity.pos, 3.0, 6.0, ModRandom()),
            CompoundTagEntityProvider(compoundTag, serverWorld, Mod.LOGGER),
            MobEntitySpawnPredicate(entity.world),
            SimpleMobSpawner(serverWorld)
        )
        spawner.tryPlacement(10)
    }

    fun burstAction(source: ServerCommandSource) {
        BurstAction(source.playerOrThrow).perform()
    }

    fun placePillars(source: ServerCommandSource) {
        val entity = source.playerOrThrow
        PillarAction(entity).perform()
    }

    fun obsidilithDeath(source: ServerCommandSource){
        val entity = source.playerOrThrow
        ObsidilithUtils.onDeath(entity, 100)
    }

    @Environment(EnvType.CLIENT)
    fun registerClientHandlers(){
        ClientPlayNetworking.registerGlobalReceiver(TestPacket.ID) { packet: TestPacket, context: ClientPlayNetworking.Context -> 
            testClientCallback(context.client())
        }
    }

    fun registerHandlers(){
        PayloadTypeRegistry.playS2C().register(TestPacket.ID, TestPacket.codec);
    }


    fun testClient(source: ServerCommandSource) {
        PlayerLookup.around(source.world, source.position, 100.0).forEach {
            ServerPlayNetworking.send(it, TestPacket())
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

    fun killZombies(source: ServerCommandSource) {
        val zombie = EntityType.ZOMBIE.create(source.world) ?: return
        val pos = source.playerOrThrow.pos.add(VecUtils.yAxis.multiply(-5.0))
        zombie.setPos(pos)
        source.world.spawnEntity(zombie)
        ModComponents.getWorldEventScheduler(source.world).addEvent(TimedEvent({
            zombie.damage(source.world.damageSources.playerAttack(source.playerOrThrow), 30f)
        }, 2))
    }

    fun lichSpawn(source: ServerCommandSource){
        SoulStarItem.spawnLich(BlockPos.ofFloored(source.position), source.world)
    }

    fun levitationPerformance(source: ServerCommandSource){
//        LevitationBlockEntity.tickFlight(source.playerOrThrow)
        MonolithBlock.getExplosionPower(source.world, BlockPos.ofFloored(source.position), 2.0f)
    }

    fun wallTeleport(source: ServerCommandSource) {
        WallTeleport(source.world, source.playerOrThrow).tryTeleport(source.playerOrThrow.rotationVector, source.playerOrThrow.eyePos)
    }

    fun attackRepeatedly(source: ServerCommandSource) {
        val target = source.world.getEntitiesByType(Entities.GAUNTLET) { true }.firstOrNull()
        for(i in 0..240 step 80) {
            ModComponents.getWorldEventScheduler(source.world).addEvent(TimedEvent({
               target?.damage(source.world.damageSources.playerAttack(source.playerOrThrow), 9.0f)
            }, i))
        }
    }
}