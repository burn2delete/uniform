(ns gravity.c9-ownership-checker.catalog
  "Stable C9 diagnostic and override catalog for the hosted facade.")

(def diagnostic-ids
  ["C9-USE-AFTER-MOVE" "C9-USE-AFTER-CONSUME" "C9-BORROW-ESCAPE"
   "C9-MUT-ALIAS" "C9-MOVE-WHILE-BORROWED" "C9-REGION-ESCAPE"
   "C9-ARENA-GENERATION" "C9-LINEAR-LEAK" "C9-LINEAR-DOUBLE"
   "C9-TRANSFER" "C9-RUNTIME-CHECK" "C9-UNSAFE"])

(def governing-document
  "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md")

(def rejected-designs
  [{:diagnostic "C9-USE-AFTER-MOVE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-use-after-move.gravity" :rejected-design :use-after-move}
   {:diagnostic "C9-USE-AFTER-CONSUME" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-use-after-consume.gravity" :rejected-design :use-after-terminal-consume}
   {:diagnostic "C9-BORROW-ESCAPE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-borrow-escape.gravity" :rejected-design :borrow-outlives-valid-scope}
   {:diagnostic "C9-MUT-ALIAS" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-mut-alias.gravity" :rejected-design :mutable-access-while-aliased}
   {:diagnostic "C9-MOVE-WHILE-BORROWED" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-move-while-borrowed.gravity" :rejected-design :move-during-active-borrow}
   {:diagnostic "C9-REGION-ESCAPE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-region-escape.gravity" :rejected-design :region-value-escapes}
   {:diagnostic "C9-ARENA-GENERATION" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-arena-generation.gravity" :rejected-design :stale-arena-generation}
   {:diagnostic "C9-LINEAR-LEAK" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-linear-leak.gravity" :rejected-design :missing-terminal-resource-state}
   {:diagnostic "C9-LINEAR-DOUBLE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-linear-double.gravity" :rejected-design :duplicate-terminal-resource-state}
   {:diagnostic "C9-TRANSFER" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-transfer.gravity" :rejected-design :invalid-ownership-transfer}
   {:diagnostic "C9-RUNTIME-CHECK" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-runtime-check.gravity" :rejected-design :runtime-check-unavailable}
   {:diagnostic "C9-UNSAFE" :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-unsafe.gravity" :rejected-design :manual-lifetime-or-resource-flow-without-audit}])

(def override-diagnostics
  {:use-after-move "C9-USE-AFTER-MOVE" :use-after-consume "C9-USE-AFTER-CONSUME"
   :borrow-escape "C9-BORROW-ESCAPE" :mut-alias "C9-MUT-ALIAS"
   :move-while-borrowed "C9-MOVE-WHILE-BORROWED" :region-escape "C9-REGION-ESCAPE"
   :arena-generation "C9-ARENA-GENERATION" :linear-leak "C9-LINEAR-LEAK"
   :linear-double "C9-LINEAR-DOUBLE" :transfer "C9-TRANSFER"
   :runtime-check "C9-RUNTIME-CHECK" :unsafe "C9-UNSAFE"})

(defn ownership-message [id]
  (case id
    "C9-USE-AFTER-MOVE" "owned value is used after move"
    "C9-USE-AFTER-CONSUME" "linear or owned value is used after terminal consumption"
    "C9-BORROW-ESCAPE" "borrow outlives owner, region, provider, callback, or task scope"
    "C9-MUT-ALIAS" "mutable access overlaps active aliases"
    "C9-MOVE-WHILE-BORROWED" "owner is moved while an active borrow exists"
    "C9-REGION-ESCAPE" "region value escapes its valid lifetime"
    "C9-ARENA-GENERATION" "arena value is used after reset generation invalidation"
    "C9-LINEAR-LEAK" "linear resource may miss a terminal operation"
    "C9-LINEAR-DOUBLE" "linear resource may reach multiple terminal operations"
    "C9-TRANSFER" "ownership transfer lacks explicit destination cleanup or lifetime proof"
    "C9-RUNTIME-CHECK" "required dynamic ownership check is unavailable in the active profile"
    "C9-UNSAFE" "manual lifetime, alias, or resource behavior lacks unsafe audit evidence"
    "Ownership checking failed"))
