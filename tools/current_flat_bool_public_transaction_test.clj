(ns current-flat-bool-public-transaction-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is run-tests testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-c10.source-contract :as c6-contract]
            [gravity.c6-c10.source-interface :as c6-interface]
            [gravity.c11-mir.source-contract :as c11-contract]
            [gravity.c11-mir.source-interface :as c11-interface]))

(def ^:private source-text
  "(ns gravity.test.flat-bool-v3 (:profile :hosted) (:target :jvm) (:effects #{}) (:capabilities #{}) (:safety :safe) (:exports [main]))\n\n(defn main [] (do true false true))\n")

(def ^:private c6-v3-header
  {:interface :gravity/c6-c10 :version 3})

(def ^:private c11-v3-header
  {:interface :gravity/c11-mir :version 3})

(def ^:private c6-v2-header
  {:interface :gravity/c6-c10 :version 2})

(def ^:private c11-v2-header
  {:interface :gravity/c11-mir :version 2})

(def ^:private flat-bool-family
  :closed-flat-bool-literal-sequence)

(def ^:private fixture*
  (delay
    (let [left (java.io.File/createTempFile "gravity-flat-bool-public-" ".gravity")
          right (java.io.File/createTempFile "gravity-flat-bool-crossed-" ".gravity")
          left-path (.getCanonicalPath left)
          right-path (.getCanonicalPath right)]
      (spit left source-text)
      (spit right source-text)
      {:left-path left-path
       :right-path right-path
       :left-context
       (bootstrap/p15-s23-stage2-gravity-checked-core-context
        left-path source-text :jvm)
       :right-context
       (bootstrap/p15-s23-stage2-gravity-checked-core-context
        right-path source-text :jvm)
       :files [left right]})))

(def ^:private v3-cohort*
  (delay
    (let [{:keys [left-context]} @fixture*
          core
          (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
           left-context c6-v3-header)
          core-report
          (bootstrap/p15-s23-stage2-gravity-checked-core-verification-report
           core left-context c6-v3-header)
          mir
          (bootstrap/p15-s23-stage2-c11-mir-artifact
           core left-context c11-v3-header)
          mir-report
          (bootstrap/p15-s23-stage2-c11-mir-verification-report
           mir core left-context c11-v3-header)]
      {:core core
       :core-report core-report
       :mir mir
       :mir-report mir-report})))

(def ^:private v2-cohort*
  (delay
    (let [{:keys [left-context]} @fixture*
          core
          (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
           left-context c6-v2-header)
          core-report
          (bootstrap/p15-s23-stage2-gravity-checked-core-verification-report
           core left-context c6-v2-header)
          mir
          (bootstrap/p15-s23-stage2-c11-mir-artifact
           core left-context c11-v2-header)
          mir-report
          (bootstrap/p15-s23-stage2-c11-mir-verification-report
           mir core left-context c11-v2-header)]
      {:core core
       :core-report core-report
       :mir mir
       :mir-report mir-report})))

(defn- sha256-bytes
  [^bytes bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest)))))

(defn- file-identity
  [relative-path]
  (let [file (io/file relative-path)
        bytes (java.nio.file.Files/readAllBytes (.toPath file))]
    {:path relative-path
     :bytes (alength bytes)
     :hash (sha256-bytes bytes)}))

(defn- current-c6-binding
  [report]
  (:source-binding report))

(defn- current-c6-program
  [binding]
  (or (:source-program binding) binding))

(defn- assert-current-c6-binding!
  [report]
  (let [binding (current-c6-binding report)
        program (current-c6-program binding)
        identity (file-identity (:source-relative-path program))
        abi c6-contract/flat-bool-current-compiler-program-abi]
    (is (= :development-current (:source-mode binding)))
    (is (= :development-current (:source-mode program)))
    (is (= 3 (:version program)))
    (is (= flat-bool-family (:semantic-family program)))
    (is (= (:builder-function abi) (:builder-function program)))
    (is (= (:verifier-function abi) (:verifier-function program)))
    (is (= (:required-functions abi) (:function-shapes binding)))
    (is (= (:bytes identity) (:source-byte-count binding)))
    (is (= (:hash identity) (:source-content-hash binding)))
    (is (= (:bytes identity) (:source-byte-count program)))
    (is (= (:hash identity) (:source-content-hash program)))))

