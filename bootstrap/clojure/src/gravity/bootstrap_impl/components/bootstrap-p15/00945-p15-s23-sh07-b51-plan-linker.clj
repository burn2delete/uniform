(declare c-backend-canonical-value c4-artifact-id reader-canonical-value
         p15-s23-compiler-source-form-record-from-text
         p15-s23-compiler-def-value
         p15-s23-stage2-compiler-artifact-augmented-emitter
         sh06-resolution-definition-records sh06-resolution-core-records
         sh06-resolution-analysis-inputs
         p15-s23-sh07-b51-derive-callsite-census
         p15-s23-stage2-construct-emitted-core-plan
         p15-s23-sh07-b51-plan-valid-structure?
         p15-s23-sh07-b51-plan-links-valid-structure?
         p15-s23-sh07-b51-execution-bundle-core-valid-structure?
         p15-s23-sh07-b51-execution-bundle-valid-structure?
         p15-s23-sh07-b51-execution-bundle-authorized?
         p15-s23-sh07-b51-current-authoritative-linked-products-v4
         ^:private p15-s23-sh07-b51-issue-top-level-validation-receipt!
         ^:private p15-s23-sh07-b51-retire-admitted-runtime-context!
         p15-s23-stage2-runtime-execute-function
         ^:private p15-s23-stage2-runtime-execute-function-scoped)

(def p15-s23-sh07-b51-module-specs
  [{:module-ordinal 0
    :module 'gravity.sh07-b51.producer-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 1
    :module 'gravity.sh07-b51.producer-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 2
    :module 'gravity.sh07-b51.producer-c
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-resolve-digest-preimage]}
   {:module-ordinal 3
    :module 'gravity.sh07-b51.producer-d
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-build-template
               sh07-b51-verify-template
               sh07-b51-resolve-template
               sh07-b51-verify-resolved
               sh07-b51-build-final-artifact]}
   {:module-ordinal 4
    :module 'gravity.sh07-b51.root6-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
    :owner :r6-independent-verifier
    :exports []}
   {:module-ordinal 5
    :module 'gravity.sh07-b51.root6-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
    :owner :r6-independent-verifier
    :exports '[sh07-b51-build-independent-verifier-binding]}
   {:module-ordinal 6
    :module 'gravity.sh07-b51.root8
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
    :owner :r8-finalizer
    :exports '[sh07-b51-finalize-rejection]}])

(def ^:private p15-s23-sh07-b51-source-specs
  (vec
   (concat
    [{:ordinal 0 :kind :l1-contract :module-ordinal nil
      :path "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"}
     {:ordinal 1 :kind :l2-contract :module-ordinal nil
      :path "docs/phase-01-core-language/012-l2-core-language-semantics.md"}
     {:ordinal 2 :kind :checked-core :module-ordinal nil
      :path "bootstrap/gravity/src/gravity/checked_core.gravity"}]
    (mapv (fn [{:keys [module-ordinal logical-source-path]}]
            {:ordinal (+ 3 module-ordinal)
             :kind :module
             :module-ordinal module-ordinal
             :path logical-source-path})
          p15-s23-sh07-b51-module-specs)
    [{:ordinal 10 :kind :compiler :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/compiler.gravity"}
     {:ordinal 11 :kind :emitter :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/emitter.gravity"}])))

(def ^:private p15-s23-sh07-b51-source-event-plan
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal)
       :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal)
       :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal)
       :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal)
       :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal)
       :domain :compiler-artifact-binding
       :source-ordinal 11
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding}))))

;; This marker is denial-only: it grants no source or product authority.  The
;; V9 object-mode wrapper binds it before opening its private C/T session so a
;; legacy compatibility branch can never fall through to a checked-out path,
;; even if a later refactor accidentally reaches that branch.  The session's
;; unforgeable token remains the positive authority for every governed read.
(def ^:dynamic ^:private
  *p15-s23-sh07-b51-v9-receipted-bundle-construction* false)

(defn- p15-s23-sh07-b51-governed-read-fail!
  [reason details]
  (throw (ex-info "SH-07 B51 governed source read rejected"
                  {:id "SH07-B51-00945-GOVERNED-READ"
                   :reason reason
                   :details details})))

(def ^:private p15-s23-sh07-b51-git-limits
  {:stdout-max-bytes 1073741824
   :stderr-max-bytes 1048576
   :deadline-ms 3600000})

(def ^:private p15-s23-sh07-b51-checker-entrypoint
  "gravity.tools.sh07-b51-attempt32-checker-child")

(def ^:private p15-s23-sh07-b51-coordinator-entrypoint
  "gravity.tools.sh07-b51-attempt32-coordinator-child")

(def ^:private p15-s23-sh07-b51-coordinator-option-names
  ["--snapshot-commit"
   "--snapshot-tree"
   "--completed-at"
   "--evidence-relative-root"
   "--staging-root"
   "--eligibility-review-commit"
   "--eligibility-review-tree"
   "--eligibility-receipt-path"
   "--eligibility-receipt-blob"
   "--eligibility-receipt-byte-count"
   "--eligibility-receipt-raw-sha256"
   "--eligibility-receipt-id"])

(defn- p15-s23-sh07-b51-read-stream-bytes
  [^java.io.InputStream stream maximum coordinate]
  (let [output (java.io.ByteArrayOutputStream.)
        buffer (byte-array 8192)]
    (loop [byte-count 0]
      (let [count (.read stream buffer)]
        (if (neg? count)
          (.toByteArray output)
          (let [next-count (+ byte-count count)]
            (when (> next-count maximum)
              (p15-s23-sh07-b51-governed-read-fail!
               :git-stream-overflow
               {:coordinate coordinate
                :maximum maximum
                :observed-at-least next-count}))
            (.write output buffer 0 count)
            (recur next-count)))))))

(defn- p15-s23-sh07-b51-strict-utf8
  [bytes coordinate]
  (try
    (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                    (.onMalformedInput
                     java.nio.charset.CodingErrorAction/REPORT)
                    (.onUnmappableCharacter
                     java.nio.charset.CodingErrorAction/REPORT))]
      (str (.decode decoder (java.nio.ByteBuffer/wrap bytes))))
    (catch java.nio.charset.CharacterCodingException _
      (p15-s23-sh07-b51-governed-read-fail!
       :malformed-utf8 {:coordinate coordinate}))))

(defn- p15-s23-sh07-b51-raw-sha256
  [bytes]
  (str "sha256:" (sha256-bytes-hex bytes)))

(defn- p15-s23-sh07-b51-git-arguments-valid?
  [arguments]
  (let [git-id? #(and (string? %)
                      (boolean (re-matches #"[0-9a-f]{40}" %)))
        source-paths (set (map :path p15-s23-sh07-b51-source-specs))]
    (and
     (vector? arguments)
     (or
      (and (= 3 (count arguments))
           (= ["rev-parse" "--verify"] (subvec arguments 0 2))
           (boolean
            (re-matches #"[0-9a-f]{40}\^\{(?:commit|tree)\}"
                        (nth arguments 2))))
      (and (= 5 (count arguments))
           (= ["ls-tree" "-z"] (subvec arguments 0 2))
           (git-id? (nth arguments 2))
           (= "--" (nth arguments 3))
           (contains? source-paths (nth arguments 4)))
      (and (= 3 (count arguments))
           (= ["cat-file" "blob"] (subvec arguments 0 2))
           (git-id? (nth arguments 2)))))))

(defn- p15-s23-sh07-b51-remaining-millis
  [deadline-nanos]
  (max 0
       (long
        (Math/ceil
         (/ (double (- deadline-nanos (System/nanoTime))) 1000000.0)))))

(defn- p15-s23-sh07-b51-close-quietly!
  [value]
  (when value
    (try
      (.close ^java.io.Closeable value)
      (catch Throwable _ nil))))

(defn- p15-s23-sh07-b51-stop-git!
  [^Process process futures executor]
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)
      (.destroyForcibly process)
      (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)))
  (doseq [stream [(.getOutputStream process)
                  (.getInputStream process)
                  (.getErrorStream process)]]
    (p15-s23-sh07-b51-close-quietly! stream))
  (doseq [future futures]
    (.cancel ^java.util.concurrent.Future future true))
  (.shutdownNow ^java.util.concurrent.ExecutorService executor))

(defn- p15-s23-sh07-b51-completed-git-task!
  [completion deadline-nanos]
  (let [remaining (p15-s23-sh07-b51-remaining-millis deadline-nanos)
        future (.poll ^java.util.concurrent.ExecutorCompletionService
                      completion remaining
                      java.util.concurrent.TimeUnit/MILLISECONDS)]
    (when-not future
      (p15-s23-sh07-b51-governed-read-fail!
       :git-timeout {:deadline-ms (:deadline-ms p15-s23-sh07-b51-git-limits)}))
    (try
      (.get ^java.util.concurrent.Future future)
      (catch java.util.concurrent.ExecutionException error
        (throw (.getCause error))))))

(defn- p15-s23-sh07-b51-git-bytes
  [arguments]
  (when-not (and (vector? arguments)
                 (every? string? arguments)
                 (p15-s23-sh07-b51-git-arguments-valid? arguments))
    (p15-s23-sh07-b51-governed-read-fail!
     :git-argv {:arguments arguments}))
  (let [argv (into ["/usr/bin/git" "-C" (System/getProperty "user.dir")]
                   arguments)
        process (-> (ProcessBuilder. ^java.util.List argv) .start)
        executor (java.util.concurrent.Executors/newFixedThreadPool 3)
        completion (java.util.concurrent.ExecutorCompletionService. executor)
        stdout-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stdout
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getInputStream process)
                       (:stdout-max-bytes p15-s23-sh07-b51-git-limits)
                       :stdout)})))
        stderr-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stderr
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getErrorStream process)
                       (:stderr-max-bytes p15-s23-sh07-b51-git-limits)
                       :stderr)})))
        exit-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_] {:kind :exit :value (.waitFor process)})))
        futures [stdout-future stderr-future exit-future]
        deadline-nanos
        (+ (System/nanoTime)
           (* 1000000 (:deadline-ms p15-s23-sh07-b51-git-limits)))
        completed? (atom false)]
    (.close (.getOutputStream process))
    (try
      (let [results
            (loop [remaining 3 values {}]
              (if (zero? remaining)
                values
                (let [{:keys [kind] :as result}
                      (p15-s23-sh07-b51-completed-git-task!
                       completion deadline-nanos)]
                  (when (contains? values kind)
                    (p15-s23-sh07-b51-governed-read-fail!
                     :git-task-duplicate {:kind kind}))
                  (recur (dec remaining) (assoc values kind result)))))
            stdout (get-in results [:stdout :bytes])
            stderr (get-in results [:stderr :bytes])
            exit (get-in results [:exit :value])]
        (when-not (and (zero? exit) (zero? (alength ^bytes stderr)))
          (p15-s23-sh07-b51-governed-read-fail!
           :git-object-read
           {:arguments arguments
            :exit exit
            :stdout-byte-count (alength ^bytes stdout)
            :stderr-byte-count (alength ^bytes stderr)
            :stderr (p15-s23-sh07-b51-strict-utf8 stderr :git-stderr)}))
        (reset! completed? true)
        stdout)
      (finally
        (if @completed?
          (do
            (doseq [stream [(.getInputStream process) (.getErrorStream process)]]
              (p15-s23-sh07-b51-close-quietly! stream))
            (.shutdownNow executor))
          (p15-s23-sh07-b51-stop-git! process futures executor))))))

(defn- p15-s23-sh07-b51-single-lf-line
  [bytes coordinate]
  (let [text (p15-s23-sh07-b51-strict-utf8 bytes coordinate)]
    (when-not (boolean (re-matches #"[^\r\n]+\n" text))
      (p15-s23-sh07-b51-governed-read-fail!
       :git-line-grammar {:coordinate coordinate}))
    (subs text 0 (dec (count text)))))

(defn- p15-s23-sh07-b51-assert-content-coordinate!
  [commit tree phase ordinal]
  (let [actual-commit
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{commit}")])
         [phase ordinal :commit])
        actual-tree
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{tree}")])
         [phase ordinal :tree])]
    (when-not (and (= commit actual-commit) (= tree actual-tree))
      (p15-s23-sh07-b51-governed-read-fail!
       :content-coordinate
       {:phase phase :ordinal ordinal
        :expected {:commit commit :tree tree}
        :actual {:commit actual-commit :tree actual-tree}}))))

