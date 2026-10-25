(ns gravity.self-hosting.sh03-complete-construction-census-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Current-source seed observations only; these counts confer no reader,
;; compiler-module, migration, fresh-artifact, or release acceptance.
(def ^:private root
  (delay
    (loop [directory (.getParentFile
                      (io/file (.toURI
                                (io/resource
                                 "gravity/self_hosting/sh03_complete_construction_census_test.clj"))))]
      (if (.isFile (io/file directory "deps.edn")) directory
        (if-let [parent (.getParentFile directory)] (recur parent)
          (throw (ex-info "Repository root missing" {})))))))

(def ^:private reader-file
  (delay (io/file @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")))

(def ^:private current-plan
  (delay
    (let [path (.getPath @reader-file)]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
       path (slurp @reader-file)))))

(defn- execute-plan [plan function arguments]
  (with-redefs [bootstrap/sh03-reader-current-binding!
                (fn [& _] (throw (ex-info "Unexpected authenticated route" {})))]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-complete-census-current-source-test
      :compiler-artifact-plan? true}
     plan function arguments)))

(defn- execute [function arguments]
  (execute-plan @current-plan function arguments))

(def ^:private accepted-product-constructors
  '#{sh03-filter-output-tokens sh03-semantic-value-table
     sh03-semantic-value-token-index sh03-semantic-value-form-index
     sh03-compact-token-records sh03-compact-form-records sh03-literal-records
     sh03-deferred-literal-records sh03-reader-extension-invocation-records
     sh03-source-map-record sh03-accepted-semantic-template
     sh03-top-level-records sh03-top-level-value-references sh03-digest-requests})

