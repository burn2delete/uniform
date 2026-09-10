(ns gravity.self-hosting.sh07-b51-v9-contract-discharge-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt32-materialize :as materialize]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io PushbackReader StringReader]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files Paths]
           [java.util Arrays]))

(def ^:private checked-fuel-keys
  [:maximum-forms :maximum-top-level-forms :maximum-fragment-forms
   :maximum-form-children :maximum-form-depth :maximum-origin-entries
   :maximum-metadata-entries :maximum-alias-records
   :maximum-carrier-nodes :maximum-carrier-depth :maximum-carrier-width
   :maximum-scalar-bytes :maximum-fragments :maximum-module-bindings
   :maximum-bindings :maximum-resolutions :maximum-module-resolutions
   :maximum-effects :maximum-capabilities :maximum-exports
   :maximum-module-core-nodes :maximum-resolved-core-carrier-nodes
   :maximum-resolved-core-carrier-depth :maximum-resolved-core-carrier-width
   :maximum-resolved-core-scalar-bytes :maximum-template-carrier-nodes
   :maximum-template-carrier-depth :maximum-template-carrier-width
   :maximum-template-scalar-bytes :maximum-generated-digest-carrier-nodes
   :maximum-generated-digest-carrier-depth
   :maximum-generated-digest-carrier-width
   :maximum-generated-digest-scalar-bytes :maximum-digest-requests])

(defn- checked-fuel []
  (into {} (map-indexed (fn [index key] [key (+ 100 index)])
                        checked-fuel-keys)))

(defn- proof-fuel [checked]
  (reduce-kv
   (fn [result proof-key executable-key]
     (-> result
         (dissoc executable-key)
         (assoc proof-key (get checked executable-key))))
   checked materialize/fixed-fuel-executable-aliases))

(defn- pre-stages []
  {:l1 {:document :document :source-units [] :grammar-change :none}
   :c2 {:snapshot :snapshot :units [] :source-map {:entries []}
        :diagnostics {:entries []} :literal-records []
        :reader-extensions []}
   :l2 {:owner-selection :owners :core-forms {:entries []}
        :evaluation-order-root "sha256:evaluation"
        :effects-root "sha256:effects"
        :capabilities-root "sha256:capabilities"}
   :c6 {:ast {:entries []} :syntax-core-map {:entries []}
        :desugaring-trace {:entries []} :origins-facts-order []}
   :boot2 {:source :source :compiler :compiler :backend :no-backend
           :dependencies {:entries []} :runtime-assumptions :jvm}
   :boot3 {:modules
           [{:ordinal 0 :module 'example
             :input-artifacts [] :output-artifacts [] :effects [] :facts []}]
           :profile :meta :stage-advanced false}
   :boot4 {:module-manifest {:entries []} :effects {:entries []}
           :capabilities {:entries []} :fact-provenance {:entries []}}})

(defn- collections-by-owner []
  {:l1 {:module-forms [] :top-level-forms [] :fragment-forms []
        :form-children [] :form-depths [] :origin-entries []
        :metadata-entries [] :alias-records []}
   :c2 {:module-carriers []}
   :l2 {:fragments [] :module-bindings [] :bindings []
        :fragment-resolutions [] :module-resolutions [] :effects []
        :capabilities [] :exports []}
   :c6 {:module-core [] :resolved-core-carriers []}
   :boot2 {:template-carriers []}
   :boot3 {:generated-digest-carriers []}
   :boot4 {:module-digest-requests []}})

(def ^:private fuel-source-roots
  {:source-descriptors-root "sha256:source"
   :event-root "sha256:events"
   :semantic-algorithm-source-root "sha256:algorithm"
   :sh06-authority-carrier-id "sha256:sh06"
   :bundle-core-id "sha256:bundle"})

