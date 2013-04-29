package main

import (
	"fmt"
	"io"
	"os"
	"strconv"
	"sort"
	"math"
	"text/scanner"
)

var IsLocalRun = os.Getenv("COMPUTERNAME") == "THUNK"

var NumberZombiesDeltaEpsilon = 0.10

type Node struct {
	neighbors []int
	numberZombies float64
	numberIncomingZombies float64
}

func runSimulation(world []Node, timeSteps int) {
	isStabilised := false
	timeStepsRemaining := timeSteps

	for !isStabilised && timeStepsRemaining > 0 {
		for node := 0; node < len(world); node++ {
			outgoingZombiesPerDirection := world[node].numberZombies / float64(len(world[node].neighbors))
			for neighborNodeIdx := 0; neighborNodeIdx < len(world[node].neighbors); neighborNodeIdx++ {
				world[world[node].neighbors[neighborNodeIdx]].numberIncomingZombies += outgoingZombiesPerDirection
			}
		}

		isStabilised = true
		for node := 0; node < len(world); node++ {
			if math.Abs(world[node].numberZombies - world[node].numberIncomingZombies) > NumberZombiesDeltaEpsilon {
				isStabilised = false
			}
		}

		for node := 0; node < len(world); node++ {
			world[node].numberZombies = world[node].numberIncomingZombies
			world[node].numberIncomingZombies = 0
		}

		timeStepsRemaining--
	}

	zombieCounts := make([]float64, len(world))
	for node := 0; node < len(world); node++ {
		zombieCounts[node] = world[node].numberZombies
	}

	sort.Float64s(zombieCounts)

	for n := 0; n < 5; n++ {
		// No round function?
		fmt.Printf("%d ", int(math.Floor(zombieCounts[len(zombieCounts) - n - 1] + 0.5)))
	}
	fmt.Println()
}

func main() {
	var err error
	var reader io.Reader

	if IsLocalRun {
		file, err := os.Open("go/input01.txt")
		if (err != nil) { panic(err) }

		reader = file
	} else {
		reader = os.Stdin
	}

	var s scanner.Scanner
	s.Init(reader)

	var numberTestCases int
	if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read number of test cases") }
	numberTestCases, err = strconv.Atoi(s.TokenText())
	if (err != nil) { panic(err) }

	for testCase := 0; testCase < numberTestCases; testCase++ {
		var numberNodes, numberEdges, timeSteps int

		if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read number of nodes") }
		numberNodes, err = strconv.Atoi(s.TokenText())
		if (err != nil) { panic(err) }

		if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read number of edges") }
		numberEdges, err = strconv.Atoi(s.TokenText())
		if (err != nil) { panic(err) }

		if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read number of time steps") }
		timeSteps, err = strconv.Atoi(s.TokenText())
		if (err != nil) { panic(err) }

		world := make([]Node, numberNodes)

		for edge := 0; edge < numberEdges; edge++ {
			var node1, node2 int

			if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read node number 1") }
			node1, err = strconv.Atoi(s.TokenText())
			if (err != nil) { panic(err) }

			if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read node number 2") }
			node2, err = strconv.Atoi(s.TokenText())
			if (err != nil) { panic(err) }

			world[node1].neighbors = append(world[node1].neighbors, node2)
			world[node2].neighbors = append(world[node2].neighbors, node1)
		}

		for node := 0; node < numberNodes; node++ {
			var numberZombies int
			if tokenType := s.Scan(); tokenType != scanner.Int { panic ("Couldn't read number of zombies") }
			numberZombies, err = strconv.Atoi(s.TokenText())
			if err != nil { panic(err) }
			world[node].numberZombies = float64(numberZombies)
		}

		runSimulation(world, timeSteps)
	}

}
