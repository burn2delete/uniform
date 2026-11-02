

(defn p18-t02-run-bin-gravity
  [& args]
  (p18-shell-run {"GRAVITY_PACKAGED_CLI_ONLY" "1"}
                 (concat ["bin/gravity"] args)))

(defn p18-t02-command-proof
  [command path]
  (let [args (cond-> [command] path (conj path))
        packaged (apply p18-t02-run-packaged args)
        clojure-result (apply p18-t02-run-clojure args)]
    {:command (vec (concat ["bin/gravity"] args))
     :packaged-command (vec (concat ["java" "-cp"
                                     "target/phase-18/jvm-cli/gravity-jvm-cli.jar:<runtime-classpath>"
                                     "gravity.cli.Main"]
                                    args))
     :clojure-command (vec (concat ["clojure" "-M:gravity"] args))
     :exit-match? (= (:exit packaged) (:exit clojure-result))
     :stdout-match? (= (:out packaged) (:out clojure-result))
     :stderr-match? (= (:err packaged) (:err clojure-result))
     :exit (:exit packaged)
     :stdout (:out packaged)
     :stderr (:err packaged)}))

(def p18-t02-diagnostic-messages
  {"P18T02001" "packaged JVM CLI is not the seedless release artifact"
   "P18T02002" "packaged JVM CLI manifest is missing package metadata"
   "P18T02003" "packaged JVM CLI command parity proof failed"
   "P18T02004" "packaged JVM CLI target claim is unsupported"
   "P18T02005" "packaged JVM CLI provenance is missing"
   "P18T02006" "packaged JVM CLI build failed"})

(defn p18-t02-diagnostic-record
  [id fixture value data]
  {:artifact :gravity/diagnostic
   :diagnostic id
   :severity :error
   :stage :p18-t02-packaged-jvm-cli
   :fixture fixture
   :message (get p18-t02-diagnostic-messages id)
   :observed value
   :facts data})

(defn p18-t02-candidate-diagnostics
  [fixture candidate]
  (let [manifest (:package-manifest candidate)
        missing-fields
        (vec (remove #(contains? manifest %)
                     [:package-id :package-version :entrypoint :target
                      :artifact-path :content-hash]))
        parity (:command-parity candidate)]
    (vec
     (concat
      (when (seq missing-fields)
        [(p18-t02-diagnostic-record
          "P18T02002" fixture manifest {:missing-fields missing-fields})])
      (when-not (every? true?
                        (mapcat (fn [proof]
                                  [(:exit-match? proof)
                                   (:stdout-match? proof)
                                   (:stderr-match? proof)])
                                parity))
        [(p18-t02-diagnostic-record
          "P18T02003" fixture parity {:required [:exit :stdout :stderr]})])
      (when-not (= :jvm-hosted (:target manifest))
        [(p18-t02-diagnostic-record
          "P18T02004" fixture (:target manifest)
          {:supported-target :jvm-hosted})])
      (when-not (:provenance-record candidate)
        [(p18-t02-diagnostic-record
          "P18T02005" fixture nil
          {:required-record :provenance-record})])))))

(defn p18-t02-rejected-fixture-records
  [candidate]
  (let [fixtures
        [{:fixture :p18-t02-missing-package-metadata
          :expected-diagnostic "P18T02002"
          :candidate (update candidate :package-manifest dissoc
                             :package-id :package-version)}
         {:fixture :p18-t02-command-parity-mismatch
          :expected-diagnostic "P18T02003"
          :candidate (assoc-in candidate
                               [:command-parity 0 :stdout-match?]
                               false)}
         {:fixture :p18-t02-invalid-target-claim
          :expected-diagnostic "P18T02004"
          :candidate (assoc-in candidate
                               [:package-manifest :target]
                               :native-seedless)}
         {:fixture :p18-t02-missing-package-provenance
          :expected-diagnostic "P18T02005"
          :candidate (assoc candidate :provenance-record nil)}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic candidate]}]
       (let [diagnostics (p18-t02-candidate-diagnostics fixture candidate)
             observed (set (map :diagnostic diagnostics))]
         {:fixture fixture
          :status :rejected
          :expected-diagnostic expected-diagnostic
          :diagnostics diagnostics
          :matches-expected? (contains? observed expected-diagnostic)}))
     fixtures)))

