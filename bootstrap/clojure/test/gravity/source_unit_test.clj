(ns gravity.source-unit-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.source-unit :as source-unit]))

(deftest co-canonical-source-extension-policy-is-extracted
  (testing "the policy remains exactly two first-class, case-sensitive extensions"
    (is (= #{".qst" ".gravity"}
           source-unit/co-canonical-source-extensions))
    (is (= 2 (count source-unit/co-canonical-source-extensions)))
    (doseq [extension [".qst" ".gravity"]]
      (is (contains? source-unit/co-canonical-source-extensions extension))
      (is (true? (source-unit/qst-or-gravity-source?
                  (str "module" extension)))))
    (doseq [extension [".QST" ".GRAVITY"]]
      (is (false? (source-unit/qst-or-gravity-source?
                  (str "module" extension)))))))

(deftest source-extension-and-kind-preserve-bootstrap-edge-cases
  (testing "ordinary and path-like names use the final dot"
    (doseq [[path expected-extension expected-kind]
            [["module.gravity" ".gravity" :gravity-branded-source]
             ["module.qst" ".qst" :qst-theory-source]
             ["src/nested/module.gravity" ".gravity" :gravity-branded-source]
             ["/tmp/nested/module.qst" ".qst" :qst-theory-source]
             ["module.gravity.backup" ".backup" :gravity-source]
             ["module..gravity" ".gravity" :gravity-branded-source]
             ["module." "." :gravity-source]
             ["module" nil :gravity-source]
             [".gravity" nil :gravity-source]
             [".qst" nil :gravity-source]
             ["" nil :gravity-source]
             [nil nil :gravity-source]]]
      (is (= expected-extension
             (source-unit/gravity-source-extension path))
          path)
      (is (= expected-kind
             (source-unit/gravity-source-kind path))
          path)))
  (testing "invalid extensions are not accidentally accepted"
    (doseq [path ["module.txt"
                  "module.QST"
                  "module.GRAVITY"
                  "module"
                  ".gravity"]]
      (is (false? (source-unit/qst-or-gravity-source? path)) path)
      (is (= :gravity-source
             (source-unit/gravity-source-kind path))
          path)))
  (testing "the existing File name semantics are retained for separators"
    (doseq [path ["dir/module.gravity"
                  "dir\\module.gravity"
                  "dir/with.dots/module.qst"]]
      (is (= (source-unit/gravity-source-extension path)
             (source-unit/gravity-source-extension (java.io.File. path)))
          path))))

(deftest source-extension-functions-are-deterministic-and-provenance-preserving
  (let [paths [nil
               "module.gravity"
               "module.qst"
               "module.txt"
               "/checkout/src/module.gravity"
               "./src/../src/module.qst"]
        snapshot (mapv (fn [path]
                         [(source-unit/gravity-source-extension path)
                          (source-unit/qst-or-gravity-source? path)
                          (source-unit/gravity-source-kind path)])
                       paths)]
    (is (= snapshot
           (mapv (fn [path]
                   [(source-unit/gravity-source-extension path)
                    (source-unit/qst-or-gravity-source? path)
                    (source-unit/gravity-source-kind path)])
                 paths)))
    (is (= ".gravity"
           (source-unit/gravity-source-extension
            (java.io.File. "/checkout/src/module.gravity"))))
    (is (= :gravity-branded-source
           (source-unit/gravity-source-kind
            (java.io.File. "/checkout/src/module.gravity"))))))

(deftest source-unit-namespace-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.source-unit) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.source-unit (:namespace contract)))
    (is (= :stage0-co-canonical-source-extension-metadata
           (:contract-boundary contract)))
    (is (= #{'co-canonical-source-extensions
             'gravity-source-extension
             'qst-or-gravity-source?
             'gravity-source-kind}
           (set (keys (:public-api contract)))))
    (is (= [:supplied-source-path] (:artifact-inputs contract)))
    (is (= [:source-extension :source-kind]
           (:artifact-outputs contract)))
    (is (= ['clojure.core 'java.io.File]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (some #{:source-unit-identity}
              (get-in contract [:ownership :does-not-own])))
    (is (empty? (ns-aliases 'gravity.source-unit)))
    (is (= #{'co-canonical-source-extensions
             'gravity-source-extension
             'qst-or-gravity-source?
             'gravity-source-kind}
           (set (keys (ns-publics 'gravity.source-unit)))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
