(ns gravity.build.trace-semantic-delivery
  "Selected RV child output and inside-lease semantic correlation. No launcher,
  namespace admission, lock/lease disposition, parent exit or cleanup authority."
  (:require [clojure.edn :as edn]
            [gravity.build.trace-input-syntax-child :as child]
            [gravity.build.trace-input-syntax-receipt :as receipt]
            [gravity.development-test-runner :as development])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.channels Channels]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption Path SecureDirectoryStream StandardOpenOption]
           [java.nio.file.attribute PosixFilePermissions]
           [java.util UUID]))

(def ^:private selected
  {:namespaces [(namespace receipt/selector)] :test-vars [(str receipt/selector)] :fail-fast? false})

(defn- require! [condition field]
  (when-not condition
    (throw (ex-info "Selected trace semantic delivery refused"
                    {:id "RV-TRACE-SEMANTIC-DELIVERY" :field field
                     :genuine-execution-established? false :cache-admission? false}))))

(defn- request-id! [request expected]
  ;; A canonical UUID is the normal delivery request's existing identity, not
  ;; an input-size allowance or a caller-controlled relative pathname.
  (let [id (:id request)]
    (require! (string? id) :request-id)
    (require! (= id (str (UUID/fromString id))) :request-id)
    (require! (= selected (:selection request)) :selection)
    (require! (= (:source expected) (:source request)) :source)
    id))

(defn- provenance [request expected]
  (merge (:source request)
         {:request-id (:id request) :selector receipt/selector
          :test-source-sha256 (:test-source-sha256 expected)
          :evidence-kind (:evidence-kind expected)}))

