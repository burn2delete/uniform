

(defn- sh06-resolution-finalize-candidate
  ([source-path artifact-with-envelope]
   (sh06-resolution-finalize-candidate
    source-path artifact-with-envelope ::legacy))
  ([source-path artifact-with-envelope context]
  (let [proof
        (if (= ::legacy context)
          (sh06-resolution-capability-based-proof-for-construction
           artifact-with-envelope)
          (do
            (when (nil? context)
              (sh06-resolution-c5-fail! source-path :missing-context {}))
            (sh06-resolution-capability-based-proof-for-construction
             artifact-with-envelope context)))
        artifact
        (assoc artifact-with-envelope :capability-based-proof proof)]
    ;; The construction report above already replayed the exact authenticated
    ;; request and every stored run product.  The only post-construction change
    ;; is installing that report as the embedded proof, so a second identical
    ;; Gravity replay here would add cost without checking a new semantic fact.
    (when-not (and (= :complete (:status proof))
                   (empty? (:failed-checks proof))
                   (= proof (:capability-based-proof artifact)))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path
       :final-authenticated-resolution-artifact
       (cond-> {:failed-checks (:failed-checks proof)}
         (:preflight-observations proof)
         (assoc :preflight-observations
                (:preflight-observations proof))
         (:carrier-validation proof)
         (assoc :carrier-validation
                (:carrier-validation proof)))
       {}))
    artifact)))

(defn sh06-resolution-source-artifact
  [source-path source-text]
  (with-sh06-resolution-transport-bounds
   (sh06-resolution-finalize-candidate
    source-path
    (sh06-resolution-source-artifact-candidate
     source-path
     (sh05-macro-source-artifact source-path source-text)
     true))))

(defn sh06-resolution-artifact-from-sh05-artifact
  "Resolve an already authenticated SH-05 artifact without rebuilding C3/SH-05.

  The one-argument form derives the source path from the authenticated parent;
  callers with a path-scoped snapshot should use the two-argument form so the
  parent provenance is joined to that exact path.
  "
  ([sh05-artifact]
   (sh06-resolution-artifact-from-sh05-artifact
    (get-in sh05-artifact [:provenance :source-path]) sh05-artifact))
  ([source-path sh05-artifact]
   (with-sh06-resolution-transport-bounds
    (sh06-resolution-finalize-candidate
     source-path
     (sh06-resolution-source-artifact-candidate
      source-path sh05-artifact false))))
  ([source-path sh05-artifact context]
   (when (nil? context)
     (sh06-resolution-c5-fail! source-path :missing-context {}))
   (with-sh06-resolution-transport-bounds
    (sh06-resolution-finalize-candidate
     source-path
     (sh06-resolution-source-artifact-candidate
      source-path sh05-artifact false context)
     context))))

(defn sh06-resolution-file-artifact
  [source-path]
  (let [c2-artifact (compiler-c2-reader-file-artifact source-path)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)]
    (sh06-resolution-source-artifact source-path source-text)))

(defn sh06-resolution-utf8-byte-count-up-to
  [^String text maximum]
  (let [length (.length text)]
    (loop [index 0 byte-count 0]
      (if (or (>= index length) (> byte-count maximum))
        byte-count
        (let [code (int (.charAt text index))]
          (cond
            (<= code 0x7f)
            (recur (inc index) (inc byte-count))

            (<= code 0x7ff)
            (recur (inc index) (+ byte-count 2))

            (and (<= 0xd800 code 0xdbff)
                 (< (inc index) length)
                 (let [low (int (.charAt text (inc index)))]
                   (<= 0xdc00 low 0xdfff)))
            (recur (+ index 2) (+ byte-count 4))

            :else
            (recur (inc index) (+ byte-count 3))))))))

(defn sh06-resolution-bounded-pr-str
  [source-path artifact]
  (let [maximum (:maximum-serialized-bytes
                 sh06-resolution-artifact-bounds)
        builder (StringBuilder.)
        byte-count (volatile! 0)
        pending-high? (volatile! false)
        char-array-class (Class/forName "[C")]
    (letfn [(add-bytes! [amount]
              (let [observed (vswap! byte-count + amount)]
                (when (> observed maximum)
                  (sh06-resolution-boundary-fail!
                   "C5-UNRESOLVED" source-path
                   :maximum-resolution-serialization-bytes
                   {:observed-serialized-bytes observed
                    :maximum-serialized-bytes maximum}
                   {:aggregate-artifact-bounds
                    sh06-resolution-artifact-bounds}))))
            (consume-code! [code]
              (if @pending-high?
                (if (<= 0xdc00 code 0xdfff)
                  (do (vreset! pending-high? false)
                      (add-bytes! 4))
                  (do (vreset! pending-high? false)
                      (add-bytes! 3)
                      (consume-code! code)))
                (cond
                  (<= code 0x7f) (add-bytes! 1)
                  (<= code 0x7ff) (add-bytes! 2)
                  (<= 0xd800 code 0xdbff) (vreset! pending-high? true)
                  :else (add-bytes! 3))))
            (append-range! [value offset length]
              (dotimes [ordinal length]
                (let [index (+ offset ordinal)
                      code
                      (if (string? value)
                        (int (.charAt ^String value index))
                        (int (aget ^chars value index)))]
                  (consume-code! code)))
              (if (string? value)
                (.append builder ^String value offset (+ offset length))
                (.append builder ^chars value offset length)))]
      (let [writer
            (proxy [java.io.Writer] []
              (write
                ([value]
                 (cond
                   (integer? value)
                   (let [character (char value)]
                     (consume-code! (int character))
                     (.append builder character))

                   (string? value)
                   (append-range! value 0 (.length ^String value))

                   (instance? char-array-class value)
                   (append-range! value 0 (alength ^chars value))

                   :else
                   (throw
                    (IllegalArgumentException.
                     (str "unsupported writer value " (class value))))))
                ([value offset length]
                 (append-range! value offset length)))
              (flush [])
              (close []))]
        (binding [*out* writer]
          (pr artifact)
          (.flush writer))
        (when @pending-high?
          (vreset! pending-high? false)
          (add-bytes! 3))
        (str builder)))))

(defn sh06-resolution-serialize
  [artifact]
  (let [report (sh06-resolution-artifact-verification artifact)]
    (when-not (= :passed (:status report))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED"
       (or (get-in artifact [:provenance :source-path])
           "<sh06-resolution-serialize>")
       :verified-resolution-artifact-before-serialization
       (:failed-checks report) {}))
    (let [source-path
          (or (get-in artifact [:provenance :source-path])
              "<sh06-resolution-serialize>")]
      (try
        (sh06-resolution-bounded-pr-str source-path artifact)
        (catch InterruptedException interrupted
          (.interrupt (Thread/currentThread))
          (throw interrupted))
        (catch StackOverflowError error
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" source-path
           :contained-resolution-serialization-resource
           {:contained-host-error (.getName (class error))}
           {:aggregate-artifact-bounds sh06-resolution-artifact-bounds}))
        (catch OutOfMemoryError error
          (sh06-resolution-boundary-fail!
           "C5-UNRESOLVED" source-path
           :contained-resolution-serialization-resource
           {:contained-host-error (.getName (class error))}
           {:aggregate-artifact-bounds
            sh06-resolution-artifact-bounds}))))))
