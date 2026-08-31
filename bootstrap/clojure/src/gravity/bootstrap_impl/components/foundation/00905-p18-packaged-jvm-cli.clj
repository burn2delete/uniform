

(defn p18-packaged-jvm-cli?
  []
  (= "true" (System/getProperty "gravity.packaged.jvm.cli")))

(defn p18-cli-version-record
  []
  (cli/p18-cli-version-record (p18-packaged-jvm-cli?)))

(defn p18-cli-help-text
  []
  (cli/p18-cli-help-text (p18-packaged-jvm-cli?)))

(defn p18-seedless-overclaim!
  []
  (if (p18-packaged-jvm-cli?)
    (fail! "P18T02001"
           "packaged JVM CLI is bootstrap-hosted and cannot be claimed as the seedless release artifact"
           {:source-span {:source "bin/gravity"}
            :phase "P18-T02"
            :bootstrap-hosted? true
            :packaged-jvm-cli? true
            :seedless-release? false
            :remediation "Complete P18-T03 through P18-T05 before claiming the public gravity command is seedless."})
    (fail! "P18T01001"
           "thin CLI wrapper is bootstrap-hosted and cannot be claimed as the seedless release artifact"
           {:source-span {:source "bin/gravity"}
            :phase "P18-T01"
            :bootstrap-hosted? true
            :seedless-release? false
            :remediation "Complete P18-T03 and P18-T05 before claiming the public gravity command is seedless."})))

(defn p18-ensure-dir!
  [path]
  (.mkdirs (java.io.File. path)))

(defn p18-file-bytes
  [path]
  (java.nio.file.Files/readAllBytes
   (.toPath (java.io.File. path))))

(defn p18-bytes-sha256
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn p18-file-sha256
  [path]
  (if (.isFile (java.io.File. path))
    (str "sha256:" (p18-bytes-sha256 (p18-file-bytes path)))
    "sha256:missing"))