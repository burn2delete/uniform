(def p15-s23-b3-llvm-max-final-artifact-carrier-nodes 65536)
(def p15-s23-b3-llvm-max-final-artifact-carrier-depth 512)

(defn- p15-s23-b3-llvm-safe-carrier-facts
  [validation]
  (select-keys validation
               [:reason :observed-nodes :observed-depth
                :maximum-nodes :maximum-depth :maximum-width]))

(defn- p15-s23-b3-llvm-require-trusted-final-carrier!
  [source-path artifact]
  (let [validation
        (p15-s23-trusted-carrier-validation
         artifact :default-only
         p15-s23-b3-llvm-max-final-artifact-carrier-nodes
         p15-s23-b3-llvm-max-final-artifact-carrier-depth
         p15-s23-b3-llvm-max-final-artifact-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       (assoc (p15-s23-b3-llvm-safe-carrier-facts validation)
              :missing-fact
              :trusted-comparator-free-b3-final-artifact-carrier)))
    validation))

(defn- p15-s23-b3-llvm-validated-options!
  [source-path options]
  (let [validation
        (p15-s23-trusted-carrier-validation options :reject 16 4 4)]
    (when-not (= :passed (:status validation))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       (assoc (p15-s23-b3-llvm-safe-carrier-facts validation)
              :missing-fact :trusted-comparator-free-b3-options)))
    (let [class-name (when (some? options) (.getName (class options)))
          option-keys (when (map? options) (set (keys options)))
          output-directory
          (when (contains? option-keys :output-directory)
            (:output-directory options))]
      (when-not
       (and (contains? p15-s23-trusted-carrier-map-classes class-name)
          (contains? #{#{} #{:output-directory}
                       #{:run-linux-development-tools?}
                       #{:output-directory :run-linux-development-tools?}}
                     option-keys)
          (or (not (contains? option-keys :run-linux-development-tools?))
              (boolean? (:run-linux-development-tools? options)))
            (or (nil? output-directory) (string? output-directory)))
        (p15-s23-b3-llvm-fail!
         "B3-MANIFEST" source-path {}
         {:missing-fact :trusted-comparator-free-b3-options
          :bounded-reason :exact-b3-output-directory-options}))
      {:output-directory output-directory
       :run-linux-development-tools?
       (true? (:run-linux-development-tools? options))})))

