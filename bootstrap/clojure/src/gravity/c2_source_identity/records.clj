(ns gravity.c2-source-identity.records
  (:require [clojure.string :as str]))

(defn source-unit-record
  [{:keys [explicit-project-context reader-source-identity-inputs
           reader-project-root-record reader-canonical-hash
           gravity-source-extension gravity-source-kind]}
   source-path source-text reader-options project-context]
  (let [context (explicit-project-context project-context)
        identity-inputs (reader-source-identity-inputs source-text
                                                       reader-options
                                                       context)
        project-root (reader-project-root-record context)]
    (merge
     {:artifact :gravity/source-unit
      :source-id (reader-canonical-hash identity-inputs)
      :path source-path
      :extension (gravity-source-extension source-path)
      :source-kind (gravity-source-kind source-path)
      :project-relative-path (:project-relative-path context)
      :project-root (:project-root-id context)
      :project-root-record project-root
      :identity-inputs identity-inputs}
     (select-keys identity-inputs
                  [:encoding :bytes-hash :reader-options
                   :enabled-features :extension-policy]))))

(defn token-record
  [token source-unit]
  (let [source-id (:source-id source-unit)]
    (-> token
        (assoc :token-id (keyword (str "tok-" (:index token)))
               :source-id source-id
               :source-path (:path source-unit)
               :span (assoc (:span token) :file source-id)
               :trivia-before []
               :reader-origin :source)
        (dissoc :index))))

(defn form-record
  [record source-unit]
  (let [source-id (:source-id source-unit)]
    (-> record
        (assoc :source-id source-id
               :source-path (:path source-unit)
               :span (assoc (:span record) :file source-id)
               :origin (merge {:kind :source
                               :source-id source-id
                               :source-path (:path source-unit)}
                              (when (map? (:origin record))
                                (:origin record)))))))

(defn literal-records
  [form-tree]
  (let [literal-kinds #{:nil :boolean :integer :ratio :decimal :string
                        :character :symbol :keyword :tagged-literal}
        candidates (filter #(contains? literal-kinds (:kind %)) form-tree)]
    (mapv
     (fn [idx {:keys [kind raw value span tag form-id]}]
       {:literal-id (keyword (str "lit-" idx))
        :form-id form-id
        :kind kind
        :raw raw
        :decoded value
        :span span
        :facts
        (case kind
          :integer
          {:radix (cond
                    (re-find #"^[+-]?0[xX]" raw) 16
                    (re-find #"^[+-]?0[bB]" raw) 2
                    :else 10)
           :sign (cond
                   (str/starts-with? raw "-") :negative
                   (str/starts-with? raw "+") :explicit-positive
                   :else :unsigned)
           :exact? true}
          :ratio
          (let [[numerator denominator] (str/split raw #"/" 2)]
            {:numerator-spelling numerator
             :denominator-spelling denominator
             :exact? true})
          :decimal
          {:exponent-spelling (second (re-find #"([eE][+-]?[0-9]+)" raw))
           :exact? false}
          :string
          {:escapes (mapv (fn [[match offset]]
                            {:raw match :character-offset offset})
                          (map vector
                               (re-seq #"\\(?:[btnfr\"\\]|u[0-9A-Fa-f]{4})"
                                       raw)
                               (keep-indexed (fn [offset ch]
                                               (when (= \\ ch) offset))
                                             raw)))}
          :character {:escape raw}
          :symbol {:namespace (namespace value)}
          :keyword {:namespace (namespace value)}
          :tagged-literal {:tag tag}
          {})})
     (range)
     candidates)))

(defn trivia-records
  [token-stream]
  (mapv (fn [token]
          {:trivia-id (:token-id token)
           :kind (:kind token)
           :raw (:raw token)
           :span (:span token)
           :source-id (:source-id token)
           :source-path (:source-path token)})
        (filter :trivia? token-stream)))
