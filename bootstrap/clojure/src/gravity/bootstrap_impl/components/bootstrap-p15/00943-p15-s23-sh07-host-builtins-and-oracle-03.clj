
(defn- p15-s23-sh07-p-decimal-magnitude-compare [left right]
  (cond
    (< (count left) (count right)) -1
    (> (count left) (count right)) 1
    :else
    (loop [index 0]
      (if (= index (count left)) 0
          (let [l (nth left index) r (nth right index)]
            (cond (< l r) -1 (> l r) 1 :else (recur (inc index))))))))

(defn- p15-s23-sh07-p-decimal-add-magnitudes [left right]
  (loop [li (dec (count left)) ri (dec (count right)) carry 0 out ()]
    (if (and (neg? li) (neg? ri) (zero? carry))
      (vec out)
      (let [l (if (neg? li) 0 (- (nth left li) 48))
            r (if (neg? ri) 0 (- (nth right ri) 48))
            total (+ l r carry)]
        (recur (dec li) (dec ri) (quot total 10)
               (conj out (+ 48 (mod total 10))))))))

(defn- p15-s23-sh07-p-decimal-subtract-magnitudes [larger smaller]
  (loop [li (dec (count larger)) si (dec (count smaller)) borrow 0 out ()]
    (if (neg? li)
      (let [digits (vec (drop-while #(= 48 %) out))]
        (if (seq digits) digits [48]))
      (let [l (- (nth larger li) 48 borrow)
            s (if (neg? si) 0 (- (nth smaller si) 48))
            borrowed? (< l s)
            digit (- (if borrowed? (+ l 10) l) s)]
        (recur (dec li) (dec si) (if borrowed? 1 0)
               (conj out (+ 48 digit)))))))

(defn- p15-s23-sh07-p-decimal-signed [negative? digits]
  (if (= digits [48]) {:negative? false :digits [48]}
      {:negative? (boolean negative?) :digits digits}))

(defn- p15-s23-sh07-p-decimal-signed-add [left right]
  (if (= (:negative? left) (:negative? right))
    (p15-s23-sh07-p-decimal-signed
     (:negative? left)
     (p15-s23-sh07-p-decimal-add-magnitudes (:digits left) (:digits right)))
    (let [comparison
          (p15-s23-sh07-p-decimal-magnitude-compare
           (:digits left) (:digits right))]
      (cond
        (zero? comparison) (p15-s23-sh07-p-decimal-signed false [48])
        (pos? comparison)
        (p15-s23-sh07-p-decimal-signed
         (:negative? left)
         (p15-s23-sh07-p-decimal-subtract-magnitudes
          (:digits left) (:digits right)))
        :else
        (p15-s23-sh07-p-decimal-signed
         (:negative? right)
         (p15-s23-sh07-p-decimal-subtract-magnitudes
          (:digits right) (:digits left)))))))

(defn- p15-s23-sh07-p-decimal-small-digits [value]
  (mapv #(+ 48 (- (int %) 48)) (str value)))

(defn- p15-s23-sh07-p-decimal-power-parts [power]
  (cond
    (and (integer? power)
         (<= -999999999999999999 power 999999999999999999))
    (let [negative? (neg? power)
          text (str power)
          digits (mapv int (if negative? (subs text 1) text))]
      (when (<= (count digits) 18)
        (p15-s23-sh07-p-decimal-signed negative? digits)))

    (and (map? power)
         (= #{:artifact :sign :digit-codepoints} (set (keys power)))
         (= :gravity/signed-decimal-power (:artifact power))
         (contains? #{:negative :positive} (:sign power))
         (vector? (:digit-codepoints power))
         (<= (count (:digit-codepoints power)) 258)
         (p15-s23-sh07-p-decimal-digits? (:digit-codepoints power))
         (not= 48 (first (:digit-codepoints power))))
    (p15-s23-sh07-p-decimal-signed
     (= :negative (:sign power)) (:digit-codepoints power))

    :else nil))

(defn- p15-s23-sh07-p-decimal-key? [key]
  (and (map? key)
       (= #{:kind :sign :coefficient :decimal-power} (set (keys key)))
       (= :decimal (:kind key))
       (contains? #{-1 1} (:sign key))
       (vector? (:coefficient key))
       (<= (count (:coefficient key)) 257)
       (p15-s23-sh07-p-decimal-digits? (:coefficient key))
       (or (= [48] (:coefficient key))
           (and (not= 48 (first (:coefficient key)))
                (not= 48 (peek (:coefficient key)))))
       (some? (p15-s23-sh07-p-decimal-power-parts (:decimal-power key)))
       (if (= [48] (:coefficient key))
         (and (= 1 (:sign key)) (= 0 (:decimal-power key))) true)))

(defn- p15-s23-sh07-p-decimal-power-value [signed]
  (if (= [48] (:digits signed))
    0
    (if (<= (count (:digits signed)) 18)
      (let [magnitude
            (reduce (fn [result code] (+ (* result 10) (- code 48)))
                    0 (:digits signed))]
        (if (:negative? signed) (- magnitude) magnitude))
      {:artifact :gravity/signed-decimal-power
       :sign (if (:negative? signed) :negative :positive)
       :digit-codepoints (:digits signed)})))

(defn p15-s23-sh07-p-decimal-spell [key]
  (when-not (p15-s23-sh07-p-decimal-key? key)
    (throw (ex-info "unsupported producer decimal semantic key"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= [48] (:coefficient key))
    "0E+0"
    (let [coefficient (:coefficient key)
          power (p15-s23-sh07-p-decimal-power-parts (:decimal-power key))
          adjustment
          (p15-s23-sh07-p-decimal-signed
           false (p15-s23-sh07-p-decimal-small-digits (dec (count coefficient))))
          exponent (p15-s23-sh07-p-decimal-signed-add power adjustment)
          anticipated-count
          (+ (if (= -1 (:sign key)) 1 0)
             (count coefficient)
             (if (> (count coefficient) 1) 1 0)
             2
             (count (:digits exponent)))
          _
          (when (> anticipated-count 261)
            (throw (ex-info "producer decimal spelling exceeds G19 domain"
                            {:reason :unsupported-decimal-readable-spelling})))
          text
          (str (when (= -1 (:sign key)) "-")
               (char (first coefficient))
               (when (> (count coefficient) 1)
                 (str "." (apply str (map char (rest coefficient)))))
               "E" (if (:negative? exponent) "-" "+")
               (apply str (map char (:digits exponent))))]
      (when (> (count text) 261)
        (throw (ex-info "producer decimal spelling exceeds G19 domain"
                        {:reason :unsupported-decimal-readable-spelling})))
      text)))

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
