(ns gravity.self-hosting.a1-canonical-schema
  "Bounded Clojure seed kernel for the accepted A1 canonical-schema decision.

  This namespace is deliberately isolated.  It is not a compiler stage, does
  not advance BOOT7/BOOT8, and owns exactly the three public operations below."
  (:import (clojure.lang BigInt PersistentArrayMap PersistentHashMap
                         PersistentVector)
           (java.math BigInteger)
           (java.security MessageDigest)
           (java.util Arrays)))

(def ^:private limits
  {:string-bytes 65536
   :items 1024
   :schemas 512
   :depth 64
   :input-bytes 786432
   :output-bytes 750000
   :frames 65
   :key-slots 1024
   :digest-slots 1024
   :work 65536})

(def ^:private terminal-work 10)
(def ^:private terminal-bytes 128)
(def ^:private uint64-max 18446744073709551615N)
(def ^:private schema-id-pattern #"[a-z][a-z0-9-]{0,63}")
(def ^:private allowed-kinds
  #{"null" "boolean" "uint64" "string" "enum" "array" "object"
    "tagged-union"})

(def ^:private namespace-contract
  {:namespace 'gravity.self-hosting.a1-canonical-schema
   :decision "docs/artifacts/phase-15/reports/a1-canonical-schema-invariant-architecture-decision.md"
   :public-api {'canonical-copy 1
                'admit-schema-registry 1
                'validate-and-copy 3}
   :seed :clojure
   :successor :gravity-uniform
   :advances-gates []
   :held ["A2" "A3" "Stage B" "Stage C" "G1" "G2" "G3" "G4" "G5" "G6"]})

(defn- accepted [value]
  {"status" "accepted" "diagnostic" "OK" "value" value "path" []})

(defn- rejected [diagnostic path]
  {"status" "typed-rejected" "diagnostic" diagnostic "value" nil
   "path" path})

(defn- path-of [& segments]
  (reduce (fn [path segment] (cons segment path)) nil segments))

(defn- path-child [path segment]
  (cons segment path))

(defn- path-count [path]
  (min 64 (count path)))

(defn- path-segment [path index]
  (if (vector? path)
    (nth path index)
    (nth path (- (count path) index 1))))

(defn- path-vector [path]
  (mapv #(path-segment path %) (range (path-count path))))

(defn- fail! [diagnostic path]
  (throw (ex-info diagnostic {:a1/failure true
                              :diagnostic diagnostic
                              :path path})))

(defn- budget []
  (atom {:work {:limit (:work limits) :reserved terminal-work :committed 0}
         :input {:limit (:input-bytes limits) :reserved 0 :committed 0}
         :output {:limit (:output-bytes limits) :reserved 0 :committed 0}
         :terminal-result {:limit terminal-bytes :reserved terminal-bytes
                           :committed 0 :work terminal-work
                           :metered-bytes terminal-bytes :reserved? true}
         :frames {:limit (:frames limits) :live 0 :peak 0}
         :key-slots {:limit (:key-slots limits) :live 0 :peak 0}
         :digest-slots {:limit (:digest-slots limits) :live 0 :peak 0}}))

(defn- reserve! [state counter quantity path]
  (when (neg? quantity) (fail! "E-HOST" (path-of "internal")))
  (let [{:keys [limit reserved committed]} (get @state counter)]
    (when (> (+ committed reserved quantity) limit)
      (fail! "E-BOUND" path))
    (swap! state update-in [counter :reserved] + quantity)))

(defn- commit! [state counter quantity]
  (when (or (neg? quantity)
            (< (get-in @state [counter :reserved]) quantity))
    (fail! "E-HOST" (path-of "internal")))
  (swap! state (fn [s]
                 (-> s
                     (update-in [counter :reserved] - quantity)
                     (update-in [counter :committed] + quantity)))))

(defn- release-reservation! [state counter quantity]
  (when (or (neg? quantity)
            (< (get-in @state [counter :reserved]) quantity))
    (fail! "E-HOST" (path-of "internal")))
  (swap! state update-in [counter :reserved] - quantity))

(defn- charge! [state counter quantity path]
  (reserve! state counter quantity path)
  (commit! state counter quantity))

(defn- acquire! [state counter quantity path]
  (let [{:keys [limit live]} (get @state counter)]
    (when (neg? quantity) (fail! "E-HOST" (path-of "internal")))
    (when (> (+ live quantity) limit) (fail! "E-BOUND" path))
    (swap! state (fn [s]
                   (-> s
                       (update-in [counter :live] + quantity)
                       (update-in [counter :peak] max (+ live quantity)))))))

(defn- release! [state counter quantity]
  (when (or (neg? quantity) (< (get-in @state [counter :live]) quantity))
    (fail! "E-HOST" (path-of "internal")))
  (swap! state update-in [counter :live] - quantity))

(defn- work! [state quantity path]
  (charge! state :work quantity path))

(defn- utf8-length [^String value]
  (loop [index 0 total 0]
    (if (= index (.length value))
      total
      (let [code-point (.codePointAt value index)]
        (recur (+ index (Character/charCount code-point))
               (+ total (cond
                          (<= code-point 0x7f) 1
                          (<= code-point 0x7ff) 2
                          (<= code-point 0xffff) 3
                          :else 4)))))))

(defn- scalar-string? [^String value]
  (loop [index 0]
    (if (= index (.length value))
      true
      (let [unit (int (.charAt value index))]
        (cond
          (<= 0xD800 unit 0xDBFF)
          (and (< (inc index) (.length value))
               (<= 0xDC00 (int (.charAt value (inc index))) 0xDFFF)
               (recur (+ index 2)))

          (<= 0xDC00 unit 0xDFFF) false
          :else (recur (inc index)))))))

(defn- exact-class? [value klass]
  (and (some? value) (= (class value) klass)))

(defn- canonical-map? [value]
  (or (exact-class? value PersistentArrayMap)
      (exact-class? value PersistentHashMap)))

(defn- canonical-vector? [value]
  (exact-class? value PersistentVector))

(defn- no-metadata! [value path]
  (when (and (instance? clojure.lang.IMeta value) (some? (meta value)))
    (fail! "E-TYPE" path)))

(defn- uint64? [value]
  (and (or (exact-class? value Long) (exact-class? value BigInt))
       (<= 0 value uint64-max)))

(defn- byte-compare [^String left ^String right]
  ;; UTF-8 preserves scalar-value order, so no byte array is needed.
  (loop [left-index 0 right-index 0]
    (cond
      (= left-index (.length left)) (if (= right-index (.length right)) 0 -1)
      (= right-index (.length right)) 1
      :else
      (let [left-point (.codePointAt left left-index)
            right-point (.codePointAt right right-index)
            comparison (compare left-point right-point)]
        (if (zero? comparison)
          (recur (+ left-index (Character/charCount left-point))
                 (+ right-index (Character/charCount right-point)))
          comparison)))))

(defn- segment-compare [left right]
  (cond
    (and (string? left) (string? right)) (byte-compare left right)
    (string? left) -1
    (string? right) 1
    :else (compare left right)))

(defn- path-compare [left right]
  (loop [index 0]
    (cond
      (= index (path-count left)) (if (= index (path-count right)) 0 -1)
      (= index (path-count right)) 1
      :else (let [comparison (segment-compare (path-segment left index)
                                              (path-segment right index))]
              (if (zero? comparison) (recur (inc index)) comparison)))))

(defn- first-path [paths]
  (reduce (fn [best path]
            (if (or (nil? best) (neg? (path-compare path best))) path best))
          nil paths))

(defn- merge-index-pass! [^objects entries ^ints source ^ints target n width]
  (doseq [start (range 0 n (* 2 width))]
    (let [middle (min n (+ start width))
          end (min n (+ start (* 2 width)))]
      (loop [left start right middle output start]
        (when (< output end)
          (cond
            (= left middle)
            (do (aset-int target output (aget source right))
                (recur left (inc right) (inc output)))

            (= right end)
            (do (aset-int target output (aget source left))
                (recur (inc left) right (inc output)))

            (not (pos? (byte-compare (key (aget entries (aget source left)))
                                     (key (aget entries (aget source right))))))
            (do (aset-int target output (aget source left))
                (recur (inc left) right (inc output)))

            :else
            (do (aset-int target output (aget source right))
                (recur left (inc right) (inc output)))))))))

(defn- apply-index-order! [^objects entries ^ints order n]
  (dotimes [start n]
    (when-not (neg? (aget order start))
      (let [saved (aget entries start)]
        (loop [destination start]
          (let [source (aget order destination)]
            (aset-int order destination -1)
            (if (= source start)
              (aset entries destination saved)
              (do
                (aset entries destination (aget entries source))
                (recur source)))))))))

(defn- bottom-up-mergesort [entries n]
  (let [ordered (object-array n)
        left (int-array n)
        right (int-array n)]
    (doseq [[index entry] (map-indexed vector entries)]
      (aset ordered index entry)
      (aset-int left index index))
    (loop [width 1 source left target right]
      (if (>= width n)
        (do (apply-index-order! ordered source n) ordered)
        (do
          (merge-index-pass! ordered source target n width)
          (recur (* 2 width) target source))))))

(defn- ordered-entries [state value path consume]
  (let [n (count value)]
    (work! state n path)
    (acquire! state :key-slots n path)
    (try
      (let [entries (seq value)]
        (doseq [[key _] entries]
          (when-not (exact-class? key String) (fail! "E-TYPE" path))
          (when-not (scalar-string? key) (fail! "E-TYPE" path))
          (when (> (utf8-length key) (:string-bytes limits))
            (fail! "E-BOUND" path)))
        (let [key-bytes (reduce + 0 (map #(utf8-length (key %)) entries))
              rounds (loop [size (max 1 n) result 0]
                       (if (<= size 1) result
                           (recur (quot (inc size) 2) (inc result))))]
          (work! state (* (+ n key-bytes) rounds) path)
          (consume (bottom-up-mergesort entries n))))
      (finally (release! state :key-slots n)))))

(declare meter-value!)

(defn- meter-string! [state value path counter]
  (when-not (scalar-string? value) (fail! "E-TYPE" path))
  (let [size (utf8-length value)]
    (when (> size (:string-bytes limits)) (fail! "E-BOUND" path))
    (charge! state counter (+ 5 size) path)
    (+ 5 size)))

(defn- path-payload-size [path]
  (reduce (fn [total index]
            (let [segment (path-segment path index)]
              (+ total (if (string? segment)
                         (+ 5 (utf8-length segment))
                         9))))
          0 (range (path-count path))))

(defn- emit-rejection! [state diagnostic path]
  (let [element-work (path-count path)
        payload-bytes (path-payload-size path)
        snapshot @state
        work-counter (:work snapshot)
        output-counter (:output snapshot)
        fits? (and (<= (+ (:committed work-counter)
                          (:reserved work-counter)
                          element-work)
                       (:limit work-counter))
                   (<= (+ (:committed output-counter)
                          (:reserved output-counter)
                          payload-bytes)
                       (:limit output-counter)))]
    (if-not fits?
      (rejected "E-BOUND" [])
      (do
        (swap! state (fn [current]
                       (-> current
                           (update-in [:work :reserved] + element-work)
                           (update-in [:output :reserved] + payload-bytes))))
        (commit! state :work element-work)
        (commit! state :output payload-bytes)
        (rejected diagnostic (path-vector path))))))

(defn- meter-value! [state value path depth counter]
  (when (> depth (:depth limits)) (fail! "E-BOUND" path))
  (acquire! state :frames 1 path)
  (try
    (work! state 1 path)
    (cond
      (nil? value) (do (charge! state counter 1 path) 1)
      (exact-class? value Boolean) (do (charge! state counter 2 path) 2)
      (uint64? value) (do (charge! state counter 9 path) 9)
      (exact-class? value String) (meter-string! state value path counter)

      (canonical-vector? value)
      (do
        (no-metadata! value path)
        (let [n (count value)]
          (when (> n (:items limits)) (fail! "E-BOUND" path))
          (charge! state counter 5 path)
          (+ 5 (reduce + 0 (map-indexed
                             (fn [index item]
                               (meter-value! state item (path-child path index)
                                             (inc depth) counter))
                             value)))))

      (canonical-map? value)
      (do
        (no-metadata! value path)
        (let [n (count value)]
          (when (> n (:items limits)) (fail! "E-BOUND" path))
          (charge! state counter 5 path)
          (+ 5 (ordered-entries
                 state value path
                 #(reduce + 0
                          (map (fn [[key item]]
                                 (+ (meter-value! state key (path-child path key)
                                                  (inc depth) counter)
                                    (meter-value! state item (path-child path key)
                                                  (inc depth) counter))) %))))))

      :else (fail! "E-TYPE" path))
    (finally (release! state :frames 1))))

(defn- canonical-uint [value]
  (if (<= value Long/MAX_VALUE) (long value) (bigint value)))

(declare copy-value!)

(def ^:private construct-vector (fn [items] (vec items)))
(def ^:private construct-map
  (fn [entries] (reduce (fn [result [key value]] (assoc result key value))
                        PersistentHashMap/EMPTY entries)))
(def ^:dynamic ^:private *audit-sink* nil)

(defn- copy-value! [state value path depth]
  (when (> depth (:depth limits)) (fail! "E-BOUND" path))
  (acquire! state :frames 1 path)
  (try
    (work! state 1 path)
    (cond
      (nil? value) nil
      (exact-class? value Boolean) value
      (uint64? value) (do
                        (when (and (exact-class? value BigInt)
                                   (<= value Long/MAX_VALUE))
                          (work! state 1 path))
                        (canonical-uint value))
      (exact-class? value String) value
      (canonical-vector? value)
      (do
        (work! state 1 path)
        (construct-vector
          (map-indexed (fn [index item]
                         (copy-value! state item (path-child path index) (inc depth)))
                       value)))
      (canonical-map? value)
      (do
        (work! state 1 path)
        (ordered-entries
          state value path
          #(construct-map
             (map (fn [[key item]]
                    [(copy-value! state key (path-child path key) (inc depth))
                     (copy-value! state item (path-child path key) (inc depth))]) %))))
      :else (fail! "E-TYPE" path))
    (finally (release! state :frames 1))))

(defn- schema-id! [value path]
  (when-not (exact-class? value String) (fail! "E-ID-TYPE" path))
  (when-not (re-matches schema-id-pattern value) (fail! "E-ID-SYNTAX" path))
  value)

(defn- exact-fields! [definition allowed required path]
  (let [actual (keys definition)]
    (when (or (not (every? string? actual))
              (not (every? #(contains? definition %) required))
              (not (every? allowed actual)))
      (fail! "E-KEYSET" path))))

(defn- boolean-field! [definition name path]
  (when-not (exact-class? (get definition name) Boolean)
    (fail! "E-SCHEMA" (path-child path name))))

(defn- uint-field! [definition name maximum path]
  (let [value (get definition name)]
    (when-not (and (uint64? value) (<= value maximum))
      (fail! "E-SCHEMA" (path-child path name)))
    (long value)))

(declare value-digest! canonical-equal!)

(defn- ensure-unique! [state values path]
  (let [n (count values)]
    (acquire! state :digest-slots n path)
    (try
      (let [digests (object-array n)]
        (loop [index 0]
          (when (< index n)
            (let [item (nth values index)
                  digest (value-digest! state item (path-child path index))]
              (work! state 1 (path-child path index))
              (loop [prior-index 0]
                (when (< prior-index index)
                  (if (and (Arrays/equals ^bytes (aget digests prior-index)
                                          ^bytes digest)
                           (canonical-equal! state (nth values prior-index) item
                                             (path-child path prior-index)))
                    (fail! "E-SCHEMA" (path-child path index))
                    (recur (inc prior-index)))))
              (aset digests index digest)
              (recur (inc index))))))
      (finally (release! state :digest-slots n)))))

(defn- refs-in-definition! [state definition path]
  (let [kind (get definition "kind")]
    (when-not (and (exact-class? kind String) (contains? allowed-kinds kind))
      (fail! "E-SCHEMA" (path-child path "kind")))
    (case kind
      "null" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "boolean" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "uint64" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "string"
      (do (exact-fields! definition #{"kind" "ascii-only" "max-bytes"}
                         #{"kind" "ascii-only" "max-bytes"} path)
          (boolean-field! definition "ascii-only" path)
          (uint-field! definition "max-bytes" (:string-bytes limits) path)
          [])
      "enum"
      (do (exact-fields! definition #{"kind" "values"} #{"kind" "values"} path)
          (let [values (get definition "values")]
            (when-not (and (canonical-vector? values)
                           (<= 1 (count values) 1024)
                           (every? #(exact-class? % String) values))
              (fail! "E-SCHEMA" (path-child path "values")))
            (ensure-unique! state values (path-child path "values")))
          [])
      "array"
      (do (exact-fields! definition
                         #{"kind" "item" "min-items" "max-items" "unique"}
                         #{"kind" "item" "min-items" "max-items" "unique"} path)
          (boolean-field! definition "unique" path)
          (let [minimum (uint-field! definition "min-items" 1024 path)
                maximum (uint-field! definition "max-items" 1024 path)]
            (when (> minimum maximum) (fail! "E-SCHEMA" path)))
          [(schema-id! (get definition "item") (path-child path "item"))])
      "object"
      (do (exact-fields! definition #{"kind" "required" "optional"}
                         #{"kind" "required" "optional"} path)
          (let [required (get definition "required")
                optional (get definition "optional")]
            (when-not (and (canonical-map? required) (canonical-map? optional)
                           (<= (+ (count required) (count optional)) 1024)
                           (not-any? #(contains? optional %) (keys required)))
              (fail! "E-SCHEMA" path))
            ;; Closed-value admission and the ranked reference preflight have
            ;; already established field/ref types and canonical fault order.
            []))
      "tagged-union"
      (do (exact-fields! definition #{"kind" "tag-key" "variants"}
                         #{"kind" "tag-key" "variants"} path)
          (let [tag-key (get definition "tag-key")
                variants (get definition "variants")]
            (when-not (and (exact-class? tag-key String) (not= tag-key "value")
                           (canonical-map? variants) (<= 1 (count variants) 1024))
              (fail! "E-SCHEMA" path))
            [])))))

(defn- candidate-refs [state id definition]
  (if-not (canonical-map? definition)
    []
    (case (get definition "kind")
      "array" (do (work! state 1 (path-of id "item"))
                  [[id (path-of id "item") (get definition "item")]])
      "object"
      (reduce into []
              (for [name ["required" "optional"]
                    :let [fields (get definition name)]
                    :when (canonical-map? fields)]
                (ordered-entries state fields (path-of id name)
                                 #(do (work! state (count %) (path-of id name))
                                      (mapv (fn [[field ref]]
                                              [id (path-of id field) ref]) %)))))
      "tagged-union"
      (if (canonical-map? (get definition "variants"))
        (ordered-entries state (get definition "variants")
                         (path-of id "variants")
                         #(do (work! state (count %) (path-of id "variants"))
                              (mapv (fn [[tag ref]]
                                      [id (path-of id tag) ref]) %)))
        [])
      [])))

(def ^:private diagnostic-rank
  {"E-TYPE" 0 "E-KEYSET" 1 "E-CYCLE" 2 "E-SCHEMA" 3 "E-HOST" 4 "OK" 5})

(defn- ranked-fault [faults]
  (reduce
    (fn [best fault]
      (if (nil? best)
        fault
        (let [rank-order (compare (get diagnostic-rank (:diagnostic fault) 99)
                                  (get diagnostic-rank (:diagnostic best) 99))]
          (if (or (neg? rank-order)
                  (and (zero? rank-order)
                       (neg? (path-compare (:path fault) (:path best)))))
            fault best))))
    nil faults))

(defn- registry-shape-faults [state ordered]
  (keep (fn [[id definition]]
          (try
            (when-not (canonical-map? definition)
              (fail! "E-SCHEMA" (path-of id)))
            (refs-in-definition! state definition (path-of id))
            nil
            (catch clojure.lang.ExceptionInfo failure
              (let [data (ex-data failure)]
                (when (= "E-BOUND" (:diagnostic data)) (throw failure))
                data))))
        ordered))

(defn- check-ordered-registry! [state registry ordered]
  (let [refs (vec (mapcat (fn [[id definition]]
                            (candidate-refs state id definition)) ordered))]
    ;; Diagnostic-rank passes precede lower-ranked shape faults.
    (doseq [[_ path ref] refs]
      (when-not (exact-class? ref String) (fail! "E-ID-TYPE" path)))
    (let [syntax-paths
          (concat (keep (fn [[id _]]
                          (when-not (re-matches schema-id-pattern id)
                            (path-of id)))
                        ordered)
                  (keep (fn [[_ path ref]]
                          (when-not (re-matches schema-id-pattern ref) path))
                        refs))]
      (when (seq syntax-paths) (fail! "E-ID-SYNTAX" (first-path syntax-paths))))
    (doseq [[_ path ref] refs]
      (when-not (contains? registry ref) (fail! "E-UNKNOWN-ID" path)))
    (let [shape-faults (vec (registry-shape-faults state ordered))
          high-fault (ranked-fault
                       (filter #(contains? #{"E-TYPE" "E-KEYSET"}
                                           (:diagnostic %))
                               shape-faults))
          _ (when high-fault (fail! (:diagnostic high-fault) (:path high-fault)))
          shape-fault (ranked-fault shape-faults)
          _ (when (and (empty? refs) shape-fault)
              (fail! (:diagnostic shape-fault) (:path shape-fault)))
          _ (swap! state assoc :registry-graph-built? true)
          graph (reduce (fn [result [id _]] (assoc result id [])) {} ordered)
          graph (reduce (fn [result [id _ ref]] (update result id conj ref))
                        graph refs)]
    (let [colors (atom {})
          heights (atom {})]
      (letfn [(visit [id]
                (case (get @colors id)
                  :gray (fail! "E-CYCLE" (path-of id))
                  :black (get @heights id)
                  (do
                    (acquire! state :frames 1 (path-of id))
                    (try
                      (swap! colors assoc id :gray)
                      (let [children (get graph id)
                            child-height
                            (loop [index 0 maximum -1]
                              (if (= index (count children))
                                maximum
                                (recur (inc index)
                                       (max maximum (visit (nth children index))))))
                            height (inc child-height)]
                        (when (> height (:depth limits))
                          (fail! "E-BOUND" (path-of id)))
                        (swap! heights assoc id height)
                        (swap! colors assoc id :black)
                        height)
                      (finally (release! state :frames 1))))))]
        (doseq [[id _] ordered] (visit id))))
      (when shape-fault (fail! (:diagnostic shape-fault) (:path shape-fault)))
      graph)))

(defn- check-registry! [state registry]
  (when (> (count registry) (:schemas limits)) (fail! "E-BOUND" nil))
  (ordered-entries
    state registry nil
    (fn [entries]
      (work! state (count registry) nil)
      (check-ordered-registry! state registry entries))))

(defn- digest-byte! [state ^MessageDigest digest value path]
  (work! state 1 path)
  (.update digest (byte value)))

(defn- digest-u32! [state digest value path]
  (doseq [shift [24 16 8 0]]
    (digest-byte! state digest (bit-and 0xff (bit-shift-right value shift)) path)))

(defn- digest-u64! [state digest value path]
  (let [bits (.longValue ^BigInteger (biginteger value))]
    (doseq [shift [56 48 40 32 24 16 8 0]]
      (digest-byte! state digest
                    (bit-and 0xff (unsigned-bit-shift-right bits shift)) path))))

(defn- digest-string! [state digest ^String value path]
  (loop [index 0]
    (when (< index (.length value))
      (let [point (.codePointAt value index)]
        (cond
          (<= point 0x7f)
          (digest-byte! state digest point path)

          (<= point 0x7ff)
          (do (digest-byte! state digest (+ 0xc0 (bit-shift-right point 6)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path))

          (<= point 0xffff)
          (do (digest-byte! state digest (+ 0xe0 (bit-shift-right point 12)) path)
              (digest-byte! state digest (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path))

          :else
          (do (digest-byte! state digest (+ 0xf0 (bit-shift-right point 18)) path)
              (digest-byte! state digest (+ 0x80 (bit-and (bit-shift-right point 12) 0x3f)) path)
              (digest-byte! state digest (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path)))
        (recur (+ index (Character/charCount point)))))))

(declare digest-value!)

(defn- digest-value! [state digest value path]
  (let [tag (cond (nil? value) 0
                  (exact-class? value Boolean) 1
                  (uint64? value) 2
                  (exact-class? value String) 3
                  (canonical-vector? value) 4
                  :else 5)]
    (digest-byte! state digest tag path)
    (cond
      (nil? value) nil
      (exact-class? value Boolean)
      (digest-byte! state digest (if value 1 0) path)
      (uint64? value)
      (digest-u64! state digest value path)
      (exact-class? value String)
      (do (digest-u32! state digest (utf8-length value) path)
          (digest-string! state digest value path))
      (canonical-vector? value)
      (do
        (digest-u32! state digest (count value) path)
        (doseq [[index item] (map-indexed vector value)]
          (digest-u32! state digest index path)
          (digest-value! state digest item (path-child path index))))
      (canonical-map? value)
      (do
        (digest-u32! state digest (count value) path)
        (ordered-entries
          state value path
          (fn [entries]
            (doseq [[key item] entries]
              (digest-value! state digest key (path-child path key))
              (digest-value! state digest item (path-child path key)))))))))

(defn- value-digest! [state value path]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (digest-value! state digest value path)
    (.digest digest)))

(declare canonical-equal!)

(defn- canonical-equal! [state left right path]
  (work! state 1 path)
  (cond
    (and (nil? left) (nil? right)) true
    (and (exact-class? left Boolean) (exact-class? right Boolean)) (= left right)
    (and (uint64? left) (uint64? right)) (= left right)
    (and (exact-class? left String) (exact-class? right String))
    (let [compared (min (utf8-length left) (utf8-length right))]
      (work! state compared path)
      (= left right))
    (and (canonical-vector? left) (canonical-vector? right)
         (= (count left) (count right)))
    (every? true?
            (map-indexed (fn [index item]
                           (canonical-equal! state item (nth right index)
                                             (path-child path index)))
                         left))
    (and (canonical-map? left) (canonical-map? right)
         (= (count left) (count right)))
    (ordered-entries
      state left path
      (fn [left-entries]
        (every? true?
                (map (fn [[key item]]
                       (and (contains? right key)
                            (canonical-equal! state item (get right key)
                                              (path-child path key))))
                     left-entries))))
    :else false))

(declare validate-value!)

(defn- validate-array! [state registry definition value path depth]
  (when-not (canonical-vector? value) (fail! "E-TYPE" path))
  (let [n (count value)]
    (when-not (<= (long (get definition "min-items")) n
                  (long (get definition "max-items")))
      (fail! "E-SCHEMA" path))
    (when (get definition "unique")
      (ensure-unique! state value path))
    (doseq [[index item] (map-indexed vector value)]
      (validate-value! state registry (get definition "item") item
                       (path-child path index) (inc depth)))))

(defn- validate-object! [state registry definition value path depth]
  (when-not (canonical-map? value) (fail! "E-TYPE" path))
  (let [required (get definition "required")
        optional (get definition "optional")]
    (when (or (not (every? #(contains? value %) (keys required)))
              (not (every? #(or (contains? required %)
                                (contains? optional %))
                           (keys value))))
      (fail! "E-KEYSET" path))
    (ordered-entries
      state value path
      (fn [entries]
        (doseq [[field item] entries]
          (validate-value! state registry (or (get required field) (get optional field))
                           item (path-child path field) (inc depth)))))))

(defn- validate-tagged! [state registry definition value path depth]
  (when-not (canonical-map? value) (fail! "E-TYPE" path))
  (let [tag-key (get definition "tag-key")]
    (when-not (and (= 2 (count value))
                   (contains? value tag-key) (contains? value "value"))
      (fail! "E-KEYSET" path))
    (let [tag (get value tag-key)]
      (when-not (exact-class? tag String) (fail! "E-TYPE" (path-child path tag-key)))
      (let [selected (get (get definition "variants") tag)]
        (when-not selected (fail! "E-SCHEMA" (path-child path tag-key)))
        (work! state 1 path)
        (validate-value! state registry selected (get value "value")
                         (path-child path "value") (inc depth))))))

(defn- validate-value! [state registry schema-id value path depth]
  (when (> depth (:depth limits)) (fail! "E-BOUND" path))
  (acquire! state :frames 1 path)
  (try
    (work! state 1 path)
    (let [definition (get registry schema-id)]
      (when-not definition (fail! "E-UNKNOWN-ID" path))
      (case (get definition "kind")
        "null" (when-not (nil? value) (fail! "E-TYPE" path))
        "boolean" (when-not (exact-class? value Boolean) (fail! "E-TYPE" path))
        "uint64" (when-not (uint64? value) (fail! "E-TYPE" path))
        "string" (do
                   (when-not (exact-class? value String) (fail! "E-TYPE" path))
                   (let [byte-count (utf8-length value)]
                     (when (> byte-count (long (get definition "max-bytes")))
                       (fail! "E-SCHEMA" path))
                     (when (and (get definition "ascii-only")
                                (some #(> (int %) 0x7f) value))
                       (fail! "E-SCHEMA" path))))
        "enum" (do
                 (when-not (exact-class? value String) (fail! "E-TYPE" path))
                 (when-not (some #(= value %) (get definition "values"))
                   (fail! "E-SCHEMA" path)))
        "array" (validate-array! state registry definition value path depth)
        "object" (validate-object! state registry definition value path depth)
        "tagged-union" (validate-tagged! state registry definition value path depth)))
    (finally (release! state :frames 1))))

(declare measure-value!)

(defn- checked-size [left right path]
  (let [sum (+ left right)]
    (when (> sum (:output-bytes limits)) (fail! "E-BOUND" path))
    sum))

(defn- measure-value! [state value path depth]
  (when (> depth (:depth limits)) (fail! "E-BOUND" path))
  (acquire! state :frames 1 path)
  (try
    (work! state 1 path)
    (cond
      (nil? value) 1
      (exact-class? value Boolean) 2
      (uint64? value) 9
      (exact-class? value String) (+ 5 (utf8-length value))
      (canonical-vector? value)
      (reduce (fn [size [index item]]
                (checked-size size
                              (measure-value! state item (path-child path index) (inc depth))
                              path))
              5 (map-indexed vector value))
      (canonical-map? value)
      (ordered-entries
        state value path
        #(reduce (fn [size [key item]]
                   (checked-size
                     (checked-size size
                                   (measure-value! state key (path-child path key) (inc depth))
                                   path)
                     (measure-value! state item (path-child path key) (inc depth)) path))
                 5 %))
      :else (fail! "E-HOST" (path-of "internal")))
    (finally (release! state :frames 1))))

(defn- finish-copy! [state value]
  (let [size (measure-value! state value nil 0)]
    (reserve! state :output size nil)
    (commit! state :output size)
    (copy-value! state value nil 0)))

(defn- finalize-terminal! [state]
  (commit! state :work terminal-work)
  (commit! state :terminal-result terminal-bytes)
  (swap! state assoc-in [:terminal-result :reserved?] false))

(defn- execute [operation args]
  (let [state (budget)]
    (try
      (let [result
            (try
              (case operation
        :copy
        (do (when-not (= 1 (count args))
              (fail! "E-TYPE" (path-of "arguments")))
            (let [value (nth args 0)]
              (work! state 1 (path-of "arguments"))
              (meter-value! state value nil 0 :input)
              (accepted (finish-copy! state value))))

        :registry
        (do (when-not (= 1 (count args))
              (fail! "E-TYPE" (path-of "arguments")))
            (let [registry (nth args 0)]
              (work! state 1 (path-of "arguments"))
              (meter-value! state registry nil 0 :input)
              (when-not (canonical-map? registry) (fail! "E-TYPE" nil))
              (check-registry! state registry)
              (accepted (finish-copy! state registry))))

        :validate
        (do (when-not (= 3 (count args))
              (fail! "E-TYPE" (path-of "arguments")))
            (let [[registry schema-id value] args]
              (work! state 1 (path-of "arguments"))
              (meter-value! state registry nil 0 :input)
              (work! state 1 (path-of "arguments"))
              (schema-id! schema-id (path-of "schema-id"))
              (meter-value! state schema-id (path-of "schema-id") 0 :input)
              (work! state 1 (path-of "arguments"))
              (meter-value! state value nil 0 :input)
              (when-not (canonical-map? registry) (fail! "E-TYPE" nil))
              (check-registry! state registry)
              (when-not (contains? registry schema-id)
                (fail! "E-UNKNOWN-ID" (path-of "schema-id")))
              (swap! state assoc :phase2-work (get-in @state [:work :committed]))
              (validate-value! state registry schema-id value nil 0)
              (accepted (finish-copy! state value)))))
              (catch clojure.lang.ExceptionInfo failure
                (let [data (ex-data failure)]
                  (if (:a1/failure data)
                    (emit-rejection! state (:diagnostic data) (:path data))
                    (emit-rejection! state "E-HOST" (path-of "internal")))))
              (catch InterruptedException _
                (.interrupt (Thread/currentThread))
                (emit-rejection! state "E-HOST" (path-of "internal")))
              (catch Exception _
                (emit-rejection! state "E-HOST" (path-of "internal"))))]
        (finalize-terminal! state)
        result)
      (catch InterruptedException _
        (.interrupt (Thread/currentThread))
        (emit-rejection! state "E-HOST" (path-of "internal")))
      (catch Exception _
        (emit-rejection! state "E-HOST" (path-of "internal")))
      (finally
        (when (instance? clojure.lang.IAtom *audit-sink*)
          (reset! *audit-sink* @state))))))

(defn canonical-copy [& args]
  (execute :copy args))

(defn admit-schema-registry [& args]
  (execute :registry args))

(defn validate-and-copy [& args]
  (execute :validate args))
