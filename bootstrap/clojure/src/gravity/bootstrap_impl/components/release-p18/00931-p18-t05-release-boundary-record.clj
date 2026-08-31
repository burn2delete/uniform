

(defn p18-t05-release-boundary-record
  [release-binary p18-t03-artifact p18-t04-artifact]
  (let [seed-boundary (:seed-boundary-record p18-t03-artifact)
        compiler-path (:compiler-path-record p18-t03-artifact)
        runtime-boundary (:runtime-boundary-record p18-t03-artifact)
        final-proof (get-in compiler-path [:final-seed-retirement-proof])
        components
        [(p18-t05-boundary-component
          :gravity-binary
          {:path p18-t05-release-binary-path
           :binary-name "gravity"
           :content-hash (:content-hash release-binary)
           :executable? (:executable? release-binary)
           :artifact-id
           (c4-artifact-id {:component :gravity-binary
                            :path p18-t05-release-binary-path
                            :content-hash (:content-hash release-binary)})
           :emitted-by :gravity-stage3-release-compiler
           :release-artifact-id (:release-artifact-id p18-t03-artifact)
           :command-contract-proof-id (:artifact-id p18-t04-artifact)})
         (p18-t05-boundary-component
         :compiler-path
          {:compiler-path-id (:compiler-path-id compiler-path)
           :artifact-id (:artifact-id compiler-path)
           :stage :stage3-self-hosted
           :clojure-seed-boundary?
           (:compiler-path-clojure-seed-boundary? seed-boundary)
           :seed-fact-source :p18-t03-seed-boundary-record})
         (p18-t05-boundary-component
          :runtime-path
          {:runtime-path-id (:runtime-path-id runtime-boundary)
           :artifact-id (:artifact-id runtime-boundary)
           :runtime-family (:runtime-family runtime-boundary)
           :clojure-seed-boundary?
           (:runtime-path-clojure-seed-boundary? seed-boundary)
           :seed-fact-source :p18-t03-seed-boundary-record})
         (p18-t05-boundary-component
          :release-compiler-path
          {:release-compiler-id (:release-compiler-id compiler-path)
           :artifact-id (:release-compiler-id compiler-path)
           :stage :stage3-release-compiler
           :clojure-seed-boundary?
           (:release-compiler-clojure-seed-boundary? seed-boundary)
           :seed-fact-source :p18-t03-seed-boundary-record})]
        seed-facts
        {:binary-clojure-seed-boundary? false
         :compiler-path-clojure-seed-boundary?
         (:compiler-path-clojure-seed-boundary? seed-boundary)
         :runtime-path-clojure-seed-boundary?
         (:runtime-path-clojure-seed-boundary? seed-boundary)
         :release-compiler-clojure-seed-boundary?
         (:release-compiler-clojure-seed-boundary? seed-boundary)
         :p15-final-seed-retirement-proof-id (:artifact-id final-proof)
         :p15-clojure-seed-retired? (:clojure-seed-retired? final-proof)
         :p15-clojure-seed-boundary? (:clojure-seed-boundary? final-proof)}
        base0
        {:artifact :gravity/p18-t05-seedless-release-boundary
         :schema-version "gravity.seedless-release-boundary/v1"
         :task "P18-T05"
         :phase :binary-distribution-and-seedless-release
         :release-binary-path p18-t05-release-binary-path
         :release-boundary-path p18-t05-release-boundary-path
         :release-boundary-components components
         :required-components p18-t05-required-components
         :seed-boundary-facts seed-facts
         :bootstrap-hosted-command-boundary
         {:command "bin/gravity"
          :role :phase-18-proof-runner-before-final-installation
          :included-in-public-release-boundary? false
          :clojure-seed-boundary? true
          :replacement-candidate p18-t05-release-binary-path}
         :bootstrap-recovery-boundary
         {:command "bin/gravity-bootstrap"
          :role :audit-and-recovery-only
          :included-in-public-release-boundary? false
          :clojure-seed-boundary? true}
         :source-evidence
         {:p18-t03-self-hosted-release-artifact-proof
          (:artifact-id p18-t03-artifact)
          :p18-t04-executable-command-contract-proof
          (:artifact-id p18-t04-artifact)
          :p15-final-seed-retirement-proof-id (:artifact-id final-proof)}
         :final-reproducibility-gate-complete? false
         :final-release-governance-complete? false
         :next-required-capability
         :p18-t06-reproducibility-provenance-sbom-signing-governance}
        seedless? (p18-t05-seed-boundary-retired? base0)
        base (assoc base0
                    :status (if seedless? :complete :incomplete)
                    :clojure-seed-boundary? (not seedless?)
                    :seedless-release-boundary? seedless?
                    :next-required-capability
                    (if seedless?
                      :p18-t06-reproducibility-provenance-sbom-signing-governance
                      :p15-s23-final-seed-retirement))]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t05-components-by-key
  [boundary]
  (into {} (map (juxt :component identity)
                (:release-boundary-components boundary))))

(defn p18-t05-update-component
  [boundary component f]
  (update boundary :release-boundary-components
          (fn [components]
            (mapv (fn [record]
                    (if (= component (:component record))
                      (f record)
                      record))
                  components))))

(defn p18-t05-required-seedless?
  [boundary]
  (let [components (p18-t05-components-by-key boundary)]
    (every? (fn [component]
              (false? (:clojure-seed-boundary? (get components component))))
            p18-t05-required-components)))

(defn p18-t05-boundary-diagnostics
  [boundary]
  (let [components (:release-boundary-components boundary)
        by-key (p18-t05-components-by-key boundary)
        public-components components
        seed-facts (:seed-boundary-facts boundary)
        public-paths (set (keep :path public-components))
        release-compiler-components
        (filter #(= :release-compiler-path (:component %)) public-components)
        release-compiler-ids (distinct (keep :release-compiler-id
                                             release-compiler-components))]
    (vec
     (concat
      (when (some #(true? (:clojure-seed-boundary? %)) public-components)
        [(p18-t05-diagnostic-record
          "P18T05001" boundary
          {:components-with-clojure-seed
           (mapv :component
                 (filter #(true? (:clojure-seed-boundary? %))
                         public-components))})])
      (let [missing-components
            (vec (remove #(contains? by-key %) p18-t05-required-components))
            missing-seed-fields
            (mapv :component
                  (remove #(contains? % :clojure-seed-boundary?)
                          public-components))]
        (when (or (seq missing-components) (seq missing-seed-fields))
          [(p18-t05-diagnostic-record
            "P18T05002" boundary
            {:missing-components missing-components
             :missing-seed-fields missing-seed-fields})]))
      (when-not
          (and (false? (:binary-clojure-seed-boundary? seed-facts))
               (false? (:compiler-path-clojure-seed-boundary? seed-facts))
               (false? (:runtime-path-clojure-seed-boundary? seed-facts))
               (false? (:release-compiler-clojure-seed-boundary? seed-facts))
               (true? (:p15-clojure-seed-retired? seed-facts))
               (false? (:p15-clojure-seed-boundary? seed-facts)))
        [(p18-t05-diagnostic-record
          "P18T05003" boundary
          {:seed-boundary-facts seed-facts})])
      (when (or (contains? public-paths "bin/gravity")
                (contains? public-paths "bin/gravity-bootstrap")
                (true? (get-in boundary
                               [:bootstrap-recovery-boundary
                                :included-in-public-release-boundary?])))
        [(p18-t05-diagnostic-record
          "P18T05004" boundary
          {:public-paths (vec public-paths)
           :bootstrap-recovery-boundary
           (:bootstrap-recovery-boundary boundary)})])
      (when-not (and (= 1 (count release-compiler-components))
                     (= 1 (count release-compiler-ids))
                     (re-find #"^sha256:" (str (first release-compiler-ids))))
        [(p18-t05-diagnostic-record
          "P18T05005" boundary
          {:release-compiler-components
           (mapv #(select-keys % [:component :release-compiler-id
                                  :artifact-id])
                 release-compiler-components)})])))))

(defn p18-t05-accepted-boundary-record
  [{:keys [fixture output-path expected-stdout]}]
  (let [binary p18-t05-release-binary-path
        check-result (p18-t04-shell binary "check" fixture)
        run-result (p18-t04-shell binary "run" fixture)
        compile-result (p18-t04-shell binary "compile" fixture "-o"
                                      output-path)
        compile-artifact (p18-t04-read-edn-stdout compile-result)
        executable-result (p18-t04-shell output-path)
        inspect-result (p18-t04-shell binary "inspect"
                                      (str output-path ".gravity-artifact.edn"))
        inspected-artifact (p18-t04-read-edn-stdout inspect-result)
        release-proof-result
        (p18-t04-shell binary "p18-t05-seedless-release-boundary")
        release-boundary (p18-t04-read-edn-stdout release-proof-result)
        stdout-matches? (= expected-stdout (:out executable-result))
        command-survived?
        (and (zero? (:exit check-result))
             (zero? (:exit run-result))
             (zero? (:exit compile-result))
             (zero? (:exit executable-result))
             (zero? (:exit inspect-result))
             (zero? (:exit release-proof-result))
             stdout-matches?)
        seedless-artifact?
        (and (false? (:clojure-seed-boundary? inspected-artifact))
             (false? (:compiler-path-clojure-seed-boundary?
                      inspected-artifact))
             (false? (:runtime-path-clojure-seed-boundary?
                      inspected-artifact))
             (false? (:release-compiler-clojure-seed-boundary?
                      inspected-artifact)))
        seedless-release-proof?
        (and (false? (:clojure-seed-boundary? release-boundary))
             (p18-t05-required-seedless? release-boundary))]
    {:fixture fixture
     :output-path output-path
     :status (if command-survived?
               :accepted
               :failed)
     :expected-stdout expected-stdout
     :check-result (select-keys check-result [:exit :out :err])
     :run-result (select-keys run-result [:exit :out :err])
     :compile-result (select-keys compile-result [:exit :out :err])
     :compile-artifact
     (select-keys compile-artifact
                  [:kind :task :status :executable-path
                   :clojure-seed-boundary?
                   :compiler-path-clojure-seed-boundary?
                   :runtime-path-clojure-seed-boundary?
                   :release-compiler-clojure-seed-boundary?])
     :executable-result (select-keys executable-result [:exit :out :err])
     :inspect-result (select-keys inspect-result [:exit :out :err])
     :release-proof-result
     (select-keys release-proof-result [:exit :out :err])
     :command-survived? command-survived?
     :artifact-inspection-seedless? seedless-artifact?
     :release-proof-command-seedless? seedless-release-proof?
     :stdout-matches? stdout-matches?
     :matches-expected? command-survived?}))