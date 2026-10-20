(ns gravity.self-hosting.sh10-bounded-escape-destination-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private root
  (delay
    (loop [p (.getParent (.toPath (io/file (.toURI (io/resource
                "gravity/self_hosting/sh10_bounded_escape_destination_test.clj")))))]
      (cond (nil? p) (throw (ex-info "repository root missing" {}))
            (.isFile (.toFile (.resolve p "deps.edn"))) p
            :else (recur (.getParent p))))))
(defn- path [relative] (str (.resolve @root relative)))
(def ^:private source "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")
(def ^:private fixtures "bootstrap/clojure/fixtures/self-hosting/sh-10/")
(defn- fixture [family extension]
  (str fixtures family "/bounded-escape-destinations" extension))
(defn- compile-plan [relative]
  (let [p (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! p :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter p (slurp p))))
(def ^:private c9 (delay (compile-plan source)))
(def ^:private accepted (delay (compile-plan (fixture "accepted" ".gravity"))))
(def ^:private rejected (delay (compile-plan (fixture "rejected" ".gravity"))))
(defn- execute [plan function args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
    {:engine :sh10-bounded-escape-development :compiler-artifact-plan? true}
    @plan function args))
(defn- invoke [function & args] (execute c9 function (vec args)))
(defn- request [destination end]
  (execute accepted 'bounded-escape-request [destination end]))
(defn- check [r] (invoke 'sh10-check-ownership-request r))
(def ^:private destinations [:function-return :closure-capture :global-or-static-storage])
(defn- scenario [r]
  {:artifact :gravity/sh10-authenticated-owned-mutable-scenario :schema-version 1
   :scenario-id (:request-id r) :core-node-id :value
   :initialization (:initialization r) :owner-lifetime-end (:owner-lifetime-end r)
   :events (mapv #(select-keys % [:event-id :op :borrow-id :destination-owner
                                :destination-lifetime-end :escape-destination :control-path])
                 (:events r))})

(deftest sh10-bounded-escape-fixtures-development-kernel
  (doseq [family ["accepted" "rejected"]]
    (let [g (slurp (path (fixture family ".gravity")))
          q (slurp (path (fixture family ".qst")))]
      (is (= g q))
      ;; Q has hosted-reader parity only; the G fixture is compiled/executed below.
      (is (= (bootstrap/read-forms (path (fixture family ".gravity")) g)
             (bootstrap/read-forms (path (fixture family ".qst")) q)))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c9)))
  (doseq [policy ['sh10-ownership-policy 'sh10-authenticated-owned-mutable-policy]]
    (is (= (set destinations) (:escape-destinations (invoke policy)))))
  (doseq [destination destinations]
    (is (= :accepted (:status (check (request destination 8)))))
    (is (= :escape-of-inactive-borrow
           (get-in (check (execute rejected 'inactive-escape-request [destination]))
                   [:diagnostics 0 :reason])))))

(deftest sh10-bounded-escape-transitions-development-kernel
  (doseq [destination destinations acquisition [:borrow-immutable :borrow-mutable]
          end [0 8 10]]
    (let [r (assoc-in (request destination end) [:events 0 :op] acquisition)
          result (check r) fact (get-in result [:ownership-facts 1])]
      (is (= :accepted (:status result)))
      (is (= destination (:escape-destination fact)))
      (is (= (get-in r [:events 1 :source-span]) (:source-span fact)))
      (is (= (get-in r [:events 1 :origin-chain]) (:origin-chain fact)))
      (is (= (:request-id r) (:request-id result)))
      (is (= r (get-in result [:identity-input :request])))
      (is (= :passed (:status (invoke 'sh10-verify-ownership-result r result))))
      (is (= :rejected (:status (invoke 'sh10-verify-ownership-result r
                                  (assoc-in result [:ownership-facts 1 :escape-destination] :ffi-retention)))))))
  (doseq [destination destinations]
    (let [r (request destination 11) result (check r) d (first (:diagnostics result))]
      (is (= [:rejected "C9-BORROW-ESCAPE" :borrow-outlives-owner]
             [(:status result) (:rule d) (:reason d)]))
      (is (= {:owner-end 10 :destination-end 11} (:lifetime-interval d)))
      (is (= [destination :borrow :value :owner :meta :jvm]
             ((juxt :escape-path :borrow-id :value-id :owner-id :profile :target) d)))
      (is (= (get-in r [:events 1 :source-span]) (:source-span d)))
      (is (= (get-in r [:events 1 :origin-chain]) (:generated-origin-chain d)))
      (is (= (get-in r [:events 1 :control-path]) (:control-path d)))
      (is (= :passed (:status (invoke 'sh10-verify-ownership-result r result))))
      (is (= :rejected (:status (invoke 'sh10-verify-ownership-result r
                                  (assoc-in result [:diagnostics 0 :source-span] {}))))))
    (doseq [borrow [:wrong nil]]
      (let [r (assoc-in (request destination 8) [:events 1 :borrow-id] borrow)]
        (is (= (if (nil? borrow) :malformed-ownership-event :escape-of-inactive-borrow)
               (get-in (check r) [:diagnostics 0 :reason])))))
    (let [r (assoc (request destination 2147483647) :owner-lifetime-end 2147483647)]
      (is (= :accepted (:status (check r))))))
  (doseq [end [nil -1 2147483648 1/2 "10" :infinity]]
    (is (= :malformed-ownership-event
           (get-in (check (request :closure-capture end)) [:diagnostics 0 :reason]))))
  (doseq [destination [nil :global-storage :closure :ffi-retention :task-or-actor-transfer]]
    (is (= :malformed-ownership-event
           (get-in (check (request destination 8)) [:diagnostics 0 :reason]))))
  (doseq [[field value reason] [[:availability :moved :operation-after-move]
                               [:availability :consumed :operation-after-consume]
                               [:initialization :uninitialized :operation-before-initialization]]]
    (let [r (assoc (request :closure-capture 8) :events [(second (:events (request :closure-capture 8)))])
          state {:initialization :initialized :availability :available :current-owner :owner
                 :immutable-borrow-count 0 :immutable-borrows {} :issued-borrow-identities {} :mutable-borrow-id nil}]
      ;; Direct original transition preserves availability/initialization precedence.
      (is (= reason (get-in (invoke 'sh10-transition r (first (:events r)) (assoc state field value))
                           [:diagnostic :reason]))))))

(defn- supplied-c8 []
  ;; Conditional supplied-record validation only, NOT a fresh C8 proof/issuer.
  (let [r (request :closure-capture 8)
        ids {:type-fact-id (:type-fact-id r) :effect-fact-id (:effect-fact-id r)
             :capability-proof-id (:type-fact-id r)}
        bound {:artifact :gravity/sh09-identity-bound-effected-core-template :schema-version 1
               :status :accepted :scope :pure-authenticated-sh08-primitive-typed-core
               :effected-core {:module {:namespace 'bounded.escape :profile :meta :target :jvm}
                               :effect-requests [{:core-node-id :value :source-span (:source-span r)
                                                 :origin-chain [{:from :supplied-c8}]}]
                               :type-table {:value :gravity.type/string}
                               :effect-graph {:nodes {:value {:artifact :gravity/c8-effect-node
                                                             :direct #{} :latent #{} :transitive #{} :ordering :pure}}}}
               :fact-identities {:value ids} :identity-input {:supplied true}
               :identity-resolution :coordinator-digest-bound :provenance {:supplied true}
               :diagnostics [] :pending [] :self-hosted? false :clojure-seed-boundary? true}
        receipt {:artifact :gravity/sh09-effect-identity-binding-verification :status :passed
                 :checks [:fresh-authenticated-effected-core :exact-digest-request-order
                          :resolved-digest-shape :type-effect-capability-linkage
                          :path-neutral-identity-input :separate-actual-path-provenance]
                 :expected bound :candidate bound}]
    [bound receipt]))

(deftest sh10-bounded-escape-adapter-development-kernel
  (let [[bound receipt] (supplied-c8)]
    (doseq [destination destinations]
      (let [s (scenario (request destination 8))
            runtime-s (assoc s :artifact :gravity/sh10-runtime-checked-mutable-borrow-scenario)
            result (invoke 'sh10-build-authenticated-owned-mutable-analysis bound receipt s)
            r (:ownership-request result)]
        (is (true? (invoke 'sh10-authenticated-owned-mutable-scenario-valid? s)))
        (is (true? (invoke 'sh10-runtime-mutable-borrow-scenario-valid? runtime-s)))
        (is (= :accepted (:status result)))
        (is (= destination (get-in r [:events 1 :escape-destination])))
        (is (= 8 (get-in r [:events 1 :destination-lifetime-end])))
        (is (= (get-in bound [:effected-core :effect-requests 0 :source-span]) (get-in r [:events 1 :source-span])))
        (is (= [{:from :supplied-c8}] (get-in r [:events 1 :origin-chain])))
        (is (= :passed (:status (invoke 'sh10-verify-authenticated-owned-mutable-analysis bound receipt s result))))
        (is (= :rejected (:status (invoke 'sh10-verify-authenticated-owned-mutable-analysis bound receipt s
                                   (assoc-in result [:ownership-request :events 1 :destination-lifetime-end] 9)))))
        (is (= :rejected (:status (invoke 'sh10-build-authenticated-owned-mutable-analysis bound
                                   (assoc receipt :candidate {}) s))))
        (doseq [end [11]]
          (is (= :borrow-outlives-owner (get-in (invoke 'sh10-build-authenticated-owned-mutable-analysis
                                                       bound receipt (assoc-in s [:events 1 :destination-lifetime-end] end))
                                              [:diagnostics 0 :reason]))))))
    (doseq [mutate [#(assoc-in % [:events 1 :escape-destination] :ffi-retention)
                    #(assoc-in % [:events 1 :destination-lifetime-end] nil)
                    #(assoc-in % [:events 1 :destination-lifetime-end] 2147483648)
                    #(assoc-in % [:events 1 :destination-owner] :other)
                    #(assoc-in % [:events 1 :borrow-id] nil)
                    #(assoc-in % [:events 1 :extra] true)]]
      (let [s (mutate (scenario (request :closure-capture 8)))]
        (is (false? (invoke 'sh10-authenticated-owned-mutable-scenario-valid? s)))
        (is (false? (invoke 'sh10-runtime-mutable-borrow-scenario-valid?
                            (assoc s :artifact :gravity/sh10-runtime-checked-mutable-borrow-scenario))))
        (is (= :malformed-owned-mutable-lifecycle-scenario
               (get-in (invoke 'sh10-build-authenticated-owned-mutable-analysis bound receipt s) [:diagnostics 0 :reason])))))
    ;; Owned adapter acquisition remains immutable-only; runtime family is separate.
    (let [s (assoc-in (scenario (request :closure-capture 8)) [:events 0 :op] :borrow-mutable)]
      (is (false? (invoke 'sh10-authenticated-owned-mutable-scenario-valid? s)))
      (is (true? (invoke 'sh10-runtime-mutable-borrow-scenario-valid?
                        (assoc s :artifact :gravity/sh10-runtime-checked-mutable-borrow-scenario)))))))
