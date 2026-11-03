(declare sh03-reader-authenticated-envelope-options
         sh04-syntax-selection!
         sh04-syntax-current-binding!)

(def ^:private sh04-development-syntax-contract
  {:artifact :gravity/sh04-syntax-object-contract
   :schema-version 1
   :owner :gravity-source
   :input :gravity/sh03-authenticated-reader-product
   :output :gravity/sh04-syntax-template-result
   :coordinator-adapter :gravity/sh04-to-c3-syntax-products-v1
   :sealed-product-kind :gravity/sh04-syntax-products
   :resolved-authoritative-result :gravity/sh04-syntax-object-artifact
   :identity-domain :gravity/sh04-syntax-object-v1
   :identity-fields [:form :span :source :namespace :phase :profile :metadata
                     :hygiene :origin :facts :reader-binding :reader-source-revision
                     :ownership :version]
   :physical-provenance-excluded-from-identity [:actual-source-path]
   :reference-order :prior-only
   :serialization :gravity-tagged-vector-v1
   :host-authority [:validate-sh03-boundary :resolve-digest-requests :bind-c3-artifact]
   :self-hosted? false
   :whole-language? false
   :clojure-seed-boundary? true})

(def ^:private sh04-development-syntax-diagnostics
  {:diagnostics ["C3-SHAPE" "C3-ID" "C3-SPAN" "C3-ORIGIN"
                 "C3-HYGIENE" "C3-CAPTURE" "C3-METADATA"
                 "C3-FACT-STALE" "C3-SERIALIZE"]
   :stable-fields [:rule :severity :stage :primary :related :origin-chain
                   :profile :target :facts :remediation]
   :stops [:macro-expansion :name-resolution :analysis]})
(defn-
 sh04-syntax-source-mode
 [request-source invocation]
 (if
  (and
   (map? invocation)
   (contains? invocation :syntax-source-selection))
  (do
   (sh04-syntax-selection! request-source invocation)
   :development-current)
  :pinned))

(defn-
 sh04-syntax-selection-path
 [invocation]
 (get-in invocation [:syntax-source-selection :source-path]))

