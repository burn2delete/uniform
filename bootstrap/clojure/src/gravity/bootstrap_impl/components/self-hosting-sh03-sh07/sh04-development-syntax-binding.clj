(def ^:private sh04-development-syntax-contract
  {:artifact :gravity/sh04-syntax-object-contract :schema-version 1
   :owner :gravity-source :input :gravity/sh03-authenticated-reader-product
   :output :gravity/sh04-syntax-template-result
   :coordinator-adapter :gravity/sh04-to-c3-syntax-products-v1
   :sealed-product-kind :gravity/sh04-syntax-products
   :resolved-authoritative-result :gravity/sh04-syntax-object-artifact
   :identity-domain :gravity/sh04-syntax-object-v1
   :identity-fields [:form :span :source :namespace :phase :profile :metadata
                     :hygiene :origin :facts :reader-binding :reader-source-revision
                     :ownership :version]
   :physical-provenance-excluded-from-identity [:actual-source-path]
   :reference-order :prior-only :serialization :gravity-tagged-vector-v1
   :host-authority [:validate-sh03-boundary :resolve-digest-requests :bind-c3-artifact]
   :self-hosted? false :whole-language? false :clojure-seed-boundary? true})

(def ^:private sh04-development-syntax-diagnostics
  {:diagnostics ["C3-SHAPE" "C3-ID" "C3-SPAN" "C3-ORIGIN" "C3-HYGIENE"
                 "C3-CAPTURE" "C3-METADATA" "C3-FACT-STALE" "C3-SERIALIZE"]
   :stable-fields [:rule :severity :stage :primary :related :origin-chain
                   :profile :target :facts :remediation]
   :stops [:macro-expansion :name-resolution :analysis]})

(defn- sh04-development-syntax-source! [request-source selected-path]
  (try
    ;; Snapshot the original path before canonicalization can follow an alias.
    (let [snapshot (c2-pass-cache/source-snapshot! selected-path)
          expected (.getCanonicalPath (java.io.File. (sh04-syntax-resolve-source-path)))
          actual (.getCanonicalPath (java.io.File. (:canonical-path snapshot)))]
      (when-not (= expected actual)
        (sh04-syntax-boundary-fail!
         "C3-ID" request-source :selected-current-syntax-source nil {}))
      {:source-path actual :source-byte-count (:byte-count snapshot)
       :source-content-hash (:bytes-hash snapshot)
       :source-text
       (str (.decode
              (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
              (java.nio.ByteBuffer/wrap (:bytes snapshot))))})
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread)) (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (if (= "C3-ID" (:id (ex-data error))) (throw error)
        (sh04-syntax-boundary-fail! "C3-ID" request-source
          :stable-current-syntax-source nil {:cause-id (:id (ex-data error))})))
    (catch Exception error
      (sh04-syntax-boundary-fail! "C3-ID" request-source
        :strict-current-syntax-source nil {:cause-class (.getName (class error))}))))

