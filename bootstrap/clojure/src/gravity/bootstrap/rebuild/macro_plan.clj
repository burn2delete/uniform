(ns gravity.bootstrap.rebuild.macro-plan
  "Persist and reload one current-development Gravity macro plan.

  This is a narrow bootstrap component boundary.  It invokes the existing
  current-development macro-plan builder in one child and reloads only its
  EDN data plan in a fresh child.  It does not define macro semantics or claim
  a rebuilt compiler."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap]
            [gravity.bootstrap.rebuild.inputs]
            [gravity.digest :as digest]
            [gravity.macro :as macro])
  (:import [java.io File]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute BasicFileAttributes]))

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
(def ^:private plan-identity-keys
  [:plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])
(def ^:private recomputed-plan-identity-keys
  [:plan-semantic-hash :functions-semantic-hash :function-count
   :function-shapes-hash :public-function-hashes :public-function-shapes])

(def ^:private accepted-fixture
  {:path "bootstrap/clojure/fixtures/self-hosting/sh-05/accepted/defn-basic.gravity"
   :sha256 "sha256:b840a6da3225c88aca70cb9e3234d509a2729d312d876192856aafe5622e2ed2"
   :byte-count 290
   :expected-expanded-forms '[(def add-one (fn [x] (+ x 1)))]})
(def ^:private rejected-diagnostic
  {:artifact :gravity/sh05-macro-diagnostic
   :rule "C4-RETURN"
   :severity :error
   :lifecycle :active
   :stage :macro-expansion
   :generated-span nil
   :profile :meta
   :target :jvm
   :build-effects []
   :capabilities []
   :hygiene-context {:marks [] :lexical-scopes []}
   :remediation "Return a valid syntax object from the macro."
   :diagnostic-id-request {:digest-ref 0}
   :macro-name 'defn
   :facts {:reason :macro-return-not-syntax
           :caller-profile :meta
           :generated-profile :meta
           :generated-unsafe? false
           :catalog-mapping {:c4-rule "C4-RETURN" :owner-rules []}}})
(def ^:private rejected-fixture
  {:path "bootstrap/clojure/fixtures/self-hosting/sh-05/rejected/return-non-syntax.gravity"
   :sha256 "sha256:b5e68ccf6f7acc74f6b5054d38896fb6a24c81a07ea0b4eaf139caa43409b9a8"
   :byte-count 565
   :diagnostic (:rule rejected-diagnostic)})

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
   :comparison-modes [:plan-identity :macro-template-result :diagnostic-equivalence]
   :conformance {:accepted :macro-template-result
                 :rejected :structured-c4-diagnostic
                 :native-target :unrun}
   :failure-output-policy :retain-published-outputs
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

(defn- macro-operations
  []
  (if-let [operations (ns-resolve 'gravity.bootstrap 'macro-stage-operations)]
    (@operations)
    (fail! :bootstrap "existing macro stage operations are unavailable" {})))

(defn- recomputed-plan-identities
  [plan]
  (try
    (select-keys (macro/compute-plan-identities plan (macro-operations))
                 plan-identity-keys)
    (catch Exception error
      (fail! :plan-identities
             "existing macro identity computation could not consume the persisted plan"
             {:cause-class (some-> error class .getName)
              :cause-message (.getMessage error)}))))

(defn- canonical-plan-id
  [plan]
  (let [canonicalize (:c-backend-canonical-value (macro-operations))
        plan-base (dissoc plan :plan-id)]
    (str "sha256:" (digest/sha256-hex (pr-str (canonicalize plan-base))))))

(defn- bytes-text
  [^bytes bytes]
  (String. bytes StandardCharsets/UTF_8))

(defn- utf8-snapshot
  [^bytes bytes label]
  (let [text (bytes-text bytes)]
    (when-not (java.util.Arrays/equals
               bytes (.getBytes ^String text StandardCharsets/UTF_8))
      (fail! :utf8-snapshot
             "authenticated fixture bytes are not one lossless UTF-8 snapshot"
             {:label label}))
    text))

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
                        (java.io.StringReader. (utf8-snapshot bytes label)))]
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
                   (not (forbidden-plan-value? identities))
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
    (let [recomputed (recomputed-plan-identities plan)]
      ;; The existing builder hashes the insertion order of a function-name
      ;; map.  EDN map reload does not preserve that incidental order; the
      ;; semantic plan/function hashes and shape/public identities remain
      ;; stable and bind the persisted body.
      (when-not (= (select-keys identities recomputed-plan-identity-keys)
                   (select-keys recomputed recomputed-plan-identity-keys))
        (fail! :plan-identities
               "persisted macro plan identities do not match the existing compiler identity computation"
               {:declared (select-keys identities plan-identity-keys)
                :recomputed recomputed})))
    (let [expected-plan-id (canonical-plan-id plan)]
      (when-not (= expected-plan-id (:plan-id plan))
        (fail! :plan-id
               "persisted macro plan ID is not the canonical plan identity"
               {:declared (:plan-id plan) :expected expected-plan-id})))
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
  (let [commit (str/trim
                (bytes-text (:stdout-bytes
                             (command-output! project-root
                                              ["git" "rev-parse" "HEAD"]
                                              :compiler-revision))))
        tree (str/trim
              (bytes-text (:stdout-bytes
                           (command-output! project-root
                                            ["git" "rev-parse" "HEAD^{tree}"]
                                            :compiler-revision))))
        status (command-output! project-root
                                ["git" "status" "--porcelain" "--untracked-files=no"]
                                :compiler-revision)
        status-text (bytes-text (:stdout-bytes status))]
    {:commit commit
     :tree tree
     :tracked-source-state (if (str/blank? status-text) :clean :dirty)
     :tracked-source-status status-text}))

