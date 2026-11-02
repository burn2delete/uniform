(ns pr554-frozen-source-view-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "tools/pr554_frozen_source_view.clj"))
(require '[pr554-frozen-source-view :as view])

(def frozen (edn/read-string (slurp view/output-path)))
(def expected (view/build))

(defn rejects? [f]
  (try (f) false (catch clojure.lang.ExceptionInfo _ true)))

(deftest frozen-source-identities-and-family-accounting
  (is (view/check-static! frozen))
  (is (= 84 (:selected-entry-count frozen)))
  (is (= {:source-decisions-and-historical-mappings 54
          :conservation-inventory 1
          :qualified-b6-receipts 7
          :qualified-c18-receipts 13
          :domain-gap-context 2
          :platform-task-interface-reconciliation 2
          :read-only-census-and-manifest-input 1
          :read-only-census-tooling 4}
         (:families frozen)))
  (is (= 84 (:verified-count (view/verify-source! frozen))))
  (is (= 28785 (get-in (first (filter #(= :conservation-inventory (:family %))
                                     (:entries frozen)))
                       [:summary :counts :facets])))
  (is (= 70 (get-in (first (filter #(= "specs/requirement-reconciliation/domain-gap-completion.edn"
                                      (:path %)) (:entries frozen)))
                    [:summary :counts :records])))
  (is (= #{"specs/requirement-reconciliation/source-dispositions.edn"
           "specs/requirement-reconciliation/task-baseline.edn"}
         (set (map :path (:historical-manifest-inputs frozen)))))
  (is (= {:original-documents 240 :extension-sources 5
          :source-facets 29177 :source-dispositions 10096 :requirements 1231}
         (get-in frozen [:current-canonical-comparison :counts])))
  (is (false? (get-in frozen [:current-canonical-comparison :state :denominator-ready?])))
  (is (= :absent (:current-main-state (first (filter #(= "specs/requirement-reconciliation/task-baseline.edn"
                                                         (:path %))
                                                   (:historical-manifest-inputs frozen))))))
  (let [receipt (first (filter #(= "specs/requirement-reconciliation/platform-task-interfaces.edn"
                                  (:path %)) (:entries frozen)))
        evidence (first (filter #(= "specs/requirement-reconciliation/evidence.md"
                                   (:path %)) (:entries frozen)))]
    (is (= 11 (get-in receipt [:summary :source-binding-count])))
    (is (= view/source-head (get-in receipt [:source :commit])))
    (is (= view/source-tree (get-in receipt [:source :tree])))
    (is (= {:start-byte 4172 :end-byte 4466 :start-line 79 :end-line 79}
           (select-keys (get-in receipt [:summary :source-bindings 0 :source])
                        [:start-byte :end-byte :start-line :end-line])))
    (is (= {:old-file-recovered-by-removing-interface-insertion true
            :original-target-records 4
            :architecture-decisions 4
            :conditional-product-outputs 7
            :old-scope-and-history :byte-identical
            :all-new-interfaces :open}
           (get-in receipt [:summary :conservation])))
    (is (= {:interface-records 11 :architecture-decision-rows 4
            :conditional-product-output-rows 7
            :platform-target-conformance-records 4
            :original-task-file-recovery true}
           (get-in evidence [:summary :conservation])))
    (is (= {:start-byte 17296 :end-byte 18011 :start-line 287 :end-line 296}
           (get-in evidence [:summary :span])))))

(deftest historical-view-has-explicit-boundaries
  (is (contains? (set (:nonclaims frozen)) :not-current-canonical-source))
  (is (contains? (set (:nonclaims frozen)) :not-requirement-denominator))
  (is (contains? (set (:nonclaims frozen)) :no-blind-hash-refresh))
  (is (contains? (set (:nonclaims frozen)) :no-denied-action-retry))
  (is (every? #(not= :same (get-in % [:current-main :state]))
              (:entries frozen))))

