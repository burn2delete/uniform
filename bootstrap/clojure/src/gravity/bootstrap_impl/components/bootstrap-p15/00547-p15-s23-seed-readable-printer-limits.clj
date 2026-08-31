

;; This printer is intentionally a Clojure-seed compatibility boundary.  It
;; prevents host reader/printer implementation details (notably BigInt's `N`
;; suffix and ambient print vars) from becoming Gravity output semantics, but
;; it does not claim that readable printing has moved into Gravity source.
(def p15-s23-seed-readable-printer-limits
  {:maximum-arguments 256
   :maximum-nodes 4096
   :maximum-depth 96
   :maximum-collection-width 512
   :maximum-integer-bits 4096
   :maximum-scalar-bytes 32768
   :maximum-output-bytes 262144})

(def p15-s23-seed-readable-printer-scalar-class-kinds
  {java.lang.Boolean :boolean
   java.lang.Byte :integer
   java.lang.Short :integer
   java.lang.Integer :integer
   java.lang.Long :integer
   java.math.BigInteger :integer
   clojure.lang.BigInt :integer
   clojure.lang.Ratio :ratio
   java.lang.Double :floating
   java.lang.String :string
   java.lang.Character :character
   clojure.lang.Keyword :keyword
   clojure.lang.Symbol :symbol
   java.util.Date :instant
   java.util.UUID :uuid})

(def p15-s23-seed-readable-printer-collection-class-kinds
  {clojure.lang.PersistentVector :vector
   clojure.lang.MapEntry :vector
   clojure.lang.PersistentList :list
   clojure.lang.PersistentList$EmptyList :list
   clojure.lang.PersistentArrayMap :map
   clojure.lang.PersistentHashMap :map
   clojure.lang.PersistentHashSet :set})