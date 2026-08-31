(ns gravity.self-hosting.p18-t00-orchestration-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.p18.t00.orchestration :as orchestration]))

(def accepted-fixture
  {:gravity "module.gravity"
   :qst "module.qst"
   :expected-stdout "ok\n"
   :bootstrap-module 'sample.main
   :release-module "sample"
   :bootstrap-output-prefix "target/bootstrap"
   :release-output-prefix "target/release"})

(defn- semantic-summary
  [path]
  (let [gravity? (.endsWith ^String path ".gravity")
        kind (if gravity? :gravity-branded-source :qst-theory-source)]
    {:source-path path
     :source-extension (if gravity? ".gravity" ".qst")
     :source-kind kind
     :recognized-source? true
     :source-unit-path path
     :source-unit-kind kind
     :reader-source-path path
     :reader-source-map-path path
     :module {:name 'sample.main}
     :syntax-count 2
     :compiled-kind :gravity/compiled-module}))

(defn- shell-result
  [release? args]
  (let [command (if release? (second args) (nth args 2 nil))
        output-path (last args)
        source-path (cond
                      (.endsWith ^String output-path "-gravity")
                      "module.gravity"

                      (.endsWith ^String output-path "-qst")
                      "module.qst")]
    (cond
      (= "check" command)
      {:exit 0
       :out (if release?
              "gravity check passed: sample\n"
              "gravity stage0 check passed: sample.main\n")
       :err ""}

      (= "compile" command)
      {:exit 0 :out "artifact\n" :err ""
       :artifact {:source {:path source-path}}}

      :else {:exit 0 :out "ok\n" :err ""})))

(deftest accepted-command-orchestration-preserves-shell-order
  (let [events (atom [])
        run (fn [kind release?]
              (fn [& args]
                (swap! events conj [kind (vec args)])
                (shell-result release? args)))
        record
        (orchestration/accepted-extension-record
         {:semantic-summary semantic-summary
          :bootstrap-shell (run :bootstrap false)
          :release-shell (run :release true)
          :read-edn-stdout :artifact
          :compile-artifact-source-path #(get-in % [:source :path])}
         accepted-fixture)]
    (doseq [key [:bootstrap-check-parity? :bootstrap-run-parity?
                 :bootstrap-compile-parity?
                 :bootstrap-run-compiled-parity? :release-check-parity?
                 :release-run-parity? :release-compile-parity?
                 :semantic-equivalent?
                 :provenance-preserves-actual-extension?
                 :no-deprecation-or-compatibility-warning?
                 :matches-expected?]]
      (is (true? (get record key)) key))
    (is (= [[:bootstrap ["clojure" "-M:gravity" "check" "module.gravity"]]
            [:bootstrap ["clojure" "-M:gravity" "check" "module.qst"]]
            [:bootstrap ["clojure" "-M:gravity" "run" "module.gravity"]]
            [:bootstrap ["clojure" "-M:gravity" "run" "module.qst"]]
            [:bootstrap ["clojure" "-M:gravity" "run-compiled"
                         "module.gravity"]]
            [:bootstrap ["clojure" "-M:gravity" "run-compiled" "module.qst"]]
            [:bootstrap ["clojure" "-M:gravity" "compile" "module.gravity"
                         "-o" "target/bootstrap-gravity"]]
            [:bootstrap ["clojure" "-M:gravity" "compile" "module.qst"
                         "-o" "target/bootstrap-qst"]]
            [:bootstrap ["target/bootstrap-gravity"]]
            [:bootstrap ["target/bootstrap-qst"]]
            [:release ["bin/gravity" "check" "module.gravity"]]
            [:release ["bin/gravity" "check" "module.qst"]]
            [:release ["bin/gravity" "run" "module.gravity"]]
            [:release ["bin/gravity" "run" "module.qst"]]
            [:release ["bin/gravity" "compile" "module.gravity"
                       "-o" "target/release-gravity"]]
            [:release ["bin/gravity" "compile" "module.qst"
                       "-o" "target/release-qst"]]
            [:release ["target/release-gravity"]]
            [:release ["target/release-qst"]]]
           @events))))

(deftest rejected-command-orchestration-preserves-shell-order
  (let [events (atom [])
        rejection {:exit 1 :out "" :err "RULE rejected"}
        shell (fn [kind]
                (fn [& args]
                  (swap! events conj [kind (vec args)])
                  rejection))
        record
        (orchestration/rejected-extension-record
         {:bootstrap-shell (shell :bootstrap)
          :release-shell (shell :release)}
         {:gravity "bad.gravity" :qst "bad.qst"
          :expected-diagnostic "RULE" :output-prefix "target/bad"})]
    (is (true? (:matches-expected? record)))
    (is (= [[:bootstrap ["clojure" "-M:gravity" "run-compiled"
                         "bad.gravity"]]
            [:bootstrap ["clojure" "-M:gravity" "run-compiled" "bad.qst"]]
            [:release ["bin/gravity" "compile" "bad.gravity"
                       "-o" "target/bad-gravity"]]
            [:release ["bin/gravity" "compile" "bad.qst"
                       "-o" "target/bad-qst"]]]
           @events))))

(deftest artifact-assembly-preserves-release-first-and-identity-input
  (let [events (atom [])
        artifact
        (orchestration/co-canonical-source-extensions-artifact!
         {:write-final-release-artifacts! #(swap! events conj :release)
          :accepted-fixtures [:accepted]
          :rejected-fixtures [:rejected]
          :accepted-extension-record
          #(do (swap! events conj [:accepted %]) {:accepted %})
          :rejected-extension-record
          #(do (swap! events conj [:rejected %]) {:rejected %})
          :capability-proof
          #(do (swap! events conj [:proof (:kind %)]) {:proved true})
          :artifact-id
          #(do (swap! events conj [:id (:capability-based-proof %)]) "id")})]
    (is (= [:release [:accepted :accepted] [:rejected :rejected]
            [:proof :gravity/p18-t00-co-canonical-source-extensions-proof]
            [:id {:proved true}]]
           @events))
    (is (= "id" (:artifact-id artifact)))
    (is (= [{:accepted :accepted}] (:accepted-extension-parity artifact)))
    (is (= [{:rejected :rejected}] (:rejected-extension-parity artifact)))
    (is (= ["C2" "C15" "PKG3" "PKG10" "PKG12"
            "T1" "BOOT7" "BOOT8" "D9"]
           (:governing-documents artifact)))))

(deftest bootstrap-artifact-wrapper-binds-current-public-operations
  (let [events (atom [])
        artifact
        (with-redefs
         [bootstrap/p18-t06-write-final-release-artifacts!
          #(swap! events conj :release)
          bootstrap/p18-t00-accepted-extension-fixtures [:accepted]
          bootstrap/p18-t00-rejected-extension-fixtures [:rejected]
          bootstrap/p18-t00-accepted-extension-record
          #(do (swap! events conj [:accepted %]) {:accepted %})
          bootstrap/p18-t00-rejected-extension-record
          #(do (swap! events conj [:rejected %]) {:rejected %})
          bootstrap/p18-t00-capability-proof
          #(do (swap! events conj [:proof (:kind %)]) {:proved true})
          bootstrap/c4-artifact-id
          #(do (swap! events conj [:id (:capability-based-proof %)]) "id")]
         (bootstrap/p18-t00-co-canonical-source-extensions-artifact!))]
    (is (= [:release [:accepted :accepted] [:rejected :rejected]
            [:proof :gravity/p18-t00-co-canonical-source-extensions-proof]
            [:id {:proved true}]]
           @events))
    (is (= "id" (:artifact-id artifact)))
    (is (= {:proved true} (:capability-based-proof artifact)))))

(deftest report-writing-and-bootstrap-public-wrappers-retain-contract
  (let [events (atom [])
        artifact {:accepted-extension-parity [:accepted]
                  :rejected-extension-parity [:rejected]}
        result
        (orchestration/write-co-canonical-source-extension-artifacts!
         {:artifact! #(do (swap! events conj :artifact) artifact)
          :artifact-dir "artifacts"
          :report-path "reports/report.md"
          :report-parent "reports"
          :ensure-dir! #(swap! events conj [:ensure %])
          :write-edn! #(swap! events conj [:edn %1 %2])
          :write-report! #(swap! events conj [:report %1 %2])
          :report-markdown (constantly "report")})]
    (is (= artifact result))
    (is (= [:artifact [:ensure "artifacts"] [:ensure "reports"]
            [:edn "artifacts/p18-t00-co-canonical-source-extensions-proof.edn"
             artifact]
            [:edn "artifacts/p18-t00-accepted-extension-parity.edn" [:accepted]]
            [:edn "artifacts/p18-t00-rejected-extension-parity.edn" [:rejected]]
            [:report "reports/report.md" "report"]]
           @events)))
  (doseq [[var expected]
          [[#'bootstrap/p18-t00-accepted-extension-record
            '([{:keys [gravity qst expected-stdout bootstrap-module
                       release-module bootstrap-output-prefix
                       release-output-prefix]}])]
           [#'bootstrap/p18-t00-rejected-extension-record
            '([{:keys [gravity qst expected-diagnostic output-prefix]}])]
           [#'bootstrap/p18-t00-co-canonical-source-extensions-artifact! '([])]
           [#'bootstrap/p18-t00-write-co-canonical-source-extension-artifacts!
            '([])]]]
    (is (= expected (:arglists (meta var)))))
  (is (not-any? #{'gravity.bootstrap}
                (map ns-name
                     (vals (ns-aliases 'gravity.p18.t00.orchestration))))))
