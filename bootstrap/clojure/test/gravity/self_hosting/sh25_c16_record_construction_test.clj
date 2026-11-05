(ns gravity.self-hosting.sh25-c16-record-construction-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Development execution only: no caller adoption or validation authority.
(def ^:private source-path
  (.getCanonicalPath
   (io/file "language/compiler/src/gravity/compiler/c16_incremental_compilation_design.gravity")))

(def ^:private source-text (delay (slurp source-path)))

(def ^:private plan
  (delay
    (let [emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path @source-text))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-c16-record-construction-development
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- source-contract [contract-name]
  ;; Read constant contract data for the oracle; constructors execute above.
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. @source-text))]
    (loop []
      (let [form (edn/read {:eof ::eof} reader)]
        (cond
          (= ::eof form)
          (throw (ex-info "C16 contract not found" {:contract contract-name}))
          (and (seq? form) (= 'def (first form))
               (= contract-name (second form)))
          (nth form 2)
          :else (recur))))))

(deftest cache-key-executes-supplied-fields-and-contract
  (let [arguments
        [:type-check "sha256:source" "sha256:reader" "sha256:syntax"
         "sha256:macro" "sha256:namespace" "sha256:profile"
         "sha256:target" "sha256:compiler" "sha256:pass"
         {:graph "sha256:dependencies" :facts #{:source-spans :proofs}}
         {:replay "sha256:replay" :provenance {:source "build.gravity"}}
         #{:build/read-file} [:core :types]]
        expected
        (merge
         {:artifact :gravity/cache-key
          :contract (source-contract 'c16-cache-key-contract)}
         (zipmap [:stage :source :reader :syntax :macro-expansion :namespace
                  :profile :target :compiler :pass-contract :dependencies
                  :build-effects :capabilities :language-facets]
                 arguments))]
    (is (= expected (invoke 'build-c16-cache-key arguments)))))

;; C16's source records have their own schema. These are constructor products,
;; not substitutes for the hosted projection/artifact records or their checks.
(def ^:private constructor-cases
  [{:function 'build-c16-cache-key
    :artifact :gravity/cache-key
    :contract 'c16-cache-key-contract
    :contract-artifact :gravity/c16-cache-key-contract
    :fields [:stage :source :reader :syntax :macro-expansion :namespace
             :profile :target :compiler :pass-contract :dependencies
             :build-effects :capabilities :language-facets]
    :arguments
    [:type-check "sha256:source-a" "sha256:reader-b" "sha256:syntax-c"
     "sha256:expansion-d" "sha256:namespace-e" "sha256:profile-f"
     "sha256:target-g" "sha256:compiler-h" "sha256:pass-i"
     {:graph "sha256:graph-j" :edges [{:from :syntax :to :typed-core}]}
     {:replay "sha256:replay-k" :provenance {:source "build.gravity"}}
     #{:build/read-file} [:core :types]]}
   {:function 'build-c16-cache-entry
    :artifact :gravity/cache-entry
    :contract 'c16-cache-entry-contract
    :contract-artifact :gravity/c16-cache-entry-manifest-contract
    :fields [:cache-key :artifact-id :producer :inputs :preserved-facts
             :invalidated-by :diagnostics :trust :revalidation :provenance]
    :arguments
    ["sha256:entry-key" "sha256:entry-artifact"
     {:stage :effect-check :pass-id :effects :pass-version "v2"
      :compiler-id "compiler-a" :pass-contract "sha256:effect-contract"}
     ["sha256:input-a" {:source "src/a.gravity" :syntax "sha256:syntax-a"}]
     {:safety {:outcome :runtime-checked :checks [:bounds]}
      :proofs [{:claim :in-range :assumptions #{:typed-input}}]
      :source-spans [{:source "src/a.gravity" :line 12}]}
     #{:source-change :certificate-trust-policy-change}
     [{:rule "C16-PROOF" :origin-chain [{:source "macro.gravity" :line 3}]}]
     :local-build :required-before-release
     {:source "sha256:entry-source" :compiler "sha256:entry-compiler"
      :profile :meta :target :jvm
      :package-lock {:packages ["compiler/core"]}
      :provider-manifests [{:provider :proof :policy "sha256:policy-a"}]
      :build-replay {:inputs ["sha256:build-input"]}
      :dependency-graph {:nodes [:source :typed-core]}
      :created-by-stage :effect-check :verified-by-stage :pending}]}
   {:function 'build-c16-invalidation-trace
    :artifact :gravity/invalidation-trace
    :contract 'c16-invalidation-trace-contract
    :contract-artifact :gravity/c16-invalidation-trace-contract
    :fields [:trace-id :cause :invalidating-input :affected-nodes
             :downstream-stages :proofs-invalidated :diagnostics-invalidated
             :profile :target :remediation]
    :arguments
    ["trace-a" :certificate-trust-policy-change
     {:policy {:before "sha256:old-policy" :after "sha256:new-policy"}
      :source-span {:source "policy.gravity" :line 7}}
     [{:node :proof-a :field-path [:assumptions :provider]}]
     [:safety-check :target-lowering]
     [{:proof-id "proof-a" :claim :in-bounds :runtime-checks-restored [:bounds]}]
     [{:diagnostic-id "diag-a" :origin-chain ["source-a" "expansion-b"]}]
     :native :llvm-x86_64-linux {:action :revalidate :stage :safety-check}]}
   {:function 'build-c16-revalidation-report
    :artifact :gravity/revalidation-report
    :contract 'c16-revalidation-contract
    :contract-artifact :gravity/c16-revalidation-contract
    :fields [:artifact-id :cache-key :stage :checks-run :checks-missing
             :outcome :speculation :blocking-diagnostics :profile :target]
    :arguments
    ["sha256:revalidation-artifact" "sha256:revalidation-key" :mir
     [:cache-key-equality :producer-pass-contract]
     [:proof-certificate-freshness :build-effect-replay-present]
     :speculative
     {:source :interactive-build :revalidation :partial
      :required-full-revalidation [:proof-certificate-freshness]}
     [{:rule "C16-SPECULATIVE" :facts {:proof :unknown}
       :source-span {:source "revalidate.gravity" :line 9}}]
     :hosted :jvm]}
   {:function 'build-c16-artifact-reuse-report
    :artifact :gravity/artifact-reuse-report
    :contract 'c16-incremental-compilation-contract
    :contract-artifact :gravity/c16-incremental-compilation-contract
    :fields [:artifact-id :cache-entry :reuse-status :revalidation-report
             :invalidation-trace :diagnostics :publishable]
    :arguments
    ["sha256:reuse-artifact"
     {:artifact :gravity/cache-entry :trust :unknown
      :revalidation :required-before-use
      :provenance {:source "sha256:reuse-source" :build-replay {:inputs []}}}
     :speculative
     {:outcome :speculative :checks-run [:cache-key-equality]
      :checks-missing [:proof-certificate-freshness]}
     {:trace-id "reuse-trace" :proofs-invalidated ["proof-b"]}
     [{:rule "C16-STALE" :facts {:changed-field [:provider :policy]}}]
     false]}])

(defn- expected-product [{:keys [artifact contract fields arguments]}]
  (merge {:artifact artifact :contract (source-contract contract)}
         (zipmap fields arguments)))

(deftest all-five-constructors-execute-complete-records
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan)))
  (doseq [{:keys [function contract-artifact arguments] :as case}
          constructor-cases]
    (testing (str function)
      (let [product (invoke function arguments)]
        (is (= (expected-product case) product))
        (is (map? (:contract product)))
        (is (= contract-artifact (get-in product [:contract :artifact])))))))

(deftest every-supplied-parameter-affects-only-its-record-field
  (doseq [{:keys [function fields arguments] :as case} constructor-cases
          [index field] (map-indexed vector fields)]
    (testing (str function " " field)
      (let [replacement (if (= :publishable field) true [::changed field])
            changed (assoc arguments index replacement)
            product (invoke function changed)]
        (is (= (assoc (expected-product case) field replacement) product))
        (is (not= (expected-product case) product))))))

(deftest quoted-parameter-templates-and-damaged-products-do-not-match
  (doseq [{:keys [function artifact contract fields arguments] :as case}
          constructor-cases]
    (testing (str function)
      (let [expected (expected-product case)
            product (invoke function arguments)
            template
            (merge {:artifact artifact :contract contract}
                   (zipmap fields
                           (map #(if (= :blocking-diagnostics %)
                                   'diagnostics
                                   (symbol (name %)))
                                fields)))]
        (is (not= template product))
        (is (not= (assoc expected :contract contract) product))
        (doseq [field (keys expected)]
          (testing (str field)
            (is (not= expected (dissoc product field)))
            (is (not= expected (assoc product field ::altered)))))))))

(deftest all-five-constructors-reject-wrong-arity-during-execution
  (doseq [{:keys [function arguments]} constructor-cases
          wrong-arguments [(pop arguments) (conj arguments :extra)]]
    (testing (str function " with " (count wrong-arguments) " arguments")
      ;; Real runtime entry must reject; a host ArityException is not evidence.
      (let [data (try
                   (invoke function wrong-arguments)
                   nil
                   (catch clojure.lang.ExceptionInfo exception
                     (ex-data exception)))]
        (is (= "L2-FUNCTION-ARITY" (:id data)))
        (is (= function (:function data)))
        (is (= (count arguments) (:expected-arity data)))
        (is (= (count wrong-arguments) (:actual-arity data)))
        (is (= source-path (get-in data [:source-span :source])))))))

(deftest speculative-and-incomplete-reports-gain-no-validation-authority
  (doseq [[checks-run checks-missing outcome speculation publishable]
          [[[:cache-key-equality] [:proof-certificate-freshness]
            :speculative {:source :repl-inspection :revalidation :partial} false]
           [[] [:cache-key-equality :producer-pass-contract] nil nil nil]]]
    (let [report-arguments
          ["sha256:partial-artifact" "sha256:partial-key" :type-check
           checks-run checks-missing outcome speculation [] :meta :jvm]
          report (invoke 'build-c16-revalidation-report report-arguments)
          entry (invoke 'build-c16-cache-entry
                        ["sha256:partial-key" "sha256:partial-artifact"
                         {} [] {} #{} [] :unknown :required-before-release
                         {:source "sha256:partial-source" :verified-by-stage nil}])
          reuse-arguments
          ["sha256:partial-artifact" entry :speculative report {} [] publishable]
          reuse (invoke 'build-c16-artifact-reuse-report reuse-arguments)]
      (is (= (expected-product (assoc (nth constructor-cases 3)
                                     :arguments report-arguments))
             report))
      (is (= (expected-product (assoc (nth constructor-cases 4)
                                     :arguments reuse-arguments))
             reuse))
      (is (= :full-revalidation-before-publish
             (get-in reuse [:contract :release-rule])))
      (is (= :speculative-artifacts-cannot-publish
             (get-in report [:contract :release-rule])))
      (is (= :unknown (get-in reuse [:cache-entry :trust])))
      (is (= :required-before-release
             (get-in reuse [:cache-entry :revalidation])))
      (is (nil? (get-in reuse [:cache-entry :provenance :verified-by-stage])))
      (is (= publishable (:publishable reuse)))
      (doseq [product [entry report reuse]
              status [:status :verified? :verification :release-status]]
        (is (not (contains? product status)))))))
