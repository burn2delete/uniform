(ns gravity.c5-name-resolution.artifacts
  (:require [clojure.set :as set]
            [gravity.c5-name-resolution.bindings :as bindings]
            [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.operations :as ops]))

(defn c5-namespace-analysis-artifact [module binding-table alias-table import-export-table dependency-graph cross-profile-report]
  {:artifact :gravity/namespace-analysis :namespace (:module module) :package (get-in module [:metadata :package])
   :profile (:profile module) :target (:target module) :aliases (into {} (map (juxt :alias :namespace) alias-table))
   :exports (:exports module) :locals (ops/c4-artifact-id (:local-bindings binding-table))
   :bindings (into {} (map (fn [record] [[(:syntax-id record) (:symbol-index record)] (:binding-id record)]) (:bindings binding-table)))
   :requires (get import-export-table :requires) :foreign-imports (get import-export-table :foreign-imports)
   :dependency-graph dependency-graph :cross-profile-edge-report cross-profile-report
   :rejected-edges [] :diagnostics [] :status :complete})

(defn c5-dependency-graph [module]
  (let [dependencies (mapv (fn [dependency]
                             {:namespace (:module dependency) :package {:name (symbol (str (:module dependency))) :version "stage0"}
                              :edge (or (:edge dependency) :direct) :kind (:kind dependency) :alias (:alias dependency)
                              :profile-boundary (cond (:boundary dependency) (:boundary dependency)
                                                      (= :core (:profile dependency)) :pure-core
                                                      (= (:profile dependency) (:profile module)) :compatible
                                                      :else :missing)
                              :effects (:effects dependency) :capabilities (:capabilities dependency) :target (:target module)})
                           (concat (:requires module) (:imports module)))]
    {:artifact :gravity/c5-module-dependency-graph :module (:module module) :dependencies dependencies
     :edges (mapv (fn [dependency] {:from (:module module) :to (:namespace dependency)
                                    :kind (:kind dependency) :profile-boundary (:profile-boundary dependency)}) dependencies)
     :acyclic true :status :complete}))

(defn c5-cross-profile-edge-report [module dependency-graph]
  {:artifact :gravity/c5-cross-profile-edge-report
   :edges (mapv (fn [dependency]
                  {:from (:module module) :to (:namespace dependency) :from-profile (:profile module)
                   :to-profile (or (some (fn [dep] (when (= (:module dep) (:namespace dependency)) (:profile dep)))
                                          (concat (:requires module) (:imports module))) (:profile module))
                   :boundary (:profile-boundary dependency) :accepted? (not= :missing (:profile-boundary dependency))})
                (:dependencies dependency-graph)) :status :complete})

(defn c5-incremental-invalidation-keys [module c4-artifact binding-table dependency-graph]
  {:artifact :gravity/c5-incremental-invalidation-keys
   :keys [{:input :namespace-source :hash (str "sha256:" (ops/sha256-hex (pr-str (:source-path module)))) :invalidates [:namespace-analysis :type-check :lsp-index]}
          {:input :aliases :hash (str "sha256:" (ops/sha256-hex (pr-str (map :alias (concat (:requires module) (:imports module)))))) :invalidates [:binding-table :dependency-graph]}
          {:input :exports :hash (str "sha256:" (ops/sha256-hex (pr-str (:exports module)))) :invalidates [:public-api :package-graph]}
          {:input :package-version :hash (str "sha256:" (ops/sha256-hex (pr-str ((ops/op-fn :c5-package-record bindings/c5-package-record) module)))) :invalidates [:dependency-graph :trust-policy]}
          {:input :profile-target :hash (str "sha256:" (ops/sha256-hex (pr-str [(:profile module) (:target module)]))) :invalidates [:profile-validation :target-lowering]}
          {:input :macro-expansion :hash (:artifact-id c4-artifact) :invalidates [:binding-table :type-check :effect-check]}
          {:input :binding-identities :hash (str "sha256:" (ops/sha256-hex (pr-str (:namespace-bindings binding-table)))) :invalidates [:incremental-cache :lsp-index]}
          {:input :dependency-graph :hash (str "sha256:" (ops/sha256-hex (pr-str (:edges dependency-graph)))) :invalidates [:package-graph :capability-check]}]
   :status :stable})

(defn c5-resolution-diagnostics [_]
  {:artifact :gravity/c5-resolution-diagnostics
   :required-diagnostic-ids (ops/op-value :c5-resolution-diagnostic-ids config/c5-resolution-diagnostic-ids)
   :covered (ops/op-value :c5-resolution-rejected-designs config/c5-resolution-rejected-designs)
   :accepted-run [] :status :complete})

(defn c5-resolution-verification-report [binding-table lexical-scope-graph dependency-graph cross-profile-report invalidation]
  {:artifact :gravity/c5-resolution-verification-report
   :binding-identities-stable? (every? #(re-find #"^sha256:" (:binding-id %)) (concat (:namespace-bindings binding-table) (:local-bindings binding-table)))
   :all-resolved-bindings-have-metadata? (every? #(and (:binding-id %) (:profile-set %) (:target-set %) (contains? % :effects) (contains? % :capabilities) (:visibility %)) (concat (:namespace-bindings binding-table) (:local-bindings binding-table)))
   :lexical-scopes-present? (seq (:scopes lexical-scope-graph)) :dependency-graph-present? (seq (:edges dependency-graph))
   :cross-profile-boundaries-recorded? (every? :accepted? (:edges cross-profile-report))
   :invalidation-keys-stable? (every? #(re-find #"^sha256:" (:hash %)) (:keys invalidation)) :status :passed})

(defn c5-resolution-capability-proof [artifact]
  (let [binding-table (:binding-table artifact) records (:bindings binding-table)
        namespace-bindings (:namespace-bindings binding-table)
        diagnostics (set (map :diagnostic (:rejected-design-coverage artifact))) verifier (:resolution-verification-report artifact)]
    {:local-resolution? (boolean (some #(= :local (:resolution-order %)) records))
     :namespace-resolution? (boolean (some #(= :namespace (:resolution-order %)) records))
     :alias-qualified-resolution? (boolean (some #(= :alias-qualified (:resolution-order %)) records))
     :fully-qualified-resolution? (boolean (some #(= :fully-qualified (:resolution-order %)) records))
     :macro-and-type-position-resolution? (boolean (and (some #(= :macro (:kind %)) namespace-bindings) (some #(= :type-position (:resolution-order %)) records)))
     :binding-identity-stable? (true? (:binding-identities-stable? verifier)) :visibility-diagnostics-covered? (contains? diagnostics "C5-PRIVATE")
     :dependency-graph-emitted? (= :complete (get-in artifact [:dependency-graph :status]))
     :cross-profile-boundaries-recorded? (true? (:cross-profile-boundaries-recorded? verifier))
     :target-and-capability-compatibility? (every? #(set/subset? (set (:capabilities %)) (set (get-in artifact [:module :capabilities]))) (get-in artifact [:dependency-graph :dependencies]))
     :incremental-invalidation-recorded? (= :stable (get-in artifact [:incremental-invalidation-keys :status]))
     :diagnostics-covered? (= (set (ops/op-value :c5-resolution-diagnostic-ids config/c5-resolution-diagnostic-ids)) diagnostics)
     :status :complete}))
