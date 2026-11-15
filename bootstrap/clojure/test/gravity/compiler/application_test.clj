(ns gravity.compiler.application-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.compiler.application :as application])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]
           [java.security MessageDigest]))

;; These are fresh caller source programs, not accepted Core/MIR fixtures or
;; imports from the SH12/SH13 test helpers. C2-C11 and SAFE9 remain the owners.
(def ^:private add-policy
  {:profile :hosted :target :jvm :effects #{:error/raise}
   :capabilities #{} :safety :safe})

(def ^:private bool-policy (assoc add-policy :effects #{}))

(defn- source-text [module parameters body effects]
  (str "(ns " module "\n"
       "  (:profile :hosted)\n  (:target :jvm)\n"
       "  (:effects " (pr-str effects) ")\n"
       "  (:capabilities #{})\n  (:safety :safe)\n  (:exports [main]))\n\n"
       "(defn main [" parameters "]\n  " body ")\n"))

(def ^:private add-source
  (source-text 'compiler.integration.add "left :- I64 right :- I64"
               "(+ left right)" #{:error/raise}))

(defn- with-source-file [filename text operation]
  (let [directory (Files/createTempDirectory
                   "uniform-application-" (make-array FileAttribute 0))
        path (.resolve directory filename)]
    (try
      (spit (.toFile path) text :encoding "UTF-8")
      (operation (.getCanonicalPath (.toFile path)))
      (finally
        (Files/deleteIfExists path)
        (Files/deleteIfExists directory)))))

(defn- sha256 [^bytes bytes]
  (str "sha256:"
       (apply str
              (map #(format "%02x" (bit-and 255 %))
                   (.digest (MessageDigest/getInstance "SHA-256") bytes)))))

(defn- content-id? [value]
  (and (string? value) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- diagnostic-source [data]
  (or (get-in data [:source-span :source])
      (get-in data [:source-span :file])
      (get-in data [:primary :span :source])
      (get-in data [:primary :span :file])
      (:source-path data)))

(defn- record-rejection [label data]
  (println "current-source-to-verified-mir rejection"
           (pr-str {:case label :id (:id data) :missing-fact (:missing-fact data)
                    :source (diagnostic-source data)})))

(defn- assert-source-result [result path text module policy & [c11-functions]]
  (let [bytes (.getBytes ^String text StandardCharsets/UTF_8)
        expected-hash (sha256 bytes)
        core (:checked-core result)
        mir (:mir-module result)
        report (:mir-verification result)
        receipt (:source-policy-receipt result)
        parameter? (seq (get-in (peek (:core-nodes core)) [:type :params]))
        [mir-builder mir-verifier]
        (or c11-functions '[sh12-build-branch-join-mir sh12-verify-branch-join-mir])]
    (is (= :accepted (:status result)))
    (is (= {:path path :content-hash expected-hash :byte-count (alength bytes)}
           (:source result)))
    (is (content-id? (:artifact-id result)))
    (is (= expected-hash (:source-content-hash core)
           (get-in core [:authenticated-input :source-content-hash])
           (get-in core [:semantic-provenance :source-content-hash])
           (get-in core [:physical-provenance :source-content-hash])))
    (is (= path (get-in core [:physical-provenance :actual-paths :source])))
    (is (= module (get-in core [:source-core-input :module]) (:module-id mir)))
    (is (= :jvm (:source-target core)
           (get-in core [:target-request-metadata :source-target])
           (get-in core [:target-request-metadata :requested-target])))
    (is (= :hosted (:profile core) (:profile mir)))
    (is (= {:implementation :clojure-stage0-seed :target :jvm :self-hosted? false}
           (:compiler-host result)))
    (is (= :passed (get-in result [:core-verification :status]) (:status report)))
    (is (= :gravity-source (get-in result [:core-verification :semantic-authority])))
    (is (= (:artifact-id core) (:source-core mir) (:checked-core-artifact-id report)))
    (is (= (:module-id mir) (:mir-module-id report)))
    (is (= [] (:diagnostics core) (:diagnostics mir) (:diagnostics report)))
    (is (= {:interface :gravity/compiler-application :version 1}
           (:source-interface receipt)))
    (is (= policy (:policy receipt)))
    (is (= :passed (:envelope-verification receipt)))
    (is (= :development-unadmitted (:admission-status receipt)))
    (is (= :selected-current-source-host-seed (:verification-scope receipt)))
    (is (false? (:self-hosted? receipt)))
    (is (= #{:reader :syntax :expansion :resolution :expression :checked-core}
           (set (keys (:owner-sources receipt)))))
    (doseq [[owner binding] (:owner-sources receipt)]
      (let [owner-bytes (Files/readAllBytes (.toPath (io/file (:source-path binding))))]
        (is (= (sha256 owner-bytes) (:source-content-hash binding)) (str owner))
        (is (= (alength owner-bytes) (:source-byte-count binding)) (str owner))))
    (is (= (get-in receipt [:owner-sources :reader :source-content-hash])
           (get-in receipt [:reader-plan-binding :source-content-hash])
           (get-in receipt [:reader-source-revision :source-content-hash])))
    (is (= (get-in receipt [:owner-sources :syntax :source-content-hash])
           (get-in receipt [:syntax-owner :binding :source-content-hash])
           (get-in receipt [:syntax-owner :source-revision :source-content-hash])))
    (is (= (get-in receipt [:owner-sources :expression :source-content-hash])
           (get-in receipt [:expression-binding :source-content-hash])))
    (is (content-id? (get-in receipt [:syntax-owner :artifact-id])))
    (is (= :accepted (get-in receipt [:syntax-owner :envelope :status])))
    (doseq [key [:semantic-envelope-id :provenance-binding-id]]
      (is (content-id? (get-in receipt [:syntax-owner :envelope key])) (str key)))
    (if parameter?
      (do
        (doseq [key [:sh06-artifact-id :sh06-semantic-projection-id :projection-binding
                     :root-syntax-id :function-syntax-id :function-scope-id]]
          (is (content-id? (get-in receipt [:parameter-resolution key])) (str key)))
        (is (false? (get-in receipt [:expression-binding :invoked?]))))
      (do
        (is (= {} (:parameter-resolution receipt)))
        (is (true? (get-in receipt [:expression-binding :invoked?])))))
    (is (false? (:self-hosted? core)))
    (is (true? (:clojure-seed-boundary? core)))
    (doseq [[owner builder verifier]
            [[:c6-c10 'c6-c10-build-checked-core-template 'c6-c10-verify-checked-core-template]
             [:c11 mir-builder mir-verifier]]]
      (let [binding (get-in result [:compiler-bindings owner])
            owner-bytes (Files/readAllBytes (.toPath (io/file (:source-path binding))))]
        (is (= builder (:builder-function binding)))
        (is (= verifier (:verifier-function binding)))
        (is (= (sha256 owner-bytes) (:source-content-hash binding)))
        (is (= (alength owner-bytes) (:source-byte-count binding)))
        (doseq [key [:plan-semantic-hash :functions-semantic-hash
                     :builder-semantic-hash :verifier-semantic-hash]]
          (is (content-id? (get binding key)) (str owner " " key)))))
    (println "current-source-to-verified-mir"
             (pr-str {:source expected-hash :path path :module module
                      :artifact (:artifact-id result) :checked-core (:artifact-id core)
                      :mir-module (:module-id mir) :source-target (:source-target core)
                      :source-policy (:owner-metadata-content-id receipt)
                      :syntax (get-in receipt [:syntax-owner :artifact-id])
                      :resolution (get-in receipt [:parameter-resolution :sh06-artifact-id])
                      :mir-report-scope (:scope report)}))))

(deftest current-source-to-verified-mir
  (let [compiler-identities (atom nil)]
   (doseq [[filename module parameters body names operand-order]
          [["add.gravity" 'compiler.integration.add "left :- I64 right :- I64"
            "(+ left right)" '[left right] [0 1]]
           ["renamed-sum.qst" 'example.renamed.sum "augend :- I64 addend :- I64"
            "(+ addend augend)" '[augend addend] [1 0]]]]
    (testing filename
      (let [text (source-text module parameters body #{:error/raise})]
        (with-source-file filename text
          (fn [path]
            (let [result (if (= filename "add.gravity")
                           (application/current-source-to-verified-mir path add-policy)
                           (binding [*print-length* 1 *print-level* 1 *print-meta* true
                                     *print-dup* true *print-readably* false
                                     *print-namespace-maps* true]
                             (application/current-source-to-verified-mir path add-policy)))
                  core (:checked-core result) mir (:mir-module result)
                  nodes (:core-nodes core) ids (mapv :node-id nodes)
                  add (nth nodes 4) root (peek nodes)
                  function (get-in mir [:functions 'main])
                  operations (get-in function [:blocks (:entry function) :instructions])
                  guard (nth operations 2) token (:result guard)
                  identities (into {}
                                   (map (fn [[owner value]]
                                          [owner (select-keys value
                                                   [:source-content-hash :plan-semantic-hash
                                                    :functions-semantic-hash
                                                    :builder-semantic-hash :verifier-semantic-hash])]))
                                   (:compiler-bindings result))]
              (assert-source-result result path text module add-policy)
              (if-let [previous @compiler-identities]
                (is (= previous identities)
                    "Caller print bindings and source rename cannot change compiler identities")
                (reset! compiler-identities identities))
              (is (= [:parameter :parameter :local :local :integer-add :function]
                     (mapv :source-operation nodes)))
              (is (= names (get-in root [:attributes :params])))
              (is (= (mapv #(vector (get ids %)) operand-order)
                     (mapv :operands (subvec nodes 2 4))))
              (is (= [:gravity/i64 :gravity/i64] (get-in root [:type :params])))
              (is (= :gravity/i64 (:type add) (get-in root [:type :return]) (:returns function)))
              (is (= #{:error/raise} (get-in root [:type :latent-effects])
                     (:latent-effects function)
                     (get-in core [:effect-facts (:node-id root) :residual])))
              (is (= #{:error/numeric} (get-in root [:type :throws])))
              (is (every? #(= :persistent-immutable-value (:model %)) (vals (:ownership-facts core))))
              (is (= :runtime-checked (get-in add [:safety :outcome])
                     (get-in core [:safety-facts (:node-id add) :outcome])))
              (is (= [64 :signed :checked :error/numeric #{:error/raise}]
                     ((juxt :bit-width :signedness :numeric-mode :failure :effects-introduced)
                      (get-in add [:safety :check]))))
              (is (= [:local :local :runtime-check :integer-add :function-boundary]
                     (mapv :opcode operations)))
              (is (= (subvec ids 0 2) (:params function)))
              (is (= (subvec ids 2 4) (:operands guard)))
              (is (= [token (nth ids 2) (nth ids 3)] (:operands (nth operations 3))))
              (is (= 1 (count (:runtime-check-table mir))))
              (is (= (:op-id guard)
                     (get-in mir [:data-flow-graph :definitions token :operation-id]))))))))))
  (let [text (source-text 'example.boolean.choice "enabled :- Bool"
                          "(if enabled true false)" #{})]
    (with-source-file "choice.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path bool-policy)
              core (:checked-core result) mir (:mir-module result)
              root (peek (:core-nodes core)) function (get-in mir [:functions 'main])]
          (assert-source-result result path text 'example.boolean.choice bool-policy)
          (is (= [:gravity/bool] (get-in root [:type :params])))
          (is (= :gravity/bool (get-in root [:type :return]) (:returns function)))
          (is (= #{} (get-in root [:type :latent-effects]) (:latent-effects function)))
          (is (= '[enabled] (get-in root [:attributes :params])))
          (is (= 4 (count (:blocks function))))
          (is (empty? (:runtime-check-table mir))))))))

(deftest current-source-flat-boolean-to-verified-mir
  (let [module 'compiler.integration.boolean.sequence
        text (source-text module "" "(do true false)" #{})]
    (with-source-file "boolean-sequence.gravity" text
      (fn [path]
        (let [result (application/current-source-to-verified-mir path bool-policy)
              core (:checked-core result) mir (:mir-module result)
              root (peek (:core-nodes core)) function (get-in mir [:functions 'main])
              operations (get-in function [:blocks (:entry function) :instructions])]
          (assert-source-result result path text module bool-policy
                                '[sh12-build-flat-bool-sequence-mir
                                  sh12-verify-flat-bool-sequence-mir])
          (is (= [:literal :literal :do :function] (mapv :source-operation (:core-nodes core))))
          (is (= [] (get-in root [:type :params]) (:params function)))
          (is (= :gravity/bool (get-in root [:type :return]) (:returns function)))
          (is (= #{} (get-in root [:type :latent-effects]) (:latent-effects function)))
          (is (= 1 (count (:blocks function))))
          (is (= [:constant :constant :sequence :function-boundary] (mapv :opcode operations)))
          (is (= [true false] (mapv #(get-in % [:constant-payload :value]) (take 2 operations))))
          (is (every? #(= :proven-safe (:outcome %)) (vals (:safety-facts core))))
          (is (empty? (:runtime-check-table mir)))
          (is (= :closed-flat-bool-literal-sequence (get-in result [:mir-verification :scope])))
          (is (some #{:same-block-definitions-uses-and-return-dominance}
                    (get-in result [:mir-verification :checks]))))))))

(deftest current-source-rejects-invalid-programs
  (doseq [[label text policy expected-rule]
          [["malformed" (str add-source "[") add-policy "C2-DELIMITER"]
           ["operand-type" (str/replace add-source "(+ left right)" "(+ left true)")
            add-policy "C6-LOWERING-GAP"]
           ["arity" (str/replace add-source "(+ left right)" "(+ left)")
            add-policy "C6-LOWERING-GAP"]
           ["undeclared-raise" (str/replace add-source "#{:error/raise}" "#{}")
            bool-policy "C8-UNDECLARED"]]]
    (testing label
      (with-source-file (str label ".gravity") text
        (fn [path]
          (let [data (failure #(application/current-source-to-verified-mir path policy))]
            (record-rejection label data)
            (is (= expected-rule (:id data)) (pr-str data))
            (is (= path (diagnostic-source data)) (pr-str data))
            (when (= label "undeclared-raise")
              (is (= :declared-numeric-error-effect (:missing-fact data))))))))))

(deftest current-source-rejects-supplied-authority
  (with-source-file "explicit-policy.gravity" add-source
    (fn [path]
      (doseq [[label policy]
              (concat [["missing-policy" nil] ["empty-policy" {}]
                       ["missing-policy-effects" (dissoc add-policy :effects)]
                       ["unsupported-js-target" (assoc add-policy :target :js)]]
                      (for [key [:checked-core :mir-module :verifier-report :binding :token]]
                        [(str "supplied-" (name key))
                         (assoc add-policy key {:status :accepted})]))]
        (let [data (failure #(application/current-source-to-verified-mir path policy))]
          (record-rejection label data)
          (is (some? data) (pr-str policy))
          (is (string? (:id data)) (pr-str data))))
      (let [data (failure #(application/current-source-to-verified-mir
                            {:source-path path :checked-core {:status :accepted}} add-policy))]
        (record-rejection "supplied-source-artifact" data)
        (is (some? data))
        (is (string? (:id data)))))))

(defn- with-mutated-runtime-result [function-name transform operation]
  (let [original bootstrap/p15-s23-stage2-runtime-execute-function
        changed? (atom false)]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [context plan function arguments]
                    (let [result (original context plan function arguments)]
                      (if (and (= function-name function)
                               (compare-and-set! changed? false true))
                        (transform result)
                        result)))]
      (let [data (failure operation)]
        (is @changed? (str "The original " function-name " result must be reached"))
        (is (some? data) (str "Mutated " function-name " result must be rejected"))
        (is (string? (:id data)) (pr-str data))
        data))))

(deftest current-source-rejects-tampered-handoffs
  (with-source-file "tamper.gravity" add-source
    (fn [path]
      (doseq [[label function transform]
              [["Core type fact" 'c6-c10-build-checked-core-template
                (fn [result]
                  (let [id (get-in result [:artifact-template :core-nodes 4 :node-id])]
                    (assoc-in result [:artifact-template :type-facts id :type] :gravity/bool)))]
               ["Core report" 'c6-c10-verify-checked-core-template
                #(assoc % :checks [])]
               ["MIR guard" 'sh12-build-branch-join-mir
                #(assoc % :runtime-check-table {})]
               ["MIR report" 'sh12-verify-branch-join-mir
                #(assoc % :checked-core-artifact-id (str "sha256:" (apply str (repeat 64 "0"))))]]]
        (testing label
          (let [data (with-mutated-runtime-result function transform
                       #(application/current-source-to-verified-mir path add-policy))]
            (record-rejection label data)
            (is (= path (diagnostic-source data)) (pr-str data))))))))

(deftest current-source-rejects-source-and-binding-substitution
  (with-source-file "substitution.gravity" add-source
    (fn [path]
      (let [original bootstrap/p15-s23-c6c10-private-builder-envelope]
        (doseq [kind [:source :binding :file-bytes]]
          (testing kind
            (spit path add-source :encoding "UTF-8")
            (let [changed? (atom false)
                  data
                  (with-redefs [bootstrap/p15-s23-c6c10-private-builder-envelope
                                (fn [request-source ingress binding]
                                  (let [envelope (original request-source ingress binding)]
                                    (if (compare-and-set! changed? false true)
                                      (case kind
                                        :source (assoc envelope :source-content-hash
                                                       (str "sha256:" (apply str (repeat 64 "0"))))
                                        :binding (assoc-in envelope [:binding-pins :builder-source-hash]
                                                           (str "sha256:" (apply str (repeat 64 "0"))))
                                        :file-bytes (do (spit path (str/replace add-source "left" "renamed")
                                                              :encoding "UTF-8")
                                                        envelope))
                                      envelope)))]
                    (failure #(application/current-source-to-verified-mir path add-policy)))]
              (record-rejection (str "substitution-" (name kind)) data)
              (is @changed? "The genuine source envelope must be constructed first")
              (is (some? data) (str kind " substitution must be rejected"))
              (is (string? (:id data)) (pr-str data))
              (is (= path (diagnostic-source data)) (pr-str data)))))))))

(deftest current-source-keeps-the-increment-scope
  (doseq [[label text]
          [["subtraction" (str/replace add-source "(+ left right)" "(- left right)")]
           ["multiplication" (str/replace add-source "(+ left right)" "(* left right)")]
           ["multiple-functions" (str add-source "\n(defn extra [] true)\n")]]]
    (testing label
      (with-source-file (str label ".gravity") text
        (fn [path]
          (let [data (failure #(application/current-source-to-verified-mir path add-policy))]
            (record-rejection label data)
            (is (some? data) (str label " is outside this increment"))
            (is (string? (:id data)) (pr-str data))))))))
