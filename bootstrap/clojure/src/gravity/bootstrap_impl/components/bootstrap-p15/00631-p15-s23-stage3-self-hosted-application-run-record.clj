

(defn p15-s23-stage3-self-hosted-application-run-record
  [proof-contract equivalence candidate driver runtime]
  (let [output
        (get-in equivalence [:accepted-record :candidate-output])
        app-artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:fixture p15-s23-accepted-app-source-path
                       :output output
                       :toolchain (:artifact-id candidate)
                       :driver (:artifact-id driver)
                       :runtime (:artifact-id runtime)})))
        boundary (:toolchain-boundary proof-contract)]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-run-record
     :fixture p15-s23-accepted-app-source-path
     :application-artifact-id app-artifact-id
     :compiled-by (:compiled-by boundary)
     :compiler (:compiler boundary)
     :verified-by (:verified-by boundary)
     :executed-by (:executed-by boundary)
     :stage3-toolchain-uses-clojure-seed?
     (true? (:stage3-toolchain-uses-clojure-seed? boundary))
     :expected-output p15-s23-accepted-app-expected-stdout
     :stdout output
     :accepted-output-equivalent?
     (= output p15-s23-accepted-app-expected-stdout)
     :stage3-equivalence-complete?
     (true? (get-in equivalence
                    [:capability-based-proof
                     :stage3-equivalence-bundle-present?]))
     :candidate-seedless?
     (true? (get-in candidate
                    [:capability-based-proof
                     :compiler-path-seedless?]))
     :driver-executed?
     (true? (get-in driver
                    [:capability-based-proof
                     :stage2-compiler-driver-executed?]))
     :runtime-kernel-executed?
     (true? (get-in runtime
                    [:capability-based-proof
                     :stage2-runtime-kernel-executed?]))
     :status
     (if (and (= output p15-s23-accepted-app-expected-stdout)
              (true? (get-in equivalence
                             [:capability-based-proof
                              :stage3-equivalence-bundle-present?]))
              (true? (get-in candidate
                             [:capability-based-proof
                              :compiler-path-seedless?]))
              (true? (get-in driver
                             [:capability-based-proof
                              :stage2-compiler-driver-executed?]))
              (true? (get-in runtime
                             [:capability-based-proof
                              :stage2-runtime-kernel-executed?]))
              (false? (:stage3-toolchain-uses-clojure-seed? boundary)))
       :complete
       :failed)}))

(defn p15-s23-stage3-self-hosted-application-rejected-record
  [equivalence rejected]
  (let [stage3-diagnostics
        (set (get-in equivalence
                     [:rejected-record :candidate-diagnostics]))
        rejected-diagnostics
        (set (map :diagnostic
                  (:rejected-app-diagnostic-records rejected)))
        expected #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-rejected-record
     :rejected-fixtures (mapv :fixture p15-s23-rejected-app-fixtures)
     :stage3-diagnostics (vec (sort stage3-diagnostics))
     :rejected-proof-diagnostics (vec (sort rejected-diagnostics))
     :expected-diagnostics (vec (sort expected))
     :rejected-diagnostics-equivalent?
     (= stage3-diagnostics rejected-diagnostics expected)
     :status
     (if (= stage3-diagnostics rejected-diagnostics expected)
       :complete
       :failed)}))

