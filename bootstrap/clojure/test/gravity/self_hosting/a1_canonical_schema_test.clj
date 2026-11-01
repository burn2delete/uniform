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
  (is (= '([value path])
         (:arglists (meta (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                                      'schema-id!)))))
  (is (= '([value])
         (:arglists (meta (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                                      'canonical-uint)))))
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
    (is (= "E-TYPE" (get left-result "diagnostic")))))

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

(deftest larger-valid-values-keep-the-closed-domain
  (let [long-string (apply str (repeat 65537 "a"))
        large-vector (vec (repeat 1025 nil))
        large-map (phm (map (fn [index] [(str "key-" index) nil])
                            (range 1025)))
        large-registry (phm (map (fn [index]
                                   [(str "s" index) {"kind" "null"}])
                                 (range 513)))]
    (doseq [value [long-string large-vector large-map]]
      (let [actual (a1/canonical-copy value)]
        (is (= "accepted" (get actual "status")))
        (is (= value (get actual "value")))))
    (is (= "accepted"
           (get (a1/admit-schema-registry large-registry) "status")))))

(deftest schema-identifiers-retain-alphabet-without-length-quota
  (let [long-id (str "schema-" (apply str (repeat 128 "x")))
        invalid-id (str long-id "!")]
    (is (= "accepted"
           (get (a1/admit-schema-registry
                 {long-id {"kind" "null"}})
                "status")))
    (is (= "E-ID-SYNTAX"
           (get (a1/admit-schema-registry
                 {invalid-id {"kind" "null"}})
                "diagnostic")))))

(deftest declared-schema-bounds-remain-semantic
  (let [registry {"text" {"kind" "string" "ascii-only" true "max-bytes" 3}
                  "array" {"kind" "array" "item" "text"
                            "min-items" 0 "max-items" 2 "unique" false}}]
    (is (= "E-SCHEMA"
           (get (a1/validate-and-copy registry "text" "four") "diagnostic")))
    (is (= "E-SCHEMA"
           (get (a1/validate-and-copy registry "array"
                                      ["one" "two" "tri"]) "diagnostic")))))

(deftest nested-values-and-reference-graphs-have-no-global-depth-quota
  (letfn [(nested [depth]
            (loop [remaining depth value nil]
              (if (zero? remaining) value
                  (recur (dec remaining) [value]))))]
    (is (= "accepted" (get (a1/canonical-copy (nested 65)) "status")))))

(deftest schema-reference-graphs-retain-cycle-rejection
  (letfn [(chain [edges]
            (into {}
                  (concat
                    [[(str "s" edges) {"kind" "null"}]]
                    (map (fn [index]
                           [(str "s" index)
                            {"kind" "array" "item" (str "s" (inc index))
                             "min-items" 0 "max-items" 1 "unique" false}])
                         (range edges)))))]
    (is (= "accepted" (get (a1/admit-schema-registry (chain 65)) "status")))
    (is (= "E-CYCLE"
           (get (a1/admit-schema-registry
                 {"a" {"kind" "array" "item" "a"
                        "min-items" 0 "max-items" 1 "unique" false}})
                "diagnostic")))))

(deftest wide-reference-traversal-preserves-field-validation
  (let [fields (into {} (map (fn [index] [(str "field-" index) "leaf"])
                             (range 500)))
        registry {"leaf" {"kind" "null"}
                  "root" {"kind" "object" "required" fields "optional" {}}}]
    (is (= "accepted" (get (a1/admit-schema-registry registry) "status")))))

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

(deftest rejected-untagged-hostile-retains-schema-diagnostic
  (let [registry (into {}
                       (map (fn [index]
                              [(str "s" index)
                               {"kind" "untagged-union"
                                "variants" ["s0" "s1"]}])
                            (range 20)))]
    (is (= "E-SCHEMA"
           (get (a1/admit-schema-registry registry) "diagnostic")))))

(deftest unselected-hostile-branch-does-not-change-validation
  (let [base {"nil" {"kind" "null"}
              "union" {"kind" "tagged-union" "tag-key" "tag"
                       "variants" {"good" "nil"}}}
        hostile (assoc base
                       "deep" {"kind" "array" "item" "nil" "min-items" 1024
                               "max-items" 1024 "unique" true}
                       "union" {"kind" "tagged-union" "tag-key" "tag"
                                "variants" {"good" "nil" "hostile" "deep"}})
        value {"tag" "good" "value" nil}
        run (fn [registry]
              (get (a1/validate-and-copy registry "union" value) "status"))]
    (is (= "accepted" (run base)))
    (is (= "accepted" (run hostile)))))

(deftest host-exceptions-are-total-and-interrupt-is-preserved
  (let [constructor (ns-resolve 'gravity.self-hosting.a1-canonical-schema
                                'construct-map)]
    (with-redefs-fn {constructor (fn [_] (throw (Exception. "injected")))}
      #(let [actual (a1/canonical-copy {"x" 1})]
         (is (= (result "E-HOST" ["internal"]) actual))
         (is (vector? (get actual "path")))))
    (with-redefs-fn {constructor (fn [_] (throw (ex-info "foreign" {})))}
      #(let [actual (a1/canonical-copy {"x" 1})]
         (is (= (result "E-HOST" ["internal"]) actual))
         (is (vector? (get actual "path")))))
    (with-redefs-fn {constructor (fn [_] (throw (InterruptedException. "stop")))}
      #(do
         (let [actual (a1/canonical-copy {"x" 1})
               interrupted? (.isInterrupted (Thread/currentThread))]
           (Thread/interrupted)
           (is (= (result "E-HOST" ["internal"]) actual))
           (is (vector? (get actual "path")))
           (is interrupted?))))))
