

(defn stage1-reader-core-bootstrap-builtins-from-definitions
  [reader-source-path definitions]
  (let [builtins
        (stage1-reader-core-bootstrap-literal-definition-value
         reader-source-path definitions
         'stage1-reader-core-bootstrap-builtins)]
    (when-not (map? builtins)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:stage1-reader-core-bootstrap-builtins]}))
    (doseq [field [:engine :profile :owner :replaces :host-fallbacks
                   :effects :capabilities :operations :preserves
                   :provenance]]
      (when-not (contains? builtins field)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE003" reader-source-path builtins
         {:missing-fields [field]})))
    (when-not (= :gravity-core-bootstrap-builtins-v1 (:engine builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:engine]}))
    (when-not (= :meta (:profile builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:profile]}))
    (when-not (= :gravity-source (:owner builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:owner]}))
    (when-not (contains? (set (:replaces builtins))
                         :clojure-seed-builtins)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:replaces]}))
    (when-not (= [] (:host-fallbacks builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE005" reader-source-path builtins
       {:host-fallbacks (:host-fallbacks builtins)}))
    (when-not (empty? (:effects builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE005" reader-source-path builtins
       {:effects (:effects builtins)}))
    (when-not (empty? (:capabilities builtins))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE005" reader-source-path builtins
       {:capabilities (:capabilities builtins)}))
    (let [operations (:operations builtins)
          operation-names (set (map :op operations))
          required-operation-set
          (set stage1-reader-core-bootstrap-required-operations)]
      (when-not (vector? operations)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE003" reader-source-path builtins
         {:missing-fields [:operations]}))
      (when-not (set/subset? required-operation-set operation-names)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE002" reader-source-path operations
         {:missing-operations
          (vec (remove operation-names
                       stage1-reader-core-bootstrap-required-operations))}))
      (doseq [operation operations]
        (when-not (= :gravity-source (:implemented-by operation))
          (stage1-reader-core-bootstrap-fail!
           "STAGE1CORE005" reader-source-path operation
           {:missing-fields [:implemented-by]}))))
    (when-not (= :gravity-source (get-in builtins [:provenance :owner]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-core-bootstrap-builtin-replacement
                 (get-in builtins [:provenance :purpose]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-clojure-seed-builtins-with-gravity-core-bootstrap
                 (get-in builtins [:provenance :retirement-objective]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE003" reader-source-path builtins
       {:missing-fields [:provenance :retirement-objective]}))
    (assoc builtins
           :core-bootstrap-builtins-id
           (str "sha256:" (sha256-hex (pr-str builtins))))))

(defn stage1-reader-core-bootstrap-runtime-from-definitions
  [reader-source-path definitions]
  (let [runtime
        (stage1-reader-core-bootstrap-literal-definition-value
         reader-source-path definitions
         'stage1-reader-core-bootstrap-runtime)
        diagnostics (:diagnostics runtime)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint :unsupported-builtin-operation
                 :missing-builtin-record :builtin-runtime-divergence
                 :illegal-host-fallback
                 :invalid-core-bootstrap-runtime])
        required-stages
        [:stage1-core-bootstrap-create-character-stream
         :stage1-core-bootstrap-execute-token-automaton
         :stage1-core-bootstrap-execute-form-builder
         :stage1-core-bootstrap-compare-stage0]
        direct-stages (:direct-stages runtime)]
    (when-not (map? runtime)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:stage1-reader-core-bootstrap-runtime]}))
    (doseq [field [:engine :entrypoint :replaces :base-runtime
                   :core-bootstrap-builtins :input :output
                   :direct-stages :uses-runtimes :uses-builtins
                   :uses-executors :preserves :diagnostics
                   :provenance]]
      (when-not (contains? runtime field)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE006" reader-source-path runtime
         {:missing-fields [field]})))
    (when-not (= :gravity-reader-core-bootstrap-runtime-v1
                 (:engine runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:engine]}))
    (when-not (= :stage1-read-source-core-bootstrap
                 (:entrypoint runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:entrypoint]}))
    (when-not (contains? (set (:replaces runtime))
                         :clojure-seed-builtins)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:replaces]}))
    (when-not (= :stage1-reader-self-hosted-runtime
                 (:base-runtime runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:base-runtime]}))
    (when-not (= :stage1-reader-core-bootstrap-builtins
                 (:core-bootstrap-builtins runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:core-bootstrap-builtins]}))
    (when-not (= [:source-path :source-text] (:input runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:output runtime))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:output]}))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? #{:stage1-reader-self-hosted-runtime
                             :stage1-reader-source-runtime}
                           (set (:uses-runtimes runtime)))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                           (set (:uses-builtins runtime)))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:uses-builtins]}))
    (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
                           (set (:uses-executors runtime)))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source (get-in runtime [:provenance :owner]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-core-bootstrap-runtime
                 (get-in runtime [:provenance :purpose]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-clojure-seed-builtins-with-gravity-core-bootstrap
                 (get-in runtime [:provenance :retirement-objective]))
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path runtime
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE006" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc runtime
           :core-bootstrap-runtime-id
           (str "sha256:" (sha256-hex (pr-str runtime))))))

(defn stage1-reader-core-bootstrap-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-core-bootstrap-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-core-bootstrap-runtime
               source-path
               source-text
               stage1-reader-core-bootstrap-runtime
               stage1-reader-core-bootstrap-builtins))
            (:body definition)))))