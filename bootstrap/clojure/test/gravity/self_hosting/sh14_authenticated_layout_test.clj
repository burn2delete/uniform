(ns gravity.self-hosting.sh14-authenticated-layout-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-c10-mir-adapter-test]
            [gravity.self-hosting.sh13-c11-domain-evidence-adapter-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh14_authenticated_layout_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "SH14-AUTH-LAYOUT-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c12-relative-path
  "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")
(def ^:private maximum-c12-source-bytes (* 1024 1024))

(defn- bounded-c12-source-text
  []
  (let [source-path (.resolve @root c12-relative-path)
        options (into-array java.nio.file.OpenOption
                            [java.nio.file.StandardOpenOption/READ
                             java.nio.file.LinkOption/NOFOLLOW_LINKS])]
    (with-open [channel (java.nio.channels.FileChannel/open source-path options)]
      (let [size (.size channel)]
        (when (or (neg? size) (> size maximum-c12-source-bytes))
          (throw (ex-info "C12 source exceeds the SH14 source-only bound"
                          {:id "SH14-AUTH-LAYOUT-SOURCE-BOUND"
                           :maximum-bytes maximum-c12-source-bytes
                           :actual-bytes size})))
        (let [buffer (java.nio.ByteBuffer/allocate (int size))]
          (loop []
            (when (.hasRemaining buffer)
              (let [read-count (.read channel buffer)]
                (when (neg? read-count)
                  (throw (ex-info "C12 source ended before its measured size"
                                  {:id "SH14-AUTH-LAYOUT-SOURCE-SHORT-READ"})))
                (recur))))
          (String. (.array buffer) java.nio.charset.StandardCharsets/UTF_8))))))

