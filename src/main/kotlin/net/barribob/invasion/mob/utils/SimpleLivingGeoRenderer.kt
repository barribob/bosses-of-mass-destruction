package net.barribob.invasion.mob.utils

import net.barribob.invasion.render.IRenderLight
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>?,
    private val brightness: IRenderLight<T>? = null
    ) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : IAnimatable, T : LivingEntity {

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
    }
}