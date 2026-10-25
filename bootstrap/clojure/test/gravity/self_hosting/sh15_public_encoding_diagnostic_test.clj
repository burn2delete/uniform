(ns gravity.self-hosting.sh15-public-encoding-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-public-namespace-diagnostic-test]))

(def ^:private fixture-path #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/fixture-path)
(def ^:private public-data #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/public-data)
(def ^:private fields #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/fields)
(def ^:private c15 #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/c15)
(def ^:private resolve-private #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/resolve-private)
(def ^:private verify #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/verify)
(def ^:private write-bytes! #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/write-bytes!)
(def ^:private delete-tree! #'gravity.self-hosting.sh15-public-namespace-diagnostic-test/delete-tree!)
(defn- fixture [extension]
  (fixture-path "rejected" "encoding-invalid-utf8" (str "." extension)))
(def ^:private originals
  (delay
    (into {}
          (map (fn [extension]
                 (let [public (public-data (fixture extension))]
                   ;; Retain actual kind/range values during the required run.
                   (prn {:actual-public-encoding-input
                         {:extension extension :primary (:primary public)
                          :origin-chain (:origin-chain public) :source-id (:source-id public)
                          :remapped-from (:remapped-from public) :facts (:facts public)}})
                   [extension public])) ["gravity" "qst"]))))
(defn- rejected [result rule]
  (is (= :rejected (:status result)))
  (is (= rule (get-in result [:diagnostics 0 :rule]))))

(deftest actual-public-encoding-retains-captured-byte-evidence-without-decoded-points
  (let [rows
        (mapv
         (fn [extension]
           (let [source-path (fixture extension) public (get @originals extension)
                 before (pr-str public) arguments (fields public)
                 {:keys [request candidate]} (resolve-private source-path arguments)
                 bytes (java.nio.file.Files/readAllBytes (.toPath (io/file source-path)))
                 byte-hash (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
                 result (verify source-path arguments candidate request)
                 span (get-in candidate [:primary :span])]
             (is (= "C2-ENCODING" (:id public)))
             (is (= :C2-ENCODING (:rule public) (:rule candidate)))
             (is (= "L1-SOURCE-ENCODING" (:remapped-from public)))
             (is (= :passed (:status result)))
             (is (= #{:file :source :byte-start :byte-end} (set (keys span))))
             (is (not (contains? span :start)))
             (is (not (contains? span :end)))
             (is (= byte-hash (:source-id public) (:file span)
                    (get-in candidate [:primary :artifact]) (get-in candidate [:facts :bytes-hash])))
             (is (= [{:kind :source-bytes :path source-path}] (:origin-chain candidate)))
             (is (= :utf-8 (get-in candidate [:facts :declared-encoding])))
             (is (pos? (get-in candidate [:facts :malformed-input-length])))
             (is (<= 0 (:byte-start span) (:byte-end span) (alength bytes)))
             (doseq [[field index] [[:rule 0] [:severity 1] [:stage 2] [:primary 3]
                                    [:related 4] [:origin-chain 5] [:profile 6] [:target 7]
                                    [:facts 8] [:remediation 9] [:redactions 10]]]
               (is (= (nth arguments index) (get candidate field))))
             (is (every? #(and (contains? candidate %) (nil? (get candidate %))) [:profile :target]))
             (is (contains? (:primary candidate) :syntax-id))
             (is (nil? (get-in candidate [:primary :syntax-id])))
             (is (= before (pr-str public)))
             (is (some #{:upstream-source-authentication} (:nonclaims result)))
             (let [view (c15 'sh15-render-bounded-diagnostic-text [candidate])]
               (is (= :accepted (:status view)))
               (is (= :bounded-ascii-bytes-v1 (:rendering-version view)))
               (is (string? (:text view))))
             candidate)) ["gravity" "qst"])]
    (is (= (:diagnostic-id (first rows)) (:diagnostic-id (second rows))))))

(deftest byte-only-span-rejects-open-shapes-malformed-hashes-and-offsets
  (let [arguments (fields (get @originals "gravity")) span (get-in arguments [3 :span])]
    (doseq [malformed [(dissoc span :file) (dissoc span :source)
                       (assoc span :file nil) (assoc span :file "sha256:bad")
                       (assoc span :file :source-id) (assoc span :source "")
                       (assoc span :source 7) (assoc span :start nil) (assoc span :end nil)
                       (assoc span :byte-start -1) (assoc span :byte-end -1)
                       (assoc span :byte-start "0")
                       (assoc span :byte-start 2 :byte-end 1)
                       (assoc span :byte-end 2147483648) (assoc span :extra true)]]
      (let [result (c15 'build-c15-diagnostic (assoc-in arguments [3 :span] malformed))]
        (rejected result "C15-SPAN")
        (is (= :primary (get-in result [:diagnostics 0 :schema-field])))))
    (let [result (c15 'build-c15-diagnostic (assoc-in arguments [3 :span :byte-end] 1.5))]
      (rejected result "C15-SCHEMA")
      (is (= :original-fields (get-in result [:diagnostics 0 :schema-field]))))))

(deftest byte-origin-retains-the-observed-kind-and-rejects-other-two-key-forms
  (let [arguments (fields (get @originals "gravity")) origin (get-in arguments [5 0])]
    (doseq [malformed [(assoc origin :kind :source) (assoc origin :kind :generated)
                       (assoc origin :kind "source-bytes") (dissoc origin :kind)
                       (dissoc origin :path) (assoc origin :path "") (assoc origin :path 7)
                       (assoc origin :source-id (get-in arguments [3 :artifact]))
                       (assoc origin :extra true)]]
      (let [result (c15 'build-c15-diagnostic (assoc-in arguments [5 0] malformed))]
        (rejected result "C15-ORIGIN")
        (is (= :origin-chain (get-in result [:diagnostics 0 :schema-field])))))))

(deftest byte-transport-fresh-verification-rejects-loss-tamper-and-coherent-rehash
  (let [public (get @originals "gravity") source-path (fixture "gravity")
        arguments (fields public) {:keys [candidate request]} (resolve-private source-path arguments)
        other (resolve-private (fixture "qst") (fields (get @originals "qst")))
        wording (-> public (assoc :message "Different prose" :remediation "Different prose")
                    (assoc-in [:remediation-records 0 :message] "Different guidance"))
        changed-fields (fields wording) reworded (resolve-private source-path changed-fields)]
    (is (= :passed (:status (verify source-path changed-fields (:candidate reworded) (:request reworded)))))
    (is (= (:diagnostic-id candidate) (get-in reworded [:candidate :diagnostic-id])
           (get-in other [:candidate :diagnostic-id])))
    (rejected (verify source-path arguments (:candidate other) (:request other)) "C15-ID")
    (doseq [changed [(update-in candidate [:primary :span] dissoc :file)
                     (update-in candidate [:primary :span :byte-end] inc)
                     (assoc-in candidate [:origin-chain 0 :kind] :source)
                     (assoc-in candidate [:primary :span :start] {:line 1 :column 1})]]
      (rejected (verify source-path arguments changed request) "C15-ID"))
    (let [replacement (update-in request [:preimage :facts :malformed-input-length] inc)
          fresh (bootstrap/sh03-reader-canonical-digest source-path replacement)
          coherent (assoc candidate :facts (get-in replacement [:preimage :facts])
                                   :diagnostic-id-request replacement :diagnostic-id fresh)]
      (rejected (verify source-path arguments coherent replacement) "C15-ID"))))

(deftest same-undecodable-bytes-keep-byte-hash-identity-across-physical-and-logical-paths
  (let [left (java.nio.file.Files/createTempDirectory "gravity-c15-bytes-left-"
                                                    (make-array java.nio.file.attribute.FileAttribute 0))
        right (java.nio.file.Files/createTempDirectory "gravity-c15-bytes-right-"
                                                     (make-array java.nio.file.attribute.FileAttribute 0))
        manifest (.getBytes "{:paths [\"src\"]}\n" java.nio.charset.StandardCharsets/UTF_8)
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file (fixture "gravity"))))
        changed (java.util.Arrays/copyOf bytes (inc (alength bytes)))
        paths [(.resolve left "src/same.gravity") (.resolve right "src/same.gravity")
               (.resolve right "src/other.gravity") (.resolve right "src/changed.gravity")]]
    (aset-byte changed (alength bytes) (byte 32))
    (try
      (doseq [root [left right]] (write-bytes! (.resolve root "deps.edn") manifest))
      (doseq [p (take 3 paths)] (write-bytes! p bytes))
      (write-bytes! (last paths) changed)
      (let [rows (mapv (fn [p]
                         (let [source-path (str p) public (public-data source-path)
                               arguments (fields public)
                               {:keys [candidate request]} (resolve-private source-path arguments)]
                           (is (= "C2-ENCODING" (:id public)))
                           (is (= :passed (:status (verify source-path arguments candidate request))))
                           candidate)) paths)]
        (is (apply distinct? (map #(get-in % [:primary :span :source]) rows)))
        (is (apply = (map #(get-in % [:primary :artifact]) (take 3 rows))))
        (is (apply = (map :diagnostic-id (take 3 rows))))
        (is (not= (:diagnostic-id (first rows)) (:diagnostic-id (last rows)))))
      (finally (delete-tree! left) (delete-tree! right)))))
