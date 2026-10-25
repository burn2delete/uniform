(ns gravity.self-hosting.sh03-projected-construction-policy-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Current-source seed feedback only. This never authenticates or renews pins.
(def ^:private root
  (delay
    (loop [directory (.getParentFile
                      (io/file (.toURI
                                (io/resource
                                 "gravity/self_hosting/sh03_projected_construction_policy_test.clj"))))]
      (if (.isFile (io/file directory "deps.edn")) directory
        (if-let [parent (.getParentFile directory)] (recur parent)
          (throw (ex-info "Repository root missing" {})))))))

(def ^:private current-plan
  (delay
    (let [file (io/file @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
          source-path (.getPath file)]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))
       source-path (slurp file)))))

(defn- execute-plan [plan function arguments]
  (with-redefs [bootstrap/sh03-reader-current-binding!
                (fn [& _] (throw (ex-info "Unexpected pinned-reader route" {})))]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-projected-construction-current-source-test
      :compiler-artifact-plan? true}
     plan function arguments)))

(defn- execute [function arguments]
  (execute-plan @current-plan function arguments))

(defn- input [text retain-trivia]
  (let [bytes (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
    [{:artifact :gravity/sh03-source-unit :schema-version 1
      :project-root-id (str "sha256:" (apply str (repeat 64 "0")))
      :logical-source-id "src/projected-construction.gravity"
      :source-content-hash (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
      :source-byte-count (alength bytes) :encoding :utf-8
      :actual-path-provenance {:path "/declared/projected-construction.gravity"
                               :extension ".gravity" :source-kind :gravity}}
     (mapv #(bit-and 255 %) bytes)
     {:artifact :gravity/sh03-reader-policy :schema-version 1
      :retain-trivia retain-trivia
      :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}]))

(defn- projected-and-actual [text retain-trivia]
  (let [[unit bytes policy :as arguments] (input text retain-trivia)
        decoded (execute 'sh03-decode-source-bytes [unit bytes])
        scanned (execute 'sh03-scan-tokens [(:characters decoded)])
        tokens (:tokens scanned)
        parsed (execute 'sh03-parse-tokens [tokens policy])
        result (execute 'sh03-read-source-unit arguments)
        carrier (bootstrap/p15-s23-trusted-carrier-validation
                 result :default-only 167772160 2048 1048576)]
    (is (= :accepted (:status decoded)))
    (is (= :accepted (:status scanned)))
    (is (= :accepted (:status parsed)))
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (is (= :passed (:status carrier)))
    [(execute 'sh03-projected-result-node-count [bytes policy tokens parsed])
     (:observed-nodes carrier)]))

(deftest compiler-source-policy-couples-intake-and-raw-preimage-width
  (let [bounds (execute 'sh03-bounds-record [])
        boundary (execute 'sh03-execution-boundary-record [])
        budget (:result-carrier-node-budget bounds)
        envelope (+ (:fixed-nodes budget)
                    (* (:nodes-per-source-byte budget) (:maximum-source-bytes bounds))
                    (* (:nodes-per-token budget) (:maximum-tokens bounds))
                    (* (:nodes-per-form budget) (:maximum-forms bounds))
                    (* (:nodes-per-reader-tag budget) (:maximum-reader-tags bounds)))]
    (is (= :gravity/sh03-compiler-source-v1 (:resource-policy bounds)))
    (is (= [4194304 4194304 524288 262144 4194304]
           (mapv bounds [:maximum-source-bytes :maximum-characters :maximum-tokens
                         :maximum-forms :maximum-result-carrier-width])))
    (is (= 4194304 (get-in boundary [:gravity-input-responsibility :maximum-source-bytes])
           (get-in boundary [:clojure-boundary-responsibility :maximum-width])))
    (is (= 537985024 envelope (:derived-maximum-nodes budget)))
    (is (= 167772160 (:admitted-maximum-nodes budget)
           (:maximum-result-carrier-nodes bounds)))
    (is (> envelope (:admitted-maximum-nodes budget)))
    (is (= :prospective-physical-node-count (:construction-guard budget)))
    (is (= [512 65536 1048576 67108864 8437834]
           (mapv bounds [:maximum-delimiter-depth :maximum-container-width
                         :maximum-canonical-scalar-bytes
                         :maximum-canonical-total-scalar-bytes
                         :maximum-semantic-work-units])))))

(deftest projected-construction-covers-real-reader-products
  (doseq [retain-trivia [false true]
          fixture ["complete-reader-surface" "unicode-supplementary"
                   "numeric-work-boundary" "numeric-semantic-work-deferred"
                   "numeric-set-distinct-deferred" "depth-512"]]
    (testing (str fixture " retain-trivia=" retain-trivia)
      (let [text (slurp
                  (io/file @root
                           (str "bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/"
                                fixture ".gravity")))
            [projected actual] (projected-and-actual text retain-trivia)]
        (is (>= projected actual))))))

(deftest projected-construction-variable-charge-matches-physical-growth
  ;; The trusted host walker is independent of the prospective formula.
  ;; Subtracting two nonempty products removes the fixed reserve, so missing
  ;; per-row charges cannot hide behind that reserve in these fixtures.
  (let [sample (str "; retained comment\n"
                    "[nil true false 42 0xff 2/3 1/0 1.25e2 \"a\\nb\" \\newline :a/b x/y]\n"
                    "{:a [1 2] :b #{3 4}} '(x) @x ^:private x `(~x ~@ys)\n"
                    "#uuid \"550e8400-e29b-41d4-a716-446655440000\"\n"
                    "#inst \"2024-01-02T03:04:05Z\"\n"
                    (apply str (repeat 257 "9")) "\n")]
    (doseq [retain-trivia [false true]]
      (let [[projected-one actual-one] (projected-and-actual sample retain-trivia)
            [projected-two actual-two] (projected-and-actual (str sample sample)
                                                                 retain-trivia)]
        (is (= (- actual-two actual-one) (- projected-two projected-one)))))))

(deftest projected-construction-counts-shared-occurrences
  (let [shared {:kind :symbol :name-codepoints [120 121]}
        once {:first shared}
        twice {:first shared :second shared}
        physical #(-> (bootstrap/p15-s23-trusted-carrier-validation
                       % :default-only 1000 20 100)
                      :observed-nodes)]
    (is (= (physical once)
           (execute 'sh03-construction-atomic-nodes [once])))
    (is (= (physical twice)
           (execute 'sh03-construction-atomic-nodes [twice])))
    (is (= (inc (physical shared)) (- (physical twice) (physical once))))))

(deftest projected-construction-prepays-temporary-indexes-and-filter
  (doseq [retain-trivia [false true]]
    (let [[unit bytes policy :as arguments] (input "[1 :a \"xy\"] ; trivia\n" retain-trivia)
          decoded (execute 'sh03-decode-source-bytes [unit bytes])
          scanned (execute 'sh03-scan-tokens [(:characters decoded)])
          tokens (:tokens scanned)
          parsed (execute 'sh03-parse-tokens [tokens policy])
          result (execute 'sh03-read-source-unit arguments)
          entries (:semantic-value-table result)
          token-index (into {} (map (fn [entry]
                                     [(:token-id entry)
                                      {:artifact :gravity/semantic-value-reference
                                       :schema-version 1 :value-id (:value-id entry)
                                       :field :descriptor}]) entries))
          form-index (into {} (map (juxt :form-id identity) entries))
          scratch (cond-> [token-index form-index]
                    (not retain-trivia) (conj (filterv #(not (:trivia? %)) tokens)))
          physical #(-> (bootstrap/p15-s23-trusted-carrier-validation
                         % :default-only 167772160 2048 4194304)
                        :observed-nodes)
          final (execute 'sh03-projected-result-node-count [bytes policy tokens parsed])
          construction (execute 'sh03-projected-construction-node-count
                                [bytes policy tokens parsed])]
      (is (= :accepted (:status result)))
      (is (= (reduce + (map physical scratch)) (- construction final))))))

(deftest projected-construction-refuses-before-product-construction
  (let [maximum (:maximum-result-carrier-nodes (execute 'sh03-bounds-record []))
        at-limit (execute 'sh03-construction-rejection [maximum maximum])
        below-required (execute 'sh03-construction-rejection [maximum (dec maximum)])
        ;; Seed-only fault injection changes the count result, not policy or
        ;; source authority. Any reached accepted-product constructor fails.
        forbidden '[sh03-filter-output-tokens sh03-semantic-value-table
                    sh03-semantic-value-token-index sh03-semantic-value-form-index
                    sh03-compact-token-records sh03-compact-form-records
                    sh03-literal-records sh03-deferred-literal-records
                    sh03-reader-extension-invocation-records sh03-source-map-record
                    sh03-accepted-semantic-template sh03-top-level-records
                    sh03-top-level-value-references]
        plan (reduce (fn [plan function]
                       (assoc-in plan [:functions function :instructions]
                                 [{:op :unreachable-accepted-constructor}]))
                     (assoc-in @current-plan
                               [:functions 'sh03-projected-result-node-count :instructions]
                               [{:op :quote :value (inc maximum)}])
                     forbidden)
        result (execute-plan plan 'sh03-read-source-unit (input "[1 2]\n" true))]
    (is (nil? at-limit))
    (is (= :result-construction-node-limit (:reason below-required)))
    (is (= :rejected (:status result)))
    (is (= "C2-HASH" (get-in result [:diagnostics 0 :id])))
    (is (= :result-construction-node-limit
           (get-in result [:diagnostics 0 :reader-state :reason])))
    (is (= :before-accepted-products
           (get-in result [:diagnostics 0 :facts :construction-phase])))
    (is (= [] (:token-stream result) (:form-tree result)))
    (is (false? (get-in result [:diagnostics 0 :facts :result-committed?])))))

(defn- with-reader-phase-observer [operation]
  (let [call-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-call-scoped)
        original @call-var
        phases '#{sh03-decode-source-bytes sh03-scan-tokens sh03-parse-tokens
                  sh03-projected-construction-node-count sh03-semantic-value-table
                  sh03-compact-token-records sh03-compact-form-records
                  sh03-source-map-record sh03-digest-requests}]
    (with-redefs-fn
      {call-var
       (fn [runtime plan env instruction scope]
         (let [function (:function instruction)]
           (if (contains? phases function)
             (let [started (System/nanoTime)]
               (println (pr-str {:reader-phase function :event :begin}))
               (let [result (original runtime plan env instruction scope)]
                 (println (pr-str {:reader-phase function :event :end
                                   :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
                                   :status (when (map? result) (:status result))}))
                 result))
             (original runtime plan env instruction scope))))}
      operation)))

(deftest authenticated-reader-accepts-real-effects-compiler-input
  ;; Real authenticated intake and fresh reconstruction, with the stock trivia
  ;; mode. This is reader evidence, not C8/C9 or module-migration acceptance.
  (let [file (io/file @root "bootstrap/gravity/src/gravity/effects.gravity")
        path (.getPath file)
        bytes (bootstrap/sh03-reader-read-target-source-bytes! path)
        target-calls (atom 0)
        original bootstrap/read-source-form-records
        resolved
        (with-redefs [bootstrap/read-source-form-records
                      (fn [source text]
                        (when (= (.getCanonicalPath (io/file source))
                                 (.getCanonicalPath file))
                          (swap! target-calls inc)
                          (throw (ex-info "Target source reached host reader" {})))
                        (original source text))]
          (with-reader-phase-observer
            #(bootstrap/sh03-reader-resolved-result!
              path bytes (bootstrap/reader-project-context-for-source path)
              bootstrap/standard-reader-options)))
        raw (:raw-result resolved)
        report (:verification-report resolved)]
    (println (pr-str {:real-reader-input :effects
                      :source-byte-count (alength bytes)
                      :source-content-hash (str "sha256:" (bootstrap/sha256-bytes-hex bytes))
                      :status (:status raw) :tokens (count (:token-stream raw))
                      :forms (count (:form-tree raw))
                      :fresh-replay (:verified? report)
                      :diagnostics (mapv #(select-keys % [:id :reader-state :facts])
                                         (:diagnostics raw))}))
    (is (= 1677424 (alength bytes)))
    (is (> (alength bytes) 1048576))
    (is (= :accepted (:status raw) (:status (:result resolved))))
    (is (true? (:verified? report)))
    (is (> (count (:token-stream raw)) 131072))
    (is (> (count (:form-tree raw)) 65536))
    (is (= :gravity/sh03-compiler-source-v1 (get-in raw [:bounds :resource-policy])))
    (is (zero? @target-calls))))
