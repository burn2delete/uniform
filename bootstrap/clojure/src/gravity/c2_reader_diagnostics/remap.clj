(ns gravity.c2-reader-diagnostics.remap
  (:require [clojure.string :as str]))

(defn remap-exception!
  [{:keys [diagnostic-ids standard-reader-options fail!]} source-path ex]
  (let [data (ex-data ex)
        old-id (:id data)
        cause (str (or (:cause-message data) (:message data)))
        reader-engine-diagnostic
        (when (and (string? old-id) (str/starts-with? old-id "STAGE1")) old-id)
        owner-id (case old-id
                   ("STAGE1READER001" "STAGE1READER002") "L1-DELIMITER"
                   "STAGE1READER003" "L1-STRING"
                   "STAGE1READER004" "L1-READER-EXTENSION"
                   "STAGE1READER005" "L1-MAP-ARITY"
                   "STAGE1READER007" "L1-NUMERIC"
                   old-id)
        id (cond
             (= "L1-SOURCE-ENCODING" owner-id) "C2-ENCODING"
             (= "L1-SOURCE-EXTENSION" owner-id) "C2-EXTENSION"
             (= "L1-DELIMITER" owner-id) "C2-DELIMITER"
             (= "L1-STRING" owner-id) "C2-STRING"
             (= "L1-NUMERIC" owner-id) "C2-NUMERIC"
             (= "L1-IDENTIFIER" owner-id) "C2-IDENTIFIER"
             (= "L1-NS-SHAPE" owner-id) "C2-NS-SHAPE"
             (= "L1-MAP-ARITY" owner-id) "C2-MAP"
             (= "L1-METADATA" owner-id) "C2-METADATA"
             (= "L1-READER-EXTENSION" owner-id) "C2-EXTENSION"
             (str/includes? cause "Duplicate key") "C2-SET"
             :else owner-id)
        span (:source-span data)
        reader-state
        (or (:reader-state data)
            {:artifact :gravity/reader-state
             :stage (if (contains? #{"STAGE1READER003" "STAGE1READER004"
                                     "STAGE1READER007"} old-id)
                      :lexical-tokenization
                      :recursive-form-building)
             :byte-offset (:byte-start span)
             :line (get-in span [:start :line])
             :column (get-in span [:start :column])
             :token-id (:token-id data)
             :form-id (:form-id data)})]
    (if (contains? (set diagnostic-ids) id)
      (let [preserved-fields (dissoc data :id :message :diagnostic-family :reader-options)]
        (fail! id source-path data
               (cond-> (assoc preserved-fields
                              :cause-message (or (:cause-message data) (:message data))
                              :reader-options standard-reader-options
                              :reader-state reader-state)
                 (and owner-id (not= owner-id id)) (assoc :remapped-from owner-id)
                 reader-engine-diagnostic (assoc :reader-engine-diagnostic reader-engine-diagnostic))))
      (throw ex))))
