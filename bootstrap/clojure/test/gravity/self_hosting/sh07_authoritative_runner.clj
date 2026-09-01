(ns gravity.self-hosting.sh07-authoritative-runner
  "Runs cache-free SH-07 authoritative module acceptance and public replay."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [gravity.bootstrap :as bootstrap])
  (:import (java.nio.file Files LinkOption Path Paths)))

(def ^:private contract-resource
  "gravity/self_hosting/sh07_proof_contract.edn")

(defn- proof-contract-snapshot
  []
  (let [resource (io/resource contract-resource)]
    (when-not resource
      (throw
       (ex-info "SH-07 proof contract is absent"
                {:id "SH07-PROOF-CONTRACT-ABSENT"
                 :resource contract-resource})))
    (let [bytes (with-open [input (io/input-stream resource)]
                  (.readAllBytes input))
          digest (java.security.MessageDigest/getInstance "SHA-256")]
      (.update digest bytes)
      {:contract (edn/read-string (String. bytes "UTF-8"))
       :sha256
       (str "sha256:"
            (apply str (map #(format "%02x" (bit-and % 0xff))
                            (.digest digest))))})))

(defn- proof-contract [] (:contract (proof-contract-snapshot)))

(defn- modules
  [contract]
  (into
   (sorted-map)
   (map (fn [[module-name path]]
          [(name module-name) path]))
   (:authoritative-modules contract)))

(defn module-names
  []
  (vec (keys (modules (proof-contract)))))

(defn- module-catalog-for
  "Returns the deterministic module-to-source catalog used by --fresh.

  The tab-delimited CLI representation is intentionally narrow so external
  checkpoint tooling can validate paths without attempting to parse EDN."
  [contract]
  (let [catalog (modules contract)
        invalid
        (vec
         (for [[module path] catalog
               :when (or (not (re-matches #"[A-Za-z0-9][A-Za-z0-9._-]*"
                                          module))
                         (not (string? path))
                         (string/blank? path)
                         (string/includes? path "\t")
                         (string/includes? path "\n"))]
           [module path]))
        duplicate-paths
        (->> catalog vals frequencies
             (keep (fn [[path count]] (when (< 1 count) path)))
             sort vec)]
    (when (or (seq invalid) (seq duplicate-paths))
      (throw
       (ex-info "SH-07 authoritative module catalog is malformed"
                {:id "SH07-AUTHORITATIVE-CATALOG"
                 :invalid invalid
                 :duplicate-paths duplicate-paths})))
    catalog))

(defn module-catalog []
  (module-catalog-for (proof-contract)))