(defn- assert-current-c11-rule!
  [rule]
  (let [identity (file-identity (:source-relative-path rule))
        abi c11-contract/flat-bool-current-source-contract-abi]
    (is (= {:interface :gravity/c11-mir :version 3}
           (:source-interface rule)))
    (is (= :development-current (:source-mode rule)))
    (is (= flat-bool-family (:semantic-family rule)))
    (is (= (:builder-function abi) (:builder-function rule)))
    (is (= (:verifier-function abi) (:verifier-function rule)))
    (is (= (:required-functions abi) (:function-shapes rule)))
    (is (= (:bytes identity) (:source-byte-count rule)))
    (is (= (:hash identity) (:source-content-hash rule)))))

(defn- operation-path
  [module operation-id]
  (let [entrypoint (first (keys (:functions module)))]
    (first
     (for [[block-id block] (get-in module [:functions entrypoint :blocks])
           [index operation] (map-indexed vector (:instructions block))
           :when (= operation-id (:op-id operation))]
       [:functions entrypoint :blocks block-id :instructions index]))))

(defn- rehash-c11-artifact
  [artifact checked-core context]
  (let [with-mir-id
        (assoc artifact :mir-id
               (bootstrap/p15-s23-c11-mir-recomputed-id artifact))
        with-artifact-id
        (assoc with-mir-id :artifact-id
               (bootstrap/p15-s23-c11-mir-digest
                {:kind (:kind with-mir-id)
                 :schema-version (:schema-version with-mir-id)
                 :mir-id (:mir-id with-mir-id)}))]
    (assoc with-artifact-id
           :actual-path-binding-id
           (bootstrap/p15-s23-c11-mir-recomputed-actual-path-binding-id
            with-artifact-id checked-core context))))

(defn- rejection-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- assert-rejected!
  [label thunk]
  (let [data (rejection-data thunk)]
    (is (map? data) label)
    (is (or (string? (:id data))
            (string? (:diagnostic-id data)))
        [label data])
    data))

(deftest public-v3-transaction-authenticates-current-c6-and-c11
  (let [{:keys [core core-report mir mir-report]} @v3-cohort*
        module (:mir-module mir)
        operations (bootstrap/p15-s23-c11-mir-operation-sequence module)
        source-binding (get-in core-report [:source-policy-receipt])]
    (testing "the real public C6 route and its fresh report are v3/current"
      (is (= :gravity/p15-s23-stage2-closed-checked-core-artifact
             (:kind core)))
      (is (= :passed (:status core-report)))
      (is (= c6-v3-header (:source-interface core-report)))
      (is (= flat-bool-family (:semantic-family core-report)))
      (is (= c6-v3-header (:source-interface source-binding)))
      (is (= :selected-current-source-host-seed
             (:verification-scope core-report)))
      (assert-current-c6-binding! core-report)
      (is (= [:literal :literal :literal :do :function]
             (mapv :source-operation (:core-nodes core)))))
    (testing "the public C11 constructor and separate fresh report join the C6 product"
      (is (= :gravity/p15-s23-c11-authenticated-mir-artifact (:kind mir)))
      (is (= 3 (:schema-version mir)))
      (is (= flat-bool-family (get-in mir [:scope :semantic-family])))
      (is (= (:artifact-id core) (:source-core-artifact-id mir)))
      (is (= c6-v3-header
             (get-in mir [:checked-core-ingress :source-policy :source-interface])))
      (assert-current-c11-rule! (:source-rule mir))
      (is (= (:source-rule mir) (:source-rule mir-report)))
      (is (= :gravity/c11-mir-verification-report (:artifact mir-report)))
      (is (= :passed (:status mir-report)))
      (is (= :passed (:semantic-replay-parity mir-report)))
      (is (= :passed (get-in mir-report [:independent-verifier :status])))
      (is (= (:mir-id mir) (:mir-id mir-report)))
      (is (= (:artifact-id core) (:checked-core-artifact-id mir-report)))
      (is (= :clojure-stage0-rule-runner
             (get-in mir-report [:execution-tcb :runner])))
      (is (vector? operations))
      (is (seq operations))
      (is (= (count operations)
             (count (set (map :op-id operations)))))
      (is (every? #(and (some? (:op-id %)) (keyword? (:opcode %)))
                  operations)))))