(defn- p15-s23-sh07-b51-ls-tree-row
  [commit path]
  (let [bytes (p15-s23-sh07-b51-git-bytes
               ["ls-tree" "-z" commit "--" path])
        text (p15-s23-sh07-b51-strict-utf8 bytes [:ls-tree path])]
    (when-not (and (pos? (count text))
                   (= \u0000 (last text))
                   (= 1 (count (filter #(= \u0000 %) text))))
      (p15-s23-sh07-b51-governed-read-fail!
       :ls-tree-record {:path path}))
    (let [record (subs text 0 (dec (count text)))
          tab (.indexOf ^String record "\t")
          header (when (pos? tab) (subs record 0 tab))
          observed-path (when (pos? tab) (subs record (inc tab)))
          fields (when header (str/split header #" " -1))]
      (when-not (and (= 3 (count fields))
                     (= "100644" (nth fields 0 nil))
                     (= "blob" (nth fields 1 nil))
                     (boolean (re-matches #"[0-9a-f]{40}"
                                          (nth fields 2 "")))
                     (= path observed-path))
        (p15-s23-sh07-b51-governed-read-fail!
         :ls-tree-projection {:path path :record record}))
      {:mode (nth fields 0) :type (nth fields 1)
       :blob-oid (nth fields 2)})))

(defn- p15-s23-sh07-b51-acquire-source-entry
  [commit tree spec]
  (let [{:keys [ordinal path]} spec]
    (p15-s23-sh07-b51-assert-content-coordinate!
     commit tree :pre-acquisition ordinal)
    (let [tree-row (p15-s23-sh07-b51-ls-tree-row commit path)
          bytes (p15-s23-sh07-b51-git-bytes
                 ["cat-file" "blob" (:blob-oid tree-row)])
          text (p15-s23-sh07-b51-strict-utf8 bytes [:source ordinal path])
          descriptor
          {:ordinal ordinal
           :kind (:kind spec)
           :module-ordinal (:module-ordinal spec)
           :path path
           :mode (:mode tree-row)
           :blob-oid (:blob-oid tree-row)
           :byte-count (alength bytes)
           :raw-sha256 (p15-s23-sh07-b51-raw-sha256 bytes)}]
      (p15-s23-sh07-b51-assert-content-coordinate!
       commit tree :post-acquisition ordinal)
      {:descriptor descriptor :bytes bytes :text text})))

(defn- p15-s23-sh07-b51-process-role-fail!
  [reason details]
  (throw (ex-info "SH-07 B51 process role rejected"
                  {:id "SH07-B51-TRUSTED-TCB"
                   :reason reason
                   :details details})))

(defn- p15-s23-sh07-b51-process-role-for-observation
  [{:keys [arguments loaded-project-namespaces]}]
  (let [arguments (when (vector? arguments) arguments)
        loaded (set loaded-project-namespaces)
        checker-child
        'gravity.tools.sh07-b51-attempt32-checker-child
        coordinator
        'gravity.tools.sh07-b51-attempt32-coordinator-child
        producer
        'gravity.tools.sh07-b51-attempt30-producer-census
        independent
        'gravity.tools.sh07-b51-attempt30-independent-census
        bootstrap 'gravity.bootstrap
        checker-tail (when (and arguments (<= 5 (count arguments)))
                       (subvec arguments (- (count arguments) 5)))
        coordinator-tail-count
        (+ 3 (* 2 (count p15-s23-sh07-b51-coordinator-option-names)))
        coordinator-tail
        (when (and arguments
                   (<= coordinator-tail-count (count arguments)))
          (subvec arguments (- (count arguments) coordinator-tail-count)))
        coordinator-option-names
        (when coordinator-tail
          (mapv first (partition 2 (subvec coordinator-tail 3))))
        producer-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "producer-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child producer])
             (not-any? loaded [coordinator independent]))
        independent-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "independent-verifier-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child independent])
             (not-any? loaded [coordinator producer]))
        coordinator-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-coordinator-entrypoint]
                (when coordinator-tail (subvec coordinator-tail 0 3)))
             (= p15-s23-sh07-b51-coordinator-option-names
                coordinator-option-names)
             (every? loaded [bootstrap coordinator])
             (not-any? loaded [checker-child producer independent]))]
    (cond
      producer-process? :producer-census
      independent-process? :independent-verifier-census
      coordinator-process? :m
      :else
      (p15-s23-sh07-b51-process-role-fail!
       :unauthenticated-process-role
       {:argument-count (count arguments)
        :loaded-project-namespaces (vec (sort-by str loaded))}))))

(defn- p15-s23-sh07-b51-process-role
  []
  (let [info (.info (java.lang.ProcessHandle/current))
        argument-value (.arguments info)
        arguments (when (.isPresent argument-value)
                    (vec (.get argument-value)))
        loaded-project-namespaces
        (->> (loaded-libs)
             (filter #(str/starts-with? (str %) "gravity."))
             set)]
    (p15-s23-sh07-b51-process-role-for-observation
     {:arguments arguments
      :loaded-project-namespaces loaded-project-namespaces})))

(defn- p15-s23-sh07-b51-open-source-session
  [commit tree]
  (when-not (and (string? commit)
                 (boolean (re-matches #"[0-9a-f]{40}" commit))
                 (string? tree)
                 (boolean (re-matches #"[0-9a-f]{40}" tree)))
    (p15-s23-sh07-b51-governed-read-fail!
     :content-coordinate-shape {:commit commit :tree tree}))
  (let [entries (mapv #(p15-s23-sh07-b51-acquire-source-entry
                        commit tree %)
                      p15-s23-sh07-b51-source-specs)
        descriptors (mapv :descriptor entries)
        token (Object.)
        state {:token token
               :thread (Thread/currentThread)
               :role (p15-s23-sh07-b51-process-role)
               :commit commit :tree tree
               :entries entries
               :descriptors descriptors
               :descriptors-root
               (reader-canonical-hash
                [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
               :cursor (atom 0)
               :events (atom [])
               :checked-products (atom {})
               :finalized (atom false)}]
    {:token token :state state}))

(defn- p15-s23-sh07-b51-active-source-session
  []
  (let [{:keys [token state] :as binding-value}
        *p15-s23-sh07-b51-governed-source-session*]
    (when-not (and (= #{:token :state} (set (keys binding-value)))
                   (map? state)
                   (identical? token (:token state))
                   (identical? (Thread/currentThread) (:thread state))
                   (false? @(:finalized state)))
      (p15-s23-sh07-b51-governed-read-fail!
       :inactive-or-forged-session {}))
    state))

(defn- p15-s23-sh07-b51-governed-source-entry
  [source-ordinal]
  (let [state (p15-s23-sh07-b51-active-source-session)]
    (when-not (and (integer? source-ordinal)
                   (<= 0 source-ordinal)
                   (< source-ordinal (count (:entries state))))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-ordinal {:source-ordinal source-ordinal}))
    (nth (:entries state) source-ordinal)))

(defn- p15-s23-sh07-b51-source-event-product-id
  [event product]
  (reader-canonical-hash
   [:gravity/sh07-b51-source-event-product-id-v1
    {:ordinal (:ordinal event)
     :domain (:domain event)
     :consumer (:consumer event)
     :source-raw-sha256 (:source-raw-sha256 event)
     :product product}]))

(defn- p15-s23-sh07-b51-governed-source-event!
  [domain source-ordinal module-ordinal consumer product-fn]
  (let [state (p15-s23-sh07-b51-active-source-session)
        ordinal @(:cursor state)
        expected (nth p15-s23-sh07-b51-source-event-plan ordinal nil)
        coordinate {:ordinal ordinal :domain domain
                    :source-ordinal source-ordinal
                    :module-ordinal module-ordinal
                    :consumer consumer}]
    (when-not (= expected coordinate)
      (p15-s23-sh07-b51-governed-read-fail!
       :event-order {:expected expected :actual coordinate}))
    (let [entry (nth (:entries state) source-ordinal)
          product (product-fn entry)
          raw-sha (get-in entry [:descriptor :raw-sha256])
          event-base (assoc coordinate :source-raw-sha256 raw-sha)
          product-id
          (p15-s23-sh07-b51-source-event-product-id event-base product)
          event (assoc event-base :product-id product-id)]
      (swap! (:events state) conj event)
      (swap! (:cursor state) inc)
      product)))

(defn- p15-s23-sh07-b51-session-checked-product
  [key]
  (get @(:checked-products (p15-s23-sh07-b51-active-source-session)) key))

(defn- p15-s23-sh07-b51-cache-session-product!
  [key value]
  (let [cache (:checked-products (p15-s23-sh07-b51-active-source-session))]
    (when (contains? @cache key)
      (p15-s23-sh07-b51-governed-read-fail!
       :duplicate-checked-product {:key key}))
    (swap! cache assoc key value)
    value))

(defn- p15-s23-sh07-b51-finalize-source-session!
  []
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptors (:descriptors state)
        events @(:events state)
        counters {:acquire-count (count (:entries state))
                  :decode-count (count (:entries state))
                  :event-count (count events)
                  :finalize-count 1}]
    (when-not (and (= 12 (count descriptors))
                   (= 39 @(:cursor state) (count events))
                   (= (vec (range 39)) (mapv :ordinal events))
                   (= {:acquire-count 12 :decode-count 12
                       :event-count 39 :finalize-count 1}
                      counters)
                   (compare-and-set! (:finalized state) false true))
      (p15-s23-sh07-b51-governed-read-fail!
       :session-finalize
       {:descriptor-count (count descriptors)
        :event-count (count events)
        :cursor @(:cursor state)}))
    {:schema :gravity/sh07-b51-source-read-receipt-v3
     :role (:role state)
     :commit (:commit state)
     :tree (:tree state)
     :source-descriptors descriptors
     :source-descriptors-root (:descriptors-root state)
     :events events
     :event-root
     (reader-canonical-hash
      [:gravity/sh07-b51-source-event-root-v1 events])
     :counters counters
     :finalized true}))

(defn- p15-s23-sh07-b51-active-module-ordinal
  [source-path]
  (some (fn [{:keys [module-ordinal logical-source-path]}]
          (when (= logical-source-path source-path) module-ordinal))
        p15-s23-sh07-b51-module-specs))

(defn- p15-s23-sh07-b51-compatibility-source-record
  [source-path]
  (when *p15-s23-sh07-b51-v9-receipted-bundle-construction*
    (p15-s23-sh07-b51-governed-read-fail!
     :legacy-live-source-read-in-v9
     {:source-path source-path}))
  (let [direct (when (string? source-path) (java.io.File. source-path))
        file (if (and direct (.isFile direct))
               (.getCanonicalFile direct)
               (p15-s23-sh07-governed-source-file source-path))]
    (when-not (and file (.isFile ^java.io.File file))
      (p15-s23-sh07-b51-governed-read-fail!
       :compatibility-source-missing {:source-path source-path}))
    (let [bytes (with-open [stream (java.io.FileInputStream. ^java.io.File file)]
                  (p15-s23-sh07-b51-read-stream-bytes
                   stream 268435456 :compatibility-source))]
      {:path (.getCanonicalPath ^java.io.File file)
       :bytes bytes
       :text (p15-s23-sh07-b51-strict-utf8 bytes source-path)})))

(def p15-s23-sh07-b51-public-root-specs
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(def ^:private p15-s23-sh07-b51-m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def ^:private p15-s23-sh07-b51-definition-keys
  #{:name :arity :visibility :source-hash})

(def ^:private p15-s23-sh07-b51-external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})

(def ^:private p15-s23-sh07-b51-edge-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})

(def ^:private p15-s23-sh07-b51-census-edge-keys
  #{:ordinal :caller-module-ordinal :caller-module :caller-function
    :callsite-path :callee-module-ordinal :callee-module :callee-function
    :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-m1-keys
  #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(def ^:private p15-s23-sh07-b51-m3-keys
  #{:artifact :schema-version :manifest-id :module-plans :routes
    :public-roots :bundle-core-id :compile-census-certificate-ids
    :bundle-id})

(def ^:private p15-s23-sh07-b51-bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})

(def ^:private p15-s23-sh07-b51-root8-external-callees
  '[sh07-b51-build-template
    sh07-b51-entrypoint-envelope
    sh07-b51-exact-keys?
    sh07-b51-kind-remediation
    sh07-b51-kind-rule
    sh07-b51-nonnegative-integer?
    sh07-b51-pending-rejected-envelope-shape?
    sh07-b51-pending-rejection
    sh07-b51-raw-carrier-shape?
    sh07-b51-semantic-coordinate
    sh07-b51-sha256-id?
    sh07-b51-template-shape-for-plan?])

;; Attempt 30 cannot acquire implementation authority until both independent
;; census certificates and the resulting M3/root snapshot have been frozen.
;; These deliberately non-digest values fail every authority check and must be
;; replaced only by a separately reviewed immutable implementation snapshot.
(def ^:private p15-s23-sh07-b51-expected-m3-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-a-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-b-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-semantic-root
  :gravity/provisional-no-authority)

;; Historical public name retained only so older host code fails closed rather
;; than resolving a removed Var.  It is not an accepted M3-v1 pin.
(def p15-s23-sh07-b51-expected-execution-bundle-id
  p15-s23-sh07-b51-expected-m3-id)

(def p15-s23-sh07-b51-host-pin-boundary-id
  "sha256:ecae8acf77cf8c64588aef4cad380c0a7873baab1b282398b03f532ae94b0a6e")

(def p15-s23-sh07-b51-runtime-token-boundary-id
  "sha256:844b3ea1b9bd75f416bfb51ff2c40e9a8aed1803813d6a11b3e54e44e75809ad")

(def ^:private p15-s23-sh07-b51-admitted-runtime-contexts
  (java.util.HashMap.))
(def ^:private p15-s23-sh07-b51-admitted-runtime-context-queue
  (java.lang.ref.ReferenceQueue.))
(def ^:private p15-s23-sh07-b51-registry-prune-maximum 16)
(def ^:private p15-s23-sh07-b51-maximum-external-calls 65536)

(def ^:private p15-s23-sh07-b51-runtime-authority-keys
  #{:sh07-b51-runtime-context-token
    :sh07-b51-runtime-token-boundary-id
    :sh07-b51-linked-carrier-v4
    :sh07-b51-linked-carrier-v3
    :sh07-b51-module-source-descriptors
    :sh07-b51-link-manifest
    :sh07-b51-module-plans
    :sh07-b51-execution-bundle
    :sh07-b51-external-call-trace})

(deftype ^:private P15S23Sh07B51IdentityTokenQuery
  [token ^int identity-hash]
  Object
  (hashCode [_] identity-hash)
  (equals [_ other]
    (and (instance? java.lang.ref.WeakReference other)
         (identical? token
                     (.get ^java.lang.ref.WeakReference other)))))

(alter-meta! #'->P15S23Sh07B51IdentityTokenQuery assoc :private true)

(defn- p15-s23-sh07-b51-weak-identity-token-key [token]
  (let [identity-hash (System/identityHashCode token)]
    (proxy [java.lang.ref.WeakReference]
      [token p15-s23-sh07-b51-admitted-runtime-context-queue]
      (hashCode [] identity-hash)
      (equals [other]
        (let [registered-token (proxy-super get)]
          (and registered-token
               (instance? java.lang.ref.WeakReference other)
               (identical?
                registered-token
                (.get ^java.lang.ref.WeakReference other))))))))

(defn- p15-s23-sh07-b51-token-query [token]
  (P15S23Sh07B51IdentityTokenQuery.
   token (System/identityHashCode token)))

(defn- p15-s23-sh07-b51-prune-admitted-runtime-contexts! []
  (loop [remaining p15-s23-sh07-b51-registry-prune-maximum]
    (when (pos? remaining)
      (when-let [key (.poll ^java.lang.ref.ReferenceQueue
                            p15-s23-sh07-b51-admitted-runtime-context-queue)]
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts key)
        (recur (dec remaining))))))

(defn- p15-s23-sh07-b51-register-runtime-context! [context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [token (.get ^java.lang.ref.WeakReference (:token-ref context))]
      (.put ^java.util.HashMap p15-s23-sh07-b51-admitted-runtime-contexts
            (p15-s23-sh07-b51-weak-identity-token-key token)
            (java.lang.ref.WeakReference. context))))
  context)

(defn- p15-s23-sh07-b51-runtime-context-by-token [token]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          context (when context-ref
                    (.get ^java.lang.ref.WeakReference context-ref))]
      (when (and context-ref (nil? context))
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query))
      context)))

