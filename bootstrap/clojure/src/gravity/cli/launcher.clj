(ns gravity.cli.launcher
  "Bootstrap-hosted CLI launcher with a bootstrap-free presentation path."
  (:require [gravity.cli :as cli]))

(defn- packaged-jvm-cli?
  []
  (= "true" (System/getProperty "gravity.packaged.jvm.cli")))

(defn- bootstrap-main
  []
  (require 'gravity.bootstrap)
  (or (ns-resolve 'gravity.bootstrap '-main)
      (throw (IllegalStateException.
              "bootstrap CLI entrypoint is unavailable"))))

(defn -main
  [& arguments]
  (case (first arguments)
    ("help" "--help" "-h")
    (print (cli/p18-cli-help-text (packaged-jvm-cli?)))

    ("version" "--version")
    (prn (cli/p18-cli-version-record (packaged-jvm-cli?)))

    (apply (bootstrap-main) arguments)))
