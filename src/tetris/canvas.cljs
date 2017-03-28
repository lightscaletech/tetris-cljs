(ns tetris.canvas)

(def obj-api    (atom nil))
(def obj-canvas (atom nil))
(def size       (atom {:w 0 :h 0}))

(defn canvas [] @obj-canvas)
(defn api [] @obj-api)

(defn get-window-size []
  (let [w (.-innerWidth js/window)
        h (.-innerHeight js/window)]
    [w h]))

(defn set-size [w h] (reset! size {:w w :h h}))

(defn gsize
  ([] @size)
  ([k] (k @size)))

(defn resize
  ([] (resize nil))
  ([ev]
   (let [[w h] (get-window-size)]
     (set-size w h)
     (set! (.-width (canvas)) w)
     (set! (.-height (canvas)) h))))

(defn init []
  (reset! obj-canvas (.getElementById js/document "canvas"))
  (reset! obj-api    (.getContext (canvas) "2d"))
  (resize))

(defn clear []    (.clearRect (api) 0 0 (gsize :w) (gsize :h)))
(defn save []     (.save (api)))
(defn restore  [] (.restore (api)))

(defn draw-rectangle [x y w h c]
  (save)
  (set! (.-fillStyle (api)) c)
  (.fillRect (api) x y w h)
  (restore))

(defn draw-stroke-rectangle [x y w h c s]
  (save)
  (set! (.-strokeStyle (api)) c)
  (set! (.-lineWidth (api)) s)
  (.strokeRect (api) x y w h)
  (restore))

(defn draw-text [text s x y c]
  (save)
  (set! (.-font (api)) (str s "px arial"))
  (set! (.-fillStyle (api)) c)
  (.fillText (api) text x (- y (/ s 2)))
  (restore))
