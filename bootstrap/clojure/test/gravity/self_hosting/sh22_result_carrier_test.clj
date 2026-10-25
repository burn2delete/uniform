(ns gravity.self-hosting.sh22-result-carrier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; STD1/D8/D9: accepted operation results must fit the same existing carrier
;; policy their fresh verifier enforces; input validity alone is insufficient.
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_result_carrier_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative) emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-result-carrier." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (b/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- base-request [extension]
  (invoke (get @fixtures ["accepted" extension]) 'sh22-result-carrier-request []))
(defn- deep-request [extension depth]
  (invoke (get @fixtures ["accepted" extension]) 'sh22-result-carrier-depth-request [depth]))

(deftest sh22-export-errors-declare-complete-carrier-refusal
  ;; STD1 requires errors on each export; the module catalog is not inheritance.
  (let [previous {:vector-assoc ["STD3001" "STD3002"]
                  :vector-append ["STD3001" "STD3002"]
                  :vector-nth ["STD3001" "STD3002"]
                  :vector-slice ["STD3001" "STD3002"]
                  :ordered-map-assoc ["STD3001"] :ordered-map-get []
                  :ordered-map-get-default-keyword ["STD3001" "STD10003"]
                  :ordered-map-dissoc-keyword ["STD3001" "STD10003"]
                  :ordered-set-conj ["STD3001"]
                  :ordered-set-contains-keyword ["STD3001" "STD10003"]
                  :ordered-set-contains-symbol ["STD3001" "STD10003"]
                  :ordered-set-contains-integer ["STD3001" "STD10003"]
                  :ordered-set-equal-keyword ["STD3001" "STD10003"]
                  :ordered-set-equal-integer ["STD3001" "STD10003"]
                  :utf8-validate ["STD4001"]
                  :checked-add-i64 ["STD5001"] :checked-sub-i64 ["STD5001"]
                  :checked-mul-i64 ["STD5001"]
                  :checked-nonnegative-quot-i64 ["STD5001" "STD5002"]
                  :error-context ["STD2004"] :io-file-read ["STD8001" "STD8003"]
                  :io-stdout ["STD4001" "STD8001"] :artifact-data ["STD10003"]
                  :meta-view ["STD15001"] :canonical-data ["STD10003"]}
        manifest (call 'sh22-module-manifest [])
        exports (:exports manifest)]
    (is (= manifest (call 'sh22-module-manifest [])))
    (is (= (set (keys previous)) (set (map :name exports))
           (:operation-set (call 'sh22-library-policy []))))
    (doseq [{:keys [name errors] :as export} exports]
      (let [old (get previous name)]
        (is (= #{:name :kind :family :type :stability :effects :capabilities
                 :allocation :errors :profiles} (set (keys export))))
        (is (= 1 (count (filter #{"STD10003"} errors))) name)
        (is (= (if (some #{"STD10003"} old) old (conj old "STD10003")) errors) name)))
    (doseq [extension ["gravity" "qst"]]
      (let [input (deep-request extension 30) result (handle input)
            export (first (filter #(= (:operation input) (:name %)) exports))
            rule (get-in result [:diagnostics 0 :rule])]
        (is (= :rejected (:status result)))
        (is (= "STD10003" rule))
        (is (some #{rule} (:errors export)))
        (is (= result (handle input)))
        (is (verified? input result))))))

(deftest sh22-accepted-result-envelope-obeys-depth-bound
  (doseq [extension ["gravity" "qst"]]
    (let [good (deep-request extension 29)
          bad (invoke (get @fixtures ["rejected" extension]) 'sh22-result-carrier-over-depth-request
                      [(base-request extension)])
          accepted (handle good) rejected (handle bad)]
      (is (true? (call 'sh22-request-common-valid? [good])))
      (is (true? (call 'sh22-request-common-valid? [bad])))
      (is (= :accepted (:status accepted)))
      (is (= [(get-in good [:arguments 1])] (:value accepted)))
      (is (= :accepted (:status (call 'sh22-carrier-preflight [accepted]))))
      (is (verified? good accepted))
      (is (= :rejected (:status rejected)))
      (is (= "STD10003" (get-in rejected [:diagnostics 0 :rule])))
      (is (= :result-carrier-bound-exceeded (get-in rejected [:diagnostics 0 :reason])))
      (is (not (contains? rejected :value)))
      (is (= :accepted (:status (call 'sh22-carrier-preflight [rejected]))))
      (is (verified? bad rejected)))))

(deftest sh22-rejection-bounds-unreportable-request-identities
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)
          normal (handle (assoc base :arguments [7 :leaf]))]
      (doseq [id [(nth (iterate vector :id) 31) (apply str (repeat 35000 "i"))]
              arguments [[[] :leaf] [7 :leaf]]]
        (let [input (assoc base :request-id id :arguments arguments) result (handle input)]
          (is (true? (call 'sh22-request-common-valid? [input])))
          (is (= :rejected (:status result)))
          (is (= (set (keys normal)) (set (keys result))))
          (is (true? (nil? (:request-id result))))
          (is (true? (nil? (get-in result [:diagnostics 0 :request-id]))))
          (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))
          (is (= :request-identity-carrier-bound-exceeded (get-in result [:diagnostics 0 :reason])))
          (is (= (:source-span input) (get-in result [:diagnostics 0 :source-span])))
          (is (= (:origin-chain input) (get-in result [:diagnostics 0 :origin-chain])))
          (is (= :accepted (:status (call 'sh22-carrier-preflight [result]))))
          (is (= result (handle input)))
          ;; Recomputes the owned refusal; a redacted ID is not identity proof.
          (is (true? (verified? input result)))
          (is (true? (not (verified? input (assoc-in result [:diagnostics 0 :reason] :forged)))))))
      (doseq [id [:ordinary false 0 [] {}]]
        (let [input (assoc base :request-id id :arguments [7 :leaf]) result (handle input)]
          (is (true? (call 'sh22-request-common-valid? [input])))
          (is (= id (:request-id result) (get-in result [:diagnostics 0 :request-id])))
          (is (= "STD3002" (get-in result [:diagnostics 0 :rule])))
          (is (= :expected-vector (get-in result [:diagnostics 0 :reason])))
          (is (verified? input result)))))))

(deftest sh22-rejection-has-bounded-fallback-when-provenance-cannot-fit
  (doseq [extension ["gravity" "qst"]]
    (let [base (assoc-in (assoc (base-request extension) :request-id :id :arguments [7 :leaf])
                         [:source-span :source-id] :s)
          units (:scalar-units (call 'sh22-carrier-preflight [base]))
          source-id (keyword (apply str (repeat (+ 1 (- 65536 units)) "s")))
          input (assoc-in base [:source-span :source-id] source-id)
          result (handle input)]
      (is (true? (call 'sh22-request-common-valid? [input])))
      (is (= 65536 (:scalar-units (call 'sh22-carrier-preflight [input]))))
      (is (= :rejected (:status result)))
      (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))
      (is (= :diagnostic-carrier-bound-exceeded (get-in result [:diagnostics 0 :reason])))
      (doseq [key [:request-id :operation]]
        (is (true? (nil? (get result key))))
        (is (true? (nil? (get-in result [:diagnostics 0 key])))))
      (doseq [key [:source-span :origin-chain]]
        (is (true? (nil? (get-in result [:diagnostics 0 key])))))
      (is (= :accepted (:status (call 'sh22-carrier-preflight [result]))))
      (is (true? (verified? input result))))))

(deftest sh22-accepted-result-envelope-obeys-scalar-bound
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)]
      (doseq [[size status] [[30000 :accepted] [35000 :rejected]]]
        (let [payload (apply str (repeat size "a"))
              input (assoc base :arguments [[] payload]) result (handle input)]
          (is (true? (call 'sh22-request-common-valid? [input])))
          (is (= status (:status result)))
          (if (= status :accepted)
            (is (= [payload] (:value result)))
            (do (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))
                (is (= :result-carrier-bound-exceeded (get-in result [:diagnostics 0 :reason])))))
          (is (= :accepted (:status (call 'sh22-carrier-preflight [result]))))
          (is (verified? input result)))))))

(deftest sh22-result-carrier-paired-producers-and-forgery-controls
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity"))) (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension) accepted (handle base)
          assoc-input (assoc base :operation :vector-assoc :arguments [(:value accepted) 0 :changed])
          associated (handle assoc-input)
          bad (deep-request extension 30)
          ;; Same envelope the old producer emitted, assembled from its real
          ;; valid operation result; no caller-supplied proof/status authority.
          old-candidate (assoc accepted :value [(get-in bad [:arguments 1])]
                               :identity-input (call 'sh22-identity-input [bad]))]
      (is (= :accepted (:status accepted) (:status associated)))
      (is (= [:changed] (:value associated)))
      (is (verified? base accepted))
      (is (verified? assoc-input associated))
      (is (= :rejected (:status (call 'sh22-verify-result [bad old-candidate]))))
      (doseq [candidate [(assoc accepted :value [:forged])
                         (assoc-in accepted [:facts :allocation] :none)]]
        (is (= :rejected (:status (call 'sh22-verify-result [base candidate]))))))))
