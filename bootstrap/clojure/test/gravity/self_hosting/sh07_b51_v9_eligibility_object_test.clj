(ns gravity.self-hosting.sh07-b51-v9-eligibility-object-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files OpenOption Path Paths StandardOpenOption]))

(defn- git!
  [^Path root & arguments]
  (let [command (into [evidence/git-executable "-C" (str root)] arguments)
        process (.start (ProcessBuilder. ^java.util.List command))
        stdout-future (future (.readAllBytes (.getInputStream process)))
        stderr-future (future (.readAllBytes (.getErrorStream process)))
        exit (.waitFor process)
        output @stdout-future
        error @stderr-future]
    (when-not (zero? exit)
      (throw (ex-info "test Git command failed"
                      {:command command :exit exit
                       :stdout (String. output StandardCharsets/UTF_8)
                       :stderr (String. error StandardCharsets/UTF_8)})))
    (str/trim-newline (String. output StandardCharsets/UTF_8))))

(defn- write-bytes!
  [^Path root relative bytes]
  (let [path (.resolve root relative)]
    (Files/createDirectories (.getParent path)
                             (make-array java.nio.file.attribute.FileAttribute
                                         0))
    (Files/write path bytes
                 (into-array OpenOption
                             [StandardOpenOption/CREATE
                              StandardOpenOption/TRUNCATE_EXISTING
                              StandardOpenOption/WRITE]))
    path))

(defn- commit!
  [root message]
  (git! root "add" "-A")
  (git! root "commit" "-m" message)
  (git! root "rev-parse" "HEAD"))

(defn- typed-sha [label]
  (codec/H [:gravity/test-sh07-b51-eligibility label]))

(defn- current-repository []
  (.normalize
   (.toAbsolutePath
    (Paths/get (System/getProperty "user.dir") (make-array String 0)))))

