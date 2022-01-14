package net.barribob.boss.damageSource

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.EntityDamageSource

class UnshieldableDamageSource(source: Entity?, name: String = "mob") : EntityDamageSource(name, source)