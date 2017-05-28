(ns tetris.server.mimetype
  (:require [clojure.string :as s]))

(def mapping
  {"text/html" ["htm" "html"]
   "text/css" ["css"]
   "application/javascript" ["js"]})

(defn get-extention [uri] (last (s/split uri #"\.")))

(defn get-mime [uri]
  (let [ext (get-extention uri)
        contains-ext (fn [e] (some #(= ext %) e))]
    (reduce-kv
     #(if (empty? %1) (if (contains-ext %3) %2 %1) %1)
     "" mapping)))