(deftest public-v3-transaction-rejects-substitutions-and-coherent-ir-mutations
  (let [{:keys [core mir]} @v3-cohort*
        {:keys [left-context right-context]} @fixture*
        module (:mir-module mir)
        first-operation (first (bootstrap/p15-s23-c11-mir-operation-sequence module))
        first-operation-path (operation-path module (:op-id first-operation))
        type-mutant
        (rehash-c11-artifact
         (update-in mir (conj first-operation-path :facts :type-fact-id)
                    (constantly "sha256:forged-type-fact"))
         core left-context)
        origin-mutant
        (rehash-c11-artifact
         (update-in mir (conj first-operation-path :source :origin-chain)
                    (fnil conj []) "sha256:forged-origin")
         core left-context)
        edge-mutant
        (rehash-c11-artifact
         (update-in mir [:mir-module :data-flow-graph :edges]
                    #(vec (rest %)))
         core left-context)]
    (testing "ingress and source/context substitutions fail before admission"
      (assert-rejected! :missing-core
                         #(bootstrap/p15-s23-stage2-c11-mir-artifact
                           {} left-context c11-v3-header))
      (assert-rejected! :foreign-core
                         #(bootstrap/p15-s23-stage2-c11-mir-artifact
                           (with-meta core {:foreign true})
                           left-context c11-v3-header))
      (assert-rejected! :native-core-class
                         #(bootstrap/p15-s23-stage2-c11-mir-artifact
                           (java.util.Date.) left-context c11-v3-header))
      (assert-rejected! :crossed-context
                         #(bootstrap/p15-s23-stage2-c11-mir-artifact
                           core right-context c11-v3-header))
      (assert-rejected! :forged-core-id
                         #(bootstrap/p15-s23-stage2-c11-mir-artifact
                           (assoc core :artifact-id "sha256:forged-core")
                           left-context c11-v3-header)))
    (testing "source metadata and independently coherent IR changes fail closed"
      (assert-rejected!
       :tampered-source-rule
       #(bootstrap/p15-s23-stage2-c11-mir-verification-report
         (assoc-in mir [:source-rule :source-content-hash]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         core left-context c11-v3-header))
      (assert-rejected!
       :tampered-builder-metadata
       #(bootstrap/p15-s23-stage2-c11-mir-verification-report
         (assoc-in mir [:source-rule :builder-function]
                   'c11-build-target-independent-mir)
         core left-context c11-v3-header))
      (doseq [[label mutant] [[:type-fact type-mutant]
                              [:origin-chain origin-mutant]
                              [:data-flow-edge edge-mutant]]]
        (assert-rejected!
         label
         #(bootstrap/p15-s23-stage2-c11-mir-verification-report
           mutant core left-context c11-v3-header))))))

(deftest public-v2-regression-and-version-boundaries-remain-explicit
  (let [{:keys [core-report mir mir-report]} @v2-cohort*]
    (testing "one genuine current-v2 C6/C11 transaction still passes"
      (is (= :passed (:status core-report)))
      (is (= c6-v2-header (:source-interface core-report)))
      (is (= :passed (:status mir-report)))
      (is (= 2 (:schema-version mir)))
      (is (= c11-v2-header (:source-interface (:source-rule mir))))
      (is (= :development-current
             (get-in mir [:source-rule :source-mode])))
      (is (not= flat-bool-family
                (get-in mir [:source-rule :semantic-family]))))
    (testing "explicit v1/default boundaries are not silently promoted"
      (is (= 2 (:version c6-contract/current-compiler-program)))
      (is (= 2 (:version c11-contract/v2-source-contract)))
      (is (map? (rejection-data
                 #(c6-interface/interface-version!
                   {:interface :gravity/c6-c10 :version 1}))))
      (is (map? (rejection-data
                 #(c11-interface/public-interface-version!
                   {:interface :gravity/c11-mir :version 1})))))))

(let [result (run-tests 'current-flat-bool-public-transaction-test)
      {:keys [left-path right-path]} @fixture*]
  (doseq [file [left-path right-path]]
    (java.nio.file.Files/deleteIfExists
     (.toPath (io/file file))))
  (println {:scope :public-current-flat-bool-c6-c11-transaction
            :genuine-public-v3? true
            :genuine-public-v2? true
            :current-v3-source-mode :development-current
            :negative-controls
            [:missing-core :foreign-core :native-core-class :crossed-context
             :forged-core-id :tampered-source-rule :tampered-builder-metadata
             :type-fact :origin-chain :data-flow-edge]
            :source-input-transport :gravity-source-input-context
            :clojure-seed-boundary? true
            :self-hosted? false
            :native-c? false
            :resource-restriction? false})
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
