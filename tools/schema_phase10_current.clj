(ns schema-phase10-current
  "Read-only current-source bridge for the PR565 phase-10 schema proposal.

   The proposal is historical input: this checker authenticates its exact
   source facets against the current requirement manifest, but never applies
   its old task-field operations to a changed task predecessor."
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]))

(def historical-head
  "cb7671e95976add7b47ee4dcd84caeee18423eff")
(def historical-task-source
  "86747916c4c876d707a1548113409fb939d854c9")
(def current-source
  "dbeda7c7dc9a7bb880baa90af1a89046828db327")
(def proposal-path
  "specs/requirement-reconciliation/schema-family/decision-allocation.edn")
(def manifest-path "contracts/requirement-reconciliation.edn")
(def task-path "specs/domains-workflow/tasks.md")
(def historical-task-sha
  "5abf39bc1af6040f84b4e4db77ee93ee000dc14112e8f636970833d0f945e382")
(def historical-task-tree
  "e3f5bb1c91e3ad59ddb64fceef4eccdb07b9960b")
(def current-manifest-sha
  "d24ed46e2de0c1672a1a37c0eeacb3340452dcf56ce1b511ecc678761909155b")
(def current-task-sha
  "7f67d299cb01671756a442e7165a0b61e5c710c79081daf05c13664d0b9b0fb6")
(def current-source-tree "5b62819e511bfc7e6b5eba0527d8c9b5883c5572")
(def current-source-baseline "a1b991be499d50579a8b4366976c519ae8949224")
(def expected-facet-count 463)
(def expected-facet-digest
  "9daedb26fb3bbee1ce9994d0cbec96cb8803f9428632cdb1a2f61d208850dcf1")
(def expected-document-count 19)
(def expected-document-digest
  "0f3f9f70454ac6765204b23f3daacbe5fe388c694e715c29f2c784a611ec06ad")
(def expected-decision-ids
  ["interface/language/types/project-schema-static-type"
   "interface/safety/safe10/runtime-authority"
   "interface/schema/authorize-runtime-migration"
   "interface/schema/check-binary-reference-layout"
   "interface/schema/check-canonical-identity-input"
   "interface/schema/check-client-operation"
   "interface/schema/compatibility-diff"
   "interface/schema/decode-canonical-value"
   "interface/schema/decode-declared-format"
   "interface/schema/emit-binary-codec-binding-input"
   "interface/schema/emit-database-structure"
   "interface/schema/emit-explicit-binary-layout"
   "interface/schema/emit-graphql-resolver-adapter"
   "interface/schema/emit-graphql-sdl"
   "interface/schema/emit-http-boundary-validators"
   "interface/schema/emit-openapi-document"
   "interface/schema/emit-typed-api-client"
   "interface/schema/emit-typed-migration-plan"
   "interface/schema/emit-typed-row-adapter"
   "interface/schema/encode-canonical-value"
   "interface/schema/encode-declared-format"
   "interface/schema/expand-authoritative-schema"
   "interface/schema/generate-source-validator"
   "interface/schema/run-http-contract-tests"
   "interface/schema/validate-migration-change"])
(def expected-criterion-ids
  ["schema/graphql-generation/full-source-interface"
   "schema/database-mapping/full-source-interface"
   "schema/configuration-reload/full-source-interface"
   "schema/artifact-validation/full-source-interface"
   "schema/release-evidence-gating/full-source-interface"])

(defn require! [condition message]
  (when-not condition
    (throw (ex-info message {:diagnostic "PR565-CURRENT-SOURCE"}))))

(defn sha256 [text]
  (format "%064x"
          (BigInteger. 1 (.digest
                           (java.security.MessageDigest/getInstance "SHA-256")
                           (.getBytes ^String text "UTF-8")))))

(defn git-text [ref path]
  (let [{:keys [exit out err]} (shell/sh "git" "show" (str ref ":" path))]
    (require! (zero? exit) (str "Missing immutable Git input " ref ":" path " " err))
    out))

