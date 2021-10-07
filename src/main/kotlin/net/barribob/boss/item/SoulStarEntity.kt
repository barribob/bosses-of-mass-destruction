package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.maelstrom.general.event.Event
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.rotateVector
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvironmentInterface
import net.fabricmc.api.EnvironmentInterfaces
import net.minecraft.entity.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Sourced from [EyeOfEnderEntity]
 */
class SoulStarEntity(entityType: EntityType<out SoulStarEntity?>?, world: World?) :
    Entity(entityType, world), FlyingItemEntity {
    private var targetX = 0.0
    private var targetY = 0.0
    private var targetZ = 0.0

    constructor(world: World?, x: Double, y: Double, z: Double) : this(Entities.SOUL_STAR, world) {
        updatePosition(x, y, z)
    }

    fun setItem(stack: ItemStack) {
        if (stack.item !== Items.ENDER_EYE || stack.hasNbt()) {
            getDataTracker().set(
                ITEM, Util.make(stack.copy()) { it.count = 1 }
            )
        }
    }

    private fun getTrackedItem(): ItemStack = getDataTracker().get(ITEM) as ItemStack

    override fun getStack(): ItemStack {
        val itemStack = getTrackedItem()
        return if (itemStack.isEmpty) ItemStack(Items.ENDER_EYE) else itemStack
    }

    override fun initDataTracker() {
        getDataTracker().startTracking(ITEM, ItemStack.EMPTY)
    }

    @Environment(EnvType.CLIENT)
    override fun shouldRender(distance: Double): Boolean {
        var d = this.boundingBox.averageSideLength * 4.0
        if (java.lang.Double.isNaN(d)) {
            d = 4.0
        }
        d *= 64.0
        return distance < d * d
    }

    /**
     * @param pos the block the eye of ender is drawn towards
     */
    fun initTargetPos(pos: BlockPos) {
        val d = pos.x.toDouble()
        val e = pos.z.toDouble()
        val f = d - this.x
        val g = e - this.z
        val h = sqrt(f * f + g * g)
        targetX = this.x + f / h * 12.0
        targetZ = this.z + g / h * 12.0
        targetY = this.y + 8.0
    }

    @Environment(EnvType.CLIENT)
    override fun setVelocityClient(x: Double, y: Double, z: Double) {
        this.setVelocity(x, y, z)
        if (prevPitch == 0.0f && prevYaw == 0.0f) {
            val f = sqrt(x * x + z * z)
            yaw = (MathHelper.atan2(x, z) * 57.2957763671875).toFloat()
            pitch = (MathHelper.atan2(y, f) * 57.2957763671875).toFloat()
            prevYaw = yaw
            prevPitch = pitch
        }
    }

    override fun tick() {
        if(world.isClient && age == 1) {
            val rotationOffset = random.nextDouble() * 360
            ModComponents.getWorldEventScheduler(world)
                .addEvent(Event({ true }, { spawnTrailParticles(rotationOffset) }, { isRemoved }))
        }

        super.tick()
        var vec3d = velocity
        val d = this.x + vec3d.x
        val e = this.y + vec3d.y
        val f = this.z + vec3d.z
        val g = vec3d.horizontalLength()
        pitch = updateRotation(
            prevPitch, (MathHelper.atan2(
                vec3d.y,
                g
            ) * 57.2957763671875).toFloat()
        )
        yaw = updateRotation(
            prevYaw,
            (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        )
        if (!world.isClient) {
            val xd = targetX - d
            val zd = targetZ - f
            val distance = sqrt(xd * xd + zd * zd).toFloat()
            val k = MathHelper.atan2(zd, xd).toFloat()
            var l = MathHelper.lerp(0.0025, g, distance.toDouble())
            var m = vec3d.y
            if (distance < 1.0f) {
                l *= 0.8
                m *= 0.8

                playSound(Mod.sounds.soulStar, 1.0f, 1.0f)
                discard()
                world.spawnEntity(ItemEntity(world, this.x, this.y, this.z, this.stack))
            }
            val n = if (this.y < targetY) 1 else -1
            vec3d = Vec3d(
                cos(k.toDouble()) * l, m + (n.toDouble() - m) * 0.014999999664723873, sin(
                    k.toDouble()
                ) * l
            )
            velocity = vec3d

            updatePosition(d, e, f)
        } else {
            setPos(d, e, f)
        }

        spawnParticles(d, vec3d, e, f)
    }

    private val particleBuilder = ClientParticleBuilder(Particles.SPARKLES)
        .color(LichUtils.blueColorFade)
        .age{RandomUtils.range(80, 100)}
        .colorVariation(0.3)
        .scale { 0.05f - (it * 0.025f) }
        .brightness(Particles.FULL_BRIGHT)

    private fun spawnTrailParticles(rotationOffset: Double) {
        val look = velocity
        val cross = look.crossProduct(VecUtils.yAxis).normalize()
        val rotatedOffset = cross.rotateVector(look, rotationOffset + age * 30.0).multiply(0.25)
        val particlePos = pos.add(rotatedOffset)
        particleBuilder.build(particlePos, velocity.multiply(0.1))
    }

    private fun spawnParticles(d: Double, vec3d: Vec3d, e: Double, f: Double) {
        if (this.isTouchingWater) {
            for (p in 0..3) {
                world.addParticle(
                    ParticleTypes.BUBBLE,
                    d - vec3d.x * 0.25,
                    e - vec3d.y * 0.25,
                    f - vec3d.z * 0.25,
                    vec3d.x,
                    vec3d.y,
                    vec3d.z
                )
            }
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        val itemStack: ItemStack = this.getTrackedItem()
        if (!itemStack.isEmpty) {
            nbt.put("Item", itemStack.writeNbt(NbtCompound()))
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        val itemStack = ItemStack.fromNbt(nbt.getCompound("Item"))
        setItem(itemStack)
    }

    override fun getBrightnessAtEyes(): Float = 1.0f
    override fun isAttackable(): Boolean = false
    override fun createSpawnPacket(): Packet<*> =  Mod.networkUtils.createClientEntityPacket(this)

    /**
     * [ProjectileEntity.updateRotation]
     */
    private fun updateRotation(f: Float, g: Float): Float {
        var f1 = f
        while (g - f1 < -180.0f) {
            f1 -= 360.0f
        }
        while (g - f1 >= 180.0f) {
            f1 += 360.0f
        }
        return MathHelper.lerp(0.2f, f1, g)
    }

    companion object {
        private val ITEM: TrackedData<ItemStack>? = DataTracker.registerData(SoulStarEntity::class.java, TrackedDataHandlerRegistry.ITEM_STACK)
    }
}