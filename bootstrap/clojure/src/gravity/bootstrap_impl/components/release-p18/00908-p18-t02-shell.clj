

(defn p18-t02-shell!
  [stage args]
  (let [result (p18-shell-run args)]
    (when-not (zero? (:exit result))
      (fail! "P18T02006"
             "packaged JVM CLI build command failed"
             {:source-span {:source stage}
              :stage stage
              :command args
              :stdout (:out result)
              :stderr (:err result)
              :remediation "Install a JDK with javac and jar, then rebuild the P18-T02 package."}))
    result))

(defn p18-t02-write-manifest!
  []
  (spit p18-t02-manifest-path
        (str "Manifest-Version: 1.0\n"
             "Main-Class: gravity.cli.Main\n"
             "Implementation-Title: Gravity Packaged JVM CLI\n"
             "Implementation-Version: P18-T02\n"
             "Gravity-Bootstrap-Hosted: true\n"
             "Gravity-Seedless-Release: false\n\n")))

(defn p18-t02-source-inventory-failure!
  [report]
  (fail! "P18T02006"
         "packaged JVM CLI source inventory is invalid"
         {:source-span {:source "P18-T02 source inventory"}
          :missing-fact :complete-packaged-source-inventory
          :observed-source-count (count (:paths report))
          :observed-packaged-source-count
          (count (:packaged-source-entries report))
          :remediation
          "Restore the exact launcher and complete regular production source/data inventory before building the package."}))

(defn p18-t02-source-observation
  "Capture one validated source inventory and its derived content views."
  ([]
   (try
     (p18-t02-source-observation (p18-t02-source-inventory))
     (catch clojure.lang.ExceptionInfo error
       (if (= "packaged source inventory contains a symbolic link"
              (.getMessage error))
         (fail! "P18T02006"
                "packaged JVM CLI source inventory is invalid"
                {:source-span {:source "P18-T02 source inventory"}
                 :missing-fact :complete-packaged-source-inventory
                 :source-error (ex-data error)
                 :remediation
                 "Remove symbolic links from the production source tree before building the package."})
         (throw error)))))
  ([inventory]
   (let [report (p18-t02-source-inventory-report inventory)]
     (when-not (:valid? report)
       (p18-t02-source-inventory-failure! report))
     (let [material (:source-material report)]
       {:source-inventory inventory
        :source-inventory-report report
        :source-material material
        :source-hashes
        (into (sorted-map)
              (map (fn [{:keys [path hash]}] [path hash]))
              material)
        :packaged-source-entries (:packaged-source-entries report)
        :expected-jar-file-entries
        (p18-t02-expected-jar-file-entries inventory)
        :jar-command (p18-t02-jar-command inventory)}))))

(defn p18-t02-source-observation-input
  [inventory-or-observation]
  (if (and (map? inventory-or-observation)
           (contains? inventory-or-observation :source-inventory)
           (contains? inventory-or-observation :source-material)
           (contains? inventory-or-observation :source-hashes))
    inventory-or-observation
    (p18-t02-source-observation inventory-or-observation)))

(defn p18-t02-assert-source-observations-stable!
  [before after]
  (when-not (= (select-keys before [:source-inventory :source-material])
               (select-keys after [:source-inventory :source-material]))
    (fail! "P18T02006"
           "packaged JVM CLI source inventory changed during the build"
           {:source-span {:source "P18-T02 source inventory"}
            :missing-fact :stable-packaged-source-inventory
            :source-inventory-before (:source-inventory before)
            :source-inventory-after (:source-inventory after)
            :source-material-before (:source-material before)
            :source-material-after (:source-material after)
            :remediation
            "Build from an unchanged complete packaged source inventory and retry."}))
  after)

(declare p18-t02-jar-entry-records p18-t02-jar-observation)

