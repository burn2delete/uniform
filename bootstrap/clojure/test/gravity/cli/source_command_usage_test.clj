(ns gravity.cli.source-command-usage-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.cli.diagnostic-presentation :as diagnostic-presentation]
            [gravity.cli.entrypoint :as entrypoint]))

(defn- observe
  [arguments operations]
  (let [resolutions (atom [])
        calls (atom [])
        diagnostics (atom [])
        exits (atom [])
        stdout (java.io.StringWriter.)
        stderr (java.io.StringWriter.)]
    (binding [*out* stdout *err* stderr]
      (with-redefs [clojure.core/shutdown-agents (fn [])]
        (entrypoint/run!
         arguments
         {:resolve-operation
          (fn [operation]
            (swap! resolutions conj operation)
            (if-let [implementation (get operations operation)]
              (fn [& operands]
                (swap! calls conj [operation (vec operands)])
                (apply implementation operands))
              (throw (AssertionError. "Unexpected service resolution"))))
          :print-diagnostic!
          (fn [exception]
            (swap! diagnostics conj exception)
            (diagnostic-presentation/print-diagnostic! (constantly nil) exception))
          :exit! #(swap! exits conj %)})))
    {:resolutions @resolutions :calls @calls :diagnostics @diagnostics
     :exits @exits :stdout (str stdout) :stderr (str stderr)}))

(def ^:private source-paths
  (for [extension [".gravity" ".qst"]
        stem ["src/main" " ./source dir/caf\u00e9" "./-source"]]
    (str stem extension)))

(deftest check-forwards-source-path
  ;; Injected services prove routing and presentation, not source acceptance.
  (doseq [path source-paths]
    (testing path
      (let [artifact {:module "recorded.module" :path path}
            result (observe ["check" path]
                            {'check-file-artifact (constantly artifact)
                             'check-artifact-module-name :module})]
        (is (= ['check-file-artifact 'check-artifact-module-name]
               (:resolutions result)))
        (is (= [['check-file-artifact [path]]
                ['check-artifact-module-name [artifact]]]
               (:calls result)))
        (is (= "gravity stage0 check passed: recorded.module\n" (:stdout result)))
        (is (= "" (:stderr result)))
        (is (empty? (:diagnostics result)))
        (is (empty? (:exits result)))))))

(deftest run-compiled-forwards-source-path
  (doseq [path source-paths]
    (testing path
      (let [result (observe ["run-compiled" path]
                            {'run-compiled-file (constantly "recorded output\n")})]
        (is (= ['run-compiled-file] (:resolutions result)))
        (is (= [['run-compiled-file [path]]] (:calls result)))
        (is (= "recorded output\n" (:stdout result)))
        (is (= "" (:stderr result)))
        (is (empty? (:diagnostics result)))
        (is (empty? (:exits result)))))))

(def ^:private secret-marker "RAW-SECRET-SOURCE-ARGUMENT")

(defn- assert-local-usage
  [command operands]
  (let [result (observe (into [command] operands) {})
        diagnostics (:diagnostics result)
        diagnostic (first diagnostics)
        message (str "usage: gravity " command " <file.qst|file.gravity>")]
    (is (= [2] (:exits result)))
    (is (empty? (:resolutions result)))
    (is (empty? (:calls result)))
    (is (= "" (:stdout result)))
    (is (= 1 (count diagnostics)))
    (is (= message (some-> diagnostic .getMessage)))
    (is (= {:id "T1006" :stage :command-line :severity :error
            :message message :source-span nil :profile nil :target nil
            :remediation "Provide exactly one nonblank source path and no options."}
           (ex-data diagnostic)))
    (is (= (dissoc (ex-data diagnostic) :stage)
           (edn/read-string (:stderr result))))
    (is (nil? (some-> diagnostic .getCause)))
    (is (not (str/includes? (str (:stdout result) (:stderr result)
                                 (some-> diagnostic .getMessage)
                                 (pr-str (ex-data diagnostic)))
                           secret-marker)))))

(deftest missing-source
  (doseq [command ["check" "run-compiled"]]
    (assert-local-usage command [])))

(deftest blank-source
  (doseq [command ["check" "run-compiled"]
          path ["" " " "\t\r\n"]]
    (assert-local-usage command [path])))

(deftest extra-source
  (doseq [command ["check" "run-compiled"]
          extension [".gravity" ".qst"]]
    (assert-local-usage command [(str secret-marker extension) "extra.gravity"])))

(deftest unsupported-option
  (doseq [command ["check" "run-compiled"]
          operands [[(str "--token=" secret-marker)]
                    [(str "-" secret-marker ".qst")]
                    ["--" (str secret-marker ".gravity")]
                    [(str secret-marker ".gravity") "--format" "json"]
                    ["module.qst" (str "--token=" secret-marker)]]]
    (assert-local-usage command operands)))

(deftest non-string-source
  (doseq [command ["check" "run-compiled"] path [nil 42]]
    (assert-local-usage command [path])))

(defn- assert-service-failure
  [id]
  (doseq [[command operation] [["check" 'check-file-artifact]
                               ["run-compiled" 'run-compiled-file]]
          extension [".gravity" ".qst"]]
    (let [path (str "src/main" extension)
          exception (ex-info "service failure" {:id id :severity :error})
          result (observe [command path] {operation (fn [_] (throw exception))})]
      (is (= [operation] (:resolutions result)))
      (is (= [[operation [path]]] (:calls result)))
      (is (= [1] (:exits result)))
      (is (= "" (:stdout result)))
      (is (= 1 (count (:diagnostics result))))
      (is (identical? exception (first (:diagnostics result))))
      (is (= (ex-data exception) (edn/read-string (:stderr result)))))))

(deftest service-failure-remains-exit-one
  (assert-service-failure "TEST-SERVICE"))

(deftest service-t1006-is-not-local-usage
  (assert-service-failure "T1006"))
