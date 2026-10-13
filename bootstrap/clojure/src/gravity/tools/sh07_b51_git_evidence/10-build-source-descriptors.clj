
(defn- build-source-descriptors
  [rows-by-path bytes-by-path]
  (mapv
   (fn [[ordinal kind module-ordinal path]]
     (let [row (get rows-by-path path)
           bytes (get bytes-by-path path)]
       {:ordinal ordinal :kind kind :module-ordinal module-ordinal
        :path path :mode "100644" :blob-oid (:oid row)
        :byte-count (alength ^bytes bytes) :raw-sha256 (codec/R bytes)}))
   source-specs))

(defn- build-owned-c-descriptors
  [rows-by-path bytes-by-path]
  (mapv (fn [ordinal path]
          (blob-descriptor ordinal path (get rows-by-path path)
                           (get bytes-by-path path)))
        (range) owned-c-paths))

(def ^:private compatibility-row
  {:stage :seed-hosted-validation
   :compiler-owner :clojure-seed
   :implemented-documents [:L1 :L2 :C2 :C6]
   :language-subset :sh07-b51-verified-core
   :profiles [:meta]
   :backends []
   :runtime-services [:jvm-process-isolation :git-object-source]
   :package-build-features []
   :standard-library-subset []
   :conformance-suites [:sh07-b51-v9-contract-discharge]
   :known-gaps [:self-hosted-compiler :mir :backend :release]
   :release-eligible false})

(defn- boot5-object-descriptor
  [commit tree row bytes]
  {:commit commit :tree tree :path boot5-path :mode "100644" :type "blob"
   :blob-oid (:oid row) :byte-count (alength ^bytes bytes)
   :raw-sha256 (codec/R bytes)})

(defn- build-boot5-compatibility
  [content baseline-show candidate-row candidate-bytes baseline-row
   baseline-bytes]
  (let [candidate (boot5-object-descriptor
                   (:commit content) (:tree content) candidate-row
                   candidate-bytes)
        baseline (boot5-object-descriptor
                  pre-baseline-commit (:tree baseline-show) baseline-row
                  baseline-bytes)
        same-blob (select-keys candidate
                               [:path :mode :type :blob-oid :byte-count
                                :raw-sha256])]
    (when-not (= same-blob
                 (select-keys baseline
                              [:path :mode :type :blob-oid :byte-count
                               :raw-sha256]))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :boot5-baseline
             {:baseline baseline :candidate candidate}))
    {:baseline-document baseline
     :candidate-document candidate
     :document-equal true
     :compatibility-row compatibility-row
     :compatibility-digest
     (codec/H [:gravity/sh07-b51-boot5-compatibility-row-v1
               compatibility-row])
     :explicit-gaps (:known-gaps compatibility-row)
     :stage-support-claims []
     :stage-advanced false}))

(def ^:private a9-fixed-descriptor-fields
  {architecture-report-path
   {:blob-oid "1eef3f25b18b941deed09bdfb178573f08f7928e"
    :byte-count 393221
    :raw-sha256
    "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd"}
   provisional-report-path
   {:blob-oid "fc36dc37d4e45adc7255eb531ae8d312d9eb51da"
    :byte-count 4977
    :raw-sha256
    "sha256:7401846ab6892cc32d4ba4f0d8d0c87be531575d75abc88852f6516cd769ae4d"}
   a9-terminal-path
   {:blob-oid "ddae151f339068955ff9f7df75c975e697962bd6"
    :byte-count 13404
    :raw-sha256
    "sha256:461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1"}})

(def ^:private a9-workstream-id
  "sh07-b51-git-object-evidence-publication-architecture-v9")

(def ^:private a9-invariant-family
  "architecture/self-hosting-sh07-b51-pin-free-publication-v1")

(def ^:private a9-dependencies
  ["sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32"
   "sh07-b51-first-eval-nested-validation-architecture-v3"
   "sh07-b51-binding-replay-index-architecture-v1"
   "sh07-b51-binding-replay-index-implementation-review-erratum-v1"
   "c2-pass-cache-capacity-withholding-v1"])

