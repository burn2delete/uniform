(ns gravity.self-hosting.sh15-diagnostic-boundary-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-c10-mir-adapter-test]
            [gravity.self-hosting.sh14-authenticated-layout-test]))

(defn- repository-root []
  (let [resource (io/resource
                  "gravity/self_hosting/sh15_diagnostic_boundary_test.clj")]
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate) (throw (ex-info "repository root not found"
                                         {:id "SH15-DIAGNOSTIC-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c15-relative-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")

(defn- source-forms []
  (let [path (.resolve @root c15-relative-path)
        size (java.nio.file.Files/size path)]
    (when (> size (* 1024 1024))
      (throw (ex-info "C15 source exceeds bound"
                      {:id "SH15-DIAGNOSTIC-SOURCE-BOUND" :size size})))
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (java.io.StringReader.
                         (java.nio.file.Files/readString path)))]
      (loop [forms []]
        (let [form (read {:eof ::eof :read-cond :allow} reader)]
          (if (= ::eof form) forms (recur (conj forms form))))))))

(defn- calls-to [operator value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- compile-plan [relative-path]
  (let [source-path (str (.resolve @root relative-path))
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c15-plan (delay (compile-plan c15-relative-path)))

(defn- invoke-c15 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-diagnostic-boundary-test
    :compiler-artifact-plan? true}
   @c15-plan function arguments))

(defn- required-var [namespace name]
  (let [resolved (ns-resolve namespace name)]
    (if (var? resolved) resolved
        (throw (ex-info "required upstream helper is missing"
                        {:id "SH15-DIAGNOSTIC-UPSTREAM-HELPER-MISSING"
                         :namespace namespace :helper name})))))

(defn- sh14-value [name]
  (var-get (required-var
            'gravity.self-hosting.sh14-authenticated-layout-test name)))

(defn- invoke-c12 [function arguments]
  ((sh14-value 'invoke-c12) function arguments))

(defn- digest [ordinal]
  (format "sha256:%064x" (long ordinal)))

(def ^:private genuine-case-build-count (atom 0))

(defn- genuine-case [ordinal]
  (swap! genuine-case-build-count inc)
  (let [prepared @(sh14-value 'prepared-c11)
        c11 (:candidate prepared)
        c11-verification (:result prepared)
        domain-case ((sh14-value 'domain-case)
                     c11 c11-verification ordinal)
        evidence (:evidence domain-case)
        domain-verification
        (invoke-c12 'sh13-verify-c11-domain-evidence
                    [c11 c11-verification (:template domain-case)
                     (:resolution domain-case) evidence])
        layout-request ((sh14-value 'authenticated-request)
                        c11 c11-verification evidence 4)
        layout (invoke-c12 'sh14-build-authenticated-layout [layout-request])
        layout-verification
        (invoke-c12 'sh14-verify-authenticated-layout
                    [layout-request layout])]
    {:evidence evidence :domain-verification domain-verification
     :layout-request layout-request :layout layout
     :layout-verification layout-verification}))

(defn- external-references [case]
  (let [evidence (:evidence case)
        layout (:layout case)
        domain-ref
        {:artifact :gravity/sh15-external-sh13-evidence-reference
         :schema-version 1
         :source-artifact (:artifact evidence)
         :source-schema-version (:schema-version evidence)
         :source-status (:status evidence)
         :artifact-id (:artifact-id evidence)
         :source-identity-binding-status (:identity-binding-status evidence)
         :source-scope (:scope evidence)
         :source-lowering-status (:lowering-status evidence)
         :profile (get-in evidence [:identity-input :profile])
         :target (get-in evidence [:identity-input :target])}
        layout-ref
        {:artifact :gravity/sh15-external-sh14-layout-reference
         :schema-version 1
         :source-artifact (:artifact layout)
         :source-schema-version (:schema-version layout)
         :source-status (:status layout)
         :source-identity-binding-status (:identity-binding-status layout)
         :layout-model (get-in layout [:layout :layout-model])
         :physical-layout? (get-in layout [:layout :physical-layout?])
         :target-independent? (:target-independent? layout)
         :executable-load? (:executable-load? layout)
         :source-lowering-status (:lowering-status layout)
         :source-semantic-authority (:semantic-authority layout)
         :profile (:profile layout) :target (:target layout)}]
    {:domain-reference domain-ref
     :domain-verification-reference
     {:artifact :gravity/sh15-external-sh13-verification-reference
      :schema-version 1
      :status :external-reference-shape-and-equality-only
      :source-verifier-artifact
      (get-in case [:domain-verification :artifact])
      :source-verifier-schema-version
      (get-in case [:domain-verification :schema-version])
      :source-verifier-status (get-in case [:domain-verification :status])
      :scope (get-in case [:domain-verification :scope])
      :checks (get-in case [:domain-verification :checks])
      :nonclaims (get-in case [:domain-verification :nonclaims])
      :expected-reference domain-ref :candidate-reference domain-ref}
     :layout-reference layout-ref
     :layout-verification-reference
     {:artifact :gravity/sh15-external-sh14-verification-reference
      :schema-version 1
      :status :external-reference-shape-and-equality-only
      :source-verifier-artifact (get-in case [:layout-verification :artifact])
      :source-verifier-schema-version
      (get-in case [:layout-verification :schema-version])
      :source-verifier-status (get-in case [:layout-verification :status])
      :nonclaims (get-in case [:layout-verification :nonclaims])
      :expected-reference layout-ref :candidate-reference layout-ref}}))

;; Every semantic selector exercises the same authenticated upstream shape.
;; Keep one immutable, process-local representative so the fixed batch pays
;; the SH13/SH14 construction once.  Individual tests still build their own
;; requests and persistent mutations, and a fresh JVM always starts cold.
(def ^:private representative-case (delay (genuine-case 915001)))
(def ^:private representative-references
  (delay (external-references @representative-case)))

(defn- boundary-request [references diagnostics provenance]
  {:artifact :gravity/sh15-diagnostic-boundary-request :schema-version 1
   :domain-reference (:domain-reference references)
   :domain-verification-reference (:domain-verification-reference references)
   :layout-reference (:layout-reference references)
   :layout-verification-reference (:layout-verification-reference references)
   :upstream-diagnostics diagnostics
   :profile (get-in references [:domain-reference :profile])
   :target (get-in references [:domain-reference :target])
   :provenance provenance})

(defn- rejected-layout-result [case]
  (let [bad (assoc-in (:layout-request case)
                      [:layout-request :element-size] 16)]
    (invoke-c12 'sh14-build-authenticated-layout [bad])))

(def ^:private representative-rejected-layout
  (delay (rejected-layout-result @representative-case)))

(defn- rejected-layout-diagnostic []
  (let [result @representative-rejected-layout]
    (is (= :rejected (:status result)) result)
    (first (:diagnostics result))))

(defn- reason [result]
  (get-in result [:diagnostics 0 :facts :reason]))

(deftest sh15-diagnostic-boundary-source-surface-and-policy
  (let [forms (source-forms)
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        if-calls (mapcat #(calls-to 'if %) definitions)
        functions (:functions @c15-plan)
        ns-form (first forms)
        exports (set (second (some #(when (= :exports (first %)) %)
                                   (drop 2 ns-form))))]
    (is (seq definitions))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= #{'sh15-diagnostic-input-valid?
             'sh15-build-diagnostic-boundary
             'sh15-verify-diagnostic-boundary}
           (set (filter #(str/starts-with? (name %) "sh15-") exports))))
    (is (= 1 (get-in functions ['sh15-diagnostic-input-valid? :arity])))
    (is (= 1 (get-in functions ['sh15-build-diagnostic-boundary :arity])))
    (is (= 2 (get-in functions ['sh15-verify-diagnostic-boundary :arity])))
    (is (some #{:upstream-semantic-authentication}
              (invoke-c15 'sh15-nonclaims [])))
    (is (some #{:full-upstream-carrier-replay}
              (invoke-c15 'sh15-nonclaims [])))))

(deftest sh15-diagnostic-boundary-accepts-small-genuine-derived-references
  (let [case @representative-case]
    (is (= 1 @genuine-case-build-count))
    (is (= :passed (get-in case [:domain-verification :status])))
    (is (= :passed (get-in case [:layout-verification :status])))
    (let [references @representative-references
          request (boundary-request references [] {:actual-source-path "/a"})
          candidate (invoke-c15 'sh15-build-diagnostic-boundary [request])
          verification (invoke-c15 'sh15-verify-diagnostic-boundary
                                   [request candidate])]
      (is (true? (invoke-c15 'sh15-diagnostic-input-valid? [request])))
      (is (= :accepted (:status candidate)) candidate)
      (is (empty? (get-in candidate [:diagnostic-stream :diagnostics])))
      (is (= :external-reference-shape-and-equality-only
             (:verification-status candidate)))
      (is (= :none (:semantic-authority candidate)))
      (is (false? (:executable? candidate)))
      (is (true? (:seed-boundary? candidate)))
      (is (false? (:self-hosted? candidate)))
      (is (= :passed (:status verification))))))

(deftest sh15-diagnostic-boundary-preserves-real-rejection-partially
  (let [references @representative-references
        upstream (rejected-layout-diagnostic)
        request (boundary-request references [upstream]
                                  {:actual-source-path "/a"})
        candidate (invoke-c15 'sh15-build-diagnostic-boundary [request])
        diagnostic (get-in candidate [:diagnostic-stream :diagnostics 0])]
    (is (= 1 @genuine-case-build-count))
    (is (= :accepted (:status candidate)))
    (is (= :gravity/sh15-partial-diagnostic (:artifact diagnostic)))
    (is (= (:diagnostic-id upstream) (:upstream-diagnostic-id diagnostic)))
    (is (= (:source-span upstream) (get-in diagnostic [:primary :span])))
    (is (= (:generated-origin-chain upstream) (:origin-chain diagnostic)))
    (is (= :not-evaluated (:redactions diagnostic)))
    (is (= :partial (:conformance-status diagnostic)))
    (is (= :external-unverified-preservation-input
           (:source-status diagnostic)))))

(deftest sh15-diagnostic-boundary-rejects-schema-policy-and-hostile-inputs
  (let [refs @representative-references
        upstream (rejected-layout-diagnostic)
        request (boundary-request refs [upstream] {:actual-source-path "/a"})
        candidate (invoke-c15 'sh15-build-diagnostic-boundary [request])
        extra-ref (assoc-in request [:domain-reference :extra] :forbidden)
        wrong-policy (assoc-in request
                               [:domain-verification-reference :checks] [])
        authority (assoc-in request
                            [:layout-reference :source-semantic-authority]
                            :compiler)
        substitution (assoc-in request
                               [:domain-verification-reference
                                :candidate-reference :artifact-id]
                               (digest 915299))
        lazy-value (assoc request :upstream-diagnostics (lazy-seq [upstream]))
        scalar-value (assoc request :provenance
                            (apply str (repeat 65537 "x")))
        deep-value (assoc request :provenance
                          (loop [n 130 v :leaf]
                            (if (zero? n) v (recur (dec n) [v]))))
        overbound (assoc request :upstream-diagnostics
                         (vec (repeat 257 upstream)))
        malformed-rule
        (assoc-in request [:upstream-diagnostics 0 :rule]
                  (apply str (repeat 129 "R")))
        malformed-fact
        (assoc-in request [:upstream-diagnostics 0 :missing-fact]
                  "not-a-keyword")
        negative-span
        (assoc-in request
                  [:upstream-diagnostics 0 :source-span :start :byte] -1)
        oversized-span
        (assoc-in request
                  [:upstream-diagnostics 0 :source-span :end :byte]
                  2147483648)
        substituted-candidate (assoc candidate :semantic-authority :compiler)
        forged-id (digest 915298)
        forged-domain (assoc (:domain-reference refs) :artifact-id forged-id)
        forged-refs
        (assoc
         (assoc refs :domain-reference forged-domain)
         :domain-verification-reference
         (assoc (assoc (:domain-verification-reference refs)
                       :expected-reference forged-domain)
                :candidate-reference forged-domain))
        forged-request (boundary-request forged-refs [] {:path "/forged"})]
    (is (= 1 @genuine-case-build-count))
    (doseq [[label mutation expected]
            [[:extra extra-ref :domain-reference-invalid]
             [:policy wrong-policy :domain-verification-reference-invalid]
             [:authority authority :layout-reference-invalid]
             [:substitution substitution :domain-verification-reference-invalid]
             [:lazy lazy-value :request-carrier-rejected]
             [:scalar scalar-value :request-carrier-rejected]
             [:deep deep-value :request-carrier-rejected]
             [:overbound overbound :diagnostic-count-bound]
             [:malformed-rule malformed-rule :upstream-diagnostic-schema]
             [:malformed-fact malformed-fact :upstream-diagnostic-schema]
             [:negative-span negative-span :upstream-diagnostic-schema]
             [:oversized-span oversized-span :upstream-diagnostic-schema]]]
      (testing (str label)
        (let [result (invoke-c15 'sh15-build-diagnostic-boundary [mutation])]
          (is (= :rejected (:status result)) result)
          (is (= expected (reason result)) result)
          (is (not (contains? (first (:diagnostics result)) :request))))))
    (is (true? (invoke-c15 'sh15-diagnostic-input-valid? [forged-request]))
        "self-consistent forged references are admissible shape inputs")
    (let [verification (invoke-c15 'sh15-verify-diagnostic-boundary
                                   [request substituted-candidate])]
      (is (= :rejected (:status verification)))
      (is (= :candidate-substitution (reason verification))))))

(deftest sh15-diagnostic-boundary-identity-is-path-neutral
  (let [refs @representative-references
        diagnostic-a (rejected-layout-diagnostic)
        diagnostic-b
        (-> diagnostic-a
            (assoc-in [:source-span :actual-source-path] "/checkout/b/source")
            (assoc-in [:generated-origin-chain 0 :actual-source-path]
                      "/checkout/b/source"))
        request-a (boundary-request refs [diagnostic-a]
                                    {:actual-source-path "/checkout/a"})
        request-b (boundary-request refs [diagnostic-b]
                                    {:actual-source-path "/checkout/b"})
        candidate-a (invoke-c15 'sh15-build-diagnostic-boundary [request-a])
        candidate-b (invoke-c15 'sh15-build-diagnostic-boundary [request-b])]
    (is (= 1 @genuine-case-build-count))
    (is (= :accepted (:status candidate-a)))
    (is (= :accepted (:status candidate-b)))
    (is (= (:identity-input candidate-a) (:identity-input candidate-b)))
    (is (not= (:provenance candidate-a) (:provenance candidate-b)))
    (is (= :passed (:status (invoke-c15 'sh15-verify-diagnostic-boundary
                                        [request-a candidate-a]))))
    (is (= :passed (:status (invoke-c15 'sh15-verify-diagnostic-boundary
                                        [request-b candidate-b]))))))
