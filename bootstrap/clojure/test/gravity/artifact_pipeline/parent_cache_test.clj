(ns gravity.artifact-pipeline.parent-cache-test
  "Compiler-free controls for the actual facade parent/cache admission functions.
  Owner verification and artifact construction are explicit test stand-ins;
  these controls confer no authenticated compiler-product acceptance."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(def ^:private component-root
  "bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/")

(defn- selected-forms [file names]
  (with-open [reader (java.io.PushbackReader. (io/reader (str component-root file)))]
    (loop [found []]
      (let [form (read {:eof ::end} reader)]
        (if (= ::end form)
          (do
            (assert (= names (set (map second found))))
            found)
          (recur (cond-> found
                   (and (seq? form) (names (second form))) (conj form))))))))

(def ^:private stages
  [:source :reader :syntax :macroexpansion :resolution :checked-core])

(def ^:private parent-fields
  {:syntax :c2-reader-artifact
   :macroexpansion :sh04-syntax-artifact
   :resolution :sh05-macro-artifact
   :checked-core :sh06-resolution-artifact})

(defn- subject [stage parent-kind corrupt-rebuild?]
  (let [subject-ns (create-ns (gensym "gravity.artifact-pipeline.parent-cache-subject-"))
        trace (atom [])
        previous (mapv (fn [s] {:payload-artifact-id (str "owner/" (name s))
                               :artifact-id (str "facade/" (name s))
                               :payload {:source-identity-id (str "owner/" (name s))}})
                       (take (.indexOf stages stage) stages))
        expected (:payload-artifact-id (peek previous))
        field (parent-fields stage)
        parent (case parent-kind :current expected :foreign "owner/foreign"
                     :facade (:artifact-id (peek previous)) :missing nil)
        cached-payload (cond-> {:artifact-id "owner/cached"}
                         parent (assoc field {:artifact-id parent}))
        cache-key ["cache-key" "fixture.gravity"]
        cache (atom {cache-key {:stage stage :payload cached-payload :receipt {}}})
        put (fn [name value] (intern subject-ns name value))]
    (binding [*ns* subject-ns]
      (clojure.core/refer 'clojure.core)
      (put 'pipeline-id :gravity/authenticated-compiler-artifact-pipeline-v1)
      (put 'stage-index (zipmap stages (range)))
      (put 'stage-cache cache)
      (put 'cache-key-preimage (fn [& _] {:fixture :cache-key-input}))
      (put 'canonical-id! (fn [& _] "cache-key"))
      ;; Deliberately pass the other cache gates: an owning verifier can accept
      ;; a different valid parent chain. The facade must compare this run's edge.
      (put 'envelope-valid? (fn [& _] (swap! trace conj :envelope-checked) true))
      (put 'authenticate-reader-source! (fn [& _] (swap! trace conj :reader-source-checked)))
      (put 'revalidate-payload? (fn [& _] (swap! trace conj :owner-verified) true))
      (put 'build-payload (fn [& _]
                            (swap! trace conj :rebuilt)
                            {:artifact-id "owner/rebuilt"
                             field {:artifact-id (if corrupt-rebuild? "owner/foreign" expected)}}))
      (put 'envelope (fn [s lane source provenance parents payload hit? key]
                      (swap! trace conj :enveloped)
                      {:stage s :mode lane :source source :provenance provenance
                       :input-artifact-ids parents :payload payload :cache-hit? hit?
                       :cache-key-id key}))
      (put 'cache-put! (fn [key value] (swap! trace conj :cached) (swap! cache assoc key value) value))
      (put 'receipt (fn [& _] {:fixture :hit-receipt}))
      (doseq [form (concat
                   (selected-forms "02-clear-cache.clj" #{'fail!})
                   (selected-forms "03-revalidate-payload.clj" #{'parent-ids})
                   (selected-forms "04-envelope-valid.clj"
                                   #{'embedded-parent-id 'authenticate-embedded-parent! 'build-stage!}))]
        (eval form)))
    {:namespace subject-ns :trace trace :cache cache :cache-key cache-key
     :expected-parent expected :cached-payload cached-payload
     :run (fn [lane]
            ((ns-resolve subject-ns 'build-stage!)
             stage lane {:fixture :source} {:actual-path "fixture.gravity"}
             "fixture.gravity" "fixture text" {} previous {}))}))

(defn- close! [s] (remove-ns (ns-name (:namespace s))))

(deftest current-immediate-parent-remains-a-cache-hit
  (doseq [stage (keys parent-fields)]
    (testing (name stage)
      (let [s (subject stage :current false)]
        (try
          (let [result ((:run s) :fast)]
            (is (true? (:cache-hit? result)))
            (is (= (:cached-payload s) (:payload result)))
            (is (= [:envelope-checked :owner-verified] @(:trace s))))
          (finally (close! s)))))))

(deftest source-and-reader-cache-routes-retain-their-existing-checks
  (doseq [stage [:source :reader]]
    (let [s (subject stage :current false)]
      (try
        (let [result ((:run s) :fast)]
          (is (true? (:cache-hit? result)))
          (is (= (:cached-payload s) (:payload result)))
          (is (= (if (= :reader stage)
                   [:envelope-checked :reader-source-checked :owner-verified]
                   [:envelope-checked :owner-verified])
                 @(:trace s))))
        (finally (close! s))))))

(deftest matching-parent-does-not-bypass-owner-verification
  (doseq [stage (keys parent-fields)]
    (let [s (subject stage :current false)]
      (try
        (intern (:namespace s) 'revalidate-payload?
                (fn [& _] (swap! (:trace s) conj :owner-rejected) false))
        (let [result ((:run s) :fast)]
          (is (false? (:cache-hit? result)))
          (is (= "owner/rebuilt" (get-in result [:payload :artifact-id])))
          (is (= [:envelope-checked :owner-rejected :rebuilt :enveloped :cached]
                 @(:trace s))))
        (finally (close! s))))))

(deftest wrong-missing-or-facade-parent-rebuilds-before-owner-verification
  (doseq [stage (keys parent-fields) parent-kind [:foreign :missing :facade]]
    (testing (str (name stage) "/" (name parent-kind))
      (let [s (subject stage parent-kind false)]
        (try
          (let [result ((:run s) :fast)]
            (is (false? (:cache-hit? result)))
            (is (= "owner/rebuilt" (get-in result [:payload :artifact-id])))
            (is (= (:expected-parent s) (get-in result [:payload (parent-fields stage) :artifact-id])))
            (is (= [:envelope-checked :rebuilt :enveloped :cached] @(:trace s)))
            (is (= result (get @(:cache s) (:cache-key s)))))
          (finally (close! s)))))))

(deftest a-bad-rebuild-retains-the-owning-parent-diagnostic
  (doseq [stage (keys parent-fields) lane [:fast :fresh]]
    (testing (str (name stage) "/" (name lane))
      (let [s (subject stage :foreign true)]
        (try
          (let [error (try ((:run s) lane) nil (catch clojure.lang.ExceptionInfo e e))]
            (is (= "ARTIFACT-PARENT" (:id (ex-data error))))
            (is (= stage (:stage (ex-data error))))
            (is (= (:expected-parent s) (:expected-parent-id (ex-data error))))
            (is (= "owner/foreign" (:observed-parent-id (ex-data error))))
            (is (not-any? #{:owner-verified :enveloped :cached} @(:trace s)))
            (when (= :fast lane) (is (empty? @(:cache s)))))
          (finally (close! s)))))))
