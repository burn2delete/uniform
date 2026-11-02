(ns gravity.pass-execution-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.pass-execution :as execution]))

(defn- sha
  [character]
  (str "sha256:" (apply str (repeat 64 character))))

(def ids
  {:producer (sha \1)
   :input-a (sha \2)
   :input-b (sha \3)
   :output-a (sha \4)
   :output-b (sha \5)
   :compiler (sha \6)
   :capability-policy (sha \7)
   :facets (sha \8)
   :providers (sha \9)
   :package-lock (sha \a)
   :diagnostics-schema (sha \b)
   :dependency-graph (sha \c)
   :build-effects (sha \d)
   :profile (sha \e)
   :target (sha \f)
   :policy (sha \0)
   :provenance (str "sha256:" (apply str (take 64 (cycle "abcdef"))))
   :diagnostics (str "sha256:" (apply str (take 64 (cycle "012345"))))
   :verifier (str "sha256:" (apply str (take 64 (cycle "123abc"))))
   :evidence (str "sha256:" (apply str (take 64 (cycle "456def"))))})

(defn- pass-contract
  ([pass order]
   (pass-contract pass order #{} #{} #{}))
  ([pass order requires preserves regenerates]
   {:pass pass
    :version "1"
    :order order
    :input :gravity/test-ir
    :output :gravity/test-ir
    :requires requires
    :preserves preserves
    :invalidates #{}
    :regenerates regenerates
    :replacement-evidence {}
    :emits #{:gravity/test-output}
    :effects #{}
    :capabilities #{}
    :profiles #{:safe}
    :required-evidence #{:verification-trace}
    :verifier-required? true
    :authority-ceiling :reviewed}))

(defn- request
  [contract input-artifact-ids input-facts]
  {:stage (:pass contract)
   :contract contract
   :producer-binding-id (:producer ids)
   :input-artifact-ids input-artifact-ids
   :external-root-inputs
   (into {}
         (map (fn [artifact-id]
                [artifact-id {:kind (:input contract) :facts input-facts}]))
         (remove #{(:output-a ids) (:output-b ids)} input-artifact-ids))
   :input-facts input-facts
   :semantic-bindings
   {:compiler-id (:compiler ids)
    :capability-policy-id (:capability-policy ids)
    :facet-set-id (:facets ids)
    :provider-manifest-id (:providers ids)
    :package-lock-id (:package-lock ids)
    :diagnostic-schema-id (:diagnostics-schema ids)}
   :dependency-graph-id (:dependency-graph ids)
   :build-effect-replay-id (:build-effects ids)
   :profile-id (:profile ids)
   :target-id (:target ids)
   :policy-ids [(:policy ids)]
   :provenance
   {:provenance-id (:provenance ids)
    :source-path "/diagnostic-only/not-semantic.gravity"
    :metadata {:worker "test"}}
   :diagnostic-stream-id (:diagnostics ids)
   :execution-mode :executed
   :authority
   {:input-authorities (zipmap input-artifact-ids (repeat :reviewed))
    :claimed-level :reviewed
    :scope :test-pass}})

(defn- operations
  [output-id calls]
  {:produce!
   (fn [_request]
     (swap! calls conj :produce)
     {:tree :produced})
   :validate-output!
   (fn [produced _request _contract]
     (swap! calls conj :validate-output)
     (assoc produced :validated? true))
   :artifact-id-of
   (fn [_artifact]
     (swap! calls conj :artifact-id)
     output-id)
   :verifier-reports
   (fn [_artifact request _contract]
     (swap! calls conj :verifier)
     [{:verifier-id (:verifier ids)
       :stage (:stage request)
       :artifact-id output-id
       :status :passed}])
   :evidence-records
   (fn [_artifact _request _contract]
     (swap! calls conj :evidence)
     [{:evidence-id (:evidence ids)
       :kind :verification-trace
       :status :accepted
       :artifact-id output-id
       :authority-level :reviewed}])})

(defn- execute
  [contract input-ids facts output-id]
  (execution/execute-pass! (request contract input-ids facts)
                           (operations output-id (atom []))))

(defn- diagnostic-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (:id (ex-data failure)))))

(defn- diagnostic-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo failure
      (ex-data failure))))

