(ns tetris.shapes)

(def shape-o {:cords [[1 1]
                      [1 1]]
              :rotos {:0   {:x 0 :y 0}
                      :90  {:x 0 :y 0}
                      :180 {:x 0 :y 0}
                      :270 {:x 0 :y 0}}
              :color "#f00"})

(def shape-i {:cords [[1]
                      [1]
                      [1]
                      [1]]
              :rotos {:0   {:x +1 :y -2}
                      :90  {:x -1 :y +2}
                      :180 {:x +1 :y -2}
                      :270 {:x -1 :y +2}}
              :color "#ff0"})

(def shape-s {:cords [[0 1 1]
                      [1 1 0]]
              :rotos {:0   {:x -1 :y +1}
                      :90  {:x +1 :y -1}
                      :180 {:x -1 :y +1}
                      :270 {:x +1 :y -1}}
              :color "#0ff"})

(def shape-z {:cords [[1 1 0]
                      [0 1 1]]
              :rotos (:rotos shape-s)
              :color "#0f0"})

(def shape-l {:cords [[1 0]
                      [1 0]
                      [1 1]]
              :rotos {:0   {:x 0 :y -1}
                      :90  {:x 0 :y +1}
                      :180 {:x 0 :y -1}
                      :270 {:x 0 :y +1}}
              :color "#00f"})

(def shape-j {:cords [[0 1]
                      [0 1]
                      [1 1]]
              :rotos (:rotos shape-l)
              :color "#f0f"})

(def shape-t {:cords [[0 1 0]
                      [1 1 1]]
              :rotos {:0   {:x 0 :y +1}
                      :90  {:x 0 :y -1}
                      :180 {:x 0 :y +1}
                      :270 {:x 0 :y -1}}
              :color "#f77"})

(def rotation {:0 :90
               :90 :180
               :180 :270
               :270 :0})

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

(def left!  #(assoc-in % [:pos-x] (-> % :pos-x dec)))
(def right! #(assoc-in % [:pos-x] (-> % :pos-x inc)))
(def down!  #(assoc-in % [:pos-y] (-> % :pos-y inc)))

(defn rotate [shape]
  (let [ops (:opposite shape)
        rot ((:rot shape :0) rotation)
        {xos :x yos :y} (-> shape :rotos rot)
        npx (+ (:pos-x shape) xos)
        npy (+ (:pos-y shape) yos)
        cords (rotate-loop (:cords shape)
                           (-> shape :cords first count)
                           (-> shape :cords count)
                           ops)]
    (assoc shape
           :pos-x npx :pos-y npy
           :opposite (not ops)
           :rot rot
           :cords cords)))

(defn width [shape]
  (-> shape :cords first count))

(defn height [shape]
  (-> shape :cords count))
