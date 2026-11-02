(ns gravity.self-hosting.sh08-function-call-kernel-support
  "Shared normalized support for the SH-08/SH-09 compiler kernels.

  This is intentionally a small support seam, not a general test framework.
  The SH-08 type kernels and the SH-09 body consumers use the same delayed C7
  plan, verification context, and source-linked development builders."
  (:require [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap])
  (:import [java.nio.charset StandardCharsets]
           [java.util.concurrent TimeUnit]))

(def stage2-runtime
  {:engine :stage2-runtime-iteration-test})

(defn typed-lowering-kernel-data
  ([] (typed-lowering-kernel-data false))
  ([throw?]
   ;; Normalized DATA checks the Gravity consumer joins, not C7 authentication.
   (let [source-path "in-memory-typed.gravity"
         ns-form '(ns example.typed (:profile :meta) (:target :jvm) (:effects #{}) (:capabilities #{}) (:safety :safe))
         body (if throw? '(throw problem) 'problem)
         body-id (if throw? :throw :reference)
         body-form-id (if throw? :throw-form :body-form)
         fn-form (list 'fn '[problem :- String] :- 'String body)
         definition-form (list 'def 'identity fn-form)
         expanded [definition-form]
         source-text (str/join "\n" (map pr-str (cons ns-form expanded)))
         declaration {:type-binding-id :string :type-id :gravity.type/string :type-symbol 'String :type-form-id :type-form :type-syntax-id :type}
         signature {:body-source-child-index 4 :parameter-source-child-indexes [0] :parameters [declaration] :return declaration}
         source (fn [form-id syntax-id] {:form-id form-id :syntax-id syntax-id})
         nodes [{:node-id :reference :core-form :reference :children [] :attributes {:binding-id :parameter} :source (source :body-form :body)}
                {:node-id :function :core-form :fn :children [body-id] :resolved-binding-ids [:parameter]
                 :attributes {:parameters '[problem] :signature signature} :source (source :function-form :fn)}
                {:node-id :definition :core-form :def :children [:function] :source (source :definition-form :def)}]
         nodes (if throw? (conj nodes {:node-id :throw :core-form :throw :children [:reference] :source (source :throw-form :throw)}) nodes)
         forms [{:form-id :definition-form :syntax-id :def :value definition-form :child-form-ids [:def-op :name-form :function-form]}
                {:form-id :function-form :syntax-id :fn :value fn-form :child-form-ids [:fn-op :parameter-form :return-marker :type-form body-form-id]}
                {:form-id :parameter-form :syntax-id :parameters :value '[problem :- String] :child-form-ids [:parameter-name :parameter-marker :type-form]}
                {:form-id :parameter-name :syntax-id :parameter-name :value 'problem :scope-id :function-scope}
                {:form-id :type-form :syntax-id :type :value 'String}
                {:form-id :body-form :syntax-id :body :value 'problem}]
         forms (if throw? (conj forms {:form-id :throw-form :syntax-id :throw :value body :child-form-ids [:throw-operator :body-form]}) forms)
         forms (mapv #(assoc % :parent-form-id (case (:form-id %) :definition-form nil :function-form :definition-form
                                                   :parameter-name :parameter-form :body-form (if throw? :throw-form :function-form)
                                                   :function-form)) forms)
         bindings [{:binding-id :parameter :name 'problem :kind :local :binding-class :lexical
                    :definition-syntax-id :parameter-name :scope-id :function-scope}
                   {:binding-id :string :name 'String :kind :type :binding-class :core :namespace 'gravity.core}]
         module {:module 'example.typed :source-path source-path :forms expanded :profile :meta :target :jvm :safety :safe
                 :effects #{} :capabilities #{} :exports []}
         core-module {:namespace 'example.typed :profile :meta :target :jvm :safety :safe :effects [] :capabilities [] :exports []}
         record {:definition-name 'identity :definition-kind :named-top-level :function-syntax-id :fn :function-core-node-id :function
                 :definition-core-node-id :definition :body-core-node-id body-id :parameter-binding-ids [:parameter]
                 :fixed-arity 1 :signature signature}
         function {:definition-name 'identity :function-id :fn :function-core-node-id :function :body-core-node-id body-id
                   :parameter-binding-ids [:parameter] :parameters [{:binding-id :parameter :type :gravity.type/string}]
                   :fixed-arity 1 :return :gravity.type/string :signature signature :thrown-error-effects (if throw? [:gravity.type/string] [])}
         core {:artifact-id :core :module core-module :nodes nodes :function-records [record] :root-core-node-ids [:definition]}
         reader {:source-unit-record {:path source-path :bytes-hash (str "sha256:" (bootstrap/sha256-hex source-text))}
                 :token-stream [{:raw source-text}]}
         macro-artifact {:expanded-forms expanded :gravity-macro-boundary {:authenticated-sh04-artifact {:c2-reader-artifact reader}}}
         request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:module core-module :forms forms :binding-table bindings :top-level-form-ids [:definition-form]}
                   :authenticated-wrapper {:sh06-resolution-artifact {:sh05-macro-artifact macro-artifact}}}}
         typed {:status :accepted :module core-module :sh07-shaped-artifact-id :core :function-type-table [function]
                :type-table (cond-> {:reference :gravity.type/string :function function :definition function} throw? (assoc :throw :gravity.type/never))}
         report {:artifact :gravity/sh08-function-type-verification :status :passed :expected typed :candidate typed}
         input {:request request :typed typed :verification report}
         observation {:verification report :source {:path source-path :text source-text :sha256 (get-in reader [:source-unit-record :bytes-hash])}
                      :compiler-sources {:c7 {:source {:sha256 :current-c7} :plan-id :c7-plan}
                                         :lowering {:source {:sha256 :current-lowering} :plan-id :lowering-plan}}}
         definition {:name 'identity :params '[problem] :body (list body) :definition-form :def-fn :arity 1 :body-form-count 1
                     :source-declarations {:parameters '[problem :- String] :return '[:- String]}}]
     {:module module :input input :observation observation :definition definition})))

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

(def ^:private fresh-sh09-load-probe-expression
  "(require '[clojure.test :as test]
             'gravity.self-hosting.sh09-private-self-effect-test
             'gravity.self-hosting.sh09-evidence-metadata-test)
    (let [loaded (set (map ns-name (all-ns)))
          old-namespaces '#{gravity.self-hosting.sh08-function-call-type-test
                            gravity.self-hosting.sh08-recursive-function-type-test
                            gravity.self-hosting.sh09-body-effect-test}
          focused-namespaces '#{gravity.self-hosting.sh09-private-self-effect-test
                               gravity.self-hosting.sh09-evidence-metadata-test}
          vars [#'gravity.self-hosting.sh09-private-self-effect-test/current-gravity-private-three-parameter-source-context-development-kernel
                #'gravity.self-hosting.sh09-private-self-effect-test/current-gravity-private-self-effects-development-kernel
                #'gravity.self-hosting.sh09-evidence-metadata-test/cold-quote-metadata-effects-development-kernel]
          counters (ref {:test 0 :pass 0 :fail 0 :error 0})]
      (binding [test/*report-counters* counters]
        (test/test-vars vars))
      (let [result {:old-loaded (mapv #(contains? loaded %) old-namespaces)
                    :focused-loaded (mapv #(contains? loaded %) focused-namespaces)
                    :semantic-counters @counters}]
        (println \"SH09-FOCUSED-LOAD-ISOLATION\" (pr-str result))
        (System/exit (if (and (every? false? (:old-loaded result))
                               (every? true? (:focused-loaded result))
                               (pos? (get-in result [:semantic-counters :test]))
                               (zero? (get-in result [:semantic-counters :fail]))
                               (zero? (get-in result [:semantic-counters :error])))
                       0
                       9)))))")

(defn fresh-sh09-load-probe
  "Run representative focused SH-09 semantics in a new JVM without monoliths."
  []
  (let [command ["clojure"
                 "-Sdeps"
                 "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
                 "-M"
                 "-e"
                 fresh-sh09-load-probe-expression]
        builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (io/file (path ".")))
                  (.redirectErrorStream true))
        process (.start builder)
        completed? (.waitFor process 60 TimeUnit/SECONDS)]
    (letfn [(result [terminated?]
              (with-open [stream (.getInputStream process)]
                {:completed? (and completed? terminated?)
                 :exit (when terminated? (.exitValue process))
                 :output (if terminated?
                           (String. (.readAllBytes stream) StandardCharsets/UTF_8)
                           "")}))]
      (if completed?
        (result true)
        (do
          (.destroyForcibly process)
          (result (.waitFor process 5 TimeUnit/SECONDS)))))))

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

(defn cold-metadata-input
  "Build the small cold-evidence DATA fixture without loading the SH-08 suite."
  []
  (let [record {:function-syntax-id :function :parameter-binding-ids []}
        cut {:mode :entries :excluded-node-ids []}
        core {:function-records [record] :calls [] :call-edges [] :nodes []
              :lexical-bindings [] :definitions []}
        input {:mode :entries :cut cut :source-argument-call-node-ids []
               :source-preparation {:status :accepted}
               :quoted-products {:status :accepted}
               :inference {:status :converged}}
        cold (invoke-c7 'sh08-is-cold-evidence [input])]
    {:core core :facts {:imported-function-types []} :record record :bindings [] :forms []
     :input input :cold cold :cut cut}))

(defn cold-metadata-invalid-inputs
  [input]
  [(assoc input :quote-work-count 0) (assoc input :quote-work-count 1)
   (assoc input :quote-work-count nil) (assoc input :unknown-field true)])

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

(defn sha
  "Return the deterministic scalar identity used by normalized kernel DATA."
  [n]
  (str "sha256:" (format "%064x" n)))

(defn node
  "Build the source-linked node shape shared by recursive body kernels."
  [n kind children]
  {:node-id (sha n) :core-form kind :children (mapv sha children) :attributes {}
   :source {:syntax-id (sha (+ n 100)) :form-id (sha (+ n 200))
            :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil}
   :evaluation {:kind (case kind :fn :function-creation :quote :no-evaluation :left-to-right)
                :order (if (#{:fn :quote} kind) []
                         (mapv (fn [index n] {:index index :core-node-id (sha n)}) (range) children))}})

(defn recursive-kernel-data
  "Build the small recursive body proof fixture used by SH-09 controls."
  []
  (let [owner (fn [n] (assoc-in n [:evaluation :owner-function-syntax-id] (sha 20)))
        nodes [(owner (assoc (node 1 :literal []) :attributes {:literal-kind :boolean :value false}))
               (owner (assoc (node 2 :reference []) :attributes {:binding-id (sha 50)}))
               (owner (assoc (node 3 :reference []) :attributes {:binding-id (sha 51)}))
               (owner (node 4 :call [2 3]))
               (owner (assoc (node 5 :literal []) :attributes {:literal-kind :integer :value 0}))
               (owner (assoc (node 6 :if [1 4 5]) :evaluation
                        {:kind :condition-then-exclusive-branch :owner-function-syntax-id (sha 20)
                         :order [{:index 0 :core-node-id (sha 1)}]}))
               (node 7 :fn [6]) (node 8 :def [7])
               (assoc (node 9 :reference []) :attributes {:binding-id (sha 50)})
               (assoc (node 10 :literal []) :attributes {:literal-kind :integer :value 7}) (node 11 :call [9 10])]
        module {:namespace 'example.recursive :profile :meta :target :jvm :effects [] :capabilities []}
        edge {:ordinal 0 :call-core-node-id (sha 4) :caller-function-syntax-id (sha 20) :caller-function-core-node-id (sha 7)
              :callee-binding-id (sha 50) :callee-definition-syntax-id (sha 80) :callee-function-syntax-id (sha 20)
              :callee-function-core-node-id (sha 7) :argument-core-node-ids [(sha 3)]
              :ordered-evaluation-node-ids [(sha 2) (sha 3)] :evaluation-order :operator-then-arguments :classification :local-function}
        external-edge (assoc edge :ordinal 1 :call-core-node-id (sha 11) :caller-function-syntax-id nil
                             :caller-function-core-node-id nil :argument-core-node-ids [(sha 10)]
                             :ordered-evaluation-node-ids [(sha 9) (sha 10)])
        call {:core-node-id (sha 4) :argument-node-ids [(sha 3)]}
        external-call {:core-node-id (sha 11) :argument-node-ids [(sha 10)]
                       :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments}
        source (:source (get nodes 3)) external-source (:source (get nodes 10))
        proof {:domain :gravity/sh08-authoritative-recursive-proof-v1 :function-syntax-id (sha 20)
               :function-core-node-id (sha 7) :definition-binding-id (sha 50) :definition-syntax-id (sha 80)
               :parameter-binding-ids [(sha 51)] :fixed-arity 1 :recursive-call-core-node-id (sha 4)
               :recursive-argument-node-ids [(sha 3)] :recursive-edge edge
               :recursive-call-source {:syntax-id (:syntax-id source) :source-span (:semantic-span source)
                                       :origin-chain [] :generated-origin nil}
               :recursive-call-profile :meta :recursive-call-target :jvm :recursive-call-effects [] :recursive-call-capabilities []
               :base-node-id (sha 5) :base-type :gravity.type/integer :recursive-definition-binding-id (sha 50)
               :parameter-evidence-call-core-node-id (sha 11)
               :parameter-evidence-call {:call-core-node-id (sha 11) :caller-function-syntax-id nil :callee-function-syntax-id (sha 20)
                 :argument-node-ids [(sha 10)] :ordered-evaluation-node-ids [(sha 9) (sha 10)] :evaluation-order :operator-then-arguments
                 :argument-types [:gravity.type/integer] :result-type :gravity.type/integer
                 :syntax-id (:syntax-id external-source) :source-span (:semantic-span external-source) :origin-chain [] :generated-origin nil
                 :profile :meta :target :jvm :effects [] :capabilities [] :edge external-edge}
               :parameter-types [:gravity.type/integer]
               :proof-bound :one-named-self-recursive-positive-fixed-arity-positional-literal-base
               :captures? false :higher-order? false :monotone-fixed-point? true}
        core {:nodes nodes :module module :call-edges [edge external-edge] :calls [call external-call]
              :definitions [{:binding-id (sha 50) :syntax-id (sha 80)}]
              :function-records [{:function-syntax-id (sha 20) :function-core-node-id (sha 7) :body-core-node-id (sha 6)
                                  :definition-binding-id (sha 50) :definition-kind :named-top-level :fixed-arity 1
                                  :parameter-binding-ids [(sha 51)]}]
              :recursion-components [{:ordinal 0 :kind :self-recursive :function-syntax-ids [(sha 20)]
                                       :function-core-node-ids [(sha 7)] :internal-call-edge-ordinals [0]}]}
        facts {:recursive-proof proof :function-type-table [{:function-id (sha 20) :body-core-node-id (sha 6)
                  :parameters [{:binding-id (sha 51) :type :gravity.type/integer}] :return :gravity.type/integer}]
               :type-table {(sha 1) :gravity.type/bool} :call-type-facts []}]
    [core facts]))

(defn list-query-input
  "Build the shared normalized input for a unary collection query."
  [operation argument-types]
  (let [input (integral-ordering-input operation argument-types)]
    (assoc input
           :binding-types (:types input)
           :products {:calls (get-in input [:core :calls])
                      :call-edges (get-in input [:core :call-edges])
                      :module (:module input)})))

(defn map-keys-input
  "Build the map-keys input without reaching into the SH-08 test namespace."
  [type]
  (-> (list-query-input 'keys [type])
      (assoc-in [:module :effects] [:memory/allocate])
      (assoc-in [:core :module :effects] [:memory/allocate])))

(defn set-conversion-input
  "Build the normalized input for a persistent set conversion."
  [argument-types]
  (let [input (integral-ordering-input 'set argument-types)]
    (assoc input
           :binding-types (:types input)
           :products {:calls (get-in input [:core :calls])
                      :call-edges (get-in input [:core :call-edges])
                      :module (:module input)})))

(defn list-set-input
  "Build the list-to-set input used by the SH-09 local-call census."
  [type]
  (-> (set-conversion-input [type])
      (assoc-in [:module :effects] [:memory/allocate])
      (assoc-in [:core :module :effects] [:memory/allocate])))

(defn atomic-equality-input
  "Build the normalized atomic equality input used by module traversal tests."
  [argument-types]
  (let [input (integral-ordering-input '= argument-types)]
    (assoc input
           :binding-types (:types input)
           :products {:calls (get-in input [:core :calls])
                      :call-edges (get-in input [:core :call-edges])
                      :module (:module input)})))

(defn list-query-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-lq-derive [core node call edge nodes bindings types forms]))

(defn bare-list-parameter-input [actual]
  (let [sha #(str "sha256:" (format "%064x" %))
        declaration {:source-child-index 1 :marker-form-id (sha 1) :marker-syntax-id (sha 2)
                     :type-form-id (sha 3) :type-syntax-id (sha 4) :type-symbol 'List :type-binding-id (sha 5) :type-id :gravity.type/list}
        signature {:rule-version :sh07-atomic-function-signature-v1 :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters [declaration] :return nil :parameter-source-child-indexes [0] :body-source-child-index 2}
        record {:function-syntax-id :callee :function-core-node-id :function :body-core-node-id :body
                :parameter-binding-ids [:parameter] :fixed-arity 1 :definition-kind :named-top-level
                :definition-binding-id :definition :signature signature}
        module {:namespace 'example.list-parameter :profile :meta :target :jvm :effects [] :capabilities []}
        callee (assoc (invoke-c7 'sh08-ft-function-type-skeleton [record module]) :return :gravity.type/string)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :definition :argument-node-ids [:argument]
              :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :definition :callee-definition-syntax-id :definition-source
              :callee-function-syntax-id :callee :callee-function-core-node-id :function
              :argument-core-node-ids [:argument] :ordered-evaluation-node-ids [:operator :argument]
              :evaluation-order :operator-then-arguments :classification :local-function}
        node {:node-id :call :core-form :call :children [:operator :argument] :source (source :call)
              :resolved-binding-ids [:definition] :evaluation {:kind :operator-then-arguments}}
        nodes {:call node :argument {:node-id :argument :source (source :argument)}
               :operator {:node-id :operator :core-form :reference :source (source :operator)
                          :attributes {:binding-id :definition} :resolved-binding-ids [:definition]}}
        bindings {(sha 5) {:binding-id (sha 5) :name 'List :kind :type :binding-class :core :namespace 'gravity.core}
                  :parameter {:binding-id :parameter :name 'items :kind :local :binding-class :lexical :definition-syntax-id :parameter-source}
                  :definition {:binding-id :definition :name 'consume :kind :function :binding-class :namespace :namespace 'example.list-parameter}}
        core {:module module :function-records [record] :nodes (vec (vals nodes)) :calls [call] :call-edges [edge]}
        types {:argument actual :call :gravity.type/string :operator callee}
        context {:ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings))}]
    {:core core :node node :call call :edge edge :callee callee :nodes nodes :bindings bindings :types types
     :functions {:callee callee} :seeds {:parameter :gravity.type/list} :context context :declaration declaration :signature signature}))

(defn homogeneous-list-parameter-input [actual]
  (let [base (bare-list-parameter-input actual) sha #(str "sha256:" (format "%064x" %))
        type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        declaration (assoc (:declaration base) :rule-version :sh07-list-parameter-declaration-v1
                           :type-id type :constructor-form-id (sha 6) :constructor-syntax-id (sha 7)
                           :element-form-id (sha 8) :element-syntax-id (sha 9)
                           :element-symbol 'String :element-binding-id (sha 10))
        signature (assoc (:signature base) :parameters [declaration])
        record (assoc (first (get-in base [:core :function-records])) :signature signature)
        core (assoc (:core base) :function-records [record])
        bindings (assoc (:bindings base) (sha 10) {:binding-id (sha 10) :name 'String :kind :type :binding-class :core :namespace 'gravity.core})
        callee (assoc (:callee base) :signature signature :parameters [{:binding-id :parameter :type type}])
        types (assoc (:types base) :operator callee)]
    (assoc base :core core :bindings bindings :declaration declaration :signature signature :callee callee
                :types types :functions {:callee callee} :seeds {:parameter type} :homogeneous-type type
                :context (assoc (:context base) :ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings))))))

(defn homogeneous-list-head-input [branch-index]
  (let [type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        base (list-query-input 'first [type]) parameter (homogeneous-list-parameter-input type)
        predicate (let [moved (walk/postwalk-replace {:operator :predicate-operator :arg0 :predicate-receiver :call :condition
                                                     :ordering-binding :empty-binding} (list-query-input 'empty? [type]))]
                    (assoc moved :call (:condition moved)))
        owner #(assoc-in % [:evaluation :owner-function-syntax-id] :callee)
        reference #(assoc (owner %) :resolved-binding-ids [:parameter]
                          :attributes {:symbol 'items :position :expression :binding-id :parameter :binding-class :lexical
                                       :definition-syntax-id :parameter-source})
        nodes (-> (merge (:nodes base) (:nodes predicate))
                  (update-vals owner) (update :arg0 reference) (update :predicate-receiver reference)
                  (assoc-in [:condition :core-form] :call)
                  (assoc-in [:predicate-operator :attributes :position] :operator))
        children (if (= branch-index 1) [:condition :call :base] [:condition :base :call])
        source #(hash-map :form-id % :syntax-id % :semantic-span nil :origin-chain [] :generated-origin nil)
        branch {:node-id :branch :core-form :if :children children :source (source :branch)
                :attributes {:condition-child-index 0 :then-child-index 1 :else-child-index 2 :condition-evaluation :always-first
                             :branch-evaluation :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}}
                :evaluation {:kind :condition-then-exclusive-branch :owner-function-syntax-id :callee
                             :order [{:index 0 :core-node-id :condition}]}}
        nodes (assoc nodes :branch branch
                           :base (owner {:node-id :base :core-form :literal :children [] :source (source :base)
                                         :attributes {:literal-kind :string :value ""} :evaluation {:order []}})
                           :function {:node-id :function :core-form :fn :children [:branch] :source (source :function)
                                      :resolved-binding-ids [:parameter] :attributes {:signature (:signature parameter)} :evaluation {:order []}})
        control {:core-node-id :branch :kind :if :condition-node-id :condition
                 :branches [{:role :then :predicate :truthy :core-node-id (get children 1)}
                            {:role :else :predicate :falsey :core-node-id (get children 2)}]
                 :branch-exclusivity :exactly-one :truthiness {:false-values [:nil :false] :other-values :true} :result-policy :selected-branch}
        owned-edge #(assoc % :caller-function-syntax-id :callee :caller-function-core-node-id :function)
        edge (assoc (owned-edge (:edge base)) :ordinal 1)
        predicate-edge (assoc (owned-edge (:edge predicate)) :ordinal 0)
        core (assoc (:core base) :nodes (vec (vals nodes)) :calls [(:call predicate) (:call base)] :call-edges [predicate-edge edge]
                                :function-records (get-in parameter [:core :function-records]) :control-flow [control] :lexical-bindings [] :mutations [])
        bindings (merge (:bindings parameter) (:bindings base) (:bindings predicate))
        forms (-> (merge (:forms base) (:forms predicate))
                  (assoc-in [:arg0 :value] 'items) (assoc-in [:predicate-receiver :value] 'items))
        types {:arg0 type :predicate-receiver type :condition :gravity.type/bool :base :gravity.type/string}
        predicate-input (assoc predicate :core core :nodes nodes :node (:condition nodes) :edge predicate-edge
                                        :bindings bindings :forms forms :types types)
        derived (list-query-derived predicate-input)
        types (assoc types :predicate-operator (:descriptor derived))]
    (assoc base :core core :nodes nodes :node (:call nodes) :edge edge :bindings bindings :forms forms :types types
                :predicate-input predicate-input :predicate-derived derived)))

(defn map-association-input [key value-type empty?]
  (let [module {:namespace 'example.association :profile :meta :target :jvm :effects [:memory/allocate] :capabilities []}
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        binding (-> (get-in (integral-ordering-input '< []) [:bindings :ordering-binding])
                    (assoc :name 'assoc :binding-id :association-binding :definition-syntax-id :association-definition))
        input-children (if empty? [] [:input-key :input-value])
        children [:operator :input :key :value]
        node (fn [id kind ids attrs]
               {:node-id id :core-form kind :children ids :attributes attrs :source (source id)
                :resolved-binding-ids [] :evaluation {:kind :left-to-right :order (mapv #(hash-map :index %1 :core-node-id %2) (range) ids)}})
        operator (assoc (node :operator :reference [] {:symbol 'assoc :position :operator :binding-id :association-binding
                                                       :binding-class :core :definition-syntax-id :association-definition})
                        :resolved-binding-ids [:association-binding])
        input (node :input :collection-literal input-children {:literal-kind :map :element-count (count input-children)})
        input-key (node :input-key :literal [] {:literal-kind :keyword :value :old})
        input-value (node :input-value :literal [] {:literal-kind :integer :value 7})
        key-node (node :key :literal [] {:literal-kind :keyword :value key})
        value-node (node :value :reference [] {:binding-id :value-binding})
        call-node (-> (node :association :call children {:operator-child-index 0 :argument-count 3 :argument-child-indexes [1 2 3]
                                                        :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                      (assoc :resolved-binding-ids [:association-binding])
                      (assoc-in [:evaluation :kind] :operator-then-arguments))
        nodes (vec (concat [operator] (when-not empty? [input-key input-value]) [input key-node value-node call-node]))
        call {:core-node-id :association :operator-node-id :operator :operator-binding-id :association-binding
              :argument-node-ids [:input :key :value] :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :association :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :association-binding :callee-definition-syntax-id :association-definition
              :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids [:input :key :value]
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        core {:module module :nodes nodes :function-records [] :lexical-bindings [] :definitions [] :calls [call] :call-edges [edge] :recursion-components []}
        forms [(form :association :list (list 'assoc {} key 'value) nil children)
               (form :operator :symbol 'assoc :association []) (form :input :map (if empty? {} {:old 7}) :association input-children)
               (form :input-key :keyword :old :input []) (form :input-value :integer 7 :input [])
               (form :key :keyword key :association []) (form :value :symbol 'value :association [])]
        types {:input-key :gravity.type/keyword :input-value :gravity.type/integer :key :gravity.type/keyword :value value-type
               :input {:kind :gravity.type/map :member-types (if empty? [] [:gravity.type/keyword :gravity.type/integer]) :shape :literal}}
        bindings {:association-binding binding :value-binding {:binding-id :value-binding :binding-class :lexical :name 'value}}
        products {:calls [call] :call-edges [edge] :definitions [] :function-index {} :recursion-components [] :module module}]
    {:module module :core core :node call-node :nodes (into {} (map (juxt :node-id identity) nodes)) :call call :edge edge
     :forms (into {} (map (juxt :form-id identity) forms)) :bindings bindings :types types :products products
     :binding-types {:value-binding value-type}}))

(defn map-removal-input [key empty?]
  (let [base (walk/postwalk-replace {:association :removal :association-binding :removal-binding
                                   :association-definition :removal-definition 'assoc 'dissoc}
                                  (map-association-input key :gravity.type/bool empty?))
        children [:operator :input :key]
        node (-> (:node base) (assoc :children children)
                 (assoc-in [:attributes :argument-count] 2) (assoc-in [:attributes :argument-child-indexes] [1 2])
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [:input :key] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids [:input :key] :ordered-evaluation-node-ids children)
        core (-> (:core base) (assoc :calls [call] :call-edges [edge])
                 (update :nodes #(mapv (fn [n] (if (= :removal (:node-id n)) node n)) (remove (fn [n] (= :value (:node-id n))) %))))
        forms (-> (:forms base) (dissoc :value)
                  (assoc-in [:removal :child-form-ids] children)
                  (assoc-in [:removal :value] (list 'dissoc (get-in base [:forms :input :value]) key)))]
    (assoc base :node node :call call :edge edge :core core :forms forms :types (dissoc (:types base) :value)
                :nodes (into {} (map (juxt :node-id identity) (:nodes core)))
                :bindings (dissoc (:bindings base) :value-binding) :binding-types {}
                :products (assoc (:products base) :calls [call] :call-edges [edge]))))

(defn map-present-get-input
  ([key] (map-present-get-input key false false))
  ([key never-input? never-key?]
   (let [base (walk/postwalk-replace {:removal :present-get :removal-binding :get-binding :removal-definition :get-definition 'dissoc 'get}
                                   (map-removal-input key false))
         entries [[:file "source.gravity" :string :gravity.type/string]
                  [:line 17 :integer :gravity.type/integer]
                  [:kind :span :keyword :gravity.type/keyword]
                  [:enabled false :boolean :gravity.type/bool]
                  [:payload nil :nil :gravity.type/nil]]
         source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
         literal (fn [id kind value] {:node-id id :core-form :literal :children [] :resolved-binding-ids []
                                      :attributes {:literal-kind kind :value value} :source (source id) :evaluation {:kind :left-to-right :order []}})
         form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                    :kind kind :value value :parent-form-id parent :child-form-ids children})
         parts (mapv (fn [index [key value kind type]]
                       (let [kid (keyword (str "entry-key-" index)) vid (keyword (str "entry-value-" index))]
                         {:ids [kid vid] :nodes [(literal kid :keyword key) (literal vid kind value)]
                          :forms [(form kid :keyword key :input []) (form vid kind value :input [])]
                          :types {kid :gravity.type/keyword vid type}})) (range) entries)
         children (vec (mapcat :ids parts))
         map-type {:kind :gravity.type/map :member-types (vec (mapcat #(vector :gravity.type/keyword (nth % 3)) entries)) :shape :literal}
         input (if never-input?
                 (-> (get-in base [:nodes :input]) (assoc :core-form :reference :children [] :attributes {:binding-id :never-input})
                     (assoc-in [:evaluation :order] []))
                 (-> (get-in base [:nodes :input]) (assoc :children children :attributes {:literal-kind :map :element-count (count children)})
                     (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children))))
         key-node (if never-key?
                    (-> (get-in base [:nodes :key]) (assoc :core-form :reference :attributes {:binding-id :never-key}))
                    (get-in base [:nodes :key]))
         node-list (vec (concat [(get-in base [:nodes :operator])] (when-not never-input? (mapcat :nodes parts)) [input key-node (:node base)]))
         forms (merge (dissoc (:forms base) :input-key :input-value)
                      (when-not never-input? (into {} (map (juxt :form-id identity) (mapcat :forms parts))))
                      {:input (form :input (if never-input? :symbol :map) (if never-input? 'aborts (into {} (map #(subvec % 0 2) entries)))
                                    :present-get (if never-input? [] children))
                       :key (form :key (if never-key? :symbol :keyword) (if never-key? 'aborts key) :present-get [])})
         types (merge (when-not never-input? (apply merge (map :types parts)))
                      {:input (if never-input? :gravity.type/never map-type) :key (if never-key? :gravity.type/never :gravity.type/keyword)})
         binding-types (cond-> {} never-input? (assoc :never-input :gravity.type/never) never-key? (assoc :never-key :gravity.type/never))
         bindings (merge (:bindings base) (into {} (map (fn [[id _]] [id {:binding-id id :binding-class :lexical :kind :local :name 'aborts}]) binding-types)))]
     (assoc base :core (assoc (:core base) :nodes node-list) :nodes (into {} (map (juxt :node-id identity) node-list))
                 :forms forms :types types :bindings bindings :binding-types binding-types))))

(defn private-entry-get-input
  ([] (private-entry-get-input :file))
  ([key]
   (let [base (map-present-get-input key)
         source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
         form (fn [id kind value parent children]
                {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                 :kind kind :value value :parent-form-id parent :child-form-ids children})
         node (fn [id kind children attributes owner]
                {:node-id id :core-form kind :children children :attributes attributes :source (source id)
                 :resolved-binding-ids [] :evaluation {:kind :left-to-right :owner-function-syntax-id owner
                                                      :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}})
         parameter {:binding-id :span-binding :name 'span :kind :local :binding-class :lexical}
         helper {:function-syntax-id :helper-form :function-form-id :helper-form :function-core-node-id :helper-fn
                 :body-core-node-id :present-get :parameter-binding-ids [:span-binding] :fixed-arity 1
                 :definition-kind :named-top-level :definition-binding-id :helper-binding :definition-core-node-id :helper-def
                 :definition-name 'without-path}
         binding {:binding-id :helper-binding :name 'without-path :kind :function :binding-class :namespace
                  :namespace (get-in base [:module :namespace]) :visibility :private
                  :definition-syntax-id :helper-name :upstream-binding-id :helper-upstream}
         definition {:binding-id :helper-binding :name 'without-path :core-node-id :helper-def
                     :value-node-id :helper-fn :syntax-id :helper-def}
         maps (mapv (fn [ordinal]
                      (let [ids (into {} (for [id (concat [:input] (get-in base [:nodes :input :children]))]
                                           [id (keyword (str (name id) "-" ordinal))]))
                            rename #(walk/postwalk-replace ids %)
                            literal-nodes (mapv #(rename (get-in base [:nodes %])) (get-in base [:nodes :input :children]))
                            map-node (-> (rename (get-in base [:nodes :input]))
                                         (assoc-in [:source :form-id] (ids :input))
                                         (assoc-in [:source :syntax-id] (ids :input)))
                            map-node (assoc-in map-node [:evaluation :owner-function-syntax-id] (keyword (str "caller-" ordinal)))
                            forms (into {} (for [[old id] ids]
                                             [id (rename (get-in base [:forms old]))]))
                            forms (assoc-in forms [(ids :input) :parent-form-id] (keyword (str "entry-" ordinal)))]
                        {:node map-node :nodes literal-nodes :forms forms
                         :types (into {} (for [[old id] ids] [id (get-in base [:types old])])) :id (ids :input)})) [0 1])
         entries (mapv (fn [ordinal input]
                         (let [id (keyword (str "entry-" ordinal)) op-id (keyword (str "entry-operator-" ordinal))
                               owner (keyword (str "caller-" ordinal)) children [op-id (:id input)]
                               op (-> (node op-id :reference [] {:symbol 'without-path :position :operator :binding-id :helper-binding
                                                                :binding-class :namespace :definition-syntax-id :helper-name} owner)
                                      (assoc :resolved-binding-ids [:helper-binding]))
                               call-node (-> (node id :call children {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                                                                     :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call} owner)
                                             (assoc :resolved-binding-ids [:helper-binding])
                                             (assoc-in [:evaluation :kind] :operator-then-arguments))
                               call {:core-node-id id :operator-node-id op-id :operator-binding-id :helper-binding
                                     :argument-node-ids [(:id input)] :ordered-evaluation-node-ids children
                                     :evaluation-order :operator-then-arguments :result-policy :call-result}
                               edge {:ordinal (inc ordinal) :call-core-node-id id :caller-function-syntax-id owner
                                     :caller-function-core-node-id owner :callee-binding-id :helper-binding :callee-definition-syntax-id :helper-name
                                     :callee-function-syntax-id :helper-form :callee-function-core-node-id :helper-fn
                                     :argument-core-node-ids [(:id input)] :ordered-evaluation-node-ids children
                                     :evaluation-order :operator-then-arguments :classification :local-function}]
                           {:node call-node :operator op :call call :edge edge
                            :caller {:function-syntax-id owner :function-core-node-id owner}
                            :forms {id (form id :list nil nil children) op-id (form op-id :symbol 'without-path id [])}
                            :use {:core-node-id op-id :form-id op-id :syntax-id op-id :symbol 'without-path :position :operator
                                  :binding-id :helper-binding :binding-class :namespace :definition-syntax-id :helper-name}
                            :resolution {:reference-syntax-id op-id :symbol 'without-path :position :operator :binding-id :helper-binding
                                         :upstream-binding-id :helper-upstream :resolution-order ordinal :source-span nil}})) [0 1] maps)
         receiver (-> (node :input :reference [] {:symbol 'span :position :expression :binding-id :span-binding
                                                 :binding-class :lexical :definition-syntax-id :span-name} :helper-form)
                      (assoc :resolved-binding-ids [:span-binding]))
         get-node (assoc-in (:node base) [:evaluation :owner-function-syntax-id] :helper-form)
         get-edge (assoc (:edge base) :caller-function-syntax-id :helper-form :caller-function-core-node-id :helper-fn)
         fn-node (-> (node :helper-fn :fn [:present-get] {} nil) (assoc :source (source :helper-form) :resolved-binding-ids [:span-binding]))
         def-node (-> (node :helper-def :def [:helper-fn] {:name 'without-path :name-syntax-id :helper-name :visibility :private} nil)
                      (assoc :resolved-binding-ids [:helper-binding]))
         all-nodes (vec (concat [(get-in base [:nodes :operator]) receiver (get-in base [:nodes :key]) get-node fn-node def-node]
                                (mapcat #(concat (:nodes %) [(:node %)]) maps)
                                (mapcat #(vector (:operator %) (:node %)) entries)))
         core (-> (:core base)
                  (assoc :nodes all-nodes :definitions [definition] :function-records (into [helper] (map :caller entries))
                         :calls (into [(:call base)] (map :call entries)) :call-edges (into [get-edge] (map :edge entries))
                         :reference-uses (mapv :use entries)
                         :preserved-resolution {:resolution-table (mapv :resolution entries) :alias-table []
                                                :binding-table (vec (vals (assoc (:bindings base) :span-binding parameter :helper-binding binding)))}
                         :mutations [] :var-references [] :recur-targets [] :recur-transfers [])
                  (assoc-in [:module :exports] ['main]))
         forms (merge (select-keys (:forms base) [:present-get :operator :key])
                      {:input (form :input :symbol 'span :present-get [])
                       :helper-def (form :helper-def :list nil nil [:def-op :helper-name :helper-form])
                       :helper-name (form :helper-name :symbol 'without-path :helper-def [])
                       :helper-form (form :helper-form :list nil :helper-def [])}
                      (apply merge (map :forms maps)) (apply merge (map :forms entries)))
         types (merge {:input (get-in base [:types :input]) :key :gravity.type/keyword} (apply merge (map :types maps)))]
     (assoc base :core core :module (:module core) :nodes (into {} (map (juxt :node-id identity) all-nodes))
                 :node get-node :edge get-edge :forms forms :types types
                 :bindings (assoc (:bindings base) :span-binding parameter :helper-binding binding)
                 :binding-types {:span-binding (:input types)}
                 :products {:module (:module core) :calls (:calls core) :call-edges (:call-edges core) :definitions [definition]
                            :function-index (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
                            :recursion-components []}))))


(defn- private-self-node
  [node-id core-form children attributes owner]
  {:node-id node-id
   :core-form core-form
   :children children
   :attributes attributes
   :evaluation {:owner-function-syntax-id owner}
   :source {:syntax-id node-id
            :semantic-span {:line 1 :column 1 :length 1}
            :origin-chain []
            :generated-origin nil}})

(defn private-self-census-data []
  (let [base (private-entry-get-input)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        record (assoc (first (get-in base [:core :function-records])) :body-core-node-id :recursive-body)
        bid (:definition-binding-id record) owner (:function-syntax-id record)
        operator {:node-id :self-op :core-form :reference :children [] :source (source :self-op)
                  :attributes {:symbol 'without-path :position :operator :binding-id bid
                               :binding-class :namespace :definition-syntax-id :helper-name}
                  :resolved-binding-ids [bid] :evaluation {:owner-function-syntax-id owner}}
        argument {:node-id :self-arg :core-form :reference :children [] :source (source :self-arg)
                  :attributes {:binding-id :span-binding} :resolved-binding-ids [:span-binding]
                  :evaluation {:owner-function-syntax-id owner}}
        call-node {:node-id :self-call :core-form :call :children [:self-op :self-arg] :source (source :self-call)
                   :resolved-binding-ids [bid]
                   :evaluation {:kind :operator-then-arguments :owner-function-syntax-id owner
                                :order [{:index 0 :core-node-id :self-op} {:index 1 :core-node-id :self-arg}]}}
        body {:node-id :recursive-body :core-form :do :children [:present-get :self-call]
              :source (source :recursive-body) :evaluation {:owner-function-syntax-id owner}}
        call {:core-node-id :self-call :operator-node-id :self-op :operator-binding-id bid
              :argument-node-ids [:self-arg] :ordered-evaluation-node-ids [:self-op :self-arg]
              :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 3 :call-core-node-id :self-call :caller-function-syntax-id owner :caller-function-core-node-id :helper-fn
              :callee-binding-id bid :callee-definition-syntax-id :helper-name :callee-function-syntax-id owner
              :callee-function-core-node-id :helper-fn :argument-core-node-ids [:self-arg]
              :ordered-evaluation-node-ids [:self-op :self-arg] :evaluation-order :operator-then-arguments :classification :local-function}
        use {:core-node-id :self-op :form-id :self-op :syntax-id :self-op :symbol 'without-path :position :operator
             :binding-id bid :binding-class :namespace :definition-syntax-id :helper-name}
        resolution {:reference-syntax-id :self-op :symbol 'without-path :position :operator :binding-id bid
                    :upstream-binding-id :helper-upstream :resolution-order 3 :source-span nil}
        component {:ordinal 0 :kind :self-recursive :function-syntax-ids [owner] :function-core-node-ids [:helper-fn]
                   :internal-call-edge-ordinals [3]}
        core (-> (:core base)
                 (update :nodes #(into (mapv (fn [node] (if (= :helper-fn (:node-id node))
                                                         (assoc node :children [:recursive-body]) node)) %)
                                       [operator argument call-node body]))
                 (assoc-in [:function-records 0] record)
                 (update :calls conj call) (update :call-edges conj edge)
                 (update :reference-uses conj use) (update-in [:preserved-resolution :resolution-table] conj resolution)
                 (assoc :recursion-components [component]))
        forms (merge (:forms base) {:self-op (form :self-op :symbol 'without-path :self-call [])
                                   :self-arg (form :self-arg :symbol 'span :self-call [])
                                   :self-call (form :self-call :list nil :recursive-body [:self-op :self-arg])})]
    {:core core :record record :nodes (into {} (map (juxt :node-id identity) (:nodes core)))
     :bindings (:bindings base) :forms forms}))

(defn private-self-string-proof-data []
  (let [{:keys [core record bindings forms]} (private-self-census-data)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        owner (:function-syntax-id record)
        condition (assoc (private-self-node :independent-condition :literal [] {:literal-kind :boolean :value false} owner)
                         :source (source :independent-condition))
        base (assoc (private-self-node :independent-base :reference [] {:binding-id :span-binding} owner)
                    :source (source :independent-base) :resolved-binding-ids [:span-binding])
        core (-> core
                 (update :calls #(vec (rest %)))
                 (update :call-edges #(mapv (fn [i edge] (assoc edge :ordinal i)) (range) (rest %)))
                 (assoc-in [:recursion-components 0 :internal-call-edge-ordinals] [2])
                 (update :nodes (fn [nodes]
                                  (conj (mapv (fn [n]
                                                (cond
                                                  (contains? #{:input-0 :input-1} (:node-id n))
                                                  (assoc n :core-form :literal :children []
                                                           :attributes {:literal-kind :string :value "entry"})
                                                  (= :recursive-body (:node-id n))
                                                  (assoc n :core-form :if :children [:independent-condition :self-call :independent-base])
                                                  (= :self-call (:node-id n))
                                                  (assoc n :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                                                                        :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                                                  (= :self-arg (:node-id n))
                                                  (assoc n :attributes {:symbol 'span :position :expression :binding-id :span-binding
                                                                        :binding-class :lexical :definition-syntax-id nil})
                                                  :else n))
                                              (remove #(contains? #{:operator :input :key :present-get} (:node-id %)) nodes))
                                        condition base))))
        forms (reduce (fn [out id] (assoc-in (assoc-in (assoc-in out [id :kind] :string) [id :value] "entry")
                                                       [id :child-form-ids] [])) forms [:input-0 :input-1])]
    {:core core :record record :bindings (vec (vals bindings)) :forms (vec (vals forms))}))

(defn private-self-homogeneous-head-data []
  (let [{:keys [core bindings forms]} (private-self-string-proof-data)
        type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        head (clojure.walk/postwalk-replace
              {:callee :helper-form :parameter :span-binding :function :helper-fn :branch :recursive-body}
              (homogeneous-list-head-input 2))
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        binding (first (filter #(= :span-binding (:binding-id %)) bindings))
        reference-attrs {:symbol (:name binding) :position :expression :binding-id :span-binding
                         :binding-class :lexical :definition-syntax-id (:definition-syntax-id binding)}
        nodes (-> (:nodes head)
                  (dissoc :helper-fn :base)
                  (assoc-in [:recursive-body :children] [:condition :independent-base :head-else])
                  (assoc-in [:arg0 :attributes] reference-attrs)
                  (assoc-in [:predicate-receiver :attributes] reference-attrs)
                  (assoc :head-else (assoc (private-self-node :head-else :do [:call :self-call] {} :helper-form)
                                          :source (source :head-else))))
        core (-> core
                 (update :nodes (fn [sequence]
                                  (let [retained (remove #(contains? (conj (set (keys nodes)) :independent-condition) (:node-id %)) sequence)
                                        all (merge (into {} (map (juxt :node-id identity) retained)) nodes)
                                        ;; C6 order: operands before each intrinsic consumer.
                                        order (concat (map :node-id retained)
                                                      [:predicate-operator :predicate-receiver :condition :operator :arg0 :call
                                                       :head-else :recursive-body])]
                                    (mapv all order))))
                 (update :function-records
                         #(mapv (fn [record]
                                  (case (:function-syntax-id record)
                                    :caller-0 (assoc record :parameter-binding-ids [:caller-parameter-0] :fixed-arity 1
                                                            :signature {:parameters [{:type-id type}]})
                                    :caller-1 (assoc record :parameter-binding-ids [:caller-parameter-1] :fixed-arity 1
                                                            :signature {:parameters [{:type-id type}]})
                                    record)) %))
                 (update :nodes #(mapv (fn [n] (case (:node-id n)
                                                :input-0 (assoc n :core-form :reference :attributes {:binding-id :caller-parameter-0})
                                                :input-1 (assoc n :core-form :reference :attributes {:binding-id :caller-parameter-1})
                                                n)) %))
                 (update :calls into (get-in head [:core :calls]))
                 (update :call-edges into
                         (mapv (fn [i edge] (assoc edge :ordinal (+ 3 i))) (range) (get-in head [:core :call-edges])))
                 (assoc :control-flow (-> (get-in head [:core :control-flow])
                                          (assoc-in [0 :branches 0 :core-node-id] :independent-base)
                                          (assoc-in [0 :branches 1 :core-node-id] :head-else))))
        forms (merge (into {} (map (juxt :form-id identity) forms)) (:forms head))
        bindings (merge (into {} (map (juxt :binding-id identity) bindings)) (:bindings head) {:span-binding binding})]
    {:core core :bindings (vec (vals bindings)) :forms (vec (vals forms)) :head head :type type}))

(defn indexed-private-self-data []
  (let [base (private-self-string-proof-data)
        core0 (:core base) record0 (:record base) owner (:function-syntax-id record0)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        make (fn [id kind children attrs caller]
               {:node-id id :core-form kind :children children :attributes attrs :source (source id)
                :resolved-binding-ids [] :evaluation {:owner-function-syntax-id caller :kind :left-to-right
                                                      :order (mapv (fn [i child] {:index i :core-node-id child}) (range) children)}})
        params [:span-binding :needle-binding :index-binding]
        parameter-bindings [{:binding-id :span-binding :name 'values :kind :local :binding-class :lexical}
                            {:binding-id :needle-binding :name 'value :kind :local :binding-class :lexical}
                            {:binding-id :index-binding :name 'index :kind :local :binding-class :lexical}]
        core-binding (fn [operation]
                       (-> (get-in (integral-ordering-input
                                    operation []) [:bindings :ordering-binding])
                           (assoc :binding-id operation :name operation :definition-syntax-id operation)))
        bindings (into {} (map (juxt :binding-id identity)
                               (concat (remove #(= :span-binding (:binding-id %)) (:bindings base))
                                       parameter-bindings (map core-binding '[count = get +]))))
        reference (fn [id bid]
                    (-> (make id :reference [] {:symbol (:name (bindings bid)) :position :expression :binding-id bid
                                                :binding-class :lexical :definition-syntax-id nil} owner)
                        (assoc :resolved-binding-ids [bid])))
        core-op (fn [id operation]
                  (-> (make id :reference [] {:symbol operation :position :operator :binding-id operation
                                              :binding-class :core :definition-syntax-id operation} owner)
                      (assoc :resolved-binding-ids [operation])))
        call (fn [id op args bid]
               {:core-node-id id :operator-node-id op :operator-binding-id bid :argument-node-ids args
                :ordered-evaluation-node-ids (into [op] args) :evaluation-order :operator-then-arguments :result-policy :call-result})
        call-node (fn [c caller]
                    (-> (make (:core-node-id c) :call (:ordered-evaluation-node-ids c)
                              {:operator-child-index 0 :argument-count (count (:argument-node-ids c))
                               :argument-child-indexes (vec (range 1 (inc (count (:argument-node-ids c)))))
                               :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call} caller)
                        (assoc :resolved-binding-ids [(:operator-binding-id c)])
                        (assoc-in [:evaluation :kind] :operator-then-arguments)))
        calls0 [(call :ix-count :ix-count-op [:ix-count-vector] 'count)
                (call :ix-guard :ix-guard-op [:ix-guard-index :ix-count] '=)
                (call :ix-get :ix-get-op [:ix-get-vector :ix-get-index] 'get)
                (call :ix-match :ix-match-op [:ix-match-value :ix-get] '=)
                (call :ix-step :ix-step-op [:ix-step-index :ix-unit] '+)
                (call :self-call :self-op [:ix-self-vector :ix-self-value :ix-step] :helper-binding)]
        by-id (into {} (map (juxt :core-node-id identity) calls0))
        if-attrs {:condition-child-index 0 :then-child-index 1 :else-child-index 2 :condition-evaluation :always-first
                  :branch-evaluation :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}}
        if-node (fn [id children]
                  (-> (make id :if children if-attrs owner)
                      (assoc-in [:evaluation :kind] :condition-then-exclusive-branch)
                      (assoc-in [:evaluation :order] [{:index 0 :core-node-id (first children)}])))
        self-op (first (filter #(= :self-op (:node-id %)) (:nodes core0)))
        body-nodes [(core-op :ix-guard-op '=) (reference :ix-guard-index :index-binding)
                    (core-op :ix-count-op 'count) (reference :ix-count-vector :span-binding) (call-node (by-id :ix-count) owner)
                    (call-node (by-id :ix-guard) owner) (make :ix-false :literal [] {:literal-kind :boolean :value false} owner)
                    (core-op :ix-match-op '=) (reference :ix-match-value :needle-binding)
                    (core-op :ix-get-op 'get) (reference :ix-get-vector :span-binding) (reference :ix-get-index :index-binding)
                    (call-node (by-id :ix-get) owner) (call-node (by-id :ix-match) owner)
                    (make :ix-true :literal [] {:literal-kind :boolean :value true} owner)
                    self-op (reference :ix-self-vector :span-binding) (reference :ix-self-value :needle-binding)
                    (core-op :ix-step-op '+) (reference :ix-step-index :index-binding)
                    (make :ix-unit :literal [] {:literal-kind :integer :value 1} owner)
                    (call-node (by-id :ix-step) owner) (call-node (by-id :self-call) owner)
                    (if-node :ix-inner [:ix-match :ix-true :self-call])
                    (if-node :recursive-body [:ix-guard :ix-false :ix-inner])]
        record (assoc record0 :parameter-binding-ids params :fixed-arity 3)
        kept (filter #(contains? #{:helper-fn :helper-def} (:node-id %)) (:nodes core0))
        kept (mapv #(if (= :helper-fn (:node-id %)) (assoc % :resolved-binding-ids params) %) kept)
        entries (mapv (fn [i]
                        (let [caller (keyword (str "caller-" i)) id (keyword (str "entry-" i))
                              input (keyword (str "input-" i)) needle (keyword (str "ix-needle-" i))
                              zero (keyword (str "ix-zero-" i))
                              members (mapv #(keyword (str "ix-member-" i "-" %)) (range 7))
                              old-call (first (filter #(= id (:core-node-id %)) (:calls core0)))
                              c (assoc old-call :argument-node-ids [input needle zero]
                                               :ordered-evaluation-node-ids [(:operator-node-id old-call) input needle zero])]
                          {:call c :caller caller :input input :needle needle :zero zero :members members
                           :nodes (vec (concat [(first (filter #(= (:operator-node-id c) (:node-id %)) (:nodes core0)))]
                                               (map #(make % :literal [] {:literal-kind :keyword :value :stored} caller) members)
                                               [(make input :collection-literal members {:literal-kind :vector :element-count 7} caller)
                                                (make needle :literal [] {:literal-kind :keyword :value :stored} caller)
                                                (make zero :literal [] {:literal-kind :integer :value 0} caller)
                                                (call-node c caller)]))})) [0 1])
        calls (into calls0 (map :call entries))
        edges (mapv (fn [i c]
                      (let [local? (= :helper-binding (:operator-binding-id c))
                            caller (if (< i 6) owner (:caller (nth entries (- i 6))))]
                        {:ordinal i :call-core-node-id (:core-node-id c) :caller-function-syntax-id caller
                         :caller-function-core-node-id (if (= caller owner) :helper-fn caller)
                         :callee-binding-id (:operator-binding-id c)
                         :callee-definition-syntax-id (if local? :helper-name (:operator-binding-id c))
                         :callee-function-syntax-id (when local? owner) :callee-function-core-node-id (when local? :helper-fn)
                         :argument-core-node-ids (:argument-node-ids c) :ordered-evaluation-node-ids (:ordered-evaluation-node-ids c)
                         :evaluation-order :operator-then-arguments :classification (if local? :local-function :nonlocal-or-nonfunction)}))
                    (range) calls)
        parents {:ix-count :ix-guard :ix-guard :recursive-body :ix-get :ix-match :ix-match :ix-inner :ix-step :self-call
                 :self-call :ix-inner :ix-false :recursive-body :ix-true :ix-inner :ix-inner :recursive-body}
        all-nodes (vec (concat body-nodes kept (mapcat :nodes entries)))
        child-parent (into {} (mapcat (fn [n] (map #(vector % (:node-id n)) (:children n))) body-nodes))
        forms0 (into {} (map (juxt :form-id identity) (:forms base)))
        body-forms (into {}
                         (mapcat (fn [n]
                                   (let [id (:node-id n) kind (:core-form n) children (:children n) attrs (:attributes n)
                                         parent (get parents id (get child-parent id))]
                                     (if (= :if kind)
                                       (let [op (keyword (str (name id) "-if-op"))]
                                         [[id (form id :list nil parent (into [op] children))]
                                          [op (form op :symbol 'if id [])]])
                                       [[id (form id (case kind :call :list :reference :symbol :literal (:literal-kind attrs))
                                                       (case kind :reference (:symbol attrs) :literal (:value attrs) nil)
                                                       parent children)]]))) body-nodes))
        entry-forms (into {}
                          (mapcat (fn [{:keys [call input needle zero members]}]
                                    (let [id (:core-node-id call) op (:operator-node-id call)]
                                      (concat [[id (form id :list nil nil (:ordered-evaluation-node-ids call))]
                                               [op (form op :symbol 'without-path id [])]
                                               [input (form input :vector nil id members)]
                                               [needle (form needle :keyword :stored id [])]
                                               [zero (form zero :integer 0 id [])]]
                                              (map #(vector % (form % :keyword :stored input [])) members)))) entries))
        forms (merge (select-keys forms0 [:helper-def :helper-name :helper-form]) body-forms entry-forms)
        forms (assoc-in forms [:recursive-body :parent-form-id] :helper-form)
        forms (assoc-in forms [:helper-form :child-form-ids] [:ix-fn-op :ix-parameters :recursive-body])
        forms (assoc forms :ix-fn-op (form :ix-fn-op :symbol 'fn :helper-form [])
                           :ix-parameters (form :ix-parameters :vector nil :helper-form []))
        controls (mapv (fn [n]
                         {:core-node-id (:node-id n) :kind :if :condition-node-id (first (:children n))
                          :branches [{:role :then :predicate :truthy :core-node-id (second (:children n))}
                                     {:role :else :predicate :falsey :core-node-id (nth (:children n) 2)}]
                          :branch-exclusivity :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}
                          :result-policy :selected-branch}) (filter #(= :if (:core-form %)) body-nodes))
        core (-> core0 (assoc :nodes all-nodes :calls calls :call-edges edges :control-flow controls)
                 (assoc-in [:function-records 0] record)
                 (assoc-in [:recursion-components 0 :internal-call-edge-ordinals] [5])
                 (assoc-in [:preserved-resolution :binding-table] (vec (vals bindings))))]
    {:core core :record record :nodes (into {} (map (juxt :node-id identity) all-nodes))
     :bindings (vec (vals bindings)) :forms (vec (vals forms))}))

(defn private-self-three-parameter-false-base-data []
  (let [{:keys [core record bindings forms]} (private-self-string-proof-data)
        owner (:function-syntax-id record)
        params [:flag-binding :span-binding :tag-binding]
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        make (fn [id kind attrs caller]
               {:node-id id :core-form kind :children [] :attributes attrs :source (source id)
                :resolved-binding-ids (if (= kind :reference) [(:binding-id attrs)] [])
                :evaluation {:owner-function-syntax-id caller :kind :left-to-right :order []}})
        ref (fn [id bid name] (make id :reference {:symbol name :position :expression :binding-id bid
                                                   :binding-class :lexical :definition-syntax-id nil} owner))
        extras (vec (concat [(ref :self-flag :flag-binding 'flag) (ref :self-tag :tag-binding 'tag)]
                            (mapcat (fn [i]
                                      [(make (keyword (str "flag-" i)) :literal {:literal-kind :boolean :value true} (keyword (str "caller-" i)))
                                       (make (keyword (str "tag-" i)) :literal {:literal-kind :keyword :value :tag} (keyword (str "caller-" i)))]) [0 1])))
        calls (mapv (fn [call]
                      (let [id (:core-node-id call)
                            args (if (= id :self-call) [:self-flag :self-arg :self-tag]
                                     (let [i (if (= id :entry-0) 0 1)]
                                       [(keyword (str "flag-" i)) (keyword (str "input-" i)) (keyword (str "tag-" i))]))]
                        (assoc call :argument-node-ids args :ordered-evaluation-node-ids (into [(:operator-node-id call)] args)))) (:calls core))
        call-by-id (into {} (map (juxt :core-node-id identity) calls))
        nodes (mapv (fn [n]
                      (let [id (:node-id n) call (call-by-id id)]
                        (cond
                          call (assoc n :children (:ordered-evaluation-node-ids call)
                                        :attributes {:operator-child-index 0 :argument-count 3 :argument-child-indexes [1 2 3]
                                                     :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
                                        :evaluation {:owner-function-syntax-id (get-in n [:evaluation :owner-function-syntax-id])
                                                     :kind :operator-then-arguments
                                                     :order (mapv #(hash-map :index %1 :core-node-id %2) (range) (:ordered-evaluation-node-ids call))})
                          (= id :independent-condition) (ref id :flag-binding 'flag)
                          (= id :independent-base) (make id :literal {:literal-kind :boolean :value false} owner)
                          (= id :recursive-body) (assoc n :children [:independent-condition :independent-base :self-call])
                          (= id :helper-fn) (assoc n :resolved-binding-ids params)
                          (contains? #{:input-0 :input-1} id) (assoc-in n [:attributes :value] "")
                          :else n))) (:nodes core))
        bindings (into bindings [{:binding-id :flag-binding :name 'flag :kind :local :binding-class :lexical}
                                 {:binding-id :tag-binding :name 'tag :kind :local :binding-class :lexical}])
        record (assoc record :fixed-arity 3 :parameter-binding-ids params)
        core (-> core (assoc :calls calls :nodes (into extras nodes))
                 (assoc-in [:function-records 0] record)
                 (update :call-edges #(mapv (fn [e] (let [call (call-by-id (:call-core-node-id e))]
                                                    (assoc e :argument-core-node-ids (:argument-node-ids call)
                                                             :ordered-evaluation-node-ids (:ordered-evaluation-node-ids call)))) %))
                 (assoc-in [:preserved-resolution :binding-table] bindings))
        forms (into {} (map (juxt :form-id identity) forms))
        forms (reduce (fn [out call]
                        (assoc-in out [(:core-node-id call) :child-form-ids] (:ordered-evaluation-node-ids call))) forms calls)
        forms (reduce (fn [out n]
                        (let [id (:node-id n) self? (= owner (get-in n [:evaluation :owner-function-syntax-id]))
                              parent (if self? :self-call (keyword (str "entry-" (if (= id :flag-0) 0 (if (= id :tag-0) 0 1)))))]
                          (assoc out id (form id (if self? :symbol (get-in n [:attributes :literal-kind]))
                                                (if self? (get-in n [:attributes :symbol]) (get-in n [:attributes :value]))
                                                parent [])))) forms extras)
        forms (assoc forms :independent-condition (form :independent-condition :symbol 'flag :recursive-body [])
                           :independent-base (form :independent-base :boolean false :recursive-body []))
        forms (reduce (fn [out id] (assoc-in out [id :value] "")) forms [:input-0 :input-1])]
    {:core core :record record :bindings bindings :forms (vec (vals forms))}))

(defn- private-self-throw-data
  [data]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        make (fn [id kind children attrs] (assoc (private-self-node id kind children attrs :helper-form) :source (source id)))
        additions [(make :argument-condition :literal [] {:literal-kind :boolean :value true})
                   (make :argument-payload :literal [] {:literal-kind :string :value "deep"})
                   (make :argument-transfer :throw [:argument-payload] {})
                   (make :argument-normal :reference [] {:symbol 'span :position :expression :binding-id :span-binding
                                                          :binding-class :lexical :definition-syntax-id nil})
                   (make :self-argument-if :if [:argument-condition :argument-transfer :argument-normal] {})]
        core (-> (:core data)
                 (update :function-records #(vector (first %)))
                 (assoc-in [:module :exports] [])
                 (assoc-in [:module :effects] [:error/throw])
                 (update :nodes #(into additions %))
                 (update :nodes #(mapv (fn [n]
                                         (let [n (if (= :self-call (:node-id n))
                                                   (assoc n :children [:self-op :self-argument-if]) n)
                                               owner (get-in n [:evaluation :owner-function-syntax-id])
                                               owner (when-not (contains? #{:caller-0 :caller-1} owner) owner)
                                               children (:children n) kind (:core-form n)
                                               ordered (cond (= kind :fn) [] (= kind :if) [(first children)] :else children)]
                                           (assoc n :evaluation
                                                  {:owner-function-syntax-id owner
                                                   :kind (case kind :call :operator-then-arguments :if :condition-then-exclusive-branch :left-to-right)
                                                   :order (mapv (fn [i id] {:index i :core-node-id id}) (range) ordered)}))) %))
                 (update :calls #(mapv (fn [c] (if (= :self-call (:core-node-id c))
                                                 (assoc c :argument-node-ids [:self-argument-if]
                                                          :ordered-evaluation-node-ids [:self-op :self-argument-if]) c)) %))
                 (update :call-edges #(mapv (fn [e] (if (= :self-call (:call-core-node-id e))
                                                      (assoc e :argument-core-node-ids [:self-argument-if]
                                                               :ordered-evaluation-node-ids [:self-op :self-argument-if])
                                                      (assoc e :caller-function-syntax-id nil :caller-function-core-node-id nil))) %)))
        forms (mapv #(if (= :self-call (:form-id %)) (assoc % :child-form-ids [:self-op :self-argument-if]) %) (:forms data))
        forms (conj forms {:form-id :self-argument-if :syntax-id :self-argument-if :kind :list :parent-form-id :self-call
                           :source-span nil :origin-chain [] :generated-origin nil
                           :child-form-ids [:argument-condition :argument-transfer :argument-normal]})
        ids (into {} (map-indexed (fn [i n] [(:node-id n) (sha (+ 30000 i))]) (:nodes core)))
        renamed (walk/postwalk-replace ids (assoc data :core core :forms forms))]
    (assoc renamed :node-id-map ids)))

(defn private-self-value-kernel-data
  "Build the shared private-self C7 fixture without loading SH-09 tests."
  [kind]
  (let [invoke invoke-c7
        indexed? (contains? #{:indexed :indexed-sha} kind)
        data (cond
               indexed? (indexed-private-self-data)
               (= kind :prior-three) (private-self-three-parameter-false-base-data)
               (= kind :head) (private-self-homogeneous-head-data)
               :else (private-self-string-proof-data))
        data (if (= kind :throw) (private-self-throw-data data) data)
        data (if (= kind :indexed-sha)
               (let [ids (set (concat (map :node-id (get-in data [:core :nodes]))
                                      (map :form-id (:forms data))
                                      (map :binding-id (:bindings data))
                                      (map :function-syntax-id (get-in data [:core :function-records]))))
                     renamed (into {} (map-indexed (fn [i id] [id (sha (+ 51000 i))])
                                                  (sort-by str (filter keyword? ids))))]
                 (assoc (walk/postwalk-replace renamed data) :node-id-map renamed)) data)
        {:keys [core bindings forms]} data
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        shape {:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
        proof (:recursive-proof (invoke 'sh08-ft-proof-results [shape (assoc core :node-table nodes) bindings (:module core)]))
        skeletons (invoke 'sh08-ft-function-skeletons [(:function-records core) (:module core)])
        functions (invoke 'sh08-ft-complete-function-skeletons [skeletons proof {}])
        function (get functions (:function-syntax-id proof))
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise proof))
        cold (invoke 'sh08-is-cold-infer [working core (first (:function-records core)) nodes bindings forms {}
                                         :body (:parameter-types proof)])
        return (:inferred-return-type proof)
        completed-types (walk/postwalk-replace (get data :node-id-map {})
                        (merge
                         {:self-op function :entry-operator-0 function :entry-operator-1 function
                          :self-call return :entry-0 return :entry-1 return :recursive-body return
                          :helper-fn function :helper-def function}
                         (when (= kind :head) {:head-else return})
                         (when indexed? {:ix-inner return})))
        types (merge (get-in cold [:inference :type-table]) completed-types)
        ledger (invoke 'sh08-ft-recursive-constraint-ledger [proof])
        call-facts (invoke 'sh08-ft-call-facts [(:calls core) (:call-edges core) nodes types functions (:module core)])
        facts {:domain (if indexed? :gravity/sh08-indexed-private-self-value-typed-core-v1
                           :gravity/sh08-private-self-value-typed-core-v1)
               :recursive-proof (invoke 'sh08-ft-recursive-proof-input [proof])
               :function-type-table (if indexed?
                                      (invoke 'sh08-ft-function-facts [functions nodes (:module core)])
                                      (vec (vals functions)))
               :type-table types
               :call-type-facts call-facts :constraint-ledger (into ledger (get-in cold [:inference :constraints]))}]
    {:core core :facts facts :bindings (into {} (map (juxt :binding-id identity) bindings))
     :forms forms :nodes nodes :proof proof :cold cold :invoke invoke :data data}))

(defn declared-self-kernel-data
  "Build the declared-self effects fixture without loading the SH-09 suite."
  []
  (let [[old-core] (recursive-kernel-data)
        owner #(assoc-in % [:evaluation :owner-function-syntax-id] (sha 20))
        signature {:parameters [{:type-id :gravity.type/string}]
                   :return {:type-id :gravity.type/string}}
        function {:function-id (sha 20) :body-core-node-id (sha 6)
                  :parameters [{:binding-id (sha 51) :type :gravity.type/string}]
                  :return :gravity.type/string :latent-effects [:error/throw]
                  :thrown-error-effects [:pending-sh09]}
        payload (owner (assoc (node 12 :literal []) :attributes {:literal-kind :string :value "deep"}))
        nodes (-> (:nodes old-core)
                  (assoc 2 (owner (assoc (node 3 :literal []) :attributes {:literal-kind :string :value "next"})))
                  (assoc 4 (owner (node 5 :throw [12])))
                  (assoc-in [9 :attributes] {:literal-kind :string :value "entry"})
                  (conj payload))
        calls [(assoc (get-in old-core [:calls 0])
                      :operator-binding-id (sha 50)
                      :ordered-evaluation-node-ids (mapv sha [2 3])
                      :evaluation-order :operator-then-arguments)
               (assoc (get-in old-core [:calls 1]) :operator-binding-id (sha 50))]
        core (-> old-core (assoc :nodes nodes :calls calls :lexical-bindings [])
                 (assoc-in [:module :effects] [:error/throw])
                 (assoc-in [:function-records 0 :signature] signature))
        bindings {(sha 50) {:binding-id (sha 50)} (sha 51) {:binding-id (sha 51)}}
        proof {:domain :gravity/sh08-declared-self-value-proof-v1
               :function-syntax-id (sha 20) :function-core-node-id (sha 7)
               :definition-binding-id (sha 50) :definition-syntax-id (sha 80)
               :body-core-node-id (sha 6) :parameter-binding-ids [(sha 51)]
               :parameter-types [:gravity.type/string] :declared-return-type :gravity.type/string
               :signature signature :component (first (:recursion-components core))
               :self-call-core-node-id (sha 4) :self-call (first calls) :self-edge (first (:call-edges core))
               :self-call-source (:source (get nodes 3))
               :owned-node-ids (mapv :node-id (filter #(= (sha 20) (get-in % [:evaluation :owner-function-syntax-id])) nodes))
               :incoming-call-node-ids (mapv sha [4 11])
               :value-check :complete-body-and-all-incoming-arguments
               :effect-error-status :pending-sh09-recursive-body
               :captures :own-parameters-and-lexical-locals-only}
        types (merge (zipmap (map :node-id nodes) (repeat :gravity.type/string))
                     {(sha 1) :gravity.type/bool (sha 5) :gravity.type/never (sha 7) function (sha 8) function})
        facts {:recursive-proof proof :function-type-table [function] :type-table types :constraint-ledger []
               :call-type-facts [{:call-core-node-id (sha 4) :result-type :gravity.type/string}
                                {:call-core-node-id (sha 11) :result-type :gravity.type/string}]}]
    [core facts bindings]))

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

(def ^:private c8-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")

(def c8-plan
  (delay (compile-c8-module-bundle (path c8-source-relative-path))))

(defn invoke-c8
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh09-c7-effect-adapter-test
    :compiler-artifact-plan? true}
   @c8-plan function arguments))

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

(def ^:private function-sha-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def ^:private function-sha-b
  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
(def ^:private function-sha-c
  "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
(def ^:private function-sha-d
  "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
(def ^:private function-sha-e
  "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")

(defn function-typed-result
  [actual-path]
  (let [module
        {:namespace 'gravity.self-hosting.sh09.function-synthetic
         :profile :meta :target :jvm
         :effects [] :capabilities [] :safety :safe}
        functions
        [{:artifact :gravity/c7-function-type
          :function-id function-sha-a
          :function-core-node-id function-sha-b
          :body-core-node-id function-sha-c
          :latent-effects []
          :capabilities []
          :thrown-error-effects [:pending-sh09]
          :profile :meta
          :target :jvm
          :source-span {:source "function.gravity"
                        :start-byte 0 :end-byte 1}
          :status :inferred}]
        calls
        [{:artifact :gravity/c7-call-type-fact
          :fact-id-request
          {:domain :gravity/sh08-call-type-fact-v1
           :call-core-node-id function-sha-d
           :callee-function-syntax-id function-sha-a
           :argument-types [:gravity.type/bool]
           :result-type :gravity.type/bool}
          :call-core-node-id function-sha-d
          :caller-function-syntax-id function-sha-a
          :callee-function-syntax-id function-sha-a
          :argument-types [:gravity.type/bool]
          :result-type :gravity.type/bool
          :evaluation-order :operator-then-arguments
          :syntax-id function-sha-e
          :source-span {:source "function.gravity"
                        :start-byte 2 :end-byte 3}
          :origin-chain []
          :generated-origin []
          :profile :meta
          :target :jvm
          :effects []
          :capabilities []
          :status :inferred}]
        proof {:domain :gravity/sh08-authoritative-higher-order-proof-v1}
        typed-core
        {:artifact :gravity/typed-core
         :core-input function-sha-e
         :module module
         :types {function-sha-d :gravity.type/bool}
         :constraints []
         :function-types functions
         :local-bindings []
         :calls calls
         :diagnostics []
         :higher-order-proof proof
         :higher-order-call-facts []}
        function-products
        {:function-records []
         :call-edges []
         :recursion-components []
         :lexical-bindings []}
        identity
        {:domain :gravity/sh08-authoritative-higher-order-type-v1
         :sh07-shaped-artifact-id function-sha-e
         :module module
         :function-records (:function-records function-products)
         :call-edges (:call-edges function-products)
         :recursion-components (:recursion-components function-products)
         :lexical-bindings (:lexical-bindings function-products)
         :function-type-table functions
         :local-binding-facts []
         :call-type-facts calls
         :constraint-ledger []
         :type-table {function-sha-d :gravity.type/bool}
         :convergence {:status :converged}
         :pending [:records :unions]
         :higher-order-proof proof}]
    {:artifact :gravity/sh08-function-typed-core-template
     :schema-version 3
     :status :accepted
     :scope :capture-free-higher-order-fixed-arity-one-hop
     :authentication-status :host-resolved-b47-verification-boundary
     :module module
     :sh07-shaped-artifact-id function-sha-e
     :function-products function-products
     :function-type-table functions
     :local-binding-facts []
     :call-type-facts calls
     :constraint-ledger []
     :type-table {function-sha-d :gravity.type/bool}
     :convergence {:status :converged}
     :typed-core typed-core
     :diagnostics []
     :artifact-id-request identity
     :identity-input identity
     :identity-resolution :coordinator-digest-required
     :provenance {:actual-source-path actual-path}
     :pending [:records :unions]
     :higher-order-proof proof
     :higher-order-call-facts []}))

(defn function-verification
  [typed]
  {:artifact :gravity/sh08-function-type-verification
   :status :passed
   :checks [:coherent-b47-function-records
            :path-neutral-identity-input]
   :expected typed
   :candidate typed})

(defn typed-direct-call-kernel-context
  [base]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain nil :generated-origin nil})
        record (assoc (get-in base [:records 'identity]) :definition-binding-id :target-binding)
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :target-binding :argument-node-ids [:argument]
              :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id :caller-fn :caller-function-core-node-id :caller-core
              :callee-binding-id :target-binding :callee-definition-syntax-id :target-name :callee-function-syntax-id :fn :callee-function-core-node-id :function
              :argument-core-node-ids [:argument] :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :classification :local-function}
        module (get-in base [:core :module])
        fact {:artifact :gravity/c7-call-type-fact
              :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id :call :callee-function-syntax-id :fn
                                :argument-types [:gravity.type/string] :result-type :gravity.type/string}
              :call-core-node-id :call :caller-function-syntax-id :caller-fn :callee-function-syntax-id :fn
              :argument-types [:gravity.type/string] :result-type :gravity.type/string :evaluation-order :operator-then-arguments
              :source-span nil :syntax-id :call-form :origin-chain nil :generated-origin nil
              :profile (:profile module) :target (:target module) :effects (:effects module) :capabilities (:capabilities module) :status :inferred}
        row {:constraint-id-request {:domain :gravity/sh08-function-call-constraint-v1 :call-core-node-id :call :callee-function-syntax-id :fn}
             :kind :equality :lhs :fn :rhs [{:parameter :parameter :argument :gravity.type/string :chosen :gravity.type/string}]
             :origin nil :profile :meta :target :jvm :status :solved :diagnostics []}]
    (-> base (assoc :locals {:caller-parameter 'problem} :parameter-types {:caller-parameter :gravity.type/string}
                    :current-function {:function-syntax-id :caller-fn :function-core-node-id :caller-core}
                    :calls {:call call} :edges {:call edge} :call-facts {:call fact} :call-order {:call 0} :recursion-components [] :ledger [row])
        (assoc-in [:core :function-records] [record]) (assoc-in [:records 'identity] record)
        (assoc-in [:bindings :target-binding] {:binding-id :target-binding :name 'identity :definition-syntax-id :target-name
                                              :binding-class :namespace :kind :function :namespace (:namespace module)})
        (update :nodes merge {:operator {:node-id :operator :core-form :reference :children [] :resolved-binding-ids [:target-binding]
                                         :attributes {:binding-id :target-binding :symbol 'identity :position :operator :binding-class :namespace :definition-syntax-id :target-name}
                                         :source (source :operator-form)}
                              :argument {:node-id :argument :core-form :reference :children [] :resolved-binding-ids [:caller-parameter]
                                         :attributes {:binding-id :caller-parameter} :source (source :argument-form)}
                              :call {:node-id :call :core-form :call :children [:operator :argument] :resolved-binding-ids [:target-binding]
                                     :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1] :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
                                     :evaluation {:kind :operator-then-arguments :owner-function-syntax-id :caller-fn
                                                  :order [{:index 0 :core-node-id :operator} {:index 1 :core-node-id :argument}]} :source (source :call-form)}})
        (update :types merge {:operator (get-in base [:functions 'identity]) :argument :gravity.type/string :call :gravity.type/string})
        (update :forms merge {:name-form {:form-id :name-form :syntax-id :target-name :value 'identity :parent-form-id :definition-form}
                              :call-form {:form-id :call-form :syntax-id :call-form :kind :list :child-form-ids [:operator-form :argument-form]}
                              :operator-form {:form-id :operator-form :syntax-id :operator-form :kind :symbol :value 'identity :parent-form-id :call-form}
                              :argument-form {:form-id :argument-form :syntax-id :argument-form :kind :symbol :value 'problem :parent-form-id :call-form}}))))

(defn prepend-effect-input
  [receiver value ordinal input-fn c7]
  (let [base (input-fn receiver value)
        sha (fn [n] (str "sha256:" (format "%064x" n)))
        ids (zipmap (keys (:nodes base))
                    (map sha (range (+ 1800 (* 10 ordinal)) (+ 1810 (* 10 ordinal)))))
        source #(-> % (update :form-id ids) (update :syntax-id ids))
        node-list (mapv (fn [node]
                          (-> node
                              (update :node-id ids)
                              (update :children #(mapv ids %))
                              (update :source source)
                              (update-in [:evaluation :order]
                                         #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
                        (get-in base [:core :nodes]))
        forms (into {}
                    (for [[id form] (:forms base)]
                      [(ids id)
                       (-> form (update :form-id ids) (update :syntax-id ids)
                           (update :parent-form-id ids) (update :child-form-ids #(mapv ids %)))]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (assoc :ordinal ordinal) (update :call-core-node-id ids)
                 (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        module (assoc (:module base) :effects [:memory/allocate])
        core (assoc (:core base) :module module :nodes node-list :calls [call] :call-edges [edge])
        nodes (into {} (map (juxt :node-id identity) node-list))
        node (get nodes (:core-node-id call))
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        prepend-owner? (or (= receiver :gravity.type/list) (= :gravity.type/list (:kind receiver))
                           (= receiver :gravity.type/set) (= :gravity.type/set (:kind receiver)))
        prefix (if prepend-owner? "sh08-pl-" "sh08-va-")
        result (c7 (symbol (str prefix "derive"))
                   [core node call edge nodes (:bindings base) types forms])
        descriptor (:descriptor result)
        facts {:module module
               :type-table (assoc types (:operator-node-id call) descriptor (:node-id node) (:type result))
               :function-type-table []
               :call-type-facts [(c7 (symbol (str prefix "call-fact")) [descriptor module])]
               :constraint-ledger [(c7 (symbol (str prefix "constraint")) [descriptor])]}]
    {:core core :nodes nodes :node node :forms forms :call call :edge edge :ids ids
     :bindings (:bindings base) :descriptor descriptor :result result :facts facts
     :request {:canonical-core-artifact core
               :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}}))
