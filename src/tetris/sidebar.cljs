(ns tetris.sidebar
  (:require [tetris.layout :as lo]
   [tetris.canvas :as canvas]
            [tetris.state :refer [square-size] :as state]
            [tetris.shapes :as shapes]))

(def pos-y (atom 0))
(def space 1)
(def width 5)
(def next-shape-height 6)

(defn reset-pos-y [] (reset! pos-y 0))
(defn add-pos-y [n] (swap! pos-y + n))
(defn add-space-y [] (add-pos-y space))

(defn resize [])

(def heading-bg-color "#EFEFEF")
(def heading-fg-color "#111")
(def data-bg-color "#888")
(def data-fg-color "#EEE")

(defn render-text-block [t tc bc]
  (let [ss @square-size
        hx (lo/csidebar-x)
        hy (+ (lo/csidebar-y) (* @pos-y ss))]
    (canvas/draw-rectangle
     hx hy
     (* width @square-size) ss bc)
    (canvas/draw-text
     t (/ ss 1.7)
     (+ hx (/ ss 3.5)) (+ ss hy) tc)
    (add-space-y)))

(defn render-heading [t]
  (render-text-block t heading-fg-color heading-bg-color))

(defn render-data [t]
    (render-text-block t data-fg-color data-bg-color))

(defn render-shape [gx gy shape]
  (let [ss @square-size
        sw (shapes/width shape)
        sh (shapes/height shape)
        sx (+ gx (* ss
                    (.floor js/Math
                            (- (/ width 2) (/ (shapes/width shape) 2)))))
        sy (+ gy (* ss
                    (.round js/Math
                            (- (/ next-shape-height 2) (/ (shapes/height shape) 2)))))]
    (doseq [[r cols] (map-indexed vector (:cords shape))]
      (doseq [[c v] (map-indexed vector cols)]
        (when (pos? v)
          (canvas/draw-rectangle
           (+ sx (* ss c)) (+ sy (* ss r))
           ss ss
           (:color shape)))))))

(defn render-next-shape []
  (let [ss @square-size
        gx (+ (lo/csidebar-x))
        gy (+ (lo/csidebar-y) (* @pos-y ss))]
    (canvas/draw-rectangle
     gx gy (* ss lo/sidebar-width) (* ss next-shape-height) "#999")
    (render-shape gx gy @state/next-shape)
    (add-pos-y next-shape-height)))

(defn render []
  (reset-pos-y)
  (render-heading "NEXT SHAPE")
  (render-next-shape)
  (add-space-y)
  (render-heading "SCORE")
  (render-data @state/score)
  (render-heading "LINES")
  (render-data @state/lines)
  (render-heading "LEVEL")
  (render-data @state/level))
