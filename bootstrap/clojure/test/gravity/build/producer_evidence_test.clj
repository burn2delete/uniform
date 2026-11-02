(ns gravity.build.producer-evidence-test
  (:refer-clojure :exclude [bytes])
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.build.producer-evidence :as evidence]
            [gravity.build.parent-producer-observation :as parent]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]
            [gravity.self-hosting-test-runner :as child-runner]
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
                 :runtime-tool-inputs (mapv #(hash-map :id % :sha256 h)
                                            [:clojure-runtime :java-runtime
                                             :java-executable :test-command-executable])}
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

(deftest complete-runtime-tool-and-aggregate-identities-are-required
  (doseq [id [:clojure-runtime :java-runtime :java-executable :test-command-executable]]
    (is (= :runtime-tool-inputs
           (field #(check (update-in (fixture) [:method :binding :runtime-tool-inputs]
                                      (fn [inputs] (filterv (fn [entry] (not= id (:id entry)))
                                                            inputs))))))
        (str id)))
  (is (= :runtime-digest
         (field #(check (assoc-in (fixture) [:method :binding :child-runtime-identity :sha256]
                                   (hash-value :wrong))))))
  (is (= :closure-digest
         (field #(check (assoc-in (fixture) [:method :binding :consumed-input-identity]
                                   (hash-value :wrong))))))
  (is (= :classpath-order
         (field #(check (update-in (fixture) [:method :binding :classpath-identities]
                                    (fn [entries] (vec (reverse entries)))))))))

(defn- fixture-with-output [stream output]
  (let [f (fixture)
        report (assoc-in (:report f) [:namespace-results 0 stream] output)]
    (assoc-in f [:capture :report-edn] (pr-str report))))

