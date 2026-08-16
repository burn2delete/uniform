(ns gravity.c2-source-identity-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c2-source-identity :as identity]))

(def root-id (str "sha256:" (apply str (repeat 64 "a"))))
(def policy-id (str "sha256:" (apply str (repeat 64 "b"))))
(def options
  {:retain-comments true
   :enabled-features #{:standard-reader :tooling}
   :extension-policy policy-id})
(def context
  {:project-root-id root-id
   :project-root-path "/checkout-a/project"
   :project-relative-path "src/./gravity/../app.gravity"})

(deftest path-normalization-and-classification-matrix-is-platform-neutral
  (doseq [[path normalized absolute? valid-relative?]
          [["src/./app.gravity" "src/app.gravity" false true]
           ["src\\nested\\..\\app.qst" "src/app.qst" false true]
           ["a//b/../../c.gravity" "c.gravity" false true]
           ["../src/app.gravity" "../src/app.gravity" false false]
           ["a/../../app.gravity" "../app.gravity" false false]
           ["/repo/src/app.gravity" "repo/src/app.gravity" true false]
           ["C:\\repo\\app.gravity" "C:/repo/app.gravity" true false]
           ["z:relative-looking.qst" "z:relative-looking.qst" true false]
           ["./" "" false false]
           [nil "" false false]]]
    (testing (pr-str path)
      (is (= normalized (identity/reader-normalize-relative-path path)))
      (is (= absolute?
             (identity/reader-platform-neutral-absolute-path? path)))
      (is (= valid-relative?
             (identity/reader-valid-project-relative-path? path))))))

(deftest explicit-context-and-options-preserve-exact-validation-facts
  (is (= (assoc context :project-relative-path "src/app.gravity")
         (identity/reader-explicit-project-context context)))
  (is (= {:path "/checkout-a/project" :project-root-id root-id}
         (identity/reader-project-root-record context)))
  (is (true? (identity/reader-valid-options? options)))
  (is (= options (identity/reader-validate-options! options)))
  (doseq [[value missing normalized]
           [[{} [:project-root-id :project-relative-path] nil]
           [{:project-root-id "" :project-relative-path ""}
            [] ""]
           [{:project-root-id "truthy-but-invalid"
             :project-relative-path "/absolute.gravity"}
            [] "absolute.gravity"]]]
    (let [error (try
                  (identity/reader-explicit-project-context value)
                  nil
                  (catch clojure.lang.ExceptionInfo ex ex))]
      (is (= "reader project context requires a project-root id and relative path"
             (ex-message error)))
      (is (= "C2-HASH" (:id (ex-data error))))
      (is (= value (:project-context (ex-data error))))
      (is (= missing (:missing-fields (ex-data error))))
      (is (= normalized
             (:normalized-project-relative-path (ex-data error))))))
  (doseq [value [nil
                 (assoc options :retain-comments :yes)
                 (assoc options :enabled-features [:standard-reader])
                 (assoc options :extension-policy (str "sha256:" (apply str (repeat 64 "A"))))]]
    (is (false? (identity/reader-valid-options? value)))
    (let [error (try
                  (identity/reader-validate-options! value)
                  nil
                  (catch clojure.lang.ExceptionInfo ex ex))]
      (is (= "C2-HASH" (:id (ex-data error))))
      (is (= value (:reader-options (ex-data error))))
      (is (= {:retain-comments :boolean
              :enabled-features :set
              :extension-policy :sha256-lowercase-hex}
             (:required-fields (ex-data error)))))))

(deftest identity-preimage-is-checkout-neutral-with-injected-byte-hash
  (let [context-b (assoc context :project-root-path "/checkout-b/project")
        expected {:project-root-id root-id
                  :project-relative-path "src/app.gravity"
                  :encoding :utf-8
                  :bytes-hash "sha256:bytes"
                  :reader-options options
                  :enabled-features #{:standard-reader :tooling}
                  :extension-policy policy-id}
        preimages
        (identity/with-operations
         {:sha256-hex (fn [source]
                        (is (= "(def answer 42)" source))
                        "bytes")}
         #(mapv (fn [project-context]
                  (identity/reader-source-identity-inputs
                   "(def answer 42)" options project-context))
                [context context-b]))]
    (is (= [expected expected] preimages))
    (is (not-any? #(contains? expected %)
                  [:path :project-root-path :source-path :extension
                   :source-kind]))))

