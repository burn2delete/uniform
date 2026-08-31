

(defn js-ts-backend-validate-manifest!
  ([source-path manifest]
   (js-ts-backend-fail!
    "B6-MANIFEST"
    "JS/TS manifest validation requires trusted runtime context"
    source-path manifest
    {:missing-fact :closed-runtime-validation-context}))
  ([source-path manifest closed-runtime-context]
  (let [required-top-level
        [:artifact :schema-version :backend :profile :target :module :emits
         :content-hashes :input :effects :capabilities :safety :host-globals
         :numeric-representation :nullish-policy :exception-policy
         :typescript-compiler :conformance :closed-plan-runtime
         :manifest-hash
         :clojure-seed-boundary?
         :self-hosted? :release-grade? :diagnostics]
        missing (vec (remove #(contains? manifest %) required-top-level))
        runtime-version (get-in manifest [:target :runtime-version])
        valid-target?
        (and (string? runtime-version)
             (boolean (re-matches #"v20(?:\.[0-9]+){1,2}" runtime-version))
             (= {:runtime js-ts-backend-runtime
                 :runtime-version runtime-version
                 :ecmascript js-ts-backend-ecmascript
                 :module-format js-ts-backend-module-format}
                (:target manifest)))
        valid-hashes?
        (and (= #{:javascript :typescript-declarations :source-map
                  :package-metadata}
                (set (keys (:content-hashes manifest))))
             (every? #(and (string? %)
                           (boolean
                            (re-matches #"sha256:[0-9a-f]{64}" %)))
                     (vals (:content-hashes manifest))))
        manifest-hash-valid?
        (and (string? (:manifest-hash manifest))
             (= (:manifest-hash manifest)
                (str "sha256:"
                     (sha256-hex
                      (pr-str
                       (c-backend-canonical-value
                        (update (dissoc manifest :manifest-hash)
                                :closed-plan-runtime
                                p15-s23-closed-runtime-target-semantic-record)))))))
        writes-stdout? (true? (get-in manifest [:module :side-effects]))
        stdout-authorized?
        (contains? (set (:capabilities manifest)) :io/stdout)
        host-global-valid?
        (= (if writes-stdout?
             [{:module "node:process"
               :symbol :stdout
               :effect :io/write
               :capability :io/stdout
               :representation :uint8array-bytes}]
             [])
           (:host-globals manifest))
        input (:input manifest)
        closed-plan-runtime (:closed-plan-runtime manifest)
        eligibility (:target-eligibility input)
        source-declared-target (:source-declared-target input)
        valid-input?
        (and (= #{:source-content-hash :source-declared-target
                  :requested-backend-target :target-eligibility
                  :stage2-plan-hash :compiler-driver-rule-hash
                  :runtime-rule-hash :runtime-artifact-hash
                  :expression-lowering-artifact-hash
                  :expression-lowering-source-content-hash
                  :expression-lowering-semantic-hash
                  :expression-lowering-invoked?
                  :expression-lowering-generic-bridge-residual?
                  :plan-assembly-function :plan-assembly-artifact-hash
                  :plan-assembly-source-content-hash
                  :plan-assembly-semantic-hash :plan-assembly-invoked?
                  :plan-assembly-generic-bridge-residual?}
                (set (keys input)))
             (every? #(boolean
                       (re-matches #"sha256:[0-9a-f]{64}" (str %)))
                     [(:source-content-hash input)
                      (:stage2-plan-hash input)
                      (:expression-lowering-artifact-hash input)
                      (:expression-lowering-source-content-hash input)
                      (:expression-lowering-semantic-hash input)
                      (:plan-assembly-artifact-hash input)
                      (:plan-assembly-source-content-hash input)
                      (:plan-assembly-semantic-hash input)
                      (:compiler-driver-rule-hash input)
                      (:runtime-rule-hash input)
                      (:runtime-artifact-hash input)])
             (true? (:expression-lowering-invoked? input))
             (true? (:expression-lowering-generic-bridge-residual? input))
             (= p15-s23-stage2-compiler-artifact-plan-assembly-function
                (:plan-assembly-function input))
             (= (:expression-lowering-artifact-hash input)
                (:plan-assembly-artifact-hash input))
             (= (:expression-lowering-source-content-hash input)
                (:plan-assembly-source-content-hash input))
             (= (:expression-lowering-semantic-hash input)
                (:plan-assembly-semantic-hash input))
             (true? (:plan-assembly-invoked? input))
             (true? (:plan-assembly-generic-bridge-residual? input))
             (= js-ts-backend-target (:requested-backend-target input))
             (= :accepted (:status eligibility))
             (= source-declared-target
                (:source-declared-target eligibility))
             (= js-ts-backend-target (:requested-target eligibility))
             (or (and (= js-ts-backend-target source-declared-target)
                      (= :source-and-request-agree (:selection eligibility)))
                 (and (= :jvm source-declared-target)
                      (= :explicit-bootstrap-seed-target-override
                         (:selection eligibility))
                      (true? (:bootstrap-seed-target? eligibility)))))]
    (when-not (and (empty? missing)
                   (= :gravity/js-ts-backend-manifest (:artifact manifest))
                   (= 1 (:schema-version manifest))
                   (= :gravity.backend/js-ts (:backend manifest))
                   (= :hosted (:profile manifest))
                   valid-target?
                   (= {:side-effects writes-stdout?
                       :package-boundary :standalone}
                      (:module manifest))
                   (= #{:javascript :typescript-declarations :source-map
                        :package-metadata :manifest :provenance}
                      (set (:emits manifest)))
                   (= 6 (count (:emits manifest)))
                   valid-input?
                   (= #{:declared :inferred :capabilities}
                      (set (keys (:effects manifest))))
                   (set/subset? (get-in manifest [:effects :declared] #{})
                                #{:io/write})
                   (set/subset? (get-in manifest [:effects :inferred] #{})
                                #{:io/write})
                   (set/subset? (get-in manifest [:effects :inferred] #{})
                                (get-in manifest [:effects :declared] #{}))
                   (= writes-stdout?
                      (contains? (get-in manifest [:effects :inferred] #{})
                                 :io/write))
                   (set/subset? (set (:capabilities manifest))
                                #{:io/stdout})
                   (= (set (:capabilities manifest))
                      (set (get-in manifest [:effects :capabilities])))
                   (or (not writes-stdout?)
                       (and (contains? (get-in manifest
                                               [:effects :declared] #{})
                                       :io/write)
                            (contains? (get-in manifest
                                               [:effects :inferred] #{})
                                       :io/write)))
                   (or (not writes-stdout?) stdout-authorized?)
                   host-global-valid?
                   valid-hashes?
                   manifest-hash-valid?
                   (= {:mode :hosted-scalar-spelling
                       :bytes :utf8
                       :lossy-number-lowering? false}
                      (:numeric-representation manifest))
                   (= :no-host-nullish-inputs (:nullish-policy manifest))
                   (= :no-host-exception-boundary-in-slice
                      (:exception-policy manifest))
                   (= {:available? false :required? false
                       :reason :tsc-not-installed}
                      (:typescript-compiler manifest))
                   (= {:node-check :passed
                       :stage2-differential :passed
                       :stdout-byte-exact? true
                       :source-map :partial
                       :source-map-coverage :source-unit-only
                       :per-form-origin-preserved? false
                       :b6-conforming? false}
                      (:conformance manifest))
                   (p15-s23-closed-runtime-target-record-authentic?
                    closed-plan-runtime closed-runtime-context)
                   (= :complete
                      (get-in closed-plan-runtime [:validation :status]))
                   (= 1
                      (get-in closed-plan-runtime
                              [:invocation :invocation-count]))
                   (= :complete
                      (get-in closed-plan-runtime [:execution :status]))
                   (boolean
                    (re-matches #"sha256:[0-9a-f]{64}"
                                (str (:record-hash closed-plan-runtime))))
                   (true? (:clojure-seed-boundary? closed-plan-runtime))
                   (false? (:self-hosted? closed-plan-runtime))
                   (= {:mode :safe :unsafe-islands [] :status :preserved}
                      (:safety manifest))
                   (true? (:clojure-seed-boundary? manifest))
                   (false? (:self-hosted? manifest))
                   (false? (:release-grade? manifest))
                   (= [] (:diagnostics manifest)))
      (js-ts-backend-fail!
       "B6-MANIFEST" "JS/TS artifact manifest is incomplete or contradictory"
       source-path manifest
       {:missing-fields missing
        :target-valid? valid-target?
        :input-valid? valid-input?
        :content-hashes-valid? valid-hashes?
        :manifest-hash-valid? manifest-hash-valid?
        :missing-fact :complete-js-ts-manifest})))
  :passed))

(defn js-ts-backend-run-node-process!
  [arguments source-path diagnostic-id message]
  (let [stdout-file (java.io.File/createTempFile "gravity-js-node-out-" ".bin")
        stderr-file (java.io.File/createTempFile "gravity-js-node-err-" ".txt")
        pb (ProcessBuilder. ^java.util.List (into ["node"] arguments))]
    (try
      (.redirectOutput pb stdout-file)
      (.redirectError pb stderr-file)
      (let [process (.start pb)
            finished? (.waitFor process 60000
                                java.util.concurrent.TimeUnit/MILLISECONDS)]
        (when-not finished?
          (.destroyForcibly process)
          (js-ts-backend-fail!
           diagnostic-id message source-path nil
           {:node-command (into ["node"] arguments)
            :missing-fact :node-process-completion}))
        (let [result {:exit (.exitValue process)
                      :stdout-bytes
                      (vec (map #(bit-and (int %) 0xff)
                                (java.nio.file.Files/readAllBytes
                                 (.toPath stdout-file))))
                      :stderr (if (.exists stderr-file)
                                (slurp stderr-file) "")}]
          (when-not (zero? (:exit result))
            (js-ts-backend-fail!
             diagnostic-id message source-path nil
             {:node-command (into ["node"] arguments)
              :node-result result
              :missing-fact :node20-esm-acceptance}))
          result))
      (catch clojure.lang.ExceptionInfo ex
        (throw ex))
      (catch Exception ex
        (js-ts-backend-fail!
         "B6-TARGET" "Node 20 runtime is unavailable"
         source-path nil
         {:runtime js-ts-backend-runtime
          :cause-message (.getMessage ex)
          :missing-fact :node20-runtime}))
      (finally
        (.delete stdout-file)
        (.delete stderr-file)))))