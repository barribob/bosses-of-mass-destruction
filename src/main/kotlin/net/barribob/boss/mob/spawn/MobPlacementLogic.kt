package net.barribob.boss.mob.spawn

class MobPlacementLogic(
    private val locationFinder: ISpawnPosition,
    private val entityProvider: IEntityProvider,
    private val spawnPredicate: ISpawnPredicate,
    private val spawner: IMobSpawner,
) {
    fun tryPlacement(tries: Int): Boolean {
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