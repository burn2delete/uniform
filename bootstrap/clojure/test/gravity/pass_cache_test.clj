(ns gravity.pass-cache-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pass-cache :as cache])
  (:import [java.nio.file Files LinkOption Path Paths StandardOpenOption]
           [java.util.concurrent TimeUnit]
           [java.nio.file.attribute PosixFilePermissions]))

(defn- sha [character]
  (str "sha256:" (apply str (repeat 64 character))))

(def ids
  {:producer (sha (char 49)) :input (sha (char 50))
   :artifact (sha (char 51)) :compiler (sha (char 52))
   :capability-policy (sha (char 53)) :facets (sha (char 54))
   :providers (sha (char 55)) :package-lock (sha (char 56))
   :diagnostics-schema (sha (char 57)) :dependency-graph (sha (char 97))
   :build-effects (sha (char 98)) :profile (sha (char 99))
   :target (sha (char 100)) :policy (sha (char 101))
   :provenance (sha (char 102)) :diagnostics (sha (char 48))
   :verifier (sha (char 97)) :evidence (sha (char 98))})

(defn- contract []
  {:pass :generic-pass :version "1" :order 10
   :input :gravity/test-ir :output :gravity/test-ir
   :requires #{:source} :preserves #{:source}
   :invalidates #{} :regenerates #{:output} :replacement-evidence {}
   :emits #{:gravity/test-output} :effects #{} :capabilities #{}
   :profiles #{:safe} :required-evidence #{:verification-trace}
   :verifier-required? true :authority-ceiling :reviewed})

(defn- request []
  (let [c (contract)]
    {:stage (:pass c) :contract c :producer-binding-id (:producer ids)
     :input-artifact-ids [(:input ids)]
     :external-root-inputs {(:input ids) {:kind (:input c) :facts #{:source}}}
     :input-facts #{:source}
     :semantic-bindings {:compiler-id (:compiler ids)
                         :capability-policy-id (:capability-policy ids)
                         :facet-set-id (:facets ids)
                         :provider-manifest-id (:providers ids)
                         :package-lock-id (:package-lock ids)
                         :diagnostic-schema-id (:diagnostics-schema ids)}
     :dependency-graph-id (:dependency-graph ids)
     :build-effect-replay-id (:build-effects ids)
     :profile-id (:profile ids) :target-id (:target ids)
     :policy-ids [(:policy ids)]
     :provenance {:provenance-id (:provenance ids)
                  :source-path nil :metadata {}}
     :diagnostic-stream-id (:diagnostics ids) :execution-mode :executed
     :authority {:input-authorities {(:input ids) :reviewed}
                 :claimed-level :reviewed :scope :test}}))

(defn- operations [calls]
  {:produce! (fn [_]
               (swap! calls update :produce (fnil inc 0))
               {:kind :generic-artifact :artifact-id (:artifact ids)
                :payload [:fresh]})
   :validate-output! (fn [artifact _ _]
                       (swap! calls update :validate (fnil inc 0))
                       artifact)
   :artifact-id-of (fn [artifact] (:artifact-id artifact))
   :validation-binding-id (sha (char 99))
   :verifier-reports (fn [_ request _]
                       [{:verifier-id (:verifier ids) :stage (:stage request)
                         :artifact-id (:artifact ids) :status :passed}])
   :evidence-records (fn [_ _ _]
                       [{:evidence-id (:evidence ids)
                         :kind :verification-trace :status :accepted
                         :artifact-id (:artifact ids)
                         :authority-level :reviewed}])
   :validate-diagnostic-stream! (fn [_ _] (swap! calls update :diagnostic (fnil inc 0)))
   :validate-verifier-report! (fn [_ _] (swap! calls update :verifier (fnil inc 0)))
   :validate-evidence-record! (fn [_ _] (swap! calls update :evidence (fnil inc 0)))})

(defn- delete-tree! [^Path root]
  (when (Files/exists root (make-array LinkOption 0))
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (.toArray paths)))]
        (Files/deleteIfExists ^Path path)))))

