

(defn p18-t04-public-self-host-verify-command-artifact!
  []
  (let [p15-final-proof (p18-t06-current-p15-final-seed-proof)
        p18-final-proof (p18-t06-write-final-release-artifacts!)
        complete? (p18-t04-self-host-verify-complete?
                   p15-final-proof p18-final-proof)
        compiler-source (p18-t04-self-host-verify-compiler-source)
        diagnostics
        (if complete?
          []
          [(p18-t04-diagnostic-record
            "P18T04007"
            "gravity self-host verify"
            {:source "gravity self-host verify"
             :p15-final-seed-retirement-proof
             (select-keys p15-final-proof
                          [:kind :artifact-id :proof-id :status
                           :full-language-compiler-self-hosted?
                           :clojure-seed-retired?
                           :clojure-seed-boundary?])
             :p18-final-release-proof
             (select-keys p18-final-proof
                          [:kind :artifact-id :status :final-release?
                           :seedless-release? :clojure-seed-boundary?])
             :compiler-source compiler-source
             :proof-artifact-path
             p18-t04-public-self-host-verify-proof-path
             :remediation
             "Complete P15-S23 final seed retirement and P18-T06 final release before `gravity self-host verify` can succeed."})])
        artifact-base
        {:kind :gravity/p18-t04-public-self-host-verify-command-proof
         :task "P18-T04"
         :status (if complete? :complete :incomplete)
         :phase :binary-distribution-and-seedless-release
         :command ["gravity" "self-host" "verify"]
         :scope :current-public-release-surface
         :governing-documents ["TEST13" "BOOT7" "BOOT8" "T1" "D9"]
         :compiler-source compiler-source
         :p15-final-seed-retirement-proof
         (select-keys p15-final-proof
                      [:kind :artifact :artifact-id :proof-id :status
                       :source-path :full-language-compiler-self-hosted?
                       :clojure-seed-retired? :clojure-seed-boundary?
                       :next-required-capability])
         :p18-final-release-proof
         (select-keys p18-final-proof
                      [:kind :artifact-id :status :target :final-release?
                       :seedless-release? :clojure-seed-boundary?
                       :next-required-capability])
         :p18-final-release-proof-path p18-t06-final-proof-path
         :p18-release-binary-path p18-t06-release-binary-path
         :bootstrap-hosted? true
         :final-self-host-verification? complete?
         :full-language-conformance? false
         :self-hosted-conformance-runner? false
         :diagnostics diagnostics}
        proof (p18-t04-public-self-host-verify-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p18-t04-write-public-self-host-verify-command-artifacts!
  []
  (let [artifact (p18-t04-public-self-host-verify-command-artifact!)]
    (p18-t02-write-edn! p18-t04-public-self-host-verify-proof-path
                        artifact)
    (p18-t02-write-edn! p18-t04-public-self-host-verify-diagnostics-path
                        (:diagnostics artifact))
    artifact))

(defn p18-t04-public-self-host-verify-usage!
  [args]
  (p18-t04-fail!
   "P18T04008"
   {:source "gravity self-host"
    :command (vec args)
    :expected-command ["gravity" "self-host" "verify"]
    :remediation "Use `gravity self-host verify` exactly."}))

(defn p18-t04-public-self-host-verify-command!
  [args]
  (if-not (and (= "self-host" (first args))
               (= "verify" (second args))
               (= 2 (count args)))
    (p18-t04-public-self-host-verify-usage! args)
    (let [artifact (p18-t04-write-public-self-host-verify-command-artifacts!)]
      (if (= :complete (:status artifact))
        (prn artifact)
        (p18-t04-fail!
         "P18T04007"
         {:source "gravity self-host verify"
          :command (vec args)
          :proof-artifact-path p18-t04-public-self-host-verify-proof-path
          :proof-artifact-id (:artifact-id artifact)
          :p15-final-seed-retirement-proof
          (:p15-final-seed-retirement-proof artifact)
          :p18-final-release-proof (:p18-final-release-proof artifact)
          :compiler-source (:compiler-source artifact)
          :bootstrap-hosted? true
          :full-language-compiler-self-hosted? false
          :clojure-seed-boundary? true
          :remediation
          "Complete P15-S23 final seed retirement and P18-T06 final release before claiming public self-host verification."})))))

(def p18-t00-artifact-dir p18-t00-semantics/artifact-dir)
(def p18-t00-report-path p18-t00-semantics/report-path)
(def p18-t00-accepted-extension-fixtures
  p18-t00-semantics/accepted-extension-fixtures)
(def p18-t00-rejected-extension-fixtures
  p18-t00-semantics/rejected-extension-fixtures)

(defn p18-t00-shell
  [& args]
  (p18-shell-run args))

(defn p18-t00-output-has-warning?
  [result]
  (p18-t00-semantics/output-has-warning? result))

(defn p18-t00-semantic-summary
  [path]
  (let [source-text (slurp path)
        compile-artifact (compile-source path source-text)
        reader-artifact (read-source-artifact path source-text)
        source-unit (c2-source-unit-record
                     path source-text standard-reader-options)]
    (p18-t00-semantics/semantic-summary
     {:path path
      :compile-artifact compile-artifact
      :reader-artifact reader-artifact
      :source-unit source-unit
      :source-extension (gravity-source-extension path)
      :source-kind (gravity-source-kind path)
      :recognized-source? (qst-or-gravity-source? path)})))

(defn p18-t00-compile-artifact-source-path
  [artifact]
  (p18-t00-semantics/compile-artifact-source-path artifact))

(defn p18-t00-accepted-extension-record
  [{:keys [gravity qst expected-stdout bootstrap-module release-module
           bootstrap-output-prefix release-output-prefix]}]
  (p18-t00-orchestration/accepted-extension-record
   {:semantic-summary p18-t00-semantic-summary
    :bootstrap-shell p18-t00-shell
    :release-shell p18-t06-shell
    :read-edn-stdout p18-t04-read-edn-stdout
    :compile-artifact-source-path p18-t00-compile-artifact-source-path}
   {:gravity gravity
    :qst qst
    :expected-stdout expected-stdout
    :bootstrap-module bootstrap-module
    :release-module release-module
    :bootstrap-output-prefix bootstrap-output-prefix
    :release-output-prefix release-output-prefix}))

(defn p18-t00-rejected-extension-record
  [{:keys [gravity qst expected-diagnostic output-prefix]}]
  (p18-t00-orchestration/rejected-extension-record
   {:bootstrap-shell p18-t00-shell
    :release-shell p18-t06-shell}
   {:gravity gravity
    :qst qst
    :expected-diagnostic expected-diagnostic
    :output-prefix output-prefix}))

(defn p18-t00-capability-proof
  [artifact]
  (p18-t00-semantics/capability-proof
   artifact
   {:co-canonical-source-extensions co-canonical-source-extensions
    :accepted-extension-fixtures p18-t00-accepted-extension-fixtures
    :qst-source-kind (gravity-source-kind "examples/core-app.qst")
    :gravity-source-kind
    (gravity-source-kind "examples/core-app.gravity")}))

(defn p18-t00-report-markdown
  [artifact]
  (p18-t00-semantics/report-markdown artifact p18-t00-artifact-dir))

(defn p18-t00-co-canonical-source-extensions-artifact!
  []
  (p18-t00-orchestration/co-canonical-source-extensions-artifact!
   {:write-final-release-artifacts! p18-t06-write-final-release-artifacts!
    :accepted-fixtures p18-t00-accepted-extension-fixtures
    :rejected-fixtures p18-t00-rejected-extension-fixtures
    :accepted-extension-record p18-t00-accepted-extension-record
    :rejected-extension-record p18-t00-rejected-extension-record
    :capability-proof p18-t00-capability-proof
    :artifact-id c4-artifact-id}))

(defn p18-t00-write-co-canonical-source-extension-artifacts!
  []
  (p18-t00-orchestration/write-co-canonical-source-extension-artifacts!
   {:artifact! p18-t00-co-canonical-source-extensions-artifact!
    :artifact-dir p18-t00-artifact-dir
    :report-path p18-t00-report-path
    :report-parent (.getParent (java.io.File. p18-t00-report-path))
    :ensure-dir! p18-ensure-dir!
    :write-edn! p18-t02-write-edn!
    :write-report! spit
    :report-markdown p18-t00-report-markdown}))