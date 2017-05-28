(ns tetris.server.web
  (:require [org.httpkit.server :as http]
            [tetris.server.api :as api]
            [tetris.server.mimetype :as mime]))

(def -server (atom nil))

(def default-file "index.html")
(def web-route "./target-dev/")

(defn get-file [uri]
  (try (slurp (str web-route uri))
       (catch Exception e nil)))

(defn response [uri]
  (if-let [d (get-file uri)]
    {:status 200
     :headers {"Content-Type" (mime/get-mime uri)}
     :body d}
    nil))

(defn static [r]
  (if-let [f (response (:uri r))]
    f
    (response default-file)))

(defn handler [r]
  (if-let [api-res (api/request r)]
    api-res
    (static r)))

(defn start [] (reset! -server (http/run-server #'handler {:port 8081})))
(defn stop [] (reset! -server nil))
