(ns gravity.cli.launcher
  "Bootstrap-hosted CLI launcher with a bootstrap-free presentation path."
  (:require [gravity.cli :as cli]
            [gravity.cli.commands.bootstrap :as bootstrap-commands]
            [gravity.cli.commands.compiler :as compiler-commands]
            [gravity.cli.commands.platform :as platform-commands]
            [gravity.cli.entrypoint :as cli-entrypoint]))

(def ^:private bootstrap-command-names
  (into #{"--assert-seedless-release" "assert-seedless-release"
          "test" "self-host" "compile" "check" "run" "run-compiled" "build"}
        (concat (keys bootstrap-commands/path-commands)
                (keys bootstrap-commands/no-argument-commands)
                (keys bootstrap-commands/optional-path-commands)
                (keys compiler-commands/commands)
                (keys platform-commands/commands))))

(defn- packaged-jvm-cli?
  []
  (= "true" (System/getProperty "gravity.packaged.jvm.cli")))

(defn- bootstrap-main
  []
  (require 'gravity.bootstrap)
  (or (ns-resolve 'gravity.bootstrap '-main)
      (throw (IllegalStateException.
              "bootstrap CLI entrypoint is unavailable"))))

(defn- exit!
  [status]
  (System/exit status))

(defn- usage-error!
  []
  (binding [*out* *err*]
    (println (cli/cli-usage-text)))
  (exit! 2))

(defn -main
  [& arguments]
  (case (first arguments)
    ("help" "--help" "-h")
    (print (cli/p18-cli-help-text (packaged-jvm-cli?)))

    ("version" "--version")
    (prn (cli/p18-cli-version-record (packaged-jvm-cli?)))

    nil (usage-error!)
    (if (= "build" (first arguments))
      (cli-entrypoint/run-build! arguments exit!)
      (if (contains? bootstrap-command-names (first arguments))
        (apply (bootstrap-main) arguments)
        (usage-error!)))))
