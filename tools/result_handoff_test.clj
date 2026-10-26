(ns result-handoff-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.java.io :as io]
            [clojure.string :as str]))
(binding [*command-line-args* nil] (load-file "tools/result_handoff.clj"))
(def hash-a (apply str (repeat 64 "a")))
(def oid (apply str (repeat 40 "b")))
(def document {:sha256 hash-a :bytes 120
               :value {:before {:commit oid :tree oid :status ""}
                       :process {:exit 1 :elapsed-ms 42 :argv ["clojure" "/private/local/test.clj"]}
                       :counts {:test 2 :pass 3 :fail 1 :error 0}
                       :repeated [{:counts {:test 2 :pass 3 :fail 1 :error 0}}]
                       :private "/Users/private/secret" :operation :uncertain
                       :unrun true}})
(def plan {:receipts [{:id "process" :file "/private/receipt.edn" :sha256 hash-a}]
           :fields [{:label "head" :receipt "process" :path [:before :commit] :type :oid}
                    {:label "tree" :receipt "process" :path [:before :tree] :type :oid}
                    {:label "clean" :receipt "process" :path [:before :status] :type :clean-status}
                    {:label "exit" :receipt "process" :path [:process :exit] :type :integer}
                    {:label "argv" :receipt "process" :path [:process :argv] :type :argv-digest}
                    {:label "counts" :receipt "process" :path [:counts] :type :counts}
                    {:label "operation" :receipt "process" :path [:operation] :type :status}
                    {:label "unrun" :receipt "process" :path [:unrun] :type :boolean}]})
(defn rejects? [f] (try (f) false (catch Exception _ true)))
(deftest immutable-typed-projection
  (let [packet (result-handoff/project plan (constantly document))
        fields (into {} (map (juxt :label :value) (:fields packet)))
        reread (clojure.edn/read-string (pr-str packet))]
    (is (= packet reread))
    (is (= :none (:authority packet)))
    (is (false? (:new-execution packet)))
    (is (= 1 (get fields "exit")))
    (is (= (:counts (:value document)) (get fields "counts")))
    (is (= :uncertain (get fields "operation")))
    (is (true? (get fields "unrun")))
    (is (not (str/includes? (pr-str packet) "/private/")))
    (is (not (str/includes? (pr-str packet) "/Users/")))
    (is (= hash-a (get-in packet [:receipts 0 :sha256])))
    (is (= [:counts] (get-in packet [:fields 5 :path])))
    ;; A claimed zero or passed status stays attributed, never accepted.
    (is (= :retained-producer-receipts (:observation-source packet)))
    (is (not (contains? packet :accepted)))))