(defn- private-write! [^Path path ^String text]
  (Files/write path (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
               (into-array java.nio.file.OpenOption
                           [StandardOpenOption/CREATE
                            StandardOpenOption/TRUNCATE_EXISTING
                            StandardOpenOption/WRITE]))
  (Files/setPosixFilePermissions path
                                 (PosixFilePermissions/fromString "rw-------"))
  path)

(defn- make-fifo! [^Path path]
  (let [process (.start (ProcessBuilder. ["mkfifo" (str path)]))]
    (when-not (zero? (.waitFor process))
      (throw (ex-info "mkfifo unavailable" {:path (str path)})))
    path))

(def ^:private maximum-child-log-bytes (* 64 1024))

(defn- bounded-child-log-text
  [^Path path]
  (let [declared-size (Files/size path)
        limit (int (min declared-size maximum-child-log-bytes))
        bytes (byte-array limit)
        observed
        (with-open [^java.io.InputStream stream
                    (Files/newInputStream
                     path (make-array java.nio.file.OpenOption 0))]
          (loop [offset 0]
            (if (= offset limit)
              offset
              (let [count (.read stream bytes (int offset)
                                 (int (- limit offset)))]
                (if (<= count 0)
                  offset
                  (recur (+ offset count)))))))]
    (str (String. bytes (int 0) (int observed)
                  java.nio.charset.StandardCharsets/UTF_8)
         (when (> declared-size observed) "\n...[child log truncated]"))))

(defn- run-independent-cache-child!
  [^Path directory ^Path marker target-id barrier? hang? wait?]
  (let [clojure-command (or (System/getenv "GRAVITY_CLOJURE") "clojure")
        source-root (.toAbsolutePath (Paths/get "bootstrap/clojure/src"
                                                (make-array String 0)))
        test-root (.toAbsolutePath (Paths/get "bootstrap/clojure/test"
                                              (make-array String 0)))
        ready-marker (.resolve directory "publication.ready")
        expression
        (str "(require 'gravity.pass-cache 'gravity.pass-cache-test)"
             "(try (let [request (var-get (ns-resolve 'gravity.pass-cache-test 'request))"
             "      operations (var-get (ns-resolve 'gravity.pass-cache-test 'operations))"
             "      req "
             (if target-id
               (str "(assoc (request) :target-id " (pr-str target-id) ")")
               "(request)")
             "      key (gravity.pass-cache/stage-cache-key req)"
             "      calls (atom {}) base (operations calls)"
             "      produce (:produce! base)"
             "      ops (assoc base :produce! (fn [r]"
             "             (java.nio.file.Files/write"
             "              (java.nio.file.Paths/get " (pr-str (str marker))
             "               (make-array String 0))"
             "              (.getBytes \"x\\n\" java.nio.charset.StandardCharsets/UTF_8)"
             "              (into-array java.nio.file.OpenOption"
             "               [java.nio.file.StandardOpenOption/CREATE"
             "                java.nio.file.StandardOpenOption/APPEND]))"
             (if barrier?
               (str "             (loop [] (when (< (count (java.nio.file.Files/readAllLines"
                    " (java.nio.file.Paths/get " (pr-str (str marker))
                    " (make-array String 0)))) 2) (Thread/sleep 10) (recur)))")
               "")
             "             (produce r)))"
             "      result "
             (if hang?
               (str "(with-redefs-fn {(ns-resolve 'gravity.pass-cache '*publication-hook*)"
                    " (fn [] (java.nio.file.Files/write"
                    " (java.nio.file.Paths/get " (pr-str (str ready-marker))
                    " (make-array String 0)) (.getBytes \"ready\""
                    " java.nio.charset.StandardCharsets/UTF_8)"
                    " (into-array java.nio.file.OpenOption"
                    " [java.nio.file.StandardOpenOption/CREATE_NEW"
                    " java.nio.file.StandardOpenOption/WRITE]))"
                    " (Thread/sleep 60000))} #(gravity.pass-cache/lookup-or-compute!")
               "(gravity.pass-cache/lookup-or-compute!")
             "              (gravity.pass-cache/open-local-store "
             (pr-str (str directory)) ") key req ops"
             (if hang? "))]" ")]" )
             "  (prn {:child-status (:status result) :calls @calls"
             "        :marker-exists? (java.nio.file.Files/exists"
             "         (java.nio.file.Paths/get " (pr-str (str marker))
             "          (make-array String 0))"
             "         (make-array java.nio.file.LinkOption 0))})"
             "  (when-not (#{:stored :hit} (:status result)) (System/exit 9)))"
             " (finally (shutdown-agents)))")
        child-log
        (Files/createTempFile
         directory "gravity-pass-cache-child-" ".log"
         (into-array
          java.nio.file.attribute.FileAttribute
          [(PosixFilePermissions/asFileAttribute
            (PosixFilePermissions/fromString "rw-------"))]))
        builder (doto (ProcessBuilder.
                       [clojure-command "-Sdeps"
                        (str "{:paths [" (pr-str (str source-root)) " "
                             (pr-str (str test-root)) "]}")
                        "-M" "-e" expression])
                  (.redirectErrorStream true)
                  (.redirectOutput (.toFile child-log)))
        process (try
                  (.start builder)
                  (catch java.io.IOException error
                    (throw (ex-info "required clojure subprocess unavailable"
                                    {:command clojure-command
                                     :log-path (str child-log)} error))))]
    (if-not wait?
      {:available? true :finished? false :exit nil :process process
       :log-path child-log :log-text nil}
      (let [finished? (try (.waitFor process 30 TimeUnit/SECONDS)
                           (catch InterruptedException _ false))]
        (when-not finished?
          (.destroyForcibly process)
          (when-not (.waitFor process 10 TimeUnit/SECONDS)
            (throw (ex-info "timed-out cache child survived forced termination"
                            {:log-path (str child-log)}))))
        {:available? true :finished? finished?
         :timed-out? (not finished?) :alive? (.isAlive process)
         :exit (when finished? (.exitValue process))
         :log-path child-log
         :log-text (bounded-child-log-text child-log)}))))

(defn- await-path!
  [^Path path timeout-millis]
  (let [deadline (+ (System/nanoTime) (* timeout-millis 1000000))]
    (loop []
      (cond
        (Files/exists path (make-array LinkOption 0)) true
        (< (System/nanoTime) deadline) (do (Thread/sleep 20) (recur))
        :else false))))

(deftest public-contract-is-exact-and-nonauthoritative
  (let [contract (cache/pass-cache-contract) publics (ns-publics 'gravity.pass-cache)]
    (is (= '#{pass-cache-contract stage-cache-key open-local-store lookup!
              store! lookup-or-compute!} (set (keys publics))))
    (is (= (set (keys (:public-api contract))) (set (keys publics))))
    (is (= ".cpcache/compiler-pass/v2" (:storage-root contract)))
    (is (some #{'clojure.set}
              (get-in contract [:dependency-direction :requires])))
    (is (some #{'gravity.bootstrap}
              (get-in contract [:dependency-direction :forbids])))
    (is (some #{'gravity.c2-pass-cache}
              (get-in contract [:dependency-direction :forbids])))
    (is (false? (get-in contract [:authority :authoritative?])))
    (is (false? (get-in contract [:authority :release-authority?])))
    (is (false? (get-in contract [:authority :proof-authority?])))
    (is (false? (get-in contract [:authority :equivalence-authority?])))
    (is (false? (get-in contract [:authority :self-hosting-authority?])))))

(deftest key-binds-contract-and-c16-invalidators
  (let [base (request) key (cache/stage-cache-key base)
        changing [:contract :stage :producer-binding-id :input-artifact-ids
                  :semantic-bindings :dependency-graph-id
                  :build-effect-replay-id :profile-id :target-id :policy-ids
                  :diagnostic-stream-id :provenance]]
    (doseq [field changing]
      (let [changed (case field
                      :contract (assoc-in base [:contract :version] "2")
                      :stage (assoc base :stage :other-pass
                                     :contract (assoc (contract)
                                                      :pass :other-pass))
                      :producer-binding-id (assoc base field (sha (char 57)))
                      :input-artifact-ids
                      (let [replacement (sha (char 56))]
                        (-> base
                            (assoc :input-artifact-ids [replacement]
                                   :external-root-inputs
                                   {replacement {:kind (:input (contract))
                                                 :facts #{:source}}})
                            (assoc-in [:authority :input-authorities]
                                      {replacement :reviewed})))
                      :semantic-bindings (assoc-in base [field :compiler-id] (sha (char 57)))
                      :dependency-graph-id (assoc base field (sha (char 57)))
                      :build-effect-replay-id (assoc base field (sha (char 57)))
                      :profile-id (assoc base field (sha (char 57)))
                      :target-id (assoc base field (sha (char 57)))
                      :policy-ids (assoc base field [(sha (char 57))])
                      :diagnostic-stream-id (assoc base field (sha (char 57)))
                      :provenance (assoc-in base [field :provenance-id] (sha (char 57))))]
        (is (not= (:semantic-key-id key)
                  (:semantic-key-id (cache/stage-cache-key changed)))
            (str "invalidator " field))))))

(deftest cold-warm-reuse-runs-producer-once-and-revalidates
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) request (request) key (cache/stage-cache-key request)
            store (cache/open-local-store directory)
            first-result (cache/lookup-or-compute! store key request (operations calls))
            second-result (cache/lookup-or-compute! store key request (operations calls))
            lowered-request (assoc request :authority
                                   (assoc (:authority request)
                                          :input-authorities {(:input ids) :none}
                                          :claimed-level :none))
            lowered-key (cache/stage-cache-key lowered-request)
            lowered-result (cache/lookup-or-compute!
                            store lowered-key lowered-request (operations calls))]
        (is (= :stored (:status first-result)))
        (is (= :hit (:status second-result)))
        (is (= 1 (:produce @calls)))
        (is (= 3 (:validate @calls)))
        (is (= :gravity/pass-cache-reuse-receipt
               (get-in second-result [:reuse-receipt :artifact])))
        (is (= :hit (:status lowered-result)))
        (is (= :none (get-in lowered-result
                             [:reuse-receipt :authority :effective-level])))
        (is (false? (get-in second-result [:reuse-receipt :claims :release?])))
        (is (false? (get-in second-result [:reuse-receipt :claims :proof?])))
        (is (false? (get-in second-result [:reuse-receipt :claims :equivalence?])))
        (is (= 3 (:diagnostic @calls)))
        (is (= 3 (:verifier @calls)))
        (is (= 3 (:evidence @calls))))
      (finally (delete-tree! directory)))))

