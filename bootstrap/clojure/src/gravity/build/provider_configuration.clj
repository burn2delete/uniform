(ns gravity.build.provider-configuration
  "Pure normalization of explicit decoded inputs for one selection context.
  Local bootstrap carrier: specs/build-workflow/tasks.md, after the implementation
  region. L15 owns layer order; PKG1/PKG6 own request/deny separation.
  This product supplies no selected provider, compatibility verdict or grant.")

(def ^:private layers
  [:source :package :workspace :profile-default :compiler-default])

(defn- refuse! [reason path]
  ;; Never render submitted values, keys, locations or exception causes.
  (throw (ex-info "Invalid explicit provider configuration"
                  {:id "BUILD-PROVIDER-CONFIGURATION"
                   :reason reason :path path})))

(defn- native! [value classes path]
  (when-not (contains? classes (class value))
    (refuse! :carrier path))
  (when (some? (meta value)) (refuse! :metadata path))
  value)

(defn- fields! [value fields path]
  (native! value #{clojure.lang.PersistentArrayMap
                   clojure.lang.PersistentHashMap} path)
  (doseq [key (keys value)]
    (when-not (= clojure.lang.Keyword (class key)) (refuse! :fields path)))
  (when-let [missing (first (sort-by str (remove #(contains? value %) fields)))]
    (refuse! :missing-field (conj path missing)))
  (when-not (= fields (set (keys value))) (refuse! :fields path))
  value)

(defn- vector! [value path]
  (native! value #{clojure.lang.PersistentVector} path))

(defn- text? [value]
  (and (= String (class value))
       (not (empty? value))
       ;; UTF-16 must represent Unicode scalars. No control characters in these
       ;; public identifiers/locations; no input size or work quota is imposed.
       (loop [i 0]
         (if (= i (.length ^String value))
           true
           (let [c (.charAt ^String value i)]
             (cond
               (Character/isISOControl c) false
               (Character/isHighSurrogate c)
               (and (< (inc i) (.length ^String value))
                    (Character/isLowSurrogate (.charAt ^String value (inc i)))
                    (recur (+ i 2)))
               (Character/isLowSurrogate c) false
               :else (recur (inc i))))))))

(defn- token? [value]
  (and (text? value) (not-any? #(Character/isWhitespace ^char %) value)))

(defn- identifier! [value qualified? path]
  (when-not (and (= clojure.lang.Keyword (class value))
                 (token? (name value))
                 (not (.contains ^String (name value) "/"))
                 (if qualified? (token? (namespace value))
                     (nil? (namespace value)))
                 (or (nil? (namespace value))
                     (not (.contains ^String (namespace value) "/"))))
    (refuse! :identifier path))
  value)

(defn- location! [value path]
  (fields! value #{:file :start :end} path)
  (when-not (and (text? (:file value))
                 (= Long (class (:start value)))
                 (= Long (class (:end value)))
                 (<= 0 (:start value) (:end value)))
    (refuse! :location path))
  {:file (:file value) :start (:start value) :end (:end value)})

(defn- reference! [value path]
  (fields! value #{:id :version} path)
  (let [id (:id value) version (:version value)]
    (when-not (and (= clojure.lang.Symbol (class id))
                   (nil? (meta id))
                   (token? (namespace id)) (token? (name id))
                   (not (.contains ^String (namespace id) "/"))
                   (not (.contains ^String (name id) "/"))
                   (= String (class version))
                   (re-matches #"[0-9]+(?:\.[0-9]+)*(?:-[0-9A-Za-z.-]+)?(?:\+[0-9A-Za-z.-]+)?"
                               version))
      (refuse! :reference path))
    {:id id :version version}))

(defn- selection! [value path]
  (fields! value #{:capability :phase :provider :location} path)
  (let [capability (identifier! (:capability value) true (conj path :capability))
        phase (:phase value)]
    (when-not (and (= clojure.lang.Keyword (class phase))
                   (contains? #{:build :runtime} phase))
      (refuse! :phase (conj path :phase)))
    {:capability capability :phase phase
     :provider (reference! (:provider value) (conj path :provider))
     :location (location! (:location value) (conj path :location))}))

(defn- selections! [value path]
  (vector! value path)
  (let [entries (mapv (fn [i item] (selection! item (conj path i))) (range) value)]
    (loop [i 0 seen #{}]
      (when (< i (count entries))
        (let [key ((juxt :phase :capability) (nth entries i))]
          (when (contains? seen key) (refuse! :duplicate-selection (conj path i)))
          (recur (inc i) (conj seen key)))))
    (vec (sort-by (juxt (comp str :phase) (comp str :capability)) entries))))

(defn- declarations! [value path]
  (vector! value path)
  (->> value
       (map-indexed
        (fn [i item]
          (let [path (conj path i)]
            (fields! item #{:id :location} path)
            {:id (identifier! (:id item) true (conj path :id))
             :location (location! (:location item) (conj path :location))})))
       (sort-by (juxt (comp str :id) (comp :file :location)
                      (comp :start :location) (comp :end :location)))
       vec))

(defn- summary! [value path]
  (fields! value #{:build :runtime} path)
  (into (array-map)
        (for [phase [:build :runtime]]
          (let [entry (get value phase) path (conj path phase)]
            (fields! entry #{:request :deny} path)
            [phase {:request (declarations! (:request entry) (conj path :request))
                    :deny (declarations! (:deny entry) (conj path :deny))}]))))

(defn- layer! [name value profile target]
  (let [path [:layers name]
        default? (contains? #{:profile-default :compiler-default} name)]
    (fields! value (cond-> #{:selections :effects :capabilities}
                    default? (conj :profile :target)) path)
    (when default?
      (identifier! (:profile value) false (conj path :profile))
      (identifier! (:target value) false (conj path :target))
      (when-not (= profile (:profile value)) (refuse! :profile-binding (conj path :profile)))
      (when-not (= target (:target value)) (refuse! :target-binding (conj path :target))))
    (cond-> {:layer name
             :selections (selections! (:selections value) (conj path :selections))
             :effects (summary! (:effects value) (conj path :effects))
             :capabilities (summary! (:capabilities value) (conj path :capabilities))}
      default? (assoc :profile profile :target target))))

(defn normalize
  "Normalize the closed local carrier, retaining all five layers and denials.
  Inputs must already be public, decoded native data. No reading, evaluation,
  provider lookup, policy decision or effect-to-capability derivation occurs.
  Reject ambiguous per-layer (phase, capability) entries instead of picking one.
  Diagnostics contain only implementation-owned paths and reason keywords."
  [configuration]
  (fields! configuration #{:profile :target :layers} [])
  (let [profile (identifier! (:profile configuration) false [:profile])
        target (identifier! (:target configuration) false [:target])
        input-layers (:layers configuration)]
    (fields! input-layers (set layers) [:layers])
    {:profile profile :target target
     :layers (mapv #(layer! % (get input-layers %) profile target) layers)}))
