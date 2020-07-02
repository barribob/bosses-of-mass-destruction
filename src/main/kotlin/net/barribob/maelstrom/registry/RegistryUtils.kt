package net.barribob.maelstrom.registry

import net.barribob.maelstrom.render.EntityRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.MobEntity

@Environment(EnvType.CLIENT)
fun <T: MobEntity, M: EntityModel<T>> registerModRenderer (entity: EntityType<T>, entityModel: M, textures : String, shadowSize: Float = 0.5f) {
    EntityRendererRegistry.INSTANCE.register(entity) { entityRenderDispatcher, _ -> EntityRenderer(entityRenderDispatcher, entityModel, shadowSize, textures) }
}