(defn git-tree [ref]
  (let [{:keys [exit out err]} (shell/sh "git" "rev-parse" "--verify"
                                         (str ref "^{tree}"))]
    (require! (and (zero? exit) (re-matches #"[0-9a-f]{40}" (str/trim out)))
              (str "Missing immutable Git tree " ref " " err))
    (str/trim out)))

(defn historical-task! [ref expected-sha expected-tree]
  (let [text (git-text ref task-path)
        tree (git-tree ref)]
    (require! (= expected-tree tree)
              "Historical task Git tree changed")
    (require! (= expected-sha (sha256 text))
              "Historical task source bytes changed")
    {:text text :tree tree :sha256 (sha256 text)}))

(def historical-proposal
  (edn/read-string (git-text historical-head proposal-path)))
(def current-manifest-text (git-text current-source manifest-path))
(def current-manifest (edn/read-string current-manifest-text))
(def current-task-text (slurp task-path))

(def facet-keys
  [:id :document-id :source-occurrence-id :kind :start-byte :end-byte :raw-sha256])

(defn facet-row [facet]
  (select-keys facet facet-keys))

(defn source-pair!
  ([] (source-pair! historical-proposal))
  ([proposal]
  (require! (= current-manifest-sha (sha256 current-manifest-text))
            "Current manifest bytes are not the authenticated applying source")
  (require! (= current-source-tree (:source-tree current-manifest))
            "Current source tree identity changed")
  (require! (= current-source-baseline (:source-baseline current-manifest))
            "Current source baseline identity changed")
  (let [{historical-task-text :text} (historical-task! historical-task-source
                                                        historical-task-sha
                                                        historical-task-tree)
        witnesses (:source-facet-witnesses proposal)
        historical-facets (mapv #(facet-row (:metadata %)) witnesses)
        current-by-id (into {} (map (juxt :id identity) (:source-facets current-manifest)))
        historical-by-id (into {} (map (juxt :id identity) historical-facets))
        historical-ids (set (keys historical-by-id))
        current-docs (into {} (map (juxt :id identity) (:original-documents current-manifest)))
        document-pairs
        (->> witnesses
             (map (fn [w] [(get-in w [:metadata :document-id])
                           (:document-sha256 w)]))
             set
             sort
             vec)]
    (require! (= expected-facet-count (count historical-facets)
                       (count historical-ids))
              "Source facet population is lost or duplicated")
    (require! (set/subset? historical-ids (set (keys current-by-id)))
              "A historical source facet is absent from the current manifest")
    (doseq [[id historical] historical-by-id
            :let [current (get current-by-id id)]]
      (require! (= historical (facet-row current))
                (str "Historical/current source facet mismatch " id)))
    (doseq [witness witnesses
            :let [document-id (get-in witness [:metadata :document-id])
                  current (get current-docs document-id)]]
      (require! current (str "Missing current source document " document-id))
      (require! (= (:path witness) (:path current))
                (str "Historical witness path mismatch " document-id))
      (require! (= (:document-sha256 witness) (:sha256 current))
                (str "Historical witness document hash mismatch " document-id)))
    (doseq [[document-id document-sha] document-pairs
            :let [current (get current-docs document-id)]]
      (require! current (str "Missing current source document " document-id))
      (require! (= document-sha (:sha256 current))
                (str "Historical/current document hash mismatch " document-id)))
    (let [facet-digest (sha256 (pr-str (vec (sort-by :id historical-facets))))
          document-digest (sha256 (pr-str document-pairs))]
      (require! (= expected-facet-digest facet-digest)
                "Attached facet digest changed")
      (require! (= expected-document-count (count document-pairs))
                "Attached document population changed")
      (require! (= expected-document-digest document-digest)
                "Attached document digest changed")
      {:facets (count historical-facets)
       :documents (count document-pairs)
       :facet-digest facet-digest
       :document-digest document-digest
       :historical-task-tree historical-task-tree
       :historical-task-sha256 (sha256 historical-task-text)}))))

(defn proposal! [proposal]
  (let [operations (:decision-field-proposals proposal)
        ids (mapv :id operations)]
    (require! (= 25 (count operations) (count (set ids)))
              "Decision allocation count or uniqueness changed")
    (require! (= expected-decision-ids ids)
              "Decision allocation IDs/order changed")
    (require! (= 25 (get-in proposal [:counts :assigned-decisions]))
              "Decision allocation count is not 25")
    (require! (= 833 (get-in proposal [:counts :implementation-decisions-open]))
              "Historical Open decision population changed")
    (doseq [operation operations
            :let [before (:before-record operation)
                  after (:after operation)]]
      (require! (= :open (:status before))
                (str "Historical decision is not Open " (:id operation)))
      (require! (not (contains? before :criterion-task-allocation))
                (str "Historical decision already allocated " (:id operation)))
      (require! (and (= :unassessed (:implementation-acceptance after))
                     (= :pending (:independent-review after))
                     (= :pending (:canonical-adoption after)))
                (str "Decision allocation falsely claims acceptance " (:id operation)))
      (require! (seq (:source-facet-ids after))
                (str "Decision allocation lost source facets " (:id operation)))
      (require! (seq (:criterion-witnesses after))
                (str "Decision allocation lost criterion witness " (:id operation))))
    {:decisions (count operations)
     :ids (count (set ids))
     :open-before 833
     :implementation-acceptance :unassessed
     :canonical-adoption :pending}))

(defn task! [text]
  (require! (= current-task-sha (sha256 text))
            "Current task bytes changed without a new reconciled predecessor")
  (require! (= 1 (count (re-seq #"(?m)^- \[ \] `schema` Schema$" text)))
            "Schema task is missing, renamed or duplicated")
  (require! (= 7 (count (re-seq #"(?m)^- \[ \] `[^`]+` " text)))
            "Domain task population changed")
  (require! (= 1 (count (re-seq #"(?m)^- \[ \] `schema` Schema\n\nStatus: Open\n" text)))
            "Schema task status is not Open")
  (require! (str/includes? text "Source-specific acceptance for S1-S9 remains open.")
            "S1-S9 source acceptance is missing")
  (require! (str/includes? text "arbitrary input/work quotas confer no acceptance authority")
            "Accounting-only quota language was not retired")
  (let [ids (mapv second (re-seq #"(?m)^Criterion `([^`]+)` \(Open\)\." text))]
    (require! (= expected-criterion-ids ids)
              "Schema criterion IDs/order/status changed")
    {:task-sha256 (sha256 text) :schema-criteria (count ids) :task-count 7}))

(defn catches? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(defn negative-controls! [proposal source-summary]
  (let [bad [(update proposal :decision-field-proposals pop)
             (update proposal :decision-field-proposals conj
                     (first (:decision-field-proposals proposal)))
             (assoc-in proposal [:decision-field-proposals 0 :id] "forged-id")
             (assoc-in proposal [:decision-field-proposals 0 :before-record :status] :accepted)
             (assoc-in proposal [:decision-field-proposals 0 :after :implementation-acceptance]
                       :accepted)
             (update proposal :decision-field-proposals
                     (fn [operations]
                       (assoc (vec operations)
                              0 (nth operations 1)
                              1 (nth operations 0))))]]
    (require! (every? (fn [x] (catches? #(proposal! x))) bad)
              "Decision/facet tamper control accepted")
    (doseq [mutated [(assoc-in proposal [:source-facet-witnesses 0 :metadata :raw-sha256]
                                  "forged")
                     (assoc-in proposal [:source-facet-witnesses 0 :metadata :id]
                                  "forged-facet")]]
      (require! (catches?
                 (fn []
                   (let [rows (mapv (fn [w] (facet-row (:metadata w)))
                                    (:source-facet-witnesses mutated))]
                     (require! (= expected-facet-digest
                                  (sha256 (pr-str (vec (sort-by :id rows)))))
                               "Facet digest changed"))))
                "Facet tamper control accepted"))
    (require! (catches? #(task! (str/replace current-task-text
                                              "schema/graphql-generation/full-source-interface"
                                              "schema/forged-interface")))
              "Task criterion-ID tamper control accepted")
    (require! (catches?
               #(source-pair!
                 (assoc-in proposal [:source-facet-witnesses 0 :path]
                           "docs/forged/unrelated-source.md")))
              "Witness document-path tamper control accepted")
    (doseq [[ref expected-sha expected-tree message]
            [[(apply str (repeat 40 "0")) historical-task-sha historical-task-tree
              "Historical task ref tamper control accepted"]
             [historical-task-source (apply str (repeat 64 "0")) historical-task-tree
              "Historical task hash tamper control accepted"]
             [historical-task-source historical-task-sha (apply str (repeat 40 "0"))
              "Historical task tree tamper control accepted"]]]
      (require! (catches? #(historical-task! ref expected-sha expected-tree))
                message))
    (require! (= 463 (:facets source-summary))
              "Source summary changed during negative controls")
    (+ (count bad) 3 1 3)))

(when (= ["--check"] (vec *command-line-args*))
  (let [source-summary (source-pair!)
        proposal-summary (proposal! historical-proposal)
        task-summary (task! current-task-text)
        negative-controls (negative-controls! historical-proposal source-summary)]
    (prn {:kind :pr565-current-source-pair
          :historical-head historical-head
          :historical-task-source historical-task-source
          :current-source current-source
          :current-manifest-sha256 current-manifest-sha
          :historical-task-sha256 historical-task-sha
          :current-task-sha256 current-task-sha
          :source-pair source-summary
          :proposal proposal-summary
          :task task-summary
          :task-transition :historical-field-operations-held
          :negative-controls negative-controls
          :implementation-acceptance :unassessed
          :canonical-adoption :pending
          :no-denominator-renewal true
          :no-accounting-admission true})
    (shutdown-agents)))