(defn- valid-source-binding?
  [binding]
  (and (map? binding)
       (= #{:source-byte-count :source-bytes-sha256} (set (keys binding)))
       (integer? (:source-byte-count binding))
       (not (neg? (:source-byte-count binding)))
       (string? (:source-bytes-sha256 binding))
       (boolean
        (re-matches #"sha256:[0-9a-f]{64}" (:source-bytes-sha256 binding)))))

(declare source-bytes-sha256)

(defn- validate-source-binding!
  [contract module-name actual]
  (let [expected
        (get-in contract
                [:authoritative-coverage-census :module-expectations
                 (keyword module-name)])]
    (when expected
      (let [binding (:source-binding expected)]
        (when-not (and (valid-source-binding? binding)
                       (= binding
                          {:source-byte-count (:byte-count actual)
                           :source-bytes-sha256 (:sha256 actual)}))
          (throw
           (ex-info "SH-07 authoritative source differs from its census contract"
                    {:id "SH07-AUTHORITATIVE-SOURCE-MISMATCH"
                     :module module-name
                     :expected binding
                     :actual {:source-byte-count (:byte-count actual)
                              :source-bytes-sha256 (:sha256 actual)}})))))))

(defn- module-source-contracts-for
  "Returns exact source bindings for modules with authoritative census expectations."
  [contract]
  (let [catalog (module-catalog-for contract)
        expectations
        (get-in contract [:authoritative-coverage-census :module-expectations])]
    (when-not (map? expectations)
      (throw (ex-info "SH-07 census module expectations are malformed"
                      {:id "SH07-AUTHORITATIVE-SOURCE-CONTRACT"})))
    (into
     (sorted-map)
     (for [[module expectation] expectations
           :let [module-name (name module)
                 path (get catalog module-name)
                 binding (:source-binding expectation)]]
       (do
         (when-not (and path (valid-source-binding? binding))
           (throw
            (ex-info "SH-07 authoritative source contract is malformed"
                     {:id "SH07-AUTHORITATIVE-SOURCE-CONTRACT"
                      :module module-name
                      :path path
                      :source-binding binding})))
         [module-name {:source-path path :source-binding binding}])))))

(defn module-source-contracts []
  (module-source-contracts-for (proof-contract)))

(def coverage-census-policies
  #{:exact-precommitted :source-bound-derived})

(def derived-census-unsupported-claims
  [:exact-authentic-coverage :aggregate :release])

(defn coverage-census-policy
  "Returns the explicitly duplicated top-level and census policy marker."
  [contract]
  (let [top-level (:coverage-census-policy contract)
        nested (get-in contract [:authoritative-coverage-census :policy])]
    (when (= top-level nested) top-level)))

(defn- valid-census-policy-contract?
  [contract]
  (let [policy (coverage-census-policy contract)
        census (:authoritative-coverage-census contract)
        claims (:authority-claims contract)
        expectations (:module-expectations census)]
    (and (= 2 (:schema-version contract))
         (contains? coverage-census-policies policy)
         (map? census)
         (= 2 (:schema-version census))
         (= policy (:policy census))
         (map? expectations)
         (every?
          (fn [[_ expectation]]
            (and (map? expectation)
                 (valid-source-binding? (:source-binding expectation))
                 (symbol? (:module-namespace expectation))
                 (if (= :exact-precommitted policy)
                   (and (map? (:request-counts expectation))
                        (map? (:core-counts expectation)))
                   (and (not (contains? expectation :request-counts))
                        (not (contains? expectation :core-counts))))))
          expectations)
         (= (= :source-bound-derived policy)
            (= false (:counts-precommitted? census))
            (= false (:independent-count-oracle? census)))
         (if (= :source-bound-derived policy)
           (and (= derived-census-unsupported-claims
                  (:unsupported-claims census)
                  (get claims :unsupported-claims))
                (= false (get claims :counts-precommitted?))
                (= false (get claims :independent-count-oracle?))
                (= false (get claims :aggregate-authoritative?))
                (= false (get claims :release-authoritative?))
                (= true (get claims :attestation-required))
                (= :gravity/sh07-source-bound-attestation-v1
                   (get claims :attestation-schema)))
           (and (true? (:counts-precommitted? census))
                (true? (:independent-count-oracle? census))
                (= true (get claims :counts-precommitted?))
                (= true (get claims :independent-count-oracle?))
                (= [] (get claims :unsupported-claims))
                (= false (get claims :aggregate-authoritative?))
                (= false (get claims :release-authoritative?))
                (or (not (contains? claims :attestation-required))
                    (= false (get claims :attestation-required))))))))

(defn validate-coverage-census-contract!
  "Rejects an ambiguous policy before any expensive proof transaction starts."
  [contract]
  (when-not (valid-census-policy-contract? contract)
    (throw
     (ex-info "SH-07 coverage census policy is malformed or ambiguous"
              {:id "SH07-COVERAGE-CENSUS-POLICY"
               :policy (coverage-census-policy contract)})))
  contract)

(defn- validate-selected-source-contracts!
  [contract selected]
  (let [contracts (module-source-contracts-for contract)
        policy (coverage-census-policy contract)]
    (doseq [module-name selected
            :let [expectation
                  (get-in contract
                          [:authoritative-coverage-census :module-expectations
                           (keyword module-name)])]]
      (when (and (= :source-bound-derived policy) (nil? expectation))
        (throw
         (ex-info "Source-bound-derived authority requires a module expectation"
                  {:id "SH07-COVERAGE-CENSUS-MODULE-MISSING"
                   :module module-name})))
      (when expectation
        (let [{:keys [source-path]} (get contracts module-name)]
          (validate-source-binding!
           contract module-name (source-bytes-sha256 source-path)))))))

(defn- source-bytes-sha256
  [path]
  (let [source-path (.toPath (io/file path))
        options (doto (java.util.HashSet.)
                  (.add java.nio.file.StandardOpenOption/READ)
                  (.add java.nio.file.LinkOption/NOFOLLOW_LINKS))
        digest (java.security.MessageDigest/getInstance "SHA-256")]
    (with-open [channel
                (java.nio.file.Files/newByteChannel
                 source-path options
                 (make-array java.nio.file.attribute.FileAttribute 0))]
      (let [attributes
            (java.nio.file.Files/readAttributes
             source-path java.nio.file.attribute.BasicFileAttributes
             (into-array java.nio.file.LinkOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
        (when-not (and (.isRegularFile attributes)
                       (not (.isSymbolicLink attributes)))
          (throw (ex-info "SH-07 module source is not a regular non-symlink"
                          {:id "SH07-AUTHORITATIVE-SOURCE-TYPE"
                           :path path})))
        (let [buffer (java.nio.ByteBuffer/allocate 1048576)]
          (loop [byte-count 0]
            (let [read (.read channel buffer)]
              (if (= -1 read)
                {:byte-count byte-count
                 :sha256
                 (str "sha256:"
                      (apply str
                             (map #(format "%02x" (bit-and % 0xff))
                                  (.digest digest))))}
                (do
                  (.flip buffer)
                  (.update digest buffer)
                  (.clear buffer)
                  (recur (+ byte-count read)))))))))))

(def authoritative-coverage-census-request-count-keys
  #{:fragment-count :root-form-count :form-count :binding-count
    :local-binding-count :resolution-count})

(def authoritative-coverage-census-core-count-keys
  #{:core-node-count :definition-count :call-count :reference-count
    :keyword-lookup-count :core-form-frequencies})

(def authoritative-coverage-census-integrity-keys
  #{:root-form-id-order-exact? :form-id-order-exact?
    :source-snapshot-stable? :source-revision-bound-to-bytes?
    :target-source-reread-disabled?})

(def authoritative-coverage-census-keys
  #{:artifact :schema-version :authority-scope :aggregate-authoritative?
    :module :module-namespace :source-revision-id :sh07-artifact-id
    :sh06-status :task :request-schema-version :scope :source-binding
    :request-counts :core-counts :integrity :census-hash
    :coverage-census-policy :counts-precommitted?
    :independent-count-oracle? :unsupported-claims})

(defn authoritative-coverage-census
  "Builds the compact module census from an already-built SH-07 carrier.

  This is a pure projection. It neither rebuilds nor independently verifies
  the carrier and therefore has only the authority of its enclosing fresh
  authoritative module output."
  ([module-name artifact request core source-binding-before source-binding-after]
   (authoritative-coverage-census
    module-name artifact request core source-binding-before source-binding-after
    :exact-precommitted))
  ([module-name artifact request core source-binding-before source-binding-after
    policy]
   (let [module-namespace (get-in request [:module :namespace])
        source-revision-id (get-in request [:lineage :source-revision-id])
        fragments (:fragment-manifest request)
        fragment-coverage (:fragment-coverage core)
        request-root-ids (vec (:top-level-form-ids request))
        fragment-root-ids (vec (mapcat :root-form-ids fragments))
        request-form-ids (mapv :form-id (:forms request))
        fragment-form-ids (vec (mapcat :form-ids fragments))
        nodes (:nodes core)
        derived? (= :source-bound-derived policy)
        census
        {:artifact :gravity/sh07-authoritative-coverage-census
         :schema-version 2
         :authority-scope
         (if derived?
           :individual-source-bound-derived
           :individual-existing-runner-output-only)
         :aggregate-authoritative? false
         :coverage-census-policy policy
         :counts-precommitted? (not derived?)
         :independent-count-oracle? (not derived?)
         :unsupported-claims
         (if derived? derived-census-unsupported-claims [])
         :module module-name
         :module-namespace module-namespace
         :source-revision-id source-revision-id
         :sh07-artifact-id (:artifact-id artifact)
         :sh06-status (get-in artifact [:sh06-resolution-artifact :status])
         :task (:task artifact)
         :request-schema-version (:schema-version request)
         :scope (:scope request)
         :source-binding
         {:source-byte-count (:byte-count source-binding-before)
          :source-bytes-sha256 (:sha256 source-binding-before)}
         :request-counts
         {:fragment-count (count fragments)
          :root-form-count (count request-root-ids)
          :form-count (count (:forms request))
          :binding-count (count (:binding-table request))
          :local-binding-count
          (count (filter #(= module-namespace (:namespace %))
                         (:binding-table request)))
          :resolution-count (count (:resolution-table request))}
         :core-counts
         {:core-node-count (count nodes)
          :definition-count (count (:definitions core))
          :call-count (count (:calls core))
          :reference-count (count (:reference-uses core))
          :keyword-lookup-count (count (:keyword-lookups core))
          :core-form-frequencies
          (into (sorted-map) (frequencies (map :core-form nodes)))}
         :integrity
         {:root-form-id-order-exact?
          (= request-root-ids
             (vec (:covered-root-form-ids fragment-coverage))
             fragment-root-ids)
          :form-id-order-exact?
          (= request-form-ids
             (vec (:covered-form-ids fragment-coverage))
             fragment-form-ids)
          :source-snapshot-stable?
          (= source-binding-before source-binding-after)
          :source-revision-bound-to-bytes?
          (= (:sha256 source-binding-before)
             source-revision-id
             (get-in request [:module :source-revision-id]))
          :target-source-reread-disabled?
          (false? (get-in artifact
                          [:gravity-core-boundary :target-source-reread?]))}}]
    (assoc census :census-hash
           (bootstrap/reader-canonical-hash
            {:domain :gravity/sh07-authoritative-coverage-census-v2
             :census census})))))

(defn authoritative-coverage-census-valid?
  [contract module-name census]
  (let [policy (coverage-census-policy contract)
        expected
        (get-in contract
                [:authoritative-coverage-census :module-expectations
                 (keyword module-name)])
        payload (dissoc census :census-hash)
        counts? #(and (map? %)
                      (every? (fn [[_ value]]
                                (and (integer? value) (not (neg? value))))
                              %))
        frequencies-map (get-in census [:core-counts :core-form-frequencies])]
    (and (valid-census-policy-contract? contract)
         (= :gravity/sh07-authoritative-coverage-census (:artifact census))
         (= authoritative-coverage-census-keys (set (keys census)))
         (= (get-in contract [:authoritative-coverage-census :schema-version])
            (:schema-version census))
         (= policy (:coverage-census-policy census))
         (= (if (= :source-bound-derived policy)
              :individual-source-bound-derived
              :individual-existing-runner-output-only)
            (:authority-scope census))
         (false? (:aggregate-authoritative? census))
         (= (= :source-bound-derived policy)
            (= false (:counts-precommitted? census))
            (= false (:independent-count-oracle? census)))
         (= (if (= :source-bound-derived policy)
              derived-census-unsupported-claims [])
            (:unsupported-claims census))
         (= module-name (:module census))
         (symbol? (:module-namespace census))
         (string? (:source-revision-id census))
         (string? (:sh07-artifact-id census))
         (= :accepted (:sh06-status census))
         (string? (:task census))
         (pos-int? (:request-schema-version census))
         (keyword? (:scope census))
         (= #{:source-byte-count :source-bytes-sha256}
            (set (keys (:source-binding census))))
         (and (integer? (get-in census [:source-binding :source-byte-count]))
              (not (neg? (get-in census
                                 [:source-binding :source-byte-count]))))
         (= (:source-revision-id census)
            (get-in census [:source-binding :source-bytes-sha256]))
         (= authoritative-coverage-census-request-count-keys
            (set (keys (:request-counts census))))
         (= authoritative-coverage-census-core-count-keys
            (set (keys (:core-counts census))))
         (= authoritative-coverage-census-integrity-keys
            (set (keys (:integrity census))))
         (counts? (:request-counts census))
         (counts? (dissoc (:core-counts census) :core-form-frequencies))
         (map? frequencies-map)
         (every? (fn [[form count]]
                   (and (keyword? form) (pos-int? count)))
                 frequencies-map)
         (every? true? (vals (:integrity census)))
         (= (:census-hash census)
            (bootstrap/reader-canonical-hash
             {:domain :gravity/sh07-authoritative-coverage-census-v2
              :census payload}))
         (and (some? expected)
              (= (:module-namespace expected)
                     (:module-namespace census))
                  (= (:source-binding expected)
                     (:source-binding census))
                  (if (= :exact-precommitted policy)
                    (and (= (:request-counts expected) (:request-counts census))
                         (= (:core-counts expected) (:core-counts census)))
                    (and (not (contains? expected :request-counts))
                         (not (contains? expected :core-counts))))))))

(defn- run-module
  [contract module-name]
  (let [path (get (modules contract) module-name)]
    (when-not path
      (throw
       (ex-info "Unknown SH-07 authoritative module"
                {:id "SH07-AUTHORITATIVE-MODULE"
                 :module module-name
                 :available (module-names)})))
    (let [started (System/nanoTime)
          source-binding-before (source-bytes-sha256 path)
          _ (validate-source-binding!
             contract module-name source-binding-before)
          proof-run (bootstrap/sh07-core-file-proof-transaction path)
          artifact (:artifact proof-run)
          verification (:verification proof-run)
          proof-transaction (:proof-transaction proof-run)
          request
          (get-in artifact
                  [:gravity-core-boundary
                   :authenticated-core-request])
          core
          (get-in artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact])
          capability-proof (:capability-proof proof-run)
          required-request-products
          (:required-request-products contract)
          required-core-products
          (:required-core-products contract)
          required-core-product-counts
          (get-in contract
                  [:required-core-product-counts
                   (keyword module-name)]
                  {})
          required-verification-checks
          (:required-verification-checks contract)
          proof-transaction-contract (:proof-transaction contract)
          phase-by-name
          (into {} (map (juxt :phase identity))
                (:phases proof-transaction))
          independent-audit
          (get phase-by-name :independent-audit)
          boundary (:boundary contract)
          source-binding-after (source-bytes-sha256 path)
          census-policy (coverage-census-policy contract)
          source-revision-id
          (get-in request [:lineage :source-revision-id])
          coverage-census
          (authoritative-coverage-census
           module-name artifact request core
           source-binding-before source-binding-after census-policy)
          contract-checks
          {:coverage-census-policy-current?
           (= census-policy
              (get-in contract [:authoritative-coverage-census :policy]))
           :counts-precommitted-policy-current?
           (= (if (= :source-bound-derived census-policy) false true)
              (get-in coverage-census [:counts-precommitted?]))
           :independent-count-oracle-policy-current?
           (= (if (= :source-bound-derived census-policy) false true)
              (get-in coverage-census [:independent-count-oracle?]))
           :unsupported-claims-explicit?
           (= (if (= :source-bound-derived census-policy)
                derived-census-unsupported-claims [])
              (:unsupported-claims coverage-census))
           :request-schema-current?
           (= (:request-schema-version boundary)
              (:schema-version request))
           :task-current?
           (= (:task boundary) (:task artifact))
           :scope-current?
           (= (:scope boundary) (:scope request))
           :adapter-current?
           (= (:adapter boundary)
              bootstrap/sh07-core-adapter-contract
              (get-in artifact
                      [:gravity-core-boundary :adapter-contract]))
           :fresh-process-required?
           (true? (:fresh-authoritative-process-required boundary))
           :iteration-cache-non-authoritative?
           (false? (:iteration-cache-authoritative boundary))
           :coverage-milestone-current?
           (= "SH-07-B47" (:coverage-milestone contract))
           :target-source-reread-disabled?
           (false?
            (get-in artifact
                    [:gravity-core-boundary :target-source-reread?]))
           :source-snapshot-stable?
           (= source-binding-before source-binding-after)
           :source-revision-bound-to-bytes?
           (= (:sha256 source-binding-before)
              source-revision-id
              (get-in request [:module :source-revision-id]))
           :authoritative-coverage-census-current?
           (authoritative-coverage-census-valid?
            contract module-name coverage-census)
           :required-request-products-present?
           (every? #(contains? request %)
                   required-request-products)
           :required-core-products-present?
           (every? #(contains? core %)
                   required-core-products)
           :required-core-product-counts-exact?
           (every?
            (fn [[product expected-count]]
              (let [value (get core product)]
                (and (coll? value)
                     (= expected-count (count value)))))
            required-core-product-counts)
           :required-verification-checks-present-and-passed?
           (every?
            #(and (contains? capability-proof %)
                  (true? (get capability-proof %)))
            required-verification-checks)
           :capability-proof-complete?
           (and (= :gravity/sh07-core-capability-proof
                   (:artifact capability-proof))
                (= :complete (:status capability-proof))
                (empty? (:failed-checks capability-proof)))
           :independent-verification-passed?
           (and (= :gravity/sh07-core-artifact-verification
                   (:artifact verification))
                (= :passed (:status verification))
                (empty? (:failed-checks verification)))
           :proof-transaction-current?
           (and (= :gravity/sh07-proof-transaction-receipt
                   (:artifact proof-transaction))
                (= :passed (:status proof-transaction))
                (= (:schema-version proof-transaction-contract)
                   (:schema-version proof-transaction))
                (= (:phase-order proof-transaction-contract)
                   (:phase-order proof-transaction))
                (= (:maximum-receipts-per-phase
                    proof-transaction-contract)
                   (:maximum-receipts proof-transaction))
                (true?
                 (:exact-verifier-root-identity-required
                  proof-transaction-contract))
                (true?
                 (:verification-check-catalog-binding-required
                  proof-transaction-contract))
                (true?
                 (:immutable-receipt-carriers-required
                  proof-transaction-contract))
                (= #{[:sh05 :construction] [:sh05 :final]
                     [:sh06 :final] [:sh07 :final]}
                   (set (keys (:check-catalog-bindings
                               proof-transaction))))
                (every? string?
                        (vals (:check-catalog-bindings
                               proof-transaction)))
                (true? (:thread-confined? proof-transaction))
                (false? (:cross-epoch-reuse? proof-transaction))
                (zero? (:cross-epoch-reuse-count proof-transaction))
                (false? (:failed-report-reuse? proof-transaction))
                (zero? (:failed-report-reuse-count proof-transaction))
                (zero? (:failed-report-executions proof-transaction))
                (true?
                 (:construction-receipts-cleared? proof-transaction))
                (true? (:final-snapshot-rechecked? proof-transaction))
                (true? (:cleanup-complete? proof-transaction))
                (zero? (:retained-receipt-count proof-transaction))
                (= (:artifact-id artifact)
                   (:artifact-id proof-transaction))
                (= (bootstrap/reader-canonical-hash verification)
                   (:verification-report-id proof-transaction))
                (= {:source-byte-count (:byte-count source-binding-before)
                    :source-content-hash (:sha256 source-binding-before)}
                   (select-keys
                    (:source-snapshot proof-transaction)
                    [:source-byte-count :source-content-hash]))
                (= [0 1]
                   (mapv :epoch (:phases proof-transaction)))
                (every?
                 map?
                 [(get-in proof-transaction
                          [:checked-core-revision :sh05-macro-revision])
                  (get-in proof-transaction
                          [:checked-core-revision
                           :sh06-resolution-revision])])
                (every?
                 (fn [[stage minimum]]
                   (<= minimum
                       (get-in independent-audit
                               [:verification-executions stage]
                               0)))
                 (:minimum-independent-audit-executions
                  proof-transaction-contract))
                (every?
                 #(<= (:receipt-count %)
                      (:maximum-receipts-per-phase
                       proof-transaction-contract))
                 (:phases proof-transaction))
                (<=
                 (reduce + 0
                         (map #(get-in %
                                       [:verification-executions :sh05]
                                       0)
                              (:phases proof-transaction)))
                 (:maximum-sh05-full-verifications
                  proof-transaction-contract)))}
          failed-contract-checks
          (vec
           (for [[check passed?] contract-checks
                 :when (not (true? passed?))]
             check))
          elapsed-ms
          (long (/ (- (System/nanoTime) started) 1000000))]
      {:module module-name
       :source-path path
       :source-byte-count (:byte-count source-binding-before)
       :source-bytes-sha256 (:sha256 source-binding-before)
       :source-revision-id source-revision-id
       :status (:status artifact)
       :artifact-id (:artifact-id artifact)
       :coverage-census coverage-census
       :schema-version (:schema-version request)
       :fragment-count (count (:fragment-manifest request))
       :form-count (get-in core [:fragment-coverage :form-count])
       :resolution-count
       (get-in core [:fragment-coverage :resolution-count])
       :keyword-lookup-count (count (:keyword-lookups core))
       :function-record-count (count (:function-records core))
       :call-edge-count (count (:call-edges core))
       :recursion-component-count (count (:recursion-components core))
       :verification-status
       (if (= :complete (:status capability-proof))
         :passed :failed)
       :capability-proof-status (:status capability-proof)
       :proof-transaction proof-transaction
       :failed-checks
       (vec
        (distinct
         (concat (:failed-checks capability-proof)
                 failed-contract-checks)))
       :contract-checks contract-checks
       :elapsed-ms elapsed-ms})))

