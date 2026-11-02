(ns gravity.self-hosting.a1-canonical-schema-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.a1-canonical-schema :as a1])
  (:import (clojure.lang BigInt PersistentHashMap PersistentVector)
           (java.math BigInteger)
           (java.util ArrayList HashMap)))

(deftype ThrowingMeta []
  clojure.lang.IMeta
  (meta [_] (throw (Exception. "must not be called"))))

(defn- result [diagnostic path]
  {"status" "typed-rejected" "diagnostic" diagnostic "value" nil "path" path})

(defn- phm [entries]
  (reduce (fn [m [k v]] (assoc m k v)) PersistentHashMap/EMPTY entries))

(defn- value-with-meter [total]
  (let [string-count 12
        payload (- total 5 (* string-count 5))
        quotient (quot payload string-count)
        remainder (rem payload string-count)]
    (vec (map (fn [index]
                (apply str (repeat (+ quotient (if (< index remainder) 1 0)) "a")))
              (range string-count)))))

(defn- short-schema-ids []
  (take 512
        (concat (map str "abcdefghijklmnopqrstuvwxyz")
                (for [left "abcdefghijklmnopqrstuvwxyz"
                      right "0123456789abcdefghijklmnopqrstuvwxyz-"]
                  (str left right)))))

(def scalar-registry
  {"nil" {"kind" "null"}
   "bool" {"kind" "boolean"}
   "uint" {"kind" "uint64"}
   "text" {"kind" "string" "ascii-only" false "max-bytes" 65536}
   "choice" {"kind" "enum" "values" ["a" "b"]}})

(defn- fixture [name]
  (-> (io/file "bootstrap/clojure/fixtures/self-hosting/a1-canonical-schema" name)
      slurp edn/read-string))

(defn- run-fixture [{:keys [operation value registry schema-id]}]
  (case operation
    :canonical-copy (a1/canonical-copy value)
    :admit-schema-registry (a1/admit-schema-registry value)
    :validate-and-copy (a1/validate-and-copy registry schema-id value)))

(deftest literal-fixture-catalogs-match-closed-results
  (doseq [case (:literal-cases (fixture "accepted.edn"))]
    (is (= "accepted" (get (run-fixture case) "status")) (pr-str case))
    (is (= (:diagnostic case) (get (run-fixture case) "diagnostic"))))
  (doseq [case (:literal-cases (fixture "rejected.edn"))]
    (let [actual (run-fixture case)]
      (is (= (:diagnostic case) (get actual "diagnostic")) (pr-str case))
      (is (= (:path case) (get actual "path"))))))

