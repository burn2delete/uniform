(ns workstream-composition
  "Verify committed lifecycle metadata separately from immutable source content.
  The caller still pins the complete integration commit and tree."
  (:import [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(let [property "gravity.workstream-governance.library"
      previous (System/getProperty property)]
  (try
    (System/setProperty property "true")
    (load-file "tools/validate_workstream_governance.clj")
    (finally
      (if previous (System/setProperty property previous)
          (System/clearProperty property)))))
(alias 'wg 'gravity.workstream-governance)

(defn sha256 [text]
  (apply str (map #(format "%02x" (bit-and (int %) 255))
                  (.digest (MessageDigest/getInstance "SHA-256")
                           (.getBytes ^String text StandardCharsets/UTF_8)))))

(defn- require! [condition message]
  (when-not condition
    (throw (ex-info message {:code "WORKTREE-COMPOSITION"}))))

(defn verify!
  "READ-FILE reads a regular committed Git blob at [commit path], never disk.
  Only the named existing active shard and its manifest entry may change.
  Returns evidence, not independent review or permission to integrate."
  [read-file source head base id changed-paths]
  (require! (and (string? id) (re-matches #"[a-z0-9][a-z0-9.-]*" id))
            "workstream id must name a flat active shard")
  (let [path (str "contracts/workstream-active/" id ".json")
        manifest-path "contracts/workstream-ledger.json"
        _ (require! (every? #{path manifest-path} changed-paths)
                    "source-to-integration diff contains non-lifecycle content")
        read-ledger
        (fn [commit]
          (let [text (read-file commit manifest-path)
                manifest (wg/read-strict-json text)
                policy (wg/read-strict-json (read-file commit "contracts/workstream-governance.json"))
                _ (require! (empty? (wg/validate-contract policy)) "invalid committed governance policy")
                active-states (set (get-in policy ["lifecycle" "active_candidate_states"]))
                _ (require! (and (= 2 (get manifest "schema_version"))
                                  (empty? (wg/validate-ledger manifest)))
                            "invalid canonical manifest")
                entries (get manifest "records")
                _ (require! (and (= (count entries) (get manifest "record_count"))
                                  (= (mapv #(get % "id") entries)
                                     (vec (sort (map #(get % "id") entries))))
                                  (= (set (map #(get % "ordinal") entries))
                                     (set (range (count entries))))
                                  (= (count entries) (count (set (map #(get % "path") entries))))
                                  (= (get manifest "terminal_count")
                                     (count (filter #(true? (get % "terminal")) entries)))
                                  (= (get manifest "active_count")
                                     (count (filter #(false? (get % "terminal")) entries))))
                            "manifest index or counts are inconsistent")
                items (mapv (fn [entry]
                              (let [bytes (read-file commit (get entry "path"))
                                    item (wg/read-strict-json bytes)
                                    terminal? (true? (get entry "terminal"))
                                    expected-path (if terminal?
                                                    (str "contracts/workstream-records/terminal/"
                                                         (get entry "id") "-" (get entry "sha256") ".json")
                                                    (str "contracts/workstream-active/" (get entry "id") ".json"))]
                                (require! (= (get entry "sha256") (sha256 bytes))
                                          "shard digest differs from manifest")
                                (require! (and (= expected-path (get entry "path"))
                                                (= (select-keys entry ["id" "state" "invariant_family" "dependencies"])
                                                   (select-keys item ["id" "state" "invariant_family" "dependencies"]))
                                                (= terminal? (not (contains? active-states (get item "state")))))
                                          "shard identity or lifecycle differs from manifest")
                                item))
                            (sort-by #(get % "ordinal") entries))
                migration (get manifest "migration")
                migration-text (read-file commit (get migration "source_path"))
                migration-source (wg/read-strict-json migration-text)
                _ (require! (and (= (get migration "source_sha256") (sha256 migration-text))
                                  (= (get migration "decoded_record_count")
                                     (count (get migration-source "workstreams")))
                                  (= (get migration "decoded_aggregate_sha256")
                                     (sha256 (wg/canonical-json (get migration-source "workstreams")))))
                            "retained migration evidence differs from its commitment")
                _ (require! (= (get manifest "aggregate_sha256")
                                (sha256 (wg/canonical-json items)))
                            "decoded aggregate digest differs from manifest")
                aggregate {"schema_version" 1 "contract_id" "gravity/workstream-ledger-v1"
                           "governance_contract" "contracts/workstream-governance.json"
                           "workstreams" items}
                _ (require! (empty? (wg/validate-ledger aggregate))
                            "lifecycle or admission evidence is invalid")]
            {:text text :manifest manifest :items items}))
        before (read-ledger source)
        after (read-ledger head)
        entry (fn [view] (first (filter #(= id (get % "id"))
                                       (get-in view [:manifest "records"]))))
        record (fn [view] (first (filter #(= id (get % "id")) (:items view))))
        old-entry (entry before) new-entry (entry after)
        old-record (record before) new-record (record after)
        immutable ["id" "title" "invariant_family" "owner" "author" "dependencies"
                   "architecture_decision" "owned_paths" "governing_contracts"
                   "residual_host_boundaries" "no_overclaim_authority"]
        strip-entry #(dissoc % "state" "sha256")
        strip-manifest #(dissoc % "records" "aggregate_sha256")
        other-entries #(vec (remove (fn [item] (= id (get item "id")))
                                   (get-in % [:manifest "records"])))]
    (require! (and old-entry new-entry (= path (get old-entry "path"))
                   (= path (get new-entry "path"))
                   (false? (get old-entry "terminal")) (false? (get new-entry "terminal")))
              "composition requires an existing active reservation")
    (require! (and (= (strip-manifest (:manifest before)) (strip-manifest (:manifest after)))
                   (= (other-entries before) (other-entries after))
                   (= (strip-entry old-entry) (strip-entry new-entry)))
              "manifest changed outside the selected lifecycle entry")
    (require! (= (select-keys old-record immutable) (select-keys new-record immutable))
              "ownership, contracts, dependencies or authority changed after source freeze")
    (require! (= (get old-record "history")
                 (vec (take (count (get old-record "history")) (get new-record "history"))))
              "historical lifecycle prefix changed")
    (require! (and (= source (get new-record "candidate_commit"))
                   (= base (get new-record "base_commit"))
                   (= "integration-eligible" (get new-record "state"))
                   (true? (get new-record "clean_worktree")))
              "record must bind eligible source, exact integration base and clean capture")
    {:workstream id :source-commit source :integration-commit head :base-commit base
     :changed-paths (vec (sort changed-paths))
     :manifest-sha256 (sha256 (:text after))
     :shard-path path :shard-sha256 (get new-entry "sha256")
     :aggregate-sha256 (get-in after [:manifest "aggregate_sha256"])
     :state (get new-record "state") :content-conserved true}))
