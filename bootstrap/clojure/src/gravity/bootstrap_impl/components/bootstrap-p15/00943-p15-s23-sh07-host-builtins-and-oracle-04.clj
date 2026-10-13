
(defn p15-s23-sh07-p-decimal-inverse [text]
  (when-not (and (string? text) (<= 1 (count text) 261)
                 (every? #(<= (int %) 127) text))
    (throw (ex-info "malformed producer decimal inverse input"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= text "0E+0")
    {:kind :decimal :sign 1 :coefficient [48] :decimal-power 0}
    (let [characters (vec text)
          negative? (= \- (first characters))
          start (if negative? 1 0)
          first-digit (get characters start)
          e-index (.indexOf ^java.util.List characters \E)]
      (when-not (and first-digit (<= (int \1) (int first-digit) (int \9))
                     (> e-index start) (= e-index (.lastIndexOf ^java.util.List characters \E)))
        (throw (ex-info "noncanonical producer decimal mantissa"
                        {:reason :unsupported-decimal-readable-spelling})))
      (let [mantissa (subvec characters start e-index)
            exponent-part (subvec characters (inc e-index))
            dotted? (and (> (count mantissa) 1) (= \. (second mantissa)))
            coefficient-chars (if dotted? (into [(first mantissa)] (subvec mantissa 2)) mantissa)
            exponent-sign (first exponent-part)
            exponent-chars (subvec exponent-part 1)]
        (when-not
         (and (or (= 1 (count mantissa))
                  (and dotted? (> (count mantissa) 2)))
              (every? #(<= (int \0) (int %) (int \9)) coefficient-chars)
              (not= \0 (last coefficient-chars))
              (contains? #{\- \+} exponent-sign)
              (seq exponent-chars)
              (every? #(<= (int \0) (int %) (int \9)) exponent-chars)
              (or (= 1 (count exponent-chars)) (not= \0 (first exponent-chars))))
          (throw (ex-info "noncanonical producer decimal grammar"
                          {:reason :unsupported-decimal-readable-spelling})))
        (let [coefficient (mapv int coefficient-chars)
              exponent
              (p15-s23-sh07-p-decimal-signed
               (= \- exponent-sign) (mapv int exponent-chars))
              adjustment
              (p15-s23-sh07-p-decimal-signed
               true (p15-s23-sh07-p-decimal-small-digits (dec (count coefficient))))
              key {:kind :decimal
                   :sign (if negative? -1 1)
                   :coefficient coefficient
                   :decimal-power
                   (p15-s23-sh07-p-decimal-power-value
                    (p15-s23-sh07-p-decimal-signed-add exponent adjustment))}]
          (when-not (= text (p15-s23-sh07-p-decimal-spell key))
            (throw (ex-info "producer decimal inverse does not respell"
                            {:reason :unsupported-decimal-readable-spelling})))
          key)))))

(declare c2-reader-artifact-id
         sh03-reader-final-decimal-occurrence-projection-valid?)

(defn- p15-s23-sh07-p-decimal-occurrence
  [evidence]
  (when-not (and (map? evidence)
                 (= #{:c2-reader-artifact :adapted-form-id
                      :adapted-token-id}
                    (set (keys evidence))))
    (throw (ex-info "producer decimal projection evidence is malformed"
                    {:reason :unsupported-decimal-readable-spelling})))
  (let [artifact (:c2-reader-artifact evidence)
        form-id (:adapted-form-id evidence)
        token-id (:adapted-token-id evidence)
        projection (:sh07-decimal-occurrence-projection artifact)
        selected
        (filterv #(and (= form-id (:adapted-form-id %))
                       (= token-id (:adapted-token-id %)))
                 (:occurrences projection))]
    (when-not
     (and (= (:artifact-id artifact) (c2-reader-artifact-id artifact))
          (sh03-reader-final-decimal-occurrence-projection-valid? artifact)
          (= 1 (count selected)))
      (throw (ex-info "producer decimal projection authentication failed"
                      {:reason :unsupported-decimal-readable-spelling})))
    (first selected)))

(defn- p15-s23-sh07-p-decimal-occurrence-raw
  [evidence occurrence]
  (let [form-id (:adapted-form-id occurrence)
        token-id (:adapted-token-id occurrence)
        forms
        (filterv #(and (= form-id (:form-id %))
                       (= token-id (:open-token %)))
                 (get-in evidence [:c2-reader-artifact :form-tree]))
        literals
        (filterv #(= form-id (:form-id %))
                 (get-in evidence
                         [:c2-reader-artifact :literal-decoding-records]))]
    (when-not (and (= 1 (count forms)) (= 1 (count literals))
                   (= (:raw (first forms)) (:raw (first literals))))
      (throw (ex-info "producer decimal occurrence text is not unique"
                      {:reason :unsupported-decimal-readable-spelling})))
    (:raw (first forms))))

(defn- p15-s23-sh07-p-decimal-occurrence-identity
  [evidence occurrence]
  [(get-in evidence [:c2-reader-artifact :artifact-id])
   (:adapted-form-id occurrence)
   (:adapted-token-id occurrence)])

(defn- p15-s23-sh07-p-decimal-c2-evidence?
  [text key evidence]
  (try
    (let [occurrence (p15-s23-sh07-p-decimal-occurrence evidence)
          entry (:semantic-value-entry occurrence)
          descriptor (:descriptor entry)]
      (and (= text (p15-s23-sh07-p-decimal-occurrence-raw
                    evidence occurrence))
           (if (<= (count text) 256)
        (and (p15-s23-sh07-p-decimal-key? (:semantic-key entry))
             (= key (:semantic-key entry) (:semantic-key descriptor))
             (nil? (:normalization-reason descriptor)))
        (and (= :deferred (:semantic-validation descriptor))
             (= :reader-semantic-work-boundary
                (:normalization-reason descriptor))
             (= {:kind :decimal
                 :normalization :reader-semantic-work-boundary
                 :bounded-semantic-key (:semantic-key descriptor)}
                (:semantic-key entry))))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch Throwable _ false)))

(defn p15-s23-sh07-p-decimal-c2-branch [text key evidence]
  (let [inverse (p15-s23-sh07-p-decimal-inverse text)]
    (when-not (and (= key inverse)
                   (p15-s23-sh07-p-decimal-c2-evidence? text key evidence))
      (throw (ex-info "producer decimal C2 evidence mismatch"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (<= (count text) 256)
      {:status :exact-normalized :semantic-key key}
      {:status :normalization-deferred
       :normalization-reason :reader-semantic-work-boundary
       :semantic-key key})))

(defn p15-s23-sh07-p-decimal-decoration [key evidence]
  (let [text (p15-s23-sh07-p-decimal-spell key)
        branch (p15-s23-sh07-p-decimal-c2-branch text key evidence)]
    (when-not (= key (:semantic-key branch))
      (throw (ex-info "producer decimal branch lost semantic identity"
                      {:reason :unsupported-decimal-readable-spelling})))
    text))

(defn p15-s23-sh07-p-canonical-decimal-decoration
  [source-evidence replay-evidence]
  (let [occurrence (p15-s23-sh07-p-decimal-occurrence source-evidence)
        replay-occurrence
        (p15-s23-sh07-p-decimal-occurrence replay-evidence)
        key (get-in occurrence [:semantic-value-entry :semantic-key])]
    (when-not (p15-s23-sh07-p-decimal-key? key)
      (throw (ex-info "producer source occurrence has no exact decimal key"
                      {:reason :unsupported-decimal-readable-spelling})))
    (when (= (p15-s23-sh07-p-decimal-occurrence-identity
              source-evidence occurrence)
             (p15-s23-sh07-p-decimal-occurrence-identity
              replay-evidence replay-occurrence))
      (throw (ex-info "producer decimal replay must be a fresh occurrence"
                      {:reason :unsupported-decimal-readable-spelling})))
    (p15-s23-sh07-p-decimal-decoration key replay-evidence)))

;; Independently authored source-oracle decimal algebra.  Keep changes here
;; mechanically separate from the producer model above.
(defn- p15-s23-sh07-o-decimal-digits? [digits]
  (and (vector? digits) (seq digits)
       (loop [remaining digits]
         (if (empty? remaining) true
             (let [code (first remaining)]
               (if (and (integer? code) (<= 48 code 57))
                 (recur (rest remaining)) false))))))

(defn- p15-s23-sh07-o-decimal-compare [left right]
  (if (= (count left) (count right))
    (loop [l left r right]
      (if (empty? l) 0
          (cond (< (first l) (first r)) -1
                (> (first l) (first r)) 1
                :else (recur (rest l) (rest r)))))
    (if (< (count left) (count right)) -1 1)))

(defn- p15-s23-sh07-o-decimal-plus [left right]
  (loop [l (reverse left) r (reverse right) carry 0 result []]
    (if (and (empty? l) (empty? r) (zero? carry))
      (vec (reverse result))
      (let [total (+ carry
                     (if (empty? l) 0 (- (first l) 48))
                     (if (empty? r) 0 (- (first r) 48)))]
        (recur (rest l) (rest r) (quot total 10)
               (conj result (+ 48 (mod total 10))))))))

(defn- p15-s23-sh07-o-decimal-minus [larger smaller]
  (loop [l (reverse larger) r (reverse smaller) borrow 0 result []]
    (if (empty? l)
      (let [forward (reverse result)
            normalized (drop-while #(= 48 %) forward)]
        (vec (if (seq normalized) normalized [48])))
      (let [raw (- (- (first l) 48) borrow)
            subtrahend (if (empty? r) 0 (- (first r) 48))
            take? (< raw subtrahend)]
        (recur (rest l) (rest r) (if take? 1 0)
               (conj result (+ 48 (- (if take? (+ raw 10) raw)
                                      subtrahend))))))))

(defn- p15-s23-sh07-o-decimal-normal-signed [sign digits]
  {:sign (if (= digits [48]) :positive sign) :digits digits})

(defn- p15-s23-sh07-o-decimal-sum [left right]
  (if (= (:sign left) (:sign right))
    (p15-s23-sh07-o-decimal-normal-signed
     (:sign left) (p15-s23-sh07-o-decimal-plus (:digits left) (:digits right)))
    (let [order (p15-s23-sh07-o-decimal-compare (:digits left) (:digits right))]
      (cond
        (zero? order) (p15-s23-sh07-o-decimal-normal-signed :positive [48])
        (pos? order)
        (p15-s23-sh07-o-decimal-normal-signed
         (:sign left) (p15-s23-sh07-o-decimal-minus (:digits left) (:digits right)))
        :else
        (p15-s23-sh07-o-decimal-normal-signed
         (:sign right) (p15-s23-sh07-o-decimal-minus (:digits right) (:digits left)))))))

(defn- p15-s23-sh07-o-decimal-count-digits [value]
  (mapv int (str value)))

(defn- p15-s23-sh07-o-decimal-power [power]
  (if (and (integer? power)
           (<= -999999999999999999 power 999999999999999999))
    (let [negative? (neg? power)
          digits (mapv int (str (if negative? (- power) power)))]
      (when (<= (count digits) 18)
        (p15-s23-sh07-o-decimal-normal-signed
         (if negative? :negative :positive) digits)))
    (when (and (map? power)
               (= #{:artifact :sign :digit-codepoints} (set (keys power)))
               (= :gravity/signed-decimal-power (:artifact power))
               (contains? #{:negative :positive} (:sign power))
               (vector? (:digit-codepoints power))
               (<= (count (:digit-codepoints power)) 258)
               (p15-s23-sh07-o-decimal-digits? (:digit-codepoints power))
               (not= 48 (first (:digit-codepoints power))))
      (p15-s23-sh07-o-decimal-normal-signed
       (:sign power) (:digit-codepoints power)))))
