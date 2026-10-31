(ns gravity.backend.c.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.c :as c]
            [gravity.bootstrap :as bootstrap]))

(defn- native [name & args]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b2-c17-" name))) args))

(defn- operation [id opcode operands type value]
  {:op-id id :block-id "entry" :opcode opcode :operands operands :type type
   :source-operation :fixture-data :constant-payload {:value value}
   :source {:span {:source "emission-data.gravity" :start {:line 1 :column 1}
                   :end {:line 1 :column 2}}
            :generated-origin [{:kind :fixture-expansion :origin-id (str "parent-" id)}]}
   :facts {:source-origin-id (str "origin-" id) :safety-proof-id (str "proof-" id)
           :proof-certificate-ids [(str "certificate-" id)]}})

(defn- operations []
  (vec (concat
        (map-indexed #(operation (str "constant-" %1) :constant []
                                 (cond (nil? %2) :gravity/nil (boolean? %2) :gravity/bool
                                       :else :gravity/int) %2)
                     [0 7 -7 Long/MIN_VALUE Long/MAX_VALUE true false nil])
        (map-indexed #(operation (str "forward-" %1) %2 ["constant-1"] :gravity/int nil)
                     [:local :local-binding :sequence :lexical-scope :function-boundary])
        (map-indexed #(operation (str "truth-" %1) :truthiness [%2] :gravity/bool nil)
                     ["constant-1" "constant-5" "constant-7"])
        (map-indexed #(operation (str "comparison-" %1) %2 ["constant-1" "constant-2"]
                                 :gravity/bool nil)
                     [:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte])
        [(operation "join" :conditional-join ["comparison-0" "constant-1" "constant-2"]
                    :gravity/int nil)])))

(defn- fixture []
  (let [ops (operations)]
    {:operations ops :index (zipmap (map :op-id ops) (range))
     :function {:blocks {"entry" {:terminator {:operands ["join"]}}}}
     :b1 {:artifact-id "fixture-b1-data" :target {:triple "fixture-target" :data-layout "fixture-layout"}
          :abi {:id "fixture-abi"} :source-map {:id "fixture-map"}
          :runtime {:gravity-runtime-providers [] :platform-runtime-providers [:fixture-platform]}
          :providers {:id "fixture-provider"}
          :proofs {:certificates {:content-id "fixture-proof-table" :entry-count (count ops)}}}
     :mir {:module-id "fixture-module"}
     :dialect {:target-triple "fixture-target" :flags ["-std=c17"]}}))

(defn- rejection [expected actual]
  (try (native "assert-exact!" "emission-data.gravity" expected actual :exact-emission-data)
       nil (catch clojure.lang.ExceptionInfo exception (ex-data exception))))

(defn- capture-native []
  (let [{:keys [operations index function b1 mir dialect]} (fixture)
        records (mapv #(native "operation-record" % operations index) operations)
        return-id (native "return-id" function ["entry"])
        value-name (native "value-name" index return-id)]
    {:operations operations :operation-records records
     :source-text (native "source-text" records value-name)
     :header-text (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-header-text))
     :build (native "build-manifest-record" dialect)
     :abi (native "abi-layout-record" b1) :runtime (native "runtime-helper-record" b1)
     :debug-entries (native "source-debug-entries" operations records)
     :debug-map (native "source-debug-map-record" b1 operations records return-id value-name)
     :proof-bindings (native "operation-proof-bindings" operations records)
     :discard-casts (native "discard-cast-records" operations records)
     :assumptions (native "proof-to-c-assumption-record" b1 mir operations records return-id value-name 7)
     :return-id return-id :value-name value-name
     :exit-codes (mapv #(native "process-exit-code" %) [true false 0 7 255 nil])
     :exact-match (native "assert-exact!" "emission-data.gravity" records records :exact-emission-data)
     :rejection (rejection {:operation-count 22} {:operation-count 21})
     :fallback (native "operation-expression" (operation "unknown" :unknown [] :gravity/int nil)
                       operations index)}))

(def ^:private gravity-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :c))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- gravity-call [name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-c-emission-data-development :compiler-artifact-plan? true}
   @gravity-plan name arguments))

(deftest emits-c-source-and-preserves-provenance
  (let [{:keys [operations operation-records source-text header-text debug-map assumptions rejection]}
        (capture-native)]
    (is (= 22 (count operations) (count operation-records)))
    (is (str/includes? source-text "int main(void)"))
    (is (str/includes? source-text "INT64_MIN"))
    (is (str/includes? source-text "INT64_C(9223372036854775807)"))
    (is (str/includes? header-text "int main(void);"))
    (is (= 22 (count (:entries debug-map)) (count (:operation-bindings assumptions))))
    (doseq [[op record entry binding] (map vector operations operation-records (:entries debug-map)
                                          (:operation-bindings assumptions))]
      (is (= (:op-id op) (:mir-operation-id record) (:mir-operation-id entry) (:mir-operation-id binding)))
      (is (= (get-in op [:source :span]) (:source-span entry)))
      (is (= (get-in op [:source :generated-origin]) (:generated-origin-chain entry)))
      (is (= (get-in op [:facts :source-origin-id]) (:source-origin-id entry) (:source-origin-id binding)))
      (is (= (get-in op [:facts :safety-proof-id]) (:safety-proof-id binding))))
    (is (= "B2-MANIFEST" (:id rejection)))
    (is (= :exact-emission-data (:missing-fact rejection)))
    (is (= :rejected (:fallback-status rejection)))))