(deftest fixed-fuel-is-the-exact-34-key-checked-core-projection
  (let [checked (checked-fuel)
        proof (proof-fuel checked)]
    (is (= 34 (count checked) (count proof)))
    (is (= checked (materialize/fixed-fuel-executable proof checked)))
    (testing "alias collisions fail before subject construction"
      (is (= :fixed-fuel-executable-mismatch
             (try
               (materialize/fixed-fuel-executable
                (assoc proof :maximum-forms 999) checked)
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))))

(deftest subjects-precede-the-exact-seven-operation-specs
  (let [subjects
        (materialize/build-fuel-subjects fuel-source-roots (pre-stages)
                                         (collections-by-owner))
        operations (materialize/build-fixed-fuel-operation-specs subjects)]
    (is (= 7 (count subjects) (count operations)))
    (is (= (vec (range 7)) (mapv :ordinal subjects) (mapv :ordinal operations)))
    (is (= [:l1 :c2 :l2 :c6 :boot2 :boot3 :boot4]
           (mapv :owner subjects) (mapv :owner operations)))
    (is (= (mapv :subject-id subjects) (mapv :subject-id operations)))
    (is (every? #(= #{:ordinal :owner :kind :source-roots :collections
                      :collection-roots :stage-semantic-root :subject-id}
                    (set (keys %)))
                subjects))
    (is (every? #(not-any? (set (keys %))
                           [:receipt-id :operation-id :witness-id
                            :certificate-id :checker-id])
                subjects))))

(deftest pre-stage-and-source-root-smuggling-fails-closed
  (testing "a later field cannot enter an exact receipt-free prefix"
    (is (= :pre-stage-prefix-shape
           (try
             (materialize/build-fuel-subjects
              fuel-source-roots
              (assoc-in (pre-stages) [:l2 :authority-receipt-id]
                        "sha256:forged")
              (collections-by-owner))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error)))))))
  (testing "fuel-source roots are a closed five-key map"
    (is (= :fuel-source-roots-shape
           (try
             (materialize/build-fuel-subjects
              (assoc fuel-source-roots :host-path "/tmp/escape")
              (pre-stages) (collections-by-owner))
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error))))))))

