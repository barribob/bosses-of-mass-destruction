package net.barribob.boss.utils

import net.barribob.boss.utils.ModUtils.model
import net.barribob.boss.utils.ModUtils.normal
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.block.*
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.particle.BillboardParticle
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.DragonFireballEntityRenderer
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.GuardianEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.entity.mob.FlyingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.*
import net.minecraft.world.*
import net.minecraft.world.RaycastContext.FluidHandling
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

object VanillaCopies {

    fun create(world: World, key: RegistryKey<DamageType>, attacker: Entity?): DamageSource {
        return DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).getEntry(key).get(), attacker)
    }

    /**
     * [FlyingEntity.travel]
     */
    fun travel(movementInput: Vec3d, entity: LivingEntity, baseFrictionCoefficient: Float = 0.91f) {
        when {
            entity.isTouchingWater -> {
                entity.updateVelocity(0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(0.800000011920929)
            }
            entity.isInLava -> {
                entity.updateVelocity(0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(0.5)
            }
            else -> {
                val friction = if (entity.isOnGround) {
                    entity.world.getBlockState(BlockPos.ofFloored(entity.x, entity.y - 1.0, entity.z)).block
                        .slipperiness * baseFrictionCoefficient
                } else {
                    baseFrictionCoefficient
                }
                val g = 0.16277137f / (friction * friction * friction)

                entity.updateVelocity(if (entity.isOnGround) 0.1f * g else 0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(friction.toDouble())
            }
        }
        entity.updateLimbs(false)
    }

    /**
     * Adapted from [MobEntity.lookAtEntity]
     */
    fun MobEntity.lookAtTarget(target: Vec3d, maxYawChange: Float, maxPitchChange: Float) {
        val d: Double = target.x - this.x
        val e: Double = target.z - this.z
        val g: Double = target.y - this.eyeY

        val h = sqrt(d * d + e * e)
        val i = (MathHelper.atan2(e, d) * 57.2957763671875).toFloat() - 90.0f
        val j = (-(MathHelper.atan2(g, h) * 57.2957763671875)).toFloat()
        this.pitch = changeAngle(this.pitch, j, maxPitchChange)
        this.yaw = changeAngle(this.yaw, i, maxYawChange)
    }

    /**
     * [MobEntity.changeAngle]
     */
    private fun changeAngle(oldAngle: Float, newAngle: Float, maxChangeInAngle: Float): Float {
        var f = MathHelper.wrapDegrees(newAngle - oldAngle)
        if (f > maxChangeInAngle) {
            f = maxChangeInAngle
        }
        if (f < -maxChangeInAngle) {
            f = -maxChangeInAngle
        }
        return oldAngle + f
    }

    /**
     * [ClientPlayNetworkHandler.onEntitySpawn]
     */
    fun handleClientSpawnEntity(client: MinecraftClient, packet: EntitySpawnS2CPacket) {
        val d: Double = packet.x
        val e: Double = packet.y
        val f: Double = packet.z
        val entityType = packet.entityType
        val world = client.world ?: return

        val entity15 = entityType.create(world)

        if (entity15 != null) {
            val i: Int = packet.id
            entity15.updateTrackedPosition(d, e, f)
            entity15.refreshPositionAfterTeleport(d, e, f)
            entity15.pitch = (packet.pitch * 360) / 256.0f
            entity15.yaw = (packet.yaw * 360) / 256.0f
            entity15.id = i
            entity15.uuid = packet.uuid
            world.addEntity(i, entity15 as Entity?)
        }
    }

    /**
     * [DragonFireballEntityRenderer.render]
     */
    fun renderBillboard(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        dispatcher: EntityRenderDispatcher,
        layer: RenderLayer,
        rotation: Quaternionf = Quaternionf()
    ) {
        matrixStack.push()
        matrixStack.multiply(dispatcher.rotation)
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f))
        matrixStack.multiply(rotation)
        val entry = matrixStack.peek()
        val matrix4f = entry.model
        val matrix3f = entry.normal
        val vertexConsumer = vertexConsumerProvider.getBuffer(layer)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 0, 0, 1)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 0, 1, 1)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 1, 1, 0)
        produceVertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 1, 0, 0)
        matrixStack.pop()
    }

    /**
     * [DragonFireballEntityRenderer.produceVertex]
     */
    private fun produceVertex(
        vertexConsumer: VertexConsumer,
        modelMatrix: Matrix4f,
        normalMatrix: Matrix3f,
        light: Int,
        x: Float,
        y: Int,
        textureU: Int,
        textureV: Int,
    ) {
        vertexConsumer.vertex(modelMatrix, x - 0.5f, y.toFloat() - 0.25f, 0.0f).color(255, 255, 255, 255)
            .texture(
                textureU.toFloat(),
                textureV.toFloat()
            ).overlay(OverlayTexture.DEFAULT_UV).light(light)
            .normal(normalMatrix, 0.0f, 1.0f, 0.0f).next()
    }

    /**
     * [LivingEntity.canSee]
     */
    fun hasDirectLineOfSight(to: Vec3d, from: Vec3d, world: BlockView, entity: Entity): Boolean {
        return world.raycast(
            RaycastContext(
                to,
                from,
                RaycastContext.ShapeType.COLLIDER,
                FluidHandling.NONE,
                entity
            )
        ).type == HitResult.Type.MISS
    }

    /**
     * [EntityRenderer.getBlockLight]
     */
    fun getBlockLight(entity: Entity, blockPos: BlockPos?): Int {
        return if (entity.isOnFire) 15 else entity.world.getLightLevel(LightType.BLOCK, blockPos)
    }

    /**
     * Adapted from [EnderDragonEntity.awardExperience]
     *
     * No longer is present perhaps changed to [EnderDragonEntity.updatePostDeath]?
     */
    fun awardExperience(amount: Int, pos: Vec3d, world: World) {
        var amt = amount
        while (amt > 0) {
            val i = ExperienceOrbEntity.roundToOrbSize(amt)
            amt -= i
            world.spawnEntity(ExperienceOrbEntity(world, pos.x, pos.y, pos.z, i))
        }
    }

    /**
     * [BillboardParticle.buildGeometry] without rotation
     */
    fun buildFlatGeometry(
        camera: Camera, tickDelta: Float,
        prevPosX: Double,
        prevPosY: Double,
        prevPosZ: Double,
        x: Double,
        y: Double,
        z: Double,
        scale: Float,
        rotation: Float
    ): Array<Vector3f> {
        val vec3d = camera.pos
        val f = (MathHelper.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.getX()).toFloat()
        val g = (MathHelper.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.getY()).toFloat()
        val h = (MathHelper.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.getZ()).toFloat()

        val vector3fs = arrayOf(
            Vector3f(-1.0f, 0.0f, -1.0f),
            Vector3f(-1.0f, 0.0f, 1.0f),
            Vector3f(1.0f, 0.0f, 1.0f),
            Vector3f(1.0f, 0.0f, -1.0f)
        )

        for (k in 0..3) {
            val vector3f2 = vector3fs[k]
            vector3f2.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(rotation))
            vector3f2.mul(scale)
            vector3f2.add(f, g, h)
        }

        return vector3fs
    }

    /**
     * [BillboardParticle.buildGeometry]
     */
    fun buildBillboardGeometry(
        camera: Camera, tickDelta: Float,
        prevPosX: Double,
        prevPosY: Double,
        prevPosZ: Double,
        x: Double,
        y: Double,
        z: Double,
        scale: Float,
        rotation: Float
    ): Array<Vector3f> {
        val vec3d = camera.pos
        val f = (MathHelper.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.getX()).toFloat()
        val g = (MathHelper.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.getY()).toFloat()
        val h = (MathHelper.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.getZ()).toFloat()
        val quaternion2: Quaternionf = camera.rotation

        val vector3fs = arrayOf(
            Vector3f(-1.0f, -1.0f, 0.0f),
            Vector3f(-1.0f, 1.0f, 0.0f),
            Vector3f(1.0f, 1.0f, 0.0f),
            Vector3f(1.0f, -1.0f, 0.0f)
        )

        for (k in 0..3) {
            val vector3f2 = vector3fs[k]
            vector3f2.rotate(RotationAxis.POSITIVE_Z.rotationDegrees(rotation))
            vector3f2.rotate(quaternion2)
            vector3f2.mul(scale)
            vector3f2.add(f, g, h)
        }

        return vector3fs
    }

    /**
     * [EnderDragonEntity.destroyBlocks]
     */
    fun Entity.destroyBlocks(box: Box): Boolean {
        val i = MathHelper.floor(box.minX)
        val j = MathHelper.floor(box.minY)
        val k = MathHelper.floor(box.minZ)
        val l = MathHelper.floor(box.maxX)
        val m = MathHelper.floor(box.maxY)
        val n = MathHelper.floor(box.maxZ)
        var bl = false
        var bl2 = false
        for (o in i..l) {
            for (p in j..m) {
                for (q in k..n) {
                    val blockPos = BlockPos(o, p, q)
                    val blockState: BlockState = this.world.getBlockState(blockPos)
                    if (!blockState.isAir && blockState.block == Blocks.FIRE) {
                        if (this.world.gameRules.getBoolean(GameRules.DO_MOB_GRIEFING)
                            && !blockState.isIn(BlockTags.WITHER_IMMUNE)
                        ) {
                            bl2 = this.world.breakBlock(blockPos, false) || bl2
                        } else {
                            bl = true
                        }
                    }
                }
            }
        }
        return bl
    }

    /**
     * [GuardianEntityRenderer.render]
     */
    fun renderBeam(actor: LivingEntity, target: Vec3d, prevTarget: Vec3d, tickDelta: Float, color: Vec3d, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, layer: RenderLayer) {
            val j: Float = actor.world.time.toFloat() + tickDelta
            val k = j % 1.0f
            val l: Float = actor.standingEyeHeight
            matrixStack.push()
            matrixStack.translate(0.0, l.toDouble(), 0.0)
            val vec3d: Vec3d = MathUtils.lerpVec(tickDelta, prevTarget, target)
            val vec3d2: Vec3d = this.fromLerpedPosition(actor, l.toDouble(), tickDelta)
            var vec3d3 = vec3d.subtract(vec3d2)
            val m = vec3d3.length().toFloat()
            vec3d3 = vec3d3.normalize()
            val n = acos(vec3d3.y).toFloat()
            val o = atan2(vec3d3.z, vec3d3.x).toFloat()
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - o) * 57.295776f))
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 57.295776f))
            val q = j * 0.05f * -1.5f
            val red = (color.x * 255).toInt()
            val green = (color.y * 255).toInt()
            val blue = (color.z * 255).toInt()
            val x = MathHelper.cos(q + 2.3561945f) * 0.282f
            val y = MathHelper.sin(q + 2.3561945f) * 0.282f
            val z = MathHelper.cos(q + 0.7853982f) * 0.282f
            val aa = MathHelper.sin(q + 0.7853982f) * 0.282f
            val ab = MathHelper.cos(q + 3.926991f) * 0.282f
            val ac = MathHelper.sin(q + 3.926991f) * 0.282f
            val ad = MathHelper.cos(q + 5.4977875f) * 0.282f
            val ae = MathHelper.sin(q + 5.4977875f) * 0.282f
            val af = MathHelper.cos(q + 3.1415927f) * 0.2f
            val ag = MathHelper.sin(q + 3.1415927f) * 0.2f
            val ah = MathHelper.cos(q + 0.0f) * 0.2f
            val ai = MathHelper.sin(q + 0.0f) * 0.2f
            val aj = MathHelper.cos(q + 1.5707964f) * 0.2f
            val ak = MathHelper.sin(q + 1.5707964f) * 0.2f
            val al = MathHelper.cos(q + 4.712389f) * 0.2f
            val am = MathHelper.sin(q + 4.712389f) * 0.2f
            val aq = -1.0f - k // Negated K to reverse direction of laser movement
            val ar = m * 2.5f + aq
            val vertexConsumer: VertexConsumer = vertexConsumerProvider.getBuffer(layer)
            val entry: MatrixStack.Entry = matrixStack.peek()
            val matrix4f = entry.model
            val matrix3f = entry.normal
            method_23173(vertexConsumer, matrix4f, matrix3f, af, m, ag, red, green, blue, 0.4999f, ar)
            method_23173(vertexConsumer, matrix4f, matrix3f, af, 0.0f, ag, red, green, blue, 0.4999f, aq)
            method_23173(vertexConsumer, matrix4f, matrix3f, ah, 0.0f, ai, red, green, blue, 0.0f, aq)
            method_23173(vertexConsumer, matrix4f, matrix3f, ah, m, ai, red, green, blue, 0.0f, ar)
            method_23173(vertexConsumer, matrix4f, matrix3f, aj, m, ak, red, green, blue, 0.4999f, ar)
            method_23173(vertexConsumer, matrix4f, matrix3f, aj, 0.0f, ak, red, green, blue, 0.4999f, aq)
            method_23173(vertexConsumer, matrix4f, matrix3f, al, 0.0f, am, red, green, blue, 0.0f, aq)
            method_23173(vertexConsumer, matrix4f, matrix3f, al, m, am, red, green, blue, 0.0f, ar)
            var `as` = 0.0f
            if (actor.age % 2 == 0) {
                `as` = 0.5f
            }
            method_23173(vertexConsumer, matrix4f, matrix3f, x, m, y, red, green, blue, 0.5f, `as` + 0.5f)
            method_23173(
                vertexConsumer,
                matrix4f,
                matrix3f,
                z,
                m,
                aa,
                red,
                green,
                blue,
                1.0f,
                `as` + 0.5f
            )
            method_23173(vertexConsumer, matrix4f, matrix3f, ad, m, ae, red, green, blue, 1.0f, `as`)
            method_23173(vertexConsumer, matrix4f, matrix3f, ab, m, ac, red, green, blue, 0.5f, `as`)
            matrixStack.pop()
    }

    /**
     * [GuardianEntityRenderer.method_23173]
     */
    fun method_23173(
        vertexConsumer: VertexConsumer,
        matrix4f: Matrix4f,
        matrix3f: Matrix3f,
        f: Float,
        g: Float,
        h: Float,
        i: Int,
        j: Int,
        k: Int,
        l: Float,
        m: Float
    ) {
        vertexConsumer.vertex(matrix4f, f, g, h)
            .color(i, j, k, 255)
            .texture(l, m)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728800)
            .normal(matrix3f, 0.0f, 0.0f, -1.0f) // Changed from normal(0, 1, 0) because that brightened it for some reason that I cannot fathom with my pathetic opengl knowledge
            .next()
    }

    /**
     * [GuardianEntityRenderer.fromLerpedPosition]
     */
    fun fromLerpedPosition(entity: LivingEntity, yOffset: Double, delta: Float): Vec3d {
        val d = MathHelper.lerp(delta.toDouble(), entity.lastRenderX, entity.x)
        val e = MathHelper.lerp(delta.toDouble(), entity.lastRenderY, entity.y) + yOffset
        val f = MathHelper.lerp(delta.toDouble(), entity.lastRenderZ, entity.z)
        return Vec3d(d, e, f)
    }

    /**
     * [TallPlantBlock.onBreakInCreative]
     */
    fun onBreakInCreative(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        val doubleBlockHalf = state.get(TallPlantBlock.HALF) as DoubleBlockHalf
        if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
            val blockPos = pos.down()
            val blockState = world.getBlockState(blockPos)
            if (blockState.block === state.block && blockState.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(blockPos, Blocks.AIR.defaultState, 35)
                world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState))
            }
        }
    }
}