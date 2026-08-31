

(defn stage1-reader-compiler-driver-from-definitions
  [reader-source-path definitions]
  (let [driver
        (stage1-reader-compiler-driver-literal-definition-value
         reader-source-path definitions
         'stage1-reader-compiler-driver)
        diagnostics (:diagnostics driver)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint :unsupported-driver-operation
                 :missing-driver-record :artifact-routing-divergence
                 :diagnostic-stream-divergence
                 :illegal-seed-orchestration-fallback
                 :invalid-compiler-driver])
        required-stages
        [:stage1-driver-load-reader-source
         :stage1-driver-resolve-entrypoint
         :stage1-driver-execute-core-bootstrap-runtime
         :stage1-driver-emit-diagnostic-stream
         :stage1-driver-emit-proof-artifact
         :stage1-driver-record-provenance]
        direct-stages (:direct-stages driver)]
    (when-not (map? driver)
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV003" reader-source-path driver
       {:missing-fields [:stage1-reader-compiler-driver]}))
    (doseq [field [:engine :entrypoint :replaces :base-runtime
                   :core-bootstrap-builtins :input :output :artifact
                   :diagnostic-stream :proof-kind :host-boundaries
                   :seed-orchestration-fallbacks :driver-operations
                   :direct-stages :uses-runtimes :uses-builtins
                   :uses-executors :preserves :diagnostics
                   :provenance]]
      (when-not (contains? driver field)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV007" reader-source-path driver
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-compiler-driver-v1 (:engine driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-compiler-driver
                 (:entrypoint driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:entrypoint]}))
    (when-not (contains? (set (:replaces driver))
                         :clojure-seed-orchestration)
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-core-bootstrap-runtime
                 (:base-runtime driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:base-runtime]}))
    (when-not (= :stage1-reader-core-bootstrap-builtins
                 (:core-bootstrap-builtins driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:core-bootstrap-builtins]}))
    (when-not (= [:source-path :source-text] (:input driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:output driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:output]}))
    (when-not (= :gravity/stage1-reader-compiler-driver-artifact
                 (:artifact driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV004" reader-source-path driver
       {:missing-fields [:artifact]}))
    (when-not (= :gravity/stage1-reader-compiler-driver-diagnostic-stream
                 (:diagnostic-stream driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV005" reader-source-path driver
       {:missing-fields [:diagnostic-stream]}))
    (when-not (= :gravity/stage1-reader-compiler-driver-proof
                 (:proof-kind driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV004" reader-source-path driver
       {:missing-fields [:proof-kind]}))
    (when-not (set/subset? #{:host-command-invocation
                             :host-file-read}
                           (set (:host-boundaries driver)))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:host-boundaries]}))
    (when-not (= [] (:seed-orchestration-fallbacks driver))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV006" reader-source-path driver
       {:seed-orchestration-fallbacks
        (:seed-orchestration-fallbacks driver)}))
    (let [driver-operations (:driver-operations driver)
          operation-names (set driver-operations)
          required-operation-set
          (set stage1-reader-compiler-driver-required-operations)]
      (when-not (vector? driver-operations)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV003" reader-source-path driver
         {:missing-fields [:driver-operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV002" reader-source-path driver-operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-compiler-driver-required-operations))})))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? #{:stage1-reader-compiler-driver
                             :stage1-reader-core-bootstrap-runtime
                             :stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes driver)))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins driver)))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors driver)))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source (get-in driver [:provenance :owner]))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-compiler-driver-orchestration-replacement
                 (get-in driver [:provenance :purpose]))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-clojure-seed-orchestration-with-gravity-compiler-driver
                 (get-in driver [:provenance :retirement-objective]))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path driver
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV007" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc driver
           :compiler-driver-id
           (str "sha256:" (sha256-hex (pr-str driver))))))

(defn stage1-reader-compiler-driver-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-compiler-driver-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-compiler-driver
               source-path
               source-text
               stage1-reader-compiler-driver
               stage1-reader-core-bootstrap-runtime
               stage1-reader-core-bootstrap-builtins))
            (:body definition)))))

(defn stage1-reader-execute-compiler-driver
  [reader-source-path
   definitions
   source-path
   source-text
   self-hosted-runtime
   core-bootstrap-runtime
   core-bootstrap-builtins
   compiler-driver]
  (let [driver-id (:compiler-driver-id compiler-driver)
        driver-operations (:driver-operations compiler-driver)
        annotate-with-driver
        (fn [record]
          (assoc record
                 :compiler-driver-id driver-id
                 :compiler-driver-engine (:engine compiler-driver)
                 :seed-orchestration-replacement
                 :gravity-reader-compiler-driver-v1))
        records
        (stage1-reader-execute-core-bootstrap-runtime
         reader-source-path
         definitions
         source-path
         source-text
         self-hosted-runtime
         core-bootstrap-runtime
         core-bootstrap-builtins)
        annotated-records (mapv annotate-with-driver records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (-> (or trace {})
                   (assoc :compiler-driver compiler-driver)
                   (assoc :compiler-driver-operation-coverage
                          {:required
                           stage1-reader-compiler-driver-required-operations
                           :provided driver-operations
                           :covered?
                           (set/subset?
                            (set stage1-reader-compiler-driver-required-operations)
                            (set driver-operations))})
                   (assoc :seed-orchestration-fallbacks
                          (:seed-orchestration-fallbacks compiler-driver))
                   (assoc :host-command-boundaries
                          (:host-boundaries compiler-driver))
                   (assoc :artifact-routing
                          {:artifact (:artifact compiler-driver)
                           :diagnostic-stream
                           (:diagnostic-stream compiler-driver)
                           :proof-kind (:proof-kind compiler-driver)})
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-compiler-driver)
                   (update :character-stream annotate-with-driver)
                   (update :token-stream annotate-with-driver)
                   (assoc :compiler-driver-applied
                          {:driver-id driver-id
                           :operation-count (count driver-operations)
                           :host-boundaries
                           (:host-boundaries compiler-driver)
                           :seed-orchestration-fallbacks
                           (:seed-orchestration-fallbacks
                            compiler-driver)})))))
    annotated-records))