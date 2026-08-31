(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn- p15-s23-b4-wasm-run-node!
  [candidate source-path module-bytes expected-result invocation-audit]
  (p15-s23-b4-wasm-require-authority!
   candidate source-path :run-pinned-node-wasm)
  (when-not (instance? clojure.lang.IAtom invocation-audit)
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" source-path {}
     {:missing-fact :invocation-local-node-audit}))
  (let [node (p15-s23-b4-wasm-node-preflight! candidate source-path)
        workspace (java.nio.file.Files/createTempDirectory
                   "gravity-b4-wasm-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        encoded (.encodeToString (java.util.Base64/getEncoder)
                                 (byte-array (map unchecked-byte
                                                  module-bytes)))
        command [(:actual-path node) "-e" p15-s23-b4-wasm-node-script
                 encoded (str expected-result)]
        process-holder (atom nil)
        stdout-holder (atom nil)
        stderr-holder (atom nil)
        primary (atom nil)]
    (try
      (java.nio.file.Files/setPosixFilePermissions
       workspace #{java.nio.file.attribute.PosixFilePermission/OWNER_READ
                   java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
                   java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE})
      (let [builder (ProcessBuilder. ^java.util.List command)
            _ (.directory builder (.toFile workspace))
            environment (.environment builder)
            _ (.clear environment)
            _ (.put environment "PATH" "/usr/bin:/bin")
            _ (.put environment "LC_ALL" "C")
            _ (.put environment "LANG" "C")
            _ (.put environment "HOME" (.toString workspace))
            _ (.put environment "TMPDIR" (.toString workspace))
            _ (.redirectErrorStream builder false)
            process (.start builder)
            _ (reset! process-holder process)
            _ (swap! p15-s23-b4-wasm-node-state update :starts inc)
            _ (swap! invocation-audit update :starts inc)
            _ (.close (.getOutputStream process))
            stdout-future (future (p15-s23-b4-wasm-read-stream
                                   (.getInputStream process)))
            _ (reset! stdout-holder stdout-future)
            stderr-future (future (p15-s23-b4-wasm-read-stream
                                   (.getErrorStream process)))
            _ (reset! stderr-holder stderr-future)
            finished? (.waitFor process p15-s23-b4-wasm-node-timeout-ms
                                java.util.concurrent.TimeUnit/MILLISECONDS)
            termination (when-not finished?
                          (p15-s23-b4-wasm-kill-tree! process))
            _ (when (and termination
                         (not (true?
                               (:captured-process-set-reaped? termination))))
                (p15-s23-b4-wasm-fail!
                 "B4-TARGET" source-path {}
                 {:missing-fact :captured-process-set-reaping
                  :captured-descendant-count
                  (:descendant-count termination)
                  :captured-process-set-reaped?
                  (:captured-process-set-reaped? termination)}))
            stdout (deref stdout-future 3000 nil)
            stderr (deref stderr-future 3000 nil)]
        (when-not (and finished? stdout stderr
                       (zero? (.exitValue process))
                       (= "" (:text stderr))
                       (= (str "B4NODE1:" expected-result "\n")
                          (:text stdout)))
          (let [exit-code (when finished? (.exitValue process))
                diagnostic
                (if (not finished?)
                  "B4-TARGET"
                  (case exit-code
                    71 "B4-TARGET"
                    72 "B4-MANIFEST"
                    73 "B4-IMPORT"
                    74 "B4-EXPORT"
                    75 "B4-EXPORT"
                    76 "B14-DIFFERENTIAL"
                    77 "B4-TARGET"
                    0 "B14-DIFFERENTIAL"
                    "B4-TARGET"))]
           (p15-s23-b4-wasm-fail!
           diagnostic
           source-path {}
           {:missing-fact (if finished?
                            :pinned-node-wasm-result
                            :bounded-node-timeout-and-process-tree-cleanup)
            :exit-code exit-code
            :timed-out? (not finished?)
            :expected-result expected-result
            :invocation-local-start-count (:starts @invocation-audit)
            :captured-descendant-count (:descendant-count termination)
            :captured-process-set-reaped?
            (:captured-process-set-reaped? termination)})))
        (when-not (= {:starts 1} @invocation-audit)
          (p15-s23-b4-wasm-fail!
           "B4-MANIFEST" source-path {}
           {:missing-fact :exactly-one-invocation-local-node-process
            :invocation-local-start-count (:starts @invocation-audit)}))
        {:artifact :gravity/b4-node-conformance-execution
         :status :passed :tool :node :version (:version node)
         :architecture (:architecture node)
         :tool-content-hash (:content-hash node)
         :tool-byte-count (:byte-count node)
         :probe-script-hash p15-s23-b4-wasm-node-script-hash
         :timeout-ms p15-s23-b4-wasm-node-timeout-ms
         :stdin :closed :environment :fixed-private
         :imports [] :exports [{:name "main" :kind :function}]
         :validate :passed :compile :passed :instantiate :passed
         :expected-result expected-result :observed-result expected-result
         :repeat-result expected-result
         :stdout-hash (:hash stdout) :stdout-byte-count (:byte-count stdout)
         :stderr-hash (:hash stderr) :stderr-byte-count (:byte-count stderr)
         :invocation-local-start-count (:starts @invocation-audit)
         :process-tree termination
         :atomic-tool-identity-binding? false
         :whole-process-tree-reaping-proved? false})
      (catch InterruptedException interrupted
        (reset! primary interrupted)
        (doseq [reader [@stdout-holder @stderr-holder]]
          (when reader (future-cancel reader)))
        (when-let [process @process-holder]
          (try
            (p15-s23-b4-wasm-kill-tree! process)
            (catch Throwable cleanup
              (.addSuppressed interrupted cleanup))))
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch Throwable error
        (reset! primary error)
        (when-let [process @process-holder]
          (when (.isAlive process)
            (p15-s23-b4-wasm-kill-tree! process)))
        (doseq [reader [@stdout-holder @stderr-holder]]
          (when reader (future-cancel reader)))
        (throw error))
      (finally
        (try
          (p15-s23-b4-wasm-delete-tree! workspace)
          (catch Throwable cleanup
            (if-let [failure @primary]
              (.addSuppressed ^Throwable failure cleanup)
              (throw cleanup))))))))

(defn p15-s23-b4-wasm-source-rule [binding]
  {:artifact :gravity/b4-pinned-source-rule
   :owner :gravity-source
   :source-content-hash (:source-content-hash binding)
   :source-byte-count (:source-byte-count binding)
   :plan-semantic-hash (:plan-semantic-hash binding)
   :functions-semantic-hash (:functions-semantic-hash binding)
   :builder-function p15-s23-b4-wasm-builder-function
   :builder-semantic-hash (:builder-semantic-hash binding)
   :function-shapes (:function-shapes binding)
   :compiled-by :clojure-stage0-seed
   :executed-by :clojure-stage0-rule-runner
   :self-hosted? false})

(defn p15-s23-b4-wasm-semantic-input [artifact]
  (p15-s23-c13-c14-b1-path-neutral-value
   (dissoc artifact :semantic-id :artifact-id :actual-path-binding-id
           :actual-path-provenance)))

(defn p15-s23-b4-wasm-artifact-id [artifact]
  (p15-s23-c11-mir-digest (p15-s23-b4-wasm-semantic-input artifact)))

(defn p15-s23-b4-wasm-actual-path-binding-id
  [semantic-id provenance]
  (p15-s23-c11-mir-digest
   {:kind :gravity/b4-actual-path-binding
    :semantic-id semantic-id :actual-path-provenance provenance})))
