package net.barribob.invasion.mob.utils

import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.entity.LivingEntity
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>?
) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : IAnimatable, T : LivingEntity