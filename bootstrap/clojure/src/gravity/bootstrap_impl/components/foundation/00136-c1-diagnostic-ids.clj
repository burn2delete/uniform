

(def c1-diagnostic-ids
  ["C1-PIPELINE" "C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
   "C1-UNCHECKED-BACKEND" "C1-MANIFEST"])

(def c1-architecture-diagnostic-ids
  (vec (concat c1-diagnostic-ids ["C1-DOMAIN-ANCHOR" "C1-SELF-HOST"])))