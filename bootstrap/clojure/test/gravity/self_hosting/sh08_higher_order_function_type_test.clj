(ns gravity.self-hosting.sh08-higher-order-function-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_higher_order_function_type_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 HO2 test source is not on the classpath"
                {:id "STD08-HO2-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "STD08-HO2-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
(def ^:private bridge-relative-path
  (str fixture-root "/higher_order_function_type_bridge.gravity"))
(def ^:private envelope-relative-path
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")

(def ^:private bridge-plan
  (delay
    (let [source-path (str (.resolve @root bridge-relative-path))
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(def ^:private ho1-test-namespace
  (delay
    (require 'gravity.self-hosting.sh08-higher-order-function-value-test)
    'gravity.self-hosting.sh08-higher-order-function-value-test))

(def ^:private ho1-cache-limit 2)
(def ^:private ho1-cache (atom {}))
(def ^:private ho1-cache-order (atom []))

(declare path source-hash byte-count)

(defn sh08-ho2-reset-ho1-cache! []
  (reset! ho1-cache {})
  (reset! ho1-cache-order [])
  nil)

(def ^:private exact-ho1-verification-checks
  #{:request-replayed? :request-bound? :function-value-facts-bound?
    :call-facts-bound? :evaluation-order-bound?
    :path-neutral-identity-bound? :provenance-separated?
    :candidate-recomputed?})

(defn- exact-ho1-verification? [verification result]
  (let [checks (:checks verification)]
    (and (= #{:artifact :schema-version :status :checks :failed-checks
              :candidate}
            (set (keys verification)))
         (= :gravity/sh08-higher-order-function-value-verification
            (:artifact verification))
         (= 1 (:schema-version verification))
         (= :passed (:status verification))
         (= [] (:failed-checks verification))
         (= exact-ho1-verification-checks (set (keys checks)))
         (every? true? (vals checks))
         (= result (:candidate verification)))))

(defn- ho1-run-cache-eligible? [run]
  (and (= :accepted (get-in run [:bridge-request :status]))
       (= :accepted (get-in run [:result :status]))
       (exact-ho1-verification? (:verification run) (:result run))
       (= (:verification run) (:fresh-verification run))
       (map? (:request run))))

(defn- ho1-cache-store! [key run]
  (when (ho1-run-cache-eligible? run)
    (let [order (vec (remove #{key} @ho1-cache-order))
          next-order (conj order key)
          evicted (when (> (count next-order) ho1-cache-limit)
                    (first next-order))
          retained-order (if evicted
                           (vec (rest next-order))
                           next-order)]
      (swap! ho1-cache
             (fn [entries]
               (let [entries (if evicted (dissoc entries evicted) entries)]
                 (assoc entries key run))))
      (reset! ho1-cache-order retained-order)
      run)))

(defn- ho1-cache-count [] (count @ho1-cache))

(use-fixtures
  :once
  (fn [tests]
    (sh08-ho2-reset-ho1-cache!)
    (try
      (tests)
      (finally
        (sh08-ho2-reset-ho1-cache!)))))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- invoke-bridge [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-higher-order-function-type
    :compiler-artifact-plan? true}
   @bridge-plan function arguments))

(defn- ho1-var [name]
  (or (ns-resolve @ho1-test-namespace name)
      (throw (ex-info "SH-08 HO1 harness function is unavailable"
                      {:id "STD08-HO2-HO1-HARNESS" :name name}))))

(defn- ho1-call [name arguments]
  (apply (deref (ho1-var name)) arguments))

(defn- ho1-cache-key [family basename extension function-name apply-name]
  (let [source-path (path (str fixture-root "/" family "/" basename extension))
        source-text (slurp source-path)
        ho1-bridge-path
        (path (str fixture-root "/higher_order_function_value_bridge.gravity"))
        ho1-bridge-text (slurp ho1-bridge-path)]
    {:source {:logical-fixture [family basename extension]
              :actual-source-path source-path
              :content-hash (source-hash source-text)
              :byte-count (byte-count source-text)}
     :ho1-bridge-revision {:content-hash (source-hash ho1-bridge-text)
                           :byte-count (byte-count ho1-bridge-text)}
     :request-identity {:artifact
                        :gravity/sh08-higher-order-function-value-request
                        :schema-version 1
                        :profile :meta
                        :target :jvm
                        :function-value-name function-name
                        :apply-function-name apply-name}}))

(defn- ho1-run-cached [family basename extension function-name apply-name]
  (let [key (ho1-cache-key family basename extension function-name apply-name)]
    (if (contains? @ho1-cache key)
      (get @ho1-cache key)
      (let [request
            (ho1-call 'request-for
                      [family basename extension function-name apply-name])
            run0 (assoc (ho1-call 'run-request [request]) :request request)
            fresh-verification
            (ho1-call 'invoke-bridge
                      ['sh08-verify-higher-order-function-value-result
                       [request (:bridge-request run0) (:result run0)]])
            run (assoc run0 :fresh-verification fresh-verification)]
        (ho1-cache-store! key run)
        run))))

(defn- ho1-request-for [family basename extension function-name apply-name]
  (ho1-call 'request-for
            [family basename extension function-name apply-name]))

(defn- source-hash [source-text]
  (str "sha256:" (bootstrap/sha256-hex source-text)))

(defn- byte-count [source-text]
  (alength
   (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)))

(defn- canonical-id [value]
  (bootstrap/p15-s23-c6c10-canonical-digest
   "<sh08-ho2-test>" value))

(defn- host-exact-id [domain preimage]
  ;; This host-owned boundary binds exact EDN bytes without imposing the
  ;; narrower C6 carrier width on an already verified upstream graph. Stream
  ;; the bytes so the full graph is not copied into a second giant String.
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        sink (java.io.OutputStream/nullOutputStream)
        stream (java.security.DigestOutputStream. sink digest)
        writer (java.io.OutputStreamWriter.
                stream java.nio.charset.StandardCharsets/UTF_8)]
    (binding [*out* writer]
      (pr {:domain domain :preimage preimage}))
    (.flush writer)
    (.close writer)
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and % 0xff))
                     (.digest digest))))))

(defn- function-shapes [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

(defn- bridge-source-revision []
  (let [source-text (slurp (path bridge-relative-path))
        builder 'sh08-build-higher-order-function-type-request
        verifier 'sh08-verify-higher-order-function-type-result]
    {:owner :sh-types
     :source-language :gravity
     :logical-source-path
     "self-hosting/sh-08/higher-order-function-type-bridge"
     :source-content-hash (source-hash source-text)
     :source-byte-count (byte-count source-text)
     :plan-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
       @bridge-plan))
     :functions-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest (:functions @bridge-plan))
     :function-count (count (:functions @bridge-plan))
     :function-shapes (function-shapes @bridge-plan)
     :builder-function builder
     :builder-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions @bridge-plan) builder))
     :verifier-function verifier
     :verifier-semantic-hash
     (bootstrap/p15-s23-c11-mir-digest
      (get (:functions @bridge-plan) verifier))}))

