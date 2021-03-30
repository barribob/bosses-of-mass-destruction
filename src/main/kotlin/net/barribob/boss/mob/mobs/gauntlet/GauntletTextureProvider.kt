package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.render.ITextureProvider
import net.minecraft.util.Identifier

class GauntletTextureProvider: ITextureProvider<GauntletEntity> {
    override fun getTexture(entity: GauntletEntity): Identifier {
        val texture =  if (entity.hurtTime > 0) {
            "gauntlet_hurt.png"
        } else {
            "gauntlet.png"
        }
        return Mod.identifier("textures/entity/$texture")
    }
}