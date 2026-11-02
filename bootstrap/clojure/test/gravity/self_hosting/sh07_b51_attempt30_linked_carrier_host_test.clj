(ns gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private exact-carrier-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :semantic-manifest :semantic-root-hash :module-source-descriptors
    :link-manifest :module-plan-carriers :execution-bundle :physical-census
    :compile-census-certificates})

(def ^:private exact-m3-v2-keys
  #{:artifact :schema-version :manifest-id :module-plans :routes
    :public-roots :bundle-core-id :compile-census-certificate-ids
    :bundle-id})

(def ^:private exact-s30-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :module-source-ids :link-manifest-id :module-plan-ids
    :execution-bundle-id :compile-census-certificate-ids :schedule
    :category-commitments :logical-public-surface
    :inherited-contract-value-hashes :total-row-count})

(def ^:private exact-certificate-keys
  #{:artifact :domain :schema-version :status :role
    :architecture-report-sha256 :claim-id :claim :assumptions :checker
    :inputs :result :provenance :raw-source-transcript :m2-transcript
    :profile-bounds :evidence-root :certificate-id})

(def ^:private predecessor-id
  "sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234")

(defn- canonical-id [value]
  (bootstrap/p15-s23-sh07-b51-canonical-id value))

(defn- test-id [coordinate]
  (canonical-id
   {:domain :gravity/sh07-b51-attempt30-host-test-id-v1
    :schema-version 1 :coordinate coordinate}))

(defn- private-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "missing private bootstrap test seam" {:name name}))))

(def ^:private bundle-core-products
  (delay (bootstrap/p15-s23-sh07-b51-current-bundle-core-products)))

(defn- sum-field [rows field]
  (reduce + 0 (map field rows)))

(defn- physical-census [products]
  (let [descriptors (:module-source-descriptors products)
        plans (:module-plans products)
        edges (get-in products [:link-manifest :edges])
        rows
        (mapv
         (fn [descriptor plan]
           (let [source-id (:module-source-id descriptor)
                 definition-count (count (:definitions descriptor))
                 external-count
                 (count (filter #(= source-id (:caller-module-source-id %))
                                edges))
                 builtin-count (count (:builtin-calls descriptor))]
             {:module-ordinal (:module-ordinal descriptor)
              :module (:module descriptor)
              :logical-source-path (:logical-source-path descriptor)
              :owner (:owner descriptor)
              :source-byte-count (:source-byte-count descriptor)
              :source-sha256 (:source-sha256 descriptor)
              :module-source-id source-id
              :plan-id (:plan-id plan)
              :physical-definition-count definition-count
              :reachable-definition-count definition-count
              :physical-local-runtime-callsite-count 0
              :reachable-local-runtime-callsite-count 0
              :physical-external-runtime-callsite-count external-count
              :reachable-external-runtime-callsite-count external-count
              :physical-referenced-builtin-occurrence-count builtin-count
              :reachable-referenced-builtin-occurrence-count builtin-count
              :core-binding-count (:core-binding-count descriptor)
              :lexical-binding-count (:lexical-binding-count descriptor)
              :external-binding-count (count (:external-bindings descriptor))
              :aggregate-binding-count (:aggregate-binding-count descriptor)}))
         descriptors plans)
        semantic-row-count
        (+ 8 7
           (sum-field rows :reachable-definition-count)
           (sum-field rows :reachable-definition-count)
           (sum-field rows :reachable-local-runtime-callsite-count)
           (sum-field rows :reachable-external-runtime-callsite-count)
           (sum-field rows
                      :reachable-referenced-builtin-occurrence-count))
        occupancies (mapv :aggregate-binding-count rows)]
    {:module-count 7
     :module-censuses rows
     :physical-definition-count
     (sum-field rows :physical-definition-count)
     :reachable-definition-count
     (sum-field rows :reachable-definition-count)
     :physical-local-runtime-callsite-count
     (sum-field rows :physical-local-runtime-callsite-count)
     :reachable-local-runtime-callsite-count
     (sum-field rows :reachable-local-runtime-callsite-count)
     :physical-external-runtime-callsite-count
     (sum-field rows :physical-external-runtime-callsite-count)
     :reachable-external-runtime-callsite-count
     (sum-field rows :reachable-external-runtime-callsite-count)
     :physical-referenced-builtin-occurrence-count
     (sum-field rows :physical-referenced-builtin-occurrence-count)
     :reachable-referenced-builtin-occurrence-count
     (sum-field rows :reachable-referenced-builtin-occurrence-count)
     :semantic-row-count semantic-row-count
     :module-binding-occupancies occupancies
     :maximum-binding-occupancy (apply max occupancies)
     :binding-ceiling 2440}))