(defn- p15-s23-sh07-b51-remove-runtime-context!
  [token context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          registered-context
          (when context-ref (.get ^java.lang.ref.WeakReference context-ref))]
      (when (identical? context registered-context)
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query)
        true))))

(defn- p15-s23-sh07-b51-unadmitted-runtime-context [runtime]
  (assoc (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         :sh07-b51-external-call-trace (atom [])))

(defn- p15-s23-sh07-b51-register-admitted-runtime-context
  [linked-products carrier raw route args runtime]
  (let [token (Object.)
        trace (atom [])
        top-level-invocation (Object.)
        top-level-owner
        (java.util.concurrent.atomic.AtomicReference. {:state :entering})
        receipt-slot
        (java.util.concurrent.atomic.AtomicReference. {:state :available})
        admitted-runtime
        (merge
         (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         {:sh07-b51-runtime-context-token token
          :sh07-b51-runtime-token-boundary-id
          p15-s23-sh07-b51-runtime-token-boundary-id
          :sh07-b51-linked-carrier-v4 carrier
          ;; The Stage2 V2 builtin bridge still reads this historical key.
          ;; It holds the same carrier-v4 object; no carrier-v3 is admitted.
          :sh07-b51-linked-carrier-v3 carrier
          :sh07-b51-module-source-descriptors
          (:module-source-descriptors linked-products)
          :sh07-b51-link-manifest (:link-manifest linked-products)
          :sh07-b51-module-plans (:module-plans linked-products)
         :sh07-b51-execution-bundle (:execution-bundle linked-products)
         :sh07-b51-external-call-trace trace})
        context
        {:token-ref (java.lang.ref.WeakReference. token)
         :runtime-ref (java.lang.ref.WeakReference. admitted-runtime)
         :carrier carrier
         :module-source-descriptors
         (:module-source-descriptors linked-products)
         :link-manifest (:link-manifest linked-products)
         :module-plans (:module-plans linked-products)
         :execution-bundle (:execution-bundle linked-products)
         :external-call-trace trace
         :authority-ref {:carrier carrier :products linked-products}
         :raw-ref raw
         :selected-root-ref (:root route)
         :selected-plan-ref (:plan route)
         :arguments-ref args
         :top-level-invocation-ref top-level-invocation
         :top-level-owner-ref top-level-owner
         :receipt-slot-ref receipt-slot
         :thread-ref (Thread/currentThread)}]
    (when (contains? #{1 6 8} (get-in route [:root :root-ordinal]))
      (when-not (and (vector? args)
                     (pos? (count args))
                     (identical? raw (nth args 0))
                     (identical? carrier
                                 (:v18-executable-descriptor raw)))
        (throw (ex-info "selected SH-07 root arguments are not admission-bound"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-invocation-identity}))))
    (try
      (p15-s23-sh07-b51-issue-top-level-validation-receipt! context)
      (p15-s23-sh07-b51-register-runtime-context! context)
      {:runtime admitted-runtime :context-lease context}
      (catch Throwable failure
        (p15-s23-sh07-b51-retire-admitted-runtime-context! context)
        (p15-s23-sh07-b51-remove-runtime-context! token context)
        (throw failure)))))

(defn- p15-s23-sh07-b51-admitted-runtime-context [runtime]
  (when (map? runtime)
    (let [token (:sh07-b51-runtime-context-token runtime)
          context
          (when token
            (p15-s23-sh07-b51-runtime-context-by-token token))]
      (when
       (and context
            (identical? token
                        (.get ^java.lang.ref.WeakReference
                              (:token-ref context)))
            (identical? runtime
                        (.get ^java.lang.ref.WeakReference
                              (:runtime-ref context)))
            (identical? (Thread/currentThread) (:thread-ref context))
            (= p15-s23-sh07-b51-runtime-token-boundary-id
               (:sh07-b51-runtime-token-boundary-id runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v4 runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v3 runtime))
            (identical? (:module-source-descriptors context)
                        (:sh07-b51-module-source-descriptors runtime))
            (identical? (:link-manifest context)
                        (:sh07-b51-link-manifest runtime))
            (identical? (:module-plans context)
                        (:sh07-b51-module-plans runtime))
            (identical? (:execution-bundle context)
                        (:sh07-b51-execution-bundle runtime))
            (identical? (:external-call-trace context)
                        (:sh07-b51-external-call-trace runtime)))
        context))))

(defn p15-s23-sh07-b51-runtime-admitted-products-match?
  [runtime carrier descriptors manifest plans bundle]
  (boolean
   (when-let [context
              (p15-s23-sh07-b51-admitted-runtime-context runtime)]
     (and (identical? carrier (:carrier context))
          (identical? descriptors (:module-source-descriptors context))
          (identical? manifest (:link-manifest context))
          (identical? plans (:module-plans context))
          (identical? bundle (:execution-bundle context))))))

(def ^:private p15-s23-sh07-b51-effective-builtins
  '#{* + - / < <= = > >= assoc conj count first get hash-map list pr-str
     rest second str vector symbol? keyword? char? number? list? seq? vector?
     map? set? string? contains? even? set sort-by-pr-str vec quot subvec
     integer? boolean? keys sh07-canonical-text-compare
     sh07-declared-digest-hash sh07-observe-utf8-byte-count
     sh07-b51-admitted-executable-carrier-validation?})

(defn p15-s23-sh07-b51-exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn p15-s23-sh07-b51-digest-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-sh07-b51-canonical-id [value]
  (reader-canonical-hash value))

(defn p15-s23-sh07-b51-owner-edge-kind
  [caller-owner callee-owner]
  (cond
    (= caller-owner callee-owner)
    (case caller-owner
      :r1-producer :producer-internal
      :r6-independent-verifier :root6-internal
      nil)

    (and (= :r8-finalizer caller-owner)
         (= :r1-producer callee-owner))
    :root8-to-producer-interface

    :else nil))

(defn p15-s23-sh07-b51-binding-census [source-path module module-ordinal]
  (let [syntax-stream
        (mapv (fn [ordinal form]
                {:form form
                 :syntax/id
                 (p15-s23-sh07-b51-canonical-id
                  {:domain :gravity/sh07-b51-attempt24-syntax-v1
                   :module-ordinal module-ordinal :form-ordinal ordinal})
                 :span {:source source-path :form-index ordinal}})
              (range) (:forms module))
        sh05
        {:artifact-id
         (p15-s23-sh07-b51-canonical-id
          {:domain :gravity/sh07-b51-attempt24-sh05-census-v1
           :module-ordinal module-ordinal})
         :expanded-syntax-stream syntax-stream}
        definitions (sh06-resolution-definition-records module sh05)
        core (sh06-resolution-core-records)
        analysis (sh06-resolution-analysis-inputs module sh05 {})
        lexical (vec (mapcat :bindings (:lexical-scopes analysis)))]
    {:definition-count (count definitions)
     :core-binding-count (count core)
     :lexical-binding-count (count lexical)}))

(defn p15-s23-sh07-b51-builtin-call-census [function-table]
  (vec
   (mapcat
    (fn [caller]
      (let [definition (get function-table caller)
            caller-hash (p15-s23-sh07-source-definition-hash definition)]
        (mapcat
         (fn [body-form]
           (letfn [(walk [value]
                     (cond
                       (seq? value)
                       (if (= 'quote (first value))
                         []
                         (let [callee (first value)
                               row
                               (when (contains? p15-s23-sh07-b51-effective-builtins
                                                callee)
                                 [{:caller caller
                                   :caller-function-source-hash caller-hash
                                   :builtin callee
                                   :arity (count (rest value))
                                   :compiler-internal?
                                   (contains?
                                    p15-s23-sh07-declared-builtin-exact-arities
                                    callee)}])]
                           (into (or row []) (mapcat walk value))))
                       (map? value)
                       (mapcat walk
                               (mapcat identity
                                       (sort-by #(pr-str
                                                  (reader-canonical-value %))
                                                value)))
                       (coll? value) (mapcat walk value)
                       :else []))]
             (walk body-form)))
         (:body definition))))
    (sort-by str (keys function-table)))))

(defn p15-s23-sh07-b51-module-spec [ordinal]
  (when (and (integer? ordinal) (<= 0 ordinal 6))
    (nth p15-s23-sh07-b51-module-specs ordinal)))

(defn p15-s23-sh07-b51-definition-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-definition-keys)
       (symbol? (:name row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:public :private} (:visibility row))
       (p15-s23-sh07-b51-digest-id? (:source-hash row))))

