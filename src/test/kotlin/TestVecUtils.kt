import net.barribob.maelstrom.general.newVec3d
import net.barribob.maelstrom.general.planeProject
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestVecUtils {

    @Test
    fun testPlaneProject() {
        val planeVector = Vec3d(0.0, 1.0, 0.0)
        val projection = newVec3d(1.0, 1.0, 1.0).planeProject(planeVector)
        assertEquals(newVec3d(1.0, 0.0, 1.0), projection)
    }
}