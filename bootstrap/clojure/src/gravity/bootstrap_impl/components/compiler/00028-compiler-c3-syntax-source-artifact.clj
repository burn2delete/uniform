

(declare compiler-c3-syntax-source-artifact)

(def c4-macro-diagnostic-ids
  ["C4-NOT-MACRO"
   "C4-RETURN"
   "C4-DEPTH"
   "C4-SIZE"
   "C4-BUILD-EFFECT"
   "C4-HYGIENE"
   "C4-CAPTURE"
   "C4-GENERATED-UNSAFE"
   "C4-PROFILE"
   "C4-TRACE"])

(def c4-macro-governing-document
  "docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md")

(def c4-macro-rejected-designs
  [{:diagnostic "C4-NOT-MACRO"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-not-macro.gravity"
    :rejected-design :invalid-macro-invocation}
   {:diagnostic "C4-RETURN"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-return.gravity"
    :rejected-design :macro-output-not-syntax}
   {:diagnostic "C4-DEPTH"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-depth.gravity"
    :rejected-design :unbounded-expansion}
   {:diagnostic "C4-SIZE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-size.gravity"
    :rejected-design :unbounded-expansion-size}
   {:diagnostic "C4-BUILD-EFFECT"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-build-effect.gravity"
    :rejected-design :ambient-host-authority}
   {:diagnostic "C4-HYGIENE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-hygiene.gravity"
    :rejected-design :hygiene-violation}
   {:diagnostic "C4-CAPTURE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-capture.gravity"
    :rejected-design :illegal-authority-capture}
   {:diagnostic "C4-GENERATED-UNSAFE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-generated-unsafe.gravity"
    :rejected-design :generated-unsafe-without-safe6}
   {:diagnostic "C4-PROFILE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-profile.gravity"
    :rejected-design :generated-profile-bypass}
   {:diagnostic "C4-TRACE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c4-trace.gravity"
    :rejected-design :unreplayable-expansion-trace}])

(def c4-macro-override-diagnostics
  {:not-macro "C4-NOT-MACRO"
   :return "C4-RETURN"
   :depth "C4-DEPTH"
   :size "C4-SIZE"
   :build-effect "C4-BUILD-EFFECT"
   :hygiene "C4-HYGIENE"
   :capture "C4-CAPTURE"
   :generated-unsafe "C4-GENERATED-UNSAFE"
   :profile "C4-PROFILE"
   :trace "C4-TRACE"})

(defn c4-macro-source-overrides
  [module]
  (get-in module [:metadata :compiler :c4-macro] {}))

(defn c4-macro-message
  [id]
  (case id
    "C4-NOT-MACRO" "macro invocation is invalid or unavailable"
    "C4-RETURN" "macro output is not valid syntax"
    "C4-DEPTH" "macro expansion exceeded the deterministic depth limit"
    "C4-SIZE" "macro expansion exceeded the deterministic size limit"
    "C4-BUILD-EFFECT" "macro used undeclared or ungranted build effects"
    "C4-HYGIENE" "macro expansion violates hygiene"
    "C4-CAPTURE" "macro expansion captures a binding illegally"
    "C4-GENERATED-UNSAFE" "macro generated unsafe code without required metadata"
    "C4-PROFILE" "macro generated code illegal for the caller profile"
    "C4-TRACE" "macro expansion trace is not replayable"
    "macro expansion engine failed"))

(defn c4-macro-fail!
  [id source-path subject extra]
  (fail! id
         (c4-macro-message id)
         (merge {:source-span (or (:source-span subject)
                                  (:call-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c4-macro-expansion
                 :stage :macro-expansion
                 :document-id "C4"
                 :expected-document c4-macro-governing-document
                 :macro (:macro subject)
                 :macro-version (:macro-version subject)
                 :definition-span (:definition-span subject)
                 :call-site-span (:call-span subject)
                 :generated-span (:generated-span subject)
                 :active-profile (:profile subject)
                 :target (:target subject)
                 :build-effects (:build-effects subject)
                 :capabilities (:capabilities subject)
                 :hygiene-context (:hygiene subject)
                 :remediation "Rebuild macro expansion with syntax-object inputs and outputs, authorized build effects, visible hygiene, generated-origin links, replayable traces, and downstream safety checks."}
                extra)))

(defn c4-macro-validate-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c4-macro-override-diagnostics fail-kind)]
      (c4-macro-fail! id source-path
                      {:source-span (source-span source-path 0)
                       :macro (symbol (str "fixture/" (name fail-kind)))
                       :macro-version "fixture"
                       :profile :hosted
                       :target :jvm
                       :build-effects #{}
                       :capabilities #{}
                       :hygiene {:marks []}}
                      {:missing-fields [fail-kind]}))))

