(ns gravity.build.parent-capture-io-test
  "Compiler-free private DATA IO. Global cleanup=true positives are synthetic."
  (:require [clojure.test :refer [deftest is]]
            [clojure.edn :as edn]
            [gravity.build.parent-producer-observation :as parent]
            [gravity.build.producer-evidence :as evidence]
            [gravity.build.producer-evidence-test])
  (:import [java.nio.file Files Path LinkOption SecureDirectoryStream StandardOpenOption]
           [java.nio.file.attribute FileAttribute PosixFilePermissions]
           [java.nio.channels SeekableByteChannel]))

(def contract-bytes (var-get (ns-resolve 'gravity.build.producer-evidence-test 'contract-bytes)))
(defn fixture []
  ;; Reuse DATA constructors only; no older suite or actual producer is run.
  (let [snapshots ((var-get (ns-resolve 'gravity.build.producer-evidence-test 'source-shaped-snapshots)))
        {:keys [capture report]} ((var-get (ns-resolve 'gravity.build.producer-evidence-test 'fixture-with-snapshot)) (last snapshots))
        report (assoc report :test-producer-observation
                      (assoc (select-keys capture [:binding-before :binding-after :producer-provenance :applying-source])
                             :schema :gravity/sh01-fresh-producer-observation-v1))
        result {:status :passed :exit-code 0 :root-exited? true :wait-interrupted? false :wait-error nil
                :tracked-owned-processes-terminal? true :cleanup-complete? true :descendant-coverage :unknown
                :timeout-containment-unproven? false
                :stdout-capture-status :complete :stderr-capture-status :complete
                :stdout-truncated? false :stderr-truncated? false
                :stdout-capture-error nil :stderr-capture-error nil
                :stdout-capture-forced-close? false :stderr-capture-forced-close? false}]
    {:report report :observation {:schema :gravity/rv2-parent-process-observation-v1 :authority :non-authoritative
                                 :process-result result :capture-v1-ready? false :cache-admission? false}}))
