(ns gravity.c12-domain-ir
  "Hosted Stage0 C12 domain-IR registry and artifact projection.

  The leaf preserves the Clojure seed compatibility adapter over shared domain
  helpers. It is not domain-verifier, proof, plugin, backend, self-hosting, or
  release authority."
  (:require [gravity.c12-domain-ir.artifact :as artifact]
            [gravity.c12-domain-ir.operations :as operations]
            [gravity.c12-domain-ir.policy :as policy]))

(def ^:private ^:dynamic domain-ir-diagnostic-ids [])
(def ^:private ^:dynamic domain-ir-diagnostic-messages {})
(def ^:private ^:dynamic domain-ir-required-families [])
(def ^:private ^:dynamic domain-ir-registry-seed [])

(def ^:dynamic c12-domain-ir-governing-document
  "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md")

(defn- configuration []
  {:c12-domain-ir-governing-document c12-domain-ir-governing-document
   :domain-ir-diagnostic-ids domain-ir-diagnostic-ids
   :domain-ir-diagnostic-messages domain-ir-diagnostic-messages
   :domain-ir-required-families domain-ir-required-families
   :domain-ir-registry-seed domain-ir-registry-seed})

(defn c12-domain-ir-source-overrides [module]
  (operations/invoke :c12-domain-ir-source-overrides policy/source-overrides module))

(defn c12-domain-ir-validate-source-overrides! [source-path overrides]
  (operations/invoke :c12-domain-ir-validate-source-overrides!
                     (fn [path values]
                       (operations/invoke :domain-ir-validate-overrides!
                                          (policy/unsupported :domain-ir-validate-overrides!)
                                          path (policy/source-overrides-artifact values)))
                     source-path overrides))

(defn c12-domain-ir-diagnostic-catalog [source-path]
  (operations/invoke :c12-domain-ir-diagnostic-catalog
                     (fn [path]
                       (policy/diagnostic-catalog
                        (configuration) path
                        (fn [source index]
                          (operations/invoke :source-span
                                             (fn [p i] {:source p :form-index i})
                                             source index))))
                     source-path))

(defn compiler-c12-domain-ir-source-artifact [source-path source-text]
  (operations/invoke :compiler-c12-domain-ir-source-artifact
                     (fn [path text] (artifact/source-artifact (configuration) path text))
                     source-path source-text))

(defn compiler-c12-domain-ir-file-artifact [path]
  (operations/invoke :compiler-c12-domain-ir-file-artifact
                     (fn [source-path] (artifact/file-artifact (configuration) source-path))
                     path))

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (let [merged (merge (operations/current-operations) operations)]
    (binding [c12-domain-ir-governing-document
              (get merged :c12-domain-ir-governing-document c12-domain-ir-governing-document)
              domain-ir-diagnostic-ids
              (get merged :domain-ir-diagnostic-ids domain-ir-diagnostic-ids)
              domain-ir-diagnostic-messages
              (get merged :domain-ir-diagnostic-messages domain-ir-diagnostic-messages)
              domain-ir-required-families
              (get merged :domain-ir-required-families domain-ir-required-families)
              domain-ir-registry-seed
              (get merged :domain-ir-registry-seed domain-ir-registry-seed)]
      (operations/with-operations operations thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c12-engine-contract {:arglists '([])}
   'c12-domain-ir-governing-document {:kind :constant}
   'c12-domain-ir-source-overrides {:arglists '([module])}
   'c12-domain-ir-validate-source-overrides! {:arglists '([source-path overrides])}
   'c12-domain-ir-diagnostic-catalog {:arglists '([source-path])}
   'compiler-c12-domain-ir-source-artifact {:arglists '([source-path source-text])}
   'compiler-c12-domain-ir-file-artifact {:arglists '([path])}})

(defn c12-engine-contract [] (policy/engine-contract public-api))
