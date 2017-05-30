(ns tetris.server.api
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [tetris.server.api.game :as game]
            [tetris.server.api.score :as score]
            [tetris.server.response :as resp]))

(defn- decode-uri-component [uc] (java.net.URLDecoder/decode uc))
(defn- split-chunks [url] (str/split url #"&"))
(defn- split-equals [chunk] (str/split chunk #"="))
(defn query-params-to-map [qp]
  (when qp
    (reduce #(let [kv (split-equals %2)]
               (assoc %1 (-> kv first keyword) (-> kv last decode-uri-component)))
            {} (split-chunks qp))))

(defn- parse-body [r]
  (condp = (-> r :headers (get "content-type"))
    "application/json" (json/read-str (-> r :body slurp str) :key-fn keyword)
    nil))

(defn- prep-request [r]
  (assoc r
         :uri (map keyword (-> r :uri (str/split #"/") rest))
         :query-string (-> r :query-string query-params-to-map)
         :body (when (:body r) (parse-body r))))

(defn request [r]
  (try
    (let [r' (prep-request r)]
      (condp = (-> r' :uri first)
        :game (game/request r')
        :score (score/request r')
        nil))
    (catch Exception e
      (cond
        (-> e ex-data :cause (= :exp)) (resp/error-expired-token)
        (-> e ex-data :cause (= :nbf)) (resp/error-before-token)
        :else (resp/server-error "There was an error with the server.")))))
