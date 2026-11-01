(ns gravity.reader
  "Native source intake, strict UTF-8 decoding and stage0 reader artifact assembly.

  This namespace owns native intake through an explicit byte owner, strict decoding and the existing
  artifact envelope and pass record.
  Path policy, parsing, syntax, source identity and diagnostic construction remain with their
  supplied owners. Operations are trusted seed dependencies, not evidence of
  canonical reader authentication or self-hosting.")

(def ^:private namespace-contract
  {:namespace 'gravity.reader
   :governing-contracts [:D1 :D2 :D3 :D8 :D9 :L1 :C1 :C2 :C3 :C15 :BOOT1 :BOOT8 :TEST2]
   :contract-boundary :native-source-intake-and-stage0-reader-artifact-assembly
   :public-api
   {'read-source-artifact
    {:arglists '([source-path source-text project-context operations])
     :returns :gravity/stage0-reader-artifact}
    'decode-source-bytes
    {:arglists '([sha256-bytes-hex-var fail-var path bytes])
     :returns :decoded-utf8-text :admission :none :opens-source? false
     :operation-inputs [:sha256-bytes-hex-var :fail-var]}
    'read-source-text
    {:arglists '([qst-or-gravity-source-var source-path-policy-fail-var decode-source-bytes-var read-target-source-bytes-var path])
     :returns :decoded-utf8-text :admission :none :opens-source? true
     :grants-file-authority? false :direct-file-access? false
     :source-byte-capture :explicit-existing-owner-var
     :operation-inputs [:qst-or-gravity-source-var :source-path-policy-fail-var :decode-source-bytes-var :read-target-source-bytes-var]}}
   :artifact-inputs [:source-path :decoded-source-text :explicit-project-context
                     :seed-reader-operations :standard-reader-options
                     :captured-source-bytes :raw-byte-hash-var :diagnostic-var
                     :extension-predicate-var :path-policy-refusal-var :decoder-var :source-byte-reader-var]
   :artifact-outputs [:gravity/stage0-reader-artifact :decoded-utf8-text
                      :unchanged-source-encoding-failure :unchanged-source-extension-failure
                      :unchanged-source-byte-owner-failure]
   :preserves [:source-spans :metadata :reader-origin :profile-context
               :source-unit-identity :namespace-clause-syntax]
   :invalidates []
   :diagnostic-ownership
   {:owns ["L1-SOURCE-ENCODING"]
    :construction :explicit-existing-owner-var
    :delegates ["L1-SOURCE-EXTENSION" "C2-ENCODING"]
    :propagates [:L1 :C2]
    :does-not-own [:token-and-form-policy :namespace-shape-policy :c15-schema
                   :diagnostic-identity :redaction :rendering]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :operation-inputs
   [:read-source-form-records :validate-ns-syntax! :reader-module-context
    :syntax-object-stream :l1-source-unit-artifacts :standard-reader-options
    :gravity-source-extension :gravity-source-kind :utf8-byte-count]
   :unsupported [:other-encodings :replacement-decoding :file-authority
                 :operation-authentication :complete-reader-conformance
                 :migration :release :seed-retirement]
   :test-owner 'gravity.reader.artifact-test
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-reader? false
   :authenticated-reader? false
   :self-hosted? false})

(defn read-source-artifact
  "Assemble the existing stage0 artifact using explicit owning operations.

  Operations retain the bootstrap's interposition and rejection behavior.
  This is a seed composition API; supplied operations confer no authority."
  [source-path source-text project-context
   {:keys [read-source-form-records validate-ns-syntax! reader-module-context
           syntax-object-stream l1-source-unit-artifacts standard-reader-options
           gravity-source-extension gravity-source-kind utf8-byte-count]}]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        context (reader-module-context forms)
        syntax (syntax-object-stream source-path records context)
        reader-details (l1-source-unit-artifacts source-path source-text
                                                 standard-reader-options
                                                 project-context)]
    (merge
     {:kind :gravity/stage0-reader-artifact
      :pass {:name :reader
             :input :source-bytes
             :output :syntax-object-stream
             :preserves [:source-spans :metadata :reader-origin
                         :profile-context :source-unit-identity]
             :rejects ["L1-DELIMITER" "L1-STRING" "L1-NUMERIC"
                       "L1-IDENTIFIER" "L1-MAP-ARITY"
                       "L1-METADATA" "L1-NS-SHAPE"
                       "L1-READER-EXTENSION" "L1-SOURCE-ENCODING"
                       "L1-SOURCE-EXTENSION"]}
      :source {:path source-path
               :extension (gravity-source-extension source-path)
               :encoding :utf-8
               :source-kind (gravity-source-kind source-path)
               :byte-count (utf8-byte-count source-text)
               :form-count (count records)}
      :module-context (dissoc context :namespace-clause-syntax)
      :syntax-object-stream syntax
      :namespace-clause-syntax (:namespace-clause-syntax context)
      :diagnostics []}
     reader-details)))

(defn decode-source-bytes
  "Decode one already-captured source snapshot without reopening its path."
  [sha256-bytes-hex-var fail-var path bytes]
  (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                  (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                  (.onUnmappableCharacter
                   java.nio.charset.CodingErrorAction/REPORT))
        input (java.nio.ByteBuffer/wrap bytes)
        output (java.nio.CharBuffer/allocate (max 1 (alength bytes)))
        result (.decode decoder input output true)]
    (when (.isError result)
      (let [start (.position input)
            end (+ start (max 1 (.length result)))
            bytes-hash (str "sha256:" ((deref sha256-bytes-hex-var) bytes))]
        ((deref fail-var) "L1-SOURCE-ENCODING"
               "source bytes cannot be decoded as UTF-8"
               {:severity :error
                :stage :read-source
                :source-id bytes-hash
                :source-span {:source path
                              :byte-start start
                              :byte-end end}
                :primary {:span {:source path
                                 :byte-start start
                                 :byte-end end}
                          :artifact bytes-hash}
                :related []
                :origin-chain [{:kind :source-bytes :path path}]
                :profile nil
                :target nil
                :facts {:declared-encoding :utf-8
                        :bytes-hash bytes-hash
                        :malformed-input-length (.length result)}
                :reader-state {:stage :source-decoding
                               :encoding :utf-8}
                :remediation "Save the source as valid UTF-8 without replacement decoding."})))
    (let [flush-result (.flush decoder output)]
      (when (.isError flush-result)
        (.throwException flush-result)))
    (.flip output)
    (.toString output)))

(defn read-source-text
  [qst-or-gravity-source-var source-path-policy-fail-var decode-source-bytes-var read-target-source-bytes-var path]
  (when-not ((deref qst-or-gravity-source-var) path)
    ((deref source-path-policy-fail-var) path))
  ((deref decode-source-bytes-var)
   path
   ((deref read-target-source-bytes-var) path)))

(doseq [operation [#'read-source-artifact #'decode-source-bytes #'read-source-text]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
