package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentV3

interface ILichSummonCounterComponent: ComponentV3 {
    fun getValue(): Int
    fun increment()
}