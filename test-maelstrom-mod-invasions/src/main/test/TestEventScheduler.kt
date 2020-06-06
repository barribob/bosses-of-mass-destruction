package main.test

import net.barribob.maelstrom.general.EventScheduler
import org.junit.Test
import kotlin.test.assertEquals

class TestEventScheduler {

    @Test
    fun testEventAddedAndFired() {
        val eventManager = EventScheduler()
        var eventsFired = 0
        val incrementer = { eventsFired += 1 }

        eventManager.addEvent(eventManager, incrementer, 1)
        eventManager.updateEvents()
        assert(eventsFired == 0)
        eventManager.updateEvents()
        assert(eventsFired == 1)
    }

    @Test
    fun testMultipleEventsFired() {
        val eventManager = EventScheduler()
        var eventsFired = 0
        val incrementer = { eventsFired += 1 }

        eventManager.addEvent(eventManager, incrementer, 1)
        eventManager.updateEvents()
        eventManager.addEvent(eventManager, incrementer, 3)
        eventManager.updateEvents()
        assert(eventsFired == 1)

        eventManager.addEvent(eventManager, incrementer, 1)
        eventManager.updateEvents()
        assert(eventsFired == 1)
        eventManager.updateEvents()
        assert(eventsFired == 2)
        eventManager.updateEvents()
        assert(eventsFired == 3)
    }

    @Test
    fun testOrderOfEventsFired() {
        val eventManager = EventScheduler()
        var eventStr = ""
        var ticks = 0

        eventManager.addEvent(eventManager, { eventStr += "Fourth!" }, 3)
        eventManager.addEvent(eventManager, { eventStr += "Third!" }, 2)
        eventManager.addEvent(eventManager, { eventStr += "First!" }, 1)
        eventManager.addEvent(eventManager, { eventStr += "Second!" }, 1)

        eventManager.updateEvents()
        eventManager.updateEvents()
        assertEquals("First!Second!", eventStr)
        eventManager.updateEvents()
        assertEquals("First!Second!Third!", eventStr)
        eventManager.updateEvents()
        assertEquals("First!Second!Third!Fourth!", eventStr)
    }
}