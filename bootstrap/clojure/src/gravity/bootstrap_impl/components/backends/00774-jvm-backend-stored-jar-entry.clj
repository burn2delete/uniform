

(defn
 jvm-backend-stored-jar-entry!
 [jar-output entry-name bytes]
 (gravity.backend.jvm/stored-jar-entry! jar-output entry-name bytes))

(defn
 jvm-backend-write-deterministic-jar!
 [jar-path class-directory]
 (gravity.backend.jvm/write-jar!
  #'jvm-backend-stored-jar-entry!
  #'jvm-backend-main-class
  jar-path
  class-directory))

(defn
 jvm-backend-classfile-major
 [path]
 (gravity.backend.jvm/classfile-major path))

(defn
 jvm-backend-jar-record
 [jar-path]
 (gravity.backend.jvm/jar-record jar-path))

(defn
 jvm-backend-validate-content-hashes!
 [source-path manifest paths]
 (gravity.backend.jvm/validate-content-hashes!
  #'sha256-bytes-hex
  #'jvm-backend-fail!
  source-path
  manifest
  paths))
