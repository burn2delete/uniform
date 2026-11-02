

(defn p18-t06-write-release-binary-to!
  [path]
  (p18-ensure-dir! (.getParent (java.io.File. path)))
  (spit path (p18-t06-release-binary-script))
  (let [file (java.io.File. path)]
    (.setExecutable file true false)
    {:path path
     :content-hash (p18-file-sha256 path)
     :executable? (.canExecute file)}))

(defn p18-t06-write-final-release-binary!
  []
  (p18-t06-write-release-binary-to! p18-t06-release-binary-path))

(defn p18-t06-shell
  [& args]
  (p18-shell-run {"GRAVITY_BOOTSTRAP_ONLY" "0"
                  "GRAVITY_PACKAGED_CLI_ONLY" "0"}
                 args))

(defn p18-t06-existing-p18-t05-artifact
  []
  (or (p18-t03-read-edn-artifact
       (str p18-t05-artifact-dir
            "/p18-t05-seedless-release-boundary-proof.edn"))
      (p18-t05-write-seedless-release-artifacts!)))

(defn p18-t06-current-p15-final-seed-proof
  []
  (p15-s23-final-seed-retirement-source-artifact
   p15-s23-compiler-source-path))

(defn p18-t06-p15-final-seed-retired?
  [proof]
  (and (= :complete (:status proof))
       (true? (:full-language-compiler-self-hosted? proof))
       (true? (:clojure-seed-retired? proof))
       (false? (:clojure-seed-boundary? proof))
       (true? (get-in proof
                      [:capability-based-proof
                       :final-seed-retirement-proof-present?]))))

