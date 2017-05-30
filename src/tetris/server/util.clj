(ns tetris.server.util
  (:require [tetris.server.sign :as sign]))

;; Extracting data from request body
(defn tok [r] (-> r :body :token sign/validate))
(defn score [r] (-> r :body :score))

;; Testing the token
(defn valid-token-state [t states] (some #(= (:state t) %) states))
