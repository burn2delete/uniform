
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

(defn- p15-s23-sh07-o-decimal-authentic-key? [key]
  (and (map? key)
       (= #{:kind :sign :coefficient :decimal-power} (set (keys key)))
       (= :decimal (:kind key))
       (contains? #{-1 1} (:sign key))
       (vector? (:coefficient key))
       (<= (count (:coefficient key)) 257)
       (p15-s23-sh07-o-decimal-digits? (:coefficient key))
       (if (= [48] (:coefficient key))
         (and (= 1 (:sign key)) (= 0 (:decimal-power key)))
         (and (not= 48 (first (:coefficient key)))
              (not= 48 (last (:coefficient key)))))
       (some? (p15-s23-sh07-o-decimal-power (:decimal-power key)))))

(defn- p15-s23-sh07-o-decimal-encoded-power [signed]
  (if (= [48] (:digits signed)) 0
      (if (<= (count (:digits signed)) 18)
        (let [magnitude
              (loop [remaining (:digits signed) result 0]
                (if (empty? remaining) result
                    (recur (rest remaining)
                           (+ (* result 10) (- (first remaining) 48)))))]
          (if (= :negative (:sign signed)) (- magnitude) magnitude))
        {:artifact :gravity/signed-decimal-power
         :sign (:sign signed) :digit-codepoints (:digits signed)})))

(defn p15-s23-sh07-o-decimal-spell [key]
  (when-not (p15-s23-sh07-o-decimal-authentic-key? key)
    (throw (ex-info "source oracle rejected decimal key"
                    {:reason :unsupported-decimal-readable-spelling})))
  (if (= [48] (:coefficient key)) "0E+0"
      (let [coefficient (:coefficient key)
            base (p15-s23-sh07-o-decimal-power (:decimal-power key))
            shift (p15-s23-sh07-o-decimal-normal-signed
                   :positive
                   (p15-s23-sh07-o-decimal-count-digits
                    (dec (count coefficient))))
            exponent (p15-s23-sh07-o-decimal-sum base shift)
            anticipated-count
            (+ (if (= -1 (:sign key)) 1 0)
               (count coefficient)
               (if (= 1 (count coefficient)) 0 1)
               2
               (count (:digits exponent)))
            _
            (when (> anticipated-count 261)
              (throw (ex-info "source oracle decimal exceeds bound"
                              {:reason :unsupported-decimal-readable-spelling})))
            result (str (if (= -1 (:sign key)) "-" "")
                        (char (first coefficient))
                        (if (= 1 (count coefficient)) ""
                            (str "." (apply str (map char (rest coefficient)))))
                        "E" (if (= :negative (:sign exponent)) "-" "+")
                        (apply str (map char (:digits exponent))))]
        (when (> (count result) 261)
          (throw (ex-info "source oracle decimal exceeds bound"
                          {:reason :unsupported-decimal-readable-spelling})))
        result)))
