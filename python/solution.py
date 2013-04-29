import sys
import os

is_local_run = os.environ.get('COMPUTERNAME') == 'THUNK'

def run_simulation(neighbors_by_node, zombie_count_by_node):
    def compute_incoming_zombies(node):
        return sum([zombie_count_by_node[neighbor] / len(neighbors_by_node[neighbor]) for neighbor in neighbors_by_node[node]])

    new_zombie_counts_by_node = [compute_incoming_zombies(node) for node in range(len(zombie_count_by_node))]
    deltas = [counts[0] - counts[1] for counts in zip(new_zombie_counts_by_node, zombie_count_by_node)]
    is_stabilised = all(abs(delta) < 0.10 for delta in deltas)

    if is_stabilised:
        print(" ".join([str(round(count)) for count in sorted(new_zombie_counts_by_node, reverse=True)[0:5]]))
    else:
        run_simulation(neighbors_by_node, new_zombie_counts_by_node)

def process_test_cases(reader):
    num_test_cases = int(reader.readline())

    for test_case in range(num_test_cases):
        [num_nodes, num_edges, time_steps] = [int(str_val) for str_val in reader.readline().split(" ")]

        neighbors_by_node = [[] for _ in range(num_nodes)]

        for _ in range(num_edges):
            [node1, node2] = [int(str_val) for str_val in reader.readline().split(" ")]
            neighbors_by_node[node1].append(node2)
            neighbors_by_node[node2].append(node1)

        zombie_count_by_node = [int(reader.readline()) for _ in range(num_nodes)]

        run_simulation(neighbors_by_node, zombie_count_by_node)

if is_local_run:
    reader = open("input02.txt")
else:
    reader = sys.stdin

process_test_cases(reader)
