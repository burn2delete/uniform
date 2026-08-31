

(defn sh03-reader-plan-validate!
  [source-path plan]
  (let [carrier
        (p15-s23-trusted-carrier-validation
         plan :default-only sh03-reader-plan-maximum-nodes
         sh03-reader-plan-maximum-depth sh03-reader-plan-maximum-width)]
    (when-not (= :passed (:status carrier))
      (sh03-reader-boundary-fail!
       source-path :trusted-bounded-sh03-reader-plan
       plan (select-keys carrier [:reason :observed-nodes :observed-depth
                                  :maximum-nodes :maximum-depth
                                  :maximum-width])))
    (let [functions (:functions plan)]
      (when-not
       (and (map? plan)
            (= sh03-reader-plan-keys (set (keys plan)))
            (= :gravity/stage2-compiler-artifact-plan (:kind plan))
            (true? (:compiler-artifact-plan? plan))
            (= sh03-reader-entrypoint (:entrypoint plan))
            (= 'gravity.bootstrap.reader (get-in plan [:module :module]))
            (= :meta (get-in plan [:module :profile]))
            (= :jvm (get-in plan [:module :target]))
            (= #{} (get-in plan [:module :effects]))
            (= #{} (get-in plan [:module :capabilities]))
            (= :safe (get-in plan [:module :safety]))
            (= :gravity-source (get-in plan [:compiler :owner]))
            (= :p15-s23-stage2-expression-lowering
               (get-in plan [:compiler :stage]))
            (= :clojure-stage0-seed
               (get-in plan [:compiler :compiled-by]))
            (= :clojure-stage2-generic-rule-runner
               (get-in plan [:compiler :executed-by]))
            (true? (get-in plan [:compiler :generic-bridge-residual?]))
            (false? (get-in plan [:compiler :self-hosted?]))
            (map? functions)
            (= sh03-reader-expected-function-count (count functions))
            (contains? functions sh03-reader-entrypoint)
            (contains? functions sh03-reader-verifier)
            (every?
             (fn [[function-name function]]
               (and (str/starts-with? (name function-name)
                                      sh03-reader-function-prefix)
                    (= sh03-reader-function-keys
                       (set (keys function)))
                    (= function-name (:name function))
                    (vector? (:params function))
                    (= (:arity function) (count (:params function)))
                    (= (count (:params function))
                       (count (distinct (:params function))))
                    (every? symbol? (:params function))
                    (pos-int? (:body-form-count function))
                    (vector? (:instructions function))
                    (seq (:instructions function))
                    (= sh03-reader-binding-keys
                       (set (keys (:binding function))))
                    (= function-name (get-in function [:binding :name]))
                    (= :function (get-in function [:binding :kind]))
                    (= 'gravity.bootstrap.reader
                       (get-in function [:binding :namespace]))
                    (= :meta (get-in function [:binding :profile]))
                    (= :jvm (get-in function [:binding :target]))
                    (= #{} (get-in function [:binding :effects]))
                    (= #{} (get-in function [:binding :capabilities]))))
             functions))
        (sh03-reader-boundary-fail!
         source-path :exact-sh03-reader-plan-envelope
         plan {:observed-keys (when (map? plan) (set (keys plan)))}))
      (let [static-audit (sh03-reader-plan-static-audit! source-path plan)
            identities (sh03-reader-plan-identities plan)
            expected-plan-id
            (reader-canonical-hash (dissoc plan :plan-id))]
        (when-not
         (and (= expected-plan-id (:plan-id plan))
              (= sh03-reader-expected-plan-semantic-hash
                 (:plan-semantic-hash identities))
              (= sh03-reader-expected-functions-semantic-hash
                 (:functions-semantic-hash identities))
              (= sh03-reader-expected-function-count
                 (:function-count identities))
              (= sh03-reader-expected-function-names-hash
                 (:function-names-hash identities))
              (= sh03-reader-expected-function-shapes-hash
                 (:function-shapes-hash identities))
              (= sh03-reader-expected-entrypoint-semantic-hash
                 (:entrypoint-semantic-hash identities))
              (= sh03-reader-expected-verifier-semantic-hash
                 (:verifier-semantic-hash identities))
              (= sh03-reader-expected-builtin-functions-hash
                 (:builtin-functions-hash identities))
              (= (:instruction-summary plan)
                 (:instruction-summary static-audit)
                 (:instruction-summary identities))
              (= (:builtin-functions static-audit)
                 (:builtin-functions
                  (sh03-reader-plan-instruction-summary
                   source-path functions))))
          (sh03-reader-boundary-fail!
           source-path :pinned-recomputable-sh03-reader-plan-identities
           identities {:observed-plan-id (:plan-id plan)}))
        identities))))

(defn sh03-reader-build-pinned-binding!
  [request-source]
  (let [source (sh03-reader-read-pinned-source-bytes! request-source)
        built (sh03-reader-build-plan! request-source source)
        identities
        (sh03-reader-plan-validate! (:source-path source) (:plan built))]
    (merge
     (dissoc built :builtin-functions)
     identities
     {:artifact :gravity/sh03-pinned-reader-plan-binding
      :status :complete
      :semantic-authority :gravity-source
      :compiled-by :clojure-stage0-seed
      :executed-by :clojure-stage2-generic-rule-runner
      :generic-bridge-residual? true
      :self-hosted? false})))

(def ^:private sh03-reader-cached-binding
  (delay (sh03-reader-build-pinned-binding! "<sh03-reader-bootstrap>")))

(defn sh03-reader-current-binding!
  [request-source]
  (let [current-source
        (sh03-reader-read-pinned-source-bytes! request-source)
        binding @sh03-reader-cached-binding
        identities
        (sh03-reader-plan-validate! request-source (:plan binding))]
    (when-not
     (and (= (:source-byte-count current-source)
             (:source-byte-count binding))
          (= (:source-content-hash current-source)
             (:source-content-hash binding))
          (= (select-keys identities
                          [:plan-semantic-hash
                           :functions-semantic-hash
                           :function-count
                           :function-names-hash
                           :function-shapes-hash
                           :entrypoint-semantic-hash
                           :verifier-semantic-hash
                           :builtin-functions-hash])
             (select-keys binding
                          [:plan-semantic-hash
                           :functions-semantic-hash
                           :function-count
                           :function-names-hash
                           :function-shapes-hash
                           :entrypoint-semantic-hash
                           :verifier-semantic-hash
                           :builtin-functions-hash])))
      (sh03-reader-boundary-fail!
       request-source :fresh-sh03-reader-source-and-plan-binding
       binding {:current-source-content-hash
                (:source-content-hash current-source)}))
    binding))

(def sh03-reader-result-keys
  #{:artifact :schema-version :status :source-unit :actual-path-provenance
    :token-stream :form-tree :top-level-form-ids :top-level-parsed-records
    :parsed-semantic-values :semantic-value-table :literal-decoding-records
    :semantic-error-deferment-record
    :reader-extension-invocation-records :reader-source-map
    :incremental-reader-hashes :semantic-reader-template :digest-requests
    :diagnostics :bounds :execution-boundary})

(def sh03-reader-verification-report-keys
  #{:artifact :schema-version :status :verified? :reader-result-status
    :semantic-reader-template :digest-requests :diagnostics :bounds
    :execution-boundary})

(def sh03-reader-digest-request-keys
  #{:algorithm :depends-on :encoding :key :ordinal :preimage})

(def sh03-reader-source-digest-request-keys
  (conj sh03-reader-digest-request-keys :observed-id))

(def sh03-reader-accepted-digest-request-names
  [:source-content :source-unit :token-stream :form-tree
   :extension-invocation-set :reader-result])

(def sh03-reader-rejected-digest-request-names
  [:source-content :source-unit])

(def sh03-reader-source-slice-keys
  #{:artifact :schema-version :encoding :source-content-id
    :byte-start :byte-end :scalar-start :scalar-end})

(def sh03-reader-semantic-value-entry-keys
  #{:artifact :schema-version :value-id :form-id :token-id :kind
    :descriptor :semantic-key})

(def sh03-reader-semantic-value-reference-keys
  #{:artifact :schema-version :value-id :field})

(def sh03-reader-form-value-reference-keys
  #{:artifact :schema-version :form-id})

(defn sh03-reader-byte-vector
  [bytes]
  (mapv #(bit-and (int %) 0xff) bytes))

(defn sh03-reader-byte-array
  [values]
  (byte-array (map #(unchecked-byte (int %)) values)))

(defn sh03-reader-tag-codepoints
  [tag]
  (let [value (name tag)]
    (vec (.toArray (.codePoints value)))))