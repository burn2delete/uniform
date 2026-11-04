(ns seedless.schema
  "Bootstrap work descriptions, distinct from migration or release acceptance."
  (:require [simplify.schema :as s]))

(def stages (mapv #(format "SH-%02d" %) (range 30)))
(def kinds ["implementation" "adoption" "seed-removal" "rebuild" "equivalence" "release"])
(def workstreams ["compiler" "runtime" "build"])
(def texts (s/array-of s/text))
(def dependencies
  (assoc texts :description
         "Exact slice keys from existing or same-plan slices; use prose in proposal/evidence instead."))
(def stack-parent
  {:type "string"
   :description
   "Empty string for a standalone slice, otherwise exactly one key from dependencies; never prose, a branch, or a PR."})
(def slice
  (s/object-of
   {:key s/text :title s/text :stage {:type "string" :enum stages}
    :workstream {:type "string" :enum workstreams}
    :kind {:type "string" :enum kinds}
    :evidence s/text :seed-paths texts :uniform-paths texts :consumer s/text
    :proposal s/text :preserve s/text :positive s/text :negative s/text
    :residual-host-boundary s/text
    :paths (assoc texts :minItems 1) :dependencies dependencies
    :stack-on stack-parent}))
(def plan
  (s/object-of {:coverage (assoc texts :minItems 1)
                :source-closure (assoc texts :minItems 1)
                :target s/text :subset s/text :summary s/text
                :blockers texts :slices (s/array-of slice)}))
(def implementation
  (s/object-of {:outcome {:type "string" :enum ["checkpoint" "ready" "needs_input" "restricted"]}
                :summary s/text :evidence s/text :residual-host-boundary s/text
                :checks (s/array-of (assoc texts :minItems 1))}))
(def review
  (s/object-of {:verdict {:type "string" :enum ["approved" "changes_requested" "needs_input"]}
                :base s/text :candidate s/text :tree s/text
                :evidence-scope {:type "string" :enum (conj kinds "completion")}
                :summary s/text :findings texts :residual-host-boundary s/text}))