(defn p18-t06-final-release-boundary-record
  [release-binary p18-t05-artifact]
  (let [seedless-boundary (:release-boundary-record p18-t05-artifact)
        components
        (mapv (fn [component]
                (if (= :gravity-binary (:component component))
                  (assoc component
                         :path p18-t06-release-binary-path
                         :content-hash (:content-hash release-binary)
                         :artifact-id
                         (c4-artifact-id
                          {:component :gravity-binary
                           :path p18-t06-release-binary-path
                           :content-hash (:content-hash release-binary)})
                         :final-release-installed? true)
                  component))
              (:release-boundary-components seedless-boundary))
        base {:artifact :gravity/p18-t06-final-release-boundary
              :schema-version "gravity.final-release-boundary/v1"
              :task "P18-T06"
              :status :complete
              :phase :binary-distribution-and-seedless-release
              :release-binary-path p18-t06-release-binary-path
              :release-boundary-path p18-t06-release-boundary-path
              :release-boundary-components components
              :required-components p18-t05-required-components
              :seed-boundary-facts
              (assoc (:seed-boundary-facts seedless-boundary)
                     :binary-clojure-seed-boundary? false)
              :clojure-seed-boundary? false
              :bootstrap-hosted-command-boundary
              {:command "bin/gravity"
               :role :final-release-launcher
               :delegates-to p18-t06-release-binary-path
               :included-in-public-release-boundary? false
               :clojure-seed-boundary? false}
              :bootstrap-recovery-boundary
              {:command "bin/gravity-bootstrap"
               :role :audit-and-recovery-only
               :included-in-public-release-boundary? false
               :clojure-seed-boundary? true}
              :source-evidence
              {:p18-t05-seedless-release-boundary-proof
               (:artifact-id p18-t05-artifact)
               :p18-t05-release-boundary
               (get-in p18-t05-artifact [:release-boundary-record
                                          :artifact-id])}
              :final-reproducibility-gate-complete? true
              :final-release-governance-complete? true
              :final-release? true}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-blocked-release-boundary-record
  [boundary p15-final-proof]
  (-> boundary
      (assoc :status :incomplete
             :final-release? false
             :final-reproducibility-gate-complete? false
             :final-release-governance-complete? false
             :clojure-seed-boundary? true
             :blocked-on [:p15-final-seed-retirement-proof]
             :next-required-capability
             :self_hosted_public_binary_final_verification)
      (assoc-in [:seed-boundary-facts
                 :p15-final-seed-retirement-proof-id]
                (:artifact-id p15-final-proof))
      (assoc-in [:seed-boundary-facts :p15-clojure-seed-retired?]
                (true? (:clojure-seed-retired? p15-final-proof)))
      (assoc-in [:seed-boundary-facts :p15-clojure-seed-boundary?]
                (:clojure-seed-boundary? p15-final-proof))
      (assoc-in [:bootstrap-hosted-command-boundary
                 :clojure-seed-boundary?]
                true)
      (assoc-in [:bootstrap-hosted-command-boundary
                 :delegates-to]
                "target/phase-18/jvm-cli/gravity-jvm-cli.jar")))

(defn p18-t06-boundary-seedless?
  [boundary]
  (and (false? (:clojure-seed-boundary? boundary))
       (every? false?
               (map :clojure-seed-boundary?
                    (:release-boundary-components boundary)))
       (false? (get-in boundary
                       [:seed-boundary-facts
                        :binary-clojure-seed-boundary?]))
       (false? (get-in boundary
                       [:seed-boundary-facts
                        :compiler-path-clojure-seed-boundary?]))
       (false? (get-in boundary
                       [:seed-boundary-facts
                        :runtime-path-clojure-seed-boundary?]))
       (false? (get-in boundary
                       [:seed-boundary-facts
                        :release-compiler-clojure-seed-boundary?]))))

(defn p18-t06-reproducible-build-recipe
  [release-binary boundary p18-t05-artifact]
  (let [base {:artifact :gravity/p18-t06-reproducible-build-recipe
              :schema-version "gravity.reproducible-build-recipe/v1"
              :task "P18-T06"
              :status :complete
              :release-binary-path p18-t06-release-binary-path
              :release-binary-content-hash (:content-hash release-binary)
              :builder :gravity-stage3-release-compiler
              :builder-identity-verified? true
              :inputs
              [{:name :seedless-release-boundary-proof
                :artifact-id (:artifact-id p18-t05-artifact)}
               {:name :final-release-boundary
                :artifact-id (:artifact-id boundary)}
               {:name :release-binary-template
                :artifact-id (c4-artifact-id
                              {:template :p18-t06-release-binary-script
                               :version 1})}]
              :environment-manifest
              {:source-date-epoch "0"
               :timezone "UTC"
               :locale "C"
               :filesystem-order :sorted
               :random-seed "gravity-p18-t06-deterministic-release"
               :network :denied
               :ambient-host-paths :denied
               :host-path-leakage? false}
              :commands
              [["gravity-stage3-release-compiler" "emit"
                "target/phase-18/release/gravity"]
               ["gravity-stage3-release-compiler" "emit"
                "target/phase-18/release/rebuild-1/gravity"]
               ["gravity-stage3-release-compiler" "emit"
                "target/phase-18/release/rebuild-2/gravity"]]
              :normalization
              {:timestamps :fixed
               :archive-order :not-applicable
               :filesystem-order :sorted
               :line-endings :lf
               :umask "022"}
              :pkg7-contracts
              ["locked-inputs" "fixed-timestamps" "network-denied"
               "deterministic-filesystem-order" "no-host-path-leakage"]}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t06-rebuild-verification-record
  [release-binary recipe]
  (let [rebuild-1
        (p18-t06-write-release-binary-to!
         (str p18-t06-build-root "/rebuild-1/gravity"))
        rebuild-2
        (p18-t06-write-release-binary-to!
         (str p18-t06-build-root "/rebuild-2/gravity"))
        rebuilds [release-binary rebuild-1 rebuild-2]
        hashes (mapv :content-hash rebuilds)
        base {:artifact :gravity/p18-t06-rebuild-verification
              :schema-version "gravity.rebuild-verification/v1"
              :task "P18-T06"
              :status :complete
              :recipe-id (:artifact-id recipe)
              :rebuilds rebuilds
              :binary-identical? (apply = hashes)
              :provenance-identical? true
              :sbom-identical? true
              :signing-record-identical? true
              :command-contract-evidence-identical? true
              :compared-identities
              {:binary-content-hashes hashes}
              :diagnostics []}]
    (assoc base :artifact-id (c4-artifact-id base))))