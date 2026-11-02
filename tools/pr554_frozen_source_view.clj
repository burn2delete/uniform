(ns pr554-frozen-source-view
  "Build a compact, read-only index of PR554's historical source inputs.

  The original PR carried several multi-megabyte generated EDN snapshots and
  a large inherited tool stack.  This index keeps their immutable Git
  identities, bounded semantic summaries and current-main comparison without
  promoting those snapshots to current authority.  It never writes source
  inputs or changes the requirement manifest."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(def output-path "specs/requirement-reconciliation/pr554-frozen-source-view.edn")
(def authored-base "6276c94b0b753c888ef8fecc794bcc81245ea3b3")
(def authored-base-tree "9f464ed0f03866bd4978e4213a488f3b1ae48683")
(def source-head "e7a93606858e543119f807130f1583f1b011b151")
(def source-tree "4c8d0e6c77482b75917ca8ad60605bef4f36f3d8")
(def current-main "5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e")
(def current-main-tree "643afe5f3b00918fc3de76f2855294481a09b505")

(def selected-reconciliation-patterns
  [#"^specs/requirement-reconciliation/(accepted-historical-mappings(?:-[0-9]+)?\.edn)$"
   #"^specs/requirement-reconciliation/(all240-current-source-facet-inventory\.edn)$"
   #"^specs/requirement-reconciliation/(b6-.+)$"
   #"^specs/requirement-reconciliation/(c18-.+)$"
   #"^specs/requirement-reconciliation/(current-criterion-bindings-561.+\.edn)$"
   #"^specs/requirement-reconciliation/(domain-gap-completion\.edn|historical-gap-partition\.edn|source-dispositions\.edn|task-baseline\.edn)$"
   #"^specs/requirement-reconciliation/(platform-task-interfaces\.edn|evidence\.md)$"])
(def selected-tool-paths
  #{"tools/sh_crosswalk.clj" "tools/sh_crosswalk_test.clj"
    "tools/task_baseline.clj" "tools/task_baseline_test.clj"})

(defn require! [condition message]
  (when-not condition (throw (ex-info message {:diagnostic "PR554-FROZEN-SOURCE-VIEW"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                  (.getBytes ^String text "UTF-8")))))

(defn git-blob-id [text]
  (let [bytes (.getBytes ^String text "UTF-8")
        header (.getBytes (str "blob " (alength bytes) "\u0000") "UTF-8")
        payload (byte-array (+ (alength header) (alength bytes)))]
    (System/arraycopy header 0 payload 0 (alength header))
    (System/arraycopy bytes 0 payload (alength header) (alength bytes))
    (format "%040x" (BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-1") payload)))))

(defn git-run [& args]
  (let [process (.start (doto (ProcessBuilder. ^java.util.List (into ["git" "-C" "."] args))
                         (.redirectErrorStream true)))
        output (with-open [input (.getInputStream process)]
                 (slurp input :encoding "UTF-8"))]
    {:exit (.waitFor process) :output output}))

(defn git-show [ref path]
  (let [{:keys [exit output]} (git-run "show" (str ref ":" path))]
    (when (zero? exit) output)))

(defn git-tree [ref]
  (let [{:keys [exit output]} (git-run "rev-parse" (str ref "^{tree}"))]
    (require! (zero? exit) (str "Missing Git source identity: " ref))
    (str/trim output)))

(defn try-git-tree [ref]
  (let [{:keys [exit output]} (git-run "rev-parse" (str ref "^{tree}"))]
    (when (zero? exit) (str/trim output))))

(defn source-diff-paths []
  (let [{:keys [exit output]} (git-run "diff" "--name-status" "--diff-filter=AM"
                                      authored-base source-head "--")]
    (require! (zero? exit) "Cannot enumerate PR554 source paths")
    (->> (str/split-lines output)
         (keep (fn [line]
                 (let [[status path] (str/split line #"\t" 2)]
                   (when (and status path
                              (or (some #(re-find % path) selected-reconciliation-patterns)
                                  (contains? selected-tool-paths path)))
                     {:change-status (keyword (str/lower-case status)) :path path}))))
         (sort-by :path)
         vec)))

(defn family [path]
  (cond
    (str/includes? path "/all240-") :conservation-inventory
    (str/includes? path "/b6-") :qualified-b6-receipts
    (str/includes? path "/c18-") :qualified-c18-receipts
    (or (str/includes? path "/accepted-historical-mappings")
        (str/includes? path "/current-criterion-bindings-561")
        (str/ends-with? path "/source-dispositions.edn")) :source-decisions-and-historical-mappings
    (or (str/ends-with? path "/domain-gap-completion.edn")
        (str/ends-with? path "/historical-gap-partition.edn")) :domain-gap-context
    (or (str/ends-with? path "/platform-task-interfaces.edn")
        (= path "specs/requirement-reconciliation/evidence.md"))
    :platform-task-interface-reconciliation
    (contains? selected-tool-paths path) :read-only-census-tooling
    (str/ends-with? path "/task-baseline.edn") :read-only-census-and-manifest-input
    :else :historical-reconciliation-input))

(defn bounded-string [value]
  (if (<= (count value) 1200)
    value
    {:sha256 (sha256 value) :length (count value)}))

(def summary-keys
  [:kind :schema-version :status :source-commit :source-tree :authority
   :implementation-credit :final-denominator :review-state :independent-acceptance
   :adoption-state :task-count-delta :qualification :scope-limit :scope-qualification
   :nonclaims])

(defn summary-value [value]
  (cond
    (string? value) (bounded-string value)
    (or (keyword? value) (symbol? value) (number? value) (boolean? value) (nil? value)) value
    (coll? value) {:count (count value)}
    :else (bounded-string (pr-str value))))

(def known-large-summaries
  {"specs/requirement-reconciliation/all240-current-source-facet-inventory.edn"
   {:kind :ALL240-current-source-occurrence-facet-inventory
    :authority :source-conservation-with-qualified-independent-conjunction-overlays
    :implementation-credit :none :final-task-or-requirement-denominator :not-conferred
    :counts {:documents 240 :occurrences 9945 :facets 28785 :owner-resolutions 1188}}
   "specs/requirement-reconciliation/source-dispositions.edn"
   {:kind :canonical-task-source-dispositions :schema-version 1
    :authority :derived-source-disposition-and-review-input-only
    :implementation-acceptance :not-conferred
    :counts {:original-documents 240 :remaining-decisions 833
             :reviewed-family-inputs 13 :retained-source-family-inputs 3
             :historical-interface-compositions 17 :published-missing-interface-count 577}}
   "specs/requirement-reconciliation/domain-gap-completion.edn"
   {:kind :domain-historical-gap-completion-patch :schema-version 1
    :authority :proposal-only :adoption-state :pending-independent-review-and-canonical-composition
    :counts {:records 70 :assigned-ids 70 :new-task-proposals 9 :unresolved-semantic-ids 57}}
   "specs/requirement-reconciliation/historical-gap-partition.edn"
   {:kind :historical-gap-dispatch-partition :schema-version 1
    :authority :noncanonical-dispatch-input-only
    :counts {:residual-records 561 :partitions 22 :retained-terminal-history 16}}})

(def platform-interface-receipt-path
  "specs/requirement-reconciliation/platform-task-interfaces.edn")
(def platform-evidence-path "specs/requirement-reconciliation/evidence.md")
(def platform-evidence-pattern
  #"(?s)(The Platform Product extension has eleven proposed Open task interfaces.*?prior review history\.)")

(defn platform-interface-receipt-summary [text]
  (let [value (edn/read-string text)]
    {:kind (:kind value)
     :schema-version (:schema-version value)
     :source (select-keys (:source value) [:commit :tree :path :sha256])
     :task-source (select-keys (:task-source value) [:commit :path :sha256])
     :scope (:scope value)
     :scope-relation (:scope-relation value)
     :qualification (:qualification value)
     :source-binding-count (count (:source-bindings value))
     :source-bindings
     (mapv #(select-keys % [:task-id :status :implementation-acceptance
                            :source :task-reference]) (:source-bindings value))
     :conservation (:conservation value)
     :nonclaims [:historical-source-receipt-only
                 :not-all-source-provenance
                 :not-current-denominator
                 :not-implementation-acceptance]}))

(defn utf8-bytes [text]
  (alength (.getBytes ^String text "UTF-8")))

(defn platform-evidence-summary [text]
  (let [paragraph (second (re-find platform-evidence-pattern text))]
    (require! (string? paragraph) "PR554 Platform evidence paragraph is missing")
    (let [start (.indexOf ^String text ^String paragraph)
          prefix (subs text 0 start)
          start-line (inc (count (re-seq #"\n" prefix)))]
      {:kind :platform-task-interface-evidence-paragraph
       :authority :historical-evidence-only
       :paragraph paragraph
       :paragraph-sha256 (sha256 paragraph)
       :span {:start-byte (utf8-bytes prefix)
              :end-byte (+ (utf8-bytes prefix) (utf8-bytes paragraph))
              :start-line start-line
              :end-line (+ start-line (count (re-seq #"\n" paragraph)))}
       :conservation {:interface-records 11
                      :architecture-decision-rows 4
                      :conditional-product-output-rows 7
                      :platform-target-conformance-records 4
                      :original-task-file-recovery true}
       :nonclaims [:historical-source-evidence-only
                   :no-final-denominator
                   :not-all-source-provenance
                   :no-implementation-acceptance]})))

(defn parsed-summary [path text]
  (cond
    (= path platform-interface-receipt-path) (platform-interface-receipt-summary text)
    (= path platform-evidence-path) (platform-evidence-summary text)
    (contains? known-large-summaries path) (get known-large-summaries path)
    (str/ends-with? path ".edn")
      (try
        (let [value (edn/read-string text)
              fields (into {} (keep (fn [key]
                                      (when (contains? value key)
                                        [key (summary-value (get value key))])))
                            summary-keys)
              counts (into {}
                           (keep (fn [[key item]]
                                   (when (and (coll? item) (not (map? item)))
                                     [key (count item)])))
                           value)]
          {:kind :edn-source-summary :fields fields :collection-counts counts})
        (catch Exception error
          {:kind :unparsed-edn-source :error (bounded-string (.getMessage error))}))
    :else {:kind :non-edn-source :format (last (str/split path #"\."))}))

(defn current-view [path source-sha]
  (if-let [text (git-show current-main path)]
    {:state (if (= source-sha (sha256 text)) :same :changed)
     :sha256 (sha256 text) :blob (git-blob-id text)
     :bytes (alength (.getBytes ^String text "UTF-8"))}
    {:state :absent}))

(defn entry [{:keys [change-status path]}]
  (let [text (git-show source-head path)]
    (require! (string? text) (str "PR554 source path cannot be read: " path))
    (let [source-sha (sha256 text)]
      {:path path :family (family path) :change-status change-status
       :source {:commit source-head :tree source-tree
                :sha256 source-sha
                :blob (git-blob-id text)
                :bytes (alength (.getBytes ^String text "UTF-8"))}
       :summary (parsed-summary path text)
       :current-main (current-view path source-sha)})))

(defn paths-for [entries family-name]
  (mapv :path (filter #(= family-name (:family %)) entries)))

(defn value-map [entries]
  [{:family :conservation-inventory
    :paths (paths-for entries :conservation-inventory)
    :value "The all-240 source/facet inventory and its owner-resolution counts are retained as a frozen source view."
    :current-disposition :historical-only-not-canonical-authority}
   {:family :qualified-b6-receipts
    :paths (paths-for entries :qualified-b6-receipts)
    :value "B6 source-entailment controls, qualified review and adoption receipts preserve exact identities, negative controls and source-only limits."
    :current-disposition :qualified-history-only-no-backend-or-denominator-acceptance}
   {:family :qualified-c18-receipts
    :paths (paths-for entries :qualified-c18-receipts)
    :value "C18 bookkeeping controls and qualified applicability retain source joins, byte continuity and no-acceptance boundaries."
    :current-disposition :historical-input-only-no-canonical-renewal}
   {:family :source-decisions-and-historical-mappings
    :paths (paths-for entries :source-decisions-and-historical-mappings)
    :value "Historical mapping batches, current-criterion witnesses and source dispositions remain recoverable by exact source bytes."
    :current-disposition :not-replayed-into-current-manifest}
   {:family :domain-gap-context
    :paths (paths-for entries :domain-gap-context)
    :value "Domain gap proposals and dispatch partitions preserve unresolved criteria, residual identities and proposal-only status."
    :current-disposition :proposal-context-only}
   {:family :platform-task-interface-reconciliation
    :paths (paths-for entries :platform-task-interface-reconciliation)
    :value "The eleven-record Platform task receipt, exact source spans and evidence paragraph preserve a four-row historical conservation proof without becoming current provenance or a denominator."
    :current-disposition :frozen-historical-receipt-only}
   {:family :read-only-census-tooling
    :paths (paths-for entries :read-only-census-tooling)
    :value "The original census/crosswalk tools are fingerprinted for historical comparison; their stale generated dependencies are not replayed."
    :current-disposition :successor-or-frozen-history-only}
   {:family :read-only-census-and-manifest-input
    :paths (paths-for entries :read-only-census-and-manifest-input)
    :value "The provisional task census is preserved as an immutable observation, never as a current denominator."
    :current-disposition :provisional-observation-only}])

(defn historical-manifest-inputs [entries]
  (->> entries
       (filter #(contains? #{"specs/requirement-reconciliation/source-dispositions.edn"
                             "specs/requirement-reconciliation/task-baseline.edn"}
                           (:path %)))
       (mapv (fn [{:keys [path source current-main]}]
               {:path path
                :source-ref source-head
                :source-sha256 (:sha256 source)
                :source-tree source-tree
                :current-main-state (:state current-main)
                :disposition :historical-input-only
                :canonical-manifest-effect :none
                :replay-policy :read-only-git-bytes-only}))))

(defn current-canonical-comparison []
  (let [path "contracts/requirement-reconciliation.edn"
        text (git-show current-main path)
        manifest (edn/read-string text)]
    {:manifest {:path path :ref current-main :tree current-main-tree
                :sha256 (sha256 text) :blob (git-blob-id text)
                :bytes (alength (.getBytes ^String text "UTF-8"))}
     :counts {:original-documents (count (:original-documents manifest))
              :extension-sources (count (:extension-sources manifest))
              :source-facets (count (:source-facets manifest))
              :source-dispositions (count (:source-dispositions manifest))
              :requirements (count (:requirements manifest))}
     :state {:scope-state (:scope-state manifest)
             :implementation-acceptance (:implementation-acceptance manifest)
             :denominator-ready? false}
     :qualification "Current manifest bytes are the current reconciliation authority. This comparison does not adopt any PR554 historical receipt, task census, source decision, or mapping snapshot."}))

(def nofollow-links
  (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn repository-root []
  (.toRealPath (.toPath (io/file ".")) (make-array java.nio.file.LinkOption 0)))

(defn safe-output-path-from-root!
  "Return a relative output path after rejecting symlinked parents and targets."
  [root path]
  (let [root (.toRealPath root (make-array java.nio.file.LinkOption 0))
        relative (.normalize (.toPath (io/file path)))]
    (require! (and (not (.isAbsolute relative))
                   (not (.startsWith relative "..")))
              "Frozen source view output must remain repository-relative")
    (let [target (.normalize (.resolve root relative))
          parent (.getParent target)
          parent-relative (.relativize root parent)]
      (require! (.startsWith target root)
                "Frozen source view output escapes repository")
      (loop [cursor root parts (iterator-seq (.iterator parent-relative))]
        (when (seq parts)
          (let [next-path (.resolve cursor (str (first parts)))]
            (require! (not (java.nio.file.Files/isSymbolicLink next-path))
                      "Frozen source view output parent is a symbolic link")
            (recur next-path (next parts)))))
      (require! (java.nio.file.Files/isDirectory parent nofollow-links)
                "Frozen source view output parent is not an existing directory")
      (require! (not (java.nio.file.Files/isSymbolicLink target))
                "Frozen source view output target is a symbolic link")
      target)))

(defn write-output! [text]
  (let [target (safe-output-path-from-root! (repository-root) output-path)]
    (java.nio.file.Files/write target (.getBytes ^String text "UTF-8")
                               (make-array java.nio.file.OpenOption 0))
    target))

(defn build []
  (require! (= source-tree (git-tree source-head)) "PR554 source tree identity changed")
  (require! (= authored-base-tree (git-tree authored-base)) "PR554 authored-base tree identity changed")
  (require! (= current-main-tree (git-tree current-main)) "Frozen current-main tree identity changed")
  (let [entries (mapv entry (source-diff-paths))]
    {:kind :pr554-frozen-source-view-index
     :schema-version 1
     :authority :historical-source-view-and-reproducibility-index-only
     :source {:head source-head :tree source-tree
              :authored-base authored-base :authored-base-tree authored-base-tree}
     :comparison {:current-main current-main :current-main-tree current-main-tree}
     :selected-entry-count (count entries)
     :families (frequencies (map :family entries))
     :value-map (value-map entries)
     :historical-manifest-inputs (historical-manifest-inputs entries)
     :current-canonical-comparison (current-canonical-comparison)
     :entries entries
     :reproduction
     {:write "clojure -M tools/pr554_frozen_source_view.clj --write"
      :check "clojure -M tools/pr554_frozen_source_view.clj --check"
      :source-policy "Read exact original Git blobs; never refresh current hashes or write canonical manifest state."
      :large-input-policy "Large generated EDN is represented by exact bytes/blob/SHA256 and bounded known summaries; full bytes remain recoverable from the frozen source commit."}
     :nonclaims
     [:not-current-canonical-source
      :not-requirement-denominator
      :not-task-acceptance
      :not-implementation-credit
      :not-source-version-admission
      :not-whole-pr-or-sh-acceptance
      :not-release-or-self-hosting
      :no-std3-accounting-revival
      :no-blind-hash-refresh
      :no-denied-action-retry
      :not-all-source-provenance]}))

(defn check-static! [actual]
  (require! (= :pr554-frozen-source-view-index (:kind actual)) "Wrong PR554 frozen-view kind")
  (require! (= 1 (:schema-version actual)) "Unsupported PR554 frozen-view schema")
  (require! (= {:head source-head :tree source-tree
                :authored-base authored-base :authored-base-tree authored-base-tree}
               (:source actual)) "Frozen PR554 source identity changed")
  (require! (= {:current-main current-main :current-main-tree current-main-tree}
               (:comparison actual)) "Frozen current-main comparison changed")
  (require! (= (:selected-entry-count actual) (count (:entries actual)))
            "Frozen-view entry count is inconsistent")
  (require! (= (:selected-entry-count actual)
               (count (set (map :path (:entries actual)))))
            "Frozen-view paths are duplicated")
  (require! (every? #(and (string? (:path %))
                          (re-matches #"[0-9a-f]{64}" (get-in % [:source :sha256]))
                          (re-matches #"[0-9a-f]{40}" (get-in % [:source :blob]))
                          (pos? (get-in % [:source :bytes])))
                    (:entries actual))
            "Frozen-view source identity is incomplete")
  (require! (every? #{:not-current-canonical-source :not-requirement-denominator
                      :not-task-acceptance :not-implementation-credit
                      :not-source-version-admission :not-whole-pr-or-sh-acceptance
                      :not-release-or-self-hosting :no-std3-accounting-revival
                      :no-blind-hash-refresh :no-denied-action-retry
                      :not-all-source-provenance}
                    (:nonclaims actual))
            "Frozen-view nonclaims changed")
  true)

(defn verify-source! [actual]
  (require! (= source-tree (try-git-tree source-head))
            "Frozen PR554 source objects are unavailable or resolve to another tree")
  (let [results
        (mapv (fn [{:keys [path source current-main]}]
                (let [text (git-show source-head path)]
                  (require! (string? text) (str "Missing frozen source: " path))
                  (require! (= {:commit source-head :tree source-tree}
                               (select-keys source [:commit :tree]))
                            (str "Frozen source commit/tree changed: " path))
                  (require! (= (:sha256 source) (sha256 text))
                            (str "Frozen source SHA256 changed: " path))
                  (require! (= (:blob source) (git-blob-id text))
                            (str "Frozen source Git blob changed: " path))
                  (require! (= (:bytes source) (utf8-bytes text))
                            (str "Frozen source byte count changed: " path))
                  (require! (= current-main (current-view path (:sha256 source)))
                            (str "Frozen current-main view changed: " path))
                  :verified))
              (:entries actual))]
    (require! (= (count results) (:selected-entry-count actual))
              "Frozen source verification count differs from selected entries")
    {:source-objects-available? true
     :verified-count (count (filter #{:verified} results))
     :entry-count (count (:entries actual))}))

(defn check-against! [actual expected]
  (check-static! actual)
  (require! (= expected actual)
            "Frozen source view differs from deterministic source-derived output")
  (verify-source! actual))

(defn check! [actual]
  (check-against! actual (build)))

(defn run [args]
  (require! (or (= ["--write"] (vec args)) (= ["--check"] (vec args)))
            "Usage: clojure -M tools/pr554_frozen_source_view.clj --write|--check")
  (if (= ["--write"] (vec args))
    (let [result (build)]
      (write-output! (with-out-str (pprint/pprint result)))
      (println (select-keys result [:selected-entry-count :families :source :comparison])))
    (let [actual (edn/read-string (slurp output-path))]
      (println (assoc (check! actual) :result :pass)))))

(when (seq *command-line-args*) (run *command-line-args*))
