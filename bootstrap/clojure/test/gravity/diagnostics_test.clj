(ns gravity.diagnostics-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c15-diagnostics :as c15]
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
             'resolve-request-id 'bind-private-record 'verify-private-record}
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
               'resolve-request-id 'bind-private-record 'verify-private-record}
             (set (keys (:public-api contract)))))
      (is (= [:rule-id :human-message :structured-data :explicit-span-and-identity-operations
              :native-seed-diagnostic-fields :source-invocation :canonical-request-digest
              :original-eleven-c15-fields :private-candidate :complete-digest-request]
             (:artifact-inputs contract)))
      (is (= [:stage0-exception-info-carrier :native-seed-diagnostic-record :legacy-diagnostic-id
              :resolved-request-data :private-c15-binding :source-owned-rejection]
             (:artifact-outputs contract)))
      (is (= ['clojure.core]
             (get-in contract [:dependency-direction :requires])))
      (is (= ['gravity.bootstrap 'gravity.c15-diagnostics 'gravity.cli 'gravity.release]
             (get-in contract [:dependency-direction :forbids])))
      (is (some #{:c15-schema}
                (get-in contract [:diagnostic-ownership :does-not-own])))
      (is (true? (:bootstrap-hosted? contract)))
      (is (true? (:clojure-seed-boundary? contract)))
      (is (false? (:self-hosted? contract))))))

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
