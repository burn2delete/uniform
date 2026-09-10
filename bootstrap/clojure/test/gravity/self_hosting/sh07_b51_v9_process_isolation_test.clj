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

(defn- coordinator-private-var [name]
  (ns-resolve 'gravity.tools.sh07-b51-attempt32-coordinator-child name))

(defn- eligibility-coordinate []
  {:schema :gravity/sh07-b51-external-review-object-v1
   :commit git-b :tree git-a :parents [git-a]
   :path
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn"
   :mode "100644" :type "blob" :blob-oid git-b :byte-count 1
   :raw-sha256 sha-a :receipt-id sha-a})

(defn- eligibility-result []
  (let [coordinate (eligibility-coordinate)
        observations (mapv (fn [ordinal] {:ordinal ordinal}) (range 5))
        pre-observations (mapv (fn [ordinal] {:ordinal ordinal}) (range 84))
        check
        {:coordinate coordinate
         :observations observations
         :pre-discharge-observations pre-observations
         :decode-count 1
         :receipt-id-check-count 1
         :pre-discharge-reconstruction-count 1
         :pre-discharge-git-operation-count 84
         :pre-discharge-document-descriptor-count 33
         :pre-discharge-source-descriptor-count 13
         :pre-discharge-pre-masked-file-count 2
         :pre-discharge-owned-c-artifact-count 33
         :pre-discharge-a9-artifact-count 3
         :pre-discharge-baseline-boot5-count 1
         :pre-discharge-static-decode-count 69
         :content-match true
         :source-descriptors-match true
         :source-descriptors-root-match true
         :pre-masked-root-match true
         :owned-c-root-match true
         :pre-discharge-match true}
        receipt
        {:artifact :gravity/sh07-b51-evidence-eligibility-receipt-v2
         :schema-version 2
         :content {:commit git-a :tree git-b}
         :freeze {:active-record-ref
                  {:transition {:architecture-report-sha256 sha-a}}}
         :source-descriptors []
         :source-descriptors-root sha-a
         :pre-masked-root sha-a
         :pre-evidence-contract-discharge {}
         :review {}
         :state :evidence-eligible
         :self-id-omits [[:receipt-id]]
         :nonclaims []
         :receipt-id sha-a}]
    {:coordinate coordinate
     :receipt receipt
     :observations observations
     :operation-count 5
     :pre-object-snapshot {}
     :pre-discharge-observations pre-observations
     :pre-discharge-git-operation-count 84
     :scope :immutable-q-coordinate-and-pre-discharge
     :eligibility-check check
     :eligibility-check-root
     (codec/H [:gravity/sh07-b51-eligibility-check-root-v1 check])
     :remaining-gaps []
     :authority :none}))

(defn- participant-result [participant check]
  {:participant participant
   :eligibility-check check
   :eligibility-check-root
   (codec/H [:gravity/sh07-b51-eligibility-check-root-v1 check])})

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

(deftest coordinator-mirrors-the-complete-eligibility-result
  (let [result (eligibility-result)
        options {:working-directory "/immutable/repository"
                 :content {:commit git-a :tree git-b}
                 :eligibility (eligibility-coordinate)}
        authenticate!
        (var-get (coordinator-private-var 'authenticate-eligibility-input!))]
    (with-redefs-fn
      {(coordinator-private-var 'load-git-evidence!)
       (fn [] {:validate-eligibility-object! (fn [_] result)})}
      #(let [authenticated (authenticate! options {:H codec/H})]
         (is (= (:eligibility-check result)
                (:eligibility-preflight authenticated)))
         (is (= (:eligibility-check-root result)
                (:eligibility-check-root authenticated)))
         (is (= sha-a (:architecture-report-sha256 authenticated)))))
    (is (= [:l :m :a :b] coordinator/eligibility-participant-order))
    (is (= {:participants [:l :m :a :b]
            :q-coordinate-git-operation-count 20
            :pre-discharge-git-operation-count 336
            :git-operation-count 356
            :q-coordinate-decode-count 4
            :receipt-id-check-count 4
            :pre-discharge-reconstruction-count 4
            :pre-discharge-static-decode-count 276
            :pre-discharge-document-descriptor-count 132
            :pre-discharge-source-descriptor-count 52
            :pre-discharge-pre-masked-file-count 8
            :pre-discharge-owned-c-artifact-count 132
            :pre-discharge-a9-artifact-count 12
            :pre-discharge-baseline-boot5-count 4
            :content-match-count 4
            :source-descriptors-match-count 4
            :source-descriptors-root-match-count 4
            :pre-masked-root-match-count 4
            :owned-c-root-match-count 4
            :pre-discharge-match-count 4}
           (coordinator/aggregate-eligibility-counters!
            codec/H
            (mapv #(participant-result % (:eligibility-check result))
                  [:l :m :a :b]))))))

(deftest coordinator-rejects-a-partial-pre-discharge-result
  (let [partial
        (assoc (eligibility-result) :remaining-gaps
               [:pre-discharge-84-operation-reconstruction])
        authenticate!
        (var-get (coordinator-private-var 'authenticate-eligibility-input!))]
    (with-redefs-fn
      {(coordinator-private-var 'load-git-evidence!)
       (fn [] {:validate-eligibility-object! (fn [_] partial)})}
      #(is (thrown? clojure.lang.ExceptionInfo
                    (authenticate!
                     {:working-directory "/immutable/repository"
                      :content {:commit git-a :tree git-b}
                     :eligibility (eligibility-coordinate)}
                     {:H codec/H}))))))

(deftest eligibility-aggregation-rejects-order-coordinate-observation-and-counter-drift
  (let [result (eligibility-result)
        participants
        (mapv #(participant-result % (:eligibility-check result))
              coordinator/eligibility-participant-order)
        mutate-check
        (fn [results index update-check]
          (let [check (update-check (get-in results
                                           [index :eligibility-check]))]
            (assoc results index
                   (participant-result
                    (get-in results [index :participant]) check))))
        coordinate-drift
        (mutate-check participants 1
                      #(assoc-in % [:coordinate :blob-oid] git-a))
        q-observation-drift
        (mutate-check participants 2
                      #(assoc % :observations
                              (vec (reverse (:observations %)))))
        pre-observation-drift
        (mutate-check participants 3
                      #(update % :pre-discharge-observations pop))
        old-counter
        (mutate-check
         participants 2
         #(-> %
              (assoc :pre-discharge-masked-file-count 2)
              (dissoc :pre-discharge-pre-masked-file-count)))]
    (doseq [invalid [(assoc-in participants [1 :participant] :a)
                     (assoc-in participants [3 :eligibility-check-root]
                               sha-a)
                     coordinate-drift
                     q-observation-drift
                     pre-observation-drift
                     old-counter]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (coordinator/aggregate-eligibility-counters!
                    codec/H invalid))))))

(deftest child-eligibility-check-is-bound-to-request-and-authenticated-coordinate
  (let [check (:eligibility-check (eligibility-result))
        request {:eligibility (eligibility-coordinate)}
        joined {:role :producer-census
                :request request
                :request-id
                (codec/H [:gravity/sh07-b51-checker-request-id-v1 request])}
        validate!
        (var-get (coordinator-private-var 'validate-child-eligibility!))]
    (is (= check (validate! joined {:H codec/H}
                            {:eligibility-check check})))
    (doseq [[invalid-joined invalid-check]
            [[(assoc joined :request-id sha-a) check]
             [joined (assoc-in check [:coordinate :blob-oid] git-a)]
             [joined (assoc check :observations
                            (vec (reverse (:observations check))))]
             [joined (update check :pre-discharge-observations pop)]
             [joined (assoc check :pre-discharge-match false)]]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (validate! invalid-joined {:H codec/H}
                              {:eligibility-check invalid-check}))))))

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
