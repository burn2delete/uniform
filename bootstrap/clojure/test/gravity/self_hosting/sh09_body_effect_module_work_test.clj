(ns gravity.self-hosting.sh09-body-effect-module-work-test
  "Owning module and producer work conservation over explicit child reports."
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- effect [function arguments]
  (apply (var-get (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf))
         function arguments))

(def ^:private module-ledger-stage-order
  '[body-ma-context body-va-context body-ae-context body-nt-context body-vp-context
    body-ip-context body-st-context body-mapp-context body-kp-context body-strp-context
    body-listp-context body-setp-context body-symp-context body-boolp-context body-nilp-context
    body-chp-context body-ane-context body-mc-context body-mg-context body-sc-context
    body-lc-context body-lq-context body-lp-context body-cq-context body-vn-context
    body-si-context body-mr-context body-lr-context body-ij-context body-nq-context
    body-eh-source-products body-throw-joins body-derive-module-nodes body-eh-verify
    body-tc-verify body-allowance-check])

(defn- scripted-module-ledger [failure resource? allowance]
  ;; Script only semantic child reports. The actual owning Gravity parent,
  ;; remaining-budget expressions, aliases and ledger helpers still execute.
  (effect 'body-effect-module-ledger-work [])
  (let [seam (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original (var-get seam) calls (atom []) ledger-calls (atom 0)
        weights (dissoc (zipmap module-ledger-stage-order (range 1 37)) 'body-lr-context)]
    (with-redefs-fn
      {seam (fn [runtime plan callee args scope]
              (when (= 'body-effect-reported-stage-work callee) (swap! ledger-calls inc))
              (cond
                (= 'body-ds-prefix-work callee) 7
                (= 'body-shape-valid? callee) (not= failure :shape)
                (= 'body-index callee) {}
                (= 'body-build-graph callee) {:functions {}}
                (contains? '#{body-function-joins-valid? body-call-joins-with-foreign-valid?
                              body-local-joins-valid?} callee) (not= failure :join)
                (= 'body-lr-context callee)
                (let [failed? (= failure callee)]
                  (is (= 4 (count args)))
                  (is (nil? (nth args 3))) ; This scripted request has no source forms.
                  (swap! calls conj {:callee callee :argument-count (count args)})
                  {:status (if failed? :rejected :accepted)
                   :reason (when failed? :selected-child-failure)
                   :context {:owner callee}})
                (or (= 'body-is-context callee) (contains? weights callee))
                (let [left (last args) failed? (= failure callee)
                      work (if (and resource? failed?) (inc left)
                               (if (= 'body-is-context callee) 11 (get weights callee)))]
                  (swap! calls conj {:callee callee :allowance left :work-count work})
                  {:status (if failed? :rejected :accepted)
                   :reason (when failed? (if resource? :body-deferred-work-limit :selected-child-failure))
                   :context {:owner callee} :parameter-value-premise nil
                   :nodes {} :groups {} :functions {} :rows [] :work-count work})
                :else (original runtime plan callee args scope)))}
      (fn []
        {:result (effect 'body-analyze-module
                         [{:canonical-core-artifact {} :b47-context {:authenticated-core-request {}}}
                          {} {} allowance])
         :calls @calls :ledger-calls @ledger-calls}))))

(deftest owning-module-reports-each-invoked-child-once-through-every-failure
  ;; Ledger routing fixtures do not stand in for semantic producer acceptance.
  (let [allowance 100000 ledger (effect 'body-effect-module-ledger-work [])
        prefix (+ 7 11 ledger)
        expected (vec (cons 'body-is-context module-ledger-stage-order))
        weights (dissoc (zipmap module-ledger-stage-order (range 1 37)) 'body-lr-context)
        complete (scripted-module-ledger nil false allowance)]
    (is (= 209 ledger))
    (is (= :accepted (get-in complete [:result :status])))
    (is (= expected (mapv :callee (:calls complete))))
    (is (= 1 (:ledger-calls complete)))
    (is (= (+ prefix (reduce + (vals weights))) (get-in complete [:result :work-count])))
    (is (= (- allowance 7) (get-in complete [:calls 0 :allowance])))
    (doseq [[index call] (map-indexed vector (rest (:calls complete)))]
      (if (= 'body-lr-context (:callee call))
        (is (= {:callee 'body-lr-context :argument-count 4} call))
        (is (= (- allowance prefix (reduce + (keep :work-count (take index (rest (:calls complete))))))
               (:allowance call)))))
    (doseq [failure expected resource? [false true]
            :when (not (and resource? (= failure 'body-lr-context)))]
      (let [{:keys [result calls ledger-calls]} (scripted-module-ledger failure resource? allowance)
            invoked (inc (.indexOf expected failure))
            paid (+ 7 (if (= failure 'body-is-context) 0 ledger)
                    (reduce + (keep :work-count calls)))]
        (is (= :rejected (:status result)))
        (is (= (if resource? :body-deferred-work-limit :selected-child-failure) (:reason result)))
        (is (= (subvec expected 0 invoked) (mapv :callee calls)))
        (is (= paid (:work-count result)))
        (is (= (cond (= failure 'body-is-context) 0
                     (> invoked 31) 2
                     :else 1) ledger-calls))
        (when resource? (is (= (inc allowance) (:work-count result))))))
    (let [short (scripted-module-ledger nil false (dec prefix))]
      (is (= ['body-is-context] (mapv :callee (:calls short))))
      (is (= :body-deferred-work-limit (get-in short [:result :reason])))
      (is (= prefix (get-in short [:result :work-count])))
      (is (zero? (:ledger-calls short))))
    (let [funded (scripted-module-ledger 'body-ma-context true prefix)]
      (is (= ['body-is-context 'body-ma-context] (mapv :callee (:calls funded))))
      (is (= :body-deferred-work-limit (get-in funded [:result :reason])))
      (is (= (inc prefix) (get-in funded [:result :work-count])))
      (is (= 1 (:ledger-calls funded))))
    (let [shape (scripted-module-ledger :shape false allowance)
          prefix-limit (scripted-module-ledger nil false 6)
          join (scripted-module-ledger :join false allowance)]
      (is (= :incomplete-canonical-core-coverage (get-in shape [:result :reason])))
      (is (= 7 (get-in shape [:result :work-count]) (get-in prefix-limit [:result :work-count])))
      (is (= [] (:calls shape) (:calls prefix-limit)))
      (is (zero? (:ledger-calls shape)))
      (is (zero? (:ledger-calls prefix-limit)))
      (is (= 1 (:ledger-calls join)))
      (is (= :incoherent-function-or-call-body-type-origin-join (get-in join [:result :reason])))
      (is (= (vec (cons 'body-is-context (take 30 module-ledger-stage-order)))
             (mapv :callee (:calls join))))
      (is (= (+ prefix (reduce + (keep weights (take 30 module-ledger-stage-order))))
             (get-in join [:result :work-count]))))))

(defn- producer-ledger-fixtures []
  (mapv (fn [index]
          (let [key (mapv #(str "sha256:" (format "%064x" (+ 5000 (* 2 index) %))) [0 1])]
            {:producer-sh06-artifact-id (first key) :producer-sh07-artifact-id (second key)
             :module-key key :request {:producer-index index} :facts {:producer-index index}}))
        (range 3)))

(defn- scripted-producer-ledger [entries allowance weights failure resource?]
  (effect 'body-effect-module-ledger-work [])
  (let [seam (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        instruction-seam (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-instruction-scoped)
        original (var-get seam) original-instruction (var-get instruction-seam)
        calls (atom []) copies (atom 0)]
    (with-redefs-fn
      {seam (fn [runtime plan callee args scope]
              (if (= 'body-analyze-module callee)
                (let [index (:producer-index (first args)) left (last args)
                      failed? (= failure index) work (if (and failed? resource?) (inc left) (get weights index))]
                  (swap! calls conj {:index index :allowance left :work-count work})
                  (if failed?
                    {:status :rejected :reason (if resource? :body-deferred-work-limit :selected-producer-failure)
                     :effect :retained-child-detail :work-count work}
                    {:status :accepted :graph {:producer-index index} :work-count work}))
                (original runtime plan callee args scope)))
       instruction-seam
       (fn [runtime plan env instruction scope]
         ;; Exact-three assoc is executed inline, before generic dispatch.
         (let [argument (first (:args instruction))
               entry-copy? (and (= :builtin-call (:op instruction)) (= 'assoc (:function instruction))
                                (= :local (:op argument))
                                (some #(identical? (get env (:name argument)) %) entries))
               value (original-instruction runtime plan env instruction scope)]
           (when entry-copy? (swap! copies inc))
           value))}
      (fn [] {:result (effect 'body-analyze-producers-work [entries allowance])
              :calls @calls :entry-copies @copies}))))

(deftest producer-analysis-keeps-one-caller-pool-and-pays-every-publication
  ;; Scripted module reports exercise the actual producer worker and native
  ;; bank construction, without conferring authentication on these fixtures.
  (let [entries (producer-ledger-fixtures) allowance 200000 weights [70000 11 13]
        map-work (fn [size] (+ 1 (* size size) (* 2 size)))
        key-work (mapv #(get (effect 'body-lsi-first-data-work [(:module-key %) 1048576]) :work-count) entries)
        schedule (loop [index 0 used 26 out []]
                   (if (= index 3) out
                     (let [keyed (+ used 9 (get key-work index))
                           published (+ keyed (get weights index) (map-work 6) (map-work (inc index)))]
                       (recur (inc index) published (conj out {:keyed keyed :published published})))))
        run #(scripted-producer-ledger entries % weights nil false)
        accepted (run allowance) result (:result accepted) total (:work-count result)]
    (is (= [149 149 149] key-work))
    (is (= :accepted (:status result)))
    (is (= 3 (:entry-copies accepted) (count (:bank result))))
    (is (= (mapv #(hash-map :index % :allowance (- allowance (:keyed (get schedule %))) :work-count (get weights %))
                 (range 3)) (:calls accepted)))
    (is (> (get-in accepted [:calls 0 :allowance]) 65536))
    (is (= (:published (last schedule)) total))
    (is (= result (:result (run total))))
    (is (= :body-deferred-work-limit (get-in (run (dec total)) [:result :reason])))
    (doseq [failure (range 3) resource? [false true]]
      (let [{:keys [result calls entry-copies]}
            (scripted-producer-ledger entries allowance weights failure resource?)
            keyed (:keyed (get schedule failure))
            expected (if resource? (inc allowance) (+ keyed (get weights failure) (map-work 6)))]
        (is (= :rejected (:status result)))
        (is (= (if resource? :body-deferred-work-limit :selected-producer-failure) (:reason result)))
        (is (= (vec (range (inc failure))) (mapv :index calls)))
        (is (= failure entry-copies))
        (is (= expected (:work-count result)))
        (is (= (:module-key (get entries failure)) (:producer-module result)))
        (when-not resource?
          (is (= :retained-child-detail (:effect result)))
          (is (= (:module-key (get entries failure)) (:producer-module result)))
          (is (identical? (:facts (get entries failure)) (:producer-facts result))))))
    (let [first-boundary (:published (first schedule))
          short (run (dec first-boundary))]
      (is (= :body-deferred-work-limit (get-in short [:result :reason])))
      (is (= first-boundary (get-in short [:result :work-count])))
      (is (= 1 (count (:calls short))))
      (is (zero? (:entry-copies short))))
    (let [publication-boundary (+ (:keyed (first schedule)) (first weights) (map-work 6))
          short (scripted-producer-ledger entries (dec publication-boundary) weights 0 false)
          overrun (scripted-producer-ledger entries allowance [(inc allowance) 11 13] nil false)
          repeated (scripted-producer-ledger [(first entries) (first entries)] allowance weights nil false)]
      (is (= :body-deferred-work-limit (get-in short [:result :reason])))
      (is (= publication-boundary (get-in short [:result :work-count])))
      (is (not (contains? (:result short) :producer-facts)))
      (is (= :body-deferred-work-limit (get-in overrun [:result :reason])))
      (is (= 1 (count (:calls overrun))))
      (is (zero? (:entry-copies overrun)))
      (is (= :accepted (get-in repeated [:result :status])))
      (is (= [0 0] (mapv :index (:calls repeated))))
      (is (= 2 (:entry-copies repeated)))
      (is (= 1 (count (get-in repeated [:result :bank]))))
      (is (= (+ 26 (* 2 (+ 9 (first key-work) (first weights) (map-work 6)))
                (map-work 1) (map-work 2)) (get-in repeated [:result :work-count]))))
    (let [empty-result (effect 'body-analyze-producers-work [[] 26])]
      (is (= {:status :accepted :bank {} :work-count 26} empty-result))
      (is (= :body-deferred-work-limit (:reason (effect 'body-analyze-producers-work [[] 25]))))
      (is (= empty-result (effect 'body-analyze-producers [[]]))))))

(deftest producer-key-domain-is-complete-before-child-execution-or-native-hashing
  (let [entries (producer-ledger-fixtures) callbacks (atom 0) realizations (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "producer key hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "producer key equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "producer key printing"))))
        delayed (lazy-seq (swap! realizations inc) (list :must-not-realize))]
    (doseq [key [opaque delayed [opaque :id] [:id opaque] [:id delayed]]]
      (let [{:keys [result calls entry-copies]}
            (scripted-producer-ledger [(assoc (first entries) :module-key key)] 1048576 [7] nil false)]
        (is (= :untrusted-or-transitive-body-producer (:reason result)))
        (is (= [] calls))
        (is (zero? entry-copies))
        (is (not (contains? result :producer-facts)))))
    (doseq [key [nil 7 [:only-one]]]
      (let [{:keys [result calls entry-copies]}
            (scripted-producer-ledger [(assoc (first entries) :module-key key)] 1048576 [7] nil false)]
        (is (= :accepted (:status result)))
        (is (= [0] (mapv :index calls)))
        (is (= 1 entry-copies))
        (is (contains? (:bank result) key))))
    (let [short (scripted-producer-ledger entries 34 [7 11 13] nil false)]
      (is (= :body-deferred-work-limit (get-in short [:result :reason])))
      (is (= 35 (get-in short [:result :work-count])))
      (is (= [] (:calls short))))
    (is (zero? @callbacks))
    (is (zero? @realizations))))

(defn- normalized-atomic-producer-entry [ordinal]
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        invoke (var-get (ns-resolve upstream 'invoke-c7))
        base ((var-get (ns-resolve upstream 'atomic-equality-input))
              [:gravity.type/string :gravity.type/string])
        sha (var-get (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'sha))
        ids (zipmap (keys (:nodes base)) (map sha (range (+ 7000 (* 16 ordinal)) (+ 7010 (* 16 ordinal)))))
        nodes (mapv (fn [node]
                      (-> node (update :node-id ids) (update :children #(mapv ids %))
                          (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
                    (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base)
                    {:nodes nodes :calls [call] :call-edges [edge] :root-core-node-ids [(ids :call)]
                     :definitions [] :lexical-bindings [] :match-branch-records [] :match-pattern-records []
                     :match-decision-skeletons [] :evaluation-order [] :recursion-components []
                     :error-transfers [] :error-handlers []})
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derived (invoke 'sh08-ae-derive [core (last nodes) call edge (into {} (map (juxt :node-id identity) nodes))
                                      (:bindings base) types (:forms base) 65536])
        descriptor (:descriptor derived)
        facts (merge {:module (:module core)
                      :type-table (assoc types (:operator-node-id call) descriptor (:core-node-id call) (:type derived))
                      :function-type-table [] :local-binding-facts []
                      :constraint-ledger [(invoke 'sh08-ae-constraint [descriptor])]
                      :call-type-facts [(invoke 'sh08-ae-call-fact [descriptor (:module core)])]}
                     (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]))
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                          :evaluation-order :lexical-bindings :match-branch-records
                                          :match-decision-skeletons :match-pattern-records])
                        :nodes (effect 'body-throw-semantic-nodes [core]))
        key [(sha (+ 8000 (* 2 ordinal))) (sha (+ 8001 (* 2 ordinal)))]]
    (is (= :accepted (:status derived)))
    {:producer-sh06-artifact-id (first key) :producer-sh07-artifact-id (second key) :module-key key
     :request {:canonical-core-artifact (assoc core :identity-preimage preimage)
               :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))
                                                         :binding-table (vec (vals (:bindings base)))}}}
     :facts facts}))

(deftest producer-worker-preserves-real-normalized-module-graphs-and-default-wrapper
  ;; Actual C7 derivation and Effects module analysis over normalized DATA;
  ;; these entries do not claim producer-envelope or source authentication.
  (let [entries (mapv normalized-atomic-producer-entry [0 1])
        run #(effect 'body-analyze-producers-work [entries %])
        accepted (run 1048576) total (:work-count accepted)
        legacy (effect 'body-analyze-producers [entries])
        explicit (run 65536)]
    (is (= :accepted (:status accepted)))
    (is (= 2 (count (:bank accepted))))
    (is (= legacy explicit))
    (when (= :accepted (:status accepted))
      (is (= accepted (run total)))
      (is (= :body-deferred-work-limit (:reason (run (dec total)))))
      (doseq [entry entries]
        (let [analyzed (effect 'body-analyze-module [(:request entry) (:facts entry) {} 1048576])]
          (is (= :accepted (:status analyzed)))
          (is (= (:graph analyzed) (get-in accepted [:bank (:module-key entry) :graph])))
          (is (= (assoc entry :graph (:graph analyzed)) (get-in accepted [:bank (:module-key entry)])))))
      (when (= :accepted (:status explicit)) (is (= (:bank accepted) (:bank explicit)))))))
