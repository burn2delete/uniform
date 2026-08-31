

(defn- c2-source-identity-ops
  []
  {:sha256-hex sha256-hex
   :reader-canonical-hash reader-canonical-hash
   :gravity-source-extension gravity-source-extension
   :gravity-source-kind gravity-source-kind
   :reader-normalize-relative-path reader-normalize-relative-path
   :reader-platform-neutral-absolute-path?
   reader-platform-neutral-absolute-path?
   :reader-valid-project-relative-path? reader-valid-project-relative-path?
   :reader-explicit-project-context reader-explicit-project-context
   :reader-valid-options? reader-valid-options?
   :reader-validate-options! reader-validate-options!
   :reader-project-root-record reader-project-root-record
   :reader-source-identity-inputs reader-source-identity-inputs
   :c2-source-unit-record c2-source-unit-record
   :c2-token-record c2-token-record
   :c2-form-record c2-form-record
   :c2-literal-records c2-literal-records
   :c2-trivia-records c2-trivia-records})