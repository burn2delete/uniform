(ns gravity.self-hosting.sh07-conformance-table-test
  "Focused static checks for the reviewed SH-07 fixture table.

  These tests census and authenticate the compatibility projection only.  They
  intentionally do not invoke a compiler, a process runner, a cache, or the
  authoritative SH-07 proof contract."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-conformance-table :as table]))

(defn- error-id
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(defn- private-fn
  [name]
  (or (some-> (ns-resolve 'gravity.self-hosting.sh07-conformance-table name)
              deref)
      (throw (ex-info "Conformance probe helper is absent"
                      {:id "SH07-CONFORMANCE-TEST-HELPER" :name name}))))

(defn- delete-tree!
  [path]
  (when (java.nio.file.Files/exists path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                        path
                        (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- fixture-parent
  [root]
  (let [parent (.resolve root "bootstrap/clojure/fixtures/self-hosting")]
    (java.nio.file.Files/createDirectories parent
                                            (make-array java.nio.file.attribute.FileAttribute 0))
    parent))

(defn- with-probe-root
  [root operation]
  (with-redefs-fn
    {(ns-resolve 'gravity.self-hosting.sh07-conformance-table 'root-path)
     (fn [] root)}
    operation))

(deftest reviewed-table-has-exact-scope
  (let [value (table/table)]
    (is (= table/table-schema (:schema value)))
    (is (= 1 (:version value)))
    (is (= table/expected-suite-ids (:suite-order value)))
    (is (= 14 (count (:suites value))))
    (is (= {:suite-count 14
            :accepted-count 71
            :rejected-count 84
            :pair-count 155
            :source-of-truth :rows}
           (:coverage value)))
    (is (= :non-authoritative (:authority value)))
    (is (false? (:authoritative? value)))
    (is (false? (:complete? value)))
    (is (every? #(= table/expected-module-id (:module-id %))
                (:suites value)))))

(deftest authenticated-pairs-match-census
  (let [value (table/table)
        all-rows (vec (table/rows value))
        accepted (filter #(= :accepted (:status %)) all-rows)
        rejected (filter #(= :rejected (:status %)) all-rows)]
    ;; table/table performs the byte-identity, source-declaration, ownership,
    ;; and oracle checks; these assertions make its exact census explicit.
    (is (= (get-in value [:coverage :pair-count]) (count all-rows)))
    (is (= (count all-rows) (count (set (map :id all-rows)))))
    (is (= (get-in value [:coverage :accepted-count]) (count accepted)))
    (is (= (get-in value [:coverage :rejected-count]) (count rejected)))
    (is (every? #(and (= (str (:basename %) ".gravity") (:gravity %))
                       (= (str (:basename %) ".qst") (:qst %)))
                all-rows))
    (is (every? #(and (keyword? (:profile %))
                       (keyword? (:target %)))
                       all-rows))))

(deftest fixture-pair-binding-exposes-bounded-content-identity
  (let [binding (table/fixture-pair-binding
                 (table/table) "sh07-b15/keyword-lookup-zero-arity")]
    (is (= #{:schema :suite-id :row-id :status :sha256 :bytes :authenticated?}
           (set (keys binding))))
    (is (= "sh07-b15" (:suite-id binding)))
    (is (= "sh07-b15/keyword-lookup-zero-arity" (:row-id binding)))
    (is (= :rejected (:status binding)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:sha256 binding)))
    (is (<= 1 (:bytes binding) table/maximum-fixture-peer-bytes))
    (is (true? (:authenticated? binding)))
    (is (= "SH07-CONFORMANCE-FIXTURE-ROW"
           (error-id #(table/fixture-pair-binding
                       (table/table) "sh07-b15/not-reviewed"))))))

(deftest b13-static-fixture-census-is-table-driven
  (let [census (table/fixture-census "sh07-b13")
        accepted (:accepted census)
        rejected (:rejected census)
        required #{"deterministic-top-level-fragments"
                   "cross-fragment-reference"
                   "alias-root-aligned-fragments"}]
    (is (= #{:suite-id :accepted :rejected :directory-accepted
             :directory-rejected :exact-directory? :pairs}
           (set (keys census))))
    (is (seq accepted))
    (is (seq rejected))
    (is (= accepted (:directory-accepted census)))
    (is (= rejected (:directory-rejected census)))
    (is (true? (:exact-directory? census)))
    (is (= 5 (count (:pairs census))))
    (is (every? :authenticated? (:pairs census)))
    (is (= required accepted))
    (is (= #{"fragment-root-form-bound" "unsupported-core-form"}
           rejected))
    (is (= "SH07-CONFORMANCE-SUITE"
           (error-id #(table/fixture-census "unreviewed"))))))

(deftest manifest-coverage-is-derived-from-rows
  (let [value (table/table)]
    (is (= (:coverage value)
           (table/coverage-from-rows (:suites value))))))

(deftest static-validation-fails-closed
  (let [value (table/table)]
    (testing "unknown fields are rejected"
      (is (= "SH07-CONFORMANCE-TABLE-KEYS"
             (error-id #(table/validate-table (assoc value :unexpected true))))))
    (testing "unsafe fixture names are rejected"
      (is (= "SH07-CONFORMANCE-BASENAME"
             (error-id #(table/validate-table
                         (assoc-in value [:suites 0 :accepted 0 :basename]
                                   "../escape"))))))
    (testing "legacy directory parity is explicit"
      (is (= "SH07-CONFORMANCE-LEGACY-BOOLEAN"
             (error-id #(table/validate-table
                         (assoc-in value [:suites 0 :legacy :directory-parity?]
                                   "true"))))))))

(deftest preserved-bespoke-boundaries-remain-nonauthoritative
  (let [value (table/table)
        rows (vec (table/rows value))
        callback-result (table/run-row (first rows)
                                       (fn [row] {:seen (:id row)}))]
    (is (= 14 (count (filter seq (map :bespoke-boundaries (:suites value))))))
    (is (seq (:preserved-bespoke value)))
    (is (seq (:nonclaims value)))
    (is (= :non-authoritative (:authority callback-result)))
    (is (false? (:authoritative? callback-result)))
    (is (= :callback-result (:status callback-result)))
    (is (= (:id (first rows)) (:row-id callback-result)))
    (is (= {:seen (:id (first rows))} (:result callback-result)))))

(deftest fixture-root-rejects-symlink-and-symlink-component
  (let [root (java.nio.file.Files/createTempDirectory
              "sh07-conformance-root-probe"
              (make-array java.nio.file.attribute.FileAttribute 0))
        parent (fixture-parent root)
        outside (java.nio.file.Files/createTempDirectory
                 "sh07-conformance-outside-probe"
                 (make-array java.nio.file.attribute.FileAttribute 0))
        root-link (.resolve parent "root-link")
        safe (.resolve parent "safe")
        component-link (.resolve safe "component-link")
        checked (private-fn 'fixture-root-path!)]
    (try
      (java.nio.file.Files/createSymbolicLink
       root-link outside (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/createDirectories safe
                                              (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/createSymbolicLink
       component-link outside (make-array java.nio.file.attribute.FileAttribute 0))
      (with-probe-root root
        #(do
           (is (= "SH07-CONFORMANCE-FIXTURE-SYMLINK"
                  (error-id (fn []
                              (checked
                               "bootstrap/clojure/fixtures/self-hosting/root-link"
                               "probe-root")))))
           (is (= "SH07-CONFORMANCE-FIXTURE-SYMLINK"
                  (error-id (fn []
                              (checked
                               "bootstrap/clojure/fixtures/self-hosting/safe/component-link"
                               "probe-component")))))))
      (finally
        (delete-tree! root)
        (delete-tree! outside)))))

(deftest fixture-census-rejects-one-sided-peers
  (let [root (java.nio.file.Files/createTempDirectory
              "sh07-conformance-census-probe"
              (make-array java.nio.file.attribute.FileAttribute 0))
        family (.resolve (fixture-parent root) "accepted")
        census (private-fn 'fixture-files)]
    (try
      (java.nio.file.Files/createDirectories family
                                              (make-array java.nio.file.attribute.FileAttribute 0))
      (spit (.toFile (.resolve family "orphan.gravity")) "(orphan)")
      (is (= "SH07-CONFORMANCE-FIXTURE-PAIR"
             (error-id #(census (fixture-parent root) "accepted" "probe-census"))))
      (finally
        (delete-tree! root)))))

(deftest fixture-pair-validation-is-byte-bounded
  (let [root (java.nio.file.Files/createTempDirectory
              "sh07-conformance-bound-probe"
              (make-array java.nio.file.attribute.FileAttribute 0))
        family (.resolve (fixture-parent root) "probe/accepted")
        validate-pair (private-fn 'validate-pair!)
        oversized (byte-array (inc table/maximum-fixture-peer-bytes))
        suite {:id "probe"
               :fixture-root "bootstrap/clojure/fixtures/self-hosting/probe"}
        row {:id "probe/oversized" :basename "oversized"
             :gravity "oversized.gravity" :qst "oversized.qst"}]
    (try
      (java.nio.file.Files/createDirectories
       family (make-array java.nio.file.attribute.FileAttribute 0))
      (doseq [name ["oversized.gravity" "oversized.qst"]]
        (java.nio.file.Files/write (.resolve family name) oversized
                                   (make-array java.nio.file.OpenOption 0)))
      (with-probe-root root
        #(is (= "SH07-CONFORMANCE-FIXTURE-BOUND"
                (error-id (fn [] (validate-pair suite :accepted row))))))
      (finally
        (delete-tree! root)))))
