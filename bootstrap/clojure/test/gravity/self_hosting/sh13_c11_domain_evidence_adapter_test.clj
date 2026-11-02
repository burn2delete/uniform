(ns gravity.self-hosting.sh13-c11-domain-evidence-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-c10-mir-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh13_c11_domain_evidence_adapter_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH13-C11-DOMAIN-EVIDENCE-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- compile-plan
  [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c12-plan
  (delay
    (compile-plan
     "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")))

(defn- invoke-c12
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh13-c11-domain-evidence-adapter-test
    :compiler-artifact-plan? true}
   @c12-plan function arguments))

(defn- sh12-value
  [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh12-c10-mir-adapter-test name)))

(defn- c12-source-exports
  []
  (let [source-path
        (str (.resolve @root
                       "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity"))
        source-text (slurp source-path)
        reader (java.io.PushbackReader.
                (java.io.StringReader. source-text))
        ns-form (read {:eof nil} reader)
        exports-clause
        (some (fn [clause]
                (when (and (seq? clause)
                           (= :exports (first clause)))
                  clause))
              (drop 2 ns-form))]
    (set (second exports-clause))))

(defn- digest
  [ordinal]
  (format "sha256:%064x" (long ordinal)))

(def ^:private prepared-c11
  (delay
    @(sh12-value 'prepared-c11)))

(defn- require-admission-stage!
  [stage expected actual]
  (if (= expected actual)
    actual
    (throw
     (ex-info "C12 input admission stage rejected"
              {:stage stage
               :expected expected
               :actual actual}))))

(deftest sh13-c11-domain-evidence-surface-and-policy
  (let [functions (:functions @c12-plan)
        exported-names
        #{'sh13-c11-domain-evidence-policy
          'sh13-c11-domain-evidence-carrier-preflight
          'sh13-c11-domain-evidence-input-valid?
          'sh13-build-c11-domain-evidence-template
          'sh13-c11-domain-evidence-identity-request
          'sh13-bind-c11-domain-evidence
          'sh13-verify-c11-domain-evidence}
        exported-arities
        {'sh13-c11-domain-evidence-policy 0
         'sh13-c11-domain-evidence-carrier-preflight 1
         'sh13-c11-domain-evidence-input-valid? 2
         'sh13-build-c11-domain-evidence-template 2
         'sh13-c11-domain-evidence-identity-request 3
         'sh13-bind-c11-domain-evidence 4
         'sh13-verify-c11-domain-evidence 5}
        source-text
        (java.nio.file.Files/readString
         (.resolve @root
                   "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity"))
        representative-carrier
        (vec (repeat 64 (vec (range 64))))
        representative-preflight
        (invoke-c12
         'sh13-c11-domain-evidence-carrier-preflight
         [representative-carrier])
        policy
        (invoke-c12 'sh13-c11-domain-evidence-policy [])]
    (doseq [[name arity] exported-arities]
      (is (= arity (get-in functions [name :arity])) name))
    (is (every? (set (c12-source-exports)) exported-names))
    (is (= :safety-proof-reference (:domain policy)))
    (is (= :proof-reference-shape-only (:scope policy)))
    (is (= :rejected (:lowering-status policy)))
    (is (= :c11-mir-verifier-pending (:fallback policy)))
    (is (= :blocked (get-in policy [:b1-preflight :status])))
    (is (some #{:generic-mir-verifier} (:nonclaims policy)))
    (is (some #{:executable-load} (:nonclaims policy)))
    (is (some #{:target-backend-lowering} (:nonclaims policy)))
    (is (some #{:c12-authority} (:nonclaims policy)))
    (is (.contains source-text
                   "(subvec frontier 1 (count frontier))"))
    (is (not (.contains source-text "(subvec frontier 1)")))
    (is (= :accepted (:status representative-preflight)))
    (is (= true (:nodes-exact? representative-preflight)))))

(deftest sh13-c11-domain-evidence-input-admission
  (let [candidate (:candidate @prepared-c11)
        verification (:result @prepared-c11)
        operation (:operation candidate)
        candidate-preflight
        (invoke-c12 'sh13-c11-domain-evidence-carrier-preflight
                    [candidate])
        _candidate-preflight
        (require-admission-stage!
         :candidate-preflight :accepted (:status candidate-preflight))
        verification-preflight
        (invoke-c12 'sh13-c11-de-verification-envelope
                    [verification])
        _verification-preflight
        (require-admission-stage!
         :verification-preflight :accepted (:status verification-preflight))
        candidate-envelope-valid?
        (invoke-c12 'sh13-c11-de-candidate-envelope-valid?
                    [candidate])
        _candidate-envelope
        (require-admission-stage!
         :candidate-envelope true candidate-envelope-valid?)
        identity-resolution-valid?
        (invoke-c12 'sh13-c11-de-identity-resolution-valid?
                    [candidate operation])
        _identity-resolution
        (require-admission-stage!
         :identity-resolution true identity-resolution-valid?)
        operation-valid?
        (invoke-c12 'sh13-c11-de-operation-valid? [candidate])
        _operation
        (require-admission-stage! :operation true operation-valid?)
        function-valid?
        (invoke-c12 'sh13-c11-de-function-valid?
                    [candidate operation])
        _function
        (require-admission-stage! :function true function-valid?)
        source-map-valid?
        (invoke-c12 'sh13-c11-de-source-map-valid?
                    [candidate operation])
        _source-map
        (require-admission-stage! :source-map true source-map-valid?)
        tables-valid?
        (invoke-c12 'sh13-c11-de-tables-valid?
                    [candidate operation])
        _tables
        (require-admission-stage! :tables true tables-valid?)
        upstream-ids
        (invoke-c12 'sh13-c11-de-upstream-ids
                    [candidate operation])
        upstream-ids-valid?
        (invoke-c12 'sh13-c11-de-all-sha? [upstream-ids])
        _upstream-ids
        (require-admission-stage!
         :upstream-ids true upstream-ids-valid?)
        verification-valid?
        (invoke-c12 'sh13-c11-de-verification-valid?
                    [candidate verification])
        _verification
        (require-admission-stage!
         :verification true verification-valid?)
        input-valid?
        (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                    [candidate verification])
        _input
        (require-admission-stage! :input true input-valid?)
        template
        (invoke-c12
         'sh13-build-c11-domain-evidence-template
         [candidate verification])
        _template
        (require-admission-stage!
         :template :accepted (:status template))]
    (is (= :accepted (:status candidate-preflight))
        candidate-preflight)
    (is (= :accepted (:status verification-preflight))
        verification-preflight)
    (is candidate-envelope-valid? :candidate-envelope)
    (is identity-resolution-valid? :identity-resolution)
    (is operation-valid? :operation)
    (is function-valid? :function)
    (is source-map-valid? :source-map)
    (is tables-valid? :tables)
    (is upstream-ids-valid? upstream-ids)
    (is verification-valid? :verification)
    (is input-valid? :input)
    (is (= :accepted (:status template)) template)))

(deftest sh13-c11-domain-evidence-positive
  (let [candidate (:candidate @prepared-c11)
        verification (:result @prepared-c11)
        template
        (invoke-c12
         'sh13-build-c11-domain-evidence-template
         [candidate verification])
        request
        (invoke-c12
         'sh13-c11-domain-evidence-identity-request
         [candidate verification template])
        resolution
        {:request (:request request)
         :digest (digest 913001)}
        bound
        (invoke-c12
         'sh13-bind-c11-domain-evidence
         [candidate verification template resolution])
        result
        (invoke-c12
         'sh13-verify-c11-domain-evidence
         [candidate verification template resolution bound])]
    (is (= :accepted (:status candidate)))
    (is (= :gravity/sh12-c10-proof-reference-mir-verification
           (:artifact verification)))
    (is (= :passed (:status verification)))
    (is (= :accepted (:status template)))
    (is (= :accepted (:status request)))
    (is (= :accepted (:status bound)))
    (is (= :passed (:status result)))
    (is (= template
           (invoke-c12
            'sh13-build-c11-domain-evidence-template
            [candidate verification])))
    (is (= request
           (invoke-c12
            'sh13-c11-domain-evidence-identity-request
            [candidate verification template])))
    (is (= bound (:expected result)))
    (is (= bound (:candidate result)))
    (is (= :gravity/sh13-semantic-anchor
           (get-in template [:semantic-anchor :artifact])))
    (is (= :safety-proof-reference
           (get-in template [:semantic-anchor :domain])))
    (is (= :gravity/sh13-safety-proof-reference-payload
           (get-in template [:payload :artifact])))
    (is (= (:capability-proof-table candidate)
           (get-in template [:facts :c11-capability-proof-table])))
    (is (= :rejected (:lowering-status template)))
    (is (= :pending (get-in template [:fallback :status])))
    (is (= :c11-mir-verifier-pending
           (get-in template [:fallback :kind])))
    (is (= (get candidate :provenance) (:provenance bound)))
    (is (= (get candidate :provenance)
           (get-in template [:provenance])))
    (is (= (digest 913001)
           (get bound :artifact-id)))
    (is (= resolution (:identity-resolution bound)))
    (is (= :coordinator-resolution-shape-checked
           (:identity-binding-status bound)))))

(deftest sh13-c11-domain-evidence-fact-table-and-id-mutations
  (let [candidate (:candidate @prepared-c11)
        verification (:result @prepared-c11)
        operation (:operation candidate)
        facts (:facts operation)
        type-id (:type-fact-id facts)
        effect-id (:effect-fact-id facts)
        capability-id (:capability-proof-id facts)
        ownership-id (:ownership-fact-id facts)
        outcome-id (:safety-outcome-id facts)
        proof-id (:safety-proof-id facts)
        table-mutations
        [[:type-table
          (assoc-in candidate [:type-table type-id] {})]
         [:effect-table
          (assoc-in candidate [:effect-table effect-id] {})]
         [:ownership-table
          (assoc-in candidate [:ownership-table ownership-id] {})]
         [:capability-proof-table
          (assoc-in candidate
                    [:capability-proof-table capability-id]
                    {})]
         [:capability-table
          (assoc-in candidate [:capability-table capability-id] {})]
         [:safety-table
          (assoc-in candidate [:safety-table outcome-id] {})]
         [:proof-certificate-table
          (assoc-in candidate [:proof-certificate-table proof-id] {})]]
        cross-id-candidate
        (assoc-in candidate
                  [:operation :facts :safety-proof-id]
                  (digest 913101))
        table-candidate (second (first table-mutations))
        table-verification
        (assoc (assoc verification :expected table-candidate)
               :candidate table-candidate)
        cross-id-verification
        (assoc (assoc verification :expected cross-id-candidate)
               :candidate cross-id-candidate)]
    (is (invoke-c12 'sh13-c11-de-type-entry-valid?
                    [candidate operation type-id effect-id capability-id])
        :type-entry)
    (is (invoke-c12 'sh13-c11-de-effect-entry-valid?
                    [candidate operation effect-id])
        :effect-entry)
    (is (invoke-c12 'sh13-c11-de-ownership-entry-valid?
                    [candidate operation ownership-id])
        :ownership-entry)
    (is (invoke-c12 'sh13-c11-de-capability-proof-entry-valid?
                    [candidate operation capability-id])
        :capability-proof-entry)
    (is (invoke-c12 'sh13-c11-de-capability-entry-valid?
                    [candidate capability-id effect-id])
        :capability-entry)
    (is (invoke-c12 'sh13-c11-de-safety-entry-valid?
                    [candidate outcome-id proof-id])
        :safety-entry)
    (is (invoke-c12 'sh13-c11-de-proof-entry-valid?
                    [candidate proof-id outcome-id])
        :proof-entry)
    (is (invoke-c12 'sh13-c11-de-tables-valid?
                    [candidate operation]))
    (is (invoke-c12 'sh13-c11-de-operation-valid? [candidate]))
    (doseq [[label mutated] table-mutations]
      (is (false?
           (invoke-c12 'sh13-c11-de-tables-valid?
                       [mutated (:operation mutated)]))
          label))
    (is (false?
         (invoke-c12 'sh13-c11-de-operation-valid?
                     [cross-id-candidate])))
    (is (false?
         (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                     [table-candidate table-verification])))
    (is (false?
         (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                     [cross-id-candidate cross-id-verification])))))

(deftest sh13-c11-domain-evidence-hostile-carriers-and-recomputation
  (let [candidate (:candidate @prepared-c11)
        verification (:result @prepared-c11)
        operation (:operation candidate)
        operation-id (:op-id operation)
        token-id (:result operation)
        template
        (invoke-c12 'sh13-build-c11-domain-evidence-template
                    [candidate verification])
        request
        (invoke-c12 'sh13-c11-domain-evidence-identity-request
                    [candidate verification template])
        resolution {:request (:request request)
                    :digest (digest 913201)}
        bound
        (invoke-c12 'sh13-bind-c11-domain-evidence
                    [candidate verification template resolution])
        bad-template (assoc template :scope :substituted)
        bad-operation
        (assoc-in candidate [:operation :opcode] :load)
        bad-token
        (assoc-in candidate [:values token-id :defined-by]
                  (digest 913202))
        bad-source-map
        (assoc-in candidate [:source-map operation-id] {})
        verified-bad-operation
        (assoc (assoc verification :expected bad-operation)
               :candidate bad-operation)
        verified-bad-token
        (assoc (assoc verification :expected bad-token)
               :candidate bad-token)
        verified-bad-source-map
        (assoc (assoc verification :expected bad-source-map)
               :candidate bad-source-map)
        wrong-request-resolution {:request {}
                                  :digest (digest 913201)}
        reused-digest-resolution
        {:request (:request request)
         :digest (get-in candidate [:upstream :c10-safety-core-id])}
        tampered-bound (assoc bound :lowering-status :eligible)
        over-width (vec (range 1025))
        over-depth
        (loop [remaining 257 value :leaf]
          (if (= remaining 0)
            value
            (recur (- remaining 1) [value])))
        cyclic-reference (atom nil)
        cyclic-seq (lazy-seq (cons :cycle @cyclic-reference))
        _cyclic (reset! cyclic-reference cyclic-seq)
        width-preflight
        (invoke-c12 'sh13-c11-domain-evidence-carrier-preflight
                    [over-width])
        depth-preflight
        (invoke-c12 'sh13-c11-domain-evidence-carrier-preflight
                    [over-depth])
        cyclic-preflight
        (invoke-c12 'sh13-c11-domain-evidence-carrier-preflight
                    [cyclic-seq])]
    (is (= :accepted (:status template)))
    (is (= :accepted (:status request)))
    (is (= :accepted (:status bound)))
    (is (false?
         (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                     [bad-operation verified-bad-operation])))
    (is (false?
         (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                     [bad-token verified-bad-token])))
    (is (false?
         (invoke-c12 'sh13-c11-domain-evidence-input-valid?
                     [bad-source-map verified-bad-source-map])))
    (is (= :rejected
           (:status
            (invoke-c12 'sh13-c11-domain-evidence-identity-request
                        [candidate verification bad-template]))))
    (is (= :rejected
           (:status
            (invoke-c12 'sh13-bind-c11-domain-evidence
                        [candidate verification template
                         wrong-request-resolution]))))
    (is (= :rejected
           (:status
            (invoke-c12 'sh13-bind-c11-domain-evidence
                        [candidate verification template
                         reused-digest-resolution]))))
    (is (= :rejected
           (:status
            (invoke-c12 'sh13-verify-c11-domain-evidence
                        [candidate verification template resolution
                         tampered-bound]))))
    (is (= :rejected (:status width-preflight)))
    (is (= :carrier-vector-width (:reason width-preflight)))
    (is (= :rejected (:status depth-preflight)))
    (is (= :carrier-depth-bound (:reason depth-preflight)))
    (is (= :rejected (:status cyclic-preflight)))
    (is (= :arbitrary-seq (:reason cyclic-preflight)))))

(deftest sh13-c11-domain-evidence-path-neutral-provenance
  (let [candidate-a (:candidate @prepared-c11)
        verification-a (:result @prepared-c11)
        candidate-b
        (assoc candidate-a
               :provenance
               (assoc (:provenance candidate-a)
                      :actual-source-path
                      "/checkout-b/function.gravity"))
        verification-b
        (assoc (assoc verification-a :expected candidate-b)
               :candidate candidate-b)
        template-a
        (invoke-c12 'sh13-build-c11-domain-evidence-template
                    [candidate-a verification-a])
        template-b
        (invoke-c12 'sh13-build-c11-domain-evidence-template
                    [candidate-b verification-b])
        request-a
        (invoke-c12 'sh13-c11-domain-evidence-identity-request
                    [candidate-a verification-a template-a])
        request-b
        (invoke-c12 'sh13-c11-domain-evidence-identity-request
                    [candidate-b verification-b template-b])
        resolution {:request (:request request-a)
                    :digest (digest 913301)}
        bound-a
        (invoke-c12 'sh13-bind-c11-domain-evidence
                    [candidate-a verification-a template-a resolution])
        bound-b
        (invoke-c12 'sh13-bind-c11-domain-evidence
                    [candidate-b verification-b template-b resolution])
        result-a
        (invoke-c12 'sh13-verify-c11-domain-evidence
                    [candidate-a verification-a template-a resolution
                     bound-a])
        result-b
        (invoke-c12 'sh13-verify-c11-domain-evidence
                    [candidate-b verification-b template-b resolution
                     bound-b])]
    (is (= :accepted (:status template-a)))
    (is (= :accepted (:status template-b)))
    (is (not= (:provenance template-a) (:provenance template-b)))
    (is (= request-a request-b))
    (is (= :accepted (:status bound-a)))
    (is (= :accepted (:status bound-b)))
    (is (= (:artifact-id bound-a) (:artifact-id bound-b)))
    (is (not= (:provenance bound-a) (:provenance bound-b)))
    (is (= :passed (:status result-a)))
    (is (= :passed (:status result-b)))))
