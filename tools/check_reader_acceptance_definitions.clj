;; SOURCE/DATA only. Never require or evaluate the acceptance namespace.
(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.walk :as walk]
         '[clojure.string :as str]
         '[clojure.test :refer [deftest is run-tests]])

(def acceptance-root "bootstrap/clojure/acceptance")
(def acceptance-path (str acceptance-root "/gravity/reader/declared_input_acceptance_test.clj"))
(defn read-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader. (io/reader path))]
      (loop [forms []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) forms (recur (conj forms form))))))))
(def forms (read-forms acceptance-path))
(defn nodes [forms] (tree-seq coll? seq forms))
(defn definition [forms name]
  (first (filter #(and (seq? %) (contains? #{'defn- 'defn 'deftest} (first %))
                       (= name (second %))) forms)))
(defn calls [forms name]
  (filter #(and (seq? %) (= name (first %))) (nodes forms)))
(defn normal-route? [forms]
  (let [symbols (set (filter symbol? (nodes forms)))
        observer (definition forms 'normal-build!)]
    (and (seq (calls observer 'invocation/run-declared-input-build!))
         (seq (calls forms 'entrypoint/run!))
         (seq (calls observer 'apply))
         (some #{'(apply consume arguments)} (nodes observer))
         (some #{'(apply consume (assoc (vec arguments) 2 candidate))} (nodes observer))
         (= 1 (count (calls forms 'with-redefs-fn)))
         (not-any? #(re-find #"call-with-authorized-inputs|reader-declared-input-build!|read-input!|with-redefs$" (str %)) symbols))))
(defn excluded? [deps]
  (not-any? #(and (string? %) (or (= acceptance-root %) (= "bootstrap/clojure" %)))
            (concat (:paths deps) (mapcat #(concat (:paths %) (:extra-paths %) (:replace-paths %))
                                          (vals (:aliases deps))))))
(defn fixture-forms [text]
  (binding [*read-eval* false *data-readers* {'resource #(vector :resource %)
                                           'unknown #(vector :unknown %)
                                           'text #(vector :text %)}]
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop [result []]
        (let [v (read {:eof ::end} r)]
          (if (= ::end v) result (recur (conj result v))))))))

(deftest loader-and-real-entry-source-contract
  (let [deps (edn/read-string (slurp "deps.edn"))]
    (is (excluded? deps))
    (is (not (excluded? (update deps :paths conj acceptance-root)))))
  (is (normal-route? forms))
  (is (not (normal-route? (walk/postwalk-replace
                           {'invocation/run-declared-input-build! 'bootstrap/reader-declared-input-build!} forms))))
  (is (not (normal-route? (conj forms '(inputs/call-with-authorized-inputs grant inputs run)))))
  (is (not (normal-route? (walk/postwalk-replace {'(apply consume arguments) 'artifact} forms))))
  (is (= #{'normal-granted-pair-and-distinct-same-key-occurrences
           'normal-live-verifier-refuses-tampered-duplicate-missing-and-stale-records
           'normal-complete-population-and-policy-refusals
           'normal-cli-json-is-the-real-command-projection}
         (set (map second (filter #(and (seq? %) (= 'deftest (first %))) forms)))))
  (doseq [path ["bootstrap/clojure/test/gravity/development_test_runner.clj"
                "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj"
                "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"]]
    (is (not (str/includes? (slurp path) "gravity.reader.declared-input-acceptance-test")))))

(deftest fixture-pairs-and-whole-population-data
  (doseq [stem ["granted" "disallowed" "undeclared-effect" "unknown" "mixed-effect" "mixed-unknown"]]
    (let [base (str "bootstrap/clojure/fixtures/reader-extension-binding/" stem)
          qst (slurp (str base ".qst")) gravity (slurp (str base ".gravity"))]
      (is (= qst gravity))
      (is (not= qst (str gravity " ")))))
  (let [root "bootstrap/clojure/fixtures/reader-extension-binding/"
        granted (slurp (str root "granted.gravity")) unknown (slurp (str root "unknown.gravity"))
        expected [:resource "bootstrap/clojure/fixtures/accepted/reader-extension-input.txt"]
        mixed (fixture-forms (str granted unknown))]
    (is (= [expected] (fixture-forms granted)))
    (is (= [expected expected] (fixture-forms (str granted granted))))
    (is (= [:resource :unknown] (mapv first mixed)))
    (is (not= mixed (fixture-forms granted))))
  (let [source (slurp "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00813-sh03-reader-canonical-identity.clj")
        constructor (subs source (.indexOf source "(defn reader-declared-input-build!"))]
    (is (< (.indexOf constructor "prepared (reader-resource-prepare!")
           (.indexOf constructor "requests (reader-resource-requests!")
           (.indexOf constructor "call-with-scope")))))

(let [report (run-tests)]
  (when (pos? (+ (:fail report) (:error report))) (System/exit 1)))
