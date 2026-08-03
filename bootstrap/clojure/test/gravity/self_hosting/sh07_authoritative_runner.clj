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
          artifact (bootstrap/sh07-core-file-artifact path)
          request
          (get-in artifact
                  [:gravity-core-boundary
                   :authenticated-core-request])
          core
          (get-in artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact])
          capability-proof
          (bootstrap/sh07-core-capability-based-proof artifact)
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
           (= "SH-07-B35" (:coverage-milestone contract))
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
                (empty? (:failed-checks capability-proof)))}
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
     :cache-used? false
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