(defn c4-artifact-id
  [value]
  (str "sha256:" (sha256-hex (pr-str value))))

(defn c4-macro-environment
  [macro-artifact]
  (c4-macro-evidence/c4-macro-environment
   macro-artifact {:sha256-hex sha256-hex}))

(defn c4-expansion-input
  [module c3-artifact macro-artifact]
  (c4-macro-evidence/c4-expansion-input
   module c3-artifact macro-artifact
   {:artifact-id-of c4-artifact-id
    :max-macro-expansion-depth max-macro-expansion-depth}))

(defn c4-expanded-syntax-stream
  [macro-artifact]
  (c4-macro-evidence/c4-expanded-syntax-stream
   macro-artifact {:sha256-hex sha256-hex}))

(defn c4-trace-records
  [macro-artifact]
  (c4-macro-evidence/c4-trace-records macro-artifact))

(defn c4-hygiene-capture-records
  [trace-records]
  (c4-macro-evidence/c4-hygiene-capture-records trace-records))

(defn c4-build-effect-log
  [module trace-records]
  (c4-macro-evidence/c4-build-effect-log module trace-records))

(defn c4-macro-safety-declarations
  [macro-environment]
  (c4-macro-evidence/c4-macro-safety-declarations macro-environment))

(defn c4-generated-origin-source-map
  [trace-records expanded-stream]
  (c4-macro-evidence/c4-generated-origin-source-map
   trace-records expanded-stream))

(defn c4-expansion-cache-key
  [expansion-input trace-records]
  (c4-macro-evidence/c4-expansion-cache-key
   expansion-input trace-records {:sha256-hex sha256-hex}))

(defn c4-trace-replay-report
  [trace-records cache-key]
  (c4-macro-evidence/c4-trace-replay-report trace-records cache-key))

(defn c4-macro-safety-report
  [trace-records safety-declarations]
  (c4-macro-evidence/c4-macro-safety-report
   trace-records safety-declarations))

(defn c4-macro-capability-proof
  [artifact]
  (let [trace-records (:macro-expansion-trace artifact)
        diagnostics (set (map :diagnostic (:rejected-design-coverage artifact)))
        cache-key (:expansion-cache-key artifact)
        replay (:trace-replay-report artifact)
        safety (:macro-safety-report artifact)
        build-log (:build-effect-log artifact)]
    {:syntax-input-output-valid?
     (boolean (and (seq (get-in artifact [:c3-syntax-object-artifact
                                          :syntax-object-stream]))
                   (seq (:expanded-syntax-stream artifact))))
     :deterministic-expansion-trace?
     (boolean (and (seq trace-records)
                   (every? #(and (re-find #"^sha256:" (str (first (:output-syntax %))))
                                 (:macro-version %))
                           trace-records)))
     :hygiene-and-capture-recorded?
     (boolean (and (seq (:hygiene-capture-records artifact))
                   (every? #(contains? % :hygiene) trace-records)))
     :build-effects-authorized?
     (every? #(#{:granted :not-required} (:authorization %))
             (:records build-log))
     :generated-origin-present?
     (every? #(seq (:generated-origin %)) trace-records)
     :generated-unsafe-checked?
     (boolean (and (= :complete (:status safety))
                   (seq (:generated-unsafe safety))))
     :cache-replay-guarded?
     (boolean (and (re-find #"^sha256:" (:hash cache-key))
                   (= :passed (:status replay))))
     :diagnostics-covered?
     (= (set c4-macro-diagnostic-ids) diagnostics)
     :self-hosting-comparison-ready?
     (= :ready (get-in artifact [:self-hosting-comparison-inputs :status]))
     :status :complete}))