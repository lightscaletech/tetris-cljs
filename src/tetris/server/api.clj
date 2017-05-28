(ns tetris.server.api)

(defn getr [r]
  )

(defn postr [r]
  )

(defn request [r]
  (condp = (:request-method r)
    :get (getr r)
    :post (postr r)
    nil))
