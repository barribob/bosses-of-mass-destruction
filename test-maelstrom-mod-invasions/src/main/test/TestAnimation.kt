package main.test

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.barribob.maelstrom.animation.client.Animation
import org.junit.Test
import kotlin.test.assertEquals

class TestAnimation {
    @Test
    fun testAnimationEnds() {
        val animationObject = JsonObject()
        animationObject.add("animation_length" , JsonPrimitive(0.1))
        val animation: Animation = Animation(animationObject)

        assertEquals(false, animation.isEnded())

        for(i in 1..20) {
            animation.update()
        }

        assertEquals(true, animation.isEnded())
    }
}