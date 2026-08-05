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
           (= "SH-07-B39" (:coverage-milestone contract))
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
     :schema-version 1
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
      (str "Expected --list or --fresh <module|all>; available modules: "
           (string/join "|" (module-names)))
      {:id "SH07-AUTHORITATIVE-USAGE"
       :arguments (vec arguments)
       :available (module-names)}))))
