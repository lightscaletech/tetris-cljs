(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs "2.0.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.495"]
                 [adzerk/boot-reload "0.5.1"]

                 [org.clojure/tools.nrepl "0.2.12"]
                 [pandeiro/boot-http "0.7.6"]
                 [http-kit "2.2.0"]
                 [compojure "1.6.0"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[tetris.server.core :as tsc])

(deftask run []
  (tsc/-main))

(deftask dev []
  (comp (watch)
        (speak)
        (reload :on-jsload 'tetris.core/main)
        (cljs)
        (target :dir #{"target-dev"})
        (run)))

(deftask prod []
  (comp (cljs :optimizations :advanced
              :output-dir "../main-out")
        (target :dir #{"target-prod"})))

(deftask prod-dev []
  (comp (watch)
        (speak)
        (prod)))
