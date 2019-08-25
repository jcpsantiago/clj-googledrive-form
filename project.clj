(defproject jcpsantiago.googledriveform "0.1.0"
  :description "Web form that uploads a file to google drive"
  :url "https://clj-googledrive-form.herokuapp.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.2"]
                 [org.clojure/core.async "0.4.500"]
                 [cheshire "5.8.1"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [http-kit "2.4.0-alpha3"]
                 [proto-repl "0.3.1"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :main ^:skip-aot googledriveform.core
  :min-lein-version "2.0.0"
  :uberjar-name "googledriveform.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
