

(defn compile-source-from-records
  [source-path source-text records]
  (let [macro-artifact (macro-source-artifact-from-records
                        source-path source-text records)
        module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
        _ (executable-profile! source-path module (:forms module))
        _ (validate-module-effects! module)]
    {:kind :gravity/stage0-hosted-artifact
     :compiler {:owner :clojure-bootstrap
                :stage :stage0
                :retirement-objective :replace-with-gravity-self-hosted-compiler}
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities])
     :syntax-object-stream (:expanded-syntax-object-stream macro-artifact)
     :macro-expansion-trace (:macro-expansion-trace macro-artifact)
     :hosted-execution {:entrypoint 'main
                        :runtime :clojure/jvm
                        :effects (:effects module)
                        :capabilities (:capabilities module)}}))

(defn compile-source
  [source-path source-text]
  (compile-source-from-records
   source-path source-text
   (read-source-form-records source-path source-text)))

(defn source-path-policy-fail!
  ([source-path]
   (let [source-file (java.io.File. source-path)
         bytes (when (.isFile source-file)
                 (java.nio.file.Files/readAllBytes (.toPath source-file)))]
     (source-path-policy-fail! source-path bytes)))
  ([source-path bytes]
   (let [bytes-hash (when bytes
                      (str "sha256:" (sha256-bytes-hex bytes)))
         span {:source source-path :byte-start 0 :byte-end 0}]
     (fail! "L1-SOURCE-EXTENSION"
            "Gravity source path does not use a co-canonical source extension"
            (cond->
             {:severity :error
              :stage :read-source
              :source-span span
              :primary (cond-> {:span span}
                         bytes-hash (assoc :artifact bytes-hash))
              :related []
              :origin-chain
              [(cond-> {:kind :source-path :path source-path}
                 bytes-hash (assoc :source-id bytes-hash))]
              :profile nil
              :target nil
              :facts
              (cond->
               {:actual-extension (gravity-source-extension source-path)
                :allowed-extensions
                (vec (sort co-canonical-source-extensions))}
                bytes-hash (assoc :bytes-hash bytes-hash
                                  :byte-count (alength bytes)))
              :reader-state {:stage :source-unit-policy}
              :remediation
              "Rename the source to .qst or .gravity; both are canonical and first-class."}
              bytes-hash (assoc :source-id bytes-hash))))))

(defn decode-gravity-source-bytes
  "Decode one already-captured source snapshot without reopening its path."
  [path bytes]
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
            bytes-hash (str "sha256:" (sha256-bytes-hex bytes))]
        (fail! "L1-SOURCE-ENCODING"
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

(defn read-gravity-source-text
  [path]
  (when-not (qst-or-gravity-source? path)
    (source-path-policy-fail! path))
  (decode-gravity-source-bytes
   path
   (java.nio.file.Files/readAllBytes
    (.toPath (java.io.File. path)))))

(defn compile-file
  [path]
  (compile-source path (read-gravity-source-text path)))

(defn read-file-artifact
  [path]
  (read-source-artifact path (read-gravity-source-text path)))

(defn module-file-artifact
  [path]
  (module-source-artifact path (slurp path)))

(defn core-file-artifact
  [path]
  (core-source-artifact path (slurp path)))