(defn- leaf-count [row-count]
  (max 1 (quot (+ row-count 15) 16)))

(defn- fold-level-count [leaves]
  (loop [children leaves level 1]
    (if (<= children 16)
      level
      (recur (quot (+ children 15) 16) (inc level)))))

(defn- category-commitments [physical]
  (let [counts
        {:roots 8
         :members (:reachable-definition-count physical)
         :functions (:reachable-definition-count physical)
         :local-runtime-call-edges
         (:reachable-local-runtime-callsite-count physical)
         :external-runtime-call-edges
         (:reachable-external-runtime-callsite-count physical)
         :referenced-builtins
         (:reachable-referenced-builtin-occurrence-count physical)
         :module-contracts 7}]
    (mapv
     (fn [category]
       (let [rows (get counts category)
             leaves (leaf-count rows)]
         {:category category :row-count rows :leaf-count leaves
          :fold-level-count (fold-level-count leaves)
          :root-digest (test-id [:category-root category rows])}))
     bootstrap/p15-s23-sh07-b51-v4-categories)))

(defn- certificate-inputs [products]
  (let [descriptors (:module-source-descriptors products)
        plans (:module-plans products)]
    {:architecture-report-sha256
     bootstrap/p15-s23-sh07-b51-attempt30-architecture-report-sha256
     :snapshot-commit "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
     :snapshot-tree "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
     :predecessor-executable-descriptor-id predecessor-id
     :module-source-identities (mapv :source-sha256 descriptors)
     :module-source-byte-counts (mapv :source-byte-count descriptors)
     :emitter-source-id (:emitter-source-id products)
     :emitter-semantic-id (:emitter-semantic-id products)
     :m0-ids (mapv :module-source-id descriptors)
     :m1-id (get-in products [:link-manifest :manifest-id])
     :m2-ids (mapv :plan-id plans)
     :bundle-core-id (:execution-bundle-core-id products)}))

(defn- checker [role]
  (let [algorithm (case role
                    :producer-census :source-first-exhaustive
                    :independent-verifier-census
                    :m2-first-independent-exhaustive)]
    {:artifact-id (test-id [role :artifact])
     :source-sha256 (test-id [role :source])
     :semantic-id (test-id [role :semantic])
     :executable-id (test-id [role :executable])
     :dependency-closure-id (test-id [role :dependency-closure])
     :algorithm-role algorithm}))

(defn- transcript [artifact role checker-value inputs]
  (let [record-root (test-id [role artifact :record-root])
        base {:artifact artifact :schema-version 1
              :node-count 1 :row-count 0 :record-root record-root}
        artifact-id
        (canonical-id
         {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
          :schema-version 1 :artifact artifact :checker checker-value
          :inputs inputs :node-count 1 :row-count 0
          :record-root record-root})]
    (assoc base :artifact-id artifact-id)))

