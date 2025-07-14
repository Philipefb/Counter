(defproject contador "0.1.0-SNAPSHOT"
  :description "SPA contador"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [reagent "1.0.0"]
                 [compojure "1.7.0"]
                 [ring "1.9.6"]
                 [cheshire "5.11.0"]
                 [ring/ring-defaults "0.3.4"]
                 [com.datomic/peer "1.0.7075"]]

  :plugins [[lein-cljsbuild "1.1.8"]]
  :source-paths ["src"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js" "target"]
  :cljsbuild {:builds {:main {:source-paths ["src/contador/frontend"]
                              :compiler {:output-to "resources/public/js/app.js"
                                         :optimizations :whitespace
                                         :pretty-print true
                                         :main contador.frontend.frontend}}}}

  :main contador.core
  :aot [contador.core]
  :target-path "target/%s"
  :profiles {:uberjar {:omit-source true
                       :main contador.core
                       :aot :all}})
