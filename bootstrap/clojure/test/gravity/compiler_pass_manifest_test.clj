(ns gravity.compiler-pass-manifest-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.set :as set]
            [gravity.compiler-pass-manifest :as manifest]))

(def upstream-artifact
  {:kind :gravity/stage0-math-conformance-artifact
   :profile-manifest {:metadata {}}})

(defn diagnostic-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (:id (ex-data exception)))))

(deftest compiler-pass-manifest-is-a-bootstrap-free-stage-boundary
  (is (nil? (find-ns 'gravity.bootstrap)))
  (let [contract (manifest/compiler-pass-manifest-contract)
        publics (ns-publics 'gravity.compiler-pass-manifest)
        artifact (manifest/compiler-pass-source-artifact-from-upstream
                  "compiler-passes.gravity" upstream-artifact)]
    (is (= 'gravity.compiler-pass-manifest (:namespace contract)))
    (is (= :stage0-compiler-pass-manifest
           (:contract-boundary contract)))
    (is (some #{'gravity.bootstrap}
              (get-in contract [:dependency-direction :forbids])))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-authority? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name spec] (:public-api contract) :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name)))) name))
    (is (nil? (get (ns-aliases 'gravity.compiler-pass-manifest)
                   'bootstrap)))
    (is (= :gravity/stage0-pass-contract-manifest-artifact
           (:kind artifact)))
    (is (= ["C1" "C15" "C16" "C17" "C18"]
           (:document-set artifact)))
    (is (= manifest/compiler-pass-default-stage-order
           (:pipeline-stage-order artifact)))
    (is (= 19 (count (:pass-contract-registry artifact))))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= manifest/compiler-pass-diagnostic-ids
           (get-in artifact [:compiler-pass-results
                             :required-diagnostic-ids])))))

(deftest stage-validators-reject-invalid-boundary-evidence
  (let [suite (manifest/compiler-pass-suite {})
        source-path "compiler-passes.gravity"
        profile-manifest {}]
    (doseq [[expected-id validate]
            [["C1-PASS-CONTRACT"
              #(manifest/compiler-pass-validate-pipeline!
                source-path profile-manifest
                (update suite :contracts
                        (fn [contracts]
                          (assoc contracts 0 (dissoc (first contracts)
                                                     :output)))))]
             ["C1-EVIDENCE-DROP"
              #(manifest/compiler-pass-validate-pipeline!
                source-path profile-manifest
                (update suite :contracts
                        (fn [contracts]
                          (assoc contracts 0
                                 (assoc (first contracts)
                                        :invalidates [:source-spans]
                                        :regenerates []
                                        :emits [])))))]
             ["C16-PROOF"
              #(manifest/compiler-pass-validate-incremental!
                source-path profile-manifest
                (assoc suite :proof-reuse-records
                       [{:proof-id :stale :status :stale :reuse :accepted}]))]
             ["C17-CAPABILITY"
              #(manifest/compiler-pass-validate-plugins!
                source-path profile-manifest
                (assoc-in suite [:plugin-manifest :requested-scopes]
                          #{:write-release-artifact}))]
             ["C18-EVIDENCE"
              #(manifest/compiler-pass-validate-verification!
                source-path profile-manifest
                (assoc suite :risk-classification
                       [{:pass :read-source
                         :risk :critical
                         :minimum-evidence #{:required}
                         :available-evidence #{}
                         :release-gate :required}]))]]]
      (testing expected-id
        (is (= expected-id (diagnostic-id validate)))))))

(def ^:private legacy-required manifest/compiler-pass-contract-required-fields)
(def ^:private legacy-durable manifest/compiler-pass-durable-facts)
(defn- legacy-missing [record fields] (manifest/compiler-pass-missing-fields record fields))
(defn- legacy-intersection [left right] (set/intersection left right))
(defn- legacy-difference [left right] (set/difference left right))
(defn- legacy-subset? [left right] (set/subset? left right))
(defn- legacy-c1-failure [id path input record extra]
  (manifest/compiler-pass-fail! id path input record extra))
(defn- legacy-c18-failure [id path subject extra]
  (throw (ex-info "native C18 refusal" {:id id :path path :subject subject :extra extra})))
