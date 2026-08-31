

(defn sh04-syntax-build-binding!
  [request-source]
  (let [source (sh04-syntax-read-pinned-source! request-source)
        emitter
        (:emitter
         (c-backend-stage2-plan-emitter-source-rule!
          (:source-path source) :jvm))
        plan
        (p15-s23-stage2-compiler-artifact-plan
         emitter (:source-path source) (:source-text source))
        identities (sh04-syntax-plan-identities plan)]
    (when-not
     (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
          (true? (:compiler-artifact-plan? plan))
          (= 'gravity.bootstrap.syntax (get-in plan [:module :module]))
          (= :meta (get-in plan [:module :profile]))
          (= :jvm (get-in plan [:module :target]))
          (= #{} (get-in plan [:module :effects]))
          (= #{} (get-in plan [:module :capabilities]))
          (= :safe (get-in plan [:module :safety]))
          (= sh04-syntax-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh04-syntax-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh04-syntax-expected-function-count
             (:function-count identities))
          (= sh04-syntax-expected-function-names-hash
             (:function-names-hash identities))
          (= sh04-syntax-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh04-syntax-public-function-hashes
             (:public-function-hashes identities))
          (= sh04-syntax-public-function-shapes
             (:public-function-shapes identities)))
      (sh04-syntax-boundary-fail!
       "C3-ID" request-source :pinned-syntax-plan-and-functions
       identities {}))
    (merge source identities
           {:artifact :gravity/sh04-pinned-syntax-plan-binding
            :status :complete
            :semantic-authority :gravity-source
            :compiled-by :clojure-stage0-seed
            :executed-by :clojure-stage2-generic-rule-runner
            :generic-bridge-residual? true
            :self-hosted? false
            :plan plan})))

(def ^:private sh04-syntax-cached-binding
  (delay (sh04-syntax-build-binding! "<sh04-syntax-bootstrap>")))

(defn sh04-syntax-current-binding!
  [request-source]
  (let [fresh (sh04-syntax-read-pinned-source! request-source)
        binding @sh04-syntax-cached-binding
        identities (sh04-syntax-plan-identities (:plan binding))]
    (when-not
     (and (= (:source-byte-count fresh) (:source-byte-count binding))
          (= (:source-content-hash fresh) (:source-content-hash binding))
          (= (select-keys binding
                          [:plan-semantic-hash :functions-semantic-hash
                           :function-count :function-names-hash
                           :function-shapes-hash :public-function-hashes
                           :public-function-shapes])
             identities))
      (sh04-syntax-boundary-fail!
       "C3-ID" request-source :fresh-syntax-source-and-plan-binding
       binding {}))
    binding))

(declare sh04-syntax-strip-host-metadata)

(defn sh04-syntax-execute!
  [source-path binding function arguments]
  (try
    (let [clean-arguments
          (sh04-syntax-strip-host-metadata arguments)]
      (sh04-syntax-strip-host-metadata
       (p15-s23-stage2-runtime-execute-function
        {:engine :gravity-sh04-pinned-syntax-runner
         :compiler-artifact-plan? true}
        (:plan binding) function clean-arguments)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :bounded-syntax-host-stack function
       {:contained-host-error (.getName (class error))}))
    (catch AssertionError error
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :contained-syntax-assertion function
       {:contained-host-error (.getName (class error))}))
    (catch LinkageError error
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :contained-syntax-linkage function
       {:contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo error
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :contained-syntax-runtime-diagnostic
       function {:contained-diagnostic (:id (ex-data error))}))
    (catch Exception error
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :contained-syntax-host-failure function
       {:contained-host-error (.getName (class error))
        :cause-message (.getMessage error)}))))

(defn sh04-syntax-require-carrier!
  [source-path carrier value]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value :default-only 1048576 256 4096)]
    (when-not (= :passed (:status validation))
      (sh04-syntax-boundary-fail!
       "C3-SHAPE" source-path :bounded-syntax-result-carrier
       carrier (select-keys validation [:reason :observed-nodes
                                        :observed-depth :maximum-nodes
                                        :maximum-depth :maximum-width])))
    value))

