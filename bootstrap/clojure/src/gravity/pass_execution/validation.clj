(ns gravity.pass-execution.validation
  "Closed-schema validation shared by execution receipts and evidence DAGs."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.diagnostics :as diagnostics]))

(defn exact-map!
  [value fields id label]
  ;; Bound operation maps and returned records before inspecting keys.
  (canonical/preflight-canonical! value)
  (when-not (map? value)
    (diagnostics/fail! id (str label " must be a map") {:field label}))
  (reduce-kv (fn [_ key _]
               (when-not (contains? fields key)
                 (diagnostics/fail! id (str label " has an unknown field")
                                    {:field label :unknown-field key}))
               nil)
             nil value)
  (let [missing (reduce (fn [result field]
                          (if (contains? value field)
                            result
                            (conj result field)))
                        [] fields)]
    (when (seq missing)
      (diagnostics/fail! id (str label " is missing required fields")
                         {:field label :missing-fields missing})))
  value)

(defn sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches config/sha256-pattern value))))

(defn require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (diagnostics/fail! "C16-KEY"
                       "semantic identity must be lowercase SHA-256"
                       {:field field :observed value}))
  value)

(defn distinct-vector!
  [field value predicate]
  (when-not (vector? value)
    (diagnostics/fail! "C16-ENTRY" "receipt identity field must be a vector"
                       {:field field :observed value}))
  (when (> (count value) config/maximum-nodes)
    (diagnostics/fail! "C16-KEY"
                       "receipt vector exceeds its cardinality bound"
                       {:field field :maximum-cardinality config/maximum-nodes}))
  (when-not (and (every? predicate value)
                 (= (count value) (count (distinct value))))
    (diagnostics/fail! "C16-ENTRY"
                       "receipt vector is malformed or contains duplicates"
                       {:field field :observed value}))
  value)

(defn sorted-sha-vector!
  [field value]
  (distinct-vector! field value sha256-id?)
  (when-not (= value (vec (sort value)))
    (diagnostics/fail! "C16-KEY"
                       "semantic identity vectors must use lexical SHA-256 order"
                       {:field field :observed value
                        :remediation "sort semantic identity vectors lexically"}))
  value)

(defn keyword-set!
  [field value]
  (when-not (and (set? value) (every? keyword? value))
    (diagnostics/fail! "C1-PASS-CONTRACT"
                       "pass fact fields must be keyword sets"
                       {:field field :observed value}))
  value)

(defn authority-level!
  [field value]
  (when-not (contains? config/authority-rank value)
    (diagnostics/fail! "C16-POLICY" "unknown authority level"
                       {:field field :observed value}))
  value)

(defn weakest-authority
  [levels]
  (first (sort-by config/authority-rank levels)))

(defn validate-operations!
  [operations expected id]
  (exact-map! operations expected id :operations)
  (doseq [[key operation] operations]
    (when-not (fn? operation)
      (diagnostics/fail! id "pass execution operations must be functions"
                         {:operation key
                          :observed-class (some-> operation class .getName)})))
  operations)

(defn validate-semantic-bindings!
  [bindings]
  (exact-map! bindings config/semantic-binding-fields "C16-KEY"
              :semantic-bindings)
  (doseq [[field value] bindings] (require-sha256! field value))
  bindings)

(defn validate-provenance!
  [provenance]
  (exact-map! provenance config/provenance-fields "C16-ENTRY" :provenance)
  (require-sha256! :provenance-id (:provenance-id provenance))
  (when-not (or (nil? (:source-path provenance))
                (string? (:source-path provenance)))
    (diagnostics/fail! "C16-ENTRY"
                       "source path metadata must be a string or nil" {}))
  (when-not (map? (:metadata provenance))
    (diagnostics/fail! "C16-ENTRY" "provenance metadata must be a map" {}))
  (canonical/canonical-bytes (:metadata provenance))
  provenance)