(defn p15-s23-stage3-self-hosted-application-toolchain-record
  [proof-contract equivalence candidate driver runtime]
  (let [boundary (:toolchain-boundary proof-contract)]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-toolchain-record
     :compiler (:compiler boundary)
     :compiled-by (:compiled-by boundary)
     :verified-by (:verified-by boundary)
     :executed-by (:executed-by boundary)
     :stage3-toolchain-uses-clojure-seed?
     (true? (:stage3-toolchain-uses-clojure-seed? boundary))
     :stage3-self-hosted-application-run?
     (true? (:stage3-self-hosted-application-run? boundary))
     :rejected-application-fails-closed?
     (true? (:rejected-application-fails-closed? boundary))
     :final-seed-retirement-proof-present?
     (true? (:final-seed-retirement-proof-present? boundary))
     :equivalence-bundle-complete?
     (true? (get-in equivalence
                    [:capability-based-proof
                     :stage3-equivalence-bundle-present?]))
     :candidate-seedless?
     (true? (get-in candidate
                    [:capability-based-proof
                     :compiler-path-seedless?]))
     :stage2-driver-uses-gravity-front-end?
     (true? (get-in driver
                    [:capability-based-proof
                     :stage2-source-front-end-used?]))
     :runtime-uses-gravity-primitives?
     (true? (get-in runtime
                    [:capability-based-proof
                     :gravity-runtime-primitives-used?]))
     :status
     (if (and (false? (:stage3-toolchain-uses-clojure-seed? boundary))
              (true? (:stage3-self-hosted-application-run? boundary))
              (true? (:rejected-application-fails-closed? boundary))
              (false? (:final-seed-retirement-proof-present? boundary))
              (true? (get-in equivalence
                             [:capability-based-proof
                              :stage3-equivalence-bundle-present?]))
              (true? (get-in candidate
                             [:capability-based-proof
                              :compiler-path-seedless?]))
              (true? (get-in driver
                             [:capability-based-proof
                              :stage2-source-front-end-used?]))
              (true? (get-in runtime
                             [:capability-based-proof
                              :gravity-runtime-primitives-used?])))
       :complete
       :failed)}))

(defn p15-s23-stage3-self-hosted-application-runtime-record
  [runtime accepted]
  (let [runtime-proof (:capability-based-proof runtime)
        accepted-output
        (get-in accepted [:accepted-output-comparison :accepted-stdout])]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-runtime-record
     :runtime-artifact-id (:artifact-id runtime)
     :runtime-kind (:kind runtime)
     :accepted-output accepted-output
     :runtime-kernel-present?
     (true? (:stage2-runtime-kernel-present? runtime-proof))
     :runtime-kernel-executed?
     (true? (:stage2-runtime-kernel-executed? runtime-proof))
     :gravity-runtime-primitives-used?
     (true? (:gravity-runtime-primitives-used? runtime-proof))
     :capability-proof-present?
     (true? (get-in runtime
                    [:capability-based-proof
                     :does-not-use-clojure-runtime-primitives?]))
     :accepted-output-equivalent?
     (= accepted-output p15-s23-accepted-app-expected-stdout)
     :status
     (if (and (true? (:stage2-runtime-kernel-present? runtime-proof))
              (true? (:stage2-runtime-kernel-executed? runtime-proof))
              (true? (:gravity-runtime-primitives-used? runtime-proof))
              (true? (:does-not-use-clojure-runtime-primitives?
                      runtime-proof))
              (= accepted-output p15-s23-accepted-app-expected-stdout))
       :complete
       :failed)}))

(defn p15-s23-stage3-self-hosted-application-boundary-record
  [proof-contract run rejected toolchain runtime]
  (let [claims (:self-hosting-claims proof-contract)]
    {:artifact :gravity/p15-s23-stage3-self-hosted-application-boundary-record
     :stage3-self-hosted-application-run?
     (true? (:stage3-self-hosted-application-run? claims))
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? claims))
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? claims))
     :final-seed-retirement-proof-present?
     (true? (get-in proof-contract
                    [:toolchain-boundary
                     :final-seed-retirement-proof-present?]))
     :accepted-run-complete? (= :complete (:status run))
     :rejected-run-complete? (= :complete (:status rejected))
     :toolchain-record-complete? (= :complete (:status toolchain))
     :runtime-record-complete? (= :complete (:status runtime))
     :status
     (if (and (true? (:stage3-self-hosted-application-run? claims))
              (false? (:full-language-compiler-self-hosted? claims))
              (false? (:clojure-seed-retired? claims))
              (false? (get-in proof-contract
                              [:toolchain-boundary
                               :final-seed-retirement-proof-present?]))
              (= :complete (:status run))
              (= :complete (:status rejected))
              (= :complete (:status toolchain))
              (= :complete (:status runtime)))
       :complete
       :failed)}))