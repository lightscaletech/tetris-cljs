(ns tetris.background
  (:require [tetris.canvas :as can]))

(defn render []
  (can/draw-rectangle 0 0 (can/gsize :w) (can/gsize :h) "#444"))
