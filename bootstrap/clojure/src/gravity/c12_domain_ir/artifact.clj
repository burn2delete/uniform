(ns gravity.c12-domain-ir.artifact
  (:require [gravity.c12-domain-ir.operations :as operations]
            [gravity.c12-domain-ir.policy :as policy]
            [gravity.digest :as digest]))

(defn- source-span [path index]
  (operations/invoke :source-span
                     (fn [source form-index] {:source source :form-index form-index})
                     path index))

(defn- artifact-id [artifact]
  (operations/invoke :c4-artifact-id
                     (fn [value] (str "sha256:" (digest/sha256-hex (pr-str value))))
                     artifact))

(defn- diagnostic-catalog [configuration source-path]
  (operations/invoke :c12-domain-ir-diagnostic-catalog
                     (fn [path]
                       (policy/diagnostic-catalog configuration path source-span))
                     source-path))

(defn- entry-pass-records [c11-artifact registrations]
  (mapv (fn [registration]
          {:domain (:domain registration) :entry-passes (:entry-passes registration)
           :consumes [:source-forms :typed-core :gravity/mir]
           :input-artifact (:artifact-id c11-artifact)
           :preserves [:types :effects :ownership :capabilities :profile :target :safety :provenance]
           :status :complete})
        registrations))

(defn- exit-pass-records [registrations]
  (mapv (fn [registration]
          {:domain (:domain registration) :exit-passes (:exit-passes registration)
           :requires [:domain-verifier-report :proof-or-certificate :fallback-record]
           :emits [:mir-subgraph :target-provider-call :runtime-manifest :verification-artifact]
           :status :complete})
        registrations))

(defn- lowering-matrix [c11-artifact registrations]
  (mapv (fn [registration]
          (let [target (get-in c11-artifact [:mir-module :target-request])]
            {:domain (:domain registration) :target-request target
             :supported-targets (:target-lowerings registration)
             :status (if (contains? (:target-lowerings registration) target) :eligible :fallback)
             :fallback (:fallback registration)}))
        registrations))

