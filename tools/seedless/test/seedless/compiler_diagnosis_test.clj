(ns seedless.compiler-diagnosis-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [gravity.compiler-probe :as probe]
            [seedless.compiler-diagnosis :as diagnosis]
            [seedless.compiler-observation :as compiler]
            [seedless.workflow-test :as fixture]
            [simplify.runtime :as rt]
            [simplify.schema :as s]))

;; These small source programs and captures are synthetic. Real recorded
;; capture provenance and production workflow boundaries have separate tests.
(def paths ["module.clj" "producer.clj" diagnosis/ownership-path diagnosis/requirements-path])
(use-fixtures :each
  (fn [test] (with-redefs [rt/event! (fn [& _]) probe/input-paths paths] (test))))

(defn context []
  (let [ctx (fixture/repository! (fixture/context)) cwd (get-in ctx [:config :repo-dir])]
    (spit (io/file cwd "module.clj") "(defn subject [x] (keys x))\n")
    (spit (io/file cwd "producer.clj") "(defn infer [x] {:status :rejected :reason :unfamiliar-alpha})\n")
    (io/make-parents (io/file cwd diagnosis/ownership-path))
    (spit (io/file cwd diagnosis/ownership-path)
          (pr-str {:schema :gravity/self-hosting-slice-ownership-v1
                   :module-owners {"producer.clj" :sh-types "module.clj" :sh-envelope}
                   :reserved-leaf-modules {"c7-type-checker" :sh-types}
                   :slice-owners {"SH-08" {:leaf-owner :sh-types} "SH-02" {:leaf-owner :sh-envelope}}
                   :future-path-conventions {:worker-may-create [:new-fixture :dedicated-test]}}))
    (spit (io/file cwd diagnosis/requirements-path)
          "2. **SH-08.2:** Check function parameters/results and calls.\n3. **SH-02.3:** Compute deterministic path-neutral semantic identity.\n")
    (rt/git ctx cwd "add" ".")
    (rt/git ctx cwd "commit" "-m" "Synthetic source-grounded diagnosis fixture")
    (assoc-in ctx [:config :compiler-observation-command] compiler/command)))

(defn reference [cwd path line]
  {:path path :sha256 (compiler/input-sha256 cwd path)
   :line-start line :line-end line
   :excerpt (nth (str/split-lines (slurp (io/file cwd path))) (dec line))})

