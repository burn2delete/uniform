(ns gravity.self-hosting.sh08-collection-call-inference-test
  "Complete current Reader/C5/SH07/C7 source composition for collection inference."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(defn- fixture [family basename extension]
  (support/path (str "bootstrap/clojure/fixtures/self-hosting/sh-08/"
                     family "/" basename extension)))

(defn- stage [label operation]
  (println (pr-str {:collection-inference-stage label :event :begin}))
  (let [result (operation)]
    (println (pr-str {:collection-inference-stage label :event :complete}))
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

(deftest collection-caller-inference-current-source-composition
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [request (current-request (fixture "accepted" "collection-caller-inference" extension))
            result (support/invoke-c7 'sh08-function-type-core-artifact [request])]
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (when (= :accepted (:status result))
          (let [functions (into {} (map (juxt :definition-name identity))
                                (:function-type-table result))
                inferred-map {:kind :gravity.type/map :shape :literal
                              :member-types [:gravity.type/keyword :gravity.type/integer
                                             :gravity.type/keyword :gravity.type/bool]}]
            (is (= inferred-map
                   (get-in functions ['key-count :parameters 0 :type])
                   (get-in functions ['forwarded-key-count :parameters 0 :type])
                   (get-in functions ['same-keys? :parameters 0 :type])))
            (is (= :gravity.type/integer (get-in functions ['key-count :return])
                   (get-in functions ['forwarded-key-count :return])
                   (get-in functions ['main :return])))
            (is (= :gravity.type/bool (get-in functions ['same-keys? :return])))
            (is (= :passed (:status (support/invoke-c7
                                    'sh08-verify-function-type-result [request result]))))))))))

(deftest invalid-collection-callers-current-source-composition
  (let [request (current-request (fixture "rejected" "collection-caller-string-key" ".gravity"))
        result (support/invoke-c7 'sh08-function-type-core-artifact [request])
        diagnostic (first (filter #(= :map-keys-finite-keyword-map-type-required (:reason %))
                                  (:diagnostics result)))
        node (first (filter #(= (:core-node-id diagnostic) (:node-id %))
                            (get-in request [:canonical-core-artifact :nodes])))]
    (is (= :rejected (:status result)))
    (is (= "C7-TYPE-MISMATCH" (:rule diagnostic)))
    (is (= :validated-complete-finite-keyword-key-map (:expected-type diagnostic)))
    (is (= {:kind :gravity.type/map :shape :literal
            :member-types [:gravity.type/string :gravity.type/integer]}
           (:actual-type diagnostic)))
    (is (= [:meta :jvm] ((juxt :profile :target) diagnostic)))
    (is (some? node))
    (is (some? (:source-span diagnostic)))
    (is (= (get-in node [:source :semantic-span]) (:source-span diagnostic)))
    (is (= (get-in node [:source :syntax-id]) (:syntax-id diagnostic)))
    (is (= (first (:resolved-binding-ids node)) (:binding-id diagnostic)))
    (is (= (get-in node [:source :origin-chain]) (:origin-chain diagnostic)))
    (is (= (get-in node [:source :generated-origin]) (:generated-origin diagnostic)))))