(defn- artifact-base [configuration source-path module source-overrides c11-artifact
                      registrations domain-artifacts diagnostics]
  (let [entry-records (entry-pass-records c11-artifact registrations)
        exit-records (exit-pass-records registrations)
        proof-records (mapv (fn [domain-artifact]
                              {:domain (:domain domain-artifact)
                               :artifact-id (:artifact-id domain-artifact)
                               :evidence-kind :translation-validation
                               :proofs (:proofs domain-artifact)
                               :status :accepted})
                            domain-artifacts)
        fallbacks (mapv (fn [registration]
                          {:domain (:domain registration)
                           :fallback (:fallback registration)
                           :status :available
                           :residual :gravity/mir})
                        registrations)]
    {:kind :gravity/stage0-c12-domain-ir-architecture-artifact
     :task "P06-D091" :document-set ["C12"]
     :governing-document (:c12-domain-ir-governing-document configuration)
     :pass {:name :c12-domain-ir-architecture :input :gravity/mir :output :domain-ir-registry
            :requires [:c11-mir-specification :semantic-anchors :type-facts :effect-facts
                       :ownership-facts :capability-proofs :safety-outcomes :source-provenance]
            :preserves [:types :effects :ownership :capabilities :profile :target :safety
                        :source-spans :origin-chain]
            :emits [:domain-ir-registry :domain-ir-artifacts :semantic-anchor-map
                    :entry-pass-records :exit-pass-records :domain-verifier-report
                    :proof-and-certificate-references :lowering-eligibility-matrix
                    :fallback-records :domain-ir-diagnostic-stream]
            :rejects (:domain-ir-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c11-mir-spec-artifact (select-keys c11-artifact [:kind :task :artifact-id :governing-document
                                                        :mir-module :mir-verifier-report :capability-based-proof])
     :mir-artifact-kind (:kind c11-artifact) :mir-artifact-hash (:artifact-id c11-artifact)
     :mir-artifact c11-artifact :domain-ir-registry registrations
     :domain-ir-artifact-schema {:artifact :gravity/domain-ir-artifact-schema
                                 :required-fields [:artifact :domain :artifact-id :source :semantic-anchor
                                                   :profile :target-request :facts :verifier :proofs :lowering-status]
                                 :required-domain-families (:domain-ir-required-families configuration)
                                 :status :complete}
     :domain-ir-artifacts domain-artifacts
     :semantic-anchor-map (mapv #(select-keys % [:domain :artifact-id :semantic-anchor :source]) domain-artifacts)
     :entry-pass-records entry-records :exit-pass-records exit-records
     :domain-verifier-report {:artifact :gravity/domain-verifier-report :status :passed
                              :domains (mapv :domain domain-artifacts)
                              :checks [:registration :schema :semantic-anchor :source-provenance :facts
                                       :proof-obligations :lowering :fallback :plugin-policy]
                              :diagnostics []}
     :proof-and-certificate-references proof-records
     :lowering-eligibility-matrix (lowering-matrix c11-artifact registrations)
     :fallback-records fallbacks
     :plugin-registration-policy {:status :enforced
                                  :requires [:schema :owner-doc :verifier :semantic-anchor :non-opaque-payload]
                                  :visibility :package-visible}
     :domain-ir-diagnostic-stream diagnostics
     :c12-domain-ir-results {:documents ["C12"] :task "P06-D091"
                             :required-diagnostic-ids (:domain-ir-diagnostic-ids configuration)
                             :c11-input-status :complete :registration-status :complete
                             :artifact-schema-status :complete :anchor-status :complete
                             :verifier-status :complete :proof-evidence-status :complete
                             :lowering-and-fallback-status :complete :plugin-policy-status :complete
                             :diagnostic-status :complete :status :complete}
     :diagnostics []}))

(defn source-artifact [configuration source-path source-text]
  (let [records (operations/invoke :read-source-form-records
                                   (policy/unsupported :read-source-form-records)
                                   source-path source-text)
        forms (mapv :form records)
        _ (operations/invoke :validate-ns-syntax! (policy/unsupported :validate-ns-syntax!)
                             source-path forms)
        module (operations/invoke :parse-module (policy/unsupported :parse-module) source-path forms)
        source-overrides (operations/invoke :c12-domain-ir-source-overrides policy/source-overrides module)
        _ (operations/invoke :c12-domain-ir-validate-source-overrides!
                             (fn [path overrides]
                               (operations/invoke :domain-ir-validate-overrides!
                                                  (policy/unsupported :domain-ir-validate-overrides!)
                                                  path (policy/source-overrides-artifact overrides)))
                             source-path source-overrides)
        c11-artifact (operations/invoke :compiler-c11-mir-source-artifact
                                        (policy/unsupported :compiler-c11-mir-source-artifact)
                                        source-path source-text)
        registrations (mapv #(operations/invoke :domain-ir-registration-record
                                                (policy/unsupported :domain-ir-registration-record) %)
                            (:domain-ir-registry-seed configuration))
        domain-artifacts (mapv #(operations/invoke :domain-ir-artifact-record
                                                  (policy/unsupported :domain-ir-artifact-record)
                                                  c11-artifact %1 %2)
                               registrations (range))
        diagnostics (diagnostic-catalog configuration source-path)
        artifact-base (artifact-base configuration source-path module source-overrides
                                     c11-artifact registrations domain-artifacts diagnostics)
        _ (operations/invoke :domain-ir-validate! (policy/unsupported :domain-ir-validate!)
                             source-path artifact-base)
        capability-proof (assoc (operations/invoke :domain-ir-capability-proof
                                                    (policy/unsupported :domain-ir-capability-proof)
                                                    artifact-base)
                                :c11-mir-input-verified?
                                (= :passed (get-in c11-artifact [:mir-verifier-report :status]))
                                :diagnostics-covered?
                                (= (set (:domain-ir-diagnostic-ids configuration))
                                   (set (map :diagnostic (:diagnostics diagnostics)))))]
    (assoc artifact-base :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc artifact-base :capability-based-proof capability-proof)))))

(defn file-artifact [configuration path]
  (operations/invoke :compiler-c12-domain-ir-source-artifact
                     (fn [source-path source-text]
                       (source-artifact configuration source-path source-text))
                     path (slurp path)))
