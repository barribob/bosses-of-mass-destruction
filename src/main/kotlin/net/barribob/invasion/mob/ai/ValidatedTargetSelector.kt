package net.barribob.invasion.mob.ai

import net.barribob.invasion.mob.ai.valid_direction.IValidDirection
import net.barribob.invasion.utils.IVelPos
import net.barribob.maelstrom.general.random.IRandom
import net.barribob.maelstrom.static_utilities.negateServer
import net.barribob.maelstrom.static_utilities.rotateVector
import net.minecraft.util.math.Vec3d

class ValidatedTargetSelector(private val velPos: IVelPos, private val validator: IValidDirection, private val random: IRandom) : ITargetSelector {
    private var previousDirection: Vec3d = random.getVector()

    override fun getTarget(): Vec3d {
        val pos = velPos.getPos()
        for (i in 5 until 200 step 20) {
            val newDirection = previousDirection.rotateVector(random.getVector(), random.getDouble() * i)

            if(validator.isValidDirection(newDirection.normalize())) {
                previousDirection = newDirection
                return pos.add(newDirection)
            }
        }

        previousDirection = previousDirection.negateServer()
        return pos.add(previousDirection)
    }
}