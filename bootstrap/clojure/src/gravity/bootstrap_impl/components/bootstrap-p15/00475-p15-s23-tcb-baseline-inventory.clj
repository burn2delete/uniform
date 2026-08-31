

(defn p15-s23-tcb-baseline-inventory
  [source-path provenance-artifact]
  (let [components
        [{:component :clojure-stage0-bootstrap
          :kind :compiler-seed
          :trusted? true
          :artifact-id :clojure-stage0-bootstrap
          :hash (get-in provenance-artifact
                        [:build-input-record :compiler-hash])
          :reason :compiles-and-verifies-current-candidate}
         {:component :clojure-stage0-verifier
          :kind :verifier-implementation
          :trusted? true
          :artifact-id
          "bootstrap/clojure/src/gravity/bootstrap.clj"
          :hash (p15-s23-provenance-file-hash
                 "bootstrap/clojure/src/gravity/bootstrap.clj")
          :reason :validates-p15-s23-evidence}
         {:component :jvm-runtime
          :kind :host-runtime
          :trusted? true
          :artifact-id :host-jvm-runtime
          :reason :executes-clojure-seed-and-verifier}
         {:component :host-filesystem-source-loading
          :kind :host-io-boundary
          :trusted? true
          :artifact-id source-path
          :reason :loads-gravity-source-and-fixtures-for-stage0}
         {:component :deps-lockfile
          :kind :dependency-lock
          :trusted? true
          :artifact-id "deps.edn"
          :hash (p15-s23-provenance-file-hash "deps.edn")
          :reason :locks-stage0-bootstrap-dependencies}]]
    {:artifact :gravity/p15-s23-baseline-tcb-inventory
     :source-path source-path
     :inventory-id (c4-artifact-id components)
     :trusted-components components
     :trusted-component-count (count components)
     :scope :current-clojure-seed-candidate
     :status :complete}))

(defn p15-s23-tcb-current-inventory
  [source-path source-data inventory-artifact pipeline-artifact
   runtime-artifact stage-artifact conformance-artifact provenance-artifact]
  (let [trusted
        [{:component :clojure-stage0-bootstrap
          :kind :compiler-seed
          :trusted? true
          :artifact-id :clojure-stage0-bootstrap
          :hash (get-in provenance-artifact
                        [:build-input-record :compiler-hash])
          :reason :still-compiles-and-verifies-current-candidate}
         {:component :clojure-stage0-verifier
          :kind :verifier-implementation
          :trusted? true
          :artifact-id
          "bootstrap/clojure/src/gravity/bootstrap.clj"
          :hash (p15-s23-provenance-file-hash
                 "bootstrap/clojure/src/gravity/bootstrap.clj")
          :reason :still-validates-p15-s23-evidence}
         {:component :jvm-runtime
          :kind :host-runtime
          :trusted? true
          :artifact-id :host-jvm-runtime
          :reason :still-executes-clojure-seed-and-verifier}
         {:component :host-filesystem-source-loading
          :kind :host-io-boundary
          :trusted? true
          :artifact-id source-path
          :reason :still-loads-gravity-source-and-fixtures}
         {:component :deps-lockfile
          :kind :dependency-lock
          :trusted? true
          :artifact-id "deps.edn"
          :hash (p15-s23-provenance-file-hash "deps.edn")
          :reason :still-locks-stage0-bootstrap-dependencies}]
        evidence-controls
        [{:component :gravity-p15-s23-compiler-source
          :kind :gravity-source
          :artifact-id (str "sha256:"
                            (sha256-hex (:source-text source-data)))
          :control :source-inventory}
         {:component :compiler-source-inventory
          :kind (:kind inventory-artifact)
          :artifact-id (:artifact-id inventory-artifact)
          :control :verified-evidence}
         {:component :compiler-pipeline-manifest
          :kind (:kind pipeline-artifact)
          :artifact-id (:artifact-id pipeline-artifact)
          :control :verified-evidence}
         {:component :runtime-manifest-and-capability-enforcement-report
          :kind (:kind runtime-artifact)
          :artifact-id (:artifact-id runtime-artifact)
          :control :verified-evidence}
         {:component :stage-comparison-report
          :kind (:kind stage-artifact)
          :artifact-id (:artifact-id stage-artifact)
          :control :verified-evidence}
         {:component :self-hosting-conformance-report
          :kind (:kind conformance-artifact)
          :artifact-id (:artifact-id conformance-artifact)
          :control :verified-evidence}
         {:component :bootstrap-provenance-attestation
          :kind (:kind provenance-artifact)
          :artifact-id (:artifact-id provenance-artifact)
          :control :verified-evidence}]]
    {:artifact :gravity/p15-s23-current-tcb-inventory
     :source-path source-path
     :inventory-id (c4-artifact-id
                    {:trusted trusted
                     :evidence-controls evidence-controls})
     :trusted-components trusted
     :trusted-component-count (count trusted)
     :evidence-controlled-components evidence-controls
     :evidence-control-count (count evidence-controls)
     :current-candidate-is-clojure-seed? true
     :full-language-compiler-self-hosted? false
     :clojure-seed-retired? false
     :status :complete}))

