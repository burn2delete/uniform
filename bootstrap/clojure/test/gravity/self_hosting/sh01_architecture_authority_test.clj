(ns gravity.self-hosting.sh01-architecture-authority-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]))

(System/setProperty "gravity.architecture-authority.library" "true")
(load-file "tools/validate_architecture_authority.clj")
(alias 'authority 'gravity.architecture-authority)

(def ^:private ledger-path "contracts/workstream-ledger.json")
(def ^:private fixture-ledger
  {"workstreams"
   [{"id" "sh07-root6-fixture-v2" "state" "draft"
     "invariant_family" "architecture/self-hosting-sh07-root6-fixture"
     "base_commit" "e143921004ff76b5f5ad7e55e8cd24fe23455ded"
     "architecture_decision" "tools/fixtures/architecture_authority/accepted/root6-v2-history.md"
     "dependencies" ["sh07-b51-vector-destructuring-architecture-v18-attempt-17"
                     "sh07-b51-vector-destructuring-architecture-v18-attempt-19"]}
    {"id" "sh07-b51-vector-destructuring-architecture-v18-attempt-17" "state" "integrated"}
    {"id" "sh07-b51-vector-destructuring-architecture-v18-attempt-19" "state" "integrated"}
    {"id" "sh07-b51-vector-destructuring-architecture-v18-attempt-15" "state" "rejected"}
    {"id" "sh07-root6-fixture-v1" "state" "draft"
     "invariant_family" "architecture/self-hosting-sh07-root6-fixture"
     "base_commit" "e143921004ff76b5f5ad7e55e8cd24fe23455ded"
     "architecture_decision" "tools/fixtures/architecture_authority/rejected/root6-v1-integrated-attempt15.md"
     "dependencies" ["sh07-b51-vector-destructuring-architecture-v18-attempt-15"]}]})
(def ^:private accepted-fixture
  "tools/fixtures/architecture_authority/accepted/root6-v2-history.md")
(def ^:private rejected-fixture
  "tools/fixtures/architecture_authority/rejected/root6-v1-integrated-attempt15.md")
(def ^:private malformed-fixture
  "tools/fixtures/architecture_authority/rejected/unknown-authority-key.md")
(def ^:private v1-report
  "docs/artifacts/phase-15/reports/sh07-root6-utf8-byte-census-architecture-v1.md")
(def ^:private v2-report
  "docs/artifacts/phase-15/reports/sh07-root6-utf8-byte-census-architecture-v2.md")

(defn- has-code? [errors code]
  (some #(str/starts-with? % (str code " ")) errors))

(deftest corrected-machine-authority-history-passes
  (is (empty? (authority/validate-report-content
               accepted-fixture (authority/read-report accepted-fixture)
               fixture-ledger))))

(deftest rejected-integrated-history-fails-closed
  (let [errors (authority/validate-report-content
                rejected-fixture (authority/read-report rejected-fixture)
                fixture-ledger)]
    (is (has-code? errors "AA005"))
    (is (has-code? errors "AA006"))))

(deftest authority-block-is-closed
  (let [errors (authority/validate-report malformed-fixture ledger-path)]
    (is (has-code? errors "AA002"))
    (is (some #(str/includes? % "unknown keys") errors))))

(deftest legacy-history-never-grants-authority
  (testing "the corrected report remains checkable as retained legacy history"
    (is (empty? (authority/validate-report v2-report ledger-path
                                           {:require-block? false}))))
  (testing "the rejected report's false authority phrase is stable"
    (let [errors (authority/validate-report v1-report ledger-path
                                            {:require-block? false})]
      (is (has-code? errors "AA006")))))

(deftest active-reports-require-machine-authority
  (let [errors (authority/validate-report v2-report ledger-path)]
    (is (has-code? errors "AA001"))
    (is (some #(str/includes? % "fenced gravity-architecture-authority-v1")
              errors))))

(deftest dependency-state-is-exact
  (let [content (authority/read-report accepted-fixture)
        mutated (str/replace content
                             "\"required_state\": \"integrated\""
                             "\"required_state\": \"accepted\"")
        errors (authority/validate-report-content accepted-fixture mutated fixture-ledger)]
    (is (has-code? errors "AA002"))))

(deftest unknown-workstream-is-not-authority
  (let [content (authority/read-report accepted-fixture)
        mutated (str/replace content "sh07-root6-fixture-v2" "unknown-workstream")
        errors (authority/validate-report-content accepted-fixture mutated fixture-ledger)]
    (is (has-code? errors "AA004"))))

(deftest missing-v2-shard-fails-closed
  (let [manifest {"schema_version" 2
                  "records" [{"ordinal" 0 "id" "missing" "state" "integrated"
                               "invariant_family" "x" "dependencies" []
                               "path" "contracts/workstream-records/does-not-exist.json"
                               "sha256" (apply str (repeat 64 "0"))}]}
        errors (authority/validate-report-content accepted-fixture
                                                   (authority/read-report accepted-fixture)
                                                   manifest)]
    (is (has-code? errors "AA004"))))
