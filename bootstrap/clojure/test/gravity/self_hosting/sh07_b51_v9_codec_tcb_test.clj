(ns gravity.self-hosting.sh07-b51-v9-codec-tcb-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.tools.sh07-b51-v9-codec :as codec]))

(def ^:private expected-publics
  '#{H W R transport-limits transport-api})

(deftest codec-public-and-dependency-surfaces-are-closed
  (is (= expected-publics
         (set (keys (ns-publics 'gravity.tools.sh07-b51-v9-codec)))))
  (is (= '#{H W R}
         (->> (ns-publics 'gravity.tools.sh07-b51-v9-codec)
              (keep (fn [[name public-var]]
                      (when (fn? @public-var) name)))
              set)))
  (is (= '#{canonical digest}
         (set (keys (ns-aliases 'gravity.tools.sh07-b51-v9-codec)))))
  (doseq [[var-value arglists]
          [[#'codec/H '([value])]
           [#'codec/W '([value])]
           [#'codec/R '([bytes])]]]
    (is (= arglists (:arglists (meta var-value))) (str var-value)))
  (is (= #{:strict-read-wire :bounded-wire-bytes :read-bounded-bytes
           :read-bounded-frame :bounded-byte-count! :unsigned-byte-compare
           :unsigned-wire-sort}
         (set (keys codec/transport-api))))
  (is (every? fn? (vals codec/transport-api))))

(deftest codec-contract-preserves-the-bootstrap-tcb-and-nonclaims
  (let [contract-var
        (get (ns-interns 'gravity.tools.sh07-b51-v9-codec)
             'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.tools.sh07-b51-v9-codec (:namespace contract)))
    (is (= :sh07-b51-v9-codec-tcb (:contract-boundary contract)))
    (is (= '[H W R] (:identity-authority contract)))
    (is (= 'transport-api (:transport-api contract)))
    (is (= codec/transport-limits (:transport-limits contract)))
    (is (= ['gravity.c2-artifact-identity.canonical 'gravity.digest]
           (get-in contract [:dependency-direction :requires])))
    (is (= '#{gravity.bootstrap
              gravity.c2-artifact-identity
              gravity.tools.sh07-b51-attempt32-checker-child
              gravity.tools.sh07-b51-attempt32-coordinator-child}
           (set (get-in contract [:dependency-direction :forbids]))))
    (is (= {:bootstrap-tcb? true
            :gravity-semantic-authority? false
            :release? false
            :self-hosting? false
            :seed-retirement? false
            :hostile-host? false
            :aggregate-sh07? false}
           (:authority contract)))))

(deftest codec-source-has-no-facade-or-participant-load-edge
  (let [facade (slurp (io/resource "gravity/tools/sh07_b51_v9_codec.clj"))
        resources (map second (re-seq #"\"(/gravity/[^\"]+)\"" facade))
        source (str (str/replace facade #"(?s)\(load\s.*\)\s*$" "")
                    (str/join "\n" (map #(slurp (io/resource
                                                (str (subs % 1) ".clj")))
                                           resources)))]
    (is (seq resources))
    (is (every? #(str/starts-with? % "/gravity/tools/sh07_b51_v9_codec/") resources))
    (testing "the codec uses the canonical and digest leaves directly"
      (is (re-find
           #"\[gravity\.c2-artifact-identity\.canonical :as canonical\]"
           source))
      (is (re-find #"\[gravity\.digest :as digest\]" source))
      (is (not (re-find #"\[gravity\.c2-artifact-identity :as" source)))
      (is (not (re-find #"\[gravity\.bootstrap" source))))
    (testing "strict UTF-8 is decoder-based and never constructs String from bytes"
      (is (re-find #"CodingErrorAction/REPORT" source))
      (is (not (re-find #"\(String\.\s+bytes" source))))
    (testing "the codec does not dynamically load checker or coordinator code"
      (is (not (re-find #"\((?:requiring-resolve|require|load|load-file|resolve|eval)\s"
                        source))))))
