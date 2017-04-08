(ns tetris.grid.control
  (:require [tetris.input :as input]
            [tetris.state :as state :refer [current-shape square-size shapes]]
            [tetris.shapes :as shape]
            [tetris.layout :as lo]
            [tetris.time :as time]
            [tetris.grid.collision :as collis]
            [tetris.grid.lines :as lines]))

(def control-left   (atom false))
(def control-right  (atom false))
(def control-down   (atom false))
(def control-rotate (atom false))
(def control-quick-down (atom false))

(def touch-move (atom {:x 0 :y 0}))
(def touch-down-point (atom nil))

(defn move-shape-left []
  (when (and (> (:pos-x @current-shape) 0)
             (not (collis/check-collision @current-shape -1 0)))
    (swap! current-shape shape/left!)))

(defn move-shape-right []
  (when (and (< (:pos-x @current-shape)
                (- lo/grid-width (shape/width @current-shape)))
             (not (collis/check-collision @current-shape 1 0)))
    (swap! current-shape shape/right!)))

(defn place-shape! []
  (swap! shapes conj @current-shape)
  (lines/clear!)
  (when (-> @current-shape :pos-y (<= 0)) (state/gameover))
  (reset! current-shape nil)
  (reset! state/quick-down-active false)
  (state/add-shape))

(defn move-shape-down []
  (if (collis/place?)
    (place-shape!)
    (do (swap! current-shape shape/down!)
        (time/set-move))))

(defn rotate-shape []
  (let [ns (shape/rotate @current-shape)
        nsw (shape/width ns)]
    (if (collis/check-collision ns 0 0)
      (if (collis/check-collision ns -1 0)
        (reset! current-shape (assoc ns :pos-x (+ (:pos-x ns) -1)))
        (when-not (collis/check-collision ns 1 0)
          (reset! current-shape (assoc ns :pos-x (+ (:pos-x ns) 1)))))
      (if (< (:pos-x ns) 0)
        (reset! current-shape (assoc ns :pos-x 0))
        (if (> (+ nsw (:pos-x ns)) lo/grid-width)
          (reset! current-shape (assoc ns :pos-x (- lo/grid-width nsw)))
          (reset! current-shape ns))))))

(defn quick-down [] (reset! state/quick-down-active true))

(defn control-toggling [con done fun]
  (when (and (not @con) @done) (reset! done false))
  (when (and @con (not @done)) (fun) (reset! done true)))

(defn assoc+ [m k a] (assoc m k (+ a (k m))))
(defn move-amount [k a mfn]
  (repeat a (mfn))
  (swap! touch-move assoc+ k a))

(defn touch-shape-move []
  (when (and (not @touch-down-point) @input/touch-down)
    (reset! touch-down-point @input/touch-pos))

  (when (and @touch-down-point (not @input/touch-down))
    (let [{xtd :x ytd :y} @touch-down-point
          {xtl :x ytl :y} @input/touch-pos
          ss2 (/ @square-size 2)]
      (when (and (>= xtl (- xtd ss2)) (<= xtl (+ xtd ss2))
                 (>= ytl (- ytd ss2)) (<= ytl (+ ytd ss2)))
        (rotate-shape)))
    (reset! touch-down-point nil))

  (if-let [{dx :x dy :y} (input/touch-diff)]
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
  (control-toggling input/left-active  control-left   move-shape-left)
  (control-toggling input/right-active control-right  move-shape-right)
  (control-toggling input/down-active  control-down   move-shape-down)
  (control-toggling input/up-active    control-rotate rotate-shape)
  (control-toggling input/space-active control-quick-down quick-down)
  (touch-shape-move))
