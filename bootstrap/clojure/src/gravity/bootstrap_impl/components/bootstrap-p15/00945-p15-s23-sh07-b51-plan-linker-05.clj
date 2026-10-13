
(defn- p15-s23-sh07-b51-active-source-session
  []
  (let [{:keys [token state] :as binding-value}
        *p15-s23-sh07-b51-governed-source-session*]
    (when-not (and (= #{:token :state} (set (keys binding-value)))
                   (map? state)
                   (identical? token (:token state))
                   (identical? (Thread/currentThread) (:thread state))
                   (false? @(:finalized state)))
      (p15-s23-sh07-b51-governed-read-fail!
       :inactive-or-forged-session {}))
    state))

(defn- p15-s23-sh07-b51-governed-source-entry
  [source-ordinal]
  (let [state (p15-s23-sh07-b51-active-source-session)]
    (when-not (and (integer? source-ordinal)
                   (<= 0 source-ordinal)
                   (< source-ordinal (count (:entries state))))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-ordinal {:source-ordinal source-ordinal}))
    (nth (:entries state) source-ordinal)))

(defn- p15-s23-sh07-b51-source-event-product-id
  [event product]
  (reader-canonical-hash
   [:gravity/sh07-b51-source-event-product-id-v1
    {:ordinal (:ordinal event)
     :domain (:domain event)
     :consumer (:consumer event)
     :source-raw-sha256 (:source-raw-sha256 event)
     :product product}]))

(defn- p15-s23-sh07-b51-governed-source-event!
  [domain source-ordinal module-ordinal consumer product-fn]
  (let [state (p15-s23-sh07-b51-active-source-session)
        ordinal @(:cursor state)
        expected (nth p15-s23-sh07-b51-source-event-plan ordinal nil)
        coordinate {:ordinal ordinal :domain domain
                    :source-ordinal source-ordinal
                    :module-ordinal module-ordinal
                    :consumer consumer}]
    (when-not (= expected coordinate)
      (p15-s23-sh07-b51-governed-read-fail!
       :event-order {:expected expected :actual coordinate}))
    (let [entry (nth (:entries state) source-ordinal)
          product (product-fn entry)
          raw-sha (get-in entry [:descriptor :raw-sha256])
          event-base (assoc coordinate :source-raw-sha256 raw-sha)
          product-id
          (p15-s23-sh07-b51-source-event-product-id event-base product)
          event (assoc event-base :product-id product-id)]
      (swap! (:events state) conj event)
      (swap! (:cursor state) inc)
      product)))

(defn- p15-s23-sh07-b51-session-checked-product
  [key]
  (get @(:checked-products (p15-s23-sh07-b51-active-source-session)) key))

(defn- p15-s23-sh07-b51-cache-session-product!
  [key value]
  (let [cache (:checked-products (p15-s23-sh07-b51-active-source-session))]
    (when (contains? @cache key)
      (p15-s23-sh07-b51-governed-read-fail!
       :duplicate-checked-product {:key key}))
    (swap! cache assoc key value)
    value))

(defn- p15-s23-sh07-b51-finalize-source-session!
  []
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptors (:descriptors state)
        events @(:events state)
        counters {:acquire-count (count (:entries state))
                  :decode-count (count (:entries state))
                  :event-count (count events)
                  :finalize-count 1}]
    (when-not (and (= 13 (count descriptors))
                   (= 40 @(:cursor state) (count events))
                   (= (vec (range 40)) (mapv :ordinal events))
                   (= {:acquire-count 13 :decode-count 13
                       :event-count 40 :finalize-count 1}
                      counters)
                   (compare-and-set! (:finalized state) false true))
      (p15-s23-sh07-b51-governed-read-fail!
       :session-finalize
       {:descriptor-count (count descriptors)
        :event-count (count events)
        :cursor @(:cursor state)}))
    {:schema :gravity/sh07-b51-source-read-receipt-v3
     :role (:role state)
     :commit (:commit state)
     :tree (:tree state)
     :source-descriptors descriptors
     :source-descriptors-root (:descriptors-root state)
     :events events
     :event-root
     (reader-canonical-hash
      [:gravity/sh07-b51-source-event-root-v1 events])
     :counters counters
     :finalized true}))

(defn- p15-s23-sh07-b51-active-module-ordinal
  [source-path]
  (some (fn [{:keys [module-ordinal logical-source-path]}]
          (when (= logical-source-path source-path) module-ordinal))
        p15-s23-sh07-b51-module-specs))