(defn- c12-source-forms
  []
  (with-open [reader
              (clojure.lang.LineNumberingPushbackReader.
               (java.io.StringReader. (bounded-c12-source-text)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn- calls-to
  [operator value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

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
    (compile-plan c12-relative-path)))

(defn- invoke-c12
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh14-authenticated-layout-test
    :compiler-artifact-plan? true}
   @c12-plan function arguments))

(defn- c12-source-exports
  []
  (let [source-text
        (bounded-c12-source-text)
        reader (java.io.PushbackReader.
                (java.io.StringReader. source-text))
        ns-form (read {:eof nil} reader)
        exports-clause
        (some (fn [clause]
                (when (and (seq? clause) (= :exports (first clause)))
                  clause))
              (drop 2 ns-form))]
    (set (second exports-clause))))

(defn- sh12-value
  [name]
  (let [resolved
        (ns-resolve
         'gravity.self-hosting.sh12-c10-mir-adapter-test name)]
    (if (var? resolved)
      (var-get resolved)
      (throw
       (ex-info
        "required SH12 test helper is missing"
        {:id "SH14-AUTH-LAYOUT-SH12-HELPER-MISSING"
         :helper name})))))

(def ^:private prepared-c11
  (delay @(sh12-value 'prepared-c11)))

(defn- digest
  [ordinal]
  (format "sha256:%064x" (long ordinal)))

(defn- domain-case
  [candidate verification ordinal]
  (let [template
        (invoke-c12 'sh13-build-c11-domain-evidence-template
                    [candidate verification])
        identity-request
        (invoke-c12 'sh13-c11-domain-evidence-identity-request
                    [candidate verification template])
        resolution {:request (:request identity-request)
                    :digest (digest ordinal)}
        evidence
        (invoke-c12 'sh13-bind-c11-domain-evidence
                    [candidate verification template resolution])]
    {:template template
     :identity-request identity-request
     :resolution resolution
     :evidence evidence}))

(defn- layout-request
  [candidate evidence slot-count]
  (let [operation (:operation candidate)
        facts (:facts operation)
        source-span
        (invoke-c12 'sh14-auth-layout-logical-source-span [candidate])
        origin-chain
        (invoke-c12 'sh14-auth-layout-logical-origin-chain [candidate])
        element-size 8
        alignment 8
        raw-size (* slot-count element-size)
        size-bytes
        (* alignment
           (quot (+ raw-size (dec alignment)) alignment))]
    {:artifact :gravity/sh14-data-layout-request
     :value-id :authenticated-fixed-slots
     :kind :tuple
     :payload [(:type-fact-id facts)
               (:effect-fact-id facts)
               (:ownership-fact-id facts)
               (:safety-outcome-id facts)]
     :capacity slot-count
     :length slot-count
     :element-size element-size
     :size-bytes size-bytes
     :alignment alignment
     :allocation-regime :static
     :mutable false
     :profile (get-in evidence [:identity-input :profile])
     :target (get-in evidence [:identity-input :target])
     :source-span source-span
     :origin-chain origin-chain
     :type-fact-id (:type-fact-id facts)
     :effect-fact-id (:effect-fact-id facts)
     :ownership-fact-id (:ownership-fact-id facts)
     :safety-fact-id (:safety-outcome-id facts)}))

(defn- authenticated-request
  [candidate verification evidence slot-count]
  {:artifact :gravity/sh14-authenticated-layout-request
   :schema-version 1
   :c11-mir candidate
   :c11-verification verification
   :domain-evidence evidence
   :layout-request (layout-request candidate evidence slot-count)
   :facts (:facts evidence)
   :semantic-anchor (:semantic-anchor evidence)
   :profile (get-in evidence [:identity-input :profile])
   :target (get-in evidence [:identity-input :target])
   :provenance (:provenance evidence)})

(defn- prepared-request
  [slot-count ordinal]
  (let [candidate (:candidate @prepared-c11)
        verification (:result @prepared-c11)
        evidence (:evidence (domain-case candidate verification ordinal))]
    (authenticated-request candidate verification evidence slot-count)))

(defn- reason
  [result]
  (get-in result [:diagnostics 0 :reason]))

(def ^:private diagnostic-keys
  #{:diagnostic-id :rule :family :reason :missing-fact :stage :domain
    :artifact-id :semantic-anchor :owner-document :verifier :value-id
    :kind :profile :target :source-span :generated-origin-chain
    :remediation})

(def ^:private accepted-keys
  #{:artifact :schema-version :status :layout :facts :semantic-anchor
    :profile :target :target-independent? :executable-load?
    :lowering-status :semantic-authority :identity-binding-status
    :identity-input :provenance :diagnostics :pending :nonclaims})

(def ^:private pending-obligations
  [:trusted-digest-resolution :independent-canonical-digest-verification
   :c11-mir-verifier :physical-allocation :target-abi
   :pointer-and-lifetime-layouts :backend-lowering :runtime-execution
   :executable-load :semantic-authority :proof-certificate
   :self-hosting :release :seed-retirement])

(deftest sh14-authenticated-layout-source-parses-before-compilation
  (let [forms (c12-source-forms)
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        if-calls (mapcat #(calls-to 'if %) definitions)]
    (is (seq forms))
    (is (= 'ns (ffirst forms)))
    (is (seq definitions))
    (is (seq if-calls))
    (is (every? #(= 4 (count %)) if-calls))))

(deftest sh14-authenticated-layout-surface-arity-and-nonclaims
  (let [functions (:functions @c12-plan)
        exported
        #{'sh14-authenticated-layout-input-valid?
          'sh14-build-authenticated-layout
          'sh14-verify-authenticated-layout}
        nonclaims
        #{:physical-allocation :target-abi :pointer-semantics
          :lifetime-semantics :backend-lowering :runtime-execution
          :executable-artifact :compiler-authority :proof-certificate
          :trusted-digest-resolution
          :independent-canonical-digest-verification :c11-mir-verifier
          :self-hosting :release :seed-retirement}]
    (is (var? (ns-resolve
               'gravity.self-hosting.sh12-c10-mir-adapter-test
               'prepared-c11)))
    (is (= 1 (get-in functions
                     ['sh14-authenticated-layout-input-valid? :arity])))
    (is (= 1 (get-in functions
                     ['sh14-build-authenticated-layout :arity])))
    (is (= 2 (get-in functions
                     ['sh14-verify-authenticated-layout :arity])))
    (is (every? (c12-source-exports) exported))
    (is (= nonclaims
           (set (invoke-c12 'sh14-auth-layout-nonclaims []))))))

(deftest sh14-authenticated-layout-genuine-positive-computes-logical-offsets
  (let [request (prepared-request 4 914001)
        candidate
        (invoke-c12 'sh14-build-authenticated-layout [request])
        verification
        (invoke-c12 'sh14-verify-authenticated-layout
                    [request candidate])]
    (is (true? (invoke-c12 'sh14-authenticated-layout-input-valid?
                           [request])))
    (is (= :accepted (:status candidate)) candidate)
    (is (= accepted-keys (set (keys candidate))) candidate)
    (is (= :fixed-slot-v1 (get-in candidate [:layout :layout-model])))
    (is (= false (get-in candidate [:layout :physical-layout?])))
    (is (= [0 8 16 24]
           (get-in candidate [:layout :logical-byte-offsets])))
    (is (= 8 (get-in candidate [:layout :element-size-bytes])))
    (is (= 8 (get-in candidate [:layout :alignment-bytes])))
    (is (= 32 (get-in candidate [:layout :raw-size-bytes])))
    (is (= 32 (get-in candidate [:layout :logical-size-bytes])))
    (is (true? (:target-independent? candidate)))
    (is (false? (:executable-load? candidate)))
    (is (= :rejected (:lowering-status candidate)))
    (is (= :none (:semantic-authority candidate)))
    (is (= :unbound (:identity-binding-status candidate)))
    (is (= pending-obligations (:pending candidate)))
    (is (= :authenticated-fixed-slots
           (get-in request [:layout-request :value-id])))
    (is (= :static (get-in request [:layout-request :allocation-regime])))
    (is (= (:profile request) (get-in request [:layout-request :profile])))
    (is (= (:target request) (get-in request [:layout-request :target])))
    (is (= (invoke-c12 'sh14-auth-layout-logical-source-span
                       [(:c11-mir request)])
           (get-in request [:layout-request :source-span])))
    (is (= (invoke-c12 'sh14-auth-layout-logical-origin-chain
                       [(:c11-mir request)])
           (get-in request [:layout-request :origin-chain])))
    (is (= "gravity-logical://authenticated-c11-mir"
           (get-in request
                   [:layout-request :source-span :actual-source-path])))
    (is (not= (get-in request [:c11-mir :operation :source :source-span])
              (get-in request [:layout-request :source-span])))
    (is (= (:payload (:layout-request request))
           (get-in candidate [:identity-input :layout-fact-tuple])))
    (is (= (invoke-c12 'sh14-identity-input [(:layout-request request)])
           (get-in candidate [:identity-input :layout-request])))
    (is (= (:pending candidate)
           (get-in candidate [:identity-input :pending])))
    (is (= true (get-in candidate [:identity-input :target-independent?])))
    (is (= false (get-in candidate [:identity-input :executable-load?])))
    (is (= :rejected (get-in candidate [:identity-input :lowering-status])))
    (is (= :none (get-in candidate [:identity-input :semantic-authority])))
    (is (= :unbound
           (get-in candidate [:identity-input :identity-binding-status])))
    (is (= (:facts request) (:facts candidate)))
    (is (= (:semantic-anchor request) (:semantic-anchor candidate)))
    (is (= (:profile request) (:profile candidate)))
    (is (= (:target request) (:target candidate)))
    (is (= (:provenance request) (:provenance candidate)))
    (is (= :passed (:status verification)))
    (is (= candidate (:expected verification)))
    (is (= candidate (:candidate verification)))))

(deftest sh14-authenticated-layout-rejects-mutations-and-hostile-carriers
  (let [request (prepared-request 4 914101)
        candidate
        (invoke-c12 'sh14-build-authenticated-layout [request])
        wrong-facts (assoc request :facts {})
        wrong-anchor (assoc request :semantic-anchor {})
        wrong-profile (assoc request :profile :hosted)
        wrong-target
        (update request :target #(if (= % :jvm) :llvm :jvm))
        wrong-layout
        (assoc-in request [:layout-request :element-size] 16)
        wrong-value-id
        (assoc-in request [:layout-request :value-id] :other-layout)
        wrong-allocation
        (assoc-in request [:layout-request :allocation-regime] :heap)
        wrong-nested-profile
        (update-in request [:layout-request :profile]
                   #(if (= % :safe) :meta :safe))
        wrong-nested-target
        (update-in request [:layout-request :target]
                   #(if (= % :portable) :portable-mir :portable))
        wrong-nested-source-span
        (assoc-in request [:layout-request :source-span]
                  (get-in request [:c11-mir :operation :source :source-span]))
        wrong-nested-origin
        (assoc-in request [:layout-request :origin-chain]
                  (get-in request [:c11-mir :operation :source :origin-chain]))
        reordered-facts
        (update-in request [:layout-request :payload]
                   #(vec (reverse %)))
        substituted-fact
        (assoc-in request [:layout-request :payload 2]
                  (digest 914199))
        overbound-layout
        (assoc request :layout-request
               (assoc (layout-request
                       (:c11-mir request) (:domain-evidence request) 4)
                      :capacity 4097
                      :length 4097
                      :payload (get-in request [:layout-request :payload])
                      :size-bytes 32776))
        overflow-layout
        (assoc request :layout-request
               (assoc (layout-request
                       (:c11-mir request) (:domain-evidence request) 4)
                      :payload (get-in request [:layout-request :payload])
                      :size-bytes 9223372036854775807))
        lazy-request (assoc request :facts (lazy-seq [:hostile]))
        deep-request
        (assoc request :facts
               (loop [remaining 257 value :leaf]
                 (if (zero? remaining)
                   value
                   (recur (dec remaining) [value]))))
        substituted-candidate
        (assoc-in candidate [:layout :logical-byte-offsets] [0 8 24 32])
        extra-key-candidate (assoc candidate :host-carrier :forbidden)
        authority-candidate (assoc candidate :semantic-authority :compiler)]
    (doseq [[label mutation expected-reason]
            [[:facts wrong-facts :facts-substitution]
             [:anchor wrong-anchor :semantic-anchor-substitution]
             [:profile wrong-profile :profile-substitution]
             [:target wrong-target :target-substitution]
             [:layout wrong-layout :element-size-not-eight]
             [:value-id wrong-value-id :layout-value-id-substitution]
             [:allocation wrong-allocation :allocation-regime-not-static]
             [:nested-profile wrong-nested-profile
              :layout-profile-substitution]
             [:nested-target wrong-nested-target :layout-target-substitution]
             [:nested-source-span wrong-nested-source-span
              :layout-source-span-substitution]
             [:nested-origin wrong-nested-origin :layout-origin-substitution]
             [:reordered-facts reordered-facts :layout-fact-substitution]
             [:substituted-fact substituted-fact :layout-fact-substitution]
             [:overbound overbound-layout :layout-length-not-four]
             [:overflow overflow-layout :unaligned-size-mismatch]
             [:lazy lazy-request :request-member-carrier-rejected]
             [:deep deep-request :request-member-carrier-rejected]]]
      (testing (str label)
        (is (not= request mutation)
            "hostile mutation must change the authenticated request")
        (let [result
              (invoke-c12 'sh14-build-authenticated-layout [mutation])]
          (is (= :rejected (:status result)) result)
          (is (= expected-reason (reason result)) result)
          (is (= #{:artifact :schema-version :status :diagnostics :pending}
                 (set (keys result))) result)
          (is (= diagnostic-keys
                 (set (keys (first (:diagnostics result))))) result)
          (is (= "S7-LAYOUT"
                 (get-in result [:diagnostics 0 :diagnostic-id])))
          (is (= "S7-LAYOUT" (get-in result [:diagnostics 0 :rule])))
          (is (= :logical-data-layout
                 (get-in result [:diagnostics 0 :family])))
          (is (= :domain-ir-layout (get-in result [:diagnostics 0 :stage])))
          (is (= :compiler-data-layout (get-in result [:diagnostics 0 :domain])))
          (is (= "C12" (get-in result [:diagnostics 0 :owner-document])))
          (is (= :gravity.compiler.c12-domain-ir-architecture/sh14-verify-authenticated-layout
                 (get-in result [:diagnostics 0 :verifier])))
          (is (not (contains? (first (:diagnostics result)) :candidate)))
          (is (not (contains? (first (:diagnostics result)) :evidence))))))
    (let [verification
          (invoke-c12 'sh14-verify-authenticated-layout
                      [request substituted-candidate])]
      (is (= :rejected (:status verification)))
      (is (= :candidate-substitution (reason verification)))
      (is (= diagnostic-keys
             (set (keys (first (:diagnostics verification)))))))
    (let [verification
          (invoke-c12 'sh14-verify-authenticated-layout
                      [request extra-key-candidate])]
      (is (= :rejected (:status verification)))
      (is (= :candidate-schema-mismatch (reason verification))))
    (let [verification
          (invoke-c12 'sh14-verify-authenticated-layout
                      [request authority-candidate])]
      (is (= :rejected (:status verification)))
      (is (= :candidate-substitution (reason verification))))))

(deftest sh14-authenticated-layout-identity-is-path-neutral-with-separate-provenance
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
        evidence-a
        (:evidence (domain-case candidate-a verification-a 914201))
        evidence-b
        (:evidence (domain-case candidate-b verification-b 914201))
        request-a
        (authenticated-request candidate-a verification-a evidence-a 4)
        request-b
        (authenticated-request candidate-b verification-b evidence-b 4)
        layout-a
        (invoke-c12 'sh14-build-authenticated-layout [request-a])
        layout-b
        (invoke-c12 'sh14-build-authenticated-layout [request-b])]
    (is (= :accepted (:status layout-a)))
    (is (= :accepted (:status layout-b)))
    (is (= (:identity-input layout-a) (:identity-input layout-b)))
    (is (= (get-in request-a [:layout-request :source-span])
           (get-in request-b [:layout-request :source-span])))
    (is (= (get-in request-a [:layout-request :origin-chain])
           (get-in request-b [:layout-request :origin-chain])))
    (is (not= (:provenance layout-a) (:provenance layout-b)))
    (is (= :passed
           (:status
            (invoke-c12 'sh14-verify-authenticated-layout
                        [request-a layout-a]))))
    (is (= :passed
           (:status
            (invoke-c12 'sh14-verify-authenticated-layout
                        [request-b layout-b]))))))
