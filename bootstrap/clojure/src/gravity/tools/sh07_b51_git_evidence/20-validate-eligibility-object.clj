
(defn validate-eligibility-object!
  "Validate Q/TQ as the direct sibling-lineage child of C using exactly five
  Git object operations, then independently reconstruct and byte-compare the
  E4 PRE discharge with exactly 209 further Git object operations."
  [{:keys [repository-root content eligibility-coordinate]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (exact-keys! eligibility-coordinate eligibility-coordinate-keys
               "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate-keys)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:eligibility-commit (:commit eligibility-coordinate)]
                         [:eligibility-tree (:tree eligibility-coordinate)]
                         [:eligibility-blob (:blob-oid eligibility-coordinate)]]]
    (require-git-id! value field "SH07-B51-RECEIPT-JOIN"))
  (when-not
   (and (= :gravity/sh07-b51-external-review-object-v1
           (:schema eligibility-coordinate))
        (= [(:commit content)] (:parents eligibility-coordinate))
        (= eligibility-receipt-path (:path eligibility-coordinate))
        (= "100644" (:mode eligibility-coordinate))
        (= "blob" (:type eligibility-coordinate))
        (integer? (:byte-count eligibility-coordinate))
        (not (neg? (:byte-count eligibility-coordinate)))
        (sha256-id? (:raw-sha256 eligibility-coordinate))
        (sha256-id? (:receipt-id eligibility-coordinate)))
    (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate))
  (let [repository
        (canonical-directory! repository-root :repository-root
                              "SH07-B51-GIT-SNAPSHOT")
        c (:commit content)
        q (:commit eligibility-coordinate)
        path (:path eligibility-coordinate)
        show-q (show-operation! repository q)
        diff-cq (diff-name-status-operation! repository c q)
        tree-q (ls-tree-operation! repository q [path])
        blob-q (cat-file-operation! repository
                                     (:blob-oid eligibility-coordinate) true)
        tree-c (ls-tree-operation! repository c [path])
        observations
        (mapv #(assoc %1 :ordinal %2)
              [show-q diff-cq tree-q blob-q tree-c] (range 5))
        receipt (get-in blob-q [:parsed-result :value])
        q-row (first (get-in tree-q [:parsed-result :rows]))
        c-row (first (get-in tree-c [:parsed-result :rows]))]
    (validate-eligibility-receipt! content receipt)
    (when-not
     (and (= {:commit q :tree (:tree eligibility-coordinate)
              :parents [c]}
             (:parsed-result show-q))
          (= {:parent c :commit q
              :entries [{:status :added :path path}]}
             (:parsed-result diff-cq))
          (= {:path path :present true :mode "100644" :type "blob"
              :oid (:blob-oid eligibility-coordinate)}
             q-row)
          (= {:path path :present false :mode nil :type nil :oid nil}
             c-row)
          (= (:byte-count eligibility-coordinate)
             (get-in blob-q [:parsed-result :byte-count]))
          (= (:raw-sha256 eligibility-coordinate)
             (get-in blob-q [:parsed-result :raw-sha256]))
          (= (:receipt-id eligibility-coordinate) (:receipt-id receipt)))
      (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-object-join))
    (let [pre (reconstruct-pre-object-snapshot!
               {:repository-root repository :content content})
          received-discharge (:pre-evidence-contract-discharge receipt)
          pre-carriers
          (get-in receipt
                  [:pre-evidence-contract-discharge :evidence :carriers])
          expected-carriers
          (assoc pre-carriers
                 :content content
                 :document-read-receipt (:document-read-receipt pre)
                 :source-descriptors (:source-descriptors pre)
                 :source-descriptors-root (:source-descriptors-root pre)
                 :pre-masked-root (:pre-masked-root pre)
                 :owned-c-descriptors (:owned-c-descriptors pre)
                 :owned-c-root (:owned-c-root pre)
                 :row-plan row-plan
                 :foundation-static (:foundation-static pre)
                 :boot5-compatibility (:boot5-compatibility pre)
                 :ownership-plan (:ownership-plan pre)
                 :catalog-projection (:catalog-projection pre)
                 :governance-rules (:governance-rules pre)
                 :identity-domain-registry (:identity-domain-registry pre)
                 :publication-boundary (:publication-boundary pre)
                 :proof-scope (:proof-scope pre)
                 :progress (:progress pre)
                 :nonclaims nonclaims
                 :a9-stage-result (:a9-stage-result pre))
          expected-evidence
          (assoc (:evidence received-discharge) :carriers expected-carriers)
          expected-resolved (resolve-contract-phase! :pre-evidence
                                                      expected-carriers)
          expected-without-id
          (-> received-discharge
              (assoc :evidence expected-evidence
                     :resolved-rows (:resolved-rows expected-resolved)
                     :resolved-root (:resolved-root expected-resolved)
                     :authority (pre-evidence-authority))
              (dissoc :discharge-id))
          expected-discharge
          (assoc expected-without-id :discharge-id
                 (codec/H
                  [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
                   expected-without-id]))
          content-match
          (and (= content (:content receipt))
               (= content (:content pre-carriers))
               (= content (get-in pre-carriers [:a9-stage-result :content])))
          source-match (= (:source-descriptors pre)
                          (:source-descriptors receipt)
                          (:source-descriptors pre-carriers))
          source-root-match (= (:source-descriptors-root pre)
                               (:source-descriptors-root receipt)
                               (:source-descriptors-root pre-carriers))
          pre-mask-match (= (:pre-masked-root pre)
                            (:pre-masked-root receipt)
                            (:pre-masked-root pre-carriers))
          owned-root-match (= (:owned-c-root pre) (:owned-c-root pre-carriers))
          pre-discharge-match
          (Arrays/equals ^bytes (codec/W expected-discharge)
                         ^bytes (codec/W received-discharge))
          eligibility-check
          {:coordinate eligibility-coordinate
           :observations observations
           :pre-discharge-observations (:observations pre)
           :decode-count 1
           :receipt-id-check-count 1
           :pre-discharge-reconstruction-count 1
           :pre-discharge-git-operation-count 209
           :pre-discharge-document-descriptor-count 33
           :pre-discharge-source-descriptor-count 13
           :pre-discharge-pre-masked-file-count 2
           :pre-discharge-owned-c-artifact-count 160
           :pre-discharge-a9-artifact-count 3
           :pre-discharge-baseline-boot5-count 1
           :pre-discharge-static-decode-count 196
           :content-match content-match
           :source-descriptors-match source-match
           :source-descriptors-root-match source-root-match
           :pre-masked-root-match pre-mask-match
           :owned-c-root-match owned-root-match
           :pre-discharge-match pre-discharge-match}
          check-root
          (codec/H [:gravity/sh07-b51-eligibility-check-root-v1
                    eligibility-check])]
      (when-not
       (and content-match source-match source-root-match pre-mask-match
            owned-root-match pre-discharge-match
            (= (:document-read-receipt pre)
               (:document-read-receipt pre-carriers))
            (= (:owned-c-descriptors pre)
               (:owned-c-descriptors pre-carriers))
            (= (:boot5-compatibility pre)
               (:boot5-compatibility pre-carriers))
            (= (pre-evidence-authority) (:authority received-discharge)))
        (fail! "SH07-B51-RECEIPT-JOIN" :pre-object-carrier-join))
      {:coordinate eligibility-coordinate
       :receipt receipt
       :observations observations
       :operation-count 5
       :pre-object-snapshot pre
       :pre-discharge-observations (:observations pre)
       :pre-discharge-git-operation-count 209
       :scope :immutable-q-coordinate-and-pre-discharge
       :eligibility-check eligibility-check
       :eligibility-check-root check-root
       :remaining-gaps []
       :authority :none})))

