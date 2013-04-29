class solution {
    def static isLocalRun = System.getenv("COMPUTERNAME") == "THUNK"

    def static NUMBER_ZOMBIES_DELTA_EPSILON = 0.10

    private static class World {
        int numberNodes
        Map<Integer, List<Integer>> neighborsByNode
        List<Double> numberZombiesByNode
        boolean isStabilised

        private double computeIncomingZombiesFromNeighbor(int neighbor) {
            numberZombiesByNode[neighbor] / neighborsByNode[neighbor].size().toDouble()
        }

        World step() {
            def newNumberZombiesByNode =
                (0 ..< numberNodes).collect { node ->
                    neighborsByNode[node].collect { computeIncomingZombiesFromNeighbor(it) }.sum()
                }

            // A little awkward...
            def deltas = [newNumberZombiesByNode, numberZombiesByNode].transpose().collect { newZombies, zombies -> newZombies - zombies }
            def isStabilised = !deltas.any { delta -> delta.abs() > NUMBER_ZOMBIES_DELTA_EPSILON }

            new World(numberNodes: numberNodes,
                      neighborsByNode: neighborsByNode,
                      numberZombiesByNode: newNumberZombiesByNode,
                      isStabilised: isStabilised
                     )
        }

        List<Integer> topFiveMostCrowdedNodes() {
            numberZombiesByNode.sort().reverse().take(5).collect { it.round().toInteger() }
        }
    }

    public static void main(String[] args) {
        def inputStream
        if (isLocalRun) {
            inputStream = new FileInputStream("input01.txt")
        } else {
            inputStream = System.in
        }

        def reader = new BufferedReader(new InputStreamReader(inputStream))
        def lineIterator = reader.iterator()

        def numberTestCases = lineIterator.next().toInteger()
        numberTestCases.times {
            def (numberNodes, numberEdges, timeSteps) = lineIterator.next().split(" ").collect { it.toInteger() }
            def unidirectionalEdges = lineIterator.take(numberEdges).collect { line -> line.split(" ").collect { it.toInteger() } }
            def bidirectionalEdges = []
            bidirectionalEdges.addAll(unidirectionalEdges)
            bidirectionalEdges.addAll(unidirectionalEdges.collect { it.reverse() })

            // A little too much...
            def neighborsByNode = bidirectionalEdges.groupBy { edge -> edge[0] }.collectEntries { node, neighbors -> [ node, neighbors.collect { it[1] }]}
            def numberZombiesByNode = lineIterator.take(numberNodes).collect { it.toDouble() }

            def world = new World(numberNodes: numberNodes,
                                  neighborsByNode: neighborsByNode,
                                  numberZombiesByNode: numberZombiesByNode)
            def remainingTimeSteps = timeSteps

            while (remainingTimeSteps > 0 && !world.isStabilised) {
                world = world.step()
                remainingTimeSteps--
            }

            println(world.topFiveMostCrowdedNodes().join(" "))
        }
    }
}
