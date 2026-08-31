

(defn p15-s23-stage3-seedless-compiler-candidate-evidence-link-record
  [stage2-whole whole-compiler driver source-front-end front-end-executor
   plan-emitter runtime-executor runtime-kernel accepted rejected]
  (let [links
        [{:link :stage2-whole-language-compiler
          :artifact (:kind stage2-whole)
          :artifact-id (:artifact-id stage2-whole)}
         {:link :whole-language-compiler-artifact
          :artifact (:kind whole-compiler)
          :artifact-id (:artifact-id whole-compiler)}
         {:link :stage2-compiler-driver
          :artifact (:kind driver)
          :artifact-id (:artifact-id driver)}
         {:link :stage2-source-front-end
          :artifact (:kind source-front-end)
          :artifact-id (:artifact-id source-front-end)}
         {:link :stage2-front-end-executor
          :artifact (:kind front-end-executor)
          :artifact-id (:artifact-id front-end-executor)}
         {:link :stage2-plan-emitter
          :artifact (:kind plan-emitter)
          :artifact-id (:artifact-id plan-emitter)}
         {:link :stage2-runtime-executor
          :artifact (:kind runtime-executor)
          :artifact-id (:artifact-id runtime-executor)}
         {:link :stage2-runtime-kernel
          :artifact (:kind runtime-kernel)
          :artifact-id (:artifact-id runtime-kernel)}
         {:link :accepted-app-execution-proof
          :artifact (:kind accepted)
          :artifact-id (:artifact-id accepted)}
         {:link :rejected-app-diagnostic-proof
          :artifact (:kind rejected)
          :artifact-id (:artifact-id rejected)}]
        links-with-status
        (mapv (fn [link]
                (assoc link
                       :status
                       (if (re-find #"^sha256:"
                                    (str (:artifact-id link)))
                         :verified
                         :missing)))
              links)
        covered (set (map :link links-with-status))]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-evidence-link-record
     :links links-with-status
     :required-links
     (vec (sort p15-s23-stage3-seedless-compiler-candidate-required-links))
     :required-links-covered?
     (= p15-s23-stage3-seedless-compiler-candidate-required-links covered)
     :all-artifacts-identified?
     (every? #(= :verified (:status %)) links-with-status)
     :status
     (if (and (= p15-s23-stage3-seedless-compiler-candidate-required-links
                 covered)
              (every? #(= :verified (:status %)) links-with-status))
       :complete
       :failed)}))

(defn p15-s23-stage3-seedless-compiler-candidate-accepted-record
  [source-record candidate-record stage2-whole driver whole-compiler]
  (let [driver-accepted (:accepted-record driver)
        stage2-accepted (:accepted-record stage2-whole)
        whole-accepted
        (:accepted-application-compile-record whole-compiler)
        output (:stage2-driver-output driver-accepted)
        current-output (:stdout whole-accepted)]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-accepted-run-record
     :fixture p15-s23-accepted-app-source-path
     :compiler-source-modules-accepted?
     (= :complete (:status source-record))
     :candidate-compile-path-complete?
     (= :complete (:status candidate-record))
     :stage2-whole-language-compiler-artifact-id (:artifact-id stage2-whole)
     :stage2-compiler-driver-artifact-id (:artifact-id driver)
     :stage2-plan-id (:stage2-plan-id driver-accepted)
     :stage2-whole-language-stage-plan-id
     (:stage2-plan-id stage2-accepted)
     :seedless-candidate-output output
     :stage2-whole-language-output (:stage2-output stage2-accepted)
     :current-stage-output current-output
     :expected-output p15-s23-accepted-app-expected-stdout
     :accepted-output-equivalent?
     (= output
        (:stage2-output stage2-accepted)
        current-output
        p15-s23-accepted-app-expected-stdout)
     :stage2-plan-emitted?
     (true? (:stage2-plan-emitted? driver-accepted))
     :stage2-runtime-executed?
     (true? (:stage2-runtime-executed? driver-accepted))
     :binding-table-equivalent?
     (true? (:binding-table-equivalent? driver-accepted))
     :function-instructions-equivalent?
     (true? (:function-instructions-equivalent? driver-accepted))
     :instruction-summary-equivalent?
     (true? (:instruction-summary-equivalent? driver-accepted))
     :status
     (if (and (= :complete (:status source-record))
              (= :complete (:status candidate-record))
              (= :complete (:status driver-accepted))
              (true? (:stage2-plan-emitted? driver-accepted))
              (true? (:stage2-runtime-executed? driver-accepted))
              (= output
                 (:stage2-output stage2-accepted)
                 current-output
                 p15-s23-accepted-app-expected-stdout))
       :complete
       :failed)}))

(defn p15-s23-stage3-seedless-compiler-candidate-rejected-record
  [stage2-whole driver whole-compiler]
  (let [driver-rejected (:rejected-record driver)
        stage2-rejected (:rejected-record stage2-whole)
        whole-rejected
        (:rejected-application-diagnostic-record whole-compiler)
        observed (set (:observed-diagnostics driver-rejected))
        stage2-observed (set (:stage2-observed-diagnostics stage2-rejected))
        current (set (:diagnostics whole-rejected))
        expected #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-rejected-diagnostic-record
     :fixtures (:fixtures whole-rejected)
     :seedless-candidate-diagnostics (vec (sort observed))
     :stage2-whole-language-diagnostics (vec (sort stage2-observed))
     :current-stage-diagnostics (vec (sort current))
     :expected-diagnostics (vec (sort expected))
     :all-fixtures-rejected?
     (and (= :complete (:status driver-rejected))
          (true? (:all-fixtures-rejected? whole-rejected)))
     :diagnostics-equivalent?
     (= observed stage2-observed current expected)
     :diagnostic-codes-stable? (= observed expected)
     :status
     (if (and (= :complete (:status driver-rejected))
              (true? (:all-fixtures-rejected? whole-rejected))
              (= observed stage2-observed current expected))
       :complete
       :failed)}))

(defn p15-s23-stage3-seedless-compiler-candidate-boundary-record
  [proof-contract driver stage2-whole]
  (let [seed-boundary (:seed-boundary proof-contract)
        claims (:self-hosting-claims proof-contract)
        driver-boundary (:boundary-record driver)
        stage2-boundary (:boundary-record stage2-whole)]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-boundary-record
     :compiler-path-uses-clojure-seed?
     (true? (:compiler-path-uses-clojure-seed? seed-boundary))
     :candidate-seedless?
     (true? (:candidate-seedless? seed-boundary))
     :verifier-boundary (:verifier-boundary seed-boundary)
     :release-compiler-boundary (:release-compiler-boundary seed-boundary)
     :clojure-stage0-verifier?
     (true? (:clojure-stage0-verifier? seed-boundary))
     :clojure-stage0-release-compiler?
     (true? (:clojure-stage0-release-compiler? seed-boundary))
     :stage2-compiler-driver-executed?
     (true? (:stage2-compiler-driver-executed? driver-boundary))
     :stage2-runtime-kernel-used?
     (true? (:stage2-runtime-kernel-used? driver-boundary))
     :stage2-whole-language-stage-present?
     (= :complete (:status stage2-boundary))
     :stage2-whole-language-recorded-clojure-verifier?
     (true? (:clojure-stage0-verifier? stage2-boundary))
     :stage2-whole-language-recorded-clojure-release-compiler?
     (true? (:clojure-stage0-release-compiler? stage2-boundary))
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? claims))
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? claims))
     :seedless-compiler-candidate?
     (true? (:seedless-compiler-candidate? claims))
     :status
     (if (and (false? (:compiler-path-uses-clojure-seed?
                      seed-boundary))
              (true? (:candidate-seedless? seed-boundary))
              (= :gravity-stage3-verifier
                 (:verifier-boundary seed-boundary))
              (= :gravity-stage3-release-compiler
                 (:release-compiler-boundary seed-boundary))
              (false? (:clojure-stage0-verifier? seed-boundary))
              (false? (:clojure-stage0-release-compiler?
                       seed-boundary))
              (true? (:stage2-compiler-driver-executed?
                      driver-boundary))
              (true? (:stage2-runtime-kernel-used? driver-boundary))
              (false? (:full-language-compiler-self-hosted? claims))
              (false? (:clojure-seed-retired? claims))
              (true? (:seedless-compiler-candidate? claims)))
       :complete
       :failed)}))

