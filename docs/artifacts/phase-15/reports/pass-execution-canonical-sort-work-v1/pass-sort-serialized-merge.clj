(require '[gravity.self-hosting.sh01-host-resource-broker :as broker]
         '[clojure.java.shell :as shell] '[clojure.string :as str])
(System/setProperty "gravity.workstream-governance.library" "true")
(load-file "tools/validate_workstream_governance.clj")
(def read-json gravity.workstream-governance/read-strict-json)
(defn checked [& args]
 (let [r (apply shell/sh args)]
  (when-not (zero? (:exit r)) (throw (ex-info "serialized merge command failed" {:command args :exit (:exit r) :stderr (:err r) :stdout (:out r)})))
  (str/trim (:out r))))
(defn ensure! [test data] (when-not test (throw (ex-info "serialized merge HOLD" data))))
(defn now [] (str (java.time.Instant/now)))
(def arguments (vec *command-line-args*))
(ensure! (= 4 (count arguments)) {:reason :arguments})
(def pr (arguments 0))
(def base (arguments 1))
(def head (arguments 2))
(def tree (arguments 3))
(def start (System/nanoTime))
(def watchdog (doto (Thread. #(do (Thread/sleep 720000) (println :serialized-merge-timeout) (flush) (System/exit 124))) (.setDaemon true) (.start)))
(def result
 (broker/with-lease {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1" :timeout-ms 600000
                    :on-event (fn [event] (prn (select-keys event [:event :phase :resource-class :wait-ms])) (flush))}
 :exclusive
 (fn []
  (with-open [channel (java.nio.channels.FileChannel/open
                       (java.nio.file.Paths/get "/private/tmp/gravity-sh07-heavy.lock" (make-array String 0))
                       (into-array java.nio.file.OpenOption [java.nio.file.StandardOpenOption/CREATE java.nio.file.StandardOpenOption/WRITE java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
   (let [held (.tryLock channel)]
    (ensure! held {:reason :canonical-lock-busy})
    (try
     (println :exclusive-and-canonical-lock-held) (flush)
     (checked "git" "fetch" "origin" "main")
     (ensure! (= base (checked "git" "rev-parse" "origin/main")) {:reason :main-advanced :expected base})
     (ensure! (= head (checked "git" "rev-parse" "HEAD")) {:reason :head-drift})
     (ensure! (= tree (checked "git" "rev-parse" "HEAD^{tree}")) {:reason :tree-drift})
     (ensure! (str/blank? (checked "git" "status" "--porcelain")) {:reason :dirty})
     (let [preflight (read-json (checked "clojure" "-J-Xmx512m" "-M" "tools/check_worktree_preflight.clj" "--mode" "integration" "--base-ref" "origin/main" "--candidate-base" base "--candidate-commit" head "--candidate-tree" tree))
           github (read-json (checked "gh" "pr" "view" pr "--json" "state,baseRefName,headRefOid,mergeable,mergeStateStatus,statusCheckRollup,reviewDecision"))]
      (ensure! (true? (get-in preflight ["preconditions" "all-pass"])) {:reason :preflight :value preflight})
      (ensure! (and (= "OPEN" (get github "state")) (= "main" (get github "baseRefName"))
                    (= head (get github "headRefOid")) (= "MERGEABLE" (get github "mergeable"))
                    (= "CLEAN" (get github "mergeStateStatus"))
                    (empty? (get github "statusCheckRollup"))
                    (not= "CHANGES_REQUESTED" (get github "reviewDecision"))) {:reason :github-gates :value github})
      (checked "gh" "pr" "merge" pr "--merge" "--match-head-commit" head)
      (checked "git" "fetch" "origin" "main")
      (checked "git" "merge-base" "--is-ancestor" head "origin/main")
      (let [after (read-json (checked "gh" "pr" "view" pr "--json" "state,mergeCommit,mergedAt,headRefOid"))
            main (checked "git" "rev-parse" "origin/main")]
       (ensure! (= "MERGED" (get after "state")) {:reason :not-merged :value after})
       {:schema :gravity/serialized-scoped-integration-observation-v1 :authority :integration-only
        :base base :candidate head :tree tree :pull-request pr :github-after after :origin-main main
        :preflight preflight :github-before github :at (now) :elapsed-ms (/ (- (System/nanoTime) start) 1e6)
        :serialization [:exclusive-sh01-broker :canonical-sh07-heavy-lock]
        :nonclaims [:fresh-full-suite :proof :sh07 :release :self-hosting :seed-retirement]}))
     (finally (.release held))))))))
(spit (str "/tmp/pass-sort-merge-" pr ".edn") (str (pr-str result) "\n"))
(prn result)
(shutdown-agents)
