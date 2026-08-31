(ns gravity.module-analysis.boundaries
  (:require [clojure.string :as str]))

(defn profile-direct-import-allowed?
  [profile-direct-imports consumer-profile producer-profile]
  (contains? (get profile-direct-imports consumer-profile #{}) producer-profile))

(defn assert-unique-aliases!
  [{:keys [fail! source-span]} source-path dependencies]
  (let [aliases (keep :alias dependencies)
        duplicate (first (for [[alias n] (frequencies aliases) :when (> n 1)]
                           alias))]
    (when duplicate
      (fail! "L3-AMBIGUOUS-NAME"
             "namespace alias resolves to multiple dependencies"
             {:source-span (source-span source-path 0)
              :alias duplicate
              :remediation "Use one unique alias per required or imported module."}))))

(defn assert-referred-names-unambiguous!
  [{:keys [fail! source-span]} source-path dependencies]
  (let [referred (mapcat :refer dependencies)
        duplicate (first (for [[sym n] (frequencies referred) :when (> n 1)]
                           sym))]
    (when duplicate
      (fail! "L3-AMBIGUOUS-NAME"
             "unqualified imported name resolves to multiple dependencies"
             {:source-span (source-span source-path 0)
              :symbol duplicate
              :remediation "Remove one refer or qualify the symbol through an alias."}))))

(defn assert-qualified-symbols-resolve!
  [{:keys [collect-code-symbols fail!]} source-path forms module dependencies]
  (let [aliases (set (map str (keep :alias dependencies)))
        allowed-qualified (conj aliases (str (:module module)))
        unknown (first (for [sym (mapcat collect-code-symbols forms)
                             :let [ns-part (namespace sym)]
                             :when (and ns-part
                                        (not (contains? allowed-qualified ns-part))
                                        (not (str/includes? ns-part ".")))]
                         sym))]
    (when unknown
      (fail! "L3-UNKNOWN-ALIAS"
             "qualified symbol uses an unknown namespace alias"
             {:source-span {:source source-path}
              :symbol unknown
              :alias (symbol (namespace unknown))
              :remediation "Declare the alias in :requires or :imports, or use a fully qualified namespace."}))))

(defn assert-profile-boundaries!
  [{:keys [profile-direct-import-allowed? fail! source-span]}
   source-path module dependencies]
  (doseq [dependency dependencies]
    (let [dep-profile (:profile dependency)
          module-profile (:profile module)]
      (when (and dep-profile
                 (not (profile-direct-import-allowed? module-profile dep-profile))
                 (nil? (:boundary dependency)))
        (fail! "L3-CROSS-PROFILE"
               "cross-profile import requires an explicit boundary"
               {:source-span (source-span source-path 0)
                :module (:module dependency)
                :profile module-profile
                :dependency-profile dep-profile
                :remediation "Use a :core API, profile-safe facade, typed schema/artifact boundary, or explicit interop boundary."})))))

(defn assert-namespace-effect-and-capability!
  [{:keys [required-capabilities-for-effects fail!]}
   source-path module inferred-effects]
  (let [declared-effects (:effects module)
        widened (first (remove declared-effects inferred-effects))
        required-capabilities (required-capabilities-for-effects inferred-effects)
        missing-capability (first (remove (:capabilities module)
                                          required-capabilities))]
    (when widened
      (fail! "L3-EFFECT-WIDEN"
             "inferred namespace effects exceed declared effect allowance"
             {:source-span {:source source-path}
              :effect widened :declared-effects declared-effects
              :remediation "Declare the effect at namespace level or remove the effectful form."}))
    (when missing-capability
      (fail! "L3-CAPABILITY-MISSING"
             "namespace requires a capability not declared by the namespace"
             {:source-span {:source source-path}
              :required-capability missing-capability
              :declared-capabilities (:capabilities module)
              :remediation "Declare the required capability or remove the capability-using form."}))))