(deftest public-contract-is-exact-private-and-bootstrap-free
  (let [contract (execution/pass-execution-contract)
        api (:public-api contract)
        publics (ns-publics 'gravity.pass-execution)]
    (is (= '#{pass-execution-contract canonical-pass-contract pass-contract-id
              validate-pass-contract! execute-pass!
              validate-execution-receipt! compose-evidence-dag evidence-root}
           (set (keys publics))))
    (is (= (set (keys api)) (set (keys publics))))
    (doseq [[symbol declaration] api]
      (is (= (:arglists declaration) (:arglists (meta (get publics symbol))))))
    (is (nil? (get publics 'namespace-contract)))
    (is (= ['clojure.core 'clojure.set 'gravity.digest]
           (get-in contract [:dependency-direction :requires])))
    (is (some #{'gravity.bootstrap}
              (get-in contract [:dependency-direction :forbids])))
    (is (every? false?
                (map contract
                     [:authoritative? :cache-storage? :pass-implementation?
                      :proof-authority? :release-authority?
                      :self-hosting-authority? :aggregate-authority?])))))

(deftest pass-contracts-have-bounded-canonical-identities
  (let [contract (pass-contract :parse 10 #{:source} #{:source} #{:syntax})
        reordered (into (array-map) (reverse (seq contract)))]
    (is (= (execution/pass-contract-id contract)
           (execution/pass-contract-id reordered)))
    (is (= (execution/canonical-pass-contract contract)
           (execution/canonical-pass-contract reordered)))
    (is (not= (execution/pass-contract-id contract)
              (execution/pass-contract-id (assoc contract :order 10N))))
    (is (= :type-sensitive-integral-tags
           (get-in (execution/pass-execution-contract)
                   [:semantic-ordering :integers])))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/validate-pass-contract!
                            (assoc contract :unknown true)))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/validate-pass-contract!
              (into contract
                    (map (fn [index] [(keyword (str "unknown-" index)) true]))
                    (range 17000))))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/pass-contract-id
                            (with-meta contract {:host-only true})))))
    (is (= "C1-EVIDENCE-DROP"
           (diagnostic-id
            #(execution/validate-pass-contract!
              (assoc contract :invalidates #{:proof}
                              :regenerates #{}
                              :replacement-evidence {})))))
    (is (= contract
           (execution/validate-pass-contract! contract)))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/validate-pass-contract!
                            (assoc contract :effects [:io])))))
    (is (= "D1-PIPELINE-ORDER"
           (diagnostic-id #(execution/validate-pass-contract!
                            (assoc contract :order 0)))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/pass-contract-id
                            (assoc contract :version
                                   (apply str (repeat 700000 "x")))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :pass
                     (keyword (apply str (repeat 699000 \u0001))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :pass
                     (keyword (apply str (repeat 350000 \u0001))
                              (apply str (repeat 350000 \u0002))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :pass
                     (symbol (apply str (repeat 699000 \u0003))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :required-evidence
                     (set (map (comp keyword str) (range 17000))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (.shiftLeft java.math.BigInteger/ONE 13000000))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (bigint (.shiftLeft java.math.BigInteger/ONE
                                        13000000)))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (/ (.shiftLeft java.math.BigInteger/ONE 13000000)
                        3))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (.negate (.shiftLeft java.math.BigInteger/ONE
                                          13000000)))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (bigint (.negate
                              (.shiftLeft java.math.BigInteger/ONE
                                          13000000))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :order
                     (/ (.negate (.shiftLeft java.math.BigInteger/ONE
                                             13000000))
                        3))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/pass-contract-id
              (assoc contract :invalidates
                     (set (map (comp keyword str) (range 17000))))))))))

