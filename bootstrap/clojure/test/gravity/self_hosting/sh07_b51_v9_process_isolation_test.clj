(ns gravity.self-hosting.sh07-b51-v9-process-isolation-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-attempt32-checker-child :as child]
            [gravity.tools.sh07-b51-attempt32-coordinator-child :as coordinator]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io PushbackReader]
           [java.nio.file Paths]))

(def git-a "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def git-b "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
(def sha-a
  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

(def ^:private strict-read-wire (:strict-read-wire codec/transport-api))

(defn- fake-operations
  ([calls] (fake-operations calls nil))
  ([calls fail-at]
   {:m-process-handle-id (codec/H [:m-process])
    :build-child
    (fn [role]
      (let [sequence (if (= :producer-census role) :a :b)]
        (swap! calls conj (keyword (str "build-" (name sequence))))
        {:role role :sequence sequence
         :argv-id (codec/H [:argv sequence])
         :request-id (codec/H [:request sequence])}))
    :start-child
    (fn [built]
      (let [sequence (:sequence built)]
        (swap! calls conj (keyword (str "start-" (name sequence))))
        (assoc built :process-start
               {:process-handle-id (codec/H [:process sequence])})))
    :join-child
    (fn [started]
      (let [sequence (:sequence started)]
        (swap! calls conj (keyword (str "join-" (name sequence))))
        (assoc started :exit-code
               (if (= fail-at (keyword (str (name sequence) "-exit")))
                 2 0))))
    :validate-child
    (fn [joined]
      (let [sequence (:sequence joined)
            marker (keyword (str "validate-" (name sequence)))]
        (swap! calls conj marker)
        (when (= fail-at marker)
          (throw (ex-info "synthetic validation rejection" {:marker marker})))
        (assoc joined :response-preimage-id
               (codec/H [:response sequence]))))
    :m-session
    (fn []
      (swap! calls conj :m-session)
      (when (= fail-at :m-session)
        (throw (ex-info "synthetic M rejection" {})))
      {:source-session-id (codec/H [:source-session :m])})}))

(deftest coordinator-enforces-the-exact-eleven-state-topology
  (let [calls (atom [])
        result (coordinator/coordinate! (fake-operations calls) codec/H)
        events (:events result)]
    (is (= [:build-a :start-a :join-a :validate-a
            :build-b :start-b :join-b :validate-b :m-session]
           @calls))
    (is (= (vec (range 11)) (mapv :ordinal events)))
    (is (= coordinator/coordinator-event-plan
           (mapv (juxt :event :role) events)))
    (is (nil? (:prior-event-root (first events))))
    (is (= (mapv :event-id (butlast events))
           (mapv :prior-event-root (rest events))))
    (is (= (get-in events [9 :source-session-id])
           (get-in events [10 :source-session-id])))
    (is (= (codec/H
            [:gravity/sh07-b51-coordinator-event-root-v1 events])
           (:event-root result)))))

(deftest coordinator-fails-before-constructing-a-later-participant
  (testing "A exit failure prevents response validation, B construction, and M"
    (let [calls (atom [])]
      (is (thrown? clojure.lang.ExceptionInfo
                   (coordinator/coordinate!
                    (fake-operations calls :a-exit) codec/H)))
      (is (= [:build-a :start-a :join-a] @calls))))
  (testing "A validation failure prevents even materializing B argv"
    (let [calls (atom [])]
      (is (thrown? clojure.lang.ExceptionInfo
                   (coordinator/coordinate!
                    (fake-operations calls :validate-a) codec/H)))
      (is (= [:build-a :start-a :join-a :validate-a] @calls))))
  (testing "B validation failure prevents the M source session"
    (let [calls (atom [])]
      (is (thrown? clojure.lang.ExceptionInfo
                   (coordinator/coordinate!
                    (fake-operations calls :validate-b) codec/H)))
      (is (= [:build-a :start-a :join-a :validate-a
              :build-b :start-b :join-b :validate-b]
             @calls)))))

(defn- coordinator-arguments [staging-root]
  ["--snapshot-commit" git-a
   "--snapshot-tree" git-b
   "--completed-at" "1970-01-01T00:00:00Z"
   "--evidence-relative-root"
   "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9"
   "--staging-root" staging-root
   "--eligibility-review-commit" git-b
   "--eligibility-review-tree" git-a
   "--eligibility-receipt-path"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn"
   "--eligibility-receipt-blob" git-b
   "--eligibility-receipt-byte-count" "1"
   "--eligibility-receipt-raw-sha256" sha-a
   "--eligibility-receipt-id" sha-a])

(deftest coordinator-child-builds-only-closed-nonsemantic-requests
  (let [cwd (System/getProperty "user.dir")
        separator java.io.File/pathSeparator
        cp (str/join separator ["/a" "/b" "/c" "/d"])
        options
        (assoc (coordinator/validate-entry-preflight!
                {:arguments (coordinator-arguments "/private/tmp/sh07-stage")
                 :environment coordinator/closed-environment
                 :working-directory cwd
                 :classpath cp})
               :architecture-report-sha256 sha-a)
        request (coordinator/checker-request options :producer-census)
        built (coordinator/build-child options (coordinator/load-codec!)
                                       :producer-census)
        environment (into {} (.environment ^ProcessBuilder (:builder built)))]
    (is (= #{:schema :role :content :eligibility :evidence :limits}
           (set (keys request))))
    (is (empty? (select-keys request
                            [:m0s :m1 :m2s :bundle-core :source-forms
                             :module-transactions :source-read-receipt])))
    (is (= coordinator/closed-environment environment))
    (is (not (contains? environment "HOME")))
    (is (not (contains? environment "PATH")))
    (is (= ["--role" "producer-census"]
           (subvec (:argv built) (- (count (:argv built)) 2))))
    (is (= 39 (count (:m-argv options))))
    (is (= "gravity.tools.sh07-b51-attempt32-coordinator-child"
           (nth (:m-argv options) 14)))
    (is (= request
           (strict-read-wire :request (:request-bytes built))))))

(deftest preflight-rejects-environment-classpath-and-option-drift
  (let [cwd (System/getProperty "user.dir")
        cp (str/join java.io.File/pathSeparator
                     ["/a" "/b" "/c" "/d"])
        base {:arguments (coordinator-arguments "/private/tmp/sh07-stage")
              :environment coordinator/closed-environment
              :working-directory cwd :classpath cp}]
    (is (thrown? clojure.lang.ExceptionInfo
                 (coordinator/validate-entry-preflight!
                  (update base :environment assoc "PATH" "/bin"))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (coordinator/validate-entry-preflight!
                  (assoc base :classpath "/a:/b:/c"))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (coordinator/validate-entry-preflight!
                  (update base :arguments #(vec (reverse %))))))))

(defn- first-form [resource]
  (with-open [reader (PushbackReader. (io/reader (io/resource resource)))]
    (binding [*read-eval* false]
      (read reader))))

(defn- required-namespaces [ns-form]
  (->> (drop 2 ns-form)
       (filter #(and (seq? %) (= :require (first %))))
       (mapcat rest)
       (map #(if (vector? %) (first %) %))
       vec))

(deftest both-entrypoints-have-project-free-static-require-closures
  (is (= ['clojure.edn 'clojure.string]
         (required-namespaces
          (first-form
           "gravity/tools/sh07_b51_attempt32_checker_child.clj"))))
  (is (= ['clojure.edn 'clojure.string]
         (required-namespaces
          (first-form
           "gravity/tools/sh07_b51_attempt32_coordinator_child.clj"))))
  (let [source
        (slurp (io/resource
                "gravity/tools/sh07_b51_attempt32_coordinator_child.clj"))]
    (is (not (str/includes?
              source
              "gravity.tools.sh07-b51-attempt30-producer-census")))
    (is (not (str/includes?
              source
              "gravity.tools.sh07-b51-attempt30-independent-census")))))

(deftest checker-entry-preflight-derives-the-exact-seventeen-token-argv
  (let [cwd (System/getProperty "user.dir")
        cp (str/join java.io.File/pathSeparator ["/a" "/b" "/c" "/d"])
        result
        (child/validate-entry-preflight!
         {:arguments ["--role" "producer-census"]
          :environment child/closed-environment
          :working-directory cwd :classpath cp})]
    (is (= 17 (count (:child-argv result))))
    (is (= "gravity.tools.sh07-b51-attempt32-checker-child"
           (nth (:child-argv result) 14)))
    (is (= ["--role" "producer-census"]
           (subvec (:child-argv result) 15)))))
