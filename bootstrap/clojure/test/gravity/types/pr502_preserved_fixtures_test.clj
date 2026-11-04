(ns gravity.types.pr502-preserved-fixtures-test
  "Exact source preservation controls for the remaining PR502 fixture families.

  These files are deliberately outside accepted fixtures. The test proves
  byte conservation and .gravity/.qst pairing only; it does not claim reader,
  C7, verifier, lowering, runtime, or source-product acceptance."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]))

(def ^:private fixture-digests
  {"compact-source-proof-interval" "b73cb794e6ef937a8fa786c6a6d5ae91afced2457d34367e07ce50f72306ccec"
   "compact-source-proof-minimum" "cb92409e01b28c9218c631327a104a07dcb14c4c9d867d4b97557b074e832e6b"
   "function-verifier-qualified-map-get" "78485b120a675672be4b3828fa455fadabdbaf532fd1e0c86a723efa790fe8e5"
   "function-verifier-work-ledger" "b4a7f92c80f2c0ab755cbedcf7a3f71ed8926bfbb2032f520fbbda7e85df6a33"
   "local-runtime-bare-set-equality" "a65f559b1bb75251268783e6addc406a5eea5359f97ea041b008157631f0f373"})

(defn- fixture-path [stem extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-08/preserved/"
       stem extension))

(defn- sha256 [path]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest (java.nio.file.Files/readAllBytes
                     (java.nio.file.Paths/get path (make-array String 0))))
    (format "%064x" (java.math.BigInteger. 1 (.digest digest)))))

(deftest preserved-fixtures-are-exact-and-byte-paired
  (doseq [[stem expected] fixture-digests]
    (let [gravity (fixture-path stem ".gravity")
          qst (fixture-path stem ".qst")]
      (is (.exists (io/file gravity)) (str "missing preserved source: " gravity))
      (is (.exists (io/file qst)) (str "missing preserved companion: " qst))
      (is (= expected (sha256 gravity)) (str "source changed: " gravity))
      (is (= expected (sha256 qst)) (str "companion changed: " qst))
      (is (= (slurp gravity) (slurp qst))
          (str "source and companion differ: " stem)))))
