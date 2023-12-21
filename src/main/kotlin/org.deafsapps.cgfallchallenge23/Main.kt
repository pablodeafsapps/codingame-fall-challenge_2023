import java.util.*
import java.io.*
import java.math.*

private const val HIGH_POWER_LIGHT = 1
private const val STANDARD_POWER_LIGHT = 0

/**
 * Score points by scanning valuable fish faster than your opponent.
 **/
fun main() {
    val input = Scanner(System.`in`)

    val creatures = mutableListOf<Creature>()
    val creatureCount = input.nextInt()
    for (i in 0 until creatureCount) {
        creatures.add(
            Creature(id = input.nextInt(), color = input.nextInt(), type = input.nextInt())
        )
    }

    // game loop
    while (true) {
        val myScore = input.nextInt()
        val foeScore = input.nextInt()

        val myScansAwardedCount = input.nextInt()
        val myScannedCreatureIdsAwarded = mutableListOf<Int>()
        for (i in 0 until myScansAwardedCount) {
            myScannedCreatureIdsAwarded.add(input.nextInt())
        }

        val foeScanCount = input.nextInt()
        val foeScannedCreatureIds = mutableListOf<Int>()
        for (i in 0 until foeScanCount) {
            foeScannedCreatureIds.add(input.nextInt())
        }

        val myDroneCount = input.nextInt()
        val myDrones = mutableListOf<Drone>()
        for (i in 0 until myDroneCount) {
            myDrones.add(
                Drone(id = input.nextInt(), x = input.nextInt(), y = input.nextInt(), emergency = input.nextInt(), battery = input.nextInt())
            )
        }

        val foeDroneCount = input.nextInt()
        val foeDrones = mutableListOf<Drone>()
        for (i in 0 until foeDroneCount) {
            foeDrones.add(
                Drone(id = input.nextInt(), x = input.nextInt(), y = input.nextInt(), emergency = input.nextInt(), battery = input.nextInt())
            )
        }

        val droneScanCount = input.nextInt()
        System.err.println("Drones scans: $droneScanCount")
        val myScannedCreatureIds = mutableListOf<Int>()
        for (i in 0 until droneScanCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            if (myDrones.map { d -> d.id }.contains(droneId)) myScannedCreatureIds.add(creatureId)
        }

        val visibleCreatureCount = input.nextInt()
        val visibleCreatures = mutableListOf<VisibleCreature>()
        for (i in 0 until visibleCreatureCount) {
            val id = input.nextInt()
            visibleCreatures.add(
                VisibleCreature(
                    creature = creatures.first { it.id == id },
                    x = input.nextInt(),
                    y = input.nextInt(),
                    vx = input.nextInt(),
                    vy = input.nextInt()
                )
            )
        }

        val radarBlipCount = input.nextInt()
        for (i in 0 until radarBlipCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            val radar = input.next()
            if (myDrones.contains(id = droneId)) {
                creatures.find { it.id == creatureId }?.radarPosition = RadarPosition.valueOf(radar)
            }
        }
        System.err.println("Creatures: $creatures")

        for (i in 0 until myDroneCount) {
            val drone = myDrones[i]
            if (drone.needsSurface) {
                drone.moveToSurface()
            } else {
                System.err.println("My Scanned Creature IDs: $myScannedCreatureIds")
                val unscannedVisibleCreaturesByDistance: List<VisibleCreature> =
                    visibleCreatures.getUnscannedVisibleCreaturesWithDistanceSortedFromMe(
                        me = drone,
                        myScannedCreatureIds = myScannedCreatureIds
                    )
                System.err.println("Unscanned Visible Creatures By Distance: $unscannedVisibleCreaturesByDistance")
                val closestUnscannedVisibleCreature: VisibleCreature? =
                    unscannedVisibleCreaturesByDistance.firstOrNull()
                System.err.println("Closest Unscanned Visible Creature: $closestUnscannedVisibleCreature")

                closestUnscannedVisibleCreature?.let { visibleCreature ->
                    drone.moveToVisibleCreature(visibleCreature = visibleCreature)
                } ?: run {
                    creatures.getUnscannedCreaturesWithTypeSorted(myScannedCreatureIds = myScannedCreatureIds)
                        .firstOrNull()?.let { rarestUnscannedCreature ->
                            System.err.println("Rarest Unscanned Creature: $rarestUnscannedCreature")
                            drone.moveToCreatureArea(creature = rarestUnscannedCreature)
                        } ?: run {
                        drone.moveToSurface()
                    }
                }
            }
        }
    }
}

