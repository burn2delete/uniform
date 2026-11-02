

(defn jvm-backend-value-source
  [instruction counter indent env]
  (gravity.backend.jvm/value-source #'jvm-backend-value-declaration #'jvm-backend-write-source #'jvm-backend-test-source #'jvm-backend-value-source instruction counter indent env))

(defn jvm-backend-instruction-source
  ([instruction counter indent]
   (gravity.backend.jvm/instruction-source #'jvm-backend-byte-array-source #'jvm-backend-write-source #'jvm-backend-value-source #'jvm-backend-test-source #'jvm-backend-value-declaration #'jvm-backend-instruction-source instruction counter indent))
  ([instruction counter indent env]
   (gravity.backend.jvm/instruction-source #'jvm-backend-byte-array-source #'jvm-backend-write-source #'jvm-backend-value-source #'jvm-backend-test-source #'jvm-backend-value-declaration #'jvm-backend-instruction-source instruction counter indent env)))

(defn jvm-backend-java-source
  [plan]
  (gravity.backend.jvm/java-source #'jvm-backend-instruction-source plan))

(def jvm-backend-module-source (gravity.backend.jvm/module-source jvm-backend-module-name))

(defn jvm-backend-source-map
  [source-text java-source]
  (gravity.backend.jvm/source-map #'sha256-hex source-text java-source))

(defn jvm-backend-output-paths
  [output-path]
  (gravity.backend.jvm/output-paths output-path))

(defn
 jvm-backend-run-process!
 [command source-path diagnostic-id message]
 (gravity.backend.jvm/run-process!
  #'jvm-backend-fail!
  command
  source-path
  diagnostic-id
  message))

(defn
 jvm-backend-tool-version!
 [command source-path]
 (gravity.backend.jvm/tool-version!
  #'jvm-backend-run-process!
  #'jvm-backend-fail!
  #'jvm-backend-target-release
  command
  source-path))

(defn
 jvm-backend-write-file!
 [path content]
 (gravity.backend.jvm/write-file! path content))
