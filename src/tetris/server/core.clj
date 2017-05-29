(ns tetris.server.core
  (:require [taoensso.timbre :as log]
            [clojure.tools.cli :as cli]
            [tetris.server.web :as web]
            [tetris.server.config :as cfg])
  (:gen-class))

(defn run [{:keys [config]}]
  (log/info "Starting Tetris server")
  (cfg/load config)
  (web/start))

(defn validate-config-file [p]
  (.exists (clojure.java.io/as-file p)))

(def cli-opts
  [["-c" "--config CONFIG" "Path to config file"
    :parse-fn #(str %)
    :validate [#(string? %) "Please specify a valid config file."]]
   ["-h" "--help"]])

(defn not-validate-args? [args]
  (or (-> args :errors empty? not)
      (-> args :options :help)
      (-> args :options :config empty?)))

(defn display-args-messages [args]
  (doseq [err (:errors args)] (println err))
  (println)
  (println (:summary args)))

(defn -main [& args]
  (let [pargs (cli/parse-opts args cli-opts)]
    (if (not-validate-args? pargs)
      (display-args-messages pargs)
      (run (:options pargs)))))
