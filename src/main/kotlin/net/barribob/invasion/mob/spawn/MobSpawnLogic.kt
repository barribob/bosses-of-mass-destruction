package net.barribob.invasion.mob.spawn

class MobSpawnLogic(
    private val locationFinder: ISpawnPosition,
    private val entityProvider: IEntityProvider,
    private val spawnPredicate: ISpawnPredicate,
    private val spawner: IMobSpawner,
) {
    fun trySpawnMob(tries: Int): Boolean {
        val entity = entityProvider.getEntity() ?: return false
        for (i in 0 until tries) {
            val location = locationFinder.getPos()
            if (spawnPredicate.canSpawn(location, entity)) {
                spawner.spawn(location, entity)
                return true
            }
        }

        return false
    }
}