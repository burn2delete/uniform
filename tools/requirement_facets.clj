(ns requirement-facets
  "Exact source facets conserve evidence, not semantic acceptance."
  (:require [clojure.string :as str]))

(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "REQUIREMENT-FACET"}))))

(defn scope-unresolved? [facet]
  (if (= :historical-superseded (:requirement-role facet))
    (not= :reviewed (get-in facet [:supersession :review-state]))
    (and (:unresolved facet) (not= :acceptance-qualification (:unresolved-category facet)))))

(defn current-requirement-facet? [facet source-bytes by-source]
  (let [o (get by-source (:source-occurrence-id facet))]
    (and (not= :historical-superseded (:requirement-role facet))
         (not (false? (:normative? facet)))
         (not (#{:layout :nonnormative :navigation :heading} (:kind facet)))
         (not (str/blank? (String. ^bytes (get source-bytes (:document-id o))
                                  (int (:start-byte facet)) (int (- (:end-byte facet) (:start-byte facet)))
                                  java.nio.charset.StandardCharsets/UTF_8))))))

(defn validate-supersession! [manifest by-source facet]
  (when (= :historical-superseded (:requirement-role facet))
    (let [s (:supersession facet)
          d (first (filter #(= (:decision-id s) (:id %)) (:scope-decisions manifest)))
          authority (get by-source (:authority-source-occurrence-id s))]
      (require! (and authority (= :source-block (:structural-kind authority))
                     (= :accepted (:state d)) (not (str/blank? (:authority d)))
                     (= (:scope-version manifest) (:scope-version s) (:scope-version d))
                     (= (:source-id d) (:document-id authority))
                     (= (:authority-source-occurrence-id d) (:authority-source-occurrence-id s))
                     (= (:authority-sha256 d) (:authority-sha256 s) (:raw-sha256 authority))
                     (= (:id facet) (:source-facet-id s))
                     (= (:raw-sha256 facet) (:source-fragment-sha256 s))
                     (#{:proposed :reviewed} (:review-state s))
                     (string? (:reason s)) (not (str/blank? (:reason s))))
                "Facet supersession lacks exact source authority, scope or lineage"))))

(defn validate! [manifest sources by-source sha256]
  (let [facets (:source-facets manifest) ids (mapv :id facets)
        by-id (into {} (map (juxt :id identity) facets))
        requirement-ids (set (map :id (:requirements manifest)))
        source-bytes (into {} (map (fn [[id text]] [id (.getBytes ^String text "UTF-8")]) sources))
        unit-facets (reduce (fn [index f]
                              (reduce #(update %1 %2 (fnil conj #{}) (:id f)) index (:unit-ids f)))
                            {} facets)
        grouped (group-by :source-occurrence-id facets)]
    (require! (= (count ids) (count (set ids))) "Duplicate source facet ID")
    (doseq [f facets]
      (require! (contains? #{nil :historical-superseded} (:requirement-role f)) "Unknown facet requirement role")
      (validate-supersession! manifest by-source f))
    (doseq [f facets :when (:unresolved-category f)]
      (require! (and (:unresolved f)
                     (#{:scope-identity :nonoverlap :acceptance-qualification} (:unresolved-category f))
                     (string? (:qualification-reason f)) (not (str/blank? (:qualification-reason f)))
                     (seq (:unit-ids f)))
                "Categorized residual needs a known owner, reason and valid category"))
    (doseq [{:keys [source-occurrence-id start-byte end-byte start-line end-line raw-sha256 unit-ids]} facets]
      (let [o (get by-source source-occurrence-id)]
        (require! (and o (= :source-block (:structural-kind o))
                       (integer? start-byte) (integer? end-byte)
                       (<= (:start-byte o) start-byte) (< start-byte end-byte)
                       (<= end-byte (:end-byte o)) (every? requirement-ids unit-ids))
                  "Facet has invalid source span or dangling requirement")
        (let [bytes (.getBytes ^String (get sources (:document-id o)) "UTF-8")
              decode (fn [start end]
                       (str (.decode (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                                     (java.nio.ByteBuffer/wrap bytes (int start) (int (- end start))))))
              raw (decode start-byte end-byte)
              line (+ 1 (count (filter #{\newline} (decode 0 start-byte))))
              last-line (+ line (count (filter #{\newline} raw)) (if (str/ends-with? raw "\n") -1 0))]
          (require! (and (= raw-sha256 (sha256 raw)) (= start-line line) (= end-line last-line))
                    "Facet bytes, hash or line bounds differ from pinned source"))))
    (doseq [requirement (:requirements manifest) id (:source-facet-ids requirement)]
      (require! (and (contains? by-id id)
                     (contains? (set (:unit-ids (get by-id id))) (:id requirement)))
                "Requirement facet is absent or belongs to another requirement"))
    (doseq [requirement (:requirements manifest)
            :when (or (seq (:source-facet-ids requirement)) (contains? unit-facets (:id requirement)))]
      (require! (= (set (:source-facet-ids requirement)) (get unit-facets (:id requirement) #{}))
                "Requirement facets differ from inverse facet/unit relation")
      (require! (some #(current-requirement-facet? (get by-id %) source-bytes by-source)
                      (:source-facet-ids requirement))
                "Current requirement has no substantive current provenance"))
    (doseq [d (:source-dispositions manifest) :when (or (seq (:facet-ids d)) (seq (get grouped (:occurrence-id d))))]
      (let [o (get by-source (:occurrence-id d)) fs (sort-by :start-byte (get grouped (:occurrence-id d)))]
        (require! (and (= (set (:facet-ids d)) (set (map :id fs)))
                       (= (set (:requirement-ids d)) (set (mapcat :unit-ids fs)))
                       (= (:start-byte o) (:start-byte (first fs)))
                       (= (:end-byte o) (:end-byte (last fs)))
                       (= (map :end-byte (butlast fs)) (map :start-byte (rest fs))))
                  "Facet partition has a hole, overlap or omitted source fragment")))
    (require! (= (set (keys grouped))
                 (set (map :occurrence-id (filter #(seq (:facet-ids %)) (:source-dispositions manifest)))))
              "Facet occurrence has no complete source disposition")
    {:facets (count facets)
     :historical-superseded-facets (count (filter #(= :historical-superseded (:requirement-role %)) facets))
     :acceptance-qualified-facets (count (filter #(and (:unresolved %) (= :acceptance-qualification (:unresolved-category %))) facets))
     :unresolved-occurrence-ids (set (map :source-occurrence-id (filter scope-unresolved? facets)))}))
