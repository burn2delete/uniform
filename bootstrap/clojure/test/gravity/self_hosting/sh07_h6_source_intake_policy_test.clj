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
          (is (= #{:ordinal :path :field :owner-id :limit} (set (keys (:details error)))))
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
    (let [hash-calls (atom 0) too-wide (vec (repeat 16777217 32))]
      (with-redefs-fn {(private-var 'sha256-bytes-hex)
                      (fn [_] (swap! hash-calls inc) (throw (ex-info "hash reached" {})))}
        #(let [error (failure (fn [] (source! (-> args
                                                                (assoc-in [1 :byte-count] 16777217)
                                                                (assoc-in [2 :blob-bytes] too-wide)))))]
           (is (= {:name :max-source-entry-bytes :maximum 16777216 :observed 16777217}
                  (get-in error [:details :limit])))))
      (is (zero? @hash-calls)))))

(deftest retained-path-byte-bound-and-proof-projection-only
  (let [path (apply str (repeat 2048 "\u00e9")) args (entry path "a")]
    (is (= path (:source-path (source! args))))
    (is (= :origin-source-binding
           (:reason (failure #(source! (entry (str path "x") "a")))))))
  ;; Tree-chain/schema/OID admission belongs to the complete owning H6 gate.
  (is (= "a" (:source-text (source! (assoc-in (entry "a") [2 :tree-chain] :unvalidated))))))

(deftest retained-entry-exact-byte-cap-is-independent-of-plan-nodes
  (let [text (String. (char-array 16777216 \space))
        args (entry text) result (source! args)]
    (is (= 16777216 (:source-byte-count result)))
    (is (= text (:source-text result)))
    (is (nil? (lexical! text)))))

(defn- flat-source [n] (str "[" (apply str (repeat n "0 ")) "]"))
(defn- nested-source [n middle]
  (str (apply str (repeat n "[")) middle (apply str (repeat n "]"))))

(deftest lexical-exact-depth-width-and-cumulative-node-reservations
  (is (nil? (lexical! (nested-source 512 ""))))
  (is (nil? (lexical! (flat-source 131072))))
  (let [exact (str "[" (flat-source 131071) (flat-source 131071)
                   (flat-source 131070) (flat-source 131070) "]")]
    (is (> (count exact) 524288))
    (is (nil? (lexical! exact)))
    (is (= :max-plan-nodes
           (get-in (failure #(lexical! (str (subs exact 0 (dec (count exact))) " 0]")))
                   [:details :limit :name])))))

(deftest lexical-bound-failures-precede-the-host-reader
  (let [calls (atom 0)]
    (with-redefs-fn {(private-var 'read-source-form-records)
                    (fn [& _] (swap! calls inc) (throw (ex-info "host reader reached" {})))}
      #(doseq [[limit text]
                [[:max-plan-depth (nested-source 513 "")]
                 [:max-plan-depth (nested-source 512 "0")]
                 [:max-plan-depth (str (apply str (repeat 513 "'")) "x")]
                 [:max-plan-width (flat-source 131073)]
                 [:max-plan-nodes (str "[" (apply str (repeat 5 (flat-source 110000))) "]")]
                 [:max-plan-nodes (str "[" (apply str (repeat 5 (str "[" (apply str (repeat 40000 "'x ")) "]"))) "]")]]]
         (let [error (failure (fn [] (intake! (entry text))))]
           (is (= :origin-reader-contract (:reason error)))
           (is (= limit (get-in error [:details :limit :name]))))))
    (is (zero? @calls))))

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

(deftest parsed-carrier-exact-and-over-bound-controls
  (let [wide (vec (repeat 131072 0))
        exact [wide wide wide (vec (repeat 131067 0))]]
    (is (nil? (forms! exact)))
    (is (= :max-plan-nodes
           (get-in (failure #(forms! (update exact 3 conj 0))) [:details :limit :name])))
    (is (= :max-plan-width
           (get-in (failure #(forms! [(conj wide 0)])) [:details :limit :name]))))
  (let [nested (reduce (fn [v _] [v]) [] (range 511))]
    (is (nil? (forms! [nested])))
    (is (= :max-plan-depth
           (get-in (failure #(forms! [[nested]])) [:details :limit :name]))))
  (is (= :origin-reader-contract (:reason (failure #(forms! [(Object.)])))))
  (is (= :origin-reader-contract
         (:reason (failure #(forms! [(with-meta 'x {:untrusted (Object.)})]))))))

(deftest parsed-expansion-check-runs-after-host-parsing
  (let [calls (atom 0) args (entry "'x")]
    (with-redefs-fn {(private-var 'read-source-form-records)
                    (fn [& _] (swap! calls inc) [{:form (vec (repeat 131073 0))}])}
      #(is (= :max-plan-width
              (get-in (failure (fn [] (intake! args))) [:details :limit :name]))))
    (is (= 1 @calls))))

(deftest map-entry-width-agrees-with-owning-carrier-policy
  (let [exact (into {} (map (fn [i] [i i]) (range 131072)))
        over (assoc exact 131072 131072)]
    (is (nil? (forms! [exact])))
    (is (nil? (lexical! (pr-str exact))))
    (doseq [operation [#(forms! [over]) #(lexical! (pr-str over))]]
      (is (= {:name :max-plan-width :maximum 131072 :observed 131073}
             (get-in (failure operation) [:details :limit]))))))

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
