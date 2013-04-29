; I apologize in advance to any Clojure programmers reading this code ;)

(ns solution
  (:gen-class))

(def zombie-counts-delta-epsilon 0.10)

(defrecord World [number-nodes neighbors-by-node zombie-counts is-stabilised])

(defn run-simulation [world]
  (letfn [(compute-incoming-zombies [node zombie-counts]
            ; Please have mercy on me.
            (reduce + (map #(/ (zombie-counts %1) (double (count ((:neighbors-by-node world) %1)))) ((:neighbors-by-node world) node))))]
    (loop [zombie-counts (:zombie-counts world)]
      (let [new-zombie-counts (vec (map #(compute-incoming-zombies %1 zombie-counts) (range (:number-nodes world))))
            deltas (map #(reduce - %1) (map vector new-zombie-counts zombie-counts))
            is-stabilised (every? #(< (Math/abs %1) zombie-counts-delta-epsilon) deltas)
            ]
        (if is-stabilised
          (println (clojure.string/join " " (map #(Math/round %1) (take 5(reverse (sort zombie-counts))))))
          (recur new-zombie-counts)
          )))))

; http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
(defn map-vals [f m]
  (reduce (fn [altered-map [k v]] (assoc altered-map k (f v))) {} m))

(defn edge-end-node [edge] (second edge))
(defn edges-end-nodes [edges] (map edge-end-node edges))

(defn process-test-cases [reader]
  (let [lines (line-seq reader)
        [[num-test-cases-str], rest] (split-at 1 lines)
        num-test-cases (read-string num-test-cases-str)]
    (loop [num-test-cases-remaining num-test-cases rest rest]
      (let [[[world-specs], rest] (split-at 1 rest)
            [number-nodes, number-edges, time-steps] (map read-string (clojure.string/split world-specs #"\s"))
            [edge-specs, rest] (split-at number-edges rest)
            [zombie-count-specs, rest] (split-at number-nodes rest)
            ; Now build-up the initial state.
            unidirectional-edges (map #(map read-string (clojure.string/split %1 #"\s")) edge-specs)
            reversed-unidirectional-edges (map reverse unidirectional-edges)
            bidirectional-edges (concat unidirectional-edges reversed-unidirectional-edges)
            neighbors-by-node (map-vals edges-end-nodes (group-by #(first %1) bidirectional-edges))
            zombie-counts (vec (map read-string zombie-count-specs))
           ]
            (run-simulation (map->World{:number-nodes number-nodes,
                                        :neighbors-by-node neighbors-by-node,
                                        :zombie-counts zombie-counts
                                        :is-stabilised false}))
            (if (> num-test-cases-remaining 1) (recur (dec num-test-cases-remaining) rest))
    ))))

(defn -main []
  (let [is-local-run (= "THUNK" (System/getenv "COMPUTERNAME"))
        input-source (if is-local-run "../input02.txt" *in*)]
    (process-test-cases (clojure.java.io/reader input-source))))

