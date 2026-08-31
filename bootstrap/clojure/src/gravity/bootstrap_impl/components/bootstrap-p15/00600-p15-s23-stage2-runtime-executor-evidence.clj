

(defn p15-s23-stage2-runtime-executor-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-runtime-executor-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-plan-id
           (get-in artifact [:accepted-record :stage2-plan-id])
           :stage2-runtime-executed?
           (:stage2-runtime-executed? proof)
           :stage0-instruction-runner-replaced?
           (:stage0-instruction-runner-replaced? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
	           :rejected-diagnostics-equivalent?
	           (:rejected-diagnostics-equivalent? proof)
	           :stage2-runtime-kernel-used?
	           (:stage2-runtime-kernel-used? proof)
	           :stage2-runtime-host-replaced?
	           (:stage2-runtime-host-replaced? proof)
	           :stage2-runtime-primitives-replaced?
	           (:stage2-runtime-primitives-replaced? proof)
	           :gravity-runtime-primitives-used?
	           (:gravity-runtime-primitives-used? proof)
	           :does-not-use-clojure-stage0-runtime-host?
	           (:does-not-use-clojure-stage0-runtime-host? proof)
	           :does-not-use-clojure-runtime-primitives?
	           (:does-not-use-clojure-runtime-primitives? proof)
	           :residual-clojure-rule-runner-recorded?
	           (:residual-clojure-rule-runner-recorded? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage2-source-front-end-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity
    :generated-origin :diagnostic-codes :effects :capabilities
    :profile :compiler-lineage :artifact-provenance})

(def p15-s23-stage2-source-front-end-required-emits
  #{:stage2-front-end-record :stage2-reader-record
    :stage2-macro-expansion-record :module-context
    :expanded-core-forms :accepted-front-end-comparison
    :rejected-diagnostic-comparison :stage2-front-end-boundary-record})

(def p15-s23-stage2-source-front-end-required-steps
  #{:scan-characters :classify-tokens :build-forms
    :build-syntax-objects :expand-built-in-macros
    :validate-module-contract :emit-front-end-record})

(def p15-s23-stage2-source-front-end-diagnostic-messages
  {"P15S23F001" "P15-S23 stage2 source front-end contract is missing"
   "P15S23F002" "P15-S23 stage2 source front-end rule set is incomplete"
   "P15S23F003" "P15-S23 stage2 source front-end macro expansion is incomplete"
   "P15S23F004" "P15-S23 stage2 source front-end accepted output is not equivalent"
   "P15S23F005" "P15-S23 stage2 source front-end rejected diagnostics are not preserved"
   "P15S23F006" "P15-S23 stage2 source front-end evidence links are incomplete"
   "P15S23F007" "P15-S23 stage2 source front-end preservation or emission contract is incomplete"
   "P15S23F008" "P15-S23 stage2 source front-end boundary record is incomplete"
   "P15S23F009" "P15-S23 stage2 source front-end rejected malformed source or overclaim"})

(def p15-s23-stage2-source-front-end-diagnostic-ids
  ["P15S23F001" "P15S23F002" "P15S23F003" "P15S23F004"
   "P15S23F005" "P15S23F006" "P15S23F007" "P15S23F008"
   "P15S23F009"])

(defn p15-s23-stage2-source-front-end-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-source-front-end-diagnostic-messages
              id
              "P15-S23 stage2 source front-end proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-source-front-end
                 :diagnostic-family :p15-s23-stage2-source-front-end
                 :value value
                 :remediation "Keep the stage2 reader and macro front-end rules authored in Gravity source, execute accepted and rejected fixtures through the declared front-end boundary, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-source-front-end-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-source-front-end
   :source-span {:source source-path}
   :message (get p15-s23-stage2-source-front-end-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_source_front_end})

(defonce p15-s23-source-artifact-cache
  (atom {}))

(defn p15-s23-source-artifact-cache-key
  [kind source-path]
  (let [file (java.io.File. source-path)]
    [kind (.getCanonicalPath file) (.lastModified file) (.length file)]))

(defn p15-s23-cached-source-artifact
  [kind source-path build-fn]
  (if *p15-s23-artifact-build-context*
    (p15-s23-context-artifact kind source-path build-fn)
    (let [cache-key (p15-s23-source-artifact-cache-key kind source-path)]
      (if-let [artifact (get @p15-s23-source-artifact-cache cache-key)]
        artifact
        (let [artifact (build-fn)]
          (swap! p15-s23-source-artifact-cache assoc cache-key artifact)
          artifact)))))

(defn p15-s23-front-end-state
  [source-path source-text]
  {:source-path source-path
   :source-text source-text
   :idx 0
   :line 1
   :column 1})

(defn p15-s23-front-end-current-char
  [state]
  (let [idx (:idx state)
        text (:source-text state)]
    (when (< idx (count text))
      (nth text idx))))

(defn p15-s23-front-end-advance
  [state]
  (let [ch (p15-s23-front-end-current-char state)
        previous-cr?
        (and (= \newline ch)
             (pos? (:idx state))
             (= \return (.charAt ^String (:source-text state)
                                 (dec (:idx state)))))]
    (cond
      (= \return ch)
      (-> state
          (update :idx inc)
          (assoc :line (inc (:line state)) :column 1))

      (and (= \newline ch) (not previous-cr?))
      (-> state
          (update :idx inc)
          (assoc :line (inc (:line state)) :column 1))

      previous-cr?
      (update state :idx inc)

      :else
      (-> state
          (update :idx inc)
          (update :column inc)))))

(defn p15-s23-front-end-location
  [state]
  {:line (:line state)
   :column (:column state)
   :char (:idx state)
   :byte (utf8-byte-count (subs (:source-text state) 0 (:idx state)))})

(defn p15-s23-front-end-span
  [source-path form-index start-state end-state]
  {:source source-path
   :form-index form-index
   :start (p15-s23-front-end-location start-state)
   :end (p15-s23-front-end-location end-state)
   :byte-start (:byte (p15-s23-front-end-location start-state))
   :byte-end (:byte (p15-s23-front-end-location end-state))})

(defn p15-s23-front-end-reader-error!
  [source-path state data]
  (p15-s23-stage2-source-front-end-fail!
   "P15S23F009" source-path data
   (merge {:reader-state (select-keys state [:idx :line :column])}
          data)))

(defn p15-s23-front-end-skip-comment
  [state]
  (loop [state state]
    (let [ch (p15-s23-front-end-current-char state)]
      (cond
        (nil? ch) state
        (= \return ch)
        (let [advanced (p15-s23-front-end-advance state)]
          (if (= \newline (p15-s23-front-end-current-char advanced))
            (p15-s23-front-end-advance advanced)
            advanced))
        (= \newline ch) (p15-s23-front-end-advance state)
        :else (recur (p15-s23-front-end-advance state))))))

(defn p15-s23-front-end-skip-ignored
  [state]
  (loop [state state]
    (let [ch (p15-s23-front-end-current-char state)]
      (cond
        (nil? ch) state
        (Character/isWhitespace ^Character ch)
        (recur (p15-s23-front-end-advance state))
        (= \, ch)
        (recur (p15-s23-front-end-advance state))
        (= \; ch)
        (recur (p15-s23-front-end-skip-comment state))
        :else state))))

(defn p15-s23-front-end-delimiter?
  [ch]
  (or (nil? ch)
      (Character/isWhitespace ^Character ch)
      (contains? #{\( \) \[ \] \{ \} \" \;} ch)
      (= \, ch)))

(declare p15-s23-front-end-read-form)