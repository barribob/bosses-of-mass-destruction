package net.barribob.maelstrom.animation.client

import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.mob.MobUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import java.util.*

/**
 * Manages animations of all entities. All that needs to be done is that it should be added to the entity to start the animation. Animations get automatically removed when the entity is removed or
 * dies.
 *
 * @author Barribob
 *
 */
class ClientAnimationWatcher {
    private val animations = HashMap<Entity, MutableMap<String, Animation>>()
    private val resetModelValues = HashMap<EntityModel<out Entity>, MutableMap<ModelPart, FloatArray>>()
    private val loader = AnimationLoader()

    fun startAnimation(entity: Entity, animationId: String) {
        if (!animations.containsKey(entity)) {
            animations[entity] = HashMap<String, Animation>()
        }
        if (!animations[entity]!!.containsKey(animationId)) {
            animations[entity]!![animationId] = Animation(loader.getAnimationObject(animationId))
        }
        animations[entity]!![animationId]!!.startAnimation()
    }

    // TODO: looping animations
    fun checkLoopingAnimation(entity: Entity, animationId: String) {
        if (!animations.containsKey(entity)) {
            animations[entity] = HashMap<String, Animation>()
        }
        if (!animations[entity]!!.containsKey(animationId)) {
            animations[entity]!![animationId] = Animation(loader.getAnimationObject(animationId))
        }
    }

    private fun removeAnimation(entity: Entity, animationId: String) {
        if (animations.containsKey(entity)) {
            if (animations[entity]!!.containsKey(animationId)) {
                animations[entity]!!.remove(animationId)
                // TODO: Additional logic needed to be able to smoothly chain consecutive animations
            }
        }
    }

    fun tick() {
        if (!MinecraftClient.getInstance().isPaused) {
            val entitiesToRemove: MutableList<Entity> = ArrayList<Entity>()
            for ((entity, value) in animations) {
                if (!entity.isAlive && MobUtils.isEntityInWorld(entity)) {
                    entitiesToRemove.add(entity)
                    continue
                }

                // Pause animation on death
                if (entity is LivingEntity && entity.health <= 0) {
                    continue
                }
                val animsToRemove: MutableList<String> = ArrayList()
                for ((key, value1) in value.entries) {
                    if (value1.isEnded()) {
                        animsToRemove.add(key)
                    } else {
                        value1.update()
                    }
                }

                // Remove ended animations
                for (id in animsToRemove) {
                    value.remove(id)
                }
            }

            // Remove entities not in this world anymore
            for (entity in entitiesToRemove) {
                animations.remove(entity)
            }
        }
    }

    /**
     * Resets the models positions and rotations back to what they originally were.
     *
     * This solves an issue that comes from the fact that all instances of a particular entity share the same model and thus will each alter the models values. Those values can carry over to the next
     * entity who uses the model, so those values have to be reset before each entity get rendered with the model.
     */
    fun resetModel(model: EntityModel<out Entity>) {
        val resetValues = resetModelValues[model] ?: return

        for ((renderer, values) in resetValues) {
            renderer.pitch = values[0]
            renderer.yaw = values[1]
            renderer.roll = values[2]
            renderer.pivotX = values[3]
            renderer.pivotY = values[4]
            renderer.pivotZ = values[5]
        }
    }

    /**
     * Record the model positions that the model was at before the animation modified it. That way
     * we can reset the model to its previous position for other entities that use the same model
     */
    private fun setResetModelPositions(model: EntityModel<out Entity>, parts: List<ModelPart>) {
        val resetValues = HashMap<ModelPart, FloatArray>()
        for (renderer in parts) {
            resetValues[renderer] = floatArrayOf(
                    renderer.pitch,
                    renderer.yaw,
                    renderer.roll,
                    renderer.pivotX,
                    renderer.pivotY,
                    renderer.pivotZ)
        }
        resetModelValues[model] = resetValues
    }

    fun setModelRotations(model: EntityModel<out Entity>, entity: Entity, partialTicks: Float) {
        if (animations.containsKey(entity)) {

            // Record the previous state of the modified parts to allow the model to be reset after it has been altered
            val modifiedParts = animations[entity]!!.values.flatMap { getModelParts(model, it) ?: listOf() }
            setResetModelPositions(model, modifiedParts)

            // Change the model rotations to the animation
            val ticks = if (entity is LivingEntity && entity.health <= 0f) 0f else partialTicks
            val animationData = animations[entity]!!.values.flatMap { it.getModelAnimations(ticks) }

            for ((data, part) in animationData zip modifiedParts) {
                part.pitch += data.first.x.toFloat()
                part.yaw += data.first.y.toFloat()
                part.roll += data.first.z.toFloat()
                part.pivotX -= data.second.x.toFloat()
                part.pivotY -= data.second.y.toFloat()
                part.pivotZ -= data.second.z.toFloat()
            }
        }
    }

    /**
     * Use reflection to match the java model's parts to the bedrock animation parts.
     */
    private fun getModelParts(model: EntityModel<out Entity>, animation: Animation): List<ModelPart>? {
        try {
            val result = animation.animationObject.getAsJsonObject("bones").entrySet()
                    .map { model::class.java.getDeclaredField(it.key) }
            result.forEach { if (!it.isAccessible) it.isAccessible = true }
            return result.map { it.get(model) as ModelPart }
        } catch (e: Exception) {
            when (e) {
                is NoSuchFieldException,
                is SecurityException,
                is IllegalAccessException -> {
                    val fieldNames = animation.animationObject.getAsJsonObject("bones").entrySet().map { it.key }
                    MaelstromMod.LOGGER.error("Animation failure: Failed to get animation fields: $fieldNames from model $model: ${e.message}")
                }
            }
        }

        return null
    }
}