(deftest public-surface-and-total-arity-are-closed
  (is (= '#{canonical-copy admit-schema-registry validate-and-copy}
         (set (keys (ns-publics 'gravity.self-hosting.a1-canonical-schema)))))
  (doseq [[operation calls]
          [[a1/canonical-copy [[] [nil nil]]]
           [a1/admit-schema-registry [[] [{} {}]]]
           [a1/validate-and-copy [[] [{}] [{} "nil"]
                                  [{} "nil" nil nil]]]]
          args calls]
    (is (= (result "E-TYPE" ["arguments"])
           (apply operation args)))))

(deftest canonical-copy-accepts-only-the-closed-host-domain
  (doseq [value [nil true false 0 0N Long/MAX_VALUE
                 9223372036854775808N 18446744073709551615N
                 "" "lambda" "\uD83D\uDE42" [] [nil true 7] {} {"x" [1]}]]
    (is (= "accepted" (get (a1/canonical-copy value) "status")) (pr-str value)))
  (let [small (get (a1/canonical-copy 42N) "value")
        large (get (a1/canonical-copy 9223372036854775808N) "value")
        vector-value (get (a1/canonical-copy [1 2]) "value")
        map-value (get (a1/canonical-copy {"x" 1}) "value")]
    (is (= Long (class small)))
    (is (= BigInt (class large)))
    (is (= PersistentVector (class vector-value)))
    (is (= PersistentHashMap (class map-value))))
  (let [surrogate (String. (char-array [(char 0xD800)]))]
    (doseq [value [-1 1.0 1/2 1M :keyword 'symbol #{} '(1) (subvec [1 2] 0 1)
                   (sorted-map "a" 1) (ArrayList.) (HashMap.)
                   (object-array 0) (BigInteger/valueOf 1) identity surrogate
                   (with-meta [] {:origin :host})]]
      (is (= "E-TYPE" (get (a1/canonical-copy value) "diagnostic"))
          (str (class value)))))
  (let [throwing-meta (ThrowingMeta.)]
    (is (= (result "E-TYPE" []) (a1/canonical-copy throwing-meta)))))

(deftest map-order-and-mixed-key-failures-are-stable
  (let [left (phm [["z" 1] ["aa" 2] ["a" 3]])
        right (phm [["a" 3] ["z" 1] ["aa" 2]])]
    (is (= (a1/canonical-copy left) (a1/canonical-copy right))))
  (doseq [value [(phm [[1 "bad"] ["good" 1]])
                 (phm [["good" 1] [1 "bad"]])]]
    (is (= (result "E-TYPE" []) (a1/canonical-copy value)))))

(deftest near-exhaustion-mixed-key-order-cannot-change-the-diagnostic
  (let [prefix (concat (repeat 63 (vec (repeat 1024 nil)))
                       [(vec (repeat 946 nil))])
        left (vec (concat prefix [(array-map 1 nil "a" nil)]))
        right (vec (concat prefix [(array-map "a" nil 1 nil)]))
        left-result (a1/canonical-copy left)
        right-result (a1/canonical-copy right)]
    (is (= left-result right-result))
    (is (= "E-BOUND" (get left-result "diagnostic")))))

(deftest fixed-index-mergesort-bounds-reverse-and-interleaved-1024-keys
  (let [sort-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             'bottom-up-mergesort)
        sort! (var-get sort-var)
        keys (mapv #(str (char %)) (range 1024))
        reverse-entries (mapv #(clojure.lang.MapEntry/create % nil)
                              (reverse keys))
        interleaved (concat (take-nth 2 keys) (take-nth 2 (rest keys)))
        interleaved-entries (mapv #(clojure.lang.MapEntry/create % nil) interleaved)
        sorted-reverse (mapv key (sort! reverse-entries 1024))
        sorted-interleaved (mapv key (sort! interleaved-entries 1024))]
    (is (= keys sorted-reverse))
    (is (= keys sorted-interleaved))))

(deftest registry-admission-covers-the-exact-schema-algebra
  (let [registry
        {"nil" {"kind" "null"}
         "bool" {"kind" "boolean"}
         "uint" {"kind" "uint64"}
         "text" {"kind" "string" "ascii-only" true "max-bytes" 8}
         "enum" {"kind" "enum" "values" ["a" "b"]}
         "array" {"kind" "array" "item" "uint" "min-items" 0
                  "max-items" 3 "unique" true}
         "object" {"kind" "object" "required" {"id" "uint"}
                   "optional" {"name" "text"}}
         "union" {"kind" "tagged-union" "tag-key" "type"
                  "variants" {"none" "nil" "some" "object"}}}]
    (is (= "accepted" (get (a1/admit-schema-registry registry) "status"))))
  (doseq [[registry diagnostic]
          [[{1 {"kind" "null"}} "E-TYPE"]
           [{"Bad" {"kind" "null"}} "E-ID-SYNTAX"]
           [{"a" {"kind" "array" "item" 1 "min-items" 0
                  "max-items" 1 "unique" false}} "E-ID-TYPE"]
           [{"a" {"kind" "array" "item" "missing" "min-items" 0
                  "max-items" 1 "unique" false}} "E-UNKNOWN-ID"]
           [{"a" {"kind" "array" "item" "b" "min-items" 0
                  "max-items" 1 "unique" false}
             "b" {"kind" "array" "item" "a" "min-items" 0
                  "max-items" 1 "unique" false}} "E-CYCLE"]
           [{"a" {"kind" "object" "required" {"x" "n"}
                  "optional" {"x" "n"}}
             "n" {"kind" "null"}} "E-SCHEMA"]
           [{"a" {"kind" "enum" "values" ["x" "x"]}} "E-SCHEMA"]
           [{"a" {"kind" "tagged-union" "tag-key" "t" "variants" {}}}
            "E-SCHEMA"]
           [{"a" {"kind" "any-json"}} "E-SCHEMA"]
           [{"a" {"kind" "untagged-union" "variants" ["a"]}} "E-SCHEMA"]]]
    (is (= diagnostic (get (a1/admit-schema-registry registry) "diagnostic"))
        (pr-str registry))))

(deftest phase-rank-path-order-and-argument-order-are-stable
  (is (= (result "E-ID-TYPE" ["b" "item"])
         (a1/admit-schema-registry
           {"a" {"kind" "bogus"}
            "b" {"kind" "array" "item" 1 "min-items" 0
                 "max-items" 1 "unique" false}})))
  (is (= "E-KEYSET"
         (get (a1/admit-schema-registry
                {"a" {"kind" "null" "extra" true}
                 "b" {"kind" "array" "item" "c" "min-items" 0
                      "max-items" 1 "unique" false}
                 "c" {"kind" "array" "item" "b" "min-items" 0
                      "max-items" 1 "unique" false}})
              "diagnostic")))
  (is (= "E-CYCLE"
         (get (a1/admit-schema-registry
                {"a" {"kind" "bogus"}
                 "b" {"kind" "array" "item" "c" "min-items" 0
                      "max-items" 1 "unique" false}
                 "c" {"kind" "array" "item" "b" "min-items" 0
                      "max-items" 1 "unique" false}})
              "diagnostic")))
  (is (= ["a!"]
         (get (a1/admit-schema-registry
                {"z!" {"kind" "null"} "a!" {"kind" "null"}})
              "path")))
  (is (= (result "E-TYPE" ["a"])
         (a1/validate-and-copy {"a" (Object.)} :bad identity))))

(deftest validation-selects-one-schema-and-copies-only-after-success
  (let [registry
        {"uint" {"kind" "uint64"}
         "text" {"kind" "string" "ascii-only" true "max-bytes" 4}
         "array" {"kind" "array" "item" "uint" "min-items" 1
                  "max-items" 3 "unique" true}
         "object" {"kind" "object" "required" {"id" "uint"}
                   "optional" {"name" "text"}}
         "union" {"kind" "tagged-union" "tag-key" "type"
                  "variants" {"number" "uint" "record" "object"}}}]
    (doseq [[schema value]
            [["uint" 18446744073709551615N]
             ["array" [1 2 3]]
             ["object" {"id" 7 "name" "Ada"}]
             ["union" {"type" "number" "value" 9}]]]
      (is (= "accepted"
             (get (a1/validate-and-copy registry schema value) "status"))))
    (doseq [[schema value diagnostic path]
            [["missing" nil "E-UNKNOWN-ID" ["schema-id"]]
             ["array" [] "E-SCHEMA" []]
             ["array" [1 1] "E-SCHEMA" [1]]
             ["object" {"name" "Ada"} "E-KEYSET" []]
             ["object" {"id" 1 "extra" 2} "E-KEYSET" []]
             ["union" {"type" 1 "value" 2} "E-TYPE" ["type"]]
             ["union" {"type" "other" "value" 2} "E-SCHEMA" ["type"]]
             ["union" {"type" "number"} "E-KEYSET" []]]]
      (is (= (result diagnostic path)
             (a1/validate-and-copy registry schema value))))
    (is (= "E-ID-TYPE"
           (get (a1/validate-and-copy registry :uint 1) "diagnostic")))
    (is (= "E-ID-SYNTAX"
           (get (a1/validate-and-copy registry "Bad" 1) "diagnostic")))))

(deftest bounds-are-terminal-and-precede-later-faults
  (let [too-long (apply str (repeat 65537 "a"))
        too-many (vec (repeat 1025 nil))
        high-work (phm (map (fn [index]
                              [(format "key-%04d-%040d" index index) nil])
                            (range 1024)))
        too-many-schemas
        (phm (map (fn [index]
                    [(format "s%03d" index) {"kind" "null"}])
                  (range 513)))]
    (doseq [value [too-long too-many high-work]]
      (is (= "E-BOUND" (get (a1/canonical-copy value) "diagnostic"))))
    (is (= "E-BOUND"
           (get (a1/admit-schema-registry too-many-schemas) "diagnostic"))))
  (let [later-invalid (conj (vec (repeat 1024 nil)) identity)]
    (is (= "E-BOUND" (get (a1/canonical-copy later-invalid) "diagnostic")))))

(deftest exact-resource-edges-are-accepted-before-limit-plus-one
  (is (= "accepted" (get (a1/canonical-copy (apply str (repeat 65536 "a")))
                          "status")))
  (is (= "accepted" (get (a1/canonical-copy (vec (repeat 1024 nil))) "status")))
  (let [registry (into {} (map #(vector % {"kind" "null"})
                               (short-schema-ids)))]
    (is (= 512 (count registry)))
    (is (= "accepted" (get (a1/admit-schema-registry registry) "status"))))
  (is (= "accepted" (get (a1/canonical-copy (value-with-meter 750000)) "status")))
  (is (= "E-BOUND" (get (a1/canonical-copy (value-with-meter 750001))
                         "diagnostic")))
  (let [budget-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema 'budget)
        meter-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema 'meter-value!)
        exact-state ((var-get budget-var))
        over-state ((var-get budget-var))]
    ((var-get meter-var) exact-state (value-with-meter 786432) [] 0 :input)
    (is (= 786432 (get-in @exact-state [:input :committed])))
    (is (= "E-BOUND"
           (try
             ((var-get meter-var) over-state (value-with-meter 786433) [] 0 :input)
             "OK"
             (catch clojure.lang.ExceptionInfo failure
               (:diagnostic (ex-data failure))))))))

(deftest exact-input-meter-edge-is-observed-through-the-public-wrapper
  (let [registry
        (into {"nil" {"kind" "null"}}
              (map (fn [index]
                     [(str "p" index)
                      {"kind" "tagged-union"
                       "tag-key" (str (char (+ (int \A) index))
                                      (apply str
                                             (repeat (if (< index 11) 65535 64444)
                                                     "x")))
                       "variants" {"x" "nil"}}])
                   (range 12)))
        over-registry (update-in registry ["p11" "tag-key"] str "x")]
    (is (= "accepted"
           (get (a1/validate-and-copy registry "nil" nil) "status")))
    (is (= "E-BOUND"
           (get (a1/validate-and-copy over-registry "nil" nil) "diagnostic")))))

(deftest exact-work-edge-completes-through-the-public-wrapper
  (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             '*audit-sink*)
        sink (atom nil)
        value (into {}
                    (map (fn [index]
                           [(str "k" index
                                 (when (= index 299)
                                   (apply str (repeat 936 "x"))))
                            (cond
                              (< index 4) [nil]
                              (< index 7) 0N
                              :else nil)])
                         (range 300)))]
    (with-bindings {sink-var sink}
      (is (= "accepted" (get (a1/canonical-copy value) "status"))))
    (is (= 65536 (get-in @sink [:work :committed])))
    (is (zero? (get-in @sink [:work :reserved])))))

(deftest reservation-commit-and-live-counters-close-at-zero
  (let [resolve-private #(var-get
                           (ns-resolve 'gravity.self-hosting.a1-canonical-schema %))
        make-budget (resolve-private 'budget)
        reserve! (resolve-private 'reserve!)
        commit! (resolve-private 'commit!)
        release-reservation! (resolve-private 'release-reservation!)
        acquire! (resolve-private 'acquire!)
        release! (resolve-private 'release!)
        state (make-budget)]
    (reserve! state :output 100 [])
    (commit! state :output 60)
    (release-reservation! state :output 40)
    (is (= {:limit 750000 :reserved 0 :committed 60}
           (:output @state)))
    (acquire! state :key-slots 1024 [])
    (release! state :key-slots 1024)
    (is (= 0 (get-in @state [:key-slots :live])))
    (is (= 1024 (get-in @state [:key-slots :peak])))
    (is (= "E-BOUND"
           (try (reserve! state :output 750000 []) "OK"
                (catch clojure.lang.ExceptionInfo failure
                  (:diagnostic (ex-data failure))))))
    (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                               '*audit-sink*)
          sink (atom nil)]
      (with-bindings {sink-var sink} (a1/canonical-copy nil))
      (is (zero? (get-in @sink [:work :reserved])))
      (is (zero? (get-in @sink [:output :reserved])))
      (is (false? (get-in @sink [:terminal-result :reserved?])))
      (is (zero? (get-in @sink [:terminal-result :reserved])))
      (is (= 128 (get-in @sink [:terminal-result :committed])))
      (doseq [counter [:frames :key-slots :digest-slots]]
        (is (zero? (get-in @sink [counter :live])))))))

(deftest diagnostic-path-reservation-falls-back-to-terminal-empty-bound
  (let [resolve-private #(var-get
                           (ns-resolve 'gravity.self-hosting.a1-canonical-schema %))
        state ((resolve-private 'budget))
        path-of (resolve-private 'path-of)
        emit! (resolve-private 'emit-rejection!)]
    (swap! state assoc-in [:work :committed] 65526)
    (is (= (result "E-BOUND" [])
           (emit! state "E-TYPE" (path-of "field"))))
    (is (zero? (get-in @state [:output :committed])))
    (is (zero? (get-in @state [:output :reserved])))))

(deftest depth-limit-allows-root-through-sixty-four
  (letfn [(nested [depth]
            (loop [remaining depth value nil]
              (if (zero? remaining) value
                  (recur (dec remaining) [value]))))]
    (is (= "accepted" (get (a1/canonical-copy (nested 64)) "status")))
    (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                               '*audit-sink*)
          sink (atom nil)
          actual (with-bindings {sink-var sink}
                   (a1/canonical-copy (nested 65)))]
      (is (= (result "E-BOUND" (vec (repeat 64 0))) actual))
      (is (= (* 64 9) (get-in @sink [:output :committed])))
      (is (zero? (get-in @sink [:output :reserved])))
      (is (zero? (get-in @sink [:work :reserved])))
      (is (false? (get-in @sink [:terminal-result :reserved?])))
      (doseq [counter [:frames :key-slots :digest-slots]]
        (is (zero? (get-in @sink [counter :live])))))))

(deftest schema-reference-depth-has-the-same-sixty-four-edge-boundary
  (letfn [(chain [edges]
            (into {}
                  (concat
                    [[(str "s" edges) {"kind" "null"}]]
                    (map (fn [index]
                           [(str "s" index)
                            {"kind" "array" "item" (str "s" (inc index))
                             "min-items" 0 "max-items" 1 "unique" false}])
                         (range edges)))))]
    (is (= "accepted" (get (a1/admit-schema-registry (chain 64)) "status")))
    (is (= "E-BOUND" (get (a1/admit-schema-registry (chain 65)) "diagnostic")))))

(deftest wide-reference-traversal-retains-one-child-cursor-at-a-time
  (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             '*audit-sink*)
        sink (atom nil)
        fields (into {} (map (fn [index] [(str (char index)) "leaf"])
                             (range 500)))
        registry {"leaf" {"kind" "null"}
                  "root" {"kind" "object" "required" fields "optional" {}}}]
    (with-bindings {sink-var sink}
      (is (= "accepted" (get (a1/admit-schema-registry registry) "status"))))
    (is (<= (get-in @sink [:frames :peak]) 65))
    (is (zero? (get-in @sink [:frames :live])))))

(deftest selected-tagged-branch-does-not-evaluate-unselected-branch
  (let [registry
        {"nil" {"kind" "null"}
         "deep" {"kind" "array" "item" "nil" "min-items" 1024
                 "max-items" 1024 "unique" true}
         "union" {"kind" "tagged-union" "tag-key" "tag"
                  "variants" {"good" "nil" "hostile" "deep"}}}]
    (is (= "accepted"
           (get (a1/validate-and-copy registry "union"
                                     {"tag" "good" "value" nil})
                "status")))))

(deftest rejected-untagged-hostile-does-not-build-a-reference-graph
  (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             '*audit-sink*)
        sink (atom nil)
        registry (into {}
                       (map (fn [index]
                              [(str "s" index)
                               {"kind" "untagged-union"
                                "variants" ["s0" "s1"]}])
                            (range 20)))]
    (with-bindings {sink-var sink}
      (is (= "E-SCHEMA"
             (get (a1/admit-schema-registry registry) "diagnostic"))))
    (is (not (true? (:registry-graph-built? @sink))))
    (is (<= (get-in @sink [:work :committed]) 65526))
    (doseq [counter [:frames :key-slots :digest-slots]]
      (is (zero? (get-in @sink [counter :live]))))))

(deftest unselected-hostile-branch-adds-no-phase-three-work
  (let [sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             '*audit-sink*)
        base {"nil" {"kind" "null"}
              "union" {"kind" "tagged-union" "tag-key" "tag"
                       "variants" {"good" "nil"}}}
        hostile (assoc base
                       "deep" {"kind" "array" "item" "nil" "min-items" 1024
                               "max-items" 1024 "unique" true}
                       "union" {"kind" "tagged-union" "tag-key" "tag"
                                "variants" {"good" "nil" "hostile" "deep"}})
        value {"tag" "good" "value" nil}
        run (fn [registry]
              (let [sink (atom nil)]
                (with-bindings {sink-var sink}
                  (is (= "accepted"
                         (get (a1/validate-and-copy registry "union" value)
                              "status"))))
                @sink))
        base-audit (run base)
        hostile-audit (run hostile)]
    (is (= (- (get-in base-audit [:work :committed])
              (:phase2-work base-audit))
           (- (get-in hostile-audit [:work :committed])
              (:phase2-work hostile-audit))))
    (doseq [audit [base-audit hostile-audit]
            counter [:frames :key-slots :digest-slots]]
      (is (zero? (get-in audit [counter :live]))))))

(deftest host-exceptions-are-total-and-interrupt-is-preserved
  (let [constructor (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                                'construct-map)
        sink-var (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                             '*audit-sink*)
        sink (atom nil)]
    (with-bindings {sink-var sink}
      (with-redefs-fn {constructor (fn [_] (throw (Exception. "injected")))}
        #(is (= (result "E-HOST" ["internal"])
                (a1/canonical-copy {"x" 1})))))
    (is (= 33 (get-in @sink [:output :committed])))
    (is (zero? (get-in @sink [:output :reserved])))
    (with-redefs-fn {constructor (fn [_] (throw (InterruptedException. "stop")))}
      #(do
         (let [actual (a1/canonical-copy {"x" 1})
               interrupted? (.isInterrupted (Thread/currentThread))]
           (Thread/interrupted)
           (is (= (result "E-HOST" ["internal"]) actual))
           (is interrupted?))))))
