(ns gravity.resolution.stage0-namespace
  "Hosted Stage0 namespace declaration parsing; no source or C5 admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:public-api {clause-args {:authenticates-inputs? false, :admission :none, :operation-inputs [], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :none, :arglists ([clause-map key]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, parse-clause {:authenticates-inputs? false, :admission :none, :operation-inputs [:fail! :source-span], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [fail! source-span]} source-path clause]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, parse-dependencies {:authenticates-inputs? false, :admission :none, :operation-inputs [:parse-dependency-entry], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [parse-dependency-entry]} source-path kind entries]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, parse-dependency-entry {:authenticates-inputs? false, :admission :none, :operation-inputs [:fail! :source-span :parse-options], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [fail! source-span parse-options]} source-path kind entry]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, parse-module {:authenticates-inputs? false, :admission :none, :operation-inputs [:require-ns :parse-clause :single-clause-value :clause-args :parse-dependencies :fail! :source-span :known-source-profiles :supported-profiles :supported-targets :bootstrap-target-supported?], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [require-ns parse-clause single-clause-value clause-args parse-dependencies fail! source-span known-source-profiles supported-profiles supported-targets bootstrap-target-supported?]} source-path forms]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, parse-options {:authenticates-inputs? false, :admission :none, :operation-inputs [:fail! :source-span], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [fail! source-span]} source-path entry option-items]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, require-ns {:authenticates-inputs? false, :admission :none, :operation-inputs [:ns-form? :fail! :source-span], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [ns-form? fail! source-span]} source-path forms]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}, single-clause-value {:authenticates-inputs? false, :admission :none, :operation-inputs [:fail! :source-span], :input-boundary :caller-owned-native-stage0-data, :grants-authority? false, :callback-selection :existing-entry-captured-values, :arglists ([{:keys [fail! source-span]} source-path clause-map key required?]), :proves-legality? false, :contract-boundary :stage0-native-namespace-declaration-parser}}, :source-reading? false, :macro-execution? false, :proof-authority? false, :tests [gravity.resolution.stage0-namespace-test], :dependency-direction {:requires [clojure.string], :consumed-by [gravity.module-analysis.clauses gravity.module-analysis.dependencies gravity.module-analysis.module], :forbids [gravity.bootstrap gravity.module-analysis gravity.resolution gravity.reader gravity.diagnostics gravity.package gravity.build gravity.cli gravity.release]}, :canonical-c5-admission? false, :canonical-l3-authority? false, :self-hosted? false, :governing-contracts [D0 D1 D2 D3 D6 D8 D9 L3 C1 C5 C15 P1 B1 BOOT1 BOOT8 TEST2], :unsupported [:source-authentication :callback-validation :canonical-c5-diagnostics :canonical-p1-b1-manifests :library-profile-sets :cross-file-imports-and-cycles :package-version-trust :lexical-binding-and-shadow-resolution :post-parse-alias-profile-effect-capability-legality :module-artifact-construction-hash-and-publication :typed-core-and-mir-admission :backend-runtime-provider-authority :release :migration :self-hosting :seed-retirement], :artifact-inputs [:ordered-native-forms-and-source-path :namespace-clause-map-and-required-flag :dependency-kind-entry-and-option-items :caller-selected-parser-failure-and-span-values :caller-selected-known-and-supported-profile-target-catalogs], :namespace gravity.resolution.stage0-namespace, :release-authority? false, :diagnostic-ownership {:decisions [L3-NS-MISSING L3-NS-CLAUSE L3-PROFILE-MULTIPLE L3-UNKNOWN-ALIAS L3-AMBIGUOUS-NAME L3-PRIVATE-IMPORT P1-PROFILE-UNSUPPORTED B1-TARGET-UNSUPPORTED], :upstream [L1-NS-SHAPE :reader-and-namespace-syntax], :construction-and-publication :supplied-failure-and-span-operations, :post-parse-admission :existing-module-analysis-boundaries}, :artifact-outputs [:first-namespace-form :clause-and-argument-vectors :dependency-option-map :ordered-dependency-records :native-stage0-module-declaration :original-owner-failure-return-or-exception], :contract-boundary :stage0-native-namespace-declaration-parser})

(defn require-ns
  [{:keys [ns-form? fail! source-span]} source-path forms]
  (let [first-form (first forms)]
    (when-not (ns-form? first-form)
      (fail! "L3-NS-MISSING"
             "Gravity source must start with an ns form"
             {:source-span (source-span source-path 0)
              :remediation "Add an ns form with :profile, :effects, and :capabilities clauses."}))
    first-form))

(defn parse-clause
  [{:keys [fail! source-span]} source-path clause]
  (when-not (and (seq? clause) (keyword? (first clause)))
    (fail! "L3-NS-CLAUSE"
           "namespace clause must be a list starting with a keyword"
           {:source-span (source-span source-path 0)
            :clause clause
            :remediation "Use clauses such as (:profile :hosted) or (:effects #{:io/write})."}))
  [(first clause) (vec (rest clause))])

(defn single-clause-value
  [{:keys [fail! source-span]} source-path clause-map key required?]
  (let [values (get clause-map key)]
    (cond
      (and required? (empty? values))
      (fail! "L3-NS-MISSING"
             (str "namespace is missing " key " clause")
             {:source-span (source-span source-path 0)
              :missing key
              :remediation "Declare the required namespace clause."})

      (> (count values) 1)
      (fail! "L3-PROFILE-MULTIPLE"
             (str "namespace declares " key " more than once")
             {:source-span (source-span source-path 0)
              :clause key
              :remediation "Keep one active implementation profile/target clause."})

      :else (first values))))

(defn clause-args
  [clause-map key]
  (vec (mapcat identity (get clause-map key))))

(defn parse-options
  [{:keys [fail! source-span]} source-path entry option-items]
  (when-not (even? (count option-items))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency options must be key/value pairs"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use dependency entries such as [gravity.io :as io :profile :hosted]."}))
  (loop [items option-items options {}]
    (if-let [[k v & more] (seq items)]
      (do
        (when-not (keyword? k)
          (fail! "L3-UNKNOWN-ALIAS"
                 "namespace dependency option keys must be keywords"
                 {:source-span (source-span source-path 0)
                  :entry entry
                  :option k
                  :remediation "Use keyword options such as :as, :refer, :profile, :effects, or :boundary."}))
        (recur more (assoc options k v)))
      options)))

