(ns gravity.self-hosting.sh15-resolved-reader-diagnostic-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh15-reader-canonical-diagnostic-test]))

;; Reuse the existing original-source producer, compiler plans, and field mapping.
(def ^:private original
  #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/original)
(def ^:private fields
  #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/fields)
(def ^:private c15
  #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/c15)
(def ^:private path
  #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/path)
(def ^:private request-for
  #'gravity.self-hosting.sh15-reader-canonical-diagnostic-test/request-for)

(defn- digest [source-path request]
  (bootstrap/sh03-reader-canonical-digest source-path request))
(defn- resolve-private [source-path arguments]
  (let [unresolved (c15 'build-c15-diagnostic arguments)
        request (:diagnostic-id-request unresolved)]
    {:unresolved unresolved :request request
     :candidate (assoc unresolved :diagnostic-id (digest source-path request))}))
(defn- verify [source-path arguments candidate request]
  ;; The host computes a fresh digest; the guest rebuilds from separate originals.
  (c15 'verify-c15-resolved-diagnostic
       [arguments candidate request (digest source-path request)]))
(defn- rejected [result rule]
  (is (= :rejected (:status result)))
  (is (= :gravity/c15-resolved-diagnostic-verification (:artifact result)))
  (is (= rule (get-in result [:diagnostics 0 :rule])))
  (is (some #{:digest-recomputation} (:nonclaims result))))

(deftest actual-reader-diagnostics-bind-and-verify-with-real-request-digests
  (doseq [[family rule] [["delimiter" "C2-DELIMITER"] ["duplicate-set" "C2-SET"]]]
    (let [rows
          (mapv
           (fn [extension]
             (let [source-path (path family extension)
                   source (original family extension)
                   before (pr-str source) arguments (fields (:event source))
                   {:keys [unresolved request candidate]} (resolve-private source-path arguments)
                   result (verify source-path arguments candidate request)]
               (is (= :rejected (get-in source [:result :status])))
               (is (= rule (:rule candidate)))
               (is (= (request-for arguments) request))
               (is (= {:digest-ref 0} (:diagnostic-id unresolved)))
               (is (re-matches #"sha256:[0-9a-f]{64}" (:diagnostic-id candidate)))
               (is (= (digest source-path request) (:diagnostic-id candidate)))
               (is (not= (digest source-path (:preimage request)) (:diagnostic-id candidate)))
               (is (= (assoc unresolved :diagnostic-id (:diagnostic-id candidate)) candidate))
               (is (= :passed (:status result)))
               (is (= [] (:diagnostics result)))
               (is (= :fresh-fields-and-external-digest-equality (:verification-status result)))
               (is (= #{:digest-recomputation :upstream-source-authentication
                        :public-safe-projection :canonical-stream-verification :self-hosting :release}
                      (set (:nonclaims result))))
               (doseq [key [:profile :target]]
                 (is (contains? candidate key))
                 (is (nil? (get candidate key))))
               (is (contains? (:primary candidate) :syntax-id))
               (is (nil? (get-in candidate [:primary :syntax-id])))
               (is (= (:primary candidate) (nth arguments 3)))
               (is (= (:origin-chain candidate) (nth arguments 5)))
               (is (= (:related candidate) (nth arguments 4)))
               (when (= family "duplicate-set")
                 (is (seq (:related candidate))))
               (is (= before (pr-str source)))
               candidate))
           ["gravity" "qst"])]
      (is (= (mapv :diagnostic-id-request rows) (repeat 2 (:diagnostic-id-request (first rows)))))
      (is (apply = (map :diagnostic-id rows))))))

(deftest resolved-verification-rejects-every-candidate-field-and-digest-substitution
  (let [source-path (path "duplicate-set" "gravity")
        arguments (fields (:event (original "duplicate-set" "gravity")))
        {:keys [request candidate]} (resolve-private source-path arguments)
        changed-request (assoc-in request [:preimage :facts :control] :changed)
        fresh (digest source-path request)]
    (is (= :passed (:status (verify source-path arguments candidate request))))
    (doseq [key (keys candidate)
            mutation [(dissoc candidate key) (assoc candidate key :substituted)]]
      (rejected (verify source-path arguments mutation request) "C15-ID"))
    (rejected (verify source-path arguments (assoc candidate :unexpected true) request) "C15-ID")
    (doseq [replacement [nil "malformed" (digest source-path changed-request)
                         (digest source-path (:preimage request))]]
      (is (not= fresh replacement))
      (rejected (c15 'verify-c15-resolved-diagnostic
                      [arguments candidate request replacement]) "C15-ID"))))

(deftest coherent-request-and-candidate-substitutions-do-not-replace-original-authority
  (let [source-path (path "delimiter" "gravity")
        arguments (fields (:event (original "delimiter" "gravity")))
        {:keys [request candidate]} (resolve-private source-path arguments)
        mutations [(assoc request :ordinal 1) (assoc request :purpose :other-purpose)
                   (assoc request :domain :other-domain) (assoc request :extra true)
                   (update request :preimage dissoc :stage)
                   (assoc-in request [:preimage :facts :control] :changed)]]
    (doseq [replacement mutations]
      (let [replacement-digest (digest source-path replacement)
            coherent (assoc candidate :diagnostic-id-request replacement
                                      :diagnostic-id replacement-digest)]
        (is (not= (:diagnostic-id candidate) replacement-digest))
        (rejected (verify source-path arguments coherent replacement) "C15-ID")))))

(deftest resolved-verification-rebuilds-from-the-separate-original-eleven-fields
  (let [source-path (path "duplicate-set" "gravity")
        arguments (fields (:event (original "duplicate-set" "gravity")))
        {:keys [request candidate]} (resolve-private source-path arguments)
        changes [(assoc arguments 0 "C2-CONTROL") (assoc arguments 1 :warning)
                 (assoc arguments 2 :compiler-diagnostics)
                 (assoc-in arguments [3 :artifact] :other-artifact)
                 (assoc-in arguments [3 :syntax-id] :produced-syntax)
                 (assoc-in arguments [4 0 :role] :other-related-role)
                 (assoc-in arguments [5 0 :logical-source-id] "other-logical-source")
                 (assoc arguments 6 :safe) (assoc arguments 7 :portable)
                 (assoc-in arguments [8 :control] :changed)
                 (assoc-in arguments [9 0 :kind] :other-remediation)
                 (assoc arguments 10 [:input-omitted])]]
    (doseq [changed changes]
      (let [{replacement :candidate replacement-request :request}
            (resolve-private source-path changed)]
        (is (= :passed (:status (verify source-path changed replacement replacement-request))))
        (rejected (verify source-path arguments replacement replacement-request) "C15-ID")
        (rejected (verify source-path changed candidate request) "C15-ID")))
    (doseq [[malformed rule]
            [[(subvec arguments 0 10) "C15-SCHEMA"]
             [(assoc arguments 8 "untyped facts") "C15-FACTS"]
             [(update arguments 3 dissoc :syntax-id) "C15-SPAN"]
             [(assoc arguments 5 [{:kind :generated}]) "C15-ORIGIN"]]]
      (rejected (verify source-path malformed candidate request) rule))))

(deftest actual-c15-hashes-exclude-physical-location-and-wording-but-retain-semantic-facts
  (doseq [family ["delimiter" "duplicate-set"]]
    (let [source-path (path family "gravity") source (original family "gravity")
          moved-path (str "/relocated/canonical-reader-" family ".gravity")
          moved (original family "gravity" moved-path)
          arguments (fields (:event source)) moved-fields (fields (:event moved))
          wording (assoc-in arguments [9 0 :message] "Changed human guidance")
          semantic (assoc-in arguments [8 :control] :changed)
          rows (mapv (fn [[p values]]
                       (let [{:keys [request candidate]} (resolve-private p values)]
                         (is (= :passed (:status (verify p values candidate request))))
                         candidate))
                     [[source-path arguments] [moved-path moved-fields]
                      [source-path wording] [source-path semantic]])
          [base relocated reworded changed] rows]
      (is (not= (:primary base) (:primary relocated)))
      (is (not= (:remediation base) (:remediation reworded)))
      (is (= (:diagnostic-id base) (:diagnostic-id relocated) (:diagnostic-id reworded)))
      (is (not= (:diagnostic-id base) (:diagnostic-id changed)))
      ;; Upstream event metadata is preserved separately and never enters the 11 fields.
      (is (= arguments (fields (assoc (:event source) :diagnostic-id (:diagnostic-id changed)
                                      :private-context {:display "different"}))))
      (is (= #{:rule :primary-artifact :stage :facts}
             (set (keys (get-in base [:diagnostic-id-request :preimage]))))))))
