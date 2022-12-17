package net.barribob.boss.render

import com.google.common.collect.Lists
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
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
import net.minecraft.util.math.RotationAxis
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

@Environment(EnvType.CLIENT)
open class ModGeoRenderer<T>(
    renderManager: EntityRendererFactory.Context?,
    private val modelProvider: GeoModel<T>,
    private val postRenderers: IRenderer<T>? = null,
    private val preRenderers: IRenderer<T>? = null,
    private val brightness: IRenderLight<T>? = null,
    private val overlayOverride: IOverlayOverride? = null,
    ) :
    EntityRenderer<T>(renderManager),
    GeoRenderer<T> where T : Entity, T : GeoAnimatable {
    private val layerRenderers: MutableList<GeoRenderLayer<T>> = Lists.newArrayList()
    private var animatableEntity: T? = null

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
        animatableEntity = entity
        val shouldSit = entity.hasVehicle() && entity.vehicle != null
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
        val predicate: AnimationState<T> = AnimationState(entity,
            limbSwing,
            limbSwingAmount,
            partialTicks,
            limbSwingAmount <= -0.15f || limbSwingAmount >= 0.15f)
        modelProvider.setCustomAnimations(entity, getInstanceId(entity), predicate)
        stack.push()
        preRenderers?.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn)
        stack.translate(0.0, 0.009999999776482582, 0.0)
        MinecraftClient.getInstance().textureManager.bindTexture(getTexture(entity))
        val model = modelProvider.getBakedModel(modelProvider.getModelResource(entity))
        val renderType = getRenderType(entity,getTexture(entity), bufferIn, partialTicks)
        this.defaultRender(
            stack,
            entity,
            bufferIn,
            renderType,
            null,
            entityYaw,
            partialTicks,
            packedLightIn)
        if (!entity.isSpectator) {
            val var20: MutableIterator<GeoRenderLayer<T>> = layerRenderers.iterator()
            while (var20.hasNext()) {
                val next = var20.next()
                val layerRenderer: GeoRenderLayer<T> = next
                layerRenderer.render(stack,
                    entity,
                    model,
                    renderType,
                    bufferIn,
                    null,
                    partialTicks,
                    packedLightIn,
                    overlayOverride?.getOverlay() ?: getPackedOverlay(0.0f))
            }
        }
        stack.pop()
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn)
        postRenderers?.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn)
    }

    override fun getPackedOverlay(animatable: T, u: Float): Int {
        return overlayOverride?.getOverlay() ?: getPackedOverlay(0.0f);
    }

    override fun getTexture(entity: T): Identifier {
        return modelProvider.getTextureResource(entity)
    }

    private fun applyRotations(
        entityLiving: T,
        matrixStackIn: MatrixStack,
        rotationYaw: Float,
    ) {
        val pose = entityLiving.pose
        if (pose != EntityPose.SLEEPING) {
            matrixStackIn.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationYaw))
        }
    }

    private fun handleRotationFloat(livingBase: T, partialTicks: Float): Float {
        return livingBase.age.toFloat() + partialTicks
    }

    override fun getTextureLocation(instance: T): Identifier = modelProvider.getTextureResource(instance)

    companion object Init {
        fun getPackedOverlay(uIn: Float): Int {
            return OverlayTexture.getUv(OverlayTexture.getU(uIn).toFloat(), false)
        }
    }

    override fun getGeoModel(): GeoModel<T> {
        return modelProvider
    }

    override fun getAnimatable(): T? {
        return animatableEntity
    }
}