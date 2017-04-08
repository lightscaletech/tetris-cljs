(ns tetris.widgets
  (:require [tetris.canvas :as canvas]
            [tetris.state :as state]
            [tetris.input :as input]))

(def btn-fg-color "#000")
(def btn-bg-color "#EEE")
(def txt-color    "#FFF")

(def widg {:x 0 :y 0 :w 0 :h 0})

(defn real-point [n] (* n @state/square-size))

(defn make-widg [ren base attrs]
  (let [w (merge widg base attrs)
        r #(ren w %)]
    (assoc w :ren r)))

(def mouse-down-pos (atom nil))
(defn mouse-over [w pos]
  (let [wxl (-> w :x real-point (+ @state/game-x))
        wxr (-> w :w real-point (+ wxl))
        wyt (-> w :y dec real-point (+ @state/game-y))
        wyb (-> w :h real-point (+ wyt))
        {mx :x my :y} pos]
    (and (>= mx wxl) (<= mx wxr)
         (>= my wyt) (<= my wyb))))

(defn mouse-click [w]
  (when (and (not @input/mouse-click-down)
             (mouse-over w @mouse-down-pos))
    (reset! mouse-down-pos nil)
    ((:cb w)))
  (when @input/mouse-click-down (reset! mouse-down-pos @input/mouse-pos)))

(defn button-render [w state]
  (let [x (-> w :x real-point (+ @state/game-x))
        y (-> w :y dec real-point (+ @state/game-y))
        wi (-> w :w real-point)
        h (-> w :h real-point)
        bgc (:bg-color w)
        sp 2]
    (when (mouse-over w @input/mouse-pos)
      (canvas/draw-stroke-rectangle
       (- x (+ sp (/ sp 2))) (- y (+ sp (/ sp 2)))
       (+ wi (* sp 2) sp) (+ h (* sp 2) sp) bgc sp)
      (mouse-click w))
    (canvas/draw-rectangle x y wi h bgc)
    (canvas/draw-text
     (:text w) (-> w :size real-point)
     (+ x (real-point 0.6)) (+ y @state/square-size) (:fg-color w))))

(defn make-button [attrs]
  (make-widg button-render
             {:text "" :cb #() :size 0.6
              :bg-color btn-bg-color :fg-color btn-fg-color}
             attrs))

(defn text-line-render [w state]
  (canvas/draw-text
   ((if (:atom w) deref str) (:text w)) (-> w :size real-point)
   (-> w :x real-point (+ @state/game-x)) (-> w :y real-point (+ @state/game-y))
   (:color w)))

(defn make-text-line [attrs]
  (make-widg text-line-render
             {:text "" :atom false :size 1
              :color txt-color} attrs))

(defn render
  ([widgets] (render widgets (atom nil)))
  ([widgets state] (doseq [[k widg] widgets] ((:ren widg) state))))
