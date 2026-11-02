(ns profile-requirements
  "Current-source qualification for the original profile proposals. No compiler launch or canonical write."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def source-view-path "specs/requirement-reconciliation/profile-system/current-source-view.edn")
(def proposal-path "specs/requirement-reconciliation/profile-system/proposal.edn")
(def gap-path "specs/requirement-reconciliation/profile-system/gap-completion.edn")
(def decision-path "specs/requirement-reconciliation/profile-system/decision-allocation.edn")

(defn sha256 [s]
  (format "%064x"
          (BigInteger. 1
                       (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                (.getBytes ^String s "UTF-8")))))

(defn require! [ok message]
  (when-not ok
    (throw (ex-info message {:diagnostic "PROFILE-REQUIREMENTS"}))))

(defn git-text [ref path]
  (let [{:keys [exit out]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Pinned Git source unavailable: " ref ":" path))
    out))

(defn git-kind! [commit]
  (let [{:keys [exit out]} (shell/sh "git" "cat-file" "-t" commit)]
    (require! (and (zero? exit) (= "commit" (str/trim out)))
              (str "Pinned Git commit unavailable: " commit))))

(defn verify-commit-tree! [{:keys [commit tree]}]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit)
                 (string? tree) (re-matches #"[0-9a-f]{40}" tree))
            "Invalid pinned commit/tree")
  (git-kind! commit)
  (let [{:keys [exit out]} (shell/sh "git" "rev-parse" "--verify" (str commit "^{tree}"))]
    (require! (and (zero? exit) (= tree (str/trim out)))
              (str "Pinned commit/tree mismatch: " commit))))

(defn verify-commit! [{:keys [commit]}]
  (require! (and (string? commit) (re-matches #"[0-9a-f]{40}" commit))
            "Invalid pinned commit")
  (git-kind! commit))

(defn view []
  (edn/read-string (slurp source-view-path)))

(defn pinned-edn [{:keys [commit ref path sha256]}]
  (let [source (or commit ref) text (git-text source path)]
    (require! (= sha256 (profile-requirements/sha256 text))
              (str "Pinned input digest differs: " path))
    (edn/read-string text)))

(defn family? [id]
  (boolean (re-matches #"P(?:[1-9]|1[0-3])" id)))

(defn byte-slice [text start end]
  (String. (java.util.Arrays/copyOfRange (.getBytes ^String text "UTF-8")
                                         (int start) (int end)) "UTF-8"))

;; Execute only the three exact public declarations from the pinned occurrence
;; API. The compiler, manifest and proof entrypoints are never loaded.
(defn load-occurrence-api! [api]
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader.
                       (git-text (:commit api) (:path api))))]
    (doseq [form (take-while #(not= ::eof %) (repeatedly #(read {:eof ::eof} reader)))
            :when (and (seq? form) (= 'defn (first form))
                       (#{'sha256 'require! 'source-occurrences} (second form)))]
      (binding [*ns* (the-ns 'profile-requirements)]
        (eval form)))))
(declare source-occurrences)

(defn task-record [text marker id]
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")]
    (require! (and (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote start)) text)))
                   (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote end)) text))))
              "Missing or repeated task marker")
    (let [body (subs text (+ (.indexOf text start) (count start)) (.indexOf text end))
          records (str/split (str/trim body) #"\n\n(?=- \[[ x]\])")
          matches (filter #(re-find (re-pattern (str "^- \\[[ x]\\] `"
                                                       (java.util.regex.Pattern/quote id) "` ")) %)
                          records)]
      (require! (= 1 (count matches)) "Missing, renamed or repeated literal task ID")
      (first matches))))

