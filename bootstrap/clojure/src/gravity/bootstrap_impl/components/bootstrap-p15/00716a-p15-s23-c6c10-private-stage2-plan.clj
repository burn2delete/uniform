(defn- p15-s23-c6c10-private-stage2-plan
  [emitter source-path source-text authoritative-module]
  ;; The module is parsed exactly once from genuine C2/C3 forms.  Invoke only
  ;; the pinned Gravity compiler artifact's construction seam: public P1/math,
  ;; effect, capability, and safety admission belongs after this boundary to
  ;; the Gravity C6-C10 module.
  (let [function-table (stage0-function-table authoritative-module)]
    (p15-s23-stage2-construct-emitted-core-plan
     emitter source-path source-text authoritative-module function-table)))