(defn- host-resolution [domain preimage]
  (let [digest (host-exact-id domain preimage)]
    {:algorithm :sha256
     :authority :clojure-host
     :domain domain
     :preimage preimage
     :observed-id digest
     :computed-id digest
     :status :passed}))

(defn- compiled-local-call-arity-mismatches [plan]
  (let [arities (into {}
                      (map (fn [[name function]]
                             [name (:arity function)]))
                      (:functions plan))
        mismatches (atom [])]
    (walk/prewalk
     (fn [value]
       (when (and (map? value)
                  (= :function-call (:op value))
                  (contains? arities (:function value))
                  (not= (get arities (:function value))
                        (count (:args value))))
         (swap! mismatches conj
                {:function (:function value)
                 :expected (get arities (:function value))
                 :actual (count (:args value))}))
       value)
     (mapv :instructions (vals (:functions plan))))
    @mismatches))

(defn- compiled-local-calls [plan function]
  (let [calls (atom [])]
    (walk/prewalk
     (fn [value]
       (when (and (map? value) (= :function-call (:op value)))
         (swap! calls conj (:function value)))
       value)
     (get-in plan [:functions function :instructions]))
    @calls))

(defn- incomplete-function-types [value]
  (->> (tree-seq coll? seq value)
       (filter #(and (map? %)
                     (= :function (:kind %))
                     (or (contains? % :parameters)
                         (contains? % :return))))
       (remove #(= #{:kind :parameters :return :latent-effects
                     :capabilities :ownership :throws :profile :target}
                   (set (keys %))))
       vec))

(defn- observation [run]
  (let [request (:request run)
        result (:result run)
        function-fact (first (:function-value-facts result))
        call-fact (first (:call-facts result))
        core (get-in request [:sh07-context :canonical-core-artifact])]
    (invoke-bridge
     'ho2-derived-observation
     [core function-fact call-fact])))

