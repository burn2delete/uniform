(ns gravity.self-hosting.sh03-reader-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Coordinator API assumptions for this dedicated lane:
;; - sh03-reader-resolved-result! is the authenticated low-level entry point.
;; - compiler-c2-reader-file-artifact, compiler-c3-syntax-file-artifact,
;;   p15-s23-source-syntax-c2-artifact, and check-file-artifact all route target
;;   source through that entry point after the SH-03 cutover.
;; Legacy-oracle tripwires are resolved by name so an eventual explicit oracle
;; rename does not require coupling this namespace to a private implementation.

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh03_reader_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-03 test source is not on the classpath"
                      {:id "SH03-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH03-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve path "deps.edn")))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-03")

(def ^:private rejected-fixtures
  (sorted-map
   "abbreviation-splice-outside" "C2-ABBREV"
   "abbreviation-unattached" "C2-ABBREV"
   "abbreviation-unquote-outside" "C2-ABBREV"
   "character-invalid" "C2-STRING"
   "character-octal-out-of-range" "C2-STRING"
   "character-surrogate-unicode" "C2-STRING"
   "delimiter-mismatched" "C2-DELIMITER"
   "depth-513" "C2-HASH"
   "encoding-invalid-utf8" "C2-ENCODING"
   "extension-invalid-inst-calendar" "C2-EXTENSION"
   "extension-invalid-uuid-payload" "C2-EXTENSION"
   "extension-unsupported" "C2-EXTENSION"
   "malformed-identifier" "C2-IDENTIFIER"
   "malformed-numeric" "C2-NUMERIC"
   "map-odd-arity" "C2-MAP"
   "metadata-invalid-map-key" "C2-METADATA"
   "metadata-invalid-shape" "C2-METADATA"
   "metadata-invalid-target" "C2-METADATA"
   "namespace-clause-dependency-shape" "L1-NS-SHAPE"
   "namespace-clause-doc-shape" "L1-NS-SHAPE"
   "namespace-clause-key-nonkeyword" "L1-NS-SHAPE"
   "namespace-clause-metadata-shape" "L1-NS-SHAPE"
   "namespace-clause-not-list" "L1-NS-SHAPE"
   "namespace-clause-set-shape" "L1-NS-SHAPE"
   "namespace-clause-single-arity" "L1-NS-SHAPE"
   "namespace-clause-unknown" "L1-NS-SHAPE"
   "namespace-clause-vector-shape" "L1-NS-SHAPE"
   "namespace-missing-name" "L1-NS-SHAPE"
   "namespace-name-nonsymbol" "L1-NS-SHAPE"
   "numeric-incomplete-exponent" "C2-NUMERIC"
   "numeric-invalid-radix" "C2-NUMERIC"
   "set-duplicate" "C2-SET"
   "set-equivalent-decimal" "C2-SET"
   "set-equivalent-decimal-exponent" "C2-SET"
   "set-equivalent-integer" "C2-SET"
   "set-equivalent-ratio" "C2-SET"
   "set-numeric-equivalence-limit" "C2-HASH"
   "string-invalid-escape" "C2-STRING"
   "string-invalid-unicode-escape" "C2-STRING"))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes
   (.toPath (java.io.File. source-path))))

(defn- utf8-text
  [bytes]
  (String. bytes java.nio.charset.StandardCharsets/UTF_8))

(defn- sh03-result
  [source-path]
  (let [bytes (source-bytes source-path)]
    (bootstrap/sh03-reader-resolved-result!
     source-path bytes
     (bootstrap/reader-project-context-for-source source-path)
     bootstrap/standard-reader-options)))

(defn- sh03-result-with-options
  [source-path reader-options]
  (bootstrap/sh03-reader-resolved-result!
   source-path (source-bytes source-path)
   (bootstrap/reader-project-context-for-source source-path)
   reader-options))

(def ^:private pinned-sh03-plan
  (delay (:plan (bootstrap/sh03-reader-current-binding!
                 "<sh03-dedicated-test>"))))

(def ^:private sh03-facade-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity")

(def ^:private sh03-facade-source-byte-count 36589)
(def ^:private sh03-facade-source-content-hash
  "sha256:ce473f5587d1eb8e1a629cd42133f5bbd82465e918db2a35c9b6867988f613d3")
(def ^:private sh03-facade-plan-semantic-hash
  "sha256:9c93c2048d85638adb10039766bf6b85fbd9ce1cf1bce7bda522afd6b8e658b0")
(def ^:private sh03-facade-functions-semantic-hash
  "sha256:7eb60afe90602213e836163084b16aa996e4bf6ec5c69abbf545acebe02f15e9")

(def ^:private sh03-facade-plan
  (delay
    (let [source-path (path sh03-facade-source-relative-path)
          source-bytes (source-bytes source-path)
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source-path :jvm))]
      (is (= sh03-facade-source-byte-count (alength source-bytes)))
      (is (= sh03-facade-source-content-hash
             (str "sha256:" (bootstrap/sha256-bytes-hex source-bytes))))
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path (utf8-text source-bytes)))))

(defn- sh03-facade-execute
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh03-facade-host-runner
    :compiler-artifact-plan? true}
   @sh03-facade-plan function arguments))

