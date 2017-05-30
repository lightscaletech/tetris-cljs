(ns tetris.server.response
  (:require [clojure.data.json :as json]))

(defn- response [code headers body] {:status code :headers headers :body body})

(defn- json-response [code body]
  (response code {"Content-Type" "application/json"} (json/write-str body)))

(defn- error-response [code mes] (json-response code {:message mes}))
(defn client-error [mes] (error-response 400 mes))
(defn server-error [mes] (error-response 500 mes))
(defn success
  ([] (success {}))
  ([data] (json-response 200 (assoc data :message "Success"))))

;; Client error responses
(defn error-no-token [] (client-error "No token supplied"))
(defn error-no-score [] (client-error "No score supplied"))
(defn error-invalid-token [] (client-error "Invalid token supplied"))
(defn error-expired-token [] (client-error "Web token has expired"))
(defn error-before-token [] (client-error "Sent a new request too soon"))