(defn run-authoritative
  [requested]
  (let [contract (validate-coverage-census-contract! (proof-contract))
        selected
        (if (= requested "all")
          (vec (keys (modules contract)))
          [requested])
        _ (validate-selected-source-contracts! contract selected)
        started (System/nanoTime)
        results (mapv #(run-module contract %) selected)
        proof-receipt-reuse-count
        (reduce
         + 0
         (for [result results
               phase (get-in result [:proof-transaction :phases])
               [_ count] (:verification-reuses phase)]
           count))
        passed?
        (every?
         #(and (= :accepted (:status %))
               (= :passed (:verification-status %))
               (= :complete (:capability-proof-status %))
               (every? true? (vals (:contract-checks %)))
               (empty? (:failed-checks %)))
         results)]
    {:artifact :gravity/sh07-authoritative-proof-run
     :schema-version 3
     :status (if passed? :passed :failed)
     :coverage-census-policy (coverage-census-policy contract)
     :authority-scope
     (if (= :source-bound-derived (coverage-census-policy contract))
       :individual-source-bound-derived
       :individual-existing-runner-output-only)
     :counts-precommitted?
     (= :exact-precommitted (coverage-census-policy contract))
     :independent-count-oracle?
     (= :exact-precommitted (coverage-census-policy contract))
     :aggregate-authoritative? false
     :unsupported-claims
     (if (= :source-bound-derived (coverage-census-policy contract))
       derived-census-unsupported-claims [])
     :attestation-required?
     (= :source-bound-derived (coverage-census-policy contract))
     :fresh-process-required? true
     :persistent-iteration-cache-used? false
     :proof-receipt-reuse-used? (pos? proof-receipt-reuse-count)
     :proof-receipt-reuse-count proof-receipt-reuse-count
     :modules results
     :elapsed-ms
     (long (/ (- (System/nanoTime) started) 1000000))}))

