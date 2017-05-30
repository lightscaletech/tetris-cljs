(ns tetris.server.sign
  (:require [clj-time.core :as tc]
            [tetris.server.config :as cfg]
            [buddy.sign.jwt :as jwt]))

(defn- mktimeout [timeout] (tc/plus (tc/now) (tc/seconds timeout)))
(defn- mkclaim [timeout data]
  (reduce-kv #(assoc %1 %2 %3)
             {:exp (mktimeout timeout)
              :iat (tc/now)
              :nbf (tc/plus (tc/now) (tc/seconds 3))} data))

(defn make [{:keys [timeout data]}]
  (jwt/sign (mkclaim timeout data) (cfg/sign :secret)))

(defn validate [tok] (jwt/unsign tok (cfg/sign :secret)))
