(ns gravity.self-hosting.sh15-reader-rejection-families-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh03-reader-adapter-test]
            [gravity.self-hosting.sh15-reader-canonical-diagnostic-test]
            [gravity.self-hosting.sh15-resolved-reader-diagnostic-test]))

;; C2's existing paired negatives supply error meaning; C15 preserves their DATA.
;; Namespace-clause containment (raw L1-NS-SHAPE -> C2-NS-SHAPE) is a separate join.
(def ^:private cases
  [["encoding-invalid-utf8" "C2-ENCODING"]
   ["abbreviation-unattached" "C2-ABBREV"]
   ["string-invalid-escape" "C2-STRING"]
   ["malformed-numeric" "C2-NUMERIC"]
   ["malformed-identifier" "C2-IDENTIFIER"]
   ["map-odd-arity" "C2-MAP"]
   ["metadata-invalid-map-key" "C2-METADATA"]
   ["extension-unsupported" "C2-EXTENSION"]
   ["depth-513" "C2-HASH"]])
(def ^:private fixture-path #'gravity.self-hosting.sh03-reader-adapter-test/fixture-path)
(def ^:private reader-plan #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/reader-plan)
(def ^:private invoke #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/invoke)
(def ^:private fields #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/fields)
(def ^:private resolve-private
  #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/verify)
(def ^:private owner-rules #'gravity.self-hosting.sh03-reader-adapter-test/rejected-fixtures)

(defn- original [family extension]
  (let [source-path (fixture-path "rejected" family (str "." extension))
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file source-path)))
        unit (bootstrap/sh03-reader-input-source-unit
              source-path bytes (bootstrap/reader-project-context-for-source source-path))
        policy (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        result (invoke (force @reader-plan) 'sh03-read-source-unit
                       [unit (mapv #(bit-and 255 %) bytes) policy])
        raw (first (:diagnostics result))]
    {:source-path source-path :bytes (mapv #(bit-and 255 %) bytes) :result result
     :raw raw :event (bootstrap/sh03-reader-resolve-diagnostic-id source-path raw)}))

(deftest additional-direct-reader-rule-families-preserve-real-data-through-c15
  (doseq [[family rule] cases]
    (is (= rule (get @owner-rules family)))
    (let [rows
          (mapv
           (fn [extension]
             (let [{:keys [source-path result raw event] :as source} (original family extension)
                   before (pr-str [raw event]) arguments (fields event)
                   {:keys [unresolved request candidate]} (resolve-private source-path arguments)
                   verification (verify source-path arguments candidate request)]
               (is (= :rejected (:status result)))
               (is (= 1 (count (:diagnostics result))))
               (is (= rule (:id event) (:rule candidate)))
               (is (= :c2-reader (:diagnostic-family event)))
               (is (= :read-source (:stage candidate)))
               (is (= {:digest-ref 0} (:diagnostic-id unresolved)))
               (is (re-matches #"sha256:[0-9a-f]{64}" (:diagnostic-id candidate)))
               (is (= :passed (:status verification)))
               (is (= :fresh-fields-and-external-digest-equality
                      (:verification-status verification)))
               (doseq [[field index] [[:rule 0] [:severity 1] [:stage 2] [:primary 3]
                                      [:related 4] [:origin-chain 5] [:profile 6] [:target 7]
                                      [:facts 8] [:remediation 9] [:redactions 10]]]
                 (is (= (nth arguments index) (get candidate field)) [family extension field]))
               (is (map? (:facts candidate)))
               (is (every? keyword? (keys (:facts candidate))))
               (is (seq (:remediation candidate)))
               (is (every? #(keyword? (:kind %)) (:remediation candidate)))
               (is (every? #(and (contains? candidate %) (nil? (get candidate %)))
                           [:profile :target]))
               (is (contains? (:primary candidate) :syntax-id))
               (is (nil? (get-in candidate [:primary :syntax-id])))
               (is (= (:source-span event) (get-in candidate [:primary :span])))
               (is (= before (pr-str [raw event])))
               (assoc source :request request :candidate candidate)))
           ["gravity" "qst"])]
      (is (= (:bytes (first rows)) (:bytes (second rows))))
      (is (= (:request (first rows)) (:request (second rows))))
      (is (= (get-in rows [0 :candidate :diagnostic-id])
             (get-in rows [1 :candidate :diagnostic-id]))))))
