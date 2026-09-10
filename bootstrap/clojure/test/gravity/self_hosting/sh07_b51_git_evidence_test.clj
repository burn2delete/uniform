(ns gravity.self-hosting.sh07-b51-git-evidence-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-git-evidence :as evidence]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files OpenOption Path StandardOpenOption]))

(def ^:private no-open-options (make-array OpenOption 0))
(def ^:private strict-read-wire (:strict-read-wire codec/transport-api))

(defn- git!
  [^Path directory & arguments]
  (let [command (into [evidence/git-executable "-C" (str directory)]
                      arguments)
        process (.start (doto (ProcessBuilder. ^java.util.List command)
                          (.redirectErrorStream true)))
        bytes (.readAllBytes (.getInputStream process))
        exit (.waitFor process)]
    (when-not (zero? exit)
      (throw (ex-info "test Git command failed"
                      {:command command :exit exit
                       :output (String. bytes StandardCharsets/UTF_8)})))
    (clojure.string/trim-newline
     (String. bytes StandardCharsets/UTF_8))))

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

(defn- initialized-repository!
  []
  (let [root (Files/createTempDirectory
              "sh07-b51-git-evidence-"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (git! root "init")
    (git! root "config" "user.name" "Gravity Test")
    (git! root "config" "user.email" "gravity@example.invalid")
    (write-bytes! root "seed.edn" (codec/W {:seed true}))
    (write-bytes! root "docs/artifacts/phase-15/evidence/.keep"
                  (.getBytes "tracked parent\n" StandardCharsets/UTF_8))
    (let [commit (commit! root "content")
          tree (git! root "show" "-s" "--format=%T" commit)]
      {:root root :content {:commit commit :tree tree}})))

(defn- payload-values []
  (mapv (fn [ordinal] {:payload ordinal :accepted true}) (range 6)))

(defn- stage!
  [values]
  (let [root (Files/createTempDirectory
              "sh07-b51-stage-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        descriptors
        (mapv
         (fn [[ordinal name kind] value]
           (let [bytes (codec/W value)]
             (write-bytes! root name bytes)
             {:ordinal ordinal :name name :kind kind
              :relative-path (str evidence/evidence-relative-root "/" name)
              :wire-codec :gravity/sh07-b51-edn-wire-v1
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)
              :value-id
              (codec/H
               [:gravity/sh07-b51-staged-payload-value-v1
                {:ordinal ordinal :name name :kind kind :value value}])}))
         evidence/payload-specs values)]
    {:root root :descriptors descriptors}))

(defn- typed-sha [label]
  (codec/H [:gravity/test-sh07-b51-v9 label]))

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

(defn- add-worktree!
  [root commit suffix]
  (let [path (.resolve (.getParent root)
                       (str (.getFileName root) "-" suffix))]
    (git! root "worktree" "add" "--detach" (str path) commit)
    path))

(defn- failure-reason
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:reason (ex-data error)))))

(deftest validates-distinct-clean-content-worktrees
  (let [{:keys [root content]} (initialized-repository!)
        publication (add-worktree! root (:commit content) "publication")
        result (evidence/validate-publication-worktrees!
                {:source-worktree root
                 :publication-worktree publication
                 :content content})]
    (is (= content
           (select-keys (:source-worktree result) [:commit :tree])
           (select-keys (:publication-worktree result) [:commit :tree])))
    (is (not= (get-in result [:source-worktree :canonical-path])
              (get-in result [:publication-worktree :canonical-path])))
    (is (= (get-in result [:source-worktree :git-common-directory])
           (get-in result [:publication-worktree :git-common-directory])))
    (is (= :none (:authority result)))
    (is (= :publication-worktree-separation
           (failure-reason
            #(evidence/validate-publication-worktrees!
              {:source-worktree root
               :publication-worktree root
               :content content}))))))

