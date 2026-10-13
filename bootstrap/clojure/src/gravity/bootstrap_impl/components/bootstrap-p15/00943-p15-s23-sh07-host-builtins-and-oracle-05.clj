
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

(defn p15-s23-sh07-o-decimal-inverse [text]
  (let [chars (when (string? text) (vec text))]
    (when-not (and chars (<= 1 (count chars) 261)
                   (every? #(<= (int %) 127) chars))
      (throw (ex-info "source oracle decimal inverse domain"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (= chars [\0 \E \+ \0])
      {:kind :decimal :sign 1 :coefficient [48] :decimal-power 0}
      (let [negative? (= \- (first chars))
            offset (if negative? 1 0)
            e-positions (keep-indexed #(when (= %2 \E) %1) chars)]
        (when-not (= 1 (count e-positions))
          (throw (ex-info "source oracle decimal exponent marker"
                          {:reason :unsupported-decimal-readable-spelling})))
        (let [e-position (first e-positions)
              mantissa (subvec chars offset e-position)
              exponent-part (subvec chars (inc e-position))
              dot-positions (keep-indexed #(when (= %2 \.) %1) mantissa)
              dotted? (= 1 (count dot-positions))
              coefficient-chars
              (if dotted?
                (let [dot (first dot-positions)]
                  (when-not (= dot 1)
                    (throw (ex-info "source oracle decimal point"
                                    {:reason :unsupported-decimal-readable-spelling})))
                  (into (subvec mantissa 0 dot) (subvec mantissa (inc dot))))
                mantissa)
              exponent-sign (first exponent-part)
              exponent-chars (subvec exponent-part 1)]
          (when-not
           (and (seq coefficient-chars)
                (<= (int \1) (int (first coefficient-chars)) (int \9))
                (every? #(<= (int \0) (int %) (int \9)) coefficient-chars)
                (not= \0 (last coefficient-chars))
                (or (= 1 (count coefficient-chars)) dotted?)
                (or (= exponent-sign \+) (= exponent-sign \-))
                (seq exponent-chars)
                (every? #(<= (int \0) (int %) (int \9)) exponent-chars)
                (or (= 1 (count exponent-chars))
                    (not= \0 (first exponent-chars))))
            (throw (ex-info "source oracle noncanonical decimal"
                            {:reason :unsupported-decimal-readable-spelling})))
          (let [coefficient (mapv int coefficient-chars)
                exponent (p15-s23-sh07-o-decimal-normal-signed
                          (if (= exponent-sign \-) :negative :positive)
                          (mapv int exponent-chars))
                shift (p15-s23-sh07-o-decimal-normal-signed
                       :negative
                       (p15-s23-sh07-o-decimal-count-digits
                        (dec (count coefficient))))
                key {:kind :decimal :sign (if negative? -1 1)
                     :coefficient coefficient
                     :decimal-power
                     (p15-s23-sh07-o-decimal-encoded-power
                      (p15-s23-sh07-o-decimal-sum exponent shift))}]
            (when-not (= text (p15-s23-sh07-o-decimal-spell key))
              (throw (ex-info "source oracle decimal respelling mismatch"
                              {:reason :unsupported-decimal-readable-spelling})))
            key))))))

(defn- p15-s23-sh07-o-decimal-occurrence
  [evidence]
  (when-not (and (map? evidence)
                 (= 3 (count evidence))
                 (contains? evidence :c2-reader-artifact)
                 (contains? evidence :adapted-form-id)
                 (contains? evidence :adapted-token-id))
    (throw (ex-info "source oracle projection evidence is malformed"
                    {:reason :unsupported-decimal-readable-spelling})))
  (let [artifact (get evidence :c2-reader-artifact)
        wanted-form (get evidence :adapted-form-id)
        wanted-token (get evidence :adapted-token-id)
        candidates
        (loop [remaining
               (get-in artifact
                       [:sh07-decimal-occurrence-projection :occurrences])
               result []]
          (if (empty? remaining)
            result
            (let [candidate (first remaining)]
              (recur
               (rest remaining)
               (if (and (= wanted-form (get candidate :adapted-form-id))
                        (= wanted-token (get candidate :adapted-token-id)))
                 (conj result candidate)
                 result)))))]
    (when-not (and (= (get artifact :artifact-id)
                      (c2-reader-artifact-id artifact))
                   (true?
                    (sh03-reader-final-decimal-occurrence-projection-valid?
                     artifact))
                   (= 1 (count candidates)))
      (throw (ex-info "source oracle projection authentication failed"
                      {:reason :unsupported-decimal-readable-spelling})))
    (first candidates)))

(defn- p15-s23-sh07-o-decimal-occurrence-text
  [evidence occurrence]
  (let [artifact (get evidence :c2-reader-artifact)
        form-id (get occurrence :adapted-form-id)
        token-id (get occurrence :adapted-token-id)
        forms
        (loop [remaining (get artifact :form-tree) selected []]
          (if (empty? remaining) selected
              (let [form (first remaining)]
                (recur (rest remaining)
                       (if (and (= form-id (get form :form-id))
                                (= token-id (get form :open-token)))
                         (conj selected form) selected)))))
        literals
        (loop [remaining (get artifact :literal-decoding-records) selected []]
          (if (empty? remaining) selected
              (let [literal (first remaining)]
                (recur (rest remaining)
                       (if (= form-id (get literal :form-id))
                         (conj selected literal) selected)))))]
    (when-not (and (= 1 (count forms)) (= 1 (count literals))
                   (= (get (first forms) :raw)
                      (get (first literals) :raw)))
      (throw (ex-info "source oracle occurrence text is not unique"
                      {:reason :unsupported-decimal-readable-spelling})))
    (get (first forms) :raw)))

(defn- p15-s23-sh07-o-decimal-occurrence-identity
  [evidence occurrence]
  [(get-in evidence [:c2-reader-artifact :artifact-id])
   (get occurrence :adapted-form-id)
   (get occurrence :adapted-token-id)])

(defn- p15-s23-sh07-o-decimal-c2-evidence?
  [text key evidence]
  (try
    (let [occurrence (p15-s23-sh07-o-decimal-occurrence evidence)
          entry (get occurrence :semantic-value-entry)
          descriptor (get entry :descriptor)]
      (and (= text (p15-s23-sh07-o-decimal-occurrence-text
                    evidence occurrence))
           (if (<= (count text) 256)
        (and (p15-s23-sh07-o-decimal-authentic-key?
              (get entry :semantic-key))
             (= key (get entry :semantic-key))
             (= key (get descriptor :semantic-key))
             (= nil (get descriptor :normalization-reason)))
        (and (= :deferred (get descriptor :semantic-validation))
             (= :reader-semantic-work-boundary
                (get descriptor :normalization-reason))
             (= {:kind :decimal
                 :normalization :reader-semantic-work-boundary
                 :bounded-semantic-key (get descriptor :semantic-key)}
                (get entry :semantic-key))))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch Throwable _ false)))

(defn p15-s23-sh07-o-decimal-c2-branch [text key evidence]
  (let [inverse-key (p15-s23-sh07-o-decimal-inverse text)]
    (when-not (and (= key inverse-key)
                   (p15-s23-sh07-o-decimal-c2-evidence? text key evidence))
      (throw (ex-info "source oracle decimal C2 evidence mismatch"
                      {:reason :unsupported-decimal-readable-spelling})))
    (if (<= (count text) 256)
      {:status :exact-normalized :semantic-key key}
      {:status :normalization-deferred
       :normalization-reason :reader-semantic-work-boundary
       :semantic-key key})))

(defn p15-s23-sh07-o-decimal-decoration [key evidence]
  (let [text (p15-s23-sh07-o-decimal-spell key)
        branch (p15-s23-sh07-o-decimal-c2-branch text key evidence)]
    (when-not (= key (:semantic-key branch))
      (throw (ex-info "source oracle decimal branch lost semantic identity"
                      {:reason :unsupported-decimal-readable-spelling})))
    text))
