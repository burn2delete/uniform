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
  (let [{:keys [raw replay fresh]} @current-pair
        template (:artifact-template raw) nodes (:core-nodes template)
        add-id (:node-id (get nodes 4)) root-id (:node-id (get nodes 5))
        verify #(producer 'c6-c10-verify-checked-core-template
                  (:envelope fresh) % (:digest-requests replay))]
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
