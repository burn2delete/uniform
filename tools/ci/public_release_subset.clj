(ns ci.public-release-subset
  "Small CI adapter for the existing P18 public release subset test.

  The adapter owns only observation and output isolation. The development
  runner and bootstrap implementation remain the semantic authorities."
  (:refer-clojure :exclude [run!]))

(def public-test-argv
  ["--progress"
   "--namespace" "gravity.bootstrap-test"
   "--exact" "p18-public-test-command-runs-current-public-subset"])

(def ^:private public-artifact-dir
  "target/ci/p18-public-test-artifacts")

(def runtime-symbols
  {:runner 'gravity.development-test-runner/run-cli!
   :artifact-dir 'gravity.bootstrap/p18-t04-artifact-dir
   :t02-package 'gravity.bootstrap/p18-t02-write-packaged-jvm-cli-artifacts!
   :t04-sweep 'gravity.bootstrap/p18-t04-write-public-test-command-artifacts!
   :accepted-record 'gravity.bootstrap/p18-t04-public-test-accepted-record
   :rejected-record 'gravity.bootstrap/p18-t04-public-test-rejected-record
   :shell-run 'gravity.bootstrap/p18-shell-run})

(defn exit!
  "Exit only after the temporary runtime redefinitions have unwound."
  [code]
  (System/exit (int code)))

(defn- bounded-text [value]
  (let [text (str value)]
    (if (> (count text) 240)
      (str (subs text 0 237) "...")
      text)))

(defn- bounded-argv [argv]
  (when (sequential? argv)
    (mapv bounded-text (take 32 argv))))

(defn- marker! [phase edge data]
  (try
    (binding [*out* *err*
              *print-length* 64
              *print-level* 6]
      (println (pr-str (merge {:marker "ci-public-release-subset"
                               :phase phase
                               :edge edge
                               :monotonic-ns (System/nanoTime)}
                              data)))
      (flush))
    (catch Throwable _
      ;; Diagnostics cannot replace or alter the wrapped semantic call.
      nil)))

(defn- marker-call! [phase edge data-fn]
  (try
    (marker! phase edge (data-fn))
    (catch Throwable _
      nil)))

(defn- throw-data [error]
  {:class (.getName (class error))
   :message (bounded-text (.getMessage ^Throwable error))})

(defn- fixture-data [args _result]
  (let [fixture (first args)]
    (when (map? fixture)
      (cond-> {:fixture (bounded-text (:fixture fixture))}
        (contains? fixture :command)
        (assoc :command (bounded-text (:command fixture)))))))

(defn- shell-args [args]
  (if (= 1 (count args))
    (first args)
    (second args)))

(defn- shell-return-data [result]
  (when (map? result)
    (select-keys result [:exit])))

(defn- traced
  [phase original data-fn]
  (fn [& args]
    (marker-call! phase :enter #(data-fn args nil))
    (try
      (let [result (apply original args)]
        (marker-call! phase :return #(data-fn args result))
        result)
      (catch Throwable error
        (marker-call! phase :throw #(throw-data error))
        (throw error)))))

(defn resolve-runtime-vars
  "Resolve the real runner/bootstrap Vars at execution time, not load time."
  []
  (into {}
        (map (fn [[key symbol]]
               [key (or (requiring-resolve symbol)
                        (throw (ex-info "Public subset runtime Var is unavailable"
                                        {:symbol symbol :key key})))]))
        runtime-symbols))

(defn- var-value [value]
  (if (var? value) @value value))

(defn- runtime-bindings [runtime]
  (let [shell (var-value (:shell-run runtime))
        t02 (var-value (:t02-package runtime))
        t04 (var-value (:t04-sweep runtime))
        accepted (var-value (:accepted-record runtime))
        rejected (var-value (:rejected-record runtime))]
    {(:artifact-dir runtime) public-artifact-dir
     (:shell-run runtime) (traced :p18-shell-run shell
                                  (fn [args result]
                                    (if result
                                      (merge {:argv (bounded-argv (shell-args args))}
                                             (shell-return-data result))
                                      {:argv (bounded-argv (shell-args args))})))
     (:t02-package runtime) (traced :t02-package t02 (constantly {}))
     (:t04-sweep runtime) (traced :t04-sweep t04 (constantly {}))
     (:accepted-record runtime) (traced :accepted-fixture accepted fixture-data)
     (:rejected-record runtime) (traced :rejected-fixture rejected fixture-data)}))

(defn- run-with-runtime! [runtime]
  (let [runner (var-value (:runner runtime))]
    (with-redefs-fn (runtime-bindings runtime)
      (fn [] (runner public-test-argv)))))

(defn run!
  "Run the fixed public subset through the real development test runner."
  ([] (run! {}))
  ([options]
   (when-not (or (nil? options) (and (map? options) (empty? options)))
     (throw (ex-info "Public subset runner accepts no execution options"
                     {:options options})))
   (marker! :runtime-resolution :enter {})
   (let [runtime (try
                   (resolve-runtime-vars)
                   (catch Throwable error
                     (marker! :runtime-resolution :throw (throw-data error))
                     (throw error)))]
     (marker! :runtime-resolution :return {})
     (let [exit-code (run-with-runtime! runtime)]
       (when-not (integer? exit-code)
         (throw (ex-info "Public subset runner returned a non-integer exit code"
                         {:exit-code exit-code})))
       (exit! exit-code)
       exit-code))))
