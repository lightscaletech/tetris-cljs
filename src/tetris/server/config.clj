(ns tetris.server.config
  (:refer-clojure :exclude [load])
  (:require [clojure.edn :as edn]))

(def -file-name (atom nil))
(def -config (atom nil))

(defn load [p]
  (reset! -file-name p)
  ())
