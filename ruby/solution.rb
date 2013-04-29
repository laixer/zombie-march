def run_simulation(neighbors_by_node, zombie_counts_by_node)
  new_zombie_counts_by_node =
    (0 ... zombie_counts_by_node.length).map do |node|
      neighbors_by_node[node].map { |neighbor| zombie_counts_by_node[neighbor].to_f / neighbors_by_node[neighbor].length }.inject(:+)
    end

  deltas = zombie_counts_by_node.zip(new_zombie_counts_by_node).map { |count1, count2| count1 - count2 }
  is_stabilised = deltas.all? { |delta| delta.abs < 0.10 }

  if is_stabilised
    puts new_zombie_counts_by_node.sort.reverse.take(5).map {|count| count.round }.join(" ")
  else
    run_simulation(neighbors_by_node, new_zombie_counts_by_node)
  end
end

def process_test_cases(input)
  num_test_cases = input.readline.to_i
  num_test_cases.times do
    (num_nodes, num_edges, time_steps) = input.readline.split.map { |x| x.to_i }

    unidirectional_edges = (0 ... num_edges).map { input.readline.split.map { |x| x.to_i } }
    reversed_unidirectional_edges = unidirectional_edges.map { |edge| edge.reverse }
    bidirectional_edges = unidirectional_edges + reversed_unidirectional_edges

    neighbors_by_node = bidirectional_edges.group_by { |edge| edge[0] }
    neighbors_by_node = Hash[neighbors_by_node.map {|node, neighbors| [node, neighbors.map { |edge| edge[1] }]}]

    zombie_counts_by_node = (0 ... num_nodes).map { input.readline.to_i }

    run_simulation(neighbors_by_node, zombie_counts_by_node)
  end
end

if ENV["COMPUTERNAME"] == "THUNK"
  input = open('input02.txt')
else
  input = STDIN
end

process_test_cases(input)
