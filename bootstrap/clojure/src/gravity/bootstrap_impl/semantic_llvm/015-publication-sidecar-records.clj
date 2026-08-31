(defn- p15-s23-b3-llvm-write-edn-file!
  [candidate workspace name record source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :write-private-publication-sidecar)
  (let [root (.normalize (.toAbsolutePath workspace))
        path (.normalize (.toAbsolutePath (.resolve workspace name)))
        canonical (c-backend-canonical-value record)
        bytes (.getBytes (str (pr-str canonical) "\n")
                         java.nio.charset.StandardCharsets/UTF_8)]
    (when-not (and (contains? #{"manifest.edn" "provenance.edn"
                                "conformance.edn"}
                              name)
                   (= root (.getParent path))
                   (not (java.nio.file.Files/exists
                         path (make-array java.nio.file.LinkOption 0))))
      (p15-s23-b3-llvm-fail!
       "B3-MANIFEST" source-path {}
       {:missing-fact :closed-contained-sidecar-logical-path}))
    (java.nio.file.Files/write
     path bytes
     (into-array java.nio.file.OpenOption
                 [java.nio.file.StandardOpenOption/CREATE_NEW
                  java.nio.file.StandardOpenOption/WRITE]))
    (let [observed
          (p15-s23-b3-llvm-file-snapshot!
           candidate workspace path source-path
           :verify-private-publication-sidecar
           p15-s23-b3-llvm-max-emitted-file-bytes)]
      (when-not (java.util.Arrays/equals
                 ^bytes bytes ^bytes (:bytes observed))
        (p15-s23-b3-llvm-fail!
         "B13-HASH" source-path {}
         {:missing-fact :published-sidecar-roundtrip}))
      (assoc (p15-s23-b3-llvm-snapshot-content observed)
             :logical-path name))))

(defn- p15-s23-b3-llvm-provenance-sidecar-record
  [artifact]
  {:artifact :gravity/b13-final-bounded-llvm-provenance
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :semantic-id (:semantic-id artifact)
   :c13-c14-b1-packet
   (p15-s23-c13-c14-b1-sidecar-evidence! artifact)
   :contextual-evidence-ids
   (get-in artifact [:c18-record :evidence-ids])
   :c14-request (:c14-request artifact)
   :b1-packet (:b1-packet artifact)
   :b3-record (:b3-record artifact)
   :c18-record (:c18-record artifact)})

(defn- p15-s23-b3-llvm-conformance-sidecar-record
  [artifact]
  {:artifact :gravity/b14-final-bounded-llvm-conformance
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :b14-record (:b14-record artifact)})

(defn- p15-s23-b3-llvm-manifest-sidecar-record
  [artifact provenance-hash conformance-hash]
  {:artifact :gravity/b13-final-bounded-llvm-manifest
   :schema-version 1
   :final-artifact-id (:artifact-id artifact)
   :semantic-id (:semantic-id artifact)
   :b13-record (:b13-record artifact)
   :core-artifacts
   (select-keys (get-in artifact [:b13-record :artifact-files])
                [:llvm-ir :object :executable])
   :sidecars {:provenance provenance-hash
              :conformance conformance-hash}
   :graph
   [{:from "program.ll" :to "program.o" :edge :codegen}
    {:from "program.o" :to "program" :edge :link}
    {:from "provenance.edn" :to "manifest.edn" :edge :hash-bound}
    {:from "conformance.edn" :to "manifest.edn" :edge :hash-bound}]})
