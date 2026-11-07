(ns gravity.backend.gpu.native-manifest-test
  "Cold unverified document-data tests; no Bootstrap or source-builder dependency."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.gpu :as gpu]
            [gravity.digest :as digest]))

(def ^:private kernel-value (gpu/native-kernel-text))
(def ^:private stub-value (gpu/native-host-stub-text))
(def ^:private diagnostic-values
  ["B8-TARGET" "B8-KERNEL" "B8-HOST-EFFECT" "B8-MEMORY" "B8-TRANSFER"
   "B8-SYNC" "B8-ATOMIC" "B8-LAUNCH" "B8-MATH" "B8-MANIFEST"])
(defn- hash-text [text] (digest/sha256-hex text))
(defn- kernel-valid? [text] (gpu/native-kernel-structurally-valid? text))
(defn- stub-valid? [text] (gpu/native-host-stub-structurally-valid? text))
(defn- publish-refusal [id source-path subject extra]
  (throw (ex-info "Caller-owned unverified document refusal"
                  (merge {:id id :source-path source-path :subject subject} extra))))
(defn- ports []
  {:artifact-id #'hash-text :kernel-text #'kernel-value :host-stub-text #'stub-value
   :kernel-structurally-valid? #'kernel-valid? :host-stub-structurally-valid? #'stub-valid?
   :diagnostic-ids #'diagnostic-values :fail! #'publish-refusal})
(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :specialized-lowering-artifact
   {:kind :gravity/stage0-specialized-lowering-artifact :capability-based-proof {:status :complete}}
   :gpu-backend-manifest (gpu/native-manifest (ports) "unverified-source.gravity" {:fixture :unverified-input})
   :b8-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (gpu/validate-native-manifest! (ports) "gpu-manifest-fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-two-fixed-document-texts-with-deterministic-caller-hashes
  (let [input-id {:fixture :unverified-input :id "gpu-document-fixture"}
        path "unverified-gpu-source.gravity"
        manifest (gpu/native-manifest (ports) path input-id)
        kernel (get-in manifest [:kernel-ir-or-target-modules 0 :content])
        stub (get-in manifest [:host-stub-artifact :content])
        kernel-file (io/file "target/validation/gpu-native-manifest/gravity_stage0_gpu.spvasm")
        stub-file (io/file "target/validation/gpu-native-manifest/gravity_stage0_gpu_host.c")]
    (is (= manifest (gpu/native-manifest (ports) path input-id)))
    (is (identical? input-id (:input-artifact manifest)))
    (is (identical? input-id (get-in manifest [:source-debug-map :source])))
    (is (= [(str path ":kernel") (str path ":transfer") (str path ":sync")]
           (get-in manifest [:source-debug-map :source-spans])))
    (is (= [:mir :c14-target-lowering :b1-interface :b8-gpu-backend]
           (get-in manifest [:source-debug-map :generated-origin-chain])))
    (is (= kernel-value (gpu/native-kernel-text) kernel))
    (is (= stub-value (gpu/native-host-stub-text) stub))
    (is (= (digest/sha256-hex kernel) (get-in manifest [:kernel-ir-or-target-modules 0 :hash])
           (get-in manifest [:device-binary-or-intermediate-artifacts 0 :hash])))
    (is (= (digest/sha256-hex stub) (get-in manifest [:host-stub-artifact :hash])))
    (is (= :requires-proof-command (get-in manifest [:device-binary-or-intermediate-artifacts 0 :status])))
    (is (= :not-available-in-current-environment (get-in manifest [:spirv-validation-record :status])))
    (doseq [[file text] [[kernel-file kernel] [stub-file stub]]]
      (io/make-parents file)
      (spit file text)
      (is (= text (slurp file))))))

(deftest preserves-required-and-forbidden-substring-policies-and-known-gaps
  (let [kernel-tokens ["spirv.module" "gpu.func @gravity_stage0_kernel" "gravity.profile = \"gpu\""
                       "gravity.proof" "memref.load" "gpu.barrier" "memref.store"]
        stub-tokens ["gravity_gpu_copy_host_to_device" "gravity_gpu_launch" "gravity_gpu_copy_device_to_host"]]
    (is (true? (gpu/native-kernel-structurally-valid? kernel-value)))
    (is (true? (gpu/native-host-stub-structurally-valid? stub-value)))
    (doseq [token kernel-tokens]
      (is (false? (gpu/native-kernel-structurally-valid? (str/replace kernel-value token "removed")))))
    (doseq [token stub-tokens]
      (is (false? (gpu/native-host-stub-structurally-valid? (str/replace stub-value token "removed")))))
    (doseq [token ["host_alloc" "throw" "eval"]]
      (is (false? (gpu/native-kernel-structurally-valid? (str kernel-value token)))))
    (is (false? (gpu/native-host-stub-structurally-valid? (str stub-value "malloc"))))
    (is (true? (gpu/native-kernel-structurally-valid? (str/join " " kernel-tokens))))
    (is (true? (gpu/native-host-stub-structurally-valid? (str/join " " stub-tokens))))))

(deftest rejects-the-existing-document-shape-conditions
  (let [data (unverified-data) manifest :gpu-backend-manifest]
    (is (= :complete (gpu/validate-native-manifest! (ports) "fixture.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :specialized-lowering-artifact) "B8-MANIFEST" :specialized-lowering-artifact]
             [(assoc-in data [:specialized-lowering-artifact :capability-based-proof :status] :forged)
              "B8-MANIFEST" :specialized-lowering-proof]
             [(assoc-in data [manifest :target :api] :forged) "B8-TARGET" :target-api]
             [(assoc-in data [manifest :host-device-boundary-artifact :status] :missing) "B8-KERNEL" :host-device-boundary]
             [(assoc-in data [manifest :kernel-lowering-map :status] :missing) "B8-KERNEL" :kernel-lowering-map]
             [(assoc-in data [manifest :device-memory-lifetime-report :status] :missing) "B8-MEMORY" :device-memory-lifetime]
             [(assoc-in data [manifest :transfer-graph :status] :missing) "B8-TRANSFER" :transfer-graph]
             [(assoc-in data [manifest :synchronization-graph :status] :missing) "B8-SYNC" :synchronization-graph]
             [(assoc-in data [manifest :synchronization-graph :atomics] []) "B8-ATOMIC" :atomic-memory-scope]
             [(assoc-in data [manifest :launch-descriptor :status] :missing) "B8-LAUNCH" :launch-descriptor]
             [(assoc-in data [manifest :target-feature-and-occupancy-report :status] :missing) "B8-LAUNCH" :occupancy-report]
             [(assoc-in data [manifest :math-certificate-bundle :status] :missing) "B8-MATH" :math-certificate-bundle]
             [(assoc-in data [manifest :source-debug-map :status] :missing) "B8-MANIFEST" :source-debug-map]
             [(update data manifest dissoc :spirv-validation-record) "B8-MANIFEST" :gpu-artifact-manifest]
             [(assoc-in data [:b8-diagnostic-stream :diagnostics] []) "B8-MANIFEST" :b8-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= "gpu-manifest-fixture.gravity" (:source-path result)))))))

(deftest preserves-all-seventeen-failure-steps-with-returning-callbacks
  (let [calls (atom []) manifest {} data {:gpu-backend-manifest manifest}
        supplied (assoc (ports) :kernel-text "invalid" :host-stub-text "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (gpu/validate-native-manifest! supplied "ordered.gravity" data)))
    (is (= ["B8-MANIFEST" "B8-MANIFEST" "B8-TARGET" "B8-KERNEL" "B8-KERNEL"
            "B8-MEMORY" "B8-TRANSFER" "B8-SYNC" "B8-ATOMIC" "B8-LAUNCH" "B8-LAUNCH"
            "B8-MATH" "B8-MANIFEST" "B8-KERNEL" "B8-MANIFEST" "B8-MANIFEST" "B8-MANIFEST"]
           (mapv first @calls)))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof] [:target-api]
            [:host-device-boundary] [:kernel-lowering-map] [:device-memory-lifetime] [:transfer-graph]
            [:synchronization-graph] [:atomic-memory-scope] [:launch-descriptor] [:occupancy-report]
            [:math-certificate-bundle] [:source-debug-map] [:kernel-module-structure]
            [:host-stub-structure] [:gpu-artifact-manifest] [:b8-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? manifest (nth % 2)) (take 14 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest propagates-the-caller-exception-without-running-later-checks
  (let [error (ex-info "Caller identity" {:owner :test}) calls (atom [])]
    (is (identical? error
                    (try (gpu/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error)))
                          "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B8-MANIFEST" "identity.gravity" nil {:missing-fields [:specialized-lowering-artifact]}]] @calls))))

(deftest resolves-failure-vars-at-each-original-use
  (let [error (Error. "Late caller failure") calls (atom [])
        late (fn [& args] (swap! calls conj args) (throw error))]
    (with-redefs [publish-refusal (fn [& args] (swap! calls conj args)
                                  (alter-var-root #'publish-refusal (constantly late)))]
      (is (identical? error
                      (try (gpu/validate-native-manifest! (ports) "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest preserves-separate-kernel-and-stub-hash-and-content-reads
  (let [kernel kernel-value stub stub-value calls (atom [])
        kernel1 (str kernel "\n// first change\n") stub1 (str stub "\n// first change\n")
        kernel2 (str kernel "\n// second change\n") stub2 (str stub "\n// second change\n")]
    (with-redefs [kernel-value kernel stub-value stub
                  hash-text (fn [text]
                              (swap! calls conj [:first text])
                              (alter-var-root #'kernel-value (constantly kernel1))
                              (alter-var-root #'stub-value (constantly stub1))
                              (alter-var-root #'hash-text
                                              (constantly (fn [next-text]
                                                            (swap! calls conj [:second next-text])
                                                            (alter-var-root #'kernel-value (constantly kernel2))
                                                            (alter-var-root #'stub-value (constantly stub2))
                                                            (digest/sha256-hex next-text))))
                              (digest/sha256-hex text))]
      (let [manifest (gpu/native-manifest (ports) "late-source.gravity" :unverified-input)]
        (is (= [[:first kernel] [:second stub1]] @calls))
        (is (= kernel2 (get-in manifest [:kernel-ir-or-target-modules 0 :content])))
        (is (= stub2 (get-in manifest [:host-stub-artifact :content])))
        (is (= (digest/sha256-hex kernel) (get-in manifest [:kernel-ir-or-target-modules 0 :hash])
               (get-in manifest [:device-binary-or-intermediate-artifacts 0 :hash])))
        (is (= (digest/sha256-hex stub1) (get-in manifest [:host-stub-artifact :hash])))
        (is (not= (digest/sha256-hex kernel2) (get-in manifest [:kernel-ir-or-target-modules 0 :hash])))
        (is (not= (digest/sha256-hex stub2) (get-in manifest [:host-stub-artifact :hash])))))))

(deftest preserves-late-predicate-and-host-stub-value-interposition
  (let [data (unverified-data) calls (atom []) replacement (str stub-value "\n// late stub\n")]
    (with-redefs [stub-value stub-value stub-valid? stub-valid?
                  kernel-valid? (fn [text]
                                  (swap! calls conj [:kernel text])
                                  (alter-var-root #'stub-value (constantly replacement))
                                  (alter-var-root #'stub-valid? (constantly (fn [stub] (swap! calls conj [:stub stub]) false)))
                                  true)]
      (is (= "B8-MANIFEST" (:id (refusal data))))
      (is (= [[:kernel kernel-value] [:stub replacement]] @calls)))))

(deftest selects-the-failure-function-before-its-subject-lookup
  (let [events (atom []) armed (atom false) error (ex-info "Original caller" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b8-diagnostic-stream left))
                       (reset! armed false)
                       (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late caller"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error
                      (try (gpu/validate-native-manifest! (ports) "selection.gravity" data)
                           nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest keeps-forged-document-completion-distinct-from-authentication
  (let [data (unverified-data)
        forged (-> data
                   (assoc-in [:gpu-backend-manifest :kernel-ir-or-target-modules] [{:content "not a kernel" :hash "forged"}])
                   (assoc-in [:gpu-backend-manifest :host-stub-artifact] {:content "not C" :hash "forged"})
                   (assoc-in [:gpu-backend-manifest :launch-descriptor :workgroup-size] [-1 :forged nil]))
        empty-report (gpu/native-coverage-report (ports) {})]
    (is (= :complete (gpu/validate-native-manifest! (ports) "forged.gravity" forged)))
    (is (= :complete (:status empty-report)))
    (is (false? (:backend-interface-input-verified? empty-report)))
    (is (false? (:manifest-complete? empty-report)))
    (is (false? (:diagnostics-covered? empty-report)))
    (is (true? (:host-effect-absence-covered? empty-report)))
    (is (= :not-available-in-current-environment
           (:external-spirv-validation? (gpu/native-coverage-report (ports) data))))))

(deftest public-apis-describe-the-non-admitting-boundary
  (is (= 10 (count (ns-publics 'gravity.backend.gpu))))
  (doseq [operation [#'gpu/native-kernel-text #'gpu/native-host-stub-text
                     #'gpu/native-kernel-structurally-valid? #'gpu/native-host-stub-structurally-valid?
                     #'gpu/native-manifest #'gpu/validate-native-manifest! #'gpu/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.gpu (:namespace contract)))
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? contract)))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by])))
      (is (contains? entry :operation-inputs)))))
