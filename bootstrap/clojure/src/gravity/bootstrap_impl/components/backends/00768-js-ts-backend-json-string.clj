

(defn js-ts-backend-json-string
  [value]
  (gravity.backend.js/json-string value))

(defn js-ts-backend-source-map-source
  "Emit a deterministic v3 source map.  Generated lines are conservatively\n  anchored at the beginning of the source unit, while the Gravity extension\n  records the path-neutral content identity and explicit stage2 origin chain.\n  The source text itself is embedded so consumers can recover exact line and\n  Unicode content without consulting a checkout path."
  [javascript source-text]
  (gravity.backend.js/source-map-source #'sha256-hex #'js-ts-backend-json-string javascript source-text))

(defn js-ts-backend-package-source
  [writes-stdout?]
  (gravity.backend.js/package-source writes-stdout?))
