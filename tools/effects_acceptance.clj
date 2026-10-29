(ns effects-acceptance
  "Nonlaunching source/selector binding for the finite Effects aggregate change.
  Reads Git and source data only. Never loads a compiler or accepts semantics."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def test-namespace "gravity.self-hosting.sh09-body-effect-test")
(def test-path "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj")
(def effects-path "bootstrap/gravity/src/gravity/effects.gravity")
(def consumer-path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
(def selectors
  ["body-effect-public-forwarding-source-boundary"
   "body-module-vectors-check-caller-and-every-producer"
   "body-module-vectors-have-no-cumulative-quota"
   "body-private-map-keys-authenticated-builder-composition"
   "body-private-map-keys-development-kernel"])
(def consumers
  {'build-body-effect-result 3 'verify-body-effect-result 4
   'sh09-build-authenticated-body-effect-result 3
   'sh09-verify-authenticated-body-effect-result 4})
(defn command []
  (into ["clojure" "-M:dev-test" "--namespace" test-namespace]
        (mapcat #(vector "--exact" %) selectors)))
(def resource-paths
  [effects-path test-path consumer-path
   "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
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
  (throw (ex-info "Effects acceptance binding rejected" {:reason reason})))
(defn require! [ok reason] (when-not ok (reject! reason)))
(defn sha256 [text]
  (format "%064x" (java.math.BigInteger. 1
    (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes text "UTF-8")))))
(defn forms [text]
  (binding [*read-eval* false *data-readers* {} *default-data-reader-fn* (fn [& _] (reject! :tagged-source))]
    (with-open [r (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
      (loop [out []]
        (let [form (read {:eof ::eof} r)]
          (if (= ::eof form) out (recur (conj out form))))))))
(defn definitions [text kind]
  (into {} (for [f (forms text) :when (and (seq? f) (= kind (first f)))] [(second f) f])))
(defn git-output [root & args]
  (let [{:keys [exit out]} (apply shell/sh "git" "-C" (str root) args)]
    (require! (zero? exit) :git-read)
    out))
(defn git [root & args] (str/trim (apply git-output root args)))
(defn snapshot [root main]
  (require! (boolean (re-matches #"[0-9a-f]{40}" main)) :main-identity)
  (require! (= main (git root "rev-parse" (str main "^{commit}"))) :main-identity)
  (require! (empty? (apply git root "status" "--porcelain=v1" "--untracked-files=all" "--" dependency-roots))
            :dirty-dependency)
  {:candidate {:commit (git root "rev-parse" "HEAD") :tree (git root "rev-parse" "HEAD^{tree}")}
   :main {:commit main :tree (git root "rev-parse" (str main "^{tree}"))}
   :dependency-trees (into (sorted-map)
                      (for [p dependency-roots] [p (git root "rev-parse" (str "HEAD:" p))]))
   :resources (into (sorted-map)
                (for [p resource-paths
                      :let [file (io/file root p)
                            _ (require! (.isFile file) :missing-resource)
                            text (slurp file)
                            committed (git-output root "show" (str "HEAD:" p))]]
                  (do (require! (= text committed) :uncommitted-resource)
                      [p (sha256 text)])))})

(defn validate-identity! [expected]
  (require! (= #{:candidate :main :dependency-trees :resources} (set (keys expected))) :missing-identity)
  (doseq [key [:candidate :main]]
    (require! (= #{:commit :tree} (set (keys (get expected key)))) :missing-identity)
    (doseq [value (vals (get expected key))]
      (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))) :missing-identity)))
  (require! (= (set dependency-roots) (set (keys (:dependency-trees expected)))) :missing-identity)
  (doseq [value (vals (:dependency-trees expected))]
    (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))) :missing-identity))
  (require! (= (set resource-paths) (set (keys (:resources expected)))) :resource-identity)
  (doseq [value (vals (:resources expected))]
    (require! (and (string? value) (boolean (re-matches #"[0-9a-f]{64}" value))) :resource-identity))
  expected)

(defn prepare
  "Validate an exact observed snapshot and source routes, without requiring tests.
  read-text is an inspection seam; it must return the actual bytes of each resource."
  [expected observed read-text]
  (validate-identity! expected)
  (require! (= expected observed) :source-identity)
  (doseq [p resource-paths]
    (require! (= (get-in expected [:resources p]) (sha256 (read-text p))) :resource-identity))
  (let [tests (definitions (read-text test-path) 'deftest)
        effects (definitions (read-text effects-path) 'defn)
        public (definitions (read-text consumer-path) 'defn)]
    (require! (= (symbol test-namespace) (second (first (forms (read-text test-path))))) :namespace)
    (doseq [s selectors] (require! (contains? tests (symbol s)) :missing-selector))
    (doseq [[name arity] consumers]
      (require! (= arity (count (nth (get (merge effects public) name) 2 nil))) :consumer-arity))
    (require! (= '(defn sh09-build-authenticated-body-effect-result [typed verification binding]
                   (effects/build-body-effect-result typed verification binding))
                 (get public 'sh09-build-authenticated-body-effect-result)) :consumer-route)
    (require! (= '(defn sh09-verify-authenticated-body-effect-result [typed verification binding candidate]
                   (effects/verify-body-effect-result typed verification binding candidate))
                 (get public 'sh09-verify-authenticated-body-effect-result)) :consumer-route)
    (let [nodes (tree-seq coll? seq (get effects 'build-body-effect-result))
          calls (filter #(and (seq? %) (= 'body-module-vectors-valid? (first %))) nodes)
          branches (filter #(and (seq? %) (= 'if (first %)) (= (first calls) (second %))) nodes)]
      (require! (= ['(body-module-vectors-valid? core facts (get producers :entries))] (vec calls)) :builder-route)
      (require! (= 1 (count branches)) :builder-route)
      (require! (= '(body-error typed :malformed-body-module-vectors {}) (last (first branches))) :builder-diagnostic))
    {:schema :effects/aggregate-selection-v1 :authority :none :identity expected
     :namespace test-namespace :selectors selectors :consumers consumers
     :command (command)
     :execution :unrun-held}))

(defn bind-result
  "Bind an actual future runner result to the plan and before/after snapshots.
  No execution occurs here. A caller must capture the machine sink during run-cli!;
  no sink (namespace load/fixture/driver failure) is a driver error, not a test refusal."
  [plan before after result]
  (require! (= :effects/aggregate-selection-v1 (:schema plan)) :plan)
  (require! (= [:none :unrun-held (command)] ((juxt :authority :execution :command) plan)) :plan)
  (require! (= test-namespace (:namespace plan)) :namespace)
  (require! (= selectors (:selectors plan)) :selector)
  (require! (= consumers (:consumers plan)) :consumer)
  (validate-identity! (:identity plan))
  (require! (= (:identity plan) before after) :source-identity)
  (require! (some? result) :missing-runner-result)
  (require! (= :gravity/development-test-result-v1 (:schema result)) :runner-result)
  (require! (= [:non-authoritative false] ((juxt :authority :authoritative?) result)) :runner-result)
  (require! (= {:namespaces [test-namespace]
                :test-vars (mapv #(str test-namespace "/" %) selectors) :fail-fast? false}
               (:selection result)) :selector)
  (let [{:keys [test pass fail error]} (:summary result)]
    (require! (every? #(and (integer? %) (<= 0 %)) [test pass fail error]) :runner-counts)
    (require! (= test (count selectors)) :incomplete-execution)
    (require! (pos? (+ pass fail error)) :incomplete-execution)
    (let [passed? (zero? (+ fail error))]
      (require! (= [(if passed? :passed :failed) (if passed? 0 1)]
                   [(:outcome result) (:exit-code result)]) :misattributed-result)
      {:schema :effects/aggregate-observation-v1 :authority :non-authoritative
       :identity before :runner result :outcome (if passed? :passed :failed)
       :semantic-acceptance false})))

(defn -main [& args]
  (try
    (if (and (= 3 (count args)) (= "--snapshot" (second args)))
      (let [[root _ main] args] (prn (snapshot root main)))
      (do
        (require! (= 2 (count args)) :usage)
        (let [[root manifest] args expected (edn/read-string (slurp manifest))]
          (prn (prepare expected (snapshot root (get-in expected [:main :commit]))
                        #(slurp (io/file root %)))))))
    (finally (shutdown-agents))))

