(ns gravity.self-hosting.sh07-b51-attempt30-producer-compact-verifier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test]))

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
      (intern runtime-ns 'sort-by-pr-str
              (fn [values] (apply list (sort-by pr-str values))))
      (doseq [form definitions] (intern runtime-ns (second form)))
      (doseq [form definitions] (eval form)))
    runtime-ns))

(def runtime (delay (producer-runtime)))

(defn invoke [function & arguments]
  (apply @(ns-resolve @runtime function) arguments))

(defn private-host-test-value [name]
  (var-get
   (or (ns-resolve
        'gravity.self-hosting.sh07-b51-attempt30-linked-carrier-host-test
        name)
       (throw (ex-info "missing Attempt30 host-test fixture" {:name name})))))

(defn accepted-carrier []
  (:carrier @(private-host-test-value 'accepted-fixture)))

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
