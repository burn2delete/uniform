(ns gravity.evolution.deprecation-proposals
  "Pure GOV8 construction from explicit public inputs. Supplied references
  are not authenticated; proposals never approve deprecation or removal."
  (:require [clojure.string :as str]))

(defn- text? [x]
  (and (string? x) (not (str/blank? x))))

(defn- public-data? [x]
  (cond
    (or (nil? x) (boolean? x) (string? x) (keyword? x)) true
    ;; Exact classes exclude mutable Number implementations and subclasses.
    (contains? #{Byte Short Integer Long Float Double
                 java.math.BigInteger java.math.BigDecimal
                 clojure.lang.BigInt clojure.lang.Ratio} (class x)) true
    (and (map? x) (not (record? x)))
    (and (every? keyword? (keys x)) (every? public-data? (vals x)))
    (vector? x) (every? public-data? x)
    (set? x) (every? keyword? x)
    :else false))

(defn- public-map? [x]
  (and (map? x) (not (record? x)) (public-data? x)))

(defn- references? [x]
  (and (vector? x) (seq x) (every? text? x)))

(defn- canonical [x]
  (cond
    (map? x) (into (sorted-map) (map (fn [[k v]] [k (canonical v)])) x)
    (vector? x) (mapv canonical x)
    (set? x) (into (sorted-set) x)
    :else x))

(defn- diagnostic [id path reason]
  {:id id :message "Invalid explicit deprecation proposal input"
   :path path :reason reason})

(def ^:private subject-fields
  [[:contract text? "DEPRECATION-INPUT"]
   [:revision text? "DEPRECATION-INPUT"]
   [:surfaces #(and (set? %) (seq %) (every? keyword? %)) "DEPRECATION-INPUT"]
   [:history #(and (vector? %) (every? public-map? %)) "DEPRECATION-INPUT"]
   [:retained-tests references? "DEPRECATION-INPUT"]])

(def ^:private proposal-fields
  [[:id text? "DEPRECATION-INPUT"]
   [:owner text? "DEPRECATION-INPUT"]
   [:diagnostic text? "GOV8004"]
   [:warn-in text? "GOV8004"]
   [:error-in text? "GOV8004"]
   [:remove-in text? "GOV8004"]
   [:migration references? "DEPRECATION-INPUT"]])

(def ^:private provenance-fields
  (mapv #(vector % text? "DEPRECATION-INPUT")
        [:source-id :input-id :policy-id :tool-id]))

(defn- field-errors [data path fields]
  (into []
        (keep (fn [[field valid? id]]
                (cond
                  (not (contains? data field))
                  (diagnostic id (conj path field) :missing-field)
                  (not (valid? (get data field)))
                  (diagnostic id (conj path field) :invalid-field))))
        fields))

(defn- replacement-errors [proposal]
  (let [fields [:replacement :no-replacement-rationale]
        supplied (filterv #(contains? proposal %) fields)]
    (if (empty? supplied)
      [(diagnostic "GOV8003" [:proposal :replacement]
                   :missing-replacement-or-rationale)]
      (field-errors proposal [:proposal]
                    (mapv #(vector % text? "GOV8003") supplied)))))

(defn- argument-errors [data path fields]
  (if (public-map? data)
    (field-errors data path fields)
    [(diagnostic "DEPRECATION-INPUT" path :invalid-field)]))

(defn create-proposal
  "Construct a proposed deprecation record or payload-free diagnostics.
  Schedule/revision labels remain opaque. History and test references are
  retained data, not proof of coverage, approval or an elapsed removal window.
  See specs/evolution-workflow/tasks.md#deprecation-proposal-carrier."
  [subject proposal provenance]
  (let [proposal-errors
        (if (public-map? proposal)
          (concat (field-errors proposal [:proposal] (take 2 proposal-fields))
                  (replacement-errors proposal)
                  (field-errors proposal [:proposal] (drop 2 proposal-fields)))
          [(diagnostic "DEPRECATION-INPUT" [:proposal] :invalid-field)])
        diagnostics
        (vec (concat (argument-errors subject [:subject] subject-fields)
                     proposal-errors
                     (argument-errors provenance [:provenance] provenance-fields)))]
    (if (seq diagnostics)
      {:status :rejected :diagnostics diagnostics}
      {:status :proposed :diagnostics []
       :artifact
       (canonical
        (merge (select-keys subject (mapv first subject-fields))
               (select-keys proposal (concat (mapv first proposal-fields)
                                             [:replacement :no-replacement-rationale]))
               {:schema :gravity.evolution/deprecation-proposal-v1
                :kind :gravity/deprecation-proposal
                :status :proposed :authority :non-admitting
                :provenance (select-keys provenance (mapv first provenance-fields))}))})))