private fun List<Creature>.getUnscannedCreaturesWithTypeSorted(myScannedCreatureIds: List<Int>): List<Creature> =
    getUnscannedCreatures(myScannedCreatureIds = myScannedCreatureIds)
        .sortedByDescending { it.type }

private fun List<Creature>.getUnscannedCreatures(myScannedCreatureIds: List<Int>): List<Creature> =
    filter { creature -> !myScannedCreatureIds.contains(creature.id) }

private fun List<VisibleCreature>.getUnscannedVisibleCreaturesWithDistanceSortedFromMe(me: Drone, myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    getUnscannedVisibleCreaturesWithDistanceFromMe(me = me, myScannedCreatureIds = myScannedCreatureIds)
        .sortedBy { it.distance }

private fun List<VisibleCreature>.getUnscannedVisibleCreaturesWithDistanceFromMe(me: Drone, myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    getUnscannedVisibleCreatures(myScannedCreatureIds = myScannedCreatureIds)
        .getVisibleCreaturesWithDistanceFromMe(me = me).sortedByDescending { it.distance }

private fun List<VisibleCreature>.getUnscannedVisibleCreatures(myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    filter { visibleCreature -> !myScannedCreatureIds.contains(visibleCreature.creature.id) }

private fun List<VisibleCreature>.getVisibleCreaturesWithDistanceFromMe(me: Drone): List<VisibleCreature> =
    map { vc -> vc.copy(distance = me.getDistanceByCreature(creature = vc)) }

private fun List<Drone>.contains(id: Int): Boolean = map { d -> d.id }.contains(id)

private fun Drone.moveToVisibleCreature(visibleCreature: VisibleCreature) {
    System.err.println("ACTION: Move to Visible Creature -> id = ${visibleCreature.creature.id}, distance = ${Math.sqrt(visibleCreature.distance ?: 100_000.0)}")
    println("MOVE ${visibleCreature.x + visibleCreature.vx} ${visibleCreature.y + visibleCreature.vy} ${getLightPower(creature = visibleCreature)}")
}

private fun Drone.moveToCreatureArea(creature: Creature) {
    System.err.println("ACTION: Move to Creature Area -> ${creature.radarPosition}")
    println("MOVE ${getDirectionByRadarPosition(radarPosition = creature.radarPosition)} ${Random().nextInt(2)}")
}

private fun Drone.moveToSurface() {
    System.err.println("ACTION: Move to Surface")
    println("MOVE $x 500 0")
}

private fun Drone.getDirectionByRadarPosition(radarPosition: RadarPosition): String =
    when (radarPosition) {
        RadarPosition.TL -> "0 0"
        RadarPosition.TR -> "10000 0"
        RadarPosition.BL -> "0 10000"
        RadarPosition.BR -> "10000 10000"
        RadarPosition.UNKNOWN -> throw IllegalArgumentException("creature::radarPosition should be initialised")
    }

private fun Drone.getDistanceByCreature(creature: VisibleCreature): Double =
    Math.pow((x - creature.x).toDouble(), 2.0) + Math.pow((y - creature.y).toDouble(), 2.0)

private fun Drone.getLightPower(creature: VisibleCreature): Int =
    if (hasEnoughBattery() && creature.isCloseEnough()) HIGH_POWER_LIGHT else STANDARD_POWER_LIGHT

private fun Drone.hasEnoughBattery(): Boolean = battery >= 5

private fun VisibleCreature.isCloseEnough(): Boolean {
    val distance = Math.sqrt(distance ?: 100_000.0)
    return 800 < distance && distance <= 2_000
}

data class Creature(val id: Int, val color: Int, val type: Int, var radarPosition: RadarPosition = RadarPosition.UNKNOWN)

data class Drone(val id: Int, val x: Int, val y: Int, val emergency: Int, val battery: Int, var needsSurface: Boolean = false)

data class VisibleCreature(val creature: Creature, val x: Int, val y: Int, val vx: Int, val vy: Int, var distance: Double? = null)

enum class RadarPosition {
    UNKNOWN, TL, TR, BL, BR
}
