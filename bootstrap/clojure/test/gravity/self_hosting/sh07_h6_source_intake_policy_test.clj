(ns gravity.self-hosting.sh07-h6-source-intake-policy-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- private-var [sym]
  (or (ns-resolve 'gravity.bootstrap sym)
      (throw (ex-info "Missing retained intake function" {:symbol sym}))))

(defn- invoke [sym & args] (apply (private-var sym) args))

(defn- sha [raw]
  (str "sha256:"
       (apply str (map #(format "%02x" (bit-and (int %) 255))
                       (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                ^bytes raw)))))

(defn- entry
  ([text] (entry "bootstrap/gravity/p15_s23/emitter.gravity" text))
  ([path text]
   (let [raw (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)]
     [path {:path path :byte-count (alength raw) :raw-sha256 (sha raw)}
      {:path path :blob-bytes (mapv #(bit-and (int %) 255) raw)} text])))

(defn- failure [operation]
  (try (operation) nil
       (catch clojure.lang.ExceptionInfo error (ex-data error))))

(defn- source! [args]
  (apply invoke 'p15-s23-h6-origin-source! args))

(defn- intake! [args]
  (apply invoke 'p15-s23-h6-retained-source-form-records! args))

(defn- lexical! [text] (invoke 'p15-s23-h6-seed-text-preflight! text))
(defn- forms! [forms] (invoke 'p15-s23-h6-source-forms-preflight! forms))

(deftest retained-entry-strict-utf8-and-descriptor-joins
  (let [args (entry "a\u00e9\ud83d\ude00")
        result (source! args)]
    (is (= 7 (:source-byte-count result)))
    (is (= (nth args 3) (:source-text result)))
    (is (= #{:source-path :source-text :source-byte-count :source-content-hash}
           (set (keys result))))
    (doseq [[label changed]
            [[:descriptor-count (assoc-in args [1 :byte-count] 8)]
             [:descriptor-path (assoc-in args [1 :path] "hostile/path")]
             [:proof-path (assoc-in args [2 :path] "hostile/path")]
             [:descriptor-hash (assoc-in args [1 :raw-sha256] (str "sha256:" (apply str (repeat 64 "0"))))]
             [:claimed-text (assoc args 3 "other")]
             [:malformed-utf8 (-> (entry "aa") (assoc-in [2 :blob-bytes] [195 40]))]
             [:unpaired-path (let [path (str "p" (char 0xd800))]
                               (-> args (assoc 0 path) (assoc-in [1 :path] path)
                                   (assoc-in [2 :path] path)))]]]
      (testing (name label)
        (let [error (failure #(source! changed))]
          (is (= "SH07-B51-00945-GOVERNED-READ" (:id error)))
          (is (= :origin-source-binding (:reason error)))
          (is (= #{:ordinal :path :field :owner-id} (set (keys (:details error)))))
          (is (nil? (get-in error [:details :path]))))))))

(deftest retained-entry-rejects-nonplain-carriers-before-realization-or-hash
  (let [args (entry "a") calls (atom 0)
        lazy-bytes (map (fn [_] (swap! calls inc) 97) (range 1))]
    (doseq [values [lazy-bytes (subvec [97 98] 0 1) (with-meta [97] {:x true})]]
      (let [error (failure #(source! (assoc-in args [2 :blob-bytes] values)))]
        (is (= :origin-shape-bound (:reason error)))))
    (is (zero? @calls))
    (is (= :origin-shape-bound
           (:reason (failure #(source! (assoc args 1 (with-meta (nth args 1) {:x true})))))))
    (doseq [byte [nil -1 256 1/2]]
      (let [error (failure #(source! (-> args (assoc-in [1 :byte-count] -1)
                                               (assoc-in [2 :blob-bytes 0] byte))))]
        (is (= :origin-shape-bound (:reason error)))
        (is (= [:blob-bytes] (get-in error [:details :field])))))
))

(deftest retained-long-path-and-proof-projection-only
  (let [path (apply str (repeat 2048 "\u00e9")) args (entry path "a")]
    (is (= path (:source-path (source! args))))
    (is (= (str path "x") (:source-path (source! (entry (str path "x") "a"))))))
  ;; Tree-chain/schema/OID admission belongs to the complete owning H6 gate.
  (is (= "a" (:source-text (source! (assoc-in (entry "a") [2 :tree-chain] :unvalidated))))))


(defn- flat-source [n] (str "[" (apply str (repeat n "0 ")) "]"))
(defn- nested-source [n middle]
  (str (apply str (repeat n "[")) middle (apply str (repeat n "]"))))

(deftest lexical-and-parsed-inputs-have-no-project-capacity-quota
  (is (nil? (lexical! (nested-source 513 "0"))))
  (is (nil? (lexical! (flat-source 131073))))
  (is (nil? (forms! [(vec (repeat 131073 0))])))
  (let [shared {:a [1 2]}]
    (is (nil? (forms! [shared shared (with-meta 'x {:doc "retained"})]))))
  (doseq [value [(Object.) (with-meta 'x {:bad (Object.)})]]
    (is (= :origin-reader-contract (:reason (failure #(forms! [value])))))))

(deftest parsed-native-cycle-is-rejected-without-an-alias-discount
  (let [backing (object-array 2)
        cyclic (clojure.lang.PersistentArrayMap. backing)]
    (aset backing 0 :self)
    (aset backing 1 cyclic)
    (is (= :origin-reader-contract (:reason (failure #(forms! [cyclic])))))))



(deftest seed-dispatch-rejections-precede-host-expansion
  (let [calls (atom 0)]
    (with-redefs-fn {(private-var 'read-source-form-records)
                    (fn [& _] (swap! calls inc) (throw (ex-info "host reader reached" {})))}
      #(doseq [text ["#(%999999999)" "#\"x\"" "#?(:clj 0)" "#:x{:a 0}"
                    "#=(identity 0)" "#evil 0" "#inst \"2000\""
                    "`x" "::x" "'::x"]]
         (is (= :origin-reader-contract (:reason (failure (fn [] (intake! (entry text)))))))))
    (is (zero? @calls)))
  (is (nil? (lexical! "#{1 2} #'name #_discarded 0")))
  (is (nil? (lexical! "[\"` ::x\" \\`]"))))




(deftest fresh-intake-preserves-host-records-and-denies-ambient-readers
  (let [args (entry "; comment\n(def x '[a \"b\\\"c\" \\] #{1 2}])\n")
        text (nth args 3) path (first args)
        expected (bootstrap/read-source-form-records-host-oracle path text)
        fabricated {:source-path path :source-text text :records [{:form :wrong}]}
        calls (atom 0)
        original (var-get (private-var 'read-source-form-records))]
    (binding [bootstrap/*authenticated-source-form-records* fabricated]
      (with-redefs-fn {(private-var 'read-source-form-records)
                      (fn [path text]
                        (swap! calls inc)
                        (is (nil? bootstrap/*authenticated-source-form-records*))
                        (is (false? *read-eval*))
                        (is (= {} *data-readers*))
                        (is (nil? *default-data-reader-fn*))
                        (original path text))}
        #(let [a (intake! args) b (intake! args)]
           (is (= expected (:records a) (:records b)))
           (is (= (mapv :form expected) (:forms a) (:forms b)))
           (is (not (identical? (:records a) (:records b)))))))
    (is (= 2 @calls)))
  (let [calls (atom 0)]
    (binding [*data-readers* {'evil (fn [_] (swap! calls inc) :injected)}
              *default-data-reader-fn* (fn [& _] (swap! calls inc) :injected)]
      (is (some? (failure #(intake! (entry "#evil 0"))))))
    (is (zero? @calls))))

(deftest actual-emitter-retained-intake-preserves-fresh-host-records
  ;; ROOT reviews this selector before running it under the bounded JVM broker.
  ;; This is the real formerly blocked source; no plan execution or H6 admission.
  (let [path "bootstrap/gravity/p15_s23/emitter.gravity"
        raw (java.nio.file.Files/readAllBytes
             (.toPath (java.io.File. path)))
        text (String. raw java.nio.charset.StandardCharsets/UTF_8)
        args (entry path text)
        expected (bootstrap/read-source-form-records-host-oracle path text)
        first-result (intake! args)
        second-result (intake! args)]
    (is (= 1670330 (alength raw)))
    (is (> (count text) 524288))
    (is (= (alength raw) (:source-byte-count first-result)))
    (is (= expected (:records first-result) (:records second-result)))
    (is (= (mapv :form expected) (:forms first-result) (:forms second-result)))
    (is (not (identical? (:records first-result) (:records second-result))))))

(defn- trace-actual-operations [events operations]
  (into {}
        (map (fn [[sym event]]
               (let [variable (private-var sym) original (var-get variable)]
                 [variable (fn [& args]
                             (let [result (apply original args)]
                               (swap! events conj event)
                               result))])))
        operations))

(deftest production-emitter-rule-joins-authentication-preflight-and-fresh-parsing
  (let [path (bootstrap/c-backend-resolve-p15-s23-compiler-source-path)
        pinned (bootstrap/p15-s23-stage2-compiler-pinned-source!
                path "<h6-production-control>" :jvm "P15S23Q001"
                bootstrap/p15-s23-stage2-plan-emitter-fail!)
        old-data (binding [bootstrap/*authenticated-source-form-records* nil]
                   (bootstrap/p15-s23-compiler-source-form-record-from-text
                    path (:source-text pinned)))
        emitter (bootstrap/p15-s23-compiler-def-value
                 path (:forms old-data) 'p15-s23-stage2-plan-emitter)
        expected {:emitter emitter :source-path path
                  :rule-record (bootstrap/p15-s23-stage2-plan-emitter-rule-record emitter)
                  :source-rule-hash
                  (str "sha256:" (bootstrap/sha256-hex
                                   (pr-str (bootstrap/c-backend-canonical-value emitter))))}
        events (atom [])
        operations (trace-actual-operations
                    events [['p15-s23-stage2-compiler-pinned-source! :authenticated]
                            ['p15-s23-h6-seed-text-preflight! :lexical]
                            ['read-source-form-records :raw-read]
                            ['p15-s23-h6-source-forms-preflight! :parsed]])
        raw-reader (get operations (private-var 'read-source-form-records))
        operations (assoc operations (private-var 'read-source-form-records)
                          (fn [& args]
                            (is (nil? bootstrap/*authenticated-source-form-records*))
                            (is (false? *read-eval*))
                            (is (= {} *data-readers*))
                            (is (nil? *default-data-reader-fn*))
                            (apply raw-reader args)))
        fabricated {:source-path path :source-text (:source-text pinned)
                    :records [{:form :fabricated-ambient-record}]}]
    (is (= 128499 (:source-byte-count pinned)))
    (is (= "sha256:57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527"
           (:source-content-hash pinned)))
    (binding [bootstrap/*authenticated-source-form-records* fabricated]
      (with-redefs-fn operations
        #(is (= expected (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                          "<h6-production-control>" :jvm)))))
    (is (= [:authenticated :lexical :raw-read :parsed] @events))))

(deftest production-compiler-tamper-fails-real-pinned-hash-before-parsing
  (let [source (bootstrap/c-backend-resolve-p15-s23-compiler-source-path)
        raw (java.nio.file.Files/readAllBytes (.toPath (java.io.File. source)))
        changed (aclone raw)
        _ (aset-byte changed 0 (unchecked-byte (bit-xor 1 (bit-and 255 (aget raw 0)))))
        path (java.nio.file.Files/createTempFile
              "h6-pinned-compiler-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))
        events (atom [])
        actual-pinned (var-get (private-var 'p15-s23-stage2-compiler-pinned-source!))
        operations
        {(private-var 'c-backend-resolve-p15-s23-compiler-source-path) (fn [] (str path))
         (private-var 'p15-s23-stage2-compiler-pinned-source!)
         (fn [& args] (swap! events conj :authentication-started) (apply actual-pinned args))
         (private-var 'p15-s23-h6-seed-text-preflight!)
         (fn [& _] (swap! events conj :lexical) (throw (ex-info "preflight reached" {})))
         (private-var 'read-source-form-records)
         (fn [& _] (swap! events conj :raw-read) (throw (ex-info "reader reached" {})))
         (private-var 'p15-s23-h6-source-forms-preflight!)
         (fn [& _] (swap! events conj :parsed) (throw (ex-info "postcheck reached" {})))}]
    (try
      (java.nio.file.Files/write path changed (make-array java.nio.file.OpenOption 0))
      (with-redefs-fn operations
        #(let [error (failure (fn [] (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                                     "<h6-tamper-control>" :jvm)))]
           (is (= "P15S23Q001" (:id error)))
           (is (= :stage2-compiler-source-content-hash (:missing-fact error)))
           (is (= 128499 (alength changed)))))
      (is (= [:authentication-started] @events))
      (finally (java.nio.file.Files/deleteIfExists path)))))