(defn p18-t02-build-packaged-jvm-cli-observation!
  "Build once and return the operation-local observations used by package records."
  ([]
   (p18-t02-build-packaged-jvm-cli-observation!
    (p18-t02-source-observation)
    true))
  ([inventory]
   (p18-t02-build-packaged-jvm-cli-observation!
    (p18-t02-source-observation inventory)
    false))
  ([source-observation refresh-post-inventory?]
   (p18-ensure-dir! p18-t02-build-root)
   (p18-ensure-dir! p18-t02-classes-dir)
   (p18-t02-write-manifest!)
   (p18-t02-shell!
    "p18-t02-javac"
    ["javac" "-cp" (str/join java.io.File/pathSeparator
                             (p18-t02-classpath-entries))
     "-d" p18-t02-classes-dir
     p18-t02-launcher-source])
   (p18-t02-shell!
    "p18-t02-jar"
    (:jar-command source-observation))
   (let [post-source-observation
         (if refresh-post-inventory?
           (p18-t02-source-observation)
           (p18-t02-source-observation
            (:source-inventory source-observation)))
         _ (p18-t02-assert-source-observations-stable!
            source-observation post-source-observation)
         jar-observation (p18-t02-jar-observation)
         jar-entries (:jar-entries jar-observation)
         jar-file-entries (:jar-file-entries jar-observation)
         source-report
         (p18-t02-jar-source-inventory-report
          (:source-inventory post-source-observation) jar-entries)
         jar-report
         (p18-t02-jar-inventory-report
          (:source-inventory post-source-observation) jar-file-entries)
         observation
         {:source-observation-before source-observation
          :source-observation-after post-source-observation
          :jar-observation jar-observation
          :jar-source-inventory-report source-report
          :jar-inventory-report jar-report
          :jar-path p18-t02-jar-path}]
     (when-not (and (:valid? source-report) (:valid? jar-report))
       (fail! "P18T02006"
              "packaged JVM CLI JAR inventory is invalid"
              {:source-span {:source p18-t02-jar-path}
               :missing-fact :exact-whole-jar-file-inventory
               :expected-file-entries (:expected jar-report)
               :observed-file-entries (:observed jar-report)
               :missing-file-entries (:missing jar-report)
               :unexpected-file-entries (:unexpected jar-report)
               :packaged-source-inventory-valid?
               (:valid? source-report)
               :remediation
               "Rebuild the package from the exact declared launcher and Clojure source inventory without omissions or ambient files."}))
     observation)))

(defn p18-t02-build-packaged-jvm-cli!
  ([] (p18-t02-build-packaged-jvm-cli-observation!)
      p18-t02-jar-path)
  ([inventory]
   (p18-t02-build-packaged-jvm-cli-observation! inventory)
   p18-t02-jar-path))

(defn p18-t02-jar-entry-records
  []
  (with-open [jar-file (java.util.jar.JarFile. p18-t02-jar-path)]
    (->> (enumeration-seq (.entries jar-file))
         (map (fn [entry]
                {:name (.getName entry)
                 :directory? (.isDirectory entry)}))
         (sort-by :name)
         vec)))

(defn p18-t02-jar-entries
  []
  (mapv :name (p18-t02-jar-entry-records)))

(defn p18-t02-jar-file-entries
  []
  (into []
        (comp (remove :directory?)
              (map :name))
        (p18-t02-jar-entry-records)))

(defn p18-t02-jar-observation
  []
  (let [records (p18-t02-jar-entry-records)]
    {:jar-entry-records records
     :jar-entries (mapv :name records)
     :jar-file-entries (into []
                             (comp (remove :directory?)
                                   (map :name))
                             records)}))