(defn p15-s23-sh07-b51-external-binding-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-external-binding-keys)
       (symbol? (:symbol row))
       (integer? (:callee-module-ordinal row))
       (<= 0 (:callee-module-ordinal row) 6)
       (symbol? (:callee-module row))
       (p15-s23-sh07-b51-digest-id? (:callee-source-sha256 row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-definition-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))))

(defn p15-s23-sh07-b51-builtin-call-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-builtin-call-keys)
       (symbol? (:caller row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (contains? p15-s23-sh07-b51-effective-builtins (:builtin row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (= (contains? p15-s23-sh07-declared-builtin-exact-arities
                     (:builtin row))
          (:compiler-internal? row))
       (or (not (:compiler-internal? row))
           (= (get p15-s23-sh07-declared-builtin-exact-arities
                   (:builtin row))
              (:arity row)))))

(defn p15-s23-sh07-b51-module-source-id [descriptor]
  (p15-s23-sh07-b51-canonical-id
   (dissoc descriptor :module-source-id)))

(defn- p15-s23-sh07-b51-module-input-record
  [spec {:keys [descriptor bytes text]}]
  (let [path (:logical-source-path spec)
        module (parse-module path (read-forms path text))
        table (stage0-function-table module)]
    (when-not
     (and (= path (:path descriptor))
          (= (:module spec) (:module module))
          (= :meta (:profile module))
          (= :jvm (:target module))
          (= #{} (:effects module))
          (= #{} (:capabilities module))
          (= :safe (:safety module)))
      (p15-s23-sh07-b51-governed-read-fail!
       :module-source-contract
       {:module-ordinal (:module-ordinal spec) :path path}))
    {:spec spec :path path :text text :bytes bytes
     :source-descriptor descriptor :module module :table table
     :source-sha256 (p15-s23-sh07-b51-raw-sha256 bytes)}))

(defn- p15-s23-sh07-b51-validate-module-input!
  [module-ordinal]
  (let [spec (p15-s23-sh07-b51-module-spec module-ordinal)
        validated?
        (p15-s23-sh07-b51-governed-source-event!
         :module-validation (+ 3 module-ordinal) module-ordinal
         'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*
         (fn [entry]
           (p15-s23-sh07-b51-cache-session-product!
            [:module-input module-ordinal]
            (p15-s23-sh07-b51-module-input-record spec entry))
           true))]
    (when-not (true? validated?)
      (p15-s23-sh07-b51-governed-read-fail!
       :module-validation {:module-ordinal module-ordinal}))
    (p15-s23-sh07-b51-session-checked-product
     [:module-input module-ordinal])))

(defn- p15-s23-sh07-b51-compatibility-module-input
  [spec]
  (let [{:keys [path bytes text]}
        (p15-s23-sh07-b51-compatibility-source-record
         (:logical-source-path spec))
        descriptor {:path (:logical-source-path spec)}]
    (assoc (p15-s23-sh07-b51-module-input-record
            spec {:descriptor descriptor :bytes bytes :text text})
           :compatibility-path path)))

(defn- p15-s23-sh07-b51-module-source-valid-with-input?
  [descriptor source]
  (let [spec (:spec source)
        definitions (:definitions descriptor)
        external-bindings (:external-bindings descriptor)
        builtin-calls (:builtin-calls descriptor)
        lexical-count (:lexical-binding-count descriptor)
        aggregate (when (and (vector? definitions)
                             (integer? (:core-binding-count descriptor))
                             (integer? lexical-count)
                             (vector? external-bindings))
                    (+ (:core-binding-count descriptor)
                       (count definitions) lexical-count
                       (count external-bindings)))
        source-bytes (:bytes source)
        source-module (:module source)
        source-table (:table source)
        source-path (:path source)
        expected-binding-census
        (when source-module
          (p15-s23-sh07-b51-binding-census
           source-path source-module
           (:module-ordinal spec)))
        expected-builtin-calls
        (when source-table
          (p15-s23-sh07-b51-builtin-call-census source-table))
        expected-definitions
        (when source-table
          (mapv (fn [form]
                  (let [definition
                        (function-definition-form source-path form)
                        definition (assoc definition :arity
                                          (count (:params definition)))]
                    {:name (:name definition) :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)}))
                (filter #(function-definition-form source-path %)
                        (:forms source-module))))]
    (and (p15-s23-sh07-b51-exact-keys? descriptor
                                              p15-s23-sh07-b51-m0-keys)
         (= :gravity/sh07-b51-module-source-v1 (:artifact descriptor))
         (= 1 (:schema-version descriptor))
         (= (select-keys spec [:module-ordinal :module :logical-source-path
                               :owner :exports])
            (select-keys descriptor
                         [:module-ordinal :module :logical-source-path
                          :owner :exports]))
         (integer? (:source-byte-count descriptor))
         (<= 0 (:source-byte-count descriptor))
         (p15-s23-sh07-b51-digest-id? (:source-sha256 descriptor))
         (= (:source-byte-count descriptor) (alength source-bytes))
         (= (:source-sha256 descriptor)
            (str "sha256:" (sha256-bytes-hex source-bytes)))
         (= :meta (:profile descriptor))
         (= :jvm (:target descriptor))
         (= #{} (:effects descriptor))
         (= #{} (:capabilities descriptor))
         (= :safe (:safety descriptor))
         (vector? definitions)
         (every? p15-s23-sh07-b51-definition-row-valid? definitions)
         (= expected-definitions definitions)
         (= (count definitions) (count (set (map :name definitions))))
         (= (:exports descriptor)
            (vec (filter (set (:exports descriptor))
                         (map :name definitions))))
         (every? #(= (contains? (set (:exports descriptor)) (:name %))
                      (= :public (:visibility %)))
                 definitions)
         (= (:core-binding-count expected-binding-census)
            (:core-binding-count descriptor))
         (integer? (:lexical-binding-count descriptor))
         (<= 0 (:lexical-binding-count descriptor))
         (= (:lexical-binding-count expected-binding-census)
            (:lexical-binding-count descriptor))
         (= (:definition-count expected-binding-census)
            (count definitions))
         (vector? external-bindings)
         (every? p15-s23-sh07-b51-external-binding-row-valid?
                 external-bindings)
         (= (count external-bindings)
            (count (set (map :symbol external-bindings))))
         (vector? builtin-calls)
         (every? p15-s23-sh07-b51-builtin-call-row-valid? builtin-calls)
         (= expected-builtin-calls builtin-calls)
         (= aggregate (:aggregate-binding-count descriptor))
         (<= aggregate 2440)
         (= (:module-source-id descriptor)
            (p15-s23-sh07-b51-module-source-id descriptor)))))

(defn- p15-s23-sh07-b51-module-source-valid* [descriptor]
  (when-let [spec (p15-s23-sh07-b51-module-spec
                   (:module-ordinal descriptor))]
    (let [source
          (if *p15-s23-sh07-b51-governed-source-session*
            (p15-s23-sh07-b51-session-checked-product
             [:module-input (:module-ordinal descriptor)])
            (p15-s23-sh07-b51-compatibility-module-input spec))]
      (and source
           (p15-s23-sh07-b51-module-source-valid-with-input?
            descriptor source)))))

(defn p15-s23-sh07-b51-module-source-valid? [descriptor]
  (try
    (boolean (p15-s23-sh07-b51-module-source-valid* descriptor))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-module-source [descriptor]
  (let [sealed (assoc descriptor :module-source-id
                      (p15-s23-sh07-b51-module-source-id descriptor))]
    (when-not (p15-s23-sh07-b51-module-source-valid? sealed)
      (throw (ex-info "invalid SH-07 B51 M0 descriptor"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    sealed))

(defn p15-s23-sh07-b51-build-module-source
  [source-path source-text module binding-census external-bindings
   builtin-calls]
  (let [spec (first (filter #(= (:module module) (:module %))
                            p15-s23-sh07-b51-module-specs))
        source-bytes (.getBytes ^String source-text
                                java.nio.charset.StandardCharsets/UTF_8)
        function-table (stage0-function-table module)
        definitions
        (mapv (fn [form]
                (when-let [definition (function-definition-form source-path form)]
                  (let [definition (assoc definition
                                          :arity (count (:params definition)))]
                    {:name (:name definition)
                     :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)})))
              (filter #(function-definition-form source-path %)
                      (:forms module)))
        base
        {:artifact :gravity/sh07-b51-module-source-v1
         :schema-version 1
         :module-source-id nil
         :module-ordinal (:module-ordinal spec)
         :module (:module spec)
         :logical-source-path (:logical-source-path spec)
         :owner (:owner spec)
         :source-byte-count (alength source-bytes)
         :source-sha256 (str "sha256:" (sha256-bytes-hex source-bytes))
         :profile (:profile module)
         :target (:target module)
         :effects (:effects module)
         :capabilities (:capabilities module)
         :safety (:safety module)
         :definitions definitions
         :exports (:exports spec)
         :core-binding-count (:core-binding-count binding-census)
         :lexical-binding-count (:lexical-binding-count binding-census)
         :external-bindings external-bindings
         :aggregate-binding-count
         (+ (:core-binding-count binding-census)
            (:definition-count binding-census)
            (:lexical-binding-count binding-census)
            (count external-bindings))
         :builtin-calls builtin-calls}]
    (when-not (and spec
                   (= source-path (:logical-source-path spec))
                   (= (set (keys function-table))
                      (set (map :name definitions)))
                   (= (count definitions)
                      (:definition-count binding-census)))
      (throw (ex-info "invalid SH-07 B51 module source input"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (p15-s23-sh07-b51-seal-module-source base)))

(defn p15-s23-sh07-b51-edge-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys? row p15-s23-sh07-b51-edge-keys)
       (integer? (:ordinal row)) (<= 0 (:ordinal row))
       (p15-s23-sh07-b51-digest-id? (:caller-module-source-id row))
       (symbol? (:caller-function row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (vector? (:callsite-path row))
       (every? #(and (integer? %) (<= 0 %)) (:callsite-path row))
       (p15-s23-sh07-b51-digest-id? (:callee-module-source-id row))
       (symbol? (:callee-function row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-function-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))
       (= :authenticated-b51-namespace-external (:authorization row))))

(defn p15-s23-sh07-b51-manifest-id [manifest]
  (p15-s23-sh07-b51-canonical-id (dissoc manifest :manifest-id)))

(defn- p15-s23-sh07-b51-definition-by-name [descriptor function]
  (first (filter #(= function (:name %)) (:definitions descriptor))))

(defn- p15-s23-sh07-b51-link-manifest-valid*
  [descriptors manifest]
  (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
        ordinal-by-id (into {} (map (juxt :module-source-id :module-ordinal))
                            descriptors)
        edges (:edges manifest)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        root8 (when (and (vector? descriptors) (= 7 (count descriptors)))
                (nth descriptors 6))
        root8-edges
        (filter #(= (:module-source-id root8)
                    (:caller-module-source-id %)) edges)
        root8-root1-edges
        (filter #(and (= 'sh07-b51-finalize-rejection
                         (:caller-function %))
                      (= 'sh07-b51-build-template
                         (:callee-function %))) root8-edges)
        root6-source-ids
        (if (and (vector? descriptors) (= 7 (count descriptors)))
          (set (map :module-source-id (subvec descriptors 4 6)))
          #{})
        root6-cross-edges
        (filter #(and (contains? root6-source-ids
                                 (:caller-module-source-id %))
                      (not (contains? root6-source-ids
                                      (:callee-module-source-id %))))
                edges)
        source-census (p15-s23-sh07-b51-derive-callsite-census)
        projected-census
        (mapv (fn [edge]
                (let [caller (get by-id (:caller-module-source-id edge))
                      callee (get by-id (:callee-module-source-id edge))]
                  {:ordinal (:ordinal edge)
                   :caller-module-ordinal (:module-ordinal caller)
                   :caller-module (:module caller)
                   :caller-function (:caller-function edge)
                   :callsite-path (:callsite-path edge)
                   :callee-module-ordinal (:module-ordinal callee)
                   :callee-module (:module callee)
                   :callee-function (:callee-function edge)
                   :arity (:arity edge)
                   :owner-edge (:owner-edge edge)}))
              edges)]
    (and (= 7 (count descriptors))
         (= (range 7) (map :module-ordinal descriptors))
         (every? p15-s23-sh07-b51-module-source-valid? descriptors)
         (p15-s23-sh07-b51-exact-keys? manifest p15-s23-sh07-b51-m1-keys)
         (= :gravity/sh07-b51-link-manifest-v1 (:artifact manifest))
         (= 1 (:schema-version manifest))
         (= (mapv :module-source-id descriptors)
            (:module-source-ids manifest))
         (vector? edges)
         (= ordered edges)
         (= (vec (range (count edges))) (mapv :ordinal edges))
         (= source-census projected-census)
         (= (count edges)
            (count (set (map (juxt :caller-module-source-id
                                   :caller-function :callsite-path)
                             edges))))
         (= p15-s23-sh07-b51-root8-external-callees
            (mapv :symbol (:external-bindings root8)))
         (= 1 (count root8-root1-edges))
         (empty? root6-cross-edges)
         (every? (fn [descriptor]
                   (= (set (map :symbol (:external-bindings descriptor)))
                      (set (map :callee-function
                                (filter #(= (:module-source-id descriptor)
                                            (:caller-module-source-id %))
                                        edges)))))
                 descriptors)
         (every?
          (fn [row]
            (let [caller (get by-id (:caller-module-source-id row))
                  callee (get by-id (:callee-module-source-id row))
                  caller-definition
                  (p15-s23-sh07-b51-definition-by-name
                   caller (:caller-function row))
                  callee-definition
                  (p15-s23-sh07-b51-definition-by-name
                   callee (:callee-function row))
                  external (first (filter
                                   #(= (:callee-function row) (:symbol %))
                                   (:external-bindings caller)))
                  expected-owner-edge
                  (p15-s23-sh07-b51-owner-edge-kind
                   (:owner caller) (:owner callee))]
              (and (p15-s23-sh07-b51-edge-row-valid? row)
                   caller callee caller-definition callee-definition external
                   (= (:caller-function-source-hash row)
                      (:source-hash caller-definition))
                   (= (:callee-function-source-hash row)
                      (:source-hash callee-definition))
                   (= (:arity row) (:arity callee-definition) (:arity external))
                   (= (:module-ordinal callee)
                      (:callee-module-ordinal external))
                   (= (:module callee) (:callee-module external))
                   (= (:source-sha256 callee)
                      (:callee-source-sha256 external))
                   (= (:source-hash callee-definition)
                      (:callee-definition-source-hash external))
                   (= expected-owner-edge
                      (:owner-edge row) (:owner-edge external)))))
          edges)
         (= (:manifest-id manifest)
            (p15-s23-sh07-b51-manifest-id manifest)))))

(defn p15-s23-sh07-b51-link-manifest-valid? [descriptors manifest]
  (try
    (boolean (p15-s23-sh07-b51-link-manifest-valid* descriptors manifest))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-link-manifest [descriptors edges]
  (let [ordinal-by-id
        (into {} (map (juxt :module-source-id :module-ordinal)) descriptors)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        base {:artifact :gravity/sh07-b51-link-manifest-v1
              :schema-version 1
              :module-source-ids (mapv :module-source-id descriptors)
              :edges (mapv #(assoc %1 :ordinal %2) ordered (range))}
        manifest (assoc base :manifest-id
                        (p15-s23-sh07-b51-manifest-id base))]
    (when-not (p15-s23-sh07-b51-link-manifest-valid?
               descriptors manifest)
      (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
            projected
            (mapv (fn [edge]
                    (let [caller (get by-id (:caller-module-source-id edge))
                          callee (get by-id (:callee-module-source-id edge))]
                      {:ordinal (:ordinal edge)
                       :caller-module-ordinal (:module-ordinal caller)
                       :caller-module (:module caller)
                       :caller-function (:caller-function edge)
                       :callsite-path (:callsite-path edge)
                       :callee-module-ordinal (:module-ordinal callee)
                       :callee-module (:module callee)
                       :callee-function (:callee-function edge)
                       :arity (:arity edge)
                       :owner-edge (:owner-edge edge)}))
                  (:edges manifest))
            source-census (p15-s23-sh07-b51-derive-callsite-census)]
        (throw (ex-info "invalid SH-07 B51 M1 manifest"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-external-link
                         :descriptor-valid?
                         (mapv p15-s23-sh07-b51-module-source-valid?
                               descriptors)
                         :source-census-match? (= source-census projected)
                         :source-census-count (count source-census)
                         :projected-count (count projected)
                         :root8-external-symbols
                         (mapv :symbol
                               (:external-bindings (nth descriptors 6)))}))))
    manifest))

(defn p15-s23-sh07-b51-enrich-census-edges
  [descriptors census-edges]
  (let [by-ordinal (into {} (map (juxt :module-ordinal identity)) descriptors)]
    (when-not (and (vector? census-edges)
                   (= (vec (range (count census-edges)))
                      (mapv :ordinal census-edges))
                   (every? #(p15-s23-sh07-b51-exact-keys?
                             % p15-s23-sh07-b51-census-edge-keys)
                           census-edges))
      (throw (ex-info "invalid SH-07 B51 callsite census"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (mapv
     (fn [row]
       (let [caller (get by-ordinal (:caller-module-ordinal row))
             callee (get by-ordinal (:callee-module-ordinal row))
             caller-definition
             (p15-s23-sh07-b51-definition-by-name caller (:caller-function row))
             callee-definition
             (p15-s23-sh07-b51-definition-by-name callee (:callee-function row))]
         (when-not (and caller callee caller-definition callee-definition
                        (= (:caller-module row) (:module caller))
                        (= (:callee-module row) (:module callee)))
           (throw (ex-info "callsite census does not join module sources"
                           {:id "L2-UNKNOWN-CORE-FORM"
                            :reason :unauthenticated-b51-external-link
                            :ordinal (:ordinal row)})))
         {:ordinal (:ordinal row)
          :caller-module-source-id (:module-source-id caller)
          :caller-function (:caller-function row)
          :caller-function-source-hash (:source-hash caller-definition)
          :callsite-path (:callsite-path row)
          :callee-module-source-id (:module-source-id callee)
          :callee-function (:callee-function row)
          :callee-function-source-hash (:source-hash callee-definition)
          :arity (:arity row)
          :owner-edge (:owner-edge row)
          :authorization :authenticated-b51-namespace-external}))
     census-edges)))

(defn- p15-s23-sh07-b51-derive-callsite-census-from-modules
  [modules]
  (let [all-function-names
        (vec (mapcat (comp keys :table) modules))
        _ (when-not (= (count all-function-names)
                       (count (set all-function-names)))
            (throw (ex-info "SH-07 B51 module definitions are duplicated"
                            {:id "L2-UNKNOWN-CORE-FORM"
                             :reason :unauthenticated-b51-external-link})))
        owner-by-function
        (into {} (for [{:keys [spec table]} modules, name (keys table)]
                   [name {:module-ordinal (:module-ordinal spec)
                          :module (:module spec) :owner (:owner spec)}]))
        rows
        (vec
         (mapcat
          (fn [{:keys [spec table]}]
            (let [locals (set (keys table))]
              (mapcat
               (fn [caller]
                 (let [definition (get table caller)]
                   (mapcat
                    (fn [body-index body-form]
                      (letfn [(walk [value path]
                                (cond
                                  (seq? value)
                                  (if (= 'quote (first value))
                                    []
                                    (let [callee (first value)
                                          callee-owner (get owner-by-function callee)
                                          row
                                          (when (and callee-owner
                                                     (not (contains? locals callee)))
                                            [{:caller-module-ordinal
                                              (:module-ordinal spec)
                                              :caller-module (:module spec)
                                              :caller-function caller
                                              :callsite-path path
                                              :callee-module-ordinal
                                              (:module-ordinal callee-owner)
                                              :callee-module (:module callee-owner)
                                              :callee-function callee
                                              :arity (count (rest value))
                                              :owner-edge
                                              (cond
                                                (= (:owner spec)
                                                   (:owner callee-owner))
                                                (if (= :r1-producer (:owner spec))
                                                  :producer-internal
                                                  :root6-internal)
                                                (and (= :r8-finalizer (:owner spec))
                                                     (= :r1-producer
                                                        (:owner callee-owner)))
                                                :root8-to-producer-interface
                                                :else :forbidden-owner-edge)}])]
                                      (into (or row [])
                                            (mapcat (fn [index child]
                                                      (walk child
                                                            (conj path index)))
                                                    (range) value))))

                                  (vector? value)
                                  (mapcat (fn [index child]
                                            (walk child (conj path index)))
                                          (range) value)

                                  (map? value)
                                  (let [entries
                                        (sort-by
                                         #(pr-str (reader-canonical-value %))
                                         value)]
                                    (mapcat
                                     (fn [entry-index [key item]]
                                       (concat
                                        (walk key (conj path entry-index 0))
                                        (walk item (conj path entry-index 1))))
                                     (range) entries))

                                  (set? value)
                                  (mapcat (fn [index item]
                                            (walk item (conj path index)))
                                          (range)
                                          (sort-by
                                           #(pr-str (reader-canonical-value %))
                                           value))
                                  :else []))]
                        (walk body-form [body-index])))
                    (range) (:body definition))))
               (sort-by str (keys table)))))
          modules))
        ordered
        (vec (sort-by (juxt :caller-module-ordinal
                            (comp str :caller-function)
                            (comp count :callsite-path)
                            :callsite-path)
                      rows))]
    (when-not (and (= 1480 (count ordered))
                   (not-any? #(= :forbidden-owner-edge (:owner-edge %))
                             ordered)
                   (= (count ordered)
                      (count (set (map (juxt :caller-module-ordinal
                                             :caller-function :callsite-path)
                                       ordered)))))
      (throw (ex-info "SH-07 B51 source callsite census is not governed"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :observed-count (count ordered)})))
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn p15-s23-sh07-b51-derive-callsite-census []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :callsite-census)
        (let [modules
              (mapv
               (fn [spec]
                 (let [module-ordinal (:module-ordinal spec)
                       source
                       (p15-s23-sh07-b51-session-checked-product
                        [:module-input module-ordinal])]
                   (when-not source
                     (p15-s23-sh07-b51-governed-read-fail!
                      :module-validation-not-consumed
                      {:module-ordinal module-ordinal}))
                   (p15-s23-sh07-b51-governed-source-event!
                    :callsite-census (+ 3 module-ordinal) module-ordinal
                    'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census
                    (fn [entry]
                      (when-not (and (= (:source-descriptor source)
                                         (:descriptor entry))
                                     (= (:text source) (:text entry)))
                        (p15-s23-sh07-b51-governed-read-fail!
                         :cached-source-mismatch
                         {:module-ordinal module-ordinal
                          :domain :callsite-census}))
                      true))
                   source))
               p15-s23-sh07-b51-module-specs)
              census
              (p15-s23-sh07-b51-derive-callsite-census-from-modules modules)]
          (p15-s23-sh07-b51-cache-session-product!
           :callsite-census census)))
    (p15-s23-sh07-b51-derive-callsite-census-from-modules
     (mapv p15-s23-sh07-b51-compatibility-module-input
           p15-s23-sh07-b51-module-specs))))

(defn- p15-s23-sh07-b51-build-link-authority-from-census
  [census-edges]
  (let [sources
        (if *p15-s23-sh07-b51-governed-source-session*
          (mapv
           (fn [spec]
             (let [module-ordinal (:module-ordinal spec)
                   source
                   (p15-s23-sh07-b51-session-checked-product
                    [:module-input module-ordinal])]
               (when-not source
                 (p15-s23-sh07-b51-governed-read-fail!
                  :module-validation-not-consumed
                  {:module-ordinal module-ordinal}))
               (p15-s23-sh07-b51-governed-source-event!
                :link-authority (+ 3 module-ordinal) module-ordinal
                'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census
                (fn [entry]
                  (when-not (and (= (:source-descriptor source)
                                     (:descriptor entry))
                                 (= (:text source) (:text entry)))
                    (p15-s23-sh07-b51-governed-read-fail!
                     :cached-source-mismatch
                     {:module-ordinal module-ordinal
                      :domain :link-authority}))
                  true))
               source))
           p15-s23-sh07-b51-module-specs)
          (mapv p15-s23-sh07-b51-compatibility-module-input
                p15-s23-sh07-b51-module-specs))
        by-ordinal (into {} (map (fn [source]
                                  [(get-in source [:spec :module-ordinal])
                                   source])
                                sources))
        external-symbols
        (into {}
              (map (fn [ordinal]
                     [ordinal
                      (vec (sort-by str
                                    (set (map :callee-function
                                              (filter #(= ordinal
                                                          (:caller-module-ordinal %))
                                                      census-edges)))))]))
              (range 7))
        descriptors
        (mapv
         (fn [{:keys [spec path text module table]}]
           (let [ordinal (:module-ordinal spec)
                 externals
                 (mapv
                  (fn [symbol]
                    (let [sample (first (filter
                                         #(and (= ordinal
                                                  (:caller-module-ordinal %))
                                               (= symbol (:callee-function %)))
                                         census-edges))
                          callee-source (get by-ordinal
                                             (:callee-module-ordinal sample))
                          callee-definition (get (:table callee-source) symbol)]
                      {:symbol symbol
                       :callee-module-ordinal (:callee-module-ordinal sample)
                       :callee-module (get-in callee-source [:spec :module])
                       :callee-source-sha256 (:source-sha256 callee-source)
                       :callee-definition-source-hash
                       (p15-s23-sh07-source-definition-hash callee-definition)
                       :arity (:arity sample)
                       :owner-edge (:owner-edge sample)}))
                  (get external-symbols ordinal))
                 builtins (p15-s23-sh07-b51-builtin-call-census table)]
             (p15-s23-sh07-b51-build-module-source
              path text module
              (p15-s23-sh07-b51-binding-census
               path module (:module-ordinal spec))
              externals builtins)))
         sources)
        edges (p15-s23-sh07-b51-enrich-census-edges descriptors census-edges)
        manifest (p15-s23-sh07-b51-seal-link-manifest descriptors edges)]
    {:module-source-descriptors descriptors
     :link-manifest manifest
     :sources sources}))

(defn p15-s23-sh07-b51-build-link-authority []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :link-authority)
        (p15-s23-sh07-b51-cache-session-product!
         :link-authority
         (p15-s23-sh07-b51-build-link-authority-from-census
          (p15-s23-sh07-b51-derive-callsite-census))))
    (p15-s23-sh07-b51-build-link-authority-from-census
     (p15-s23-sh07-b51-derive-callsite-census))))

(def ^:private p15-s23-sh07-b51-compiler-source-path
  "bootstrap/gravity/p15_s23/compiler.gravity")

(defn- p15-s23-sh07-b51-current-emitter []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :current-emitter)
        (let [emitter
              (p15-s23-sh07-b51-governed-source-event!
               :compiler-emitter 10 nil
               'gravity.bootstrap/p15-s23-sh07-b51-current-emitter
               (fn [{:keys [descriptor text]}]
                 (let [source-path (:path descriptor)
                       forms
                       (:forms
                        (p15-s23-compiler-source-form-record-from-text
                         source-path text))]
                   (p15-s23-compiler-def-value
                    source-path forms 'p15-s23-stage2-plan-emitter))))]
          (p15-s23-sh07-b51-cache-session-product!
           :current-emitter emitter)))
    (let [{:keys [path text]}
          (p15-s23-sh07-b51-compatibility-source-record
           p15-s23-sh07-b51-compiler-source-path)
          forms (:forms
                 (p15-s23-compiler-source-form-record-from-text path text))]
      (p15-s23-compiler-def-value
       path forms 'p15-s23-stage2-plan-emitter))))

(defn p15-s23-sh07-b51-build-execution-bundle
  ([]
   (p15-s23-sh07-b51-build-execution-bundle
    (p15-s23-sh07-b51-current-emitter)))
  ([emitter]
  (when-not (= emitter (p15-s23-sh07-b51-current-emitter))
    (throw (ex-info "unauthenticated SH-07 B51 compiler identity"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-external-link})))
  (let [{:keys [module-source-descriptors link-manifest sources]}
        (p15-s23-sh07-b51-build-link-authority)
        sources
        (or (when *p15-s23-sh07-b51-governed-source-session*
              (p15-s23-sh07-b51-session-checked-product :plan-sources))
            sources)
        compilation-emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        raw-plans
        (mapv
         (fn [descriptor source]
           (let [module
                 (cond->
                  (assoc (:module source)
                         :p15-s23-sh07-module-source-descriptor descriptor
                         :p15-s23-sh07-link-manifest link-manifest)
                   (:source-authority source)
                   (assoc :p15-s23-sh07-source-authority
                          (:source-authority source))
                   (:compiler-artifact-binding source)
                   (assoc :p15-s23-sh07-compiler-artifact-binding
                          (:compiler-artifact-binding source)))]
             (p15-s23-stage2-construct-emitted-core-plan
              compilation-emitter (:path source) (:text source)
              module (:table source))))
         module-source-descriptors sources)
        ;; M2 identity is constructed lexically from the seven plans emitted
        ;; immediately above.  There is deliberately no Var that accepts an
        ;; arbitrary plan and returns an M2 product or id.
        plans
        (mapv
         (fn [descriptor raw-plan]
           (let [emitter-source-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-source-content-hash])
                 emitter-semantic-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-semantic-hash])
                 base
                 (-> (dissoc raw-plan :plan-id)
                     (assoc :sh07-b51-module-source-id
                            (:module-source-id descriptor)
                            :sh07-b51-link-manifest-id
                            (:manifest-id link-manifest))
                     ;; Attempt30 makes the already authenticated compiler
                     ;; binding coordinates explicit in every M2.  The two
                     ;; census implementations join these aliases without
                     ;; depending on a lower-level compiler-field spelling.
                     (assoc-in [:compiler :source-content-hash]
                               emitter-source-id)
                     (assoc-in [:compiler :semantic-hash]
                               emitter-semantic-id))
                 identity
                 (-> base
                     (update :source dissoc :path)
                     (update :module dissoc :source-path))]
             (assoc base :plan-id
                    (c4-artifact-id
                     (c-backend-canonical-value identity)))))
         module-source-descriptors raw-plans)
        plan-by-source
        (into {} (map (juxt :sh07-b51-module-source-id identity)) plans)
        bundle-core
        {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
         :schema-version 1
         :manifest-id (:manifest-id link-manifest)
         :module-plans
         (mapv (fn [descriptor plan]
                 {:module-ordinal (:module-ordinal descriptor)
                  :module-source-id (:module-source-id descriptor)
                  :plan-id (:plan-id plan)})
               module-source-descriptors plans)
         :routes
         (mapv (fn [edge]
                 {:edge-ordinal (:ordinal edge)
                  :caller-plan-id
                  (:plan-id
                   (get plan-by-source (:caller-module-source-id edge)))
                  :callee-plan-id
                  (:plan-id
                   (get plan-by-source (:callee-module-source-id edge)))})
               (:edges link-manifest))
         :public-roots
         (mapv (fn [root]
                 {:root-ordinal (:root-ordinal root)
                  :function (:function root)
                  :arity (:arity root)
                  :plan-id (:plan-id
                            (nth plans (:module-ordinal root)))})
               p15-s23-sh07-b51-public-root-specs)}
        bundle-core-id (p15-s23-sh07-b51-canonical-id bundle-core)
        emitter-source-id (get-in (first plans)
                                  [:compiler :source-content-hash])
        emitter-semantic-id (get-in (first plans)
                                    [:compiler :semantic-hash])
        products
        {:artifact :gravity/sh07-b51-linked-plan-products-v2
         :module-source-descriptors module-source-descriptors
         :link-manifest link-manifest
         :module-plans plans
         :emitter-source-id emitter-source-id
         :emitter-semantic-id emitter-semantic-id
         :execution-bundle-core bundle-core
         :execution-bundle-core-id bundle-core-id}]
    (when-not (and
               (p15-s23-sh07-b51-digest-id? emitter-source-id)
               (p15-s23-sh07-b51-digest-id? emitter-semantic-id)
               (every? #(and (= emitter-source-id
                                 (get-in % [:compiler :source-content-hash]))
                              (= emitter-semantic-id
                                 (get-in % [:compiler :semantic-hash])))
                       plans)
               (p15-s23-sh07-b51-execution-bundle-core-valid-structure?
                module-source-descriptors link-manifest plans bundle-core
                bundle-core-id))
      (throw (ex-info "invalid fresh-source SH-07 B51 bundle-core products"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :emitter-source-id emitter-source-id
                       :emitter-semantic-id emitter-semantic-id
                       :manifest-valid?
                       (p15-s23-sh07-b51-link-manifest-valid?
                        module-source-descriptors link-manifest)
                       :plan-valid?
                       (mapv #(p15-s23-sh07-b51-plan-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :plan-links-valid?
                       (mapv #(p15-s23-sh07-b51-plan-links-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :bundle-core-id bundle-core-id
                       :computed-bundle-core-id
                       (p15-s23-sh07-b51-canonical-id bundle-core)})))
    products)))

(defn- p15-s23-sh07-b51-project-context
  [commit tree source-path]
  (let [project-root-id
        (reader-canonical-hash
         [:gravity/sh07-b51-c2-project-root-id-v1
          {:content {:commit commit :tree tree}
           :scope :sh07-b51-c2-explicit-project}])]
    {:project-root-id project-root-id
     :project-root-path nil
     :project-relative-path source-path}))

(def ^:private p15-s23-sh07-b51-resolved-pass
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :passed :rule nil})

(def ^:private p15-s23-sh07-b51-resolved-reject
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :rejected :rule "C6-VERIFY"})

(defn- p15-s23-sh07-b51-session-checked-core-binding
  [emitter]
  (let [{:keys [descriptor bytes text]}
        (p15-s23-sh07-b51-session-checked-product :checked-core-entry)
        source-path (:path descriptor)
        source-hash (p15-s23-sh07-b51-raw-sha256 bytes)
        plan (p15-s23-stage2-compiler-artifact-plan
              emitter source-path text)
        identities (sh07-core-plan-identities plan)]
    (when-not
     (and (= sh07-core-expected-source-byte-count (alength bytes))
          (= sh07-core-expected-source-content-hash source-hash)
          (= sh07-core-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh07-core-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh07-core-expected-function-count (:function-count identities))
          (= sh07-core-expected-function-names-hash
             (:function-names-hash identities))
          (= sh07-core-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh07-core-public-function-hashes
             (:public-function-hashes identities))
          (= sh07-core-public-function-shapes
             (:public-function-shapes identities)))
      (p15-s23-sh07-b51-governed-read-fail!
       :checked-core-binding
       {:source-path source-path :source-hash source-hash}))
    (merge
     {:artifact :gravity/sh07-pinned-core-plan-binding
      :source-path source-path
      :source-byte-count (alength bytes)
      :source-content-hash source-hash
      :plan plan
      :semantic-authority :gravity-source
      :compiled-by :clojure-stage0-seed
      :executed-by :clojure-stage2-generic-rule-runner
      :generic-bridge-residual? true
      :self-hosted? false}
     identities)))

(defn- p15-s23-sh07-b51-build-module-transaction
  [commit tree module-ordinal source]
  (let [descriptor (:source-descriptor source)
        source-path (:path descriptor)
        source-text (:text source)
        project-context
        (p15-s23-sh07-b51-project-context commit tree source-path)
        c2-artifact
        (compiler-c2-reader-source-artifact
         source-path source-text project-context)
        c3-artifact
        (compiler-c3-syntax-source-artifact
         source-path source-text project-context c2-artifact
         sh03-reader-internal-product-authority)
        sh05-artifact
        (sh05-macro-artifact-from-c3-artifact*
         source-path c3-artifact true)
        resolution-artifact
        (sh06-resolution-finalize-candidate
         source-path
         (sh06-resolution-source-artifact-candidate
          source-path sh05-artifact true))
        resolution-verification
        (sh06-resolution-artifact-verification resolution-artifact)
        request (sh07-core-authenticated-request resolution-artifact)
        raw (sh07-core-execute!
             source-path 'sh07-build-core-template [request])
        template (:core-template raw)
        digest-requests (:digest-requests raw)
        template-verification
        (sh07-core-execute!
         source-path 'sh07-verify-core-template
         [request template digest-requests])
        resolved-digests
        (sh07-core-digest-requests source-path digest-requests)
        resolution
        (sh07-core-execute!
         source-path 'sh07-resolve-core-template
         [template resolved-digests])
        resolved (:value resolution)
        bad-resolved (assoc-in resolved [:module :namespace] 'gravity.altered)
        pass-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request resolved digest-requests resolved-digests])
        reject-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request bad-resolved digest-requests resolved-digests])
        form-ids (mapv :form-id (:forms request))
        node-form-ids (mapv :form-id (:nodes resolved))]
    (when-not
     (and (= module-ordinal (:module-ordinal descriptor))
          (= source-path (:path descriptor))
          (= (:raw-sha256 descriptor)
             (get-in c2-artifact [:source-unit-record :bytes-hash]))
          (= (:project-root-id project-context)
             (get-in c2-artifact [:source-unit-record :project-root]))
          (= {:path nil :project-root-id (:project-root-id project-context)}
             (get-in c2-artifact [:source-unit-record :project-root-record]))
          (= :passed (:status resolution-verification))
          (= source-path (get-in request [:provenance :actual-source-path]))
          (= :accepted (:status raw))
          (= :passed (:status template-verification))
          (= :accepted (:status resolution))
          (= p15-s23-sh07-b51-resolved-pass pass-actual)
          (= p15-s23-sh07-b51-resolved-reject reject-actual)
          (not= 'gravity.altered (get-in resolved [:module :namespace]))
          (empty? (:diagnostics resolved))
          (= (count (:nodes resolved)) (count (:forms request)))
          (= (count form-ids) (count (set form-ids)))
          (= (set form-ids) (set node-form-ids))
          (= (count node-form-ids) (count (set node-form-ids)))
          (<= (count digest-requests) (+ (count (:forms request)) 4)))
      (p15-s23-sh07-b51-governed-read-fail!
       :module-transaction
       {:module-ordinal module-ordinal
        :resolution-status (:status resolution-verification)
        :template-status (:status raw)
        :template-verification (:status template-verification)
        :resolution (:status resolution)}))
    {:module-ordinal module-ordinal
     :source-descriptor descriptor
     :resolution-artifact resolution-artifact
     :c2-reader-artifact c2-artifact
     :authenticated-core-request request
     :resolved-digests resolved-digests
     :module-core (:nodes resolved)
     :template-carrier template
     :generated-digest-carrier digest-requests
     :resolved-core-carrier resolved
     :module-digest-requests digest-requests}))

