(ns seedless.retry-scope-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.main :as main]
            [seedless.workflow-test :as fixture]
            [simplify.runtime :as rt]
            [simplify.workflow :as shared]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _])] (test))))

(defn- snapshot [ctx task]
  (let [worktree (:worktree task)
        paths (shared/changed-paths ctx worktree)
        diff (rt/git ctx worktree "diff" "--binary" "--no-ext-diff" "--no-renames" "HEAD")
        untracked (rt/git ctx worktree "ls-files" "--others" "--exclude-standard" "-z")]
    {:paths paths
     :dirty-diff-sha256 (rt/digest (str diff "\u0000" untracked))
     :file-sha256 (into (sorted-map)
                        (map (fn [path]
                               [path (rt/digest (slurp (io/file worktree path)))])
                             paths))}))

(defn- case-context []
  (let [ctx (fixture/repository! (fixture/context))
        repo (get-in ctx [:config :repo-dir])
        base (rt/git ctx repo "rev-parse" "HEAD")
        tree (rt/git ctx repo "rev-parse" "HEAD^{tree}")
        key "scope-slice"
        error {:message "Worker changed an unowned path"
               :details {:writer-ended? true :path "unowned.edn"}}
        task {:key key :status :attention :resume-status :checking
              :paths ["module.clj"] :description {:key key :paths ["module.clj"]}
              :worktree repo :base base :candidate base :tree tree :error error
              :checks {:candidate base :tree tree :result :passed}
              :review {:base base :candidate base :tree tree :verdict "approved"}
              :head-workflows {:candidate base :state :passed}}]
    (rt/update! ctx assoc-in [:tasks key] task)
    {:ctx ctx :key key :task task :repo repo}))

(defn- inspection [ctx task key]
  (let [snap (snapshot ctx task)
        unowned (vec (remove #{"module.clj"} (:paths snap)))]
    {:evidence "Original owner inspected the ended writer and the exact dirty bytes."
     :scope-extension {:task key
                       :original-paths (:paths task)
                       :base (:base task)
                       :candidate (:candidate task)
                       :tree (:tree task)
                       :worktree (:worktree task)
                       :original-error (:error task)
                       :paths unowned
                       :dirty-diff-sha256 (:dirty-diff-sha256 snap)
                       :file-sha256 (select-keys (:file-sha256 snap) unowned)
                       :authorization {:owner :master-coordinator
                                       :paths unowned
                                       :substantive? true
                                       :evidence "The coordinator's exact inventory inspection authorizes these files only."}}}))

(defn- run-retry! [{:keys [ctx key task]} file]
  (rt/with-lock ctx #(main/retry! ctx key (str file))))

(defn- writers [ctx]
  (let [file (:coordination-file ctx)]
    (if (.isFile (io/file file))
      (:writers (rt/read-edn file))
      {})))

(deftest inspected-scope-extension-preserves-dirty-bytes-and-retires-old-credit
  (let [{:keys [ctx key task repo] :as case} (case-context)
        unowned (io/file repo "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (let [before (slurp unowned)
            record (inspection ctx task key)]
        (rt/write-edn! inspection-file record)
        (run-retry! case inspection-file)
        (let [updated (get-in @(:state ctx) [:tasks key])
              history (last (:scope-inspection-history updated))]
          (is (= :repair (:status updated)))
          (is (= ["module.clj" "unowned.edn"] (:paths updated)))
          (is (= (:paths updated) (get-in updated [:description :paths])))
          (is (nil? (:checks updated)))
          (is (nil? (:review updated)))
          (is (nil? (:head-workflows updated)))
          (is (= before (slurp unowned)))
          (is (= (:error task) (get-in history [:original :error])))
          (is (= (:checks task) (get-in history [:original :checks])))
          (is (= (:review task) (get-in history [:original :review])))
          (is (= (:head-workflows task) (get-in history [:original :head-workflows])))
          (is (= ["unowned.edn"] (:amended-paths history)))
          (is (empty? (writers ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest stale-or-mismatched-scope-is-rejected-without-state-change
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:repo case) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (let [record (inspection ctx task key)
            before @(:state ctx)]
        (spit unowned "{:changed after inspection}\n")
        (rt/write-edn! inspection-file record)
        (is (thrown-with-msg? Exception #"file bytes changed" (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest unsettled-native-effects-and-main-fences-block-scope-amendment
  (let [effect-blocks (map (fn [status]
                             (fn [ctx]
                               (rt/update! ctx assoc-in [:effects (name status)] {:status status})))
                           [:pending :uncertain :restricted])
        main-block (fn [ctx]
                     (rt/coordinate! ctx
                                     (fn [state]
                                       [(assoc-in state [:main-effects "merge/scope"]
                                                  {:state-file (:state-file ctx) :state-dir (:state-dir ctx)})
                                        nil])))]
    (doseq [block (concat effect-blocks [main-block])]
      (let [{:keys [ctx key task] :as case} (case-context)
            unowned (io/file (:repo case) "unowned.edn")
            inspection-file (io/file (:root ctx) "inspection.edn")]
        (try
          (spit unowned "{:observed true}\n")
          (rt/write-edn! inspection-file (inspection ctx task key))
          (block ctx)
          (let [before @(:state ctx)]
            (is (thrown? Exception (run-retry! case inspection-file)))
            (is (= before @(:state ctx)))
            (is (empty? (writers ctx))))
          (finally (fixture/delete-tree! (:root ctx))))))))

(deftest directory-or-unobserved-scope-is-rejected-without-state-change
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:repo case) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (let [record (inspection ctx task key)
            before @(:state ctx)
            invalid (assoc-in record [:scope-extension :paths] ["."])]
        (rt/write-edn! inspection-file invalid)
        (is (thrown-with-msg? Exception #"observed unowned files"
                               (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest busy-amended-paths-fail-closed-and-retain-the-original-owner
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:repo case) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (rt/write-edn! inspection-file (inspection ctx task key))
      (let [foreign (rt/claim-paths! ctx "foreign-owner" ["unowned.edn"] :implementing)
            before @(:state ctx)]
        (try
          (is (thrown-with-msg? Exception #"paths are busy" (run-retry! case inspection-file)))
          (is (= before @(:state ctx)))
          (is (= "foreign-owner" (:key (get-in (rt/read-edn (:coordination-file ctx))
                                                [:writers (:id foreign)]))))
          (finally (rt/release-paths! ctx foreign))))
      (finally (fixture/delete-tree! (:root ctx))))))
