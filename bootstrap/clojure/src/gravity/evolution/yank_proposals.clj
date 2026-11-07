(ns gravity.evolution.yank-proposals
  "Pure GOV10 proposal construction from explicit decoded public inputs.
  References and owner identity are supplied, not authenticated. No yank,
  approval, emergency exception or package-policy decision is performed."
  (:require [clojure.string :as str]))

(defn- text? [x] (and (string? x) (not (str/blank? x))))

(defn- immutable-number? [x]
  ;; Exact classes exclude mutable Number implementations and host subclasses.
  (contains? #{Byte Short Integer Long Float Double
               java.math.BigInteger java.math.BigDecimal
               clojure.lang.BigInt clojure.lang.Ratio}
             (class x)))

(defn- public-data? [x]
  (cond
    (or (nil? x) (boolean? x) (string? x) (keyword? x)
        (symbol? x) (immutable-number? x)) true
    (and (map? x) (not (record? x)))
    (and (every? keyword? (keys x)) (every? public-data? (vals x)))
    (or (vector? x) (list? x) (set? x)) (every? public-data? x)
    :else false))

(defn- ordering-key [x]
  (binding [*print-length* nil *print-level* nil *print-readably* true
            *print-meta* false *print-dup* false *print-namespace-maps* false]
    (pr-str x)))

(defn- canonical [x]
  (cond
    (map? x) (into (sorted-map) (map (fn [[k v]] [k (canonical v)])) x)
    (vector? x) (mapv canonical x)
    (list? x) (apply list (map canonical x))
    ;; Sort insertion order for hash collisions, but let value equality alone
    ;; determine membership. Unequal supported values can print identically.
    (set? x) (into #{} (sort-by ordering-key (map canonical x)))
    :else x))

(defn- diagnostic [id path reason]
  {:id id :message "Invalid explicit yank proposal input"
   :path path :reason reason})

(def ^:private identity-fields
  (mapv #(vector % text? "GOV10001") [:package :registry :version]))
(def ^:private content-fields
  (mapv #(vector % text? "GOV10002") [:content-id :provenance-ref]))
(def ^:private proposal-fields
  [[:owner text? "GOV10001"]
   [:reason text? "GOV10006"]
   [:replacement-guidance text? "GOV10006"]
   [:resolution-behavior
    #(and (map? %) (public-data? %)
          (= {:new-selection :forbidden :existing-locks :preserve} %))
    "GOV10006"]])
(def ^:private provenance-fields
  (mapv #(vector % text? "GOV10002")
        [:source-id :input-id :policy-id :tool-id]))

(defn- field-errors [data path fields map-id]
  (if-not (map? data)
    [(diagnostic map-id path :invalid-field)]
    (into []
          (keep (fn [[field valid? id]]
                  (cond
                    (not (contains? data field))
                    (diagnostic id (conj path field) :missing-field)
                    (not (valid? (get data field)))
                    (diagnostic id (conj path field) :invalid-field))))
          fields)))

(defn- history-errors [release]
  (cond
    (not (contains? release :history))
    [(diagnostic "GOV10006" [:release :history] :missing-field)]
    (not (map? (:history release)))
    [(diagnostic "GOV10006" [:release :history] :invalid-field)]
    :else
    (into []
          (mapcat
           (fn [field]
             (let [history (:history release)
                   records (get history field)
                   path [:release :history field]]
               (cond
                 (not (contains? history field))
                 [(diagnostic "GOV10006" path :missing-field)]
                 (not (vector? records))
                 [(diagnostic "GOV10006" path :invalid-field)]
                 :else
                 (mapcat
                  (fn [index record]
                    (field-errors
                     record (conj path index)
                     (concat identity-fields content-fields
                             (when (= :locks field)
                               [[:lock-id text? "GOV10001"]]))
                     "GOV10006"))
                  (range) records)))))
          [:locks :contents])))

(defn propose-yank
  "Return an input-sensitive :proposed artifact, or payload-free diagnostics.
  Complete release/history records are retained as canonical inert EDN. Only
  declared proposal/provenance fields are projected. All inputs must already
  be public; this is neither an authentication nor a secret-export boundary.
  Version is an opaque exact release label, not a resolution constraint."
  [release proposal provenance]
  (let [release-errors
        (if-not (map? release)
          [(diagnostic "GOV10001" [:release] :invalid-field)]
          (concat
           (field-errors release [:release]
                         (concat identity-fields
                                 [[:namespace text? "GOV10001"]]
                                 content-fields) "GOV10001")
           (when-not (public-data? release)
             [(diagnostic "GOV10006" [:release] :invalid-field)])
           (history-errors release)))
        diagnostics
        (vec (concat release-errors
                     (field-errors proposal [:proposal] proposal-fields "GOV10001")
                     (field-errors provenance [:provenance] provenance-fields "GOV10002")))]
    (if (seq diagnostics)
      {:status :rejected :diagnostics diagnostics}
      {:status :proposed :diagnostics []
       :artifact
       (canonical
        {:schema :gravity.evolution/yank-proposal-v1
         :kind :gravity/yank-proposal
         :status :proposed
         :authority :non-admitting
         :release release
         :proposal (select-keys proposal (mapv first proposal-fields))
         :provenance (select-keys provenance (mapv first provenance-fields))})})))