(defn- p15-s23-sh07-b51-source-authority-product-from-m0-m1
  [descriptor link-manifest]
  {:artifact :gravity/sh07-compiler-source-authority-v2
   :manifest-id (:manifest-id link-manifest)
   :module-source-id (:module-source-id descriptor)
   :logical-source-path (:logical-source-path descriptor)
   :source-sha256 (:source-sha256 descriptor)
   :module (:module descriptor)
   :function-source-hashes
   (into (sorted-map)
         (map (juxt :name :source-hash))
         (:definitions descriptor))})

(defn- p15-s23-sh07-b51-replay-returned-source-authority-events!
  [m0s link-manifest source-read-receipt]
  (let [events (:events source-read-receipt)
        source-descriptors (:source-descriptors source-read-receipt)]
    (when-not (and (= 7 (count m0s))
                   (= 12 (count source-descriptors))
                   (= 39 (count events)))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-authority-event-replay-shape
       {:m0-count (count m0s)
        :source-descriptor-count (count source-descriptors)
        :event-count (count events)}))
    (doseq [module-ordinal (range 7)]
      (let [event-ordinal (+ 25 module-ordinal)
            event (nth events event-ordinal)
            expected-coordinate
            (nth p15-s23-sh07-b51-source-event-plan event-ordinal)
            m0 (nth m0s module-ordinal)
            source-descriptor
            (nth source-descriptors (+ 3 module-ordinal))
            product
            (p15-s23-sh07-b51-source-authority-product-from-m0-m1
             m0 link-manifest)
            replayed-id
            (p15-s23-sh07-b51-source-event-product-id event product)]
        (when-not
         (and (= expected-coordinate
                 (select-keys event
                              [:ordinal :domain :source-ordinal
                               :module-ordinal :consumer]))
              (= (:module-ordinal m0) module-ordinal)
              (= (:logical-source-path m0) (:path source-descriptor))
              (= (:source-sha256 m0) (:raw-sha256 source-descriptor)
                 (:source-raw-sha256 event))
              (= replayed-id (:product-id event)))
          (p15-s23-sh07-b51-governed-read-fail!
           :source-authority-event-replay
           {:module-ordinal module-ordinal
            :event-ordinal event-ordinal
            :expected-product-id replayed-id
            :actual-product-id (:product-id event)}))))
    true))

