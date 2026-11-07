(ns gravity.backend.llvm.differential-evidence-input-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private source-path
  "bootstrap/gravity/src/gravity/backend/w5_b14_backend_conformance_verifier.gravity")

;; Compile the current owner, without renewing historical migration pins.
(def ^:private plan
  (delay
    (let [emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path (slurp source-path)))))

(defn- invoke [arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-b14-linux-evidence-input-test
    :compiler-artifact-plan? true}
   @plan 'w5-b14-linux-differential-evidence-input arguments))

(def ^:private fields
  [:content-hash :byte-count :format :architecture :header])

(defn- expected [process-result object executable]
  {:kind :gravity/b14-bounded-llvm-differential-record
   :process-result process-result
   :elf-object (select-keys object fields)
   :elf-executable (select-keys executable fields)})

(deftest current-source-constructs-the-complete-preimage
  (let [process {:status :complete :expected-exit-code 7 :observed-exit-code 7
                 :stdout "" :stderr "" :extra {:nested [nil false 17]}}
        object {:content-hash "sha256:object" :byte-count 64 :format :elf
                :architecture :x86_64 :header {:type :relocatable}
                :bytes [1 2] :bookkeeping :excluded}
        executable {:content-hash "sha256:executable" :byte-count 128 :format :elf
                    :architecture :x86_64 :header {:type :executable}
                    :bytes [3 4] :bookkeeping :also-excluded}]
    (is (= (expected process object executable)
           (invoke [process object executable])))
    (doseq [side [0 1]
            field fields
            change [#(dissoc % field) #(assoc % field nil)
                    #(assoc % field [:changed field])]]
      (let [[o e] (update [object executable] side change)]
        (is (= (expected process o e) (invoke [process o e])))))
    (is (not= (invoke [process {} {}])
              (invoke [process {:header nil} {}])))
    (is (not= (invoke [process object executable])
              (invoke [(assoc process :arbitrary-field :retained)
                       object executable])))
    (is (= (invoke [process object executable])
           (invoke [process (assoc object :bytes [9] :bookkeeping :changed)
                    (assoc executable :bytes [8] :bookkeeping :changed)])))))

(deftest evidence-input-rejects-wrong-arity
  (doseq [arguments [[] [{}] [{} {}] [{} {} {} {}]]]
    (is (thrown? clojure.lang.ExceptionInfo (invoke arguments)))))
