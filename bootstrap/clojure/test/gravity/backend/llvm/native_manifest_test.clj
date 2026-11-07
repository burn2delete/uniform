(ns gravity.backend.llvm.native-manifest-test
  "Legacy document data only; no source builder or authenticated backend packet."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.llvm :as llvm]
            [gravity.bootstrap :as bootstrap]))

(defn- ports []
  {:artifact-id #'bootstrap/c4-artifact-id
   :llvm-ir #'bootstrap/b3-document-llvm-ir
   :diagnostic-ids #'bootstrap/b3-document-diagnostic-ids
   :fail! #'bootstrap/b3-document-fail!})

(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :native-lowering-artifact
   {:kind :gravity/stage0-native-lowering-artifact :capability-based-proof {:status :complete}}
   :llvm-backend-manifest (bootstrap/b3-document-llvm-manifest "unverified-fixture-input")
   :b3-diagnostic-stream
   {:diagnostics (mapv #(hash-map :diagnostic %) bootstrap/b3-document-diagnostic-ids)}})

(defn- refusal [artifact]
  (try (bootstrap/b3-document-validate! "llvm-manifest-fixture.gravity" artifact)
       nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-real-fixed-llvm-document-data-with-deterministic-identity
  (let [input-id {:fixture :unverified-input :id "llvm-document-fixture"}
        manifest (llvm/native-manifest (ports) input-id)
        text (get-in manifest [:llvm-ir-files 0 :content])
        file (io/file "target/validation/llvm-native-manifest/gravity_stage0_b3.ll")]
    (is (= manifest (bootstrap/b3-document-llvm-manifest input-id)))
    (is (= manifest (llvm/native-manifest (ports) input-id)))
    (is (identical? input-id (:input-artifact manifest)))
    (is (identical? input-id (get-in manifest [:source-debug-map :source])))
    (is (= bootstrap/b3-document-llvm-ir text))
    (is (= (bootstrap/c4-artifact-id text) (get-in manifest [:llvm-ir-files 0 :hash])))
    (is (str/includes? text "define i64 @gravity_entry"))
    (is (= :requires-proof-command (get-in manifest [:llvm-verifier-record :status])))
    (io/make-parents file)
    (spit file text)
    (is (= text (slurp file)))
    (is (= (bootstrap/c4-artifact-id manifest)
           (bootstrap/c4-artifact-id (bootstrap/b3-document-llvm-manifest input-id))))))

(deftest rejects-every-existing-structural-condition-with-original-diagnostics
  (let [data (unverified-data)]
    (is (= :complete (llvm/validate-native-manifest! (ports) "fixture.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :native-lowering-artifact) "B3-MANIFEST" :native-lowering-artifact]
             [(assoc-in data [:native-lowering-artifact :capability-based-proof :status] :forged)
              "B3-MANIFEST" :native-lowering-proof]
             [(assoc-in data [:llvm-backend-manifest :target-record :status] :unpinned) "B3-TARGET" :target-record]
             [(assoc-in data [:llvm-backend-manifest :metadata-policy :status] :ungated) "B3-METADATA" :metadata-policy]
             [(assoc-in data [:llvm-backend-manifest :pointer-ownership-memory-map :status] :missing)
              "B3-POINTER" :pointer-ownership-memory-map]
             [(assoc-in data [:llvm-backend-manifest :numeric-floating-lowering :status] :missing)
              "B3-NUMERIC" :numeric-floating-lowering]
             [(assoc-in data [:llvm-backend-manifest :atomic-volatile-concurrency-lowering :status] :missing)
              "B3-ATOMIC" :atomic-volatile-lowering]
             [(assoc-in data [:llvm-backend-manifest :runtime-abi-helper-map :hidden-runtime-services] [:hidden])
              "B3-RUNTIME" :hidden-runtime-services]
             [(assoc-in data [:llvm-backend-manifest :target-record :calling-conventions] [])
              "B3-ABI" :calling-convention]
             [(assoc-in data [:llvm-backend-manifest :pass-pipeline-record :status] :missing)
              "B3-PASS" :pass-pipeline-record]
             [(update data :llvm-backend-manifest dissoc :source-debug-map) "B3-MANIFEST" :llvm-artifact-manifest]
             [(assoc-in data [:b3-diagnostic-stream :diagnostics] []) "B3-MANIFEST" :b3-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= :b3-llvm-backend-document (:diagnostic-family result)))
        (is (= :gravity.backend/llvm (:backend result)))
        (is (= :native (:profile result)))
        (is (= :rejected (:fallback-status result)))
        (is (= "llvm-manifest-fixture.gravity" (get-in result [:source-span :source])))))))

(deftest rejects-all-original-forbidden-fixture-ir-flags
  (let [data (unverified-data)]
    (doseq [flag [" nsw" " nuw" " inbounds" " fast" "!tbaa"]]
      (with-redefs [bootstrap/b3-document-llvm-ir (str bootstrap/b3-document-llvm-ir flag)]
        (let [result (refusal data)
              report (bootstrap/b3-document-capability-proof data)]
          (is (= "B3-UB" (:id result)))
          (is (= [:conservative-llvm-ir] (:missing-fields result)))
          (is (false? (:safe-llvm-undefined-behavior-rejected? report)))
          (is (= :complete (:status report))))))))

(deftest preserves-refusal-order-arguments-and-returning-failure
  (let [calls (atom [])
        supplied (assoc (ports) :fail! (fn [& args] (swap! calls conj args) false))
        bad {:fixture :unverified-empty-data}]
    (is (= :complete (llvm/validate-native-manifest! supplied "ordered.gravity" bad)))
    (is (= ["B3-MANIFEST" "B3-MANIFEST" "B3-TARGET" "B3-METADATA" "B3-POINTER"
            "B3-NUMERIC" "B3-ATOMIC" "B3-ABI" "B3-PASS" "B3-MANIFEST" "B3-MANIFEST"]
           (mapv first @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (= [[:native-lowering-artifact] [:native-lowering-proof] [:target-record] [:metadata-policy]
            [:pointer-ownership-memory-map] [:numeric-floating-lowering] [:atomic-volatile-lowering]
            [:calling-convention] [:pass-pipeline-record] [:llvm-artifact-manifest] [:b3-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? nil? (map #(nth % 2) @calls)))))

(deftest preserves-throwing-callback-identity-and-stops-at-the-original-condition
  (let [error (ex-info "caller-owned failure" {:identity :caller})
        calls (atom [])
        supplied (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error)))]
    (is (identical? error
                    (try (llvm/validate-native-manifest! supplied "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= 1 (count @calls)))
    (is (= ["B3-MANIFEST" "identity.gravity" nil {:missing-fields [:native-lowering-artifact]}]
           (first @calls)))))

(deftest resolves-failure-var-at-each-original-use
  (let [error (Error. "late owner failure")
        calls (atom [])
        late (fn [& args] (swap! calls conj args) (throw error))]
    (with-redefs [bootstrap/b3-document-fail!
                  (fn [& args]
                    (swap! calls conj args)
                    (alter-var-root #'bootstrap/b3-document-fail! (constantly late)))]
      (is (identical? error
                      (try (bootstrap/b3-document-validate! "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:native-lowering-artifact] [:native-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest retains-late-ir-value-reads-including-hash-callback-interposition
  (let [original bootstrap/b3-document-llvm-ir
        replacement (str original "\n; late fixture value\n")
        hash-inputs (atom [])
        hash-operation bootstrap/c4-artifact-id]
    (with-redefs [bootstrap/c4-artifact-id
                  (fn [text]
                    (swap! hash-inputs conj text)
                    (alter-var-root #'bootstrap/b3-document-llvm-ir (constantly replacement))
                    (hash-operation text))
                  bootstrap/b3-document-llvm-ir original]
      (let [manifest (bootstrap/b3-document-llvm-manifest "late-value")]
        (is (= [original] @hash-inputs))
        (is (= replacement (get-in manifest [:llvm-ir-files 0 :content])))
        (is (= (hash-operation original) (get-in manifest [:llvm-ir-files 0 :hash])))
        (is (not= (hash-operation replacement) (get-in manifest [:llvm-ir-files 0 :hash])))))))

(deftest selects-failure-before-evaluating-its-artifact-argument
  (let [events (atom []) armed (atom false)
        error (ex-info "original owner selected" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b3-diagnostic-stream left))
                       (reset! armed false)
                       (swap! events conj :argument)
                       (alter-var-root #'bootstrap/b3-document-fail!
                                       (constantly (fn [& _] (throw (Error. "late owner selected"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [bootstrap/b3-document-diagnostic-ids (lazy-seq (reset! armed true) nil)
                  bootstrap/b3-document-fail! (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error
                      (try (bootstrap/b3-document-validate! "selection.gravity" data)
                           nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest preserves-known-coverage-vacuity-without-authentication
  (let [data (unverified-data)
        forged (-> data
                   (assoc-in [:llvm-backend-manifest :target-record :triple] :forged)
                   (assoc-in [:llvm-backend-manifest :llvm-ir-files]
                             [{:content "not LLVM" :hash "forged"}])
                   (assoc-in [:llvm-backend-manifest :proof-to-llvm-metadata-map] :forged))
        empty-report (llvm/native-coverage-report (ports) {})]
    (is (= :complete (bootstrap/b3-document-validate! "forged-data.gravity" forged)))
    (is (= :complete (:status (bootstrap/b3-document-capability-proof forged))))
    (is (= :complete (:status empty-report)))
    (is (false? (:backend-interface-input-verified? empty-report)))
    (is (false? (:manifest-complete? empty-report)))
    (is (false? (:diagnostics-covered? empty-report)))
    (is (= (bootstrap/b3-document-capability-proof data) (llvm/native-coverage-report (ports) data)))))

(deftest public-apis-record-the-non-admitting-boundary
  (is (= 19 (count (ns-publics 'gravity.backend.llvm))))
  (doseq [operation [#'llvm/native-manifest #'llvm/validate-native-manifest! #'llvm/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.llvm (:namespace contract)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? contract)))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by])))
      (is (seq (:operation-inputs entry))))))
