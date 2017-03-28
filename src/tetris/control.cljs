(ns tetris.control)

(def up-active    (atom false))
(def down-active  (atom false))
(def left-active  (atom false))
(def right-active (atom false))
(def space-active (atom false))
(def enter-active (atom false))

(def mouse-pos    (atom {:x 0 :y 0}))
(def mouse-click-down   (atom false))

(defn- up-down [e]    (reset! up-active true))
(defn- up-up [e]      (reset! up-active false))
(defn- down-down [e]  (reset! down-active true))
(defn- down-up [e]    (reset! down-active false))
(defn- left-down [e]  (reset! left-active true))
(defn- left-up [e]    (reset! left-active false))
(defn- right-down [e] (reset! right-active true))
(defn- right-up [e]   (reset! right-active false))
(defn- enter-down [e] (reset! enter-active true))
(defn- enter-up [e]   (reset! enter-active false))
(defn- space-down [e] (reset! space-active true))
(defn- space-up [e]   (reset! space-active false))


(def key-mapping {37 {:down left-down
                      :up left-up}
                  38 {:down up-down
                      :up up-up}
                  39 {:down right-down
                      :up right-up}
                  40 {:down down-down
                      :up down-up}
                  32 {:down space-down
                      :up space-up}
                  13 {:down enter-down
                      :up enter-up}})

(defn key-down [e]
  (if-let [key-map (key-mapping (.-keyCode e))]
    ((:down key-map))))
(defn key-up [e]
  (if-let [key-map (key-mapping (.-keyCode e))]
    ((:up key-map))))

(defn mouse-move [e] (reset! mouse-pos {:x (.-offsetX e) :y (.-offsetY e)}))
(defn mouse-down [e]
  (mouse-move e)
  (reset! mouse-click-down true))

(defn mouse-up   [e]
  (mouse-move e)
  (reset! mouse-click-down false))

(defn init []
  (let [win js/window]
    (.addEventListener win "keydown" key-down)
    (.addEventListener win "keyup" key-up)
    (.addEventListener win "mousemove" mouse-move)
    (.addEventListener win "mouseup" mouse-up)
    (.addEventListener win "mousedown" mouse-down)))
