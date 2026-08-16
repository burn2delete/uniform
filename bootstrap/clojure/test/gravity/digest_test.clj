(ns gravity.digest-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.digest :as digest]))

(deftest sha256-string-known-vectors
  (testing "String inputs are encoded as UTF-8 before hashing"
    (doseq [[text expected]
            [["" "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]
             ["abc" "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"]
             ["The quick brown fox jumps over the lazy dog"
              "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"]
             ["\u0000"
              "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"]
             ["Gravity \u03bb \ud83d\ude80\u0000"
              "650345ba4163bde478a83878dbee94989c21ffc7083be854f38867907b62fc6a"]]]
      (is (= expected (digest/sha256-hex text)) text)
      (is (= 64 (count (digest/sha256-hex text))))
      (is (re-matches #"[0-9a-f]{64}" (digest/sha256-hex text))))))

(deftest sha256-byte-array-known-vectors
  (testing "byte-array inputs are consumed literally, including NUL and high bytes"
    (doseq [[bytes expected]
            [[(byte-array 0)
              "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]
             [(byte-array [(byte 0)])
              "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d"]
             [(byte-array [(byte 0) (unchecked-byte 0xff) (unchecked-byte 0x80)])
              "f742b965f156c10374bc23aea96e3a8aff8facd6fc079defeaa30219ad86f211"]]]
      (is (= expected (digest/sha256-bytes-hex bytes)) bytes)
      (is (re-matches #"[0-9a-f]{64}" (digest/sha256-bytes-hex bytes))))))

(deftest sha256-string-and-byte-array-inputs-remain-distinct
  (testing "UTF-8 bytes agree when supplied explicitly"
    (let [text "\u00e9"
          utf8-bytes (.getBytes text "UTF-8")]
      (is (= (digest/sha256-hex text)
             (digest/sha256-bytes-hex utf8-bytes)))
      (is (= "4a99557e4033c3539de2eb65472017cad5f9557f7a0625a09f1c3f6e2ba69c4c"
             (digest/sha256-hex text)))))
  (testing "a byte array is not silently decoded as text"
    (let [latin-1-byte (byte-array [(unchecked-byte 0xe9)])]
      (is (= "de2e331d891ae267a7009cb45b4e8830f170e0c937288ea2731a1941c7a53b0d"
             (digest/sha256-bytes-hex latin-1-byte)))
      (is (not= (digest/sha256-hex "\u00e9")
                (digest/sha256-bytes-hex latin-1-byte)))))
  (testing "the two entry points do not cross-coerce their input types"
    (is (thrown? IllegalArgumentException
                 (digest/sha256-hex (byte-array [(byte 97)]))))
    (is (thrown? ClassCastException
                 (digest/sha256-bytes-hex "a")))))

(deftest sha256-primitives-are-deterministic
  (let [text "repeatable \u03bb\u0000"
        bytes (.getBytes text "UTF-8")
        string-results (repeatedly 8 #(digest/sha256-hex text))
        byte-results (repeatedly 8 #(digest/sha256-bytes-hex bytes))]
    (is (= 1 (count (set string-results))))
    (is (= 1 (count (set byte-results))))
    (is (= (first string-results) (first byte-results)))))

(deftest sha256-public-api-is-small-and-stable
  (is (= #{'sha256-hex 'sha256-bytes-hex}
         (set (keys (ns-publics 'gravity.digest)))))
  (is (= '([text]) (:arglists (meta #'digest/sha256-hex))))
  (is (= '([bytes]) (:arglists (meta #'digest/sha256-bytes-hex))))
  (is (empty? (ns-aliases 'gravity.digest))))

(deftest sha256-namespace-contract-is-narrow-and-acyclic
  (let [contract-var
        (get (ns-interns 'gravity.digest) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.digest (:namespace contract)))
    (is (= :stage0-sha256-primitives (:contract-boundary contract)))
    (is (= #{'sha256-hex 'sha256-bytes-hex}
           (set (keys (:public-api contract)))))
    (is (= [:utf8-string :byte-array] (:artifact-inputs contract)))
    (is (= [:sha256-lowercase-hex] (:artifact-outputs contract)))
    (is (= ['clojure.core 'java.security.MessageDigest]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [nonclaim [:canonical-encoding
                      :artifact-identity
                      :source-reads
                      :signing
                      :authority-logic
                      :artifact-cache-key
                      :artifact-cache-policy
                      :artifact-provenance]]
      (is (some #{nonclaim}
                (get-in contract [:ownership :does-not-own]))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? contract)))))