(defn- p15-s23-b3-llvm-verify-integrity!
  ([artifact]
   (p15-s23-b3-llvm-verify-integrity! artifact :final))
  ([artifact publication-phase]
   (p15-s23-b3-llvm-require-trusted-final-carrier!
    "<b3-llvm>" artifact)
   (when-not
    (and (contains? #{:pre-final :final} publication-phase)
         (if (= :pre-final publication-phase)
           (and (nil? (get-in artifact
                              [:actual-path-provenance :publication-path]))
                (not (contains? (:actual-path-provenance artifact)
                                :publication-receipt)))
           (map? (get-in artifact
                         [:actual-path-provenance
                          :publication-receipt])))
         (= :gravity/p15-s23-b3-authenticated-llvm-artifact
           (:kind artifact))
        (p15-s23-b3-llvm-frozen-contract-valid? artifact)
        (= (:semantic-id artifact)
           (p15-s23-b3-llvm-artifact-id artifact))
        (= (:artifact-id artifact)
           (p15-s23-c11-mir-digest
            {:kind (:kind artifact) :schema-version (:schema-version artifact)
             :semantic-id (:semantic-id artifact)}))
        (= (:actual-path-binding-id artifact)
           (p15-s23-b3-llvm-actual-path-binding-id
            (:semantic-id artifact) (:actual-path-provenance artifact)))
        (= (get-in artifact [:c14-request :request-id])
           (p15-s23-c11-mir-digest
            {:kind :gravity/c14-bounded-llvm-lowering-request
             :request
             (dissoc (:c14-request artifact) :request-id)}))
        (= (get-in artifact [:b1-packet :input :verifier-report-id])
           (p15-s23-c11-mir-digest
            (get-in artifact [:b1-packet :input :verifier-report])))
        (= (get-in artifact [:b3-record :lowering-id])
           (p15-s23-c11-mir-digest
            (dissoc (:lowering artifact)
                    :clojure-seed-boundary? :self-hosted?)))
        (= (get-in artifact [:b13-record :content-hashes])
           (into
            (sorted-map)
            (map (fn [[kind record]] [kind (:content-hash record)]))
            (get-in artifact [:b13-record :artifact-files])))
        (= (get-in artifact [:b13-record :build-id])
           (p15-s23-c11-mir-digest
            (dissoc (get-in artifact [:b13-record :build-identity])
                    :build-id)))
        (= (get-in artifact [:b13-record :build-id])
           (get-in artifact [:b13-record :build-identity :build-id]))
        (= (get-in artifact [:b13-record :content-hashes])
           (get-in artifact
                   [:b13-record :build-identity
                    :artifact-content-hashes]))
        (every?
         #(= (get-in artifact [:b13-record :build-id])
             (:bundle-build-id %))
         (vals (get-in artifact [:b13-record :artifact-files])))
        (= (get-in artifact
                   [:b13-record :reproducibility
                    :environment-inputs-digest])
           (p15-s23-c11-mir-digest
            (get-in artifact
                    [:b13-record :reproducibility
                     :environment-inputs])))
        (= (get-in artifact
                   [:b13-record :reproducibility
                    :target-toolchain-digest])
           (p15-s23-c11-mir-digest
            (get-in artifact
                    [:toolchain-evidence :toolchain-fingerprint])))
        (= (get-in artifact
                   [:b13-record :reproducibility
                    :target-toolchain-digest])
           (get-in artifact
                   [:b13-record :compiler-provenance
                    :target-toolchain-digest]))
        (= (get-in artifact
                   [:b13-record :reproducibility
                    :pass-pipeline-digest])
           (p15-s23-c11-mir-digest
            {:c11 (get-in artifact
                          [:b13-record :pass-provenance :c11])
             :c13 (get-in artifact
                          [:b13-record :pass-provenance :c13])
             :b3 (get-in artifact
                         [:b13-record :pass-provenance :b3])
             :optimization-level
             (get-in artifact
                     [:b3-record :pass-record :optimization-level])
             :ub-sensitive-flags
             (get-in artifact
                     [:b3-record :pass-record :ub-sensitive-flags])}))
        (= (get-in artifact
                   [:b13-record :reproducibility
                    :pass-pipeline-digest])
           (get-in artifact
                   [:b13-record :pass-provenance
                    :pass-pipeline-digest]))
        (= {:artifact-kind :llvm-ir
            :logical-path "program.ll" :mode "0644"}
           (select-keys
            (get-in artifact [:b13-record :artifact-files :llvm-ir])
            [:artifact-kind :logical-path :mode]))
        (= {:artifact-kind :mach-o-object
            :logical-path "program.o" :mode "0644"
            :format :mach-o :architecture :arm64}
           (select-keys
            (get-in artifact [:b13-record :artifact-files :object])
            [:artifact-kind :logical-path :mode :format :architecture]))
        (= {:artifact-kind :mach-o-executable
            :logical-path "program" :mode "0755"
            :format :mach-o :architecture :arm64}
           (select-keys
            (get-in artifact [:b13-record :artifact-files :executable])
            [:artifact-kind :logical-path :mode :format :architecture]))
        (not (contains? (:toolchain-evidence artifact)
                        :publication-payload))
        (= :jvm (get-in artifact
                        [:c14-request :source-target-selection
                         :source-declaration-target]))
        (= :llvm (get-in artifact
                         [:c14-request :source-target-selection
                          :requested-lowering-target]))
        (false? (get-in artifact
                        [:c14-request :source-target-selection
                         :direct-source-declared-llvm?]))
        (true? (get-in artifact [:b14-record :same-result?]))
        (= :closed (get-in artifact [:c18-record :release-gate]))
        (true? (:seed-boundary? artifact))
        (false? (:c11-llvm-credit? artifact))
        (false? (:target-lowering-credit? artifact))
        (false? (:backend-credit? artifact))
        (false? (:public-target? artifact))
        (false? (:release-credit? artifact))
         (false? (:self-hosted? artifact)))
     (p15-s23-b3-llvm-fail!
      "B3-MANIFEST"
      (get-in artifact [:actual-path-provenance :source]) artifact
      {:missing-fact :content-bound-final-b3-artifact}))
   :passed))
