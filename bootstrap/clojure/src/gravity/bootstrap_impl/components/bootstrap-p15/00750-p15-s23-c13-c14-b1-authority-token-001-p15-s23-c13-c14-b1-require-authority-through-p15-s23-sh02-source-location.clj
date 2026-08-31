(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-c13-c14-b1-require-authority!
  [candidate source-path operation]
  (when-not (identical? candidate p15-s23-c13-c14-b1-authority-token)
    (p15-s23-b3-llvm-fail!
     "B1-INPUT" source-path {}
     {:missing-fact :opaque-c13-c14-b1-construction-authority
      :bounded-reason operation})))

(defn- p15-s23-c13-c14-b1-resolve-source-path
  [candidate request-source relative]
  (p15-s23-c13-c14-b1-require-authority!
   candidate request-source :resolve-pinned-bridge-source)
  (let [c11-path (java.io.File. (p15-s23-c11-mir-resolve-source-path))
        root (loop [directory (.getParentFile c11-path)]
               (if (or (nil? directory)
                       (.isFile (java.io.File. directory relative)))
                 directory
                 (recur (.getParentFile directory))))]
    (if root
      (.getCanonicalPath (java.io.File. root relative))
      relative)))

(defn- p15-s23-c13-c14-b1-read-pinned-bytes!
  [candidate request-source source-file expected-byte-count]
  (p15-s23-c13-c14-b1-require-authority!
   candidate request-source :read-pinned-bridge-source)
  (let [path (.toPath source-file)
        nofollow
        (into-array java.nio.file.LinkOption
                    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (java.nio.file.Files/readAttributes
         path java.nio.file.attribute.BasicFileAttributes nofollow)]
    (when-not (and (.isRegularFile attributes)
                   (= (long expected-byte-count) (.size attributes)))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :exact-regular-pinned-gravity-bridge-source
        :expected-source-bytes expected-byte-count
        :observed-source-bytes (.size attributes)
        :regular-file? (.isRegularFile attributes)}))
    (let [limit (inc expected-byte-count)
          buffer (byte-array limit)
          observed
          (with-open
           [input
            (java.nio.file.Files/newInputStream
             path
             (into-array java.nio.file.OpenOption
                         [java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
            (loop [offset 0]
              (if (= offset limit)
                offset
                (let [read-count (.read input buffer offset (- limit offset))]
                  (if (= read-count -1)
                    offset
                    (recur (+ offset read-count)))))))]
      (when-not (= expected-byte-count observed)
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" request-source {}
         {:missing-fact :stable-exact-pinned-gravity-bridge-source-size
          :expected-source-bytes expected-byte-count
          :observed-source-bytes observed}))
      (java.util.Arrays/copyOf buffer expected-byte-count))))

(defn- p15-s23-c13-c14-b1-source-binding!
  [candidate request-source
   {:keys [owner relative-path source-byte-count source-content-hash
           plan-semantic-hash functions-semantic-hash
           builder-semantic-hash builder-function required-functions
           emitter-target]}]
  (p15-s23-c13-c14-b1-require-authority!
   candidate request-source :load-pinned-bridge-source)
  (let [source-path
        (p15-s23-c13-c14-b1-resolve-source-path
         candidate request-source relative-path)
        source-file (java.io.File. source-path)]
    (when-not (.isFile source-file)
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :pinned-gravity-bridge-source}))
    (let [bytes
          (p15-s23-c13-c14-b1-read-pinned-bytes!
           candidate request-source source-file source-byte-count)
          observed-byte-count (alength bytes)
          observed-content-hash
          (str "sha256:" (sha256-bytes-hex bytes))]
      (when-not (and (= source-byte-count observed-byte-count)
                     (= source-content-hash observed-content-hash))
        (p15-s23-b3-llvm-fail!
         "B1-INPUT" request-source {}
         {:missing-fact :pinned-gravity-bridge-source-identity
          :expected-source-bytes source-byte-count
          :observed-source-bytes observed-byte-count
          :expected-source-content-hash source-content-hash
          :observed-source-content-hash observed-content-hash}))
      (let [source-text
            (String. bytes java.nio.charset.StandardCharsets/UTF_8)
            emitter
            (:emitter
             (c-backend-stage2-plan-emitter-source-rule!
              request-source (or emitter-target :llvm-x86_64-linux)))
            plan
            (p15-s23-stage2-compiler-artifact-plan
             emitter source-path source-text)
            functions (:functions plan)
            shapes
            (into {}
                  (map (fn [[name _]]
                         [name (select-keys (get functions name)
                                            [:arity :params])]))
                  required-functions)
            observed-plan-hash
            (p15-s23-c11-mir-digest
             (p15-s23-stage2-compiler-artifact-semantic-input plan))
            observed-functions-hash
            (p15-s23-c11-mir-digest functions)
            observed-builder-hash
            (p15-s23-c11-mir-digest (get functions builder-function))]
        (when-not
         (and (= required-functions shapes)
              (= plan-semantic-hash observed-plan-hash)
              (= functions-semantic-hash observed-functions-hash)
              (= builder-semantic-hash observed-builder-hash))
          (p15-s23-b3-llvm-fail!
           "B1-INPUT" request-source {}
           {:missing-fact :pinned-gravity-bridge-function-identity
            :observed-source-content-hash observed-content-hash}))
        {:owner owner
         :source-path source-path
         :source-byte-count observed-byte-count
         :source-content-hash observed-content-hash
         :plan-semantic-hash observed-plan-hash
         :functions-semantic-hash observed-functions-hash
         :builder-semantic-hash observed-builder-hash
         :function-shapes shapes
         :plan plan}))))

