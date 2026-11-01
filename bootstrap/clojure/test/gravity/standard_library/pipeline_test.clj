(ns gravity.standard-library.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.standard-library :as library]))

(defn- compile-source [relative]
  (let [path (.getCanonicalPath (io/file relative)) text (slurp path :encoding "UTF-8")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
    {:path path :text text :sha256 (bootstrap/sha256-hex text)
     :plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text)}))

(def ^:private source
  (delay (compile-source "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(def ^:private fixtures
  (delay
    (into {} (for [extension [".gravity" ".qst"]]
               [extension (compile-source
                            (str "bootstrap/clojure/fixtures/self-hosting/sh-22/accepted/library-requests" extension))]))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-standard-library-kernel-development :compiler-artifact-plan? true}
   plan function arguments))
(defn- source-invoke [function arguments] (invoke (:plan @source) function arguments))
(defn- request [extension function] (invoke (get-in @fixtures [extension :plan]) function []))

(deftest source-manifest-and-native-stage-state-their-limited-boundaries
  (let [manifest (library/module-manifest source-invoke)
        contract (:gravity/namespace-contract (meta #'library/record-call!))]
    (is (= :gravity/standard-library-module-manifest (:artifact manifest)))
    (is (= 'gravity.stdlib.self-hosting-core (:module manifest)))
    (is (= :experimental (:stability manifest)))
    (is (= #{:meta} (:profiles manifest)))
    (is (every? #(= :experimental (:stability %)) (:exports manifest)))
    (is (= #{'record-call! 'module-manifest 'execute-request 'verify-result}
           (set (keys (ns-publics 'gravity.standard-library)))))
    (is (empty? (ns-aliases 'gravity.standard-library)))
    (is (= :internal (:stability contract)))
    (is (false? (:upstream-authentication contract)))
    (is (false? (:legacy-compile-proof-safety-labels-authoritative? contract)))
    (is (some #{:provider-io} (:unsupported contract)))
    (is (some #{:authenticated-sh13-sh14-sh19} (:unsupported contract)))))

(deftest complete-paired-source-kernels-produce-real-values-and-preserve-request-data
  (let [rows
        (mapv
         (fn [extension]
           (into {}
                 (for [[function expected] [['sh22-vector-assoc-request [10 99 30]]
                                             ['sh22-vector-nth-request 30]
                                             ['sh22-checked-add-request
                                              {:artifact :gravity/result :variant :ok :value 42}]]]
                   (let [original (request extension function) before (pr-str original)
                         envelope (library/execute-request source-invoke original)
                         result (:result envelope)]
                     (is (= :accepted (:status result)))
                     (is (= :passed (get-in envelope [:verification :status])))
                     (is (= expected (:value result)))
                     (is (= (:request-id original) (:request-id result)))
                     (is (= (:source-span original) (get-in result [:provenance :source-span])))
                     (is (= (:origin-chain original) (get-in result [:provenance :origin-chain])))
                     (is (= (:actual-source-path original) (get-in result [:provenance :actual-source-path])))
                     (is (= (:arguments original) (get-in result [:identity-input :arguments])))
                     (is (= before (pr-str original)))
                     [function envelope]))))
         [".gravity" ".qst"])]
    (is (apply = rows))
    (is (= (get-in @fixtures [".gravity" :text]) (get-in @fixtures [".qst" :text])))))

(deftest index-and-overflow-refusals-remain-source-errors-with-exact-replay
  (doseq [extension [".gravity" ".qst"]]
    (let [vector-request (request extension 'sh22-vector-assoc-request)
          add-request (request extension 'sh22-checked-add-request)]
      (doseq [[original rule] [[(assoc-in vector-request [:arguments 1] 3) "STD3002"]
                              [(assoc add-request :arguments [Long/MAX_VALUE 1]) "STD5001"]]]
        (let [envelope (library/execute-request source-invoke original)
              result (:result envelope)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= :passed (get-in envelope [:verification :status])))
          (is (= result (source-invoke 'sh22-handle-request [original])))
          (is (= (:source-span original) (get-in result [:diagnostics 0 :source-span]))))))))

(deftest replay-rejects-coherent-substitution-and-does-not-expose-a-failed-candidate
  (let [original (request ".gravity" 'sh22-vector-assoc-request)
        candidate (:result (library/execute-request source-invoke original))
        changed-request (assoc-in original [:arguments 2] 100)
        changed (:result (library/execute-request source-invoke changed-request))]
    (doseq [replacement [(assoc candidate :control :substituted)
                         (dissoc candidate :identity-input)
                         (assoc candidate :value [10 100 30]) changed]]
      (let [result (library/verify-result source-invoke original replacement)]
        (is (= :rejected (:status result)))
        (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))))
    (is (= :passed (:status (library/verify-result source-invoke changed-request changed))))
    (let [calls (atom [])
          operation (fn [function arguments]
                      (swap! calls conj function)
                      (source-invoke function
                                     (if (= 'sh22-verify-result function)
                                       (update arguments 1 assoc :control :substituted) arguments)))
          result (library/execute-request operation original)]
      (is (= ['sh22-handle-request 'sh22-verify-result] @calls))
      (is (= :rejected (:status result)))
      (is (= "STD10003" (get-in result [:diagnostics 0 :rule])))
      (is (not (contains? result :result))))))

(deftest native-call-records-preserve-compatibility-without-granting-proof-authority
  (let [ctx (atom {:profile :meta :target :jvm})
        node {:node-id :fixture/unbound-native-call
              :source-span {:source "fixture:unbound-library-source" :form-index 0}}
        args (mapv #(hash-map :value %) ['fixture/unbound-module #{:meta} :experimental])
        checker-a (atom {}) checker-b (atom {})
        direct (library/record-call! #'bootstrap/record-checker! #'bootstrap/dispatch-arg-value
                                     checker-a ctx node 'fixture/unbound-call args #{} #{} :fixture/unbound-type
                                     {:standard-library-kind :namespace-contract})
        compatibility (bootstrap/record-standard-library-call!
                        checker-b ctx node 'fixture/unbound-call args #{} #{} :fixture/unbound-type
                        {:standard-library-kind :namespace-contract})]
    (is (= direct compatibility))
    (is (= @checker-a @checker-b))
    (is (= (:source-span node) (:source-span direct)))
    (is (= :meta (:profile direct)))
    (is (= :jvm (:target direct)))
    (is (= #{} (:effects direct) (:capabilities direct)))
    (is (nil? (library/record-call! nil nil nil nil nil nil nil nil nil nil nil)))
    (is (= '([checker ctx node operator args effects capabilities return-type spec])
           (:arglists (meta #'bootstrap/record-standard-library-call!))))))

(deftest operation-var-reads-retain-the-original-before-arguments-timing
  (let [writes (atom []) changed (atom false)
        original-writer bootstrap/record-checker!
        original-argument bootstrap/dispatch-arg-value
        old-writer (fn [checker key value] (swap! writes conj :old) (original-writer checker key value))
        new-writer (fn [checker key value] (swap! writes conj :new) (original-writer checker key value))
        argument (fn [args index]
                   (when (compare-and-set! changed false true)
                     (alter-var-root #'bootstrap/record-checker! (constantly new-writer)))
                   (original-argument args index))
        checker (atom {}) ctx (atom {:profile :meta :target :jvm})
        node {:node-id :fixture/unbound-native-call}]
    (with-redefs [bootstrap/record-checker! old-writer bootstrap/dispatch-arg-value argument]
      (dotimes [_ 2]
        (bootstrap/record-standard-library-call!
         checker ctx node 'fixture/unbound-call [] #{} #{} :fixture/unbound-type
         {:standard-library-kind :namespace-contract})))
    (is (= [:old :new] @writes))
    (is (= 2 (count (:standard-library-namespace-contracts @checker))))))
