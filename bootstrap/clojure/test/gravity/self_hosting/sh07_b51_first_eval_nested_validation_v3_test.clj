(ns gravity.self-hosting.sh07-b51-first-eval-nested-validation-v3-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "missing SH-07 V3 receipt seam" {:symbol symbol}))))

(defn- private-call [symbol & args]
  (apply @(private-var symbol) args))

(defn- identity-copy [value]
  (with-meta value {::identity-copy true}))

(defn- rejection-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (ex-data failure))))

(deftype SpoofEqualsToken []
  Object
  (equals [_ _] true)
  (hashCode [_] 0))

(defn- synthetic-receipt-fixture []
  (let [root1-function 'sh07-b51-build-template
        receipt-caller 'sh07-b51-root1-admitted-raw-carrier-shape?
        params ['raw]
        receipt-instruction
        {:op :builtin-call
         :function 'sh07-b51-admitted-executable-carrier-validation?
         :args []
         :compiler-internal-caller receipt-caller}
        root1-plan-id
        "sha256:1111111111111111111111111111111111111111111111111111111111111111"
        root8-plan-id
        "sha256:8888888888888888888888888888888888888888888888888888888888888888"
        root1-plan
        {:plan-id root1-plan-id
         :sh07-b51-module-source-id "synthetic-root1-source"
         :source {:path "synthetic-root1.gravity"}
         :functions
         {root1-function
          {:name root1-function
           :arity 1
           :params params
           :instructions [receipt-instruction]}
          receipt-caller
          {:name receipt-caller
           :arity 1
           :params params
           ;; Retain the exact same instruction object so the focused test
           ;; exercises identity validation without compiling a carrier.
           :instructions [receipt-instruction]}}}
        root8-function 'sh07-b51-finalize-rejection
        root8-plan
        {:plan-id root8-plan-id
         :sh07-b51-module-source-id "synthetic-root8-source"
         :source {:path "synthetic-root8.gravity"}
         :functions
         {root8-function
          {:name root8-function :arity 1 :params params
           :instructions [{:op :literal :value nil}]}}}
        root1
        {:root-ordinal 1 :function root1-function :arity 1
         :plan-id root1-plan-id :owner "producer-a"}
        root8
        {:root-ordinal 8 :function root8-function :arity 1
         :plan-id root8-plan-id :owner "root8"}
        public-roots
        (into [root1]
              (map (fn [ordinal]
                     (if (= 8 ordinal)
                       root8
                       {:root-ordinal ordinal
                        :function (symbol (str "synthetic-root-" ordinal))
                        :arity 0
                        :plan-id (str "synthetic-plan-" ordinal)
                        :owner (str "synthetic-owner-" ordinal)}))
                   (range 2 9)))
        execution-bundle {:public-roots public-roots}
        carrier
        {:artifact :synthetic-sh07-v3-carrier
         :semantic-root-hash
         "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
         :compile-census-certificates
         [{:certificate-id
           "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"}
          {:certificate-id
           "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"}]}
        raw {:v18-executable-descriptor carrier}
        arguments [raw]
        descriptors [{:module-source-id "synthetic-root1-source"}
                     {:module-source-id "synthetic-root8-source"}]
        manifest {:manifest-id "synthetic-manifest" :edges []}
        plans [root1-plan root8-plan]
        products
        {:module-source-descriptors descriptors
         :link-manifest manifest
         :module-plans plans
         :execution-bundle execution-bundle}]
    {:carrier carrier
     :raw raw
     :arguments arguments
     :receipt-instruction receipt-instruction
     :root1 root1
     :root8 root8
     :root1-plan root1-plan
     :root8-plan root8-plan
     :products products}))

(defn- register-fixture [fixture root]
  (private-call
   'p15-s23-sh07-b51-register-admitted-runtime-context
   (:products fixture) (:carrier fixture) (:raw fixture)
   {:root root
    :plan (if (= 1 (:root-ordinal root))
            (:root1-plan fixture)
            (:root8-plan fixture))}
   (:arguments fixture) {:focused-v3-test true}))

(defn- unregister! [runtime]
  (private-call 'p15-s23-sh07-b51-unregister-admitted-runtime-context!
                runtime))

(defn- context-for [runtime]
  (private-call 'p15-s23-sh07-b51-admitted-runtime-context runtime))

(defn- slot-value [context]
  (.get ^java.util.concurrent.atomic.AtomicReference
        (:receipt-slot-ref context)))

(defn- owner-value [owner]
  (.get ^java.util.concurrent.atomic.AtomicReference owner))

(defn- pending-call [fixture prior-scope]
  (private-call
   'p15-s23-sh07-b51-runtime-machine-call-action
   (:root1-plan fixture) (:function (:root1 fixture))
   (:arguments fixture) [] prior-scope))

(deftest top-level-receipt-rejects-copied-identities-before-one-shot-cas
  (let [fixture (synthetic-receipt-fixture)
        lease (register-fixture fixture (:root1 fixture))
        runtime (:runtime lease)]
    (try
      (let [context (context-for runtime)
            issued (slot-value context)
            receipt (:receipt-ref issued)
            scope (:scope-ref issued)
            state-ref (:state-ref receipt)
            assert-entry
            #(private-call 'p15-s23-sh07-b51-assert-scoped-function-entry!
                           runtime (:root1-plan fixture)
                           (:function (:root1 fixture)) % scope)
            consume
            #(private-call
              'p15-s23-sh07-b51-consume-admitted-carrier-validation!
              runtime (:root1-plan fixture) %1 (:carrier fixture) %2)]
        (is (true? (assert-entry (:arguments fixture))))
        (testing "a value-equal copied argument vector is not the admitted vector"
          (let [copied (identity-copy (:arguments fixture))
                data (rejection-data #(assert-entry copied))]
            (is (= (:arguments fixture) copied))
            (is (not (identical? (:arguments fixture) copied)))
            (is (= ["L2-BUILTIN-ERROR"
                    :unauthenticated-scoped-function-entry]
                   ((juxt :id :reason) data)))
            (is (= {:state :fresh} (.get state-ref)))))
        (testing "a value-equal copied instruction cannot consume the receipt"
          (let [copied (identity-copy (:receipt-instruction fixture))
                data (rejection-data #(consume copied scope))]
            (is (= (:receipt-instruction fixture) copied))
            (is (not (identical? (:receipt-instruction fixture) copied)))
            (is (= ["L2-BUILTIN-ERROR" :unauthenticated-receipt]
                   ((juxt :id :reason) data)))
            (is (= {:state :fresh} (.get state-ref)))))
        (testing "a different scope cannot consume or mutate the receipt"
          (let [data
                (rejection-data
                 #(consume (:receipt-instruction fixture) (Object.)))]
            (is (= ["L2-BUILTIN-ERROR" :unauthenticated-receipt]
                   ((juxt :id :reason) data)))
            (is (= {:state :fresh} (.get state-ref)))))
        (testing "the exact identities consume once and replay fails closed"
          (is (true? (consume (:receipt-instruction fixture) scope)))
          (is (= {:state :consumed} (.get state-ref)))
          (is (= ["L2-BUILTIN-ERROR" :unauthenticated-receipt]
                 ((juxt :id :reason)
                  (rejection-data
                   #(consume (:receipt-instruction fixture) scope)))))))
      (finally
        (unregister! runtime)
        (java.lang.ref.Reference/reachabilityFence (:context-lease lease))))))

(deftest owner-cleanup-requires-the-exact-issued-payload-object
  (let [fixture (synthetic-receipt-fixture)
        lease (register-fixture fixture (:root1 fixture))
        runtime (:runtime lease)]
    (try
      (let [context (context-for runtime)
            issued (slot-value context)
            receipt (:receipt-ref issued)
            owner (:owner-ref issued)
            copied-issued (identity-copy issued)]
        (is (= issued copied-issued))
        (is (not (identical? issued copied-issued)))
        (is (false?
             (boolean
              (private-call 'p15-s23-sh07-b51-close-issued-owner!
                            context owner copied-issued))))
        (is (identical? issued (slot-value context)))
        (is (= :active (:state (owner-value owner))))
        (is (= {:state :fresh} (owner-value (:state-ref receipt))))
        (is (true?
             (private-call 'p15-s23-sh07-b51-close-issued-owner!
                           context owner issued)))
        (is (= {:state :retired} (slot-value context)))
        (is (= {:state :closed} (owner-value owner)))
        (is (= {:state :invalid} (owner-value (:state-ref receipt)))))
      (finally
        (unregister! runtime)
        (java.lang.ref.Reference/reachabilityFence (:context-lease lease))))))

(deftest a-losing-pending-entry-closes-only-itself
  (let [fixture (synthetic-receipt-fixture)
        lease (register-fixture fixture (:root8 fixture))
        runtime (:runtime lease)
        first-owner (java.util.concurrent.atomic.AtomicReference. nil)
        second-owner (java.util.concurrent.atomic.AtomicReference. nil)]
    (try
      (let [[first-action first-frames] (pending-call fixture nil)
            first-descriptor (nth first-action 4)
            first-lifecycle (:lifecycle-ref first-descriptor)
            first-scope
            (private-call
             'p15-s23-sh07-b51-runtime-machine-enter-pending-root1!
             runtime first-action first-frames nil first-owner)
            context (context-for runtime)
            winner (slot-value context)
            winner-receipt (:receipt-ref winner)
            [second-action second-frames] (pending-call fixture nil)
            second-lifecycle (:lifecycle-ref (nth second-action 4))
            data
            (rejection-data
             #(private-call
               'p15-s23-sh07-b51-runtime-machine-enter-pending-root1!
               runtime second-action second-frames nil second-owner))]
        (is (= ["L2-UNKNOWN-CORE-FORM"
                :unauthenticated-pending-root1-entry]
               ((juxt :id :reason) data)))
        (is (= {:state :closed} (owner-value second-lifecycle)))
        (is (nil? (owner-value second-owner)))
        (is (identical? winner (slot-value context)))
        (is (= :active (:state (owner-value first-lifecycle))))
        (is (= {:state :fresh}
               (owner-value (:state-ref winner-receipt))))
        (is (identical?
             nil
             (private-call
              'p15-s23-sh07-b51-runtime-machine-complete-pending-root1!
              runtime (nth first-frames (- (count first-frames) 2))
              first-scope first-owner)))
        (is (= {:state :retired} (slot-value context)))
        (is (= {:state :closed} (owner-value first-lifecycle)))
        (is (nil? (owner-value first-owner)))
        (is (= {:state :invalid}
               (owner-value (:state-ref winner-receipt)))))
      (finally
        (unregister! runtime)
        (java.lang.ref.Reference/reachabilityFence (:context-lease lease))))))

(deftest nominal-pending-entry-tampering-rejects-with-owner-safe-lifecycle
  (doseq [[label expected-lifecycle alter]
          [["descriptor shallow clone" {:state :pending}
            (fn [action frames prior]
              [(assoc action 4 (into {} (nth action 4))) frames prior])]
           ["copied first instruction" {:state :closed}
            (fn [action frames prior]
              [(assoc action 3 (identity-copy (nth action 3))) frames prior])]
           ["copied sequence frame" {:state :closed}
            (fn [action frames prior]
              [action (assoc frames (dec (count frames))
                             (identity-copy (peek frames))) prior])]
           ["copied prior scope" {:state :closed}
            (fn [action frames prior]
              [action frames (identity-copy prior)])]
           ["altered action arity" {:state :closed}
            (fn [action frames prior]
              [(conj action :unexpected) frames prior])]]]
    (testing label
      (let [fixture (synthetic-receipt-fixture)
            lease (register-fixture fixture (:root8 fixture))
            runtime (:runtime lease)
            prior-scope (with-meta {:scope :prior} {::original true})
            cleanup-owner (java.util.concurrent.atomic.AtomicReference. nil)]
        (try
          (let [[action frames] (pending-call fixture prior-scope)
                lifecycle (:lifecycle-ref (nth action 4))
                [changed-action changed-frames changed-scope]
                (alter action frames prior-scope)
                context (context-for runtime)
                data
                (rejection-data
                 #(private-call
                   'p15-s23-sh07-b51-runtime-machine-enter-pending-root1!
                   runtime changed-action changed-frames changed-scope
                   cleanup-owner))]
            (is (= ["L2-UNKNOWN-CORE-FORM"
                    :unauthenticated-pending-root1-entry]
                   ((juxt :id :reason) data)))
            (is (= {:state :available} (slot-value context)))
            (is (= expected-lifecycle (owner-value lifecycle)))
            (is (nil? (owner-value cleanup-owner))))
          (finally
            (unregister! runtime)
            (java.lang.ref.Reference/reachabilityFence
             (:context-lease lease))))))))

(deftest public-admission-does-not-publish-token-receipt-or-scope-authority
  (let [fixture (synthetic-receipt-fixture)
        proof-var (private-var 'p15-s23-sh07-b51-v4-admission-proof)
        route {:root (:root1 fixture) :plan (:root1-plan fixture)}
        result
        (with-redefs-fn
          {proof-var
           (fn [_ _ _]
             {:authority {:carrier (:carrier fixture)
                          :products (:products fixture)}
              :route route})}
          #(bootstrap/p15-s23-sh07-b51-admit-linked-raw-v4
            (:raw fixture) 1 (:arguments fixture)
            {:benign :preserved
             :sh07-b51-runtime-context-token (Object.)
             :sh07-b51-linked-carrier-v4 :caller-spoof}))
        public-runtime (:runtime result)]
    (is (= (:products fixture) (:products result)))
    (is (= route (select-keys result [:root :plan])))
    (is (= :preserved (:benign public-runtime)))
    (is (nil? (:sh07-b51-runtime-context-token public-runtime)))
    (is (nil? (:sh07-b51-linked-carrier-v4 public-runtime)))
    (is (nil? (:receipt public-runtime)))
    (is (nil? (:scope public-runtime)))
    (is (instance? clojure.lang.IAtom
                   (:sh07-b51-external-call-trace public-runtime)))))

(deftest registry-rejects-value-equal-spoof-even-with-the-real-hash-bucket
  (let [fixture (synthetic-receipt-fixture)
        lease (register-fixture fixture (:root8 fixture))
        runtime (:runtime lease)]
    (try
      (let [real-token (:sh07-b51-runtime-context-token runtime)
            spoof (SpoofEqualsToken.)
            query-constructor
            @(private-var '->P15S23Sh07B51IdentityTokenQuery)
            forced-query
            (query-constructor spoof (System/identityHashCode real-token))
            registry @(private-var 'p15-s23-sh07-b51-admitted-runtime-contexts)
            spoof-runtime
            (assoc runtime :sh07-b51-runtime-context-token spoof)]
        (is (= spoof real-token))
        (is (not (identical? spoof real-token)))
        (is (nil? (.get ^java.util.HashMap registry forced-query)))
        (is (nil? (context-for spoof-runtime)))
        (is (identical? (:context-lease lease) (context-for runtime))))
      (finally
        (unregister! runtime)
        (java.lang.ref.Reference/reachabilityFence (:context-lease lease))))))

(deftest unregister-is-idempotent-and-leaves-no-live-registry-or-receipt-state
  (let [fixture (synthetic-receipt-fixture)
        lease (register-fixture fixture (:root1 fixture))
        runtime (:runtime lease)
        context (:context-lease lease)
        issued (slot-value context)
        receipt (:receipt-ref issued)
        owner (:owner-ref issued)]
    (is (identical? context (context-for runtime)))
    (is (nil? (unregister! runtime)))
    (is (nil? (context-for runtime)))
    (is (= {:state :retired} (slot-value context)))
    (is (= {:state :closed} (owner-value owner)))
    (is (= {:state :invalid} (owner-value (:state-ref receipt))))
    (is (nil? (unregister! runtime)))
    (is (nil? (context-for runtime)))
    (java.lang.ref.Reference/reachabilityFence context)))