(deftest v1-isolation-and-path-policy
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [v1 (.resolve ^Path directory ".cpcache/compiler-pass/v1")]
        (Files/createDirectories
         v1 (make-array java.nio.file.attribute.FileAttribute 0))
        (let [store (cache/open-local-store directory)]
          (is (= (.normalize (.resolve ^Path directory ".cpcache/compiler-pass/v2"))
                 (:root store)))
          (is (Files/exists v1 (make-array LinkOption 0)))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/open-local-store
                        (.resolve ^Path directory "../escape"))))))
      (finally (delete-tree! directory)))))

(deftest store-schema-and-retained-directory-identities-are-bound
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            req (request) key (cache/stage-cache-key req)
            ops (operations (atom {}))]
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup! (assoc store :extra true) key ops)))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup! (assoc store :entries (:blobs store))
                                    key ops)))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup!
                      (assoc-in store [:store-policy :maximum-entry-count] 1)
                      key ops)))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup!
                      (update store :directory-identities
                              #(vec (reverse %)))
                      key ops))))
      (finally (delete-tree! directory)))))

(deftest unknown-fields-and-authority-provenance-policy
  (let [base (request) key (cache/stage-cache-key base)]
    (is (thrown? clojure.lang.ExceptionInfo
                 (cache/stage-cache-key (assoc base :telemetry-id (sha (char 49))))))
    (is (= (:semantic-key-id key)
           (:semantic-key-id
            (cache/stage-cache-key
             (assoc base :provenance (assoc (:provenance base)
                                            :source-path "/different/path"
                                            :metadata {:wall-clock 99})
                        :authority (assoc (:authority base)
                                         :claimed-level :none))))))
    (is (= (:semantic-key-id key)
           (:semantic-key-id
            (cache/stage-cache-key
             (assoc base :authority
                    (assoc (:authority base)
                           :input-authorities {(:input ids) :none}
                           :claimed-level :none)))))
        "authority remains non-semantic while reuse authority is monotonic")
    (is (not= (:semantic-key-id key)
              (:semantic-key-id
               (cache/stage-cache-key
                (assoc base :authority
                       (assoc (:authority base) :scope :other-scope)))))
        "producer-observable authority scope is semantic")
    (is (thrown? clojure.lang.ExceptionInfo
                 (cache/stage-cache-key
                  (assoc base :authority
                         (assoc (:authority base) :scope " ")))))
    (is (not= (:semantic-key-id key)
              (:semantic-key-id
               (cache/stage-cache-key
                (assoc-in base [:provenance :provenance-id] (sha (char 49)))))))))

(deftest lookup-or-compute-requires-the-exact-key-producing-request
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-exact-request-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            reviewed-request (request)
            reviewed-key (cache/stage-cache-key reviewed-request)
            calls (atom {})
            ops (operations calls)
            none-request
            (assoc reviewed-request :authority
                   {:input-authorities {(:input ids) :none}
                    :claimed-level :none
                    :scope :test})
            provenance-request
            (assoc reviewed-request :provenance
                   (assoc (:provenance reviewed-request)
                          :source-path "/different/nonsemantic/source"
                          :metadata {:revision 2}))]
        (is (= (:semantic-key-id reviewed-key)
               (:semantic-key-id (cache/stage-cache-key none-request)))
            "authority levels remain absent from semantic identity")
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup-or-compute!
                      store reviewed-key none-request ops)))
        (is (= (:semantic-key-id reviewed-key)
               (:semantic-key-id (cache/stage-cache-key provenance-request)))
            "nonidentity provenance detail remains absent from semantic identity")
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup-or-compute!
                      store reviewed-key provenance-request ops)))
        (is (empty? @calls) "rejection precedes lookup validation and producer")
        (with-open [paths (Files/list (:entries store))]
          (is (empty? (vec (.toArray paths)))))
        (with-open [paths (Files/list (:receipts store))]
          (is (empty? (vec (.toArray paths)))
              "no producer authority receipt is published")))
      (finally (delete-tree! directory)))))

