(ns gravity.build.producer-evidence
  "Offline RV2/RV4 Reader producer capture checking. Never launches work.

  A separately reviewed method digest is the trust input. Matching captures
  establish consistency only, not that a producer ran or reuse is admitted."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.digest :as digest])
  (:import [java.io PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file Files Paths]))

(def ^:private selection ['gravity.c2-artifact-identity-test])
(def ^:private lexical-paths
  #{"bootstrap/clojure/src/gravity/c2_lexical_validation.clj"
    "bootstrap/clojure/src/gravity/c2_lexical_validation/operations.clj"
    "bootstrap/clojure/src/gravity/c2_lexical_validation/primitives.clj"
    "bootstrap/clojure/src/gravity/c2_lexical_validation/product.clj"})

(defn- require! [condition field]
  (when-not condition
    (throw (ex-info "Producer evidence is missing or inconsistent"
                    {:id "PRODUCER-EVIDENCE-INVALID" :field field
                     :cache-admission? false :authority :non-authoritative}))))

(defn- keys! [value fields field]
  (require! (and (map? value) (= fields (set (keys value)))) field))

(defn- hash? [value]
  (and (string? value) (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- byte-hash [bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- value-hash [value]
  (byte-hash (.getBytes (pr-str value) StandardCharsets/UTF_8)))

(defn- decode [bytes]
  (str (.decode (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))
                (ByteBuffer/wrap bytes))))

(defn- read-edn [text]
  (let [reject (fn [& _] (require! false :tagged-edn))
        options {:eof ::eof :readers {'inst reject 'uuid reject} :default reject}]
    (with-open [reader (PushbackReader. (StringReader. text))]
      (let [value (edn/read options reader)]
        (require! (not= ::eof value) :empty-edn)
        (require! (= ::eof (edn/read options reader)) :trailing-edn)
        value))))

(defn- relative-path? [path]
  (and (string? path) (not (str/blank? path))
       (not (str/includes? path "\\"))
       (every? #(and (not (str/blank? %)) (not (#{"." ".."} %)))
               (str/split path #"/" -1))
       (not (str/includes? path ":"))))

(defn- identity! [identity key field]
  (keys! identity #{key :sha256} field)
  (require! (hash? (:sha256 identity)) field)
  (require! (if (= key :path)
              (relative-path? (:path identity))
              (keyword? (:id identity))) field))

(defn- source! [source]
  (keys! source #{:commit :tree :repository-identity :clean?} :source)
  (require! (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %))
                    [(:commit source) (:tree source)]) :source)
  (require! (and (hash? (:repository-identity source))
                 (true? (:clean? source))) :source))

(defn- producer-snapshot! [snapshot repository-identity]
  ;; Preserve the actual wiring's snapshot records without inferring that the
  ;; supplied schema, engine or counters were observed by a running child.
  (let [base-fields #{:schema :repository-identity :path-count :snapshot-engine
                      :hashed-path-count :hashed-byte-count}
        overlay-fields #{:tracked-path-count :overlay-path-count}
        engine (:snapshot-engine snapshot)
        schema (case engine
                 :full-content-v1 :gravity/sh01-complete-repository-snapshot-v1
                 (:git-index-full-content-v2 :git-index-overlay-v2)
                 :gravity/sh01-incremental-repository-snapshot-v2
                 nil)]
    (require! (and (map? snapshot)
                   (= repository-identity (:repository-identity snapshot))) :producer-snapshot)
    (when-not (= #{:repository-identity} (set (keys snapshot)))
      (keys! snapshot (if (= :git-index-overlay-v2 engine)
                        (into base-fields overlay-fields) base-fields) :producer-snapshot)
      (require! (and schema (= schema (:schema snapshot))) :producer-snapshot)
      (require! (every? #(and (integer? %) (<= 0 %))
                        (vals (apply dissoc snapshot [:schema :repository-identity :snapshot-engine])))
                :producer-snapshot))))

(defn- binding! [binding contract]
  (keys! binding #{:source :consumed-input-identity :inputs :inventories
                   :shadow-inputs :classpath-identities :child-runtime-identity
                   :runtime-tool-inputs} :binding)
  (source! (:source binding))
  (require! (hash? (:consumed-input-identity binding)) :consumed-input-identity)
  (let [entries (for [component (:components contract)
                      entry (:tests component)
                      :when (= (first selection) (:namespace entry))] entry)
        entry (first entries)
        closure (:cache-closure entry)
        inputs (:inputs binding)]
    (require! (= 1 (count entries)) :catalog-selection)
    (require! (= :gravity/stage0-explicit-test-cache-closure-v1 (:schema closure))
              :closure-schema)
    (require! (= :gravity/stage0-incremental-test-dependencies-v2
                 (:schema contract)) :contract-schema)
    (require! (true? (get-in entry [:test-policy :freshness-required?])) :fresh-policy)
    (require! (and (vector? inputs) (seq inputs)
                   (= (:inputs closure) (mapv #(select-keys % [:path :role]) inputs))
                   (= (count inputs) (count (set (map :path inputs))))
                   (every? (set (map :path inputs)) lexical-paths)) :declared-inputs)
    (doseq [input inputs]
      (keys! input #{:path :role :sha256} :input)
      (require! (and (relative-path? (:path input)) (hash? (:sha256 input))) :input))
    (let [inventories (:inventories binding)]
      (require! (and (vector? inventories)
                     (= (:inventories closure)
                        (mapv #(dissoc % :paths) inventories))) :inventories)
      (doseq [{:keys [root suffix paths]} inventories]
        (require! (and (vector? paths) (= paths (vec (sort (distinct paths))))
                       (every? #(and (relative-path? %)
                                     (str/starts-with? % (str root "/"))
                                     (str/ends-with? % suffix)) paths)) :inventory-paths)))
    ;; The reviewed method supplies the pinned runner's complete shadow list.
    ;; This checker does not rediscover classloader resources or infer imports.
    (let [shadows (:shadow-inputs binding)]
      (require! (and (vector? shadows) (seq shadows)
                     (= (mapv :path shadows) (vec (sort (distinct (map :path shadows))))))
                :shadow-inputs)
      (doseq [shadow shadows]
        (keys! shadow #{:path :role :sha256} :shadow-input)
        (require! (= :shadow (:role shadow)) :shadow-input)
        (identity! (dissoc shadow :role) :path :shadow-input)))
    (doseq [[field key] [[:classpath-identities :path] [:runtime-tool-inputs :id]]]
      (let [identities (get binding field)]
        (require! (and (vector? identities) (seq identities)
                       (= (count identities) (count (distinct (map key identities))))) field)
        (doseq [identity identities] (identity! identity key field))))
    (require! (every? (set (map :id (:runtime-tool-inputs binding)))
                      [:clojure-runtime :java-runtime :java-executable :test-command-executable])
              :runtime-tool-inputs)
    (require! (= (mapv :path (:classpath-identities binding))
                 (mapv #(format "classpath/entry-%04d" %)
                       (range (count (:classpath-identities binding))))) :classpath-order)
    (let [runtime (:child-runtime-identity binding)
          fingerprints (:fingerprints runtime)]
      (keys! runtime #{:id :sha256 :fingerprints} :child-runtime)
      (require! (= :actual-child-runtime-v1 (:id runtime)) :child-runtime)
      (require! (hash? (:sha256 runtime)) :child-runtime)
      (keys! fingerprints #{:basis :basis-file :configuration-sources
                            :jvm-input-arguments :environment :system-properties}
             :runtime-fingerprints)
      (require! (every? hash? (vals fingerprints)) :runtime-fingerprints)
      (require! (= (:sha256 runtime) (value-hash (into (sorted-map) fingerprints)))
                :runtime-digest))
    (require! (= (:consumed-input-identity binding)
                 (value-hash (array-map
                              :schema (:schema closure)
                              :dependency-contract-schema (:schema contract)
                              :inputs inputs
                              :shadow-inputs (:shadow-inputs binding)
                              :inventories (:inventories binding)))) :closure-digest)))

(defn- output! [output]
  (require! (and (map? output) (string? (:text output))
                 (false? (:truncated? output))
                 (every? #(and (integer? %) (<= 0 %))
                         [(:bytes output) (:observed-bytes output) (:limit-bytes output)])
                 (<= (:bytes output) (:limit-bytes output))
                 (= (:bytes output) (:observed-bytes output)
                    (alength (.getBytes ^String (:text output) StandardCharsets/UTF_8))))
            :complete-output))

(defn- report! [report]
  (require! (and (= :gravity/self-hosting-test-report-v2 (:schema report))
                 (= :non-authoritative (:authority report))
                 (false? (:authoritative? report))
                 (= :passed (:status report)) (= 0 (:exit-code report))
                 (true? (:child-jvm-launched? report))
                 (true? (:tests-executed? report))
                 (false? (:child-cache-hit? report))
                 (= {:decision :fresh :cacheable? false :stored? false
                     :producer-executed? true :reason :unreviewed-or-fresh-test-policy}
                    (:child-cache-receipt report))
                 (= selection (:namespaces report))
                 (= [] (:skipped-namespaces report))
                 (= selection (mapv :namespace (:namespace-results report)))) :report)
  (let [members (:namespace-results report)
        summary (:summary report)]
    (require! (and (map? summary) (= #{:test :pass :fail :error} (set (keys summary)))
                   (every? #(and (integer? %) (<= 0 %)) (vals summary))
                   (pos? (:test summary)) (pos? (:pass summary))
                   (= 0 (:fail summary) (:error summary))
                   (= summary (select-keys report [:test :pass :fail :error]))
                   (= summary (apply merge-with +
                                     (map #(select-keys % [:test :pass :fail :error]) members))))
              :summary)
    (doseq [member members]
      (require! (and (true? (:attempted? member)) (= :passed (:status member))
                     (= 0 (:exit-code member))) :member)
      (output! (:stdout member)) (output! (:stderr member)))))

(defn- capture! [binding capture]
  (keys! capture #{:schema :binding-before :binding-after :producer-provenance
                   :applying-source :terminal :report-edn} :capture)
  (require! (= :gravity/reader-producer-capture-v1 (:schema capture)) :capture-schema)
  (require! (= binding (:binding-before capture) (:binding-after capture)) :bookends)
  (require! (= (:source binding) (:applying-source capture)) :applying-source)
  (producer-snapshot! (get-in capture [:producer-provenance :repository-snapshot])
                      (get-in binding [:source :repository-identity]))
  (require! (= {:revision (select-keys (:source binding) [:commit :tree])
                :repository-snapshot
                {:repository-identity (get-in binding [:source :repository-identity])}
                :consumed-input-identity (:consumed-input-identity binding)
                :classpath-identities (:classpath-identities binding)
                :child-runtime-identity (:child-runtime-identity binding)}
               (assoc (:producer-provenance capture) :repository-snapshot
                      {:repository-identity (get-in binding [:source :repository-identity])}))
            :producer-provenance)
  (require! (= {:exit-code 0 :natural-exit? true :timed-out? false
                :stdout-complete? true :stderr-complete? true
                :cleanup-complete? true :root-dead? true}
               (:terminal capture)) :terminal)
  (require! (string? (:report-edn capture)) :report-edn)
  (let [report (read-edn (:report-edn capture))
        producer (:producer-provenance capture)]
    (report! report)
    ;; Fresh returns may omit these fields. If present, preserve and compare
    ;; them rather than silently replacing a producer with applying metadata.
    (doseq [[key expected] [[:test-producer-provenance producer]
                            [:test-applying-revision
                             (select-keys (:applying-source capture) [:commit :tree])]
                            [:test-applying-repository-snapshot (:repository-snapshot producer)]]]
      (require! (or (not (contains? report key)) (= expected (get report key)))
                :report-provenance)))
  capture)

(defn serialize-fresh-capture
  "Serialize a fresh return and explicit supplied observations as capture v1.

  Calls no observer, operation, cache or broker. Returns only new capture
  bytes, not a review pin, measured facts, authentication or admission."
  [contract-bytes supplied report]
  (keys! supplied #{:binding-before :binding-after :producer-provenance
                    :applying-source :terminal} :supplied-observations)
  (require! (map? report) :report)
  (let [contract (read-edn (decode contract-bytes))
        binding (:binding-before supplied)
        capture (assoc supplied :schema :gravity/reader-producer-capture-v1
                       :report-edn (pr-str report))]
    (binding! binding contract)
    (capture! binding capture)
    (.getBytes (pr-str capture) StandardCharsets/UTF_8)))

(defn serialize-observed-fresh-capture
  "Serialize the child wiring's observation with separately observed terminal facts.

  Does not supply terminal facts or confer method review or runtime admission."
  [contract-bytes report terminal]
  (let [observation (:test-producer-observation report)]
    (keys! observation #{:schema :binding-before :binding-after :producer-provenance
                         :applying-source} :child-observation)
    (require! (= :gravity/sh01-fresh-producer-observation-v1 (:schema observation))
              :child-observation)
    (serialize-fresh-capture contract-bytes
                             (assoc (dissoc observation :schema) :terminal terminal)
                             report)))

(defn verify-capture!
  "Check pinned method, dependency contract and retained capture bytes offline.

  expected-method-sha256 must come from independent exact-method review, not
  from the method itself. Identities are observations supplied by the capturer;
  this function neither measures a live child nor authenticates execution."
  [expected-method-sha256 method-bytes contract-bytes capture-bytes]
  (require! (and (hash? expected-method-sha256)
                 (= expected-method-sha256 (byte-hash method-bytes))) :method-digest)
  (let [method (read-edn (decode method-bytes))]
    (keys! method #{:schema :contract-sha256 :capture-sha256 :binding} :method)
    (require! (= :gravity/reader-producer-method-v1 (:schema method)) :method-schema)
    (require! (= (:contract-sha256 method) (byte-hash contract-bytes)) :contract-digest)
    (require! (= (:capture-sha256 method) (byte-hash capture-bytes)) :capture-digest)
    (let [contract (read-edn (decode contract-bytes))
          binding (:binding method)
          capture (read-edn (decode capture-bytes))]
      (binding! binding contract)
      (capture! binding capture)
      {:schema :gravity/reader-producer-evidence-check-v1
       :status :consistent :authority :non-authoritative
       :method-sha256 expected-method-sha256
       :capture-sha256 (:capture-sha256 method)
       :contract-sha256 (:contract-sha256 method)
       :producer-revision (select-keys (:source binding) [:commit :tree])
       :applying-source (:applying-source capture)
       :producer-executed-by-driver? false :cache-admission? false
       :real-evidence-accepted? false})))

(defn -main [& arguments]
  (require! (= 4 (count arguments)) :arguments)
  (let [[expected-method-sha256 method contract capture] arguments
        read-bytes (fn [path]
                     (let [path (Paths/get path (make-array String 0))]
                       (require! (Files/isRegularFile
                                  path (into-array java.nio.file.LinkOption
                                                   [java.nio.file.LinkOption/NOFOLLOW_LINKS]))
                                 :artifact-file)
                       (Files/readAllBytes path)))]
    (prn (verify-capture! expected-method-sha256 (read-bytes method)
                          (read-bytes contract) (read-bytes capture)))))
