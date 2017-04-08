(ns tetris.state
  (:require [tetris.shapes :as shapes]
            [tetris.input :as input]
            [tetris.time :as time]
            [tetris.highscore :as hs]))

(def game-state (atom nil))
(def paused (atom nil))

(def square-size (atom 0))
(def game-x (atom 0))
(def game-y (atom 0))

(def shapes (atom []))
(def ns-control-block (atom false))
(def next-shape (atom nil))
(def current-shape (atom nil))

(defn make-shape [shape gw]
  (assoc shape
         :opposite false
         :rot :0
         :pos-x (- (/ gw 2)
                   (.round js/Math (-> shape :cords first count (/ 2))))
         :pos-y (-> shape :cords count - (+ 1))))

(defn load-shape [gw]
  (if (input/active?) (reset! ns-control-block true))
  (reset! current-shape @next-shape)
  (reset! next-shape (make-shape (shapes/pick-shape) gw))
  (time/set-move))

(def score-line  10)
(def score-shape 5)
(def score (atom 0))
(def lines (atom 0))
(def level (atom 1))
(def shape-count (atom 0))
(def countdown (atom false))

(def level-gap 10)

(def down-speed time/down-interval)
(def speed-increase 15)

(def quick-down-active time/quick-down-active)

(defn add-score [s] (hs/update-highscore (swap! score + s)))

(defn level-up []
  (when (>= @lines (* @level level-gap))
    (swap! level inc)
    (swap! down-speed * (/ (- 100 speed-increase) 100))))

(defn add-lines [l]
  (swap! lines + l)
  (add-score (* l l score-line))
  (level-up))

(defn add-shape []
  (swap! shape-count inc)
  (add-score score-shape))

(defn set-game-state [s] (reset! game-state s))

(defn reset-game []
  (reset! hs/new-highscore false)
  (reset! paused false)
  (reset! down-speed 900)
  (reset! score 0)
  (reset! lines 0)
  (reset! level 1)
  (reset! shapes [])
  (reset! next-shape nil)
  (reset! current-shape nil))

(defn start-game []
  (reset! countdown true)
  (set-game-state :game)
  (time/set-move))

(defn gameover [] (set-game-state :gameover))
(defn restart []
  (reset! countdown true)
  (reset-game))

(defn startscreen []
  (set-game-state :start)
  (reset-game))

(defn pause [] (reset! paused true))
(defn unpause []
  (time/set-move)
  (reset! paused false))
