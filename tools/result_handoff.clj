(ns result-handoff
  "Read-only, explicitly selected projections of retained EDN evidence.
  This tool does not execute commands, classify acceptance, or publish anything."
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files LinkOption OpenOption Path StandardOpenOption]
           [java.security MessageDigest]))

(defn reject [message] (throw (ex-info message {:kind :invalid-handoff})))
(defn sha256 [bytes]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.digest (MessageDigest/getInstance "SHA-256") bytes))))
(def max-evidence-bytes (* 16 1024 1024))
(defn read-bounded-bytes [^java.nio.channels.ReadableByteChannel channel limit]
  ;; Enforce the capture bound on actual reads, including growth after open.
  ;; Read at most one byte beyond the limit to distinguish EOF from overflow.
  (let [buffer (ByteBuffer/allocate 8192)
        output (ByteArrayOutputStream.)]
    (loop [captured 0]
      (.clear buffer)
      (.limit buffer (int (min (.capacity buffer) (inc (- limit captured)))))
      (let [n (.read channel buffer)]
        (cond
          (= -1 n) (.toByteArray output)
          (> (+ captured n) limit) (reject "Evidence exceeds the reader's host capture limit")
          :else (do (.write output (.array buffer) 0 n)
                    (recur (+ captured n))))))))
(defn read-document [file]
  ;; Parse and hash the same captured bytes; never reopen for provenance.
  (let [path (Path/of (str file) (make-array String 0))
        options (into-array OpenOption [StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS])
        bytes (with-open [channel (Files/newByteChannel path options)]
                (read-bounded-bytes channel max-evidence-bytes))
        decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))
        text (str (.decode decoder (ByteBuffer/wrap bytes)))
        eof (Object.)
        value (with-open [reader (PushbackReader. (StringReader. text))]
                (let [opts {:eof eof :readers {} :default (fn [& _] (reject "Tagged EDN is unsupported"))}
                      value (edn/read opts reader)]
                  (when (or (identical? eof value)
                            (not (identical? eof (edn/read opts reader))))
                    (reject "Evidence must contain exactly one EDN value"))
                  value))]
    {:sha256 (sha256 bytes) :bytes (alength bytes) :value value}))

(defn token? [value]
  (and (string? value) (boolean (re-matches #"[A-Za-z0-9][A-Za-z0-9._/+>=:-]*" value))
       (not (str/includes? value "://"))
       (not (re-find #"(?i)(secret|password|credential|token=)" value))))
(defn digest? [value n]
  (and (string? value) (boolean (re-matches (re-pattern (str "[0-9a-f]{" n "}")) value))))
(defn typed-value [type value]
  (case type
    :oid (if (or (digest? value 40) (digest? value 64)) value (reject "Invalid Git identity"))
    :sha256 (if (digest? value 64) value (reject "Invalid SHA256 identity"))
    :integer (if (and (integer? value) (<= 0 value)) value (reject "Invalid nonnegative measurement"))
    :boolean (if (boolean? value) value (reject "Invalid boolean observation"))
    :status (if (and (keyword? value) (nil? (namespace value)) (token? (name value)))
              value (reject "Invalid status observation"))
    :selector (if (and (string? value) (token? value)) value (reject "Invalid selector"))
    :clean-status (if (string? value) (empty? value) (reject "Invalid Git status observation"))
    :argv-digest (if (and (vector? value) (seq value) (every? string? value))
                   {:sha256 (sha256 (.getBytes (pr-str value) StandardCharsets/UTF_8))
                    :argument-count (count value)}
                   (reject "Invalid argv observation"))
    :counts (if (and (map? value) (seq value)
                     (every? #{:test :tests :pass :fail :error :assertions :failures :errors} (keys value))
                     (every? #(and (integer? %) (<= 0 %)) (vals value)))
              (into (sorted-map) value) (reject "Invalid test counts"))
    (reject "Unsupported projection type")))

(defn select-value [value path]
  (reduce (fn [node key]
            (cond
              (and (map? node) (contains? node key)) (get node key)
              (and (vector? node) (integer? key) (<= 0 key) (< key (count node))) (nth node key)
              :else (reject "Selected receipt field is missing"))) value path))

(defn project
  "Only plan-selected typed values leave the private evidence snapshot.
  All evidence remains attributed producer reporting, including exit codes."
  [plan load-document]
  (when-not (and (= #{:receipts :fields} (set (keys plan)))
                 (vector? (:receipts plan)) (seq (:receipts plan))
                 (vector? (:fields plan)) (seq (:fields plan)))
    (reject "Plan requires receipts and explicit fields"))
  (let [ids (mapv :id (:receipts plan))
        labels (mapv :label (:fields plan))]
    (when-not (and (every? token? ids) (= (count ids) (count (set ids)))
                   (every? token? labels) (= (count labels) (count (set labels))))
      (reject "Receipt IDs and field labels must be unique public tokens"))
    (let [documents
          (into (sorted-map)
                (for [{:keys [id file sha256] :as receipt} (:receipts plan)]
                  (do
                    (when-not (and (= #{:id :file :sha256} (set (keys receipt)))
                                   (string? file) (digest? sha256 64))
                      (reject "Receipt requires a file and expected SHA256"))
                    (let [document (load-document file)]
                      (when-not (= sha256 (:sha256 document))
                        (reject "Retained receipt digest mismatch"))
                      [id document]))))
          fields
          (mapv (fn [{:keys [label receipt path type] :as field}]
                  (when-not (and (= #{:label :receipt :path :type} (set (keys field)))
                                 (contains? documents receipt) (vector? path) (seq path)
                                 (every? #(or (keyword? %) (and (integer? %) (<= 0 %))) path))
                    (reject "Field requires a known receipt and explicit key/index path"))
                  {:label label :receipt receipt :path path :type type
                   :value (typed-value type (select-value (get-in documents [receipt :value]) path))})
                (:fields plan))]
      {:schema :result-handoff-v1 :authority :none :new-execution false
       :observation-source :retained-producer-receipts
       :receipts (mapv (fn [[id doc]] (assoc (select-keys doc [:sha256 :bytes]) :id id)) documents)
       :fields fields})))

(defn -main [& args]
  (when-not (= 1 (count args))
    (reject "Usage: clojure -Sdeps '{:paths [\"tools\"]}' -M -m result-handoff PLAN.edn"))
  (let [plan (read-document (first args))
        result (project (:value plan) read-document)]
    (prn (assoc result :plan-sha256 (:sha256 plan)))))

;; Library loading has no CLI dispatch. Invoke explicitly with -m result-handoff.