(defn sh04-syntax-raise-result!
  [source-path result]
  (sh04-syntax-require-carrier! source-path :gravity-syntax-result result)
  (when-not (= :accepted (:status result))
    (let [diagnostic (first (:diagnostics result))
          rule (or (:rule diagnostic) "C3-SHAPE")]
      (c3-syntax-fail!
       rule source-path
       {:source-span (or (get-in diagnostic [:primary :span])
                         (source-span source-path 0))
        :syntax-id (get-in diagnostic [:primary :syntax-id])
        :producer :gravity.bootstrap.syntax
        :form-kind (get-in diagnostic [:facts :form-kind])}
       {:missing-fields (or (get-in diagnostic [:facts :missing-fields])
                            [:accepted-gravity-syntax-result])
        :facts {:gravity-diagnostic diagnostic}})))
  result)

(defn sh04-syntax-resolve-object-template!
  [source-path syntax-template resolved-digests]
  ;; Digest references are control data only in schema-declared identity
  ;; slots.  Semantic forms may legitimately contain an ordinary map such as
  ;; {:digest-ref 0}; recursively walking the artifact would reinterpret that
  ;; source data and change the program being compiled.
  (when-not (and (= 2 (count resolved-digests))
                 (= {:digest-ref 1} (:syntax-id syntax-template)))
    (sh04-syntax-boundary-fail!
     "C3-ID" source-path :declared-syntax-id-reference
     (:syntax-id syntax-template)
     {:resolved-digest-count (count resolved-digests)}))
  (assoc syntax-template :syntax-id (second resolved-digests)))

(defn sh04-syntax-resolve-stream-template!
  [source-path stream-template resolved-digests]
  (when-not (and (= 1 (count resolved-digests))
                 (= {:digest-ref 0} (:artifact-id stream-template)))
    (sh04-syntax-boundary-fail!
     "C3-ID" source-path :declared-stream-artifact-id-reference
     (:artifact-id stream-template)
     {:resolved-digest-count (count resolved-digests)}))
  (assoc stream-template :artifact-id (first resolved-digests)))

(defn sh04-syntax-resolve-request-preimage!
  [source-path request resolved-digests]
  (let [ordinal (:ordinal request)
        dependencies (:depends-on request)
        preimage (:preimage request)]
    (case ordinal
      0
      (do
        (when-not (and (= [] dependencies) (empty? resolved-digests))
          (sh04-syntax-boundary-fail!
           "C3-ID" source-path :exact-syntax-request-zero-dependencies
           dependencies {:resolved-digest-count (count resolved-digests)}))
        preimage)

      1
      (do
        (when-not (and (= [0] dependencies)
                       (= 1 (count resolved-digests))
                       (= {:digest-ref 0}
                          (get-in preimage
                                  [:reader-binding
                                   :semantic-binding-id])))
          (sh04-syntax-boundary-fail!
           "C3-ID" source-path :declared-reader-binding-reference
           (get-in preimage [:reader-binding :semantic-binding-id])
           {:dependencies dependencies
            :resolved-digest-count (count resolved-digests)}))
        (assoc-in preimage [:reader-binding :semantic-binding-id]
                  (first resolved-digests)))

      (sh04-syntax-boundary-fail!
       "C3-ID" source-path :known-syntax-request-ordinal
       ordinal {:dependencies dependencies}))))

(defn sh04-syntax-resolve-stream-request-preimage!
  [source-path request resolved-digests]
  (when-not (and (= 0 (:ordinal request))
                 (= [] (:depends-on request))
                 (empty? resolved-digests))
    (sh04-syntax-boundary-fail!
     "C3-ID" source-path :exact-syntax-stream-request-dependencies
     request {:resolved-digest-count (count resolved-digests)}))
  (:preimage request))