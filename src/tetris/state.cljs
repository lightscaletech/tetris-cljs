(ns tetris.state
  (:require [tetris.shapes :as shapes]))

(def game-state (atom nil))

(def square-size (atom 0))

(def shapes (atom []))
(def next-shape (atom nil))
(def current-shape (atom nil))

(defn make-shape [shape gw]
  (assoc shape
         :opposite false
         :flip-x false
         :flip-y false
         :pos-x (- (/ gw 2)
                   (.round js/Math (-> shape :cords first count (/ 2))))
         :pos-y (-> shape :cords count - (+ 1))))

(defn load-shape [gw]
  (reset! current-shape @next-shape)
  (reset! next-shape (make-shape (shapes/pick-shape) gw)))

(def score (atom 0))
(def lines (atom 0))
(def level (atom 1))

(def level-gap 10)

(def quick-down-active (atom false))

(defn level-up [] (when (>= @lines (* @level level-gap)) (swap! level inc)))

(defn add-lines [l]
  (swap! lines + l)
  (swap! score + (* l l 10))
  (level-up))

(defn set-game-state [s] (reset! game-state s))

(defn reset-game []
  (set-game-state :start)
  (reset! score 0)
  (reset! lines 0)
  (reset! level 1)
  (reset! shapes [])
  (reset! next-shape nil)
  (reset! current-shape nil))

(defn start-game [] (set-game-state :game))
(defn gameover [] (set-game-state :gameover))
(defn restart [] (reset-game))
