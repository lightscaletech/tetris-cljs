(ns tetris.layout
  (:require
   [tetris.canvas :as can]
   [tetris.state :as state]))

(def ssize state/square-size)
(def start-x state/game-x)
(def start-y state/game-y)
(def space 1)

(def grid-padding (* space 1))
(def grid-width 10)
(def grid-height 20)

(defn cgrid-y [] (+ @start-y (* grid-padding @ssize)))
(defn cgrid-x [] (+ @start-x (* grid-padding @ssize)))
(defn cgrid-w [] (* grid-width @ssize))
(defn cgrid-h [] (* grid-height @ssize))

(def sidebar-mult 1)
(def sidebar-x (+ grid-padding grid-width (/ grid-padding 2)))
(def sidebar-width 3.25)

(defn csidebar-ss [] (* @ssize sidebar-mult))
(defn csidebar-width [] (* sidebar-width (csidebar-ss)))
(defn csidebar-x [] (+ @start-x (* sidebar-x @ssize)))
(defn csidebar-y [] (+ @start-y (* grid-padding @ssize)))

(def width (+ space grid-width (* space 0.5) sidebar-width space))
(def height (+ space grid-height space))

(defn cwidth [] (* width @ssize))
(defn cheight [] (* height @ssize))

(def dialog-padding (* space 1))
(def dialog-width (- width (* dialog-padding 2)))
(def dialog-height (- height (* dialog-padding 2)))

(defn cdialog-y [] (+ @start-y (* dialog-padding @ssize)))
(defn cdialog-x [] (+ @start-x (* dialog-padding @ssize)))
(defn cdialog-width [] (* dialog-width @ssize))
(defn cdialog-height [] (* dialog-height @ssize))

(def pause-modal-w 5)
(def pause-modal-x (- (/ width 2) (/ pause-modal-w 2)))

(defn cpause-modal-x  []  (+ @start-x (* pause-modal-x @ssize)))
(defn cpause-modal-sy [h] (- (/ height 2) (/ h 2)))
(defn cpause-modal-y  [h] (+ @start-y (* (- (/ height 2) (/ h 2)) @ssize)))
(defn cpause-modal-w  []  (* pause-modal-w @ssize))

(defn resize []
  (let [{cw :w ch :h} (can/gsize)
        sq (.ceil js/Math
                   (if (> (* (/ ch height) width) cw)
                     (/ cw width) (/ ch height)))
        sx (.round js/Math (- (/ cw 2) (/ (* sq width) 2)))
        sy (.round js/Math (- (/ ch 2) (/ (* sq height) 2)))]
    (reset! ssize sq)
    (reset! start-x sx)
    (reset! start-y sy)))
