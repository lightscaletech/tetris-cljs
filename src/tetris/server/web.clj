(ns tetris.server.web
  (:require [taoensso.timbre :as log]
            [org.httpkit.server :as http]
            [tetris.server.api :as api]
            [tetris.server.mimetype :as mime]
            [tetris.server.config :as cfg]))

(def -server (atom nil))

(defn get-file [uri]
  (try (slurp (str (cfg/web :document-root) uri))
       (catch Exception e nil)))

(defn response [uri]
  (if-let [d (get-file uri)]
    (do (log/info "Serving static file: " uri)
        {:status 200
         :headers {"Content-Type" (mime/get-mime uri)}
         :body d})
    nil))

(defn static [r]
  (if-let [f (response (:uri r))]
    f
    (response (cfg/web :default-file))))

(defn handler [r]
  (if-let [api-res (api/request r)]
    api-res
    (static r)))

(defn start []
  (log/info "Starting web server on port: " (cfg/web :port))
  (reset! -server (http/run-server #'handler {:port (cfg/web :port)})))

(defn stop []
  (log/info "Stopping web server")
  (reset! -server nil))
