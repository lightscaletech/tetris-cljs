(ns tetris.grid.utils
  (:require [tetris.shapes :as shape]
            [tetris.state :refer [shapes]]))

(defn shape-in-line [l m i s]
  (let [sh (shape/height s)
        rowi (- l (:pos-y s))]
    (if (and (>= rowi 0 ) (<= rowi sh))
      (conj m {:index i :row rowi
               :pos-x (:pos-x s) :cords (get (:cords s) rowi)
               :cols (reduce + 0 (get (:cords s) rowi))})
      m)))

(defn shapes-in-line [l] (reduce-kv #(shape-in-line l %1 %2 %3) [] @shapes))
