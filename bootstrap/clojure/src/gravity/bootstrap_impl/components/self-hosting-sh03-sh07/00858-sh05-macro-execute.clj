

(defn sh05-macro-execute!
  [source-path binding function arguments]
  (try
    (sh04-syntax-strip-host-metadata
     (p15-s23-stage2-runtime-execute-function
      {:engine :gravity-sh05-pinned-macro-runner
       :compiler-artifact-plan? true}
      (:plan binding) function
      (sh04-syntax-strip-host-metadata arguments)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh05-macro-boundary-fail!
       "C4-DEPTH" source-path :bounded-macro-host-stack function
       {:contained-host-error (.getName (class error))}))
    (catch AssertionError error
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path :contained-macro-assertion function
       {:contained-host-error (.getName (class error))}))
    (catch LinkageError error
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path :contained-macro-linkage function
       {:contained-host-error (.getName (class error))}))
    (catch clojure.lang.ExceptionInfo error
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path :contained-macro-runtime-diagnostic
       function {:contained-diagnostic (:id (ex-data error))}))
    (catch Exception error
      (sh05-macro-boundary-fail!
       "C4-RETURN" source-path :contained-macro-host-failure function
       {:contained-host-error (.getName (class error))
        :cause-message (.getMessage error)}))))

(defn sh05-resolve-request-preimage!
  [source-path request resolved-digests]
  (let [ordinal (:ordinal request)
        purpose (:purpose request)
        preimage (:preimage request)]
    (case purpose
      :sh05-expanded-syntax-id
      (do
        (when-not (and (= 0 ordinal) (empty? resolved-digests))
          (sh05-macro-boundary-fail!
           "C4-TRACE" source-path :exact-expanded-syntax-request
           request {:resolved-digest-count (count resolved-digests)}))
        preimage)

      :sh05-expansion-provenance-binding-id
      (do
        (when-not (and (= 1 ordinal)
                       (= 1 (count resolved-digests))
                       (= {:digest-ref 0}
                          (:semantic-artifact-id preimage)))
          (sh05-macro-boundary-fail!
           "C4-TRACE" source-path :declared-provenance-artifact-reference
           (:semantic-artifact-id preimage)
           {:resolved-digest-count (count resolved-digests)}))
        (assoc preimage :semantic-artifact-id (first resolved-digests)))

      :sh05-macro-diagnostic-id
      (do
        (when-not (and (= 0 ordinal) (empty? resolved-digests))
          (sh05-macro-boundary-fail!
           "C4-TRACE" source-path :exact-macro-diagnostic-request
           request {:resolved-digest-count (count resolved-digests)}))
        preimage)

      (sh05-macro-boundary-fail!
       "C4-TRACE" source-path :known-macro-digest-request-purpose
       purpose {:ordinal ordinal}))))

(defn sh05-macro-resolve-digests!
  [source-path digest-requests]
  (reduce
   (fn [resolved request]
     (let [ordinal (:ordinal request)
           preimage
           (sh05-resolve-request-preimage!
            source-path request resolved)]
       (when-not (= ordinal (count resolved))
         (sh05-macro-boundary-fail!
          "C4-TRACE" source-path :ordered-macro-digest-requests
          request {:resolved-count (count resolved)}))
       (conj resolved
             (p15-s23-c6c10-canonical-digest source-path preimage))))
   [] digest-requests))

(defn sh05-deep-merge
  [left right]
  (merge-with (fn [left-value right-value]
                (if (and (map? left-value) (map? right-value))
                  (sh05-deep-merge left-value right-value)
                  right-value))
              left right))