(deftest every-derived-identity-and-path-mutation-fails-closed
  (is (= {:source-objects-available? true :verified-count 84 :entry-count 84}
         (select-keys (view/check-against! frozen expected)
                      [:source-objects-available? :verified-count :entry-count])))
  (is (rejects? #(with-redefs [view/try-git-tree (constantly nil)]
                   (view/verify-source! frozen))))
  (is (rejects? #(with-redefs [view/git-show (fn [_ _] nil)]
                   (view/verify-source! frozen))))
  (let [first-entry (first (:entries frozen))
        mutations
        [(assoc-in frozen [:source :head] "0000000000000000000000000000000000000000")
         (assoc-in frozen [:source :tree] "0000000000000000000000000000000000000000")
         (assoc-in frozen [:comparison :current-main] "0000000000000000000000000000000000000000")
         (assoc frozen :selected-entry-count 81)
         (assoc-in frozen [:entries 0 :change-status] :m)
         (assoc-in frozen [:entries 0 :path] "specs/requirement-reconciliation/platform-task-interfaces.edn")
         (assoc-in frozen [:entries 0 :source :sha256]
                   "0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in frozen [:entries 0 :source :blob]
                   "0000000000000000000000000000000000000000")
         (assoc-in frozen [:entries 0 :source :bytes] 1)
         (assoc-in frozen [:entries 0 :current-main :sha256]
                   "0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in frozen [:entries 0 :current-main :blob]
                   "0000000000000000000000000000000000000000")
         (assoc-in frozen [:entries 0 :current-main :bytes] 1)
         (assoc-in frozen [:entries 0 :summary :fields :kind] :invented)
         (assoc-in frozen [:families :invented] 1)
         (assoc-in frozen [:value-map 0 :value] "invented")
         (assoc-in frozen [:historical-manifest-inputs 0 :source-sha256]
                   "0000000000000000000000000000000000000000000000000000000000000000")
         (assoc-in frozen [:current-canonical-comparison :counts :requirements] 0)
         (assoc-in frozen [:reproduction :check] "false")
         (update frozen :entries conj (assoc first-entry
                                        :path "specs/requirement-reconciliation/platform-task-interfaces.edn"
                                        :source (assoc (:source first-entry)
                                                       :sha256 "0000000000000000000000000000000000000000000000000000000000000000"
                                                       :blob "0000000000000000000000000000000000000000")))]
        ]
    (doseq [mutated mutations]
      (is (rejects? #(view/check-against! mutated expected))))))

(deftest frozen-index-output-rejects-symlink-targets-and-parents
  (let [temp (.toFile (java.nio.file.Files/createTempDirectory
                       "pr554-output-boundary-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        root (io/file temp "checkout")
        output (io/file root view/output-path)
        sentinel (io/file temp "outside.edn")
        parent (.getParentFile output)
        outside-parent (io/file temp "outside-parent")]
    (try
      (io/make-parents output)
      (.mkdirs outside-parent)
      (spit sentinel "sentinel")
      (spit output "original")
      (is (= (.toRealPath (.toPath output) (make-array java.nio.file.LinkOption 0))
             (view/safe-output-path-from-root! (.toPath root) view/output-path)))
      (java.nio.file.Files/delete (.toPath output))
      (java.nio.file.Files/createSymbolicLink (.toPath output) (.toPath sentinel)
                                              (make-array java.nio.file.attribute.FileAttribute 0))
      (is (rejects? #(view/safe-output-path-from-root! (.toPath root) view/output-path)))
      (java.nio.file.Files/delete (.toPath output))
      (java.nio.file.Files/delete (.toPath parent))
      (java.nio.file.Files/createSymbolicLink (.toPath parent) (.toPath outside-parent)
                                              (make-array java.nio.file.attribute.FileAttribute 0))
      (is (rejects? #(view/safe-output-path-from-root! (.toPath root) view/output-path)))
      (is (= "sentinel" (slurp sentinel)))
      (finally
        (doseq [file (reverse (file-seq temp))]
          (io/delete-file file true))))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result)))
    (System/exit 1)))
