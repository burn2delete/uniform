
(defn sh06-resolution-context-root
  [context]
  (let [source-path
        (or (get-in context [:external-binding-authority :source-path])
            "<sh06-resolution-context>")
        external (:external-binding-authority context)
        plan (:resolution-plan-authority context)]
    (when-not
     (and (some? context)
          (sh06-resolution-exact-keys?
           context #{:external-binding-authority :resolution-plan-authority})
          (map? external) (map? plan)
          (= (:content external) (:content plan))
          (= (:authority-id external)
             (reader-canonical-hash
              [:gravity/sh07-b51-c5-external-binding-authority-v1
               (dissoc external :authority-id)]))
          (= (:authority-id plan)
             (reader-canonical-hash
              [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
               (dissoc plan :authority-id)])))
      (sh06-resolution-c5-fail!
       source-path :context-root-authority {}))
    (sh06-resolution-context-plan-binding! source-path context)
    (reader-canonical-hash
     [:gravity/sh07-b51-sh06-context-root-v1 context])))

(defn sh06-resolution-context-validation
  [artifact context]
  (let [source-path (or (get-in artifact [:provenance :source-path])
                        "<sh06-resolution-artifact>")]
    (try
      (when (nil? context)
        (sh06-resolution-c5-fail! source-path :missing-context {}))
      (let [sh05 (:sh05-macro-artifact artifact)
            base (sh06-resolution-base-request source-path sh05)
            expected
            (sh06-resolution-c5-authority-request!
             source-path sh05 base context)
            binding (sh06-resolution-context-plan-binding!
                     source-path context)
            actual (get-in artifact
                           [:gravity-resolution-boundary
                            :authenticated-resolution-request])
            root (sh06-resolution-context-root context)]
        (when-not (= expected actual)
          (sh06-resolution-c5-fail!
           source-path :artifact-request-context-mismatch {}))
        {:status :passed
         :context-root root
         :resolution-plan-authority-id
         (get-in context [:resolution-plan-authority :authority-id])
         :external-binding-authority-id
         (get-in context [:external-binding-authority :authority-id])
         :plan-binding binding})
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch Throwable error
        {:status :failed
         :source-path source-path
         :contained-host-error (.getName (class error))
         :contained-diagnostic
         (when (instance? clojure.lang.ExceptionInfo error)
           (:id (ex-data error)))}))))

(defn- sh06-resolution-artifact-verification*
  ([artifact construction?]
   (sh06-resolution-artifact-verification* artifact construction? ::legacy))
  ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (let [legacy? (= ::legacy context)
         aggregate-validation
         (sh06-resolution-carrier-validation
          artifact)
         carrier-ok? (= :passed (:status aggregate-validation))
         boundary (when (and carrier-ok? (map? artifact))
                    (:gravity-resolution-boundary artifact))
         shape-checks
         {:trusted-artifact-carrier? carrier-ok?
          :top-level-artifact-map? (and carrier-ok? (map? artifact))
          :exact-artifact-shape?
          (and carrier-ok? (map? artifact)
               (= sh06-resolution-artifact-keys (set (keys artifact))))
          :resolution-boundary-map? (map? boundary)
          :exact-boundary-shape?
          (and (map? boundary)
               (= (sh06-resolution-boundary-keys-for boundary) (set (keys boundary))))}
         shape-failed
         (->> shape-checks
              (keep (fn [[key passed?]] (when-not passed? key)))
              vec)
         component-validations
         (when (empty? shape-failed)
           (sh06-resolution-component-validations artifact))
         components-ok?
         (and component-validations
              (every? #(= :passed (:status %))
                      (vals component-validations)))
         preflight-checks
         (assoc shape-checks
                :trusted-semantic-components? (boolean components-ok?))
         preflight-failed
         (->> preflight-checks
              (keep (fn [[key passed?]] (when-not passed? key)))
              vec)]
     (if (empty? preflight-failed)
       (if legacy?
         (sh06-resolution-artifact-verification-bounded*
          artifact construction?)
         (sh06-resolution-artifact-verification-bounded*
          artifact construction? context))
       {:artifact :gravity/sh06-resolution-artifact-verification
        :status :failed
        :checks preflight-checks
        :failed-checks preflight-failed
        :source-path "<sh06-resolution-artifact>"
        :carrier-validation aggregate-validation
        :component-validations component-validations
        :gravity-verification nil
        :upstream-verification nil})))))

