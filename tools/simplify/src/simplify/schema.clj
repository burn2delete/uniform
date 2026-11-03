(ns simplify.schema
  (:require [clojure.string :as str]
            [simplify.runtime :as rt]))

(def text {:type "string" :minLength 1})
(defn array-of [item] {:type "array" :items item})
(defn object-of [properties]
  {:type "object" :properties properties :required (mapv name (keys properties)) :additionalProperties false})
(def finding
  (object-of {:key text :title text :evidence text :proposal text :preserve text
              :paths (assoc (array-of text) :minItems 1)
              :dependencies (array-of text)
              :kind {:type "string" :enum ["redundant-work" "abstraction" "duplication" "obsolete"]}}))
(def audit (object-of {:coverage (assoc (array-of text) :minItems 1) :summary text :findings (array-of finding)}))
(def implementation (object-of {:outcome {:type "string" :enum ["ready" "needs_input" "restricted"]} :summary text
                                :checks (array-of (assoc (array-of text) :minItems 1))}))
(def review (object-of {:verdict {:type "string" :enum ["approved" "changes_requested" "needs_input"]}
                       :base text :candidate text :tree text
                       :summary text :findings (array-of text)}))

(defn validate!
  "Validate the exact JSON Schema subset used by our three output contracts."
  ([schema value] (validate! schema value []))
  ([schema value path]
   (let [valid-type? (case (:type schema)
                       "object" (map? value) "array" (vector? value)
                       "string" (string? value) false)]
     (when-not valid-type? (rt/fail! "Invalid structured output type" {:path path :expected (:type schema)}))
     (when (and (:enum schema) (not (some #{value} (:enum schema))))
       (rt/fail! "Invalid structured output enum" {:path path :value value}))
     (case (:type schema)
       "object" (let [expected (set (keys (:properties schema)))]
                  (when-not (= expected (set (keys value)))
                    (rt/fail! "Structured output keys differ" {:path path :expected expected :actual (set (keys value))}))
                  (doseq [[k child] (:properties schema)] (validate! child (get value k) (conj path k))))
       "array" (do
                 (when (< (count value) (or (:minItems schema) 0)) (rt/fail! "Required output evidence is missing" {:path path}))
                 (doseq [[i item] (map-indexed vector value)] (validate! (:items schema) item (conj path i))))
       "string" (when (and (:minLength schema) (str/blank? value)) (rt/fail! "Blank structured output" {:path path})))
     value)))
