(ns gravity.test-scope-runner
  "Small selection boundary shared by the language and bootstrap test entrypoints.

  The existing runners remain responsible for loading and executing tests.  This
  namespace only binds a named project scope to explicit test namespaces, so a
  project command cannot silently fall back to the broad bootstrap catalog."
  (:require [clojure.string :as str]))

(def ^:private scopes
  {:language
   ['gravity.reader.normal-dispatch-test
    'gravity.syntax.pipeline-test
    'gravity.resolution.pipeline-test
    'gravity.c7-type-checker-test]

   :compiler
   ['gravity.reader.composition-test
    'gravity.checked-core.pipeline-test
    'gravity.c8-effect-checker-test
    'gravity.c11-mir-test]

   :bootstrap
   ['gravity.self-hosting.w5-stage-equivalence-verifier-test
    'gravity.self-hosting.w5-compiler-identity-verifier-test
    'gravity.self-hosting.w5-trust-provenance-verifier-test
    'gravity.self-hosting.w5-stage-rebuild-orchestrator-test
    'gravity.self-hosting.w5-subsystem-closure-verifier-test]})

(defn scope-namespaces
  "Return the fixed, non-authoritative namespace scope for a project command."
  [scope]
  (or (get scopes scope)
      (throw (ex-info "Unknown test project scope"
                      {:id "TEST-SCOPE-UNKNOWN" :scope scope}))))

(defn- selector-value
  [option value]
  (let [value (some-> value str/trim)]
    (when (str/blank? value)
      (throw (ex-info (str option " requires a non-empty value")
                      {:id "TEST-SCOPE-SELECTOR" :option option})))
    value))

(defn- namespace-selections
  [args]
  (loop [remaining (seq args)
         selected []]
    (if-not remaining
      selected
      (let [argument (first remaining)
            tail (next remaining)]
        (if (= "--namespace" argument)
          (if (seq tail)
            (recur (next tail)
                   (conj selected
                         (symbol (selector-value argument (first tail)))))
            (throw (ex-info "--namespace requires an owned test namespace"
                            {:id "TEST-SCOPE-SELECTOR"})))
          (recur tail selected))))))

(defn- ensure-scope!
  [scope selected]
  (let [allowed (set (scope-namespaces scope))
        unknown (seq (remove allowed selected))]
    (when unknown
      (throw (ex-info "Test namespace is outside the requested project scope"
                      {:id "TEST-SCOPE-NAMESPACE"
                       :scope scope
                       :namespace (first unknown)
                       :allowed (vec (scope-namespaces scope))})))
    (when (empty? selected)
      (throw (ex-info "A project test command requires an explicit namespace selection"
                      {:id "TEST-SCOPE-EXPLICIT" :scope scope})))
    selected))

(defn normalize-development-args
  "Bind a development-runner invocation to one project scope.

  An empty invocation runs the fixed scope.  Any non-empty invocation must
  name at least one namespace, while ordinary development selectors such as
  `--exact`, `--regex`, `--prefix`, `--list` and `--fail-fast` are passed on to
  the existing development runner unchanged.  The scope is an initial
  explicit catalog, not a coverage claim; add a namespace here when its
  ownership is deliberately assigned to this project."
  [scope args]
  (let [args (vec args)]
    (if (empty? args)
      (vec (mapcat #(vector "--namespace" (str %))
                   (scope-namespaces scope)))
      (if (= ["--list"] args)
        args
      (let [selected (namespace-selections args)]
        (ensure-scope! scope selected)
        args)))))

(defn normalize-bootstrap-args
  "Bind a bootstrap validation invocation to its fixed namespaces.

  Bootstrap validation intentionally exposes only namespace selection and
  fail-fast controls.  Rebuild/equivalence evidence remains owned by the
  bootstrap project and is not inferred from this development gate."
  [args]
  (let [args (vec args)]
    (if (empty? args)
      (vec (mapcat #(vector "--namespace" (str %))
                   (scope-namespaces :bootstrap)))
      (if (= ["--list"] args)
        args
      (loop [remaining (seq args)
             selected []]
        (if-not remaining
          (do (ensure-scope! :bootstrap selected) args)
          (let [argument (first remaining)
                tail (next remaining)]
            (cond
              (= "--fail-fast" argument)
              (recur tail selected)

              (= "--namespace" argument)
              (if (seq tail)
                (recur (next tail)
                       (conj selected
                             (symbol (selector-value argument (first tail)))))
                (throw (ex-info "--namespace requires an owned test namespace"
                                {:id "TEST-SCOPE-SELECTOR"})))

              :else
              (throw (ex-info "Unsupported bootstrap validation option"
                              {:id "TEST-SCOPE-OPTION"
                               :option argument}))))))))))

(defn usage
  [scope]
  (str "Project test scope: " (name scope) "\n"
       "Empty invocation runs the fixed namespace scope.\n"
       "Use --namespace NAME (repeatable), --exact/--regex/--prefix, "
       "--list and --fail-fast with development scopes.\n"
       "Allowed namespaces:\n"
       (str/join "\n" (map #(str "  " %) (scope-namespaces scope)))
       "\n"))
