(ns gravity.digest
  "Stage 0 SHA-256 primitives for string and byte-array inputs.

  This leaf owns only SHA-256 computation and the historical lowercase
  hexadecimal rendering. It intentionally does not claim canonical encoding,
  artifact identity, source reads, signing, authority, or cache policy. Those
  concerns stay with their owning stages and callers.")

(def ^:private namespace-contract
  {:namespace 'gravity.digest
   :contract-boundary :stage0-sha256-primitives
   :public-api
   {'sha256-hex
    {:arglists '([text])
     :returns :lowercase-64-character-hex-string}
    'sha256-bytes-hex
    {:arglists '([bytes])
     :returns :lowercase-64-character-hex-string}}
   :artifact-inputs [:utf8-string :byte-array]
   :artifact-outputs [:sha256-lowercase-hex]
   :ownership
   {:owns [:sha256-computation :lowercase-hex-rendering]
    :does-not-own [:canonical-encoding
                   :artifact-identity
                   :source-reads
                   :signing
                   :authority-logic
                   :artifact-cache-key
                   :artifact-cache-policy
                   :artifact-provenance]}
   :dependency-direction
   {:requires ['clojure.core 'java.security.MessageDigest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner 'gravity.digest-test/sha256-namespace-contract-is-narrow-and-acyclic
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn sha256-hex
  "Return the SHA-256 digest of a String's UTF-8 bytes as lowercase hex.

  String conversion intentionally follows the existing bootstrap call site:
  this function accepts a String and does not coerce arbitrary values or byte
  arrays. The returned value has no `sha256:` prefix."
  [text]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        (.getBytes text "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn sha256-bytes-hex
  "Return the SHA-256 digest of a byte array as lowercase hex.

  Bytes are consumed literally; no text decoding or canonicalization occurs.
  The returned value has no `sha256:` prefix."
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))