(defn- count-only [operation]
  (let [dispatch (ns-resolve 'gravity.bootstrap
                            'p15-s23-stage2-runtime-execute-function-scoped)
        original @dispatch]
    (with-redefs-fn
      {dispatch
       (fn [runtime plan callee arguments scope]
         (when (contains? accepted-product-constructors callee)
           (throw (ex-info "Census reached a reader-product constructor"
                           {:function callee})))
         (original runtime plan callee arguments scope))}
      operation)))

(defn- input [text retain-trivia]
  (let [array (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)
        path "/declared/complete-census.gravity"
        context {:project-root-id (str "sha256:" (apply str (repeat 64 "0")))
                 :project-relative-path "src/complete-census.gravity"}]
    [(bootstrap/sh03-reader-input-source-unit path array context)
     (mapv #(bit-and 255 %) array)
     {:artifact :gravity/sh03-reader-policy :schema-version 1
      :retain-trivia retain-trivia
      :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}]))

(defn- parsed-input [[unit bytes policy]]
  (let [decoded (execute 'sh03-decode-source-bytes [unit bytes])
        scanned (execute 'sh03-scan-tokens [(:characters decoded)])
        tokens (:tokens scanned)
        parsed (execute 'sh03-parse-tokens [tokens policy])]
    (is (= :accepted (:status decoded)))
    (is (= :accepted (:status scanned)))
    (is (= :accepted (:status parsed)))
    [bytes policy tokens parsed]))

(defn- physical-nodes [value]
  (let [checked (bootstrap/p15-s23-trusted-carrier-validation
                 value :default-only 167772160 2048 4194304)]
    (is (= :passed (:status checked)))
    (:observed-nodes checked)))

(defn- observed-small [text retain-trivia]
  (let [arguments (input text retain-trivia)
        [_ _ tokens _ :as parsed] (parsed-input arguments)
        census (count-only #(execute 'sh03-complete-construction-census parsed))
        result (execute 'sh03-read-source-unit arguments)
        entries (:semantic-value-table result)
        ;; Construct the actual scratch schemas independently in the host and
        ;; count every occurrence, including repeated entry object references.
        token-index (into {} (map (fn [entry]
                                   [(:token-id entry)
                                    {:artifact :gravity/semantic-value-reference
                                     :schema-version 1 :value-id (:value-id entry)
                                     :field :descriptor}]) entries))
        form-index (into {} (map (juxt :form-id identity) entries))
        scratch (cond-> [token-index form-index]
                  (not retain-trivia) (conj (filterv #(not (:trivia? %)) tokens)))]
    (is (= :accepted (:status result)))
    (is (true? (:complete? census)))
    (is (= 537985024 (:limit census)))
    (is (>= (:projected-result-nodes census) (physical-nodes result)))
    (is (= (reduce + (map physical-nodes scratch))
           (- (:accepted-construction-nodes census) (:projected-result-nodes census))))
    [census (physical-nodes result)]))

(deftest complete-census-matches-independent-final-and-scratch-growth
  (let [sample (str "; retained trivia\n"
                    "[nil true false 42 0xff 2/3 1/0 1.25e2 \"a\\nb\" \\newline :a/b x/y]\n"
                    "{:a [1 2] :b #{3 4}} '(x) @x ^:private x `(~x ~@ys)\n"
                    "#uuid \"550e8400-e29b-41d4-a716-446655440000\"\n"
                    "#inst \"2024-01-02T03:04:05Z\"\n"
                    (apply str (repeat 257 "9")) "\n")]
    (doseq [retain-trivia [false true]]
      (let [[one actual-one] (observed-small sample retain-trivia)
            [two actual-two] (observed-small (str sample sample) retain-trivia)]
        ;; Differencing removes the fixed reserve rather than allowing it to
        ;; hide omitted variable rows or digest occurrences.
        (is (= (- actual-two actual-one)
               (- (:projected-result-nodes two) (:projected-result-nodes one))))))))

(deftest complete-census-does-not-grant-production-construction-capacity
  (let [parsed (parsed-input (input "[1 2]\n" true))
        bounds (execute 'sh03-bounds-record [])
        maximum (:maximum-result-carrier-nodes bounds)
        production-plan (assoc-in @current-plan
                                  [:functions 'sh03-projected-result-node-count :instructions]
                                  [{:op :quote :value (inc maximum)}])]
    (doseq [diagnostic-limit [1 537985024]]
      (let [plan (assoc-in production-plan [:functions 'sh03-bounds-record :instructions]
                           [{:op :quote
                             :value (assoc-in bounds
                                              [:result-carrier-node-budget :derived-maximum-nodes]
                                              diagnostic-limit)}])
            census (count-only #(execute-plan plan 'sh03-complete-construction-census parsed))
            production (count-only #(execute-plan plan 'sh03-projected-construction-node-count parsed))
            rejection (execute-plan plan 'sh03-construction-rejection [production maximum])]
        (is (= diagnostic-limit (:limit census)))
        (is (= (> diagnostic-limit 1) (:complete? census)))
        (is (= (+ maximum 3) production))
        (is (= :result-construction-node-limit (:reason rejection)))
        (is (= maximum (get-in rejection [:facts :maximum-accepted-construction-nodes])))
        (is (= :refused-prefix (get-in rejection [:facts :construction-count-scope])))))))

(defn- execute-phase [function arguments]
  (let [started (System/nanoTime)]
    (println (pr-str {:complete-census-phase function :event :begin}))
    (flush)
    (let [result (execute function arguments)]
      (println (pr-str {:complete-census-phase function :event :end
                        :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
                        :status (when (map? result) (:status result))}))
      (flush)
      result)))

(defn- real-source-census [label relative expected-bytes expected-hash estimated-tokens estimated-forms]
  (let [file (io/file @root relative)
        path (.getPath file)
        array (bootstrap/sh03-reader-read-target-source-bytes! path)
        hash (str "sha256:" (bootstrap/sha256-bytes-hex array))
        reader-array (java.nio.file.Files/readAllBytes (.toPath @reader-file))
        reader-hash (str "sha256:" (bootstrap/sha256-bytes-hex reader-array))
        unit (bootstrap/sh03-reader-input-source-unit
              path array (bootstrap/reader-project-context-for-source path))
        policy (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        bytes (mapv #(bit-and 255 %) array)
        original bootstrap/read-source-form-records
        census
        (with-redefs [bootstrap/read-source-form-records
                      (fn [source text]
                        (when (= (.getCanonicalPath file)
                                 (.getCanonicalPath (io/file source)))
                          (throw (ex-info "Real census input reached host parser" {})))
                        (original source text))]
          (count-only
           #(let [tokens
                  (let [decoded (execute-phase 'sh03-decode-source-bytes [unit bytes])]
                    (is (= :accepted (:status decoded)))
                    (is (= expected-bytes (:source-byte-count decoded)))
                    (let [scanned (execute-phase 'sh03-scan-tokens [(:characters decoded)])]
                      (is (= :accepted (:status scanned)))
                      (:tokens scanned)))
                  parsed (execute-phase 'sh03-parse-tokens [tokens policy])]
              (is (= :accepted (:status parsed)))
              (is (< 131072 (count tokens) 524289))
              (is (< 65536 (count (:form-tree parsed)) 262145))
              (let [result (execute-phase 'sh03-complete-construction-census
                                          [bytes policy tokens parsed])]
                (println (pr-str {:real-complete-construction-census label
                                  :source-byte-count (alength array)
                                  :source-content-hash hash :reader-source-content-hash reader-hash
                                  :tokens (count tokens) :forms (count (:form-tree parsed))
                                  :planning-estimates {:tokens estimated-tokens :forms estimated-forms}
                                  :root-forms (count (:top-level-form-ids parsed))
                                  :retain-trivia (:retain-trivia policy)
                                  :node-count-unit :logical-physical-value-occurrences
                                  :heap-allocation-measurement? false
                                  :physical-copies {:output-tokens 2 :output-forms 2 :semantic-values 2
                                                    :source-maps 1 :root-ids 4
                                                    :temporary-token-index 1 :temporary-form-index 1}
                                  :census result
                                  :production-maximum-construction-nodes 167772160
                                  :acceptance-claimed? false}))
                (flush)
                result))))]
    (is (= expected-bytes (alength array)))
    (is (= expected-hash hash (:source-content-hash unit)))
    (is (= reader-hash (get-in @current-plan [:source :sha256])))
    (is (true? (:retain-trivia policy)))
    (is (= 537985024 (:limit census)))
    (is (true? (:complete? census)))
    (is (<= (:projected-result-nodes census) (:accepted-construction-nodes census)
            (:limit census)))))

(deftest complete-construction-census-real-effects
  (real-source-census
   :effects "bootstrap/gravity/src/gravity/effects.gravity" 1677424
   "sha256:52f7e9380f6da26c03eac2ef8113e776391735961e5aae3908dc8a4974f94f71"
   417156 208465))

(deftest complete-construction-census-largest-c7
  (real-source-census
   :c7 "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity" 2016051
   "sha256:e61bb053a468270e016c6b5f5560dbdfa5129da084f2039066d82a0412d87e9a"
   472738 236101))
