(ns tetris.grid
  (:require [tetris.canvas :as can]
            [tetris.shapes :as shapes]
            [tetris.time :as time]
            [tetris.control :as control]
            [tetris.layout :as lo]
            [tetris.state
             :refer [current-shape shapes square-size]
             :as state]))

(defn check-bottom []
  (>= (+ (shapes/height @current-shape) (:pos-y @current-shape))
     lo/grid-height))

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

(defn check-collision [ox oy]
  (let [cshp @current-shape
        cheight (shapes/height cshp)
        cwidth (shapes/width cshp)
        cpos-xl (:pos-x cshp)
        cpos-xr (+ cpos-xl (dec cwidth))
        cpos-yt (:pos-y cshp)
        cpos-yb (+ cpos-yt (dec cheight))]
    (some
     #(let [swidth (shapes/width %)
            sheight (shapes/height %)
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

(defn vec-remove
  [coll pos]
  (if (and (= (count coll) 1) (= pos 0))
    []
    (vec (concat (subvec coll 0 pos)
                 (when (< pos (-> coll count dec)) (subvec coll (inc pos)))))))

(defn move-down-from-line [l lines]
  (let [scount (count @shapes)]
    (loop [i 0]
      (let [s (get @shapes i)]
        (when (and (<= (:pos-y s) l)
                   (<= (+ (:pos-y s) (shapes/height s)) lo/grid-height))
          (swap! shapes assoc-in [i :pos-y]
                 (-> @shapes (get i) :pos-y (+ lines)))))
      (when (< i scount)
        (recur (inc i))))))

(defn remove-shape-rows [shp]
  (let [s (:shape shp)
        r (:row shp)
        ts (get @shapes s)]
    (swap! shapes #(assoc-in % [s :cords] (-> ts :cords (vec-remove r))))))

(defn find-empty-shapes []
  (reduce-kv #(if (-> %3 :cords count zero?)
                (conj %1 %2))
             '() @shapes))

(defn remove-empty-shapes []
  (doseq [s (find-empty-shapes)] (swap! shapes vec-remove s)))

(defn find-line-rows [between]
  (reduce
   (fn [c l]
     (let [shps (reduce-kv
                 #(let [py (:pos-y %3) rowi (- l py)]
                    (if (and (<= py l)
                             (>= (+ py (shapes/height %3)) l))
                      (conj %1 {:shape %2 :row rowi
                                :squares (->> rowi (get (:cords %3))
                                              (remove zero?) count)})
                      %1))
                 [] @shapes)]
       (if (= (reduce #(-> %2 :squares (+ %1)) 0 shps) lo/grid-width)
         (assoc c
                :shapes (into (:shapes c) shps)
                :line l
                :lines (inc (:lines c)))
         c)))
   {:shapes [] :line 0 :lines 0} between))

(defn check-for-lines [sy ey]
  (let [{:keys [shapes line lines]} (find-line-rows (range sy (inc ey)))]
    (when (-> shapes count pos?)
      (doseq [s (reverse shapes)] (remove-shape-rows s))
      (move-down-from-line line lines)
      (remove-empty-shapes)
      (state/add-lines lines))))

(defn place []
  (if (or (check-bottom)
          (check-collision 0 1))
    (do (swap! shapes conj @current-shape)
        (check-for-lines (:pos-y @current-shape)
                         (+ (:pos-y @current-shape)
                            (-> @current-shape shapes/height dec)))
        (when (-> @current-shape :pos-y (<= 0)) (state/gameover))
        (reset! current-shape nil)
        (reset! state/quick-down-active false)
        true)
    false))

(defn move-shape-left []
  (when (and (> (:pos-x @current-shape) 0)
             (not (check-collision -1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x dec)))))

(defn move-shape-right []
  (when (and (< (:pos-x @current-shape)
                (- lo/grid-width (shapes/width @current-shape)))
             (not (check-collision 1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x inc)))))

(defn move-shape-down []
  (when-not (place)
    (swap! current-shape #(assoc-in % [:pos-y] (-> % :pos-y inc)))
    (time/set-move)))

(defn rotate-shape []
  (shapes/rotate current-shape)
  (when (> (+ (:pos-x @current-shape) (shapes/width @current-shape))
           lo/grid-width)
    (swap! current-shape
           #(assoc-in % [:pos-x] (- lo/grid-width (shapes/width @current-shape))))))

(defn move-current-shape [] (when (time/check-move) (move-shape-down)))

(defn quick-down [] (reset! state/quick-down-active true))

(defn create-shape []
  (state/load-shape lo/grid-width)
  (time/set-move))

(def control-left   (atom false))
(def control-right  (atom false))
(def control-down   (atom false))
(def control-rotate (atom false))
(def control-quick-down (atom false))

(defn control-toggling [con done fun]
  (when (and @con (not @done)) (fun) (reset! done true))
  (when (and (not @con) @done) (reset! done false)))

(defn control-shape []
  (control-toggling control/left-active  control-left   move-shape-left)
  (control-toggling control/right-active control-right  move-shape-right)
  (control-toggling control/down-active  control-down   move-shape-down)
  (control-toggling control/up-active    control-rotate rotate-shape)
  (control-toggling control/space-active control-quick-down quick-down))

(defn render-shape [shape]
  (let [cords (:cords shape)
        gx (lo/cgrid-x) gy (lo/cgrid-y)
        sx (* (:pos-x shape) @square-size)
        sy (* (:pos-y shape) @square-size)]
    (doseq [[r cols] (map-indexed vector cords)]
      (when (-> shape :pos-y (+ r) neg? not)
        (doseq [[c v] (map-indexed vector cols)]
          (when (pos? v)
            (can/draw-rectangle
             (+ gx sx (* c @square-size)) (+ gy sy (* r @square-size))
             @square-size @square-size
             (:color shape))))))))

(defn render-all-shapes []
  (doseq [s @shapes] (render-shape s)))

(defn render []
  (can/draw-rectangle (lo/cgrid-x) (lo/cgrid-y) (lo/cgrid-w) (lo/cgrid-h) "#999")
  (if @current-shape
    (do (move-current-shape)
        (render-shape @current-shape)
        (control-shape))
    (create-shape))
  (render-all-shapes))