(defn- source-compiler-inputs
  [source compiler-revision]
  {:source source :compiler-revision compiler-revision})

(defn- assert-stable-inputs!
  [before project-root phase]
  (let [after (source-compiler-inputs (source-identity! project-root)
                                      (compiler-revision! project-root))]
    (when-not (= before after)
      (fail! :provenance-drift
             "source or compiler inputs changed during macro plan run"
             {:phase phase :before before :after after}))
    after))

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
   " text (pr-str payload)"
   " plan-bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)]"
   " (when-let [parent (.getParentFile (io/file " (literal plan-path) "))]"
   " (java.nio.file.Files/createDirectories (.toPath parent)"
   " (make-array java.nio.file.attribute.FileAttribute 0)))"
   " (java.nio.file.Files/write (.toPath (io/file " (literal plan-path) "))"
   " plan-bytes (into-array java.nio.file.StandardOpenOption"
   " [java.nio.file.StandardOpenOption/CREATE_NEW"
   " java.nio.file.StandardOpenOption/WRITE]))"
   " (println (pr-str {:status :complete"
   " :plan-id (:plan-id plan)"
   " :source-content-hash (:source-content-hash binding)"
   " :plan-sha256 (str \"sha256:\" (b/sha256-hex text))}))))"))

(defn- execution-expression
  [plan-path record-path expected-plan-sha256 expected-plan-id
   source compiler-revision toolchain]
  (str
   "(do "
   "(require '[clojure.edn :as edn] '[clojure.java.io :as io] "
   "'[gravity.bootstrap :as b] '[gravity.macro :as m]) "
   "(let [plan-bytes (java.nio.file.Files/readAllBytes"
   " (.toPath (io/file " (literal plan-path) ")))"
   " plan-text (String. plan-bytes java.nio.charset.StandardCharsets/UTF_8)"
   " plan-sha256 (str \"sha256:\" (b/sha256-bytes-hex plan-bytes))"
   " _ (when-not (= " (literal expected-plan-sha256) " plan-sha256)"
   " (throw (ex-info \"persisted macro plan bytes changed before execution\""
   " {:reason :plan-file-digest})))"
   " payload (edn/read-string plan-text)"
   " plan (:plan payload) identities (:identities payload)"
   " _ (when-not (and (= " (literal schema) " (:schema payload))"
   " (= " (literal component-id) " (:component-id payload))"
   " (= " (literal expected-plan-id) " (:plan-id plan))"
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
   " (let [fixture-file (.getCanonicalFile (io/file (:path fixture)))"
   " fixture-bytes (java.nio.file.Files/readAllBytes (.toPath fixture-file))"
   " _ (when-not (= (:sha256 fixture)"
   " (str \"sha256:\" (b/sha256-bytes-hex fixture-bytes)))"
   " (throw (ex-info \"macro fixture bytes do not match its declaration\""
   " {:reason :fixture-identity :fixture fixture})))"
   " fixture-text (String. fixture-bytes java.nio.charset.StandardCharsets/UTF_8)"
   " _ (when-not (java.util.Arrays/equals fixture-bytes"
   " (.getBytes fixture-text java.nio.charset.StandardCharsets/UTF_8))"
   " (throw (ex-info \"macro fixture bytes are not a lossless UTF-8 snapshot\""
   " {:reason :utf8-snapshot :fixture fixture})))"
   " source (.getCanonicalPath fixture-file)"
   " c3 (b/compiler-c3-syntax-source-artifact source fixture-text"
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
   " result (m/execute-plan! source binding 'sh05-expand-macro-template"
   " [request] operations)"
   " template (:expansion-template result)"
   " expanded-forms (when (contains? fixture :expected-expanded-forms)"
   " (mapv #(m/expanded-form % operations) [form]))]"
   " {:status (:status result) :fixture (select-keys fixture [:path :sha256 :byte-count])"
   " :source-byte-count (alength fixture-bytes)"
   " :source-content-hash (str \"sha256:\" (b/sha256-bytes-hex fixture-bytes))"
   " :utf8-snapshot? true"
   " :artifact (:artifact result) :schema-version (:schema-version result)"
   " :expansion-template template"
   " :digest-requests (:digest-requests result)"
   " :digest-request-count (count (:digest-requests result))"
   " :expansion-template? (map? template)"
   " :macro (:macro template) :macro-version (:macro-version template)"
   " :expanded-syntax-recipe (:expanded-syntax-recipe template)"
   " :expanded-forms expanded-forms"
   " :diagnostic (get-in result [:diagnostics 0 :rule])"
   " :diagnostics (:diagnostics result)}))"
   " accepted (run-fixture " (literal (select-keys accepted-fixture
                                                   [:path :sha256 :byte-count :expected-expanded-forms])) ")"
   " rejected (run-fixture " (literal (select-keys rejected-fixture
                                                    [:path :sha256 :byte-count])) ")"
   " record {:schema " (literal schema)
   " :component-id " (literal component-id)
   " :execution-child {:loaded-plan? true :fresh-c3-request? true"
   " :fresh-operations? true :source-mode :development-current"
   " :macro-version (get-in identities [:public-function-hashes 'sh05-expand-macro-template])"
   " :loaded-plan-id (:plan-id plan) :loaded-plan-sha256 plan-sha256}"
   " :accepted accepted :rejected rejected"
   " :provenance {:compiler-revision " (literal compiler-revision)
   " :source " (literal source)
   " :toolchain " (literal toolchain)
   " :live-worktree? true :input-stability :stable :authority :non-authoritative}"
   " :boundary {:host-language :clojure :seed-hosted? true"
   " :compiled-by :clojure-stage0-seed"
   " :executed-by :clojure-stage2-generic-rule-runner"
   " :self-hosted? false :native-conformance :unrun"
   " :network :not-enforced"
   " :observation :sh05-expand-macro-template-via-stage2-runtime"
   " :authority :non-authoritative} }"
   " record-text (pr-str record)"
   " record-bytes (.getBytes record-text java.nio.charset.StandardCharsets/UTF_8)]"
   " (when-let [parent (.getParentFile (io/file " (literal record-path) "))]"
   " (java.nio.file.Files/createDirectories (.toPath parent)"
   " (make-array java.nio.file.attribute.FileAttribute 0)))"
   " (java.nio.file.Files/write (.toPath (io/file " (literal record-path) "))"
   " record-bytes (into-array java.nio.file.StandardOpenOption"
   " [java.nio.file.StandardOpenOption/CREATE_NEW"
   " java.nio.file.StandardOpenOption/WRITE]))"
   " (println (pr-str {:status :complete :record-path "
   (literal record-path) " :record-sha256 (str \"sha256:\""
   " (b/sha256-bytes-hex record-bytes))"
   " :plan-id (:loaded-plan-id (:execution-child record))"
   " :plan-sha256 (:loaded-plan-sha256 (:execution-child record))"
   " :accepted (:status accepted)"
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

(defn- expected-provenance
  [source compiler-revision toolchain]
  {:source source
   :compiler-revision compiler-revision
   :toolchain toolchain
   :live-worktree? true
   :input-stability :stable
   :authority :non-authoritative})

(def ^:private c4-diagnostic-keys
  #{:artifact :rule :severity :lifecycle :stage :macro-name :macro-version
    :definition-span :call-site-span :generated-span :profile :target
    :build-effects :capabilities :hygiene-context :origin-chain :facts
    :remediation :diagnostic-id-request})

(def ^:private c4-diagnostic-facts-keys
  #{:reason :generated-profile :caller-profile :generated-unsafe?
    :trace-replay-id :recorded-trace-replay-id :catalog-mapping})

