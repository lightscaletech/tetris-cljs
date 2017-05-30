(ns tetris.server.api.game
  (:require [tetris.server.util :as u]
            [tetris.server.sign :as sign]
            [tetris.server.response :as resp]))

(defn start [r]
  (resp/success
   {:token (sign/make {:timeout 180 :data {:state "start"
                                           :score 0}})}))

(defn end [r]
  (resp/success
   {:token (sign/make {:timeout (* 60 60 48)
                       :data {:state "gameover"
                              :score 0}})}))

(defn pause [r]
  (resp/success
   {:token (sign/make {:timeout (* 60 60 48)
                       :score 0})}))

(defn unpause [r]
  (resp/success
   {:token (sign/make {:timeout (* 60 60 48)
                       :score 0})}))

(defn request [r]
  (condp = (-> r :uri last)
    :start (start r)
    :end (end r)
    :pause (pause r)
    :unpause (unpause r)
    nil))
