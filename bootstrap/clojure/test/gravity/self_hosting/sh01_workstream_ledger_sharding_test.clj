(ns gravity.self-hosting.sh01-workstream-ledger-sharding-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(System/setProperty "gravity.workstream-governance.library" "true")
(load-file "tools/validate_workstream_governance.clj")
(alias 'governance 'gravity.workstream-governance)

(def ^:private contract-path "contracts/workstream-governance.json")
(def ^:private manifest-path "contracts/workstream-ledger.json")

(defn- errors-have? [errors diagnostic]
  (boolean (some #(str/starts-with? % (str diagnostic " ")) errors)))

(deftest canonical-sharded-ledger-and-migration-parity-pass
  (testing "the compact manifest decodes every migrated v1 record"
    (let [manifest (governance/load-json manifest-path)]
      (is (= 2 (get manifest "schema_version")))
      (is (= "gravity/workstream-ledger-v2" (get manifest "contract_id")))
      (is (= 114 (get manifest "record_count")))
      (is (= 110 (get manifest "terminal_count")))
      (is (= 4 (get manifest "active_count")))
      (is (= "exact" (get-in manifest ["migration" "parity"])))
      (is (= 114 (get-in manifest ["migration" "decoded_record_count"]))))
    (is (empty? (governance/validate-documents contract-path manifest-path)))))

(deftest active-reservations-retain-family-exclusivity
  (let [manifest (governance/load-json manifest-path)
        active (filter #(not (get % "terminal")) (get manifest "records"))]
    (is (= 4 (count active)))
    (is (= (count active)
           (count (set (map #(get % "id") active)))))
    (is (= (count active)
           (count (set (map #(get % "invariant_family") active)))))
    (is (every? #(str/starts-with? (get % "path") "contracts/workstream-active/")
                active))))

(deftest sharded-manifest-tamper-fails-with-wg013
  (let [manifest (governance/load-json manifest-path)
        changed-path (assoc-in manifest ["records" 0 "path"] "../outside.json")]
    (is (errors-have? (governance/validate-ledger changed-path) "WG013"))))

(deftest content-tamper-is-detected-through-filesystem-validation
  (let [manifest (governance/load-json manifest-path)
        tampered (assoc manifest "aggregate_sha256" (apply str (repeat 64 "0")))
        temporary (Files/createTempFile "gravity-workstream-ledger-" ".json"
                                        (make-array FileAttribute 0))]
    (try
      (Files/write temporary
                   (.getBytes (str (governance/canonical-json tampered) "\n")
                              StandardCharsets/UTF_8)
                   (make-array java.nio.file.OpenOption 0))
      (is (errors-have? (governance/validate-documents
                         contract-path (str temporary))
                        "WG013"))
      (finally
        (Files/deleteIfExists temporary)))))