(def ^:private c4-diagnostic-preimage-keys
  #{:domain :rule :macro-name :macro-version :call-site-syntax-id
    :definition-syntax-id :reader-semantic-binding-id :source-revision-id
    :profile :target :semantic-call-span :requested-build-effects
    :requested-capabilities})

(def ^:private c4-source-span-keys
  #{:column-end :line-start :file :source :line-end :scalar-end
    :scalar-start :byte-start :byte-end :column-start})

(defn- valid-c4-span?
  [span source]
  (and (map? span)
       (= c4-source-span-keys (set (keys span)))
       (= source (:source span))
       (sha256-id? (:file span))
       (every? #(and (integer? %) (not (neg? %)))
               (vals (select-keys span [:scalar-start :scalar-end
                                        :byte-start :byte-end])))
       (every? #(and (integer? %) (pos? %))
               (vals (select-keys span [:line-start :line-end
                                        :column-start :column-end])))
       (<= (:scalar-start span) (:scalar-end span))
       (<= (:byte-start span) (:byte-end span))))

(defn- valid-c4-call-site-span?
  [span source]
  (and (map? span)
       (= #{:all :primary} (set (keys span)))
       (= 1 (count (:all span)))
       (= (:primary span) (first (:all span)))
       (valid-c4-span? (:primary span) source)))

(defn- valid-c4-origin-chain?
  [diagnostic source]
  (let [origin (first (:origin-chain diagnostic))
        producer (:producer origin)]
    (and (vector? (:origin-chain diagnostic))
         (= 1 (count (:origin-chain diagnostic)))
         (= #{:kind :span :producer :producer-version :input-syntax-ids
              :generation-reason :build-effects}
            (set (keys origin)))
         (= :source (:kind origin))
         (= (:primary (:call-site-span diagnostic)) (:span origin))
         (= #{:kind :name :identity :source-id :generated-form-id}
            (set (keys producer)))
         (= :reader (:kind producer))
         (= 'gravity.bootstrap.reader (:name producer))
         (sha256-id? (:identity producer))
         (sha256-id? (:source-id producer))
         (= (:source-id producer) (:file (:span origin)))
         (nil? (:generated-form-id producer))
         (= "SH-03" (:producer-version origin))
         (= [] (:input-syntax-ids origin))
         (= :source-read (:generation-reason origin))
         (= [] (:build-effects origin))
         (valid-c4-span? (:span origin) source))))

(defn- valid-c4-diagnostic-preimage?
  [request diagnostic macro-version]
  (let [preimage (:preimage request)]
    (and (map? request)
         (= #{:ordinal :purpose :preimage} (set (keys request)))
         (= 0 (:ordinal request))
         (= :sh05-macro-diagnostic-id (:purpose request))
         (map? preimage)
         (= c4-diagnostic-preimage-keys (set (keys preimage)))
         (= :gravity/sh05-c4-diagnostic-v1 (:domain preimage))
         (= (:rule diagnostic) (:rule preimage))
         (= (:macro-name diagnostic) (:macro-name preimage))
         (= macro-version (:macro-version preimage))
         (= (:profile diagnostic) (:profile preimage))
         (= (:target diagnostic) (:target preimage))
         (= (:build-effects diagnostic) (:requested-build-effects preimage))
         (= (:capabilities diagnostic) (:requested-capabilities preimage))
         (= {:source nil} (:semantic-call-span preimage))
         (every? sha256-id?
                 (map preimage [:call-site-syntax-id
                                :definition-syntax-id
                                :reader-semantic-binding-id
                                :source-revision-id])))))

(defn- valid-c4-diagnostic?
  [diagnostic source macro-version digest-requests]
  (let [expected (select-keys rejected-diagnostic
                               [:artifact :rule :severity :lifecycle :stage
                                :generated-span :profile :target :build-effects
                                :capabilities :hygiene-context :remediation
                                :diagnostic-id-request :macro-name])
        facts (:facts diagnostic)]
    (and (map? diagnostic)
         (= c4-diagnostic-keys (set (keys diagnostic)))
         (= expected (select-keys diagnostic (keys expected)))
         (= macro-version (:macro-version diagnostic))
         (= {:source macro-source-path
             :byte-start 0
             :byte-end macro-source-byte-count}
            (:definition-span diagnostic))
         (valid-c4-call-site-span? (:call-site-span diagnostic) source)
         (valid-c4-origin-chain? diagnostic source)
         (map? facts)
         (= c4-diagnostic-facts-keys (set (keys facts)))
         (= (select-keys (:facts rejected-diagnostic)
                         [:reason :generated-profile :caller-profile
                          :generated-unsafe? :catalog-mapping])
            (select-keys facts [:reason :generated-profile :caller-profile
                                :generated-unsafe? :catalog-mapping]))
         (sha256-id? (:trace-replay-id facts))
         (= (:trace-replay-id facts) (:recorded-trace-replay-id facts))
         (valid-c4-diagnostic-preimage? (first digest-requests)
                                        diagnostic macro-version))))

(defn- validate-fixture-record!
  [record project-root source compiler-revision toolchain macro-version]
  (let [accepted (:accepted record)
        rejected (:rejected record)
        rejected-source (.getPath
                         (canonical-file project-root (:path rejected-fixture)))]
    (when-not (and (= {:loaded-plan? true
                       :fresh-c3-request? true
                       :fresh-operations? true
                       :source-mode :development-current
                       :macro-version macro-version
                       :loaded-plan-id (:loaded-plan-id (:execution-child record))
                       :loaded-plan-sha256 (:loaded-plan-sha256 (:execution-child record))}
                      (:execution-child record))
                   (= (expected-provenance source compiler-revision toolchain)
                      (:provenance record))
                   (= {:host-language :clojure
                       :seed-hosted? true
                       :compiled-by :clojure-stage0-seed
                       :executed-by :clojure-stage2-generic-rule-runner
                       :self-hosted? false
                       :native-conformance :unrun
                       :network :not-enforced
                       :observation :sh05-expand-macro-template-via-stage2-runtime
                       :authority :non-authoritative}
                      (:boundary record))
                   (= :accepted (:status accepted))
                   (= (select-keys accepted-fixture [:path :sha256 :byte-count])
                      (:fixture accepted))
                   (= (:byte-count accepted-fixture)
                      (:source-byte-count accepted))
                   (= (:sha256 accepted-fixture)
                      (:source-content-hash accepted))
                   (true? (:utf8-snapshot? accepted))
                   (= [] (:diagnostics accepted))
                   (= :gravity/sh05-macro-template-result (:artifact accepted))
                   (= 1 (:schema-version accepted))
                   (map? (:expansion-template accepted))
                   (not (forbidden-plan-value? (:expansion-template accepted)))
                   (vector? (:digest-requests accepted))
                   (not (forbidden-plan-value? (:digest-requests accepted)))
                   (= (count (:digest-requests accepted))
                      (:digest-request-count accepted))
                   (= 2 (:digest-request-count accepted))
                   (true? (:expansion-template? accepted))
                   (= 'gravity.core/defn (:macro accepted))
                   (sha256-id? (:macro-version accepted))
                   (map? (:expanded-syntax-recipe accepted))
                   (= (:expected-expanded-forms accepted-fixture)
                      (:expanded-forms accepted))
                   (= :rejected (:status rejected))
                   (= (select-keys rejected-fixture [:path :sha256 :byte-count])
                      (:fixture rejected))
                   (= (:byte-count rejected-fixture)
                      (:source-byte-count rejected))
                   (= (:sha256 rejected-fixture)
                      (:source-content-hash rejected))
                   (true? (:utf8-snapshot? rejected))
                   (= :gravity/sh05-macro-template-result (:artifact rejected))
                   (= 1 (:schema-version rejected))
                   (nil? (:expansion-template rejected))
                   (vector? (:digest-requests rejected))
                   (not (forbidden-plan-value? (:digest-requests rejected)))
                   (= (count (:digest-requests rejected))
                      (:digest-request-count rejected))
                   (= 1 (:digest-request-count rejected))
                   (false? (:expansion-template? rejected))
                   (= (:diagnostic rejected-fixture) (:diagnostic rejected))
                   (vector? (:diagnostics rejected))
                   (= 1 (count (:diagnostics rejected)))
                   (valid-c4-diagnostic? (first (:diagnostics rejected))
                                         rejected-source
                                         macro-version
                                         (:digest-requests rejected)))
      (fail! :conformance "macro execution child produced unexpected fixture outcomes"
             {:accepted (select-keys accepted
                                     [:status :artifact :schema-version
                                      :digest-request-count :expansion-template?
                                      :macro :macro-version :expanded-forms
                                      :diagnostics])
              :rejected (select-keys rejected [:status :artifact :schema-version
                                               :digest-request-count
                                               :expansion-template? :diagnostic
                                               :diagnostics])}))))

(defn- validate-execution-summary!
  [summary record plan-id plan-sha256 record-sha256]
  (when-not (and (= :complete (:status summary))
                 (= plan-id (:plan-id summary))
                 (= plan-sha256 (:plan-sha256 summary))
                 (= record-sha256 (:record-sha256 summary))
                 (= plan-id (get-in record [:execution-child :loaded-plan-id]))
                 (= plan-sha256
                    (get-in record [:execution-child :loaded-plan-sha256])))
    (fail! :execution-summary
           "execution child summary does not bind the loaded plan and record"
           {:summary summary
            :plan-id plan-id
            :plan-sha256 plan-sha256
            :record-sha256 record-sha256
            :record-execution (select-keys (:execution-child record)
                                           [:loaded-plan-id :loaded-plan-sha256])})))

(defn- output-attributes
  [^File file]
  (let [path (.toPath file)
        no-follow-links (into-array LinkOption [LinkOption/NOFOLLOW_LINKS])]
    (when (Files/isRegularFile path no-follow-links)
      (Files/readAttributes path BasicFileAttributes no-follow-links))))

(defn- output-ownership
  [^File file]
  (let [canonical (.getCanonicalFile file)
        attributes (output-attributes canonical)]
    (when-not (and attributes (.isRegularFile attributes)
                   (some? (.fileKey attributes)))
      (fail! :output-ownership
             "bootstrap macro output is not an owned regular file"
             {:path (.getPath canonical)}))
    {:file canonical
     :file-key (.fileKey attributes)
     :size (.size attributes)
     :sha256 (file-sha256 canonical)}))

(defn- remember-output!
  [owned ^File file]
  (swap! owned conj (output-ownership file))
  file)

(defn- retained-output-receipt
  [owned]
  (mapv (fn [{:keys [^File file file-key size sha256]}]
          {:path (.getPath file)
           :file-key (str file-key)
           :size size
           :sha256 sha256})
        @owned))

(defn- retain-owned-outputs!
  [owned]
  {:policy (:failure-output-policy default-component)
   :outputs (retained-output-receipt owned)})

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
         inputs (source-compiler-inputs source revision)
         output-root (.getCanonicalFile (io/file (str output-root)))
         plan-file (canonical-file output-root (:output-plan-path component))
         record-file (canonical-file output-root (:output-record-path component))]
     (doseq [^File file [plan-file record-file]]
       (when (.exists file)
         (fail! :output-exists "bootstrap macro output already exists"
                {:path (.getPath file)})))
     (let [owned-outputs (atom #{})]
       (try
         (let [producer (run-child! project-root
                                    (producer-expression (.getPath plan-file))
                                    :producer)
               _ (remember-output! owned-outputs plan-file)
               _ (assert-stable-inputs! inputs project-root :after-producer)
               payload (validate-persisted-plan! (read-edn-file! plan-file "macro plan"))
               producer-summary (:summary producer)
               plan-sha256 (file-sha256 plan-file)
               canonical-source-path (.getPath
                                      (canonical-file project-root macro-source-path))]
           (when-not (and (= :complete (:status producer-summary))
                          (= (get-in payload [:plan :plan-id])
                             (:plan-id producer-summary))
                          (= macro-source-sha256
                             (:source-content-hash producer-summary))
                          (= plan-sha256 (:plan-sha256 producer-summary)))
             (fail! :producer-summary
                    "producer summary does not bind the persisted plan bytes and source"
                    {:summary producer-summary
                     :plan-sha256 plan-sha256
                     :plan-id (get-in payload [:plan :plan-id])}))
           (when-not (= canonical-source-path
                        (get-in payload [:identities :source-path]))
             (fail! :source-plan-binding
                    "persisted plan identity points at a different source path"
                    {:expected canonical-source-path
                     :observed (get-in payload [:identities :source-path])}))
           (when-not (= (:sha256 source) (get-in payload [:plan :source :sha256]))
             (fail! :source-plan-binding "persisted plan source hash drifted"
                    {:source source :plan-source (get-in payload [:plan :source])}))
           (let [execution (run-child! project-root
                                       (execution-expression (.getPath plan-file)
                                                             (.getPath record-file)
                                                             plan-sha256
                                                             (get-in payload [:plan :plan-id])
                                                             source
                                                             revision
                                                             toolchain)
                                       :execution)
                 _ (remember-output! owned-outputs record-file)
                 _ (assert-stable-inputs! inputs project-root :after-execution)
                 record (read-edn-file! record-file "macro execution record")
                 record-sha256 (file-sha256 record-file)
                 macro-version (get-in payload [:identities :public-function-hashes
                                                macro-function])
                 execution-summary (:summary execution)
                 _ (validate-execution-summary!
                    execution-summary record (get-in payload [:plan :plan-id])
                    plan-sha256 record-sha256)
                 result (do
                          (validate-fixture-record! record project-root source
                                                    revision toolchain macro-version)
                          {:component component
                           :source source
                           :compiler-revision revision
                           :toolchain toolchain
                           :producer-summary producer-summary
                           :execution-summary execution-summary
                           :plan {:plan-id (get-in payload [:plan :plan-id])
                                  :identities (:identities payload)}
                           :record record
                           :output-sha256 {:plan (file-sha256 plan-file)
                                           :record record-sha256}
                           :plan-path (.getPath plan-file)
                           :record-path (.getPath record-file)})]
             result))
         (catch clojure.lang.ExceptionInfo error
           (throw (ex-info (.getMessage error)
                           (assoc (ex-data error)
                                  :failure-output-policy
                                  (:failure-output-policy component)
                                  :retained-outputs
                                  (retain-owned-outputs! owned-outputs))
                           error)))
         )))))
