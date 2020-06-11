package net.barribob.maelstrom.animation.client

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.barribob.maelstrom.MaelstromMod
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.Resource
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

class AnimationLoader {
    fun getAnimationObject(animationId: String): JsonObject {
        val s = animationId.split(".")
        if (s.size < 2) {
            MaelstromMod.LOGGER.error("Animation of id:$animationId is not formatted like <file.animation>")
            return JsonObject()
        }
        val filename = s[0]
        val animName = s[1]
        val loc = Identifier(MaelstromMod.MODID, "animations/$filename.json")
        val jsonParser = JsonParser()
        var resource: Resource? = null
        try {
            resource = MinecraftClient.getInstance().resourceManager.getResource(loc)
            val animationObject: JsonObject = jsonParser.parse(IOUtils.toString(resource.inputStream, StandardCharsets.UTF_8)).asJsonObject
            return animationObject.getAsJsonObject("animations").getAsJsonObject(animName)
        } catch (e: IOException) {
            MaelstromMod.LOGGER.error("Failed to load animation ${loc.path}: $e")
        } finally {
            IOUtils.closeQuietly(resource)
        }
        return JsonObject()
    }
}