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

        val myScanCount = input.nextInt()
        val myScannedCreatureIds = mutableListOf<Int>()
        for (i in 0 until myScanCount) {
            myScannedCreatureIds.add(input.nextInt())
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
        for (i in 0 until droneScanCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
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

        //----------
        val radarBlipCount = input.nextInt()
        for (i in 0 until radarBlipCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            val radar = input.next()
        }
        //----------

        for (i in 0 until myDroneCount) {
            val drone = myDrones[i]
            val unscannedVisibleCreaturesByDistance: List<VisibleCreature> = visibleCreatures.getUnscannedVisibleCreaturesWithDistanceSortedFromMe(me = drone, myScannedCreatureIds = myScannedCreatureIds)
            System.err.println(unscannedVisibleCreaturesByDistance)
            val closestUnscannedVisibleCreature = unscannedVisibleCreaturesByDistance.firstOrNull()
            System.err.println(closestUnscannedVisibleCreature)

            closestUnscannedVisibleCreature?.let { creature ->
                println("MOVE ${creature.x + creature.vx} ${creature.y + creature.vy} ${drone.getLightPower(creature = creature)}")
            } ?: run {
                println("WAIT $STANDARD_POWER_LIGHT")
            }
        }
    }
}

private fun List<VisibleCreature>.getUnscannedVisibleCreaturesWithDistanceSortedFromMe(me: Drone, myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    getUnscannedVisibleCreaturesWithDistanceFromMe(me = me, myScannedCreatureIds = myScannedCreatureIds)
        .sortedByDescending { it.distance }.reversed()

private fun List<VisibleCreature>.getUnscannedVisibleCreaturesWithDistanceFromMe(me: Drone, myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    getUnscannedVisibleCreatures(myScannedCreatureIds = myScannedCreatureIds)
        .getVisibleCreaturesWithDistanceFromMe(me = me).sortedByDescending { it.distance }

private fun List<VisibleCreature>.getUnscannedVisibleCreatures(myScannedCreatureIds: List<Int>): List<VisibleCreature> =
    filter { visibleCreature -> !myScannedCreatureIds.contains(visibleCreature.creature.id) }

private fun List<VisibleCreature>.getVisibleCreaturesWithDistanceFromMe(me: Drone): List<VisibleCreature> =
    map { vc -> vc.copy(distance = me.getDistanceByCreature(creature = vc)) }

private fun Drone.getDistanceByCreature(creature: VisibleCreature): Double =
    Math.pow((x - creature.x).toDouble(), 2.0) + Math.pow((y - creature.y).toDouble(), 2.0)

private fun Drone.getLightPower(creature: VisibleCreature): Int =
    if (hasEnoughBattery() && creature.isCloseEnough()) HIGH_POWER_LIGHT else STANDARD_POWER_LIGHT


private fun Drone.hasEnoughBattery(): Boolean = battery >= 5

private fun VisibleCreature.isCloseEnough(): Boolean {
    val distance = Math.sqrt(distance ?: 10_000.0)
    return 800 < distance && distance <= 2_000
}

data class Creature(val id: Int, val color: Int, val type: Int)

data class Drone(val id: Int, val x: Int, val y: Int, val emergency: Int, val battery: Int)

data class VisibleCreature(val creature: Creature, val x: Int, val y: Int, val vx: Int, val vy: Int, var distance: Double? = null)
