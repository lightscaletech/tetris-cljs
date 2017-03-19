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

(defn place []
  (if (or (check-bottom))
    (do (swap! shapes conj @current-shape)
        (reset! current-shape nil)
        true)
    false))

(defn move-shape-left []
  (when (> (:pos-x @current-shape) 0)
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x dec)))))

(defn move-shape-right []
  (when (< (:pos-x @current-shape) (- width (shapes/width @current-shape)))
    (swap! current-shape #(assoc-in % [:pos-x] (-> % :pos-x inc)))))

(defn move-shape-down []
  (swap! current-shape #(assoc-in % [:pos-y] (-> % :pos-y inc))))

(defn move-current-shape []
  (when (and (time/check-move)
             (not (place)))
    (move-shape-down)
    (time/set-move)))

(defn make-shape [shape]
  (assoc shape
         :pos-x (- (/ width 2)
                   (.round js/Math (-> shape :cords first count (/ 2))))
         :pos-y (-> shape :cords count - (+ 1))))

(defn create-shape []
  (reset! current-shape (make-shape (shapes/pick-shape)))
  (time/set-move))

(def control-left  (atom false))
(def control-right (atom false))

(defn control-toggling [con done fun]
  (when (and @con (not @done)) (fun) (reset! done true))
  (when (and (not @con) @done) (reset! done false)))

(defn control-shape []
  (control-toggling control/left-active control-left move-shape-left)
  (control-toggling control/right-active control-right move-shape-right))

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