(defn- typed-request [run actual-path]
  (let [fresh-verification
        (ho1-call 'invoke-bridge
                  ['sh08-verify-higher-order-function-value-result
                   [(:request run) (:bridge-request run) (:result run)]])
        _ (when-not (= (:verification run) fresh-verification)
            (throw (ex-info "HO1 verifier recomputation mismatch"
                            {:id "STD08-HO2-HO1-RECOMPUTE"})))
        ho1-preimage
        {:request (:request run)
         :candidate (:result run)
         :verification fresh-verification
         :context (get-in run [:request :sh07-context])
         :canonical-core-artifact-id
         (get-in run [:request :sh07-context
                      :canonical-core-artifact :artifact-id])}
        ho1-authentication
        (host-resolution :gravity/sh08-ho2-ho1-exact-input-v1 ho1-preimage)
        revision (bridge-source-revision)
        bridge-authentication
        (host-resolution :gravity/sh08-ho2-bridge-revision-v1 revision)]
    {:artifact :gravity/sh08-higher-order-function-typed-core-request
   :schema-version 1
   :ho1-request (:request run)
   :ho1-result (:result run)
   :ho1-verification fresh-verification
   :ho1-verification-id (:observed-id ho1-authentication)
   :ho1-authentication ho1-authentication
   :bridge-revision revision
   :bridge-authentication bridge-authentication
   :signature-observations [(observation run)]
   :actual-source-path actual-path}))

