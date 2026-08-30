(ns gravity.self-hosting.cli-dispatch-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.cli.dispatch :as dispatch]
            [gravity.cli.entrypoint :as entrypoint]))

(defn- fake-resolver
  [operation]
  (case operation
    p18-cli-help-text (fn [] "injected help\n")
    p18-cli-version-record (fn [] {:version "injected"})
    compiler-c2-reader-file-artifact
    (fn [path] {:artifact :reader :path path})
    check-file-artifact (fn [path] {:module path})
    check-artifact-module-name :module
    run-file (fn [path] (str "ran " path "\n"))
    run-compiled-file (fn [path] (str "compiled " path "\n"))
    (throw (ex-info "unexpected fake operation" {:operation operation}))))

(deftest handled-commands-return-true-and-use-injected-operations
  (testing "presentation commands"
    (let [result (atom nil)
          output (with-out-str
                   (reset! result
                           (dispatch/dispatch! fake-resolver ["help"])))]
      (is (true? @result))
      (is (= "injected help\n" output))))
  (testing "path artifact commands"
    (let [result (atom nil)
          output (with-out-str
                   (reset!
                    result
                    (dispatch/dispatch!
                     fake-resolver ["read" "module.gravity"])))]
      (is (true? @result))
      (is (= {:artifact :reader :path "module.gravity"}
             (edn/read-string output)))))
  (testing "unknown commands return false without output"
    (let [result (atom nil)
          output (with-out-str
                   (reset! result
                           (dispatch/dispatch!
                            fake-resolver ["not-a-command"])))]
      (is (false? @result))
      (is (empty? output)))))

(deftest entrypoint-owns-unknown-command-and-diagnostic-exits
  (testing "unknown command"
    (let [statuses (atom [])
          stderr (java.io.StringWriter.)]
      (binding [*err* stderr]
        (entrypoint/run!
         ["not-a-command"]
         {:resolve-operation fake-resolver
          :print-diagnostic! (fn [_] (throw (AssertionError.)))
          :exit! #(swap! statuses conj %)}))
      (is (= [2] @statuses))
      (is (str/starts-with? (str stderr)
                            "usage: clojure -M:gravity "))))
  (testing "ExceptionInfo is presented and exits one"
    (let [statuses (atom [])
          diagnostics (atom [])
          resolver (fn [operation]
                     (case operation
                       compiler-c2-reader-file-artifact
                       (fn [_] (throw (ex-info "rejected" {:id "TEST"})))))]
      (entrypoint/run!
       ["read" "rejected.gravity"]
       {:resolve-operation resolver
        :print-diagnostic! #(swap! diagnostics conj (ex-data %))
        :exit! #(swap! statuses conj %)})
      (is (= [{:id "TEST"}] @diagnostics))
      (is (= [1] @statuses)))))

(deftest extracted-command-boundary-is-bootstrap-free
  (doseq [namespace ['gravity.cli.commands.bootstrap
                     'gravity.cli.commands.compiler
                     'gravity.cli.commands.platform
                     'gravity.cli.compile-command
                     'gravity.cli.dispatch
                     'gravity.cli.entrypoint]]
    (require namespace)
    (is (not-any? #{'gravity.bootstrap}
                  (map ns-name (vals (ns-aliases namespace)))))))
