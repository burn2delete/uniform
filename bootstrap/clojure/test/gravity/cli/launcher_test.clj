(ns gravity.cli.launcher-test
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.build.invocation :as build-invocation]
            [gravity.cli :as cli]
            [gravity.cli.launcher :as launcher]))

(def ^:private presentation-aliases
  ["help" "--help" "-h" "version" "--version"])

(def ^:private repository-root
  (System/getProperty "user.dir"))

(defn- child-command
  [packaged? expression]
  ["clojure"
   (str "-J-Dgravity.packaged.jvm.cli=" (if packaged? "true" "false"))
   "-Srepro" "-M" "-e" expression])

(defn- child-observation
  [packaged? expression]
  (let [result (apply shell/sh (concat (child-command packaged? expression)
                                       [:dir repository-root]))
        lines (str/split-lines (:out result))
        marker (edn/read-string (last lines))]
    {:exit (:exit result)
     :err (:err result)
     :presentation (str/join "\n" (butlast lines))
     :marker marker}))

(defn- without-known-java-tool-options-banner
  [stderr]
  ;; The JDK emits this one deterministic line when CI supplies
  ;; JAVA_TOOL_OPTIONS. Remove only the exact inherited value; any other
  ;; stderr remains part of the assertion.
  (let [options (System/getenv "JAVA_TOOL_OPTIONS")
        banner (when (seq options)
                 (str "Picked up JAVA_TOOL_OPTIONS: " options "\n"))]
    (if (and banner (str/starts-with? stderr banner))
      (subs stderr (count banner))
      stderr)))

(defn- presentation-expression
  [argument]
  (str "(require 'gravity.cli.launcher)"
       " (gravity.cli.launcher/-main " (pr-str argument) ")"
       " (prn {:bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
       " :entrypoint-loaded (boolean (find-ns 'gravity.cli.entrypoint))"
       " :cli-loaded (boolean (find-ns 'gravity.cli))"
       " :packaged-jvm-cli? (= \"true\""
       " (System/getProperty \"gravity.packaged.jvm.cli\"))})"))

(defn- usage-expression
  [arguments]
  (str "(require 'gravity.cli.launcher)"
       " (let [exit-code (atom nil)]"
       "   (with-redefs-fn {(ns-resolve 'gravity.cli.launcher 'exit!)"
       "                      (fn [status] (reset! exit-code status))}"
       "     #(gravity.cli.launcher/-main "
       (if (seq arguments)
         (str "\"" (first arguments) "\"")
         "") "))"
       "   (prn {:exit-code @exit-code"
       "         :bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
       "         :entrypoint-loaded (boolean (find-ns 'gravity.cli.entrypoint))"
       "         :cli-loaded (boolean (find-ns 'gravity.cli))}))"))

(defn- build-expression
  []
  (str "(require 'gravity.cli.launcher 'gravity.build.invocation)"
       " (let [exit-code (atom nil)"
       "       called (atom nil)]"
       "   (with-redefs-fn"
       "     {(ns-resolve 'gravity.cli.launcher 'exit!)"
       "        (fn [status] (reset! exit-code status))"
       "      (ns-resolve 'gravity.build.invocation"
       "                  'run-declared-input-build!)"
       "        (fn [arguments]"
       "          (reset! called (vec arguments))"
       "          :accepted-build)"
       "      (ns-resolve 'gravity.build.invocation 'success-json)"
       "        (fn [_] {:json \"{\\\"status\\\":\\\"passed\\\"}\"})}"
       "     #(gravity.cli.launcher/-main"
       "       \"build\" \"--reader-inputs\" \"{}\"))"
       "   (prn {:called @called"
       "         :exit-code @exit-code"
       "         :bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
       "         :entrypoint-loaded (boolean (find-ns 'gravity.cli.entrypoint))"
       "         :cli-loaded (boolean (find-ns 'gravity.cli))}))"))

(defn- expected-presentation
  [argument packaged?]
  (if (#{"help" "--help" "-h"} argument)
    (str/trimr (cli/p18-cli-help-text packaged?))
    (pr-str (cli/p18-cli-version-record packaged?))))

(deftest presentation-aliases-are-bootstrap-free-in-fresh-jvms
  (doseq [packaged? [false true]
          argument presentation-aliases]
    (testing (str argument " packaged=" packaged?)
      (let [{:keys [exit err presentation marker]}
            (child-observation packaged? (presentation-expression argument))]
        (is (= 0 exit) (pr-str {:err err :marker marker}))
        (is (= (expected-presentation argument packaged?) presentation))
        (is (= {:bootstrap-loaded false
                :entrypoint-loaded false
                :cli-loaded true
                :packaged-jvm-cli? packaged?}
               marker))))))

(deftest usage-errors-are-bootstrap-free-and-retain-exit-code
  (doseq [arguments [[] ["unknown-command"]]]
    (testing (pr-str arguments)
      (let [{:keys [exit err marker]} (child-observation
                                      false (usage-expression arguments))]
        (is (= 0 exit) (pr-str {:err err :marker marker}))
        (is (= (str (cli/cli-usage-text) "\n")
               (without-known-java-tool-options-banner err)))
        (is (= {:exit-code 2
                :bootstrap-loaded false
                :entrypoint-loaded false
                :cli-loaded true}
               marker))))))

(deftest build-route-uses-standalone-invocation
  (let [bootstrap-before (find-ns 'gravity.bootstrap)
        called (atom nil)
        output (with-redefs [build-invocation/run-declared-input-build!
                             (fn [arguments]
                               (reset! called arguments)
                               :accepted-build)
                             build-invocation/success-json
                             (fn [artifact]
                               (is (= :accepted-build artifact))
                               {:json "{\"status\":\"passed\"}"})
                             clojure.core/shutdown-agents
                             (fn [])]
                 (with-out-str
                   (launcher/-main "build" "--reader-inputs" "{}")))
        bootstrap-after (find-ns 'gravity.bootstrap)]
    (is (= ["build" "--reader-inputs" "{}"] @called))
    (is (= "{\"status\":\"passed\"}\n" output))
    (is (identical? bootstrap-before bootstrap-after)))
  (let [{:keys [exit err presentation marker]}
        (child-observation
         false
         (build-expression))]
    (is (= 0 exit) (pr-str {:err err :marker marker}))
    (is (= "" (without-known-java-tool-options-banner err)))
    (is (= "{\"status\":\"passed\"}" presentation))
    (is (= {:called ["build" "--reader-inputs" "{}"]
            :exit-code nil
            :bootstrap-loaded false
            :entrypoint-loaded true
            :cli-loaded true}
           marker))))

(deftest non-presentation-arguments-forward-unchanged
  (let [expression
        (str "(require 'gravity.cli.launcher)"
             " (let [calls (atom nil)"
             "       bootstrap (create-ns 'gravity.bootstrap)"
             "       loaded-libs (var-get (resolve 'clojure.core/*loaded-libs*))]"
             "   (intern bootstrap '-main"
             "           (fn [& arguments] (reset! calls (vec arguments))))"
             "   (dosync (alter loaded-libs conj 'gravity.bootstrap))"
             "   (gravity.cli.launcher/-main \"read\""
             "                             \"payload.gravity\" \"--flag\")"
             "   (prn {:forwarded @calls"
             "         :bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
             "         :cli-loaded (boolean (find-ns 'gravity.cli))}))")
        {:keys [exit err marker]} (child-observation false expression)]
    (is (= 0 exit) (pr-str {:err err :marker marker}))
    (is (= {:forwarded ["read" "payload.gravity" "--flag"]
            :bootstrap-loaded true
            :cli-loaded true}
           marker))))