(defn- owner-transaction []
  (let [top-form-id "sha256:top-form"
        top-syntax-id "sha256:top-syntax"
        fn-form-id "sha256:fn-form"
        fn-syntax-id "sha256:fn-syntax"
        body-form-id "sha256:body-form"
        body-syntax-id "sha256:body-syntax"
        def-node-id "sha256:def-node"
        fn-node-id "sha256:fn-node"
        body-node-id "sha256:body-node"
        binding-id "sha256:binding"
        q {:lineage {:sh06-semantic-projection-id "sha256:source"}
           :top-level-form-ids [top-form-id]
           :forms [{:form-id top-form-id :syntax-id top-syntax-id
                    :parent-form-id nil :child-form-ids [fn-form-id]}
                   {:form-id fn-form-id :syntax-id fn-syntax-id
                    :parent-form-id top-form-id
                    :child-form-ids [body-form-id]}
                   {:form-id body-form-id :syntax-id body-syntax-id
                    :parent-form-id fn-form-id :child-form-ids []
                    :declared-name 'target}]
           :macro-origin-traces
           [{:macro 'defn :output-def-syntax-id top-syntax-id
             :introduced-fn-syntax-id fn-syntax-id}]}
        rc {:nodes [{:node-id def-node-id :children [fn-node-id]}
                    {:node-id fn-node-id :children [body-node-id]}
                    {:node-id body-node-id :children []}]
            :definitions [{:name 'target :binding-id binding-id
                           :core-node-id def-node-id
                           :value-node-id fn-node-id}]
            :function-records
            [{:function-core-node-id fn-node-id
              :function-form-id fn-form-id
              :function-syntax-id fn-syntax-id
              :body-core-node-id body-node-id
              :fixed-arity 1 :definition-kind :named-top-level
              :definition-binding-id binding-id
              :definition-core-node-id def-node-id
              :definition-name 'target}]}]
    {:module-ordinal 0 :authenticated-core-request q
     :resolved-core-carrier rc}))

(deftest authentic-owner-selection-follows-the-top-level-defn-root
  (let [transaction (owner-transaction)
        registry (materialize/build-owner-registry [transaction])
        root-specs [{:root-ordinal 1 :function 'target :arity 1
                     :module-ordinal 0}]
        plan-id "sha256:plan"
        bundle-core
        {:module-plans [{:module-ordinal 0 :plan-id plan-id}]
         :public-roots [{:root-ordinal 1 :function 'target :arity 1
                         :plan-id plan-id}]}
        selection
        (materialize/build-owner-selection root-specs bundle-core registry)]
    (is (= 1 (count registry)))
    (is (= 0 (:top-level-form-ordinal (first registry))))
    (is (= "sha256:def-node" (:defn-id (first registry))))
    (is (= "sha256:body-node" (:declared-body-root-id (first registry))))
    (is (= (:registry-id (first registry))
           (get-in selection [:selections 0 :registry-id])))
    (is (= [] (:rejected selection)))
    (testing "a duplicate authentic owner is ambiguity, never scan-first"
      (is (= :owner-root-candidate-count
             (try
               (materialize/build-owner-selection
                root-specs bundle-core (conj registry (first registry)))
               nil
               (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error)))))))
    (testing "an untraced top-level root is rejected, not filtered"
      (let [forged
            (-> transaction
                (update-in [:authenticated-core-request :top-level-form-ids]
                           conj "sha256:extra-top")
                (update-in [:authenticated-core-request :forms]
                           conj {:form-id "sha256:extra-top"
                                 :syntax-id "sha256:extra-syntax"
                                 :parent-form-id nil :child-form-ids []}))]
        (is (= :owner-top-level-trace-count
               (try
                 (materialize/build-owner-registry [forged])
                 nil
                 (catch clojure.lang.ExceptionInfo error
                 (:reason (ex-data error))))))))))

(defn- row-predicate-specs
  [row-ordinal]
  (filterv #(= row-ordinal (:row-ordinal %)) evidence/predicate-specs))

(defn- expected-predicate-triples
  [row]
  (vec
   (for [phase evidence/phases
         obligation (:obligations row)
         :let [bindings
               (filterv #(and (= phase (:phase %))
                              (some #{obligation} (:obligations %)))
                        (:binding-specs row))]
         :when (seq bindings)]
     [phase obligation (mapv :path bindings)])))

(defn- assert-row-contract!
  [row-ordinal]
  (let [row (nth evidence/row-plan row-ordinal)
        specs (row-predicate-specs row-ordinal)]
    (is (= #{:ordinal :id :document-ordinals :sections :disposition
             :obligations :binding-specs :test-ids :diagnostic}
           (set (keys row))))
    (is (= row-ordinal (:ordinal row)))
    (is (= (expected-predicate-triples row)
           (mapv (juxt :phase :obligation :required-subject-paths) specs)))
    (is (every?
         (fn [spec]
           (= (:predicate-id spec)
              (codec/H
               [:gravity/sh07-b51-contract-predicate-id-v1
                {:row-plan-id evidence/row-plan-id
                 :row-ordinal row-ordinal
                 :phase (:phase spec)
                 :obligation (:obligation spec)
                 :program (:program spec)}])))
         specs))))

(defn- first-pre-subject
  []
  (let [row (first evidence/row-plan)
        binding (first (:binding-specs row))
        value {:ordinal 0 :path "AGENTS.md"}
        without-id (assoc binding :row-ordinal 0 :value value)]
    (assoc without-id :value-id
           (codec/H [:gravity/sh07-b51-contract-binding-v2 without-id]))))

(deftest contract-agents
  (assert-row-contract! 0)
  (let [spec (first (filter #(and (= 0 (:row-ordinal %))
                                  (= :pre-evidence (:phase %))
                                  (= :normative-priority (:obligation %)))
                            evidence/predicate-specs))
        subject (first-pre-subject)
        env {:context {:row-ordinal 0 :row-id :agents
                       :phase :pre-evidence
                       :obligation :normative-priority}
             :subjects [subject]
             :carriers
             {:document-read-receipt
              {:documents [{:ordinal 0 :path "AGENTS.md"}]}}}]
    (is (true? (evidence/evaluate-contract-predicate! spec env)))
    (is (false?
         (evidence/evaluate-contract-predicate!
          spec (assoc-in env [:subjects 0 :value-id]
                         (codec/H [:gravity/test :wrong])))))
    (is (= :predicate-spec-or-env
           (try
             (evidence/evaluate-contract-predicate!
              (assoc spec :program [:literal true]) env)
             nil
             (catch clojure.lang.ExceptionInfo error
               (:reason (ex-data error))))))))

(deftest contract-foundation (assert-row-contract! 1))
(deftest contract-l1 (assert-row-contract! 2))
(deftest contract-l2 (assert-row-contract! 3))
(deftest contract-c2 (assert-row-contract! 4))
(deftest contract-c6 (assert-row-contract! 5))
(deftest contract-c11-nonclaim (assert-row-contract! 6))
(deftest contract-boot1 (assert-row-contract! 7))
(deftest contract-boot2 (assert-row-contract! 8))
(deftest contract-boot3 (assert-row-contract! 9))
(deftest contract-boot4 (assert-row-contract! 10))
(deftest contract-boot5 (assert-row-contract! 11))
(deftest contract-boot6-8 (assert-row-contract! 12))
(deftest contract-test10 (assert-row-contract! 13))
(deftest contract-test11 (assert-row-contract! 14))
(deftest contract-test13 (assert-row-contract! 15))
(deftest proof-contract-scope (assert-row-contract! 16))
(deftest backlog-ownership (assert-row-contract! 17))
(deftest catalog-governance (assert-row-contract! 18))

(def ^:private fixture-resource-root
  "gravity/self_hosting/fixtures/sh07-b51-git-object-v9/")

(defn- read-one-canonical-fixture
  [name]
  (let [resource (io/resource (str fixture-resource-root name))]
    (when-not resource
      (throw (ex-info "missing V9 conformance fixture" {:name name})))
    (let [bytes (with-open [input (io/input-stream resource)]
                  (.readAllBytes input))
          text (String. ^bytes bytes StandardCharsets/UTF_8)
          reader (PushbackReader. (StringReader. text))
          value (binding [*read-eval* false *data-readers* {}]
                  (edn/read {:eof ::eof} reader))
          trailing (binding [*read-eval* false *data-readers* {}]
                     (edn/read {:eof ::eof} reader))]
      (when-not (= ::eof trailing)
        (throw (ex-info "fixture contains trailing forms" {:name name})))
      {:bytes bytes :value value})))

(defn- git-id? [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- source-event-plan
  []
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal) :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal) :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal) :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal) :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal) :domain :compiler-artifact-binding
       :source-ordinal 11 :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding})
    [{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
      :module-ordinal nil
      :consumer
      'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}])))

(defn- accepted-fixture-valid?
  [fixture]
  (let [descriptors (:source-descriptors fixture)
        events (:source-events fixture)
        descriptor-by-ordinal (into {} (map (juxt :ordinal identity)
                                             descriptors))]
    (and
     (= #{:schema :row-plan-id :source-descriptors :source-events
          :predicate-specs :predicate-spec-id :self-id-omits :fixture-id}
        (set (keys fixture)))
     (= :gravity/sh07-b51-contract-predicate-fixture-v1 (:schema fixture))
     (= evidence/row-plan-id (:row-plan-id fixture))
     (= 13 (count descriptors))
     (= (vec (range 13)) (mapv :ordinal descriptors))
     (every?
      (fn [[spec descriptor]]
        (let [[ordinal kind module-ordinal path] spec]
          (and
           (= #{:ordinal :kind :module-ordinal :path :mode :blob-oid
                :byte-count :raw-sha256}
              (set (keys descriptor)))
           (= [ordinal kind module-ordinal path "100644"]
              ((juxt :ordinal :kind :module-ordinal :path :mode) descriptor))
           (git-id? (:blob-oid descriptor))
           (integer? (:byte-count descriptor))
           (not (neg? (:byte-count descriptor)))
           (sha256-id? (:raw-sha256 descriptor)))))
      (map vector evidence/source-specs descriptors))
     (= 40 (count events))
     (= (vec (range 40)) (mapv :ordinal events))
     (every?
      true?
      (map
       (fn [expected event]
         (and
          (= #{:ordinal :domain :source-ordinal :module-ordinal :consumer
               :source-raw-sha256 :product-id}
             (set (keys event)))
          (= expected
             (select-keys event
                          [:ordinal :domain :source-ordinal :module-ordinal
                           :consumer]))
          (= (:raw-sha256
              (get descriptor-by-ordinal (:source-ordinal event)))
             (:source-raw-sha256 event))
          (sha256-id? (:product-id event))))
       (source-event-plan) events))
     (= evidence/predicate-specs (:predicate-specs fixture))
     (= evidence/predicate-spec-id (:predicate-spec-id fixture))
     (= [[:fixture-id]] (:self-id-omits fixture))
     (= (:fixture-id fixture)
        (codec/H [:gravity/sh07-b51-contract-predicate-fixture-v1
                  (dissoc fixture :fixture-id)])))))

