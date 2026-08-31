(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn- p15-s23-b4-wasm-invoke-builder!
  [candidate binding b1-record source-path expected]
  (p15-s23-b4-wasm-require-authority!
   candidate source-path :invoke-pinned-gravity-b4-builder)
  (let [mir (get-in b1-record [:bounded-lowering-payload :mir])
        result
        (try
          (p15-s23-stage2-runtime-execute-function
           {:engine :gravity-b4-pinned-builder-host-runner
            :compiler-artifact-plan? true}
           (:plan binding) p15-s23-b4-wasm-builder-function [b1-record])
          (catch StackOverflowError _
            (p15-s23-b4-wasm-fail!
             "B1-UNSUPPORTED" source-path {}
             {:missing-fact :bounded-gravity-b4-builder-host-stack})))]
    (when (= :rejected (:status result))
      (p15-s23-b4-wasm-fail!
       (or (:diagnostic result) "B1-UNSUPPORTED") source-path result
       {:missing-fact (:missing-fact result)
        :operation-id (:operation-id result) :opcode (:opcode result)
        :observed-type (:type result)}))
    (when-not (p15-s23-b4-wasm-gravity-lowering-valid?
               result mir expected)
      (p15-s23-b4-wasm-fail!
       "B4-MANIFEST" source-path result
       {:missing-fact :independent-gravity-lowering-reconstruction}))
    result))

(defn- p15-s23-b4-wasm-read-stream [stream]
  (with-open [input stream output (java.io.ByteArrayOutputStream.)]
    (let [buffer (byte-array 1024)]
      (loop [total 0]
        (let [n (.read input buffer)]
          (if (neg? n)
            (let [bytes (.toByteArray output)]
              {:bytes bytes :byte-count total
               :hash (str "sha256:" (sha256-bytes-hex bytes))
               :text (String. bytes java.nio.charset.StandardCharsets/UTF_8)})
            (let [next (+ total n)]
              (when (> next p15-s23-b4-wasm-max-tool-output-bytes)
                (throw (ex-info "bounded B4 tool stream exceeded"
                                {:b4-stream-limit? true
                                 :byte-count next})))
              (.write output buffer 0 n)
              (recur next))))))))

(defn- p15-s23-b4-wasm-kill-tree! [process]
  (let [root (.toHandle process)
        descendants (with-open [stream (.descendants root)]
                      (vec (iterator-seq
                            (.iterator (.limit stream (long 65))))))]
    (doseq [handle descendants]
      (try (.destroyForcibly ^java.lang.ProcessHandle handle)
           (catch Exception _ nil)))
    (try (.destroyForcibly root) (catch Exception _ nil))
    (let [deadline (+ (System/nanoTime) 2000000000)]
      (loop []
        (let [alive (or (.isAlive root)
                        (some #(.isAlive ^java.lang.ProcessHandle %)
                              descendants))]
          (if (and alive (< (System/nanoTime) deadline))
            (do (Thread/sleep 10) (recur))
            {:descendant-count (count descendants)
             :captured-process-set-reaped? (not alive)}))))))

(defn- p15-s23-b4-wasm-delete-tree! [path]
  (when (and path (java.nio.file.Files/exists
                   path (make-array java.nio.file.LinkOption 0)))
    (with-open [stream (java.nio.file.Files/walk
                        path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (sort-by #(.getNameCount ^java.nio.file.Path %)
                             > (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- p15-s23-b4-wasm-node-preflight! [candidate source-path]
  (p15-s23-b4-wasm-require-authority!
   candidate source-path :pinned-node-preflight)
  (when-not (= p15-s23-b4-wasm-node-script-hash
               (str "sha256:" (sha256-hex p15-s23-b4-wasm-node-script)))
    (p15-s23-b4-wasm-fail!
     "B4-TARGET" source-path {}
     {:missing-fact :pinned-node-probe-script-identity
      :content-hash
      (str "sha256:" (sha256-hex p15-s23-b4-wasm-node-script))}))
  (let [path (java.nio.file.Paths/get
              p15-s23-b4-wasm-node-path (make-array String 0))]
    (when-not (java.nio.file.Files/isRegularFile
               path (make-array java.nio.file.LinkOption 0))
      (p15-s23-b4-wasm-fail!
       "B4-TARGET" source-path {}
       {:missing-fact :pinned-node-executable}))
    (let [actual (.toRealPath path (make-array java.nio.file.LinkOption 0))
          size (java.nio.file.Files/size actual)
          bytes (java.nio.file.Files/readAllBytes actual)
          hash (str "sha256:" (sha256-bytes-hex bytes))]
      (when-not (and (= size p15-s23-b4-wasm-node-byte-count)
                     (= hash p15-s23-b4-wasm-node-content-hash)
                     (= (.toString actual) p15-s23-b4-wasm-node-path))
        (p15-s23-b4-wasm-fail!
         "B4-TARGET" source-path {}
         {:missing-fact :pinned-node-identity
          :byte-count size :content-hash hash}))
      {:actual-path (.toString actual) :byte-count size
       :content-hash hash :version p15-s23-b4-wasm-node-version
       :architecture p15-s23-b4-wasm-node-architecture}))))
