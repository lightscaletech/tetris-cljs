(ns tetris.canvas)

(def obj-api    (atom nil))
(def obj-canvas (atom nil))

(defn canvas [] @obj-canvas)
(defn api [] @obj-api)

(defn init []
  )
