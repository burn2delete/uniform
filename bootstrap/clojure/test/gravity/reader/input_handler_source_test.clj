(ns gravity.reader.input-handler-source-test
  "Isolated source model, not Gravity execution or provider authority."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]))

(defn- source-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (java.io.PushbackReader.
                       (java.io.StringReader.
                        (slurp path)))]
      (loop [out []]
        (let [value (read {:eof ::end} reader)]
          (if (= ::end value) out (recur (conj out value))))))))

(defn- forms []
  (source-forms "bootstrap/gravity/src/gravity/reader.gravity"))

(defn- definition [forms name]
  (nth (first (filter #(and (seq? %) (= 'def (first %))
                           (= name (second %))) forms)) 2))

(def ^:private pin-source
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00645-p15-s23-stage2-runtime-artifact-source-relative-path.clj")

(defn- hash-bytes [bytes]
  (apply str (map #(format "%02x" (bit-and 255 %))
                  (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes))))

(defn- compiler-revision []
  (let [text (slurp "bootstrap/gravity/p15_s23/compiler.gravity")
        current "hash-map list pr-str rest second str vector build-read-declared-input]"
        previous "hash-map list pr-str rest second str vector]"
        old-text (str/replace text current previous)
        old-bytes (.getBytes old-text java.nio.charset.StandardCharsets/UTF_8)]
    ;; Authenticate the authored preimage before deriving the new input binding.
    (is (= 2 (count (re-seq (re-pattern (java.util.regex.Pattern/quote current)) text))))
    (is (= 128447 (alength old-bytes)))
    (is (= "be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854"
           (hash-bytes old-bytes)))
    (is (= text (str/replace old-text previous current)))
    {:bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)
     :previous-bytes old-bytes :text text}))

