(ns gravity.reader.dispatch
  "Bootstrap handler dispatch for already-validated text syntax seeds (C2/L12).

  Policy selects reviewed implementations, never caller functions or purity
  assertions. Providers are trusted host bindings, supplied separately from
  policy. This leaf does not parse source or authenticate Reader products."
  (:require [clojure.set :as set]
            [clojure.instant :as instant]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]))

(defn- reject! [reason]
  (throw (ex-info "Reader extension rejected"
                  {:id "C2-EXTENSION" :reason reason})))

(defn- native-map! [value key-class]
  ;; Check classes before calling collection protocols or consulting keys.
  (when-not (and (#{clojure.lang.PersistentArrayMap
                   clojure.lang.PersistentHashMap} (class value))
                 (nil? (meta value))
                 (every? #(and (= key-class (class %)) (nil? (meta %)))
                         (keys value)))
    (reject! :native-map))
  value)

(defn- exact! [value fields]
  (native-map! value clojure.lang.Keyword)
  (when-not (= fields (set (keys value))) (reject! :fields))
  value)

(defn- text! [value]
  (when-not (= String (class value))
    (reject! :text))
  ;; Do not silently replace unpaired UTF-16 surrogates at a host boundary.
  (loop [index 0]
    (when (< index (.length ^String value))
      (let [c (.charAt ^String value index)]
        (cond
          (Character/isHighSurrogate c)
          (if (and (< (inc index) (.length ^String value))
                   (Character/isLowSurrogate (.charAt ^String value (inc index))))
            (recur (+ index 2))
            (reject! :unicode))
          (Character/isLowSurrogate c) (reject! :unicode)
          :else (recur (inc index))))))
  value)

