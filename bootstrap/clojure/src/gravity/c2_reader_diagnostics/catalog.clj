(ns gravity.c2-reader-diagnostics.catalog)

(def diagnostic-ids
  ["C2-ENCODING"
   "C2-DELIMITER"
   "C2-STRING"
   "C2-NUMERIC"
   "C2-IDENTIFIER"
   "C2-NS-SHAPE"
   "C2-MAP"
   "C2-SET"
   "C2-METADATA"
   "C2-ABBREV"
   "C2-EXTENSION"
   "C2-HASH"])

(def governing-document
  "docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md")

(def rejected-designs
  [{:diagnostic "C2-ENCODING"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-encoding.gravity"
    :rejected-design :nondeterministic-source-decoding}
   {:diagnostic "C2-DELIMITER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-delimiter.gravity"
    :rejected-design :malformed-delimiter-tree}
   {:diagnostic "C2-STRING"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-string.gravity"
    :rejected-design :lost-string-escape-facts}
   {:diagnostic "C2-NUMERIC"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/malformed-numeric.gravity"
    :rejected-design :malformed-numeric-reclassified-or-host-parsed}
   {:diagnostic "C2-IDENTIFIER"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/malformed-identifier.gravity"
    :rejected-design :malformed-symbol-or-keyword-spelling}
   {:diagnostic "C2-NS-SHAPE"
    :fixture "bootstrap/clojure/fixtures/self-hosting/sh-03/rejected/namespace-missing-name.gravity"
    :rejected-design :host-owned-or-malformed-namespace-clause-shape}
   {:diagnostic "C2-MAP"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-map.gravity"
    :rejected-design :odd-map-literal}
   {:diagnostic "C2-SET"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-set.gravity"
    :rejected-design :duplicate-literal-set-entry}
   {:diagnostic "C2-METADATA"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-metadata.gravity"
    :rejected-design :unattached-or-invalid-metadata}
   {:diagnostic "C2-ABBREV"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-abbrev.gravity"
    :rejected-design :invalid-reader-abbreviation}
   {:diagnostic "C2-EXTENSION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-extension.gravity"
    :rejected-design :ambient-reader-extension-authority}
   {:diagnostic "C2-HASH"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c2-hash.gravity"
    :rejected-design :unstable-reader-artifact-identity}])

(def override-diagnostics
  {:encoding "C2-ENCODING"
   :abbrev "C2-ABBREV"
   :hash "C2-HASH"})

(defn source-overrides [module]
  (get-in module [:metadata :compiler :c2-reader] {}))

(defn message [id]
  (case id
    "C2-ENCODING" "source decoding failed or used an undeclared encoding"
    "C2-DELIMITER" "reader delimiter structure is malformed"
    "C2-STRING" "string or character literal is malformed"
    "C2-NUMERIC" "numeric candidate fails every enabled numeric literal grammar"
    "C2-IDENTIFIER" "symbol or keyword has an invalid surface spelling"
    "C2-NS-SHAPE" "namespace clause has invalid reader-level syntax shape"
    "C2-MAP" "map literal has odd arity"
    "C2-SET" "literal set contains duplicate entries decidable at read time"
    "C2-METADATA" "metadata is unattached or has invalid reader shape"
    "C2-ABBREV" "reader abbreviation placement is invalid"
    "C2-EXTENSION" "source extension is noncanonical or reader extension is unknown, disallowed, or effect-violating"
    "C2-HASH" "reader artifact identity is unstable or incomplete"
    "reader document coverage failed"))
