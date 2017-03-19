(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs "2.0.0"]
                 [adzerk/boot-reload "0.5.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [pandeiro/boot-http "0.7.6"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[pandeiro.boot-http :refer [serve]])

(deftask dev []
  (comp (watch)
        (speak)
        (reload :on-jsload 'tetris.core/onload)
        (cljs :source-map true)
        (serve :port 8080)
        (target )))
