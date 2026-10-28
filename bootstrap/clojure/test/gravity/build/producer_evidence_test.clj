(ns gravity.build.producer-evidence-test
  (:refer-clojure :exclude [bytes])
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.build.producer-evidence :as evidence]
            [gravity.digest :as digest]))

(defn- bytes [value] (.getBytes (pr-str value) "UTF-8"))
(defn- hash-bytes [value] (str "sha256:" (digest/sha256-bytes-hex value)))
(defn- hash-value [value] (hash-bytes (bytes value)))
(def ^:private h (hash-value :synthetic-not-execution))
(def ^:private contract-bytes
  (.getBytes (slurp "contracts/stage0-incremental-test-dependencies.edn") "UTF-8"))
(def ^:private closure
  (->> (:components (edn/read-string (String. contract-bytes "UTF-8")))
       (mapcat :tests) (filter #(= 'gravity.c2-artifact-identity-test (:namespace %)))
       first :cache-closure))

(defn- fixture []
  (let [source {:commit (apply str (repeat 40 "a"))
                :tree (apply str (repeat 40 "b")) :repository-identity h :clean? true}
        fingerprints (into (sorted-map)
                           (map #(vector % h)
                                [:basis :basis-file :configuration-sources
                                 :jvm-input-arguments :environment :system-properties]))
        runtime {:id :actual-child-runtime-v1 :sha256 (hash-value fingerprints)
                 :fingerprints fingerprints}
        material {:source source
                 :inputs (mapv #(assoc % :sha256
                                      (if (= "contracts/stage0-incremental-test-dependencies.edn"
                                             (:path %))
                                        (hash-bytes contract-bytes) h)) (:inputs closure))
                 :inventories (mapv #(assoc % :paths []) (:inventories closure))
                 :shadow-inputs [{:path "bootstrap/clojure/src/data_readers.clj"
                                  :role :shadow :sha256 h}]
                 :classpath-identities [{:path "classpath/entry-0000" :sha256 h}
                                        {:path "classpath/entry-0001" :sha256 (hash-value :second)}]
                 :child-runtime-identity runtime
                 :runtime-tool-inputs [{:id :java :sha256 h}]}
        consumed (hash-value (array-map
                              :schema (:schema closure)
                              :dependency-contract-schema
                              :gravity/stage0-incremental-test-dependencies-v2
                              :inputs (:inputs material)
                              :shadow-inputs (:shadow-inputs material)
                              :inventories (:inventories material)))
        binding (assoc material :consumed-input-identity consumed)
        output {:text "" :bytes 0 :observed-bytes 0 :limit-bytes 1024 :truncated? false}
        summary {:test 1 :pass 2 :fail 0 :error 0}
        report (merge {:schema :gravity/self-hosting-test-report-v2
                       :authority :non-authoritative :authoritative? false
                       :status :passed :exit-code 0
                       :child-jvm-launched? true :tests-executed? true :child-cache-hit? false
                       :child-cache-receipt
                       {:decision :fresh :cacheable? false :stored? false
                        :producer-executed? true :reason :unreviewed-or-fresh-test-policy}
                       :namespaces ['gravity.c2-artifact-identity-test]
                       :skipped-namespaces [] :fail-fast? false :summary summary
                       :namespace-results [(merge summary
                                                  {:namespace 'gravity.c2-artifact-identity-test
                                                   :attempted? true :status :passed :exit-code 0
                                                   :elapsed-ms 1 :stdout output :stderr output})]}
                      summary)
        capture {:schema :gravity/reader-producer-capture-v1
                 :binding-before binding :binding-after binding
                 :applying-source source
                 :producer-provenance {:revision (select-keys source [:commit :tree])
                                       :repository-snapshot {:repository-identity h}
                                       :consumed-input-identity consumed
                                       :classpath-identities (:classpath-identities binding)
                                       :child-runtime-identity runtime}
                 :terminal {:exit-code 0 :natural-exit? true :timed-out? false
                            :stdout-complete? true :stderr-complete? true
                            :cleanup-complete? true :root-dead? true}
                 :report-edn (pr-str report)}]
    {:method {:schema :gravity/reader-producer-method-v1
              :contract-sha256 (hash-bytes contract-bytes)
              :capture-sha256 (hash-value capture) :binding binding}
     :capture capture :report report}))

(defn- check [{:keys [method capture]}]
  (let [method (assoc method :capture-sha256 (hash-value capture))]
    (evidence/verify-capture! (hash-value method) (bytes method) contract-bytes (bytes capture))))

(defn- field [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (:field (ex-data error)))))

(deftest retained-synthetic-capture-is-consistent-without-execution
  (let [result (check (fixture))]
    (is (= :consistent (:status result)))
    (is (false? (:producer-executed-by-driver? result)))
    (is (false? (:real-evidence-accepted? result)))
    (is (false? (:cache-admission? result)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest external-digests-reject-tampering-before-reading-capture
  (let [{:keys [method capture]} (fixture)]
    (is (= :method-digest
           (field #(evidence/verify-capture! h (bytes method) contract-bytes (bytes capture)))))
    (is (= :capture-digest
           (field #(evidence/verify-capture! (hash-value method) (bytes method) contract-bytes
                                             (bytes (assoc capture :terminal {}))))))
    (is (= :contract-digest
           (field #(evidence/verify-capture! (hash-value method) (bytes method) (bytes {})
                                             (bytes capture)))))))

(deftest missing-and-wrong-input-identities-reject-even-with-new-capture-pin
  (doseq [path [[ :method :binding :inputs]
                [:method :binding :inventories]
                [:method :binding :runtime-tool-inputs]
                [:method :binding :shadow-inputs]]]
    (is (some? (field #(check (assoc-in (fixture) path [])))) (pr-str path)))
  (doseq [input (:inputs closure)]
    (let [f (fixture)
          index (.indexOf (mapv :path (:inputs closure)) (:path input))
          changed (assoc-in f [:capture :binding-after :inputs index :sha256]
                            (hash-value :changed))]
      (is (= :bookends (field #(check changed))) (:path input))))
  (doseq [mutation [(fn [f] (assoc-in f [:capture :binding-after :source :clean?] false))
                    (fn [f] (update-in f [:capture :binding-after :classpath-identities]
                                       #(vec (reverse %))))
                    (fn [f] (assoc-in f [:capture :binding-after :child-runtime-identity :sha256]
                                       (hash-value :wrong-runtime)))
                    (fn [f] (assoc-in f [:capture :binding-after :source :commit]
                                       (apply str (repeat 40 "c"))))]]
    (is (= :bookends (field #(check (mutation (fixture))))))))

(deftest provenance-and-terminal-gaps-never-pass
  (doseq [path [[:capture :producer-provenance :revision]
                [:capture :producer-provenance :consumed-input-identity]
                [:capture :producer-provenance :child-runtime-identity]
                [:capture :producer-provenance :classpath-identities]]]
    (is (= :producer-provenance (field #(check (assoc-in (fixture) path nil))))))
  (is (= :applying-source
         (field #(check (assoc-in (fixture) [:capture :applying-source :commit] "wrong")))))
  (doseq [[key value] (:terminal (:capture (fixture)))]
    (is (= :terminal (field #(check (update-in (fixture) [:capture :terminal] dissoc key))))
        (str key " missing"))
    (is (= :terminal (field #(check (assoc-in (fixture) [:capture :terminal key]
                                             (if (boolean? value) (not value) 1)))))
        (str key " wrong"))))

(deftest incomplete-failed-or-wrong-report-rejects
  (doseq [[path value] [[[:summary :fail] 1]
                        [[:namespaces] ['gravity.unrelated-test]]
                        [[:namespace-results] []]
                        [[:namespace-results 0 :stdout :truncated?] true]
                        [[:namespace-results 0 :stderr :observed-bytes] 1]
                        [[:authoritative?] true]
                        [[:tests-executed?] false]
                        [[:child-cache-hit?] true]
                        [[:child-cache-receipt :stored?] true]
                        [[:exit-code] 1]]]
    (let [fixture (fixture)
          broken (assoc-in (:report fixture) path value)]
      (is (some? (field #(check (assoc-in fixture [:capture :report-edn] (pr-str broken)))))
          (pr-str path))))
  (doseq [text ["{} {}" "#inst \"2020-01-01\"" "#uuid \"00000000-0000-0000-0000-000000000000\""
                "#unknown {}" "#=(throw (Exception.))"]]
    (is (thrown? Exception
                 (check (assoc-in (fixture) [:capture :report-edn] text))))))

(deftest malformed-method-and-missing-artifact-refuse
  (let [method-bytes (.getBytes "#=(System/exit 0)" "UTF-8")]
    (is (thrown? Exception
                 (evidence/verify-capture! (hash-bytes method-bytes) method-bytes
                                           contract-bytes (bytes {})))))
  (is (= :arguments (field #(evidence/-main))))
  (is (= :artifact-file
         (field #(evidence/-main h "/no-such-reader-evidence-artifact" "unused" "unused")))))

(deftest cli-reads-only-explicit-artifacts-and-roundtrips-result
  (let [{:keys [method capture]} (fixture)
        directory (java.nio.file.Files/createTempDirectory
                   "reader-evidence-control-" (make-array java.nio.file.attribute.FileAttribute 0))
        artifacts [(bytes method) contract-bytes (bytes capture)]
        paths (mapv #(.resolve directory %) ["method.edn" "contract.edn" "capture.edn"])]
    (try
      (doseq [[path content] (map vector paths artifacts)]
        (java.nio.file.Files/write path content (make-array java.nio.file.OpenOption 0)))
      (let [result (edn/read-string
                    (with-out-str
                      (apply evidence/-main (hash-value method) (map str paths))))]
        (is (= :consistent (:status result)))
        (is (= (hash-value capture) (:capture-sha256 result))))
      (finally
        (doseq [path paths] (java.nio.file.Files/deleteIfExists path))
        (java.nio.file.Files/delete directory)))))
