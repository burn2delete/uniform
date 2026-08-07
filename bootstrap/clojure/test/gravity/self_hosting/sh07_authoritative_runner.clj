(ns gravity.self-hosting.sh07-authoritative-runner
  "Runs cache-free SH-07 authoritative module acceptance and public replay."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [gravity.bootstrap :as bootstrap]))

(def ^:private contract-resource
  "gravity/self_hosting/sh07_proof_contract.edn")

(defn- proof-contract
  []
  (let [resource (io/resource contract-resource)]
    (when-not resource
      (throw
       (ex-info "SH-07 proof contract is absent"
                {:id "SH07-PROOF-CONTRACT-ABSENT"
                 :resource contract-resource})))
    (edn/read-string (slurp resource))))

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

(defn module-catalog
  "Returns the deterministic module-to-source catalog used by --fresh.

  The tab-delimited CLI representation is intentionally narrow so external
  checkpoint tooling can validate paths without attempting to parse EDN."
  []
  (let [catalog (modules (proof-contract))
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

(defn- source-bytes-sha256
  [path]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        bytes (java.nio.file.Files/readAllBytes
               (.toPath (io/file path)))]
    (.update digest bytes)
    {:byte-count (alength bytes)
     :sha256
     (str
      "sha256:"
      (apply str (map #(format "%02x" (bit-and % 0xff))
                      (.digest digest))))}))

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
    :request-counts :core-counts :integrity :census-hash})

(defn authoritative-coverage-census
  "Builds the compact module census from an already-built SH-07 carrier.

  This is a pure projection. It neither rebuilds nor independently verifies
  the carrier and therefore has only the authority of its enclosing fresh
  authoritative module output."
  [module-name artifact request core source-binding-before source-binding-after]
  (let [module-namespace (get-in request [:module :namespace])
        source-revision-id (get-in request [:lineage :source-revision-id])
        fragments (:fragment-manifest request)
        fragment-coverage (:fragment-coverage core)
        request-root-ids (vec (:top-level-form-ids request))
        fragment-root-ids (vec (mapcat :root-form-ids fragments))
        request-form-ids (mapv :form-id (:forms request))
        fragment-form-ids (vec (mapcat :form-ids fragments))
        nodes (:nodes core)
        census
        {:artifact :gravity/sh07-authoritative-coverage-census
         :schema-version 1
         :authority-scope :individual-existing-runner-output-only
         :aggregate-authoritative? false
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
            {:domain :gravity/sh07-authoritative-coverage-census-v1
             :census census}))))

(defn authoritative-coverage-census-valid?
  [contract module-name census]
  (let [expected
        (get-in contract
                [:authoritative-coverage-census :module-expectations
                 (keyword module-name)])
        payload (dissoc census :census-hash)
        counts? #(and (map? %)
                      (every? (fn [[_ value]]
                                (and (integer? value) (not (neg? value))))
                              %))
        frequencies-map (get-in census [:core-counts :core-form-frequencies])]
    (and (= :gravity/sh07-authoritative-coverage-census (:artifact census))
         (= authoritative-coverage-census-keys (set (keys census)))
         (= (get-in contract [:authoritative-coverage-census :schema-version])
            (:schema-version census))
         (= :individual-existing-runner-output-only (:authority-scope census))
         (false? (:aggregate-authoritative? census))
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
             {:domain :gravity/sh07-authoritative-coverage-census-v1
              :census payload}))
         (or (nil? expected)
             (and (= (:module-namespace expected)
                     (:module-namespace census))
                  (= (:request-counts expected) (:request-counts census))
                  (= (:core-counts expected) (:core-counts census)))))))

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
          source-revision-id
          (get-in request [:lineage :source-revision-id])
          coverage-census
          (authoritative-coverage-census
           module-name artifact request core
           source-binding-before source-binding-after)
          contract-checks
          {:request-schema-current?
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
           (= "SH-07-B45" (:coverage-milestone contract))
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
  (let [contract (proof-contract)
        selected
        (if (= requested "all")
          (vec (keys (modules contract)))
          [requested])
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
     :schema-version 2
     :status (if passed? :passed :failed)
     :fresh-process-required? true
     :persistent-iteration-cache-used? false
     :proof-receipt-reuse-used? (pos? proof-receipt-reuse-count)
     :proof-receipt-reuse-count proof-receipt-reuse-count
     :modules results
     :elapsed-ms
     (long (/ (- (System/nanoTime) started) 1000000))}))

(defn -main
  [& arguments]
  (cond
    (= ["--list"] (vec arguments))
    (doseq [name (module-names)] (println name))

    (= ["--catalog"] (vec arguments))
    (doseq [[module path] (module-catalog)]
      (println (str module "\t" path)))

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
      (str "Expected --list, --catalog, or --fresh <module|all>; available modules: "
           (string/join "|" (module-names)))
      {:id "SH07-AUTHORITATIVE-USAGE"
       :arguments (vec arguments)
       :available (module-names)}))))
