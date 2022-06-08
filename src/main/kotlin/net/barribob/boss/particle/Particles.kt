package net.barribob.boss.particle

import net.barribob.boss.Mod
import net.barribob.boss.mob.mobs.obsidilith.BurstAction
import net.barribob.boss.mob.mobs.obsidilith.PillarAction
import net.barribob.boss.mob.mobs.obsidilith.WaveAction
import net.barribob.boss.mob.mobs.void_blossom.SpikeAction.Companion.indicatorDelay
import net.barribob.boss.mob.mobs.void_blossom.SpikeWaveAction
import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import kotlin.math.sin

object Particles {
    val DISAPPEARING_SWIRL: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("disappearing_swirl"),
        ModParticleType(true)
    )

    val SOUL_FLAME: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("soul_flame"),
        ModParticleType(true)
    )

    val LICH_MAGIC_CIRCLE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("magic_circle"),
        ModParticleType(true)
    )

    val OBSIDILITH_BURST: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_burst"),
        ModParticleType(true)
    )

    val ENCHANT: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("enchant"),
        ModParticleType(true)
    )

    val OBSIDILITH_BURST_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_burst_indicator"),
        ModParticleType(true)
    )

    val OBSIDILITH_WAVE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_wave"),
        ModParticleType(true)
    )

    val OBSIDILITH_WAVE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_wave_indicator"),
        ModParticleType(true)
    )

    val DOWNSPARKLE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("downsparkle"),
        ModParticleType(true)
    )

    val OBSIDILITH_SPIKE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_spike_indicator"),
        ModParticleType(true)
    )

    val OBSIDILITH_SPIKE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_spike"),
        ModParticleType(true)
    )

    val PILLAR_RUNE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("pillar_rune"),
        ModParticleType(true)
    )

    val PILLAR_SPAWN_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("pillar_spawn_indicator"),
        ModParticleType(true)
    )

    val PILLAR_SPAWN_INDICATOR_2: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("pillar_spawn_indicator_2"),
        ModParticleType(true)
    )

    val OBSIDILITH_ANVIL_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("obsidilith_anvil_indicator"),
        ModParticleType(true)
    )

    val SPARKLES: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("sparkles"),
        ModParticleType(true)
    )

    val GAUNTLET_REVIVE_SPARKLES: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("gauntlet_revive_sparkles"),
        ModParticleType(true)
    )

    val EYE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("eye_open"),
        ModParticleType(true)
    )

    val LINE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("line"),
        ModParticleType(true)
    )

    val VOID_BLOSSOM_SPIKE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("void_blossom_spike_indicator"),
        ModParticleType(true)
    )

    val VOID_BLOSSOM_SPIKE_WAVE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("void_blossom_spike_wave_indicator"),
        ModParticleType(true)
    )

    val PETAL: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("petal"),
        ModParticleType(true)
    )

    val SPORE: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("spore"),
        ModParticleType(true)
    )

    val SPORE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("spore_indicator"),
        ModParticleType(true)
    )

    val FLUFF: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("fluff"),
        ModParticleType(false)
    )

    val POLLEN: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("pollen"),
        ModParticleType(true)
    )

    val EARTHDIVE_INDICATOR: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("earthdive_indicator"),
        ModParticleType(false)
    )

    val ROD: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("rod"),
        ModParticleType(false)
    )

    val HORIZONTAL_ROD: DefaultParticleType = Registry.register(
        Registry.PARTICLE_TYPE,
        Mod.identifier("ground_rod"),
        ModParticleType(false)
    )

    const val FULL_BRIGHT = 15728880

    fun clientInit() {
        val particleFactory = ParticleFactoryRegistry.getInstance()

        particleFactory.register(DISAPPEARING_SWIRL) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(SOUL_FLAME) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
                particle.setColorOverride { ModColors.COMET_BLUE }
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle
            }
        }

        particleFactory.register(LICH_MAGIC_CIRCLE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, 40, VanillaCopies::buildBillboardGeometry)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.scale(4f)
                particle
            }
        }

        particleFactory.register(OBSIDILITH_BURST) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(7, 15), VanillaCopies::buildBillboardGeometry)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.scale(4f)
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.ORANGE, ModColors.RUNIC_BROWN) }
                particle
            }
        }

        particleFactory.register(ENCHANT) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle =
                    SimpleParticle(context, RandomUtils.range(30, 50), VanillaCopies::buildBillboardGeometry, false)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (sin(it.toDouble() * Math.PI) + 1f).toFloat() * 0.1f }
                particle
            }
        }

        particleFactory.register(OBSIDILITH_BURST_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    BurstAction.burstDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.ORANGE }
                particle.setColorVariation(0.3)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(OBSIDILITH_WAVE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(7, 15), VanillaCopies::buildBillboardGeometry)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.scale(4f)
                particle.setColorVariation(0.25)
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.RED, ModColors.DARK_RED) }
                particle
            }
        }

        particleFactory.register(OBSIDILITH_WAVE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    WaveAction.waveDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.RED }
                particle.setColorVariation(0.3)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(DOWNSPARKLE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(OBSIDILITH_SPIKE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    WaveAction.waveDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.COMET_BLUE }
                particle.setColorVariation(0.3)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(OBSIDILITH_SPIKE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.WHITE, ModColors.COMET_BLUE) }
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setColorVariation(0.25)
                particle
            }
        }

        particleFactory.register(PILLAR_RUNE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(context, 10, VanillaCopies::buildBillboardGeometry, false)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (sin(it.toDouble() * Math.PI) + 1f).toFloat() * 0.1f }
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.WHITE, ModColors.ENDER_PURPLE) }
                particle.setColorVariation(0.2)
                particle
            }
        }

        particleFactory.register(PILLAR_SPAWN_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, PillarAction.pillarDelay, VanillaCopies::buildBillboardGeometry)
                particle.setColorOverride { ModColors.ENDER_PURPLE }
                particle.scale(2.0f)
                particle.setColorVariation(0.25)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle
            }
        }

        particleFactory.register(PILLAR_SPAWN_INDICATOR_2) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(context, PillarAction.pillarDelay, VanillaCopies::buildBillboardGeometry, false)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (sin(it.toDouble() * Math.PI) + 1f).toFloat() * 0.1f }
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.WHITE, ModColors.ENDER_PURPLE) }
                particle.setColorVariation(0.2)
                particle
            }
        }

        particleFactory.register(OBSIDILITH_ANVIL_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    RandomUtils.range(25, 27),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.ENDER_PURPLE }
                particle.setColorVariation(0.3)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(SPARKLES) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(GAUNTLET_REVIVE_SPARKLES) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
                particle.setColorOverride { ModColors.LASER_RED }
                particle.setColorVariation(0.25)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle
            }
        }

        particleFactory.register(EYE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(60, 70), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(LINE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(20, 30), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(ROD) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(8, 10), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(HORIZONTAL_ROD) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(8, 10), VanillaCopies::buildFlatGeometry)
            }
        }

        particleFactory.register(VOID_BLOSSOM_SPIKE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    indicatorDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.ENDER_PURPLE }
                particle.setColorVariation(0.2)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(VOID_BLOSSOM_SPIKE_WAVE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    SpikeWaveAction.indicatorDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.ENDER_PURPLE }
                particle.setColorVariation(0.2)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(PETAL) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry, false)
            }
        }

        particleFactory.register(POLLEN) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { c ->
                val particle = SimpleParticle(c, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry, false)
                particle.setColorOverride { Vec3d(1.0, 0.9, 0.4) }
                particle.setColorVariation(0.15)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { 0.05f * (1 - it * 0.25f) }
                val randomRot = RandomUtils.range(0, 360)
                val angularMomentum = RandomUtils.randSign() * 4f
                particle.setRotationOverride { randomRot + it.getAge() * angularMomentum }
                particle
            }
        }

        particleFactory.register(SPORE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it, RandomUtils.range(7, 15), VanillaCopies::buildBillboardGeometry)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.scale(4f)
                particle.setColorOverride { age -> MathUtils.lerpVec(age, ModColors.GREEN, ModColors.DARK_GREEN) }
                particle.setColorVariation(0.25)
                particle
            }
        }

        particleFactory.register(SPORE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(
                    context,
                    SporeBallProjectile.explosionDelay + RandomUtils.range(-1, 2),
                    VanillaCopies::buildFlatGeometry
                )
                particle.setColorOverride { ModColors.GREEN }
                particle.setColorVariation(0.35)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 + it) * 0.25f }
                particle
            }
        }

        particleFactory.register(FLUFF) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry)
            }
        }

        particleFactory.register(EARTHDIVE_INDICATOR) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) { context ->
                val particle = SimpleParticle(context, RandomUtils.range(15, 20), VanillaCopies::buildBillboardGeometry, doCollision = false)
                particle.setColorOverride { ModColors.RUNIC_BROWN }
                particle.setColorVariation(0.25)
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.setScaleOverride { (1 - (it * 0.25f)) * 0.35f }
                particle
            }
        }
    }
}