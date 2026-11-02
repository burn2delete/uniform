

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

(declare p18-t02-jar-entries p18-t02-jar-file-entries)

(defn p18-t02-build-packaged-jvm-cli!
  []
  (p18-t02-validate-source-inventory!)
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
   (p18-t02-jar-command))
  (let [source-report
        (p18-t02-jar-source-inventory-report (p18-t02-jar-entries))
        jar-report
        (p18-t02-jar-inventory-report (p18-t02-jar-file-entries))]
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
              "Rebuild the package from the exact declared launcher and Clojure source inventory without omissions or ambient files."})))
  p18-t02-jar-path)

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
  [package-manifest jar-hash]
  (let [base {:artifact :gravity/p18-t02-reproducible-build-record
              :schema-version "gravity.reproducible-build/v1"
              :package-id (:package-id package-manifest)
              :recipe {:javac ["javac" "-cp" "<runtime-classpath>" "-d"
                               p18-t02-classes-dir p18-t02-launcher-source]
                       :jar (p18-t02-jar-command)}
              :timestamp-policy :fixed-jar-entry-time
              :network-policy :disabled-after-local-runtime-dependencies
              :source-inventory p18-t02-source-inventory
              :source-hashes (p18-t02-source-hashes)
              :expected-output-hash jar-hash
              :reproducible-claim :packaged-jvm-bootstrap-milestone
              :final-release-reproducible? false}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-provenance-record
  [package-manifest dependency-record reproducible-record jar-hash]
  (let [base {:artifact :gravity/p18-t02-provenance-record
              :schema-version "gravity.provenance/v1"
              :artifact-path p18-t02-jar-path
              :artifact-content-hash jar-hash
              :package-id (:package-id package-manifest)
              :project-manifest-hash (p18-file-sha256 "deps.edn")
              :compiler-identity :clojure-stage0-bootstrap
              :builder-identity {:kind :local-jdk
                                 :java-version (System/getProperty "java.version")
                                 :java-runtime (System/getProperty "java.runtime.name")}
              :source-material (p18-t02-source-material)
              :dependency-record-id (:artifact-id dependency-record)
              :reproducible-build-record-id (:artifact-id reproducible-record)
              :generated-source-ledger []
              :binary-blob-ledger []
              :revocation-status :not-applicable-local-bootstrap-package
              :bootstrap-hosted? true
              :seedless-release? false}]
    (assoc base :artifact-id (c4-artifact-id base))))

(defn p18-t02-sbom-record
  [package-manifest dependency-record provenance-record]
  (let [base {:artifact :gravity/p18-t02-sbom
              :schema-version "gravity.sbom/v1"
              :package-id (:package-id package-manifest)
              :package-version (:package-version package-manifest)
              :artifact-path p18-t02-jar-path
              :dependencies (:dependencies dependency-record)
              :source-references (p18-t02-source-material)
              :capability-summary {:declared #{:io/stdout}
                                   :denied #{:network/ambient :shell/ambient}}
              :unsafe-summary {:unsafe-islands 0}
              :generated-source-summary []
              :binary-blob-summary []
              :provenance-record-id (:artifact-id provenance-record)
              :bootstrap-hosted? true
              :seedless-release? false}]
    (assoc base :artifact-id (c4-artifact-id base))))

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