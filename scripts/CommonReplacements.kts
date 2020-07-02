import java.io.File

// A small script that replaces 1.15.2 genereated blockbench java models
// line by line to make them (mostly) compatable with 1.16 snapshots

val filename = "C:\\Users\\micha\\Documents\\GitHub\\maelstrom-mod-invasions\\scripts\\ModelMaelstromScout.java"
val text = File(filename).bufferedReader().use { it.readText() }
        .replace("EntityLiving", "LivingEntity")
        .replace("EntityAIBase", "Goal")
        .replace("getAttackTarget()", "target")
        .replace("onGround", "isOnGround")
        .replace("getPosition()", "pos")
        .replace("getNavigator()", "navigation")
        .replace("getNodeProcessor()", "nodeMaker")
        .replace("noPath()", "isIdle")
        .replace("getPathNodeType", "getDefaultNodeType")
print(text)