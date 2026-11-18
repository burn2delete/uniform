
(defn- jvm-backend-source-input-projection
  [packet source-content-hash plan-hash]
  (let [compiler-record (:stage2-compiler-artifact-record packet)
        plan (:plan packet)
        current-runtime-development?
        (c-backend-current-runtime-development-packet? packet)]
    (cond->
     {:source-content-hash source-content-hash
      :plan-assembly-invoked? (:plan-assembly-invoked? compiler-record)
      :source-declared-target
      (get-in packet [:target-eligibility :source-declared-target])
      :plan-assembly-artifact-hash
      (:plan-assembly-artifact-hash compiler-record)
      :expression-lowering-artifact-hash (:artifact-hash compiler-record)
      :runtime-rule-hash (get-in packet [:stage2-runtime-rule :runtime-rule-hash])
      :expression-lowering-semantic-hash (:semantic-hash compiler-record)
      :expression-lowering-generic-bridge-residual?
      (:generic-bridge-residual? compiler-record)
      :plan-assembly-generic-bridge-residual?
      (:plan-assembly-generic-bridge-residual? compiler-record)
      :instruction-summary (:instruction-summary plan)
      :plan-emitter-source-rule-hash
      (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])
      :stage2-plan-hash plan-hash
      :requested-backend-target :jvm
      :plan-assembly-function (:plan-assembly-function compiler-record)
      :expression-lowering-invoked? (:invoked? compiler-record)
      :runtime-artifact-hash
      (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
      :expression-lowering-source-content-hash
      (:source-content-hash compiler-record)
      :plan-assembly-semantic-hash
      (:plan-assembly-semantic-hash compiler-record)
      :plan-assembly-source-content-hash
      (:plan-assembly-source-content-hash compiler-record)
      :target-eligibility (:target-eligibility packet)}
      current-runtime-development?
      (assoc :current-runtime-development? true
             :runtime-owner-content-hash
             (get-in packet [:stage2-runtime-rule
                             :current-runtime-development-binding
                             :runtime-source-content-hash])
             :runtime-compatibility-content-hash
             (get-in packet [:stage2-runtime-rule
                             :current-runtime-compatibility-link
                             :source-content-hash]))
      (not current-runtime-development?)
      (assoc :compiler-driver-rule-hash
             (get-in packet [:stage2-compiler-driver-rule
                             :driver-rule-hash])))))

(defn- jvm-backend-current-runtime-development-source-revisions-current?
  [packet source-path target]
  (let [runtime-rule (:stage2-runtime-rule packet)
        runtime-binding (:current-runtime-development-binding runtime-rule)
        compatibility-link (:current-runtime-compatibility-link runtime-rule)
        {:keys [runtime-source compatibility-source]}
        (c-backend-current-runtime-development-source-paths)
        runtime-data
        (c-backend-current-runtime-development-read-source!
         runtime-source source-path target :current-runtime-source)
        compatibility-data
        (c-backend-current-runtime-development-read-source!
         compatibility-source source-path target :runtime-compatibility-source)]
    (and
     (= runtime-source (:runtime-artifact-source-path runtime-rule))
     (= compatibility-source (:source-path compatibility-link))
     (= (:sha256 runtime-data) (:runtime-source-content-hash runtime-binding))
     (= (:sha256 compatibility-data) (:source-content-hash compatibility-link)))))