(defn- sh03-facade-inputs
  [source-path]
  (let [bytes (source-bytes source-path)
        source-unit
        (bootstrap/sh03-reader-input-source-unit
         source-path bytes
         (bootstrap/reader-project-context-for-source source-path))
        reader-policy
        (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        resolved
        (bootstrap/sh03-reader-resolved-result!
         source-path bytes
         (bootstrap/reader-project-context-for-source source-path)
         bootstrap/standard-reader-options)]
    [source-unit reader-policy (:raw-result resolved)
     (:verification-report resolved)]))

(defn- sh03-facade-substitutions
  [[source-unit reader-policy reader-result verification-report]]
  (let [other-root (str "sha256:" (apply str (repeat 64 "0")))]
    [[(assoc source-unit :project-root-id other-root)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:source-unit :project-root-id] other-root)
      verification-report]
     [(update source-unit :source-byte-count inc)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (update-in reader-result [:source-unit :source-byte-count] inc)
      verification-report]
     [(assoc source-unit :encoding :utf-16)
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:source-unit :encoding] :utf-16)
      verification-report]
     [source-unit (update reader-policy :retain-trivia not)
      reader-result verification-report]
     [source-unit reader-policy
      (update-in reader-result [:source-unit :reader-options
                                :retain-trivia] not)
      verification-report]
     [(assoc-in source-unit [:actual-path-provenance :path]
                "/substituted/source.gravity")
      reader-policy reader-result verification-report]
     [source-unit reader-policy
      (assoc-in reader-result [:actual-path-provenance :path]
                "/substituted/source.gravity")
      verification-report]
     [source-unit reader-policy reader-result
      (assoc verification-report :unexpected true)]
     [source-unit reader-policy reader-result
      (dissoc verification-report :bounds)]
     [source-unit reader-policy reader-result
      (update-in verification-report [:bounds :maximum-tokens] inc)]
     [source-unit reader-policy
      (update-in reader-result [:bounds :maximum-tokens] inc)
      verification-report]
     [(assoc source-unit :unexpected true)
      reader-policy reader-result verification-report]
     [source-unit (assoc reader-policy :unexpected true)
      reader-result verification-report]
     [source-unit reader-policy (assoc reader-result :unexpected true)
      verification-report]]))

(defn- direct-sh03-result
  [source-unit source-bytes reader-policy]
  (bootstrap/sh03-reader-execute-plan!
   "<sh03-dedicated-test>" @pinned-sh03-plan
   bootstrap/sh03-reader-entrypoint
   [source-unit source-bytes reader-policy]))

(defn- direct-sh03-verification
  [source-unit source-bytes reader-policy result]
  (bootstrap/sh03-reader-execute-plan!
   "<sh03-dedicated-test>" @pinned-sh03-plan
   bootstrap/sh03-reader-verifier
   [source-unit source-bytes reader-policy result]))

(defn- thrown-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:unexpected-host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- accepted-reader-products
  [result]
  (select-keys
   result
   [:status :token-stream :form-tree :top-level-form-ids
    :top-level-parsed-records :parsed-semantic-values
    :semantic-value-table
    :literal-decoding-records :semantic-error-deferment-record
    :reader-source-map :bounds :execution-boundary]))

(defn- diagnostic-parity-view
  [diagnostic]
  (select-keys
   diagnostic
   [:id :severity :stage :diagnostic-family :raw-spelling :facts
    :remapped-from :reader-engine-diagnostic :reader-state
   :remediation-records :redactions]))

(defn- records-by-tag
  [artifact]
  (into {} (map (juxt :tag identity)
                (:reader-extension-invocation-records artifact))))

(defn- ids-resolve?
  [records id-key products product-id-key]
  (let [ids (set (map product-id-key products))]
    (every? ids (keep id-key records))))

(defn- ascii-codepoints-text
  [codepoints]
  (apply str (map char codepoints)))

(defn- delete-tree!
  [root-path]
  (doseq [file (reverse (file-seq (.toFile root-path)))]
    (java.nio.file.Files/deleteIfExists (.toPath file))))

(defn- write-bytes!
  [target bytes]
  (java.nio.file.Files/createDirectories (.getParent target)
                                         (make-array
                                          java.nio.file.attribute.FileAttribute
                                          0))
  (java.nio.file.Files/write
   target bytes (make-array java.nio.file.OpenOption 0))
  target)

(defn- same-file?
  [target candidate]
  (when (or (string? candidate) (instance? java.io.File candidate))
    (try
      (= (.getCanonicalPath (java.io.File. (str target)))
         (.getCanonicalPath (io/file candidate)))
      (catch Exception _ false))))

(def ^:private target-reader-tripwire-symbols
  '[read-gravity-source-text
    read-source-form-records-host-oracle
    read-forms
    read-source-artifact
    stage1-reader-token-stream
    stage1-reader-products-from-token-stream
    stage1-reader-decode-atom
    stage1-reader-decode-string
    stage1-reader-decode-character
    p15-s23-stage2-front-end-read-source-form-records
    c2-reader-products-clojure-oracle
    c2-reader-products-legacy-oracle
    c2-reader-products-host-oracle])

(defn- tripwire-binding
  [target var]
  (let [original (var-get var)
        label (-> var meta :name)]
    (fn [& arguments]
      (if (same-file? target (first arguments))
        (throw
         (ex-info
          "A prohibited target-source reader boundary was invoked"
          {:id "SH03-TARGET-SOURCE-REREAD"
           :target target
           :boundary label}))
        (apply original arguments)))))

(defn- target-reader-tripwire-bindings
  [target]
  (let [bootstrap-bindings
        (into {}
              (keep
               (fn [symbol]
                 (when-let [var (ns-resolve 'gravity.bootstrap symbol)]
                   [var (tripwire-binding target var)])))
              target-reader-tripwire-symbols)
        slurp-var #'clojure.core/slurp]
    (assoc bootstrap-bindings slurp-var
           (tripwire-binding target slurp-var))))

(defn- with-target-reader-tripwires
  [target thunk]
  (with-redefs-fn (target-reader-tripwire-bindings target) thunk))

(deftest sh03-co-canonical-fixtures-have-accepted-and-rejected-parity
  (testing "the explicit rejection table covers the complete checked-in surface"
    (let [expected (set (keys rejected-fixtures))]
      (is (= expected (fixture-basenames "rejected" ".gravity")))
      (is (= expected (fixture-basenames "rejected" ".qst")))))
  (testing "accepted complete surface and physical newline forms"
    (let [basenames ["complete-reader-surface"
                     "depth-512"
                     "namespace-clause-shapes"
                     "newline-lf"
                     "newline-crlf"
                     "newline-cr"
                     "numeric-work-boundary"
                     "numeric-semantic-work-deferred"
                     "numeric-set-distinct-deferred"
                     "unicode-supplementary"]]
      (is (= (set basenames) (fixture-basenames "accepted" ".gravity")))
      (is (= (set basenames) (fixture-basenames "accepted" ".qst")))
      (doseq [basename basenames]
      (let [gravity-path (fixture-path "accepted" basename ".gravity")
            qst-path (fixture-path "accepted" basename ".qst")
            gravity (sh03-result gravity-path)
            qst (sh03-result qst-path)]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path)))
        (is (= :accepted (get-in gravity [:result :status])))
        (is (= :accepted (get-in qst [:result :status])))
        (is (= (accepted-reader-products (:result gravity))
               (accepted-reader-products (:result qst))))
        (is (= gravity-path
               (get-in gravity [:result :actual-path-provenance :path])))
        (is (= qst-path
               (get-in qst [:result :actual-path-provenance :path])))
        (is (= [".gravity" ".qst"]
               [(get-in gravity
                        [:result :actual-path-provenance :extension])
                (get-in qst
                        [:result :actual-path-provenance :extension])]))))))
  (testing "every structured rejection is co-canonical"
    (doseq [[basename expected-id] rejected-fixtures]
      (let [gravity-path (fixture-path "rejected" basename ".gravity")
            qst-path (fixture-path "rejected" basename ".qst")
            gravity (sh03-result gravity-path)
            qst (sh03-result qst-path)
            gravity-diagnostic (get-in gravity [:result :diagnostics 0])
            qst-diagnostic (get-in qst [:result :diagnostics 0])]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path)))
        (is (= :rejected (get-in gravity [:result :status])))
        (is (= :rejected (get-in qst [:result :status])))
        (is (= expected-id (:id gravity-diagnostic)))
        (is (= expected-id (:id qst-diagnostic)))
        (is (= (diagnostic-parity-view gravity-diagnostic)
               (diagnostic-parity-view qst-diagnostic)))
        (is (not (contains? gravity-diagnostic :diagnostic-id-request)))
        (is (boolean
             (re-matches #"sha256:[0-9a-f]{64}"
                         (:diagnostic-id gravity-diagnostic))))))))

