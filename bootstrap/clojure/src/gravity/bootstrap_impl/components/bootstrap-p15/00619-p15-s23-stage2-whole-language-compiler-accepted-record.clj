

(defn p15-s23-stage2-whole-language-compiler-accepted-record
  [source-record driver-artifact whole-compiler-artifact]
  (let [driver-accepted (:accepted-record driver-artifact)
        whole-accepted
        (:accepted-application-compile-record whole-compiler-artifact)
        stage2-output (:stage2-driver-output driver-accepted)
        current-output (:stdout whole-accepted)]
    {:artifact
     :gravity/p15-s23-stage2-whole-language-compiler-accepted-run-record
     :fixture p15-s23-accepted-app-source-path
     :compiler-source-modules-accepted?
     (= :complete (:status source-record))
     :stage2-compiler-driver-artifact-id (:artifact-id driver-artifact)
     :current-stage-compiler-artifact-id
     (get-in whole-compiler-artifact
             [:compiler-artifact-manifest :compiler-artifact-id])
     :stage2-plan-id (:stage2-plan-id driver-accepted)
     :reference-stage0-plan-id (:reference-stage0-plan-id driver-accepted)
     :stage2-plan-emitted?
     (true? (:stage2-plan-emitted? driver-accepted))
     :stage2-runtime-executed?
     (true? (:stage2-runtime-executed? driver-accepted))
     :stage2-output stage2-output
     :current-stage-output current-output
     :expected-output p15-s23-accepted-app-expected-stdout
     :stage2-output-equivalent-to-current-stage?
     (= stage2-output current-output p15-s23-accepted-app-expected-stdout)
     :binding-table-equivalent?
     (true? (:binding-table-equivalent? driver-accepted))
     :function-instructions-equivalent?
     (true? (:function-instructions-equivalent? driver-accepted))
     :instruction-summary-equivalent?
     (true? (:instruction-summary-equivalent? driver-accepted))
     :effect-summary-equivalent?
     (true? (:effect-summary-equivalent? driver-accepted))
     :status
     (if (and (= :complete (:status source-record))
              (= :complete (:status driver-accepted))
              (true? (:stage2-plan-emitted? driver-accepted))
              (true? (:stage2-runtime-executed? driver-accepted))
              (= stage2-output current-output
                 p15-s23-accepted-app-expected-stdout))
       :complete
       :failed)}))