(defn p18-t02-packaged-jvm-cli-artifact!
  []
  (p18-t02-build-packaged-jvm-cli!)
  (let [jar-entries (p18-t02-jar-entries)
        jar-file-entries (p18-t02-jar-file-entries)
        jar-source-report
        (p18-t02-jar-source-inventory-report jar-entries)
        jar-inventory-report
        (p18-t02-jar-inventory-report jar-file-entries)
        jar-entry-frequencies (:entry-frequencies jar-source-report)
        expected-source-entries (:expected jar-source-report)
        jar-hash (p18-file-sha256 p18-t02-jar-path)
        dependency-record (p18-t02-dependency-record)
        package-manifest (p18-t02-package-manifest jar-hash)
        artifact-manifest (p18-t02-artifact-manifest package-manifest jar-hash)
        reproducible-record
        (p18-t02-reproducible-build-record package-manifest jar-hash)
        provenance-record
        (p18-t02-provenance-record package-manifest dependency-record
                                   reproducible-record jar-hash)
        sbom-record (p18-t02-sbom-record package-manifest dependency-record
                                         provenance-record)
        signing-record (p18-t02-signing-record artifact-manifest
                                               provenance-record sbom-record)
        command-parity
        [(p18-t02-command-proof "check" "examples/hello.gravity")
         (p18-t02-command-proof "run" "examples/hello.gravity")
         (p18-t02-command-proof "compile" "examples/hello.gravity")
         (p18-t02-command-proof "check" "examples/core-app.gravity")
         (p18-t02-command-proof "run" "examples/core-app.gravity")
         (p18-t02-command-proof "compile" "examples/core-app.gravity")
         (p18-t02-command-proof "p18-t01-thin-cli-wrapper" nil)]
        rejected-command
        (p18-t02-command-proof
         "run-compiled"
         "bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity")
        bin-version (p18-t02-run-bin-gravity "--version")
        seedless-claim (p18-t02-run-bin-gravity "--assert-seedless-release")
        candidate {:package-manifest package-manifest
                   :artifact-manifest artifact-manifest
                   :dependency-record dependency-record
                   :provenance-record provenance-record
                   :sbom-record sbom-record
                   :signing-record signing-record
                   :command-parity command-parity}
        rejected-fixtures (p18-t02-rejected-fixture-records candidate)
        proof {:packaged-jar-built? (.isFile (java.io.File. p18-t02-jar-path))
               :jar-contains-launcher?
               (boolean
                (some #{"gravity/cli/Main.class"} jar-entries))
               :jar-contains-bootstrap-source?
               (= 1 (get jar-entry-frequencies "gravity/bootstrap.clj" 0))
               :jar-contains-cli-source?
               (= 1 (get jar-entry-frequencies "gravity/cli.clj" 0))
               :jar-contains-diagnostics-source?
               (= 1 (get jar-entry-frequencies "gravity/diagnostics.clj" 0))
               :jar-contains-darwin-publication-source?
               (= 1 (get jar-entry-frequencies
                         "gravity/darwin_publication.clj" 0))
               :jar-contains-packaged-sources-exactly-once?
               (:valid? jar-source-report)
               :jar-file-inventory-exact?
               (:valid? jar-inventory-report)
               :bin-gravity-launches-packaged-jar?
               (and (zero? (:exit bin-version))
                    (str/includes? (:out bin-version)
                                   ":packaged-jvm-cli? true"))
               :command-parity? (every? true?
                                        (mapcat (fn [proof]
                                                  [(:exit-match? proof)
                                                   (:stdout-match? proof)
                                                   (:stderr-match? proof)])
                                                command-parity))
               :accepted-hello-through-packaged-cli?
               (and (zero? (:exit (first command-parity)))
                    (zero? (:exit (second command-parity))))
               :accepted-core-app-through-packaged-cli?
               (and (zero? (:exit (nth command-parity 3)))
                    (zero? (:exit (nth command-parity 4))))
               :rejected-diagnostic-preserved?
               (and (= 1 (:exit rejected-command))
                    (str/includes? (:stderr rejected-command)
                                   "L2-BUILTIN-ARITY"))
               :package-records-present?
               (every? some?
                       [package-manifest dependency-record artifact-manifest
                        provenance-record sbom-record signing-record])
               :seedless-overclaim-rejected?
               (and (= 1 (:exit seedless-claim))
                    (str/includes? (:err seedless-claim) "P18T02001"))
               :bootstrap-hosted? true
               :seedless-release? false}
        artifact-base
        {:kind :gravity/p18-t02-packaged-jvm-cli-proof
         :task "P18-T02"
         :status :complete
         :phase :binary-distribution-and-seedless-release
         :jar-path p18-t02-jar-path
         :jar-content-hash jar-hash
         :jar-entries jar-entries
         :jar-file-entries jar-file-entries
         :packaged-source-inventory p18-t02-source-inventory
         :packaged-source-entries expected-source-entries
         :command-boundary
         {:public-command "bin/gravity"
          :packaged-command ["java" "-cp"
                             "target/phase-18/jvm-cli/gravity-jvm-cli.jar:<runtime-classpath>"
                             "gravity.cli.Main"]
          :bootstrap-recovery-command "bin/gravity-bootstrap"
          :bootstrap-hosted? true
          :packaged-jvm-cli? true
          :seedless-release? false
          :public-release-boundary? false
          :runtime-dependency-boundary :clojure-jvm-runtime-dependency}
         :package-manifest package-manifest
         :dependency-record dependency-record
         :artifact-manifest artifact-manifest
         :reproducible-build-record reproducible-record
         :provenance-record provenance-record
         :sbom-record sbom-record
         :signing-record signing-record
         :accepted-command-proofs command-parity
         :rejected-command-proof rejected-command
         :bin-version-output (:out bin-version)
         :unsupported-release-claim
         {:command ["bin/gravity" "--assert-seedless-release"]
          :diagnostic "P18T02001"
          :bootstrap-hosted? true
          :packaged-jvm-cli? true
          :seedless-release? false}
         :rejected-fixtures rejected-fixtures
         :capability-based-proof proof}
        artifact (assoc artifact-base :artifact-id
                        (c4-artifact-id artifact-base))]
    artifact))