(ns gravity.c5-name-resolution.config)

(def c5-resolution-diagnostic-ids
  ["C5-UNRESOLVED" "C5-AMBIGUOUS" "C5-PRIVATE" "C5-ALIAS" "C5-SHADOW"
   "C5-CYCLE" "C5-CROSS-PROFILE" "C5-CAPABILITY" "C5-TARGET" "C5-FOREIGN"])

(def c5-resolution-governing-document
  "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md")

(def c5-resolution-rejected-designs
  [{:diagnostic "C5-UNRESOLVED" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-unresolved.gravity" :rejected-design :unresolved-symbol}
   {:diagnostic "C5-AMBIGUOUS" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-ambiguous.gravity" :rejected-design :ambiguous-unqualified-symbol}
   {:diagnostic "C5-PRIVATE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-private.gravity" :rejected-design :private-binding-access}
   {:diagnostic "C5-ALIAS" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-alias.gravity" :rejected-design :unknown-or-duplicate-alias}
   {:diagnostic "C5-SHADOW" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-shadow.gravity" :rejected-design :illegal-shadowing}
   {:diagnostic "C5-CYCLE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-cycle.gravity" :rejected-design :namespace-dependency-cycle}
   {:diagnostic "C5-CROSS-PROFILE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-cross-profile.gravity" :rejected-design :cross-profile-edge-without-boundary}
   {:diagnostic "C5-CAPABILITY" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-capability.gravity" :rejected-design :imported-binding-without-capability}
   {:diagnostic "C5-TARGET" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-target.gravity" :rejected-design :target-incompatible-import}
   {:diagnostic "C5-FOREIGN" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c5-foreign.gravity" :rejected-design :malformed-foreign-import-record}])

(def c5-resolution-override-diagnostics
  {:unresolved "C5-UNRESOLVED" :ambiguous "C5-AMBIGUOUS" :private "C5-PRIVATE"
   :alias "C5-ALIAS" :shadow "C5-SHADOW" :cycle "C5-CYCLE"
   :cross-profile "C5-CROSS-PROFILE" :capability "C5-CAPABILITY"
   :target "C5-TARGET" :foreign "C5-FOREIGN"})

(def c5-special-form-symbols
  '#{quote if do let fn loop recur def defn defmacro defschema defprotocol
     syntax-quote unquote splice-unquote unsafe})

(def c5-core-auto-imports
  '#{println + - * / = < > <= >= str pr-str hash-map vector list conj assoc
     get first second rest count})

(def c5-type-auto-imports
  '#{I8 I16 I32 I64 U8 U16 U32 U64 F32 F64 Bool String Symbol Keyword
     Dynamic Unit Never})

(def known-source-profiles
  #{:core :hardware :firmware :kernel :native :hosted :distributed :ai :meta
    :gpu :formal})

(def supported-targets #{:jvm})

(defn c5-resolution-source-overrides [module]
  (get-in module [:metadata :compiler :c5-resolution] {}))

(defn c5-resolution-message [id]
  (case id
    "C5-UNRESOLVED" "symbol has no resolvable binding"
    "C5-AMBIGUOUS" "symbol has multiple legal bindings"
    "C5-PRIVATE" "private binding is accessed outside its namespace boundary"
    "C5-ALIAS" "namespace alias is unknown or duplicated"
    "C5-SHADOW" "lexical binding shadows a namespace binding illegally"
    "C5-CYCLE" "namespace dependency graph contains an illegal cycle"
    "C5-CROSS-PROFILE" "cross-profile import lacks an accepted boundary"
    "C5-CAPABILITY" "imported binding requires an unavailable capability"
    "C5-TARGET" "imported binding is incompatible with the active target"
    "C5-FOREIGN" "foreign import record is malformed"
    "name resolution and namespace analysis failed"))