(deftest input-fact-root-and-each-semantic-binding-invalidate-key
  (let [base (request) key (cache/stage-cache-key base)
        bindings (keys (:semantic-bindings base))]
    (is (not= (:semantic-key-id key)
              (:semantic-key-id
               (cache/stage-cache-key
                (assoc base :input-facts #{:source :extra})))))
    (is (not= (:semantic-key-id key)
              (:semantic-key-id
               (cache/stage-cache-key
                (let [new-id (sha (char 48))]
                  (assoc base
                         :input-artifact-ids (vec (sort [(:input ids) new-id]))
                         :external-root-inputs
                         (assoc (:external-root-inputs base) new-id
                                {:kind (:input (contract)) :facts #{:source}})
                         :authority
                         (assoc-in (:authority base)
                                   [:input-authorities new-id] :reviewed)))))))
    (doseq [binding bindings]
      (let [changed (assoc-in base [:semantic-bindings binding]
                              (sha (char 48)))]
        (is (not= (:semantic-key-id key)
                  (:semantic-key-id (cache/stage-cache-key changed)))
            (str "binding invalidator " binding))))))

(deftest corrupt-entry-is-retained-and-fresh-result-is-withheld
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) request (request) key (cache/stage-cache-key request)
            store (cache/open-local-store directory)
            ops (operations calls)]
        (cache/lookup-or-compute! store key request ops)
        (let [blob-paths (with-open [paths (Files/list (:blobs store))]
                           (vec (.toArray paths)))
              receipt-paths (with-open [paths (Files/list (:receipts store))]
                              (vec (.toArray paths)))
              entry-path (first (filter #(.endsWith (str %) ".edn")
                                        (with-open [paths (Files/list (:entries store))]
                                          (vec (.toArray paths)))))]
          (Files/write ^Path entry-path (.getBytes "not-canonical")
                       (into-array java.nio.file.OpenOption
                                   [java.nio.file.StandardOpenOption/TRUNCATE_EXISTING
                                    java.nio.file.StandardOpenOption/WRITE]))
          (let [fresh (cache/lookup-or-compute! store key request ops)]
            (is (= :miss (:status fresh)))
            (is (= :withheld (get-in fresh [:cache-evidence :cache-publication])))
            (is (= 2 (:produce @calls)))
            (is (= "not-canonical"
                   (String. (Files/readAllBytes ^Path entry-path))))
            (cache/open-local-store directory)
            (is (every? #(Files/exists ^Path % (make-array LinkOption 0))
                        (concat blob-paths receipt-paths))
                "undecipherable retained entry preserves all CAS evidence"))))
      (finally (delete-tree! directory)))))

(deftest incomplete-publication-orphans-require-validated-recovery
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-orphan-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            req (request) key (cache/stage-cache-key req)
            hook-calls (atom 0)
            hook-var (ns-resolve 'gravity.pass-cache '*publication-hook*)]
        (is (thrown?
             clojure.lang.ExceptionInfo
             (with-redefs-fn
               {hook-var (fn []
                           (when (= 3 (swap! hook-calls inc))
                             (throw (ex-info "simulated pre-entry crash" {}))))}
               #(cache/lookup-or-compute!
                 store key req (operations (atom {}))))))
        (with-open [paths (Files/list (:blobs store))]
          (is (= 1 (count (vec (.toArray paths))))))
        (with-open [paths (Files/list (:receipts store))]
          (is (= 1 (count (vec (.toArray paths))))))
        (with-open [paths (Files/list (:entries store))]
          (is (empty? (vec (.toArray paths)))))
        (let [reopened (cache/open-local-store directory)]
          (with-open [paths (Files/list (:receipts reopened))]
            (is (= 1 (count (vec (.toArray paths))))))
          (cache/lookup! reopened key (operations (atom {}))))
        (with-open [paths (Files/list (:blobs store))]
          (is (empty? (vec (.toArray paths)))))
        (with-open [paths (Files/list (:receipts store))]
          (is (empty? (vec (.toArray paths))))))
      (finally (delete-tree! directory)))))

(deftest cross-scope-producer-receipt-cannot-be-stored
  (let [directory-a (Files/createTempDirectory
                     "gravity-pass-cache-scope-a-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
        directory-b (Files/createTempDirectory
                     "gravity-pass-cache-scope-b-"
                     (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [request-a (request)
            key-a (cache/stage-cache-key request-a)
            result-a (cache/lookup-or-compute!
                      (cache/open-local-store directory-a) key-a request-a
                      (operations (atom {})))
            request-b (assoc-in request-a [:authority :scope] :other-scope)
            key-b (cache/stage-cache-key request-b)
            store-b (cache/open-local-store directory-b)]
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/store! store-b key-b (:artifact result-a)
                                   (:producer-receipt result-a)
                                   (operations (atom {})))))
        (with-open [paths (Files/list (:entries store-b))]
          (is (empty? (vec (.toArray paths))))))
      (finally
        (delete-tree! directory-a)
        (delete-tree! directory-b)))))

(deftest canonical-entry-tampering-preserves-all-prior-cas
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-commit-integrity-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [req (request) key (cache/stage-cache-key req)
            ops (operations (atom {}))
            result (cache/lookup-or-compute!
                    (cache/open-local-store directory) key req
                    ops)
            store (cache/open-local-store directory)
            entry-path (.resolve ^Path (:entries store)
                                 (str (:cache-key-id key) ".edn"))
            original-bytes (Files/readAllBytes entry-path)
            cas-paths (concat
                       (with-open [paths (Files/list (:blobs store))]
                         (vec (.toArray paths)))
                       (with-open [paths (Files/list (:receipts store))]
                         (vec (.toArray paths))))
            encode (var-get (ns-resolve 'gravity.pass-cache 'encoded-value))
            entry-id-fn (var-get (ns-resolve 'gravity.pass-cache 'entry-id))
            maximum-entry-bytes
            (var-get (ns-resolve 'gravity.pass-cache 'maximum-entry-bytes))
            rewrite! (fn [entry]
                       (Files/write
                        entry-path (encode entry maximum-entry-bytes)
                        (into-array java.nio.file.OpenOption
                                    [StandardOpenOption/TRUNCATE_EXISTING
                                     StandardOpenOption/WRITE])))]
        ;; A stale identity after a canonical blob-reference change is not a
        ;; valid commit and therefore cannot authorize reachability deletion.
        (rewrite! (assoc (:cache-entry result) :blob-id (sha (char 48))))
        (cache/lookup! store key ops)
        (is (every? #(Files/exists ^Path % (make-array LinkOption 0)) cas-paths))
        ;; Recomputing the entry id is insufficient: referenced receipt/blob
        ;; existence and cross-record integrity are also mandatory.
        (let [base (assoc (dissoc (:cache-entry result) :entry-id)
                          :producer-receipt-id (sha (char 48)))]
          (rewrite! (assoc base :entry-id (entry-id-fn base))))
        (cache/lookup! store key ops)
        (is (every? #(Files/exists ^Path % (make-array LinkOption 0)) cas-paths))
        (Files/write entry-path original-bytes
                     (into-array java.nio.file.OpenOption
                                 [StandardOpenOption/TRUNCATE_EXISTING
                                  StandardOpenOption/WRITE]))
        (let [wrong-path (.resolve ^Path (:entries store)
                                   (str (sha (char 48)) ".edn"))]
          (Files/write wrong-path original-bytes
                       (into-array java.nio.file.OpenOption
                                   [StandardOpenOption/CREATE_NEW
                                    StandardOpenOption/WRITE]))
          (Files/setPosixFilePermissions
           wrong-path (PosixFilePermissions/fromString "rw-------"))
          (cache/lookup! store key ops)
          (is (every? #(Files/exists ^Path % (make-array LinkOption 0))
                      cas-paths))))
      (finally (delete-tree! directory)))))

(deftest recovery-rejects-semantically-mismatched-referenced-blob
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-recovery-artifact-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            ops (operations calls)
            store (cache/open-local-store directory)
            result (cache/lookup-or-compute! store key req ops)
            encode (var-get (ns-resolve 'gravity.pass-cache 'encoded-value))
            blob-id-fn (var-get (ns-resolve 'gravity.pass-cache 'blob-id))
            entry-id-fn (var-get (ns-resolve 'gravity.pass-cache 'entry-id))
            maximum-blob-bytes
            (var-get (ns-resolve 'gravity.pass-cache 'maximum-blob-bytes))
            maximum-entry-bytes
            (var-get (ns-resolve 'gravity.pass-cache 'maximum-entry-bytes))
            entry-path (.resolve ^Path (:entries store)
                                 (str (:cache-key-id key) ".edn"))
            good-blob-path (.resolve ^Path (:blobs store)
                                     (str (get-in result
                                                  [:cache-entry :blob-id]) ".edn"))
            receipt-path (.resolve ^Path (:receipts store)
                                   (str (get-in result
                                                [:cache-entry
                                                 :producer-receipt-id]) ".edn"))
            bad-artifact {:kind :generic-artifact
                          :artifact-id (sha (char 48))
                          :payload [:semantically-wrong]}
            bad-bytes (encode bad-artifact maximum-blob-bytes)
            bad-blob-id (blob-id-fn bad-bytes)
            bad-blob-path (.resolve ^Path (:blobs store)
                                    (str bad-blob-id ".edn"))
            orphan-artifact {:kind :generic-artifact
                             :artifact-id (:artifact ids)
                             :payload [:collectible-orphan]}
            orphan-bytes (encode orphan-artifact maximum-blob-bytes)
            orphan-path (.resolve ^Path (:blobs store)
                                  (str (blob-id-fn orphan-bytes) ".edn"))
            write-new!
            (fn [path bytes]
              (Files/write
               path bytes
               (into-array java.nio.file.OpenOption
                           [StandardOpenOption/CREATE_NEW
                            StandardOpenOption/WRITE]))
              (Files/setPosixFilePermissions
               path (PosixFilePermissions/fromString "rw-------")))
            tampered-base (assoc (dissoc (:cache-entry result) :entry-id)
                                 :blob-id bad-blob-id)
            tampered-entry (assoc tampered-base :entry-id
                                  (entry-id-fn tampered-base))]
        (write-new! bad-blob-path bad-bytes)
        (write-new! orphan-path orphan-bytes)
        (Files/write
         entry-path (encode tampered-entry maximum-entry-bytes)
         (into-array java.nio.file.OpenOption
                     [StandardOpenOption/TRUNCATE_EXISTING
                      StandardOpenOption/WRITE]))
        (let [validation-count (get @calls :validate 0)
              rejected (cache/lookup! store key ops)]
          (is (= [:rejected "C16-STALE"]
                 [(:status rejected)
                  (get-in rejected
                          [:cache-evidence :contained-diagnostic])]))
          (is (= (+ validation-count 2) (:validate @calls))
              "recovery and direct lookup both apply the current artifact validator")
          (is (every? #(Files/exists ^Path % (make-array LinkOption 0))
                      [good-blob-path bad-blob-path receipt-path orphan-path])
              "semantic blob mismatch retains referenced and orphan CAS evidence")))
      (finally (delete-tree! directory)))))

