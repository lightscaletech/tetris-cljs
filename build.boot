(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs "2.0.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.495"]
                 [adzerk/boot-reload "0.5.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [pandeiro/boot-http "0.7.6"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[pandeiro.boot-http :refer [serve]])

(deftask dev []
  (comp (watch)
        (speak)
        (reload :on-jsload 'tetris.core/main)
        (cljs)
        (serve :port 8080 :dir "target-dev")
        (target :dir #{"target-dev"})))

(deftask prod []
  (comp (cljs :optimizations :advanced
              :output-dir "../main-out")
        (target :dir #{"target-prod"})))

(deftask prod-dev []
  (comp (watch)
        (speak)
        (serve :port 8080 :dir "target-prod")
        (prod)))
