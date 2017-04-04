(ns tetris.time
  (:require [tetris.state :as s]))

(def state (atom {:time 0
                  :diff 0}))

(def move (atom 0))
(def move-quick-interval 19)

(defn cur-time [] (:time @state))
(defn diff-time [] (:diff @state))

(defn now [] (.now js/Date))

(defn init [] (swap! state assoc :time (now)))
(defn tick []
  (let [n (now) t (cur-time)] (swap! state assoc :time n :diff (- t n))))

(defn set-move [] (reset! move (now)))
(defn check-move []
  (or (< (+ @move @s/down-speed) (now))
      (and @s/quick-down-active (< (+ @move move-quick-interval) (now)))))