(deftest recovery-rejects-cross-scope-receipt-substitution
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-recovery-scope-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            ops (operations (atom {}))
            request-a (request) key-a (cache/stage-cache-key request-a)
            result-a (cache/lookup-or-compute! store key-a request-a ops)
            request-b (assoc-in request-a [:authority :scope] :other-scope)
            key-b (cache/stage-cache-key request-b)
            result-b (cache/lookup-or-compute! store key-b request-b ops)
            entry-a-path (.resolve ^Path (:entries store)
                                   (str (:cache-key-id key-a) ".edn"))
            entry-b-path (.resolve ^Path (:entries store)
                                   (str (:cache-key-id key-b) ".edn"))
            entry-id-fn (var-get (ns-resolve 'gravity.pass-cache 'entry-id))
            encode (var-get (ns-resolve 'gravity.pass-cache 'encoded-value))
            maximum-entry-bytes
            (var-get (ns-resolve 'gravity.pass-cache 'maximum-entry-bytes))
            hook-var (ns-resolve 'gravity.pass-cache '*publication-hook*)
            receipt-paths
            (fn []
              (with-open [paths (Files/list (:receipts store))]
                (set (vec (.toArray paths)))))
            create-receipt-orphan!
            (fn [orphan-request]
              (let [before (receipt-paths)
                    calls (atom 0)
                    orphan-key (cache/stage-cache-key orphan-request)]
                (is (thrown?
                     clojure.lang.ExceptionInfo
                     (with-redefs-fn
                       {hook-var (fn []
                                   (when (= 2 (swap! calls inc))
                                     (throw (ex-info "pre-entry crash" {}))))}
                       #(cache/lookup-or-compute!
                         store orphan-key orphan-request ops))))
                (first (remove before (receipt-paths)))))
            substituted-base
            (assoc (dissoc (:cache-entry result-a) :entry-id)
                   :producer-receipt-id
                   (get-in result-b [:producer-receipt :receipt-id]))
            substituted (assoc substituted-base :entry-id
                               (entry-id-fn substituted-base))]
        ;; First prove the exact-validator recovery branch is live: two valid
        ;; retained entries permit collection of a third receipt orphan.
        (let [baseline-orphan
              (create-receipt-orphan!
               (assoc request-a :target-id (sha (char 50))))]
          (is (Files/exists ^Path baseline-orphan (make-array LinkOption 0)))
          (cache/lookup! store key-a ops)
          (is (not (Files/exists ^Path baseline-orphan
                                 (make-array LinkOption 0)))))
        (Files/delete entry-b-path)
        (Files/write entry-a-path (encode substituted maximum-entry-bytes)
                     (into-array java.nio.file.OpenOption
                                 [StandardOpenOption/TRUNCATE_EXISTING
                                  StandardOpenOption/WRITE]))
        ;; The same branch now sees a valid but wrong-scope receipt.  A newly
        ;; collectible orphan must survive, proving recovery became incomplete.
        (let [tampered-orphan
              (create-receipt-orphan!
               (assoc request-a :target-id (sha (char 51))))]
          (is (Files/exists ^Path tampered-orphan (make-array LinkOption 0)))
          (cache/lookup! store key-a ops)
          (is (Files/exists ^Path tampered-orphan (make-array LinkOption 0))
              "cross-scope key reconstruction disables orphan deletion")))
      (finally (delete-tree! directory)))))

(deftest late-corrupt-orphan-prevents-the-entire-deletion-plan
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-late-corrupt-orphan-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            req (request) key (cache/stage-cache-key req)
            hook-calls (atom 0)
            hook-var (ns-resolve 'gravity.pass-cache '*publication-hook*)]
        (is (thrown?
             clojure.lang.ExceptionInfo
             (with-redefs-fn
               {hook-var (fn []
                           (when (= 3 (swap! hook-calls inc))
                             (throw (ex-info "simulated pre-entry crash" {}))))}
               #(cache/lookup-or-compute!
                 store key req (operations (atom {}))))))
        (let [corrupt (.resolve ^Path (:receipts store)
                                (str (sha (char 48)) ".edn"))]
          (private-write! corrupt "not-canonical")
          (let [all-candidates
                (concat
                 (with-open [paths (Files/list (:blobs store))]
                   (vec (.toArray paths)))
                 (with-open [paths (Files/list (:receipts store))]
                   (vec (.toArray paths))))]
            (is (= [:rejected "C16-ENTRY"]
                   (let [result (cache/lookup!
                                 store key (operations (atom {})))]
                     [(:status result)
                      (get-in result
                              [:cache-evidence :contained-diagnostic])])))
            (is (every? #(Files/exists ^Path % (make-array LinkOption 0))
                        all-candidates)
                "late invalid candidate prevents both blob and receipt deletion"))))
      (finally (delete-tree! directory)))))

