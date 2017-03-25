(ns tetris.shapes)

(def shape-o {:cords [[1 1]
                      [1 1]]
              :color "#f00"})

(def shape-i {:cords [[1]
                      [1]
                      [1]
                      [1]]
              :color "#ff0"})

(def shape-s {:cords [[0 1 1]
                      [1 1 0]]
              :color "#0ff"})

(def shape-z {:cords [[1 1 0]
                      [0 1 1]]
              :color "#0f0"})

(def shape-l {:cords [[1 0]
                      [1 0]
                      [1 1]]
              :color "#00f"})

(def shape-j {:cords [[0 1]
                      [0 1]
                      [1 1]]
              :color "#f0f"})

(def shape-t {:cords [[0 1 0]
                      [1 1 1]]
              :color "#f77"})

(defn pick-shape []
  (let [mapping [shape-o shape-i shape-t
                 shape-j shape-l
                 shape-z shape-s]
        num (rand-int (count mapping))]
    (get mapping num)))

(defn- rotate-row-loop [cords col rows ops]
  (loop [r (dec rows) res []]
    (if (<= 0 r)
      (recur (dec r)
             (conj res (get (get cords r) col)))
      res)))

(defn- rotate-loop [cords cols rows ops]
  (loop [c 0 res []]
    (if (< c cols)
      (recur (inc c)
             (conj res (rotate-row-loop cords c rows ops)))
      res)))

(defn rotate [shape]
  (let [ops (:opposite @shape)]
    (swap! shape assoc-in [:cords]
           (rotate-loop (:cords @shape)
                        (-> @shape :cords first count)
                        (-> @shape :cords count)
                        ops))
    (swap! shape #(assoc % :opposite (not ops)))))

(defn width [shape]
  (-> shape :cords first count))

(defn height [shape]
  (-> shape :cords count))
