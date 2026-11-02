(ns gravity.build.trace-input-syntax-receipt
  "Collector for one RV1 trace control; never loads or executes the control.

  Supplied observations cannot establish genuine execution or method admission.
  D9/TEST2 and docs/verification-reuse.md govern the evidence distinction."
  (:require [clojure.edn :as edn]
            [clojure.walk :as walk])
  (:import [java.nio ByteBuffer]
           [java.nio.charset StandardCharsets]
           [java.nio.file LinkOption Path SecureDirectoryStream StandardOpenOption]))

(def selector
  'gravity.self-hosting.resolution-parent-mutation-test/actual-macro-trace-input-syntax-id-mutation-at-second-id)

(def receipt-name "trace-input-syntax-receipt.edn")

(defn- events-of [events kind]
  (filterv #(= kind (:event %)) events))

(defn- stable-entry? [event]
  (every? true? (map event [:invocation-current? :proof-transaction-absent?
                           :observed-roots-stable?])))

(defn- run-checks [events]
  (let [entries (events-of events :trace-id-enter)
        returns (events-of events :trace-id-return)
        kinds (mapv :event events)
        entry-positions (keep-indexed #(when (= :trace-id-enter %2) %1) kinds)
        return-positions (keep-indexed #(when (= :trace-id-return %2) %1) kinds)
        resolve-positions (keep-indexed #(when (= :resolve-return %2) %1) kinds)]
    {:two-id-entries (= [1 2] (mapv :ordinal entries))
     :two-id-returns (= [1 2] (mapv :ordinal returns))
     :resolve-return-counts (= [0 1] (mapv :resolve-returns entries))
     :stable-entry-context (and (= 2 (count entries)) (every? stable-entry? entries))
     :same-parent (let [enters (events-of events :macro-enter)]
                    (and (seq enters) (every? #(true? (:same-parent? %)) enters)))
     :no-delegate-throw (not-any? #{:trace-id-throw :macro-throw} kinds)
     :id-and-resolve-order
     (and (= 2 (count entry-positions)) (= 2 (count return-positions))
          (= 1 (count (filter #(< % (second entry-positions)) resolve-positions)))
          (< (first entry-positions) (first return-positions)
             (first resolve-positions) (second entry-positions) (second return-positions)))}))

(defn- observation-checks [observation]
  (let [positive (:positive-events observation)
        changed (:changed-events observation)
        positive-entries (events-of positive :trace-id-enter)
        positive-returns (events-of positive :trace-id-return)
        changed-entries (events-of changed :trace-id-enter)
        changed-returns (events-of changed :trace-id-return)
        stored (:stored-id (first positive-entries))
        position #(first (keep-indexed (fn [i event] (when (% event) i)) changed))
        second-entry (position #(and (= :trace-id-enter (:event %)) (= 2 (:ordinal %))))
        mutation (position #(= :trace-field-mutated (:event %)))
        second-return (position #(and (= :trace-id-return (:event %)) (= 2 (:ordinal %))))
        refusal (position #(and (= :macro-return (:event %))
                                (= :failed (get-in % [:report :status]))
                                (false? (get-in % [:report :checks :trace-replay-current?]))
                                (some #{:trace-replay-current} (get-in % [:report :failed-checks]))))]
    {:selected-phase (= :trace-control-complete (:phase observation))
     :selected-field (= :input-syntax-id (:field observation))
     :positive-events (every? true? (vals (run-checks positive)))
     :changed-events (every? true? (vals (run-checks changed)))
     :stored-id-and-positive-returns
     (and (string? stored) (seq stored)
          (= [stored stored] (mapv :stored-id positive-entries))
          (= [stored stored] (mapv :stored-id changed-entries))
          (= [stored stored] (mapv :result positive-returns)))
     :changed-original-id-returns
     (and (= stored (:result (first changed-returns)))
          (string? (:result (second changed-returns)))
          (not= stored (:result (second changed-returns))))
     :one-second-id-mutation
     (and (= [{:event :trace-field-mutated :ordinal 2}]
             (events-of changed :trace-field-mutated))
          (not-any? #{:trace-field-mutated} (map :event positive))
          (every? some? [second-entry mutation second-return refusal])
          (< second-entry mutation second-return refusal))
     :public-positive (= :passed (get-in observation [:public-positive-report :status]))
     :positive-macro-report
     (let [reports (events-of positive :macro-return)]
       (and (seq reports) (every? #(= :passed (get-in % [:report :status])) reports)))
     :original-refusal (and (instance? Throwable (:changed-error observation))
                            (= "C5-UNRESOLVED" (:id (ex-data (:changed-error observation)))))
     :reached-second-id (true? (:reached-second-id? observation))
     :no-earlier-refusal (false? (:earlier-gate-refusal? observation))
     :changed-slot (and (string? (:before observation)) (string? (:after observation))
                        (not= (:before observation) (:after observation)))
     :alias-restored (true? (:alias-restored? observation))
     :original-verifier (true? (:no-verifier-report-substitution? observation))
     :scope-flags (every? false? (map #(get observation %)
                                    [:callback-printer-control-implemented?
                                     :nested-child-alias-control-implemented? :whole-rv1-closed?]))}))

(defn collect
  "Dereference the selected control's observations once, after its caller returns.

  completion supplies :returned?, :counts and original :error. The selected
  test's final assertions check alias and dispatch restoration; counters must
  cover that entire test including finally. No separate dispatch observation is
  invented here. provenance supplies source pins, exact selector, request
  identity and :evidence-kind (:data-unit-control or :supplied-test-observation).

  Returns raw references, including original Throwables, plus validation facts.
  Even consistent supplied data remains constructed/unadmitted evidence."
  [observations completion provenance]
  (let [observation @observations
        counts (:counts completion)
        checks (merge
                (observation-checks observation)
                {:test-returned (true? (:returned? completion))
                 :no-test-error (nil? (:error completion))
                 :selected-selector (= selector (:selector provenance))
                 :test-counters (and (= 1 (:test counts))
                                     (integer? (:pass counts)) (pos? (:pass counts))
                                     (= 0 (:fail counts)) (= 0 (:error counts)))
                 :source-provenance
                 (and (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %))
                              ((juxt :source-commit :source-tree) provenance))
                      (string? (:test-source-sha256 provenance))
                      (re-matches #"[0-9a-f]{64}" (:test-source-sha256 provenance))
                      (string? (:request-id provenance)) (boolean (seq (:request-id provenance))))
                 :evidence-kind (contains? #{:data-unit-control :supplied-test-observation}
                                           (:evidence-kind provenance))})]
    {:schema :gravity/trace-input-syntax-observation-v1
     :selector selector
     :method-status :constructed-unadmitted
     :genuine-execution-established? false
     :cache-admission? false
     :validation (if (every? true? (vals checks)) :consistent-supplied-observation :nonpassing)
     :checks checks
     :observation observation :completion completion :provenance provenance
     :restoration-source :selected-test-assertions-including-finally}))

(defn receipt-bytes
  "Encode retained observations as readable EDN. Original Throwable references
  stay in collect's return; their standard Throwable->map records enter bytes.
  Unsupported EDN values refuse publication rather than printing #object data.
  No host capture allowance is added here; the owning method admits these bytes."
  [collected]
  (let [data (walk/postwalk #(if (instance? Throwable %) (Throwable->map %) %) collected)
        text (binding [*print-length* nil *print-level* nil *print-meta* false]
               (pr-str data))
        decoded (edn/read-string {:readers {}} text)]
    (when-not (= data decoded)
      (throw (ex-info "Trace observation cannot round-trip as EDN"
                      {:id "TRACE-RECEIPT-EDN"})))
    (.getBytes (str text "\n") StandardCharsets/UTF_8)))

(defn publish!
  "Write one fixed relative receipt with CREATE_NEW through the owning method's
  retained SecureDirectoryStream. Caller owns the private directory, its custody,
  lifecycle admission and eventual read/validation. This function closes only its
  new channel. admit-bytes! must enforce the owning method's existing capture
  contract; called before creation, with encoded byte count. Its refusal escapes.

  No overwrite, partial-file deletion, retry, directory close, process work or
  cleanup-success claim. On IO failure the original exception escapes and any
  partial CREATE_NEW output remains for the owner to record as failed."
  [^SecureDirectoryStream directory collected admit-bytes!]
  (when-not (instance? SecureDirectoryStream directory)
    (throw (ex-info "Trace receipt requires an owner-held secure directory handle"
                    {:id "TRACE-RECEIPT-CUSTODY"})))
  (let [bytes (receipt-bytes collected)
        relative (Path/of receipt-name (make-array String 0))]
    (admit-bytes! (alength bytes))
    (with-open [channel (.newByteChannel directory relative
                                        #{StandardOpenOption/CREATE_NEW StandardOpenOption/WRITE
                                          LinkOption/NOFOLLOW_LINKS}
                                        (make-array java.nio.file.attribute.FileAttribute 0))]
      (let [buffer (ByteBuffer/wrap bytes)]
        (while (.hasRemaining buffer) (.write channel buffer))))
    {:receipt-name receipt-name :byte-count (alength bytes)
     :validation (:validation collected) :method-status :constructed-unadmitted}))
