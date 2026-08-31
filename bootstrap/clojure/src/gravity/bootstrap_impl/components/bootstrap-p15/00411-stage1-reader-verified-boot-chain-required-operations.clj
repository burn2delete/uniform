

(def stage1-reader-verified-boot-chain-required-operations
  [:verify-boot-chain
   :load-runtime-image
   :activate-runtime-image
   :dispatch-machine-instructions
   :schedule-kernel-process
   :load-artifact
   :record-trust-anchor])

(defn stage1-reader-verified-boot-chain-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))

(defn stage1-reader-verified-boot-chain-from-definitions
  [reader-source-path definitions]
  (let [boot-chain
        (stage1-reader-verified-boot-chain-literal-definition-value
         reader-source-path definitions
         'stage1-reader-verified-boot-chain)
        diagnostics (:diagnostics boot-chain)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint
                 :unsupported-boot-operation
                 :missing-boot-chain-record
                 :artifact-loader-divergence
                 :scheduler-authority-divergence
                 :unreproducible-boot-provenance
                 :illegal-machine-kernel-fallback
                 :invalid-verified-boot-chain])
        required-stages
        [:stage1-boot-chain-verify
         :stage1-boot-chain-load-runtime-image
         :stage1-boot-chain-activate-runtime-image
         :stage1-boot-chain-dispatch-machine-instructions
         :stage1-boot-chain-schedule-kernel-process
         :stage1-boot-chain-load-artifact
         :stage1-boot-chain-record-trust-anchor]
        direct-stages (:direct-stages boot-chain)]
    (when-not (map? boot-chain)
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT003" reader-source-path boot-chain
       {:missing-fields [:stage1-reader-verified-boot-chain]}))
    (doseq [field [:engine :entrypoint :replaces :runtime-image
                   :input :output :artifact :diagnostic-stream
                   :proof-kind :machine-boundaries
                   :trust-anchor-boundaries :boot-chain-fallbacks
                   :boot-chain-operations :direct-stages
                   :uses-runtimes :uses-builtins :uses-executors
                   :preserves :diagnostics :provenance]]
      (when-not (contains? boot-chain field)
        (stage1-reader-verified-boot-chain-fail!
         "STAGE1BOOT008" reader-source-path boot-chain
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-verified-boot-chain-v1
                 (:engine boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-verified-boot-chain
                 (:entrypoint boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:entrypoint]}))
    (when-not (set/subset? #{:machine-instruction-dispatch
                             :kernel-process-scheduler
                             :artifact-loader}
                           (set (:replaces boot-chain)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-runtime-image
                 (:runtime-image boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:runtime-image]}))
    (when-not (= [:verified-boot-chain :runtime-image
                  :source-path :source-text]
                 (:input boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records
                 (:output boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:output]}))
    (when-not (= :gravity/stage1-reader-verified-boot-chain-artifact
                 (:artifact boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT004" reader-source-path boot-chain
       {:missing-fields [:artifact]}))
    (when-not (= :gravity/stage1-reader-verified-boot-chain-diagnostic-stream
                 (:diagnostic-stream boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:diagnostic-stream]}))
    (when-not (= :gravity/stage1-reader-verified-boot-chain-proof
                 (:proof-kind boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT006" reader-source-path boot-chain
       {:missing-fields [:proof-kind]}))
    (when-not (= [] (:machine-boundaries boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT007" reader-source-path boot-chain
       {:machine-boundaries (:machine-boundaries boot-chain)}))
    (when-not (set/subset? #{:hardware-reset-vector
                             :firmware-root-of-trust
                             :external-auditor-key}
                           (set (:trust-anchor-boundaries boot-chain)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT006" reader-source-path boot-chain
       {:missing-fields [:trust-anchor-boundaries]}))
    (when-not (= [] (:boot-chain-fallbacks boot-chain))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT007" reader-source-path boot-chain
       {:boot-chain-fallbacks (:boot-chain-fallbacks boot-chain)}))
    (let [boot-chain-operations (:boot-chain-operations boot-chain)
          operation-names (set boot-chain-operations)
          required-operation-set
          (set stage1-reader-verified-boot-chain-required-operations)]
      (when-not (vector? boot-chain-operations)
        (stage1-reader-verified-boot-chain-fail!
         "STAGE1BOOT003" reader-source-path boot-chain
         {:missing-fields [:boot-chain-operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-verified-boot-chain-fail!
         "STAGE1BOOT002" reader-source-path boot-chain-operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-verified-boot-chain-required-operations))})))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? #{:stage1-reader-verified-boot-chain
                             :stage1-reader-runtime-image
                             :stage1-reader-runtime-entrypoint
                             :stage1-reader-compiler-driver
                             :stage1-reader-core-bootstrap-runtime
                             :stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes boot-chain)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins boot-chain)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors boot-chain)))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path boot-chain
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source
                 (get-in boot-chain [:provenance :owner]))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT006" reader-source-path boot-chain
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-verified-boot-chain-machine-boundary-replacement
                 (get-in boot-chain [:provenance :purpose]))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT006" reader-source-path boot-chain
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-machine-kernel-and-artifact-loader-boundaries-with-verified-boot-chain
                 (get-in boot-chain
                         [:provenance :retirement-objective]))
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT006" reader-source-path boot-chain
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT008" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc boot-chain
           :verified-boot-chain-id
           (str "sha256:" (sha256-hex (pr-str boot-chain))))))

(defn stage1-reader-verified-boot-chain-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-verified-boot-chain-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-verified-boot-chain
               source-path
               source-text
               stage1-reader-verified-boot-chain
               stage1-reader-runtime-image))
            (:body definition)))))