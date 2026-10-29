(ns gravity.reader.token-root-scan-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.c2-lexical-validation :as validation]
            [gravity.c2-lexical-validation-test :as fixture]
            [gravity.c2-lexical-validation.product]))

(defn- private-var [name]
  (ns-resolve 'gravity.c2-lexical-validation.product name))

(defn- check-token [token roots]
  ((private-var 'token-contains-root?) token roots
   ((private-var 'shortest-root-length) roots)))

(defn- legacy-token [token roots]
  (some #(str/includes? (:raw token) %) roots))

(defn- report [tokens forms roots]
  (validation/c2-lexical-product-validation fixture/source-text tokens forms roots))

(defn- outcome [f]
  (try [:returned (f)]
       (catch Throwable error [:threw (class error) (.getMessage error)])))

(deftest string-length-shortcut-agrees-with-original-containment
  (let [unicode (str (char 0x03bb) (String. (Character/toChars 0x1f642)))
        strings ["" "a" "()" "(a)" "x(a)y" "((a))" unicode (str "(" unicode ")")]]
    (doseq [raw strings roots (concat [#{}] (map hash-set strings)
                                     (for [left strings right strings] (set [left right])))]
      (is (= (boolean (legacy-token {:raw raw} roots))
             (boolean (check-token {:raw raw} roots))) (pr-str [raw roots])))))

(deftest complete-reports-and-independent-negatives-are-preserved
  (doseq [tokens [fixture/token-stream
                  (assoc-in fixture/token-stream [1 :raw] "(a)")
                  (assoc-in fixture/token-stream [1 :raw] "x(a)y")
                  (assoc-in fixture/token-stream [1 :raw] "b")]
          forms [fixture/form-tree
                 (assoc-in fixture/form-tree [0 :raw] "")
                 (assoc-in fixture/form-tree [0 :raw] "a")
                 (assoc-in fixture/form-tree [0 :children] [:missing])
                 (assoc-in fixture/form-tree [1 :parent-form-id] nil)]]
    (let [expected (with-redefs-fn {(private-var 'shortest-root-length) (constantly nil)}
                     #(report tokens forms [:form-0]))]
      (is (= expected (report tokens forms [:form-0])))))
  (is (true? (:no-token-contains-top-level-form?
               (report fixture/token-stream fixture/form-tree [:form-0]))))
  (is (false? (:no-token-contains-top-level-form?
                (report fixture/token-stream (assoc-in fixture/form-tree [0 :raw] "a") [:form-0]))))
  (doseq [raw ["(a)" "x(a)y"]]
    (is (false? (:no-token-contains-top-level-form?
                 (report (assoc-in fixture/token-stream [1 :raw] raw) fixture/form-tree [:form-0])))))
  (is (false? (:token-raw-slices-exact?
               (report (assoc-in fixture/token-stream [1 :raw] "b") fixture/form-tree [:form-0])))))

(deftest opaque-lookups-malformed-values-and-callbacks-retain-original-behavior
  (let [calls (atom []) token (reify clojure.lang.ILookup
                               (valAt [_ key] (swap! calls conj key) "x")
                               (valAt [_ key _] (swap! calls conj key) "x"))
        roots #{"(a)" "(b)"}
        before (outcome #(legacy-token token roots)) before-calls @calls
        _ (reset! calls []) after (outcome #(check-token token roots))]
    (is (= before after))
    (is (= before-calls @calls)))
  (doseq [raw [nil 0 false (StringBuilder. "x")]
          roots [#{} #{"(a)"} #{false "(a)"} #{17}]]
    (is (= (outcome #(boolean (legacy-token {:raw raw} roots)))
           (outcome #(boolean (check-token {:raw raw} roots))))))
  (let [token {:raw "x"} roots #{"(a)" "(b)"} calls (atom [])
        callback (fn [raw root] (swap! calls conj [raw root]) (= 2 (count @calls)))
        expected (with-redefs [str/includes? callback] (legacy-token token roots))
        before @calls _ (reset! calls [])
        observed (with-redefs [str/includes? callback] (check-token token roots))]
    (is (= expected observed))
    (is (= before @calls))))

(deftest short-native-tokens-do-not-scan-whole-root-sets
  (let [roots (set (map #(str "(root-" % ")") (range 128)))
        tokens (repeat 256 {:raw "x"}) visits (atom 0) original-some some
        count-some (fn [predicate items]
                     (if (identical? roots items)
                       (original-some (fn [item] (swap! visits inc) (predicate item)) items)
                       (original-some predicate items)))]
    (with-redefs [clojure.core/some count-some]
      (is (not-any? #(legacy-token % roots) tokens)))
    (is (= (* 128 256) @visits))
    (reset! visits 0)
    (with-redefs [clojure.core/some count-some]
      (is (not-any? #(check-token % roots) tokens)))
    (is (zero? @visits)))
  (is (nil? (find-ns 'gravity.bootstrap))))
