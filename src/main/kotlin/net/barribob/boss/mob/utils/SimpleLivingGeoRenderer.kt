package net.barribob.boss.mob.utils

import net.barribob.boss.render.*
import net.barribob.boss.utils.ModUtils.model
import net.barribob.boss.utils.ModUtils.normal
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vector4f
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.geo.render.built.GeoBone
import software.bernie.geckolib3.geo.render.built.GeoCube
import software.bernie.geckolib3.geo.render.built.GeoModel
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.model.provider.GeoModelProvider
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer
import software.bernie.geckolib3.renderers.geo.IGeoRenderer
import software.bernie.geckolib3.util.RenderUtils

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRendererFactory.Context?,
    modelProvider: AnimatedGeoModel<T>,
    private val brightness: IRenderLight<T>? = null,
    private val iBoneLight: IBoneLight? = null,
    private val renderer: IRenderer<T>? = null,
    private val renderWithModel: IRendererWithModel? = null,
    private val overlayOverride: IOverlayOverride? = null,
    private val renderLayer: RenderLayer? = null,
    private val deathRotation: Boolean = true
    ) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : IAnimatable, T : LivingEntity {
    private val renderHelper = GeoRenderer(modelProvider, ::getTexture, this)

    override fun getRenderType(
        animatable: T,
        partialTicks: Float,
        stack: MatrixStack?,
        renderTypeBuffer: VertexConsumerProvider?,
        vertexBuilder: VertexConsumer?,
        packedLightIn: Int,
        textureLocation: Identifier?
    ): RenderLayer {
        return renderLayer
            ?: renderHelper.getRenderType(
                animatable,
                partialTicks,
                stack,
                renderTypeBuffer,
                vertexBuilder,
                packedLightIn,
                textureLocation
            )
    }

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

    override fun renderCube(
        cube: GeoCube, stack: MatrixStack, bufferIn: VertexConsumer, packedLightIn: Int,
        packedOverlayIn: Int, red: Float, green: Float, blue: Float, alpha: Float
    ) {
        RenderUtils.moveToPivot(cube, stack)
        RenderUtils.rotate(cube, stack)
        RenderUtils.moveBackFromPivot(cube, stack)
        val matrix3f = stack.peek().normal
        val matrix4f = stack.peek().model

        for (quad in cube.quads) {
            if (quad == null) {
                continue
            }
            val normal = quad.normal.copy()
            normal.transform(matrix3f)
            for (vertex in quad.vertices) {
                val vector4f = Vector4f(
                    vertex.position.x, vertex.position.y, vertex.position.z,
                    1.0f
                )
                vector4f.transform(matrix4f)
                bufferIn.vertex(
                    vector4f.x, vector4f.y, vector4f.z, red, green, blue, alpha,
                    vertex.textureU, vertex.textureV, packedOverlayIn, packedLightIn, normal.x, normal.y,
                    normal.z
                )
            }
        }
    }

    override fun getDeathMaxRotation(entityLivingBaseIn: T): Float = if(deathRotation) 90f else 0f

    class GeoRenderer<T>(
        val geoModel: AnimatedGeoModel<T>,
        private val textureLocation: (T) -> Identifier,
        private val renderer: SimpleLivingGeoRenderer<T>
    ) :
        IGeoRenderer<T> where T : IAnimatable, T : LivingEntity {
        override fun getGeoModelProvider(): GeoModelProvider<*> = geoModel
        override fun getTextureResource(p0: T): Identifier = textureLocation(p0)
        override fun renderRecursively(
            bone: GeoBone,
            stack: MatrixStack,
            bufferIn: VertexConsumer,
            packedLightIn: Int,
            packedOverlayIn: Int,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float
        ) {
            renderer.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha)
        }
    }
}