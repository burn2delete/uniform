(ns gravity.reader.normal-dispatch-test
  "Compiler-free execution of the exact normal host-decoding bodies.
  Parsed graph/value-table fixtures are injected; no Reader producer, envelope
  authentication or fresh Gravity reconstruction is executed or simulated."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.c2-source-identity :as source]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest]
            [gravity.source-unit :as source-unit]
            [gravity.reader.dispatch :as reader-dispatch]))

(def ^:private component-root
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/")
(def ^:private reader-root (str component-root "self-hosting-sh03-sh07/"))

(defn- source-definition [path name]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (binding [*read-eval* false]
      (loop []
        (let [form (read {:eof ::end} r)]
          (when (= ::end form) (throw (ex-info "Missing canonical definition" {:name name})))
          (if (and (seq? form) (= name (second form))) form (recur)))))))

(defn- install! [path names]
  ;; Evaluate exact, named Clojure definitions only. No bootstrap load order,
  ;; compiler, source-rule, authentication or runtime producer is required.
  (doseq [name names]
    (binding [*ns* (the-ns 'gravity.reader.normal-dispatch-test)]
      (eval (source-definition path name)))))

(defn sh03-reader-boundary-fail! [_ rule _ _]
  (throw (ex-info "Fixture reached normal Reader refusal" {:id "C2-HASH" :rule rule})))

(declare sh03-reader-host-bigint! sh03-reader-host-decimal-value!
         sh03-reader-host-character-value! sh03-reader-host-values!
         reader-standard-dispatch-products-match?
         sh03-reader-semantic-value-reference-keys sh03-reader-source-slice-keys
         standard-reader-policy sh03-reader-source-scalar-boundaries!
         sh03-reader-semantic-value-closure! sh03-reader-form-value-reference!
         semantic-late-sh03-reader-adapt-products!-identities)

(install! (str component-root "foundation/00802-reader-canonical-value.clj")
          '[standard-reader-policy])
(install! (str reader-root "00811-sh03-reader-plan-validate.clj")
          '[sh03-reader-source-slice-keys sh03-reader-semantic-value-reference-keys])
(install! (str reader-root "00813-sh03-reader-canonical-identity.clj")
          '[sh03-reader-decode-raw-bytes! sh03-reader-source-scalar-boundaries!])
(install! (str reader-root "00814-sh03-reader-source-slice-text.clj")
          '[sh03-reader-source-slice-text! sh03-reader-accepted-raw-text!
            sh03-reader-codepoints-text! sh03-reader-semantic-reference-value!])
(install! (str reader-root "00815-sh03-reader-normalization-deferred.clj")
          '[sh03-reader-host-atomic-value! sh03-reader-metadata-map!
            sh03-reader-host-values! reader-standard-dispatch-products-match?])
(install! "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/146156-sh03-reader-adapt-identities.clj"
          '[semantic-late-sh03-reader-adapt-products!-identities])

(defn- canonical-hash [value]
  (letfn [(normalize [value] (canonical/reader-canonical-value normalize value))]
    (canonical/reader-canonical-hash digest/sha256-hex normalize value)))

(defn- unit
  ([text] (unit "fixtures/registered.gravity" text))
  ([path text]
  (source/with-operations
    {:sha256-hex digest/sha256-hex :reader-canonical-hash canonical-hash
     :gravity-source-extension source-unit/gravity-source-extension
     :gravity-source-kind source-unit/gravity-source-kind}
    #(source/c2-source-unit-record
      path text
      {:retain-comments true :enabled-features #{:standard-reader}
       :extension-policy (canonical-hash standard-reader-policy)}
      {:project-root-id (str "sha256:" (apply str (repeat 64 "a")))
       :project-relative-path path}))))

(defn- span [start end]
  {:byte-start start :byte-end end
   :start {:line 1 :column (inc start) :column-unit :unicode-scalar :char start :byte start}
   :end {:line 1 :column (inc end) :column-unit :unicode-scalar :char end :byte end}})

(defn- fixture [tag payload]
  (let [text (str "#" tag " " (pr-str payload))
        bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
        source-unit (unit text)
        content-id (:bytes-hash source-unit)
        start (+ 2 (count (str tag))) end (count text)
        slice {:artifact :gravity/source-slice :schema-version 1 :encoding :utf-8
               :source-content-id content-id :byte-start start :byte-end end
               :scalar-start start :scalar-end end}
        reference {:artifact :gravity/semantic-value-reference :schema-version 1
                   :value-id "value/0" :field :descriptor}
        entry {:form-id "form/1" :token-id "token/1" :kind :string
               :descriptor {:decoded-codepoints (mapv int payload)}}]
    {:source-unit source-unit :bytes bytes :content-id content-id
     :boundaries (sh03-reader-source-scalar-boundaries! "fixture" text bytes)
     :forms [{:form-id "form/0" :kind :tagged-literal :origin :source
              :tag-codepoints (mapv int (str tag))
              :children ["form/1"] :span (span 0 end)}
             {:form-id "form/1" :kind :string :open-token "token/1" :children []
              :span (span start end) :raw slice :value reference}]
     :index {:by-value-id {"value/0" entry}}}))

(defn- execute [{:keys [source-unit bytes content-id boundaries forms index]}]
  (sh03-reader-host-values! (:path source-unit) bytes content-id
                           boundaries forms index source-unit))

(defn- failure [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (ex-data e))))

(deftest exact-normal-decoder-dispatches-real-standard-handlers
  (doseq [[tag payload expected handler]
           [['uuid "550e8400-e29b-41d4-a716-446655440000"
             (java.util.UUID/fromString "550e8400-e29b-41d4-a716-446655440000")
             'gravity.reader.standard/read-uuid]
            ['inst "2026-10-28T00:00:00.000-00:00"
             (java.util.Date. 1793145600000) 'gravity.reader.standard/read-inst]]]
    (let [input (fixture tag payload)
          calls (atom []) seeds (atom []) original reader-dispatch/dispatch
          result (with-redefs [reader-dispatch/dispatch
                              (fn [& args] (swap! calls conj (second args))
                                (swap! seeds conj (nth args 2))
                                (apply original args))]
                   (binding [*data-readers* {tag (fn [_] (throw (Exception. "ambient")))}]
                     (execute input)))
          record (first (:extension-dispatch-observations result))]
      (is (= expected (get-in result [:values "form/0"])))
      (is (= payload (get-in result [:values "form/1"])))
      (is (= [tag] @calls))
      (is (= :source (:origin (first @seeds))))
      (is (= (get-in input [:source-unit :path]) (:source-path (first @seeds))))
      (is (= handler (:handler record)))
      (is (= :invoked (:status record)))
      (is (= :unsealed-bootstrap-observation (:authority record)))
      (is (= :source (:origin record)))
      (is (= (get-in input [:source-unit :path]) (:source-path record)))
      (is (= (get-in input [:source-unit :source-id]) (:source-id record)))
      (is (= (:content-id input) (:source-content-hash record)))
      (is (= (canonical-hash standard-reader-policy) (:policy-id record)))
      (is (= "form/0" (:form-id record)))
      (is (= (get-in input [:forms 0 :span]) (:span record)))
      (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %)
                  [(:input-hash record) (:result-hash record)]))
      (is (= record (first (:extension-dispatch-observations (execute input))))))))

(deftest policy-and-hash-refusals-precede-dispatch
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom 0) original reader-dispatch/dispatch]
    (with-redefs [reader-dispatch/dispatch
                  (fn [& args] (swap! calls inc) (apply original args))]
      (doseq [path [[:source-unit :extension-policy]
                    [:source-unit :reader-options :extension-policy]
                    [:source-unit :identity-inputs :extension-policy]
                    [:source-unit :identity-inputs :reader-options :extension-policy]]]
        (is (= :policy-hash
               (:reason (failure #(execute (assoc-in input path "sha256:altered")))))))
      (doseq [policy [(assoc standard-reader-policy :registered-tags ['uuid 'resource])
                      (assoc standard-reader-policy :handler 'gravity.reader/read-input)
                      (assoc standard-reader-policy :ambient-authority :allowed)]]
        (is (= "C2-EXTENSION"
               (:id (with-redefs [standard-reader-policy policy]
                      (failure #(execute input)))))))
      (is (zero? @calls)))))

(deftest complete-tag-population-is-checked-before-any-handler
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom 0)]
    ;; Put an unknown tag before a valid tag in source order; the reverse
    ;; host-value walk must still reject before it can call the valid handler.
    (with-redefs [reader-dispatch/dispatch (fn [& _] (swap! calls inc))]
      (let [unknown (assoc (first (:forms input)) :form-id "unknown"
                           :tag-codepoints (mapv int "resource"))
            malformed (update input :forms #(into [unknown] %))]
        (is (= :unknown-tag (:reason (failure #(execute malformed)))))
        (is (zero? @calls))))))

(deftest original-source-slices-and-semantic-joins-still-refuse
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom 0)]
    (with-redefs [reader-dispatch/dispatch (fn [& _] (swap! calls inc))]
      (doseq [[path value]
               [[[:forms 1 :raw :source-content-id] "sha256:substituted"]
                [[:forms 1 :raw :byte-start] 0]
                [[:forms 1 :value :value-id] "missing"]
                [[:index :by-value-id "value/0" :form-id] "other"]]]
        (is (= "C2-HASH" (:id (failure #(execute (assoc-in input path value))))))
      (is (zero? @calls))))))

(deftest source-and-payload-changes-change-observation-hashes
  (let [a (execute (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000"))
        b (execute (fixture 'uuid "550e8400-e29b-41d4-a716-446655440001"))
        a (first (:extension-dispatch-observations a))
        b (first (:extension-dispatch-observations b))]
    (is (not= (:source-id a) (:source-id b)))
    (is (not= (:source-content-hash a) (:source-content-hash b)))
    (is (not= (:input-hash a) (:input-hash b)))
    (is (not= (:result-hash a) (:result-hash b)))
    (is (= (:policy-id a) (:policy-id b)))))

(deftest normal-adapter-cannot-dispatch-before-its-existing-semantic-closure-check
  (let [calls (atom []) sentinel (ex-info "Stop at existing validation boundary" {})]
    ;; A negative fence only: the required validation operation throws. It does
    ;; not fabricate a successful verifier/producer or authenticated input.
    (with-redefs [sh03-reader-semantic-value-closure!
                  (fn [& _] (swap! calls conj :closure-check) (throw sentinel))
                  reader-dispatch/dispatch
                  (fn [& _] (swap! calls conj :unexpected-dispatch))]
      (is (identical? sentinel
                      (try (semantic-late-sh03-reader-adapt-products!-identities {})
                           nil (catch clojure.lang.ExceptionInfo error error))))
      (is (= [:closure-check] @calls)))))

(deftest source-snapshot-identity-and-origin-refusals-precede-handler-invocation
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom 0) original reader-dispatch/dispatch
        changed "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"]
    (with-redefs [reader-dispatch/dispatch
                  (fn [& args] (swap! calls inc) (apply original args))]
      (doseq [path [[:source-unit :source-id]
                    [:source-unit :bytes-hash]
                    [:source-unit :identity-inputs :bytes-hash]
                    [:source-unit :project-root]
                    [:source-unit :project-root-record :project-root-id]
                    [:source-unit :identity-inputs :project-root-id]]]
        (is (= :source-identity (:reason (failure #(execute (assoc-in input path changed)))))))
      (is (= :source-identity
             (:reason (failure #(execute (assoc input :bytes (.getBytes "stale" "UTF-8")))))))
      (is (= :source-identity
             (:reason (failure #(execute (assoc input :bytes nil))))))
      (is (= :sh03-reader-dispatch-source-binding
             (:rule (failure #(execute (assoc input :content-id changed))))))
      (is (= :sh03-reader-dispatch-source-binding
             (:rule (failure #(sh03-reader-host-values!
                               "other.gravity" (:bytes input) (:content-id input)
                               (:boundaries input) (:forms input) (:index input)
                               (:source-unit input))))))
      (doseq [origin [nil :generated :caller-asserted]]
        (is (= :origin (:reason (failure #(execute (assoc-in input [:forms 0 :origin] origin)))))))
      (doseq [[path value] [[[:span :byte-start] -1]
                            [[:span :byte-end] 1000]
                            [[:span :start :byte] 1]
                            [[:span :start :column] 0]]]
        (is (some? (failure #(execute (assoc-in input (into [:forms 0] path) value))))))
      (is (zero? @calls)))))

(deftest foreign-identity-inputs-reject-without-host-callbacks
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom 0)
        foreign (proxy [java.util.HashMap] []
                  (entrySet [] (swap! calls inc) (throw (Exception. "foreign identity"))))
        foreign-meta (proxy [Object clojure.lang.IMeta] []
                       (meta [] (swap! calls inc) {}))]
    (doseq [value [foreign foreign-meta (lazy-seq (swap! calls inc) [1])
                   (with-meta {} {:foreign true})]]
      (is (= :source-identity
             (:reason (failure #(execute
                                 (assoc-in input [:source-unit :identity-inputs :extra] value)))))))
    (is (zero? @calls))))

(deftest direct-path-and-normal-span-refuse-before-callbacks
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        calls (atom [])
        foreign-path (proxy [Object] []
                       (toString [] (swap! calls conj :string) "foreign.gravity"))
        foreign-span (proxy [java.util.HashMap] []
                       (get [_] (swap! calls conj :get) 0))
        handler-calls (atom 0) original reader-dispatch/dispatch]
    (with-redefs [reader-dispatch/dispatch
                  (fn [& args] (swap! handler-calls inc) (apply original args))]
      (is (= :text
             (:reason (failure #(reader-dispatch/dispatch-standard-tag
                                 (assoc (:source-unit input) :path foreign-path)
                                 standard-reader-policy 'uuid (first (:forms input))
                                 "550e8400-e29b-41d4-a716-446655440000")))))
      (is (= :native-map
             (:reason (failure #(execute (assoc-in input [:forms 0 :span] foreign-span))))))
      (is (empty? @calls))
      (is (zero? @handler-calls)))))

(defn- reconstruction-fixture [input]
  (let [result (execute input)
        ids {"form/0" :form-0 "form/1" :form-1}]
    {:ids ids
     :forms (mapv (fn [form] {:form-id (get ids (:form-id form))
                             :value (get-in result [:values (:form-id form)])})
                  (:forms input))
     :observations (:extension-dispatch-observations result)}))

(defn- reconstructed? [input candidate]
  (reader-standard-dispatch-products-match?
   (get-in input [:source-unit :path]) (:bytes input) (:content-id input)
   (:boundaries input) (:forms input) (:index input) (:source-unit input)
   (:ids candidate) (:forms candidate) (:observations candidate)))

(deftest returned-products-require-fresh-pure-handler-values-and-observations
  (doseq [[tag payload] [['uuid "550e8400-e29b-41d4-a716-446655440000"]
                        ['inst "2026-10-28T00:00:00.000-00:00"]]]
    (let [input (fixture tag payload) candidate (reconstruction-fixture input)
          calls (atom []) original reader-dispatch/dispatch]
      (with-redefs [reader-dispatch/dispatch
                    (fn [& args] (swap! calls conj (second args)) (apply original args))]
        (is (true? (reconstructed? input candidate)))
        (is (= [tag] @calls)))
      (doseq [path [[:handler] [:policy-id] [:source-id] [:source-content-hash]
                    [:source-path] [:origin] [:span] [:input-hash] [:result-hash]
                    [:policy-selection :entry] [:grant-id]]]
        (is (false? (reconstructed?
                     input (assoc-in candidate (into [:observations 0] path) :forged)))))
      (doseq [observations [nil [] (conj (:observations candidate) (first (:observations candidate)))]]
        (is (false? (reconstructed? input (assoc candidate :observations observations)))))
      (is (false? (reconstructed? input (assoc-in candidate [:forms 0 :value] :forged))))
      (is (false? (reconstructed? input (update candidate :forms subvec 1))))
      (is (false? (reconstructed? input (assoc-in candidate [:ids "form/0"] :missing)))))))

(deftest both-source-kinds-retain-declared-source-identity
  (let [input (fixture 'uuid "550e8400-e29b-41d4-a716-446655440000")
        text (String. ^bytes (:bytes input) java.nio.charset.StandardCharsets/UTF_8)
        other (assoc input :source-unit (unit "fixtures/registered.qst" text))
        a (execute input) b (execute other)
        observation #(first (:extension-dispatch-observations %))]
    (is (= ".qst" (get-in other [:source-unit :extension])))
    (is (= :qst-theory-source (get-in other [:source-unit :source-kind])))
    (is (= (:values a) (:values b)))
    (is (not= (:source-id (observation a)) (:source-id (observation b))))
    (is (= (:source-content-hash (observation a))
           (:source-content-hash (observation b))))))
