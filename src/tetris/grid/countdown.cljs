(ns tetris.grid.countdown
  (:require [tetris.time :as time]
            [tetris.state :as s]
            [tetris.canvas :as can]
            [tetris.layout :as lo]))

(def nums {3 {:cords [[0 1 1 0]
                      [1 0 0 1]
                      [0 0 1 0]
                      [1 0 0 1]
                      [0 1 1 0]]}
           2 {:cords [[0 1 1 0]
                      [1 0 0 1]
                      [0 0 0 1]
                      [0 0 1 0]
                      [1 1 1 1]]}
           1 {:cords [[0 0 1 0]
                      [0 1 1 0]
                      [0 0 1 0]
                      [0 0 1 0]
                      [0 1 1 1]]}})

(def interval 1000)
(def change-time (atom nil))
(def current (atom nil))

(defn init []
  (s/load-shape lo/grid-width)
  (reset! change-time (time/now))
  (reset! current 3))

(defn deinit []
  (reset! change-time nil)
  (reset! current nil))

(defn change []
  (reset! change-time (time/now))
  (swap! current dec))

(defn last? [] (= @current 1))

(defn timeout? [] (> (- (time/now) @change-time) interval))

(defn render-num [{:keys [cords]}]
  (let [gx (lo/cgrid-x) gy (lo/cgrid-y)
        mp! #(+ %1 (* @lo/ssize (+ %2 %3)))]
      (doseq [[r cols] (map-indexed vector cords)]
        (doseq [[c v] (map-indexed vector cols)]
          (when (pos? v)
            (can/draw-rectangle
             (mp! gx 3 c) (mp! gy 7 r)
             @lo/ssize @lo/ssize
             "#FFF"))))))

(defn render []
  (when (and (nil? @change-time) (nil? @current)) (init))
  (render-num (get nums @current))
  (if (timeout?)
    (if (last?)
      (do (deinit) false)
      (do (change) true))
    true))
