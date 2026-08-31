

(defn reader-normalize-relative-path
  [path]
  (c2-source-identity-call
   :reader-normalize-relative-path
   c2-source-identity/reader-normalize-relative-path path))

(defn reader-platform-neutral-absolute-path?
  [path]
  (c2-source-identity-call
   :reader-platform-neutral-absolute-path?
   c2-source-identity/reader-platform-neutral-absolute-path? path))

(defn reader-valid-project-relative-path?
  [path]
  (c2-source-identity-call
   :reader-valid-project-relative-path?
   c2-source-identity/reader-valid-project-relative-path? path))

(defn reader-explicit-project-context
  [project-context]
  (c2-source-identity-call
   :reader-explicit-project-context
   c2-source-identity/reader-explicit-project-context project-context))

(defn reader-valid-options?
  [reader-options]
  (c2-source-identity-call
   :reader-valid-options?
   c2-source-identity/reader-valid-options? reader-options))

(defn reader-validate-options!
  [reader-options]
  (c2-source-identity-call
   :reader-validate-options!
   c2-source-identity/reader-validate-options! reader-options))

(defn reader-project-root-record
  [project-context]
  (c2-source-identity-call
   :reader-project-root-record
   c2-source-identity/reader-project-root-record project-context))

(defn reader-source-identity-inputs
  [source-text reader-options project-context]
  (c2-source-identity-call
   :reader-source-identity-inputs
   c2-source-identity/reader-source-identity-inputs
   source-text reader-options project-context))