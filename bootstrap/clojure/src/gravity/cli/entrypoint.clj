(ns gravity.cli.entrypoint
  "Process boundary for the bootstrap-hosted CLI."
  (:refer-clojure :exclude [run!])
  (:require [clojure.string :as str]
            [gravity.build.invocation :as build-invocation]
            [gravity.cli :as cli]
            [gravity.cli.dispatch :as dispatch]))

(def usage-text (cli/cli-usage-text))

(defn- print-build-failure! [exception exit!]
  ;; Ordinary host load/build exceptions are CLI failures, not private output.
  ;; Preserve control flow, even when a loader wrapped the original cause.
  (when (.isInterrupted (Thread/currentThread)) (throw exception))
  (let [seen (java.util.IdentityHashMap.)]
    (loop [cause exception propagate? false interrupted? false]
      (if (and cause (not (.containsKey seen cause)))
        (do
          (.put seen cause true)
          (recur (.getCause ^Throwable cause)
                 (or propagate? (instance? Error cause)
                     (instance? java.util.concurrent.CancellationException cause)
                     (and (instance? java.io.InterruptedIOException cause)
                          (not (instance? java.net.SocketTimeoutException cause)))
                     (instance? java.nio.channels.ClosedByInterruptException cause))
                 (or interrupted? (instance? InterruptedException cause))))
        (do
          (when interrupted? (.interrupt (Thread/currentThread)))
          (when (or propagate? interrupted?) (throw exception))))))
  (let [{:keys [json exit-code]} (build-invocation/diagnostic-json exception)]
    (binding [*out* *err*] (println json))
    (exit! exit-code)))

(defn run-build!
  "Run the standalone declared-input build path at the CLI boundary.

  The build invocation validates its command, policy and input data before it
  resolves the bootstrap Reader constructor. Keep the existing redacted error
  projection and exit-code behavior when this narrow route is selected."
  [arguments exit!]
  (try
    (println (:json
              (build-invocation/success-json
               (build-invocation/run-declared-input-build! (vec arguments)))))
    (shutdown-agents)
    (catch clojure.lang.ExceptionInfo exception
      (print-build-failure! exception exit!))
    (catch Exception exception
      (print-build-failure! exception exit!))))

(defn- source-command-usage-error
  [arguments]
  (when (contains? #{"check" "run-compiled"} (first arguments))
    (let [[command path & remaining] arguments]
      (when-not (and (string? path) (not (str/blank? path))
                     (not (str/starts-with? path "-")) (empty? remaining))
        (let [message (str "usage: gravity " command " <file.qst|file.gravity>")]
          (ex-info message
                   {:id "T1006" :stage :command-line :severity :error
                    :message message :source-span nil :profile nil :target nil
                    :remediation "Provide exactly one nonblank source path and no options."}))))))

(defn run!
  [arguments {:keys [resolve-operation print-diagnostic! exit!]}]
  ;; Only this local shape decision selects usage exit 2. Service diagnostic
  ;; IDs, including T1006, retain the existing downstream failure handling.
  (if-let [usage-error (source-command-usage-error arguments)]
    (do (print-diagnostic! usage-error)
        (exit! 2))
    (try
      (when-not (dispatch/dispatch! resolve-operation arguments)
        (binding [*out* *err*]
          (println usage-text))
        (exit! 2))
      (shutdown-agents)
      (catch clojure.lang.ExceptionInfo exception
        (if (= "build" (first arguments))
          (print-build-failure! exception exit!)
          (do (print-diagnostic! exception)
              (exit! 1))))
      (catch Exception exception
        (if (= "build" (first arguments))
          (print-build-failure! exception exit!)
          (throw exception))))))
