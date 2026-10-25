(ns gravity.self-hosting.sh03-phase-lifetime-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; Byte-for-byte snapshot of immutable commit 5bbf30835625f835e5075373dd77407067362e73.
;; Keep the regression self-contained in fresh/shallow checkouts.
(def ^:private baseline-relative-path
  "bootstrap/clojure/fixtures/self-hosting/sh-03/policy/sh03-phase-lifetime-5bb-reader.gravity")
(def ^:private baseline-reader-hash
  "sha256:1b66e5f5b44669901d56a592685633a647d828074e8b1833597e3ffdf853d443")
(def ^:private reader-relative-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")

(def ^:private root
  (delay
    (loop [directory (.getParentFile
                      (io/file (.toURI
                                (io/resource
                                 "gravity/self_hosting/sh03_phase_lifetime_test.clj"))))]
      (if (.isFile (io/file directory "deps.edn")) directory
        (if-let [parent (.getParentFile directory)] (recur parent)
          (throw (ex-info "Repository root missing" {})))))))

(defn- compile-source [text]
  (let [path (.getPath (io/file @root reader-relative-path))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
     path text)))

(def ^:private current-plan
  (delay (compile-source (slurp (io/file @root reader-relative-path)))))

(def ^:private baseline-plan
  (delay
    ;; This invokes no subprocess, target-reader oracle or authenticated route.
    (let [file (io/file @root baseline-relative-path)
          _ (when-not (= 269031 (.length file))
              (throw (ex-info "Frozen phase-lifetime baseline byte bound mismatch" {})))
          out (slurp file :encoding "UTF-8")]
      (when-not (= baseline-reader-hash (str "sha256:" (bootstrap/sha256-hex out)))
        (throw (ex-info "Frozen reader baseline identity mismatch" {})))
      (compile-source out))))

(defn- execute-plan [plan function arguments]
  (with-redefs [bootstrap/sh03-reader-current-binding!
                (fn [& _] (throw (ex-info "Unexpected authenticated reader route" {})))]
    (bootstrap/p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh03-phase-lifetime-current-source-test
      :compiler-artifact-plan? true}
     plan function arguments)))

(defn- input-bytes [bytes retain-trivia]
  [{:artifact :gravity/sh03-source-unit :schema-version 1
    :project-root-id (str "sha256:" (apply str (repeat 64 "0")))
    :logical-source-id "src/phase-lifetime.gravity"
    :source-content-hash (str "sha256:"
                             (bootstrap/sha256-bytes-hex
                              (byte-array (map unchecked-byte bytes))))
    :source-byte-count (count bytes) :encoding :utf-8
    :actual-path-provenance {:path "/declared/phase-lifetime.gravity"
                             :extension ".gravity" :source-kind :gravity}}
   bytes
   {:artifact :gravity/sh03-reader-policy :schema-version 1
    :retain-trivia retain-trivia
    :enabled-reader-tags [[105 110 115 116] [117 117 105 100]]}])

(defn- input [text retain-trivia]
  (input-bytes (mapv #(bit-and 255 %)
                    (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8))
               retain-trivia))

(defn- compare-baseline [arguments]
  (let [expected (execute-plan @baseline-plan 'sh03-read-source-unit arguments)
        actual (execute-plan @current-plan 'sh03-read-source-unit arguments)]
    ;; Complete equality includes all preimages, source maps, semantic values,
    ;; deferred facts, resource declarations and structured rejection details.
    (is (= expected actual))
    actual))

(deftest phase-lifetime-complete-output-matches-frozen-reader
  (doseq [retain-trivia [false true]
          fixture ["complete-reader-surface" "unicode-supplementary" "newline-crlf"
                   "numeric-semantic-work-deferred" "numeric-work-boundary"]]
    (testing (str fixture " retain-trivia=" retain-trivia)
      (let [text (slurp (io/file @root
                                (str "bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/"
                                     fixture ".gravity")))
            result (compare-baseline (input text retain-trivia))]
        (is (= :accepted (:status result)))))))

(deftest phase-lifetime-rejections-match-frozen-reader
  (doseq [retain-trivia [false true]
          text ["[1)" "\"bad\\q\"" "1e+" "#unknown 1" "{:a}" "^1 x" "~x"]]
    (let [result (compare-baseline (input text retain-trivia))]
      (is (= :rejected (:status result)))))
  (doseq [bytes [[128] [65 13 10 237 160 128] [240 144 128] [244 144 128 128]]]
    (let [result (compare-baseline (input-bytes bytes true))]
      (is (= :rejected (:status result)))
      (is (= "C2-ENCODING" (get-in result [:diagnostics 0 :id])))))
  (let [[unit bytes policy] (input "[1]" true)]
    (doseq [arguments [[(assoc unit :source-byte-count 100) bytes policy]
                      [unit bytes (assoc policy :retain-trivia :invalid)]
                      [(assoc unit :encoding :utf-16) bytes policy]]]
      (is (= :rejected (:status (compare-baseline arguments)))))))

(def ^:private phase-functions
  '#{sh03-decode-source-bytes sh03-scan-tokens sh03-parse-tokens
     sh03-construct-parsed-source-unit})

(defn- observe-phases [events operation]
  (let [call-var (ns-resolve 'gravity.bootstrap
                            'p15-s23-stage2-runtime-execute-call-scoped)
        original @call-var]
    (with-redefs-fn
      {call-var
       (fn [runtime plan env instruction scope]
         (when (contains? phase-functions (:function instruction))
           (swap! events conj
                  {:function (:function instruction)
                   :phase (get env 'phase)
                   :env-keys (set (keys env))
                   :characters (get env 'characters)
                   :tokens (get env 'tokens)
                   :parsed (get env 'parsed)}))
         (original runtime plan env instruction scope))}
      operation)))

(defn- assert-phase-readsets [events]
  (let [expected-keys '#{source-unit source-bytes reader-policy phase characters tokens parsed}
        expected-phases '{sh03-decode-source-bytes :decode
                          sh03-scan-tokens :scan
                          sh03-parse-tokens :parse
                          sh03-construct-parsed-source-unit :construct}]
    (doseq [{:keys [function phase env-keys characters tokens parsed]} events]
      ;; This observes the caller interpreter environment, not just the formal
      ;; callee arguments. Old nested lets would retain decoded/characters here.
      (is (= expected-keys env-keys))
      (is (= (get expected-phases function) phase))
      (is (not-any? env-keys '[decoded scanned parsed-result]))
      (case phase
        :decode (is (every? nil? [characters tokens parsed]))
        :scan (do (is (every? vector? characters))
                  (is (every? nil? [tokens parsed])))
        :parse (do (is (nil? characters))
                   (is (nil? parsed))
                   (is (every? #(contains? % :token-id) tokens)))
        :construct (do
                     (is (nil? characters))
                     (is (every? #(contains? % :token-id) tokens))
                     (is (= :accepted (:status parsed))))))))

(deftest phase-lifetime-loop-has-only-original-source-outer-readset
  (let [definition (get-in @current-plan [:functions 'sh03-read-source-unit])
        loops (filter #(and (map? %) (= :loop (:op %)))
                      (tree-seq coll? seq (:instructions definition)))]
    (is (= '[source-unit source-bytes reader-policy] (:params definition)))
    (is (= 1 (count loops)))
    (is (= '[phase characters tokens parsed] (mapv :name (:bindings (first loops)))))
    (let [events (atom [])
          result (observe-phases
                  events #(execute-plan @current-plan 'sh03-read-source-unit
                                        (input "[1 :a \"🙂\"] ; trivia\n" true)))]
      (is (= :accepted (:status result)))
      (is (= '[sh03-decode-source-bytes sh03-scan-tokens sh03-parse-tokens
               sh03-construct-parsed-source-unit]
             (mapv :function @events)))
      (assert-phase-readsets @events))))

(deftest phase-lifetime-fresh-replay-runs-each-phase-again
  (let [events (atom [])
        arguments (input "[nil 2/3 \"🙂\"]\r\n" false)
        report
        (observe-phases
         events
         #(let [result (execute-plan @current-plan 'sh03-read-source-unit arguments)]
            (execute-plan @current-plan 'sh03-verify-reader-result
                          (conj arguments result))))]
    (is (true? (:verified? report)))
    (is (= :accepted (:status report)))
    (is (= (zipmap phase-functions (repeat 2))
           (frequencies (map :function @events))))
    (assert-phase-readsets @events)))
