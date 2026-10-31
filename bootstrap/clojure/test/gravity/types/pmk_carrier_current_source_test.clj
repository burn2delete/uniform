(ns gravity.types.pmk-carrier-current-source-test
  "Inert composed-source equations against the accepted accounting-removal source. No Gravity definition or project model is evaluated."
  (:require [clojure.test :refer [deftest is]] [clojure.java.shell :as shell]
            [clojure.walk :as walk]))
(def base "361db6aa215c7adcb21a06ab1e9865c32ae8e4f0")
(def owners [["sh08" "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"]
             ["body" "bootstrap/gravity/src/gravity/effects.gravity"]
             ["p15-s23" "bootstrap/gravity/p15_s23/emitter.gravity"]])
(defn before [path]
  (let [r (shell/sh "git" "show" (str base ":" path))] (assert (zero? (:exit r))) (:out r)))
(defn definitions [text]
  (binding [*read-eval* false]
    (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
      (loop [out {}] (let [x (read {:eof ::end} r)]
                      (if (= ::end x) out
                        (recur (if (and (seq? x) (contains? '#{defn defn-} (first x))) (assoc out (second x) x) out))))))))
(defn nodes [x] (tree-seq coll? seq x))
(def old-width '[(vector? (get parameter-form :value)) (= (count names) (count (get parameter-form :value)))])
(def width '(let [values (get parameter-form :value)] (if (vector? values) (= (count names) (count values)) false)))
(def correspondence
  '(let [values (get parameter-form :value)]
     (loop [i 0]
       (if (= i (count names)) true
         (let [form (get forms (get names i)) value (get values i)]
           (if (map? form)
             (let [kind (get form :kind) original (get form :value)]
               (if (if (= :symbol kind)
                     (if (symbol? original) (if (symbol? value) (= original value) false) false)
                     (if (= :keyword kind)
                       (if (keyword? original) (if (keyword? value) (= original value) false) false) false))
                 (recur (+ i 1)) false)) false))))))
(def failure {:status :rejected :reason :private-map-keys-source-mismatch})
(defn expected [old]
  (let [accepted (filter #(and (map? %) (= :accepted (:status %))) (nodes old))]
    (assert (= 1 (count accepted)))
    (assert (= 1 (count (for [x (nodes old) :when (vector? x)
                              pair (partition 2 1 x) :when (= old-width (vec pair))] pair))))
    (assert (some #{failure} (nodes old)))
    (walk/postwalk
      (fn [x]
        (cond (= x (first accepted)) (list 'if correspondence x failure)
              (vector? x) (loop [xs (seq x) out []]
                            (if-not xs out
                              (if (= old-width (vec (take 2 xs))) (recur (nnext xs) (conj out width))
                                (recur (next xs) (conj out (first xs))))))
              :else x)) old)))
(defn erase [new]
  (walk/postwalk
    (fn [x]
      (cond (and (seq? x) (= 'if (first x)) (= correspondence (second x))) (nth x 2)
            (and (vector? x) (some #{width} x)) (vec (mapcat #(if (= width %) old-width [%]) x))
            :else x)) new))
(defn outside [text name]
  (let [a (.indexOf text (str "(defn " name " ")) b (.indexOf text "\n(defn " (inc a))]
    (assert (and (pos? a) (pos? b))) (str (subs text 0 a) "<owned>" (subs text b))))
(defn replace-data [x old new] (walk/postwalk-replace {old new} x))

(deftest main-defect-and-complete-correctness-equation
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter")) a (before path) b (slurp path)
                old (definitions a) new (definitions b) of (get old name) nf (get new name)]]
    (is (= 6 (count (nth of 2)) (count (nth nf 2))))
    (is (not= of nf))
    (is (= nf (expected of)) "Add only the two accepted correctness sites; preserve existing refusal priority")
    (is (= of (erase nf)))
    (is (= (dissoc old name) (dissoc new name)))
    (is (= (outside a name) (outside b name)))
    (is (= 1 (count (filter #{width} (nodes nf)))))
    (is (= 1 (count (filter #{correspondence} (nodes nf))))))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.types.private-map-parameter-test))))

(deftest effective-main-source-corruptions
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter"))
                old (get (definitions (before path)) name) good (get (definitions (slurp path)) name)]]
    (doseq [[a b] [['(vector? values) true] ['(= (count names) (count values)) true]
                   ['(map? form) true] ['(symbol? original) true] ['(symbol? value) true]
                   ['(keyword? original) true] ['(keyword? value) true] ['(= original value) true]
                   ['(get values i) '(get values 0)] ['(get forms (get names i)) '(get forms (first names))]
                   ['(recur (+ i 1)) true] ['(= i (count names)) '(= i 1)]]]
      (let [bad (replace-data good a b)]
        (is (not= good bad))
        (is (not= (expected old) bad))))
    (let [bad (assoc (vec good) 2 (vec (butlast (nth good 2))))]
      (is (= 5 (count (nth bad 2))))
      (is (not= (expected old) bad)))
    (let [accepted (first (filter #(and (map? %) (= :accepted (:status %))) (nodes old)))
          without (replace-data good (list 'if correspondence accepted failure) accepted)
          lifted (list 'defn name (nth good 2) (list 'if correspondence (nth without 3) failure))]
      (is (not= good lifted))
      (is (not= (expected old) lifted) "Lifting the walk changes existing failure priority"))))


(deftest proposed-regression-definitions-remain-unloaded-data
  (let [text (slurp "bootstrap/clojure/test/gravity/types/pmk_carrier_regression_test.clj")
        forms (binding [*read-eval* false]
                (with-open [r (java.io.PushbackReader. (java.io.StringReader. text))]
                  (loop [out []]
                    (let [x (read {:eof ::end} r)]
                      (if (= ::end x) out (recur (conj out x)))))))
        selected (mapv second (filter #(= 'deftest (first %)) forms))
        syms (set (filter symbol? (mapcat nodes forms)))
        cases (first (filter #(and (vector? %) (vector? (first %))
                                   (= :carrier-keyword (first (first %))))
                             (mapcat nodes forms)))]
    (is (= 14 (count forms)))
    (is (= 'gravity.types.pmk-carrier-regression-test (second (first forms))))
    (is (every? '#{ns def defn- deftest} (map first forms)))
    (is (= '[pmk-carrier-real-owner-valid-vectors pmk-carrier-real-owner-fourteen-data-cases
             pmk-carrier-real-owner-native-guard-and-refusal-priority
             pmk-carrier-real-owner-proof-consumer-retains-source-refusal] selected))
    (is (= #{:carrier-keyword :carrier-list :carrier-map :carrier-set :width :late-name :late-marker
             :late-type :opaque-payload :opaque-child :missing-child :children-reordered :metadata}
           (set (map first cases))))
    (is (every? syms '[pr359-descriptor-c7 pr359-descriptor-body pr359-descriptor-emit
                       private-map-keys-input private-map-keys-two-argument-input]))
    (is (nil? (find-ns 'gravity.types.pmk-carrier-regression-test)))
    (is (nil? (find-ns 'gravity.self-hosting.sh08-function-call-type-test)))
    (is (nil? (find-ns 'gravity.self-hosting.sh09-body-effect-test)))
    (is (nil? (find-ns 'gravity.bootstrap)))))
