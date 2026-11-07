(ns gravity.backend.mlir.native-manifest-test
  "Fixed document data only; no source builder or authenticated backend packet."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.mlir :as mlir]
            [gravity.bootstrap :as bootstrap]))

(defn- ports []
  {:artifact-id #'bootstrap/c4-artifact-id
   :module-text #'bootstrap/b7-document-mlir-module
   :structurally-valid? #'bootstrap/b7-document-mlir-structurally-valid?
   :diagnostic-ids #'bootstrap/b7-document-diagnostic-ids
   :fail! #'bootstrap/b7-document-fail!})

(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :native-lowering-artifact
   {:kind :gravity/stage0-native-lowering-artifact :capability-based-proof {:status :complete}}
   :mlir-backend-manifest (bootstrap/b7-document-mlir-manifest "unverified-fixture-input")
   :b7-diagnostic-stream
   {:diagnostics (mapv #(hash-map :diagnostic %) bootstrap/b7-document-diagnostic-ids)}})

(defn- refusal [artifact]
  (try (bootstrap/b7-document-validate! "mlir-manifest-fixture.gravity" artifact)
       nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-fixed-document-text-with-deterministic-identity
  (let [input-id {:fixture :unverified-input :id "mlir-document-fixture"}
        manifest (mlir/native-manifest (ports) input-id)
        text (get-in manifest [:mlir-modules 0 :content])
        file (io/file "target/validation/mlir-native-manifest/gravity_stage0.mlir")]
    (is (= manifest (bootstrap/b7-document-mlir-manifest input-id)))
    (is (= manifest (mlir/native-manifest (ports) input-id)))
    (is (identical? input-id (:input-artifact manifest)))
    (is (identical? input-id (get-in manifest [:source-debug-map :source])))
    (is (= (mlir/native-module-text) bootstrap/b7-document-mlir-module text))
    (is (= (bootstrap/c4-artifact-id text) (get-in manifest [:mlir-modules 0 :hash])))
    (is (= :not-available-in-current-environment
           (get-in manifest [:mlir-verifier-report :external-toolchain])))
    (is (false? (get-in manifest [:semantic-authority-record :mlir-verifier-is-gravity-proof])))
    (io/make-parents file)
    (spit file text)
    (is (= text (slurp file)))
    (is (= (bootstrap/c4-artifact-id manifest)
           (bootstrap/c4-artifact-id (bootstrap/b7-document-mlir-manifest input-id))))))

(deftest substring-check-retains-every-required-token-and-its-known-limitation
  (let [tokens ["module attributes" "func.func @gravity_entry" "arith.constant" "arith.addi"
                "gravity.numeric_mode" "gravity.proof" "gravity.source_span" "loc("]
        text (mlir/native-module-text)]
    (is (true? (mlir/native-module-structurally-valid? text)))
    (doseq [token tokens]
      (is (false? (mlir/native-module-structurally-valid? (str/replace text token "removed")))))
    (is (true? (mlir/native-module-structurally-valid? (str/join " " tokens))))
    (is (= (bootstrap/b7-document-mlir-structurally-valid? text)
           (mlir/native-module-structurally-valid? text)))))

(deftest rejects-existing-document-shape-conditions-with-owner-diagnostics
  (let [data (unverified-data)
        manifest :mlir-backend-manifest]
    (is (= :complete (mlir/validate-native-manifest! (ports) "fixture.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :native-lowering-artifact) "B7-MANIFEST" :native-lowering-artifact]
             [(assoc-in data [:native-lowering-artifact :capability-based-proof :status] :forged)
              "B7-MANIFEST" :native-lowering-proof]
             [(assoc-in data [manifest :dialect-registry-manifest :status] :missing) "B7-DIALECT" :dialect-registry]
             [(assoc-in data [manifest :gravity-dialect-operation-schema :status] :missing) "B7-DIALECT" :operation-schema]
             [(assoc-in data [manifest :standard-dialect-fact-mapping :status] :missing) "B7-METADATA" :standard-dialect-mapping]
             [(assoc-in data [manifest :operation-and-type-mapping-record :status] :missing) "B7-METADATA" :operation-type-mapping]
             [(assoc-in data [manifest :conversion-target-and-legality-report :status] :missing) "B7-CONVERSION" :conversion-legality]
             [(assoc-in data [manifest :mlir-verifier-report :status] :missing) "B7-VERIFY" :verifier-report]
             [(assoc-in data [manifest :metadata-preservation-policy :status] :missing) "B7-METADATA" :metadata-policy]
             [(assoc-in data [manifest :downstream-handoff-manifests 0 :status] :missing) "B7-HANDOFF" :downstream-handoff]
             [(assoc-in data [manifest :pass-pipeline-log 0 :verifier-before] :missing) "B7-PASS" :pass-pipeline-log]
             [(update data manifest dissoc :source-debug-map) "B7-MANIFEST" :mlir-artifact-manifest]
             [(assoc-in data [:b7-diagnostic-stream :diagnostics] []) "B7-MANIFEST" :b7-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= :b7-mlir-backend-document (:diagnostic-family result)))
        (is (= :gravity.backend/mlir (:backend result)))
        (is (= :native (:profile result)))
        (is (= :rejected (:fallback-status result)))
        (is (= "mlir-manifest-fixture.gravity" (get-in result [:source-span :source])))))))

(deftest preserves-all-fourteen-refusal-steps-and-returning-callbacks
  (let [calls (atom [])
        supplied (assoc (ports) :module-text "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))
        data {:fixture :forged-unverified-document-data
              :mlir-backend-manifest {:downstream-handoff-manifests [{}] :pass-pipeline-log [{}]}}]
    (is (= :complete (mlir/validate-native-manifest! supplied "ordered.gravity" data)))
    (is (= ["B7-MANIFEST" "B7-MANIFEST" "B7-DIALECT" "B7-DIALECT" "B7-METADATA"
            "B7-METADATA" "B7-CONVERSION" "B7-VERIFY" "B7-METADATA" "B7-HANDOFF"
            "B7-PASS" "B7-VERIFY" "B7-MANIFEST" "B7-MANIFEST"] (mapv first @calls)))
    (is (= [[:native-lowering-artifact] [:native-lowering-proof] [:dialect-registry] [:operation-schema]
            [:standard-dialect-mapping] [:operation-type-mapping] [:conversion-legality] [:verifier-report]
            [:metadata-policy] [:downstream-handoff] [:pass-pipeline-log] [:mlir-module-structure]
            [:mlir-artifact-manifest] [:b7-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? (:mlir-backend-manifest data) (nth % 2)) (take 11 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest propagates-owner-exception-identity-at-the-first-refusal
  (let [error (ex-info "caller-owned refusal" {:identity :owner}) calls (atom [])]
    (is (identical? error
                    (try (mlir/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error)))
                          "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B7-MANIFEST" "identity.gravity" nil {:missing-fields [:native-lowering-artifact]}]] @calls))))

(deftest resolves-failure-var-at-each-original-use
  (let [error (Error. "late owner failure") calls (atom [])
        late (fn [& args] (swap! calls conj args) (throw error))]
    (with-redefs [bootstrap/b7-document-fail!
                  (fn [& args] (swap! calls conj args)
                    (alter-var-root #'bootstrap/b7-document-fail! (constantly late)))]
      (is (identical? error
                      (try (bootstrap/b7-document-validate! "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:native-lowering-artifact] [:native-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest preserves-late-module-reads-during-hash-callback-interposition
  (let [original bootstrap/b7-document-mlir-module replacement (str original "\n// late value\n")
        hash-inputs (atom []) hash-operation bootstrap/c4-artifact-id]
    (with-redefs [bootstrap/c4-artifact-id
                  (fn [text] (swap! hash-inputs conj text)
                    (alter-var-root #'bootstrap/b7-document-mlir-module (constantly replacement))
                    (hash-operation text))
                  bootstrap/b7-document-mlir-module original]
      (let [manifest (bootstrap/b7-document-mlir-manifest "late-value")]
        (is (= [original] @hash-inputs))
        (is (= replacement (get-in manifest [:mlir-modules 0 :content])))
        (is (= (hash-operation original) (get-in manifest [:mlir-modules 0 :hash])))
        (is (not= (hash-operation replacement) (get-in manifest [:mlir-modules 0 :hash])))))))

(deftest uses-late-structural-predicate-with-original-module-value
  (let [data (unverified-data) calls (atom [])]
    (with-redefs [bootstrap/b7-document-mlir-structurally-valid?
                  (fn [text] (swap! calls conj text) false)]
      (is (= "B7-VERIFY" (:id (refusal data))))
      (is (= [:mlir-module-structure] (:missing-fields (refusal data))))
      (is (false? (:mlir-structurally-valid? (bootstrap/b7-document-capability-proof data)))))
    (is (= (repeat 3 bootstrap/b7-document-mlir-module) @calls))))

(deftest records-legacy-coverage-vacuity-without-authentication
  (let [data (unverified-data)
        forged (-> data
                   (assoc-in [:mlir-backend-manifest :mlir-modules] [{:content "not MLIR" :hash "forged"}])
                   (assoc-in [:mlir-backend-manifest :downstream-handoff-manifests] [])
                   (assoc-in [:mlir-backend-manifest :pass-pipeline-log] []))
        empty-report (mlir/native-coverage-report (ports) {})]
    (is (= :complete (bootstrap/b7-document-validate! "forged-data.gravity" forged)))
    (is (= :complete (:status empty-report)))
    (is (true? (:pass-pipeline-verified? empty-report)))
    (is (false? (:backend-interface-input-verified? empty-report)))
    (is (false? (:manifest-complete? empty-report)))
    (is (false? (:diagnostics-covered? empty-report)))
    (is (= :not-available-in-current-environment
           (:external-mlir-toolchain-validation? (mlir/native-coverage-report (ports) data))))
    (is (= (bootstrap/b7-document-capability-proof data) (mlir/native-coverage-report (ports) data)))))

(deftest public-apis-describe-non-admitting-data-boundaries
  (is (= 9 (count (ns-publics 'gravity.backend.mlir))))
  (doseq [operation [#'mlir/native-module-text #'mlir/native-module-structurally-valid?
                     #'mlir/native-manifest #'mlir/validate-native-manifest! #'mlir/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.mlir (:namespace contract)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? contract)))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by])))
      (is (contains? entry :operation-inputs)))))