(defn sh06-resolution-contained-verification-failure
  [error]
  {:artifact :gravity/sh06-resolution-artifact-verification
   :status :failed
   :checks {:contained-host-resource-failure? false}
   :failed-checks [:contained-host-resource-failure?]
   :source-path "<sh06-resolution-artifact>"
   :contained-host-error (.getName (class error))
   :gravity-verification nil
   :upstream-verification nil})

(defn- sh06-resolution-artifact-verification-contained
  ([artifact construction?]
   (sh06-resolution-artifact-verification-contained
    artifact construction? ::legacy))
  ([artifact construction? context]
  (try
    (if (= ::legacy context)
      (sh06-resolution-artifact-verification* artifact construction?)
      (sh06-resolution-artifact-verification* artifact construction? context))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh06-resolution-contained-verification-failure error))
    (catch OutOfMemoryError error
      (sh06-resolution-contained-verification-failure error))
    (catch AssertionError error
      (sh06-resolution-contained-verification-failure error))
    (catch LinkageError error
      (sh06-resolution-contained-verification-failure error))
    (catch Exception error
      (sh06-resolution-contained-verification-failure error)))))

;; Stage and seed dependencies qualify reuse without calling replaceable code.
;; The legitimate scoped binding remains an explicit epoch input.
(let
 [canonical-operations
  resolution-stage-defaults
  canonical-binding-root
  ^{:line 42, :column 34}
  (.getRawRoot #'*sh06-resolution-operation-binding*)
  canonical-roots
  ^{:line 44, :column 11}
  (into
   {}
   ^{:line 44, :column 20}
   (map
    ^{:line 44, :column 25}
    (fn
     [v]
     [v ^{:line 44, :column 36} (.getRawRoot ^clojure.lang.Var v)]))
   ^{:line 45, :column 17}
   (concat
    ^{:line 45, :column 25}
    (vals ^{:line 45, :column 31} (ns-publics 'gravity.resolution))
    ^{:line 46, :column 25}
    (filter var? ^{:line 46, :column 38} (vals canonical-operations))
    [#'resolution-stage-operations
     #'resolution-stage-defaults
     #'resolution-stage-input
     #'sh06-resolution-cached-binding
     #'sh06-resolution-expected-source-byte-count
     #'sh06-resolution-expected-source-content-hash
     #'sh06-resolution-public-function-shapes
     #'sh06-resolution-source-relative-path
     #'known-source-profiles
     #'sh06-resolution-all-core-symbols
     #'sh06-resolution-special-symbols
     #'sh06-resolution-type-symbols
     #'sh06-resolution-boundary-keys
     #'sh06-resolution-envelope-stage
     #'sh06-resolution-sealed-artifact-kind]))]
 ^{:line 158, :column 1}
 (defn
  sh06-resolution-artifact-verification
  ^{:line 159, :column 3}
  ([artifact]
   ^{:line 160, :column 4}
   (let
    [source-path
     ^{:line 160, :column 22}
     (or
      ^{:line 160, :column 26}
      (get-in artifact [:provenance :source-path])
      "<sh06-verification>")
     plan-binding
     ^{:line 161, :column 23}
     (sh06-resolution-current-binding! source-path)]
    ^{:line 162, :column 5}
    (binding
     [*sh06-resolution-operation-binding* plan-binding]
     ^{:line 163, :column 6}
     (let
      [report
       ^{:line 164, :column 4}
       (sh07-proof-transaction-report
        :sh06
        :final
        {:verifier-root
         ^{:line 17, :column 4}
         (let
          [entries
           ^{:line 17, :column 18}
           (.iterator
            ^{:line 17, :column 29}
            (.entrySet ^java.util.Map canonical-roots))]
          ^{:line 18, :column 6}
          (if
           ^{:line 18, :column 10}
           (and
            ^{:line 18, :column 15}
            (identical?
             ^{:line 18, :column 27}
             (.getRawRoot #'resolution-stage-defaults)
             canonical-operations)
            ^{:line 19, :column 15}
            (identical?
             ^{:line 19, :column 27}
             (.getRawRoot #'*sh06-resolution-operation-binding*)
             canonical-binding-root)
            ^{:line 20, :column 15}
            (loop
             []
             ^{:line 21, :column 17}
             (if
              ^{:line 21, :column 21}
              (.hasNext entries)
              ^{:line 22, :column 19}
              (let
               [entry
                ^{:line 22, :column 31}
                (.next entries)
                v
                ^{:line 22, :column 49}
                (.getKey ^java.util.Map$Entry entry)]
               ^{:line 23, :column 21}
               (if
                ^{:line 23, :column 25}
                (and
                 ^{:line 23, :column 30}
                 (identical?
                  ^{:line 23, :column 42}
                  (.getValue ^java.util.Map$Entry entry)
                  ^{:line 24, :column 42}
                  (.getRawRoot ^clojure.lang.Var v))
                 ^{:line 25, :column 30}
                 (nil?
                  ^{:line 25, :column 36}
                  (.getThreadBinding ^clojure.lang.Var v)))
                ^{:line 26, :column 23}
                (recur)
                false))
              true)))
           ^{:line 27, :column 8}
           (.get #'sh06-resolution-artifact-verification-contained)
           ^{:line 28, :column 8}
           (Object.))),
         :report-schema-version 1,
         :check-catalog-domain :gravity/sh06-final-verification-checks,
         :source-content-hash
         ^{:line 170, :column 27}
         (:source-content-hash plan-binding),
         :plan-semantic-hash
         ^{:line 171, :column 26}
         (:plan-semantic-hash plan-binding),
         :functions-semantic-hash
         ^{:line 173, :column 6}
         (:functions-semantic-hash plan-binding),
         :reader-invocation
         ^{:line 174, :column 25}
         (sh06-resolution-reader-invocation),
         :resolution-operation-binding plan-binding}
        artifact
        (fn*
         []
         ^{:line 176, :column 6}
         (sh06-resolution-artifact-verification-contained
          artifact
          false)))]
      ^{:line 177, :column 7}
      (sh06-resolution-binding-current! source-path plan-binding)
      report))))
  ^{:line 179, :column 3}
  ([artifact context]
   ^{:line 180, :column 4}
   (let
    [validation
     ^{:line 180, :column 21}
     (sh06-resolution-context-validation artifact context)]
    ^{:line 181, :column 6}
    (if-not
     ^{:line 181, :column 14}
     (= :passed ^{:line 181, :column 25} (:status validation))
     ^{:line 182, :column 8}
     (assoc
      ^{:line 182, :column 15}
      (sh06-resolution-contained-verification-failure
       ^{:line 183, :column 16}
       (ex-info
        "SH-06 context validation failed"
        {:id "C5-UNRESOLVED"}))
      :context-validation
      validation)
     ^{:line 186, :column 8}
     (sh07-proof-transaction-report
      :sh06
      :final
      {:source-content-hash
       sh06-resolution-expected-source-content-hash,
       :resolution-operation-binding
       *sh06-resolution-operation-binding*,
       :external-binding-authority-id
       ^{:line 200, :column 10}
       (:external-binding-authority-id validation),
       :resolution-plan-authority-id
       ^{:line 198, :column 10}
       (:resolution-plan-authority-id validation),
       :reader-invocation
       ^{:line 201, :column 29}
       (sh06-resolution-reader-invocation),
       :functions-semantic-hash
       sh06-resolution-expected-functions-semantic-hash,
       :report-schema-version 1,
       :verifier-root
       ^{:line 17, :column 4}
       (let
        [entries
         ^{:line 17, :column 18}
         (.iterator
          ^{:line 17, :column 29}
          (.entrySet ^java.util.Map canonical-roots))]
        ^{:line 18, :column 6}
        (if
         ^{:line 18, :column 10}
         (and
          ^{:line 18, :column 15}
          (identical?
           ^{:line 18, :column 27}
           (.getRawRoot #'resolution-stage-defaults)
           canonical-operations)
          ^{:line 19, :column 15}
          (identical?
           ^{:line 19, :column 27}
           (.getRawRoot #'*sh06-resolution-operation-binding*)
           canonical-binding-root)
          ^{:line 20, :column 15}
          (loop
           []
           ^{:line 21, :column 17}
           (if
            ^{:line 21, :column 21}
            (.hasNext entries)
            ^{:line 22, :column 19}
            (let
             [entry
              ^{:line 22, :column 31}
              (.next entries)
              v
              ^{:line 22, :column 49}
              (.getKey ^java.util.Map$Entry entry)]
             ^{:line 23, :column 21}
             (if
              ^{:line 23, :column 25}
              (and
               ^{:line 23, :column 30}
               (identical?
                ^{:line 23, :column 42}
                (.getValue ^java.util.Map$Entry entry)
                ^{:line 24, :column 42}
                (.getRawRoot ^clojure.lang.Var v))
               ^{:line 25, :column 30}
               (nil?
                ^{:line 25, :column 36}
                (.getThreadBinding ^clojure.lang.Var v)))
              ^{:line 26, :column 23}
              (recur)
              false))
            true)))
         ^{:line 27, :column 8}
         (.get #'sh06-resolution-artifact-verification-contained)
         ^{:line 28, :column 8}
         (Object.))),
       :sh06-context-root
       ^{:line 196, :column 29}
       (:context-root validation),
       :check-catalog-domain :gravity/sh06-final-verification-checks,
       :plan-semantic-hash sh06-resolution-expected-plan-semantic-hash}
      artifact
      (fn*
       []
       ^{:line 203, :column 10}
       (sh06-resolution-artifact-verification-contained
        artifact
        false
        context))))))
  ^{:line 205, :column 3}
  ([artifact context invocation]
   ^{:line 206, :column 4}
   (if
    ^{:line 206, :column 8}
    (nil? context)
    ^{:line 207, :column 6}
    (sh06-resolution-artifact-verification artifact context)
    ^{:line 208, :column 6}
    (sh06-resolution-with-reader-invocation
     ^{:line 209, :column 7}
     (or
      ^{:line 209, :column 11}
      (get-in artifact [:provenance :source-path])
      "<sh06-verification>")
     invocation
     (fn*
      []
      ^{:line 210, :column 8}
      (if
       ^{:line 210, :column 12}
       (= :gravity.bootstrap/legacy context)
       ^{:line 211, :column 10}
       (sh06-resolution-artifact-verification artifact)
       ^{:line 212, :column 10}
       (sh06-resolution-artifact-verification artifact context))))))))

(defn- sh06-resolution-capability-based-proof-for-construction
  ([artifact]
   (sh06-resolution-capability-based-proof-for-construction
    artifact ::legacy))
  ([artifact context]
  (let [report
        (if (= ::legacy context)
          (sh06-resolution-artifact-verification-contained artifact true)
          (sh06-resolution-artifact-verification-contained
           artifact true context))]
    (cond->
     (assoc (:checks report)
            :artifact :gravity/sh06-resolution-capability-proof
            :status (if (= :passed (:status report)) :complete :failed)
            :failed-checks (:failed-checks report))
      (and (= :failed (:status report))
           (:preflight-observations report))
      (assoc :preflight-observations
             (:preflight-observations report))
      (and (= :failed (:status report))
           (:carrier-validation report))
      (assoc :carrier-validation
             (:carrier-validation report))))))

(defn sh06-resolution-capability-based-proof
  [artifact]
  (let [report (sh06-resolution-artifact-verification artifact)]
    (assoc (:checks report)
           :artifact :gravity/sh06-resolution-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report))))
