(ns gravity.diagnostics-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c15-diagnostics :as c15]
            [gravity.c15-diagnostics.artifact :as artifact]
            [gravity.c15-diagnostics.operations :as c15-ops]
            [gravity.c15-diagnostics.projection :as projection]
            [gravity.c15-diagnostics.records :as records]
            [gravity.c15-diagnostics.validation :as validation]
            [gravity.diagnostics :as diagnostics]))

(defn- captured-exception-info
  [f]
  (try
    (f)
    ::unexpected-success
    (catch clojure.lang.ExceptionInfo exception
      exception)))

(deftest base-exception-info-carrier-is-extracted-with-bootstrap-parity
  (testing "ordinary and nil data preserve the stage0 carrier"
    (doseq [data [{:fact :ordinary :count 2} nil]]
      (let [direct (diagnostics/diagnostic "D1-TEST" "carrier message" data)
            compatibility
            (bootstrap/diagnostic "D1-TEST" "carrier message" data)
            expected
            (merge {:id "D1-TEST"
                    :message "carrier message"
                    :bootstrap-stage :stage0}
                   data)]
        (doseq [exception [direct compatibility]]
          (is (instance? clojure.lang.ExceptionInfo exception))
          (is (= "carrier message" (ex-message exception)))
          (is (= expected (ex-data exception)))
          (is (nil? (ex-cause exception))))
        (is (= (ex-data direct) (ex-data compatibility))))))
  (testing "caller data retains the historical override precedence"
    (let [data {:id nil
                :message :caller-owned-message
                :bootstrap-stage nil
                :fact :retained}
          direct
          (diagnostics/diagnostic "D1-DEFAULT" "positional message" data)
          compatibility
          (bootstrap/diagnostic "D1-DEFAULT" "positional message" data)]
      (doseq [exception [direct compatibility]]
        (is (= "positional message" (ex-message exception)))
        (is (= {:id nil
                :message :caller-owned-message
                :bootstrap-stage nil
                :fact :retained}
               (ex-data exception))))))
  (testing "both fail functions throw the same unwrapped carrier"
    (let [data {:fact :rejection}
          direct
          (captured-exception-info
           #(diagnostics/fail! "D1-FAIL" "rejected" data))
          compatibility
          (captured-exception-info
           #(bootstrap/fail! "D1-FAIL" "rejected" data))]
      (doseq [exception [direct compatibility]]
        (is (instance? clojure.lang.ExceptionInfo exception))
        (is (= "rejected" (ex-message exception)))
        (is (= {:id "D1-FAIL"
                :message "rejected"
                :bootstrap-stage :stage0
                :fact :rejection}
               (ex-data exception))))
      (is (= (ex-data direct) (ex-data compatibility)))))
  (testing "bootstrap fail remains a thin compatibility seam"
    (let [sentinel (ex-info "sentinel" {:sentinel true})
          observed
          (with-redefs [bootstrap/diagnostic
                        (fn [_ _ _] sentinel)]
            (captured-exception-info
             #(bootstrap/fail! "ignored" "ignored" {})))]
      (is (identical? sentinel observed))))
  (testing "the extracted carrier has no namespace dependency back to bootstrap"
    (is (empty? (ns-aliases 'gravity.diagnostics)))
    (is (= '([id message data]) (:arglists (meta #'diagnostics/diagnostic))))
    (is (= '([id message data]) (:arglists (meta #'diagnostics/fail!))))
    (is (= #{'diagnostic 'fail! 'stable-id 'diagnostic-record
             'resolve-request-id 'bind-private-record 'verify-private-record 'source-overrides 'native-catalog
             'native-context-valid? 'native-primary-valid? 'validate-native!
             'native-fixture-diagnostics 'native-artifact-base 'native-capability-proof
             'native-failure! 'native-validate-source-overrides! 'native-source-artifact 'native-file-artifact}
           (set (keys (ns-publics 'gravity.diagnostics))))))
  (testing "the namespace contract states its API and ownership boundary"
    (let [contract-var
          (get (ns-interns 'gravity.diagnostics) 'namespace-contract)
          contract (var-get contract-var)]
      (is (true? (:private (meta contract-var))))
      (is (= 'gravity.diagnostics (:namespace contract)))
      (is (= :stage0-diagnostic-carriers-and-record-data
             (:contract-boundary contract)))
      (is (= #{'diagnostic 'fail! 'stable-id 'diagnostic-record
               'resolve-request-id 'bind-private-record 'verify-private-record 'source-overrides 'native-catalog
               'native-context-valid? 'native-primary-valid? 'validate-native!
               'native-fixture-diagnostics 'native-artifact-base 'native-capability-proof
               'native-failure! 'native-validate-source-overrides! 'native-source-artifact 'native-file-artifact}
             (set (keys (:public-api contract)))))
      (is (= [:native-c15-artifacts :native-validation-configuration :explicit-native-validation-operations
              :native-compiler-metadata :native-c15-rule-id-order :rule-id :human-message :structured-data :explicit-span-and-identity-operations
              :native-seed-diagnostic-fields :source-invocation :canonical-request-digest
              :original-eleven-c15-fields :private-candidate :complete-digest-request
              :trusted-native-source-module-configuration-and-c14-parent
              :trusted-zero-argument-operation-suppliers]
             (:artifact-inputs contract)))
      (is (= [:native-phase-and-primary-predicates :native-record-validation-result
              :native-source-overrides :native-c15-catalog :stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id
              :resolved-request-data :private-c15-binding :source-owned-rejection
              :native-c15-fixture-diagnostics-artifact-and-capability-data
              :native-failure-payload-or-original-callback-result]
             (:artifact-outputs contract)))
      (is (= ['clojure.core 'clojure.string]
             (get-in contract [:dependency-direction :requires])))
      (is (= ['gravity.bootstrap 'gravity.c15-diagnostics 'gravity.cli 'gravity.release]
             (get-in contract [:dependency-direction :forbids])))
      (is (some #{:canonical-c15-schema}
                (get-in contract [:diagnostic-ownership :does-not-own])))
      (is (true? (:bootstrap-hosted? contract)))
      (is (true? (:clojure-seed-boundary? contract)))
      (is (false? (:self-hosted? contract))))))

(deftest complete-stage-api-retains-explicit-metadata-and-native-boundaries
  (let [publics (ns-publics 'gravity.diagnostics)
        contract (:gravity/namespace-contract (meta #'diagnostics/diagnostic))]
    (is (= 19 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name operation] publics]
      (is (= contract (:gravity/namespace-contract (meta operation))))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists]))))
    (is (some #{'gravity.c15-diagnostics.artifact} (get-in contract [:dependency-direction :consumed-by])))
    (is (some #{:canonical-stream-ordering} (get-in contract [:diagnostic-ownership :does-not-own])))))

(deftest bootstrap-record-wrapper-preserves-phase-context
  (doseq [context [{} {:profile :kernel :target :llvm :syntax-id "caller-syntax-id"}]]
    (let [arguments ["C15-FACTS" :info :c15 :facts "caller.gravity" 2
                     "caller-artifact" {:fact :preserved} [{:kind :inspect}]]
          options (mapcat identity context)
          compatibility (apply bootstrap/c15-diagnostic-record (concat arguments options))
          direct (apply diagnostics/diagnostic-record
                        bootstrap/source-span bootstrap/c15-stable-diagnostic-id
                        (concat arguments options))]
      (is (= direct compatibility))
      (is (= (:profile context) (:profile compatibility)))
      (is (= (:target context) (:target compatibility)))
      (is (contains? (:primary compatibility) :syntax-id))
      (is (= (:syntax-id context) (get-in compatibility [:primary :syntax-id]))))))

(def ^:private c15-source
  (delay
    (let [path (.getCanonicalPath
                (io/file "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity"))
          text (slurp path :encoding "UTF-8")
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      {:path path :text text :sha256 (bootstrap/sha256-hex text)
       :plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text)})))

(defn- invoke-c15 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-private-diagnostic-binding-development :compiler-artifact-plan? true}
   (:plan @c15-source) function arguments))

(defn- native-schema-rejection []
  (let [exception (captured-exception-info
                    #(c15/c15-diagnostics-validate! (:path @c15-source) {}))]
    {:message (ex-message exception) :data (ex-data exception)}))

(defn- system-fields [rejection]
  ;; A new private C15 diagnostic about an actual schema failure. The original
  ;; producer diagnostic is retained separately, not retagged or authenticated.
  [(get-in rejection [:data :id]) :error :compiler-diagnostics
   {:span nil :syntax-id nil :artifact nil} [] [] nil nil
   (select-keys (:data rejection)
                [:schema-field :missing-fields :offending-diagnostic-id :artifact-id])
   [{:kind :supply-valid-diagnostic-data}] []])

(defn- bind-private [fields]
  (diagnostics/bind-private-record invoke-c15 bootstrap/sh03-reader-canonical-digest
                                   (:path @c15-source) fields))

(defn- verify-private [fields candidate request]
  (diagnostics/verify-private-record invoke-c15 bootstrap/sh03-reader-canonical-digest
                                     (:path @c15-source) fields candidate request))

(deftest complete-c15-source-binds-an-actual-native-schema-failure-privately
  (let [rejection (native-schema-rejection) fields (system-fields rejection)
        before (pr-str rejection) field-text (pr-str fields)
        result (bind-private fields) record (:diagnostic result)
        request (:diagnostic-id-request record)]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind (:plan @c15-source))))
    (is (= (:sha256 @c15-source) (bootstrap/sha256-hex (:text @c15-source))))
    (is (= "C15-SCHEMA" (get-in rejection [:data :id]) (:rule record)))
    (is (= :passed (:status result)))
    (is (= :passed (get-in result [:verification :status])))
    (is (= record (assoc (invoke-c15 'build-c15-diagnostic fields)
                         :diagnostic-id (:diagnostic-id record))))
    (is (= (bootstrap/sh03-reader-canonical-digest (:path @c15-source) request)
           (:diagnostic-id record)))
    (is (not= (bootstrap/sh03-reader-canonical-digest (:path @c15-source) (:preimage request))
              (:diagnostic-id record)))
    (is (= (nth fields 8) (get-in request [:preimage :facts])))
    (is (= {:span nil :syntax-id nil :artifact nil} (:primary record)))
    (is (nil? (:profile record)))
    (is (nil? (:target record)))
    (is (= before (pr-str rejection)))
    (is (= field-text (pr-str fields)))
    (is (= #{:digest-recomputation :upstream-source-authentication :public-safe-projection
             :canonical-stream-verification :self-hosting :release}
           (set (get-in result [:verification :nonclaims]))))
    (is (not (contains? result :public-safe?)))))

(deftest private-binding-keeps-source-construction-failures-unresolved
  (let [fields (system-fields (native-schema-rejection))
        calls (atom [])]
    (doseq [[changed rule] [[(assoc fields 8 "untyped facts") "C15-FACTS"]
                          [(assoc fields 9 []) "C15-REMEDIATION"]
                          [(assoc fields 6 "invented profile") "C15-SCHEMA"]
                          [(update fields 3 dissoc :syntax-id) "C15-SPAN"]]]
      (let [expected (invoke-c15 'build-c15-diagnostic changed)
            observed (diagnostics/bind-private-record
                       invoke-c15 (fn [& args] (swap! calls conj args))
                       (:path @c15-source) changed)]
        (is (= expected observed))
        (is (= :rejected (:status observed)))
        (is (= rule (get-in observed [:diagnostics 0 :rule])))
        (is (= {:digest-ref 0} (get-in observed [:diagnostics 0 :diagnostic-id])))))
    (is (empty? @calls))))

(deftest fresh-c15-verification-rejects-candidate-and-coherent-request-substitution
  (let [fields (system-fields (native-schema-rejection))
        record (:diagnostic (bind-private fields))
        request (:diagnostic-id-request record)]
    (is (= :passed (:status (verify-private fields record request))))
    (doseq [key (keys record)
            candidate [(dissoc record key) (assoc record key :substituted)]]
      (let [result (verify-private fields candidate request)]
        (is (= :rejected (:status result)))
        (is (= "C15-ID" (get-in result [:diagnostics 0 :rule])))))
    (doseq [changed [(assoc request :ordinal 1) (assoc request :purpose :other-purpose)
                     (assoc request :domain :other-domain) (assoc request :extra true)
                     (assoc-in request [:preimage :facts :control] :substituted)]]
      (let [candidate (assoc record :diagnostic-id-request changed
                             :diagnostic-id (bootstrap/sh03-reader-canonical-digest
                                              (:path @c15-source) changed))
            result (verify-private fields candidate changed)]
        (is (= :rejected (:status result)))
        (is (= "C15-ID" (get-in result [:diagnostics 0 :rule])))))))

(deftest binding-rehashes-before-verification-and-emits-no-candidate-on-drift
  (let [fields (system-fields (native-schema-rejection)) calls (atom [])
        digest (fn [path request]
                 (swap! calls conj request)
                 (bootstrap/sh03-reader-canonical-digest
                   path (if (= 1 (count @calls)) request (assoc request :control :changed))))
        result (diagnostics/bind-private-record invoke-c15 digest (:path @c15-source) fields)]
    (is (= 2 (count @calls)))
    (is (apply = @calls))
    (is (= :rejected (:status result)))
    (is (= "C15-ID" (get-in result [:diagnostics 0 :rule])))
    (is (not (contains? result :diagnostic)))
    (is (= result (invoke-c15 'verify-c15-resolved-diagnostic
                              [fields (:diagnostic (bind-private fields)) (first @calls)
                               (bootstrap/sh03-reader-canonical-digest
                                 (:path @c15-source) (assoc (first @calls) :control :changed))])))))

(deftest private-identity-excludes-wording-and-preserves-separate-original-fields
  (let [fields (system-fields (native-schema-rejection))
        wording (assoc-in fields [9 0 :message] "Changed human guidance")
        semantic (assoc-in fields [8 :control] :changed)
        [base reworded changed] (mapv #(-> % bind-private :diagnostic) [fields wording semantic])]
    (is (= (:diagnostic-id base) (:diagnostic-id reworded)))
    (is (not= (:diagnostic-id base) (:diagnostic-id changed)))
    (is (not= (:remediation base) (:remediation reworded)))
    (doseq [[original candidate] [[fields reworded] [wording base] [fields changed] [semantic base]]]
      (let [result (verify-private original candidate (:diagnostic-id-request candidate))]
        (is (= :rejected (:status result)))
        (is (= "C15-ID" (get-in result [:diagnostics 0 :rule])))))))

(deftest reader-request-wrapper-retains-complete-data-and-late-operation-ports
  ;; This is explicit prevalidated request data from C15, not Reader execution.
  (let [fields (system-fields (native-schema-rejection))
        raw (invoke-c15 'build-c15-diagnostic fields) before (pr-str raw)
        path (:path @c15-source)
        expected (-> raw (dissoc :diagnostic-id-request)
                     (assoc :diagnostic-id (bootstrap/sh03-reader-canonical-digest
                                             path (:diagnostic-id-request raw))))]
    (is (= expected (bootstrap/sh03-reader-resolve-diagnostic-id path raw)))
    (is (= before (pr-str raw)))
    (with-redefs [bootstrap/sh03-reader-canonical-digest (fn [p request] [p request])]
      (is (= [path (:diagnostic-id-request raw)]
             (:diagnostic-id (bootstrap/sh03-reader-resolve-diagnostic-id path raw)))))
    (doseq [request [nil :not-a-request "request"]]
      (let [value (assoc raw :diagnostic-id-request request)
            observed (captured-exception-info
                       #(bootstrap/sh03-reader-resolve-diagnostic-id path value))]
        (is (= "C2-HASH" (:id (ex-data observed))))
        (is (= [:sh03-reader-diagnostic-id-request] (:missing-fields (ex-data observed))))))
    (let [calls (atom []) sentinel (ex-info "missing request" {})
          observed (captured-exception-info
                     #(diagnostics/resolve-request-id
                        (fn [& args] (swap! calls conj args))
                        (fn [p value] (is (= path p)) (is (= {} value)) (throw sentinel))
                        path {}))]
      (is (identical? sentinel observed))
      (is (empty? @calls)))))

;; Native fixture data only; no C14 authentication or publication authority.
(defn- native-pipeline-operations []
  {:read-source-form-records (fn [_ _] [{:form '(ns native.diagnostics.pipeline (:profile :hosted))}])
   :validate-ns-syntax! (fn [_ _] nil)
   :parse-module (fn [path _] {:module 'native.diagnostics.pipeline :source-path path
                              :profile :hosted :target :jvm :effects #{} :capabilities #{}
                              :safety :safe :metadata {}})
   :compiler-c14-lowering-source-artifact
   (fn [_ _] {:kind :gravity/stage0-c14-target-lowering-artifact :task "P06-D093"
              :artifact-id "fixture:unbound-native-c14"
              :target-artifact-manifest {:artifact :gravity/target-artifact-manifest}
              :capability-based-proof {:status :complete}})})

(deftest native-file-invoke-is-captured-before-source-reading
  (let [calls (atom []) marker (Object.)
        original (fn [key _ & values] (swap! calls conj [key values]) marker)]
    (with-redefs [c15-ops/invoke original
                  clojure.core/slurp
                  (fn [path]
                    (is (= "native-port.qst" path))
                    (alter-var-root #'c15-ops/invoke
                                    (constantly (fn [& _] (throw (AssertionError. "Late invoke root")))))
                    "native source text")]
      (is (identical? marker (artifact/file-artifact {} "native-port.qst"))))
    (is (= [[:compiler-c15-diagnostics-source-artifact ["native-port.qst" "native source text"]]] @calls))))

(deftest native-override-failure-root-precedes-name-evaluation
  (let [calls (atom []) marker (Object.)
        original (fn [& args] (swap! calls conj args) marker)
        named (reify clojure.lang.Named
                (getName [_]
                  (alter-var-root #'validation/fail!
                                  (constantly (fn [& _] (throw (AssertionError. "Late failure root")))))
                  "native-name")
                (getNamespace [_] nil))
        configuration {:compiler-verification-override-diagnostics {named ["C15-SCHEMA" :native-subject]}
                       :c15-diagnostics-diagnostic-ids ["C15-SCHEMA"]}]
    (with-redefs [validation/fail! original]
      (is (identical? marker (validation/validate-source-overrides! configuration "native-port.gravity" {:fail named}))))
    (is (= 1 (count @calls)))
    (is (= [configuration "C15-SCHEMA" "native-port.gravity"] (vec (take 3 (first @calls)))))
    (is (= "c15-invalid-native-name" (:diagnostic-id (nth (first @calls) 3))))))

(deftest native-fixture-record-root-precedes-span-arguments
  ;; These minimal records observe trusted seed callback timing only.
  (let [record-var (ns-resolve 'gravity.c15-diagnostics.artifact 'diagnostic-record)
        build (var-get (ns-resolve 'gravity.c15-diagnostics.artifact 'diagnostics))
        events (atom [])
        after (fn [rule & _] (swap! events conj [:after rule]) {:ordering-key [rule] :rule rule})
        before (fn [rule & _] (swap! events conj [:before rule]) {:ordering-key [rule] :rule rule})
        result (with-redefs-fn
                 {record-var before
                  #'records/source-span
                  (fn [& _] (alter-var-root record-var (constantly after)) {:source "native-span"})}
                 #(build "native-port.qst" "native-parent" {}))]
    (is (= [[:before "C15-FACTS"] [:before "C15-ORIGIN"]
            [:after "C15-REDACTION"] [:after "C15-GOLDEN"]] @events))
    (is (= ["C15-FACTS" "C15-GOLDEN" "C15-ORIGIN" "C15-REDACTION"] (mapv :rule result)))))

(deftest native-source-assembly-uses-validation-rebound-during-projection
  (let [events (atom []) base {:native :base} proof {:native :proof}
        after (fn [_ path value]
                (is (= "native-port.gravity" path)) (is (identical? base value))
                (swap! events conj :late-validation))
        result
        (with-redefs [projection/artifact-base
                      (fn [& _] (alter-var-root #'validation/validate! (constantly after))
                        (swap! events conj :projection) base)
                      validation/validate!
                      (fn [& _] (throw (AssertionError. "Early validation root")))]
          (c15/with-operations
            (assoc (native-pipeline-operations) :c15-diagnostics-capability-proof (constantly proof)
                               :c4-artifact-id (constantly "native-result"))
            #(c15/compiler-c15-diagnostics-source-artifact "native-port.gravity" "source")))]
    (is (= [:projection :late-validation] @events))
    (is (= {:native :base :capability-based-proof proof :artifact-id "native-result"} result))))

(deftest native-capability-projection-keeps-later-predicates-live
  (let [value (c15/with-operations (native-pipeline-operations)
                #(c15/compiler-c15-diagnostics-source-artifact "native-port.gravity" "source"))
        identity-operation records/stable-id context-operation validation/context-valid?
        primary-operation validation/primary-valid? events (atom [])
        after-primary (fn [record] (swap! events conj :primary) (primary-operation record))
        after-context (fn [record]
                        (swap! events conj :context)
                        (alter-var-root #'validation/primary-valid? (constantly after-primary))
                        (context-operation record))
        result
        (with-redefs [records/stable-id
                      (fn [record] (swap! events conj :identity)
                        (alter-var-root #'validation/context-valid? (constantly after-context))
                        (identity-operation record))
                      validation/context-valid? (fn [& _] (throw (AssertionError. "Early context root")))
                      validation/primary-valid? (fn [& _] (throw (AssertionError. "Early primary root")))]
          (c15/c15-diagnostics-capability-proof value))]
    (is (= (vec (concat (repeat 4 :identity) (repeat 4 :context) (repeat 4 :primary))) @events))
    (is (= :complete (:status result)))))

(deftest native-failure-captures-nested-invoke-and-preserves-exceptions
  (let [original c15-ops/invoke span {:source "native-fallback"}
        observed
        (with-redefs [c15-ops/invoke original]
          (try
            (c15/with-operations
              {:source-span
               (fn [& _]
                 (alter-var-root #'c15-ops/invoke
                                 (constantly (fn [& _] (throw (AssertionError. "Late nested invoke root")))))
                 span)}
              #(c15/c15-diagnostics-fail! "C15-SCHEMA" "native-port.qst" {:source-span false} {:stage nil}))
            nil (catch clojure.lang.ExceptionInfo e e)))]
    (is (= "C15-SCHEMA" (:id (ex-data observed))))
    (is (identical? span (:source-span (ex-data observed))))
    (is (contains? (ex-data observed) :stage))
    (is (nil? (:stage (ex-data observed))))
    (let [error (ex-info "original callback failure" {})
          caught (try
                   (c15/with-operations {:fail! (fn [& _] (throw error))}
                     #(c15/c15-diagnostics-fail! "C15-SCHEMA" "native-port.qst" {} {}))
                   nil (catch clojure.lang.ExceptionInfo e e))]
      (is (identical? error caught)))))
