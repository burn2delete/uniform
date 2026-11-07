(ns gravity.lowering.native-backend-test
  "Native backend document data only; no source admission or target execution."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.digest :as digest]
            [gravity.lowering :as lowering]))

(def ^:private required-fields
  [:schema-version :kind :backend :profile :target :content-hash :inputs :evidence :provenance])
(def ^:private diagnostic-values ["fixture:declared-native-catalog"])
(defn- artifact-id [value] (str "sha256:" (digest/sha256-hex (pr-str value))))
(defn- publish-refusal [id path subject extra]
  (throw (ex-info "Caller-owned native backend document refusal"
                  (merge {:id id :source-path path :subject subject} extra))))
(defn- ports [] {:artifact-id #'artifact-id :fail! #'publish-refusal
                :manifest-required-fields #'required-fields :diagnostic-ids #'diagnostic-values})
(defn- unverified-data []
  {:fixture :unverified-native-backend-data
   :backend-interface-artifact {:capability-based-proof {:status :complete}}
   :target-lowering-manifest (mapv #(hash-map :backend %) [:gravity.backend/c :gravity.backend/llvm :gravity.backend/mlir])
   :c-backend {:status :complete :ub-rejection {:status :rejected}}
   :llvm-backend {:status :complete :metadata-policy {:status :gated}}
   :mlir-backend {:status :complete :verifier-report {:status :passed}
                  :conversion-legality-report {:status :passed}}
   :artifact-manifests
   (mapv #(lowering/native-backend-artifact-manifest (ports) % :fixture "unverified content" :input :evidence)
         [:gravity.backend/c :gravity.backend/llvm :gravity.backend/mlir])
   :metadata-preservation-report {:status :preserved}
   :backend-conformance-record {:status :passed}
   :native-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (lowering/validate-native-backends! (ports) "fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-an-actual-deterministic-manifest-with-borrowed-identities
  (let [input {:fixture :unverified-input} evidence {:fixture :unverified-evidence}
        content "native backend document fixture\n"
        manifest (lowering/native-backend-artifact-manifest (ports) :gravity.backend/c :fixture content input evidence)
        file (io/file "target/validation/lowering-native-backend/manifest.edn")]
    (is (= manifest (lowering/native-backend-artifact-manifest (ports) :gravity.backend/c :fixture content input evidence)))
    (is (= (artifact-id content) (:content-hash manifest)))
    (doseq [key [:source :mir :backend-interface]] (is (identical? input (get-in manifest [:inputs key]))))
    (is (identical? evidence (get-in manifest [:evidence :proofs])))
    (is (= :native (:profile manifest)))
    (is (= :x86_64-stage0 (:target manifest)))
    (is (= ["C14" "B1" "B2/B3/B7" "B13" "B14"] (get-in manifest [:provenance :passes])))
    (is (= "gravity-stage0-clojure" (get-in manifest [:provenance :compiler])))
    (is (= :none (get-in manifest [:reproducibility :timestamp-policy])))
    (io/make-parents file) (spit file (pr-str manifest))
    (is (= manifest (edn/read-string (slurp file))))))

(deftest all-thirteen-record-conditions-have-stable-refusals
  (let [data (unverified-data)]
    (is (= :complete (lowering/validate-native-backends! (ports) "good.gravity" data)))
    (doseq [[bad id missing]
            [[(assoc-in data [:backend-interface-artifact :capability-based-proof :status] :missing) "B13-EVIDENCE" :backend-interface-proof]
             [(assoc data :target-lowering-manifest []) "B14-COVERAGE" :native-backends]
             [(assoc-in data [:c-backend :status] :missing) "B2-MANIFEST" :c-backend]
             [(assoc-in data [:c-backend :ub-rejection :status] :forged) "B2-UB" :ub-rejection]
             [(assoc-in data [:llvm-backend :status] :missing) "B3-MANIFEST" :llvm-backend]
             [(assoc-in data [:llvm-backend :metadata-policy :status] :missing) "B3-METADATA" :llvm-metadata-policy]
             [(assoc-in data [:mlir-backend :status] :missing) "B7-MANIFEST" :mlir-backend]
             [(assoc-in data [:mlir-backend :verifier-report :status] :missing) "B7-VERIFY" :mlir-verifier]
             [(update-in data [:artifact-manifests 0] dissoc :backend) "B13-SCHEMA" :artifact-manifest-schema]
             [(assoc-in data [:artifact-manifests 0 :content-hash] "wrong-prefix") "B13-HASH" :content-hash]
             [(assoc-in data [:metadata-preservation-report :status] :missing) "B14-METADATA" :metadata-preservation]
             [(assoc-in data [:backend-conformance-record :status] :missing) "B14-EVIDENCE" :conformance-evidence]
             [(assoc-in data [:native-diagnostic-stream :diagnostics] []) "B14-NEGATIVE" :native-diagnostics]]]
      (let [actual (refusal bad)]
        (is (= id (:id actual)))
        (is (= [missing] (:missing-fields actual)))
        (is (= "fixture.gravity" (:source-path actual)))))))

(deftest returning-failure-preserves-all-thirteen-steps
  (let [calls (atom []) manifests [{:content-hash "bad"}]
        data {:artifact-manifests manifests}
        operations (assoc (ports) :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (lowering/validate-native-backends! operations "ordered.gravity" data)))
    (is (= ["B13-EVIDENCE" "B14-COVERAGE" "B2-MANIFEST" "B2-UB" "B3-MANIFEST" "B3-METADATA"
            "B7-MANIFEST" "B7-VERIFY" "B13-SCHEMA" "B13-HASH" "B14-METADATA" "B14-EVIDENCE" "B14-NEGATIVE"]
           (mapv first @calls)))
    (is (= [[:backend-interface-proof] [:native-backends] [:c-backend] [:ub-rejection] [:llvm-backend]
            [:llvm-metadata-policy] [:mlir-backend] [:mlir-verifier] [:artifact-manifest-schema] [:content-hash]
            [:metadata-preservation] [:conformance-evidence] [:native-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (identical? (first manifests) (nth (nth @calls 8) 2)))
    (is (identical? (first manifests) (nth (nth @calls 9) 2)))
    (is (every? nil? (map #(nth % 2) (concat (take 8 @calls) (drop 10 @calls)))))))

(deftest exact-thrown-object-and-first-failure-are-preserved
  (let [error (Error. "Original native backend failure") calls (atom [])]
    (is (identical? error
                    (try (lowering/validate-native-backends!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error))) "throw.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B13-EVIDENCE" "throw.gravity" nil {:missing-fields [:backend-interface-proof]}]] @calls))))

(deftest failure-operation-is-read-at-each-original-use
  (let [calls (atom []) error (Error. "Late native backend failure")]
    (with-redefs [publish-refusal
                  (fn [& args] (swap! calls conj args)
                    (alter-var-root #'publish-refusal
                                    (constantly (fn [& later] (swap! calls conj later) (throw error)))))]
      (is (identical? error (try (lowering/validate-native-backends! (ports) "late.gravity" {})
                                 nil (catch Throwable actual actual)))))
    (is (= ["B13-EVIDENCE" "B14-COVERAGE"] (mapv first @calls)))))

(deftest hash-operation-is-late-and-its-exact-result-is-retained
  (let [calls (atom []) token (Object.) content (Object.)]
    (with-redefs [artifact-id (fn [actual] (swap! calls conj actual) token)]
      (let [manifest (lowering/native-backend-artifact-manifest (ports) :fixture :fixture content :input :proof)]
        (is (= [content] @calls))
        (is (identical? token (:content-hash manifest)))))
    (let [error (Error. "Caller hash failed")]
      (with-redefs [artifact-id (fn [_] (throw error))]
        (is (identical? error
                        (try (lowering/native-backend-artifact-manifest (ports) nil nil nil nil nil)
                             nil (catch Throwable actual actual))))))))

(deftest required-fields-and-diagnostic-values-remain-live
  (let [data (-> (unverified-data)
                 (assoc-in [:backend-interface-artifact :capability-based-proof :status] :missing)
                 (update-in [:artifact-manifests 0] dissoc :backend))
        calls (atom [])]
    (with-redefs [required-fields required-fields diagnostic-values diagnostic-values
                  publish-refusal (fn [& args]
                                    (swap! calls conj args)
                                    (alter-var-root #'required-fields (constantly []))
                                    (alter-var-root #'diagnostic-values (constantly [])))]
      (is (= :complete (lowering/validate-native-backends! (ports) "live.gravity" data))))
    (is (= ["B13-EVIDENCE" "B14-NEGATIVE"] (mapv first @calls)))))

(deftest required-fields-are-read-again-for-each-manifest
  (let [data (unverified-data) original required-fields]
    (with-redefs [required-fields (lazy-seq
                                   (alter-var-root #'required-fields (constantly (conj original :late-required)))
                                   (seq original))]
      (is (= "B13-SCHEMA" (:id (refusal data)))))))

(deftest failure-callee-is-selected-before-final-diagnostic-subject-is-read
  (let [armed (atom false) events (atom []) error (Error. "Original selected failure")
        comparator (fn [left right]
                     (when (and @armed (= :native-diagnostic-stream left))
                       (reset! armed false) (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late failure"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error (try (lowering/validate-native-backends! (ports) "selected.gravity" data)
                                 nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest count-vacuity-prefix-and-duplicate-limitations-remain-explicit
  (let [data (unverified-data)
        empty-manifests (assoc data :artifact-manifests [])
        duplicated (assoc data :artifact-manifests (vec (repeat 3 (first (:artifact-manifests data)))))
        forged (assoc-in data [:artifact-manifests 0 :content-hash] "sha256:not-a-digest")]
    (doseq [supplied [empty-manifests duplicated forged]]
      (is (= :complete (lowering/validate-native-backends! (ports) "forged.gravity" supplied))))
    (is (false? (:artifact-emission-complete? (lowering/native-backends-coverage-report (ports) empty-manifests))))
    (is (true? (:artifact-emission-complete? (lowering/native-backends-coverage-report (ports) duplicated))))
    (is (true? (:artifact-emission-complete? (lowering/native-backends-coverage-report (ports) forged))))
    (is (= :complete (:status (lowering/native-backends-coverage-report (ports) {}))))
    (is (false? (:backend-interface-input-verified? (lowering/native-backends-coverage-report (ports) {}))))
    (is (false? (:mlir-conversion-verified?
                (lowering/native-backends-coverage-report (ports)
                  (assoc-in data [:mlir-backend :conversion-legality-report :status] :missing)))))
    (is (= :complete (lowering/validate-native-backends! (ports) "conversion.gravity"
                     (assoc-in data [:mlir-backend :conversion-legality-report :status] :missing))))))

(deftest malformed-hash-values-preserve-native-host-exceptions
  (let [data (assoc-in (unverified-data) [:artifact-manifests 0 :content-hash] nil)
        calls (atom [])]
    (is (instance? NullPointerException
                   (try (lowering/validate-native-backends! (assoc (ports) :fail! (fn [& args] (swap! calls conj args)))
                          "nil-hash.gravity" data)
                        nil (catch Throwable actual actual))))
    (is (= [] @calls))))

(deftest new-apis-declare-non-admitting-contracts
  (is (= 25 (count (ns-publics 'gravity.lowering))))
  (doseq [operation [#'lowering/native-backend-artifact-manifest #'lowering/validate-native-backends!
                     #'lowering/native-backends-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation)) entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? entry)))
      (is (= '[clojure.set] (get-in contract [:dependency-direction :requires])))
      (is (some #{'gravity.bootstrap} (get-in contract [:dependency-direction :must-not-require]))))))
