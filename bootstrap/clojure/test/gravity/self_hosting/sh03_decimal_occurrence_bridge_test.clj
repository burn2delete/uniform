(ns gravity.self-hosting.sh03-decimal-occurrence-bridge-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "missing SH-07 decimal bridge seam" {:symbol symbol}))))

(defn- private-call
  [symbol & args]
  (apply @(private-var symbol) args))

(defn- failure-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (ex-data failure))))

(def ^:private reader-path bootstrap/sh03-reader-source-relative-path)

(def ^:private current-plan
  (delay
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
               reader-path :jvm))
     reader-path (slurp reader-path))))

(defn- decimal-products
  [source-text]
  (let [source-path "src/sh03-decimal-bridge-test.gravity"
        source-bytes (.getBytes ^String source-text
                                java.nio.charset.StandardCharsets/UTF_8)
        source-unit
        {:artifact :gravity/sh03-source-unit
         :schema-version 1
         :project-root-id (str "sha256:" (apply str (repeat 64 "0")))
         :logical-source-id source-path
         :source-content-hash
         (str "sha256:" (bootstrap/sha256-bytes-hex source-bytes))
         :source-byte-count (alength source-bytes)
         :encoding :utf-8
         :actual-path-provenance {:path "/declared/sh03-decimal-bridge-test.gravity"
                                  :extension ".gravity"
                                  :source-kind :gravity}}
        reader-policy
        {:artifact :gravity/sh03-reader-policy
         :schema-version 1
         :retain-trivia true
         :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}
        arguments [source-unit
                   (mapv #(bit-and 255 %) source-bytes)
                   reader-policy]
        raw-result
        (bootstrap/p15-s23-stage2-runtime-execute-function
         {:engine :gravity-sh03-decimal-bridge-test
          :compiler-artifact-plan? true}
         @current-plan 'sh03-read-source-unit arguments)
        binding (bootstrap/sh03-reader-current-binding!
                 source-path reader-path)
        resolved
        (bootstrap/sh03-reader-resolve-digest-requests!
         source-path raw-result source-bytes)
        resolved
        (assoc resolved
               :raw-result raw-result
               :verification-report {:status :accepted :verified? true}
               :plan-binding (dissoc binding :plan)
               :invocation-context
               {:reader-source-path reader-path
                :authenticated-envelope-options {:source :development-current}})]
    (bootstrap/sh03-reader-adapt-products!
     source-path source-text source-bytes bootstrap/standard-reader-options
     (bootstrap/reader-project-context-for-source source-path)
     resolved)))

(defn- c2-artifact
  [products]
  (let [projection (:sh07-decimal-occurrence-projection products)
        boundary {:adapter-contract (:sh03-reader-adapter-contract products)
                  :decimal-occurrence-projection-id (:projection-id projection)
                  :authenticated-envelope-descriptor
                  (:sh02-reader-envelope-descriptor products)}
        artifact
        {:kind :gravity/stage0-c2-reader-document-artifact
         :task "P06-D081"
         :document-set ["C2"]
         :source-unit-record (:source-unit products)
         :reader-product-integrity {}
         :incremental-reader-hashes {}
         :representation-boundary {}
         :source-overrides {}
         :capability-based-proof {}
         :gravity-reader-boundary boundary
         :sh07-decimal-occurrence-projection projection
         :form-tree (:form-tree products)
         :literal-decoding-records (:literal-decoding-records products)}]
    (assoc artifact :artifact-id (bootstrap/c2-reader-artifact-id artifact))))

(defn- occurrence-evidence
  [artifact occurrence]
  {:c2-reader-artifact artifact
   :adapted-form-id (:adapted-form-id occurrence)
   :adapted-token-id (:adapted-token-id occurrence)})

(deftest v3-contract-emits-authenticated-repeated-decimal-occurrences
  (let [source-text "[1.2E+0 1.20E+0 1E+1000000000000000000]\n"
        products (decimal-products source-text)
        projection (:sh07-decimal-occurrence-projection products)
        recomputed
        (bootstrap/sh03-reader-decimal-occurrence-projection-from-products!
         "src/sh03-decimal-bridge-test.gravity" products)
        occurrences (:occurrences projection)
        artifact (c2-artifact products)
        first-evidence (occurrence-evidence artifact (first occurrences))
        second-evidence (occurrence-evidence artifact (second occurrences))
        first-key (get-in first-evidence
                          [:c2-reader-artifact
                           :sh07-decimal-occurrence-projection
                           :occurrences 0 :semantic-value-entry :semantic-key])
        second-key (get-in second-evidence
                           [:c2-reader-artifact
                            :sh07-decimal-occurrence-projection
                            :occurrences 1 :semantic-value-entry :semantic-key])
        deferred-key (get-in artifact
                             [:sh07-decimal-occurrence-projection
                              :occurrences 2 :semantic-value-entry
                              :semantic-key])]
    (is (= :gravity/sh03-to-c2-reader-products-v3
           (:sh03-reader-adapter-contract products)))
    (is (= projection recomputed))
    (is (= 3 (:occurrence-count projection)))
    (is (= first-key second-key))
    (is (not= (select-keys (first occurrences)
                           [:adapted-form-id :adapted-token-id])
              (select-keys (second occurrences)
                           [:adapted-form-id :adapted-token-id])))
    (is (true? (private-call
                'sh03-reader-decimal-occurrence-projection-shape-valid?
                projection)))
    (is (= first-key
           (get-in (private-call 'p15-s23-sh07-p-decimal-occurrence
                                 first-evidence)
                   [:semantic-value-entry :semantic-key])))
    (is (= second-key
           (get-in (private-call 'p15-s23-sh07-o-decimal-occurrence
                                 second-evidence)
                   [:semantic-value-entry :semantic-key])))
    (is (= :exact-normalized
           (:status (bootstrap/p15-s23-sh07-p-decimal-c2-branch
                     "1.2E+0" first-key first-evidence))))
    (is (= :normalization-deferred
           (:status (bootstrap/p15-s23-sh07-o-decimal-c2-branch
                     "1E+1000000000000000000"
                     deferred-key
                     (occurrence-evidence artifact (nth occurrences 2))))))))

(deftest v3-consumers-reject-legacy-source-projection-identity-and-replay-tampering
  (let [source-text "[1.2E+0 1.20E+0 1E+1000000000000000000]\n"
        products (decimal-products source-text)
        projection (:sh07-decimal-occurrence-projection products)
        artifact (c2-artifact products)
        occurrence (first (:occurrences projection))
        evidence (occurrence-evidence artifact occurrence)
        p-occurrence #(private-call 'p15-s23-sh07-p-decimal-occurrence %)
        tampered-projection
        (assoc-in artifact [:sh07-decimal-occurrence-projection
                            :adapted-source-unit-id]
                  "sha256:0000000000000000000000000000000000000000000000000000000000000000")
        tampered-source-base
        (assoc-in artifact [:form-tree 1 :raw] "9.9E+0")
        tampered-source
        (assoc tampered-source-base :artifact-id
               (bootstrap/c2-reader-artifact-id tampered-source-base))
        stale-replay-evidence
        (into {}
              (map (fn [candidate]
                     [[(:adapted-form-id candidate)
                       (:adapted-token-id candidate)] evidence])
                   (:occurrences projection)))
        replay-carrier {:c2-reader-artifact artifact
                        :root-adapted-form-id :form-0
                        :decimal-replay-evidence stale-replay-evidence}
        replay-result
        (private-call 'p15-s23-sh07-o-c2-decoration replay-carrier)]
    (testing "legacy V2 adapters never become accepted joins"
      (is (= :source-integrity-mismatch
             (:reason
              (failure-data
               #(p-occurrence
                 (assoc-in evidence
                           [:c2-reader-artifact :gravity-reader-boundary
                            :adapter-contract]
                           :gravity/sh03-to-c2-reader-products-v2)))))))
    (testing "projection and source tampering fail closed"
      (is (= :source-integrity-mismatch
             (:reason (failure-data #(p-occurrence
                                      {:c2-reader-artifact tampered-projection
                                       :adapted-form-id (:adapted-form-id occurrence)
                                       :adapted-token-id (:adapted-token-id occurrence)})))))
      (is (false?
           (private-call 'p15-s23-sh07-p-decimal-c2-evidence?
                         "1.2E+0"
                         (get-in occurrence [:semantic-value-entry :semantic-key])
                         (assoc evidence :c2-reader-artifact tampered-source)))))
    (testing "stale artifact identities and replay identities are rejected"
      (is (= :source-integrity-mismatch
             (:reason (failure-data #(p-occurrence
                                      (assoc evidence
                                             :c2-reader-artifact
                                             (assoc artifact
                                                    :artifact-id
                                                    "sha256:0000000000000000000000000000000000000000000000000000000000000000")))))))
      (is (= :failed (:status replay-result)))
      (is (= :unsupported-decimal-readable-spelling
             (:reason replay-result))))))
