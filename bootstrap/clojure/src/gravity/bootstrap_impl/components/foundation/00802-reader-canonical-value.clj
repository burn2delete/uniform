

(defn reader-canonical-value
  [value]
  (c2-artifact-identity-call
   :reader-canonical-value
   c2-artifact-identity/reader-canonical-value value))

(defn reader-canonical-hash
  [value]
  (c2-artifact-identity-call
   :reader-canonical-hash
   c2-artifact-identity/reader-canonical-hash value))
(def standard-reader-policy
  {:policy :gravity/standard-reader
   :version 1
   :registered-tags ['inst 'uuid]
   :ambient-authority :denied})

(defn reader-project-root-path
  [source-path]
  (let [source-file (.getCanonicalFile (java.io.File. source-path))
        start (if (.isDirectory source-file)
                source-file
                (.getParentFile source-file))]
    (or (loop [candidate start]
          (when candidate
            (if (.isFile (java.io.File. candidate "deps.edn"))
              candidate
              (recur (.getParentFile candidate)))))
        start
        (.getParentFile source-file))))

(defn reader-project-root-id
  [project-root-path]
  (let [manifest (java.io.File. project-root-path "deps.edn")]
    (if (.isFile manifest)
      (reader-canonical-hash
       {:project-manifest "deps.edn"
        :bytes-hash
        (str "sha256:"
             (sha256-bytes-hex
              (java.nio.file.Files/readAllBytes (.toPath manifest))))})
      (reader-canonical-hash {:project-root-kind :standalone-source-root}))))

(declare reader-normalize-relative-path)

(defn reader-project-context-for-source
  [source-path]
  (let [source-file (.getCanonicalFile (java.io.File. source-path))
        project-root (.getCanonicalFile
                      (reader-project-root-path source-path))
        relative-path (.relativize (.toPath project-root)
                                   (.toPath source-file))]
    {:project-root-id (reader-project-root-id project-root)
     :project-root-path (.getPath project-root)
     :project-relative-path (reader-normalize-relative-path relative-path)}))

(declare reader-normalize-relative-path
         reader-platform-neutral-absolute-path?
         reader-valid-project-relative-path?
         reader-explicit-project-context
         reader-valid-options?
         reader-validate-options!
         reader-project-root-record
         reader-source-identity-inputs
         c2-source-unit-record
         c2-token-record
         c2-form-record
         c2-literal-records
         c2-trivia-records)