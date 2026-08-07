(ns gravity.self-hosting.sh08-function-call-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_function_call_type_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-08 function/call test source is not on the classpath"
        {:id "SH08-FUNCTION-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH08-FUNCTION-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- compile-plan
  []
  (let [source-path (path c7-source-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c7-plan (delay (compile-plan)))

(defn- invoke-c7
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-function-call-type-leaf
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- fixture-artifact
  [family basename extension]
  (bootstrap/sh07-core-file-artifact
   (fixture-path family basename extension)))

(defn- canonical-core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(def ^:private verification-reports (atom {}))

(defn- b47-verification
  [artifact]
  (or (get @verification-reports artifact)
      (let [verifier
            (or (ns-resolve 'gravity.bootstrap
                            'sh07-core-artifact-verification)
                (throw
                 (ex-info
                  "Required SH-07-B47 verification is absent"
                  {:id "SH08-B47-VERIFICATION-ABSENT"})))
            report (verifier artifact)]
        (swap! verification-reports assoc artifact report)
        report)))

(defn- sh08-verification-preimage
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:domain :gravity/sh08-b47-verification-binding-v1
     :verified-artifact-id (:artifact-id artifact)
     :opaque-provenance-binding-id
     (:provenance-binding-id core)
     :authenticated-wrapper artifact
     :canonical-core-artifact core
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-identity-preimage (:identity-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :verification-report report}))

(defn- coordinator-verification
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        source-path (get-in artifact [:provenance :source-path])
        extension (if (.endsWith source-path ".qst") ".qst" ".gravity")
        preimage (sh08-verification-preimage artifact report)
        digest (bootstrap/p15-s23-c11-mir-digest preimage)]
    {:artifact :gravity/sh07-b47-coordinator-verification-v16
     :schema-version 16
     :boundary :clojure-coordinator-verifier
     :verified-artifact-id (:artifact-id artifact)
     :verified-identity-input
     (bootstrap/sh07-core-artifact-identity-input artifact)
     :verified-source-path source-path
     :verified-source-extension extension
     :report report
     :check-catalog (set (keys (:checks report)))
     :opaque-provenance-binding-id
     (:provenance-binding-id core)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :verification-digest-resolution
     {:ordinal 0
      :purpose :sh08-b47-verification-binding
      :preimage preimage
      :digest digest}}))

(defn- b47-context
  [artifact report coordinator]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:artifact-id (:artifact-id artifact)
     :artifact-status (:status artifact)
     :artifact-kind (:kind artifact)
     :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (:identity-preimage core)
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-core-artifact core
     :provenance (:provenance artifact)
     :lineage (get-in boundary [:authenticated-core-request :lineage])
     :authenticated-wrapper artifact
     :verification
     {:status (:status report)
      :checks (:checks report)
      :failed-checks (:failed-checks report)
      :receipt-context :gravity/sh07-b47-verification-v16
      :opaque-provenance-binding-id
      (:opaque-provenance-binding-id coordinator)
      :verification-digest
      (get-in coordinator [:verification-digest-resolution :digest])}}))

(def ^:dynamic ^:private *function-request-cache*
  ;; These verified request bundles retain the complete authenticated wrapper,
  ;; canonical core, and verification report.  Rebuilding one repeatedly also
  ;; canonical-hashes that large carrier again.  Cache by object identity so a
  ;; deliberately rewritten artifact used by a negative test can never inherit
  ;; a request from an equal-looking but distinct carrier.
  (java.util.IdentityHashMap.))

(defn- function-request
  [artifact]
  (locking *function-request-cache*
    (if (.containsKey *function-request-cache* artifact)
      (.get *function-request-cache* artifact)
      (let [report (b47-verification artifact)
            coordinator (coordinator-verification artifact report)
            context (b47-context artifact report coordinator)
            request
            {:canonical-core-artifact (:canonical-core-artifact context)
             :b47-context context
             :coordinator-verification coordinator}]
        ;; Failed receipts are diagnostic inputs, not reusable verification
        ;; evidence.  Rebuild them on every call so mutation-focused tests keep
        ;; exercising the verifier rather than a cached failure carrier.
        (when (= :passed (:status report))
          (.put *function-request-cache* artifact request))
        request))))

(def ^:private accepted-gravity
  (delay (fixture-artifact "accepted" "function-local-call" ".gravity")))
(def ^:private accepted-qst
  (delay (fixture-artifact "accepted" "function-local-call" ".qst")))
(def ^:private accepted-two-gravity
  (delay (fixture-artifact "accepted" "function-two-hop-call" ".gravity")))
(def ^:private accepted-two-qst
  (delay (fixture-artifact "accepted" "function-two-hop-call" ".qst")))
(def ^:private rejected-gravity
  (delay (fixture-artifact "rejected" "function-call-arity" ".gravity")))
(def ^:private rejected-qst
  (delay (fixture-artifact "rejected" "function-call-arity" ".qst")))
(def ^:private rejected-type-gravity
  (delay
    (fixture-artifact "rejected" "function-call-type-mismatch" ".gravity")))
(def ^:private rejected-type-qst
  (delay
    (fixture-artifact "rejected" "function-call-type-mismatch" ".qst")))
(def ^:private rejected-nonlocal-gravity
  (delay (fixture-artifact "rejected" "function-call-nonlocal" ".gravity")))
(def ^:private rejected-nonlocal-qst
  (delay (fixture-artifact "rejected" "function-call-nonlocal" ".qst")))
(def ^:private recursive-b47-gravity
  (delay
    (bootstrap/sh07-core-file-artifact
     (path
      (str "bootstrap/clojure/fixtures/self-hosting/sh-07/"
           "b47-function-call-recursion/accepted/"
           "function-call-recursion.gravity")))))

(defn- function-result
  [artifact]
  (invoke-c7 'sh08-function-type-core-artifact
             [(function-request artifact)]))

(defn- verification-result
  [artifact candidate]
  (invoke-c7 'sh08-verify-function-type-result
             [(function-request artifact) candidate]))

(defn- result-function
  [result]
  (first (:function-type-table result)))

(defn- result-call
  [result]
  (first (:call-type-facts result)))

(defn- result-local
  [result]
  (first (:local-binding-facts result)))

(defn- first-difference
  [expected actual]
  (cond
    (and (vector? expected) (vector? actual))
    (let [shared-count (min (count expected) (count actual))
          index
          (first
           (filter #(not= (get expected %) (get actual %))
                   (range shared-count)))]
      (cond
        (some? index)
        {:kind :index :location index
         :expected (get expected index) :actual (get actual index)}

        (not= (count expected) (count actual))
        {:kind :count :location shared-count
         :expected-count (count expected) :actual-count (count actual)}

        :else nil))

    (and (map? expected) (map? actual))
    (let [key-name
          (first
           (filter #(not= (get expected %) (get actual %))
                   (sort-by pr-str
                            (set (concat (keys expected)
                                         (keys actual))))))]
      (when (some? key-name)
        {:kind :key :location key-name
         :expected (get expected key-name)
         :actual (get actual key-name)}))

    (= expected actual) nil

    :else
    {:kind :value :expected expected :actual actual}))

(defn- sha-id
  [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(defn- synthetic-id
  [family index]
  (keyword "sh08-synthetic" (str (name family) "-" index)))

(defn- synthetic-chain-input
  [function-count]
  (let [function-indexes (range function-count)
        function-id #(synthetic-id :function %)
        function-node-id #(synthetic-id :function-node %)
        parameter-id #(synthetic-id :parameter %)
        definition-id #(synthetic-id :definition %)
        definition-binding-id #(synthetic-id :definition-binding %)
        body-id
        (fn [index]
          (if (zero? index)
            (synthetic-id :leaf-reference index)
            (synthetic-id :call index)))
        source (fn [syntax-id] {:syntax-id syntax-id :origin-chain []})
        segments
        (mapv
         (fn [index]
           (let [parameter-reference-id
                 (synthetic-id :parameter-reference index)
                 operator-id (synthetic-id :operator index)
                 call-id (body-id index)
                 fn-id (function-id index)
                 fn-node-id (function-node-id index)
                 parameter (parameter-id index)
                 definition-binding (definition-binding-id index)]
             (if (zero? index)
               {:nodes
                [{:node-id call-id
                  :core-form :reference
                  :children []
                  :attributes {:binding-id parameter}
                  :source (source call-id)}
                 {:node-id fn-node-id
                  :core-form :fn
                  :children [call-id]
                  :attributes {}
                  :source (source fn-id)}
                 {:node-id (definition-id index)
                  :core-form :def
                  :children [fn-node-id]
                  :attributes {}
                  :source (source (definition-id index))}]
                :calls []
                :edges []}
               {:nodes
                [{:node-id operator-id
                  :core-form :reference
                  :children []
                  :attributes
                  {:binding-id (definition-binding-id (dec index))}
                  :source (source operator-id)}
                 {:node-id parameter-reference-id
                  :core-form :reference
                  :children []
                  :attributes {:binding-id parameter}
                  :source (source parameter-reference-id)}
                 {:node-id call-id
                  :core-form :call
                  :children [operator-id parameter-reference-id]
                  :attributes {}
                  :source (source call-id)}
                 {:node-id fn-node-id
                  :core-form :fn
                  :children [call-id]
                  :attributes {}
                  :source (source fn-id)}
                 {:node-id (definition-id index)
                  :core-form :def
                  :children [fn-node-id]
                  :attributes {}
                  :source (source (definition-id index))}]
                :calls
                [{:core-node-id call-id
                  :argument-node-ids [parameter-reference-id]}]
                :edges
                [{:call-core-node-id call-id
                  :classification :local-function
                  :callee-function-syntax-id (function-id (dec index))}]})))
         function-indexes)
        top-operator-id (synthetic-id :top-operator function-count)
        top-argument-id (synthetic-id :top-argument function-count)
        top-call-id (synthetic-id :top-call function-count)
        top-definition-id (synthetic-id :top-definition function-count)
        top-segment
        {:nodes
         [{:node-id top-operator-id
           :core-form :reference
           :children []
           :attributes
           {:binding-id (definition-binding-id (dec function-count))}
           :source (source top-operator-id)}
          {:node-id top-argument-id
           :core-form :literal
           :children []
           :attributes {:literal-kind :integer}
           :source (source top-argument-id)}
          {:node-id top-call-id
           :core-form :call
           :children [top-operator-id top-argument-id]
           :attributes {}
           :source (source top-call-id)}
          {:node-id top-definition-id
           :core-form :def
           :children [top-call-id]
           :attributes {}
           :source (source top-definition-id)}]
         :calls
         [{:core-node-id top-call-id
           :argument-node-ids [top-argument-id]}]
         :edges
         [{:call-core-node-id top-call-id
           :classification :local-function
           :callee-function-syntax-id
           (function-id (dec function-count))}]}
        nodes (vec (mapcat :nodes (conj segments top-segment)))
        calls (vec (mapcat :calls (conj segments top-segment)))
        edges (vec (mapcat :edges (conj segments top-segment)))
        function-records
        (mapv
         (fn [index]
           {:function-syntax-id (function-id index)
            :function-core-node-id (function-node-id index)
            :body-core-node-id (body-id index)
            :parameter-binding-ids [(parameter-id index)]})
         function-indexes)
        definitions
        (conj
         (mapv
          (fn [index]
            {:binding-id (definition-binding-id index)
             :value-node-id (function-node-id index)})
          function-indexes)
         {:binding-id (synthetic-id :result-binding function-count)
          :value-node-id top-call-id})
        function-types
        (into
         {}
         (map
          (fn [index]
            [(function-id index)
             {:function-id (function-id index)
              :function-core-node-id (function-node-id index)
              :body-core-node-id (body-id index)
              :parameter-binding-ids [(parameter-id index)]
              :parameters
              [{:binding-id (parameter-id index)
                :type :gravity.type/unknown}]
              :return :gravity.type/unknown
              :status :inferred}])
          function-indexes))]
    {:nodes nodes
     :node-table (into {} (map (juxt :node-id identity) nodes))
     :function-records function-records
     :definitions definitions
     :calls calls
     :edges edges
     :function-types function-types
     :call-node-ids (mapv :core-node-id calls)}))

(deftest sh08-function-request-cache-is-identity-scoped-and-fail-closed
  (let [artifact (with-meta {:artifact-id "same"} {:instance :first})
        equal-but-distinct
        (with-meta {:artifact-id "same"} {:instance :second})
        failed-artifact {:artifact-id "failed"}
        verification-calls (atom 0)
        coordinator-calls (atom 0)]
    (binding [*function-request-cache* (java.util.IdentityHashMap.)]
      (with-redefs
       [b47-verification
        (fn [candidate]
          (swap! verification-calls inc)
          {:status (if (identical? candidate failed-artifact)
                     :failed :passed)})
        coordinator-verification
        (fn [candidate report]
          (swap! coordinator-calls inc)
          {:candidate candidate :report report})
        b47-context
        (fn [candidate report coordinator]
          {:canonical-core-artifact {:candidate candidate}
           :report report
           :coordinator coordinator})]
        (let [first-request (function-request artifact)
              repeated-request (function-request artifact)
              distinct-request (function-request equal-but-distinct)
              first-failed (function-request failed-artifact)
              repeated-failed (function-request failed-artifact)]
          (is (identical? first-request repeated-request))
          (is (not (identical? first-request distinct-request)))
          (is (not (identical? first-failed repeated-failed)))
          (is (= 4 @verification-calls))
          (is (= 4 @coordinator-calls)))))))

(deftest sh08-function-fixtures-and-c7-exports-are-executable
  (is (= (slurp (fixture-path "accepted" "function-local-call" ".gravity"))
         (slurp (fixture-path "accepted" "function-local-call" ".qst"))))
  (is (= (slurp (fixture-path "rejected" "function-call-arity" ".gravity"))
         (slurp (fixture-path "rejected" "function-call-arity" ".qst"))))
  (is (= (slurp (fixture-path "accepted" "function-two-hop-call" ".gravity"))
         (slurp (fixture-path "accepted" "function-two-hop-call" ".qst"))))
  (is (= (slurp (fixture-path "rejected" "function-call-nonlocal" ".gravity"))
         (slurp (fixture-path "rejected" "function-call-nonlocal" ".qst"))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c7-plan)))
  (is (= :meta (get-in @c7-plan [:module :profile])))
  (is (= :jvm (get-in @c7-plan [:module :target])))
  (doseq [function
          '[sh08-function-type-boundary-policy
            sh08-function-type-core-artifact
            sh08-verify-function-type-result]]
    (is (map? (get-in @c7-plan [:functions function])) function)))

(deftest sh08-function-boundary-policy-and-malformed-request-fail-closed
  (let [policy (invoke-c7 'sh08-function-type-boundary-policy [])
        result (invoke-c7 'sh08-function-type-core-artifact [{}])]
    (is (= :host-validated-b47-report-and-opaque-provenance-digest
           (:verification-boundary policy)))
    (is (false?
         (:independent-gravity-cryptographic-verification? policy)))
    (is (true? (:host-digest-authority-retained? policy)))
    (is (= :rejected (:status result)))
    (is (= :authenticated-b47-request-required
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-b47-semantic-identity-normalization-is-exact
  (let [projection-id (sha-id "a")
        binding-id (sha-id "b")
        upstream-id (sha-id "c")
        artifact-id (sha-id "d")
        raw-binding
        {:binding-id binding-id
         :upstream-binding-id upstream-id
         :definition-artifact-id artifact-id}
        raw-lineage
        {:sh06-semantic-projection-id projection-id
         :authenticated-sh06-artifact-id artifact-id}
        raw-lineage-without-authenticated-id
        {:sh06-semantic-projection-id projection-id}
        raw-node
        {:core-form :var
         :attributes
         {:binding-id binding-id
          :upstream-binding-id upstream-id
          :definition-artifact-id artifact-id
          :authenticated-sh06-artifact-id artifact-id}}]
    (is (= (assoc raw-binding
                  :upstream-binding-id binding-id
                  :definition-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-binding
                      [projection-id raw-binding])))
    (is (= (assoc raw-lineage
                  :authenticated-sh06-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id raw-lineage])))
    (is (= (assoc raw-lineage-without-authenticated-id
                  :authenticated-sh06-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id
                       raw-lineage-without-authenticated-id])))
    (is (= (-> raw-node
               (assoc-in [:attributes :upstream-binding-id] binding-id)
               (assoc-in [:attributes :definition-artifact-id]
                         projection-id)
               (assoc-in [:attributes :authenticated-sh06-artifact-id]
                         projection-id))
           (invoke-c7 'sh08-ft-semantic-node
                      [projection-id raw-node])))))

(deftest sh08-b47-accepted-identity-field-diagnostic
  (doseq [[extension artifact]
          [[".gravity" @accepted-gravity]
           [".qst" @accepted-qst]]]
    (let [core (canonical-core artifact)
          preimage (:identity-preimage core)
          projection-id
          (invoke-c7 'sh08-ft-semantic-projection-id [preimage])
          expected
          {:module-id (:module preimage)
           :lineage (:lineage preimage)
           :source-root (:root-core-node-ids preimage)
           :nodes (:nodes preimage)
           :definitions (:definitions preimage)
           :binding-table (:binding-table preimage)
           :calls (:calls preimage)
           :function-records (:function-records preimage)
           :call-edges (:call-edges preimage)
           :recursion-components (:recursion-components preimage)
           :lexical-bindings (:lexical-bindings preimage)}
          actual
          {:module-id (:module core)
           :lineage
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id (:lineage core)])
           :source-root (:root-core-node-ids core)
           :nodes
           (invoke-c7 'sh08-ft-semantic-nodes
                      [projection-id (:nodes core)])
           :definitions (:definitions core)
           :binding-table
           (invoke-c7 'sh08-ft-semantic-bindings
                      [projection-id
                       (get-in artifact
                               [:gravity-core-boundary
                                :authenticated-core-request
                                :binding-table])])
           :calls (:calls core)
           :function-records (:function-records core)
           :call-edges (:call-edges core)
           :recursion-components (:recursion-components core)
           :lexical-bindings (:lexical-bindings core)}]
      (doseq [key-name
              [:module-id :lineage :source-root :nodes :definitions
               :binding-table :calls :function-records :call-edges
               :recursion-components :lexical-bindings]]
        (let [expected-value (get expected key-name)
              actual-value (get actual key-name)
              difference (first-difference expected-value actual-value)]
          (is (= expected-value actual-value)
              (pr-str
               {:extension extension
                :field key-name
                :expected-count
                (when (coll? expected-value) (count expected-value))
                :actual-count
                (when (coll? actual-value) (count actual-value))
                :first-difference difference})))))))

(deftest sh08-edge-lineage-is-exact-and-mutations-fail
  (let [call-id (sha-id "1")
        operator-id (sha-id "2")
        argument-id (sha-id "3")
        caller-syntax-id (sha-id "4")
        caller-core-id (sha-id "5")
        callee-syntax-id (sha-id "6")
        callee-core-id (sha-id "7")
        callee-binding-id (sha-id "8")
        callee-definition-id (sha-id "9")
        callee-definition-syntax-id (sha-id "a")
        call
        {:core-node-id call-id
         :operator-node-id operator-id
         :operator-binding-id callee-binding-id
         :argument-node-ids [argument-id]
         :ordered-evaluation-node-ids [operator-id argument-id]
         :evaluation-order :operator-then-arguments
         :result-policy :call-result}
        edge
        {:ordinal 0
         :call-core-node-id call-id
         :caller-function-syntax-id caller-syntax-id
         :caller-function-core-node-id caller-core-id
         :callee-binding-id callee-binding-id
         :callee-definition-syntax-id callee-definition-syntax-id
         :callee-function-syntax-id callee-syntax-id
         :callee-function-core-node-id callee-core-id
         :argument-core-node-ids [argument-id]
         :ordered-evaluation-node-ids [operator-id argument-id]
         :evaluation-order :operator-then-arguments
         :classification :local-function}
        functions
        {caller-syntax-id
         {:function-core-node-id caller-core-id}
         callee-syntax-id
         {:function-core-node-id callee-core-id
          :definition-binding-id callee-binding-id
          :definition-core-node-id callee-definition-id}}
        definitions
        [{:binding-id callee-binding-id
          :core-node-id callee-definition-id
          :syntax-id callee-definition-syntax-id}]
        nodes
        {call-id
         {:evaluation
          {:owner-function-syntax-id caller-syntax-id}}}
        valid?
        (fn [candidate]
          (invoke-c7 'sh08-ft-call-edge-valid?
                     [candidate call functions definitions nodes]))]
    (is (true? (valid? edge)))
    (is (false?
         (valid?
          (assoc edge :callee-definition-syntax-id (sha-id "b")))))
    (is (false?
         (valid?
          (assoc edge :caller-function-core-node-id (sha-id "c")))))
    (is (false?
         (valid?
          (assoc edge :caller-function-syntax-id (sha-id "d")))))))

(deftest sh08-bounded-inference-reports-nonconvergence
  (let [result
        (invoke-c7
         'sh08-ft-infer-acyclic
         [[] {} [] [] [] [] {} {} 0])]
    (is (= :nonconverged (:convergence-status result)))
    (is (= 0 (:round-count result) (:round-bound result)))
    (is (= :bounded-function-type-inference-nonconvergence
           (get-in result [:diagnostics 0 :reason])))
    (is (= :fixed-point-within-declared-round-bound
           (get-in result [:diagnostics 0 :expected])))))

(deftest sh08-two-and-three-function-chains-reach-one-consistent-pass
  (doseq [function-count [2 3]]
    (let [{:keys [nodes node-table function-records definitions
                  calls edges function-types call-node-ids]}
          (synthetic-chain-input function-count)
          round-bound
          (invoke-c7
           'sh08-ft-inference-round-bound
           [nodes function-records calls []])
          result
          (invoke-c7
           'sh08-ft-infer-acyclic
           [nodes node-table [] definitions calls edges
            function-types {} round-bound])]
      (is (= :converged (:convergence-status result)))
      (is (<= (:round-count result) (:round-bound result)))
      (is (= round-bound (:round-bound result)))
      (doseq [function (vals (:function-types result))]
        (is (= [:gravity.type/integer]
               (mapv :type (:parameters function))))
        (is (= :gravity.type/integer (:return function))))
      (doseq [call-node-id call-node-ids]
        (is (= :gravity.type/integer
               (get (:type-table result) call-node-id)))))))

(deftest sh08-function-return-inference-is-monotone-and-conflicts-fail-closed
  (let [function-id (synthetic-id :return-function 0)
        body-id (synthetic-id :return-body 0)
        node {:node-id body-id :source {:origin-chain []}}
        base
        {function-id
         {:function-id function-id
          :body-core-node-id body-id
          :return :gravity.type/integer
          :return-conflict nil}}
        unchanged
        (invoke-c7
         'sh08-ft-update-function-returns
         [base {body-id node} {body-id :gravity.type/unknown}])
        conflicting
        (invoke-c7
         'sh08-ft-update-function-returns
         [base {body-id node} {body-id :gravity.type/string}])
        conflict
        (invoke-c7
         'sh08-ft-first-return-conflict
         [conflicting {body-id node}])]
    (is (= :gravity.type/integer
           (get-in unchanged [function-id :return])))
    (is (nil? (get-in unchanged [function-id :return-conflict])))
    (is (= :gravity.type/integer
           (get-in conflicting [function-id :return])))
    (is (= :gravity.type/integer (:expected conflict)))
    (is (= :gravity.type/string (:actual conflict)))
    (is (= node (:node conflict)))))

(deftest sh08-nonlocal-call-constraint-is-never-silently-unknown
  (let [call-id (sha-id "e")
        call {:core-node-id call-id :argument-node-ids []}
        edge {:classification :nonlocal-or-nonfunction}
        result
        (invoke-c7
         'sh08-ft-call-constraints
         [[call] [edge] {call-id {:source {:origin-chain []}}}
          {} {} {}])]
    (is (= 1 (count (:diagnostics result))))
    (is (= "C7-ANNOTATION"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :unsupported-nonlocal-call
           (get-in result [:diagnostics 0 :reason])))
    (is (= :supported-local-first-order-call
           (get-in result [:diagnostics 0 :expected])))
    (is (= :nonlocal-or-nonfunction
           (get-in result [:diagnostics 0 :actual])))))

(deftest sh08-call-mismatch-retains-expected-and-actual-types
  (let [call-id (synthetic-id :mismatch-call 0)
        argument-id (synthetic-id :mismatch-argument 0)
        function-id (synthetic-id :mismatch-function 0)
        parameter-id (synthetic-id :mismatch-parameter 0)
        call {:core-node-id call-id :argument-node-ids [argument-id]}
        edge {:classification :local-function
              :callee-function-syntax-id function-id}
        function-types
        {function-id
         {:parameters
          [{:binding-id parameter-id :type :gravity.type/integer}]}}
        result
        (invoke-c7
         'sh08-ft-call-constraints
         [[call] [edge] {call-id {:source {:origin-chain []}}}
          {argument-id :gravity.type/string}
          {parameter-id :gravity.type/integer}
          function-types])]
    (is (= :call-argument-type-mismatch
           (get-in result [:diagnostics 0 :reason])))
    (is (= :gravity.type/integer
           (get-in result [:diagnostics 0 :expected])))
    (is (= :gravity.type/string
           (get-in result [:diagnostics 0 :actual])))
    (is (= parameter-id
           (get-in result [:diagnostics 0 :parameter-binding-id])))))

(deftest sh08-function-local-call-products-are-typed-and-linked
  (let [gravity (function-result @accepted-gravity)
        qst (function-result @accepted-qst)
        function (result-function gravity)
        functions (:function-type-table gravity)
        local (result-local gravity)
        call (result-call gravity)
        calls (:call-type-facts gravity)]
    (is (= :accepted (:status gravity) (:status qst))
        (pr-str
         {:gravity
          (select-keys (first (:diagnostics gravity))
                       [:rule :reason :expected-type :actual-type])
          :qst
          (select-keys (first (:diagnostics qst))
                       [:rule :reason :expected-type :actual-type])}))
    (when (= :accepted (:status gravity) (:status qst))
      (is (= :gravity/sh08-function-typed-core-template
           (:artifact gravity)))
    (is (= 3 (:schema-version gravity)))
    (is (= :first-order-fixed-arity-functions-locals-calls
           (:scope gravity)))
    (is (= :host-resolved-b47-verification-boundary
           (:authentication-status gravity)))
    (let [policy
          (invoke-c7 'sh08-function-type-boundary-policy [])]
      (is (= :host-validated-b47-report-and-opaque-provenance-digest
             (:verification-boundary policy)))
      (is (false?
           (:independent-gravity-cryptographic-verification? policy)))
      (is (true? (:host-digest-authority-retained? policy))))
    (is (= (:identity-input gravity)
           (:artifact-id-request gravity)))
    (is (= (:identity-input gravity)
           (:identity-input qst)))
    (is (= (:provenance (canonical-core @accepted-gravity))
           (:provenance gravity)))
    (is (= (:provenance (canonical-core @accepted-qst))
           (:provenance qst)))
    (is (= (:artifact-id (canonical-core @accepted-gravity))
           (:sh07-shaped-artifact-id gravity)))
    (is (= (:artifact-id (canonical-core @accepted-qst))
           (:sh07-shaped-artifact-id qst)))
    (is (not (contains? (:identity-input gravity) :source-path)))
    (is (= 3 (count functions)))
    (doseq [function-fact functions]
      (is (= :inferred (:status function-fact)))
      (is (= 1 (:fixed-arity function-fact)))
      (is (= [:gravity.type/integer]
             (mapv :type (:parameters function-fact))))
      (is (= :gravity.type/integer (:return function-fact))))
    (is (= :gravity.type/integer (:type local)))
    (is (= 3 (count calls)))
    (doseq [call-fact calls]
      (is (= [:gravity.type/integer] (:argument-types call-fact)))
      (is (= :gravity.type/integer (:result-type call-fact)))
      (is (= :operator-then-arguments (:evaluation-order call-fact))))
    (is (every? #(= :solved (:status %))
                (:constraint-ledger gravity)))
    (is (not-any? #{:gravity.type/unknown}
                  (vals (:type-table gravity))))
    (is (= :converged (get-in gravity [:convergence :status])))
    (is (<= (get-in gravity [:convergence :round-count])
            (get-in gravity [:convergence :round-bound])))
    (is (= :monotone-finite-type-fact-propagation
           (get-in gravity [:convergence :proof])))
    (is (= (set (map :function-core-node-id functions))
           (set (map :function-core-node-id
                     (get-in gravity
                             [:function-products :function-records])))))
    (is (= :gravity.type/integer (:return function)))
      (is (= :gravity.type/integer (:result-type call))))))

(deftest sh08-two-function-chain-converges-consistently
  (let [gravity (function-result @accepted-two-gravity)
        qst (function-result @accepted-two-qst)]
    (is (= :accepted (:status gravity) (:status qst)))
    (is (= 2 (count (:function-type-table gravity))))
    (is (= 2 (count (:call-type-facts gravity))))
    (is (every? #(= [:gravity.type/integer]
                    (mapv :type (:parameters %)))
                (:function-type-table gravity)))
    (is (every? #(= :gravity.type/integer (:return %))
                (:function-type-table gravity)))
    (is (every? #(= :gravity.type/integer (:result-type %))
                (:call-type-facts gravity)))
    (is (= :converged (get-in gravity [:convergence :status])))
    (is (= (:identity-input gravity) (:identity-input qst)))))

(deftest sh08-function-results-are-repeatable-and-verifiable
  (let [gravity (function-result @accepted-gravity)
        repeated (function-result @accepted-gravity)
        qst (function-result @accepted-qst)]
    (is (= gravity repeated))
    (is (= :passed (:status (verification-result @accepted-gravity gravity))))
    (is (= :passed (:status (verification-result @accepted-qst qst))))
    (is (= (:function-type-table gravity)
           (:function-type-table qst)))
    (is (= (:call-type-facts gravity)
           (:call-type-facts qst)))
    (is (= (:local-binding-facts gravity)
           (:local-binding-facts qst)))
    (is (= (:constraint-ledger gravity)
           (:constraint-ledger qst)))))

(deftest sh08-function-result-alterations-fail-closed
  (let [artifact @accepted-gravity
        result (function-result artifact)
        alterations
        [(assoc-in result [:function-type-table 0 :return]
                  :gravity.type/string)
         (assoc-in result [:local-binding-facts 0 :type]
                   :gravity.type/string)
         (assoc-in result [:call-type-facts 0 :result-type]
                   :gravity.type/string)
         (assoc-in result [:constraint-ledger 0 :status]
                   :rejected)
         (assoc-in result [:convergence :round-count] 0)]]
    (doseq [candidate alterations]
      (is (= :rejected
             (:status (verification-result artifact candidate)))))))

(deftest sh08-b47-product-alterations-fail-before-inference
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        alterations
        [(assoc-in core [:function-records 0 :fixed-arity] 99)
         (assoc-in core [:call-edges 0 :callee-function-syntax-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:call-edges 0 :callee-definition-syntax-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:call-edges 0 :caller-function-core-node-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:lexical-bindings 0 :ordinal] 9)
         (assoc-in core [:function-records 0 :body-core-node-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")]]
    (doseq [candidate alterations]
      (let [request
            (assoc (function-request artifact)
                   :canonical-core-artifact candidate)
            result (invoke-c7 'sh08-function-type-core-artifact [request])]
        (is (= :rejected (:status result)))
        (is (= "C7-VERIFY"
               (get-in result [:diagnostics 0 :rule])))))))

(deftest sh08-host-resolved-verification-digest-binds-exact-b47-evidence
  (let [request (function-request @accepted-gravity)
        wrong-id (sha-id "0")
        alterations
        [(assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :preimage :verified-artifact-id]
          wrong-id)
         (assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :digest]
          wrong-id)
         (assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :preimage :canonical-identity-preimage :binding-table]
          [])]]
    (doseq [candidate alterations]
      (let [result
            (invoke-c7 'sh08-function-type-core-artifact [candidate])]
        (is (= :rejected (:status result)))
        (is (= :untrusted-b47-coordinator-verification
               (get-in result [:diagnostics 0 :actual-type])))))))

(deftest sh08-coordinated-carrier-and-identity-rewrites-fail-real-b47-verification
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        bindings
        (get-in artifact
                [:gravity-core-boundary :authenticated-core-request
                 :binding-table])
        identity-bindings (get-in core [:identity-preimage :binding-table])
        definitions (:definitions core)
        identity-definitions (get-in core [:identity-preimage :definitions])
        semantically-used-binding-ids
        (set
         (concat
          (map :definition-binding-id (:function-records core))
          (mapcat :parameter-binding-ids (:function-records core))
          (map :operator-binding-id (:calls core))
          (map :binding-id (:lexical-bindings core))
          (map :binding-id (:reference-uses core))))
        unused-definition-index
        (first
         (keep-indexed
          (fn [index definition]
            (when-not
             (contains? semantically-used-binding-ids
                        (:binding-id definition))
              index))
          definitions))
        unused-definition (get definitions unused-definition-index)
        unused-binding-index
        (first
         (keep-indexed
          (fn [index binding]
            (when (contains?
                   #{(:binding-id binding) (:upstream-binding-id binding)}
                   (:binding-id unused-definition))
              index))
          bindings))
        unused-binding (get bindings unused-binding-index)
        identity-definition-index
        (first
         (keep-indexed
          (fn [index definition]
            (when (= (:binding-id unused-definition)
                     (:binding-id definition))
              index))
          identity-definitions))
        identity-binding-index
        (first
         (keep-indexed
          (fn [index binding]
            (when (or (= (:binding-id binding)
                         (:binding-id unused-binding))
                      (= (:upstream-binding-id binding)
                         (:binding-id unused-binding))
                      (= (:binding-id binding)
                         (:upstream-binding-id unused-binding)))
              index))
          identity-bindings))
        remove-at
        (fn [values index]
          (if (and (integer? index) (< -1 index (count values)))
            (vec (concat (subvec values 0 index)
                         (subvec values (+ index 1))))
            nil))
        reversed-bindings (vec (reverse bindings))
        reversed-identity-bindings (vec (reverse identity-bindings))
        reversed
        (-> artifact
            (assoc-in
             [:gravity-core-boundary :authenticated-core-request
              :binding-table]
             reversed-bindings)
            (assoc-in
             [:gravity-core-boundary :canonical-core-artifact
              :identity-preimage :binding-table]
             reversed-identity-bindings))
        check-rejected
        (fn [rewritten]
          (let [report (b47-verification rewritten)
                request (function-request rewritten)
                result
                (invoke-c7 'sh08-function-type-core-artifact [request])]
            (is (= :failed (:status report)))
            (is (seq (:failed-checks report)))
            (is (= :rejected (:status result)))
            (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
            (is (= :untrusted-b47-context
                   (get-in result [:diagnostics 0 :actual-type])))))]
    (is (some? unused-binding-index))
    (is (some? unused-definition-index))
    (is (some? identity-binding-index))
    (is (some? identity-definition-index))
    (check-rejected reversed)
    (when (every? some?
                  [unused-binding-index unused-definition-index
                   identity-binding-index identity-definition-index])
      (let [reduced-bindings (remove-at bindings unused-binding-index)
            reduced-definitions
            (remove-at definitions unused-definition-index)
            reduced-identity-bindings
            (remove-at identity-bindings identity-binding-index)
            reduced-identity-definitions
            (remove-at identity-definitions identity-definition-index)
            rewritten
            (-> artifact
                (assoc-in
                 [:gravity-core-boundary :authenticated-core-request
                  :binding-table]
                 reduced-bindings)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :definitions]
                 reduced-definitions)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :identity-preimage :binding-table]
                 reduced-identity-bindings)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :identity-preimage :definitions]
                 reduced-identity-definitions))]
        (check-rejected rewritten)))))

(deftest sh08-definition-and-edge-substitution-fails-before-inference
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        first-definition (get-in core [:definitions 0])
        replacement-definition (get-in core [:definitions 1])
        replacement-edge (get-in core [:call-edges 1])
        coordinated
        (-> core
            (assoc-in
             [:definitions 0]
             (merge
              first-definition
              (select-keys
               replacement-definition
               [:name :binding-id :value-node-id :syntax-id])))
            (update-in
             [:call-edges 0]
             merge
             (select-keys
              replacement-edge
              [:callee-binding-id :callee-definition-syntax-id
               :callee-function-syntax-id
               :callee-function-core-node-id])))
        request
        (assoc (function-request artifact)
               :canonical-core-artifact coordinated)
        result
        (invoke-c7 'sh08-function-type-core-artifact [request])]
    (is (not= core coordinated))
    (is (= :rejected (:status result)))
    (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
    (is (= :b47-core-context-mismatch
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-nonlocal-edge-uses-authoritative-binding-lineage
  (let [artifact @rejected-nonlocal-gravity
        core (canonical-core artifact)
        edge-index
        (first
         (keep-indexed
          (fn [index edge]
            (when (= :nonlocal-or-nonfunction (:classification edge))
              index))
          (:call-edges core)))
        edge (get-in core [:call-edges edge-index])
        call
        (first
         (filter
          #(= (:call-core-node-id edge) (:core-node-id %))
          (:calls core)))
        operator-node
        (first
         (filter
          #(= (:operator-node-id call) (:node-id %))
          (:nodes core)))
        altered
        (assoc-in
         core
         [:call-edges edge-index :callee-definition-syntax-id]
         (sha-id "0"))
        request
        (assoc (function-request artifact) :canonical-core-artifact altered)
        result (invoke-c7 'sh08-function-type-core-artifact [request])]
    (is (some? edge-index))
    (is (= (:callee-binding-id edge)
           (get-in operator-node [:attributes :binding-id])))
    (is (= (get-in operator-node [:attributes :definition-syntax-id])
           (:callee-definition-syntax-id edge)))
    (is (= :rejected (:status result)))
    (is (= :b47-core-context-mismatch
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-recursion-products-are-complete-and-cannot-be-deleted
  (let [artifact @recursive-b47-gravity
        core (canonical-core artifact)
        components (:recursion-components core)
        component-index
        (first
         (keep-indexed
          (fn [index component]
            (when (= :mutually-recursive (:kind component)) index))
          components))
        component (get components component-index)
        outside-edge-ordinal
        (first
         (remove
          (set (:internal-call-edge-ordinals component))
          (mapv :ordinal (:call-edges core))))
        alterations
        [(assoc core :recursion-components [])
         (update-in
          core
          [:recursion-components component-index :function-core-node-ids]
          pop)
         (update-in
          core
          [:recursion-components component-index
           :internal-call-edge-ordinals]
          pop)
         (assoc-in
          core
          [:recursion-components component-index
           :internal-call-edge-ordinals 0]
          outside-edge-ordinal)]]
    (is (seq components))
    (is (some? component-index))
    (is (> (count (:function-core-node-ids component)) 1))
    (is (> (count (:internal-call-edge-ordinals component)) 1))
    (is (some? outside-edge-ordinal))
    (doseq [candidate alterations]
      (let [result
            (invoke-c7
             'sh08-function-type-core-artifact
             [(assoc (function-request artifact)
                     :canonical-core-artifact candidate)])]
        (is (= :rejected (:status result)))
        (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
        (is (= :b47-core-context-mismatch
               (get-in result [:diagnostics 0 :actual-type])))))))

(deftest sh08-arity-mismatch-is-structured-and-co-canonical
  (let [gravity (function-result @rejected-gravity)
        qst (function-result @rejected-qst)]
    (is (= :rejected (:status gravity) (:status qst)))
    (is (= :first-order-fixed-arity-functions-locals-calls
           (:scope gravity)))
    (is (= (:diagnostics gravity) (:diagnostics qst)))
    (is (= "C7-TYPE-MISMATCH"
           (get-in gravity [:diagnostics 0 :rule])))
    (is (= :call-arity-mismatch
           (get-in gravity [:diagnostics 0 :reason])))
    (is (= :type-checking
           (get-in gravity [:diagnostics 0 :stage])))
    (is (string? (get-in gravity [:diagnostics 0 :diagnostic-id])))
    (is (some? (get-in gravity [:diagnostics 0 :type-id])))
    (is (map? (get-in gravity [:diagnostics 0 :constraint-id])))
    (is (= (:identity-input gravity) (:identity-input qst)))))

(deftest sh08-argument-type-mismatch-is-structured
  (let [gravity (function-result @rejected-type-gravity)
        qst (function-result @rejected-type-qst)]
    (is (= (slurp (fixture-path
                   "rejected" "function-call-type-mismatch" ".gravity"))
           (slurp (fixture-path
                   "rejected" "function-call-type-mismatch" ".qst"))))
    (is (= :rejected (:status gravity) (:status qst)))
    (is (= "C7-TYPE-MISMATCH"
           (get-in gravity [:diagnostics 0 :rule])))
    (is (= :call-argument-type-mismatch
           (get-in gravity [:diagnostics 0 :reason])))
    (is (= :gravity.type/integer
           (get-in gravity [:diagnostics 0 :expected-type])))
    (is (= :gravity.type/string
           (get-in gravity [:diagnostics 0 :actual-type])))
    (is (string? (get-in gravity [:diagnostics 0 :diagnostic-id])))
    (is (= :gravity.type/string
           (get-in gravity [:diagnostics 0 :type-id])))
    (is (map? (get-in gravity [:diagnostics 0 :constraint-id])))
    (is (string?
         (get-in gravity [:diagnostics 0 :parameter-binding-id])))
    (is (= (:diagnostics gravity) (:diagnostics qst)))))

(deftest sh08-nonlocal-call-is-diagnosed-after-sh07-lowering
  (let [gravity (function-result @rejected-nonlocal-gravity)
        qst (function-result @rejected-nonlocal-qst)]
    (is (= :gravity/sh07-canonical-core-artifact
           (:artifact (canonical-core @rejected-nonlocal-gravity))))
    (is (= :gravity/sh07-canonical-core-artifact
           (:artifact (canonical-core @rejected-nonlocal-qst))))
    (is (= :rejected (:status gravity) (:status qst)))
    (is (= "C7-ANNOTATION"
           (get-in gravity [:diagnostics 0 :rule])))
    (is (= :unsupported-nonlocal-call
           (get-in gravity [:diagnostics 0 :reason])))
    (is (= :supported-local-first-order-call
           (get-in gravity [:diagnostics 0 :expected-type])))
    (is (= :nonlocal-or-nonfunction
           (get-in gravity [:diagnostics 0 :actual-type])))
    (is (= (:diagnostics gravity) (:diagnostics qst)))))
