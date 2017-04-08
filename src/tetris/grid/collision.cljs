(ns tetris.grid.collision
  (:require [tetris.shapes :as shape]
            [tetris.layout :as lo]
            [tetris.state :refer [shapes current-shape]]))

(defn any-kv [pred col]
  (let [len (count col)]
    (loop [i 0]
      (if (pred i (get col i))
        true
        (if (< i (dec len))
          (recur (inc i))
          false)))))

(defn check-rows [ox cxl crow sxl srow]
  (any-kv
   (fn [ci cc]
      (any-kv
       (fn [si sc]
         (and (pos? cc) (pos? sc) (= (+ cxl ci ox) (+ sxl si))))
       srow))
   crow))

(defn check-shape-in-box [ox oy cshp sshp]
  (let [cpos-xl (:pos-x cshp)
        cpos-yt (:pos-y cshp)
        spos-xl (:pos-x sshp)
        spos-yt (:pos-y sshp)]
    (any-kv
     (fn [cr-y crow]
       (let [crpos-y (+ cpos-yt cr-y)]
         (any-kv
          (fn [sr-y srow]
            (and (= (+ crpos-y oy) (+ sr-y spos-yt))
                 (check-rows ox cpos-xl crow spos-xl srow)))
          (:cords sshp))))
     (:cords cshp))))

(defn check-collision [cshp ox oy]
  (let [cheight (shape/height cshp)
        cwidth (shape/width cshp)
        cpos-xl (:pos-x cshp)
        cpos-xr (+ cpos-xl (dec cwidth))
        cpos-yt (:pos-y cshp)
        cpos-yb (+ cpos-yt (dec cheight))]
    (some
     #(let [swidth (shape/width %)
            sheight (shape/height %)
            spos-xl (:pos-x %)
            spos-xr (+ spos-xl (dec swidth))
            spos-yt (:pos-y %)
            spos-yb (+ spos-yt (dec sheight))
            top-r (-> % :cords first)]
        (if (and (>= (+ cpos-yb oy) spos-yt)
                 (<= (+ cpos-yt oy) spos-yb)
                 (>= (+ cpos-xr ox) spos-xl)
                 (<= (+ cpos-xl ox) spos-xr))
          (check-shape-in-box ox oy cshp %)
          false))
     @shapes)))

(defn check-bottom []
  (>= (+ (shape/height @current-shape) (:pos-y @current-shape))
      lo/grid-height))

(defn place? [] (or (check-bottom) (check-collision @current-shape 0 1)))
