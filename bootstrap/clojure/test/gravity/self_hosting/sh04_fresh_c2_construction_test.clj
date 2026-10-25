(ns gravity.self-hosting.sh04-fresh-c2-construction-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh04_fresh_c2_construction_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (.getPath (io/file @root relative)))
(defn- source-path [] (path "bootstrap/clojure/fixtures/self-hosting/sh-04/accepted/source-syntax.gravity"))
(defn- reader-path [] (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
(defn- project [] (bootstrap/reader-project-context-for-source (source-path)))
(defn- bootstrap-var [name] (ns-resolve 'gravity.bootstrap name))
(defn- construct [text]
  (bootstrap/compiler-c3-syntax-source-artifact (source-path) text (project) nil nil (reader-path)))
(defn- fresh-c2 [text]
  (bootstrap/compiler-c2-reader-source-artifact (source-path) text (project) nil nil (reader-path)))
(def ^:private small-c2 (delay (fresh-c2 "1")))

(defn- diagnostic [thunk]
  (try (thunk) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- observe-validations [events thunk]
  ;; Operation forwarding reenters the public adapter. Count logical outer
  ;; validations while delegating every call to the actual verifier.
  (let [original bootstrap/c3-validate-c2-reader-artifact! active? (atom false)]
    (with-redefs [bootstrap/c3-validate-c2-reader-artifact!
                  (fn [source c2]
                    (if @active?
                      (original source c2)
                      (do
                        (reset! active? true)
                        (try
                          (let [report (original source c2)]
                            (swap! events conj {:kind :validation :c2 c2 :report report})
                            report)
                          (finally (reset! active? false))))))]
      (thunk))))

(deftest sh04-construction-reuses-one-fresh-validation-with-complete-parity
  (let [worker-var (bootstrap-var 'sh04-syntax-build-from-fresh-c2!) worker @worker-var
        events (atom [])
        actual (observe-validations events
                 #(with-redefs-fn
                    {worker-var (fn [source c2 report]
                                  (swap! events conj {:kind :worker :c2 c2 :report report})
                                  (worker source c2 report))}
                    (fn [] (construct "1"))))
        ;; Reconstruct the former path with the actual standalone public
        ;; verifier and unchanged builder. No passed report is fabricated.
        reference (with-redefs-fn
                    {worker-var (fn [source c2 _]
                                  (with-redefs-fn {worker-var worker}
                                    #(bootstrap/sh04-syntax-resolved-result! source c2)))}
                    #(construct "1"))
        [validation entry] @events
        proof (bootstrap/c3-syntax-capability-proof actual)]
    (is (= [:validation :worker] (mapv :kind @events)))
    (is (identical? (:c2 validation) (:c2 entry)))
    (is (identical? (:report validation) (:report entry)))
    (is (true? (get (meta worker-var) :private)))
    (is (= actual reference))
    (is (= (:artifact-id actual) (:artifact-id reference)))
    (is (= (mapv :syntax/id (:syntax-object-stream actual))
           (mapv :syntax/id (:syntax-object-stream reference))))
    (is (= (:syntax-serialization-fixture actual) (:syntax-serialization-fixture reference)))
    (is (= :passed (get-in actual [:syntax-verification-report :status])))
    (is (= :complete (:status proof)))
    (is (= :failed (:status (bootstrap/c3-syntax-capability-proof
                             (assoc-in actual [:gravity-metadata-ledger :entries 0 :action] :substituted)))))
    (is (some? (get-in actual [:gravity-syntax-boundary :gravity-syntax-serialization :semantic-payload])))
    (is (= (get-in actual [:gravity-syntax-boundary :gravity-syntax-serialization :semantic-payload])
           (get-in actual [:gravity-syntax-boundary :gravity-syntax-deserialization :semantic-payload])))))

(defn- bad-override-text []
  (slurp (path "bootstrap/clojure/fixtures/rejected/compiler-c3-shape.gravity")))

(deftest sh04-malformed-c2-precedes-invalid-overrides-and-binding
  (let [producer bootstrap/compiler-c2-reader-source-artifact
        derive bootstrap/c3-syntax-overrides-from-forms
        binding-var (bootstrap-var 'sh04-syntax-current-binding!) binding @binding-var
        overrides (atom 0) bindings (atom 0)
        result (with-redefs-fn
                 {#'bootstrap/compiler-c2-reader-source-artifact
                  (fn [& arguments] (assoc (apply producer arguments) :kind :substituted))
                  #'bootstrap/c3-syntax-overrides-from-forms
                  (fn [forms] (swap! overrides inc) (derive forms))
                  binding-var (fn [source] (swap! bindings inc) (binding source))}
                 #(diagnostic (fn [] (construct (bad-override-text)))))]
    (is (= "C3-FACT-STALE" (:id result)))
    (is (= 0 @overrides @bindings))
    (is (= (source-path) (get-in result [:source-span :source])))))

(deftest sh04-valid-c2-invalid-overrides-stop-before-binding
  (let [binding-var (bootstrap-var 'sh04-syntax-current-binding!) binding @binding-var
        bindings (atom 0) events (atom [])
        result (observe-validations events
                 #(with-redefs-fn {binding-var (fn [source] (swap! bindings inc) (binding source))}
                    (fn [] (diagnostic (fn [] (construct (bad-override-text)))))))]
    (is (= "C3-SHAPE" (:id result)))
    (is (= :fixture-override (:producer result)))
    (is (= 1 (count @events)))
    (is (= 0 @bindings))))

(deftest sh04-standalone-public-entry-validates-every-call
  (let [events (atom [])
        results (observe-validations events
                  #(mapv (fn [_] (bootstrap/sh04-syntax-resolved-result! (source-path) @small-c2)) (range 2)))]
    (is (= 2 (count @events)))
    (is (= (first results) (second results)))
    (is (every? #(identical? @small-c2 (:c2 %)) @events))
    (is (= "C3-FACT-STALE"
           (:id (diagnostic #(bootstrap/sh04-syntax-resolved-result!
                              (source-path) (assoc @small-c2 :kind :substituted))))))
    (is (= '([source-path c2-artifact]) (:arglists (meta #'bootstrap/sh04-syntax-resolved-result!))))))

(deftest sh04-precomputed-and-caller-report-ingress-stays-rejected
  (let [report (bootstrap/c3-validate-c2-reader-artifact! (source-path) @small-c2)]
    (is (= "C2-HASH"
           (:id (diagnostic #(bootstrap/compiler-c3-syntax-source-artifact
                              (source-path) "1" (project) @small-c2)))))
    (is (= "C2-HASH"
           (:id (diagnostic #(bootstrap/compiler-c3-syntax-source-artifact
                              (source-path) "1" (project) @small-c2 report (reader-path))))))
    (is (thrown? clojure.lang.ArityException
                 (bootstrap/sh04-syntax-resolved-result! (source-path) @small-c2 report)))))