(defn- sh04-syntax-selection! [request-source invocation]
  (when (some? invocation)
    (when-not (= :passed (:status (p15-s23-trusted-carrier-validation invocation :default-only)))
      (sh04-syntax-boundary-fail! "C3-ID" request-source :native-syntax-selection nil {}))
    (when (contains? invocation :syntax-source-selection)
      (let [selection (:syntax-source-selection invocation)]
        (when-not (and (map? selection) (= #{:schema :source-path} (set (keys selection)))
                       (= :gravity/sh04-development-syntax-selection-v1 (:schema selection))
                       (string? (:source-path selection)))
          (sh04-syntax-boundary-fail! "C3-ID" request-source :explicit-syntax-selection nil {}))
        (sh04-development-syntax-source! request-source (:source-path selection))))))

(defn- sh04-development-syntax-data! [request-source source]
  (let [forms (binding [*read-eval* false]
                (vec (read-forms (:source-path source) (:source-text source))))
        expected {'c3-syntax-object-contract sh04-development-syntax-contract
                  'c3-syntax-diagnostic-catalog sh04-development-syntax-diagnostics}]
    (into {}
      (for [[name value] expected]
        (let [definitions (filter #(and (seq? %) (= 'def (first %)) (= name (second %))) forms)
              definition (first definitions)
              actual (nth definition 2 nil)]
          (when-not (and (= 1 (count definitions)) (= 3 (count definition))
                         (= :passed (:status (p15-s23-trusted-carrier-validation actual :default-only)))
                         (= value actual))
            (sh04-syntax-boundary-fail! "C3-ID" request-source :syntax-public-DATA-contract name {}))
          [name actual])))))

(defn- sh04-development-syntax-interface! [request-source plan public-data identities]
  (let [exports (get-in plan [:module :exports])
        required (into (set (keys sh04-syntax-public-function-shapes)) (keys public-data))]
    (when-not
      (and (= :gravity/stage2-compiler-artifact-plan (:kind plan))
           (true? (:compiler-artifact-plan? plan))
           (= {:module 'gravity.bootstrap.syntax :profile :meta :target :jvm
               :effects #{} :capabilities #{} :safety :safe}
              (select-keys (:module plan) [:module :profile :target :effects :capabilities :safety]))
           (= :passed (:status (p15-s23-trusted-carrier-validation exports :default-only)))
           (= required (set exports)) (= (count required) (count exports))
           (= sh04-syntax-public-function-shapes (:public-function-shapes identities)))
      (sh04-syntax-boundary-fail! "C3-ID" request-source :current-syntax-module-and-exports nil {}))
    (vec (sort-by str required))))

(defn- sh04-development-syntax-binding! [request-source invocation]
  (let [source (sh04-syntax-selection! request-source invocation)
        _ (when-not source
            (sh04-syntax-boundary-fail! "C3-ID" request-source :explicit-syntax-selection nil {}))
        public-data (sh04-development-syntax-data! request-source source)
        rule (c-backend-stage2-plan-emitter-source-rule! (:source-path source) :jvm)
        raw-plan (p15-s23-stage2-compiler-artifact-plan
                   (:emitter rule) (:source-path source) (:source-text source))
        base (assoc-in (dissoc raw-plan :plan-id) [:source :byte-count] (:source-byte-count source))
        plan (assoc base :plan-id (str "sha256:" (sha256-hex (pr-str (c-backend-canonical-value base)))))
        identities (sh04-syntax-plan-identities plan)
        exports (sh04-development-syntax-interface! request-source plan public-data identities)
        after (sh04-syntax-selection! request-source invocation)
        after-rule (c-backend-stage2-plan-emitter-source-rule! (:source-path source) :jvm)
        _ (when-not (and (= source after) (= rule after-rule))
            (sh04-syntax-boundary-fail! "C3-ID" request-source :fresh-current-syntax-implementation nil {}))
        implementation
        (merge identities
          {:artifact :gravity/sh04-development-syntax-implementation-v1 :schema-version 1
           :admission-status :development-unadmitted
           :logical-source-path sh04-syntax-source-relative-path
           :source-byte-count (:source-byte-count source) :source-content-hash (:source-content-hash source)
           :exports exports :public-DATA public-data
           :compiler-identity (:compiler plan) :emitter-source-rule-hash (:source-rule-hash rule)})
        binding-id (p15-s23-c11-mir-digest implementation)]
    (merge source identities
      {:artifact :gravity/sh04-development-syntax-plan-binding-v1 :status :complete
       :semantic-authority :gravity-source :admission-status :development-unadmitted
       :compiled-by :clojure-stage0-seed :executed-by :clojure-stage2-generic-rule-runner
       :generic-bridge-residual? true :self-hosted? false :plan plan
       :implementation-binding-id binding-id
       :implementation-projection (assoc implementation :binding-id binding-id)})))

(defn- sh04-development-syntax-binding? [binding]
  (= :gravity/sh04-development-syntax-plan-binding-v1 (:artifact binding)))

(defn- sh04-development-syntax-context! [source-path invocation]
  (when (sh04-syntax-selection! source-path invocation)
    ;; Keep the two-key Reader authority projection distinct from the operation.
    (let [reader-invocation (select-keys invocation [:reader-source-path :authenticated-envelope-options])
          reader (sh03-reader-verification-invocation! source-path reader-invocation)
          selected (sh04-syntax-selection! source-path invocation)]
      {:reader-source-path (get-in reader [:binding :source-path])
       :authenticated-envelope-options (:envelope-options reader)
       :syntax-source-selection {:schema :gravity/sh04-development-syntax-selection-v1
                                 :source-path (:source-path selected)}})))

(defn- sh04-development-syntax-verification-context! [invocation c2-artifact boundary]
  (when (sh04-syntax-selection! "<sh04-selected-verification>" invocation)
    (when-not (and (= :passed (:status (p15-s23-trusted-carrier-validation c2-artifact :default-only)))
                   (= :passed (:status (p15-s23-trusted-carrier-validation boundary :default-only)))
                   (map? c2-artifact) (map? boundary))
      (sh04-syntax-boundary-fail! "C3-ID" "<sh04-selected-verification>"
        :native-selected-syntax-products nil {}))
    (let [path (get-in c2-artifact [:source-unit-record :path])
          context (sh04-development-syntax-context! path invocation)]
      (when (:registered-literal-projection c2-artifact)
        (sh04-syntax-boundary-fail! "C3-ID" path
          :selected-syntax-registered-projection-unsupported nil {}))
      (when-not (= context (:operation-context boundary))
        (sh04-syntax-boundary-fail! "C3-ID" path :same-syntax-operation-context nil {}))
      context)))

(defn- sh04-syntax-verification-binding! [source-path invocation boundary]
  (let [binding (sh04-syntax-current-binding! source-path invocation)]
    (when (and (sh04-development-syntax-binding? binding)
               (not (and (= :passed (:status (p15-s23-trusted-carrier-validation boundary :default-only)))
                          (map? boundary)
                          (= (dissoc binding :plan :source-text) (:plan-binding boundary)))))
      (sh04-syntax-boundary-fail! "C3-ID" source-path :fresh-selected-syntax-binding nil {}))
    binding))
