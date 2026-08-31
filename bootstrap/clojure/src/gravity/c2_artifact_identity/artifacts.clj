(ns gravity.c2-artifact-identity.artifacts)

(defn reader-product-integrity-record
  [{:keys [c2-path-neutral-span reader-canonical-hash]}
   source-unit top-level-form-ids incremental-hashes literal-records
   deferred-literal-records]
  (let [literal-input (mapv #(update % :span c2-path-neutral-span) literal-records)
        deferred-input (mapv #(update % :span c2-path-neutral-span)
                             deferred-literal-records)
        input {:source-id (:source-id source-unit)
               :source-identity-inputs (:identity-inputs source-unit)
               :source-bytes-hash (:bytes-hash source-unit)
               :reader-options (:reader-options source-unit)
               :top-level-form-ids (vec top-level-form-ids)
               :incremental-reader-hashes incremental-hashes
               :literal-records-hash (reader-canonical-hash literal-input)
               :deferred-literal-records-hash (reader-canonical-hash deferred-input)}]
    {:artifact :gravity/c2-reader-product-integrity
     :algorithm :sha256
     :input input
     :integrity-hash (reader-canonical-hash input)
     :status :verified}))

(defn reader-artifact-id [reader-canonical-hash artifact]
  (reader-canonical-hash
   {:kind (:kind artifact)
    :task (:task artifact)
    :document-set (:document-set artifact)
    :source-id (get-in artifact [:source-unit-record :source-id])
    :reader-product-integrity (:reader-product-integrity artifact)
    :incremental-reader-hashes (:incremental-reader-hashes artifact)
    :representation-boundary (:representation-boundary artifact)
    :source-overrides (:source-overrides artifact)
    :capability-based-proof (:capability-based-proof artifact)}))
