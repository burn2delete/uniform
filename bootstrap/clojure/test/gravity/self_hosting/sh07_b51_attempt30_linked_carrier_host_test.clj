(ns gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-fixture-support
             :as fixture-support]))

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

(defn- canonical-id [value]
  (bootstrap/p15-s23-sh07-b51-canonical-id value))

(defn- private-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "missing private bootstrap test seam" {:name name}))))


(deftest attempt30-candidate-is-closed-and-certificate-values-have-one-home
  (let [{:keys [carrier certificates]} (fixture-support/fixture)
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
        (:carrier (fixture-support/fixture)))))
  (is (= :provisional-no-authority
         (try
           (bootstrap/p15-s23-sh07-b51-current-linked-carrier-v4)
           nil
           (catch clojure.lang.ExceptionInfo failure
             (:reason (ex-data failure)))))))

(deftest attempt30-preflight-is-iterative-bounded-and-first
  (let [at-limit {:maximum-nodes 4 :maximum-depth 1 :maximum-width 3
                  :maximum-collection-count 1 :maximum-scalar-utf8 3}
        raw (fixture-support/raw-request (fixture-support/carrier))
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
  (let [{:keys [carrier products]} (fixture-support/fixture)
        authority {:carrier carrier :products products}
        raw (fixture-support/raw-request carrier)
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
  (let [{:keys [carrier certificates]} (fixture-support/fixture)
        products (fixture-support/products)
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
                 :expected-semantic-root
                 (fixture-support/test-id :caller-pin)))))))
