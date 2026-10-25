(ns gravity.self-hosting.sh15-public-namespace-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh03-reader-adapter-test]
            [gravity.self-hosting.sh15-resolved-reader-diagnostic-test]))

(def ^:private fixture-path #'gravity.self-hosting.sh03-reader-adapter-test/fixture-path)
(def ^:private write-bytes! #'gravity.self-hosting.sh03-reader-adapter-test/write-bytes!)
(def ^:private delete-tree! #'gravity.self-hosting.sh03-reader-adapter-test/delete-tree!)
(def ^:private c15 #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/c15)
(def ^:private resolve-private
  #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-resolved-reader-diagnostic-test/verify)
(defn- fixture [extension]
  (fixture-path "rejected" "namespace-clause-key-nonkeyword" (str "." extension)))
(defn- public-data [source-path]
  (try
    (bootstrap/compiler-c2-reader-file-artifact source-path)
    (throw (ex-info "Expected the actual namespace rejection" {:source-path source-path}))
    (catch clojure.lang.ExceptionInfo error (ex-data error))))
(def ^:private originals
  (delay (into {} (map #(vector % (public-data (fixture %))) ["gravity" "qst"]))))
(defn- fields [public]
  [(:rule public) (:severity public) (:stage public)
   (assoc (:primary public) :syntax-id nil) (:related public) (:origin-chain public)
   (:profile public) (:target public) (:facts public)
   (:remediation-records public) (:redactions public)])
(defn- rejected [result rule]
  (is (= :rejected (:status result)))
  (is (= rule (get-in result [:diagnostics 0 :rule]))))

(deftest actual-public-namespace-data-retains-rule-file-origin-and-owner-provenance
  (doseq [extension ["gravity" "qst"]]
    (let [public (get @originals extension) before (pr-str public)
          arguments (fields public) source-path (fixture extension)
          {:keys [request candidate]} (resolve-private source-path arguments)
          result (verify source-path arguments candidate request)]
      (is (= "C2-NS-SHAPE" (:id public)))
      (is (= :C2-NS-SHAPE (:rule public) (:rule candidate)))
      ;; The original public event is retained as private context, outside the ID.
      (is (= "L1-NS-SHAPE" (:remapped-from public)))
      (is (= :gravity/diagnostic (:artifact candidate)))
      (is (= :passed (:status result)))
      (is (= #{:file :source :start :end :byte-start :byte-end}
             (set (keys (get-in candidate [:primary :span])))))
      (is (= (:source-id public) (get-in candidate [:primary :artifact])
             (get-in candidate [:primary :span :file])
             (get-in candidate [:origin-chain 0 :source-id])))
      (is (= source-path (get-in candidate [:primary :span :source])
             (get-in candidate [:origin-chain 0 :path])))
      (doseq [[field index] [[:rule 0] [:severity 1] [:stage 2] [:primary 3]
                             [:related 4] [:origin-chain 5] [:profile 6] [:target 7]
                             [:facts 8] [:remediation 9] [:redactions 10]]]
        (is (= (nth arguments index) (get candidate field))))
      (is (= (:source-id public) (get-in request [:preimage :primary-artifact])))
      (is (= #{:rule :primary-artifact :stage :facts} (set (keys (:preimage request)))))
      (is (every? #(and (contains? candidate %) (nil? (get candidate %))) [:profile :target]))
      (is (contains? (:primary candidate) :syntax-id))
      (is (nil? (get-in candidate [:primary :syntax-id])))
      (is (= before (pr-str public)))
      (is (some #{:upstream-source-authentication} (:nonclaims result)))
      (is (some #{:public-safe-projection} (:nonclaims result))))))

(deftest private-public-span-variant-rejects-malformed-file-path-and-coordinates
  (let [arguments (fields (get @originals "gravity")) span (get-in arguments [3 :span])]
    (doseq [malformed [(assoc span :file nil) (assoc span :file :source-id)
                       (assoc span :file "sha256:bad") (assoc span :file 7)
                       (assoc span :extra true) (dissoc span :source)
                       (assoc span :source "") (assoc span :source 7)
                       (update span :byte-start inc) (update span :byte-end inc)
                       (assoc-in span [:start :line] 0) (assoc-in span [:end :char] -1)
                       (assoc-in span [:start :column-unit] :utf16)
                       (assoc-in span [:end :extra] true)
                       (-> span (assoc :byte-end 0) (assoc-in [:end :byte] 0))]]
      (let [result (c15 'build-c15-diagnostic (assoc-in arguments [3 :span] malformed))]
        (rejected result "C15-SPAN")
        (is (= :primary (get-in result [:diagnostics 0 :schema-field])))))))

(deftest private-public-source-origin-rejects-open-shapes-and-malformed-identities
  (let [arguments (fields (get @originals "gravity")) origin (get-in arguments [5 0])]
    (doseq [malformed [(assoc origin :kind :generated) (assoc origin :extra true)
                       (dissoc origin :source-id) (dissoc origin :path)
                       (assoc origin :source-id nil) (assoc origin :source-id :source-id)
                       (assoc origin :source-id "sha256:bad") (assoc origin :source-id 7)
                       (assoc origin :path "") (assoc origin :path 7)]]
      (let [result (c15 'build-c15-diagnostic (assoc-in arguments [5 0] malformed))]
        (rejected result "C15-ORIGIN")
        (is (= :origin-chain (get-in result [:diagnostics 0 :schema-field])))))))

(deftest public-transport-fresh-verification-rejects-substitution-and-excludes-wording
  (let [public (get @originals "gravity") source-path (fixture "gravity")
        arguments (fields public) {:keys [request candidate]} (resolve-private source-path arguments)
        other-fields (fields (get @originals "qst"))
        other (resolve-private (fixture "qst") other-fields)
        wording (-> public (assoc :message "Different prose" :remediation "Different prose")
                    (assoc-in [:remediation-records 0 :message] "Different guidance"))
        changed-fields (fields wording) reworded (resolve-private source-path changed-fields)]
    (is (= :passed (:status (verify source-path changed-fields (:candidate reworded) (:request reworded)))))
    (is (= (:diagnostic-id candidate) (get-in reworded [:candidate :diagnostic-id])))
    (is (not= (:diagnostic-id candidate) (get-in other [:candidate :diagnostic-id])))
    (rejected (verify source-path arguments (:candidate other) (:request other)) "C15-ID")
    (rejected (verify source-path other-fields candidate request) "C15-ID")
    (doseq [changed [(update-in candidate [:primary :span] dissoc :file)
                     (assoc-in candidate [:primary :span :file] (:source-id (get @originals "qst")))
                     (assoc-in candidate [:origin-chain 0 :path] "different/physical.gravity")
                     (assoc-in candidate [:origin-chain 0 :source-id] (:source-id (get @originals "qst")))]]
      (rejected (verify source-path arguments changed request) "C15-ID"))
    (let [replacement (assoc-in request [:preimage :facts :control] :changed)
          fresh (bootstrap/sh03-reader-canonical-digest source-path replacement)
          coherent (assoc candidate :facts (get-in replacement [:preimage :facts])
                                   :diagnostic-id-request replacement :diagnostic-id fresh)]
      (rejected (verify source-path arguments coherent replacement) "C15-ID"))
    (rejected (c15 'verify-c15-resolved-diagnostic
                    [arguments candidate request (get-in other [:candidate :diagnostic-id])]) "C15-ID")
    (is (= arguments (fields (assoc public :diagnostic-id (get-in other [:candidate :diagnostic-id])
                                          :message "Other private event wording"))))))

(deftest actual-public-physical-relocation-keeps-logical-source-identity-distinct
  (let [left (java.nio.file.Files/createTempDirectory "gravity-c15-ns-left-"
                                                    (make-array java.nio.file.attribute.FileAttribute 0))
        right (java.nio.file.Files/createTempDirectory "gravity-c15-ns-right-"
                                                     (make-array java.nio.file.attribute.FileAttribute 0))
        manifest (.getBytes "{:paths [\"src\"]}\n" java.nio.charset.StandardCharsets/UTF_8)
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file (fixture "gravity"))))
        paths [(.resolve left "src/same.gravity") (.resolve right "src/same.gravity")
               (.resolve right "src/other.gravity")]]
    (try
      (doseq [root [left right]] (write-bytes! (.resolve root "deps.edn") manifest))
      (doseq [p paths] (write-bytes! p bytes))
      (let [rows (mapv (fn [p]
                         (let [source-path (str p) public (public-data source-path)
                               arguments (fields public)
                               {:keys [candidate request]} (resolve-private source-path arguments)]
                           (is (= "C2-NS-SHAPE" (:id public)))
                           (is (= "L1-NS-SHAPE" (:remapped-from public)))
                           (is (= :passed (:status (verify source-path arguments candidate request))))
                           {:public public :candidate candidate})) paths)
            [a b c] rows]
        (is (not= (get-in a [:candidate :primary :span :source])
                  (get-in b [:candidate :primary :span :source])))
        (is (= (get-in a [:public :source-id]) (get-in b [:public :source-id])))
        (is (= (get-in a [:candidate :diagnostic-id]) (get-in b [:candidate :diagnostic-id])))
        (is (not= (get-in b [:public :source-id]) (get-in c [:public :source-id])))
        (is (not= (get-in b [:candidate :diagnostic-id]) (get-in c [:candidate :diagnostic-id]))))
      (finally (delete-tree! left) (delete-tree! right)))))
