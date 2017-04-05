(ns tetris.sidebar
  (:require [tetris.layout :as lo]
   [tetris.canvas :as canvas]
            [tetris.state :refer [square-size] :as state]
            [tetris.shapes :as shapes]
            [tetris.widgets :as w]))

(def pos-y (atom 0))
(def space 1)
(def mini-grid-w 5)
(def mini-grid-h 6)
(def sss (/ lo/sidebar-width mini-grid-w))
(def next-shape-height (* sss mini-grid-h))

(defn reset-pos-y [] (reset! pos-y 0))
(defn add-pos-y [n] (swap! pos-y + n))
(defn add-space-y [] (add-pos-y space))

(defn resize [])

(def heading-bg-color "#EFEFEF")
(def heading-fg-color "#111")
(def data-bg-color "#888")
(def data-fg-color "#EEE")

(defn render-text-block [t tc bc]
  (let [ss (lo/csidebar-ss)
        hx (lo/csidebar-x)
        hy (+ (lo/csidebar-y) (* @pos-y ss))]
    (canvas/draw-rectangle
     hx hy
     (* lo/sidebar-width ss) ss bc)
    (canvas/draw-text
     t (.round js/Math (/ ss 1.7))
     (+ hx (/ ss 3.5)) (+ ss hy) tc)
    (add-space-y)))

(defn render-heading [t]
  (render-text-block t heading-fg-color heading-bg-color))

(defn render-data [t]
    (render-text-block t data-fg-color data-bg-color))

(defn render-shape [gx gy shape]
  (let [ss (.ceil js/Math (+ (* (lo/csidebar-ss) sss)))
        sw (shapes/width shape)
        sh (shapes/height shape)
        sx (+ gx (* ss
                    (.round js/Math
                            (- (/ mini-grid-w 2) (/ (shapes/width shape) 2)))))
        sy (+ gy (* ss
                    (.round js/Math
                            (- (/ mini-grid-h 2) (/ (shapes/height shape) 2)))))]
    (doseq [[r cols] (map-indexed vector (:cords shape))]
      (doseq [[c v] (map-indexed vector cols)]
        (when (pos? v)
          (canvas/draw-rectangle
           (+ sx (* ss c)) (+ sy (* ss r))
           (+ 1 ss) (+ 1 ss)
           (:color shape)))))))

(defn render-next-shape []
  (let [ss (lo/csidebar-ss)
        gx (+ (lo/csidebar-x))
        gy (+ (lo/csidebar-y) (* @pos-y ss))]
    (canvas/draw-rectangle
     gx gy (* ss lo/sidebar-width) (* ss next-shape-height) "#999")
    (render-shape gx gy @state/next-shape)
    (add-pos-y next-shape-height)))

(def pause-btn  (w/make-button {:x lo/sidebar-x :y 14 :w lo/sidebar-width :h 1
                                  :text "Pause" :cb state/pause}))

(defn render []
  (reset-pos-y)
  (render-heading "NEXT")
  (render-next-shape)
  (add-pos-y 0.5)
  (render-heading "SCORE")
  (render-data @state/score)
  (render-heading "LINES")
  (render-data @state/lines)
  (render-heading "LEVEL")
  (render-data @state/level)
  (when (not @state/paused)
    ((:ren pause-btn))))
