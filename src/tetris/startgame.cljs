(ns tetris.startgame
  (:require [tetris.layout :as lo]
            [tetris.state :as state]
            [tetris.canvas :as can]
            [tetris.widgets :as widg]))

(def focus (atom nil))
(def widgets
  {:title (widg/make-text-line
           {:x 4.4 :y 8 :size 1.8 :text "CLOJOZ"})
   :startbtn (widg/make-button
              {:x 5.3 :y 12 :w 5.1 :h 1 :text "START GAME"
               :cb #(state/start-game)})})

(defn render []
  (can/draw-rectangle
   (lo/cdialog-x) (lo/cdialog-y) (lo/cdialog-width) (lo/cdialog-height) "#333")
  (widg/render widgets focus))
