(ns gravity.self-hosting.sh10-initialization-move-family-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh09-c7-effect-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh10_initialization_move_family_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-10 initialization/move test is not on the classpath"
                {:id "SH10-INITIALIZATION-MOVE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH10-INITIALIZATION-MOVE-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c9-source
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-10/initialization-move")

(defn- fixture-path [disposition basename extension]
  (str fixture-root "/" disposition "/" basename extension))

(def ^:private c9-plan (delay (compile-plan c9-source)))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "initialization-move" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "initialization-move" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path
     "rejected" "invalid-initialization-move" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path "rejected" "invalid-initialization-move" ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-initialization-move-family
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c9 [function arguments]
  (invoke c9-plan function arguments))

(defn- sh09-var [name]
  (or
   (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test name)
   (throw
    (ex-info "Required SH-09 test helper is unavailable"
             {:id "SH10-INITIALIZATION-MOVE-SH09-HELPER"
              :name name}))))

(defn- sh09-value [name]
  (var-get (sh09-var name)))

(defn- prepared-bound-products [typed verification]
  (let [effected ((sh09-value 'build) typed verification)
        invoke-c8 (sh09-value 'invoke-c8)
        template
        (invoke-c8
         'sh09-authenticated-effect-identity-requests
         [typed verification effected])
        digests
        (mapv
         (fn [index]
           (format "sha256:%064x" (inc index)))
         (range (count (:requests template))))
        resolved
        (mapv (fn [request digest]
                {:request request :digest digest})
              (:requests template) digests)
        bound
        (invoke-c8
         'sh09-bind-authenticated-effect-identities
         [typed verification effected resolved])
        binding-verification
        (invoke-c8
         'sh09-verify-authenticated-effect-identities
         [typed verification effected resolved bound])]
    {:bound bound
     :verification binding-verification}))

(defn- prepared-bound []
  (let [typed ((sh09-value 'typed-result)
               "/checkout-a/sh10-initialization-move.gravity")]
    (prepared-bound-products
     typed ((sh09-value 'upstream-verification) typed))))

(def ^:private prepared (delay (prepared-bound)))

(defn- core-node-id []
  (get-in @prepared [:bound :effected-core :effect-requests 0 :core-node-id]))

(defn- scenario [plan function]
  (invoke plan function [(core-node-id)]))

(defn- analyze [scenario]
  (invoke-c9
   'sh10-build-authenticated-owned-mutable-analysis
   [(:bound @prepared) (:verification @prepared) scenario]))

(defn- verify-analysis [scenario candidate]
  (invoke-c9
   'sh10-verify-authenticated-owned-mutable-analysis
   [(:bound @prepared) (:verification @prepared) scenario candidate]))

(def ^:private accepted-cases
  {'sh10-authenticated-initialize-read-scenario
   {:operations [:initialize :read]
    :initialization :initialized
    :availability :available}
   'sh10-authenticated-move-scenario
   {:operations [:initialize :move]
    :initialization :initialized
    :availability :moved}
   'sh10-authenticated-consume-scenario
   {:operations [:initialize :consume]
    :initialization :initialized
    :availability :consumed}})

(def ^:private rejected-cases
  {'sh10-authenticated-uninitialized-read-scenario
   {:rule "L10-UNINIT-READ"
    :reason :operation-before-initialization
    :operation :read
    :facts []}
   'sh10-authenticated-use-after-move-scenario
   {:rule "C9-USE-AFTER-MOVE"
    :reason :operation-after-move
    :operation :read
    :facts [:initialize :move]}
   'sh10-authenticated-double-consume-scenario
   {:rule "C9-USE-AFTER-CONSUME"
    :reason :operation-after-consume
    :operation :consume
    :facts [:initialize :consume]}
   'sh10-authenticated-invalid-escape-scenario
   {:rule "C9-BORROW-ESCAPE"
    :reason :borrow-outlives-owner
    :operation :escape-borrow
    :facts [:initialize :borrow-immutable]}})

(deftest sh10-initialization-move-source-api-and-fixtures-are-bounded
  (let [functions (:functions @c9-plan)
        policy (invoke-c9 'sh10-authenticated-owned-mutable-policy [])]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @c9-plan)))
    (is (= :meta (get-in @c9-plan [:module :profile])))
    (is (= :jvm (get-in @c9-plan [:module :target])))
    (is (= {:arity 3 :params ['bound 'verification 'scenario]}
           (select-keys
            (get functions
                 'sh10-build-authenticated-owned-mutable-analysis)
            [:arity :params])))
    (is (= {:arity 4
            :params ['bound 'verification 'scenario 'candidate]}
           (select-keys
            (get functions
                 'sh10-verify-authenticated-owned-mutable-analysis)
            [:arity :params])))
    (is (= :gravity/sh10-authenticated-owned-mutable-policy
           (:artifact policy)))
    (is (= :owned-mutable (:ownership-kind policy)))
    (is (= :owned-mutable-slot-over-authenticated-primitive-value
           (:storage-model policy)))
    (is (= #{:initialize :read :borrow-immutable :end-borrow
             :move :consume :escape-borrow}
           (:accepted-events policy)))
    (is (some #{"C9-MOVE-WHILE-BORROWED"} (:diagnostics policy)))
    (is (some #{:owned-mutable-type-inference} (:pending policy)))
    (is (= (slurp
            (path
             (fixture-path "accepted" "initialization-move" ".gravity")))
           (slurp
            (path
             (fixture-path "accepted" "initialization-move" ".qst")))))
    (is (= (slurp
            (path
             (fixture-path
              "rejected" "invalid-initialization-move" ".gravity")))
           (slurp
            (path
             (fixture-path
              "rejected" "invalid-initialization-move" ".qst")))))
    (doseq [plan [accepted-gravity-plan accepted-qst-plan
                  rejected-gravity-plan rejected-qst-plan]]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))))

(deftest sh10-authenticated-sh09-family-accepts-initialize-move-and-consume
  (let [bound (:bound @prepared)
        identities (get (:fact-identities bound) (core-node-id))
        effect-request
        (first (get-in bound [:effected-core :effect-requests]))]
    (is (= 3 (count (set (vals identities)))))
    (doseq [[function expected] accepted-cases]
      (testing (str function)
        (let [gravity-scenario (scenario accepted-gravity-plan function)
              qst-scenario (scenario accepted-qst-plan function)
              gravity-result (analyze gravity-scenario)
              qst-result (analyze qst-scenario)
              ownership (:ownership-result gravity-result)]
          (is (= gravity-scenario qst-scenario))
          (is (= gravity-result qst-result))
          (is (= :accepted (:status gravity-result)))
          (is (= :accepted (:status ownership)))
          (is (= :authenticated-sh09-owned-mutable-initialization-move-consume
                 (:scope gravity-result)))
          (is (= (:operations expected)
                 (mapv :operation (:ownership-facts gravity-result))))
          (is (= (:initialization expected)
                 (get-in ownership [:state :initialization])))
          (is (= (:availability expected)
                 (get-in ownership [:state :availability])))
          (is (= identities
                 (select-keys
                  (:preserves gravity-result)
                  [:type-fact-id :effect-fact-id :capability-proof-id])))
          (is (= (:source-span effect-request)
                 (get-in gravity-result [:preserves :source-span])))
          (is (= (:origin-chain effect-request)
                 (get-in gravity-result [:preserves :origin-chain])))
          (is (= (:identity-input bound)
                 (get-in gravity-result
                         [:identity-input :sh09-identity-input])))
          (is (= :passed
                 (:status
                  (verify-analysis gravity-scenario gravity-result)))))))))

(deftest sh10-authenticated-sh09-family-rejects-required-invalid-cases
  (let [bound (:bound @prepared)
        identities (get (:fact-identities bound) (core-node-id))]
    (doseq [[function expected] rejected-cases]
      (testing (str function)
        (let [gravity-scenario (scenario rejected-gravity-plan function)
              qst-scenario (scenario rejected-qst-plan function)
              gravity-result (analyze gravity-scenario)
              qst-result (analyze qst-scenario)
              diagnostic (first (:diagnostics gravity-result))]
          (is (= gravity-scenario qst-scenario))
          (is (= gravity-result qst-result))
          (is (= :rejected (:status gravity-result)))
          (is (= :rejected (get-in gravity-result [:ownership-result :status])))
          (is (= (:rule expected) (:diagnostic-id diagnostic)))
          (is (= (:rule expected) (:rule diagnostic)))
          (is (= (:reason expected) (:reason diagnostic)))
          (is (= (:operation expected) (:operation diagnostic)))
          (is (= :ownership-checking (:stage diagnostic)))
          (is (= :error (:severity diagnostic)))
          (is (= :meta (:profile diagnostic)))
          (is (= :jvm (:target diagnostic)))
          (is (map? (:source-span diagnostic)))
          (is (vector? (:generated-origin-chain diagnostic)))
          (is (keyword? (:remediation diagnostic)))
          (is (= (:facts expected)
                 (mapv :operation (:ownership-facts gravity-result))))
          (is (= identities
                 (select-keys
                  (:preserves gravity-result)
                  [:type-fact-id :effect-fact-id :capability-proof-id])))
          (is (= :passed
                 (:status
                  (verify-analysis gravity-scenario gravity-result)))))))))

(deftest sh10-authenticated-sh09-family-fails-closed-on-substitution
  (let [scenario
        (scenario accepted-gravity-plan
                  'sh10-authenticated-initialize-read-scenario)
        accepted (analyze scenario)
        initialize-event (first (:events scenario))
        read-event (second (:events scenario))
        wrong-node (assoc scenario :core-node-id :substituted-node)
        wrong-node-result (analyze wrong-node)
        contradictory-read
        (assoc scenario
               :scenario-id :contradictory-read
               :events [(assoc read-event
                               :borrow-id :not-a-borrow
                               :destination-owner :not-a-move
                               :destination-lifetime-end 65
                               :escape-destination :function-return)])
        duplicate-events
        (assoc scenario
               :scenario-id :duplicate-events
               :events [initialize-event
                        (assoc read-event
                               :event-id (:event-id initialize-event))])
        too-many-events
        (assoc scenario
               :scenario-id :too-many-events
               :events
               (mapv (fn [index]
                       (assoc read-event :event-id index))
                     (range 17)))
        missing-control-path
        (assoc scenario
               :scenario-id :missing-control-path
               :events [(assoc read-event :control-path nil)])
        move-while-borrowed
        (assoc
         scenario
         :scenario-id :move-while-borrowed
         :events
         [initialize-event
          {:event-id :borrow
           :op :borrow-immutable
           :borrow-id :active-borrow
           :destination-owner nil
           :destination-lifetime-end nil
           :escape-destination nil
           :control-path :normal}
          {:event-id :move
           :op :move
           :borrow-id nil
           :destination-owner :destination
           :destination-lifetime-end nil
           :escape-destination nil
           :control-path :normal}])
        altered-bound (assoc (:bound @prepared) :status :rejected)
        altered-bound-result
        (invoke-c9
         'sh10-build-authenticated-owned-mutable-analysis
         [altered-bound (:verification @prepared) scenario])
        altered-fact-bound
        (assoc-in
         (:bound @prepared)
         [:fact-identities (core-node-id) :type-fact-id]
         "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        altered-fact-result
        (invoke-c9
         'sh10-build-authenticated-owned-mutable-analysis
         [altered-fact-bound (:verification @prepared) scenario])
        malformed-results
        (mapv analyze
              [contradictory-read duplicate-events too-many-events
               missing-control-path])
        move-while-borrowed-result (analyze move-while-borrowed)]
    (is (= :rejected (:status wrong-node-result)))
    (is (= "C9-UNSAFE"
           (get-in wrong-node-result [:diagnostics 0 :diagnostic-id])))
    (is (= :scenario-does-not-bind-an-authenticated-sh09-fact
           (get-in wrong-node-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status altered-bound-result)))
    (is (= :untrusted-or-malformed-sh09-effected-core
           (get-in altered-bound-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status altered-fact-result)))
    (is (= :untrusted-or-malformed-sh09-effected-core
           (get-in altered-fact-result [:diagnostics 0 :reason])))
    (doseq [result malformed-results]
      (is (= :rejected (:status result)))
      (is (= "C9-UNSAFE"
             (get-in result [:diagnostics 0 :diagnostic-id])))
      (is (= :malformed-owned-mutable-lifecycle-scenario
             (get-in result [:diagnostics 0 :reason]))))
    (is (= :rejected (:status move-while-borrowed-result)))
    (is (= "C9-MOVE-WHILE-BORROWED"
           (get-in move-while-borrowed-result
                   [:diagnostics 0 :diagnostic-id])))
    (is (= [:initialize :borrow-immutable]
           (mapv :operation
                 (:ownership-facts move-while-borrowed-result))))
    (is (= :rejected
           (:status
            (verify-analysis
             scenario (assoc accepted :ownership-facts [])))))))
