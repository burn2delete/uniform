
(defn- mask-span-for-rhs!
  [^bytes source coordinate-ordinal coordinate rhs]
  (let [full (mask-form-bytes (:left-delimiter coordinate) rhs)
        offsets (byte-match-offsets source full)]
    (when-not (= 1 (count offsets))
      (fail! "SH07-B51-MASKED-ROOT" :masked-occurrence-census
             {:coordinate-ordinal coordinate-ordinal
              :count (count offsets)}))
    (let [form-start (inc (first offsets))
          left-byte-count
          (alength (.getBytes ^String (:left-delimiter coordinate)
                              StandardCharsets/UTF_8))
          rhs-start (+ form-start left-byte-count)
          rhs-end (+ rhs-start (alength ^bytes rhs))
          form-end (inc rhs-end)]
      {:coordinate-ordinal coordinate-ordinal
       :form-start-byte form-start
       :rhs-start-byte rhs-start
       :rhs-end-byte rhs-end
       :form-end-byte form-end
       :rhs-byte-count (alength ^bytes rhs)
       :rhs-raw-sha256 (codec/R rhs)})))

(defn- validate-mask-span!
  [span coordinate-ordinal expected-rhs id reason]
  (exact-keys! span mask-span-keys id reason)
  (let [{:keys [form-start-byte rhs-start-byte rhs-end-byte form-end-byte
                rhs-byte-count rhs-raw-sha256]} span]
    (when-not
     (and (= coordinate-ordinal (:coordinate-ordinal span))
          (every? #(and (integer? %) (not (neg? %)))
                  [form-start-byte rhs-start-byte rhs-end-byte form-end-byte
                   rhs-byte-count])
          (<= form-start-byte rhs-start-byte)
          (< rhs-start-byte rhs-end-byte)
          (<= rhs-end-byte form-end-byte)
          (= (alength ^bytes expected-rhs) rhs-byte-count)
          (= (codec/R expected-rhs) rhs-raw-sha256))
      (fail! id reason
             {:coordinate-ordinal coordinate-ordinal :span span}))
    span))

(defn- validate-mask-coordinates!
  [coordinates id reason]
  (when-not (and (vector? coordinates) (= 5 (count coordinates)))
    (fail! id reason {:coordinates coordinates}))
  (doseq [[ordinal coordinate] (map-indexed vector coordinates)]
    (exact-keys! coordinate mask-coordinate-keys id reason)
    (when-not (= coordinate (nth (expected-mask-coordinates) ordinal))
      (fail! id reason {:coordinate-ordinal ordinal
                        :coordinate coordinate})))
  coordinates)

(defn- validate-mask-files!
  [files coordinates id reason]
  (when-not (and (vector? files) (= 2 (count files)))
    (fail! id reason {:files files}))
  (let [expected-paths (vec (distinct (map :path coordinates)))]
    (doseq [[ordinal [expected-path descriptor]]
            (map-indexed vector (map vector expected-paths files))]
      (exact-keys! descriptor mask-file-keys id reason)
      (when-not
       (and (= expected-path (:path descriptor))
            (integer? (:masked-byte-count descriptor))
            (not (neg? (:masked-byte-count descriptor)))
            (sha256-id? (:masked-raw-sha256 descriptor)))
        (fail! id reason {:file-ordinal ordinal :descriptor descriptor}))))
  files)

(defn- masked-source-root
  [coordinates files]
  (codec/H [masked-source-root-domain
            {:coordinates coordinates :files files}]))

(defn validate-pre-masked-root!
  "Validate the exact acyclic four-field PRE-MASKED-ROOT schema.

  This validator checks the closed coordinate/span/file projections and
  recomputes PREIMAGE-ROOT.  It does not admit runtime/postimage fields."
  [root]
  (exact-keys! root pre-masked-root-keys
               "SH07-B51-MASKED-ROOT" :pre-masked-root-keys)
  (validate-mask-coordinates! (:coordinates root)
                              "SH07-B51-MASKED-ROOT" :pre-masked-root-coordinates)
  (let [coordinates (:coordinates root)
        pre-spans (:preimage-spans root)
        pre-files (:preimage-files root)]
    (when-not (and (vector? pre-spans) (= 5 (count pre-spans)))
      (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-spans
             {:spans pre-spans}))
    (doseq [[ordinal span] (map-indexed vector pre-spans)]
      (validate-mask-span! span ordinal provisional-rhs-bytes
                           "SH07-B51-MASKED-ROOT" :pre-masked-root-span))
    (validate-mask-files! pre-files coordinates
                          "SH07-B51-MASKED-ROOT" :pre-masked-root-files)
    (when-not (and (sha256-id? (:preimage-root root))
                   (= (:preimage-root root)
                      (masked-source-root coordinates pre-files)))
      (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-hash
             {:root (:preimage-root root)})))
  root)