(deftest four-argument-source-unit-record-is-exact
  (let [record
        (identity/with-operations
         {:sha256-hex (constantly "bytes")
          :reader-canonical-hash (fn [preimage]
                                   (is (= "sha256:bytes"
                                          (:bytes-hash preimage)))
                                   "sha256:source")
          :gravity-source-extension (constantly ".gravity")
          :gravity-source-kind (constantly :gravity)}
         #(identity/c2-source-unit-record
           "/checkout-a/project/src/app.gravity"
           "(def answer 42)" options context))]
    (is (= {:artifact :gravity/source-unit
            :source-id "sha256:source"
            :path "/checkout-a/project/src/app.gravity"
            :extension ".gravity"
            :source-kind :gravity
            :project-relative-path "src/app.gravity"
            :project-root root-id
            :project-root-record {:path "/checkout-a/project"
                                  :project-root-id root-id}
            :identity-inputs
            {:project-root-id root-id
             :project-relative-path "src/app.gravity"
             :encoding :utf-8
             :bytes-hash "sha256:bytes"
             :reader-options options
             :enabled-features #{:standard-reader :tooling}
             :extension-policy policy-id}
            :encoding :utf-8
            :bytes-hash "sha256:bytes"
            :reader-options options
            :enabled-features #{:standard-reader :tooling}
            :extension-policy policy-id}
           record))))

(deftest token-and-form-projections-overwrite-hosted-provenance-exactly
  (let [source-unit {:source-id "sha256:source" :path "actual/app.gravity"}
        token (identity/c2-token-record
               {:index 7 :kind :symbol :raw "app/value"
                :source-id :stale :source-path :stale
                :token-id :stale :trivia-before [:stale]
                :reader-origin :generated
                :span {:file :stale :byte-start 4 :byte-end 13}}
               source-unit)
        default-form (identity/c2-form-record
                      {:form-id :form-0 :kind :symbol
                       :span {:file :stale :byte-start 4 :byte-end 13}
                       :origin :not-a-map}
                      source-unit)
        overriding-form (identity/c2-form-record
                         {:form-id :form-1 :kind :abbreviation
                          :span {:byte-start 0 :byte-end 2}
                          :origin {:kind :reader-quote
                                   :source-id "sha256:origin"
                                   :source-path "generated/origin.gravity"
                                   :from :form-0}}
                         source-unit)]
    (is (= {:kind :symbol :raw "app/value"
            :token-id :tok-7
            :source-id "sha256:source"
            :source-path "actual/app.gravity"
            :span {:file "sha256:source" :byte-start 4 :byte-end 13}
            :trivia-before [] :reader-origin :source}
           token))
    (is (= {:kind :source :source-id "sha256:source"
            :source-path "actual/app.gravity"}
           (:origin default-form)))
    (is (= {:kind :reader-quote :source-id "sha256:origin"
            :source-path "generated/origin.gravity" :from :form-0}
           (:origin overriding-form)))
    (is (= "sha256:source" (get-in overriding-form [:span :file])))
    (is (= "actual/app.gravity" (:source-path overriding-form)))))