(deftest sh03-public-c2-c3-and-p15-routing-preserves-both-extensions
  (let [artifacts
        (into {}
              (for [extension [".gravity" ".qst"]]
                (let [source-path
                      (fixture-path "accepted"
                                    "complete-reader-surface" extension)
                      text (utf8-text (source-bytes source-path))
                      c2 (bootstrap/compiler-c2-reader-file-artifact
                          source-path)
                      c3 (bootstrap/compiler-c3-syntax-file-artifact
                          source-path)
                      p15-c2
                      (bootstrap/p15-s23-source-syntax-c2-artifact
                       source-path text)
                      p15-c3
                      (bootstrap/p15-s23-source-syntax-c3-artifact
                       source-path p15-c2)]
                  [extension {:c2 c2 :c3 c3
                              :p15-c2 p15-c2 :p15-c3 p15-c3}])))]
    (doseq [[extension {:keys [c2 c3 p15-c2 p15-c3]}] artifacts]
      (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2))
          extension)
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind c3))
          extension)
      (is (= :gravity/stage0-c2-reader-document-artifact (:kind p15-c2))
          extension)
      (is (= :gravity/stage0-c3-syntax-object-artifact (:kind p15-c3))
          extension)
      (is (true? (get-in c2
                         [:representation-boundary :lexical-token-stream?])))
      (is (true? (get-in c2
                         [:representation-boundary :nested-form-tree?])))
      (is (= :complete
             (get-in c2 [:representation-boundary
                         :sh03-bootstrap-subset-status])))
      (is (= #{:full-language-literal-surface
               :full-language-reader-abbreviation-surface
               :full-language-reader-extension-registry
               :host-and-seed-retirement}
             (set (get-in c2 [:representation-boundary
                              :remaining-reader-boundaries]))))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :literal-status])))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :abbreviation-status])))
      (is (= :complete-bootstrap-subset
             (get-in c2 [:c2-reader-results :extension-status])))
      (is (= :partial (get-in c2 [:c2-reader-results :status])))
      (is (= :passed
             (get-in c3 [:syntax-verification-report :status])))
      (is (= :passed
             (get-in p15-c3 [:syntax-verification-report :status])))
      (let [extensions (records-by-tag c2)
            forms (:form-tree c2)
            tokens (:token-stream c2)
            gravity-map (:gravity-reader-source-map c2)
            envelope (get-in c2 [:gravity-reader-boundary
                                 :authenticated-envelope])
            literals (:literal-decoding-records c2)
            deferred (get-in c2 [:semantic-error-deferment-record
                                 :deferred-literal-records])]
        (is (= #{'inst 'uuid} (set (keys extensions))) extension)
        (doseq [tag ['inst 'uuid]]
          (let [record (get extensions tag)]
            (is (= :invoked (:status record)) (str extension " " tag))
            (is (seq (:invocations record)) (str extension " " tag))
            (is (= #{} (:build-effects record) (:capabilities record)))
            (is (= #{:kernel :core :hosted :meta} (:profiles record)))
            (is (ids-resolve? (:invocations record) :form-id
                              forms :form-id))))
        (is (= literals (bootstrap/c2-literal-records forms)) extension)
        (is (= deferred
               (bootstrap/c2-deferred-semantic-literals forms)) extension)
        (is (some #(and (= :ratio (:kind %))
                        (= "1/0" (:raw %)))
                  deferred)
            extension)
        (is (= (mapv #(select-keys % [:token-id :span]) tokens)
               (:token-spans gravity-map))
            extension)
        (is (= (mapv #(select-keys % [:form-id :span :parent-form-id])
                     forms)
               (:form-spans gravity-map))
            extension)
        (is (ids-resolve? literals :form-id forms :form-id) extension)
        (is (ids-resolve? deferred :form-id forms :form-id) extension)
        (is (= false
               (get-in c2 [:gravity-reader-boundary
                           :target-source-reread?]))
            extension)
        (is (= :SH-03
               (get-in c2 [:gravity-reader-boundary :slice])) extension)
        (is (= {:status :not-executed
                :entrypoints
                bootstrap/sh03-reader-uncredited-source-model-entrypoints
                :self-hosting-credit? false
                :seed-retirement-credit? false
                :release-credit? false}
               (get-in c2 [:gravity-reader-boundary
                           :uncredited-source-models])) extension)
        (is (= bootstrap/p15-s23-sh02-stage-envelope-keys
               (set (keys envelope))) extension)
        (is (= :gravity/sh02-stage-authenticated-envelope
               (:artifact envelope)) extension)
        (is (= :accepted (:status envelope)) extension)
        (is (= :c2-reader (:stage envelope)) extension)
        (is (= :gravity/sh03-reader-products
               (get-in envelope [:sealed-artifact :artifact-kind])) extension)
        (is (= :gravity-source (:semantic-authority envelope)) extension)
        (is (= :template-replay-passed
               (get-in envelope [:gravity-template-replay :status])) extension)
        (is (= :pending-host-resolution
               (get-in envelope
                       [:gravity-template-replay
                        :identity-enforcement])) extension)
        (is (false?
             (get-in envelope
                     [:gravity-template-replay
                      :eligible-for-contextual-acceptance?])) extension)
        (is (pos? (:request-count envelope)) extension)
        (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %)
                    [(:semantic-envelope-id envelope)
                     (:provenance-binding-id envelope)
                     (:request-graph-id envelope)]) extension)))
    (is (= (get-in artifacts [".gravity" :c2 :parsed-semantic-values])
           (get-in artifacts [".qst" :c2 :parsed-semantic-values])))
    (is (= (get-in artifacts [".gravity" :p15-c2
                              :parsed-semantic-values])
           (get-in artifacts [".qst" :p15-c2
                              :parsed-semantic-values]))))
  (testing "public rejected routes contain source failures"
    (doseq [basename ["malformed-numeric" "encoding-invalid-utf8"
                      "namespace-clause-unknown" "depth-513"
                      "set-equivalent-decimal-exponent"]
            extension [".gravity" ".qst"]]
      (let [source-path (fixture-path "rejected" basename extension)
            expected-id (if (= "L1-NS-SHAPE"
                               (get rejected-fixtures basename))
                          "C2-NS-SHAPE"
                          (get rejected-fixtures basename))]
        (doseq [[route invoke]
                [[:c2 #(bootstrap/compiler-c2-reader-file-artifact
                        source-path)]
                 [:c3 #(bootstrap/compiler-c3-syntax-file-artifact
                        source-path)]
                 [:check #(bootstrap/check-file-artifact source-path)]]]
          (let [data (thrown-data invoke)]
            (is (= expected-id (:id data)) (str route " " source-path))
            (is (nil? (:unexpected-host-error data))
                (str route " " source-path))))))))

(deftest sh03-extension-policy-covers-invoked-and-idle-handlers
  (doseq [extension [".gravity" ".qst"]]
    (let [complete-path
          (fixture-path "accepted" "complete-reader-surface" extension)
          newline-path
          (fixture-path "accepted" "newline-lf" extension)
          complete
          (bootstrap/compiler-c2-reader-file-artifact
           complete-path)
          newline
          (bootstrap/compiler-c2-reader-file-artifact
           newline-path)
          gravity-complete
          (get-in (sh03-result complete-path)
                  [:result :reader-extension-invocation-records])
          gravity-newline
          (get-in (sh03-result newline-path)
                  [:result :reader-extension-invocation-records])
          policy (:reader-extension-policy complete)
          complete-records (records-by-tag complete)
          newline-records (records-by-tag newline)]
      (is (= :registered (:status policy)) extension)
      (is (= ['inst 'uuid] (mapv :tag (:extensions policy))) extension)
      (is (= ["inst" "uuid"]
             (mapv #(ascii-codepoints-text (:tag-codepoints %))
                   gravity-complete)) extension)
      (is (= [:invoked :invoked]
             (mapv :status gravity-complete)) extension)
      (is (every? (comp seq :invocations) gravity-complete) extension)
      (is (= [:registered-not-invoked :registered-not-invoked]
             (mapv :status gravity-newline)) extension)
      (is (every? (comp empty? :invocations) gravity-newline) extension)
      (doseq [tag ['inst 'uuid]]
        (is (= :invoked (get-in complete-records [tag :status]))
            (str extension " " tag))
        (is (= :registered-not-invoked
               (get-in newline-records [tag :status]))
            (str extension " " tag))
        (is (= [] (get-in newline-records [tag :invocations]))
            (str extension " " tag))))))

(deftest sh03-resolved-result-binds-digests-and-rejects-replay-mutation
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        execution (sh03-result source-path)
        raw (:raw-result execution)
        result (:result execution)
        report (:verification-report execution)
        requests (:resolved-requests execution)
        digests (:resolved-digests execution)]
    (is (= :accepted (:status raw) (:status result)))
    (is (= :accepted (:status report)))
    (is (true? (:verified? report)))
    (is (= [:source-content :source-unit :token-stream
            :form-tree :extension-invocation-set :reader-result]
           (mapv :key requests)))
    (is (= 6 (count digests)))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) digests))
    (is (= (first digests) (:observed-id (first requests))))
    (is (= (second digests) (get-in result [:source-unit :source-id])))
    (is (= (nth digests 2)
           (get-in result [:incremental-reader-hashes :token-stream])))
    (is (= (nth digests 3)
           (get-in result [:incremental-reader-hashes :form-tree])))
    (is (= (nth digests 4)
           (get-in result
                   [:incremental-reader-hashes
                    :extension-invocation-set])))
    (is (= (nth digests 5)
           (get-in result [:incremental-reader-hashes :reader-result])
           (:reader-result-id execution)))
    (is (= :gravity-source
           (get-in execution [:plan-binding :semantic-authority])))
    (is (true? (get-in execution
                       [:plan-binding :generic-bridge-residual?])))
    (is (false? (get-in execution [:plan-binding :self-hosted?]))))
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        original bootstrap/sh03-reader-execute-plan!
        changed? (atom false)
        data
        (with-redefs
          [bootstrap/sh03-reader-execute-plan!
           (fn [request-source plan function arguments]
             (let [value (original request-source plan function arguments)]
               (if (and (= function bootstrap/sh03-reader-entrypoint)
                        (compare-and-set! changed? false true))
                 (assoc-in value [:semantic-reader-template :status]
                           :rejected)
                 value)))]
          (thrown-data #(sh03-result source-path)))]
    (is (= "C2-HASH" (:id data)))
    (is (some #{:fresh-gravity-sh03-reader-result-replay}
              (:missing-fields data)))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-gravity-facade-rejects-source-result-and-report-substitution
  (let [source-path
        (fixture-path "accepted" "complete-reader-surface" ".gravity")
        inputs (sh03-facade-inputs source-path)
        plan @sh03-facade-plan]
    (is (= 29 (count (:functions plan))))
    (is (= sh03-facade-plan-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= sh03-facade-functions-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (:functions plan))))
    (is (true?
         (sh03-facade-execute
          'l1-c2-reader-result-compatible? inputs)))
    (doseq [candidate (sh03-facade-substitutions inputs)]
      (is (false?
           (sh03-facade-execute
            'l1-c2-reader-result-compatible? candidate)) candidate))))

(deftest sh03-gravity-facade-package-boundary-fails-closed
  (let [accepted-path
        (fixture-path "accepted" "complete-reader-surface" ".qst")
        rejected-path
        (fixture-path "rejected" "malformed-numeric" ".qst")
        accepted-inputs (sh03-facade-inputs accepted-path)
        rejected-inputs (sh03-facade-inputs rejected-path)
        accepted
        (sh03-facade-execute 'l1-c2-package-reader-result accepted-inputs)
        rejected-source
        (sh03-facade-execute 'l1-c2-package-reader-result rejected-inputs)
        no-credit
        {:artifact :gravity/l1-c2-reader-compatibility-package
         :compatibility-status :compatible
         :compatibility-only? true
         :authentication-credit? false
         :authoritative-reader-result? false
         :authoritative-route :sh03-fresh-verifier-and-digest-resolution
         :fresh-verifier-executed-here? false
         :digest-resolution-executed-here? false}]
    (doseq [[expected-status package]
            [[:accepted accepted] [:rejected rejected-source]]]
      (is (= no-credit
             (select-keys package (keys no-credit))))
      (is (= expected-status (:reader-result-status package)))
      (is (map? (:reader-verification-report package))))
    (doseq [candidate (sh03-facade-substitutions accepted-inputs)]
      (let [package
            (sh03-facade-execute 'l1-c2-package-reader-result candidate)]
        (is (= :gravity/l1-c2-reader-compatibility-package
               (:artifact package)) candidate)
        (is (= :rejected (:compatibility-status package)) candidate)
        (is (true? (:compatibility-only? package)) candidate)
        (is (false? (:authentication-credit? package)) candidate)
        (is (false? (:authoritative-reader-result? package)) candidate)
        (is (nil? (:source-unit-record package)) candidate)
        (is (empty? (:token-stream package)) candidate)
        (is (empty? (:form-tree package)) candidate)
        (is (empty? (:semantic-value-table package)) candidate)
        (is (= "C2-HASH"
               (get-in package [:reader-diagnostics 0 :id])) candidate)
        (is (false?
             (get-in package [:execution-boundary :self-hosted?]))
            candidate)))))

(deftest sh03-precomputed-reader-and-c3-seams-require-opaque-authority
  (let [source-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        bytes (source-bytes source-path)
        source-text (utf8-text bytes)
        project-context
        (bootstrap/reader-project-context-for-source source-path)
        legacy-products
        (bootstrap/c2-reader-products-clojure-oracle
         source-path source-text bootstrap/standard-reader-options
         project-context)
        public-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context legacy-products))
        resolved
        (bootstrap/sh03-reader-resolved-result!
         source-path bytes project-context bootstrap/standard-reader-options)
        products
        (bootstrap/sh03-reader-adapt-products!
         source-path source-text bytes bootstrap/standard-reader-options
         project-context resolved)
        authority
        (var-get
         (ns-resolve 'gravity.bootstrap
                     'sh03-reader-internal-product-authority))
        mutated-products
        (assoc products :sh03-reader-adapter-contract :substituted)
        private-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context mutated-products authority))
        payload-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context
           (assoc products :parsed-values [:substituted]) authority))
        raw-result-c2-data
        (thrown-data
         #(bootstrap/compiler-c2-reader-source-artifact
           source-path source-text project-context
           (assoc-in products
                     [:sh03-reader-raw-result :parsed-semantic-values 0]
                     {:artifact :gravity/semantic-value-reference
                      :value-id 999})
           authority))
        c2 (bootstrap/compiler-c2-reader-file-artifact source-path)
        public-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context c2))
        private-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context
           (assoc-in c2 [:gravity-reader-boundary :adapter-contract]
                     :substituted)
           authority))
        substituted-c2
        (let [candidate (assoc c2 :parsed-semantic-values [:substituted])]
          (assoc candidate :artifact-id
                 (bootstrap/c2-reader-artifact-id candidate)))
        payload-c3-data
        (thrown-data
         #(bootstrap/compiler-c3-syntax-source-artifact
           source-path source-text project-context substituted-c2 authority))]
    (doseq [data [public-c2-data private-c2-data payload-c2-data
                  raw-result-c2-data
                  public-c3-data private-c3-data]]
      (is (= "C2-HASH" (:id data)) data)
      (is (seq (:missing-fields data)) data)
      (is (nil? (:unexpected-host-error data)) data))
    (is (= "C3-FACT-STALE" (:id payload-c3-data)) payload-c3-data)
    (is (some #{:parsed-semantic-values-valid?}
              (:missing-fields payload-c3-data))
        payload-c3-data)
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2)))
    (is (= :gravity/stage0-c3-syntax-object-artifact
           (:kind (bootstrap/compiler-c3-syntax-file-artifact source-path))))))