(deftest validates-six-canonical-m-payloads
  (let [content {:commit (apply str (repeat 40 "1"))
                 :tree (apply str (repeat 40 "2"))}
        values (payload-values)
        {:keys [root descriptors]} (stage! values)
        response-base
        {:schema :gravity/sh07-b51-m-response-v1
         :status :accepted
         :content content
         :payload-descriptors descriptors
         :staging-receipt
         {:staging-path (str (.normalize (.toAbsolutePath root)))
          :evidence-relative-root evidence/evidence-relative-root
          :directory-mode "0700"
          :symlink-free true
          :payload-names evidence/payload-names
          :payload-descriptors descriptors}
         :coordinator-receipt {:scope :test-seam}
         :source-read-receipt {:scope :test-seam}
         :counters {:scope :test-seam}
         :transport
         {:response-preimage-id nil
          :self-id-omits [[:transport :response-preimage-id]]}}
        response-id
        (codec/H
         [:gravity/sh07-b51-response-preimage-id-v1
          (update response-base :transport dissoc :response-preimage-id)])
        response (assoc-in response-base
                           [:transport :response-preimage-id] response-id)
        result (evidence/validate-m-response!
                {:content content :staging-root root
                 :response-bytes (codec/W response)})]
    (is (= values (mapv :value (:staged-payloads result))))
    (is (= response-id (:response-preimage-id result)))
    (is (= :none (:authority result)))
    (is (= "SH07-B51-GIT-INPUT-BLOB"
           (try
             (evidence/validate-m-response!
              {:content content :staging-root root
               :response-bytes
               (codec/W (assoc-in response [:payload-descriptors 0 :byte-count]
                                  0))})
             nil
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))))

(deftest new-publication-is-seven-create-new-files-with-identity-last
  (let [{:keys [root content]} (initialized-repository!)
        publication (add-worktree! root (:commit content) "publication-new")
        values (payload-values)
        stage (stage! values)
        receipt (identity-receipt content)
        result
        (evidence/publish-new-evidence!
         {:source-worktree root :publication-worktree publication
          :content content :staging-root (:root stage)
          :payload-descriptors (:descriptors stage)
          :identity-receipt receipt})
        final-root (.resolve publication evidence/evidence-relative-root)]
    (is (= {:reservation-created true
            :identity-receipt-created-last true
            :final-write-count 7
            :foreign-entry false
            :outcome :staged-new}
           (select-keys (:publication result)
                        [:reservation-created :identity-receipt-created-last
                         :final-write-count :foreign-entry :outcome])))
    (is (= (vec (sort evidence/evidence-names))
           (->> (iterator-seq (.iterator (Files/newDirectoryStream final-root)))
                (map #(.toString (.getFileName ^Path %))) sort vec)))
    (is (= receipt
           (strict-read-wire
            :response
            (Files/readAllBytes
             (.resolve final-root evidence/identity-receipt-name)))))
    (is (not (Files/exists
              (.resolve root evidence/evidence-relative-root)
              (make-array java.nio.file.LinkOption 0))))))

(deftest failed-publication-cleans-only-an-owned-prefix-and-quarantines-foreign-data
  (let [{:keys [root content]} (initialized-repository!)
        values (payload-values)
        stage (stage! values)
        receipt (identity-receipt content)
        create-var
        (ns-resolve 'gravity.tools.sh07-b51-git-evidence
                    'create-new-bytes!)
        original @create-var
        clean-publication (add-worktree! root (:commit content) "cleanup")
        clean-final (.resolve clean-publication evidence/evidence-relative-root)
        clean-calls (atom 0)]
    (testing "an unpublished exact prefix is removed"
      (is (= :publication-failed
             (with-redefs-fn
               {create-var
                (fn [path bytes]
                  (if (= 2 (swap! clean-calls inc))
                    (throw (ex-info "injected" {}))
                    (original path bytes)))}
               #(failure-reason
                 (fn []
                   (evidence/publish-new-evidence!
                    {:source-worktree root
                     :publication-worktree clean-publication
                     :content content :staging-root (:root stage)
                     :payload-descriptors (:descriptors stage)
                     :identity-receipt receipt}))))))
      (is (not (Files/exists clean-final
                             (make-array java.nio.file.LinkOption 0)))))
    (testing "a foreign entry leaves the reservation quarantined"
      (let [publication (add-worktree! root (:commit content) "quarantine")
            final-root (.resolve publication evidence/evidence-relative-root)
            calls (atom 0)]
        (is (= :publication-quarantined
               (with-redefs-fn
                 {create-var
                  (fn [path bytes]
                    (if (= 2 (swap! calls inc))
                      (do
                        (Files/write (.resolve (.getParent ^Path path)
                                               "foreign.edn")
                                     (.getBytes "foreign\n"
                                                StandardCharsets/UTF_8)
                                     (into-array OpenOption
                                                 [StandardOpenOption/CREATE_NEW
                                                  StandardOpenOption/WRITE]))
                        (throw (ex-info "injected" {})))
                      (original path bytes)))}
                 #(failure-reason
                   (fn []
                     (evidence/publish-new-evidence!
                      {:source-worktree root :publication-worktree publication
                       :content content :staging-root (:root stage)
                       :payload-descriptors (:descriptors stage)
                       :identity-receipt receipt}))))))
        (is (Files/exists (.resolve final-root "foreign.edn")
                          (make-array java.nio.file.LinkOption 0)))
        (is (not (Files/exists
                  (.resolve final-root evidence/identity-receipt-name)
                  (make-array java.nio.file.LinkOption 0))))))))
