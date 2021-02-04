package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.utils.ISidedCooldownAction
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.boss.utils.NetworkUtils.Companion.sendVelocity
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.SideShapeType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class BurstAction(
    val entity: LivingEntity,
    val world: World,
    val sendStatus: (Byte) -> Unit,
    private val status: Byte,
) :
    ISidedCooldownAction {
    private val circlePoints = buildCircle()
    private val enchantmentParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ORANGE)
        .colorVariation(0.2)
    private val eventScheduler = ModComponents.getWorldEventScheduler(world)

    override fun perform(): Int {
        sendStatus(status)
        placeRifts()
        return 80
    }

    private fun placeRifts() {
        val pos = entity.pos.add(VecUtils.yAxis.multiply(14.0))
        world.playSound(entity.pos, Mod.sounds.teleportPrepare, SoundCategory.HOSTILE, 1.0f, range = 64.0)

        eventScheduler.addEvent(TimedEvent({
            world.playSound(entity.pos, Mod.sounds.obsidilithBurst, SoundCategory.HOSTILE, 0.5f, range = 64.0)
        }, burstDelay, shouldCancel = { !entity.isAlive }))

        for (point in circlePoints) {
            val pos1 = BlockPos(pos.add(point))
            val groundPos = findGroundBelow(world, pos1)
            val up = groundPos.up()
            if (up.y + 28 >= pos.y && isOpenBlock(up)) {
                placeRift(up)
            }
        }
    }

    private fun placeRift(up: BlockPos) {
        if (world is ServerWorld) {
            world.spawnParticle(
                Particles.OBSIDILITH_INDICATOR,
                up.asVec3d().add(Vec3d(0.5, 0.1, 0.5)),
                Vec3d.ZERO
            )

            eventScheduler.addEvent(TimedEvent({
                val rand = world.random
                val columnVel = VecUtils.yAxis.multiply(rand.nextDouble() + 1).multiply(0.25)
                val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                var ticks = 0
                eventScheduler.addEvent(TimedEvent({
                    val impactPos = up.up(ticks)
                    world.spawnParticle(
                        Particles.OBSIDILITH_BURST,
                        impactPos.asVec3d().add(VecUtils.unit.multiply(0.5)),
                        columnVel
                    )

                    val box = Box(impactPos)
                    val entities = world.getEntitiesByClass(LivingEntity::class.java, box, null)

                    entities.forEach {
                        if (it != entity) {
                            it.sendVelocity(Vec3d(it.velocity.x, 1.0, it.velocity.z))
                            it.damage(
                                DamageSource.mob(entity),
                                damage
                            )
                        }
                    }

                    ticks += 2
                }, 0, 7))
            }, burstDelay, shouldCancel = { !isOpenBlock(up) || !entity.isAlive }))
        }
    }

    private fun isOpenBlock(up: BlockPos?) = world.getBlockState(up).canReplace(
        AutomaticItemPlacementContext(
            world,
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    )

    /**
     * From Maelstrom Mod ModUtils.java
     */
    private fun findGroundBelow(world: World, pos: BlockPos): BlockPos {
        for (i in pos.y downTo 1) {
            val tempPos = BlockPos(pos.x, i, pos.z)
            if (world.getBlockState(tempPos).isSideSolid(world, tempPos, Direction.UP, SideShapeType.FULL)) {
                return tempPos
            }
        }
        return BlockPos(pos.x, 0, pos.z)
    }

    private fun buildCircle(): List<Vec3d> {
        val radius = 7
        val radiusSq = radius * radius
        val points = mutableListOf<Vec3d>()
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val pos = Vec3d(x.toDouble(), 0.0, z.toDouble())
                if (pos.lengthSquared() < radiusSq) {
                    points.add(pos)
                }
            }
        }
        return points
    }

    override fun handleClientStatus() {
        for (i in 0..50) {
            val pos = entity.eyePos().add(RandomUtils.randVec().normalize().multiply(3.0))
            enchantmentParticleFactory.velocity {
                MathUtils.unNormedDirection(pos, entity.eyePos()).crossProduct(VecUtils.yAxis).multiply(0.1)
            }.build(pos)
        }
    }

    companion object {
        const val burstDelay = 30
    }
}