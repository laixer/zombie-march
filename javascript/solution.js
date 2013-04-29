function runSimulation(neighborsByNode, zombieCountsByNode) {
    var newZombieCountsByNode = []

    for(var nodeIdx = 0; nodeIdx < zombieCountsByNode.length; nodeIdx++) {
        var incomingZombies = neighborsByNode[nodeIdx].map(function(neighbor) { return zombieCountsByNode[neighbor] / neighborsByNode[neighbor].length })
        newZombieCountsByNode[nodeIdx] = incomingZombies.reduce(function(a,b) {return a + b})
    }

    var isStabilised = true
    for(var nodeIdx = 0; nodeIdx < zombieCountsByNode.length; nodeIdx++) {
        if (Math.abs(zombieCountsByNode[nodeIdx] - newZombieCountsByNode[nodeIdx]) > 0.10) {
            isStabilised = false
            break;
        }
    }

    if (isStabilised) {
        newZombieCountsByNode.sort(function(a,b) { return b - a; })
        console.log(newZombieCountsByNode.slice(0, 5).map(function(x) { return Math.round(x) }).join(" "))
    } else {
        runSimulation(neighborsByNode, newZombieCountsByNode)
    }
}

function processTestCases(input) {
    var currentLine = 0

    var numberTestCases = input[currentLine++]
    for(var testCase = 0; testCase < numberTestCases; testCase++) {
        var worldSpecs = input[currentLine++].split(" ")
        var numberNodes = worldSpecs[0]
        var numberEdges = worldSpecs[1]
        var timeSteps = worldSpecs[2]

        var unidirectionalEdges = []
        for(var edgeIdx = 0; edgeIdx < numberEdges; edgeIdx++) {
            unidirectionalEdges.push(input[currentLine++].split(" ").map(function(x) { return parseInt(x) } ))
        }
        var reversedUnidirectionalEdges = unidirectionalEdges.map(function(x) { var copy = x.slice(); copy.reverse(); return copy; })
        var bidirectionalEdges = unidirectionalEdges.concat(reversedUnidirectionalEdges)

        var neighborsByNode = {}
        for(var edgeIdx = 0; edgeIdx < bidirectionalEdges.length; edgeIdx++) {
            var edge = bidirectionalEdges[edgeIdx]
            if (!(edge[0] in neighborsByNode)) {
                neighborsByNode[edge[0]] = []
            }
            neighborsByNode[edge[0]].push(edge[1])
        }

        var zombieCountsByNode = []
        for(var nodeIdx = 0; nodeIdx < numberNodes; nodeIdx++) {
            zombieCountsByNode[nodeIdx] = parseInt(input[currentLine++])
        }

        runSimulation(neighborsByNode, zombieCountsByNode)
    }
}

if (process.env["COMPUTERNAME"] == "THUNK") {
    fs = require('fs')
    fs.readFile('javascript/input02.txt', 'utf8', function(err, data) {
        if (err) {
            throw new Error("Unable to open input file", err)
        }
        processTestCases(data.split("\n"))
    })
} else {
    process.stdin.resume()
    process.stdin.setEncoding("utf8")
    process.stdin.on("data", function(data) {
        processTestCases(data.split("\n"))
        process.stdin.pause()
    })
}
