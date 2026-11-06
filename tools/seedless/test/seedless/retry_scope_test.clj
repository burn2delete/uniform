(ns seedless.retry-scope-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is use-fixtures]]
            [seedless.main :as main]
            [seedless.workflow-test :as fixture]
            [simplify.runtime :as rt]
            [simplify.workflow :as shared])
  (:import [java.math BigInteger]
           [java.security MessageDigest]))

(use-fixtures :each (fn [test] (with-redefs [rt/event! (fn [& _])] (test))))

(defn- raw-file-sha256 [file]
  (let [digest (MessageDigest/getInstance "SHA-256")
        buffer (byte-array 8192)]
    (with-open [in (io/input-stream file)]
      (loop []
        (let [read (.read in buffer)]
          (when (pos? read)
            (.update digest buffer 0 read)
            (recur)))))
    (format "%064x" (BigInteger. 1 (.digest digest)))))

(defn- write-bytes! [file bytes]
  (with-open [out (io/output-stream file)]
    (.write out bytes)))

(defn- snapshot [ctx task]
  (let [worktree (:worktree task)
        paths (shared/changed-paths ctx worktree)
        diff (rt/git ctx worktree "diff" "--binary" "--no-ext-diff" "--no-renames" "HEAD")
        untracked (rt/git ctx worktree "ls-files" "--others" "--exclude-standard" "-z")]
    {:paths paths
     :dirty-diff-sha256 (rt/digest (str diff "\u0000" untracked))
     :deleted-paths (vec (sort (filter #(not (.exists (io/file worktree %))) paths)))
     :non-file-paths (vec (sort (filter #(and (.exists (io/file worktree %))
                                               (not (.isFile (io/file worktree %)))) paths)))
     :file-sha256 (into (sorted-map)
                        (map (fn [path]
                               [path (raw-file-sha256 (io/file worktree path))])
                             (filter #(.isFile (io/file worktree %)) paths)))}))

(defn- case-context
  ([] (case-context ["module.clj"]))
  ([paths]
   (let [ctx0 (fixture/repository! (fixture/context))
        repo (get-in ctx0 [:config :repo-dir])
        worktree-root (get-in ctx0 [:config :worktree-root])
        _ (.mkdirs (io/file worktree-root))
        worktree (str (io/file worktree-root "scope-slice"))
        branch "codex/scope-slice"
        _ (rt/git ctx0 repo "worktree" "add" "-b" branch worktree "HEAD")
        identity {:repository "fixture/repo" :repo-dir (rt/canonical repo)}
        ctx (assoc ctx0 :identity identity :owns-lock? (atom false) :command "retry")
        _ (rt/update! ctx assoc :identity identity)
        base (rt/git ctx worktree "rev-parse" "HEAD")
        tree (rt/git ctx worktree "rev-parse" "HEAD^{tree}")
        key "scope-slice"
        error {:message "Worker changed an unowned path"
               :details {:writer-ended? true :path "unowned.edn"}}
        task {:key key :status :attention :resume-status :checking
              :paths paths :description {:key key :paths paths}
              :worktree worktree :branch branch :base base :candidate base :tree tree :error error
              :check-failures [{:job "old-check" :message "retained failure"}]
              :implementation-job {:thread "thread-1" :turn "turn-1"}
              :reporting-error {:message "retained reporting context"}
              :checks {:candidate base :tree tree :result :passed}
              :review {:base base :candidate base :tree tree :verdict "approved"}
              :head-workflows {:candidate base :state :passed}}]
    (rt/update! ctx assoc-in [:tasks key] task)
    {:ctx ctx :key key :task task})))

(defn- inspection [ctx task key]
  (let [snap (snapshot ctx task)
        unowned (vec (remove (fn [path]
                               (some #(shared/covers? % path) (:paths task)))
                             (:paths snap)))]
    {:evidence "Original owner inspected the ended writer and the exact dirty bytes."
     :scope-extension {:task key
                       :original-paths (:paths task)
                       :base (:base task)
                       :candidate (:candidate task)
                       :tree (:tree task)
                       :worktree (:worktree task)
                       :branch (:branch task)
                       :original-error (:error task)
                       :paths unowned
                       :dirty-diff-sha256 (:dirty-diff-sha256 snap)
                       :file-sha256 (:file-sha256 snap)
                       :deleted-paths (:deleted-paths snap)
                       :non-file-paths (:non-file-paths snap)}}))

(defn- run-retry! [{:keys [ctx key]} file]
  (main/with-current-lock ctx #(main/retry! ctx key (str file))))

(defn- writers [ctx]
  (let [file (:coordination-file ctx)]
    (if (.isFile (io/file file))
      (:writers (rt/read-edn file))
      {})))

(deftest inspected-scope-extension-preserves-dirty-bytes-and-retires-old-credit
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
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
          (is (= (:check-failures task) (get-in history [:original :check-failures])))
          (is (= (:resume-status task) (get-in history [:original :resume-status])))
          (is (= (:implementation-job task) (get-in history [:original :implementation-job])))
          (is (= (:reporting-error task) (get-in history [:original :reporting-error])))
          (is (= ["unowned.edn"] (:amended-paths history)))
          (is (empty? (writers ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest directory-owned-children-stay-owned-while-a-separate-file-is-amended
  (let [{:keys [ctx key task] :as case} (case-context ["owned-dir" "module.clj"])
        owned-child (io/file (:worktree task) "owned-dir/child.clj")
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (.mkdirs (.getParentFile owned-child))
      (spit owned-child "{:owned-child true}\n")
      (spit unowned "{:observed true}\n")
      (rt/write-edn! inspection-file (inspection ctx task key))
      (run-retry! case inspection-file)
      (let [updated (get-in @(:state ctx) [:tasks key])
            history (last (:scope-inspection-history updated))]
        (is (= ["owned-dir" "module.clj" "unowned.edn"] (:paths updated)))
        (is (= ["unowned.edn"] (:amended-paths history)))
        (is (contains? (get-in history [:observed :file-sha256]) "owned-dir/child.clj"))
        (is (contains? (get-in history [:observed :file-sha256]) "unowned.edn"))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest directory-owned-child-cannot-be-reclassified-as-unowned
  (let [{:keys [ctx key task] :as case} (case-context ["owned-dir" "module.clj"])
        owned-child (io/file (:worktree task) "owned-dir/child.clj")
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (.mkdirs (.getParentFile owned-child))
      (spit owned-child "{:owned-child true}\n")
      (spit unowned "{:observed true}\n")
      (let [record (inspection ctx task key)
            before @(:state ctx)
            invalid (assoc-in record [:scope-extension :paths]
                              ["owned-dir/child.clj" "unowned.edn"])]
        (rt/write-edn! inspection-file invalid)
        (is (thrown-with-msg? Exception #"already-owned path"
                              (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest owned-deletion-is-explicit-while-new-unowned-file-can-be-amended
  (let [{:keys [ctx key task] :as case} (case-context)
        renamed (io/file (:worktree task) "renamed.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (is (.delete (io/file (:worktree task) "module.clj")))
      (spit renamed "{:renamed true}\n")
      (let [record (inspection ctx task key)]
        (is (= ["module.clj"] (get-in record [:scope-extension :deleted-paths])))
        (is (contains? (get-in record [:scope-extension :file-sha256]) "renamed.edn"))
        (rt/write-edn! inspection-file record)
        (run-retry! case inspection-file)
        (let [updated (get-in @(:state ctx) [:tasks key])
              history (last (:scope-inspection-history updated))]
          (is (= ["module.clj" "renamed.edn"] (:paths updated)))
          (is (= ["module.clj"] (get-in history [:observed :deleted-paths])))
          (is (empty? (writers ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest unowned-deletion-cannot-be-added-to-an-extension
  (let [{:keys [ctx key task] :as case} (case-context ["owned.clj"])
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (is (.delete (io/file (:worktree task) "module.clj")))
      (let [record (inspection ctx task key)
            before @(:state ctx)]
        (rt/write-edn! inspection-file record)
        (is (thrown-with-msg? Exception #"existing files"
                              (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest incomplete-dirty-byte-snapshot-is-rejected
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit (io/file (:worktree task) "module.clj") "(ns fixture)\n(def retained true)\n")
      (spit unowned "{:observed true}\n")
      (let [record (inspection ctx task key)
            before @(:state ctx)
            partial (assoc-in record [:scope-extension :file-sha256]
                              (select-keys (get-in record [:scope-extension :file-sha256])
                                           ["unowned.edn"]))]
        (rt/write-edn! inspection-file partial)
        (is (thrown-with-msg? Exception #"file bytes changed"
                              (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest stale-or-mismatched-scope-is-rejected-without-state-change
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
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

(deftest raw-invalid-utf8-bytes-are-distinct-and-stale-bytes-fail
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (write-bytes! unowned (byte-array [(byte -128)]))
      (let [record (inspection ctx task key)
            before (get-in record [:scope-extension :file-sha256 "unowned.edn"])]
        (write-bytes! unowned (byte-array [(byte -127)]))
        (is (not= before (raw-file-sha256 unowned)))
        (rt/write-edn! inspection-file record)
        (is (thrown-with-msg? Exception #"file bytes changed"
                              (run-retry! case inspection-file))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest forged-scope-authorization-is-rejected
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (let [record (assoc-in (inspection ctx task key) [:scope-extension :authorization]
                             {:owner :master-coordinator
                              :paths ["unowned.edn"]
                              :substantive? true
                              :evidence "forged"})
            before @(:state ctx)]
        (rt/write-edn! inspection-file record)
        (is (thrown-with-msg? Exception #"self-attested coordinator authorization"
                              (run-retry! case inspection-file)))
        (is (= before @(:state ctx)))
        (is (empty? (writers ctx))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest scope-amendment-requires-retry-cli-context
  (let [{:keys [ctx key task] :as original} (case-context)
        ctx (assoc ctx :command "status")
        case (assoc original :ctx ctx)
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (rt/write-edn! inspection-file (inspection ctx task key))
      (is (thrown-with-msg? Exception #"trusted original-owner retry CLI context"
                            (run-retry! case inspection-file)))
      (is (empty? (writers ctx)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest retained-worktree-identity-is-revalidated
  (let [{:keys [ctx key task] :as case} (case-context)
        unowned (io/file (:worktree task) "unowned.edn")
        inspection-file (io/file (:root ctx) "inspection.edn")]
    (try
      (spit unowned "{:observed true}\n")
      (rt/write-edn! inspection-file (inspection ctx task key))
      (rt/update! ctx assoc-in [:tasks key :branch] "codex/other-branch")
      (is (thrown-with-msg? Exception #"Worktree identity differs"
                            (run-retry! case inspection-file)))
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
            unowned (io/file (:worktree task) "unowned.edn")
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
        unowned (io/file (:worktree task) "unowned.edn")
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
        unowned (io/file (:worktree task) "unowned.edn")
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
