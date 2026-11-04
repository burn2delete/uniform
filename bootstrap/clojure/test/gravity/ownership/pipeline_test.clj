(ns gravity.ownership.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [gravity.bootstrap :as b]
            [gravity.c9-ownership-checker :as c9]
            [gravity.c9-ownership-checker.artifact :as artifact]
            [gravity.c9-ownership-checker.catalog :as catalog]
            [gravity.c9-ownership-checker.graphs :as graphs]
            [gravity.c9-ownership-checker.lifecycles :as lifecycles]
            [gravity.c9-ownership-checker.resources :as resources]
            [gravity.c9-ownership-checker.verification :as verification]
            [gravity.ownership :as ownership]
            [gravity.self-hosting.sh10-ownership-transition-fixture-support
             :as fixture-support]))

(defn- node [id operation operands extra]
  {:node-id id :source-operation operation :operands operands
   :source {:origin-id (str id ":origin") :span {:source "ownership-data.gravity" :start 0 :end 1}}
   :ownership (ownership/persistent-ownership :value (assoc extra :storage :persistent-value))})

(deftest contract-states-the-exact-data-boundary
  (is (= #{'clojure.set} (set (map ns-name (vals (ns-aliases 'gravity.ownership))))))
  (is (= #{'persistent-ownership 'forwarded-value-node-ids 'propagate-managed-ownership
           'shared-owner-domain-id 'canonical-owner-id 'persistent-aliasing-valid?
           'persistent-ownership-expected-keys 'persistent-ownership-schema-valid?
           'expected-managed-node-ids 'native-ownership-graph 'native-borrow-graph
           'native-lifetime-interval-map 'native-escape-analysis-report
           'native-region-lifetime-graph 'native-arena-generation-graph
           'native-linear-resource-flow-graph 'native-transfer-records
           'native-runtime-check-records 'native-unsafe-audit-references
           'native-ownership-diagnostics 'native-linear-paths-exact?
           'native-verifier-report 'native-ownership-capability-proof 'native-validate!
           'native-diagnostic-ids 'native-governing-document 'native-rejected-designs
           'native-override-diagnostics 'native-ownership-message 'native-artifact-projection}
         (set (keys (ns-publics 'gravity.ownership)))))
  (doseq [[name operation] (ns-publics 'gravity.ownership)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.ownership (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:general-c9-analysis} (:unsupported contract))))))

(deftest native-catalog-values-retain-stable-diagnostics-paths-and-snapshots
  (let [ids ownership/native-diagnostic-ids
        document ownership/native-governing-document
        designs ownership/native-rejected-designs
        overrides ownership/native-override-diagnostics]
    (is (= ["C9-USE-AFTER-MOVE" "C9-USE-AFTER-CONSUME" "C9-BORROW-ESCAPE"
            "C9-MUT-ALIAS" "C9-MOVE-WHILE-BORROWED" "C9-REGION-ESCAPE"
            "C9-ARENA-GENERATION" "C9-LINEAR-LEAK" "C9-LINEAR-DOUBLE"
            "C9-TRANSFER" "C9-RUNTIME-CHECK" "C9-UNSAFE"] ids))
    (is (= ids (mapv :diagnostic designs)))
    (is (= (set ids) (set (vals overrides))))
    (is (.isFile (io/file document)))
    (doseq [design designs]
      (is (.isFile (io/file (:fixture design)))))
    (doseq [[stage helper facade]
            [[ids catalog/diagnostic-ids c9/c9-ownership-diagnostic-ids]
             [document catalog/governing-document c9/c9-ownership-governing-document]
             [designs catalog/rejected-designs c9/c9-ownership-rejected-designs]
             [overrides catalog/override-diagnostics c9/c9-ownership-override-diagnostics]]]
      (is (identical? stage helper))
      (is (identical? stage facade)))
    (with-redefs [ownership/native-diagnostic-ids ["stage-rebound"]
                  ownership/native-governing-document "stage-rebound"
                  ownership/native-rejected-designs []
                  ownership/native-override-diagnostics {}]
      ;; Compatibility aliases keep their original initialization snapshots.
      (is (identical? ids catalog/diagnostic-ids))
      (is (identical? ids c9/c9-ownership-diagnostic-ids))
      (is (identical? document catalog/governing-document))
      (is (identical? designs catalog/rejected-designs))
      (is (identical? overrides catalog/override-diagnostics))))
  (doseq [name '[native-diagnostic-ids native-governing-document
                native-rejected-designs native-override-diagnostics]]
    (let [contract (:gravity/namespace-contract (meta (ns-resolve 'gravity.ownership name)))]
      (is (= :native-catalog-data (get-in contract [:public-api name :kind])))
      (is (true? (:native-catalog-status-is-data? contract))))))

(deftest native-messages-and-original-facade-interposition-remain-exact
  (doseq [message [ownership/native-ownership-message catalog/ownership-message c9/c9-ownership-message]]
    (is (= "owned value is used after move" (message "C9-USE-AFTER-MOVE")))
    (is (= "linear resource may reach multiple terminal operations" (message "C9-LINEAR-DOUBLE")))
    (is (= "required dynamic ownership check is unavailable in the active profile"
           (message "C9-RUNTIME-CHECK")))
    (doseq [id [nil false :C9-USE-AFTER-MOVE "unknown" {} []]]
      (is (= "Ownership checking failed" (message id))))
    (doseq [id ownership/native-diagnostic-ids]
      (is (not= "Ownership checking failed" (message id)))))
  (let [calls (atom [])]
    (is (= :interposed
           (c9/with-operations {:c9-ownership-message (fn [id] (swap! calls conj id) :interposed)}
             #(c9/c9-ownership-message :unknown))))
    (is (= [:unknown] @calls)))
  (with-redefs [catalog/ownership-message (constantly :helper-rebound)]
    (is (= :helper-rebound (c9/c9-ownership-message "C9-UNSAFE"))))
  (let [ids ["provided-native-id"] designs [{:diagnostic "provided-native-id" :fixture "provided"}]
        registry (c9/with-operations
                   {:c9-ownership-diagnostic-ids ids :c9-ownership-rejected-designs designs
                    :source-span (fn [path index] {:source path :form-index index})}
                   #(c9/c9-ownership-diagnostics "provided-native-source" {}))]
    (is (identical? ids (:required-diagnostic-ids registry)))
    (is (= ["provided-native-id"] (mapv :diagnostic (:diagnostics registry))))
    (is (= "C9-USE-AFTER-MOVE" (first catalog/diagnostic-ids)))))

(def ^:private validation-rules
  [[:ownership-graph-complete? "C9-USE-AFTER-MOVE"]
   [:borrow-rules-proven? "C9-MUT-ALIAS"]
   [:lifetime-intervals-recorded? "C9-BORROW-ESCAPE"]
   [:move-and-consume-recorded? "C9-USE-AFTER-CONSUME"]
   [:region-and-arena-recorded? "C9-REGION-ESCAPE"]
   [:linear-flow-complete? "C9-LINEAR-LEAK"]
   [:transfer-boundaries-explicit? "C9-TRANSFER"]
   [:runtime-checks-profile-gated? "C9-RUNTIME-CHECK"]
   [:unsafe-audit-references-recorded? "C9-UNSAFE"]
   [:diagnostics-covered? "C9-UNSAFE"]
   [:verifier-passed? "C9-UNSAFE"]])

(def ^:private native-report-inputs
  ;; Explicit native data only; these labels are not authentic C8 evidence.
  [{:capability-based-proof {:status :complete}}
   {:owners {"value" {}} :status :complete}
   {:edges [{:edge :immutable-borrow} {:edge :mutable-borrow}] :status :complete}
   {:intervals {"interval" {}} :status :complete}
   {:moves [:move] :consumes [:consume]}
   {:status :complete}
   {:regions {"region" {}} :status :complete}
   {:arenas {"arena" {}} :status :complete}
   {:resources {"resource" {:terminal-paths [{:terminal-count 1}]}} :status :complete}
   {:records (mapv #(hash-map :boundary %) [:function :actor :task :ffi])}
   {:records [{:profile-legal? true}]}
   {:records [{:unsafe-island :provided-native-data :review :required}]}
   {:diagnostics [{:diagnostic "native-a"} {:diagnostic "native-z"}]}])

(defn- native-assembly-inputs []
  (into ["native-metadata-only"
         {:module 'native.assembly :profile :hosted :target :jvm
          :metadata {:unsafe-island :provided :ai/human-review :required}} {}]
        (conj native-report-inputs
              (apply ownership/native-verifier-report ownership/native-linear-paths-exact?
                     ["native-z" "native-a"] native-report-inputs))))

(defn- assembly-operations [calls]
  (let [proof (fn [value]
                (swap! calls conj [:proof value])
                (ownership/native-ownership-capability-proof value))]
    {:validate! (fn [path value]
                  (swap! calls conj [:validate path value])
                  (ownership/native-validate!
                   proof
                   (fn [id source stage facts]
                     (swap! calls conj [:refusal id source stage facts])
                     (throw (ex-info "Native assembly validation refusal"
                                     {:id id :source source :stage stage :facts facts})))
                   path value))
     :capability-proof proof
     :artifact-id (fn [value] (swap! calls conj [:identity value]) (b/c4-artifact-id value))
     :governing-document "provided-native-document" :diagnostic-ids ["native-z" "native-a"]}))

(deftest native-assembly-keeps-original-map-order-identities-and-nonauthority-envelope
  (let [calls (atom []) inputs (native-assembly-inputs)
        result (apply ownership/native-artifact-projection (assembly-operations calls) inputs)
        raw (:artifact result) events @calls
        base (nth (first events) 2) enriched (second (last events))
        contract (:gravity/namespace-contract (meta #'ownership/native-artifact-projection))]
    (is (= #{:kind :admission :authenticates-inputs? :artifact} (set (keys result))))
    (is (= :gravity/native-c9-artifact-projection (:kind result)))
    (is (= :none (:admission result)))
    (is (false? (:authenticates-inputs? result)))
    (is (= :gravity/stage0-c9-ownership-checker-artifact (:kind raw)))
    (is (= [:validate :proof :proof :identity] (mapv first events)))
    (is (= "native-metadata-only" (second (first events))))
    (is (identical? base (second (nth events 1))))
    (is (identical? base (second (nth events 2))))
    (is (= (dissoc raw :artifact-id) enriched))
    (is (not (contains? enriched :admission)))
    (is (not (contains? enriched :authenticates-inputs?)))
    (is (= (b/c4-artifact-id enriched) (:artifact-id raw)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id raw)))
    (is (identical? (get-in inputs [1 :metadata]) (get-in raw [:module :metadata])))
    (is (identical? (nth inputs 4) (:ownership-graph raw)))
    (is (identical? (nth inputs 14) (:unsafe-audit-references raw)))
    (is (= #{:validate! :capability-proof :artifact-id :governing-document :diagnostic-ids}
           (:native-assembly-operation-keys contract)))
    (is (true? (:source-path-is-metadata-only? contract)))
    (is (false? (:projection-envelope-enters-legacy-hash? contract)))
    (is (= :unchanged (get-in (c9/c9-engine-contract) [:native-artifact-projection :source-and-file-entrypoints])))))

(deftest native-assembly-refuses-missing-native-fields-before-proof-publication-or-identity
  (doseq [[field id] validation-rules]
    (let [calls (atom []) inputs (update (native-assembly-inputs) 16 assoc
                                       (if (= field :verifier-passed?) :status field)
                                       (if (= field :verifier-passed?) :failed false))
          error (try (apply ownership/native-artifact-projection (assembly-operations calls) inputs)
                     (catch clojure.lang.ExceptionInfo value value))]
      (is (= id (:id (ex-data error))))
      (is (= {:missing-fields [field]} (:facts (ex-data error))))
      (is (= [:validate :proof :refusal] (mapv first @calls))))))

(deftest native-assembly-closed-operation-map-precedes-all-input-and-callback-observation
  (let [calls (atom []) operations (assembly-operations calls)
        inputs (assoc (native-assembly-inputs) 1 (Object.))]
    (doseq [bad (concat (map #(dissoc operations %) (keys operations))
                       (map #(assoc operations % (fn [& _] (throw (ex-info "forbidden operation" {}))))
                            [:read-source-form-records :compiler-c8-effect-source-artifact
                             :c9-ownership-graph :artifact-operations :*operations*]))]
      (let [error (try (apply ownership/native-artifact-projection bad inputs)
                       (catch clojure.lang.ExceptionInfo value value))]
        (is (= :native-c9-assembly-operations (:id (ex-data error))))
        (is (= (set (keys operations)) (:expected-operation-keys (ex-data error))))
        (is (= (set (keys bad)) (:actual-operation-keys (ex-data error))))
        (is (empty? @calls))))))

(deftest native-assembly-keeps-callback-failure-identity-and-duplicate-proof-barriers
  (doseq [failure [:validate [:proof 1] [:proof 2] :identity]]
    (let [calls (atom []) proof-count (atom 0)
          error (ex-info "Original assembly callback failure" {:operation failure})
          event! (fn [event] (swap! calls conj event) (when (= event failure) (throw error)))
          proof (fn [value] (event! [:proof (swap! proof-count inc)])
                  (ownership/native-ownership-capability-proof value))
          operations {:validate! (fn [path value]
                                   (event! :validate)
                                   (ownership/native-validate! proof (fn [& _] (throw error)) path value))
                      :capability-proof proof
                      :artifact-id (fn [value] (event! :identity) (b/c4-artifact-id value))
                      :governing-document "native" :diagnostic-ids ["native-z" "native-a"]}
          observed (try (apply ownership/native-artifact-projection operations (native-assembly-inputs))
                        (catch Throwable value value))]
      (is (identical? error observed))
      (is (= (case failure
               :validate [:validate]
               [:proof 1] [:validate [:proof 1]]
               [:proof 2] [:validate [:proof 1] [:proof 2]]
               :identity [:validate [:proof 1] [:proof 2] :identity]) @calls)))))

(defn- native-assembly-id [value] (b/c4-artifact-id value))

(deftest native-assembly-preserves-captured-functions-and-late-var-roots
  (doseq [capture-var? [false true]]
    (with-redefs [native-assembly-id (constantly "early-native-id")]
      (let [captured (if capture-var? #'native-assembly-id native-assembly-id)
            operations (assoc (assembly-operations (atom []))
                              :artifact-id captured
                              :validate! (fn [& _]
                                           (alter-var-root #'native-assembly-id
                                                           (constantly (constantly "late-native-id")))))
            result (apply ownership/native-artifact-projection operations (native-assembly-inputs))]
        (is (= (if capture-var? "late-native-id" "early-native-id")
               (get-in result [:artifact :artifact-id])))))))

(deftest native-assembly-never-enters-source-or-file-routes-and-preserves-host-failures
  (let [forbidden (fn [& _] (throw (ex-info "Source/file route must remain unreachable" {})))]
    (with-redefs [b/compiler-c9-ownership-source-artifact forbidden
                  b/compiler-c9-ownership-file-artifact forbidden
                  c9/compiler-c9-ownership-source-artifact forbidden
                  c9/compiler-c9-ownership-file-artifact forbidden
                  artifact/source-artifact forbidden artifact/file-artifact forbidden]
      (is (= :none (:admission
                    (apply ownership/native-artifact-projection
                           (assembly-operations (atom [])) (native-assembly-inputs)))))))
  (doseq [index [1 3]]
    (let [error (ex-info "Original native view failure" {:index index})
          opaque (reify clojure.lang.Associative
                   (entryAt [_ _] (throw error)) (containsKey [_ _] (throw error))
                   (assoc [_ _ _] (throw error))
                   (valAt [_ _] (throw error)) (valAt [_ _ _] (throw error))
                   (count [_] 0) (cons [_ _] (throw error)) (empty [_] {})
                   (equiv [_ _] false) (seq [_] nil))
          calls (atom []) operations (assoc (assembly-operations calls) :validate! nil)
          observed (try (apply ownership/native-artifact-projection operations
                               (assoc (native-assembly-inputs) index opaque))
                        (catch Throwable value value))]
      ;; A new callback-type check would replace the original map/view failure.
      (is (identical? error observed))
      (is (empty? @calls)))))

(def ^:private native-reporters
  [ownership/native-verifier-report verification/verifier-report
   (fn [predicate ids & inputs]
     (c9/with-operations {:c9-linear-paths-exact? predicate :c9-ownership-diagnostic-ids ids}
       #(apply c9/c9-ownership-verifier-report inputs)))])

(deftest native-verifier-preserves-projection-flags-and-duplicate-callbacks
  (doseq [report native-reporters]
    (let [calls (atom []) linear (nth native-report-inputs 8)
          predicate (fn [value] (swap! calls conj value) :truthy-native-label)
          result (apply report predicate ["native-z" "native-a" "native-z"] native-report-inputs)]
      (is (= :gravity/c9-ownership-verifier-report (:artifact result)))
      (is (= :passed (:status result)))
      (is (every? true? (vals (dissoc result :artifact :status))))
      (is (= [linear linear] @calls))
      (is (every? #(identical? linear %) @calls)))
    ;; The original status decision checks supplied record contents separately
    ;; from several completion flags. Keep that native compatibility behavior.
    (let [inputs (reduce #(update %1 %2 dissoc :status) native-report-inputs [1 2 3 6 7 8])
          result (apply report (constantly true) ["native-z" "native-a"] inputs)]
      (is (= :passed (:status result)))
      (is (false? (:ownership-graph-complete? result)))
      (is (false? (:borrow-rules-proven? result)))
      (is (false? (:lifetime-intervals-recorded? result)))
      (is (false? (:region-and-arena-recorded? result)))
      (is (false? (:linear-flow-complete? result))))
    (let [calls (atom 0)
          result (apply report (fn [_] (= 1 (swap! calls inc)))
                        ["native-z" "native-a"] native-report-inputs)]
      (is (true? (:linear-flow-complete? result)))
      (is (= :failed (:status result)))
      (is (= 2 @calls)))
    (doseq [throw-at [1 2]]
      (let [error (ex-info "original verifier callback" {:at throw-at}) calls (atom 0)
            observed (try
                       (apply report (fn [_] (when (= throw-at (swap! calls inc)) (throw error)) true)
                              ["native-z" "native-a"] native-report-inputs)
                       (catch Throwable value value))]
        (is (identical? error observed))
        (is (= throw-at @calls))))))

(deftest native-diagnostic-registry-preserves-order-spans-and-failure-barriers
  (doseq [construct [ownership/native-ownership-diagnostics verification/ownership-diagnostics
                    (fn [span ids designs path owners]
                      (c9/with-operations {:source-span span :c9-ownership-diagnostic-ids ids
                                           :c9-ownership-rejected-designs designs}
                        #(c9/c9-ownership-diagnostics path owners)))]]
    (let [calls (atom []) ids ["z" "a"]
          designs [{:diagnostic "z" :fixture :z} {:diagnostic "a" :fixture :a}
                   {:diagnostic "z" :fixture :duplicate}]
          span {:source :provided :origin :provided-native-data}
          span-fn (fn [& args] (swap! calls conj (vec args)) span)
          result (construct span-fn ids designs "native-source" {:owners (array-map "z" {} "a" {})})]
      (is (identical? ids (:required-diagnostic-ids result)))
      (is (= ["z" "a" "z"] (mapv :diagnostic (:diagnostics result))))
      (is (= [:z :a :duplicate] (mapv :fixture (:diagnostics result))))
      (is (= ["z" "z" "z"] (mapv :value-id (:diagnostics result))))
      (is (every? #(identical? span (:source-span %)) (:diagnostics result)))
      (is (= (repeat 3 ["native-source" 0]) @calls))
      (is (= :fixture/value (get-in (construct span-fn ids (vec (take 1 designs)) "native-source" {})
                                           [:diagnostics 0 :value-id])))
      (let [error (ex-info "original diagnostic span callback" {}) count (atom 0)
            observed (try (construct (fn [& _] (when (= 2 (swap! count inc)) (throw error)) span)
                                     ids designs "native-source" {})
                          (catch Throwable value value))]
        (is (identical? error observed))
        (is (= 2 @count))))))

(deftest native-linear-and-proof-projections-preserve-empty-and-unadmitted-data
  (doseq [predicate [ownership/native-linear-paths-exact? verification/linear-paths-exact?
                    c9/c9-linear-paths-exact?]]
    (doseq [linear [nil {} {:resources {}} {:resources {"r" {:terminal-paths []}}}]]
      (is (true? (predicate linear))))
    (doseq [count [0 2 nil false]]
      (is (false? (predicate {:resources {"r" {:terminal-paths [{:terminal-count count}]}}}))))
    (is (true? (predicate {:resources {"r" {:terminal-paths [{:terminal-count 1}]}}}))))
  (doseq [project [ownership/native-ownership-capability-proof verification/ownership-capability-proof
                  c9/c9-ownership-capability-proof]]
    (let [result (project {:ownership-verifier-report {:status :passed :borrow-rules-proven? :supplied}})]
      (is (= :complete (:status result)))
      (is (= :supplied (:borrow-rules-proven? result)))
      (is (nil? (:ownership-graph-complete? result)))
      (is (true? (:verifier-passed? result))))
    (is (= :complete (:status (project nil))))
    (let [error (ex-info "original proof lookup" {})
          artifact (reify clojure.lang.ILookup
                     (valAt [_ _] (throw error)) (valAt [_ _ _] (throw error)))]
      (is (identical? error (try (project artifact) (catch Throwable value value)))))))

(deftest native-validation-preserves-all-rule-order-payloads-and-callback-identity
  (doseq [validate [ownership/native-validate! verification/validate!
                   (fn [proof fail path artifact]
                     (c9/with-operations {:c9-ownership-capability-proof proof :c9-ownership-fail! fail}
                       #(c9/c9-ownership-validate! path artifact)))]]
    (doseq [missing (concat [[]] (mapv vector validation-rules) [validation-rules])]
      (let [calls (atom []) proof-calls (atom []) artifact {:unadmitted true}
            proof (apply dissoc (into {} (map (fn [[field _]] [field :truthy-native-label]) validation-rules))
                         (map first missing))
            result (validate (fn [value] (swap! proof-calls conj value) proof)
                             (fn [& args] (swap! calls conj (vec args)) :ignored)
                             "native-source" artifact)]
        (is (= :complete result))
        (is (= 1 (count @proof-calls)))
        (is (identical? artifact (first @proof-calls)))
        (is (= (mapv (fn [[field id]]
                       [id "native-source" {:stage :ownership-lifetime-region-check}
                        {:missing-fields [field]}]) missing) @calls))))
    (let [error (ex-info "original validation refusal" {}) calls (atom [])
          observed (try (validate (constantly {})
                                  (fn [& args] (swap! calls conj (vec args)) (throw error))
                                  "native-source" {})
                        (catch Throwable value value))]
      (is (identical? error observed))
      (is (= ["C9-USE-AFTER-MOVE"] (mapv first @calls))))))

(def ^:private record-builders
  [[ownership/native-lifetime-interval-map lifecycles/lifetime-interval-map c9/c9-lifetime-interval-map
    :gravity/c9-lifetime-interval-map]
   [ownership/native-escape-analysis-report lifecycles/escape-analysis-report c9/c9-escape-analysis-report
    :gravity/c9-escape-analysis-report]
   [ownership/native-region-lifetime-graph lifecycles/region-lifetime-graph c9/c9-region-lifetime-graph
    :gravity/c9-region-lifetime-graph]
   [ownership/native-arena-generation-graph lifecycles/arena-generation-graph c9/c9-arena-generation-graph
    :gravity/c9-arena-generation-graph]
   [ownership/native-linear-resource-flow-graph resources/linear-resource-flow-graph c9/c9-linear-resource-flow-graph
    :gravity/c9-linear-resource-flow-graph]
   [ownership/native-transfer-records resources/transfer-records c9/c9-transfer-records :gravity/c9-transfer-records]
   [ownership/native-runtime-check-records resources/runtime-check-records c9/c9-runtime-check-records
    :gravity/c9-runtime-check-records]
   [ownership/native-unsafe-audit-references resources/unsafe-audit-references c9/c9-unsafe-audit-references
    :gravity/c9-unsafe-audit-references]])

(deftest native-records-preserve-module-lookup-order-and-original-exceptions
  (doseq [[direct helper facade artifact] record-builders construct [direct helper facade]]
    (let [lookups (atom [])
          module (reify clojure.lang.ILookup
                   (valAt [_ key]
                     (swap! lookups conj key) (case key :profile :native :module 'native.module nil))
                   (valAt [_ key fallback]
                     (swap! lookups conj key) (case key :profile :native :module 'native.module fallback)))
          result (construct module)]
      (is (= artifact (:artifact result)))
      (is (= 'native.module (:module result)))
      (is (= (if (= artifact :gravity/c9-runtime-check-records) [:profile :module] [:module]) @lookups)))
    (let [error (ex-info "original record lookup failure" {:artifact artifact})
          module (reify clojure.lang.ILookup
                   (valAt [_ _] (throw error)) (valAt [_ _ _] (throw error)))
          observed (try (construct module) (catch Throwable value value))]
      (is (identical? error observed)))))

(deftest native-record-links-profile-flags-and-review-fields-remain-data
  (let [module {:module 'native.module :profile :hosted}
        lifetimes (ownership/native-lifetime-interval-map module)
        region (ownership/native-region-lifetime-graph module)
        arena (ownership/native-arena-generation-graph module)
        linear (ownership/native-linear-resource-flow-graph module)
        contract (:gravity/namespace-contract (meta #'ownership/native-runtime-check-records))]
    (is (= 9 (count (:intervals lifetimes))))
    (is (= #{:copy :serialize} (get-in lifetimes [:intervals "lt-region-outer" :allowed-escapes])))
    (is (= [:arena-reset] (get-in lifetimes [:intervals "lt-arena-generation-1" :invalidates])))
    (is (= "region-outer" (get-in region [:regions "region-inner" :parent])))
    (is (= [false true] (mapv :valid? (get-in arena [:arenas "arena-main" :generations]))))
    (is (= #{1} (set (map :terminal-count (mapcat :terminal-paths (vals (:resources linear)))))))
    (is (= #{:normal :error :panic :cancellation}
           (set (map :path (get-in linear [:resources "resource-file" :terminal-paths])))))
    (is (= #{:function :actor :task :ffi}
           (set (map :boundary (:records (ownership/native-transfer-records module))))))
    (is (= [:manual-lifetime-extension :manual-resource-flow]
           (mapv :unsafe-island (:records (ownership/native-unsafe-audit-references module)))))
    (is (= [:required :required]
           (mapv :review (:records (ownership/native-unsafe-audit-references module)))))
    (is (true? (:runtime-check-profile-flags-are-data? contract)))
    (is (false? (:performs-runtime-checks? contract)))
    (is (= [] (:invalidated-verified-ir-facts contract))))
  (doseq [profile [:hosted :native :distributed :ai :core :firmware :kernel :hardware :gpu nil :unknown]]
    (let [result (ownership/native-runtime-check-records {:profile profile})
          expected (contains? #{:hosted :native :distributed :ai} profile)]
      (is (= 4 (count (:records result))))
      (is (= (repeat 4 profile) (map :profile (:records result))))
      (is (= (repeat 4 expected) (map :profile-legal? (:records result)))))))

(deftest record-facade-interposition-precedes-module-observation
  (let [calls (atom [])
        module (reify clojure.lang.ILookup
                 (valAt [_ _] (throw (ex-info "default construction must remain unreachable" {})))
                 (valAt [_ _ _] (throw (ex-info "default construction must remain unreachable" {}))))
        result (c9/with-operations
                 {:c9-runtime-check-records (fn [value] (swap! calls conj value) :interposed)}
                 #(c9/c9-runtime-check-records module))]
    (is (= :interposed result))
    (is (= 1 (count @calls)))
    (is (identical? module (first @calls)))))

(def ^:private graph-projectors
  [ownership/native-ownership-graph graphs/ownership-graph
   (fn [span ids node module graph]
     (c9/with-operations {:source-span span :c9-node-ids ids :c9-node node}
       #(c9/c9-ownership-graph module graph)))])

(deftest native-graph-projections-retain-source-owner-order-and-callback-order
  (doseq [project graph-projectors]
    (let [calls (atom []) source-z {:span :original-z :origin-chain [:generated :source]}
          source-a {:span :original-a :origin-chain [:source]}
          module {:module 'native.graph :source-path "native-graph-data" :profile :hosted :target :jvm}
          input {:nodes {"z" {:source source-z} "a" {:source source-a}}}
          span (fn [path index] (swap! calls conj [:span path index]) {:source path :form-index index})
          ids (fn [graph] (is (identical? input graph)) (swap! calls conj :ids) ["z" "a"])
          node (fn [values index fallback]
                 (swap! calls conj [:node values index fallback]) :selected-node)
          result (project span ids node module input)]
      (is (= :gravity/c9-ownership-graph (:artifact result)))
      (is (= ["a" "z"] (vec (keys (:owners result)))))
      (is (= "owner-2" (get-in result [:owners "a" :owner-id])))
      (is (= "owner-1" (get-in result [:owners "z" :owner-id])))
      (is (= :persistent-immutable (get-in result [:owners "z" :kind])))
      (is (= :owned-mutable (get-in result [:owners "a" :kind])))
      (is (identical? source-z (get-in result [:owners "z" :source])))
      (is (identical? source-a (get-in result [:owners "a" :source])))
      (is (= [:hosted :jvm] ((juxt :profile :target) (get-in result [:owners "a"]))))
      (is (= :selected-node (get-in result [:moves 0 :value])))
      (is (= [:ids [:node ["z" "a"] 1 "core-node-owned"]
              [:span "native-graph-data" 0] [:span "native-graph-data" 0]] @calls)))))

(deftest native-borrow-projection-retains-each-callback-and-fallback
  (doseq [project [ownership/native-borrow-graph graphs/borrow-graph
                   (fn [ids node module graph]
                     (c9/with-operations {:c9-node-ids ids :c9-node node}
                       #(c9/c9-borrow-graph module graph)))]]
    (let [calls (atom [])
          ids (fn [_] (swap! calls conj :ids) ["zero" "one"])
          node (fn [values index fallback]
                 (swap! calls conj [index fallback]) (get values index fallback))
          result (project ids node {:module 'native.graph} {})]
      (is (= :gravity/c9-borrow-graph (:artifact result)))
      (is (= ["one" "one" "one" "core-node-field" "core-node-transfer"]
             (mapv :value (:edges result))))
      (is (= [:ids [1 "core-node-owned"] [1 "core-node-owned"] [1 "core-node-owned"]
              [2 "core-node-field"] [3 "core-node-transfer"]] @calls)))))

(deftest native-graph-refusals-preserve-exception-identity-and-stop-later-callbacks
  (doseq [project graph-projectors
          failure [:ids :node :span :owner-key]]
    (let [calls (atom []) error (ex-info "original graph callback" {:callback failure})
          ids (fn [_]
                (swap! calls conj :ids)
                (when (= :ids failure) (throw error))
                (if (= :owner-key failure) ["string-id" :keyword-id] []))
          node (fn [& _] (swap! calls conj :node) (when (= :node failure) (throw error)) :node)
          span (fn [& _] (swap! calls conj :span) (throw error))
          observed (try (project span ids node {} {}) (catch Throwable value value))]
      (if (= :owner-key failure)
        (is (instance? ClassCastException observed))
        (is (identical? error observed)))
      (is (= (case failure :ids [:ids] :node [:ids :node]
                    :span [:ids :node :span] :owner-key [:ids]) @calls)))))

(deftest real-paired-gravity-ownership-programs-accept-and-reject
  ;; Current Gravity normalized-request kernels. No authenticated Core/C8
  ;; composition or native/provider grant is attempted by these fixtures.
  (doseq [extension [".gravity" ".qst"]]
    (let [plan (fixture-support/compile-plan
                (fixture-support/fixture-relative-path
                 "accepted" "ownership-transitions" extension))
          bad-plan (fixture-support/compile-plan
                    (fixture-support/fixture-relative-path
                     "rejected" "invalid-ownership-transitions" extension))
          request (fixture-support/invoke (delay plan)
                                          'sh10-many-immutable-request [])
          bad-request (fixture-support/invoke (delay bad-plan)
                                              'sh10-use-after-move-request [])
          result (fixture-support/check request)
          rejection (fixture-support/check bad-request)]
      (doseq [compiled [plan bad-plan]]
        (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
        (is (= (:plan-id compiled)
               (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc compiled :plan-id))))))))
      (is (= :accepted (:status result)))
      (is (= :rejected (:status rejection)))
      (is (= "C9-USE-AFTER-MOVE" (get-in rejection [:diagnostics 0 :rule])))
      (is (= (:source-span request) (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (fixture-support/invoke-c9
               'sh10-verify-ownership-result [request result])))))))

(deftest forwarding-propagation-and-schema-retain-existing-semantics
  (let [source (node :allocated :str [] {:provider-id :gravity.reference/jvm-managed-allocator
                                        :lifetime :managed-reachability})
        local (node :local :local [:allocated] {:binding-node-id :allocated})
        join (node :join :if [:test :local :other] {:incoming-node-ids [:local :other]})
        nodes [source local join]
        result (ownership/propagate-managed-ownership #'b/p15-s23-closed-core-forwarded-value-node-ids nodes)]
    (is (= result (b/p15-s23-closed-core-propagate-managed-ownership nodes)))
    (is (= (mapv :source nodes) (mapv :source result)))
    (is (= [:allocated] (ownership/forwarded-value-node-ids local)))
    (is (= [:local :other] (ownership/forwarded-value-node-ids join)))
    (is (= #{:allocated :local :join} (b/p15-s23-closed-core-expected-managed-node-ids nodes)))
    (is (= (b/p15-s23-closed-core-expected-managed-node-ids nodes)
           (ownership/expected-managed-node-ids #'b/p15-s23-closed-core-forwarded-value-node-ids nodes)))
    (is (= :not-required (get-in (first (b/p15-s23-closed-core-propagate-managed-ownership [local source]))
                                [:ownership :provider-requirement])))
    (doseq [candidate result]
      (is (b/p15-s23-closed-core-persistent-aliasing-valid? candidate))
      (is (b/p15-s23-closed-core-persistent-ownership-schema-valid? candidate true))
      (is (= (b/p15-s23-closed-core-persistent-ownership-expected-keys candidate true)
             (ownership/persistent-ownership-expected-keys
               b/p15-s23-closed-core-persistent-ownership-common-keys candidate true)))
      (doseq [field [:model :shareability :alias-policy :mutation]]
        (is (false? (ownership/persistent-aliasing-valid? (assoc-in candidate [:ownership field] :forged)))))
      (is (false? (b/p15-s23-closed-core-persistent-ownership-schema-valid?
                    (assoc-in candidate [:ownership :forged] true) true))))
    (doseq [[operation operands expected]
            [[:let-binding [:a] [:a]] [:do [:a :b] [:b]] [:let [:a :b] [:b]]
             [:function [:a :b] [:b]] [:literal [] []] [:unknown [:a] []]]]
      (is (= expected (ownership/forwarded-value-node-ids {:source-operation operation :operands operands}))))))

(deftest owner-identity-fallback-and-extra-precedence-are-preserved
  (let [function {:kind :function :node-id "function-source-id"}
        product {:core-nodes [function] :ownership-facts {:value {:owner-id :forged}}}
        subject {:node-id :value}
        expected (b/p15-s23-closed-core-digest
                   {:kind :gravity/persistent-shared-owner-domain :function-node-id "function-source-id"})]
    (is (= expected (b/p15-s23-closed-core-shared-owner-domain-id [function])))
    (is (= expected (b/p15-s23-closed-core-canonical-owner-id product subject)))
    (is (= expected (ownership/canonical-owner-id #'b/p15-s23-closed-core-shared-owner-domain-id product subject)))
    (is (= :fallback (b/p15-s23-closed-core-canonical-owner-id
                      {:ownership-facts {:value {:owner-id :fallback}}} subject)))
    (is (= :not-applicable (b/p15-s23-closed-core-canonical-owner-id {} subject)))
    (is (nil? (b/p15-s23-closed-core-shared-owner-domain-id [])))
    ;; Existing merge precedence constructs DATA and does not authorize it.
    (is (= :forged (:mutation (ownership/persistent-ownership :value {:mutation :forged}))))))

(deftest wrappers-preserve-late-vars-dispatch-and-host-lookup-order
  (let [original b/p15-s23-closed-core-persistent-ownership-schema-valid?
        seen (atom nil)]
    (with-redefs [b/p15-s23-closed-core-persistent-ownership-schema-valid?
                  (fn [& args] (reset! seen args) :two-argument-port)]
      (is (= :two-argument-port (original {:source-operation :str})))
      (is (= [{:source-operation :str} true] @seen))))
  (with-redefs [b/p15-s23-closed-core-forwarded-value-node-ids (constantly [])
                b/p15-s23-closed-core-shared-owner-domain-id (constantly :owner-port)
                b/p15-s23-closed-core-persistent-ownership-expected-keys (constantly #{:only})]
    (is (= #{:allocated} (b/p15-s23-closed-core-expected-managed-node-ids
                          [{:node-id :allocated :source-operation :str}
                           {:node-id :local :source-operation :local :operands [:allocated]}])))
    (is (= :owner-port (b/p15-s23-closed-core-canonical-owner-id {} {})))
    (is (b/p15-s23-closed-core-persistent-ownership-schema-valid? {:ownership {:only nil}} false)))
  (with-redefs [b/p15-s23-closed-core-digest (constantly :digest-port)]
    (is (= :digest-port (b/p15-s23-closed-core-shared-owner-domain-id [{:kind :function}]))))
  (let [lookups (atom [])
        subject (reify clojure.lang.ILookup
                  (valAt [_ key] (swap! lookups conj key) nil)
                  (valAt [_ key fallback] (swap! lookups conj key) fallback))]
    (is (false? (ownership/persistent-aliasing-valid? subject)))
    (is (= [:ownership :operands] @lookups))
    (reset! lookups [])
    (ownership/persistent-ownership-expected-keys #{} subject false)
    (is (= [:ownership :source-operation] @lookups))))
