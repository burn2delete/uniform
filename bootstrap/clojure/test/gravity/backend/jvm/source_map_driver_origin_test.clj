(ns gravity.backend.jvm.source-map-driver-origin-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.digest :as digest])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(deftest emitted-source-maps-record-the-compiler-route
  (let [source-text (str "(ns jvm.source.map (:profile :hosted) (:target :jvm) "
                         "(:effects #{:io/write}) (:capabilities #{:io/stdout}))\n"
                         "(defn main [] (println \"source map \u03bb\"))\n")
        directory (Files/createTempDirectory "gravity-jvm-source-map-"
                                             (make-array FileAttribute 0))]
    (try
      (doseq [[route development? expected-origins]
              [["development" true
                [:source-unit :c2-reader :stage2-source-front-end
                 :stage2-plan-emitter :jvm-lowering]]
               ["driver" false
                [:source-unit :c2-reader :stage2-source-front-end
                 :stage2-plan-emitter :stage2-compiler-driver :jvm-lowering]]]]
        (testing route
          (let [{:keys [emitted-files]}
                (bootstrap/jvm-backend-source-artifact
                 "source-map.gravity" source-text
                 {:output-path (str (.resolve directory route))
                  :emit? true
                  :current-runtime-development? development?})
                map-bytes (Files/readAllBytes (.toPath (io/file (:source-map emitted-files))))
                source-map (edn/read-string (String. map-bytes StandardCharsets/UTF_8))
                manifest (edn/read-string (slurp (:manifest emitted-files) :encoding "UTF-8"))
                provenance (edn/read-string (slurp (:provenance emitted-files) :encoding "UTF-8"))
                origins (:origin-chain source-map)
                driver-in-map? (boolean (some #{:stage2-compiler-driver} origins))]
            (is (= expected-origins origins))
            (is (= driver-in-map?
                   (boolean (some #{:stage2-compiler-driver} (:pass-history provenance)))))
            (when development?
              (is (false? (:stage2-compiler-driver-used? manifest)))
              (is (= (:stage2-compiler-driver-used? manifest) driver-in-map?))
              (is (= :development-unadmitted (:runtime-revision-status manifest))))
            (is (= (str "sha256:" (digest/sha256-bytes-hex map-bytes))
                   (get-in manifest [:content-hashes :source-map])))
            (is (= (str "sha256:" (digest/sha256-hex source-text))
                   (get-in source-map [:source :content-hash])))
            (is (= "sources/gravity/stage2/Program.java" (get-in source-map [:generated :file])))
            (is (= :source-unit-only (:coverage source-map)))
            (is (false? (:per-form-origin-preserved? source-map))))))
      (finally
        (doseq [file (reverse (file-seq (.toFile directory)))]
          (Files/deleteIfExists (.toPath file)))))))
