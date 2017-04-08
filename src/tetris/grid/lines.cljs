(ns tetris.grid.lines
  (:require [tetris.layout :as lo]
            [tetris.shapes :as shape]
            [tetris.state :as state
             :refer [current-shape shapes]]))

(defn vec-remove
  [coll pos]
  (if (and (= (count coll) 1) (= pos 0))
    []
    (reduce-kv
     (fn [c k v] (if (= k pos) c (conj c v)))
     [] coll)))

(defn move-down-from-line [line]
  (let [{l :line} line]
    (doseq [[i s] (map-indexed vector @shapes)]
      (when (and (<= (:pos-y s) l)
                 (<= (+ (:pos-y s) (shape/height s)) lo/grid-height))
        (swap! shapes assoc-in [i :pos-y] (-> @shapes (get i) :pos-y inc))))))

(defn move-lines-down [lines]
  (doseq [l lines] (move-down-from-line l)))

(defn remove-shape [i] (swap! shapes vec-remove i))
(defn find-empty-shapes []
  (reduce-kv #(if (-> %3 :cords count zero?) (conj %1 %2) %1) [] @shapes))

(defn remove-empty-shapes [] (doseq [s (reverse (find-empty-shapes))] (remove-shape s)))

(defn remove-shape-row [l]
  (doseq [{s :index r :row} (:shapes l)] (remove-shape-row s)
         (let [shape (get @shapes s)]
           (swap! shapes #(assoc-in % [s :cords] (-> shape :cords (vec-remove r)))))))

(defn remove-empty-lines [lines]
  (doseq [l lines] (doseq [s (:shapes l)] (remove-shape-row s))))

(defn shape-in-line [l m i s]
  (let [sh (shape/height s)
        rowi (- l (:pos-y s))]
    (if (and (>= rowi 0 ) (<= rowi sh))
      (conj m {:index i :row rowi
               :cols (reduce + 0 (get (:cords s) rowi))})
      m)))

(defn full-line [l]
  (let [shps (reduce-kv #(shape-in-line l %1 %2 %3) [] @shapes)]
    (when (= lo/grid-width (reduce #(-> %2 :cols (+ %1)) 0 shps))
      {:line l :shapes shps})))

(defn find-full-lines [s e]
  (reduce #(if-let [fl (full-line %2)]
             (conj %1 fl) %1)
          [] (range s (inc e))))

(defn clear-full-lines [sy ey]
  (let [c (loop [l sy c 0]
            (if (<= l ey)
              (let [fl (full-line l)]
                (if (-> fl nil? not)
                  (do (remove-shape-row fl)
                      (move-down-from-line fl)
                      (recur (inc l) (inc c)))
                  (recur (inc l) c)))
              c))]
    (when (pos? c)
      (state/add-lines c)
      (remove-empty-shapes))))

(def clear! #(clear-full-lines (:pos-y @current-shape)
                               (+ (:pos-y @current-shape)
                                  (-> @current-shape shape/height))))
