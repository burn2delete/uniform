(ns gravity.self-hosting.sh17-target-lowering-hardening-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh17_target_lowering_hardening_test.clj")]
    (when-not resource
      (throw (ex-info "SH-17 test source is not on the classpath"
                      {:id "SH17-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH17-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(def ^:private engine-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")

(defn- compile-plan []
  (let [source-path (path engine-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(defn- source-forms []
  (with-open [reader
              (java.io.PushbackReader.
               (io/reader (path engine-path)))]
    (loop [result []]
      (let [form (read {:eof ::eof} reader)]
        (if (= form ::eof)
          result
          (recur (conj result form)))))))

(defn- calls-named [name forms]
  (let [calls (atom [])]
    (doseq [form forms]
      (walk/postwalk
       (fn [value]
         (when (and (seq? value) (= name (first value)))
           (swap! calls conj value))
         value)
       form))
    @calls))

(def ^:private engine-plan (delay (compile-plan)))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh17-target-lowering-hardening
    :compiler-artifact-plan? true}
   @engine-plan function arguments))

(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- target [backend]
  (case backend
    :llvm {:backend :llvm
           :canonical-target :llvm-x86_64-linux
           :triple "x86_64-unknown-linux-gnu"
           :architecture :x86_64
           :object-format :elf}
    :c {:backend :c
        :triple "arm64-apple-macosx14.0.0"
        :architecture :arm64
        :object-format :c17-source}
    :wasm {:backend :wasm
           :triple "wasm32-unknown-unknown"
           :architecture :wasm32
           :object-format :wasm-module}))

(defn- abi [backend]
  {:calling-convention (if (= backend :wasm) :wasm-core
                           (if (= backend :llvm) :sysv-amd64 :c))
   :pointer-width (if (= backend :wasm) 32 64)
   :endianness :little
   :layout-id (digest "a")})

(defn- operation [operation-id opcode operands result-id source-id]
  {:operation-id operation-id
   :opcode opcode
   :operands operands
   :result-id result-id
   :type :gravity/integer
   :effects []
   :capabilities []
   :source-id source-id})

(defn- source-entry [operation-id source-id checkout]
  {:operation-id operation-id
   :source-id source-id
   :span {:source-id source-id
          :start-byte 0
          :end-byte 8
          :actual-source-path (str checkout "/program.gravity")}
   :origin-chain
   [{:generator-id (digest "b")
     :anchor-id (digest "c")
     :actual-source-path (str checkout "/generated.gravity")}]})

(defn- request [backend checkout]
  (let [constant-source (digest "d")
        return-source (digest "e")
        operations
        [(operation :op/constant :constant [] :value/constant constant-source)
         (assoc
          (operation :op/return :return [:value/constant]
                     :value/return return-source)
          :effects [:io/write]
          :capabilities [:stdio/write])]]
    {:artifact :gravity/sh17-target-lowering-request
     :schema-version 1
     :backend backend
     :profile :hosted
     :target (target backend)
     :abi (abi backend)
     :runtime {:family :minimal-native :services []}
     :providers [{:effect :io/write
                  :capability :stdio/write
                  :provider-id (digest "9")}]
     :effects [:io/write]
     :capabilities [:stdio/write]
     :c11-revision (invoke 'sh17-current-c11-revision [])
     :mir {:artifact :gravity/verified-target-independent-mir
           :schema-version 1
           :verified? true
           :mir-id (digest "f")
           :entrypoint :function/main
           :functions
           [{:function-id :function/main
             :entry-block :block/entry
             :return-type :gravity/integer
             :blocks
             [{:block-id :block/entry
               :operations operations
               :terminator {:kind :return
                            :value-id :value/constant}}]}]}
     :proofs
     [{:proof-id (digest "1")
       :operation-id :op/constant
       :claim :integer-constant-representable
       :status :verified}]
     :source-map
     [(source-entry :op/constant constant-source checkout)
      (source-entry :op/return return-source checkout)]
     :actual-path-provenance
     [{:kind :actual-source-path
       :path (str checkout "/program.gravity")}] }))

(defn- build [request]
  (invoke 'sh17-build-target-lowering [request]))

(defn- verify [request candidate]
  (invoke 'sh17-verify-target-lowering [request candidate]))

(defn- reason [result]
  (get-in result [:diagnostics 0 :reason]))

(defn- c11-source-rule []
  (let [revision (invoke 'sh17-current-c11-revision [])]
    {:artifact :gravity/c11-pinned-source-rule
     :owner :gravity-source
     :source-content-hash (:source-content-hash revision)
     :source-byte-count (:source-byte-count revision)
     :plan-semantic-hash (:plan-semantic-hash revision)
     :functions-semantic-hash (:functions-semantic-hash revision)
     :builder-function (:builder-function revision)
     :builder-semantic-hash (:builder-semantic-hash revision)
     :verifier-function (:verifier-function revision)
     :verifier-semantic-hash (:verifier-semantic-hash revision)
     :function-shapes (:function-shapes revision)
     :compiled-by :clojure-stage0-seed
     :executed-by :clojure-stage0-rule-runner
     :clojure-seed-boundary? true
     :self-hosted? false}))

(deftest sh17-source-compiles-and-c11-revision-is-complete
  (let [plan @engine-plan
        if-calls (calls-named 'if (source-forms))
        revision (invoke 'sh17-current-c11-revision [])
        source-rule (c11-source-rule)
        changed-shapes
        (assoc (:function-shapes source-rule)
               'c11-build-data-flow-for-node
               {:arity 2 :params '[node result]})]
    (is (= 253588 (:source-byte-count revision)))
    (is (= 237 (:function-count revision)))
    (is (= 'verify-c11-mir-module (:verifier-function revision)))
    (is (= 27 (count (:function-shapes revision))))
    (is (pos? (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (contains? (:functions plan) 'sh17-build-target-lowering))
    (is (contains? (:functions plan) 'sh17-verify-target-lowering))
    (is (true? (invoke 'c14-c11-source-rule-valid? [source-rule])))
    (is (false?
         (invoke 'c14-c11-source-rule-valid?
                 [(assoc source-rule :function-shapes changed-shapes)])))))

(deftest sh17-lowers-and-verifies-all-three-bounded-targets
  (doseq [backend [:llvm :c :wasm]]
    (testing (name backend)
      (let [input (request backend "/checkout-a")
            result (build input)
            verification (verify input result)]
        (is (= :accepted (:status result)))
        (is (= backend (:backend result)))
        (is (= backend
               (first
                (get-in result
                        [:target-program 0 :operations 0
                         :target-opcode]))))
        (is (= (:c11-revision input)
               (get-in result [:identity-input :c11-revision])))
        (is (= (get-in input [:mir :mir-id])
               (get-in result [:identity-input :mir-id])))
        (is (= (:proofs input)
               (get-in result [:identity-input :proofs])))
        (is (= (:providers input)
               (get-in result [:identity-input :providers])))
        (is (= (:effects input)
               (get-in result [:identity-input :effects])))
        (is (= (:capabilities input)
               (get-in result [:identity-input :capabilities])))
        (is (= [:io/write]
               (get-in result
                       [:target-program 0 :operations 1 :effects])))
        (is (= [:stdio/write]
               (get-in result
                       [:target-program 0 :operations 1 :capabilities])))
        (is (= (get-in input
                       [:mir :functions 0 :blocks 0 :operations 1
                        :source-id])
               (get-in result
                       [:target-program 0 :operations 1 :source-id])))
        (is (= :passed (:status verification)))
        (is (empty? (:diagnostics verification)))))))

(deftest sh17-enforces-exact-normalized-schemas-and-references
  (let [base (request :llvm "/checkout-a")
        cases
        [(assoc base :unexpected true)
         (assoc base :c11-revision
                (assoc (:c11-revision base) :function-count 138))
         (assoc-in base [:mir :unexpected] true)
         (assoc-in base [:mir :functions 0 :unexpected] true)
         (assoc-in base [:mir :functions 0 :blocks 0 :unexpected] true)
         (assoc-in base [:mir :functions 0 :blocks 0 :operations 0
                         :unexpected] true)
         (assoc-in base [:source-map 0 :unexpected] true)
         (assoc-in base [:source-map 0 :span :unexpected] true)
         (assoc-in base [:source-map 0 :origin-chain 0 :unexpected] true)
         (assoc-in base [:source-map 0 :source-id] (digest "8"))
         (assoc-in base [:proofs 0 :unexpected] true)
         (assoc base :providers
                [{:effect :io/read
                  :capability :fs/read
                  :provider-id (digest "2")
                  :unexpected true}])
         (assoc-in base [:providers 0 :effect] :io/read)
         (assoc base :actual-path-provenance [])
         (assoc-in base [:actual-path-provenance 0 :unexpected] true)
         (assoc-in base [:mir :entrypoint] :function/not-main)
         (assoc-in base
                   [:mir :functions 0 :blocks]
                   (conj
                    (get-in base [:mir :functions 0 :blocks])
                    {:block-id :block/second
                     :operations
                     [(operation :op/constant :integer-add
                                 [:value/constant] :value/second
                                 (digest "7"))]
                     :terminator {:kind :return
                                  :value-id :value/second}}))
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :operands]
                   [:value/missing])
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :result-id]
                   :value/constant)
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :terminator :value-id]
                   :value/missing)
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 0 :operands]
                   [:value/return])
         (assoc-in base
                   [:mir :functions 0 :blocks 0 :operations 1 :effects]
                   [:io/read])
         (assoc-in base [:source-map 0 :operation-id] :op/missing)
         (assoc-in base [:proofs 0 :operation-id] :op/missing)]]
    (doseq [hostile cases]
      (let [result (build hostile)]
        (is (= :rejected (:status result)))
        (is (contains? #{:invalid-normalized-request
                         :cross-reference-mismatch}
                       (reason result)))))))

(deftest sh17-preflight-contains-hostile-depth-width-and-scalars
  (let [base (request :llvm "/checkout-a")
        deep (loop [remaining 10000 value base]
               (if (zero? remaining) value (recur (dec remaining) [value])))
        wide (into {} (map (fn [index] [(keyword (str "k" index)) index])
                           (range 300)))
        over-operands
        (assoc-in base [:mir :functions 0 :blocks 0 :operations 0 :operands]
                  (vec (repeat 17 :value/x)))
        long-id (keyword (apply str (repeat 129 "x")))
        over-id
        (assoc-in base [:mir :functions 0 :blocks 0 :operations 0
                        :operation-id] long-id)
        over-i64
        (assoc-in base [:source-map 0 :span :end-byte]
                  9223372036854775808N)]
    (is (= :structure-depth-bound
           (:reason (invoke 'sh17-structural-preflight [deep]))))
    (is (= :structure-width-bound
           (:reason (invoke 'sh17-structural-preflight [wide]))))
    (is (= :structure-sequence-carrier
           (:reason
            (invoke 'sh17-structural-preflight [(iterate inc 0)]))))
    (doseq [hostile [over-operands over-id over-i64]]
      (is (= :rejected (:status (build hostile))))
      (is (contains? #{:invalid-normalized-request
                       :structure-noncanonical-scalar}
                       (reason (build hostile)))))))

(deftest sh17-linux-llvm-target-contract-rejects-cross-target-substitution
  (let [base (request :llvm "/checkout-a")
        hostile-targets
        [(assoc-in base [:target :canonical-target] :llvm-x86-64-linux)
         (assoc-in base [:target :canonical-target] :darwin-arm64)
         (assoc-in base [:target :triple] "arm64-apple-macosx14.0.0")
         (assoc-in base [:target :architecture] :arm64)
         (assoc-in base [:target :architecture] :aarch64)
         (assoc-in base [:target :object-format] :mach-o)
         (update base :target dissoc :canonical-target)
         (assoc-in base [:target :triple] "wasm32-unknown-unknown")]]
    (doseq [hostile hostile-targets]
      (let [result (build hostile)]
        (is (= :rejected (:status result)))
        (is (= "C14-TARGET"
               (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (= :target-contract-mismatch
               (reason result)))))))

(deftest sh17-identity-is-path-neutral-and-provenance-retains-paths
  (doseq [backend [:llvm :c :wasm]]
    (let [left (build (request backend "/checkout-left"))
          right (build (request backend "/checkout-right"))]
      (is (= :accepted (:status left) (:status right)))
      (is (= (:identity-input left) (:identity-input right)))
      (is (not= (:provenance left) (:provenance right)))
      (is (not (.contains (pr-str (:identity-input left))
                          "/checkout-left")))
      (is (.contains (pr-str (:provenance left)) "/checkout-left")))))

(deftest sh17-verifier-rejects-result-substitution-before-comparison
  (let [input (request :wasm "/checkout-a")
        result (build input)
        altered (assoc-in result [:target-program 0 :operations 0
                                  :target-opcode]
                          [:llvm :constant])
        deep-candidate
        (assoc result :padding
               (loop [remaining 10000 value nil]
                 (if (zero? remaining)
                   value
                   (recur (dec remaining) [value]))))
        sequence-candidate (assoc result :padding (iterate inc 0))]
    (is (= :passed (:status (verify input result))))
    (is (= :rejected (:status (verify input altered))))
    (is (= :result-substitution (reason (verify input altered))))
    (is (= :candidate-structural-bound
           (reason (verify input deep-candidate))))
    (is (= :candidate-structural-bound
           (reason (verify input sequence-candidate))))))
