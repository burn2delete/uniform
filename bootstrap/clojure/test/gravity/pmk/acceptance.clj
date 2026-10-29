(ns gravity.pmk.acceptance
  "Nonlaunching PMK selector and result attribution. Never loads an owner."
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def test-path "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj")
(def source-path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def test-ns "gravity.self-hosting.sh08-function-call-type-test")
(def closure-sha256 "b997383da2d2f5e61390f0bd089958201a039c2768233e774c7eafbfd8da6d38")
(def owner-closure-sha256 "bca67eb1d190b1f4007d6d2ee5e61ee7a3c0fe8e53d7ee16ec14d3a7ca23fbb7")
(def owner-paths
  {:c7 source-path
   :effects "bootstrap/gravity/src/gravity/effects.gravity"
   :emitter "bootstrap/gravity/p15_s23/emitter.gravity"})
(def owner-bridges
  {'invoke-c7 :c7 'return-premise-c7 :c7 'return-premise-effects :effects 'return-premise-emit :emitter
   'pr359-descriptor-c7 :c7 'pr359-descriptor-body :effects 'pr359-descriptor-emit :emitter})
(def helper-lookups #{'return-premise-helper 'pr359-descriptor-helper})
(def roots ["bootstrap/clojure/src" "bootstrap/gravity"
            "bootstrap/clojure/fixtures/self-hosting/sh-08"])
(def resource-files
  {"gravity/self_hosting/sh08_function_call_type_test.clj" test-path
   "gravity/self_hosting/sh09_body_effect_test.clj" "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj"
   "gravity/pmk/acceptance.clj" "bootstrap/clojure/test/gravity/pmk/acceptance.clj"
   "gravity/development_test_runner.clj" "bootstrap/clojure/test/gravity/development_test_runner.clj"})

(defn check! [condition reason]
  (when-not condition (throw (ex-info "PMK acceptance preparation rejected" {:reason reason}))))

(defn sha [bytes]
  (format "%064x" (java.math.BigInteger. 1
                   (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes))))

(defn read-bytes [root relative]
  (let [base (.toPath (.getCanonicalFile (io/file root)))
        path (.normalize (.resolve base relative))]
    (check! (.startsWith path base) :outside-root)
    (loop [p path]
      (when p
        (check! (not (java.nio.file.Files/isSymbolicLink p)) :symbolic-input)
        (recur (.getParent p))))
    (java.nio.file.Files/readAllBytes path)))

(defn capture
  "Read local bytes and classpath locations only; no Git/process/owner calls.
  Git bookends must be supplied separately by the caller, never inferred here."
  [root]
  (let [root (.getCanonicalFile (io/file root))
        files (sort (distinct
                     (concat ["deps.edn"] (vals resource-files)
                       (for [dir roots file (file-seq (io/file root dir)) :when (.isFile file)]
                         (str/replace (str (.relativize (.toPath root) (.toPath file)))
                                      java.io.File/separator "/")))))
        inputs (into (sorted-map) (for [p files] [p (sha (read-bytes root p))]))
        resources (into (sorted-map)
                    (concat resource-files
                      (for [p files :when (str/starts-with? p "bootstrap/clojure/src/")]
                        [(subs p (count "bootstrap/clojure/src/")) p])))
        locations (into (sorted-map)
                    (for [[r p] resources]
                      [r {:expected (.toURI (io/file root p))
                          :actual (mapv #(.toURI %) (enumeration-seq
                                     (.getResources (clojure.lang.RT/baseLoader) r)))}]))]
    {:root (.toURI root) :inputs inputs
     :inputs-sha256 (sha (.getBytes (pr-str inputs) "UTF-8"))
     :resource-set-sha256 (sha (.getBytes (pr-str (vec (keys locations))) "UTF-8"))
     :input-count (count inputs)
     :source-sha256 (get inputs source-path) :tests-sha256 (get inputs test-path)
     :runtime {:java-version (System/getProperty "java.version")
               :java-vendor (System/getProperty "java.vendor")
               :clojure-version (clojure-version)
               :jars (vec (for [p (str/split (System/getProperty "java.class.path")
                                                   (re-pattern java.io.File/pathSeparator))
                                      :let [f (io/file p)] :when (.isFile f)]
                                 [(.getName f) (sha (java.nio.file.Files/readAllBytes (.toPath f)))]))}
     :launch {:dev-test (get-in (edn/read-string (String. (read-bytes root "deps.edn") "UTF-8"))
                               [:aliases :dev-test])
              :classpath-roots
              (vec (for [p (str/split (System/getProperty "java.class.path")
                                      (re-pattern java.io.File/pathSeparator))
                         :let [f (.getCanonicalFile (io/file p))] :when (.isDirectory f)]
                     (do (check! (.startsWith (.toPath f) (.toPath root)) :external-classpath-root)
                         (str/replace (str (.relativize (.toPath root) (.toPath f)))
                                      java.io.File/separator "/"))))}
     :source (String. (read-bytes root source-path) "UTF-8")
     :owner-sources (into {} (for [[owner path] owner-paths]
                              [owner (String. (read-bytes root path) "UTF-8")]))
     :tests (String. (read-bytes root test-path) "UTF-8")
     :resources locations}))

(defn definitions [text]
  (binding [*read-eval* false *data-readers* {}
            *default-data-reader-fn* (fn [& _] (check! false :source-tag))]
    (with-open [r (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
      (loop [out {}]
        (let [f (read {:eof ::eof} r)]
          (if (= ::eof f) out
            (if (and (seq? f) (#{'defn 'defn- 'deftest} (first f)))
              (do (check! (not (contains? out (second f))) :duplicate-definition)
                  (recur (assoc out (second f) f)))
              (recur out))))))))

(defn calls [form]
  (letfn [(visit [x]
            (when (coll? x)
              (when-not (and (seq? x) (= 'quote (first x)))
                (concat (when (and (seq? x) (symbol? (first x))) [x])
                        (mapcat visit x)))))]
    (visit (drop 2 form))))

(defn helper-closure [defs start]
  (loop [pending [start] seen #{} refs #{}]
    (if-let [n (peek pending)]
      (if (contains? seen n) (recur (pop pending) seen refs)
        (let [sites (calls (get defs n))
              next-refs (set (map first sites))
              owners (for [site sites
                           :when (#{'invoke-c7 'return-premise-c7 'return-premise-effects 'return-premise-emit} (first site))
                           :let [quoted (second site)]
                           :when (and (seq? quoted) (= 'quote (first quoted)))]
                       (second quoted))]
          (recur (into (pop pending) (filter #(contains? defs %) next-refs))
                 (conj seen n) (into (into refs next-refs) owners))))
      refs)))

(defn oid? [s]
  (and (string? s) (boolean (re-matches #"[0-9a-f]{40}" s))))

(defn owner-contract-data [x]
  (let [order #(compare (pr-str %1) (pr-str %2))]
    (cond
      (map? x) (into (sorted-map-by order) (map (fn [[k v]] [(owner-contract-data k) (owner-contract-data v)]) x))
      (set? x) (into (sorted-set-by order) (map owner-contract-data x))
      (vector? x) (mapv owner-contract-data x)
      (seq? x) (apply list (map owner-contract-data x))
      :else x)))

(defn closure-digest [manifest]
  (sha (.getBytes (pr-str (if (:owner-contract manifest)
                           (owner-contract-data [(:selection manifest) (:consumer-edges manifest) (:owner-contract manifest)])
                           [(:selection manifest) (:consumer-edges manifest)])) "UTF-8")))

(defn owner-closure [defs start]
  (loop [pending [start] seen #{} owners #{}]
    (if-let [n (peek pending)]
      (if (contains? seen n) (recur (pop pending) seen owners)
        (let [sites (calls (get defs n))
              refs (for [site sites :when (contains? defs (first site))] (first site))
              lookups (for [site sites :when (helper-lookups (first site))
                            :let [q (second site)]
                            :when (and (seq? q) (= 'quote (first q)) (contains? defs (second q)))] (second q))
              reached (for [site sites
                            :let [applied? (= 'apply (first site))
                                  bridge (if applied? (second site) (first site))
                                  q (if applied? (nth site 2 nil) (second site))]
                            :when (and (contains? owner-bridges bridge) (seq? q) (= 'quote (first q)))]
                        [(get owner-bridges bridge) (second q)])]
          (recur (into (pop pending) (concat refs lookups)) (conj seen n) (into owners reached))))
      owners)))

(defn check-owner-contract! [manifest observation snapshot tests]
  (let [contract (:owner-contract manifest)]
    (check! (= :pmk-owner-preparation-v2 (:version contract)) :owner-contract-version)
    (check! (= (:predecessor contract) (:predecessor observation)) :request-predecessor)
    (check! (and (map? (:owner-sources snapshot))
                 (= (set (keys owner-paths)) (set (keys (:owner-sources snapshot))))
                 (every? string? (vals (:owner-sources snapshot)))) :owner-source-set)
    (doseq [[owner path] owner-paths]
      (check! (= (get-in contract [:source-pins path]) (get-in snapshot [:inputs path])
                 (sha (.getBytes ^String (get-in snapshot [:owner-sources owner]) "UTF-8"))) :owner-source-bytes))
    (doseq [[path digest] (:source-pins contract)]
      (check! (= digest (get-in snapshot [:inputs path])) :owner-input-pin))
    (doseq [[name digest] (:bridge-pins contract)]
      (check! (= digest (sha (.getBytes (pr-str (get tests name)) "UTF-8"))) :owner-bridge))
    (let [owners (into {} (for [[owner text] (:owner-sources snapshot)] [owner (definitions text)]))]
    (doseq [{:keys [selector apis owner-apis]} (:selection manifest)]
      (let [reached (owner-closure tests selector)]
        (check! (and (map? owner-apis) (seq owner-apis)
                     (= apis (set (mapcat val owner-apis)))) :owner-api-shape)
        (doseq [[owner names] owner-apis]
          (check! (and (contains? owners owner) (set? names) (seq names)
                       (every? #(and (contains? reached [owner %]) (contains? (get owners owner) %)) names)) :wrong-owner-consumer))))
    (doseq [[owner caller callee] (:consumer-edges contract)]
      (let [defs (get owners owner)]
        (check! (and (contains? defs caller) (contains? defs callee)
                     (some #(= callee (first %)) (calls (get defs caller)))) :wrong-owner-edge)))
    (check! (= (set (keys owner-paths)) (set (map first (:consumer-edges contract)))) :missing-owner-edge))))

(defn prepare [manifest observation snapshot]
  (let [before (:before observation) after (:after observation)
        tests (definitions (:tests snapshot)) source (definitions (:source snapshot))
        selection (:selection manifest)]
    (check! (and (= "ACC-04" (:task manifest))
                 (every? #(and (= #{:head :tree} (set (keys %))) (every? oid? (vals %)))
                         [(:source-revision manifest) (:main manifest) before after])) :identity)
    (check! (and (= #{:head :tree} (set (keys before))) (= before after)) :checkout-bookends)
    (check! (= (:source-revision manifest) (:source-revision observation)) :source-identity)
    (check! (= (:main manifest) (:main observation)) :main-identity)
    (check! (and (map? (:inputs snapshot)) (seq (:inputs snapshot))
                 (every? (fn [[p h]] (and (string? p) (string? h)
                                          (boolean (re-matches #"[0-9a-f]{64}" h))))
                         (:inputs snapshot))) :input-shape)
    (check! (= (:inputs-sha256 manifest) (:inputs-sha256 snapshot)
               (sha (.getBytes (pr-str (into (sorted-map) (:inputs snapshot))) "UTF-8"))) :dependency-bytes)
    (check! (= (:input-count manifest) (:input-count snapshot) (count (:inputs snapshot))) :input-count)
    (check! (and (= (:source-sha256 manifest) (get-in snapshot [:inputs source-path]))
                 (= (:tests-sha256 manifest) (get-in snapshot [:inputs test-path]))) :source-inventory)
    (check! (= (:source-sha256 manifest) (sha (.getBytes ^String (:source snapshot) "UTF-8"))) :source-bytes)
    (check! (= (:tests-sha256 manifest) (sha (.getBytes ^String (:tests snapshot) "UTF-8"))) :test-bytes)
    (check! (and (seq (:runtime manifest)) (= (:runtime manifest) (:runtime snapshot))) :runtime-identity)
    (check! (and (= (:launch manifest) (:launch snapshot))
                 (= ["-m" "gravity.development-test-runner"] (get-in snapshot [:launch :dev-test :main-opts]))
                 (= ["bootstrap/clojure/src" "bootstrap/clojure/test"] (get-in snapshot [:launch :classpath-roots]))) :launch-identity)
    (check! (and (instance? java.net.URI (:root observation))
                 (= "file" (.getScheme ^java.net.URI (:root observation)))
                 (= (:root observation) (:root snapshot))) :resource-root)
    (check! (seq (:resources snapshot)) :missing-resources)
    (check! (= (:resource-set-sha256 manifest)
               (sha (.getBytes (pr-str (vec (sort (keys (:resources snapshot))))) "UTF-8"))) :resource-set)
    (doseq [[resource {:keys [expected actual]}] (:resources snapshot)]
      (let [path (get resource-files resource (str "bootstrap/clojure/src/" resource))]
        (check! (contains? (:inputs snapshot) path) :resource-input-missing)
        (check! (= (.resolve ^java.net.URI (:root observation) path) expected) :resource-path)
        (check! (= [expected] actual) :resource-shadow-or-mismatch)))
    (check! (= (if (:owner-contract manifest) owner-closure-sha256 closure-sha256)
               (closure-digest manifest)) :closure-contract)
    (check! (= (list 'ns (symbol test-ns))
               (binding [*read-eval* false]
                 (take 2 (read-string (:tests snapshot))))) :test-namespace)
    (check! (and (vector? selection) (seq selection)
                 (= (count selection) (count (set (map :selector selection))))) :selection)
    (doseq [{:keys [selector apis]} selection]
      (check! (= 'deftest (first (get tests selector))) :wrong-selector)
      (when-not (:owner-contract manifest)
        (check! (and (set? apis) (seq apis)
                     (every? (helper-closure tests selector) apis)) :wrong-test-consumer)))
    (when (:owner-contract manifest)
      (check-owner-contract! manifest observation snapshot tests))
    (check! (seq (:consumer-edges manifest)) :missing-consumers)
    (doseq [[caller callee] (:consumer-edges manifest)]
      (check! (and (contains? source caller) (contains? source callee)
                   (some #(= callee (first %)) (calls (get source caller)))) :wrong-production-consumer))
    {:schema :pmk-acceptance-plan-v1 :task "ACC-04" :execution :unrun
     :source-revision (:source-revision manifest) :main (:main manifest)
     :root (str (:root observation)) :resource-set-sha256 (:resource-set-sha256 manifest)
     :checkout before :inputs-sha256 (:inputs-sha256 snapshot)
     :runtime (:runtime snapshot)
     :launch (:launch snapshot) :closure (:selection manifest)
     :selection (mapv #(str test-ns "/" (:selector %)) selection)
     :consumer-edges (:consumer-edges manifest)
     :owner-contract (:owner-contract manifest)
     :argv (into ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace" test-ns]
                 (mapcat #(vector "--exact" (str (:selector %))) selection))}))

(defn attribute
  "Check a future normal runner sink result against source bookends and selectors.
  This validates attribution only; it does not authenticate the reported run."
  [plan after-plan result]
  (check! (= :pmk-acceptance-plan-v1 (:schema plan)) :plan-schema)
  (check! (= plan after-plan) :source-drift)
  (check! (= :gravity/development-test-result-v1 (:schema result)) :runner-schema)
  (check! (and (= :non-authoritative (:authority result))
               (false? (:authoritative? result))) :runner-authority)
  (check! (= (:selection plan) (get-in result [:selection :test-vars])) :result-selection)
  (check! (= [test-ns] (get-in result [:selection :namespaces])) :result-namespace)
  (check! (false? (get-in result [:selection :fail-fast?])) :result-invocation)
  (let [{:keys [test pass fail error]} (:summary result)]
    (check! (every? #(and (integer? %) (<= 0 %)) [test pass fail error]) :result-counts)
    (let [passed? (and (= test (count (:selection plan))) (zero? fail) (zero? error))]
      (check! (and (= (if passed? :passed :failed) (:outcome result))
                   (= (if passed? 0 1) (:exit-code result))) :misattributed-result))
    (cond-> {:schema :pmk-attributed-run-v1 :authenticated? false
     :source-revision (:source-revision plan) :checkout (:checkout plan)
     :inputs-sha256 (:inputs-sha256 plan) :selection (:selection plan)
     :reported-result result :semantic-acceptance :not-established}
      (:owner-contract plan) (assoc :owner-contract (:owner-contract plan) :closure (:closure plan)))))
