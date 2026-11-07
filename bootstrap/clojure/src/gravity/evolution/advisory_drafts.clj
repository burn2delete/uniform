(ns gravity.evolution.advisory-drafts
  "Pure GOV10 draft production from explicit decoded public data.
  See specs/evolution-workflow/tasks.md#security-advisory-draft-carrier.
  Supplied ownership, severity, versions and provenance are claims, not verified
  facts. No assessment, disclosure, publication or package enforcement occurs."
  (:require [clojure.string :as str]))

(defn- text? [x] (and (string? x) (not (str/blank? x))))

(defn- data-map? [x]
  (and (contains? #{clojure.lang.PersistentArrayMap
                    clojure.lang.PersistentHashMap
                    clojure.lang.PersistentTreeMap} (class x))
       (nil? (meta x))
       (every? keyword? (keys x))))

(defn- data-vector? [x]
  (and (contains? #{clojure.lang.PersistentVector
                    clojure.lang.APersistentVector$SubVector} (class x))
       (nil? (meta x))))

(defn- public-data? [x]
  (cond
    (or (nil? x) (boolean? x) (string? x) (keyword? x)
        (contains? #{Byte Short Integer Long Float Double
                     java.math.BigInteger java.math.BigDecimal
                     clojure.lang.BigInt clojure.lang.Ratio} (class x))) true
    (data-map? x) (every? public-data? (vals x))
    (data-vector? x) (every? public-data? x)
    :else false))

(defn- public-map? [x] (and (data-map? x) (public-data? x)))
(defn- texts? [x] (and (data-vector? x) (every? text? x)))
(defn- nonempty-texts? [x] (and (texts? x) (seq x)))

(defn- canonical [x]
  (cond
    (data-map? x) (into (sorted-map)
                        (map (fn [[k v]] [k (canonical v)])) x)
    (data-vector? x) (mapv canonical x)
    :else x))

(defn- diagnostic [path reason]
  {:id "GOV10006" :message "Invalid explicit advisory draft input"
   :path path :reason reason})

(def ^:private subject-fields [[:package text?] [:registry text?]])
(def ^:private provenance-fields
  [[:source-id text?] [:input-id text?] [:policy-id text?] [:tool-id text?]
   [:references nonempty-texts?]])
(def ^:private advisory-fields
  [[:id text?] [:revision text?] [:owner text?]
   [:subject data-map? subject-fields]
   [:affected-versions nonempty-texts?]
   [:patched-versions texts?]
   [:severity text?] [:impact text?] [:mitigations nonempty-texts?]
   [:provenance data-map? provenance-fields]
   [:history #(and (data-vector? %) (every? public-map? %))]])

(defn- field-errors [data path fields]
  (into []
        (mapcat
         (fn [[field valid? nested]]
           (let [field-path (conj path field)]
             (cond
               (not (contains? data field))
               [(diagnostic field-path :missing-field)]
               (not (valid? (get data field)))
               [(diagnostic field-path :invalid-field)]
               nested (field-errors (into {} (get data field)) field-path nested)
               :else []))))
        fields))

(defn create-draft
  "Construct a deterministic, non-admitting advisory draft or GOV10006 errors.
  Required declarations and retained history are preserved as public data.
  Unknown top-level fields cannot promote the draft. No input is mutated and
  no reference is resolved. Version declarations remain unauthenticated."
  [advisory]
  ;; Enumerating native map entries does not invoke a tree map's comparator.
  ;; Use our own map before field lookup/projection, including nested fields.
  (let [advisory (if (data-map? advisory) (into {} advisory) advisory)
        diagnostics
        (if-not (data-map? advisory)
          [(diagnostic [:advisory] :invalid-field)]
          (let [errors (field-errors advisory [:advisory] advisory-fields)]
            (if (or (seq errors) (public-data? advisory))
              errors
              [(diagnostic [:advisory] :invalid-field)])))]
    (if (seq diagnostics)
      {:status :rejected :diagnostics diagnostics}
      {:status :draft :diagnostics []
       :artifact
       (canonical
        {:schema :gravity.evolution/advisory-draft-v1
         :kind :gravity/security-advisory-draft
         :status :draft
         :authority :non-admitting
         :version-declarations :unauthenticated-claims
         :advisory (select-keys advisory (mapv first advisory-fields))})})))
