(ns gravity.self-hosting.sh08-function-call-kernel-support
  "Shared normalized support for the SH-08/SH-09 compiler kernels.

  This is intentionally a small support seam, not a general test framework.
  The SH-08 type kernels and the SH-09 body consumers use the same delayed C7
  plan, verification context, and source-linked development builders."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]))

(defn repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_function_call_kernel_support.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-08 function/call kernel support is not on the classpath"
        {:id "SH08-FUNCTION-KERNEL-SUPPORT-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH08-FUNCTION-KERNEL-SUPPORT-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(defn path
  [relative]
  (str (.resolve @root relative)))

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

(def c7-plan (delay (compile-plan)))

(defn invoke-c7
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-function-call-type-leaf
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

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

(defn coordinator-verification
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

(defn b47-context
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

(defn integral-ordering-input [operation argument-types]
  ;; Normalized DATA for the actual Gravity algorithms; no source admission.
  (let [module {:namespace 'example.ordering :profile :meta :target :jvm :effects [] :capabilities []}
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        arguments (mapv #(keyword (str "arg" %)) (range (count argument-types)))
        children (into [:operator] arguments)
        binding {:binding-id :ordering-binding :binding-class :core :kind :var :name operation :namespace 'gravity.core
                 :package {:name 'gravity/core :version "bootstrap-contract-v1"} :type-ref :gravity.type/core-var
                 :safety :safe :effects [] :capabilities [] :profile-set [:meta] :target-set [:all]
                 :definition-syntax-id :ordering-definition}
        operator {:node-id :operator :core-form :reference :children [] :resolved-binding-ids [:ordering-binding]
                  :attributes {:symbol operation :position :operator :binding-id :ordering-binding :binding-class :core
                               :definition-syntax-id :ordering-definition} :source (source :operator) :evaluation {:order []}}
        operands (mapv (fn [id] {:node-id id :core-form :reference :children [] :attributes {:binding-id id}
                                 :source (source id) :evaluation {:order []}}) arguments)
        node {:node-id :call :core-form :call :children children :resolved-binding-ids [:ordering-binding]
              :attributes {:operator-child-index 0 :argument-count (count arguments) :argument-child-indexes (vec (range 1 (count children)))
                           :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
              :source (source :call) :evaluation {:owner-function-syntax-id nil :kind :operator-then-arguments
                                                :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}}
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :ordering-binding :argument-node-ids arguments
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :ordering-binding :callee-definition-syntax-id :ordering-definition
              :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids arguments
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        nodes (into [operator] (conj operands node))
        core {:module module :nodes nodes :function-records [] :calls [call] :call-edges [edge]}
        bindings {:ordering-binding binding}
        types (zipmap arguments argument-types)]
    {:module module :core core :node node :call call :edge edge :nodes (into {} (map (juxt :node-id identity) nodes))
     :bindings bindings :types types :seeds types
     :forms (into {} (map (fn [id] [id {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                      :kind (if (= id :call) :list :symbol) :value (if (= id :operator) operation id)
                                      :parent-form-id (when-not (= id :call) :call) :child-form-ids (if (= id :call) children [])}]) (conj children :call)))}))

(defn nonnegative-integer-quotient-input [values]
  (let [base (integral-ordering-input 'quot (vec (repeat (count values) :gravity.type/integer)))
        input (reduce (fn [input [index value]]
                        (let [id (keyword (str "arg" index))]
                          (-> input
                              (assoc-in [:nodes id :core-form] :literal)
                              (assoc-in [:nodes id :resolved-binding-ids] [])
                              (assoc-in [:nodes id :attributes] {:literal-kind :integer :value value})
                              (assoc-in [:nodes id :evaluation] {:kind :value :order []})
                              (assoc-in [:forms id :kind] :integer)
                              (assoc-in [:forms id :value] value))))
                      base (map-indexed vector values))
        module (assoc (:module input) :effects [:memory/allocate])
        nodes (mapv #(get (:nodes input) (:node-id %)) (get-in input [:core :nodes]))]
    (-> input (assoc :module module :binding-types (:types input))
        (assoc-in [:core :module] module) (assoc-in [:core :nodes] nodes)
        (assoc :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module module}))))

(defn nonnegative-integer-quotient-transfer-input [values position]
  (let [input (nonnegative-integer-quotient-input values) id (keyword (str "arg" position))
        op (keyword (str "throw-op-" position)) payload (keyword (str "payload-" position))
        source (get-in input [:nodes id :source]) form (get-in input [:forms id])
        transfer (-> (get-in input [:nodes id])
                     (assoc :core-form :throw :children [payload] :attributes {} :resolved-binding-ids [])
                     (assoc :evaluation {:order [{:index 0 :core-node-id payload}]}))
        payload-form (assoc form :form-id payload :syntax-id payload :parent-form-id id :kind :string :value "original quotient transfer" :child-form-ids [])
        payload-node {:node-id payload :core-form :literal :children [] :resolved-binding-ids []
                      :attributes {:literal-kind :string :value "original quotient transfer"}
                      :source (assoc source :form-id payload :syntax-id payload) :evaluation {:kind :value :order []}}
        input (-> input (assoc-in [:nodes id] transfer) (assoc-in [:nodes payload] payload-node)
                  (assoc-in [:forms id] (assoc form :kind :list :value '(throw "original quotient transfer") :child-form-ids [op payload]))
                  (assoc-in [:forms op] (assoc form :form-id op :syntax-id op :parent-form-id id :kind :symbol :value 'throw :child-form-ids []))
                  (assoc-in [:forms payload] payload-form)
                  (assoc-in [:types id] :gravity.type/never) (assoc-in [:types payload] :gravity.type/string))]
    (assoc-in input [:core :nodes] (conj (mapv #(get (:nodes input) (:node-id %)) (get-in input [:core :nodes])) payload-node))))

(def ^:private effects-source-relative-path
  "bootstrap/gravity/src/gravity/effects.gravity")

(defn module-root-entry
  [bundle]
  (let [entries (filterv #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))]
    (when-not (= 1 (count entries))
      (throw (ex-info "Expected one actual bundle root" {})))
    (first entries)))

(defn module-root-plan
  [bundle]
  (:plan (module-root-entry bundle)))

(defn compile-c8-module-bundle
  [source-path]
  (let [effects-path (path effects-source-relative-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        dependency (bootstrap/compiler-c5-resolution-file-artifact effects-path)
        inputs {:artifact :gravity/sh06-module-inputs
                :schema-version 1
                :module-artifacts [dependency]}
        caller (bootstrap/compiler-c5-resolution-file-artifact source-path inputs)
        dependency-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan
                         emitter effects-path (slurp effects-path))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path (slurp source-path)
     {:caller-resolution-artifact caller
      :dependency-plans [dependency-plan]})))

(def ^:private effects-plan
  (delay
    (let [source (path effects-source-relative-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                             source :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source (slurp source)))))

(defn leaf
  [function & args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :body-effect-development-kernel}
   @effects-plan function (vec args)))

(defn seal-binding
  [request typed verification]
  (let [preimage {:domain :gravity/sh09-sh08-function-binding-v1
                  :request request
                  :typed typed
                  :verification verification}]
    {:artifact :gravity/sh09-authenticated-sh08-function-binding
     :schema-version 1
     :boundary :clojure-coordinator-verifier
     :status :passed
     :digest-resolution
     {:algorithm :sha256
      :encoding :gravity/canonical-edn-v1
      :preimage preimage
      :digest (bootstrap/p15-s23-c11-mir-digest preimage)
      :status :passed}}))

(defn fresh-type-products
  [relative]
  (let [t0 (System/nanoTime)
        c5 (bootstrap/compiler-c5-resolution-file-artifact (path relative))
        t1 (System/nanoTime)
        artifact (bootstrap/sh07-core-from-resolution-artifact c5)
        t2 (System/nanoTime)
        report (bootstrap/sh07-core-artifact-verification artifact)
        t3 (System/nanoTime)
        coordinator (coordinator-verification artifact report)
        t4 (System/nanoTime)
        context (b47-context artifact report coordinator)
        t5 (System/nanoTime)
        request {:canonical-core-artifact (:canonical-core-artifact context)
                 :b47-context context
                 :coordinator-verification coordinator}
        typed (invoke-c7 'sh08-function-type-core-artifact [request])
        t6 (System/nanoTime)
        verification (invoke-c7 'sh08-verify-function-type-result [request typed])
        t7 (System/nanoTime)]
    {:c5 c5
     :artifact artifact
     :report report
     :request request
     :typed typed
     :verification verification
     :development-timing
     {:clock :monotonic-nanoseconds
      :authority :non-authoritative
      :boundary :existing-public-call-elapsed
      :nested-work :included
      :c5-construction (- t1 t0)
      :c6-construction (- t2 t1)
      :c6-verification (- t3 t2)
      :coordinator-verification (- t4 t3)
      :b47-context (- t5 t4)
      :c7-construction (- t6 t5)
      :c7-verification (- t7 t6)}}))

(defn reseal-effect-graph
  [candidate graph]
  (-> candidate
      (assoc :effect-graph graph)
      (assoc-in [:identity-input :effect-graph] graph)))
