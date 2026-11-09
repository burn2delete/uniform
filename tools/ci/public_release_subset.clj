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

(def ^:private diagnostic-schema
  "gravity/ci-public-release-diagnostic-v1")

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

(defn- sha256-bytes [^bytes bytes]
  (let [digest (doto (java.security.MessageDigest/getInstance "SHA-256")
                 (.update bytes))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn- utf8-bytes [text]
  (.getBytes (str text) java.nio.charset.StandardCharsets/UTF_8))

(defn- diagnostic-edn-bytes [value]
  (binding [*print-length* nil
            *print-level* nil
            *print-readably* true]
    (utf8-bytes (str (pr-str value) "\n"))))

(defn- path-string [^java.nio.file.Path path]
  (str path))

(defn- throwable-data [error]
  {:class (.getName (class error))
   :message (bounded-text (.getMessage ^Throwable error))})

(defn- fresh-directory! []
  (let [parent (java.io.File. public-artifact-dir)]
    (when-not (or (.isDirectory parent) (.mkdirs parent))
      (throw (ex-info "Public diagnostic artifact directory is unavailable"
                      {:path public-artifact-dir})))
    (path-string
     (java.nio.file.Files/createTempDirectory
      (.toPath parent)
      "run-"
      (make-array java.nio.file.attribute.FileAttribute 0)))))

(defn- capture-context []
  (try
    {:enabled? true
     :directory (fresh-directory!)
     :records (atom [])
     :failure (atom nil)
     :ordinal (atom 0)}
    (catch Throwable error
      (let [failure {:stage :directory
                     :error (throwable-data error)}]
        (marker! :diagnostic-capture :unavailable failure)
        {:enabled? false
         :directory nil
         :records (atom [])
         :failure (atom failure)
         :ordinal (atom 0)}))))

(defn- capture-failure! [context phase error path]
  (let [failure {:phase phase
                 :path path
                 :error (throwable-data error)}]
    (when (compare-and-set! (:failure context) nil failure)
      (marker! phase :diagnostic-capture-failed failure))
    nil))

(defn- capture-skipped! [context phase]
  (marker! phase :diagnostic-capture-skipped
           {:reason :prior-capture-failure
            :failure @(:failure context)}))

(defn- write-bytes! [^java.nio.file.Path path ^bytes bytes]
  (java.nio.file.Files/write
   path
   bytes
   (into-array java.nio.file.OpenOption
               [java.nio.file.StandardOpenOption/WRITE
                java.nio.file.StandardOpenOption/TRUNCATE_EXISTING])))

(defn- write-manifest! [context prefix status]
  (let [path* (atom nil)]
    (try
      (let [path (java.nio.file.Files/createTempFile
                  (java.nio.file.Paths/get (:directory context)
                                           (make-array String 0))
                  prefix
                  ".edn"
                  (make-array java.nio.file.attribute.FileAttribute 0))]
        (reset! path* path)
        (let [envelope {:schema diagnostic-schema
                        :authority :diagnostic
                        :authoritative? false
                        :status status
                        :directory (:directory context)
                        :records @(:records context)}
              bytes (diagnostic-edn-bytes envelope)]
          (write-bytes! path bytes)
          (let [actual (java.nio.file.Files/readAllBytes path)]
            (marker! :diagnostic-capture :manifest-written
                     {:path (path-string path)
                      :status status
                      :bytes (alength actual)
                      :sha256 (sha256-bytes actual)}))))
      (catch Throwable error
        (capture-failure! context :diagnostic-capture error
                          (some-> @path* path-string))))))

(defn- checkpoint-capture! [context]
  (when (and (:enabled? context)
             (nil? @(:failure context)))
    (write-manifest! context
                     "partial-manifest-"
                     :partial)))

(defn- capture-record! [context phase result]
  (if-not (:enabled? context)
    (capture-skipped! context phase)
    (if @(:failure context)
      (capture-skipped! context phase)
      (let [ordinal (swap! (:ordinal context) inc)
            prefix (str (name phase) "-" (format "%04d-" ordinal))
            path* (atom nil)]
        (try
          (let [path (java.nio.file.Files/createTempFile
                      (java.nio.file.Paths/get (:directory context)
                                               (make-array String 0))
                      prefix
                      ".edn"
                      (make-array java.nio.file.attribute.FileAttribute 0))]
            (reset! path* path)
            (let [envelope {:schema diagnostic-schema
                            :authority :diagnostic
                            :authoritative? false
                            :phase phase
                            :ordinal ordinal
                            :record result}
                  bytes (diagnostic-edn-bytes envelope)]
              (write-bytes! path bytes)
              (let [actual (java.nio.file.Files/readAllBytes path)
                    metadata {:path (path-string path)
                              :phase phase
                              :ordinal ordinal
                              :bytes (alength actual)
                              :sha256 (sha256-bytes actual)}]
                (swap! (:records context) conj metadata)
                (marker! phase :diagnostic-captured metadata)
                (checkpoint-capture! context))))
          (catch Throwable error
            (capture-failure! context phase error (some-> @path* path-string)))))))
  result)

(defn- finalize-capture! [context]
  (when (and (:enabled? context)
             (nil? @(:failure context)))
    (write-manifest! context "manifest-" :final)))

(defn- traced
  ([phase original data-fn]
   (traced phase original data-fn nil))
  ([phase original data-fn on-return]
   (fn [& args]
     (marker-call! phase :enter #(data-fn args nil))
     (try
       (let [result (apply original args)]
         (when on-return
           (try
             (on-return args result)
             (catch Throwable error
               ;; Capture diagnostics cannot replace the wrapped result.
               (marker! phase :diagnostic-capture-failed
                        (throwable-data error)))))
         (marker-call! phase :return #(data-fn args result))
         result)
       (catch Throwable error
         (marker-call! phase :throw #(throw-data error))
         (throw error))))))

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

(defn- runtime-bindings [runtime capture]
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
     (:accepted-record runtime)
     (traced :accepted-fixture accepted fixture-data
             (fn [_args result]
               (capture-record! capture :accepted-fixture result)))
     (:rejected-record runtime)
     (traced :rejected-fixture rejected fixture-data
             (fn [_args result]
               (capture-record! capture :rejected-fixture result)))}))

(defn- run-with-runtime! [runtime]
  (let [runner (var-value (:runner runtime))
        capture (capture-context)]
    (try
      (with-redefs-fn (runtime-bindings runtime capture)
        (fn [] (runner public-test-argv)))
      (finally
        (finalize-capture! capture)))))

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