(defn p15-s23-sh07-b51-build-receipted-bundle-core-products
  [commit tree]
  (binding [*p15-s23-sh07-b51-v9-receipted-bundle-construction* true]
    (let [{:keys [token state] :as session}
          (p15-s23-sh07-b51-open-source-session commit tree)]
      (binding [*p15-s23-sh07-b51-governed-source-session*
                {:token token :state state}]
      ;; These three products are semantic event values.  Their bytes remain
      ;; solely in the private session entries and never cross the wrapper.
      (p15-s23-sh07-b51-governed-source-event!
       :l1-contract 0 nil 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :l2-contract 1 nil 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :checked-core-revision 2 nil
       'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract
       (fn [{:keys [descriptor text] :as entry}]
         (p15-s23-sh07-b51-cache-session-product!
          :checked-core-entry entry)
         (vec (read-forms (:path descriptor) text))))
      (doseq [module-ordinal (range 7)]
        (p15-s23-sh07-b51-validate-module-input! module-ordinal))
      (let [census (p15-s23-sh07-b51-derive-callsite-census)
            authority
            (p15-s23-sh07-b51-build-link-authority-from-census census)
            authority
            (p15-s23-sh07-b51-cache-session-product!
             :link-authority authority)
            emitter (p15-s23-sh07-b51-current-emitter)
            compilation-emitter
            (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
            sources (:sources authority)
            descriptors (:module-source-descriptors authority)
            manifest (:link-manifest authority)
            source-authorities
            (mapv
             (fn [descriptor source]
               (let [module
                     (assoc (:module source)
                            :p15-s23-sh07-module-source-descriptor descriptor
                            :p15-s23-sh07-link-manifest manifest)]
                 (p15-s23-sh07-compiler-source-authority
                  (:path source) (:text source) module (:table source))))
             descriptors sources)
            compiler-bindings
            (mapv
             (fn [source]
               (p15-s23-stage2-compiler-artifact-binding!
                compilation-emitter (:path source)
                (:target (:module source))))
             sources)
            plan-sources
            (mapv #(assoc %1
                          :source-authority %2
                          :compiler-artifact-binding %3)
                  sources source-authorities compiler-bindings)
            _ (p15-s23-sh07-b51-cache-session-product!
               :plan-sources plan-sources)
            products
            (p15-s23-sh07-b51-build-execution-bundle emitter)
            checked-core-binding
            (p15-s23-sh07-b51-session-checked-core-binding emitter)
            checked-core-binding-var
            (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)
            module-transactions
            (with-redefs-fn
              {checked-core-binding-var (delay checked-core-binding)}
              (fn []
                (mapv
                 (fn [module-ordinal]
                   (p15-s23-sh07-b51-build-module-transaction
                    commit tree module-ordinal
                    (p15-s23-sh07-b51-session-checked-product
                     [:module-input module-ordinal])))
                 (range 7))))
            source-forms (mapv (comp vec :forms :module) sources)
            receipt (p15-s23-sh07-b51-finalize-source-session!)
            returned-products
            {:m0s (:module-source-descriptors products)
             :m1 (:link-manifest products)
             :m2s (:module-plans products)
             :bundle-core (:execution-bundle-core products)
             :execution-bundle-core-id (:execution-bundle-core-id products)
             :source-forms source-forms
             :module-transactions module-transactions
             :source-read-receipt receipt}]
        (p15-s23-sh07-b51-replay-returned-source-authority-events!
         (:m0s returned-products) (:m1 returned-products) receipt)
        returned-products)))))