(deftest execution-is-exact-once-and-receipt-is-complete
  (let [contract (pass-contract :resolve 20 #{:parsed} #{:parsed} #{:resolved})
        calls (atom [])
        result (execution/execute-pass!
                (request contract [(:input-a ids)] #{:parsed})
                (operations (:output-a ids) calls))
        receipt (:receipt result)]
    (is (= [:produce :validate-output :artifact-id :verifier :evidence]
           @calls))
    (is (= {:tree :produced :validated? true} (:artifact result)))
    (is (= #{:parsed :resolved} (:output-facts receipt)))
    (is (= {(:input-a ids)
            {:kind :gravity/test-ir :facts #{:parsed}}}
           (:external-root-inputs receipt)))
    (is (= (execution/pass-contract-id contract)
           (:pass-contract-id receipt)))
    (is (= :executed (:execution-mode receipt)))
    (is (= #{:artifact :schema-version :receipt-id :stage :pass-contract-id
             :producer-binding-id :input-artifact-ids :external-root-inputs
             :output-artifact-id
             :input-facts :output-facts :requires :preserves :invalidates
             :regenerates :replacement-evidence :effects :semantic-bindings
             :dependency-graph-id :build-effect-replay-id :profile-id
             :target-id :policy-ids :provenance :diagnostic-stream-id
             :verifier-reports :evidence-records :execution-mode :authority}
           (set (keys receipt))))
    (is (= {:input-authorities {(:input-a ids) :reviewed}
            :claimed-level :reviewed
            :effective-level :reviewed
            :ceiling :reviewed
            :scope :test-pass
            :authority-contribution? false
            :aggregate-authoritative? false}
           (:authority receipt)))
    (doseq [field [:producer-binding-id :input-artifact-ids
                   :output-artifact-id :semantic-bindings
                   :dependency-graph-id :build-effect-replay-id :profile-id
                   :target-id :policy-ids :provenance :diagnostic-stream-id
                   :verifier-reports :evidence-records]]
      (is (contains? receipt field)))))

(deftest diagnostic-provenance-does-not-pollute-semantic-identity
  (let [contract (pass-contract :resolve 20)
        base (request contract [(:input-a ids)] #{})
        changed (assoc base :provenance
                       {:provenance-id (:provenance ids)
                        :source-path "/another/checkout/source.gravity"
                        :metadata {:elapsed-ms 9999 :machine "ephemeral"}})
        receipt-a (:receipt (execution/execute-pass!
                             base (operations (:output-a ids) (atom []))))
        receipt-b (:receipt (execution/execute-pass!
                             changed (operations (:output-a ids) (atom []))))
        dag-a (execution/compose-evidence-dag [receipt-a] [contract])
        dag-b (execution/compose-evidence-dag [receipt-b] [contract])]
    (is (= (:receipt-id receipt-a) (:receipt-id receipt-b)))
    (is (not= (:provenance receipt-a) (:provenance receipt-b)))
    (is (= (execution/evidence-root dag-a)
           (execution/evidence-root dag-b)))))

(deftest operation-and-request-schemas-are-strict
  (let [contract (pass-contract :resolve 20)
        base-request (request contract [(:input-a ids)] #{})
        base-ops (operations (:output-a ids) (atom []))]
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/execute-pass!
                            base-request (dissoc base-ops :produce!)))))
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/execute-pass!
                            base-request (assoc base-ops :produce! :not-a-fn)))))
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/execute-pass!
                            base-request (assoc base-ops :extra identity)))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/execute-pass!
              base-request
              (into base-ops
                    (map (fn [index]
                           [(keyword (str "operation-" index)) identity]))
                    (range 17000))))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :unknown true) base-ops))))
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :input-artifact-ids
                                   [(:input-a ids) (:input-a ids)])
                            base-ops))))
    (is (= "D1-ARTIFACT-GAP"
           (diagnostic-id #(execution/execute-pass!
                            (-> base-request
                                (assoc :input-artifact-ids [])
                                (assoc :external-root-inputs {})
                                (assoc-in [:authority :input-authorities] {}))
                            base-ops))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :input-artifact-ids
                                   (vec (repeat 17000 (:input-a ids))))
                            base-ops))))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/execute-pass!
                            (assoc-in base-request
                                      [:external-root-inputs (:input-a ids) :kind]
                                      :gravity/wrong-ir)
                            base-ops))))
    (is (= "D1-ARTIFACT-GAP"
           (diagnostic-id #(execution/execute-pass!
                            (assoc-in base-request
                                      [:external-root-inputs (:input-a ids) :extra]
                                      true)
                            base-ops))))
    (is (= "C1-EVIDENCE-DROP"
           (diagnostic-id #(execution/execute-pass!
                            (assoc-in base-request
                                      [:external-root-inputs (:input-a ids) :facts]
                                      #{:unclaimed})
                            base-ops))))
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :execution-mode
                                   :revalidated-reuse)
                            base-ops))))
    (is (= "C16-POLICY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :authority
                                   {:input-authorities
                                    {(:input-a ids) :non-authoritative}
                                    :claimed-level :reviewed
                                    :scope :test-pass})
                            base-ops))))
    (doseq [blank-scope ["" " \t\n"]]
      (is (= "C16-POLICY"
             (diagnostic-id #(execution/execute-pass!
                              (assoc-in base-request [:authority :scope]
                                        blank-scope)
                              base-ops)))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :policy-ids
                                   [(:producer ids) (:policy ids)])
                            base-ops))))
    (is (= "C16-KEY"
           (diagnostic-id #(execution/execute-pass!
                            (assoc base-request :policy-ids
                                   (vec (repeat 17000 (:policy ids))))
                            base-ops))))
    (let [too-deep (nth (iterate vector :leaf) 70)]
      (is (= "C16-KEY"
             (diagnostic-id
              #(execution/execute-pass!
                (assoc-in base-request [:provenance :metadata]
                          {:nested too-deep})
                base-ops)))))))