(deftest canonical-accounting-and-recovery-traversal-are-bounded
  (is (thrown? clojure.lang.ExceptionInfo
               ((var-get (ns-resolve 'gravity.pass-cache 'canonical-bytes))
                (vec (repeat 12 (apply str (repeat 100 "x")))) 512))
      "aggregate scalar bytes reject before final encoding")
  (let [canonical-bytes
        (var-get (ns-resolve 'gravity.pass-cache 'canonical-bytes))]
    (is (thrown? clojure.lang.ExceptionInfo
                 (canonical-bytes (apply str (repeat 2 (char 0))) 75))
        "single scalar accounts printed control-character escapes")
    (is (thrown? clojure.lang.ExceptionInfo
                 (canonical-bytes
                  (vec (repeat 5 (str "x" (char 0)))) 425))
        "aggregate scalars account every printed escape")
    (is (thrown? clojure.lang.ExceptionInfo
                 (canonical-bytes
                  (String. (Character/toChars 0x1f600)) 69))
        "supplementary code points account four printed UTF-8 bytes")
    (doseq [named [(keyword "namespace-1234567890" "name-1234567890")
                   (symbol "namespace-1234567890" "name-1234567890")]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (canonical-bytes named 100))
          "namespaced keyword/symbol components are both charged"))
    (let [large-integer (java.math.BigInteger.
                         (apply str (repeat 200 "9")))]
      (is (thrown? clojure.lang.ExceptionInfo
                   (canonical-bytes large-integer 150))
          "integer bit-length bounds decimal printing before str")
      (is (thrown? clojure.lang.ExceptionInfo
                   (canonical-bytes
                    (clojure.lang.Ratio. large-integer
                                         (.subtract large-integer
                                                    java.math.BigInteger/ONE))
                    300))
          "ratio components are independently pre-accounted"))
    (is (thrown? clojure.lang.ExceptionInfo
                 (canonical-bytes
                  (java.math.BigDecimal. java.math.BigInteger/ONE 1000000)
                  1024))
        "BigDecimal precision/scale bounds printing before str")
    (is (thrown? clojure.lang.ExceptionInfo
                 (canonical-bytes [nil nil] 200))
        "container and child tagged-node syntax is conservatively charged"))
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-recovery-bound-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [store (cache/open-local-store directory)
            blob-id-fn (var-get (ns-resolve 'gravity.pass-cache 'blob-id))
            contents ["orphan-a" "orphan-b"]
            paths (mapv (fn [content]
                          (let [bytes (.getBytes content
                                                 java.nio.charset.StandardCharsets/UTF_8)
                                path (.resolve ^Path (:blobs store)
                                               (str (blob-id-fn bytes) ".edn"))]
                            (private-write! path content)))
                        contents)
            maximum-var (ns-resolve 'gravity.pass-cache 'maximum-blob-count)
            original-maximum (var-get maximum-var)]
        (try
          (alter-var-root maximum-var (constantly 1))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/open-local-store directory)))
          (is (every? #(Files/exists ^Path % (make-array LinkOption 0)) paths)
              "over-count recovery rejects before deleting any residue")
          (finally (alter-var-root maximum-var
                                   (constantly original-maximum)))))
      (finally (delete-tree! directory)))))

(deftest same-key-divergence-and-validator-requirements-fail-closed
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) request (request) key (cache/stage-cache-key request)
            store (cache/open-local-store directory) ops (operations calls)
            first-result (cache/lookup-or-compute! store key request ops)
            divergent (assoc (:artifact first-result) :payload [:different])]
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/store! store key divergent
                                   (:producer-receipt first-result) ops)))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup! store key (dissoc ops :validation-binding-id))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/lookup! store key (assoc ops :validate-evidence-record! nil))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/store! store key (:artifact first-result)
                                   (assoc (:producer-receipt first-result)
                                          :profile-id (sha (char 49))) ops))))
      (finally (delete-tree! directory)))))

(deftest decoded-entry-identities-cannot-become-paths
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-entry-id-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            store (cache/open-local-store directory)
            result (cache/lookup-or-compute! store key req (operations calls))
            entry-id-fn (var-get (ns-resolve 'gravity.pass-cache 'entry-id))
            validate-entry-fn
            (var-get (ns-resolve 'gravity.pass-cache 'validate-entry-record!))
            relative-name-fn
            (var-get (ns-resolve 'gravity.pass-cache 'relative-name!))]
        (doseq [field [:blob-id :producer-receipt-id]]
          (let [base (assoc (dissoc (:cache-entry result) :entry-id)
                            field "../escape")
                malicious (assoc base :entry-id (entry-id-fn base))]
            (is (thrown? clojure.lang.ExceptionInfo
                         (validate-entry-fn key malicious))
                (str "decoded " field " rejected before path use"))))
        (doseq [name ["../escape" "a/b" "." ".."]]
          (is (thrown? clojure.lang.ExceptionInfo (relative-name-fn name)))))
      (finally (delete-tree! directory)))))

(deftest cooperative-same-key-writers-converge
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) request (request) key (cache/stage-cache-key request)
            store-a (cache/open-local-store directory)
            store-b (cache/open-local-store directory)
            run (fn [store] (cache/lookup-or-compute! store key request
                                                       (operations calls)))
            first-result (future (run store-a))
            second-result (future (run store-b))]
        (is (#{:stored :hit} (:status @first-result)))
        (is (#{:stored :hit} (:status @second-result)))
        (is (= 1 (:produce @calls))))
      (finally (delete-tree! directory)))))

(deftest different-keys-share-one-content-addressed-blob
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) base (request)
            request-a base
            request-b (assoc base :target-id (sha (char 49)))
            key-a (cache/stage-cache-key request-a)
            key-b (cache/stage-cache-key request-b)
            store (cache/open-local-store directory)]
        (cache/lookup-or-compute! store key-a request-a (operations calls))
        (cache/lookup-or-compute! store key-b request-b (operations calls))
        (with-open [paths (Files/list (:blobs store))]
          (is (= 1 (count (vec (.toArray paths)))))))
      (finally (delete-tree! directory)))))

