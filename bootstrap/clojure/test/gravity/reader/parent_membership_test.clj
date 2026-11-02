(ns gravity.reader.parent-membership-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.c2-lexical-validation :as validation]
            [gravity.c2-lexical-validation-test :as fixture]))

(defn- report [forms roots]
  (validation/c2-lexical-product-validation
   fixture/source-text fixture/token-stream forms roots))

(defn- reference-membership [forms roots]
  (let [by-id (into {} (map (juxt :form-id identity)) forms)
        frequency (frequencies (mapcat :children forms))
        roots (set roots)]
    (every? (fn [{id :form-id parent-id :parent-form-id}]
              (if (contains? roots id)
                (and (nil? parent-id) (zero? (get frequency id 0)))
                (let [parent (by-id parent-id)]
                  (and parent (= 1 (get frequency id 0))
                       (= 1 (count (filter #{id} (:children parent))))))))
            forms)))

(deftest indexed-membership-agrees-with-sibling-scan
  ;; Exhaust all edges and declared parents of three nodes, including cycles,
  ;; self edges, wrong parents, missing parents and shared children.
  (let [ids [:a :b :c]
        base (second fixture/form-tree)]
    (doseq [mask (range 512)
            parents [[nil :a :a] [nil :b :c] [:missing nil :b]]]
      (let [forms (mapv (fn [i id parent]
                          (assoc base :form-id id :parent-form-id parent
                                 :children (filterv #(bit-test mask (+ (* i 3) (.indexOf ids %))) ids)))
                        (range 3) ids parents)]
        (is (= (boolean (reference-membership forms [:a]))
               (:non-root-single-parent? (report forms [:a]))) (str mask parents))))))

(deftest malformed-links-and-last-wins-parents-remain-rejected
  (doseq [forms [(assoc-in fixture/form-tree [0 :children] [:form-1 :form-1])
                (assoc-in fixture/form-tree [1 :parent-form-id] :missing)
                (assoc-in fixture/form-tree [0 :children] [])
                (conj fixture/form-tree (assoc (first fixture/form-tree) :children []))
                (into [(assoc (first fixture/form-tree) :children [])] fixture/form-tree)]]
    (let [result (report forms [:form-0])]
      (is (= :failed (:status result)))
      (is (= (boolean (reference-membership forms [:form-0]))
             (:non-root-single-parent? result))))))

(deftest membership-is-fresh-and-keeps-independent-checks
  (let [original (report fixture/form-tree [:form-0])
        changed (report (assoc-in fixture/form-tree [1 :parent-form-id] nil) [:form-0])]
    (is (= :passed (:status original)))
    (is (true? (:non-root-single-parent? original)))
    (is (false? (:non-root-single-parent? changed)))
    (is (false? (:parent-child-bidirectional? changed)))
    (is (= original (report fixture/form-tree [:form-0]))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest wide-parent-does-not-rescan-siblings
  (let [width 128 ids (mapv #(keyword (str "child-" %)) (range width))
        forms (into [(assoc (first fixture/form-tree) :children ids)]
                    (map #(assoc (second fixture/form-tree) :form-id %) ids))
        visits (atom 0) original-filter filter
        count-filter (fn [predicate items]
                       (original-filter (fn [item] (swap! visits inc) (predicate item)) items))]
    (with-redefs [clojure.core/filter count-filter]
      (is (true? (reference-membership forms [:form-0]))))
    (is (= (* width width) @visits))
    (reset! visits 0)
    (with-redefs [clojure.core/filter count-filter]
      (is (true? (:non-root-single-parent? (report forms [:form-0])))))
    (is (zero? @visits))))

(deftest opaque-parents-and-ids-keep-the-scan-fallback
  (let [calls (atom 0)
        base (first fixture/form-tree)
        parent (reify clojure.lang.ILookup
                 (valAt [_ key] (swap! calls inc) (get base key))
                 (valAt [_ key missing] (swap! calls inc) (get base key missing)))
        qualify (ns-resolve 'gravity.c2-lexical-validation.product 'native-child-ids)]
    (is (nil? (qualify parent)))
    (is (zero? @calls))
    (is (true? (:non-root-single-parent? (report [parent (second fixture/form-tree)] [:form-0])))))
  (doseq [id [nil "child" 17 false]]
    (let [forms [(assoc (first fixture/form-tree) :children [id])
                 (assoc (second fixture/form-tree) :form-id id)]]
      (is (= (boolean (reference-membership forms [:form-0]))
             (:non-root-single-parent? (report forms [:form-0])))))))
