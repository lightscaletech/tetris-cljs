(ns tetris.server.api.score
  (:require [tetris.server.util :as u]
            [tetris.server.response :as resp]
            [tetris.server.sign :as sign]))

(defn add [r]
  (if-let [t (u/tok r)]
    (if (u/valid-token-state t ["start" "unpause" "score"])
      (if-let [s (u/score r)]
        (resp/success
         {:token
          (sign/make {:timeout (* 60 3)
                      :data {:state "score"
                             :score s}})})
        (resp/error-no-score))
      (resp/error-invalid-token))
    (resp/error-no-token)))

(defn request [r]
  (condp = (-> r :uri last)
    :add (add r)
    nil))