(deftest filesystem-boundaries-and-v1-bytes
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [v1 (.resolve ^Path directory ".cpcache/compiler-pass/v1")
            marker (.resolve v1 "legacy.edn")
            marker-bytes (.getBytes "legacy-v1" java.nio.charset.StandardCharsets/UTF_8)]
        (Files/createDirectories
         v1 (make-array java.nio.file.attribute.FileAttribute 0))
        (Files/write marker marker-bytes
                     (into-array java.nio.file.OpenOption
                                 [java.nio.file.StandardOpenOption/CREATE_NEW
                                  java.nio.file.StandardOpenOption/WRITE]))
        (let [store (cache/open-local-store directory)]
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/open-local-store (.resolve ^Path directory "../escape"))))
          (is (= "legacy-v1" (String. (Files/readAllBytes marker))))
          (Files/setPosixFilePermissions
           (:entries store) (PosixFilePermissions/fromString "rwxrwxrwx"))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/lookup! store (cache/stage-cache-key (request))
                                      (operations (atom {})))))))
      (finally (delete-tree! directory)))))

(deftest canonical-host-values-reject-metadata-and-forbidden-forms
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) request (request) key (cache/stage-cache-key request)
            store (cache/open-local-store directory)
            result (cache/lookup-or-compute! store key request (operations calls))]
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/store! store key
                                   (with-meta (:artifact result) {:host 1})
                                   (:producer-receipt result)
                                   (operations calls))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/stage-cache-key
                      (assoc request :diagnostic-stream-id "not-sha")))))
      (finally (delete-tree! directory)))))

(deftest exact-operation-and-authority-claims-are-required
  (let [base (request) key (cache/stage-cache-key base) calls (atom {})
        ops (operations calls)]
    (let [directory (Files/createTempDirectory
                     "gravity-pass-cache-test-"
                     (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (let [store (cache/open-local-store directory)
              result (cache/lookup-or-compute! store key base ops)
              receipt (assoc-in (:producer-receipt result)
                                [:authority :aggregate-authoritative?] true)]
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/lookup! store key (assoc ops :unknown true))))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/lookup! store key
                                      (dissoc ops :validate-output!))))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/store! store key (:artifact result) receipt ops)))
          (is (false? (get-in result [:cache-evidence :release?])))
          (is (false? (get-in result [:cache-evidence :proof?]))))
        (finally (delete-tree! directory))))))

(deftest different-key-producers-overlap-while-same-key-is-serialized
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) entered (atom 0) both-entered (promise)
            release (promise) base (request)
            request-a base
            request-b (assoc base :target-id (sha (char 49)))
            key-a (cache/stage-cache-key request-a)
            key-b (cache/stage-cache-key request-b)
            store-a (cache/open-local-store directory)
            store-b (cache/open-local-store directory)
            producer (fn [_]
                       (let [n (swap! entered inc)]
                         (when (= n 2) (deliver both-entered true))
                         @release)
                       {:kind :generic-artifact :artifact-id (:artifact ids)
                        :payload [:overlap]})
            ops (assoc (operations calls) :produce! producer)
            first-result (future
                           (cache/lookup-or-compute! store-a key-a request-a ops))
            second-result (future
                            (cache/lookup-or-compute! store-b key-b request-b ops))]
        ;; If global admission were held over producer execution, the second
        ;; producer could never reach this barrier.
        (is (= true (deref both-entered 3000 false)))
        (deliver release true)
        (is (#{:stored :hit} (:status (deref first-result 3000 nil))))
        (is (#{:stored :hit} (:status (deref second-result 3000 nil))))
        (is (= 2 @entered)))
      (finally (delete-tree! directory)))))

(deftest independent-process-same-key-convergence-and-crash-cleanup
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-process-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        marker (.resolve ^Path directory "producer.log")]
    (try
      (let [first-future (future (run-independent-cache-child!
                                  directory marker nil false false true))
            second-future (future (run-independent-cache-child!
                                   directory marker nil false false true))
            first (deref first-future 45000 nil)
            second (deref second-future 45000 nil)]
        (is first)
        (is second)
        (is (:available? first))
        (is (:available? second))
        (is (:finished? first))
        (is (:finished? second))
        (is (false? (:alive? first)))
        (is (false? (:alive? second)))
        (is (zero? (:exit first))
            (str "first child log: " (:log-text first)))
        (is (zero? (:exit second))
            (str "second child log: " (:log-text second)))
        ;; Independent processes must converge on one immutable entry;
        ;; the producer marker is written exactly once across both.
        (let [marker-exists? (Files/exists marker (make-array LinkOption 0))]
          (is marker-exists?
              (str "first child log: " (:log-text first)
                   "\nsecond child log: " (:log-text second)))
          (when marker-exists?
            (is (= 1 (count (Files/readAllLines marker)))))))
      (finally (delete-tree! directory)))))

(deftest independent-process-different-key-producers-overlap
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-process-overlap-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        marker (.resolve ^Path directory "producer.log")]
    (try
      (let [target-a (sha (char 49)) target-b (sha (char 50))
            first-future (future (run-independent-cache-child!
                                  directory marker target-a true false true))
            second-future (future (run-independent-cache-child!
                                   directory marker target-b true false true))
            first (deref first-future 45000 nil)
            second (deref second-future 45000 nil)]
        (is first)
        (is second)
        (is (:available? first))
        (is (:available? second))
        (is (:finished? first))
        (is (:finished? second))
        (is (false? (:alive? first)))
        (is (false? (:alive? second)))
        (is (zero? (:exit first))
            (str "first child log: " (:log-text first)))
        (is (zero? (:exit second))
            (str "second child log: " (:log-text second)))
        (let [marker-exists? (Files/exists marker (make-array LinkOption 0))]
          (is marker-exists?
              (str "first child log: " (:log-text first)
                   "\nsecond child log: " (:log-text second)))
          (when marker-exists?
            (is (= 2 (count (Files/readAllLines marker)))))))
      (finally (delete-tree! directory)))))

(deftest killed-independent-writer-releases-lock-for-recovery
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-process-kill-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        marker (.resolve ^Path directory "producer.log")]
    (try
      (let [killed (run-independent-cache-child!
                    directory marker nil false true false)
            process ^Process (:process killed)
            ready (.resolve ^Path directory "publication.ready")
            staging (.resolve ^Path directory
                              ".cpcache/compiler-pass/v2/staging")]
        (is (:available? killed))
        (is (instance? java.lang.Process process))
        (is (await-path! ready 30000)
            "writer must durably stage bytes before forced termination")
        (with-open [paths (Files/list staging)]
          (is (seq (vec (.toArray paths)))
              "a durable staging residue must exist before kill"))
        (.destroyForcibly process)
        (is (.waitFor process 10 TimeUnit/SECONDS))
        (is (false? (.isAlive process)))
        (let [recovery (run-independent-cache-child!
                        directory marker nil false false true)]
          (is (:finished? recovery))
          (is (false? (:alive? recovery)))
          (is (zero? (:exit recovery))
              (str "recovery child log: " (:log-text recovery)))
          (with-open [paths (Files/list staging)]
            (is (empty? (vec (.toArray paths)))
                "reopen must reclaim killed-writer staging residue"))))
      (finally (delete-tree! directory)))))

