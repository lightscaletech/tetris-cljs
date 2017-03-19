(ns tetris.core
  (:require [tetris.canvas :as canvas]
            [tetris.grid :as grid]
            [tetris.game :as game]
            [tetris.control :as control]))

(defn onload
  ([] (onload nil))
  ([ev]
   (canvas/init)
   (grid/init)
   (control/init)
   (game/start)))

(defn resize []
  (canvas/resize)
  (grid/resize))

(defn main []
  (.addEventListener js/window "DOMContentLoaded" onload)
  (.addEventListener js/window "resize" resize true))
