; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-ingress
 [state]
 (clojure.core/let
  [{:keys [record context]}
   state
   _
   (p15-s23-reference-runtime-bounded-value!
    (:runtime-artifact-source-path context)
    :jvm
    :runtime-target-record
    record
    p15-s23-reference-runtime-max-rule-nodes
    p15-s23-reference-runtime-max-contract-depth)
   _
   (p15-s23-reference-runtime-bounded-value!
    (:runtime-artifact-source-path context)
    :jvm
    :runtime-target-context
    context
    p15-s23-reference-runtime-max-contract-nodes
    p15-s23-reference-runtime-max-contract-depth)
   digest?
   (fn*
    [p1__1186#]
    (and (string? p1__1186#) (boolean (re-matches #"sha256:[0-9a-f]{64}" p1__1186#))))
   context-envelope-valid?
   (and
    (map? context)
    (=
     #{:runtime-artifact-source-path
       :runtime-decision-record-ids
       :stdout-hash
       :plan-id
       :runtime-action-record-ids
       :entrypoint
       :source-id
       :runtime-adapter-record
       :runtime-adapter-record-hash}
     (set (keys context)))
    (digest? (:plan-id context))
    (digest? (:source-id context))
    (symbol? (:entrypoint context))
    (digest? (:stdout-hash context))
    (string? (:runtime-artifact-source-path context))
    (digest? (:runtime-adapter-record-hash context))
    (map? (:runtime-adapter-record context))
    (vector? (:runtime-decision-record-ids context))
    (vector? (:runtime-action-record-ids context)))
   expected-record-hash
   (p15-s23-reference-runtime-hash (dissoc record :record-hash :actual-path-binding))
   expected-actual-path-binding-base
   {:actual-path (:runtime-artifact-source-path context),
    :runtime-artifact-hash (:runtime-artifact-hash record),
    :runtime-source-content-hash p15-s23-stage2-runtime-artifact-expected-source-content-hash}
   expected-actual-path-binding
   (assoc
    expected-actual-path-binding-base
    :binding-hash
    (p15-s23-reference-runtime-hash expected-actual-path-binding-base))
   runtime-source-file
   (when context-envelope-valid? (java.io.File. (:runtime-artifact-source-path context)))
   runtime-source-file-valid?
   (and
    runtime-source-file
    (.isAbsolute runtime-source-file)
    (.isFile runtime-source-file)
    (= (.getCanonicalPath runtime-source-file) (:runtime-artifact-source-path context))
    (= (.length runtime-source-file) p15-s23-stage2-runtime-artifact-expected-source-byte-count))
   runtime-source-file-hash
   (when
    runtime-source-file-valid?
    (str
     "sha256:"
     (sha256-bytes-hex (java.nio.file.Files/readAllBytes (.toPath runtime-source-file)))))
   contract-binding
   (:runtime-contract-binding record)]
  (clojure.core/assoc
   state
   :digest?
   digest?
   :context-envelope-valid?
   context-envelope-valid?
   :expected-record-hash
   expected-record-hash
   :expected-actual-path-binding-base
   expected-actual-path-binding-base
   :expected-actual-path-binding
   expected-actual-path-binding
   :runtime-source-file
   runtime-source-file
   :runtime-source-file-valid?
   runtime-source-file-valid?
   :runtime-source-file-hash
   runtime-source-file-hash
   :contract-binding
   contract-binding)))
