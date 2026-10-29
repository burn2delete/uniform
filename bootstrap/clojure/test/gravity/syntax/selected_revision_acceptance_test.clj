(ns gravity.syntax.selected-revision-acceptance-test
  "Input refusal controls only; never loads Bootstrap or calls a genuine owner."
  (:require [clojure.test :refer [deftest is]]
            [gravity.syntax.selected-revision-acceptance :as acceptance]
            [gravity.resolve.selected-source-harness :as h]))

(deftest incomplete-request-cannot-reach-any-owner
  (let [calls (atom []) owner #(do (swap! calls conj %) (throw (ex-info "unexpected owner" {})))]
    (doseq [prepared [nil {} {:root "/fixture" :source {:head "head" :tree "tree"}}]]
      (is (= :prepared-C3-input-contract
             (try (acceptance/exercise! owner prepared) nil
                  (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
    (is (empty? @calls))))

(deftest drift-refuses-before-context-or-construction
  (let [calls (atom []) owner #(swap! calls conj %)
        prepared {:root "/fixture" :source {:head "head" :tree "tree"}
                  :source-path "/input" :source-text "source" :reader-source-path "/reader"
                  :invocation {:syntax-source-selection {:source-path "/syntax"}}
                  :inputs {:fixture {:path "/input" :text "source" :bytes 192 :sha256 h/fixture-sha}
                           :reader {:path "/reader"} :syntax {:path "/syntax"}}}]
    (with-redefs [h/recheck-inputs! (fn [_] (throw (ex-info "fake source drift" {:reason :input-drift})))]
      (is (= :input-drift (try (acceptance/exercise! owner prepared) nil
                              (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
    (is (empty? @calls))
    (is (not-any? #{'compiler-c5-resolution-source-artifact 'sh05-macro-artifact-verification}
                  acceptance/owner-operations))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest omitted-runtime-binding-refuses-before-owner-lookup
  (let [calls (atom []) prepared {:root "/fixture" :source {:head "head" :tree "tree"}
                                 :source-path "/input" :source-text "source" :reader-source-path "/reader"
                                 :invocation {:syntax-source-selection {:source-path "/syntax"}}
                                 :inputs {:fixture {:path "/input" :text "source" :bytes 192 :sha256 h/fixture-sha}
                                          :reader {:path "/reader"} :syntax {:path "/syntax"}}
                                 :closure {:source-files [{:path "/input"}] :cwd "/fixture"
                                           :classpath ["/unbound.jar"] :argv ["/unbound-java"]}}]
    (with-redefs [h/recheck-inputs! (fn [_] nil)
                  acceptance/source-closure-paths (fn [_] ["/input"])]
      (is (= :complete-consumed-closure
             (try (acceptance/exercise! #(swap! calls conj %) prepared) nil
                  (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))
    (is (empty? @calls))))
