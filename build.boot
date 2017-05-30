(def project-name "tetris-cljs2")
(def version "0.1.0")

(def jar-file (str project-name "_" version ".jar"))

(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs "2.0.0"]
                 [org.clojure/clojurescript "1.9.495"]
                 [adzerk/boot-reload "0.5.1"]

                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.json "0.2.6"]
                 [com.taoensso/timbre "4.10.0"]
                 [http-kit "2.2.0"]
                 [buddy/buddy-sign "1.5.0"]
                 [clj-time "0.13.0"]])

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[tetris.server.core :as tsc])

(deftask run []
  (with-pass-thru _
    (tsc/run {:config "config/dev.edn"})))

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

(deftask build []
  (comp (aot :all true)
        (uber)
        (jar :file jar-file
             :main 'tetris.server.core)
        #_(sift :include #{(re-pattern jar-file)})
        (target :dir #{"jar"})))