(defn p15-s23-tcb-delta-classification
  [baseline current]
  (let [baseline-components (set (map :component
                                      (:trusted-components baseline)))
        residual-components (set (map :component
                                      (:trusted-components current)))
        evidence-components (set (map :component
                                      (:evidence-controlled-components
                                       current)))
        unclassified (set/difference baseline-components
                                     residual-components)
        rows
        (vec
         (concat
          (map (fn [component]
                 {:component component
                  :before :trusted
                  :after :trusted
                  :classification :residual-trusted
                  :whole-language-retired? false})
               (sort baseline-components))
          (map (fn [component]
                 {:component component
                  :before :not-authoritative
                  :after :evidence-control
                  :classification :evidence-controlled
                  :whole-language-retired? false})
               (sort evidence-components))))]
    {:artifact :gravity/p15-s23-tcb-delta-classification
     :rows rows
     :classified-baseline-components (vec (sort baseline-components))
     :evidence-controlled-components (vec (sort evidence-components))
     :unclassified-baseline-components (vec (sort unclassified))
     :retired-components []
     :reduced-components []
     :residual-trusted-components (vec (sort residual-components))
     :classification-complete? (empty? unclassified)
     :whole-language-tcb-reduced? false
     :reason :current-candidate-still-compiled-and-verified-by-clojure-stage0
     :status (if (empty? unclassified) :complete :failed)}))

(defn p15-s23-tcb-residual-trust-boundary-record
  [current]
  (let [residual-boundaries (set (map :component
                                      (:trusted-components current)))
        missing (set/difference p15-s23-tcb-required-residual-boundaries
                                residual-boundaries)]
    {:artifact :gravity/p15-s23-residual-trust-boundary-record
     :residual-boundaries (vec (sort residual-boundaries))
     :required-residual-boundaries
     (vec (sort p15-s23-tcb-required-residual-boundaries))
     :missing-required-residual-boundaries (vec (sort missing))
     :clojure-seed-still-trusted?
     (contains? residual-boundaries :clojure-stage0-bootstrap)
     :clojure-verifier-still-trusted?
     (contains? residual-boundaries :clojure-stage0-verifier)
     :host-runtime-still-trusted?
     (contains? residual-boundaries :jvm-runtime)
     :host-source-loading-still-trusted?
     (contains? residual-boundaries :host-filesystem-source-loading)
     :deps-lockfile-still-trusted?
     (contains? residual-boundaries :deps-lockfile)
     :status (if (empty? missing) :complete :failed)}))

(defn p15-s23-tcb-evidence-link-table
  [inventory-artifact runtime-artifact stage-artifact
   conformance-artifact provenance-artifact]
  (let [links
        [{:link :compiler-source-inventory
          :artifact-id (:artifact-id inventory-artifact)
          :status :verified}
         {:link :runtime-manifest-and-capability-enforcement-report
          :artifact-id (:artifact-id runtime-artifact)
          :proof-id (:proof-id runtime-artifact)
          :status :verified}
         {:link :stage-comparison-report
          :artifact-id (:artifact-id stage-artifact)
          :proof-id (:proof-id stage-artifact)
          :status :verified}
         {:link :self-hosting-conformance-report
          :artifact-id (:artifact-id conformance-artifact)
          :proof-id (:proof-id conformance-artifact)
          :status :verified}
         {:link :bootstrap-provenance-attestation
          :artifact-id (:artifact-id provenance-artifact)
          :proof-id (:proof-id provenance-artifact)
          :provenance-record-id
          (get-in provenance-artifact
                  [:bootstrap-provenance-record
                   :provenance-record-id])
          :status :verified}]
        covered (set (map :link links))]
    {:artifact :gravity/p15-s23-tcb-evidence-link-table
     :links links
     :required-links (vec (sort p15-s23-tcb-required-evidence-links))
     :required-links-covered?
     (= p15-s23-tcb-required-evidence-links covered)
     :status (if (= p15-s23-tcb-required-evidence-links covered)
               :complete
               :failed)}))