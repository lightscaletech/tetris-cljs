(ns tetris.grid
  (:require [tetris.canvas :as can]
            [tetris.shapes :as tshape]
            [tetris.time :as time]
            [tetris.control :as control]
            [tetris.layout :as lo]
            [tetris.state
             :refer [current-shape shapes square-size]
             :as state]))

(defn check-bottom []
  (>= (+ (tshape/height @current-shape) (:pos-y @current-shape))
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

(defn check-collision [cshp ox oy]
  (let [cheight (tshape/height cshp)
        cwidth (tshape/width cshp)
        cpos-xl (:pos-x cshp)
        cpos-xr (+ cpos-xl (dec cwidth))
        cpos-yt (:pos-y cshp)
        cpos-yb (+ cpos-yt (dec cheight))]
    (some
     #(let [swidth (tshape/width %)
            sheight (tshape/height %)
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
    (reduce-kv
     (fn [c k v] (if (= k pos) c (conj c v)))
     [] coll)))

(defn move-down-from-line [line]
  (let [{l :line} line]
    (doseq [[i s] (map-indexed vector @shapes)]
      (when (and (<= (:pos-y s) l)
                 (<= (+ (:pos-y s) (tshape/height s)) lo/grid-height))
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
  (let [sh (tshape/height s)
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

(defn check-for-lines [sy ey]
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

(defn place []
  (if (or (check-bottom)
          (check-collision @current-shape 0 1))
    (do (swap! shapes conj @current-shape)
        (check-for-lines (:pos-y @current-shape)
                         (+ (:pos-y @current-shape)
                            (-> @current-shape tshape/height)))
        (when (-> @current-shape :pos-y (<= 0)) (state/gameover))
        (reset! current-shape nil)
        (reset! state/quick-down-active false)
        (state/add-shape)
        true)
    false))

(defn move-shape-left []
  (when (and (> (:pos-x @current-shape) 0)
             (not (check-collision @current-shape -1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x dec)))))

(defn move-shape-right []
  (when (and (< (:pos-x @current-shape)
                (- lo/grid-width (tshape/width @current-shape)))
             (not (check-collision @current-shape 1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x inc)))))

(defn move-shape-down []
  (when-not (place)
    (swap! current-shape #(assoc-in % [:pos-y] (-> % :pos-y inc)))
    (time/set-move)))

(defn rotate-shape []
  (let [ns (tshape/rotate @current-shape)
        nsw (tshape/width ns)]
    (if (check-collision ns 0 0)
      (if (check-collision ns -1 0)
        (reset! current-shape (assoc ns :pos-x (+ (:pos-x ns) -1)))
        (when-not (check-collision ns 1 0)
          (reset! current-shape (assoc ns :pos-x (+ (:pos-x ns) 1)))))
      (if (< (:pos-x ns) 0)
        (reset! current-shape (assoc ns :pos-x 0))
        (if (> (+ nsw (:pos-x ns)) lo/grid-width)
          (reset! current-shape (assoc ns :pos-x (- lo/grid-width nsw)))
          (reset! current-shape ns))))))

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
  (when (and (not @con) @done) (reset! done false))
  (when (and @con (not @done)) (fun) (reset! done true)))

(def touch-move (atom {:x 0 :y 0}))
(def touch-down-point (atom nil))
(defn assoc+ [m k a] (assoc m k (+ a (k m))))
(defn move-amount [k a mfn]
  (repeat a (mfn))
  (swap! touch-move assoc+ k a))

(defn touch-shape-move []
  (when (and (not @touch-down-point) @control/touch-down)
    (reset! touch-down-point @control/touch-pos))

  (when (and @touch-down-point (not @control/touch-down))
    (let [{xtd :x ytd :y} @touch-down-point
          {xtl :x ytl :y} @control/touch-pos
          ss2 (/ @square-size 2)]
      (when (and (>= xtl (- xtd ss2)) (<= xtl (+ xtd ss2))
                 (>= ytl (- ytd ss2)) (<= ytl (+ ytd ss2)))
        (rotate-shape)))
    (reset! touch-down-point nil))

  (if-let [{dx :x dy :y} (control/touch-diff)]
    (let [xs (.round js/Math (/ dx @square-size))
          ys (.round js/Math (/ dy @square-size))
          xtm (:x @touch-move) ytm (:y @touch-move)
          xtmd (- xs xtm) ytmd (- ys ytm)]
      (when (zero? ytm)
        (and (pos? xtmd) (move-amount :x xtmd move-shape-right))
        (and (neg? xtmd) (move-amount :x xtmd move-shape-left)))
      (when (and (zero? xtm) (not @state/ns-control-block))
        (and (pos? ytmd) (move-amount :y ytmd move-shape-down))))
    (do (if @state/ns-control-block (reset! state/ns-control-block false))
        (reset! touch-move {:x 0 :y 0}))))

(defn control-shape []
  (control-toggling control/left-active  control-left   move-shape-left)
  (control-toggling control/right-active control-right  move-shape-right)
  (control-toggling control/down-active  control-down   move-shape-down)
  (control-toggling control/up-active    control-rotate rotate-shape)
  (control-toggling control/space-active control-quick-down quick-down)
  (touch-shape-move))

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
    (do (when (not @state/paused)
          (move-current-shape)
          (control-shape))
        (render-shape @current-shape))
    (create-shape))
  (render-all-shapes))
