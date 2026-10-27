(ns gravity.c3-reader-expansions
  "Project already validated Reader expansion history; grants no proof authority."
  (:require [clojure.string :as str]))

(defn- form-descendants [by-id root]
  (loop [pending [root] found #{}]
    (if-let [id (peek pending)]
      (if (contains? found id)
        (recur (pop pending) found)
        (recur (into (pop pending) (:children (by-id id))) (conj found id)))
      found)))

(defn- position [seed bytes offset]
  (let [span (:span seed) relative (- offset (:byte-start span))]
    (when-not (<= 0 relative (alength bytes))
      (throw (ex-info "Reader expansion is outside its retained root"
                      {:reason :reader-expansion-surface})))
    (let [prefix (String. (java.util.Arrays/copyOfRange bytes 0 (int relative))
                         java.nio.charset.StandardCharsets/UTF_8)
          lines (str/split prefix #"\r\n|\r|\n" -1)
          last-line (peek lines)
          line (or (:line-start span) (get-in span [:start :line]))
          column (or (:column-start span) (get-in span [:start :column]))]
      {:scalar (+ (:scalar-start span) (.codePointCount prefix 0 (.length prefix)))
       :line (+ line (dec (count lines)))
       :column (+ (if (= 1 (count lines)) column 1)
                  (.codePointCount ^String last-line 0 (.length ^String last-line)))})))

(defn project [forms descendant-ids seed semantic-source-id]
  (let [entries (for [form forms :when (contains? descendant-ids (:form-id form))
                     origin (:generated-origin form)] [form origin])]
    (if (empty? entries) []
    (let [bytes (.getBytes ^String (get-in seed [:reader-origin :raw-excerpt])
                          java.nio.charset.StandardCharsets/UTF_8)]
    (vec
      (for [[form origin] entries
            :let [span (:from origin)
                  start (position seed bytes (:byte-start span))
                  end (position seed bytes (:byte-end span))]]
        {:form-id (:form-id form) :input-form-id (:child-form-id origin)
         :reader-abbreviation (:reason origin)
         :surface-span {:file semantic-source-id
                        :byte-start (:byte-start span) :byte-end (:byte-end span)
                        :scalar-start (:scalar start) :scalar-end (:scalar end)
                        :line-start (:line start) :column-start (:column start)
                        :line-end (:line end) :column-end (:column end)}
         :expanded-form (:expanded-form origin)}))))))

(defn roots [c2 semantic-source-id]
  (let [forms (:form-tree c2) by-id (into {} (map (juxt :form-id identity)) forms)]
    (mapv (fn [seed]
            {:form-id (:form-id seed)
             :origins (project forms (form-descendants by-id (:form-id seed)) seed semantic-source-id)})
          (:syntax-seed-stream c2))))

(defn current? [expected source-nodes]
  (and (= (mapv :form-id expected) (mapv #(get-in % [:source :form-id]) source-nodes))
       (every?
         (fn [[{:keys [origins]} node]]
           (let [facts (filterv #(= :authenticated-reader-products (:fact-kind %)) (:facts node))
                 value (:value (first facts))]
             (and (= 1 (count facts))
                  (= (boolean (seq origins)) (contains? value :reader-expansion-origins))
                  (or (empty? origins) (= origins (:reader-expansion-origins value))))))
         (map vector expected source-nodes))))
