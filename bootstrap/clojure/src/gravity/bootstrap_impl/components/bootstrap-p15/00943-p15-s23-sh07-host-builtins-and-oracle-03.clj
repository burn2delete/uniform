
(defn p15-s23-sh07-observe-utf8-byte-count
  [text observed-before maximum]
  (when-not
   (and (string? text)
        (integer? observed-before)
        (integer? maximum)
        (<= 0 observed-before maximum 1073741824))
    (throw
     (IllegalArgumentException.
      "sh07-observe-utf8-byte-count requires a string and governed counters")))
  (let [length (.length ^String text)
        record
        (fn [status reason observed-after prospective units scalars]
          {:artifact :gravity/sh07-utf8-byte-observation-v1
           :status status
           :reason reason
           :maximum maximum
           :observed-before observed-before
           :observed-after observed-after
           :prospective-after prospective
           :utf16-units-scanned units
           :unicode-scalars-scanned scalars})]
    (loop [index 0 observed-after observed-before scalars 0]
      (if (= index length)
        (record :accepted nil observed-after observed-after index scalars)
        (let [unit (int (.charAt ^String text index))]
          (cond
            (<= 0xd800 unit 0xdbff)
            (if (and (< (inc index) length)
                     (let [low (int (.charAt ^String text (inc index)))]
                       (<= 0xdc00 low 0xdfff)))
              (let [width 4
                    remaining (- maximum observed-after)
                    prospective (+ observed-after width)]
                (if (<= width remaining)
                  (recur (+ index 2) prospective (inc scalars))
                  (record :rejected :utf8-byte-limit observed-after
                          prospective index scalars)))
              (record :rejected :invalid-unicode-scalar-sequence
                      observed-after nil index scalars))

            (<= 0xdc00 unit 0xdfff)
            (record :rejected :invalid-unicode-scalar-sequence
                    observed-after nil index scalars)

            :else
            (let [width (cond (<= unit 0x7f) 1
                              (<= unit 0x7ff) 2
                              :else 3)
                  remaining (- maximum observed-after)
                  prospective (+ observed-after width)]
              (if (<= width remaining)
                (recur (inc index) prospective (inc scalars))
                (record :rejected :utf8-byte-limit observed-after
                        prospective index scalars)))))))))

;; G19 keeps decimal text out of the generic seed printer.  These two closures
;; consume only the authenticated four-key C2 semantic value.  They are
;; deliberately duplicated: the C11/producer model and the source oracle must
;; not share decimal grammar, arithmetic, inversion, or rendering authority.
(defn- p15-s23-sh07-p-decimal-digits? [digits]
  (and (vector? digits) (seq digits)
       (every? #(and (integer? %) (<= 48 % 57)) digits)))

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
