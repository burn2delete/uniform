(ns seedless.diagnosis-workflow-test
  "Controller-boundary regressions. Model, compiler and GitHub responses in this
  namespace are deterministic fixtures, never evidence of live model execution."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [gravity.compiler-probe :as probe]
            [seedless.compiler-diagnosis :as diagnosis]
            [seedless.compiler-observation :as compiler]
            [seedless.compiler-observation-test :as observations]
            [seedless.github :as gh]
            [seedless.main :as main]
            [seedless.workflow :as w]
            [seedless.workflow-test :as fixture]
            [simplify.codex :as codex]
            [simplify.runtime :as rt]
            [simplify.workflow :as shared]))

(def ownership-path "docs/self-hosting-slice-ownership.edn")
(def requirements-path "docs/self-hosting-milestone-completion-plan.md")
(def fixture-inputs ["module.clj" "producer.clj" requirements-path])

(use-fixtures :each
  (fn [test]
    (with-redefs [rt/event! (fn [& _]) probe/input-paths fixture-inputs]
      (test))))

(defn context []
  (let [ctx (observations/context)
        cwd (get-in ctx [:config :repo-dir])]
    (io/make-parents (io/file cwd ownership-path))
    (spit (io/file cwd "module.clj")
          "(ns fixture)\n(defn example [receiver] (map-keys receiver))\n")
    (spit (io/file cwd "producer.clj")
          "(ns fixture.checker)\n(defn reject-type [receiver] {:status :rejected :receiver receiver})\n")
    (spit (io/file cwd ownership-path)
          (pr-str {:schema :gravity/self-hosting-slice-ownership-v1
                   :module-owners {"module.clj" :sh-types "producer.clj" :sh-types}
                   :slice-owners {"SH-08" {:leaf-owner :sh-types}}}))
    (spit (io/file cwd requirements-path)
          "# Synthetic transport fixture for existing SH-08 requirements\n\n## SH-08 - Bootstrap types\n\n2. **SH-08.2:** Check function parameters/results, direct/indirect calls, closures, recursive functions and recur parameter compatibility.\n")
    (rt/git ctx cwd "add" "module.clj" "producer.clj" ownership-path requirements-path)
    (rt/git ctx cwd "commit" "-m" "synthetic source-grounded diagnosis fixture")
    ctx))

(defn unknown-observation
  "Synthetic compiler result with real temporary-repository byte identities."
  [ctx id reason]
  (let [cwd (get-in ctx [:config :repo-dir])
        value (-> (observations/observation ctx "unused-unclassified-key")
                  (assoc :inputs (compiler/declared-inputs cwd)
                         :status :blocked
                         :position {:component 0 :stage :function-types}
                         :reason "Unfamiliar fixture rejection needs source investigation"
                         :subject {:source-path "module.clj"
                                   :source-sha256 (compiler/input-sha256 cwd "module.clj")
                                   :core-artifact-id (str "sha256:" (rt/digest (slurp (io/file cwd "module.clj"))))
                                   :core-verification :passed})
                  (assoc-in [:route :closure] [{:component-id "compiler" :path "module.clj"
                                                :source-path "module.clj" :dependencies []}])
                  (assoc :failure {:id id :kind :unclassified :source "module.clj"
                                   :producer ":function-types" :consumer "Current-source canonical Core"
                                   :reason "Unclassified fixture rejection"
                                   :diagnostic {:id id :reason reason
                                                :compiler-result-status :rejected
                                                :stage :type-checking
                                                :source-span {:line-start 2 :line-end 2}}}))
        observation (assoc value :input-id (compiler/input-id value))]
    (assoc observation :execution {:exitCode 1 :stdout (pr-str observation) :stderr ""
                                  :capture-truncated false})))

(defn reference [cwd path first-line last-line]
  {:path path :sha256 (compiler/input-sha256 cwd path)
   :line-start first-line :line-end last-line
   :excerpt (str/join "\n" (subvec (vec (str/split-lines (slurp (io/file cwd path))))
                                  (dec first-line) last-line))})

(def positive-check ["clojure" "-M:test" "positive-fixture"])
(def negative-check ["clojure" "-M:test" "negative-fixture"])

(defn repair-diagnosis [cwd observed]
  {:outcome "repair" :source-revision (:source-revision observed)
   :input-id (:input-id observed)
   :ownership-id (get-in (diagnosis/context cwd observed {} []) [:ownership :id])
   :summary "Inspect the receiver constraint at its owning type checker"
   :cause "The source call reaches the producer rejection before a typed product is available."
   :producer-refs [(reference cwd "producer.clj" 2 2)]
   :trigger-refs [(reference cwd "module.clj" 2 2)]
   :owners [{:role "repair" :milestone "SH-08" :task "SH-08.2"
             :paths ["producer.clj"]
             :requirement (reference cwd requirements-path 5 5)}]
   :checks [{:kind "positive" :command positive-check
             :establishes "Required receiver type is accepted through the same producing checker."}
            {:kind "negative" :command negative-check
             :establishes "Malformed receiver domains retain the original rejection semantics."}]
   :preserve "Preserve complete source population, receiver legality, prior checks and stable diagnostics."
   :candidates [] :missing-evidence []})

(defn blocked-diagnosis [cwd observed]
  {:outcome "blocked" :source-revision (:source-revision observed)
   :input-id (:input-id observed)
   :ownership-id (get-in (diagnosis/context cwd observed {} []) [:ownership :id])
   :summary "The terminal capture does not retain the inferred receiver type."
   :cause "A diagnostic string does not determine the producer's inferred receiver domain."
   :producer-refs [] :trigger-refs [] :owners [] :checks []
   :preserve "Map-key receiver and malformed-input checks remain required."
   :candidates []
   :missing-evidence ["Capture the actual inferred receiver type and the producing constraint before authorizing a repair."]})

(defn repair-plan [observed]
  (observations/plan
   [(assoc (fixture/slice "untrusted-model-task-name")
           :kind "implementation" :stage "SH-08" :compiler-task "SH-08.2"
           :compiler-role "repair" :compiler-failure (get-in observed [:failure :work-key])
           :paths ["producer.clj"] :seed-paths ["producer.clj"] :uniform-paths []
           :proposal "Repair the source-traced checker without changing its malformed-input contract."
           :positive "Remove the selected obstruction on the complete current compiler route."
           :negative "Reject malformed receiver types through the owning checker.")]))