(defn-
 sh04-development-syntax-data!
 [request-source source]
 (let [forms (binding [*read-eval* false]
               (vec (read-forms (:source-path source) (:source-text source))))
       expected {'c3-syntax-object-contract sh04-development-syntax-contract
                 'c3-syntax-diagnostic-catalog sh04-development-syntax-diagnostics}]
   (into {}
         (for [[name value] expected]
           (let [definitions (filter #(and (seq? %)
                                           (= 'def (first %))
                                           (= name (second %)))
                                     forms)
                 definition (first definitions)
                 actual (when (and (= 1 (count definitions))
                                   (= 3 (count definition)))
                          (nth definition 2))]
             (when-not (and actual
                            (= :passed
                               (:status (p15-s23-trusted-carrier-validation
                                         actual :default-only)))
                            (= value actual))
               (sh04-syntax-boundary-fail!
                "C3-ID" request-source :syntax-public-DATA-contract name {}))
             [name actual])))))

(defn-
 sh04-development-syntax-interface!
 [request-source plan public-data identities]
 (let [exports (get-in plan [:module :exports])
       required (into (set (keys sh04-syntax-public-function-shapes))
                      (keys public-data))]
   (when-not
    (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
         (true? (:compiler-artifact-plan? plan))
         (= {:module 'gravity.bootstrap.syntax :profile :meta :target :jvm
             :effects #{} :capabilities #{} :safety :safe}
            (select-keys (:module plan)
                         [:module :profile :target :effects :capabilities :safety]))
         (= :passed (:status (p15-s23-trusted-carrier-validation
                              exports :default-only)))
         (= required (set exports))
         (= (count required) (count exports))
         (= sh04-syntax-public-function-shapes
            (:public-function-shapes identities)))
     (sh04-syntax-boundary-fail!
      "C3-ID" request-source :current-syntax-module-and-exports nil {}))
   (vec (sort-by str required))))

(defn
 sh04-development-syntax-binding!
 [request-source invocation]
 (let [selected-source-path (sh04-syntax-selection-path invocation)
       _ (sh04-syntax-selection! request-source invocation)
       source (sh04-syntax-read-current-source!
               request-source selected-source-path)
       public-data (sh04-development-syntax-data! request-source source)
       binding (sh04-syntax-current-binding! request-source invocation)
       plan (:plan binding)
       source-forms (binding [*read-eval* false]
                      (vec (read-forms (:source-path source)
                                       (:source-text source))))
       source-module (parse-module (:source-path source) source-forms)
       interface-plan (assoc-in plan [:module :exports]
                                (:exports source-module))
       identities (select-keys
                   binding
                   [:plan-semantic-hash
                    :functions-semantic-hash
                    :function-count
                    :function-names-hash
                    :function-shapes-hash
                    :public-function-hashes
                    :public-function-shapes])
       exports (sh04-development-syntax-interface!
                request-source interface-plan public-data identities)
       rule (c-backend-stage2-plan-emitter-source-rule!
             (:source-path source) :jvm)
       after (sh04-syntax-read-current-source!
              request-source selected-source-path)
       after-rule (c-backend-stage2-plan-emitter-source-rule!
                   (:source-path after) :jvm)]
   (when-not
    (and (= (select-keys source [:source-path :source-byte-count
                                 :source-content-hash])
            (select-keys after [:source-path :source-byte-count
                                :source-content-hash]))
         (= rule after-rule))
    (sh04-syntax-boundary-fail!
     "C3-ID" request-source :fresh-current-syntax-implementation nil {}))
   (let [implementation
         (merge identities
                {:artifact :gravity/sh04-development-syntax-implementation-v1
                 :schema-version 1
                 :admission-status :development-unadmitted
                 :logical-source-path sh04-syntax-source-relative-path
                 :source-byte-count (:source-byte-count source)
                 :source-content-hash (:source-content-hash source)
                 :exports exports
                 :public-DATA public-data
                 :compiler-identity (:compiler plan)
                 :emitter-source-rule-hash (:source-rule-hash rule)})
         binding-id (p15-s23-c11-mir-digest implementation)]
     (assoc binding
            :implementation-binding-id binding-id
            :implementation-projection
            (assoc implementation :binding-id binding-id)))))

(defn-
 sh04-development-syntax-binding?
 [binding]
 (= :gravity/sh04-development-syntax-plan-binding-v1 (:artifact binding)))

(defn
 sh04-syntax-assembly-binding!
 [request-source invocation]
 (if (and (map? invocation)
          (contains? invocation :syntax-source-selection))
   (sh04-development-syntax-binding! request-source invocation)
   (sh04-syntax-current-binding! request-source invocation)))

(defn
 sh04-development-syntax-context!
 [source-path invocation]
 (when (and (map? invocation)
            (contains? invocation :syntax-source-selection))
   (let [reader-invocation (select-keys invocation
                                        [:reader-source-path
                                         :authenticated-envelope-options])
         reader (sh03-reader-verification-invocation!
                 source-path reader-invocation)
         selected-source-path (sh04-syntax-selection-path invocation)]
     {:reader-source-path (get-in reader [:binding :source-path])
      :authenticated-envelope-options (:envelope-options reader)
      :syntax-source-selection
      {:schema :gravity/sh04-development-syntax-selection-v1
       :source-path selected-source-path}})))

(defn
 sh04-development-syntax-verification-context!
 [invocation c2-artifact boundary]
 (when (and (map? invocation)
            (contains? invocation :syntax-source-selection))
   (when-not (and (map? c2-artifact)
                  (map? boundary)
                  (map? (:plan-binding boundary))
                  (map? (get-in boundary
                                [:plan-binding
                                 :implementation-projection])))
     (sh04-syntax-boundary-fail!
      "C3-ID" "<sh04-selected-verification>"
      :native-selected-syntax-products nil {}))
   (let [path (get-in c2-artifact [:source-unit-record :path])
         context (sh04-development-syntax-context! path invocation)
         projection (:registered-literal-projection c2-artifact)]
     (when (and projection
                (or (not (map? projection))
                    (not= #{:reader-source-revision
                            :bindings
                            :schema-version
                            :reader-binding
                            :upstream-product-binding
                            :upstream-integrity-hash
                            :projection-id
                            :artifact
                            :upstream-artifact-id}
                           (set (keys projection)))))
       (sh04-syntax-boundary-fail!
        "C3-ID" path :selected-syntax-registered-projection-unsupported nil {}))
     (when-not (= context (:operation-context boundary))
       (sh04-syntax-boundary-fail!
        "C3-ID" path :same-syntax-operation-context nil {}))
     context)))

(defn
 sh04-syntax-verification-binding!
 [source-path invocation boundary]
 (let [binding (sh04-syntax-assembly-binding! source-path invocation)]
   (when (and (sh04-development-syntax-binding? binding)
              (not (and (map? boundary)
                        (= (dissoc binding :plan :source-text)
                           (:plan-binding boundary)))))
     (sh04-syntax-boundary-fail!
      "C3-ID" source-path :fresh-selected-syntax-binding nil {}))
   binding))

(defn
 sh04-syntax-build-binding!
 ([request-source]
  (gravity.syntax/build-plan-binding!
   request-source
   (syntax-stage-runtime-operations)))
 ([request-source invocation]
  (sh03-reader-authenticated-envelope-options request-source invocation)
  (let [source-mode (sh04-syntax-source-mode request-source invocation)]
   (gravity.syntax/build-plan-binding!
    request-source
    (syntax-stage-runtime-operations)
    source-mode
    (sh04-syntax-selection-path invocation)))))

(def ^:private sh04-syntax-cached-binding
  (gravity.syntax/plan-binding-cache (syntax-stage-runtime-operations)))

(defn
 sh04-syntax-current-binding!
 ([request-source]
  (gravity.syntax/current-plan-binding!
   request-source
   sh04-syntax-cached-binding
   (syntax-stage-runtime-operations)))
 ([request-source invocation]
  (sh03-reader-authenticated-envelope-options request-source invocation)
  (let [source-mode (sh04-syntax-source-mode request-source invocation)]
   (gravity.syntax/current-plan-binding!
    request-source
    sh04-syntax-cached-binding
    (syntax-stage-runtime-operations)
    source-mode
    (sh04-syntax-selection-path invocation)))))
(defn
 sh04-syntax-execute!
 [source-path binding function arguments]
 (gravity.syntax/execute-plan!
  source-path
  binding
  function
  arguments
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-require-carrier!
 [source-path carrier value]
 (gravity.syntax/require-result-carrier!
  source-path
  carrier
  value
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-raise-result!
 [source-path result]
 (gravity.syntax/accept-result!
  source-path
  result
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-object-template!
 [source-path syntax-template resolved-digests]
 (gravity.syntax/resolve-object-template!
  source-path
  syntax-template
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-stream-template!
 [source-path stream-template resolved-digests]
 (gravity.syntax/resolve-stream-template!
  source-path
  stream-template
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-request-preimage!
 [source-path request resolved-digests]
 (gravity.syntax/resolve-request-preimage!
  source-path
  request
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-stream-request-preimage!
 [source-path request resolved-digests]
 (gravity.syntax/resolve-stream-request-preimage!
  source-path
  request
  resolved-digests
  (syntax-stage-runtime-operations)))
