

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
   (gravity.reader/reject-source-path! #'gravity-source-extension #'co-canonical-source-extensions
                                      #'sha256-bytes-hex #'fail! source-path bytes)))

(defn decode-gravity-source-bytes
  "Decode one already-captured source snapshot without reopening its path."
  [path bytes]
  (gravity.reader/decode-source-bytes #'sha256-bytes-hex #'fail! path bytes))

(defn read-gravity-source-text
  [path]
  (gravity.reader/read-source-text #'qst-or-gravity-source? #'source-path-policy-fail!
                                   #'decode-gravity-source-bytes path))

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