(ns gravity.build.trace-semantic-delivery-test
  "Selected output/correlation DATA and private filesystem controls only."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.build.trace-input-syntax-child :as child]
            [gravity.build.trace-input-syntax-receipt :as receipt]
            [gravity.build.trace-semantic-delivery :as delivery]
            [gravity.development-test-runner :as development])
  (:import [java.nio.channels SeekableByteChannel]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path SecureDirectoryStream]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]))

(defn- expected []
  {:source {:source-commit (apply str (repeat 40 "a")) :source-tree (apply str (repeat 40 "b"))
            :configuration-sha256 (apply str (repeat 64 "c"))}
   :test-source-sha256 (apply str (repeat 64 "d")) :evidence-kind :data-unit-control})

(defn- request []
  {:id "00000000-0000-0000-0000-000000000001" :source (:source (expected))
   :selection {:namespaces [(namespace receipt/selector)] :test-vars [(str receipt/selector)]
               :fail-fast? false}})

(defn- machine [passed?]
  {:schema development/machine-result-schema :authority :non-authoritative :authoritative? false
   :selection (:selection (request))
   :summary {:test 1 :pass 1 :fail (if passed? 0 1) :error 0}
   :outcome (if passed? :passed :failed) :exit-code (if passed? 0 1)})

(defn- record-data [passed?]
  (let [request (request) expected (expected) machine (machine passed?)]
    {:schema :gravity/trace-input-syntax-observation-v1 :selector receipt/selector
     :method-status :constructed-unadmitted :genuine-execution-established? false :cache-admission? false
     :validation (if passed? :consistent-supplied-observation :nonpassing)
     :checks {:DATA-consistency-check passed?}
     :observation {:phase :trace-control-complete :field :input-syntax-id :alias-restored? true
                   :reached-second-id? true :earlier-gate-refusal? false
                   :public-positive-report {:status :passed :fixture :DATA}
                   :no-verifier-report-substitution? true :whole-rv1-closed? false
                   :changed-error {:via [{:type 'clojure.lang.ExceptionInfo
                                          :message "DATA outer refusal"
                                          :data {:id "C5-UNRESOLVED"}}]
                                   :cause "DATA inner cause" :data {:id "DATA-INNER"}}}
     :completion {:returned? true :run-return (:exit-code machine) :run-error nil :error nil
                  :counts (:summary machine) :machine-results [machine]
                  :failure-reports (if passed? [] [{:type :fail :fixture :DATA}])}
     :provenance (merge (:source request)
                        {:request-id (:id request) :selector receipt/selector
                         :test-source-sha256 (:test-source-sha256 expected)
                         :evidence-kind :data-unit-control})
     :restoration-source :selected-test-assertions-including-finally}))

