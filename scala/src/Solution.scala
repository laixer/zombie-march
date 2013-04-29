import annotation.tailrec
import io.Source

object Solution {
  val isLocalRun = sys.env.get("COMPUTERNAME") match {
    case Some("THUNK") => true
    case _ => false
  }

  val NumberZombiesDeltaEpsilon = 0.10

  class World(val numberNodes: Int,
              val neighborsByNode: Map[Int, Array[Int]],
              val numberZombiesByNode: Array[Double],
              val isStabilised: Boolean) {

    def this(numberNodes: Int, neighborsByNode: Map[Int, Array[Int]], numberZombiesByNode: Array[Double]) =
      this(numberNodes, neighborsByNode, numberZombiesByNode, false)

    def step: World = {
      def computeIncomingZombiesFromNeighbor(neighbor: Int) =
        numberZombiesByNode(neighbor) / neighborsByNode(neighbor).length.toDouble

      val newNumberZombiesByNode =
        0.until(numberNodes).map(node => neighborsByNode(node).map(computeIncomingZombiesFromNeighbor).sum)

      val isStabilised =
        !newNumberZombiesByNode.zip(numberZombiesByNode)
                               .exists{case (zombies, newZombies) => (zombies - newZombies).abs > NumberZombiesDeltaEpsilon}

      new World(numberNodes, neighborsByNode, newNumberZombiesByNode.toArray, isStabilised)
    }

    def topFiveMostCrowdedNodes: Array[Int] =
      numberZombiesByNode.sorted.reverse.take(5).map(_.round.toInt)
  }

  @tailrec
  def simulate(world: World, timeStepsRemaining: Int): World = {
    if (world.isStabilised || timeStepsRemaining == 0) world else simulate(world.step, timeStepsRemaining - 1)
  }

  def main(args: Array[String]) = {
    val source = if(isLocalRun) Source.fromFile("scala/input01.txt") else Source.fromInputStream(System.in)
    val lineIterator = source.getLines()

    val numberTestCases = lineIterator.next().toInt
    for (testCase <- 1.to(numberTestCases)) {
      val Array(numberNodes, numberEdges, timeSteps) = lineIterator.next().split(" ").map(_.toInt)

      def convertEdgeSpecToTuple(edgeSpecLine: String) = {
        val parts = edgeSpecLine.split(" ").map(_.toInt)
        (parts(0), parts(1))
      }

      val unidirectionalEdges = lineIterator.take(numberEdges).map(convertEdgeSpecToTuple).toArray
      val bidirectionalEdges = unidirectionalEdges ++ unidirectionalEdges.map(_.swap)
      // Maybe a bit too cryptic...
      val neighborsByNode = bidirectionalEdges.groupBy{case (node1, node2) => node1}.mapValues(_.map(_._2))

      val numberZombiesByNode = lineIterator.take(numberNodes).map(_.toDouble).toArray

      val world = new World(numberNodes, neighborsByNode, numberZombiesByNode)
      val endWorld = simulate(world, timeSteps)

      println(endWorld.topFiveMostCrowdedNodes.mkString(" "))
    }
  }
}