(defn with-controller [ctx observed response operation]
  (let [cwd (get-in ctx [:config :repo-dir]) base (rt/git ctx cwd "rev-parse" "HEAD")]
    (with-redefs [w/fresh-main! (constantly base)
                  w/worktree! (fn [& _] {:worktree cwd :branch "main"})
                  gh/pages (fn [& _] [])
                  gh/issue! (fn [& _] {:number 1 :id 1})
                  gh/milestone! (fn [& _] {:number 8})
                  gh/relation! (fn [& _]) gh/status! (fn [& _]) gh/comment! (fn [& _])
                  w/campaign-issue! (fn [& _] {:number 100 :id 100})
                  w/job! (fn [_ role worktree prompt schema]
                           (response role worktree prompt schema))
                  codex/command! (fn [_ _ _ argv]
                                   (if (= compiler/command argv)
                                     (observations/execute-result! (observed))
                                     {:exitCode 0}))]
      (operation))))

(deftest unfamiliar-results-reach-read-only-diagnosis-and-normal-admission
  (doseq [[id reason] [["FUTURE-TYPE-A" :receiver-constraint-unavailable]
                       ["FUTURE-TYPE-B" :function-result-incoherent]]]
    (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
          raw (unknown-observation ctx id reason) roles (atom [])
          proposal (repair-diagnosis cwd raw)]
      (try
        (is (diagnosis/investigable? cwd raw compiler/command))
        (with-controller ctx (constantly raw)
          (fn [role _ prompt _]
            (swap! roles conj role)
            (case role
              :diagnose (do (is (str/includes? prompt id))
                            (is (str/includes? prompt (:input-id raw))) proposal)
              :plan (repair-plan (diagnosis/validate! cwd compiler/command raw proposal {} []))))
          #(let [selected (first (:slices (w/plan! ctx)))
                 task (w/task ctx (:key selected))]
             (is (= [:diagnose :plan] @roles))
             (is (= "SH-08.2" (:compiler-task selected)))
             (is (= ["producer.clj"] (:paths task)))
             (is (= :ready (:status task)))
             (is (= (:source-revision raw) (get-in task [:compiler-observation :source-revision])))
             (is (= (:input-id raw) (get-in task [:compiler-observation :input-id])))
             (is (w/eligible? ctx task []))
             (is (not (w/eligible? ctx task [{:status :implementing :paths ["producer.clj"]}])))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest unfamiliar-partial-admission-resumes-its-original-diagnosis-and-selection
  (doseq [change [:unchanged :ownership :duplicate :overlap :head-change]]
    (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
          raw (unknown-observation ctx "FUTURE-ADMISSION" :receiver-constraint-unavailable)
          original-pr (when (= :head-change change)
                        {:number 77 :title "Independent source scenario" :draft true
                         :body (gh/marker "different-compiler-scope") :paths ["producer.clj"]
                         :head {:ref "codex/independent-scenario" :sha (apply str (repeat 40 "a"))}
                         :base {:ref "main" :sha (:source-revision raw)}})
          disposition {:candidate "pr/77" :disposition "independent-successor"
                       :reason "The inspected PR changes a distinct caller scenario while this selection repairs its producer."}
          proposal (cond-> (repair-diagnosis cwd raw)
                     original-pr (assoc :candidates [disposition]))
          diagnosed (diagnosis/validate! cwd compiler/command raw proposal {} (if original-pr [original-pr] []))
          roles (atom []) issues (atom 0) relations (atom 0) snapshots (atom 0)
          prs (atom (if original-pr [original-pr] []))]
      (try
        (with-controller ctx (constantly raw)
          (fn [role _ _ _]
            (swap! roles conj role)
            (case role :diagnose proposal
                  :plan (cond-> (repair-plan diagnosed)
                          original-pr (assoc :candidate-dispositions [disposition]))))
          #(with-redefs [gh/pages (fn [_ endpoint]
                                   (if (str/ends-with? endpoint "pulls?state=open")
                                     (do (swap! snapshots inc) @prs)
                                     [{:filename "producer.clj"}]))
                         gh/issue! (fn [& _] (let [id (swap! issues inc)] {:number id :id id}))
                         gh/relation! (fn [& _]
                                        (when (= 1 (swap! relations inc))
                                          (throw (ex-info "Fixture relationship read interrupted" {}))))]
             (is (thrown-with-msg? Exception #"Fixture relationship read interrupted" (w/plan! ctx)))
             (let [pending (:planning (rt/read-edn (:state-file ctx)))
                   selected (get-in pending [:selection :slices])
                   key (:key (first selected))
                   original (w/task ctx key)]
               (is (= [:diagnose :plan] @roles))
               (is (= 1 @issues))
               (is (:admission-pending original))
               (is (not (w/eligible? ctx original [])))
               (reset! (:state ctx) (rt/read-edn (:state-file ctx)))
               (case change
                 :unchanged
                 (do
                   (is (= selected (:slices (w/plan! ctx))))
                   (is (= [:diagnose :plan] @roles))
                   (is (not (:admission-pending (w/task ctx key))))
                   (is (w/eligible? ctx (w/task ctx key) []))
                   (is (nil? (:planning @(:state ctx)))))
                 :ownership
                 (do
                   (spit (io/file cwd ownership-path)
                         (pr-str (assoc (rt/read-edn (io/file cwd ownership-path)) :module-owners {})))
                   (rt/git ctx cwd "add" ownership-path)
                   (rt/git ctx cwd "commit" "-m" "invalidate synthetic diagnosis ownership")
                   (is (compiler/current? cwd raw compiler/command))
                   (is (not (diagnosis/current-diagnosis? cwd (:compiler-observation pending))))
                   (is (thrown-with-msg? Exception #"changed before admission resumed" (w/plan! ctx)))
                   (is (= [:diagnose :plan :diagnose] @roles))
                   (is (:admission-pending (w/task ctx key)))
                   (is (not (w/eligible? ctx (w/task ctx key) []))))
                 (:duplicate :overlap)
                 (do
                   (reset! prs [{:number 77 :title "Newly visible compiler candidate" :draft true
                                 :body (gh/marker (if (= :duplicate change) key "different-compiler-scope"))}])
                   (is (thrown-with-msg? Exception
                                         (if (= :duplicate change) #"existing candidate|duplicates a retained candidate"
                                             #"explicit independent-successor")
                                         (w/plan! ctx)))
                   (is (= [:diagnose :plan] @roles))
                   (is (= 1 @relations))
                   (is (:admission-pending (w/task ctx key)))
                   (is (not (w/eligible? ctx (w/task ctx key) [])))
                   (is (= 77 (get-in @(:state ctx) [:planning :open-prs 0 :number]))))
                 :head-change
                 (do
                   (swap! prs assoc-in [0 :head :sha] (apply str (repeat 40 "b")))
                   (is (thrown-with-msg? Exception #"(?i)(candidate.*changed|changed.*candidate|replan)" (w/plan! ctx)))
                   (is (= [:diagnose :plan] @roles))
                   (is (= 1 @relations))
                   (is (:admission-pending (w/task ctx key)))
                   (is (not (w/eligible? ctx (w/task ctx key) [])))
                   (is (= (:candidate-snapshot pending) (get-in @(:state ctx) [:planning :candidate-snapshot])))))
               (is (= 2 @snapshots))
               (is (= 1 @issues))
               (is (= [key] (mapv :key (w/tasks ctx))))
               (is (= (:issue original) (:issue (w/task ctx key))))
               (is (= (:compiler-observation original) (:compiler-observation (w/task ctx key)))))))
        (finally (fixture/delete-tree! (:root ctx)))))))

(deftest investigator-uses-the-existing-read-only-worker-policy
  (let [ctx (context) modes (atom [])]
    (try
      (with-redefs [codex/job! (fn [_ _ _ mode _ _ output-schema]
                                 (swap! modes conj mode)
                                 (is (= diagnosis/schema output-schema))
                                 {})]
        (w/job! ctx :diagnose (get-in ctx [:config :repo-dir])
                "Read-only synthetic investigation" diagnosis/schema))
      (is (= [:read] @modes))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest invalid-investigator-ownership-stops-before-planning-and-admission
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        raw (unknown-observation ctx "FUTURE-UNKNOWN" :receiver-constraint-unavailable)
        invalid (assoc-in (repair-diagnosis cwd raw) [:owners 0 :milestone] "SH-19")
        roles (atom [])]
    (try
      (with-controller ctx (constantly raw)
        (fn [role _ _ _]
          (swap! roles conj role)
          (is (= :diagnose role))
          invalid)
        #(let [result (w/plan! ctx)]
           (is (= [:diagnose] @roles))
           (is (empty? (:slices result)))
           (is (seq (:blockers result)))
           (is (empty? (w/tasks ctx)))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest an-unchanged-unresolved-investigation-is-retained-without-model-spin
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        raw (unknown-observation ctx "FUTURE-UNKNOWN" :missing-inferred-type)
        roles (atom []) inspection (io/file (:root ctx) "source-inspection.edn")]
    (try
      (with-controller ctx (constantly raw)
        (fn [role _ _ _]
          (swap! roles conj role)
          (is (= :diagnose role))
          (blocked-diagnosis cwd raw))
        #(do (dotimes [_ 2]
               (let [result (w/plan! ctx)]
                 (is (empty? (:slices result)))
                 (is (seq (:blockers result)))))
             (is (= [:diagnose] @roles))
             (is (empty? (w/tasks ctx)))
             (let [blocker (:diagnosis-blocker @(:state ctx))
                   effect (get-in @(:state ctx) [:effects (:effect blocker)])]
               (is (= :unresolved (:status blocker)))
               (is (= :done (:status effect)))
               (rt/write-edn! inspection
                              {:evidence "Inspected newly available receiver constraint traces for the unchanged source."})
               (main/retry! ctx "audit" (str inspection))
               (is (= blocker (get-in @(:state ctx) [:inspections 0 :diagnosis-blocker])))
               (dotimes [_ 2]
                 (is (empty? (:slices (w/plan! ctx)))))
               (is (= [:diagnose :diagnose] @roles))
               (is (= effect (get-in @(:state ctx) [:effects (:effect blocker)])))
               (is (empty? (w/tasks ctx))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest source-diagnosis-does-not-authorize-unowned-or-stale-implementation
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        raw (unknown-observation ctx "FUTURE-UNKNOWN" :receiver-constraint-unavailable)
        valid (repair-diagnosis cwd raw)]
    (try
      (doseq [invalid [(assoc valid :input-id "different-inputs")
                       (assoc valid :source-revision "different-revision")
                       (assoc-in valid [:producer-refs 0 :path] "../outside.clj")
                       (assoc-in valid [:producer-refs 0 :sha256] (apply str (repeat 64 "0")))
                       (assoc-in valid [:producer-refs 0 :excerpt] "This is not the referenced source")
                       (assoc valid :producer-refs [])
                       (assoc valid :trigger-refs [])
                       (assoc-in valid [:trigger-refs 0] (reference cwd "producer.clj" 2 2))
                       (assoc valid :ownership-id "stale-ownership")
                       (assoc-in valid [:owners 0 :task] "SH-08.999")
                       (assoc-in valid [:owners 0 :milestone] "SH-19")
                       (assoc-in valid [:owners 0 :paths] ["unowned.clj"])
                       (assoc valid :checks [(first (:checks valid))])]]
        (is (thrown? Exception (diagnosis/validate! cwd compiler/command raw invalid {} []))))
      (let [observed (diagnosis/validate! cwd compiler/command raw valid {} [])]
        (is (diagnosis/current-diagnosis? cwd observed))
        (spit (io/file cwd ownership-path) (pr-str {:schema :gravity/self-hosting-slice-ownership-v1
                                                  :module-owners {} :slice-owners {}}))
        (is (compiler/current? cwd raw compiler/command))
        (is (not (diagnosis/current-diagnosis? cwd observed)))
        (is (thrown? Exception (diagnosis/validate! cwd compiler/command raw valid {} []))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest execution-and-evidence-blockers-cannot-become-repair-authority
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        raw (unknown-observation ctx "FUTURE-UNKNOWN" :receiver-constraint-unavailable)]
    (try
      (doseq [blocked [(assoc raw :restricted true)
                       (assoc-in raw [:terminal :status] :timeout)
                       (assoc-in raw [:terminal :status] :terminated)
                       (assoc-in raw [:terminal :exit] 137)
                       (assoc-in raw [:execution :capture-truncated] true)
                       (dissoc raw :input-id)
                       (assoc raw :inputs [])
                       (assoc-in raw [:failure :kind] :environment)
                       (assoc-in raw [:failure :id] "P18T04007")
                       (assoc-in raw [:failure :diagnostic :compiler-result-status] :unavailable)
                       (assoc-in raw [:subject :source-sha256] (apply str (repeat 64 "0")))
                       (assoc-in raw [:failure :source] "unrelated.clj")]]
        (is (not (diagnosis/investigable? cwd blocked compiler/command))))
      (with-controller ctx (constantly (assoc-in raw [:terminal :status] :timeout))
        (fn [& _] (is false "Execution blockers must never invoke an investigator or planner"))
        #(is (empty? (:slices (w/plan! ctx)))))
      (spit (io/file cwd "module.clj") "changed source without fresh observation\n")
      (is (not (diagnosis/investigable? cwd raw compiler/command)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest validated-diagnosis-preserves-existing-candidate-identity
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        raw (unknown-observation ctx "FUTURE-UNKNOWN" :receiver-constraint-unavailable)
        proposal (repair-diagnosis cwd raw)
        interpreted (diagnosis/validate! cwd compiler/command raw proposal {} [])
        plan (repair-plan interpreted)
        selected (first (:slices (compiler/bind-selection! cwd compiler/command interpreted plan {} [])))
        candidate (assoc selected :status :acceptance-pending :candidate "retained-immutable-candidate"
                                  :pr {:number 42} :description selected)
        known {(:key candidate) candidate}]
    (try
      (rt/update! ctx assoc :tasks known)
      (is (thrown? Exception (compiler/bind-selection! cwd compiler/command interpreted plan known [])))
      (is (= candidate (w/task ctx (:key candidate))))
      (is (not (w/eligible? ctx candidate [])))
      (let [reuse (assoc (blocked-diagnosis cwd raw) :outcome "reuse"
                        :candidates [{:candidate (str "slice/" (:key candidate))
                                      :disposition "reuse" :reason "Exact repair already has an immutable candidate."}])]
        (diagnosis/validate! cwd compiler/command raw reuse known [])
        (is (thrown? Exception
                     (diagnosis/validate! cwd compiler/command raw
                                          (assoc-in reuse [:candidates 0 :candidate] "slice/unknown") known []))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest intact-retained-map-key-capture-enters-the-production-investigation-boundary
  ;; The complete capture and all provenance fields are preserved verbatim.
  ;; Source/hash and command doubles make its recorded inputs applicable to this
  ;; regression; they do not recreate a live compiler execution or receiver type.
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])
        retained (edn/read-string (slurp (io/resource "fixtures/compiler-envelope-map-keys.edn")))
        raw (:observation retained)
        hashes (assoc (into {} (map (juxt :path :sha256) (:inputs raw)))
                      (get-in raw [:subject :source-path]) (get-in raw [:subject :source-sha256]))
        original-hash compiler/input-sha256
        calls (atom [])]
    (try
      (is (= "d5775869ceb05bb28d0fc247ee42c00fa0179bd0" (:source-revision raw)))
      (is (= "a05ff121fc7be2837164781eaa42292233a85f0c7ec36990e904791d5fd0b922" (:input-id raw)))
      (is (= (:input-id raw) (compiler/input-id raw)))
      (is (= 14 (count (:inputs raw))))
      (is (= :map-keys-finite-keyword-map-type-required (get-in raw [:failure :diagnostic :reason])))
      (with-redefs [probe/input-paths (mapv :path (:inputs raw))
                    compiler/input-sha256 (fn [root path]
                                             (or (get hashes path) (original-hash root path)))]
        (with-controller ctx (constantly raw)
          (fn [role _ prompt _]
            (swap! calls conj role)
            (is (= :diagnose role))
            (is (str/includes? prompt (:source-revision raw)))
            (is (str/includes? prompt (get-in raw [:subject :core-artifact-id])))
            (blocked-diagnosis cwd raw))
          #(let [result (w/plan! ctx)
                 observed (:compiler-observation @(:state ctx))]
             (is (= [:diagnose] @calls))
             (is (empty? (:slices result)))
             (is (some (fn [blocker] (str/includes? blocker "inferred receiver")) (:blockers result)))
             (is (= (:source-revision raw) (:source-revision observed)))
             (is (= (:inputs raw) (:inputs observed)))
             (is (= (:subject raw) (:subject observed)))
             (is (= (:failure raw) (:failure observed)))
             (is (empty? (w/tasks ctx))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(defn candidate! [ctx key before]
  (let [cwd (get-in ctx [:config :repo-dir])
        proposal (repair-diagnosis cwd before)
        interpreted (diagnosis/validate! cwd compiler/command before proposal {} [])
        selected (first (:slices (compiler/bind-selection! cwd compiler/command interpreted
                                                          (repair-plan interpreted) {} [])))
        wt (w/worktree! ctx key (:source-revision before))
        task (merge selected wt {:base (:source-revision before) :description selected
                                 :issue {:number 1 :id 1} :pr {:number 42 :html_url "fixture-pr-42"}})]
    (fixture/add-task! ctx task)
    task))

(defn revise-candidate! [ctx task source]
  (let [cwd (:worktree task)]
    (spit (io/file cwd "producer.clj") source)
    (rt/git ctx cwd "add" "producer.clj")
    (rt/git ctx cwd "commit" "-m" "synthetic candidate source advance")
    (w/task! ctx (:key task) :candidate (rt/git ctx cwd "rev-parse" "HEAD")
             :tree (rt/git ctx cwd "rev-parse" "HEAD^{tree}"))
    (w/task ctx (:key task))))

(defn progress-proposal [task outcome before-ref]
  {:outcome outcome :base (:base task) :candidate (:candidate task) :tree (:tree task)
   :before-input-id (get-in task [:compiler-observation :input-id])
   :after-input-id (get-in task [:compiler-result :input-id])
   :summary (if (= "established" outcome)
              "The exact producer obstruction is removed and its malformed-input semantics remain checked."
              "Changed diagnostic text alone does not establish removal of the original obstruction.")
   :original-obstruction
   {:removed (= "established" outcome)
    :evidence "The source-specific positive check exercises the repaired receiver constraint at its actual producer."
    :before-refs [before-ref]
    :after-refs [(reference (:worktree task) "producer.clj" 2 2)]}
   :preceding-behavior {:preserved (= "established" outcome)
                       :evidence "The full declared route retains its completed prefix; negative semantic checks preserve malformed receiver rejection."}
   :checks [{:kind "positive" :command positive-check :establishes "The selected source-specific obstruction is removed."}
            {:kind "negative" :command negative-check :establishes "Malformed receiver domains remain rejected."}]
   :missing-evidence (if (= "pending" outcome) ["Independent source-specific failure-removal evidence is still required."] [])})

(deftest unfamiliar-after-result-is-investigated-while-the-candidate-remains-unaccepted
  (let [ctx (context) before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        baseline-ref (reference (get-in ctx [:config :repo-dir]) "producer.clj" 2 2)
        task (candidate! ctx "pending-after" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :pending :receiver receiver})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-AFTER" :different-obstruction)
        roles (atom [])]
    (try
      (with-controller ctx (constantly after)
        (fn [role cwd _ schema]
          (swap! roles conj [role schema])
          (cond
            (= :diagnose role) (blocked-diagnosis cwd after)
            (= schema diagnosis/progress-schema) (progress-proposal (w/task ctx (:key task)) "pending" baseline-ref)
            :else (throw (ex-info "Ordinary acceptance cannot precede progress evidence" {:role role}))))
        #(with-redefs [rt/lease-phase! (fn [& _])]
           (w/check-candidate! ctx (:key task) [positive-check negative-check] nil)
           (let [pending (w/task ctx (:key task))]
             (is (= :acceptance-pending (:status pending)))
             (is (= :passed (get-in pending [:checks :result])))
             (is (= (:candidate task) (:candidate pending)))
             (is (= (:tree task) (:tree pending)))
             (is (= (:source-revision before) (get-in pending [:compiler-observation :source-revision])))
             (is (= (:input-id after) (get-in pending [:compiler-result :input-id])))
             (is (not (compiler/advanced? (:compiler-observation pending) (:compiler-result pending))))
             (is (not (w/eligible? ctx pending [])))
             (is (= :acceptance-pending (w/accept! ctx (:key task) [positive-check negative-check])))
             (is (= :acceptance-pending (:status (w/task ctx (:key task)))))
             (is (nil? (:review (w/task ctx (:key task)))))
             (is (= [[:diagnose diagnosis/schema] [:review diagnosis/progress-schema]] @roles))
             (is (= :acceptance-pending (w/accept! ctx (:key task) [positive-check negative-check])))
             (is (= 2 (count @roles)))
             (let [retained @(:state ctx)
                   intent (:compiler-progress-intent (w/task ctx (:key task)))]
               (doseq [status [:pending :uncertain :restricted]]
                 (rt/update! ctx assoc-in [:effects intent :status] status)
                 (w/task! ctx (:key task) :compiler-progress-inspection "changed-fixture-evidence")
                 (is (thrown-with-msg? Exception #"needs reconciliation" (w/assess-compiler-progress! ctx (:key task))))
                 (is (= status (get-in @(:state ctx) [:effects intent :status])))
                 (is (= intent (:compiler-progress-intent (w/task ctx (:key task)))))
                 (is (= 2 (count @roles))))
               (rt/update! ctx (constantly retained)))
             (let [inspection (io/file (:root ctx) "pending-inspection.edn")]
               (rt/write-edn! inspection
                              {:evidence "Inspected new producer constraint evidence for the retained candidate."})
               (main/retry! ctx (:key task) (str inspection))
               (is (true? (:compiler-progress-resume (w/task ctx (:key task)))))
               (is (= :acceptance-pending (w/continue-pending! ctx (:key task))))
               (is (= [[:diagnose diagnosis/schema] [:review diagnosis/progress-schema]
                       [:review diagnosis/progress-schema]] @roles))
               (w/continue-pending! ctx (:key task))
               (is (= 3 (count @roles)))
               (is (= (:candidate task) (:candidate (w/task ctx (:key task)))))
               (is (nil? (:review (w/task ctx (:key task))))))
             (is (= "" (rt/git ctx (:worktree task) "status" "--porcelain"))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest same-stage-progress-requires-source-evidence-and-exact-independent-review
  ;; The source edits and all model/compiler results here are synthetic. The
  ;; real checking/acceptance gates run against isolated Git commits and receipts.
  (let [ctx (context) before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        baseline-ref (reference (get-in ctx [:config :repo-dir]) "producer.clj" 2 2)
        task (candidate! ctx "proved-after" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :checked :receiver receiver})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-NEXT" :next-source-obstruction)
        reviews (atom [])]
    (try
      (with-controller ctx (constantly after)
        (fn [role cwd _ schema]
          (cond
            (= :diagnose role) (blocked-diagnosis cwd after)
            (= schema diagnosis/progress-schema)
            (do (swap! reviews conj :source-progress)
                (progress-proposal (w/task ctx (:key task)) "established" baseline-ref))
            :else
            (do (swap! reviews conj :independent-acceptance)
                (merge (select-keys (w/task ctx (:key task)) [:base :candidate :tree])
                       {:verdict "approved" :evidence-scope "implementation"
                        :summary "Independent exact candidate review" :findings []
                        :residual-host-boundary "Fixture bootstrap host remains."}))))
        #(with-redefs [rt/lease-phase! (fn [& _])
                       shared/pr-info (fn [_ t] {:state "OPEN" :headRefOid (:candidate t) :isDraft false})
                       gh/api (fn [_ _]
                                {:number 42 :state "open"
                                 :body (str "<!-- uniform-seedless:" (:key task) " -->\nImplements #1.")
                                 :head {:ref (:branch task) :sha (:candidate (w/task ctx (:key task)))
                                        :repo {:full_name "fixture/repo"}}
                                 :base {:ref "main" :repo {:full_name "fixture/repo"}}})]
           (w/check-candidate! ctx (:key task) [positive-check negative-check] nil)
           (is (= :acceptance-pending (:status (w/task ctx (:key task)))))
           (let [checked (w/task ctx (:key task))
                 proof (progress-proposal checked "established" baseline-ref)]
             (doseq [invalid [(assoc proof :candidate "different-candidate")
                              (assoc proof :before-input-id "different-inputs")
                              (assoc-in proof [:original-obstruction :removed] false)
                              (assoc-in proof [:original-obstruction :before-refs 0 :excerpt] "invented source")
                              (assoc-in proof [:preceding-behavior :preserved] false)
                              (assoc-in proof [:checks 0 :command] ["clojure" "-M:test" "not-executed"])
                              (assoc proof :checks [(first (:checks proof))])]]
               (is (thrown? Exception (diagnosis/validate-progress! ctx checked invalid))))
             (is (diagnosis/validate-progress! ctx checked proof)))
           (is (true? (w/accept! ctx (:key task) [positive-check negative-check])))
           (let [accepted (w/task ctx (:key task))]
             (is (= :ci (:status accepted)))
             (is (= [:source-progress :independent-acceptance] @reviews))
             (is (= (select-keys accepted [:base :candidate :tree])
                    (select-keys (:review accepted) [:base :candidate :tree]))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest campaign-investigates-an-unaccepted-frontier-once-and-retains-its-blocker
  (let [ctx (context) before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        baseline-ref (reference (get-in ctx [:config :repo-dir]) "producer.clj" 2 2)
        task (candidate! ctx "campaign-pending" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :pending :receiver receiver})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-AFTER" :receiver-type-not-captured)
        real-worktree w/worktree! roles (atom [])]
    (try
      (w/task! ctx (:key task) :status :acceptance-pending)
      ;; This frontier already has checked source and an unresolved independent
      ;; assessment. Campaign should continue its source investigation once.
      (let [diagnosed (diagnosis/validate! (:worktree task) compiler/command after
                                          (blocked-diagnosis (:worktree task) after)
                                          (:tasks @(:state ctx)) [])]
        (w/task! ctx (:key task) :compiler-result diagnosed
                 :compiler-progress (progress-proposal (assoc task :compiler-result diagnosed) "pending" baseline-ref)
                 :checks {:candidate (:candidate task) :tree (:tree task) :result :passed
                          :commands (vec (concat (get-in ctx [:config :checks]) [positive-check negative-check]))}))
      (with-controller ctx (constantly after)
        (fn [role cwd _ _]
          (swap! roles conj role)
          (is (= :diagnose role))
          (blocked-diagnosis cwd after))
        #(with-redefs [w/worktree! real-worktree gh/setup! (fn [& _])]
           (w/campaign! ctx)
           (let [pending (w/task ctx (:key task)) round (:round @(:state ctx))]
             (is (= [:diagnose] @roles))
             (is (= :acceptance-pending (:status pending)))
             (is (= (:candidate task) (:candidate pending)))
             (is (= (:candidate task) (get-in @(:state ctx) [:last-plan :base])))
             (is (:compiler-investigation-planned pending))
             (is (seq (get-in @(:state ctx) [:last-plan :blockers])))
             (w/campaign! ctx)
             (is (= round (:round @(:state ctx))))
             (is (= [:diagnose] @roles))
             (is (= :acceptance-pending (:status (w/task ctx (:key task))))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest campaign-keeps-pending-acceptance-with-its-running-worker
  (let [ctx (assoc (fixture/context) :config {:parallelism 2 :poll-ms 1})
        held "held-review" independent "independent-source"
        started (promise) release-worker (promise) worker-finished (promise)
        independent-finished (promise) held-poll-observed (promise)
        exposed (atom false) held-polls (atom 0) review-owner (atom nil)
        continued (atom []) independent-observations (atom [])]
    (try
      (fixture/add-task! ctx (assoc (fixture/slice held) :paths ["held.clj"]))
      (fixture/add-task! ctx (assoc (fixture/slice independent) :paths ["independent.clj"]))
      (w/task! ctx independent :status :not-planned)
      (with-redefs [gh/setup! (fn [& _]) gh/status! (fn [& _]) gh/comment! (fn [& _])
                    w/campaign-issue! (fn [& _] {}) w/fresh-main! (constantly "fixture-base")
                    w/paused? (fn [_]
                                (when (and (realized? started) (compare-and-set! exposed false true))
                                  (w/task! ctx independent :status :ready))
                                (when (and (realized? independent-finished) (not (realized? release-worker))
                                           (= 2 (swap! held-polls inc)))
                                  (deliver held-poll-observed true))
                                false)
                    w/prepare! (fn [_ key lease]
                                 (if (= key held)
                                   (do
                                     (reset! review-owner :worker)
                                     ;; The real worker releases its write lease before
                                     ;; read-only acceptance, while its Future stays live.
                                     (rt/with-claimed-paths ctx lease
                                       #(w/task! ctx key :status :acceptance-pending :candidate "immutable-head"))
                                     (deliver started true)
                                     (when (= ::timeout (deref release-worker 10000 ::timeout))
                                       (throw (ex-info "Scheduler fixture worker release timed out" {})))
                                     (reset! review-owner nil)
                                     (deliver worker-finished true)
                                     :acceptance-pending)
                                   (rt/with-claimed-paths ctx lease
                                     #(do
                                        (swap! independent-observations conj
                                               {:key key :during-review @review-owner})
                                        (w/task! ctx key :status :not-planned)
                                        (deliver independent-finished true)))))
                    w/continue-pending! (fn [_ key]
                                          (swap! continued conj {:key key :existing-review @review-owner
                                                                 :worker-finished (realized? worker-finished)})
                                          (when (realized? worker-finished)
                                            (w/task! ctx key :status :not-planned)))
                    w/plan! (fn [_]
                              (let [plan {:slices [] :blockers ["Synthetic scheduler exercise is complete"]}]
                                (rt/update! ctx assoc :last-plan plan
                                            :last-discovery-key (w/discovery-key ctx "fixture-base"))
                                plan))]
        (let [campaign (future (w/campaign! ctx))]
          (try
            (is (= true (deref held-poll-observed 10000 ::timeout)))
            (is (= :worker @review-owner))
            (is (not (realized? worker-finished)))
            (is (empty? @continued))
            (is (= [{:key independent :during-review :worker}] @independent-observations))
            (deliver release-worker true)
            (is (not= ::timeout (deref campaign 10000 ::timeout)))
            (is (= [{:key held :existing-review nil :worker-finished true}] @continued))
            (is (= :attention (:status @(:state ctx))))
            (finally
              (deliver release-worker true)
              (when-not (realized? campaign) (future-cancel campaign))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest pending-source-candidate-can-admit-a-scoped-successor-without-accepting-its-parent
  (let [ctx (context) before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        task (candidate! ctx "stack-pending" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :pending :receiver receiver})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-AFTER" :next-source-obstruction)
        real-worktree w/worktree! roles (atom [])
        disposition {:candidate (str "slice/" (:key task)) :disposition "independent-successor"
                     :reason "The next observed source obstruction has a different repair and stacks on the retained candidate."}]
    (try
      (w/task! ctx (:key task) :status :acceptance-pending :compiler-result after)
      (with-controller ctx (constantly after)
        (fn [role cwd _ _]
          (swap! roles conj role)
          (let [proposal (assoc (repair-diagnosis cwd after) :candidates [disposition])]
            (case role
              :diagnose proposal
              :plan (-> (repair-plan (diagnosis/validate! cwd compiler/command after proposal
                                                        (:tasks @(:state ctx)) []))
                        (assoc :candidate-dispositions [disposition])
                        (update :slices (fn [slices] (mapv (fn [slice]
                                                          (assoc slice :stack-on (:key task)
                                                                       :dependencies [(:key task)])) slices)))))))
        #(with-redefs [w/worktree! real-worktree]
           (let [result (w/plan! ctx) selected (first (:slices result))
                 successor (w/task ctx (:key selected)) parent (w/task ctx (:key task))]
             (is (= [:diagnose :plan] @roles))
             (is (= (:candidate parent) (:planning-base successor)))
             (is (= (:key parent) (:stack-on successor)))
             (is (= [(:key parent)] (:dependencies successor)))
             (is (= :acceptance-pending (:status parent)))
             (is (= (:candidate task) (:candidate parent)))
             (is (w/eligible? ctx successor []))
             (is (not (w/dependencies-merged? ctx successor)))
             (is (= 2 (count (w/tasks ctx)))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest pending-semantic-evidence-can-complete-without-another-author-or-model-spin
  (let [ctx (context) before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        baseline-ref (reference (get-in ctx [:config :repo-dir]) "producer.clj" 2 2)
        task (candidate! ctx "continued-proof" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :checked :receiver receiver})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-NEXT" :next-source-obstruction)
        additional ["clojure" "-M:test" "source-specific-proof-fixture"]
        reviews (atom 0) acceptance-calls (atom 0) roles (atom [])]
    (try
      (with-controller ctx (constantly after)
        (fn [role cwd _ schema]
          (swap! roles conj [role schema])
          (cond
            (= :diagnose role) (blocked-diagnosis cwd after)
            (= schema diagnosis/progress-schema)
            (let [round (swap! reviews inc)]
              (-> (progress-proposal (w/task ctx (:key task))
                                    (if (= round 1) "pending" "established") baseline-ref)
                  (assoc-in [:checks 0 :command] additional)))
            :else (if (= 1 (swap! acceptance-calls inc))
                    (throw (ex-info "Synthetic independent review unavailable" {}))
                    (merge (select-keys (w/task ctx (:key task)) [:base :candidate :tree])
                           {:verdict "approved" :evidence-scope "implementation" :summary "Independent exact candidate review"
                            :findings [] :residual-host-boundary "Fixture bootstrap host remains."}))))
        #(with-redefs [shared/pr-info (fn [_ t] {:state "OPEN" :headRefOid (:candidate t) :isDraft false})
                       gh/api (fn [_ _]
                                {:number 42 :state "open"
                                 :body (str "<!-- uniform-seedless:" (:key task) " -->\nImplements #1.")
                                 :head {:ref (:branch task) :sha (:candidate task) :repo {:full_name "fixture/repo"}}
                                 :base {:ref "main" :repo {:full_name "fixture/repo"}}})]
           (with-redefs [rt/lease-phase! (fn [& _])]
             (w/check-candidate! ctx (:key task) [positive-check negative-check] nil))
           (is (= :acceptance-pending (w/accept! ctx (:key task) [positive-check negative-check])))
           (let [prior (w/task ctx (:key task))]
             (is (not (some #{additional} (get-in prior [:checks :commands]))))
             (let [error (try (w/continue-pending! ctx (:key task)) nil (catch Exception e e))
                   inspection (io/file (:root ctx) "review-inspection.edn")]
               (is (= "Synthetic independent review unavailable" (some-> error .getMessage)))
               (when error (w/attention! ctx (:key task) error))
               (is (= :attention (:status (w/task ctx (:key task)))))
               (is (= :review (:resume-status (w/task ctx (:key task)))))
               (is (true? (:pending-evidence-resume (w/task ctx (:key task)))))
               (rt/write-edn! inspection {:evidence "Inspected the failed read-only acceptance job; source and checks are unchanged."})
               (main/retry! ctx (:key task) (str inspection))
               (is (= :acceptance-pending (:status (w/task ctx (:key task)))))
               (w/continue-pending! ctx (:key task)))
             (let [accepted (w/task ctx (:key task)) count-before (count @roles)]
               (is (= :ci (:status accepted)))
               (is (= 2 @reviews))
               (is (= 2 @acceptance-calls))
               (is (not (:pending-evidence-resume accepted)))
               (is (= (:candidate prior) (:candidate accepted)))
               (is (= (:tree prior) (:tree accepted)))
               (is (every? (set (get-in accepted [:checks :commands]))
                           (conj (get-in prior [:checks :commands]) additional)))
               (is (not-any? (fn [[role _]] (= :implement role)) @roles))
               (w/continue-pending! ctx (:key task))
               (is (= count-before (count @roles)))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest confirmed-original-obstruction-reopens-only-the-inspected-candidate-repair
  (let [ctx (context) before (unknown-observation ctx "FUTURE-ORIGINAL" :receiver-constraint-unavailable)
        task (candidate! ctx "retained-obstruction" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :rejected :receiver receiver :inspected true})\n")
        after (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                   "FUTURE-ORIGINAL" :receiver-constraint-unavailable)
        roles (atom []) inspection (io/file (:root ctx) "obstruction-inspection.edn")]
    (try
      (w/task! ctx (:key task) :status :acceptance-pending :compiler-progress-resume true
               :checks {:candidate (:candidate task) :tree (:tree task) :result :passed
                        :commands (vec (concat (get-in ctx [:config :checks]) [positive-check negative-check]))})
      (with-controller ctx (constantly after)
        (fn [role cwd _ _]
          (swap! roles conj role)
          (is (= :diagnose role))
          (assoc (repair-diagnosis cwd after)
                 :candidates [{:candidate (str "slice/" (:key task)) :disposition "wait"
                               :reason "The selected obstruction remains in this exact retained source candidate."}]))
        #(let [diagnosed (w/diagnose! ctx (:worktree task) after [])]
           (is (= (:compiler-failure task) (get-in diagnosed [:failure :work-key])))
           (w/task! ctx (:key task) :compiler-result diagnosed)
           (let [error (try (w/continue-pending! ctx (:key task)) nil (catch Exception e e))]
             (is (= "Compiler rerun still reaches the selected failure" (some-> error .getMessage)))
             (is (true? (:compiler-repair-required (ex-data error))))
             (is (= (select-keys task [:base :candidate :tree])
                    (select-keys (ex-data error) [:base :candidate :tree])))
             (when error (w/attention! ctx (:key task) error))
             (let [retained (w/task ctx (:key task))]
               (is (= :attention (:status retained)))
               (is (true? (:pending-evidence-resume retained)))
               (rt/write-edn! inspection
                              {:evidence "Inspected the source-grounded result: the original selected obstruction still holds on the exact candidate."})
               (main/retry! ctx (:key task) (str inspection))
               (let [repair (w/task ctx (:key task))]
                 (is (= :repair (:status repair)))
                 (is (= (select-keys retained [:base :candidate :tree :issue :checks :compiler-observation :compiler-result])
                        (select-keys repair [:base :candidate :tree :issue :checks :compiler-observation :compiler-result])))
                 (is (not (:pending-evidence-resume repair)))
                 (is (not (:compiler-progress-resume repair))))
               ;; A later real commit must not inherit this old source-repair verdict.
               (rt/update! ctx assoc-in [:tasks (:key task)] retained)
               (let [later (revise-candidate! ctx retained
                                              "(ns fixture.checker)\n(defn reject-type [receiver] {:status :pending :receiver receiver})\n(def later-inspection true)\n")]
                 (is (not= (:candidate retained) (:candidate later)))
                 (rt/write-edn! inspection
                                {:evidence "Inspected the later candidate; the retained obstruction result names an earlier commit."})
                 (main/retry! ctx (:key task) (str inspection))
                 (is (= :acceptance-pending (:status (w/task ctx (:key task)))))
                 (is (= (:candidate later) (:candidate (w/task ctx (:key task)))))))
             (is (= [:diagnose] @roles))
             (is (= [(:key task)] (mapv :key (w/tasks ctx)))))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest composing-new-main-archives-old-progress-and-assesses-the-new-exact-candidate
  (let [ctx (context) repo (get-in ctx [:config :repo-dir])
        before (unknown-observation ctx "FUTURE-BEFORE" :first-obstruction)
        before-ref (reference repo "producer.clj" 2 2)
        task (candidate! ctx "composed-proof" before)
        task (revise-candidate! ctx task "(ns fixture.checker)\n(defn reject-type [receiver] {:status :checked :receiver receiver})\n")
        observed (fn [] (unknown-observation (assoc-in ctx [:config :repo-dir] (:worktree task))
                                             "FUTURE-AFTER" :next-obstruction))
        reviews (atom 0)]
    (try
      (with-controller ctx observed
        (fn [role cwd _ schema]
          (cond
            (= :diagnose role) (blocked-diagnosis cwd (observed))
            (= schema diagnosis/progress-schema)
            (do (swap! reviews inc)
                (progress-proposal (w/task ctx (:key task)) "pending" before-ref))
            :else (throw (ex-info "Composition cannot use stale review" {:role role}))))
        #(with-redefs [rt/lease-phase! (fn [& _])]
           (w/check-candidate! ctx (:key task) [positive-check negative-check] nil)
           (let [checked (w/task ctx (:key task)) proof (progress-proposal checked "established" before-ref)]
             (is (diagnosis/validate-progress! ctx checked proof))
             (w/task! ctx (:key task) :compiler-progress proof :compiler-investigation-planned "previous-frontier")
             (let [old (w/task ctx (:key task))]
               (spit (io/file repo "module.clj") "(ns fixture)\n(defn example [receiver] (map-keys receiver))\n(def incoming-main true)\n")
               (rt/git ctx repo "add" "module.clj")
               (rt/git ctx repo "commit" "-m" "disjoint current main source change")
               (let [base (rt/git ctx repo "rev-parse" "HEAD")]
                 (is (w/merge-base! ctx old base))
                 (let [composed (w/task ctx (:key task))]
                   (is (= base (:base composed)))
                   (is (not= (:candidate old) (:candidate composed)))
                   (is (= (:compiler-observation old) (:compiler-observation composed)))
                   (is (nil? (:compiler-result composed)))
                   (is (nil? (:compiler-progress composed)))
                   (is (nil? (:compiler-investigation-planned composed)))
                   (is (= [(select-keys old [:base :candidate :tree :compiler-result :compiler-progress])]
                          (:compiler-assessment-history composed)))
                   (is (str/includes? (slurp (io/file (:worktree task) "module.clj")) "incoming-main")))
                 (w/check-candidate! ctx (:key task) [positive-check negative-check] nil)
                 (is (= :acceptance-pending (w/accept! ctx (:key task) [positive-check negative-check])))
                 (is (= 1 @reviews))
                 (is (= (:candidate (w/task ctx (:key task)))
                        (get-in (w/task ctx (:key task)) [:compiler-progress :candidate]))))))))
      (finally (fixture/delete-tree! (:root ctx))))))