(defn- scalar-set! [value item-class]
  (when-not (and (= clojure.lang.PersistentHashSet (class value))
                 (nil? (meta value))
                 (every? #(and (= item-class (class %)) (nil? (meta %))) value))
    (reject! :scalar-set))
  value)

(defn- read-text [seed _provider]
  ;; The pure implementation has no provider reference or ambient lookup.
  seed)

(defn- read-input [seed provider]
  (let [value ((:invoke provider) (:datum seed))]
    (when-not (= String (class value)) (reject! :provider-result))
    ;; Empty resource contents are legal text.
    (when (seq value) (text! value))
    (assoc seed :datum value)))

(defn- read-inst [seed _provider]
  (assoc seed :datum (instant/read-instant-date (:datum seed))))

(defn- read-uuid [seed _provider]
  (assoc seed :datum (java.util.UUID/fromString (:datum seed))))

(def ^:private handlers
  {'gravity.reader/read-text
   {:execute read-text :effects #{} :capabilities #{}}
   'gravity.reader/read-input
   {:execute read-input :effects #{:filesystem/read} :capabilities #{:fs/read}}
   'gravity.reader.standard/read-inst
   {:execute read-inst :effects #{} :capabilities #{}}
   'gravity.reader.standard/read-uuid
   {:execute read-uuid :effects #{} :capabilities #{}}})

(defn dispatch
  "Execute one policy-registered tag on an already-validated text seed.

  provider is nil for pure calls. An effectful call requires an exact build
  grant for the selected handler, provider and input. Only the reviewed
  bootstrap handlers above are executable; this is not arbitrary code loading."
  [policy tag seed provider]
  (exact! policy #{:id :profile :registry :enabled-tags :build-effects :capabilities})
  (text! (:id policy))
  (when-not (= clojure.lang.Keyword (class (:profile policy))) (reject! :profile))
  (native-map! (:registry policy) clojure.lang.Symbol)
  (scalar-set! (:enabled-tags policy) clojure.lang.Symbol)
  (scalar-set! (:build-effects policy) clojure.lang.Keyword)
  (scalar-set! (:capabilities policy) clojure.lang.Keyword)
  (when-not (and (= clojure.lang.Symbol (class tag)) (nil? (meta tag)))
    (reject! :tag))
  (let [entry (get (:registry policy) tag)]
    (when-not entry (reject! :unknown-tag))
    (when-not (contains? (:enabled-tags policy) tag) (reject! :disallowed-tag))
    (exact! entry #{:handler :build-effects :capabilities :profiles})
    (when-not (and (= clojure.lang.Symbol (class (:handler entry)))
                   (nil? (meta (:handler entry)))) (reject! :handler))
    (scalar-set! (:build-effects entry) clojure.lang.Keyword)
    (scalar-set! (:capabilities entry) clojure.lang.Keyword)
    (scalar-set! (:profiles entry) clojure.lang.Keyword)
    (let [handler (get handlers (:handler entry))]
      (when-not handler (reject! :unknown-handler))
      (when-not (contains? (:profiles entry) (:profile policy))
        (reject! :disallowed-profile))
      ;; Implementation-owned effects cannot be erased by registry metadata.
      (when-not (and (set/subset? (:effects handler) (:build-effects entry))
                     (set/subset? (:build-effects entry) (:build-effects policy)))
        (reject! :undeclared-effect))
      (when-not (and (set/subset? (:capabilities handler) (:capabilities entry))
                     (set/subset? (:capabilities entry) (:capabilities policy)))
        (reject! :missing-capability))
      (exact! seed #{:source-id :form-id :span :datum})
      (text! (:source-id seed))
      (text! (:form-id seed))
      (text! (:datum seed))
      (when (seq (:effects handler))
        (exact! provider #{:id :grant :invoke})
        (text! (:id provider))
        (when-not (fn? (:invoke provider)) (reject! :provider))
        (exact! (:grant provider)
                #{:id :principal :provider :phase :lifetime :capability :input})
        (text! (get-in provider [:grant :id]))
        (text! (get-in provider [:grant :input]))
        (when-not (and (= (:handler entry) (get-in provider [:grant :principal]))
                       (= (:id provider) (get-in provider [:grant :provider]))
                       (= :build (get-in provider [:grant :phase]))
                       (= :invocation (get-in provider [:grant :lifetime]))
                       (= :fs/read (get-in provider [:grant :capability]))
                       (= (:datum seed) (get-in provider [:grant :input])))
          (reject! :grant)))
      (let [result ((:execute handler) seed
                    (when (seq (:effects handler)) provider))]
        {:value result
         :invocation {:artifact :gravity/reader-extension-invocation
                      :policy-id (:id policy) :tag tag :handler (:handler entry)
                      :policy-selection {:entry entry
                                         :enabled? true
                                         :build-effects (:build-effects policy)
                                         :capabilities (:capabilities policy)}
                      :profile (:profile policy)
                      :source-id (:source-id seed) :form-id (:form-id seed)
                      :span (:span seed)
                      :build-effects (:effects handler)
                      :capabilities (:capabilities handler)
                      :provider-id (when (seq (:effects handler)) (:id provider))
                      :grant-id (when (seq (:effects handler))
                                  (get-in provider [:grant :id]))
                      :status :invoked}}))))

(defn- canonical-hash [value]
  ;; Use exactly the existing hosted C2 policy encoding, not a new policy id.
  (letfn [(normalize [value]
            (canonical/reader-canonical-value normalize value))]
    (canonical/reader-canonical-hash digest/sha256-hex normalize value)))

(defn- standard-policy! [source-unit reader-policy]
  (native-map! source-unit clojure.lang.Keyword)
  (exact! reader-policy #{:policy :version :registered-tags :ambient-authority})
  ;; Validate fixed native contents before hashing or consulting registrations.
  (let [tags (:registered-tags reader-policy)]
    (when-not (and (= clojure.lang.PersistentVector (class tags))
                   (nil? (meta tags))
                   (= clojure.lang.Keyword (class (:policy reader-policy)))
                   (= Long (class (:version reader-policy)))
                   (= clojure.lang.Keyword (class (:ambient-authority reader-policy)))
                   (every? #(and (= clojure.lang.Symbol (class %))
                                 (nil? (meta %))) tags)
                   (= reader-policy
                      {:policy :gravity/standard-reader :version 1
                       :registered-tags ['inst 'uuid] :ambient-authority :denied}))
      (reject! :standard-policy)))
  (let [options (:reader-options source-unit)
        identity (:identity-inputs source-unit)
        policy-id (canonical-hash reader-policy)]
    (native-map! options clojure.lang.Keyword)
    (native-map! identity clojure.lang.Keyword)
    (native-map! (:reader-options identity) clojure.lang.Keyword)
    (doseq [value [(:extension-policy source-unit)
                   (:extension-policy options)
                   (:extension-policy identity)
                   (get-in identity [:reader-options :extension-policy])]]
      (when-not (and (= String (class value)) (= policy-id value))
        (reject! :policy-hash)))
    (doseq [value [(:source-id source-unit) (:bytes-hash source-unit)]]
      (when-not (and (= String (class value))
                     (re-matches #"sha256:[0-9a-f]{64}" value))
        (reject! :source-identity)))
    {:id policy-id :profile :meta
                  :registry {'inst {:handler 'gravity.reader.standard/read-inst
                                    :build-effects #{} :capabilities #{} :profiles #{:meta}}
                             'uuid {:handler 'gravity.reader.standard/read-uuid
                                    :build-effects #{} :capabilities #{} :profiles #{:meta}}}
                  :enabled-tags (set (:registered-tags reader-policy))
                  :build-effects #{} :capabilities #{}}))

(defn validate-standard-tags!
  "Preflight the complete parsed tag population before any handler call."
  [source-unit reader-policy tags]
  (let [policy (standard-policy! source-unit reader-policy)]
    (doseq [tag tags]
      (when-not (and (= clojure.lang.Symbol (class tag)) (nil? (meta tag))
                     (contains? (:enabled-tags policy) tag))
        (reject! :unknown-tag)))
    policy))

(defn- span! [span]
  (exact! span #{:start :end :byte-start :byte-end})
  (doseq [position [(:start span) (:end span)]]
    (exact! position #{:line :column :column-unit :char :byte})
    (when-not (= :unicode-scalar (:column-unit position)) (reject! :span))
    (doseq [key [:line :column :char :byte]]
      (when-not (#{Long Integer clojure.lang.BigInt java.math.BigInteger}
                  (class (get position key)))
        (reject! :span))))
  (doseq [value [(:byte-start span) (:byte-end span)]]
    (when-not (#{Long Integer clojure.lang.BigInt java.math.BigInteger} (class value))
      (reject! :span))))

(defn dispatch-standard-tag
  "Normal SH03 host-decoding seam, after the existing source/result checks.

  This supports the existing standard inst/uuid surface only. The observation
  has source/policy/input/result hashes but is not an authenticated product or
  an authority token. No provider or caller-selected handler enters this seam."
  [source-unit reader-policy tag form payload]
  (let [policy (validate-standard-tags! source-unit reader-policy [tag])
        policy-id (:id policy)]
    (native-map! form clojure.lang.Keyword)
    (text! (:form-id form))
    (text! payload)
    (span! (:span form))
    (let [seed {:source-id (:source-id source-unit) :form-id (:form-id form)
                :span (:span form) :datum payload}
          input-hash (canonical-hash {:domain :gravity/reader-extension-input-v1
                                      :policy-id policy-id :tag tag :seed seed})
          result (dispatch policy tag seed nil)
          observation (assoc (:invocation result)
                             :authority :unsealed-bootstrap-observation
                             :source-content-hash (:bytes-hash source-unit)
                             :input-hash input-hash
                             :result-hash
                             (canonical-hash {:domain :gravity/reader-extension-result-v1
                                              :input-hash input-hash :value (:value result)}))]
      {:value (get-in result [:value :datum])
       :observation observation})))
