(ns tetris.grid
  (:require [tetris.canvas :as can]
            [tetris.layout :as lo]
            [tetris.time :as time]
            [tetris.state
             :refer [current-shape shapes square-size]
             :as state]
            [tetris.grid.countdown :as countdown]
            [tetris.grid.control :as control]
            [tetris.grid.collision :as collis]
            [tetris.grid.lines :as lines]
            [tetris.grid.gameover :as go]))

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

(defn render-current-shape [] (render-shape @current-shape))

(defn standard []
  (move-current-shape)
  (render-current-shape)
  (control/control-shape))

(defn render []
  (can/draw-rectangle (lo/cgrid-x) (lo/cgrid-y) (lo/cgrid-w) (lo/cgrid-h)
                      (if @state/countdown "#444" "#999"))
  (if @state/countdown
    (reset! state/countdown (countdown/render))
    (do
      (when (and (not @state/paused) @current-shape) (standard))
      (when (and (not @state/paused)
                 (not (lines/to-clear?))
                 (not @current-shape)
                 (not (go/gameover?)))
        (state/load-shape lo/grid-width))
      (when (and @state/paused @current-shape) (render-current-shape))
      (render-all-shapes)
      (when (and (not @state/paused) (lines/to-clear?))
        (lines/render))
      (when (and (not @state/paused) (go/gameover?)) (go/render)))))
