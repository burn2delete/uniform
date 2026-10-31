(ns gravity.types.pmk-carrier-main-source-test
  "Inert current-main source equations. No Gravity definition or project model is evaluated."
  (:require [clojure.test :refer [deftest is]] [clojure.java.shell :as shell]
            [clojure.walk :as walk] [clojure.edn :as edn]))
(def base "ae0cd80165b2085e7ce362d0c7bfc5eeec79b2de")
(def owners [["sh08" "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"]
             ["body" "bootstrap/gravity/src/gravity/effects.gravity"]
             ["p15-s23" "bootstrap/gravity/p15_s23/emitter.gravity"]])
;; The pinned source equation still includes historical accounting; only the
;; live C7 owner removes it. Effects and the emitter keep their microbudgets.
(def unmetered-owners #{"sh08"})
(defn unmetered-owner? [prefix] (contains? unmetered-owners prefix))
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
(def failure {:status :rejected :reason :private-map-keys-source-mismatch :work-count 'base})
(defn expected [old]
  (let [accepted (filter #(and (map? %) (= :accepted (:status %))) (nodes old))]
    (assert (= 1 (count accepted)))
    (assert (= 1 (count (for [x (nodes old) :when (vector? x)
                              pair (partition 2 1 x) :when (= old-width (vec pair))] pair))))
    (assert (= 'base (:work-count (first accepted))))
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
(defn without-accounting [form]
  (let [drop-base (fn [bindings]
                    (vec (mapcat (fn [[name value]]
                                   (if (= 'base name) [] [name value]))
                                 (partition 2 bindings))))
        strip (walk/postwalk
                (fn [x]
                  (if (map? x) (dissoc x :work-count) x))
                form)
        args (nth strip 2)
        body (walk/postwalk
               (fn [x]
                 (if (and (seq? x) (= 'if (first x)) (= '(> base budget) (second x)))
                   (nth x 3)
                   x))
               (nth strip 3))
        body (if (and (seq? body) (= 'let (first body)) (vector? (second body)))
               (apply list (assoc (vec body) 1 (drop-base (second body))))
               body)]
    (apply list (assoc (vec strip) 2 (vec (butlast args)) 3 body))))
(defn expected-for [prefix old]
  (if (unmetered-owner? prefix) (without-accounting (expected old)) (expected old)))

(deftest main-defect-and-complete-correctness-equation
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter")) a (before path) b (slurp path)
                old (definitions a) new (definitions b) of (get old name) nf (get new name)]]
    (is (= 7 (count (nth of 2))))
    (is (= (if (unmetered-owner? prefix) 6 7) (count (nth nf 2))))
    (is (not= of nf))
    (is (= nf (expected-for prefix of)) "Preserve semantic carrier and failure priority in the complete equation")
    (is (= (if (unmetered-owner? prefix) (without-accounting of) of) (erase nf)))
    (is (= (dissoc old name) (dissoc new name)))
    (is (= (outside a name) (outside b name)))
    (is (= 1 (count (filter #{width} (nodes nf)))))
    (is (= 1 (count (filter #{correspondence} (nodes nf))))))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.types.private-map-parameter-test))))

(deftest effective-main-source-corruptions
  (doseq [[prefix path] owners
          :let [name (symbol (str prefix "-pmk-parameter"))
                old (get (definitions (before path)) name) good (get (definitions (slurp path)) name)
                target (expected-for prefix old)]]
    (doseq [[a b] [['(vector? values) true] ['(= (count names) (count values)) true]
                   ['(map? form) true] ['(symbol? original) true] ['(symbol? value) true]
                   ['(keyword? original) true] ['(keyword? value) true] ['(= original value) true]
                   ['(get values i) '(get values 0)] ['(get forms (get names i)) '(get forms (first names))]
                   ['(recur (+ i 1)) true] ['(= i (count names)) '(= i 1)]]]
      (let [bad (replace-data good a b)]
        (is (not= good bad))
        (is (not= target bad))))
    (let [bad (assoc (vec good) 2 (vec (butlast (nth good 2))))]
      (is (= (if (unmetered-owner? prefix) 5 6) (count (nth bad 2))))
      (is (not= target bad)))
    (let [live (without-accounting old)
          accepted (first (filter #(and (map? %) (= :accepted (:status %))) (nodes live)))
          live-failure (dissoc failure :work-count)
          without (replace-data good (list 'if correspondence accepted live-failure) accepted)
          lifted (list 'defn name (nth good 2) (list 'if correspondence (nth without 3) live-failure))]
      (is (not= good lifted))
      (is (not= target lifted) "Lifting the walk changes existing failure priority"))))

(deftest data-expectations-are-not-semantic-results
  (let [p (edn/read-string (slurp "specs/accounting-removal/pmk-carrier-main-data.edn"))]
    (is (= :inert-data-only (:authority p)))
    (is (= :unrun (:semantic-execution p)))
    (is (= 7 (:parameter-ABI p)))
    (is (= :preserved (:remaining-accounting p)))
    (is (every? #(= :unrun (:observed %)) (:cases p)))
    (is (= #{:valid :carrier :width :late-name :late-marker :late-type :opaque :children :metadata}
           (set (map :kind (:cases p)))))))
