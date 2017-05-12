(ns tetris.grid.gameover
  (:require [tetris.state :as s]
            [tetris.canvas :as can]
            [tetris.layout :as lo]
            [tetris.grid.utils :as u]
            [tetris.time :as time]))

(def foreground-color "#999")
(def background-color "#333")

(def line-int 60)
(def end-int 1000)

(def last-line (atom nil))
(def ended (atom nil))

(def gameover (atom false))
(def lines (atom []))

(defn reset-go! []
  (reset! gameover false)
  (reset! last-line nil)
  (reset! ended nil)
  (reset! lines []))

(defn gameover? [] @gameover)
(defn gameover! []
  (reset! gameover true))

(defn finish? [] (and @ended (< (+ @ended end-int) (time/now))))
(defn finish! []
  (s/gameover)
  (reset-go!))

(defn render-line [r l]
  (let [len (count l)
        ss @s/square-size
        py (+ (lo/cgrid-y) (* (- lo/grid-height r) ss))]
    (loop [c 0]
      (when (< c len)
        (can/draw-rectangle
         (+ (lo/cgrid-x) (* ss c)) py ss ss
         (if (pos? (get l c)) foreground-color background-color))
        (recur (inc c))))))

(defn render-lines []
  (let [len (count @lines)]
    (loop [r 1]
      (when (<= r len)
        (render-line r (get @lines r))
        (recur (inc r))))))

(defn next-line [] (- lo/grid-height (count @lines)))

(defn load-line? []
  (and (nil? @ended)
       (or (nil? @last-line)
           (< (+ @last-line line-int) (time/now)))))

(defn make-line! [l]
  (let [shps (u/shapes-in-line l)
        line (vec (replicate lo/grid-width 0))]
    (reduce
     (fn [c s] (reduce-kv #(assoc %1 (+ (:pos-x s) %2) %3) c (:cords s)))
     line shps)))

(defn load-line! []
  (swap! lines conj (make-line! (next-line)))
  (reset! last-line (time/now)))

(defn all-loaded? [] (and (not @ended) (= (count @lines) (inc lo/grid-height))))
(defn set-ended! [] (reset! ended (time/now)))

(defn render []
  (render-lines)
  (and (all-loaded?) (set-ended!))
  (and (load-line?) (load-line!))
  (and (finish?) (finish!)))
