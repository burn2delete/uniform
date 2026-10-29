(ns gravity.pmk.acceptance-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pmk.acceptance :as pmk]))

(def identity-record {:head (apply str (repeat 40 "a")) :tree (apply str (repeat 40 "b"))})
(def observation {:before identity-record :after identity-record
                  :root (java.net.URI. "file:/synthetic/")
                  :source-revision identity-record :main identity-record})
(def source-text "(defn consumer [x] (producer x)) (defn producer [x] x)")
(def test-text "(ns gravity.self-hosting.sh08-function-call-type-test) (deftest selected (helper)) (defn helper [] (invoke-c7 'consumer []))")
(def inputs (sorted-map pmk/source-path (pmk/sha (.getBytes source-text "UTF-8"))
                        pmk/test-path (pmk/sha (.getBytes test-text "UTF-8"))
                        "bootstrap/clojure/src/owner.clj" (apply str (repeat 64 "c"))))
(def snapshot
  {:source source-text :tests test-text
   :inputs inputs :input-count 3 :root (:root observation)
   :inputs-sha256 (pmk/sha (.getBytes (pr-str inputs) "UTF-8"))
   :runtime {:java-version "synthetic" :clojure-version "synthetic" :jars []}
   :launch {:dev-test {:main-opts ["-m" "gravity.development-test-runner"]}
            :classpath-roots ["bootstrap/clojure/src" "bootstrap/clojure/test"]}
   :resources {"owner.clj" {:expected (java.net.URI. "file:/synthetic/bootstrap/clojure/src/owner.clj")
                           :actual [(java.net.URI. "file:/synthetic/bootstrap/clojure/src/owner.clj")]}}})
(def manifest
  {:task "ACC-04" :source-revision identity-record :main identity-record
   :inputs-sha256 (:inputs-sha256 snapshot)
   :input-count 3 :launch (:launch snapshot)
   :source-sha256 (pmk/sha (.getBytes ^String (:source snapshot) "UTF-8"))
   :tests-sha256 (pmk/sha (.getBytes ^String (:tests snapshot) "UTF-8"))
   :runtime (:runtime snapshot)
   :resource-set-sha256 (pmk/sha (.getBytes (pr-str ["owner.clj"]) "UTF-8"))
   :selection [{:selector 'selected :role :synthetic :apis #{'consumer}}]
   :consumer-edges [['consumer 'producer]]})

(defn prepare [m o s]
  ;; Substitute only the independently fixed synthetic closure, never the input
  ;; manifest's possibly mutated selection/edges. No compiler or owner is mocked.
  (with-redefs [pmk/closure-sha256 (pmk/closure-digest manifest)]
    (pmk/prepare m o s)))

(defn reason [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))