(defn parse-dependency-entry
  [{:keys [fail! source-span parse-options]} source-path kind entry]
  (when-not (and (vector? entry) (symbol? (first entry)))
    (fail! "L3-UNKNOWN-ALIAS"
           "namespace dependency entry must start with a module symbol"
           {:source-span (source-span source-path 0)
            :entry entry
            :remediation "Use entries such as [gravity.io :as io]."}))
  (let [[module & option-items] entry
        options (parse-options source-path entry option-items)
        alias (:as options)
        refer (:refer options)
        effects (or (:effects options) #{})
        capabilities (or (:capabilities options) #{})]
    (when (and alias (not (symbol? alias)))
      (fail! "L3-UNKNOWN-ALIAS" "namespace alias must be a symbol"
             {:source-span (source-span source-path 0)
              :entry entry :alias alias
              :remediation "Use :as with a symbolic alias."}))
    (when (= :all refer)
      (fail! "L3-AMBIGUOUS-NAME"
             "wildcard imports are rejected for stable stage0 modules"
             {:source-span (source-span source-path 0)
              :entry entry
              :remediation "Import explicit public symbols instead of :refer :all."}))
    (when (some #(str/starts-with? (name %) "private-")
                (if (vector? refer) refer []))
      (fail! "L3-PRIVATE-IMPORT"
             "private definitions cannot be imported as public API"
             {:source-span (source-span source-path 0)
              :entry entry :refer refer
              :remediation "Export a public facade or remove the private import."}))
    {:kind kind :module module :alias alias
     :refer (cond (nil? refer) [] (vector? refer) refer :else [refer])
     :profile (:profile options) :boundary (:boundary options)
     :edge (:edge options) :facade (:facade options)
     :evidence (or (:evidence options) #{}) :artifact (:artifact options)
     :artifact-schema (:artifact-schema options) :runtime (:runtime options)
     :memory (:memory options) :generated? (boolean (:generated? options))
     :matrix-override (:matrix-override options)
     :producer-effects (or (:producer-effects options) #{})
     :producer-capabilities (or (:producer-capabilities options) #{})
     :safety-evidence (or (:safety-evidence options) #{})
     :provider (:provider options) :effects effects :capabilities capabilities
     :visibility (or (:visibility options) :public)}))

(defn parse-dependencies
  [{:keys [parse-dependency-entry]} source-path kind entries]
  (mapv #(parse-dependency-entry source-path kind %) entries))

(defn parse-module
  [{:keys [require-ns parse-clause single-clause-value clause-args
           parse-dependencies fail! source-span known-source-profiles
           supported-profiles supported-targets bootstrap-target-supported?]}
   source-path forms]
  (let [ns-form (require-ns source-path forms)
        module-name (second ns-form)
        clauses (map #(parse-clause source-path %) (drop 2 ns-form))
        clause-map (reduce (fn [acc [k v]] (update acc k (fnil conj []) v))
                           {} clauses)
        active-profile-values (get clause-map :profile)
        library-profile-values (get clause-map :profiles)
        profile (first (single-clause-value source-path clause-map :profile true))
        target (or (first (single-clause-value source-path clause-map :target false))
                   :jvm)
        effects (or (first (single-clause-value source-path clause-map :effects false))
                    #{})
        capabilities (or (first (single-clause-value source-path clause-map
                                                     :capabilities false))
                         #{})
        requires (parse-dependencies source-path :require
                                     (clause-args clause-map :requires))
        imports (parse-dependencies source-path :import
                                    (clause-args clause-map :imports))
        exports (or (first (single-clause-value source-path clause-map :exports false))
                    [])
        safety (or (first (single-clause-value source-path clause-map :safety false))
                   :safe)
        providers (or (first (single-clause-value source-path clause-map
                                                  :providers false))
                      [])
        metadata (or (first (single-clause-value source-path clause-map :metadata false))
                     {})
        docs (or (first (single-clause-value source-path clause-map :doc false)) nil)]
    (when (or (> (count active-profile-values) 1)
              (and (seq active-profile-values) (seq library-profile-values))
              (seq library-profile-values))
      (fail! "L3-PROFILE-MULTIPLE"
             "stage0 implementation namespaces must declare exactly one active profile"
             {:source-span (source-span source-path 0)
              :profile-clauses active-profile-values
              :profiles-clauses library-profile-values
              :remediation "Use one (:profile p) clause for stage0 implementation modules."}))
    (when-not (symbol? module-name)
      (fail! "L3-NS-MISSING" "namespace name must be a symbol"
             {:source-span (source-span source-path 0)
              :remediation "Use a symbolic namespace name, for example hello.main."}))
    (when-not (contains? known-source-profiles profile)
      (fail! "P1-PROFILE-UNSUPPORTED"
             "stage0 bootstrap does not know this source profile"
             {:source-span (source-span source-path 0)
              :profile profile :known known-source-profiles
              :supported supported-profiles
              :remediation "Use a known source profile such as :hosted, :core, or :kernel."}))
    (when-not (bootstrap-target-supported? target)
      (fail! "B1-TARGET-UNSUPPORTED"
             "stage0 bootstrap does not support this requested target"
             {:source-span (source-span source-path 0)
              :target target :supported supported-targets
              :remediation "Use a target enabled by the selected bootstrap backend."}))
    {:module module-name :source-path source-path :profile profile :target target
     :effects effects :capabilities capabilities :requires requires
     :imports imports :exports exports :safety safety :providers providers
     :metadata metadata :doc docs :forms (vec (rest forms))}))

(doseq [operation [#'require-ns #'parse-clause #'single-clause-value #'clause-args #'parse-options #'parse-dependency-entry #'parse-dependencies #'parse-module]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
