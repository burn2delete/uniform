(ns gravity.reader.sealed-pure-integration-test
  "UNRUN normal Gravity pure-text acceptance definition. Requiring this
  namespace loads Bootstrap. Do not load it for SOURCE/MODELED checks."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.reader.pure-text :as pure]))

(defn- inputs [stem extension]
  (let [root (.getCanonicalPath (io/file "."))
        relative (str "bootstrap/clojure/fixtures/reader-extension-binding/" stem extension)
        path (.getCanonicalPath (io/file root relative))]
    {:path path :text (slurp path :encoding "UTF-8")
     :context {:project-root-id (bootstrap/reader-project-root-id root)
               :project-root-path root :project-relative-path relative}
     :reader (.getCanonicalPath (io/file root bootstrap/sh03-reader-source-relative-path))}))

(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(deftest normal-gravity-pure-text-pair
  (doseq [extension [".qst" ".gravity"]]
    (let [{:keys [path text context reader]} (inputs "normal-pure" extension)
          invocation {:reader-source-path reader
                      :authenticated-envelope-options {:source :development-current}}
          artifact (bootstrap/reader-pure-source-artifact path text context reader)
          record (first (:reader-extension-invocation-records artifact))
          call (first (:invocations record))
          view (bootstrap/c3-reader-artifact-view artifact invocation)]
      (testing path
        (is (= (pure/policy-id) (get-in artifact [:source-unit-record :extension-policy])))
        (is (= 2 (:schema-version record)))
        (is (= 'sh03-reader-read-text (:handler record)))
        (is (= (:input-seed call) (:returned-seed call)))
        (is (= "registered pure payload" (last (:parsed-semantic-values artifact))))
        (is (not (contains? artifact :pure-extension-results)))
        (is (= (:reader-extension-invocation-records artifact)
               (:reader-extension-invocation-records view)))
        (is (= (:artifact-id artifact) (:artifact-id view)))
        (is (= "C2-HASH" (:id (diagnostic #(bootstrap/c3-reader-artifact-view artifact)))))
        (doseq [[field changed]
                [[[:reader-extension-invocation-records 0 :policy] {}]
                 [[:reader-extension-invocation-records 0 :handler] 'other]
                 [[:reader-extension-invocation-records 0 :invocations 0 :input-seed :datum] {}]
                 [[:reader-extension-invocation-records 0 :invocations 0 :returned-seed :datum] {}]
                 [[:reader-extension-invocation-records 0 :invocations 0 :payload-form-id] "other"]
                 [[:reader-extension-invocation-records 0 :invocations 0 :span :byte-end] 0]
                 [[:reader-extension-invocation-records 0 :invocations 0 :invocation :handler] 'other]
                 [[:gravity-reader-boundary :plan-binding :source-content-hash] "changed"]
                 [[:source-unit-record :identity-inputs :project-root-id] "changed"]
                 [[:gravity-reader-boundary :pure-text-reconstruction-input :source-text]
                  (str text "\n\"different source\"\n")]]]
          (is (= "C2-HASH"
                 (:id (diagnostic #(bootstrap/c3-reader-artifact-view
                                     (assoc-in artifact field changed) invocation))))))
        (doseq [changed [(assoc-in artifact [:reader-extension-invocation-records 0 :invocations] [])
                         (update-in artifact [:reader-extension-invocation-records 0 :invocations] #(into % %))
                         ;; Recompute public C2 incremental, integrity and
                         ;; artifact hashes. Fresh reconstruction still refuses.
                         (let [changed (assoc-in artifact [:reader-extension-invocation-records 0 :handler] 'other)
                               hashes (bootstrap/c2-incremental-hashes
                                       (:source-unit-record changed) (:token-stream changed)
                                       (:form-tree changed) (:syntax-seed-stream changed)
                                       (:reader-extension-invocation-records changed) (:reader-diagnostics changed))
                               integrity (bootstrap/c2-reader-product-integrity-record
                                          (:source-unit-record changed) (:top-level-form-ids changed) hashes
                                          (:literal-decoding-records changed)
                                          (get-in changed [:semantic-error-deferment-record :deferred-literal-records]))
                               changed (assoc changed :incremental-reader-hashes hashes
                                                      :reader-product-integrity integrity)]
                           (assoc changed :artifact-id (bootstrap/c2-reader-artifact-id changed)))]]
          (is (= "C2-HASH"
                 (:id (diagnostic #(bootstrap/c3-reader-artifact-view changed invocation))))))))))

(deftest normal-gravity-pure-text-mixed-refusals
  (doseq [extension [".qst" ".gravity"] stem ["normal-unknown" "normal-effect"]]
    (let [{:keys [path text context reader]} (inputs stem extension)]
      (is (= "C2-EXTENSION"
             (:id (diagnostic #(bootstrap/reader-pure-source-artifact path text context reader))))))))
