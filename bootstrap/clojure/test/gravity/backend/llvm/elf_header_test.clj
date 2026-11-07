(ns gravity.backend.llvm.elf-header-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

; Current-source implementation feedback; no migration or release authority.
(def ^:private engine-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity"))
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source (slurp source)))))

(defn- prefix-valid? [octets kind]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-elf-prefix-development :compiler-artifact-plan? true}
   @engine-plan 'w5-b13-linux-elf-header-prefix-valid? [octets kind]))

(defn- header [type]
  [127 69 76 70 2 1 1 0 0 0 0 0 0 0 0 0 type 0 62 0])

(deftest current-uniform-elf-prefix-checkpoint
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine-plan)))
  (doseq [[kind type] [[:object 1] [:executable 2] [:other 2] [nil 2]]]
    (let [octets (header type)]
      (is (true? (prefix-valid? octets kind)))
      (is (true? (prefix-valid? (into octets [0 255 42]) kind)))
      (is (false? (prefix-valid? (pop octets) kind)))
      (doseq [index [0 1 2 3 4 5 6 16 17 18 19]]
        (is (false? (prefix-valid? (update octets index inc) kind))
            (str "changed inspected byte " index)))
      (is (false? (prefix-valid? (conj octets :invalid) kind)))
      (is (false? (prefix-valid? (apply list octets) kind))))))
