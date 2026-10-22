(ns gravity.self-hosting.sh12-branch-join-mir-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh12_branch_join_mir_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-12 branch/join test is not on the classpath"
        {:id "SH12-BRANCH-JOIN-TEST-ROOT"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH12-BRANCH-JOIN-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(def ^:private c11-source
  "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")

(def ^:private accepted-fixtures
  {".gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/branch_join_mir.gravity"
   ".qst"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/branch_join_mir.qst"})

(def ^:private rejected-fixtures
  {".gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/rejected/malformed_branch_join_mir.gravity"
   ".qst"
   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/rejected/malformed_branch_join_mir.qst"})

(defn- compile-plan
  [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c11-plan (delay (compile-plan c11-source)))

(def ^:private accepted-plans
  (into {}
        (map (fn [[extension path]]
               [extension (delay (compile-plan path))]))
        accepted-fixtures))

(def ^:private rejected-plans
  (into {}
        (map (fn [[extension path]]
               [extension (delay (compile-plan path))]))
        rejected-fixtures))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh12-branch-join-mir-test
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- checked-core
  [extension]
  (invoke
   (get accepted-plans extension)
   'sh12-accepted-branch-join-checked-core
   [(str (.resolve @root (get accepted-fixtures extension)))]))

(defn- rejected-path
  [extension]
  (str (.resolve @root (get rejected-fixtures extension))))

(defn- build-mir
  [checked]
  (invoke c11-plan 'sh12-build-branch-join-mir [checked]))

(defn- verify-mir
  [checked mir]
  (invoke c11-plan 'sh12-verify-branch-join-mir [checked mir]))

(defn- diagnostic
  [report]
  (first (:diagnostics report)))

(def ^:private parameter-c6-source
  "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")

(def ^:private parameter-c6-plan (delay (compile-plan parameter-c6-source)))

(deftest sh12-current-c6-selects-last-result-sources
  ;; Direct original Gravity helpers on bounded DATA, not fabricated Core or
  ;; source admission. Every production caller supplies a bounded Vector.
  (doseq [[values expected] [[[] nil] [[:only] :only] [[:first :last] :last]
                            [[:first :middle :last] :last] [[:first nil] nil] [[:first false] false]]]
    (is (= expected (invoke parameter-c6-plan 'c6c10-last-value [values]))))
  (let [payload {:inert [false nil]}]
    (is (identical? payload (invoke parameter-c6-plan 'c6c10-last-value [[:first payload]]))))
  (doseq [operation [:do :let :function]
          operands [[:only] [:first :last] [:first :middle :last]]]
    (is (= [(last operands)]
           (invoke parameter-c6-plan 'c6c10-source-value-ids
                   [{:source-operation operation :operands operands}]))))
  (doseq [[operation operands expected]
          [[:local [:binding] [:binding]] [:let-binding [:initializer] [:initializer]]
           [:truthy [:condition] [:condition]] [:if [:test :then :else] [:then :else]]
           [:literal [] []]]]
    (is (= expected (invoke parameter-c6-plan 'c6c10-source-value-ids
                           [{:source-operation operation :operands operands}])))))

(defn- parameter-compiled-binding-data [source-path]
  ;; Immutable DATA from the same delayed compiled C6 plan, not a cached
  ;; source ingress, accepted Core, verifier report or source admission.
  (let [plan @parameter-c6-plan hash (get-in plan [:source :sha256])
        digest #(bootstrap/p15-s23-c6c10-canonical-digest source-path %)
        manifest (bootstrap/p15-s23-c6c10-function-merkle-manifest source-path hash (:functions plan))
        binding {:source-path (str (.resolve @root parameter-c6-source))
                 :source-content-hash hash
                 :plan-semantic-hash (digest (bootstrap/p15-s23-c6c10-plan-pin-input hash plan manifest))
                 :builder-semantic-hash
                 (digest {:domain :gravity/c6-c10-exported-builder-v1 :source-content-hash hash
                          :definition (bootstrap/p15-s23-c6c10-path-neutral-value
                                       hash (get-in plan [:functions 'c6-c10-build-checked-core-template]))})}]
    binding))

(defn- parameter-source-products
  ([source-path source-text] (parameter-source-products source-path source-text nil))
  ([source-path source-text compiled-binding]
   (parameter-source-products source-path source-text compiled-binding nil))
  ([source-path source-text compiled-binding reader-source-path]
   ;; Always reconstruct original source-derived C2/C5 BEFORE using binding
   ;; DATA. These products remain development DATA, not frozen admission.
   (let [ingress ((ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-private-ingress-products)
                  source-path source-text reader-source-path)
         binding (if (nil? compiled-binding) (parameter-compiled-binding-data source-path) compiled-binding)]
     {:ingress ingress :compiled-binding-data binding
      :envelope (bootstrap/p15-s23-c6c10-private-builder-envelope source-path ingress binding)})))

(defn- parameter-input [source-path source-text]
  (:envelope (parameter-source-products source-path source-text)))

(deftest sh12-current-reader-parent-products-feed-original-parameter-production
  ;; Every source call constructs current C2/C3 and its genuine SH05/SH06
  ;; parents. Neither a historical pin nor an accepted artifact is substituted.
  (let [directory "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        binding (parameter-compiled-binding-data
                  (str (.resolve @root (str directory "bool_parameter_source.gravity"))))]
    (doseq [extension ["gravity" "qst"]]
      (let [path (str (.resolve @root (str directory "bool_parameter_source." extension)))
            text (slurp path) products (parameter-source-products path text binding reader-path)
            input (:envelope products)
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
            template (:artifact-template raw)
            fresh (parameter-source-products path text binding reader-path)
            replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
            verify #(invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                            [(:envelope fresh) % (:digest-requests replay)])]
        (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
        (is (= input (:envelope fresh)))
        (is (= raw replay))
        (is (= :passed (:status (verify template))))
        (is (= :gravity/c6-c10-resolved-bool-parameter-v1 (get-in input [:resolved-parameter :kind])))
        (is (= [:parameter :local :truthy :literal :literal :if :function]
               (mapv :source-operation (:core-nodes template))))
        (is (= [:gravity/bool] (get-in (last (:core-nodes template)) [:type :params])))
        (is (= :rejected (:status (verify (assoc-in template [:core-nodes 0 :type] :gravity/i64)))))
        (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
              context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
              core (bootstrap/p15-s23-c6c10-public-artifact
                     context (:compiled-binding-data products) (:ingress products) sealed)
              mir (build-mir core)]
          (is (= (:sealed-artifact-template sealed)
                 (dissoc core :target-request-metadata :physical-provenance)))
          (is (= :passed (:status (verify-mir core mir)))))))))

(deftest sh12-compiled-binding-data-reuse-preserves-fresh-source-products
  ;; Six original source ingresses: one default baseline per extension, then
  ;; four G/G/Q/Q supplied-binding calls. No Core/result/report is cached.
  (let [paths (mapv #(str (.resolve @root
                           (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_bool_parameter_source." %)))
                    ["gravity" "qst"])
        texts (mapv slurp paths)
        ingress-var (ns-resolve 'gravity.bootstrap 'p15-s23-c6c10-private-ingress-products)
        original-ingress @ingress-var
        original-manifest bootstrap/p15-s23-c6c10-function-merkle-manifest
        ingress-calls (atom []) manifest-calls (atom 0)]
    (with-redefs-fn
      {ingress-var (fn [path text]
                     (swap! ingress-calls conj [path text])
                     (original-ingress path text))
       #'bootstrap/p15-s23-c6c10-function-merkle-manifest
       (fn [path hash functions]
         (swap! manifest-calls inc)
         (original-manifest path hash functions))}
      (fn []
        (let [baselines (mapv parameter-source-products paths texts)
              binding (:compiled-binding-data (first baselines))
              plan @parameter-c6-plan hash (get-in plan [:source :sha256])
              diagnostic-path (second paths)]
          ;; Positive controls establish that both original observers are live.
          (is (= (mapv vector paths texts) @ingress-calls))
          (is (= 2 @manifest-calls))
          (is (= binding (:compiled-binding-data (second baselines))))
          ;; Independent original inline formula, using the other fixture's
          ;; diagnostic context. The binding DATA itself is path-neutral.
          (let [digest #(bootstrap/p15-s23-c6c10-canonical-digest diagnostic-path %)
                manifest (bootstrap/p15-s23-c6c10-function-merkle-manifest
                           diagnostic-path hash (:functions plan))
                expected {:source-path (str (.resolve @root parameter-c6-source))
                          :source-content-hash hash
                          :plan-semantic-hash
                          (digest (bootstrap/p15-s23-c6c10-plan-pin-input hash plan manifest))
                          :builder-semantic-hash
                          (digest {:domain :gravity/c6-c10-exported-builder-v1 :source-content-hash hash
                                   :definition (bootstrap/p15-s23-c6c10-path-neutral-value
                                                hash (get-in plan [:functions 'c6-c10-build-checked-core-template]))})}]
            (is (= expected binding))
            (is (= 3 @manifest-calls)))
          (reset! ingress-calls [])
          (reset! manifest-calls 0)
          (let [indexes [0 0 1 1]
                products (mapv #(parameter-source-products (get paths %) (get texts %) binding) indexes)]
            (is (= (mapv #(vector (get paths %) (get texts %)) indexes) @ingress-calls))
            (is (= 4 (count @ingress-calls)))
            (is (zero? @manifest-calls))
            (doseq [[index product] (map vector indexes products)]
              (is (= (get baselines index) product))
              (is (= (:envelope (get baselines index)) (:envelope product)))
              (is (= (:ingress (get baselines index)) (:ingress product)))
              (is (identical? binding (:compiled-binding-data product))))
            (println "compiled-binding DATA parity" :compiled-function-count (count (:functions plan))
                     :candidate-original-ingress-calls (count @ingress-calls)
                     :candidate-manifest-constructions @manifest-calls)))))))

(deftest sh12-current-c6-types-the-last-body-result-and-preserves-diagnostic-selection
  (let [source (str (.resolve @root
                     "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        temporary (java.io.File/createTempFile "gravity-c6-last-body-result-" ".gravity")]
    (try
      ;; A real two-expression function: its first body value is Bool and its
      ;; last is Integer. No patched Core or source-admission assertion.
      (spit temporary (str/replace (slurp source) "(if flag true false)" "flag 7"))
      (let [path (.getAbsolutePath temporary) text (slurp temporary)
            input (parameter-input path text)
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
            fresh (parameter-input path text)
            replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [fresh])
            core (:artifact-template raw) nodes (:core-nodes core)
            first-body (get-in nodes [1 :node-id]) last-body (get-in nodes [2 :node-id])
            function (get-in nodes [3 :node-id])
            first-type (get-in core [:type-facts first-body :type-id])
            last-type (get-in core [:type-facts last-body :type-id])
            verify (fn [candidate]
                     (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                             [fresh candidate (:digest-requests replay)]))
            front (:front-end-products input) syntaxes (:c3-syntax-object-stream front)
            forms (:form-tree front) leaf (first (filter #(= :integer (:kind %)) forms))
            first-syntax (first syntaxes) last-syntax (last syntaxes)
            exact-form (first (filter #(= (get-in first-syntax [:source :form-id]) (:form-id %)) forms))
            reject (fn [envelope form]
                     (invoke parameter-c6-plan 'c6c10-input-form-rejection
                             [:C6-CORE-SHAPE :lower-to-core :diagnostic/c6-core-shape envelope form
                              {:missing-fact :selected-last-syntax-test} :repair-source-form]))]
        (is (= :accepted (:status raw)))
        (is (= [:parameter :local :literal :function] (mapv :source-operation nodes)))
        (is (= [first-body last-body] (get-in nodes [3 :operands])))
        (is (= :gravity/integer (get-in nodes [3 :type :return])))
        (is (not= first-type last-type))
        (is (= last-type (get-in core [:typed-core :functions function :return])))
        (is (= [last-body] (get-in core [:ownership-facts function :source-value-ids])))
        (is (= :passed (:status (verify core))))
        (is (= :rejected (:status (verify (assoc-in core [:typed-core :functions function :return] first-type)))))
        (is (nil? (invoke parameter-c6-plan 'c6c10-top-level-syntax-by-form-id [syntaxes (:form-id leaf)])))
        (is (not= (:syntax/id first-syntax) (:syntax/id last-syntax)))
        (let [fallback (reject input leaf) exact (reject input exact-form)
              absent (reject (assoc-in input [:front-end-products :c3-syntax-object-stream] []) leaf)]
          (is (= :rejected (:status fallback) (:status exact) (:status absent)))
          (is (= "C6-CORE-SHAPE" (get-in fallback [:diagnostics 0 :rule])))
          (is (= (:syntax/id last-syntax) (get-in fallback [:diagnostics 0 :primary :syntax-id])))
          (is (= (:syntax/id first-syntax) (get-in exact [:diagnostics 0 :primary :syntax-id])))
          (is (= :not-applicable (get-in absent [:diagnostics 0 :primary :syntax-id])))))
      (finally (.delete temporary)))))

(deftest sh12-current-c6-forwards-ordered-i64-parameter-arms
  ;; c6c10-product-depth's scalar selection subtree, not Map field typing,
  ;; a range witness, a new max primitive or execution of the whole helper.
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_i64_depth_choice_source.gravity"))
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        input (:envelope (parameter-source-products path (slurp path) nil reader-path))
        raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
    (is (= :gravity/c6-c10-resolved-two-i64-parameters-v1 (get-in input [:resolved-parameter :kind])))
    (is (= :if (get-in input [:stage2-plan :functions 'main :instructions 0 :op])))
    (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))))

(deftest sh12-current-c6-consumes-i64-order-as-a-condition
  ;; c6c10-all-lexical-bindings-resolve-loop? consumes strict index ordering
  ;; as control. This closed predicate subtree does not execute its recursion.
  (let [directory "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        binding (parameter-compiled-binding-data
                  (str (.resolve @root (str directory "two_i64_order_condition_source.gravity"))))]
    (doseq [extension ["gravity" "qst"]]
      (let [path (str (.resolve @root (str directory "two_i64_order_condition_source." extension)))
            text (slurp path) products (parameter-source-products path text binding reader-path)
            input (:envelope products) raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
        (is (= :gravity/c6-c10-resolved-two-i64-parameters-v1 (get-in input [:resolved-parameter :kind])))
        (is (= :if (get-in input [:stage2-plan :functions 'main :instructions 0 :op])))
        (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
        (when (= :accepted (:status raw))
          (let [template (:artifact-template raw) nodes (:core-nodes template) ids (mapv :node-id nodes)
                fresh (parameter-source-products path text binding reader-path)
                replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
                verify #(invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                [(:envelope fresh) % (:digest-requests replay)])]
            (is (= input (:envelope fresh)))
            (is (= raw replay))
            (is (= :passed (:status (verify template))))
            (is (= [:parameter :parameter :local :local :integer-lt :truthy :literal :literal :if :function]
                   (mapv :source-operation nodes)))
            (is (= 10 (count nodes)))
            (is (= 9 (count (get-in template [:dependency-order-graph :edges]))))
            (is (= [true false] (mapv #(get-in nodes [% :attributes :value]) [6 7])))
            (is (= [:gravity/i64 :gravity/i64] (get-in nodes [9 :type :params])))
            (is (= :gravity/bool (get-in nodes [9 :type :return]) (get-in nodes [4 :type]) (get-in nodes [8 :type])))
            (is (= [(get ids 4)] (get-in nodes [5 :operands])))
            (is (= [(get ids 5) (get ids 6) (get ids 7)] (get-in nodes [8 :operands])))
            (is (= [(get ids 8)] (get-in nodes [9 :operands])))
            (is (= (get-in nodes [8 :source :origin-id])
                   (get-in nodes [5 :source :generated-origin 0 :input-origin-id])))
            (is (= :exact-integer-ordering (get-in nodes [4 :safety :basis])
                   (get-in nodes [4 :safety :proof :numeric-semantics])))
            (is (= :integer-exact (get-in nodes [4 :safety :proof :mathematical-mode])))
            (doseq [node nodes]
              (is (= #{} (:effects node) (:capabilities node)))
              (is (= :proven-safe (get-in node [:safety :outcome]))))
            (let [forms (get-in input [:front-end-products :form-tree])
                  form (first (filter #(= (:form-id %) (second (get-in input [:front-end-products :top-level-form-ids]))) forms))
                  envelope {:form-tree forms :resolved-parameter (:resolved-parameter input)}
                  plan (get-in input [:stage2-plan :functions 'main]) call (first (:body plan))
                  gate #(invoke parameter-c6-plan 'c6c10-i64-order-condition-body? [envelope % form])]
              ;; UNIT guard checks retain original DATA, not new source-auth
              ;; evidence or accepted products manufactured from substitutions.
              (is (true? (gate plan)))
              (doseq [changed [(assoc-in plan [:instructions 0 :test :function] '<=)
                               (assoc-in plan [:instructions 0 :test :args 0 :name] 'consumer-index)
                               (assoc-in plan [:instructions 0 :test :args 1] {:op :literal :value 4})
                               (assoc-in plan [:instructions 0 :then :value] false)
                               (assoc-in plan [:instructions 0 :then :value] nil)
                               (assoc-in plan [:instructions 0 :else :value] 0)
                               (assoc-in plan [:instructions 0 :else :op] :if)
                               (assoc plan :instructions []) (assoc plan :body [])
                               (assoc plan :body [call call])
                               (assoc plan :body [(list 'when (second call) true false)])
                               (assoc plan :body [(list 'if (list '<= 'binding-index 'consumer-index) true false)])]]
                (is (false? (gate changed))))
              (let [calls (atom 0) opaque (proxy [Object] []
                                          (hashCode [] (swap! calls inc) 0)
                                          (equals [_] (swap! calls inc) false))]
                (.hashCode opaque) (.equals opaque :probe)
                (is (= 2 @calls)) (reset! calls 0)
                (is (false? (gate (assoc-in plan [:instructions 0 :then :value] opaque))))
                (is (false? (gate (assoc plan :body [(list 'if (second call) opaque false)]))))
                (is (zero? @calls))))
            (doseq [candidate [(assoc-in template [:core-nodes 4 :operands] [(get ids 3) (get ids 2)])
                               (assoc-in template [:core-nodes 5 :operands] [(get ids 0)])
                               (assoc-in template [:core-nodes 5 :source :generated-origin 0 :input-origin-id]
                                         (get-in nodes [4 :source :origin-id]))
                               (assoc-in template [:core-nodes 6 :type] :gravity/integer)
                               (assoc-in template [:core-nodes 8 :operands] [(get ids 5) (get ids 7) (get ids 6)])
                               (assoc-in template [:core-nodes 9 :type :return] :gravity/i64)
                               (assoc-in template [:resolved-parameter :parameters 1 :parameter-index] 0)
                               (assoc-in template [:proof-records (get ids 4) :numeric-mode] :unchecked)]]
              (is (= :rejected (:status (verify candidate)))))
            (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
                  context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
                  core (bootstrap/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
                  mir (build-mir core) original (verify-mir core mir)
                  result (invoke c11-plan 'sh13-run-branch-join-mir-with-arguments [core mir [0 1]])]
              (is (= (:sealed-artifact-template sealed) (dissoc core :target-request-metadata :physical-provenance)))
              (is (= :passed (:status original)))
              (is (= :accepted (:status result)))
              (is (= true (get-in result [:execution :value])))
              (is (= [0 1] (:arguments result))))))))))

(deftest sh12-i64-order-condition-rejects-broader-source-bodies
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_i64_order_condition_source.gravity"))
        text (slurp path) binding (parameter-compiled-binding-data path)
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))]
    (doseq [[source seed-arity-error?]
            [[(str/replace text ") true false)" ") 0 false)") false]
             [(str/replace text ") true false)" ") true)") true]
             [(str/replace text ") true false))" ") true false) false)") false]
             [(str/replace text "binding-index" "<") false]]]
      (let [temporary (java.io.File/createTempFile "gravity-sh12-i64-condition-reject-" ".gravity")]
        (try
          (spit temporary source)
          (let [[raw error]
                (try
                  (let [input (:envelope (parameter-source-products (.getCanonicalPath temporary) source binding reader-path))]
                    [(invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input]) nil])
                  (catch clojure.lang.ExceptionInfo error [nil (ex-data error)]))]
            ;; Omitted else is rejected by the original seed before C6 mapping.
            (if seed-arity-error?
              (do (is (nil? raw))
                  (is (= ["L2-UNKNOWN-CORE-FORM" 3 2]
                         ((juxt :id :expected-arity :actual-arity) error))))
              (do (is (= :rejected (:status raw)) (pr-str error))
                  (is (= "C6-LOWERING-GAP" (get-in raw [:diagnostics 0 :rule]))))))
          (finally (.delete temporary)))))))

(deftest sh12-i64-condition-extension-preserves-the-original-direct-body
  ;; One affected G source; the old direct guard short-circuits the new case.
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_i64_order_source.gravity"))
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        input (:envelope (parameter-source-products path (slurp path) nil reader-path))
        raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input]) template (:artifact-template raw)]
    (is (= :accepted (:status raw)))
    (is (= [:parameter :parameter :local :local :integer-lt :function] (mapv :source-operation (:core-nodes template))))
    (is (= :gravity/bool (get-in template [:core-nodes 5 :type :return])))
    (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                   [input template (:digest-requests raw)]))))))

(deftest sh12-current-c6-produces-two-i64-order-parameters
  ;; c6c10-build-edge-list's strict-index-order subtree, not a mixed literal
  ;; comparison, runtime width witness, or execution of that complete helper.
  (let [directory "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        binding (parameter-compiled-binding-data (str (.resolve @root (str directory "two_i64_order_source.gravity"))))]
    (doseq [extension ["gravity" "qst"]]
      (let [path (str (.resolve @root (str directory "two_i64_order_source." extension))) text (slurp path)
            products (parameter-source-products path text binding reader-path) input (:envelope products)
            declaration (:resolved-parameter input) entries (:parameters declaration)
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
        (is (= :gravity/c6-c10-resolved-two-i64-parameters-v1 (:kind declaration)))
        (is (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id :projection-binding
                 :root-syntax-id :function-syntax-id :function-scope-id :type-binding :parameters} (set (keys declaration))))
        (is (= [0 1] (mapv :parameter-index entries)))
        (is (= '[dependency-index consumer-index] (mapv #(get-in % [:parameter-binding :name]) entries)
               (get-in input [:stage2-plan :functions 'main :params])))
        (is (= {:catalog :gravity/core :ordinal 21} (get-in declaration [:type-binding :semantic-span])))
        (is (= [:core :type 'gravity.core 'I64] ((juxt :binding-class :kind :namespace :name) (:type-binding declaration))))
        (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
        (when (= :accepted (:status raw))
          (let [template (:artifact-template raw) nodes (:core-nodes template) ids (mapv :node-id nodes)
                comparison (get nodes 4) function (get nodes 5)
                fresh (parameter-source-products path text binding reader-path)
                replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
                verify (fn [candidate] (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                              [(:envelope fresh) candidate (:digest-requests replay)]))]
            (is (= input (:envelope fresh)))
            (is (= raw replay))
            (is (= :passed (:status (verify template))))
            (is (= [:parameter :parameter :local :local :integer-lt :function] (mapv :source-operation nodes)))
            (is (= 6 (count nodes)))
            (is (= 5 (count (get-in template [:dependency-order-graph :edges]))))
            (is (= [[] [] [(get ids 0)] [(get ids 1)] [(get ids 2) (get ids 3)] [(get ids 4)]] (mapv :operands nodes)))
            (is (= [:gravity/i64 :gravity/i64 :gravity/i64 :gravity/i64 :gravity/bool] (mapv :type (take 5 nodes))))
            (is (= [:gravity/i64 :gravity/i64] (get-in function [:type :params])))
            (is (= :gravity/bool (get-in function [:type :return])))
            (is (= '[dependency-index consumer-index] (get-in function [:attributes :params])))
            (is (= (subvec ids 0 2) (mapv :binding-node-id (:lexical-binding-records template))))
            (is (= :exact-integer-ordering (get-in comparison [:attributes :numeric-semantics])
                   (get-in comparison [:safety :basis]) (get-in comparison [:safety :proof :numeric-semantics])))
            (is (= :proof-required (get-in comparison [:attributes :numeric-mode]) (get-in comparison [:safety :proof :numeric-mode])))
            (is (= :integer-exact (get-in comparison [:attributes :mathematical-mode])))
            (is (= :not-applicable (get-in comparison [:attributes :overflow])))
            (is (= [:gravity/i64 :gravity/i64] (get-in comparison [:attributes :operand-types])))
            (is (= :binary-integer-order-comparison-bool (get-in template [:type-facts (get ids 4) :producer-rule])))
            (doseq [index [0 1]]
              (let [node (get nodes index) id (get ids index) entry (get entries index)]
                (is (= [] (:operands node)))
                (is (not (contains? (:attributes node) :value)))
                (is (= index (get-in node [:attributes :parameter-index])))
                (is (= (get-in entry [:parameter-binding :binding-id]) (get-in node [:attributes :resolved-binding-id])))
                (is (= (get-in declaration [:type-binding :binding-id]) (get-in entry [:type-resolution :binding-id]) (get-in node [:attributes :type-binding-id])))
                (is (= :resolved-i64-parameter-binding (get-in node [:safety :basis]) (get-in node [:safety :proof :basis])))
                (is (= :resolved-i64-parameter-declaration (get-in template [:type-facts id :producer-rule])))))
            (doseq [node nodes]
              (is (= #{} (:effects node) (:capabilities node)))
              (is (= :proven-safe (get-in node [:safety :outcome]))))
            (let [forms (get-in input [:front-end-products :form-tree])
                  form (first (filter #(= (:form-id %) (second (get-in input [:front-end-products :top-level-form-ids]))) forms))
                  envelope {:form-tree forms :resolved-parameter declaration}
                  plan (get-in input [:stage2-plan :functions 'main])]
              ;; UNIT body guards on derived original DATA, not source-auth
              ;; or passed-upstream mutation claims. Positive gate is live.
              (is (true? (invoke parameter-c6-plan 'c6c10-i64-order-body? [envelope plan form])))
              (doseq [changed [(assoc-in plan [:instructions 0 :function] '<=)
                               (assoc-in plan [:instructions 0 :function] '=)
                               (assoc-in plan [:instructions 0 :args 0 :name] 'consumer-index)
                               (assoc-in plan [:instructions 0 :args 1 :name] 'dependency-index)
                               (assoc-in plan [:instructions 0 :args 1] {:op :literal :value 4})
                               (assoc plan :instructions []) (assoc plan :body [])]]
                (is (false? (invoke parameter-c6-plan 'c6c10-i64-order-body? [envelope changed form])))))
            (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
                  context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
                  core (bootstrap/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
                  mir (build-mir core) verification (verify-mir core mir)
                  run (invoke c11-plan 'sh13-run-branch-join-mir-with-arguments [core mir [0 1]])]
              (is (= (:sealed-artifact-template sealed) (dissoc core :target-request-metadata :physical-provenance)))
              (is (= :passed (:status verification)))
              (is (= :accepted (:status run)))
              (is (= true (get-in run [:execution :value])))
              (is (= [0 1] (:arguments run))))
            (doseq [malformed [(assoc-in input [:resolved-parameter :parameters] (vec (reverse entries)))
                               (assoc-in input [:resolved-parameter :parameters 1] (first entries))
                               (assoc-in input [:resolved-parameter :type-binding :name] 'Bool)
                               (assoc-in input [:resolved-parameter :type-binding :semantic-span :ordinal] 12)
                               (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :scope-id] (:root-syntax-id declaration))
                               (assoc-in input [:resolved-parameter :parameters 1 :parameter-index] 0)
                               (assoc-in input [:resolved-parameter :parameters 1 :coordinates 3] (get-in entries [0 :coordinates 1]))
                               (assoc-in input [:stage2-plan :functions 'main :params] '[consumer-index dependency-index])]]
              (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
            (doseq [index [0 1] field [[:parameter-binding :binding-id] [:type-resolution :binding-id] [:coordinates 1 :projected-syntax-id]]
                    value [nil "" 17]]
              (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                              [(assoc-in input (into [:resolved-parameter :parameters index] field) value)])))))
            (doseq [candidate [(assoc-in template [:core-nodes 1 :type] :gravity/integer)
                               (assoc-in template [:core-nodes 3 :operands] [(get ids 0)])
                               (assoc-in template [:core-nodes 4 :attributes :operand-types] [:gravity/integer :gravity/integer])
                               (assoc-in template [:core-nodes 4 :attributes :primitive] '<=)
                               (assoc-in template [:type-facts (get ids 1) :producer-rule] :resolved-bool-parameter-declaration)
                               (assoc-in template [:core-nodes 5 :type :params] [:gravity/i64])
                               (update template :lexical-binding-records #(vec (reverse %)))]]
              (is (= :rejected (:status (verify candidate)))))
            (let [changed (assoc-in input [:resolved-parameter :sh06-artifact-id] (:projection-binding declaration))
                  substituted (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [changed])]
              (is (not= (:sh06-artifact-id declaration) (:projection-binding declaration)))
              (is (= :accepted (:status substituted)))
              (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                             [changed (:artifact-template substituted) (:digest-requests substituted)]))))
              (is (= :rejected (:status (verify (:artifact-template substituted))))))))))))

(deftest sh12-two-i64-order-rejects-mixed-literal-and-shadowed-operator
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_i64_order_source.gravity"))
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        text (slurp path) binding (parameter-compiled-binding-data path)]
    (doseq [[source expected]
            [[(str/replace text "consumer-index :- I64" "consumer-index :- Bool") "C6-VERIFY"]
             [(str/replace text "(< dependency-index consumer-index)" "(< dependency-index 4)") "C6-LOWERING-GAP"]
             [(str/replace text "dependency-index" "<") "C6-LOWERING-GAP"]]]
      (let [temporary (java.io.File/createTempFile "gravity-sh12-two-i64-order-reject-" ".gravity")]
        (try
          (spit temporary source)
          (let [observed (try
                           (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                   [(:envelope (parameter-source-products (.getCanonicalPath temporary) source binding reader-path))])
                           (catch clojure.lang.ExceptionInfo error {:failure (ex-data error)}))]
            (is (= expected (or (get-in observed [:failure :id]) (get-in observed [:diagnostics 0 :rule]))) (pr-str observed)))
          (finally (.delete temporary)))))))

(deftest sh12-two-i64-order-preserves-single-i64-identity
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/i64_parameter_source.gravity"))
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        input (:envelope (parameter-source-products path (slurp path) nil reader-path))
        raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
        template (:artifact-template raw) nodes (:core-nodes template)]
    (is (= :gravity/c6-c10-resolved-i64-parameter-v1 (get-in input [:resolved-parameter :kind])))
    (is (= :accepted (:status raw)))
    (is (= [:parameter :local :function] (mapv :source-operation nodes)))
    (is (= [:gravity/i64] (get-in nodes [2 :type :params])))
    (is (= [:gravity/i64 :gravity/i64] (mapv :type (take 2 nodes))))
    (is (= :gravity/i64 (get-in nodes [2 :type :return])))
    (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template [input template (:digest-requests raw)]))))
    (let [calls (atom {:hash 0 :equals 0})
          opaque (proxy [Object] []
                   (hashCode [] (swap! calls update :hash inc) 424242)
                   (equals [_] (swap! calls update :equals inc) false))]
      (.hashCode opaque)
      (.equals opaque :probe)
      (is (= {:hash 1 :equals 1} @calls))
      (reset! calls {:hash 0 :equals 0})
      (let [rejected (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                             [(assoc-in input [:resolved-parameter :kind] opaque)])]
        (is (= :rejected (:status rejected)))
        (is (= "C6-VERIFY" (get-in rejected [:diagnostics 0 :rule])))
        (is (= {:hash 0 :equals 0} @calls))))))

(deftest sh12-current-c6-produces-resolved-i64-identity
  ;; The declared signed-64 type is not a runtime argument/range witness.
  ;; These are real C2/C5 products and current C6 reconstruction, not admission.
  (let [directory "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
        binding (parameter-compiled-binding-data (str (.resolve @root (str directory "i64_parameter_source.gravity"))))]
    (doseq [extension ["gravity" "qst"]]
      (let [path (str (.resolve @root (str directory "i64_parameter_source." extension)))
            text (slurp path) products (parameter-source-products path text binding) input (:envelope products)
            declaration (:resolved-parameter input)
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
        (is (= :gravity/c6-c10-resolved-i64-parameter-v1 (:kind declaration)))
        (is (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id :projection-binding
                 :root-syntax-id :function-syntax-id :function-scope-id :parameter-index :coordinates
                 :parameter-binding :type-binding :type-resolution} (set (keys declaration))))
        (is (= [:core :type 'gravity.core 'I64]
               ((juxt :binding-class :kind :namespace :name) (:type-binding declaration))))
        (is (= {:catalog :gravity/core :ordinal 21} (get-in declaration [:type-binding :semantic-span])))
        (is (= 0 (:parameter-index declaration)))
        (is (= '[child-count] (get-in input [:stage2-plan :functions 'main :params])))
        (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
        (when (= :accepted (:status raw))
          (let [template (:artifact-template raw) nodes (:core-nodes template)
                [binder local function] nodes id (:node-id binder) local-id (:node-id local) function-id (:node-id function)
                fresh (parameter-source-products path text binding)
                replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
                verify (fn [candidate] (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                              [(:envelope fresh) candidate (:digest-requests replay)]))]
            (is (= input (:envelope fresh)))
            (is (= raw replay))
            (is (= :passed (:status (verify template))))
            (is (= [:parameter :local :function] (mapv :source-operation nodes)))
            (is (= 2 (count (get-in template [:dependency-order-graph :edges]))))
            (is (= [] (:operands binder)))
            (is (= [id] (:operands local)))
            (is (= [local-id] (:operands function)))
            (is (= [:gravity/i64 :gravity/i64] (mapv :type [binder local])))
            (is (= [:gravity/i64] (get-in function [:type :params])))
            (is (= :gravity/i64 (get-in function [:type :return])))
            (is (= '[child-count] (get-in function [:attributes :params])))
            (is (= id (get-in local [:attributes :resolved-binding])))
            (is (= [id] (mapv :binding-node-id (:lexical-binding-records template))))
            (is (= (get-in declaration [:parameter-binding :binding-id]) (get-in binder [:attributes :resolved-binding-id])))
            (is (= (get-in declaration [:type-binding :binding-id]) (get-in declaration [:type-resolution :binding-id])
                   (get-in binder [:attributes :type-binding-id])))
            (is (= (get-in declaration [:coordinates 1 :c2-form-id]) (get-in binder [:attributes :source-form-id])))
            (is (not (contains? (:attributes binder) :value)))
            (is (= :resolved-i64-parameter-binding (get-in binder [:safety :basis]) (get-in binder [:safety :proof :basis])))
            (is (= :gravity/i64 (get-in binder [:safety :proof :type])))
            (is (= :resolved-i64-parameter-declaration (get-in template [:type-facts id :producer-rule])))
            (is (= :gravity/i64 (get-in template [:type-facts id :type]) (get-in template [:type-facts local-id :type])))
            (is (= [(get-in template [:type-facts id :type-id])]
                   (get-in template [:typed-core :functions function-id :params])))
            (is (= (get-in template [:type-facts local-id :type-id]) (get-in template [:typed-core :functions function-id :return])))
            (doseq [node nodes]
              (is (= #{} (:effects node) (:capabilities node)))
              (is (= :proven-safe (get-in node [:safety :outcome])))
              (is (contains? (:source-origin-table template) (get-in node [:source :origin-id]))))
            (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
                  context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
                  core (bootstrap/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
                  mir (build-mir core) verification (verify-mir core mir)
                  run (invoke c11-plan 'sh13-run-branch-join-mir-with-arguments [core mir [0]])]
              (is (= (:sealed-artifact-template sealed) (dissoc core :target-request-metadata :physical-provenance)))
              (is (= :passed (:status verification)))
              (is (= :accepted (:status run)))
              (is (= 0 (get-in run [:execution :value])))
              (is (= [0] (:arguments run))))
            (let [calls (atom {:hash 0 :equals 0})
                  opaque (proxy [Object] []
                           (hashCode [] (swap! calls update :hash inc) 424242)
                           (equals [_] (swap! calls update :equals inc) false))]
              (.hashCode opaque)
              (.equals opaque :probe)
              (is (= {:hash 1 :equals 1} @calls))
              (reset! calls {:hash 0 :equals 0})
              (let [rejected (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                     [(assoc-in input [:resolved-parameter :kind] opaque)])]
                (is (= :rejected (:status rejected)))
                (is (= "C6-VERIFY" (get-in rejected [:diagnostics 0 :rule])))
                (is (= {:hash 0 :equals 0} @calls))))
            (doseq [malformed [(assoc-in input [:resolved-parameter :kind] :gravity/c6-c10-resolved-bool-parameter-v1)
                               (assoc-in input [:resolved-parameter :parameter-index] 1)
                               (assoc-in input [:resolved-parameter :type-binding :name] 'Bool)
                               (assoc-in input [:resolved-parameter :type-binding :semantic-span :ordinal] 12)
                               (assoc-in input [:resolved-parameter :parameter-binding :scope-id] (:root-syntax-id declaration))
                               (assoc-in input [:resolved-parameter :coordinates 3] (get-in declaration [:coordinates 1]))
                               (assoc-in input [:stage2-plan :functions 'main :params] '[other])]]
              (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
            (doseq [field [[:parameter-binding :binding-id] [:type-binding :binding-id]
                           [:type-resolution :binding-id] [:coordinates 1 :projected-syntax-id]]
                    value [nil "" 17]]
              (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                              [(assoc-in input (into [:resolved-parameter] field) value)])))))
            (doseq [candidate [(assoc-in template [:core-nodes 0 :type] :gravity/integer)
                               (assoc-in template [:core-nodes 1 :type] :gravity/bool)
                               (assoc-in template [:core-nodes 1 :operands] [])
                               (assoc-in template [:core-nodes 2 :type :return] :gravity/integer)
                               (assoc-in template [:core-nodes 0 :safety :basis] :resolved-bool-parameter-binding)
                               (assoc-in template [:type-facts id :producer-rule] :resolved-bool-parameter-declaration)
                               (assoc template :lexical-binding-records [])]]
              (is (= :rejected (:status (verify candidate)))))
            (let [replacement (:projection-binding declaration)
                  changed (assoc-in input [:resolved-parameter :sh06-artifact-id] replacement)
                  substituted (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [changed])]
              (is (not= replacement (:sh06-artifact-id declaration)))
              (is (= :accepted (:status substituted)))
              (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                             [changed (:artifact-template substituted) (:digest-requests substituted)]))))
              (is (= :rejected (:status (verify (:artifact-template substituted))))))))))))

(deftest sh12-i64-parameter-identity-rejects-unresolved-and-broader-bodies
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/i64_parameter_source.gravity"))
        text (slurp path) binding (parameter-compiled-binding-data path)]
    (doseq [[source expected]
            [[(str/replace text " :- I64" " :- String") "C6-VERIFY"]
             [(str/replace text " :- I64" "") "C6-VERIFY"]
             [(str/replace text "] child-count)" "] missing)") "C5-UNRESOLVED"]
             [(str/replace text "] child-count)" "] (if true child-count child-count))") "C6-LOWERING-GAP"]
             [(str/replace text "] child-count)" "] child-count child-count)") "C6-LOWERING-GAP"]]]
      (let [temporary (java.io.File/createTempFile "gravity-sh12-i64-identity-reject-" ".gravity")]
        (try
          (spit temporary source)
          (let [observed (try
                           (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                   [(:envelope (parameter-source-products (.getAbsolutePath temporary) source binding))])
                           (catch clojure.lang.ExceptionInfo error {:failure (ex-data error)}))]
            (is (= expected (or (get-in observed [:failure :id]) (get-in observed [:diagnostics 0 :rule])))
                (pr-str observed)))
          (finally (.delete temporary)))))))

(deftest sh12-current-c6-produces-three-independently-resolved-bool-parameters
  ;; Three independent c6c10-map-if premises, with real source/C2/C5 joins.
  ;; Current C11 also reconstructs this family; SH13 owns tuple execution.
  ;; This is not execution of the complete compiler helper.
 (let [compiled-binding (parameter-compiled-binding-data
                         (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/three_bool_parameter_source.gravity")))]
  (doseq [extension ["gravity" "qst"]]
    (let [path (str (.resolve @root (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/three_bool_parameter_source." extension)))
          text (slurp path) products (parameter-source-products path text compiled-binding) input (:envelope products)
          declaration (:resolved-parameter input) entries (:parameters declaration)
          raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
      (is (= :gravity/c6-c10-resolved-three-bool-parameters-v1 (:kind declaration)))
      (is (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id :projection-binding
               :root-syntax-id :function-syntax-id :function-scope-id :type-binding :parameters}
             (set (keys declaration))))
      (is (= [0 1 2] (mapv :parameter-index entries)))
      (is (= '[implicit? literal-plan? nil-plan?] (mapv #(get-in % [:parameter-binding :name]) entries)
             (get-in input [:stage2-plan :functions 'main :params])))
      (is (= 3 (get-in input [:stage2-plan :functions 'main :arity])))
      (is (= :gravity/stage2-compiler-artifact-plan (get-in input [:stage2-plan :kind])))
      (is (true? (get-in input [:stage2-plan :compiler-artifact-plan?])))
      (is (= :clojure-stage0-seed (get-in input [:stage2-plan :compiler :compiled-by])))
      (is (= [:core :type 'gravity.core 'Bool]
             ((juxt :binding-class :kind :namespace :name) (:type-binding declaration))))
      (is (= 3 (count (set (map #(get-in % [:parameter-binding :binding-id]) entries)))))
      (is (= 9 (count (set (mapcat #(map :projected-syntax-id (rest (:coordinates %))) entries)))))
      (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
      (when (= :accepted (:status raw))
        (let [template (:artifact-template raw) nodes (:core-nodes template) binders (subvec nodes 0 3)
              ids (mapv :node-id binders) function (last nodes) function-id (:node-id function)
              fresh (parameter-source-products path text compiled-binding) replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
              verify (fn [candidate] (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                            [(:envelope fresh) candidate (:digest-requests replay)]))]
          (is (= input (:envelope fresh)))
          (is (= raw replay))
          (is (= :passed (:status (verify template))))
          (is (= 13 (count nodes)))
          (is (= 12 (count (get-in template [:dependency-order-graph :edges]))))
          (is (= [:parameter :parameter :parameter :local :truthy :local :truthy :local :literal :if :literal :if :function]
                 (mapv :source-operation nodes)))
          (is (= '[implicit? literal-plan? nil-plan?] (get-in function [:attributes :params])))
          (is (= [:gravity/bool :gravity/bool :gravity/bool] (get-in function [:type :params])))
          (is (= :gravity/bool (get-in function [:type :return])))
          (is (= (mapv #(get-in template [:type-facts % :type-id]) ids)
                 (get-in template [:typed-core :functions function-id :params])))
          (is (= ids (mapv :binding-node-id (:lexical-binding-records template))))
          (doseq [[binder-index local-index] [[0 3] [1 5] [2 7]]]
            (is (= [(get ids binder-index)] (get-in nodes [local-index :operands]))))
          (doseq [[index binder entry] (map vector [0 1 2] binders entries)]
            (let [id (:node-id binder)]
              (is (= [] (:operands binder)))
              (is (= :gravity/bool (:type binder)))
              (is (= index (get-in binder [:attributes :parameter-index])))
              (is (= [:functions 'main :parameters index :binder] (:path binder)))
              (is (= (get-in entry [:parameter-binding :binding-id]) (get-in binder [:attributes :resolved-binding-id])))
              (is (= (get-in declaration [:type-binding :binding-id]) (get-in entry [:type-resolution :binding-id])
                     (get-in binder [:attributes :type-binding-id])))
              (is (= (get-in entry [:coordinates 1 :c2-form-id]) (get-in binder [:attributes :source-form-id])))
              (is (not (contains? (:attributes binder) :value)))
              (is (= :resolved-bool-parameter-declaration (get-in template [:type-facts id :producer-rule])))
              (is (= :proven-safe (get-in binder [:safety :outcome])))))
          (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
                context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
                core (bootstrap/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
                mir (build-mir core)
                verification (verify-mir core mir)]
            (is (= (:sealed-artifact-template sealed) (dissoc core :target-request-metadata :physical-provenance)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification))))
          (doseq [malformed [(assoc-in input [:resolved-parameter :parameters] [])
                             (assoc-in input [:resolved-parameter :kind] :gravity/unknown-parameter-declaration)
                             (assoc-in input [:resolved-parameter :parameters] (vec (reverse entries)))
                             (assoc-in input [:resolved-parameter :parameters 1] (first entries))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-index] 0)
                             (assoc-in input [:resolved-parameter :parameters 1 :extra] true)
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates] [])
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates 2 :role] :parameter-name)
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates 1] (get-in entries [0 :coordinates 1]))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :binding-id] (get-in entries [0 :parameter-binding :binding-id]))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :name] 'implicit?)
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :scope-id] (:root-syntax-id declaration))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :semantic-span :binding-ordinal] 0)
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :profile-set] [:meta])
                             (assoc-in input [:resolved-parameter :parameters 1 :type-resolution :reference-syntax-id] (get-in entries [0 :type-resolution :reference-syntax-id]))
                             (assoc-in input [:resolved-parameter :type-binding :name] 'I64)
                             (assoc-in input [:stage2-plan :functions 'main :params] '[nil-plan? literal-plan? implicit?])
                             (assoc-in input [:stage2-plan :functions 'main :arity] 1)]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
          (doseq [index [0 1 2]
                  field [[:parameter-binding :binding-id] [:parameter-binding :definition-syntax-id]
                        [:type-resolution :binding-id] [:coordinates 1 :projected-syntax-id]]
                  value [nil "" 17]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                            [(assoc-in input (into [:resolved-parameter :parameters index] field) value)])))))
          (doseq [index [0 1 2]
                  malformed [(assoc-in input [:resolved-parameter :parameters index :parameter-index] 3)
                             (assoc-in input [:resolved-parameter :parameters index :parameter-binding :scope-id] (:root-syntax-id declaration))
                             (assoc-in input [:resolved-parameter :parameters index :parameter-binding :semantic-span :binding-ordinal] 3)
                             (assoc-in input [:resolved-parameter :parameters index :coordinates 3] (get-in entries [(mod (inc index) 3) :coordinates 3]))]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
          (let [replacement (:projection-binding declaration)
                changed (assoc-in input [:resolved-parameter :sh06-artifact-id] replacement)
                forged (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [changed])]
            (is (not= replacement (:sh06-artifact-id declaration)))
            (is (= :accepted (:status forged)))
            (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                           [changed (:artifact-template forged) (:digest-requests forged)]))))
            (is (= :rejected (:status (verify (:artifact-template forged))))))
          (doseq [candidate [(assoc-in template [:core-nodes 1 :attributes :parameter-index] 0)
                             (assoc-in template [:core-nodes 1 :attributes :resolved-binding-id] (get-in entries [0 :parameter-binding :binding-id]))
                             (assoc-in template [:core-nodes 7 :operands] [(first ids)])
                             (assoc-in template [:typed-core :functions function-id :params] [(get-in template [:type-facts (first ids) :type-id])])
                             (update template :lexical-binding-records #(vec (reverse %)))]]
            (is (= :rejected (:status (verify candidate)))))))))))

(deftest sh12-three-bool-parameter-ingress-and-unselected-types-fail-closed
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/three_bool_parameter_source.gravity"))
        text (slurp path) binding (parameter-compiled-binding-data path)]
    (doseq [[label source expected]
            [[:wrong-type (str/replace text "nil-plan? :- Bool" "nil-plan? :- I64") "C6-VERIFY"]
             [:missing-type (str/replace text "nil-plan? :- Bool" "nil-plan?") "C6-VERIFY"]
             [:unresolved (str/replace text "literal-plan? nil-plan? false" "literal-plan? missing false") "C5-UNRESOLVED"]]]
      (let [temporary (java.io.File/createTempFile "gravity-sh12-three-bool-reject-" ".gravity")]
        (try
          (spit temporary source)
          (let [observed (try (parameter-source-products (.getAbsolutePath temporary) source binding)
                              (catch clojure.lang.ExceptionInfo error {:failure (ex-data error)}))]
            (is (= expected (get-in observed [:failure :id])) (pr-str [label (:failure observed)])))
          (finally (.delete temporary)))))
    ;; A selected argument cannot hide the original Bool operand in an
    ;; integer-only equality inside the nested branch. No execution occurs.
    (let [source (str/replace text "literal-plan? nil-plan? false" "literal-plan? (= nil-plan? 0) false")
          temporary (java.io.File/createTempFile "gravity-sh12-three-bool-arm-type-" ".gravity")]
      (try
        (spit temporary source)
        (let [input (:envelope (parameter-source-products (.getAbsolutePath temporary) source binding))
              raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
          (is (= :rejected (:status raw)))
          (is (= "C7-TYPE-MISMATCH" (get-in raw [:diagnostics 0 :rule]))))
        (finally (.delete temporary))))))

(deftest sh12-three-parameter-producer-preserves-one-and-two-parameter-products
  ;; G-only affected regressions, not a replay of the old paired proof. The
  ;; original current C6 verifier independently reconstructs each same input.
  (let [directory "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
        binding (parameter-compiled-binding-data (str (.resolve @root (str directory "bool_parameter_source.gravity"))))]
    (doseq [[stem kind names count-nodes]
            [["bool_parameter_source" :gravity/c6-c10-resolved-bool-parameter-v1 '[flag] 7]
             ["two_bool_parameter_source" :gravity/c6-c10-resolved-two-bool-parameters-v1 '[left right] 12]]]
      (let [path (str (.resolve @root (str directory stem ".gravity")))
            input (:envelope (parameter-source-products path (slurp path) binding))
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
            template (:artifact-template raw) nodes (:core-nodes template) function (last nodes)
            arity (count names) binders (vec (take arity nodes)) ids (mapv :node-id binders)]
        (is (= kind (get-in input [:resolved-parameter :kind])))
        (is (= :accepted (:status raw)))
        (is (= count-nodes (count nodes)))
        (is (= names (get-in function [:attributes :params])))
        (is (= (vec (repeat arity :gravity/bool)) (get-in function [:type :params])))
        (is (= (vec (range arity)) (mapv #(get-in % [:attributes :parameter-index]) binders)))
        (is (every? #(= [] (:operands %)) binders))
        (is (= ids (mapv :binding-node-id (:lexical-binding-records template))))
        (is (= (mapv #(get-in template [:type-facts % :type-id]) ids)
               (get-in template [:typed-core :functions (:node-id function) :params])))
        (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                       [input template (:digest-requests raw)]))))))))

(deftest sh12-current-c6-produces-two-independently-resolved-bool-parameters
  ;; Real source/C2/C5 products, not a hand-built type witness or MIR. This
  ;; producer checks retain their original source lineage; the closed C11
  ;; consumer now also reconstructs this family. SH13 owns tuple execution.
 (let [compiled-binding (parameter-compiled-binding-data
                         (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_bool_parameter_source.gravity")))]
  (doseq [extension ["gravity" "qst"]]
    (let [path (str (.resolve @root (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_bool_parameter_source." extension)))
          text (slurp path) products (parameter-source-products path text compiled-binding) input (:envelope products)
          declaration (:resolved-parameter input) entries (:parameters declaration)
          raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
      (is (= :gravity/c6-c10-resolved-two-bool-parameters-v1 (:kind declaration)))
      (is (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id :projection-binding
               :root-syntax-id :function-syntax-id :function-scope-id :type-binding :parameters}
             (set (keys declaration))))
      (is (= [0 1] (mapv :parameter-index entries)))
      (is (= '[left right] (mapv #(get-in % [:parameter-binding :name]) entries)
             (get-in input [:stage2-plan :functions 'main :params])))
      (is (= 2 (get-in input [:stage2-plan :functions 'main :arity])))
      (is (= :gravity/stage2-compiler-artifact-plan (get-in input [:stage2-plan :kind])))
      (is (true? (get-in input [:stage2-plan :compiler-artifact-plan?])))
      (is (= :clojure-stage0-seed (get-in input [:stage2-plan :compiler :compiled-by])))
      (is (= [:core :type 'gravity.core 'Bool]
             ((juxt :binding-class :kind :namespace :name) (:type-binding declaration))))
      (is (= 2 (count (set (map #(get-in % [:parameter-binding :binding-id]) entries)))))
      (is (= 6 (count (set (mapcat #(map :projected-syntax-id (rest (:coordinates %))) entries)))))
      (is (= :accepted (:status raw)) (pr-str (:diagnostics raw)))
      (when (= :accepted (:status raw))
        (let [template (:artifact-template raw) nodes (:core-nodes template) binders (subvec nodes 0 2)
              ids (mapv :node-id binders) function (last nodes) function-id (:node-id function)
              fresh (parameter-source-products path text compiled-binding) replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
              verify (fn [candidate] (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                            [(:envelope fresh) candidate (:digest-requests replay)]))]
          (is (= input (:envelope fresh)))
          (is (= raw replay))
          (is (= :passed (:status (verify template))))
          (is (= 12 (count nodes)))
          (is (= [:parameter :parameter :local :truthy :local :literal :if :truthy :literal :literal :if :function]
                 (mapv :source-operation nodes)))
          (is (= '[left right] (get-in function [:attributes :params])))
          (is (= [:gravity/bool :gravity/bool] (get-in function [:type :params])))
          (is (= :gravity/integer (get-in function [:type :return])))
          (is (= (mapv #(get-in template [:type-facts % :type-id]) ids)
                 (get-in template [:typed-core :functions function-id :params])))
          (is (= ids (mapv :binding-node-id (:lexical-binding-records template))))
          (is (= [(first ids)] (get-in nodes [2 :operands])))
          (is (= [(second ids)] (get-in nodes [4 :operands])))
          (doseq [[index binder entry] (map vector [0 1] binders entries)]
            (let [id (:node-id binder)]
              (is (= [] (:operands binder)))
              (is (= :gravity/bool (:type binder)))
              (is (= index (get-in binder [:attributes :parameter-index])))
              (is (= [:functions 'main :parameters index :binder] (:path binder)))
              (is (= (get-in entry [:parameter-binding :binding-id]) (get-in binder [:attributes :resolved-binding-id])))
              (is (= (get-in declaration [:type-binding :binding-id]) (get-in entry [:type-resolution :binding-id])
                     (get-in binder [:attributes :type-binding-id])))
              (is (= (get-in entry [:coordinates 1 :c2-form-id]) (get-in binder [:attributes :source-form-id])))
              (is (not (contains? (:attributes binder) :value)))
              (is (= :resolved-bool-parameter-declaration (get-in template [:type-facts id :producer-rule])))
              (is (= :proven-safe (get-in binder [:safety :outcome])))))
          (let [sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
                context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
                core (bootstrap/p15-s23-c6c10-public-artifact context (:compiled-binding-data products) (:ingress products) sealed)
                mir (build-mir core)
                verification (verify-mir core mir)]
            (is (= (:sealed-artifact-template sealed) (dissoc core :target-request-metadata :physical-provenance)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification))))
          (doseq [malformed [(assoc-in input [:resolved-parameter :parameters] [])
                             (assoc-in input [:resolved-parameter :parameters] (vec (reverse entries)))
                             (assoc-in input [:resolved-parameter :parameters 1] (first entries))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-index] 0)
                             (assoc-in input [:resolved-parameter :parameters 1 :extra] true)
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates] [])
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates 2 :role] :parameter-name)
                             (assoc-in input [:resolved-parameter :parameters 1 :coordinates 1] (get-in entries [0 :coordinates 1]))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :binding-id] (get-in entries [0 :parameter-binding :binding-id]))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :name] 'left)
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :scope-id] (:root-syntax-id declaration))
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :semantic-span :binding-ordinal] 0)
                             (assoc-in input [:resolved-parameter :parameters 1 :parameter-binding :profile-set] [:meta])
                             (assoc-in input [:resolved-parameter :parameters 1 :type-resolution :reference-syntax-id] (get-in entries [0 :type-resolution :reference-syntax-id]))
                             (assoc-in input [:resolved-parameter :type-binding :name] 'I64)
                             (assoc-in input [:stage2-plan :functions 'main :params] '[right left])
                             (assoc-in input [:stage2-plan :functions 'main :arity] 1)]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
          (doseq [field [[:parameter-binding :binding-id] [:parameter-binding :definition-syntax-id]
                        [:type-resolution :binding-id] [:coordinates 1 :projected-syntax-id]]
                  value [nil "" 17]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                            [(assoc-in input (into [:resolved-parameter :parameters 1] field) value)])))))
          (let [replacement (:projection-binding declaration)
                changed (assoc-in input [:resolved-parameter :sh06-artifact-id] replacement)
                forged (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [changed])]
            (is (not= replacement (:sh06-artifact-id declaration)))
            (is (= :accepted (:status forged)))
            (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                           [changed (:artifact-template forged) (:digest-requests forged)]))))
            (is (= :rejected (:status (verify (:artifact-template forged))))))
          (doseq [candidate [(assoc-in template [:core-nodes 1 :attributes :parameter-index] 0)
                             (assoc-in template [:core-nodes 1 :attributes :resolved-binding-id] (get-in entries [0 :parameter-binding :binding-id]))
                             (assoc-in template [:core-nodes 4 :operands] [(first ids)])
                             (assoc-in template [:typed-core :functions function-id :params] [(get-in template [:type-facts (first ids) :type-id])])
                             (update template :lexical-binding-records #(vec (reverse %)))]]
            (is (= :rejected (:status (verify candidate)))))))))))

(deftest sh12-two-bool-parameter-ingress-rejects-wrong-annotation-and-unresolved-local
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_bool_parameter_source.gravity"))
        text (slurp path)]
    (doseq [[label source expected] [[:wrong-bool (str/replace text "right :- Bool" "right :- I64") "C6-VERIFY"]
                                    [:unresolved (str/replace text "left right false" "left missing false") "C5-UNRESOLVED"]]]
      (let [temporary (java.io.File/createTempFile "gravity-sh12-two-bool-reject-" ".gravity")]
        (try
          (spit temporary source)
          (let [observed (try (parameter-input (.getAbsolutePath temporary) source)
                              (catch clojure.lang.ExceptionInfo error {:failure (ex-data error)}))]
            (is (= expected (get-in observed [:failure :id])) (pr-str [label (:failure observed)])))
          (finally (.delete temporary)))))))

(deftest sh12-two-bool-parameter-producer-checks-both-source-arms
  ;; Actual source/C2/C5 input reaches C6. No argument selection can hide a
  ;; Bool operand in the else arm's integer-only equality operation.
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/two_bool_parameter_source.gravity"))
        source (str/replace (slurp path) ") 11 22)" ") 11 (= right 0))")
        temporary (java.io.File/createTempFile "gravity-sh12-two-bool-arm-type-" ".gravity")]
    (try
      (spit temporary source)
      (let [input (parameter-input (.getAbsolutePath temporary) source)
            raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
        (is (= :rejected (:status raw)))
        (is (= "C7-TYPE-MISMATCH" (get-in raw [:diagnostics 0 :rule]))))
      (finally (.delete temporary)))))

(deftest sh12-two-parameter-producer-preserves-singular-kind-and-arity-priority
  ;; One affected original G source, not an unchanged paired-source proof.
  (let [path (str (.resolve @root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        reader-path (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        input (:envelope (parameter-source-products path (slurp path) nil reader-path)) declaration (:resolved-parameter input)
        raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
        template (:artifact-template raw) function (last (:core-nodes template))
        bad (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                    [(assoc-in input [:stage2-plan :functions 'main :arity] 2)])]
    (is (= :gravity/c6-c10-resolved-bool-parameter-v1 (:kind declaration)))
    (is (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id :projection-binding
             :root-syntax-id :function-syntax-id :function-scope-id :parameter-index :coordinates
             :parameter-binding :type-binding :type-resolution} (set (keys declaration))))
    (is (= :accepted (:status raw)))
    (is (= 7 (count (:core-nodes template))))
    (is (= '[flag] (get-in function [:attributes :params])))
    (is (= [:gravity/bool] (get-in function [:type :params])))
    (is (= 1 (count (get-in template [:typed-core :functions (:node-id function) :params]))))
    (is (= :passed (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                   [input template (:digest-requests raw)]))))
    (is (= :rejected (:status bad)))
    (is (= ["C6-CORE-SHAPE" :single-main-stage2-plan]
           [(get-in bad [:diagnostics 0 :rule]) (get-in bad [:diagnostics 0 :facts :missing-fact])]))))

(deftest sh12-current-c6-produces-a-resolved-bool-parameter-without-a-mir-instruction
  (doseq [extension [".gravity" ".qst"]]
    (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source" extension)
          source-path (str (.resolve @root relative)) text (slurp source-path)
          input (parameter-input source-path text)
          evidence (:resolved-parameter input)
          result (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])]
      (is (= :gravity/stage2-compiler-artifact-plan (get-in input [:stage2-plan :kind])))
      (is (= :clojure-stage0-seed (get-in input [:stage2-plan :compiler :compiled-by])))
      (is (= [:core :type 'gravity.core 'Bool]
             ((juxt :binding-class :kind :namespace :name) (:type-binding evidence))))
      (is (= [:lexical :local 'flag]
             ((juxt :binding-class :kind :name) (:parameter-binding evidence))))
      (is (= [:parameter-vector :parameter-name :annotation-marker :annotation-type]
             (mapv :role (:coordinates evidence))))
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [template (:artifact-template result) nodes (:core-nodes template)
              binder (first (filter #(= :parameter (:source-operation %)) nodes))
              local (first (filter #(= :local (:source-operation %)) nodes))
              function (last nodes)
              fresh (parameter-input source-path text)
              replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [fresh])
              verify (fn [candidate]
                       (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                               [fresh candidate (:digest-requests replay)]))]
          (is (= input fresh))
          (is (= result replay))
          (is (= :passed (:status (verify template))))
          (is (= 7 (count nodes)))
          (is (= :binding (:kind binder)))
          (is (= [] (:operands binder)))
          (is (not (contains? (:attributes binder) :value)))
          (is (= :gravity/bool (:type binder)))
          (is (= [(:node-id binder)] (:operands local)))
          (is (= ['flag] (get-in function [:attributes :params])))
          (is (= [:gravity/bool] (get-in function [:type :params])))
          (is (= :gravity/bool (get-in function [:type :return])))
          (is (= [(get-in template [:type-facts (:node-id binder) :type-id])]
                 (get-in template [:typed-core :functions (:node-id function) :params])))
          (is (= :resolved-bool-parameter-declaration
                 (get-in template [:type-facts (:node-id binder) :producer-rule])))
          (is (= :proven-safe (get-in binder [:safety :outcome])))
          (is (true? (get-in template [:dependency-order-graph :all-lexical-bindings-resolve?])))
          (is (= evidence (:resolved-parameter template)))
          ;; This original C6 family is not the existing supplied SH-12 family;
          ;; its rejection is not evidence of parameter-specific MIR support.
          (doseq [report [(build-mir template)
                          (invoke c11-plan 'sh13-run-branch-join-mir [template {}])]]
            (is (= :rejected (:status report)))
            (is (= ["C11-MODULE" :checked-core-shape]
                   ((juxt :diagnostic-id :missing-fact) (diagnostic report)))))
          ;; A genuine existing SH-12 MIR establishes the separate empty-params
          ;; consumer fence, without fabricating a parameter instruction.
          (let [checked (checked-core extension) mir (build-mir checked)
                function-name (first (keys (:functions mir)))
                mutated (assoc-in mir [:functions function-name :params] ['flag])
                context (invoke c11-plan 'sh13-branch-join-context [checked mutated])
                rejected (invoke c11-plan 'sh13-run-branch-join-mir [checked mutated])]
            (is (= :passed (:status (verify-mir checked mir))))
            (is (false? (invoke c11-plan 'sh13-branch-join-shape-valid? [checked mutated context])))
            (is (= :rejected (:status rejected)))
            (is (= ["C11-BLOCK" :block-terminator-or-control-flow-edge]
                   ((juxt :diagnostic-id :missing-fact) (diagnostic rejected))))
            (is (nil? (:execution rejected)))
            (is (nil? (:projection rejected))))
          ;; Invalid physical IDs are rejected as malformed DATA. The separate
          ;; valid-hash substitution test below checks source reconstruction.
          (let [substituted (assoc-in input [:resolved-parameter :sh06-artifact-id] "substituted")
                forged (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [substituted])]
            (is (= :rejected (:status (verify (:artifact-template forged))))))
          (doseq [malformed [(dissoc input :resolved-parameter)
                             (assoc-in input [:stage2-plan :kind] :gravity/stage2-hosted-core-compiled-plan)
                             (update input :stage2-plan dissoc :compiler-artifact-plan?)
                             (assoc-in input [:stage2-plan :compiler :compiled-by] :gravity-source)
                             (assoc-in input [:stage2-plan :functions 'main :arity] 2)
                             (assoc-in input [:stage2-plan :functions 'main :params] '[other])
                             (assoc-in input [:resolved-parameter :extra] true)
                             (assoc-in input [:resolved-parameter :source-content-hash] "other-source")
                             (assoc-in input [:resolved-parameter :parameter-index] 1)
                             (assoc-in input [:resolved-parameter :coordinates] [])
                             (assoc-in input [:resolved-parameter :coordinates 1 :c2-form-id] "missing")
                             (assoc-in input [:resolved-parameter :parameter-binding :binding-class] :core)
                             (assoc-in input [:resolved-parameter :parameter-binding :definition-syntax-id] "other")
                             (assoc-in input [:resolved-parameter :parameter-binding :namespace] 'other)
                             (assoc-in input [:resolved-parameter :type-binding :kind] :var)
                             (assoc-in input [:resolved-parameter :type-binding :namespace] 'other)
                             (assoc-in input [:resolved-parameter :type-binding :name] 'I64)
                             (assoc-in input [:resolved-parameter :type-binding :package :version] "forged")
                             (assoc-in input [:resolved-parameter :type-resolution :position] :expression)
                             (assoc-in input [:resolved-parameter :type-resolution :upstream-binding-id] "other")]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
          (doseq [path [[:sh06-artifact-id] [:sh06-semantic-projection-id]
                        [:projection-binding] [:root-syntax-id] [:function-syntax-id] [:function-scope-id]
                        [:parameter-binding :binding-id] [:parameter-binding :upstream-binding-id]
                        [:parameter-binding :definition-artifact-id] [:parameter-binding :definition-syntax-id]
                        [:type-binding :binding-id] [:type-binding :upstream-binding-id]
                        [:type-binding :definition-artifact-id] [:type-binding :definition-syntax-id]
                        [:coordinates 0 :projected-form-id] [:coordinates 3 :projected-syntax-id]]
                  value [nil "" 17 "sha256:not-a-digest"]]
            (is (= :rejected
                   (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template
                                    [(assoc-in input (into [:resolved-parameter] path) value)])))))
          (doseq [malformed [(-> input
                                (assoc-in [:resolved-parameter :type-binding :binding-id] nil)
                                (assoc-in [:resolved-parameter :type-resolution :binding-id] nil))
                             (assoc-in input [:resolved-parameter :parameter-binding :scope-id]
                                       (:root-syntax-id evidence))
                             (assoc-in input [:resolved-parameter :parameter-binding :profile-set] [:meta])
                             (assoc-in input [:resolved-parameter :type-binding :profile-set] [:meta])
                             (assoc-in input [:resolved-parameter :type-binding :target-set] [:jvm])
                             (assoc-in input [:resolved-parameter :type-binding :semantic-span :ordinal] 13)
                             (assoc-in input [:stage2-plan :module :exports] ['main])
                             (assoc-in input [:stage2-plan :functions 'main :binding :visibility] :public)
                             (assoc-in input [:authoritative-module :exports] [])]]
            (is (= :rejected (:status (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [malformed])))))
          (doseq [candidate [(assoc-in template [:core-nodes 0 :type] :gravity/integer)
                             (assoc-in template [:core-nodes 0 :attributes :parameter-index] 1)
                             (assoc-in template [:core-nodes 1 :operands] [])
                             (assoc template :lexical-binding-records [])]]
            (is (= :rejected (:status (verify candidate))))))))))

(deftest sh12-current-c6-replay-distinguishes-projection-substitution-from-source
  ;; One actual source/C2/C5 reconstruction, not a second public C6 admission
  ;; route. Same-input replay cannot authenticate a coherently replaced hash.
  (let [path (str (.resolve @root
                   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        input (parameter-input path (slurp path))
        replacement (get-in input [:resolved-parameter :projection-binding])
        substituted (assoc-in input [:resolved-parameter :sh06-artifact-id] replacement)
        original (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
        forged (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [substituted])
        verify (fn [source result candidate]
                 (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                         [source candidate (:digest-requests result)]))]
    (is (not= replacement (get-in input [:resolved-parameter :sh06-artifact-id])))
    (is (= :accepted (:status original)))
    (is (= :accepted (:status forged)))
    (is (= :passed (:status (verify substituted forged (:artifact-template forged)))))
    (is (= :rejected (:status (verify input original (:artifact-template forged)))))))

(deftest sh12-current-c6-preserves-hosted-export-gate-before-module-checks
  ;; Execute the ORIGINAL compiled export predicate, not a fabricated hosted
  ;; packet or a replacement acceptance function. Public hosted admission is
  ;; still outside this development fragment check.
  (let [function (get-in @parameter-c6-plan [:functions 'c6-c10-build-checked-core-template])
        body (:then (first (:instructions function)))
        shape (first (filter #(= 'plan-shape-valid? (:name %)) (:bindings body)))
        predicate (get-in shape [:expr :then :then :test])]
    (is (= :let (:op body)))
    (is (= '= (:function predicate)))
    (doseq [[input module authoritative expected]
            [[{} {:exports ['main]} {:exports []} true]
             [{} {:exports []} {:exports ['main]} false]
             [{} {} {:exports ['main]} false]
             [{:resolved-parameter {}} {} {:exports ['main]} true]
             [{:resolved-parameter {}} {} {:exports []} false]
             [{:resolved-parameter {}} {} {} false]]]
      (is (= expected
             (bootstrap/p15-s23-stage2-runtime-execute-instruction
              {:engine :gravity-sh12-branch-join-mir-test :compiler-artifact-plan? true}
              @parameter-c6-plan
              {'input input 'module module 'authoritative-module authoritative}
              predicate))))))

(deftest sh12-current-c6-bool-parameter-becomes-an-entry-value-not-an-instruction
  ;; Existing assembly attaches an EXPLICIT request to real current products.
  ;; It is DATA composition, not pinned public C6/C11 source admission.
  (doseq [extension [".gravity" ".qst"]]
    (let [path (str (.resolve @root
                     (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source" extension)))
          text (slurp path) products (parameter-source-products path text)
          input (:envelope products)
          raw (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [input])
          fresh (parameter-source-products path text)
          replay (invoke parameter-c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
          upstream (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                           [(:envelope fresh) (:artifact-template raw) (:digest-requests replay)])
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact
                context (:compiled-binding-data products) (:ingress products) sealed)
          physical (:physical-provenance core)
          request (:target-request-metadata core)
          request-preimage {:domain :gravity/c6-c10-physical-request-binding-v1
                            :artifact-id (:artifact-id core)
                            :source-content-hash (:source-content-hash context)
                            :target-request-metadata request
                            :actual-paths (:actual-paths physical)
                            :digest-graph-proof-id (:digest-graph-proof-id physical)}
          mir (build-mir core)]
      (is (= :accepted (:status raw)))
      (is (= :passed (:status upstream)))
      (is (= raw replay))
      (is (= (:sealed-artifact-template sealed)
             (dissoc core :target-request-metadata :physical-provenance)))
      (is (= (:artifact-id core) (first (:root-digests sealed))
             (:artifact-id (:sealed-artifact-template sealed))))
      (is (= {:requested-target :jvm :source-target :jvm
              :identity-bearing? false :downstream-lowering-required? true} request))
      (is (= {:source path :gravity-c6-c10-source (str (.resolve @root parameter-c6-source))}
             (:actual-paths physical)))
      (is (= (:source-content-hash input) (:source-content-hash core)
             (:source-content-hash context) (:source-content-hash physical)))
      (is (= (get-in input [:front-end-products :source-unit-id]) (:c2-source-unit-id physical)))
      (is (= (get-in input [:front-end-products :c3-artifact-id]) (:c3-artifact-id physical)))
      (is (= (:binding-pins input) (:binding-pins physical)))
      (is (= (get-in sealed [:graph-proof :graph-proof-id]) (:digest-graph-proof-id physical)))
      (is (= (bootstrap/p15-s23-c6c10-canonical-digest path request-preimage)
             (:request-binding-id physical)))
      (is (= :gravity/mir-module (:artifact mir))
          (pr-str {:diagnostics (:diagnostics mir)
                   :shape-checks
                   (when-not (= :gravity/mir-module (:artifact mir))
                     (let [view (invoke c11-plan 'sh12-c6p-role-view [core])]
                       {:lineage (invoke c11-plan 'sh12-c6p-lineage-valid? [core])
                        :metadata (invoke c11-plan 'sh12-c6p-producer-metadata-valid? [core])
                        :roles (invoke c11-plan 'sh12-c6p-shape-valid? [core view])
                        :nodes (invoke c11-plan 'sh12-c6p-role-nodes-valid? [core 0])
                        :declaration (invoke c11-plan 'sh12-c6p-declaration-valid? [core])
                        :tables (invoke c11-plan 'sh12-c6p-tables-valid?
                                        [core (:core-nodes core) (:ids view) (:origins view) 0])}))}))
      (when (= :gravity/mir-module (:artifact mir))
        (let [nodes (:core-nodes core) binder (first nodes)
              binder-id (:node-id binder) local-id (:node-id (second nodes))
              function (get-in mir [:functions 'main]) entry (:entry function)
              operations (vec (mapcat :instructions (vals (:blocks function))))
              values (get-in mir [:data-flow-graph :values])
              definition {:value-id binder-id :parameter-index 0 :block-id entry}
              verify #(verify-mir core %)]
          (is (= :passed (:status (verify mir))))
          (is (= :pending (:verification-status mir)))
          (is (= :constructed-unverified (get-in mir [:pass-execution-record :status])))
          (is (= (:artifact-id core) (:source-core mir)))
          (is (= [:hosted :jvm :jvm request]
                 ((juxt :profile :source-target :target-request :target-request-metadata) mir)))
          (is (= [binder-id] (:params function)))
          (is (= :gravity/bool (:returns function)))
          (is (= 4 (count (:blocks function))))
          (is (= 7 (count values)))
          (is (= 6 (count operations)))
          (is (not-any? #(= binder-id (:op-id %)) operations))
          (is (= definition (get-in mir [:data-flow-graph :definitions binder-id])))
          (is (= definition (:defined-by (get values binder-id))))
          (is (= :parameter (:kind (get values binder-id))))
          (is (= :not-applicable (:constant-payload (get values binder-id))))
          (is (= (:source binder) (:source (get values binder-id)) (get-in mir [:source-map binder-id])))
          (is (= [{:from binder-id :consumer-kind :operation :consumer-id local-id
                   :consumer-block entry :operand-index 0 :edge-kind :operand}]
                 (filterv #(= binder-id (:from %)) (get-in mir [:data-flow-graph :edges]))))
          (doseq [node nodes]
            (is (= (get-in core [:type-facts (:node-id node)])
                   (get-in mir [:type-table (get-in core [:type-facts (:node-id node) :fact-id])]))))
          (let [execution (invoke c11-plan 'sh13-run-branch-join-mir [core mir])]
            (is (= :rejected (:status execution)))
            (is (= ["C11-VERIFY" :bounded-execution-subset]
                   ((juxt :diagnostic-id :missing-fact) (diagnostic execution))))
            (is (nil? (:execution execution))))
          (doseq [malformed [(dissoc core :target-request-metadata)
                             (dissoc core :physical-provenance)
                             (:artifact-template raw)
                             (assoc-in core [:target-request-metadata :requested-target] nil)
                             (assoc-in core [:physical-provenance :source-content-hash] (:mapping-id core))
                             (assoc-in core [:physical-provenance :c2-source-unit-id] nil)
                             (assoc-in core [:physical-provenance :digest-graph-proof-id] nil)
                             (assoc-in core [:physical-provenance :binding-pins :builder-source-hash] (:mapping-id core))
                             (assoc-in core [:core-nodes 0 :attributes :parameter-index] 1)
                             (assoc-in core [:core-nodes 0 :type] :gravity.type/bool)
                             (assoc-in core [:core-nodes 1 :operands] [(:node-id (last nodes))])
                             (assoc-in core [:core-nodes 1 :node-id] binder-id)
                             (assoc-in core [:core-nodes 6 :attributes :params] ['other])
                             (assoc-in core [:core-nodes 6 :type :params] [:gravity/integer])
                             (assoc-in core [:resolved-parameter :parameter-binding :scope-id]
                                       (get-in core [:resolved-parameter :root-syntax-id]))
                             (assoc-in core [:resolved-parameter :type-binding :name] 'I64)
                             (assoc-in core [:resolved-parameter :type-resolution :source-span :byte-start] 0)
                             (assoc-in core [:typed-core :locals binder-id :mutability] :mutable)
                             (assoc-in core [:dependency-order-graph :edges] [])
                             (assoc-in core [:source-origin-table (get-in binder [:source :origin-id]) :domain] :other)
                             (assoc-in core [:origin-closure (get-in binder [:source :origin-id]) :generated-role] :other)
                             (assoc-in core [:core-nodes 0 :source :span :start :column] nil)
                             (assoc-in core [:core-nodes 5 :source-operation] :other)]]
            (is (= :rejected (:status (build-mir malformed)))))
          ;; These scalars survive the carrier walk, but must reject as DATA
          ;; rather than reach count/dissoc on an unchecked nested carrier.
          (let [origin (get-in binder [:source :origin-id])]
            (doseq [row [nil "not-a-row" 17]]
              (let [then-origin (get-in nodes [3 :source :origin-id])
                    report (build-mir (assoc-in core [:source-origin-table then-origin] row))]
                (is (= :rejected (:status report)))
                (is (= "C11-ORIGIN" (:diagnostic-id (diagnostic report))))))
            (doseq [[field invalids] [[:c2-metadata [nil [] 0 ""]]
                                      [:c2-generated-origin [false {} 0 ""]]]
                    value invalids]
              (let [malformed (-> core
                                  (assoc-in [:source-origin-table origin field] value)
                                  (assoc-in [:origin-closure origin field] value))
                    report (build-mir malformed)]
                (is (= :rejected (:status report)))
                (is (= "C11-ORIGIN" (:diagnostic-id (diagnostic report))))))
            (doseq [rows [nil 17 {} ""]]
              (is (= :rejected
                     (:status (build-mir (assoc-in core [:resolved-parameter :coordinates] rows)))))))
          (let [equalities (atom 0)
                hostile (proxy [Object] []
                          (equals [_] (swap! equalities inc) false))]
            ;; Positive observation proves the counter itself is live.
            (is (false? (.equals hostile nil)))
            (is (= 1 @equalities))
            (reset! equalities 0)
            (is (false? (invoke c11-plan 'sh12-c6p-scalars-valid? [hostile])))
            (is (= :rejected (:status (build-mir (assoc core :diagnostics [hostile])))))
            (is (true? (invoke c11-plan 'sh12-c6p-family? [core])))
            (is (false? (invoke c11-plan 'sh12-c6p-family? [(assoc core :kind hostile)])))
            (is (= :rejected (:status (build-mir (assoc core :kind hostile)))))
            (is (zero? @equalities)))
          (doseq [[malformed expected]
                  [[(assoc-in core [:type-facts binder-id :declaration :parameter-index] 1) "C11-TYPE"]
                   [(assoc-in core [:effect-facts binder-id :direct] #{:io/write}) "C11-EFFECT"]
                   [(assoc-in core [:core-nodes 0 :safety :check] {}) "C11-SAFETY"]
                   [(assoc-in core [:capability-facts binder-id :fact-id] binder-id) "C11-VERIFY"]
                   [(assoc-in core [:core-nodes 0 :source :span :start :column] nil) "C11-ORIGIN"]]]
            (let [report (build-mir malformed)]
              (is (= :rejected (:status report)))
              (is (= expected (:diagnostic-id (diagnostic report))))
              (when (not= expected "C11-ORIGIN")
                (is (= binder-id (:value-id (diagnostic report))))
                (is (= :not-applicable (:operation-id (diagnostic report)))))))
          ;; Input authenticity remains the independent original C6 replay,
          ;; separately from this C11 supplied-DATA semantic-family verifier.
          (doseq [candidate [(assoc-in (:artifact-template raw) [:core-nodes 0 :attributes :parameter-index] 1)
                             (assoc-in (:artifact-template raw) [:core-nodes 1 :operands] [])
                             (assoc-in (:artifact-template raw) [:resolved-parameter :type-binding :name] 'I64)
                             (assoc (:artifact-template raw) :proof-records {})]]
            (is (= :rejected
                   (:status (invoke parameter-c6-plan 'c6-c10-verify-checked-core-template
                                    [(:envelope fresh) candidate (:digest-requests replay)])))))
          (doseq [candidate [(assoc-in mir [:functions 'main :params] [])
                             (assoc-in mir [:functions 'main :params] [binder-id binder-id])
                             (assoc-in mir [:data-flow-graph :definitions binder-id :operation-id] nil)
                             (update-in mir [:data-flow-graph :definitions binder-id] dissoc :parameter-index)
                             (assoc-in mir [:data-flow-graph :definitions binder-id :parameter-index] 1)
                             (assoc-in mir [:data-flow-graph :definitions binder-id :block-id]
                                       (str (:artifact-id core) ":mir:then"))
                             (assoc-in mir [:data-flow-graph :values binder-id :kind] :instruction-result)
                             (update-in mir [:functions 'main :blocks entry :instructions]
                                        conj (assoc (first operations) :op-id binder-id))
                             (update mir :source-map dissoc binder-id)
                             (assoc-in mir [:type-table (get-in core [:type-facts binder-id :fact-id]) :type] :gravity/integer)
                             (assoc-in mir [:safety-table (str binder-id ":safety-outcome") :proof] binder-id)
                             (assoc-in mir [:data-flow-graph :edges 0 :from] (:node-id (last nodes)))
                             (assoc-in mir [:control-flow-graph :join :incoming] [])
                             (assoc mir :source-core (:mapping-id core))]]
            (is (= :rejected (:status (verify candidate))))))))))

(def ^:private stable-diagnostic-fields
  #{:diagnostic-id :severity :stage :mir-module :function :block
    :operation-id :value-id :source-span :origin-chain :profile
    :target-request :missing-fact :provenance :remediation})

(deftest sh12-branch-join-api-is-gravity-owned-and-bounded
  (let [functions (:functions @c11-plan)
        policy (invoke c11-plan 'sh12-branch-join-mir-policy [])]
    (is (= 0 (get-in functions ['sh12-branch-join-mir-policy :arity])))
    (is (= 1 (get-in functions ['sh12-build-branch-join-mir :arity])))
    (is (= 2 (get-in functions ['sh12-verify-branch-join-mir :arity])))
    (is (= :single-conditional-branch-join (:scope policy)))
    (is (= :entry-then-else-join-return
           (:control-flow-shape policy)))
    (is (= 1 (:maximum-functions policy)))
    (is (= 1 (:maximum-conditionals policy)))
    (is (true? (:target-independent? policy)))
    (is (some #{:generic-c11-verifier} (:nonclaims policy)))
    (is (some #{:sh12-completion} (:pending policy)))))

(deftest sh12-branch-join-fixtures-are-co-canonical
  (doseq [fixtures [accepted-fixtures rejected-fixtures]]
    (is (= (slurp (str (.resolve @root (get fixtures ".gravity"))))
           (slurp (str (.resolve @root (get fixtures ".qst")))))))
  (is (= (checked-core ".gravity")
         (-> (checked-core ".qst")
             (assoc-in [:provenance :actual-source-path]
                       (get-in (checked-core ".gravity")
                               [:provenance :actual-source-path])))))
  (is (= (invoke (get rejected-plans ".gravity")
                 'sh12-branch-join-rejection-cases
                 [(rejected-path ".gravity")])
         (mapv #(assoc % :actual-source-path (rejected-path ".gravity"))
               (invoke (get rejected-plans ".qst")
                       'sh12-branch-join-rejection-cases
                       [(rejected-path ".qst")])))))

(deftest sh12-constructs-and-verifies-a-branch-join-diamond
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            report (verify-mir checked mir)
            function (first (vals (:functions mir)))
            blocks (:blocks function)
            control-flow (:control-flow-graph mir)
            join (:join control-flow)
            incoming (:incoming join)]
        (is (= :gravity/mir-module (:artifact mir)))
        (is (= (str (.resolve @root (get accepted-fixtures extension)))
               (get-in checked [:provenance :actual-source-path])))
        (is (= (:artifact-id checked) (:source-core mir)))
        (is (true? (:target-independent? mir)))
        (is (= 4 (count blocks)))
        (is (= 4 (count (:edges control-flow))))
        (is (= 2 (count incoming)))
        (is (= (set (map :predecessor incoming))
               (set (:predecessors (get blocks (:block-id join))))))
        (is (= [:conditional-branch :branch :branch :return]
               (mapv (fn [block-id]
                       (get-in blocks [block-id :terminator :kind]))
                     [(:entry function)
                      (:predecessor (first incoming))
                      (:predecessor (second incoming))
                      (:block-id join)])))
        (doseq [node (:core-nodes checked)]
          (let [node-id (:node-id node)]
            (is (contains? (:type-table mir)
                           (get-in checked [:type-facts node-id :fact-id])))
            (is (contains? (:effect-table mir)
                           (get-in checked [:effect-facts node-id :fact-id])))
            (is (contains? (:ownership-table mir)
                           (get-in checked
                                   [:ownership-facts node-id :fact-id])))
            (is (contains? (:capability-table mir)
                           (str node-id ":capability-fact")))
            (is (contains? (:safety-table mir)
                           (str node-id ":safety-outcome")))
            (is (contains? (:profile-target-table mir)
                           (str node-id ":profile-target-fact")))
            (is (= (:source node) (get-in mir [:source-map node-id])))))
        (is (contains? (:capability-proof-table mir)
                       "sh12:branch-join:capability-proof"))
        (is (= "sh12:branch-join:provenance"
               (get-in mir [:provenance
                            :checked-core-provenance-binding-id])))
        (is (= :passed (:status report)))
        (is (empty? (:diagnostics report)))
        (is (every? (set (:preserves report))
                    [:type-facts :effect-facts :ownership-facts
                     :safety-facts :source-origin-map
                     :control-flow-edges :data-flow-edges
                     :dominance-facts]))))))

(deftest sh12-runtime-check-consumes-and-guards-the-branch-condition
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked
            (invoke (get rejected-plans extension)
                    'sh12-runtime-checked-core
                    [(checked-core extension)])
            mir (build-mir checked)
            report (verify-mir checked mir)
            function-name (first (keys (:functions mir)))
            function (first (vals (:functions mir)))
            blocks (:blocks function)
            operations (mapcat :instructions (vals (:blocks function)))
            runtime-operation
            (first (filter #(= :runtime-check (:opcode %)) operations))
            if-operation
            (first (filter #(= :if (:source-operation %)) operations))
            condition-id "sh12:branch-join:condition"
            check-id "sh12:branch-join:runtime-check"
            check-operation-id (str check-id ":mir:runtime-check")
            token-id (str check-id ":mir:token")
            data-edges (get-in mir [:data-flow-graph :edges])
            terminator-uses
            (get-in mir [:data-flow-graph :terminator-uses])
            check-entry (get-in mir [:runtime-check-table check-id])
            entry-block-id (:entry function)
            entry-block (get blocks entry-block-id)
            entry-terminator (:terminator entry-block)
            entry-terminator-id (:terminator-id entry-terminator)
            entry-operation-ids (mapv :op-id (:instructions entry-block))
            join-block-id (get-in mir [:control-flow-graph :join :block-id])
            join-operation-ids
            (mapv :op-id
                  (get-in function [:blocks join-block-id :instructions]))
            effect-entry
            (get-in mir [:effect-table
                         (get-in runtime-operation
                                 [:facts :effect-fact-id])])
            late-check-mir
            (-> mir
                (update-in [:functions function-name :blocks
                            entry-block-id :instructions]
                           #(vec (remove
                                  (fn [operation]
                                    (= check-operation-id
                                       (:op-id operation)))
                                  %)))
                (update-in [:functions function-name :blocks
                            join-block-id :instructions]
                           #(vec (cons runtime-operation %))))
            late-check-report (verify-mir checked late-check-mir)]
        (is (= :passed (:status report)))
        (is (empty? (:diagnostics report)))
        (is (= check-operation-id (:op-id runtime-operation)))
        (is (= [condition-id] (:operands runtime-operation)))
        (is (= #{:error/raise} (:effects runtime-operation)))
        (is (= #{:error/raise} (:latent-effects function)))
        (is (= #{:error/raise} (:direct effect-entry)))
        (is (= #{:error/raise}
               (get-in mir [:data-flow-graph :values token-id :effects])))
        (is (= entry-terminator-id
               (get-in runtime-operation
                       [:facts :guarded-operation-id])))
        (is (= entry-terminator-id
               (:guarded-operation-id check-entry)))
        (is (= check-operation-id (:check-operation-id check-entry)))
        (is (= token-id (:token-value-id check-entry)))
        (is (= entry-block-id (:block-id runtime-operation)))
        (is (= check-operation-id (last entry-operation-ids)))
        (is (= -1 (.indexOf join-operation-ids check-operation-id)))
        (is (< -1 (.indexOf join-operation-ids (:op-id if-operation))))
        (is (= [token-id condition-id] (:operands entry-terminator)))
        (is (= check-id
               (get-in entry-terminator [:facts :runtime-check-id])))
        (is (= token-id
               (get-in entry-terminator [:facts :runtime-check-token-id])))
        (is (= [condition-id
                "sh12:branch-join:then"
                "sh12:branch-join:else"]
               (:operands if-operation)))
        (is (some #(and (= condition-id (:from %))
                        (= check-operation-id (:consumer-id %))
                        (= entry-block-id (:consumer-block %))
                        (= :operand (:edge-kind %)))
                  data-edges))
        (is (some #(and (= token-id (:value-id %))
                        (= entry-terminator-id (:consumer-id %))
                        (= entry-block-id (:consumer-block %))
                        (= :runtime-check-guard (:edge-kind %)))
                  terminator-uses))
        (is (some #(and (= condition-id (:value-id %))
                        (= entry-terminator-id (:consumer-id %))
                        (= 1 (:operand-index %))
                        (= :condition (:edge-kind %)))
                  terminator-uses))
        (is (= :runtime-check-generated
               (get-in runtime-operation
                       [:source :generated-origin 0 :role])))
        (is (= :rejected (:status late-check-report)))
        (is (= "C11-BLOCK"
               (get-in late-check-report [:diagnostics 0 :diagnostic-id])))
        (is (= :block-terminator-or-control-flow-edge
               (get-in late-check-report [:diagnostics 0 :missing-fact])))))))

(deftest sh12-rejects-malformed-control-flow-and-fact-loss
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            rejected-plan (get rejected-plans extension)
            cases
            (invoke rejected-plan
                    'sh12-branch-join-rejection-cases
                    [(rejected-path extension)])]
        (doseq [{:keys [case diagnostic-id missing-fact
                        actual-source-path]} cases]
          (testing (name case)
            (let [mutation
                  (invoke rejected-plan
                          'sh12-malformed-branch-join-mir
                          [case mir actual-source-path])
                  malformed (:mir mutation)
                  report (verify-mir checked malformed)
                  observed (diagnostic report)]
              (is (= actual-source-path (:actual-source-path mutation)))
              (is (not= mir malformed))
              (is (= :rejected (:status report)))
              (is (= 1 (count (:diagnostics report))))
              (is (= stable-diagnostic-fields (set (keys observed))))
              (is (= diagnostic-id (:diagnostic-id observed)))
              (is (= missing-fact (:missing-fact observed)))
              (is (= :error (:severity observed)))
              (is (= :c11-mir-verification (:stage observed)))
              (is (= actual-source-path
                     (get-in observed [:provenance
                                       :actual-source-path])))
              (if (= diagnostic-id "C11-MODULE")
                (is (= :not-applicable (:block observed)))
                (is (string? (:block observed))))
              (if (= diagnostic-id "C11-DOMINANCE")
                (is (= "sh12:branch-join:if" (:value-id observed)))
                (is (= :not-applicable (:value-id observed)))))))))))

(deftest sh12-rejects-malformed-checked-core-before-construction
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            rejected-plan (get rejected-plans extension)
            actual-source-path (rejected-path extension)
            cases
            (invoke rejected-plan
                    'sh12-checked-core-rejection-cases
                    [actual-source-path])]
        (doseq [{:keys [case diagnostic-id missing-fact]} cases]
          (testing (name case)
            (let [malformed
                  (invoke rejected-plan
                          'sh12-malformed-checked-core
                          [case checked actual-source-path])
                  construction (build-mir malformed)
                  verification (verify-mir malformed mir)]
              (is (= :rejected (:status construction)))
              (is (= diagnostic-id
                     (get-in construction
                             [:diagnostics 0 :diagnostic-id])))
              (is (= missing-fact
                     (get-in construction
                             [:diagnostics 0 :missing-fact])))
              (is (= :rejected (:status verification)))
              (is (= diagnostic-id
                     (get-in verification
                             [:diagnostics 0 :diagnostic-id])))
              (is (= missing-fact
                     (get-in verification
                             [:diagnostics 0 :missing-fact])))
              (if (= case :checked-empty-provenance-path)
                (is (= ""
                       (get-in verification
                               [:diagnostics 0 :provenance
                                :actual-source-path])))
                (is (= actual-source-path
                       (get-in verification
                               [:diagnostics 0 :provenance
                                :actual-source-path])))))))))))

(deftest sh12-rejects-unbound-inline-ownership-and-effects
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            condition-id "sh12:branch-join:condition"
            mutate-condition
            (fn [transform]
              (update checked :core-nodes
                      (fn [nodes]
                        (mapv
                         (fn [node]
                           (if (= condition-id (:node-id node))
                             (transform node)
                             node))
                         nodes))))
            cases
            [{:malformed
              (mutate-condition
               #(assoc-in % [:ownership :value-id] "attacker:value"))
              :diagnostic-id "C11-VERIFY"
              :missing-fact :ownership-fact}
             {:malformed
              (mutate-condition #(assoc % :effects #{:network/http}))
              :diagnostic-id "C11-EFFECT"
              :missing-fact :effect-fact-or-ordering}
             {:malformed
              (assoc-in
               (mutate-condition #(assoc % :effects #{:network/http}))
               [:effect-facts condition-id :transitive]
               #{:network/http})
              :diagnostic-id "C11-EFFECT"
              :missing-fact :effect-fact-or-ordering}]]
        (doseq [{:keys [malformed diagnostic-id missing-fact]} cases]
          (let [construction (build-mir malformed)
                verification (verify-mir malformed mir)]
            (is (= :rejected (:status construction)))
            (is (= diagnostic-id
                   (get-in construction [:diagnostics 0 :diagnostic-id])))
            (is (= missing-fact
                   (get-in construction [:diagnostics 0 :missing-fact])))
            (is (= :rejected (:status verification)))
            (is (= diagnostic-id
                   (get-in verification [:diagnostics 0 :diagnostic-id])))
            (is (= missing-fact
                   (get-in verification [:diagnostics 0 :missing-fact])))))))))

(deftest sh12-rejects-host-constructed-empty-symbol-origin
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [checked (checked-core extension)
            mir (build-mir checked)
            node-id (get-in checked [:core-nodes 0 :node-id])
            malformed
            (update checked :core-nodes
                    (fn [nodes]
                      (mapv
                       (fn [node]
                         (if (= node-id (:node-id node))
                           (-> node
                               (assoc-in [:source :generated?] true)
                               (assoc-in [:source :generated-origin]
                                         [(symbol "")]))
                           node))
                       nodes)))
            construction (build-mir malformed)
            verification (verify-mir malformed mir)]
        (is (= :rejected (:status construction)))
        (is (= "C11-ORIGIN"
               (get-in construction [:diagnostics 0 :diagnostic-id])))
        (is (= :source-span-or-origin-fact
               (get-in construction [:diagnostics 0 :missing-fact])))
        (is (= :rejected (:status verification)))
        (is (= "C11-ORIGIN"
               (get-in verification [:diagnostics 0 :diagnostic-id])))
        (is (= :source-span-or-origin-fact
               (get-in verification [:diagnostics 0 :missing-fact])))))))

(deftest sh12-node-local-preflight-diagnostics-preserve-source
  ;; Supplied checked-Core DATA exercises the original C11 ingress, not C10
  ;; source authentication. Distinct spans/origins make an if-node fallback fail.
  (let [then-id "sh12:branch-join:then"
        else-id "sh12:branch-join:else"
        checked
        (update (checked-core ".gravity") :core-nodes
                (fn [nodes]
                  (mapv (fn [node]
                          (if (#{then-id else-id} (:node-id node))
                            (-> node
                                (assoc-in [:source :generated?] true)
                                (assoc-in [:source :generated-origin]
                                          [{:kind :macro-expansion
                                            :source-id (:node-id node)}]))
                            node))
                        nodes)))
        nodes (into {} (map (juxt :node-id identity) (:core-nodes checked)))
        mir (build-mir checked)
        bad-type #(assoc-in % [:type-facts %2 :type] :gravity.type/bool)
        cases
        [[(bad-type checked then-id) then-id "C11-TYPE"
          :type-fact-or-operation-schema]
         [(bad-type checked else-id) else-id "C11-TYPE"
          :type-fact-or-operation-schema]
         [(bad-type (bad-type checked else-id) then-id) then-id "C11-TYPE"
          :type-fact-or-operation-schema]
         [(assoc-in checked [:effect-facts then-id :direct] #{:io/write})
          then-id "C11-EFFECT" :effect-fact-or-ordering]
         [(assoc-in checked [:ownership-facts else-id :value-id] "wrong:value")
          else-id "C11-VERIFY" :ownership-fact]
         [(assoc-in checked [:core-nodes 1 :operands] :malformed)
          then-id "C11-DOMINANCE" :core-node-operands]]]
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= mir (invoke c11-plan 'c11-build-target-independent-mir [checked])))
    (is (= :passed (:status (verify-mir checked mir))))
    (doseq [[malformed node-id diagnostic-id missing-fact] cases
            [phase report] [[:construction (build-mir malformed)]
                            [:verification (verify-mir malformed mir)]]]
      (let [observed (diagnostic report)
            source (:source (get nodes node-id))
            legacy (invoke c11-plan 'sh12-branch-join-diagnostic
                           [diagnostic-id missing-fact malformed
                            (when (= phase :verification) mir)])
            expected (assoc legacy :operation-id node-id
                            :value-id (if (= "C11-DOMINANCE" diagnostic-id)
                                        node-id :not-applicable)
                            :block :not-applicable
                            :source-span (:span source)
                            :origin-chain (:generated-origin source))]
        (is (= :rejected (:status report)))
        (is (= phase (:phase report)))
        (is (= 1 (count (:diagnostics report))))
        (is (= stable-diagnostic-fields (set (keys observed))))
        (is (= diagnostic-id (:diagnostic-id observed)))
        (is (= missing-fact (:missing-fact observed)))
        (is (= node-id (:operation-id observed)))
        (is (= (:span source) (:source-span observed)))
        (is (= (:generated-origin source) (:origin-chain observed)))
        (is (= :not-applicable (:block observed)))
        (is (= expected observed))))
    ;; Missing/malformed source and aggregate problems retain original fallback.
    (doseq [[malformed diagnostic-id missing-fact]
            [[(assoc-in checked [:core-nodes 1 :source] nil)
              "C11-ORIGIN" :source-span-or-origin-fact]
             [(assoc-in checked [:core-nodes 2 :source :generated-origin] [])
              "C11-ORIGIN" :source-span-or-origin-fact]
             [(dissoc checked :core-nodes) "C11-MODULE" :checked-core-shape]
             [(assoc-in checked [:type-facts else-id :fact-id]
                        (get-in checked [:type-facts then-id :fact-id]))
              "C11-TYPE" :unique-type-fact-identity]]
            [phase report] [[:construction (build-mir malformed)]
                            [:verification (verify-mir malformed mir)]]]
      (let [expected (invoke c11-plan 'sh12-branch-join-diagnostic
                             [diagnostic-id missing-fact malformed
                              (when (= phase :verification) mir)])]
        (is (= :rejected (:status report)))
        (is (= [expected] (:diagnostics report)))))))

(deftest sh12-operation-type-diagnostics-preserve-expected-source
  ;; Supplied checked-Core ingress; candidate MIR metadata is not source authority.
  (let [checked
        (update (checked-core ".gravity") :core-nodes
                (fn [nodes]
                  (mapv (fn [node]
                          (-> node
                              (assoc-in [:source :generated?] true)
                              (assoc-in [:source :generated-origin]
                                        [{:kind :macro-expansion
                                          :source-id (:node-id node)}])))
                        nodes)))
        mir (build-mir checked)
        function-name (:entrypoint checked)
        prefix [:functions function-name :blocks]
        then-block (str (:artifact-id checked) ":mir:then")
        else-block (str (:artifact-id checked) ":mir:else")
        then-path (conj prefix then-block :instructions 0)
        else-path (conj prefix else-block :instructions 0)
        wrong-type #(assoc-in % (conj %2 :type) :gravity.type/bool)
        wrong-then (wrong-type mir then-path)
        cases
        [[wrong-then then-path]
         [(wrong-type mir else-path) else-path]
         [(wrong-type (wrong-type mir else-path) then-path) then-path]
         [(assoc-in mir (conj then-path :facts :type-fact-id) "wrong:type-fact")
          then-path]
         [(assoc-in wrong-then [:data-flow-graph :values "sh12:branch-join:then" :type]
                    :gravity.type/bool) then-path]
         [(-> (wrong-type mir else-path)
              (assoc-in (conj else-path :source) {:span :forged :generated-origin [:forged]})
              (assoc-in (conj else-path :block-id) "forged:block")) else-path]]]
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= mir (invoke c11-plan 'c11-build-target-independent-mir [checked])))
    (is (= :passed (:status (verify-mir checked mir))))
    (doseq [[malformed path] cases]
      (let [report (verify-mir checked malformed)
            operation (get-in mir path)
            source (:source operation)
            observed (diagnostic report)
            expected
            (assoc (invoke c11-plan 'sh12-branch-join-diagnostic
                           ["C11-TYPE" :type-fact-or-operation-schema checked malformed])
                   :operation-id (:op-id operation) :block (:block-id operation)
                   :source-span (:span source) :origin-chain (:generated-origin source))]
        (is (= :rejected (:status report)))
        (is (= 1 (count (:diagnostics report))))
        (is (= stable-diagnostic-fields (set (keys observed))))
        (is (= "C11-TYPE" (:diagnostic-id observed)))
        (is (= :type-fact-or-operation-schema (:missing-fact observed)))
        (is (= (:op-id operation) (:operation-id observed)))
        (is (= (:block-id operation) (:block observed)))
        (is (= (:span source) (:source-span observed)))
        (is (= (:generated-origin source) (:origin-chain observed)))
        (is (= expected observed))))
    ;; A malformed first expected source cannot redirect to a later mismatch.
    ;; This defensive helper input is not a freshly constructed MIR product.
    (let [malformed (wrong-type wrong-then else-path)
          context (invoke c11-plan 'sh12-branch-join-fact-context [mir malformed])
          context (assoc-in context [:expected-operations 1 :source] nil)
          report (invoke c11-plan 'sh12-branch-join-type-rejection
                         [checked mir malformed context])
          expected (invoke c11-plan 'sh12-branch-join-diagnostic
                           ["C11-TYPE" :type-fact-or-operation-schema checked malformed])]
      (is (= :rejected (:status report)))
      (is (= [expected] (:diagnostics report))))
    ;; Original earlier/aggregate failures must not become operation diagnoses.
    (doseq [[malformed id missing]
            [[(assoc wrong-then :type-table {})
              "C11-TYPE" :type-fact-or-operation-schema]
             [(assoc-in wrong-then [:functions function-name :returns] :gravity.type/bool)
              "C11-TYPE" :type-fact-or-operation-schema]
             [(assoc-in mir [:data-flow-graph :values "sh12:branch-join:then" :type]
                        :gravity.type/bool)
              "C11-TYPE" :type-fact-or-operation-schema]
             [(assoc wrong-then :module-id 'forged.module)
              "C11-MODULE" :module-identity-or-target-independence]
             [(assoc-in wrong-then (conj prefix then-block :instructions) [])
              "C11-BLOCK" :block-terminator-or-control-flow-edge]]]
      (let [report (verify-mir checked malformed)
            expected (invoke c11-plan 'sh12-branch-join-diagnostic
                             [id missing checked malformed])]
        (is (= :rejected (:status report)))
        (is (= [expected] (:diagnostics report)))))))

(deftest sh12-operation-effect-safety-diagnostics-preserve-expected-source
  ;; Supplied checked-Core DATA; only fresh expected MIR supplies attribution.
  (let [checked
        (update (checked-core ".gravity") :core-nodes
                (fn [nodes]
                  (mapv #(-> % (assoc-in [:source :generated?] true)
                             (assoc-in [:source :generated-origin]
                                       [{:kind :macro-expansion :source-id (:node-id %)}])) nodes)))
        mir (build-mir checked)
        function-name (:entrypoint checked)
        prefix [:functions function-name :blocks]
        then-block (str (:artifact-id checked) ":mir:then")
        else-block (str (:artifact-id checked) ":mir:else")
        then-path (conj prefix then-block :instructions 0)
        else-path (conj prefix else-block :instructions 0)
        terminator-path (conj prefix then-block :terminator :effects)
        value-path [:data-flow-graph :values "sh12:branch-join:then"]]
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= mir (invoke c11-plan 'c11-build-target-independent-mir [checked])))
    (is (= :passed (:status (verify-mir checked mir))))
    (doseq [[family id reason field value second-field third-field]
            [[:effects "C11-EFFECT" :effect-fact-or-ordering
              [:effects] #{:io/write} [:facts :effect-fact-id] [:ordering]]
             [:safety "C11-SAFETY" :safety-outcome-or-runtime-check-fact
              [:facts :safety-outcome-id] "wrong:outcome"
              [:facts :safety-proof-id] [:facts :runtime-check-id]]]]
      (let [mutate #(assoc-in % (into %2 field) value)
            wrong-then (mutate mir then-path)
            value-only (assoc-in mir (conj value-path family) :forged)
            later-only (if (= family :effects)
                         (assoc-in value-only terminator-path #{:io/write}) value-only)
            cases
            [[wrong-then then-path]
             [(mutate mir else-path) else-path]
             [(mutate (mutate mir else-path) then-path) then-path]
             [(assoc-in mir (into then-path second-field) "wrong:fact") then-path]
             [(mutate later-only then-path) then-path]
             [(-> (mutate mir else-path)
                  (assoc-in (conj else-path :source) {:span :forged :generated-origin [:forged]})
                  (assoc-in (conj else-path :block-id) "forged:block")) else-path]
             [(assoc-in mir (into then-path third-field) :forged) then-path]]]
        (doseq [[malformed path] cases]
          (let [report (verify-mir checked malformed)
                operation (get-in mir path) source (:source operation)
                observed (diagnostic report)
                expected
                (assoc (invoke c11-plan 'sh12-branch-join-diagnostic [id reason checked malformed])
                       :operation-id (:op-id operation) :block (:block-id operation)
                       :source-span (:span source) :origin-chain (:generated-origin source))]
            (is (= :rejected (:status report)))
            (is (= 1 (count (:diagnostics report))))
            (is (= stable-diagnostic-fields (set (keys observed))))
            (is (= id (:diagnostic-id observed)))
            (is (= reason (:missing-fact observed)))
            (is (= (:op-id operation) (:operation-id observed)))
            (is (= (:block-id operation) (:block observed)))
            (is (= (:span source) (:source-span observed)))
            (is (= (:generated-origin source) (:origin-chain observed)))
            (is (= expected observed))))
        ;; Defensive internal context only: do not skip a malformed first
        ;; expected source in favor of the later valid operation mismatch.
        (let [malformed (mutate wrong-then else-path)
              context (invoke c11-plan 'sh12-branch-join-fact-context [mir malformed])
              context (assoc-in context [:expected-operations 1 :source] nil)
              helper (if (= family :effects)
                       'sh12-branch-join-effect-rejection 'sh12-branch-join-safety-rejection)
              report (invoke c11-plan helper [checked mir malformed context])
              expected (invoke c11-plan 'sh12-branch-join-diagnostic [id reason checked malformed])]
          (is (= :rejected (:status report)))
          (is (= [expected] (:diagnostics report))))
        (let [aggregates
              (if (= family :effects)
                [[(assoc wrong-then :effect-table {}) id reason]
                 [(assoc wrong-then :effect-order-graph :forged) id reason]
                 [(assoc-in wrong-then [:functions function-name :latent-effects] #{:io/write}) id reason]
                 [(assoc-in mir terminator-path #{:io/write}) id reason]]
                [[(assoc wrong-then :safety-table {}) id reason]
                 [(assoc wrong-then :runtime-check-table {:forged {}}) id reason]
                 [(assoc wrong-then :proof-certificate-table {:forged {}}) id reason]
                 [(assoc wrong-then :effect-table {}) "C11-EFFECT" :effect-fact-or-ordering]])]
          (doseq [[malformed diagnostic-id missing]
                  (into aggregates
                        [[value-only id reason]
                         [(assoc wrong-then :type-table {}) "C11-TYPE" :type-fact-or-operation-schema]
                         [(assoc wrong-then :module-id 'forged.module) "C11-MODULE" :module-identity-or-target-independence]
                         [(assoc-in wrong-then (conj prefix then-block :instructions) [])
                          "C11-BLOCK" :block-terminator-or-control-flow-edge]])]
            (let [report (verify-mir checked malformed)
                  expected (invoke c11-plan 'sh12-branch-join-diagnostic
                                   [diagnostic-id missing checked malformed])]
              (is (= :rejected (:status report)))
              (is (= [expected] (:diagnostics report))))))))))
