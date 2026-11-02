(ns gravity.build.trace-input-syntax-receipt-test
  "DATA/IO unit controls only. No Macro, public C5 or genuine RV execution."
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.build.trace-input-syntax-receipt :as receipt])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption SecureDirectoryStream]
           [java.nio.file.attribute FileAttribute]))

(defn- entry [ordinal]
  {:event :trace-id-enter :ordinal ordinal :resolve-returns (dec ordinal)
   :stored-id "stored-data-id" :operand [{:input-syntax-id "data-only"}]
   :invocation-current? true :proof-transaction-absent? true :observed-roots-stable? true})

(defn- data-observation []
  (let [prefix [{:event :macro-enter :same-parent? true}
                (entry 1) {:event :trace-id-return :ordinal 1 :result "stored-data-id"}
                {:event :resolve-enter} {:event :resolve-return} (entry 2)]]
    {:phase :trace-control-complete :field :input-syntax-id
     :positive-events (conj prefix {:event :trace-id-return :ordinal 2 :result "stored-data-id"}
                            {:event :macro-return :report {:status :passed}})
     :public-positive-report {:status :passed :fixture :DATA}
     :changed-events (conj prefix {:event :trace-field-mutated :ordinal 2}
                           {:event :trace-id-return :ordinal 2 :result "changed-data-id"}
                           {:event :macro-return
                            :report {:status :failed :checks {:trace-replay-current? false}
                                     :failed-checks [:trace-replay-current]}})
     :changed-error (ex-info "DATA original outer refusal" {:id "C5-UNRESOLVED" :fixture :DATA})
     :reached-second-id? true :earlier-gate-refusal? false
     :before "data-before" :after "data-after" :alias-restored? true
     :callback-printer-control-implemented? false :nested-child-alias-control-implemented? false
     :no-verifier-report-substitution? true :whole-rv1-closed? false}))

(defn- data-provenance []
  {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
   :test-source-sha256 (apply str (repeat 64 "c"))
   :request-id "DATA-unit-control-not-an-execution" :evidence-kind :data-unit-control
   :selector receipt/selector})

(defn- completed []
  {:returned? true :counts {:test 1 :pass 27 :fail 0 :error 0} :error nil})

(defn- collect-data
  ([observation] (collect-data observation (completed) (data-provenance)))
  ([observation completion provenance]
   (receipt/collect (atom observation) completion provenance)))

(deftest consistent-data-remains-unadmitted-and-retains-original-references
  (let [observation (data-observation) completion (completed) provenance (data-provenance)
        collected (collect-data observation completion provenance)
        decoded (edn/read-string (String. (receipt/receipt-bytes collected) StandardCharsets/UTF_8))]
    (is (= :consistent-supplied-observation (:validation collected)))
    (is (every? true? (vals (:checks collected))))
    (is (identical? observation (:observation collected)))
    (is (identical? completion (:completion collected)))
    (is (identical? provenance (:provenance collected)))
    (is (identical? (:changed-error observation) (get-in collected [:observation :changed-error])))
    (is (= "C5-UNRESOLVED" (get-in decoded [:observation :changed-error :data :id])))
    (is (= "DATA original outer refusal" (get-in decoded [:observation :changed-error :cause])))
    (is (= (:changed-events observation) (get-in decoded [:observation :changed-events])))
    (is (= :selected-test-assertions-including-finally (:restoration-source decoded)))
    (is (= :data-unit-control (get-in decoded [:provenance :evidence-kind])))
    (is (= :constructed-unadmitted (:method-status decoded)))
    (is (false? (:genuine-execution-established? decoded)))
    (is (false? (:cache-admission? decoded)))
    (is (not (contains? decoded :cleanup-complete?)))))

(deftest absent-incomplete-and-old-observations-never-become-consistent
  (doseq [observation [nil {} {:case :live-callee :phase :complete}
                       {:phase :positive-trace-reachability :field :input-syntax-id :events []}]]
    (let [collected (collect-data observation)]
      (is (= :nonpassing (:validation collected)))
      (is (identical? observation (:observation collected)))
      (is (= observation (:observation (edn/read-string
                                       (String. (receipt/receipt-bytes collected) StandardCharsets/UTF_8))))))))