(defn- certificate
  [products role common-claim]
  (let [checker-value (checker role)
        inputs (certificate-inputs products)
        raw (transcript :gravity/sh07-b51-raw-source-census-transcript-v1
                        role checker-value inputs)
        m2 (transcript :gravity/sh07-b51-m2-census-transcript-v1
                       role checker-value inputs)
        physical (:physical-census common-claim)
        result
        {:status :accepted
         :normalized-census-id (:normalized-census-id common-claim)
         :definition-count (:physical-definition-count physical)
         :local-count (:physical-local-runtime-callsite-count physical)
         :external-count (:physical-external-runtime-callsite-count physical)
         :builtin-count
         (:physical-referenced-builtin-occurrence-count physical)
         :semantic-row-count (:semantic-row-count physical)
         :external-bijection-complete true
         :builtin-parity-complete true
         :provenance-complete true
         :ordering-complete true
         :reachability-complete true}
        provenance
        {:invocation-id (str "attempt30-test-" (name role))
         :tool-closure-id (test-id [role :tool-closure])
         :raw-transcript-artifact-id (:artifact-id raw)
         :m2-transcript-artifact-id (:artifact-id m2)
         :reproduction-command-id (test-id [role :reproduction-command])
         :evidence-relative-path (str "test-evidence/" (name role) ".edn")
         :completed-at "2026-09-02T00:00:00Z"}
        assumptions
        {:sha256-collision-resistance :assumed
         :canonical-reader-hash-id
         :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
         :source-emitter-bytes-immutable true
         :checker-closures-independent true
         :host-pin-boundary-id
         bootstrap/p15-s23-sh07-b51-host-pin-boundary-id
         :runtime-token-boundary-id
         bootstrap/p15-s23-sh07-b51-runtime-token-boundary-id}
        claim-preimage
        {:domain :gravity/sh07-b51-common-census-claim-id-v1
         :schema-version 1
         :architecture-report-sha256
         bootstrap/p15-s23-sh07-b51-attempt30-architecture-report-sha256
         :inputs inputs :assumptions assumptions :claim common-claim
         :profile-bounds bootstrap/p15-s23-sh07-b51-attempt30-profile-bounds}
        evidence-domain
        (case role
          :producer-census
          :gravity/sh07-b51-producer-census-evidence-root-v1
          :independent-verifier-census
          :gravity/sh07-b51-independent-verifier-census-evidence-root-v1)
        evidence-preimage
        {:domain evidence-domain :schema-version 1 :checker checker-value
         :inputs inputs :raw-source-transcript raw :m2-transcript m2
         :result result
         :reproduction-command-id (:reproduction-command-id provenance)
         :profile-bounds bootstrap/p15-s23-sh07-b51-attempt30-profile-bounds}
        base
        {:artifact :gravity/sh07-b51-compile-census-certificate
         :domain (case role
                   :producer-census
                   :gravity/sh07-b51-producer-census-certificate-v1
                   :independent-verifier-census
                   :gravity/sh07-b51-independent-verifier-census-certificate-v1)
         :schema-version 1 :status :accepted :role role
         :architecture-report-sha256
         bootstrap/p15-s23-sh07-b51-attempt30-architecture-report-sha256
         :claim-id (canonical-id claim-preimage)
         :claim common-claim :assumptions assumptions :checker checker-value
         :inputs inputs :result result :provenance provenance
         :raw-source-transcript raw :m2-transcript m2
         :profile-bounds bootstrap/p15-s23-sh07-b51-attempt30-profile-bounds
         :evidence-root (canonical-id evidence-preimage)}]
    (assoc base :certificate-id (canonical-id base))))

(defn- deterministic-certificate-pair [products]
  (let [physical (physical-census products)
        commitments (category-commitments physical)
        normalized-id
        (canonical-id
         {:domain :gravity/sh07-b51-normalized-census-id-v1
          :schema-version 1 :physical-census physical
          :ordered-category-row-roots (mapv :root-digest commitments)
          :category-row-counts (mapv :row-count commitments)})
        claim
        {:domain :gravity/sh07-b51-common-census-claim-v1
         :normalized-census-id normalized-id
         :external-bijection-commitment (test-id :external-bijection)
         :builtin-parity-commitments
         (mapv #(test-id [:builtin-parity %]) (range 7))
         :provenance-completeness-commitment
         (test-id :provenance-completeness)
         :physical-census physical
         :category-schedule bootstrap/p15-s23-sh07-b51-v4-schedule
         :category-commitments commitments}]
    [(certificate products :producer-census claim)
     (certificate products :independent-verifier-census claim)]))

(def ^:private accepted-fixture
  (delay
    (let [products @bundle-core-products
          certificates (deterministic-certificate-pair products)
          carrier
          (bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
           products certificates)]
      {:carrier carrier :certificates certificates
       :products (assoc products :execution-bundle
                        (:execution-bundle carrier))})))

(defn- raw-request [carrier]
  {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v4
   :schema-version 4
   :authenticated-sh06-request {}
   :authenticated-sh06-verification-report {}
   :sh06-resolution-artifact {}
   :predecessor-executable-descriptor
   {:executable-closure-descriptor-id predecessor-id}
   :v18-executable-descriptor carrier
   :predecessor-contract-carrier {}
   :predecessor-raw-outcome {}
   :physical-invocation {}
   :scope :sh07-b51-vector-destructuring})

(deftest attempt30-candidate-is-closed-and-certificate-values-have-one-home
  (let [{:keys [carrier certificates]} @accepted-fixture
        m3 (:execution-bundle carrier)
        s30 (:semantic-manifest carrier)
        certificate-value-keys
        (filter #(and (map-entry? %)
                      (= :compile-census-certificates (key %)))
                (tree-seq coll? seq carrier))]
    (is (every? bootstrap/p15-s23-sh07-b51-compile-census-certificate-v1-valid?
                certificates))
    (is (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
         certificates))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
          (vec (reverse certificates)))))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-compile-census-certificate-v1-valid?
          (assoc (first certificates) :unknown true))))
    (is (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? carrier))
    (is (= exact-carrier-keys (set (keys carrier))))
    (is (= exact-m3-v2-keys (set (keys m3))))
    (is (= exact-s30-keys (set (keys s30))))
    (is (every? #(= exact-certificate-keys (set (keys %))) certificates))
    (is (= (mapv :certificate-id certificates)
           (:compile-census-certificate-ids m3)
           (:compile-census-certificate-ids s30)))
    (is (= 1 (count certificate-value-keys)))
    (is (not-any? #{:p29-id}
                  (filter keyword? (tree-seq coll? seq carrier))))))

