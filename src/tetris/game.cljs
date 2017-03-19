(ns tetris.game
  (:require [tetris.canvas :as canvas]
            [tetris.background :as bg]
            [tetris.grid :as grid]))

(declare frame)
(defn frame-loop [] (.requestAnimationFrame js/window frame))

(defn frame []
  (canvas/clear)
  (canvas/save)

  (bg/render)
  (grid/render)

  (canvas/restore)
  (frame-loop))

(defn start []

  (frame-loop))