(deftest canonical-source-closure-and-predicate-fixture-is-literal
  (let [{:keys [bytes value]} (read-one-canonical-fixture "accepted.edn")]
    (is (Arrays/equals ^bytes bytes ^bytes (codec/W value)))
    (is (accepted-fixture-valid? value))))

(def ^:private required-rejected-case-ids
  #{:source-omitted :source-extra :source-reordered
    :event-omitted :event-extra :event-reordered
    :predicate-omitted :predicate-extra :predicate-reordered
    :predicate-id-changed :fixture-id-changed
    :direct-read-escape :preflight-after-project-load
    :static-load-before-preflight :malformed-utf8 :wire-divergence
    :pid-only-normalization :host-only-normalization :empty-target-race
    :checker-a-before-b-fail-fast
    :a9-schema-legacy :a9-stage-changed :a9-result-generic
    :a9-descriptor-omitted :a9-descriptor-reordered
    :a9-terminal-coordinate-extra :a9-terminal-manifest-mutated
    :a9-check-false :a9-check-omitted :a9-historical-claim-true
    :a9-historical-claim-omitted :a9-generic-governance-result
    :a9-ownership-result-added
    :pre-mask-legacy-key :pre-mask-key-omitted
    :pre-mask-postimage-smuggled :pre-mask-runtime-carrier-smuggled
    :runtime-mask-pre-name :runtime-mask-missing-postimage
    :runtime-mask-equality-false :eligibility-counter-legacy-mask-name
    :ownership-schema-legacy :ownership-a9-reservation
    :ownership-a9-handoff :ownership-e9-handoff
    :ownership-a9-not-read-only :ownership-allocation-reordered
    :ownership-plan-id-changed
    :governance-a9-cleanup :governance-e9-cleanup
    :governance-cleanup-reordered :governance-order-mutated
    :governance-result-omitted :governance-result-extra-e9
    :tag-registry-old-a9 :tag-registry-old-ownership
    :tag-registry-missing-stage-result :h-domain-a9-stage-result
    :h-domain-old-a9 :h-domain-old-ownership
    :pre-phase-old-a9-key :pre-phase-old-mask-key
    :runtime-phase-old-mask-key :phase-order-mutated
    :phase-prefix-length-mutated :phase-prior-ref-container-mutated
    :phase-root-domain-mutated
    :lifecycle-old-milestone :lifecycle-a9-bit-false
    :lifecycle-later-integration-false :lifecycle-cleanup-false
    :lifecycle-extra-a9-cleanup
    :external-review-schema-wrong :external-review-key-omitted
    :external-review-key-extra :external-review-parent-mutated
    :external-review-path-mutated :external-review-mode-mutated
    :external-review-type-mutated})