(def ^:private a9-record-keys
  #{"architecture_decision" "author" "base_commit" "candidate_commit"
    "clean_worktree" "dependencies" "disposition" "evidence"
    "governing_contracts" "history" "id" "invariant_family"
    "no_overclaim_authority" "owned_paths" "owner"
    "residual_host_boundaries" "reviews" "state" "title"})

(def ^:private governance-transitions
  {"draft" #{"frozen" "held" "rejected" "superseded" "abandoned"}
   "frozen" #{"draft" "review-pending" "held" "rejected"
              "superseded" "abandoned"}
   "review-pending" #{"draft" "accepted" "held" "rejected"
                      "superseded" "abandoned"}
   "accepted" #{"integration-eligible" "held" "rejected"
                 "superseded" "abandoned"}
   "integration-eligible" #{"integrated" "held" "rejected"
                            "superseded" "abandoned"}
   "held" #{"draft" "frozen" "rejected" "superseded" "abandoned"}
   "integrated" #{} "rejected" #{} "superseded" #{} "abandoned" #{}})

(defn- exact-json-keys? [value expected]
  (and (map? value) (= expected (set (keys value)))))

(defn- a9-record-shape-valid? [record]
  (and
   (exact-json-keys? record a9-record-keys)
   (every? string?
           (map #(get record %)
                ["architecture_decision" "author" "base_commit"
                 "candidate_commit" "disposition" "id" "invariant_family"
                 "owner" "state" "title"]))
   (boolean? (get record "clean_worktree"))
   (every? #(and (vector? (get record %))
                 (every? string? (get record %)))
           ["dependencies" "governing_contracts" "owned_paths"
            "residual_host_boundaries"])
   (let [evidence (get record "evidence")]
     (and (exact-json-keys?
           evidence
           #{"accepted_fixtures" "rejected_fixtures" "stable_diagnostics"
             "validation_commands"})
          (every? #(and (vector? (get evidence %))
                        (every? string? (get evidence %)))
                  ["accepted_fixtures" "rejected_fixtures"
                   "stable_diagnostics"])
          (vector? (get evidence "validation_commands"))
          (every?
           #(and (exact-json-keys? % #{"command" "exit_code" "result"})
                 (string? (get % "command"))
                 (integer? (get % "exit_code"))
                 (string? (get % "result")))
           (get evidence "validation_commands"))))
   (let [authority (get record "no_overclaim_authority")]
     (and (exact-json-keys?
           authority
           #{"integration_only" "release" "seed_retirement" "self_hosting"})
          (every? boolean? (vals authority))))
   (and (vector? (get record "reviews"))
        (every?
         #(and (exact-json-keys? % #{"kind" "result" "reviewer"})
               (every? string? (vals %)))
         (get record "reviews")))
   (and (vector? (get record "history"))
        (seq (get record "history"))
        (every?
         #(and (exact-json-keys? % #{"actor" "at" "reason" "state"})
               (every? string? (vals %)))
         (get record "history")))))

(defn- history-transitions-valid? [history]
  (and (= "draft" (get (first history) "state"))
       (every?
        (fn [[left right]]
          (contains? (get governance-transitions (get left "state") #{})
                     (get right "state")))
        (partition 2 1 history))))

(defn- a9-artifact-descriptors
  [rows-by-path bytes-by-path]
  (mapv
   (fn [path]
     (let [row (get rows-by-path path)
           bytes (get bytes-by-path path)
           descriptor
           {:path path :mode "100644" :type "blob" :blob-oid (:oid row)
            :byte-count (alength ^bytes bytes) :raw-sha256 (codec/R bytes)}]
       (when-not (= (get a9-fixed-descriptor-fields path)
                    (select-keys descriptor
                                 [:blob-oid :byte-count :raw-sha256]))
         (fail! "SH07-B51-GIT-SNAPSHOT" :a9-fixed-descriptor
                {:path path :descriptor descriptor}))
       descriptor))
   a9-only-paths-e1))

(defn- terminal-manifest-entry!
  [ledger record]
  (let [matches (filterv #(= a9-workstream-id (get % "id"))
                         (get ledger "records"))]
    (when-not (= 1 (count matches))
      (fail! "SH07-B51-GIT-SNAPSHOT" :a9-terminal-manifest-census))
    (first matches)))