(defn- initialize-content!
  [include-a9-terminal?]
  (let [root (Files/createTempDirectory
              "sh07-b51-pre-objects-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        source (current-repository)]
    (git! root "init")
    (git! root "config" "user.name" "Gravity Test")
    (git! root "config" "user.email" "gravity@example.invalid")
    (git! root "fetch" "--no-tags" (str source)
          evidence/pre-baseline-commit)
    (git! root "checkout" "--detach" evidence/pre-baseline-commit)
    (doseq [path evidence/pre-c-paths
            :when (and (not= path evidence/boot5-path)
                       (or include-a9-terminal?
                           (not= path evidence/a9-terminal-path)))]
      (let [source-path (.resolve source path)
            bytes (if (Files/isRegularFile source-path
                                           (make-array java.nio.file.LinkOption
                                                       0))
                    (Files/readAllBytes source-path)
                    (codec/W {:test-object path}))]
        (write-bytes! root path bytes)))
    (let [c (commit! root "C fixture")
          t (git! root "show" "-s" "--format=%T" c)]
      {:root root :content {:commit c :tree t}})))

(defn- pre-carriers
  [snapshot]
  (let [content (:content snapshot)]
    {:content content
     :document-read-receipt (:document-read-receipt snapshot)
     :source-descriptors (:source-descriptors snapshot)
     :source-descriptors-root (:source-descriptors-root snapshot)
     :pre-masked-root (:pre-masked-root snapshot)
     :owned-c-descriptors (:owned-c-descriptors snapshot)
     :owned-c-root (:owned-c-root snapshot)
     :row-plan evidence/row-plan
     :foundation-static (:foundation-static snapshot)
     :boot5-compatibility (:boot5-compatibility snapshot)
     :ownership-plan (:ownership-plan snapshot)
     :catalog-projection (:catalog-projection snapshot)
     :governance-rules (:governance-rules snapshot)
     :identity-domain-registry (:identity-domain-registry snapshot)
     :publication-boundary (:publication-boundary snapshot)
     :proof-scope (:proof-scope snapshot)
     :progress (:progress snapshot)
     :nonclaims evidence/nonclaims
     :a9-stage-result (:a9-stage-result snapshot)}))

(defn- pre-discharge
  [snapshot]
  (let [carriers (pre-carriers snapshot)
        resolved (evidence/resolve-contract-phase! :pre-evidence carriers)
        base
        {:artifact :gravity/sh07-b51-pre-evidence-contract-discharge-v1
         :schema-version 1
         :phase :pre-evidence
         :row-plan-id evidence/row-plan-id
         :prior-ref nil
         :evidence {:schema :gravity/sh07-b51-phase-evidence-v1
                    :phase :pre-evidence
                    :row-plan-id evidence/row-plan-id
                    :carriers carriers}
         :resolved-rows (:resolved-rows resolved)
         :resolved-root (:resolved-root resolved)
         :authority
         {:phase :pre-evidence
          :attained [:a9-terminal-record-integrated]
          :deferred [:c-content-frozen :q-evidence-eligible
                     :fresh-runtime-complete :e9-evidence-immutable
                     :i9-integrated :g9-integrated :p9-integrated
                     :final-cleanup-complete]
          :grants [:contract-discharge-only]
          :aggregate-sh07 (:progress carriers)
          :nonclaims evidence/nonclaims}
         :self-id-omits [[:discharge-id]]}]
    (assoc base :discharge-id
           (codec/H [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
                     base]))))

(defn- eligibility-receipt
  [snapshot]
  (let [content (:content snapshot)
        discharge (pre-discharge snapshot)
        pre-masked-root
        (get-in discharge [:evidence :carriers :pre-masked-root])
        architecture-report-sha256 (typed-sha :architecture-report)
        active-record-raw (typed-sha :active-record)
        base
        {:artifact :gravity/sh07-b51-evidence-eligibility-receipt-v2
         :schema-version 2
         :content content
         :freeze
         {:state :frozen
          :candidate-commit (:commit content)
          :candidate-tree (:tree content)
          :active-record-ref
          {:container
           {:commit (:commit content) :tree (:tree content) :parents []
            :path
            "contracts/workstream-active/sh07-b51-git-object-evidence-publication-implementation-v9.json"
            :mode "100644" :type "blob" :blob-oid (:commit content)
            :byte-count 1 :raw-sha256 active-record-raw}
           :value-path [] :record-id active-record-raw
           :transition
           {:workstream-id
            "sh07-b51-git-object-evidence-publication-implementation-v9"
            :from :draft :to :frozen
            :candidate-commit (:commit content)
            :candidate-tree (:tree content)
            :architecture-report-sha256 architecture-report-sha256
            :history-ordinal 0}}
          :frozen-at "1970-01-01T00:00:00Z"}
         :source-descriptors (:source-descriptors snapshot)
         :source-descriptors-root (:source-descriptors-root snapshot)
         :pre-masked-root pre-masked-root
         :pre-evidence-contract-discharge discharge
         :review
         {:reviewer "independent-test-reviewer"
          :candidate-commit (:commit content)
          :candidate-tree (:tree content)
          :artifact-path "reviews/eligibility.md"
          :artifact-raw-sha256 (typed-sha :review)
          :result :evidence-eligible
          :completed-at "1970-01-01T00:00:00Z"}
         :state :evidence-eligible
         :self-id-omits [[:receipt-id]]
         :nonclaims evidence/nonclaims}]
    (assoc base :receipt-id
           (codec/H
            [:gravity/sh07-b51-evidence-eligibility-receipt-v2 base]))))

(defn- replace-pre-carrier
  [receipt key value]
  (let [discharge (:pre-evidence-contract-discharge receipt)
        carriers (assoc (get-in discharge [:evidence :carriers]) key value)
        resolved (evidence/resolve-contract-phase! :pre-evidence carriers)
        discharge-base
        (-> discharge
            (assoc-in [:evidence :carriers] carriers)
            (assoc :resolved-rows (:resolved-rows resolved)
                   :resolved-root (:resolved-root resolved))
            (dissoc :discharge-id))
        next-discharge
        (assoc discharge-base :discharge-id
               (codec/H
                [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
                 discharge-base]))
        receipt-base
        (-> receipt
            (assoc :pre-evidence-contract-discharge next-discharge)
            (dissoc :receipt-id))]
    (assoc receipt-base :receipt-id
           (codec/H
            [:gravity/sh07-b51-evidence-eligibility-receipt-v2
             receipt-base]))))

(defn- commit-eligibility!
  [root content receipt]
  (let [bytes (codec/W receipt)]
    (write-bytes! root evidence/eligibility-receipt-path bytes)
    (let [q (commit! root "Q")
          tq (git! root "show" "-s" "--format=%T" q)
          row (git! root "ls-tree" q "--" evidence/eligibility-receipt-path)
          oid (nth (re-matches #"100644 blob ([0-9a-f]{40})\t.+" row) 1)]
      {:receipt receipt :bytes bytes
       :coordinate
       {:schema :gravity/sh07-b51-external-review-object-v1
        :commit q :tree tq :parents [(:commit content)]
        :path evidence/eligibility-receipt-path
        :mode "100644" :type "blob" :blob-oid oid
        :byte-count (alength ^bytes bytes)
        :raw-sha256 (codec/R bytes)
        :receipt-id (:receipt-id receipt)}})))

(defn- failure-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest literal-pre-plan-is-closed-and-dense
  (is (= 33 (count evidence/document-paths)))
  (is (= 12 (count evidence/source-specs)))
  (is (= 27 (count evidence/owned-c-paths)))
  (is (= 3 (count evidence/a9-only-paths-e1)))
  (is (= evidence/a9-terminal-path (last evidence/a9-only-paths-e1)))
  (is (= :content-addressed-terminal-record
         (:kind (last evidence/a9-paths-e1))))
  (is (= 72 (count evidence/pre-c-paths)
         (count (distinct evidence/pre-c-paths))))
  (is (= 19 (count evidence/row-plan)))
  (is (= (vec (range 19)) (mapv :ordinal evidence/row-plan)))
  (is (= 62 evidence/planned-binding-count))
  (is (= 124 (count evidence/predicate-specs)))
  (is (= (vec (range 124)) (mapv :ordinal evidence/predicate-specs)))
  (is (= evidence/predicate-spec-id
         (codec/H [:gravity/sh07-b51-contract-predicate-spec-id-v1
                   evidence/predicate-specs]))))

(deftest c-object-pre-foundation-is-exactly-77-bounded-git-operations
  (let [{:keys [root content]} (initialize-content! true)
        ;; Mutable filesystem bytes are not an authority source after C/T.
        _ (write-bytes! root evidence/a9-terminal-path
                        (.getBytes "{}\n" StandardCharsets/UTF_8))
        snapshot (evidence/reconstruct-pre-object-snapshot!
                  {:repository-root root :content content})
        operations (mapv :operation (:observations snapshot))]
    (is (= 77 (:git-operation-count snapshot)
           (count (:observations snapshot))))
    (is (= (vec (range 77)) (mapv :ordinal (:observations snapshot))))
    (is (= :show-tree (first operations)))
    (is (= :ls-tree-z-requested (second operations)))
    (is (= (vec (repeat 72 :cat-file-blob)) (subvec operations 2 74)))
    (is (= [:show-tree :ls-tree-z-requested :cat-file-blob]
           (subvec operations 74 77)))
    (is (every? #(= evidence/git-executable (first (:argv %)))
                (:observations snapshot)))
    (is (= {:acquire-count 33 :decode-count 33
            :event-count 33 :finalize-count 1}
           (get-in snapshot [:document-read-receipt :counters])))
    (is (= 33
           (count (get-in snapshot [:document-read-receipt :documents]))
           (count (get-in snapshot [:document-read-receipt :events]))))
    (is (= 12 (count (:source-descriptors snapshot))))
    (is (= 27 (count (:owned-c-descriptors snapshot))))
    (is (= 3 (count (get-in snapshot
                            [:a9-stage-result :a9-artifact-descriptors]))))
    (is (true? (get-in snapshot
                       [:a9-stage-result :checks :all-checks-pass])))
    (is (= :terminal-record-integrated
           (get-in snapshot [:a9-stage-result :result])))
    (is (= #{:coordinates :preimage-spans :preimage-files :preimage-root}
           (set (keys (:pre-masked-root snapshot)))))
    (is (= 5 (count (get-in snapshot [:pre-masked-root :coordinates]))))
    (is (= 2 (count (get-in snapshot [:pre-masked-root :preimage-files]))))
    (is (= [13 14 15 16 59]
           (mapv :form-ordinal
                 (get-in snapshot [:pre-masked-root :coordinates]))))
    (is (= :gravity/sh07-b51-v9-ownership-plan-e1
           (get-in snapshot [:ownership-plan :schema])))
    (is (= [:i9 :e9 :g9 :p9]
           (get-in snapshot
                   [:ownership-plan :reservation-required-stages])))
    (is (= [:i9 :g9 :p9]
           (get-in snapshot
                   [:ownership-plan :handoff-required-stages])))
    (is (contains? (set (:v9-tag-registry snapshot))
                   :gravity/sh07-b51-a9-stage-result-e1))
    (is (not (contains?
              (set (get-in snapshot [:identity-domain-registry :domains]))
              :gravity/sh07-b51-a9-stage-result-e1)))
    (is (contains?
         (set (get-in snapshot [:identity-domain-registry :domains]))
         :gravity/sh07-b51-v9-ownership-plan-e1))
    (is (not (contains? (set (:v9-tag-registry snapshot))
                        :gravity/sh07-b51-a9-lifecycle-receipt-v1)))
    (is (= 63 (:static-decode-count snapshot)))
    (is (true? (get-in snapshot [:boot5-compatibility :document-equal])))
    (is (= :none (:authority snapshot)))))

(deftest missing-required-a9-terminal-object-fails-closed
  (let [{:keys [root content]} (initialize-content! false)
        result
        (failure-data
         #(evidence/reconstruct-pre-object-snapshot!
           {:repository-root root :content content}))]
    (is (= "SH07-B51-GIT-SNAPSHOT" (:id result)))
    (is (= :pre-c-path (:reason result)))
    (is (= evidence/a9-terminal-path (:path result)))))

(deftest a9-fixed-bytes-and-authenticated-ledger-row-reject-drift
  (testing "terminal bytes must reproduce the literal descriptor"
    (let [{:keys [root content]} (initialize-content! true)
          _ (write-bytes! root evidence/a9-terminal-path
                          (.getBytes "{}\n" StandardCharsets/UTF_8))
          c (commit! root "mutated terminal")
          t (git! root "show" "-s" "--format=%T" c)
          result
          (failure-data
           #(evidence/reconstruct-pre-object-snapshot!
             {:repository-root root :content {:commit c :tree t}}))]
      (is (not= c (:commit content)))
      (is (= "SH07-B51-GIT-SNAPSHOT" (:id result)))
      (is (= :a9-fixed-descriptor (:reason result)))
      (is (= evidence/a9-terminal-path (:path result)))))
  (testing "the concrete argv path is checked against the C ledger row"
    (let [{:keys [root]} (initialize-content! true)
          ledger-path "contracts/workstream-ledger.json"
          ledger (String. (Files/readAllBytes (.resolve root ledger-path))
                          StandardCharsets/UTF_8)
          _ (write-bytes!
             root ledger-path
             (.getBytes (str/replace ledger
                                     "\"ordinal\":161"
                                     "\"ordinal\":162")
                        StandardCharsets/UTF_8))
          c (commit! root "mutated ledger")
          t (git! root "show" "-s" "--format=%T" c)
          result
          (failure-data
           #(evidence/reconstruct-pre-object-snapshot!
             {:repository-root root :content {:commit c :tree t}}))]
      (is (= "SH07-B51-GIT-SNAPSHOT" (:id result)))
      (is (= :a9-stage-result-checks (:reason result)))
      (is (= [:manifest-entry-match] (:failed-checks result))))))

(deftest q-coordinate-joins-five-operations-to-the-pre-foundation
  (let [{:keys [root content]} (initialize-content! true)
        snapshot (evidence/reconstruct-pre-object-snapshot!
                  {:repository-root root :content content})
        {:keys [receipt coordinate]}
        (commit-eligibility! root content (eligibility-receipt snapshot))
        result (evidence/validate-eligibility-object!
                {:repository-root root :content content
                 :eligibility-coordinate coordinate})]
    (is (= receipt (:receipt result)))
    (is (= 5 (:operation-count result)))
    (is (= 77 (:pre-discharge-git-operation-count result)))
    (is (= [:show-tree :diff-tree-name-status-z :ls-tree-z-requested
            :cat-file-blob :ls-tree-z-requested]
           (mapv :operation (:observations result))))
    (is (= :immutable-q-coordinate-and-pre-discharge (:scope result)))
    (is (= 1 (get-in result [:eligibility-check :decode-count])))
    (is (= 2 (get-in result
                     [:eligibility-check
                      :pre-discharge-pre-masked-file-count])))
    (is (true? (get-in result
                       [:eligibility-check :pre-discharge-match])))
    (is (= (:eligibility-check-root result)
           (codec/H [:gravity/sh07-b51-eligibility-check-root-v1
                     (:eligibility-check result)])))
    (is (= :none (:authority result)))
    (is (= [] (:remaining-gaps result)))))

(deftest skeletal-source-descriptors-and-one-path-q-deltas-reject
  (let [{:keys [root content]} (initialize-content! true)
        snapshot (evidence/reconstruct-pre-object-snapshot!
                  {:repository-root root :content content})
        receipt (eligibility-receipt snapshot)]
    (testing "ordinal-only descriptors never satisfy eligibility"
      (let [bad-base
            (assoc receipt :source-descriptors
                   (mapv (fn [ordinal] {:ordinal ordinal}) (range 12)))
            bad (assoc bad-base :receipt-id
                       (codec/H
                        [:gravity/sh07-b51-evidence-eligibility-receipt-v2
                         (dissoc bad-base :receipt-id)]))
            {:keys [coordinate]} (commit-eligibility! root content bad)
            result
            (failure-data
             #(evidence/validate-eligibility-object!
               {:repository-root root :content content
                :eligibility-coordinate coordinate}))]
        (is (= "SH07-B51-RECEIPT-JOIN" (:id result)))
        (is (= :source-descriptor-keys (:reason result)))))
    (testing "Q must add exactly the eligibility receipt"
      (git! root "checkout" "--detach" (:commit content))
      (let [good (eligibility-receipt snapshot)
            bytes (codec/W good)]
        (write-bytes! root evidence/eligibility-receipt-path bytes)
        (write-bytes! root "foreign.edn" (codec/W {:foreign true}))
        (let [q (commit! root "Q with foreign entry")
              tq (git! root "show" "-s" "--format=%T" q)
              row (git! root "ls-tree" q "--"
                        evidence/eligibility-receipt-path)
              oid (nth (re-matches #"100644 blob ([0-9a-f]{40})\t.+" row) 1)
              coordinate
              {:schema :gravity/sh07-b51-external-review-object-v1
               :commit q :tree tq :parents [(:commit content)]
               :path evidence/eligibility-receipt-path
               :mode "100644" :type "blob" :blob-oid oid
               :byte-count (alength ^bytes bytes)
               :raw-sha256 (codec/R bytes) :receipt-id (:receipt-id good)}
              result
              (failure-data
               #(evidence/validate-eligibility-object!
                 {:repository-root root :content content
                  :eligibility-coordinate coordinate}))]
          (is (= "SH07-B51-RECEIPT-JOIN" (:id result)))
          (is (= :eligibility-object-join (:reason result))))))))

(deftest pre-mask-and-registry-carriers-reject-aliases-or-drift
  (testing "the superseded full-mask key is outside the closed receipt schema"
    (let [{:keys [root content]} (initialize-content! true)
          snapshot (evidence/reconstruct-pre-object-snapshot!
                    {:repository-root root :content content})
          good (eligibility-receipt snapshot)
          bad-base (-> good
                       (dissoc :pre-masked-root)
                       (assoc :masked-root (:pre-masked-root good))
                       (dissoc :receipt-id))
          bad (assoc bad-base :receipt-id
                     (codec/H
                      [:gravity/sh07-b51-evidence-eligibility-receipt-v2
                       bad-base]))
          {:keys [coordinate]} (commit-eligibility! root content bad)
          result
          (failure-data
           #(evidence/validate-eligibility-object!
             {:repository-root root :content content
              :eligibility-coordinate coordinate}))]
      (is (= "SH07-B51-RECEIPT-JOIN" (:id result)))
      (is (= :eligibility-receipt-keys (:reason result)))))
  (testing "an unknown H domain cannot survive full PRE W equality"
    (let [{:keys [root content]} (initialize-content! true)
          snapshot (evidence/reconstruct-pre-object-snapshot!
                    {:repository-root root :content content})
          receipt (eligibility-receipt snapshot)
          registry (get-in receipt
                           [:pre-evidence-contract-discharge :evidence
                            :carriers :identity-domain-registry])
          bad-registry
          (update registry :domains conj :gravity/test-unknown-domain)
          bad (replace-pre-carrier receipt :identity-domain-registry
                                   bad-registry)
          {:keys [coordinate]} (commit-eligibility! root content bad)
          result
          (failure-data
           #(evidence/validate-eligibility-object!
             {:repository-root root :content content
              :eligibility-coordinate coordinate}))]
      (is (= "SH07-B51-RECEIPT-JOIN" (:id result)))
      (is (= :pre-object-carrier-join (:reason result))))))
