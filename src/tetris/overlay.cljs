(ns tetris.overlay
  (:require [tetris.canvas :as can]))

(defn render []
  (let [{:keys [w h]} (can/gsize)]
    (can/draw-rectangle 0 0 w h "rgba(0, 0, 0, 0.65)")))
