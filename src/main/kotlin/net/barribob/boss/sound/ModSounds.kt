package net.barribob.boss.sound

import net.barribob.boss.Mod
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class ModSounds {
    private val soundIdMap = mutableMapOf<SoundEvent, Identifier>()

    val cometShoot = newSound("comet_shoot")
    val cometPrepare = newSound("comet_prepare")
    val missileShoot = newSound("missile_shoot")
    val missilePrepare = newSound("missile_prepare")
    val teleportPrepare = newSound("teleport_prepare")
    val minionRune = newSound("minion_rune")
    val minionSummon = newSound("minion_summon")
    val ragePrepare = newSound("rage_prepare")
    val obsidilithBurst = newSound("obsidilith_burst")
    val waveIndicator = newSound("wave_indicator")
    val spikeIndicator = newSound("spike_indicator")
    val spike = newSound("spike")
    val obsidilithPrepareAttack = newSound("obsidilith_prepare_attack")
    val energyShield = newSound("energy_shield")
    val gauntletIdle = newSound("gauntlet_idle")
    val gauntletHurt = newSound("gauntlet_hurt")
    val gauntletCast = newSound("gauntlet_cast")

    fun init() {
        registerSound(cometShoot)
        registerSound(cometPrepare)
        registerSound(missileShoot)
        registerSound(missilePrepare)
        registerSound(teleportPrepare)
        registerSound(minionRune)
        registerSound(minionSummon)
        registerSound(ragePrepare)
        registerSound(obsidilithBurst)
        registerSound(waveIndicator)
        registerSound(spikeIndicator)
        registerSound(spike)
        registerSound(obsidilithPrepareAttack)
        registerSound(energyShield)
        registerSound(gauntletIdle)
        registerSound(gauntletHurt)
        registerSound(gauntletCast)
    }

    private fun registerSound(event: SoundEvent) {
        Registry.register(Registry.SOUND_EVENT, soundIdMap[event], event)
    }

    private fun newSound(id: String): SoundEvent {
        val identifier = Mod.identifier(id)
        val soundEvent = SoundEvent(identifier)
        soundIdMap[soundEvent] = identifier
        return soundEvent
    }
}