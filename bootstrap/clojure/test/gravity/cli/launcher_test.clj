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

(defn- presentation-expression
  [argument]
  (str "(require 'gravity.cli.launcher)"
       " (gravity.cli.launcher/-main " (pr-str argument) ")"
       " (prn {:bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
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
                :cli-loaded true
                :packaged-jvm-cli? packaged?}
               marker))))))

(deftest usage-errors-are-bootstrap-free-and-retain-exit-code
  (doseq [arguments [[] ["unknown-command"]]]
    (testing (pr-str arguments)
      (let [{:keys [exit err marker]} (child-observation
                                      false (usage-expression arguments))]
        (is (= 0 exit) (pr-str {:err err :marker marker}))
        (is (= (str (cli/cli-usage-text) "\n") err))
        (is (= {:exit-code 2
                :bootstrap-loaded false
                :cli-loaded true}
               marker))))))

(deftest build-route-uses-standalone-invocation
  (let [called (atom nil)
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
                   (launcher/-main "build" "--reader-inputs" "{}")))]
    (is (= ["build" "--reader-inputs" "{}"] @called))
    (is (= "{\"status\":\"passed\"}\n" output))
    (is (nil? (find-ns 'gravity.bootstrap))))
  (let [{:keys [exit err marker]}
        (child-observation
         false
         (str "(require 'gravity.cli.launcher)"
              " (let [exit-code (atom nil)]"
              "   (with-redefs-fn {(ns-resolve 'gravity.cli.launcher 'exit!)"
              "                      (fn [status] (reset! exit-code status))}"
              "     #(gravity.cli.launcher/-main \"build\" \"--bad\"))"
              "   (prn {:exit-code @exit-code"
              "         :bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
              "         :cli-loaded (boolean (find-ns 'gravity.cli))}))"))]
    (is (= 0 exit) (pr-str {:err err :marker marker}))
    (is (str/includes? err "\"code\":\"T1006\""))
    (is (= {:exit-code 2
            :bootstrap-loaded false
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