(deftest complete-output-requires-valid-capture-metadata
  (let [output {:text "ok" :bytes 2 :observed-bytes 2 :limit-bytes 2 :truncated? false}]
    (doseq [stream [:stdout :stderr]]
      (is (= :consistent (:status (check (fixture-with-output stream output)))))
      (doseq [key [:text :bytes :observed-bytes :limit-bytes :truncated?]]
        (is (= :complete-output
               (field #(check (fixture-with-output stream (dissoc output key)))))
            (str stream " missing " key)))
      (doseq [[key value] [[:limit-bytes -1] [:limit-bytes 1] [:limit-bytes 2.0]
                            [:limit-bytes "2"] [:bytes -1] [:bytes 1]
                            [:observed-bytes -1] [:observed-bytes 3]
                            [:truncated? true]]]
        (is (= :complete-output
               (field #(check (fixture-with-output stream (assoc output key value)))))
            (str stream " wrong " key " " value))))))

(deftest serialized-output-remains-bound-to-reviewed-input-bytes
  (let [output {:text "\u00e9" :bytes 2 :observed-bytes 2 :limit-bytes 2 :truncated? false}
        {:keys [method capture]} (fixture-with-output :stdout output)
        capture-bytes (bytes capture)
        method (assoc method :capture-sha256 (hash-bytes capture-bytes))
        method-bytes (bytes method)
        method-pin (hash-bytes method-bytes)
        pins [(hash-bytes method-bytes) (hash-bytes contract-bytes) (hash-bytes capture-bytes)]
        result (evidence/verify-capture! method-pin method-bytes contract-bytes capture-bytes)
        serialized (bytes result)
        readback (edn/read-string (String. serialized "UTF-8"))]
    (is (= :consistent (:status readback)))
    (is (= pins [(:method-sha256 readback) (:contract-sha256 readback)
                 (:capture-sha256 readback)]))
    (is (= result readback))
    (is (= (hash-bytes serialized) (hash-bytes (bytes readback))))
    (is (= pins [(hash-bytes method-bytes) (hash-bytes contract-bytes)
                 (hash-bytes capture-bytes)]))
    (is (false? (:real-evidence-accepted? readback)))
    (is (false? (:cache-admission? readback)))
    (is (= :capture-digest
           (field #(evidence/verify-capture!
                    method-pin method-bytes contract-bytes
                    (bytes (assoc-in capture [:terminal :exit-code] 1))))))
    (is (= :terminal
           (field #(check (assoc-in (fixture-with-output :stdout output)
                                      [:capture :terminal :exit-code] 1)))))))

(defn- source-shaped-snapshots []
  (let [base {:repository-identity h :path-count 4 :hashed-path-count 2 :hashed-byte-count 8}]
    [(assoc base :schema :gravity/sh01-complete-repository-snapshot-v1
                 :snapshot-engine :full-content-v1)
     (assoc base :schema :gravity/sh01-incremental-repository-snapshot-v2
                 :snapshot-engine :git-index-full-content-v2)
     (assoc base :schema :gravity/sh01-incremental-repository-snapshot-v2
                 :snapshot-engine :git-index-overlay-v2
                 :tracked-path-count 4 :overlay-path-count 2)]))

(defn- fixture-with-snapshot [snapshot]
  (assoc-in (fixture) [:capture :producer-provenance :repository-snapshot] snapshot))

(deftest supplied-runtime-snapshot-records-survive-pinned-serialization
  (doseq [snapshot (source-shaped-snapshots)]
    (let [{:keys [method capture]} (fixture-with-snapshot snapshot)
          capture-bytes (bytes capture)
          method (assoc method :capture-sha256 (hash-bytes capture-bytes))
          method-bytes (bytes method)
          pins [(hash-bytes method-bytes) (hash-bytes contract-bytes) (hash-bytes capture-bytes)]
          result (evidence/verify-capture! (first pins) method-bytes contract-bytes capture-bytes)
          readback (edn/read-string (String. capture-bytes "UTF-8"))]
      (is (= :consistent (:status result)))
      (is (= snapshot (get-in readback [:producer-provenance :repository-snapshot])))
      (is (= pins [(:method-sha256 result) (:contract-sha256 result) (:capture-sha256 result)]))
      (is (= pins [(hash-bytes method-bytes) (hash-bytes contract-bytes) (hash-bytes capture-bytes)]))
      (is (= result (edn/read-string (String. (bytes result) "UTF-8"))))
      (is (false? (:real-evidence-accepted? result)))
      (is (false? (:cache-admission? result)))
      (let [changed (update-in capture [:producer-provenance :repository-snapshot :hashed-byte-count] inc)]
        (is (not= (hash-bytes capture-bytes) (hash-value changed)))
        (is (= :capture-digest
               (field #(evidence/verify-capture! (first pins) method-bytes contract-bytes (bytes changed)))))
        (is (not= (:capture-sha256 result)
                  (:capture-sha256 (check (assoc (fixture-with-snapshot snapshot) :capture changed)))))))))

(deftest unknown-or-incomplete-producer-snapshots-refuse
  (doseq [snapshot (source-shaped-snapshots)]
    (doseq [key (keys snapshot)]
      (is (= :producer-snapshot
             (field #(check (fixture-with-snapshot (dissoc snapshot key)))))
          (str (:snapshot-engine snapshot) " missing " key)))
    (doseq [[key value] [[:schema :unknown] [:snapshot-engine :unknown]
                         [:repository-identity (hash-value :wrong-source)]
                         [:path-count -1] [:hashed-path-count "2"] [:hashed-byte-count nil]
                         [:unexpected true]]]
      (is (= :producer-snapshot
             (field #(check (fixture-with-snapshot (assoc snapshot key value)))))
          (str (:snapshot-engine snapshot) " wrong " key))))
  (is (= :producer-snapshot (field #(check (fixture-with-snapshot nil))))))

(deftest full-provenance-does-not-enable-miss-hit-or-failed-capture
  (doseq [snapshot (source-shaped-snapshots)]
    (doseq [decision [:miss :hit]]
      (let [f (fixture-with-snapshot snapshot)
            report (-> (:report f)
                       (assoc :child-cache-hit? (= :hit decision) :tests-executed? (= :miss decision))
                       (assoc-in [:child-cache-receipt :decision] decision))]
        (is (= :report
               (field #(check (assoc-in f [:capture :report-edn] (pr-str report))))))))
    (is (= :terminal
           (field #(check (assoc-in (fixture-with-snapshot snapshot)
                                      [:capture :terminal :natural-exit?] false)))))))

(deftest full-snapshots-retain-provenance-identity-refusals
  (doseq [snapshot (source-shaped-snapshots)]
    (let [f (fixture-with-snapshot snapshot)
          provenance (get-in f [:capture :producer-provenance])]
      (doseq [changed [(assoc-in provenance [:revision :commit] (apply str (repeat 40 "c")))
                       (assoc provenance :consumed-input-identity (hash-value :wrong-input))
                       (assoc-in provenance [:child-runtime-identity :sha256] (hash-value :wrong-runtime))
                       (update provenance :classpath-identities #(vec (reverse %)))]]
        (is (= :producer-provenance
               (field #(check (assoc-in f [:capture :producer-provenance] changed)))))))))

(deftest actual-child-observation-flows-to-fresh-serializer
  ;; This exercises the real wiring and serializer with injected observations;
  ;; it is not a real producer or terminal observation.
  (let [{:keys [capture report]} (fixture-with-snapshot (last (source-shaped-snapshots)))
        observed {:binding (:binding-before capture)
                  :repository-snapshot (get-in capture [:producer-provenance :repository-snapshot])}
        events (atom [])
        binding-var (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                               'observe-fresh-producer-binding!)
        loaders-var (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                               'supported-resource-loaders?)]
    (with-redefs-fn
      {binding-var (fn [& _] (swap! events conj :observe) observed)
       loaders-var (constantly true)}
      (fn []
        (let [returned (wiring/run-child-unit!
                        ['gravity.c2-artifact-identity-test] {:observe-producer? true}
                        #(do (swap! events conj :operation) report))
              serialized (evidence/serialize-observed-fresh-capture
                           contract-bytes returned (:terminal capture))
              readback (edn/read-string (String. serialized "UTF-8"))]
          (is (= [:observe :operation :observe] @events))
          (is (= (:binding-before capture) (:binding-before readback) (:binding-after readback)))
          (is (= (:producer-provenance capture) (:producer-provenance readback)))
          (is (= returned (edn/read-string (:report-edn readback))))
          (is (false? (:child-cache-hit? returned)))
          (is (false? (get-in returned [:child-cache-receipt :cacheable?])))
          (is (= :terminal (field #(evidence/serialize-observed-fresh-capture
                                   contract-bytes returned
                                   (assoc (:terminal capture) :cleanup-complete? false)))))
          (is (= :child-observation
                 (field #(evidence/serialize-observed-fresh-capture contract-bytes report (:terminal capture))))))))))

(deftest child-observation-drift-or-unsupported-selection-refuses
  (let [calls (atom 0)
        operations (atom 0)
        observer (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring
                             'observe-fresh-producer-binding!)]
    (with-redefs-fn
      {observer (fn [& _] {:binding {:observation (swap! calls inc)}})}
      (fn []
        (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
               (try (wiring/run-child-unit!
                      ['gravity.c2-artifact-identity-test] {:observe-producer? true}
                      #(do (swap! operations inc) {:status :passed}))
                    (catch clojure.lang.ExceptionInfo error (:id (ex-data error))))))
        (is (= 1 @operations))
        (is (= 2 @calls))
        (is (= "SH01-PRODUCER-OBSERVATION"
               (try (wiring/run-child-unit!
                      ['gravity.c11-mir-test] {:observe-producer? true}
                      #(swap! operations inc))
                    (catch clojure.lang.ExceptionInfo error (:id (ex-data error))))))
        (is (= 1 @operations))))))

(deftest producer-observation-is-explicit-and-runtime-required
  (let [options {:cache-directory "/tmp/unused-source-control"
                 :timeout-ms 1000 :repository-identity h}]
    (is (child-runner/valid-development-cache-options? options))
    (is (child-runner/valid-development-cache-options? (assoc options :observe-producer? true)))
    (is (false? (child-runner/valid-development-cache-options? (assoc options :observe-producer? false))))
    (is (false? (child-runner/valid-development-cache-options? (assoc options :unknown-observation true)))))
  (let [calls (atom 0)
        runtime (ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring 'effective-child-runtime)]
    (with-redefs-fn
      {runtime (constantly nil)}
      (fn []
        (is (= "SH01-PRODUCER-OBSERVATION"
               (try (wiring/run-child-unit!
                      ['gravity.c2-artifact-identity-test] {:observe-producer? true}
                      #(swap! calls inc))
                    (catch clojure.lang.ExceptionInfo error (:id (ex-data error))))))
        (is (zero? @calls))))))

(deftest producer-observer-rejects-missing-nonregular-and-changing-inputs
  ;; Runtime/context ports are injected; declared files are read, never run.
  (let [resolve-wiring #(ns-resolve 'gravity.self-hosting.sh01-development-loop-wiring %)
        observer (var-get (resolve-wiring 'observe-fresh-producer-binding!))
        state-var (resolve-wiring 'required-input-state)
        state (var-get state-var)
        inventory-var (resolve-wiring 'closure-inventory)
        inventory (var-get inventory-var)
        binding (:binding-before (:capture (fixture)))
        common {(resolve-wiring 'effective-child-runtime) (constantly (:child-runtime-identity binding))
                #'wiring/prepare-context (constantly {:revision (select-keys (:source binding) [:commit :tree])
                                                      :snapshot {:repository-identity h}
                                                      :runtime-tool-inputs (:runtime-tool-inputs binding)})
                (resolve-wiring 'explicit-classpath-identities) (constantly (:classpath-identities binding))
                (resolve-wiring 'run-git-bytes!) (fn [& _] (.getBytes "" "UTF-8"))}
        first-path (:path (first (:inputs closure)))
        observe #(with-redefs-fn (merge common %)
                   (fn [] (observer (System/getProperty "user.dir") [] {:repository-identity h})))
        id #(try (observe %) nil (catch clojure.lang.ExceptionInfo error (:id (ex-data error))))]
    (is (= (mapv :path (:inputs closure)) (mapv :path (get-in (observe {}) [:binding :inputs]))))
    (let [git-binding {:schema :gravity/rv2-git-binding-v1 :synthetic true}
          observed (with-redefs-fn common
                     (fn [] (observer (System/getProperty "user.dir") []
                                      {:repository-identity h :producer-git-binding git-binding})))]
      (is (= {:id :selected-observer-git-binding :sha256 (hash-value git-binding)}
             (last (get-in observed [:binding :runtime-tool-inputs])))))
    ;; Equal child bookends do not justify attributing already loaded code to
    ;; a source snapshot different from the parent's prelaunch identity.
    (doseq [identity [nil (hash-value :changed-before-child-startup)]]
      (is (= {:id "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE" :input-kind :child-startup}
             (with-redefs-fn common
               (fn []
                 (try
                   (observer (System/getProperty "user.dir") [] {:repository-identity identity})
                   nil
                   (catch clojure.lang.ExceptionInfo error
                     (select-keys (ex-data error) [:id :input-kind]))))))))
    (is (= "SH01-PRODUCER-OBSERVATION"
           (id {state-var (fn [root path] (when-not (= first-path path) (state root path)))})))
    (is (= "SH01-PRODUCER-OBSERVATION"
           (id {state-var (fn [root path] (state root (if (= first-path path) "." path)))})))
    (let [reads (atom {})]
      (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             (id {state-var (fn [root path]
                              (let [n (get (swap! reads update path (fnil inc 0)) path)
                                    observed (state root path)]
                                (if (and (= first-path path) (> n 1))
                                  (update observed :executable? not) observed)))}))))
    (let [reads (atom 0)]
      (is (= "SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE"
             (id {inventory-var (fn [root declaration]
                                  (let [observed (inventory root declaration)]
                                    (if (> (swap! reads inc) (count (:inventories closure)))
                                      (update observed :paths conj "synthetic-changed-inventory")
                                      observed)))}))))))

(defn- fresh-supplied [capture]
  (select-keys capture [:binding-before :binding-after :producer-provenance :applying-source :terminal]))

(deftest retained-parent-observation-connects-to-the-existing-fresh-serializer
  ;; All parent process facts are injected. The current canonical API never
  ;; supplies global cleanup=true; this positive is serialization-only.
  (let [{:keys [capture report]} (fixture-with-snapshot (last (source-shaped-snapshots)))
        report (assoc report :test-producer-observation
                      (assoc (select-keys capture [:binding-before :binding-after
                                                   :producer-provenance :applying-source])
                             :schema :gravity/sh01-fresh-producer-observation-v1))
        result {:status :passed :exit-code 0 :wait-interrupted? false :wait-error nil
                :root-exited? true :tracked-owned-processes-terminal? true
                :cleanup-complete? true :descendant-coverage :unknown
                :timeout-containment-unproven? false
                :stdout-capture-status :complete :stderr-capture-status :complete
                :stdout-truncated? false :stderr-truncated? false
                :stdout-capture-forced-close? false :stderr-capture-forced-close? false
                :stdout-capture-error nil :stderr-capture-error nil}
        process (Object.) token (Object.) events (atom [])]
    (with-redefs [runner/retain-owned-process! (fn [p] (is (identical? process p))
                                               (swap! events conj :retain) token)
                  runner/process-result (fn [p _ owned]
                                          (is (identical? process p))
                                          (is (identical? token owned))
                                          (swap! events conj :result) result)
                  runner/retained-process-handles (fn [owned]
                                                    (is (identical? token owned))
                                                    (swap! events conj :handles) [(Object.)])
                  runner/finalize-owned-process! (fn [& _] (throw (AssertionError. "second finalization")))]
      (let [observation (parent/observe! process {})
            bytes (parent/serialize-capture contract-bytes report observation)
            readback (edn/read-string (String. bytes "UTF-8"))]
        (is (= [:retain :result :handles] @events))
        (is (= (:terminal capture) (:terminal readback)))
        (is (= report (edn/read-string (:report-edn readback))))
        (is (identical? token (:owned-process observation)))
        (is (= :unknown (:descendant-coverage observation)))
        (is (false? (:capture-v1-ready? observation)))
        (doseq [changed [(assoc result :cleanup-complete? nil)
                         (assoc result :cleanup-complete? false)
                         (assoc result :root-exited? false)
                         (assoc result :status :error)
                         (assoc result :wait-interrupted? true)
                         (assoc result :wait-error "injected wait error")
                         (assoc result :timeout-containment-unproven? true)
                         (assoc result :stdout-capture-status :error)
                         (assoc result :stderr-truncated? true)
                         (assoc result :stdout-capture-forced-close? true)
                         (assoc result :stderr-capture-error "injected stream error")
                         (dissoc result :timeout-containment-unproven?)
                         (dissoc result :stdout-truncated?)]]
          (is (= :terminal
                 (field #(parent/serialize-capture contract-bytes report
                                                   (assoc observation :process-result changed))))))
        (is (= [:retain :result :handles] @events))
        (is (= "RV2-PARENT-OBSERVATION"
               (try (parent/serialize-capture contract-bytes report {}) nil
                    (catch clojure.lang.ExceptionInfo error (:id (ex-data error))))))))))

(deftest fresh-return-serialization-retains-explicit-supplied-provenance
  (doseq [snapshot (cons {:repository-identity h} (source-shaped-snapshots))
          returned-provenance? [false true]]
    (let [{:keys [method capture report]} (fixture-with-snapshot snapshot)
          supplied (fresh-supplied capture)
          report (cond-> report
                   returned-provenance?
                   (assoc :test-producer-provenance (:producer-provenance supplied)
                          :test-applying-revision (select-keys (:applying-source supplied) [:commit :tree])
                          :test-applying-repository-snapshot snapshot))
          input-pins (mapv hash-value [supplied report])
          capture-bytes (evidence/serialize-fresh-capture contract-bytes supplied report)
          readback (edn/read-string (String. capture-bytes "UTF-8"))
          method (assoc method :capture-sha256 (hash-bytes capture-bytes))
          result (evidence/verify-capture! (hash-value method) (bytes method) contract-bytes capture-bytes)]
      (is (= supplied (fresh-supplied readback)))
      (is (= report (edn/read-string (:report-edn readback))))
      (is (= snapshot (get-in readback [:producer-provenance :repository-snapshot])))
      (is (= input-pins (mapv hash-value [supplied report])))
      (is (= (hash-bytes capture-bytes) (:capture-sha256 result)))
      (is (= :consistent (:status result)))
      (is (false? (:real-evidence-accepted? result)))
      (is (false? (:cache-admission? result))))))

(deftest returned-provenance-must-match-supplied-fresh-observations
  (let [{:keys [capture report] :as f} (fixture-with-snapshot (last (source-shaped-snapshots)))
        supplied (fresh-supplied capture)]
    (doseq [[key wrong] [[:test-producer-provenance nil]
                         [:test-producer-provenance {:revision {:commit "wrong"}}]
                         [:test-applying-revision nil]
                         [:test-applying-revision {:commit "wrong" :tree "wrong"}]
                         [:test-applying-repository-snapshot nil]
                         [:test-applying-repository-snapshot {:repository-identity (hash-value :wrong)}]]]
      (let [bad-report (assoc report key wrong)]
        (is (= :report-provenance
               (field #(evidence/serialize-fresh-capture contract-bytes supplied bad-report))))
        (is (= :report-provenance
               (field #(check (assoc-in f [:capture :report-edn] (pr-str bad-report))))))))))

(deftest fresh-serializer-refuses-missing-identities-and-incomplete-terminal
  (let [{:keys [capture report]} (fixture-with-snapshot (last (source-shaped-snapshots)))
        supplied (fresh-supplied capture)]
    (doseq [key (keys supplied)]
      (is (= :supplied-observations
             (field #(evidence/serialize-fresh-capture contract-bytes (dissoc supplied key) report)))))
    (doseq [[path wrong expected]
            [[[:producer-provenance] nil :producer-snapshot]
             [[:producer-provenance :revision :commit] (apply str (repeat 40 "c")) :producer-provenance]
             [[:producer-provenance :consumed-input-identity] (hash-value :wrong) :producer-provenance]
             [[:producer-provenance :child-runtime-identity :sha256] (hash-value :wrong) :producer-provenance]
             [[:producer-provenance :classpath-identities]
              (vec (reverse (get-in supplied [:producer-provenance :classpath-identities]))) :producer-provenance]
             [[:binding-before :source :clean?] false :source]
             [[:binding-after :source :commit] (apply str (repeat 40 "c")) :bookends]
             [[:applying-source :commit] (apply str (repeat 40 "c")) :applying-source]
             [[:terminal :natural-exit?] false :terminal]]]
      (is (= expected
             (field #(evidence/serialize-fresh-capture contract-bytes (assoc-in supplied path wrong) report)))
          (pr-str path)))
    (doseq [key (keys (:terminal supplied))]
      (is (= :terminal
             (field #(evidence/serialize-fresh-capture contract-bytes
                                                      (update supplied :terminal dissoc key) report)))))
    (is (= :complete-output
           (field #(evidence/serialize-fresh-capture contract-bytes supplied
                                                      (assoc-in report [:namespace-results 0 :stdout :truncated?] true)))))))

(deftest fresh-serializer-does-not-execute-functions-or-admit-other-results
  (let [{:keys [capture report]} (fixture)
        supplied (fresh-supplied capture)
        calls (atom 0)
        operation (fn [] (swap! calls inc))]
    (is (= :supplied-observations
           (field #(evidence/serialize-fresh-capture contract-bytes operation report))))
    (is (= :report
           (field #(evidence/serialize-fresh-capture contract-bytes supplied operation))))
    (is (zero? @calls))
    (doseq [changed [(assoc report :status :failed :exit-code 1)
                     (assoc-in report [:child-cache-receipt :decision] :miss)
                     (assoc report :child-cache-hit? true :tests-executed? false)]]
      (is (= :report
             (field #(evidence/serialize-fresh-capture contract-bytes supplied changed)))))))