(deftest source-selection-controls
  (let [plan (prepare manifest observation snapshot)]
    (is (= :unrun (:execution plan)))
    (is (= ["clojure" "-J-Xmx2g" "-M:dev-test" "--namespace" pmk/test-ns
            "--exact" "selected"] (:argv plan)))
    (is (= [(str pmk/test-ns "/selected")] (:selection plan))))
  (doseq [[expected m o s]
          [[:identity manifest (dissoc observation :before) snapshot]
           [:source-identity manifest (assoc-in observation [:source-revision :head] (apply str (repeat 40 "d"))) snapshot]
           [:main-identity manifest (assoc-in observation [:main :tree] (apply str (repeat 40 "e"))) snapshot]
           [:checkout-bookends manifest (assoc-in observation [:after :tree] (apply str (repeat 40 "d"))) snapshot]
           [:dependency-bytes manifest observation (assoc snapshot :inputs-sha256 "changed")]
           [:input-shape manifest observation (assoc snapshot :inputs {})]
           [:input-shape manifest observation (assoc-in snapshot [:inputs "bad"] "not-a-digest")]
           [:input-count manifest observation (assoc snapshot :input-count 4)]
           [:source-bytes manifest observation (update snapshot :source str " ")]
           [:test-bytes manifest observation (update snapshot :tests str " ")]
           [:runtime-identity manifest observation (assoc snapshot :runtime {})]
           [:launch-identity manifest observation (assoc-in snapshot [:launch :classpath-roots] ["other"])]
           [:launch-identity manifest observation (assoc-in snapshot [:launch :dev-test :main-opts] ["-m" "other"])]
           [:resource-root manifest (dissoc observation :root) snapshot]
           [:missing-resources manifest observation (assoc snapshot :resources {})]
           [:resource-set manifest observation (assoc-in snapshot [:resources "extra"] {})]
           [:resource-shadow-or-mismatch manifest observation (assoc-in snapshot [:resources "owner.clj" :actual] ["other"])]
           [:resource-shadow-or-mismatch manifest observation (update-in snapshot [:resources "owner.clj" :actual] conj "duplicate")]
           [:resource-path manifest observation (assoc-in snapshot [:resources "owner.clj"]
                                                        {:expected (java.net.URI. "file:/wrong")
                                                         :actual [(java.net.URI. "file:/wrong")]})]
           [:closure-contract (assoc-in manifest [:selection 0 :selector] 'helper) observation snapshot]
           [:closure-contract (assoc-in manifest [:selection 0 :selector] 'absent) observation snapshot]
           [:closure-contract (assoc-in manifest [:selection 0 :apis] #{'wrong}) observation snapshot]
           [:closure-contract (assoc-in manifest [:selection 0 :role] :wrong) observation snapshot]
           [:closure-contract (assoc manifest :selection []) observation snapshot]
           [:closure-contract (dissoc manifest :consumer-edges) observation snapshot]
           [:closure-contract (assoc manifest :consumer-edges [['producer 'consumer]]) observation snapshot]
           [:closure-contract (assoc manifest :consumer-edges [['consumer 'missing]]) observation snapshot]]]
    (is (= expected (reason #(prepare m o s))) (str expected))))

(deftest attributed-failure-controls
  (let [plan (prepare manifest observation snapshot)
        result {:schema :gravity/development-test-result-v1
                :authority :non-authoritative :authoritative? false
                :selection {:namespaces [pmk/test-ns] :test-vars (:selection plan) :fail-fast? false}
                :summary {:test 1 :pass 3 :fail 0 :error 0} :outcome :passed :exit-code 0}
        failed (assoc result :summary {:test 1 :pass 2 :fail 1 :error 0} :outcome :failed :exit-code 1)]
    (is (= result (:reported-result (pmk/attribute plan plan result))))
    (is (= failed (:reported-result (pmk/attribute plan plan failed))))
    (is (false? (:authenticated? (pmk/attribute plan plan failed))))
    (doseq [[expected p r]
            [[:source-drift (assoc plan :inputs-sha256 "changed") result]
             [:runner-schema plan (dissoc result :schema)]
             [:runner-authority plan (assoc result :authoritative? true)]
             [:result-selection plan (assoc-in result [:selection :test-vars] ["wrong/test"])]
             [:result-namespace plan (assoc-in result [:selection :namespaces] ["wrong"])]
             [:result-invocation plan (assoc-in result [:selection :fail-fast?] true)]
             [:result-counts plan (assoc-in result [:summary :test] nil)]
             [:misattributed-result plan (assoc failed :outcome :passed)]
             [:misattributed-result plan (assoc failed :exit-code 0)]
             [:misattributed-result plan (assoc-in result [:summary :test] 0)]]]
      (is (= expected (reason #(pmk/attribute plan p r)))))))

(defn replacement [s path field text]
  (let [next-inputs (assoc (:inputs s) path (pmk/sha (.getBytes ^String text "UTF-8")))]
    (assoc s field text :inputs next-inputs
           :inputs-sha256 (pmk/sha (.getBytes (pr-str next-inputs) "UTF-8")))))

(defn replacement-manifest [path field text]
  (assoc manifest :inputs-sha256 (:inputs-sha256 (replacement snapshot path field text))
         (if (= field :source) :source-sha256 :tests-sha256)
         (pmk/sha (.getBytes ^String text "UTF-8"))))

(deftest quoted-consumer-is-not-a-call
  (let [source "(defn consumer [x] '(producer x)) (defn producer [x] x)"
        tests "(ns gravity.self-hosting.sh08-function-call-type-test) (deftest selected 'consumer)"
        source-manifest (replacement-manifest pmk/source-path :source source)
        test-manifest (replacement-manifest pmk/test-path :tests tests)]
    (is (= :wrong-production-consumer
           (reason #(prepare source-manifest observation (replacement snapshot pmk/source-path :source source)))))
    (is (= :wrong-test-consumer
           (reason #(prepare test-manifest observation (replacement snapshot pmk/test-path :tests tests)))))))

(deftest namespace-and-plan-schema-controls
  (doseq [[expected tests]
          [[:test-namespace "(ns wrong) (deftest selected (invoke-c7 'consumer []))"]
           [:test-namespace "(deftest selected (invoke-c7 'consumer []))"]
           [:wrong-selector "(ns gravity.self-hosting.sh08-function-call-type-test) (defn selected [] nil)"]]]
    (is (= expected
           (reason #(prepare (replacement-manifest pmk/test-path :tests tests)
                             observation (replacement snapshot pmk/test-path :tests tests))))))
  (let [plan (prepare manifest observation snapshot)]
    (is (= :plan-schema (reason #(pmk/attribute (dissoc plan :schema) plan {}))))))

(deftest bound-resource-must-be-in-inventory
  (let [without-owner (dissoc inputs "bootstrap/clojure/src/owner.clj")
        digest (pmk/sha (.getBytes (pr-str without-owner) "UTF-8"))
        m (assoc manifest :inputs-sha256 digest :input-count 2)
        s (assoc snapshot :inputs without-owner :inputs-sha256 digest :input-count 2)]
    (is (= :resource-input-missing (reason #(prepare m observation s))))))