(defn validate-runtime-masked-root!
  "Validate the complete eight-field RUNTIME-MASKED-ROOT.

  With PRE supplied, the preimage projection is also compared byte-for-byte
  with Q's authenticated PRE-MASKED-ROOT.  Runtime roots must carry a true
  independently recomputed pre/post equality bit; a PRE-only or legacy
  `:masked-root` map therefore cannot pass this validator."
  ([root]
   (validate-runtime-masked-root! root nil))
  ([root pre-masked-root]
   (exact-keys! root runtime-masked-root-keys
                "SH07-B51-MASKED-ROOT" :runtime-masked-root-keys)
   (validate-mask-coordinates! (:coordinates root)
                               "SH07-B51-MASKED-ROOT"
                               :runtime-masked-root-coordinates)
   (let [projection (select-keys root pre-masked-root-keys)
         coordinates (:coordinates root)
         post-spans (:postimage-spans root)
         post-files (:postimage-files root)]
     (validate-pre-masked-root! projection)
     (when (some? pre-masked-root)
       (validate-pre-masked-root! pre-masked-root)
       (when-not (Arrays/equals ^bytes (codec/W projection)
                                ^bytes (codec/W pre-masked-root))
         (fail! "SH07-B51-MASKED-ROOT" :pre-projection-mismatch)))
     (when-not (and (vector? post-spans) (= 5 (count post-spans)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-spans
              {:spans post-spans}))
     (doseq [[ordinal span] (map-indexed vector post-spans)]
       ;; Post RHS bytes are authenticated by the runtime caller.  The root
       ;; validator checks their observational shape and raw-byte identity;
       ;; the constructor below additionally checks the exact four pins and
       ;; quote form before producing these spans.
       (exact-keys! span mask-span-keys
                    "SH07-B51-MASKED-ROOT" :runtime-masked-root-span)
       (when-not
        (and (= ordinal (:coordinate-ordinal span))
             (every? #(and (integer? %) (not (neg? %)))
                     (map span [:form-start-byte :rhs-start-byte
                                :rhs-end-byte :form-end-byte :rhs-byte-count]))
             (<= (:form-start-byte span) (:rhs-start-byte span))
             (< (:rhs-start-byte span) (:rhs-end-byte span))
             (<= (:rhs-end-byte span) (:form-end-byte span))
             (sha256-id? (:rhs-raw-sha256 span)))
         (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-span
                {:coordinate-ordinal ordinal :span span})))
     (validate-mask-files! post-files coordinates
                           "SH07-B51-MASKED-ROOT"
                           :runtime-masked-root-files)
     (when-not (and (sha256-id? (:postimage-root root))
                    (= (:postimage-root root)
                       (masked-source-root coordinates post-files)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-hash
              {:root (:postimage-root root)}))
     (when-not (and (= true (:equal root))
                    (= (:preimage-root root) (:postimage-root root)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-equality
              {:preimage-root (:preimage-root root)
               :postimage-root (:postimage-root root)
               :equal (:equal root)})))
   root))

(defn- validate-runtime-pin-map!
  [pin-map]
  (exact-keys! pin-map runtime-pin-map-keys
               "SH07-B51-MASKED-ROOT" :runtime-pin-map-keys)
  (when-not (every? sha256-id? (map pin-map runtime-pin-order))
    (fail! "SH07-B51-MASKED-ROOT" :runtime-pin-map-values
           {:pin-map pin-map}))
  pin-map)

(defn- carrier-value
  [carrier]
  (if (and (seq? carrier) (= 2 (count carrier))
           (= 'quote (first carrier)))
    (second carrier)
    carrier))

(defn- quoted-carrier-bytes!
  [carrier]
  (let [value (carrier-value carrier)]
    (when-not (map? value)
      (fail! "SH07-B51-MASKED-ROOT" :quoted-carrier-shape
             {:carrier carrier}))
    (let [wire (codec/W (list 'quote value))
          length (alength ^bytes wire)]
      (when-not (and (pos? length) (= 10 (aget wire (dec length))))
        (fail! "SH07-B51-MASKED-ROOT" :quoted-carrier-wire))
      (Arrays/copyOf ^bytes wire (dec length)))))

(defn- validate-mask-source-input!
  [bytes-by-path decoded-static]
  (when-not (and (map? bytes-by-path) (map? decoded-static))
    (fail! "SH07-B51-MASKED-ROOT" :source-input-shape))
  (doseq [{:keys [path]} pre-mask-specs]
    (let [bytes (get bytes-by-path path)
          text (get decoded-static path)]
      (when-not (bytes? bytes)
        (fail! "SH07-B51-MASKED-ROOT" :source-bytes
               {:path path}))
      (when-not (string? text)
        (fail! "SH07-B51-MASKED-ROOT" :decoded-source
               {:path path}))
      (when-not (= text (decode-utf8! bytes "SH07-B51-MASKED-ROOT"
                                       :source-utf8))
        (fail! "SH07-B51-MASKED-ROOT" :decoded-source-mismatch
               {:path path}))))
  [bytes-by-path decoded-static])

(defn- replace-runtime-rhs!
  [bytes-by-path coordinates pre-spans rhs-bytes]
  (let [ordinals
        (sort-by
         (fn [ordinal]
           [(:path (nth coordinates ordinal))
            (:rhs-start-byte (nth pre-spans ordinal))])
         #(compare %2 %1)
         (range 5))]
    (reduce
     (fn [sources ordinal]
       (let [coordinate (nth coordinates ordinal)
             span (nth pre-spans ordinal)
             path (:path coordinate)
             source (get sources path)]
         (when-not (byte-match-at? source provisional-rhs-bytes
                                    (:rhs-start-byte span))
           (fail! "SH07-B51-MASKED-ROOT" :runtime-preimage-rhs
                  {:coordinate-ordinal ordinal :path path}))
         (assoc sources path
                (replace-byte-span source (:rhs-start-byte span)
                                   (:rhs-end-byte span)
                                   (nth rhs-bytes ordinal)))))
     bytes-by-path ordinals)))
