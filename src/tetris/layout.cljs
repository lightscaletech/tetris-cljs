(ns tetris.layout
  (:require
   [tetris.canvas :as can]
   [tetris.state :as state]))

(def ssize state/square-size)

(def space 1)

(def grid-padding (* space 1))
(def grid-width 10)
(def grid-height 20)

(defn cgrid-x [] (* grid-padding @ssize))
(defn cgrid-y [] (cgrid-x))
(defn cgrid-w [] (* grid-width @ssize))
(defn cgrid-h [] (* grid-height @ssize))

(def sidebar-x (+ grid-width (* grid-padding 2)))
(def sidebar-width 5)

(defn csidebar-width [] (* sidebar-width @ssize))
(defn csidebar-x [] (* sidebar-x @ssize))
(defn csidebar-y [] (* grid-padding @ssize))

(def width (+ space grid-width space sidebar-width space))
(def height (+ space grid-height space))

(def dialog-padding (* space 1))
(def dialog-width (- width (* dialog-padding 2)))
(def dialog-height (- height (* dialog-padding 2)))

(defn cdialog-x [] (* dialog-padding @ssize))
(defn cdialog-y [] (cdialog-x))
(defn cdialog-width [] (* dialog-width @ssize))
(defn cdialog-height [] (* dialog-height @ssize))

(defn resize []
  (let [{cw :w ch :h} (can/gsize)
        sq (.floor js/Math
                   (if (> (* (/ ch height) width) cw)
                     (/ cw width) (/ ch height)))]
    (reset! ssize sq)))
