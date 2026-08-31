; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-class-jar
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
   state
   class-path
   (.toPath (java.io.File. (:class-file staged-paths)))
   module-class-path
   (.toPath (java.io.File. (:module-class staged-paths)))
   class-major
   (jvm-backend-classfile-major class-path)
   _
   (when-not
    (and
     (= jvm-backend-required-classfile-major class-major)
     (= jvm-backend-required-classfile-major (jvm-backend-classfile-major module-class-path)))
    (jvm-backend-fail!
     "B5-MANIFEST"
     "generated JVM classfile version is invalid"
     source-path
     nil
     {:observed-classfile-major class-major,
      :required-classfile-major jvm-backend-required-classfile-major,
      :missing-fact :classfile-major-65}))
   _
   (jvm-backend-write-deterministic-jar!
    (.toPath (java.io.File. (:jar staged-paths)))
    (.toPath (java.io.File. (str stage-directory "/classes"))))
   jar-record
   (jvm-backend-jar-record (.toPath (java.io.File. (:jar staged-paths))))
   expected-entries
   ["META-INF/MANIFEST.MF" "gravity/stage2/Program.class" "module-info.class"]
   _
   (when-not
    (and
     (= expected-entries (:entries jar-record))
     (= jvm-backend-main-class (:main-class jar-record))
     (= jvm-backend-module-name (:module-name jar-record)))
    (jvm-backend-fail!
     "B5-MANIFEST"
     "generated JVM JAR structure is invalid"
     source-path
     nil
     {:observed-jar jar-record,
      :expected-entries expected-entries,
      :missing-fact :modular-executable-jar}))]
  (clojure.core/assoc
   state
   :class-path
   class-path
   :module-class-path
   module-class-path
   :class-major
   class-major
   :jar-record
   jar-record
   :expected-entries
   expected-entries)))