(defn- legacy-c1-ports []
  {:missing-fields #'legacy-missing :required-fields #'legacy-required :durable-facts #'legacy-durable
   :intersection #'legacy-intersection :difference #'legacy-difference :fail! #'legacy-c1-failure})
(defn- legacy-c18-ports []
  {:subset? #'legacy-subset? :difference #'legacy-difference :fail! #'legacy-c18-failure})
(defn- legacy-failure-data [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(def ^:private legacy-module {:source-path "legacy-compiler.gravity" :profile :hosted :target :jvm})
(def ^:private legacy-contract
  (manifest/compiler-pass-contract :native/compile :C1 :source :instruction-plan [] [] [] [] [] [] :high [:fixture]))

(deftest legacy-gate-apis-declare-separate-native-boundaries
  (let [contract (manifest/compiler-pass-manifest-contract)]
    (is (= 38 (count (ns-publics 'gravity.compiler-pass-manifest))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (every? (set (:governing-contracts contract)) '[C1 C15 C18 D1 D9]))
    (doseq [operation [#'manifest/native-stage0-compiler-manifest #'manifest/native-stage0-compiler-suite
                       #'manifest/native-stage0-compiler-suite-present? #'manifest/validate-native-stage0-pass-contract!
                       #'manifest/validate-native-stage0-verification-risk!]]
      (let [entry (get-in contract [:public-api (:name (meta operation))])]
        (is (= (:arglists (meta operation)) (:arglists entry)))
        (is (= :stage0-native-legacy-compiler-gate (:contract-boundary entry)))
        (is (= :none (:admission entry)))
        (is (false? (:authenticates-inputs? entry)))
        (is (false? (:issues-certificates? entry)))))))

(deftest legacy-manifest-and-suite-retain-lookups-identity-and-presence
  (let [events (atom []) values (zipmap [:profile :target :effects :capabilities :metadata] (repeatedly 5 #(Object.)))
        input (reify clojure.lang.ILookup
                (valAt [_ key] (swap! events conj key) (get values key))
                (valAt [_ key default] (get values key default)))
        projected (manifest/native-stage0-compiler-manifest input)]
    (is (= [:profile :target :effects :capabilities :metadata] @events))
    (doseq [[output input] [[:profile :profile] [:target :target] [:source-effects :effects]
                          [:source-capabilities :capabilities] [:metadata :metadata]]]
      (is (identical? (get values input) (get projected output)))))
  (doseq [[compiler expected present?]
          [[{} {} false] [{:passes {}} {} false] [{:compiled-gate nil} nil true]
           [{:compiled-gate false} false true] [{:compiled-gate {}} {} true]]]
    (let [input {:metadata {:compiler compiler}}]
      (is (= expected (manifest/native-stage0-compiler-suite input)))
      (is (= present? (manifest/native-stage0-compiler-suite-present? input)))))
  (let [gate (Object.) input {:metadata {:compiler {:compiled-gate gate}}}]
    (is (identical? gate (manifest/native-stage0-compiler-suite input))))
  (is (= {} (manifest/native-stage0-compiler-suite {})))
  (is (false? (manifest/native-stage0-compiler-suite-present? {}))))

(deftest legacy-c1-checks-required-fields-and-durable-replacements
  (is (nil? (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {} legacy-contract)))
  (doseq [field legacy-required]
    (let [record (legacy-failure-data #(manifest/validate-native-stage0-pass-contract!
                                       (legacy-c1-ports) legacy-module {} (dissoc legacy-contract field)))]
      (is (= "C1-PASS-CONTRACT" (:id record)))
      (is (= [field] (:missing-fields record)))
      (is (= :compiler-pass-contract (:diagnostic-family record)))
      (is (= "legacy-compiler.gravity" (get-in record [:source-span :source])))))
  ;; Required-field presence accepts false; it does not authenticate a verifier.
  (is (nil? (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {}
              (assoc legacy-contract :verifier-gate? false))))
  (doseq [replacement [:regenerates :replacement-evidence :emits]]
    (is (nil? (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {}
                (assoc legacy-contract :invalidates #{:types} replacement [:types])))))
  (let [record (legacy-failure-data #(manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {}
                                     (assoc legacy-contract :invalidates #{:types :nondurable})))]
    (is (= "C1-EVIDENCE-DROP" (:id record)))
    (is (= [:types] (:missing-fields record)))))

(deftest legacy-c1-ports-and-catalogs-remain-live-with-original-lookup-order
  (let [events (atom []) fields [:late] base (legacy-c1-ports)
        old-missing (fn [record actual] (swap! events conj [:selected record actual]) [:late])
        late-missing (fn [& _] (throw (AssertionError. "callee changed during catalog selection")))
        operations (reify clojure.lang.ILookup
                     (valAt [_ key]
                       (when (= :required-fields key)
                         (alter-var-root #'legacy-missing (constantly late-missing)))
                       (get base key))
                     (valAt [_ key default] (get base key default)))]
    (with-redefs [legacy-required fields legacy-missing old-missing]
      (is (= "C1-PASS-CONTRACT"
             (:id (legacy-failure-data #(manifest/validate-native-stage0-pass-contract!
                                         operations legacy-module {} {})))))
      (is (= [[:selected {} fields]] @events))))
  (let [events (atom []) data {:invalidates [:types] :regenerates [] :replacement-evidence [] :emits [:types]}
        record (reify clojure.lang.ILookup
                 (valAt [_ key]
                   (swap! events conj [:record key])
                   (when (= key :invalidates)
                     (alter-var-root #'legacy-intersection
                                     (constantly (fn [& _] (throw (AssertionError. "late intersection"))))))
                   (get data key))
                 (valAt [_ key default] (get data key default)))]
    (with-redefs [legacy-missing (fn [& _] [])
                  legacy-intersection (fn [left right] (swap! events conj :intersection) (set/intersection left right))]
      (is (nil? (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {} record)))
      (is (= [[:record :invalidates] :intersection [:record :regenerates]
              [:record :replacement-evidence] [:record :emits]] @events))))
  (with-redefs [legacy-missing (fn [& _] (alter-var-root #'legacy-durable (constantly #{:late})) [])
                legacy-durable legacy-durable]
    (is (= "C1-EVIDENCE-DROP"
           (:id (legacy-failure-data #(manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {}
                                       {:invalidates [:late]})))))))

(deftest legacy-c1-returning-and-throwing-refusals-preserve-identity
  (let [record {} input (Object.) marker (Object.) calls (atom [])]
    (with-redefs [legacy-c1-failure (fn [& args] (swap! calls conj (vec args)) marker)
                  legacy-intersection (fn [& _] (throw (AssertionError. "durable check followed required-field refusal")))]
      (is (identical? marker (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module input record)))
      (is (= 1 (count @calls)))
      (is (= "C1-PASS-CONTRACT" (get-in @calls [0 0])))
      (is (identical? input (get-in @calls [0 2])))
      (is (identical? record (get-in @calls [0 3])))))
  (doseq [sentinel [(Exception. "original C1 exception") (Error. "original C1 error")]]
    (with-redefs [legacy-c1-failure (fn [& _] (throw sentinel))]
      (is (identical? sentinel
                      (try (manifest/validate-native-stage0-pass-contract! (legacy-c1-ports) legacy-module {} {})
                           (catch Throwable error error)))))))

(deftest legacy-c18-keeps-risk-subset-sorted-evidence-and-default-version
  (let [unused-module (reify clojure.lang.ILookup
                        (valAt [_ _] (throw (AssertionError. "accepted risk read module")))
                        (valAt [_ _ _] (throw (AssertionError. "accepted risk read module"))))]
    (doseq [risk [:low :medium nil]]
      (is (nil? (manifest/validate-native-stage0-verification-risk! {} unused-module
                  {:risk risk :minimum-evidence [:needed] :available-evidence []}))))
    (doseq [risk [:high :critical]]
      (is (nil? (manifest/validate-native-stage0-verification-risk! (legacy-c18-ports) unused-module
                  {:risk risk :minimum-evidence [:needed] :available-evidence [:needed]})))))
  (doseq [risk [:high :critical] version [nil false ""]]
    (let [record {:risk risk :pass :native/compile :artifact-id :native/pass :version version
                  :minimum-evidence [:z :a] :available-evidence []}
          failure (legacy-failure-data #(manifest/validate-native-stage0-verification-risk!
                                          (legacy-c18-ports) legacy-module record))]
      (is (= "C18-EVIDENCE" (:id failure)))
      (is (= "legacy-compiler.gravity" (:path failure)))
      (is (= {:stage :stage0-compiled-compiler-gate :pass-id :native/compile
              :version (or version "stage0-p06-s1") :risk-class risk
              :required-evidence #{:a :z} :available-evidence #{}
              :affected-profiles #{:hosted} :affected-targets #{:jvm} :artifact-id :native/pass}
             (:subject failure)))
      (is (= [:a :z] (get-in failure [:extra :missing-fields]))))))

(deftest legacy-c18-selects-failure-before-module-arguments-and-keeps-throws
  (let [events (atom []) marker (Object.) values {:source-path (Object.) :profile (Object.) :target (Object.)}
        late (fn [& _] (throw (AssertionError. "late C18 failure")))
        input (reify clojure.lang.ILookup
                (valAt [_ key]
                  (swap! events conj key)
                  (alter-var-root #'legacy-c18-failure (constantly late))
                  (get values key))
                (valAt [_ key default] (get values key default)))
        calls (atom [])]
    (with-redefs [legacy-c18-failure (fn [& args] (swap! calls conj (vec args)) marker)]
      (is (identical? marker (manifest/validate-native-stage0-verification-risk! (legacy-c18-ports) input
                              {:risk :high :minimum-evidence [:needed]})))
      (is (= [:source-path :profile :target] @events))
      (is (identical? (:source-path values) (get-in @calls [0 1])))
      (is (identical? (:profile values) (first (get-in @calls [0 2 :affected-profiles]))))
      (is (identical? (:target values) (first (get-in @calls [0 2 :affected-targets]))))))
  (doseq [sentinel [(Exception. "original C18 exception") (Error. "original C18 error")]]
    (with-redefs [legacy-c18-failure (fn [& _] (throw sentinel))]
      (is (identical? sentinel
                      (try (manifest/validate-native-stage0-verification-risk! (legacy-c18-ports) legacy-module
                             {:risk :critical :minimum-evidence [:needed]})
                           (catch Throwable error error)))))))
