; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-node-parity
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     target
     output-path
     emit?
     node-version
     packet
     compiler-artifact-record
     compiler-artifact-source-path
     driver-record
     runtime-record
     runtime-rule
     driver-rule
     closed-plan-runtime
     closed-runtime-context
     plan
     plan-hash
     javascript
     writes-stdout?
     source-map
     package-metadata
     js-hash
     declaration-hash
     source-map-hash
     package-hash
     source-hash
     expected-output
     expected-bytes]}
   state
   temp-directory
   (java.nio.file.Files/createTempDirectory
    "gravity-js-ts-validate-"
    (make-array java.nio.file.attribute.FileAttribute 0))
   temp-module
   (.resolve temp-directory "program.mjs")
   _
   (java.nio.file.Files/write
    temp-module
    (.getBytes javascript java.nio.charset.StandardCharsets/UTF_8)
    (into-array
     java.nio.file.OpenOption
     [java.nio.file.StandardOpenOption/CREATE_NEW java.nio.file.StandardOpenOption/WRITE]))
   _
   (js-ts-backend-run-node-process!
    ["--check" (.toString temp-module)]
    source-path
    "B6-TARGET"
    "Node rejected generated ES2022 ESM")
   execution
   (js-ts-backend-run-node-process!
    [(.toString temp-module)]
    source-path
    "B14-DIFFERENTIAL"
    "generated JS/TS execution failed")
   _
   (java.nio.file.Files/deleteIfExists temp-module)
   _
   (java.nio.file.Files/deleteIfExists temp-directory)
   _
   (when-not
    (= expected-bytes (:stdout-bytes execution))
    (js-ts-backend-fail!
     "B14-DIFFERENTIAL"
     "JS/TS execution differs from the authoritative stage2 runtime"
     source-path
     nil
     {:expected-stdout-hash (str "sha256:" (sha256-hex expected-output)),
      :actual-stdout-hash
      (str "sha256:" (sha256-bytes-hex (byte-array (map byte (:stdout-bytes execution))))),
      :missing-fact :stage2-js-execution-equivalence}))]
  (clojure.core/assoc
   state
   :temp-directory
   temp-directory
   :temp-module
   temp-module
   :execution
   execution)))
