(ns tetris.background
  (:require [tetris.canvas :as can]
            [tetris.layout :as lo]))

(defn render []
  (can/draw-rectangle 0 0 (can/gsize :w) (can/gsize :h) "#444")
  (can/draw-rectangle @lo/start-x @lo/start-y (lo/cwidth) (lo/cheight) "#222"))
