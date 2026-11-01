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
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.reader.declared-input-acceptance-test)))
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
  (doseq [stem ["granted" "disallowed" "undeclared-effect" "unknown" "mixed-effect" "mixed-unknown"
                "normal-pure" "normal-unknown" "normal-effect"
                "repeated-granted" "resource-unknown" "resource-outside-grant" "changed-source"]]
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
  (doseq [extension [".qst" ".gravity"]]
    (let [root "bootstrap/clojure/fixtures/reader-extension-binding/"
          text #(slurp (str root % extension))
          granted (text "granted")]
      (is (= (str granted granted) (text "repeated-granted")))
      (is (= (str granted (text "unknown")) (text "resource-unknown")))
      (is (= (conj (fixture-forms granted) [:resource "outside-explicit-grant"])
             (fixture-forms (text "resource-outside-grant"))))
      (is (not= granted (text "changed-source")))
      (is (= (fixture-forms granted) (fixture-forms (text "changed-source"))))))
  (let [source (slurp "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00813-sh03-reader-canonical-identity.clj")
        constructor (subs source (.indexOf source "(defn reader-declared-input-build!"))]
    (is (< (.indexOf constructor "prepared (reader-resource-prepare!")
           (.indexOf constructor "requests (reader-resource-requests!")
           (.indexOf constructor "call-with-scope")))))

(defn tamper-and-grant-definitions? [forms]
  (let [tamper (definition forms 'normal-live-verifier-refuses-tampered-duplicate-missing-and-stale-records)
        refusals (definition forms 'normal-complete-population-and-policy-refusals)
        paths (set (map #(nth % 2) (calls tamper 'assoc-in)))
        base [:reader-extension-invocation-records 0 :invocations 0]]
    (and (every? paths [[:source-unit-record :bytes-hash]
                        (conj base :input-seed :datum)
                        (conj base :returned-seed :datum)
                        (conj base :invocation :request :input)
                        (conj base :invocation :request :source-content-hash)])
         (some #{'(assoc artifact :artifact-id "sha256:stale")} (nodes tamper))
         (some #{'(fixture "changed-source" extension)} (nodes tamper))
         (some #{'(command source [] requirements)} (nodes refusals))
         (some #{'(= "--allow-read" flag)} (nodes refusals))
         (seq (calls refusals 'remove)))))

(deftest explicit-original-input-and-permission-definitions
  (is (tamper-and-grant-definitions? forms))
  (doseq [[original replacement]
          [[':bytes-hash ':unrelated]
           ['(fixture "changed-source" extension) '(fixture "granted" extension)]
           ['(= "--allow-read" flag) '(= "--unrelated" flag)]
           ['(command source [] requirements) '(command source inputs requirements)]
           ['(assoc artifact :artifact-id "sha256:stale") 'artifact]]]
    (is (not (tamper-and-grant-definitions? (walk/postwalk-replace {original replacement} forms)))))
  ;; Read conserved genuine pure definitions as DATA; never require their ns.
  (let [pure (read-forms "bootstrap/clojure/test/gravity/reader/sealed_pure_integration_test.clj")
        positive (definition pure 'normal-gravity-pure-text-pair)
        negative (definition pure 'normal-gravity-pure-text-mixed-refusals)]
    (is (seq (calls positive 'bootstrap/reader-pure-source-artifact)))
    (is (seq (calls positive 'bootstrap/c3-reader-artifact-view)))
    (is (some #{'[".qst" ".gravity"]} (nodes positive)))
    (is (some #{'(:handler record)} (nodes positive)))
    (is (some #{'(pure/policy-id)} (nodes positive)))
    (is (some #{'["normal-unknown" "normal-effect"]} (nodes negative)))
    (is (some #{"C2-EXTENSION"} (nodes negative)))))

(defn inert-observer-reports [helper visits false-guard?]
  ;; Evaluate only the extracted observer closure and its post-command check
  ;; over core DATA. Never evaluate the command, redefinitions, project Var,
  ;; namespace, constructor, consumer, provider or any acceptance test body.
  (let [observer (first (vals (second (first (calls helper 'with-redefs-fn)))))
        guard (first (calls observer 'when))
        observer (if false-guard?
                   (walk/postwalk-replace {guard (cons 'when (cons false (nnext guard)))} observer)
                   observer)
        ;; Only direct forms in the outer let body can protect command return.
        postcheck (first (filter #(and (seq? %) (= 'is (first %))) (drop 2 (nth helper 3))))
        make-observer (eval (list 'fn '[consume inspect-live observations resource-artifact?] observer))
        check-observations (eval (list 'fn '[observations] (or postcheck nil)))
        resource-shape (definition forms 'resource-artifact?)
        resource? (eval (cons 'fn (drop 2 resource-shape)))
        observations (atom 0) inspections (atom 0) forwards (atom []) reports (atom [])
        product {:reader-extension-invocation-records [{:schema-version 3}]}
        observer (make-observer (fn [& args] (swap! forwards conj args) product)
                                (fn [_ _] (swap! inspections inc)) observations resource?)]
    (with-redefs [clojure.test/report #(swap! reports conj %)]
      (dotimes [_ visits] (observer :core-authority :core-path product :core-context))
      (check-observations observations))
    {:failures (count (filter #(= :fail (:type %)) @reports))
     :inspections @inspections :observations @observations :forwards (count @forwards)}))

(deftest live-observer-cannot-silently-skip-or-duplicate
  (let [helper (definition forms 'normal-build!)
        ;; Remove precisely the new direct post-command check to model F1.
        old (walk/postwalk-replace {'(is (= 1 @observations)) nil} helper)]
    (is (= {:failures 0 :inspections 0 :observations 0 :forwards 1}
           (inert-observer-reports old 1 true)))
    (is (= {:failures 1 :inspections 0 :observations 0 :forwards 1}
           (inert-observer-reports helper 1 true)))
    (is (= {:failures 1 :inspections 0 :observations 0 :forwards 0}
           (inert-observer-reports helper 0 false)))
    (is (= {:failures 0 :inspections 1 :observations 1 :forwards 3}
           (inert-observer-reports helper 1 false)))
    (is (= {:failures 1 :inspections 2 :observations 2 :forwards 6}
           (inert-observer-reports helper 2 false)))))

(let [report (run-tests)]
  (when (pos? (+ (:fail report) (:error report))) (System/exit 1)))
