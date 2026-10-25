(ns gravity.self-hosting.sh06-current-cohort-reconciliation-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(def observed-reader-adoption (atom nil))
(defn- path [relative]
  (.getCanonicalPath (io/file (System/getProperty "user.dir") relative)))

(deftest current-reader-reaches-original-small-c3-consumer
  ;; Current small-source composition, not whole-owner fit or migration.
  (let [reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        source (path "bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/newline-lf.gravity")
        text (slurp source :encoding "UTF-8")
        artifact (b/compiler-c3-syntax-source-artifact
                   source text (b/reader-project-context-for-source source) nil nil reader)
        c2 (:c2-reader-artifact artifact)
        boundary (:gravity-syntax-boundary artifact)
        stream (:syntax-object-stream artifact)
        revision (:reader-source-revision boundary)
        binding (b/sh03-reader-current-binding! source reader)
        authentic? #(b/c3-syntax-stream-reader-products-authentic? stream %1 %2)
        old-reader "sha256:e3f840456ac3f2df71bb5ddf894f9524d5a8df74297252bad49925d9060a9f71"
        stale (assoc revision :source-content-hash old-reader)
        replaced-c2 (assoc-in c2 [:sh03-reader-authentication :reader-source-revision] stale)
        replaced-boundary (-> boundary
                              (assoc :reader-source-revision stale)
                              (assoc-in [:reader-authentication-provenance
                                         :actual-sh03-envelope-descriptor :source-revision] stale))]
    (is (= :passed (get-in artifact [:syntax-verification-report :status])))
    (is (true? (authentic? c2 boundary)))
    (is (= b/sh03-reader-expected-source-content-hash
           (:source-content-hash binding) (:source-content-hash revision)))
    (is (= 249 (:function-count binding)))
    (is (= b/sh03-reader-expected-plan-semantic-hash (:plan-semantic-hash binding)))
    (is (not= old-reader (:source-content-hash revision)))
    (is (= stale (get-in replaced-c2 [:sh03-reader-authentication :reader-source-revision])
           (:reader-source-revision replaced-boundary)
           (get-in replaced-boundary [:reader-authentication-provenance
                                      :actual-sh03-envelope-descriptor :source-revision])))
    (is (= (get-in boundary [:reader-authentication-provenance :actual-sh03-authenticated-envelope])
           (get-in replaced-boundary [:reader-authentication-provenance :actual-sh03-authenticated-envelope])))
    (is (false? (authentic? replaced-c2 replaced-boundary)))
    (is (false? (authentic? (update c2 :sh03-reader-authentication dissoc :reader-source-revision) boundary)))
    (reset! observed-reader-adoption
            {:reader (select-keys binding [:source-byte-count :source-content-hash :plan-semantic-hash
                                          :functions-semantic-hash :function-count :entrypoint-semantic-hash
                                          :verifier-semantic-hash])
             :syntax-verification :passed
             :genuine-current-transport true :old-revision-substitution-rejected true
             :scope :one-original-small-c2-to-c3-consumer
             :nonclaims [:whole-source-fit :all-functions-invoked :migration :self-hosting :release]})))
