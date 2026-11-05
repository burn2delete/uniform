(ns gravity.self-hosting.sh15-stream-verification-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

;; Compile only the owning C15 source. The reused helper has delayed C12 plans;
;; none of those plans or native dependencies are realized by these tests.
(def ^:private plan
  (delay
    (#'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan
     "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")))

(defn- c15 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-stream-development :compiler-artifact-plan? true}
   @plan function arguments))

(defn- verify [stream] (c15 'verify-c15-diagnostic-stream [stream]))

(defn- digest [request]
  ;; The existing seed resolver hashes the actual request, outside Uniform.
  (bootstrap/sh03-reader-canonical-digest
   "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity" request))

(defn- empty-stream []
  {:stage :read-source :input-artifact nil :output-artifact nil
   :diagnostics []
   :summary-counts {:error 0 :warning 0 :info 0 :hint 0 :internal-error 0}
   :deterministic-ordering-key :diagnostic-id
   :redaction-policy :private-unassessed :rendering-version "c15-private-v1"})

(def ^:private reader-span
  {:source "/source.gravity" :byte-start 0 :byte-end 1
   :start {:byte 0 :char 0 :line 1 :column 1 :column-unit :unicode-scalar}
   :end {:byte 1 :char 1 :line 1 :column 2 :column-unit :unicode-scalar}})

(defn- reader-fields [severity]
  ["C2-DELIMITER" severity :read-source
   {:span reader-span :syntax-id nil :artifact :source-unit} []
   [{:kind :source-bytes :path "/source.gravity"}] nil nil
   {:reason :unmatched-delimiter :severity-control severity}
   [{:kind :close-delimiter :message "Close the source form"}] []])

(defn- resolved-row [fields]
  (let [unresolved (c15 'build-c15-diagnostic fields)
        request (:diagnostic-id-request unresolved)
        resolved-digest (digest request)]
    (is (= :gravity/diagnostic (:artifact unresolved)))
    (is (= {:digest-ref 0} (:diagnostic-id unresolved)))
    {:candidate (assoc unresolved :diagnostic-id resolved-digest)
     :binding {:original-fields fields :request request :resolved-digest resolved-digest}}))

(def ^:private reader-rows
  (delay (mapv #(resolved-row (reader-fields %))
               [:error :warning :info :hint :internal-error])))

(defn- stream-for [rows]
  (let [rows (vec (sort-by #(get-in % [:candidate :diagnostic-id]) rows))
        diagnostics (mapv :candidate rows)]
    (assoc (empty-stream)
           :stage (:stage (first diagnostics))
           :input-artifact :source-unit :diagnostics diagnostics
           :diagnostic-bindings (mapv :binding rows)
           :summary-counts (merge (:summary-counts (empty-stream))
                                  (frequencies (map :severity diagnostics))))))

;; Supplied fixtures use the current C5 primary/all and generated-origin shapes.
;; Their structure is under test; they do not authenticate a C5 producer.
(defn- generated-fields []
  (let [source-id (digest {:fixture :source}) syntax-id (digest {:fixture :syntax})
        version (digest {:fixture :producer})
        flat {:file source-id :source "/macro.gravity" :byte-start 0 :byte-end 1
              :scalar-start 0 :scalar-end 1 :line-start 1 :line-end 1
              :column-start 1 :column-end 2}
        span {:primary flat :all [flat] :source "/macro.gravity"}
        definition {:source "/macros.gravity" :byte-start 4 :byte-end 9}
        source {:kind :source :span flat
                :producer {:kind :reader :name 'reader :identity version
                           :source-id source-id :generated-form-id nil}
                :producer-version "reader-v1" :input-syntax-ids [syntax-id]
                :generation-reason :source-read :build-effects []}
        generated {:kind :generated
                   :producer {:kind :macro :name 'example/expand :version version
                              :definition-syntax-id syntax-id}
                   :inputs [syntax-id] :call-site-span span :definition-span definition
                   :generated-span {:kind :generated :producer 'example/expand
                                    :ordinal 0 :call-site-syntax-id syntax-id}
                   :reason :macro-expansion :build-effects []}]
    ["C5-UNRESOLVED" :error :name-resolution
     {:span span :syntax-id syntax-id :artifact source-id}
     [{:role :generated-by :span definition :artifact syntax-id}
      {:role :macro-call-site :span span :artifact syntax-id}]
     [source generated] :hosted :jvm {:reason :unresolved-binding :symbol 'missing}
     [{:kind :declare-or-import-binding}] nil]))

(def ^:private generated-row (delay (resolved-row (generated-fields))))

(defn- rejected [stream rule field reason]
  (let [result (verify stream) diagnostic (first (:diagnostics result))]
    (is (= :rejected (:status result)))
    (is (= :gravity/c15-diagnostic-stream-verification (:artifact result)))
    (is (= rule (:rule diagnostic)))
    (is (= field (:schema-field diagnostic)))
    (is (= reason (get-in diagnostic [:facts :reason])))
    result))

(deftest private-stream-verification-executes-empty-single-and-multiple-records
  (doseq [stream [(empty-stream)
                  (assoc (empty-stream) :stage :type-check :input-artifact :typed-core
                         :output-artifact :checked-core :diagnostic-bindings [])
                  (stream-for [(first @reader-rows)])
                  (stream-for @reader-rows)
                  (stream-for [@generated-row])]]
    (let [result (verify stream)]
      (is (= :passed (:status result)))
      (is (= :private-stream-structure-and-external-digest-equality
             (:verification-status result)))
      (is (= stream (:diagnostic-stream result)))
      (is (= (:summary-counts stream) (:summary-counts result)))
      (is (= (mapv (fn [index diagnostic binding]
                     {:record-index index :diagnostic-id (:diagnostic-id diagnostic)
                      :request (:request binding)})
                   (range) (:diagnostics stream) (:diagnostic-bindings stream))
             (:identity-request-joins result)))
      (is (= [] (:diagnostics result)))
      (is (= result (verify stream)))))
  (let [stream (stream-for @reader-rows)
        row (first (:diagnostics (:diagnostic-stream (verify stream))))]
    (is (= {:error 1 :warning 1 :info 1 :hint 1 :internal-error 1}
           (:summary-counts stream)))
    (doseq [field [:profile :target]]
      (is (contains? row field))
      (is (nil? (get row field))))
    (is (contains? (:primary row) :syntax-id))
    (is (nil? (get-in row [:primary :syntax-id])))
    (is (= reader-span (get-in row [:primary :span]))))
  (let [row (:candidate @generated-row)
        result (verify (stream-for [@generated-row]))]
    (is (= row (get-in result [:diagnostic-stream :diagnostics 0])))
    (is (= [:source :generated] (mapv :kind (:origin-chain row))))
    (is (= [:generated-by :macro-call-site] (mapv :role (:related row))))))

(deftest actual-span-variants-and-absent-system-context-remain-distinct
  (let [source-id (digest {:fixture :span-source})
        spans [reader-span
               {:file source-id :source "/bytes.gravity" :byte-start 0 :byte-end 2}
               {:source-id :source-unit :actual-source-path "/located.gravity"
                :start {:byte 0 :line 1 :column 1}
                :end {:byte 1 :line 1 :column 2}}]]
    (doseq [span spans]
      (let [row (resolved-row (assoc-in (reader-fields :error) [3 :span] span))
            result (verify (stream-for [row]))]
        (is (= :passed (:status result)))
        (is (= span (get-in result [:diagnostic-stream :diagnostics 0 :primary :span])))))
    (let [fields ["C15-SCHEMA" :error :compiler-diagnostics
                  {:span nil :syntax-id nil :artifact nil} [] [] nil nil
                  {:reason :unavailable-input} [{:kind :supply-input}] [:input-omitted]]
          stream (assoc (stream-for [(resolved-row fields)]) :input-artifact nil)
          result (verify stream)]
      (is (= :passed (:status result)))
      (is (= {:span nil :syntax-id nil :artifact nil}
             (get-in result [:diagnostic-stream :diagnostics 0 :primary])))
      (is (nil? (get-in result [:diagnostic-stream :input-artifact]))))))

(deftest malformed-stream-envelopes-return-actual-c15-rejections
  (doseq [[stream field reason]
          (concat
           [[nil :stream :stream-map-required]
            [[] :stream :stream-map-required]
            [(list :stream) :stream :unsupported-canonical-carrier]]
           (for [field (keys (empty-stream))]
             [(dissoc (empty-stream) field) field :missing-stream-field])
           (for [[field value reason]
                 [[:stage "read-source" :invalid-stage]
                  [:input-artifact 17 :invalid-input-artifact]
                  [:output-artifact [] :invalid-output-artifact]
                  [:diagnostics {} :invalid-diagnostics]
                  [:summary-counts "zero" :invalid-summary-counts]
                  [:deterministic-ordering-key nil :invalid-ordering-key]
                  [:redaction-policy true :invalid-redaction-policy]
                  [:rendering-version "" :invalid-rendering-version]]]
             [(assoc (empty-stream) field value) field reason]))]
    (rejected stream "C15-SCHEMA" field reason))
  (is (= :rejected (:status (verify (assoc (empty-stream) :stage :type-check))))))

(deftest stream-counts-order-and-binding-cardinality-are-checked
  (let [stream (stream-for @reader-rows)]
    (doseq [summary [{:error 5} (assoc (:summary-counts stream) :error -1)
                     (assoc (:summary-counts stream) :error "1")
                     (assoc (:summary-counts stream) :extra 0)]]
      (rejected (assoc stream :summary-counts summary)
                "C15-SCHEMA" :summary-counts :invalid-summary-counts))
    (rejected (assoc-in stream [:summary-counts :error] 99)
              "C15-SCHEMA" :summary-counts :summary-count-mismatch)
    (rejected (assoc stream :diagnostics (vec (reverse (:diagnostics stream)))
                           :diagnostic-bindings (vec (reverse (:diagnostic-bindings stream))))
              "C15-ORDER" :diagnostics :noncanonical-order)
    (rejected (assoc stream :deterministic-ordering-key :rule)
              "C15-ORDER" :deterministic-ordering-key :unsupported-ordering-key)
    (doseq [bindings [[] (conj (:diagnostic-bindings stream)
                              (first (:diagnostic-bindings stream)))]]
      (rejected (assoc stream :diagnostic-bindings bindings)
                "C15-ID" :diagnostic-bindings :binding-count-mismatch))
    (rejected (dissoc stream :diagnostic-bindings)
              "C15-ID" :diagnostic-bindings :binding-count-mismatch)
    (rejected (assoc stream :diagnostic-bindings {})
              "C15-SCHEMA" :diagnostic-bindings :invalid-diagnostic-bindings)
    (rejected (stream-for [(first @reader-rows) (first @reader-rows)])
              "C15-ID" :diagnostic-id :duplicate-diagnostic-id)
    (rejected (assoc stream :stage :compiler-diagnostics)
              "C15-SCHEMA" :stage :record-stage-mismatch)))

(deftest separate-originals-reject-missing-context-and-changed-semantic-data
  (let [stream (stream-for [(first @reader-rows)])
        row (first (:diagnostics stream))]
    (doseq [changed [(dissoc row :profile) (dissoc row :target)
                     (update row :primary dissoc :syntax-id)
                     (assoc row :profile :meta :target :jvm)
                     (assoc-in row [:facts :reason] :other-reason)
                     (assoc-in row [:primary :span :source] "/elsewhere.gravity")
                     (assoc row :origin-chain []) (assoc row :redactions [:applied])]]
      (let [result (rejected (assoc-in stream [:diagnostics 0] changed)
                             "C15-ID" :candidate :candidate-substitution)
            diagnostic (first (:diagnostics result))]
        (is (= 0 (:record-index diagnostic)))
        (is (= (:diagnostic-id row) (:offending-diagnostic-id diagnostic)))
        (is (= :source-unit (:artifact-id diagnostic)))))
    (doseq [binding [nil {} (dissoc (first (:diagnostic-bindings stream)) :original-fields)
                     (assoc (first (:diagnostic-bindings stream)) :extra true)]]
      (rejected (assoc-in stream [:diagnostic-bindings 0] binding)
                "C15-SCHEMA" :diagnostic-bindings :invalid-record-binding))
    (rejected (assoc-in stream [:diagnostic-bindings 0 :original-fields 8] "prose")
              "C15-FACTS" :facts :invalid-facts))
  (let [stream (stream-for [@generated-row])]
    (rejected (assoc-in stream [:diagnostics 0 :origin-chain] [])
              "C15-ID" :candidate :candidate-substitution)
    (rejected (assoc-in stream [:diagnostic-bindings 0 :original-fields 5] [])
              "C15-ORIGIN" :origin-chain :invalid-origin)
    (rejected (assoc-in stream [:diagnostics 0 :related] [])
              "C15-ID" :candidate :candidate-substitution)))

(deftest stale-and-substituted-requests-cannot-replace-the-per-record-join
  (let [stream (stream-for [(first @reader-rows)])
        binding (first (:diagnostic-bindings stream)) request (:request binding)]
    (doseq [changed [(assoc request :ordinal 1) (assoc request :purpose :other)
                     (assoc request :domain :other) (assoc request :extra true)
                     (assoc-in request [:preimage :facts :reason] :other-reason)]]
      (rejected (assoc-in stream [:diagnostic-bindings 0 :request] changed)
                "C15-ID" :diagnostic-id-request :request-substitution)
      ;; Even a coherent candidate/request/digest replacement cannot replace
      ;; the separate originals. Digests here are real seed computations.
      (let [replacement-digest (digest changed)]
        (rejected (-> stream
                      (assoc-in [:diagnostic-bindings 0 :request] changed)
                      (assoc-in [:diagnostic-bindings 0 :resolved-digest] replacement-digest)
                      (assoc-in [:diagnostics 0 :diagnostic-id-request] changed)
                      (assoc-in [:diagnostics 0 :diagnostic-id] replacement-digest))
                  "C15-ID" :diagnostic-id-request :request-substitution)))
    (rejected (assoc-in stream [:diagnostic-bindings 0 :original-fields 8 :reason] :edited)
              "C15-ID" :diagnostic-id-request :request-substitution)
    (rejected (assoc-in stream [:diagnostic-bindings 0 :resolved-digest] (digest {:other :request}))
              "C15-ID" :candidate :candidate-substitution)
    (rejected (assoc-in stream [:diagnostic-bindings 0 :resolved-digest] nil)
              "C15-ID" :resolved-digest :invalid-resolved-digest))
  (let [stream (stream-for @reader-rows)]
    (rejected (assoc stream :diagnostic-bindings (vec (reverse (:diagnostic-bindings stream))))
              "C15-ID" :diagnostic-id-request :request-substitution)))

(deftest wording-is-independent-of-identity-and-flags-confer-no-publication-authority
  (let [row (first @reader-rows)
        reworded (resolved-row (assoc-in (get-in row [:binding :original-fields])
                                        [9 0 :message] "Different human guidance"))]
    (is (= (get-in row [:candidate :diagnostic-id])
           (get-in reworded [:candidate :diagnostic-id])))
    (doseq [stream [(stream-for [row]) (stream-for [reworded])
                    (assoc (stream-for [row]) :redaction-policy :public-safe
                           :public-safe? true :redaction-applied? true)]]
      (let [result (verify stream)]
        (is (= :passed (:status result)))
        (is (= :none (:semantic-authority result)))
        (is (= :not-evaluated (:redaction-status result)))
        (is (not (contains? result :public-safe?)))
        (is (= #{:digest-recomputation :upstream-source-authentication
                 :public-safe-projection :golden-fixture-verification
                 :self-hosting :release :seed-retirement}
               (set (:nonclaims result))))))
    (is (= :rejected
           (:status (verify (assoc (empty-stream) :diagnostics [{}]
                                   :public-safe? true :redaction-applied? true)))))))
