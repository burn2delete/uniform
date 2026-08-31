

(defn c-backend-stage2-plan-emitter-source-rule!
  "Load and validate the Gravity-authored P15-S23 stage2 plan-emitter rule.

  Runtime-derived C lowering is deliberately bound to this source rule rather
  than silently reusing the Clojure stage0 plan emitter.  The returned rule
  hash is path-neutral and is carried into manifests/provenance so the exact
  Gravity rule set used for lowering is auditable."
  [source-path target]
  (let [compiler-source (c-backend-resolve-p15-s23-compiler-source-path)]
    (when-not (.isFile (java.io.File. compiler-source))
      (p15-s23-stage2-plan-emitter-fail!
       "P15S23Q001"
       compiler-source
       nil
       {:missing-fields [:compiler-source]
        :requested-source source-path
        :target target
        :missing-fact :stage2-plan-emitter-source}))
    (let [pinned-source
          (p15-s23-stage2-compiler-pinned-source!
           compiler-source source-path target "P15S23Q001"
           p15-s23-stage2-plan-emitter-fail!)
          source-data
          (try
            (p15-s23-compiler-source-form-record-from-text
             compiler-source (:source-text pinned-source))
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-plan-emitter-source
                :cause-message (.getMessage ex)})))
          emitter
          (try
            (p15-s23-compiler-def-value compiler-source
                                         (:forms source-data)
                                         'p15-s23-stage2-plan-emitter)
            (catch clojure.lang.ExceptionInfo ex
              (throw ex))
            (catch Exception ex
              (p15-s23-stage2-plan-emitter-fail!
               "P15S23Q001"
               compiler-source
               nil
               {:requested-source source-path
                :target target
                :missing-fact :stage2-plan-emitter-definition
                :cause-message (.getMessage ex)})))]
      (when-not (map? emitter)
        (p15-s23-stage2-plan-emitter-fail!
         "P15S23Q001"
         compiler-source
         emitter
         {:requested-source source-path
          :target target
          :missing-fields [:stage2-plan-emitter]
          :missing-fact :stage2-plan-emitter-definition}))
      (let [rule-record (p15-s23-stage2-plan-emitter-rule-record emitter)]
        (when-not (= :complete (:status rule-record))
          (p15-s23-stage2-plan-emitter-fail!
           "P15S23Q002"
           compiler-source
           rule-record
           {:requested-source source-path
            :target target
            :missing-fact :stage2-plan-emitter-rule-set}))
        {:emitter emitter
         :source-path compiler-source
         :rule-record rule-record
         :source-rule-hash
         (str "sha256:"
              (sha256-hex
               (pr-str (c-backend-canonical-value emitter))))}))))