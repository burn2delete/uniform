

(defn p15-s23-stage2-front-end-executor-accepted-record
  [executor front-end emitter runtime]
  (let [source-path p15-s23-accepted-app-source-path
        source-text (slurp source-path)
        front-end-record
        (p15-s23-stage2-front-end-source-module-record
         front-end source-path source-text)
        module (:module front-end-record)
        stage2-plan (p15-s23-stage2-emitted-core-plan
                     emitter source-path source-text module)
        runtime-record
        (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
        reference-macro-artifact
        (macro-source-artifact source-path source-text)
        read-form-parity?
        (= (:forms front-end-record) (read-forms source-path source-text))
        macro-expanded-form-parity?
        (= (:expanded-forms front-end-record)
           (:expanded-forms reference-macro-artifact))
        syntax-count-parity?
        (= (count (:expanded-syntax-object-stream front-end-record))
           (count (:expanded-syntax-object-stream
                   reference-macro-artifact)))
        expected-stdout (get-in executor
                                [:accepted-scope :expected-stdout])
        output-equivalent? (= expected-stdout (:stdout runtime-record))
        complete?
        (and read-form-parity?
             macro-expanded-form-parity?
             syntax-count-parity?
             output-equivalent?)]
    {:artifact :gravity/p15-s23-stage2-front-end-executor-accepted-record
     :fixture source-path
     :executor-engine (:engine executor)
     :front-end-engine (:engine front-end)
     :source-id (:source-id front-end-record)
     :form-count (count (:forms front-end-record))
     :expanded-form-count (count (:expanded-forms front-end-record))
     :macro-expansion-count (count (:macro-expansion-trace
                                    front-end-record))
     :stage2-plan-id (:plan-id stage2-plan)
     :stage2-runtime-execution-record runtime-record
     :stage2-front-end-executor-output (:stdout runtime-record)
     :expected-stdout expected-stdout
     :read-form-parity? read-form-parity?
     :macro-expanded-form-parity? macro-expanded-form-parity?
     :syntax-count-parity? syntax-count-parity?
     :accepted-output-equivalent? output-equivalent?
     :stage2-front-end-record
     (select-keys front-end-record
                  [:artifact :source-path :source-id :status
                   :source-unit-record :token-stream :form-tree
                   :top-level-form-ids :syntax-seed-stream
                   :reader-source-map :literal-decoding-records
                   :semantic-error-deferment-record :reader-diagnostics
                   :incremental-reader-hashes :reader-product-integrity
                   :c3-artifact-id :c3-capability-proof
                   :c3-syntax-object-stream])
     :stage2-reader-products
     (select-keys front-end-record
                  [:reader-products :source-unit-record :token-stream
                   :form-tree :top-level-form-ids :syntax-seed-stream
                   :reader-source-map :literal-decoding-records
                   :semantic-error-deferment-record
                   :reader-extension-invocation-records
                   :reader-diagnostics :incremental-reader-hashes
                   :reader-product-integrity :c3-artifact-id])
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-front-end-executor-rejected-records
  [executor front-end emitter runtime]
  (mapv
   (fn [{:keys [fixture expected-diagnostic rejected-design]}]
     (let [diagnostic
           (try
             (let [source-text (slurp fixture)
                   front-end-record
                   (p15-s23-stage2-front-end-source-module-record
                    front-end fixture source-text)
                   module (:module front-end-record)
                   stage2-plan
                   (p15-s23-stage2-emitted-core-plan
                    emitter fixture source-text module)]
               (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
               nil)
             (catch clojure.lang.ExceptionInfo ex
               (:id (ex-data ex))))]
       {:fixture fixture
        :rejected-design rejected-design
        :expected-diagnostic expected-diagnostic
        :status :rejected
        :diagnostic diagnostic
        :matches-expected? (= expected-diagnostic diagnostic)}))
   (:rejected-diagnostic-contract executor)))

(defn p15-s23-stage2-front-end-executor-rejected-record
  [records]
  (let [expected (set (map :expected-diagnostic records))
        observed (set (map :diagnostic records))
        mismatches (remove :matches-expected? records)
        matches? (and (= expected observed) (empty? mismatches))]
    {:artifact
     :gravity/p15-s23-stage2-front-end-executor-rejected-record
     :expected-diagnostics (p15-s23-stage2-sort-values expected)
     :observed-diagnostics (p15-s23-stage2-sort-values observed)
     :fixture-count (count records)
     :mismatch-count (count mismatches)
     :records records
     :status (if matches? :complete :failed)}))

(defn p15-s23-stage2-front-end-executor-evidence-link-record
  [runtime-kernel-artifact nucleus-artifact plan-emitter-artifact
   runtime-artifact pipeline-artifact]
  (let [links {:stage2-runtime-kernel
               {:artifact (:kind runtime-kernel-artifact)
                :artifact-id (:artifact-id runtime-kernel-artifact)
                :present?
                (= :gravity/p15-s23-stage2-runtime-kernel-artifact
                   (:kind runtime-kernel-artifact))}
               :stage2-compiler-nucleus
               {:artifact (:kind nucleus-artifact)
                :artifact-id (:artifact-id nucleus-artifact)
                :present?
                (= :gravity/p15-s23-stage2-compiler-nucleus-artifact
                   (:kind nucleus-artifact))}
               :stage2-plan-emitter
               {:artifact (:kind plan-emitter-artifact)
                :artifact-id (:artifact-id plan-emitter-artifact)
                :present?
                (= :gravity/p15-s23-stage2-plan-emitter-artifact
                   (:kind plan-emitter-artifact))}
               :stage2-runtime-executor
               {:artifact (:kind runtime-artifact)
                :artifact-id (:artifact-id runtime-artifact)
                :present?
                (= :gravity/p15-s23-stage2-runtime-executor-artifact
                   (:kind runtime-artifact))}
               :compiler-pipeline-manifest
               {:artifact (:kind pipeline-artifact)
                :artifact-id (:artifact-id pipeline-artifact)
                :present?
                (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                   (:kind pipeline-artifact))}}
        missing (set (for [[k v] links :when (not (:present? v))] k))]
    {:artifact
     :gravity/p15-s23-stage2-front-end-executor-evidence-link-record
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing)
     :all-required-links-present? (empty? missing)
     :status (if (empty? missing) :complete :failed)}))

(defn p15-s23-stage2-front-end-executor-boundary-record
  [executor]
  (let [claims (:self-hosting-claims executor)
        seed-boundary (:seed-boundary executor)]
    {:artifact
     :gravity/p15-s23-stage2-front-end-executor-boundary-record
     :implemented-by (:implemented-by executor)
     :executed-by (:executed-by executor)
     :compiled-by (get-in executor [:lineage :compiled-by])
     :stage2-front-end-executor-used? true
     :stage2-front-end-host-replaced? true
     :stage2-runtime-kernel-used?
     (= :gravity-stage2-runtime-kernel (:executed-by executor))
     :stage2-runtime-host-replaced?
     (= :replaced-by-stage2-runtime-kernel
        (:stage0-runtime-host-boundary seed-boundary))
     :stage2-runtime-primitives-replaced?
     (= :gravity-runtime-primitives
        (:host-primitive-boundary seed-boundary))
     :clojure-stage2-front-end-host? false
     :clojure-stage0-runtime-host?
     (= :clojure-stage0-runtime-host
        (:stage0-runtime-host-boundary seed-boundary))
     :clojure-host-primitive-boundary?
     (= :clojure-runtime-primitives
        (:host-primitive-boundary seed-boundary))
     :gravity-runtime-primitives?
     (= :gravity-runtime-primitives
        (:host-primitive-boundary seed-boundary))
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? claims)
     :clojure-seed-retired? (:clojure-seed-retired? claims)
     :seed-boundary seed-boundary
     :next-required-capability (:next-required-capability executor)
     :status :complete}))