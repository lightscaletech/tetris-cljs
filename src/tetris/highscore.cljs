(ns tetris.highscore)

(def local-storage (.-localStorage js/window))
(def set-local-storage #(.setItem local-storage %1 %2))
(def get-local-storage #(.getItem local-storage %))

(def hs-ls-key ::highscore)
(def highscore (atom nil))
(def new-highscore (atom false))

(defn set-highscore [s]
  (set-local-storage hs-ls-key s)
  (reset! highscore s))

(defn update-highscore [s]
  (when (> s @highscore)
    (set-highscore s)
    (reset! new-highscore true)))

(if-let [hs (get-local-storage hs-ls-key)]
  (reset! highscore hs)
  (set-highscore 0))