(defn task-open! [text marker id]
  (let [record (task-record text marker id)]
    (require! (str/starts-with? record (str "- [ ] `" id "` "))
              (str "Current task is accepted or has changed status: " marker "/" id))
    (require! (re-find #"(?m)^(?:Status|Current evidence/status): Open(?:;.*)?$" record)
              (str "Current task is not Open: " marker "/" id))
    record))

(defn task-ids [text marker]
  (let [start (str "<!-- " marker ":start -->")
        end (str "<!-- " marker ":end -->")
        body (subs text (+ (.indexOf text start) (count start)) (.indexOf text end))]
    (mapv #(vec (rest %))
          (re-seq #"(?m)^- \[([ x])\] `([^`]+)` " body))))

(defn apply-field [rows {:keys [collection id field-path before-present? before after]}]
  (require! (= :requirements collection) "Unexpected requirement edit collection")
  (require! (= [:profile-family-task-interface] field-path)
            "Edit would overwrite existing authority")
  (require! (contains? rows id) "Edit targets missing requirement")
  (let [record (get rows id) k (first field-path)]
    (require! (= before-present? (contains? record k)) "Stale or duplicate field application")
    (require! (= before (get record k)) "Field before value differs")
    (assoc rows id (assoc record k after))))

(defn validate-historical-proposal! [p manifest inputs source-texts task-texts]
  (let [documents (filterv #(family? (:id %)) (:original-documents inputs))
        facets (filterv #(family? (:document-id %)) (:source-facets manifest))
        ids (set (mapcat :unit-ids facets))
        records (into {} (map (juxt :id identity) (:requirements manifest)))
        task-inputs (into {} (map (juxt (fn [t] [(:path t) (:id t)]) identity)
                                  (:canonical-task-inputs inputs)))
        dispositions (filterv #(re-find #"^P[0-9]+/source/" (:occurrence-id %))
                              (:source-dispositions manifest))
        occurrences (vec (mapcat (fn [d]
                                   (map #(dissoc % :context)
                                        (source-occurrences (:id d) (get source-texts (:id d)))))
                                 documents))]
    (require! (= "requirement-reconciliation/phase-03-profile-system" (:scope-key p))
              "Wrong source family")
    (require! (= documents (:source-documents p)) "Lost or invented document pin")
    (doseq [d documents]
      (require! (= (:current-sha256 d) (:original-sha256 d)
                   (sha256 (get source-texts (:id d))))
                "Wrong current/original source hash"))
    (require! (= occurrences (:source-occurrences p)) "Lost or changed raw occurrence")
    (require! (= facets (:source-facets p)) "Lost facet, negative condition or source context")
    (require! (= dispositions (:source-dispositions p))
              "Lost or changed source disposition")
    (let [aliases (filterv #(and (= :reviewed (:review-state %))
                                 (or (ids (:id %)) (ids (:target %))))
                           (:aliases manifest))]
      (require! (= aliases (:aliases p)) "Alias changed or context discarded"))
    (doseq [f facets]
      (let [text (get source-texts (:document-id f))]
        (require! (= (:raw-sha256 f)
                     (sha256 (byte-slice text (:start-byte f) (:end-byte f))))
                  "Wrong facet raw byte hash")
        (require! (or (= :context (:kind f)) (seq (:unit-ids f)))
                  "Normative facet lacks an owner")))
    (require! (= ids (set (map :proposed-id (:units p))))
              "Missing or invented semantic ID")
    (require! (= (count ids) (count (:units p)) (count (:edits p)))
              "Duplicate unit or edit")
    (require! (= (:inherited-decisions p)
                 (vec (for [r (:scope-model-reviews manifest)
                            d (:decisions r) :when (ids (:id d))]
                        {:review (select-keys r [:family :candidate-ref :artifact-sha256 :qualification])
                         :decision d})))
              "Inherited decision lineage changed")
    (doseq [u (:units p)]
      (let [record (get records (:proposed-id u))]
        (require! (= (:obligation record) (:obligation u)) "Obligation/context changed")
        (require! (= (:inherited-record-sha256 u) (sha256 (pr-str record)))
                  "Historical/decision lineage changed")
        (require! (= (:historical-support record) (:historical-support u))
                  "Historical support changed")
        (require! (= (select-keys record [:atomicity :nonoverlap])
                     (select-keys u [:atomicity :nonoverlap]))
                  "Inherited review state changed")
        (require! (= (:equivalence-proposals u)
                     (vec (for [d (:inherited-decisions p)
                                :when (= (:id record) (get-in d [:decision :id]))]
                            (:decision d))))
                  "Unit lost its split/alias/qualification decision")
        (require! (= (mapv :id (filter #(some #{(:id record)} (:unit-ids %)) facets))
                     (:source-facet-ids u))
                  "Unit lost a contextual facet")
        (require! (= (mapv #(select-keys % [:id :source-occurrence-id :start-byte :end-byte
                                             :start-line :end-line :raw-sha256 :kind :unresolved])
                            (filter #(some #{(:id record)} (:unit-ids %)) facets))
                     (:acceptance-facets u))
                  "Unit acceptance span differs")
        (require! (= :unassessed (:current-acceptance u))
                  "Proposal inflated implementation acceptance")
        (require! (seq (:task-gaps u)) "Unassessed implementation qualification erased")
        (doseq [reference (concat (:task-references u) (:dependencies u))]
          (let [task (get task-inputs [(:path reference) (:id reference)])
                text (get task-texts (:path reference))]
            (require! task "Task reference absent from frozen canonical population")
            (require! (= (:sha256 reference) (:file-sha256 task) (sha256 text))
                      "Wrong historical applying task hash")
            (require! (= (:source-ref reference) (:source-commit inputs))
                      "Wrong applying source commit")
            (require! (= (:marker reference) (str (:component task) "-implementation-tasks"))
                      "Wrong task marker")
            (task-record text (:marker reference) (:id reference))))))
    (require! (= (:edits p)
                 (mapv (fn [u]
                         {:collection :requirements :id (:proposed-id u)
                          :field-path [:profile-family-task-interface]
                          :before-present? false :before nil
                          :after (select-keys u [:task-references :dependencies
                                                 :task-amendment-id :task-gaps
                                                 :current-acceptance])})
                       (:units p)))
              "Guarded field edit differs from reviewed unit proposal")
    (doseq [e (:task-edits p)]
      (require! (= [:text] (:field-path e)) "Unexpected task field")
      (require! (= :task-source-records (:collection e)) "Unexpected task collection")
      (require! (= (:source-sha256 e) (sha256 (get task-texts (:path e))))
                "Stale task-source amendment")
      (require! (= (:before e) (task-record (get task-texts (:path e)) (:marker e) (:id e)))
                "Task amendment before differs")
      (let [expected (case (:insertion e)
                       :work-before-source-basis
                       (do (require! (= "platform-implementation-tasks" (:marker e))
                                     "Work insertion has wrong task owner")
                           (require! (= 1 (count (re-seq #"\n\nSource basis:" (:before e))))
                                     "Work insertion lacks exact evidence boundary")
                           (str/replace-first (:before e) "\n\nSource basis:"
                                              (str (:addition e) "\n\nSource basis:")))
                       :record-tail (str (:before e) (:addition e))
                       (throw (ex-info "Unknown task insertion" {})))]
        (require! (and (:before-present? e) (seq (:addition e)) (= expected (:after e)))
                  "Task amendment loses existing acceptance")
        (require! (str/starts-with? (:after e) (str "- [ ] `" (:id e) "`"))
                  "Task status or ID changed")))
    (reduce apply-field records (:edits p))
    {:documents (count documents) :occurrences (count occurrences) :facets (count facets)
     :dispositions (count dispositions) :units (count ids)
     :task-record-amendments (count (:task-edits p))
     :requirement-field-edits (count (:edits p))
     :unique-task-references (count (set (mapcat #(concat (:task-references %)
                                                        (:dependencies %)) (:units p))))}))

(defn task-amendment-projection! [p historical-task-texts]
  ;; This is deliberately source-only: no retired roadmap generator is loaded.
  ;; It proves the proposed record replacement keeps IDs/status/marker ownership,
  ;; and keeps Platform acceptance in its work field rather than Source basis.
  (doseq [[path edits] (sort-by key (group-by :path (:task-edits p)))]
    (let [before-text (get historical-task-texts path)
          before-ids (task-ids before-text (:marker (first edits)))
          after-text (reduce (fn [text e]
                               (require! (= 1 (count (re-seq (re-pattern
                                                               (java.util.regex.Pattern/quote
                                                                (:before e))) text)))
                                         "Ambiguous task replacement span")
                               (str/replace-first text (:before e) (:after e)))
                             before-text edits)
          after-ids (task-ids after-text (:marker (first edits)))]
      (require! (= before-ids after-ids) "Task amendment changed IDs or statuses")
      (when (= "specs/platform-workflow/tasks.md" path)
        (doseq [e edits]
          (let [before (task-record before-text (:marker e) (:id e))
                after (task-record after-text (:marker e) (:id e))
                source-before (second (re-find #"(?s)\n\nSource basis:.*" before))
                source-after (second (re-find #"(?s)\n\nSource basis:.*" after))]
            (require! (= source-before source-after) "Platform source basis changed")
            (require! (str/includes? after (:addition e))
                      "Platform acceptance landed outside work field"))))))
  {:task-record-amendments (count (:task-edits p))
   :paths (count (set (map :path (:task-edits p))))})

(defn accounting-safe! [v]
  (let [texts (concat (map :addition (:task-edits v))
                      (map #(pr-str (:task-gaps %)) (:units v))
                      (map #(pr-str (:obligation %)) (:units v)))
        accounting #"(?i)(?:compiler|project)\s+(?:work\s+)?(?:quota|budget|allowance|limit)"
        positive (fn [s]
                   (and (re-find #"(?i)(?:enforce|require|admit|reject|exhaust|bound)"
                                 s)
                        (re-find accounting s)
                        (not (re-find (re-pattern
                                       (str "(?i)(?:no|not|never|without|removed|retired|external|physical|do\\s+not|does\\s+not)"
                                            "[^.\\n]{0,80}" accounting))
                                      s))))]
    (require! (not-any? positive texts)
              "Profile proposal introduces compiler/project accounting enforcement")
    true))

(defn source-documents [manifest]
  (filterv #(family? (:id %)) (:original-documents manifest)))

(defn family-manifest-view [manifest]
  (let [docs (source-documents manifest)
        ids (set (map :id docs))
        facets (filterv #(ids (:document-id %)) (:source-facets manifest))
        fids (set (map :id facets))]
    {:documents docs
     :facets facets
     :requirements (filterv #(some fids (:source-facet-ids %)) (:requirements manifest))
     :dispositions (filterv #(re-find #"^P[0-9]+/source/" (:occurrence-id %))
                             (:source-dispositions manifest))
     :aliases (filterv #(and (= :reviewed (:review-state %))
                             (or (fids (:id %)) (fids (:target %))))
                       (:aliases manifest))}))

(defn validate-gap-source-records! [gap manifest baseline]
  (let [documents (:source-documents gap)
        manifest-documents (into {} (map (juxt :id identity)
                                         (:original-documents manifest)))
        expected-documents (mapv #(select-keys (manifest-documents (:id %))
                                               [:id :sequence :path :sha256])
                                 documents)
        facets (into {} (map (juxt :id identity) (:source-facets manifest)))
        docs (into {} (map (juxt :id identity) documents))]
    (require! (= documents expected-documents)
              "Gap source-document population changed")
    (doseq [{:keys [path] :as document} documents]
      (require! (= (:sha256 document) (sha256 (git-text baseline path)))
                (str "Gap source document drift: " path)))
    (doseq [mapping (:mappings gap)
            :let [predicates (get-in mapping [:after :source-predicates])
                  dispatch-ids (:source-facet-ids (:dispatch mapping))]]
      (require! (= dispatch-ids (mapv :id predicates))
                "Gap source-facet order or identity changed")
      (doseq [predicate predicates
              :let [canonical (facets (:id predicate))
                    document (docs (:document-id predicate))]]
        (require! canonical "Gap source facet is missing from the manifest")
        (require! document "Gap source document is missing from the proposal")
        (require! (= canonical (dissoc predicate :raw-text :acceptance-role))
                  "Gap source facet value changed")
        (let [text (git-text baseline (:path document))
              raw (byte-slice text (:start-byte predicate) (:end-byte predicate))]
          (require! (= raw (:raw-text predicate))
                    "Gap source facet raw text changed")
          (require! (= (:raw-sha256 predicate) (sha256 raw))
                    "Gap source facet raw hash changed")
          (require! (= (:acceptance-role predicate)
                       (case (:kind predicate)
                         :context :supporting-context
                         :syntax-or-schema-example :illustrative-shape-not-mandatory-literal
                         :required-source-predicate))
                    "Gap source facet acceptance role changed"))))
    {:documents (count documents)
     :source-facets (count (mapcat #(get-in % [:after :source-predicates]) (:mappings gap)))}))

(defn validate-gap! [gap partition]
  (require! (= :profile-historical-gap-completion-proposal (:kind gap))
            "Wrong profile gap proposal kind")
  (require! (= :finite-actionable-interface-proposals-only (:authority gap))
            "Gap proposal is not proposal-only")
  (require! (= "requirement-reconciliation/phase-03-profile-system" (:scope-key gap))
            "Wrong gap proposal scope")
  (require! (= {:assigned-gaps 34 :completed-proposal-mappings 34
                :existing-full-criterion-witnesses 0 :proposed-open-criterion-interfaces 34
                :adopted-mapping-completions 0 :implementation-accepted 0
                :canonical-task-count-delta 0 :task-record-amendments 12}
               (:counts gap))
            "Gap count or acceptance status changed")
  (let [ids (:assigned-ids (:partition gap))
        mappings (:mappings gap)
        historical-ids (some #(when (= "phase-03" (:family %)) (:ids %))
                             (:partitions partition))
        residuals (into {} (map (juxt :id identity) (:residual-records partition)))
        predecessor-ref (get-in gap [:predecessor :ref])]
    (require! (= ids historical-ids) "Gap assignment partition changed")
    (require! (and (= (set ids) (set (map :id mappings)))
                   (= (count ids) (count mappings)))
              "Gap mapping identity population changed")
    (doseq [m mappings
            :let [a (:after m) e (:decision-edit m)
                  dispatch (get residuals (:id m))]]
      (require! (= (:dispatch m) dispatch)
                "Gap dispatch source record changed")
      (require! (and (= (str "profile-gap/" (:id m)) (:criterion-id a))
                     (= (str "interface/" (:id m)) (:id e)))
                "Gap mapping/edit identity differs")
      (require! (and (= :open (:status a))
                     (= :pending-canonical-review-and-guarded-task-composition (:adoption a))
                     (= :unassessed (:implementation-acceptance a))
                     (= :not-conferred (:whole-facet-entailment-acceptance a))
                     (seq (:inputs a)) (seq (:negative-cases a)) (seq (:outputs a))
                     (seq (:source-predicates a)) (:task-reference a)
                     (seq (:dependencies a)))
                "Gap criterion is incomplete or falsely accepted")
      (require! (and (= :remaining-decisions (:collection e))
                     (= [:mapping-completion-proposal] (:field-path e))
                     (false? (:before-present? e)) (nil? (:before e)))
                "Gap edit is not an absent-field guard"))
    (doseq [m mappings
            reference (concat [(:task-reference (:after m))]
                              (map :task-reference (:dependencies (:after m))))]
      (require! (= predecessor-ref (:ref reference))
                "Gap task reference predecessor changed")
      (let [text (git-text predecessor-ref (:path reference))]
        (require! (= (:sha256 reference) (sha256 text))
                  "Gap task reference hash changed")
        (task-record text (:marker reference) (:id reference))))
    (doseq [e (:task-edits gap)]
      (require! (= predecessor-ref (:ref e))
                "Gap task amendment predecessor changed")
      (let [text (git-text predecessor-ref (:path e))]
        (require! (= (:sha256 e) (sha256 text))
                  "Gap task amendment source hash changed")
        (require! (= (:before e) (task-record text (:marker e) (:id e)))
                  "Gap task amendment before record changed")
        (require! (str/starts-with? (:after e) (str "- [ ] `" (:id e) "`"))
                  "Gap task amendment changed task status or identity")
        (let [mapped (filter #(and (= (:path e) (get-in % [:after :task-reference :path]))
                                    (= (:id e) (get-in % [:after :task-reference :id])))
                             mappings)]
          (require! (= (:historical-ids e) (mapv :id mapped))
                    "Gap task amendment mapping IDs changed")
          (require! (= (:criterion-ids e)
                       (mapv #(get-in % [:after :criterion-id]) mapped))
                    "Gap task amendment criterion IDs changed")
          (doseq [criterion-id (:criterion-ids e)]
            (require! (str/includes? (:after e) criterion-id)
                      "Gap criterion was not inserted into its task field")))))
    (require! (= 12 (count (:task-edits gap))) "Gap task amendment count changed")
    {:mappings (count mappings) :task-record-amendments (count (:task-edits gap))}))

(defn validate-decision! [decision expected-order-sha256]
  (require! (= :language-profile-decision-task-allocation-proposal (:kind decision))
            "Wrong decision allocation kind")
  (require! (= :guarded-proposal-only (:authority decision))
            "Decision allocation is not proposal-only")
  (require! (= {:assigned 74 :reused-reviewed-interface-proposals 31
                :existing-current-criterion-witnesses 1 :new-criterion-proposals 42
                :canonical-task-count-delta 0 :implementation-accepted 0
                :canonical-adopted 0}
               (:counts decision))
            "Decision allocation count or acceptance status changed")
  (let [rows (:rows decision)
        ids (map :id rows)]
    (require! (= 74 (count rows) (count (set ids)))
              "Decision row identity population changed")
    (require! (= expected-order-sha256 (sha256 (pr-str (mapv :id rows))))
              "Decision row order changed")
    (require! (= {"phase-01" 64 "phase-03" 10} (frequencies (map :family rows)))
              "Decision family population changed")
    (doseq [r rows
            :let [o (:operation r) a (:after o)
                  criterion (or (:reviewed-proposed-interface a) a)]]
      (require! (= (:id r) (:id o)) "Decision operation identity differs")
      (require! (and (= :remaining-decisions (:collection o))
                     (= [:criterion-task-allocation] (:field-path o))
                     (false? (:before-present? o)) (nil? (:before o))
                     (= (:family r) (get-in r [:dispatch :primary-dispatch-family]))
                     (= :open (:status a)) (= :unassessed (:implementation-acceptance a))
                     (= :pending (:canonical-adoption a))
                     (#{:not-conferred :bounded-operation-only}
                      (:current-criterion-entailment a))
                     (seq (:positive criterion)) (seq (:negative criterion))
                     (seq (:outputs criterion)))
                "Decision criterion is incomplete or falsely accepted"))
    (require! (= 2 (count (:unresolved-source-index decision)))
              "Unresolved source-index residual changed")
    (require! (= :unchanged (get-in decision [:conservation :all-open-statuses]))
              "Decision source statuses were changed")
    {:rows (count rows) :families (frequencies (map :family rows))}))

(defn validate-current-view! [v proposal manifest-old manifest-current]
  (require! (= :gravity/noncanonical-profile-current-source-view (:kind v))
            "Wrong profile current-source-view kind")
  (require! (= 1 (:version v)) "Unsupported profile current-source-view version")
  (require! (= :proposal-only (:authority v)) "Profile source-view is not proposal-only")
  (require! (= "requirement-reconciliation/phase-03-profile-system" (:scope-key v))
            "Wrong profile source-view scope")
  (verify-commit-tree! (:base v))
  (verify-commit-tree! (get-in v [:historical-source :candidate]))
  (verify-commit-tree! (get-in v [:historical-source :source-baseline]))
  (doseq [entry [(:input-manifest (:historical-source v))
                 (:frozen-inputs (:historical-source v))
                 (:proposal-format (:historical-source v))
                 (:occurrence-api (:historical-source v))
                 (:gap-partition (:historical-source v))
                 (:decision-predecessor (:historical-source v))]]
    (verify-commit! entry))
  (verify-commit! (get-in v [:historical-source :decision-partition]))
  (let [candidate (get-in v [:historical-source :candidate :commit])]
    (doseq [{:keys [path historical-sha256] :as artifact}
            (get-in v [:historical-source :artifacts])]
      (let [artifact-sha256 (:sha256 artifact)
            expected (or historical-sha256 artifact-sha256)]
        (require! (= expected (sha256 (git-text candidate path)))
                  (str "Historical profile artifact drift: " path))
        (when (and artifact-sha256 (not historical-sha256))
          (require! (= artifact-sha256 (sha256 (slurp path)))
                    (str "Local profile artifact drift: " path))))))
  (let [current (family-manifest-view manifest-current)
        historical (family-manifest-view manifest-old)
        current-docs (mapv #(select-keys % [:id :path :sha256])
                           (source-documents manifest-current))
        source-docs (get-in v [:current-source :documents])
        refs (mapcat #(concat (:task-references %) (:dependencies %)) (:units proposal))
        edits (:task-edits proposal)]
    (require! (= (get-in v [:current-source :manifest :sha256])
                 (sha256 (slurp (get-in v [:current-source :manifest :path]))))
              "Current profile manifest drift")
    (require! (= source-docs current-docs) "Current profile document population changed")
    (doseq [{:keys [path] :as source-doc} source-docs]
      (require! (= (:sha256 source-doc) (sha256 (slurp path)))
                (str "Current profile source document drift: " path)))
    (require! (= (:documents historical) (:documents current))
              "Current profile document records changed")
    (require! (= (:facets historical) (:facets current))
              "Current profile facet/source-value population changed")
    (require! (= (:requirements historical) (:requirements current))
              "Current profile requirement records changed")
    (require! (= (:dispositions historical) (:dispositions current))
              "Current profile source dispositions changed")
    (require! (= (:aliases historical) (:aliases current))
              "Current profile alias context changed")
    (doseq [{:keys [path marker] :as source-doc} (get-in v [:current-source :task-sources])]
      (require! (= (:sha256 source-doc) (sha256 (slurp path))) (str "Current task source drift: " path))
      (require! (= 1 (count (re-seq (re-pattern (str "<!-- " marker ":start -->"))
                                 (slurp path))))
                (str "Current task marker drift: " path)))
    (doseq [reference (concat refs (map #(select-keys % [:path :id :marker]) edits))
            :let [source (first (filter #(= (:path %) (:path reference))
                                        (get-in v [:current-source :task-sources])))
                  text (when source (slurp (:path source)))]]
      (require! source "Referenced current task source is not pinned")
      (task-open! text (:marker reference) (:id reference)))
    (require! (= (get-in v [:expected :documents]) (count (:documents current)))
              "Unexpected current profile document count")
    (require! (= (get-in v [:expected :facets]) (count (:facets current)))
              "Unexpected current profile facet count")
    (require! (= (get-in v [:expected :dispositions]) (count (:dispositions current)))
              "Unexpected current profile disposition count")
    (require! (= (get-in v [:expected :requirements]) (count (:requirements current)))
              "Unexpected current profile requirement count")
    (require! (= (get-in v [:expected :unique-task-references])
                 (count (set (map :id refs))))
              "Unexpected current task-reference identity count")
    (require! (= :unassessed (get-in v [:qualification-policy :implementation-acceptance]))
              "Profile current view confers implementation acceptance")
    (require! (= :retired (get-in v [:accounting-policy :compiler-project-accounting]))
              "Compiler project accounting is not explicitly retired")
    (require! (= :not-enforced (get-in v [:accounting-policy :compiler-work-quotas]))
              "Compiler work quotas are not explicitly rejected")
    {:documents (count (:documents current)) :facets (count (:facets current))
     :dispositions (count (:dispositions current)) :requirements (count (:requirements current))
     :current-task-sources (count (get-in v [:current-source :task-sources]))
     :unique-task-references (count (set (map :id refs)))}))

(defn reject! [label thunk]
  (require! (try (thunk) false (catch clojure.lang.ExceptionInfo _ true))
            (str "Negative control accepted: " label))
  label)

(defn run [args]
  (require! (empty? args) "This current-source checker takes no arguments")
  (let [v (view)
        proposal (edn/read-string (slurp proposal-path))
        manifest-old (pinned-edn (get-in v [:historical-source :input-manifest]))
        inputs (pinned-edn (get-in v [:historical-source :frozen-inputs]))
        source-texts (into {} (map (fn [d] [(:id d) (slurp (:path d))])
                                   (:source-documents proposal)))
        paths (set (concat (map :path (:task-edits proposal))
                          (map :path (mapcat #(concat (:task-references %)
                                                      (:dependencies %))
                                             (:units proposal)))))
        historical-tasks (into {} (map (fn [path]
                                         [path (git-text (:source-commit inputs) path)]) paths))
        _ (load-occurrence-api! (get-in v [:historical-source :occurrence-api]))
        counts (validate-historical-proposal! proposal manifest-old inputs
                                               source-texts historical-tasks)
        _ (task-amendment-projection! proposal historical-tasks)
        gap (edn/read-string (slurp gap-path))
        decision (edn/read-string (slurp decision-path))
        gap-partition (pinned-edn (get-in v [:historical-source :gap-partition]))
        gap-source-result (validate-gap-source-records!
                           gap manifest-old
                           (get-in v [:historical-source :source-baseline :commit]))
        manifest-current (edn/read-string (slurp (get-in v [:current-source :manifest :path])))
        current-result (validate-current-view! v proposal manifest-old manifest-current)
        gap-result (validate-gap! gap gap-partition)
        decision-result (validate-decision! decision
                                            (get-in v [:expected :decision-row-order-sha256]))
        _ (accounting-safe! proposal)
        validate-source (fn [mutated]
                          (validate-current-view! mutated proposal manifest-old manifest-current))
        current-reference (first (mapcat #(concat (:task-references %)
                                                  (:dependencies %)) (:units proposal)))
        negatives [(reject! :current-manifest-drift
                            #(validate-source (assoc-in v [:current-source :manifest :sha256] "wrong")))
                   (reject! :reordered-current-docs
                            #(validate-source (update-in v [:current-source :documents] reverse)))
                   (reject! :current-task-status
                            #(let [source (first (filter (fn [candidate]
                                                           (= (:path candidate) (:path current-reference)))
                                                         (get-in v [:current-source :task-sources])))
                                   text (slurp (:path source))
                                   checked (str "- [ ] `" (:id current-reference) "`")
                                   accepted (str "- [x] `" (:id current-reference) "`")]
                               (task-open! (str/replace-first text checked accepted)
                                           (:marker current-reference) (:id current-reference))))
                   (reject! :historical-artifact-drift
                            #(validate-source (assoc-in v [:historical-source :artifacts 0 :sha256]
                                                        "wrong")))
                   (reject! :proposal-false-acceptance
                            #(validate-historical-proposal!
                              (assoc-in proposal [:units 0 :current-acceptance] :accepted)
                              manifest-old inputs source-texts historical-tasks))
                   (reject! :lost-profile-facet
                            #(validate-historical-proposal!
                              (update proposal :source-facets pop)
                              manifest-old inputs source-texts historical-tasks))
                   (reject! :gap-false-adoption
                            #(validate-gap! (assoc-in gap [:counts :adopted-mapping-completions] 34)
                                            gap-partition))
                   (reject! :gap-source-hash
                            #(validate-gap-source-records!
                              (assoc-in gap [:mappings 0 :after :source-predicates 0 :raw-sha256]
                                        "wrong")
                              manifest-old
                              (get-in v [:historical-source :source-baseline :commit])))
                   (reject! :decision-reorder
                            #(validate-decision! (update decision :rows reverse)
                                                 (get-in v [:expected :decision-row-order-sha256])))
                   (reject! :accounting-enforcement
                            #(accounting-safe! (assoc-in proposal [:task-edits 0 :addition]
                                                        "Enforce compiler work quota.")))]
        result {:kind :profile-current-source-controls
                :authority :proposal-only
                :base (:base v)
                :historical-candidate (get-in v [:historical-source :candidate])
                :profile counts
                :current current-result
                :gap-source gap-source-result
                :gap gap-result
                :decision decision-result
                :accounting-policy (:accounting-policy v)
                :negative-controls negatives
                :canonical-writes false
                :implementation-acceptance :unassessed
                :independent-review :pending
                :canonical-adoption :pending
                :nonclaims (:nonclaims v)}]
    (prn result)))

(run *command-line-args*)
(shutdown-agents)
