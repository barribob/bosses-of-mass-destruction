package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.mob.mobs.gauntlet.GauntletMoveLogic.Companion.blindnessPercentage
import net.barribob.boss.render.ITextureProvider
import net.minecraft.util.Identifier

class GauntletTextureProvider: ITextureProvider<GauntletEntity> {
    override fun getTexture(entity: GauntletEntity): Identifier {
        val healthRatio = entity.health / entity.maxHealth
        val texture =  if (entity.hurtTime > 0) {
            if(healthRatio < blindnessPercentage) "gauntlet2_hurt.png" else "gauntlet_hurt.png"
        } else {
            if(healthRatio < blindnessPercentage) "gauntlet2.png" else "gauntlet.png"
        }
        return Mod.identifier("textures/entity/$texture")
    }
}