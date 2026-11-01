(ns gravity.safety.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.safety :as safety]
            [gravity.self-hosting.sh11-numeric-safety-test]))

(defn- helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh11-numeric-safety-test name)))

(defn- node [id operation operands effects capabilities outcome]
  {:node-id id :path [:test id] :source-operation operation :operands operands
   :source {:origin-id (str id ":origin")
            :span {:source "safety-records.gravity" :start 0 :end 1}}
   :profile :hosted :type :gravity/integer :effects effects :capabilities capabilities
   :ownership {:model :persistent-immutable-value}
   :safety {:outcome outcome :basis (b/p15-s23-closed-core-safety-basis operation)}})

(defn- proof-args [subject]
  ["sha256:supplied-test-hash" (:path subject) (:source-operation subject) (:source subject)
   (:profile subject) (:type subject) (:effects subject) (:capabilities subject)
   (:ownership subject) (get-in subject [:safety :basis])])

(deftest contract-identifies-records-and-keeps-classification-with-the-owner
  (is (empty? (ns-aliases 'gravity.safety)))
  (is (= #{'managed-allocation-check 'transcript-delivery-check 'safety-basis
           'pure-safety-proof 'pure-safety-proof-valid? 'structural-safety-proof
           'structural-safety-proof-valid? 'child-obligation-refs 'rederive-proven-safety}
         (set (keys (ns-publics 'gravity.safety)))))
  (doseq [[name operation] (ns-publics 'gravity.safety)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.safety (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (= :existing-gravity-c10-and-checked-core-verifier (:outcome-classification-owner contract))))))

(deftest actual-paired-gravity-programs-retain-all-four-outcomes
  ;; Original normalized safety requests, not genuine source/Core or provider
  ;; admission. Unsafe/check records here are data, not executed native actions.
  (doseq [extension [".gravity" ".qst"]]
    (let [compile-plan (helper 'compile-plan) invoke (helper 'invoke)
          plan (compile-plan (str "bootstrap/clojure/fixtures/self-hosting/sh-11/accepted/numeric-safety-outcomes" extension))
          bad-plan (compile-plan (str "bootstrap/clojure/fixtures/self-hosting/sh-11/rejected/invalid-numeric-safety" extension))
          requests (mapv #(invoke (delay plan) % [])
                         '[sh11-proven-division-request sh11-runtime-division-request sh11-unsafe-overflow-request])
          bad-request (invoke (delay bad-plan) 'sh11-zero-divisor-request [])
          results (mapv (helper 'classify) requests)
          rejection ((helper 'classify) bad-request)]
      (doseq [compiled [plan bad-plan]]
        (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
        (is (= (:plan-id compiled)
               (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc compiled :plan-id))))))))
      (is (= [:proven-safe :runtime-checked :unsafe-island] (mapv :outcome results)))
      (is (= [:accepted :accepted :accepted] (mapv :status results)))
      (is (= :rejected (:outcome rejection) (:status rejection)))
      (is (= "C10-NUMERIC" (get-in rejection [:diagnostics 0 :rule])))
      (doseq [[request result] (map vector requests results)]
        (is (= (:source-span request) (get-in result [:preserves :source-span])))
        (is (= :passed (:status ((helper 'invoke-c10) 'sh11-verify-safety-result [request result]))))))))

(deftest check-and-proof-records-keep-exact-identity-and-provenance
  (let [subject (node :literal :literal [] #{} #{} :proven-safe)
        args (proof-args subject)
        pure (apply b/p15-s23-closed-core-pure-safety-proof args)
        effectful (assoc subject :effects #{:io/write} :capabilities #{:io/stdout})
        children [{:node-id :child :outcome :runtime-checked :runtime-check-id :check-id
                   :safety-proof-id :not-applicable :capability-proof-ids [:capability-proof]}]
        structural (apply b/p15-s23-closed-core-structural-safety-proof (concat (proof-args effectful) [children]))]
    (is (= pure (apply safety/pure-safety-proof #'b/p15-s23-closed-core-digest args)))
    (is (= structural (apply safety/structural-safety-proof #'b/p15-s23-closed-core-digest
                            (concat (proof-args effectful) [children]))))
    (doseq [proof [pure structural]]
      (is (= (:proof-id proof) (b/p15-s23-closed-core-digest (dissoc proof :proof-id))))
      (is (= (:ownership-digest proof) (b/p15-s23-closed-core-digest (:ownership subject))))
      (is (= (get-in subject [:source :span]) (get-in proof [:source-origin :span])))
      (is (seq (:invalidation-conditions proof))))
    (is (b/p15-s23-closed-core-pure-safety-proof-valid? pure))
    (is (b/p15-s23-closed-core-structural-safety-proof-valid? structural effectful children))
    (doseq [changed [(assoc pure :proof-id :forged) (assoc pure :basis :changed)
                     (assoc pure :effect-requirements #{:io/write})]]
      (is (false? (safety/pure-safety-proof-valid? #'b/p15-s23-closed-core-digest changed))))
    (doseq [changed [(assoc structural :proof-id :forged) (assoc structural :child-obligations [])
                     (assoc structural :effects #{})]]
      (is (false? (safety/structural-safety-proof-valid? #'b/p15-s23-closed-core-digest changed effectful children))))
    (doseq [[wrapper direct kind] [[b/p15-s23-closed-core-managed-allocation-check safety/managed-allocation-check :managed-allocation-result]
                                  [b/p15-s23-closed-core-transcript-delivery-check safety/transcript-delivery-check :reference-transcript-delivery]]]
      (let [check (wrapper (first args) (:path subject) (:source subject))]
        (is (= check (direct #'b/p15-s23-closed-core-digest (first args) (:path subject) (:source subject))))
        (is (= :required (:status check)))
        (is (= kind (:kind check)))
        (is (= (:check-id check) (b/p15-s23-closed-core-digest (dissoc check :artifact :check-id :status))))))
    (is (false? (:live-external-io? (b/p15-s23-closed-core-transcript-delivery-check (first args) (:path subject) (:source subject)))))))

(deftest rederivation-preserves-order-child-links-and-nonproven-metadata
  (let [pure (node :pure :literal [] #{} #{} :proven-safe)
        checked (assoc-in (node :checked :println [] #{:io/write} #{:io/stdout} :runtime-checked)
                          [:safety :check] {:check-id :check-id :condition :test-condition})
        parent (node :parent :do [:pure :checked] #{:io/write} #{:io/stdout} :proven-safe)
        unsafe (assoc-in (node :unsafe :literal [] #{} #{} :unsafe-island)
                         [:safety :unsafe-audit] {:unsafe-island true :ai/human-review :required})
        rejected (assoc-in (node :rejected :literal [] #{} #{} :rejected) [:safety :diagnostics] [:refused])
        nodes [pure checked parent unsafe rejected]
        calls (atom []) original-pure b/p15-s23-closed-core-pure-safety-proof
        original-structural b/p15-s23-closed-core-structural-safety-proof
        original-refs b/p15-s23-closed-core-child-obligation-refs
        result (with-redefs [b/p15-s23-checked-core-capability-proof-records
                            (fn [values evidence]
                              (swap! calls conj [:capabilities values evidence])
                              [{:core-node-id :checked :capability :io/stdout :proof-id :capability-proof}])
                            b/p15-s23-closed-core-pure-safety-proof
                            (fn [& args] (swap! calls conj [:pure]) (apply original-pure args))
                            b/p15-s23-closed-core-structural-safety-proof
                            (fn [& args] (swap! calls conj [:structural]) (apply original-structural args))
                            b/p15-s23-closed-core-child-obligation-refs
                            (fn [node by-id proofs]
                              (swap! calls conj [:children (set (keys by-id))])
                              (original-refs node by-id proofs))]
                 (b/p15-s23-closed-core-rederive-proven-safety "sha256:test-data" nodes :mock-data-port))
        children (get-in result [2 :safety :proof :child-obligations])]
    (is (= [:capabilities :pure :children :structural] (mapv first @calls)))
    (is (= [:capabilities nodes :mock-data-port] (first @calls)))
    (is (= [:children #{:pure :checked}] (nth @calls 2)))
    (is (= (mapv :source nodes) (mapv :source result)))
    (is (= (mapv :node-id nodes) (mapv :node-id result)))
    (is (= [:pure :checked] (mapv :node-id children)))
    (is (= (get-in result [0 :safety :proof :proof-id]) (:safety-proof-id (first children))))
    (is (= :check-id (:runtime-check-id (second children))))
    (is (= [:capability-proof] (:capability-proof-ids (second children))))
    (doseq [index [1 3 4]] (is (identical? (nth nodes index) (nth result index))))))

(deftest record-digest-predicates-are-not-safety-admission
  (let [subject (node :literal :literal [] #{} #{} :proven-safe)
        record (apply b/p15-s23-closed-core-pure-safety-proof (proof-args subject))
        changed (assoc record :type :supplied-unverified-type)
        self-consistent (assoc changed :proof-id (b/p15-s23-closed-core-digest (dissoc changed :proof-id)))
        calls (atom [])]
    ;; The existing helper checks its limited record contract. The owner still
    ;; regenerates from verified node/source data before authoritative use.
    (is (b/p15-s23-closed-core-pure-safety-proof-valid? self-consistent))
    (is (not= record self-consistent))
    (with-redefs [b/p15-s23-closed-core-digest (fn [input] (swap! calls conj input) :digest-port)]
      (is (= :digest-port (:proof-id (apply b/p15-s23-closed-core-pure-safety-proof (proof-args subject)))))
      (is (= (:ownership subject) (first @calls)))
      (is (= :digest-port (:ownership-digest (second @calls))))
      (is (= 2 (count @calls))))))
