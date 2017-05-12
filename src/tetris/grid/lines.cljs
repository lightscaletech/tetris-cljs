(ns tetris.grid.lines
  (:require [tetris.layout :as lo]
            [tetris.shapes :as shape]
            [tetris.time :as time]
            [tetris.canvas :as can]
            [tetris.grid.utils :as u]
            [tetris.state :as state
             :refer [current-shape shapes square-size]]))

(def ani-int 33)
(def remove-delay 200)

(def to-clear  (atom []))
(def animating (atom {}))
(def cleared   (atom 0))
(def last-move (atom 0))

(defn vec-remove
  [coll pos]
  (if (and (= (count coll) 1) (= pos 0))
    []
    (reduce-kv
     (fn [c k v] (if (= k pos) c (conj c v)))
     [] coll)))

(defn move-down-from-line [l]
  (doseq [[i s] (map-indexed vector @shapes)]
    (when (and (<= (:pos-y s) l)
               (<= (+ (:pos-y s) (shape/height s)) lo/grid-height))
      (swap! shapes assoc-in [i :pos-y] (-> @shapes (get i) :pos-y inc)))))

(defn move-lines-down [lines]
  (doseq [l lines] (move-down-from-line l)))

(defn remove-shape [i] (swap! shapes vec-remove i))
(defn find-empty-shapes []
  (reduce-kv #(if (-> %3 :cords count zero?) (conj %1 %2) %1) [] @shapes))

(defn remove-empty-shapes [] (doseq [s (reverse (find-empty-shapes))] (remove-shape s)))

(defn remove-shape-row [shps]
  (doseq [{s :index r :row} shps]
    (let [shape (get @shapes s)]
      (swap! shapes #(assoc-in % [s :cords] (-> shape :cords (vec-remove r)))))))

(defn remove-empty-lines [lines]
  (doseq [l lines] (doseq [s (:shapes l)] (remove-shape-row s))))

(defn full-line [l]
  (when (= lo/grid-width
           (reduce #(-> %2 :cols (+ %1)) 0
                   (u/shapes-in-line l)))
    l))

(defn find-full-lines [s e]
  (reduce #(if-let [fl (full-line %2)]
             (conj %1 fl) %1)
          [] (range s (inc e))))

(defn clear-full-lines [sy ey]
  (reset! to-clear (set (find-full-lines sy ey)))
  (reset! last-move (time/now))
  (reset! animating {})
  (reset! cleared 0))

(def clear! #(clear-full-lines (:pos-y @current-shape)
                               (+ (:pos-y @current-shape)
                                  (shape/height @current-shape))))

(defn to-clear? [] (-> @to-clear count pos?))

(defn render-line [l p f]
  (let [op (if f dec inc)
        s (if f lo/grid-width 0)
        check (if f
                #(>= % (- lo/grid-width p))
                #(<= % p))
        gx (lo/cgrid-x) gy (lo/cgrid-y)]
    (loop [c s]
      (when (check c)
        (can/draw-rectangle
         (+ gx (* @square-size c)) (+ gy (* @square-size l))
         @square-size @square-size "#222")
        (recur (op c))))))

(defn render-animating []
  (doseq [l (keys @animating)]
    (let [{:keys [pos flip]} (get @animating l)] (render-line l pos flip))))

(defn map-nth [m i] (get m (nth (keys m) i)))
(defn map-first [m] (map-nth m 0))
(defn map-last [m] (map-nth m (-> m keys count dec)))

(defn find-next-line
  ([] (first @to-clear))
  ([l]
   (let [tc (vec @to-clear)
         len (count tc)]
     (loop [i 0]
       (let [cl (nth tc i)]
         (if (not (= l cl))
           (when (< i len) (recur (inc i)))
           (if (= len (inc i)) nil (nth tc (inc i)))))))))

(defn load-animating-line
  ([l] (load-animating-line l true))
  ([l f]
   (swap! animating assoc l {:pos 0 :flip (not f)})))

(defn check-animating []
  (let [ll (-> @animating keys last)]
    (if-let [lla (get @animating ll)]
      (when (= (:pos lla) (/ lo/grid-width 2))
        (if-let [nl (find-next-line ll)] (load-animating-line nl (:flip lla))))
      (load-animating-line (find-next-line)))))

(defn ani-line-com [l]
  (swap! animating dissoc l)
  (swap! to-clear disj l)
  (swap! cleared inc)
  (remove-shape-row (u/shapes-in-line l))
  (move-down-from-line l)
  (when (-> @to-clear count zero?)
    (state/add-lines @cleared)
    (remove-empty-shapes)))

(defn tick []
  (doseq [al (keys @animating)]
    (let [ald (get @animating al)
          aldp (:pos ald)]
      (when (and (-> ald :ftime not) (< aldp lo/grid-width))
        (swap! animating assoc al (assoc ald :pos (inc aldp))))
      (when (and (-> ald :ftime not) (>= aldp lo/grid-width))
        (swap! animating assoc al (assoc ald :ftime (time/now))))
      (when (and (:ftime ald) (> (time/now) (+ remove-delay (:ftime ald))))
        (ani-line-com al)))))

(defn render []
  (render-animating)
  (when (> (time/now) (+ @last-move ani-int))
    (tick)
    (check-animating)
    (reset! last-move (time/now))))
