

(defn- c2-pass-cache-revalidate-artifact!
  [source-path source-text expected-source-unit current-binding
   artifact entry key]
  (let [source-unit (:source-unit-record artifact)
        token-stream (:token-stream artifact)
        form-tree (:form-tree artifact)
        syntax-seeds (:syntax-seed-stream artifact)
        extension-invocations
        (:reader-extension-invocation-records artifact)
        diagnostics (:reader-diagnostics artifact)
        incremental-hashes
        (c2-incremental-hashes source-unit token-stream form-tree syntax-seeds
                               extension-invocations diagnostics)
        integrity-record
        (c2-reader-product-integrity-record
         source-unit (:top-level-form-ids artifact) incremental-hashes
         (:literal-decoding-records artifact)
         (get-in artifact
                 [:semantic-error-deferment-record
                  :deferred-literal-records]))
        artifact-id (c2-reader-artifact-id artifact)
        reconstructed-source
        (c2-reader-artifact-source-text source-path artifact)
        accepted-reader-boundary
        (:gravity-reader-boundary artifact)
        envelope-descriptor
        (:authenticated-envelope-descriptor accepted-reader-boundary)
        envelope (:authenticated-envelope accepted-reader-boundary)
        descriptor-summary
        (:value
         (some #(when (= :reader-product-identities (:name %)) %)
               (:semantic-projections envelope-descriptor)))
        expected-uncredited
        (:uncredited-source-models c2-pass-cache-boundary-contract)
        boundary-projection-id
        (c2-pass-cache-boundary-projection-id artifact)]
    (p15-s23-stage2-sh02-descriptor-envelope-verify!
     envelope :c2-reader :gravity/sh03-reader-products
     envelope-descriptor source-path)
    (when-not
     (and (= :gravity/stage0-c2-reader-document-artifact (:kind artifact))
          (= "P06-D081" (:task artifact))
          (= ["C2"] (:document-set artifact))
          (= c2-pass-cache-pass-contract (:pass artifact))
          (= expected-source-unit source-unit)
          (= source-text reconstructed-source)
          (= (get-in key [:semantic-preimage :source-unit :source-id])
             (:source-id source-unit))
          (= :gravity/sh03-to-c2-reader-products-v2
             (:adapter-contract accepted-reader-boundary))
          (= #{:slice :owner :plan-binding :resolved-reader-result
               :adapter-contract :uncredited-source-models
               :semantic-value-table-id
               :authenticated-envelope-descriptor
               :authenticated-envelope :target-source-reread?
               :clojure-adapter-residual? :self-hosted?}
             (set (keys accepted-reader-boundary)))
          (= :SH-03 (:slice accepted-reader-boundary))
          (= :gravity-source (:owner accepted-reader-boundary))
          (= (:exact-sh03-binding current-binding)
             (:plan-binding accepted-reader-boundary))
          (= (get-in current-binding [:boundary-binding :plan-binding-id])
             (c2-pass-cache/canonical-content-id
              {:domain :gravity/c2-pass-cache-exact-sh03-plan-binding-v1
               :binding (:plan-binding accepted-reader-boundary)}))
          (= :accepted
             (get-in accepted-reader-boundary
                     [:resolved-reader-result :status]))
          (= expected-uncredited
             (:uncredited-source-models accepted-reader-boundary))
          (= (:semantic-value-table-id descriptor-summary)
             (:semantic-value-table-id accepted-reader-boundary))
          (false? (:target-source-reread? accepted-reader-boundary))
          (true? (:clojure-adapter-residual? accepted-reader-boundary))
          (false? (:self-hosted? accepted-reader-boundary))
          (empty? diagnostics)
          (empty? (:diagnostics artifact))
          (= incremental-hashes (:incremental-reader-hashes artifact))
          (= integrity-record (:reader-product-integrity artifact))
          (= artifact-id (:artifact-id artifact))
          (or (nil? entry)
              (and (= boundary-projection-id
                      (:boundary-projection-id entry))
                   (= (get-in incremental-hashes [:reader-diagnostics])
                      (:diagnostics entry)))))
      (throw
       (ex-info
        "cached C2 artifact failed current compiler/pass/SH03 revalidation"
        {:id "C16-STALE"
         :stage :c2-reader
         :artifact-id (:artifact-id artifact)
         :cache-key (:semantic-key-id key)
         :release-authority? false
         :self-hosted? false})))
    (c2-reader-validate! source-path artifact)
    artifact))

