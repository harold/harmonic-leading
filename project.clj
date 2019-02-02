(defproject harmonic-leading "0.1.0-SNAPSHOT"
  :description "Audio experiments with continuous harmony."
  :url "https://github.com/harold/harmonic-leading"
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot harmonic-leading.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
