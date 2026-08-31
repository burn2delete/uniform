

(defn js-ts-backend-node-version!
  [source-path]
  (let [result (js-ts-backend-run-node-process!
                ["--version"] source-path "B6-TARGET"
                "Node runtime version check failed")
        version (str/trim
                 (String. (byte-array (map byte (:stdout-bytes result)))
                          java.nio.charset.StandardCharsets/UTF_8))]
    (when-not (re-matches #"v20(?:\..*)?" version)
      (js-ts-backend-fail!
       "B6-TARGET" "JS/TS backend requires Node 20"
       source-path nil
       {:observed-node-version version
        :required-node-version "20.x"
        :missing-fact :node20-runtime}))
    version))

(defn js-ts-backend-output-paths
  [output-path]
  ;; `-o` names an artifact directory for the multi-file JS/TS target.  Fixed
  ;; logical filenames keep sourceMappingURL and every emitted byte independent
  ;; of the checkout and caller-selected output path.
  {:javascript (str output-path "/program.mjs")
   :typescript-declarations (str output-path "/program.d.ts")
   :source-map (str output-path "/program.mjs.map")
   :package-metadata (str output-path "/package.json")
   :manifest (str output-path "/manifest.edn")
   :provenance (str output-path "/provenance.edn")})

(defn js-ts-backend-delete-tree!
  [path]
  (when (and path (.exists (.toFile path)))
    (doseq [file (reverse (file-seq (.toFile path)))]
      (.delete file))))

(defn js-ts-backend-stage-files!
  [output-path paths contents source-path]
  (when-not (c-backend-output-path-allowed? output-path)
    (js-ts-backend-fail!
     "C14-INPUT" "JS/TS artifact directory is outside declared roots"
     source-path nil
     {:output-path output-path
      :missing-fact :output-path-containment}))
  (let [output-directory (java.io.File. output-path)
        expected-parent (.getCanonicalPath output-directory)]
    (when (.exists output-directory)
      (js-ts-backend-fail!
       "C14-INPUT" "JS/TS backend requires a fresh artifact directory"
       source-path nil
       {:output-path output-path
        :missing-fact :fresh-output-path}))
    (doseq [[kind path] paths]
      (when-not (= expected-parent
                   (.getCanonicalPath (.getParentFile (java.io.File. path))))
        (js-ts-backend-fail!
         "C14-INPUT" "JS/TS sidecar escaped its artifact directory"
         source-path nil
         {:output-kind kind :output-path path
          :missing-fact :artifact-directory-containment})))
    (let [parent-file (or (.getParentFile output-directory)
                          (java.io.File. "."))]
      (when (and (.exists parent-file) (not (.isDirectory parent-file)))
        (js-ts-backend-fail!
         "C14-INPUT" "JS/TS artifact parent is not a directory"
         source-path nil
         {:output-path output-path
          :output-parent (.getPath parent-file)
          :missing-fact :output-parent-directory}))
      (when (and (not (.exists parent-file))
                 (not (.mkdirs parent-file)))
        (js-ts-backend-fail!
         "C14-INPUT" "JS/TS artifact parent directory is unavailable"
         source-path nil
         {:output-path output-path
          :output-parent (.getPath parent-file)
          :missing-fact :output-parent-directory}))
      (let [stage-directory
            (try
              (java.nio.file.Files/createTempDirectory
               (.toPath parent-file)
               ".gravity-js-ts-stage-"
               (make-array java.nio.file.attribute.FileAttribute 0))
              (catch Exception ex
                (js-ts-backend-fail!
                 "C14-INPUT"
                 "JS/TS staging directory could not be created"
                 source-path nil
                 {:output-path output-path
                  :output-parent (.getPath parent-file)
                  :cause-message (.getMessage ex)
                  :missing-fact :output-staging-directory})))]
        (try
        (doseq [[kind value] contents]
          (let [target-name (.getName (java.io.File. (get paths kind)))
                staged-path (.resolve stage-directory target-name)]
            (java.nio.file.Files/write
             staged-path
             (.getBytes (str value)
                        java.nio.charset.StandardCharsets/UTF_8)
             (into-array java.nio.file.OpenOption
                          [java.nio.file.StandardOpenOption/CREATE_NEW
                           java.nio.file.StandardOpenOption/WRITE]))))
        (let [staged-javascript
              (.toFile
               (.resolve stage-directory
                         (.getName (java.io.File. (:javascript paths)))))]
          (when-not (.setExecutable staged-javascript true false)
            (js-ts-backend-fail!
             "B6-MANIFEST"
             "staged JS/TS entrypoint could not be made executable"
             source-path nil
             {:missing-fact :executable-module-permission})))
        (try
          (java.nio.file.Files/move
           stage-directory
           (.toPath output-directory)
           (into-array java.nio.file.CopyOption
                       [java.nio.file.StandardCopyOption/ATOMIC_MOVE]))
          (catch Exception ex
            (js-ts-backend-fail!
             "B6-MANIFEST"
             "JS/TS artifact directory could not be atomically committed"
             source-path nil
             {:cause-message (.getMessage ex)
              :missing-fact :atomic-artifact-directory-commit})))
          (finally
            (js-ts-backend-delete-tree! stage-directory)))))))