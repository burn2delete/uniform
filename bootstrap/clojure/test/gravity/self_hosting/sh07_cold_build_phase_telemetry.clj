(ns gravity.self-hosting.sh07-cold-build-phase-telemetry
  "Explicit, development-only SH-07 cold-build phase telemetry.

  This namespace observes one caller-supplied SH-07 artifact invocation through
  temporary root-Var wrappers.  It never changes an artifact, identity,
  diagnostic, cache, or authoritative runner.  Timing and allocation are
  host-variable observations; the bounded cardinality rows and call counts are
  diagnostic accounting only."
  (:require [gravity.bootstrap :as bootstrap]))

(def ^:private phase-order
  [:sh07-core-file-artifact
   :sh07-pinned-plan-binding
   :sh06-resolution-source-artifact
   :stage2-plan-emission
   :macro-parse-expand
   :function-table
   :function-lowering
   :sh07-authenticated-request
   :sh07-structural-run
   :core-template-construction
   :core-template-verification
   :digest-resolution
   :core-resolved-template
   :core-resolved-verification
   :digest-preimage-runtime])

(def ^:private execute-phase-by-function
  {'sh07-build-core-template :core-template-construction
   'sh07-verify-core-template :core-template-verification
   'sh07-resolve-core-template :core-resolved-template
   'sh07-verify-core-resolved :core-resolved-verification
   'sh07-resolve-identity-preimage :digest-preimage-runtime
   'sh07-resolve-provenance-preimage :digest-preimage-runtime})

(def ^:private known-digest-purposes
  #{:sh07-core-node-id :sh07-core-artifact-id
    :sh07-core-provenance-binding-id})

(def ^:private cardinality-keys
  [:forms :nodes :binding-table :resolution-table :var-references
   :mutations :error-transfers :error-handlers :children :evaluated-children
   :function-records :call-edges :fragment-manifest :digest-requests])

(def ^:private default-options
  {:maximum-progress-events 256
   :maximum-cardinality-per-key 1000000
   ;; There are three declared SH-07 purposes plus the bounded :other bucket.
   :maximum-digest-purpose-rows 4
   :on-progress nil
   :runner nil})

;; `with-redefs-fn` changes root Vars.  Explicit telemetry is serialized in a
;; JVM so that one profile cannot observe another profile's temporary roots.
(def ^:private profile-lock (Object.))

(defn- fail!
  [id message facts]
  (throw
   (ex-info message
            (assoc facts
                   :id id
                   :slice :SH-07
                   :telemetry :gravity/sh07-cold-build-phase-telemetry-v1))))

(defn- normalize-options
  [supplied]
  (when-not (map? supplied)
    (fail! "SH07-COLD-TELEMETRY-OPTIONS"
           "SH-07 cold-build telemetry options must be a map"
           {:value supplied}))
  (let [unknown (seq (remove (set (keys default-options)) (keys supplied)))
        options (merge default-options supplied)]
    (when unknown
      (fail! "SH07-COLD-TELEMETRY-OPTIONS"
             "SH-07 cold-build telemetry option is not recognized"
             {:option (first (sort-by pr-str unknown))}))
    (doseq [key [:maximum-progress-events
                 :maximum-cardinality-per-key]]
      (when-not (and (integer? (get options key))
                     (pos? (get options key)))
        (fail! "SH07-COLD-TELEMETRY-OPTIONS"
               "SH-07 cold-build telemetry bound must be a positive integer"
               {:option key :value (get options key)})))
    (when-not (and (integer? (:maximum-digest-purpose-rows options))
                   (<= 4 (:maximum-digest-purpose-rows options)))
      (fail! "SH07-COLD-TELEMETRY-OPTIONS"
             "SH-07 cold-build telemetry purpose bound is below its fixed buckets"
             {:option :maximum-digest-purpose-rows
              :value (:maximum-digest-purpose-rows options)
              :minimum 4}))
    (when (and (some? (:on-progress options))
               (not (fn? (:on-progress options))))
      (fail! "SH07-COLD-TELEMETRY-OPTIONS"
             "SH-07 cold-build telemetry progress callback must be callable"
             {:option :on-progress :value (:on-progress options)}))
    (when (and (some? (:runner options))
               (not (fn? (:runner options))))
      (fail! "SH07-COLD-TELEMETRY-OPTIONS"
             "SH-07 cold-build telemetry runner must be callable"
             {:option :runner :value (:runner options)}))
    options))

(defn- bootstrap-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (fail! "SH07-COLD-TELEMETRY-SEAM"
             "Required SH-07 telemetry seam is unavailable"
             {:symbol symbol})))

(defn- empty-phase-observations
  []
  (into (sorted-map)
        (map (fn [phase]
               [phase {:call-count 0
                       :failed-call-count 0
                       :elapsed-ns 0
                       :last-status nil}])
             phase-order)))