(deftest extraction-dereferences-once-and-preserves-original-test-throwable
  (let [observation (data-observation) reads (atom 0)
        source (reify clojure.lang.IDeref (deref [_] (swap! reads inc) observation))
        original (ex-info "DATA original test error" {:fixture :DATA})
        collected (receipt/collect source (assoc (completed) :error original) (data-provenance))]
    (is (= 1 @reads))
    (is (identical? observation (:observation collected)))
    (is (identical? original (get-in collected [:completion :error])))
    (is (= :nonpassing (:validation collected)))))

(deftest multiple-macro-checks-and-later-resolve-events-are-preserved
  ;; The original control constrains the Resolve count AT second ID entry,
  ;; not the entire run's later event count. This is still supplied DATA.
  (let [observation (data-observation)
        expand (fn [events]
                 (vec (concat (subvec events 0 3)
                              [{:event :macro-return :report {:status :passed}}]
                              (subvec events 3 5)
                              [{:event :macro-enter :same-parent? true}]
                              (subvec events 5)
                              [{:event :resolve-enter} {:event :resolve-return}])))
        observation (-> observation (update :positive-events expand) (update :changed-events expand))
        collected (collect-data observation)]
    (is (= :consistent-supplied-observation (:validation collected)))
    (is (identical? observation (:observation collected)))))

