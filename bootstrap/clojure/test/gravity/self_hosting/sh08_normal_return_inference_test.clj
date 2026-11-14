(ns gravity.self-hosting.sh08-normal-return-inference-test
  "L2/L5/C7 source composition for normal completion during inference."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(defn- fixture [family basename extension]
  (support/path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/"
                     family "/" basename extension)))

(defn- stage [label operation]
  (println (pr-str {:normal-return-stage label :event :begin}))
  (let [result (operation)]
    (println (pr-str {:normal-return-stage label :event :complete}))
    result))

(defn- current-request [source-path]
  (let [reader (support/path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        invocation {:reader-source-path reader
                    :authenticated-envelope-options {:source :development-current}
                    :syntax-source-selection
                    {:schema :gravity/sh04-development-syntax-selection-v1
                     :source-path (support/path "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")}
                    :macro-source-mode :development-current}
        resolution (stage :c5 #(bootstrap/compiler-c5-resolution-source-artifact
                               source-path (slurp source-path)
                               {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                               reader invocation))
        core (stage :sh07 #(bootstrap/sh07-core-from-resolution-artifact resolution invocation))
        report (stage :sh07-verification #(bootstrap/sh07-core-artifact-verification core invocation))
        coordinator (support/coordinator-verification core report)
        context (support/b47-context core report coordinator)]
    (is (= :passed (:status report)) (pr-str (:failed-checks report)))
    {:canonical-core-artifact (:canonical-core-artifact context)
     :b47-context context :coordinator-verification coordinator}))

(defn- accepted-seed-observations [source-path]
  ;; Observe these same accepted bytes on the temporary single-source seed.
  ;; This does not assert typed lowering, a native target or JVM ABI behavior.
  (let [text (slurp source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path text)
        token (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
        execute #(bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :normal-return-inference-seed-observation} compiled %1 %2)]
    (is (= (str "sha256:" (bootstrap/sha256-hex text))
           (get-in compiled [:source :sha256])))
    (is (= :ok (execute 'main [])))
    (doseq [name '[choose-do choose-let choose-argument]]
      (is (= :ok (execute name [false])))
      (let [thrown (try (execute name [true]) nil
                       (catch clojure.lang.ExceptionInfo error error))]
        (is (= "stop" (:payload (ex-data thrown))))
        (is (identical? token (:gravity.bootstrap/source-throw (ex-data thrown))))))))

(deftest eager-normal-return-current-source-composition
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [source-path (fixture "accepted" "normal-return-inference" extension)
            request (current-request source-path)
            result (support/invoke-c7 'sh08-function-type-core-artifact [request])]
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (when (= :accepted (:status result))
          (let [functions (into {} (map (juxt :definition-name identity))
                                (:function-type-table result))
                types (:type-table result)
                nodes (into {} (map (juxt :node-id identity))
                            (get-in request [:canonical-core-artifact :nodes]))]
            (doseq [name '[stop relay-stop]]
              (is (= :gravity.type/never (get-in functions [name :return]))))
            (doseq [name '[choose-do choose-let choose-argument normal-sequence main]]
              (is (= :gravity.type/keyword (get-in functions [name :return]))))
            (is (= :gravity.type/nil (get-in functions ['discard :return])))
            (is (= :gravity.type/string (get-in functions ['normal :return])))
            (doseq [name '[choose-do choose-let choose-argument]]
              (let [body (nodes (get-in functions [name :body-core-node-id]))
                    branch (nodes (get (:children body) 1))]
                (is (= :if (:core-form body)))
                (is (= :gravity.type/never (types (:node-id branch))))
                (is (= [:gravity.type/string]
                       (get-in functions [name :thrown-error-effects])))))
            (let [verification (support/invoke-c7 'sh08-verify-function-type-result [request result])]
              (is (= :passed (:status verification)))
              (when (= :passed (:status verification))
                (let [binding (support/seal-binding request result verification)
                      effects (stage :c8 #(support/leaf 'build-body-effect-result
                                                       result verification binding))]
                  (is (= :accepted (:status effects)) (pr-str (:diagnostics effects)))
                  (is (= :passed (:status (support/leaf 'verify-body-effect-result
                                                       result verification binding effects)))))))
            (accepted-seed-observations source-path)))))))

(deftest declared-normal-return-mismatch-current-source-composition
  (let [request (current-request (fixture "rejected" "normal-return-declaration-mismatch" ".gravity"))
        result (support/invoke-c7 'sh08-function-type-core-artifact [request])
        diagnostic (first (filter #(= :function-result-type-mismatch (:reason %))
                                  (:diagnostics result)))
        node (first (filter #(= (:core-node-id diagnostic) (:node-id %))
                            (get-in request [:canonical-core-artifact :nodes])))]
    (is (= :rejected (:status result)))
    (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
    (is (= :gravity.type/keyword (:expected-type diagnostic)))
    (is (= :gravity.type/string (:actual-type diagnostic)))
    (is (= [:meta :jvm] ((juxt :profile :target) diagnostic)))
    (is (= :do (:core-form node)))
    (is (some? (:source-span diagnostic)))
    (is (= (get-in node [:source :semantic-span]) (:source-span diagnostic)))
    (is (= (get-in node [:source :syntax-id]) (:syntax-id diagnostic)))
    (is (= (get-in node [:source :origin-chain]) (:origin-chain diagnostic)))))

(deftest normal-return-source-extension-parity
  (doseq [[family basename] [["accepted" "normal-return-inference"]
                            ["rejected" "normal-return-declaration-mismatch"]]]
    (is (= (slurp (fixture family basename ".gravity"))
           (slurp (fixture family basename ".qst"))))))