(defn- new-state
  [options]
  {:options options
   :phase-observations (atom (empty-phase-observations))
   :digest-observations (atom (sorted-map))
   :progress-event-count (atom 0)
   :progress-truncated? (atom false)
   :callback-error-count (atom 0)})

(defn- bounded-cardinality
  [value maximum]
  (cond
    (nil? value)
    {:kind :nil :count 0 :truncated? false}

    (or (map? value) (set? value) (vector? value)
        (list? value) (seq? value)
        (instance? java.util.Collection value))
    (let [observed (bounded-count (inc maximum) value)
          truncated? (> observed maximum)]
      {:kind (cond
               (map? value) :map
               (set? value) :set
               (vector? value) :vector
               (or (list? value) (seq? value)) :sequence
               :else :collection)
       :count (if truncated? maximum observed)
       :truncated? truncated?})

    :else
    {:kind :scalar :count 1 :truncated? false}))

(defn- preimage-cardinality
  [preimage maximum]
  (let [preimage-map? (map? preimage)
        selected
        (if preimage-map?
          (into (sorted-map)
                (keep (fn [key]
                        (when (contains? preimage key)
                          [key (bounded-cardinality (get preimage key)
                                                    maximum)])))
                cardinality-keys)
          (sorted-map))
        total (reduce + 0 (map :count (vals selected)))]
    {:top-level-kind (if preimage-map? :map :non-map)
     :top-level-key-count (if preimage-map? (count preimage) 0)
     :selected selected
     :selected-entry-count total
     :truncated? (boolean (some :truncated? (vals selected)))}))

(defn- notify-progress!
  [state event]
  (let [options (:options state)
        ordinal (swap! (:progress-event-count state) inc)]
    (if (> ordinal (:maximum-progress-events options))
      (reset! (:progress-truncated? state) true)
      (when-let [callback (:on-progress options)]
        (try
          (callback (assoc event :ordinal ordinal))
          (catch Throwable _
            ;; Telemetry must not alter the result of the observed operation.
            (swap! (:callback-error-count state) inc)))))))

