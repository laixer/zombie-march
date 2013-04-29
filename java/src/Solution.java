import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Solution {

    private static final boolean isLocalRun = "THUNK".equals(System.getenv("COMPUTERNAME"));

    private static final double NUMBER_ZOMBIES_DELTA_EPSILON = 0.10;

    private static class NeighborList implements Iterable<Integer> {
        private List<Integer> neighbors = new ArrayList<>();

        public void addNeighbor(int index) {
            neighbors.add(index);
        }

        @Override
        public Iterator<Integer> iterator() {
            return neighbors.iterator();
        }

        public int size() {
            return neighbors.size();
        }
    }

    private static class World {
        private int numberNodes;
        private NeighborList[] neighborsByNode;
        private double[] numberZombiesByNode;
        private double[] numberIncomingZombiesAtNode;
        private boolean stabilised = false;

        public World(int numberNodes) {
            this.numberNodes = numberNodes;
            this.neighborsByNode = new NeighborList[numberNodes];
            for(int node = 0; node < this.numberNodes; node++) {
                this.neighborsByNode[node] = new NeighborList();
            }
            this.numberZombiesByNode = new double[numberNodes];
            this.numberIncomingZombiesAtNode = new double[numberNodes];
        }

        public void linkNodes(int node1, int node2) {
            neighborsByNode[node1].addNeighbor(node2);
            neighborsByNode[node2].addNeighbor(node1);
        }

        public void setInitialZombies(int node, int numberZombies) {
            numberZombiesByNode[node] = numberZombies;
        }

        public void step() {
            for(int nodeIndex = 0; nodeIndex < numberNodes; nodeIndex++) {
                double outgoingZombiesPerDirection = numberZombiesByNode[nodeIndex] / (double) neighborsByNode[nodeIndex].size();
                for(int neighborNode : neighborsByNode[nodeIndex]) {
                    numberIncomingZombiesAtNode[neighborNode] += outgoingZombiesPerDirection;
                }
            }

            stabilised = true;
            for(int nodeIndex = 0; nodeIndex < numberNodes; nodeIndex++) {
                if (Math.abs(numberZombiesByNode[nodeIndex] - numberIncomingZombiesAtNode[nodeIndex]) > NUMBER_ZOMBIES_DELTA_EPSILON) {
                    stabilised = false;
                }
            }

            for(int nodeIndex = 0; nodeIndex < numberNodes; nodeIndex++) {
                numberZombiesByNode[nodeIndex] = numberIncomingZombiesAtNode[nodeIndex];
                numberIncomingZombiesAtNode[nodeIndex] = 0;
            }
        }

        public List<Integer> getTopFiveMostCrowdedNodes() {
            double[] sortedZombieCounts = new double[numberNodes];
            System.arraycopy(numberZombiesByNode, 0, sortedZombieCounts, 0, numberNodes);

            List<Integer> topFiveZombieCounts = new ArrayList<>();
            Arrays.sort(sortedZombieCounts);
            for(int i = 0; i < 5; i++) {
                topFiveZombieCounts.add((int) Math.round(sortedZombieCounts[numberNodes - i - 1]));
            }

            return topFiveZombieCounts;
        }

        public boolean isStabilised() {
            return stabilised;
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream inputStream;
        if (isLocalRun) {
            inputStream = new FileInputStream("java/input01.txt");
        } else {
            inputStream = System.in;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int numberTestCases = Integer.parseInt(reader.readLine());

        for(int testCase = 0; testCase < numberTestCases; testCase++) {
            String[] worldSpecs = reader.readLine().split(" ");
            int numberNodes = Integer.parseInt(worldSpecs[0]);
            int numberEdges = Integer.parseInt(worldSpecs[1]);
            int timeSteps = Integer.parseInt(worldSpecs[2]);

            World world = new World(numberNodes);

            for(int edge = 0; edge < numberEdges; edge++) {
                String[] edgeSpecs = reader.readLine().split(" ");
                int node1Index = Integer.parseInt(edgeSpecs[0]);
                int node2Index = Integer.parseInt(edgeSpecs[1]);
                world.linkNodes(node1Index, node2Index);
            }

            for(int nodeIndex = 0; nodeIndex < numberNodes; nodeIndex++) {
                world.setInitialZombies(nodeIndex, Integer.parseInt(reader.readLine()));
            }

            int remainingTimeSteps = timeSteps;

            while (remainingTimeSteps > 0 && !world.isStabilised()) {
                world.step();
                remainingTimeSteps--;
            }

            for(int zombieCount : world.getTopFiveMostCrowdedNodes()) {
                System.out.print(zombieCount + " ");
            }
            System.out.println();
        }
    }
}
