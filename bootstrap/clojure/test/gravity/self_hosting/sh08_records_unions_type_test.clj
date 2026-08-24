(ns gravity.self-hosting.sh08-records-unions-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_records_unions_type_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-08 records/unions test source is not on the classpath"
        {:id "SH08-RECORDS-UNIONS-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH08-RECORDS-UNIONS-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08/records-unions")
(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [disposition basename extension]
  (path (str fixture-root "/" disposition "/" basename extension)))

(defn- compile-plan
  []
  (let [source-path (path c7-source-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c7-plan (delay (compile-plan)))

(defn- invoke-c7
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-records-unions-type-model-leaf
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- fixture-artifact
  [disposition basename]
  (bootstrap/sh07-core-file-artifact
   (fixture-path disposition basename ".gravity")))

(def ^:private verification-reports (atom {}))

(defn- b47-verification
  [artifact]
  (or (get @verification-reports artifact)
      (let [verifier
            (or (ns-resolve 'gravity.bootstrap
                            'sh07-core-artifact-verification)
                (throw
                 (ex-info
                  "Required SH-07-B47 verification is absent"
                  {:id "SH08-RECORDS-UNIONS-B47-VERIFICATION-ABSENT"})))
            report (verifier artifact)]
        (swap! verification-reports assoc artifact report)
        report)))

(defn- verification-preimage
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:domain :gravity/sh08-b47-verification-binding-v1
     :verified-artifact-id (:artifact-id artifact)
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :authenticated-wrapper artifact
     :canonical-core-artifact core
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-identity-preimage (:identity-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :verification-report report}))

(defn- coordinator-verification
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        source-path (get-in artifact [:provenance :source-path])
        preimage (verification-preimage artifact report)]
    {:artifact :gravity/sh07-b47-coordinator-verification-v16
     :schema-version 16
     :boundary :clojure-coordinator-verifier
     :verified-artifact-id (:artifact-id artifact)
     :verified-identity-input
     (bootstrap/sh07-core-artifact-identity-input artifact)
     :verified-source-path source-path
     :verified-source-extension ".gravity"
     :report report
     :check-catalog (set (keys (:checks report)))
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :verification-digest-resolution
     {:ordinal 0
      :purpose :sh08-b47-verification-binding
      :preimage preimage
      :digest (bootstrap/p15-s23-c11-mir-digest preimage)}}))

(defn- b47-context
  [artifact report coordinator]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:artifact-id (:artifact-id artifact)
     :artifact-status (:status artifact)
     :artifact-kind (:kind artifact)
     :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (:identity-preimage core)
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-core-artifact core
     :provenance (:provenance artifact)
     :lineage (get-in boundary [:authenticated-core-request :lineage])
     :authenticated-wrapper artifact
     :verification
     {:status (:status report)
      :checks (:checks report)
      :failed-checks (:failed-checks report)
      :receipt-context :gravity/sh07-b47-verification-v16
      :opaque-provenance-binding-id
      (:opaque-provenance-binding-id coordinator)
      :verification-digest
      (get-in coordinator [:verification-digest-resolution :digest])}}))

(def ^:private request-cache (java.util.IdentityHashMap.))

(defn- authenticated-request
  [artifact]
  (locking request-cache
    (if (.containsKey request-cache artifact)
      (.get request-cache artifact)
      (let [report (b47-verification artifact)
            coordinator (coordinator-verification artifact report)
            context (b47-context artifact report coordinator)
            request
            {:canonical-core-artifact (:canonical-core-artifact context)
             :b47-context context
             :coordinator-verification coordinator}]
        (when (= :passed (:status report))
          (.put request-cache artifact request))
        request))))

(def ^:private accepted-artifact
  (delay (fixture-artifact "accepted" "closed-record-union-family")))
(def ^:private rejected-artifact
  (delay (fixture-artifact "rejected" "closed-record-union-errors")))

(def ^:private bounded-descriptor
  {:record-type
   {:name :user
    :fields [{:name :id :type :gravity.type/integer}
             {:name :name :type :gravity.type/string}]}
   :record-construction
   {:type :user
    :fields [{:name :id :value 7}
             {:name :name :value "Ada"}]}
   :record-access {:record-type :user :field :name}
   :union-type
   {:name :maybe-integer
    :variants [{:tag :some :payload-type :gravity.type/integer}
               {:tag :none :payload-type :gravity.type/nil}]}
   :union-construction
   {:type :maybe-integer :variant :some :payload 7}
   :union-match
   {:union-type :maybe-integer
    :branches [{:variant :some :result 1}
               {:variant :none :result 0}]}})

(defn- type-result
  [artifact definition-name]
  (invoke-c7 'sh08-record-union-type-core-artifact
             [(authenticated-request artifact) definition-name]))

(defn- verification-result
  [artifact definition-name candidate]
  (invoke-c7 'sh08-verify-record-union-type-result
             [(authenticated-request artifact) definition-name candidate]))

(deftest sh08-records-unions-fixtures-and-c7-exports-are-executable
  (doseq [[disposition basename]
          [["accepted" "closed-record-union-family"]
           ["rejected" "closed-record-union-errors"]]]
    (is (= (slurp (fixture-path disposition basename ".gravity"))
           (slurp (fixture-path disposition basename ".qst")))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c7-plan)))
  (doseq [function
          '[sh08-record-union-type-boundary-policy
            sh08-record-union-type-core-artifact
            sh08-verify-record-union-type-result]]
    (is (map? (get-in @c7-plan [:functions function])) function)))