(def ^:private authoritative-run-result-keys
  #{:artifact :schema-version :status :coverage-census-policy
    :authority-scope :counts-precommitted? :independent-count-oracle?
    :aggregate-authoritative? :unsupported-claims :attestation-required?
    :fresh-process-required? :persistent-iteration-cache-used?
    :proof-receipt-reuse-used? :proof-receipt-reuse-count :modules
    :elapsed-ms})

(def ^:private authoritative-module-result-keys
  #{:module :source-path :source-byte-count :source-bytes-sha256
    :source-revision-id :status :artifact-id :coverage-census
    :schema-version :fragment-count :form-count :resolution-count
    :keyword-lookup-count :function-record-count :call-edge-count
    :recursion-component-count :verification-status
    :capability-proof-status :proof-transaction :failed-checks
    :contract-checks :elapsed-ms})

(def ^:private authoritative-contract-check-keys
  #{:coverage-census-policy-current?
    :counts-precommitted-policy-current?
    :independent-count-oracle-policy-current?
    :unsupported-claims-explicit?
    :request-schema-current?
    :task-current?
    :scope-current?
    :adapter-current?
    :fresh-process-required?
    :iteration-cache-non-authoritative?
    :coverage-milestone-current?
    :target-source-reread-disabled?
    :source-snapshot-stable?
    :source-revision-bound-to-bytes?
    :authoritative-coverage-census-current?
    :required-request-products-present?
    :required-core-products-present?
    :required-core-product-counts-exact?
    :required-verification-checks-present-and-passed?
    :capability-proof-complete?
    :independent-verification-passed?
    :proof-transaction-current?})

