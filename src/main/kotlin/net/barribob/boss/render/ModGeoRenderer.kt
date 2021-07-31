package net.barribob.boss.render

import com.google.common.collect.Lists
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPose
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.model.provider.GeoModelProvider
import software.bernie.geckolib3.model.provider.data.EntityModelData
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer
import software.bernie.geckolib3.renderers.geo.IGeoRenderer

@Environment(EnvType.CLIENT)
open class ModGeoRenderer<T>(
    renderManager: EntityRendererFactory.Context?,
    private val modelProvider: AnimatedGeoModel<T>,
    private val additionalRenderers: IRenderer<T>? = null,
    private val brightness: IRenderLight<T>? = null,
) :
    EntityRenderer<T>(renderManager),
    IGeoRenderer<T> where T : Entity, T : IAnimatable {
    private val layerRenderers: MutableList<GeoLayerRenderer<T>> = Lists.newArrayList()

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
    }

    // Todo: move a lot of this stuff into IRenderers so we can mix and match and eventually get rid of SimpleLivingGeoRenderer
    override fun render(
        entity: T,
        entityYaw: Float,
        partialTicks: Float,
        stack: MatrixStack,
        bufferIn: VertexConsumerProvider,
        packedLightIn: Int,
    ) {
        val shouldSit = entity.hasVehicle() && entity.vehicle != null
        val entityModelData = EntityModelData()
        entityModelData.isSitting = shouldSit
        var f = MathHelper.lerpAngleDegrees(partialTicks, entity.prevYaw, entity.yaw)
        val f1 = 0.0f
        var f2 = f1 - f
        var f7: Float
        if (shouldSit && entity.vehicle is LivingEntity) {
            val livingentity = entity.vehicle as LivingEntity?
            f = MathHelper.lerpAngleDegrees(partialTicks, livingentity!!.prevBodyYaw, livingentity.bodyYaw)
            f2 = f1 - f
            f7 = MathHelper.wrapDegrees(f2)
            if (f7 < -85.0f) {
                f7 = -85.0f
            }
            if (f7 >= 85.0f) {
                f7 = 85.0f
            }
            f = f1 - f7
            if (f7 * f7 > 2500.0f) {
                f += f7 * 0.2f
            }
            f2 = f1 - f
        }
        val f6 = MathHelper.lerp(partialTicks, entity.prevPitch, entity.pitch)
        f7 = handleRotationFloat(entity, partialTicks)
        applyRotations(entity, stack, f)
        val limbSwingAmount = 0.0f
        val limbSwing = 0.0f
        val predicate: AnimationEvent<*> = AnimationEvent(entity,
            limbSwing,
            limbSwingAmount,
            partialTicks,
            limbSwingAmount <= -0.15f || limbSwingAmount >= 0.15f,
            listOf(entityModelData))
        modelProvider.setLivingAnimations(entity, getUniqueID(entity), predicate)
        stack.push()
        stack.translate(0.0, 0.009999999776482582, 0.0)
        MinecraftClient.getInstance().textureManager.bindTexture(getTexture(entity))
        val model = modelProvider.getModel(modelProvider.getModelLocation(entity))
        val renderColor = getRenderColor(entity, partialTicks, stack, bufferIn, null as VertexConsumer?, packedLightIn)
        val renderType = getRenderType(entity, partialTicks, stack, bufferIn, null as VertexConsumer?, packedLightIn,
            getTexture(entity))
        this.render(model,
            entity,
            partialTicks,
            renderType,
            stack,
            bufferIn,
            null as VertexConsumer?,
            packedLightIn,
            getPackedOverlay(0.0f),
            renderColor.red
                .toFloat() / 255.0f,
            renderColor.blue.toFloat() / 255.0f,
            renderColor.green.toFloat() / 255.0f,
            renderColor.alpha
                .toFloat() / 255.0f)
        if (!entity.isSpectator) {
            val var20: MutableIterator<GeoLayerRenderer<T>> = layerRenderers.iterator()
            while (var20.hasNext()) {
                val next = var20.next()
                val layerRenderer: GeoLayerRenderer<T> = next
                layerRenderer.render(stack,
                    bufferIn,
                    packedLightIn,
                    entity,
                    limbSwing,
                    limbSwingAmount,
                    partialTicks,
                    f7,
                    f2,
                    f6)
            }
        }
        stack.pop()
        super<EntityRenderer>.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn)
        additionalRenderers?.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn)
    }

    override fun getTexture(entity: T): Identifier {
        return modelProvider.getTextureLocation(entity)
    }

    override fun getGeoModelProvider(): GeoModelProvider<*> {
        return modelProvider
    }

    private fun applyRotations(
        entityLiving: T,
        matrixStackIn: MatrixStack,
        rotationYaw: Float,
    ) {
        val pose = entityLiving.pose
        if (pose != EntityPose.SLEEPING) {
            matrixStackIn.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw))
        }
    }

    private fun handleRotationFloat(livingBase: T, partialTicks: Float): Float {
        return livingBase.age.toFloat() + partialTicks
    }

    override fun getTextureLocation(instance: T): Identifier {
        return modelProvider.getTextureLocation(instance)
    }

    fun addLayer(layer: GeoLayerRenderer<T>): Boolean {
        return layerRenderers.add(layer)
    }

    companion object Init {
        fun getPackedOverlay(uIn: Float): Int {
            return OverlayTexture.getUv(OverlayTexture.getU(uIn).toFloat(), false)
        }
    }
}