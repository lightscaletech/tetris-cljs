(ns tetris.game
  (:require [tetris.canvas :as canvas]
            [tetris.state :as state]
            [tetris.background :as bg]
            [tetris.grid :as grid]
            [tetris.sidebar :as sidebar]
            [tetris.gameover :as gameover]
            [tetris.startgame :as startgame]))

(declare frame)
(defn frame-loop [] (.requestAnimationFrame js/window frame))

(defn render-game []
  (grid/render)
  (sidebar/render))

(defn frame []
  (canvas/clear)
  (canvas/save)

  (bg/render)
  (condp = @state/game-state
    :start (startgame/render)
    :game (render-game)
    :gameover (gameover/render))

  (canvas/restore)
  (frame-loop))

(defn start []
  (state/reset-game)
  (frame-loop))
