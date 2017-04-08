(ns tetris.control)

(def up-active    (atom false))
(def down-active  (atom false))
(def left-active  (atom false))
(def right-active (atom false))
(def space-active (atom false))
(def enter-active (atom false))

(def touch-pos (atom {:x 0 :y 0}))
(def touch-start-pos (atom {:x 0 :y 0}))
(def touch-down (atom false))

(def mouse-pos    (atom {:x 0 :y 0}))
(def mouse-click-down   (atom false))

(def active?
  #(or @touch-down @mouse-click-down
       @up-active @down-active @left-active @right-active
       @space-active @enter-active))

(def key-mapping {37 left-active
                  38 up-active
                  39 right-active
                  40 down-active
                  32 space-active
                  13 enter-active})

(defn key-down [e] (if-let [a (key-mapping (.-keyCode e))] (reset! a true)))
(defn key-up [e] (if-let [a (key-mapping (.-keyCode e))] (reset! a false)))
(defn mouse-move [e] (reset! mouse-pos {:x (.-offsetX e) :y (.-offsetY e)}))
(defn mouse-down [e]
  (mouse-move e)
  (reset! mouse-click-down true))
(defn mouse-up   [e]
  (mouse-move e)
  (reset! mouse-click-down false))

(defn mtouch-pos [e]
  (let [t (-> e .-changedTouches (aget 0))]
    {:x (.-clientX t) :y (.-clientY t)}))

(defn touch-down! [e]
  (.preventDefault e)
  (let [p (mtouch-pos e)]
    (reset! mouse-pos p)
    (reset! touch-start-pos p)
    (reset! touch-pos p))
  (reset! mouse-click-down true)
  (reset! touch-down true))
(defn touch-up! [e]
  (let [p (mtouch-pos e)]
    (reset! mouse-pos p)
    (reset! touch-start-pos {:x 0 :y 0})
    (reset! touch-pos p))
  (reset! mouse-click-down false)
  (reset! touch-down false))
(defn touch-move! [e]
  (.preventDefault e)
  (let [p (mtouch-pos e)]
    (reset! mouse-pos p)
    (reset! touch-pos p)))
(defn touch-diff []
  (when @touch-down
    {:x (- (:x @touch-pos) (:x @touch-start-pos))
     :y (- (:y @touch-pos) (:y @touch-start-pos))}))

(defn on [ev cb] (.addEventListener (.-body js/document) ev cb
                                    (clj->js {:passive false})))
(defn init []
  (on "keydown"    key-down)
  (on "keyup"      key-up)
  (on "mousemove"  mouse-move)
  (on "mouseup"    mouse-up)
  (on "mousedown"  mouse-down)
  (on "touchstart" touch-down!)
  (on "touchend"   touch-up!)
  (on "touchmove"  touch-move!))