(deftest rejected-fixture-is-canonical-closed-and-covers-e1
  (let [{:keys [bytes value]} (read-one-canonical-fixture "rejected.edn")
        cases (:cases value)
        ids (mapv :id cases)]
    (is (Arrays/equals ^bytes bytes ^bytes (codec/W value)))
    (is (= #{:accepted-schema :cases}
           (set (keys value))))
    (is (= :gravity/sh07-b51-contract-predicate-fixture-v1
           (:accepted-schema value)))
    (is (= (count ids) (count (distinct ids))))
    (is (= required-rejected-case-ids (set ids)))
    (is (every?
         #(= #{:id :family :target :operation :value :expected-reason}
             (set (keys %)))
         cases))
    (is (= #{:source-closure :contract-predicate :process-source-closure
             :codec :process :publication :a9-terminal :masked-source
             :ownership :governance :registry :phase :lifecycle
             :external-review-coordinate}
           (set (map :family cases))))))

(defn- evidence-private
  [symbol]
  (let [resolved (ns-resolve 'gravity.tools.sh07-b51-git-evidence symbol)]
    (when-not resolved
      (throw (ex-info "missing V9 E2 evidence seam" {:symbol symbol})))
    (var-get resolved)))

(defn- file-bytes
  [path]
  (Files/readAllBytes
   (Paths/get path (make-array String 0))))

(def ^:private a9-fixed-blob-oids
  ["1eef3f25b18b941deed09bdfb178573f08f7928e"
   "fc36dc37d4e45adc7255eb531ae8d312d9eb51da"
   "ddae151f339068955ff9f7df75c975e697962bd6"])

(defn- canonical-a9-stage-result
  []
  (let [paths evidence/a9-only-paths-e1
        bytes-by-path (into {} (map (fn [path] [path (file-bytes path)]) paths))
        rows-by-path
        (into {} (map (fn [path oid] [path {:oid oid}])
                      paths a9-fixed-blob-oids))
        decoded-static
        {evidence/a9-terminal-path
         (String. ^bytes (get bytes-by-path evidence/a9-terminal-path)
                  StandardCharsets/UTF_8)
         "contracts/workstream-ledger.json"
         (String. ^bytes (file-bytes "contracts/workstream-ledger.json")
                  StandardCharsets/UTF_8)}]
    ((evidence-private 'build-a9-stage-result-e1)
     {:commit (apply str (repeat 40 "1"))
      :tree (apply str (repeat 40 "2"))}
     rows-by-path bytes-by-path decoded-static)))

(def ^:private a9-check-keys
  #{:canonical-json :record-shape-valid :filename-sha256-match
    :manifest-entry-match :workstream-id-match :invariant-family-match
    :architecture-decision-path-match :owned-paths-match
    :accepted-fixtures-equal-owned-paths :base-commit-id-syntax-valid
    :candidate-commit-id-syntax-valid :candidate-differs-from-base
    :clean-worktree-recorded :independent-accepted-review-present
    :history-transitions-valid :history-final-state-match
    :record-state-integrated :authority-ceiling-match :all-checks-pass})

(def ^:private a9-historical-claim-keys
  #{:candidate-bound-review :candidate-tree :integration-commit
    :integration-tree :candidate-integration-ancestry
    :report-preservation-across-integration
    :supporting-report-preservation-across-integration
    :ownership-reservation :ownership-handoff :reservation-release
    :worktree-removal :branch-removal :finally-entered})

(defn- valid-a9-stage-result?
  [value]
  (and
   (= #{:schema :stage :content :a9-artifact-descriptors :terminal-record
        :checks :result :historical-claims :nonclaims}
      (set (keys value)))
   (= :gravity/sh07-b51-a9-stage-result-e1 (:schema value))
   (= :a9 (:stage value))
   (= #{:commit :tree} (set (keys (:content value))))
   (= 3 (count (:a9-artifact-descriptors value)))
   (= evidence/a9-only-paths-e1
      (mapv :path (:a9-artifact-descriptors value)))
   (= #{:coordinate :manifest-entry :record}
      (set (keys (:terminal-record value))))
   (= #{:path :mode :type :blob-oid :byte-count :raw-sha256 :commit :tree}
      (set (keys (get-in value [:terminal-record :coordinate]))))
   (= #{"dependencies" "id" "invariant_family" "ordinal" "path"
        "sha256" "state" "terminal"}
      (set (keys (get-in value [:terminal-record :manifest-entry]))))
   (= a9-check-keys (set (keys (:checks value))))
   (every? true? (vals (:checks value)))
   (= :terminal-record-integrated (:result value))
   (= a9-historical-claim-keys (set (keys (:historical-claims value))))
   (every? false? (vals (:historical-claims value)))
   (= evidence/nonclaims (:nonclaims value))))

