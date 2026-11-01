(ns gravity.self-hosting.hosted-native-data-preflight-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(def ^:private path "hosted-native-preflight-test.gravity")
(def ^:private source
  "(ns hosted.preflight (:profile :hosted) (:target :jvm) (:effects #{:memory/allocate}) (:capabilities #{:memory/allocator}))\n(defn check [value] (hosted-native-data-preflight value))")
(def ^:private plan
  (delay (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
           (b/p15-s23-stage2-compiler-artifact-plan emitter path source))))
(defn- check [x]
  (b/p15-s23-stage2-runtime-execute-function {:engine :hosted-native-data-preflight-test} @plan 'check [x]))
(defn- builtin [p name args] (b/p15-s23-stage2-runtime-invoke-builtin p name args))
(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (select-keys (ex-data error) [:id :reason]))))
(defn- field! [x name value]
  (let [field (.getDeclaredField (class x) name)]
    (is (.trySetAccessible field)) (.set field x value) x))
(defn- raw-array-map [entries] (clojure.lang.PersistentArrayMap. (object-array entries)))

(deftype ForeignMeta [calls]
  clojure.lang.IMeta (meta [_] (swap! calls inc) (throw (ex-info "foreign meta" {}))))
(deftype ForeignTail [calls]
  clojure.lang.ISeq
  (first [_] (swap! calls inc) nil)
  (next [_] (swap! calls inc) nil)
  (more [_] (swap! calls inc) nil)
  (cons [this _] (swap! calls inc) this)
  (count [_] (swap! calls inc) 0)
  (empty [this] (swap! calls inc) this)
  (equiv [_ _] (swap! calls inc) false)
  (seq [this] (swap! calls inc) this))
(defn- foreign-vector [calls]
  (reify clojure.lang.IPersistentVector
    (length [_] (swap! calls inc) 0)
    (nth [_ _] (swap! calls inc) nil)
    (nth [_ _ _] (swap! calls inc) nil)
    (assocN [this _ _] (swap! calls inc) this)
    (cons [this _] (swap! calls inc) this)
    (count [_] (swap! calls inc) 0)
    (empty [this] (swap! calls inc) this)
    (equiv [_ _] (swap! calls inc) false)
    (seq [_] (swap! calls inc) nil)
    (peek [_] (swap! calls inc) nil)
    (pop [this] (swap! calls inc) this)
    (containsKey [_ _] (swap! calls inc) false)
    (entryAt [_ _] (swap! calls inc) nil)
    (assoc [this _ _] (swap! calls inc) this)
    (valAt [_ _] (swap! calls inc) nil)
    (valAt [_ _ _] (swap! calls inc) nil)))

(deftest hosted-native-data-is-finite-and-preserved
  (doseq [x [nil false 7 7N 3/7 \a 'symbol :keyword "text"
             {:op :quote :value [{:op :println :value :inert}]}
             (list 1 2) (clojure.lang.Cons. 1 (list 2)) #{1 2}
             (sorted-map :a 1) (sorted-set :a :b)]]
    (is (= {:status :passed} (check x))))
  (let [leaf {:shared true} x [leaf leaf]]
    (is (= {:status :passed} (check x))))
  (is (= {:status :passed} (check (reduce (fn [x _] [x]) :leaf (range 1000)))))
  (is (= {:status :passed} (check (vec (repeat 33000 true)))))
  (let [metadata {:op :println :value [1 2]} x (with-meta ['x] metadata)
        symbol (with-meta 'symbol metadata)]
    (is (= {:status :passed} (check x)))
    (is (= {:status :passed} (check symbol)))
    (is (identical? metadata (meta x)))
    (is (identical? metadata (meta symbol)))))

(deftest hosted-native-views-have-explicit-visible-data-policy
  (let [calls (atom 0) hidden (ForeignMeta. calls)
        backing (with-meta [hidden :visible :also-visible hidden] {:unused hidden})
        view (subvec backing 1 3) nested (clojure.lang.APersistentVector$SubVector. nil view 0 1)]
    (is (= {:status :passed} (check view)))
    (is (= {:status :passed} (check nested)))
    (is (identical? backing (.-v ^clojure.lang.APersistentVector$SubVector view)))
    (is (= 0 @calls)))
  (doseq [[view reason]
          [[(clojure.lang.APersistentVector$SubVector. nil [1] 0 2) :invalid-vector-view-extent]
           [(clojure.lang.APersistentVector$SubVector. nil [1] -1 1) :invalid-vector-view-extent]]]
    (is (= {:status :rejected :reason reason} (check view))))
  (let [calls (atom 0) view (clojure.lang.APersistentVector$SubVector. nil (foreign-vector calls) 0 0)]
    (is (= {:status :rejected :reason :untrusted-vector-backing} (check view)))
    (is (= 0 @calls)))
  (let [view (clojure.lang.APersistentVector$SubVector. nil [] 0 0)]
    (field! view "v" view)
    (is (= {:status :rejected :reason :cyclic-vector-backing} (check view)))))

(deftest hosted-native-data-rejects-cycles-and-foreign-callbacks
  (let [calls (atom 0) x (ForeignMeta. calls)]
    (is (= {:status :rejected :reason :untrusted-scalar-class} (check x)))
    (is (= {:status :rejected :reason :untrusted-scalar-class} (check (with-meta [] {:hidden x}))))
    (is (= 0 @calls)))
  (let [calls (atom 0) x (foreign-vector calls)]
    (is (= {:status :rejected :reason :untrusted-vector-class} (check x)))
    (is (= 0 @calls)))
  (let [x (raw-array-map [:cycle nil])]
    (field! x "array" (object-array [:cycle x]))
    (is (= {:status :rejected :reason :cyclic-carrier} (check x))))
  (let [x (with-meta [] {:cycle nil}) metadata (meta x)]
    (field! metadata "array" (object-array [:cycle x]))
    (is (= {:status :rejected :reason :cyclic-carrier} (check x))))
  (let [calls (atom 0) x (clojure.lang.Cons. 1 (list 2))]
    (field! x "_more" (ForeignTail. calls))
    (is (= {:status :rejected :reason :untrusted-list-tail-class} (check x)))
    (is (= 0 @calls)))
  (let [calls (atom 0) cmp (reify java.util.Comparator (compare [_ a z] (swap! calls inc) (compare a z)))
        x (sorted-map-by cmp :a 1 :b 2)]
    (reset! calls 0)
    (is (= {:status :rejected :reason :untrusted-map-class-or-comparator} (check x)))
    (is (= 0 @calls))))

(deftest existing-native-domains-and-profile-gates-remain-strict
  (doseq [x [(with-meta [] {:line 1}) (subvec [1 2] 0 1)]]
    (is (= :rejected (:status (b/p15-s23-trusted-carrier-validation x :default-only))))
    (is (= (b/p15-s23-trusted-carrier-validation x :default-only)
           (b/p15-s23-trusted-carrier-validation x :default-only 1 1 1))))
  (let [calls (atom 0) x (ForeignMeta. calls)]
    (doseq [bad [(assoc-in @plan [:module :profile] :meta)
                 (assoc-in @plan [:module :target] :native)
                 (dissoc @plan :compiler-artifact-plan?)]]
      (is (= {:id "HOSTED-NATIVE-DATA" :reason :hosted-jvm-plan-required}
             (diagnostic #(builtin bad 'hosted-native-data-preflight [x])))))
    (is (= "L2-BUILTIN-ARITY" (:id (diagnostic #(builtin @plan 'hosted-native-data-preflight [x x])))))
    (doseq [[name args] [['bootstrap-native-carrier-header [x]] ['bootstrap-native-identical? [x x]]]]
      (is (some? (diagnostic #(builtin @plan name args)))))
    (is (= 0 @calls))))

(deftest hosted-native-data-rejects-foreign-maps-and-opaque-keys-before-callbacks
  (let [calls (atom 0) deny (fn [] (swap! calls inc) (throw (AssertionError. "foreign callback")))
        foreign-map (java.lang.reflect.Proxy/newProxyInstance
                     (.getClassLoader clojure.lang.IPersistentMap)
                     (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
                     (reify java.lang.reflect.InvocationHandler (invoke [_ _ _ _] (deny))))
        opaque (proxy [Object] [] (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny)))
        keyed (raw-array-map [opaque :value])]
    (doseq [x [foreign-map [foreign-map] (with-meta [] foreign-map)]]
      (is (= {:status :rejected :reason :untrusted-map-class-or-comparator} (check x))))
    (doseq [x [opaque keyed]]
      (is (= {:status :rejected :reason :untrusted-scalar-class} (check x))))
    (is (= 0 @calls))))
