(ns gravity.evolution.change-sketches
  "Initial GOV1 records only; see the owned change sketch carrier in tasks.md."
  (:require [clojure.string :as str]))

(def ^:private change-fields
  [[:id :text] [:revision :text] [:title :text] [:owner :text]
   [:problem-statement :text] [:scope :keywords]
   [:affected-surfaces :keywords] [:affected-documents :documents]])

(def ^:private provenance-fields
  [[:source-id :text] [:input-id :text] [:policy-id :text] [:tool-id :text]])

(defn- text? [x]
  (and (string? x) (not (str/blank? x))))

(defn- field-reason [kind value]
  (case kind
    :text (cond (not (string? value)) :invalid-type
                (str/blank? value) :blank-field)
    :keywords (cond (not (set? value)) :invalid-type
                    (empty? value) :empty-field
                    (not-every? keyword? value) :invalid-member)
    :documents (cond (not (vector? value)) :invalid-type
                     (empty? value) :empty-field
                     (not-every? text? value) :invalid-member)))

(defn- diagnostic [path reason]
  {:id "GOV1001" :message "Invalid explicit change sketch input"
   :path path :reason reason})

(defn- field-errors [data argument fields]
  (if-not (and (map? data) (not (record? data)))
    [(diagnostic [argument] :invalid-type)]
    (into []
          (keep (fn [[field kind]]
                  (when-let [reason (if (contains? data field)
                                      (field-reason kind (get data field))
                                      :missing-field)]
                    (diagnostic [argument field] reason))))
          fields)))

(defn- canonical [x]
  (cond
    (map? x) (into (sorted-map) (map (fn [[k v]] [k (canonical v)])) x)
    (set? x) (into (sorted-set) x)
    (vector? x) (mapv canonical x)
    :else x))

(defn create-sketch
  "Construct a deterministic, non-admitting GOV1 :sketch from public inputs.
  Select only the declared fields; caller approvals, states and histories have
  no authority. References and owner labels are retained, not authenticated."
  [change provenance]
  (let [diagnostics (into (field-errors change :change change-fields)
                          (field-errors provenance :provenance provenance-fields))]
    (if (seq diagnostics)
      {:status :rejected :diagnostics diagnostics}
      (let [subject (select-keys change (mapv first change-fields))
            references (select-keys provenance (mapv first provenance-fields))]
        {:status :sketch :diagnostics []
         :artifact
         (canonical
          (assoc subject
                 :schema :gravity.evolution/change-sketch-v1
                 :kind :gravity/language-change-record
                 :state :sketch
                 :authority :non-admitting
                 :provenance references
                 :decision-history
                 [{:event :authored :actor (:owner subject) :state :sketch
                   :subject (select-keys subject [:id :revision])
                   :provenance references}]))}))))
