package net.barribob.boss.mob.utils

import net.barribob.boss.render.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector4f
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.geo.render.built.GeoBone
import software.bernie.geckolib3.geo.render.built.GeoModel
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.model.provider.GeoModelProvider
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer
import software.bernie.geckolib3.renderer.geo.IGeoRenderer

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>,
    private val brightness: IRenderLight<T>? = null,
    private val iBoneLight: IBoneLight? = null,
    private val renderer: IRenderer<T>? = null,
    private val renderData: IRenderDataProvider<T>? = null, // Todo: this is unnecessary with IRenderer<T>
    private val renderWithModel: IRendererWithModel? = null,
    private val overlayOverride: IOverlayOverride? = null,
    private val deathRotation: Boolean = true
    ) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : IAnimatable, T : LivingEntity {
    private val renderHelper = GeoRenderer(modelProvider, ::getTexture)

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
    }

    override fun renderRecursively(
        bone: GeoBone,
        stack: MatrixStack?,
        bufferIn: VertexConsumer?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val packedLight = iBoneLight?.getLightForBone(bone, packedLightIn) ?: packedLightIn
        val color = Vector4f(red, green, blue, alpha)
        val newColor = iBoneLight?.getColorForBone(bone, color) ?: color
        super.renderRecursively(bone, stack, bufferIn, packedLight, packedOverlayIn, newColor.x, newColor.y, newColor.z, newColor.w)
    }

    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        renderData?.provide(entity, tickDelta)
        renderer?.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.push()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.pop()
    }

    override fun render(
        model: GeoModel,
        animatable: T,
        partialTicks: Float,
        type: RenderLayer,
        matrixStackIn: MatrixStack,
        renderTypeBuffer: VertexConsumerProvider,
        vertexBuilder: VertexConsumer?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        // Calling super here causes a noSuchMethodError for some reason
        val packetOverlay = overlayOverride?.getOverlay() ?: packedOverlayIn
        renderHelper.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packetOverlay, red, green, blue, alpha)
        renderWithModel?.render(model, partialTicks, type, matrixStackIn, renderTypeBuffer, packedLightIn, packetOverlay, red, green, blue, alpha)
    }

    override fun getDeathMaxRotation(entityLivingBaseIn: T): Float = if(deathRotation) 90f else 0f

    class GeoRenderer<T>(val geoModel: AnimatedGeoModel<T>, private val textureLocation: (T) -> Identifier) : IGeoRenderer<T>  where T : IAnimatable, T : LivingEntity  {
        override fun getGeoModelProvider(): GeoModelProvider<*> = geoModel
        override fun getTextureLocation(p0: T): Identifier = textureLocation(p0)
    }
}