(defn- run-typed-request [request]
  (let [bridge-request
        (invoke-bridge
         'sh08-build-higher-order-function-type-request [request])
        result
        (invoke-bridge
         'sh08-bind-higher-order-function-type-result
         [request bridge-request])]
    {:request request
     :bridge-request bridge-request
     :result result
     :verification
     (invoke-bridge
      'sh08-verify-higher-order-function-type-result
      [request bridge-request result])}))

(defn- false-checks [checks]
  (->> checks
       (keep (fn [[name passed?]] (when-not (= true passed?) name)))
       sort
       vec))

(defn- ho1-primary-preflight [run typed]
  (let [ho1-request (:request run)
        ho1-result (:result run)
        ho1-verification (:verification run)
        fresh-verification (:fresh-verification run)
        artifact (:sh07-artifact ho1-request)
        sh07-verification (:sh07-verification ho1-request)
        context (:sh07-context ho1-request)
        core (:canonical-core-artifact context)
        predicates
        {:sh07-artifact-accepted? (= :accepted (:status artifact))
         :sh07-verification-passed? (= :passed (:status sh07-verification))
         :sh07-failed-checks-empty? (empty? (:failed-checks sh07-verification))
         :sh07-checks-all-true? (empty? (false-checks
                                         (:checks sh07-verification)))
         :context-accepted? (= :accepted (:artifact-status context))
         :context-id-bound? (= (:artifact-id context) (:artifact-id artifact))
         :core-id-bound? (= (:artifact-id core) (:artifact-id artifact))
         :ho1-bridge-accepted? (= :accepted
                                  (get-in run [:bridge-request :status]))
         :ho1-result-accepted? (= :accepted (:status ho1-result))
         :ho1-verification-passed? (= :passed (:status ho1-verification))
         :ho1-verification-failures-empty?
         (empty? (:failed-checks ho1-verification))
         :ho1-candidate-bound? (= ho1-result (:candidate ho1-verification))
         :fresh-verification-exact? (= ho1-verification fresh-verification)
         :result-core-id-bound?
         (= (:artifact-id core) (:canonical-core-artifact-id ho1-result))
         :gravity-sh07-lineage-valid?
         (= true (invoke-bridge 'ho2-sh07-lineage-valid? [ho1-request]))
         :gravity-envelope-lineage-valid?
         (= true (invoke-bridge 'ho2-envelope-lineage-valid? [ho1-request]))
         :gravity-ho1-result-shape-valid?
         (= true
            (invoke-bridge
             'ho2-ho1-result-shape-valid?
             [{:ho1-result ho1-result
               :ho1-verification ho1-verification}]))
         :gravity-ho1-input-valid?
         (= true (invoke-bridge 'ho2-ho1-input-valid? [typed]))
         :gravity-ho1-authentication-valid?
         (= true (invoke-bridge 'ho2-ho1-authentication-valid? [typed]))
         :gravity-bridge-authentication-valid?
         (= true (invoke-bridge 'ho2-bridge-authentication-valid? [typed]))}
        summary
        {:predicates predicates
         :failed-predicates (false-checks predicates)
         :sh07-artifact-status (:status artifact)
         :sh07-artifact-diagnostics (:diagnostics artifact)
         :sh07-verification-status (:status sh07-verification)
         :sh07-verification-failed-checks (:failed-checks sh07-verification)
         :sh07-verification-false-checks
         (false-checks (:checks sh07-verification))
         :ho1-bridge-status (get-in run [:bridge-request :status])
         :ho1-bridge-diagnostics (get-in run [:bridge-request :diagnostics])
         :ho1-result-status (:status ho1-result)
         :ho1-result-diagnostics (:diagnostics ho1-result)
         :ho1-verification-status (:status ho1-verification)
         :ho1-verification-failed-checks (:failed-checks ho1-verification)
         :ho1-verification-false-checks
         (false-checks (:checks ho1-verification))
         :ids {:artifact (:artifact-id artifact)
               :context (:artifact-id context)
               :core (:artifact-id core)
               :result (:canonical-core-artifact-id ho1-result)}}]
    (assoc summary :ok? (empty? (:failed-predicates summary)))))

(defn- accepted-run [extension]
  (let [run
        (ho1-run-cached "accepted" "function-value-typed-call" extension
                        'sh08-ho2-identity 'sh08-ho2-apply-one)]
    (run-typed-request
     (typed-request
      run
      (path (str fixture-root "/accepted/function-value-typed-call"
                 extension))))))

(deftest sh08-ho2-bridge-has-narrow-exported-api
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @bridge-plan)))
  (is (= {:arity 0 :params []}
         (select-keys
          (get-in @bridge-plan
                  [:functions 'sh08-higher-order-function-type-policy])
          [:arity :params])))
  (is (= {:arity 1 :params ['request]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-build-higher-order-function-type-request])
          [:arity :params])))
  (is (= {:arity 2 :params ['request 'bridge-request]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-bind-higher-order-function-type-result])
          [:arity :params])))
  (is (= {:arity 3 :params ['request 'bridge-request 'candidate]}
         (select-keys
          (get-in @bridge-plan
                  [:functions
                   'sh08-verify-higher-order-function-type-result])
          [:arity :params])))
  (is (= [] (compiled-local-call-arity-mismatches @bridge-plan)))
  (let [revision (bridge-source-revision)]
    (is (= (count (:functions @bridge-plan)) (:function-count revision)))
    (is (= (function-shapes @bridge-plan) (:function-shapes revision)))
    (is (= (bootstrap/p15-s23-c11-mir-digest
            (get-in @bridge-plan
                    [:functions
                     'sh08-verify-higher-order-function-type-result]))
           (:verifier-semantic-hash revision))))
  (is (some #{'ho2-ho1-input-valid?}
            (compiled-local-calls
             @bridge-plan 'sh08-build-higher-order-function-type-request)))
  (is (some #{'ho2-ho1-authentication-valid?}
            (compiled-local-calls
             @bridge-plan 'sh08-build-higher-order-function-type-request)))
  (is (some #{'ho2-bridge-authentication-valid?}
            (compiled-local-calls
             @bridge-plan 'sh08-build-higher-order-function-type-request))))

(deftest sh08-ho2-exact-host-resolution-dry-run
  (let [preimage {:request {:exact :request}
                  :candidate {:exact :candidate}
                  :verification {:exact :verification}
                  :context {:exact :context}
                  :canonical-core-artifact-id
                  (canonical-id {:exact :canonical-core})}
        resolution
        (host-resolution :gravity/sh08-ho2-ho1-exact-input-v1 preimage)
        hostile-id (canonical-id {:hostile :same-length})]
    (is (= true
           (invoke-bridge 'ho2-host-resolution-valid?
                          [resolution
                           :gravity/sh08-ho2-ho1-exact-input-v1
                           preimage (:observed-id resolution)])))
    (is (= false
           (invoke-bridge 'ho2-host-resolution-valid?
                          [(assoc resolution :observed-id hostile-id)
                           :gravity/sh08-ho2-ho1-exact-input-v1
                           preimage (:computed-id resolution)])))
    (is (= false
           (invoke-bridge 'ho2-host-resolution-valid?
                          [resolution
                           :gravity/sh08-ho2-ho1-exact-input-v1
                           (assoc preimage :candidate {:coordinated :change})
                           (:observed-id resolution)])))
    (is (= #{:kind :parameters :return :latent-effects :capabilities
             :ownership :throws :profile :target}
           (set
            (keys
             (invoke-bridge
              'ho2-complete-function-type
              [[:gravity.type/i64] :gravity.type/i64 #{} #{}
               :immutable #{} :meta :jvm])))))
    (let [diagnostic
          (invoke-bridge
           'ho2-diagnostic
           ["STD08-HO2-CONSTRAINT" :nested-c7-reason :expected
            {:source {:node-id :n7 :syntax-id :s7
                      :span {:file "fixture.gravity" :start 7 :end 8}
                      :binding-id :b7 :origin-chain [:source :generated]
                      :generated-origin :macro-7}
             :profile :meta :target :jvm}])]
      (is (= :n7 (get-in diagnostic [:source :node-id])))
      (is (= :s7 (get-in diagnostic [:source :syntax-id])))
      (is (= :b7 (get-in diagnostic [:source :binding-id])))
      (is (= [:source :generated]
             (get-in diagnostic [:source :origin-chain])))
      (is (= [{:rule "C7-TYPE-MISMATCH"
               :reason :nested-c7-reason}]
             (get-in diagnostic [:facts :c7-reasons]))))
    (let [wide-preimage {:verified-upstream-container (vec (range 140))}
          wide-resolution
          (host-resolution :gravity/sh08-ho2-wide-carrier-preflight-v1
                           wide-preimage)]
      (is (= 140 (count (:verified-upstream-container wide-preimage))))
      (is (= :passed (:status wide-resolution)))
      (is (= (:observed-id wide-resolution)
             (:computed-id wide-resolution)))
      (is (= 71 (count (:observed-id wide-resolution)))))))

(deftest sh08-ho2-policy-binds-bounds-and-nonclaims
  (let [policy (invoke-bridge 'sh08-higher-order-function-type-policy [])
        complete-function-type
        {:kind :function
         :parameters [:gravity.type/i64]
         :return :gravity.type/i64
         :latent-effects #{}
         :capabilities #{}
         :ownership :shared
         :throws #{}
         :profile :meta
         :target :jvm}
        environment-entry
        {:kind :function
         :id :synthetic-function-value
         :type complete-function-type}]
    (is (= :monomorphic-named-function-value-one-hop-signature-inference
           (:scope policy)))
    (is (= {:parameters [:gravity.type/i64]
            :return :gravity.type/i64}
           (:accepted-signature policy)))
    (is (= 8 (get-in policy [:bounds :maximum-signatures])))
    (is (= 16 (get-in policy [:bounds :maximum-arguments])))
    (is (= 32 (get-in policy [:bounds :maximum-constraints])))
    (is (= :available-authenticated-call-anchors-without-reconstruction
           (:request-level-absence-diagnostic policy)))
    (is (some #{:polymorphism} (:pending policy)))
    (is (some #{:captures} (:pending policy)))
    (is (= true
           (invoke-bridge 'ho2-sh07-carrier-status-valid? [:accepted])))
    (is (= false
           (invoke-bridge 'ho2-sh07-carrier-status-valid? [:passed])))
    (is (= true
           (invoke-bridge
            'ho2-ho1-verification-artifact-valid?
            [:gravity/sh08-higher-order-function-value-verification])))
    (is (= false
           (invoke-bridge
            'ho2-ho1-verification-artifact-valid?
            [:gravity/sh08-higher-order-function-verification])))
    (is (= [] (incomplete-function-types environment-entry)))
    (is (= [(dissoc complete-function-type :throws)]
           (incomplete-function-types
            (assoc environment-entry :type
                   (dissoc complete-function-type :throws)))))))

(deftest sh08-ho2-ho1-cache-is-bounded-and-resettable
  (sh08-ho2-reset-ho1-cache!)
  (let [make-run
        (fn [tag]
          (let [result {:status :accepted :tag tag}
                verification
                {:artifact
                 :gravity/sh08-higher-order-function-value-verification
                 :schema-version 1
                 :status :passed
                 :checks (zipmap exact-ho1-verification-checks
                                 (repeat true))
                 :failed-checks []
                 :candidate result}]
            {:request {:tag tag}
             :bridge-request {:status :accepted}
             :result result
             :verification verification
             :fresh-verification verification}))
        accepted-a (make-run :a)
        accepted-b (make-run :b)
        accepted-c (make-run :c)
        incomplete
        (let [verification
              (update (:verification accepted-a) :checks
                      dissoc :request-bound?)]
          (assoc accepted-a :verification verification
                 :fresh-verification verification))
        failed
        (let [verification
              (assoc (:verification accepted-a)
                     :failed-checks [:request-bound?])]
          (assoc accepted-a :verification verification
                 :fresh-verification verification))
        false-check
        (let [verification
              (assoc-in (:verification accepted-a)
                        [:checks :request-bound?] false)]
          (assoc accepted-a :verification verification
                 :fresh-verification verification))]
    (ho1-cache-store! :a accepted-a)
    (ho1-cache-store! :b accepted-b)
    (ho1-cache-store! :c accepted-c)
    (is (= 2 (ho1-cache-count)))
    (is (not (contains? @ho1-cache :a)))
    (is (contains? @ho1-cache :b))
    (is (contains? @ho1-cache :c))
    (ho1-cache-store! :incomplete incomplete)
    (ho1-cache-store! :failed failed)
    (ho1-cache-store! :false-check false-check)
    (is (= 2 (ho1-cache-count)))
    (is (not-any? #(contains? @ho1-cache %)
                  [:incomplete :failed :false-check]))
    (sh08-ho2-reset-ho1-cache!)
    (is (= 0 (ho1-cache-count)))))

(deftest sh08-ho2-accepts-authenticated-monomorphic-signature
  (doseq [extension [".gravity" ".qst"]]
    (let [run (accepted-run extension)
          bridge-request (:bridge-request run)
          result (:result run)
          verification (:verification run)]
      (is (= :accepted (:status bridge-request))
          [extension (:diagnostics bridge-request)])
      (is (= :gravity/sh08-higher-order-function-typed-core
             (:artifact result)) extension)
      (is (= :passed (:status verification))
          [extension (:diagnostics verification)])
      (is (= [:gravity.type/i64]
             (get-in result [:signature-facts 0 :parameter-types]))
          extension)
      (is (= :gravity.type/i64
             (get-in result [:signature-facts 0 :return-type]))
          extension)
      (is (= :gravity.type/i64
             (get-in result [:indirect-call-facts 0 :result-type]))
          extension)
      (is (= [:selected-identity-parameter
              :apply-callable-parameter
              :apply-value-parameter]
             (mapv :parameter-role (:parameter-facts result))))
      (is (= [:outer-value-argument :inner-value-reference :identity-body
              :indirect-call-result :outer-function-value-argument
              :outer-apply-result]
             (mapv :role (:node-type-facts result))))
      (is (= [:ho2-c1-outer-literal-to-value
              :ho2-c2-value-to-identity-parameter
              :ho2-c3-identity-return :ho2-c4-call-results]
             (mapv :constraint-id (:constraint-ledger result))))
      (is (= [:outer-value-type :parameter-type :return-type
              :call-result-type]
             (get-in result [:derivation :trace])))
      (is (= (get-in result [:parameter-facts 0 :binding-id])
             (get-in result
                     [:indirect-call-facts 0 :identity-parameter-binding-id])))
      (is (= (get-in result [:parameter-facts 2 :binding-id])
             (get-in result
                     [:indirect-call-facts 0 :value-parameter-binding-id])))
      (let [inferred-output
            (select-keys
             result [:signature-facts :parameter-facts :indirect-call-facts
                     :node-type-facts :type-environment :constraint-ledger
                     :derivation])]
        (is (not-any? #{:Dynamic :dynamic :Any :Object :unknown}
                      (tree-seq coll? seq inferred-output)))
        (is (= [] (incomplete-function-types inferred-output)) extension))
      (is (= (get-in run [:request :ho1-authentication])
             (:ho1-authentication result)))
      (is (= (get-in result [:identity-input :signature-facts])
             [(select-keys (first (:signature-facts result))
                          [:signature-id :function-value-id :name :kind
                           :parameters :parameter-types :return-type
                           :fixed-arity :effects :latent-effects
                           :capabilities :ownership
                           :throws :evidence :profile :target])])))))

(deftest sh08-ho2-identity-is-path-neutral-and-provenance-separated
  (let [a (accepted-run ".gravity")
        b (accepted-run ".qst")]
      (is (= (get-in a [:result :identity-input])
           (get-in b [:result :identity-input])))
    (is (not= (get-in a [:result :provenance])
              (get-in b [:result :provenance])))
    (is (not= (get-in a [:result :provenance :source-path])
              (get-in b [:result :provenance :source-path])))
    (is (= :direct-acyclic-derivation
           (get-in a [:result :derivation :model])))
    (is (= :derived (get-in a [:result :derivation :status])))
    (is (= 4 (get-in a [:result :derivation :step-count])))))

(deftest sh08-ho2-binds-outer-literal-through-inner-reference
  (let [core
        {:module {:profile :meta :target :jvm}
         :calls [{:core-node-id :outer-call
                  :operator-node-id :outer-operator
                  :operator-binding-id :apply-binding
                  :argument-node-ids [:function-value :outer-literal]
                  :ordered-evaluation-node-ids
                  [:outer-operator :function-value :outer-literal]
                  :evaluation-order :operator-then-arguments}
                 {:core-node-id :identity-body
                  :operator-binding-id :helper-binding}]
         :nodes
         [{:node-id :outer-call :core-form :call
           :children [:outer-operator :function-value :outer-literal]}
          {:node-id :function-value :core-form :reference
           :attributes {:binding-id :identity-binding}}
          {:node-id :outer-literal :core-form :literal
           :attributes {:literal-kind :integer}
           :source {:syntax-id :outer-literal-syntax
                    :span {:file "synthetic.gravity" :start 7 :end 8}
                    :origin-chain [:source]}}
          {:node-id :inner-call :children [:inner-value]}
          {:node-id :inner-value :core-form :reference
           :attributes {:binding-id :apply-value-binding}
           :evaluation {:owner-function-syntax-id :apply-function}}
          {:node-id :identity-body :core-form :reference
           :attributes {:binding-id :identity-parameter}
           :source {:syntax-id :identity-body-syntax
                    :span {:file "synthetic.gravity" :start 10 :end 11}
                    :origin-chain [:source]}
           :evaluation {:owner-function-syntax-id :identity-function}}
          {:node-id :outer-operator :core-form :reference
           :attributes {:binding-id :apply-binding}}]
         :function-records
         [{:function-syntax-id :apply-function
           :parameter-binding-ids [:apply-callable-binding
                                   :apply-value-binding]}
          {:function-syntax-id :identity-function
           :definition-binding-id :identity-binding}]}
        function-fact
        {:function-syntax-id :identity-function
         :definition-binding-id :identity-binding
         :parameter-binding-ids [:identity-parameter]
         :body-core-node-id :identity-body}
        call-fact
        {:call-core-node-id :inner-call
         :caller-function-syntax-id :apply-function
         :callable-parameter-binding-id :apply-callable-binding
         :argument-core-node-ids [:inner-value]
         :outer-apply-call-core-node-id :outer-call
         :outer-apply-operator-binding-id :apply-binding
         :outer-call-evaluation-order :operator-then-arguments
         :outer-function-value-argument-core-node-id :function-value
         :outer-function-value-binding-id :identity-binding
         :outer-function-value-argument-ordinal 0}]
    (is (= true
           (invoke-bridge 'ho2-inner-argument-valid?
                          [core call-fact])))
    (is (= true
           (invoke-bridge 'ho2-outer-argument-valid?
                          [core function-fact call-fact])))
    (is (= :outer-literal
           (get (invoke-bridge 'ho2-derived-observation
                               [core function-fact call-fact])
                :argument-node-id)))
    (is (= :inner-value
           (get (invoke-bridge 'ho2-derived-observation
                               [core function-fact call-fact])
                :inner-argument-node-id)))
    (is (= {:node-id :identity-body
            :source {:syntax-id :identity-body-syntax
                     :span {:file "synthetic.gravity" :start 10 :end 11}
                     :origin-chain [:source]}
            :binding-id :identity-parameter
            :profile :meta
            :target :jvm}
           (invoke-bridge 'ho2-node-diagnostic-fact
                          [core :identity-body])))
    (is (= {:node-id :outer-literal
            :source {:syntax-id :outer-literal-syntax
                     :span {:file "synthetic.gravity" :start 7 :end 8}
                     :origin-chain [:source]}
            :binding-id :apply-value-binding
            :profile :meta
            :target :jvm}
           (invoke-bridge 'ho2-outer-value-diagnostic-fact
                          [core call-fact])))
    (let [node-children-permuted
          (assoc-in core [:nodes 0 :children]
                    [:outer-operator :outer-literal :function-value])
          arguments-permuted
          (-> core
              (assoc-in [:calls 0 :argument-node-ids]
                        [:outer-literal :function-value])
              (assoc-in [:calls 0 :ordered-evaluation-node-ids]
                        [:outer-operator :outer-literal :function-value]))
          ordered-permuted
          (assoc-in core [:calls 0 :ordered-evaluation-node-ids]
                    [:outer-operator :outer-literal :function-value])
          substituted
          (assoc-in core [:nodes 4 :attributes :binding-id] :other-binding)]
      (is (= false
             (invoke-bridge 'ho2-outer-argument-valid?
                            [node-children-permuted function-fact call-fact])))
      (is (= false
             (invoke-bridge 'ho2-outer-argument-valid?
                            [arguments-permuted function-fact call-fact])))
      (is (= false
             (invoke-bridge 'ho2-outer-argument-valid?
                            [ordered-permuted function-fact call-fact])))
      (is (= false
             (invoke-bridge 'ho2-inner-argument-valid?
                            [substituted call-fact]))))))

(deftest sh08-ho2-rejects-unresolved-signature-structurally
  (let [run
        (ho1-run-cached "accepted" "function-value-typed-call" ".gravity"
                        'sh08-ho2-identity 'sh08-ho2-apply-one)
        authenticated-request
        (typed-request
         run
         (path (str fixture-root
                    "/accepted/function-value-typed-call.gravity")))
        preflight (ho1-primary-preflight run authenticated-request)
        request (assoc authenticated-request :signature-observations [])
        result (run-typed-request request)
        core (get-in request [:ho1-request :sh07-context
                              :canonical-core-artifact])
        call-fact (get-in request [:ho1-result :call-facts 0])
        call-node-id (:call-core-node-id call-fact)
        call-node (some #(when (= call-node-id (:node-id %)) %)
                        (:nodes core))
        call-source (:source call-node)
        diagnostic-source
        (get-in result [:bridge-request :diagnostics 0 :source])]
    (is (:ok? preflight) (pr-str preflight))
    (when-not (:ok? preflight)
      (throw (ex-info "HO1 primary preflight failed before HO2 semantics"
                      preflight)))
    (is (= :rejected (get-in result [:bridge-request :status])))
    (is (= "STD08-HO2-CONSTRAINT"
           (get-in result [:bridge-request :diagnostics 0 :rule])))
    (is (= :unresolved-signature-observation
           (get-in result [:bridge-request :diagnostics 0 :reason])))
    (is (= call-node-id (:node-id diagnostic-source)))
    (is (= (:syntax-id call-source) (:syntax-id diagnostic-source)))
    (is (nil? (:span diagnostic-source)))
    (is (nil? (:binding-id diagnostic-source)))
    (is (= [] (:origin-chain diagnostic-source)))
    (is (= [{:rule "C7-ANNOTATION"
             :reason :unresolved-signature-observation}]
           (get-in result
                   [:bridge-request :diagnostics 0 :facts :c7-reasons])))
    (is (= :passed (get-in result [:verification :status])))))

(deftest sh08-ho2-rejects-request-level-conflicting-observations
  (let [run
        (ho1-run-cached "accepted" "function-value-typed-call" ".gravity"
                        'sh08-ho2-identity 'sh08-ho2-apply-one)
        request
        (typed-request
         run
         (path (str fixture-root
                    "/accepted/function-value-typed-call.gravity")))
        preflight (ho1-primary-preflight run request)
        first-observation (first (:signature-observations request))
        conflict-observation
        (assoc first-observation :return-type :gravity.type/f32)
        conflict-request
        (assoc request :signature-observations
               [first-observation conflict-observation])
        result (run-typed-request conflict-request)]
    (is (:ok? preflight) (pr-str preflight))
    (when-not (:ok? preflight)
      (throw (ex-info "HO1 primary preflight failed before HO2 conflict"
                      preflight)))
    (is (= :rejected (get-in result [:bridge-request :status])))
    (is (= "STD08-HO2-CONSTRAINT"
           (get-in result [:bridge-request :diagnostics 0 :rule])))
    (is (= :conflicting-signature-observations
           (get-in result [:bridge-request :diagnostics 0 :reason])))
    (is (= :passed (get-in result [:verification :status])))))

(deftest sh08-ho2-rejects-hostile-upstream-and-type-substitutions
  (let [run (accepted-run ".gravity")
        request (:request run)
        hostile-id (canonical-id {:hostile :valid-length-substitution})
        altered-ho1
        (assoc-in request [:ho1-result :canonical-core-artifact-id]
                  hostile-id)
        coordinated-ho1
        (-> request
            (assoc-in [:ho1-result :canonical-core-artifact-id] hostile-id)
            (assoc-in [:ho1-verification :candidate
                       :canonical-core-artifact-id] hostile-id)
            (assoc :ho1-verification-id hostile-id))
        altered-envelope
        (assoc-in request [:ho1-request :envelope :semantic-envelope-id]
                  hostile-id)
        altered-sh07
        (assoc-in request [:ho1-request :sh07-context :identity-input]
                  {:altered true})
        altered-observation
        (assoc-in request [:signature-observations 0 :parameter-type]
                  :gravity.type/f32)
        coordinated-bridge
        (-> request
            (assoc-in [:bridge-revision :source-content-hash] hostile-id)
            (assoc-in [:bridge-revision :plan-semantic-hash] hostile-id)
            (assoc-in [:bridge-revision :functions-semantic-hash] hostile-id)
            (assoc-in [:bridge-revision :builder-semantic-hash] hostile-id)
            (assoc-in [:bridge-revision :verifier-semantic-hash] hostile-id))]
    (doseq [[candidate expected-rule]
            [[altered-ho1 "STD08-HO2-HO1"]
             [coordinated-ho1 "STD08-HO2-HO1"]
             [altered-envelope "STD08-HO2-ENVELOPE"]
             [altered-sh07 "STD08-HO2-SH07"]
             [altered-observation "STD08-HO2-CONSTRAINT"]
             [coordinated-bridge "STD08-HO2-SCHEMA"]]]
      (let [result
            (invoke-bridge
             'sh08-build-higher-order-function-type-request [candidate])]
        (is (= :rejected (:status result)))
        (is (= expected-rule (get-in result [:diagnostics 0 :rule])))
        (is (= :meta (get-in result [:diagnostics 0 :profile])))
        (is (= :jvm (get-in result [:diagnostics 0 :target])))
        (is (vector?
             (get-in result
                     [:diagnostics 0 :facts :upstream-diagnostics]))))))
  (let [run (accepted-run ".gravity")
        altered-result
        (assoc-in (:result run) [:signature-facts 0 :return-type]
                  :gravity.type/f32)
        verification
        (invoke-bridge
         'sh08-verify-higher-order-function-type-result
         [(:request run) (:bridge-request run) altered-result])]
    (is (= :rejected (:status verification)))
    (is (= "STD08-HO2-VERIFY"
           (get-in verification [:diagnostics 0 :rule])))))

(deftest sh08-ho2-fixture-pairs-are-co-canonical
  (doseq [[family basename]
          [["accepted" "function-value-typed-call"]]]
    (is (= (slurp (path (str fixture-root "/" family "/" basename
                              ".gravity")))
           (slurp (path (str fixture-root "/" family "/" basename
                              ".qst")))))))
