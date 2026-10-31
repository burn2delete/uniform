;; Read and compare source/data only; never load or evaluate Gravity or model forms.
(load-file "specs/accounting-removal/parameter-source-composition/source_forms.clj")
(require '[source-forms :as sf] '[clojure.edn :as edn]
         '[clojure.java.shell :as sh] '[clojure.string :as s] '[clojure.walk :as w]
         '[clojure.test :as t :refer [deftest is]])
(def base "70dcb2220701529dbb02bd2317a822eb3ac72fed")
(def Map-ref "a93ef34ba81f812d6142d9f8c560d926168c89b3")
(def ACC-ref "04ceaf61dc0ac7da6bc4e1909a4a744785276563")
(def folder "specs/accounting-removal/parameter-source-composition/")
(def inputs (str folder "inputs/"))
(def prod ["bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
           "bootstrap/gravity/src/gravity/effects.gravity" "bootstrap/gravity/p15_s23/emitter.gravity"])
(def authentic ["bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj"
                "bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj"
                "bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"])
(def models (mapv #(str "bootstrap/clojure/test/gravity/types/private_map_" % "_test.clj") ["cycle" "fact" "source" "physical" "metadata" "parameter"]))
(defn sha [x] (format "%064x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes x "UTF-8")))))
(defn git [& xs] (let [r (apply sh/sh "git" xs)] (assert (zero? (:exit r)) (:err r)) (:out r)))
(def source-text (memoize (fn [ref p] (git "show" (str ref ":" p)))))
(defn entries [text] (sf/forms text))
(defn name-of [e] (when (seq? (:form e)) (second (:form e))))
(defn raw [text e] (subs text (:start e) (:end e)))
(defn at [es name] (:form (sf/named es name)))
(defn nodes [x] (tree-seq coll? seq x))
(defn stable [x] (s/replace (pr-str x) #"__[0-9]+#" "__reader#"))
(defn named-entries [es] (into {} (for [e es :when (and (seq? (:form e)) (symbol? (second (:form e))))] [(name-of e) e])))
(defn prune [text es names]
  (reduce (fn [out e] (str (subs out 0 (:start e)) "<selected-form>" (subs out (:end e))))
          text (sort-by :start > (filter #(names (name-of %)) es))))
(def mp? '#{sh08-mp-compatibility body-mp-compatibility p15-s23-mp-compatibility})
(def overlaps '#{sh08-pmk-proof body-pmk-proof body-pmk-entry-evidence p15-s23-pmk-proof p15-s23-pmk-entry-evidence})
(defn Map-normalize [name f]
  (if-not (overlaps name) f
    (w/postwalk
      (fn [x]
        (let [x (if (and (seq? x) (mp? (first x)) (= 10 (count x))) (apply list (butlast x)) x)]
          (cond
            (= x '(+ used (if (= :accepted (get source :status)) (get compatibility :work-count) 0))) 'used
            (= x '(if (= :private-map-keys-work-limit (get source :reason)) true (= :map-parameter-work-limit (get compatibility :reason)))) '(= :private-map-keys-work-limit (get source :reason))
            (= x '(if (> spent budget) true (= :map-parameter-work-limit (get compatibility :reason)))) '(> spent budget)
            (= x {:status :accepted :work-count 0 :premise '(get entry :compatibility)}) (dissoc x :work-count)
            (= x {:status :not-applicable :work-count 0}) (dissoc x :work-count)
            (and (seq? x) (= 'let (first x)) (vector? (second x)) (some #{'(+ used (get compatibility :work-count))} (second x)))
            (apply list 'let (vec (mapcat (fn [[k v]] (cond (= k 'total) [] (= k 'spent) [k 'used] :else [k v])) (partition 2 (second x)))) (drop 2 x))
            :else x))) f)))
(def h (edn/read-string (slurp (str inputs "ACC04-handoff.edn"))))
(def units (vec (cons {:unit "cycle" :base (get-in h [:cycle :base]) :reviewed-candidate (get-in h [:cycle :candidate])
                      :source-changes (mapv (fn [{:keys [path changed-original-forms]}] {:path path :changed-forms changed-original-forms}) (get-in h [:cycle :lineage]))}
                     (:units h))))
(def pmk-snapshot
  (memoize (fn [ref p]
    (let [text (source-text ref p) es (filter #(and (symbol? (name-of %)) (s/includes? (str (name-of %)) "-pmk-")) (entries text))]
      {:text text :entries (vec es) :forms (into {} (map (juxt name-of :form) es))}))))
(def current (into {} (for [p prod :let [text (slurp p) es (entries text)]] [p {:text text :entries es :named (named-entries es)}])))
(def main-snap (into {} (for [p prod :let [text (source-text base p) es (entries text)]] [p {:text text :entries es :named (named-entries es)}])))
(def Map-snap (into {} (for [p prod :let [text (source-text Map-ref p) es (entries text)]] [p {:text text :entries es :named (named-entries es)}])))
(def rows (atom []))
(def composition
  (reduce
    (fn [state unit]
      (reduce
        (fn [state {:keys [path changed-forms]}]
          (let [a (pmk-snapshot (get-in unit [:base :head]) path)
                b (pmk-snapshot (get-in unit [:reviewed-candidate :head]) path)
                declared (set changed-forms)
                changed (set (filter #(not= (get-in a [:forms %]) (get-in b [:forms %]))
                                     (set (concat (keys (:forms a)) (keys (:forms b))))))]
            (assert (= declared changed) [(:unit unit) path :declared-delta])
            (assert (= (prune (:text a) (:entries a) declared) (prune (:text b) (:entries b) declared)) [(:unit unit) path :outside-delta])
            (reduce
              (fn [state name]
                (let [old (get-in a [:forms name]) new (get-in b [:forms name]) actual (get-in state [path name])
                      relation (cond (= actual old) :exact-parent (= actual (Map-normalize name old)) :reviewed-Map-commutation :else :UNACCEPTED-parent-drift)]
                  (assert (not= :UNACCEPTED-parent-drift relation) [(:unit unit) path name])
                  (swap! rows conj {:unit (:unit unit) :path path :name name :relation relation
                                   :base (get-in unit [:base :head]) :accepted (get-in unit [:reviewed-candidate :head])
                                   :old-form-sha256 (sha (pr-str old)) :current-form-sha256 (sha (pr-str actual))
                                   :accepted-form-sha256 (sha (pr-str new)) :composed-form-sha256 (sha (pr-str (Map-normalize name new)))})
                  (assoc-in state [path name] (Map-normalize name new)))) state (sort declared)))) state (:source-changes unit)))
    (into {} (for [p prod] [p (:forms (pmk-snapshot Map-ref p))])) units))
(def pmk-changed (into {} (for [p prod] [p (set (map :name (filter #(= p (:path %)) @rows)))])))
(def Map-changed
  (into {} (for [p prod :let [a (main-snap p) b (Map-snap p)]]
    [p (set (for [[name e] (:named a) :let [other (get-in b [:named name])]
                  :when (not= (raw (:text a) e) (raw (:text b) other))] name))])))
(def expected (into {} (for [p prod]
  [p (merge (into {} (map (fn [[n e]] [n (:form e)]) (:named (Map-snap p))))
            (select-keys (composition p) (pmk-changed p)))])))
(defn reject! [id] (throw (ex-info "Combined source contract refused" {:id id})))
(defn form-contract! [p name form]
  (let [wanted (get-in expected [p name])]
    (when-not (= (nth wanted 2 nil) (nth form 2 nil)) (reject! :declared-ABI))
    (when-not (= wanted form) (reject! :semantic-guard-or-proof-schema))) true)
(defn refusal [f] (try (f) nil (catch clojure.lang.ExceptionInfo e (:id (ex-data e)))))
(defn direct-calls [f]
  (cond (and (seq? f) (= 'quote (first f))) []
        (seq? f) (cons f (mapcat direct-calls f)) (coll? f) (mapcat direct-calls f) :else []))
(defn bad-arities [forms]
  (let [defs (into {} (for [f forms :when (and (seq? f) (= 'defn (first f)) (vector? (nth f 2 nil)))] [(second f) (nth f 2)]))]
    (vec (for [f forms :when (and (seq? f) (= 'defn (first f))) call (mapcat direct-calls (drop 3 f))
               :let [args (get defs (first call)) n (count (rest call)) amp (if args (.indexOf args '&) -1)]
               :when (and args (if (neg? amp) (not= n (count args)) (< n amp)))]
           {:caller (second f) :callee (first call) :arguments n :parameters args}))))

(deftest exact-inputs-and-qualified-authority
  (doseq [[p hash] [["ACC04-handoff.edn" "eb471e1232e0ace96882dffcb223a45c7cc713e46e49b8bd88e563241d49cb27"]
                    ["cycle-result.edn" "a2f82f4e280ebe79d2c216821e3ce818bf5bbe6d98a0c476fa01c296971f95ca"]
                    ["cycle-freeze.edn" "d64771ca82ba228dc0d8cc29ccd2b7f8316af368711ff144ccdf1ea61fe03ef7"]
                    ["cycle-static-composition.edn" "0373ed6e53000d6612509614dcfcd22cd2062abdb9c315d8f0e170dc6462e5b4"]
                    ["cycle-assessment.edn" "49c0a409f2921fdbb6885390a2f4071d24d93a6891efb372eed631be390471ed"]
                    ["Map-R1-receipt.edn" "bdf111c44fc280bc6538e194f031c6400ae9d494ccb4eaa99dbad1c25aa510e7"]
                    ["Map-original-CR-receipt.edn" "1f8b566db6c5f1c09fef8b13bd9848ea79a226dcb57675d02e3890f87db81ef3"]
                    ["List-receipt.edn" "c657429c60e017a13a9c51d1529f33a7962f31aa9beb88e51a1ba940a291a499"]]]
    (is (= hash (sha (slurp (str inputs p)))) p))
  (doseq [unit (:units h) :let [{:keys [path sha256]} (:review-consumption unit) file (.getName (java.io.File. path))]]
    (is (= sha256 (sha (slurp (str inputs file)))))
    (is (= (source-text ACC-ref path) (slurp (str inputs file)))))
  (is (= :qualified-draft-suitable-static-preparation-only (get-in h [:indexing-prerequisite :classification])))
  (is (false? (get-in h [:indexing-prerequisite :semantic-acceptance]))))

(deftest complete-production-equations-and-byte-conservation
  (is (= 46 (count @rows)))
  (is (= 31 (count (set (map (juxt :path :name) @rows)))))
  (doseq [p prod :let [a (main-snap p) m (Map-snap p) c (current p) changed (into (Map-changed p) (pmk-changed p))]]
    (is (= (count (:entries a)) (count (:entries c))))
    (is (= (prune (:text a) (:entries a) changed) (prune (:text c) (:entries c) changed)) p)
    (doseq [name changed] (is (form-contract! p name (at (:entries c) name)) (str p " " name)))
    (doseq [[name e] (:named a) :when (not (changed name))]
      (is (= (raw (:text a) e) (raw (:text c) (get-in c [:named name]))) (str p " " name)))
    (doseq [name (remove (pmk-changed p) (Map-changed p))]
      (is (= (raw (:text m) (get-in m [:named name])) (raw (:text c) (get-in c [:named name])))))
    (doseq [name (pmk-changed p)]
      (is (= (get-in composition [p name]) (Map-normalize name (get-in (pmk-snapshot ACC-ref p) [:forms name])))))
    (is (empty? (bad-arities (map :form (:entries c)))))
    (is (empty? (bad-arities (map :form (:entries a)))))))

(def descriptor-test 'pr359-descriptor-private-map-route-reconstructs-the-complete-descriptor)
(defn adapt-descriptor-test [text]
  (s/replace text "(pr359-descriptor-body 'body-lp-context request facts bindings 65536)" "(pr359-descriptor-body 'body-lp-context request facts bindings)"))
(def authentic-current (into {} (for [p authentic] [p {:text (slurp p) :entries (entries (slurp p))}])))
(deftest preserved-authentic-and-model-controls
  (doseq [p (rest authentic)] (is (= (source-text Map-ref p) (slurp p)) p))
  (let [p (first authentic) a (source-text Map-ref p) ae (entries a) c (:text (authentic-current p)) ce (:entries (authentic-current p))
        approved (source-text ACC-ref p) pe (entries approved)]
    (is (= (prune a ae #{descriptor-test}) (prune c ce #{descriptor-test})))
    (is (= (adapt-descriptor-test (raw approved (sf/named pe descriptor-test))) (raw c (sf/named ce descriptor-test))))
    (doseq [name '[sh08-homogeneous-list-parameter-development-kernel sh08-bare-map-parameter-development-kernel
                   sh08-bare-list-parameter-development-kernel list-parameter-compatibility-preserves-complete-validation-without-allowance
                   map-parameter-compatibility-preserves-complete-validation-without-allowance]]
      (is (= (raw a (sf/named ae name)) (raw c (sf/named ce name))) (str name))))
  (doseq [p models] (is (= (source-text ACC-ref p) (slurp p)) p))
  (is (= 22 (count (for [p models e (entries (slurp p)) :when (= 'deftest (first (:form e)))] e)))))

(defn helper-inventory [f]
  (vec (for [scope (nodes f) :when (and (seq? scope) (= 'let (first scope)))
             [name value] (partition 2 (second scope))
             :when (and (symbol? name) (seq? value) (= 'fn* (first value)) (vector? (second value)))]
         {:name name :parameters (second value) :definition value :Map-wrapper? (boolean (some mp? (nodes value)))
          :calls (vec (filter #(and (seq? %) (= name (first %))) (nodes scope)))})))
(deftest actual-ABIs-and-R1-lexical-call-arity
  (doseq [[p prefix] (map vector prod ["sh08" "body" "p15-s23"])
          [suffix arity] [["mp-compatibility" 8] ["lp-compatibility" 8] ["pmk-physical" 4]
                          ["pmk-acyclic" 2] ["pmk-metadata-work" 1] ["pmk-parameter" 6] ["pmk-source" 8]]]
    (is (= arity (count (nth (at (:entries (current p)) (symbol (str prefix "-" suffix))) 2)))))
  (let [p (second prod) f (at (:entries (current p)) 'body-lp-context)]
    (is (= '[request facts bindings] (nth f 2)))
    (is (not-any? '#{:work-count :map-work budget map-budget map-work} (nodes f))))
  (let [es (:entries (authentic-current (first authentic)))]
    (doseq [name '[sh08-homogeneous-list-parameter-development-kernel sh08-bare-map-parameter-development-kernel sh08-bare-list-parameter-development-kernel]]
      (let [helpers (filter :Map-wrapper? (helper-inventory (at es name)))]
        (is (= 1 (count helpers)))
        (is (every? #(= [] (:parameters %)) helpers))
        (is (every? #(= 1 (count %)) (mapcat :calls helpers)))))
    (let [siblings (filter #(and (= 'check (:name %)) (not (:Map-wrapper? %))) (helper-inventory (at es 'sh08-homogeneous-list-parameter-development-kernel)))]
      (is (= 1 (count siblings)))
      (is (= 1 (count (:parameters (first siblings)))))
      (is (= [1 1] (mapv #(count (rest %)) (:calls (first siblings))))))))

(deftest indexing-exclusion-and-carrier-residual
  (let [p (first prod) names '[sh08-lq-step sh08-pmk-check-with-forms sh08-pmk-index-forms sh08-pmk-type-view]]
    (doseq [name names] (is (= (raw (:text (main-snap p)) (get-in (main-snap p) [:named name]))
                              (raw (:text (current p)) (get-in (current p) [:named name])))))
    (is (= (raw (:text (Map-snap p)) (get-in (Map-snap p) [:named 'sh08-ft-call-constraints-with-context]))
           (raw (:text (current p)) (get-in (current p) [:named 'sh08-ft-call-constraints-with-context])))))
  (let [p "specs/accounting-removal/private-map-parameter-residual.edn" raw (slurp (str inputs "private-map-parameter-residual.edn")) r (edn/read-string raw)]
    (is (= (source-text ACC-ref p) raw))
    (is (false? (:fixed-by-this-increment r)))
    (is (= :preexisting-raw-input-host-model-exception (:classification r)))
    (is (= :non-vector-parameter-form-value (:condition r)))))

(def negatives (atom []))
(defn negative! [id form f expected-reason]
  (let [envelope {:sha256 (sha (pr-str form)) :form form}]
    (is (= (:sha256 envelope) (sha (pr-str (:form envelope)))))
    (let [reason (refusal #(f (:form envelope)))]
      (is (= expected-reason reason) (str id))
      (swap! negatives conj {:id id :rehashed-form-sha256 (:sha256 envelope) :refused-by reason}))))
(defn all-vector-call? [x]
  (and (seq? x) (symbol? (first x)) (vector? (second x))
       (some #(s/ends-with? (str (first x)) %) ["-all-true?" "-all?" "-typed-all"])))
(deftest coherent-source-guard-schema-and-ABI-corruptions
  (doseq [[p prefix] (map vector prod ["sh08" "body" "p15-s23"])
          suffix ["mp-compatibility" "lp-compatibility" "pmk-physical" "pmk-acyclic" "pmk-metadata-work" "pmk-parameter" "pmk-source" "pmk-fact-census"]
          :let [name (symbol (str prefix "-" suffix)) f (get-in expected [p name])]
          :when f
          :let [record (first (filter #(and (map? %) (= :accepted (:status %))) (nodes f)))]
          :when record]
    (negative! [name :restored-zero-work-report] (w/postwalk-replace {record (assoc record :work-count 0)} f)
      #(form-contract! p name %) :semantic-guard-or-proof-schema))
  (doseq [p prod name (sort (into (Map-changed p) (pmk-changed p)))
          :let [f (get-in expected [p name]) predicate (first (filter all-vector-call? (nodes f)))
                validator #(form-contract! p name %)]]
    (when predicate
      (let [conditions (second predicate)]
        (doseq [i (range (count conditions))]
          (let [changed (list (first predicate) (vec (concat (subvec conditions 0 i) (subvec conditions (inc i)))))
                mutant (w/postwalk-replace {predicate changed} f)]
            (negative! [name :lost-semantic-predicate i] mutant validator :semantic-guard-or-proof-schema)))))
    (when-let [record (first (filter #(and (map? %) (= :accepted (:status %))) (nodes f)))]
      (negative! [name :changed-success-record] (w/postwalk-replace {record (assoc record :status :rejected)} f) validator :semantic-guard-or-proof-schema))
    (when-let [record (first (filter #(and (map? %) (some (set (keys %)) [:profile :target :invalidation :source-call :key-domain-proof])) (nodes f)))]
      (let [key (first (filter (set (keys record)) [:profile :target :invalidation :source-call :key-domain-proof]))]
        (negative! [name :missing-proof-field key] (w/postwalk-replace {record (dissoc record key)} f) validator :semantic-guard-or-proof-schema))))
  (doseq [[p prefix] (map vector prod ["sh08" "body" "p15-s23"])
          suffix ["mp-compatibility" "pmk-physical" "pmk-acyclic" "pmk-metadata-work" "pmk-parameter" "pmk-source"]
          :let [name (symbol (str prefix "-" suffix)) f (get-in expected [p name])]]
    (negative! [name :restored-budget-parameter] (assoc (vec f) 2 (conj (nth f 2) 'budget)) #(form-contract! p name %) :declared-ABI))
  (doseq [[p _prefix] (map vector prod ["sh08" "body" "p15-s23"])
          suffix ["-mp-compatibility" "-pmk-physical" "-pmk-acyclic" "-pmk-metadata-work" "-pmk-parameter"]
          :let [es (:entries (current p)) owner (first (filter (fn [e] (some #(and (seq? %) (symbol? (first %)) (s/ends-with? (str (first %)) suffix)) (mapcat direct-calls (drop 3 (:form e))))) es))]
          :when owner
          :let [f (:form owner) call (first (filter #(and (seq? %) (symbol? (first %)) (s/ends-with? (str (first %)) suffix)) (mapcat direct-calls (drop 3 f))))
                mutant (w/postwalk-replace {call (apply list (concat call ['budget]))} f)]]
    (negative! [(second f) :restored-caller-budget suffix] mutant
      (fn [bad] (when (seq (bad-arities (map #(if (= (second f) (name-of %)) bad (:form %)) es))) (reject! :direct-call-arity))) :direct-call-arity))
  (doseq [[p prefix] (map vector prod ["sh08" "body" "p15-s23"])
          :let [name (symbol (str prefix "-pmk-acyclic")) f (get-in expected [p name])]]
    (is (some #{'(= root callee)} (nodes f)))
    (negative! [name :disabled-reentry-check] (w/postwalk-replace {'(= root callee) false} f) #(form-contract! p name %) :semantic-guard-or-proof-schema)))

(defn assertions [f] (filter #(and (seq? %) (= 'is (first %))) (nodes f)))
(deftest coherent-test-control-loss-refusals
  (let [r (edn/read-string (slurp (str inputs "private-map-parameter-residual.edn")))]
    (negative! :false-carrier-residual-closeout (assoc r :fixed-by-this-increment true)
      (fn [bad] (when-not (false? (:fixed-by-this-increment bad)) (reject! :unrepaired-residual))) :unrepaired-residual))
  (let [f (at (:entries (authentic-current (first authentic))) 'sh08-homogeneous-list-parameter-development-kernel)
        equality '(is (= checked (check)))
        validator (fn [bad] (when-not (= (stable f) (stable bad)) (reject! :R1-control-conservation)))]
    (is (some #{equality} (nodes f)))
    (negative! :weakened-R1-result-equality (w/postwalk-replace {equality '(is (= :accepted (:status (check))))} f) validator :R1-control-conservation)
    (negative! :restored-R1-call-argument (w/postwalk-replace {'(check) '(check 65536)} f) validator :R1-control-conservation))
  (doseq [p models :let [forms (mapv :form (entries (slurp p)))
                        original (first (filter #(and (seq? %) (= 'deftest (first %))) forms))
                        premise (first (assertions original))
                        mutant (w/postwalk-replace {premise nil} original)
                        required (frequencies (map stable (assertions original)))]]
    (negative! [p :removed-model-control] mutant
      (fn [bad] (let [actual (frequencies (map stable (assertions bad)))]
                  (when (some (fn [[k n]] (< (get actual k 0) n)) required) (reject! :model-control-loss)))) :model-control-loss)))

(deftest source-only-namespace-boundary
  (doseq [n '[gravity.bootstrap gravity.self-hosting.sh08-function-call-type-test gravity.self-hosting.sh09-body-effect-test
              gravity.self-hosting.sh01-stage2-runtime-iteration-test gravity.types.private-map-cycle-test
              gravity.types.private-map-fact-test gravity.types.private-map-source-test gravity.types.private-map-physical-test
              gravity.types.private-map-metadata-test gravity.types.private-map-parameter-test]]
    (is (nil? (find-ns n)))))

(let [counts (t/run-tests 'user)
      touched (vec (concat prod authentic models))
      result {:kind :complete-parameter-source-composition-controls :base base :List-Map-source Map-ref :ACC04-source ACC-ref
              :counts counts :source-equations (count @rows) :PMK-definitions 31
              :combined-production-definitions (reduce + (for [p prod] (count (into (Map-changed p) (pmk-changed p)))))
              :coherently-rehashed-negative-controls (count @negatives)
              :model-controls :preserved-exact-source-not-evaluated :genuine-semantic-execution :UNRUN
              :target-evaluations 0 :Bootstrap-compiler-kernel-native-runtime-model-helper-evaluations 0
              :current-namespace-loads :core-and-source-reader-only
              :changed-source-test-files (into (sorted-map) (for [p touched] [p (sha (slurp p))]))}]
  (spit ".cpcache/parameter-source-composition/source-equations.edn" (pr-str @rows))
  (spit ".cpcache/parameter-source-composition/negative-controls.edn" (pr-str @negatives))
  (spit ".cpcache/parameter-source-composition/source-controls-result.edn" (pr-str result))
  (prn result) (shutdown-agents)
  (when (pos? (+ (:fail counts) (:error counts))) (System/exit 1)))