(deftest admission-happens-before-any-publication
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            store (cache/open-local-store directory)
            ops (operations calls)
            count-var (ns-resolve 'gravity.pass-cache 'maximum-entry-count)
            bytes-var (ns-resolve 'gravity.pass-cache 'maximum-store-bytes)
            original-count (var-get count-var)
            original-bytes (var-get bytes-var)]
        (try
          (alter-var-root count-var (constantly 0))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/lookup-or-compute! store key req ops)))
          (with-open [entries (Files/list (:entries store))]
            (is (empty? (vec (.toArray entries)))))
          (finally (alter-var-root count-var
                                   (constantly original-count))))
        (try
          (alter-var-root bytes-var (constantly 0))
          (is (thrown? clojure.lang.ExceptionInfo
                       (cache/lookup-or-compute! store key req ops)))
          (with-open [blobs (Files/list (:blobs store))]
            (is (empty? (vec (.toArray blobs)))))
          (finally (alter-var-root bytes-var
                                   (constantly original-bytes)))))
      (finally (delete-tree! directory)))))

(deftest no-follow-special-files-unsafe-modes-and-active-staging
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [outside (Files/createTempDirectory
                     "gravity-pass-cache-outside-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
            cpcache (.resolve directory ".cpcache")
            compiler-pass (.resolve cpcache "compiler-pass")
            v2 (.resolve compiler-pass "v2")]
        (Files/createDirectories
         compiler-pass (make-array java.nio.file.attribute.FileAttribute 0))
        (Files/createSymbolicLink v2 outside (make-array java.nio.file.attribute.FileAttribute 0))
        (is (thrown? clojure.lang.ExceptionInfo
                     (cache/open-local-store directory)))
        (delete-tree! directory))
      (finally (when (Files/exists directory (make-array LinkOption 0))
                 (delete-tree! directory)))))
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            store (cache/open-local-store directory) ops (operations calls)]
        (cache/lookup-or-compute! store key req ops)
        (let [entry-path (.resolve ^Path (:entries store)
                                   (str (:cache-key-id key) ".edn"))
              outside (Files/createTempFile
                       "gravity-pass-cache-entry-" ".edn"
                       (make-array java.nio.file.attribute.FileAttribute 0))]
          (private-write! outside "outside")
          (Files/delete entry-path)
          (Files/createSymbolicLink entry-path outside
                                    (make-array java.nio.file.attribute.FileAttribute 0))
          (is (= [:rejected "C16-POLICY"]
                 (let [result (cache/lookup! store key ops)]
                   [(:status result)
                    (get-in result [:cache-evidence :contained-diagnostic])])))
          (is (= "outside" (String. (Files/readAllBytes outside))))
          ;; Remove the hostile link before exercising later inventory paths;
          ;; the outside target remains untouched and the entry is republished
          ;; through the normal immutable path.
          (Files/deleteIfExists entry-path)
          (cache/lookup-or-compute! store key req ops))
        (let [stage (.resolve ^Path (:staging store)
                              ".stage-00000000-0000-0000-0000-000000000000.tmp")]
          (private-write! stage "active")
          (swap! (var-get (ns-resolve 'gravity.pass-cache 'active-staging))
                 conj (str stage))
          (let [reopened (cache/open-local-store directory)]
            (is (Files/exists stage (make-array LinkOption 0)))
            (swap! (var-get (ns-resolve 'gravity.pass-cache 'active-staging))
                   disj (str stage))
            (is (map? reopened))))
        (let [fifo (.resolve ^Path (:entries store)
                             (str (sha (char 49)) ".edn"))]
          (make-fifo! fifo)
          (is (= [:rejected "C16-POLICY"]
                 (let [result (cache/lookup! store key ops)]
                   [(:status result)
                    (get-in result [:cache-evidence :contained-diagnostic])])))))
      (finally (delete-tree! directory)))))

(deftest hardlink-lock-and-entry-identity-are-rejected
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            store (cache/open-local-store directory) ops (operations calls)]
        (cache/lookup-or-compute! store key req ops)
        (let [entry-path (.resolve ^Path (:entries store)
                                   (str (:cache-key-id key) ".edn"))
              copy (Files/createTempFile
                    "gravity-pass-cache-copy-" ".edn"
                    (make-array java.nio.file.attribute.FileAttribute 0))]
          (Files/deleteIfExists copy)
          (Files/createLink copy entry-path)
          (is (= [:rejected "C16-POLICY"]
                 (let [result (cache/lookup! store key ops)]
                   [(:status result)
                    (get-in result [:cache-evidence :contained-diagnostic])]))))
        (let [lock-path (.resolve ^Path (:locks store)
                                  (str (:cache-key-id key) ".lock"))
              target (Files/createTempFile
                      "gravity-pass-cache-lock-" ".tmp"
                      (make-array java.nio.file.attribute.FileAttribute 0))]
          (Files/deleteIfExists target)
          (Files/delete lock-path)
          (Files/createSymbolicLink lock-path target
                                    (make-array java.nio.file.attribute.FileAttribute 0))
          (is (= [:rejected "C16-POLICY"]
                 (let [result (cache/lookup! store key ops)]
                   [(:status result)
                    (get-in result [:cache-evidence :contained-diagnostic])])))))
      (finally (delete-tree! directory)))))

(deftest canonical-edn-tagged-deep-and-oversize-records-are-rejected
  (let [directory (Files/createTempDirectory
                   "gravity-pass-cache-test-"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [calls (atom {}) req (request) key (cache/stage-cache-key req)
            store (cache/open-local-store directory) ops (operations calls)]
        (cache/lookup-or-compute! store key req ops)
        (let [entry-path (.resolve ^Path (:entries store)
                                   (str (:cache-key-id key) ".edn"))]
          (private-write! entry-path "#gravity/not-allowed [nil]")
          (is (= :rejected (:status (cache/lookup! store key ops))))
          (private-write! entry-path (str "[" (apply str (repeat 120 "["))
                                         "nil" (apply str (repeat 120 "]")) "]"))
          (is (= :rejected (:status (cache/lookup! store key ops))))
          (private-write! entry-path
                          (str "[" (apply str (repeat 33000 "nil ")) "]"))
          (is (= :rejected (:status (cache/lookup! store key ops)))
              "flat scalar input is bounded before EDN allocation")
          (private-write! entry-path (apply str (repeat 5000000 "x")))
          (is (= :rejected (:status (cache/lookup! store key ops))))))
      (finally (delete-tree! directory)))))
