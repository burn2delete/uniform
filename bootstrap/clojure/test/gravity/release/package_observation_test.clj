(ns gravity.release.package-observation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- diagnostic-data
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest source-observations-are-fresh-and-records-reuse-their-views
  (let [original-inventory bootstrap/p18-t02-source-inventory
        original-sha256 bootstrap/p18-file-sha256
        discoveries (atom 0)
        hashed-paths (atom [])
        source-paths (set (map :path (original-inventory)))]
    (with-redefs
      [bootstrap/p18-t02-source-inventory
       (fn []
         (swap! discoveries inc)
         (original-inventory))
       bootstrap/p18-file-sha256
       (fn [path]
         (swap! hashed-paths conj path)
         (original-sha256 path))]
      (let [first-observation (bootstrap/p18-t02-source-observation)
            second-observation (bootstrap/p18-t02-source-observation)]
        (is (= (:source-material first-observation)
               (:source-material second-observation)))
        (is (not (identical? first-observation second-observation)))
        (is (= 2 @discoveries))
        (is (= (* 2 (count source-paths))
               (count (filter source-paths @hashed-paths))))
        (is (= (into (sorted-map)
                     (map (fn [{:keys [path hash]}] [path hash]))
                     (:source-material first-observation))
               (:source-hashes first-observation)))))))

(deftest package-record-projections-do-not-rehash-an-observation
  (let [observation (bootstrap/p18-t02-source-observation)
        package-manifest (bootstrap/p18-t02-package-manifest "sha256:jar")
        dependency-record {:artifact-id "sha256:dependencies"
                           :dependencies []}
        original-sha256 bootstrap/p18-file-sha256
        source-paths (set (map :path (:source-inventory observation)))
        hashed-paths (atom [])]
    (with-redefs
      [bootstrap/p18-file-sha256
       (fn [path]
         (swap! hashed-paths conj path)
         (original-sha256 path))]
      (let [reproducible
            (bootstrap/p18-t02-reproducible-build-record
             package-manifest "sha256:jar" observation)
            provenance
            (bootstrap/p18-t02-provenance-record
             package-manifest dependency-record reproducible "sha256:jar"
             observation)
            sbom
            (bootstrap/p18-t02-sbom-record
             package-manifest dependency-record provenance observation)]
        (is (= (:source-hashes observation)
               (:source-hashes reproducible)))
        (is (= (:source-material observation)
               (:source-material provenance)
               (:source-references sbom)))))
    (is (empty? (filter source-paths @hashed-paths)))))

(deftest build-observation-enumerates-the-jar-once
  (let [original-sha256 bootstrap/p18-file-sha256
        original-entry-records bootstrap/p18-t02-jar-entry-records
        source-paths (set (map :path (bootstrap/p18-t02-source-inventory)))
        hashed-paths (atom [])
        jar-enumerations (atom 0)]
    (with-redefs
      [bootstrap/p18-file-sha256
       (fn [path]
         (swap! hashed-paths conj path)
         (original-sha256 path))
       bootstrap/p18-t02-jar-entry-records
       (fn []
         (swap! jar-enumerations inc)
         (original-entry-records))]
      (is (= bootstrap/p18-t02-jar-path
             (bootstrap/p18-t02-build-packaged-jvm-cli!))))
    (is (= 1 @jar-enumerations))
    (is (= (* 2 (count source-paths))
           (count (filter source-paths @hashed-paths))))))

(deftest build-rejects-source-drift-between-pre-and-post-observations
  (let [source-path "bootstrap/clojure/src/gravity/bootstrap.clj"
        original-source (slurp source-path)
        error-data
        (try
          (with-redefs
            [bootstrap/p18-t02-shell!
             (fn [stage _args]
               (when (= "p18-t02-javac" stage)
                 (spit source-path
                       (str original-source "\n; package observation drift\n")))
               {:exit 0 :out "" :err ""})
             bootstrap/p18-t02-jar-observation
             (fn [] {:jar-entry-records []
                     :jar-entries []
                     :jar-file-entries []})]
            (bootstrap/p18-t02-build-packaged-jvm-cli-observation!))
          nil
          (catch clojure.lang.ExceptionInfo error
            (ex-data error))
          (finally
            (spit source-path original-source)))]
    (is (= "P18T02006" (:id error-data)))
    (is (= :stable-packaged-source-inventory (:missing-fact error-data)))
    (is (not= (:source-material-before error-data)
              (:source-material-after error-data)))))

(deftest source-observation-rejects-duplicate-and-missing-members
  (let [inventory (bootstrap/p18-t02-source-inventory)
        duplicate (conj inventory (first inventory))
        missing (assoc inventory 1 (assoc (nth inventory 1) :path nil))]
    (doseq [invalid [duplicate missing]]
      (let [error-data
            (diagnostic-data
             #(bootstrap/p18-t02-source-observation invalid))]
        (is (= "P18T02006" (:id error-data)))
        (is (= :complete-packaged-source-inventory
               (:missing-fact error-data)))))))