(defn p15-s23-stage3-seedless-compiler-candidate-lineage-record
  [source-path proof-contract stage2-whole provenance]
  (let [lineage (:lineage proof-contract)
        replaces (set (:replaces lineage))]
    {:artifact
     :gravity/p15-s23-stage3-seedless-compiler-candidate-lineage-record
     :source-path source-path
     :source-language (:source-language lineage)
     :compiled-by (:compiled-by lineage)
     :verified-by (:verified-by lineage)
     :release-compiled-by (:release-compiled-by lineage)
     :executed-by (:executed-by lineage)
     :replaces (vec (sort replaces))
     :stage2-whole-language-compiler-artifact-id (:artifact-id stage2-whole)
     :provenance-artifact-id (:artifact-id provenance)
     :lineage-traversable-to-seed?
     (true? (get-in provenance
                    [:compiler-lineage-graph
                     :lineage-traversable-to-seed?]))
     :status
     (if (and (= :gravity (:source-language lineage))
              (= :gravity-stage2-compiler-driver
                 (:compiled-by lineage))
              (= :gravity-stage3-verifier (:verified-by lineage))
              (= :gravity-stage3-release-compiler
                 (:release-compiled-by lineage))
              (= :gravity-stage2-runtime-kernel
                 (:executed-by lineage))
              (set/subset?
               #{:clojure-stage0-verifier
                 :clojure-stage0-release-compiler}
               replaces)
              (true? (get-in provenance
                             [:compiler-lineage-graph
                              :lineage-traversable-to-seed?])))
       :complete
       :failed)}))