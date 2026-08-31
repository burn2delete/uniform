; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-compile
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     output-path
     emit?
     output
     parent
     javac-version
     java-version
     packet
     trusted-emitter-rule
     trusted-driver-rule
     trusted-runtime-rule
     trusted-plan
     plan
     writes-stdout?
     java-source
     module-source
     source-map
     plan-hash
     source-hash
     compiler-record
     closed-plan-runtime
     stage-directory
     staged-paths]}
   state]
  (jvm-backend-write-file! (.toPath (java.io.File. (:java-source staged-paths))) java-source)
  (jvm-backend-write-file! (.toPath (java.io.File. (:module-source staged-paths))) module-source)
  (java.nio.file.Files/createDirectories
   (.toPath (java.io.File. (str stage-directory "/classes")))
   (make-array java.nio.file.attribute.FileAttribute 0))
  (jvm-backend-run-process!
   [*jvm-backend-javac-command*
    "--release"
    "21"
    "-encoding"
    "UTF-8"
    "-g:source,lines"
    "-proc:none"
    "-implicit:none"
    "-Werror"
    "-d"
    (str stage-directory "/classes")
    (:module-source staged-paths)
    (:java-source staged-paths)]
   source-path
   "B5-TARGET"
   "javac rejected generated Java 21 source")
  state))
