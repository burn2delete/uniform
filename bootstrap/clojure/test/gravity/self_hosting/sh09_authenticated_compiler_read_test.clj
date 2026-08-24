(ns gravity.self-hosting.sh09-authenticated-compiler-read-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh09_authenticated_compiler_read_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-09 authenticated compiler-read test is not on the classpath"
        {:id "SH09-COMPILER-READ-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH09-COMPILER-READ-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private c8-source
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")

(def ^:private accepted-fixture
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-09/accepted/"
   "authenticated-compiler-read.edn"))

(def ^:private rejected-fixture
  (str
   "bootstrap/clojure/fixtures/self-hosting/sh-09/rejected/"
   "authenticated-compiler-read.edn"))

(defn- read-fixture
  [relative]
  (edn/read-string (slurp (path relative))))

(def ^:private accepted (delay (read-fixture accepted-fixture)))
(def ^:private rejected (delay (read-fixture rejected-fixture)))

(defn- compile-plan
  []
  (let [source-path (path c8-source)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private c8-plan (delay (compile-plan)))

(defn- invoke-c8
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh09-authenticated-compiler-read-test
    :compiler-artifact-plan? true}
   @c8-plan function arguments))

(defn- sh08-var
  [name]
  (or
   (ns-resolve
    'gravity.self-hosting.sh08-function-call-type-test name)
   (throw
    (ex-info
     "Required SH-08 function helper is unavailable"
     {:id "SH09-COMPILER-READ-SH08-HELPER" :name name}))))

(defn- sh08-call
  [name & arguments]
  (apply (var-get (sh08-var name)) arguments))

(defn- source-text
  [profile effects capabilities]
  (str
   "; SH-09 authenticated compiler-read input.\n"
   "(ns gravity.self-hosting.sh09.compiler-read\n"
   "  (:profile " (pr-str profile) ")\n"
   "  (:target :jvm)\n"
   "  (:exports [sh09-read-ir sh09-read-ir-result])\n"
   "  (:effects " (pr-str (set effects)) ")\n"
   "  (:capabilities " (pr-str (set capabilities)) ")\n"
   "  (:safety :safe))\n\n"
   "(defn sh09-read-ir [value]\n"
   "  (let [local-value value]\n"
   "    local-value))\n\n"
   "(def sh09-read-ir-result (sh09-read-ir true))\n"))

(defn- seal-binding
  [request typed verification]
  (let [preimage
        {:domain :gravity/sh09-sh08-function-binding-v1
         :request request
         :typed typed
         :verification verification}]
    {:artifact :gravity/sh09-authenticated-sh08-function-binding
     :schema-version 1
     :boundary :clojure-coordinator-verifier
     :status :passed
     :digest-resolution
     {:algorithm :sha256
      :encoding :gravity/canonical-edn-v1
      :preimage preimage
      :digest (bootstrap/p15-s23-c11-mir-digest preimage)
      :status :passed}}))

(defn- fresh-sh08-products
  [label profile effects capabilities]
  (let [directory
        (java.nio.file.Files/createTempDirectory
         (str "sh09-compiler-read-" label "-")
         (make-array java.nio.file.attribute.FileAttribute 0))
        source-path (.resolve directory "input.gravity")]
    (try
      (spit (str source-path)
            (source-text profile effects capabilities))
      (let [artifact
            (bootstrap/sh07-core-file-artifact (str source-path))
            request (sh08-call 'function-request artifact)
            typed (sh08-call 'function-result artifact)
            verification
            (sh08-call 'verification-result artifact typed)]
        {:request request
         :typed typed
         :verification verification
         :binding (seal-binding request typed verification)})
      (finally
        (java.nio.file.Files/deleteIfExists source-path)
        (java.nio.file.Files/deleteIfExists directory)))))

(def ^:private accepted-products-a
  (delay
   (fresh-sh08-products
    "checkout-a"
    (:module-profile @accepted)
    (:module-effects @accepted)
    (:module-capabilities @accepted))))

(def ^:private accepted-products-b
  (delay
   (fresh-sh08-products
    "checkout-b"
    (:module-profile @accepted)
    (:module-effects @accepted)
    (:module-capabilities @accepted))))

(def ^:private undeclared-products
  (delay
   (fresh-sh08-products
    "undeclared-effect"
    :meta [] [:compiler/ir-read])))

(defn- context-for
  [typed overrides]
  (let [call (first (:call-type-facts typed))
        artifact-id (:sh07-shaped-artifact-id typed)
        context
        (-> (:authority-context @accepted)
            (assoc-in [:occurrence :call-core-node-id]
                      (:call-core-node-id call))
            (assoc-in [:occurrence :callee-function-syntax-id]
                      (:callee-function-syntax-id call))
            (assoc :resource-subject
                   {:ir-level :typed-core
                    :typed-artifact-id artifact-id})
            (assoc-in [:grant :scope]
                      {:ir-level :typed-core
                       :typed-artifact-id artifact-id}))]
    (merge context overrides)))

(defn- build
  [products context]
  (invoke-c8
   'sh09-build-authenticated-compiler-read-effect-result
   [(:typed products) (:verification products)
    (:binding products) context]))

(defn- forged-product-carrier
  [products product-path replacement]
  (let [typed (:typed products)
        forged-products
        (assoc-in (:function-products typed) product-path replacement)
        forged-identity
        (-> (:identity-input typed)
            (assoc :function-records (:function-records forged-products))
            (assoc :call-edges (:call-edges forged-products))
            (assoc :recursion-components
                   (:recursion-components forged-products))
            (assoc :lexical-bindings (:lexical-bindings forged-products)))
        forged-typed
        (-> typed
            (assoc :function-products forged-products)
            (assoc :identity-input forged-identity)
            (assoc :artifact-id-request forged-identity))
        forged-verification
        (-> (:verification products)
            (assoc :expected forged-typed)
            (assoc :candidate forged-typed))]
    {:request (:request products)
     :typed forged-typed
     :verification forged-verification
     :binding
     (seal-binding (:request products) forged-typed forged-verification)}))

(deftest sh09-authenticated-compiler-read-policy-is-closed-and-bounded
  (let [functions (:functions @c8-plan)
        policy
        (invoke-c8 'sh09-authenticated-compiler-read-policy [])]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @c8-plan)))
    (is (= {:arity 4
            :params ['typed 'verification 'binding 'context]}
           (select-keys
            (get functions
                 'sh09-build-authenticated-compiler-read-effect-result)
            [:arity :params])))
    (is (= {:arity 5
            :params ['typed 'verification 'binding 'context 'candidate]}
           (select-keys
            (get functions
                 'sh09-verify-authenticated-compiler-read-effect-result)
            [:arity :params])))
    (is (= :compiler/read-ir (:effect policy)))
    (is (= :compiler/ir-read (:required-capability policy)))
    (is (= :meta (:profile policy)))
    (is (= :jvm (:target policy)))
    (is (= :build (:phase policy)))
    (is (= :gravity/sh09-authenticated-sh08-function-binding
           (:upstream-binding-artifact policy)))
    (is (= :clojure-coordinator-verifier
           (:upstream-binding-boundary policy)))
    (is (not (contains? (:authority-context-keys policy) :effect)))
    (is (not (contains? (:authority-context-keys policy)
                        :required-capability)))
    (is (not (contains? (:authority-context-keys policy) :profile)))
    (is (some #{:authenticated-authority-context} (:pending policy)))
    (is (some #{:effectful-identity-digest-binding} (:pending policy)))))

(deftest sh09-propagates-one-authenticated-declared-compiler-read
  (let [products-a @accepted-products-a
        products-b @accepted-products-b
        typed-a (:typed products-a)
        typed-b (:typed products-b)
        context-a (context-for typed-a {})
        context-b (context-for typed-b {})
        result-a (build products-a context-a)
        result-b (build products-b context-b)
        request (first (:effect-requests result-a))
        legality (first (:effect-legality-results result-a))
        graph (:effect-graph result-a)
        call-id (get-in typed-a [:call-type-facts 0 :call-core-node-id])
        function-id (get-in typed-a [:function-type-table 0 :function-id])
        node (get-in graph [:nodes call-id])
        function (get-in graph [:functions function-id])]
    (is (= :accepted (:status result-a) (:status result-b)))
    (is (= :passed (get-in products-a [:verification :status])))
    (is (= :passed (get-in products-b [:verification :status])))
    (is (= :declared-compiler-read-call-effect (:scope result-a)))
    (is (= (:identity-input result-a) (:identity-input result-b)))
    (is (not= (:provenance result-a) (:provenance result-b)))
    (is (= :compiler/read-ir (:effect request)))
    (is (= :compiler/ir-read (:required-capability request)))
    (is (= #{:compiler/read-ir} (:declared-effects request)))
    (is (= #{:compiler/ir-read} (:declared-capabilities request)))
    (is (= :accepted (:status legality)))
    (is (= :compiler/read-ir
           (get-in legality [:capability-proof :effect])))
    (is (= :compiler/ir-read
           (get-in legality [:capability-proof :capability])))
    (is (= #{:compiler/read-ir}
           (:direct node) (:latent node) (:transitive node)))
    (is (= #{:compiler/read-ir}
           (:declared function) (:inferred function) (:latent function)))
    (is (= #{:compiler/ir-read} (:capabilities function)))
    (is (= #{:compiler/read-ir}
           (get-in graph [:namespace :declared])
           (get-in graph [:namespace :inferred])
           (get-in graph [:namespace :escaping])
           (:residual-effects graph)))
    (is (= [:compiler/read-ir] (:build-effects graph)))
    (is (= context-a
           (get-in result-a [:identity-input :authority-context])))
    (is (= :passed (get-in result-a [:upstream-binding :status])))
    (is (= (get legality :capability-proof)
           (get-in result-a [:identity-input :capability-proof])))
    (is (= :passed
           (:status
            (invoke-c8
             'sh09-verify-authenticated-compiler-read-effect-result
             [typed-a (:verification products-a) (:binding products-a)
              context-a result-a]))))))

(deftest sh09-rejects-declaration-authority-ambient-and-profile-gaps
  (doseq [{:keys [id route module-profile module-effects
                  context-overrides expected]}
          (:cases @rejected)]
    (testing (name id)
      (let [products
            (if (= id :undeclared-effect)
              @undeclared-products
              @accepted-products-a)
            typed (:typed products)
            context (context-for typed context-overrides)
            result
            (if (= route :normalized-effect-request)
              (let [accepted-result (build products context)
                    request
                    (assoc (first (:effect-requests accepted-result))
                           :profile module-profile)]
                (invoke-c8 'sh09-check-effect-request [request]))
              (build products context))
            diagnostic (first (:diagnostics result))]
        (is (= :rejected (:status result)))
        (is (= 1 (count (:diagnostics result))))
        (is (= (:rule expected)
               (:rule diagnostic) (:diagnostic-id diagnostic)))
        (is (= (:reason expected) (:reason diagnostic)))
        (is (= (:denied-layer expected) (:denied-layer diagnostic)))
        (is (= :compiler/read-ir (:effect diagnostic)))
        (is (= :compiler/ir-read (:capability diagnostic)))
        (is (= (or module-profile :meta) (:profile diagnostic)))
        (is (= (get-in typed [:call-type-facts 0 :call-core-node-id])
               (:core-node-id diagnostic)))))))

(deftest sh09-rejects-forged-context-upstream-and-result-substitution
  (let [products @accepted-products-a
        typed (:typed products)
        verification (:verification products)
        binding (:binding products)
        context (context-for typed {})
        accepted-result (build products context)
        malformed-context (assoc context :effect :compiler/write-ir)
        malformed-result (build products malformed-context)
        forged-verification (assoc verification :status :rejected)
        forged-upstream
        (invoke-c8
         'sh09-build-authenticated-compiler-read-effect-result
         [typed forged-verification binding context])
        forged-check-verification
        (assoc verification :checks [:path-neutral-identity-input])
        forged-checks
        (invoke-c8
         'sh09-build-authenticated-compiler-read-effect-result
         [typed forged-check-verification binding context])
        forged-products
        (forged-product-carrier
         products [:call-edges]
         ["not-an-authenticated-b47-call-edge"])
        forged-products-result
        (build forged-products context)
        substituted (assoc accepted-result :status :substituted)
        result-verification
        (invoke-c8
         'sh09-verify-authenticated-compiler-read-effect-result
         [typed verification binding context substituted])]
    (is (= :rejected (:status malformed-result)))
    (is (= "C8-VERIFY"
           (get-in malformed-result [:diagnostics 0 :rule])))
    (is (= :malformed-compiler-read-authority-context
           (get-in malformed-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status forged-upstream)))
    (is (= "C8-VERIFY"
           (get-in forged-upstream [:diagnostics 0 :rule])))
    (is (= :untrusted-sh08-function-binding
           (get-in forged-upstream [:diagnostics 0 :reason])))
    (is (= :rejected (:status forged-checks)))
    (is (= :untrusted-sh08-function-binding
           (get-in forged-checks [:diagnostics 0 :reason])))
    (is (= :rejected (:status forged-products-result)))
    (is (= :untrusted-sh08-compiler-read-carrier
           (get-in forged-products-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status result-verification)))
    (is (= accepted-result (:expected result-verification)))
    (is (= substituted (:candidate result-verification)))))

(deftest sh09-rejects-authenticated-product-coherence-mutations
  (let [products @accepted-products-a
        typed (:typed products)
        context (context-for typed {})
        record (get-in typed [:function-products :function-records 0])
        edge (get-in typed [:function-products :call-edges 0])
        lexical (get-in typed [:function-products :lexical-bindings 0])
        mutations
        [{:family :function-form-id
          :path [:function-records 0 :function-form-id]
          :value (:definition-core-node-id record)}
         {:family :definition-binding-id
          :path [:function-records 0 :definition-binding-id]
          :value (:function-core-node-id record)}
         {:family :definition-core-node-id
          :path [:function-records 0 :definition-core-node-id]
          :value (:function-core-node-id record)}
         {:family :callee-binding-id
          :path [:call-edges 0 :callee-binding-id]
          :value (:call-core-node-id edge)}
         {:family :callee-definition-syntax-id
          :path [:call-edges 0 :callee-definition-syntax-id]
          :value (:call-core-node-id edge)}
         {:family :argument-core-node-ids
          :path [:call-edges 0 :argument-core-node-ids]
          :value [(:call-core-node-id edge)]}
         {:family :ordered-evaluation-node-ids
          :path [:call-edges 0 :ordered-evaluation-node-ids]
          :value (vec (reverse (:ordered-evaluation-node-ids edge)))}
         {:family :lexical-binding-content
          :path [:lexical-bindings 0 :name]
          :value 'substituted-local}]]
    (is (map? lexical))
    (doseq [{:keys [family path value]} mutations]
      (testing (name family)
        (let [forged (forged-product-carrier products path value)
              result (build forged context)]
          (is (= :rejected (:status result)))
          (is (= "C8-VERIFY" (get-in result [:diagnostics 0 :rule])))
          (is (= :untrusted-sh08-compiler-read-carrier
                 (get-in result [:diagnostics 0 :reason]))))))))

(deftest sh09-rejects-request-substitution-and-fabricated-passed-summary
  (let [products @accepted-products-a
        typed (:typed products)
        verification (:verification products)
        context (context-for typed {})
        substituted-request (:request @accepted-products-b)
        substituted-products
        (assoc products :request substituted-request
               :binding (seal-binding substituted-request typed verification))
        report-path [:coordinator-verification :report]
        report (get-in (:request products) report-path)
        fabricated-request
        (assoc-in (:request products) report-path
                  (assoc report :checks {:fabricated-passed? true}))
        envelope-request
        (assoc-in
         (:request products)
         [:b47-context :authenticated-wrapper :gravity-core-boundary
          :authenticated-envelope :semantic-artifact-id]
         (get-in typed [:call-type-facts 0 :call-core-node-id]))
        digest-preimage-request
        (assoc-in
         (:request products)
         [:coordinator-verification :verification-digest-resolution
          :preimage :domain]
         :gravity/fabricated-passed-summary)
        request-candidates
        [[:different-valid-sh08-request substituted-request]
         [:fabricated-passed-b47-summary fabricated-request]
         [:mismatched-authenticated-envelope envelope-request]
         [:mismatched-verification-preimage digest-preimage-request]]]
    (doseq [[label candidate-request] request-candidates]
      (testing (name label)
        (let [candidate
              (if (= label :different-valid-sh08-request)
                substituted-products
                (assoc products :request candidate-request
                       :binding
                       (seal-binding candidate-request typed verification)))
              result (build candidate context)]
          (is (= :rejected (:status result)))
          (is (= "C8-VERIFY" (get-in result [:diagnostics 0 :rule])))
          (is (= :untrusted-sh08-function-binding
                 (get-in result [:diagnostics 0 :reason]))))))))