(defn- compiler-c2-reader-file-artifact-uncached
  [path]
  (let [source-bytes (sh03-reader-read-target-source-bytes! path)
        project-context (reader-project-context-for-source path)
        resolved (sh03-reader-resolved-result!
                  path source-bytes project-context standard-reader-options)
        result (:result resolved)
        _ (sh03-reader-raise-rejection!
           path source-bytes standard-reader-options project-context result)
        source-text (sh03-reader-strict-source-text!
                     path path source-bytes)
        products (sh03-reader-adapt-products!
                  path source-text source-bytes standard-reader-options
                  project-context resolved)]
    (compiler-c2-reader-source-artifact
     path source-text project-context products
     sh03-reader-internal-product-authority)))

(defn compiler-c2-reader-file-artifact-cached
  "Return the local persistent C2 pass-cache result for one source file.

  The operational envelope contains the unchanged C2 artifact and explicit
  hit/miss evidence.  A validated hit does not execute the target reader.  This
  local cache has no release, proof, equivalence, or self-hosting authority."
  [path cache-base]
  (let [snapshot (c2-pass-cache/bounded-source-snapshot!
                  path sh03-reader-maximum-source-bytes)
        current-binding (c2-pass-cache-current-binding! path)
        {:keys [key source-text source-unit]}
        (c2-pass-cache-key-context! path snapshot current-binding)
        store (c2-pass-cache/open-local-store cache-base)
        result
        (c2-pass-cache/lookup-or-compute!
         store key
         {:current-binding (:entry-binding current-binding)
          :artifact-id-of c2-reader-artifact-id
          :boundary-projection-id-of
          c2-pass-cache-boundary-projection-id
          :validate-artifact!
          (fn [artifact entry cache-key]
            (c2-pass-cache-revalidate-artifact!
             path source-text source-unit current-binding
             artifact entry cache-key))
          :compute! #(compiler-c2-reader-file-artifact-uncached path)})]
    {:kind :gravity/local-c2-pass-cache-result
     :stage :c2-reader
     :c2-reader-artifact (:artifact result)
     :cache-evidence (:cache-evidence result)
     :cache-contract (c2-pass-cache/cache-contract)
     :clojure-adapter-residual? true
     :self-hosted? false
     :release-authority? false
     :proof-authority? false
     :equivalence-authority? false}))

(defn compiler-c2-reader-file-artifact
  "Compile one source file through the ordinary C2 reader path.

  Reuse is content-addressed below the source's explicit project root and is
  accepted only after current C2, pass, diagnostic, provenance, and SH-03
  boundary revalidation.  The returned value is the unchanged C2 artifact;
  local cache receipts remain non-authoritative."
  [path]
  (:c2-reader-artifact
   (compiler-c2-reader-file-artifact-cached
    path (.getPath (reader-project-root-path (str path))))))

(def c3-syntax-diagnostic-ids
  c3-syntax-diagnostics/c3-syntax-diagnostic-ids)

(def c3-syntax-governing-document
  c3-syntax-diagnostics/c3-syntax-governing-document)

(def c3-syntax-rejected-designs
  c3-syntax-diagnostics/c3-syntax-rejected-designs)

(def c3-syntax-override-diagnostics
  c3-syntax-diagnostics/c3-syntax-override-diagnostics)

(declare c3-syntax-source-overrides
         c3-syntax-overrides-from-forms
         c3-syntax-message
         c3-syntax-fail!
         c3-syntax-validate-overrides!)

(defn- c3-syntax-diagnostics-ops
  []
  {:fail! fail!
   :source-span source-span
   :c3-syntax-source-overrides c3-syntax-source-overrides
   :c3-syntax-overrides-from-forms c3-syntax-overrides-from-forms
   :c3-syntax-message c3-syntax-message
   :c3-syntax-fail! c3-syntax-fail!
   :c3-syntax-validate-overrides! c3-syntax-validate-overrides!
   :c3-syntax-diagnostic-ids c3-syntax-diagnostic-ids
   :c3-syntax-governing-document c3-syntax-governing-document
   :c3-syntax-rejected-designs c3-syntax-rejected-designs
   :c3-syntax-override-diagnostics c3-syntax-override-diagnostics})