(def ^:private proof-transaction-result-keys
  #{:artifact :schema-version :status :thread-confined? :owner-thread-id
    :phase-order :phases :source-snapshot :checked-core-revision
    :check-catalog-bindings :maximum-receipts :artifact-id
    :verification-report-id :construction-receipts-cleared?
    :final-snapshot-rechecked? :cross-epoch-reuse-count
    :cross-epoch-reuse? :failed-report-executions
    :failed-report-reuse-count :failed-report-reuse? :cleanup-complete?
    :retained-receipt-count})

(def ^:private proof-phase-result-keys
  #{:phase :epoch :verification-executions :verification-reuses
    :receipt-count})

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- nonnegative-integer?
  [value]
  (and (integer? value) (not (neg? value))))

(defn- receipt-failure!
  [message data]
  (throw (ex-info message (assoc data :id "SH07-AUTHORITATIVE-RECEIPT"))))

(defn- exported-source-binding!
  "Authenticate one contract-owned source beneath the isolated export root."
  [source-root source-path]
  (when-not (and (string? source-root) (not (string/blank? source-root))
                 (string? source-path) (not (string/blank? source-path)))
    (receipt-failure! "Authoritative source root or path is absent"
                      {:source-root source-root :source-path source-path}))
  (let [^Path lexical-root (.normalize
                            (.toAbsolutePath
                             (Paths/get source-root (make-array String 0))))
        ^Path relative (Paths/get source-path (make-array String 0))
        _ (when-not (and (Files/isDirectory lexical-root
                                      (into-array LinkOption
                                                  [LinkOption/NOFOLLOW_LINKS]))
                           (not (Files/isSymbolicLink lexical-root)))
            (receipt-failure! "Authoritative source root is not a safe directory"
                              {:source-root source-root}))
        ^Path root (.toRealPath lexical-root (make-array LinkOption 0))
        ^Path candidate (.normalize (.resolve root relative))]
    (when-not (and (not (.isAbsolute relative))
                   (.startsWith candidate root))
      (receipt-failure! "Authoritative source path escapes its export root"
                        {:source-root source-root :source-path source-path}))
    (loop [current root
           names (seq (iterator-seq (.iterator relative)))]
      (when-let [name (first names)]
        (let [next-path (.resolve ^Path current ^Path name)]
          (when (or (Files/isSymbolicLink next-path)
                    (and (next names)
                         (not (Files/isDirectory
                               next-path
                               (into-array LinkOption
                                           [LinkOption/NOFOLLOW_LINKS])))))
            (receipt-failure! "Authoritative source path contains an unsafe component"
                              {:source-path source-path}))
          (recur next-path (next names)))))
    (when-not (and (Files/isRegularFile
                    candidate (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (not (Files/isSymbolicLink candidate)))
      (receipt-failure! "Authoritative source is not a regular non-symlink"
                        {:source-path source-path}))
    (let [first-pass (source-bytes-sha256 (str candidate))
          second-pass (source-bytes-sha256 (str candidate))]
      (when-not (= first-pass second-pass)
        (receipt-failure! "Authoritative source changed during receipt validation"
                          {:source-path source-path}))
      {:source-byte-count (:byte-count first-pass)
       :source-bytes-sha256 (:sha256 first-pass)})))

(defn- valid-proof-transaction-result?
  [contract module-result actual-binding]
  (let [proof (:proof-transaction module-result)
        proof-contract (:proof-transaction contract)
        phases (:phases proof)
        independent-audit (some #(when (= :independent-audit (:phase %)) %)
                                phases)
        revision (:checked-core-revision proof)
        revision-hash-fields
        [:source-content-hash :plan-semantic-hash :functions-semantic-hash
         :function-names-hash :function-shapes-hash]
        valid-revision?
        (fn [value]
          (and (map? value)
               (every? #(sha256-id? (get value %)) revision-hash-fields)
               (nonnegative-integer? (:source-byte-count value))
               (nonnegative-integer? (:function-count value))
               (string? (:observed-source-path value))))]
    (and (map? proof)
         (= proof-transaction-result-keys (set (keys proof)))
         (= :gravity/sh07-proof-transaction-receipt (:artifact proof))
         (= (:schema-version proof-contract) (:schema-version proof))
         (= :passed (:status proof))
         (true? (:thread-confined? proof))
         (integer? (:owner-thread-id proof))
         (pos? (:owner-thread-id proof))
         (= (:phase-order proof-contract) (:phase-order proof))
         (vector? phases)
         (= (:phase-order proof) (mapv :phase phases))
         (= [0 1] (mapv :epoch phases))
         (every? #(and (= proof-phase-result-keys (set (keys %)))
                       (map? (:verification-executions %))
                       (map? (:verification-reuses %))
                       (every? nonnegative-integer?
                               (vals (:verification-executions %)))
                       (every? nonnegative-integer?
                               (vals (:verification-reuses %)))
                       (nonnegative-integer? (:receipt-count %))
                       (<= (:receipt-count %)
                           (:maximum-receipts-per-phase proof-contract)))
                 phases)
         (every? (fn [[stage minimum]]
                   (<= minimum
                       (get-in independent-audit
                               [:verification-executions stage] 0)))
                 (:minimum-independent-audit-executions proof-contract))
         (= {:source-path (:source-path module-result)
             :source-byte-count (:source-byte-count actual-binding)
             :source-content-hash (:source-bytes-sha256 actual-binding)}
            (:source-snapshot proof))
         (valid-revision? revision)
         (valid-revision? (:sh05-macro-revision revision))
         (valid-revision? (:sh06-resolution-revision revision))
         (= #{[:sh05 :construction] [:sh05 :final]
              [:sh06 :final] [:sh07 :final]}
            (set (keys (:check-catalog-bindings proof))))
         (every? sha256-id? (vals (:check-catalog-bindings proof)))
         (= (:maximum-receipts-per-phase proof-contract)
            (:maximum-receipts proof))
         (= (:artifact-id module-result) (:artifact-id proof))
         (sha256-id? (:verification-report-id proof))
         (true? (:construction-receipts-cleared? proof))
         (true? (:final-snapshot-rechecked? proof))
         (zero? (:cross-epoch-reuse-count proof))
         (false? (:cross-epoch-reuse? proof))
         (zero? (:failed-report-executions proof))
         (zero? (:failed-report-reuse-count proof))
         (false? (:failed-report-reuse? proof))
         (true? (:cleanup-complete? proof))
         (zero? (:retained-receipt-count proof)))))

(defn- validate-authoritative-module-result!
  [contract source-root module-name module-result]
  (let [{:keys [source-path source-binding]}
        (get (module-source-contracts-for contract) module-name)
        actual-binding (exported-source-binding! source-root source-path)
        census (:coverage-census module-result)
        contract-checks (:contract-checks module-result)
        nonnegative-count-keys
        [:fragment-count :form-count :resolution-count :keyword-lookup-count
         :function-record-count :call-edge-count :recursion-component-count
         :elapsed-ms]]
    (when-not
     (and (= authoritative-module-result-keys (set (keys module-result)))
          (= module-name (:module module-result))
          (= source-path (:source-path module-result))
          (= source-binding actual-binding)
          (= (:source-byte-count actual-binding)
             (:source-byte-count module-result)
             (get-in census [:source-binding :source-byte-count]))
          (= (:source-bytes-sha256 actual-binding)
             (:source-bytes-sha256 module-result)
             (:source-revision-id module-result)
             (:source-revision-id census)
             (get-in census [:source-binding :source-bytes-sha256]))
          (= :accepted (:status module-result))
          (sha256-id? (:artifact-id module-result))
          (= (:artifact-id module-result) (:sh07-artifact-id census))
          (= (get-in contract [:boundary :request-schema-version])
             (:schema-version module-result))
          (every? #(let [value (get module-result %)]
                     (and (integer? value) (not (neg? value))))
                  nonnegative-count-keys)
          (= :passed (:verification-status module-result))
          (= :complete (:capability-proof-status module-result))
          (valid-proof-transaction-result?
           contract module-result actual-binding)
          (= [] (:failed-checks module-result))
          (= authoritative-contract-check-keys (set (keys contract-checks)))
          (every? true? (vals contract-checks))
          (= (:fragment-count module-result)
             (get-in census [:request-counts :fragment-count]))
          (= (:form-count module-result)
             (get-in census [:request-counts :form-count]))
          (= (:resolution-count module-result)
             (get-in census [:request-counts :resolution-count]))
          (= (:keyword-lookup-count module-result)
             (get-in census [:core-counts :keyword-lookup-count]))
          (authoritative-coverage-census-valid?
           contract module-name census))
      (receipt-failure! "Authoritative module receipt failed validation"
                        {:module module-name}))
    module-result))

