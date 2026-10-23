(ns gravity.self-hosting.sh12-checked-i64-add-producer-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

;; Current original producers, not a supplied Core/MIR or frozen admission.
(defn existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn invoke [name & args]
  ((existing 'invoke) (existing 'c11-plan) name (vec args)))

(defn producer [name & args]
  ((existing 'invoke) (existing 'parameter-c6-plan) name (vec args)))

(defn fixture [disposition extension]
  (str (.resolve @(existing 'root)
         (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/"
              disposition "/coordinator-checked-i64-add-producer." extension))))

(defn products [path text binding]
  ((existing 'parameter-source-products) path text binding
   (str (.resolve @(existing 'root) "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))))

(def current-pair
  (delay
    (let [path (fixture "accepted" "gravity") text (slurp path)
          first-product (products path text nil)
          binding (:compiled-binding-data first-product)
          raw (producer 'c6-c10-build-checked-core-template (:envelope first-product))
          fresh (products path text binding)
          replay (producer 'c6-c10-build-checked-core-template (:envelope fresh))
          verification (producer 'c6-c10-verify-checked-core-template
                         (:envelope fresh) (:artifact-template raw) (:digest-requests replay))
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! path raw)
          context (bootstrap/p15-s23-stage2-gravity-checked-core-context path text :jvm)
          core (bootstrap/p15-s23-c6c10-public-artifact context binding (:ingress first-product) sealed)]
      {:path path :text text :products first-product :binding binding
       :raw raw :fresh fresh :replay replay :verification verification :core core})))

(deftest sh12-checked-i64-add-source-shape
  ;; Syntax/byte parity only. This selector executes no compiler plan.
  (doseq [disposition ["accepted" "rejected"]]
    (let [g (slurp (fixture disposition "gravity")) q (slurp (fixture disposition "qst"))]
      (is (= g q))
      (is (every? #(< (int %) 128) g))
      (let [[declaration function] (read-string (str "[" g "]"))]
        (is (= 'defn (first function)))
        (is (= '[left :- I64 right :- I64] (nth function 2)))
        (is (= '(+ left right) (nth function 3)))
        (is (= (if (= disposition "accepted") '(:effects #{:error/raise}) '(:effects #{}))
               (first (filter #(and (seq? %) (= :effects (first %))) declaration))))))))

(deftest sh12-checked-i64-add-produces-original-bound-facts
  (let [{:keys [raw replay verification core products fresh]} @current-pair
        nodes (:core-nodes core) ids (mapv :node-id nodes)
        add (get nodes 4) root (get nodes 5) check (get-in add [:safety :check])]
    (is (= :accepted (:status raw)))
    (is (= raw replay))
    (is (= (:envelope products) (:envelope fresh)))
    (is (= :passed (:status verification)))
    (is (= :bounded-checked-i64-add-hosted-safe-jvm-source (get-in products [:envelope :scope])))
    (is (= :checked-i64-add-v1 (get-in core [:scope :family])))
    (is (= [:parameter :parameter :local :local :integer-add :function] (mapv :source-operation nodes)))
    (is (= 6 (count nodes) (count (set ids)) (count (:source-origin-table core))))
    (is (= [:gravity/i64 :gravity/i64] (get-in root [:type :params])))
    (is (= :gravity/i64 (:type add) (get-in root [:type :return])))
    (is (= #{:error/raise} (get-in root [:type :latent-effects])
           (get-in core [:effect-facts (get ids 5) :latent])
           (get-in core [:effect-facts (get ids 5) :transitive])
           (get-in core [:effect-facts (get ids 5) :residual])))
    (is (= #{:error/numeric} (get-in root [:type :throws])
           (get-in core [:effect-graph :functions (get ids 5) :throws])))
    (is (= :runtime-checked (get-in add [:safety :outcome])
           (get-in core [:safety-facts (get ids 4) :outcome])))
    (is (nil? (get-in add [:safety :proof])))
    (is (nil? (get-in core [:proof-records (get ids 4)])))
    (is (= (subvec ids 2 4) (get-in check [:predicate :operand-ids])))
    (is (= [64 :signed :checked :error/numeric #{:error/raise}]
           ((juxt :bit-width :signedness :numeric-mode :failure :effects-introduced) check)))
    (is (= (:source add) (:source check)))
    (is (= (:check-id check) (get-in core [:safety-facts (get ids 4) :runtime-check])))
    (is (= 5 (count (get-in core [:dependency-order-graph :edges]))))
    (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %) ids))
    (is (true? (:clojure-seed-boundary? core)))
    (is (false? (:self-hosted? core)))))

(deftest sh12-checked-i64-add-replay-rejects-fact-and-check-substitution
  (let [{:keys [raw replay fresh products verification]} @current-pair
        template (:artifact-template raw) nodes (:core-nodes template)
        add-id (:node-id (get nodes 4)) root-id (:node-id (get nodes 5))
        verify #(producer 'c6-c10-verify-checked-core-template
                  (:envelope fresh) % (:digest-requests replay))]
    (is (= raw replay))
    (is (= (:envelope products) (:envelope fresh)))
    (is (= :passed (:status verification)))
    (doseq [candidate [(assoc-in template [:core-nodes 4 :attributes :numeric-mode] :wrapping)
                       (assoc-in template [:core-nodes 4 :operands] (vec (reverse (:operands (get nodes 4)))))
                       (assoc-in template [:core-nodes 4 :safety :outcome] :proven-safe)
                       (update-in template [:core-nodes 4 :safety] dissoc :check)
                       (assoc-in template [:core-nodes 4 :safety :check :bit-width] 32)
                       (assoc-in template [:core-nodes 4 :safety :check :failure] :panic/numeric)
                       (assoc-in template [:core-nodes 4 :source :generated-origin] [:forged])
                       (assoc-in template [:type-facts add-id :type] :gravity/bool)
                       (assoc-in template [:effect-facts root-id :residual] #{})
                       (update-in template [:semantic-provenance :binding-pins] dissoc :builder-source-hash)]]
      (is (= :rejected (:status (verify candidate)))))))

(deftest sh12-checked-i64-add-rejects-undeclared-error-effect
  (let [{:keys [binding]} @current-pair
        path (fixture "rejected" "gravity") input (products path (slurp path) binding)
        result (producer 'c6-c10-build-checked-core-template (:envelope input))]
    (is (= :bounded-checked-i64-add-hosted-safe-jvm-source (get-in input [:envelope :scope])))
    (is (= :rejected (:status result)))
    (is (= "C8-UNDECLARED" (name (get-in result [:diagnostics 0 :rule]))))
    (is (nil? (:artifact-template result)))))

(deftest sh12-checked-i64-add-rejects-original-body-and-mode-mismatches
  (let [{:keys [products]} @current-pair input (:envelope products)
        build #(producer 'c6-c10-build-checked-core-template %)]
    ;; Mutated transport is rejected, never used as a fake accepted producer.
    (doseq [candidate [(assoc input :scope :bounded-pure-hosted-safe-jvm-source)
                       (assoc-in input [:stage2-plan :functions 'main :instructions 0 :args] [])
                       (update-in input [:stage2-plan :functions 'main :instructions 0 :args] #(vec (reverse %)))
                       (assoc-in input [:stage2-plan :functions 'main :instructions 0 :args 1 :name] 'left)
                       (assoc-in input [:stage2-plan :functions 'main :arity] 1)
                       (assoc-in input [:authoritative-module :safety] :unsafe)
                       (assoc-in input [:resolved-parameter :kind] :gravity/c6-c10-resolved-two-bool-parameters-v1)]]
      (is (= :rejected (:status (build candidate)))))))

;; SUPPLIED boundary metadata only: these tiny records have no Core rows or
;; source authentication. Only current-pair above exercises an original producer.
(defn- envelope-data [checked?]
  {:status :accepted :diagnostics [] :digest-requests []
   :digest-graph-root {:digest-ref 0} :digest-graph-roots [{:digest-ref 0}]
   :authority {:semantic-owner :gravity-source :host-role :generic-validation-hashing-and-instantiation
               :scope (if checked? :bounded-checked-i64-add-c6-c10 :bounded-pure-c6-c10) :self-hosted? false}
   :artifact-template
   (if checked?
     {:kind :gravity/p15-s23-stage2-gravity-checked-core-artifact :schema-version 1
      :status :complete-for-checked-i64-add-hosted-safe-jvm-slice :profile :hosted :source-target :jvm
      :scope {:artifact :gravity/c6-c10-checked-core-scope-contract
              :family :checked-i64-add-v1 :profiles [:hosted] :safety-modes [:safe] :source-targets [:jvm]
              :source-operations [:parameter :local :integer-add :function] :scalar-literals []
              :intrinsic-effects #{:error/raise} :intrinsic-capabilities #{}
              :ownership :persistent-immutable :safety :runtime-checked :whole-language? false}}
     {:kind :gravity/test-artifact :value [1 2]})})

(defn- boundary-error [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error
         (select-keys (ex-data error) [:id :missing-fact]))))

(deftest sh12-checked-i64-add-envelope-authority-data-kernel
  (let [checked (envelope-data true) pure (envelope-data false)
        validate #(bootstrap/p15-s23-c6c10-validate-builder-result-envelope! "boundary-data.gravity" %)]
    (is (identical? pure (validate pure)))
    (is (identical? checked (validate checked)))
    (doseq [candidate
            (concat [(assoc checked :authority (:authority pure)) (assoc pure :authority (:authority checked))]
              (for [[path value] [[[:kind] :gravity/test-artifact] [[:schema-version] 2]
                                  [[:status] :complete-for-bounded-pure-hosted-safe-jvm-slice]
                                  [[:profile] :native] [[:source-target] :wasm]
                                  [[:scope :family] :other-family] [[:scope :artifact] :other-scope]
                                  [[:scope :profiles] [:native]] [[:scope :safety-modes] [:unsafe]]
                                  [[:scope :source-targets] [:wasm]] [[:scope :source-operations] [:integer-add]]
                                  [[:scope :scalar-literals] [:integer]] [[:scope :intrinsic-effects] #{}]
                                  [[:scope :intrinsic-capabilities] #{:host/ambient}] [[:scope :ownership] :mutable]
                                  [[:scope :safety] :proven-safe] [[:scope :whole-language?] true]]]
                (assoc-in checked (into [:artifact-template] path) value))
              (for [field (keys (:authority checked))] (update checked :authority dissoc field))
              (for [[field value] [[:semantic-owner :host] [:host-role :semantic-authority]
                                  [:scope :unknown] [:self-hosted? true]]]
                (assoc-in checked [:authority field] value))
              [(assoc-in checked [:authority :extra] true)
               (assoc-in checked [:artifact-template :scope :extra] true)])]
      (is (= {:id "C6-VERIFY" :missing-fact :exact-gravity-builder-result-envelope}
             (boundary-error #(validate candidate)))))))

(deftest sh12-checked-i64-add-envelope-carrier-and-rejection-data-kernel
  (let [checked (envelope-data true)
        rejected {:status :rejected :artifact-template nil :fatal? true :diagnostics [{:rule "C6-VERIFY"}]
                  :containment {} :digest-requests [] :digest-graph-roots []}
        validate #(bootstrap/p15-s23-c6c10-validate-builder-result-envelope! "boundary-data.gravity" %)]
    (is (identical? rejected (validate rejected)))
    (is (= (assoc rejected :propagated-upstream? true) (validate (assoc rejected :propagated-upstream? true))))
    (doseq [[candidate fact]
            [[(assoc checked :digest-requests (list)) :bounded-digest-request-vector]
             [(assoc checked :digest-requests (with-meta [] {:untrusted true})) :bounded-digest-request-vector]
             [(assoc-in checked [:artifact-template :scope] (with-meta {} {:untrusted true})) :metadata-free-canonical-value]
             [(assoc checked :authority (with-meta {} {:untrusted true})) :metadata-free-canonical-value]
             [(assoc checked :artifact-template (Object.)) :canonical-value-domain]
             [(assoc checked :extra true) :exact-gravity-builder-result-envelope]
             [(dissoc checked :authority) :exact-gravity-builder-result-envelope]
             [(assoc checked :diagnostics [{}]) :exact-gravity-builder-result-envelope]
             [(assoc checked :diagnostics (list)) :exact-gravity-builder-result-envelope]
             [(assoc rejected :fatal? false) :exact-gravity-builder-result-envelope]
             [(assoc rejected :artifact-template {}) :exact-gravity-builder-result-envelope]
             [(assoc rejected :diagnostics []) :exact-gravity-builder-result-envelope]
             [(assoc rejected :containment nil) :exact-gravity-builder-result-envelope]
             [(assoc rejected :authority (:authority checked)) :exact-gravity-builder-result-envelope]]]
      (is (= {:id "C6-VERIFY" :missing-fact fact} (boundary-error #(validate candidate)))))))

(deftest sh12-checked-i64-add-pure-transport-sealer-data-kernel
  ;; Generic test artifact transport only; never a fabricated original Core.
  (let [path "boundary-data.gravity" base (:artifact-template (envelope-data false))
        request (fn [ordinal preimage dependencies]
                  {:algorithm :sha256 :encoding :gravity/canonical-edn-v1 :key ordinal :ordinal ordinal
                   :preimage preimage :depends-on dependencies})
        root-request #(request %1 {:domain :gravity/c6-c10-checked-core-artifact-v1 :semantic-artifact %2} %3)
        raw (assoc (envelope-data false) :artifact-template (assoc base :artifact-id {:digest-ref 0})
                   :digest-requests [(root-request 0 base [])])
        seal #(bootstrap/p15-s23-c6c10-seal-digest-request-result! path %)
        sealed (seal raw)]
    (is (= :accepted (:status sealed)))
    (is (= (:authority raw) (:authority sealed)))
    (is (= (first (:root-digests sealed)) (get-in sealed [:sealed-artifact-template :artifact-id])))
    (is (= {:id "C6-VERIFY" :missing-fact :root-bound-gravity-artifact-template}
           (boundary-error #(seal (assoc-in raw [:artifact-template :value] [2 1])))))
    (let [seed {:seed 1} seed-digest (bootstrap/p15-s23-c6c10-canonical-digest path seed)
          collision {:kind :gravity/test-artifact :value {{:digest-ref 0} :from-ref seed-digest :from-digest}}
          raw (assoc raw :artifact-template (assoc collision :artifact-id {:digest-ref 1})
                     :digest-graph-root {:digest-ref 1} :digest-graph-roots [{:digest-ref 1}]
                     :digest-requests [(request 0 seed []) (root-request 1 collision [0])])]
      (is (= {:id "C6-VERIFY" :missing-fact :collision-free-resolved-map-keys}
             (boundary-error #(seal raw)))))))

(defn- original-node-kernel []
  ;; Exact original defn forms on supplied DATA, not full source admission.
  (let [path (str (.resolve @(existing 'root) (existing 'parameter-c6-source)))
        names '#{c6c10-vector-contains? c6c10-conj-unique c6c10-exact-digest-ref?
                 c6c10-insert-ordinal c6c10-sort-ordinals c6c10-collect-ref-sequence
                 c6c10-collect-ref-map c6c10-collect-digest-refs c6c10-digest-ref
                 c6c10-digest-intern-sequence c6c10-digest-intern-map-entries
                 c6c10-digest-intern-key c6c10-request-key-for-preimage c6c10-add-digest-request
                 c6c10-merge-map-keys c6c10-merge-maps c6c10-safety-basis c6c10-persistent-ownership
                 c6c10-empty-product c6c10-checked-add-effects c6c10-checked-add-check c6c10-add-node-with-origin}
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? names (second %))) forms)
        primitives '#{defn if let = < + get count keys first rest contains? conj assoc set
                      map? vector? set? seq? boolean? integer? string? symbol? keyword? char?}
        callees (set (for [form selected expression (tree-seq coll? seq form)
                          :when (and (seq? expression) (symbol? (first expression)))] (first expression)))
        text (str "(ns gravity.test.original-node-kernel (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        _ (assert (and (= 22 (count selected)) (= names (set (map second selected))) (< (count text) 20000)
                       (every? #(or (contains? names %) (contains? primitives %)) callees)))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "original-node-kernel.gravity" text)]
    (fn [name & arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
        {:engine :sh12-original-node-data-kernel :compiler-artifact-plan? true} plan name (vec arguments)))))

(deftest sh12-checked-i64-add-original-node-builder-ownership-graph-kernel
  (let [call (original-node-kernel) path "original-node-kernel.gravity"
        origin (call 'c6c10-add-digest-request {:requests [] :next-ordinal 0} {:origin :supplied-data})
        source {:origin-id (:ref origin) :span {:start 0 :end 1} :plan-path [:data]}
        origins {:origin-id (:ref origin) :semantic {:origin :supplied-data} :raw {} :source source}
        build (fn [product operation operands node-path]
                (call 'c6c10-add-node-with-origin product {:source-content-hash "supplied-data"}
                      {:span (:span source)} node-path :expression operation true 1 operands
                      {:numeric-mode :checked} :gravity/i64 :value {} nil nil origins))
        first-node (build (call 'c6c10-empty-product (:state origin)) :literal [] [:left])
        product (build first-node :integer-add [(:result-node-id first-node) (:result-node-id first-node)] [:add])
        nodes (:nodes product) node (second nodes) check-id (get-in node [:safety :check :check-id])
        check-ordinal (:digest-ref check-id) owner-ordinal (dec check-ordinal)
        template (assoc (:artifact-template (envelope-data true)) :core-nodes nodes :source-origin-table (:origin-table product))
        root (call 'c6c10-add-digest-request (:state product)
                   {:domain :gravity/c6-c10-checked-core-artifact-v1 :semantic-artifact template})
        raw (assoc (envelope-data true) :artifact-template (assoc template :artifact-id (:ref root))
                   :digest-requests (get-in root [:state :requests])
                   :digest-graph-root (:ref root) :digest-graph-roots [(:ref root)])
        requests (:digest-requests raw) preimage (get-in requests [check-ordinal :preimage])
        validate #(bootstrap/p15-s23-c6c10-validate-digest-request-graph! path %)
        seal #(bootstrap/p15-s23-c6c10-seal-digest-request-result! path %)]
    (is (= 8 (count requests) (:request-count (validate raw))))
    (is (= 2 (count nodes) (:plan-node-count product)))
    (is (= 1 (:maximum-depth product)))
    (is (= {:digest-ref owner-ordinal} (:ownership-digest preimage)))
    (is (= (:node-id node) (:operation-id preimage) (get-in requests [owner-ordinal :preimage :core-node-id])))
    (is (= (:ref origin) (get-in requests [owner-ordinal :preimage :source-origin-id])))
    (is (= (:ownership node) (get-in requests [owner-ordinal :preimage :ownership])))
    (is (= :runtime-checked (get-in node [:safety :outcome])))
    (is (nil? (get-in node [:safety :proof])))
    (is (= {:digest-ref 2} (get-in nodes [0 :safety :proof :ownership-digest])))
    (is (= :proven-safe (get-in nodes [0 :safety :outcome])))
    (is (= :accepted (:status (seal raw))))
    (let [removed (update-in raw [:digest-requests check-ordinal :preimage] dissoc :ownership-digest)]
      (is (= {:id "C6-VERIFY" :missing-fact :exact-prior-digest-request-dependency-closure}
             (boundary-error #(validate removed))))
      (is (= {:id "C6-VERIFY" :missing-fact :root-reachable-digest-request-closure}
             (boundary-error #(validate (update-in removed [:digest-requests check-ordinal :depends-on]
                                         (fn [dependencies] (vec (remove #{owner-ordinal} dependencies)))))))))
    (let [changed (assoc-in raw [:digest-requests owner-ordinal :preimage :ownership :role] :substituted)
          before (seal raw) after (seal changed)]
      (is (= 8 (:request-count (validate changed))))
      (is (not= (get (:resolved-digests before) owner-ordinal) (get (:resolved-digests after) owner-ordinal)))
      (is (not= (get (:resolved-digests before) check-ordinal) (get (:resolved-digests after) check-ordinal)))
      (is (not= (:root-digests before) (:root-digests after))))))

(deftest sh12-checked-i64-add-original-replay-rejects-connected-ownership-substitution
  ;; Full original source/verifier; excluded from the cheap node DATA selector.
  (let [{:keys [raw fresh]} @current-pair requests (:digest-requests raw)
        check-ordinal (first (keep-indexed #(when (= :gravity/c6-c10-checked-i64-add-check-v1
                                                     (get-in %2 [:preimage :domain])) %1) requests))
        owner-ref (get-in requests [check-ordinal :preimage :ownership-digest])
        other-owner (first (keep-indexed #(when (and (= :gravity/c9-persistent-ownership-v1 (get-in %2 [:preimage :domain]))
                                                     (not= %1 (:digest-ref owner-ref))) {:digest-ref %1}) requests))
        wrong (-> raw (assoc-in [:digest-requests check-ordinal :preimage :ownership-digest] other-owner)
                  (assoc-in [:digest-requests check-ordinal :preimage :retained-original-ownership-digest] owner-ref)
                  (update-in [:digest-requests check-ordinal :depends-on] #(vec (sort (distinct (conj % (:digest-ref other-owner)))))))
        changed (assoc-in raw [:digest-requests (:digest-ref owner-ref) :preimage :ownership :role] :substituted)]
    (doseq [candidate [wrong changed]]
      (is (= (count requests) (:request-count (bootstrap/p15-s23-c6c10-validate-digest-request-graph! (:path @current-pair) candidate))))
      (let [report (producer 'c6-c10-verify-checked-core-template
                            (:envelope fresh) (:artifact-template candidate) (:digest-requests candidate))]
        (is (= :rejected (:status report)))
        (is (= :exact-digest-request-replay (get-in report [:diagnostics 0 :facts :missing-fact])))))))

(defn- original-diagnostic-kernel []
  ;; Exact original definitions; supplied diagnostic DATA, no source ingress.
  (let [path (str (.resolve @(existing 'root) (existing 'parameter-c6-source)))
        names '#{c6c10-vector-contains? c6c10-conj-unique c6c10-conj-all-unique c6c10-copy-vector c6c10-append
                 c6c10-exact-digest-ref? c6c10-insert-ordinal c6c10-sort-ordinals c6c10-collect-ref-sequence
                 c6c10-collect-ref-map c6c10-collect-digest-refs c6c10-digest-ref c6c10-digest-intern-sequence
                 c6c10-digest-intern-map-entries c6c10-digest-intern-key c6c10-request-key-for-preimage
                 c6c10-add-digest-request c6c10-merge-map-keys c6c10-merge-maps c6c10-literal-type-from-form-kind
                 c6c10-diagnostic-zero-span c6c10-diagnostic-primary c6c10-canonical-diagnostic-rule
                 c6c10-canonical-diagnostic-stage c6c10-diagnostic-involved-artifacts c6c10-diagnostic-related
                 c6c10-diagnostic-subject-request c6c10-rule-in-family? c6c10-observed-form-type
                 c6c10-diagnostic-family-facts c6c10-diagnostic-rejection}
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? names (second %))) forms)
        primitives '#{defn if let quote = < + get count keys first rest contains? conj assoc set
                      map? vector? set? seq? boolean? integer? string? symbol? keyword? char?}
        callees (set (for [form selected expression (tree-seq coll? seq form)
                          :when (and (seq? expression) (symbol? (first expression)))] (first expression)))
        text (str "(ns gravity.test.original-diagnostic-kernel (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        _ (assert (= 31 (count selected)) (pr-str {:selected-definitions (count selected)}))
        _ (assert (= names (set (map second selected))) (pr-str {:selected-names (mapv second selected)}))
        _ (assert (< (count text) 24000) (pr-str {:source-characters (count text)}))
        _ (assert (every? #(or (contains? names %) (contains? primitives %)) callees)
                  (pr-str {:unclosed-callees (vec (remove #(or (contains? names %) (contains? primitives %)) callees))}))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "original-diagnostic-kernel.gravity" text)]
    (fn [name & arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
        {:engine :sh12-original-diagnostic-data-kernel :compiler-artifact-plan? true} plan name (vec arguments)))))

(deftest sh12-checked-i64-add-original-diagnostic-rule-and-constructor-kernel
  (let [call (original-diagnostic-kernel)]
    (doseq [rule [:L2-BUILTIN-ARITY :C6-LOWERING-GAP :C6-CORE-SHAPE :C6-ORIGIN :C6-VERIFY
                  :C7-TYPE-MISMATCH :C7-VERIFY :C8-UNDECLARED :C8-VERIFY :C10-PROOF]]
      (is (= (name rule) (call 'c6c10-canonical-diagnostic-rule rule))))
    (is (= "external/existing-rule" (call 'c6c10-canonical-diagnostic-rule "external/existing-rule")))
    (doseq [rule [:unknown :C8-CAPABILITY nil 7]]
      (is (= :unassigned-diagnostic-rule (call 'c6c10-canonical-diagnostic-rule rule))))
    (let [source {:source-content-hash "supplied-diagnostic-data" :syntax-id :namespace-syntax
                  :span {:source "supplied-diagnostic-data" :start 0 :end 1}
                  :origin-id :source-origin :generated-origin [:generated-origin]}
          facts {:missing-fact :declared-numeric-error-effect :observed-effects #{}}
          build #(call 'c6c10-diagnostic-rejection {:requests [] :next-ordinal 0}
                       :C8-UNDECLARED :effect-check :diagnostic/c8-undeclared source % :declare-numeric-error-effect)
          raw (build facts) diagnostic (first (:diagnostics raw))
          sealed (bootstrap/p15-s23-c6c10-seal-digest-request-result! "diagnostic-data.gravity" raw)
          sealed-diagnostic (first (:sealed-diagnostics sealed))]
      (is (= 2 (count (:digest-requests raw))))
      (is (= :rejected (:status raw) (:status sealed)))
      (is (nil? (:artifact-template raw)))
      (is (= ["C8-UNDECLARED" :effect-check :diagnostic/c8-undeclared :error :active :hosted :jvm]
             ((juxt :rule :stage :message-key :severity :lifecycle :profile :target) diagnostic)))
      (is (= (:span source) (get-in diagnostic [:primary :span])))
      (is (= :namespace-syntax (get-in diagnostic [:primary :syntax-id])))
      (is (= [:generated-origin :source-origin] (:origin-chain diagnostic)))
      (is (= 'main (get-in diagnostic [:facts :function])))
      (is (= facts (select-keys (:facts diagnostic) (keys facts))))
      (is (= :declare-numeric-error-effect (get-in diagnostic [:remediation 0 :kind])))
      (is (= {:fatal? true :stop-after :effect-check :downstream-artifacts-forbidden true} (:containment raw)))
      (is (= (:diagnostic-id sealed-diagnostic) (first (:root-digests sealed))))
      (is (= raw (build facts)))
      (is (not= (:root-digests sealed)
                (:root-digests (bootstrap/p15-s23-c6c10-seal-digest-request-result!
                                "diagnostic-data.gravity" (build (assoc facts :observed-effects #{:error/raise}))))))
      (is (= {:id "C6-VERIFY" :missing-fact :root-bound-gravity-diagnostic-identity}
             (boundary-error #(bootstrap/p15-s23-c6c10-seal-digest-request-result!
                                "diagnostic-data.gravity" (assoc-in raw [:diagnostics 0 :rule] "C8-VERIFY"))))))))
