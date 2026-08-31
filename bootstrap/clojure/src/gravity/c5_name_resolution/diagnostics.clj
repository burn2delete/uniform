(ns gravity.c5-name-resolution.diagnostics
  (:require [gravity.c5-name-resolution.artifacts :as artifacts]
            [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.operations :as ops]))

(defn c5-resolution-fail! [id source-path subject extra]
  (ops/fail! id
             ((ops/op-fn :c5-resolution-message config/c5-resolution-message) id)
             (merge {:source-span (or (:source-span subject) (:span subject) (ops/source-span source-path 0))
                     :diagnostic-family :c5-name-resolution :stage :name-resolution :document-id "C5"
                     :expected-document (ops/op-value :c5-resolution-governing-document config/c5-resolution-governing-document)
                     :symbol (:symbol subject) :syntax-id (:syntax-id subject) :namespace (:namespace subject)
                     :active-profile (:profile subject) :target (:target subject)
                     :candidate-bindings (:candidate-bindings subject) :dependency-edge (:dependency-edge subject)
                     :capabilities (:capabilities subject)
                     :remediation "Resolve names through lexical, namespace, alias, package, foreign, core, or target-intrinsic records with explicit profile, target, effect, capability, visibility, and dependency metadata."}
                    extra)))

(defn c5-resolution-validate-overrides! [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get (ops/op-value :c5-resolution-override-diagnostics config/c5-resolution-override-diagnostics) fail-kind)]
      ((ops/op-fn :c5-resolution-fail! c5-resolution-fail!) id source-path
       {:source-span (ops/source-span source-path 0) :symbol (symbol (str "fixture/" (name fail-kind)))
        :syntax-id "fixture-override" :namespace (:module module) :profile (:profile module)
        :target (:target module) :capabilities (:capabilities module)}
       {:missing-fields [fail-kind]}))))

(defn c5-resolution-validate! [source-path artifact]
  (let [proof ((ops/op-fn :c5-resolution-capability-proof artifacts/c5-resolution-capability-proof) artifact)]
    (doseq [[field id] [[:local-resolution? "C5-UNRESOLVED"]
                        [:namespace-resolution? "C5-UNRESOLVED"]
                        [:alias-qualified-resolution? "C5-ALIAS"]
                        [:fully-qualified-resolution? "C5-UNRESOLVED"]
                        [:macro-and-type-position-resolution? "C5-UNRESOLVED"]
                        [:binding-identity-stable? "C5-UNRESOLVED"]
                        [:visibility-diagnostics-covered? "C5-PRIVATE"]
                        [:dependency-graph-emitted? "C5-CYCLE"]
                        [:cross-profile-boundaries-recorded? "C5-CROSS-PROFILE"]
                        [:target-and-capability-compatibility? "C5-CAPABILITY"]
                        [:incremental-invalidation-recorded? "C5-UNRESOLVED"]
                        [:diagnostics-covered? "C5-UNRESOLVED"]]]
      (when-not (get proof field)
        ((ops/op-fn :c5-resolution-fail! c5-resolution-fail!) id source-path
         {:stage :name-resolution} {:missing-fields [field]}))))
  :complete)
