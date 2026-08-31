(ns gravity.cli.dispatch
  "Bootstrap command routing over explicitly injected operations."
  (:require [gravity.cli.commands.bootstrap :as bootstrap-commands]
            [gravity.cli.commands.compiler :as compiler-commands]
            [gravity.cli.commands.platform :as platform-commands]
            [gravity.cli.compile-command :as compile-command]))

(def ^:private path-commands
  (merge compiler-commands/commands
         platform-commands/commands
         bootstrap-commands/path-commands))

(def ^:private special-commands
  #{"help" "--help" "-h" "--version" "version"
    "--assert-seedless-release" "assert-seedless-release"
    "test" "self-host" "compile" "check" "run" "run-compiled"})

(defn- invoke
  [resolve-operation operation & arguments]
  (apply (resolve-operation operation) arguments))

(defn- operation-value
  [resolve-operation operation]
  (var-get (resolve-operation operation)))

(defn- print-artifact!
  [resolve-operation operation arguments]
  (prn (apply invoke resolve-operation operation arguments)))

(defn- dispatch-generic!
  [resolve-operation command path]
  (cond
    (contains? path-commands command)
    (do (print-artifact! resolve-operation (get path-commands command) [path])
        true)

    (contains? bootstrap-commands/no-argument-commands command)
    (do (print-artifact! resolve-operation
                         (get bootstrap-commands/no-argument-commands command)
                         [])
        true)

    (contains? bootstrap-commands/optional-path-commands command)
    (let [[operation default-operation]
          (get bootstrap-commands/optional-path-commands command)]
      (print-artifact!
       resolve-operation operation
       [(or path (operation-value resolve-operation default-operation))])
      true)

    :else false))

(defn- run-test-command!
  [resolve-operation arguments]
  (if (seq (rest arguments))
    (invoke resolve-operation 'p18-t04-public-test-overclaim! arguments)
    (print-artifact! resolve-operation
                     'p18-t04-public-test-command-artifact! [])))

(defn- run-command!
  [resolve-operation arguments]
  (let [path (second arguments)
        request (invoke resolve-operation 'p18-t04-parse-run-request arguments)]
    (if (:runtime-derived-requested? request)
      (print (:stdout
              (invoke resolve-operation
                      'p18-t04-run-runtime-derived-c-file! request)))
      (print (invoke resolve-operation 'run-file path)))))

(defn dispatch!
  "Dispatches arguments. Returns false only when the command is unknown."
  [resolve-operation arguments]
  (let [[command path] arguments]
    (if (contains? special-commands command)
      (do
        (case command
          "help" (print (invoke resolve-operation 'p18-cli-help-text))
          "--help" (print (invoke resolve-operation 'p18-cli-help-text))
          "-h" (print (invoke resolve-operation 'p18-cli-help-text))
          "--version" (print-artifact! resolve-operation
                                       'p18-cli-version-record [])
          "version" (print-artifact! resolve-operation
                                     'p18-cli-version-record [])
          "--assert-seedless-release"
          (invoke resolve-operation 'p18-seedless-overclaim!)
          "assert-seedless-release"
          (invoke resolve-operation 'p18-seedless-overclaim!)
          "test" (run-test-command! resolve-operation arguments)
          "self-host"
          (invoke resolve-operation
                  'p18-t04-public-self-host-verify-command! arguments)
          "compile" (compile-command/run! resolve-operation arguments)
          "check"
          (let [artifact (invoke resolve-operation 'check-file-artifact path)]
            (println "gravity stage0 check passed:"
                     (invoke resolve-operation
                             'check-artifact-module-name artifact)))
          "run" (run-command! resolve-operation arguments)
          "run-compiled"
          (print (invoke resolve-operation 'run-compiled-file path)))
        true)
      (dispatch-generic! resolve-operation command path))))
