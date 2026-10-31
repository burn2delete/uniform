(ns gravity.self-hosting.sh08-function-call-kernel-support
  "Shared normalized support for the SH-08 quotient kernels.

  This is intentionally a small support seam, not a general test framework.
  Both the cheap kernel namespace and the legacy SH-08 namespace use this one
  delayed C7 plan and these exact normalized input builders."
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