(defn invoke-child!
  "Independent child-local portion of the proposed selected caller. output-root
  is an owner-established private absolute directory on the cooperative host;
  authentic root identity/permissions and absence of hostile same-user path
  replacement are caller obligations, not proved by this function.

  The child creates a NEW0700 request directory, owns its SecureDirectoryStream,
  and invokes exact child96 with the actual observations IDeref and existing
  byte-admission function. completion-sink must be constructed INSIDE this child
  by the common completion writer API; no Java handle/function crosses a JVM.
  The required common public writer factory is not yet exported by252.

  No normal-result framing or private validator is copied here. The child
  transport must propagate the binding's nonpassing/IO facts, not just exit0.
  Root and request authenticity, guards, canonical L and all process lifecycle
  remain outside this function. No existing directory/output is reused/deleted."
  [^Path output-root request expected observations completion-sink admit-bytes!]
  (require! (and (instance? Path output-root) (.isAbsolute output-root)) :output-root)
  (require! (and (ifn? completion-sink) (ifn? admit-bytes!)) :child-local-ports)
  (let [id (request-id! request expected)
        path (.resolve output-root id)
        _ (Files/createDirectory path
                                 (into-array java.nio.file.attribute.FileAttribute
                                             [(PosixFilePermissions/asFileAttribute
                                               (PosixFilePermissions/fromString "rwx------"))]))
        directory (Files/newDirectoryStream path)]
    (when-not (instance? SecureDirectoryStream directory)
      (.close directory)
      (require! false :secure-directory-provider))
    (let [outcome (try
                    {:result (binding [development/*machine-result-sink* completion-sink]
                               (child/invoke! observations directory admit-bytes!
                                              (provenance request expected)))}
                    (catch Throwable error {:error error}))
          close-error (try (.close directory) nil (catch Throwable error error))]
      (if-let [original (:error outcome)]
        (do (when (and close-error (not (identical? original close-error)))
              (.addSuppressed ^Throwable original ^Throwable close-error))
            (throw original))
        (do (when close-error (throw close-error))
            (:result outcome))))))

(defn correlate
  "Correlate the RV-specific record with the request and machine result already
  read/normalized by common252. This is additional semantic-record validation,
  NOT a copy of the common private closed-child validator or source attestation.
  False/unknown cleanup never enters this result. Matched DATA remains DATA."
  [request expected machine record]
  (request-id! request expected)
  (require! (and (map? machine) (= development/machine-result-schema (:schema machine))) :common-result)
  (require! (and (map? record)
                 (= #{:schema :selector :method-status :genuine-execution-established?
                      :cache-admission? :validation :checks :observation :completion
                      :provenance :restoration-source} (set (keys record)))) :semantic-record)
  (require! (= :gravity/trace-input-syntax-observation-v1 (:schema record)) :schema)
  (require! (= receipt/selector (:selector record)) :selector)
  (require! (and (= :constructed-unadmitted (:method-status record))
                 (false? (:genuine-execution-established? record))
                 (false? (:cache-admission? record))) :authority)
  (require! (= (provenance request expected) (:provenance record)) :provenance)
  (require! (= :selected-test-assertions-including-finally (:restoration-source record)) :restoration-source)
  (let [completion (:completion record)
        passed? (= :passed (:outcome machine))]
    ;; Correlation is full-value equality with the common API's actual result;
    ;; do not reconstruct a substitute machine result from receipt booleans.
    (require! (= [machine] (:machine-results completion)) :machine-result)
    (require! (= (:summary machine) (:counts completion)) :counters)
    (require! (and (true? (:returned? completion))
                   (= (:exit-code machine) (:run-return completion))
                   (nil? (:run-error completion))) :run-completion)
    (require! (= (if passed? :consistent-supplied-observation :nonpassing)
                 (:validation record)) :semantic-outcome)
    (when passed?
      (require! (and (seq (:checks record)) (every? true? (vals (:checks record))))
                :semantic-checks)
      (require! (and (nil? (:error completion)) (empty? (:failure-reports completion))) :reported-errors)
      (require! (and (= :trace-control-complete (get-in record [:observation :phase]))
                     (= :input-syntax-id (get-in record [:observation :field]))
                     (true? (get-in record [:observation :alias-restored?]))
                     (true? (get-in record [:observation :reached-second-id?]))
                     (false? (get-in record [:observation :earlier-gate-refusal?]))
                     (= :passed (get-in record [:observation :public-positive-report :status]))
                     (true? (get-in record [:observation :no-verifier-report-substitution?]))
                     (false? (get-in record [:observation :whole-rv1-closed?]))
                     (= "C5-UNRESOLVED" (get-in record [:observation :changed-error :via 0 :data :id])))
                :selected-observation))
    {:correlation (if passed? :matched-supplied-observation :matched-nonpassing-observation)
     :record record :request request :method-status :constructed-unadmitted
     :genuine-execution-established? false :cache-admission? false}))

(defn- close-semantic-resource [outcome resource]
  (try
    (.close ^java.io.Closeable resource)
    outcome
    (catch Throwable close-error
      (if-let [primary (:error outcome)]
        (do (when-not (or (identical? primary close-error)
                          (some #(identical? close-error %) (.getSuppressed ^Throwable primary)))
              (.addSuppressed ^Throwable primary close-error))
            outcome)
        {:error close-error}))))

(defn read-correlated!
  "Read one bounded strict-UTF8 EDN semantic receipt through the PARENT's own
  retained private root handle. Both child directory and receipt are relative
  NOFOLLOW opens. Caller supplies its existing capture allowance, unchanged.
  No directory/file removal, retry, lease release or cleanup success claim."
  [^SecureDirectoryStream root request expected machine limit-bytes]
  (require! (instance? SecureDirectoryStream root) :parent-root)
  (require! (and (integer? limit-bytes) (pos? limit-bytes)
                 (< limit-bytes Integer/MAX_VALUE)) :capture-representability)
  (let [id (request-id! request expected)
        directory (.newDirectoryStream root (Path/of id (make-array String 0))
                                       (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        outcome
        (try
          (let [input (Channels/newInputStream
                       (.newByteChannel directory (Path/of receipt/receipt-name (make-array String 0))
                                        #{StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS}
                                        (make-array java.nio.file.attribute.FileAttribute 0)))
                outcome
                (try
                  (let [bytes (.readNBytes input (int limit-bytes))
                        _ (require! (= -1 (.read input)) :capture-limit)
                        decoder (doto (.newDecoder StandardCharsets/UTF_8)
                                  (.onMalformedInput CodingErrorAction/REPORT)
                                  (.onUnmappableCharacter CodingErrorAction/REPORT))
                        text (str (.decode decoder (ByteBuffer/wrap bytes)))
                        reject (fn [& _] (require! false :tagged-EDN))
                        record (with-open [reader (PushbackReader. (StringReader. text))]
                                 (let [eof (Object.) options {:eof eof :readers {} :default reject}
                                       record (edn/read options reader)]
                                   (require! (and (not (identical? eof record))
                                                  (identical? eof (edn/read options reader))) :one-record)
                                   record))]
                    {:result (correlate request expected machine record)})
                  (catch Throwable error {:error error}))]
            ;; Close input then directory, retaining the identical primary and
            ;; each distinct cleanup failure in that source-defined order.
            (close-semantic-resource outcome input))
          (catch Throwable error {:error error}))
        outcome (close-semantic-resource outcome directory)]
    (if-let [error (:error outcome)] (throw error) (:result outcome))))

(defn inside-lease-callback
  "Selected callback for common252 :inside-lease-report!, not a lease wrapper.
  The common API owns strict machine read/normalization and later disposition.
  Returned callback only reads/correlates the semantic file before that phase;
  its value cannot replace the common result. Original throw paths record their
  partial/error facts without reading a nonexistent child receipt. Audit is
  parent-local, initially nil, and retains actual values/errors. No terminal
  assessment, canonical-L release or parent report is invented here."
  [root expected limit-bytes audit]
  (fn [observation]
    (require! (compare-and-set! audit nil {:state :observing}) :callback-cardinality)
    (try
      (if (= :threw (:tag observation))
        (reset! audit {:state :not-observed :observation observation :error (:error observation)})
        (do
          (require! (= :returned (:tag observation)) :callback-outcome)
          (require! (= (namespace receipt/selector) (str (get-in observation [:job :namespace]))) :callback-namespace)
          (let [result (read-correlated! root (:delivery-request observation) expected
                                         (get-in observation [:result :development-result]) limit-bytes)]
            (reset! audit {:state :correlated :observation observation :semantic result}))))
      (catch Throwable error
        (reset! audit {:state :refused :observation observation :error error})
        (throw error)))))
