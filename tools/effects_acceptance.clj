(ns effects-acceptance
  "Read-only current-main source and runner-result binding for PR533.

  This adapter validates source bytes and exact development-test receipts. It
  launches no compiler, process, broker or native operation and grants no
  semantic or migration authority."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def test-namespace "gravity.self-hosting.sh09-body-effect-test")
(def test-path "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj")
(def effects-path "language/compiler/src/gravity/effects.gravity")
(def consumer-path "language/compiler/src/gravity/compiler/c8_effect_checker_engine.gravity")
(def selectors
  ["body-private-map-keys-authenticated-builder-composition"
   "body-module-vectors-have-no-cumulative-quota"
   "body-module-vectors-check-caller-and-every-producer"])
(def pmk-selectors ["body-private-map-keys-authenticated-builder-composition"])
(def accepted-selections [selectors pmk-selectors])
(def consumers
  {'build-body-effect-result 3
   'verify-body-effect-result 4
   'sh09-build-authenticated-body-effect-result 3
   'sh09-verify-authenticated-body-effect-result 4})

(def resource-paths
  [effects-path test-path consumer-path
   "language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity"
   "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj"
   "bootstrap/clojure/test/gravity/development_test_runner.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
   "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-private-map-keys.gravity"
   "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-private-map-keys.qst"
   "deps.edn"])
(def dependency-roots
  ["bootstrap/clojure/src" "bootstrap/clojure/test" "bootstrap/clojure/fixtures"
   "bootstrap/gravity" "contracts" "tools"])

(defn reject! [reason]
  (throw (ex-info "Effects current acceptance binding rejected" {:reason reason})))

(defn require! [condition reason]
  (when-not condition (reject! reason)))

(defn sha256 [text]
  (format "%064x"
          (java.math.BigInteger.
           1
           (.digest (java.security.MessageDigest/getInstance "SHA-256")
                    (.getBytes ^String text "UTF-8")))))

(defn forms [text]
  (binding [*read-eval* false
            *data-readers* {}
            *default-data-reader-fn* (fn [& _] (reject! :tagged-source))]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (java.io.StringReader. text))]
      (loop [out []]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form) out (recur (conj out form))))))))

(defn definitions [text kind]
  (into {}
        (for [form (forms text)
              :when (and (seq? form) (= kind (first form)))]
          [(second form) form])))

