

(defn stage1-reader-runtime-image-from-definitions
  [reader-source-path definitions]
  (let [runtime-image
        (stage1-reader-runtime-image-literal-definition-value
         reader-source-path definitions
         'stage1-reader-runtime-image)
        diagnostics (:diagnostics runtime-image)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint
                 :unsupported-runtime-image-operation
                 :missing-runtime-image-record
                 :filesystem-authority-divergence
                 :stdout-routing-divergence
                 :runtime-image-provenance-gap
                 :illegal-os-boundary-fallback
                 :invalid-runtime-image])
        required-stages
        [:stage1-runtime-image-load
         :stage1-runtime-image-install-entrypoint
         :stage1-runtime-image-mount-source
         :stage1-runtime-image-execute-entrypoint
         :stage1-runtime-image-route-stdout
         :stage1-runtime-image-emit-artifact
         :stage1-runtime-image-record-machine-boundary]
        direct-stages (:direct-stages runtime-image)]
    (when-not (map? runtime-image)
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG003" reader-source-path runtime-image
       {:missing-fields [:stage1-reader-runtime-image]}))
    (doseq [field [:engine :entrypoint :replaces :runtime-entrypoint
                   :input :output :artifact :diagnostic-stream
                   :proof-kind :os-boundaries :machine-boundaries
                   :image-fallbacks :runtime-image-operations
                   :direct-stages :uses-runtimes :uses-builtins
                   :uses-executors :preserves :diagnostics :provenance]]
      (when-not (contains? runtime-image field)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG008" reader-source-path runtime-image
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-runtime-image-v1 (:engine runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-runtime-image
                 (:entrypoint runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:entrypoint]}))
    (when-not (set/subset? #{:os-process-launch
                             :os-filesystem-read
                             :stdout-stream}
                           (set (:replaces runtime-image)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-runtime-entrypoint
                 (:runtime-entrypoint runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:runtime-entrypoint]}))
    (when-not (= [:runtime-image :source-path :source-text]
                 (:input runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records
                 (:output runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:output]}))
    (when-not (= :gravity/stage1-reader-runtime-image-artifact
                 (:artifact runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG004" reader-source-path runtime-image
       {:missing-fields [:artifact]}))
    (when-not (= :gravity/stage1-reader-runtime-image-diagnostic-stream
                 (:diagnostic-stream runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:diagnostic-stream]}))
    (when-not (= :gravity/stage1-reader-runtime-image-proof
                 (:proof-kind runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG006" reader-source-path runtime-image
       {:missing-fields [:proof-kind]}))
    (when-not (= [] (:os-boundaries runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG007" reader-source-path runtime-image
       {:os-boundaries (:os-boundaries runtime-image)}))
    (when-not (set/subset? #{:machine-instruction-dispatch
                             :kernel-process-scheduler
                             :artifact-loader}
                           (set (:machine-boundaries runtime-image)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG006" reader-source-path runtime-image
       {:missing-fields [:machine-boundaries]}))
    (when-not (= [] (:image-fallbacks runtime-image))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG007" reader-source-path runtime-image
       {:image-fallbacks (:image-fallbacks runtime-image)}))
    (let [runtime-image-operations (:runtime-image-operations runtime-image)
          operation-names (set runtime-image-operations)
          required-operation-set
          (set stage1-reader-runtime-image-required-operations)]
      (when-not (vector? runtime-image-operations)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG003" reader-source-path runtime-image
         {:missing-fields [:runtime-image-operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG002" reader-source-path runtime-image-operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-runtime-image-required-operations))})))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? #{:stage1-reader-runtime-image
                             :stage1-reader-runtime-entrypoint
                             :stage1-reader-compiler-driver
                             :stage1-reader-core-bootstrap-runtime
                             :stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes runtime-image)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins runtime-image)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors runtime-image)))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path runtime-image
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source
                 (get-in runtime-image [:provenance :owner]))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG006" reader-source-path runtime-image
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-runtime-image-os-boundary-replacement
                 (get-in runtime-image [:provenance :purpose]))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG006" reader-source-path runtime-image
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-os-process-filesystem-and-stdout-boundaries-with-bootstrapped-runtime-image
                 (get-in runtime-image
                         [:provenance :retirement-objective]))
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG006" reader-source-path runtime-image
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG008" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc runtime-image
           :runtime-image-id
           (str "sha256:" (sha256-hex (pr-str runtime-image))))))

(defn stage1-reader-runtime-image-entrypoint-valid?
  [definitions]
  (let [definition (get definitions stage1-reader-runtime-image-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-runtime-image
               source-path
               source-text
               stage1-reader-runtime-image
               stage1-reader-runtime-entrypoint))
            (:body definition)))))