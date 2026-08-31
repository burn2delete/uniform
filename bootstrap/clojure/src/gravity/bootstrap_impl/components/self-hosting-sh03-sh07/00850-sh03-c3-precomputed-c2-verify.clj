

(defn- sh03-c3-precomputed-c2-verify!
  [candidate source-path c2-artifact]
  (let [boundary (:gravity-reader-boundary c2-artifact)
        envelope (:authenticated-envelope boundary)
        envelope-descriptor
        (:authenticated-envelope-descriptor boundary)
        expected-binding
        (dissoc (sh03-reader-current-binding! source-path) :plan)]
    (when-not
     (and (identical? candidate sh03-reader-internal-product-authority)
          (= :gravity/stage0-c2-reader-document-artifact
             (:kind c2-artifact))
          (= :SH-03 (:slice boundary))
          (= :gravity-source (:owner boundary))
          (= :gravity/sh03-to-c2-reader-products-v2
             (:adapter-contract boundary))
          (= expected-binding (:plan-binding boundary))
          (= p15-s23-sh02-stage-envelope-keys (set (keys envelope)))
          (= :accepted (:status envelope))
          (= :c2-reader (:stage envelope))
          (= :gravity/sh03-reader-products
             (get-in envelope [:sealed-artifact :artifact-kind]))
          (map? envelope-descriptor)
          (= :passed
             (p15-s23-stage2-sh02-descriptor-envelope-verify!
              envelope :c2-reader :gravity/sh03-reader-products
              envelope-descriptor source-path)))
      (c2-reader-fail!
       "C2-HASH" source-path
       {:stage :read-source
        :source-span (source-span source-path 0)
        :reader-options standard-reader-options}
       {:missing-fields [:authenticated-sh03-c3-precomputed-c2]}))
    c2-artifact))

;; SH-04 executes the pinned Gravity syntax leaf and adapts only its verified
;; products into the compatibility-rich C3 artifact below.  Clojure remains the
;; declared plan runner, digest resolver, envelope binder, and provenance host.
(def sh04-syntax-source-relative-path
  "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity")
(def sh04-syntax-facade-relative-path
  "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity")
(def sh04-syntax-expected-source-byte-count 81241)
(def sh04-syntax-expected-source-content-hash
  "sha256:afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553")
(def sh04-syntax-expected-plan-semantic-hash
  "sha256:be302e17f542733b5e0078f451b11ea6b8efa43b9761fb6c4035f9bb527083db")
(def sh04-syntax-expected-functions-semantic-hash
  "sha256:d78a2353d0f93d5b0d5438c8fdca4b18202627bb63290522e8aea29df24c0f9b")
(def sh04-syntax-expected-function-count 102)
(def sh04-syntax-expected-function-names-hash
  "sha256:890db57cf9eaf955afd12bf69f2f68f6be637c6c21abde8b0ac726a4fab5ee4a")
(def sh04-syntax-expected-function-shapes-hash
  "sha256:33d929948fd0803b6b5c48eeed9ee5c1de6c52f40b81e96693a4db8685fb0b4a")
(def sh04-syntax-public-function-hashes
   {'c3-syntax-build-template
   "sha256:357fbc856a887303d79f89630c2a753a060e20e82c2cd2cf0c99f193d1cae1c2"
   'c3-generated-syntax-template
   "sha256:49d6cd154963531ec8e3e5b3b96d054ea2ed4dd01f26cd567e03ad303f2d467b"
   'c3-syntax-verify-template
   "sha256:9da610b70369c51fc0417f1d0eaf022e5c17dd66d0e77908ec47455bb620e0e6"
   'c3-syntax-verify-resolved
   "sha256:9c636df1fd6be6bb288794aeda88c18b52ad6ecf1f67b93bea2a9deea11def75"
   'c3-syntax-stream-build-template
   "sha256:6d8d7c7cbc6332c9593dd44a23fcf635f51bfd38beedcacf75ca4383c5e854b8"
   'c3-syntax-stream-verify-resolved
   "sha256:012f527a03a4e89c55a81399d4ac6f4ec581f3c192d57d860913f89ee416a249"
   'c3-syntax-serialize-template
   "sha256:f7dd9496917431b9bb2632f10f65beaa8b37577cff65951f4bb03a8de1162ae1"
   'c3-syntax-deserialize-template
   "sha256:37c6865345d142ba9b357ca086ce193ee89d72c5a2e3a3765486268bbfb7a784"
   'c3-syntax-graph-verify-template
   "sha256:d20f7492852b225dbe2c8f50e1a2d456a5596828b42368d5260aa2985b2f2650"})
