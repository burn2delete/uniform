(ns gravity.bootstrap.module-literals-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap.module-literals :as literals]))

(defn- refuse! [reason facts]
  (throw (ex-info "literal refusal" {:id "BOOT2006" :facts (assoc facts :reason reason)})))

(defn- diagnostic [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo exception (ex-data exception))))

(deftest seed-literal-data-and-inert-quote
  (doseq [value [nil false true 42 1.25M \a :tag "text"
                 {:nested [nil true 7 "value" #{:a}]}]]
    (is (= value (literals/materialize! #'refuse! value))))
  (let [data '(data (quote untouched) (dangerous call))]
    (is (identical? data (literals/materialize! #'refuse! (list 'quote data)))))
  (is (= {:symbol 'catalog :forms '(call argument)}
         (literals/materialize! #'refuse! '{:symbol (quote catalog) :forms (quote (call argument))}))))

(deftest well-formed-data-has-no-project-work-quota
  (doseq [value [(nth (iterate vector :leaf) 66)
                 (apply str (repeat 65537 "x")) (vec (repeat 65537 nil))]]
    (is (= value (literals/materialize! #'refuse! value)))))

(deftest real-shape-refusals-and-late-diagnostic-port
  (doseq [value ['other '(identity 1) '() '(quote) '(quote data extra)
                 {:nested ['other]} (Object.) (list 'quote (Object.))
                 (nth (iterate vector 'other) 66)
                 (conj (vec (repeat 65537 nil)) 'other)]]
    (is (= {:id "BOOT2006" :facts {:reason :unsupported-literal-data}}
           (diagnostic #(literals/materialize! #'refuse! value)))))
  (let [calls (atom [])]
    (doseq [label [:first :second]]
      (with-redefs [refuse! (fn [reason facts]
                             (swap! calls conj [label reason facts])
                             (throw (ex-info "replacement" {:label label})))]
        (is (= {:label label} (diagnostic #(literals/materialize! #'refuse! 'other))))))
    (is (= [[:first :unsupported-literal-data {}] [:second :unsupported-literal-data {}]] @calls)))
  (is (= :none (:admission (:gravity/namespace-contract (meta #'literals/materialize!))))))

(deftest complete-paired-source-plans-and-real-refusals
  ;; Ordinary complete-source seed development only. No source/Core admission.
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "gravity-module-literals-" (make-array java.nio.file.attribute.FileAttribute 0)))
        compile-plan (requiring-resolve 'gravity.bootstrap/p15-s23-stage2-compiler-artifact-plan)
        emitter-for (requiring-resolve 'gravity.bootstrap/c-backend-stage2-plan-emitter-source-rule!)
        execute (requiring-resolve 'gravity.bootstrap/p15-s23-stage2-runtime-execute-function)
        sha256 (requiring-resolve 'gravity.bootstrap/sha256-hex)
        canonical (requiring-resolve 'gravity.bootstrap/c-backend-canonical-value)
        long-value (apply str (repeat 65537 "x"))
        source (str "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog "
                    (pr-str long-value) ") (defn main [] catalog)")]
    (try
      (let [plans
            (mapv (fn [extension]
                    (let [file (io/file directory (str "literal" extension))
                          _ (spit file source :encoding "UTF-8")
                          path (.getCanonicalPath file) actual (slurp file :encoding "UTF-8")
                          emitter (:emitter (emitter-for path :jvm))
                          plan (compile-plan emitter path actual)
                          refusal (diagnostic #(compile-plan emitter path
                                                 "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog (identity 1))"))]
                      (is (= source actual))
                      (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
                      (is (= (str "sha256:" (sha256 actual)) (get-in plan [:source :sha256])))
                      (is (= (:plan-id plan)
                             (str "sha256:" (sha256 (pr-str (canonical (dissoc plan :plan-id)))))))
                      (is (= long-value (execute {:engine :literal-development} plan 'main [])))
                      (is (= "BOOT2006" (:id refusal)))
                      (is (= :unsupported-literal-data (get-in refusal [:facts :reason])))
                      plan)) [".gravity" ".qst"])]
        (is (= (first plans) (second plans))))
      (finally
        (doseq [file (.listFiles directory)] (io/delete-file file))
        (io/delete-file directory)))))
