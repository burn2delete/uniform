(ns gravity.self-hosting.sh08-function-call-type-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_function_call_type_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-08 function/call test source is not on the classpath"
        {:id "SH08-FUNCTION-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH08-FUNCTION-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
(def ^:private c7-source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- compile-plan
  []
  (let [source-path (path c7-source-relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c7-plan (delay (compile-plan)))

(defn- invoke-c7
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-function-call-type-leaf
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- fixture-artifact
  [family basename extension]
  (bootstrap/sh07-core-file-artifact
   (fixture-path family basename extension)))

(defn- canonical-core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(def ^:private verification-reports (atom {}))

(defn- b47-verification
  [artifact]
  (or (get @verification-reports artifact)
      (let [verifier
            (or (ns-resolve 'gravity.bootstrap
                            'sh07-core-artifact-verification)
                (throw
                 (ex-info
                  "Required SH-07-B47 verification is absent"
                  {:id "SH08-B47-VERIFICATION-ABSENT"})))
            report (verifier artifact)]
        (swap! verification-reports assoc artifact report)
        report)))

(defn- sh08-verification-preimage
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:domain :gravity/sh08-b47-verification-binding-v1
     :verified-artifact-id (:artifact-id artifact)
     :opaque-provenance-binding-id
     (:provenance-binding-id core)
     :authenticated-wrapper artifact
     :canonical-core-artifact core
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-identity-preimage (:identity-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :verification-report report}))

(defn- coordinator-verification
  [artifact report]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)
        source-path (get-in artifact [:provenance :source-path])
        extension (if (.endsWith source-path ".qst") ".qst" ".gravity")
        preimage (sh08-verification-preimage artifact report)
        digest (bootstrap/p15-s23-c11-mir-digest preimage)]
    {:artifact :gravity/sh07-b47-coordinator-verification-v16
     :schema-version 16
     :boundary :clojure-coordinator-verifier
     :verified-artifact-id (:artifact-id artifact)
     :verified-identity-input
     (bootstrap/sh07-core-artifact-identity-input artifact)
     :verified-source-path source-path
     :verified-source-extension extension
     :report report
     :check-catalog (set (keys (:checks report)))
     :opaque-provenance-binding-id
     (:provenance-binding-id core)
     :provenance-binding-preimage (:provenance-binding-preimage core)
     :authenticated-envelope-descriptor
     (:authenticated-envelope-descriptor boundary)
     :authenticated-envelope (:authenticated-envelope boundary)
     :verification-digest-resolution
     {:ordinal 0
      :purpose :sh08-b47-verification-binding
      :preimage preimage
      :digest digest}}))

(defn- b47-context
  [artifact report coordinator]
  (let [boundary (:gravity-core-boundary artifact)
        core (:canonical-core-artifact boundary)]
    {:artifact-id (:artifact-id artifact)
     :artifact-status (:status artifact)
     :artifact-kind (:kind artifact)
     :input-domain :gravity/sh07-b47-canonical-core-v16
     :identity-input (:identity-preimage core)
     :authenticated-core-request (:authenticated-core-request boundary)
     :canonical-core-artifact core
     :provenance (:provenance artifact)
     :lineage (get-in boundary [:authenticated-core-request :lineage])
     :authenticated-wrapper artifact
     :verification
     {:status (:status report)
      :checks (:checks report)
      :failed-checks (:failed-checks report)
      :receipt-context :gravity/sh07-b47-verification-v16
      :opaque-provenance-binding-id
      (:opaque-provenance-binding-id coordinator)
      :verification-digest
      (get-in coordinator [:verification-digest-resolution :digest])}}))

(def ^:dynamic ^:private *function-request-cache*
  ;; These verified request bundles retain the complete authenticated wrapper,
  ;; canonical core, and verification report.  Rebuilding one repeatedly also
  ;; canonical-hashes that large carrier again.  Cache by object identity so a
  ;; deliberately rewritten artifact used by a negative test can never inherit
  ;; a request from an equal-looking but distinct carrier.
  (java.util.IdentityHashMap.))

(defn- function-request
  [artifact]
  (locking *function-request-cache*
    (if (.containsKey *function-request-cache* artifact)
      (.get *function-request-cache* artifact)
      (let [report (b47-verification artifact)
            coordinator (coordinator-verification artifact report)
            context (b47-context artifact report coordinator)
            request
            {:canonical-core-artifact (:canonical-core-artifact context)
             :b47-context context
             :coordinator-verification coordinator}]
        ;; Failed receipts are diagnostic inputs, not reusable verification
        ;; evidence.  Rebuild them on every call so mutation-focused tests keep
        ;; exercising the verifier rather than a cached failure carrier.
        (when (= :passed (:status report))
          (.put *function-request-cache* artifact request))
        request))))

(deftest sh08-imported-function-types-development-kernel
  ;; Normalized DATA exercises Gravity's joins and inference, not B47 authority.
  (let [sha #(str "sha256:" (format "%064x" %))
        module {:namespace 'example.types :profile :meta :target :jvm :exports '[identity-i64]
                :effects [:io/write] :capabilities [:io/stdout]}
        original {:binding-id (sha 1) :name 'identity-i64 :namespace 'example.types :kind :function
                  :binding-class :namespace :visibility :public :definition-artifact-id (sha 2)
                  :definition-syntax-id (sha 3) :package {:name 'example.types}
                  :profile-set [:meta] :target-set [:jvm] :type-ref :gravity.type/function
                  :effects [:io/write] :capabilities [:io/stdout] :safety :safe}
        origin {:ordinary-module-artifact-id (sha 4) :origin-analysis-id (sha 5) :origin-binding-id (sha 1)
                :origin-definition-artifact-id (sha 2) :origin-definition-syntax-id (sha 3)
                :origin-definition (select-keys original [:name :kind :namespace :package :visibility :profile-set :target-set :type-ref :effects :capabilities :safety])}
        c5 {:artifact-id (sha 4) :binding-table {:bindings [original]}
            :gravity-resolution-boundary {:resolved-analysis {:artifact-id (sha 5) :module-contract module}}}
        caller-binding (assoc original :binding-id (sha 6) :upstream-binding-id (sha 7) :binding-class :import :semantic-span origin)
        caller {:canonical-core-artifact {:module module}
                :b47-context {:authenticated-wrapper {:sh06-resolution-artifact
                              {:binding-table {:bindings [(assoc caller-binding :binding-id (sha 7))]}
                               :gravity-resolution-boundary {:module-inputs {:module-artifacts [c5]}}}}}}
        declaration {:type-id :gravity.type/i64}
        record {:definition-binding-id (sha 8) :definition-name 'identity-i64 :function-syntax-id (sha 9)
                :function-core-node-id (sha 18) :body-core-node-id (sha 19) :parameter-binding-ids [(sha 11)] :fixed-arity 1
                :signature {:parameters [declaration] :return declaration}}
        producer {:b47-context {:artifact-id (sha 10) :authenticated-wrapper {:sh06-resolution-artifact c5}
                                :authenticated-core-request {:lineage {:authenticated-sh06-artifact-id (sha 4)}
                                                             :binding-table [{:binding-id (sha 8) :upstream-binding-id (sha 1)}]}}
                  :canonical-core-artifact {:function-records [record]}}
        function {:artifact :gravity/c7-function-type :function-id (sha 9) :definition-name 'identity-i64
                  :function-core-node-id (sha 18) :body-core-node-id (sha 19) :parameter-binding-ids [(sha 11)] :signature (:signature record)
                  :parameters [{:binding-id (sha 11) :type :gravity.type/i64}]
                  :return :gravity.type/i64 :fixed-arity 1 :latent-effects [:io/write] :capabilities [:io/stdout]
                  :thrown-error-effects [:pending-sh09] :status :inferred}
        typed {:function-type-table [function] :identity-input {:derived-producer :kernel}}
        join #(invoke-c7 'sh08-im-origin [caller %1 %2 %3])
        imported (join caller-binding producer typed)
        context {:imported-functions {(sha 6) imported}}
        call {:core-node-id (sha 12) :argument-node-ids [(sha 13)] :evaluation-order :operator-then-arguments}
        edge {:call-core-node-id (sha 12) :callee-binding-id (sha 6) :classification :nonlocal-or-nonfunction}
        constrained #(invoke-c7 'sh08-ft-call-constraints-with-context
                       [[call] [edge] {} {(sha 13) %} {(sha 99) :gravity.type/string} {} context])]
    (is (= :accepted (:status imported)))
    (is (= [:io/write] (get-in imported [:function-type :latent-effects])))
    (is (= [:io/stdout] (get-in imported [:function-type :capabilities])))
    (is (= [:pending-sh09] (get-in imported [:function-type :thrown-error-effects])))
    (is (= :pending-sh09-imported-body (:effect-status imported)))
    (is (= :pending-imported-effects (:legality-status imported)))
    (is (= [] (:diagnostics (constrained :gravity.type/i64))))
    (is (= {(sha 99) :gravity.type/string} (:binding-types (constrained :gravity.type/i64))))
    (doseq [actual [:gravity.type/string :gravity.type/bool :gravity.type/integer :gravity.type/u64]]
      (let [diagnostic (first (:diagnostics (constrained actual)))]
        (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
        (is (= [:gravity.type/i64 actual] ((juxt :expected :actual) diagnostic)))))
    (is (false? (get-in (constrained :gravity.type/unknown) [:constraints 0 :solution :arguments-complete?])))
    (is (= :equality (get-in (constrained :gravity.type/i64) [:constraints 0 :kind])))
    (is (= :sh08-imported-function-call-v1 (get-in (constrained :gravity.type/i64) [:constraints 0 :producer-rule])))
    (is (every? #(contains? (get-in (constrained :gravity.type/i64) [:constraints 0]) %)
                [:dependencies :solution :invalidation :profile :target]))
    (let [fact (first (invoke-c7 'sh08-ft-call-facts-with-context
                        [[call] [edge] {} {(sha 13) :gravity.type/i64} {} (assoc module :effects [] :capabilities []) context]))]
      (is (= [:io/write] (:effects fact)))
      (is (= [:io/stdout] (:capabilities fact)))
      (is (= :callee-module-declarations (:effect-facts-source fact)))
      (is (= (invoke-c7 'sh08-im-reference [imported]) (get-in fact [:fact-id-request :imported-function])))
      (is (not (contains? (:imported-function fact) :producer-identity))))
    (let [other (assoc imported :binding-id (sha 16))
          base {:status :accepted :pending [] :typed-core {} :identity-input {}}
          left (invoke-c7 'sh08-im-result [base (array-map (sha 6) imported (sha 16) other)])
          right (invoke-c7 'sh08-im-result [base (array-map (sha 16) other (sha 6) imported)])]
      (is (= left right))
      (is (= 2 (count (:imported-function-types left))))
      (is (= 1 (count (:module-type-producers left)))))
    (doseq [value [nil 1 true "wrong" {}]]
      (let [checked (invoke-c7 'sh08-im-inputs-check
                      [{:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests value}])]
        (is (= "C7-VERIFY" (:rule checked)))
        (is (= :malformed-module-type-inputs (:reason checked)))))
    (is (= :call-arity-mismatch (get-in (invoke-c7 'sh08-im-call-constraint [call edge [] imported {}]) [:diagnostics 0 :reason])))
    (is (= :gravity.type/i64
           (invoke-c7 'sh08-ft-infer-node-with-context
             [{:node-id (sha 12) :core-form :call} {} {} {} [edge] context])))
    (is (= (:function-type imported)
           (invoke-c7 'sh08-ft-infer-node-with-context
             [{:core-form :reference :attributes {:binding-id (sha 6)}} {}
              (invoke-c7 'sh08-im-seeds [{(sha 6) imported} {}]) {} [] context])))
    (doseq [key [:ordinary-module-artifact-id :origin-analysis-id :origin-binding-id :origin-definition-artifact-id :origin-definition-syntax-id :origin-definition]]
      (is (= "C7-VERIFY" (:rule (join (update caller-binding :semantic-span dissoc key) producer typed)))))
    (is (= "C7-VERIFY" (:rule (join caller-binding (assoc-in producer [:b47-context :authenticated-core-request :lineage :authenticated-sh06-artifact-id] (sha 999)) typed))))
    (is (= "C7-VERIFY" (:rule (join caller-binding (assoc-in producer [:canonical-core-artifact :function-records 0 :signature :parameters] [nil]) typed))))
    (is (= "C7-VERIFY" (:rule (join caller-binding (update-in producer [:canonical-core-artifact :function-records 0 :signature] dissoc :return) typed))))
    (is (= "C7-VERIFY" (:rule (join caller-binding producer (assoc-in typed [:function-type-table 0 :return] :gravity.type/bool)))))))

(deftest sh08-imported-inferred-signatures-development-kernel
  ;; Complete producer facts, never caller observations, define the boundary.
  (let [sha #(str "sha256:" (format "%064x" %))
        record {:function-syntax-id (sha 1) :function-core-node-id (sha 2) :body-core-node-id (sha 3)
                :definition-name 'identity :fixed-arity 1 :parameter-binding-ids [(sha 4)]}
        function {:artifact :gravity/c7-function-type :function-id (sha 1) :function-core-node-id (sha 2) :body-core-node-id (sha 3)
                  :definition-name 'identity :fixed-arity 1 :parameter-binding-ids [(sha 4)]
                  :parameters [{:binding-id (sha 4) :type :gravity.type/string}] :return :gravity.type/string :status :inferred}
        complete #(invoke-c7 'sh08-im-complete-signature? [%1 %2])]
    (doseq [type [:gravity.type/string :gravity.type/bool :gravity.type/integer :gravity.type/i64]]
      (is (true? (complete record (-> function (assoc :return type) (assoc-in [:parameters 0 :type] type))))))
    (is (true? (complete (assoc record :fixed-arity 0 :parameter-binding-ids [])
                        (assoc function :fixed-arity 0 :parameter-binding-ids [] :parameters [] :return :gravity.type/bool))))
    (let [partial (assoc record :signature {:parameters [{:type-id :gravity.type/i64}] :return nil})
          typed (assoc function :signature (:signature partial) :parameters [{:binding-id (sha 4) :type :gravity.type/i64}] :return :gravity.type/i64)]
      (is (true? (complete partial typed)))
      (is (false? (complete partial (assoc-in typed [:parameters 0 :type] :gravity.type/bool)))))
    (doseq [bad [nil :gravity.type/unknown {} {:artifact :gravity/c7-function-type :parameters [] :return :gravity.type/bool}
                 {:kind :gravity.type/vector :element-types [:gravity.type/unknown] :shape :literal}
                 {:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/bool] :shape :literal}]]
      (is (false? (complete record (assoc function :return bad))))
      (is (false? (complete record (assoc-in function [:parameters 0 :type] bad)))))
    (doseq [bad [(assoc function :function-id (sha 99)) (assoc function :function-core-node-id (sha 99))
                 (assoc function :body-core-node-id (sha 99)) (assoc function :status :unresolved)
                 (assoc function :fixed-arity 2) (assoc function :parameter-binding-ids [(sha 99)])
                 (assoc-in function [:parameters 0 :binding-id] (sha 99))]]
      (is (false? (complete record bad))))
    (is (false? (complete (assoc record :parameter-binding-ids [(sha 4) (sha 4)] :fixed-arity 2)
                         (assoc function :fixed-arity 2 :parameter-binding-ids [(sha 4) (sha 4)]
                                         :parameters [(first (:parameters function)) (first (:parameters function))]))))))

(deftest sh08-imported-work-boundaries-development-kernel
  ;; Invocation-local memo/limit policy DATA. Public C7 always starts empty.
  (let [bounds (invoke-c7 'sh08-local-bounds-value [])
        id "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        module-id "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        c5 {:artifact-id module-id}
        request {:b47-context {:artifact-id id :authenticated-wrapper {:sh06-resolution-artifact c5}}}
        usage {:status :accepted :nodes 1 :scalar-units 1}
        memo {id {:request request :result {:status :accepted} :usage usage}}
        check #(invoke-c7 'sh08-im-work-check [%1 %2 %3])]
    (is (= :accepted (:status (check {} {} usage))))
    (is (= :module-type-distinct-producer-limit
           (:reason (check (zipmap (range (:maximum-root-nodes bounds)) (repeat {:usage usage})) {} usage))))
    (is (= :module-type-dependency-depth-limit
           (:reason (check {} (zipmap (range (:maximum-carrier-depth bounds)) (repeat usage)) usage))))
    (is (= :module-type-total-core-work-limit
           (:reason (check {id {:usage (assoc usage :nodes (:maximum-carrier-nodes bounds))}} {} usage))))
    (is (= :module-type-total-scalar-work-limit
           (:reason (check {id {:usage (assoc usage :scalar-units (:maximum-scalar-units bounds))}} {} usage))))
    (is (= :module-type-producer-core-bound (:reason (check {} {} {:status :rejected :reason :carrier-width-bound}))))
    (is (= {:status :accepted} (:result (invoke-c7 'sh08-im-derive [request memo {}]))))
    (is (= :substituted-module-type-request
           (get-in (invoke-c7 'sh08-im-derive [(assoc request :extra true) memo {}]) [:result :diagnostics 0 :reason])))
    (is (= :cyclic-module-type-request
           (get-in (invoke-c7 'sh08-im-derive [request {} {id usage}]) [:result :diagnostics 0 :reason])))
    (is (= :undeclared-module-type-producer (:reason (invoke-c7 'sh08-im-producers [[request] memo {} []]))))
    (is (= :undeclared-module-type-producer
           (:reason (invoke-c7 'sh08-im-producers [[request] memo {} [(assoc c5 :forged true)]]))))
    (is (= :duplicate-or-invalid-module-type-producer
           (:reason (invoke-c7 'sh08-im-producers [[request request] memo {} [c5]]))))
    (is (= :module-type-input-count-limit
           (:reason (invoke-c7 'sh08-im-inputs-check
                      [{:artifact :gravity/sh08-module-type-inputs :schema-version 1
                        :module-requests (vec (repeat (inc (:maximum-root-nodes bounds)) request))}]))))
    (let [duplicated-proof (String. (char-array (inc (:maximum-scalar-units bounds)) \x))
          supplied {:artifact :gravity/sh08-module-type-inputs :schema-version 1
                    :module-requests [(assoc request :coordinator-verification {:raw-proof duplicated-proof})]}]
      ;; Container validation does not re-flatten proof bodies. This malformed
      ;; producer still rejects through the unchanged full authentication path.
      (is (= :rejected (:status (invoke-c7 'sh08-carrier-preflight [supplied]))))
      (is (= :accepted (:status (invoke-c7 'sh08-im-inputs-check [supplied]))))
      (is (= :rejected (get-in (invoke-c7 'sh08-im-producers [(:module-requests supplied) {} {} [c5]]) [:status]))))))

(deftest sh08-atomic-signature-inference-development-kernel
  ;; Execute the current Gravity inference algorithm with normalized records;
  ;; this does not substitute for authenticated B47/C7 composition.
  (let [sha #(str "sha256:" (format "%064x" %))
        declaration {:source-child-index 1 :marker-form-id (sha 1) :marker-syntax-id (sha 2)
                     :type-form-id (sha 3) :type-syntax-id (sha 4)
                     :type-symbol 'I64 :type-binding-id (sha 5) :type-id :gravity.type/i64}
        signature {:rule-version :sh07-atomic-function-signature-v1
                   :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters [declaration] :return (assoc declaration :source-child-index 2)
                   :parameter-source-child-indexes [0] :body-source-child-index 4}
        record {:function-syntax-id (sha 10) :function-core-node-id (sha 11)
                :body-core-node-id (sha 12) :parameter-binding-ids [(sha 13)] :fixed-arity 1 :signature signature}
        bindings {(sha 5) {:binding-id (sha 5) :kind :type :binding-class :core :namespace 'gravity.core :name 'I64}}
        module {:profile :meta :target :jvm :effects #{} :capabilities #{}}
        functions (invoke-c7 'sh08-ft-function-skeletons [[record] module])
        seeds (invoke-c7 'sh08-ft-declaration-seeds [functions {}])
        body {:node-id (sha 12) :core-form :reference :children [] :attributes {:binding-id (sha 13)}}
        function {:node-id (sha 11) :core-form :fn :children [(sha 12)] :source {:syntax-id (sha 10)}}
        infer (fn [body-node]
                (invoke-c7 'sh08-ft-infer-acyclic
                  [[body-node function] {(sha 12) body-node (sha 11) function} [] [] [] [] functions seeds 8]))
        result (infer body)
        mismatch (infer (assoc body :core-form :literal :attributes {:literal-kind :string}))]
    (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 1 bindings])))
    (is (= {(sha 13) :gravity.type/i64} seeds))
    (is (= :converged (:convergence-status result)))
    (is (= :gravity.type/i64 (get-in result [:type-table (sha 12)])))
    (is (= :gravity.type/i64 (get-in result [:function-types (sha 10) :return])))
    (is (= :type-conflict (:convergence-status mismatch)))
    (is (= [:gravity.type/i64 :gravity.type/string]
           ((juxt :expected :actual) (first (:diagnostics mismatch)))))
    (doseq [actual [:gravity.type/string :gravity.type/integer :gravity.type/u64 :gravity.type/f32 :gravity.type/f64]]
      (let [call {:core-node-id (sha 20) :argument-node-ids [(sha 21)]}
            edge {:classification :local-function :callee-function-syntax-id (sha 10)}
            constrained (invoke-c7 'sh08-ft-call-constraints
                          [[call] [edge] {} {(sha 21) actual} seeds functions])]
        (is (= :call-argument-type-mismatch (:reason (first (:diagnostics constrained)))))
        (is (= [:gravity.type/i64 actual] ((juxt :expected :actual) (first (:diagnostics constrained)))))))
    (doseq [bad [(assoc signature :rule-version nil) (assoc signature :extra true)
                 (assoc-in signature [:parameters 0 :type-id] :gravity.type/integer)
                 (assoc-in signature [:parameters 0 :type-binding-id] (sha 999))]]
      (is (false? (invoke-c7 'sh08-ft-signature-valid? [bad 1 bindings]))))
    (is (false? (invoke-c7 'sh08-ft-signature-valid? [signature 1 (assoc-in bindings [(sha 5) :kind] :var)])))
    (doseq [bad [0 nil {} []]]
      (is (false? (invoke-c7 'sh08-ft-function-signature-valid?
                            [record (assoc function :attributes {:parameters bad :fixed-arity 1}) bindings]))))
    (is (false? (invoke-c7 'sh08-ft-function-signature-valid?
                          [(dissoc record :signature)
                           (assoc function :attributes {:parameters '[x] :fixed-arity 1}
                                           :lowering-rule-version :sh07-atomic-function-signature-v1) bindings])))
    (let [boolean-declaration (assoc declaration :source-child-index 4 :type-id :gravity.type/bool
                                    :type-symbol 'Bool :type-binding-id (sha 6)
                                    :marker-form-id (sha 31) :marker-syntax-id (sha 32)
                                    :type-form-id (sha 33) :type-syntax-id (sha 34))
          mixed (assoc signature :parameters [declaration boolean-declaration nil]
                                 :return nil :parameter-source-child-indexes [0 3 6] :body-source-child-index 2)
          mixed-record (assoc record :signature mixed :fixed-arity 3 :parameter-binding-ids [(sha 13) (sha 14) (sha 15)])
          mixed-functions (invoke-c7 'sh08-ft-function-skeletons [[mixed-record] module])
          mixed-bindings (assoc bindings (sha 6) {:binding-id (sha 6) :name 'Bool :kind :type
                                                :binding-class :core :namespace 'gravity.core})
          mixed-seeds (invoke-c7 'sh08-ft-declaration-seeds [mixed-functions {(sha 15) :gravity.type/string}])]
      (is (true? (invoke-c7 'sh08-ft-signature-valid? [mixed 3 mixed-bindings])))
      (is (= {(sha 13) :gravity.type/i64 (sha 14) :gravity.type/bool (sha 15) :gravity.type/string} mixed-seeds))
      (is (= [0 3 6] (get-in mixed-functions [(sha 10) :signature :parameter-source-child-indexes]))))
    (doseq [proof-kind [:higher-order :recursive] compatible? [true false]]
      (let [boolean-declaration (assoc declaration :type-id :gravity.type/bool :type-symbol 'Bool :type-binding-id (sha 6))
            declared (assoc signature :parameters [boolean-declaration]
                                      :return (assoc (if compatible? boolean-declaration declaration) :source-child-index 2))
            base (invoke-c7 'sh08-ft-function-skeletons [[(assoc record :signature declared)] module])
            proof-type (assoc (get base (sha 10)) :return :gravity.type/bool
                              :parameters [{:binding-id (sha 13) :type :gravity.type/bool}])
            higher-order {:status :accepted :identity {:record record} :identity-function-type proof-type}
            recursive {:status :accepted :function-syntax-id (sha 10) :function-record record
                       :parameter-types [:gravity.type/bool] :base-type :gravity.type/bool :function-node function}
            completed (invoke-c7 'sh08-ft-complete-function-skeletons
                                [base (if (= proof-kind :recursive) recursive {})
                                      (if (= proof-kind :higher-order) higher-order {})])
            completed-seeds (invoke-c7 'sh08-ft-declaration-seeds [completed {}])
            checked (invoke-c7 'sh08-ft-infer-acyclic
                              [[body function] {(sha 12) body (sha 11) function} [] [] [] [] completed completed-seeds 8])]
        (is (= (if compatible? :gravity.type/bool :gravity.type/i64) (get-in completed [(sha 10) :return])))
        (is (= (if compatible? :converged :type-conflict) (:convergence-status checked)))))
    (let [names '[I8 I16 I32 I64 U8 U16 U32 U64 F32 F64 Bool String Symbol Keyword Ratio]
          types (mapv #(invoke-c7 'sh08-ft-atomic-type-id [%]) names)]
      (is (= (count names) (count (set types))))
      (is (= [:gravity.type/f32 :gravity.type/f64] (subvec types 8 10)))
      (is (not (contains? (set types) :gravity.type/integer))))))

(def ^:private accepted-gravity
  (delay (fixture-artifact "accepted" "function-local-call" ".gravity")))
(def ^:private accepted-qst
  (delay (fixture-artifact "accepted" "function-local-call" ".qst")))
(def ^:private accepted-two-gravity
  (delay (fixture-artifact "accepted" "function-two-hop-call" ".gravity")))
(def ^:private accepted-two-qst
  (delay (fixture-artifact "accepted" "function-two-hop-call" ".qst")))
(def ^:private rejected-gravity
  (delay (fixture-artifact "rejected" "function-call-arity" ".gravity")))
(def ^:private rejected-qst
  (delay (fixture-artifact "rejected" "function-call-arity" ".qst")))
(def ^:private rejected-type-gravity
  (delay
    (fixture-artifact "rejected" "function-call-type-mismatch" ".gravity")))
(def ^:private rejected-type-qst
  (delay
    (fixture-artifact "rejected" "function-call-type-mismatch" ".qst")))
(def ^:private accepted-nonlocal-gravity
  (delay (fixture-artifact "accepted" "function-call-nonlocal" ".gravity")))
(def ^:private accepted-nonlocal-qst
  (delay (fixture-artifact "accepted" "function-call-nonlocal" ".qst")))
(def ^:private recursive-b47-gravity
  (delay
    (bootstrap/sh07-core-file-artifact
     (path
      (str "bootstrap/clojure/fixtures/self-hosting/sh-07/"
           "b47-function-call-recursion/accepted/"
           "function-call-recursion.gravity")))))

(defn- function-result
  [artifact]
  (invoke-c7 'sh08-function-type-core-artifact
             [(function-request artifact)]))

(defn- verification-result
  [artifact candidate]
  (invoke-c7 'sh08-verify-function-type-result
             [(function-request artifact) candidate]))

(defn- result-function
  [result]
  (first (:function-type-table result)))

(defn- result-call
  [result]
  (first (:call-type-facts result)))

(defn- result-local
  [result]
  (first (:local-binding-facts result)))

(defn- first-difference
  [expected actual]
  (cond
    (and (vector? expected) (vector? actual))
    (let [shared-count (min (count expected) (count actual))
          index
          (first
           (filter #(not= (get expected %) (get actual %))
                   (range shared-count)))]
      (cond
        (some? index)
        {:kind :index :location index
         :expected (get expected index) :actual (get actual index)}

        (not= (count expected) (count actual))
        {:kind :count :location shared-count
         :expected-count (count expected) :actual-count (count actual)}

        :else nil))

    (and (map? expected) (map? actual))
    (let [key-name
          (first
           (filter #(not= (get expected %) (get actual %))
                   (sort-by pr-str
                            (set (concat (keys expected)
                                         (keys actual))))))]
      (when (some? key-name)
        {:kind :key :location key-name
         :expected (get expected key-name)
         :actual (get actual key-name)}))

    (= expected actual) nil

    :else
    {:kind :value :expected expected :actual actual}))

(defn- sha-id
  [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(defn- synthetic-id
  [family index]
  (keyword "sh08-synthetic" (str (name family) "-" index)))

(defn- synthetic-chain-input
  [function-count]
  (let [function-indexes (range function-count)
        function-id #(synthetic-id :function %)
        function-node-id #(synthetic-id :function-node %)
        parameter-id #(synthetic-id :parameter %)
        definition-id #(synthetic-id :definition %)
        definition-binding-id #(synthetic-id :definition-binding %)
        body-id
        (fn [index]
          (if (zero? index)
            (synthetic-id :leaf-reference index)
            (synthetic-id :call index)))
        source (fn [syntax-id] {:syntax-id syntax-id :origin-chain []})
        segments
        (mapv
         (fn [index]
           (let [parameter-reference-id
                 (synthetic-id :parameter-reference index)
                 operator-id (synthetic-id :operator index)
                 call-id (body-id index)
                 fn-id (function-id index)
                 fn-node-id (function-node-id index)
                 parameter (parameter-id index)
                 definition-binding (definition-binding-id index)]
             (if (zero? index)
               {:nodes
                [{:node-id call-id
                  :core-form :reference
                  :children []
                  :attributes {:binding-id parameter}
                  :source (source call-id)}
                 {:node-id fn-node-id
                  :core-form :fn
                  :children [call-id]
                  :attributes {}
                  :source (source fn-id)}
                 {:node-id (definition-id index)
                  :core-form :def
                  :children [fn-node-id]
                  :attributes {}
                  :source (source (definition-id index))}]
                :calls []
                :edges []}
               {:nodes
                [{:node-id operator-id
                  :core-form :reference
                  :children []
                  :attributes
                  {:binding-id (definition-binding-id (dec index))}
                  :source (source operator-id)}
                 {:node-id parameter-reference-id
                  :core-form :reference
                  :children []
                  :attributes {:binding-id parameter}
                  :source (source parameter-reference-id)}
                 {:node-id call-id
                  :core-form :call
                  :children [operator-id parameter-reference-id]
                  :attributes {}
                  :source (source call-id)}
                 {:node-id fn-node-id
                  :core-form :fn
                  :children [call-id]
                  :attributes {}
                  :source (source fn-id)}
                 {:node-id (definition-id index)
                  :core-form :def
                  :children [fn-node-id]
                  :attributes {}
                  :source (source (definition-id index))}]
                :calls
                [{:core-node-id call-id
                  :argument-node-ids [parameter-reference-id]}]
                :edges
                [{:call-core-node-id call-id
                  :classification :local-function
                  :callee-function-syntax-id (function-id (dec index))}]})))
         function-indexes)
        top-operator-id (synthetic-id :top-operator function-count)
        top-argument-id (synthetic-id :top-argument function-count)
        top-call-id (synthetic-id :top-call function-count)
        top-definition-id (synthetic-id :top-definition function-count)
        top-segment
        {:nodes
         [{:node-id top-operator-id
           :core-form :reference
           :children []
           :attributes
           {:binding-id (definition-binding-id (dec function-count))}
           :source (source top-operator-id)}
          {:node-id top-argument-id
           :core-form :literal
           :children []
           :attributes {:literal-kind :integer}
           :source (source top-argument-id)}
          {:node-id top-call-id
           :core-form :call
           :children [top-operator-id top-argument-id]
           :attributes {}
           :source (source top-call-id)}
          {:node-id top-definition-id
           :core-form :def
           :children [top-call-id]
           :attributes {}
           :source (source top-definition-id)}]
         :calls
         [{:core-node-id top-call-id
           :argument-node-ids [top-argument-id]}]
         :edges
         [{:call-core-node-id top-call-id
           :classification :local-function
           :callee-function-syntax-id
           (function-id (dec function-count))}]}
        nodes (vec (mapcat :nodes (conj segments top-segment)))
        calls (vec (mapcat :calls (conj segments top-segment)))
        edges (vec (mapcat :edges (conj segments top-segment)))
        function-records
        (mapv
         (fn [index]
           {:function-syntax-id (function-id index)
            :function-core-node-id (function-node-id index)
            :body-core-node-id (body-id index)
            :parameter-binding-ids [(parameter-id index)]})
         function-indexes)
        definitions
        (conj
         (mapv
          (fn [index]
            {:binding-id (definition-binding-id index)
             :value-node-id (function-node-id index)})
          function-indexes)
         {:binding-id (synthetic-id :result-binding function-count)
          :value-node-id top-call-id})
        function-types
        (into
         {}
         (map
          (fn [index]
            [(function-id index)
             {:function-id (function-id index)
              :function-core-node-id (function-node-id index)
              :body-core-node-id (body-id index)
              :parameter-binding-ids [(parameter-id index)]
              :parameters
              [{:binding-id (parameter-id index)
                :type :gravity.type/unknown}]
              :return :gravity.type/unknown
              :status :inferred}])
          function-indexes))]
    {:nodes nodes
     :node-table (into {} (map (juxt :node-id identity) nodes))
     :function-records function-records
     :definitions definitions
     :calls calls
     :edges edges
     :function-types function-types
     :call-node-ids (mapv :core-node-id calls)}))

(defn- structural-match-input []
  (let [sha #(str "sha256:" (format "%064x" %))
        node (fn [i kind attrs children]
               {:node-id (sha i) :core-form kind :attributes attrs :children (mapv sha children)
                :resolved-binding-ids (if-let [b (:binding-id attrs)] [b] [])
                :source {:form-id (sha i) :syntax-id (sha i) :semantic-span {:byte-start i :byte-end (inc i)}
                         :origin-chain [] :generated-origin nil}})
        sequence [(node 100 :reference {:binding-id (sha 1000)} [])
                  (node 101 :reference {:binding-id (sha 1001)} [])
                  (node 102 :literal {:literal-kind :keyword :value :value} [])
                  (node 103 :collection-literal {:literal-kind :map :element-count 2 :source-value :ignored-syntax} [102 101])
                  (node 104 :collection-literal {:literal-kind :vector :element-count 2 :source-value :ignored-syntax} [100 103])
                  (node 105 :literal {:literal-kind :keyword :value :payload} [])
                  (node 106 :literal {:literal-kind :keyword :value :extra} [])
                  (node 107 :literal {:literal-kind :keyword :value :retained} [])
                  (node 108 :collection-literal {:literal-kind :map :element-count 4 :source-value :ignored-syntax} [106 107 105 104])
                  (node 109 :reference {:binding-id (sha 2000)} [])
                  (assoc (node 110 :let {:initializer-child-indexes [0] :binding-count 1 :body-count 1
                                        :binding-vector-form-id (sha 113) :binding-vector-syntax-id (sha 113)
                                        :body-child-indexes [1] :result-policy {:kind :last-body :child-index 1}} [108 109])
                         :resolved-binding-ids [(sha 2000)])]
        forms (into {} (map (fn [n]
                              (let [a (:attributes n) k (:core-form n) id (:node-id n)]
                                [id {:form-id id :syntax-id id
                                     :kind (case k :literal (:literal-kind a) :collection-literal (:literal-kind a) :reference :symbol :list)
                                     :value (:value a) :child-form-ids (:children n)
                                     :scope-id (if (= id (sha 108)) (sha 3001) nil)}])) sequence))
        forms (-> forms
                  (assoc (sha 111) {:form-id (sha 111) :syntax-id (sha 111) :kind :symbol :value 'packet :scope-id (sha 3000)})
                  (assoc (sha 113) {:form-id (sha 113) :syntax-id (sha 113) :kind :vector :parent-form-id (sha 110)
                                    :child-form-ids [(sha 111) (sha 108)]})
                  (assoc-in [(sha 110) :child-form-ids] [(sha 112) (sha 113) (sha 109)]))
        lexical {:let-core-node-id (sha 110) :ordinal 0 :name 'packet :binding-id (sha 2000)
                 :definition-form-id (sha 111) :definition-syntax-id (sha 111) :binding-scope-id (sha 3000)
                 :initializer-node-id (sha 108) :initializer-form-id (sha 108) :initializer-syntax-id (sha 108) :initializer-scope-id (sha 3001)
                 :visible-prior-binding-ids [] :mutability :immutable}]
    {:nodes (into {} (map (juxt :node-id identity) sequence)) :sequence sequence :forms forms :lexical [lexical]
     :bindings {(sha 2000) {:binding-class :lexical :binding-id (sha 2000) :definition-syntax-id (sha 111)
                           :scope-id (sha 3000) :name 'packet}}
     :binding-types {(sha 1000) :gravity.type/bool (sha 1001) :gravity.type/i64}
     :root (sha 108) :reference (sha 109) :flag (sha 100) :value (sha 101) :sha sha}))

(defn- quoted-data-input
  ([value] (quoted-data-input value 7000))
  ([value owner-index]
   (let [sha #(str "sha256:" (format "%064x" %))
         counter (atom (+ owner-index 100)) forms (atom {})
         form (fn [id kind value parent children]
                {:form-id id :syntax-id id :kind kind :value value :parent-form-id parent :child-form-ids children
                 :source-span {:byte-start @counter :byte-end (inc @counter)} :origin-chain [] :generated-origin nil})
         owner (sha owner-index) operator (sha (+ 8000 owner-index))
         build (fn build [value parent]
                 (let [id (sha (swap! counter inc))
                       kind (cond (nil? value) :nil (boolean? value) :boolean (integer? value) :integer
                                  (char? value) :character (string? value) :string (keyword? value) :keyword
                                  (symbol? value) :symbol (vector? value) :vector (map? value) :map
                                  (set? value) :set (seq? value) :list :else :unsupported)
                       values (cond (map? value) (vec (mapcat identity value))
                                    (coll? value) (vec value) :else [])
                       ids (mapv #(build % id) values)]
                   (swap! forms assoc id (form id kind value parent ids)) id))
         payload (build value owner)
         owner-form (form owner :list (list 'quote value) nil [operator payload])
         operator-form (form operator :symbol 'quote owner [])
         forms (assoc @forms owner owner-form operator operator-form)
         node {:node-id (sha (+ owner-index 2)) :core-form :quote :children [] :resolved-binding-ids []
               :source {:form-id owner :syntax-id owner :semantic-span (:source-span owner-form) :origin-chain [] :generated-origin nil}
               :evaluation {:kind :no-evaluation :order []}
               :attributes {:quoted-form-id payload :quoted-syntax-id payload
                            :quoted-kind (get-in forms [payload :kind]) :quoted-value value}}]
     {:node node :forms forms :source-forms (vec (vals forms)) :payload payload :owner owner :sha sha})))

(defn- character-quote-input [value]
  (let [input (quoted-data-input (char 65))
        owner (:owner input) payload (:payload input)]
    (-> input (assoc-in [:node :attributes :quoted-value] value)
        (assoc-in [:forms owner :value] (list 'quote value))
        (assoc-in [:forms payload :value] value))))

(deftest sh08-character-literal-and-quote-development-kernel
  (let [scalar #(bootstrap/sh03-reader-host-character-value! "<character-kernel>" {:codepoint %})
        quoted (fn [value] (let [{:keys [node forms]} (character-quote-input value)] (invoke-c7 'sh08-qd-node [node forms])))
        literal (fn [value] {:node-id :character :core-form :literal :children [] :attributes {:literal-kind :character :value value}})]
    (doseq [value [(char 0) (char 65) (char 0xffff) (scalar 0x10000) (scalar 0x1f600) (scalar 0x10ffff)]]
      (let [result (quoted value)]
        (is (true? (invoke-c7 'sh08-character-carrier? [value])))
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :gravity.type/character (get-in result [:data :type])))
        (is (= [] (get-in result [:data :children])))
        (is (= value (get-in result [:data :value])))))
    (doseq [value [(assoc (scalar 0x1f600) :artifact :forged)
                   (assoc (scalar 0x1f600) :extra true)
                   (assoc (scalar 0x1f600) :codepoint 65)
                   (assoc (scalar 0x1f600) :codepoint 0x110000)
                   (assoc (scalar 0x1f600) :text nil)
                   (assoc (scalar 0x1f600) :text "")
                   (assoc (scalar 0x1f600) :text "three")]]
      (is (false? (invoke-c7 'sh08-character-carrier? [value])))
      (is (= :rejected (:status (quoted value))))
      (is (= :rejected (:status (invoke-c7 'sh08-type-node [(literal value) {} {:profile :meta :target :jvm}])))))
    ;; This finite representation guard does not re-prove C2 decoding.
    (is (true? (invoke-c7 'sh08-character-carrier? [(assoc (scalar 0x1f600) :text "xx")])))
    (let [{:keys [node forms]} (quoted-data-input (scalar 0x1f600))
          result (invoke-c7 'sh08-qd-node [node forms])]
      (is (= :accepted (:status result)))
      (is (= :map (get-in result [:data :kind])))
      (is (= :gravity.type/map (get-in result [:data :type :kind]))))))

(deftest sh08-quoted-data-recursive-development-kernel
  (doseq [[value type] [[nil :gravity.type/nil] [true :gravity.type/bool] [false :gravity.type/bool]
                        [42 :gravity.type/integer] [\a :gravity.type/character] ["hello" :gravity.type/string]
                        [:token :gravity.type/keyword] ['unresolved-data-name :gravity.type/symbol]]]
    (let [{:keys [node forms payload]} (quoted-data-input value)
          result (invoke-c7 'sh08-qd-node [node forms]) data (:data result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= type (:type data)))
      (is (= value (:value data)))
      (is (= {:kind :quoted-data :quote-core-node-id (:node-id node) :form-id payload
              :syntax-id payload :data-path [] :child-form-ids []} (:origin data)))))
  (doseq [[value kind] [['() :gravity.type/list] [[] :gravity.type/vector] [{} :gravity.type/map] [#{} :gravity.type/set]
                        ['(throw unresolved-data-name) :gravity.type/list]
                        ['(quote (throw unresolved-data-name)) :gravity.type/list]
                        [[nil true 1 \a "s" :k 'symbol '(nested data) #{:a :b} {:k []}] :gravity.type/vector]
                        [{'symbol [:value] :k '(item)} :gravity.type/map]]]
    (let [{:keys [node forms source-forms]} (quoted-data-input value)
          result (invoke-c7 'sh08-qd-node [node forms])
          products (invoke-c7 'sh08-qd-products [[node] source-forms {:profile :meta :target :jvm}])
          fact (first (:constraints products))]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= kind (get-in result [:data :type :kind])))
      (is (= value (get-in result [:data :value])))
      (is (= [] (:diagnostics products)))
      (is (= (:data result) (:quoted-data fact) (get-in fact [:dependencies :data])))
      (is (= :no-evaluation (get-in fact [:solution :evaluation])))
      (is (= (get-in result [:data :type]) (get (:type-table products) (:node-id node))))))
  (let [{:keys [node forms]} (quoted-data-input '(quote (throw unresolved-data-name)))
        result (invoke-c7 'sh08-qd-node [node forms])]
    (is (= :gravity.type/symbol (get-in result [:data :children 0 :type])))
    (is (= :gravity.type/list (get-in result [:data :children 1 :type :kind])))
    (is (= [1 1] (get-in result [:data :children 1 :children 1 :origin :data-path])))))

(deftest sh08-quoted-data-authentication-development-kernel
  (let [{:keys [node forms payload owner sha]} (quoted-data-input [true false])
        child-ids (get-in forms [payload :child-form-ids])]
    (doseq [[changed-node changed-forms]
            [[(assoc-in node [:attributes :quoted-form-id] (sha 999)) forms]
             [(assoc-in node [:attributes :quoted-kind] :map) forms]
             [(assoc-in node [:attributes :quoted-value] [false true]) forms]
             [(assoc node :children [(sha 999)]) forms]
             [(assoc node :resolved-binding-ids [(sha 999)]) forms]
             [(assoc-in node [:evaluation :kind] :left-to-right) forms]
             [node (assoc-in forms [(first (get-in forms [owner :child-form-ids])) :value] 'execute)]
             [node (assoc-in forms [payload :parent-form-id] (sha 999))]
             [node (assoc-in forms [payload :child-form-ids] (vec (reverse child-ids)))]
             [node (assoc-in forms [(first child-ids) :value] "wrong")]
             [node (assoc-in forms [payload :child-form-ids] [payload])]]]
      (is (= "C7-VERIFY" (:rule (invoke-c7 'sh08-qd-node [changed-node changed-forms])))))
    (is (= ["C7-ANNOTATION" :quoted-data-work-limit]
           ((juxt :rule :reason) (invoke-c7 'sh08-qd-node-with-budget [node forms 1]))))
    (let [wide (quoted-data-input (vec (repeat 1025 true)))]
      (is (= ["C7-ANNOTATION" :quoted-data-work-limit]
             ((juxt :rule :reason) (invoke-c7 'sh08-qd-node [(:node wide) (:forms wide)])))))
    (let [invented {:node-id (sha 888) :core-form :call :source {:form-id (first child-ids)}}
          products (invoke-c7 'sh08-qd-products [[node invented] (vec (vals forms)) {}])]
      (is (= ["C7-VERIFY" :quoted-payload-is-executable]
             ((juxt :rule :reason) (first (:diagnostics products))))))
    (doseq [kind [:decimal :ratio :integer :tagged]]
      (let [value {:kind :gravity/deferred-literal :digits "100000"}
            forms (-> forms (assoc-in [payload :kind] kind) (assoc-in [payload :value] value)
                      (assoc-in [payload :child-form-ids] []) (assoc-in [owner :value] (list 'quote value)))
            node (assoc node :attributes {:quoted-form-id payload :quoted-syntax-id payload :quoted-kind kind :quoted-value value})
            result (invoke-c7 'sh08-qd-node [node forms])]
        (is (= "C7-ANNOTATION" (:rule result)))
        (is (= (if (= kind :tagged) :unsupported-quoted-data-kind :unsupported-quoted-numeric-representation) (:reason result)))))))

(deftest sh08-quoted-data-work-budget-development-kernel
  (let [first-input (quoted-data-input [true 1]) second-input (quoted-data-input 'next-data 9000)
        {:keys [node forms payload]} first-input
        last-child (second (get-in forms [payload :child-form-ids]))
        invalid (assoc-in forms [last-child :kind] :decimal)
        source-forms (vec (vals (merge invalid (:forms second-input))))
        nodes [node (:node second-input)]
        exhausted (invoke-c7 'sh08-qd-products-with-budget [nodes source-forms {} 3])
        spare (invoke-c7 'sh08-qd-products-with-budget [nodes source-forms {} 4])]
    (is (= [:unsupported-quoted-numeric-representation :quoted-data-work-limit] (mapv :reason (:diagnostics exhausted))))
    (is (= 3 (:work-count exhausted)))
    (is (= {} (:type-table exhausted)))
    (is (= [:unsupported-quoted-numeric-representation] (mapv :reason (:diagnostics spare))))
    (is (= 4 (:work-count spare)))
    (is (= :gravity.type/symbol (get (:type-table spare) (get-in second-input [:node :node-id]))))
    (let [invented {:node-id :invented :core-form :call :source {:form-id last-child}}
          collided (invoke-c7 'sh08-qd-products-with-budget
                              [[node invented (:node second-input)] (vec (vals (merge forms (:forms second-input)))) {} 3])]
      (is (= [:quoted-payload-is-executable :quoted-data-work-limit] (mapv :reason (:diagnostics collided))))
      (is (= 3 (:work-count collided))))))

(defn- pattern-alias-kernel-input []
  ;; Normalized pass input; genuine C5/C6 source products are tested separately.
  (let [{:keys [nodes forms lexical bindings binding-types sha] :as input} (structural-match-input)
        binder (sha 5000) scope (sha 9000)
        binding-record {:pattern-kind :binding :pattern-binding-id binder :pattern-binding-scope-id scope
                        :pattern-form-id (sha 606) :pattern-syntax-id (sha 606) :path [1]
                        :pattern-binding-use-syntax-ids [(sha 500)]}
        root-record {:pattern-kind :map :core-node-id (sha 502) :clause-ordinal 0}
        tree {:record root-record :children [{:key :payload :coordinate 1 :tree {:record binding-record}}]}
        ref {:node-id (sha 500) :core-form :reference :children [] :attributes {:binding-id binder}
             :source {:form-id (sha 500) :syntax-id (sha 500)}}
        body (fn [id] {:node-id (sha id) :core-form :reference :children []
                       :attributes {:binding-id (sha 1002)} :source {:form-id (sha id) :syntax-id (sha id)}})
        outer {:node-id (sha 502) :core-form :match :children [(sha 108) (sha 800)]
               :attributes {:branch-child-indexes [1]} :source {:syntax-id (sha 502)}}
        inner {:node-id (sha 800) :core-form :match :children [(sha 500) (sha 801) (sha 802)]
               :attributes {:branch-child-indexes [1 2]} :source {:syntax-id (sha 800)}}
        sequence (into (filterv #(not (contains? #{(sha 109) (sha 110)} (:node-id %))) (:sequence input))
                       [ref (body 801) (body 802) inner outer])
        nodes (into {} (map (juxt :node-id identity) sequence))
        forms (assoc forms (sha 500) {:form-id (sha 500) :syntax-id (sha 500) :kind :symbol :value 'packet})
        base {:nodes nodes :node-sequence sequence :forms forms :lexical-bindings []
              :binding-index (assoc bindings binder {:binding-class :lexical :scope-id scope}) :structural? true}
        outer-group (assoc base :node outer :branches [{:clause-ordinal 0 :branch-core-node-id (sha 800)}]
                           :patterns [binding-record] :pattern-trees [tree])
        inner-tree (fn [flag] {:record {:pattern-kind :vector :vector-width 2}
                               :children [{:coordinate 0 :tree {:record {:pattern-kind :literal :pattern-value flag}
                                                               :form {:kind :boolean}}}]})
        inner-group (assoc base :node inner :branches [{:clause-ordinal 0 :branch-core-node-id (sha 801)}
                                                     {:clause-ordinal 1 :branch-core-node-id (sha 802)}]
                           :patterns [] :pattern-trees [(inner-tree true) (inner-tree false)])]
    (assoc input :groups [inner-group outer-group] :nodes nodes :sequence sequence :forms forms :lexical []
                 :binding-types (assoc binding-types (sha 1002) :gravity.type/i64) :binder binder)))

(deftest sh08-pattern-alias-development-kernel
  (let [{:keys [groups nodes sequence lexical binding-types binder sha]} (pattern-alias-kernel-input)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [sequence nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
        enriched (invoke-c7 'sh08-sm-groups-with-shapes [groups (:binding-shapes inference)])
        checked (invoke-c7 'sh08-bm-checks [enriched (:type-table inference) {} {:profile :meta :target :jvm}])
        evidence (get (:binding-shapes inference) binder)
        alias-shape (invoke-c7 'sh08-sm-group-shape [(first enriched) (:type-table inference) (:binding-types inference) {}])]
    (is (= :converged (:convergence-status inference)) (pr-str inference))
    (is (= [] (:diagnostics inference)))
    (is (> (:round-count inference) 1))
    (is (= :gravity.type/i64 (get (:type-table inference) (sha 800)) (get (:type-table inference) (sha 502))))
    (is (= :gravity.type/vector (:kind (:type evidence))))
    (is (= [] (:diagnostics checked)) (pr-str checked))
    (is (= 3 (count (:constraints checked))))
    (is (= :alternatives (:shape-kind alias-shape)))
    (is (= evidence (:pattern-binding-evidence alias-shape)))
    (is (= [true false] (get-in alias-shape [:alternatives 0 :parts 0 :shape :domain :values])))
    (is (= (sha 502) (:match-core-node-id evidence)))
    (is (= [1] (:pattern-path evidence)))
    (is (= [3] (get-in evidence [:extractions 0 :constructor-path])))
    (is (= (sha 104) (get-in evidence [:extractions 0 :value-node-id])))
    (is (= (sha 9000) (:scope-id evidence)))
    (is (= 0 (:clause-ordinal evidence)))
    (let [bad (assoc-in (first enriched) [:nodes (sha 500) :source :syntax-id] (sha 999))]
      (is (= ["C7-VERIFY" :invalid-pattern-shape-link]
             ((juxt :rule :reason) (invoke-c7 'sh08-sm-group-shape [bad (:type-table inference) (:binding-types inference) {}])))))
    (is (= :nonconverged (:convergence-status
                         (invoke-c7 'sh08-ft-infer-acyclic-with-context
                                    [sequence nodes lexical [] [] [] {} binding-types 1 {:boolean-matches groups}]))))
    (is (= (:binding-shapes inference)
           (:binding-shapes (invoke-c7 'sh08-ft-infer-acyclic-with-context
                                       [sequence nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}]))))))

(deftest sh08-pattern-alias-correlations-development-kernel
  (let [scalar {:status :accepted :shape-kind :scalar :type :gravity.type/bool :domain {:values [true false]} :shape-size 1}
        shape {:status :accepted :shape-kind :map :type {:kind :gravity.type/map}
               :shape-size 5 :parts [{:key :a :coordinate 1 :shape scalar} {:key :b :coordinate 3 :shape scalar}]}
        literal (fn [v] {:record {:pattern-kind :literal :pattern-value v} :form {:kind :boolean}})
        binding (fn [id] {:record {:pattern-kind :binding :pattern-binding-id id :path [id]}})
        pattern (fn [a b] {:record {:pattern-kind :map}
                           :children [{:key :a :tree a} {:key :b :tree b}]})
        group {:node {:node-id :outer} :branches [{:clause-ordinal 0} {:clause-ordinal 1} {:clause-ordinal 2}]
               :pattern-trees [(pattern (literal true) (literal true)) (pattern (literal false) (literal false))
                               (pattern (binding :x) (binding :y))]}
        partition (invoke-c7 'sh08-sm-partition [group shape])
        extractions (get-in partition [:branches 2 :extractions])
        alias (fn [id] {:status :accepted :shape-kind :alternatives :type :gravity.type/bool
                        :alternatives (mapv :shape (filter #(= id (:binding-id %)) extractions))})
        constructor {:status :accepted :shape-kind :vector :shape-size 3 :type {:kind :gravity.type/vector}
                     :parts [{:coordinate 0 :shape (alias :x)} {:coordinate 1 :shape (alias :y)}]}
        rebuilt (invoke-c7 'sh08-sm-construct-cases [constructor])
        pairs (fn [result] (mapv (fn [case] (mapv #(get-in % [:shape :domain :values 0]) (:parts case))) (:alternatives result)))
        independent (update-in constructor [:parts 1 :shape :alternatives]
                               #(mapv (fn [s] (assoc s :case-origins {:independent (get-in s [:case-origins :outer])})) %))]
    (is (= :accepted (:status partition)))
    (is (= [] (:remaining partition)))
    (is (= 4 (count extractions)))
    (is (= #{0 1} (set (map #(get-in % [:case-origins :outer :case-index]) extractions))))
    (is (= [[false true] [true false]] (pairs rebuilt)))
    (is (= 2 (count (:alternatives rebuilt))))
    (is (= #{[true true] [true false] [false true] [false false]}
           (set (pairs (invoke-c7 'sh08-sm-construct-cases [independent])))))
    (let [tree {:record {:pattern-kind :vector :vector-width 2}
                :children [{:coordinate 0 :tree (literal true)} {:coordinate 1 :tree (literal true)}]}
          split (invoke-c7 'sh08-sm-split [rebuilt tree])]
      (is (= [] (:selected split)))
      (is (= 2 (count (:residual split)))))
    (is (= rebuilt (invoke-c7 'sh08-sm-construct-cases [constructor])))))

(deftest sh08-pattern-alias-limit-development-kernel
  (let [{:keys [groups nodes binding-types binder sha]} (pattern-alias-kernel-input)
        target (sha 6000) scope (sha 9000)
        scalar {:status :accepted :shape-kind :scalar :type :gravity.type/bool :domain {:values [true false]} :shape-size 40000}
        prior {binder {:binding-id binder :scope-id scope :type :gravity.type/bool :shape-size 80001
                       :use-syntax-ids [(sha 500)] :alternatives [(assoc scalar :node-id :a) (assoc scalar :node-id :b)]}
               target {:old-evidence :must-be-replaced}}
        record {:pattern-kind :binding :pattern-binding-id target :pattern-binding-scope-id scope :path []
                :pattern-binding-use-syntax-ids [(sha 801)]}
        group (assoc (first groups) :branches [{:clause-ordinal 0}] :patterns [record] :pattern-trees [{:record record}])
        seeded (invoke-c7 'sh08-sm-seed-shapes [group {} binding-types {} prior])
        marker (get seeded target)
        use-group (-> group (assoc :binding-shapes seeded)
                      (assoc-in [:node :children 0] (sha 801))
                      (assoc-in [:nodes (sha 801) :attributes :binding-id] target))
        failure (invoke-c7 'sh08-sm-group-shape [use-group {} binding-types {}])]
    (is (= :rejected (:status marker)))
    (is (= ["C7-ANNOTATION" :structural-alias-evidence-limit :bounded-pattern-binding-alternatives]
           ((juxt :rule :reason :expected) marker)))
    (is (= {:shape-size 80000 :alternative-count 2} (:actual marker)))
    (is (not (contains? marker :old-evidence)))
    (is (= (dissoc failure :node) marker))
    (is (= (sha 801) (get-in failure [:node :node-id])))
    (let [choices (fn [origin] {:shape-kind :alternatives
                               :alternatives (mapv #(assoc scalar :case-origins {origin %}) (range 33))})
          overflow (invoke-c7 'sh08-sm-construct-cases
                              [{:status :accepted :shape-kind :vector :parts [{:shape (choices :a)} {:shape (choices :b)}]}])]
      (is (= ["C7-ANNOTATION" :structural-match-residual-limit :bounded-correlated-constructor-product]
             ((juxt :rule :reason :expected) overflow))))))

(deftest sh08-pattern-alias-c8-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [upstream 'gravity.self-hosting.sh09-c7-effect-adapter-test
        invoke-c8 (var-get (ns-resolve upstream 'invoke-c8))
        fixture (var-get (ns-resolve upstream 'function-typed-result))
        verify (var-get (ns-resolve upstream 'function-verification))
        {:keys [groups nodes sequence lexical binding-types]} (pattern-alias-kernel-input)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [sequence nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
        enriched (invoke-c7 'sh08-sm-groups-with-shapes [groups (:binding-shapes inference)])
        base (fixture "/development/pattern-alias.gravity")
        facts (:constraints (invoke-c7 'sh08-bm-checks [enriched (:type-table inference) {} (:module base)]))
        raw (assoc base :constraint-ledger facts)
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (verify typed)
        effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed verification])]
    (is (seq (:call-type-facts typed)))
    (is (false? (invoke-c8 'sh09-adapter-call-facts-valid? [[] (:module typed)])))
    (is (true? (invoke-c8 'sh09-adapter-call-facts-valid? [(:call-type-facts typed) (:module typed)])))
    (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
    (is (true? (= facts (get-in effected [:typed-core :constraints]))))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification effected]))))
    (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result
                                        [typed verification (assoc-in effected [:typed-core :constraints 0 :constructor-shape :alternatives] [])]))))))

(deftest sh08-structural-match-shape-development-kernel
  (let [{:keys [nodes forms lexical bindings binding-types root reference sha]} (structural-match-input)
        shape-args [nodes {} binding-types {} lexical bindings forms #{} 0]
        shape (invoke-c7 'sh08-sm-shape (into [reference] shape-args))
        flag (get-in shape [:parts 1 :shape :parts 0 :shape])
        value (get-in shape [:parts 1 :shape :parts 1 :shape :parts 0 :shape])
        literal (fn [value kind] {:record {:pattern-kind :literal :pattern-value value} :form {:kind kind}})
        wildcard {:record {:pattern-kind :wildcard}}
        map-pattern (fn [children] {:record {:pattern-kind :map} :children children})
        child (fn [key tree] {:key key :tree tree})
        two-bools (assoc shape :parts [{:key :a :coordinate 1 :shape flag}
                                      {:key :b :coordinate 3 :shape (assoc flag :node-id (sha 999))}])
        patterns [(map-pattern [(child :a (literal true :boolean)) (child :b (literal true :boolean))])
                  (map-pattern [(child :a (literal true :boolean))])
                  (map-pattern [(child :b (literal true :boolean))]) wildcard]
        partitions (reduce (fn [{:keys [remaining selected]} pattern]
                             (let [results (mapv #(invoke-c7 'sh08-sm-split [% pattern]) remaining)]
                               (is (every? #(= :accepted (:status %)) results))
                               {:remaining (vec (mapcat :residual results))
                                :selected (conj selected (vec (mapcat :selected results)))}))
                           {:remaining [two-bools] :selected []} patterns)]
    (is (= :accepted (:status shape)) (pr-str shape))
    (is (= root (:constructor-node-id shape)))
    (is (= lexical (:binding-links shape)))
    (is (= [:extra :payload] (mapv :key (:parts shape))))
    (is (= :gravity.type/i64 (:type value)))
    (is (= [true false] (get-in flag [:domain :values])))
    (is (= [[true true] [true false] [false true] [false false]]
           (mapv (fn [shapes] (mapv #(first (get-in % [:shape :domain :values])) (:parts (first shapes))))
                 (:selected partitions))))
    (is (= [] (:remaining partitions)))
    (is (= "L7-PATTERN-TYPE" (:rule (invoke-c7 'sh08-sm-split [value (literal "wrong" :string)]))))
    (is (= ["C7-ANNOTATION" :unsupported-numeric-pattern-elaboration :established-numeric-pattern-elaboration]
           ((juxt :rule :reason :expected) (invoke-c7 'sh08-sm-split [value (literal 1 :integer)]))))
    (let [descriptor {:kind :gravity/arbitrary-decimal-literal :unscaled-value "10" :scale 1 :literal-id (sha 1)}
          decimal {:shape-kind :scalar :type :gravity.type/decimal :domain {:values [descriptor]}}
          split (invoke-c7 'sh08-sm-split [decimal (literal (assoc descriptor :literal-id (sha 2)) :decimal)])]
      (is (= ["C7-ANNOTATION" :unsupported-numeric-pattern-elaboration :established-numeric-pattern-elaboration]
             ((juxt :rule :reason :expected) split))))
    (is (= [] (:selected (invoke-c7 'sh08-sm-split [shape (map-pattern [(child :missing wildcard)])]))))
    (let [nil-shape {:status :accepted :shape-kind :scalar :type :gravity.type/nil :domain {:values [nil]}}
          present (assoc shape :parts [{:key :present :coordinate 1 :shape nil-shape}])]
      (is (= 1 (count (:selected (invoke-c7 'sh08-sm-split [present (map-pattern [(child :present (literal nil :nil))])])))))
      (is (= 0 (count (:selected (invoke-c7 'sh08-sm-split [present (map-pattern [(child :absent (literal nil :nil))])]))))))
    (doseq [[reason altered-nodes altered-lexical]
            [[:invalid-immutable-shape-link nodes [(assoc (first lexical) :initializer-node-id (sha 103))]]
             [:invalid-immutable-shape-link nodes [(assoc (first lexical) :mutability :mutable)]]
             [:constructor-child-source-mismatch (assoc-in nodes [root :children 1] (sha 101)) lexical]
             [:forged-structural-map-key (assoc-in nodes [(sha 106) :core-form] :reference) lexical]]]
      (is (= reason (:reason (invoke-c7 'sh08-sm-shape [reference altered-nodes {} binding-types {} altered-lexical bindings forms #{} 0])))))
    (is (= :accepted (:status (invoke-c7 'sh08-sm-shape [root nodes {} binding-types {} lexical bindings forms #{} 0]))))))

(defn- structural-match-products-input []
  (let [{:keys [sha nodes forms lexical bindings binding-types] :as input} (structural-match-input)
        mid (sha 502) itemb (sha 5000) scope (sha 9000)
        region (fn [i] {:kind :match-branch :match-syntax-id mid :clause-ordinal i})
        form (fn [i kind value parent children]
               {:form-id (sha i) :syntax-id (sha i) :kind kind :value value :parent-form-id (when parent (sha parent))
                :child-form-ids (mapv sha children) :scope-id (when (= i 606) scope)
                :source-span {:byte-start i :byte-end (inc i)} :origin-chain [] :generated-origin nil})
        patterns-source [(form 600 :map {:payload [true {:value 'item}]} 502 [601 602])
                         (form 601 :keyword :payload 600 []) (form 602 :vector [true {:value 'item}] 600 [603 604])
                         (form 603 :boolean true 602 []) (form 604 :map {:value 'item} 602 [605 606])
                         (form 605 :keyword :value 604 []) (form 606 :symbol 'item 604 []) (form 607 :symbol '_ 502 [])]
        forms (reduce #(assoc %1 (:form-id %2) %2) forms patterns-source)
        forms (assoc forms mid (form 502 :list nil 110 [503 109 600 500 607 501])
                           (sha 500) (form 500 :symbol 'item 502 []) (sha 501) (form 501 :symbol 'fallback 502 []))
        rows [[600 :map {:payload [true {:value 'item}]} nil [] nil]
              [602 :vector nil 2 [1] 0] [603 :literal true nil [1 0] 1]
              [604 :map {:value 'item} nil [1 1] 1] [606 :binding 'item nil [1 1 1] 3]
              [607 :wildcard '_ nil [] nil]]
        patterns (mapv (fn [ordinal [i kind value width path parent]]
                         {:ordinal ordinal :clause-ordinal (if (= i 607) 1 0) :local-ordinal (if (= i 607) 0 ordinal)
                          :parent-local-ordinal parent :root-local-ordinal 0 :parent-ordinal parent :root-ordinal (if (= i 607) 5 0)
                          :core-node-id mid :depth (count path) :path path :pattern-kind kind :pattern-value value :vector-width width
                          :pattern-form-id (sha i) :pattern-syntax-id (sha i)
                          :pattern-binding-id (when (= i 606) itemb) :pattern-binding-scope-id (when (= i 606) scope)
                          :pattern-binding-use-syntax-ids (if (= i 606) [(sha 500)] [])
                          :authenticated-sh06-artifact-id (sha 50) :sh06-semantic-projection-id (sha 51)}) (range) rows)
        branches (mapv (fn [i]
                         (let [p (get patterns (if (zero? i) 0 5))]
                           (merge (select-keys p [:core-node-id :pattern-kind :pattern-value :pattern-form-id :pattern-syntax-id
                                                  :pattern-binding-id :pattern-binding-scope-id :pattern-binding-use-syntax-ids
                                                  :authenticated-sh06-artifact-id :sh06-semantic-projection-id])
                                  {:ordinal i :clause-ordinal i :clause-count 2 :scrutinee-core-node-id (sha 109)
                                   :branch-core-node-id (sha (+ 500 i)) :branch-child-index (inc i)
                                   :branch-form-id (sha (+ 500 i)) :branch-syntax-id (sha (+ 500 i))
                                   :pattern-binding-ids (if (zero? i) [itemb] []) :conditional-region (region i)
                                   :scrutinee-evaluation :evaluate-scrutinee-once :branch-evaluation :conditionally-evaluate-selected-branch}))) (range 2))
        clause-fields [:clause-ordinal :branch-child-index :pattern-kind :pattern-form-id :pattern-syntax-id :pattern-value
                       :branch-form-id :branch-syntax-id :pattern-binding-id :pattern-binding-scope-id :pattern-binding-ids]
        attrs {:scrutinee-child-index 0 :branch-count 2 :branch-child-indexes [1 2] :branch-clauses (mapv #(select-keys % clause-fields) branches)
               :evaluation-order :scrutinee-then-source-ordered-pattern-candidates :selection-policy :source-ordered-pattern-candidates
               :runtime-reachability :not-asserted-by-sh07-b11 :result-type-join :pending-sh08 :exhaustiveness :pending-sh08
               :authenticated-sh06-artifact-id (sha 50) :sh06-semantic-projection-id (sha 51)}
        source (fn [i] {:form-id (sha i) :syntax-id (sha i) :semantic-span {:byte-start i :byte-end (inc i)} :origin-chain [] :generated-origin nil})
        extra [{:node-id (sha 500) :core-form :reference :children [] :attributes {:binding-id itemb} :resolved-binding-ids [itemb]
                :source (source 500) :evaluation {:region (region 0)}}
               {:node-id (sha 501) :core-form :reference :children [] :attributes {:binding-id (sha 1002)} :resolved-binding-ids [(sha 1002)]
                :source (source 501) :evaluation {:region (region 1)}}
               {:node-id mid :core-form :match :children [(sha 109) (sha 500) (sha 501)] :attributes attrs :resolved-binding-ids [itemb]
                :source (source 502) :evaluation {:kind :scrutinee-then-source-ordered-pattern-candidates :order [{:index 0 :core-node-id (sha 109)}]}}]
        sequence (into (vec (butlast (:sequence input))) (conj extra (assoc-in (last (:sequence input)) [:children 1] mid)))
        nodes (into {} (map (juxt :node-id identity) sequence))
        bindings (assoc bindings itemb {:binding-id itemb :binding-class :lexical :definition-syntax-id (sha 606) :scope-id scope :name 'item})
        skeleton (merge (select-keys attrs [:selection-policy :runtime-reachability :result-type-join :exhaustiveness
                                            :authenticated-sh06-artifact-id :sh06-semantic-projection-id])
                        {:core-node-id mid :scrutinee-core-node-id (sha 109) :branch-core-node-ids [(sha 500) (sha 501)]})]
    (assoc input :core {:nodes sequence :lexical-bindings lexical :match-branch-records branches
                        :match-pattern-records patterns :match-decision-skeletons [skeleton]}
           :products {:status :accepted :node-table nodes :binding-index bindings :lexical-bindings lexical}
           :forms forms :nodes nodes :bindings bindings :binding-types (assoc binding-types (sha 1002) :gravity.type/i64)
           :item itemb :match-id mid)))

(defn- quoted-structural-kernel-input []
  (let [{:keys [core products forms lexical sha] :as input} (structural-match-products-input)
        joined (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])
        quoted (quoted-data-input (array-map :extra :retained :payload [true {:value 'ready}]))
        qnode (assoc (:node quoted) :node-id (sha 108))
        qforms (assoc-in (:forms quoted) [(:owner quoted) :scope-id] (sha 3001))
        forms (-> (merge forms qforms) (assoc-in [(sha 113) :child-form-ids] [(sha 111) (:owner quoted)]))
        lexical (mapv #(assoc % :initializer-form-id (:owner quoted) :initializer-syntax-id (:owner quoted)) lexical)
        sequence (mapv (fn [node] (cond (= (sha 108) (:node-id node)) qnode
                                        (= (:match-id input) (:node-id node))
                                        (-> node (assoc :children [(sha 109) (sha 500)])
                                            (assoc-in [:attributes :branch-child-indexes] [1]))
                                        :else node)) (:nodes core))
        nodes (into {} (map (juxt :node-id identity) sequence))
        group (-> (first (:boolean-matches joined))
                  (assoc :nodes nodes :node-sequence sequence :forms forms :lexical-bindings lexical :node (nodes (:match-id input)))
                  (update :branches #(vector (first %))) (update :pattern-trees #(vector (first %))))
        quote-products (invoke-c7 'sh08-qd-products [sequence (vec (vals forms)) {:profile :meta :target :jvm}])]
    (assoc input :groups [group] :sequence sequence :nodes nodes :forms forms :lexical lexical :quote-products quote-products)))

(deftest sh08-quoted-data-structural-development-kernel
  (let [{:keys [groups sequence nodes forms lexical binding-types quote-products item match-id sha]} (quoted-structural-kernel-input)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [sequence nodes lexical [] [] [] {} binding-types 50
                              {:boolean-matches groups :quote-types (:type-table quote-products)}])
        enriched (invoke-c7 'sh08-sm-groups-with-shapes [groups (:binding-shapes inference)])
        checked (invoke-c7 'sh08-bm-checks [enriched (:type-table inference) {} {:profile :meta :target :jvm}])
        fact (first (:constraints checked)) extracted (first (:extractions fact))]
    (is (= [] (:diagnostics quote-products)))
    (is (= :converged (:convergence-status inference)))
    (is (= :gravity.type/symbol (get (:binding-types inference) item) (get (:type-table inference) match-id)))
    (is (= [] (:diagnostics checked)) (pr-str checked))
    (is (= :quoted-data (get-in fact [:constructor-shape :constructor-origin])))
    (is (= (sha 108) (get-in fact [:constructor-shape :data-origin :quote-core-node-id])))
    (is (= 1 (count (get-in fact [:constructor-shape :binding-links]))))
    (is (= [3 1 1] (:constructor-path extracted) (get-in extracted [:data-origin :data-path])))
    (is (nil? (:value-node-id extracted)))
    (is (some? (get-in extracted [:data-origin :form-id])))
    (is (= (sha 108) (get-in extracted [:data-origin :quote-core-node-id])))
    (is (= :gravity.type/symbol (:type extracted)))
    (is (not= (get-in extracted [:data-origin :form-id]) (get-in extracted [:data-origin :quote-core-node-id])))
    (is (= (:type-table inference)
           (:type-table (invoke-c7 'sh08-ft-infer-acyclic-with-context
                                   [sequence nodes lexical [] [] [] {} binding-types 50
                                    {:boolean-matches groups :quote-types (:type-table quote-products)}])))))
  (let [{:keys [node forms]} (quoted-data-input {'non-keyword 'value})
        quoted (invoke-c7 'sh08-qd-node [node forms])
        shape (invoke-c7 'sh08-qd-shape [(:data quoted) node])]
    (is (= :accepted (:status quoted)))
    (is (= ["C7-ANNOTATION" :unsupported-structural-map-key] ((juxt :rule :reason) shape)))))

(deftest sh08-structural-match-inference-development-kernel
  (let [{:keys [core products forms nodes bindings lexical binding-types item match-id sha]} (structural-match-products-input)
        joined (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])
        groups (:boolean-matches joined)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
        checked (invoke-c7 'sh08-bm-checks [groups (:type-table inference) {} {:profile :meta :target :jvm}])
        facts (:constraints checked) extraction (get-in facts [0 :extractions 0])]
    (is (= :accepted (:status joined)) (pr-str joined))
    (is (= :converged (:convergence-status inference)))
    (is (= :gravity.type/i64 (get (:binding-types inference) item)))
    (is (= :gravity.type/i64 (get (:type-table inference) match-id)))
    (is (= [] (:diagnostics checked)) (pr-str checked))
    (is (= 2 (count facts)))
    (is (= [:equality :equality] (mapv :kind facts)))
    (is (= [1 1 1] (:pattern-path extraction)))
    (is (= [3 1 1] (:constructor-path extraction)))
    (is (= :gravity.type/i64 (:type extraction)))
    (is (= (sha 9000) (:scope-id extraction)))
    (is (= [true] (get-in facts [0 :matched-shapes 0 :parts 1 :shape :parts 0 :shape :domain :values])))
    (is (= [false] (get-in facts [1 :matched-shapes 0 :parts 1 :shape :parts 0 :shape :domain :values])))
    (let [partial (-> (first groups) (update :branches #(vector (first %))) (update :pattern-trees #(vector (first %))))]
      (is (= ["C7-ANNOTATION" :unsupported-partial-structural-match]
             ((juxt :rule :reason) (:diagnostic (invoke-c7 'sh08-sm-check [partial (:type-table inference) {} {:profile :meta}])))))
      (is (= ["L7-NONEXHAUSTIVE" :nonexhaustive-structural-match]
             ((juxt :rule :reason) (:diagnostic (invoke-c7 'sh08-sm-check [partial (:type-table inference) {} {:profile :formal}]))))))
    (let [rows (vec (rest (:match-pattern-records core)))
          root (assoc (rows 0) :ordinal 0 :local-ordinal 0 :root-ordinal 0 :parent-ordinal nil :parent-local-ordinal nil :depth 0 :path [])
          patterns [root
                    (assoc (rows 1) :ordinal 1 :local-ordinal 1 :parent-ordinal 0 :parent-local-ordinal 0 :path [0] :depth 1)
                    (assoc (rows 2) :ordinal 2 :local-ordinal 2 :parent-ordinal 0 :parent-local-ordinal 0 :path [1] :depth 1)
                    (assoc (rows 3) :ordinal 3 :local-ordinal 3 :parent-ordinal 2 :parent-local-ordinal 2 :path [1 1] :depth 2)
                    (assoc (rows 4) :ordinal 4 :root-ordinal 4)]
          branch (assoc (first (:match-branch-records core)) :pattern-kind :vector
                        :pattern-form-id (sha 602) :pattern-syntax-id (sha 602) :pattern-value [true {:value 'item}])
          clause-fields (keys (get-in core [:nodes (- (count (:nodes core)) 2) :attributes :branch-clauses 0]))
          mid-index (- (count (:nodes core)) 2)
          altered (-> core (assoc :match-pattern-records patterns)
                      (assoc-in [:match-branch-records 0] branch)
                      (assoc-in [:nodes mid-index :attributes :branch-clauses 0] (select-keys branch clause-fields)))
          altered-forms (-> forms (assoc-in [(sha 602) :parent-form-id] match-id)
                            (assoc-in [match-id :child-form-ids 2] (sha 602)))
          result (invoke-c7 'sh08-bm-products [altered products (vec (vals altered-forms))])]
      (is (= :accepted (:status result)) (pr-str result)))
    (let [group (assoc-in (first groups) [:node :evaluation :owner-function-syntax-id] (sha 8000))
          functions {(sha 8000) {:body-core-node-id (sha 110) :signature {:return {:type-id :gravity.type/i64}}}}
          altered (assoc (:type-table inference) (sha 501) :gravity.type/string)
          diagnostic (:diagnostic (invoke-c7 'sh08-sm-check [group altered functions {:profile :meta :target :jvm}]))]
      (is (= :gravity.type/i64 (invoke-c7 'sh08-sm-expected-return [group functions])))
      (is (= ["C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch :gravity.type/i64 :gravity.type/string]
             ((juxt :rule :reason :expected :actual) diagnostic)))
      (is (= (sha 501) (get-in diagnostic [:node :node-id]))))
    (doseq [[label altered] [[:path (assoc-in core [:match-pattern-records 4 :path] [1 0 1])]
                             [:parent (assoc-in core [:match-pattern-records 4 :parent-ordinal] 1)]
                             [:root (assoc-in core [:match-pattern-records 4 :root-ordinal] 4)]
                             [:scope (assoc-in core [:match-pattern-records 4 :pattern-binding-scope-id] nil)]
                             [:binding (assoc-in core [:match-branch-records 0 :pattern-binding-ids] [])]
                             [:extra-field (assoc-in core [:match-pattern-records 4 :invented-proof] true)]
                             [:orphan (update core :match-pattern-records conj (last (:match-pattern-records core)))]]]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [altered products (vec (vals forms))]))) (name label)))
    (let [altered (assoc-in forms [(sha 605) :value] :forged-key)]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [core products (vec (vals altered))])))))
    (let [call-nodes (assoc-in nodes [(sha 101) :core-form] :call)
          shape (invoke-c7 'sh08-sm-shape [(sha 104) call-nodes {(sha 101) :gravity.type/i64} binding-types {} lexical bindings forms #{} 0])]
      (is (= :gravity.type/i64 (get-in shape [:parts 1 :shape :parts 0 :shape :type]))))
    (let [nonliteral (assoc-in nodes [(sha 100) :core-form] :if)
          shape (invoke-c7 'sh08-sm-shape [(sha 100) nonliteral {(sha 100) :gravity.type/nil} {} {} lexical bindings forms #{} 0])
          split (invoke-c7 'sh08-sm-split [shape {:record {:pattern-kind :literal :pattern-value nil} :form {:kind :nil}}])]
      (is (= [] (:residual split)))
      (is (= 1 (count (:selected split)))))))

(defn- guarded-match-input []
  (let [{:keys [core products forms sha item match-id binding-types] :as input} (structural-match-products-input)
        guard-id (sha 700) region {:kind :match-guard :match-syntax-id match-id :clause-ordinal 0}
        source {:rule-version :sh07-root-map-guard-v1 :source-child-index 3 :form-id guard-id :syntax-id guard-id}
        clause (assoc source :child-index 3 :conditional-region region :evaluation :once-after-structural-success
                      :on-pattern-failure :next-candidate :on-true :branch-body :on-false :next-candidate
                      :non-boolean-type-effect-legality :pending-later-legality)
        expression '(allow item)
        forms (-> forms
                  (assoc-in [(sha 603) :kind] :symbol)
                  (assoc-in [(sha 603) :value] '_)
                  (assoc-in [(sha 602) :value] ['_ {:value 'item}])
                  (assoc-in [(sha 600) :value :payload] ['_ {:value 'item}])
                  (assoc-in [(sha 600) :value :when] expression)
                  (update-in [(sha 600) :child-form-ids] into [(sha 701) guard-id])
                  (assoc (sha 701) {:form-id (sha 701) :syntax-id (sha 701) :kind :keyword :value :when
                                    :parent-form-id (sha 600) :child-form-ids []})
                  (assoc guard-id {:form-id guard-id :syntax-id guard-id :kind :list :value expression
                                   :parent-form-id (sha 600) :child-form-ids [(sha 702) (sha 703)]})
                  (assoc (sha 702) {:form-id (sha 702) :syntax-id (sha 702) :kind :symbol :value 'allow :parent-form-id guard-id})
                  (assoc (sha 703) {:form-id (sha 703) :syntax-id (sha 703) :kind :symbol :value 'item :parent-form-id guard-id}))
        node (fn [i kind attrs children]
               {:node-id (sha i) :core-form kind :attributes attrs :children (mapv sha children)
                :resolved-binding-ids (if-let [id (:binding-id attrs)] [id] [])
                :source {:form-id (sha i) :syntax-id (sha i) :semantic-span {:byte-start i :byte-end (inc i)}
                         :origin-chain [] :generated-origin nil} :evaluation {:region region}})
        guards [(node 702 :reference {:binding-id (sha 8000)} []) (node 703 :reference {:binding-id item} [])
                (node 700 :call {:argument-count 1} [702 703])]
        mid-index (- (count (:nodes core)) 2)
        core (-> core
                 (assoc-in [:match-pattern-records 2 :pattern-kind] :wildcard)
                 (assoc-in [:match-pattern-records 2 :pattern-value] '_)
                 (assoc-in [:match-pattern-records 0 :pattern-value :payload] ['_ {:value 'item}])
                 (assoc-in [:match-branch-records 0 :pattern-value :payload] ['_ {:value 'item}])
                 (assoc-in [:nodes mid-index :attributes :branch-clauses 0 :pattern-value :payload] ['_ {:value 'item}])
                 (assoc-in [:match-pattern-records 0 :guard] source)
                 (assoc-in [:match-pattern-records 0 :pattern-value :when] expression)
                 (assoc-in [:match-pattern-records 4 :pattern-binding-use-syntax-ids] [(sha 703) (sha 500)])
                 (assoc-in [:match-branch-records 0 :guard] (assoc clause :node-id guard-id))
                 (assoc-in [:match-branch-records 0 :pattern-value :when] expression)
                 (assoc-in [:nodes mid-index :attributes :branch-clauses 0 :guard] clause)
                 (assoc-in [:nodes mid-index :attributes :branch-clauses 0 :pattern-value :when] expression)
                 (assoc-in [:nodes mid-index :attributes :guard-child-indexes] [3])
                 (assoc-in [:nodes mid-index :attributes :guard-rule-version] :sh07-root-map-guard-v1)
                 (assoc-in [:nodes mid-index :lowering-rule-version] :sh07-root-map-guard-v1)
                 (update-in [:nodes mid-index :children] conj guard-id))
        sequence (vec (concat (take 10 (:nodes core)) guards (drop 10 (:nodes core))))
        nodes (into {} (map (juxt :node-id identity) sequence))
        calls [{:core-node-id guard-id :argument-node-ids [(sha 703)] :evaluation-order :operator-then-arguments}]
        permit {:function-id (sha 7000) :parameter-binding-ids [(sha 8001)]
                :parameters [{:binding-id (sha 8001) :type :gravity.type/i64}] :return :gravity.type/bool :status :inferred}
        functions {(sha 7000) permit}]
    (assoc input :core (assoc core :nodes sequence :calls calls)
           :products (assoc products :node-table nodes :calls calls) :forms forms :nodes nodes :guard-id guard-id
           :guard-index (+ mid-index 3) :functions functions
           :calls calls :edges [{:call-core-node-id guard-id :classification :local-function :callee-function-syntax-id (sha 7000)}]
           :binding-types (assoc binding-types (sha 8000) permit (sha 8001) :gravity.type/i64))))

(defn- literal-guard-input [truth fallback?]
  (let [{:keys [core products forms nodes sha guard-id match-id] :as input} (guarded-match-input)
        node (-> (get nodes guard-id) (assoc :core-form :literal :attributes {:literal-kind :boolean :value truth}
                                            :children [] :resolved-binding-ids []))
        forms (-> forms (assoc-in [guard-id :kind] :boolean) (assoc-in [guard-id :value] truth)
                        (assoc-in [guard-id :child-form-ids] []) (assoc-in [(sha 600) :value :when] truth))
        sequence (mapv #(if (= guard-id (:node-id %)) node %) (remove #(contains? #{(sha 702) (sha 703)} (:node-id %)) (:nodes core)))
        mid-index (- (count sequence) 2)
        core (-> core (assoc :nodes sequence :calls [])
                 (assoc-in [:match-pattern-records 0 :pattern-value :when] truth)
                 (assoc-in [:match-pattern-records 4 :pattern-binding-use-syntax-ids] [(sha 500)])
                 (assoc-in [:match-branch-records 0 :pattern-value :when] truth)
                 (assoc-in [:nodes mid-index :attributes :branch-clauses 0 :pattern-value :when] truth))
        core (if fallback? core
               (-> core (update :match-pattern-records pop)
                   (assoc :match-branch-records [(assoc (first (:match-branch-records core)) :clause-count 1 :guard
                                                      (assoc (get-in core [:match-branch-records 0 :guard]) :child-index 2))])
                   (assoc-in [:nodes mid-index :attributes :branch-count] 1)
                   (assoc-in [:nodes mid-index :attributes :branch-child-indexes] [1])
                   (assoc-in [:nodes mid-index :attributes :branch-clauses]
                             [(assoc-in (first (get-in core [:nodes mid-index :attributes :branch-clauses])) [:guard :child-index] 2)])
                   (assoc-in [:nodes mid-index :attributes :guard-child-indexes] [2])
                   (assoc-in [:nodes mid-index :children] [(sha 109) (sha 500) guard-id])
                   (assoc-in [:match-decision-skeletons 0 :branch-core-node-ids] [(sha 500)])
                   (update :nodes #(vec (remove (fn [n] (= (sha 501) (:node-id n))) %)))))
        forms (if fallback? forms (update-in forms [match-id :child-form-ids] #(vec (take 4 %))))
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))]
    (assoc input :core core :nodes nodes :forms forms :calls []
           :products (assoc products :node-table nodes :calls []))))

(defn- throwing-guard-input
  ([] (throwing-guard-input false))
  ([subset?]
  (let [{:keys [core forms products sha guard-id match-id] :as input} (guarded-match-input)
        expression '(throw item)
        sequence (mapv (fn [node] (if (= guard-id (:node-id node))
                                   (assoc node :core-form :throw :attributes {} :children [(sha 703)] :resolved-binding-ids []) node))
                       (remove #(= (sha 702) (:node-id %)) (:nodes core)))
        index (first (keep-indexed #(when (= match-id (:node-id %2)) %1) sequence))
        core (-> core (assoc :nodes sequence :calls [] :call-edges [])
                 (assoc :error-transfers [{:core-node-id guard-id :value-core-node-id (sha 703)}])
                 (assoc-in [:match-pattern-records 0 :pattern-value :when] expression)
                 (assoc-in [:match-branch-records 0 :pattern-value :when] expression)
                 (assoc-in [:nodes index :attributes :branch-clauses 0 :pattern-value :when] expression))
        forms (-> forms (assoc-in [guard-id :value] expression) (assoc-in [(sha 702) :value] 'throw)
                  (assoc-in [(sha 600) :value :when] expression))
        core (if subset?
               (-> core (assoc-in [:match-pattern-records 2 :pattern-kind] :literal)
                   (assoc-in [:match-pattern-records 2 :pattern-value] true)
                   (assoc-in [:match-pattern-records 0 :pattern-value :payload 0] true)
                   (assoc-in [:match-branch-records 0 :pattern-value :payload 0] true)
                   (assoc-in [:nodes index :attributes :branch-clauses 0 :pattern-value :payload 0] true)) core)
        forms (if subset? (-> forms (assoc-in [(sha 603) :kind] :boolean) (assoc-in [(sha 603) :value] true)
                              (assoc-in [(sha 602) :value 0] true) (assoc-in [(sha 600) :value :payload 0] true)) forms)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))]
    (assoc input :core core :nodes nodes :forms forms :calls [] :edges []
                 :products (assoc products :node-table nodes :calls [])))))

(deftest sh08-root-map-guard-development-kernel
  (let [{:keys [core products forms nodes lexical binding-types item match-id guard-id functions calls edges sha]} (guarded-match-input)
        joined (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])
        groups (:boolean-matches joined)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) nodes lexical [] calls edges functions binding-types 50 {:boolean-matches groups}])
        types (:type-table inference)
        checked (invoke-c7 'sh08-bm-checks [groups types functions {:profile :meta :target :jvm}])
        facts (:constraints checked)]
    (is (= :accepted (:status joined)) (pr-str joined))
    (is (= :converged (:convergence-status inference)))
    (is (= :gravity.type/i64 (get types match-id)))
    (is (= :gravity.type/bool (get types guard-id)))
    (is (= :gravity.type/i64 (get (:binding-types inference) item)))
    (is (= [] (:diagnostics checked)) (pr-str checked))
    (is (= :unknown (get-in facts [0 :guard :truth])))
    (is (= (get-in facts [0 :guard]) (get-in facts [0 :dependencies :guard])))
    (is (= :unknown (get-in facts [0 :solution :guard-truth])))
    (is (contains? (set (get-in facts [0 :invalidation])) :guard-node-type))
    (is (= {:dependencies {:a 1} :solution {:b 2} :invalidation [:c]}
           (invoke-c7 'sh08-gd-constraint [{} {:dependencies {:a 1} :solution {:b 2} :invalidation [:c]}])))
    (is (= [guard-id] (get-in facts [0 :guard :call-node-ids])))
    (is (= (get-in nodes [guard-id :evaluation]) (get-in facts [0 :guard :call-membership 0 :evaluation])))
    (is (= (get-in facts [0 :remaining-before]) (get-in facts [0 :remaining-after])))
    (is (true? (:exhaustive-after? (last facts))))
    (is (= (count (get-in facts [0 :matched-shapes])) (count (get-in facts [0 :remaining-before]))))
    (let [guard-node (get nodes guard-id)
          declarations {:effects [:compiler/read-ir] :capabilities [:compiler/read]}
          effectful (assoc-in (first groups) [:nodes guard-id :preserved-declarations] declarations)
          result (invoke-c7 'sh08-gd-result [effectful (first (:branches effectful)) types true])
          input (throwing-guard-input)
          throwing (first (:boolean-matches (invoke-c7 'sh08-bm-products [(:core input) (:products input) (vec (vals (:forms input)))])))
          completion (invoke-c7 'sh08-gd-result [throwing (first (:branches throwing)) (assoc types guard-id :gravity.type/never) true])]
      (is (= :accepted (:status result)))
      (is (= [(:effects declarations) (:capabilities declarations)]
             ((juxt :effects :capabilities) (:guard result))))
      (is (= :accepted (:status completion)))
      (is (= :never (:truth completion)))
      (is (= :gravity.type/never (get-in completion [:guard :type]))))
    (doseq [type [:gravity.type/nil :gravity.type/integer :gravity.type/string :gravity.type/unknown]]
      (let [diagnostic (:diagnostic (invoke-c7 'sh08-sm-check [(first groups) (assoc types guard-id type) functions {:profile :meta}]))]
        (is (= ["L7-GUARD-TYPE" :guard-result-not-boolean :gravity.type/bool type]
               ((juxt :rule :reason :expected :actual) diagnostic)))
        (is (= guard-id (get-in diagnostic [:node :node-id])))))
    (let [partial (-> (first groups) (update :branches #(vector (first %))) (update :pattern-trees #(vector (first %))))]
      (is (= "C7-ANNOTATION" (get-in (invoke-c7 'sh08-sm-check [partial types functions {:profile :meta}]) [:diagnostic :rule])))
      (is (= "L7-NONEXHAUSTIVE" (get-in (invoke-c7 'sh08-sm-check [partial types functions {:profile :formal}]) [:diagnostic :rule]))))
    (doseq [[label altered altered-products altered-forms]
            [[:source (assoc-in core [:match-pattern-records 0 :guard :source-child-index] 1) products forms]
             [:index (assoc-in core [:match-branch-records 0 :guard :child-index] 2) products forms]
             [:node (assoc-in core [:match-branch-records 0 :guard :node-id] (sha 500)) products forms]
             [:clause (assoc-in core [:nodes (- (count (:nodes core)) 2) :attributes :branch-clauses 0 :guard :on-false] :branch-body) products forms]
             [:uses (assoc-in core [:match-pattern-records 4 :pattern-binding-use-syntax-ids] [(sha 500)]) products forms]
             [:calls core (assoc products :calls []) forms]
             [:parent core products (assoc-in forms [guard-id :parent-form-id] match-id)]]]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [altered altered-products (vec (vals altered-forms))]))) (name label)))))

(deftest sh08-quoted-data-guard-development-kernel
  (let [{:keys [core products forms binding-types functions]} (literal-guard-input false true)
        group (first (:boolean-matches (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])))
        branch (first (:branches group)) guard-id (get-in branch [:guard :node-id])
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) (:nodes group) (:lexical-bindings group) [] [] [] functions binding-types 50 {:boolean-matches [group]}])]
    (doseq [[value expected] [[false :false] [true :true] [nil :gravity.type/nil] ["wrong" :gravity.type/string]
                             ['(throw unresolved) {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/symbol] :shape :literal}]]]
      (let [quoted (quoted-data-input value)
            node (assoc (:node quoted) :node-id guard-id)
            qresult (invoke-c7 'sh08-qd-node [node (:forms quoted)])
            altered (-> group (assoc-in [:nodes guard-id] node) (update :forms merge (:forms quoted)))
            checked (invoke-c7 'sh08-gd-result [altered branch {guard-id (get-in qresult [:data :type])} true])]
        (if (boolean? value)
          (do (is (= :accepted (:status checked))) (is (= expected (:truth checked))))
          (do (is (= ["L7-GUARD-TYPE" :guard-result-not-boolean :gravity.type/bool expected]
                     ((juxt :rule :reason :expected :actual) checked)))
              (is (= guard-id (get-in checked [:node :node-id])))))))
    (let [quoted (quoted-data-input false) node (assoc (:node quoted) :node-id guard-id)
          group (-> group (assoc-in [:nodes guard-id] node) (update :forms merge (:forms quoted)))
          checked (invoke-c7 'sh08-sm-check [group (:type-table inference) functions {:profile :meta}])]
      (is (nil? (:diagnostic checked)))
      (is (= :false (get-in checked [:constraints 0 :guard :truth])))
      (is (= [] (get-in checked [:constraints 0 :matched-shapes])))
      (is (seq (get-in checked [:constraints 0 :structurally-matched-shapes]))))))

(deftest sh08-quoted-data-c8-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [upstream 'gravity.self-hosting.sh09-c7-effect-adapter-test
        invoke-c8 (var-get (ns-resolve upstream 'invoke-c8))
        fixture (var-get (ns-resolve upstream 'function-typed-result)) verify (var-get (ns-resolve upstream 'function-verification))
        {:keys [quote-products groups nodes sequence lexical binding-types]} (quoted-structural-kernel-input)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [sequence nodes lexical [] [] [] {} binding-types 50
                              {:boolean-matches groups :quote-types (:type-table quote-products)}])
        enriched (invoke-c7 'sh08-sm-groups-with-shapes [groups (:binding-shapes inference)])
        base (fixture "/development/quoted-data.gravity")
        facts (into (:constraints quote-products) (:constraints (invoke-c7 'sh08-bm-checks [enriched (:type-table inference) {} (:module base)])))
        raw (assoc base :constraint-ledger facts)
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (verify typed) effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed verification])]
    (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
    (is (true? (= facts (get-in effected [:typed-core :constraints]))))
    (is (= (:call-type-facts typed) (:call-type-facts effected)))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification effected]))))
    (doseq [candidate [(assoc-in effected [:typed-core :constraints 0 :quoted-data :origin :data-path] [999])
                       (assoc-in effected [:typed-core :constraints 0 :dependencies :quote :children] [:invented])
                       (update-in effected [:typed-core :constraints] subvec 1)]]
      (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification candidate])))))))

(deftest sh08-root-map-guard-literal-development-kernel
  (doseq [[truth fallback?] [[false true] [true false]]]
    (let [{:keys [core products forms nodes lexical binding-types functions sha guard-id]} (literal-guard-input truth fallback?)
          joined (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])
          groups (:boolean-matches joined)
          inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                               [(:nodes core) nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
          checked (invoke-c7 'sh08-bm-checks [groups (:type-table inference) {} {:profile :meta}])
          first-fact (first (:constraints checked))]
      (is (= :accepted (:status joined)) (pr-str joined))
      (is (= [] (:diagnostics checked)) (pr-str checked))
      (is (= (if truth :true :false) (get-in first-fact [:guard :truth])))
      (is (= :gravity.type/i64 (get-in first-fact [:extractions 0 :type])))
      (if truth
        (is (true? (:exhaustive-after? first-fact)))
        (do (is (= [] (:matched-shapes first-fact)))
            (is (= (:remaining-before first-fact) (:remaining-after first-fact)))
            (let [group (assoc-in (first groups) [:node :evaluation :owner-function-syntax-id] (sha 9009))
                  functions {(sha 9009) {:body-core-node-id (:node-id (:node group)) :signature {:return {:type-id :gravity.type/i64}}}}
                  bad-types (assoc (:type-table inference) (sha 500) :gravity.type/string)
                  checked (invoke-c7 'sh08-sm-check [group bad-types functions {:profile :meta}])]
              (is (nil? (:diagnostic checked)))
              (is (= :gravity.type/string (get-in checked [:constraints 0 :rhs])))
              (is (= :gravity.type/never (get-in checked [:constraints 0 :solution :effective-result-type])))
              (is (= :not-executed (get-in checked [:constraints 0 :solution :body-completion])))
              (is (= :gravity.type/i64 (:type checked)))
              (is (= :gravity.type/i64 (invoke-c7 'sh08-ft-infer-node-with-context
                                                  [(:node group) bad-types {} functions [] {:boolean-matches [group]}])))))))))

(deftest sh08-guard-match-forward-inference-development-kernel
  (let [make (fn [id form children attributes] {:node-id id :core-form form :children children :attributes attributes})
        match-node (make :match :match [:condition :call :transfer] {:branch-child-indexes [1 2]})
        nodes [(make :condition :literal [] {:literal-kind :boolean :value true})
               (make :operator :reference [] {:binding-id :callee-binding})
               (make :call :call [:operator] {})
               (make :problem :literal [] {:literal-kind :string :value "problem"})
               (make :transfer :throw [:problem] {}) match-node
               (assoc (make :caller-node :fn [:match] {}) :source {:syntax-id :caller})
               (make :value :literal [] {:literal-kind :string :value "resolved"})
               (assoc (make :callee-node :fn [:value] {}) :source {:syntax-id :callee})]
        indexed (into {} (map (juxt :node-id identity) nodes))
        records [{:function-syntax-id :caller :function-core-node-id :caller-node :body-core-node-id :match :parameter-binding-ids [] :fixed-arity 0}
                 {:function-syntax-id :callee :function-core-node-id :callee-node :body-core-node-id :value :parameter-binding-ids [] :fixed-arity 0}]
        functions (invoke-c7 'sh08-ft-function-skeletons [records {:profile :meta :target :jvm :effects #{:error/throw} :capabilities #{}}])
        calls [{:core-node-id :call :argument-node-ids []}]
        edges [{:call-core-node-id :call :classification :local-function :callee-function-syntax-id :callee}]
        group {:node match-node :nodes indexed :branches [{:branch-core-node-id :call} {:branch-core-node-id :transfer}]}
        context {:boolean-matches [group]}
        pass (invoke-c7 'sh08-ft-infer-pass-with-context [nodes [] [] calls edges functions {} context])
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context [nodes indexed [] [] calls edges functions {} 20 context])]
    (is (= :gravity.type/unknown (get-in pass [:type-table :match])))
    (is (= :converged (:convergence-status inference)) (pr-str inference))
    (is (= :gravity.type/string (get-in inference [:type-table :call]) (get-in inference [:type-table :match])))
    (is (= :gravity.type/string (get-in inference [:function-types :caller :return])))
    (doseq [[left right expected] [[:gravity.type/unknown :gravity.type/never :gravity.type/unknown]
                                  [:gravity.type/unknown :gravity.type/string :gravity.type/unknown]
                                  [:gravity.type/never :gravity.type/string :gravity.type/string]
                                  [:gravity.type/string :gravity.type/never :gravity.type/string]]]
      (is (= expected (invoke-c7 'sh08-gd-match-result-type [match-node {:call left :transfer right} context]))))))

(deftest sh08-root-map-guard-completion-development-kernel
  (let [{:keys [core products forms nodes lexical binding-types functions guard-id sha]} (throwing-guard-input)
        group (first (:boolean-matches (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])))
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes lexical [] [] [] functions binding-types 50 {:boolean-matches [group]}])
        types (:type-table inference)
        single (-> group (update :branches #(vector (first %))) (update :pattern-trees #(vector (first %))))
        checked (invoke-c7 'sh08-sm-check [single types functions {:profile :meta}])]
    (is (= :gravity.type/never (get types guard-id)))
    (is (= :never (invoke-c7 'sh08-gd-truth [group (first (:branches group)) types])))
    (is (= :unknown (invoke-c7 'sh08-gd-truth [group (first (:branches group)) (dissoc types guard-id)])))
    (is (= :gravity.type/never (:type checked)) (pr-str checked))
    (is (= :gravity.type/i64 (get-in checked [:constraints 0 :rhs])))
    (is (= :gravity.type/never (get-in checked [:constraints 0 :solution :effective-result-type])))
    (is (= [] (get-in checked [:constraints 0 :matched-shapes]) (get-in checked [:constraints 0 :remaining-after])))
    (is (= "L7-UNREACHABLE" (get-in (invoke-c7 'sh08-sm-check [group types functions {:profile :meta}]) [:diagnostic :rule])))
    (let [input (guarded-match-input)
          original (first (:boolean-matches (invoke-c7 'sh08-bm-products [(:core input) (:products input) (vec (vals (:forms input)))])))
          altered (assoc-in original [:node :evaluation :owner-function-syntax-id] (sha 9009))
          declared {(sha 9009) {:body-core-node-id (:node-id (:node group)) :signature {:return {:type-id :gravity.type/i64}}}}
          checked (invoke-c7 'sh08-sm-check [altered (assoc types guard-id :gravity.type/bool (sha 500) :gravity.type/string) declared {:profile :meta}])]
      (is (= "C7-TYPE-MISMATCH" (get-in checked [:diagnostic :rule]))))
    (let [input (throwing-guard-input true)
          subset (first (:boolean-matches (invoke-c7 'sh08-bm-products [(:core input) (:products input) (vec (vals (:forms input)))])))
          checked (invoke-c7 'sh08-sm-check [subset types functions {:profile :meta}])]
      (is (nil? (:diagnostic checked)) (pr-str checked))
      (is (= :gravity.type/i64 (:type checked)))
      (is (= [] (get-in checked [:constraints 0 :matched-shapes])))
      (is (= 1 (count (get-in checked [:constraints 0 :remaining-after]))))
      (is (= [] (get-in checked [:constraints 1 :remaining-after])))))
  (require 'gravity.self-hosting.sh08-loop-recur-type-test)
  (let [{:keys [core forms bindings types]} ((ns-resolve 'gravity.self-hosting.sh08-loop-recur-type-test 'typed-handler-kernel-data))
        handler (assoc (first (filter #(= :handler (:node-id %)) (:nodes core)))
                       :core-form :literal :children [] :attributes {:literal-kind :boolean :value false} :resolved-binding-ids [])
        core (-> core (update :nodes #(mapv (fn [node] (if (= :handler (:node-id node)) handler node)) %))
                 (assoc-in [:error-handlers 0 :catch-binding-use-syntax-ids] [])
                 (assoc-in [:identity-preimage :error-handlers 0 :catch-binding-use-syntax-ids] []))
        forms (mapv #(case (:form-id %) :handler-form (assoc % :kind :boolean :value false :child-form-ids [])
                                        :catch-form (assoc % :value '(catch String e false))
                                        :try-form (assoc % :value '(try (throw problem) (catch String e false))) %) forms)
        types (assoc types :handler :gravity.type/bool :try :gravity.type/bool)
        source (invoke-c7 'sh08-eh-source-products [core forms bindings 65536])
        checked (invoke-c7 'sh08-eh-type-checks [(:groups source) types (into {} (map (juxt :node-id identity) (:nodes core)))])
        group {:nodes (into {} (map (juxt :node-id identity) (:nodes core))) :forms (into {} (map (juxt :form-id identity) forms)) :node-sequence (:nodes core)}
        result (invoke-c7 'sh08-gd-result [group {:guard {:node-id :try}} types true])]
    (is (= :accepted (:status source)) (pr-str source))
    (is (= [] (:diagnostics checked)))
    (is (= :accepted (:status result)))
    (is (= :unknown (:truth result)))
    (is (= :gravity.type/bool (get-in result [:guard :type])))))

(deftest sh08-root-map-guard-c8-composition
  ;; Normalized compiled C7/C8 oracle; actual source verification is separate.
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [upstream 'gravity.self-hosting.sh09-c7-effect-adapter-test
        invoke-c8 (var-get (ns-resolve upstream 'invoke-c8))
        fixture (var-get (ns-resolve upstream 'function-typed-result)) verify (var-get (ns-resolve upstream 'function-verification))
        {:keys [core products forms nodes lexical binding-types functions calls edges guard-id]} (guarded-match-input)
        groups (:boolean-matches (invoke-c7 'sh08-bm-products [core products (vec (vals forms))]))
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes lexical [] calls edges functions binding-types 50 {:boolean-matches groups}])
        base (fixture "/development/guard.gravity") module (:module base)
        facts (:constraints (invoke-c7 'sh08-bm-checks [groups (:type-table inference) functions module]))
        call-facts (invoke-c7 'sh08-ft-call-facts-with-context [calls edges nodes (:type-table inference) functions module {}])
        raw (assoc base :constraint-ledger facts :call-type-facts call-facts)
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (verify typed) result (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed verification])]
    (is (= [guard-id] (mapv :call-core-node-id call-facts)))
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (is (= facts (get-in result [:typed-core :constraints])))
    (is (= call-facts (:call-type-facts result)))
    (is (= (get-in nodes [guard-id :evaluation]) (get-in result [:typed-core :constraints 0 :guard :call-membership 0 :evaluation])))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification result]))))
    (doseq [candidate [(assoc-in result [:typed-core :constraints 0 :guard :truth] :true)
                       (assoc-in result [:typed-core :constraints 0 :dependencies :guard :type] :gravity.type/string)
                       (assoc-in result [:typed-core :constraints 0 :guard :conditional-region :clause-ordinal] 1)
                       (assoc-in result [:typed-core :constraints 0 :solution :guard-fallthrough-shapes] [])
                       (assoc result :call-type-facts [])]]
      (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification candidate])))))))

(deftest sh08-root-map-guard-authenticated-source-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [invoke-c8 (var-get (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8))
        artifacts (mapv #(fixture-artifact "accepted" "function-root-map-guards" %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [facts (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (:constraint-ledger result))
              guarded (filterv :guard facts) core (canonical-core artifact)
              nodes (into {} (map (juxt :node-id identity) (:nodes core)))
              functions (into {} (map (juxt :definition-name identity) (:function-type-table result)))
              call-ids (mapv :core-node-id (:calls core))
              guard-call-ids (vec (mapcat #(get-in % [:guard :call-node-ids]) guarded))
              choose-node (get nodes (get-in functions ['choose :body-core-node-id]))
              verification (verification-result artifact result)
              effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [result verification])]
          (is (= 6 (count facts)))
          (is (= 4 (count guarded)))
          (is (= {:unknown 2 :false 1 :true 1} (frequencies (map #(get-in % [:guard :truth]) guarded))))
          (is (= :gravity.type/i64 (get (:type-table result) (:node-id choose-node))))
          (is (= [4 5] (get-in choose-node [:attributes :guard-child-indexes])))
          (is (= 6 (count (:children choose-node))))
          (is (= call-ids (mapv :call-core-node-id (:call-type-facts result))))
          (is (= (set call-ids) (set guard-call-ids)))
          (is (= 2 (count call-ids)))
          (is (seq (filter #(> (count (:pattern-binding-use-syntax-ids %)) 1) (:match-pattern-records core))))
          (doseq [fact guarded]
            (is (= :gravity.type/bool (get-in fact [:guard :type])))
            (is (= (:guard fact) (get-in fact [:dependencies :guard])))
            (is (= (get-in fact [:guard :truth]) (get-in fact [:solution :guard-truth])))
            (is (= :once-after-structural-success (get-in fact [:guard :evaluation])))
            (is (= [] (get-in fact [:guard :effects]) (get-in fact [:guard :capabilities])))
            (doseq [call (get-in fact [:guard :call-membership])]
              (is (= (get-in nodes [(:core-node-id call) :evaluation]) (:evaluation call)))))
          (let [false-fact (first (filter #(= :false (get-in % [:guard :truth])) guarded))
                true-fact (first (filter #(= :true (get-in % [:guard :truth])) guarded))]
            (is (= [] (:matched-shapes false-fact)))
            (is (seq (:structurally-matched-shapes false-fact)))
            (is (= (:remaining-before false-fact) (:remaining-after false-fact)))
            (is (true? (:exhaustive-after? true-fact))))
          (is (= :passed (:status verification)))
          (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
          (is (= (:typed-core result) (:typed-core effected)))
          (is (= (:call-type-facts result) (:call-type-facts effected)))
          (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [result verification effected]))))
          (doseq [altered [(assoc result :call-type-facts [])
                           (update result :constraint-ledger #(mapv (fn [c] (if (:guard c) (assoc-in c [:guard :truth] :true) c)) %))]]
            (is (= :rejected (:status (verification-result artifact altered))))
            (is (= :rejected (:status (invoke-c8 'sh09-build-authenticated-pure-effect-result [altered verification]))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" "function-root-map-guards" ".gravity"))
           (slurp (fixture-path "accepted" "function-root-map-guards" ".qst"))))))

(deftest sh08-quoted-data-authenticated-source-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [invoke-c8 (var-get (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8))
        basename "function-recursive-quoted-data"
        artifacts (mapv #(fixture-artifact "accepted" basename %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [core (canonical-core artifact) nodes (:nodes core)
              quotes (filterv #(= :quote (:core-form %)) nodes)
              facts (filterv #(= :sh08-recursive-quoted-data-v1 (:producer-rule %)) (:constraint-ledger result))
              data (vec (mapcat #(tree-seq (fn [datum] (seq (:children datum))) :children (:quoted-data %)) facts))
              payload-forms (set (map #(get-in % [:origin :form-id]) data))
              kinds (set (map :kind data))
              functions (into {} (map (juxt :definition-name identity) (:function-type-table result)))
              structural (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (:constraint-ledger result))
              data-extractions (filterv :data-origin (mapcat :extractions structural))
              alias-shapes (filterv #(get-in % [:constructor-shape :pattern-binding-evidence]) structural)
              guarded (first (filter :guard structural))
              verification (verification-result artifact result)
              effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [result verification])]
          (is (= 9 (count functions)))
          (is (= 10 (count quotes) (count facts)))
          (is (= #{:nil :boolean :integer :character :string :keyword :symbol :list :vector :map :set} kinds))
          (is (= :gravity.type/symbol (get-in functions ['token :return])
                 (get-in functions ['extract-token :return]) (get-in functions ['nested-alias :return])
                 (get-in functions ['forward-token :return])))
          (is (= {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/symbol] :shape :literal}
                 (get-in functions ['inert-data :return])))
          (is (not-any? #(contains? payload-forms (get-in % [:source :form-id])) nodes))
          (is (not-any? #(= :throw (:core-form %)) nodes))
          (is (= 1 (count (:calls core)) (count (:call-type-facts result))))
          (is (every? #(and (= [] (:children %)) (= [] (:resolved-binding-ids %))
                           (= :no-evaluation (get-in % [:evaluation :kind]))) quotes))
          (doseq [fact facts]
            (is (= (:quoted-data fact) (get-in fact [:dependencies :data])))
            (is (= (:rhs fact) (get-in fact [:solution :type])))
            (is (= :no-evaluation (get-in fact [:solution :evaluation]))))
          (is (seq data-extractions))
          (is (every? #(nil? (:value-node-id %)) data-extractions))
          (is (every? #(= :quoted-data (get-in % [:data-origin :kind])) data-extractions))
          (is (seq alias-shapes))
          (is (some #(seq (get-in % [:constructor-shape :binding-links])) structural))
          (is (= :false (get-in guarded [:guard :truth])))
          (is (= [] (:matched-shapes guarded)))
          (is (seq (:structurally-matched-shapes guarded)))
          (is (= :passed (:status verification)))
          (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
          (is (true? (= (:typed-core result) (:typed-core effected))))
          (is (= (:call-type-facts result) (:call-type-facts effected)))
          (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [result verification effected]))))
          (let [index (.indexOf (:constraint-ledger result) (first facts))]
            (doseq [candidate [(assoc-in result [:constraint-ledger index :quoted-data :origin :data-path] [999])
                               (assoc-in result [:constraint-ledger index :dependencies :quote :attributes :quoted-kind] :invented)
                               (assoc-in result [:constraint-ledger index :rhs] :gravity.type/string)]]
              (is (= :rejected (:status (verification-result artifact candidate))))
              (is (= :rejected (:status (invoke-c8 'sh09-build-authenticated-pure-effect-result [candidate verification]))))))
          (let [request (function-request artifact) index (.indexOf (:nodes core) (first quotes))]
            (doseq [[keys value] [[[:attributes :quoted-form-id] "invented"] [[:attributes :quoted-kind] :invented]
                                  [[:attributes :quoted-value] :invented] [[:children] ["invented"]]
                                  [[:resolved-binding-ids] ["invented"]] [[:core-form] :call]]]
              (let [changed (assoc-in request (into [:canonical-core-artifact :nodes index] keys) value)
                    rejected (invoke-c7 'sh08-function-type-core-artifact [changed])]
                (is (= :rejected (:status rejected)))
                (is (= "C7-VERIFY" (get-in rejected [:diagnostics 0 :rule])))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" basename ".gravity")) (slurp (fixture-path "accepted" basename ".qst"))))))

(deftest sh08-quoted-data-source-rejections
  (doseq [[suffix rule reason expected actual]
          [["symbol-result" "C7-TYPE-MISMATCH" :function-result-type-mismatch :gravity.type/string :gravity.type/symbol]
           ["integer-result" "C7-TYPE-MISMATCH" :function-result-type-mismatch :gravity.type/i64 :gravity.type/integer]
           ["list-guard" "L7-GUARD-TYPE" :guard-result-not-boolean :gravity.type/bool
            {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/symbol] :shape :literal}]
           ["decimal" "C7-ANNOTATION" :unsupported-quoted-numeric-representation nil nil]
           ["map-key" "C7-ANNOTATION" :unsupported-structural-map-key nil nil]]]
    (let [basename (str "function-quoted-data-" suffix)
          artifact (fixture-artifact "rejected" basename ".gravity") result (function-result artifact)
          diagnostic (first (:diagnostics result))
          forms (get-in (function-request artifact) [:b47-context :authenticated-core-request :forms])
          form (first (filter #(= (:syntax-id %) (:syntax-id diagnostic)) forms))]
      (is (= :accepted (:status artifact)))
      (is (= :rejected (:status result)))
      (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str diagnostic))
      (is (= :type-checking (:stage diagnostic)))
      (is (some #(= (:node-id %) (:core-node-id diagnostic)) (:nodes (canonical-core artifact))))
      (is (= [(:source-span form) (:origin-chain form) (:generated-origin form)]
             ((juxt :source-span :origin-chain :generated-origin) diagnostic)))
      (when expected (is (= [expected actual] ((juxt :expected-type :actual-type) diagnostic))))
      (is (= (slurp (fixture-path "rejected" basename ".gravity")) (slurp (fixture-path "rejected" basename ".qst"))))))
  (doseq [[suffix rule stage] [["arity" "C6-CORE-SHAPE" :core-lowering] ["unresolved" "C5-UNRESOLVED" :name-resolution]]]
    (let [basename (str "function-quoted-data-" suffix)
          diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil (catch clojure.lang.ExceptionInfo e (ex-data e)))]
      (is (= [rule stage] [(or (:rule diagnostic) (:id diagnostic)) (:stage diagnostic)]) (pr-str diagnostic))
      (is (= (slurp (fixture-path "rejected" basename ".gravity")) (slurp (fixture-path "rejected" basename ".qst")))))))

(deftest sh08-pattern-alias-authenticated-source-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [invoke-c8 (var-get (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8))
        basename "function-pattern-alias-nested"
        artifacts (mapv #(fixture-artifact "accepted" basename %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [core (canonical-core artifact) nodes (into {} (map (juxt :node-id identity) (:nodes core)))
              functions (into {} (map (juxt :definition-name identity) (:function-type-table result)))
              facts (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (:constraint-ledger result))
              alias-facts (filterv #(get-in % [:constructor-shape :pattern-binding-evidence]) facts)
              correlated-id (get-in functions ['correlated-rebuild :body-core-node-id])
              reconstruction (first (filter #(and (= :alternatives (get-in % [:constructor-shape :shape-kind]))
                                                  (= :collection-literal (get-in nodes [(get-in % [:constructor-shape :node-id]) :core-form]))) facts))
              whole (first (filter #(and (= 2 (count (get-in % [:constructor-shape :alternatives])))
                                         (get-in % [:constructor-shape :pattern-binding-evidence])) facts))
              item-records (filter #(= 'item (:pattern-value %)) (:match-pattern-records core))
              verification (verification-result artifact result)
              effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [result verification])]
          (is (= 6 (count functions)))
          (is (= :gravity.type/i64 (get-in functions ['nested-vector :return])))
          (is (= :gravity.type/i64 (get-in functions ['nested-map-forward :return])))
          (is (= :gravity.type/bool (get (:type-table result) correlated-id)))
          (is (>= (count alias-facts) 6))
          (is (= 2 (count (get-in reconstruction [:constructor-shape :alternatives]))))
          (is (= #{[true false] [false true]}
                 (set (map (fn [shape] (mapv #(get-in % [:shape :domain :values 0]) (:parts shape)))
                           (get-in reconstruction [:constructor-shape :alternatives])))))
          (is (= 2 (count (get-in whole [:constructor-shape :pattern-binding-evidence :extractions]))))
          (is (some #(seq (get-in % [:constructor-shape :binding-links])) alias-facts))
          (is (>= (count item-records) 6))
          (is (= (count item-records) (count (set (map :pattern-binding-id item-records)))))
          (doseq [fact alias-facts]
            (let [evidence (get-in fact [:constructor-shape :pattern-binding-evidence])]
              (is (= (count (:extractions evidence)) (count (:alternatives evidence))))
              (is (seq (:structurally-matched-shapes evidence)))
              (is (every? #(seq (:case-origins %)) (:extractions evidence)))
              (is (every? #(= (:binding-id evidence) (:binding-id %)) (:extractions evidence)))))
          (let [guarded (filter :guard facts) false-fact (first (filter #(= :false (get-in % [:guard :truth])) guarded))]
            (is (= #{:unknown :false} (set (map #(get-in % [:guard :truth]) guarded))))
            (is (= [] (:matched-shapes false-fact)))
            (is (seq (:extractions false-fact))))
          (is (= :passed (:status verification)))
          (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
          (is (true? (= (:typed-core result) (:typed-core effected))))
          (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [result verification effected]))))
          (let [index (.indexOf (:constraint-ledger result) (first alias-facts))]
            (doseq [candidate [(assoc-in result [:constraint-ledger index :constructor-shape :pattern-binding-evidence :scope-id] "forged")
                               (assoc-in result [:constraint-ledger index :constructor-shape :alternatives] [])
                               (assoc-in result [:constraint-ledger index :constructor-shape :pattern-binding-evidence :extractions] [])]]
              (is (= :rejected (:status (verification-result artifact candidate))))
              (is (= :rejected (:status (invoke-c8 'sh09-build-authenticated-pure-effect-result [candidate verification])))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" basename ".gravity")) (slurp (fixture-path "accepted" basename ".qst"))))))

(deftest sh08-pattern-alias-source-rejections
  (doseq [[suffix rule reason] [["pattern" "L7-PATTERN-TYPE" :structural-leaf-pattern-type-mismatch]
                                ["missing" "L7-UNREACHABLE" :unreachable-structural-match-clause]
                                ["width" "L7-UNREACHABLE" :unreachable-structural-match-clause]
                                ["result" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch]
                                ["false-result" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch]]]
    (let [basename (str "function-pattern-alias-" suffix)
          artifact (fixture-artifact "rejected" basename ".gravity") result (function-result artifact)
          diagnostic (first (:diagnostics result)) core (canonical-core artifact)
          nodes (into {} (map (juxt :node-id identity) (:nodes core)))
          nested (first (filter #(and (= :match (:core-form %))
                                      (= :reference (:core-form (get nodes (first (:children %)))))) (:nodes core)))
          pattern (first (filter #(and (= (:node-id nested) (:core-node-id %))
                                       (if (= suffix "pattern") (= "wrong" (:pattern-value %)) (= 0 (:depth %))))
                                 (:match-pattern-records core)))
          forms (get-in (function-request artifact) [:b47-context :authenticated-core-request :forms])
          form (first (filter #(= (:form-id %) (:pattern-form-id pattern)) forms))
          source (if (contains? #{"result" "false-result"} suffix) (:source nested)
                   {:syntax-id (:syntax-id form) :semantic-span (:source-span form)
                    :origin-chain (:origin-chain form) :generated-origin (:generated-origin form)})]
      (is (= :accepted (:status artifact)))
      (is (= :rejected (:status result)))
      (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str diagnostic))
      (is (= :type-checking (:stage diagnostic)))
      (is (= (:node-id nested) (:core-node-id diagnostic)))
      (is (= [(:syntax-id source) (:semantic-span source) (:origin-chain source) (:generated-origin source)]
             ((juxt :syntax-id :source-span :origin-chain :generated-origin) diagnostic)))
      (is (= (slurp (fixture-path "rejected" basename ".gravity")) (slurp (fixture-path "rejected" basename ".qst"))))))
  (let [basename "function-pattern-alias-scope"
        diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= ["C5-UNRESOLVED" :name-resolution] ((juxt :id :stage) diagnostic)))
    (is (= (slurp (fixture-path "rejected" basename ".gravity")) (slurp (fixture-path "rejected" basename ".qst"))))))

(defn- assert-root-map-guard-source-rejection [basename rule reason anchor expected]
  (let [artifact (fixture-artifact "rejected" basename ".gravity") result (function-result artifact)
        diagnostic (first (:diagnostics result)) core (canonical-core artifact)
        branch (first (:match-branch-records core))
        id (case anchor :guard (get-in branch [:guard :node-id]) :body (:branch-core-node-id branch) (:core-node-id branch))
        node (first (filter #(= id (:node-id %)) (:nodes core)))
        pattern-form (when (= anchor :fallback-pattern)
                       (let [form-id (:pattern-form-id (last (:match-branch-records core)))]
                         (first (filter #(= form-id (:form-id %)) (get-in (function-request artifact) [:b47-context :authenticated-core-request :forms])))))
        source (if pattern-form {:syntax-id (:syntax-id pattern-form) :semantic-span (:source-span pattern-form)
                                 :origin-chain (:origin-chain pattern-form) :generated-origin (:generated-origin pattern-form)} (:source node))]
    (is (= :accepted (:status artifact)) basename)
    (is (= :rejected (:status result)) basename)
    (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str diagnostic))
    (is (= :type-checking (:stage diagnostic)))
    (is (= id (:core-node-id diagnostic)))
    (is (= [(:syntax-id source) (:semantic-span source) (:origin-chain source) (:generated-origin source)]
           ((juxt :syntax-id :source-span :origin-chain :generated-origin) diagnostic)))
    (when expected (is (= expected ((juxt :expected-type :actual-type) diagnostic))))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst"))))))

(deftest sh08-root-map-guard-source-type-rejections
  (doseq [[name actual] [["nil" :gravity.type/nil] ["integer" :gravity.type/integer]
                         ["string" :gravity.type/string] ["unknown" :gravity.type/unknown]]]
    (assert-root-map-guard-source-rejection (str "function-root-map-guard-" name) "L7-GUARD-TYPE"
      :guard-result-not-boolean :guard [:gravity.type/bool actual])))

(deftest sh08-root-map-guard-source-boundary-rejections
  (assert-root-map-guard-source-rejection "function-root-map-guard-partial" "C7-ANNOTATION" :unsupported-partial-structural-match :match nil)
  (assert-root-map-guard-source-rejection "function-root-map-guard-result" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch
                                         :body [:gravity.type/i64 :gravity.type/string])
  (assert-root-map-guard-source-rejection "function-root-map-guard-throw" "L7-UNREACHABLE" :unreachable-structural-match-clause
                                         :fallback-pattern nil)
  (let [basename "function-root-map-guard-scope-escape"
        diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= ["C5-UNRESOLVED" :name-resolution] ((juxt :id :stage) diagnostic)))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst"))))))

(deftest sh08-structural-match-authenticated-source-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [invoke-c8 (var-get (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8))
        artifacts (mapv #(fixture-artifact "accepted" "function-structural-matches" %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [facts (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (:constraint-ledger result))
              extraction (get-in facts [0 :extractions 0]) shape (:constructor-shape (first facts))
              payload (first (filter #(= :payload (:key %)) (:parts shape)))
              core (canonical-core artifact) node-index (into {} (map (juxt :node-id identity) (:nodes core)))
              pattern (first (filter #(= (:pattern-binding-id %) (:binding-id extraction)) (:match-pattern-records core)))
              bindings (get-in (function-request artifact) [:b47-context :authenticated-core-request :binding-table])
              binding (first (filter #(= (:binding-id %) (:binding-id extraction)) bindings))
              verification (verification-result artifact result)
              effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [result verification])]
          (is (= 3 (count facts)))
          (is (every? #(= :equality (:kind %)) facts))
          (is (= :gravity.type/i64 (:type extraction)))
          (is (= (:path pattern) (:pattern-path extraction)))
          (is (= (:scope-id binding) (:pattern-binding-scope-id pattern) (:scope-id extraction)))
          (is (= [(:coordinate payload) 1 1] (:constructor-path extraction)))
          (is (= #{:extra :payload} (set (map :key (:parts shape)))))
          (doseq [part (:parts shape)]
            (is (= (:key part) (get-in node-index [(:key-node-id part) :attributes :value]))))
          (is (= [true false] (get-in payload [:shape :parts 0 :shape :domain :values])))
          (is (= [true] (get-in (first (filter #(= :payload (:key %)) (:parts (get-in facts [0 :matched-shapes 0]))))
                               [:shape :parts 0 :shape :domain :values])))
          (is (= [false] (get-in (first (filter #(= :payload (:key %)) (:parts (get-in facts [1 :matched-shapes 0]))))
                                [:shape :parts 0 :shape :domain :values])))
          (is (true? (:exhaustive-after? (last facts))))
          (is (= :passed (:status verification)))
          (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
          (is (= (:typed-core result) (:typed-core effected)))
          (is (= (:constraint-ledger result) (get-in effected [:typed-core :constraints])))
          (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [result verification effected]))))
          (doseq [altered [(update result :constraint-ledger
                                  #(mapv (fn [c] (if (= :sh08-structural-match-v1 (:producer-rule c))
                                                  (assoc-in c [:constructor-shape :parts 0 :key] :forged) c)) %))
                           (update result :constraint-ledger
                                   #(mapv (fn [c] (if (seq (:extractions c)) (assoc-in c [:extractions 0 :type] :gravity.type/string) c)) %))]]
            (is (= :rejected (:status (verification-result artifact altered))))
            (is (= :rejected (:status (invoke-c8 'sh09-build-authenticated-pure-effect-result [altered verification]))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" "function-structural-matches" ".gravity"))
           (slurp (fixture-path "accepted" "function-structural-matches" ".qst"))))))

(defn- assert-structural-source-rejection [basename rule reason anchor value]
  (let [artifact (fixture-artifact "rejected" basename ".gravity") result (function-result artifact)
        diagnostic (first (:diagnostics result)) core (canonical-core artifact)
        request (function-request artifact) forms (get-in request [:b47-context :authenticated-core-request :forms])
        nodes (:nodes core)
        pattern (cond (= anchor :leaf) (first (filter #(= value (:pattern-value %)) (:match-pattern-records core)))
                      (= anchor :root-clause) (first (filter #(and (zero? (:depth %)) (= value (:clause-ordinal %))) (:match-pattern-records core)))
                      :else (first (:match-pattern-records core)))
        source-form (first (filter #(= (:form-id %) (:pattern-form-id pattern)) forms))
        source-node (case anchor
                      :branch (first (filter #(= (:node-id %) (:branch-core-node-id (second (:match-branch-records core)))) nodes))
                      :key (let [record (first (:lexical-bindings core))
                                 ctor (first (filter #(= (:node-id %) (:initializer-node-id record)) nodes))]
                             (first (filter #(= (:node-id %) (first (:children ctor))) nodes)))
                      nil)
        source (if source-node (:source source-node)
                   {:syntax-id (:syntax-id source-form) :semantic-span (:source-span source-form)
                    :origin-chain (:origin-chain source-form) :generated-origin (:generated-origin source-form)})]
    (is (= :accepted (:status artifact)) basename)
    (is (= :rejected (:status result)) basename)
    (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str diagnostic))
    (is (= :type-checking (:stage diagnostic)))
    (is (= [(:syntax-id source) (:semantic-span source) (:origin-chain source) (:generated-origin source)]
           ((juxt :syntax-id :source-span :origin-chain :generated-origin) diagnostic)))
    (when source-node (is (= (:node-id source-node) (:core-node-id diagnostic))))
    (when (contains? #{:branch :leaf} anchor)
      (is (= [:gravity.type/i64 :gravity.type/string] ((juxt :expected-type :actual-type) diagnostic))))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst"))))))

(deftest sh08-structural-match-source-type-rejections
  (assert-structural-source-rejection "function-structural-match-pattern" "L7-PATTERN-TYPE" :structural-leaf-pattern-type-mismatch :leaf "wrong")
  (assert-structural-source-rejection "function-structural-match-missing" "L7-UNREACHABLE" :unreachable-structural-match-clause :root nil)
  (assert-structural-source-rejection "function-structural-match-result" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch :branch nil))

(deftest sh08-structural-match-source-vector-width-rejection
  (assert-structural-source-rejection "function-structural-match-width" "L7-UNREACHABLE" :unreachable-structural-match-clause :root nil))

(deftest sh08-structural-match-source-boundary-rejections
  (assert-structural-source-rejection "function-structural-match-return-shape" "L7-UNREACHABLE" :unreachable-structural-match-clause :root-clause 1)
  (assert-structural-source-rejection "function-structural-match-computed-key" "C7-ANNOTATION" :unsupported-structural-map-key :key nil)
  (let [basename "function-structural-match-scope-escape"
        diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= ["C5-UNRESOLVED" :name-resolution] ((juxt :id :stage) diagnostic)))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst"))))))

(deftest sh08-structural-match-c8-ledger-composition
  ;; Current compiled algorithms on normalized inputs, not source admission.
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [upstream 'gravity.self-hosting.sh09-c7-effect-adapter-test
        invoke-c8 (var-get (ns-resolve upstream 'invoke-c8))
        fixture (var-get (ns-resolve upstream 'function-typed-result))
        verify (var-get (ns-resolve upstream 'function-verification))
        {:keys [core products forms nodes lexical binding-types]} (structural-match-products-input)
        groups (:boolean-matches (invoke-c7 'sh08-bm-products [core products (vec (vals forms))]))
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
        constraints (:constraints (invoke-c7 'sh08-bm-checks [groups (:type-table inference) {} {:profile :meta :target :jvm}]))
        raw (assoc (fixture "/development/structural-match.gravity") :constraint-ledger constraints)
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (verify typed) result (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed verification])]
    (is (= 2 (count constraints)))
    (is (= :accepted (:status result)))
    (is (= constraints (get-in result [:typed-core :constraints])))
    (is (= identity (get-in result [:identity-input :sh08-identity-input])))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification result]))))
    (doseq [candidate [(assoc-in result [:typed-core :constraints 0 :constructor-shape :parts 1 :key] :forged)
                       (assoc-in result [:typed-core :constraints 0 :extractions 0 :type] :gravity.type/string)
                       (assoc-in result [:typed-core :constraints 0 :extractions 0 :scope-id] nil)
                       (assoc-in result [:typed-core :constraints 0 :remaining-after] [])
                       (assoc-in result [:typed-core :constraints 0 :constraint-id-request :pattern-tree :children 0 :coordinate] 0)
                       (assoc-in result [:identity-input :sh08-identity-input :constraint-ledger] [])]]
      (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification candidate])))))))

(defn- boolean-match-input []
  (let [sha #(str "sha256:" (format "%064x" %))
        mid (sha 30) sid (sha 31) bodies [(sha 32) (sha 33)]
        bid (sha 40) rid (sha 41) scope (sha 42)
        source (fn [id] {:form-id id :syntax-id id :semantic-span {:byte-start 0 :byte-end 1}
                         :origin-chain [] :generated-origin nil})
        region (fn [i] {:kind :match-branch :match-syntax-id mid :clause-ordinal i})
        forms [{:form-id mid :syntax-id mid :child-form-ids [(sha 1) sid (sha 10) (sha 12) (sha 11) (sha 13)]}
               {:form-id (sha 10) :syntax-id (sha 10) :kind :boolean :value true :scope-id nil
                :source-span {:byte-start 20 :byte-end 24} :origin-chain [] :generated-origin nil}
               {:form-id (sha 11) :syntax-id (sha 11) :kind :symbol :value 'remainder :scope-id scope
                :source-span {:byte-start 30 :byte-end 39} :origin-chain [] :generated-origin nil}
               {:form-id (sha 12) :syntax-id (sha 12)} {:form-id (sha 13) :syntax-id (sha 13)}]
        branches (mapv (fn [i]
                         {:ordinal i :clause-ordinal i :clause-count 2 :core-node-id mid
                          :scrutinee-core-node-id sid :branch-core-node-id (bodies i) :branch-child-index (inc i)
                          :pattern-kind (if (zero? i) :literal :binding)
                          :pattern-form-id (sha (+ 10 i)) :pattern-syntax-id (sha (+ 10 i))
                          :pattern-value (if (zero? i) true 'remainder)
                          :branch-form-id (sha (+ 12 i)) :branch-syntax-id (sha (+ 12 i))
                          :pattern-binding-id (when (= 1 i) rid)
                          :pattern-binding-scope-id (when (= 1 i) scope)
                          :pattern-binding-ids (if (zero? i) [] [rid])
                          :pattern-binding-use-syntax-ids (if (zero? i) [] [(sha 13)])
                          :conditional-region (region i) :scrutinee-evaluation :evaluate-scrutinee-once
                          :branch-evaluation :conditionally-evaluate-selected-branch
                          :authenticated-sh06-artifact-id (sha 50) :sh06-semantic-projection-id (sha 51)}) (range 2))
        clause-fields [:clause-ordinal :branch-child-index :pattern-kind :pattern-form-id :pattern-syntax-id
                       :pattern-value :branch-form-id :branch-syntax-id :pattern-binding-id :pattern-binding-scope-id :pattern-binding-ids]
        patterns (mapv #(merge (select-keys % [:ordinal :clause-ordinal :core-node-id :pattern-kind :pattern-value
                                              :pattern-form-id :pattern-syntax-id :pattern-binding-id :pattern-binding-scope-id
                                              :pattern-binding-use-syntax-ids :authenticated-sh06-artifact-id :sh06-semantic-projection-id])
                              {:local-ordinal 0 :root-local-ordinal 0 :depth 0 :path [] :parent-ordinal nil
                               :parent-local-ordinal nil :root-ordinal (:ordinal %) :vector-width nil}) branches)
        attrs {:scrutinee-child-index 0 :branch-count 2 :branch-child-indexes [1 2]
               :branch-clauses (mapv #(select-keys % clause-fields) branches)
               :selection-policy :source-ordered-pattern-candidates :runtime-reachability :not-asserted-by-sh07-b11
               :result-type-join :pending-sh08 :exhaustiveness :pending-sh08
               :authenticated-sh06-artifact-id (sha 50) :sh06-semantic-projection-id (sha 51)}
        skeleton (merge (select-keys attrs [:selection-policy :runtime-reachability :result-type-join :exhaustiveness
                                            :authenticated-sh06-artifact-id :sh06-semantic-projection-id])
                        {:core-node-id mid :scrutinee-core-node-id sid :branch-core-node-ids bodies})
        nodes [{:node-id sid :core-form :reference :children [] :attributes {:binding-id bid} :source (source sid)}
               {:node-id (bodies 0) :core-form :literal :children [] :attributes {:literal-kind :boolean}
                :source (source (sha 12)) :evaluation {:region (region 0)}}
               {:node-id (bodies 1) :core-form :reference :children [] :attributes {:binding-id rid}
                :source (source (sha 13)) :evaluation {:region (region 1)}}
               {:node-id mid :core-form :match :children (into [sid] bodies) :attributes attrs
                :source (source mid) :resolved-binding-ids [rid] :evaluation {}}]
        products {:status :accepted :node-table (into {} (map (juxt :node-id identity) nodes))
                  :binding-index {rid {:binding-id rid :binding-class :lexical :name 'remainder
                                       :scope-id scope :definition-syntax-id (sha 11)}}}]
    {:core {:nodes nodes :match-branch-records branches :match-pattern-records patterns :match-decision-skeletons [skeleton]}
     :products products :forms forms :bindings {bid :gravity.type/bool} :remainder rid :match-id mid :scrutinee-id sid :bodies bodies}))

(defn- keyword-match-input []
  (let [{:keys [core products forms bindings scrutinee-id bodies match-id] :as input} (boolean-match-input)
        sha #(str "sha256:" (format "%064x" %)) bid (first (keys bindings))
        forms (into {} (map (fn [form] [(:form-id form) (merge {:kind :symbol :value nil :child-form-ids []
                                                              :source-span {:byte-start 0 :byte-end 1} :origin-chain [] :generated-origin nil
                                                              :parent-form-id match-id} form)]) forms))
        forms (-> forms (assoc-in [match-id :kind] :list)
                  (assoc-in [(sha 10) :kind] :keyword) (assoc-in [(sha 10) :value] :read)
                  (assoc-in [(sha 12) :kind] :keyword) (assoc-in [(sha 12) :value] :hit)
                  (assoc-in [(sha 13) :kind] :symbol) (assoc-in [(sha 13) :value] 'remainder)
                  (assoc scrutinee-id {:form-id scrutinee-id :syntax-id scrutinee-id :kind :symbol :value 'command
                                      :parent-form-id match-id :child-form-ids [] :source-span {:byte-start 0 :byte-end 1}
                                      :origin-chain [] :generated-origin nil}
                         (sha 1) {:form-id (sha 1) :syntax-id (sha 1) :kind :symbol :value 'match
                                  :parent-form-id match-id :child-form-ids []}))
        nodes (mapv (fn [node]
                      (cond (= scrutinee-id (:node-id node)) (assoc node :resolved-binding-ids [bid])
                            (= (bodies 0) (:node-id node)) (assoc node :attributes {:literal-kind :keyword :value :hit})
                            (= (bodies 1) (:node-id node)) (assoc node :resolved-binding-ids [(:remainder input)])
                            (= match-id (:node-id node))
                            (-> node (assoc-in [:attributes :branch-clauses 0 :pattern-value] :read)
                                (assoc-in [:attributes :evaluation-order] :scrutinee-then-source-ordered-pattern-candidates)
                                (assoc-in [:evaluation :kind] :scrutinee-then-source-ordered-pattern-candidates)
                                (assoc-in [:evaluation :order] [{:index 0 :core-node-id scrutinee-id}]))
                            :else node)) (:nodes core))
        core (-> core (assoc :nodes nodes :lexical-bindings [] :module {:profile :meta :target :jvm})
                 (assoc-in [:match-branch-records 0 :pattern-value] :read)
                 (assoc-in [:match-pattern-records 0 :pattern-value] :read))
        index (into {} (map (juxt :node-id identity) nodes))
        binding-index (assoc (:binding-index products) bid {:binding-id bid :binding-class :lexical :name 'command
                                                           :scope-id (sha 70) :definition-syntax-id scrutinee-id})]
    (assoc input :core core :forms (vec (vals forms)) :form-table forms :nodes index
                 :products (assoc products :node-table index :binding-index binding-index :lexical-bindings [])
                 :bindings {bid :gravity.type/keyword} :binding-index binding-index
                 :types {scrutinee-id :gravity.type/keyword (bodies 0) :gravity.type/keyword (bodies 1) :gravity.type/keyword})))

(defn- keyword-match-alias-group [group initializer types]
  (let [sha #(str "sha256:" (format "%064x" %)) alias (sha 800) bid (sha 801) owner (sha 802) bv (sha 803) name (sha 804)
        scope (sha 805) init (get-in initializer [:source :form-id])
        source {:form-id alias :syntax-id alias :semantic-span nil :origin-chain [] :generated-origin nil}
        reference {:node-id alias :core-form :reference :children [] :attributes {:binding-id bid}
                   :resolved-binding-ids [bid] :source source}
        record {:let-core-node-id owner :ordinal 0 :binding-id bid :name 'saved :mutability :immutable
                :definition-form-id name :definition-syntax-id name :binding-scope-id scope
                :initializer-node-id (:node-id initializer) :initializer-form-id init :initializer-syntax-id (get-in initializer [:source :syntax-id])
                :initializer-scope-id scope}
        forms (-> (:forms group) (assoc-in [init :scope-id] scope)
                  (assoc alias {:form-id alias :syntax-id alias :kind :symbol :value 'saved :source-span nil :origin-chain [] :generated-origin nil}
                         owner {:form-id owner :syntax-id owner :kind :list :child-form-ids [(sha 806) bv alias]}
                         bv {:form-id bv :syntax-id bv :kind :vector :child-form-ids [name init] :parent-form-id owner}
                         name {:form-id name :syntax-id name :kind :symbol :value 'saved :scope-id scope}))
        owner-node {:node-id owner :core-form :let :children [(:node-id initializer) alias] :resolved-binding-ids [bid]
                    :attributes {:binding-vector-form-id bv :binding-vector-syntax-id bv} :source {:form-id owner :syntax-id owner}}
        group (-> group (assoc-in [:node :children 0] alias)
                  (update :nodes assoc alias reference owner owner-node (:node-id initializer) initializer)
                  (assoc :forms forms :lexical-bindings [record])
                  (assoc-in [:binding-index bid] {:binding-id bid :binding-class :lexical :name 'saved :scope-id scope :definition-syntax-id name}))]
    [group (assoc types alias :gravity.type/keyword)]))

(defn- keyword-match-call-input []
  (let [{:keys [core products form-table nodes scrutinee-id] :as input} (keyword-match-input)
        sha #(str "sha256:" (format "%064x" %)) operator (sha 900) bid (sha 901)
        source {:form-id operator :syntax-id operator :semantic-span nil :origin-chain [] :generated-origin nil}
        op {:node-id operator :core-form :reference :children [] :attributes {:symbol 'choose :binding-id bid}
            :resolved-binding-ids [bid] :source source}
        call-node (-> (nodes scrutinee-id) (assoc :core-form :call :children [operator] :resolved-binding-ids [bid]
                                                :attributes {:operator-child-index 0 :argument-count 0 :argument-child-indexes []
                                                             :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
                                                :evaluation {:kind :operator-then-arguments :order [{:index 0 :core-node-id operator}]}))
        sequence (vec (mapcat (fn [node] (if (= scrutinee-id (:node-id node)) [op call-node] [node])) (:nodes core)))
        call {:core-node-id scrutinee-id :operator-node-id operator :operator-binding-id bid :argument-node-ids []
              :ordered-evaluation-node-ids [operator] :evaluation-order :operator-then-arguments :result-policy :call-result}
        forms (-> form-table (assoc-in [scrutinee-id :kind] :list) (assoc-in [scrutinee-id :value] '(choose))
                  (assoc-in [scrutinee-id :child-form-ids] [operator])
                  (assoc operator {:form-id operator :syntax-id operator :kind :symbol :value 'choose :child-form-ids []
                                   :parent-form-id scrutinee-id :source-span nil :origin-chain [] :generated-origin nil}))
        bindings (assoc (:binding-index input) bid {:binding-id bid :binding-class :namespace :name 'choose :definition-syntax-id bid})
        core (assoc core :nodes sequence :calls [call])
        index (into {} (map (juxt :node-id identity) sequence))]
    (assoc input :core core :nodes index :forms (vec (vals forms)) :form-table forms :binding-index bindings
                 :products (assoc products :node-table index :binding-index bindings)
                 :operator operator :call call)))

(deftest sh08-keyword-call-match-development-kernel
  (let [{:keys [core products forms types bindings scrutinee-id operator call]} (keyword-match-call-input)
        joined (invoke-c7 'sh08-bm-products [core products forms])
        group (assoc (first (:boolean-matches joined)) :shape-call-context {:source-core core :budget-left 65536})
        shape #(invoke-c7 'sh08-sm-group-shape [%1 %2 bindings {}])
        result (shape group types)
        check (invoke-c7 'sh08-sm-check [group types {} {:profile :meta :target :jvm}])]
    (is (= :accepted (:status joined) (:status result)))
    (is (= :gravity.type/keyword (:type result) (:type check)))
    (is (= {:excluded-values []} (:domain result)))
    (is (= [] (:binding-links result)))
    (is (not (contains? result :call-links)))
    (is (not (contains? result :constructor-node-id)))
    (is (= result (shape (assoc-in group [:shape-call-context :budget-left] (:call-join-work result)) types)))
    (is (= :root-scalar-match-work-limit (:reason (shape (assoc-in group [:shape-call-context :budget-left] (dec (:call-join-work result))) types))))
    (doseq [bad [(assoc-in group [:forms operator :value] 'forged)
                 (assoc-in group [:nodes operator :resolved-binding-ids] [:forged])
                 (assoc-in group [:nodes operator :attributes :binding-id] :forged)]]
      (is (= :root-scalar-match-source-mismatch (:reason (shape bad types)))))
    (is (= :root-scalar-match-type-or-call-mismatch
           (:reason (shape (assoc-in group [:shape-call-context :calls scrutinee-id] (assoc call :argument-node-ids [:forged])) types))))
    (is (= :rejected (:status (shape group (dissoc types scrutinee-id)))))))

(deftest sh08-keyword-match-development-kernel
  (let [{:keys [core products forms bindings nodes types match-id scrutinee-id bodies remainder]} (keyword-match-input)
        joined (invoke-c7 'sh08-bm-products [core products forms]) group (first (:boolean-matches joined))
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) nodes [] [] [] [] {} bindings 20 {:boolean-matches (:boolean-matches joined)}])
        check #(invoke-c7 'sh08-sm-check [%1 %2 {} {:profile %3 :target :jvm}])
        result (check group (:type-table inference) :meta)
        shape (invoke-c7 'sh08-sm-group-shape [group types bindings {}])
        seeded (invoke-c7 'sh08-sm-seed-shapes [group types bindings {} {}])
        nested (-> group (assoc-in [:node :children 0] (bodies 1)) (assoc :binding-shapes seeded))
        nested-shape (invoke-c7 'sh08-sm-group-shape [nested types bindings {}])
        aliases (keyword-match-alias-group nested (nodes (bodies 1)) types)]
    (is (= :accepted (:status joined)) (pr-str joined))
    (is (= :converged (:convergence-status inference)) (pr-str (:diagnostics inference)))
    (is (= [] (:diagnostics inference)))
    (is (= :gravity.type/keyword (get-in inference [:binding-types remainder]) (get-in inference [:type-table match-id]) (:type result)))
    (is (nil? (:diagnostic result)))
    (is (= 2 (count (:constraints result))))
    (is (= {:excluded-values []} (:domain shape)))
    (is (= {:excluded-values [:read]} (get-in result [:constraints 0 :remaining-after 0 :domain])))
    (is (= :gravity.type/keyword (get-in result [:constraints 1 :extractions 0 :type])))
    (is (= {:excluded-values [:read]} (get-in result [:constraints 1 :extractions 0 :domain])))
    (is (= {:excluded-values [:read]} (get-in nested-shape [:alternatives 0 :domain])))
    (doseq [[g ts] [[nested types] aliases]]
      (is (= ["L7-UNREACHABLE" :unreachable-structural-match-clause]
             ((juxt :rule :reason) (:diagnostic (check g ts :meta))))))
    (doseq [profile [:meta :formal]]
      (let [partial (-> group (update :branches pop) (update :pattern-trees pop))]
        (is (= ["C7-ANNOTATION" :unsupported-partial-keyword-match]
               ((juxt :rule :reason) (:diagnostic (check partial types profile)))))))
    (let [wrong (-> group (assoc-in [:pattern-trees 0 :form :kind] :string) (assoc-in [:pattern-trees 0 :record :pattern-value] "read"))]
      (is (= "L7-PATTERN-TYPE" (get-in (check wrong types :meta) [:diagnostic :rule]))))
    (doseq [changed [(assoc-in group [:forms scrutinee-id :value] 'forged)
                     (assoc-in group [:nodes scrutinee-id :resolved-binding-ids] [:forged])]]
      (is (= :root-scalar-match-source-mismatch (get-in (check changed types :meta) [:diagnostic :reason]))))
    (let [literal (assoc (nodes scrutinee-id) :core-form :literal :attributes {:literal-kind :keyword :value :read} :resolved-binding-ids [])
          g (-> group (assoc-in [:nodes scrutinee-id] literal)
                      (assoc-in [:forms scrutinee-id :kind] :keyword) (assoc-in [:forms scrutinee-id :value] :read))
          literal-shape (invoke-c7 'sh08-sm-group-shape [g types bindings {}])
          [aliased ts] (keyword-match-alias-group g literal types)]
      (is (= {:values [:read]} (:domain literal-shape)))
      (is (= {:values [:read]} (:domain (invoke-c7 'sh08-sm-group-shape [aliased ts bindings {}]))))
      (is (= "L7-UNREACHABLE" (get-in (check g types :meta) [:diagnostic :rule])))
      (is (nil? (:diagnostic (check (-> g (update :branches pop) (update :pattern-trees pop)) types :meta)))))))

(deftest sh08-keyword-match-fallback-and-source-development-kernel
  (let [{:keys [core products forms nodes types match-id scrutinee-id bodies remainder bindings] :as input} (keyword-match-input)
        sha #(str "sha256:" (format "%064x" %))
        variant (fn [kind value]
                  (let [fields {:pattern-kind kind :pattern-value value :pattern-binding-id nil :pattern-binding-scope-id nil
                                :pattern-binding-ids [] :pattern-binding-use-syntax-ids []}
                        c (-> core (update-in [:match-branch-records 1] merge fields)
                              (update-in [:match-pattern-records 1] merge (dissoc fields :pattern-binding-ids))
                              (update :nodes #(mapv (fn [node] (cond
                                              (= match-id (:node-id node)) (-> node (assoc :resolved-binding-ids [])
                                                                                   (update-in [:attributes :branch-clauses 1] merge (select-keys fields [:pattern-kind :pattern-value :pattern-binding-id :pattern-binding-scope-id :pattern-binding-ids])))
                                              (= (bodies 1) (:node-id node)) (assoc node :core-form :literal :attributes {:literal-kind :keyword :value :other} :resolved-binding-ids [])
                                              :else node)) %)))
                        fs (mapv (fn [form] (cond (= (sha 11) (:form-id form)) (assoc form :kind (if (= kind :literal) :keyword :symbol) :value value :scope-id nil)
                                                 (= (sha 13) (:form-id form)) (assoc form :kind :keyword :value :other)
                                                 :else form)) forms)
                        p (-> products (assoc :node-table (into {} (map (juxt :node-id identity) (:nodes c))))
                              (update :binding-index dissoc remainder))
                        joined (invoke-c7 'sh08-bm-products [c p fs])]
                    {:core c :joined joined :group (first (:boolean-matches joined))}))
        wildcard (variant :wildcard '_) duplicate (variant :literal :read) partial (variant :literal :write)]
    (doseq [v [wildcard duplicate partial]] (is (= :accepted (get-in v [:joined :status]))))
    (let [checked (invoke-c7 'sh08-bm-checks [[(:group wildcard)] types {} {:profile :meta :target :jvm}])]
      (is (= [] (:diagnostics checked)))
      (is (= [] (get-in checked [:constraints 1 :extractions])))
      (is (= [] (get-in checked [:constraints 1 :remaining-after]))))
    (is (= "L7-UNREACHABLE" (get-in (invoke-c7 'sh08-bm-checks [[(:group duplicate)] types {} {:profile :meta :target :jvm}]) [:diagnostics 0 :rule])))
    (doseq [profile [:meta :formal]]
      (is (= ["C7-ANNOTATION" :unsupported-partial-keyword-match]
             ((juxt :rule :reason) (get-in (invoke-c7 'sh08-bm-checks [[(:group partial)] types {} {:profile profile :target :jvm}]) [:diagnostics 0])))))
    (let [outside {:node-id (sha 999) :core-form :reference :children [] :attributes {:binding-id remainder}
                   :source {:syntax-id (sha 999)}}
          altered (update core :nodes conj outside)]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [altered (assoc-in products [:node-table (sha 999)] outside) forms])))))
    (is (= :rejected (:status (invoke-c7 'sh08-bm-products [core (assoc-in products [:binding-index remainder :scope-id] :forged) forms]))))
    (let [{quoted :node quoted-forms :forms} (quoted-data-input :read)
          qid (:node-id quoted)
          joined (invoke-c7 'sh08-bm-products [core products forms])
          group (-> (first (:boolean-matches joined))
                    (assoc-in [:node :children 0] qid) (update :nodes assoc qid quoted) (update :forms merge quoted-forms))
          qtypes (assoc types qid :gravity.type/keyword)
          shape (invoke-c7 'sh08-sm-group-shape [group qtypes bindings {}])
          [alias-group alias-types] (keyword-match-alias-group group quoted qtypes)]
      (is (= :accepted (:status shape)))
      (is (= {:values [:read]} (:domain shape)))
      (is (= {:values [:read]} (:domain (invoke-c7 'sh08-sm-group-shape [alias-group alias-types bindings {}]))))
      (is (= :rejected (:status (invoke-c7 'sh08-sm-group-shape [(assoc-in group [:nodes qid :attributes :quoted-value] :forged) qtypes bindings {}])))))))

(defn- integral-ordering-input [operation argument-types]
  ;; Normalized DATA for the actual Gravity algorithms; no source admission.
  (let [module {:namespace 'example.ordering :profile :meta :target :jvm :effects [] :capabilities []}
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        arguments (mapv #(keyword (str "arg" %)) (range (count argument-types)))
        children (into [:operator] arguments)
        binding {:binding-id :ordering-binding :binding-class :core :kind :var :name operation :namespace 'gravity.core
                 :package {:name 'gravity/core :version "bootstrap-contract-v1"} :type-ref :gravity.type/core-var
                 :safety :safe :effects [] :capabilities [] :profile-set [:meta] :target-set [:all]
                 :definition-syntax-id :ordering-definition}
        operator {:node-id :operator :core-form :reference :children [] :resolved-binding-ids [:ordering-binding]
                  :attributes {:symbol operation :position :operator :binding-id :ordering-binding :binding-class :core
                               :definition-syntax-id :ordering-definition} :source (source :operator) :evaluation {:order []}}
        operands (mapv (fn [id] {:node-id id :core-form :reference :children [] :attributes {:binding-id id}
                                 :source (source id) :evaluation {:order []}}) arguments)
        node {:node-id :call :core-form :call :children children :resolved-binding-ids [:ordering-binding]
              :attributes {:operator-child-index 0 :argument-count (count arguments) :argument-child-indexes (vec (range 1 (count children)))
                           :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
              :source (source :call) :evaluation {:owner-function-syntax-id nil :kind :operator-then-arguments
                                                :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}}
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :ordering-binding :argument-node-ids arguments
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :ordering-binding :callee-definition-syntax-id :ordering-definition
              :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids arguments
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        nodes (into [operator] (conj operands node))
        core {:module module :nodes nodes :function-records [] :calls [call] :call-edges [edge]}
        bindings {:ordering-binding binding}
        types (zipmap arguments argument-types)]
    {:module module :core core :node node :call call :edge edge :nodes (into {} (map (juxt :node-id identity) nodes))
     :bindings bindings :types types :seeds types
     :forms (into {} (map (fn [id] [id {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                      :kind (if (= id :call) :list :symbol) :value (if (= id :operator) operation id)
                                      :parent-form-id (when-not (= id :call) :call) :child-form-ids (if (= id :call) children [])}]) (conj children :call)))}))

(defn- list-query-input [operation argument-types]
  (let [input (integral-ordering-input operation argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- list-query-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-lq-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-list-query-development-kernel
  (let [s :gravity.type/string b :gravity.type/bool n :gravity.type/nil nv :gravity.type/never
        positional (fn [types] {:kind :gravity.type/list :element-types types :shape :literal})
        nested (positional [s]) empty-list (positional [])
        cases [['first empty-list n] ['rest empty-list empty-list]
               ['first (positional [n]) n] ['rest (positional [n]) empty-list]
               ['first (positional [b s n]) b] ['rest (positional [b s n]) (positional [s n])]
               ['first (positional [nested s]) nested] ['rest (positional [nested s]) (positional [s])]
               ['rest :gravity.type/list :gravity.type/list]
               ['first nv nv] ['rest nv nv]]]
    (doseq [[operation input-type expected] cases]
      (let [{:keys [core node call edge nodes forms bindings types products binding-types] :as input} (list-query-input operation [input-type])
            result (list-query-derived input) descriptor (:descriptor result)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= operation (:operation descriptor)))
        (is (= (if (= nv input-type) :not-invoked :normal) (:application descriptor)))
        (is (= [] (:latent-effects descriptor) (:direct-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= {:kind :list-value-query :operation operation :operand-node-id :arg0} (:value-origin descriptor)))
        (is (= {:representation :development-persistent-list :first :stored-head-value :rest :existing-immutable-tail
                :retention :may-retain-input-storage :copy :none} (:storage-policy descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= expected (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-lq-constraint [descriptor])] (:constraints inferred)))
        (is (true? (invoke-c7 'sh08-lq-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator descriptor)])))
        (doseq [changed [(assoc descriptor :latent-effects [:memory/allocate])
                         (assoc descriptor :direct-effects [:memory/allocate])
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc-in descriptor [:query-policy :payload-evaluation] :execute)
                         (assoc-in descriptor [:storage-policy :copy] :rebuild)
                         (assoc descriptor :return :gravity.type/unknown :result-type :gravity.type/unknown)]]
          (is (false? (invoke-c7 'sh08-lq-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator changed)]))))
        (let [work (:work-count result) derive #(invoke-c7 'sh08-lq-derive [core node call edge nodes bindings types forms %])]
          (is (= result (derive work)))
          (is (= :list-query-work-limit (:reason (derive (dec work))))))))
    (is (= :list-query-head-type-required (:reason (list-query-derived (list-query-input 'first [:gravity.type/list])))))
    (doseq [operation ['first 'rest]
            type [nil :gravity.type/unknown :gravity.type/nil :gravity.type/string :gravity.type/vector :gravity.type/map :gravity.type/set
                  {:kind :gravity.type/vector :element-types [s] :shape :literal}
                  {:kind :gravity.type/list :element-types [:gravity.type/unknown] :shape :literal}
                  {:kind :gravity.type/list :element-types [nv] :shape :literal}
                  {:kind :gravity.type/list :element-types [s] :shape :literal :quote-id :forged}
                  {:kind :gravity.type/list :element-types '() :shape :literal}]]
      (is (= :rejected (:status (list-query-derived (list-query-input operation [type])))) (pr-str [operation type])))
    (doseq [operation ['first 'rest] arguments [[] [empty-list empty-list]]]
      (is (= :list-query-arity-mismatch (:reason (list-query-derived (list-query-input operation arguments))))))
    (doseq [operation ['first 'rest]]
      (let [input (list-query-input operation [(positional [s b])])]
        (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                        #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                        #(assoc-in % [:bindings :ordering-binding :name] (if (= operation 'first) 'rest 'first))
                        #(assoc-in % [:call :extra] true)
                        #(assoc-in % [:edge :evaluation-order] :operand-before-operator)
                        #(assoc-in % [:node :evaluation :order] [])
                        #(assoc-in % [:forms :arg0 :parent-form-id] :sibling)
                        #(assoc-in % [:forms :operator :value] 'seq)]]
          (is (= :rejected (:status (list-query-derived (mutate input))))))))
    (doseq [operation ['first 'rest]]
      (let [input (list-query-input operation [(positional (vec (repeat 1024 s)))])]
        (is (= :accepted (:status (list-query-derived input))))
        (is (= 1090 (:work-count (list-query-derived input))))))
    (is (= :rejected (:status (list-query-derived (list-query-input 'rest [(positional (vec (repeat 1025 s)))])))))))

(defn- bare-list-parameter-input [actual]
  (let [sha #(str "sha256:" (format "%064x" %))
        declaration {:source-child-index 1 :marker-form-id (sha 1) :marker-syntax-id (sha 2)
                     :type-form-id (sha 3) :type-syntax-id (sha 4) :type-symbol 'List :type-binding-id (sha 5) :type-id :gravity.type/list}
        signature {:rule-version :sh07-atomic-function-signature-v1 :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters [declaration] :return nil :parameter-source-child-indexes [0] :body-source-child-index 2}
        record {:function-syntax-id :callee :function-core-node-id :function :body-core-node-id :body
                :parameter-binding-ids [:parameter] :fixed-arity 1 :definition-kind :named-top-level
                :definition-binding-id :definition :signature signature}
        module {:namespace 'example.list-parameter :profile :meta :target :jvm :effects [] :capabilities []}
        callee (assoc (invoke-c7 'sh08-ft-function-type-skeleton [record module]) :return :gravity.type/string)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :definition :argument-node-ids [:argument]
              :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :definition :callee-definition-syntax-id :definition-source
              :callee-function-syntax-id :callee :callee-function-core-node-id :function
              :argument-core-node-ids [:argument] :ordered-evaluation-node-ids [:operator :argument]
              :evaluation-order :operator-then-arguments :classification :local-function}
        node {:node-id :call :core-form :call :children [:operator :argument] :source (source :call)
              :resolved-binding-ids [:definition] :evaluation {:kind :operator-then-arguments}}
        nodes {:call node :argument {:node-id :argument :source (source :argument)}
               :operator {:node-id :operator :core-form :reference :source (source :operator)
                          :attributes {:binding-id :definition} :resolved-binding-ids [:definition]}}
        bindings {(sha 5) {:binding-id (sha 5) :name 'List :kind :type :binding-class :core :namespace 'gravity.core}
                  :parameter {:binding-id :parameter :name 'items :kind :local :binding-class :lexical :definition-syntax-id :parameter-source}
                  :definition {:binding-id :definition :name 'consume :kind :function :binding-class :namespace :namespace 'example.list-parameter}}
        core {:module module :function-records [record] :nodes (vec (vals nodes)) :calls [call] :call-edges [edge]}
        types {:argument actual :call :gravity.type/string :operator callee}
        context {:ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings)) :loop-work-left 65536}]
    {:core core :node node :call call :edge edge :callee callee :nodes nodes :bindings bindings :types types
     :functions {:callee callee} :seeds {:parameter :gravity.type/list} :context context :declaration declaration :signature signature}))

(defn- bare-list-parameter-constraints [{:keys [call edge nodes types seeds functions context]}]
  (invoke-c7 'sh08-ft-call-constraints-with-context [[call] [edge] nodes types seeds functions context]))

(defn- homogeneous-list-parameter-input [actual]
  (let [base (bare-list-parameter-input actual) sha #(str "sha256:" (format "%064x" %))
        type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        declaration (assoc (:declaration base) :rule-version :sh07-list-parameter-declaration-v1
                           :type-id type :constructor-form-id (sha 6) :constructor-syntax-id (sha 7)
                           :element-form-id (sha 8) :element-syntax-id (sha 9)
                           :element-symbol 'String :element-binding-id (sha 10))
        signature (assoc (:signature base) :parameters [declaration])
        record (assoc (first (get-in base [:core :function-records])) :signature signature)
        core (assoc (:core base) :function-records [record])
        bindings (assoc (:bindings base) (sha 10) {:binding-id (sha 10) :name 'String :kind :type :binding-class :core :namespace 'gravity.core})
        callee (assoc (:callee base) :signature signature :parameters [{:binding-id :parameter :type type}])
        types (assoc (:types base) :operator callee)]
    (assoc base :core core :bindings bindings :declaration declaration :signature signature :callee callee
                :types types :functions {:callee callee} :seeds {:parameter type} :homogeneous-type type
                :context (assoc (:context base) :ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings))))))

(defn- bare-map-parameter-input [actual]
  (let [base (walk/postwalk-replace {'List 'Map :gravity.type/list :gravity.type/map}
                                  (bare-list-parameter-input :gravity.type/map))]
    (assoc-in base [:types :argument] actual)))

(defn- mixed-map-parameter-input [order]
  (let [sha #(str "sha256:" (format "%064x" %))
        actuals {:record {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]}
                 :forms {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string :gravity.type/bool]}
                 :parts {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string :gravity.type/string]}
                 :suffix :gravity.type/string}
        sources {:record (bare-map-parameter-input (:record actuals))
                 :forms (bare-list-parameter-input (:forms actuals))
                 :parts (homogeneous-list-parameter-input (:parts actuals))
                 :suffix (walk/postwalk-replace {'List 'String :gravity.type/list :gravity.type/string}
                                               (bare-list-parameter-input :gravity.type/string))}
        declarations (mapv (fn [index role]
                             (let [base (sources role) offset (* 100 (inc index))
                                   ids (into {} (map #(vector (sha %) (sha (+ offset %))) (range 1 11)))]
                               (-> (walk/postwalk-replace ids (:declaration base))
                                   (assoc :source-child-index (inc (* 3 index))))))
                           (range) order)
        signature {:rule-version :sh07-atomic-function-signature-v1 :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters declarations :return nil :parameter-source-child-indexes (mapv #(* 3 %) (range 4)) :body-source-child-index 2}
        parameters (mapv (fn [role declaration] {:binding-id role :type (:type-id declaration)}) order declarations)
        base (bare-map-parameter-input (:record actuals))
        record (assoc (first (get-in base [:core :function-records])) :parameter-binding-ids order :fixed-arity 4 :signature signature)
        callee (assoc (:callee base) :parameters parameters :signature signature)
        args (mapv #(keyword (str (name %) "-argument")) order) children (into [:operator] args)
        call (assoc (:call base) :argument-node-ids args :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids args :ordered-evaluation-node-ids children)
        node (assoc (:node base) :children children)
        nodes (into {:call node :operator (get-in base [:nodes :operator])}
                    (map (fn [id] [id {:node-id id :source {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil}}]) args))
        bindings (reduce
                   (fn [out [index role declaration]]
                     (let [offset (* 100 (inc index)) ids (into {} (map #(vector (sha %) (sha (+ offset %))) (range 1 11)))
                           moved (walk/postwalk-replace ids (:bindings (sources role)))]
                       (merge out (dissoc moved :parameter :definition)
                              {role {:binding-id role :name (symbol (name role)) :kind :local :binding-class :lexical}})))
                   {:definition (get-in base [:bindings :definition])}
                   (map vector (range) order declarations))
        core (assoc (:core base) :function-records [record] :nodes (vec (vals nodes)) :calls [call] :call-edges [edge])
        types (merge {:operator callee :call :gravity.type/string} (zipmap args (mapv actuals order)))
        seeds (into {} (map (juxt :binding-id :type) parameters))
        context {:ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings)) :loop-work-left 65536}]
    (assoc base :core core :node node :nodes nodes :call call :edge edge :callee callee :types types :bindings bindings
                :functions {:callee callee} :seeds seeds :context context :signature signature :actuals actuals :order order)))

(defn- homogeneous-list-return-input [actual element ordinal]
  ;; Normalized independent body/type DATA; no authenticated source claim.
  (let [sha #(str "sha256:" (format "%064x" (+ (* ordinal 100) %)))
        nids (zipmap [:function :body] [(sha 1) (sha 2)])
        fids (zipmap [:function :parameters :marker :application :constructor :member :body :operator] (map sha (range 10 18)))
        sids (zipmap (keys fids) (map sha (range 30 38))) scope (sha 50)
        name (symbol (str "produce" ordinal)) type {:kind :gravity.type/list :shape :homogeneous :element-type element}
        element-name (get (into {} (map (fn [[name type]] [type name]) {'I8 :gravity.type/i8 'I16 :gravity.type/i16 'I32 :gravity.type/i32 'I64 :gravity.type/i64
                                                 'U8 :gravity.type/u8 'U16 :gravity.type/u16 'U32 :gravity.type/u32 'U64 :gravity.type/u64
                                                 'F32 :gravity.type/f32 'F64 :gravity.type/f64 'Bool :gravity.type/bool
                                                 'String :gravity.type/string 'Symbol :gravity.type/symbol 'Keyword :gravity.type/keyword 'Ratio :gravity.type/ratio})) element)
        make (fn [role kind value parent children]
               {:form-id (fids role) :syntax-id (sids role) :kind kind :value value
                :parent-form-id (fids parent) :child-form-ids (mapv fids children) :scope-id scope
                :source-span nil :origin-chain [] :generated-origin []})
        forms (into {} (map (juxt :form-id identity)
                           [(make :function :list (list 'fn [] ':- (list 'List element-name) 'independent-body) nil
                                  [:operator :parameters :marker :application :body])
                            (make :operator :symbol 'fn :function []) (make :parameters :vector [] :function [])
                            (make :marker :keyword ':- :function []) (make :application :list (list 'List element-name) :function [:constructor :member])
                            (make :constructor :symbol 'List :application []) (make :member :symbol element-name :application [])
                            (make :body :symbol 'independent-body :function [])]))
        source (fn [role] (let [form (get forms (fids role))]
                           {:form-id (:form-id form) :syntax-id (:syntax-id form) :semantic-span nil :origin-chain [] :generated-origin []}))
        declaration {:rule-version :sh07-list-return-declaration-v1 :source-child-index 2
                     :marker-form-id (fids :marker) :marker-syntax-id (sids :marker)
                     :type-form-id (fids :application) :type-syntax-id (sids :application)
                     :type-symbol 'List :type-binding-id (sha 60) :type-id type
                     :constructor-form-id (fids :constructor) :constructor-syntax-id (sids :constructor)
                     :element-form-id (fids :member) :element-syntax-id (sids :member) :element-symbol element-name :element-binding-id (sha 61)
                     :constructor-reference-symbol 'List :element-reference-symbol element-name}
        signature {:rule-version :sh07-atomic-function-signature-v1 :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters [] :return declaration :parameter-source-child-indexes [] :body-source-child-index 4}
        record {:ordinal ordinal :function-syntax-id (sids :function) :function-form-id (fids :function) :function-core-node-id (nids :function)
                :body-core-node-id (nids :body) :signature signature :fixed-arity 0 :parameter-binding-ids []
                :definition-kind :named-top-level :definition-name name :definition-binding-id (sha 62)}
        module {:namespace 'normalized.list-return :profile :meta :target :jvm :effects [:memory/allocate] :capabilities []}
        function (invoke-c7 'sh08-ft-function-type-skeleton [record module])
        body {:node-id (nids :body) :core-form :reference :children [] :source (source :body) :attributes {:binding-id (sha 63)}}
        fn-node {:node-id (nids :function) :core-form :fn :children [(nids :body)] :source (source :function)
                 :attributes {:signature signature :parameters [] :fixed-arity 0} :resolved-binding-ids []}
        nodes {(nids :body) body (nids :function) fn-node}
        bindings {(sha 60) {:binding-id (sha 60) :name 'List :binding-class :core :kind :type :namespace 'gravity.core}
                  (sha 61) {:binding-id (sha 61) :name element-name :binding-class :core :kind :type :namespace 'gravity.core}
                  (sha 62) {:binding-id (sha 62) :name name :binding-class :namespace :kind :function :namespace (:namespace module)}}
        core {:module module :function-records [record] :nodes [body fn-node] :calls [] :call-edges []}
        types {(nids :body) actual (nids :function) function}]
    {:core core :record record :function function :nodes nodes :types types :forms forms :bindings bindings
     :ids nids :declaration declaration :declared-type type :signature signature :body-binding-id (sha 63)
     :context {:ordering-core core :lookup-forms forms :lookup-bindings bindings}}))

(defn- homogeneous-list-return-products [actuals element]
  (let [inputs (mapv #(homogeneous-list-return-input %1 element %2) actuals (range))
        first-input (first inputs)
        core (assoc (:core first-input) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs)) :function-records (mapv :record inputs))
        nodes (apply merge (map :nodes inputs)) types (apply merge (map :types inputs))
        forms (apply merge (map :forms inputs)) bindings (apply merge (map :bindings inputs))
        functions (into (sorted-map-by #(compare %2 %1))
                        (map #(vector (get-in % [:function :function-id]) (:function %)) inputs))
        context {:ordering-core core :lookup-forms forms :lookup-bindings bindings}
        checked (invoke-c7 'sh08-ft-update-function-returns-with-context [functions nodes types context 65536])
        updated (:function-types checked)
        function-facts (invoke-c7 'sh08-ft-function-facts [updated nodes (:module core)])
        types (reduce (fn [table function] (assoc table (:function-core-node-id function) function)) types (vals updated))
        facts {:module (:module core) :function-type-table function-facts :type-table types :constraint-ledger (:constraints checked)}]
    {:inputs inputs :core core :nodes nodes :types types :forms forms :bindings bindings :functions functions :checked checked :facts facts}))

(defn- homogeneous-list-return-definition-input [actual]
  (let [base (homogeneous-list-return-input actual :gravity.type/string 0)
        {:keys [core record forms nodes bindings types function]} base
        sha #(str "sha256:" (format "%064x" (+ 90000 %)))
        fn-id (:function-core-node-id record) body-id (:body-core-node-id record)
        fn-form-id (get-in nodes [fn-id :source :form-id]) name (:definition-name record)
        form (fn [id kind value parent children]
               {:form-id (sha id) :syntax-id (sha (+ id 20)) :kind kind :value value :parent-form-id parent
                :child-form-ids children :scope-id (sha 50) :source-span nil :origin-chain [] :generated-origin []})
        raw-fn (get-in forms [fn-form-id :value]) raw-def (list 'def name raw-fn)
        def-form (form 0 :list raw-def nil [(sha 1) (sha 2) fn-form-id])
        forms (-> forms (assoc-in [fn-form-id :parent-form-id] (:form-id def-form))
                        (assoc (sha 0) def-form (sha 1) (form 1 :symbol 'def (sha 0) [])
                               (sha 2) (form 2 :symbol name (sha 0) [])))
        source {:form-id (sha 0) :syntax-id (sha 20) :semantic-span nil :origin-chain [] :generated-origin []}
        def-node {:node-id (sha 60) :core-form :def :children [fn-id] :source source
                  :attributes {:name-syntax-id (sha 22)}}
        record (assoc record :definition-core-node-id (:node-id def-node))
        nodes (assoc nodes (:node-id def-node) def-node)
        bindings (assoc-in bindings [(:definition-binding-id record) :definition-syntax-id] (sha 22))
        definition {:binding-id (:definition-binding-id record) :name name :core-node-id (:node-id def-node) :value-node-id fn-id}
        core (assoc core :nodes [(get nodes body-id) (get nodes fn-id) def-node] :function-records [record] :definitions [definition])
        function (invoke-c7 'sh08-ft-function-type-skeleton [record (:module core)])
        checked (invoke-c7 'sh08-ft-update-function-returns-with-context
                          [{(:function-id function) function} nodes types {:ordering-core core :lookup-forms forms :lookup-bindings bindings} 65536])
        updated (get-in checked [:function-types (:function-id function)])
        typed-function (first (invoke-c7 'sh08-ft-function-facts [{(:function-id function) updated} nodes (:module core)]))
        types (assoc types fn-id updated (:node-id def-node) updated)
        facts {:module (:module core) :function-type-table [typed-function] :type-table types :constraint-ledger (:constraints checked)}
        context {:core core :records {name record} :functions {name typed-function} :nodes nodes :forms forms :bindings bindings
                 :types types :ledger (:constraints checked) :raw-roots {(:form-id def-form) raw-def} :budget 65536}]
    (assoc base :core core :record record :forms forms :nodes nodes :bindings bindings :types types :facts facts
                :function typed-function :definition definition :definition-name name :def-node-id (:node-id def-node) :context context)))

(deftest sh08-homogeneous-list-return-development-kernel
  (let [positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        atomics [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                 :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64 :gravity.type/f32 :gravity.type/f64
                 :gravity.type/bool :gravity.type/string :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio]]
    (doseq [element atomics members [[] [element] (vec (repeat 1024 element))]]
      (let [{:keys [core record function nodes types bindings forms signature declared-type] :as input}
            (homogeneous-list-return-input (positional members) element 0)
            derive #(invoke-c7 'sh08-lr-derive [core record function nodes types bindings forms %])
            result (derive 65536) premise (:premise result) row (invoke-c7 'sh08-lr-row [premise])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :sh08-homogeneous-list-return-v1 (:version premise)))
        (is (= :all-positional-elements-match (:relation premise)))
        (is (= [(positional members) declared-type (count members)]
               [(:actual-body-type premise) (:declared-return-type premise) (:checked-element-count premise)]))
        (is (= :return-compatibility (:kind row)))
        (is (= :return (get-in row [:lhs :role])))
        (is (= premise (:dependencies row)))
        (is (= result (derive (:work-count result))))
        (is (= :list-return-work-limit (:reason (derive (dec (:work-count result))))))
        (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 0 bindings])))
        (is (false? (invoke-c7 'sh08-hl-declaration? [(:declaration input) bindings])))))
    (let [s :gravity.type/string type {:kind :gravity.type/list :shape :homogeneous :element-type s}]
      (doseq [[actual relation] [[type :homogeneous-list-identity] [:gravity.type/never :non-returning-bottom]]]
        (let [{:keys [core record function nodes types bindings forms]} (homogeneous-list-return-input actual s 0)
              result (invoke-c7 'sh08-lr-derive [core record function nodes types bindings forms 65536])]
          (is (= :accepted (:status result)))
          (is (= relation (get-in result [:premise :relation])))))
      (doseq [actual [:gravity.type/list :gravity.type/nil :gravity.type/string :gravity.type/unknown
                     (positional [s :gravity.type/keyword]) (positional [:gravity.type/never]) (positional [:gravity.type/nil])
                     (positional [:gravity.type/unknown]) (positional [type]) (positional (vec (repeat 1025 s)))]]
        (is (= :rejected (:status (invoke-c7 'sh08-lr-type-work [type actual 65536]))) (pr-str actual)))
      (let [{:keys [core record function nodes types bindings forms declaration signature] :as input} (homogeneous-list-return-input (positional [s]) s 0)]
        (doseq [mutate [#(assoc-in % [:record :signature :return :element-binding-id] "forged")
                        #(assoc-in % [:bindings (:element-binding-id declaration) :kind] :var)
                        #(assoc-in % [:forms (:element-form-id declaration) :value] 'Keyword)
                        #(assoc-in % [:forms (:type-form-id declaration) :child-form-ids] [])
                        #(assoc-in % [:forms (:constructor-form-id declaration) :scope-id] :sibling)
                        #(assoc-in % [:record :signature :body-source-child-index] 2)
                        #(assoc-in % [:function :return] :gravity.type/list)
                        #(assoc-in % [:nodes (:body-core-node-id record) :source :form-id] :sibling)]]
          (let [{:keys [core record function nodes types bindings forms]} (mutate input)]
            (is (= :rejected (:status (invoke-c7 'sh08-lr-derive [core record function nodes types bindings forms 65536]))))))
        (is (= [] (invoke-c7 'sh08-ft-signature-constraints [[record] (:module core)])))
        (let [functions {(:function-id function) function}
              update #(invoke-c7 'sh08-ft-update-function-returns-with-context [functions nodes types (:context input) %])
              result (update 65536) returned (get-in result [:function-types (:function-id function)])]
          (is (= :accepted (:status result)))
          (is (= type (:return returned)))
          (is (= (positional [s]) (:return-actual-type returned)))
          (is (nil? (:return-conflict returned)))
          (is (= :sh08-homogeneous-list-return-v1 (get-in returned [:return-compatibility :version])))
          (is (= 1 (count (:constraints result))))
          (is (= result (update (:work-count result))))
          (is (= :list-return-work-limit (get-in (update (dec (:work-count result))) [:diagnostics 0 :reason])))))
      (let [inputs [(homogeneous-list-return-input (positional (vec (repeat 1024 s))) s 0)
                    (homogeneous-list-return-input (positional []) s 1)]
            core (assoc (:core (first inputs)) :nodes (vec (mapcat #(get-in % [:core :nodes]) inputs))
                                               :function-records (mapv :record inputs))
            nodes (apply merge (map :nodes inputs)) types (apply merge (map :types inputs))
            forms (apply merge (map :forms inputs)) bindings (apply merge (map :bindings inputs))
            functions (into {} (map #(vector (get-in % [:function :function-id]) (:function %)) inputs))
            seeds (into {} (map #(vector (:body-binding-id %) (get-in % [:types (get-in % [:ids :body])])) inputs))
            context {:ordering-core core :lookup-forms forms :lookup-bindings bindings :loop-work-left 65536}
            update #(invoke-c7 'sh08-ft-update-function-returns-with-context [functions nodes types context %])
            updated (update 65536) work (:work-count updated)
            infer #(invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes [] [] [] [] functions seeds 10 (assoc context :loop-work-left %)])
            inferred (infer 65536) cumulative (- 65536 (:type-join-work-left inferred))]
        (is (= :accepted (:status updated)))
        (is (= 2 (count (:constraints updated))))
        (is (= updated (update work)))
        (is (= :list-return-work-limit (get-in (update (dec work)) [:diagnostics 0 :reason])))
        (is (= :converged (:convergence-status inferred)) (pr-str (:diagnostics inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (> cumulative work))
        (is (> (:round-count inferred) 1))
        (is (= :converged (:convergence-status (infer cumulative))))
        (is (= :list-return-work-limit (get-in (infer (dec cumulative)) [:diagnostics 0 :reason])))))))

(deftest sh08-homogeneous-list-parameter-development-kernel
  (let [s :gravity.type/string positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        type {:kind :gravity.type/list :shape :homogeneous :element-type s}]
    (doseq [members [[] [s] [s s] (vec (repeat 1024 s))]]
      (let [actual (positional members)
            {:keys [core call edge callee nodes bindings signature] :as input} (homogeneous-list-parameter-input actual)
            result (bare-list-parameter-constraints input) row (first (:constraints result)) pair (first (:rhs row))
            premise (:compatibility pair)
            check #(invoke-c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes bindings %])
            checked (check 65536)]
        (is (= [] (:diagnostics result)) (pr-str result))
        (is (= :sh08-list-parameter-compatibility-v2 (:compatibility-rule-version row)))
        (is (= :sh08-homogeneous-list-parameter-v1 (:version premise)))
        (is (= :all-positional-elements-match (:relation premise)))
        (is (= [actual actual type type] [(:argument pair) (:actual-type premise) (:chosen pair) (:declared-type premise)]))
        (is (= [s (count members) [:static-length]] [(:checked-element-type premise) (:checked-element-count premise) (:forgets premise)]))
        (is (= checked (check (:work-count checked))))
        (is (= :list-parameter-work-limit (:reason (check (dec (:work-count checked))))))
        (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 1 bindings])))
        (is (false? (invoke-c7 'sh08-ft-declaration-valid? [(:declaration input) bindings])))
        (is (false? (invoke-c7 'sh08-ft-signature-valid?
                              [(assoc signature :return (assoc (:declaration input) :source-child-index 2) :body-source-child-index 4) 1 bindings])))))
    (doseq [actual [type :gravity.type/never]]
      (let [result (bare-list-parameter-constraints (homogeneous-list-parameter-input actual))]
        (is (= [] (:diagnostics result)))
        (is (= :equality (get-in result [:constraints 0 :kind])))
        (is (nil? (get-in result [:constraints 0 :rhs 0 :compatibility])))))
    (doseq [actual [:gravity.type/list :gravity.type/string :gravity.type/nil
                    (assoc type :element-type :gravity.type/keyword)
                    (positional [s :gravity.type/keyword]) (positional [:gravity.type/unknown])
                    (positional [:gravity.type/never]) (positional (vec (repeat 1025 s)))]]
      (is (seq (:diagnostics (bare-list-parameter-constraints (homogeneous-list-parameter-input actual)))) (pr-str actual)))
    (let [input (homogeneous-list-parameter-input (positional [s]))]
      (doseq [members [42 s '(x) #{s} {:member s} nil]]
        (let [{:keys [core call edge callee nodes bindings]} (homogeneous-list-parameter-input (positional members))]
          (is (= :rejected (:status (invoke-c7 'sh08-lp-compatibility
                                              [core call edge callee 0 (positional members) nodes bindings 65536]))))))
      (doseq [mutate [#(assoc-in % [:context :ordering-core :function-records 0 :signature :parameters 0 :element-binding-id] "forged")
                      #(assoc-in % [:context :ordering-bindings (get-in input [:declaration :element-binding-id]) :kind] :var)
                      #(assoc-in % [:context :ordering-core :function-records 0 :signature :parameters 0 :constructor-syntax-id] "forged")]]
        (is (seq (:diagnostics (bare-list-parameter-constraints (mutate input)))))))
    (is (seq (:diagnostics (bare-list-parameter-constraints (bare-list-parameter-input type)))))
    (is (= {:status :accepted :work-count 2} (invoke-c7 'sh08-lc-type-work [[type] 2])))
    (is (= :work-limit (:reason (invoke-c7 'sh08-lc-type-work [[type] 1]))))
    (is (= :accepted (:status (invoke-c7 'sh08-lc-type-work [[{:kind :gravity.type/vector :shape :literal :element-types [type]}] 3]))))
    (doseq [bad [(assoc type :element-type :gravity.type/nil) (assoc type :element-type :gravity.type/character)
                 (assoc type :element-type type) (assoc type :schema :secret) (assoc type :element-types [])]]
      (is (= :rejected (:status (invoke-c7 'sh08-lc-type-work [[bad] 65536])))))
    (doseq [operation ['rest 'empty?]]
      (let [result (list-query-derived (list-query-input operation [type]))]
        (is (= :accepted (:status result)))
        (is (= (if (= operation 'rest) type :gravity.type/bool) (:type result)))))
    (is (= :rejected (:status (list-query-derived (list-query-input 'first [type])))))
    (let [transfer {:argument-node-ids [:next]} target {:target-kind :function :binding-ids [:items]}
          check #(invoke-c7 'sh08-ft-loop-kernel-transfer-types [transfer target {} {:next %} [type] 65536])]
      (is (= :accepted (:status (check type))))
      (doseq [wrong [:gravity.type/list (positional [s]) (assoc type :element-type :gravity.type/keyword)]]
        (is (= :function-recur-slot-type-mismatch (get-in (check wrong) [:issues 0 :reason])))))))

(defn- homogeneous-list-head-input [branch-index]
  (let [type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        base (list-query-input 'first [type]) parameter (homogeneous-list-parameter-input type)
        predicate (let [moved (walk/postwalk-replace {:operator :predicate-operator :arg0 :predicate-receiver :call :condition
                                                     :ordering-binding :empty-binding} (list-query-input 'empty? [type]))]
                    (assoc moved :call (:condition moved)))
        owner #(assoc-in % [:evaluation :owner-function-syntax-id] :callee)
        reference #(assoc (owner %) :resolved-binding-ids [:parameter]
                          :attributes {:symbol 'items :position :expression :binding-id :parameter :binding-class :lexical
                                       :definition-syntax-id :parameter-source})
        nodes (-> (merge (:nodes base) (:nodes predicate))
                  (update-vals owner) (update :arg0 reference) (update :predicate-receiver reference)
                  (assoc-in [:condition :core-form] :call)
                  (assoc-in [:predicate-operator :attributes :position] :operator))
        children (if (= branch-index 1) [:condition :call :base] [:condition :base :call])
        source #(hash-map :form-id % :syntax-id % :semantic-span nil :origin-chain [] :generated-origin nil)
        branch {:node-id :branch :core-form :if :children children :source (source :branch)
                :attributes {:condition-child-index 0 :then-child-index 1 :else-child-index 2 :condition-evaluation :always-first
                             :branch-evaluation :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}}
                :evaluation {:kind :condition-then-exclusive-branch :owner-function-syntax-id :callee
                             :order [{:index 0 :core-node-id :condition}]}}
        nodes (assoc nodes :branch branch
                           :base (owner {:node-id :base :core-form :literal :children [] :source (source :base)
                                         :attributes {:literal-kind :string :value ""} :evaluation {:order []}})
                           :function {:node-id :function :core-form :fn :children [:branch] :source (source :function)
                                      :resolved-binding-ids [:parameter] :attributes {:signature (:signature parameter)} :evaluation {:order []}})
        control {:core-node-id :branch :kind :if :condition-node-id :condition
                 :branches [{:role :then :predicate :truthy :core-node-id (get children 1)}
                            {:role :else :predicate :falsey :core-node-id (get children 2)}]
                 :branch-exclusivity :exactly-one :truthiness {:false-values [:nil :false] :other-values :true} :result-policy :selected-branch}
        owned-edge #(assoc % :caller-function-syntax-id :callee :caller-function-core-node-id :function)
        edge (assoc (owned-edge (:edge base)) :ordinal 1)
        predicate-edge (assoc (owned-edge (:edge predicate)) :ordinal 0)
        core (assoc (:core base) :nodes (vec (vals nodes)) :calls [(:call predicate) (:call base)] :call-edges [predicate-edge edge]
                                :function-records (get-in parameter [:core :function-records]) :control-flow [control] :lexical-bindings [] :mutations [])
        bindings (merge (:bindings parameter) (:bindings base) (:bindings predicate))
        forms (-> (merge (:forms base) (:forms predicate))
                  (assoc-in [:arg0 :value] 'items) (assoc-in [:predicate-receiver :value] 'items))
        types {:arg0 type :predicate-receiver type :condition :gravity.type/bool :base :gravity.type/string}
        predicate-input (assoc predicate :core core :nodes nodes :node (:condition nodes) :edge predicate-edge
                                        :bindings bindings :forms forms :types types)
        derived (list-query-derived predicate-input)
        types (assoc types :predicate-operator (:descriptor derived))]
    (assoc base :core core :nodes nodes :node (:call nodes) :edge edge :bindings bindings :forms forms :types types
                :predicate-input predicate-input :predicate-derived derived)))

(deftest sh08-homogeneous-list-head-development-kernel
  (doseq [branch-index [1 2]]
    (let [{:keys [core node call edge nodes bindings types forms] :as input} (homogeneous-list-head-input branch-index)
          result (list-query-derived input) descriptor (:descriptor result) proof (:head-proof descriptor)
          expected (if (= branch-index 1) :gravity.type/nil :gravity.type/string)
          check #(invoke-c7 'sh08-lq-derive [core node call edge nodes bindings types forms %])]
      (is (= :accepted (get-in input [:predicate-derived :status])) (pr-str (:predicate-derived input)))
      (is (= :accepted (:status result)) (pr-str result))
      (is (= expected (:type result) (:result-type proof)))
      (is (= :sh08-list-query-v2 (:version descriptor)))
      (is (= (if (= branch-index 1) :empty :nonempty) (:knowledge proof)))
      (is (= [:parameter :arg0 :predicate-receiver :condition :branch branch-index]
             ((juxt :binding-id :receiver-core-node-id :predicate-receiver-core-node-id :condition-core-node-id :branch-core-node-id :branch-child-index) proof)))
      (is (= result (check (:work-count result))))
      (is (= "C7-GENERIC" (:rule (check (dec (:work-count result))))))
      (is (true? (invoke-c7 'sh08-lq-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator descriptor)])))
      (doseq [changed [(assoc-in descriptor [:head-proof :knowledge] :forged)
                       (assoc-in descriptor [:head-proof :receiver-core-node-id] :sibling)
                       (assoc-in descriptor [:head-proof :condition :application] :not-invoked)
                       (dissoc descriptor :head-proof)]]
        (is (false? (invoke-c7 'sh08-lq-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator changed)]))))
      (doseq [mutate [#(assoc-in % [:nodes :predicate-receiver :attributes :binding-id] :other)
                      #(assoc-in % [:nodes :arg0 :attributes :binding-id] :shadow)
                      #(assoc-in % [:nodes :arg0 :core-form] :call)
                      #(assoc-in % [:types :condition] :gravity.type/never)
                      #(assoc-in % [:types :predicate-operator :application] :not-invoked)
                      #(assoc-in % [:bindings :empty-binding :binding-class] :lexical)
                      #(assoc-in % [:bindings :parameter :mutability] :mutable)
                      #(assoc-in % [:core :function-records 0 :signature :parameters 0 :type-id] :gravity.type/list)
                      #(assoc-in % [:core :mutations] [{:target-binding-id :parameter}])
                      #(assoc-in % [:core :control-flow 0 :branches 0 :core-node-id] :sibling)
                      #(assoc-in % [:nodes :branch :attributes :truthiness :other-values] :false)]]
        (let [changed (mutate input) changed (assoc-in changed [:core :nodes] (vec (vals (:nodes changed))))]
          (is (= :rejected (:status (list-query-derived changed))))))
      (let [changed (assoc-in input [:core :calls 0 :argument-node-ids] (vec (repeat 1024 :predicate-receiver)))]
        (is (= :list-head-branch-proof-required (:reason (list-query-derived changed)))))
      (let [inner (-> (get nodes :branch) (assoc :node-id :inner :children [:inner-condition :call :inner-base])
                      (assoc-in [:evaluation :order] [{:index 0 :core-node-id :inner-condition}]))
            inner-control (-> (get-in core [:control-flow 0])
                              (assoc :core-node-id :inner :condition-node-id :inner-condition)
                              (assoc-in [:branches 0 :core-node-id] :call) (assoc-in [:branches 1 :core-node-id] :inner-base))
            changed (-> input
                        (assoc-in [:nodes :branch :children branch-index] :inner)
                        (assoc-in [:nodes :inner] inner)
                        (assoc-in [:nodes :inner-condition] (assoc (:base nodes) :node-id :inner-condition))
                        (assoc-in [:nodes :inner-base] (assoc (:base nodes) :node-id :inner-base))
                        (assoc-in [:types :inner-condition] :gravity.type/bool)
                        (update-in [:core :control-flow] conj inner-control))
            changed (assoc-in changed [:core :nodes] (vec (vals (:nodes changed))))]
        (is (= :list-head-branch-proof-required (:reason (list-query-derived changed))))))))

(deftest sh08-bare-map-parameter-development-kernel
  (let [map-type #(hash-map :kind :gravity.type/map :shape :literal :member-types %)
        actuals [(map-type []) (map-type [:gravity.type/keyword :gravity.type/bool])
                 (map-type [:gravity.type/string :gravity.type/nil :gravity.type/keyword
                            {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string :gravity.type/bool]}])]]
    (doseq [actual actuals]
      (let [{:keys [core call edge callee nodes bindings signature] :as input} (bare-map-parameter-input actual)
            result (bare-list-parameter-constraints input) row (first (:constraints result)) pair (first (:rhs row)) premise (:compatibility pair)
            scan #(invoke-c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes bindings %])
            work (:work-count (scan 65536))]
        (is (= [] (:diagnostics result)) (pr-str result))
        (is (= :argument-compatibility (:kind row)))
        (is (= :sh08-map-list-parameter-compatibility-v1 (:compatibility-rule-version row)))
        (is (= :sh08-bare-map-parameter-v1 (:version premise)))
        (is (= actual (:argument pair) (:actual-type premise)))
        (is (= :gravity.type/map (:chosen pair) (:declared-type premise) (get-in result [:binding-types :parameter])))
        (is (= [:key-value-member-types :static-entry-count] (:forgets premise)))
        (is (= [:value :provenance :schema :ownership :linearity :regions :profile :memory-obligations] (:preserves premise)))
        (is (= :accepted (:status (scan work))))
        (is (= :map-parameter-work-limit (:reason (scan (dec work)))))
        (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 1 bindings])))
        (is (nil? (invoke-c7 'sh08-ft-atomic-type-id ['Map])))
        (let [total (:compatibility-work-count result)]
          (is (= result (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] total))))
          (is (= ["C7-GENERIC" :map-parameter-work-limit]
                 ((juxt :rule :reason) (first (:diagnostics (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] (dec total)))))))))
        (doseq [budget [0 -1]]
          (is (= :map-parameter-work-limit (:reason (scan budget)))))))
    (doseq [actual [:gravity.type/map :gravity.type/never]]
      (let [result (bare-list-parameter-constraints (bare-map-parameter-input actual))]
        (is (= [] (:diagnostics result)))
        (is (= :equality (get-in result [:constraints 0 :kind])))
        (is (nil? (get-in result [:constraints 0 :rhs 0 :compatibility])))))
    (doseq [actual [:gravity.type/list :gravity.type/vector :gravity.type/string :gravity.type/nil
                     {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword]}
                     {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/unknown]}
                     {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/never]}
                     (assoc (map-type []) :schema :secret) (assoc (map-type []) :ownership :owned)]]
      (is (seq (:diagnostics (bare-list-parameter-constraints (bare-map-parameter-input actual))))) )
    (let [input (bare-map-parameter-input (map-type [:gravity.type/keyword :gravity.type/bool]))]
      (doseq [mutate [#(assoc-in % [:context :ordering-core :function-records 0 :signature :parameters 0 :type-symbol] 'List)
                      #(assoc-in % [:context :ordering-bindings :parameter :kind] :var)
                      #(assoc-in % [:context :ordering-bindings (get-in input [:declaration :type-binding-id]) :namespace] 'other)
                      #(assoc-in % [:edge :callee-function-core-node-id] :forged)
                      #(assoc-in % [:context :ordering-core :function-records 0 :signature :parameters 0 :extra] true)]]
        (is (seq (:diagnostics (bare-list-parameter-constraints (mutate input)))))))))

(deftest sh08-map-parameter-recur-development-kernel
  (let [transfer {:argument-node-ids [:next]} target {:target-kind :function :binding-ids [:packet]}
        check #(invoke-c7 'sh08-ft-loop-kernel-transfer-types [transfer target {} {:next %} [:gravity.type/map] 65536])]
    (is (= :accepted (:status (check :gravity.type/map))))
    (is (= :accepted (:status (check :gravity.type/never))))
    (doseq [wrong [{:kind :gravity.type/map :shape :literal :member-types []} :gravity.type/list :gravity.type/string]]
      (is (= :function-recur-slot-type-mismatch (get-in (check wrong) [:issues 0 :reason]))))))

(deftest sh08-mixed-map-list-parameter-development-kernel
  (doseq [order [[:record :forms :parts :suffix] [:parts :suffix :forms :record] [:forms :parts :record :suffix]]]
    (let [{:keys [signature bindings types call core] :as input} (mixed-map-parameter-input order)
          result (bare-list-parameter-constraints input) row (first (:constraints result)) pairs (:rhs row)]
      (is (= [] (:diagnostics result)) (pr-str result))
      (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 4 bindings])))
      (is (= :sh08-map-list-parameter-compatibility-v1 (:compatibility-rule-version row)))
      (is (= 4 (count pairs)))
      (doseq [[index role pair] (map vector (range) order pairs)]
        (is (= role (:parameter pair)))
        (is (= (get types (get (:argument-node-ids call) index)) (:argument pair)))
        (is (= (get-in input [:seeds role]) (:chosen pair)))
        (if (= role :suffix)
          (is (nil? (:compatibility pair)))
          (let [premise (:compatibility pair)]
            (is (= index (:argument-index premise)))
            (is (= role (:parameter-binding-id premise)))
            (is (= (get (:argument-node-ids call) index) (:argument-node-id premise)))
            (is (= ({:record :sh08-bare-map-parameter-v1 :forms :sh08-bare-list-parameter-v1 :parts :sh08-homogeneous-list-parameter-v1} role)
                   (:version premise))))))
      (let [work (:compatibility-work-count result)]
        (is (= result (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] work))))
        (is (= "C7-GENERIC" (get-in (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] (dec work))) [:diagnostics 0 :rule]))))
      (let [index (.indexOf order :suffix) id (get (:argument-node-ids call) index)
            bad (assoc-in input [:types id] :gravity.type/bool)]
        (is (= :call-argument-type-mismatch (get-in (bare-list-parameter-constraints bad) [:diagnostics 0 :reason]))))
      (let [index (.indexOf order :record) id (get (:argument-node-ids call) index)
            stopped (bare-list-parameter-constraints (assoc-in input [:types id] :gravity.type/never))]
        (is (= [] (:diagnostics stopped)))
        (is (nil? (get-in stopped [:constraints 0 :rhs index :compatibility])))
        (is (= :sh08-list-parameter-compatibility-v2 (get-in stopped [:constraints 0 :compatibility-rule-version])))))))

(deftest sh08-bare-list-parameter-development-kernel
  (let [actual {:kind :gravity.type/list :element-types [:gravity.type/symbol :gravity.type/string] :shape :literal}
        {:keys [core call edge callee nodes bindings types signature declaration] :as input} (bare-list-parameter-input actual)
        constrained (bare-list-parameter-constraints input)
        row (first (:constraints constrained)) pair (first (:rhs row)) premise (:compatibility pair)
        facts (invoke-c7 'sh08-ft-call-facts-with-context [[call] [edge] nodes types {:callee callee} (:module core) {}])
        scan #(invoke-c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes bindings %])]
    (is (= [] (:diagnostics constrained)))
    (is (= :argument-compatibility (:kind row)))
    (is (= :sh08-bare-list-parameter-v1 (:compatibility-rule-version row) (:version premise)))
    (is (= actual (:argument pair) (:actual-type premise) (get-in facts [0 :argument-types 0]) (get-in input [:types :argument])))
    (is (= :gravity.type/list (:chosen pair) (:declared-type premise) (get-in constrained [:binding-types :parameter])))
    (is (not= (:argument pair) (:chosen pair)))
    (is (= [:positional-element-types :static-length] (:forgets premise)))
    (is (= [:value :provenance :schema :ownership :linearity :regions :profile :memory-obligations] (:preserves premise)))
    (is (= [0 :argument :parameter declaration] [(:argument-index premise) (:argument-node-id premise) (:parameter-binding-id premise) (:declaration premise)]))
    (is (= :accepted (:status (scan (:work-count (scan 65536))))))
    (is (= :list-parameter-work-limit (:reason (scan (dec (:work-count (scan 65536)))))))
    (let [work (:compatibility-work-count constrained)]
      (is (= constrained (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] work))))
      (is (= ["C7-GENERIC" :list-parameter-work-limit]
             ((juxt :rule :reason) (first (:diagnostics (bare-list-parameter-constraints (assoc-in input [:context :loop-work-left] (dec work)))))))))
    (doseq [value [:gravity.type/list :gravity.type/never]]
      (let [result (bare-list-parameter-constraints (bare-list-parameter-input value))]
        (is (= [] (:diagnostics result)))
        (is (= :equality (get-in result [:constraints 0 :kind])))
        (is (nil? (get-in result [:constraints 0 :rhs 0 :compatibility])))))
    (is (nil? (get-in (bare-list-parameter-constraints (bare-list-parameter-input :gravity.type/unknown))
                     [:constraints 0 :rhs 0 :compatibility])))
    (doseq [value [:gravity.type/string :gravity.type/vector :gravity.type/nil
                   {:kind :gravity.type/list :element-types [:gravity.type/unknown] :shape :literal}
                   (assoc actual :ownership :owned) (assoc actual :schema :secret)]]
      (is (seq (:diagnostics (bare-list-parameter-constraints (bare-list-parameter-input value))))))
    (doseq [mutate [#(assoc-in % [:context :ordering-core :function-records 0 :signature :parameters 0 :type-symbol] 'Vector)
                    #(assoc-in % [:context :ordering-bindings :parameter :binding-class] :namespace)
                    #(assoc-in % [:context :ordering-bindings (get-in input [:declaration :type-binding-id]) :name] 'Vector)
                    #(update-in % [:context :ordering-core :function-records 0] dissoc :signature)
                    #(assoc-in % [:edge :callee-function-core-node-id] :forged)]]
      (is (seq (:diagnostics (bare-list-parameter-constraints (mutate input))))))
    (is (true? (invoke-c7 'sh08-ft-signature-valid? [signature 1 bindings])))
    (is (nil? (invoke-c7 'sh08-ft-atomic-type-id ['List])))
    (is (false? (invoke-c7 'sh08-im-scalar-type? [:gravity.type/list])))
    (let [transfer {:argument-node-ids [:next :marks]}
          target {:target-kind :function :binding-ids [:items :marks]}
          check #(invoke-c7 'sh08-ft-loop-kernel-transfer-types
                    [transfer target {} {:next % :marks :gravity.type/string}
                     [:gravity.type/list :gravity.type/string] 65536])]
      (is (= :accepted (:status (check :gravity.type/list))))
      (is (= :accepted (:status (check :gravity.type/never))))
      (doseq [wrong [actual :gravity.type/vector :gravity.type/string]]
        (is (= :function-recur-slot-type-mismatch (get-in (check wrong) [:issues 0 :reason])))))
    (let [record (assoc (first (:function-records core)) :signature (assoc signature :return (assoc (:declaration input) :source-child-index 2)
                                                                                                :body-source-child-index 4))
          functions (invoke-c7 'sh08-ft-function-skeletons [[record] (:module core)])
          seeds (invoke-c7 'sh08-ft-declaration-seeds [functions {}])
          body {:node-id :body :core-form :reference :children [] :attributes {:binding-id :parameter}}
          function {:node-id :function :core-form :fn :children [:body] :source {:syntax-id :callee}}
          inferred (invoke-c7 'sh08-ft-infer-acyclic [[body function] {:body body :function function} [] [] [] [] functions seeds 8])]
      (is (= :converged (:convergence-status inferred)))
      (is (= :gravity.type/list (get-in inferred [:function-types :callee :return]) (get-in inferred [:type-table :body]))))))

(deftest sh08-list-empty-development-kernel
  (doseq [type [:gravity.type/list
                {:kind :gravity.type/list :element-types [] :shape :literal}
                {:kind :gravity.type/list :element-types [:gravity.type/nil :gravity.type/bool] :shape :literal}
                :gravity.type/never]]
    (let [result (list-query-derived (list-query-input 'empty? [type])) descriptor (:descriptor result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= (if (= type :gravity.type/never) :gravity.type/never :gravity.type/bool) (:type result)))
      (is (= [] (:direct-effects descriptor) (:latent-effects descriptor)))
      (is (= :zero-elements (get-in descriptor [:query-policy :test])))
      (is (= :stored-emptiness-test (get-in descriptor [:storage-policy :empty?])))))
  (doseq [type [nil :gravity.type/unknown :gravity.type/nil :gravity.type/vector :gravity.type/string]]
    (is (= :rejected (:status (list-query-derived (list-query-input 'empty? [type]))))))
  (doseq [arguments [[] [:gravity.type/list :gravity.type/list]]]
    (is (= :list-query-arity-mismatch (:reason (list-query-derived (list-query-input 'empty? arguments)))))))

(defn- list-construction-input [argument-types]
  (let [input (integral-ordering-input 'list argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- list-construction-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-lc-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-list-construction-development-kernel
  (let [s :gravity.type/string nv :gravity.type/never
        nested {:kind :gravity.type/list :element-types [s {:kind :gravity.type/vector :element-types [:gravity.type/nil] :shape :literal}] :shape :literal}
        cases [[] [s] [:gravity.type/nil :gravity.type/bool s s :gravity.type/symbol :gravity.type/character :gravity.type/integer]
               [:gravity.type/map :gravity.type/vector :gravity.type/list :gravity.type/set]
               [nested {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/bool] :shape :literal}
                {:kind :gravity.type/set :element-types [] :shape :literal}]
               [nv] [nv s] [s nv] [nv nv]]]
    (doseq [arguments cases]
      (let [{:keys [core node call edge nodes forms bindings types products binding-types] :as input} (list-construction-input arguments)
            result (list-construction-derived input) descriptor (:descriptor result)
            normal? (not-any? #{nv} arguments)
            expected (if normal? {:kind :gravity.type/list :element-types arguments :shape :literal} nv)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= (if normal? :normal :not-invoked) (:application descriptor)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= (if normal? [:memory/allocate] []) (:direct-effects descriptor)))
        (is (= {:kind :evaluated-list-construction :operand-node-ids (:argument-node-ids call)} (:value-origin descriptor)))
        (is (not (contains? (:value-origin descriptor) :quote-core-node-id)))
        (is (= :normal-invocation-even-empty (get-in descriptor [:construction-policy :allocation])))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= expected (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-lc-constraint [descriptor])] (:constraints inferred)))
        (is (true? (invoke-c7 'sh08-lc-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator descriptor)])))
        (doseq [changed [(assoc descriptor :latent-effects [])
                         (assoc-in descriptor [:value-origin :kind] :quoted-data)
                         (assoc-in descriptor [:construction-policy :allocation] :none)
                         (assoc descriptor :return :gravity.type/bool :result-type :gravity.type/bool)]]
          (is (false? (invoke-c7 'sh08-lc-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator changed)]))))
        (let [work (:work-count result)
              derive #(invoke-c7 'sh08-lc-derive [core node call edge nodes bindings types forms %])]
          (is (= result (derive work)))
          (is (= :list-construction-work-limit (:reason (derive (dec work))))))))
    (doseq [bad [nil :gravity.type/unknown :gravity.type/function :host/object
                  {:kind :gravity.type/vector :element-types [:gravity.type/never] :shape :literal}
                  {:kind :gravity.type/map :member-types [s] :shape :literal}
                  {:kind :gravity.type/list :element-types [s]}
                  {:kind :gravity.type/list :element-types [s] :shape :literal :quote-id :forged}
                  {:kind :gravity.type/set :element-types (list s) :shape :literal}]
            arguments [[bad] [nv bad] [bad nv]]]
      (let [result (list-construction-derived (list-construction-input arguments))]
        (is (= :rejected (:status result)) (pr-str arguments))
        (is (pos? (:work-count result)))))
    (let [input (list-construction-input [s s])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :right-first)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg1 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'vector)]]
        (is (= :rejected (:status (list-construction-derived (mutate input)))))))
    (doseq [type [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64 :gravity.type/u8 :gravity.type/u16
                  :gravity.type/u32 :gravity.type/u64 :gravity.type/f32 :gravity.type/f64 :gravity.type/decimal :gravity.type/ratio]]
      (is (= :accepted (:status (list-construction-derived (list-construction-input [type]))))))
    (is (= :accepted (:status (list-construction-derived (list-construction-input (vec (repeat 1023 s)))))))
    (is (= :list-construction-arity-mismatch (:reason (list-construction-derived (list-construction-input (vec (repeat 1024 s)))))))
    (let [check #(invoke-c7 'sh08-lc-type-work [%1 %2])
          width {:kind :gravity.type/list :element-types (vec (repeat 1024 s)) :shape :literal}
          chain #(nth (iterate (fn [t] {:kind :gravity.type/list :element-types [t] :shape :literal}) s) %)]
      (is (= {:status :accepted :work-count 1025} (check [width] 1025)))
      (is (= {:status :rejected :reason :work-limit :work-count 1024} (check [width] 1024)))
      (is (= :rejected (:status (check [(assoc width :element-types (vec (repeat 1025 s)))] 65536))))
      (is (= :accepted (:status (check [(chain 128)] 65536))))
      (is (= :type-depth (:reason (check [(chain 129)] 65536))))
      (let [wide {:kind :gravity.type/list :element-types (vec (repeat 1024 width)) :shape :literal}
            rejected (check [wide] 4000)]
        (is (= :rejected (:status rejected)))
        (is (= 4000 (:work-count rejected)))))))

(defn- string-concat-input [argument-types]
  (let [input (integral-ordering-input 'str argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- string-concat-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-sc-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-string-concat-development-kernel
  (let [s :gravity.type/string nv :gravity.type/never]
    (doseq [arguments [[s s] [nv s] [s nv] [nv nv]]]
      (let [{:keys [core node call edge nodes forms bindings types products binding-types] :as input} (string-concat-input arguments)
            result (string-concat-derived input) descriptor (:descriptor result)
            normal? (= arguments [s s]) expected (if normal? s nv)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= (if normal? :normal :not-invoked) (:application descriptor)))
        (is (= [:memory/allocate] (:latent-effects descriptor)))
        (is (= (if normal? [:memory/allocate] []) (:direct-effects descriptor)))
        (is (= [] (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= 'gravity.text (:semantic-owner descriptor)))
        (is (= {:domain :binary-strings :order :left-then-right :scalars :exact-sequence-concatenation :normalization :none
                :coercion :none :formatting :none :operand-evaluation :once :allocation :normal-invocation-even-empty}
               (:concatenation-policy descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= expected (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-sc-constraint [descriptor])] (:constraints inferred)))
        (is (true? (invoke-c7 'sh08-sc-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator descriptor)])))
        (is (false? (invoke-c7 'sh08-sc-inferred-call-valid?
                      [core node call edge nodes bindings (assoc types :operator (assoc descriptor :latent-effects []))])))))
    (doseq [bad [nil :gravity.type/unknown :gravity.type/nil :gravity.type/bool :gravity.type/integer
                  :gravity.type/character :gravity.type/symbol :gravity.type/keyword
                  {:kind :gravity.type/map :member-types []} {:kind :gravity.type/vector :element-types []}]
            arguments [[bad s] [s bad] [bad nv] [nv bad]]]
      (is (= :rejected (:status (string-concat-derived (string-concat-input arguments)))) (pr-str arguments)))
    (doseq [arguments [[] [s] [s s s] [s s s s]]]
      (is (= :string-concat-arity-mismatch (:reason (string-concat-derived (string-concat-input arguments))))))
    (let [input (string-concat-input [s s])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :right-first)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg1 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'format)]]
        (is (= :rejected (:status (string-concat-derived (mutate input))))))
      (let [{:keys [core node call edge nodes bindings types forms]} input work (:work-count (string-concat-derived input))
            derive #(invoke-c7 'sh08-sc-derive [core node call edge nodes bindings types forms %])]
        (is (= 66 work))
        (is (= :accepted (:status (derive work))))
        (is (= :string-concat-work-limit (:reason (derive (dec work)))))))))

(defn- map-get-input [argument-types]
  (let [input (integral-ordering-input 'get argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- map-get-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-mg-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-map-get-development-kernel
  (let [m #(hash-map :kind :gravity.type/map :member-types % :shape :independently-established)
        s :gravity.type/string k :gravity.type/keyword b :gravity.type/bool n :gravity.type/nil nv :gravity.type/never
        vector-type {:kind :gravity.type/vector :element-types [b] :shape :literal}
        mixed (m [s s k b])]
    (doseq [[arguments expected] [[[(m []) s s] s] [[(m [s s]) s s] s] [[(m [k b]) k b] b]
                                  [[(m [s n]) s n] n] [[mixed s s] s] [[mixed k b] b]
                                  [[(m [:gravity.type/integer b s s]) s s] s]
                                  [[(m [s vector-type]) s vector-type] vector-type]
                                  [[:gravity.type/map s nv] nv] [[:gravity.type/map nv s] nv]
                                  [[(m [s s s b]) s nv] nv] [[nv s s] nv]
                                  [[mixed nv s] nv] [[nv nv nv] nv]]]
      (let [{:keys [core nodes forms bindings products binding-types] :as input} (map-get-input arguments)
            result (map-get-derived input) descriptor (:descriptor result)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= (if (= nv expected) :not-invoked :normal) (:application descriptor)))
        (is (= {:input :established-member-map :keys [:string :keyword] :key-equality :value
                :presence :independent-of-value :default :eager :normalization :none :coercion :none
                :operand-evaluation :map-then-key-then-default-once :result :one-established-type} (:lookup-policy descriptor)))
        (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= expected (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-mg-constraint [descriptor])] (:constraints inferred)))))
    (doseq [arguments [[:gravity.type/map s s] [[(m [s s])] s s]
                       [(m [s s s b]) s s] [(m [s s]) s b]
                       [nv b s] [nv s :gravity.type/unknown]
                       [:gravity.type/unknown s nv] [(m []) :gravity.type/unknown nv]
                       [:gravity.type/vector s nv] [(m []) :gravity.type/symbol nv]]]
      (is (= :rejected (:status (map-get-derived (map-get-input arguments)))) (pr-str arguments)))
    (doseq [bad [(m [s]) (m [s :gravity.type/unknown]) (m [s false]) (m [:gravity.type/unknown s])
                 (m [:bogus s]) (m [{:kind :bogus} s])
                 (assoc (m []) :member-types '()) {:kind :gravity.type/map}
                 (m (vec (repeat 1026 s)))]
            default [s nv]]
      (is (= :rejected (:status (map-get-derived (map-get-input [bad s default]))))))
    (doseq [arguments [[] [(m []) s] [(m []) s s s]]]
      (is (= :map-get-arity-mismatch (:reason (map-get-derived (map-get-input arguments))))))
    (let [input (map-get-input [(m [s s]) s s])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :lazy-default)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg2 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'contains?)]]
        (is (= :rejected (:status (map-get-derived (mutate input))))))
      (let [{:keys [core node call edge nodes bindings types forms]} input work (:work-count (map-get-derived input))
            derive #(invoke-c7 'sh08-mg-derive [core node call edge nodes bindings types forms %])]
        (is (= 69 work))
        (is (= :accepted (:status (derive work))))
        (is (= :map-get-work-limit (:reason (derive (dec work)))))))
    (let [input (map-get-input [(m (vec (repeat 1024 s))) s s])]
      (is (= :accepted (:status (map-get-derived input))))
      (is (= 1091 (:work-count (map-get-derived input)))))))

(defn- map-contains-input [argument-types]
  (let [input (integral-ordering-input 'contains? argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- map-contains-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-mc-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-map-contains-development-kernel
  ;; Normalized type DATA; Map membership needs no constructor/presence proof.
  (let [maps [:gravity.type/map {:kind :gravity.type/map :member-types [] :shape :literal}
              {:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/nil] :shape :independent}]
        keys [:gravity.type/string :gravity.type/keyword]]
    (doseq [map-type maps key-type keys]
      (let [{:keys [core nodes forms bindings products binding-types] :as input} (map-contains-input [map-type key-type])
            result (map-contains-derived input) descriptor (:descriptor result)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :gravity.type/bool (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= :normal (:application descriptor)))
        (is (= 'gravity.collections (:semantic-owner descriptor)))
        (is (= {:input :established-map :keys [:string :keyword] :key-equality :value :presence :independent-of-value
                :normalization :none :coercion :none :operand-evaluation :map-then-key-once :values :unobserved} (:membership-policy descriptor)))
        (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= :gravity.type/bool (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-mc-constraint [descriptor])] (:constraints inferred)))))
    (doseq [arguments [[:gravity.type/never :gravity.type/string] [:gravity.type/never :gravity.type/keyword]
                       [:gravity.type/map :gravity.type/never] [:gravity.type/never :gravity.type/never]]]
      (let [result (map-contains-derived (map-contains-input arguments))]
        (is (= :accepted (:status result)))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (get-in result [:descriptor :application])))))
    (doseq [type [nil :gravity.type/unknown :gravity.type/nil :gravity.type/bool :gravity.type/integer
                  :gravity.type/string :gravity.type/set {:kind :gravity.type/vector :element-types []}]
            key (conj keys :gravity.type/never)]
      (is (= :rejected (:status (map-contains-derived (map-contains-input [type key]))))))
    (doseq [type [nil :gravity.type/unknown :gravity.type/nil :gravity.type/bool :gravity.type/integer
                  :gravity.type/symbol :gravity.type/character :gravity.type/decimal {:kind :gravity.type/vector :element-types []}]
            map-type [:gravity.type/map :gravity.type/never]]
      (is (= :rejected (:status (map-contains-derived (map-contains-input [map-type type]))))))
    (doseq [arguments [[] [:gravity.type/map] [:gravity.type/map :gravity.type/string :gravity.type/string]]]
      (is (= :map-contains-arity-mismatch (:reason (map-contains-derived (map-contains-input arguments))))))
    (let [input (map-contains-input [:gravity.type/map :gravity.type/string])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :effects] [:io/write])
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :key-first)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg0 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'get)]]
        (is (= :rejected (:status (map-contains-derived (mutate input))))))
      (let [{:keys [core node call edge nodes bindings types forms]} input work (:work-count (map-contains-derived input))
            derive #(invoke-c7 'sh08-mc-derive [core node call edge nodes bindings types forms %])]
        (is (= 66 work))
        (is (= :accepted (:status (derive work))))
        (is (= :map-contains-work-limit (:reason (derive (dec work)))))))))

(defn- not-input [argument-types]
  (let [input (integral-ordering-input 'not argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- not-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-nt-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-not-development-kernel
  ;; Independently supplied normalized type DATA exercises no source admission.
  (let [types [:gravity.type/nil :gravity.type/bool :gravity.type/character :gravity.type/string
               :gravity.type/symbol :gravity.type/keyword :gravity.type/integer :gravity.type/i64 :gravity.type/decimal
               {:kind :gravity.type/vector :element-types [] :shape :literal}
               {:kind :gravity.type/map :member-types [] :shape :literal}
               :gravity.type/list :gravity.type/set :gravity.type/function :gravity.type/never]]
    (doseq [type types]
      (let [{:keys [core nodes forms bindings products binding-types] :as input} (not-input [type])
            result (not-derived input) descriptor (:descriptor result)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])
            expected (if (= :gravity.type/never type) :gravity.type/never :gravity.type/bool)]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= (if (= :gravity.type/never type) :not-invoked :normal) (:application descriptor)))
        (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= {:falsy-values [nil false] :other-values :truthy :operation :complement
                :operand-evaluation :once :operand-position :non-tail} (:truthiness-policy descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= expected (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-nt-constraint [descriptor])] (:constraints inferred)))))
    (doseq [arguments [[] [:gravity.type/bool :gravity.type/bool]]]
      (is (= :not-arity-mismatch (:reason (not-derived (not-input arguments))))))
    (doseq [type [nil :gravity.type/unknown]]
      (is (= :not-operand-type-required (:reason (not-derived (not-input [type]))))))
    (let [input (not-input [:gravity.type/string])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :effects] [:io/write])
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :operand-first)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg0 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'not=)]]
        (is (= :rejected (:status (not-derived (mutate input))))))
      (let [{:keys [core node call edge nodes bindings types forms]} input work (:work-count (not-derived input))
            derive #(invoke-c7 'sh08-nt-derive [core node call edge nodes bindings types forms %])]
        (is (= 65 work))
        (is (= :accepted (:status (derive work))))
        (is (= :not-work-limit (:reason (derive (dec work)))))))))

(defn- atomic-equality-input [argument-types]
  (let [input (integral-ordering-input '= argument-types)]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- atomic-equality-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-ae-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-atomic-equality-development-kernel
  (let [families [:gravity.type/nil :gravity.type/bool :gravity.type/character :gravity.type/string :gravity.type/symbol :gravity.type/keyword]]
    (doseq [left families right families]
      (let [{:keys [core nodes forms bindings products binding-types] :as input} (atomic-equality-input [left right])
            result (atomic-equality-derived input) descriptor (:descriptor result)
            context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                     :lookup-bindings (vec (vals bindings)) :shape-call-products products}
            inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                       [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :gravity.type/bool (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
        (is (= {:domain [:nil :boolean :character :string :symbol :keyword] :same-family :value :cross-family :false
                :identity :unobserved :strings :exact-valid-scalar-sequence :normalization :none :locale :none :numeric :unsupported}
               (:equality-policy descriptor)))
        (is (= :converged (:convergence-status inferred)))
        (is (= [] (:diagnostics inferred)))
        (is (= descriptor (get-in inferred [:type-table :operator])))
        (is (= :gravity.type/bool (get-in inferred [:type-table :call])))
        (is (= [(invoke-c7 'sh08-ae-constraint [descriptor])] (:constraints inferred)))))
    (doseq [type families order [[type :gravity.type/never] [:gravity.type/never type] [:gravity.type/never :gravity.type/never]]]
      (let [result (atomic-equality-derived (atomic-equality-input order))]
        (is (= :accepted (:status result)))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (get-in result [:descriptor :application])))))
    (doseq [unsupported [:gravity.type/unknown :gravity.type/integer :gravity.type/i64 :gravity.type/decimal
                         {:kind :gravity.type/vector :element-types [] :shape :literal}
                         {:kind :gravity.type/map :member-types [] :shape :literal}
                         :gravity.type/function :host/object]
            pair [[unsupported :gravity.type/string] [:gravity.type/string unsupported] [unsupported :gravity.type/never]]]
      (is (= :rejected (:status (atomic-equality-derived (atomic-equality-input pair))))))
    (let [input (atomic-equality-input [:gravity.type/string :gravity.type/string])]
      (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :effects] [:io/write])
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :evaluation-order] :right-first)
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:forms :arg0 :parent-form-id] :sibling)
                      #(assoc-in % [:forms :operator :value] 'identical?)]]
        (is (= :rejected (:status (atomic-equality-derived (mutate input))))))
      (is (= :atomic-equality-arity-mismatch (:reason (atomic-equality-derived (assoc-in input [:call :argument-node-ids] [:arg0])))))
      (let [{:keys [core node call edge nodes bindings types forms]} input work (:work-count (atomic-equality-derived input))
            derive #(invoke-c7 'sh08-ae-derive [core node call edge nodes bindings types forms %])]
        (is (= :accepted (:status (derive work))))
        (is (= :atomic-equality-work-limit (:reason (derive (dec work)))))))))

(defn- integral-ordering-descriptor [{:keys [core node call edge nodes bindings types module]}]
  (invoke-c7 'sh08-io-descriptor [core node call edge nodes bindings types module]))

(defn- map-association-input [key value-type empty?]
  (let [module {:namespace 'example.association :profile :meta :target :jvm :effects [:memory/allocate] :capabilities []}
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        binding (-> (get-in (integral-ordering-input '< []) [:bindings :ordering-binding])
                    (assoc :name 'assoc :binding-id :association-binding :definition-syntax-id :association-definition))
        input-children (if empty? [] [:input-key :input-value])
        children [:operator :input :key :value]
        node (fn [id kind ids attrs]
               {:node-id id :core-form kind :children ids :attributes attrs :source (source id)
                :resolved-binding-ids [] :evaluation {:kind :left-to-right :order (mapv #(hash-map :index %1 :core-node-id %2) (range) ids)}})
        operator (assoc (node :operator :reference [] {:symbol 'assoc :position :operator :binding-id :association-binding
                                                       :binding-class :core :definition-syntax-id :association-definition})
                        :resolved-binding-ids [:association-binding])
        input (node :input :collection-literal input-children {:literal-kind :map :element-count (count input-children)})
        input-key (node :input-key :literal [] {:literal-kind :keyword :value :old})
        input-value (node :input-value :literal [] {:literal-kind :integer :value 7})
        key-node (node :key :literal [] {:literal-kind :keyword :value key})
        value-node (node :value :reference [] {:binding-id :value-binding})
        call-node (-> (node :association :call children {:operator-child-index 0 :argument-count 3 :argument-child-indexes [1 2 3]
                                                        :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                      (assoc :resolved-binding-ids [:association-binding])
                      (assoc-in [:evaluation :kind] :operator-then-arguments))
        nodes (vec (concat [operator] (when-not empty? [input-key input-value]) [input key-node value-node call-node]))
        call {:core-node-id :association :operator-node-id :operator :operator-binding-id :association-binding
              :argument-node-ids [:input :key :value] :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :association :caller-function-syntax-id nil :caller-function-core-node-id nil
              :callee-binding-id :association-binding :callee-definition-syntax-id :association-definition
              :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids [:input :key :value]
              :ordered-evaluation-node-ids children :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        core {:module module :nodes nodes :function-records [] :lexical-bindings [] :definitions [] :calls [call] :call-edges [edge] :recursion-components []}
        forms [(form :association :list (list 'assoc {} key 'value) nil children)
               (form :operator :symbol 'assoc :association []) (form :input :map (if empty? {} {:old 7}) :association input-children)
               (form :input-key :keyword :old :input []) (form :input-value :integer 7 :input [])
               (form :key :keyword key :association []) (form :value :symbol 'value :association [])]
        types {:input-key :gravity.type/keyword :input-value :gravity.type/integer :key :gravity.type/keyword :value value-type
               :input {:kind :gravity.type/map :member-types (if empty? [] [:gravity.type/keyword :gravity.type/integer]) :shape :literal}}
        bindings {:association-binding binding :value-binding {:binding-id :value-binding :binding-class :lexical :name 'value}}
        products {:calls [call] :call-edges [edge] :definitions [] :function-index {} :recursion-components [] :module module}]
    {:module module :core core :node call-node :nodes (into {} (map (juxt :node-id identity) nodes)) :call call :edge edge
     :forms (into {} (map (juxt :form-id identity) forms)) :bindings bindings :types types :products products
     :binding-types {:value-binding value-type}}))

(defn- map-association-derived [{:keys [core node nodes types binding-types bindings forms products]}]
  (let [context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)]
    (invoke-c7 'sh08-ma-derive [node nodes types binding-types {} [] bindings forms #{} 0 {} context])))

(defn- chained-map-association-input []
  (let [base (map-association-input :new :gravity.type/string true)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        children [:outer-operator :association :outer-key :outer-value]
        operator (-> (get-in base [:nodes :operator]) (assoc :node-id :outer-operator :source (source :outer-operator)))
        key-node (-> (get-in base [:nodes :key]) (assoc :node-id :outer-key :source (source :outer-key)))
        value-node (-> (get-in base [:nodes :value]) (assoc :node-id :outer-value :source (source :outer-value)
                                                          :attributes {:binding-id :outer-value-binding}))
        node (-> (:node base) (assoc :node-id :outer-association :children children :source (source :outer-association))
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :core-node-id :outer-association :operator-node-id :outer-operator
                    :argument-node-ids (subvec children 1) :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :ordinal 1 :call-core-node-id :outer-association
                    :argument-core-node-ids (subvec children 1) :ordered-evaluation-node-ids children)
        core (-> (:core base) (update :nodes #(vec (concat [operator] % [key-node value-node node])))
                 (update :calls conj call) (update :call-edges conj edge))
        form (fn [id kind value ids] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                      :parent-form-id (when-not (= id :outer-association) :outer-association)
                                      :kind kind :value value :child-form-ids ids})]
    (-> base (assoc :core core :node node :call call :edge edge)
        (update :nodes merge {:outer-operator operator :outer-key key-node :outer-value value-node :outer-association node})
        (update :forms merge {:outer-association (form :outer-association :list '(assoc inner :new value) children)
                              :outer-operator (form :outer-operator :symbol 'assoc [])
                              :outer-key (form :outer-key :keyword :new []) :outer-value (form :outer-value :symbol 'next-value [])})
        (assoc-in [:forms :association :parent-form-id] :outer-association)
        (update :types merge {:association (:type (map-association-derived base)) :outer-key :gravity.type/keyword :outer-value :gravity.type/bool})
        (assoc-in [:binding-types :outer-value-binding] :gravity.type/bool)
        (assoc-in [:bindings :outer-value-binding] {:binding-id :outer-value-binding :binding-class :lexical :name 'next-value})
        (update :products assoc :calls (:calls core) :call-edges (:call-edges core)))))

(defn- map-removal-input [key empty?]
  (let [base (walk/postwalk-replace {:association :removal :association-binding :removal-binding
                                   :association-definition :removal-definition 'assoc 'dissoc}
                                  (map-association-input key :gravity.type/bool empty?))
        children [:operator :input :key]
        node (-> (:node base) (assoc :children children)
                 (assoc-in [:attributes :argument-count] 2) (assoc-in [:attributes :argument-child-indexes] [1 2])
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [:input :key] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids [:input :key] :ordered-evaluation-node-ids children)
        core (-> (:core base) (assoc :calls [call] :call-edges [edge])
                 (update :nodes #(mapv (fn [n] (if (= :removal (:node-id n)) node n)) (remove (fn [n] (= :value (:node-id n))) %))))
        forms (-> (:forms base) (dissoc :value)
                  (assoc-in [:removal :child-form-ids] children)
                  (assoc-in [:removal :value] (list 'dissoc (get-in base [:forms :input :value]) key)))]
    (assoc base :node node :call call :edge edge :core core :forms forms :types (dissoc (:types base) :value)
                :nodes (into {} (map (juxt :node-id identity) (:nodes core)))
                :bindings (dissoc (:bindings base) :value-binding) :binding-types {}
                :products (assoc (:products base) :calls [call] :call-edges [edge]))))

(defn- map-removal-derived
  ([input] (map-removal-derived input 65536))
  ([{:keys [core node nodes types binding-types bindings forms products]} budget]
   (let [context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core :budget-left budget)]
     (invoke-c7 'sh08-mr-derive [node nodes types binding-types {} [] bindings forms #{} 0 {} context]))))

(defn- map-family-removal-input [key never-key? never-input?]
  (let [base (map-removal-input key false)
        input (-> (get-in base [:nodes :input])
                  (assoc :core-form :reference :children [] :resolved-binding-ids [:map-parameter]
                         :attributes {:binding-id :map-parameter})
                  (assoc-in [:evaluation :order] []))
        key-node (if never-key?
                   (-> (get-in base [:nodes :key])
                       (assoc :core-form :reference :resolved-binding-ids [:key-parameter]
                              :attributes {:binding-id :key-parameter})) (get-in base [:nodes :key]))
        nodes (-> (:nodes base) (dissoc :input-key :input-value) (assoc :input input :key key-node))
        node-list (mapv nodes (remove #{:input-key :input-value} (map :node-id (get-in base [:core :nodes]))))
        forms (-> (:forms base) (dissoc :input-key :input-value)
                  (assoc-in [:input :kind] :symbol) (assoc-in [:input :value] 'packet) (assoc-in [:input :child-form-ids] []))
        forms (if never-key? (-> forms (assoc-in [:key :kind] :symbol) (assoc-in [:key :value] 'aborts)) forms)
        receiver-type (if never-input? :gravity.type/never :gravity.type/map)
        key-type (if never-key? :gravity.type/never :gravity.type/keyword)]
    (assoc base :nodes nodes :forms forms :core (assoc (:core base) :nodes node-list)
                :types {:input receiver-type :key key-type}
                :binding-types (cond-> {:map-parameter receiver-type} never-key? (assoc :key-parameter key-type))
                :bindings (cond-> (assoc (:bindings base) :map-parameter {:binding-id :map-parameter :binding-class :lexical :kind :local :name 'packet})
                            never-key? (assoc :key-parameter {:binding-id :key-parameter :binding-class :lexical :kind :local :name 'aborts})))))

(deftest sh08-map-family-removal-development-kernel
  (doseq [never-key? [false true] never-input? [false true]]
    (let [{:keys [core node nodes bindings types forms products binding-types call edge] :as input}
          (map-family-removal-input :removed never-key? never-input?)
          result (map-removal-derived input) descriptor (:descriptor result)
          expected (if (or never-key? never-input?) :gravity.type/never :gravity.type/map)
          work (:call-join-work result) table (assoc types :operator descriptor :removal expected)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                   :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                     [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= expected (:type result) (:result-type descriptor)))
      (is (= (if (= expected :gravity.type/never) :not-invoked :normal) (:application descriptor)))
      (is (= (if (= expected :gravity.type/never) [] [:memory/allocate]) (:direct-effects descriptor)))
      (is (= [:memory/allocate] (:latent-effects descriptor)))
      (is (nil? (:input-shape descriptor)))
      (is (nil? (:transition descriptor)))
      (is (nil? (:result-shape descriptor)))
      (is (= result (map-removal-derived input work)))
      (is (= :map-removal-work-limit (:reason (map-removal-derived input (dec work)))))
      (is (true? (invoke-c7 'sh08-mr-inferred-call-valid? [core node call edge nodes bindings table])))
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)) (pr-str (:diagnostics inferred)))
      (is (= descriptor (get-in inferred [:type-table :operator])))
      (is (= expected (get-in inferred [:type-table :removal])))
      (if never-input?
        (is (nil? (:input-type-evidence descriptor)))
        (do
          (is (= {:kind :map-family-type-only :type :gravity.type/map :node-id :input :source (get-in nodes [:input :source])}
                 (:input-type-evidence descriptor)))
          (is (nil? (:shape result)))
          (let [valid #(invoke-c7 'sh08-mr-inferred-call-valid? [core node call edge nodes bindings (assoc table :operator %)])]
            (doseq [changed [(assoc-in descriptor [:input-type-evidence :kind] :constructor)
                             (assoc-in descriptor [:input-type-evidence :type] :gravity.type/list)
                             (assoc-in descriptor [:input-type-evidence :source :syntax-id] :sibling)
                             (assoc descriptor :input-shape {:status :accepted :shape-kind :map :parts []})
                             (assoc descriptor :result-shape {:status :accepted :shape-kind :map :parts []})
                             (assoc descriptor :transition {:kind :absent :parts []})]]
              (is (false? (valid changed)))))))
      (when (= expected :gravity.type/map)
        (let [prepared (assoc (invoke-c7 'sh08-sm-call-context [products table 65536]) :source-core core :module (:module core))
              shape (invoke-c7 'sh08-sm-shape-with-bindings [:removal nodes table binding-types {} [] bindings forms #{} 0 {} prepared])]
          (is (= :rejected (:status shape)))
          (is (= :map-removal-map-shape-required (:reason shape)))))))
  (let [{:keys [core node call edge nodes bindings types forms]} (list-query-input 'count [:gravity.type/map])
        count-result (invoke-c7 'sh08-cq-derive [core node call edge nodes bindings types forms 65536])
        contains-result (map-contains-derived (map-contains-input [:gravity.type/map :gravity.type/keyword]))]
    (is (= :accepted (:status count-result) (:status contains-result)))
    (is (= :gravity.type/integer (:type count-result)))
    (is (= :gravity.type/bool (:type contains-result)))
    (is (= [] (get-in count-result [:descriptor :direct-effects]) (get-in contains-result [:descriptor :direct-effects])))))

(deftest sh08-map-removal-source-development-kernel
  (doseq [[key empty? kind expected] [[:old false :remove []] [:missing false :absent [:old]] [:missing true :absent []]]]
    (let [{:keys [module core node nodes types bindings forms] :as input} (map-removal-input key empty?)
          result (map-removal-derived input) descriptor (:descriptor result) change (:transition descriptor)
          work (:call-join-work result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :sh08-map-removal-v1 (:version descriptor)))
      (is (= 'dissoc (:operation descriptor)))
      (is (= :normal (:application descriptor)))
      (is (= kind (:kind change)))
      (is (= expected (mapv :key (:parts change))))
      (is (= (:type result) (:result-type descriptor) (get-in descriptor [:result-shape :type])))
      (is (= [:memory/allocate] (:direct-effects descriptor) (:latent-effects descriptor)))
      (is (= [] (:capabilities descriptor) (:thrown-error-effects descriptor)))
      (is (= :possible (get-in descriptor [:allocation-policy :present-key]) (get-in descriptor [:allocation-policy :absent-key])
             (get-in descriptor [:allocation-policy :empty-map])))
      (is (= :preserved (get-in descriptor [:storage-policy :input])))
      (is (= :persistent-map-removal (get-in descriptor [:result-shape :constructor-origin])))
      (is (= result (map-removal-derived input work)))
      (is (= :map-removal-work-limit (:reason (map-removal-derived input (dec work)))))
      (is (= :direct-map-removal (:dispatch (invoke-c7 'sh08-mr-call-fact [descriptor module]))))
      (is (= descriptor (:dependencies (invoke-c7 'sh08-mr-constraint [descriptor]))))
      (doseq [changed [(assoc-in input [:forms :key :parent-form-id] :sibling)
                       (assoc-in input [:forms :key :value] :wrong)
                       (assoc-in input [:forms :operator :value] 'other/dissoc)
                       (assoc-in input [:nodes :key :source :syntax-id] :forged)
                       (assoc-in input [:node :evaluation :order] [])
                       (assoc-in input [:bindings :removal-binding :binding-class] :import)
                       (assoc-in input [:bindings :removal-binding :package :version] "forged")
                       (assoc-in input [:types :key] :gravity.type/string)
                       (assoc-in input [:types :input] :gravity.type/map)]]
        (is (= :rejected (:status (map-removal-derived changed)))))
      (doseq [type [nil :gravity.type/unknown]]
        (when-not empty?
          (let [changed (-> input (assoc-in [:types :input-value] type) (assoc-in [:types :input :member-types 1] type))]
            (is (= :rejected (:status (map-removal-derived changed)))))))
      (doseq [budget [0 -1]]
        (is (= :map-removal-work-limit (:reason (map-removal-derived input budget)))))))
  (let [input (map-removal-input :old false)]
    (doseq [args [nil {} 7 [:input] [:input :key :extra]]]
      (is (= :rejected (:status (map-removal-derived (assoc-in input [:products :calls 0 :argument-node-ids] args))))))))

(deftest sh08-map-removal-inference-development-kernel
  ;; Normalized source/type DATA exercises C7 ownership without public admission.
  (doseq [[key empty?] [[:old false] [:missing false] [:missing true]]]
    (let [{:keys [core node nodes forms bindings products binding-types types call edge] :as input} (map-removal-input key empty?)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                   :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                     [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])
          expected (map-removal-derived input) descriptor (:descriptor expected)
          table (:type-table inferred)
          valid #(invoke-c7 'sh08-mr-inferred-call-valid? [core node call edge nodes bindings %])]
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)) (pr-str (:diagnostics inferred)))
      (is (= descriptor (:operator table)))
      (is (= (:type expected) (:removal table)))
      (is (= [(invoke-c7 'sh08-mr-constraint [descriptor])] (:constraints inferred)))
      (is (true? (valid table)))
      (doseq [changed [(assoc-in table [:operator :result-type] :gravity.type/map)
                       (assoc-in table [:operator :allocation-policy :absent-key] :none)
                       (assoc-in table [:operator :storage-policy :input] :consumed)
                       (assoc-in table [:operator :input-shape :call-join-work] "forged")
                       (assoc-in table [:operator :input-shape :call-join-work] -1)
                       (assoc-in table [:operator :input-shape :call-join-work] nil)
                       (assoc-in table [:operator :transition :kind] :forged)
                       (assoc-in table [:operator :source-binding :package :version] "forged")
                       (assoc table :removal :gravity.type/map)]]
        (is (false? (valid changed))))
      (let [prepared (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
            step-context (assoc context :ordering-operators {(:operator-node-id call) call}
                               :ordering-edges {(:call-core-node-id edge) edge}
                               :ordering-bindings bindings :association-forms forms :shape-call-context prepared)
            step #(invoke-c7 'sh08-mr-step [node % binding-types {} step-context %2])
            result (step types 65536) work (:work-count result)]
        (is (= :accepted (:status result)))
        (is (= descriptor (:descriptor result)))
        (is (= result (step types work)))
        (is (= :rejected (:status (step types (dec work)))))
        (doseq [id [:input :key]]
          (let [unknown (assoc types id :gravity.type/unknown)]
            (is (= {:status :deferred :type :gravity.type/unknown :work-count 2} (step unknown 2)))
            (doseq [budget [1 0 -1]]
              (let [rejected (step unknown budget)]
                (is (= :rejected (:status rejected)))
                (is (= :map-removal-work-limit (:reason rejected)))
                (is (= 2 (:work-count rejected))))))))))
  (doseq [ids [[:input] [:key] [:input :key]]]
    (let [base (map-removal-input :old false)
          input (reduce
                  (fn [data id]
                    (-> data
                        (assoc-in [:types id] :gravity.type/never)
                        (assoc-in [:nodes id :core-form] :reference)
                        (assoc-in [:nodes id :children] [])
                        (assoc-in [:nodes id :attributes] {:binding-id id})
                        (assoc-in [:forms id :kind] :symbol)
                        (assoc-in [:forms id :value] 'aborts)
                        (assoc-in [:forms id :child-form-ids] [])))
                  base ids)
          result (map-removal-derived input) descriptor (:descriptor result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :gravity.type/never (:type result)))
      (is (= :not-invoked (:application descriptor)))
      (is (= [] (:direct-effects descriptor)))
      (is (= [:memory/allocate] (:latent-effects descriptor)))
      (is (nil? (:transition descriptor)))
      (is (nil? (:result-shape descriptor)))
      (is (= (when-not (some #{:input} ids) :map) (get-in descriptor [:input-shape :shape-kind]))))))

(defn- map-removal-shape [keys]
  (let [parts (mapv (fn [index key]
                      {:coordinate (inc (* 2 index)) :key key :key-node-id [:key index] :value-node-id [:value index]
                       :shape {:status :accepted :shape-kind :scalar :type (if (even? index) :gravity.type/bool :gravity.type/nil)
                               :node-id [:value index] :source {:form-id [:original index]}
                               :domain {:values [(when (even? index) false)]}}}) (range) keys)]
    {:status :accepted :shape-kind :map :node-id :original-map :source {:form-id :original-map}
     :parts parts :type {:kind :gravity.type/map :shape :literal
                        :member-types (vec (mapcat #(vector :gravity.type/keyword (get-in % [:shape :type])) parts))}
     :shape-size (inc (* 2 (count parts)))}))

(deftest sh08-map-removal-transition-development-kernel
  (let [transition #(invoke-c7 'sh08-mr-transition [%1 %2 %3])]
    (doseq [[keys key expected kind] [[[] :missing [] :absent]
                                    [[:left] :left [] :remove] [[:left] :missing [:left] :absent]
                                    [[:left :middle :right] :left [:middle :right] :remove]
                                    [[:left :middle :right] :middle [:left :right] :remove]
                                    [[:left :middle :right] :right [:left :middle] :remove]
                                    [[:actual-path :semantic :actual-source-path] :actual-path [:semantic :actual-source-path] :remove]]]
      (let [input (map-removal-shape keys) original input result (transition input key 65536)
            change (:transition result) parts (:parts change)]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= kind (:kind change)))
        (is (= expected (mapv :key parts)))
        (is (= (mapv #(inc (* 2 %)) (range (count parts))) (mapv :coordinate parts)))
        (is (= input original))
        (is (= result (transition input key (:work-count result))))
        (is (= :map-removal-work-limit (:reason (transition input key (dec (:work-count result))))))
        (is (= (count keys) (:input-entry-count change)))
        (is (= (count expected) (:output-entry-count change)))
        (doseq [[part link] (map vector parts (:survivors change))]
          (is (= (nth (:parts input) (:input-entry-index link)) (:source-part link)))
          (is (= part (assoc (:source-part link) :coordinate (:output-coordinate link))))
          (is (= (:coordinate (:source-part link)) (:input-coordinate link)))
          (is (identical? (:shape part) (get-in link [:source-part :shape]))))
        (when (= kind :remove)
          (is (= key (get-in change [:removed-part :key])))
          (is (= (:removed-part change) (nth (:parts input) (:removed-entry-index change)))))))
    (let [input (map-removal-shape [:actual-path :semantic :actual-source-path])
          first-result (transition input :actual-path 65536)
          intermediate (assoc input :parts (get-in first-result [:transition :parts]) :type (:type first-result))
          second-result (transition intermediate :actual-source-path 65536)]
      (is (= [:semantic] (mapv :key (get-in second-result [:transition :parts]))))
      (is (= :semantic (get-in second-result [:transition :survivors 0 :source-part :key]))))
    (let [input (map-removal-shape [:left :right])]
      (doseq [changed [(assoc input :shape-kind :vector) (assoc input :parts nil)
                       (assoc-in input [:parts 1 :key] :left) (assoc-in input [:parts 0 :key] "left")
                       (assoc-in input [:parts 1 :coordinate] 7) (assoc-in input [:parts 0 :shape] nil)
                       (assoc-in input [:type :member-types] []) (assoc-in input [:type :shape] :unknown)
                       (assoc-in input [:type :member-types 1] :gravity.type/string)]]
        (is (= :map-removal-input-shape-required (:reason (transition changed :left 65536)))))
      (doseq [key [nil "left" 1 'left]]
        (is (= :map-removal-input-shape-required (:reason (transition input key 65536))))))))

(deftest sh08-map-association-development-kernel
  (doseq [[key empty? transition keys] [[:new true :add [:new]] [:new false :add [:old :new]] [:old false :replace [:old]]]
          value-type [:gravity.type/string :gravity.type/bool]]
    (let [input (map-association-input key value-type empty?) result (map-association-derived input)
          descriptor (:descriptor result) shape (:shape result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :gravity.type/applied-map-association (:kind descriptor)))
      (is (= transition (get-in descriptor [:transition :kind])))
      (is (= keys (mapv :key (:parts shape))))
      (is (= value-type (get-in (last (:parts shape)) [:shape :type])))
      (is (= :persistent-map-association (:constructor-origin shape)))
      (is (nil? (:constructor-node-id shape)))
      (is (= :input (get-in descriptor [:input-shape :constructor-node-id])))
      (is (= [:memory/allocate] (:latent-effects descriptor) (:direct-effects descriptor)))
      (is (= (if empty? [] [:gravity.type/keyword :gravity.type/integer]) (get-in descriptor [:input-shape :type :member-types])))
      (is (= :equality (:kind (invoke-c7 'sh08-ma-constraint [descriptor]))))))
  (let [{:keys [core nodes forms bindings products binding-types] :as input} (map-association-input :new :gravity.type/string false)
        context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms)) :lookup-bindings (vec (vals bindings)) :shape-call-products products}
        inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
    (is (= :converged (:convergence-status inferred)))
    (is (= [] (:diagnostics inferred)) (pr-str (:diagnostics inferred)))
    (is (= (:descriptor (map-association-derived input)) (get-in inferred [:type-table :operator])))
    (is (= {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/integer :gravity.type/keyword :gravity.type/string] :shape :literal}
           (get-in inferred [:type-table :association]))))
  (let [{:keys [core nodes forms bindings products binding-types] :as input} (chained-map-association-input)
        result (map-association-derived input) descriptor (:descriptor result)
        context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms)) :lookup-bindings (vec (vals bindings)) :shape-call-products products}
        inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= :replace (get-in descriptor [:transition :kind])))
    (is (= :persistent-map-association (get-in descriptor [:input-shape :constructor-origin])))
    (is (= :gravity.type/string (get-in descriptor [:transition :old-part :shape :type])))
    (is (= :gravity.type/bool (get-in descriptor [:transition :new-part :shape :type])))
    (is (= :converged (:convergence-status inferred)))
    (is (= [] (:diagnostics inferred)) (pr-str (:diagnostics inferred)))
    (is (= 2 (count (:constraints inferred))))
    (is (= :gravity.type/bool (last (get-in inferred [:type-table :outer-association :member-types]))))))

(deftest sh08-map-association-premises-development-kernel
  (let [input (map-association-input :new :gravity.type/string false)
        derive map-association-derived]
    (doseq [mutate [#(assoc-in % [:bindings :association-binding :binding-class] :import)
                    #(assoc-in % [:bindings :association-binding :package :version] "forged")
                    #(assoc-in % [:bindings :association-binding :profile-set] nil)
                    #(assoc-in % [:bindings :association-binding :effects] [:io/write])
                    #(assoc-in % [:node :evaluation :order] [])
                    #(assoc-in % [:products :calls 0 :extra] true) #(assoc-in % [:products :call-edges 0 :callee-function-syntax-id] :fake)
                    #(assoc-in % [:forms :key :parent-form-id] :sibling)
                    #(assoc-in % [:forms :key :value] :other)
                    #(assoc-in % [:nodes :key :core-form] :reference)
                    #(assoc-in % [:nodes :key :attributes] {:literal-kind :string :value "new"})
                    #(assoc-in % [:types :value] :gravity.type/unknown)]]
      (is (= :rejected (:status (derive (mutate input))))))
    (let [unestablished (-> input
                            (assoc-in [:nodes :input :core-form] :reference)
                            (assoc-in [:nodes :input :children] [])
                            (assoc-in [:nodes :input :attributes] {:binding-id :map-parameter})
                            (assoc-in [:bindings :map-parameter] {:binding-id :map-parameter :binding-class :lexical :name 'packet})
                            (assoc-in [:binding-types :map-parameter] (get-in input [:types :input])))]
      (is (= :map-association-map-shape-required (:reason (derive unestablished)))))
    (let [nonmap (-> input (assoc-in [:nodes :input :core-form] :literal) (assoc-in [:nodes :input :children] [])
                     (assoc-in [:nodes :input :attributes] {:literal-kind :integer :value 7})
                     (assoc-in [:forms :input :kind] :integer) (assoc-in [:forms :input :value] 7)
                     (assoc-in [:types :input] :gravity.type/integer))]
      (is (= :map-association-map-shape-required (:reason (derive nonmap)))))
    (is (= :map-association-arity-mismatch
           (:reason (derive (assoc-in input [:products :calls 0 :argument-node-ids] [:input :key])))))
    (doseq [operand [:input :value]]
      (let [result (derive (assoc-in input [:types operand] :gravity.type/never))]
        (is (= :accepted (:status result)))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (get-in result [:descriptor :application])))
        (is (= [] (get-in result [:descriptor :direct-effects])))
        (is (nil? (get-in result [:descriptor :transition])))
        (is (nil? (get-in result [:descriptor :result-shape])))))
    (let [{:keys [core node nodes types binding-types bindings forms products]} input
          context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core :budget-left 1)
          result (invoke-c7 'sh08-ma-derive [node nodes types binding-types {} [] bindings forms #{} 0 {} context])]
      (is (= :map-association-work-limit (:reason result))))))

(defn- collection-count-input [kind empty?]
  (let [base (map-association-input :unused :gravity.type/bool empty?)
        operand-ids (if empty? [] (if (= kind :map) [:input-key :input-value] [:input-value]))
        children [:operator :input]
        operator (-> (get-in base [:nodes :operator]) (assoc-in [:attributes :symbol] 'count))
        operand (-> (get-in base [:nodes :input]) (assoc :children operand-ids :attributes {:literal-kind kind :element-count (count operand-ids)})
                    (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) operand-ids)))
        node (-> (:node base) (assoc :children children :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                                                                  :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [:input] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids [:input] :ordered-evaluation-node-ids children)
        nodes (vec (concat [operator] (map #(get-in base [:nodes %]) operand-ids) [operand node]))
        type (if (= kind :map) (get-in base [:types :input])
               {:kind :gravity.type/vector :element-types (if empty? [] [:gravity.type/integer]) :shape :literal})]
    (-> base (assoc :node node :call call :edge edge :nodes (into {} (map (juxt :node-id identity) nodes)))
        (assoc-in [:core :nodes] nodes) (assoc-in [:core :calls] [call]) (assoc-in [:core :call-edges] [edge])
        (assoc-in [:bindings :association-binding :name] 'count)
        (assoc :types (assoc (select-keys (:types base) operand-ids) :input type) :binding-types {})
        (assoc-in [:forms :association :child-form-ids] children)
        (assoc-in [:forms :association :value] (list 'count (if (= kind :map) {} [])))
        (assoc-in [:forms :operator :value] 'count)
        (assoc-in [:forms :input :kind] kind) (assoc-in [:forms :input :child-form-ids] operand-ids)
        (update :products assoc :calls [call] :call-edges [edge]))))

(defn- collection-count-derived [{:keys [core node nodes types binding-types bindings forms products]}]
  (invoke-c7 'sh08-cq-derive [core node (first (:calls products)) (first (:call-edges products)) nodes bindings types forms 65536]))

(defn- list-count-input [type]
  (let [input (integral-ordering-input 'count [type])]
    (assoc input :binding-types (:types input)
                 :products {:calls (get-in input [:core :calls]) :call-edges (get-in input [:core :call-edges]) :module (:module input)})))

(defn- list-count-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-cq-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-list-count-development-kernel
  (let [positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "member hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "member equals"))))
        types [:gravity.type/list homogeneous (positional []) (positional [:gravity.type/nil :gravity.type/bool])
               (positional (vec (repeat 1024 :gravity.type/string)))
               (positional (vec (repeat 8192 :gravity.type/unknown))) (positional [opaque])]]
    (doseq [type types]
      (let [{:keys [core node call edge nodes bindings forms] :as input} (list-count-input type)
            types (:types input) result (list-count-derived input) descriptor (:descriptor result)
            derive #(invoke-c7 'sh08-cq-derive [core node call edge nodes bindings types forms %])]
        (is (= :accepted (:status result)) (pr-str (dissoc result :descriptor :constraint)))
        (is (= :gravity.type/integer (:type result) (:return descriptor) (:result-type descriptor)))
        (is (= 65 (:work-count result)))
        (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:thrown-error-effects descriptor)))
        (is (= {:operation :top-level-cardinality :construction :none :operand-evaluation :once
                :normal-input :established-persistent-list :payload-evaluation :none :input-preserved? true :result-domain :nonnegative-integer}
               (:query-semantics descriptor)))
        (is (not (contains? descriptor :cardinality)))
        (is (not (contains? descriptor :input-shape)))
        (is (= result (derive (:work-count result))))
        (is (= :collection-count-work-limit (:reason (derive (dec (:work-count result))))))
        (is (true? (invoke-c7 'sh08-cq-inferred-call-valid?
                             [core node call edge nodes bindings (assoc types :operator descriptor :call (:type result))])))
        (doseq [mutate [#(assoc-in % [:forms :arg0 :parent-form-id] :sibling)
                        #(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                        #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                        #(assoc-in % [:node :evaluation :order] [])]]
          (is (= :rejected (:status (list-count-derived (mutate input))))))))
    (is (= 0 @callbacks))
    (doseq [bad [(positional 42) (positional nil) (positional '(:type)) (assoc homogeneous :schema :forged)
                 (assoc homogeneous :element-type opaque) (assoc homogeneous :element-type :gravity.type/unknown)
                 {:kind :gravity.type/list} {:kind :gravity.type/list :shape :other :element-types []}
                 :gravity.type/string :gravity.type/nil :gravity.type/unknown :gravity.type/set]]
      (is (= :rejected (:status (list-count-derived (list-count-input bad))))))
    (is (= 0 @callbacks))
    (let [{:keys [core node call edge nodes bindings types forms] :as input} (list-count-input :gravity.type/list)
          run (fn [call node budget] (invoke-c7 'sh08-cq-derive [core node call edge nodes bindings types forms budget]))]
      (doseq [args [nil 42 :arguments '(:arg0)]]
        (is (= :collection-count-call-shape-required (:reason (run (assoc call :argument-node-ids args) node 65536)))))
      (doseq [args [[] [:arg0 :arg0] (vec (repeat 8192 :arg0))]]
        (is (= :collection-count-arity-mismatch (:reason (run (assoc call :argument-node-ids args) node 65536)))))
      (is (= :collection-count-work-limit (:reason (run (assoc call :argument-node-ids (vec (repeat 8192 :arg0))) node 0))))
      (doseq [children [nil 42 :children '(:operator :arg0) [] (vec (repeat 8192 :arg0))]]
        (is (= :collection-count-call-shape-required (:reason (run call (assoc node :children children) 65536))))
        (is (false? (invoke-c7 'sh08-cq-inferred-call-valid? [core (assoc node :children children) call edge nodes bindings types]))))
      ;; Conditional C7 consumer DATA, not C5 qualified-operator admission.
      ;; Empty-import qualified core operators remain a frontend frontier.
      (let [qualified (-> input (assoc-in [:nodes :operator :attributes :symbol] 'gravity.core/count)
                               (assoc-in [:forms :operator :value] 'gravity.core/count))]
        (is (= :accepted (:status (list-count-derived qualified))))))
    (let [result (list-count-derived (list-count-input :gravity.type/never))]
      (is (= :accepted (:status result)))
      (is (= :gravity.type/never (:type result)))
      (is (= :not-invoked (get-in result [:descriptor :application])))
      (is (= :established-map-or-vector (get-in result [:descriptor :query-semantics :normal-input]))))))

(deftest sh08-collection-count-development-kernel
  (doseq [kind [:map :vector] empty? [false true]]
    (let [{:keys [core nodes forms bindings products binding-types] :as input} (collection-count-input kind empty?)
          result (collection-count-derived input) descriptor (:descriptor result)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms)) :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :gravity.type/integer (:type result) (:return descriptor) (:result-type descriptor)))
      (is (not (contains? descriptor :cardinality)))
      (is (not (contains? descriptor :input-shape)))
      (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:capabilities descriptor) (:thrown-error-effects descriptor)))
      (is (= {:operation :top-level-cardinality :construction :none :operand-evaluation :once :normal-input :established-map-or-vector}
             (:query-semantics descriptor)))
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)))
      (is (= descriptor (get-in inferred [:type-table :operator])))
      (is (= :gravity.type/integer (get-in inferred [:type-table :association])))
      (is (= [(invoke-c7 'sh08-cq-constraint [descriptor])] (:constraints inferred)))))
  (let [input (collection-count-input :map false)]
    (doseq [mutate [#(assoc-in % [:bindings :association-binding :binding-class] :import)
                    #(assoc-in % [:bindings :association-binding :effects] [:io/write])
                    #(assoc-in % [:products :calls 0 :extra] true)
                    #(assoc-in % [:products :call-edges 0 :callee-function-syntax-id] :fake)
                    #(assoc-in % [:forms :input :parent-form-id] :sibling)
                    #(assoc-in % [:forms :operator :value] 'assoc)
                    #(assoc-in % [:forms :association :child-form-ids] 7)
                    #(assoc-in % [:node :evaluation :order] [])
                    #(assoc-in % [:types :input] :gravity.type/unknown)
                    #(assoc-in % [:types :input] :gravity.type/nil)
                    #(assoc-in % [:types :input] :gravity.type/string)]]
      (is (= :rejected (:status (collection-count-derived (mutate input))))))
    (is (= :collection-count-arity-mismatch
           (:reason (collection-count-derived (assoc-in input [:products :calls 0 :argument-node-ids] [])))))
    (let [result (collection-count-derived (assoc-in input [:types :input] :gravity.type/never))]
      (is (= :accepted (:status result)))
      (is (= :gravity.type/never (:type result)))
      (is (= :not-invoked (get-in result [:descriptor :application]))))
    ;; A legal collection type is enough. Member selection shapes are irrelevant.
    (let [parameter (-> input (assoc-in [:nodes :input :core-form] :reference) (assoc-in [:nodes :input :children] [])
                        (assoc-in [:nodes :input :attributes] {:binding-id :packet})
                        (assoc-in [:bindings :packet] {:binding-id :packet :binding-class :lexical :name 'packet}))]
      (is (= :accepted (:status (collection-count-derived parameter)))))
    (let [result (invoke-c7 'sh08-cq-derive [(:core input) (:node input) (:call input) (:edge input) (:nodes input)
                                           (:bindings input) (:types input) (:forms input) 1])]
      (is (= :collection-count-work-limit (:reason result))))))

(defn- vector-nth-input [types index]
  (let [base (collection-count-input :vector true) children [:operator :input :index]
        operator (assoc-in (get-in base [:nodes :operator]) [:attributes :symbol] 'nth)
        index-node {:node-id :index :core-form :literal :children [] :attributes {:literal-kind :integer :value index}
                    :resolved-binding-ids [] :source {:form-id :index :syntax-id :index :semantic-span nil :origin-chain [] :generated-origin nil}
                    :evaluation {:kind :left-to-right :order []}}
        node (-> (:node base) (assoc :children children :attributes {:operator-child-index 0 :argument-count 2 :argument-child-indexes [1 2]
                                                                   :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call})
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [:input :index] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids [:input :index] :ordered-evaluation-node-ids children)
        vector-type {:kind :gravity.type/vector :element-types types :shape :literal}
        operand (assoc (get-in base [:nodes :input]) :core-form :reference :attributes {:binding-id :vector-binding})
        nodes [operator operand index-node node]
        index-form {:form-id :index :syntax-id :index :source-span nil :origin-chain [] :generated-origin nil
                    :kind :integer :value index :parent-form-id :association :child-form-ids []}]
    (-> base (assoc :node node :call call :edge edge :nodes (into {} (map (juxt :node-id identity) nodes)))
        (assoc-in [:core :nodes] nodes) (assoc-in [:core :calls] [call]) (assoc-in [:core :call-edges] [edge])
        (assoc-in [:bindings :association-binding :name] 'nth)
        (assoc :types {:input vector-type :index :gravity.type/integer} :binding-types {:vector-binding vector-type})
        (assoc-in [:bindings :vector-binding] {:binding-id :vector-binding :binding-class :lexical :name 'vector-value})
        (assoc-in [:forms :input :kind] :symbol) (assoc-in [:forms :input :value] 'vector-value)
        (assoc-in [:forms :association :child-form-ids] children) (assoc-in [:forms :association :value] (list 'nth [] index))
        (assoc-in [:forms :operator :value] 'nth) (assoc-in [:forms :index] index-form)
        (update :products assoc :calls [call] :call-edges [edge]))))

(defn- vector-append-input [element-types value-type]
  ;; Normalized DATA; the source fixture must independently establish these types.
  (let [base (vector-nth-input element-types 0)
        value (-> (get-in base [:nodes :index])
                  (assoc :node-id :value :core-form :reference :children [] :resolved-binding-ids [:value-binding]
                         :attributes {:binding-id :value-binding}
                         :source {:form-id :value :syntax-id :value :semantic-span nil :origin-chain [] :generated-origin nil}))
        children [:operator :input :value]
        operator (assoc-in (get-in base [:nodes :operator]) [:attributes :symbol] 'conj)
        node (-> (:node base) (assoc :children children)
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [:input :value] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :argument-core-node-ids [:input :value] :ordered-evaluation-node-ids children)
        nodes [operator (get-in base [:nodes :input]) value node]
        value-form {:form-id :value :syntax-id :value :source-span nil :origin-chain [] :generated-origin nil
                    :kind :symbol :value 'tail :parent-form-id :association :child-form-ids []}]
    (-> base (assoc :node node :call call :edge edge :nodes (into {} (map (juxt :node-id identity) nodes)))
        (assoc-in [:core :nodes] nodes) (assoc-in [:core :calls] [call]) (assoc-in [:core :call-edges] [edge])
        (assoc-in [:bindings :association-binding :name] 'conj)
        (assoc-in [:bindings :value-binding] {:binding-id :value-binding :binding-class :lexical :name 'tail})
        (assoc :types {:input (get-in base [:types :input]) :value value-type})
        (assoc-in [:binding-types :value-binding] value-type)
        (assoc-in [:forms :association :child-form-ids] children)
        (assoc-in [:forms :association :value] '(conj vector-value tail))
        (assoc-in [:forms :operator :value] 'conj)
        (update :forms dissoc :index) (assoc-in [:forms :value] value-form)
        (update :products assoc :calls [call] :call-edges [edge]))))

(defn- vector-append-derived [{:keys [core node nodes types bindings forms products]}]
  (invoke-c7 'sh08-va-derive [core node (first (:calls products)) (first (:call-edges products)) nodes bindings types forms 65536]))

(defn- list-prepend-input [receiver value]
  ;; Normalized reference/type DATA, not authenticated source authority.
  (integral-ordering-input 'conj [receiver value]))

(defn- list-prepend-derived [{:keys [core node call edge nodes bindings types forms]}]
  (invoke-c7 'sh08-pl-derive [core node call edge nodes bindings types forms 65536]))

(deftest sh08-list-prepend-development-kernel
  (let [s :gravity.type/string k :gravity.type/keyword b :gravity.type/bool
        homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}
        positional #(hash-map :kind :gravity.type/list :shape :literal :element-types %)]
    (doseq [[receiver value expected]
            [[(positional []) :gravity.type/nil (positional [:gravity.type/nil])]
             [(positional [b s]) k (positional [k b s])]
             [:gravity.type/list s :gravity.type/list]
             [homogeneous s homogeneous]
             [(positional [homogeneous]) b (positional [b homogeneous])]
             [(positional (vec (repeat 1023 s))) s (positional (vec (repeat 1024 s)))]]]
      (let [{:keys [core node call edge nodes bindings types forms] :as input} (list-prepend-input receiver value)
            result (list-prepend-derived input) descriptor (:descriptor result)
            check #(invoke-c7 'sh08-pl-derive [core node call edge nodes bindings types forms %])
            facts (invoke-c7 'sh08-ft-call-facts-with-context [[call] [edge] nodes (assoc types :operator descriptor :call expected) {} (:module core) {}])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= expected (:type result) (:result-type descriptor) (:return descriptor)))
        (is (= :gravity.type/applied-list-prepend (:kind descriptor)))
        (is (= :direct-list-prepend (get-in facts [0 :dispatch])))
        (is (= [:memory/allocate] (:direct-effects descriptor)))
        (is (= [] (:latent-effects descriptor)))
        (is (= :normal-operand-completion (get-in descriptor [:allocation-policy :condition])))
        (is (= :memory/allocate (get-in descriptor [:allocation-policy :effect])))
        (is (= :complete-ordered-input (get-in descriptor [:prepend-semantics :suffix])))
        (is (= :may-retain-input-storage (get-in descriptor [:storage-policy :retention])))
        (is (= result (check (:work-count result))))
        (is (= ["C7-GENERIC" :list-prepend-work-limit] ((juxt :rule :reason) (check (dec (:work-count result))))))
        (is (true? (invoke-c7 'sh08-pl-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator descriptor :call expected)])))
        (doseq [changed [(assoc descriptor :direct-effects [])
                         (assoc-in descriptor [:prepend-semantics :head] :last-operand)
                         (assoc-in descriptor [:storage-policy :retention] :copies-all)
                         (assoc descriptor :operand-type-work 0)
                         (assoc descriptor :result-type-work 0)
                         (assoc descriptor :result-type :gravity.type/unknown)]]
          (is (false? (invoke-c7 'sh08-pl-inferred-call-valid? [core node call edge nodes bindings (assoc types :operator changed :call expected)]))))
        (doseq [mutate [#(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                        #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                        #(assoc-in % [:node :evaluation :order] [])
                        #(assoc-in % [:forms :arg1 :parent-form-id] :sibling)]]
          (is (= :rejected (:status (list-prepend-derived (mutate input))))))))
    (doseq [receiver [:gravity.type/list homogeneous (positional (vec (repeat 1024 s)))]]
      (let [result (list-prepend-derived (list-prepend-input receiver :gravity.type/never)) descriptor (:descriptor result)]
        (is (= :accepted (:status result)))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (:application descriptor)))
        (is (= [] (:direct-effects descriptor)))
        (is (nil? (:result-extent descriptor)))))
    (doseq [[receiver value reason]
            [[homogeneous k :list-prepend-element-type-mismatch]
             [homogeneous :gravity.type/nil :list-prepend-element-type-mismatch]
             [homogeneous :gravity.type/unknown :list-prepend-operand-type-required]
             [:gravity.type/unknown s :list-prepend-operand-type-required]
             [:gravity.type/nil s :list-prepend-list-type-required]
             [{:kind :gravity.type/vector :shape :literal :element-types []} s :list-prepend-list-type-required]
             [(positional (vec (repeat 1024 s))) s :list-prepend-type-width-limit]
             [(positional 42) s :list-prepend-operand-type-required]
             [(positional [:gravity.type/unknown]) s :list-prepend-operand-type-required]
             [(assoc homogeneous :schema :secret) s :list-prepend-operand-type-required]]]
      (is (= reason (:reason (list-prepend-derived (list-prepend-input receiver value))))))
    (let [nested #(nth (iterate (fn [type] {:kind :gravity.type/vector :shape :literal :element-types [type]}) k) %)]
      (is (= :accepted (:status (list-prepend-derived (list-prepend-input (positional []) (nested 127))))))
      (is (= :list-prepend-result-type-required (:reason (list-prepend-derived (list-prepend-input (positional []) (nested 128)))))))
    (doseq [args [[] [:arg0] [:arg0 :arg1 :arg0]]]
      (is (= :list-prepend-arity-mismatch (:reason (list-prepend-derived (assoc-in (list-prepend-input homogeneous s) [:call :argument-node-ids] args))))))
    (let [{:keys [core node call edge nodes bindings types forms]} (list-prepend-input homogeneous s)
          run (fn [args budget]
                (invoke-c7 'sh08-pl-derive [core node (assoc call :argument-node-ids args) edge nodes bindings types forms budget]))
          valid? (fn [args]
                   (invoke-c7 'sh08-pl-inferred-call-valid? [core node (assoc call :argument-node-ids args) edge nodes bindings types]))]
      (doseq [args [nil 42 :not-arguments '(:arg0 :arg1)]]
        (is (= :list-prepend-call-shape-required
               (:reason (run args 65536))))
        (is (false? (valid? args))))
      (let [many (vec (repeat 8192 :arg0))]
        (is (= :list-prepend-work-limit (:reason (run many 0))))
        (is (= :list-prepend-arity-mismatch (:reason (run many 65536))))
        (is (false? (valid? many))))
      (is (= :list-prepend-work-limit (:reason (run [:arg0 :arg1] 0)))))
    (let [{:keys [core node call edge nodes bindings types forms]} (list-prepend-input homogeneous s)]
      (doseq [children [nil 42 :children '(:operator :arg0 :arg1) [] (vec (repeat 8192 :arg0))]]
        (let [node (assoc node :children children)]
          (is (= :list-prepend-call-shape-required
                 (:reason (invoke-c7 'sh08-pl-derive [core node call edge nodes bindings types forms 65536]))))
          (is (false? (invoke-c7 'sh08-pl-inferred-call-valid? [core node call edge nodes bindings types]))))))
    (doseq [[receiver value kind status]
            [[homogeneous s :gravity.type/applied-list-prepend :accepted]
             [{:kind :gravity.type/vector :shape :literal :element-types []} s :gravity.type/applied-vector-append :accepted]
             [:gravity.type/never s :gravity.type/applied-vector-append :accepted]
             [:gravity.type/unknown s nil :deferred]
             [homogeneous :gravity.type/unknown nil :deferred]]]
      (let [{:keys [core node call edge nodes bindings types forms]} (list-prepend-input receiver value)
            context {:ordering-core core :ordering-operators {:operator call} :ordering-edges {:call edge}
                     :lookup-nodes nodes :ordering-bindings bindings :association-forms forms}
            result (invoke-c7 'sh08-va-step [node types {} {} context 65536])]
        (is (= status (:status result)))
        (is (= kind (get-in result [:descriptor :kind])))
        (when (= receiver :gravity.type/never)
          (is (= :gravity.type/never (:type result)))
          (is (= [] (get-in result [:descriptor :direct-effects]))))
        (when (= status :deferred)
          (is (= :gravity.type/unknown (:type result)))
          (is (= :conj-dispatch-work-limit
                 (:reason (invoke-c7 'sh08-va-step [node types {} {} context 1])))))))))

(deftest sh08-vector-append-development-kernel
  (doseq [[elements value] [[[] :gravity.type/keyword]
                            [[:gravity.type/integer] :gravity.type/string]
                            [[:gravity.type/integer :gravity.type/bool] :gravity.type/nil]
                            [(vec (repeat 1023 :gravity.type/keyword)) :gravity.type/keyword]]]
    (let [{:keys [core nodes forms bindings products binding-types] :as input} (vector-append-input elements value)
          result (vector-append-derived input) descriptor (:descriptor result)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                   :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                     [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= (conj elements value) (get-in result [:type :element-types])))
      (is (= (count elements) (:input-extent descriptor)))
      (is (= (inc (count elements)) (:result-extent descriptor)))
      (is (= [:memory/allocate] (:direct-effects descriptor)))
      (is (= :normal (:application descriptor)))
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)) (pr-str (:diagnostics inferred)))
      (is (= descriptor (get-in inferred [:type-table :operator])))
      (is (= (:type result) (get-in inferred [:type-table :association])))
      (is (= [(invoke-c7 'sh08-va-constraint [descriptor])] (:constraints inferred)))))
  (let [input (vector-append-input [:gravity.type/keyword] :gravity.type/keyword)]
    (doseq [operands [[:input] [:value] [:input :value]]]
      (let [result (vector-append-derived (reduce #(assoc-in %1 [:types %2] :gravity.type/never) input operands))]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (get-in result [:descriptor :application])))
        (is (= [] (get-in result [:descriptor :direct-effects])))
        (is (nil? (get-in result [:descriptor :result-extent])))))
    (doseq [mutate [#(assoc-in % [:bindings :association-binding :binding-class] :import)
                    #(assoc-in % [:bindings :association-binding :effects] [:io/write])
                    #(assoc-in % [:bindings :association-binding :package :version] "forged")
                    #(assoc-in % [:products :calls 0 :extra] true)
                    #(assoc-in % [:products :call-edges 0 :callee-function-syntax-id] :fake)
                    #(assoc-in % [:forms :value :parent-form-id] :sibling)
                    #(assoc-in % [:forms :operator :value] 'count)
                    #(assoc-in % [:node :evaluation :order] [])
                    #(assoc-in % [:types :input] :gravity.type/string)
                    #(assoc-in % [:types :value] :gravity.type/unknown)
                    #(assoc-in % [:types :input :element-types] (vec (repeat 1024 :gravity.type/keyword)))
                    #(-> % (assoc-in [:types :input] :gravity.type/string) (assoc-in [:types :value] :gravity.type/never))
                    #(-> % (assoc-in [:types :input] :gravity.type/never) (assoc-in [:types :value] :gravity.type/unknown))]]
      (is (= :rejected (:status (vector-append-derived (mutate input))))))
    (is (= :vector-append-arity-mismatch (:reason (vector-append-derived (assoc-in input [:products :calls 0 :argument-node-ids] [:input])))))
    (let [{:keys [core node call edge nodes bindings types forms]} input
          result (vector-append-derived input) work (:work-count result)
          derive #(invoke-c7 'sh08-va-derive [core node call edge nodes bindings types forms %])]
      (is (= result (derive work)))
      (is (= :vector-append-work-limit (:reason (derive (dec work))))))))

(defn- vector-nth-derived [{:keys [core node nodes types bindings forms products]}]
  (invoke-c7 'sh08-vn-derive [core node (first (:calls products)) (first (:call-edges products)) nodes bindings types forms 65536]))

(deftest sh08-vector-nth-development-kernel
  ;; Finite typed DATA tests. The authenticated fixture supplies real members.
  (doseq [[types index expected] [[[:gravity.type/string] 0 :gravity.type/string]
                                 [[:gravity.type/nil] 0 :gravity.type/nil]
                                 [[:gravity.type/integer :gravity.type/bool :gravity.type/string] 0 :gravity.type/integer]
                                 [[:gravity.type/integer :gravity.type/bool :gravity.type/string] 2 :gravity.type/string]
                                 [[{:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/string] :shape :literal}
                                   :gravity.type/string] 1 :gravity.type/string]]]
    (let [{:keys [core nodes forms bindings products binding-types] :as input} (vector-nth-input types index)
          result (vector-nth-derived input) descriptor (:descriptor result)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms)) :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types 10 context])]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= expected (:type result) (:selected-type descriptor) (:return descriptor)))
      (is (= index (:index-value descriptor) (get-in descriptor [:bounds-proof :index-value])))
      (is (= (count types) (:vector-length descriptor) (get-in descriptor [:bounds-proof :vector-length])))
      (is (= :proven-safe (get-in descriptor [:bounds-proof :outcome])))
      (is (= :not-authorized (get-in descriptor [:bounds-proof :check-elision])))
      (is (= :retained (get-in descriptor [:bounds-proof :runtime-guards])))
      (is (= [] (:direct-effects descriptor) (:latent-effects descriptor) (:thrown-error-effects descriptor)))
      (is (not (contains? descriptor :input-shape)))
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)))
      (is (= descriptor (get-in inferred [:type-table :operator])))))
  (doseq [[types index reason] [[[:gravity.type/string] -1 :vector-nth-negative-index]
                               [[:gravity.type/string] 1 :vector-nth-index-out-of-range]
                               [[] 0 :vector-nth-index-out-of-range]]]
    (let [result (vector-nth-derived (vector-nth-input types index))]
      (is (= :rejected (:status result)))
      (is (= "SAFE2-BOUNDS" (:rule result)))
      (is (= reason (:reason result)))))
  (doseq [operands [[:input] [:index] [:input :index]]]
    (let [input (reduce #(assoc-in %1 [:types %2] :gravity.type/never) (vector-nth-input [] 0) operands)
          result (vector-nth-derived input) descriptor (:descriptor result)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :gravity.type/never (:type result)))
      (is (= :not-invoked (:application descriptor)))
      (is (nil? (:bounds-proof descriptor)))
      (is (nil? (:selected-type descriptor)))))
  (doseq [type [nil :gravity.type/unknown]]
    (let [result (vector-nth-derived (vector-nth-input [type] 0))]
      (is (= :rejected (:status result)))
      (is (= "C7-ANNOTATION" (:rule result)))
      (is (= :vector-nth-selected-element-type-required (:reason result)))))
  (let [input (vector-nth-input [:gravity.type/string] 0)]
    (doseq [mutate [#(assoc-in % [:types :input] :gravity.type/string) #(assoc-in % [:types :input] :gravity.type/unknown)
                    #(assoc-in % [:types :index] :gravity.type/string) #(assoc-in % [:types :index] :gravity.type/unknown)
                    #(assoc-in % [:nodes :index :core-form] :reference)
                    #(assoc-in % [:forms :index :value] 1) #(assoc-in % [:forms :association :child-form-ids] 7)
                    #(assoc-in % [:bindings :association-binding :binding-class] :import)
                    #(assoc-in % [:node :evaluation :order] [])]]
      (is (= :rejected (:status (vector-nth-derived (mutate input))))))
    (is (= ["C7-ANNOTATION" :vector-nth-literal-index-required]
           ((juxt :rule :reason) (vector-nth-derived (assoc-in input [:nodes :index :core-form] :reference)))))
    (is (= ["C7-VERIFY" :vector-nth-index-source-mismatch]
           ((juxt :rule :reason) (vector-nth-derived (assoc-in input [:nodes :index :attributes :value] "0")))))
    (is (= :vector-nth-arity-mismatch
           (:reason (vector-nth-derived (assoc-in input [:products :calls 0 :argument-node-ids] [:input])))))
    (doseq [changed [(assoc-in input [:types :input] :gravity.type/never)
                     (assoc-in input [:types :index] :gravity.type/never)]]
      (let [bad (if (= :gravity.type/never (get-in changed [:types :input]))
                  (assoc-in changed [:types :index] :gravity.type/string)
                  (assoc-in changed [:types :input] :gravity.type/string))]
        (is (= :rejected (:status (vector-nth-derived bad))))))))

(defn- vector-nth-shape-input []
  (let [map-type {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/string] :shape :literal}
        base (vector-nth-input [map-type :gravity.type/bool] 0)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        node (fn [id kind children attrs]
               {:node-id id :core-form kind :children children :attributes attrs :source (source id) :resolved-binding-ids []
                :evaluation {:kind :left-to-right :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}})
        payload-key (node :payload-key :literal [] {:literal-kind :keyword :value :value})
        payload-value (node :payload-value :reference [] {:binding-id :text-binding})
        payload (node :payload :collection-literal [:payload-key :payload-value] {:literal-kind :map :element-count 2})
        extra (node :extra :literal [] {:literal-kind :boolean :value false})
        operand (node :input :collection-literal [:payload :extra] {:literal-kind :vector :element-count 2})
        nodes [(get-in base [:nodes :operator]) payload-key payload-value payload extra operand (get-in base [:nodes :index]) (:node base)]
        form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                  :kind kind :value value :parent-form-id parent :child-form-ids children})]
    (-> base (assoc :nodes (into {} (map (juxt :node-id identity) nodes))) (assoc-in [:core :nodes] nodes)
        (assoc-in [:bindings :text-binding] {:binding-id :text-binding :binding-class :lexical :name 'text})
        (assoc-in [:binding-types :text-binding] :gravity.type/string)
        (update :types merge {:payload-key :gravity.type/keyword :payload-value :gravity.type/string :payload map-type :extra :gravity.type/bool})
        (update :forms merge {:input (form :input :vector '[{:value text} false] :association [:payload :extra])
                              :payload (form :payload :map '{:value text} :input [:payload-key :payload-value])
                              :payload-key (form :payload-key :keyword :value :payload [])
                              :payload-value (form :payload-value :symbol 'text :payload [])
                              :extra (form :extra :boolean false :input [])}))))

(deftest sh08-vector-nth-selected-shape-development-kernel
  (let [{:keys [core node nodes types bindings binding-types forms products]} (vector-nth-shape-input)
        context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
        derive #(invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {} %])
        shape (derive context)]
    (is (= :accepted (:status shape)) (pr-str shape))
    (is (= :map (:shape-kind shape)))
    (is (= :payload (:constructor-node-id shape)))
    (is (= :payload (get-in shape [:source :form-id])))
    (is (= :gravity.type/string (get-in shape [:parts 0 :shape :type])))
    (is (= 0 (get-in shape [:vector-nth-selection :projection-links 0 :coordinate])))
    (is (= :input (get-in shape [:vector-nth-selection :projection-links 0 :vector-node :node-id])))
    (is (= :proven-safe (get-in shape [:vector-nth-selection :application :bounds-proof :outcome])))
    (when (= :accepted (:status shape))
      (is (= shape (derive (assoc context :budget-left (:call-join-work shape)))))
      (is (= :vector-nth-shape-work-limit (:reason (derive (assoc context :budget-left (dec (:call-join-work shape))))))))))

(defn- vector-nth-unrelated-shape-input []
  (let [base (vector-nth-shape-input) quoted (quoted-data-input {"unrelated" 1} 12000)
        quote-node (:node quoted) quote-id (:node-id quote-node) quote-form (:owner quoted)
        other-type {:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/integer] :shape :literal}
        selected-type (get-in base [:types :payload]) vector-type {:kind :gravity.type/vector :element-types [other-type selected-type] :shape :literal}
        operand (-> (get-in base [:nodes :input]) (assoc :children [quote-id :payload])
                    (assoc-in [:evaluation :order] [{:index 0 :core-node-id quote-id} {:index 1 :core-node-id :payload}]))
        nodes (mapv #(case (:node-id %) :extra quote-node :input operand :index (assoc-in % [:attributes :value] 1) %) (get-in base [:core :nodes]))]
    (-> base (assoc :nodes (into {} (map (juxt :node-id identity) nodes))) (assoc-in [:core :nodes] nodes)
        (update :types #(-> % (dissoc :extra) (assoc quote-id other-type :input vector-type)))
        (assoc-in [:binding-types :vector-binding] vector-type)
        (update :forms #(merge (dissoc % :extra) (:forms quoted)))
        (assoc-in [:forms quote-form :parent-form-id] :input)
        (assoc-in [:forms :input :child-form-ids] [quote-form :payload])
        (assoc-in [:forms :input :value] [(list 'quote {"unrelated" 1}) {:value 'text}])
        (assoc-in [:forms :index :value] 1))))

(defn- vector-nth-quoted-shape-input [nested?]
  (let [value [{"unrelated" 1} {:value "selected"}] input-type {:kind :gravity.type/vector :shape :literal
          :element-types [{:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/integer] :shape :literal}
                          {:kind :gravity.type/map :member-types [:gravity.type/keyword :gravity.type/string] :shape :literal}]}
        base (vector-nth-input (:element-types input-type) 1)
        quoted (quoted-data-input (if nested? [value] value) (if nested? 13000 14000))
        quote-node (:node quoted) quote-id (:node-id quote-node) quote-form (:owner quoted)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                  :kind kind :value value :parent-form-id parent :child-form-ids children})
        inner-children [:inner-operator quote-id :inner-index]
        inner-operator (assoc (get-in base [:nodes :operator]) :node-id :inner-operator :source (source :inner-operator))
        inner-index (-> (get-in base [:nodes :index]) (assoc :node-id :inner-index :source (source :inner-index)) (assoc-in [:attributes :value] 0))
        inner-node (-> (:node base) (assoc :node-id :inner-query :source (source :inner-query) :children inner-children)
                       (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) inner-children)))
        inner-call (assoc (:call base) :core-node-id :inner-query :operator-node-id :inner-operator
                          :argument-node-ids [quote-id :inner-index] :ordered-evaluation-node-ids inner-children)
        inner-edge (assoc (:edge base) :call-core-node-id :inner-query :argument-core-node-ids [quote-id :inner-index]
                          :ordered-evaluation-node-ids inner-children)
        operand-id (if nested? :inner-query quote-id) children [:operator operand-id :index]
        node (-> (:node base) (assoc :children children)
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call base) :argument-node-ids [operand-id :index] :ordered-evaluation-node-ids children)
        edge (assoc (:edge base) :ordinal (if nested? 1 0) :argument-core-node-ids [operand-id :index] :ordered-evaluation-node-ids children)
        nodes (vec (concat [(get-in base [:nodes :operator])] (when nested? [inner-operator]) [quote-node]
                           (when nested? [inner-index inner-node]) [(get-in base [:nodes :index]) node]))
        calls (if nested? [inner-call call] [call]) edges (if nested? [inner-edge edge] [edge])
        core (assoc (:core base) :nodes nodes :calls calls :call-edges edges)]
    (-> base (assoc :node node :call call :edge edge :core core :nodes (into {} (map (juxt :node-id identity) nodes)))
        (assoc :types (cond-> {quote-id (if nested? {:kind :gravity.type/vector :element-types [input-type] :shape :literal} input-type)
                              :index :gravity.type/integer}
                       nested? (assoc :inner-index :gravity.type/integer :inner-query input-type)))
        (update :forms merge (:forms quoted))
        (assoc-in [:forms quote-form :parent-form-id] (if nested? :inner-query :association))
        (assoc-in [:forms :association :child-form-ids] [:operator (if nested? :inner-query quote-form) :index])
        (update :forms merge (when nested? {:inner-query (form :inner-query :list '(nth quoted 0) :association [:inner-operator quote-form :inner-index])
                                           :inner-operator (form :inner-operator :symbol 'nth :inner-query [])
                                           :inner-index (form :inner-index :integer 0 :inner-query [])}))
        (update :products assoc :calls calls :call-edges edges))))

(defn- vector-nth-evaluated-chain-input []
  (let [base (vector-nth-quoted-shape-input true) member (vector-nth-unrelated-shape-input)
        quote-id (first (get-in base [:products :calls 0 :argument-node-ids]))
        source {:form-id :outer-vector :syntax-id :outer-vector :semantic-span nil :origin-chain [] :generated-origin nil}
        outer {:node-id :outer-vector :core-form :collection-literal :children [:input] :source source :resolved-binding-ids []
               :attributes {:literal-kind :vector :element-count 1}
               :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :input}]}}
        member-ids (remove #{:operator :index :association} (keys (:nodes member)))
        inner (-> (get-in base [:nodes :inner-query]) (assoc-in [:children 1] :outer-vector)
                  (assoc-in [:evaluation :order 1 :core-node-id] :outer-vector))
        calls (assoc-in (get-in base [:products :calls]) [0 :argument-node-ids 0] :outer-vector)
        calls (assoc-in calls [0 :ordered-evaluation-node-ids 1] :outer-vector)
        edges (-> (get-in base [:products :call-edges]) (assoc-in [0 :argument-core-node-ids 0] :outer-vector)
                  (assoc-in [0 :ordered-evaluation-node-ids 1] :outer-vector))
        nodes (-> (:nodes base) (dissoc quote-id) (merge (select-keys (:nodes member) member-ids))
                  (assoc :outer-vector outer :inner-query inner))]
    (-> base (assoc :nodes nodes :bindings (:bindings member) :binding-types (:binding-types member))
        (update :types merge (select-keys (:types member) member-ids) {:outer-vector (get-in base [:types quote-id])})
        (update :forms merge (:forms member))
        (assoc-in [:forms :association] (get-in base [:forms :association]))
        (assoc-in [:forms :input :parent-form-id] :outer-vector)
        (assoc-in [:forms :inner-query :child-form-ids 1] :outer-vector)
        (assoc-in [:forms :outer-vector] {:form-id :outer-vector :syntax-id :outer-vector :source-span nil :origin-chain [] :generated-origin nil
                                         :kind :vector :value [] :parent-form-id :inner-query :child-form-ids [:input]})
        (assoc-in [:core :nodes] (vec (vals nodes))) (assoc-in [:core :calls] calls) (assoc-in [:core :call-edges] edges)
        (update :products assoc :calls calls :call-edges edges))))

(deftest sh08-vector-nth-selected-coordinate-development-kernel
  (doseq [[input path] [[(vector-nth-unrelated-shape-input) nil] [(vector-nth-quoted-shape-input false) [1]]
                       [(vector-nth-quoted-shape-input true) [0 1]] [(vector-nth-evaluated-chain-input) nil]]]
    (let [{:keys [core node nodes types bindings binding-types forms products]} input
          context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
          derive #(invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {} %])
          shape (derive context)]
      (is (= :accepted (:status shape)) (pr-str shape))
      (is (= :map (:shape-kind shape)))
      (is (= :gravity.type/string (get-in shape [:parts 0 :shape :type])))
      (is (seq (:vector-projection-links shape)))
      (when path (is (= path (get-in shape [:data-origin :data-path]))))
      (doseq [link (:vector-projection-links shape)]
        (is (= :proven-safe (get-in link [:application :bounds-proof :outcome])))
        (is (= :not-authorized (get-in link [:application :bounds-proof :check-elision]))))
      (when (= :accepted (:status shape))
        (is (= shape (derive (assoc context :budget-left (:call-join-work shape)))))
        (is (= :vector-nth-shape-work-limit (:reason (derive (assoc context :budget-left (dec (:call-join-work shape)))))))))))

(defn- vector-append-projection-input [index unrelated?]
  (let [base (vector-append-input [:gravity.type/integer] :gravity.type/keyword)
        literal (collection-count-input :vector false)
        prefix-node (get-in literal [:nodes :input])
        prefix-value (get-in literal [:nodes :input-value])
        prefix-forms (select-keys (:forms literal) [:input :input-value])
        append (-> base (assoc-in [:nodes :input] prefix-node) (assoc-in [:nodes :input-value] prefix-value)
                   (update :types assoc :input-value :gravity.type/integer)
                   (update :forms merge prefix-forms))
        quoted (when unrelated? (quoted-data-input {"unrelated" 1} 15000))
        qid (some-> quoted :node :node-id)
        qtype {:kind :gravity.type/map :member-types [:gravity.type/string :gravity.type/integer] :shape :literal}
        input-type {:kind :gravity.type/vector :element-types [(if unrelated? qtype :gravity.type/integer)] :shape :literal}
        append (cond-> append
                 unrelated? (-> (update :nodes dissoc :input-value)
                                (assoc-in [:nodes qid] (:node quoted))
                                (assoc-in [:nodes :input :children] [qid])
                                (assoc-in [:nodes :input :evaluation :order] [{:index 0 :core-node-id qid}])
                                (update :types dissoc :input-value) (assoc-in [:types qid] qtype)
                                (update :forms dissoc :input-value) (update :forms merge (:forms quoted))
                                (assoc-in [:forms (:owner quoted) :parent-form-id] :input)
                                (assoc-in [:forms :input :child-form-ids] [(:owner quoted)])))
        append (-> append (assoc-in [:types :input] input-type)
                   (update-in [:core :nodes] (fn [_] (vec (vals (:nodes append))))))
        rename {:association :append-call :operator :append-operator :association-binding :append-binding}
        append (-> (walk/postwalk-replace rename append)
                   (assoc-in [:nodes :append-operator :attributes :position] :operator))
        result (vector-append-derived append) result-type (:type result)
        outer (vector-nth-input (:element-types result-type) index)
        children [:operator :append-call :index]
        node (-> (:node outer) (assoc :children children)
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (:call outer) :argument-node-ids [:append-call :index] :ordered-evaluation-node-ids children)
        edge (assoc (:edge outer) :ordinal 1 :argument-core-node-ids [:append-call :index] :ordered-evaluation-node-ids children)
        calls [(:call append) call] edges [(:edge append) edge]
        nodes (merge (:nodes append) (select-keys (:nodes outer) [:operator :index]) {:association node})
        core (assoc (:core append) :nodes (vec (vals nodes)) :calls calls :call-edges edges)
        forms (-> (:forms append) (merge (select-keys (:forms outer) [:operator :index :association]))
                  (assoc-in [:append-call :parent-form-id] :association)
                  (assoc-in [:association :child-form-ids] children))]
    {:core core :node node :nodes nodes :forms forms :bindings (merge (:bindings append) (select-keys (:bindings outer) [:association-binding]))
     :binding-types (:binding-types append)
     :types (assoc (:types append) :append-call result-type :append-operator (:descriptor result) :index :gravity.type/integer)
     :products (assoc (:products append) :calls calls :call-edges edges)}))

(defn- vector-append-chained-projection-input [index nested?]
  (let [base (vector-append-projection-input index false)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                  :kind kind :value value :parent-form-id parent :child-form-ids children})
        leaf {:node-id :extra-leaf :core-form :literal :children [] :resolved-binding-ids []
              :source (source :extra-leaf) :attributes {:literal-kind :keyword :value :done} :evaluation {:kind :left-to-right :order []}}
        value (if nested? {:node-id :extra :core-form :collection-literal :children [:extra-leaf] :resolved-binding-ids []
                           :source (source :extra) :attributes {:literal-kind :vector :element-count 1}
                           :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :extra-leaf}]}}
                      (assoc leaf :node-id :extra :source (source :extra)))
        value-type (if nested? {:kind :gravity.type/vector :element-types [:gravity.type/keyword] :shape :literal} :gravity.type/keyword)
        children [:second-operator :append-call :extra]
        operator (-> (get-in base [:nodes :append-operator]) (assoc :node-id :second-operator :source (source :second-operator)))
        node (-> (get-in base [:nodes :append-call]) (assoc :node-id :second-append :source (source :second-append) :children children)
                 (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        call (assoc (first (get-in base [:products :calls])) :core-node-id :second-append :operator-node-id :second-operator
                    :argument-node-ids [:append-call :extra] :ordered-evaluation-node-ids children)
        edge (assoc (first (get-in base [:products :call-edges])) :ordinal 1 :call-core-node-id :second-append
                    :argument-core-node-ids [:append-call :extra] :ordered-evaluation-node-ids children)
        outer-children [:operator :second-append :index]
        outer (-> (:node base) (assoc :children outer-children)
                  (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) outer-children)))
        outer-call (assoc (last (get-in base [:products :calls])) :argument-node-ids [:second-append :index] :ordered-evaluation-node-ids outer-children)
        outer-edge (assoc (last (get-in base [:products :call-edges])) :ordinal 2 :argument-core-node-ids [:second-append :index] :ordered-evaluation-node-ids outer-children)
        nodes (cond-> (assoc (:nodes base) :second-operator operator :second-append node :extra value :association outer)
                nested? (assoc :extra-leaf leaf))
        calls [(first (get-in base [:products :calls])) call outer-call]
        edges [(first (get-in base [:products :call-edges])) edge outer-edge]
        forms (-> (:forms base)
                  (assoc :second-append (form :second-append :list '(conj original extra) :association children)
                         :second-operator (form :second-operator :symbol 'conj :second-append [])
                         :extra (form :extra (if nested? :vector :keyword) (if nested? [:done] :done) :second-append (if nested? [:extra-leaf] [])))
                  (assoc-in [:append-call :parent-form-id] :second-append)
                  (assoc-in [:association :child-form-ids] outer-children)
                  (cond-> nested? (assoc :extra-leaf (form :extra-leaf :keyword :done :extra []))))
        types (assoc (:types base) :extra value-type :extra-leaf :gravity.type/keyword)
        result (invoke-c7 'sh08-va-derive [(:core base) node call edge nodes (:bindings base) types forms 65536])
        types (assoc types :second-operator (:descriptor result) :second-append (:type result))]
    (assoc base :core (assoc (:core base) :nodes (vec (vals nodes)) :calls calls :call-edges edges)
                :node outer :nodes nodes :forms forms :types types
                :products (assoc (:products base) :calls calls :call-edges edges))))

(deftest sh08-vector-append-projection-development-kernel
  (doseq [[index unrelated?] [[0 false] [1 false] [1 true]]]
    (let [{:keys [core node nodes types binding-types bindings forms products] :as input} (vector-append-projection-input index unrelated?)
          context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
          derive #(invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {} %])
          shape (derive context)
          links (filterv #(= :persistent-vector-append-coordinate (:kind %)) (:vector-projection-links shape))
          link (first links)]
      (is (= :accepted (:status shape)) (pr-str shape))
      (is (= :scalar (:shape-kind shape)))
      (is (= (if (zero? index) :gravity.type/integer :gravity.type/keyword) (:type shape)))
      (is (= 1 (count links)))
      (is (= (if (zero? index) :original-prefix :appended-value) (:selection link)))
      (is (= :append-call (get-in link [:append-application :call-core-node-id])))
      (is (= 'conj (get-in link [:append-application :operation])))
      (is (= (:call-core-node-id (get-in link [:append-application :source-edge])) :append-call))
      (is (= (if (zero? index) :input :value) (:selected-node-id link)))
      (when (= :accepted (:status shape))
        (is (= shape (derive (assoc context :budget-left (:call-join-work shape)))))
        (is (= :rejected (:status (derive (assoc context :budget-left (dec (:call-join-work shape))))))))
      (doseq [changed [(assoc-in input [:types :append-call :element-types 1] :gravity.type/string)
                       (assoc-in input [:nodes :append-call :evaluation :order] [])
                       (assoc-in input [:forms :value :parent-form-id] :sibling)
                       (assoc-in input [:products :calls 0 :operator-binding-id] :forged)]]
        (let [{:keys [core node nodes types binding-types bindings forms products]} changed
              shape (invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {}
                        (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)])]
          (is (= :rejected (:status shape)))))))
  (doseq [[index nested?] [[0 false] [1 false] [2 false] [2 true]]]
    (let [{:keys [core node nodes types binding-types bindings forms products]} (vector-append-chained-projection-input index nested?)
          context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
          next-demand (when nested? {:query-core-node-id :nested-query :coordinate 0
                                   :vector-type {:kind :gravity.type/vector :element-types [:gravity.type/keyword] :shape :literal}
                                   :selected-type :gravity.type/keyword :application {:call-core-node-id :nested-query}})
          derive #(invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {}
                    (cond-> % nested? (assoc :vector-coordinate-demand next-demand))])
          shape (derive context)
          links (filterv #(= :persistent-vector-append-coordinate (:kind %)) (:vector-projection-links shape))]
      (is (= :accepted (:status shape)) (pr-str shape))
      (is (= (if (zero? index) :gravity.type/integer :gravity.type/keyword) (:type shape)))
      (is (= (if (< index 2) 2 1) (count links)))
      (is (= :second-append (get-in (last links) [:append-application :call-core-node-id])))
      (when nested? (is (= {:values [:done]} (:domain shape))))
      (when (= :accepted (:status shape))
        (is (= shape (derive (assoc context :budget-left (:call-join-work shape)))))
        (is (= :rejected (:status (derive (assoc context :budget-left (dec (:call-join-work shape))))))))))
  (let [{:keys [core nodes types binding-types bindings forms products]} (vector-append-projection-input 0 false)
        context (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)
        shape (invoke-c7 'sh08-va-shape [(nodes :append-call) nodes types binding-types {} [] bindings forms #{} 0 {} context])]
    (is (= :accepted (:status shape)) (pr-str shape))
    (is (= :vector (:shape-kind shape)))
    (is (= [0 1] (mapv :coordinate (:parts shape))))
    (is (= [:gravity.type/integer :gravity.type/keyword] (mapv #(get-in % [:shape :type]) (:parts shape))))
    (is (nil? (:constructor-node-id shape)))
    (is (= :append-call (get-in shape [:vector-append-links 0 :application :call-core-node-id])))))

(deftest sh08-vector-nth-selected-coordinate-premises-development-kernel
  (let [input (vector-nth-unrelated-shape-input)
        derive (fn [{:keys [core node nodes types bindings binding-types forms products]}]
                 (invoke-c7 'sh08-vn-shape [node nodes types binding-types {} [] bindings forms #{} 0 {}
                                          (assoc (invoke-c7 'sh08-sm-call-context [products types 65536]) :source-core core)]))]
    (is (= :accepted (:status (derive input))))
    (doseq [[label changed] [[:vector-source (assoc-in input [:forms :input :syntax-id] :stale)]
                             [:selected-source (assoc-in input [:forms :payload :parent-form-id] :stale)]
                             [:unselected-source (assoc-in input [:forms (get-in input [:forms :input :child-form-ids 0]) :parent-form-id] :stale)]
                             [:child-order (assoc-in input [:nodes :input :evaluation :order] [])]
                             [:extent (assoc-in input [:nodes :input :attributes :element-count] 1)]
                             [:selected-type (assoc-in input [:types :payload] :gravity.type/bool)]
                             [:selected-key (assoc-in input [:forms :payload-key :value] :stale)]]]
      (is (= :rejected (:status (derive changed))) (name label)))))

(defn- bounded-successor-input [extent]
  ;; Canonical relationships, with arbitrary binder names and member types.
  (let [module {:namespace 'example.successor :profile :meta :target :jvm :effects [:memory/allocate] :capabilities []}
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                 :kind kind :value value :parent-form-id parent :child-form-ids children})
        node (fn [id kind children attrs scope] {:node-id id :core-form kind :children children :attributes attrs :binding-context scope
                  :resolved-binding-ids [] :source (source id)
                  :evaluation {:kind :left-to-right :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}})
        binding (fn [operation] (-> (get-in (integral-ordering-input operation []) [:bindings :ordering-binding])
                                    (assoc :binding-id operation :definition-syntax-id operation)))
        bindings {'inc (binding 'inc) '< (binding '<) 'count (binding 'count)
                  :slot {:binding-id :slot :name 'cursor :binding-class :lexical :scope-id :body :definition-syntax-id :slot-name}
                  :values-binding {:binding-id :values-binding :name 'payloads :binding-class :lexical :scope-id :outer :definition-syntax-id :values-name}}
        operator (fn [id operation] (-> (node id :reference [] {:symbol operation :position :operator :binding-id operation
                         :binding-class :core :definition-syntax-id operation} :body) (assoc :resolved-binding-ids [operation])))
        reference (fn [id bid] (assoc (node id :reference [] {:binding-id bid} :body) :resolved-binding-ids [bid]))
        call-node (fn [id op args bid]
                    (-> (node id :call (into [op] args) {:operator-child-index 0 :argument-count (count args)
                         :argument-child-indexes (vec (range 1 (inc (count args)))) :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call} :body)
                        (assoc :resolved-binding-ids [bid]) (assoc-in [:evaluation :kind] :operator-then-arguments)))
        members (mapv #(keyword (str "member-" %)) (range extent))
        vector-type {:kind :gravity.type/vector :element-types (vec (repeat extent :gravity.type/bool)) :shape :literal}
        nodes (vec (concat (map #(node % :literal [] {:literal-kind :boolean :value true} :outer) members)
                [(node :values :collection-literal members {:literal-kind :vector :element-count extent} :outer)
                 (node :zero :literal [] {:literal-kind :integer :value 0} :outer)
                 (operator :count-op 'count) (reference :values-ref :values-binding) (call-node :count-query :count-op [:values-ref] 'count)
                 (operator :less-op '<) (reference :guard-slot :slot) (call-node :guard :less-op [:guard-slot :count-query] '<)
                 (operator :inc-op 'inc) (reference :inc-slot :slot) (call-node :increment :inc-op [:inc-slot] 'inc)
                 (-> (node :again :recur [:increment] {:target-id :target :target-kind :loop :arity 1 :type-compatibility :pending-sh08} :body)
                     (assoc :resolved-binding-ids [:slot]) (assoc-in [:evaluation :kind] :arguments-left-to-right-then-transfer))
                 (reference :exit :slot)
                 (assoc-in (node :branch :if [:guard :again :exit] {} :body) [:evaluation :order] [{:index 0 :core-node-id :guard}])
                 (-> (node :loop :loop [:zero :branch] {:target-id :target :binding-count 1 :body-count 1 :initializer-child-indexes [0]
                                                       :body-child-indexes [1] :result-policy {:kind :last-body :child-index 1}} :outer)
                     (assoc :resolved-binding-ids [:slot]) (assoc-in [:evaluation :kind] :initializers-then-body-left-to-right))
                 (assoc (node :let :let [:values :loop] {:body-count 1 :binding-vector-form-id :let-bindings :binding-vector-syntax-id :let-bindings} :outer)
                        :resolved-binding-ids [:values-binding])]))
        call (fn [id op args bid] {:core-node-id id :operator-node-id op :operator-binding-id bid :argument-node-ids args
                    :ordered-evaluation-node-ids (into [op] args) :evaluation-order :operator-then-arguments :result-policy :call-result})
        calls [(call :count-query :count-op [:values-ref] 'count) (call :guard :less-op [:guard-slot :count-query] '<)
               (call :increment :inc-op [:inc-slot] 'inc)]
        edges (mapv (fn [i call] {:ordinal i :call-core-node-id (:core-node-id call) :caller-function-syntax-id nil :caller-function-core-node-id nil
                     :callee-binding-id (:operator-binding-id call) :callee-definition-syntax-id (:operator-binding-id call)
                     :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids (:argument-node-ids call)
                     :ordered-evaluation-node-ids (:ordered-evaluation-node-ids call) :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}) (range) calls)
        forms (into {} (map (juxt :form-id identity)
                  (concat (map #(form % :boolean true :values []) members)
                    [(form :values :vector [] :let-bindings members) (form :zero :integer 0 :loop-bindings [])
                     (form :let :list nil nil [:let-op :let-bindings :loop]) (form :let-bindings :vector nil :let [:values-name :values])
                     (form :values-name :symbol 'payloads :let-bindings [])
                     (form :loop :list nil :let [:loop-op :loop-bindings :branch])
                     (form :loop-bindings :vector nil :loop [:slot-name :zero]) (form :slot-name :symbol 'cursor :loop-bindings [])
                     (form :branch :list nil :loop [:if-op :guard :again :exit])
                     (form :again :list nil :branch [:recur-op :increment]) (form :exit :symbol 'cursor :branch [])]
                    (mapcat (fn [call] (let [id (:core-node-id call) op (:operator-node-id call) args (:argument-node-ids call)]
                      [(form id :list nil (case id :count-query :guard :guard :branch :increment :again) (:ordered-evaluation-node-ids call))
                       (form op :symbol (:operator-binding-id call) id [])])) calls)
                    [(form :values-ref :symbol 'payloads :count-query []) (form :guard-slot :symbol 'cursor :guard [])
                     (form :inc-slot :symbol 'cursor :increment [])])))
        slot {:loop-core-node-id :loop :ordinal 0 :name 'cursor :binding-id :slot :definition-form-id :slot-name :definition-syntax-id :slot-name
              :binding-scope-id :body :initializer-form-id :zero :initializer-syntax-id :zero :initializer-scope-id :outer
              :initializer-node-id :zero :visible-prior-binding-ids [] :mutability :immutable}
        target {:target-id :target :target-kind :loop :owner-core-node-id :loop :owner-form-id :loop :owner-syntax-id :loop :arity 1
                :binding-ids [:slot] :body-scope-id :body :parent-target-id nil :type-compatibility :pending-sh08}
        transfer {:recur-core-node-id :again :recur-form-id :again :recur-syntax-id :again :target-id :target :target-kind :loop
                  :argument-node-ids [:increment] :arity 1 :tail-position true :evaluation-order :arguments-left-to-right
                  :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        lexical {:let-core-node-id :let :ordinal 0 :name 'payloads :binding-id :values-binding :definition-form-id :values-name
                 :definition-syntax-id :values-name :binding-scope-id :outer :initializer-form-id :values :initializer-syntax-id :values
                 :initializer-scope-id :outer :initializer-node-id :values :mutability :immutable}
        core {:module module :nodes nodes :calls calls :call-edges edges :function-records [] :definitions [] :lexical-bindings [lexical]
              :loop-bindings [slot] :recur-targets [target] :recur-transfers [transfer]}
        types (merge (zipmap members (repeat :gravity.type/bool))
                      {:values vector-type :values-ref vector-type :zero :gravity.type/integer :count-query :gravity.type/integer
                       :guard-slot :gravity.type/integer :inc-slot :gravity.type/integer :increment :gravity.type/integer
                       :guard :gravity.type/bool :again :gravity.type/never :exit :gravity.type/integer :branch :gravity.type/integer
                       :loop :gravity.type/integer :let :gravity.type/integer})]
    {:core core :nodes (into {} (map (juxt :node-id identity) nodes)) :bindings bindings :types types
     :forms (-> forms (assoc-in [:values :scope-id] :outer) (assoc-in [:values-name :scope-id] :outer))}))

(defn- bounded-successor-derived [{:keys [core nodes bindings types forms]} budget]
  (invoke-c7 'sh08-si-derive [core (nodes :increment) (last (:calls core)) (last (:call-edges core)) nodes bindings types forms budget]))

(defn- bounded-successor-alias [input]
  (let [source {:form-id :alias-init :syntax-id :alias-init :semantic-span nil :origin-chain [] :generated-origin nil}
        initializer (-> (get-in input [:nodes :values-ref]) (assoc :node-id :alias-init :source source :binding-context :outer))
        reference (-> (get-in input [:nodes :values-ref]) (assoc :resolved-binding-ids [:alias-binding])
                      (assoc-in [:attributes :binding-id] :alias-binding))
        owner (-> (get-in input [:nodes :let]) (assoc :children [:values :alias-init :loop] :resolved-binding-ids [:values-binding :alias-binding])
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :values} {:index 1 :core-node-id :alias-init} {:index 2 :core-node-id :loop}]))
        record (-> (first (get-in input [:core :lexical-bindings]))
                   (assoc :ordinal 1 :name 'renamed-values :binding-id :alias-binding :definition-form-id :alias-name :definition-syntax-id :alias-name
                          :initializer-node-id :alias-init :initializer-form-id :alias-init :initializer-syntax-id :alias-init))
        nodes (assoc (:nodes input) :alias-init initializer :values-ref reference :let owner)]
    (-> input (assoc :nodes nodes) (assoc-in [:core :nodes] (conj (mapv #(get nodes (:node-id %)) (get-in input [:core :nodes])) initializer))
        (update-in [:core :lexical-bindings] conj record)
        (assoc-in [:bindings :alias-binding] {:binding-id :alias-binding :name 'renamed-values :binding-class :lexical :scope-id :outer :definition-syntax-id :alias-name})
        (assoc-in [:types :alias-init] (get-in input [:types :values]))
        (assoc-in [:forms :values-ref :value] 'renamed-values)
        (assoc-in [:forms :let-bindings :child-form-ids] [:values-name :values :alias-name :alias-init])
        (assoc-in [:forms :alias-name] {:form-id :alias-name :syntax-id :alias-name :kind :symbol :value 'renamed-values :scope-id :outer :parent-form-id :let-bindings})
        (assoc-in [:forms :alias-init] {:form-id :alias-init :syntax-id :alias-init :source-span nil :origin-chain [] :generated-origin nil
                                      :kind :symbol :value 'payloads :scope-id :outer :parent-form-id :let-bindings :child-form-ids []}))))

(deftest sh08-bounded-successor-development-kernel
  (doseq [extent [0 1 3]]
    (let [input (bounded-successor-input extent) result (bounded-successor-derived input 65536)
          descriptor (:descriptor result) proof (:range-proof descriptor)]
      (is (= :accepted (:status result))
          (pr-str (if (= :accepted (:status result)) result
                    (let [{:keys [core nodes bindings types forms]} input
                          working (assoc core :binding-table (vec (vals bindings)))
                          prepared (invoke-c7 'sh08-ft-loop-kernel-prepare [working 65536])]
                      {:result result :loop (invoke-c7 'sh08-ft-loop-type-step [working types {} 65536])
                       :origin (invoke-c7 'sh08-si-vector-origin [:values-ref :loop (get types :values-ref) core nodes types bindings forms (:parents prepared) #{} 0])
                       :count (invoke-c7 'sh08-cq-source [core (nodes :count-query) (first (:calls core)) (first (:call-edges core)) nodes bindings types (:module core)])
                       :guard (invoke-c7 'sh08-io-descriptor [core (nodes :guard) (second (:calls core)) (second (:call-edges core)) nodes bindings types (:module core)])
                       :call-forms (mapv #(invoke-c7 'sh08-si-call-forms? [(nodes %) nodes forms]) [:guard :count-query :increment])
                       :refs (mapv #(invoke-c7 'sh08-si-ref? [(nodes %) :slot forms bindings]) [:exit :guard-slot :inc-slot])}))))
      (is (= :gravity.type/integer (:type result) (:result-type descriptor)))
      (is (= :proof-required (:numeric-mode descriptor) (:numeric-mode proof)))
      (is (= :proven-safe (:outcome proof)))
      (is (= {:lower 0 :upper extent :relation :zero-le-counter-le-extent} (:invariant proof)))
      (is (= (pos? extent) (get-in proof [:guarded-edge :satisfiable])))
      (is (= :normal (:application descriptor)))
      (is (= :retained (:runtime-guards proof)))
      (is (= :not-authorized (:check-elision proof)))
      (when (= :accepted (:status result))
        (let [{:keys [core nodes bindings types]} input
              verify #(invoke-c7 'sh08-si-inferred-call-valid?
                                 [core (nodes :increment) (last (:calls core)) (last (:call-edges core)) nodes bindings
                                  (assoc types :inc-op %)])]
          (is (true? (verify descriptor)))
          (doseq [changed [(assoc-in descriptor [:range-proof :vector-extent] (inc extent))
                           (assoc-in descriptor [:range-proof :invariant :upper] (inc extent))
                           (assoc-in descriptor [:range-proof :guarded-edge :satisfiable] (zero? extent))
                           (assoc-in descriptor [:range-proof :backedge-paths] [])
                           (assoc-in descriptor [:range-proof :backedge-paths] [7])
                           (assoc-in descriptor [:range-proof :backedge-paths] [nil])
                           (assoc-in descriptor [:range-proof :backedge-paths] [{}])
                           (assoc-in descriptor [:range-proof :backedge-paths] ["path"])
                           (assoc-in descriptor [:range-proof :normal-exit :core-node-id] :inc-slot)
                           (assoc-in descriptor [:range-proof :target :target-id] :other-target)
                           (assoc-in descriptor [:range-proof :check-elision] :authorized)]]
            (is (false? (verify changed))))))
      (when (= :accepted (:status result))
        (is (= result (bounded-successor-derived input (:work-count result)))))
        (is (= :bounded-successor-work-limit (:reason (bounded-successor-derived input (dec (:work-count result))))))))
  (let [input (bounded-successor-input 3)]
    (is (= :bounded-successor-work-limit (:reason (bounded-successor-derived input 0))))
    (is (= :bounded-successor-work-limit (:reason (bounded-successor-derived input 65))))
    (let [alias (bounded-successor-alias input) result (bounded-successor-derived alias 65536)]
      (is (= :accepted (:status result)) (pr-str result))
      (is (= 2 (count (get-in result [:descriptor :range-proof :vector-origin :binding-links]))))
      (is (= result (bounded-successor-derived alias (:work-count result))))
      (is (= :bounded-successor-work-limit (:reason (bounded-successor-derived alias (dec (:work-count result)))))))
    (let [grown (assoc-in input [:core :function-records] (mapv #(hash-map :function-syntax-id (keyword (str "unrelated-" %))) (range 3)))
          original (bounded-successor-derived input 65536) result (bounded-successor-derived grown 65536)]
      (is (= :accepted (:status result)))
      (is (< (:work-count original) (:work-count result)))
      (is (= :bounded-successor-work-limit (:reason (bounded-successor-derived grown (:work-count original)))))
      (is (= result (bounded-successor-derived grown (:work-count result)))))
    (let [{:keys [core nodes bindings types forms]} input
          result (invoke-c7 'sh08-ft-infer-pass-with-context
                           [(:nodes core) (:lexical-bindings core) [] (:calls core) (:call-edges core) {}
                            {:slot :gravity.type/integer :values-binding (types :values)}
                            {:ordering-core core :lookup-nodes nodes :lookup-bindings (vec (vals bindings)) :lookup-forms forms
                             :loop-core (assoc core :binding-table (vec (vals bindings))) :loop-work-left 65536}])]
      (is (= :gravity.type/integer (get-in result [:type-table :increment])) (pr-str (:diagnostics result)))
      (is (= :gravity.type/applied-bounded-successor (get-in result [:type-table :inc-op :kind])))
      (is (= 1 (count (filter #(= :sh08-bounded-successor-v1 (:producer-rule %)) (:constraints result))))))
    (doseq [[label changed] [[:initial (assoc-in input [:nodes :zero :attributes :value] 1)]
                             [:guard (assoc-in input [:nodes :guard :children] [:less-op :count-query :guard-slot])]
                             [:slot (assoc-in input [:nodes :inc-slot :attributes :binding-id] :values-binding)]
                             [:extent (assoc-in input [:types :values-ref :element-types] [])]
                             [:transfer (assoc-in input [:core :recur-transfers 0 :argument-node-ids] [:guard-slot])]
                             [:missing-transfer (assoc-in input [:core :recur-transfers] [])]
                             [:target (assoc-in input [:core :recur-targets 0 :arity] 2)]
                             [:family (assoc-in input [:types :inc-slot] :gravity.type/i64)]
                             [:call-source (assoc-in input [:forms :inc-op :value] 'other)]]]
      (is (= :rejected (:status (bounded-successor-derived changed 65536))) (name label)))))

(deftest sh08-bounded-successor-extent-bound-development-kernel
  (let [maximum (bounded-successor-derived (bounded-successor-input 1024) 65536)
        over (bounded-successor-derived (bounded-successor-input 1025) 65536)]
    (is (= :accepted (:status maximum)))
    (is (= 1024 (get-in maximum [:descriptor :range-proof :vector-extent])
                 (get-in maximum [:descriptor :range-proof :maximum-vector-extent])))
    (is (= ["STD5001" :bounded-successor-range-proof-required] ((juxt :rule :reason) over)))))


(defn- bounded-successor-two-backedges [input]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        rename {:increment :increment-2 :inc-op :inc-op-2 :inc-slot :inc-slot-2}
        nodes (reduce (fn [out [old id]]
                        (let [node (get-in input [:nodes old])]
                          (assoc out id (-> node (assoc :node-id id :source (source id))
                                           (update :children #(mapv (fn [child] (get rename child child)) %))
                                           (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id (fn [child] (get rename child child)))) %))))))
                      (:nodes input) rename)
        again (-> (nodes :again) (assoc :node-id :again-2 :source (source :again-2) :children [:increment-2])
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :increment-2}]))
        choose {:node-id :choose :core-form :reference :children [] :attributes {:binding-id :choice-binding} :resolved-binding-ids [:choice-binding]
                :source (source :choose) :binding-context :body :evaluation {:kind :left-to-right :order []}}
        choice {:node-id :choice :core-form :if :children [:choose :again :again-2] :attributes {} :resolved-binding-ids []
                :source (source :choice) :binding-context :body :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :choose}]}}
        branch (assoc-in (nodes :branch) [:children 1] :choice)
        nodes (assoc nodes :again-2 again :choose choose :choice choice :branch branch)
        call (-> (last (get-in input [:core :calls])) (assoc :core-node-id :increment-2 :operator-node-id :inc-op-2 :argument-node-ids [:inc-slot-2]
                                                            :ordered-evaluation-node-ids [:inc-op-2 :inc-slot-2]))
        edge (-> (last (get-in input [:core :call-edges])) (assoc :ordinal 3 :call-core-node-id :increment-2 :argument-core-node-ids [:inc-slot-2]
                                                                :ordered-evaluation-node-ids [:inc-op-2 :inc-slot-2]))
        transfer (assoc (first (get-in input [:core :recur-transfers])) :recur-core-node-id :again-2 :recur-form-id :again-2
                                                                                :recur-syntax-id :again-2 :argument-node-ids [:increment-2])
        index (.indexOf (mapv :node-id (get-in input [:core :nodes])) :inc-op)
        before (subvec (get-in input [:core :nodes]) 0 index)
        after (mapv #(nodes (:node-id %)) (subvec (get-in input [:core :nodes]) index))
        ordered (vec (concat before [choose] (take 4 after) [(nodes :inc-op-2) (nodes :inc-slot-2) (nodes :increment-2) again choice] (drop 4 after)))]
    (-> input (assoc :nodes nodes) (assoc-in [:core :nodes] ordered)
        (update-in [:core :calls] conj call) (update-in [:core :call-edges] conj edge) (update-in [:core :recur-transfers] conj transfer)
        (assoc-in [:bindings :choice-binding] {:binding-id :choice-binding :binding-class :lexical :name 'choose})
        (update :types merge {:choose :gravity.type/bool :choice :gravity.type/never :again-2 :gravity.type/never
                              :inc-slot-2 :gravity.type/integer :increment-2 :gravity.type/integer})
        (update :forms (fn [forms] (reduce (fn [out [old id]]
                                           (assoc out id (-> (forms old) (assoc :form-id id :syntax-id id)
                                                             (update :child-form-ids #(mapv (fn [child] (get rename child child)) %))
                                                             (update :parent-form-id #(get rename % %))))) forms rename)))
        (assoc-in [:forms :branch :child-form-ids 2] :choice)
        (assoc-in [:forms :again :parent-form-id] :choice)
        (assoc-in [:forms :increment-2 :parent-form-id] :again-2)
        (assoc-in [:forms :again-2] {:form-id :again-2 :syntax-id :again-2 :source-span nil :origin-chain [] :generated-origin nil
                                    :kind :list :parent-form-id :choice :child-form-ids [:recur-op-2 :increment-2]})
        (assoc-in [:forms :choice] {:form-id :choice :syntax-id :choice :source-span nil :origin-chain [] :generated-origin nil
                                   :kind :list :parent-form-id :branch :child-form-ids [:if-op-2 :choose :again :again-2]})
        (assoc-in [:forms :choose] {:form-id :choose :syntax-id :choose :source-span nil :origin-chain [] :generated-origin nil
                                   :kind :symbol :value 'choose :parent-form-id :choice :child-form-ids []}))))

(deftest sh08-bounded-successor-multiple-backedges-development-kernel
  (let [{:keys [core nodes bindings types forms] :as input} (bounded-successor-two-backedges (bounded-successor-input 3))
        derive (fn [id] (invoke-c7 'sh08-si-derive [core (nodes id) (first (filter #(= id (:core-node-id %)) (:calls core)))
                                                   (first (filter #(= id (:call-core-node-id %)) (:call-edges core))) nodes bindings types forms 65536]))]
    (doseq [id [:increment :increment-2]]
      (let [result (derive id) proof (get-in result [:descriptor :range-proof])]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= 2 (count (:incoming-transfers proof)) (count (:backedge-paths proof))))
        (is (true? (invoke-c7 'sh08-si-proof-closed? [proof core (nodes id) nodes bindings types])))))
    (let [context {:ordering-core core :lookup-nodes nodes :lookup-bindings (vec (vals bindings)) :lookup-forms forms
                   :loop-core (assoc core :binding-table (vec (vals bindings))) :loop-work-left 65536}
          inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                               [(:nodes core) nodes (:lexical-bindings core) [] (:calls core) (:call-edges core) {}
                                {:choice-binding :gravity.type/bool} 16 context])]
      (is (= :converged (:convergence-status inference)) (pr-str (:diagnostics inference)))
      (is (= :gravity.type/integer (get-in inference [:type-table :increment]) (get-in inference [:type-table :increment-2])))
      (is (= 2 (count (filter #(= :sh08-bounded-successor-v1 (:producer-rule %)) (:constraints inference))))))))


(deftest sh08-bounded-successor-guard-relations-development-kernel
  (let [base (bounded-successor-input 3)
        branch (assoc-in (get-in base [:nodes :branch]) [:children] [:guard :exit :again])
        swapped (-> base (assoc-in [:nodes :branch] branch)
                    (update-in [:core :nodes] #(mapv (fn [node] (if (= :branch (:node-id node)) branch node)) %))
                    (assoc-in [:forms :branch :child-form-ids] [:if-op :guard :exit :again]))]
    (is (= ["STD5001" :bounded-successor-range-proof-required]
           ((juxt :rule :reason) (bounded-successor-derived swapped 65536))))
    (doseq [operation '[<= > >=]]
      (let [operator (-> (get-in base [:nodes :less-op]) (assoc :resolved-binding-ids [operation])
                         (assoc :attributes {:symbol operation :position :operator :binding-id operation :binding-class :core :definition-syntax-id operation}))
            guard (assoc (get-in base [:nodes :guard]) :resolved-binding-ids [operation])
            call (assoc (second (get-in base [:core :calls])) :operator-binding-id operation)
            edge (assoc (second (get-in base [:core :call-edges])) :callee-binding-id operation :callee-definition-syntax-id operation)
            input (-> base (assoc-in [:nodes :less-op] operator) (assoc-in [:nodes :guard] guard)
                      (update-in [:core :nodes] #(mapv (fn [node] (case (:node-id node) :less-op operator :guard guard node)) %))
                      (assoc-in [:core :calls 1] call) (assoc-in [:core :call-edges 1] edge)
                      (assoc-in [:bindings operation] (assoc (get-in base [:bindings '<]) :binding-id operation :name operation :definition-syntax-id operation))
                      (assoc-in [:forms :less-op :value] operation))
            descriptor (invoke-c7 'sh08-io-descriptor [(:core input) guard call edge (:nodes input) (:bindings input) (:types input) (get-in input [:core :module])])]
        (is (= operation (:operation descriptor)))
        (is (= ["STD5001" :bounded-successor-range-proof-required]
               ((juxt :rule :reason) (bounded-successor-derived input 65536))))))))


(defn- vector-loop-nth-input [extent reverse?]
  ;; Current C7 algorithms on normalized core DATA; no source admission.
  (let [base (bounded-successor-input extent) template (vector-nth-input [:gravity.type/bool] 0)
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        form (fn [id kind value parent children] {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                                                 :kind kind :value value :parent-form-id parent :child-form-ids children})
        operator (-> (get-in template [:nodes :operator]) (assoc :node-id :nth-op :source (source :nth-op) :binding-context :body))
        operand (-> (get-in base [:nodes :values-ref]) (assoc :node-id :nth-vector :source (source :nth-vector)))
        index (-> (get-in base [:nodes :inc-slot]) (assoc :node-id :nth-index :source (source :nth-index)))
        children [:nth-op :nth-vector :nth-index]
        query (-> (:node template) (assoc :node-id :read :source (source :read) :children children :binding-context :body)
                  (assoc-in [:evaluation :order] (mapv #(hash-map :index %1 :core-node-id %2) (range) children)))
        problem {:node-id :problem :core-form :literal :children [] :attributes {:literal-kind :string :value "false flag"} :resolved-binding-ids []
                 :source (source :problem) :binding-context :body :evaluation {:kind :left-to-right :order []}}
        stop {:node-id :stop :core-form :throw :children [:problem] :attributes {} :resolved-binding-ids [] :source (source :stop)
              :binding-context :body :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :problem}]}}
        check-children (if reverse? [:read :stop :again] [:read :again :stop])
        check {:node-id :check :core-form :if :children check-children :attributes {} :resolved-binding-ids [] :source (source :check)
               :binding-context :body :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :read}]}}
        branch (assoc-in (get-in base [:nodes :branch]) [:children 1] :check)
        position (.indexOf (mapv :node-id (get-in base [:core :nodes])) :inc-op)
        before (subvec (get-in base [:core :nodes]) 0 position) after (subvec (get-in base [:core :nodes]) position)
        ordered (vec (concat before [operator operand index query] (take 4 after) [problem stop check] (drop 4 after)))
        ordered (mapv #(if (= :branch (:node-id %)) branch %) ordered)
        call (assoc (:call template) :core-node-id :read :operator-node-id :nth-op :argument-node-ids [:nth-vector :nth-index] :ordered-evaluation-node-ids children)
        edge (assoc (:edge template) :ordinal 2 :call-core-node-id :read :argument-core-node-ids [:nth-vector :nth-index] :ordered-evaluation-node-ids children)
        calls (vec (concat (take 2 (get-in base [:core :calls])) [call] (drop 2 (get-in base [:core :calls]))))
        edges (vec (concat (take 2 (get-in base [:core :call-edges])) [edge] [(assoc (last (get-in base [:core :call-edges])) :ordinal 3)]))
        input (-> base (assoc :nodes (into {} (map (juxt :node-id identity) ordered)))
                  (assoc-in [:core :nodes] ordered) (assoc-in [:core :calls] calls) (assoc-in [:core :call-edges] edges)
                  (assoc-in [:bindings :association-binding] (get-in template [:bindings :association-binding]))
                  (update :types merge {:nth-vector (get-in base [:types :values]) :nth-index :gravity.type/integer :read :gravity.type/bool
                                        :problem :gravity.type/string :stop :gravity.type/never :check :gravity.type/never})
                  (assoc-in [:forms :branch :child-form-ids 2] :check)
                  (assoc-in [:forms :again :parent-form-id] :check)
                  (update :forms merge {:nth-op (form :nth-op :symbol 'nth :read [])
                                        :nth-vector (form :nth-vector :symbol 'payloads :read [])
                                        :nth-index (form :nth-index :symbol 'cursor :read [])
                                        :read (form :read :list nil :check children)
                                        :problem (form :problem :string "false flag" :stop [])
                                        :stop (form :stop :list nil :check [:throw-op :problem])
                                        :check (form :check :list nil :branch (into [:check-op] check-children))}))]
    input))

(defn- vector-loop-nth-completed [input]
  (let [{:keys [core nodes bindings types forms]} input
        result (invoke-c7 'sh08-si-derive [core (nodes :increment) (last (:calls core)) (last (:call-edges core)) nodes bindings types forms 65536])]
    (assoc input :invariant-result result :types (assoc types :inc-op (:descriptor result)))))

(defn- vector-loop-nth-derived [{:keys [core nodes bindings types forms]} budget]
  (invoke-c7 'sh08-vl-derive [core (nodes :read) (get (:calls core) 2) (get (:call-edges core) 2) nodes bindings types forms budget]))

(deftest sh08-vector-loop-nth-development-kernel
  (doseq [reverse? [false true] extent [1 3]]
    (let [input (vector-loop-nth-completed (vector-loop-nth-input extent reverse?))
          result (vector-loop-nth-derived input 65536) descriptor (:descriptor result) proof (:bounds-proof descriptor)]
      (is (= :accepted (get-in input [:invariant-result :status])) (pr-str (:invariant-result input)))
      (is (= :accepted (:status result)) (pr-str result))
      (is (= :gravity.type/bool (:type result) (:selected-type descriptor)))
      (is (= :sh08-vector-nth-loop-v1 (:version descriptor)))
      (is (nil? (:index-value descriptor)))
      (is (= extent (:vector-length descriptor)))
      (is (= :proven-safe (:outcome proof)))
      (is (= :index-within-loop-guard-and-vector-origin (:scope proof)))
      (is (= :retained (:runtime-guards proof))) (is (= :not-authorized (:check-elision proof)))
      (when (= :accepted (:status result))
        (is (= result (vector-loop-nth-derived input (:work-count result)))))
        (is (= :vector-nth-loop-work-limit (:reason (vector-loop-nth-derived input (dec (:work-count result))))))))
  (let [input (vector-loop-nth-completed (vector-loop-nth-input 3 false))]
    (is (= :vector-nth-loop-work-limit (:reason (vector-loop-nth-derived input 0))))
    (doseq [changed [(assoc-in input [:types :nth-index] :gravity.type/string)
                     (assoc-in input [:types :nth-index] :gravity.type/unknown)
                     (assoc-in input [:nodes :nth-index :core-form] :call)
                     (assoc-in input [:forms :nth-index :value] 'shadowed)
                     (assoc-in input [:core :call-edges 2 :callee-function-syntax-id] :fake)
                     (assoc-in input [:types :nth-vector :element-types] [:gravity.type/bool :gravity.type/string :gravity.type/bool])
                     (assoc-in input [:types :inc-op] :gravity.type/core-var)]]
      (is (= :rejected (:status (vector-loop-nth-derived changed 65536))))))
  (let [input (vector-loop-nth-completed (vector-loop-nth-input 0 false))]
    (is (= :vector-nth-loop-homogeneous-element-type-required (:reason (vector-loop-nth-derived input 65536))))))

(defn- keyword-loop-match-input
  ([] (keyword-loop-match-input false))
  ([normal?]
   (let [base (vector-loop-nth-input 3 false) keyword-input (keyword-match-input)
         sha #(str "sha256:" (format "%064x" %))
         replacements {(:match-id keyword-input) :check (:scrutinee-id keyword-input) :read
                       (first (:bodies keyword-input)) :again (second (:bodies keyword-input)) :stop
                       (sha 12) :again (sha 13) :stop}
         pattern-core (walk/postwalk-replace replacements (:core keyword-input))
         pattern-forms (walk/postwalk-replace replacements (:form-table keyword-input))
         pattern-bindings (walk/postwalk-replace replacements (:binding-index keyword-input))
         rid (:remainder keyword-input)
         branches (mapv (fn [branch] (if (= 1 (:clause-ordinal branch))
                                       (assoc branch :pattern-binding-use-syntax-ids (if normal? [] [:problem])) branch))
                        (:match-branch-records pattern-core))
         patterns (mapv (fn [pattern] (if (= 1 (:clause-ordinal pattern))
                                       (assoc pattern :pattern-binding-use-syntax-ids (if normal? [] [:problem])) pattern))
                        (:match-pattern-records pattern-core))
         match-node (-> (last (:nodes pattern-core))
                        (assoc :source (get-in base [:nodes :check :source]) :binding-context :body))
         problem (-> (get-in base [:nodes :problem])
                     (assoc :core-form :reference :children [] :attributes {:binding-id rid} :resolved-binding-ids [rid]))
         stop (if normal?
                (-> (get-in base [:nodes :stop])
                    (assoc :core-form :literal :children [] :attributes {:literal-kind :string :value "normal branch"} :resolved-binding-ids [])
                    (assoc :evaluation {:kind :left-to-right :order []}))
                (get-in base [:nodes :stop]))
         sequence (vec (keep (fn [node]
                               (let [id (:node-id node)]
                                 (cond (= :check id) match-node
                                       (= :problem id) (when-not normal? problem)
                                       (= :stop id) (assoc-in stop [:evaluation :region] (:conditional-region (branches 1)))
                                       (= :again id) (assoc-in node [:evaluation :region] (:conditional-region (branches 0)))
                                       (str/starts-with? (name id) "member-") (assoc node :attributes {:literal-kind :keyword :value :read})
                                       :else node))) (get-in base [:core :nodes])))
         forms (-> (merge (:forms base) pattern-forms)
                   (assoc :read (get-in base [:forms :read]) :again (get-in base [:forms :again]))
                   (assoc-in [:check :source-span] nil)
                   (assoc :stop (if normal?
                                  {:form-id :stop :syntax-id :stop :kind :string :value "normal branch" :source-span nil
                                   :origin-chain [] :generated-origin nil :parent-form-id :check :child-form-ids []}
                                  (get-in base [:forms :stop])))
                   (assoc-in [:problem :kind] :symbol) (assoc-in [:problem :value] 'remainder))
         forms (into {} (map (fn [[id form]] [id (if (str/starts-with? (name id) "member-") (assoc form :kind :keyword :value :read) form)]) forms))
         nodes (into {} (map (juxt :node-id identity) sequence))
         bindings (merge (:bindings base) pattern-bindings)
         core (-> (:core base) (assoc :nodes sequence :match-branch-records branches :match-pattern-records patterns
                                     :match-decision-skeletons (:match-decision-skeletons pattern-core)))
         products {:status :accepted :node-table nodes :binding-index bindings :lexical-bindings (:lexical-bindings core) :calls (:calls core)}]
     (assoc base :core core :nodes nodes :forms forms :bindings bindings :products products))))

(deftest sh08-keyword-loop-match-cold-inference-development-kernel
  (doseq [normal? [false true]]
    (let [{:keys [core nodes forms bindings products]} (keyword-loop-match-input normal?)
          joined (invoke-c7 'sh08-bm-products [core products (vec (vals forms))])
          context {:ordering-core core :lookup-nodes nodes :lookup-bindings (vec (vals bindings)) :lookup-forms forms
                   :loop-core (assoc core :binding-table (vec (vals bindings))) :loop-work-left 65536
                   :boolean-matches (:boolean-matches joined)}
          result (invoke-c7 'sh08-ft-infer-acyclic-with-context
                           [(:nodes core) nodes (:lexical-bindings core) [] (:calls core) (:call-edges core) {} {} 20 context])
          types (:type-table result)]
      (is (= :accepted (:status joined)) (pr-str joined))
      (is (= :converged (:convergence-status result)) (pr-str (:diagnostics result)))
      (is (= (if normal? :gravity.type/string :gravity.type/never) (get types :check)))
      (if normal?
        (is (not= :sh08-vector-nth-loop-v1 (get-in types [:nth-op :version])))
        (let [groups (invoke-c7 'sh08-sm-groups-with-call-context
                               [(invoke-c7 'sh08-sm-groups-with-shapes [(:boolean-matches joined) (:binding-shapes result)])
                                (:shape-call-context result)])
              checked (invoke-c7 'sh08-bm-checks [groups types {} (:module core)])]
          (is (= [] (:diagnostics result) (:diagnostics checked)))
          (is (= :gravity.type/keyword (get types :read) (get-in types [:nth-op :result-type]) (get types :problem)))
          (is (= :gravity.type/integer (get types :increment)))
          (is (= 2 (count (:constraints checked))))
          (is (= :sh08-vector-nth-loop-v1 (get-in types [:nth-op :version])))
          (is (= {:excluded-values []} (get-in checked [:constraints 0 :constructor-shape :domain])))
          (is (= :vector-nth-loop-selected-shape-unsupported
                 (:reason (invoke-c7 'sh08-vn-shape [(nodes :read) nodes types {} {} [] bindings forms #{} 0 {} {}])))))))))

(deftest sh08-vector-loop-nth-selected-shape-development-kernel
  (let [{:keys [nodes types bindings forms] :as input} (vector-loop-nth-completed (vector-loop-nth-input 3 false))
        result (vector-loop-nth-derived input 65536)
        shape (invoke-c7 'sh08-vn-shape [(nodes :read) nodes (assoc types :nth-op (:descriptor result))
                                       {} {} [] bindings forms #{} 0 {} {}])]
    (is (= :accepted (:status result)))
    (is (= ["C7-ANNOTATION" :vector-nth-loop-selected-shape-unsupported] ((juxt :rule :reason) shape)))))

(deftest sh08-vector-loop-nth-cold-inference-development-kernel
  (doseq [reverse? [false true]]
    (let [{:keys [core nodes bindings forms]} (vector-loop-nth-input 3 reverse?)
          context {:ordering-core core :lookup-nodes nodes :lookup-bindings (vec (vals bindings)) :lookup-forms forms
                   :loop-core (assoc core :binding-table (vec (vals bindings))) :loop-work-left 65536}
          result (invoke-c7 'sh08-ft-infer-acyclic-with-context
                           [(:nodes core) nodes (:lexical-bindings core) [] (:calls core) (:call-edges core) {} {} 16 context])
          types (:type-table result) descriptor (get types :nth-op)]
      (is (= :converged (:convergence-status result)) (pr-str (:diagnostics result)))
      (is (= [] (:diagnostics result)))
      (is (= :gravity.type/bool (get types :read) (:result-type descriptor)))
      (is (= :gravity.type/integer (get types :increment)))
      (is (= :sh08-vector-nth-loop-v1 (:version descriptor)))
      (is (= 1 (count (filter #(= :sh08-vector-nth-v1 (:producer-rule %)) (:constraints result)))))
      (is (true? (invoke-c7 'sh08-vn-inferred-call-valid? [core (nodes :read) (get (:calls core) 2) (get (:call-edges core) 2) nodes bindings types])))
      (let [prepared (assoc context :ordering-operators (into {} (map (juxt :operator-node-id identity) (:calls core)))
                                    :ordering-edges (into {} (map (juxt :call-core-node-id identity) (:call-edges core)))
                                    :ordering-bindings bindings :association-forms forms :prior-type-table types)]
        (doseq [current [{:nth-index :gravity.type/string} {:inc-op :gravity.type/core-var}
                         {:nth-vector {:kind :gravity.type/vector :element-types [:gravity.type/bool :gravity.type/string :gravity.type/bool] :shape :literal}}]]
          (is (= :rejected (:status (invoke-c7 'sh08-vn-step [(nodes :read) current {} {} prepared 65536])))))))))


(defn- vector-loop-nth-nested-target [input]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        zero (-> (get-in input [:nodes :zero]) (assoc :node-id :inner-zero :source (source :inner-zero) :binding-context :body))
        read (assoc (get-in input [:nodes :read]) :binding-context :inner-body)
        owner (-> (get-in input [:nodes :loop]) (assoc :node-id :inner-loop :source (source :inner-loop) :children [:inner-zero :read]
                                                      :resolved-binding-ids [:inner-slot] :binding-context :body)
                  (assoc-in [:attributes :target-id] :inner-target)
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :inner-zero} {:index 1 :core-node-id :read}]))
        check (-> (get-in input [:nodes :check]) (assoc-in [:children 0] :inner-loop)
                  (assoc-in [:evaluation :order] [{:index 0 :core-node-id :inner-loop}]))
        nodes (assoc (:nodes input) :inner-zero zero :inner-loop owner :read read :check check)
        ordered (vec (mapcat (fn [node] (case (:node-id node) :nth-op [zero node] :read [read owner] :check [check] [node]))
                             (get-in input [:core :nodes])))
        target {:target-id :inner-target :target-kind :loop :owner-core-node-id :inner-loop :owner-form-id :inner-loop :owner-syntax-id :inner-loop
                :arity 1 :binding-ids [:inner-slot] :body-scope-id :inner-body :parent-target-id :target :type-compatibility :pending-sh08}
        slot (assoc (first (get-in input [:core :loop-bindings])) :loop-core-node-id :inner-loop :name 'inner :binding-id :inner-slot
                    :definition-form-id :inner-name :definition-syntax-id :inner-name :binding-scope-id :inner-body
                    :initializer-form-id :inner-zero :initializer-syntax-id :inner-zero :initializer-scope-id :body :initializer-node-id :inner-zero)]
    (-> input (assoc :nodes nodes) (assoc-in [:core :nodes] ordered)
        (update-in [:core :recur-targets] conj target) (update-in [:core :loop-bindings] conj slot)
        (assoc-in [:bindings :inner-slot] {:binding-id :inner-slot :name 'inner :binding-class :lexical :scope-id :inner-body :definition-syntax-id :inner-name})
        (update :types merge {:inner-zero :gravity.type/integer :inner-loop :gravity.type/bool})
        (assoc-in [:forms :read :parent-form-id] :inner-loop)
        (assoc-in [:forms :check :child-form-ids 1] :inner-loop)
        (assoc-in [:forms :inner-zero] {:form-id :inner-zero :syntax-id :inner-zero :source-span nil :origin-chain [] :generated-origin nil
                                       :kind :integer :value 0 :scope-id :body :parent-form-id :inner-bindings :child-form-ids []})
        (assoc-in [:forms :inner-loop] {:form-id :inner-loop :syntax-id :inner-loop :source-span nil :origin-chain [] :generated-origin nil
                                       :kind :list :parent-form-id :check :child-form-ids [:inner-loop-op :inner-bindings :read]}))))

(defn- vector-loop-nth-other-vector [input]
  (let [input (bounded-successor-alias input) original (get-in input [:nodes :values])
        ids (:children original) rename (zipmap ids (map #(keyword (str "other-" (name %))) ids))
        members (mapv (fn [id] (-> (get-in input [:nodes id]) (assoc :node-id (rename id))
                                   (assoc-in [:source :form-id] (rename id)) (assoc-in [:source :syntax-id] (rename id)))) ids)
        operand (-> (get-in input [:nodes :alias-init])
                    (assoc :core-form :collection-literal :children (mapv rename ids) :resolved-binding-ids []
                           :attributes {:literal-kind :vector :element-count (count ids)})
                    (assoc :evaluation {:kind :left-to-right :order (mapv #(hash-map :index %1 :core-node-id %2) (range) (mapv rename ids))}))
        nodes (into (assoc (:nodes input) :alias-init operand) (map (juxt :node-id identity) members))
        forms (reduce (fn [out id] (assoc out (rename id) (-> (get-in input [:forms id]) (assoc :form-id (rename id) :syntax-id (rename id) :parent-form-id :alias-init))))
                      (:forms input) ids)]
    (-> input (assoc :nodes nodes :forms forms)
        (assoc-in [:core :nodes] (vec (mapcat #(if (= :alias-init (:node-id %)) (conj members operand) [%]) (get-in input [:core :nodes]))))
        (update :types merge (zipmap (mapv rename ids) (repeat :gravity.type/bool)))
        (assoc-in [:forms :alias-init :kind] :vector) (assoc-in [:forms :alias-init :value] (vec (repeat (count ids) true)))
        (assoc-in [:forms :alias-init :child-form-ids] (mapv rename ids)))))

(deftest sh08-vector-loop-nth-origin-scope-development-kernel
  (let [base (vector-loop-nth-input 3 false)
        alias (vector-loop-nth-completed (bounded-successor-alias base))
        other (vector-loop-nth-completed (vector-loop-nth-other-vector base))
        nested (vector-loop-nth-completed (vector-loop-nth-nested-target base))]
    (is (= :accepted (get-in alias [:invariant-result :status]) (get-in other [:invariant-result :status]) (get-in nested [:invariant-result :status])))
    (is (= :accepted (:status (vector-loop-nth-derived alias 65536))))
    (doseq [input [other nested]]
      (is (= ["SAFE2-BOUNDS" :vector-nth-loop-proof-or-origin-required]
             ((juxt :rule :reason) (vector-loop-nth-derived input 65536))))))
  (let [{:keys [core nodes bindings types] :as input} (vector-loop-nth-completed (vector-loop-nth-input 3 false))
        descriptor (:descriptor (vector-loop-nth-derived input 65536))
        verify #(invoke-c7 'sh08-vn-inferred-call-valid?
                           [core (nodes :read) (get (:calls core) 2) (get (:call-edges core) 2) nodes bindings (assoc types :nth-op %)])]
    (is (true? (verify descriptor)))
    (doseq [changed [(assoc-in descriptor [:bounds-proof :slot-binding-id] :shadowed)
                     (assoc-in descriptor [:bounds-proof :index-node-id] :increment)
                     (assoc-in descriptor [:bounds-proof :vector-length] 4)
                     (assoc-in descriptor [:bounds-proof :selected-type] :gravity.type/string)
                     (assoc-in descriptor [:bounds-proof :loop-invariant :call-core-node-id] :guard)
                     (assoc-in descriptor [:bounds-proof :guard-path] [])
                     (assoc-in descriptor [:bounds-proof :guard-path] 7)
                     (assoc-in descriptor [:bounds-proof :guard-path] [7])
                     (assoc-in descriptor [:bounds-proof :guard-path] [nil])
                     (assoc-in descriptor [:bounds-proof :guard-path] [{}])
                     (assoc-in descriptor [:bounds-proof :guard-path] ["path"])
                     (assoc-in descriptor [:bounds-proof :guard-path] (get-in descriptor [:bounds-proof :loop-invariant :range-proof :backedge-paths 0]))
                     (assoc-in descriptor [:bounds-proof :guard-path 1 :child-index] 2)
                     (assoc-in descriptor [:bounds-proof :vector-origin :node] (nodes :loop))
                     (assoc-in descriptor [:bounds-proof :check-elision] :authorized)
                     (assoc descriptor :return :gravity.type/string :result-type :gravity.type/string :selected-type :gravity.type/string)]]
      (is (false? (verify changed))))))


(deftest sh08-integral-ordering-development-kernel
  (doseq [operation '[< <= > >=]
          type [:gravity.type/integer :gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64]]
    (let [input (integral-ordering-input operation [type type]) descriptor (integral-ordering-descriptor input)
          row (invoke-c7 'sh08-io-constraint [descriptor])]
      (is (= :gravity.type/applied-integral-ordering (:kind descriptor)))
      (is (= [type type] (:parameter-types descriptor)))
      (is (= [:gravity.type/bool :normal :proof-required :proven-safe]
             ((juxt :result-type :application :numeric-mode :safety) descriptor)))
      (is (= ['gravity.math 'gravity.math.core] ((juxt :semantic-owner :layer0-owner) descriptor)))
      (is (= [:numeric-mode :solved] ((juxt :kind :status) row)))
      (is (= 7 (count (:call input)))) (is (= 12 (count (:edge input))))))
  (doseq [types [[:gravity.type/never :gravity.type/i64] [:gravity.type/u8 :gravity.type/never] [:gravity.type/never :gravity.type/never]]]
    (let [descriptor (integral-ordering-descriptor (integral-ordering-input '< types))]
      (is (= [:gravity.type/never :not-invoked] ((juxt :result-type :application) descriptor)))
      (is (= (when-not (every? #{:gravity.type/never} types) (vec (repeat 2 (first (remove #{:gravity.type/never} types))))) (:parameter-types descriptor)))))
  (doseq [types [[] [:gravity.type/i64] [:gravity.type/i64 :gravity.type/i64 :gravity.type/i64]
                 [:gravity.type/i64 :gravity.type/integer] [:gravity.type/i8 :gravity.type/u8]
                 [:gravity.type/f32 :gravity.type/f32] [:gravity.type/decimal :gravity.type/decimal]
                 [:gravity.type/string :gravity.type/string] [:gravity.type/unknown :gravity.type/never]
                 [:gravity.type/never :gravity.type/string] [nil :gravity.type/i64]]]
    (is (nil? (integral-ordering-descriptor (integral-ordering-input '< types))) (pr-str types)))
  (let [input (integral-ordering-input '< [:gravity.type/i64 :gravity.type/i64])
        context {:ordering-core (:core input) :lookup-nodes (:nodes input) :lookup-bindings (vec (vals (:bindings input)))}
        inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                   [(get-in input [:core :nodes]) (:nodes input) [] [] [(:call input)] [(:edge input)] {} (:seeds input) 10 context])]
    (is (= :converged (:convergence-status inferred)))
    (is (= [] (:diagnostics inferred)))
    (is (= :gravity.type/bool (get-in inferred [:type-table :call])))
    (is (= (integral-ordering-descriptor input) (get-in inferred [:type-table :operator])))
    (is (= (:seeds input) (:binding-types inferred)))
    (is (= 1 (count (:constraints inferred))))
    (doseq [[types reason] [[[:gravity.type/i64] :integral-ordering-arity-mismatch]
                            [[:gravity.type/i64 :gravity.type/u64] :integral-ordering-operand-type-mismatch]
                            [[:gravity.type/unknown :gravity.type/never] :integral-ordering-operand-type-mismatch]
                            [[:gravity.type/string :gravity.type/string] :integral-ordering-operand-type-mismatch]]]
      (let [data (integral-ordering-input '< types)
            context {:ordering-core (:core data) :lookup-nodes (:nodes data) :lookup-bindings (vec (vals (:bindings data)))}
            pass (invoke-c7 'sh08-ft-infer-pass-with-context
                   [(get-in data [:core :nodes]) [] [] [(:call data)] [(:edge data)] {} (:seeds data) context])]
        (is (= ["C7-TYPE-MISMATCH" reason] ((juxt :rule :reason) (first (:diagnostics pass)))))))
    (doseq [mutation [#(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                      #(update-in % [:bindings :ordering-binding] assoc :binding-class :namespace :kind :function :namespace 'example.ordering)
                      #(assoc-in % [:bindings :ordering-binding :kind] :function)
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:bindings :ordering-binding :namespace] 'gravity.math)
                      #(assoc-in % [:bindings :ordering-binding :name] '+)
                      #(assoc-in % [:bindings :ordering-binding :target-set] [:other])
                      #(assoc-in % [:edge :classification] :local-function)
                      #(assoc-in % [:edge :callee-function-syntax-id] :fake)
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :extra] true)
                      #(assoc-in % [:node :evaluation :order] [])]]
      (is (nil? (integral-ordering-descriptor (mutation input)))))
    (is (= :gravity.type/unknown
           (invoke-c7 'sh08-ft-infer-node-nominal [(assoc-in (get-in input [:nodes :operator]) [:node-id] :escaped)
                                                 {} {} {} [] context])))))

(deftest sh08-boolean-match-development-kernel
  (let [{:keys [core products forms bindings remainder match-id scrutinee-id bodies]} (boolean-match-input)
        joined (invoke-c7 'sh08-bm-products [core products forms])
        groups (:boolean-matches joined)
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) (:node-table products) [] [] [] [] {} bindings 20 {:boolean-matches groups}])
        checked (invoke-c7 'sh08-bm-checks [groups (:type-table inference) {} {:profile :meta :target :jvm}])
        group (first groups)
        types {scrutinee-id :gravity.type/bool (bodies 0) :gravity.type/bool (bodies 1) :gravity.type/bool}]
    (is (= :accepted (:status joined)) (pr-str joined))
    (is (= :converged (:convergence-status inference)))
    (is (= :gravity.type/bool (get (:binding-types inference) remainder)))
    (is (= :gravity.type/bool (get (:type-table inference) match-id)))
    (is (= [] (:diagnostics checked)))
    (is (every? #(= :equality (:kind %)) (:constraints checked)))
    (is (= [[true false] [false]] (mapv :remaining-before (:constraints checked))))
    (is (= [[true] [false]] (mapv :matched-values (:constraints checked))))
    (is (= [[false] []] (mapv :remaining-after (:constraints checked))))
    (is (= :gravity.type/bool (:binding-type (second (:constraints checked)))))
    (is (= (get-in core [:match-branch-records 1 :pattern-binding-scope-id])
           (:pattern-binding-scope-id (second (:constraints checked)))))
    (doseq [[label altered] [[:scrutinee (assoc-in core [:match-branch-records 0 :scrutinee-core-node-id] match-id)]
                             [:order (update core :match-branch-records #(vec (reverse %)))]
                             [:scope (assoc-in core [:match-pattern-records 1 :pattern-binding-scope-id] nil)]
                             [:parent (assoc-in core [:match-pattern-records 1 :parent-ordinal] 0)]
                             [:child (assoc-in core [:nodes 3 :children] [scrutinee-id (bodies 1) (bodies 0)])]
                             [:deleted (update core :match-branch-records pop)]
                             [:extra (update core :match-pattern-records conj (first (:match-pattern-records core)))]]]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [altered products forms]))) (name label)))
    (doseq [[rule reason altered altered-types profile]
            [["L7-PATTERN-TYPE" :boolean-pattern-type-mismatch (assoc-in group [:branches 0 :pattern-value] 1) types :meta]
             ["L7-UNREACHABLE" :unreachable-boolean-match-clause
              (-> group (assoc-in [:branches 1 :pattern-kind] :literal) (assoc-in [:branches 1 :pattern-value] true)) types :meta]
             ["C7-ANNOTATION" :unsupported-heterogeneous-boolean-match-join group (assoc types (bodies 1) :gravity.type/string) :meta]
             ["C7-ANNOTATION" :unsupported-boolean-match-scrutinee group (assoc types scrutinee-id :gravity.type/unknown) :meta]
             ["L7-NONEXHAUSTIVE" :nonexhaustive-boolean-match (update group :branches pop) types :formal]
             ["C7-ANNOTATION" :unsupported-partial-boolean-match (update group :branches pop) types :meta]]]
      (let [diagnostic (:diagnostic (invoke-c7 'sh08-bm-check [altered altered-types {} {:profile profile :target :jvm}]))]
        (is (= [rule reason] ((juxt :rule :reason) diagnostic)))))
    (is (= :gravity.type/unknown (invoke-c7 'sh08-bm-join [[] types])))
    (doseq [kind [:wildcard :binding]]
      (let [forged (-> core
                       (assoc-in [:match-branch-records 0 :pattern-kind] kind)
                       (assoc-in [:match-pattern-records 0 :pattern-kind] kind)
                       (assoc-in [:nodes 3 :attributes :branch-clauses 0 :pattern-kind] kind))]
        (is (= :rejected (:status (invoke-c7 'sh08-bm-products [forged products forms]))))))
    (is (= '_ (invoke-c7 'sh08-bm-pattern-value [{:kind :list :value '(quote _)}])))
    (is (true? (invoke-c7 'sh08-bm-source-kind-valid?
                          [{:pattern-kind :literal} {:kind :list :value '(quote _)}])))
    (is (false? (invoke-c7 'sh08-bm-source-kind-valid?
                           [{:pattern-kind :wildcard} {:kind :list :value '(quote _)}])))
    (is (= :rejected (:status (invoke-c7 'sh08-bm-products
                               [(assoc-in core [:match-pattern-records 1 :ordinal] 7) products forms]))))
    (let [outside {:node-id "outside" :core-form :reference :children []
                   :attributes {:binding-id remainder} :source {:syntax-id "outside"}}
          altered (update core :nodes conj outside)
          updated-products (assoc-in products [:node-table "outside"] outside)]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products [altered updated-products forms])))))
    (let [pattern (assoc (first (:patterns group)) :pattern-kind :map)
          unsupported (assoc group :unsupported-pattern pattern)]
      (is (= "L7-PATTERN-TYPE" (get-in (invoke-c7 'sh08-bm-check [unsupported types {} {:profile :meta}]) [:diagnostic :rule])))
      (is (= "C7-ANNOTATION" (get-in (invoke-c7 'sh08-bm-check [unsupported (dissoc types scrutinee-id) {} {:profile :meta}]) [:diagnostic :rule]))))
    (is (= {} (invoke-c7 'sh08-bm-seed-bindings
                              [groups (:node-table products) {} {} {} [] {}])))
    (doseq [family [:match-branch-records :match-pattern-records :match-decision-skeletons]
            value [0 nil {}]]
      (is (= :rejected (:status (invoke-c7 'sh08-bm-products
                                           [(assoc core family value) products forms])))))
    (let [owner "declared-owner"
          owned (assoc-in group [:node :evaluation :owner-function-syntax-id] owner)
          functions {owner {:body-core-node-id match-id :signature {:return {:type-id :gravity.type/i64}}}}
          diagnostic (:diagnostic (invoke-c7 'sh08-bm-check [owned types functions {:profile :meta}]))]
      (is (= ["C7-TYPE-MISMATCH" :gravity.type/i64 :gravity.type/bool]
             ((juxt :rule :expected :actual) diagnostic)))
      (is (= (first bodies) (get-in diagnostic [:node :node-id]))))
    (is (false? (invoke-c7 'sh08-ft-match-preimage-valid? [core (assoc core :match-pattern-records []) nil])))))

(defn- list-if-join-input
  ([left right] (list-if-join-input left right 0))
  ([left right ordinal]
  ;; Normalized source/identity DATA for independent consumer reconstruction.
  (let [sha #(str "sha256:" (format "%064x" (+ 90000 (* ordinal 1000) %)))
        function-name (if (zero? ordinal) 'optional-part (symbol (str "optional-part" ordinal)))
        roles [:if :condition :then :else :operator]
        nids (zipmap roles (map sha (range 1 6))) fids (zipmap roles (map sha (range 11 16)))
        owner (sha 80) definition (sha 81) refs (zipmap [:condition :then :else] (map sha [82 83 84]))
        names {:condition 'include :then 'left :else 'right}
        form (fn [role] {:form-id (fids role) :syntax-id (fids role) :kind (if (= role :if) :list :symbol)
                         :value (if (= role :if) '(if include left right) (if (= role :operator) 'if (names role)))
                         :parent-form-id (when-not (= role :if) (fids :if))
                         :child-form-ids (if (= role :if) (mapv fids [:operator :condition :then :else]) [])
                         :source-span nil :origin-chain [] :generated-origin []})
        forms (into {} (map #(vector (fids %) (form %)) roles))
        source (fn [role] {:form-id (fids role) :syntax-id (fids role) :semantic-span nil :origin-chain [] :generated-origin []})
        children (mapv nids [:condition :then :else])
        node {:node-id (nids :if) :core-form :if :children children :source (source :if) :resolved-binding-ids []
              :attributes {:condition-child-index 0 :then-child-index 1 :else-child-index 2 :condition-evaluation :always-first
                           :branch-evaluation :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}}
              :evaluation {:kind :condition-then-exclusive-branch :owner-function-syntax-id owner
                           :order [{:index 0 :core-node-id (nids :condition)}]}}
        nodes (assoc (into {} (for [role [:condition :then :else]]
                                [(nids role) {:node-id (nids role) :core-form :reference :children [] :source (source role)
                                              :resolved-binding-ids [(refs role)] :attributes {:binding-id (refs role)}
                                              :evaluation {:kind :left-to-right :owner-function-syntax-id owner :order []}}]))
                     (nids :if) node)
        record {:definition-name function-name :function-syntax-id owner :definition-binding-id definition}
        bindings (assoc (into {} (for [[role id] refs] [id {:binding-id id :name (names role) :kind :local :binding-class :lexical}]))
                        definition {:binding-id definition :name function-name :kind :function :binding-class :namespace :namespace 'example.list-join})
        control {:core-node-id (nids :if) :kind :if :condition-node-id (nids :condition)
                 :branches [{:role :then :predicate :truthy :core-node-id (nids :then)}
                            {:role :else :predicate :falsey :core-node-id (nids :else)}]
                 :branch-exclusivity :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}
                 :result-policy :selected-branch}
        core {:module {:namespace 'example.list-join :profile :meta :target :jvm}
              :nodes (mapv nodes children) :function-records [record] :control-flow [control]}
        types {(nids :condition) :gravity.type/bool (nids :then) left (nids :else) right}]
    {:core (update core :nodes conj node) :node node :nodes nodes :forms forms :types types :bindings bindings
     :nids nids :fids fids :owner owner :definition definition :refs refs})))

(deftest sh08-list-if-join-source-development-kernel
  (let [s :gravity.type/string finite #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}
        {:keys [core node nodes forms types bindings nids fids definition refs]} (list-if-join-input (finite [s]) (finite []))
        derive #(invoke-c7 'sh08-ij-derive [%1 %2 %3 %4 %5 %6 %7])
        result (derive core node nodes types bindings forms 65536) premise (:premise result)
        row (invoke-c7 'sh08-ij-row [premise])]
    (is (= :accepted (:status result)) (pr-str result))
    (is (= homogeneous (:type result) (:result-type premise)))
    (is (= [(finite [s]) (finite [])] (:branch-types premise)))
    (is (= :none (:expected-return-premise premise) (:runtime-conversion premise)))
    (is (= :join-compatibility (:kind row)))
    (is (= premise (:dependencies row) (get-in row [:constraint-id-request :join-compatibility])))
    (is (= result (derive core node nodes types bindings forms (:work-count result))))
    (is (= :list-if-join-work-limit (:reason (derive core node nodes types bindings forms (dec (:work-count result))))))
    (doseq [[changed-core changed-node changed-nodes changed-types changed-bindings changed-forms]
            [[(assoc core :control-flow []) node nodes types bindings forms]
             [(update core :control-flow conj (first (:control-flow core))) node nodes types bindings forms]
             [core (assoc-in node [:attributes :branch-evaluation] :both) nodes types bindings forms]
             [core (assoc-in node [:evaluation :order] []) nodes types bindings forms]
             [core (assoc node :children (mapv nids [:condition :else :then])) nodes types bindings forms]
             [core node (assoc-in nodes [(nids :then) :source :syntax-id] :forged) types bindings forms]
             [core node (assoc-in nodes [(nids :then) :evaluation :owner-function-syntax-id] :other) types bindings forms]
             [core node (assoc-in nodes [(nids :then) :resolved-binding-ids] [(refs :then) :missing]) types bindings forms]
             [core node nodes types (assoc-in bindings [definition :name] 'other) forms]
             [core node nodes types (assoc-in bindings [definition :kind] :var) forms]
             [core node nodes types (assoc-in bindings [definition :binding-class] :core) forms]
             [core node nodes types (assoc-in bindings [definition :namespace] 'other.module) forms]
             [core node nodes types (assoc-in bindings [(refs :then) :binding-id] :forged) forms]
             [core node nodes types bindings (assoc-in forms [(fids :operator) :value] 'other/if)]
             [core node nodes types bindings (assoc-in forms [(fids :then) :parent-form-id] :sibling)]
             [core node nodes types bindings (assoc-in forms [(fids :if) :child-form-ids] [])]]]
      (let [changed (derive changed-core changed-node changed-nodes changed-types changed-bindings changed-forms 65536)]
        (is (= :rejected (:status changed)))
        (is (= :list-if-join-source-mismatch (:reason changed)))))
    (doseq [changed [(assoc types (nids :condition) :gravity.type/never)
                     (assoc types (nids :condition) :gravity.type/unknown)
                     (assoc types (nids :then) (finite []))
                     (assoc types (nids :then) :gravity.type/never)
                     (assoc types (nids :else) :gravity.type/never)]]
      (is (= :not-applicable (:status (derive core node nodes changed bindings forms 65536)))))
    (is (= homogeneous (:type (derive (assoc-in core [:function-records 0 :signature :return :type-id]
                                               {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword})
                                     node nodes types bindings forms 65536))))))

(deftest sh08-list-if-join-inference-development-kernel
  (let [s :gravity.type/string finite #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type s}]
    (doseq [[left right] [[(finite [s]) (finite [])] [(finite []) (finite [s])]
                         [homogeneous (finite [])] [(finite [s s]) homogeneous]]]
      (let [{:keys [core node nodes forms types bindings nids refs]} (list-if-join-input left right)
            context {:ordering-core core :lookup-core core :lookup-nodes nodes
                     :lookup-bindings (vec (vals bindings)) :lookup-forms (vec (vals forms))}
            binding-types (into {} (for [[role id] refs] [id (get types (nids role))]))
            run #(invoke-c7 'sh08-ft-infer-pass-with-context
                            [(:nodes core) [] [] [] [] {} binding-types (assoc context :loop-work-left %)])
            result (run 65536) work (:loop-work-count result)
            row (first (:constraints result))]
        (is (= homogeneous (get-in result [:type-table (nids :if)])))
        (is (= [] (:diagnostics result)) (pr-str (:diagnostics result)))
        (is (= 1 (count (:constraints result))))
        (is (= :join-compatibility (:kind row)))
        (is (= [left right] (get-in row [:rhs :actual-branch-types])))
        (is (= homogeneous (get-in row [:solution :result-type])))
        (is (= homogeneous (invoke-c7 'sh08-ft-normal-return-type [:if [:gravity.type/bool left right] :gravity.type/unknown])))
        (is (= homogeneous (invoke-c7 'sh08-ft-infer-node-with-context [node types binding-types {} [] {}])))
        ;; The scheduling context retains the caller's remaining budget.
        (is (= (dissoc result :shape-call-context) (dissoc (run work) :shape-call-context)))
        (is (some #(= :list-if-join-work-limit (:reason %)) (:diagnostics (run (dec work)))))
        (let [unknown-types (dissoc types (nids :condition))
              step #(invoke-c7 'sh08-ij-step [node unknown-types context %])]
          (is (= {:status :deferred :work-count 1} (step 1)))
          (doseq [budget [0 -1]]
            (is (= {:status :rejected :reason :list-if-join-work-limit :work-count 1} (step budget))))
          (doseq [[left right expected] [[:gravity.type/never :gravity.type/bool :gravity.type/bool]
                                        [:gravity.type/bool :gravity.type/never :gravity.type/bool]
                                        [:gravity.type/bool :gravity.type/bool :gravity.type/bool]
                                        [:gravity.type/never :gravity.type/never :gravity.type/never]]]
            (let [types (assoc unknown-types (nids :then) left (nids :else) right)]
              (is (= {:status :deferred :type expected :work-count 1}
                     (invoke-c7 'sh08-ij-step [node types context 1]))))))
        (let [later (assoc node :node-id :deferred-if :children [:missing-condition (nids :then) (nids :else)])
              result (invoke-c7 'sh08-ft-infer-pass-with-context
                               [(conj (:nodes core) later) [] [] [] [] {} binding-types (assoc context :loop-work-left work)])]
          (is (= (inc work) (:loop-work-count result)))
          (is (some #(and (= :list-if-join-work-limit (:reason %)) (= :deferred-if (get-in % [:node :node-id])))
                    (:diagnostics result))))
        (let [functions {:owner {:function-id :owner :body-core-node-id (nids :if) :return :gravity.type/unknown}}
              updated (invoke-c7 'sh08-ft-update-function-returns [functions nodes (:type-table result)])]
          (is (= homogeneous (get-in updated [:owner :return])))
          (is (nil? (get-in updated [:owner :return-conflict]))))
        (doseq [condition [:gravity.type/string :gravity.type/keyword :gravity.type/nil]]
          (let [changed (invoke-c7 'sh08-ft-infer-pass-with-context
                                  [(:nodes core) [] [] [] [] {} (assoc binding-types (refs :condition) condition) context])]
            (is (= [] (:diagnostics changed)))
            (is (= homogeneous (get-in changed [:type-table (nids :if)])))
            (is (= condition (get-in changed [:constraints 0 :dependencies :condition-type])))
            (is (= homogeneous (invoke-c7 'sh08-ft-normal-return-type [:if [condition left right] :gravity.type/unknown])))))
        (let [changed (invoke-c7 'sh08-ft-infer-pass-with-context
                                [(:nodes core) [] [] [] [] {} (assoc binding-types (refs :condition) :gravity.type/never) context])]
          (is (= :gravity.type/never (get-in changed [:type-table (nids :if)])))
          (is (= [] (:constraints changed)))
          (is (= :gravity.type/never (invoke-c7 'sh08-ft-normal-return-type [:if [:gravity.type/never left right] homogeneous]))))))))

(deftest sh08-list-if-join-type-development-kernel
  (let [finite #(hash-map :kind :gravity.type/list :shape :literal :element-types %)
        homogeneous #(hash-map :kind :gravity.type/list :shape :homogeneous :element-type %)
        atomics [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                 :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64
                 :gravity.type/f32 :gravity.type/f64 :gravity.type/bool :gravity.type/string
                 :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio]
        join #(invoke-c7 'sh08-ij-type-work [%1 %2 %3])
        normal #(invoke-c7 'sh08-ij-normal-type [%1 %2])]
    (doseq [element atomics
            [left right] [[(finite [element]) (finite [])] [(finite []) (finite [element])]
                          [(finite [element]) (finite [element element])]
                          [(homogeneous element) (finite [])] [(finite []) (homogeneous element)]
                          [(homogeneous element) (finite [element element])]
                          [(finite [element]) (homogeneous element)]]]
      (let [result (join left right 65536)]
        (is (= :accepted (:status result)))
        (is (= (homogeneous element) (:type result) (normal left right)))
        (is (= [left right] (:branch-types result)))
        (is (= element (:element-type result)))
        (is (= 2 (count (:branch-compatibility result))))
        (is (= result (join left right (:work-count result))))
        (is (= :list-if-join-work-limit (:reason (join left right (dec (:work-count result))))))))
    (let [s :gravity.type/string left (finite [s]) same (finite []) hom (homogeneous s)]
      (doseq [type [left same hom :gravity.type/string :gravity.type/never]]
        (is (= type (normal type type)))
        (is (= type (normal :gravity.type/never type) (normal type :gravity.type/never))))
      (is (= :not-applicable (:status (join same same 65536))))
      (doseq [unsupported [:gravity.type/list :gravity.type/unknown nil :gravity.type/string
                           {:kind :gravity.type/vector :shape :literal :element-types [s]}
                           (finite [:gravity.type/keyword]) (finite [s :gravity.type/keyword])
                           (finite [(homogeneous s)]) (finite [:gravity.type/unknown])
                           (finite [:gravity.type/never]) (finite [nil])
                           (homogeneous :gravity.type/keyword)
                           (homogeneous (homogeneous s)) (assoc hom :extra :forged)
                           (assoc left :element-types '(s))
                           (finite (vec (repeat 1025 s)))]]
        (doseq [[a b] [[left unsupported] [unsupported left]]]
          (is (= :not-applicable (:status (join a b 65536))))
          (is (= :gravity.type/unknown (normal a b)))))
      (let [large (finite (vec (repeat 1024 s))) result (join large same 65536)]
        (is (= :accepted (:status result)))
        (is (= 1024 (get-in result [:branch-compatibility 0 :checked-member-count])))
        (is (= result (join large same (:work-count result))))
        (is (= :list-if-join-work-limit (:reason (join large same (dec (:work-count result)))))))
      (is (= :gravity.type/unknown
             (invoke-c7 'sh08-ft-normal-return-type [:alternatives [left same] :gravity.type/unknown])))
      (is (= :gravity.type/unknown
             (invoke-c7 'sh08-ft-normal-return-type [:try [left same] :gravity.type/unknown])))
      (is (= :gravity.type/unknown (invoke-c7 'sh08-ft-loop-kernel-normal-join [[left same]]))))))

(deftest sh08-homogeneous-list-return-branch-independence-development-kernel
  (let [{:keys [core products forms match-id scrutinee-id bodies]} (boolean-match-input)
        group (first (:boolean-matches (invoke-c7 'sh08-bm-products [core products forms])))
        owner :return-owner group (assoc-in group [:node :evaluation :owner-function-syntax-id] owner)
        finite {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}
        different (assoc finite :element-types [])
        functions {owner {:body-core-node-id match-id :signature {:return {:rule-version :sh07-list-return-declaration-v1
                                                                          :type-id {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}}}}}
        types {scrutinee-id :gravity.type/bool (first bodies) finite (second bodies) finite}
        checked (invoke-c7 'sh08-bm-check [group types functions {:profile :meta}])]
    (is (nil? (:diagnostic checked)) (pr-str checked))
    (is (= finite (:type checked)))
    (is (= :unsupported-heterogeneous-boolean-match-join
           (get-in (invoke-c7 'sh08-bm-check [group (assoc types (second bodies) different) functions {:profile :meta}]) [:diagnostic :reason])))
    (let [unknown (invoke-c7 'sh08-bm-check [group (assoc types (second bodies) :gravity.type/unknown) functions {:profile :meta}])]
      (is (nil? (:type unknown)))
      (is (= ["C7-ANNOTATION" :unknown-boolean-match-branch-type :gravity.type/unknown]
             ((juxt :rule :reason :actual) (:diagnostic unknown))))))
  (let [owner :structural-return body :structural-body
        functions {owner {:body-core-node-id body :signature {:return {:rule-version :sh07-list-return-declaration-v1
                                                                      :type-id {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}}}}}
        group {:node {:node-id body :evaluation {:owner-function-syntax-id owner}} :nodes {}}]
    (is (nil? (invoke-c7 'sh08-sm-expected-return [group functions])))
    (is (= :gravity.type/string
           (invoke-c7 'sh08-sm-expected-return [group (assoc-in functions [owner :signature :return] {:type-id :gravity.type/string})])))))

(deftest sh08-homogeneous-list-return-structural-independence-development-kernel
  (let [{:keys [core products forms nodes lexical binding-types match-id]} (structural-match-products-input)
        groups (:boolean-matches (invoke-c7 'sh08-bm-products [core products (vec (vals forms))]))
        inference (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [(:nodes core) nodes lexical [] [] [] {} binding-types 50 {:boolean-matches groups}])
        group (first (invoke-c7 'sh08-sm-groups-with-shapes [groups (:binding-shapes inference)]))
        owner :structural-return group (assoc-in group [:node :evaluation :owner-function-syntax-id] owner)
        bodies (mapv :branch-core-node-id (:branches group))
        finite {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}
        types (reduce #(assoc %1 %2 finite) (:type-table inference) bodies)
        functions {owner {:body-core-node-id match-id :signature {:return {:rule-version :sh07-list-return-declaration-v1
                                                                          :type-id {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}}}}}
        check #(invoke-c7 'sh08-sm-check [group % functions {:profile :meta}]) result (check types)]
    (is (= :converged (:convergence-status inference)))
    (is (nil? (:diagnostic result)) (pr-str result))
    (is (= finite (:type result)))
    (is (= :unsupported-heterogeneous-structural-match-join
           (get-in (check (assoc types (last bodies) (assoc finite :element-types []))) [:diagnostic :reason])))
    (is (= ["C7-ANNOTATION" :unknown-structural-match-branch-type :gravity.type/unknown]
           ((juxt :rule :reason :actual) (:diagnostic (check (assoc types (last bodies) :gravity.type/unknown))))))
    (is (= ["C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch :gravity.type/string]
           ((juxt :rule :reason :expected)
            (:diagnostic (invoke-c7 'sh08-sm-check
                                   [group types (assoc-in functions [owner :signature :return] {:type-id :gravity.type/string}) {:profile :meta}])))))))

(deftest sh08-boolean-match-c8-ledger-composition
  ;; Normalized identity/verifier oracle for the final equality representation;
  ;; no new authenticated-source evidence is manufactured here.
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [upstream 'gravity.self-hosting.sh09-c7-effect-adapter-test
        invoke-c8 (var-get (ns-resolve upstream 'invoke-c8))
        fixture (var-get (ns-resolve upstream 'function-typed-result))
        verify (var-get (ns-resolve upstream 'function-verification))
        {:keys [core products forms scrutinee-id bodies]} (boolean-match-input)
        groups (:boolean-matches (invoke-c7 'sh08-bm-products [core products forms]))
        types {scrutinee-id :gravity.type/bool (bodies 0) :gravity.type/bool (bodies 1) :gravity.type/bool}
        constraints (:constraints (invoke-c7 'sh08-bm-checks [groups types {} {:profile :meta :target :jvm}]))
        raw (assoc (fixture "/development/boolean-match.gravity") :constraint-ledger constraints)
        identity (invoke-c8 'sh09-adapter-function-identity-input [raw])
        typed (assoc raw :identity-input identity :artifact-id-request identity
                     :typed-core (invoke-c8 'sh09-adapter-function-typed-core [raw]))
        verification (verify typed)
        result (invoke-c8 'sh09-build-authenticated-pure-effect-result [typed verification])]
    (is (= [:equality :equality] (mapv :kind constraints)))
    (is (= :accepted (:status result)))
    (is (= constraints (get-in result [:typed-core :constraints])))
    (is (= identity (get-in result [:identity-input :sh08-identity-input])))
    (is (not= identity (invoke-c8 'sh09-adapter-function-identity-input
                                  [(update raw :constraint-ledger #(mapv (fn [c] (assoc c :kind :boolean-match-branch)) %))])))
    (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification result]))))
    (doseq [candidate [(assoc-in result [:typed-core :constraints] [])
                       (assoc-in result [:identity-input :sh08-identity-input :constraint-ledger 0 :remaining-after] [true false])]]
      (is (= :rejected (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result [typed verification candidate])))))))

(deftest sh08-boolean-match-authenticated-source-composition
  (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
  (let [invoke-c8 (var-get (ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8))
        artifacts (mapv #(fixture-artifact "accepted" "function-boolean-matches" %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (when (= :accepted (:status result))
        (let [constraints (filterv #(= :sh08-boolean-match-v1 (:producer-rule %)) (:constraint-ledger result))
              functions (into {} (map (juxt :definition-name identity) (:function-type-table result)))
              verification (verification-result artifact result)
              effected (invoke-c8 'sh09-build-authenticated-pure-effect-result [result verification])
              narrowed (first (filter :pattern-binding-id constraints))]
          (is (= 5 (count constraints)))
          (is (= :gravity.type/i64 (get-in functions ['select-value :return])))
          (is (= :gravity.type/bool (get-in functions ['retain-bool :return])))
          (is (= [false] (:matched-values narrowed)))
          (is (= :gravity.type/bool (:binding-type narrowed)))
          (is (= (get-in narrowed [:constraint-id-request :pattern :pattern-binding-scope-id])
                 (:pattern-binding-scope-id narrowed)))
          (is (every? #(= :solved (:status %)) constraints))
          (is (= :passed (:status verification)))
          (is (= :accepted (:status effected)) (pr-str (:diagnostics effected)))
          (is (= (:typed-core result) (:typed-core effected)))
          (is (= (:constraint-ledger result) (get-in effected [:typed-core :constraints])))
          (is (= :passed (:status (invoke-c8 'sh09-verify-authenticated-pure-effect-result
                                            [result verification effected]))))
          (let [altered (update result :constraint-ledger
                                #(mapv (fn [c] (if (= :sh08-boolean-match-v1 (:producer-rule c))
                                                (assoc c :matched-values []) c)) %))]
            (is (= :rejected (:status (verification-result artifact altered))))
            (is (= :rejected (:status (invoke-c8 'sh09-build-authenticated-pure-effect-result [altered verification]))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" "function-boolean-matches" ".gravity"))
           (slurp (fixture-path "accepted" "function-boolean-matches" ".qst"))))))

(defn- assert-boolean-match-source-rejections [cases]
  (doseq [[basename rule reason anchor ordinal] cases]
    (let [artifact (fixture-artifact "rejected" basename ".gravity")
          result (function-result artifact) diagnostic (first (:diagnostics result))
          core (canonical-core artifact)
          branch (get (:match-branch-records core) ordinal)
          expected-id (if (= anchor :branch) (:branch-core-node-id branch) (:core-node-id branch))
          source (if (= anchor :pattern)
                   (let [form (first (filter #(= (:form-id %) (:pattern-form-id branch))
                                             (get-in (function-request artifact) [:b47-context :authenticated-core-request :forms])))]
                     {:syntax-id (:syntax-id form) :semantic-span (:source-span form)
                      :origin-chain (:origin-chain form) :generated-origin (:generated-origin form)})
                   (:source (first (filter #(= expected-id (:node-id %)) (:nodes core)))))]
      (is (= :accepted (:status artifact)) basename)
      (is (= :rejected (:status result)) basename)
      (is (= [rule reason] ((juxt :rule :reason) diagnostic)) (pr-str diagnostic))
      (is (= :type-checking (:stage diagnostic)))
      (is (string? (:syntax-id diagnostic)))
      (is (map? (:source-span diagnostic)))
      (is (vector? (:origin-chain diagnostic)))
      (is (= expected-id (:core-node-id diagnostic)))
      (is (= [(:syntax-id source) (:semantic-span source) (:origin-chain source) (:generated-origin source)]
             ((juxt :syntax-id :source-span :origin-chain :generated-origin) diagnostic)))
      (when (= anchor :branch)
        (is (= [:gravity.type/i64 :gravity.type/string]
               ((juxt :expected-type :actual-type) diagnostic))))
      (is (= (slurp (fixture-path "rejected" basename ".gravity"))
             (slurp (fixture-path "rejected" basename ".qst")))))))

(deftest sh08-boolean-match-source-rejections
  (assert-boolean-match-source-rejections
    [["function-boolean-match-result" "C7-TYPE-MISMATCH" :boolean-match-branch-type-mismatch :branch 1]
     ["function-boolean-match-pattern" "L7-PATTERN-TYPE" :boolean-pattern-type-mismatch :pattern 0]
     ["function-boolean-match-duplicate" "L7-UNREACHABLE" :unreachable-boolean-match-clause :pattern 1]
     ["function-boolean-match-subsumed" "L7-UNREACHABLE" :unreachable-boolean-match-clause :pattern 2]]))

(deftest sh08-boolean-match-source-boundary-rejections
  (assert-boolean-match-source-rejections
    [["function-boolean-match-partial" "C7-ANNOTATION" :unsupported-partial-boolean-match :match 0]
     ["function-boolean-match-unknown" "C7-ANNOTATION" :unsupported-boolean-match-scrutinee :match 0]])
  ;; C6 currently admits only :meta/:jvm input. Formal C7 nonexhaustiveness is
  ;; exercised by the direct kernel, not claimed through this upstream denial.
  (let [basename "function-boolean-match-formal-partial"
        diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil
                        (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= ["C6-VERIFY" :core-lowering :formal :request-shape]
           [(:rule diagnostic) (:stage diagnostic) (:profile diagnostic) (get-in diagnostic [:facts :reason])]))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst")))))
  (let [basename "function-boolean-match-scope-escape"
        diagnostic (try (fixture-artifact "rejected" basename ".gravity") nil
                        (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= "C5-UNRESOLVED" (:id diagnostic)))
    (is (= (slurp (fixture-path "rejected" basename ".gravity"))
           (slurp (fixture-path "rejected" basename ".qst"))))))

(deftest sh08-imported-inferred-signature-authenticated-source-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-c5 (bootstrap/sh06-resolution-file-artifact (fixture-path "modules" "inferred-values" extension))
                 library (bootstrap/sh07-core-from-resolution-artifact library-c5)
                 producer (function-request library)
                 source-path (fixture-path "modules" "inferred-consumer" extension)
                 inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library-c5]}
                 c5 (bootstrap/compiler-c5-resolution-file-artifact source-path inputs)
                 core (bootstrap/sh07-core-from-resolution-artifact c5)
                 request (assoc (function-request core) :module-type-inputs
                           {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [producer]})
                 result (invoke-c7 'sh08-function-type-core-artifact [request])]
             (is (= :accepted (:status library-c5) (:status library) (:status c5) (:status core)))
             (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
             (when (= :accepted (:status result))
               (let [imports (into {} (map #(vector (get-in % [:function-type :definition-name]) %) (:imported-function-types result)))
                     producer-types (get-in result [:module-type-producers 0 :identity-input :function-type-table])
                     functions (into {} (map (juxt :definition-name identity) producer-types))
                     verification (invoke-c7 'sh08-verify-function-type-result [request result])
                     changed (assoc-in result [:imported-function-types 0 :function-type :return] :gravity.type/forged)
                     altered-verification (invoke-c7 'sh08-verify-function-type-result [request changed])]
                 (is (= :direct-imported-functions-types-only (:scope result)))
                 (is (= 4 (count imports) (count (:call-type-facts result)) (count (:function-type-table result))))
                 (is (= :gravity.type/bool (get-in imports ['ready :function-type :return])))
                 (is (= :gravity.type/i64 (get-in imports ['identity-i64 :function-type :return])))
                 (is (= :gravity.type/string (get-in imports ['identity :function-type :return])
                        (get-in imports ['identity :function-type :parameters 0 :type])))
                 (is (= :gravity.type/integer (get-in imports ['integer-chain :function-type :return])
                        (get-in imports ['integer-chain :function-type :parameters 0 :type])))
                 (is (nil? (get-in functions ['ready :signature])))
                 (is (nil? (get-in functions ['identity-i64 :signature :return])))
                 (is (nil? (get-in functions ['identity :signature])))
                 (is (= #{:gravity.type/bool :gravity.type/i64 :gravity.type/string :gravity.type/integer}
                        (set (map :result-type (:call-type-facts result)))))
                 (is (= :passed (:status verification)))
                 (is (= :rejected (:status altered-verification)))
                 (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
                 (let [effects ((ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8)
                                'sh09-build-authenticated-pure-effect-result [result verification])]
                   (is (= :rejected (:status effects)))
                   (is (= :imported-body-effects-pending (get-in effects [:diagnostics 0 :reason]))))))
             {:request request :result result :source-path source-path :inputs inputs :producer producer}))
         [".gravity" ".qst"])
        {:keys [request result source-path inputs producer]} (first cases)]
    (is (= (get-in cases [0 :result :identity-input]) (get-in cases [1 :result :identity-input])))
    (let [records (get-in producer [:canonical-core-artifact :function-records])
          index (first (keep-indexed #(when (= 'identity (:definition-name %2)) %1) records))
          forged "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"]
      (is (some? index))
      (doseq [bad [(assoc-in producer [:b47-context :authenticated-core-request :lineage :authenticated-sh06-artifact-id] forged)
                   (assoc-in producer [:canonical-core-artifact :function-records index :function-syntax-id] forged)
                   (assoc-in producer [:canonical-core-artifact :function-records index :parameter-binding-ids 0] forged)
                   (assoc-in producer [:canonical-core-artifact :function-records index :body-core-node-id] forged)]]
        (let [rejected (invoke-c7 'sh08-function-type-core-artifact [(assoc-in request [:module-type-inputs :module-requests] [bad])])]
          (is (= "C7-VERIFY" (get-in rejected [:diagnostics 0 :rule]))))))
    (doseq [[source reason]
            [[(str/replace (slurp source-path) "identity \"consumer\"" "identity true") :call-argument-type-mismatch]
             [(str/replace (slurp source-path) "(producer/ready)" "(producer/ready true)") :call-arity-mismatch]
             [(str/replace (slurp source-path) "(defn call-ready []" "(defn call-ready [] :- String") :function-result-type-mismatch]]]
      (let [c5 (bootstrap/compiler-c5-resolution-source-artifact source-path source inputs)
            core (bootstrap/sh07-core-from-resolution-artifact c5)
            q (assoc (function-request core) :module-type-inputs (:module-type-inputs request))
            rejected (invoke-c7 'sh08-function-type-core-artifact [q]) diagnostic (first (:diagnostics rejected))]
        (is (= :accepted (:status core)))
        (is (= :rejected (:status rejected)))
        (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
        (is (= reason (:reason diagnostic)))
        (is (every? #(some? (get diagnostic %)) [:core-node-id :syntax-id :source-span :profile :target]))))
    (let [library-c5 (bootstrap/sh06-resolution-file-artifact (fixture-path "modules" "unconstrained-values" ".gravity"))
          library (bootstrap/sh07-core-from-resolution-artifact library-c5)
          text "(ns example.unconstrained-consumer (:profile :meta) (:target :jvm) (:requires [example.unconstrained-values :as producer]) (:exports [attempt])) (defn attempt [] (producer/identity true))"
          c5 (bootstrap/compiler-c5-resolution-source-artifact source-path text
               {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library-c5]})
          core (bootstrap/sh07-core-from-resolution-artifact c5)
          q (assoc (function-request core) :module-type-inputs
              {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [(function-request library)]})
          rejected (invoke-c7 'sh08-function-type-core-artifact [q]) diagnostic (first (:diagnostics rejected))]
      (is (= :accepted (:status library-c5) (:status library) (:status c5) (:status core)))
      (is (= :rejected (:status rejected)))
      (is (= "C7-ANNOTATION" (:rule diagnostic)))
      (is (= :invalid-module-type-producer (:reason diagnostic)))
      (is (every? #(some? (get diagnostic %)) [:core-node-id :syntax-id :source-span :profile :target])))))

(deftest sh08-imported-function-authenticated-source-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-c5 (bootstrap/sh06-resolution-file-artifact (fixture-path "modules" "type-values" extension))
                 library (bootstrap/sh07-core-from-resolution-artifact library-c5)
                 producer (function-request library)
                 source-path (fixture-path "modules" "type-consumer" extension)
                 inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library-c5]}
                 c5 (bootstrap/compiler-c5-resolution-file-artifact source-path inputs)
                 core (bootstrap/sh07-core-from-resolution-artifact c5)
                 request (assoc (function-request core) :module-type-inputs
                           {:artifact :gravity/sh08-module-type-inputs :schema-version 1 :module-requests [producer]})
                 result (invoke-c7 'sh08-function-type-core-artifact [request])]
             (is (= :accepted (:status library-c5) (:status library) (:status c5) (:status core)))
             (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
             (when (= :accepted (:status result))
               (is (= :direct-imported-functions-types-only (:scope result)))
               (is (= 3 (count (:imported-function-types result)) (count (:function-type-table result)) (count (:call-type-facts result))))
               (is (= 1 (count (:module-type-producers result))))
               (is (= #{:gravity.type/i64 :gravity.type/bool :gravity.type/string}
                      (set (map :return (:function-type-table result)))
                      (set (map :result-type (:call-type-facts result)))))
               (is (every? #(= :direct-imported-function (:dispatch %)) (:call-type-facts result)))
               (doseq [call (:calls (canonical-core core))]
                 (let [operator-type (get (:type-table result) (:operator-node-id call))]
                   (is (= :gravity/c7-function-type (:artifact operator-type)))
                   (is (= 1 (:fixed-arity operator-type)))))
               (is (every? #(= (:artifact-id library-c5) (:producer-sh06-artifact-id %)) (:imported-function-types result)))
               (is (every? #(= [:pending-sh09] (get-in % [:function-type :thrown-error-effects])) (:imported-function-types result)))
               (is (= :passed (:status (invoke-c7 'sh08-verify-function-type-result [request result]))))
               (is (= :rejected (:status (invoke-c7 'sh08-verify-function-type-result
                                         [request (assoc-in result [:imported-function-types 0 :function-type :return] :gravity.type/bool)]))))
               (require 'gravity.self-hosting.sh09-c7-effect-adapter-test)
               (let [rejected ((ns-resolve 'gravity.self-hosting.sh09-c7-effect-adapter-test 'invoke-c8)
                               'sh09-build-authenticated-pure-effect-result
                               [result (invoke-c7 'sh08-verify-function-type-result [request result])])
                     diagnostic (first (:diagnostics rejected))]
                 (is (= :rejected (:status rejected)))
                 (is (= :imported-body-effects-pending (:reason diagnostic)))
                 (is (every? #(some? (get diagnostic %)) [:core-node-id :syntax-id :source-span :profile :target :remediation]))))
             {:library-c5 library-c5 :producer producer :core core :request request :result result :source-path source-path :inputs inputs}))
         [".gravity" ".qst"])
        {:keys [request result core producer source-path inputs]} (first cases)]
    (is (= (get-in cases [0 :result :identity-input]) (get-in cases [1 :result :identity-input])) )
    (let [missing (invoke-c7 'sh08-function-type-core-artifact [(dissoc request :module-type-inputs)])
          diagnostic (first (:diagnostics missing))]
      (is (= "C7-ANNOTATION" (:rule diagnostic)))
      (is (= :missing-imported-function-typing (:reason diagnostic)))
      (is (every? #(some? (get diagnostic %)) [:core-node-id :syntax-id :source-span :profile :target :remediation])))
    (doseq [bad [nil 1 true "wrong" {}]]
      (let [rejected (invoke-c7 'sh08-function-type-core-artifact [(assoc-in request [:module-type-inputs :module-requests] bad)])]
        (is (= "C7-VERIFY" (get-in rejected [:diagnostics 0 :rule])))
        (is (= :malformed-module-type-inputs (get-in rejected [:diagnostics 0 :reason])))))
    (doseq [bad [(assoc producer :signature {:return :gravity.type/bool})
                 (assoc-in producer [:canonical-core-artifact :function-records 0 :signature :return :type-id] :gravity.type/bool)
                 (assoc-in producer [:b47-context :authenticated-core-request :lineage :authenticated-sh06-artifact-id] "sha256:stale")]]
      (let [rejected (invoke-c7 'sh08-function-type-core-artifact [(assoc-in request [:module-type-inputs :module-requests] [bad])])]
        (is (= "C7-VERIFY" (get-in rejected [:diagnostics 0 :rule])))))
    (doseq [[source expected-reason]
            [[(clojure.string/replace (slurp source-path) "(values/identity-i64 value)" "(values/identity-i64 true)") :call-argument-type-mismatch]
             [(clojure.string/replace (slurp source-path) "(values/identity-i64 value)" "(values/identity-i64)") :call-arity-mismatch]
             [(clojure.string/replace (slurp source-path) "[value :- I64] :- I64" "[value :- I64] :- String") :function-result-type-mismatch]]]
      (let [c5 (bootstrap/compiler-c5-resolution-source-artifact source-path source inputs)
            artifact (bootstrap/sh07-core-from-resolution-artifact c5)
            q (assoc (function-request artifact) :module-type-inputs (:module-type-inputs request))
            rejected (invoke-c7 'sh08-function-type-core-artifact [q])
            diagnostic (first (:diagnostics rejected))]
        (is (= :accepted (:status artifact)))
        (is (= :rejected (:status rejected)))
        (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
        (is (= expected-reason (:reason diagnostic)))
        (is (every? #(some? (get diagnostic %)) [:core-node-id :syntax-id :source-span :profile :target :remediation]))))))

(deftest sh08-atomic-signature-authenticated-source-composition
  (let [artifacts (mapv #(fixture-artifact "accepted" "function-atomic-signatures" %) [".gravity" ".qst"])
        results (mapv function-result artifacts)]
    (doseq [[artifact result] (map vector artifacts results)]
      (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
      (is (= :passed (:status (b47-verification artifact))))
      (when (= :accepted (:status result))
        (let [functions (into {} (map (juxt :definition-name identity) (:function-type-table result)))
              declarations (filter #(= :sh08-atomic-function-signature-v1 (:producer-rule %)) (:constraint-ledger result))
              boundary (:gravity-core-boundary artifact)
              input (:authenticated-core-request boundary)
              binding-index (into {} (map (juxt :binding-id identity) (:binding-table input)))
              signature (get-in functions ['identity-i64 :signature])
              type-binding (get binding-index (get-in signature [:parameters 0 :type-binding-id]))]
          (is (= 4 (count functions)))
          (doseq [name '[identity-i64 forward-i64 shadow-type]]
            (is (= :gravity.type/i64 (get-in functions [name :return])))
            (is (= :gravity.type/i64 (get-in functions [name :parameters 0 :type]))))
          (is (= :gravity.type/bool (get-in functions ['identity-bool :return])))
          (is (= :gravity/sh08-atomic-function-signature-typed-core-v1 (get-in result [:identity-input :domain])))
          (is (= 8 (count declarations)))
          (is (every? #(= :solved (:status %)) declarations))
          (is (= [:core :type 'gravity.core 'I64] ((juxt :binding-class :kind :namespace :name) type-binding)))
          (is (= :development-unadmitted (get-in boundary [:plan-binding :revision-status])))
          (is (false? (get-in boundary [:plan-binding :self-hosted?])))
          (is (= :passed (:status (verification-result artifact result))))
          (is (= :rejected (:status (verification-result artifact (update-in result [:function-type-table 0] dissoc :signature))))))))
    (is (= (:identity-input (first results)) (:identity-input (second results))))
    (is (= (slurp (fixture-path "accepted" "function-atomic-signatures" ".gravity"))
           (slurp (fixture-path "accepted" "function-atomic-signatures" ".qst"))))))

(deftest sh08-atomic-signature-source-type-mismatches
  (doseq [[basename reason actual]
          [["function-annotation-result-mismatch" :function-result-type-mismatch :gravity.type/string]
           ["function-annotation-argument-mismatch" :call-argument-type-mismatch :gravity.type/string]
           ["function-annotation-integer-coercion" :call-argument-type-mismatch :gravity.type/integer]]]
    (let [artifact (fixture-artifact "rejected" basename ".gravity")
          result (function-result artifact) diagnostic (first (:diagnostics result))]
      (is (= :accepted (:status artifact)))
      (is (= :rejected (:status result)))
      (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
      (is (= :type-checking (:stage diagnostic)))
      (is (= reason (:reason diagnostic)))
      (is (= :gravity.type/i64 (:expected-type diagnostic)))
      (is (= actual (:actual-type diagnostic)))
      (is (string? (:core-node-id diagnostic)))
      (is (string? (:syntax-id diagnostic)))
      (is (map? (:source-span diagnostic)))
      (is (seq (:remediation diagnostic)))
      (is (= (slurp (fixture-path "rejected" basename ".gravity"))
             (slurp (fixture-path "rejected" basename ".qst")))))))

(deftest sh08-function-request-cache-is-identity-scoped-and-fail-closed
  (let [artifact (with-meta {:artifact-id "same"} {:instance :first})
        equal-but-distinct
        (with-meta {:artifact-id "same"} {:instance :second})
        failed-artifact {:artifact-id "failed"}
        verification-calls (atom 0)
        coordinator-calls (atom 0)]
    (binding [*function-request-cache* (java.util.IdentityHashMap.)]
      (with-redefs
       [b47-verification
        (fn [candidate]
          (swap! verification-calls inc)
          {:status (if (identical? candidate failed-artifact)
                     :failed :passed)})
        coordinator-verification
        (fn [candidate report]
          (swap! coordinator-calls inc)
          {:candidate candidate :report report})
        b47-context
        (fn [candidate report coordinator]
          {:canonical-core-artifact {:candidate candidate}
           :report report
           :coordinator coordinator})]
        (let [first-request (function-request artifact)
              repeated-request (function-request artifact)
              distinct-request (function-request equal-but-distinct)
              first-failed (function-request failed-artifact)
              repeated-failed (function-request failed-artifact)]
          (is (identical? first-request repeated-request))
          (is (not (identical? first-request distinct-request)))
          (is (not (identical? first-failed repeated-failed)))
          (is (= 4 @verification-calls))
          (is (= 4 @coordinator-calls)))))))

(deftest sh08-function-fixtures-and-c7-exports-are-executable
  (is (= (slurp (fixture-path "accepted" "function-local-call" ".gravity"))
         (slurp (fixture-path "accepted" "function-local-call" ".qst"))))
  (is (= (slurp (fixture-path "rejected" "function-call-arity" ".gravity"))
         (slurp (fixture-path "rejected" "function-call-arity" ".qst"))))
  (is (= (slurp (fixture-path "accepted" "function-two-hop-call" ".gravity"))
         (slurp (fixture-path "accepted" "function-two-hop-call" ".qst"))))
  (is (= (slurp (fixture-path "accepted" "function-call-nonlocal" ".gravity"))
         (slurp (fixture-path "accepted" "function-call-nonlocal" ".qst"))))
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @c7-plan)))
  (is (= :meta (get-in @c7-plan [:module :profile])))
  (is (= :jvm (get-in @c7-plan [:module :target])))
  (doseq [function
          '[sh08-function-type-boundary-policy
            sh08-function-type-core-artifact
            sh08-verify-function-type-result]]
    (is (map? (get-in @c7-plan [:functions function])) function)))

(deftest sh08-function-boundary-policy-and-malformed-request-fail-closed
  (let [policy (invoke-c7 'sh08-function-type-boundary-policy [])
        result (invoke-c7 'sh08-function-type-core-artifact [{}])]
    (is (= :host-validated-b47-report-and-opaque-provenance-digest
           (:verification-boundary policy)))
    (is (false?
         (:independent-gravity-cryptographic-verification? policy)))
    (is (true? (:host-digest-authority-retained? policy)))
    (is (= :rejected (:status result)))
    (is (= :authenticated-b47-request-required
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-b47-semantic-identity-normalization-is-exact
  (let [projection-id (sha-id "a")
        binding-id (sha-id "b")
        upstream-id (sha-id "c")
        artifact-id (sha-id "d")
        raw-binding
        {:binding-id binding-id
         :upstream-binding-id upstream-id
         :definition-artifact-id artifact-id}
        raw-lineage
        {:sh06-semantic-projection-id projection-id
         :authenticated-sh06-artifact-id artifact-id}
        raw-lineage-without-authenticated-id
        {:sh06-semantic-projection-id projection-id}
        raw-node
        {:core-form :var
         :attributes
         {:binding-id binding-id
          :upstream-binding-id upstream-id
          :definition-artifact-id artifact-id
          :authenticated-sh06-artifact-id artifact-id}}]
    (is (= (assoc raw-binding
                  :upstream-binding-id binding-id
                  :definition-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-binding
                      [projection-id raw-binding])))
    (is (= (assoc raw-lineage
                  :authenticated-sh06-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id raw-lineage])))
    (is (= (assoc raw-lineage-without-authenticated-id
                  :authenticated-sh06-artifact-id projection-id)
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id
                       raw-lineage-without-authenticated-id])))
    (is (= (-> raw-node
               (assoc-in [:attributes :upstream-binding-id] binding-id)
               (assoc-in [:attributes :definition-artifact-id]
                         projection-id)
               (assoc-in [:attributes :authenticated-sh06-artifact-id]
                         projection-id))
           (invoke-c7 'sh08-ft-semantic-node
                      [projection-id raw-node])))))

(deftest sh08-b47-accepted-identity-field-diagnostic
  (doseq [[extension artifact]
          [[".gravity" @accepted-gravity]
           [".qst" @accepted-qst]]]
    (let [core (canonical-core artifact)
          preimage (:identity-preimage core)
          projection-id
          (invoke-c7 'sh08-ft-semantic-projection-id [preimage])
          expected
          {:module-id (:module preimage)
           :lineage (:lineage preimage)
           :source-root (:root-core-node-ids preimage)
           :nodes (:nodes preimage)
           :definitions (:definitions preimage)
           :binding-table (:binding-table preimage)
           :calls (:calls preimage)
           :function-records (:function-records preimage)
           :call-edges (:call-edges preimage)
           :recursion-components (:recursion-components preimage)
           :lexical-bindings (:lexical-bindings preimage)}
          actual
          {:module-id (:module core)
           :lineage
           (invoke-c7 'sh08-ft-semantic-lineage
                      [projection-id (:lineage core)])
           :source-root (:root-core-node-ids core)
           :nodes
           (invoke-c7 'sh08-ft-semantic-nodes
                      [projection-id (:nodes core)])
           :definitions (:definitions core)
           :binding-table
           (invoke-c7 'sh08-ft-semantic-bindings
                      [projection-id
                       (get-in artifact
                               [:gravity-core-boundary
                                :authenticated-core-request
                                :binding-table])])
           :calls (:calls core)
           :function-records (:function-records core)
           :call-edges (:call-edges core)
           :recursion-components (:recursion-components core)
           :lexical-bindings (:lexical-bindings core)}]
      (doseq [key-name
              [:module-id :lineage :source-root :nodes :definitions
               :binding-table :calls :function-records :call-edges
               :recursion-components :lexical-bindings]]
        (let [expected-value (get expected key-name)
              actual-value (get actual key-name)
              difference (first-difference expected-value actual-value)]
          (is (= expected-value actual-value)
              (pr-str
               {:extension extension
                :field key-name
                :expected-count
                (when (coll? expected-value) (count expected-value))
                :actual-count
                (when (coll? actual-value) (count actual-value))
                :first-difference difference})))))))

(deftest sh08-edge-lineage-is-exact-and-mutations-fail
  (let [call-id (sha-id "1")
        operator-id (sha-id "2")
        argument-id (sha-id "3")
        caller-syntax-id (sha-id "4")
        caller-core-id (sha-id "5")
        callee-syntax-id (sha-id "6")
        callee-core-id (sha-id "7")
        callee-binding-id (sha-id "8")
        callee-definition-id (sha-id "9")
        callee-definition-syntax-id (sha-id "a")
        callee-name-syntax-id (sha-id "e")
        call
        {:core-node-id call-id
         :operator-node-id operator-id
         :operator-binding-id callee-binding-id
         :argument-node-ids [argument-id]
         :ordered-evaluation-node-ids [operator-id argument-id]
         :evaluation-order :operator-then-arguments
         :result-policy :call-result}
        edge
        {:ordinal 0
         :call-core-node-id call-id
         :caller-function-syntax-id caller-syntax-id
         :caller-function-core-node-id caller-core-id
         :callee-binding-id callee-binding-id
         :callee-definition-syntax-id callee-name-syntax-id
         :callee-function-syntax-id callee-syntax-id
         :callee-function-core-node-id callee-core-id
         :argument-core-node-ids [argument-id]
         :ordered-evaluation-node-ids [operator-id argument-id]
         :evaluation-order :operator-then-arguments
         :classification :local-function}
        functions
        {caller-syntax-id
         {:function-core-node-id caller-core-id}
         callee-syntax-id
         {:function-core-node-id callee-core-id
          :definition-binding-id callee-binding-id
          :definition-core-node-id callee-definition-id}}
        definitions
        [{:binding-id callee-binding-id
          :core-node-id callee-definition-id
          :syntax-id callee-definition-syntax-id}]
        nodes
        {call-id
         {:evaluation
          {:owner-function-syntax-id caller-syntax-id}}
         callee-definition-id
         {:node-id callee-definition-id :core-form :def :children [callee-core-id]
          :resolved-binding-ids [callee-binding-id]
          :attributes {:name-syntax-id callee-name-syntax-id}
          :source {:syntax-id callee-definition-syntax-id}}}
        valid?
        (fn [candidate]
          (invoke-c7 'sh08-ft-call-edge-valid?
                     [candidate call functions definitions nodes]))]
    (is (true? (valid? edge)))
    (is (false? (valid? (assoc edge :callee-definition-syntax-id callee-definition-syntax-id))))
    (is (false?
         (valid?
          (assoc edge :callee-definition-syntax-id (sha-id "b")))))
    (is (false?
         (valid?
          (assoc edge :caller-function-core-node-id (sha-id "c")))))
    (is (false?
         (valid?
          (assoc edge :caller-function-syntax-id (sha-id "d")))))))

(deftest sh08-bounded-inference-reports-nonconvergence
  (let [result
        (invoke-c7
         'sh08-ft-infer-acyclic
         [[] {} [] [] [] [] {} {} 0])]
    (is (= :nonconverged (:convergence-status result)))
    (is (= 0 (:round-count result) (:round-bound result)))
    (is (= :bounded-function-type-inference-nonconvergence
           (get-in result [:diagnostics 0 :reason])))
    (is (= :fixed-point-within-declared-round-bound
           (get-in result [:diagnostics 0 :expected])))))

(deftest sh08-two-and-three-function-chains-reach-one-consistent-pass
  (doseq [function-count [2 3]]
    (let [{:keys [nodes node-table function-records definitions
                  calls edges function-types call-node-ids]}
          (synthetic-chain-input function-count)
          round-bound
          (invoke-c7
           'sh08-ft-inference-round-bound
           [nodes function-records calls []])
          result
          (invoke-c7
           'sh08-ft-infer-acyclic
           [nodes node-table [] definitions calls edges
            function-types {} round-bound])]
      (is (= :converged (:convergence-status result)))
      (is (<= (:round-count result) (:round-bound result)))
      (is (= round-bound (:round-bound result)))
      (doseq [function (vals (:function-types result))]
        (is (= [:gravity.type/integer]
               (mapv :type (:parameters function))))
        (is (= :gravity.type/integer (:return function))))
      (doseq [call-node-id call-node-ids]
        (is (= :gravity.type/integer
               (get (:type-table result) call-node-id)))))))

(deftest sh08-function-return-inference-is-monotone-and-conflicts-fail-closed
  (let [function-id (synthetic-id :return-function 0)
        body-id (synthetic-id :return-body 0)
        node {:node-id body-id :source {:origin-chain []}}
        base
        {function-id
         {:function-id function-id
          :body-core-node-id body-id
          :return :gravity.type/integer
          :return-conflict nil}}
        unchanged
        (invoke-c7
         'sh08-ft-update-function-returns
         [base {body-id node} {body-id :gravity.type/unknown}])
        conflicting
        (invoke-c7
         'sh08-ft-update-function-returns
         [base {body-id node} {body-id :gravity.type/string}])
        conflict
        (invoke-c7
         'sh08-ft-first-return-conflict
         [conflicting {body-id node}])]
    (is (= :gravity.type/integer
           (get-in unchanged [function-id :return])))
    (is (nil? (get-in unchanged [function-id :return-conflict])))
    (is (= :gravity.type/integer
           (get-in conflicting [function-id :return])))
    (is (= :gravity.type/integer (:expected conflict)))
    (is (= :gravity.type/string (:actual conflict)))
    (is (= node (:node conflict)))))

(deftest sh08-nonlocal-call-constraint-is-never-silently-unknown
  (let [call-id (sha-id "e")
        call {:core-node-id call-id :argument-node-ids []}
        edge {:classification :nonlocal-or-nonfunction}
        result
        (invoke-c7
         'sh08-ft-call-constraints
         [[call] [edge] {call-id {:source {:origin-chain []}}}
          {} {} {}])]
    (is (= 1 (count (:diagnostics result))))
    (is (= "C7-ANNOTATION"
           (get-in result [:diagnostics 0 :rule])))
    (is (= :unsupported-nonlocal-call
           (get-in result [:diagnostics 0 :reason])))
    (is (= :supported-local-first-order-call
           (get-in result [:diagnostics 0 :expected])))
    (is (= :nonlocal-or-nonfunction
           (get-in result [:diagnostics 0 :actual])))))

(deftest sh08-call-mismatch-retains-expected-and-actual-types
  (let [call-id (synthetic-id :mismatch-call 0)
        argument-id (synthetic-id :mismatch-argument 0)
        function-id (synthetic-id :mismatch-function 0)
        parameter-id (synthetic-id :mismatch-parameter 0)
        call {:core-node-id call-id :argument-node-ids [argument-id]}
        edge {:classification :local-function
              :callee-function-syntax-id function-id}
        function-types
        {function-id
         {:parameters
          [{:binding-id parameter-id :type :gravity.type/integer}]}}
        result
        (invoke-c7
         'sh08-ft-call-constraints
         [[call] [edge] {call-id {:source {:origin-chain []}}}
          {argument-id :gravity.type/string}
          {parameter-id :gravity.type/integer}
          function-types])]
    (is (= :call-argument-type-mismatch
           (get-in result [:diagnostics 0 :reason])))
    (is (= :gravity.type/integer
           (get-in result [:diagnostics 0 :expected])))
    (is (= :gravity.type/string
           (get-in result [:diagnostics 0 :actual])))
    (is (= parameter-id
           (get-in result [:diagnostics 0 :parameter-binding-id])))))

(deftest sh08-function-local-call-products-are-typed-and-linked
  (let [gravity (function-result @accepted-gravity)
        qst (function-result @accepted-qst)
        function (result-function gravity)
        functions (:function-type-table gravity)
        local (result-local gravity)
        call (result-call gravity)
        calls (:call-type-facts gravity)]
    (is (= :accepted (:status gravity) (:status qst))
        (pr-str
         {:gravity
          (select-keys (first (:diagnostics gravity))
                       [:rule :reason :expected-type :actual-type])
          :qst
          (select-keys (first (:diagnostics qst))
                       [:rule :reason :expected-type :actual-type])}))
    (when (= :accepted (:status gravity) (:status qst))
      (is (= :gravity/sh08-function-typed-core-template
           (:artifact gravity)))
    (is (= 3 (:schema-version gravity)))
    (is (= :first-order-fixed-arity-functions-locals-calls
           (:scope gravity)))
    (is (= :host-resolved-b47-verification-boundary
           (:authentication-status gravity)))
    (let [policy
          (invoke-c7 'sh08-function-type-boundary-policy [])]
      (is (= :host-validated-b47-report-and-opaque-provenance-digest
             (:verification-boundary policy)))
      (is (false?
           (:independent-gravity-cryptographic-verification? policy)))
      (is (true? (:host-digest-authority-retained? policy))))
    (is (= (:identity-input gravity)
           (:artifact-id-request gravity)))
    (is (= (:identity-input gravity)
           (:identity-input qst)))
    (is (= (:provenance (canonical-core @accepted-gravity))
           (:provenance gravity)))
    (is (= (:provenance (canonical-core @accepted-qst))
           (:provenance qst)))
    (is (= (:artifact-id (canonical-core @accepted-gravity))
           (:sh07-shaped-artifact-id gravity)))
    (is (= (:artifact-id (canonical-core @accepted-qst))
           (:sh07-shaped-artifact-id qst)))
    (is (not (contains? (:identity-input gravity) :source-path)))
    (is (= 3 (count functions)))
    (doseq [function-fact functions]
      (is (= :inferred (:status function-fact)))
      (is (= 1 (:fixed-arity function-fact)))
      (is (= [:gravity.type/integer]
             (mapv :type (:parameters function-fact))))
      (is (= :gravity.type/integer (:return function-fact))))
    (is (= :gravity.type/integer (:type local)))
    (is (= 3 (count calls)))
    (doseq [call-fact calls]
      (is (= [:gravity.type/integer] (:argument-types call-fact)))
      (is (= :gravity.type/integer (:result-type call-fact)))
      (is (= :operator-then-arguments (:evaluation-order call-fact))))
    (is (every? #(= :solved (:status %))
                (:constraint-ledger gravity)))
    (is (not-any? #{:gravity.type/unknown}
                  (vals (:type-table gravity))))
    (is (= :converged (get-in gravity [:convergence :status])))
    (is (<= (get-in gravity [:convergence :round-count])
            (get-in gravity [:convergence :round-bound])))
    (is (= :monotone-finite-type-fact-propagation
           (get-in gravity [:convergence :proof])))
    (is (= (set (map :function-core-node-id functions))
           (set (map :function-core-node-id
                     (get-in gravity
                             [:function-products :function-records])))))
    (is (= :gravity.type/integer (:return function)))
      (is (= :gravity.type/integer (:result-type call))))))

(deftest sh08-two-function-chain-converges-consistently
  (let [gravity (function-result @accepted-two-gravity)
        qst (function-result @accepted-two-qst)]
    (is (= :accepted (:status gravity) (:status qst)))
    (is (= 2 (count (:function-type-table gravity))))
    (is (= 2 (count (:call-type-facts gravity))))
    (is (every? #(= [:gravity.type/integer]
                    (mapv :type (:parameters %)))
                (:function-type-table gravity)))
    (is (every? #(= :gravity.type/integer (:return %))
                (:function-type-table gravity)))
    (is (every? #(= :gravity.type/integer (:result-type %))
                (:call-type-facts gravity)))
    (is (= :converged (get-in gravity [:convergence :status])))
    (is (= (:identity-input gravity) (:identity-input qst)))))

(deftest sh08-function-results-are-repeatable-and-verifiable
  (let [gravity (function-result @accepted-gravity)
        repeated (function-result @accepted-gravity)
        qst (function-result @accepted-qst)]
    (is (= gravity repeated))
    (is (= :passed (:status (verification-result @accepted-gravity gravity))))
    (is (= :passed (:status (verification-result @accepted-qst qst))))
    (is (= (:function-type-table gravity)
           (:function-type-table qst)))
    (is (= (:call-type-facts gravity)
           (:call-type-facts qst)))
    (is (= (:local-binding-facts gravity)
           (:local-binding-facts qst)))
    (is (= (:constraint-ledger gravity)
           (:constraint-ledger qst)))))

(deftest sh08-function-result-alterations-fail-closed
  (let [artifact @accepted-gravity
        result (function-result artifact)
        alterations
        [(assoc-in result [:function-type-table 0 :return]
                  :gravity.type/string)
         (assoc-in result [:local-binding-facts 0 :type]
                   :gravity.type/string)
         (assoc-in result [:call-type-facts 0 :result-type]
                   :gravity.type/string)
         (assoc-in result [:constraint-ledger 0 :status]
                   :rejected)
         (assoc-in result [:convergence :round-count] 0)]]
    (doseq [candidate alterations]
      (is (= :rejected
             (:status (verification-result artifact candidate)))))))

(deftest sh08-b47-product-alterations-fail-before-inference
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        alterations
        [(assoc-in core [:function-records 0 :fixed-arity] 99)
         (assoc-in core [:call-edges 0 :callee-function-syntax-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:call-edges 0 :callee-definition-syntax-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:call-edges 0 :caller-function-core-node-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in core [:lexical-bindings 0 :ordinal] 9)
         (assoc-in core [:function-records 0 :body-core-node-id]
                   "sha256:0000000000000000000000000000000000000000000000000000000000000000")]]
    (doseq [candidate alterations]
      (let [request
            (assoc (function-request artifact)
                   :canonical-core-artifact candidate)
            result (invoke-c7 'sh08-function-type-core-artifact [request])]
        (is (= :rejected (:status result)))
        (is (= "C7-VERIFY"
               (get-in result [:diagnostics 0 :rule])))))))

(deftest sh08-host-resolved-verification-digest-binds-exact-b47-evidence
  (let [request (function-request @accepted-gravity)
        wrong-id (sha-id "0")
        alterations
        [(assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :preimage :verified-artifact-id]
          wrong-id)
         (assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :digest]
          wrong-id)
         (assoc-in
          request
          [:coordinator-verification :verification-digest-resolution
           :preimage :canonical-identity-preimage :binding-table]
          [])]]
    (doseq [candidate alterations]
      (let [result
            (invoke-c7 'sh08-function-type-core-artifact [candidate])]
        (is (= :rejected (:status result)))
        (is (= :untrusted-b47-coordinator-verification
               (get-in result [:diagnostics 0 :actual-type])))))))

(deftest sh08-coordinated-carrier-and-identity-rewrites-fail-real-b47-verification
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        bindings
        (get-in artifact
                [:gravity-core-boundary :authenticated-core-request
                 :binding-table])
        identity-bindings (get-in core [:identity-preimage :binding-table])
        definitions (:definitions core)
        identity-definitions (get-in core [:identity-preimage :definitions])
        semantically-used-binding-ids
        (set
         (concat
          (map :definition-binding-id (:function-records core))
          (mapcat :parameter-binding-ids (:function-records core))
          (map :operator-binding-id (:calls core))
          (map :binding-id (:lexical-bindings core))
          (map :binding-id (:reference-uses core))))
        unused-definition-index
        (first
         (keep-indexed
          (fn [index definition]
            (when-not
             (contains? semantically-used-binding-ids
                        (:binding-id definition))
              index))
          definitions))
        unused-definition (get definitions unused-definition-index)
        unused-binding-index
        (first
         (keep-indexed
          (fn [index binding]
            (when (contains?
                   #{(:binding-id binding) (:upstream-binding-id binding)}
                   (:binding-id unused-definition))
              index))
          bindings))
        unused-binding (get bindings unused-binding-index)
        identity-definition-index
        (first
         (keep-indexed
          (fn [index definition]
            (when (= (:binding-id unused-definition)
                     (:binding-id definition))
              index))
          identity-definitions))
        identity-binding-index
        (first
         (keep-indexed
          (fn [index binding]
            (when (or (= (:binding-id binding)
                         (:binding-id unused-binding))
                      (= (:upstream-binding-id binding)
                         (:binding-id unused-binding))
                      (= (:binding-id binding)
                         (:upstream-binding-id unused-binding)))
              index))
          identity-bindings))
        remove-at
        (fn [values index]
          (if (and (integer? index) (< -1 index (count values)))
            (vec (concat (subvec values 0 index)
                         (subvec values (+ index 1))))
            nil))
        reversed-bindings (vec (reverse bindings))
        reversed-identity-bindings (vec (reverse identity-bindings))
        reversed
        (-> artifact
            (assoc-in
             [:gravity-core-boundary :authenticated-core-request
              :binding-table]
             reversed-bindings)
            (assoc-in
             [:gravity-core-boundary :canonical-core-artifact
              :identity-preimage :binding-table]
             reversed-identity-bindings))
        check-rejected
        (fn [rewritten]
          (let [report (b47-verification rewritten)
                request (function-request rewritten)
                result
                (invoke-c7 'sh08-function-type-core-artifact [request])]
            (is (= :failed (:status report)))
            (is (seq (:failed-checks report)))
            (is (= :rejected (:status result)))
            (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
            (is (= :untrusted-b47-context
                   (get-in result [:diagnostics 0 :actual-type])))))]
    (is (some? unused-binding-index))
    (is (some? unused-definition-index))
    (is (some? identity-binding-index))
    (is (some? identity-definition-index))
    (check-rejected reversed)
    (when (every? some?
                  [unused-binding-index unused-definition-index
                   identity-binding-index identity-definition-index])
      (let [reduced-bindings (remove-at bindings unused-binding-index)
            reduced-definitions
            (remove-at definitions unused-definition-index)
            reduced-identity-bindings
            (remove-at identity-bindings identity-binding-index)
            reduced-identity-definitions
            (remove-at identity-definitions identity-definition-index)
            rewritten
            (-> artifact
                (assoc-in
                 [:gravity-core-boundary :authenticated-core-request
                  :binding-table]
                 reduced-bindings)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :definitions]
                 reduced-definitions)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :identity-preimage :binding-table]
                 reduced-identity-bindings)
                (assoc-in
                 [:gravity-core-boundary :canonical-core-artifact
                  :identity-preimage :definitions]
                 reduced-identity-definitions))]
        (check-rejected rewritten)))))

(deftest sh08-definition-and-edge-substitution-fails-before-inference
  (let [artifact @accepted-gravity
        core (canonical-core artifact)
        first-definition (get-in core [:definitions 0])
        replacement-definition (get-in core [:definitions 1])
        replacement-edge (get-in core [:call-edges 1])
        coordinated
        (-> core
            (assoc-in
             [:definitions 0]
             (merge
              first-definition
              (select-keys
               replacement-definition
               [:name :binding-id :value-node-id :syntax-id])))
            (update-in
             [:call-edges 0]
             merge
             (select-keys
              replacement-edge
              [:callee-binding-id :callee-definition-syntax-id
               :callee-function-syntax-id
               :callee-function-core-node-id])))
        request
        (assoc (function-request artifact)
               :canonical-core-artifact coordinated)
        result
        (invoke-c7 'sh08-function-type-core-artifact [request])]
    (is (not= core coordinated))
    (is (= :rejected (:status result)))
    (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
    (is (= :b47-core-context-mismatch
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-nonlocal-edge-uses-authoritative-binding-lineage
  (let [artifact @accepted-nonlocal-gravity
        core (canonical-core artifact)
        edge-index
        (first
         (keep-indexed
          (fn [index edge]
            (when (= :nonlocal-or-nonfunction (:classification edge))
              index))
          (:call-edges core)))
        edge (get-in core [:call-edges edge-index])
        call
        (first
         (filter
          #(= (:call-core-node-id edge) (:core-node-id %))
          (:calls core)))
        operator-node
        (first
         (filter
          #(= (:operator-node-id call) (:node-id %))
          (:nodes core)))
        altered
        (assoc-in
         core
         [:call-edges edge-index :callee-definition-syntax-id]
         (sha-id "0"))
        request
        (assoc (function-request artifact) :canonical-core-artifact altered)
        result (invoke-c7 'sh08-function-type-core-artifact [request])]
    (is (some? edge-index))
    (is (= (:callee-binding-id edge)
           (get-in operator-node [:attributes :binding-id])))
    (is (= (get-in operator-node [:attributes :definition-syntax-id])
           (:callee-definition-syntax-id edge)))
    (is (= :rejected (:status result)))
    (is (= :b47-core-context-mismatch
           (get-in result [:diagnostics 0 :actual-type])))))

(deftest sh08-recursion-products-are-complete-and-cannot-be-deleted
  (let [artifact @recursive-b47-gravity
        core (canonical-core artifact)
        components (:recursion-components core)
        component-index
        (first
         (keep-indexed
          (fn [index component]
            (when (= :mutually-recursive (:kind component)) index))
          components))
        component (get components component-index)
        outside-edge-ordinal
        (first
         (remove
          (set (:internal-call-edge-ordinals component))
          (mapv :ordinal (:call-edges core))))
        alterations
        [(assoc core :recursion-components [])
         (update-in
          core
          [:recursion-components component-index :function-core-node-ids]
          pop)
         (update-in
          core
          [:recursion-components component-index
           :internal-call-edge-ordinals]
          pop)
         (assoc-in
          core
          [:recursion-components component-index
           :internal-call-edge-ordinals 0]
          outside-edge-ordinal)]]
    (is (seq components))
    (is (some? component-index))
    (is (> (count (:function-core-node-ids component)) 1))
    (is (> (count (:internal-call-edge-ordinals component)) 1))
    (is (some? outside-edge-ordinal))
    (doseq [candidate alterations]
      (let [result
            (invoke-c7
             'sh08-function-type-core-artifact
             [(assoc (function-request artifact)
                     :canonical-core-artifact candidate)])]
        (is (= :rejected (:status result)))
        (is (= "C7-VERIFY" (get-in result [:diagnostics 0 :rule])))
        (is (= :b47-core-context-mismatch
               (get-in result [:diagnostics 0 :actual-type])))))))

(deftest sh08-arity-mismatch-is-structured-and-co-canonical
  (let [gravity (function-result @rejected-gravity)
        qst (function-result @rejected-qst)]
    (is (= :rejected (:status gravity) (:status qst)))
    (is (= :first-order-fixed-arity-functions-locals-calls
           (:scope gravity)))
    (is (= (:diagnostics gravity) (:diagnostics qst)))
    (is (= "C7-TYPE-MISMATCH"
           (get-in gravity [:diagnostics 0 :rule])))
    (is (= :call-arity-mismatch
           (get-in gravity [:diagnostics 0 :reason])))
    (is (= :type-checking
           (get-in gravity [:diagnostics 0 :stage])))
    (is (string? (get-in gravity [:diagnostics 0 :diagnostic-id])))
    (is (some? (get-in gravity [:diagnostics 0 :type-id])))
    (is (map? (get-in gravity [:diagnostics 0 :constraint-id])))
    (is (= (:identity-input gravity) (:identity-input qst)))))

(deftest sh08-argument-type-mismatch-is-structured
  (let [gravity (function-result @rejected-type-gravity)
        qst (function-result @rejected-type-qst)]
    (is (= (slurp (fixture-path
                   "rejected" "function-call-type-mismatch" ".gravity"))
           (slurp (fixture-path
                   "rejected" "function-call-type-mismatch" ".qst"))))
    (is (= :rejected (:status gravity) (:status qst)))
    (is (= "C7-TYPE-MISMATCH"
           (get-in gravity [:diagnostics 0 :rule])))
    (is (= :call-argument-type-mismatch
           (get-in gravity [:diagnostics 0 :reason])))
    (is (= :gravity.type/integer
           (get-in gravity [:diagnostics 0 :expected-type])))
    (is (= :gravity.type/string
           (get-in gravity [:diagnostics 0 :actual-type])))
    (is (string? (get-in gravity [:diagnostics 0 :diagnostic-id])))
    (is (= :gravity.type/string
           (get-in gravity [:diagnostics 0 :type-id])))
    (is (map? (get-in gravity [:diagnostics 0 :constraint-id])))
    (is (string?
         (get-in gravity [:diagnostics 0 :parameter-binding-id])))
    (is (= (:diagnostics gravity) (:diagnostics qst)))))

(deftest sh08-capture-free-one-hop-nonlocal-call-is-promoted
  (let [gravity (function-result @accepted-nonlocal-gravity)
        qst (function-result @accepted-nonlocal-qst)
        gravity-verification
        (verification-result @accepted-nonlocal-gravity gravity)
        qst-verification (verification-result @accepted-nonlocal-qst qst)]
    (is (= :gravity/sh07-canonical-core-artifact
           (:artifact (canonical-core @accepted-nonlocal-gravity))))
    (is (= :gravity/sh07-canonical-core-artifact
           (:artifact (canonical-core @accepted-nonlocal-qst))))
    (is (= :accepted (:status gravity) (:status qst)))
    (is (= :capture-free-higher-order-fixed-arity-one-hop
           (:scope gravity) (:scope qst)))
    (is (= :nonlocal-or-nonfunction
           (get-in gravity [:higher-order-proof :inner-edge :classification])))
    (is (= :local-function
           (get-in gravity [:higher-order-proof :outer-edge :classification])))
    (is (= :gravity.type/integer
           (get (:type-table gravity)
                (get-in gravity
                        [:higher-order-proof :outer-call-core-node-id]))))
    (is (= (:identity-input gravity) (:identity-input qst)))
    (is (= :passed (:status gravity-verification)
           (:status qst-verification)))))
