(ns gravity.reader.forms-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.reader :as reader]))

(defn- operations []
  {:digest-var #'b/sha256-hex :pipeline-fail-var #'b/stage1-reader-pipeline-fail!
   :line-starts-var #'b/line-start-indices :token-index-var #'b/stage1-reader-token-index
   :span-var #'b/stage1-reader-span :canonical-hash-var #'b/reader-canonical-hash
   :decode-atom-var #'b/stage1-reader-decode-atom :fail-var #'b/stage1-reader-fail!
   :read-tagged-var #'edn/read-string :form-kind-var #'b/form-kind})

(defn- parse [path text table tokens]
  (reader/build-compatibility-forms path text table tokens (operations)))

(defn- tokenize [path text table retain]
  (b/stage1-reader-token-stream path text table {:retain-comments retain}))

(defn- rejection [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo ex ex)))

(deftest form-builder-contract-declares-ports-and-no-authentication
  (let [contract (:gravity/namespace-contract (meta #'reader/build-compatibility-forms))
        api (get-in contract [:public-api 'build-compatibility-forms])]
    (is (= :hosted-native-token-stream-form-construction (:contract-boundary api)))
    (is (= '([source-path source-text table token-stream operations]) (:arglists api)))
    (is (= (set (keys (operations))) (set (:operation-inputs api))))
    (is (= :none (:admission api)))
    (is (false? (:authenticates-inputs? api)))
    (is (false? (:native-join-checks-authenticate? api)))
    (is (false? (:opens-source? api)))
    (is (false? (:callback-type-checks? api)))
    (is (= 'gravity.reader.forms-test (:test-owner api)))
    (is (= ['clojure.core] (get-in contract [:dependency-direction :requires])))))

(deftest actual-paired-source-preserves-records-raw-slices-and-flat-links
  (let [table (b/stage1-reader-table)]
    (doseq [extension [".gravity" ".qst"] retain [false true]]
      (let [path (str "bootstrap/clojure/fixtures/accepted/surface-syntax" extension)
            text (slurp path) tokens (tokenize path text table retain)
            result (parse path text table tokens)
            by-id (into {} (map (juxt :form-id identity) (:form-tree result)))]
        (is (= result (b/stage1-reader-products-from-token-stream path text table tokens)))
        (is (= result (parse path text table tokens)))
        (is (= #{:records :form-tree :root-form-ids :parsed-values} (set (keys result))))
        (is (= (:root-form-ids result) (mapv :form-id (:records result))))
        (is (= (:parsed-values result) (mapv :form (:records result))))
        (is (= {:doc "metadata survives reading"} (meta (second (:parsed-values result)))))
        (is (= (count (:form-tree result)) (count by-id)))
        (doseq [{:keys [form-id children parent-form-id raw span origin] :as node} (:form-tree result)]
          (is (= path (:source span)))
          (is (= raw (subs text (get-in span [:start :char]) (get-in span [:end :char]))))
          (is (= {:kind :source :reader :stage1-token-parser} origin))
          (is (not (contains? node :reader-operations)))
          (doseq [child children]
            (is (contains? by-id child))
            (is (= form-id (:parent-form-id (by-id child)))))
          (when parent-form-id (is (some #{form-id} (:children (by-id parent-form-id))))))))))

(deftest metadata-generated-origins-tagged-values-and-deferral-survive
  (let [path "forms.qst" table (b/stage1-reader-table)
        flags {:doc "kept" :ai/human-review true :unsafe-island true :runtime-checked true}
        text (str "^{:doc \"kept\" :ai/human-review true :unsafe-island true :runtime-checked true} "
                  "(def v {:ratio 1/0}) '(x) `(a ~x ~@xs) @v "
                  "#uuid \"00000000-0000-0000-0000-000000000000\" #inst \"2020-01-01\"")
        result (parse path text table (tokenize path text table true))
        tree (:form-tree result) generated (filter :generated-origin tree)]
    (is (= 6 (count (:parsed-values result))))
    (is (= flags (meta (first (:parsed-values result)))))
    (is (= flags (:metadata (first tree))))
    (is (= #{:metadata :quote :syntax-quote :unquote :splice-unquote :deref}
           (set (map :abbrev generated))))
    (doseq [node generated]
      (is (= path (get-in node [:surface-span :source])))
      (is (= (:abbrev node) (get-in node [:generated-origin 0 :reason])))
      (is (some #{(get-in node [:generated-origin 0 :child-form-id])} (:children node))))
    (is (some #(and (= "1/0" (:raw %)) (= :deferred (get-in % [:value :semantic-validation]))) tree))
    (is (instance? java.util.UUID (nth (:parsed-values result) 4)))
    (is (instance? java.util.Date (nth (:parsed-values result) 5)))))

(deftest recursive-composite-set-keys-build-or-produce-the-required-duplicate-refusal
  (let [table (b/stage1-reader-table) path "sets.gravity"]
    (doseq [text ["#{[1] [2]}" "#{{:a [1]} {:a [2]}}" "#{'x 'y}"]]
      (let [result (parse path text table (tokenize path text table true))]
        (is (= 2 (count (first (:parsed-values result)))))
        (is (set? (first (:parsed-values result))))))
    (doseq [text ["#{[1] [1]}" "#{{:a [1]} {:a [1]}}" "#{'x 'x}" "#{^:a x ^:a x}" "#{@x @x}"]]
      (let [failure (ex-data (rejection #(parse path text table (tokenize path text table true))))]
        (is (= "C2-SET" (:id failure)))
        (is (= path (get-in failure [:source-span :source])))
        (is (= :first-literal (get-in failure [:related 0 :role])))
        (is (= path (get-in failure [:related 0 :span :source])))
        (is (keyword? (:form-id failure)))
        (is (not= (:form-id failure) (get-in failure [:related 0 :artifact])))))))

(deftest actual-form-errors-retain-the-owning-diagnostic-and-provenance
  (let [table (b/stage1-reader-table)]
    (doseq [[text expected] [["]" "STAGE1READER001"] ["([)]" "STAGE1READER001"]
                           ["(" "STAGE1READER002"] ["{:a 1 :b}" "STAGE1READER005"]
                           ["^:a 1" "C2-METADATA"] ["^1 x" "C2-METADATA"]
                           ["^:a" "C2-METADATA"] ["'" "C2-ABBREV"]
                           ["#inst" "STAGE1READER004"] ["#uuid \"invalid\"" "STAGE1READER004"]]
            extension [".gravity" ".qst"]]
      (let [path (str "malformed" extension) tokens (tokenize path text table true)
            direct (rejection #(parse path text table tokens))
            facade (rejection #(b/stage1-reader-products-from-token-stream path text table tokens))]
        (is (= expected (:id (ex-data direct))))
        (is (= (ex-data direct) (ex-data facade)))
        (is (= path (get-in (ex-data direct) [:source-span :source])))
        (is (keyword? (:token-id (ex-data direct))))))))

(deftest existing-native-join-refusals-precede-form-construction
  (let [path "join.qst" text "1 2" table (b/stage1-reader-table)
        tokens (tokenize path text table false)
        alterations [[:kind (assoc tokens :kind :wrong)] [:source-path (assoc tokens :source-path "wrong")]
                     [:source-id (assoc tokens :source-id "wrong")] [:reader-table-id (assoc tokens :reader-table-id "wrong")]
                     [:tokens (update tokens :token-count inc)]]
        unexpected (fn [& _] (throw (AssertionError. "No context or parse after join refusal")))]
    (with-redefs [b/line-start-indices unexpected b/stage1-reader-token-index unexpected b/form-kind unexpected]
      (doseq [[field altered] alterations]
        (let [failure (ex-data (rejection #(parse path text table altered)))]
          (is (= "STAGE1PIPE004" (:id failure)))
          (is (= field (first (:missing-fields failure)))))))))

(deftest lazy-index-and-owner-roots-remain-late-at-original-expression-positions
  (let [path "late.gravity" text "1 2" table (b/stage1-reader-table)
        tokens (tokenize path text table false) ports (operations) events (atom [])
        digest (var-get #'b/sha256-hex) starts (var-get #'b/line-start-indices)
        index (var-get #'b/stage1-reader-token-index) hash (var-get #'b/reader-canonical-hash)
        kind (var-get #'b/form-kind)]
    (with-redefs [b/sha256-hex (fn [s]
                               (swap! events conj (cond (= s text) :source-digest
                                                       (= s (pr-str table)) :table-digest
                                                       :else :trivia-digest))
                               (digest s))
                  b/line-start-indices
                  (fn [s]
                    (swap! events conj :line-starts)
                    (alter-var-root #'b/stage1-reader-token-index
                                    (constantly (fn [ts]
                                                  (swap! events conj :late-index)
                                                  (alter-var-root #'b/reader-canonical-hash
                                                                  (constantly (fn [v] (swap! events conj :late-hash) (hash v))))
                                                  (index ts))))
                    (starts s))
                  b/stage1-reader-token-index (fn [& _] (throw (AssertionError. "Early token index")))
                  b/reader-canonical-hash (fn [& _] (throw (AssertionError. "Early trivia hash")))
                  b/form-kind (fn [v] (swap! events conj :kind)
                                (alter-var-root #'b/form-kind (constantly (fn [x] (swap! events conj :late-kind) (kind x))))
                                (kind v))]
      (is (= [1N 2N] (:parsed-values (reader/build-compatibility-forms path text table tokens ports))))
      (is (= [:source-digest :table-digest :line-starts :late-index
              :late-hash :trivia-digest :kind :late-hash :trivia-digest :late-kind]
             @events)))))

(deftest token-index-is-delayed-on-empty-input-and-forced-once-per-product
  (let [path "index.qst" table (b/stage1-reader-table) index (var-get #'b/stage1-reader-token-index)
        empty-tokens (tokenize path "" table true) tokens (tokenize path "1 2" table true) calls (atom 0)]
    (with-redefs [b/stage1-reader-token-index (fn [ts] (swap! calls inc) (index ts))]
      (is (= [] (:parsed-values (parse path "" table empty-tokens))))
      (is (zero? @calls))
      (is (= [1N 2N] (:parsed-values (parse path "1 2" table tokens))))
      (is (= 1 @calls))
      (parse path "1 2" table tokens)
      (is (= 2 @calls)))))

(deftest returning-pipeline-failures-preserve-all-ordered-native-checks
  (let [path "checks.gravity" text "1" table (b/stage1-reader-table)
        tokens (-> (tokenize path text table false)
                   (assoc :kind :wrong :source-path "wrong" :source-id "wrong" :reader-table-id "wrong")
                   (update :token-count inc))
        calls (atom [])]
    (with-redefs [b/stage1-reader-pipeline-fail!
                  (fn [_ _ _ data]
                    (swap! calls conj [:first (:missing-fields data)])
                    (alter-var-root #'b/stage1-reader-pipeline-fail!
                                    (constantly (fn [_ _ _ later] (swap! calls conj [:later (:missing-fields later)]) nil)))
                    nil)]
      (is (= [1N] (:parsed-values (parse path text table tokens))))
      (is (= [[:first [:kind]] [:later [:source-path]] [:later [:source-id]]
              [:later [:reader-table-id]] [:later [:tokens :token-count]]]
             @calls)))))

(deftest owner-exception-identity-and-tagged-decoder-containment-stay-intact
  (let [path "owners.qst" table (b/stage1-reader-table) sentinel (ex-info "owner sentinel" {:owner :forms})
        list-tokens (tokenize path "(1)" table false)
        atom-tokens (update-in (tokenize path "1" table false) [:tokens 0] dissoc :decoded)
        tag-tokens (tokenize path "#uuid \"invalid\"" table false)]
    (with-redefs [b/stage1-reader-span (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(parse path "(1)" table list-tokens)))))
    (with-redefs [b/stage1-reader-decode-atom (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(parse path "1" table atom-tokens)))))
    (with-redefs [b/stage1-reader-fail! (fn [& _] (throw sentinel))]
      (is (identical? sentinel (rejection #(parse path "(" table (tokenize path "(" table false))))))
    (with-redefs [edn/read-string (fn [& _] (throw sentinel))]
      (let [failure (rejection #(parse path "#uuid \"invalid\"" table tag-tokens))]
        (is (not (identical? sentinel failure)))
        (is (= "STAGE1READER004" (:id (ex-data failure))))
        (is (= "owner sentinel" (:cause-message (ex-data failure))))))))

(deftest native-joins-do-not-authenticate-token-semantics-or-discover-inputs
  (let [path "supplied.qst" text "1" table (b/stage1-reader-table)
        tokens (assoc-in (tokenize path text table false) [:tokens 0 :decoded] 'forged)
        ports (assoc (operations) :nodes (atom {:forged true}) :read-form (fn [& _] (throw (AssertionError. "Not an owner port"))))
        unexpected (fn [& _] (throw (AssertionError. "No source discovery")))]
    (with-redefs [b/stage1-reader-table unexpected b/read-source-form-records unexpected
                  b/reader-project-context-for-source unexpected]
      (let [result (reader/build-compatibility-forms path text table tokens ports)]
        (is (= ['forged] (:parsed-values result)))
        (is (= :integer (get-in result [:form-tree 0 :kind])))
        (is (= #{:records :form-tree :root-form-ids :parsed-values} (set (keys result))))))))

(deftest compatibility-form-facade-retains-its-public-arity
  (is (= '([source-path source-text table token-stream])
         (:arglists (meta #'b/stage1-reader-products-from-token-stream)))))
