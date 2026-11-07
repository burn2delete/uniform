(ns gravity.backend.llvm.elf-header-adoption-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

; Development execution feedback, not Linux execution or migration evidence.
(defn- valid? [bytes kind]
  ((ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-linux-elf-bytes-valid?)
   bytes kind))

(defn- header [type]
  [127 69 76 70 2 1 1 0 0 0 0 0 0 0 0 0 type 0 62 0])

(defn- native-bytes [octets]
  (byte-array (map unchecked-byte octets)))

(deftest production-predicate-executes-current-uniform-source
  (let [execute bootstrap/p15-s23-stage2-runtime-execute-function
        calls (atom [])]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [runtime plan function arguments]
                    (swap! calls conj {:plan plan :function function
                                       :arguments arguments})
                    (execute runtime plan function arguments))]
      (doseq [[kind type] [[:object 1] [:executable 2] [:other 2] [nil 2]]]
        (is (true? (valid? (native-bytes (header type)) kind)))
        (is (false? (valid? (native-bytes (header (if (= type 1) 2 1))) kind))))
      (doseq [[kind type] [[:object 1] [:executable 2]]]
        (is (true? (valid? (native-bytes (into (header type) [128 255])) kind)))
        (doseq [index [0 1 2 3 4 5 6 16 17 18 19]]
          (is (false? (valid? (native-bytes (assoc (header type) index 255)) kind))))))
    (is (seq @calls))
    (doseq [{:keys [plan function arguments]} @calls]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
      (is (= (.getCanonicalPath
              (io/file "bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity"))
             (get-in plan [:source :path])))
      (is (= 'w5-b13-linux-elf-header-prefix-valid? function))
      (is (vector? (first arguments))))
    (is (some #(= [128 255] (subvec (first (:arguments %)) 20))
              (filter #(= 22 (count (first (:arguments %)))) @calls)))))

(deftest native-admission-and-truncation-remain-fail-closed
  (let [touches (atom [])
        opaque (reify clojure.lang.Seqable
                 (seq [_] (swap! touches conj :seq) (throw (ex-info "opaque" {})))
                 Object
                 (toString [_] (swap! touches conj :string) "opaque"))]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                  (fn [& _] (throw (ex-info "non-native input reached execution" {})))]
      (doseq [carrier [nil false "ELF" (header 1) (apply list (header 1))
                       {:bytes (native-bytes (header 1))} (int-array (header 1))
                       opaque]]
        (is (false? (valid? carrier :object)))))
    (is (empty? @touches)))
  (doseq [length (range 20)]
    (is (false? (valid? (native-bytes (subvec (header 1) 0 length)) :object)))))

(deftest failed-execution-cannot-use-a-host-fallback-or-success-label
  (doseq [result [nil :accepted {:status :accepted} {:valid? true} "true" 1]]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] result)]
      (is (false? (valid? (native-bytes (header 1)) :object)))))
  (let [failure (ex-info "Uniform execution unavailable" {:reason :unavailable})]
    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function (fn [& _] (throw failure))]
      (is (identical? failure
                      (try (valid? (native-bytes (header 1)) :object)
                           (catch clojure.lang.ExceptionInfo error error)))))))