(defn p15-s23-sh07-b51-runtime-context [linked-products runtime]
  ;; Historical host compatibility surface.  It deliberately cannot attach a
  ;; runtime token: Attempt32 admits invocation only through bounded carrier-v4
  ;; preflight, local authority, equality/joins, and root/route/arity checks.
  (let [_ linked-products]
    (p15-s23-sh07-b51-unadmitted-runtime-context runtime)))

(defn p15-s23-sh07-b51-runtime-augmented-plan-authorized?
  [runtime plan]
  (try
    (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
          plans (:module-plans context)]
      (boolean
       (and (map? runtime) (map? plan)
            context
            (some #(identical? plan %) plans))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execute-public-root
  [root-ordinal args runtime]
  (let [_ [root-ordinal args runtime]]
    (throw
     (ex-info "Attempt32 public roots require linked carrier-v4 admission"
              {:id "L2-UNKNOWN-CORE-FORM"
               :reason :unauthenticated-b51-linked-carrier
               :result-committed? false
               :output-committed? false}))))

(defn- p15-s23-sh07-b51-plan-valid-structure?
  [descriptor manifest plan]
  (let [plan-functions (:functions plan)
        descriptor-functions (:definitions descriptor)
        exports (set (:exports descriptor))]
  (and (p15-s23-sh07-b51-module-source-valid? descriptor)
       (map? plan-functions)
       (= (set (map :name descriptor-functions))
          (set (keys plan-functions)))
       (every? (fn [row]
                 (let [definition (get plan-functions (:name row))]
                   (and (= (:arity row) (:arity definition))
                        (= (:source-hash row)
                           (p15-s23-sh07-source-definition-hash definition))
                        (= (case (:visibility row)
                             :public :public
                             :private (if (empty? exports)
                                        :stage2-local
                                        :private)
                             ::invalid)
                           (get-in definition [:binding :visibility])))))
               descriptor-functions)
       (= (:module-source-id descriptor)
          (:sh07-b51-module-source-id plan))
       (= (:manifest-id manifest) (:sh07-b51-link-manifest-id plan))
       (= (:module descriptor) (get-in plan [:module :module]))
       (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
       (= (:plan-id plan)
          (c4-artifact-id
           (c-backend-canonical-value
            (-> plan
                (dissoc :plan-id)
                (update :source dissoc :path)
                (update :module dissoc :source-path))))))))

(defn- p15-s23-sh07-b51-plan-external-links [plan]
  (vec
   (for [[caller definition] (:functions plan)
         instruction (tree-seq coll? seq (:instructions definition))
         :when (and (map? instruction) (= :function-call (:op instruction))
                    (contains? instruction :external-link))]
     (let [ordinal (get-in instruction [:external-link :edge-ordinal])]
       {:caller caller
        :callee (:function instruction)
        :arity (count (:args instruction))
        :edge-ordinal ordinal
        :external-link (:external-link instruction)}))))

(defn- p15-s23-sh07-b51-plan-links-valid-structure?
  [descriptor manifest plan]
  (let [expected
        (filter #(= (:module-source-id descriptor)
                    (:caller-module-source-id %)) (:edges manifest))
        edge-by-ordinal (into {} (map (juxt :ordinal identity)) expected)
        actual
        (mapv (fn [row]
                (assoc row :callsite-path
                       (:callsite-path
                        (get edge-by-ordinal (:edge-ordinal row)))))
              (p15-s23-sh07-b51-plan-external-links plan))]
    (and (= (count expected) (count actual))
         (= (set (map (fn [row]
                        [(:caller-function row) (:callee-function row)
                         (:arity row) (:ordinal row)
                         (:callsite-path row)]) expected))
            (set (map (fn [row]
                        [(:caller row) (:callee row) (:arity row)
                         (:edge-ordinal row) (:callsite-path row)]) actual)))
         (every? #(= {:artifact :gravity/sh07-b51-external-call-v1
                      :manifest-id (:manifest-id manifest)
                      :edge-ordinal
                   (get-in % [:external-link :edge-ordinal])}
                     (:external-link %)) actual))))

(defn- p15-s23-sh07-b51-execution-bundle-core-valid*
  [descriptors manifest plans bundle-core bundle-core-id]
  (let [plans-by-id (into {} (map (juxt :plan-id identity)) plans)
        edge-by-ordinal (into {} (map (juxt :ordinal identity))
                              (:edges manifest))
        module-plans (:module-plans bundle-core)
        routes (:routes bundle-core)
        roots (:public-roots bundle-core)]
    (and (p15-s23-sh07-b51-link-manifest-valid? descriptors manifest)
         (= 7 (count plans))
         (every? (fn [descriptor]
                   (let [plan (nth plans (:module-ordinal descriptor))]
                     (and (p15-s23-sh07-b51-plan-valid-structure?
                           descriptor manifest plan)
                          (p15-s23-sh07-b51-plan-links-valid-structure?
                           descriptor manifest plan))))
                 descriptors)
         (p15-s23-sh07-b51-exact-keys?
          bundle-core p15-s23-sh07-b51-bundle-core-keys)
         (= :gravity/sh07-b51-execution-bundle-core-id-v1
            (:domain bundle-core))
         (= 1 (:schema-version bundle-core))
         (= (:manifest-id manifest) (:manifest-id bundle-core))
         (= (vec (range 7)) (mapv :module-ordinal module-plans))
         (= (mapv (fn [descriptor plan]
                    {:module-ordinal (:module-ordinal descriptor)
                     :module-source-id (:module-source-id descriptor)
                     :plan-id (:plan-id plan)})
                  descriptors plans)
            module-plans)
         (= (vec (range (count routes))) (mapv :edge-ordinal routes))
         (= (count routes) (count (:edges manifest)))
         (every?
          (fn [route]
            (and (= #{:edge-ordinal :caller-plan-id :callee-plan-id}
                    (set (keys route)))
                 (let [edge (get edge-by-ordinal (:edge-ordinal route))
                       caller-plan (get plans-by-id (:caller-plan-id route))
                       callee-plan (get plans-by-id (:callee-plan-id route))]
                   (and edge caller-plan callee-plan
                        (= (:caller-module-source-id edge)
                           (:sh07-b51-module-source-id caller-plan))
                        (= (:callee-module-source-id edge)
                           (:sh07-b51-module-source-id callee-plan))))))
          routes)
         (= 8 (count roots))
         (= (vec (range 1 9)) (mapv :root-ordinal roots))
         (every?
          (fn [expected]
            (let [root (nth roots (dec (:root-ordinal expected)))
                  descriptor (nth descriptors (:module-ordinal expected))
                  plan (nth plans (:module-ordinal expected))
                  definition (get-in plan [:functions (:function expected)])]
              (and (= #{:root-ordinal :function :arity :plan-id}
                      (set (keys root)))
                   (= (select-keys expected [:root-ordinal :function :arity])
                      (select-keys root [:root-ordinal :function :arity]))
                   (= (:plan-id plan) (:plan-id root))
                   (= (:module-source-id descriptor)
                      (:sh07-b51-module-source-id plan))
                   (= (:arity expected) (:arity definition)))))
          p15-s23-sh07-b51-public-root-specs)
         (= bundle-core-id
            (p15-s23-sh07-b51-canonical-id bundle-core)))))

(defn- p15-s23-sh07-b51-execution-bundle-valid*
  [descriptors manifest plans bundle]
  (let [bundle-core
        (select-keys bundle
                     [:manifest-id :module-plans :routes :public-roots])
        bundle-core
        (assoc bundle-core
               :domain :gravity/sh07-b51-execution-bundle-core-id-v1
               :schema-version 1)
        certificate-ids (:compile-census-certificate-ids bundle)]
    (and (p15-s23-sh07-b51-exact-keys? bundle p15-s23-sh07-b51-m3-keys)
         (= :gravity/sh07-b51-execution-bundle-v2 (:artifact bundle))
         (= 2 (:schema-version bundle))
         (vector? certificate-ids)
         (= 2 (count certificate-ids))
         (every? p15-s23-sh07-b51-digest-id? certificate-ids)
         (= 2 (count (set certificate-ids)))
         (p15-s23-sh07-b51-execution-bundle-core-valid*
          descriptors manifest plans bundle-core (:bundle-core-id bundle))
         (= (:bundle-id bundle)
            (p15-s23-sh07-b51-canonical-id
             {:domain :gravity/sh07-b51-execution-bundle-id-v2
              :schema-version 1
              :bundle (dissoc bundle :bundle-id)})))))

(defn- p15-s23-sh07-b51-execution-bundle-core-valid-structure?
  [descriptors manifest plans bundle-core bundle-core-id]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-core-valid*
              descriptors manifest plans bundle-core bundle-core-id))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-execution-bundle-valid-structure?
  [descriptors manifest plans bundle]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-valid*
              descriptors manifest plans bundle))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-build-execution-bundle-v2
  [products certificate-ids]
  (let [bundle-core (:execution-bundle-core products)
        base
        {:artifact :gravity/sh07-b51-execution-bundle-v2
         :schema-version 2
         :manifest-id (:manifest-id bundle-core)
         :module-plans (:module-plans bundle-core)
         :routes (:routes bundle-core)
         :public-roots (:public-roots bundle-core)
         :bundle-core-id (:execution-bundle-core-id products)
         :compile-census-certificate-ids certificate-ids}
        bundle
        (assoc base :bundle-id
               (p15-s23-sh07-b51-canonical-id
                {:domain :gravity/sh07-b51-execution-bundle-id-v2
                 :schema-version 1
                 :bundle base}))]
    (when-not (p15-s23-sh07-b51-execution-bundle-valid-structure?
               (:module-source-descriptors products)
               (:link-manifest products) (:module-plans products) bundle)
      (throw (ex-info "invalid SH-07 B51 M3-v2 construction"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    bundle))

(defn p15-s23-sh07-b51-current-bundle-core-products []
  (p15-s23-sh07-b51-build-execution-bundle))

(defn p15-s23-sh07-b51-plan-valid? [descriptor manifest plan]
  (try
    (let [products (p15-s23-sh07-b51-current-bundle-core-products)
          ordinal (:module-ordinal descriptor)]
      (and (integer? ordinal) (<= 0 ordinal 6)
           (= descriptor
              (nth (:module-source-descriptors products) ordinal nil))
           (= manifest (:link-manifest products))
           (= plan (nth (:module-plans products) ordinal nil))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-plan-links-valid? [descriptor manifest plan]
  (and (p15-s23-sh07-b51-plan-valid? descriptor manifest plan)
       (p15-s23-sh07-b51-plan-links-valid-structure?
        descriptor manifest plan)))

(defn p15-s23-sh07-b51-execution-bundle-valid?
  [descriptors manifest plans bundle]
  (try
    (let [products (p15-s23-sh07-b51-current-authoritative-linked-products-v4)]
      (and (= descriptors (:module-source-descriptors products))
           (= manifest (:link-manifest products))
           (= plans (:module-plans products))
           (= bundle (:execution-bundle products))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execution-bundle-authorized?
  [descriptors manifest plans bundle]
  (and (p15-s23-sh07-b51-digest-id? p15-s23-sh07-b51-expected-m3-id)
       (= p15-s23-sh07-b51-expected-m3-id
          (:bundle-id bundle))
       (p15-s23-sh07-b51-execution-bundle-valid?
        descriptors manifest plans bundle)))

(defn p15-s23-sh07-b51-external-instruction
  [module caller callee arity callsite-path]
  (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
        manifest (:p15-s23-sh07-link-manifest module)
        rows (filter #(and (= (:module-source-id descriptor)
                             (:caller-module-source-id %))
                           (= caller (:caller-function %))
                           (= callee (:callee-function %))
                           (= arity (:arity %))
                           (= callsite-path (:callsite-path %)))
                     (:edges manifest))]
    (when-not (and (p15-s23-sh07-b51-module-source-valid? descriptor)
                   (= (:manifest-id manifest)
                      (p15-s23-sh07-b51-manifest-id manifest))
                   (= 1 (count rows)))
      (throw (ex-info "unauthenticated SH-07 B51 external call"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :caller caller :callee callee
                       :callsite-path callsite-path})))
    {:artifact :gravity/sh07-b51-external-call-v1
     :manifest-id (:manifest-id manifest)
     :edge-ordinal (:ordinal (first rows))}))

(defn- p15-s23-sh07-b51-runtime-link-fail! [plan instruction]
  (fail! "L2-UNKNOWN-CORE-FORM"
         "stage2 runtime rejected an unauthenticated B51 external link"
         {:source-span {:source (get-in plan [:source :path])}
          :operator (:function instruction)
          :reason :unauthenticated-b51-external-link
          :result-committed? false
          :output-committed? false
          :remediation
          "Execute only the independently pinned seven-module M3 bundle."}))

(defn- p15-s23-sh07-b51-runtime-authorize-external-link*
  [runtime plan instruction]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        descriptors (:module-source-descriptors context)
        manifest (:link-manifest context)
        plans (:module-plans context)
        bundle (:execution-bundle context)
        link (:external-link instruction)
        edge (when (and (map? link) (integer? (:edge-ordinal link))
                        (<= 0 (:edge-ordinal link))
                        (vector? (:edges manifest))
                        (< (:edge-ordinal link) (count (:edges manifest))))
               (nth (:edges manifest) (:edge-ordinal link) nil))
        route (when (and edge (vector? (:routes bundle))
                         (< (:ordinal edge) (count (:routes bundle))))
                (nth (:routes bundle) (:ordinal edge) nil))
        caller-plan (when route
                      (first (filter #(= (:caller-plan-id route) (:plan-id %))
                                     plans)))
        callee-plan (when route
                      (first (filter #(= (:callee-plan-id route) (:plan-id %))
                                     plans)))
        caller-definition
        (when edge (get-in plan [:functions (:caller-function edge)]))
        occurrences
        (when caller-definition
          (filter #(= instruction %)
                  (tree-seq coll? seq (:instructions caller-definition))))]
    (when-not
     (and context
          (= #{:op :function :args :external-link} (set (keys instruction)))
          (= :function-call (:op instruction))
          (= #{:artifact :manifest-id :edge-ordinal} (set (keys link)))
          (= :gravity/sh07-b51-external-call-v1 (:artifact link))
          (= (:manifest-id manifest) (:manifest-id link))
          edge route
          (= plan caller-plan)
          (= (:plan-id plan) (:caller-plan-id route))
          (= (:sh07-b51-module-source-id plan)
             (:caller-module-source-id edge))
          (= (:function instruction) (:callee-function edge))
          (= (count (:args instruction)) (:arity edge))
          (= 1 (count occurrences))
          (= (:plan-id callee-plan) (:callee-plan-id route))
          (= (:sh07-b51-module-source-id callee-plan)
             (:callee-module-source-id edge))
          (= (:callee-function edge) (:name (get-in callee-plan
                                                     [:functions
                                                      (:callee-function edge)])))
          (= (:arity edge) (:arity (get-in callee-plan
                                           [:functions
                                            (:callee-function edge)]))))
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))
    {:callee-plan callee-plan :edge edge :route route}))

(defn p15-s23-sh07-b51-runtime-authorize-external-link
  [runtime plan instruction]
  (try
    (p15-s23-sh07-b51-runtime-authorize-external-link*
     runtime plan instruction)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (if (= :unauthenticated-b51-external-link (:reason (ex-data error)))
        (throw error)
        (p15-s23-sh07-b51-runtime-link-fail! plan instruction)))
    (catch Throwable _
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))))

(defn- p15-s23-sh07-b51-runtime-record-external-call!
  [runtime caller-plan instruction authority]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        trace (:sh07-b51-external-call-trace runtime)
        callee-plan (:callee-plan authority)]
    (when-not (and context
                   (identical? trace (:external-call-trace context))
                   (instance? clojure.lang.IAtom trace)
                   (< (count @trace) p15-s23-sh07-b51-maximum-external-calls))
      (p15-s23-sh07-b51-runtime-link-fail! caller-plan instruction))
    (swap! trace conj
           {:bundle-id (get-in runtime
                               [:sh07-b51-execution-bundle :bundle-id])
            :edge-ordinal (get-in instruction [:external-link :edge-ordinal])
            :caller-plan-id (:plan-id caller-plan)
            :callee-plan-id (:plan-id callee-plan)
            :function (:function instruction)})))
