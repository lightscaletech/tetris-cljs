(ns tetris.time)

(def state (atom {:time 0
                  :diff 0}))

(def move (atom 0))
(def down-interval (atom 0))
(def quick-down-active (atom false))
(def move-quick-interval 19)

(defn cur-time [] (:time @state))
(defn diff-time [] (:diff @state))

(defn now [] (.now js/Date))

(defn init [] (swap! state assoc :time (now)))
(defn tick []
  (let [n (now) t (cur-time)] (swap! state assoc :time n :diff (- t n))))

(defn set-move [] (reset! move (now)))
(defn check-move []
  (or (< (+ @move @down-interval) (now))
      (and @quick-down-active (< (+ @move move-quick-interval) (now)))))