(defn p18-t02-dependency-record
  []
  (let [entries (p18-t02-runtime-classpath-entries)
        dependencies
        (mapv (fn [path]
                {:path path
                 :content-hash (p18-file-sha256 path)
                 :runtime-dependency? true})
              entries)
        base {:artifact :gravity/p18-t02-dependency-record
              :schema-version "gravity.dependency-record/v1"
              :dependency-source :clojure-runtime-classpath
              :bootstrap-hosted? true
              :seedless-release? false
              :dependencies dependencies}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-package-manifest
  [jar-hash]
  (let [base {:artifact :gravity/p18-t02-package-manifest
              :schema-version "gravity.package-manifest/v1"
              :package-id "gravity/jvm-cli"
              :package-version "0.0.0-p18-t02"
              :entrypoint "gravity.cli.Main"
              :source-roots ["bootstrap/clojure/src" "bootstrap/clojure/java"]
              :profile :hosted
              :target :jvm-hosted
              :runtime :clojure/jvm
              :artifact-kind :executable
              :artifact-path p18-t02-jar-path
              :content-hash jar-hash
              :bootstrap-hosted? true
              :packaged-jvm-cli? true
              :seedless-release? false
              :public-release-boundary? false
              :replacement-objective :replace-with-self-hosted-release-artifact}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-artifact-manifest
  [package-manifest jar-hash]
  (let [base {:artifact :gravity/p18-t02-artifact-manifest
              :schema-version "gravity.artifact-manifest/v1"
              :kind :executable
              :path p18-t02-jar-path
              :content-hash jar-hash
              :package-id (:package-id package-manifest)
              :package-version (:package-version package-manifest)
              :profile (:profile package-manifest)
              :target (:target package-manifest)
              :compiler-identity :clojure-stage0-bootstrap
              :runtime-boundary :clojure-jvm-runtime-dependency
              :evidence-links [:package-manifest :dependency-record
                               :provenance-record :sbom :signing-record]
              :bootstrap-hosted? true
              :seedless-release? false}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-reproducible-build-record
  ([package-manifest jar-hash]
   (p18-t02-reproducible-build-record
    package-manifest jar-hash (p18-t02-source-inventory)))
  ([package-manifest jar-hash inventory-or-observation]
   (let [source-observation
         (p18-t02-source-observation-input inventory-or-observation)
         base {:artifact :gravity/p18-t02-reproducible-build-record
               :schema-version "gravity.reproducible-build/v1"
               :package-id (:package-id package-manifest)
               :recipe {:javac ["javac" "-cp" "<runtime-classpath>" "-d"
                                p18-t02-classes-dir p18-t02-launcher-source]
                        :jar (:jar-command source-observation)}
               :timestamp-policy :fixed-jar-entry-time
               :network-policy :disabled-after-local-runtime-dependencies
               :source-inventory (:source-inventory source-observation)
               :source-hashes (:source-hashes source-observation)
               :expected-output-hash jar-hash
               :reproducible-claim :packaged-jvm-bootstrap-milestone
               :final-release-reproducible? false}]
     (assoc base :artifact-id (c4-artifact-id base)))))

(defn p18-t02-provenance-record
  ([package-manifest dependency-record reproducible-record jar-hash]
   (p18-t02-provenance-record
    package-manifest dependency-record reproducible-record jar-hash
    (p18-t02-source-inventory)))
  ([package-manifest dependency-record reproducible-record jar-hash
   inventory-or-observation]
   (let [source-observation
         (p18-t02-source-observation-input inventory-or-observation)
         base {:artifact :gravity/p18-t02-provenance-record
               :schema-version "gravity.provenance/v1"
               :artifact-path p18-t02-jar-path
               :artifact-content-hash jar-hash
               :package-id (:package-id package-manifest)
               :project-manifest-hash
               (or (get (:source-hashes source-observation) "deps.edn")
                   (p18-file-sha256 "deps.edn"))
               :compiler-identity :clojure-stage0-bootstrap
               :builder-identity {:kind :local-jdk
                                  :java-version (System/getProperty "java.version")
                                  :java-runtime (System/getProperty "java.runtime.name")}
               :source-material (:source-material source-observation)
               :dependency-record-id (:artifact-id dependency-record)
               :reproducible-build-record-id (:artifact-id reproducible-record)
               :generated-source-ledger []
               :binary-blob-ledger []
               :revocation-status :not-applicable-local-bootstrap-package
               :bootstrap-hosted? true
               :seedless-release? false}]
     (assoc base :artifact-id (c4-artifact-id base)))))

(defn p18-t02-sbom-record
  ([package-manifest dependency-record provenance-record]
   (p18-t02-sbom-record
    package-manifest dependency-record provenance-record
    (p18-t02-source-inventory)))
  ([package-manifest dependency-record provenance-record inventory-or-observation]
   (let [source-observation
         (p18-t02-source-observation-input inventory-or-observation)
         base {:artifact :gravity/p18-t02-sbom
               :schema-version "gravity.sbom/v1"
               :package-id (:package-id package-manifest)
               :package-version (:package-version package-manifest)
               :artifact-path p18-t02-jar-path
               :dependencies (:dependencies dependency-record)
               :source-references (:source-material source-observation)
               :capability-summary {:declared #{:io/stdout}
                                    :denied #{:network/ambient :shell/ambient}}
               :unsafe-summary {:unsafe-islands 0}
               :generated-source-summary []
               :binary-blob-summary []
               :provenance-record-id (:artifact-id provenance-record)
               :bootstrap-hosted? true
               :seedless-release? false}]
     (assoc base :artifact-id (c4-artifact-id base)))))

(defn p18-t02-signing-record
  [artifact-manifest provenance-record sbom-record]
  (let [payload {:artifact-manifest-id (:artifact-id artifact-manifest)
                 :content-hash (:content-hash artifact-manifest)
                 :provenance-record-id (:artifact-id provenance-record)
                 :sbom-id (:artifact-id sbom-record)
                 :signing-policy-id :p18-t02-development-hash-bound-record}
        base {:artifact :gravity/p18-t02-development-signing-record
              :schema-version "gravity.signing-record/v1"
              :payload payload
              :payload-hash (c4-artifact-id payload)
              :signature-mode :development-content-hash-record
              :cryptographic-release-signature? false
              :verification-track :bootstrap-packaged-cli
              :final-release-acceptable? false
              :replacement-objective :p18-t06-release-signing-record}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-run-packaged
  [& args]
  (p18-shell-run
   (concat ["java" "-cp"
            (str p18-t02-jar-path
                 java.io.File/pathSeparator
                 (p18-t02-runtime-classpath))
            "gravity.cli.Main"]
           args)))

(defn p18-t02-run-clojure
  [& args]
  (p18-shell-run (concat ["clojure" "-M:gravity"] args)))
