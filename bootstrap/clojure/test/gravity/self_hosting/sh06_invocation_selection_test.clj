(ns gravity.self-hosting.sh06-invocation-selection-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

;; Host invocation/arity controls only. Mocked reports below confer no artifact
;; acceptance or migration authority; real composition remains a separate test.
(defn- private-var [name] (or (ns-resolve 'gravity.bootstrap name) (throw (ex-info "Missing SH06 seam" {:name name}))))
(def ^:private source (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/resolution.gravity")))
(def ^:private invocation
  {:reader-source-path b/sh03-reader-source-relative-path
   :authenticated-envelope-options {:source :development-current}})
(defn- failure [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest selection-appends-without-repurposing-the-existing-context
  (is (= [1 2 3] (mapv count (:arglists (meta #'b/sh06-resolution-artifact-verification)))))
  (is (= [1 2 3 4 5] (mapv count (:arglists (meta #'b/sh06-resolution-artifact-from-sh05-artifact)))))
  (is (= [1 2] (mapv count (:arglists (meta #'b/sh06-resolution-serialize)))))
  (is (= [1 2] (mapv count (:arglists (meta #'b/sh06-resolution-deserialize)))))
  (doseq [report [(b/sh06-resolution-artifact-verification {} nil)
                 (b/sh06-resolution-artifact-verification {} nil invocation)
                 (b/sh06-resolution-artifact-verification {} invocation)
                 (b/sh06-resolution-artifact-verification {} :gravity.bootstrap/legacy)]]
    (is (= :failed (:status report)))
    (is (= :failed (get-in report [:context-validation :status])))))

(deftest invocation-is-lexical-validated-and-never-cached
  (let [with-invocation (private-var 'sh06-resolution-with-reader-invocation)
        current (b/sh06-resolution-current-binding! source)
        inside (with-invocation source invocation #(b/sh06-resolution-current-binding! source))]
    (is (= invocation (:reader-invocation inside)))
    (is (= (:input-key current) (:input-key inside)))
    (is (= (:source-content-hash current) (:source-content-hash inside)))
    (is (not (contains? (b/sh06-resolution-current-binding! source) :reader-invocation)))
    (is (not (contains? @(var-get (private-var 'sh06-resolution-cached-binding)) :reader-invocation)))
    (doseq [selection [(assoc invocation :extra true)
                      (assoc invocation :reader-source-path "foreign.gravity")
                      (assoc invocation :authenticated-envelope-options {:source :other})]]
      (let [ran? (atom false)]
        (is (some? (failure #(with-invocation source selection (fn [] (reset! ran? true))))))
        (is (false? @ran?))))))

(deftest existing-receipt-epoch-distinguishes-current-and-pinned-selection
  (let [epochs (atom [])
        report-var (private-var 'sh07-proof-transaction-report)]
    (with-redefs-fn
      {report-var (fn [_ _ epoch _ _]
                    (swap! epochs conj epoch)
                    {:status :passed :checks {}})}
      #(do (b/sh06-resolution-artifact-verification {} :gravity.bootstrap/legacy invocation)
           (b/sh06-resolution-artifact-verification {})))
    (is (= 2 (count @epochs)))
    (is (= invocation (:reader-invocation (first @epochs))))
    (is (nil? (:reader-invocation (second @epochs))))
    (is (not= (first @epochs) (second @epochs)))
    (is (nil? (var-get (private-var '*sh06-resolution-operation-binding*))))))

(deftest selected-reader-path-cannot-disagree-with-the-host-invocation
  (let [result
        (failure #(b/sh06-resolution-artifact-from-sh05-artifact
                    source nil {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                    "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity" invocation))]
    (is (= "C5-UNRESOLVED" (:id result)))
    (is (= [:selected-current-reader-invocation] (:missing-fields result)))))