(defn- jvm-backend-current-runtime-development-manifest-authentic?
  ([manifest expected-context]
   (try
     (and
      (= :passed (:status (p15-s23-trusted-carrier-validation
                           manifest :default-only)))
      (= :passed (:status (p15-s23-trusted-carrier-validation
                           expected-context :default-only)))
      (map? manifest)
      (map? expected-context)
      (let [runtime-context (:closed-runtime-context expected-context)
            source-path (:source-path runtime-context)
            source-text (:source-text runtime-context)
            packet
            (stage2-runtime-derived-current-runtime-development-packet
             source-path source-text :jvm
             {:validate-plan!
              (fn [candidate]
                (jvm-backend-validate-plan! source-path candidate))})]
        (jvm-backend-current-runtime-development-manifest-authentic?
         manifest expected-context packet)))
     (catch StackOverflowError _ false)
     (catch Exception _ false)))
  ([manifest expected-context packet]
  (try
    (and
     (= :passed (:status (p15-s23-trusted-carrier-validation
                          manifest :default-only)))
     (= :passed (:status (p15-s23-trusted-carrier-validation
                          expected-context :default-only)))
     (= :passed (:status (p15-s23-trusted-carrier-validation
                          packet :default-only)))
     (map? packet)
     (let [runtime-context (:closed-runtime-context expected-context)
           source-path (:source-path runtime-context)
           source-text (:source-text runtime-context)
           plan (:plan packet)
           compiler-record (:stage2-compiler-artifact-record packet)
           runtime-rule (:stage2-runtime-rule packet)
           target-record
           (current-runtime-development-target-record-from-authenticated-packet
            packet)
           plan-hash
           (c4-artifact-id
            (c-backend-canonical-value
             (select-keys plan [:kind :entrypoint :functions
                                :instruction-summary :effect-summary])))
           expected-input
           (jvm-backend-source-input-projection
            packet (:source-content-hash runtime-context) plan-hash)
           comparison-record
           (select-keys
            (:current-runtime-development-comparison-record packet)
            [:artifact :source-id :stage2-plan-id
             :accepted-output-equivalent? :status :comparison-boundary
             :revision-status :effects :clojure-seed-boundary? :self-hosted?])]
       (and
        (= #{:input :effects :capabilities :content-hashes
             :closed-runtime-context}
           (set (keys expected-context)))
        (= #{:schema :source-path :source-text :source-content-hash
             :requested-target :plan-id :runtime-source-content-hash
             :compatibility-source-content-hash}
           (set (keys runtime-context)))
        (= :gravity/current-runtime-development-validation-context-v1
           (:schema runtime-context))
        (= :jvm (:requested-target runtime-context))
        (string? source-path)
        (string? source-text)
        (= (str "sha256:" (sha256-hex source-text))
           (:source-content-hash runtime-context))
        (jvm-backend-current-runtime-development-source-revisions-current?
         packet source-path :jvm)
        (= runtime-context
           (current-runtime-development-target-context
            packet source-path source-text :jvm))
        (= expected-input (:input manifest))
        (= :gravity/current-runtime-development-target-record
           (:artifact target-record))
        (= (c-backend-canonical-value target-record)
           (c-backend-canonical-value (:closed-plan-runtime manifest)))
        (= (c-backend-current-runtime-development-source-record runtime-rule)
           (:runtime-artifact-source manifest))
        (= :p15-s23-stage2-plan-emitter (:compiler-stage manifest))
        (true? (:current-runtime-development? manifest))
        (false? (:stage2-compiler-driver-used? manifest))
        (= :development-unadmitted (:runtime-revision-status manifest))
        (= :not-checked (:runtime-effects-status manifest))
        (= comparison-record
           (:current-runtime-development-comparison-record manifest)))))
    (catch StackOverflowError _ false)
    (catch Exception _ false))))