(deftest literal-order-facts-escapes-and-trivia-are-projected
  (let [form-tree
        [{:form-id :container :kind :list}
         {:form-id :i :kind :integer :raw "-0x2A" :value -42 :span :i}
         {:form-id :r :kind :ratio :raw "+03/00" :value :deferred :span :r}
         {:form-id :d :kind :decimal :raw "1.0E-09" :value 1.0e-9 :span :d}
         {:form-id :s :kind :string :raw "\"a\\n\\u0041\""
          :value "a\nA" :span :s}
         {:form-id :c :kind :character :raw "\\newline" :value \newline :span :c}
         {:form-id :sym :kind :symbol :raw "demo/value"
          :value 'demo/value :span :sym}
         {:form-id :kw :kind :keyword :raw ":demo/value"
          :value :demo/value :span :kw}
         {:form-id :tag :kind :tagged-literal :raw "#demo x"
          :tag 'demo :value :payload :span :tag}
         {:form-id :b :kind :boolean :raw "true" :value true :span :b}]
        literals (identity/c2-literal-records form-tree)
        trivia (identity/c2-trivia-records
                [{:token-id :tok-0 :kind :spaces :raw " " :trivia? true
                  :span :space :source-id "sha256:source"
                  :source-path "app.gravity" :ignored :value}
                 {:token-id :tok-1 :kind :symbol :raw "x" :trivia? false}
                 {:token-id :tok-2 :kind :comment :raw "; note" :trivia? :yes
                  :span :comment :source-id "sha256:source"
                  :source-path "app.gravity"}])]
    (is (= [:integer :ratio :decimal :string :character
            :symbol :keyword :tagged-literal :boolean]
           (mapv :kind literals)))
    (is (= (mapv #(keyword (str "lit-" %)) (range 9))
           (mapv :literal-id literals)))
    (is (= {:radix 16 :sign :negative :exact? true}
           (get-in literals [0 :facts])))
    (is (= {:numerator-spelling "+03" :denominator-spelling "00"
            :exact? true}
           (get-in literals [1 :facts])))
    (is (= {:exponent-spelling "E-09" :exact? false}
           (get-in literals [2 :facts])))
    (is (= {:escapes [{:raw "\\n" :character-offset 2}
                      {:raw "\\u0041" :character-offset 4}]}
           (get-in literals [3 :facts])))
    (is (= {:escape "\\newline"} (get-in literals [4 :facts])))
    (is (= "demo" (get-in literals [5 :facts :namespace])))
    (is (= "demo" (get-in literals [6 :facts :namespace])))
    (is (= {:tag 'demo} (get-in literals [7 :facts])))
    (is (= [{:trivia-id :tok-0 :kind :spaces :raw " " :span :space
             :source-id "sha256:source" :source-path "app.gravity"}
            {:trivia-id :tok-2 :kind :comment :raw "; note" :span :comment
             :source-id "sha256:source" :source-path "app.gravity"}]
           trivia))))

(deftest operation-interposition-is-strict-and-can-enter-captured-original
  (doseq [[operations message data]
          [[nil "C2 source-identity operations must be a map" {:operations nil}]
           [{:unknown identity}
            "C2 source-identity operations contain unknown keys"
            {:unknown-keys [:unknown]}]
           [{:sha256-hex :invokable-but-not-a-function}
            "C2 source-identity operation must be a function"
            {:operation :sha256-hex :value :invokable-but-not-a-function}]]]
    (let [error (try
                  (identity/with-operations operations (fn [] :unused))
                  nil
                  (catch clojure.lang.ExceptionInfo ex ex))]
      (is (= message (ex-message error)))
      (is (= data (ex-data error)))))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"thunk must be a function"
                        (identity/with-operations {} :not-a-function)))
  (doseq [[key operation args message]
          [[:unknown identity [] "entrypoint key is unknown"]
           [:reader-normalize-relative-path :not-a-function []
            "entrypoint must be a function"]
           [:reader-normalize-relative-path identity :not-sequential
            "entrypoint args must be sequential"]]]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo (re-pattern message)
         (identity/call-entrypoint-body key operation args))))
  (let [captured-original identity/reader-normalize-relative-path
        calls (atom [])
        result
        (identity/with-operations
         {:reader-normalize-relative-path
          (fn [path]
            (swap! calls conj path)
            (identity/call-entrypoint-body
             :reader-normalize-relative-path captured-original [path]))}
         #(identity/reader-normalize-relative-path "a/./b/../c.gravity"))]
    (is (= "a/c.gravity" result))
    (is (= ["a/./b/../c.gravity"] @calls))))

(deftest private-contract-public-surface-and-nonauthority-are-exact
  (let [contract-var (get (ns-interns 'gravity.c2-source-identity)
                          'namespace-contract)
        contract (var-get contract-var)
        expected-publics
        '#{with-operations call-entrypoint-body
           reader-normalize-relative-path
           reader-platform-neutral-absolute-path?
           reader-valid-project-relative-path?
           reader-explicit-project-context reader-valid-options?
           reader-validate-options! reader-project-root-record
           reader-source-identity-inputs c2-source-unit-record
           c2-token-record c2-form-record c2-literal-records
           c2-trivia-records}]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.c2-source-identity (:namespace contract)))
    (is (= expected-publics
           (set (keys (:public-api contract)))
           (set (keys (ns-publics 'gravity.c2-source-identity)))))
    (is (= ['clojure.core 'clojure.string]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= {'str 'clojure.string}
           (into {} (map (fn [[alias namespace]]
                           [alias (ns-name namespace)]))
                 (ns-aliases 'gravity.c2-source-identity))))
    (doseq [claim [:filesystem-project-root-discovery
                   :project-root-authority :source-reading
                   :source-authentication :reader-tokenization
                   :reader-form-construction :canonical-c2-reader-authority
                   :canonical-c2-reader-product-authority
                   :sh03-reader-product-authentication :diagnostic-policy
                   :cache-reuse-authority :proof-authority
                   :attestation-authority :self-hosted-authority
                   :release-authority]]
      (is (some #{claim} (get-in contract [:ownership :does-not-own]))))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (true? (:bootstrap-hosted? contract)))
    (doseq [claim [:canonical-c2-authority? :project-root-authority?
                   :source-reading? :source-authentication? :proof-authority?
                   :self-hosted? :release-authority?]]
      (is (false? (get contract claim))))))
