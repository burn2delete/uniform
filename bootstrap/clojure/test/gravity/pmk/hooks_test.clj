(ns gravity.pmk.hooks-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.pmk.controller :as controller]
            [gravity.pmk.controller-test :as model]
            [gravity.pmk.first-request :as first]
            [gravity.pmk.first-request-test :as fixture]
            [gravity.pmk.hooks :as hooks]
            [clojure.java.shell :as shell]))

(defn utf8 [x] (.getBytes ^String x "UTF-8"))
(defn setup []
  (let [m (model/model) request (:request m) binding (:binding m)
        git (assoc (:checkout request) :tracked-status "")
        files {"/synthetic/input" fixture/h} events (atom []) writes (atom {})
        freeze {:request request :manifest {:synthetic true} :observation {:synthetic :observation}
                :binding binding :files files :artifact-limit-bytes 1048576}
        ports {:git! (fn [] (swap! events conj :git) git)
               :snapshot! (fn [] (swap! events conj :snapshot) {:root (java.net.URI. "file:/synthetic/")})
               :runtime! (fn [] (swap! events conj :runtime) (:runtime binding))
               :hash! (fn [path] (swap! events conj [:hash path]) (get files path))
               :read! (fn [path limit] (swap! events conj [:read path limit]) (utf8 (pr-str (model/machine-result request))))
               :write-new! (fn [path data]
                             (swap! events conj [:write path])
                             (when (contains? @writes path) (throw (ex-info "exists" {:reason :exists})))
                             (swap! writes assoc path (String. ^bytes data "UTF-8")))}]
    {:freeze freeze :ports ports :bound {:request request :binding binding} :events events :writes writes}))

(defn construct [s] (hooks/make-hooks (:freeze s) (:ports s)))

(defn bookend [s]
  (with-redefs [first/prepare (fn [manifest observation snapshot id outputs]
                               (swap! (:events s) conj [:prepare manifest observation snapshot id outputs])
                               (get-in s [:freeze :request]))]
    ((:bookend! (construct s)) (:bound s))))