(deftest each-required-observation-fact-is-checked
  (doseq [[label mutate]
          [[:field #(assoc % :field :output-syntax-id)]
           [:positive-id #(assoc-in % [:positive-events 6 :result] "other")]
           [:changed-id #(assoc-in % [:changed-events 7 :result] "stored-data-id")]
           [:stored-id #(assoc-in % [:changed-events 5 :stored-id] "other")]
           [:entry-context #(assoc-in % [:changed-events 5 :observed-roots-stable?] false)]
           [:invocation #(assoc-in % [:positive-events 1 :invocation-current?] false)]
           [:transaction #(assoc-in % [:changed-events 1 :proof-transaction-absent?] false)]
           [:parent #(assoc-in % [:changed-events 0 :same-parent?] false)]
           [:resolve-count #(assoc-in % [:changed-events 5 :resolve-returns] 0)]
           [:resolve-order #(update % :changed-events (fn [events] (assoc events 4 (events 5) 5 (events 4))))]
           [:mutation #(update % :changed-events (fn [events] (vec (remove (fn [e] (= :trace-field-mutated (:event e))) events))))]
           [:duplicate-mutation #(update % :changed-events conj {:event :trace-field-mutated :ordinal 2})]
           [:mutation-order #(update % :changed-events (fn [events] (assoc events 5 (events 6) 6 (events 5))))]
           [:refusal-check #(assoc-in % [:changed-events 8 :report :checks :trace-replay-current?] true)]
           [:refusal-id #(assoc-in % [:changed-events 8 :report :failed-checks] [])]
           [:public #(assoc-in % [:public-positive-report :status] :failed)]
           [:positive-macro #(assoc-in % [:positive-events 7 :report :status] :failed)]
           [:original-error #(assoc % :changed-error {:id "C5-UNRESOLVED"})]
           [:wrong-error #(assoc % :changed-error (ex-info "DATA different" {:id "OTHER"}))]
           [:second-id #(assoc % :reached-second-id? false)]
           [:earlier-refusal #(assoc % :earlier-gate-refusal? true)]
           [:unchanged-slot #(assoc % :after (:before %))]
           [:alias #(assoc % :alias-restored? false)]
           [:verifier #(assoc % :no-verifier-report-substitution? false)]
           [:scope #(assoc % :whole-rv1-closed? true)]]]
    (testing (name label)
      (is (= :nonpassing (:validation (collect-data (mutate (data-observation)))))))))

(deftest test-failures-and-provenance-are-not-discarded
  (let [original (ex-info "DATA test/finally failed" {:restoration :failed})]
    (doseq [completion [(assoc (completed) :returned? false :error original)
                        (assoc-in (completed) [:counts :fail] 1)
                        (assoc-in (completed) [:counts :error] 1)
                        (assoc-in (completed) [:counts :pass] 0)
                        (assoc-in (completed) [:counts :test] 2)
                        (dissoc (completed) :returned?)]]
      (let [collected (collect-data (data-observation) completion (data-provenance))]
        (is (= :nonpassing (:validation collected)))
        (is (identical? completion (:completion collected)))))
    (let [collected (collect-data nil {:error original} (data-provenance))]
      (is (identical? original (get-in collected [:completion :error])))
      (is (= {:restoration :failed}
             (get-in (edn/read-string (String. (receipt/receipt-bytes collected) StandardCharsets/UTF_8))
                     [:completion :error :data]))))
    (doseq [provenance [{} (assoc (data-provenance) :source-commit "unfrozen")
                        (assoc (data-provenance) :request-id "")
                        (assoc (data-provenance) :selector 'actual-macro-live-callee-swap-after-resolve)
                        (assoc (data-provenance) :evidence-kind :genuine-pass)]]
      (is (= :nonpassing (:validation (collect-data (data-observation) (completed) provenance)))))))

(defn- with-output-directory [f]
  (let [path (Files/createTempDirectory "trace-receipt-DATA-" (make-array FileAttribute 0))]
    (try
      (with-open [directory (Files/newDirectoryStream path)]
        (is (instance? SecureDirectoryStream directory))
        (f path directory))
      (finally
        (with-open [entries (Files/newDirectoryStream path)]
          (doseq [entry entries] (Files/delete entry)))
        (Files/delete path)))))

(deftest create-new-publication-uses-owner-handle-and-keeps-directory-open
  (with-output-directory
    (fn [path directory]
      (let [collected (collect-data (data-observation)) admitted (atom [])
            output (.resolve path receipt/receipt-name)
            result (receipt/publish! directory collected #(swap! admitted conj %))
            bytes (Files/readAllBytes output)
            decoded (edn/read-string (String. bytes StandardCharsets/UTF_8))]
        (is (= [(alength bytes)] @admitted))
        (is (= (alength bytes) (:byte-count result)))
        (is (= :data-unit-control (get-in decoded [:provenance :evidence-kind])))
        (is (= :constructed-unadmitted (:method-status result)))
        (is (thrown? java.nio.file.FileAlreadyExistsException
                     (receipt/publish! directory collected (fn [_]))))
        (is (= (vec bytes) (vec (Files/readAllBytes output))))
        (is (some? (.getFileAttributeView ^SecureDirectoryStream directory
                                         java.nio.file.attribute.BasicFileAttributeView)))))))

(deftest capture-refusal-and-unserializable-data-create-nothing
  (with-output-directory
    (fn [path directory]
      (let [original (ex-info "DATA owner capture refusal" {:fixture :DATA})
            collected (collect-data (data-observation))]
        (is (identical? original (try (receipt/publish! directory collected (fn [_] (throw original)))
                                     (catch Throwable error error))))
        (is (false? (Files/exists (.resolve path receipt/receipt-name) (make-array LinkOption 0))))
        (is (thrown? RuntimeException
                     (receipt/publish! directory (assoc collected :unsupported (Object.))
                                       (fn [_] (throw (AssertionError. "must fail before admission"))))))
        (is (false? (Files/exists (.resolve path receipt/receipt-name) (make-array LinkOption 0))))))))

(deftest nonpassing-receipt-stays-nonpassing-and-symlink-is-not-followed
  (with-output-directory
    (fn [path directory]
      (let [collected (collect-data nil)
            result (receipt/publish! directory collected (fn [_]))]
        (is (= :nonpassing (:validation result)))
        (Files/delete (.resolve path receipt/receipt-name))
        (Files/createSymbolicLink (.resolve path receipt/receipt-name) (.resolve path "absent-target")
                                 (make-array FileAttribute 0))
        (is (thrown? java.nio.file.FileAlreadyExistsException
                     (receipt/publish! directory collected (fn [_]))))
        (is (false? (Files/exists (.resolve path "absent-target") (make-array LinkOption 0))))))))

(deftest no-heavy-test-or-bootstrap-namespace-is-loaded
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.self-hosting.resolution-parent-mutation-test))))