(defn- p15-s23-c13-c14-b1-source-bindings!
  [candidate source-path]
  {:c13
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c13-mir-optimization
     :relative-path p15-s23-c13-source-relative-path
     :source-byte-count p15-s23-c13-source-byte-count
     :source-content-hash p15-s23-c13-expected-source-content-hash
     :plan-semantic-hash p15-s23-c13-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c13-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-c13-expected-builder-semantic-hash
     :builder-function p15-s23-c13-builder-function
     :required-functions p15-s23-c13-required-functions})
   :c14
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.compiler/c14-target-lowering
     :relative-path p15-s23-c14-source-relative-path
     :source-byte-count p15-s23-c14-source-byte-count
     :source-content-hash p15-s23-c14-expected-source-content-hash
     :plan-semantic-hash p15-s23-c14-expected-plan-semantic-hash
     :functions-semantic-hash
     p15-s23-c14-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-c14-expected-builder-semantic-hash
     :builder-function p15-s23-c14-builder-function
     :required-functions p15-s23-c14-required-functions})
   :b1
   (p15-s23-c13-c14-b1-source-binding!
    candidate source-path
    {:owner :gravity.backend/b1-backend-interface
     :relative-path p15-s23-b1-source-relative-path
     :source-byte-count p15-s23-b1-source-byte-count
     :source-content-hash p15-s23-b1-expected-source-content-hash
     :plan-semantic-hash p15-s23-b1-expected-plan-semantic-hash
     :functions-semantic-hash p15-s23-b1-expected-functions-semantic-hash
     :builder-semantic-hash p15-s23-b1-expected-builder-semantic-hash
     :builder-function p15-s23-b1-builder-function
     :required-functions p15-s23-b1-required-functions})})

(defn- p15-s23-sh02-source-location!
  [candidate request-source]
  (p15-s23-c13-c14-b1-require-authority!
   candidate request-source :resolve-pinned-sh02-source)
  (let [c11-source
        (.normalize
         (.toAbsolutePath
          (java.nio.file.Paths/get
           (p15-s23-c11-mir-resolve-source-path)
           (make-array String 0))))
        c11-relative
        (java.nio.file.Paths/get
         p15-s23-c11-mir-source-relative-path (make-array String 0))
        repository-root
        (loop [path c11-source
               remaining (.getNameCount c11-relative)]
          (if (zero? remaining)
            path
            (recur (.getParent path) (dec remaining))))
        sh02-relative
        (java.nio.file.Paths/get
         p15-s23-sh02-source-relative-path (make-array String 0))
        source-path (.normalize (.resolve repository-root sh02-relative))]
    (when-not (and (.endsWith c11-source c11-relative)
                   (.startsWith source-path repository-root))
      (p15-s23-b3-llvm-fail!
       "B1-INPUT" request-source {}
       {:missing-fact :bounded-pinned-sh02-source-location
        :source-path (str source-path)}))
    {:repository-root repository-root
     :source-path source-path})))