(deftest invalid-plans-and-evidence
  (doseq [changed [(assoc plan :acceptance :passed)
                   (assoc plan :receipts [])
                   (update plan :receipts conj (first (:receipts plan)))
                   (update plan :fields conj (first (:fields plan)))
                   (assoc-in plan [:fields 0 :path] [:missing])
                   (assoc-in plan [:fields 0 :receipt] "missing")
                   (assoc-in plan [:fields 0 :type] :raw)
                   (assoc-in plan [:fields 0 :type] :integer)
                   (assoc-in plan [:fields 0 :path] [])
                   (assoc-in plan [:fields 0 :label] "/private/name")
                   (assoc-in plan [:receipts 0 :sha256] (apply str (repeat 64 "c")))]]
    (is (rejects? #(result-handoff/project changed (constantly document)))))
  (doseq [[type value] [[:counts {:test 1 :pass -1}] [:counts {:tests 1 :retries 0}]
                       [:integer -1] [:integer 2.5] [:boolean nil]
                       [:status :private/name] [:selector "/private/name"]
                       [:selector "https://example.com"] [:oid "abc"]
                       [:sha256 oid] [:argv-digest []]]]
    (is (rejects? #(result-handoff/typed-value type value)))))
(deftest retained-document-reader
  (let [dir (.toFile (java.nio.file.Files/createTempDirectory "handoff-test-" (make-array java.nio.file.attribute.FileAttribute 0)))
        file (io/file dir "receipt.edn")]
    (try
      (spit file "{:exit 0}\n")
      (let [doc (result-handoff/read-document file)]
        (is (= {:exit 0} (:value doc)))
        (is (= 10 (:bytes doc)))
        (is (= (result-handoff/sha256 (java.nio.file.Files/readAllBytes (.toPath file))) (:sha256 doc))))
      (doseq [text ["" "{} {}" "{:x 1 :x 2}" "#=(System/exit 0)" "#unknown {}"]]
        (spit file text)
        (is (rejects? #(result-handoff/read-document file))))
      (java.nio.file.Files/write (.toPath file) (byte-array [(unchecked-byte 255)]) (make-array java.nio.file.OpenOption 0))
      (is (rejects? #(result-handoff/read-document file)))
      (let [link (.toPath (io/file dir "link.edn"))]
        (java.nio.file.Files/createSymbolicLink link (.toPath file) (make-array java.nio.file.attribute.FileAttribute 0))
        (is (rejects? #(result-handoff/read-document link)))
        (java.nio.file.Files/delete link))
      (finally (.delete file) (.delete dir)))))

(defn invoke-cli [args]
  (let [java (str (io/file (System/getProperty "java.home") "bin" "java"))
        classpath (str (System/getProperty "java.class.path") java.io.File/pathSeparator
                       (.getCanonicalPath (io/file "tools")))
        argv (into [java "-Xmx128m" "-cp" classpath
                    "clojure.main" "-m" "result-handoff"] args)
        process (.start (doto (ProcessBuilder. ^java.util.List argv) (.redirectErrorStream true)))
        finished (.waitFor process 10 java.util.concurrent.TimeUnit/SECONDS)]
    (when-not finished (.destroyForcibly process) (.waitFor process))
    {:exit (.exitValue process) :finished finished :output (slurp (.getInputStream process))}))

(deftest cli-producer-consumer-roundtrip
  (let [dir (.toFile (java.nio.file.Files/createTempDirectory "handoff-cli-" (make-array java.nio.file.attribute.FileAttribute 0)))
        receipt (io/file dir "receipt.edn")
        plan-file (io/file dir "plan.edn")]
    (try
      (spit receipt (pr-str (:value document)))
      (let [captured (result-handoff/read-document receipt)
            selected (assoc-in (assoc-in plan [:receipts 0 :file] (str receipt))
                               [:receipts 0 :sha256] (:sha256 captured))]
        (spit plan-file (pr-str selected))
        (let [output (with-out-str (result-handoff/-main (str plan-file)))
              packet (clojure.edn/read-string output)]
          (is (= (:sha256 (result-handoff/read-document plan-file)) (:plan-sha256 packet)))
          (is (= (:sha256 captured) (get-in packet [:receipts 0 :sha256])))
          (is (= (assoc (result-handoff/project selected result-handoff/read-document)
                        :plan-sha256 (:plan-sha256 packet)) packet))
          (is (not (str/includes? output (str dir)))))
        (let [actual (invoke-cli [(str plan-file)])]
          (is (:finished actual))
          (is (= 0 (:exit actual)))
          (is (= :result-handoff-v1 (:schema (clojure.edn/read-string (:output actual))))))
        ;; Original producer evidence is immutable for this version of the plan.
        (spit receipt "{:changed true}")
        (is (rejects? #(result-handoff/-main (str plan-file)))))
      (is (rejects? #(result-handoff/-main)))
      (let [actual (invoke-cli [])]
        (is (:finished actual))
        (is (not= 0 (:exit actual))))
      (finally (.delete receipt) (.delete plan-file) (.delete dir)))))

(deftest inherited-arguments-do-not-dispatch-library-main
  (binding [*command-line-args* ["unrelated" "arguments"]]
    (is (= "" (with-out-str (load-file "tools/result_handoff.clj")))))
  (is (fn? result-handoff/-main)))

(deftest bounded-capture-covers-growing-and-oversized-input
  (let [observed (atom 0)
        channel (reify java.nio.channels.ReadableByteChannel
                  (isOpen [_] true)
                  (close [_])
                  (read [_ buffer]
                    ;; More bytes become available on every read. No initial
                    ;; size observation can justify a later allocation/read.
                    (let [n (min 3 (.remaining ^java.nio.ByteBuffer buffer))]
                      (dotimes [_ n] (.put ^java.nio.ByteBuffer buffer (byte 32)))
                      (swap! observed + n)
                      n)))]
    (is (rejects? #(result-handoff/read-bounded-bytes channel 16)))
    (is (= 17 @observed)))
  (let [bytes (.getBytes "1234567890123456" "UTF-8")]
    (with-open [channel (java.nio.channels.Channels/newChannel
                        (java.io.ByteArrayInputStream. bytes))]
      (is (= (seq bytes) (seq (result-handoff/read-bounded-bytes channel 16))))))
  (let [file (java.io.File/createTempFile "handoff-oversized-" ".edn")]
    (try
      (with-open [random (java.io.RandomAccessFile. file "rw")]
        (.setLength random (inc result-handoff/max-evidence-bytes)))
      (is (rejects? #(result-handoff/read-document file)))
      (finally (.delete file)))))

(let [result (run-tests)]
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
