

(defn js-ts-backend-json-string
  [value]
  (str "\""
       (apply str
              (map (fn [ch]
                     (case ch
                       \" "\\\""
                       \\ "\\\\"
                       \backspace "\\b"
                       \formfeed "\\f"
                       \newline "\\n"
                       \return "\\r"
                       \tab "\\t"
                       (if (< (int ch) 32)
                         (format "\\u%04x" (int ch))
                         (str ch))))
                   (str value)))
       "\""))

(defn js-ts-backend-source-map-source
  "Emit a deterministic v3 source map.  Generated lines are conservatively
  anchored at the beginning of the source unit, while the Gravity extension
  records the path-neutral content identity and explicit stage2 origin chain.
  The source text itself is embedded so consumers can recover exact line and
  Unicode content without consulting a checkout path."
  [javascript source-text]
  (let [generated-lines (max 1 (count (str/split javascript #"\n" -1)))
        mappings (str/join ";" (repeat generated-lines "AAAA"))
        source-hash (str "sha256:" (sha256-hex source-text))
        entries
        (str/join
         ","
         (map (fn [line]
                (str "{\"generatedLine\":" line
                     ",\"generatedColumn\":0,\"sourceLine\":1"
                     ",\"sourceColumn\":0,\"originChain\":["
                     "\"source-unit\",\"stage2-plan-emitter\","
                     "\"stage2-compiler-driver\",\"js-ts-lowering\"]}"))
              (range 1 (inc generated-lines))))]
    (str "{\"version\":3,\"file\":\"program.mjs\","
         "\"sources\":[\"gravity-source\"],\"sourcesContent\":["
         (js-ts-backend-json-string source-text)
         "],\"names\":[],\"mappings\":"
         (js-ts-backend-json-string mappings)
         ",\"x_gravity\":{\"sourceContentHash\":"
         (js-ts-backend-json-string source-hash)
         ",\"sourceKind\":\"co-canonical-gravity-source\","
         "\"coverage\":\"source-unit-only\","
         "\"perFormOriginPreserved\":false,"
         "\"generatedOrigins\":[" entries "]}}\n")))

(defn js-ts-backend-package-source
  [writes-stdout?]
  (str "{\"name\":\"gravity-js-ts-artifact\",\"private\":true,"
       "\"type\":\"module\",\"sideEffects\":"
       (if writes-stdout? "true" "false")
       ",\"engines\":{\"node\":\">=20 <21\"}}\n"))