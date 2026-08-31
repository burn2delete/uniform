

(def p15-s23-b2-c17-gate-b-final-artifact-keys
  #{:artifact :schema-version :status :policy :gate-a-artifact
    :gate-a-contextual-report :toolchain-evidence :b13-record :b14-record
    :c18-record :diagnostics :whole-b2? :public? :release? :self-hosted?
    :seed-boundary? :clojure-seed-boundary?
    :semantic-id :artifact-id :actual-path-provenance
    :actual-path-binding-id :publication-receipt})

(def p15-s23-b2-c17-gate-b-option-keys
  #{:output-directory})

(def p15-s23-b2-c17-gate-b-policy
  {:artifact :gravity/b2-hosted-c17-gate-b-policy
   :schema-version 1
   :owner :gravity.backend/b2-c
   :tier :experimental
   :exposure :internal
   :profile :hosted
   :target :c
   :dialect :c17
   :target-triple "arm64-apple-macosx14.0.0"
   :maximum-tool-output-bytes 65536
   :maximum-emitted-file-bytes (* 8 1024 1024)
   :tool-timeout-ms 30000
   :maximum-captured-descendants 64
   :private-workspace-mode "0700"
   :publication-mode-policy
   {:directory "0755" :executable "0755" :nonexecutable "0644"}
   :publication-file-set
   ["program.c" "program.h" "program.o" "program"
    "manifest.edn" "provenance.edn" "conformance.edn"]
   :whole-process-tree-reaping-proved? false
   :whole-b2? false
   :public? false
   :release? false
   :self-hosted? false})

(def ^:private p15-s23-b2-c17-gate-b-developer-directory
  "/Library/Developer/CommandLineTools")

(def p15-s23-b2-c17-gate-b-environment-policy
  {:inherited-environment? false
   :fixed-values {"PATH" "/usr/bin:/bin:/usr/sbin:/sbin"
                  "LC_ALL" "C"
                  "LANG" "C"
                  "DEVELOPER_DIR"
                  p15-s23-b2-c17-gate-b-developer-directory}
   :private-physical-values ["HOME" "TMPDIR"]
   :forbidden-prefixes ["DYLD_" "CCC_" "LLVM_"]
   :forbidden-names ["SDKROOT" "MACOSX_DEPLOYMENT_TARGET" "CPATH"
                     "LIBRARY_PATH"]})

(def ^:private p15-s23-b2-c17-gate-b-private-directory-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE})

(def ^:private p15-s23-b2-c17-gate-b-directory-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE
    java.nio.file.attribute.PosixFilePermission/GROUP_READ
    java.nio.file.attribute.PosixFilePermission/GROUP_EXECUTE
    java.nio.file.attribute.PosixFilePermission/OTHERS_READ
    java.nio.file.attribute.PosixFilePermission/OTHERS_EXECUTE})

(def ^:private p15-s23-b2-c17-gate-b-nonexecutable-permissions
  #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
    java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
    java.nio.file.attribute.PosixFilePermission/GROUP_READ
    java.nio.file.attribute.PosixFilePermission/OTHERS_READ})

(def ^:private p15-s23-b2-c17-gate-b-executable-permissions
  p15-s23-b2-c17-gate-b-directory-permissions)