(deftest sh03-public-routing-does-not-invoke-target-source-reader-oracles
  (let [temporary
        (java.nio.file.Files/createTempFile
         "gravity-sh03-tripwire-" ".gravity"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-path (str temporary)
        text (str "(ns sh03.tripwire (:profile :hosted))\n"
                  "(def value ^:reader-owned [1 2 3])\n")]
    (try
      (java.nio.file.Files/write
       temporary (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (let [products
            (with-target-reader-tripwires
              source-path
              (fn []
                (let [low-level (sh03-result source-path)
                      c2 (bootstrap/compiler-c2-reader-file-artifact
                          source-path)
                      c3 (bootstrap/compiler-c3-syntax-file-artifact
                          source-path)
                      p15
                      (bootstrap/p15-s23-source-syntax-c2-artifact
                       source-path text)
                      checked (bootstrap/check-file-artifact source-path)]
                  {:low-level low-level :c2 c2 :c3 c3
                   :p15 p15 :checked checked})))]
        (is (= :accepted (get-in products [:low-level :result :status])))
        (is (= :gravity/stage0-c2-reader-document-artifact
               (get-in products [:c2 :kind])))
        (is (= :gravity/stage0-c3-syntax-object-artifact
               (get-in products [:c3 :kind])))
        (is (= :gravity/stage0-c2-reader-document-artifact
               (get-in products [:p15 :kind])))
        (is (map? (:checked products))))
      (finally
        (java.nio.file.Files/deleteIfExists temporary)))))

(deftest sh03-specialized-check-routes-reuse-authenticated-records
  (doseq [[relative expected-kind]
          [["bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity"
            :gravity/stage0-b14-backend-conformance-document-artifact]
           ["bootstrap/clojure/fixtures/accepted/core-semantics.gravity"
            :gravity/stage0-core-artifact]
           ["bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity"
            :gravity/stage0-runtime-selection-artifact]
           ["bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity"
            :gravity/stage0-managed-runtime-artifact]]]
    (let [source-path (path relative)
          artifact
          (with-target-reader-tripwires
            source-path #(bootstrap/check-file-artifact source-path))]
      (is (= expected-kind (:kind artifact)) relative)
      (is (map? artifact) relative))))

(deftest sh03-public-reader-accepts-its-own-gravity-source
  (let [source-path (path bootstrap/sh03-reader-source-relative-path)
        c2 (bootstrap/compiler-c2-reader-file-artifact source-path)]
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind c2)))
    (is (= source-path (get-in c2 [:source-unit-record :path])))
    (is (= :SH-03 (get-in c2 [:gravity-reader-boundary :slice])))
    (is (= false
           (get-in c2 [:gravity-reader-boundary :target-source-reread?])))
    (is (seq (:token-stream c2)))
    (is (seq (:form-tree c2)))
    (is (= :complete-for-slice
           (get-in c2 [:representation-boundary :status])))
    (is (= :accepted
           (get-in c2 [:gravity-reader-boundary :authenticated-envelope
                       :status])))))

(deftest sh03-unicode-character-literals-cover-supplementary-scalars
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-unicode-scalar-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        scalar-codepoint 0x1f600
        scalar-text (String. (Character/toChars scalar-codepoint))
        accepted-text
        (str "(ns sh03.unicode-scalar (:profile :hosted))\n"
             "(def smile \\" scalar-text ")\n")
        rejected-text "\\uD800\n"]
    (try
      (doseq [extension [".gravity" ".qst"]]
        (let [accepted-path
              (str (write-bytes!
                    (.resolve temporary-root (str "accepted" extension))
                    (.getBytes accepted-text
                               java.nio.charset.StandardCharsets/UTF_8)))
              rejected-path
              (str (write-bytes!
                    (.resolve temporary-root (str "rejected" extension))
                    (.getBytes rejected-text
                               java.nio.charset.StandardCharsets/UTF_8)))
              low-level (sh03-result accepted-path)
              artifact
              (bootstrap/compiler-c2-reader-file-artifact accepted-path)
              host-value
              (some
               (fn [value]
                 (when (and (map? value)
                            (= :gravity/unicode-scalar-character
                               (:artifact value)))
                   value))
               (tree-seq coll? seq (:parsed-semantic-values artifact)))
              rejected
              (thrown-data
               #(bootstrap/compiler-c2-reader-file-artifact rejected-path))]
          (is (= :accepted (get-in low-level [:result :status])) extension)
          (is (some #(= scalar-codepoint
                        (get-in % [:descriptor :codepoint]))
                    (get-in low-level [:result :semantic-value-table]))
              extension)
          (is (= scalar-codepoint (:codepoint host-value)) extension)
          (is (= scalar-text (:text host-value)) extension)
          (is (= "C2-STRING" (:id rejected)) extension)
          (is (nil? (:unexpected-host-error rejected)) extension)))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-numeric-set-equivalence-is-bounded-and-fail-closed
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-numeric-equivalence-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        huge-positive (str (apply str (repeat 257 "1")) ".0")
        huge-negative (str "-" huge-positive)
        huge-zero-left (str "0." (apply str (repeat 257 "0")))
        huge-zero-right (str "0." (apply str (repeat 258 "0")))
        nested-left (str (apply str (repeat 32 "[")) huge-zero-left
                         (apply str (repeat 32 "]")))
        nested-right (str (apply str (repeat 32 "[")) huge-zero-right
                          (apply str (repeat 32 "]")))
        cases
        [{:name "bounded-equivalent" :source "#{1.0 1.00}\n"
          :expected-id "C2-SET"}
         {:name "bounded-distinct" :source "#{1.0 2.0}\n"
          :accepted? true}
         {:name "deferred-ambiguous"
          :source (str "#{" huge-zero-left " " huge-zero-right "}\n")
          :expected-id "C2-HASH" :expected-reason
          :numeric-set-equivalence-limit}
         {:name "deferred-provably-distinct"
          :source (str "#{" huge-positive " " huge-negative "}\n")
          :accepted? true}
         {:name "deferred-standalone" :source (str huge-positive "\n")
          :accepted? true :deferred? true}
         {:name "deferred-nested-ambiguous"
          :source (str "#{" nested-left " " nested-right "}\n")
          :expected-id "C2-HASH" :expected-reason
          :numeric-set-equivalence-limit}]]
    (try
      (doseq [extension [".gravity" ".qst"]
              {:keys [name source accepted? deferred? expected-id
                      expected-reason]} cases]
        (let [source-path
              (str (write-bytes!
                    (.resolve temporary-root (str name extension))
                    (.getBytes source
                               java.nio.charset.StandardCharsets/UTF_8)))]
          (if accepted?
            (let [artifact
                  (bootstrap/compiler-c2-reader-file-artifact source-path)]
              (is (= :gravity/stage0-c2-reader-document-artifact
                     (:kind artifact)) (str extension " " name))
              (when deferred?
                (is (true? (get-in artifact
                                   [:semantic-error-deferment-record
                                    :deferred?]))
                    (str extension " " name))))
            (let [data
                  (thrown-data
                   #(bootstrap/compiler-c2-reader-file-artifact source-path))
                  low-level (when expected-reason (sh03-result source-path))
                  reason (get-in low-level
                                 [:result :diagnostics 0
                                  :reader-state :reason])]
              (is (= expected-id (:id data)) (str extension " " name))
              (when expected-reason
                (is (= expected-reason reason) (str extension " " name)))
              (is (nil? (:unexpected-host-error data))
                  (str extension " " name))))))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-invalid-direct-inputs-have-compact-replayable-summaries
  (let [normal-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        empty-bytes (byte-array 0)
        empty-vector []
        source-unit
        (bootstrap/sh03-reader-input-source-unit
         normal-path empty-bytes
         (bootstrap/reader-project-context-for-source normal-path))
        policy (bootstrap/sh03-reader-input-policy
                bootstrap/standard-reader-options)
        giant-inner-tag (vec (repeat 100000 105))
        giant-outer-tags (vec (repeat 100000 [105 110 115 116]))
        oversized-bytes (vec (repeat 1048577 0))
        cases
        [[:long-logical-id
          (assoc source-unit :logical-source-id (apply str (repeat 1025 "a")))
          empty-vector policy]
         [:long-path
          (assoc-in source-unit [:actual-path-provenance :path]
                    (apply str (repeat 1025 "p")))
          empty-vector policy]
         [:mismatched-bytes source-unit [0] policy]
         [:oversized-bytes source-unit oversized-bytes policy]
         [:outer-tag-count source-unit empty-vector
          (assoc policy :enabled-reader-tags giant-outer-tags)]
         [:inner-tag-width source-unit empty-vector
          (assoc policy :enabled-reader-tags [giant-inner-tag])]
         [:duplicate-tags source-unit empty-vector
          (assoc policy :enabled-reader-tags
                 [[105 110 115 116] [105 110 115 116]])]]]
    (doseq [[label unit bytes reader-policy] cases]
      (let [result (direct-sh03-result unit bytes reader-policy)
            report (direct-sh03-verification
                    unit bytes reader-policy result)
            carrier
            (bootstrap/p15-s23-trusted-carrier-validation
             result :default-only
             bootstrap/sh03-reader-result-maximum-nodes
             bootstrap/sh03-reader-result-maximum-depth
             bootstrap/sh03-reader-result-maximum-width)]
        (is (= :rejected (:status result)) (name label))
        (is (= [] (get-in result [:digest-requests 0 :preimage]))
            (name label))
        (is (= :passed (:status carrier)) (name label))
        (is (< (:observed-nodes carrier) 1024) (name label))
        (is (true? (:verified? report)) (name label))
        (is (= :accepted (:status report)) (name label))))))

(deftest sh03-result-carrier-depth-is-bounded-with-structured-failure
  (let [small-validation
        (bootstrap/sh03-reader-require-result-carrier!
         "carrier.gravity" :test-carrier {:value [1 2 3]})
        over-depth
        (loop [remaining (inc bootstrap/sh03-reader-result-maximum-depth)
               value nil]
          (if (zero? remaining)
            value
            (recur (dec remaining) [value])))
        data
        (thrown-data
         #(bootstrap/sh03-reader-require-result-carrier!
           "carrier.gravity" :test-carrier over-depth))]
    (is (= :passed (:status small-validation)))
    (is (= "C2-HASH" (:id data)))
    (is (some #{:trusted-bounded-sh03-reader-result}
              (:missing-fields data)))
    (is (= :test-carrier (get-in data [:facts :carrier])))
    (is (integer? (get-in data [:facts :observed-depth])))
    (is (= bootstrap/sh03-reader-result-maximum-depth
           (get-in data [:facts :maximum-depth])))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-source-identity-input-is-bounded-before-reader-execution
  (let [normal-path
        (fixture-path "accepted" "newline-lf" ".gravity")
        oversized-path
        (str (apply str
                    (repeat
                     (inc bootstrap/sh03-reader-input-maximum-identity-code-units)
                     "a"))
             ".gravity")
        data
        (thrown-data
         #(bootstrap/sh03-reader-input-source-unit
           oversized-path
           (byte-array 0)
           (bootstrap/reader-project-context-for-source normal-path)))]
    (is (= "C2-HASH" (:id data)))
    (is (some #{:bounded-sh03-reader-source-identity}
              (:missing-fields data)))
    (is (= bootstrap/sh03-reader-input-maximum-identity-code-units
           (get-in data [:facts :maximum-code-units])))
    (is (= bootstrap/sh03-reader-input-maximum-identity-utf8-bytes
           (get-in data [:facts :maximum-utf8-bytes])))
    (is (not (.contains (pr-str data) oversized-path)))
    (is (nil? (:unexpected-host-error data)))))

(deftest sh03-identities-are-cross-root-neutral-with-actual-path-provenance
  (let [left-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-left-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        right-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-right-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        manifest-bytes
        (.getBytes "{:paths [\"src\"]}\n"
                   java.nio.charset.StandardCharsets/UTF_8)
        source
        (str "(ns sh03.same-relative (:profile :hosted))\n"
             "(def value ^:stable 1/0)\n")
        bytes (.getBytes source java.nio.charset.StandardCharsets/UTF_8)
        left-source (.resolve left-root "src/same.gravity")
        right-source (.resolve right-root "src/same.gravity")]
    (try
      (write-bytes! (.resolve left-root "deps.edn") manifest-bytes)
      (write-bytes! (.resolve right-root "deps.edn") manifest-bytes)
      (write-bytes! left-source bytes)
      (write-bytes! right-source bytes)
      (let [left-path (str left-source)
            right-path (str right-source)
            left (sh03-result left-path)
            right (sh03-result right-path)
            left-result (:result left)
            right-result (:result right)
            left-c2 (bootstrap/compiler-c2-reader-file-artifact left-path)
            right-c2 (bootstrap/compiler-c2-reader-file-artifact right-path)]
        (is (not= left-path right-path))
        (is (= left-path
               (get-in left-result [:actual-path-provenance :path])))
        (is (= right-path
               (get-in right-result [:actual-path-provenance :path])))
        (is (= (dissoc left-result :actual-path-provenance)
               (dissoc right-result :actual-path-provenance)))
        (is (= (:reader-result-id left) (:reader-result-id right)))
        (is (= (get-in left-c2 [:source-unit-record :source-id])
               (get-in right-c2 [:source-unit-record :source-id])))
        (is (= (:incremental-reader-hashes left-c2)
               (:incremental-reader-hashes right-c2)))
        (is (= (:artifact-id left-c2) (:artifact-id right-c2)))
        (is (= left-path (get-in left-c2 [:source-unit-record :path])))
        (is (= right-path (get-in right-c2 [:source-unit-record :path]))))
      (finally
        (delete-tree! left-root)
        (delete-tree! right-root)))))

(deftest sh03-reader-policy-is-bound-into-resolved-and-adapted-identity
  (let [source-path
        (fixture-path "accepted" "complete-reader-surface" ".gravity")
        source-text (utf8-text (source-bytes source-path))
        without-trivia (assoc bootstrap/standard-reader-options
                              :retain-comments false)
        standard (sh03-result-with-options
                  source-path bootstrap/standard-reader-options)
        compact (sh03-result-with-options source-path without-trivia)
        context (bootstrap/reader-project-context-for-source source-path)
        standard-products
        (bootstrap/c2-reader-products
         source-path source-text bootstrap/standard-reader-options context)
        compact-products
        (bootstrap/c2-reader-products
         source-path source-text without-trivia context)]
    (is (= :accepted (get-in standard [:result :status])))
    (is (= :accepted (get-in compact [:result :status])))
    (is (not= (:reader-result-id standard) (:reader-result-id compact)))
    (is (not= (get-in standard [:result :incremental-reader-hashes])
              (get-in compact [:result :incremental-reader-hashes])))
    (is (true? (get-in standard [:result :source-unit
                                 :reader-options :retain-trivia])))
    (is (false? (get-in compact [:result :source-unit
                                 :reader-options :retain-trivia])))
    (is (some :trivia? (get-in standard [:result :token-stream])))
    (is (not-any? :trivia? (get-in compact [:result :token-stream])))
    (is (not= (get-in standard-products [:source-unit :source-id])
              (get-in compact-products [:source-unit :source-id])))
    (is (not= (:token-stream standard-products)
              (:token-stream compact-products)))
    (is (= (get-in standard [:result :actual-path-provenance])
           (get-in compact [:result :actual-path-provenance])))))

(deftest sh03-public-reader-accepts-the-exact-depth-limit-deterministically
  (let [temporary-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh03-depth-limit-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-text
        (str "(ns sh03.depth-limit (:profile :hosted))\n"
             (apply str (repeat 512 "["))
             "0"
             (apply str (repeat 512 "]"))
             "\n")
        source-bytes
        (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)]
    (try
      (doseq [extension [".gravity" ".qst"]]
        (let [source-path
              (str (write-bytes!
                    (.resolve temporary-root (str "depth-512" extension))
                    source-bytes))
              first-low-level (sh03-result source-path)
              second-low-level (sh03-result source-path)
              first-artifact
              (bootstrap/compiler-c2-reader-file-artifact source-path)
              second-artifact
              (bootstrap/compiler-c2-reader-file-artifact source-path)]
          (is (= :accepted (get-in first-low-level [:result :status]))
              extension)
          (is (= 512 (get-in first-low-level
                             [:result :bounds :maximum-delimiter-depth]))
              extension)
          (is (= (:reader-result-id first-low-level)
                 (:reader-result-id second-low-level)) extension)
          (is (= :gravity/stage0-c2-reader-document-artifact
                 (:kind first-artifact)) extension)
          (is (= :passed
                 (get-in first-artifact
                         [:lexical-product-validation :status])) extension)
          ;; The atomic leaf is one graph level below the 512 collection
          ;; levels; the reader resource bound itself counts delimiters.
          (is (= 513
                 (get-in first-artifact
                         [:lexical-product-validation :max-form-depth]))
              extension)
          (is (= (:artifact-id first-artifact)
                 (:artifact-id second-artifact)) extension)
          (is (= (:incremental-reader-hashes first-artifact)
                 (:incremental-reader-hashes second-artifact)) extension)))
      (doseq [extension [".gravity" ".qst"]]
        (let [data
              (thrown-data
               #(bootstrap/compiler-c2-reader-file-artifact
                 (fixture-path "rejected" "depth-513" extension)))]
          (is (= "C2-HASH" (:id data)) extension)
          (is (nil? (:unexpected-host-error data)) extension)))
      (finally
        (delete-tree! temporary-root)))))

(deftest sh03-public-c2-reader-scales-to-five-thousand-top-level-forms
  (let [temporary
        (java.nio.file.Files/createTempFile
         "gravity-sh03-scale-" ".gravity"
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-path (str temporary)
        source-text
        (str "(ns sh03.scale (:profile :hosted))\n"
             (apply str (repeat 4999 "0\n")))]
    (try
      (java.nio.file.Files/write
       temporary
       (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (let [first-artifact
            (bootstrap/compiler-c2-reader-file-artifact source-path)
            second-artifact
            (bootstrap/compiler-c2-reader-file-artifact source-path)]
        (is (= :gravity/stage0-c2-reader-document-artifact
               (:kind first-artifact)))
        (is (= 5000 (count (:top-level-form-ids first-artifact))))
        (is (= 5000 (count (:parsed-semantic-values first-artifact))))
        (is (= :complete-for-slice
               (get-in first-artifact [:representation-boundary :status])))
        (is (true? (get-in first-artifact
                           [:lexical-product-validation :graph-valid?])))
        (is (= (:artifact-id first-artifact)
               (:artifact-id second-artifact)))
        (is (= (:incremental-reader-hashes first-artifact)
               (:incremental-reader-hashes second-artifact))))
      (finally
        (java.nio.file.Files/deleteIfExists temporary)))))

(deftest sh03-host-boundary-failures-are-structured-and-contained
  (doseq [[label error-factory expected-field]
          [[:stack #(StackOverflowError.) :bounded-sh03-reader-host-stack]
           [:assertion #(AssertionError. "assertion")
            :contained-sh03-reader-assertion]
           [:linkage #(LinkageError. "linkage")
            :contained-sh03-reader-linkage]
           [:diagnostic #(ex-info "inner" {:id "INNER"})
            :contained-sh03-reader-runtime-diagnostic]
           [:exception #(RuntimeException. "runtime")
            :contained-sh03-reader-host-failure]]]
    (let [data
          (with-redefs
            [bootstrap/p15-s23-stage2-runtime-execute-function
             (fn [& _] (throw (error-factory)))]
            (thrown-data
             #(bootstrap/sh03-reader-execute-plan!
               "contained.gravity" {} 'sh03-read-source-unit [])))]
      (is (= "C2-HASH" (:id data)) (name label))
      (is (some #{expected-field} (:missing-fields data)) (name label))
      (is (nil? (:unexpected-host-error data)) (name label)))))