(defn- with-root [f]
  (let [root (Files/createTempDirectory "trace-semantic-DATA-" (make-array FileAttribute 0))]
    (try
      (with-open [stream (Files/newDirectoryStream root)]
        (is (instance? SecureDirectoryStream stream))
        (f root stream))
      (finally
        (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
          (doseq [path (sort-by #(.getNameCount ^Path %) > (iterator-seq (.iterator paths)))]
            (Files/delete path)))))))

(defn- put-bytes! [root bytes]
  (let [directory (.resolve root (:id (request)))]
    (Files/createDirectories directory (make-array FileAttribute 0))
    (Files/write (.resolve directory receipt/receipt-name) bytes (make-array java.nio.file.OpenOption 0))))

(defn- encoded [record] (.getBytes (pr-str record) StandardCharsets/UTF_8))

(deftest child-owns-private-handle-and-delegates-existing-ports-without-transfer
  (with-root
    (fn [root stream]
      (let [observations (atom {:fixture :DATA}) calls (atom []) handle (atom nil)
            completion-sink (fn [m] (swap! calls conj [:machine m]))
            inherited development/*machine-result-sink*]
        (with-redefs [child/invoke!
                      (fn [actual directory admit provenance]
                        (is (identical? observations actual))
                        (is (instance? SecureDirectoryStream directory))
                        (reset! handle directory)
                        (is (identical? completion-sink development/*machine-result-sink*))
                        (is (= (:provenance (record-data true)) provenance))
                        (development/*machine-result-sink* (machine true))
                        {:run-return 0 :binding-status :consistent-supplied-observation
                         :publication (receipt/publish! directory (record-data true) admit)
                         :method-status :constructed-unadmitted :genuine-execution-established? false})]
          (let [result (delivery/invoke-child! root (request) (expected) observations completion-sink
                                               #(swap! calls conj [:admit %]))
                correlated (delivery/read-correlated! stream (request) (expected) (machine true) 16777216)]
            (is (= :consistent-supplied-observation (:binding-status result)))
            (is (= [:machine :admit] (mapv first @calls)))
            (is (= :matched-supplied-observation (:correlation correlated)))
            (is (= :data-unit-control (get-in correlated [:record :provenance :evidence-kind])))
            (is (false? (:genuine-execution-established? correlated)))
            (is (identical? inherited development/*machine-result-sink*))
            (is (= (PosixFilePermissions/fromString "rwx------")
                   (Files/getPosixFilePermissions (.resolve root (:id (request))) (make-array LinkOption 0))))
            (is (thrown? java.nio.file.ClosedDirectoryStreamException
                         (.newDirectoryStream ^SecureDirectoryStream @handle
                                              (Path/of "." (make-array String 0))
                                              (make-array LinkOption 0))))))))))

(deftest original-child-throw-restores-sink-and-does-not-delete-or-reuse-directory
  (with-root
    (fn [root _]
      (let [original (ex-info "DATA original child throw" {}) calls (atom 0)
            inherited development/*machine-result-sink*]
        (with-redefs [child/invoke! (fn [& _] (swap! calls inc) (throw original))]
          (is (identical? original
                          (try (delivery/invoke-child! root (request) (expected) (atom nil) (fn [_]) (fn [_]))
                               (catch Throwable error error))))
          (is (identical? inherited development/*machine-result-sink*))
          (is (thrown? java.nio.file.FileAlreadyExistsException
                       (delivery/invoke-child! root (request) (expected) (atom nil) (fn [_]) (fn [_]))))
          (is (= 1 @calls))
          (is (Files/isDirectory (.resolve root (:id (request))) (make-array LinkOption 0))))))))

(deftest semantic-corruption-and-wrong-request-cannot-correlate
  (doseq [[label mutate]
          [[:request #(assoc-in % [:provenance :request-id] "DATA-other")]
           [:config #(assoc-in % [:provenance :configuration-sha256] (apply str (repeat 64 "e")))]
           [:source #(assoc-in % [:provenance :source-commit] (apply str (repeat 40 "f")))]
           [:selector #(assoc % :selector 'DATA/other)]
           [:counts #(assoc-in % [:completion :counts :pass] 2)]
           [:machine #(assoc-in % [:completion :machine-results 0 :exit-code] 1)]
           [:outcome #(assoc % :validation :nonpassing)]
           [:restoration #(assoc-in % [:observation :alias-restored?] false)]
           [:public-positive #(assoc-in % [:observation :public-positive-report :status] :failed)]
           [:reachability #(assoc-in % [:observation :reached-second-id?] false)]
           [:earlier-refusal #(assoc-in % [:observation :earlier-gate-refusal?] true)]
           [:outer-refusal #(assoc-in % [:observation :changed-error :via 0 :data :id] "DATA-other")]
           [:error #(assoc-in % [:completion :error] {:fixture :DATA})]
           [:unknown-cleanup #(assoc % :cleanup-complete? true)]
           [:authority #(assoc % :genuine-execution-established? true)]]]
    (testing (name label)
      (is (thrown? clojure.lang.ExceptionInfo
                   (delivery/correlate (request) (expected) (machine true) (mutate (record-data true)))))))
  (is (thrown? clojure.lang.ExceptionInfo
               (delivery/correlate (assoc-in (request) [:source :source-tree] "DATA")
                                   (expected) (machine true) (record-data true))))
  (is (thrown? IllegalArgumentException
               (delivery/correlate (assoc (request) :id "../DATA")
                                   (expected) (machine true) (record-data true)))))

(deftest strict-read-refuses-extra-frames-invalid-UTF8-and-capture-overflow
  (doseq [bytes [(encoded (str "DATA"))
                 (.getBytes (str (pr-str (record-data true)) "\n{}") StandardCharsets/UTF_8)
                 (.getBytes "#untrusted/DATA {}" StandardCharsets/UTF_8)
                 (byte-array [(unchecked-byte 0xc3) (unchecked-byte 0x28)])]]
    (with-root
      (fn [root stream]
        (put-bytes! root bytes)
        (is (thrown? Exception (delivery/read-correlated! stream (request) (expected) (machine true) 16777216))))))
  (with-root
    (fn [root stream]
      (let [bytes (encoded (record-data true))]
        (put-bytes! root bytes)
        (is (= :matched-supplied-observation
               (:correlation (delivery/read-correlated! stream (request) (expected) (machine true) (alength bytes)))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (delivery/read-correlated! stream (request) (expected) (machine true) (dec (alength bytes)))))))))

(deftest symlink-receipt-is-not-followed
  (with-root
    (fn [root stream]
      (let [directory (.resolve root (:id (request))) target (.resolve root "DATA-target")]
        (Files/createDirectory directory (make-array FileAttribute 0))
        (Files/write target (encoded (record-data true)) (make-array java.nio.file.OpenOption 0))
        (Files/createSymbolicLink (.resolve directory receipt/receipt-name) target (make-array FileAttribute 0))
        (is (thrown? java.io.IOException
                     (delivery/read-correlated! stream (request) (expected) (machine true) 16777216)))))))

(deftest callback-correlates-before-simulated-disposition-without-replacing-result
  (with-root
    (fn [root stream]
      (put-bytes! root (encoded (record-data true)))
      (let [audit (atom nil) events (atom [:DATA-common-strict-read :DATA-normalize])
            callback (delivery/inside-lease-callback stream (expected) 16777216 audit)
            result {:development-result (machine true)}
            observation {:job {:namespace (symbol (namespace receipt/selector))} :tag :returned
                         :delivery-request (request) :result result :error nil}]
        (callback observation)
        (swap! events conj :semantic-correlation)
        (swap! events conj :DATA-disposition :DATA-parent-report)
        (is (identical? result (get-in @audit [:observation :result])))
        (is (= :correlated (:state @audit)))
        (is (= [:DATA-common-strict-read :DATA-normalize :semantic-correlation
                :DATA-disposition :DATA-parent-report] @events))
        (is (thrown? clojure.lang.ExceptionInfo (callback observation)))))))

(deftest original-error-partial-facts-and-matched-nonpassing-results-stay-separate
  (let [original (ex-info "DATA original start/read error" {}) audit (atom nil)
        observation {:tag :threw :error original :delivery-request (request)}
        callback (delivery/inside-lease-callback nil (expected) 16777216 audit)]
    (callback observation)
    (is (= :not-observed (:state @audit)))
    (is (identical? original (:error @audit)))
    (is (identical? observation (:observation @audit))))
  (let [result (delivery/correlate (request) (expected) (machine false) (record-data false))]
    (is (= :matched-nonpassing-observation (:correlation result)))
    (is (false? (:genuine-execution-established? result)))
    (is (not (contains? result :cleanup-complete?)))))

(defn- error-of [f] (try (f) nil (catch Throwable error error)))

(defn- injected-read-root [read-error input-close-error directory-close-error events]
  ;; Explicit DATA channel and handles. No process, lease or real owner lock.
  (let [bytes (encoded (record-data true)) position (atom 0)
        channel (proxy [SeekableByteChannel] []
                  (read [buffer]
                    (when read-error (throw read-error))
                    (let [n (min (.remaining buffer) (- (alength bytes) @position))]
                      (if (zero? n) -1
                          (do (.put buffer bytes @position n) (swap! position + n) n))))
                  (size [] (alength bytes))
                  (position [& _] @position)
                  (isOpen [] true)
                  (close [] (swap! events conj :input-close)
                    (when input-close-error (throw input-close-error))))
        directory (proxy [SecureDirectoryStream] []
                    (newByteChannel [_ _ _] channel)
                    (close [] (swap! events conj :directory-close)
                      (when directory-close-error (throw directory-close-error))))]
    (proxy [SecureDirectoryStream] []
      (newDirectoryStream [_ _] directory)
      (close [] (throw (AssertionError. "Caller root must remain open"))))))

(deftest parent-read-preserves-primary-and-distinct-close-errors-in-order
  (doseq [[label read? input? directory?]
          [[:read-input true true false] [:read-directory true false true]
           [:read-both true true true] [:input-only false true false]
           [:directory-only false false true] [:both-close-only false true true]]]
    (testing (name label)
      (let [primary (when read? (java.io.IOException. "DATA read"))
            input-error (when input? (java.io.IOException. "DATA input close"))
            directory-error (when directory? (java.io.IOException. "DATA directory close"))
            errors (vec (remove nil? [primary input-error directory-error]))
            events (atom [])
            error (error-of #(delivery/read-correlated!
                              (injected-read-root primary input-error directory-error events)
                              (request) (expected) (machine true) 16777216))]
        (is (identical? (first errors) error))
        (is (= (subvec errors 1) (vec (.getSuppressed ^Throwable error))))
        (is (= [:input-close :directory-close] @events)))))
  (doseq [self? [true false]]
    (let [primary (java.io.IOException. "DATA read")
          close-error (if self? primary (java.io.IOException. "DATA repeated close"))
          events (atom [])
          error (error-of #(delivery/read-correlated!
                            (injected-read-root primary close-error close-error events)
                            (request) (expected) (machine true) 16777216))]
      (is (identical? primary error))
      (is (= (if self? [] [close-error]) (vec (.getSuppressed ^Throwable error))))
      (is (= [:input-close :directory-close] @events)))))

(deftest validation-and-callback-audit-retain-primary-with-cleanup-facts
  (doseq [validation? [false true]]
    (let [primary (ex-info "DATA original read or validation" {})
          input-error (java.io.IOException. "DATA input close")
          directory-error (java.io.IOException. "DATA directory close")
          events (atom []) audit (atom nil)
          root (injected-read-root (when-not validation? primary) input-error directory-error events)
          callback (delivery/inside-lease-callback root (expected) 16777216 audit)
          observation {:tag :returned :job {:namespace (symbol (namespace receipt/selector))}
                       :delivery-request (request) :result {:development-result (machine true)}}
          run #(callback observation)
          error (if validation?
                  (with-redefs [delivery/correlate (fn [& _] (throw primary))] (error-of run))
                  (error-of run))]
      (is (identical? primary error))
      (is (= [input-error directory-error] (vec (.getSuppressed ^Throwable error))))
      (is (= :refused (:state @audit)))
      (is (identical? primary (:error @audit)))
      (is (identical? observation (:observation @audit)))
      (is (= [:input-close :directory-close] @events)))))

(deftest no-genuine-runtime-or-launcher-was-loaded
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.self-hosting.resolution-parent-mutation-test)))
  (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))
  (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker))))