(defn encoded [value] (.getBytes (pr-str value) "UTF-8"))
(defn error-of [f] (try (f) nil (catch Throwable error error)))
(defn with-root [f]
  (let [root (Files/createTempDirectory "RV2-capture-DATA-" (make-array FileAttribute 0))]
    (try (with-open [directory (Files/newDirectoryStream root)]
           (is (instance? SecureDirectoryStream directory))
           (f root directory))
         (finally
           (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
             (doseq [path (sort-by #(.getNameCount ^Path %) > (iterator-seq (.iterator paths)))]
               (Files/delete path)))))))
(defn put! [root name bytes]
  (Files/write (.resolve root name) bytes (make-array java.nio.file.OpenOption 0)))
(defn options [input-limit output-limit calls]
  {:input-limit-bytes input-limit :output-limit-bytes output-limit :admit-bytes! #(swap! calls conj %)})
(defn absent? [root name] (not (Files/exists (.resolve root name) (make-array LinkOption 0))))

(deftest real-IO-and-existing-serializer-publish-only-synthetic-unadmitted-bytes
  (with-root
    (fn [root directory]
      (let [{:keys [report observation]} (fixture) input (encoded report)
            expected (parent/serialize-capture contract-bytes report observation) calls (atom [])]
        (put! root "report.edn" input)
        (let [receipt (parent/publish-capture! contract-bytes observation directory "report.edn" directory "capture.edn"
                                              (options (alength input) (alength expected) calls))
              actual (Files/readAllBytes (.resolve root "capture.edn"))
              capture (edn/read-string (String. actual "UTF-8"))]
          (is (= (vec expected) (vec actual)))
          (is (= [(alength actual)] @calls))
          (is (= report (edn/read-string (:report-edn capture))))
          (is (= :gravity/reader-producer-capture-v1 (:schema capture)))
          (is (false? (:cache-admission? receipt)))
          (is (false? (:real-evidence-accepted? receipt)))
          (is (false? (:capture-v1-ready? observation)))
          (is (= (PosixFilePermissions/fromString "rw-------")
                 (Files/getPosixFilePermissions (.resolve root "capture.edn") (make-array LinkOption 0))))
          ;; Opening through the same handle proves caller ownership survived.
          (with-open [input (.newByteChannel ^SecureDirectoryStream directory (Path/of "report.edn" (make-array String 0))
                                            #{StandardOpenOption/READ} (make-array FileAttribute 0))]
            (is (.isOpen input))))))))

(deftest failed-parent-and-report-facts-publish-no-output
  (doseq [change [#(assoc-in % [:process-result :cleanup-complete?] nil)
                  #(assoc-in % [:process-result :cleanup-complete?] false)
                  #(assoc-in % [:process-result :status] :failed)
                  #(assoc-in % [:process-result :stdout-truncated?] true)
                  #(assoc-in % [:process-result :root-exited?] false)]]
    (with-root
      (fn [root directory]
        (let [{:keys [report observation]} (fixture) calls (atom [])]
          (put! root "report.edn" (encoded report))
          (is (some? (error-of #(parent/publish-capture! contract-bytes (change observation) directory "report.edn"
                                                        directory "capture.edn" (options 1048576 1048576 calls)))))
          (is (empty? @calls)) (is (absent? root "capture.edn"))))))
  (doseq [mutate [#(assoc % :namespaces ['DATA/wrong])
                  #(assoc-in % [:test-producer-observation :binding-after :source :commit] (apply str (repeat 40 "c")))
                  #(assoc-in % [:test-producer-observation :producer-provenance :repository-snapshot :hashed-byte-count] nil)]]
    (with-root
      (fn [root directory]
        (let [{:keys [report observation]} (fixture) calls (atom [])]
          (put! root "report.edn" (encoded (mutate report)))
          (is (some? (error-of #(parent/publish-capture! contract-bytes observation directory "report.edn"
                                                        directory "capture.edn" (options 1048576 1048576 calls)))))
          (is (empty? @calls)) (is (absent? root "capture.edn")))))))

(deftest strict-input-capture-and-output-allowances-refuse-before-creation
  (doseq [input [(byte-array [(unchecked-byte 0xc3) (unchecked-byte 0x28)])
                 (.getBytes "{} {}" "UTF-8") (.getBytes "#inst \"2020-01-01\"" "UTF-8")
                 (.getBytes "#DATA/tag {}" "UTF-8") (.getBytes "{" "UTF-8") (.getBytes "" "UTF-8")]]
    (with-root
      (fn [root directory]
        (let [{:keys [observation]} (fixture) calls (atom [])]
          (put! root "report.edn" input)
          (is (some? (error-of #(parent/publish-capture! contract-bytes observation directory "report.edn"
                                                        directory "capture.edn" (options 1048576 1048576 calls)))))
          (is (empty? @calls)) (is (absent? root "capture.edn"))))))
  (doseq [which [:input :output]]
    (with-root
      (fn [root directory]
        (let [{:keys [report observation]} (fixture) input (encoded report)
              output (parent/serialize-capture contract-bytes report observation) calls (atom [])]
          (put! root "report.edn" input)
          (is (some? (error-of #(parent/publish-capture! contract-bytes observation directory "report.edn" directory "capture.edn"
                                                        (options (if (= which :input) (dec (alength input)) (alength input))
                                                                 (if (= which :output) (dec (alength output)) (alength output)) calls)))))
          (is (empty? @calls)) (is (absent? root "capture.edn")))))))

(deftest relative-names-symlinks-collisions-and-byte-refusal-preserve-existing-inputs
  (doseq [name ["../report.edn" "/report.edn" "nested/report.edn" "." ".." ""]]
    (with-root
      (fn [root directory]
        (let [{:keys [observation]} (fixture) calls (atom [])]
          (is (some? (error-of #(parent/publish-capture! contract-bytes observation directory name directory "capture.edn"
                                                        (options 1048576 1048576 calls)))))
          (is (empty? @calls)) (is (absent? root "capture.edn"))))))
  (doseq [kind [:input-link :output-link :collision :admission]]
    (with-root
      (fn [root directory]
        (let [{:keys [report observation]} (fixture) calls (atom []) target (.resolve root "target.edn")
              old (.getBytes "unchanged DATA" "UTF-8") refusal (ex-info "DATA byte refusal" {})]
          (put! root "target.edn" old)
          (if (= kind :input-link)
            (Files/createSymbolicLink (.resolve root "report.edn") target (make-array FileAttribute 0))
            (put! root "report.edn" (encoded report)))
          (when (= kind :output-link)
            (Files/createSymbolicLink (.resolve root "capture.edn") target (make-array FileAttribute 0)))
          (when (= kind :collision) (put! root "capture.edn" old))
          (let [opts (cond-> (options 1048576 1048576 calls)
                       (= kind :admission) (assoc :admit-bytes! (fn [_] (throw refusal))))
                error (error-of #(parent/publish-capture! contract-bytes observation directory "report.edn" directory "capture.edn" opts))]
            (is (some? error))
            (when (= kind :admission) (is (identical? refusal error)))
            (is (= (vec old) (vec (Files/readAllBytes target))))
            (when (#{:input-link :admission} kind) (is (absent? root "capture.edn")))
            (when (= kind :collision) (is (= (vec old) (vec (Files/readAllBytes (.resolve root "capture.edn"))))))))))))

(defn channel [bytes read-error write-error close-error events]
  (let [position (atom 0)]
    (proxy [SeekableByteChannel] []
      (read [buffer]
        (when read-error (throw read-error))
        (let [n (min (.remaining buffer) (- (alength ^bytes bytes) @position))]
          (if (zero? n) -1 (do (.put buffer bytes @position n) (swap! position + n) n))))
      (write [buffer]
        (swap! events conj :write) (when write-error (throw write-error))
        (let [n (.remaining buffer)] (.position buffer (.limit buffer)) n))
      (size [] (alength ^bytes bytes)) (position [& _] @position) (isOpen [] true)
      (close [] (swap! events conj :close) (when close-error (throw close-error))))))
(defn directory [channel opens]
  (proxy [SecureDirectoryStream] []
    (newByteChannel [path opts attrs] (swap! opens conj [(str path) (set opts)]) channel)
    (close [] (throw (AssertionError. "Caller directory must stay open")))))

(deftest primary-and-close-errors-stay-identical-without-output-or-retry
  (doseq [stage [:read :validation :input-close :write :output-close :self-close]]
    (let [{:keys [report observation]} (fixture) primary (java.io.IOException. (str "DATA " stage))
          secondary (java.io.IOException. "DATA close") captured (atom nil) events (atom []) in-opens (atom []) out-opens (atom [])
          input (channel (encoded report) (when (#{:read :self-close} stage) primary) nil
                         (case stage (:read :validation) secondary :input-close primary :self-close primary nil) events)
          output (channel (byte-array 0) nil (when (= stage :write) primary)
                          (case stage :write secondary :output-close primary nil) events)
          input-dir (directory input in-opens) output-dir (directory output out-opens)
          original evidence/serialize-observed-fresh-capture
          run #(parent/publish-capture! contract-bytes (if (= stage :validation)
                                                       (assoc-in observation [:process-result :cleanup-complete?] nil) observation)
                                        input-dir "report.edn" output-dir "capture.edn" (options 1048576 1048576 (atom [])))
          error (with-redefs [evidence/serialize-observed-fresh-capture
                             (fn [& args] (try (apply original args)
                                              (catch Throwable error (reset! captured error) (throw error))))]
                  (error-of run))
          expected (if (= stage :validation) @captured primary)]
      (is (identical? expected error))
      (is (= (if (#{:read :validation :write} stage) [secondary] []) (vec (.getSuppressed ^Throwable error))))
      (is (= 1 (count @in-opens)))
      (is (= (if (#{:write :output-close} stage) 1 0) (count @out-opens))))))

(deftest no-producer-Git-compiler-or-target-was-loaded
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.c2-artifact-identity-test))))
