(ns gravity.bootstrap-compatibility.c5-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn fixture
  [name]
  (str "bootstrap/clojure/fixtures/" name))

(defn diagnostic-id
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(deftest c5-resolution-compatibility-wrappers-preserve-arglists-and-interposition
  (let [path (fixture "accepted/compiler-c5-name-resolution.gravity")
        source-text (slurp path)
        validation-calls (atom [])
        artifact
        (with-redefs [bootstrap/c5-resolution-validate-overrides!
                      (fn [source-path module overrides]
                        (swap! validation-calls conj
                               [source-path (:module module) overrides])
                        nil)]
          (bootstrap/compiler-c5-resolution-source-artifact
           path source-text))
        file-sentinel {:interposed :compiler-c5-resolution-source-artifact}
        file-result
        (with-redefs [bootstrap/compiler-c5-resolution-source-artifact
                      (fn [_ _] file-sentinel)]
          (bootstrap/compiler-c5-resolution-file-artifact path))
        override-id
        (with-redefs [bootstrap/c5-resolution-override-diagnostics
                      {:alias "C5-OVERRIDE"}]
          (diagnostic-id
           #(bootstrap/c5-resolution-validate-overrides!
             path {:module 'demo.main}
             {:fail :alias})))
        special-record
        (with-redefs [bootstrap/c5-special-form-symbols #{'sentinel}]
          (bootstrap/c5-resolution-record
           {:module 'demo.main}
           {} {} {} [] {:syntax-id "sentinel-syntax"} 0 'sentinel))
        sentinel-artifact
        (with-redefs [bootstrap/c5-resolution-diagnostic-ids ["C5-SENTINEL"]
                      bootstrap/c5-resolution-governing-document "sentinel-doc"
                      bootstrap/c5-resolution-rejected-designs
                      [{:diagnostic "C5-SENTINEL"}]
                      bootstrap/c5-resolution-validate!
                      (fn [_ _] nil)]
          (bootstrap/compiler-c5-resolution-source-artifact
           path source-text))]
    (is (= '([source-path source-text])
           (:arglists (meta #'bootstrap/compiler-c5-resolution-source-artifact))))
    (is (= '([path])
           (:arglists (meta #'bootstrap/compiler-c5-resolution-file-artifact))))
    (is (= '([source-path module overrides])
           (:arglists (meta #'bootstrap/c5-resolution-validate-overrides!))))
    (is (= :gravity/stage0-c5-name-resolution-artifact (:kind artifact)))
    (is (= 1 (count @validation-calls)))
    (is (= path (ffirst @validation-calls)))
    (is (= file-sentinel file-result))
    (is (= "C5-OVERRIDE" override-id))
    (is (= :special-form (:resolution-order special-record)))
    (is (= "sentinel-doc" (:governing-document sentinel-artifact)))
    (is (= ["C5-SENTINEL"]
           (get-in sentinel-artifact
                   [:c5-resolution-results :required-diagnostic-ids])))
    (is (= [{:diagnostic "C5-SENTINEL"}]
           (:rejected-design-coverage sentinel-artifact)))
    (is (= #{:jvm :wasm}
           (:target-set
            (binding [bootstrap/*additional-bootstrap-targets* #{:native}]
              (with-redefs [bootstrap/supported-targets #{:jvm :wasm}]
                (bootstrap/c5-special-form-binding
                 'if {:module 'demo.main}))))))))
