(defn-
 c3-current-reader-identity-view
 [source-path c2-artifact]
 (gravity.syntax/current-reader-identity-view
  source-path
  c2-artifact
  (syntax-stage-reader-view-operations)))
(defn-
 c3-current-reader-revision-authentic?
 ([c2-artifact boundary]
  (c3-current-reader-revision-authentic? c2-artifact boundary nil))
 ([c2-artifact boundary invocation]
  (gravity.syntax/current-reader-revision-authentic?
   c2-artifact
   boundary
   invocation
   (syntax-stage-reader-view-operations))))
(defn
 c3-reader-artifact-view
 ([c2-artifact] (c3-reader-artifact-view c2-artifact nil))
 ([c2-artifact invocation]
  (gravity.syntax/reader-artifact-view
   c2-artifact
   invocation
   (syntax-stage-reader-view-operations))))

(declare c3-path-neutral-reader-artifact-view
         c3-path-neutral-syntax-object
         c3-gravity-syntax-boundary-identity-view
         c3-artifact-identity-input
         c3-artifact-id)

(defn- c3-artifact-identity-ops
  []
  {:c2-token-hash-input c2-token-hash-input
   :c2-form-hash-input c2-form-hash-input
   :c2-syntax-seed-hash-input c2-syntax-seed-hash-input
   :c2-extension-hash-input c2-extension-hash-input
   :c2-path-neutral-span c2-path-neutral-span
   :c3-path-neutral-origin c3-path-neutral-origin
   :reader-canonical-hash reader-canonical-hash
   :c3-path-neutral-reader-artifact-view c3-path-neutral-reader-artifact-view
   :c3-path-neutral-syntax-object c3-path-neutral-syntax-object
   :c3-gravity-syntax-boundary-identity-view
   c3-gravity-syntax-boundary-identity-view
   :c3-artifact-identity-input c3-artifact-identity-input
   :c3-artifact-id c3-artifact-id})
