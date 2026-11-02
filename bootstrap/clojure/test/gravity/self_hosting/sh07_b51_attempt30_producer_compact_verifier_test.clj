(ns gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-fixture-support
             :as fixture-support])
  (:import [java.nio.charset StandardCharsets]
           [java.util.concurrent TimeUnit]))

(def producer-paths
  ["bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"])

(defn read-all [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= form ::eof) forms (recur (conj forms form)))))))

(def producer-forms (delay (vec (mapcat read-all producer-paths))))

(def producer-definitions
  (delay
    (into {}
          (for [form @producer-forms
                :when (and (seq? form) (= 'defn (first form)))]
            [(second form) form]))))

(defn call-heads [value]
  (cond
    (and (seq? value) (= 'quote (first value))) #{}
    (seq? value)
    (into (if (symbol? (first value)) #{(first value)} #{})
          (mapcat call-heads value))
    (coll? value) (into #{} (mapcat call-heads value))
    :else #{}))

(def producer-call-graph
  (delay
    (into {}
          (map (fn [[name form]]
                 [name (set (filter @producer-definitions
                                    (call-heads (drop 3 form))))]))
          @producer-definitions)))

(defn call-closure-in [graph root]
  (loop [pending [root] seen #{}]
    (if (empty? pending)
      seen
      (let [current (peek pending)
            remaining (pop pending)]
        (if (contains? seen current)
          (recur remaining seen)
          (recur (into remaining (get graph current))
                 (conj seen current)))))))

(defn call-closure [root]
  (call-closure-in @producer-call-graph root))

(defn compact-call-graph []
  (update @producer-call-graph
          'sh07-b51-success-digest-replay-exact?
          disj
          'sh07-b51-success-digest-replay-exhaustive))

(defn producer-runtime []
  (let [definitions (vals @producer-definitions)
        runtime-ns
        (create-ns (gensym "gravity.sh07.b51.attempt30.producer-"))]
    (binding [*ns* runtime-ns]
      (clojure.core/refer 'clojure.core)
      (intern runtime-ns 'sh07-declared-digest-hash
              bootstrap/reader-canonical-hash)
      (intern runtime-ns 'sh07-canonical-text-compare
              bootstrap/p15-s23-sh07-canonical-text-compare)
      (intern runtime-ns 'sh07-observe-utf8-byte-count
              bootstrap/p15-s23-sh07-observe-utf8-byte-count)
      (intern runtime-ns 'sh07-b51-admitted-executable-carrier-validation?
              (fn [& _]
                (throw
                 (ex-info "receipt-only builtin reached in compact replay"
                          {:function
                           'sh07-b51-admitted-executable-carrier-validation?}))))
      (intern runtime-ns 'sort-by-pr-str
              (fn [values] (apply list (sort-by pr-str values))))
      (doseq [form definitions] (intern runtime-ns (second form)))
      (doseq [form definitions] (eval form)))
    runtime-ns))

(def runtime (delay (producer-runtime)))

(defn invoke [function & arguments]
  (apply @(ns-resolve @runtime function) arguments))

(defn accepted-carrier []
  (fixture-support/carrier))

(def ^:private isolated-carrier
  {:domain :gravity/sh07-b51-isolation-carrier
   :schema-version 4
   :semantic-root-hash "sha256:isolation"
   :module-count 7})

(def ^:private isolated-id-hashes
  {:caller-pin
   "sha256:6cdbaa48c97d8feca5ea7f58346efb766a82d708058a991d0c15f4d497800b09"
   :category-root
   "sha256:2f43891e34c0f742aa925c9f4dd9b9876119bb2f8606028822212fcab54b480c"})

(def ^:private isolated-raw-request-hash
  "sha256:507d5832ffd4e13e72049e8593959c87df5c5b6346d6590b545e96751b433be0")

(defn- isolated-fixture-observation
  []
  (let [raw (fixture-support/raw-request isolated-carrier)]
    {:carrier isolated-carrier
     :id-hashes
     {:caller-pin
      (fixture-support/test-id :caller-pin)
      :category-root
      (fixture-support/test-id [:category-root :roots 8])}
     :raw-hash (bootstrap/reader-canonical-hash raw)
     :raw-keys (sort (keys raw))
     :carrier-identical?
     (identical? isolated-carrier (:v18-executable-descriptor raw))}))

(def ^:private fresh-load-probe-expression
  "(require '[clojure.test :as test]
             'gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test)
    (let [loaded (set (map ns-name (all-ns)))
          host-namespace 'gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
          vars [#'gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test/attempt30-neutral-fixture-constructors-preserve-canonical-data
                #'gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test/attempt30-neutral-raw-request-is-consumer-input]
          counters (ref {:test 0 :pass 0 :fail 0 :error 0})]
      (binding [test/*report-counters* counters
                test/*test-out* (java.io.StringWriter.)]
        (test/test-vars vars))
      (let [result {:host-namespace-loaded? (contains? loaded host-namespace)
                    :semantic-test-names (mapv (comp :name meta) vars)
                    :semantic-counters @counters}]
        (println \"SH07-B51-ATTEMPT30-FRESH-CONSUMER\" (pr-str result))
        (System/exit (if (and (not (:host-namespace-loaded? result))
                               (pos? (get-in result [:semantic-counters :test]))
                               (pos? (get-in result [:semantic-counters :pass]))
                               (zero? (get-in result [:semantic-counters :fail]))
                               (zero? (get-in result [:semantic-counters :error])))
                      0
                      9)))))")

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b51_attempt30_producer_compact_verifier_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH07-B51 Attempt30 producer compact test is not on the classpath"
        {:id "SH07-B51-ATTEMPT30-PRODUCER-COMPACT-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B51-ATTEMPT30-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(defn- fresh-producer-compact-load-probe
  []
  (let [command ["clojure"
                 "-Sdeps"
                 "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
                 "-M"
                 "-e"
                 fresh-load-probe-expression]
        builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (io/file (str (repository-root))))
                  (.redirectErrorStream true))
        process (.start builder)
        completed? (.waitFor process 60 TimeUnit/SECONDS)]
    (letfn [(result [finished? terminated?]
              (with-open [stream (.getInputStream process)]
                {:completed? (and finished? terminated?)
                 :timed-out? (not finished?)
                 :alive? (.isAlive process)
                 :exit (when terminated? (.exitValue process))
                 :output (if (and terminated? (not (.isAlive process)))
                           (try
                             (String. (.readAllBytes stream)
                                      StandardCharsets/UTF_8)
                             (catch java.io.IOException _ ""))
                           "")}))]
      (if completed?
        (result true true)
        (do
          (.destroyForcibly process)
          (let [terminated? (.waitFor process 5 TimeUnit/SECONDS)]
            (when (.isAlive process)
              (.destroyForcibly process))
            (result false terminated?)))))))

(defn- fresh-probe-report
  [output]
  (let [marker "SH07-B51-ATTEMPT30-FRESH-CONSUMER "]
    (some->> (str/split-lines output)
             (keep (fn [line]
                     (when (str/starts-with? line marker)
                       (edn/read-string (subs line (count marker))))))
             first)))

(def ^:private fresh-isolation-test-names
  '[attempt30-neutral-fixture-constructors-preserve-canonical-data
    attempt30-neutral-raw-request-is-consumer-input])

(defn- test-inventory []
  (->> (ns-publics *ns*)
       (keep (fn [[symbol value]]
               (when (:test (meta value))
                 {:name symbol :line (:line (meta value))})))
       (sort-by (juxt :name :line))
       vec))

(deftest attempt30-neutral-fixture-constructors-preserve-canonical-data
  (let [{:keys [id-hashes raw-hash raw-keys carrier-identical?] :as observation}
        (isolated-fixture-observation)]
    (is (= isolated-id-hashes id-hashes)
        (pr-str (select-keys observation [:id-hashes])))
    (is (= isolated-raw-request-hash raw-hash)
        (pr-str (select-keys observation [:raw-hash])))
    (is (= [:artifact :authenticated-sh06-request
            :authenticated-sh06-verification-report :physical-invocation
            :predecessor-contract-carrier :predecessor-executable-descriptor
            :predecessor-raw-outcome :schema-version :scope
            :sh06-resolution-artifact :v18-executable-descriptor]
           raw-keys)
        (pr-str (select-keys observation [:raw-keys])))
    (is carrier-identical?)))

(deftest attempt30-neutral-raw-request-is-consumer-input
  (let [raw (fixture-support/raw-request isolated-carrier)]
    (is (= :gravity/sh07-b51-raw-adapter-request-linked-v4 (:artifact raw)))
    (is (= 4 (:schema-version raw)))
    (is (= :sh07-b51-vector-destructuring (:scope raw)))
    (is (identical? isolated-carrier (:v18-executable-descriptor raw)))
    (is (= isolated-raw-request-hash
           (bootstrap/reader-canonical-hash raw)))))

(deftest attempt30-compact-consumer-fresh-load-isolated
  (let [loaded-before (set (map ns-name (all-ns)))
        fixture-before (isolated-fixture-observation)
        inventory-before (test-inventory)
        probe (fresh-producer-compact-load-probe)
        loaded-after (set (map ns-name (all-ns)))
        fixture-after (isolated-fixture-observation)
        inventory-after (test-inventory)
        report (fresh-probe-report (:output probe))
        counters (or (:semantic-counters report) {})]
    (is (= loaded-before loaded-after)
        "The fresh consumer child must not mutate the preloaded parent namespaces")
    (is (identical? (:carrier fixture-before) (:carrier fixture-after))
        "Fixture support must retain the parent preloaded carrier identity")
    (is (= (dissoc fixture-before :carrier)
           (dissoc fixture-after :carrier))
        "Fixture support canonical values must survive the fresh child")
    (is (= inventory-before inventory-after)
        "The fresh consumer child must not alter the parent test inventory")
    (is (:completed? probe)
        (pr-str (select-keys probe [:completed? :timed-out? :alive? :exit])))
    (is (false? (:timed-out? probe))
        (pr-str (select-keys probe [:completed? :timed-out? :alive? :exit])))
    (is (false? (:alive? probe))
        (pr-str (select-keys probe [:completed? :timed-out? :alive? :exit])))
    (is (= 0 (:exit probe))
        (pr-str (select-keys probe [:completed? :timed-out? :alive? :exit])))
    (is (map? report)
        (pr-str (select-keys probe [:completed? :timed-out? :alive? :exit])))
    (is (false? (:host-namespace-loaded? report)) (pr-str report))
    (is (= fresh-isolation-test-names (:semantic-test-names report))
        (pr-str report))
    (is (= (count fresh-isolation-test-names) (or (:test counters) 0))
        (pr-str report))
    (is (pos? (or (:pass counters) 0)) (pr-str report))
    (is (zero? (or (:fail counters) 0)) (pr-str report))
    (is (zero? (or (:error counters) 0)) (pr-str report))))

(def forbidden-public-functions
  '#{sh07-b51-r1-linked-carrier-replay
     sh07-b51-r1-linked-category-rows
     sh07-b51-r1-linked-call-census
     sh07-b51-r1-linked-m2-call-census
     sh07-b51-r1-linked-function-calls
     sh07-b51-r1-linked-m2-function-calls
     sh07-b51-r1-linked-reachable
     sh07-b51-r1-linked-physical-census})

(defn carrier-mutations [carrier]
  [(assoc carrier :unknown true)
   (assoc carrier :domain
          :gravity/sh07-b51-v18-linked-executable-closure-carrier-v3)
   (assoc-in carrier [:compile-census-certificates 0 :role]
             :independent-verifier-census)
   (assoc-in carrier
             [:compile-census-certificates 0 :checker :algorithm-role]
             :m2-first-independent-exhaustive)
   (assoc-in carrier
             [:compile-census-certificates 0 :assumptions
              :source-emitter-bytes-immutable]
             false)
   (assoc-in carrier
             [:compile-census-certificates 0 :raw-source-transcript
              :artifact-id]
             (bootstrap/reader-canonical-hash :mutated-transcript))
   (update-in carrier
              [:compile-census-certificates 0 :claim :physical-census
               :maximum-binding-occupancy]
              inc)
   (update-in carrier
              [:compile-census-certificates 0 :claim :category-commitments
               0 :row-count]
              inc)
   (assoc-in carrier
             [:compile-census-certificates 0 :inputs
              :module-source-identities 0]
             (bootstrap/reader-canonical-hash :mutated-source))
   (assoc-in carrier
             [:compile-census-certificates 0 :inputs :m0-ids 0]
             (bootstrap/reader-canonical-hash :mutated-m0))
   (assoc-in carrier
             [:compile-census-certificates 0 :inputs :m2-ids 0]
             (bootstrap/reader-canonical-hash :mutated-m2))
   (assoc-in carrier [:link-manifest :edges 0 :owner-edge]
             :forbidden-owner-edge)
   (assoc-in carrier [:execution-bundle :public-roots 0 :function]
             'sh07-b51-verify-template)
   (assoc-in carrier
             [:execution-bundle :compile-census-certificate-ids 0]
             (bootstrap/reader-canonical-hash :mutated-certificate-id))
   (update-in carrier [:semantic-manifest :total-row-count] inc)
   (assoc carrier :semantic-root-hash
          (bootstrap/reader-canonical-hash :mutated-root))])

(deftest root1-accepts-the-exact-certificate-a-compact-carrier
  (let [carrier (accepted-carrier)]
    (is (true? (invoke 'sh07-b51-r1-certificate-a-valid?
                       (get-in carrier [:compile-census-certificates 0]))))
    (is (true? (invoke 'sh07-b51-r1-compact-carrier-v4? carrier)))
    (is (true? (invoke 'sh07-b51-executable-carrier-shape? carrier)))
    (testing "checker B is opaque to Root1 after its id joins M3-v2/S30"
      (is (true?
           (invoke
            'sh07-b51-r1-compact-carrier-v4?
            (assoc-in carrier [:compile-census-certificates 1 :unknown]
                      true)))))))

(deftest root1-compact-certificate-and-carrier-mutations-fail-closed
  (let [carrier (accepted-carrier)]
    (doseq [mutation (carrier-mutations carrier)]
      (is (false? (invoke 'sh07-b51-r1-compact-carrier-v4? mutation)))
      (is (false? (invoke 'sh07-b51-executable-carrier-shape? mutation))))))

(deftest root1-public-call-graph-routes-only-to-compact-verification
  (let [closure (call-closure-in (compact-call-graph)
                                 'sh07-b51-build-template)
        closure-heads
        (into #{}
              (mapcat (fn [name]
                        (if (= name
                               'sh07-b51-success-digest-replay-exact?)
                          #{}
                          (call-heads
                           (get @producer-definitions name)))))
              closure)
        forbidden-symbols
        (set
         (filter
          #(or (contains? forbidden-public-functions %)
               (re-find #"sh07-b51-r6" (str %))
               (re-find #"attempt30.*checker|checker.*attempt30" (str %)))
          closure-heads))]
    (is (contains? closure 'sh07-b51-r1-compact-carrier-v4?))
    (is (contains? closure 'sh07-b51-r1-certificate-a-valid?))
    (is (contains? closure 'sh07-b51-success-digest-replay-exact?))
    (is (not (contains? closure
                        'sh07-b51-success-digest-replay-exhaustive)))
    (is (contains?
         (get @producer-call-graph
              'sh07-b51-success-digest-replay-exact?)
         'sh07-b51-success-digest-replay-exhaustive))
    (is (empty? forbidden-symbols))))

(deftest root1-accepted-and-rejected-compact-paths-do-not-run-old-census
  (let [carrier (accepted-carrier)
        compact-closure
        (call-closure-in (compact-call-graph)
                         'sh07-b51-r1-compact-carrier-v4?)
        exhaustive-only
        (remove compact-closure
                (call-closure 'sh07-b51-r1-linked-carrier-replay))
        calls (atom [])
        replacements
        (into {}
              (for [name exhaustive-only
                    :let [variable (ns-resolve @runtime name)]
                    :when variable]
                [variable
                 (fn [& _]
                   (swap! calls conj name)
                   (throw (ex-info "old exhaustive replay was invoked"
                                   {:function name})))]))]
    (with-redefs-fn
      replacements
      #(do
         (is (true? (invoke 'sh07-b51-r1-compact-carrier-v4? carrier)))
         (is (false?
              (invoke 'sh07-b51-r1-compact-carrier-v4?
                      (assoc carrier :semantic-root-hash
                             (bootstrap/reader-canonical-hash
                              :rejected-no-scan)))))))
    (is (empty? @calls))))
