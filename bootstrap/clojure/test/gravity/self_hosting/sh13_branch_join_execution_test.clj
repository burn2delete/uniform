(ns gravity.self-hosting.sh13-branch-join-execution-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

;; Original Gravity producers/consumers execute through their existing seed
;; plans. The retained checked-Core records are supplied DATA, not fresh C10.
(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn- invoke [name & arguments]
  ((existing 'invoke) (existing 'c11-plan) name (vec arguments)))

(defn- checked [extension]
  ((existing 'checked-core) extension))

(defn- build [core]
  (invoke 'sh12-build-branch-join-mir core))

(defn- run [core mir]
  (invoke 'sh13-run-branch-join-mir core mir))

(defn- verify [core mir result]
  (invoke 'sh13-verify-branch-join-execution core mir result))

(defn- current-parameter-pair [path]
  ;; Original current-source products and original assembler, not a supplied
  ;; normalized module, patched MIR or public pinned-source admission.
  (let [source-start (System/nanoTime)
        text (slurp path) products ((existing 'parameter-source-products) path text)
        c6-plan (existing 'parameter-c6-plan) call (existing 'invoke)
        raw (call c6-plan 'c6-c10-build-checked-core-template [(:envelope products)])
        fresh ((existing 'parameter-source-products) path text)
        replay (call c6-plan 'c6-c10-build-checked-core-template [(:envelope fresh)])
        upstream (call c6-plan 'c6-c10-verify-checked-core-template
                       [(:envelope fresh) (:artifact-template raw) (:digest-requests replay)])
        sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
        context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
        core (bootstrap/p15-s23-c6c10-public-artifact
              context (:compiled-binding-data products) (:ingress products) sealed)
        source-end (System/nanoTime) mir (build core) construction-end (System/nanoTime)]
    ;; Observations around already-required calls only: no extra build,
    ;; acceptance cache, timing API or benchmark/isolation claim.
    (println "parameter-pair phases(ns)" (.getName (java.io.File. path))
             :source-c6-replay-seal (- source-end source-start)
             :initial-c11-construction (- construction-end source-end))
    {:raw raw :upstream upstream :core core :mir mir}))

(defn- argument-run-observation [core mir arguments]
  ;; Preserve the real missing-entrypoint RED as an observation, not a fake
  ;; rejected/accepted execution or an alternate interpreter.
  (try (invoke 'sh13-run-branch-join-mir-with-arguments core mir arguments)
       (catch clojure.lang.ExceptionInfo error
         {:consumer-exception (select-keys (ex-data error) [:id :function])})))

(deftest sh13-executes-original-c6-bool-parameter-through-explicit-arguments
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"
        original-path (str (.resolve root (str relative ".gravity")))
        same-arms (java.io.File/createTempFile "gravity-sh13-equal-bool-arms-" ".gravity")]
    (try
      ;; A real temporary source, not an altered Core/MIR, establishes the
      ;; equal-return case in which the arguments must still bind the result.
      (spit same-arms (str/replace (slurp original-path) "(if flag true false)" "(if flag true true)"))
      (doseq [[path equal-arms?] [[original-path false]
                                [(str (.resolve root (str relative ".qst"))) false]
                                [(.getAbsolutePath same-arms) true]]]
        (let [{:keys [raw upstream core mir]} (current-parameter-pair path)
              nodes (:core-nodes core) binder (first nodes) id (:node-id binder)
              definition (get-in mir [:data-flow-graph :definitions id])
              outcomes (mapv #(argument-run-observation core mir [%]) [true false])]
          (is (= :accepted (:status raw)))
          (is (= :passed (:status upstream)))
          (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
          (doseq [[argument result] (map vector [true false] outcomes)]
            (is (= :accepted (:status result)) (pr-str result))
            (when (= :accepted (:status result))
              (is (= [argument] (:arguments result)))
              (is (= (if equal-arms? true argument) (get-in result [:execution :value])))
              (is (= 506 (get-in result [:execution :fuel])))
              (is (= :passed (get-in result [:upstream-verification :status])))
              (is (false? (:self-hosted? result)))
              (is (some #{:authenticated-public-c6-c11-source-ingress} (:pending result)))
              (is (some #{:authenticated-public-c6-c11-source-ingress} (:nonclaims result)))
              (is (= [id] (mapv :value-id (get-in result [:projection :parameters]))))
              (is (= definition (get-in result [:projection :parameters 0 :definition])))
              (is (= (:source binder) (get-in result [:projection :parameters 0 :source])))
              (is (= :gravity/bool (get-in result [:projection :parameters 0 :source-type])))
              (is (= :bool (get-in result [:projection :parameters 0 :runtime-type])))
              (is (= (get-in core [:core-nodes 6 :type])
                     (get-in result [:projection :operations 5 :source-type])))
              (is (= (get-in core [:type-facts id :fact-id])
                     (get-in result [:projection :parameters 0 :facts :type-fact-id])))
              (is (= [(get-in nodes [1 :node-id]) (get-in nodes [2 :node-id])
                      (get-in nodes [(if argument 3 4) :node-id])]
                     (vec (keep :operation (get-in result [:execution :trace])))))
              (is (not-any? #(= id (:operation %)) (get-in result [:execution :trace])))
              (is (= :passed
                     (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                      core mir [argument] result))))
              (doseq [candidate [(assoc result :arguments [(not argument)])
                                 (assoc-in result [:execution :value] (not (get-in result [:execution :value])))
                                 (assoc-in result [:projection :parameters 0 :definition :operation-id] nil)
                                 (assoc-in result [:projection :parameters 0 :value-id] (:node-id (last nodes)))
                                 (assoc-in result [:projection :parameters 0 :source] {})]]
                (is (= :rejected
                       (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                        core mir [argument] candidate)))))))
          (when (every? #(= :accepted (:status %)) outcomes)
            (when equal-arms?
              (is (= true (get-in (first outcomes) [:execution :value])
                          (get-in (second outcomes) [:execution :value]))))
            (is (= :rejected
                   (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                    core mir [false] (first outcomes)))))
            (let [equalities (atom 0) hostile (proxy [Object] []
                                               (equals [_] (swap! equalities inc) true))]
              (is (true? (.equals hostile true)))
              (is (= 1 @equalities))
              (reset! equalities 0)
              (doseq [arguments [nil true 0 {} '(true) [] [true false]
                                 [nil] [0] [1] [:true] ["true"] [hostile] hostile [hostile true]]]
                (let [rejected (argument-run-observation core mir arguments)]
                  (is (= :rejected (:status rejected)))
                  (is (= "C11-TYPE" (get-in rejected [:diagnostics 0 :diagnostic-id])))
                  (is (= id (get-in rejected [:diagnostics 0 :value-id])))
                  (is (= [(get-in binder [:source :span]) (get-in binder [:source :generated-origin])]
                         [(get-in rejected [:diagnostics 0 :source-span])
                          (get-in rejected [:diagnostics 0 :origin-chain])]))
                  (is (nil? (:execution rejected)))))
              (is (zero? @equalities)))
            (doseq [[bad-core bad-mir]
                    [[(assoc-in core [:core-nodes 4 :attributes :value] nil) mir]
                     [(assoc-in core [:effect-facts id :direct] #{:io/write}) mir]
                     [(assoc-in core [:proof-records id :proof-id] nil) mir]
                     [core (assoc-in mir [:functions 'main :params] [])]
                     [core (assoc-in mir [:data-flow-graph :definitions id :operation-id] nil)]
                     [core (assoc-in mir [:data-flow-graph :edges 0 :from] (:node-id (last nodes)))]
                     [core (assoc-in mir [:control-flow-graph :join :incoming] [])]]]
              (let [original (invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
                    rejected (argument-run-observation bad-core bad-mir [nil])]
                (is (= :rejected (:status original) (:status rejected)))
                (is (= (:diagnostics original) (:diagnostics rejected)))
                (is (nil? (:execution rejected)))))
            (let [old (run core mir)]
              (is (= :rejected (:status old)))
              (is (nil? (:execution old))))
            (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir)))))))
      (finally (.delete same-arms)))))

(deftest sh13-executes-original-c6-bool-parameter-with-integer-arms
  ;; These are actual source-derived Integer results, not i64-retagged Core.
  ;; The existing argument API still accepts exactly one physical Bool.
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"]
    (doseq [[extension then-value else-value executable?]
            [["gravity" 1 0 true]
             ["qst" Long/MIN_VALUE Long/MAX_VALUE true]
             ["gravity" 9223372036854775808N 0 false]]]
      (let [source (str (.resolve root (str relative "." extension)))
            temporary (java.io.File/createTempFile "gravity-sh13-integer-arms-" (str "." extension))]
        (try
          (spit temporary (str/replace (slurp source) "(if flag true false)"
                                      (str "(if flag " then-value " " else-value ")")))
          (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
                verification (invoke 'sh12-verify-branch-join-mir core mir)
                nodes (:core-nodes core) binder (first nodes) id (:node-id binder)
                return-type (get-in nodes [6 :type])
                outcomes (when (= :passed (:status verification))
                           (mapv #(argument-run-observation core mir [%]) [true false]))]
            (is (= :accepted (:status raw)))
            (is (= :passed (:status upstream)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification)))
            (when (= :passed (:status verification))
              (is (= :gravity/integer (get-in return-type [:return])
                     (get-in mir [:functions 'main :returns])))
              (is (= [:gravity/bool :gravity/bool :gravity/bool
                      :gravity/integer :gravity/integer :gravity/integer return-type]
                     (mapv :type nodes)))
              (is (= {:value-id id :parameter-index 0
                      :block-id (get-in mir [:functions 'main :entry])}
                     (get-in mir [:data-flow-graph :definitions id])))
              (doseq [[argument result] (map vector [true false] outcomes)]
                (if executable?
                  (do
                    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
                    (when (= :accepted (:status result))
                      (is (= [argument] (:arguments result)))
                      (is (= (if argument then-value else-value) (get-in result [:execution :value])))
                      (is (= 506 (get-in result [:execution :fuel])))
                      (is (= [(get-in nodes [1 :node-id]) (get-in nodes [2 :node-id])
                              (get-in nodes [(if argument 3 4) :node-id])]
                             (vec (keep :operation (get-in result [:execution :trace])))))
                      (is (= [:gravity/bool :gravity/bool :gravity/integer :gravity/integer
                              :gravity/integer return-type]
                             (mapv :source-type (get-in result [:projection :operations]))))
                      (is (= [:bool :bool :i64 :i64 :i64 :i64]
                             (mapv :runtime-type (get-in result [:projection :operations]))))
                      (is (= :gravity/bool (get-in result [:projection :parameters 0 :source-type])))
                      (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                     core mir [argument] result))))
                      (doseq [candidate [(assoc result :arguments [(not argument)])
                                         (assoc-in result [:execution :value] false)
                                         (assoc-in result [:projection :operations 2 :source-type] :gravity.type/i64)
                                         (assoc-in result [:projection :parameters 0 :definition :operation-id] nil)]]
                        (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                        core mir [argument] candidate)))))))
                  (do
                    ;; A wide unselected arm must reject too: C11 retains
                    ;; mathematical Integer, but this interpreter is i64.
                    (is (= :rejected (:status result)))
                    (is (= :passed (get-in result [:upstream-verification :status])))
                    (is (= ["C11-VERIFY" :bounded-execution-subset]
                           ((juxt :diagnostic-id :missing-fact) (first (:diagnostics result)))))
                    (is (nil? (:execution result))))))
              (doseq [[bad-core bad-mir]
                      [[(assoc-in core [:core-nodes 4 :type] :gravity/bool) mir]
                       [(assoc-in core [:core-nodes 3 :attributes :value] false) mir]
                       [(assoc-in core [:core-nodes 6 :type :return] :gravity/bool) mir]
                       [(assoc-in core [:type-facts (get-in nodes [6 :node-id]) :return] :gravity/bool) mir]
                       [(assoc-in core [:effect-facts id :direct] #{:io/write}) mir]
                       [(assoc-in core [:proof-records (get-in nodes [4 :node-id]) :proof-id] nil) mir]
                       [(assoc-in core [:source-origin-table (get-in nodes [3 :source :origin-id])] nil) mir]
                       [core (assoc-in mir [:functions 'main :returns] :gravity/bool)]
                       [core (assoc-in mir [:data-flow-graph :definitions id :operation-id] nil)]
                       [core (assoc-in mir [:control-flow-graph :join :incoming] [])]]]
                (let [original (invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
                      rejected (argument-run-observation bad-core bad-mir [nil])]
                  (is (= :rejected (:status original) (:status rejected)))
                  (is (= (:diagnostics original) (:diagnostics rejected)))
                  (is (nil? (:execution rejected)))))
              (is (= :rejected (:status (run core mir))))
              (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))))
          (finally (.delete temporary)))))))

(deftest sh13-forwards-real-parameter-through-one-lexical-let
  ;; C6 owns all ten original nodes. Neither the lexical binder nor scope is
  ;; fabricated, erased, or replaced by an output-only interpreter shortcut.
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"]
    (doseq [[extension then-value else-value executable?]
            [["gravity" 1 0 true]
             ["qst" Long/MIN_VALUE Long/MAX_VALUE true]
             ["gravity" 9223372036854775808N 0 false]]]
      (let [source (str (.resolve root (str relative "." extension)))
            temporary (java.io.File/createTempFile "gravity-sh13-parameter-let-" (str "." extension))]
        (try
          (spit temporary (str/replace (slurp source) "(if flag true false)"
                            (str "(let [selected flag] (if selected " then-value " " else-value "))")))
          (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
                nodes (:core-nodes core) ids (mapv :node-id nodes)
                binder (get ids 0) local-binder (get ids 2) if-id (get ids 7) scope (get ids 8)
                verification (invoke 'sh12-verify-branch-join-mir core mir)]
            (is (= :accepted (:status raw)))
            (is (= :passed (:status upstream)))
            (is (= [:parameter :local :let-binding :local :truthy :literal :literal :if :let :function]
                   (mapv :source-operation nodes)))
            (is (= [[binder] [(get ids 1)] [local-binder] [local-binder if-id] [scope]]
                   (mapv #(get-in nodes [% :operands]) [1 2 3 8 9])))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification)))
            (when (= :passed (:status verification))
              (let [context (invoke 'sh13-branch-join-context core mir)
                    join-block (get-in context [:join :block-id])
                    outcomes (mapv #(argument-run-observation core mir [%]) [true false])]
                (is (= [4 1 1 3]
                       (mapv #(count (get-in context [% :instructions])) [:entry :then :else :join])))
                (is (= {:value-id binder :parameter-index 0 :block-id (get-in context [:entry :block-id])}
                       (get-in mir [:data-flow-graph :definitions binder])))
                (is (= {:value-id local-binder :operation-id local-binder
                        :block-id (get-in context [:entry :block-id])}
                       (get-in mir [:data-flow-graph :definitions local-binder])))
                (doseq [[argument result] (map vector [true false] outcomes)]
                  (if executable?
                    (do
                      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
                      (when (= :accepted (:status result))
                        (is (= (if argument then-value else-value) (get-in result [:execution :value])))
                        (is (= 503 (get-in result [:execution :fuel])))
                        (is (= [(get ids 1) local-binder (get ids 3) (get ids 4)
                                (get ids (if argument 5 6)) scope]
                               (vec (keep :operation (get-in result [:execution :trace])))))
                        (is (= scope (get-in result [:projection :return-value-id])))
                        (is (= if-id (get-in result [:projection :control-flow-graph :join :value-id])))
                        (is (= scope (:alias-of (last (get-in result [:projection :operations])))))
                        (is (= (get-in nodes [9 :type])
                               (:source-type (last (get-in result [:projection :operations])))))
                        (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                       core mir [argument] result))))
                        (doseq [candidate [(assoc result :arguments [(not argument)])
                                           (assoc-in result [:projection :return-value-id] if-id)
                                           (assoc-in result [:projection :operations 8 :alias-of] if-id)
                                           (assoc-in result [:execution :fuel] 506)
                                           (update-in result [:execution :trace]
                                                      #(vec (remove (fn [row] (= scope (:operation row))) %)))]]
                          (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                          core mir [argument] candidate)))))))
                    (do
                      (is (= :rejected (:status result)))
                      (is (= :passed (get-in result [:upstream-verification :status])))
                      (is (= ["C11-VERIFY" :bounded-execution-subset]
                             ((juxt :diagnostic-id :missing-fact) (first (:diagnostics result)))))
                      (is (nil? (:execution result))))))
                (doseq [[bad-core bad-mir]
                        [[(assoc-in core [:core-nodes 1 :operands] [local-binder]) mir]
                         [(assoc-in core [:core-nodes 3 :attributes :resolved-binding] binder) mir]
                         [(assoc-in core [:core-nodes 2 :attributes :name] 'flag) mir]
                         [(assoc-in core [:lexical-binding-records 1 :shadowed-binding] binder) mir]
                         [(assoc-in core [:core-nodes 8 :attributes :binding-count] 2) mir]
                         [(assoc-in core [:core-nodes 8 :operands] [if-id]) mir]
                         [(assoc-in core [:core-nodes 9 :operands] [if-id]) mir]
                         [(assoc-in core [:core-nodes 3 :path] [:functions 'main :body 1]) mir]
                         [(assoc-in core [:core-nodes 2 :attributes :source-form-id]
                                    (get-in nodes [0 :attributes :source-form-id])) mir]
                         [(assoc-in core [:core-nodes 2 :node-id] binder) mir]
                         [(assoc-in core [:type-facts local-binder :dependencies] []) mir]
                         [(assoc-in core [:core-nodes 6 :type] :gravity/bool) mir]
                         [(assoc-in core [:core-nodes 6 :attributes :value] false) mir]
                         [(assoc-in core [:effect-facts local-binder :direct] #{:io/write}) mir]
                         [(assoc-in core [:proof-records scope :proof-id] nil) mir]
                         [(assoc-in core [:source-origin-table (get-in nodes [3 :source :origin-id])] nil) mir]
                         [core (assoc-in mir [:data-flow-graph :definitions local-binder :block-id] join-block)]
                         [core (assoc-in mir [:data-flow-graph :definitions binder :operation-id] nil)]
                         [core (assoc-in mir [:control-flow-graph :join :incoming] [])]
                         [core (assoc-in mir [:functions 'main :blocks join-block :terminator :operands] [if-id])]]]
                  (let [original (invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
                        rejected (argument-run-observation bad-core bad-mir [nil])]
                    (is (= :rejected (:status original) (:status rejected)))
                    (is (= (:diagnostics original) (:diagnostics rejected)))
                    (is (nil? (:execution rejected)))))
                (is (= :rejected (:status (run core mir))))
                (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir)))))))
          (finally (.delete temporary)))))))

(deftest sh13-preserves-simple-bool-parameter-after-let-forwarding
  ;; One genuinely affected old family, not replay of the former three-source
  ;; 254-second selector: the common role validator and private frame changed.
  (let [path (str (.resolve @(existing 'root)
                   "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        {:keys [raw upstream core mir]} (current-parameter-pair path)
        nodes (:core-nodes core) binder (first nodes) id (:node-id binder)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream)))
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
    (is (= 7 (count nodes)))
    (is (= :gravity/bool (get-in mir [:functions 'main :returns])))
    (doseq [argument [true false]]
      (let [result (argument-run-observation core mir [argument])]
        (is (= :accepted (:status result)))
        (is (= argument (get-in result [:execution :value])))
        (is (= [argument] (:arguments result)))
        (is (= 506 (get-in result [:execution :fuel])))
        (is (= [(get-in nodes [1 :node-id]) (get-in nodes [2 :node-id])
                (get-in nodes [(if argument 3 4) :node-id])]
               (vec (keep :operation (get-in result [:execution :trace])))))
        (is (= (get-in core [:type-facts id :fact-id])
               (get-in result [:projection :parameters 0 :facts :type-fact-id])))
        (is (= (get-in nodes [6 :type])
               (:source-type (last (get-in result [:projection :operations])))))
        (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                       core mir [argument] result))))))))

(deftest sh13-forwards-bool-results-through-real-parameter-let
  ;; The same original ten-node lexical topology, not a new opcode, argument
  ;; API, patched Core or claim that the motivating compiler helpers ran.
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"]
    (doseq [[extension then-value else-value]
            [["gravity" true false] ["qst" false true]]]
      (let [source (str (.resolve root (str relative "." extension)))
            temporary (java.io.File/createTempFile "gravity-sh13-bool-let-" (str "." extension))]
        (try
          (spit temporary (str/replace (slurp source) "(if flag true false)"
                            (str "(let [selected flag] (if selected " then-value " " else-value "))")))
          (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
                nodes (:core-nodes core) ids (mapv :node-id nodes)
                parameter (get ids 0) binder (get ids 2) if-id (get ids 7) scope (get ids 8)
                verification (invoke 'sh12-verify-branch-join-mir core mir)]
            (is (= :accepted (:status raw)))
            (is (= :passed (:status upstream)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification)))
            (when (= :passed (:status verification))
              (is (= 10 (count nodes)))
              (is (= [:gravity/bool :gravity/bool :gravity/bool :gravity/bool]
                     (mapv #(get-in nodes [% :type]) [2 5 6 8])))
              (is (= :gravity/bool (get-in mir [:functions 'main :returns])))
              (is (= (get-in nodes [9 :type :return]) :gravity/bool))
              (is (= [binder if-id] (get-in nodes [8 :operands])))
              (doseq [argument [true false]]
                (let [result (argument-run-observation core mir [argument])]
                  (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
                  (when (= :accepted (:status result))
                    (is (= (if argument then-value else-value) (get-in result [:execution :value])))
                    (is (= [argument] (:arguments result)))
                    (is (= 503 (get-in result [:execution :fuel])))
                    (is (= [(get ids 1) binder (get ids 3) (get ids 4)
                            (get ids (if argument 5 6)) scope]
                           (vec (keep :operation (get-in result [:execution :trace])))))
                    (is (not-any? #(= parameter (:operation %)) (get-in result [:execution :trace])))
                    (is (= scope (get-in result [:projection :return-value-id])))
                    (is (= (get-in nodes [9 :type])
                           (:source-type (last (get-in result [:projection :operations])))))
                    (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                   core mir [argument] result))))
                    (doseq [candidate [(assoc result :arguments [(not argument)])
                                       (assoc-in result [:execution :value] 1)
                                       (assoc-in result [:projection :return-value-id] if-id)
                                       (assoc-in result [:projection :operations 8 :source-type :return] :gravity/integer)]]
                      (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                      core mir [argument] candidate))))))))
              (doseq [bad-core [(assoc-in core [:core-nodes 6 :attributes :value] 0)
                                (assoc-in core [:core-nodes 6 :attributes :value] nil)
                                (assoc-in core [:core-nodes 9 :type :return] :gravity/integer)
                                (assoc-in core [:type-facts scope :dependencies] [])
                                (assoc-in core [:lexical-binding-records 1 :shadowed-binding] parameter)]]
                (let [original (invoke 'sh12-verify-branch-join-mir bad-core mir)
                      result (argument-run-observation bad-core mir [true])]
                  (is (= :rejected (:status original) (:status result)))
                  (is (= (:diagnostics original) (:diagnostics result)))
                  (is (nil? (:execution result)))))
              (is (= :rejected (:status (run core mir))))
              (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))))
          (finally (.delete temporary)))))))

(deftest sh13-preserves-sequential-let-initializer-dependencies
  ;; Original C6 sequential visibility: the second initializer reads the
  ;; completed first binder, not the parameter or a fabricated instruction.
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"]
    (doseq [[extension then-value else-value]
            [["gravity" Long/MIN_VALUE Long/MAX_VALUE] ["qst" false true]]]
      (let [source (str (.resolve root (str relative "." extension)))
            temporary (java.io.File/createTempFile "gravity-sh13-sequential-let-" (str "." extension))]
        (try
          (spit temporary (str/replace (slurp source) "(if flag true false)"
                            (str "(let [selected flag forwarded selected] (if forwarded "
                                 then-value " " else-value "))")))
          (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
                nodes (:core-nodes core) ids (mapv :node-id nodes)
                parameter (get ids 0) first-binder (get ids 2) second-binder (get ids 4)
                if-id (get ids 9) scope (get ids 10)
                verification (invoke 'sh12-verify-branch-join-mir core mir)]
            (is (= :accepted (:status raw)))
            (is (= :passed (:status upstream)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification)))
            (when (= :passed (:status verification))
              (let [context (invoke 'sh13-branch-join-context core mir)
                    entry (get-in context [:entry :block-id]) join (get-in context [:join :block-id])]
                (is (= [:parameter :local :let-binding :local :let-binding :local
                        :truthy :literal :literal :if :let :function] (mapv :source-operation nodes)))
                (is (= [6 1 1 3] (mapv #(count (get-in context [% :instructions])) [:entry :then :else :join])))
                (is (= 12 (count (get-in mir [:data-flow-graph :values]))))
                (is (= [first-binder second-binder if-id] (get-in nodes [10 :operands])))
                (is (= [(get ids 1) (get ids 3)]
                       (mapv #(get-in nodes [% :operands 0]) [2 4])))
                (is (= [first-binder] (get-in nodes [3 :operands])))
                (is (= [second-binder] (get-in nodes [5 :operands])))
                (is (= [parameter first-binder second-binder]
                       (mapv :binding-node-id (:lexical-binding-records core))))
                (is (= {:value-id parameter :parameter-index 0 :block-id entry}
                       (get-in mir [:data-flow-graph :definitions parameter])))
                (doseq [binder [first-binder second-binder]]
                  (is (= {:value-id binder :operation-id binder :block-id entry}
                         (get-in mir [:data-flow-graph :definitions binder]))))
                (doseq [argument [true false]]
                  (let [result (argument-run-observation core mir [argument])]
                    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
                    (when (= :accepted (:status result))
                      (is (= (if argument then-value else-value) (get-in result [:execution :value])))
                      (is (= 501 (get-in result [:execution :fuel])))
                      (is (= [(get ids 1) first-binder (get ids 3) second-binder
                              (get ids 5) (get ids 6) (get ids (if argument 7 8)) scope]
                             (vec (keep :operation (get-in result [:execution :trace])))))
                      (is (= scope (get-in result [:projection :return-value-id])))
                      (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                     core mir [argument] result))))
                      (doseq [candidate [(assoc result :arguments [(not argument)])
                                         (assoc-in result [:execution :fuel] 503)
                                         (assoc-in result [:projection :return-value-id] if-id)
                                         (update-in result [:execution :trace]
                                                    #(vec (remove (fn [row] (= second-binder (:operation row))) %)))]]
                        (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                        core mir [argument] candidate))))))))
                (doseq [[bad-core bad-mir]
                        [[(assoc-in core [:core-nodes 1 :operands] [second-binder]) mir]
                         [(assoc-in core [:core-nodes 3 :operands] [parameter]) mir]
                         [(assoc-in core [:core-nodes 3 :attributes :resolved-binding] parameter) mir]
                         [(assoc-in core [:core-nodes 4 :attributes :name] 'selected) mir]
                         [(assoc-in core [:lexical-binding-records 2 :shadowed-binding] first-binder) mir]
                         [(update core :lexical-binding-records #(vec (reverse %))) mir]
                         [(assoc-in core [:core-nodes 5 :operands] [first-binder]) mir]
                         [(assoc-in core [:core-nodes 10 :attributes :binding-count] 1) mir]
                         [(assoc-in core [:core-nodes 10 :operands] [second-binder first-binder if-id]) mir]
                         [(assoc-in core [:core-nodes 4 :path] (get-in nodes [2 :path])) mir]
                         [(assoc-in core [:type-facts second-binder :dependencies] []) mir]
                         [(assoc-in core [:ownership-facts scope :source-value-ids] [second-binder]) mir]
                         [(assoc-in core [:effect-facts second-binder :direct] #{:io/write}) mir]
                         [(assoc-in core [:core-nodes 8 :attributes :value] nil) mir]
                         [(assoc-in core [:source-origin-table (get-in nodes [4 :source :origin-id])] nil) mir]
                         [core (assoc-in mir [:data-flow-graph :definitions second-binder :block-id] join)]
                         [core (assoc-in mir [:data-flow-graph :definitions second-binder :operation-id] first-binder)]
                         [core (assoc-in mir [:control-flow-graph :join :incoming] [])]]]
                  (let [original (invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
                        result (argument-run-observation bad-core bad-mir [nil])]
                    (is (= :rejected (:status original) (:status result)))
                    (is (= (:diagnostics original) (:diagnostics result)))
                    (is (nil? (:execution result)))))
                (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir)))))))
          (finally (.delete temporary)))))))

(deftest sh13-executes-real-nested-conditional-graphs
  ;; Two authentic producer conditionals require seven real blocks. No
  ;; four-block projection, fabricated phi or source-authority flag is used.
  (let [root @(existing 'root)
        relative "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source"]
    (doseq [[extension nested-side inner-condition]
            [["gravity" :then false] ["qst" :else true]]]
      (let [source (str (.resolve root (str relative "." extension)))
            temporary (java.io.File/createTempFile "gravity-sh13-nested-if-" (str "." extension))
            expression (if (= nested-side :then)
                         "(if flag (if false 11 22) 33)" "(if flag 11 (if true 22 33))")]
        (try
          (spit temporary (str/replace (slurp source) "(if flag true false)" expression))
          (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
                nodes (:core-nodes core) ids (mapv :node-id nodes)
                verification-start (System/nanoTime)
                verification (invoke 'sh12-verify-branch-join-mir core mir)
                verification-end (System/nanoTime)]
            (println "nested-if fresh-c11-verification(ns)" extension (- verification-end verification-start))
            (when (not= :passed (:status verification))
              ;; Failure-only observations from THIS already-built source
              ;; product; never another producer run or alternate acceptance.
              (let [view (invoke 'sh12-c6p-role-view core)]
                (println "nested-if original-product predicates"
                         {:lineage (invoke 'sh12-c6p-lineage-valid? core)
                          :producer (invoke 'sh12-c6p-producer-metadata-valid? core)
                          :shape (invoke 'sh12-c6p-shape-valid? core view)
                          :nodes (invoke 'sh12-c6p-role-nodes-valid? core 0)
                          :declaration (invoke 'sh12-c6p-declaration-valid? core)
                          :node-problem (invoke 'sh12-c6p-nodes-problem core nodes 0)
                          :graphs (invoke 'sh12-c6p-graphs-valid? core view)})
                (println "nested-if original-node rows"
                         (mapv #(select-keys % [:source-operation :kind :path :plan-node? :plan-depth :type :attributes]) nodes))))
            (is (= :accepted (:status raw)))
            (is (= :passed (:status upstream)))
            (is (= :passed (:status verification)) (pr-str (:diagnostics verification)))
            (when (= :passed (:status verification))
              (let [function (get-in mir [:functions 'main]) blocks (:blocks function)
                    e (:entry function) outer (get blocks e)
                    [t f] (get-in outer [:terminator :successors])
                    n (if (= nested-side :then) t f) o (if (= nested-side :then) f t)
                    [a b] (get-in blocks [n :terminator :successors])
                    k (get-in blocks [n :terminator :facts :join]) j (get-in mir [:control-flow-graph :join :block-id])
                    order (if (= nested-side :then) [e n a b k o j] [e o n a b k j])
                    inner-if-index (if (= nested-side :then) 7 8)
                    inner-literal-index (if (= nested-side :then) 3 4)
                    inner-truthy-index (+ inner-literal-index 1)
                    inner-then-index (+ inner-literal-index 2) inner-else-index (+ inner-literal-index 3)
                    other-index (if (= nested-side :then) 8 3)
                    operations (vec (mapcat #(get-in blocks [% :instructions]) order))
                    dominators {e [e] n [e n] a [e n a] b [e n b] k [e n k] o [e o] j [e j]}
                    unit-context (invoke 'sh13-c6p-nested-context core mir)]
                ;; UNIT runtime-gate checks on an actual derived context.
                ;; Mutated contexts are never submitted as public verified
                ;; executions or evidence of passed upstream source binding.
                (is (true? (invoke 'sh13-c6p-nested-shape-valid? core mir unit-context)))
                (doseq [role [:inner-then :inner-else :other]]
                  (is (false? (invoke 'sh13-c6p-nested-shape-valid? core mir
                                      (assoc-in unit-context [role :instructions 0 :constant-payload :value]
                                                9223372036854775808N))))
                  (doseq [endpoint [Long/MIN_VALUE Long/MAX_VALUE]]
                    (is (true? (invoke 'sh13-c6p-nested-shape-valid? core mir
                                       (assoc-in unit-context [role :instructions 0 :constant-payload :value]
                                                 endpoint))))))
                (doseq [payload [nil 0 1 "false"]]
                  (is (false? (invoke 'sh13-c6p-nested-shape-valid? core mir
                                      (assoc-in unit-context [:nested :instructions 0 :constant-payload :value] payload)))))
                (is (= 11 (count nodes) (count (get-in mir [:data-flow-graph :values]))))
                (is (= 7 (count blocks)))
                (is (= #{e n a b k o j} (set (keys blocks))))
                (is (= [2 2 1 1 1 1 2] (mapv #(count (get-in blocks [% :instructions])) [e n a b k o j])))
                (is (= 10 (count operations)))
                (is (= (set (rest ids)) (set (map :op-id operations))))
                (is (= 8 (count (get-in mir [:control-flow-graph :edges]))))
                (is (= dominators (get-in mir [:control-flow-graph :dominance])))
                (is (= [a b] (get-in blocks [k :predecessors])))
                (is (= (if (= nested-side :then) [k o] [o k]) (get-in blocks [j :predecessors])))
                (is (= (get ids inner-if-index) (get-in blocks [k :instructions 0 :result])))
                (is (= (get ids 9) (get-in blocks [j :instructions 0 :result])))
                (is (= [(get ids 9)] (get-in blocks [j :terminator :operands])))
                (is (= {:value-id (get ids 0) :parameter-index 0 :block-id e}
                       (get-in mir [:data-flow-graph :definitions (get ids 0)])))
                (doseq [id [k j]]
                  (is (= [] (get-in blocks [id :arguments])))
                  (is (= :conditional-join (get-in blocks [id :instructions 0 :opcode]))))
                (is (= 2 (get-in mir [:scope :maximum-conditionals])
                       (get-in mir [:pass-contract :scope :maximum-conditionals])))
                (is (= :closed-nested-conditional-branch-join (:scope verification)))
                (is (= :single-function-nested-diamonds (first (:checks verification))))
                (is (= :gravity/integer (:returns function)))
                (doseq [operation operations]
                  (is (= (:source (first (filter #(= (:op-id operation) (:node-id %)) nodes)))
                         (:source operation))))
                (doseq [argument [true false]]
                  (let [nested? (= argument (= nested-side :then))
                        result (argument-run-observation core mir [argument])
                        selected-index (if inner-condition inner-then-index inner-else-index)
                        expected-ops (if nested? [(get ids 1) (get ids 2) (get ids inner-literal-index)
                                                  (get ids inner-truthy-index) (get ids selected-index)]
                                               [(get ids 1) (get ids 2) (get ids other-index)])
                        expected-value (if (= nested-side :then) (if argument 22 33) (if argument 11 22))]
                    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
                    (when (= :accepted (:status result))
                      (is (= expected-value (get-in result [:execution :value])))
                      (is (= (if nested? 502 506) (get-in result [:execution :fuel])))
                      (is (= expected-ops (vec (keep :operation (get-in result [:execution :trace])))))
                      (is (= 10 (count (get-in result [:projection :operations]))))
                      (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                     core mir [argument] result))))
                      (doseq [candidate [(assoc result :arguments [(not argument)])
                                         (assoc-in result [:execution :value] -1)
                                         (assoc-in result [:execution :fuel] 512)
                                         (assoc-in result [:upstream-verification :scope] :single-conditional-branch-join)
                                         (assoc-in result [:upstream-verification :checks 0] :single-function-diamond)
                                         (update-in result [:projection :operations] pop)]]
                        (is (= :rejected (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                                        core mir [argument] candidate))))))))
                (doseq [[bad-core bad-mir]
                        [[core (assoc-in mir [:functions 'main :blocks k :predecessors] [b a])]
                         [core (assoc-in mir [:scope :maximum-conditionals] 1)]
                         [core (assoc-in mir [:pass-contract :scope :maximum-conditionals] 1)]
                         [core (assoc-in mir [:functions 'main :blocks a :terminator :operands] [(get ids other-index)])]
                         [core (assoc-in mir [:functions 'main :blocks a :terminator :successors] [j])]
                         [core (assoc-in mir [:functions 'main :blocks n :terminator :operands] [(get ids 2)])]
                         [core (assoc-in mir [:functions 'main :blocks k :instructions 0 :operands 1] (get ids other-index))]
                         [core (assoc-in mir [:control-flow-graph :dominance k] [e k])]
                         [core (assoc-in mir [:control-flow-graph :join :incoming 0 :value] (get ids inner-then-index))]
                         [core (assoc-in mir [:data-flow-graph :definitions (get ids inner-if-index) :block-id] e)]
                         [core (assoc-in mir [:data-flow-graph :definitions (get ids 0) :operation-id] nil)]
                         [core (update-in mir [:functions 'main :blocks] dissoc b)]
                         [core (update-in mir [:functions 'main :blocks e :instructions] into (get-in blocks [n :instructions]))]
                         [(assoc-in core [:core-nodes inner-literal-index :attributes :value] nil) mir]
                         [(assoc-in core [:core-nodes inner-else-index :attributes :value] false) mir]
                         [(assoc-in core [:effect-facts (get ids inner-if-index) :direct] #{:io/write}) mir]
                         [(assoc-in core [:source-origin-table (get-in nodes [inner-truthy-index :source :origin-id])] nil) mir]]]
                  (let [original (invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
                        result (argument-run-observation bad-core bad-mir [nil])]
                    (is (= :rejected (:status original) (:status result)))
                    (is (= (:diagnostics original) (:diagnostics result)))
                    (is (nil? (:execution result)))))
                (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
                (println "nested-if execution-recompute-and-tamper-checks(ns)" extension
                         (- (System/nanoTime) verification-end)))))
          (finally (.delete temporary)))))
    (let [core (checked ".gravity") mir (build core)]
      (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
      (doseq [candidate [(assoc-in mir [:scope :maximum-conditionals] 2)
                         (assoc-in mir [:pass-contract :scope :maximum-conditionals] 2)]]
        (is (= :rejected (:status (invoke 'sh12-verify-branch-join-mir core candidate))))))))

(deftest sh13-rejects-foreign-nested-join-operand-index-before-callback
  (let [source (str (.resolve @(existing 'root)
                     "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/bool_parameter_source.gravity"))
        temporary (java.io.File/createTempFile "gravity-sh13-nested-index-" ".gravity")]
    (try
      (spit temporary (str/replace (slurp source) "(if flag true false)" "(if flag (if false 11 22) 33)"))
      (let [{:keys [raw upstream core mir]} (current-parameter-pair (.getAbsolutePath temporary))
            inner (get-in core [:core-nodes 7 :node-id])
            edges (get-in mir [:data-flow-graph :edges])
            index (first (keep-indexed #(when (= [inner 1] [(:consumer-id %2) (:operand-index %2)]) %1) edges))
            callbacks (atom 0)
            hostile (proxy [Object] []
                      (hashCode [] (swap! callbacks inc) 7)
                      (equals [_] (swap! callbacks inc) false))]
        (is (= :accepted (:status raw)))
        (is (= :passed (:status upstream)))
        (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
        (is (integer? index))
        (doseq [argument [true false]]
          (let [result (argument-run-observation core mir [argument])]
            (is (= :accepted (:status result)))
            (is (= (if argument 22 33) (get-in result [:execution :value])))
            (is (= (if argument 502 506) (get-in result [:execution :fuel])))
            (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments
                                           core mir [argument] result))))))
        ;; The hostile methods are live, then the original public verifier
        ;; and consumer must reject this same edge without reaching them.
        (.hashCode hostile)
        (is (= 1 @callbacks))
        (reset! callbacks 0)
        (doseq [bad-index [hostile nil "1" :index 1.0 [] {} Long/MIN_VALUE Long/MAX_VALUE 3]]
          (let [candidate (assoc-in mir [:data-flow-graph :edges index :operand-index] bad-index)
                report (invoke 'sh12-verify-branch-join-mir core candidate)
                result (argument-run-observation core candidate [true])]
            (is (= :rejected (:status report) (:status result)))
            (is (= "C11-DOMINANCE" (get-in report [:diagnostics 0 :diagnostic-id])))
            (is (= (:diagnostics report) (:diagnostics result)))
            (is (nil? (:execution result)))
            (is (zero? @callbacks)))))
      (finally (.delete temporary)))))

(defn- scalar-core [core condition type then-value else-value]
  (let [values [condition then-value else-value nil nil]
        types [:gravity.type/bool type type type type]]
    (reduce (fn [result index]
              (let [id (get-in core [:core-nodes index :node-id])]
                (-> result
                    (assoc-in [:core-nodes index :type] (types index))
                    (assoc-in [:core-nodes index :attributes :value]
                              (values index))
                    (assoc-in [:type-facts id :type] (types index))
                    (assoc-in [:type-facts id :type-id] (types index)))))
            core (range 5))))

(defn- coordinates [core mir]
  (let [function (get-in mir [:functions (:entrypoint core)])
        entry (:entry function)
        [then-id else-id] (get-in function [:blocks entry :successors])]
    {:function (:fn-id function) :entry entry :then then-id :else else-id
     :join (get-in mir [:control-flow-graph :join :block-id])}))

(deftest sh13-original-branch-join-interface-is-explicit
  (let [core (checked ".gravity") mir (build core) result (run core mir)
        ;; The raw seed plan intentionally projects only six module fields,
        ;; not exports. Check the ORIGINAL source declaration, independently
        ;; of the actual compiled function arities below; no admission claim.
        source-header (binding [*read-eval* false]
                        (read-string (slurp (str (.resolve @(existing 'root) (existing 'c11-source))))))
        declared-exports (some #(when (and (seq? %) (= :exports (first %))) (second %))
                               (drop 2 source-header))]
    (is (= [2 3 3 4]
           (mapv #(get-in @(existing 'c11-plan) [:functions % :arity])
                 '[sh13-run-branch-join-mir sh13-verify-branch-join-execution
                   sh13-run-branch-join-mir-with-arguments
                   sh13-verify-branch-join-execution-with-arguments])))
    (doseq [name '[sh13-run-branch-join-mir-with-arguments
                  sh13-verify-branch-join-execution-with-arguments]]
      (is (contains? (set declared-exports) name)))
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= :meta (:profile mir)))
    (is (= :jvm (:source-target mir)))
    (is (= :gravity/sh13-branch-join-execution (:artifact result)))
    (is (= 1 (:schema-version result)))
    (is (= #{:artifact :schema-version :status :input-bindings
             :upstream-verification :projection :execution :diagnostics
             :clojure-seed-boundary? :self-hosted? :nonclaims}
           (set (keys result))))
    (is (= {:checked-core-artifact-id (:artifact-id core)
            :mir-module-id (:module-id mir)} (:input-bindings result)))
    (is (= :passed (get-in result [:upstream-verification :status])))
    (is (= :accepted (:status result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (some #{:fresh-c10-ingress} (:nonclaims result)))
    (is (= :passed (:status (verify core mir result))))
    (let [parameter-only (argument-run-observation core mir [])]
      (is (= :rejected (:status parameter-only)))
      (is (= :bounded-execution-subset (get-in parameter-only [:diagnostics 0 :missing-fact])))
      (is (nil? (:execution parameter-only))))))

(deftest sh13-executes-original-verified-diamonds
  (doseq [extension [".gravity" ".qst"]
          [condition type left right expected]
          [[true :gravity.type/i64 11 22 11]
           [false :gravity.type/i64 11 22 22]
           [true :gravity.type/i64 -9223372036854775808
            9223372036854775807 -9223372036854775808]
           [false :gravity.type/i64 -9223372036854775808
            9223372036854775807 9223372036854775807]
           [true :gravity.type/bool false true false]
           [false :gravity.type/bool false true true]]]
    (testing [extension condition type]
      (let [core (scalar-core (checked extension) condition type left right)
            mir (build core) result (run core mir)
            {:keys [function entry then else join]} (coordinates core mir)
            rows (get-in result [:projection :operations])
            ids (mapv :node-id (:core-nodes core))
            selected (if condition then else)
            selected-id (ids (if condition 1 2))
            trace (get-in result [:execution :trace])]
        (is (= :accepted (:status result)))
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :returned (get-in result [:execution :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= 507 (get-in result [:execution :fuel])))
        (is (= [{:function function :event :enter :depth 0}
                {:function function :operation (ids 0) :opcode :const}
                {:function function :block entry :terminator :conditional-branch}
                {:function function :operation selected-id :opcode :const}
                {:function function :block selected :terminator :branch}
                {:function function :block join :terminator :return}] trace))
        (is (= ids (mapv :operation-id rows)))
        (is (= [:literal :literal :literal :edge-join :function-result-alias]
               (mapv :role rows)))
        (is (= (ids 3) (:alias-of (rows 4))))
        (is (= (ids 3) (get-in result [:projection :return-value-id])))
        (is (= (:control-flow-graph mir)
               (get-in result [:projection :control-flow-graph])))
        (doseq [index (range 5)]
          (let [node ((:core-nodes core) index)
                op (first (filter #(= (ids index) (:op-id %))
                                  (mapcat :instructions
                                          (vals (get-in mir [:functions
                                                            (:entrypoint core)
                                                            :blocks])))))]
            (is (= (:source node) (:source (rows index))))
            (is (= (:facts op) (:facts (rows index))))))
        (is (= :passed (:status (verify core mir result))))))))

(deftest sh13-rejects-original-mir-tampering-before-execution
  (let [core (checked ".gravity") mir (build core)
        {:keys [entry then join]} (coordinates core mir)
        fp [:functions (:entrypoint core)]
        ep (into fp [:blocks entry :instructions 0])
        mutants [(assoc-in mir [:control-flow-graph :join :incoming 0 :value]
                           "forged-value")
                 (assoc-in mir [:control-flow-graph :edges 0 :to] join)
                 (assoc-in mir (conj ep :constant-payload :value) false)
                 (assoc-in mir (conj ep :type) :gravity.type/i64)
                 (assoc-in mir (conj ep :effects) #{:io/write})
                 (assoc-in mir (into ep [:facts :safety-outcome-id]) "forged")
                 (assoc-in mir (into ep [:facts :ownership-fact-id]) "forged")
                 (assoc-in mir (into ep [:facts :capability-fact-id]) "forged")
                 (assoc-in mir (into ep [:source :span :start]) 99)
                 (assoc-in mir [:data-flow-graph :definitions "forged"] {})
                 (assoc-in mir (into fp [:blocks then :terminator :operands]) [])]]
    (doseq [mutant mutants]
      (let [original (invoke 'sh12-verify-branch-join-mir core mutant)
            result (run core mutant)]
        (is (= :rejected (:status original)))
        (is (= :rejected (:status result)))
        (is (= original (:upstream-verification result)))
        (is (= (:diagnostics original) (:diagnostics result)))
        (is (nil? (:projection result)))
        (is (nil? (:execution result)))
        (is (= :passed (:status (verify core mutant result))))))))

(deftest sh13-execution-subset-and-result-substitution-fail-closed
  ;; The NEW literal gate must reject arbitrary values without hashing or
  ;; calling equality on them. ArrayMap values/vectors do not hash this object
  ;; while constructing these direct-helper, supplied-DATA controls.
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality invoked" {})))
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash invoked" {}))))]
    (doseq [value [opaque [opaque] {:value opaque}]]
      (is (false? (invoke 'sh13-branch-join-literal-valid?
                          {:opcode :constant :source-operation :literal
                           :operands [] :type :gravity.type/bool
                           :constant-payload {:present? true :value value}}
                          :gravity.type/bool)))
      (is (empty? @callbacks))))
  (let [base (checked ".gravity")
        invalids [(scalar-core base true :gravity.type/i64 11 "unselected")
                  (scalar-core base false :gravity.type/i64 [] 22)
                  (scalar-core base true :gravity.type/i64 11 9223372036854775808N)
                  (scalar-core base false :gravity.type/bool nil true)
                  (assoc-in base [:core-nodes 1 :source-operation] :quote)]]
    (doseq [core invalids]
      (let [mir (build core) result (run core mir)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :rejected (:status result)))
        (is (= :bounded-execution-subset
               (get-in result [:diagnostics 0 :missing-fact])))
        (is (nil? (:execution result)))
        (is (nil? (:projection result)))
        (is (= :passed (:status (verify core mir result))))))
    (let [core ((existing 'invoke)
                (get (existing 'rejected-plans) ".gravity")
                'sh12-runtime-checked-core [base])
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (= :bounded-execution-subset
             (get-in result [:diagnostics 0 :missing-fact])))
      (is (nil? (:execution result)))
      (is (nil? (:projection result))))
    (let [mir (build base) result (run base mir)
          other (scalar-core base true :gravity.type/i64 11 23)
          other-mir (build other)]
      (doseq [candidate [(assoc result :status :rejected)
                         (assoc-in result [:execution :value] 22)
                         (assoc-in result [:projection :operations 2
                                          :constant-payload :value] 23)
                         (assoc-in result [:projection :operations 0 :source] {})]]
        (let [report (verify base mir candidate)]
          (is (= :rejected (:status report)))
          (is (= "C11-VERIFY" (get-in report [:diagnostics 0 :diagnostic-id])))
          (is (= :execution-result-substitution
                 (get-in report [:diagnostics 0 :missing-fact])))))
      ;; Same declared IDs and selected result do not authenticate new input.
      (is (= (:artifact-id base) (:artifact-id other)))
      (is (= :rejected (:status (verify other other-mir result)))))))

(defn- with-integer-comparison [extension base placement operation left right]
  (let [original (:core-nodes base)
        if-node (first (filter #(= :if (:source-operation %)) original))
        target-id ((:operands if-node) ({:condition 0 :then 1 :else 2} placement))
        target-index (.indexOf (mapv :node-id original) target-id)
        target (original target-index)
        start (get-in target [:source :span :start])
        make (fn [name args]
               ((existing 'invoke) (get (existing 'accepted-plans) extension)
                name args))
        ids [(str "sh13:" (name placement) ":left")
             (str "sh13:" (name placement) ":right")]
        nodes (mapv (fn [id value index]
                      (make 'sh12-branch-join-node
                            [id (conj (:path target) index) :literal []
                             :gravity.type/i64 value (+ start index)
                             (+ start index 1)]))
                    ids [left right] [0 1])
        comparison (make 'sh12-branch-join-node
                         [target-id (:path target) operation ids
                          :gravity.type/bool nil start
                          (get-in target [:source :span :end])])]
    (reduce (fn [core node]
              (let [id (:node-id node)]
                (-> core
                    (assoc-in [:type-facts id]
                              (make 'sh12-branch-join-type-fact
                                    [id :gravity.type/i64]))
                    (assoc-in [:effect-facts id]
                              (make 'sh12-branch-join-effect-fact [id]))
                    (assoc-in [:ownership-facts id]
                              (make 'sh12-branch-join-ownership-fact [id]))
                    (assoc-in [:capability-facts id]
                              (make 'sh12-branch-join-capability-fact [id]))
                    (assoc-in [:safety-facts id] (:safety node))
                    (assoc-in [:profile-facts id]
                              {:profile :meta :target :jvm}))))
            (-> base
                (assoc :core-nodes
                       (into (into (subvec original 0 target-index) nodes)
                             (cons comparison (subvec original (inc target-index)))))
                (assoc-in [:safety-facts target-id] (:safety comparison)))
            nodes)))

(defn- integer-condition-core [extension operation left right type]
  (with-integer-comparison
    extension
    (scalar-core (checked extension) true type
                 (if (= type :gravity.type/bool) false 11)
                 (if (= type :gravity.type/bool) true 22))
    :condition operation left right))

(defn- check-integer-condition [operation role cases]
  (doseq [extension [".gravity" ".qst"]]
    (doseq [[left right type condition expected] cases]
      (let [core (integer-condition-core extension operation left right type)
            mir (build core) result (run core mir)
            ids (mapv :node-id (:core-nodes core))
            rows (get-in result [:projection :operations])]
        (is (= :accepted (:status result)))
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= 505 (get-in result [:execution :fuel])))
        (is (= (conj (subvec ids 0 3) (ids (if condition 3 4)))
               (mapv :operation (filter :operation (get-in result [:execution :trace])))))
        (is (= ids (mapv :operation-id rows)))
        (is (= (mapv :source (:core-nodes core)) (mapv :source rows)))
        (is (= [:literal :literal role :literal :literal
                :edge-join :function-result-alias] (mapv :role rows)))
        (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
               (mapv #(get-in % [:facts :type-fact-id]) rows)))
        (is (= :passed (:status (verify core mir result))))))
    (let [core (integer-condition-core extension operation 0 1 :gravity.type/i64)
          id (get-in core [:core-nodes 0 :node-id])
          right-id (get-in core [:core-nodes 1 :node-id])]
      (doseq [bad [(assoc-in core [:core-nodes 0 :attributes :value] nil)
                  (assoc-in core [:core-nodes 1 :attributes :value] 9223372036854775808N)
                  (assoc-in core [:core-nodes 2 :operands] [id])
                  (assoc-in core [:core-nodes 2 :operands] [right-id id])
                  (-> core (assoc-in [:core-nodes 0 :type] :gravity.type/bool)
                      (assoc-in [:core-nodes 0 :attributes :value] true)
                      (assoc-in [:type-facts id :type] :gravity.type/bool)
                      (assoc-in [:type-facts id :type-id] :gravity.type/bool))
                  (assoc-in core [:core-nodes 2 :operands] [id right-id id])
                  (assoc-in core [:core-nodes 0 :attributes :value] -9223372036854775809N)
                  (integer-condition-core extension :integer-gte 0 1 :gravity.type/i64)]]
        (let [mir (build bad) result (run bad mir)]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :rejected (:status result)))
          (is (= :bounded-execution-subset
                 (get-in result [:diagnostics 0 :missing-fact])))
          (is (nil? (:execution result)))))
      (let [mir (build core) result (run core mir)
            entry (get-in mir [:control-flow-graph :entry])
            path [:functions (:entrypoint core) :blocks entry :instructions 2]]
        (doseq [bad [(assoc-in mir (conj path :operands) [id id])
                    (assoc-in mir (conj path :source-operation) :literal)
                    (assoc-in mir (conj path :constant-payload) {:present? true :value false})
                    (assoc-in mir (conj path :type) :gravity.type/i64)]]
          (let [upstream (invoke 'sh12-verify-branch-join-mir core bad)
                rejection (run core bad)]
            (is (= :rejected (:status rejection)))
            (is (= (:diagnostics upstream) (:diagnostics rejection)))
            (is (nil? (:execution rejection)))))
        (doseq [candidate [(assoc-in result [:execution :value] :forged)
                           (assoc-in result [:projection :operations 2 :role] :forged)]]
          (is (= :rejected (:status (verify core mir candidate)))))))))

(deftest sh13-executes-typed-integer-equality-conditions
  (check-integer-condition
   :integer-eq :integer-equality
   [[0 0 :gravity.type/i64 true 11] [0 1 :gravity.type/i64 false 22]
    [-9223372036854775808 -9223372036854775808 :gravity.type/i64 true 11]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 false 22]
    [0 0 :gravity.type/bool true false] [0 1 :gravity.type/bool false true]]))

(deftest sh13-executes-typed-integer-less-than-conditions
  (check-integer-condition
   :integer-lt :integer-less-than
   [[0 1 :gravity.type/i64 true 11] [1 0 :gravity.type/i64 false 22]
    [0 0 :gravity.type/i64 false 22] [-1 0 :gravity.type/i64 true 11]
    [0 -1 :gravity.type/i64 false 22]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 true 11]
    [9223372036854775807 -9223372036854775808 :gravity.type/i64 false 22]
    [-9223372036854775808 -9223372036854775808 :gravity.type/i64 false 22]
    [0 1 :gravity.type/bool true false] [1 0 :gravity.type/bool false true]]))

(deftest sh13-executes-integer-less-than-or-equal-entry-conditions
  (check-integer-condition
   :integer-lte :integer-less-than-or-equal
   [[0 1 :gravity.type/i64 true 11] [1 0 :gravity.type/i64 false 22]
    [0 0 :gravity.type/i64 true 11] [-1 0 :gravity.type/i64 true 11]
    [0 -1 :gravity.type/i64 false 22]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 true 11]
    [9223372036854775807 -9223372036854775808 :gravity.type/i64 false 22]
    [-9223372036854775808 -9223372036854775808 :gravity.type/i64 true 11]
    [9223372036854775807 9223372036854775807 :gravity.type/bool true false]
    [1 0 :gravity.type/bool false true]])
  (doseq [extension [".gravity" ".qst"]]
    (doseq [[type a b] [[:gravity.type/keyword :phase/then :phase/else]
                       [:gravity.type/nil nil nil]]
            [left right expected] [[0 0 a] [1 0 b]]]
      (let [core (with-integer-comparison extension
                   (scalar-core (checked extension) true type a b)
                   :condition :integer-lte left right)
            mir (build core) result (run core mir)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :accepted (:status result)))
        (is (= expected (get-in result [:execution :value])))
        (is (= 505 (get-in result [:execution :fuel])))
        (is (= :passed (:status (verify core mir result))))))
    ;; Entry <= still does not compose with Eq/Lt arms.
    (doseq [placement [:then :else] operation [:integer-eq :integer-lt]]
      (let [base (scalar-core (checked extension) true :gravity.type/bool false true)
            arm (with-integer-comparison extension base placement operation 0 0)
            core (with-integer-comparison extension arm :condition :integer-lte 0 0)
            mir (build core) result (run core mir)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :rejected (:status result)))
        (is (= :bounded-execution-subset (get-in result [:diagnostics 0 :missing-fact])))
        (is (nil? (:execution result)))))
    (let [core (integer-condition-core extension :integer-lte 1 0 :gravity.type/i64)
          mir (build core) result (run core mir)
          {:keys [function then join]} (coordinates core mir)
          incoming [:functions function :blocks join :instructions 0 :operands]]
      ;; Both arms are checked, including the unselected then literal.
      (let [bad (assoc-in core [:core-nodes 3 :attributes :value] :not-an-integer)
            rejected (run bad (build bad))]
        (is (= :passed (get-in rejected [:upstream-verification :status])))
        (is (= :rejected (:status rejected)))
        (is (nil? (:execution rejected))))
      (doseq [bad [(assoc-in mir (conj incoming 1) :missing)
                  (assoc-in mir [:functions function :blocks then :instructions 0 :source :span] nil)]]
        (let [upstream (invoke 'sh12-verify-branch-join-mir core bad)
              rejected (run core bad)]
          (is (= :rejected (:status rejected)))
          (is (= (:diagnostics upstream) (:diagnostics rejected)))
          (is (nil? (:execution rejected)))))
      (is (= :rejected (:status (verify core mir (assoc-in result [:execution :fuel] 506))))))))

(defn- check-selected-comparison-arm [operation role cases]
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (letfn [(base [condition]
              (scalar-core (checked extension) condition
                           :gravity.type/bool false true))
            (arm [condition left right operation]
              (with-integer-comparison
                extension (base condition) placement operation left right))]
      (doseq [condition [true false]
              [left right comparison-result] cases]
        (let [core (arm condition left right operation)
              mir (build core) result (run core mir)
              nodes (:core-nodes core) ids (mapv :node-id nodes)
              if-node (first (filter #(= :if (:source-operation %)) nodes))
              computed-id ((:operands if-node) (if (= placement :then) 1 2))
              comparison (first (filter #(= computed-id (:node-id %)) nodes))
              selected? (= placement (if condition :then :else))
              other-id ((:operands if-node) (if condition 1 2))
              operation-ids (if selected?
                              (into [(ids 0)] (conj (:operands comparison) computed-id))
                              [(ids 0) other-id])
              rows (get-in result [:projection :operations])
              coordinates (coordinates core mir)
              incoming (first (filter #(= (coordinates placement) (:predecessor %))
                                      (get-in mir [:control-flow-graph :join :incoming])))]
          (is (= :accepted (:status result)))
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= (if selected? comparison-result (not condition))
                 (get-in result [:execution :value])))
          (is (= (if selected? 505 507) (get-in result [:execution :fuel])))
          (is (= operation-ids
                 (mapv :operation (filter :operation (get-in result [:execution :trace])))))
          (is (= ids (mapv :operation-id rows)))
          (is (= 7 (count rows)))
          (is (= (mapv :source nodes) (mapv :source rows)))
          (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
                 (mapv #(get-in % [:facts :type-fact-id]) rows)))
          (is (= role (:role (get rows (.indexOf ids computed-id)))))
          (is (= computed-id (:value incoming)))
          (is (= (:control-flow-graph mir)
                 (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))))
      ;; All these invalid computations are in the UNSELECTED arm. The full
      ;; typed subset is checked before entering the interpreter at all.
      (let [condition (= placement :else)
            core (arm condition 0 1 operation)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            comparison (first (filter #(= operation (:source-operation %)) nodes))
            id (first (:operands comparison))
            left-index (.indexOf ids id)
            comparison-index (.indexOf ids (:node-id comparison))]
        (doseq [bad [(assoc-in core [:core-nodes left-index :attributes :value] nil)
                    (assoc-in core [:core-nodes left-index :attributes :value] 9223372036854775808N)
                    (assoc-in core [:core-nodes comparison-index :operands] [id])
                    (-> core (assoc-in [:core-nodes left-index :type] :gravity.type/bool)
                        (assoc-in [:core-nodes left-index :attributes :value] true)
                        (assoc-in [:type-facts id :type] :gravity.type/bool)
                        (assoc-in [:type-facts id :type-id] :gravity.type/bool))
                    (with-integer-comparison
                      extension
                      (with-integer-comparison extension core
                        (if (= placement :then) :else :then) :integer-lt 0 1)
                      :condition :integer-lt 0 1)
                    (with-integer-comparison extension core
                      (if (= placement :then) :else :then) :integer-lt 0 1)
                    (arm condition 0 1 :integer-gte)]]
          (let [mir (build bad) rejection (run bad mir)]
            (is (= :passed (get-in rejection [:upstream-verification :status])))
            (is (= :rejected (:status rejection)))
            (is (= :bounded-execution-subset
                   (get-in rejection [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution rejection)))))
        (let [mir (build core) result (run core mir)
              block-id ((coordinates core mir) placement)
              path [:functions (:entrypoint core) :blocks block-id :instructions 2]]
          (doseq [bad [(assoc-in mir (conj path :operands) (vec (reverse (:operands comparison))))
                      (assoc-in mir (into path [:source :span :start]) 999)
                      (assoc-in mir [:control-flow-graph :join :incoming
                                     (if (= placement :then) 0 1) :value] id)]]
            (let [upstream (invoke 'sh12-verify-branch-join-mir core bad)
                  rejection (run core bad)]
              (is (= :rejected (:status rejection)))
              (is (= (:diagnostics upstream) (:diagnostics rejection)))
              (is (nil? (:execution rejection)))))
          (doseq [candidate [(assoc-in result [:execution :value] :forged)
                             (assoc-in result [:projection :operations 0 :role] :forged)]]
            (is (= :rejected (:status (verify core mir candidate))))))))))

(deftest sh13-executes-integer-less-than-only-in-the-selected-arm
  (check-selected-comparison-arm
   :integer-lt :integer-less-than
   [[0 1 true] [1 0 false] [1 1 false]
    [-9223372036854775808 9223372036854775807 true]
    [9223372036854775807 -9223372036854775808 false]]))

(deftest sh13-executes-integer-equality-only-in-the-selected-arm
  ;; The compiler's recursive-component validator compares a component's
  ;; function count with one inside a selected arm. These literal witnesses
  ;; cover that operation/placement, not the helper's full record ingress.
  (check-selected-comparison-arm
   :integer-eq :integer-equality
   [[1 1 true] [0 1 false] [1 0 false] [-1 -1 true]
    [-9223372036854775808 -9223372036854775808 true]
    [9223372036854775807 9223372036854775807 true]
    [-9223372036854775808 9223372036854775807 false]]))

(deftest sh13-composes-entry-and-selected-arm-comparisons
  ;; The compiler's frontier/visited count guards compose an integer condition
  ;; with an integer comparison in the selected arm. These bounded literal
  ;; witnesses do not establish count, collection or whole-helper ingress.
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (letfn [(subject [entry-op left right arm-op a b]
              (with-integer-comparison
                extension
                (with-integer-comparison
                  extension (scalar-core (checked extension) true
                                          :gravity.type/bool false true)
                  placement arm-op a b)
                :condition entry-op left right))]
      (doseq [[entry-op left right condition]
              [[:integer-eq 0 0 true] [:integer-eq 0 1 false]
               [:integer-lt -9223372036854775808 9223372036854775807 true]
               [:integer-lt 9223372036854775807 -9223372036854775808 false]]
              [arm-op a b arm-value]
              [[:integer-eq 1 1 true] [:integer-eq 0 1 false]
               [:integer-lt -9223372036854775808 9223372036854775807 true]
               [:integer-lt 9223372036854775807 -9223372036854775808 false]]]
        (let [core (subject entry-op left right arm-op a b)
              mir (build core) result (run core mir)
              nodes (:core-nodes core) ids (mapv :node-id nodes)
              rows (get-in result [:projection :operations])
              if-node (first (filter #(= :if (:source-operation %)) nodes))
              arm-id ((:operands if-node) (if (= placement :then) 1 2))
              comparison (first (filter #(= arm-id (:node-id %)) nodes))
              selected? (= placement (if condition :then :else))
              selected-ids (if selected? (conj (:operands comparison) arm-id)
                              [((:operands if-node) (if condition 1 2))])
              coordinates (coordinates core mir)
              incoming (first (filter #(= (coordinates placement) (:predecessor %))
                                      (get-in mir [:control-flow-graph :join :incoming])))
              operations (into {} (for [block (vals (get-in mir [:functions
                                                                 (:entrypoint core) :blocks]))
                                       op (:instructions block)] [(:op-id op) op]))]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :accepted (:status result)))
          (is (= (if selected? arm-value (not condition))
                 (get-in result [:execution :value])))
          (is (= (if selected? 503 505) (get-in result [:execution :fuel])))
          (is (= (into (subvec ids 0 3) selected-ids)
                 (mapv :operation (filter :operation (get-in result [:execution :trace])))))
          (is (= 9 (count rows)))
          (is (= ids (mapv :operation-id rows)))
          (is (= (mapv :source nodes) (mapv :source rows)))
          (is (= (mapv #(get-in operations [% :facts]) ids) (mapv :facts rows)))
          (is (= arm-id (:value incoming)))
          (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))))
      (let [core (subject :integer-eq 0 (if (= placement :then) 1 0)
                          :integer-lt 0 1)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            if-node (first (filter #(= :if (:source-operation %)) nodes))
            arm-id ((:operands if-node) (if (= placement :then) 1 2))
            comparison (first (filter #(= arm-id (:node-id %)) nodes))
            left-index (.indexOf ids (first (:operands comparison)))
            comparison-index (.indexOf ids arm-id)]
        ;; Invalid UNSELECTED arm payloads/signatures still reject pretrace.
        (doseq [bad [(assoc-in core [:core-nodes left-index :attributes :value] nil)
                    (assoc-in core [:core-nodes left-index :attributes :value] 9223372036854775808N)
                    (assoc-in core [:core-nodes comparison-index :operands] [])
                    (with-integer-comparison extension core
                      (if (= placement :then) :else :then) :integer-eq 0 0)]]
          (let [result (run bad (build bad))]
            (is (= :passed (get-in result [:upstream-verification :status])))
            (is (= :rejected (:status result)))
            (is (= :bounded-execution-subset (get-in result [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution result)))))
        (let [mir (build core) result (run core mir)
              path [:functions (:entrypoint core) :blocks
                    ((coordinates core mir) placement) :instructions 2]]
          (doseq [bad [(assoc-in mir (conj path :operands) [])
                      (assoc-in mir [:data-flow-graph :definitions "forged"] {})]]
            (let [original (invoke 'sh12-verify-branch-join-mir core bad)
                  rejection (run core bad)]
              (is (= :rejected (:status rejection)))
              (is (= (:diagnostics original) (:diagnostics rejection)))
              (is (nil? (:execution rejection)))))
          (is (= :rejected (:status (verify core mir (assoc-in result [:execution :value] :forged)))))
          (is (= :rejected (:status (verify core mir (assoc-in result [:projection :operations 0 :role] :forged))))))))))

(deftest sh13-preserves-literal-keyword-return-values
  ;; Actual c11-opcode outputs are supplied literal DATA here, not a lowering
  ;; or execution claim for the whole c11-opcode helper or a fresh C10 input.
  (let [constant (invoke 'c11-opcode {:source-operation :literal})
        less-than (invoke 'c11-opcode {:source-operation :integer-lt})]
    (is (= :constant constant))
    (is (= :integer-lt less-than))
    (doseq [extension [".gravity" ".qst"]
            [operation left right condition then-value else-value]
            [[nil nil nil true constant less-than]
             [nil nil nil false constant less-than]
             [nil nil nil true :left/value :right/value]
             [nil nil nil false :left/value :right/value]
             [:integer-eq 0 0 true :io/write :pure]
             [:integer-eq 0 1 false :io/write :pure]
             [:integer-lt -9223372036854775808 9223372036854775807
              true constant less-than]
             [:integer-lt 9223372036854775807 -9223372036854775808
              false constant less-than]]]
      (let [base (scalar-core (checked extension) condition
                              :gravity.type/keyword then-value else-value)
            core (if operation
                   (with-integer-comparison extension base :condition operation left right)
                   base)
            mir (build core) result (run core mir)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            rows (get-in result [:projection :operations])
            expected (if condition then-value else-value)
            types (mapv :type nodes)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :accepted (:status result)))
        (is (= :returned (get-in result [:execution :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= (namespace expected) (namespace (get-in result [:execution :value]))))
        (is (= (name expected) (name (get-in result [:execution :value]))))
        (is (= (if operation 505 507) (get-in result [:execution :fuel])))
        (is (= ids (mapv :operation-id rows)))
        (is (= types (mapv :source-type rows)))
        (is (= (mapv {:gravity.type/i64 :i64 :gravity.type/bool :bool
                      :gravity.type/keyword :keyword} types)
               (mapv :runtime-type rows)))
        (is (= (mapv :source nodes) (mapv :source rows)))
        (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
               (mapv #(get-in % [:facts :type-fact-id]) rows)))
        (is (= (mapv #(get-in % [:attributes :value])
                     (filter #(= :literal (:source-operation %)) nodes))
               (mapv #(get-in % [:constant-payload :value])
                     (filter #(= :literal (:role %)) rows))))
        (is (= :passed (:status (verify core mir result)))))))
  ;; Both arms are validated, even when the bad typed Keyword is unselected.
  (doseq [extension [".gravity" ".qst"] condition [true false]
          value [":constant" 'constant nil true 7 [] {} '() #{}]]
    (let [core (scalar-core (checked extension) condition :gravity.type/keyword
                            (if condition :constant value)
                            (if condition value :integer-lt))
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (= :bounded-execution-subset
             (get-in result [:diagnostics 0 :missing-fact])))
      (is (nil? (:projection result)))
      (is (nil? (:execution result)))
      (is (= :passed (:status (verify core mir result))))))
  (let [core (scalar-core (checked ".gravity") true
                          :gravity.type/keyword :left/value :right/value)
        quoted (assoc-in core [:core-nodes 2 :source-operation] :quote)
        quoted-result (run quoted (build quoted))
        mir (build core) result (run core mir)
        then-id (:then (coordinates core mir))
        path [:functions (:entrypoint core) :blocks then-id :instructions 0]]
    (is (= :passed (get-in quoted-result [:upstream-verification :status])))
    (is (= :bounded-execution-subset
           (get-in quoted-result [:diagnostics 0 :missing-fact])))
    (is (nil? (:execution quoted-result)))
    (doseq [mutant [(assoc-in mir (into path [:constant-payload :value]) :right/value)
                    (assoc-in mir (into path [:facts :type-fact-id]) "forged")
                    (assoc-in mir (into path [:source :span :start]) 999)]]
      (let [original (invoke 'sh12-verify-branch-join-mir core mutant)
            rejection (run core mutant)]
        (is (= :rejected (:status original)))
        (is (= (:diagnostics original) (:diagnostics rejection)))
        (is (nil? (:execution rejection)))))
    (doseq [candidate [(assoc-in result [:execution :value] :right/value)
                       (assoc-in result [:projection :operations 1 :runtime-type] :bool)
                       (assoc-in result [:projection :operations 1 :role] :integer-less-than)]]
      (is (= :rejected (:status (verify core mir candidate))))))
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality invoked" {})))
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash invoked" {}))))]
    (doseq [value [opaque [opaque] {:value opaque}]]
      (is (false? (invoke 'sh13-branch-join-literal-valid?
                          {:opcode :constant :source-operation :literal
                           :operands [] :type :gravity.type/keyword
                           :constant-payload {:present? true :value value}}
                          :gravity.type/keyword)))
      (is (empty? @callbacks))))
  (is (false? (invoke 'sh13-branch-join-entry-valid?
                      [{:opcode :constant :source-operation :literal :operands []
                        :type :gravity.type/keyword
                        :constant-payload {:present? true :value :condition}}])))
  (is (false? (invoke 'sh13-branch-join-arm-valid?
                      [{:opcode :constant} {:opcode :constant} {:opcode :integer-lt}]
                      :gravity.type/keyword))))

(defn- with-scalar-truthiness [extension base type value]
  ;; Original supplied SH12 constructors, not fresh C6 adaptation. The input
  ;; type family remains gravity.type/*; no source type ID is relabeled.
  (let [nodes (:core-nodes base)
        if-node (first (filter #(= :if (:source-operation %)) nodes))
        id (first (:operands if-node))
        index (.indexOf (mapv :node-id nodes) id)
        target (nodes index) path (:path if-node)
        make (fn [name args]
               ((existing 'invoke) (get (existing 'accepted-plans) extension) name args))
        value-id "sh13:truthiness:input"
        input (make 'sh12-branch-join-node
                    [value-id (conj path :test) :literal [] type value 0 1])
        truthy (-> target (assoc :path (conj path :truthy) :source-operation :truthy
                                :operands [value-id])
                   (update :attributes assoc :false-values [nil false]
                           :result-type :gravity.type/bool))]
    (-> base
        (assoc :core-nodes (into (into (subvec nodes 0 index) [input truthy])
                                (subvec nodes (inc index))))
        (assoc-in [:type-facts value-id] (make 'sh12-branch-join-type-fact [value-id type]))
        (assoc-in [:effect-facts value-id] (make 'sh12-branch-join-effect-fact [value-id]))
        (assoc-in [:ownership-facts value-id] (make 'sh12-branch-join-ownership-fact [value-id]))
        (assoc-in [:capability-facts value-id] (make 'sh12-branch-join-capability-fact [value-id]))
        (assoc-in [:safety-facts value-id] (:safety input))
        (assoc-in [:profile-facts value-id] {:profile :meta :target :jvm}))))

(deftest sh13-executes-original-scalar-truthiness-before-branching
  (doseq [extension [".gravity" ".qst"]
          [type value truth] [[:gravity.type/bool false false] [:gravity.type/bool true true]
                             [:gravity.type/i64 0 true] [:gravity.type/i64 -1 true]
                             [:gravity.type/i64 Long/MIN_VALUE true]
                             [:gravity.type/i64 Long/MAX_VALUE true]
                             [:gravity.type/keyword :left/value true]
                             [:gravity.type/keyword :right/value true]]
          [result-type left right] [[:gravity.type/i64 11 22]
                                    [:gravity.type/bool false true]
                                    [:gravity.type/keyword :left/value :right/value]]]
    (let [core (with-scalar-truthiness extension
                 (scalar-core (checked extension) true result-type left right) type value)
          mir (build core) result (run core mir) nodes (:core-nodes core)
          ids (mapv :node-id nodes) rows (get-in result [:projection :operations])
          coordinates (coordinates core mir)
          selected (coordinates (if truth :then :else))]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :accepted (:status result)))
      (is (= (if truth left right) (get-in result [:execution :value])))
      (is (= 506 (get-in result [:execution :fuel])))
      (is (= [(ids 0) (ids 1) (ids (if truth 2 3))]
             (mapv :operation (filter :operation (get-in result [:execution :trace])))))
      (is (= 1 (count (filter #(= (ids 0) (:operation %)) (get-in result [:execution :trace])))))
      (is (= 6 (count rows)))
      (is (= ids (mapv :operation-id rows)))
      (is (= [:literal :truthiness] (mapv :role (subvec rows 0 2))))
      (is (= (mapv :source nodes) (mapv :source rows)))
      (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
      (is (= ((:operands (first (filter #(= :if (:source-operation %)) nodes))) (if truth 1 2))
             (:value (first (filter #(= selected (:predecessor %))
                                    (get-in mir [:control-flow-graph :join :incoming]))))))
      (is (= :passed (:status (verify core mir result))))))
  (doseq [extension [".gravity" ".qst"]]
    (let [base (with-scalar-truthiness extension (checked extension) :gravity.type/i64 0)
          mir (build base) accepted (run base mir)
          candidates [(with-scalar-truthiness extension (checked extension) :gravity.type/string "text")
                      (with-scalar-truthiness extension (checked extension) :gravity.type/unit nil)
                      (assoc-in base [:core-nodes 0 :attributes :value] nil)
                      (assoc-in base [:core-nodes 1 :operands] [])
                      (assoc-in base [:core-nodes 1 :operands] ["sh13:truthiness:input" "sh13:truthiness:input"])
                      (assoc-in base [:core-nodes 1 :attributes :false-values] [false])
                      (assoc-in base [:core-nodes 1 :attributes :result-type] :gravity.type/i64)
                      (assoc-in base [:core-nodes 1 :source-operation] :local)
                      (update-in base [:core-nodes 1 :path] conj :extra)
                      (assoc-in base [:core-nodes 1 :path] [:wrong])
                      (assoc-in base [:core-nodes 3 :attributes :value] nil)]]
      (doseq [core candidates]
        (let [candidate-mir (build core) result (run core candidate-mir)]
          (is (= :rejected (:status result)))
          (is (nil? (:execution result)))
          (is (nil? (:projection result)))
          (is (= :passed (:status (verify core candidate-mir result))))))
      (let [ep [:functions (:entrypoint base) :blocks (:entry (coordinates base mir)) :instructions 1]
            mutant (assoc-in mir (conj ep :source-operation) :literal)
            result (run base mutant)]
        (is (= :rejected (:status result)))
        (is (= :rejected (get-in result [:upstream-verification :status])))
        (is (nil? (:execution result)))
        (is (= :passed (:status (verify base mutant result)))))
      (is (= :rejected (:status (verify base mir (assoc-in accepted [:execution :value] :forged))))))))

(defn- with-scalar-let [extension base placement]
  ;; Exact C6 operand/path/name family, represented in the supplied SH12 type
  ;; family. This is NOT fresh C6/C10 source adaptation or a type-ID relabeler.
  (let [nodes (:core-nodes base)
        if-node (first (filter #(= :if (:source-operation %)) nodes))
        scope-id ((:operands if-node) (if (= placement :then) 1 2))
        index (.indexOf (mapv :node-id nodes) scope-id)
        target (nodes index) path (:path target) type (:type target)
        start (get-in target [:source :span :start])
        make (fn [name args]
               ((existing 'invoke) (get (existing 'accepted-plans) extension) name args))
        prefix (str "sh13:" (name placement) ":let:")
        value-id (str prefix "value") binding-id (str prefix "binding")
        local-id (str prefix "local")
        value (make 'sh12-branch-join-node
                    [value-id (into path [:bindings 0 :expr]) :literal [] type
                     (get-in target [:attributes :value]) (inc start) (+ start 2)])
        binding (-> (make 'sh12-branch-join-node
                          [binding-id (into path [:bindings 0 :binder])
                           :let-binding [value-id] type nil (+ start 3) (+ start 4)])
                    (update :attributes assoc :name 'stored :shadowed-binding nil
                            :source-form-id (str prefix "name-form")))
        local (-> (make 'sh12-branch-join-node
                        [local-id (into path [:body 0]) :local [binding-id]
                         type nil (+ start 5) (+ start 6)])
                  (update :attributes assoc :name 'stored :resolved-binding binding-id))
        scope (-> (make 'sh12-branch-join-node
                        [scope-id path :let [binding-id local-id] type nil start (+ start 7)])
                  (assoc-in [:attributes :binding-count] 1))]
    (reduce (fn [core node]
              (let [id (:node-id node)]
                (-> core
                    (assoc-in [:type-facts id] (make 'sh12-branch-join-type-fact [id type]))
                    (assoc-in [:effect-facts id] (make 'sh12-branch-join-effect-fact [id]))
                    (assoc-in [:ownership-facts id] (make 'sh12-branch-join-ownership-fact [id]))
                    (assoc-in [:capability-facts id] (make 'sh12-branch-join-capability-fact [id]))
                    (assoc-in [:safety-facts id] (:safety node))
                    (assoc-in [:profile-facts id] {:profile :meta :target :jvm}))))
            (assoc base :core-nodes
                   (into (into (subvec nodes 0 index) [value binding local scope])
                         (subvec nodes (inc index))))
            [value binding local scope])))

(deftest sh13-executes-branch-local-scalar-let-with-original-identities
  (doseq [extension [".gravity" ".qst"] placement [:then :else]
          condition [true false]
          [type left right] [[:gravity.type/i64 -9223372036854775808 9223372036854775807]
                             [:gravity.type/bool false true]
                             [:gravity.type/keyword :left/value :right/value]]]
    (let [core (with-scalar-let extension
                 (scalar-core (checked extension) condition type left right) placement)
          mir (build core) result (run core mir)
          nodes (:core-nodes core) ids (mapv :node-id nodes)
          rows (get-in result [:projection :operations])
          if-node (first (filter #(= :if (:source-operation %)) nodes))
          scope-id ((:operands if-node) (if (= placement :then) 1 2))
          scope-index (.indexOf ids scope-id)
          alias-ids (subvec ids (- scope-index 3) (inc scope-index))
          selected? (= placement (if condition :then :else))
          expected-ids (into [(ids 0)]
                            (if selected? alias-ids
                                [((:operands if-node) (if condition 1 2))]))
          coordinates (coordinates core mir)
          incoming (first (filter #(= (coordinates placement) (:predecessor %))
                                  (get-in mir [:control-flow-graph :join :incoming])))
          operations (into {} (for [block (vals (get-in mir [:functions (:entrypoint core) :blocks]))
                                   op (:instructions block)] [(:op-id op) op]))]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :accepted (:status result)))
      (is (= (if condition left right) (get-in result [:execution :value])))
      (is (= (if selected? 504 507) (get-in result [:execution :fuel])))
      (is (= expected-ids (mapv :operation (filter :operation (get-in result [:execution :trace])))))
      (is (= (if selected? 1 0)
             (count (filter #(= (alias-ids 0) (:operation %)) (get-in result [:execution :trace])))))
      (is (= 8 (count rows)))
      (is (= ids (mapv :operation-id rows)))
      (is (= (mapv :source nodes) (mapv :source rows)))
      (is (= (mapv #(get-in operations [% :facts]) ids) (mapv :facts rows)))
      (is (= [:literal :lexical-binding :local-reference :lexical-scope]
             (mapv :role (subvec rows (- scope-index 3) (inc scope-index)))))
      (is (= scope-id (:value incoming)))
      (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
      (is (= :passed (:status (verify core mir result))))))
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (let [core (with-scalar-let extension
                 (scalar-core (checked extension) (= placement :else)
                              :gravity.type/i64 11 22) placement)
          nodes (:core-nodes core) ids (mapv :node-id nodes)
          scope-id ((:operands (first (filter #(= :if (:source-operation %)) nodes)))
                    (if (= placement :then) 1 2))
          scope-index (.indexOf ids scope-id)
          value-index (- scope-index 3) binding-index (- scope-index 2)
          local-index (dec scope-index)]
      ;; This arm is unselected; every lexical/source/type premise still joins.
      (doseq [bad [(assoc-in core [:core-nodes value-index :attributes :value] nil)
                  (assoc-in core [:core-nodes binding-index :attributes :name] 'different)
                  (assoc-in core [:core-nodes local-index :attributes :name] "stored")
                  (assoc-in core [:core-nodes local-index :attributes :resolved-binding] "other")
                  (assoc-in core [:core-nodes binding-index :attributes :source-form-id] nil)
                  (assoc-in core [:core-nodes binding-index :attributes :shadowed-binding] "outer")
                  (update-in core [:core-nodes binding-index :attributes] dissoc :shadowed-binding)
                  (assoc-in core [:core-nodes scope-index :attributes :binding-count] 2)
                  (assoc-in core [:core-nodes local-index :path]
                            (conj (pop (get-in core [:core-nodes local-index :path])) 1))
                  (assoc-in core [:core-nodes local-index :operands] [(ids value-index)])
                  (assoc-in core [:core-nodes scope-index :operands] [(ids local-index) (ids binding-index)])
                  (with-integer-comparison extension core :condition :integer-eq 0 0)
                  (with-scalar-let extension core (if (= placement :then) :else :then))]]
        (let [result (run bad (build bad))]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :rejected (:status result)))
          (is (= :bounded-execution-subset (get-in result [:diagnostics 0 :missing-fact])))
          (is (nil? (:execution result)))))
      (let [mir (build core) result (run core mir)
            path [:functions (:entrypoint core) :blocks ((coordinates core mir) placement)
                  :instructions 2]]
        (doseq [bad [(assoc-in mir (conj path :operands) [(ids value-index)])
                    (assoc-in mir (into path [:source :span :start]) 999)]]
          (let [original (invoke 'sh12-verify-branch-join-mir core bad)
                rejection (run core bad)]
            (is (= :rejected (:status rejection)))
            (is (= (:diagnostics original) (:diagnostics rejection)))
            (is (nil? (:execution rejection)))))
        (is (= :rejected (:status (verify core mir (assoc-in result [:projection :operations local-index :role] :literal))))))
      ;; Direct new-helper supplied-DATA guards: no callback-based ID/name join.
      (let [callbacks (atom [])
            opaque (proxy [Object] []
                     (equals [_] (swap! callbacks conj :equals) false)
                     (hashCode [] (swap! callbacks conj :hash) 0))
            mir (build core)
            operations (get-in mir [:functions (:entrypoint core) :blocks
                                   ((coordinates core mir) placement) :instructions])]
        (doseq [bad [(assoc-in core [:core-nodes binding-index :attributes :name] opaque)
                    (assoc-in core [:core-nodes local-index :attributes :resolved-binding] opaque)]]
          (is (false? (invoke 'sh13-branch-join-let-arm-valid? bad operations :gravity.type/i64)))
          (is (empty? @callbacks)))))))

(deftest sh13-executes-explicit-nil-carriers-without-fabricating-values
  ;; The shared scalar mapping admits Nil returns/let arms as well as tests.
  ;; These are supplied SH12 constructors, not C6-generated source evidence.
  (doseq [extension [".gravity" ".qst"]]
    (let [base (scalar-core (checked extension) true :gravity.type/nil nil nil)
          cases (concat
                 (for [condition [true false]]
                   [(scalar-core (checked extension) condition :gravity.type/nil nil nil) nil 507])
                 [[(with-scalar-truthiness extension (checked extension) :gravity.type/nil nil) 22 506]
                  [(with-scalar-truthiness extension base :gravity.type/nil nil) nil 506]]
                 (for [right [0 1]]
                   [(with-integer-comparison extension base :condition :integer-eq 0 right) nil 505])
                 (for [placement [:then :else] condition [true false]]
                   [(with-scalar-let extension
                      (scalar-core (checked extension) condition :gravity.type/nil nil nil) placement)
                    nil (if (= placement (if condition :then :else)) 504 507)]))]
      (doseq [[core expected fuel] cases]
        (let [mir (build core) result (run core mir)
              rows (get-in result [:projection :operations])]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :accepted (:status result)))
          (is (= :returned (get-in result [:execution :status])))
          (is (contains? (:execution result) :value))
          (is (= expected (get-in result [:execution :value])))
          (is (= fuel (get-in result [:execution :fuel])))
          (is (every? #(= :nil (:runtime-type %))
                      (filter #(= :gravity.type/nil (:source-type %)) rows)))
          (is (= (mapv :source (:core-nodes core)) (mapv :source rows)))
          (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))
          (is (= :rejected (:status (verify core mir (update result :execution dissoc :value)))))))
      (let [mir (build base) accepted (run base mir)
            ep [:functions (:entrypoint base) :blocks (:then (coordinates base mir)) :instructions 0]
            op (get-in mir ep)]
        (doseq [bad [(dissoc (:constant-payload op) :value)
                    {:present? false :value nil} {:present? true :value false}]]
          (is (false? (invoke 'sh13-branch-join-literal-valid?
                              (assoc op :constant-payload bad) :gravity.type/nil)))
          (let [mutant (assoc-in mir (conj ep :constant-payload) bad) result (run base mutant)]
            (is (= :rejected (:status result)))
            (is (nil? (:execution result)))
            (is (= :passed (:status (verify base mutant result))))))
        ;; C11 currently synthesizes payload presence from get. Retained original
        ;; source attributes must therefore establish explicit Nil independently,
        ;; including in the arm that would not execute.
        (doseq [core [(update-in base [:core-nodes 1 :attributes] dissoc :value)
                      (update-in base [:core-nodes 2 :attributes] dissoc :value)
                      (assoc-in base [:core-nodes 1 :attributes :value] false)
                      (assoc-in base [:core-nodes 1 :source-operation] :implicit-nil)
                      (assoc-in base [:core-nodes 1 :source-operation] :quote)]]
          (let [candidate-mir (build core) result (run core candidate-mir)]
            (is (= :rejected (:status result)))
            (is (nil? (:execution result)))
            (is (nil? (:projection result)))
            (is (= :passed (:status (verify core candidate-mir result))))))
        (let [mutant (assoc-in mir (into ep [:facts :type-fact-id]) "forged")
              result (run base mutant)]
          (is (= :rejected (:status result)))
          (is (= :rejected (get-in result [:upstream-verification :status])))
          (is (nil? (:execution result)))
          (is (= :passed (:status (verify base mutant result)))))
        (is (= :rejected (:status (verify base mir (assoc-in accepted [:execution :value] false)))))))))

(defn- with-two-expression-do [extension base placement first-type first-value second-type second-value]
  ;; The original C6 do family has two ordered body roots and forwards the last.
  ;; Supplied SH12 types remain separate from actual C6/source authentication.
  (let [nodes (:core-nodes base)
        if-node (first (filter #(= :if (:source-operation %)) nodes))
        id ((:operands if-node) (if (= placement :then) 1 2))
        index (.indexOf (mapv :node-id nodes) id)
        path (conj (:path if-node) placement)
        start (get-in nodes [index :source :span :start])
        make (fn [name args]
               ((existing 'invoke) (get (existing 'accepted-plans) extension) name args))
        prefix (str "sh13:" (name placement) ":do:")
        first-id (str prefix "first") second-id (str prefix "second")
        children (mapv (fn [child-id type value child]
                         (make 'sh12-branch-join-node
                               [child-id (into path [:body child]) :literal [] type value
                                (+ start child) (+ start child 1)]))
                       [first-id second-id] [first-type second-type] [first-value second-value] [0 1])
        scope (-> (make 'sh12-branch-join-node
                        [id path :do [first-id second-id] second-type nil start (+ start 3)])
                  (assoc-in [:attributes :body-count] 2))
        replacements (conj children scope)]
    (reduce (fn [core node]
              (let [node-id (:node-id node) type (:type node)]
                (-> core
                    (assoc-in [:type-facts node-id] (make 'sh12-branch-join-type-fact [node-id type]))
                    (assoc-in [:effect-facts node-id] (make 'sh12-branch-join-effect-fact [node-id]))
                    (assoc-in [:ownership-facts node-id] (make 'sh12-branch-join-ownership-fact [node-id]))
                    (assoc-in [:capability-facts node-id] (make 'sh12-branch-join-capability-fact [node-id]))
                    (assoc-in [:safety-facts node-id] (:safety node))
                    (assoc-in [:profile-facts node-id] {:profile :meta :target :jvm}))))
            (assoc base :core-nodes
                   (into (into (subvec nodes 0 index) replacements) (subvec nodes (inc index))))
            replacements)))

(deftest sh13-executes-two-expression-do-only-in-the-selected-arm
  (doseq [extension [".gravity" ".qst"] placement [:then :else] condition [true false]
          [first-type first-value type value other]
          [[:gravity.type/bool false :gravity.type/i64 7 8]
           [:gravity.type/nil nil :gravity.type/bool true false]
           [:gravity.type/i64 Long/MIN_VALUE :gravity.type/keyword :last/value :other/value]
           [:gravity.type/keyword :discarded/value :gravity.type/nil nil nil]
           [:gravity.type/i64 Long/MAX_VALUE :gravity.type/i64 Long/MIN_VALUE Long/MAX_VALUE]
           [:gravity.type/bool true :gravity.type/bool false true]]]
    (let [core (with-two-expression-do extension
                 (scalar-core (checked extension) condition type
                              (if (= placement :then) value other)
                              (if (= placement :else) value other))
                 placement first-type first-value type value)
          mir (build core) result (run core mir) nodes (:core-nodes core)
          ids (mapv :node-id nodes)
          if-node (first (filter #(= :if (:source-operation %)) nodes))
          scope-id ((:operands if-node) (if (= placement :then) 1 2))
          index (.indexOf ids scope-id) body-ids (subvec ids (- index 2) (inc index))
          selected? (= placement (if condition :then :else))
          rows (get-in result [:projection :operations])
          flat (into {} (for [block (vals (get-in mir [:functions (:entrypoint core) :blocks]))
                             op (:instructions block)] [(:op-id op) op]))
          incoming (first (filter #(= ((coordinates core mir) placement) (:predecessor %))
                                  (get-in mir [:control-flow-graph :join :incoming])))]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :accepted (:status result)))
      (is (= (if selected? value other) (get-in result [:execution :value])))
      (is (= (if selected? 505 507) (get-in result [:execution :fuel])))
      (is (= (into [(ids 0)] (if selected? body-ids
                              [((:operands if-node) (if condition 1 2))]))
             (mapv :operation (filter :operation (get-in result [:execution :trace])))))
      (is (= (if selected? [1 1 1] [0 0 0])
             (mapv (fn [id] (count (filter #(= id (:operation %)) (get-in result [:execution :trace])))) body-ids)))
      (is (= 7 (count rows)))
      (is (= ids (mapv :operation-id rows)))
      (is (= (mapv :source nodes) (mapv :source rows)))
      (is (= (mapv #(get-in flat [% :facts]) ids) (mapv :facts rows)))
      (is (= [:literal :literal :sequence-result] (mapv :role (subvec rows (- index 2) (inc index)))))
      (is (= (subvec body-ids 0 2) (:operands (rows index))))
      (is (= scope-id (:value incoming)))
      (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
      (is (= :passed (:status (verify core mir result))))))
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (let [base (scalar-core (checked extension) (= placement :else) :gravity.type/i64 11 22)
          core (with-two-expression-do extension base placement :gravity.type/bool false :gravity.type/i64 7)
          nodes (:core-nodes core) ids (mapv :node-id nodes)
          if-node (first (filter #(= :if (:source-operation %)) nodes))
          scope-id ((:operands if-node) (if (= placement :then) 1 2))
          index (.indexOf ids scope-id) first-index (- index 2) second-index (dec index)
          scope-path [:core-nodes index] first-path [:core-nodes first-index] second-path [:core-nodes second-index]
          nil-first (with-two-expression-do extension base placement :gravity.type/nil nil :gravity.type/i64 7)
          candidates (concat
                       (for [count [0 1 3]] (assoc-in core (into scope-path [:attributes :body-count]) count))
                       (for [operands [[] [(ids first-index)] [(ids second-index) (ids first-index)]
                                       [(ids first-index) (ids second-index) (ids first-index)]]]
                         (assoc-in core (conj scope-path :operands) operands))
                       [(update-in core (conj scope-path :attributes) dissoc :body-count)
                        (assoc-in core (conj scope-path :source-operation) :let)
                        (assoc-in core (conj first-path :path) (:path (nodes second-index)))
                        (assoc-in core (conj second-path :path) (:path (nodes first-index)))
                        (assoc-in core (into first-path [:attributes :value]) 0)
                        (assoc-in core (into second-path [:attributes :value]) nil)
                        (assoc-in core (conj first-path :effects) #{:io/write})
                        (with-two-expression-do extension base placement :gravity.type/string "discarded" :gravity.type/i64 7)
                        (update-in nil-first (conj first-path :attributes) dissoc :value)
                        (assoc-in nil-first (conj first-path :source-operation) :implicit-nil)])]
      (doseq [candidate candidates]
        (let [mir (build candidate) result (run candidate mir)]
          (is (= :rejected (:status result)))
          (is (nil? (:execution result)))
          (is (nil? (:projection result)))
          (is (= :passed (:status (verify candidate mir result))))))
      (let [mir (build core) accepted (run core mir)
            path [:functions (:entrypoint core) :blocks ((coordinates core mir) placement) :instructions 2]
            mutant (assoc-in mir (conj path :operands) [(ids second-index) (ids first-index)])
            result (run core mutant)]
        (is (= :rejected (:status result)))
        (is (= :rejected (get-in result [:upstream-verification :status])))
        (is (nil? (:execution result)))
        (is (= :passed (:status (verify core mutant result))))
        (is (= :rejected (:status (verify core mir (assoc-in accepted [:projection :operations index :role] :literal)))))))))

(deftest sh13-do-arms-keep-the-literal-condition-placement-boundary
  (doseq [extension [".gravity" ".qst"] placement [:then :else]
          operation [:integer-eq :integer-lt]]
    (let [base (with-two-expression-do extension (checked extension) placement
                 :gravity.type/bool false :gravity.type/i64 7)
          core (with-integer-comparison extension base :condition operation 0 1)
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (nil? (:execution result)))
      (is (nil? (:projection result)))
      (is (= :passed (:status (verify core mir result)))))))

(deftest sh13-composes-scalar-truthiness-with-two-expression-do
  ;; Supplied original constructors preserve both source families, not a fresh
  ;; C6 type adapter. Only truthiness + sequence gets the new eight-row layout.
  (doseq [extension [".gravity" ".qst"] placement [:then :else]
          [input-type input truth first-type first-value type value other]
          [[:gravity.type/bool true true :gravity.type/bool false :gravity.type/i64 7 8]
           [:gravity.type/bool false false :gravity.type/nil nil :gravity.type/bool true false]
           [:gravity.type/nil nil false :gravity.type/bool false :gravity.type/keyword :last/value :other/value]
           [:gravity.type/i64 0 true :gravity.type/nil nil :gravity.type/nil nil nil]
           [:gravity.type/keyword :condition/value true :gravity.type/bool false :gravity.type/i64 Long/MIN_VALUE Long/MAX_VALUE]
           [:gravity.type/keyword :condition true :gravity.type/nil nil :gravity.type/bool false true]
           [:gravity.type/i64 -1 true :gravity.type/bool false :gravity.type/keyword :last/name :other/name]
           [:gravity.type/bool false false :gravity.type/nil nil :gravity.type/nil nil nil]]]
    (let [core (with-scalar-truthiness extension
                 (with-two-expression-do extension
                   (scalar-core (checked extension) true type
                                (if (= placement :then) value other)
                                (if (= placement :else) value other))
                   placement first-type first-value type value)
                 input-type input)
          mir (build core) result (run core mir) nodes (:core-nodes core)
          ids (mapv :node-id nodes) rows (get-in result [:projection :operations])
          if-node (first (filter #(= :if (:source-operation %)) nodes))
          scope-id ((:operands if-node) (if (= placement :then) 1 2))
          index (.indexOf ids scope-id) body-ids (subvec ids (- index 2) (inc index))
          selected? (= placement (if truth :then :else))
          ops (into {} (for [block (vals (get-in mir [:functions (:entrypoint core) :blocks]))
                            op (:instructions block)] [(:op-id op) op]))
          incoming (first (filter #(= ((coordinates core mir) placement) (:predecessor %))
                                  (get-in mir [:control-flow-graph :join :incoming])))]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :accepted (:status result)))
      (is (= (if selected? value other) (get-in result [:execution :value])))
      (is (= (if selected? 504 506) (get-in result [:execution :fuel])))
      (is (= (into (subvec ids 0 2) (if selected? body-ids
                                    [((:operands if-node) (if truth 1 2))]))
             (mapv :operation (filter :operation (get-in result [:execution :trace])))))
      (is (= (if selected? [1 1 1] [0 0 0])
             (mapv (fn [id] (count (filter #(= id (:operation %)) (get-in result [:execution :trace])))) body-ids)))
      (is (= 8 (count rows)))
      (is (= ids (mapv :operation-id rows)))
      (is (= (mapv :source nodes) (mapv :source rows)))
      (is (= (mapv #(get-in ops [% :facts]) ids) (mapv :facts rows)))
      (is (= [:literal :truthiness] (mapv :role (subvec rows 0 2))))
      (is (= [:literal :literal :sequence-result] (mapv :role (subvec rows (- index 2) (inc index)))))
      (is (= scope-id (:value incoming)))
      (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
      (is (= :passed (:status (verify core mir result))))))
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (let [do-core (with-two-expression-do extension
                    (scalar-core (checked extension) true :gravity.type/nil nil nil)
                    placement :gravity.type/nil nil :gravity.type/nil nil)
          core (with-scalar-truthiness extension do-core :gravity.type/bool (= placement :else))
          nodes (:core-nodes core) ids (mapv :node-id nodes)
          if-node (first (filter #(= :if (:source-operation %)) nodes))
          scope-id ((:operands if-node) (if (= placement :then) 1 2))
          index (.indexOf ids scope-id) first-index (- index 2) second-index (dec index)
          scope [:core-nodes index] first-path [:core-nodes first-index]
          second-path [:core-nodes second-index]
          candidates [(assoc-in core (into scope [:attributes :body-count]) 1)
                      (assoc-in core (conj scope :operands) [(ids second-index) (ids first-index)])
                      (assoc-in core (conj first-path :path) (:path (nodes second-index)))
                      (assoc-in core (conj second-path :path) [:wrong])
                      (assoc-in core (into first-path [:attributes :value]) false)
                      (assoc-in core (into second-path [:attributes :value]) 0)
                      (assoc-in core (conj first-path :effects) #{:io/write})
                      (update-in core (conj first-path :attributes) dissoc :value)
                      (update-in core (conj second-path :attributes) dissoc :value)
                      (assoc-in core [:core-nodes 1 :operands] [])
                      (update-in core [:core-nodes 1 :path] conj :extra)
                      (assoc-in core [:core-nodes 1 :attributes :false-values] [false])
                      (assoc-in core [:core-nodes 1 :attributes :result-type] :gravity.type/i64)
                      (with-scalar-truthiness extension do-core :gravity.type/string "unsupported")]]
      (doseq [candidate candidates]
        (let [mir (build candidate) result (run candidate mir)]
          (is (= :rejected (:status result)))
          (is (nil? (:execution result)))
          (is (nil? (:projection result)))
          (is (= :passed (:status (verify candidate mir result))))))
      (let [mir (build core) accepted (run core mir)
            op-path [:functions (:entrypoint core) :blocks ((coordinates core mir) placement) :instructions 2]
            mutants [(assoc-in mir (conj op-path :operands) [(ids second-index) (ids first-index)])
                     (assoc-in mir (into op-path [:facts :type-fact-id]) "forged")
                     (assoc-in mir (into op-path [:source :span :start]) 999)
                     (update-in mir [:functions (:entrypoint core) :blocks ((coordinates core mir) placement)
                                     :instructions 0 :constant-payload] dissoc :value)]]
        (doseq [mutant mutants]
          (let [result (run core mutant)]
            (is (= :rejected (:status result)))
            (is (= :rejected (get-in result [:upstream-verification :status])))
            (is (nil? (:execution result)))
            (is (= :passed (:status (verify core mutant result))))))
        (is (= :rejected (:status (verify core mir (assoc-in accepted [:execution :value] false)))))
        (is (= :rejected (:status (verify core mir (assoc-in accepted [:projection :operations index :role] :literal))))))))
  (doseq [extension [".gravity" ".qst"] placement [:then :else] operation [:integer-eq :integer-lt]]
    (let [core (with-scalar-truthiness extension
                 (with-integer-comparison extension
                   (scalar-core (checked extension) true :gravity.type/bool false true)
                   placement operation 0 1)
                 :gravity.type/bool true)
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (nil? (:execution result)))
      (is (nil? (:projection result)))
      (is (= :passed (:status (verify core mir result)))))))

(deftest sh13-executes-integer-less-than-or-equal-only-in-the-selected-arm
  ;; C11's guarded origin-chain and i64 predicates contain <= in selected
  ;; branches. These supplied literal witnesses do not execute whole helpers.
  (check-selected-comparison-arm
   :integer-lte :integer-less-than-or-equal
   [[0 0 true] [0 1 true] [1 0 false] [-1 0 true] [0 -1 false]
    [-9223372036854775808 -9223372036854775808 true]
    [9223372036854775807 9223372036854775807 true]
    [-9223372036854775808 9223372036854775807 true]
    [9223372036854775807 -9223372036854775808 false]])
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (let [arm (with-integer-comparison extension
                (scalar-core (checked extension) true :gravity.type/bool false true)
                placement :integer-lte 0 0)]
      ;; Mixed entry-comparison and truthiness composition remain excluded.
      (doseq [core (conj (mapv #(with-integer-comparison extension arm :condition % 0 0)
                               [:integer-eq :integer-lt])
                         (with-scalar-truthiness extension arm :gravity.type/bool true))]
        (let [mir (build core) result (run core mir)]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :rejected (:status result)))
          (is (= :bounded-execution-subset (get-in result [:diagnostics 0 :missing-fact])))
          (is (nil? (:execution result)))
          (is (= :passed (:status (verify core mir result)))))))))

(deftest sh13-composes-two-integer-lte-span-guards
  ;; C11 span predicates contain (if (<= 0 start) (<= start end) false).
  ;; Repeated literal DATA models that value relation, not member lookup,
  ;; parameter binding, arbitrary Integer width or execution of the helper.
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (letfn [(subject [start end]
              (with-integer-comparison extension
                (with-integer-comparison extension
                  (scalar-core (checked extension) true :gravity.type/bool false false)
                  placement :integer-lte start end)
                :condition :integer-lte 0 start))]
      (doseq [[start end] [[0 0] [5 5] [-1 10] [5 4] [0 9223372036854775807]
                           [-9223372036854775808 -9223372036854775808]
                           [9223372036854775807 9223372036854775807]
                           [9223372036854775807 -9223372036854775808]
                           [-1 -9223372036854775808]]]
        (let [core (subject start end) mir (build core) result (run core mir)
              nodes (:core-nodes core) ids (mapv :node-id nodes)
              rows (get-in result [:projection :operations])
              condition (<= 0 start) selected? (= placement (if condition :then :else))
              if-node (first (filter #(= :if (:source-operation %)) nodes))
              comparison-id ((:operands if-node) (if (= placement :then) 1 2))
              comparison (first (filter #(= comparison-id (:node-id %)) nodes))
              selected-ids (if selected? (conj (:operands comparison) comparison-id)
                             [((:operands if-node) (if condition 1 2))])
              incoming (first (filter #(= ((coordinates core mir) placement) (:predecessor %))
                                      (get-in mir [:control-flow-graph :join :incoming])))
              operations (into {} (for [block (vals (get-in mir [:functions (:entrypoint core) :blocks]))
                                       op (:instructions block)] [(:op-id op) op]))]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :accepted (:status result)))
          (is (= (if selected? (<= start end) false) (get-in result [:execution :value])))
          (is (= (if selected? 503 505) (get-in result [:execution :fuel])))
          (is (= (into (subvec ids 0 3) selected-ids)
                 (mapv :operation (filter :operation (get-in result [:execution :trace])))))
          (is (= 9 (count rows)))
          (is (= ids (mapv :operation-id rows)))
          (is (= (mapv :source nodes) (mapv :source rows)))
          (is (= (mapv #(get-in operations [% :facts]) ids) (mapv :facts rows)))
          (is (= 2 (count (filter #(= :integer-less-than-or-equal (:role %)) rows))))
          (is (= comparison-id (:value incoming)))
          (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))))
      (let [core (subject (if (= placement :then) -1 1) 0)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            if-node (first (filter #(= :if (:source-operation %)) nodes))
            comparison-id ((:operands if-node) (if (= placement :then) 1 2))
            comparison (first (filter #(= comparison-id (:node-id %)) nodes))
            comparison-index (.indexOf ids comparison-id)
            left-id (first (:operands comparison)) left-index (.indexOf ids left-id)]
        ;; These invalid computations are deliberately in the unselected arm.
        (doseq [bad [(assoc-in core [:core-nodes left-index :attributes :value] nil)
                    (assoc-in core [:core-nodes left-index :attributes :value] 9223372036854775808N)
                    (assoc-in core [:core-nodes comparison-index :operands] [])
                    (assoc-in core [:core-nodes comparison-index :operands] [left-id left-id])
                    (-> core (assoc-in [:core-nodes left-index :type] :gravity.type/keyword)
                        (assoc-in [:core-nodes left-index :attributes :value] :bad/value)
                        (assoc-in [:type-facts left-id :type] :gravity.type/keyword)
                        (assoc-in [:type-facts left-id :type-id] :gravity.type/keyword))
                    (with-integer-comparison extension core
                      (if (= placement :then) :else :then) :integer-lte 0 0)]]
          (let [mir (build bad) rejected (run bad mir)]
            (is (= :passed (get-in rejected [:upstream-verification :status])))
            (is (= :rejected (:status rejected)))
            (is (= :bounded-execution-subset (get-in rejected [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution rejected)))))
        (let [mir (build core) result (run core mir)
              path [:functions (:entrypoint core) :blocks ((coordinates core mir) placement) :instructions 2]]
          (doseq [bad [(assoc-in mir (conj path :source-operation) :integer-lt)
                      (assoc-in mir (into path [:source :span :start]) 999)
                      (assoc-in mir (into path [:facts :safety-outcome-id]) :forged)
                      (assoc-in mir [:data-flow-graph :definitions "forged"] {})
                      (assoc-in mir [:control-flow-graph :join :incoming
                                     (if (= placement :then) 0 1) :value] left-id)]]
            (let [upstream (invoke 'sh12-verify-branch-join-mir core bad) rejected (run core bad)]
              (is (= :rejected (:status rejected)))
              (is (= (:diagnostics upstream) (:diagnostics rejected)))
              (is (nil? (:execution rejected)))))
          (doseq [candidate [(assoc-in result [:execution :value] :forged)
                             (assoc-in result [:projection :operations 2 :role] :integer-less-than)
                             (assoc-in result [:execution :fuel] 504)]]
            (is (= :rejected (:status (verify core mir candidate))))))))))

(deftest sh13-executes-the-bounded-integer-greater-than-family
  ;; The C11 capability helper's closed subtree is
  ;; (if (> (count granted) 0) (> (count proof-ids) 0) true).
  ;; Supplied count DATA is not evidence of capability/proof authority.
  (check-integer-condition
   :integer-gt :integer-greater-than
   [[0 0 :gravity.type/i64 false 22] [1 0 :gravity.type/i64 true 11]
    [0 1 :gravity.type/i64 false 22] [16 0 :gravity.type/i64 true 11]
    [17 16 :gravity.type/i64 true 11] [-1 0 :gravity.type/i64 false 22]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 false 22]
    [9223372036854775807 -9223372036854775808 :gravity.type/i64 true 11]
    [9223372036854775807 9223372036854775807 :gravity.type/bool false true]
    [1 0 :gravity.type/bool true false]])
  (check-selected-comparison-arm
   :integer-gt :integer-greater-than
   [[0 0 false] [1 0 true] [0 1 false] [16 0 true] [17 16 true]
    [-1 0 false] [-9223372036854775808 9223372036854775807 false]
    [9223372036854775807 -9223372036854775808 true]
    [9223372036854775807 9223372036854775807 false]])
  (doseq [extension [".gravity" ".qst"]]
    (doseq [[type a b] [[:gravity.type/keyword :phase/then :phase/else]
                       [:gravity.type/nil nil nil]]
            [left right expected] [[1 0 a] [0 0 b]]]
      (let [core (with-integer-comparison extension
                   (scalar-core (checked extension) true type a b)
                   :condition :integer-gt left right)
            mir (build core) result (run core mir)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :accepted (:status result)))
        (is (= expected (get-in result [:execution :value])))
        (is (= 505 (get-in result [:execution :fuel])))
        (is (= :passed (:status (verify core mir result)))))))
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (letfn [(subject [granted proof-count]
              (with-integer-comparison extension
                (with-integer-comparison extension
                  (scalar-core (checked extension) true :gravity.type/bool true true)
                  placement :integer-gt proof-count 0)
                :condition :integer-gt granted 0))]
      (doseq [[granted proof-count] [[0 0] [1 0] [1 1] [16 17] [17 16]
                                     [9223372036854775807 0]
                                     [-9223372036854775808 9223372036854775807]
                                     [9223372036854775807 -9223372036854775808]
                                     [9223372036854775807 9223372036854775807]]]
        (let [core (subject granted proof-count) mir (build core) result (run core mir)
              nodes (:core-nodes core) ids (mapv :node-id nodes)
              rows (get-in result [:projection :operations])
              condition (> granted 0) selected? (= placement (if condition :then :else))
              if-node (first (filter #(= :if (:source-operation %)) nodes))
              comparison-id ((:operands if-node) (if (= placement :then) 1 2))
              comparison (first (filter #(= comparison-id (:node-id %)) nodes))
              selected-ids (if selected? (conj (:operands comparison) comparison-id)
                             [((:operands if-node) (if condition 1 2))])
              incoming (first (filter #(= ((coordinates core mir) placement) (:predecessor %))
                                      (get-in mir [:control-flow-graph :join :incoming])))
              operations (into {} (for [block (vals (get-in mir [:functions (:entrypoint core) :blocks]))
                                       op (:instructions block)] [(:op-id op) op]))]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :accepted (:status result)))
          (is (= (if selected? (> proof-count 0) true) (get-in result [:execution :value])))
          (is (= (if selected? 503 505) (get-in result [:execution :fuel])))
          (is (= (into (subvec ids 0 3) selected-ids)
                 (mapv :operation (filter :operation (get-in result [:execution :trace])))))
          (is (= 9 (count rows)))
          (is (= ids (mapv :operation-id rows)))
          (is (= (mapv :source nodes) (mapv :source rows)))
          (is (= (mapv #(get-in operations [% :facts]) ids) (mapv :facts rows)))
          (is (= 2 (count (filter #(= :integer-greater-than (:role %)) rows))))
          (is (= comparison-id (:value incoming)))
          (is (= (:control-flow-graph mir) (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))))
      (let [core (subject (if (= placement :then) 0 1) 1)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            if-node (first (filter #(= :if (:source-operation %)) nodes))
            comparison-id ((:operands if-node) (if (= placement :then) 1 2))
            comparison (first (filter #(= comparison-id (:node-id %)) nodes))
            comparison-index (.indexOf ids comparison-id)
            left-id (first (:operands comparison)) left-index (.indexOf ids left-id)]
        (doseq [bad [(assoc-in core [:core-nodes left-index :attributes :value] nil)
                    (assoc-in core [:core-nodes left-index :attributes :value] -9223372036854775809N)
                    (assoc-in core [:core-nodes comparison-index :operands] [left-id])
                    (with-integer-comparison extension core
                      (if (= placement :then) :else :then) :integer-gt 1 0)]]
          (let [mir (build bad) rejected (run bad mir)]
            (is (= :passed (get-in rejected [:upstream-verification :status])))
            (is (= :rejected (:status rejected)))
            (is (= :bounded-execution-subset (get-in rejected [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution rejected)))))
        (let [mir (build core) result (run core mir)
              path [:functions (:entrypoint core) :blocks ((coordinates core mir) placement) :instructions 2]]
          (doseq [bad [(assoc-in mir (conj path :source-operation) :integer-lt)
                      (assoc-in mir (conj path :effects) #{:io/write})
                      (assoc-in mir (into path [:facts :safety-outcome-id]) :forged)
                      (assoc-in mir [:control-flow-graph :join :incoming
                                     (if (= placement :then) 0 1) :value] left-id)]]
            (let [upstream (invoke 'sh12-verify-branch-join-mir core bad) rejected (run core bad)]
              (is (= :rejected (:status rejected)))
              (is (= (:diagnostics upstream) (:diagnostics rejected)))
              (is (nil? (:execution rejected)))))
          (doseq [candidate [(assoc-in result [:execution :value] :forged)
                             (assoc-in result [:projection :operations 2 :role] :integer-less-than)
                             (assoc-in result [:execution :fuel] 504)]]
            (is (= :rejected (:status (verify core mir candidate)))))))
      (let [base (scalar-core (checked extension) true :gravity.type/bool false true)
            arm (with-integer-comparison extension base placement :integer-gt 1 0)]
        (doseq [bad (concat
                    (mapcat (fn [operation]
                              [(with-integer-comparison extension arm :condition operation 0 0)
                               (with-integer-comparison extension
                                 (with-integer-comparison extension base placement operation 0 0)
                                 :condition :integer-gt 1 0)])
                            [:integer-eq :integer-lt :integer-lte])
                    [(with-scalar-truthiness extension arm :gravity.type/bool true)
                     (with-integer-comparison extension
                       (with-two-expression-do extension base placement
                         :gravity.type/bool false :gravity.type/bool true)
                       :condition :integer-gt 1 0)
                     (with-integer-comparison extension
                       (with-scalar-let extension base placement) :condition :integer-gt 1 0)])]
          (let [mir (build bad) result (run bad mir)]
            (is (= :passed (get-in result [:upstream-verification :status])))
            (is (= :rejected (:status result)))
            (is (= :bounded-execution-subset (get-in result [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution result)))))))))
