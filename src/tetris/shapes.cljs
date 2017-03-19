(ns tetris.shapes)

(def shape-o {:cords [[1 1]
                      [1 1]]
              :color "#f00"
              :offset [0 0]})

(def shape-i {:cords [[1]
                      [1]
                      [1]
                      [1]]
              :color "#ff0"
              :offset [1 1]})

(def shape-s {:cords [[0 1 1]
                      [1 1 0]]
              :color "#0ff"
              :offset [1 1]})

(def shape-z {:cords [[1 1 0]
                      [0 1 1]]
              :color "#0f0"
              :offset [1 1]})

(def shape-l {:cords [[1 0]
                      [1 0]
                      [1 1]]
              :color "#00f"
              :offset [1 1]})

(def shape-j {:cords [[0 1]
                      [0 1]
                      [1 1]]
              :color "#f0f"
              :offset [1 1]})

(def shape-t {:cords [[0 1 0]
                      [1 1 1]]
              :color "#f77"
              :offset [1 1]})

(defn pick-shape []
  (let [mapping [shape-o shape-i shape-t
                 shape-j shape-l
                 shape-z shape-s]
        num (rand-int (count mapping))]
    (get mapping num)))

(defn width [shape]
  (-> shape :cords first count))

(defn height [shape]
  (-> shape :cords count))
