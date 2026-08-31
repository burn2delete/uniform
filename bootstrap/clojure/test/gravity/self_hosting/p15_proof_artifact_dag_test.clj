(ns gravity.self-hosting.p15-proof-artifact-dag-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private compiler-source
  "bootstrap/gravity/p15_s23/compiler.gravity")

(deftest fresh-context-reuses-an-immutable-artifact-once
  (let [build-count (atom 0)
        build (fn []
                (swap! build-count inc)
                {:kind :test/artifact
                 :artifact-id "sha256:test"})
        first-run
        (bootstrap/p15-s23-with-artifact-build-context
         (fn []
           (let [left (bootstrap/p15-s23-context-artifact
                       :test compiler-source build)
                 right (bootstrap/p15-s23-context-artifact
                        :test compiler-source build)]
             {:same? (identical? left right)
              :value left})))
        second-run
        (bootstrap/p15-s23-with-artifact-build-context
         (fn []
           (bootstrap/p15-s23-context-artifact
            :test compiler-source build)))]
    (is (:same? first-run))
    (is (= {:kind :test/artifact :artifact-id "sha256:test"}
           (:value first-run)))
    (is (= {:kind :test/artifact :artifact-id "sha256:test"}
           second-run))
    ;; Each fresh authoritative request starts empty; no result survives the
    ;; dynamic context boundary.
    (is (= 2 @build-count))))

(deftest source-record-is-shared-only-within-a-fresh-context
  (let [within
        (bootstrap/p15-s23-with-artifact-build-context
         (fn []
           (let [left (bootstrap/p15-s23-compiler-source-form-record
                       compiler-source)
                 right (bootstrap/p15-s23-compiler-source-form-record
                        compiler-source)]
             {:same? (identical? left right)
              :hash (bootstrap/sha256-hex (:source-text left))})))
        outside-left (bootstrap/p15-s23-compiler-source-form-record
                      compiler-source)
        outside-right (bootstrap/p15-s23-compiler-source-form-record
                       compiler-source)]
    (is (:same? within))
    (is (= (:hash within)
           (bootstrap/sha256-hex (:source-text outside-left))))
    (is (not (identical? outside-left outside-right)))))

(deftest pipeline-child-is-built-once-and-identity-is-unchanged
  (let [original bootstrap/p15-s23-compiler-source-inventory-source-artifact
        inventory-calls (atom 0)
        [left right]
        (with-redefs
         [bootstrap/p15-s23-compiler-source-inventory-source-artifact
          (fn [path]
            (swap! inventory-calls inc)
            (original path))]
          (bootstrap/p15-s23-with-artifact-build-context
           (fn []
             [(bootstrap/p15-s23-compiler-pipeline-manifest-source-artifact
               compiler-source)
              (bootstrap/p15-s23-compiler-pipeline-manifest-source-artifact
               compiler-source)])))]
    (testing "the same immutable child is shared by sibling requests"
      (is (identical? left right))
      (is (= 1 @inventory-calls))
      (is (= (:artifact-id left) (:artifact-id right)))
      (is (= (:manifest-id left) (:manifest-id right)))
      (is (= (:capability-based-proof left)
             (:capability-based-proof right))))))

(deftest source-content-change-cannot-reuse-same-metadata-key
  (let [file (java.io.File/createTempFile "p15-s23-context-" ".gravity")
        path (.getPath file)
        build-count (atom 0)
        build (fn []
                (swap! build-count inc)
                (slurp path))]
    (try
      (spit file "same")
      (let [mtime (.lastModified file)]
        (bootstrap/p15-s23-with-artifact-build-context
         (fn []
           (let [before (bootstrap/p15-s23-context-artifact
                         :mutation path build)]
             (spit file "edit")
             ;; Preserve the timestamp and byte length to exercise the
             ;; historical metadata-only collision case.
             (.setLastModified file mtime)
             (let [after (bootstrap/p15-s23-context-artifact
                          :mutation path build)]
               (is (= "same" before))
               (is (= "edit" after))
               (is (= 2 @build-count)))))))
      (finally
        (.delete file)))))

(deftest concurrent-siblings-build-an-artifact-once
  (let [build-count (atom 0)
        started (promise)
        release (promise)
        build (fn []
                (swap! build-count inc)
                (deliver started true)
                @release
                {:kind :test/concurrent})]
    (bootstrap/p15-s23-with-artifact-build-context
     (fn []
       (let [left (future (bootstrap/p15-s23-context-artifact
                           :concurrent compiler-source build))
             right (future (bootstrap/p15-s23-context-artifact
                            :concurrent compiler-source build))
             _ @started
             _ (deliver release true)
             left-value @left
             right-value @right]
         (is (identical? left-value right-value))
         (is (= 1 @build-count)))))))
