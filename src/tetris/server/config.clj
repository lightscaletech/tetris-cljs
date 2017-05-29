(ns tetris.server.config
  (:refer-clojure :exclude [load])
  (:require [taoensso.timbre :as log]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def -file-name (atom nil))
(def -config (atom nil))

(defn load [p]
  (log/info "Loading config file " p)
  (reset! -file-name p)
  (reset! -config (edn/read-string (slurp p))))

(defn reload [] (load @-file-name))

(defn subc [& ks] (get-in @-config ks))

(def web (partial subc :web))
(def db  (partial subc :db))
