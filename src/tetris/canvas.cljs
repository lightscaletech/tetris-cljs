(ns tetris.canvas)

(def obj-api    (atom nil))
(def obj-canvas (atom nil))

(defn canvas [] @obj-canvas)
(defn api [] @obj-api)

(defn init []
  (reset! obj-canvas (.getElementById js/document "canvas"))
  (reset! obj-api    (.getContext (canvas) "2d")))
