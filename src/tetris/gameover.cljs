(ns tetris.gameover
  (:require [tetris.layout :as lo]
            [tetris.state :as state]
            [tetris.canvas :as can]
            [tetris.widgets :as widg]))

(def focus (atom nil))
(def widgets
  {:go (widg/make-text-line
            {:x 4 :y 5 :size 1.6
             :text "GAME OVER"})
   :scoreh (widg/make-text-line
            {:x 4 :y 7 :size 0.9
             :text "Score: "})
   :score (widg/make-text-line
           {:x 8 :y 7 :size 0.9 :atom true :text state/score})
   :linesh (widg/make-text-line
            {:x 4 :y 8.5 :size 0.9
             :text "Lines: "})
   :lines (widg/make-text-line
           {:x 8 :y 8.5 :size 0.9 :atom true :text state/lines})
   :levelh (widg/make-text-line
            {:x 4 :y 10 :size 0.9
             :text "Level: "})
   :level (widg/make-text-line
           {:x 8 :y 10 :size 0.9 :atom true :text state/level})
   :btnrestart (widg/make-button
                {:x 7 :y 12 :w 4 :h 1 :text "RESTART"
                 :cb #(state/restart)})})

(defn render []
  (can/draw-rectangle
   (lo/cdialog-x) (lo/cdialog-y) (lo/cdialog-width) (lo/cdialog-height) "#333")
  (widg/render widgets focus))