(defn- jvm-backend-validate-manifest-core!
  [source-path manifest expected-context authenticated-reference]
   (if-not (and
            (= :passed (:status (p15-s23-trusted-carrier-validation
                                 manifest :default-only)))
            (= :passed (:status (p15-s23-trusted-carrier-validation
                                 expected-context :default-only)))
            (map? manifest)
            (map? expected-context))
     (jvm-backend-fail!
      "B5-MANIFEST"
      "JVM manifest validation received an untrusted carrier"
      source-path nil {:missing-fact :trusted-jvm-manifest-carrier})
     (let [required
        [:artifact :schema-version :backend :profile :target :module :emits
         :content-hashes :input :effects :capabilities :safety
         :managed-runtime :host-boundaries :toolchain :conformance
         :closed-plan-runtime
         :clojure-seed-boundary? :self-hosted? :release-grade? :diagnostics]
        missing (vec (remove #(contains? manifest %) required))
        current-runtime-development?
        (true? (:current-runtime-development? manifest))
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
        (cond->
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
          :runtime-rule-hash :runtime-artifact-hash}
          current-runtime-development?
          (into #{:current-runtime-development?
                  :runtime-owner-content-hash
                  :runtime-compatibility-content-hash})
          (not current-runtime-development?)
          (conj :compiler-driver-rule-hash))
        input-digests
        (cond->
         [(:source-content-hash input)
          (:stage2-plan-hash input)
          (:expression-lowering-artifact-hash input)
          (:expression-lowering-source-content-hash input)
          (:expression-lowering-semantic-hash input)
          (:plan-assembly-artifact-hash input)
          (:plan-assembly-source-content-hash input)
          (:plan-assembly-semantic-hash input)
          (:plan-emitter-source-rule-hash input)
          (:runtime-rule-hash input)
          (:runtime-artifact-hash input)]
          current-runtime-development?
          (conj (:runtime-owner-content-hash input)
                (:runtime-compatibility-content-hash input))
          (not current-runtime-development?)
          (conj (:compiler-driver-rule-hash input)))
        input-valid?
        (and (= required-input-keys (set (keys input)))
             (every? digest? input-digests)
             (or current-runtime-development?
                 (= p15-s23-stage2-compiler-artifact-expected-semantic-hash
                    (:expression-lowering-semantic-hash input)))
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
          (if current-runtime-development?
            (if authenticated-reference
              (jvm-backend-current-runtime-development-manifest-authentic?
               manifest expected-context authenticated-reference)
              (jvm-backend-current-runtime-development-manifest-authentic?
               manifest expected-context))
            (p15-s23-closed-runtime-target-record-authentic?
             closed-plan-runtime
             (:closed-runtime-context expected-context)))
          (if current-runtime-development?
            (and (= :complete
                    (get-in closed-plan-runtime
                            [:closed-plan-validation :status]))
                 (= :complete
                    (get-in closed-plan-runtime [:execution :status]))
                 (= :development-unadmitted
                    (get-in closed-plan-runtime
                            [:boundary :revision-status]))
                 (= :not-checked
                    (get-in closed-plan-runtime [:boundary :effects]))
                 (true? (get-in closed-plan-runtime
                                [:boundary :clojure-seed-boundary?]))
                 (false? (get-in closed-plan-runtime
                                 [:boundary :self-hosted?])))
            (and (= :complete
                    (get-in closed-plan-runtime [:validation :status]))
                 (= 1 (get-in closed-plan-runtime
                              [:invocation :invocation-count]))
                 (= :complete
                    (get-in closed-plan-runtime [:execution :status]))
                 (true? (:clojure-seed-boundary? closed-plan-runtime))
                 (false? (:self-hosted? closed-plan-runtime))))
          (or current-runtime-development?
              (= 1 (get-in closed-plan-runtime [:invocation :invocation-count])))
          (digest? (:record-hash closed-plan-runtime))
          (or current-runtime-development?
              (and (true? (:clojure-seed-boundary? closed-plan-runtime))
                   (false? (:self-hosted? closed-plan-runtime))))
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
        :missing-fact :complete-jvm-manifest}))
   :passed)))

(defn jvm-backend-validate-manifest!
  ([source-path manifest]
   (jvm-backend-fail!
    "B5-MANIFEST"
    "JVM manifest validation requires trusted artifact context"
    source-path nil
    {:missing-fact :jvm-manifest-validation-context}))
  ([source-path manifest expected-context]
   (jvm-backend-validate-manifest-core!
    source-path manifest expected-context nil)))

(defn- jvm-backend-validate-current-runtime-development-manifest!
  [source-path manifest expected-context authenticated-reference]
  (jvm-backend-validate-manifest-core!
   source-path manifest expected-context authenticated-reference))

(defn jvm-backend-preflight-output!
  [output-path source-path]
  (gravity.backend.jvm/preflight-output! #'c-backend-output-path-allowed? #'jvm-backend-fail! output-path source-path))
