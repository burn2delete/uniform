(ns gravity.c6-core-lowering.config
  "Static forms, profiles, targets, diagnostics, and C6 contract data."
  (:require [clojure.set :as set]
            [gravity.c6-core-lowering.context]))

(def core-forms
  '#{quote if do let fn loop recur def var set! try throw match})

(def lowering-gap-forms
  '#{defn when -> cond case with-open with-region defmacro defschema
     defworkflow defagent ui query ai-form})

(def known-source-profiles
  #{:core :hardware :firmware :kernel :native :hosted :distributed :ai
    :meta :gpu :formal})

(def supported-targets #{:jvm})

(def c6-lowering-diagnostic-ids
  ["C6-LOWERING-GAP"
   "C6-CORE-SHAPE"
   "C6-EVAL-ORDER"
   "C6-ORIGIN"
   "C6-EFFECT-DROP"
   "C6-UNSAFE-DROP"
   "C6-DOMAIN-BOUNDARY"
   "C6-VERIFY"])

(def c6-lowering-governing-document
  "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md")

(def c6-lowering-rejected-designs
  [{:diagnostic "C6-LOWERING-GAP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-lowering-gap.gravity"
    :rejected-design :surface-form-bypasses-core}
   {:diagnostic "C6-CORE-SHAPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-core-shape.gravity"
    :rejected-design :malformed-core-node}
   {:diagnostic "C6-EVAL-ORDER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-eval-order.gravity"
    :rejected-design :evaluation-order-lost}
   {:diagnostic "C6-ORIGIN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-origin.gravity"
    :rejected-design :introduced-form-without-origin}
   {:diagnostic "C6-EFFECT-DROP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-effect-drop.gravity"
    :rejected-design :effect-or-capability-erased}
   {:diagnostic "C6-UNSAFE-DROP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-unsafe-drop.gravity"
    :rejected-design :unsafe-metadata-erased}
   {:diagnostic "C6-DOMAIN-BOUNDARY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-domain-boundary.gravity"
    :rejected-design :malformed-domain-boundary}
   {:diagnostic "C6-VERIFY"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c6-verify.gravity"
    :rejected-design :core-verifier-failure}])

(def c6-lowering-override-diagnostics
  {:gap "C6-LOWERING-GAP"
   :core-shape "C6-CORE-SHAPE"
   :eval-order "C6-EVAL-ORDER"
   :origin "C6-ORIGIN"
   :effect-drop "C6-EFFECT-DROP"
   :unsafe-drop "C6-UNSAFE-DROP"
   :domain-boundary "C6-DOMAIN-BOUNDARY"
   :verify "C6-VERIFY"})

(def c6-domain-boundary-operators
  '#{defschema defworkflow defagent ui query ai-form})

(def c6-core-node-forms
  (set/union core-forms #{:call :literal :symbol :declared-primitive}))

(def namespace-contract
  {:namespace 'gravity.c6-core-lowering
   :contract-boundary :hosted-stage0-c6-core-lowering-engine
   :public-api :bootstrap-compatible-c6-vars
   :leaf-only-api ['c6-lowering-artifact]
   :owns [:hosted-stage0-c6-lowering-algorithm
          :hosted-stage0-c6-artifact-projection]
   :compatibility-only? true
   :canonical-sh07-authority? false
   :authority-boundary :gravity.bootstrap-sh06-adapter
   :dependency-direction {:forbids ['gravity.bootstrap 'gravity.diagnostics]
                          :requires ['clojure.set 'gravity.digest]}
   :does-not-own [:source-acquisition :sh06-authentication :canonical-sh07
                  :type-checking :effect-checking :ownership-checking
                  :safety-analysis :mir-construction :target-lowering
                  :proof-authority :equivalence :self-hosting :release]
   :operation-interposition {:partial-overrides? true
                             :unknown-keys-rejected? true
                             :accepted-keys
                             gravity.c6-core-lowering.context/operation-keys
                             :bootstrap-wrapper-arities? true}})
