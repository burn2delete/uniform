(ns gravity.reader.core-construction-binding-test
  "Inert extracted-expression checks. No project constructor or owner port runs."
  (:require [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is]]))

(def ^:private pinned "651fb42bb45f107ba4c8060fa0729b622ac75bd8")
(def ^:private root "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/")
(defn- read-forms [text]
  (binding [*read-eval* false
            *reader-resolver* (reify clojure.lang.LispReader$Resolver
                               (currentNS [_] 'gravity.bootstrap)
                               (resolveAlias [_ _] nil) (resolveClass [_ _] nil) (resolveVar [_ _] nil))]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. text))]
      (loop [result []]
        (let [form (read {:eof ::end} reader)]
          (if (= ::end form) result (recur (conj result form))))))))
(defn- original [path]
  (let [{:keys [exit out err]} (shell/sh "git" "show" (str pinned ":" path))]
    (assert (zero? exit) err) (read-forms out)))
(defn- definition [source name]
  (or (first (filter #(and (seq? %) (contains? '#{defn defn- clojure.core/defn} (first %))
                           (= name (second %))) (mapcat #(tree-seq coll? seq %) source)))
      (throw (ex-info "Missing source definition" {:name name}))))
(defn- nodes [form] (tree-seq coll? seq form))
(defn- call [form name]
  (or (first (filter #(and (seq? %) (= name (first %))) (nodes form)))
      (throw (ex-info "Missing source expression" {:name name}))))
(def ^:private current-guard (read-forms (slurp (str root "00897-sh07-core-construction-upstream-guard.clj"))))
(def ^:private current-builder (read-forms (slurp (str root "00898-sh07-core-from-authenticated-request.clj"))))
(def ^:private old-guard (original (str root "00897-sh07-core-construction-upstream-guard.clj")))
(def ^:private old-builder (original (str root "00898-sh07-core-from-authenticated-request.clj")))
(def ^:private session-forms (read-forms (slurp (str root "00899-sh07-core-verification-session.clj"))))
(defn- selected [guard builder]
  (let [roots (definition guard 'sh07-core-construction-roots)
        check (definition guard 'sh07-core-construction-check!)
        build (definition builder 'sh07-core-build-verified-artifact)
        capture (second (second (last roots)))
        predicate (call check 'every?)
        bindings (apply hash-map (second (last build)))
        expected-binding (get bindings 'construction)
        binding-vector (second (call build 'binding))]
    (assert (= 'fn (first capture)))
    (assert (= '[*sh07-core-execution-binding* captured] binding-vector))
    {:capture capture :predicate predicate :binding-vector binding-vector
     :expected-binding expected-binding}))
(def ^:private old-slices (selected old-guard old-builder))
(def ^:private new-slices (selected current-guard current-builder))

(defn- with-inert-model [slices operation]
  (let [n (create-ns (gensym "inert-construction-binding-"))
        execution (doto (intern n '*sh07-core-execution-binding* nil) (.setDynamic))
        unrelated (doto (intern n '*unrelated-dynamic* (Object.)) (.setDynamic))
        guarded (intern n 'guarded-root (Object.))]
    (try
      (binding [*ns* n]
        (clojure.core/refer 'clojure.core)
        (let [capture (eval (:capture slices))
              check (eval (list 'fn '[roots facts] (:predicate slices)))
              build-check (eval
                           (list 'fn '[captured roots construction]
                                 (list 'let (if-let [expression (:expected-binding slices)]
                                              ['construction expression] [])
                                       (list 'let '[facts construction]
                                             (list 'binding (:binding-vector slices) (:predicate slices))))))
              run-binding (eval (list 'fn '[captured operation]
                                     (list 'binding (:binding-vector slices) '(operation))))
              propagate (eval (definition session-forms 'sh07-core-session-propagate-control!))]
          ;; Only host Var, identity, map and binding primitives are executable.
          ;; All project/native owner operation names are excluded from slices.
          (doseq [form [(:capture slices) (:predicate slices) (:expected-binding slices)]
                  expression (nodes form) :when (and (seq? expression) (symbol? (first expression)))]
            (assert (contains? '#{fn if identical? var .getRawRoot var-get every? and contains?
                                 assoc} (first expression)) (str (first expression))))
          (operation {:execution execution :unrelated unrelated :guarded guarded
                      :capture-roots #(mapv capture [execution unrelated guarded])
                      :check check :build-check build-check :run-binding run-binding :propagate @propagate})))
      (finally (remove-ns (ns-name n))))))

(deftest pinned-conflict-and-corrected-legitimate-transition
  (doseq [[slices corrected?] [[old-slices false] [new-slices true]]
          outer [nil (Object.)]]
    (with-inert-model slices
      (fn [{:keys [execution capture-roots check build-check]}]
        (with-bindings {execution outer}
          (let [captured {:source-content-hash :fixture-current :plan :selected}
                roots (capture-roots)]
            (is (true? (check roots {})))
            ;; Exact selected builder binding and optional facts expression;
            ;; B/C here are predicate slices, not actual project checkpoints.
            (is (= corrected? (build-check captured roots {})))
            (is (= corrected? (build-check captured roots {})))
            (is (identical? outer (var-get execution)))
            (is (true? (check roots {})))))))))

(deftest same-binding-and-restored-outer-context-remain-legal
  (doseq [slices [old-slices new-slices]]
    (with-inert-model slices
      (fn [{:keys [execution capture-roots check build-check]}]
        (let [captured {:source-content-hash :fixture-current :plan :selected}]
          (with-bindings {execution captured}
            (let [roots (capture-roots)]
              (is (true? (build-check captured roots {})))
              (is (true? (check roots {})))
              (is (identical? captured (var-get execution))))))))))

(deftest real-root-and-unrelated-dynamic-tampering-still-refuse
  (doseq [slices [old-slices new-slices]]
    (with-inert-model slices
      (fn [{:keys [execution unrelated guarded capture-roots check]}]
        (let [outer (Object.) original (.getRawRoot ^clojure.lang.Var guarded)]
          (with-bindings {execution outer}
            (let [roots (capture-roots)]
              (try
                (.bindRoot ^clojure.lang.Var guarded (Object.))
                (is (false? (check roots {})))
                (finally (.bindRoot ^clojure.lang.Var guarded original)))
              (with-bindings {unrelated (Object.)} (is (false? (check roots {}))))))))))
  (with-inert-model new-slices
    (fn [{:keys [execution capture-roots check]}]
      (let [original (.getRawRoot ^clojure.lang.Var execution) outer (Object.)]
        (with-bindings {execution outer}
          (let [roots (capture-roots)]
            (try
              (.bindRoot ^clojure.lang.Var execution (Object.))
              (is (identical? outer (var-get execution)))
              (is (false? (check roots {})))
              (finally (.bindRoot ^clojure.lang.Var execution original)))
            (is (true? (check roots {})))))))))

(deftest selected-binding-rejects-wrong-and-equal-looking-substitutions
  (with-inert-model new-slices
    (fn [{:keys [execution capture-roots check]}]
      (let [outer (Object.) captured {:source-content-hash :fixture-current :plan :selected}
            equal-looking (into {} captured)]
        (is (= captured equal-looking))
        (is (not (identical? captured equal-looking)))
        (with-bindings {execution outer}
          (let [roots (capture-roots) facts {:execution-binding captured}]
            (doseq [wrong [nil outer equal-looking (assoc captured :plan :substituted)]]
              (with-bindings {execution wrong} (is (false? (check roots facts)))))
            (with-bindings {execution captured} (is (true? (check roots facts))))
            ;; Outside the builder the originally captured outer identity is
            ;; still required; accepting any effective binding is not a fix.
            (with-bindings {execution captured} (is (false? (check roots {}))))))))))

(deftest existing-source-hash-legality-expression-is-conserved
  (let [source (read-forms (slurp "bootstrap/clojure/src/gravity/checked_core.clj"))
        run (definition source 'run-structural-request-for-test)
        guard (call run 'when-not)
        validate (eval (list 'fn '[input-key core-binding] guard))
        key {:source ["inert.gravity" :fixture-current]}]
    (is (nil? (validate key {:source-content-hash :fixture-current :plan :selected})))
    (is (thrown? clojure.lang.ExceptionInfo (validate key {:source-content-hash :wrong})))
    (is (thrown? clojure.lang.ExceptionInfo (validate key nil)))
    ;; The old hash predicate alone cannot distinguish substituted plan data.
    ;; The new exact selected-object check above covers that separate join.
    (is (nil? (validate key {:source-content-hash :fixture-current :plan :substituted})))
    (is (= source (original "bootstrap/clojure/src/gravity/checked_core.clj")))))

(deftest controlled-binding-unwinds-with-original-throwable-and-flag
  (doseq [kind [:ordinary :fatal :interrupt :wrapped-fatal-interrupt]
          preinterrupted? [false true]]
    (with-inert-model new-slices
      (fn [{:keys [execution capture-roots check run-binding propagate]}]
        (let [outer (Object.) interrupt (InterruptedException. "synthetic interrupt")
              fatal (doto (AssertionError. "synthetic fatal") (.initCause interrupt))
              error (case kind :ordinary (java.io.IOException. "synthetic ordinary")
                          :fatal (AssertionError. "synthetic fatal only")
                          :interrupt interrupt :wrapped-fatal-interrupt (ex-info "synthetic wrapper" {} fatal))
              cause (.getCause ^Throwable error)]
        (try
          (with-bindings {execution outer}
            (let [roots (capture-roots)
                  _ (Thread/interrupted)
                  _ (when preinterrupted? (.interrupt (Thread/currentThread)))
                  observed (try
                             (try (run-binding {:plan :inert} (fn [] (throw error)))
                                  (catch Throwable caught (propagate caught) (throw caught)))
                             (catch Throwable caught
                               {:error caught :flag (.isInterrupted (Thread/currentThread))}))]
              (Thread/interrupted)
              (is (identical? error (:error observed)))
              (is (identical? cause (.getCause ^Throwable (:error observed))))
              (is (= (or preinterrupted? (contains? #{:interrupt :wrapped-fatal-interrupt} kind)) (:flag observed)))
              (is (identical? outer (var-get execution)))
              (is (true? (check roots {})))))
          (finally (Thread/interrupted))))))))

(deftest no-project-implementation-namespace-was-loaded
  (doseq [name '[gravity.bootstrap gravity.checked-core gravity.resolution gravity.syntax gravity.macro]]
    (is (nil? (find-ns name)) (str name))))
