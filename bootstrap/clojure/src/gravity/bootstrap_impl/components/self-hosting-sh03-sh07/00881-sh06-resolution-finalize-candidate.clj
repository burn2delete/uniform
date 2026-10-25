

(defn- sh06-resolution-finalize-candidate
  ([source-path artifact-with-envelope]
   (sh06-resolution-finalize-candidate
    source-path artifact-with-envelope ::legacy))
  ([source-path artifact-with-envelope context]
  (let [proof
        (if (= ::legacy context)
          (sh06-resolution-capability-based-proof-for-construction
           artifact-with-envelope)
          (do
            (when (nil? context)
              (sh06-resolution-c5-fail! source-path :missing-context {}))
            (sh06-resolution-capability-based-proof-for-construction
             artifact-with-envelope context)))
        artifact
        (assoc artifact-with-envelope :capability-based-proof proof)]
    ;; The construction report above already replayed the exact authenticated
    ;; request and every stored run product.  The only post-construction change
    ;; is installing that report as the embedded proof, so a second identical
    ;; Gravity replay here would add cost without checking a new semantic fact.
    (when-not (and (= :complete (:status proof))
                   (empty? (:failed-checks proof))
                   (= proof (:capability-based-proof artifact)))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path
       :final-authenticated-resolution-artifact
       (cond-> {:failed-checks (:failed-checks proof)}
         (:preflight-observations proof)
         (assoc :preflight-observations
                (:preflight-observations proof))
         (:carrier-validation proof)
         (assoc :carrier-validation
                (:carrier-validation proof)))
       {}))
    artifact)))

(defn sh06-resolution-source-artifact
  ([source-path source-text]
   (sh06-resolution-source-artifact source-path source-text
    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}))
  ([source-path source-text module-inputs]
  (let [plan-binding (sh06-resolution-current-binding! source-path)]
   (binding [*sh06-resolution-operation-binding* plan-binding]
  (with-sh06-resolution-transport-bounds
   (let [artifact (sh06-resolution-finalize-candidate
    source-path
    (sh06-resolution-source-artifact-candidate
     source-path
     (sh05-macro-source-artifact source-path source-text)
     true ::legacy module-inputs))]
     (sh06-resolution-binding-current! source-path plan-binding)
     artifact))))))

(defn sh06-resolution-artifact-from-sh05-artifact
  "Resolve an already authenticated SH-05 artifact without rebuilding C3/SH-05.

  The one-argument form derives the source path from the authenticated parent;
  callers with a path-scoped snapshot should use the two-argument form so the
  parent provenance is joined to that exact path.
  "
  ([sh05-artifact]
   (sh06-resolution-artifact-from-sh05-artifact
    (get-in sh05-artifact [:provenance :source-path]) sh05-artifact))
  ([source-path sh05-artifact]
   (let [plan-binding (sh06-resolution-current-binding! source-path)]
    (binding [*sh06-resolution-operation-binding* plan-binding]
   (with-sh06-resolution-transport-bounds
    (let [artifact (sh06-resolution-finalize-candidate
     source-path
     (sh06-resolution-source-artifact-candidate
      source-path sh05-artifact false))]
      (sh06-resolution-binding-current! source-path plan-binding)
      artifact)))))
  ([source-path sh05-artifact context]
   (when (nil? context)
     (sh06-resolution-c5-fail! source-path :missing-context {}))
   (with-sh06-resolution-transport-bounds
    (sh06-resolution-finalize-candidate
     source-path
     (sh06-resolution-source-artifact-candidate
      source-path sh05-artifact false context)
     context)))
  ([source-path sh05-artifact module-inputs reader-source-path]
   (let [before (sh03-reader-read-selected-source-bytes! source-path reader-source-path)
         plan-binding (sh06-resolution-current-binding! source-path)
         identity-keys [:source-content-hash :source-byte-count]]
     (binding [*sh06-resolution-operation-binding* plan-binding]
       (with-sh06-resolution-transport-bounds
         (let [candidate (sh06-resolution-source-artifact-candidate
                          source-path sh05-artifact false ::legacy module-inputs)
               retained (get-in candidate [:sh05-macro-artifact :gravity-macro-boundary
                                          :authenticated-sh04-artifact :c2-reader-artifact
                                          :sh03-reader-authentication :reader-source-revision])
               artifact (sh06-resolution-finalize-candidate source-path candidate)
               after (sh03-reader-read-selected-source-bytes! source-path reader-source-path)]
           (when-not (and (map? retained)
                          (contains? (get-in retained [:function-shapes sh03-reader-verifier]) :semantic-hash)
                          (= (select-keys before identity-keys) (select-keys after identity-keys)
                             (select-keys retained identity-keys)))
             (sh06-resolution-boundary-fail!
              "C5-UNRESOLVED" source-path :selected-current-reader-parent
              (select-keys retained identity-keys) {:reason :current-reader-parent-revision-mismatch}))
           (sh06-resolution-binding-current! source-path plan-binding)
           artifact))))))

(defn sh06-resolution-file-artifact
  ([source-path]
   (sh06-resolution-file-artifact source-path
    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}))
  ([source-path module-inputs]
  (let [c2-artifact (compiler-c2-reader-file-artifact source-path)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)]
    (sh06-resolution-source-artifact source-path source-text module-inputs))))

(defn sh06-resolution-pr-str
  [_source-path artifact]
  (pr-str artifact))

(defn sh06-resolution-serialize
  [artifact]
  (let [report (sh06-resolution-artifact-verification artifact)]
    (when-not (= :passed (:status report))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED"
       (or (get-in artifact [:provenance :source-path])
           "<sh06-resolution-serialize>")
       :verified-resolution-artifact-before-serialization
       (:failed-checks report) {}))
    (let [source-path
          (or (get-in artifact [:provenance :source-path])
              "<sh06-resolution-serialize>")]
      (try
        (sh06-resolution-pr-str source-path artifact)
        (catch InterruptedException interrupted
          (.interrupt (Thread/currentThread))
          (throw interrupted))
        (catch StackOverflowError error
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" source-path
           :contained-resolution-serialization-resource
           {:contained-host-error (.getName (class error))}
           {:resource-failure? true}))
        (catch OutOfMemoryError error
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" source-path
           :contained-resolution-serialization-resource
           {:contained-host-error (.getName (class error))}
           {:resource-failure? true}))))))
