(ns gravity.bootstrap.rebuild.macro-plan
  "Persist and reload one current-development Gravity macro plan.

  This is a narrow bootstrap component boundary.  It invokes the existing
  current-development macro-plan builder in one child and reloads only its
  EDN data plan in a fresh child.  It does not define macro semantics or claim
  a rebuilt compiler."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap.rebuild.inputs]
            [gravity.digest :as digest])
  (:import [java.io File]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption]))

(def schema :gravity/bootstrap-macro-plan-v1)
(def component-id :gravity/sh05-macro-expansion-plan)

(def ^:private macro-source-path
  "language/compiler/src/gravity/macro.gravity")
(def ^:private macro-source-byte-count 54374)
(def ^:private macro-source-sha256
  "sha256:0a6e58a8134c6f9dbd139617fda376cd8978223820562c1f5b214b658f1d8db1")
(def ^:private macro-function 'sh05-expand-macro-template)
(def ^:private macro-function-shape
  {:arity 1 :params ['request]})
(def ^:private binding-identity-keys
  [:source-path :source-byte-count :source-content-hash :source-mode
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])

(def ^:private accepted-fixture
  {:path "bootstrap/clojure/fixtures/self-hosting/sh-05/accepted/defn-basic.gravity"
   :sha256 "sha256:b840a6da3225c88aca70cb9e3234d509a2729d312d876192856aafe5622e2ed2"
   :expected-expanded-forms '[(def add-one (fn [x] (+ x 1)))]})
(def ^:private rejected-fixture
  {:path "bootstrap/clojure/fixtures/self-hosting/sh-05/rejected/return-non-syntax.gravity"
   :sha256 "sha256:b5e68ccf6f7acc74f6b5054d38896fb6a24c81a07ea0b4eaf139caa43409b9a8"
   :diagnostic "C4-RETURN"})

(def ^:private default-component
  {:component-id component-id
   :schema schema
   :source {:path macro-source-path
            :byte-count macro-source-byte-count
            :sha256 macro-source-sha256}
   :function macro-function
   :function-shape macro-function-shape
   :fixtures [accepted-fixture rejected-fixture]
   :source-mode :development-current
   :compiler-profile :meta
   :target :jvm
   :runtime :jvm
   :host-language :clojure
   :output-plan-path "target/bootstrap-macro-plan/macro-plan.edn"
   :output-record-path "target/bootstrap-macro-plan/macro-record.edn"
   :comparison-modes [:plan-identity :artifact-identity :diagnostic-equivalence]
   :conformance {:accepted :macro-expansion-artifact
                 :rejected :structured-c4-diagnostic
                 :native-target :unrun}
   :nonclaims [:whole-compiler-rebuild
               :self-hosting
               :seed-retirement
               :migration-authority
               :release-readiness]})

(defn development-macro-plan
  "Return the fixed current-development component declaration."
  []
  default-component)

(defn- fail!
  [reason message data]
  (throw (ex-info message
                  (merge {:diagnostic "BOOT-MACRO-PLAN"
                          :component-id component-id
                          :reason reason}
                         data))))

(defn- private-input
  [name]
  (or (some-> (ns-resolve 'gravity.bootstrap.rebuild.inputs name) deref)
      (throw (IllegalStateException.
              (str "bootstrap process helper is unavailable: " name)))))

(defn- command-result
  [working-root command]
  ((private-input 'command-result) working-root command))

(defn- command-output!
  [working-root command reason]
  (let [result (command-result working-root command)]
    (when-not (zero? (:exit-code result))
      (fail! reason
             "bootstrap macro child command failed"
             (select-keys result [:command :exit-code :stdout-sha256 :stderr-sha256])))
    result))

(defn- bytes-text
  [bytes]
  (String. ^bytes bytes StandardCharsets/UTF_8))

(defn- sha256-id
  [^bytes bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- file-sha256
  [^File file]
  (sha256-id (Files/readAllBytes (.toPath file))))

(defn- canonical-file
  [root relative]
  (.getCanonicalFile (io/file (str root) relative)))

(defn- regular-file!
  [root relative reason]
  (let [file (canonical-file root relative)]
    (when-not (Files/isRegularFile (.toPath file) (make-array LinkOption 0))
      (fail! reason "bootstrap macro input is not a regular file"
             {:path relative}))
    file))

(defn- read-single-edn
  [^bytes bytes label]
  (try
    (with-open [reader (java.io.PushbackReader.
                        (java.io.StringReader. (bytes-text bytes)))]
      (let [eof (Object.)
            value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (when (or (identical? eof value)
                  (not (identical? eof trailing)))
          (fail! :edn-shape (str label " is not one EDN value") {}))
        value))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Exception error
      (fail! :edn-parse (str label " is not valid EDN")
             {:cause (.getMessage error)}))))

(defn- read-edn-file!
  [^File file label]
  (read-single-edn (Files/readAllBytes (.toPath file)) label))

(defn- sha256-string?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- sha256-id?
  [value]
  (sha256-string? value))

(defn- forbidden-plan-value?
  [value]
  (or (fn? value)
      (instance? clojure.lang.Var value)
      (and (coll? value)
           (some forbidden-plan-value? value))))

(defn- expected-plan-shape?
  [plan]
  (let [definition (get-in plan [:functions macro-function])]
    (and (map? plan)
         (= :gravity/stage2-compiler-artifact-plan (:kind plan))
         (true? (:compiler-artifact-plan? plan))
         (= 'gravity.macro (get-in plan [:module :module]))
         (= :meta (get-in plan [:module :profile]))
         (= :jvm (get-in plan [:module :target]))
         (= #{} (get-in plan [:module :effects]))
         (= #{} (get-in plan [:module :capabilities]))
         (= :safe (get-in plan [:module :safety]))
         (= macro-function-shape (select-keys definition [:arity :params]))
         (sha256-id? (:plan-id plan))
         (map? (:functions plan))
         (seq (:functions plan)))))

(defn validate-persisted-plan!
  "Validate the data-only payload before a fresh execution child is started.

  The checks bind the persisted plan to the current source and its existing
  compiler identities.  No C3 artifact, Reader authority or operation value is
  accepted in this payload."
  [payload]
  (let [source (:source payload)
        plan (:plan payload)
        identities (:identities payload)
        expected-identities (select-keys identities binding-identity-keys)]
    (when-not (= schema (:schema payload))
      (fail! :plan-schema "persisted macro plan has an unexpected schema"
             {:observed (:schema payload)}))
    (when-not (= component-id (:component-id payload))
      (fail! :component-id "persisted macro plan has an unexpected component"
             {:observed (:component-id payload)}))
    (when-not (= {:path macro-source-path
                  :byte-count macro-source-byte-count
                  :sha256 macro-source-sha256}
                 source)
      (fail! :source-declaration "persisted macro plan source declaration drifted"
             {:observed source}))
    (when-not (and (map? identities)
                   (= macro-source-sha256 (:source-content-hash identities))
                   (= :development-current (:source-mode identities))
                   (= macro-source-byte-count (:source-byte-count identities))
                   (every? #(contains? identities %) binding-identity-keys)
                   (sha256-id? (:plan-semantic-hash identities))
                   (sha256-id? (:functions-semantic-hash identities))
                   (integer? (:function-count identities))
                   (sha256-id? (:function-names-hash identities))
                   (sha256-id? (:function-shapes-hash identities))
                   (= macro-function-shape
                      (get-in identities [:public-function-shapes macro-function])))
      (fail! :source-plan-binding
             "persisted macro plan identities are not bound to the current source"
             {:identities expected-identities}))
    (when-not (and (expected-plan-shape? plan)
                   (= macro-source-sha256 (get-in plan [:source :sha256]))
                   (= macro-source-byte-count (get-in plan [:source :byte-count]))
                   (not (forbidden-plan-value? plan)))
      (fail! :plan-shape
             "persisted macro plan is not the expected data-only compiler plan"
             {:plan-shape (select-keys plan [:kind :module :source :plan-id])}))
    (when-not (= plan (edn/read-string (pr-str plan)))
      (fail! :plan-edn-roundtrip
             "persisted macro plan is not an EDN data value" {}))
    payload))

(defn- source-identity!
  [project-root]
  (let [file (regular-file! project-root macro-source-path :source-missing)
        bytes (Files/readAllBytes (.toPath file))
        source {:path macro-source-path
                :byte-count (alength ^bytes bytes)
                :sha256 (sha256-id bytes)}]
    (when-not (= {:path macro-source-path
                  :byte-count macro-source-byte-count
                  :sha256 macro-source-sha256}
                 source)
      (fail! :source-identity "current macro source does not match declaration"
             {:expected {:path macro-source-path
                         :byte-count macro-source-byte-count
                         :sha256 macro-source-sha256}
              :actual source}))
    source))

(defn- compiler-revision!
  [project-root]
  {:commit (str/trim
            (bytes-text (:stdout-bytes
                         (command-output! project-root
                                          ["git" "rev-parse" "HEAD"]
                                          :compiler-revision))))
   :tree (str/trim
          (bytes-text (:stdout-bytes
                       (command-output! project-root
                                        ["git" "rev-parse" "HEAD^{tree}"]
                                        :compiler-revision))))})

(defn- classpath-entry
  [root raw]
  (.getCanonicalFile
   (let [file (io/file raw)]
     (if (.isAbsolute file) file (io/file (str root) raw)))))

(defn- classpath-record
  [root raw]
  (let [file (classpath-entry root raw)]
    (cond
      (.isDirectory file)
      {:kind :directory :path (.getPath file)}

      (.isFile file)
      {:kind :jar :file (.getName file) :sha256 (file-sha256 file)}

      :else
      (fail! :toolchain "child Clojure classpath entry is missing"
             {:entry raw :resolved (.getPath file)}))))

(defn- toolchain-record!
  [project-root]
  (let [describe (command-output! project-root
                                  ["clojure" "-Srepro" "-Sdescribe"]
                                  :toolchain)
        classpath (command-output! project-root
                                   ["clojure" "-Srepro" "-Spath"]
                                   :toolchain)
        describe-data (read-single-edn (:stdout-bytes describe)
                                       "child Clojure -Sdescribe")
        entries (->> (str/split (str/trim (bytes-text (:stdout-bytes classpath)))
                                (re-pattern (java.util.regex.Pattern/quote
                                             File/pathSeparator)))
                     (remove str/blank?)
                     (mapv #(classpath-record project-root %)))
        jars (filterv #(= :jar (:kind %)) entries)
        seed (some #(when (re-matches #"clojure-[^/]+\.jar" (:file %)) %) jars)]
    (when-not seed
      (fail! :toolchain "child Clojure classpath has no Clojure seed jar"
             {:entries entries}))
    {:command ["clojure" "-Srepro"]
     :version (:version describe-data)
     :runtime :jvm
     :seed-artifact-id (:sha256 seed)
     :classpath-entries entries
     :sdescribe-sha256 (:stdout-sha256 describe)
     :classpath-sha256 (:stdout-sha256 classpath)
     :network :not-enforced}))

(defn- literal
  [value]
  (pr-str value))

(defn- producer-expression
  [plan-path]
  (str
   "(do "
   "(require '[clojure.java.io :as io] '[gravity.bootstrap :as b]) "
   "(let [source-path " (literal macro-source-path)
   " binding (b/sh05-macro-build-binding! source-path :development-current)"
   " plan (:plan binding)"
   " payload {:schema " (literal schema)
   " :component-id " (literal component-id)
   " :source {:path source-path :byte-count " macro-source-byte-count
   " :sha256 " (literal macro-source-sha256) "}"
   " :identities (select-keys binding " (literal binding-identity-keys) ")"
   " :plan plan}"
   " text (pr-str payload)]"
   " (io/make-parents " (literal plan-path) ")"
   " (spit " (literal plan-path) " text)"
   " (println (pr-str {:status :complete"
   " :plan-id (:plan-id plan)"
   " :source-content-hash (:source-content-hash binding)"
   " :plan-sha256 (str \"sha256:\" (b/sha256-hex text))}))))"))

(defn- execution-expression
  [plan-path record-path]
  (str
   "(do "
   "(require '[clojure.edn :as edn] '[clojure.java.io :as io] "
   "'[gravity.bootstrap :as b] '[gravity.macro :as m]) "
   "(let [payload (edn/read-string (slurp " (literal plan-path) "))"
   " plan (:plan payload) identities (:identities payload)"
   " _ (when-not (and (= " (literal schema) " (:schema payload))"
   " (= " (literal component-id) " (:component-id payload))"
   " (= " (literal macro-source-sha256) " (get-in plan [:source :sha256]))"
   " (= " macro-source-byte-count " (get-in plan [:source :byte-count]))"
   " (= {:arity 1 :params ['request]}"
   " (select-keys (get-in plan [:functions 'sh05-expand-macro-template]"
   ") [:arity :params]))"
   " (= " (literal macro-source-sha256) " (:source-content-hash identities))"
   " (= :development-current (:source-mode identities)))"
   " (throw (ex-info \"persisted macro plan failed child ABI checks\""
   " {:reason :plan-shape})))"
   " binding (merge identities {:plan plan"
   " :revision-status :development-unadmitted"
   " :source-authority :development-current"
   " :source-mode :development-current})"
   " reader (.getCanonicalPath (io/file b/sh03-reader-source-relative-path))"
   " syntax-source (.getCanonicalPath (io/file \"bootstrap/gravity/src/gravity/bootstrap/syntax.gravity\"))"
   " invocation {:reader-source-path reader"
   " :authenticated-envelope-options {:source :development-current}"
   " :syntax-source-selection {:schema :gravity/sh04-development-syntax-selection-v1"
   " :source-path syntax-source}}"
   " operations ((ns-resolve 'gravity.bootstrap 'macro-stage-operations))"
   " run-fixture (fn [fixture]"
   " (let [source (.getCanonicalPath (io/file (:path fixture)))"
   " c3 (b/compiler-c3-syntax-source-artifact source (slurp source)"
   " (b/reader-project-context-for-source source) nil nil reader invocation)"
   " forms (get-in c3 [:c2-reader-artifact :parsed-semantic-values])"
   " module (b/parse-module source forms)"
   " syntax (second (b/sh05-source-syntax-stream c3))"
   " form (second forms)"
   " base-request (b/sh05-default-macro-request source c3 module syntax form"
   " invocation binding)"
   " compiler-metadata (get-in module [:metadata :compiler])"
   " request-overrides (if (and (map? compiler-metadata)"
   " (contains? compiler-metadata :sh05-request))"
   " (:sh05-request compiler-metadata) {})"
   " request (b/sh05-deep-merge base-request request-overrides)"
   " run (m/expand source binding request operations)"
   " resolved (:resolved-expansion run)]"
   " {:status :accepted :fixture (select-keys fixture [:path :sha256])"
   " :run-artifact (get-in run [:raw-template-result :artifact])"
   " :run-status (get-in run [:raw-template-result :status])"
   " :resolved-artifact (:artifact resolved)"
   " :resolved-status (:status resolved)"
   " :template-status (get-in run [:template-verification :status])"
   " :resolved-verification-status (get-in run [:resolved-verification :status])"
   " :macro (:macro resolved) :phase (:phase resolved)"
   " :expanded-syntax-recipe (:expanded-syntax-recipe resolved)"
   " :output-syntax-id (:output-syntax-id resolved)"
   " :macro-version (:macro-version resolved)}))"
   " accepted (run-fixture " (literal (select-keys accepted-fixture [:path :sha256])) ")"
   " rejected (try (run-fixture " (literal (select-keys rejected-fixture [:path :sha256])) ")"
   " {:status :unexpected-acceptance}"
   " (catch clojure.lang.ExceptionInfo error"
   " (let [data (ex-data error)]"
   " {:status :rejected :fixture " (literal (select-keys rejected-fixture [:path :sha256]))
   " :diagnostic (:id data)"
   " :source-span (:source-span data) :facts (:facts data)})))"
   " record {:schema " (literal schema)
   " :component-id " (literal component-id)
   " :execution-child {:loaded-plan? true :fresh-c3-request? true"
   " :fresh-operations? true :source-mode :development-current}"
   " :accepted accepted :rejected rejected"
   " :boundary {:host-language :clojure :seed-hosted? true"
   " :self-hosted? false :native-conformance :unrun"
   " :network :not-enforced} }]"
   " (io/make-parents " (literal record-path) ")"
   " (spit " (literal record-path) " (pr-str record))"
   " (println (pr-str {:status :complete :record-path "
   (literal record-path) " :accepted (:status accepted)"
   " :rejected (:diagnostic rejected)}))))"))

(defn- run-child!
  [project-root expression label]
  (let [result (command-result project-root
                               ["clojure" "-Srepro" "-M" "-e" expression])]
    (when-not (zero? (:exit-code result))
      (fail! :child-process
             (str "bootstrap macro " label " child failed")
             {:label label
              :command (:command result)
              :exit-code (:exit-code result)
              :stdout-sha256 (:stdout-sha256 result)
              :stderr-sha256 (:stderr-sha256 result)
              :stdout (bytes-text (:stdout-bytes result))
              :stderr (bytes-text (:stderr-bytes result))}))
    (assoc result :summary
           (read-single-edn (:stdout-bytes result)
                            (str "macro " label " child summary")))))

(defn- validate-fixture-record!
  [record]
  (let [accepted (:accepted record)
        rejected (:rejected record)]
    (when-not (and (= :accepted (:status accepted))
                   (= (select-keys accepted-fixture [:path :sha256])
                      (:fixture accepted))
                   (= :gravity/sh05-macro-template-result (:run-artifact accepted))
                   (= :accepted (:run-status accepted))
                   (= :gravity/sh05-macro-expansion-artifact (:resolved-artifact accepted))
                   (= 'gravity.core/defn (:macro accepted))
                   (= :macro-expanded (:phase accepted))
                   (= :accepted (:resolved-status accepted))
                   (= :passed (:template-status accepted))
                   (= :passed (:resolved-verification-status accepted))
                   (map? (:expanded-syntax-recipe accepted))
                   (seq (get-in accepted [:expanded-syntax-recipe :children]))
                   (sha256-id? (:output-syntax-id accepted))
                   (= :rejected (:status rejected))
                   (= (select-keys rejected-fixture [:path :sha256])
                      (:fixture rejected))
                   (= (:diagnostic rejected-fixture) (:diagnostic rejected)))
      (fail! :conformance "macro execution child produced unexpected fixture outcomes"
             {:accepted (select-keys accepted
                                     [:status :run-artifact :run-status :resolved-artifact
                                      :macro :phase :template-status :resolved-status
                                      :resolved-verification-status :output-syntax-id])
              :rejected (select-keys rejected [:status :diagnostic])}))))

(defn run-development-macro-plan!
  "Build, persist, reload, and execute the current macro plan.

  The producer child uses the existing current-development builder.  The
  execution child reads only the persisted EDN plan and identities, creates
  fresh C3 requests and operation maps, then runs the loaded plan on one
  accepted and one rejected fixture."
  ([project-root]
   (run-development-macro-plan! project-root project-root))
  ([project-root output-root]
   (let [component (development-macro-plan)
         source (source-identity! project-root)
         revision (compiler-revision! project-root)
         toolchain (toolchain-record! project-root)
         output-root (.getCanonicalFile (io/file (str output-root)))
         plan-file (canonical-file output-root (:output-plan-path component))
         record-file (canonical-file output-root (:output-record-path component))]
     (doseq [^File file [plan-file record-file]]
       (when (.exists file)
         (fail! :output-exists "bootstrap macro output already exists"
                {:path (.getPath file)})))
     (run-child! project-root (producer-expression (.getPath plan-file)) :producer)
     (let [payload (validate-persisted-plan! (read-edn-file! plan-file "macro plan"))]
       (when-not (= (:sha256 source) (get-in payload [:plan :source :sha256]))
         (fail! :source-plan-binding "persisted plan source hash drifted"
                {:source source :plan-source (get-in payload [:plan :source])}))
       (run-child! project-root
                   (execution-expression (.getPath plan-file) (.getPath record-file))
                   :execution)
       (let [record (read-edn-file! record-file "macro execution record")]
         (validate-fixture-record! record)
         {:component component
          :source source
          :compiler-revision revision
          :toolchain toolchain
          :plan {:plan-id (get-in payload [:plan :plan-id])
                 :identities (:identities payload)}
          :record record
          :output-sha256 {:plan (file-sha256 plan-file)
                          :record (file-sha256 record-file)}
          :plan-path (.getPath plan-file)
          :record-path (.getPath record-file)})))))
