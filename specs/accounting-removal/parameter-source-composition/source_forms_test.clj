(ns source-forms-test
  "Core reader controls only; no composition checker or Gravity/model loading."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(load-file "specs/accounting-removal/parameter-source-composition/source_forms.clj")
(require '[source-forms :as sf])

(defn native-forms [text]
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                     (java.io.StringReader. text))]
    (let [end (Object.)]
      (binding [*read-eval* false]
        (loop [out []]
          (let [value (read {:eof end} reader)]
            (if (identical? end value) out (recur (conj out value)))))))))

(defn stable [x] (str/replace (pr-str x) #"__[0-9]+#" "__reader#"))

(def sentinels
  [":source-forms/eof" ":list-parameter-source-forms/eof" ":eof" "::eof"
   "source-forms/eof" "eof" "nil" "false" "0" "\"eof\""
   "{:eof :source-forms/eof}" "[:list-parameter-source-forms/eof]"
   "'(:source-forms/eof :list-parameter-source-forms/eof)"])

(deftest sentinel-looking-source-values-never-end-the-stream
  (doseq [value sentinels
          :let [text (str value "\n(defn after [] 1)\n")
                actual (sf/forms text)]]
    (is (= 2 (count actual)) value)
    (is (= (stable (native-forms text)) (stable (mapv :form actual))) value)
    (is (= 'after (second (:form (sf/named actual 'after)))) value)
    (is (= "(defn after [] 1)" (subs text (:start (second actual)) (:end (second actual)))) value)))

(deftest malformed-tail-after-every-sentinel-looking-value-still-refuses
  (doseq [value sentinels
          :let [text (str value "\n(")]]
    (is (thrown? RuntimeException (sf/forms text)) value)
    (is (thrown? RuntimeException (native-forms text)) value)))

(deftest actual-eof-and-normal-reader-data
  (doseq [text ["" "  \n" "; only a comment\n"]]
    (is (= [] (sf/forms text))))
  (doseq [text ["(def a [1 2])\n(def b {:k true})" "#_ :source-forms/eof\n(def a nil)"
               "(def a #\"abc\")" "(def a #{:one :two})" "(defn a [x]\n x)"]]
    (is (= (stable (native-forms text)) (stable (mapv :form (sf/forms text)))))))

(deftest malformed-and-read-eval-input-remain-strict
  (doseq [text ["(" ")" "(def a [1 2)" "{1}" "#unknown/tag x" "'"
               "#=(+ 1 2)" ":source-forms/eof\n#=(+ 1 2)"
               ":list-parameter-source-forms/eof\n#=(+ 1 2)"]]
    (is (thrown? RuntimeException (sf/forms text)))
    (is (thrown? RuntimeException (native-forms text))))
  (binding [*read-eval* true]
    (is (thrown? RuntimeException (sf/forms "#=(+ 1 2)")))))

(deftest all-three-production-files-retain-native-reader-parity
  (doseq [path ["bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
               "bootstrap/gravity/src/gravity/effects.gravity"
               "bootstrap/gravity/p15_s23/emitter.gravity"]
          :let [text (slurp (io/file path)) actual (sf/forms text)
                expected (native-forms text)]]
    (is (= (count expected) (count actual)) path)
    (is (= (stable expected) (stable (mapv :form actual))) path)
    (doseq [{:keys [start end]} actual]
      (is (and (integer? start) (<= 0 start end (count text))) path))))

(deftest namespace-boundary
  (doseq [n '[gravity.bootstrap gravity.self-hosting.sh08-function-call-type-test
             gravity.self-hosting.sh09-body-effect-test gravity.self-hosting.sh01-stage2-runtime-iteration-test
             gravity.types.private-map-cycle-test gravity.types.private-map-fact-test
             gravity.types.private-map-source-test gravity.types.private-map-physical-test
             gravity.types.private-map-metadata-test gravity.types.private-map-parameter-test]]
    (is (nil? (find-ns n)))))

(let [result (run-tests 'source-forms-test)]
  (prn {:schema :source-reader/R1-controls-v1 :counts result
        :sentinel-looking-values (count sentinels) :production-parities 3
        :composition-checker-executed false :Gravity-or-model-evaluation false})
  (when (pos? (+ (:fail result) (:error result))) (System/exit 1)))
