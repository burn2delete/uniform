

(defn jvm-backend-stored-jar-entry!
  [jar-output entry-name bytes]
  (let [crc (java.util.zip.CRC32.)
        entry (java.util.jar.JarEntry. entry-name)]
    (.update crc bytes)
    (.setTime entry 0)
    (.setMethod entry java.util.zip.ZipEntry/STORED)
    (.setSize entry (alength bytes))
    (.setCompressedSize entry (alength bytes))
    (.setCrc entry (.getValue crc))
    (.putNextEntry jar-output entry)
    (.write jar-output bytes 0 (alength bytes))
    (.closeEntry jar-output)))

(defn jvm-backend-write-deterministic-jar!
  [jar-path class-directory]
  (let [manifest-bytes
        (.getBytes
         (str "Manifest-Version: 1.0\r\n"
              "Main-Class: " jvm-backend-main-class "\r\n\r\n")
         java.nio.charset.StandardCharsets/UTF_8)
        entries
        [["META-INF/MANIFEST.MF" manifest-bytes]
         ["gravity/stage2/Program.class"
          (java.nio.file.Files/readAllBytes
           (.resolve class-directory "gravity/stage2/Program.class"))]
         ["module-info.class"
          (java.nio.file.Files/readAllBytes
           (.resolve class-directory "module-info.class"))]]]
    (with-open [output
                (java.util.jar.JarOutputStream.
                 (java.nio.file.Files/newOutputStream
                  jar-path
                  (into-array java.nio.file.OpenOption
                              [java.nio.file.StandardOpenOption/CREATE_NEW
                               java.nio.file.StandardOpenOption/WRITE])))]
      (doseq [[entry-name bytes] entries]
        (jvm-backend-stored-jar-entry! output entry-name bytes)))))

(defn jvm-backend-classfile-major
  [path]
  (let [bytes (java.nio.file.Files/readAllBytes path)]
    (when (and (>= (alength bytes) 8)
               (= [202 254 186 190]
                  (mapv #(bit-and (int (aget bytes %)) 0xff) (range 4))))
      (+ (bit-shift-left (bit-and (int (aget bytes 6)) 0xff) 8)
         (bit-and (int (aget bytes 7)) 0xff)))))

(defn jvm-backend-jar-record
  [jar-path]
  (with-open [jar (java.util.jar.JarFile. (.toFile jar-path))]
    (let [module-entry (.getJarEntry jar "module-info.class")
          module-name
          (when module-entry
            (with-open [input (.getInputStream jar module-entry)]
              (.name (java.lang.module.ModuleDescriptor/read input))))]
      {:entries (->> (enumeration-seq (.entries jar))
                     (map #(.getName ^java.util.jar.JarEntry %))
                     sort vec)
       :main-class (some-> jar .getManifest .getMainAttributes
                           (.getValue
                            java.util.jar.Attributes$Name/MAIN_CLASS))
       :module-name module-name})))

(defn jvm-backend-validate-content-hashes!
  [source-path manifest paths]
  (doseq [[kind expected] (:content-hashes manifest)]
    (let [path (get paths kind)
          actual
          (when (and path (.isFile (java.io.File. path)))
            (str "sha256:"
                 (sha256-bytes-hex
                  (java.nio.file.Files/readAllBytes
                   (.toPath (java.io.File. path))))))]
      (when-not (= expected actual)
        (jvm-backend-fail!
         "B13-HASH" "JVM emitted artifact content hash is stale or mismatched"
         source-path nil
         {:artifact-kind kind :artifact-path path
          :expected-content-hash expected :actual-content-hash actual
          :missing-fact :content-hash-integrity}))))
  :passed)