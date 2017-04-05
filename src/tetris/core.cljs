(ns tetris.core
  (:require [tetris.canvas :as canvas]
            [tetris.sidebar :as sidebar]
            [tetris.layout :as lo]
            [tetris.game :as game]
            [tetris.control :as control]))

(defn resize []
  (canvas/resize)
  (lo/resize)
  (sidebar/resize))

(defn ^:export main []
  (.addEventListener js/window "resize" resize true)
  (canvas/init)
  (lo/resize)
  (control/init)
  (game/start))