(defn- p15-s23-sh07-b51-compatibility-source-record
  [source-path]
  (when *p15-s23-sh07-b51-v9-receipted-bundle-construction*
    (p15-s23-sh07-b51-governed-read-fail!
     :legacy-live-source-read-in-v9
     {:source-path source-path}))
  (let [direct (when (string? source-path) (java.io.File. source-path))
        file (if (and direct (.isFile direct))
               (.getCanonicalFile direct)
               (p15-s23-sh07-governed-source-file source-path))]
    (when-not (and file (.isFile ^java.io.File file))
      (p15-s23-sh07-b51-governed-read-fail!
       :compatibility-source-missing {:source-path source-path}))
    (let [bytes (with-open [stream (java.io.FileInputStream. ^java.io.File file)]
                  (p15-s23-sh07-b51-read-stream-bytes
                   stream 268435456 :compatibility-source))]
      {:path (.getCanonicalPath ^java.io.File file)
       :bytes bytes
       :text (p15-s23-sh07-b51-strict-utf8 bytes source-path)})))

;; Historical public name retained only so older host code fails closed rather
;; than resolving a removed Var.  It is not an accepted M3-v1 pin.
(def p15-s23-sh07-b51-expected-execution-bundle-id
  p15-s23-sh07-b51-expected-m3-id)

(def p15-s23-sh07-b51-host-pin-boundary-id
  "sha256:ecae8acf77cf8c64588aef4cad380c0a7873baab1b282398b03f532ae94b0a6e")

(def p15-s23-sh07-b51-runtime-token-boundary-id
  "sha256:844b3ea1b9bd75f416bfb51ff2c40e9a8aed1803813d6a11b3e54e44e75809ad")

(def ^:private p15-s23-sh07-b51-admitted-runtime-contexts
  (java.util.HashMap.))
(def ^:private p15-s23-sh07-b51-admitted-runtime-context-queue
  (java.lang.ref.ReferenceQueue.))
(def ^:private p15-s23-sh07-b51-registry-prune-maximum 16)
(def ^:private p15-s23-sh07-b51-maximum-external-calls 65536)

(def ^:private p15-s23-sh07-b51-runtime-authority-keys
  #{:sh07-b51-runtime-context-token
    :sh07-b51-runtime-token-boundary-id
    :sh07-b51-linked-carrier-v4
    :sh07-b51-linked-carrier-v3
    :sh07-b51-module-source-descriptors
    :sh07-b51-link-manifest
    :sh07-b51-module-plans
    :sh07-b51-execution-bundle
    :sh07-b51-external-call-trace})

(deftype ^:private P15S23Sh07B51IdentityTokenQuery
  [token ^int identity-hash]
  Object
  (hashCode [_] identity-hash)
  (equals [_ other]
    (and (instance? java.lang.ref.WeakReference other)
         (identical? token
                     (.get ^java.lang.ref.WeakReference other)))))

(alter-meta! #'->P15S23Sh07B51IdentityTokenQuery assoc :private true)

(defn- p15-s23-sh07-b51-weak-identity-token-key [token]
  (let [identity-hash (System/identityHashCode token)]
    (proxy [java.lang.ref.WeakReference]
      [token p15-s23-sh07-b51-admitted-runtime-context-queue]
      (hashCode [] identity-hash)
      (equals [other]
        (let [registered-token (proxy-super get)]
          (and registered-token
               (instance? java.lang.ref.WeakReference other)
               (identical?
                registered-token
                (.get ^java.lang.ref.WeakReference other))))))))

(defn- p15-s23-sh07-b51-token-query [token]
  (P15S23Sh07B51IdentityTokenQuery.
   token (System/identityHashCode token)))

(defn- p15-s23-sh07-b51-prune-admitted-runtime-contexts! []
  (loop [remaining p15-s23-sh07-b51-registry-prune-maximum]
    (when (pos? remaining)
      (when-let [key (.poll ^java.lang.ref.ReferenceQueue
                            p15-s23-sh07-b51-admitted-runtime-context-queue)]
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts key)
        (recur (dec remaining))))))

(defn- p15-s23-sh07-b51-register-runtime-context! [context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [token (.get ^java.lang.ref.WeakReference (:token-ref context))]
      (.put ^java.util.HashMap p15-s23-sh07-b51-admitted-runtime-contexts
            (p15-s23-sh07-b51-weak-identity-token-key token)
            (java.lang.ref.WeakReference. context))))
  context)

(defn- p15-s23-sh07-b51-runtime-context-by-token [token]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          context (when context-ref
                    (.get ^java.lang.ref.WeakReference context-ref))]
      (when (and context-ref (nil? context))
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query))
      context)))

(defn- p15-s23-sh07-b51-remove-runtime-context!
  [token context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          registered-context
          (when context-ref (.get ^java.lang.ref.WeakReference context-ref))]
      (when (identical? context registered-context)
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query)
        true))))

(defn- p15-s23-sh07-b51-unadmitted-runtime-context [runtime]
  (assoc (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         :sh07-b51-external-call-trace (atom [])))