(defn p15-s23-stage2-whole-language-compiler-rejected-record
  [driver-artifact whole-compiler-artifact]
  (let [driver-rejected (:rejected-record driver-artifact)
        whole-rejected
        (:rejected-application-diagnostic-record whole-compiler-artifact)
        observed (set (:observed-diagnostics driver-rejected))
        current (set (:diagnostics whole-rejected))
        expected #{"L2-FUNCTION-ARITY" "L2-BUILTIN-ARITY"}]
    {:artifact
     :gravity/p15-s23-stage2-whole-language-compiler-rejected-diagnostic-record
     :fixtures (:fixtures whole-rejected)
     :stage2-observed-diagnostics (vec (sort observed))
     :current-stage-diagnostics (vec (sort current))
     :expected-diagnostics (vec (sort expected))
     :all-fixtures-rejected?
     (and (= :complete (:status driver-rejected))
          (true? (:all-fixtures-rejected? whole-rejected)))
     :diagnostics-equivalent-to-current-stage?
     (= observed current expected)
     :diagnostic-codes-stable?
     (= observed expected)
     :status
     (if (and (= :complete (:status driver-rejected))
              (true? (:all-fixtures-rejected? whole-rejected))
              (= observed current expected))
       :complete
       :failed)}))

(defn p15-s23-stage2-whole-language-compiler-boundary-record
  [proof-contract driver-artifact whole-compiler-artifact tcb-artifact]
  (let [driver-boundary (:boundary-record driver-artifact)
        whole-boundary
        (:residual-trusted-boundary-record whole-compiler-artifact)
        tcb-boundaries
        (get-in tcb-artifact
                [:residual-trust-boundary-record :residual-boundaries])
        claims (:self-hosting-claims proof-contract)]
    {:artifact
     :gravity/p15-s23-stage2-whole-language-compiler-boundary-record
     :stage2-compiler-driver-executed?
     (true? (:stage2-compiler-driver-executed? driver-boundary))
     :stage0-compiler-driver-replaced?
     (true? (:stage0-compiler-driver-replaced? driver-boundary))
     :stage0-rule-runner-replaced?
     (true? (:stage0-rule-runner-replaced? driver-boundary))
     :stage0-reader-replaced?
     (true? (:stage0-reader-replaced? driver-boundary))
     :stage0-macro-expander-replaced?
     (true? (:stage0-macro-expander-replaced? driver-boundary))
     :stage2-source-front-end-used?
     (true? (:stage2-source-front-end-used? driver-boundary))
     :stage2-front-end-executor-used?
     (true? (:stage2-front-end-executor-used? driver-boundary))
     :stage2-runtime-kernel-used?
     (true? (:stage2-runtime-kernel-used? driver-boundary))
     :stage2-runtime-executor-used?
     (true? (:stage2-runtime-executor-used? driver-boundary))
     :clojure-stage0-driver-host?
     (true? (:clojure-stage0-driver-host? driver-boundary))
     :clojure-stage0-verifier? true
     :clojure-stage0-release-compiler? true
     :clojure-stage0-runtime-host?
     (true? (:clojure-stage0-runtime-host? driver-boundary))
     :clojure-host-primitive-boundary?
     (true? (:clojure-host-primitive-boundary? driver-boundary))
     :gravity-runtime-primitives?
     (true? (:gravity-runtime-primitives? driver-boundary))
     :current-stage-residual-boundary-recorded?
     (= :complete (:status whole-boundary))
     :residual-tcb-boundaries tcb-boundaries
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? claims))
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? claims))
     :status
     (if (and (true? (:stage2-compiler-driver-executed?
                     driver-boundary))
              (true? (:stage0-compiler-driver-replaced?
                      driver-boundary))
              (true? (:stage0-rule-runner-replaced?
                      driver-boundary))
              (true? (:stage0-reader-replaced? driver-boundary))
              (true? (:stage0-macro-expander-replaced?
                      driver-boundary))
              (true? (:stage2-source-front-end-used?
                      driver-boundary))
              (true? (:stage2-front-end-executor-used?
                      driver-boundary))
              (true? (:stage2-runtime-kernel-used?
                      driver-boundary))
              (true? (:stage2-runtime-executor-used?
                      driver-boundary))
              (true? (:clojure-stage0-driver-host?
                      driver-boundary))
              (false? (:clojure-stage0-runtime-host?
                       driver-boundary))
              (false? (:clojure-host-primitive-boundary?
                       driver-boundary))
              (true? (:gravity-runtime-primitives?
                      driver-boundary))
              (= :complete (:status whole-boundary))
              (contains? (set tcb-boundaries)
                         :clojure-stage0-bootstrap)
              (false? (:full-language-compiler-self-hosted?
                       claims))
              (false? (:clojure-seed-retired? claims)))
       :complete
       :failed)}))

(defn p15-s23-stage2-whole-language-compiler-lineage-record
  [source-path proof-contract inventory-artifact driver-artifact
   whole-compiler-artifact provenance-artifact]
  {:artifact
   :gravity/p15-s23-stage2-whole-language-compiler-lineage-record
   :source-path source-path
   :source-language :gravity
   :compiler-stage (:stage proof-contract)
   :compiled-by :gravity-stage2-compiler-driver
   :executed-by :gravity-stage2-runtime-kernel
   :verified-by :clojure-stage0-verifier
   :source-inventory-id (:inventory-id inventory-artifact)
   :stage2-compiler-driver-artifact-id (:artifact-id driver-artifact)
   :current-stage-compiler-artifact-id
   (get-in whole-compiler-artifact
           [:compiler-artifact-manifest :compiler-artifact-id])
   :bootstrap-provenance-record-id
   (get-in provenance-artifact
           [:bootstrap-provenance-record :provenance-record-id])
   :lineage-traversable-to-seed?
   (get-in provenance-artifact
           [:compiler-lineage-graph :lineage-traversable-to-seed?])
   :residual-verifier :clojure-stage0-verifier
   :residual-release-compiler :clojure-stage0-release-compiler
   :status
   (if (and (re-find #"^sha256:" (str (:inventory-id inventory-artifact)))
            (re-find #"^sha256:" (str (:artifact-id driver-artifact)))
            (re-find #"^sha256:"
                     (str (get-in whole-compiler-artifact
                                  [:compiler-artifact-manifest
                                   :compiler-artifact-id])))
            (true?
             (get-in provenance-artifact
                     [:compiler-lineage-graph
                      :lineage-traversable-to-seed?])))
     :complete
     :failed)})