(deftest execution-does-not-emit-after-a-failed-phase
  (let [contract (pass-contract :resolve 20)
        base-request (request contract [(:input-a ids)] #{})]
    (doseq [failed-phase [:produce! :validate-output! :artifact-id-of
                          :verifier-reports :evidence-records]]
      (let [calls (atom [])
            base (operations (:output-a ids) calls)
            failing (assoc base failed-phase
                           (fn [& _]
                             (swap! calls conj [:failed failed-phase])
                             (throw (ex-info "failure" {:phase failed-phase}))))]
        (is (thrown? clojure.lang.ExceptionInfo
                     (execution/execute-pass! base-request failing)))
        (is (= 1 (count (filter #{[:failed failed-phase]} @calls))))))))

(deftest output-validator-failure-short-circuits-verification-and-evidence
  (let [contract (pass-contract :resolve 20)
        calls (atom [])
        base (operations (:output-a ids) calls)
        failing
        (assoc base :validate-output!
               (fn [& _]
                 (swap! calls conj :validate-output-attempt)
                 (throw (ex-info "invalid pass output" {:phase :output}))))]
    (is (thrown? clojure.lang.ExceptionInfo
                 (execution/execute-pass!
                  (request contract [(:input-a ids)] #{}) failing)))
    (is (= [:produce :validate-output-attempt] @calls))
    (is (not-any? #{:artifact-id :verifier :evidence} @calls))))

(deftest required-c18-evidence-is-not-optional
  (let [contract (pass-contract :resolve 20)
        base-request (request contract [(:input-a ids)] #{})
        base (operations (:output-a ids) (atom []))]
    (is (= "C18-EVIDENCE"
           (diagnostic-id #(execution/execute-pass!
                            base-request
                            (assoc base :verifier-reports (fn [& _] []))))))
    (is (= "C18-EVIDENCE"
           (diagnostic-id #(execution/execute-pass!
                            base-request
                            (assoc base :evidence-records (fn [& _] []))))))
    (is (= "C18-EVIDENCE"
           (diagnostic-id
            #(execution/execute-pass!
              base-request
              (assoc base :verifier-reports
                     (fn [artifact request contract]
                       (let [record ((:verifier-reports base)
                                     artifact request contract)]
                         (vec (concat record record)))))))))
    (is (= "C18-EVIDENCE"
           (diagnostic-id
            #(execution/execute-pass!
              base-request
              (assoc base :evidence-records
                     (fn [artifact request contract]
                       (let [record ((:evidence-records base)
                                     artifact request contract)]
                         (vec (concat record record)))))))))
    (is (= "C18-EVIDENCE"
           (diagnostic-id
            #(execution/execute-pass!
              base-request
              (assoc base :evidence-records
                     (fn [artifact request contract]
                       (let [record (first ((:evidence-records base)
                                            artifact request contract))]
                         [record (assoc record :evidence-id (:input-b ids))])))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/execute-pass!
              base-request
              (assoc base :verifier-reports
                     (fn [artifact request contract]
                       (let [report (first ((:verifier-reports base)
                                            artifact request contract))]
                         [(into report
                                (map (fn [index]
                                       [(keyword (str "report-" index)) true]))
                                (range 17000))])))))))))

(deftest invalidations-require-exact-replacement-evidence
  (let [contract (assoc (pass-contract :optimize 30)
                        :invalidates #{:range-proof}
                        :replacement-evidence
                        {:range-proof :range-proof-mapping})
        base (operations (:output-a ids) (atom []))
        complete
        (assoc base :evidence-records
               (fn [_artifact _request _contract]
                 [{:evidence-id (:evidence ids)
                   :kind :verification-trace
                   :status :accepted
                   :artifact-id (:output-a ids)
                   :authority-level :reviewed}
                  {:evidence-id (:input-b ids)
                   :kind :range-proof-mapping
                   :status :accepted
                   :artifact-id (:output-a ids)
                   :authority-level :reviewed}]))]
    (is (= contract (execution/validate-pass-contract! contract)))
    (is (= {:range-proof :range-proof-mapping}
           (get-in (execution/execute-pass!
                    (request contract [(:input-a ids)] #{}) complete)
                   [:receipt :replacement-evidence])))
    (is (= "C18-EVIDENCE"
           (diagnostic-id #(execution/execute-pass!
                            (request contract [(:input-a ids)] #{}) base))))
    (is (= "C1-EVIDENCE-DROP"
           (diagnostic-id
            #(execution/validate-pass-contract!
              (assoc contract :replacement-evidence
                     {:another-fact :range-proof-mapping})))))))

(deftest diagnostics-carry-normative-context
  (let [contract (pass-contract :resolve 20)
        req (request contract [(:input-a ids)] #{})
        data (diagnostic-data
              #(execution/execute-pass!
                req (assoc (operations (:output-a ids) (atom []))
                           :produce! :not-a-function)))]
    (is (= "C16-ENTRY" (:id data)))
    (is (= :resolve (:pass data)))
    (is (= (:input-a ids) (:artifact-id data)))
    (is (= (:profile ids) (:profile-id data)))
    (is (= (:target ids) (:target-id data)))
    (is (string? (:remediation data)))))

(deftest evidence-can-only-lower-effective-authority
  (let [contract (pass-contract :resolve 20)
        base (operations (:output-a ids) (atom []))
        low-evidence
        (assoc base :evidence-records
               (fn [_artifact _request _contract]
                 [{:evidence-id (:evidence ids)
                   :kind :verification-trace
                   :status :accepted
                   :artifact-id (:output-a ids)
                   :authority-level :non-authoritative}]))
        receipt (:receipt (execution/execute-pass!
                           (request contract [(:input-a ids)] #{})
                           low-evidence))]
    (is (= :reviewed (get-in receipt [:authority :claimed-level])))
    (is (= :non-authoritative
           (get-in receipt [:authority :effective-level])))))

(deftest receipts-revalidate-with-exactly-once-evidence-operations
  (let [contract (pass-contract :resolve 20)
        receipt (:receipt (execute contract [(:input-a ids)] #{}
                                   (:output-a ids)))
        calls (atom [])
        validators
        {:validate-diagnostic-stream!
         (fn [_ _] (swap! calls conj :diagnostics))
         :validate-verifier-report!
         (fn [_ _] (swap! calls conj :verifier))
         :validate-evidence-record!
         (fn [_ _] (swap! calls conj :evidence))}]
    (is (= receipt
           (execution/validate-execution-receipt!
            receipt contract validators)))
    (is (= [:diagnostics :verifier :evidence] @calls))
    (is (= "C16-STALE"
           (diagnostic-id
            #(execution/validate-execution-receipt!
              (assoc receipt :target-id (:input-b ids)) contract validators))))
    (is (= "C16-ENTRY"
           (diagnostic-id
            #(execution/validate-execution-receipt!
              receipt contract (assoc validators :unknown identity)))))
    (let [before @calls]
      (is (= "C16-STALE"
             (diagnostic-id
              #(execution/validate-execution-receipt!
                (assoc receipt :target-id (:input-b ids))
                contract validators))))
      (is (= before @calls)))
    (let [before @calls]
      (is (= "C1-EVIDENCE-DROP"
             (diagnostic-id
              #(execution/validate-execution-receipt!
                (assoc-in receipt
                          [:external-root-inputs (:input-a ids) :facts]
                          #{:unclaimed})
                contract validators))))
      (is (= before @calls)))
    (let [before @calls]
      (is (= "C1-PASS-CONTRACT"
             (diagnostic-id
              #(execution/validate-execution-receipt!
                (assoc-in receipt
                          [:external-root-inputs (:input-a ids) :kind]
                          :gravity/wrong-ir)
                contract validators))))
      (is (= before @calls)))
    (let [before @calls]
      (is (= "D1-ARTIFACT-GAP"
             (diagnostic-id
              #(execution/validate-execution-receipt!
                (assoc receipt :input-artifact-ids [])
                contract validators))))
      (is (= before @calls)))))

(deftest receipt-validator-failures-short-circuit-later-validators
  (let [contract (pass-contract :resolve 20)
        receipt (:receipt (execute contract [(:input-a ids)] #{}
                                   (:output-a ids)))]
    (let [calls (atom [])
          validators
          {:validate-diagnostic-stream!
           (fn [& _]
             (swap! calls conj :diagnostics-attempt)
             (throw (ex-info "diagnostic stream rejected" {})))
           :validate-verifier-report!
           (fn [& _] (swap! calls conj :verifier))
           :validate-evidence-record!
           (fn [& _] (swap! calls conj :evidence))}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (execution/validate-execution-receipt!
                    receipt contract validators)))
      (is (= [:diagnostics-attempt] @calls)))
    (let [calls (atom [])
          validators
          {:validate-diagnostic-stream!
           (fn [& _] (swap! calls conj :diagnostics))
           :validate-verifier-report!
           (fn [& _]
             (swap! calls conj :verifier-attempt)
             (throw (ex-info "verifier report rejected" {})))
           :validate-evidence-record!
           (fn [& _] (swap! calls conj :evidence))}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (execution/validate-execution-receipt!
                    receipt contract validators)))
      (is (= [:diagnostics :verifier-attempt] @calls)))))

(deftest evidence-dag-is-order-invariant-and-authority-monotone
  (let [first-contract (pass-contract :resolve 10 #{:parsed} #{:parsed}
                                      #{:resolved})
        first-receipt (:receipt (execute first-contract [(:input-a ids)]
                                         #{:parsed} (:output-a ids)))
        second-contract (pass-contract :lower 20 #{:parsed :resolved}
                                       #{:parsed :resolved} #{:lowered})
        second-receipt (:receipt (execute second-contract [(:output-a ids)]
                                          (:output-facts first-receipt)
                                          (:output-b ids)))
        dag-a (execution/compose-evidence-dag
               [first-receipt second-receipt]
               [first-contract second-contract])
        dag-b (execution/compose-evidence-dag
               [second-receipt first-receipt]
               [second-contract first-contract])]
    (is (= dag-a dag-b))
    (is (= #{:artifact :schema-version :root-receipt-id :receipts :contracts
             :edges :authority :evidence-root-id}
           (set (keys dag-a))))
    (is (= (:evidence-root-id dag-a) (execution/evidence-root dag-a)))
    (is (= [{:from (:receipt-id first-receipt)
             :to (:receipt-id second-receipt)}]
           (:edges dag-a)))
    (is (= {:effective-level :reviewed
            :authority-contribution? false
            :aggregate-authoritative? false}
           (:authority dag-a)))
    (is (= "C16-STALE"
           (diagnostic-id #(execution/evidence-root
                            (assoc dag-a :root-receipt-id
                                   (:input-b ids))))))
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/evidence-root
                            (assoc dag-a :unknown true)))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/evidence-root
              (into dag-a
                    (map (fn [index] [(keyword (str "dag-" index)) true]))
                    (range 17000))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/evidence-root
              (with-meta dag-a {:host-only true})))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/evidence-root
              (update dag-a :edges
                      (fn [edges]
                        (assoc edges 0
                               (with-meta (first edges)
                                 {:host-only true}))))))))
    (is (= "C16-KEY"
           (diagnostic-id
            #(execution/evidence-root
              (update dag-a :authority with-meta {:host-only true})))))
    (is (= "C16-STALE"
           (diagnostic-id #(execution/evidence-root
                            (assoc dag-a :edges [])))))
    (is (= "C16-STALE"
           (diagnostic-id #(execution/evidence-root
                            (assoc-in dag-a [:authority :effective-level]
                                      :non-authoritative)))))))

(deftest evidence-dag-rejects-invalid-compositions
  (let [plain-a (pass-contract :a 10)
        plain-b (pass-contract :b 20)
        a (:receipt (execute plain-a [(:input-a ids)] #{} (:output-a ids)))
        b-disconnected (:receipt (execute plain-b [(:input-b ids)] #{}
                                          (:output-b ids)))
        b-connected (:receipt (execute plain-b [(:output-a ids)] #{}
                                       (:output-b ids)))
        late (pass-contract :late 30)
        early (pass-contract :early 5)
        late-receipt (:receipt (execute late [(:input-a ids)] #{}
                                    (:output-a ids)))
        early-receipt (:receipt (execute early [(:output-a ids)] #{}
                                     (:output-b ids)))
        fact-producer (pass-contract :fact-producer 10 #{} #{} #{:actual})
        fact-consumer (pass-contract :fact-consumer 20 #{:different}
                                     #{:different} #{})
        fact-a (:receipt (execute fact-producer [(:input-a ids)] #{}
                                  (:output-a ids)))
        fact-b (:receipt (execute fact-consumer [(:output-a ids)] #{:different}
                                  (:output-b ids)))
        cycle-a (:receipt (execute plain-a [(:output-b ids)] #{}
                                   (:output-a ids)))
        cycle-b (:receipt (execute plain-b [(:output-a ids)] #{}
                                   (:output-b ids)))
        unexplained-request (assoc (request plain-a [(:input-a ids)] #{})
                                   :external-root-inputs {})
        unexplained (:receipt
                     (execution/execute-pass!
                      unexplained-request
                      (operations (:output-a ids) (atom []))))
        low-request (assoc (request plain-a [(:input-a ids)] #{})
                           :authority
                           {:input-authorities {(:input-a ids)
                                                :non-authoritative}
                            :claimed-level :non-authoritative
                            :scope :test-pass})
        low-a (:receipt
               (execution/execute-pass!
                low-request (operations (:output-a ids) (atom []))))
        output-a-contract (assoc plain-a :output :gravity/a-ir)
        input-b-contract (assoc plain-b :input :gravity/b-ir)
        ir-a (:receipt (execute output-a-contract [(:input-a ids)] #{}
                               (:output-a ids)))
        ir-b (:receipt (execute input-b-contract [(:output-a ids)] #{}
                               (:output-b ids)))]
    (is (= "C16-ENTRY"
           (diagnostic-id #(execution/compose-evidence-dag
                            [a a] [plain-a]))))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/compose-evidence-dag
                            [a] [plain-b]))))
    (is (= "D1-ARTIFACT-GAP"
           (diagnostic-id #(execution/compose-evidence-dag
                            [a b-disconnected] [plain-a plain-b]))))
    (is (= "D1-PIPELINE-ORDER"
           (diagnostic-id #(execution/compose-evidence-dag
                            [late-receipt early-receipt] [late early]))))
    (is (= "C1-EVIDENCE-DROP"
           (diagnostic-id #(execution/compose-evidence-dag
                            [fact-a fact-b] [fact-producer fact-consumer]))))
    (is (= "D1-PIPELINE-ORDER"
           (diagnostic-id #(execution/compose-evidence-dag
                            [cycle-a cycle-b] [plain-a plain-b]))))
    (is (= "D1-ARTIFACT-GAP"
           (diagnostic-id #(execution/compose-evidence-dag
                            [a] []))))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/compose-evidence-dag
                            [a] [plain-a plain-b]))))
    (is (= "D1-ARTIFACT-GAP"
           (diagnostic-id #(execution/compose-evidence-dag
                            [unexplained] [plain-a]))))
    (is (= "C16-POLICY"
           (diagnostic-id #(execution/compose-evidence-dag
                            [low-a b-connected] [plain-a plain-b]))))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id #(execution/compose-evidence-dag
                            [ir-a ir-b]
                            [output-a-contract input-b-contract]))))
    (is (= "C1-PASS-CONTRACT"
           (diagnostic-id
            #(execution/compose-evidence-dag
              [(assoc-in a
                         [:external-root-inputs (:input-a ids) :kind]
                         :gravity/wrong-ir)]
              [plain-a]))))
    (is (= (execution/evidence-root
            (execution/compose-evidence-dag [a b-connected]
                                            [plain-a plain-b]))
           (execution/evidence-root
            (execution/compose-evidence-dag [b-connected a]
                                            [plain-b plain-a]))))))
