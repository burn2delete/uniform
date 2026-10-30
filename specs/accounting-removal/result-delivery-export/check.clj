;; Extracted existing writer + thin export; owned DATA IO only, no target load.
(require '[clojure.java.io :as io] '[clojure.edn :as edn] '[clojure.test :as test :refer [deftest is run-tests]])
(def roots (atom []))
(def source (or (System/getProperty "gravity.delivery-export-source")
                "bootstrap/clojure/test/gravity/development_test_runner.clj"))
(def forms
  (with-open [r (java.io.PushbackReader. (io/reader source))]
    (binding [*read-eval* false]
      (loop [out {}]
        (let [f (read {:eof ::eof} r)]
          (if (= f ::eof) out
              (recur (if (and (seq? f) (#{'defn 'defn-} (first f))) (assoc out (second f) f) out))))))))
(def model (create-ns 'result-delivery.export-model))
(binding [*ns* model]
  (clojure.core/refer 'clojure.core) (alias 'io 'clojure.java.io)
  (eval (get forms 'machine-result-file-sink)) (eval (get forms 'result-delivery-sink)))
(def factory (var-get (ns-resolve model 'result-delivery-sink)))
(def selection {:namespaces ["gravity.fixture-test"] :test-vars ["gravity.fixture-test/a"] :fail-fast? false})
(def request {:id "model-delivery" :source {:source-commit "supplied-DATA"} :selection selection})
(def result {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
             :selection selection :summary {:test 1 :pass 2 :fail 0 :error 0} :outcome :passed :exit-code 0
             :model-note "Unicode \u03bb"})
(defn delivery []
  (let [root (java.nio.file.Files/createTempDirectory "delivery-export-model-" (make-array java.nio.file.attribute.FileAttribute 0))]
    (swap! roots conj root)
    {:file (str (.resolve root "result.edn")) :limit-bytes 65536 :request request}))
(defn error-of [f] (try (f) nil (catch Throwable error error)))
(defn id-of [f] (:id (ex-data (error-of f))))
(deftest actual-export-writes-one-utf8-frame-before-inherited
  (let [d (delivery) seen (atom [])
        sink (factory d #(do (is (.exists (io/file (:file d)))) (swap! seen conj %)))]
    (sink result)
    (is (= 1 (count @seen))) (is (identical? result (first @seen)))
    (let [text (slurp (:file d) :encoding "UTF-8") record (edn/read-string text)]
      (is (= {:schema :gravity/development-test-result-delivery-v1 :request request :result result} record))
      (is (.endsWith text "\n"))
      (is (= text (str (pr-str record) "\n")))))
  (let [d (delivery) sink (factory d nil)] (sink result) (is (.exists (io/file (:file d))))))
(deftest closed-delivery-and-request-refusals
  (doseq [mutate [#(assoc % :extra true) #(dissoc % :file) #(assoc % :file "relative.edn")
                  #(assoc % :file nil) #(assoc % :limit-bytes 0) #(assoc % :limit-bytes -1)
                  #(assoc % :limit-bytes 1.5) #(assoc % :limit-bytes Integer/MAX_VALUE)
                  #(assoc-in % [:request :extra] true) #(assoc-in % [:request :id] "")
                  #(assoc-in % [:request :source] nil) #(assoc-in % [:request :selection] nil)]]
    (let [d (delivery)]
      (is (= "DEV-RESULT-DELIVERY" (id-of #(factory (mutate d) nil))))
      (is (not (.exists (io/file (:file d))))))))
(deftest selection-and-one-shot-consumption
  (let [d (delivery) calls (atom 0) sink (factory d (fn [_] (swap! calls inc)))]
    (is (= "DEV-RESULT-SELECTION" (id-of #(sink (assoc result :selection {})))))
    (is (zero? @calls)) (is (not (.exists (io/file (:file d)))))
    (is (= "DEV-RESULT-DUPLICATE" (id-of #(sink result)))))
  (let [d (delivery) sink (factory d nil)]
    (sink result) (let [before (slurp (:file d))]
                    (is (= "DEV-RESULT-DUPLICATE" (id-of #(sink result))))
                    (is (= before (slurp (:file d)))))))
(deftest byte-limit-refuses-before-creation-and-inherited
  (let [d (assoc (delivery) :limit-bytes 1) calls (atom 0)
        sink (factory d (fn [_] (swap! calls inc)))]
    (is (= "DEV-RESULT-LIMIT" (id-of #(sink result))))
    (is (not (.exists (io/file (:file d))))) (is (zero? @calls))
    (is (= "DEV-RESULT-DUPLICATE" (id-of #(sink result))))))
(deftest existing-file-directory-and-symlink-refuse-without-overwrite
  (doseq [kind [:file :directory :symlink]]
    (let [d (delivery) path (.toPath (io/file (:file d))) target (.resolve (.getParent path) "target.edn")
          calls (atom 0)]
      (spit (.toFile target) "unchanged-target")
      (case kind
        :file (spit (:file d) "unchanged-file")
        :directory (java.nio.file.Files/createDirectory path (make-array java.nio.file.attribute.FileAttribute 0))
        :symlink (java.nio.file.Files/createSymbolicLink path target (make-array java.nio.file.attribute.FileAttribute 0)))
      (let [sink (factory d (fn [_] (swap! calls inc))) error (error-of #(sink result))]
        (is (instance? java.io.IOException error)) (is (zero? @calls))
        (is (= "unchanged-target" (slurp (.toFile target))))
        (when (= kind :file) (is (= "unchanged-file" (slurp (:file d)))))
        (when (= kind :symlink) (is (java.nio.file.Files/isSymbolicLink path)))
        (is (= "DEV-RESULT-DUPLICATE" (id-of #(sink result))))))))
(deftest original-inherited-error-propagates-after-successful-write
  (doseq [error [(ex-info "model inherited" {}) (InterruptedException. "model inherited") (LinkageError. "model inherited")]]
    (let [d (delivery) calls (atom 0) sink (factory d (fn [_] (swap! calls inc) (throw error)))]
      (is (identical? error (error-of #(sink result))))
      (is (= 1 @calls)) (is (.exists (io/file (:file d))))
      (is (= "DEV-RESULT-DUPLICATE" (id-of #(sink result)))))))
(let [summary (run-tests 'user)]
  (doseq [root @roots]
    (with-open [paths (java.nio.file.Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (sort-by #(.getNameCount ^java.nio.file.Path %) (iterator-seq (.iterator paths))))]
        (java.nio.file.Files/deleteIfExists path))))
  (assert (nil? (find-ns 'gravity.development-test-runner))) (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :result-delivery-export/dataio-v1 :summary summary :actual-children 0 :actual-leases 0
        :actual-locks 0 :actual-process-probes 0 :target-runtime :UNRUN :directory-ownership :fixture-only})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