(defn validate-request-authority!
  [authority ceiling input-artifact-ids]
  (exact-map! authority config/request-authority-fields "C16-POLICY" :authority)
  (let [input-authorities (:input-authorities authority)
        _ (when-not (and (map? input-authorities)
                         (= (set input-artifact-ids)
                            (set (keys input-authorities))))
            (diagnostics/fail!
             "C16-POLICY"
             "input authority must bind every and only input artifact id"
             {:input-artifact-ids input-artifact-ids
              :bound-artifact-ids (when (map? input-authorities)
                                    (vec (sort (keys input-authorities))))}))
        levels (mapv (fn [[artifact-id level]]
                       (require-sha256! :input-authority-artifact-id artifact-id)
                       (authority-level! :input-authority-level level))
                     input-authorities)
        claimed (authority-level! :claimed-level (:claimed-level authority))]
    (let [scope (:scope authority)]
      (when-not (or (and (keyword? scope) (not (empty? (name scope))))
                    (and (string? scope)
                         (some #(not (Character/isWhitespace ^char %)) scope)))
        (diagnostics/fail! "C16-POLICY"
                           "authority scope must be explicit and nonblank"
                           {:scope scope})))
    (let [maximum (weakest-authority (conj levels ceiling))]
      (when (> (config/authority-rank claimed)
               (config/authority-rank maximum))
        (diagnostics/fail! "C16-POLICY" "pass receipt would widen authority"
                           {:claimed claimed :maximum maximum}))))
  authority)

(defn validate-input-artifact-ids!
  [input-artifact-ids]
  (sorted-sha-vector! :input-artifact-ids input-artifact-ids)
  (when (empty? input-artifact-ids)
    (diagnostics/fail!
     "D1-ARTIFACT-GAP"
     "this execution wave requires at least one input artifact"
     {:input-artifact-ids input-artifact-ids
      :remediation
      "provide an input artifact; source-authority roots are not yet supported"}))
  input-artifact-ids)

(defn validate-external-root-inputs!
  [external-root-inputs input-artifact-ids input-facts input-kind]
  (when-not (map? external-root-inputs)
    (diagnostics/fail! "D1-ARTIFACT-GAP"
                       "external roots must map artifact ids to exact descriptors"
                       {:observed external-root-inputs}))
  (doseq [[artifact-id descriptor] external-root-inputs]
    (require-sha256! :external-root-artifact-id artifact-id)
    (exact-map! descriptor config/external-root-fields "D1-ARTIFACT-GAP"
                :external-root)
    (when-not (keyword? (:kind descriptor))
      (diagnostics/fail! "D1-ARTIFACT-GAP"
                         "external-root kind must be a keyword"
                         {:artifact-id artifact-id :kind (:kind descriptor)}))
    (when-not (= input-kind (:kind descriptor))
      (diagnostics/fail!
       "C1-PASS-CONTRACT"
       "external-root kind does not match the consumer input contract"
       {:artifact-id artifact-id :external-root-kind (:kind descriptor)
        :consumer-input input-kind}))
    (keyword-set! :external-root-facts (:facts descriptor)))
  (when-not (set/subset? (set (keys external-root-inputs))
                         (set input-artifact-ids))
    (diagnostics/fail! "D1-ARTIFACT-GAP"
                       "external roots must be declared input artifacts"
                       {:external-root-input-ids
                        (vec (sort (keys external-root-inputs)))}))
  (let [external-facts (reduce set/union #{}
                               (map :facts (vals external-root-inputs)))]
    (when-not (set/subset? external-facts input-facts)
      (diagnostics/fail! "C1-EVIDENCE-DROP"
                         "external-root facts must be present in pass input facts"
                         {:external-facts external-facts
                          :input-facts input-facts})))
  external-root-inputs)

(defn validate-verifier-report-shape!
  [report output-id stage]
  (exact-map! report config/verifier-report-fields "C18-EVIDENCE"
              :verifier-report)
  (require-sha256! :verifier-id (:verifier-id report))
  (when-not (and (= stage (:stage report))
                 (= output-id (:artifact-id report))
                 (= :passed (:status report)))
    (diagnostics/fail! "C18-VALIDATION"
                       "pass verifier report did not accept this output"
                       {:report report :stage stage :artifact-id output-id}))
  report)

(defn validate-evidence-record-shape!
  [record output-id]
  (exact-map! record config/evidence-record-fields "C18-EVIDENCE"
              :evidence-record)
  (require-sha256! :evidence-id (:evidence-id record))
  (when-not (keyword? (:kind record))
    (diagnostics/fail! "C18-EVIDENCE" "evidence kind must be a keyword"
                       {:record record}))
  (when-not (and (= output-id (:artifact-id record))
                 (= :accepted (:status record)))
    (diagnostics/fail! "C18-EVIDENCE"
                       "pass evidence did not accept this output"
                       {:record record :artifact-id output-id}))
  (authority-level! :authority-level (:authority-level record))
  record)