(deftest e1-terminal-a9-is-derived-and-closed-under-mutation
  (let [a9 (canonical-a9-stage-result)]
    (is (valid-a9-stage-result? a9))
    (doseq [mutated [(assoc a9 :schema
                            :gravity/sh07-b51-a9-lifecycle-receipt-v1)
                     (assoc a9 :stage :i9)
                     (assoc a9 :result :integrated)
                     (update a9 :a9-artifact-descriptors pop)
                     (assoc a9 :a9-lifecycle {})
                     (assoc-in a9 [:terminal-record :coordinate
                                   :candidate-tree]
                               (apply str (repeat 40 "0")))]]
      (is (false? (valid-a9-stage-result? mutated))))
    (doseq [key a9-check-keys]
      (is (false? (valid-a9-stage-result?
                   (assoc-in a9 [:checks key] false))))
      (is (false? (valid-a9-stage-result?
                   (update a9 :checks dissoc key)))))
    (doseq [key a9-historical-claim-keys]
      (is (false? (valid-a9-stage-result?
                   (assoc-in a9 [:historical-claims key] true))))
      (is (false? (valid-a9-stage-result?
                   (update a9 :historical-claims dissoc key)))))))

(def ^:private pre-mask-source-paths
  ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"])

(defn- canonical-pre-masked-root
  []
  (let [bytes-by-path
        (into {} (map (fn [path] [path (file-bytes path)])
                      pre-mask-source-paths))
        decoded-static
        (into {} (map (fn [[path bytes]]
                       [path (String. ^bytes bytes StandardCharsets/UTF_8)])
                     bytes-by-path))]
    ((evidence-private 'build-pre-masked-root)
     bytes-by-path decoded-static)))

(defn- valid-pre-masked-root?
  [value]
  (and (= #{:coordinates :preimage-spans :preimage-files :preimage-root}
          (set (keys value)))
       (= 5 (count (:coordinates value)) (count (:preimage-spans value)))
       (= 2 (count (:preimage-files value)))
       (sha256-id? (:preimage-root value))))

(deftest e1-pre-mask-is-acyclic-and-runtime-mask-remains-distinct
  (let [pre-mask (canonical-pre-masked-root)
        runtime-mask
        (assoc pre-mask
               :postimage-spans (:preimage-spans pre-mask)
               :postimage-files (:preimage-files pre-mask)
               :postimage-root (:preimage-root pre-mask)
               :equal true)]
    (is (valid-pre-masked-root? pre-mask))
    (is (= #{:coordinates :preimage-spans :postimage-spans
             :preimage-files :postimage-files :preimage-root
             :postimage-root :equal}
           (set (keys runtime-mask))))
    (doseq [key [:coordinates :preimage-spans :preimage-files :preimage-root]]
      (is (false? (valid-pre-masked-root? (dissoc pre-mask key)))))
    (doseq [future-key [:postimage-spans :postimage-files :postimage-root
                        :equal :runtime-contract-discharge :e9-coordinate]]
      (is (false? (valid-pre-masked-root?
                   (assoc pre-mask future-key true)))))))

(defn- canonical-ownership-plan
  []
  ((evidence-private 'build-v9-ownership-plan-e2)))

(deftest e2-ownership-and-governance-are-stage-scoped
  (let [plan (canonical-ownership-plan)
        rules evidence/governance-rules-e1]
    (is (= #{:schema :allocations :semantic-review-owner
             :reservation-required-stages :handoff-required-stages
             :read-only-prerequisite-stages :read-only-paths
             :open-exit-gates :aggregate-sh07-state :self-id-omits :plan-id}
           (set (keys plan))))
    (is (= :gravity/sh07-b51-v9-ownership-plan-e2-v1 (:schema plan)))
    (is (= [:a9 :i9 :e9 :g9 :p9] (mapv :stage (:allocations plan))))
    (is (= [:i9 :e9 :g9 :p9] (:reservation-required-stages plan)))
    (is (= [:i9 :g9 :p9] (:handoff-required-stages plan)))
    (is (= [:a9] (:read-only-prerequisite-stages plan)))
    (is (= (:plan-id plan)
           (codec/H [:gravity/sh07-b51-v9-ownership-plan-e2-v1
                     (dissoc plan :plan-id)])))
    (doseq [mutated [(assoc plan :schema
                            :gravity/sh07-b51-v9-ownership-plan-v1)
                     (update plan :reservation-required-stages
                             #(into [:a9] %))
                     (update plan :handoff-required-stages
                             #(into [:a9] %))
                     (update plan :handoff-required-stages conj :e9)
                     (assoc plan :read-only-prerequisite-stages [])
                     (update plan :allocations
                             #(assoc % 0 (nth % 1) 1 (nth % 0)))]]
      (is (not= plan mutated)))
    (is (= #{:permitted-transitions :admission-requirements
             :external-discharge-order :final-cleanup-required-stages
             :aggregate-sh07-state}
           (set (keys rules))))
    (is (= [:i9 :g9 :p9] (:final-cleanup-required-stages rules)))
    (doseq [mutated [(update rules :final-cleanup-required-stages
                             #(into [:a9] %))
                     (update rules :final-cleanup-required-stages conj :e9)
                     (update rules :final-cleanup-required-stages reverse)
                     (update rules :external-discharge-order
                             #(assoc % 0 (nth % 1) 1 (nth % 0)))]]
      (is (not= rules mutated)))))

(deftest e2-registries-remove-old-ownership-and-split-tag-from-h-domain
  (let [report (String. ^bytes (file-bytes evidence/architecture-report-path)
                        StandardCharsets/UTF_8)
        registries ((evidence-private 'build-registries-e2) report)
        tags (set (:tag-registry registries))
        domains (set (get-in registries [:identity-domain-registry :domains]))]
    (is (contains? tags :gravity/sh07-b51-a9-stage-result-e1))
    (is (contains? tags :gravity/sh07-b51-v9-ownership-plan-e2-v1))
    (is (not (contains? tags :gravity/sh07-b51-v9-ownership-plan-e1)))
    (is (not (contains? tags
                        :gravity/sh07-b51-a9-lifecycle-receipt-v1)))
    (is (not (contains? tags :gravity/sh07-b51-v9-ownership-plan-v1)))
    (is (contains? domains :gravity/sh07-b51-v9-ownership-plan-e2-v1))
    (is (not (contains? domains :gravity/sh07-b51-v9-ownership-plan-e1)))
    (is (not (contains? domains :gravity/sh07-b51-a9-stage-result-e1)))
    (is (not (contains? domains
                        :gravity/sh07-b51-a9-lifecycle-receipt-v1)))
    (is (not (contains? domains :gravity/sh07-b51-v9-ownership-plan-v1)))
    (is (not= tags (conj tags
                         :gravity/sh07-b51-a9-lifecycle-receipt-v1)))
    (is (not= domains (conj domains
                             :gravity/sh07-b51-a9-stage-result-e1)))))

(defn- governance-result-projection
  [stage]
  {:schema :gravity/sh07-b51-governance-result-v1 :stage stage})

(defn- valid-final-lifecycle-e1?
  [value]
  (let [results (:governance-results value)]
    (and (= #{:governance-results :a9-terminal-record-integrated
              :i9-g9-p9-integrated :final-cleanup}
            (set (keys value)))
         (= 4 (count results))
         (= :gravity/sh07-b51-a9-stage-result-e1
            (:schema (first results)))
         (= [:i9 :g9 :p9] (mapv :stage (subvec results 1)))
         (every? #(= :gravity/sh07-b51-governance-result-v1 (:schema %))
                 (subvec results 1))
         (true? (:a9-terminal-record-integrated value))
         (true? (:i9-g9-p9-integrated value))
         (true? (:final-cleanup value)))))

(deftest e1-phase-and-final-lifecycle-reject-old-names-and-stage-slippage
  (let [pre-carrier-keys (evidence-private 'pre-carrier-keys)
        eligibility-receipt-keys (evidence-private 'eligibility-receipt-keys)
        a9 (canonical-a9-stage-result)
        results (into [a9] (map governance-result-projection [:i9 :g9 :p9]))
        milestones
        [:a9-terminal-record-integrated :c-content-frozen
         :q-evidence-eligible :fresh-runtime-complete :e9-evidence-immutable
         :i9-integrated :g9-integrated :p9-integrated
         :final-cleanup-complete]
        attained-prefixes
        (mapv #(subvec milestones 0 %) [1 4 6 7 9])
        lifecycle {:governance-results results
                   :a9-terminal-record-integrated true
                   :i9-g9-p9-integrated true
                   :final-cleanup true}]
    (is (= [:pre-evidence :runtime :post-i9 :post-g9 :post-p9]
           evidence/phases))
    (is (= [1 4 6 7 9] (mapv count attained-prefixes)))
    (is (= [:a9-terminal-record-integrated] (first attained-prefixes)))
    (is (contains? pre-carrier-keys :a9-stage-result))
    (is (contains? pre-carrier-keys :pre-masked-root))
    (is (not (contains? pre-carrier-keys :a9-lifecycle)))
    (is (not (contains? pre-carrier-keys :masked-root)))
    (is (contains? eligibility-receipt-keys :pre-masked-root))
    (is (not (contains? eligibility-receipt-keys :masked-root)))
    (is (valid-final-lifecycle-e1? lifecycle))
    (doseq [mutated [(assoc lifecycle :a9-terminal-record-integrated false)
                     (assoc lifecycle :i9-g9-p9-integrated false)
                     (assoc lifecycle :final-cleanup false)
                     (assoc lifecycle :a9-cleanup true)
                     (update lifecycle :governance-results pop)
                     (update lifecycle :governance-results
                             conj (governance-result-projection :e9))
                     (assoc-in lifecycle [:governance-results 0 :schema]
                               :gravity/sh07-b51-governance-result-v1)]]
      (is (false? (valid-final-lifecycle-e1? mutated))))))