(defn- with-source-authentication-model [run]
  (let [n (create-ns (gensym "compiler-input-authentication-model-"))
        forms (source-forms pin-source)]
    (try
      (binding [*ns* n]
        (clojure.core/refer 'clojure.core)
        (intern n 'sha256-bytes-hex hash-bytes)
        (doseq [name '[p15-s23-stage2-compiler-expected-source-content-hash
                      p15-s23-stage2-compiler-expected-source-byte-count
                      p15-s23-stage2-compiler-pinned-source!]]
          (eval (first (filter #(and (seq? %) (= name (second %))) forms)))))
      (run (ns-resolve n 'p15-s23-stage2-compiler-pinned-source!))
      (finally (remove-ns (ns-name n))))))

(deftest fixed-compiler-binding-authenticates-the-authored-declaration-revision
  ;; Selected host byte-authentication definition only; no Gravity parsing,
  ;; Bootstrap load, compiler plan, issuer, provider or historical admission.
  (let [{:keys [bytes previous-bytes text]} (compiler-revision)
        definitions (source-forms pin-source)
        expected-hash (definition definitions 'p15-s23-stage2-compiler-expected-source-content-hash)
        expected-count (definition definitions 'p15-s23-stage2-compiler-expected-source-byte-count)
        path (java.nio.file.Files/createTempFile
              "compiler-input-model-" ".data"
              (make-array java.nio.file.attribute.FileAttribute 0))
        changed (aclone bytes)
        _ (aset-byte changed 0 (unchecked-byte (bit-xor 1 (bit-and 255 (aget bytes 0)))))
        wrong-source (byte-array (count bytes))]
    (is (= expected-count (alength bytes)))
    (is (= expected-hash (str "sha256:" (hash-bytes bytes))))
    (try
      (with-source-authentication-model
        (fn [authenticate]
          (let [read! #(authenticate (str path) "<compiler-input-model>" :jvm "P15S23Q001"
                                    (fn [id _ _ facts] (throw (ex-info "source refused" (assoc facts :id id)))))]
            (java.nio.file.Files/write path bytes (make-array java.nio.file.OpenOption 0))
            (is (= {:source-text text :source-byte-count expected-count
                    :source-content-hash expected-hash} (read!)))
            ;; Advancing length alone cannot bless the changed source.
            (with-redefs-fn
              {(ns-resolve (:ns (meta authenticate))
                           'p15-s23-stage2-compiler-expected-source-content-hash)
               "sha256:be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854"}
              #(is (= :stage2-compiler-source-content-hash
                      (:missing-fact (try (read!) nil
                                         (catch clojure.lang.ExceptionInfo ex (ex-data ex)))))))
            (doseq [[candidate reason]
                    [[previous-bytes :stage2-compiler-source-byte-count]
                     [(java.util.Arrays/copyOf bytes (dec (alength bytes))) :stage2-compiler-source-byte-count]
                     [(java.util.Arrays/copyOf bytes (inc (alength bytes))) :stage2-compiler-source-byte-count]
                     [changed :stage2-compiler-source-content-hash]
                     [wrong-source :stage2-compiler-source-content-hash]]]
              (java.nio.file.Files/write path candidate (make-array java.nio.file.OpenOption 0))
              (let [failure (try (read!) nil (catch clojure.lang.ExceptionInfo ex (ex-data ex)))]
                (is (= "P15S23Q001" (:id failure)))
                (is (= reason (:missing-fact failure)))))
            (java.nio.file.Files/delete path)
            (is (= :stage2-compiler-source-byte-count
                   (:missing-fact (try (read!) nil (catch clojure.lang.ExceptionInfo ex (ex-data ex)))))))))
      (finally (java.nio.file.Files/deleteIfExists path)))))

(deftest stage2-builtin-declarations-match-the-complete-seed-population
  ;; Read source DATA only: no Bootstrap, Gravity compiler or runtime load.
  (let [seed (second (definition
                      (source-forms
                       "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00516-defn-form.clj")
                      'stage0-builtin-functions))
        compiler (source-forms "bootstrap/gravity/p15_s23/compiler.gravity")
        emitter (definition compiler 'p15-s23-stage2-plan-emitter)
        runtime (definition compiler 'p15-s23-stage2-runtime-executor)
        complete? (fn [functions]
                    (and (= seed (set functions))
                         (= (count functions) (count (set functions)))))]
    (is (contains? seed 'build-read-declared-input))
    (doseq [functions [(get-in emitter [:call-rules :builtin-functions])
                       (get-in runtime [:builtin-rules :functions])]]
      (is (complete? functions))
      (is (= 1 (count (filter #{'build-read-declared-input} functions))))
      (is (not (complete? (remove #{'build-read-declared-input} functions))))
      (is (not (complete? (remove #{'get} functions))))
      (is (not (complete? (conj functions 'build-read-declared-input))))
      (is (not (complete? (conj functions 'unregistered-file-read)))))))

(deftest handler-has-the-fixed-build-contract-and-one-original-builtin-occurrence
  (let [[module handler] (forms)
        options (into {} (map (fn [[k value]] [k value]) (drop 2 module)))
        calls (filter #(and (seq? %) (= 'build-read-declared-input (first %)))
                      (tree-seq coll? seq handler))]
    (is (= 'gravity.reader (second module)))
    (is (= {:profile :meta :target :jvm :exports '[read-input]
            :effects #{:build/read-file} :capabilities #{:fs/read} :safety :safe}
           options))
    (is (= '[defn read-input [seed]] (take 3 handler)))
    (is (= '((build-read-declared-input (get seed :datum))) calls))))

(deftest selected-Gravity-definition-preserves-seed-and-uses-actual-modeled-return
  (let [model (create-ns (gensym "input-handler-source-model-"))
        calls (atom []) result (atom "")]
    (try
      (binding [*ns* model]
        (clojure.core/refer 'clojure.core)
        (intern model 'build-read-declared-input
                (fn [input] (swap! calls conj input) @result))
        (eval (second (forms))))
      (let [handler (var-get (ns-resolve model 'read-input))
            seed {:source-id "source-A" :form-id "form-A" :payload-form-id "payload-A"
                  :span {:byte-start 0 :byte-end 18} :datum "schema"}]
        (doseq [returned ["" "schema text" (str (char 0x03bb))]]
          (reset! calls []) (reset! result returned)
          (is (= (assoc seed :datum returned) (handler seed)))
          (is (= ["schema"] @calls)))
        (reset! result "different return")
        (is (not= seed (handler seed))))
      (finally (remove-ns (ns-name model))))))
