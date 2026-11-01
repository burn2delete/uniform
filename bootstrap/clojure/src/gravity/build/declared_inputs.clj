(ns gravity.build.declared-inputs
  "Private host-entry issuer and fixed hermetic Reader input/result store.

  The explicit local build invocation calls the private issuer. Public operations
  consume its live opaque capability; supplied grants/receipts cannot mint it.
  Trusted Clojure host code, including private-Var access, is outside the source
  sandbox. This leaf grants no filesystem or runtime access."
  (:require [gravity.digest :as digest]))

(def provider
  {:id 'gravity.build/hermetic-inputs :version "1"
   :profiles #{:meta} :phase :build :capabilities #{:fs/read}
   :effects #{:build/read-file} :reader-effect :filesystem/read
   :lifetime :invocation :replay :recorded :audit :required
   :runtime-effects #{} :trust :trusted-core})

(def ^:private ^:dynamic *invocation* nil)

(defn- refuse! [reason]
  (throw (ex-info "Declared build input refused"
                  {:id "L15-CONTRACT" :reason reason})))

(defn- freeze
  "Validate native carriers before protocols/equality/hash/lookup; copy all
  containers so caller-owned native backing arrays cannot mutate retained data."
  ([value] (freeze value (java.util.IdentityHashMap.) #{clojure.lang.Keyword}))
  ([value ancestors] (freeze value ancestors #{clojure.lang.Keyword}))
  ([value ^java.util.IdentityHashMap ancestors key-classes]
   (let [kind (class value)
         plan-data? (contains? key-classes clojure.lang.Symbol)
         native-list? (and plan-data? (contains? #{clojure.lang.PersistentList
                                                  clojure.lang.PersistentList$EmptyList} kind))
         collection? (or native-list? (contains? #{clojure.lang.PersistentArrayMap
                                 clojure.lang.PersistentHashMap
                                 clojure.lang.PersistentVector
                                 clojure.lang.PersistentHashSet} kind))]
     (cond
       collection?
       (do
         (when (or (some? (meta value)) (.containsKey ancestors value))
           (refuse! :native-carrier))
         (.put ancestors value true)
         (try
           (cond
             native-list?
             (loop [remaining value out []]
               (cond
                 (or (nil? remaining) (= clojure.lang.PersistentList$EmptyList (class remaining)))
                 (apply list out)
                 (= clojure.lang.PersistentList (class remaining))
                 (do (when (some? (meta remaining)) (refuse! :native-carrier))
                     (recur (.next ^clojure.lang.PersistentList remaining)
                            (conj out (freeze (.first ^clojure.lang.PersistentList remaining)
                                              ancestors key-classes))))
                 :else (refuse! :native-carrier)))

             (or (= kind clojure.lang.PersistentArrayMap)
                 (= kind clojure.lang.PersistentHashMap))
             (reduce (fn [out [key item]]
                       (when-not (contains? key-classes (class key))
                         (refuse! :native-key))
                       (assoc out (freeze key ancestors key-classes) (freeze item ancestors key-classes)))
                     {} value)
             (= kind clojure.lang.PersistentVector)
             (mapv #(freeze % ancestors key-classes) value)
             :else (into #{} (map #(freeze % ancestors key-classes)) value))
           (finally (.remove ancestors value))))

       (= kind String)
       (do
         (loop [index 0]
           (when (< index (.length ^String value))
             (let [c (.charAt ^String value index)]
               (cond
                 (Character/isHighSurrogate c)
                 (if (and (< (inc index) (.length ^String value))
                          (Character/isLowSurrogate (.charAt ^String value (inc index))))
                   (recur (+ index 2)) (refuse! :unicode))
                 (Character/isLowSurrogate c) (refuse! :unicode)
                 :else (recur (inc index))))))
         value)

       (or (= kind clojure.lang.Keyword) (= kind clojure.lang.Symbol))
       (do (when (some? (meta value)) (refuse! :native-carrier)) value)

       (or (nil? value) (contains? #{Boolean Long} kind)
           (and plan-data? (contains? #{Integer Short Byte Double Float Character
                                       clojure.lang.BigInt java.math.BigInteger
                                       java.math.BigDecimal clojure.lang.Ratio} kind))) value
       :else (refuse! :native-carrier)))))

(defn- fields! [value fields]
  (when-not (and (map? value) (= fields (set (keys value))))
    (refuse! :fields))
  value)

(defn- text? [value] (and (= String (class value)) (not (empty? value))))
(defn- hash? [value]
  (and (= String (class value)) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))
(defn- content-hash [text] (str "sha256:" (digest/sha256-hex text)))

(defn- grant! [grant]
  (fields! grant #{:id :principal :provider :provider-version :profile :target
                  :phase :lifetime :scope :effects :capabilities :audit :replay
                  :handler :implementation :policy-id})
  (when-not (and (text? (:id grant)) (text? (:principal grant))
                 (= (:id provider) (:provider grant))
                 (= (:version provider) (:provider-version grant))
                 (= :meta (:profile grant)) (keyword? (:target grant))
                 (= :build (:phase grant)) (= :invocation (:lifetime grant))
                 (set? (:scope grant)) (every? text? (:scope grant))
                 (= #{:build/read-file} (:effects grant))
                 (= #{:fs/read} (:capabilities grant))
                 (= :required (:audit grant)) (= :recorded (:replay grant))
                 (= 'gravity.reader/read-input (:handler grant))
                 (hash? (:implementation grant)) (hash? (:policy-id grant)))
    (refuse! :grant))
  grant)

(defn- request! [context request]
  (fields! request #{:principal :provider :provider-version :profile :target
                    :phase :lifetime :handler :implementation :policy-id
                    :source-id :source-content-hash :form-id :payload-form-id
                    :span :input})
  (let [grant (:grant context)]
    (doseq [key [:principal :provider :provider-version :profile :target :phase
                 :lifetime :handler :implementation :policy-id]]
      (when-not (= (get grant key) (get request key)) (refuse! :request-binding)))
    (when-not (and (hash? (:source-id request)) (hash? (:source-content-hash request))
                   (text? (:form-id request)) (text? (:payload-form-id request))
                   (map? (:span request)) (text? (:input request))
                   (contains? (:scope grant) (:input request)))
      (refuse! :request-scope)))
  request)

(defn- live! [capability]
  (let [context *invocation*]
    (when-not (and (= Object (class capability)) context
                   (identical? capability (:capability context))
                   (identical? (Thread/currentThread) (:thread context))
                   @(:active context))
      (refuse! :inactive-or-foreign-invocation))
    context))

(defn- call-with-authorized-inputs
  "Trusted build-entry seam, not a source-callable grant API.
  The host invocation producer must authorize grant outside source data
  before calling this private function. Tests may inject that host boundary.
  Callback is trusted host control flow, never a provider supplied by source."
  [grant inputs run]
  (let [grant (grant! (freeze grant))
        inputs (freeze inputs)]
    (when-not (and (vector? inputs) (fn? run)) (refuse! :host-entry))
    (doseq [entry inputs]
      (fields! entry #{:resource :content})
      (when-not (and (text? (:resource entry)) (= String (class (:content entry))))
        (refuse! :declared-input)))
    (when-not (= (count inputs) (count (set (map :resource inputs))))
      (refuse! :duplicate-input))
    (let [store (into {} (map (juxt :resource identity)) inputs)
          capability (Object.)
          context {:capability capability :thread (Thread/currentThread)
                   :active (atom true) :grant grant :inputs store
                   :results (java.util.IdentityHashMap.)}]
      (when-not (every? #(contains? store %) (:scope grant))
        (refuse! :undeclared-input))
      (binding [*invocation* context]
        (try (run capability)
             (finally
               (reset! (:active context) false)
               (.clear ^java.util.IdentityHashMap (:results context))))))))

(defn preflight!
  "Validate the COMPLETE Reader request population before its first effect.
  No inputs are read or results recorded here. Callers must preflight all tags
  and other Reader policy constraints as well, before invoking the bridge."
  [capability requests]
  (let [requests (freeze requests) context (live! capability)]
    (when-not (vector? requests) (refuse! :request-population))
    (doseq [request requests] (request! context request))
    nil))

(defn- input-entry [context resource]
  ;; Fixed implementation-owned immutable store; no callback or filesystem.
  (get (:inputs context) resource))

(defn read-input!
  "Fixed build-only bridge target. Returns opaque receipt plus immutable DATA.
  A receipt can only be consumed with this live invocation's capability."
  [capability request]
  (let [request (freeze request) context (live! capability)
        _ (request! context request)
        entry (input-entry context (:input request))
        _ (when-not entry (refuse! :undeclared-input))
        text (:content entry)
        receipt (Object.)
        record {:artifact :gravity/declared-input-result :provider provider
                :grant (:grant context) :request request
                :input {:resource (:resource entry) :content-hash (content-hash text)}
                :result {:text text :content-hash (content-hash text)}
                :status :returned}]
    (.put ^java.util.IdentityHashMap (:results context) receipt record)
    {:receipt receipt :record record}))

(defn verify-original-result!
  "Fresh request/candidate validation against original invocation-owned result.
  Never consult the input store, call a provider, or reuse an acceptance flag."
  [capability request receipt candidate]
  (let [request (freeze request) candidate (freeze candidate)
        context (live! capability)]
    (request! context request)
    (when-not (= Object (class receipt)) (refuse! :receipt))
    (let [original (.get ^java.util.IdentityHashMap (:results context) receipt)]
      (when-not (and original (= request (:request original)) (= original candidate))
        (refuse! :original-result))
      (:result original))))