(defn- status-clean-operation!
  [repository]
  (git-operation!
   repository :status-clean
   ["status" "--porcelain=v1" "-z" "--untracked-files=all"] #{0}
   (fn [bytes _]
     {:clean (zero? (alength ^bytes bytes))
      :untracked-count
      (if (zero? (alength ^bytes bytes)) 0
          (count (filter #(or (str/starts-with? % "??")
                              (str/ends-with? % "??"))
                         (nul-records! bytes :status-grammar))))})))

(defn- rev-parse-line!
  [repository arguments]
  (git-operation!
   repository :rev-parse arguments #{0}
   (fn [bytes _] {:value (parse-single-line! bytes :rev-parse-grammar)})))

(defn validate-worktree-coordinate!
  "Validate one clean registered worktree at exact C/T with E9 absent."
  [{:keys [worktree content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (let [root (canonical-directory! worktree :worktree
                                   "SH07-B51-GIT-SNAPSHOT")
        head (rev-parse-line! root ["rev-parse" "--verify" "HEAD^{commit}"])
        common (rev-parse-line!
                root ["rev-parse" "--path-format=absolute" "--git-common-dir"])
        show (show-operation! root (:commit content))
        status (status-clean-operation! root)
        absent (ls-tree-operation! root (:commit content) evidence-paths)
        common-path (Paths/get (get-in common [:parsed-result :value])
                               (make-array String 0))
        coordinate
        {:canonical-path (str root)
         :git-common-directory
         (str (.normalize (.toAbsolutePath common-path)))
         :file-key (file-key root "SH07-B51-GIT-SNAPSHOT")
         :commit (:commit content)
         :tree (:tree content)
         :index-clean (get-in status [:parsed-result :clean])
         :untracked-count (get-in status [:parsed-result :untracked-count])
         :evidence-paths-absent
         (every? (complement :present)
                 (get-in absent [:parsed-result :rows]))}]
    (when-not
     (and (= (:commit content) (get-in head [:parsed-result :value]))
          (= content (select-keys (:parsed-result show) [:commit :tree]))
          (:index-clean coordinate)
          (zero? (:untracked-count coordinate))
          (:evidence-paths-absent coordinate))
      (fail! "SH07-B51-GIT-SNAPSHOT" :worktree-coordinate
             {:coordinate coordinate}))
    coordinate))

(defn validate-publication-worktrees!
  "Validate distinct clean C source and publication worktrees in one object
  database."
  [{:keys [source-worktree publication-worktree content]}]
  (let [source (validate-worktree-coordinate!
                {:worktree source-worktree :content content})
        publication (validate-worktree-coordinate!
                     {:worktree publication-worktree :content content})]
    (when-not
     (and (not= (:canonical-path source) (:canonical-path publication))
          (not= (:file-key source) (:file-key publication))
          (= (:git-common-directory source)
             (:git-common-directory publication)))
      (fail! "SH07-B51-GIT-SNAPSHOT" :publication-worktree-separation))
    {:source-worktree source
     :publication-worktree publication
     :authority :none}))

(def ^:private payload-descriptor-keys
  #{:ordinal :name :kind :relative-path :wire-codec :byte-count :raw-sha256
    :value-id})
