

(defn c-backend-stage2-compiler-driver-source-rule!
  "Load and validate the Gravity-authored P15-S23 stage2 compiler driver.

  The public runtime-derived route is allowed to use the stage2 emitter and
  runtime only through this declared driver contract.  Keep the driver hash
  independent of the checkout path while retaining the resolved source path
  as provenance."
  [source-path target]
  (let [compiler-source (c-backend-resolve-p15-s23-compiler-source-path)]
    (when-not (.isFile (java.io.File. compiler-source))
      (p15-s23-stage2-compiler-driver-fail!
       "P15S23Y001"
       compiler-source
       nil
       {:requested-source source-path
        :target target
        :missing-fields [:compiler-source]
        :missing-fact :stage2-compiler-driver-source}))
    (let [pinned-source
          (p15-s23-stage2-compiler-pinned-source!
           compiler-source source-path target "P15S23Y001"
           p15-s23-stage2-compiler-driver-fail!)
          source-data
          (try
            (p15-s23-compiler-source-form-record-from-text
             compiler-source (:source-text pinned-source))
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-compiler-driver-fail!
               "P15S23Y001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-compiler-driver-source
                :cause-message (.getMessage ex)})))
          forms (:forms source-data)
          driver
          (try
            (p15-s23-compiler-def-value
             compiler-source forms 'p15-s23-stage2-compiler-driver)
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-compiler-driver-fail!
               "P15S23Y001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-compiler-driver-definition
                :cause-message (.getMessage ex)})))
          front-end
          (try
            (p15-s23-compiler-def-value
             compiler-source forms 'p15-s23-stage2-source-front-end)
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-compiler-driver-fail!
               "P15S23Y002"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-source-front-end-definition
                :cause-message (.getMessage ex)})))]
      (when-not (map? driver)
        (p15-s23-stage2-compiler-driver-fail!
         "P15S23Y001"
         compiler-source
         driver
         {:requested-source source-path
          :target target
          :missing-fields [:p15-s23-stage2-compiler-driver]
          :missing-fact :stage2-compiler-driver-definition}))
      (when-not (map? front-end)
        (p15-s23-stage2-compiler-driver-fail!
         "P15S23Y002"
         compiler-source
         front-end
         {:requested-source source-path
          :target target
          :missing-fields [:p15-s23-stage2-source-front-end]
          :missing-fact :stage2-source-front-end-definition}))
      (let [rule-record (p15-s23-stage2-compiler-driver-rule-record driver)
            required-links
            {:nucleus :p15-s23-stage2-compiler-nucleus
             :plan-emitter :p15-s23-stage2-plan-emitter
             :runtime-executor :p15-s23-stage2-runtime-executor
             :runtime-kernel :p15-s23-stage2-runtime-kernel
             :front-end-executor :p15-s23-stage2-front-end-executor
             :source-front-end :p15-s23-stage2-source-front-end}
            observed-links (select-keys driver (keys required-links))
            missing-links
            (set (for [[field expected] required-links
                       :when (not= expected (get observed-links field))]
                   field))
            execution-contract (:execution-contract driver)
            execution-ok?
            (and (= :stage0-compiled-core-plan
                    (:compare-against execution-contract))
                 (true? (:accepted-output-must-match-expected?
                         execution-contract))
                 (= :gravity-source-bytes (:input driver))
                 (= :stage2-driver-run-record (:output driver))
                 (= :hosted-core-source-to-stage2-runtime-execution
                    (:module-responsibility driver)))]
        (when-not (and (= :complete (:status rule-record))
                       (empty? missing-links)
                       execution-ok?
                       (= :gravity/stage2-compiler-driver
                          (:artifact driver))
                       (= :p15-s23-stage2-compiler-driver
                          (:stage driver))
                       (= :p15-s23-compiler-stage
                          (:compiler-stage driver)))
          (p15-s23-stage2-compiler-driver-fail!
           "P15S23Y002"
           compiler-source
           {:driver driver
            :rule-record rule-record
            :missing-links (p15-s23-stage2-sort-values missing-links)
            :execution-contract execution-contract}
           {:requested-source source-path
            :target target
            :missing-fact :stage2-compiler-driver-rule-set
            :missing-links (p15-s23-stage2-sort-values missing-links)
            :execution-contract-valid? execution-ok?
            :rule-record rule-record}))
        (let [driver-rule-hash
              (str "sha256:"
                   (sha256-hex
                    (pr-str (c-backend-canonical-value driver))))
              source-content-hash
              (str "sha256:" (sha256-hex (:source-text source-data)))]
          {:driver driver
           :front-end front-end
           :rule-record rule-record
           :driver-engine (:engine driver)
           :driver-rule-hash driver-rule-hash
           :driver-source-path compiler-source
           :driver-source-content-hash source-content-hash
           :driver-rule-source
           {:kind :gravity-source
            :sha256 source-content-hash
            :driver-rule-hash driver-rule-hash
            :stage :p15-s23-stage2-compiler-driver}})))))