(deftest current-gravity-kernel-emits-the-same-data-and-rejects-missing-input
  (let [{:keys [operations index function b1 mir dialect]} (fixture)
        native (capture-native)
        records (mapv #(gravity-call 'b2-c-operation-record [% operations index]) operations)
        return-id (gravity-call 'b2-return-id [function ["entry"]])
        value-name (gravity-call 'b2-c-value-name [index return-id])]
    (is (= (:operation-records native) records))
    (is (= (:source-text native) (gravity-call 'b2-source-text [records value-name])))
    (is (= (:header-text native) (gravity-call 'b2-header-text [])))
    (is (= (:build native) (gravity-call 'b2-c-build-manifest-record [dialect])))
    (is (= (:abi native) (gravity-call 'b2-abi-layout-record [b1])))
    (is (= (:runtime native) (gravity-call 'b2-runtime-helper-record [b1])))
    (is (= (:debug-map native) (gravity-call 'b2-source-debug-map-record
                                              [b1 operations records return-id value-name])))
    (is (= (:assumptions native) (gravity-call 'b2-proof-to-c-assumption-record
                                               [b1 mir operations records return-id value-name 7])))
    (is (= {:status :rejected :diagnostic "B1-INPUT" :missing-fact :exact-sealed-b1-c-packet
            :operation-id nil}
           (select-keys (gravity-call 'b2-build-bounded-hosted-c17 [nil])
                        [:status :diagnostic :missing-fact :operation-id])))))

(deftest compatibility-ports-preserve-var-interposition-and-order
  (let [events (atom [])
        value-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-value-name)
        constant-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-constant-expression)
        forwarded-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-forwarded-opcode?)
        expression-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-operation-expression)
        debug-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-source-debug-entries)
        proof-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-operation-proof-bindings)
        cast-var (ns-resolve 'gravity.bootstrap 'p15-s23-b2-c17-discard-cast-records)
        op (first (operations))]
    (with-redefs-fn
      {value-var (fn [& args] (swap! events conj [:name args]) "ported-name")
       expression-var (fn [& args] (swap! events conj [:expression args]) "ported-expression")}
      #(let [record (native "operation-record" op [op] {(:op-id op) 0})]
         (is (= "ported-name" (:c-value record)))
         (is (= "ported-expression" (:c-expression record)))
         (is (= [:name :expression] (mapv first @events)))))
    (with-redefs-fn {constant-var (fn [_] "ported-constant")}
      #(is (= "ported-constant" (native "operation-expression" op [op] {}))))
    (with-redefs-fn {value-var (fn [& _] "ported-forward")
                    forwarded-var (fn [_] true)}
      #(is (= "ported-forward" (native "operation-expression"
                                       (assoc op :opcode :fixture-forward :operands ["operand"])
                                       [op] {}))))
    (with-redefs-fn {debug-var (fn [& _] [:ported-debug])}
      #(is (= [:ported-debug] (:entries (native "source-debug-map-record" {} [] [] "id" "name")))))
    (with-redefs-fn {proof-var (fn [& _] [:ported-proof])
                    cast-var (fn [& _] [:ported-cast])}
      #(let [record (native "proof-to-c-assumption-record" {} {} [] [] "id" "name" 0)]
         (is (= [:ported-proof] (:operation-bindings record)))
         (is (= :ported-cast (first (:generated-c-casts record))))))))

(deftest exact-comparison-rejects-real-data-drift
  (let [calls (atom [])
        mismatch-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-strict-first-mismatch)
        fail-var (ns-resolve 'gravity.bootstrap 'p15-s23-c-backend-fail!)]
    (doseq [[expected actual] [[{:id "a"} {:id "b"}] [[1 2] [1]]
                               [{:id "a"} {:id "a" :extra true}]]]
      (is (= "B2-MANIFEST" (:id (rejection expected actual)))))
    (is (nil? (rejection {:id "a"} {:id "a"})))
    (with-redefs-fn
      {mismatch-var (fn [& args] (swap! calls conj [:mismatch args])
                      {:path [:id] :expected-kind :string :actual-kind :keyword :secret :omit})
       fail-var (fn [& args] (swap! calls conj [:failure args]) :ported-failure)}
      #(is (= :ported-failure (native "assert-exact!" "source" {} {} :exact-data))))
    (is (= [:mismatch :failure] (mapv first @calls)))
    (is (= ["source" {} {} []] (second (first @calls))))
    (is (= ["B2-MANIFEST" "source" {}
            {:missing-fact :exact-data :bounded-reason {:path [:id] :expected-kind :string :actual-kind :keyword}}]
           (second (second @calls))))))

(deftest records-and-fallbacks-do-not-authenticate-supplied-facts
  (let [contract (:gravity/namespace-contract (meta #'c/operation-record))
        {:keys [b1 operations index]} (fixture)
        op (assoc (first operations) :type :unverified-fixture-type)
        record (native "operation-record" op operations index)]
    (is (= 'gravity.backend.c (:namespace contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (= :unverified-fixture-type (:mir-type record)))
    (is (= :proven-safe (:safety-outcome record)) "Historical data label, not an admission verdict")
    (is (= "INT64_C(0)" (:fallback (capture-native))) "Historical unknown-op fallback requires upstream rejection")
    (is (= :pending-gate-b-toolchain-verification (:status (c/abi-layout-record b1))))
    (is (= :not-performed-in-gate-a (:toolchain-verification (c/abi-layout-record b1))))
    (is (nil? (c/process-exit-code nil)) "Preserve the native helper's existing nil boundary")
    (is (= :pending-gate-b-toolchain-execution (:status (c/build-manifest-record {}))))))