(defn validate-authoritative-run-result!
  "Canonical fail-closed validator for fresh lane child receipts.

  The child report is not trusted to describe its source. Every selected
  module is rebound to the proof contract and hashed twice from the isolated
  export before a caller may issue a pass receipt."
  [requested result source-root]
  (let [contract (validate-coverage-census-contract! (proof-contract))
        catalog (modules contract)
        expected-modules (cond
                           (= requested "all")
                           (receipt-failure!
                            "Aggregate authoritative receipts are unsupported"
                            {:requested requested
                             :unsupported-claim :aggregate})

                           (contains? catalog requested) [requested]

                           :else
                           (receipt-failure!
                            "Unknown authoritative receipt module"
                            {:requested requested}))
        result-modules (:modules result)
        policy (coverage-census-policy contract)
        derived? (= :source-bound-derived policy)]
    (when-not
     (and (map? result)
          (= authoritative-run-result-keys (set (keys result)))
          (= :gravity/sh07-authoritative-proof-run (:artifact result))
          (= 3 (:schema-version result))
          (= :passed (:status result))
          (= policy (:coverage-census-policy result))
          (= (if derived?
               :individual-source-bound-derived
               :individual-existing-runner-output-only)
             (:authority-scope result))
          (= (not derived?) (:counts-precommitted? result))
          (= (not derived?) (:independent-count-oracle? result))
          (false? (:aggregate-authoritative? result))
          (= (if derived? derived-census-unsupported-claims [])
             (:unsupported-claims result))
          (= derived? (:attestation-required? result))
          (true? (:fresh-process-required? result))
          (false? (:persistent-iteration-cache-used? result))
          (boolean? (:proof-receipt-reuse-used? result))
          (integer? (:proof-receipt-reuse-count result))
          (not (neg? (:proof-receipt-reuse-count result)))
          (= (pos? (:proof-receipt-reuse-count result))
             (:proof-receipt-reuse-used? result))
          (vector? result-modules)
          (= expected-modules (mapv :module result-modules))
          (integer? (:elapsed-ms result))
          (not (neg? (:elapsed-ms result))))
      (receipt-failure! "Authoritative proof receipt failed validation"
                        {:requested requested}))
    (doseq [[module-name module-result]
            (map vector expected-modules result-modules)]
      (validate-authoritative-module-result!
       contract source-root module-name module-result))
    result))

