package net.barribob.maelstrom.animation.client

import com.google.common.collect.Lists
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.util.math.Vec3d
import kotlin.math.floor

/**
 * Class that (sort of) implements minecraft bedrock animations
 *
 * @author Barribob
 *
 */
class Animation(val animationObject: JsonObject) {
    private var ticksSinceStart = 0
    private val animationLength: Double = animationObject.get("animation_length").asDouble
    private val loop: Boolean = animationObject.has("loop")

    fun getModelAnimations(partialTicks: Float): List<Pair<Vec3d, Vec3d>> {
        // Bedrock animations are quantified in seconds
        var timeInSeconds: Float

        // Looping is achieved by just using the remainder of the ticksSinceStart / animationLength
        if (loop) {
            timeInSeconds = (ticksSinceStart + partialTicks) * 0.05f
            val numRepetitions = floor(timeInSeconds / animationLength).toFloat()
            timeInSeconds -= (animationLength * numRepetitions).toFloat()
        } else {
            timeInSeconds = (ticksSinceStart + partialTicks) * 0.05f
        }

        return animationObject.getAsJsonObject("bones").entrySet().map { (_, value) ->
            val element = value.asJsonObject
            var rotation = Vec3d.ZERO
            var offset = Vec3d.ZERO

            if (element.has("rotation")) {
                val rotations = getInterpolatedValues(timeInSeconds, element.getAsJsonObject("rotation").entrySet())
                rotation = Vec3d(Math.toRadians(rotations[0]),
                        Math.toRadians(rotations[1]),
                        Math.toRadians(rotations[2]))
            }
            if (element.has("position")) {
                val offsets = getInterpolatedValues(timeInSeconds, element.getAsJsonObject("position").entrySet())
                offset = Vec3d(offsets[0], offsets[1], offsets[2])
            }

            Pair(rotation, offset)
        }
    }

    /**
     * Interpolates between some values. The equation is basically begin + ((end - begin) * progress) where progress is (time - timeBegin) / (clipLength), assuming that time is larger than timeBegin.
     *
     * @param timeInSeconds
     * @param set
     * @return
     */
    private fun getInterpolatedValues(timeInSeconds: Float, set: Set<Map.Entry<String, JsonElement>>): DoubleArray {
        val entries: List<Map.Entry<String, JsonElement>> = Lists.newArrayList(set)
        val finalValues = DoubleArray(3)

        // Sort the event because the animations aren't guaranteed to be in order
        entries.sortedBy { it.key.toFloat() }

        var begin = entries.findLast { it.key.toFloat() < timeInSeconds }
        var end = entries.find { it.key.toFloat() > timeInSeconds }

        if (begin == null && end == null) {
            return finalValues
        }

        if (begin == null) {
            begin = end
        } else if (end == null) {
            end = begin
        }

        val clipBegin = begin!!.key.toFloat()
        val clipEnd = end!!.key.toFloat()
        val clipLength = clipEnd - clipBegin
        val progress: Float = if (clipLength == 0f) 1f else (timeInSeconds - clipBegin) / clipLength
        val beginValues = begin.value.asJsonArray
        val endValues = end.value.asJsonArray

        for (i in 0..2) {
            val beginVal = beginValues[i].asDouble
            val endVal = endValues[i].asDouble
            finalValues[i] = beginVal + (endVal - beginVal) * progress
        }

        return finalValues
    }

    fun startAnimation() {
        ticksSinceStart = 0
    }

    fun update() {
        ticksSinceStart++
    }

    fun isEnded(): Boolean {
        return !loop && (this.ticksSinceStart * 0.05f) > this.animationLength
    }
}