(deftest source-observations-bind-the-whole-request
  (let [s (setup) h (construct s)]
    (is (empty? @(:events s)))
    (is (= (controller/expected-bookend (get-in s [:freeze :request]) (get-in s [:freeze :binding])) (bookend s)))
    (is (= 2 (count (filter #{:git} @(:events s)))))
    (let [[_ manifest observation snapshot id outputs] (first (filter #(and (vector? %) (= :prepare (first %))) @(:events s)))]
      (is (= {:synthetic true} manifest))
      (is (= (get-in s [:freeze :request :checkout]) (:before observation) (:after observation) (:source-revision observation)))
      (is (= (:root snapshot) (:root observation)))
      (is (= (get-in s [:freeze :request :operation-id]) id))
      (is (= (get-in s [:freeze :request :outputs]) outputs)))
    (is (= #{:bookend! :capture! :publish!} (set (keys h)))))
  (doseq [[reason change]
          [[:consumed-file-drift #(assoc-in % [:ports :hash!] (constantly "changed"))]
           [:dirty-source #(assoc-in % [:ports :git!] (constantly (assoc fixture/git-id :tracked-status " M input")))]
           [:git-identity #(assoc-in % [:ports :git!] (constantly {:head "foreign" :tree "foreign" :tracked-status ""}))]
           [:runtime-drift #(assoc-in % [:ports :runtime!] (constantly {}))]]]
    (let [s (change (setup))]
      (is (= reason (fixture/reason #(bookend s))))
      (is (empty? @(:writes s)))))
  (let [s (setup) n (atom 0) original (get-in s [:ports :git!])
        s (assoc-in s [:ports :git!] #(if (= 1 (swap! n inc)) (original) {:changed true}))]
    (is (= :git-bookends (fixture/reason #(bookend s)))))
  (let [s (setup)]
    (with-redefs [first/prepare (fn [& _] (assoc (get-in s [:freeze :request]) :operation-id "foreign"))]
      (is (= :source-request-drift (fixture/reason #((:bookend! (construct s)) (:bound s))))))))

(deftest capture-is-the-normal-exact-machine-result
  (let [s (setup) result ((:capture! (construct s)) (:bound s) {})]
    (is (= (model/machine-result (get-in s [:freeze :request])) result))
    (is (empty? @(:writes s))))
  (doseq [data ["{} {}" "{} :gravity.pmk.hooks/eof" "#foreign/tag {}" "[]"]]
    (is (some? (try (hooks/decode-result (utf8 data)) nil (catch Exception e e)))))
  (is (thrown? java.nio.charset.CharacterCodingException
               (hooks/decode-result (byte-array [(unchecked-byte 255)]))))
  (let [s (setup) wrong (assoc-in (model/machine-result (get-in s [:freeze :request])) [:selection :test-vars] [])
        s (assoc-in s [:ports :read!] (fn [& _] (utf8 (pr-str wrong))))]
    (is (= :result-selection (fixture/reason #((:capture! (construct s)) (:bound s) {})))))
  (let [s (setup) h (construct s)]
    (is (= :bound-request (fixture/reason #((:capture! h) (assoc-in (:bound s) [:request :operation-id] "foreign") {}))))
    (is (empty? @(:events s))))
  (let [s (setup) s (assoc-in s [:ports :read!] (fn [& _] (byte-array 1048577)))]
    (is (= :capture-size (fixture/reason #((:capture! (construct s)) (:bound s) {}))))))

(defn terminal [s status]
  {:schema :pmk-first-controller-result-v1 :operation-id (get-in s [:freeze :request :operation-id])
   :request-sha256 (first/digest (get-in s [:freeze :request])) :status status
   :process {:stdout "observed stdout" :stderr "observed stderr"}})

(deftest create-new-bounded-publication-preserves-failure
  (doseq [status [:passed :failed]]
    (let [s (setup) h (construct s) receipt (terminal s status) paths (get-in s [:freeze :request :outputs])]
      ((:publish! h) receipt)
      (is (= 3 (count @(:writes s))))
      (is (= "observed stdout" (get @(:writes s) (:stdout paths))))
      (is (= status (:status (edn/read-string (get @(:writes s) (:terminal paths))))))
      (is (= [:stdout :stderr :terminal]
             (mapv (fn [[_ path]] (first (for [[k v] paths :when (= path v)] k))) @(:events s))))
      (is (= :exists (fixture/reason #((:publish! h) receipt))))
      (is (= 3 (count @(:writes s))))))
  (let [s (setup) h (construct s)]
    (is (= :terminal-identity (fixture/reason #((:publish! h) (assoc (terminal s :passed) :operation-id "foreign")))))
    (is (empty? @(:writes s))))
  (let [s (setup) h (construct s) paths (get-in s [:freeze :request :outputs])]
    (is (= :capture-unavailable (fixture/reason #((:publish! h) (dissoc (terminal s :passed) :process)))))
    (is (empty? @(:writes s)))
    ((:publish! h) (dissoc (terminal s :failed) :process))
    (is (= :unavailable (:capture-state (edn/read-string (get @(:writes s) (:terminal paths)))))))
  (doseq [limit [1 10]]
    (let [s (assoc-in (setup) [:freeze :artifact-limit-bytes] limit)]
      (is (= :capture-size (fixture/reason #((:publish! (construct s)) (terminal s :failed)))))
      (is (empty? @(:writes s)))))
  (let [s (setup) s (assoc-in s [:ports :write-new!] (fn [& _] (throw (ex-info "native write" {:reason :native-write}))))]
    (is (= :native-write (fixture/reason #((:publish! (construct s)) (terminal s :failed)))))
    (is (= 0 (count @(:writes s))))))

(deftest hooks-compose-with-the-existing-C-L-controller
  (doseq [drift? [false true]]
    (let [m (model/model) s (setup) hashes (atom 0)
          s (if drift? (assoc-in s [:ports :hash!] (fn [_] (if (= 1 (swap! hashes inc)) fixture/h "changed"))) s)
          m (-> m (update :ports merge (construct s))
                (assoc-in [:ports :supervise!] (fn [& _] (assoc model/process-pass :stdout "" :stderr ""))))
          calls (atom 0)
          result (with-redefs [first/prepare (fn [& _] (swap! calls inc) (:request m))]
                   (model/run-model m))
          terminal-path (get-in m [:request :outputs :terminal])
          saved (edn/read-string (get @(:writes s) terminal-path))]
      (is (= (if drift? :failed :passed) (:status result) (:status saved)))
      (is (= [:L :C] (mapv :guard (:releases result))))
      (is (= (if drift? 1 2) @calls))
      (is (= 3 (count @(:writes s))))
      (is (= (if drift? :consumed-file-drift nil) (get-in result [:error :reason])))))
  (let [s (setup) freeze (assoc-in (:freeze s) [:binding :runtime :clojure :path] "/synthetic/clojure")
        freeze (update freeze :files assoc "/usr/bin/git" fixture/h "/synthetic/clojure" fixture/h)]
    (is (= hooks/io-keys (set (keys (hooks/normal-io freeze)))))
    (is (empty? @(:events s)))
    (doseq [n '[gravity.bootstrap gravity.self-hosting.sh01-host-resource-broker gravity.self-hosting.sh01-parallel-test-runner]]
      (is (nil? (find-ns n))))))

(deftest git-observation-does-not-request-branch-tracking
  (let [s (setup) freeze (update (:freeze s) :files assoc "/usr/bin/git" fixture/h "/synthetic/clojure" fixture/h)
        freeze (assoc-in freeze [:binding :runtime :clojure :path] "/synthetic/clojure")
        calls (atom []) io (hooks/normal-io freeze)]
    (with-redefs [shell/sh (fn [& args] (swap! calls conj (vec args)) {:exit 0 :out "observed\n"})]
      (is (= {:head "observed" :tree "observed" :tracked-status "observed"} ((:git! io)))))
    (is (= ["status" "--porcelain" "--untracked-files=no" "--no-ahead-behind"]
           (subvec (last @calls) 4 8)))
    (is (= 3 (count @calls)))))
