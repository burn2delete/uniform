(ns gravity.self-hosting.sh08-record-access-match-typing-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-records-unions-type-test]))

(defn- repository-root []
  (let [resource (io/resource "gravity/self_hosting/sh08_record_access_match_typing_test.clj")]
    (when-not resource
      (throw (ex-info "SH-08 test source is unavailable" {:id "SH08-RAM-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate) (throw (ex-info "Repository root unavailable" {:id "SH08-RAM-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root "bootstrap/clojure/fixtures/self-hosting/sh-08/record-access-match-typing")
(def ^:private c7-source "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private descriptor-definition 'closed-record-union-descriptor)
(def ^:private accepted-definition 'accepted-record-access-match-program)
(def ^:private extensions [".gravity" ".qst"])
(defn- path [relative] (str (.resolve @root relative)))
(defn- fixture-path [disposition basename extension]
  (path (str fixture-root "/" disposition "/" basename extension)))
(defn- old-helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-records-unions-type-test name)
      (throw (ex-info "Integrated descriptor helper unavailable" {:helper name}))))
(defn- invoke-c7 [function arguments] ((old-helper 'invoke-c7) function arguments))
(defn- fixture-artifact [disposition basename extension]
  (bootstrap/sh07-core-file-artifact (fixture-path disposition basename extension)))

(def ^:private artifacts
  (delay (into {} (for [extension extensions]
                    [extension {:accepted (fixture-artifact "accepted" "closed-record-access-match" extension)
                                :rejected (fixture-artifact "rejected" "closed-record-access-match-errors" extension)}]))))
(def ^:private verification-reports (atom {}))
(defn- b47-verification [artifact]
  (or (get @verification-reports artifact)
      (let [verifier (or (ns-resolve 'gravity.bootstrap 'sh07-core-artifact-verification)
                         (throw (ex-info "B47 verification unavailable" {})))
            report (verifier artifact)]
        (swap! verification-reports assoc artifact report)
        report)))
(defn- verification-preimage [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:domain :gravity/sh08-b47-verification-binding-v1
     :verified-artifact-id (:artifact-id artifact)
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :authenticated-wrapper artifact
     :canonical-core-artifact core
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-identity-preimage (:identity-preimage core)
     :authenticated-envelope-descriptor (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :verification-report report}))
(defn- artifact-extension [artifact]
  (let [source-path (get-in artifact [:provenance :source-path])]
    (cond (str/ends-with? source-path ".gravity") ".gravity"
          (str/ends-with? source-path ".qst") ".qst"
          :else (throw (ex-info "Unsupported fixture extension" {:source-path source-path})))))
(defn- coordinator-verification [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        source-path (get-in artifact [:provenance :source-path])
        preimage (verification-preimage artifact report)]
    {:artifact :gravity/sh07-b47-coordinator-verification-v16
     :schema-version 16 :boundary :clojure-coordinator-verifier
     :verified-artifact-id (:artifact-id artifact)
     :verified-identity-input (bootstrap/sh07-core-artifact-identity-input artifact)
     :verified-source-path source-path
     :verified-source-extension (artifact-extension artifact)
     :report report :check-catalog (set (keys (:checks report)))
     :opaque-provenance-binding-id (:provenance-binding-id core)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :authenticated-envelope-descriptor (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :verification-digest-resolution
     {:ordinal 0 :purpose :sh08-b47-verification-binding :preimage preimage
      :digest (bootstrap/p15-s23-c11-mir-digest preimage)}}))
(defn- b47-context [artifact report coordinator]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:artifact-id (:artifact-id artifact) :artifact-status (:status artifact)
     :artifact-kind (:kind artifact) :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (:identity-preimage core)
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-core-artifact core :provenance (:provenance artifact)
     :lineage (get-in boundary [:authenticated-core-request :lineage])
     :authenticated-wrapper artifact
     :verification {:status (:status report) :checks (:checks report)
                    :failed-checks (:failed-checks report)
                    :receipt-context :gravity/sh07-b47-verification-v16
                    :opaque-provenance-binding-id (:opaque-provenance-binding-id coordinator)
                    :verification-digest (get-in coordinator [:verification-digest-resolution :digest])}}))
(def ^:private request-cache (java.util.IdentityHashMap.))
(defn- authenticated-request [artifact]
  (locking request-cache
    (if (.containsKey request-cache artifact) (.get request-cache artifact)
        (let [report (b47-verification artifact)
              coordinator (coordinator-verification artifact report)
              context (b47-context artifact report coordinator)
              request {:canonical-core-artifact (:canonical-core-artifact context)
                       :b47-context context :coordinator-verification coordinator}]
          (when (= :passed (:status report)) (.put request-cache artifact request))
          request))))
(defn- descriptor-result [request]
  (invoke-c7 'sh08-record-union-type-core-artifact [request descriptor-definition]))
(defn- typing-result [request input-definition descriptor]
  (invoke-c7 'sh08-record-access-match-typed-core-artifact
             [request descriptor-definition input-definition descriptor]))
(defn- verification-result [request input-definition descriptor candidate]
  (invoke-c7 'sh08-verify-record-access-match-typing-result
             [request descriptor-definition input-definition descriptor candidate]))
(defn- sha256 [text]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (format "%064x" (BigInteger. 1 (.digest digest (.getBytes text "UTF-8"))))))
(defn- assert-node-link [actual-path value]
  (is (string? (:core-node-id value)))
  (is (string? (:syntax-id value)))
  (is (map? (:source-span value)))
  (is (string? actual-path)))

(deftest sh08-fixtures-and-final-c7-source-coverage-are-exact
  (doseq [[disposition basename] [["accepted" "closed-record-access-match"]
                                  ["rejected" "closed-record-access-match-errors"]]]
    (is (= (slurp (fixture-path disposition basename ".gravity"))
           (slurp (fixture-path disposition basename ".qst")))))
  (let [source-text (slurp (path c7-source))
        plan @(var-get (old-helper 'c7-plan))]
    ;; Refreshed only after the final owned C7 source is frozen.
    (is (= 284176 (count (.getBytes source-text "UTF-8"))))
    (is (= "e33a54b8cf202399a1f7dfae23221f653a8d43387566539fb026a54d2f44b275"
           (sha256 source-text)))
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
    (doseq [function '[sh08-record-access-match-typing-boundary-policy
                       sh08-record-access-match-typed-core-artifact
                       sh08-verify-record-access-match-typing-result]]
      (is (map? (get-in plan [:functions function])) function))))

(deftest sh08-policy-states-bounded-authority-and-residuals
  (let [policy (invoke-c7 'sh08-record-access-match-typing-boundary-policy [])]
    (is (= :self-hosting/sh08-record-access-match-typing-v1 (:invariant-family policy)))
    (is (= :self-hosting/sh08-records-unions (:descriptor-invariant policy)))
    (doseq [key [:node-linked-type-facts? :branch-spans?
                 :bounded-pattern-binding-environments? :bounded-constraint-ledger?
                 :overall-match-expression-type? :closed-record-construction-and-access-only?
                 :closed-union-match-narrowing-only? :executable-gravity-type-behavior?
                 :host-digest-authority-retained? :clojure-jvm-digest-authority-retained?]]
      (is (true? (get policy key)) key))
    (doseq [key [:general-pattern-compiler? :runtime-behavior?
                 :independent-gravity-cryptographic-verification?]]
      (is (false? (get policy key)) key))
    (doseq [key [:match-guards-and-effects :defaults-and-unreachable-branches
                 :general-pattern-binding-environments :complete-branch-provenance
                 :complete-constraint-ledger :ownership-and-linear-pattern-moves
                 :safe-classification]]
      (is (= :pending (get policy key)) key))))

(deftest sh08-both-source-extensions-produce-identical-node-linked-semantics
  (let [results (into {} (for [extension extensions
                               :let [artifact (get-in @artifacts [extension :accepted])
                                     request (authenticated-request artifact)
                                     descriptor (descriptor-result request)
                                     result (typing-result request accepted-definition descriptor)]]
                           [extension {:artifact artifact :request request
                                       :descriptor descriptor :result result}]))
        gravity-result (get-in results [".gravity" :result])
        qst-result (get-in results [".qst" :result])]
    (doseq [extension extensions
            :let [{:keys [artifact request descriptor result]} (get results extension)
                  actual-path (get-in artifact [:provenance :source-path])]]
      (testing extension
        (is (= :accepted (:status descriptor)))
        (is (= :accepted (:status result)))
        (is (= 2 (:schema-version result)))
        (is (= actual-path (:actual-source-path result)))
        (is (= extension (:source-extension result)))
        (is (= :authenticated-b47-core-provenance (:provenance-status result)))
        (is (= (:identity-input result) (:artifact-id-request result)))
        (is (= 2 (count (:record-construction-type-facts result))))
        (doseq [fact (:record-construction-type-facts result)] (assert-node-link actual-path fact))
        (assert-node-link actual-path (:record-access-type-fact result))
        (assert-node-link actual-path (:union-construction-type-fact result))
        (is (= [:some :none] (mapv :variant (:union-match-narrowing-facts result))))
        (is (= [:gravity.type/integer :gravity.type/nil]
               (mapv :narrowed-payload-type (:union-match-narrowing-facts result))))
        (is (= [:payload :empty-payload]
               (mapv #(get-in % [:bindings 0 :name])
                     (:pattern-binding-environments result))))
        (doseq [fact (:union-match-narrowing-facts result)] (assert-node-link actual-path fact))
        (is (= :gravity.type/string (get-in result [:match-expression-type-fact :expression-type])))
        (is (= "present" (get-in result [:match-expression-type-fact :selected-result])))
        (assert-node-link actual-path (:match-expression-type-fact result))
        (is (= :solved (get-in result [:constraint-ledger :status])))
        (is (= 7 (count (get-in result [:constraint-ledger :constraints]))))
        (doseq [constraint (get-in result [:constraint-ledger :constraints])]
          (assert-node-link actual-path constraint)
          (is (= :solved (:status constraint))))
        (is (= :passed (:status (verification-result request accepted-definition descriptor result))))))
    (is (not= (:actual-source-path gravity-result) (:actual-source-path qst-result)))
    (is (= (:sh07-shaped-artifact-id gravity-result) (:sh07-shaped-artifact-id qst-result)))
    (is (= (:identity-input gravity-result) (:identity-input qst-result)))
    (is (= (get-in gravity-result [:typed-core :types]) (get-in qst-result [:typed-core :types])))
    (is (= (mapv #(dissoc % :source-span) (:union-match-narrowing-facts gravity-result))
           (mapv #(dissoc % :source-span) (:union-match-narrowing-facts qst-result))))))

(def ^:private rejection-cases
  [['missing-field-program "C7-TYPE-MISMATCH" :record-missing-field]
   ['extra-field-program "C7-TYPE-MISMATCH" :record-extra-field]
   ['wrong-field-program "C7-TYPE-MISMATCH" :record-field-type-mismatch]
   ['invalid-access-field-program "C7-TYPE-MISMATCH" :record-access-invalid-field]
   ['invalid-variant-program "C7-TYPE-MISMATCH" :union-invalid-variant]
   ['nonexhaustive-match-program "C7-TYPE-MISMATCH" :union-match-nonexhaustive]
   ['contradictory-narrowing-program "C7-TYPE-MISMATCH" :union-match-contradictory-narrowing]
   ['branch-result-type-mismatch-program "C7-TYPE-MISMATCH" :union-match-branch-type-mismatch]
   ['malformed-record-program "C7-VERIFY" :record-construction-shape-invalid]
   ['malformed-union-program "C7-VERIFY" :union-construction-shape-invalid]
   ['invalid-branch-shape-program "C7-VERIFY" :union-match-branch-shape-invalid]
   ['duplicate-branch-program "C7-TYPE-MISMATCH" :union-match-unreachable-duplicate-variant]])
(deftest sh08-both-source-extensions-reject-the-full-shape-and-type-matrix
  (doseq [extension extensions
          :let [artifact (get-in @artifacts [extension :rejected])
                request (authenticated-request artifact)
                descriptor (descriptor-result request)
                actual-path (get-in artifact [:provenance :source-path])]
          [input-definition rule reason] rejection-cases]
    (testing (str extension " " input-definition)
      (let [result (typing-result request input-definition descriptor)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= rule (:rule diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= actual-path (:actual-source-path result) (:actual-source-path diagnostic)))
        (is (= extension (:source-extension result) (:source-extension diagnostic)))
        (is (= (:sh07-shaped-artifact-id result) (:core-artifact-id diagnostic)))
        (is (= (:core-node-id result) (:core-node-id diagnostic)))
        (is (= (:syntax-id result) (:syntax-id diagnostic)))
        (is (= (:source-span result) (:source-span diagnostic)))
        (is (map? (:provenance result)))
        (is (= (:provenance-status result) (:provenance-status diagnostic)))
        (assert-node-link actual-path diagnostic)))))

(deftest sh08-authentication-substitutions-preserve-best-actual-provenance
  (doseq [extension extensions
          :let [artifact (get-in @artifacts [extension :accepted])
                request (authenticated-request artifact)
                actual-path (get-in artifact [:provenance :source-path])
                opposite (if (= extension ".gravity") ".qst" ".gravity")
                cases [[:core (assoc-in request [:canonical-core-artifact :artifact-id] :substituted-core)
                        :b47-core-context-mismatch]
                       [:context (assoc-in request [:b47-context :artifact-status] :rejected)
                        :untrusted-b47-context]
                       [:digest (assoc-in request [:coordinator-verification :verification-digest-resolution :digest]
                                          :substituted-digest)
                        :untrusted-b47-coordinator-verification]
                       [:path (assoc-in request [:b47-context :provenance :source-path]
                                        (str actual-path ".substituted"))
                        :untrusted-b47-context]
                       [:extension (assoc-in request [:coordinator-verification :verified-source-extension] opposite)
                        :source-extension-path-mismatch]]]
          [label candidate-request reason] cases]
    (testing (str extension " " label)
      (let [descriptor (descriptor-result candidate-request)
            result (typing-result candidate-request accepted-definition descriptor)
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= "C7-VERIFY" (:rule diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (= actual-path (:actual-source-path result) (:actual-source-path diagnostic)))
        (is (string? (:core-node-id diagnostic)))
        (is (string? (:syntax-id diagnostic)))
        (is (map? (:source-span diagnostic)))))))

(deftest sh08-descriptor-and-result-substitutions-fail-closed-on-both-extensions
  (doseq [extension extensions
          :let [artifact (get-in @artifacts [extension :accepted])
                request (authenticated-request artifact)
                descriptor (descriptor-result request)
                result (typing-result request accepted-definition descriptor)
                descriptor-cases [[(assoc-in descriptor [:descriptor :record-type :name] :substituted-user)
                                   :descriptor-fact-substitution]
                                  [(assoc descriptor :provenance {})
                                   :descriptor-provenance-substitution]]
                result-cases [(assoc-in result [:record-construction-type-facts 0 :actual-type]
                                        :gravity.type/string)
                              (assoc-in result [:record-access-type-fact :result-type]
                                        :gravity.type/integer)
                              (assoc-in result [:union-construction-type-fact :variant] :none)
                              (assoc-in result [:union-match-narrowing-facts 0 :narrowed-payload-type]
                                        :gravity.type/string)
                              (assoc-in result [:match-expression-type-fact :expression-type]
                                        :gravity.type/integer)
                              (assoc-in result [:constraint-ledger :status] :unsolved)
                              (assoc result :identity-input {})
                              (assoc result :provenance {})
                              (assoc result :actual-source-path "substituted")]]]
    (doseq [[candidate reason] descriptor-cases]
      (let [rejected (typing-result request accepted-definition candidate)]
        (is (= :rejected (:status rejected)))
        (is (= reason (get-in rejected [:diagnostics 0 :reason])))))
    (doseq [candidate result-cases]
      (is (= :rejected (:status (verification-result request accepted-definition descriptor candidate)))))))

(deftest sh08-boundary-fails-closed-with-explicit-no-provenance-state
  (let [result (invoke-c7 'sh08-record-access-match-typed-core-artifact
                          [{} descriptor-definition accepted-definition {}])
        diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= "C7-VERIFY" (:rule diagnostic)))
    (is (= :authenticated-b47-request-required (:reason diagnostic)))
    (is (= :no-authenticated-provenance-available (:provenance-status result)))
    (is (nil? (:actual-source-path result)))))