(defn- record-phase!
  [state phase elapsed status]
  (when (contains? (set phase-order) phase)
    (swap! (:phase-observations state)
           update phase
           (fn [current]
             (-> current
                 (update :call-count inc)
                 (update :failed-call-count
                         #(if (= :failed status) (inc %) %))
                 (update :elapsed-ns + elapsed)
                 (assoc :last-status status)))))
  (notify-progress!
   state
   {:kind :phase
    :phase phase
    :status status
    :elapsed-ns elapsed}))

(defn- observe-phase!
  [state phase operation]
  (let [started (System/nanoTime)
        status (atom :passed)]
    (try
      (operation)
      (catch Throwable error
        (reset! status :failed)
        (throw error))
      (finally
        (record-phase! state phase (- (System/nanoTime) started) @status)))))

(defn- canonical-purpose
  [purpose]
  (if (contains? known-digest-purposes purpose) purpose :other))

(defn- update-maximum-cardinality
  [old cardinality]
  (merge-with max old cardinality))

(defn- observe-digest!
  [state purpose preimage operation]
  (let [started (System/nanoTime)
        status (atom :passed)
        purpose (canonical-purpose purpose)
        cardinality (preimage-cardinality
                     preimage
                     (get-in state [:options :maximum-cardinality-per-key]))]
    (try
      (operation)
      (catch Throwable error
        (reset! status :failed)
        (throw error))
      (finally
        (let [elapsed (- (System/nanoTime) started)]
          (swap! (:digest-observations state)
                 update purpose
                 (fn [current]
                   (let [current (or current
                                     {:call-count 0
                                      :failed-call-count 0
                                      :elapsed-ns 0
                                      :selected-entry-count 0
                                      :maximum-cardinality {}})]
                     (-> current
                         (update :call-count inc)
                         (update :failed-call-count
                                 #(if (= :failed @status) (inc %) %))
                         (update :elapsed-ns + elapsed)
                         (update :selected-entry-count
                                 + (:selected-entry-count cardinality))
                         (update :maximum-cardinality
                                 update-maximum-cardinality
                                 (into (sorted-map)
                                       (map (fn [[key value]]
                                              [key (:count value)])
                                            (:selected cardinality))))))))
          (notify-progress!
           state
           {:kind :digest
            :purpose purpose
            :status @status
            :elapsed-ns elapsed
            :cardinality cardinality}))))))

(defn- execute-wrapper
  [state original]
  (fn [source-path function arguments]
    (if-let [phase (get execute-phase-by-function function)]
      (observe-phase!
       state phase
       #(original source-path function arguments))
      (original source-path function arguments))))

(defn- phase-wrapper
  [state phase original]
  (fn [& arguments]
    (observe-phase! state phase #(apply original arguments))))

(defn- digest-wrapper
  [state original]
  (fn [source-path purpose preimage resolved-digests]
    (observe-digest!
     state purpose preimage
     #(original source-path purpose preimage resolved-digests))))

(defn- wrapper-roots
  [state]
  (let [phase-seams
        {'sh07-core-build-binding! :sh07-pinned-plan-binding
         'sh06-resolution-source-artifact :sh06-resolution-source-artifact
         'p15-s23-stage2-compiler-artifact-plan :stage2-plan-emission
         'macro-source-artifact :macro-parse-expand
         'stage0-function-table :function-table
         'p15-s23-stage2-seed-compile-function :function-lowering
         'sh07-core-authenticated-request :sh07-authenticated-request
         'sh07-core-run-structural-request-for-test :sh07-structural-run
         'sh07-core-digest-requests :digest-resolution}
        phase-roots
        (into {}
              (map (fn [[symbol phase]]
                     (let [root (bootstrap-var symbol)]
                       [root (phase-wrapper state phase @root)])))
              phase-seams)
        execute-root (bootstrap-var 'sh07-core-execute!)
        digest-root (bootstrap-var 'sh07-core-resolve-digest-preimage!)]
    (assoc phase-roots
           execute-root (execute-wrapper state @execute-root)
           digest-root (digest-wrapper state @digest-root))))

(defn- result-summary
  [result]
  (cond-> {:value-kind (cond
                         (map? result) :map
                         (vector? result) :vector
                         (nil? result) :nil
                         :else :scalar)}
    (map? result)
    (merge (select-keys result [:artifact :kind :status :artifact-id]))))

(defn- delay-realized?
  []
  (if-let [root (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)]
    (realized? @root)
    false))

(defn run-profile
  "Observe one explicit SH-07 file-artifact invocation.

  `source-path` is passed to the default runner.  Tests and bounded probes may
  provide `:runner`, a function receiving a context map with `:source-path`,
  `:observe-phase`, and `:observe-digest`; this avoids launching the full
  artifact build while exercising telemetry accounting.  `:on-progress` is a
  synchronous, bounded diagnostic callback and callback failures are swallowed.
  The returned receipt is bounded and non-authoritative."
  ([source-path]
   (run-profile source-path {}))
  ([source-path supplied-options]
   (when-not (and (string? source-path) (seq source-path))
     (fail! "SH07-COLD-TELEMETRY-SOURCE"
            "SH-07 cold-build telemetry source path must be a non-empty string"
            {:source-path source-path}))
   (let [options (normalize-options supplied-options)
         runner (or (:runner options)
                    (fn [{:keys [source-path]}]
                      (bootstrap/sh07-core-file-artifact source-path)))]
     (locking profile-lock
       (let [state (new-state options)
             realized-before? (delay-realized?)
             started (System/nanoTime)
             result
             (with-redefs-fn
              (wrapper-roots state)
              #(observe-phase!
                state :sh07-core-file-artifact
                (fn []
                  (runner {:source-path source-path
                           :observe-phase
                           (fn [phase operation]
                             (observe-phase! state phase operation))
                           :observe-digest
                           (fn [purpose preimage operation]
                             (observe-digest! state purpose preimage operation))}))))
             elapsed (- (System/nanoTime) started)]
         {:schema :gravity/sh07-cold-build-phase-telemetry-v1
          :authority :non-authoritative
          :authoritative? false
          :purpose :bounded-cold-build-diagnosis
          :source-path source-path
          :cold-plan-binding-realized-before? realized-before?
          :elapsed-ns elapsed
          :elapsed-ms (/ (double elapsed) 1000000.0)
          :result-summary (result-summary result)
          :phase-observations @(:phase-observations state)
          :digest-observations @(:digest-observations state)
          :progress-event-count @(:progress-event-count state)
          :progress-truncated? @(:progress-truncated? state)
          :progress-callback-error-count @(:callback-error-count state)
          :deterministic-accounting
          [:phase-observations :digest-observations :preimage-cardinality]
          :host-variable-observations [:elapsed-ns :elapsed-ms]
          :persistent-cache-authority? false
          :semantic-result-comparison :not-performed
          :nonclaims
          [:performance :benchmark :proof :integration :release
           :self-hosting :seed-retirement :semantic-rewrite :cache-authority]})))))

(defn -main
  [& arguments]
  (when-not (= 2 (count arguments))
    (fail! "SH07-COLD-TELEMETRY-USAGE"
           "Use --source <path>"
           {:arguments (vec arguments)}))
  (when-not (= "--source" (first arguments))
    (fail! "SH07-COLD-TELEMETRY-USAGE"
           "Use --source <path>"
           {:arguments (vec arguments)}))
  (println (pr-str (run-profile (second arguments)))))