(defn raw-observation [ctx reason]
  (let [cwd (get-in ctx [:config :repo-dir])
        raw {:source-revision (rt/git ctx cwd "rev-parse" "HEAD")
             :inputs (compiler/declared-inputs cwd)
             :route {:command compiler/command :stage :current-source-function-types
                     :target :llvm-x86_64-linux :triple "x86_64-unknown-linux-gnu"
                     :scope :synthetic-complete-test-population
                     :closure [{:component-id :synthetic-module :source-path "module.clj"}]}
             :position {:component 0 :stage :function-types} :completed []
             :subject {:source-path "module.clj" :source-sha256 (compiler/input-sha256 cwd "module.clj")}
             :terminal {:status :failed :exit 1} :status :blocked
             :failure {:id "SYNTHETIC-REJECTION" :kind :unclassified :source "module.clj"
                       :reason "Synthetic typed compiler rejection"
                       :diagnostic {:compiler-result-status :rejected :reason reason :function 'subject}}}
        raw (assoc raw :input-id (compiler/input-id raw))]
    (assoc raw :execution {:exitCode 1 :stdout (pr-str raw) :stderr "" :capture-truncated false})))

(defn proposal [ctx observation]
  (let [cwd (get-in ctx [:config :repo-dir])]
    {:outcome "repair" :source-revision (:source-revision observation) :input-id (:input-id observation)
     :ownership-id (get-in (diagnosis/context cwd observation {} []) [:ownership :id])
     :summary "Trace synthetic inference rejection" :cause "The producer rejects the source operation."
     :producer-refs [(reference cwd "producer.clj" 1)] :trigger-refs [(reference cwd "module.clj" 1)]
     :owners [{:role "repair" :milestone "SH-08" :task "SH-08.2" :paths ["producer.clj"]
               :requirement (reference cwd diagnosis/requirements-path 1)}]
     :checks [{:kind "positive" :command ["clojure" "-M:test" "--exact" "accepted-case"]
               :establishes "Accept the required operation"}
              {:kind "negative" :command ["clojure" "-M:test" "--exact" "rejected-case"]
               :establishes "Reject malformed receiver"}]
     :preserve "Preserve malformed-input rejection and earlier accepted behavior."
     :candidates [] :missing-evidence []}))

(deftest source-reference-schema-validates-real-primitive-types
  (is (= true (s/validate! {:type "boolean"} true)))
  (is (= false (s/validate! {:type "boolean"} false)))
  (is (= 1 (s/validate! {:type "integer" :minimum 1} 1)))
  (is (thrown? clojure.lang.ExceptionInfo (s/validate! {:type "integer" :minimum 1} 0)))
  (is (thrown? clojure.lang.ExceptionInfo (s/validate! {:type "integer"} "1")))
  (is (thrown? clojure.lang.ExceptionInfo (s/validate! {:type "boolean"} "true"))))

(deftest generic-investigation-and-ownership-need-no-diagnostic-name-rule
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])]
    (try
      (doseq [reason [:unfamiliar-alpha :unfamiliar-beta]]
        (let [observation (raw-observation ctx reason)
              output (diagnosis/validate! cwd compiler/command observation (proposal ctx observation) {} [])]
          (is (diagnosis/investigable? cwd observation compiler/command))
          (is (= :actionable-failure (:status output)))
          (is (= "SH-08.2" (get-in output [:failure :owner :task])))
          (is (= observation (get-in output [:diagnosis :raw-observation])))
          (is (diagnosis/current-diagnosis? cwd output))
          (doseq [invalid [(assoc-in output [:failure :owner :paths] ["module.clj"])
                           (assoc-in output [:diagnosis :proposal :owners 0 :task] "SH-08.99")
                           (assoc-in output [:diagnosis :id] "forged")]]
            (is (not (diagnosis/current-diagnosis? cwd invalid))))
          (is (diagnosis/validate-selection! cwd output
                                           {:compiler-role "repair" :compiler-task "SH-08.2" :stage "SH-08"
                                            :paths ["producer.clj"]}))
          (is (thrown? clojure.lang.ExceptionInfo
                       (diagnosis/validate-selection! cwd output
                                                     {:compiler-role "repair" :compiler-task "SH-08.2" :stage "SH-08"
                                                      :paths ["producer.clj" "module.clj"]})))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest execution-and-evidence-blockers-never-gain-investigation-authority
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) raw (raw-observation ctx :unfamiliar-alpha)]
    (try
      (doseq [invalid [(dissoc raw :execution)
                       (assoc raw :restricted true)
                       (assoc-in raw [:execution :capture-truncated] true)
                       (assoc-in raw [:execution :exitCode] 124)
                       (assoc-in raw [:execution :exitCode] 137)
                       (assoc-in raw [:execution :stdout] (str (get-in raw [:execution :stdout]) " {}"))
                       (-> raw (assoc :execution-warnings ["arbitrary output\n"])
                           (update-in [:execution :stdout] #(str "arbitrary output\n" %)))
                       (assoc-in raw [:failure :kind] :environment)
                       (assoc-in raw [:failure :id] "P18T04007")
                       (assoc-in raw [:subject :source-sha256] (apply str (repeat 64 "0")))
                       (assoc-in raw [:failure :diagnostic :reason] :invented)
                       (assoc-in raw [:inputs 0 :sha256] (apply str (repeat 64 "0")))]]
        (is (not (diagnosis/investigable? cwd invalid compiler/command))))
      (doseq [kind [:resource :environment :stale-evidence :release-refusal]]
        (let [raw (assoc-in (dissoc raw :execution) [:failure :kind] kind)
              captured (assoc raw :execution {:exitCode 1 :stdout (pr-str raw) :capture-truncated false})]
          (is (not (diagnosis/investigable? cwd captured compiler/command)))))
      (let [refusal (assoc-in (dissoc raw :execution) [:failure :id] "P18T04007")]
        (is (not (diagnosis/investigable? cwd
                                         (assoc refusal :execution {:exitCode 1 :stdout (pr-str refusal)})
                                         compiler/command))))
      (spit (io/file cwd "producer.clj") "Changed source after capture\n")
      (is (not (diagnosis/investigable? cwd raw compiler/command)))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest structured-result-origin-is-distinct-from-arbitrary-host-exceptions
  (let [component {:source-path "module.clj"}
        rejected (try (#'probe/require-compiler-result!
                        {:status :rejected :diagnostics [{:id "UNFAMILIAR-PRODUCER" :reason :new-rejection}]}
                        :accepted "A current source-producing stage")
                      (catch clojure.lang.ExceptionInfo error error))
        classified (probe/classify-failure component :current-frontend rejected)]
    (is (= :rejected (get-in classified [:failure :diagnostic :compiler-result-status])))
    (is (= "UNFAMILIAR-PRODUCER" (get-in classified [:failure :id])))
    (is (nil? (get-in (probe/classify-failure component :current-frontend
                                              (ex-info "Unavailable prerequisite" {:id "SOURCE-PIN"}))
                     [:failure :diagnostic :compiler-result-status])))
    (is (= :resource (get-in (probe/classify-failure component :current-frontend
                                                    (ex-info "Resource failure" {:resource-failure? true}))
                            [:failure :kind])))
    (is (= :environment (get-in (probe/classify-failure component :current-frontend
                                                       (java.io.IOException. "Unavailable input"))
                               [:failure :kind])))))

(deftest repair-attribution-requires-real-source-current-ownership-and-retained-work
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) raw (raw-observation ctx :unfamiliar-alpha)
        output (proposal ctx raw)]
    (try
      (doseq [invalid [(assoc-in output [:producer-refs 0 :excerpt] "invented source")
                       (assoc-in output [:trigger-refs 0 :line-start] 500)
                       (assoc-in output [:producer-refs 0 :path] "../outside.clj")
                       (assoc-in output [:owners 0 :task] "SH-08.99")
                       (assoc-in output [:owners 0 :paths] ["module.clj"])
                       (assoc output :ownership-id "stale")
                       (assoc output :missing-evidence ["The actual receiver remains unknown."])
                       (assoc output :checks [(first (:checks output))])
                       (assoc output :candidates [{:candidate "slice/absent" :disposition "reuse" :reason "invented"}])]]
        (is (thrown? clojure.lang.ExceptionInfo (diagnosis/validate! cwd compiler/command raw invalid {} []))))
      (is (thrown? clojure.lang.ExceptionInfo
                   (diagnosis/validate! cwd compiler/command raw output
                                        {"retained" {:key "retained" :status :review :paths ["producer.clj"]}} [])))
      (is (diagnosis/validate! cwd compiler/command raw
                              (assoc-in output [:owners 0 :paths]
                                        ["producer.clj" "bootstrap/clojure/src/gravity/c7_type_checker/inference.clj"
                                         "bootstrap/clojure/test/gravity/c7_type_checker/inference_test.clj"])
                              {} []))
      (let [blocker (assoc output :outcome "blocked" :owners [] :producer-refs [] :trigger-refs []
                           :checks [] :missing-evidence ["The retained capture omits the inferred receiver type."])
            waiting (diagnosis/validate! cwd compiler/command raw blocker {} [])]
        (is (= :blocked (:status waiting)))
        (is (nil? (get-in waiting [:failure :owner]))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest source-span-selects-the-actual-trigger-in-the-module
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir])]
    (try
      (spit (io/file cwd "module.clj") "(defn subject [x] (keys x))\n(defn unrelated [] 1)\n")
      (rt/git ctx cwd "add" "module.clj")
      (rt/git ctx cwd "commit" "-m" "Add unrelated source for span attribution")
      (let [raw (-> (raw-observation ctx :unfamiliar-alpha) (dissoc :execution)
                    (assoc-in [:failure :diagnostic :source-span] {:line-start 1 :line-end 1}))
            observation (assoc raw :execution {:exitCode 1 :stdout (pr-str raw) :capture-truncated false})
            output (proposal ctx observation)]
        (is (diagnosis/validate! cwd compiler/command observation output {} []))
        (is (thrown? clojure.lang.ExceptionInfo
                     (diagnosis/validate! cwd compiler/command observation
                                          (assoc output :trigger-refs [(reference cwd "module.clj" 2)]) {} []))))
      (finally (fixture/delete-tree! (:root ctx))))))

(deftest same-stage-progress-requires-independent-source-and-executed-semantic-evidence
  (let [ctx (context) cwd (get-in ctx [:config :repo-dir]) raw (raw-observation ctx :unfamiliar-alpha)
        selected (diagnosis/validate! cwd compiler/command raw (proposal ctx raw) {} [])
        old-ref (reference cwd "producer.clj" 1)]
    (try
      (spit (io/file cwd "producer.clj") "(defn infer [x] {:status :accepted :value x})\n")
      (rt/git ctx cwd "add" "producer.clj")
      (rt/git ctx cwd "commit" "-m" "Synthetic original obstruction repair")
      (let [after (raw-observation ctx :unfamiliar-beta)
            candidate (rt/git ctx cwd "rev-parse" "HEAD") tree (rt/git ctx cwd "rev-parse" "HEAD^{tree}")
            semantic-checks (get-in selected [:diagnosis :proposal :checks])
            task {:base (:source-revision raw) :candidate candidate :tree tree :worktree cwd
                  :compiler-observation selected :compiler-result after
                  :checks {:result :passed :candidate candidate :tree tree :commands (mapv :command semantic-checks)}}
            progress {:outcome "established" :base (:base task) :candidate candidate :tree tree
                      :before-input-id (:input-id selected) :after-input-id (:input-id after)
                      :summary "Synthetic independent review traced the removed obstruction and preserved rejections."
                      :original-obstruction {:removed true :evidence "The original producing condition is removed."
                                             :before-refs [old-ref] :after-refs [(reference cwd "producer.clj" 1)]}
                      :preceding-behavior {:preserved true :evidence "The complete route and accepted prefix are preserved."}
                      :checks semantic-checks :missing-evidence []}]
        (is (not (compiler/advanced? selected after)))
        (is (diagnosis/validate-progress! ctx task progress))
        ;; Existing fast-path baselines predate generic diagnosis. Their
        ;; recorded owner/consumer identity still grounds a producer-only fix.
        (let [known (-> selected (dissoc :diagnosis)
                        (assoc-in [:failure :witness :consumer-source] (select-keys old-ref [:path :sha256])))]
          (is (diagnosis/validate-progress! ctx (assoc task :compiler-observation known) progress))
          (is (diagnosis/validate-progress! ctx
                                           (assoc task :compiler-observation (update-in known [:failure] dissoc :witness))
                                           progress))
          (is (thrown? clojure.lang.ExceptionInfo
                       (diagnosis/validate-progress! ctx
                                                    (assoc task :compiler-observation
                                                           (-> known (update-in [:failure] dissoc :witness)
                                                               (assoc-in [:failure :owner :paths] ["unrecorded-producer.clj"])))
                                                    progress))))
        (is (false? (diagnosis/validate-progress!
                     ctx task (assoc progress :outcome "pending" :missing-evidence ["No independent semantic conclusion yet."]))))
        (doseq [invalid [(assoc progress :candidate (:base task))
                         (assoc-in progress [:original-obstruction :removed] false)
                         (assoc-in progress [:original-obstruction :before-refs] [])
                         (assoc-in progress [:original-obstruction :after-refs 0 :excerpt] "unverified source")
                         (assoc progress :checks [(first semantic-checks)])
                         (assoc-in progress [:checks 0 :command] ["clojure" "unexecuted-check"])]]
          (is (thrown? clojure.lang.ExceptionInfo (diagnosis/validate-progress! ctx task invalid))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (diagnosis/validate-progress! ctx (assoc-in task [:checks :candidate] (:base task)) progress)))
        (swap! (:state ctx) assoc-in
               [:restricted-commands (rt/digest (pr-str ["git" "show" (str (:source-revision raw) ":producer.clj")]))]
               {:reason "Recorded source-inspection restriction"})
        (is (= true (try (diagnosis/validate-progress! ctx task progress) false
                        (catch clojure.lang.ExceptionInfo error (:restricted (ex-data error)))))))
      (finally (fixture/delete-tree! (:root ctx))))))
