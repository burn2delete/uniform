(ns gravity.source-span
  "Hosted source-position metadata primitives.

  This leaf preserves the bootstrap reader's source-position arithmetic:
  line starts and character positions are UTF-16 indices, while byte offsets
  are UTF-8 counts over the corresponding prefix. It does not read source,
  decode input, validate spans, or construct diagnostics. Indexed reader locations
  retain their separate scalar-column labels and explicit late owner Vars.")

(def ^:private namespace-contract
  {:namespace 'gravity.source-span
   :governing-contracts [:D1 :D2 :D3 :D8 :D9 :L1 :C2 :C3 :C15 :BOOT8 :TEST2]
   :contract-boundary :hosted-source-position-metadata
   :owner-call-resolution :per-call-var-root-before-arguments
   :public-api
   {'indexed-source-location
    {:arglists '([byte-count-var source-text line-starts idx])
     :returns :indexed-reader-source-location :admission :none
     :operation-inputs [:byte-count-var] :authenticates-inputs? false :opens-source? false
     :callback-type-checks? false :test-owner 'gravity.source-span-test}
    'indexed-source-span
    {:arglists '([location-var source-path source-text line-starts start end])
     :returns :indexed-reader-source-span :admission :none
     :operation-inputs [:location-var] :authenticates-inputs? false :opens-source? false
     :callback-type-checks? false :test-owner 'gravity.source-span-test}
    'line-terminator-char?
    {:arglists '([ch])
     :returns :boolean}
    'line-start-indices
    {:arglists '([source-text])
     :returns :ordered-utf16-line-start-indices}
    'char-index-at
    {:arglists '([line-starts line column])
     :returns :utf16-character-index}
    'utf8-byte-count
    {:arglists '([text])
     :returns :utf8-byte-count}
    'source-location
    {:arglists '([source-text line-starts line column])
     :returns :source-location}
    'source-span
    {:arglists '([source form-index]
                 [source-path source-text line-starts form-index
                  start-line start-column end-line end-column])
     :returns :source-span}}
   :artifact-inputs [:source-text :line-start-indices :line-column-coordinates
                     :source-path :form-index :indexed-reader-coordinates :byte-count-var :location-var]
   :artifact-outputs [:line-start-indices :source-location :source-span
                      :indexed-reader-source-location :indexed-reader-source-span]
   :ownership
   {:owns [:line-terminator-classification
           :utf16-line-start-indexing
           :utf16-character-index-lookup
           :utf8-prefix-byte-counting
           :source-location-metadata
           :source-span-metadata :indexed-reader-location-metadata :indexed-reader-span-metadata]
    :does-not-own [:source-decoding
                   :source-unit-identity
                   :source-reading
                   :tokenization
                   :syntax-objects
                   :span-validation
                   :diagnostic-construction
                   :diagnostic-policy
                   :reader-state]}
   :dependency-direction
   {:requires ['clojure.core 'java.lang.String
               'java.nio.charset.StandardCharsets]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :test-owner
   'gravity.source-span-test/source-position-namespace-contract-is-narrow-and-acyclic
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn line-terminator-char?
  "Return true for either single-character line terminator accepted by stage0.

  CRLF is classified as two characters here; `line-start-indices` owns the
  pairwise treatment needed to advance over it as one line ending."
  [ch]
  (or (= \newline ch) (= \return ch)))

(defn line-start-indices
  "Return UTF-16 indices at which source lines begin.

  The result always starts with zero. LF, CR, and CRLF advance to the first
  index after their terminator; CRLF contributes one line start, not two."
  [^String source-text]
  (let [source-length (count source-text)]
    (loop [idx 0
           starts [0]]
      (if (>= idx source-length)
        starts
        (let [ch (.charAt source-text idx)]
          (cond
            (= \return ch)
            (if (and (< (inc idx) source-length)
                     (= \newline (.charAt source-text (inc idx))))
              (recur (+ idx 2) (conj starts (+ idx 2)))
              (recur (inc idx) (conj starts (inc idx))))

            (= \newline ch)
            (recur (inc idx) (conj starts (inc idx)))

            :else
            (recur (inc idx) starts)))))))

(defn char-index-at
  "Return the UTF-16 source index for one-based line and column coordinates.

  This intentionally retains bootstrap boundary behavior: unknown lines use
  a zero base, and columns below one clamp their offset to zero. No coordinate
  validation or line-start normalization is performed here."
  [line-starts line column]
  (+ (get line-starts (dec line) 0) (max 0 (dec column))))

(defn utf8-byte-count
  "Return the Java UTF-8 byte count for text using bootstrap encoding rules."
  [text]
  (alength (.getBytes ^String text
                      java.nio.charset.StandardCharsets/UTF_8)))

(defn- utf8-prefix-byte-count
  [^String text end]
  ;; Match Java's UTF-8 replacement byte for an unpaired surrogate, including
  ;; a prefix that ends between a valid pair. Do not allocate the prefix text
  ;; and its encoded byte array for every hosted-reader source position.
  (if (or (nil? text) (neg? end))
    (utf8-byte-count (subs text 0 end))
    (let [end (int end)]
      (loop [index 0 total 0]
        (if (= index end)
          total
          (let [unit (int (.charAt text index))]
            (cond
              (<= unit 0x7f) (recur (inc index) (inc total))
              (<= unit 0x7ff) (recur (inc index) (+ total 2))
              (and (<= 0xd800 unit 0xdbff) (< (inc index) end)
                   (<= 0xdc00 (int (.charAt text (inc index))) 0xdfff))
              (recur (+ index 2) (+ total 4))
              (<= 0xd800 unit 0xdfff) (recur (inc index) (inc total))
              :else (recur (inc index) (+ total 3)))))))))

(defn source-location
  "Build a source location with one-based coordinates and prefix offsets."
  [source-text line-starts line column]
  (let [char-index (min (count source-text)
                        (char-index-at line-starts line column))]
    {:line line
     :column column
     :char char-index
     :byte (utf8-prefix-byte-count source-text char-index)}))

(defn source-span
  "Build either a compact source/form reference or a byte-aware source span.

  The two-argument form intentionally returns only source and form identity;
  the eight-argument form retains the historical location and byte fields."
  ([source form-index]
   {:source source :form-index form-index})
  ([source-path source-text line-starts form-index start-line start-column
    end-line end-column]
   (let [start (source-location source-text line-starts start-line start-column)
         end (source-location source-text line-starts end-line end-column)]
     {:source source-path
      :form-index form-index
      :start start
      :end end
      :byte-start (:byte start)
      :byte-end (:byte end)})))

(defn indexed-source-location
  [byte-count-var source-text line-starts idx]
  (let [idx (min idx (count source-text))
        line-number (loop [line 0]
                      (if (and (< (inc line) (count line-starts))
                               (<= (line-starts (inc line)) idx))
                        (recur (inc line))
                        (inc line)))
        line-start (line-starts (dec line-number))
        line-prefix (subs source-text line-start idx)
        column (inc (.codePointCount line-prefix 0 (.length line-prefix)))]
    {:line line-number
     :column column
     :column-unit :unicode-scalar
     :char idx
     :byte ((deref byte-count-var) (subs source-text 0 idx))}))


(defn indexed-source-span
  [location-var source-path source-text line-starts start end]
  (let [start-location ((deref location-var) source-text line-starts start)
        end-location ((deref location-var) source-text line-starts end)]
    {:source source-path
     :start start-location
     :end end-location
     :byte-start (:byte start-location)
     :byte-end (:byte end-location)}))
