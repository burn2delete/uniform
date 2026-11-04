(ns simplify.project-membership-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [simplify.github :as gh]
            [simplify.runtime :as rt])
  (:import [java.nio.file Files]))

(defn temp-dir []
  (str (Files/createTempDirectory "uniform-project-membership-test-"
                                  (make-array java.nio.file.attribute.FileAttribute 0))))

(defn context []
  (let [dir (temp-dir)
        config {:repo-dir dir :state-dir dir}]
    (merge {:state-dir dir
            :state-file (str (io/file dir "state.edn"))
            :log-lock (Object.)
            :state (atom {:schema 1 :tasks {} :jobs {} :effects {}})
            :config config}
           (rt/coordination dir config))))

(def duplicate-error
  (ex-info "GitHub GraphQL failed"
           {:errors [{:type "UNPROCESSABLE"
                      :message "Content already exists in this project"}]
            :restricted false}))

(defn membership-page [nodes has-next? end-cursor]
  {:node {:items {:nodes nodes
                  :pageInfo {:hasNextPage has-next?
                             :endCursor end-cursor}}}})

(defn project-context []
  (doto (context)
    (rt/update! assoc :project {:id "project"})))

(defn calls-of [calls kind]
  (filter #(if (map? %) (= kind (:kind %)) (= kind %)) @calls))

(deftest duplicate-add-is-read-back-once-without-repeating-add
  (let [ctx (project-context)
        calls (atom [])]
    (with-redefs [gh/graphql (fn [_ query variables]
                               (cond
                                 (str/starts-with? query "query")
                                 (do (swap! calls conj {:kind :read :variables variables})
                                     (if (= 1 (count (calls-of calls :read)))
                                       (membership-page [] false nil)
                                       (membership-page [{:id "item-after-race"
                                                          :content {:id "issue"}}] false nil)))

                                 (str/starts-with? query "mutation")
                                 (do (swap! calls conj {:kind :add :variables variables})
                                     (throw duplicate-error))

                                 :else (throw (AssertionError. (str "Unexpected GraphQL operation: " query)))))]
      (is (= "item-after-race" (gh/item! ctx {:node_id "issue"})))
      (is (= "item-after-race" (gh/item! ctx {:node_id "issue"})))
      (is (= 1 (count (calls-of calls :add))))
      (is (= 2 (count (calls-of calls :read))))
      (is (= :done (get-in @(:state ctx) [:effects "project-item/project/issue" :status])))
      (is (= "item-after-race" (get-in @(:state ctx) [:effects "project-item/project/issue" :result]))))))

(deftest preexisting-membership-still-skips-add-across-pages
  (let [ctx (project-context)
        calls (atom [])]
    (with-redefs [gh/graphql (fn [_ query variables]
                               (is (str/starts-with? query "query"))
                               (swap! calls conj variables)
                               (if (:after variables)
                                 (membership-page [{:id "item-existing"
                                                    :content {:id "issue"}}] false nil)
                                 (membership-page [{:id "other-item"
                                                    :content {:id "other"}}] true "next")))]
      (is (= "item-existing" (gh/item! ctx {:node_id "issue"})))
      (is (= 2 (count @calls)))
      (is (every? #(nil? (:after %)) (take 1 @calls)))
      (is (= "next" (:after (second @calls))))
      (is (= :done (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))

(deftest duplicate-add-without-exact-readback-remains-uncertain
  (doseq [[label postread]
          [["empty" (fn [_ _ _] (membership-page [] false nil))]
           ["unavailable" (fn [& _] {})]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [gh/graphql (fn [_ query variables]
                                   (cond
                                     (str/starts-with? query "query")
                                     (do (swap! calls conj :read)
                                         (if (= 1 (count (calls-of calls :read)))
                                           (membership-page [] false nil)
                                           (postread nil query variables)))

                                     (str/starts-with? query "mutation")
                                     (do (swap! calls conj :add)
                                         (throw duplicate-error))

                                     :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
          (is (thrown-with-msg? Exception
                                #"Duplicate Project membership has no verified readback|evidence is unavailable"
                                (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :add))))
          (is (= 2 (count (calls-of calls :read))))
          (is (= :uncertain (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))

(deftest duplicate-read-restriction-is-not-retried
  (let [ctx (project-context)
        calls (atom [])
        restricted-read (ex-info "GitHub GraphQL failed"
                                 {:errors [{:type "FORBIDDEN" :message "Resource not accessible"}]
                                  :restricted true})]
    (with-redefs [gh/graphql (fn [_ query _]
                               (cond
                                 (str/starts-with? query "query")
                                 (do (swap! calls conj :read)
                                     (if (= 1 (count (calls-of calls :read)))
                                       (membership-page [] false nil)
                                       (throw restricted-read)))

                                 (str/starts-with? query "mutation")
                                 (do (swap! calls conj :add)
                                     (throw duplicate-error))

                                 :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
      (is (thrown-with-msg? Exception #"GitHub GraphQL failed" (gh/item! ctx {:node_id "issue"})))
      (is (= 1 (count (calls-of calls :add))))
      (is (= 2 (count (calls-of calls :read))))
      (is (= :restricted (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))

(deftest unrelated-or-restricted-add-fails-without-postread
  (doseq [[label add-error expected-status]
          [["unrelated" (ex-info "GitHub GraphQL failed"
                                  {:errors [{:type "VALIDATION" :message "invalid content"}]
                                   :restricted false})
            :uncertain]
           ["duplicate without restriction proof" (ex-info "GitHub GraphQL failed"
                                                              {:errors [{:type "UNPROCESSABLE"
                                                                         :message "Content already exists in this project"}]})
            :uncertain]
           ["restricted" (ex-info "GitHub GraphQL failed"
                                   {:errors [{:type "FORBIDDEN" :message "Resource not accessible"}]
                                    :restricted true})
            :restricted]]]
    (testing label
      (let [ctx (project-context)
            calls (atom [])]
        (with-redefs [gh/graphql (fn [_ query _]
                                   (cond
                                     (str/starts-with? query "query")
                                     (do (swap! calls conj :read)
                                         (membership-page [] false nil))

                                     (str/starts-with? query "mutation")
                                     (do (swap! calls conj :add)
                                         (throw add-error))

                                     :else (throw (AssertionError. "Unexpected GraphQL operation"))))]
          (is (thrown? Exception (gh/item! ctx {:node_id "issue"})))
          (is (= 1 (count (calls-of calls :add))))
          (is (= 1 (count (calls-of calls :read))))
          (is (= expected-status (get-in @(:state ctx) [:effects "project-item/project/issue" :status]))))))))