(defn -main
  [& arguments]
  (cond
    (= ["--list"] (vec arguments))
    (doseq [name (module-names)] (println name))

    (= ["--catalog"] (vec arguments))
    (doseq [[module path] (module-catalog)]
      (println (str module "\t" path)))

    (= ["--source-contracts"] (vec arguments))
    (let [snapshot (proof-contract-snapshot)]
      (println (str "#proof-contract-sha256\t" (:sha256 snapshot)))
      (println (str "#coverage-census-policy\t"
                    (name (coverage-census-policy (:contract snapshot)))))
      (doseq [[module {:keys [source-path source-binding]}]
              (module-source-contracts-for (:contract snapshot))]
        (println
         (str module "\t" source-path "\t"
              (:source-byte-count source-binding) "\t"
              (:source-bytes-sha256 source-binding)))))

    (and (= 2 (count arguments))
         (= "--fresh" (first arguments))
         (contains? (set (conj (module-names) "all"))
                    (second arguments)))
    (let [result (run-authoritative (second arguments))]
      (println (pr-str result))
      (when-not (= :passed (:status result))
        (System/exit 1)))

    :else
    (throw
     (ex-info
      (str "Expected --list, --catalog, --source-contracts, or --fresh <module|all>; available modules: "
           (string/join "|" (module-names)))
      {:id "SH07-AUTHORITATIVE-USAGE"
       :arguments (vec arguments)
       :available (module-names)}))))
