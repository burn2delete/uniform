

(defn stage1-reader-runtime-entrypoint-from-definitions
  [reader-source-path definitions]
  (let [runtime-entrypoint
        (stage1-reader-runtime-entrypoint-literal-definition-value
         reader-source-path definitions
         'stage1-reader-runtime-entrypoint)
        diagnostics (:diagnostics runtime-entrypoint)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint :unsupported-runtime-operation
                 :missing-runtime-entrypoint-record
                 :source-routing-divergence :artifact-output-divergence
                 :process-exit-divergence :illegal-runner-fallback
                 :invalid-runtime-entrypoint])
        required-stages
        [:stage1-runtime-entrypoint-decode-command
         :stage1-runtime-entrypoint-open-source
         :stage1-runtime-entrypoint-deliver-source
         :stage1-runtime-entrypoint-execute-driver
         :stage1-runtime-entrypoint-route-artifact
         :stage1-runtime-entrypoint-map-exit]
        direct-stages (:direct-stages runtime-entrypoint)]
    (when-not (map? runtime-entrypoint)
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE003" reader-source-path runtime-entrypoint
       {:missing-fields [:stage1-reader-runtime-entrypoint]}))
    (doseq [field [:engine :entrypoint :replaces :compiler-driver
                   :input :output :artifact :diagnostic-stream
                   :proof-kind :os-boundaries :runner-fallbacks
                   :entrypoint-operations :direct-stages
                   :uses-runtimes :uses-builtins :uses-executors
                   :preserves :diagnostics :provenance]]
      (when-not (contains? runtime-entrypoint field)
        (stage1-reader-runtime-entrypoint-fail!
         "STAGE1RTE008" reader-source-path runtime-entrypoint
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-runtime-entrypoint-v1
                 (:engine runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-runtime-entrypoint
                 (:entrypoint runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:entrypoint]}))
    (when-not (set/subset? #{:clojure-driver-runner
                             :host-command-invocation
                             :host-file-read}
                           (set (:replaces runtime-entrypoint)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-compiler-driver
                 (:compiler-driver runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:compiler-driver]}))
    (when-not (= [:source-path :source-text] (:input runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records
                 (:output runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:output]}))
    (when-not (= :gravity/stage1-reader-runtime-entrypoint-artifact
                 (:artifact runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE005" reader-source-path runtime-entrypoint
       {:missing-fields [:artifact]}))
    (when-not (= :gravity/stage1-reader-runtime-entrypoint-diagnostic-stream
                 (:diagnostic-stream runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:diagnostic-stream]}))
    (when-not (= :gravity/stage1-reader-runtime-entrypoint-proof
                 (:proof-kind runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE005" reader-source-path runtime-entrypoint
       {:missing-fields [:proof-kind]}))
    (when-not (set/subset? #{:os-process-launch
                             :os-filesystem-read
                             :stdout-stream}
                           (set (:os-boundaries runtime-entrypoint)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:os-boundaries]}))
    (when-not (= [] (:runner-fallbacks runtime-entrypoint))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE007" reader-source-path runtime-entrypoint
       {:runner-fallbacks (:runner-fallbacks runtime-entrypoint)}))
    (let [entrypoint-operations (:entrypoint-operations runtime-entrypoint)
          operation-names (set entrypoint-operations)
          required-operation-set
          (set stage1-reader-runtime-entrypoint-required-operations)]
      (when-not (vector? entrypoint-operations)
        (stage1-reader-runtime-entrypoint-fail!
         "STAGE1RTE003" reader-source-path runtime-entrypoint
         {:missing-fields [:entrypoint-operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-runtime-entrypoint-fail!
         "STAGE1RTE002" reader-source-path entrypoint-operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-runtime-entrypoint-required-operations))})))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? #{:stage1-reader-runtime-entrypoint
                             :stage1-reader-compiler-driver
                             :stage1-reader-core-bootstrap-runtime
                             :stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes runtime-entrypoint)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins runtime-entrypoint)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors runtime-entrypoint)))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source
                 (get-in runtime-entrypoint [:provenance :owner]))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-runtime-entrypoint-host-runner-replacement
                 (get-in runtime-entrypoint [:provenance :purpose]))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-clojure-driver-runner-and-host-io-with-gravity-runtime-entrypoint
                 (get-in runtime-entrypoint
                         [:provenance :retirement-objective]))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path runtime-entrypoint
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE008" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc runtime-entrypoint
           :runtime-entrypoint-id
           (str "sha256:" (sha256-hex (pr-str runtime-entrypoint))))))

(defn stage1-reader-runtime-entrypoint-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-runtime-entrypoint-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-runtime-entrypoint
               source-path
               source-text
               stage1-reader-runtime-entrypoint
               stage1-reader-compiler-driver))
            (:body definition)))))

(defn stage1-reader-execute-runtime-entrypoint
  [reader-source-path
   definitions
   source-path
   source-text
   self-hosted-runtime
   core-bootstrap-runtime
   core-bootstrap-builtins
   compiler-driver
   runtime-entrypoint]
  (let [runtime-entrypoint-id (:runtime-entrypoint-id runtime-entrypoint)
        entrypoint-operations (:entrypoint-operations runtime-entrypoint)
        annotate-with-entrypoint
        (fn [record]
          (assoc record
                 :runtime-entrypoint-id runtime-entrypoint-id
                 :runtime-entrypoint-engine (:engine runtime-entrypoint)
                 :host-runner-replacement
                 :gravity-reader-runtime-entrypoint-v1))
        records
        (stage1-reader-execute-compiler-driver
         reader-source-path
         definitions
         source-path
         source-text
         self-hosted-runtime
         core-bootstrap-runtime
         core-bootstrap-builtins
         compiler-driver)
        annotated-records (mapv annotate-with-entrypoint records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (-> (or trace {})
                   (assoc :runtime-entrypoint runtime-entrypoint)
                   (assoc :runtime-entrypoint-operation-coverage
                          {:required
                           stage1-reader-runtime-entrypoint-required-operations
                           :provided entrypoint-operations
                           :covered?
                           (set/subset?
                            (set stage1-reader-runtime-entrypoint-required-operations)
                            (set entrypoint-operations))})
                   (assoc :runner-fallbacks
                          (:runner-fallbacks runtime-entrypoint))
                   (assoc :os-boundaries
                          (:os-boundaries runtime-entrypoint))
                   (assoc :runtime-artifact-routing
                          {:artifact (:artifact runtime-entrypoint)
                           :diagnostic-stream
                           (:diagnostic-stream runtime-entrypoint)
                           :proof-kind (:proof-kind runtime-entrypoint)})
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-runtime-entrypoint)
                   (update :character-stream annotate-with-entrypoint)
                   (update :token-stream annotate-with-entrypoint)
                   (assoc :runtime-entrypoint-applied
                          {:runtime-entrypoint-id runtime-entrypoint-id
                           :operation-count (count entrypoint-operations)
                           :os-boundaries (:os-boundaries runtime-entrypoint)
                           :runner-fallbacks
                           (:runner-fallbacks runtime-entrypoint)})))))
    annotated-records))