(ns tetris.grid
  (:require [tetris.canvas :as can]
            [tetris.layout :as lo]
            [tetris.time :as time]
            [tetris.state
             :refer [current-shape shapes square-size]
             :as state]
            [tetris.countdown :as countdown]
            [tetris.grid.control :as control]
            [tetris.grid.collision :as collis]))

(defn create-shape [] (state/load-shape lo/grid-width))

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

(defn move-current-shape [] (when (time/check-move) (control/move-shape-down)))

(defn render-all-shapes []
  (doseq [s @shapes] (render-shape s)))

(defn render []
  (can/draw-rectangle (lo/cgrid-x) (lo/cgrid-y) (lo/cgrid-w) (lo/cgrid-h) "#999")
  (if @state/countdown
    (reset! state/countdown (countdown/render))
    (if @current-shape
      (do (when (not @state/paused)
            (move-current-shape)
            (control/control-shape))
          (render-shape @current-shape))
      (create-shape)))
  (render-all-shapes))