(def sh04-syntax-public-function-shapes
  {'c3-syntax-build-template {:arity 1 :params ['descriptor]}
   'c3-generated-syntax-template
   {:arity 12
    :params ['base-syntax-id 'form 'generated-span 'producer
             'generation-reason 'namespace-context 'profile 'metadata
             'hygiene 'facts 'reader-binding
             'reader-source-revision]}
   'c3-syntax-verify-template
   {:arity 2 :params ['syntax 'digest-requests]}
   'c3-syntax-verify-resolved
   {:arity 5
    :params ['syntax 'digest-requests 'resolved-digests
             'reader-binding 'reader-source-revision]}
   'c3-syntax-stream-build-template
   {:arity 4
    :params ['resolved-products 'reader-binding
             'reader-source-revision 'root-syntax-ids]}
   'c3-syntax-stream-verify-resolved
   {:arity 3
    :params ['resolved-stream 'digest-requests 'resolved-digests]}
   'c3-syntax-serialize-template
   {:arity 3
    :params ['resolved-stream 'digest-requests 'resolved-digests]}
   'c3-syntax-deserialize-template
   {:arity 1 :params ['carrier]}
   'c3-syntax-graph-verify-template
   {:arity 1 :params ['syntax-objects]}})

(def sh04-syntax-adapter-contract
  :gravity/sh04-to-c3-syntax-products-v1)
(def sh04-syntax-sealed-artifact-kind
  :gravity/sh04-syntax-products)
(def sh04-syntax-envelope-stage :c3-syntax)

(defn sh04-syntax-boundary-fail!
  [rule source-path missing-field observed facts]
  (c3-syntax-fail!
   rule source-path
   {:source-span (source-span source-path 0)
    :producer :gravity.bootstrap.syntax
    :form-kind :syntax-object}
   {:missing-fields [missing-field]
    :facts (merge {:sh04-boundary :gravity-syntax-plan} facts)
    :observed observed}))

(defn sh04-syntax-resolve-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate
               (java.io.File. directory sh04-syntax-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh04-syntax-source-relative-path)))

(defn sh04-syntax-read-pinned-source!
  [request-source]
  (let [source-path (sh04-syntax-resolve-source-path)
        nio-path (.toPath (java.io.File. source-path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           nio-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (sh04-syntax-boundary-fail!
             "C3-ID" request-source :pinned-syntax-source-readable
             source-path {:cause-message (.getMessage error)})))
        _ (when-not (and attributes
                         (.isRegularFile attributes)
                         (= (long sh04-syntax-expected-source-byte-count)
                            (.size attributes)))
            (sh04-syntax-boundary-fail!
             "C3-ID" request-source :exact-pinned-syntax-source
             source-path
             {:expected-byte-count sh04-syntax-expected-source-byte-count
              :observed-byte-count (when attributes (.size attributes))}))
        bytes
        (try
          (java.nio.file.Files/readAllBytes nio-path)
          (catch Exception error
            (sh04-syntax-boundary-fail!
             "C3-ID" request-source :pinned-syntax-source-bytes
             source-path {:cause-message (.getMessage error)})))
        content-hash (str "sha256:" (sha256-bytes-hex bytes))]
    (when-not (and (= sh04-syntax-expected-source-byte-count
                      (alength bytes))
                   (= sh04-syntax-expected-source-content-hash content-hash))
      (sh04-syntax-boundary-fail!
       "C3-ID" request-source :pinned-syntax-source-identity
       source-path {:observed-content-hash content-hash
                    :observed-byte-count (alength bytes)}))
    {:source-path source-path
     :source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
     :source-byte-count (alength bytes)
     :source-content-hash content-hash}))

(defn sh04-syntax-plan-identities
  [plan]
  (let [functions (:functions plan)
        shapes
        (into (sorted-map)
              (map (fn [[name function]]
                     [name (select-keys function [:arity :params])]))
              functions)]
    {:plan-semantic-hash
     (p15-s23-c11-mir-digest
      (p15-s23-stage2-compiler-artifact-semantic-input plan))
     :functions-semantic-hash (p15-s23-c11-mir-digest functions)
     :function-count (count functions)
     :function-names-hash
     (p15-s23-c11-mir-digest (vec (keys functions)))
     :function-shapes-hash (p15-s23-c11-mir-digest shapes)
     :public-function-hashes
     (into (sorted-map)
           (map (fn [name]
                  [name (p15-s23-c11-mir-digest (get functions name))]))
           (keys sh04-syntax-public-function-hashes))
     :public-function-shapes
     (select-keys shapes (keys sh04-syntax-public-function-shapes))}))