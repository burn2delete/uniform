(ns gravity.backend.hdl.native-manifest-test
  "Cold unverified HDL document data; no Bootstrap or source-builder dependency."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.hdl :as hdl]
            [gravity.digest :as digest]))

(def ^:private diagnostic-values
  ["B9-TARGET" "B9-WIDTH" "B9-CLOCK" "B9-RESET" "B9-CDC"
   "B9-RUNTIME" "B9-UNBOUNDED" "B9-INTERFACE" "B9-TIMING" "B9-MANIFEST"])
(defn- hash-text [text] (digest/sha256-hex text))
(defn- publish-refusal [id source-path subject extra]
  (throw (ex-info "Caller-owned unverified HDL document refusal"
                  (merge {:id id :source-path source-path :subject subject} extra))))
(defn- ports []
  {:artifact-id #'hash-text :hdl-module #'hdl/hdl-module :testbench #'hdl/testbench
   :timing-constraints #'hdl/timing-constraints
   :hdl-structurally-valid? #'hdl/hdl-structurally-valid?
   :testbench-structurally-valid? #'hdl/testbench-structurally-valid?
   :timing-structurally-valid? #'hdl/timing-structurally-valid?
   :diagnostic-ids #'diagnostic-values :fail! #'publish-refusal})
(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :specialized-lowering-artifact
   {:kind :gravity/stage0-specialized-lowering-artifact :capability-based-proof {:status :complete}}
   :hdl-backend-manifest (hdl/native-manifest (ports) "unverified-source.gravity" {:fixture :unverified-input})
   :b9-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (hdl/validate-native-manifest! (ports) "hdl-manifest-fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-three-fixed-texts-with-deterministic-hashes-and-borrowed-identities
  (let [input-id {:fixture :unverified-input :id "hdl-document-fixture"}
        path "unverified-hardware-source.gravity"
        manifest (hdl/native-manifest (ports) path input-id)]
    (is (= manifest (hdl/native-manifest (ports) path input-id)))
    (is (identical? input-id (:input-artifact manifest)))
    (is (identical? input-id (get-in manifest [:hardware-ir-handoff-record :source-artifact])))
    (is (identical? input-id (get-in manifest [:source-debug-map :source])))
    (is (= [(str path ":hardware-ir") (str path ":state-machine")]
           (get-in manifest [:simulation-trace-schema :source-links])))
    (is (= (mapv #(str path %) [":hardware-ir" ":ports" ":state-machine" ":timing"])
           (get-in manifest [:source-debug-map :locations])))
    (is (= [:mir :c12-hardware-domain-ir :c14-target-lowering :b1-interface :b9-hdl-backend]
           (get-in manifest [:source-debug-map :generated-origin-chain])))
    (is (= :not-available-in-current-environment
           (get-in manifest [:external-synthesis-validation-record :status])))
    (doseq [[field text filename expected-sha]
            [[[:hdl-artifacts 0] hdl/hdl-module "gravity_stage0_hdl.sv"
              "bddf15e7fde20828f1a615c9f1fb78c50cecbbd2727a3d27a9f977d2d4858fa5"]
             [[:testbench] hdl/testbench "gravity_stage0_hdl_tb.sv"
              "9c4c046a82ca2f154c8d373d102cc30194e5b28199c3cbdf05c2172d6c397b1f"]
             [[:timing-constraint-file] hdl/timing-constraints "gravity_stage0.sdc"
              "a580b6b5b757d1d50e97c3e8049eaef9855342c7d718630866b4ce22cc527406"]]]
      (let [record (get-in manifest field) file (io/file "target/validation/hdl-native-manifest" filename)]
        (is (= text (:content record)))
        (is (= expected-sha (digest/sha256-hex text) (:hash record)))
        (io/make-parents file)
        (spit file text)
        (is (= text (slurp file)))))))

(deftest missing-reference-tokens-reject-at-the-original-text-checks
  (let [data (unverified-data)]
    (doseq [[text-var original tokens id missing]
            [[#'hdl/hdl-module hdl/hdl-module
              ["module gravity_stage0_hdl" "typedef enum logic [1:0]" "always_ff @(posedge clk)" "if (rst)" "assign output_ready"]
              "B9-MANIFEST" :hdl-structure]
             [#'hdl/testbench hdl/testbench
              ["module gravity_stage0_hdl_tb" "gravity_stage0_hdl dut" "repeat (" "$finish"]
              "B9-MANIFEST" :testbench-structure]
             [#'hdl/timing-constraints hdl/timing-constraints
              ["create_clock" "set_input_delay" "set_output_delay"]
              "B9-TIMING" :timing-constraint-structure]]]
      (doseq [token tokens]
        (with-redefs-fn {text-var (str/replace original token "removed")}
          (fn [] (let [result (refusal data)]
                   (is (= id (:id result)))
                   (is (= [missing] (:missing-fields result))))))))))

(deftest rejects-all-existing-document-record-conditions
  (let [data (unverified-data) m :hdl-backend-manifest]
    (is (= :complete (hdl/validate-native-manifest! (ports) "fixture.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :specialized-lowering-artifact) "B9-MANIFEST" :specialized-lowering-artifact]
             [(assoc-in data [:specialized-lowering-artifact :capability-based-proof :status] :forged) "B9-MANIFEST" :specialized-lowering-proof]
             [(assoc-in data [m :target :hdl] :forged) "B9-TARGET" :target-hdl]
             [(assoc-in data [m :hardware-ir-handoff-record :status] :missing) "B9-MANIFEST" :hardware-ir-handoff-record]
             [(assoc-in data [m :hdl-artifacts 0 :status] :missing) "B9-MANIFEST" :hdl-artifact]
             [(assoc-in data [m :width-and-numeric-report :status] :missing) "B9-WIDTH" :width-and-numeric-report]
             [(update-in data [m :interface-port-schema :ports 0] dissoc :width) "B9-WIDTH" :port-widths]
             [(assoc-in data [m :clock-domain-report :status] :missing) "B9-CLOCK" :clock-domain-report]
             [(assoc-in data [m :reset-domain-report :status] :missing) "B9-RESET" :reset-domain-report]
             [(assoc-in data [m :cdc-report :status] :missing) "B9-CDC" :cdc-report]
             [(assoc-in data [m :cdc-report :unmediated-crossings] [:unmediated]) "B9-CDC" :cdc-report]
             [(assoc-in data [m :runtime-construct-rejection-report :status] :missing) "B9-RUNTIME" :runtime-rejection-report]
             [(assoc-in data [m :state-machine-graph :status] :missing) "B9-UNBOUNDED" :state-machine-graph]
             [(assoc-in data [m :interface-port-schema :status] :missing) "B9-INTERFACE" :interface-port-schema]
             [(assoc-in data [m :timing-constraint-file :status] :missing) "B9-TIMING" :timing-constraint-file]
             [(assoc-in data [m :simulation-trace-schema :status] :missing) "B9-MANIFEST" :simulation-trace-schema]
             [(assoc-in data [m :source-debug-map :status] :missing) "B9-MANIFEST" :source-debug-map]
             [(assoc-in data [m :hardware-audit-records 0 :status] :missing) "B9-MANIFEST" :hardware-audit-records]
             [(update data m dissoc :memory-block-manifest) "B9-MANIFEST" :hdl-artifact-manifest]
             [(assoc-in data [:b9-diagnostic-stream :diagnostics] []) "B9-MANIFEST" :b9-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= "hdl-manifest-fixture.gravity" (:source-path result)))))))

(deftest preserves-all-twenty-two-refusal-steps-and-callback-subjects
  (let [calls (atom []) manifest {:interface-port-schema {:ports [{}]} :hardware-audit-records [{}]}
        data {:hdl-backend-manifest manifest}
        supplied (assoc (ports) :hdl-module "invalid" :testbench "invalid" :timing-constraints "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (hdl/validate-native-manifest! supplied "ordered.gravity" data)))
    (is (= ["B9-MANIFEST" "B9-MANIFEST" "B9-TARGET" "B9-MANIFEST" "B9-MANIFEST" "B9-MANIFEST"
            "B9-WIDTH" "B9-WIDTH" "B9-CLOCK" "B9-RESET" "B9-CDC" "B9-RUNTIME" "B9-UNBOUNDED"
            "B9-INTERFACE" "B9-TIMING" "B9-TIMING" "B9-MANIFEST" "B9-MANIFEST" "B9-MANIFEST"
            "B9-MANIFEST" "B9-MANIFEST" "B9-MANIFEST"] (mapv first @calls)))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof] [:target-hdl]
            [:hardware-ir-handoff-record] [:hdl-artifact] [:hdl-structure] [:width-and-numeric-report]
            [:port-widths] [:clock-domain-report] [:reset-domain-report] [:cdc-report] [:runtime-rejection-report]
            [:state-machine-graph] [:interface-port-schema] [:timing-constraint-file] [:timing-constraint-structure]
            [:testbench-structure] [:simulation-trace-schema] [:source-debug-map] [:hardware-audit-records]
            [:hdl-artifact-manifest] [:b9-diagnostics]] (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? manifest (nth % 2)) (take 19 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest retains-first-failure-exception-identity
  (let [error (ex-info "Caller identity" {:owner :test}) calls (atom [])]
    (is (identical? error
                    (try (hdl/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error))) "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B9-MANIFEST" "identity.gravity" nil {:missing-fields [:specialized-lowering-artifact]}]] @calls))))

(deftest failure-var-is-resolved-at-every-original-use
  (let [error (Error. "Late caller failure") calls (atom [])
        late (fn [& args] (swap! calls conj args) (throw error))]
    (with-redefs [publish-refusal (fn [& args] (swap! calls conj args)
                                  (alter-var-root #'publish-refusal (constantly late)))]
      (is (identical? error
                      (try (hdl/validate-native-manifest! (ports) "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest hash-order-and-content-order-are-distinct-and-preserved
  (let [events (atom []) values {:hdl-module hdl/hdl-module :testbench hdl/testbench :timing-constraints hdl/timing-constraints}
        lookup (fn [key default]
                 (swap! events conj [:lookup key])
                 (if (= :artifact-id key)
                   (fn [text] (swap! events conj [:hash text]) (hash-text text))
                   (get values key default)))
        supplied (reify clojure.lang.ILookup
                   (valAt [_ key] (lookup key nil))
                   (valAt [_ key default] (lookup key default)))]
    (hdl/native-manifest supplied "order.gravity" :unverified-id)
    (is (= [[:lookup :artifact-id] [:lookup :hdl-module] [:hash hdl/hdl-module]
            [:lookup :artifact-id] [:lookup :testbench] [:hash hdl/testbench]
            [:lookup :artifact-id] [:lookup :timing-constraints] [:hash hdl/timing-constraints]
            [:lookup :hdl-module] [:lookup :timing-constraints] [:lookup :testbench]] @events))))

(deftest three-hash-calls-retain-late-function-and-value-interposition
  (let [originals [hdl/hdl-module hdl/testbench hdl/timing-constraints] calls (atom [])
        changed (fn [number] (mapv #(str % "\n// change " number "\n") originals))
        set-texts! (fn [texts]
                     (doseq [[operation text] (map vector [#'hdl/hdl-module #'hdl/testbench #'hdl/timing-constraints] texts)]
                       (alter-var-root operation (constantly text))))]
    (with-redefs [hdl/hdl-module hdl/hdl-module hdl/testbench hdl/testbench hdl/timing-constraints hdl/timing-constraints
                  hash-text (fn [text]
                              (swap! calls conj [:first text]) (set-texts! (changed 1))
                              (alter-var-root #'hash-text
                                              (constantly (fn [second-text]
                                                            (swap! calls conj [:second second-text]) (set-texts! (changed 2))
                                                            (alter-var-root #'hash-text
                                                                            (constantly (fn [third-text]
                                                                                          (swap! calls conj [:third third-text])
                                                                                          (set-texts! (changed 3))
                                                                                          (digest/sha256-hex third-text))))
                                                            (digest/sha256-hex second-text))))
                              (digest/sha256-hex text))]
      (let [manifest (hdl/native-manifest (ports) "late.gravity" :unverified-id)]
        (is (= [[:first (nth originals 0)] [:second (nth (changed 1) 1)] [:third (nth (changed 2) 2)]] @calls))
        (doseq [[field text hashed]
                [[[:hdl-artifacts 0] (nth (changed 3) 0) (nth originals 0)]
                 [[:testbench] (nth (changed 3) 1) (nth (changed 1) 1)]
                 [[:timing-constraint-file] (nth (changed 3) 2) (nth (changed 2) 2)]]]
          (is (= text (:content (get-in manifest field))))
          (is (= (digest/sha256-hex hashed) (:hash (get-in manifest field))))
          (is (not= (digest/sha256-hex text) (:hash (get-in manifest field)))))))))

(deftest validator-and-coverage-predicate-order-and-late-lookup-remain-observable
  (let [data (unverified-data) calls (atom [])
        supplied (assoc (ports)
                        :hdl-structurally-valid? (fn [_] (swap! calls conj :hdl) true)
                        :testbench-structurally-valid? (fn [_] (swap! calls conj :testbench) true)
                        :timing-structurally-valid? (fn [_] (swap! calls conj :timing) true))]
    (is (= :complete (hdl/validate-native-manifest! supplied "order.gravity" data)))
    (is (= [:hdl :timing :testbench] @calls))
    (reset! calls [])
    (is (= :complete (:status (hdl/native-coverage-report supplied data))))
    (is (= [:testbench :hdl :timing] @calls))
    (with-redefs [hdl/testbench-structurally-valid? hdl/testbench-structurally-valid?
                  hdl/timing-structurally-valid? (fn [_]
                                                 (alter-var-root #'hdl/testbench-structurally-valid? (constantly (fn [_] false)))
                                                 true)]
      (is (= [:testbench-structure] (:missing-fields (refusal data)))))))

(deftest failure-operation-is-selected-before-final-subject-evaluation
  (let [events (atom []) armed (atom false) error (ex-info "Original caller" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b9-diagnostic-stream left))
                       (reset! armed false) (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late caller"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error (try (hdl/validate-native-manifest! (ports) "selection.gravity" data)
                                 nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest legacy-empty-collections-and-present-invalid-widths-do-not-authenticate
  (let [data (unverified-data)
        forged (-> data
                   (assoc-in [:hdl-backend-manifest :hdl-artifacts 0 :content] "not HDL")
                   (assoc-in [:hdl-backend-manifest :testbench :content] "not a testbench")
                   (assoc-in [:hdl-backend-manifest :interface-port-schema :ports] [{:width -1}])
                   (assoc-in [:hdl-backend-manifest :hardware-audit-records] []))
        empty-ports (assoc-in forged [:hdl-backend-manifest :interface-port-schema :ports] [])
        report (hdl/native-coverage-report (ports) {})]
    (is (= :complete (hdl/validate-native-manifest! (ports) "forged.gravity" forged)))
    (is (= :complete (hdl/validate-native-manifest! (ports) "empty-ports.gravity" empty-ports)))
    (is (= :complete (:status report)))
    (is (false? (:backend-interface-input-verified? report)))
    (is (false? (:manifest-complete? report)))
    (is (false? (:diagnostics-covered? report)))
    (is (= :not-available-in-current-environment (:external-synthesis-validation? (hdl/native-coverage-report (ports) data))))))

(deftest three-new-apis-have-explicit-non-admitting-contracts
  (is (= 15 (count (ns-publics 'gravity.backend.hdl))))
  (doseq [operation [#'hdl/native-manifest #'hdl/validate-native-manifest! #'hdl/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.hdl (:namespace contract)))
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? entry)))
      (is (seq (:required-operations entry)))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by]))))))