(deftest attempt30-boundaries-and-production-authority-are-fail-closed
  (is (= bootstrap/p15-s23-sh07-b51-host-pin-boundary-id
         (canonical-id
          {:domain :gravity/sh07-b51-host-pin-boundary-id-v1
           :schema-version 1 :component :p15-s23-sh07-b51-plan-linker
           :pin-key-names
           [:expected-m3-id :expected-certificate-a-id
            :expected-certificate-b-id :expected-semantic-root]
           :caller-supplied false})))
  (is (= bootstrap/p15-s23-sh07-b51-runtime-token-boundary-id
         (canonical-id
          {:domain :gravity/sh07-b51-runtime-token-boundary-id-v1
           :schema-version 1 :component :p15-s23-sh07-b51-runtime-context
           :token-kind :private-object-identity :caller-supplied false})))
  (is (= :provisional-no-authority
         (:status (bootstrap/p15-s23-sh07-b51-attempt30-authority-status))))
  (is (false?
       (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-authorized?
        (:carrier @accepted-fixture))))
  (is (= :provisional-no-authority
         (try
           (bootstrap/p15-s23-sh07-b51-current-linked-carrier-v4)
           nil
           (catch clojure.lang.ExceptionInfo failure
             (:reason (ex-data failure)))))))

(deftest attempt30-preflight-is-iterative-bounded-and-first
  (let [at-limit {:maximum-nodes 4 :maximum-depth 1 :maximum-width 3
                  :maximum-collection-count 1 :maximum-scalar-utf8 3}
        raw (raw-request (:carrier @accepted-fixture))
        equality-calls (atom 0)
        equality-var
        (private-var 'p15-s23-sh07-b51-v4-bounded-carrier-equal?)]
    (is (= {:nodes 4 :depth 1 :width 3 :collection-count 1
            :scalar-utf8 3}
           (bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile
            ["a" "b" "c"] at-limit)))
    (doseq [bounds [(assoc at-limit :maximum-nodes 3)
                    (assoc at-limit :maximum-depth 0)
                    (assoc at-limit :maximum-width 2)
                    (assoc at-limit :maximum-collection-count 0)
                    (assoc at-limit :maximum-scalar-utf8 2)]]
      (is (nil?
           (bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile
            ["a" "b" "c"] bounds))))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-supplied-raw-v4-preflight?
          (assoc raw :v18-executable-descriptor (map identity (range))))))
    (with-redefs-fn
      {equality-var (fn [& _] (swap! equality-calls inc) true)}
      #(is (nil?
            (bootstrap/p15-s23-sh07-b51-admit-linked-raw-v4
             (assoc raw :v18-executable-descriptor (map identity (range)))
             1 [{}] {}))))
    (is (zero? @equality-calls))))

