(ns acc08-fence
  "Owned operation-fence cut. No broker, locks, processes or default I/O ports."
  (:require [clojure.edn :as edn])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.security MessageDigest]
           [java.util UUID]))

(def domain-keys #{:domain-sha256 :participants-sha256 :protocol-config-sha256})
(def identity-keys
  (into domain-keys [:operation-id :request-sha256 :source-sha256]))
(defn- fail! [reason]
  (throw (ex-info "ACC08 operation fence refused" {:id "ACC08-ADMISSION-FENCE" :reason reason})))
(defn- require! [condition reason] (when-not condition (fail! reason)))
(defn- hex? [x] (and (string? x) (boolean (re-matches #"[0-9a-f]{64}" x))))
(defn- uuid-string? [x]
  (and (string? x)
       (try (= x (str (UUID/fromString x))) (catch IllegalArgumentException _ false))))
(defn validate-domain! [domain]
  (require! (and (map? domain) (= domain-keys (set (keys domain)))
                 (every? hex? (vals domain))) :domain)
  domain)
(defn validate-identity! [identity]
  (require! (and (map? identity) (= identity-keys (set (keys identity)))
                 (uuid-string? (:operation-id identity))
                 (every? hex? (vals (dissoc identity :operation-id)))) :identity)
  identity)
(defn domain-of [identity] (select-keys identity domain-keys))
(defn sha256 [bytes]
  (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") ^bytes bytes))))
(defn- canonical [x]
  (if (map? x) (into (sorted-map) (map (fn [[k v]] [k (canonical v)]) x)) x))
(defn- bytes-of [value] (.getBytes (str (pr-str (canonical value)) "\n") StandardCharsets/UTF_8))
(defn record-name [identity kind]
  (str "acc08-fence-" (:operation-id identity) "." (name kind) ".edn"))
(defn encode-record
  "Checksum detects corruption; it is not authentication. Trusted ports authenticate."
  [kind identity receipt-sha256]
  (validate-identity! identity)
  (require! (#{:claim :terminal} kind) :record-kind)
  (require! (if (= :claim kind) (nil? receipt-sha256) (hex? receipt-sha256)) :receipt-reference)
  (let [payload (cond-> {:schema :effects/operation-fence-v1 :kind kind :identity identity}
                  (= :terminal kind) (assoc :receipt-sha256 receipt-sha256))]
    (bytes-of {:payload payload :sha256 (sha256 (bytes-of payload))})))
(defn- restore-interrupt! [error]
  ;; Throwable causes can form cycles; use identity, never user equality.
  ;; Diagnostic traversal must not replace the original port/grant error.
  (try
    (let [seen (java.util.IdentityHashMap.)]
      (loop [cause error]
        (cond (nil? cause) nil
              (.containsKey seen cause) nil
              (instance? InterruptedException cause) (.interrupt (Thread/currentThread))
              :else (do (.put seen cause true)
                        (recur (.getCause ^Throwable cause))))))
    (catch Throwable _ nil)))
(defn- port! [ports key & arguments]
  (let [f (get ports key)]
    (require! (fn? f) :missing-port)
    (try (apply f arguments)
         (catch Throwable error (restore-interrupt! error) (throw error)))))
(defn- decode-record [entry]
  (require! (= :regular (:kind entry)) :nonregular-record)
  (let [text (try (str (.decode (doto (.newDecoder StandardCharsets/UTF_8)
                                 (.onMalformedInput CodingErrorAction/REPORT)
                                 (.onUnmappableCharacter CodingErrorAction/REPORT))
                               (ByteBuffer/wrap ^bytes (:bytes entry))))
                  (catch java.nio.charset.CharacterCodingException _ (fail! :encoding)))
        wrapped (try
                  (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
                    (let [options {:eof ::end :readers {} :default (fn [& _] (fail! :tagged-data))}
                          value (edn/read options reader)]
                      (require! (= ::end (edn/read options reader)) :extra-data)
                      value))
                  (catch RuntimeException _ (fail! :malformed-record)))
        payload (:payload wrapped) kind (:kind payload) identity (:identity payload)]
    (require! (and (map? wrapped) (= #{:payload :sha256} (set (keys wrapped)))
                   (= (:sha256 wrapped) (sha256 (bytes-of payload)))) :record-digest)
    (require! (= :effects/operation-fence-v1 (:schema payload)) :record-schema)
    (validate-identity! identity)
    (require! (and (#{:claim :terminal} kind)
                   (= (if (= kind :claim) #{:schema :kind :identity}
                          #{:schema :kind :identity :receipt-sha256}) (set (keys payload)))) :record-shape)
    (require! (or (= kind :claim) (hex? (:receipt-sha256 payload))) :receipt-reference)
    (require! (= (:name entry) (record-name identity kind)) :record-name)
    payload))
(defn- authenticate-operation! [ports identity]
  (require! (= {:status :authorized-operation :identity identity}
               (port! ports :authenticate-operation! identity)) :operation-authentication))
(defn- authenticate-terminal! [ports identity receipt]
  (require! (= {:status :terminal-in-accepted-creation-envelope
                :identity identity :receipt-sha256 receipt}
               (port! ports :authenticate-terminal! identity receipt)) :terminal-unresolved))
(defn- state! [ports domain]
  (validate-domain! domain)
  (let [entries (port! ports :read-all!)
        _ (require! (vector? entries) :store-result)
        _ (require! (= (count entries) (count (set (map :name entries)))) :duplicate-record)
        records (mapv decode-record entries)
        groups (group-by #(get-in % [:identity :operation-id]) records)]
    (into {}
      (for [[id records] groups
            :let [claims (filterv #(= :claim (:kind %)) records)
                  terminals (filterv #(= :terminal (:kind %)) records)
                  _ (require! (and (= 1 (count claims)) (<= (count terminals) 1)) :orphan-or-duplicate)
                  identity (:identity (first claims))
                  _ (require! (every? #(= identity (:identity %)) records) :identity-mismatch)
                  _ (require! (= domain (domain-of identity)) :stale-domain)
                  _ (authenticate-operation! ports identity)
                  terminal (first terminals)
                  _ (when terminal (authenticate-terminal! ports identity (:receipt-sha256 terminal)))]]
        [id {:identity identity :state (if terminal :retired :unresolved)}]))))

(defn admit!
  "Shared owner must call this at EVERY grant attempt. The transaction port
  serializes the scan and existing grant with register!/retire! publication."
  [ports domain grant!]
  (require! (fn? grant!) :grant-port)
  (port! ports :transaction! domain
    (fn []
      (require! (every? #(= :retired (:state %)) (vals (state! ports domain))) :unresolved-claim)
      (try (grant!) (catch Throwable error (restore-interrupt! error) (throw error))))))

(defn register!
  "Publish an immutable claim while actual exclusive C and L are held, before
  launch. A failed/partial write is never deleted or automatically retried."
  [ports identity custody]
  (validate-identity! identity)
  (let [domain (domain-of identity)
        result
        (port! ports :transaction! domain
          (fn []
            (let [before (state! ports domain)]
              (require! (not (contains? before (:operation-id identity))) :operation-reuse)
              (require! (every? #(= :retired (:state %)) (vals before)) :unresolved-claim)
              (authenticate-operation! ports identity)
              (require! (= {:status :owned-exclusive-C-and-L :identity identity}
                           (port! ports :assert-owned-guards! identity custody)) :guard-custody)
              (let [bytes (encode-record :claim identity nil)]
                (require! (= :published-new (port! ports :publish-new! (record-name identity :claim) bytes))
                          :publication-unconfirmed)
                (require! (= {(:operation-id identity) {:identity identity :state :unresolved}}
                             (into {} (filter #(= :unresolved (:state (val %))) (state! ports domain))))
                          :claim-readback)
                (require! (= {:status :owned-exclusive-C-and-L :identity identity}
                             (port! ports :assert-owned-guards! identity custody)) :guard-custody)
                {:status :registered :identity identity :claim-sha256 (sha256 bytes)}))))]
    (require! (= :registered (:status result)) :transaction-unconfirmed)
    result))

(defn retire!
  "Explicit correlated terminal disposition only. No expiry, exit cleanup,
  reset, delete, overwrite or recovery path exists in this owned cut."
  [ports identity receipt]
  (validate-identity! identity)
  (require! (hex? receipt) :receipt-reference)
  (let [domain (domain-of identity)
        result
        (port! ports :transaction! domain
          (fn []
            (let [current (get (state! ports domain) (:operation-id identity))]
              (require! (= {:identity identity :state :unresolved} current) :retirement-state)
              (authenticate-terminal! ports identity receipt)
              (require! (= :published-new
                           (port! ports :publish-new! (record-name identity :terminal)
                             (encode-record :terminal identity receipt))) :publication-unconfirmed)
              (require! (= :retired (get-in (state! ports domain) [(:operation-id identity) :state]))
                        :retirement-readback)
              {:status :retired :identity identity :receipt-sha256 receipt})))]
    (require! (= :retired (:status result)) :transaction-unconfirmed)
    result))
