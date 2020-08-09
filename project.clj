(defproject jcpsantiago.sagemaker-multimodel-clj "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.8"]
                 [my-clj-boost "1.1.0"]
                 [metosin/jsonista "0.2.6"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 ; [io.pedestal/pedestal.jetty "0.5.8"]
                 [io.pedestal/pedestal.immutant "0.5.8"]]
                 ;; [io.pedestal/pedestal.tomcat "0.5.8"]

                 ; [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 ; [org.slf4j/jul-to-slf4j "1.7.26"]
                 ; [org.slf4j/jcl-over-slf4j "1.7.26"]
                 ; [org.slf4j/log4j-over-slf4j "1.7.26"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "jcpsantiago.sagemaker-multimodel-clj.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.8"]]}
             :uberjar {:aot [jcpsantiago.sagemaker-multimodel-clj.server]
                       :omit-source true}}
  :main ^{:skip-aot true} jcpsantiago.sagemaker-multimodel-clj.server
  :target-path "target/%s"
  :jar-name "sage.jar"
  :uberjar-name "ubersage.jar")