(deftest attempt30-admission-order-route-arity-and-stripped-compatibility-runtime
  (let [{:keys [carrier products]} @accepted-fixture
        authority {:carrier carrier :products products}
        raw (raw-request carrier)
        events (atom [])
        preflight-var
        (private-var 'p15-s23-sh07-b51-supplied-raw-v4-preflight?)
        load-var (private-var 'p15-s23-sh07-b51-load-local-authority-v4)
        equality-var
        (private-var 'p15-s23-sh07-b51-v4-bounded-carrier-equal?)
        route-var (private-var 'p15-s23-sh07-b51-v4-route-and-arity)
        original-preflight @preflight-var
        original-equality @equality-var
        original-route @route-var
        admission
        (with-redefs-fn
          {preflight-var
           (fn [value]
             (swap! events conj :preflight)
             (original-preflight value))
           load-var
           (fn [] (swap! events conj :local-products-and-pins) authority)
           equality-var
           (fn [supplied current]
             (swap! events conj :equality-and-joins)
             (original-equality supplied current))
           route-var
           (fn [linked-products root args]
             (swap! events conj :route-and-arity)
             (original-route linked-products root args))}
          #(bootstrap/p15-s23-sh07-b51-admit-linked-raw-v4
            raw 1 [{}] {:sh07-b51-runtime-context-token :caller-forged}))]
    (is (= [:preflight :local-products-and-pins :equality-and-joins
            :route-and-arity]
           @events))
    (is (= 'sh07-b51-build-template (get-in admission [:root :function])))
    (is (nil? (get-in admission
                      [:runtime :sh07-b51-runtime-context-token])))
    (is (nil? (get-in admission
                      [:runtime :sh07-b51-runtime-token-boundary-id])))
    (is (nil? (get-in admission
                      [:runtime :sh07-b51-linked-carrier-v4])))
    (is (nil?
         (with-redefs-fn
           {load-var (fn [] authority)}
           #(bootstrap/p15-s23-sh07-b51-admit-linked-raw-v4
             raw 1 [] {}))))))

(deftest attempt30-mutations-old-schemas-and-unauthorized-edges-reject
  (let [{:keys [carrier certificates]} @accepted-fixture
        products @bundle-core-products
        first-edge (get-in products [:link-manifest :edges 0])
        caller-id (:caller-module-source-id first-edge)
        caller-index
        (first (keep-indexed
                (fn [index descriptor]
                  (when (= caller-id (:module-source-id descriptor)) index))
                (:module-source-descriptors products)))
        callee-symbol (:callee-function first-edge)
        unauthorized-products
        (-> products
            (assoc-in [:link-manifest :edges 0 :owner-edge]
                      :forbidden-owner-edge)
            (update-in
             [:module-source-descriptors caller-index :external-bindings]
             (fn [bindings]
               (mapv #(if (= callee-symbol (:symbol %))
                        (assoc % :owner-edge :forbidden-owner-edge)
                        %)
                     bindings))))
        edge-validator
        @(private-var 'p15-s23-sh07-b51-v4-declared-edge-valid?)]
    (doseq [mutation [(assoc carrier :unknown true)
                      (dissoc carrier :physical-census)
                      (assoc carrier :domain
                             :gravity/sh07-b51-v18-linked-executable-closure-carrier-v3)
                      (assoc-in carrier [:execution-bundle :artifact]
                                :gravity/sh07-b51-execution-bundle-v1)
                      (update-in carrier [:physical-census :semantic-row-count]
                                 inc)
                      (assoc-in carrier
                                [:compile-census-certificates 1 :role]
                                :producer-census)]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? mutation))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (bootstrap/p15-s23-sh07-b51-build-linked-carrier-v4-candidate
                  unauthorized-products certificates)))
    (is (edge-validator (:module-source-descriptors products) first-edge))
    (is (false?
         (edge-validator (:module-source-descriptors unauthorized-products)
                         (get-in unauthorized-products
                                 [:link-manifest :edges 0]))))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-v4-candidate-matches-pin-map?
          carrier
          (assoc (bootstrap/p15-s23-sh07-b51-v4-candidate-pin-map carrier)
                 :expected-semantic-root (test-id :caller-pin)))))))
