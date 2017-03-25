(ns tetris.grid
  (:require [tetris.canvas :as can]
            [tetris.shapes :as shapes]
            [tetris.time :as time]
            [tetris.control :as control]))

(def width  10)
(def height 20)
(def padding 30)

(def pos (atom {:x 0 :y 0 :w 0 :h 0}))
(def square-size (atom 0))

(def current-shape (atom nil))
(def shapes (atom []))

(defn check-bottom []
  (>= (+ (shapes/height @current-shape) (:pos-y @current-shape))
     height))

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

(defn place []
  (if (or (check-bottom)
          (check-collision 0 1))
    (do (swap! shapes conj @current-shape)
        (reset! current-shape nil)
        true)
    false))

(defn move-shape-left []
  (when (and (> (:pos-x @current-shape) 0)
             (not (check-collision -1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x dec)))))

(defn move-shape-right []
  (when (and (< (:pos-x @current-shape) (- width (shapes/width @current-shape)))
             (not (check-collision 1 0)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x inc)))))

(defn move-shape-down []
  (when-not (place) (swap! current-shape #(assoc-in % [:pos-y] (-> % :pos-y inc)))))

(defn rotate-shape []
  (shapes/rotate current-shape)
  (when (> (+ (:pos-x @current-shape) (shapes/width @current-shape)) width)
    (swap! current-shape
           #(assoc-in % [:pos-x] (- width (shapes/width @current-shape))))))

(defn move-current-shape []
  (when (time/check-move)
    (move-shape-down)
    (time/set-move)))

(defn make-shape [shape]
  (assoc shape
         :opposite false
         :flip-x false
         :flip-y false
         :pos-x (- (/ width 2)
                   (.round js/Math (-> shape :cords first count (/ 2))))
         :pos-y (-> shape :cords count - (+ 1))))

(defn create-shape []
  (reset! current-shape (make-shape (shapes/pick-shape)))
  (time/set-move))

(def control-left   (atom false))
(def control-right  (atom false))
(def control-down   (atom false))
(def control-rotate (atom false))

(defn control-toggling [con done fun]
  (when (and @con (not @done)) (fun) (reset! done true))
  (when (and (not @con) @done) (reset! done false)))

(defn control-shape []
  (control-toggling control/left-active  control-left   move-shape-left)
  (control-toggling control/right-active control-right  move-shape-right)
  (control-toggling control/down-active  control-down   move-shape-down)
  (control-toggling control/up-active    control-rotate rotate-shape))

(defn render-shape [shape]
  (let [cords (:cords shape)
        gx (:x @pos) gy (:y @pos)
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
  (let [{:keys [x y w h]} @pos]
    (can/draw-rectangle x y w h "#999"))
  (if @current-shape
    (do (move-current-shape)
        (render-shape @current-shape)
        (control-shape))
    (create-shape))
  (render-all-shapes))

(defn resize []
  (let [{cw :w ch :h} (can/gsize)
        hp (- ch (* padding 2))
        wp (- cw (* padding 2))
        sq (/ hp height)
        cw (* sq width)]
    (if (> wp cw)
      (do (reset! pos {:x padding :y padding :w cw :h hp})
          (reset! square-size sq))
      (do (reset! pos {:x padding :y padding :w wp :h (* (/ wp width) height)})
          (reset! square-size (/ wp width))))))

(defn init [] (resize))
