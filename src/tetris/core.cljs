(ns tetris.core
  (:require [tetris.canvas :as canvas]
            [tetris.sidebar :as sidebar]
            [tetris.layout :as lo]
            [tetris.game :as game]
            [tetris.control :as control]))

(defn onload
  ([] (onload nil))
  ([ev]
   (canvas/init)
   (lo/resize)
   (control/init)
   (game/start)))

(defn resize []
  (canvas/resize)
  (lo/resize)
  (sidebar/resize))

(defn main []
  (.addEventListener js/window "DOMContentLoaded" onload)
  (.addEventListener js/window "resize" resize true))
