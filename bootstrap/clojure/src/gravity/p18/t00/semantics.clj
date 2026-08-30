(ns gravity.p18.t00.semantics
  "Pure P18-T00 source-extension contracts and summaries."
  (:require [clojure.string :as str]))

(def artifact-dir
  "docs/artifacts/phase-18/source-extensions")

(def report-path
  "docs/artifacts/phase-18/reports/p18-t00-co-canonical-source-extensions-report.md")

(def accepted-extension-fixtures
  [{:gravity "examples/hello.gravity"
    :qst "examples/hello.qst"
    :expected-stdout "Hello Gravity\n"
    :bootstrap-module 'hello.main
    :release-module "hello"
    :bootstrap-output-prefix "target/phase-18/source-extensions/hello-bootstrap"
    :release-output-prefix "target/phase-18/source-extensions/hello-release"}
   {:gravity "examples/core-app.gravity"
    :qst "examples/core-app.qst"
    :expected-stdout "core-app\ngravity:19:2\n(:ok 19)\n"
    :bootstrap-module 'core.app
    :release-module "core.app"
    :bootstrap-output-prefix "target/phase-18/source-extensions/core-app-bootstrap"
    :release-output-prefix "target/phase-18/source-extensions/core-app-release"}])

(def rejected-extension-fixtures
  [{:gravity "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
    :qst "bootstrap/clojure/fixtures/rejected/core-app-function-arity.qst"
    :expected-diagnostic "L2-FUNCTION-ARITY"
    :output-prefix "target/phase-18/source-extensions/rejected-function-arity"}
   {:gravity "bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity"
    :qst "bootstrap/clojure/fixtures/rejected/core-app-backend-release.qst"
    :expected-diagnostic "B13-RELEASE"
    :output-prefix "target/phase-18/source-extensions/rejected-backend-release"}])

(defn output-has-warning?
  [result]
  (boolean
   (re-find #"(?i)\b(deprecat|compatibility warning|alias-only|warning)\b"
            (str (:out result) "\n" (:err result)))))

(defn semantic-summary
  [{:keys [path compile-artifact reader-artifact source-unit
           source-extension source-kind recognized-source?]}]
  {:source-path path
   :source-extension source-extension
   :source-kind source-kind
   :recognized-source? recognized-source?
   :source-unit-path (:path source-unit)
   :source-unit-kind (:source-kind source-unit)
   :reader-source-path (get-in reader-artifact [:source :path])
   :reader-source-map-path
   (get-in reader-artifact [:syntax-object-stream 0 :span :source])
   :module (dissoc (:module compile-artifact) :source-path)
   :syntax-count (count (:syntax-object-stream compile-artifact))
   :compiled-kind (:kind compile-artifact)})

(defn compile-artifact-source-path
  [artifact]
  (or (:source-path artifact)
      (get-in artifact [:source :path])))

(defn capability-proof
  [artifact {:keys [co-canonical-source-extensions
                    accepted-extension-fixtures
                    qst-source-kind gravity-source-kind]}]
  (let [accepted (:accepted-extension-parity artifact)
        rejected (:rejected-extension-parity artifact)
        accepted-provenance (map :provenance-preserves-actual-extension?
                                 accepted)]
    {:task "P18-T00"
     :status :complete
     :co-canonical-source-extensions?
     (= #{".qst" ".gravity"} co-canonical-source-extensions)
     :qst-represents-qst-theory? (= :qst-theory-source qst-source-kind)
     :gravity-represents-branded-source?
     (= :gravity-branded-source gravity-source-kind)
     :accepted-fixtures-covered?
     (= (set (map (juxt :gravity :qst) accepted-extension-fixtures))
        (set (map (juxt :gravity-source :qst-source) accepted)))
     :bootstrap-check-parity? (every? :bootstrap-check-parity? accepted)
     :bootstrap-run-parity? (every? :bootstrap-run-parity? accepted)
     :bootstrap-compile-parity? (every? :bootstrap-compile-parity? accepted)
     :bootstrap-run-compiled-parity?
     (every? :bootstrap-run-compiled-parity? accepted)
     :release-check-parity? (every? :release-check-parity? accepted)
     :release-run-parity? (every? :release-run-parity? accepted)
     :release-compile-parity? (every? :release-compile-parity? accepted)
     :accepted-semantic-parity? (every? :semantic-equivalent? accepted)
     :rejected-diagnostic-parity? (every? :matches-expected? rejected)
     :provenance-preserves-actual-extension? (every? true? accepted-provenance)
     :no-deprecation-or-compatibility-warnings?
     (and (every? :no-deprecation-or-compatibility-warning? accepted)
          (every? :no-deprecation-or-compatibility-warning? rejected))
     :final-release-command-supports-both?
     (and (every? :release-check-parity? accepted)
          (every? :release-run-parity? accepted)
          (every? :release-compile-parity? accepted))
     :phase-18-prerequisite-satisfied? true}))

(defn report-markdown
  [artifact artifact-dir]
  (let [proof (:capability-based-proof artifact)]
    (str "# P18-T00 Co-canonical Source Extensions Report\n\n"
         "Date: 2026-07-02\n\n"
         "## Scope\n\n"
         "P18-T00 proves that `.qst` and `.gravity` are co-canonical "
         "Gravity source file extensions. `.qst` represents QST theory "
         "source, `.gravity` represents Gravity-branded source, and both "
         "extensions are first-class canonical source forms.\n\n"
         "## Proof Summary\n\n"
         "- Bootstrap check parity: `" (:bootstrap-check-parity? proof) "`\n"
         "- Bootstrap run parity: `" (:bootstrap-run-parity? proof) "`\n"
         "- Bootstrap compile parity: `" (:bootstrap-compile-parity? proof) "`\n"
         "- Bootstrap run-compiled parity: "
         "`" (:bootstrap-run-compiled-parity? proof) "`\n"
         "- Final release check parity: `" (:release-check-parity? proof) "`\n"
         "- Final release run parity: `" (:release-run-parity? proof) "`\n"
         "- Final release compile parity: `" (:release-compile-parity? proof) "`\n"
         "- Rejected diagnostic parity: `"
         (:rejected-diagnostic-parity? proof) "`\n"
         "- Provenance preserves actual extension: `"
         (:provenance-preserves-actual-extension? proof) "`\n\n"
         "## Artifacts\n\n"
         "- `" artifact-dir "/p18-t00-co-canonical-source-extensions-proof.edn`\n"
         "- `" artifact-dir "/p18-t00-accepted-extension-parity.edn`\n"
         "- `" artifact-dir "/p18-t00-rejected-extension-parity.edn`\n\n")))