(deftest sh08-records-unions-bounded-descriptor-schema-is-executable
  (is (true?
       (invoke-c7 'sh08-ru-supported-type?
                  [:gravity.type/integer])))
  (is (true?
       (invoke-c7 'sh08-ru-all-field-schemas-valid?
                  [(get-in bounded-descriptor [:record-type :fields])])))
  (is (true?
       (invoke-c7 'sh08-ru-all-field-values-valid?
                  [(get-in bounded-descriptor
                           [:record-construction :fields])])))
  (is (true?
       (invoke-c7 'sh08-ru-all-variant-schemas-valid?
                  [(get-in bounded-descriptor [:union-type :variants])])))
  (is (true?
       (invoke-c7 'sh08-ru-all-match-branches-valid?
                  [(get-in bounded-descriptor [:union-match :branches])])))
  (is (true?
       (invoke-c7 'sh08-ru-family-shape? [bounded-descriptor]))))

(deftest sh08-records-unions-boundary-fails-closed
  (let [policy (invoke-c7 'sh08-record-union-type-boundary-policy [])
        result
        (invoke-c7 'sh08-record-union-type-core-artifact
                   [{} 'accepted-record-union-family])
        diagnostic (first (:diagnostics result))]
    (is (= :named-map-definition-in-authenticated-checked-core
           (:descriptor-source policy)))
    (is (true? (:closed-family-only? policy)))
    (is (true? (:preparatory-source-model? policy)))
    (is (false? (:roadmap-credit? policy)))
    (is (false? (:executable-record-union-language-behavior? policy)))
    (is (false? (:nominal-sh07-carrier? policy)))
    (is (false?
         (:independent-gravity-cryptographic-verification? policy)))
    (is (= :rejected (:status result)))
    (is (= "C7-VERIFY" (:rule diagnostic)))
    (is (= :authenticated-b47-request-required (:reason diagnostic)))))

(deftest sh08-records-unions-accepted-descriptor-model-is-source-bound
  (let [artifact @accepted-artifact
        result (type-result artifact 'accepted-record-union-family)
        verification
        (verification-result artifact 'accepted-record-union-family result)]
    (is (= :accepted (:status result)))
    (is (= :gravity/sh08-record-union-preparatory-model
           (:artifact result)))
    (is (= :authenticated-closed-record-union-descriptor-model
           (:scope result)))
    (is (true? (:preparatory-source-model? result)))
    (is (false? (:roadmap-credit? result)))
    (is (false? (:executable-record-union-language-behavior? result)))
    (is (false? (:nominal-sh07-carrier? result)))
    (is (= :host-resolved-b47-verification-boundary
           (:authentication-status result)))
    (is (= :user (get-in result [:descriptor :record-type :name])))
    (is (= :name (get-in result [:descriptor :record-access :field])))
    (is (= :some
           (get-in result [:descriptor :union-construction :variant])))
    (is (= [:some :none]
           (mapv :variant
                 (get-in result [:descriptor :union-match :branches]))))
    (is (not (contains? result :typed-core)))
    (is (not (contains? result :construction-type-facts)))
    (is (not (contains? result :access-type-facts)))
    (is (not (contains? result :match-type-facts)))
    (is (not (contains? result :constraint-ledger)))
    (is (= (:identity-input result) (:artifact-id-request result)))
    (is (every? (set (:pending result))
                [:general-record-and-union-surface-syntax
                 :constructor-pattern-lowering
                 :record-construction-typing
                 :record-access-typing
                 :union-construction-typing
                 :union-match-typing-and-narrowing
                 :uniform-gravity-and-qst-execution-gates
                 :sh07-completion
                 :sh08-completion]))
    (is (= :passed (:status verification)))))

(deftest sh08-records-unions-descriptor-rejections-are-stable
  (doseq [[definition-name reason]
          [['missing-field-family :record-missing-field]
           ['wrong-field-type-family :record-field-type-mismatch]
           ['invalid-variant-family :union-invalid-variant]
           ['nonexhaustive-match-family :union-match-nonexhaustive]]]
    (testing (name definition-name)
      (let [result (type-result @rejected-artifact definition-name)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= :authenticated-closed-record-union-descriptor-model
               (:scope result)))
        (is (= "C7-VERIFY" (:rule diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :type-checking (:stage diagnostic)))
        (is (= :meta (:profile diagnostic)))
        (is (= :jvm (:target diagnostic)))
        (is (map? (:source-span diagnostic)))
        (is (= :coordinator-digest-required (:diagnostic-id diagnostic)))
        (is (= reason
               (get-in diagnostic [:diagnostic-id-request :reason])))
        (is (= (:expected-type diagnostic)
               (get-in diagnostic [:diagnostic-id-request :expected])))
        (is (= (:actual-type diagnostic)
               (get-in diagnostic [:diagnostic-id-request :actual])))
        (is (map? (:remediation diagnostic)))))))

(deftest sh08-records-unions-result-verification-detects-mutation
  (let [artifact @accepted-artifact
        result (type-result artifact 'accepted-record-union-family)
        mutated (assoc-in result [:descriptor :record-access :field] :id)]
    (is (= :rejected
           (:status
            (verification-result
             artifact 'accepted-record-union-family mutated))))))
