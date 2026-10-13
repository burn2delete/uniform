(ns gravity.self-hosting.sh07-b51-v9-existing-object-replay-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files OpenOption Path StandardOpenOption]))

(defn- git!
  [^Path root & arguments]
  (let [command (into [evidence/git-executable "-C" (str root)] arguments)
        process (.start (doto (ProcessBuilder. ^java.util.List command)
                          (.redirectErrorStream true)))
        output (.readAllBytes (.getInputStream process))
        exit (.waitFor process)]
    (when-not (zero? exit)
      (throw (ex-info "test Git command failed"
                      {:command command :exit exit
                       :output (String. output StandardCharsets/UTF_8)})))
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
  (codec/H [:gravity/test-sh07-b51-existing label]))

(defn- identity-receipt
  [content]
  {:artifact :gravity/sh07-b51-checker-process-identity-receipt-v1
   :schema-version 1
   :content content
   :semantic-ordinals (vec (range 11))
   :coordinator-events []
   :coordinator-event-root (typed-sha :coordinator-events)
   :child-process-receipts []
   :launcher-receipt {:scope :test-seam}
   :coordinator-receipt {:scope :test-seam}
   :payload-joins {:scope :test-seam}
   :evidence-eligibility {:scope :test-seam}
   :pre-discharge-id (typed-sha :pre-discharge)
   :runtime-contract-discharge {:scope :test-seam}
   :receipt-id-normalization {:scope :test-seam}
   :self-id-omits [[:receipt-id]]
   :receipt-id (typed-sha :normalized-receipt)
   :nonclaims evidence/nonclaims})

(defn- initialize-e9!
  [& {:keys [extra-path]}]
  (let [root (Files/createTempDirectory
              "sh07-b51-existing-"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (git! root "init")
    (git! root "config" "user.name" "Gravity Test")
    (git! root "config" "user.email" "gravity@example.invalid")
    (write-bytes! root "seed.edn" (codec/W {:seed true}))
    (write-bytes! root "docs/artifacts/phase-15/evidence/.keep"
                  (.getBytes "tracked parent\n" StandardCharsets/UTF_8))
    (let [c (commit! root "C")
          t (git! root "show" "-s" "--format=%T" c)
          content {:commit c :tree t}
          payloads (mapv (fn [ordinal]
                           {:payload ordinal :fresh-process true})
                         (range 6))
          receipt (identity-receipt content)
          values (conj payloads receipt)]
      (doseq [[path value] (map vector evidence/evidence-paths values)]
        (write-bytes! root path (codec/W value)))
      (when extra-path
        (write-bytes! root extra-path (codec/W {:foreign true})))
      (let [e (commit! root "E")
            te (git! root "show" "-s" "--format=%T" e)]
        ;; Existing replay must remain valid when no E9 worktree path exists.
        (git! root "checkout" "--detach" c)
        {:root root :content content :payloads payloads :receipt receipt
         :existing {:commit e :tree te :parents [c]}}))))

(defn- failure
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (select-keys (ex-data error) [:id :reason]))))

(deftest existing-replay-is-object-only-and-writes-zero-final-bytes
  (let [{:keys [root content payloads receipt existing]} (initialize-e9!)
        final-root (.resolve root evidence/evidence-relative-root)
        before (git! root "status" "--porcelain=v1" "-z"
                     "--untracked-files=all")
        result
        (evidence/verify-existing-object-replay!
         {:repository-root root :content content
          :existing-evidence existing
          :fresh-payload-values payloads
          :fresh-identity-receipt-id (:receipt-id receipt)})
        after (git! root "status" "--porcelain=v1" "-z"
                    "--untracked-files=all")]
    (is (= 7 (count (:objects result))))
    (is (= 7 (count (:artifact-descriptors result))))
    (is (= (vec (range 17)) (mapv :ordinal (:observations result))))
    (is (every? #(= evidence/git-executable (first (:argv %)))
                (:observations result)))
    (is (= [true true 0 :verified-existing :none]
           [(:payloads-equal result) (:receipt-id-equal result)
            (:final-write-count result) (:outcome result)
            (:authority result)]))
    (is (= before after ""))
    (is (not (Files/exists final-root
                           (make-array java.nio.file.LinkOption 0))))))

(deftest existing-replay-rejects-payload-drift-and-non-seven-deltas
  (testing "fresh process output must reproduce all six W byte vectors"
    (let [{:keys [root content payloads receipt existing]} (initialize-e9!)]
      (is (= {:id "SH07-B51-GIT-RELOAD"
              :reason :existing-semantic-replay}
             (failure
              #(evidence/verify-existing-object-replay!
                {:repository-root root :content content
                 :existing-evidence existing
                 :fresh-payload-values (assoc payloads 2 {:drift true})
                 :fresh-identity-receipt-id (:receipt-id receipt)}))))))
  (testing "E must be the direct seven-addition child of C"
    (let [{:keys [root content payloads receipt existing]}
          (initialize-e9! :extra-path "foreign.edn")]
      (is (= {:id "SH07-B51-GIT-RELOAD"
              :reason :existing-coordinate-or-delta}
             (failure
              #(evidence/verify-existing-object-replay!
                {:repository-root root :content content
                 :existing-evidence existing
                 :fresh-payload-values payloads
                 :fresh-identity-receipt-id (:receipt-id receipt)}))))))
  (testing "the existing E/TE coordinate is exact and direct from C"
    (let [{:keys [root content payloads receipt existing]} (initialize-e9!)
          invoke
          (fn [coordinate]
            (evidence/verify-existing-object-replay!
             {:repository-root root :content content
              :existing-evidence coordinate
              :fresh-payload-values payloads
              :fresh-identity-receipt-id (:receipt-id receipt)}))]
      (is (= {:id "SH07-B51-GIT-RELOAD"
              :reason :existing-evidence}
             (failure #(invoke (dissoc existing :parents)))))
      (is (= {:id "SH07-B51-GIT-RELOAD"
              :reason :existing-evidence}
             (failure #(invoke (assoc existing :path "forged")))))
      (is (= {:id "SH07-B51-GIT-RELOAD"
              :reason :existing-parent}
             (failure #(invoke (assoc existing :parents []))))))))