(defn sh05-defn-form?
  [form]
  (and (seq? form) (= 'defn (first form))))

(defn sh05-defmacro-form?
  [form]
  (and (seq? form) (= 'defmacro (first form))))

(defn sh05-semantic-component-id
  [call-site-syntax-id role value]
  (reader-canonical-hash
   {:domain :gravity/sh05-input-syntax-component-v1
    :call-site-syntax-id call-site-syntax-id
    :role role
    :semantic-value (sh04-syntax-strip-host-metadata value)}))

(defn sh05-default-macro-request
  [source-path c3-artifact module syntax form]
  (let [[_ name parameters & body] form
        call-site-syntax-id (:syntax/id syntax)
        reader-binding
        (get-in c3-artifact
                [:gravity-syntax-boundary :reader-semantic-binding])
        reader-source-revision
        (get-in c3-artifact
                [:gravity-syntax-boundary :reader-source-revision])
        macro-environment-id
        (reader-canonical-hash
         {:domain :gravity/sh05-bootstrap-macro-environment-v1
          :macros [{:name 'defn :version sh05-macro-version
                    :phase :macro-expansion
                    :build-effects [] :capabilities []}]})
        base-policy
        {:macro-return-kind :syntax
         :expansion-depth 0
         :output-node-count (+ 4 (count body))
         :requested-build-effects []
         :declared-build-effects []
         :granted-build-effects []
         :requested-capabilities []
         :granted-capabilities []
         :hidden-captures []
         :capture-policy :explicit-only
         :generated-unsafe? false
         :unsafe-declared? false
         :allowed-profiles [(:profile module)]
         :generated-profile (:profile module)}
        build-policy-id
        (reader-canonical-hash
         {:domain :gravity/sh05-build-policy-v1 :policy base-policy})
        trace-replay-id
        (reader-canonical-hash
         {:domain :gravity/sh05-trace-replay-v1
          :call-site-syntax-id call-site-syntax-id
          :macro-environment-id macro-environment-id
          :build-policy-id build-policy-id})
        semantic-span
        (let [span
              (select-keys (:span syntax)
                           [:file :byte-start :byte-end
                            :scalar-start :scalar-end :line-start
                            :column-start :line-end :column-end])]
          ;; SH-04 retains the physical source in :source for diagnostics.
          ;; Macro semantic identity uses only its co-canonical source id.
          (assoc span :source (:file span)))]
    {:artifact :gravity/sh05-authenticated-c3-macro-request
     :schema-version 1
     :call
     {:macro-name 'defn
      :macro-version sh05-macro-version
      :call-site-syntax-id call-site-syntax-id
      :definition-syntax-id
      (reader-canonical-hash
       {:domain :gravity/sh05-defn-macro-definition-v1
        :macro-version sh05-macro-version})
      :name-syntax-id
      (sh05-semantic-component-id call-site-syntax-id :name name)
      :parameter-vector-syntax-id
      (sh05-semantic-component-id call-site-syntax-id
                                  :parameters parameters)
      :body-syntax-ids
      (mapv #(sh05-semantic-component-id call-site-syntax-id
                                         [:body %1] %2)
            (range) body)
      :semantic-call-span semantic-span
      :metadata (or (:metadata syntax) {})
      :hygiene (select-keys (:hygiene syntax)
                            [:marks :lexical-scopes])
      :origin-chain (vec (:origin syntax))
      :call-site-span (:span syntax)
      :definition-span
      {:source sh05-macro-source-relative-path
       :byte-start 0 :byte-end sh05-macro-expected-source-byte-count}}
     :context
     {:phase :macro-expansion
      :profile (:profile module)
      :target (:target module)
      :macro-environment-id macro-environment-id
      :build-policy-id build-policy-id
      :trace-replay-id trace-replay-id}
     :policy (assoc base-policy
                    :recorded-trace-replay-id trace-replay-id)
     :provenance {:actual-source-path source-path}
     :reader-binding reader-binding
     :reader-source-revision reader-source-revision
     :reader-semantic-binding-id (:semantic-binding-id reader-binding)
     :source-revision-id (:revision-id reader-source-revision)}))

(defn sh05-macro-raise-rejection!
  [source-path raw resolved-digests]
  (let [diagnostic
        (let [template (first (:diagnostics raw))]
          (when-not (and (= 1 (count resolved-digests))
                         (= {:digest-ref 0}
                            (:diagnostic-id-request template)))
            (sh05-macro-boundary-fail!
             "C4-TRACE" source-path :declared-diagnostic-id-reference
             (:diagnostic-id-request template)
             {:resolved-digest-count (count resolved-digests)}))
          (assoc template :diagnostic-id-request
                 (first resolved-digests)))
        rule (:rule diagnostic)]
    (c4-macro-fail!
     rule source-path
     {:source-span (source-span source-path 0)
      :macro (:macro-name diagnostic)
      :macro-version (:macro-version diagnostic)
      :profile (:profile diagnostic)
      :target (:target diagnostic)
      :build-effects (:build-effects diagnostic)
      :capabilities (:capabilities diagnostic)
      :hygiene (:hygiene-context diagnostic)}
     {:severity (:severity diagnostic)
      :diagnostic-family :c4-macro-expansion
      :stage :macro-expansion
      :facts (:facts diagnostic)
      :remediation (:remediation diagnostic)
      :gravity-diagnostic diagnostic
      :diagnostic-id (get diagnostic :diagnostic-id-request)})))