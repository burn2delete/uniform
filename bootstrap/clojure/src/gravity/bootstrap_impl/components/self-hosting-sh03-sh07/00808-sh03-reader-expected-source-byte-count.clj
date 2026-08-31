

;; SH-03 executes only this exact Gravity-authored reader closure.  The pinned
;; values are filled after the leaf source and its independent review freeze.
;; Keeping these separate from the builder makes the source/plan tripwire
;; explicit and prevents a rebuild from silently authorizing changed code.
(def sh03-reader-expected-source-byte-count 257161)
(def sh03-reader-uncredited-source-model-entrypoints
  ['stage1-read-source-formal-release-governance-seed-retirement
   'stage1-read-source-release-attestation-seed-retirement
   'stage1-read-source-diverse-bootstrap-verification
   'stage1-read-source-verified-boot-chain
   'stage1-read-source-runtime-image
   'stage1-read-source-runtime-entrypoint
   'stage1-read-source-compiler-driver
   'stage1-read-source-core-bootstrap
   'stage1-read-source-self-hosted-runtime])
(def sh03-reader-expected-source-content-hash
  "sha256:bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77")
(def sh03-reader-expected-plan-semantic-hash
  "sha256:25689f7c0bf9c1872aab4f3e41ac930efdf8c350231b14b2573e23a6d76fc475")
(def sh03-reader-expected-functions-semantic-hash
  "sha256:2e7bd1504df9cac0451d391f4f20cd5fac524e4f289289d136ac43f279f759cb")
(def sh03-reader-expected-function-count 231)
(def sh03-reader-expected-function-names-hash
  "sha256:27c0e98eec6e92acdf9d4c5a9db966374136f1c8084f7e9f13bea1610d6bf868")
(def sh03-reader-expected-function-shapes-hash
  "sha256:af3f655c92eb88945c6a46cdc8f0dcc157c92a8e7f9cd80cc2595129bd088e07")
(def sh03-reader-expected-entrypoint-semantic-hash
  "sha256:9ea6a927c2f25fa80b123af18d3a6824bd9e2863e8c55fbc9f02839d882eb0cc")
(def sh03-reader-expected-verifier-semantic-hash
  "sha256:625bbeccf94f75fedbd6232e4a54b1a44435f06bf07352b8d19b1a35196038a7")
(def sh03-reader-expected-builtin-functions-hash
  "sha256:03394c173b55bcb279070adc77d0494ee334c42505d814593f46109000dc1400")

(declare sh03-reader-canonical-maximum-scalar-bytes
         sh03-reader-canonical-maximum-total-scalar-bytes)

(def sh03-reader-source-relative-path
  "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
(def sh03-reader-entrypoint 'sh03-read-source-unit)
(def sh03-reader-verifier 'sh03-verify-reader-result)
(def sh03-reader-function-prefix "sh03-")
(def sh03-reader-plan-maximum-nodes 524288)
(def sh03-reader-plan-maximum-depth 512)
(def sh03-reader-plan-maximum-width 131072)
(def sh03-reader-result-maximum-nodes 167772160)
(def sh03-reader-result-maximum-depth 2048)
(def sh03-reader-result-maximum-width 1048576)
(def sh03-reader-input-maximum-identity-utf8-bytes 4096)
(def sh03-reader-input-maximum-identity-code-units 1024)

(def sh03-reader-plan-keys
  #{:kind :compiler-artifact-plan? :entrypoint :source :compiler :module
    :functions :instruction-summary :effect-summary :sh03-reader :plan-id})

(def sh03-reader-function-keys
  #{:name :params :arity :body-form-count :binding :instructions})

(def sh03-reader-binding-keys
  #{:name :kind :namespace :profile :target :visibility :effects
    :capabilities})

(def sh03-reader-instruction-keysets
  {:literal #{:op :value}
   :quote #{:op :value}
   :local #{:op :name}
   :vector-literal #{:op :items}
   :set-literal #{:op :items}
   :map-literal #{:op :entries}
   :do #{:op :body}
   :if #{:op :test :then :else}
   :let #{:op :bindings :body}
   :loop #{:op :bindings :binding-count :body}
   :recur #{:op :args}
   :builtin-call #{:op :function :args}
   :function-call #{:op :function :args}})

(def sh03-reader-allowed-opcodes
  (set (keys sh03-reader-instruction-keysets)))

(def sh03-reader-allowed-builtins
  (set/union stage0-builtin-functions
             p15-s23-stage2-compiler-artifact-builtins))

(defn sh03-reader-boundary-fail!
  [source-path missing-fact subject facts]
  (c2-reader-fail!
   "C2-HASH" source-path
   {:stage :read-source
    :source-span (source-span source-path 0)
    :reader-options standard-reader-options}
   {:missing-fields [missing-fact]
    :facts (merge {:sh03-boundary :gravity-reader-plan}
                  facts)
    :observed subject}))

(defn sh03-reader-resolve-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate (java.io.File. directory
                                        sh03-reader-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh03-reader-source-relative-path)))

(defn sh03-reader-read-pinned-source-bytes!
  [request-source]
  (let [source-path (sh03-reader-resolve-source-path)
        path (.toPath (java.io.File. source-path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (sh03-reader-boundary-fail!
             request-source :pinned-sh03-reader-source-readable
             source-path {:cause-message (.getMessage error)})))]
    (when-not (and attributes
                   (.isRegularFile attributes)
                   (= (long sh03-reader-expected-source-byte-count)
                      (.size attributes)))
      (sh03-reader-boundary-fail!
       request-source :exact-regular-pinned-sh03-reader-source
       source-path
       {:expected-source-byte-count sh03-reader-expected-source-byte-count
        :observed-source-byte-count (when attributes (.size attributes))
        :regular-file? (boolean (and attributes
                                     (.isRegularFile attributes)))}))
    (let [limit (inc sh03-reader-expected-source-byte-count)
          buffer (byte-array limit)
          observed
          (try
            (with-open [input
                        (java.nio.file.Files/newInputStream
                         path
                         (into-array java.nio.file.OpenOption
                                     [java.nio.file.LinkOption/NOFOLLOW_LINKS]))]
              (loop [offset 0]
                (if (= offset limit)
                  offset
                  (let [n (.read input buffer offset (- limit offset))]
                    (if (= -1 n) offset (recur (+ offset n)))))))
            (catch Exception error
              (sh03-reader-boundary-fail!
               request-source :stable-pinned-sh03-reader-source-read
               source-path {:cause-message (.getMessage error)})))
          bytes (java.util.Arrays/copyOf
                 buffer sh03-reader-expected-source-byte-count)
          content-hash (str "sha256:" (sha256-bytes-hex bytes))]
      (when-not (and (= sh03-reader-expected-source-byte-count observed)
                     (= sh03-reader-expected-source-content-hash content-hash))
        (sh03-reader-boundary-fail!
         request-source :pinned-sh03-reader-source-identity
         source-path
         {:expected-source-byte-count sh03-reader-expected-source-byte-count
          :observed-source-byte-count observed
          :expected-source-content-hash
          sh03-reader-expected-source-content-hash
          :observed-source-content-hash content-hash}))
      {:source-path source-path
       :bytes bytes
       :source-byte-count observed
       :source-content-hash content-hash})))

(defn sh03-reader-strict-source-text!
  [request-source source-path bytes]
  (try
    (str
     (.decode
      (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
        (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
        (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
      (java.nio.ByteBuffer/wrap bytes)))
    (catch java.nio.charset.CharacterCodingException error
      (sh03-reader-boundary-fail!
       request-source :valid-utf8-pinned-sh03-reader-source
       source-path {:cause-message (.getMessage error)}))))