(defn command
  ([] (command selectors))
  ([selected]
   (require! (some #{selected} accepted-selections) :selector)
   (into ["clojure" "-M:dev-test" "--namespace" test-namespace]
         (mapcat #(vector "--exact" %) selected))))

(defn git-output [root & args]
  (let [{:keys [exit out err]} (apply shell/sh "git" "-C" (str root) args)]
    (require! (zero? exit) [:git-read (vec args) (str err)])
    out))

(defn git [root & args]
  (str/trim (apply git-output root args)))

(defn snapshot
  "Capture exact source/resource identities without loading the compiler."
  [root main]
  (require! (and (string? main) (re-matches #"[0-9a-f]{40}" main)) :main-identity)
  (require! (= main (git root "rev-parse" (str main "^{commit}"))) :main-identity)
  (require! (str/blank? (apply git-output root "status" "--porcelain=v1"
                                "--untracked-files=all" "--" dependency-roots))
            :dirty-dependency)
  {:candidate {:commit (git root "rev-parse" "HEAD")
               :tree (git root "rev-parse" "HEAD^{tree}")}
   :main {:commit main
          :tree (git root "rev-parse" (str main "^{tree}"))}
   :dependency-trees
   (into (sorted-map)
         (for [path dependency-roots]
           [path (git root "rev-parse" (str "HEAD:" path))]))
   :resources
   (into (sorted-map)
         (for [path resource-paths
               :let [file (io/file root path)
                     _ (require! (.isFile file) [:missing-resource path])
                     text (slurp file)
                     committed (git-output root "show" (str "HEAD:" path))]]
           (do
             (require! (= text committed) [:uncommitted-resource path])
             [path (sha256 text)])))})

(defn validate-identity! [identity]
  (require! (= #{:candidate :main :dependency-trees :resources}
               (set (keys identity)))
            :missing-identity)
  (doseq [key [:candidate :main]]
    (require! (= #{:commit :tree} (set (keys (get identity key)))) :missing-identity)
    (doseq [value (vals (get identity key))]
      (require! (and (string? value) (re-matches #"[0-9a-f]{40}" value))
                :missing-identity)))
  (require! (= (set dependency-roots) (set (keys (:dependency-trees identity))))
            :missing-identity)
  (doseq [value (vals (:dependency-trees identity))]
    (require! (and (string? value) (re-matches #"[0-9a-f]{40}" value))
              :missing-identity))
  (require! (= (set resource-paths) (set (keys (:resources identity))))
            :resource-identity)
  (doseq [value (vals (:resources identity))]
    (require! (and (string? value) (re-matches #"[0-9a-f]{64}" value))
              :resource-identity))
  identity)

(defn- nodes [form]
  (tree-seq coll? seq form))

(defn- contains-call? [form symbol]
  (some #(and (seq? %) (= symbol (first %))) (nodes form)))

(defn- contains-symbol? [form symbol]
  (some #{symbol} (nodes form)))

(defn- public-routes! [public]
  (require! (= '(defn sh09-build-authenticated-body-effect-result
                  [typed verification binding]
                  (effects/build-body-effect-result typed verification binding))
                (get public 'sh09-build-authenticated-body-effect-result))
            :consumer-route)
  (require! (= '(defn sh09-verify-authenticated-body-effect-result
                  [typed verification binding candidate]
                  (effects/verify-body-effect-result typed verification binding candidate))
                (get public 'sh09-verify-authenticated-body-effect-result))
            :consumer-route))

(defn- current-effects-route! [effects]
  (let [builder (get effects 'build-body-effect-result)
        builder-text (pr-str builder)]
    (require! (contains-call? builder 'body-analyze-producers) :builder-route)
    (require! (not (contains-call? builder 'body-module-vectors-valid?))
              :obsolete-builder-route)
    (require! (not-any? #(str/includes? builder-text %)
                        ["body-aggregate-valid?"
                         "body-analyze-producers-work"
                         "body-deferred-work-limit"
                         ":aggregate-body-node-or-function-limit"
                         ":malformed-body-module-vectors"])
              :obsolete-accounting-route)))

(defn- current-pmk-route! [tests]
  (let [pmk (get tests 'body-private-map-keys-authenticated-builder-composition)]
    (require! (contains-call? pmk 'compile-c8-module-bundle)
              :missing-public-composition)
    (doseq [symbol ['sh09-build-authenticated-body-effect-result
                    'sh09-verify-authenticated-body-effect-result]]
      (require! (contains-symbol? pmk symbol) [:missing-public-composition symbol]))))

(defn prepare
  "Bind current source bytes and routes without executing any selector."
  ([expected observed read-text]
   (prepare expected observed read-text selectors))
  ([expected observed read-text selected]
   (validate-identity! expected)
   (require! (= expected observed) :source-identity)
   (require! (some #{selected} accepted-selections) :selector)
   (doseq [path resource-paths]
     (require! (= (get-in expected [:resources path])
                  (sha256 (read-text path)))
               [:resource-identity path]))
   (let [tests (definitions (read-text test-path) 'deftest)
         effects (definitions (read-text effects-path) 'defn)
         public (definitions (read-text consumer-path) 'defn)]
     (require! (= (symbol test-namespace)
                  (second (first (forms (read-text test-path)))))
               :namespace)
     (doseq [selector selected]
       (require! (contains? tests (symbol selector)) [:missing-selector selector]))
     (current-pmk-route! tests)
     (public-routes! public)
     (current-effects-route! effects)
     (require! (= consumers
                  (into {}
                        (for [[name _] consumers]
                          [name (count (nth (get (merge effects public) name) 2))])))
               :consumer-arity)
     {:schema :effects/current-selection-v1
      :authority :none
      :execution :unrun
      :identity expected
      :namespace test-namespace
      :selectors selected
      :consumers consumers
      :command (command selected)
      :source-drift-policy :before-after-identities-must-match})))

(defn prepare-pmk [expected observed read-text]
  (prepare expected observed read-text pmk-selectors))

(defn bind-result
  "Bind an actual development runner receipt to one prepared selection.
  The returned observation remains non-authoritative."
  [plan before after result]
  (require! (= :effects/current-selection-v1 (:schema plan)) :plan)
  (require! (= [:none :unrun (command (:selectors plan))]
               ((juxt :authority :execution :command) plan))
            :plan)
  (require! (= test-namespace (:namespace plan)) :namespace)
  (require! (= consumers (:consumers plan)) :consumer)
  (validate-identity! (:identity plan))
  (require! (= (:identity plan) before after) :source-identity)
  (require! (some? result) :missing-runner-result)
  (require! (= :gravity/development-test-result-v1 (:schema result)) :runner-result)
  (require! (= [:non-authoritative false]
               ((juxt :authority :authoritative?) result))
            :runner-result)
  (require! (= {:namespaces [test-namespace]
                :test-vars (mapv #(str test-namespace "/" %) (:selectors plan))
                :fail-fast? false}
               (:selection result))
            :selector)
  (let [{:keys [test pass fail error]} (:summary result)]
    (require! (every? #(and (integer? %) (<= 0 %)) [test pass fail error])
              :runner-counts)
    (require! (and (= test (count (:selectors plan)))
                   (= test (+ pass fail error)))
              :incomplete-execution)
    (let [passed? (zero? (+ fail error))]
      (require! (= [(if passed? :passed :failed) (if passed? 0 1)]
                   [(:outcome result) (:exit-code result)])
                :misattributed-result)
      {:schema :effects/current-observation-v1
       :authority :non-authoritative
       :semantic-acceptance false
       :identity before
       :runner result
       :outcome (if passed? :passed :failed)})))

(defn -main [& _]
  (println "effects-acceptance is a read-only adapter; call snapshot and prepare explicitly"))
