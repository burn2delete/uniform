

(defn jvm-backend-validate-manifest!
  ([source-path manifest]
   (jvm-backend-fail!
    "B5-MANIFEST"
    "JVM manifest validation requires trusted artifact context"
    source-path manifest
    {:missing-fact :jvm-manifest-validation-context}))
  ([source-path manifest expected-context]
   (let [required
        [:artifact :schema-version :backend :profile :target :module :emits
         :content-hashes :input :effects :capabilities :safety
         :managed-runtime :host-boundaries :toolchain :conformance
         :closed-plan-runtime
         :clojure-seed-boundary? :self-hosted? :release-grade? :diagnostics]
        missing (vec (remove #(contains? manifest %) required))
        hashes (:content-hashes manifest)
        input (:input manifest)
        closed-plan-runtime (:closed-plan-runtime manifest)
        inferred-effects (get-in manifest [:effects :inferred] #{})
        println-count (get-in input [:instruction-summary :println] 0)
        writes-stdout? (and (set? inferred-effects)
                            (contains? inferred-effects :io/write))
        eligibility (:target-eligibility input)
        digest? #(and (string? %)
                      (boolean
                       (re-matches #"sha256:[0-9a-f]{64}" %)))
        required-input-keys
        #{:source-content-hash :source-declared-target
          :requested-backend-target :target-eligibility
          :stage2-plan-hash :instruction-summary
          :expression-lowering-artifact-hash
          :expression-lowering-source-content-hash
          :expression-lowering-semantic-hash
          :expression-lowering-invoked?
          :expression-lowering-generic-bridge-residual?
          :plan-assembly-function :plan-assembly-artifact-hash
          :plan-assembly-source-content-hash :plan-assembly-semantic-hash
          :plan-assembly-invoked?
          :plan-assembly-generic-bridge-residual?
          :plan-emitter-source-rule-hash
          :compiler-driver-rule-hash :runtime-rule-hash
          :runtime-artifact-hash}
        input-valid?
        (and (= required-input-keys (set (keys input)))
             (every? digest?
                     [(:source-content-hash input)
                      (:stage2-plan-hash input)
                      (:expression-lowering-artifact-hash input)
                      (:expression-lowering-source-content-hash input)
                      (:expression-lowering-semantic-hash input)
                      (:plan-assembly-artifact-hash input)
                      (:plan-assembly-source-content-hash input)
                      (:plan-assembly-semantic-hash input)
                      (:plan-emitter-source-rule-hash input)
                      (:compiler-driver-rule-hash input)
                      (:runtime-rule-hash input)
                      (:runtime-artifact-hash input)])
             (= p15-s23-stage2-compiler-artifact-expected-semantic-hash
                (:expression-lowering-semantic-hash input))
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
             (= :jvm (:requested-backend-target input))
             (= :jvm (:source-declared-target input))
             (= {:status :accepted
                 :source-declared-target :jvm
                 :requested-target :jvm
                 :selection :source-and-request-agree}
                eligibility)
             (map? (:instruction-summary input))
             (integer? println-count)
             (not (neg? println-count))
             (= writes-stdout?
                (pos? println-count)))
        managed-runtime
        {:family :managed :host :jvm-21
         :delegated #{:startup :gc :classloading}
         :generated #{:entrypoint :byte-array-values}
         :linked #{}
         :forbidden #{:reflection :dynamic-loading :native-access
                      :threads :monitors}}
         manifest-hash-valid?
         (and (contains? manifest :manifest-hash)
              (= (:manifest-hash manifest)
                 (str "sha256:"
                      (sha256-hex
                       (pr-str
                        (c-backend-canonical-value
                         (update (dissoc manifest :manifest-hash)
                                 :closed-plan-runtime
                                 p15-s23-closed-runtime-target-semantic-record)))))))
         expected-context-valid?
         (and (map? expected-context)
              (= #{:input :effects :capabilities :content-hashes
                   :closed-runtime-context}
                 (set (keys expected-context)))
              (= (:input expected-context) input)
              (= (:effects expected-context) (:effects manifest))
              (= (:capabilities expected-context)
                 (:capabilities manifest))
              (= (:content-hashes expected-context)
                 (:content-hashes manifest)))]
    (when-not
     (and (empty? missing)
          (= :gravity/jvm-backend-manifest (:artifact manifest))
          (= 1 (:schema-version manifest))
          (= :gravity.backend/jvm (:backend manifest))
          (= :hosted (:profile manifest))
          (= {:classfile 65 :runtime :jvm-21 :module-system :named
              :packaging :modular-executable-jar}
             (:target manifest))
          (= {:name jvm-backend-module-name
              :main-class jvm-backend-main-class
              :side-effects writes-stdout?}
             (:module manifest))
          (= #{:java-sources :class-files :modular-executable-jar
               :source-map :manifest :provenance}
             (set (:emits manifest)))
          (= #{:java-source :module-source :class-file :module-class
               :jar :source-map}
             (set (keys hashes)))
          (every? #(boolean
                    (re-matches #"sha256:[0-9a-f]{64}" (str %)))
                  (vals hashes))
          input-valid?
          (= #{:declared :inferred :capabilities}
             (set (keys (:effects manifest))))
          (set? inferred-effects)
          (set? (get-in manifest [:effects :declared]))
          (set? (get-in manifest [:effects :capabilities]))
          (set? (:capabilities manifest))
          (set/subset? (get-in manifest [:effects :declared]) #{:io/write})
          (set/subset? inferred-effects #{:io/write})
          (set/subset? inferred-effects
                       (get-in manifest [:effects :declared] #{}))
          (set/subset? (get-in manifest [:effects :capabilities])
                       #{:io/stdout})
          (set/subset? (:capabilities manifest) #{:io/stdout})
          (= (set (:capabilities manifest))
             (set (get-in manifest [:effects :capabilities])))
          (or (not writes-stdout?)
              (and (contains? (get-in manifest [:effects :declared] #{})
                              :io/write)
                   (contains? (get-in manifest [:effects :inferred] #{})
                              :io/write)
                   (contains? (set (:capabilities manifest)) :io/stdout)))
          (= (if writes-stdout?
               [{:class "java.lang.System" :member "out"
                 :operation :write-byte-array
                 :effect :io/write :capability :io/stdout
                 :representation :byte-array}]
               [])
             (:host-boundaries manifest))
          (= managed-runtime (:managed-runtime manifest))
          manifest-hash-valid?
          expected-context-valid?
          (= {:mode :safe :unsafe-islands [] :status :preserved}
             (:safety manifest))
          (= {:target-release 21 :encoding :utf8
              :debug [:source :lines] :annotation-processing :disabled}
             (:toolchain manifest))
          (= {:classfile-major :passed :jar-entries :passed
              :main-class :passed :stage2-differential :passed
              :stdout-byte-exact? true :source-map :partial
              :source-map-coverage :source-unit-only
              :per-form-origin-preserved? false :b5-conforming? false
              :verified-mir-input? false}
             (:conformance manifest))
          (p15-s23-closed-runtime-target-record-authentic?
           closed-plan-runtime
           (:closed-runtime-context expected-context))
          (= :complete (get-in closed-plan-runtime [:validation :status]))
          (= 1 (get-in closed-plan-runtime [:invocation :invocation-count]))
          (= :complete (get-in closed-plan-runtime [:execution :status]))
          (digest? (:record-hash closed-plan-runtime))
          (true? (:clojure-seed-boundary? closed-plan-runtime))
          (false? (:self-hosted? closed-plan-runtime))
          (true? (:clojure-seed-boundary? manifest))
          (false? (:self-hosted? manifest))
          (false? (:release-grade? manifest))
          (= [] (:diagnostics manifest)))
      (jvm-backend-fail!
       "B5-MANIFEST" "JVM artifact manifest is incomplete or contradictory"
       source-path manifest
        {:missing-fields missing
        :input-valid? input-valid?
        :manifest-hash-valid? manifest-hash-valid?
        :expected-context-valid? expected-context-valid?
        :missing-fact :complete-jvm-manifest})))
   :passed))

(defn jvm-backend-preflight-output!
  [output-path source-path]
  (when-not (c-backend-output-path-allowed? output-path)
    (jvm-backend-fail!
     "C14-INPUT" "JVM artifact directory is outside declared roots"
     source-path nil
     {:output-path output-path :missing-fact :output-path-containment}))
  (let [output (java.io.File. output-path)
        parent (or (.getParentFile output) (java.io.File. "."))]
    (when (.exists output)
      (jvm-backend-fail!
       "C14-INPUT" "JVM backend requires a fresh artifact directory"
       source-path nil
       {:output-path output-path :missing-fact :fresh-output-path}))
    (when (and (.exists parent) (not (.isDirectory parent)))
      (jvm-backend-fail!
       "C14-INPUT" "JVM artifact parent is not a directory"
       source-path nil
       {:output-path output-path :output-parent (.getPath parent)
        :missing-fact :output-parent-directory}))
    (when (and (not (.exists parent)) (not (.mkdirs parent)))
      (jvm-backend-fail!
       "C14-INPUT" "JVM artifact parent directory is unavailable"
       source-path nil
       {:output-path output-path :output-parent (.getPath parent)
        :missing-fact :output-parent-directory}))
    {:output output :parent parent}))