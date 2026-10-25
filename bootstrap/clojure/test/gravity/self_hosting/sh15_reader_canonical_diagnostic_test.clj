(ns gravity.self-hosting.sh15-reader-canonical-diagnostic-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-absent-layout-location-test]))

(def ^:private compile-plan
  #'gravity.self-hosting.sh15-absent-layout-location-test/compile-plan)
(def ^:private root
  (delay (loop [p (.getParentFile (io/file (io/resource
                 "gravity/self_hosting/sh15_reader_canonical_diagnostic_test.clj")))]
           (if (.isFile (io/file p "deps.edn")) p (recur (.getParentFile p))))))
(def ^:private c15-path
  "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity")
(def ^:private c15-plan (delay (compile-plan c15-path)))
(def ^:private reader-plan
  (delay (compile-plan "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh15-reader-construction-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- c15 [function arguments] (invoke @c15-plan function arguments))
(defn- path [family extension]
  (.getPath (io/file @root (str "bootstrap/clojure/fixtures/self-hosting/sh-15/rejected/"
                               "canonical-reader-" family "." extension))))
(defn- original [family extension & [physical-path]]
  (let [source-path (path family extension)
        bytes (java.nio.file.Files/readAllBytes (.toPath (io/file source-path)))
        unit (bootstrap/sh03-reader-input-source-unit
              source-path bytes (bootstrap/reader-project-context-for-source source-path))
        unit (if physical-path (assoc-in unit [:actual-path-provenance :path] physical-path) unit)
        policy (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        result (invoke @reader-plan 'sh03-read-source-unit
                       [unit (mapv #(bit-and 255 %) bytes) policy])
        diagnostic (first (:diagnostics result))]
    {:result result :raw diagnostic
     :event (bootstrap/sh03-reader-resolve-diagnostic-id source-path diagnostic)}))
(defn- fields [diagnostic]
  [(:id diagnostic) (:severity diagnostic) (:stage diagnostic)
   (assoc (:primary diagnostic) :syntax-id nil) (:related diagnostic)
   (:origin-chain diagnostic) nil nil (:facts diagnostic)
   (:remediation-records diagnostic) (:redactions diagnostic)])
(defn- request-for [arguments]
  {:ordinal 0 :purpose :c15-diagnostic-id :domain :gravity/c15-diagnostic-id-v1
   :preimage {:rule (nth arguments 0) :primary-artifact (get (nth arguments 3) :artifact)
              :stage (nth arguments 2) :facts (nth arguments 8)}})

(deftest original-reader-diagnostics-construct-real-private-c15-data
  (doseq [[family rule] [["delimiter" "C2-DELIMITER"] ["duplicate-set" "C2-SET"]]]
    (let [{:keys [result raw event]} (original family "gravity")
          before (pr-str event) arguments (fields event)
          candidate (c15 'build-c15-diagnostic arguments)]
      (is (= :rejected (:status result)))
      (is (= rule (:id event)))
      (is (string? (:diagnostic-id event)))
      (is (not (contains? event :diagnostic-id-request)))
      (is (map? (:diagnostic-id-request raw)))
      (is (= {:digest-ref 0} (:diagnostic-id candidate)))
      (is (= (request-for arguments) (:diagnostic-id-request candidate)))
      (doseq [[field index] [[:rule 0] [:severity 1] [:stage 2] [:primary 3]
                             [:related 4] [:origin-chain 5] [:profile 6] [:target 7]
                             [:facts 8] [:remediation 9] [:redactions 10]]]
        (is (= (nth arguments index) (get candidate field)) field))
      (is (map? (:contract candidate)))
      (is (= before (pr-str event)))
      (when (= family "duplicate-set")
        (is (seq (:related event)))))))

(defn- failure [result] (first (:diagnostics result)))

(deftest private-construction-rejects-malformed-typed-fields
  (let [arguments (fields (:event (original "duplicate-set" "gravity")))
        primary (nth arguments 3)
        cases [[(assoc arguments 0 "") "C15-SCHEMA"]
               [(assoc arguments 1 :unknown) "C15-SCHEMA"]
               [(assoc arguments 2 nil) "C15-SCHEMA"]
               [(assoc-in arguments [3 :span :start :line] 99) "C15-SPAN"]
               [(assoc-in arguments [3 :span :start :column-unit] :utf16) "C15-SPAN"]
               [(assoc arguments 3 (dissoc primary :syntax-id)) "C15-SPAN"]
               [(assoc-in arguments [4 0 :span :byte-start] 99) "C15-SPAN"]
               [(assoc arguments 5 [{:kind :generated}]) "C15-ORIGIN"]
               [(assoc arguments 6 "meta") "C15-SCHEMA"]
               [(assoc arguments 8 "untyped prose") "C15-FACTS"]
               [(assoc arguments 8 {"untyped-key" 1}) "C15-FACTS"]
               [(assoc arguments 9 []) "C15-REMEDIATION"]
               [(assoc arguments 9 [{:message "no category"}]) "C15-REMEDIATION"]
               [(assoc arguments 10 :not-evaluated) "C15-SCHEMA"]]]
    (doseq [[input rule] cases]
      (let [result (c15 'build-c15-diagnostic input) diagnostic (failure result)]
        (is (= :rejected (:status result)))
        (is (= rule (:rule diagnostic)))
        (is (= :compiler-diagnostics (:stage diagnostic)))
        (is (contains? diagnostic :offending-diagnostic-id))
        (is (contains? diagnostic :artifact-id))
        (is (keyword? (:schema-field diagnostic)))
        (is (= {:digest-ref 0} (:diagnostic-id diagnostic)))))))

(deftest construction-preflights-input-and-expanded-private-record
  (let [arguments (fields (:event (original "delimiter" "gravity")))
        calls (atom 0)
        opaque (reify Object (toString [_] (swap! calls inc)
                               (throw (ex-info "not renderable" {}))))
        wide (into {} (map #(vector (keyword (str "k" %)) 1) (range 513)))
        deep (nth (iterate vector :leaf) 130)]
    (doseq [facts [opaque wide deep (list :not-canonical)]]
      (let [result (c15 'build-c15-diagnostic (assoc arguments 8 facts))]
        (is (= :rejected (:status result)))
        (is (= "C15-SCHEMA" (:rule (failure result))))))
    (is (= 0 @calls))
    (let [facts (into {} (map #(vector (keyword (str "k" %))
                                      (apply str (repeat 4000 "x"))) (range 15)))
          result (c15 'build-c15-diagnostic (assoc arguments 8 facts))]
      (is (= :rejected (:status result)))
      (is (= :constructed-carrier-bound (get-in result [:diagnostics 0 :facts :reason]))))))

(deftest original-fields-are-separate-from-untrusted-verification-inputs
  ;; Resolved positive verification awaits the actual 00813 C15 binding seam.
  ;; No fabricated hash is used to confer a passing verification result.
  (let [arguments (fields (:event (original "delimiter" "gravity")))
        candidate (c15 'build-c15-diagnostic arguments)
        request (:diagnostic-id-request candidate)]
    (doseq [[original candidate request digest rule]
            [[{} candidate request nil "C15-SCHEMA"]
             [(subvec arguments 0 10) candidate request nil "C15-SCHEMA"]
             [(assoc arguments 8 "prose") candidate request nil "C15-FACTS"]
             [arguments candidate (assoc request :ordinal 1) nil "C15-ID"]
             [arguments candidate (assoc-in request [:preimage :facts] {}) nil "C15-ID"]
             [arguments candidate request nil "C15-ID"]
             [arguments (Object.) request nil "C15-SCHEMA"]]]
      (let [result (c15 'verify-c15-resolved-diagnostic [original candidate request digest])]
        (is (= :rejected (:status result)))
        (is (= :gravity/c15-resolved-diagnostic-verification (:artifact result)))
        (is (= rule (:rule (failure result))))
        (is (some #{:digest-recomputation} (:nonclaims result)))))))

(deftest request-identity-excludes-location-wording-and-private-reader-event
  (let [source (original "delimiter" "gravity")
        relocated (original "delimiter" "gravity" "/relocated/canonical-reader-delimiter.gravity")
        arguments (fields (:event source)) moved (fields (:event relocated))
        a (c15 'build-c15-diagnostic arguments) b (c15 'build-c15-diagnostic moved)
        wording (assoc-in arguments [9 0 :message] "different human guidance")
        changed (assoc-in arguments [8 :semantic-control] :changed)]
    (is (= :rejected (get-in source [:result :status])
           (get-in relocated [:result :status])))
    (is (not= (:primary a) (:primary b)))
    (is (= (:diagnostic-id-request a) (:diagnostic-id-request b)))
    (is (= (:diagnostic-id-request a)
           (:diagnostic-id-request (c15 'build-c15-diagnostic wording))))
    (is (not= (:diagnostic-id-request a)
              (:diagnostic-id-request (c15 'build-c15-diagnostic changed))))
    (is (= #{:rule :primary-artifact :stage :facts}
           (set (keys (get-in a [:diagnostic-id-request :preimage])))))
    (doseq [candidate [a b]]
      (is (contains? candidate :profile))
      (is (contains? candidate :target))
      (is (nil? (:profile candidate)))
      (is (nil? (:target candidate)))
      (is (contains? (:primary candidate) :syntax-id))
      (is (nil? (get-in candidate [:primary :syntax-id]))))))

(deftest paired-malformed-inputs-use-original-reader-for-both-spellings
  (doseq [family ["delimiter" "duplicate-set"]]
    (let [g (original family "gravity") q (original family "qst")
          a (c15 'build-c15-diagnostic (fields (:event g)))
          b (c15 'build-c15-diagnostic (fields (:event q)))]
      (is (= (slurp (path family "gravity")) (slurp (path family "qst"))))
      (is (= :rejected (get-in g [:result :status]) (get-in q [:result :status])))
      (is (= (:rule a) (:rule b)))
      (is (= (:diagnostic-id-request a) (:diagnostic-id-request b))))))
