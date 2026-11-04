

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
  (gravity.release/file-bytes path))

(defn release-execute-uniform-sha256
  "Execute current SHA source; host hashes below authenticate the plan only.
  This is a hosted invocation boundary, not migration or release evidence."
  [arguments]
  (let [relative "bootstrap/gravity/src/gravity/stdlib/sha256.gravity"
        file (p15-s23-sh07-governed-source-file relative)
        reject! (fn [reason]
                  (throw (ex-info "Uniform release SHA-256 binding is invalid"
                                  {:rule "SH20SHA002" :reason reason})))]
    (when-not file (reject! :missing-kernel-source))
    (let [path (.getCanonicalPath ^java.io.File file)
          source (java.nio.file.Files/readAllBytes (.toPath ^java.io.File file))
          text (p15-s23-sh07-b51-strict-utf8 source path)
          emitter (:emitter (c-backend-stage2-plan-emitter-source-rule! path :jvm))
          plan (p15-s23-stage2-compiler-artifact-plan emitter path text)
          expected-module
          '{:module gravity.stdlib.sha256 :profile :meta :target :jvm
            :effects #{:memory/allocate} :capabilities #{} :safety :safe
            :exports [sh22-sha256-policy sh22-sha256-pad sh22-sha256]}
          digest (fn [value]
                   (str "sha256:" (sha256-hex (pr-str (c-backend-canonical-value value)))))]
      (when-not (= (str "sha256:" (sha256-bytes-hex source))
                   (get-in plan [:source :sha256]))
        (reject! :kernel-source-plan-mismatch))
      (when-not (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
                     (true? (:compiler-artifact-plan? plan))
                     ;; The macro artifact omits exports from its module view.
                     ;; Check the complete ABI against the retained source, then
                     ;; check the exact projection the plan compiler emits.
                     (= expected-module
                        (select-keys
                         (parse-module path (mapv :form (read-source-form-records path text)))
                         (keys expected-module)))
                     (= (dissoc expected-module :exports)
                        (:module plan))
                     (= '{:arity 1 :params [arguments]}
                        (select-keys (get-in plan [:functions 'sh22-sha256])
                                     [:arity :params])))
        (reject! :kernel-abi-mismatch))
      (when-not (= (:plan-id plan) (digest (dissoc plan :plan-id)))
        (reject! :kernel-plan-id-mismatch))
      (when-not (java.util.Arrays/equals
                 ^bytes source
                 ^bytes (java.nio.file.Files/readAllBytes (.toPath ^java.io.File file)))
        (reject! :kernel-source-changed))
      (p15-s23-stage2-runtime-execute-function
       {:engine :gravity-sh22-sha256 :compiler-artifact-plan? true}
       plan 'sh22-sha256 [arguments]))))

(defn p18-bytes-sha256
  [bytes]
  (gravity.release/uniform-bytes-sha256 release-execute-uniform-sha256 bytes))

(defn p18-file-sha256
  [path]
  (gravity.release/file-sha256 #'p18-bytes-sha256 #'p18-file-bytes path))
