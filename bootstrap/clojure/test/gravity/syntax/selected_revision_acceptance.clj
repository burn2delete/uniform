(ns gravity.syntax.selected-revision-acceptance
  "One explicit C2-to-C3 acceptance operation. No loader, launcher or admission port."
  (:require [clojure.java.io :as io] [clojure.string :as str]
            [gravity.resolve.selected-source-harness :as h]))

(def selector :syntax/selected-reader-revision)
(def owner-operations
  '[reader-project-context-for-source compiler-c3-syntax-source-artifact
    c3-syntax-verification-report c3-artifact-id
    p15-s23-stage2-sh02-descriptor-envelope p15-s23-stage2-sh02-descriptor-envelope-verify!])

(defn source-closure-paths [root]
  (mapv #(str (io/file root %))
        (str/split-lines (h/git! root "ls-files" "bootstrap/clojure/src"
                                "bootstrap/gravity" "bootstrap/clojure/test" "deps.edn"))))

(defn recheck-closure! [{:keys [root closure] :as prepared}]
  (h/recheck-inputs! prepared)
  (let [runtime-paths (set (map :path (:runtime-files closure)))
        source-roots #{(str (io/file root "bootstrap/clojure/src"))
                       (str (io/file root "bootstrap/clojure/test"))}
        argv (:argv closure) cp-index (.indexOf ^java.util.List (vec argv) "-cp")]
    (h/ensure! (and (= (set (source-closure-paths root)) (set (map :path (:source-files closure))))
                  (seq (:runtime-files closure)) (seq (:classpath closure))
                  (= root (:cwd closure)) (runtime-paths (first argv))
                  (every? #(or (source-roots %) (runtime-paths %)) (:classpath closure))
                  (not (neg? cp-index))
                  (= (get argv (inc cp-index)) (str/join java.io.File/pathSeparator (:classpath closure))))
               :complete-consumed-closure))
  (doseq [input (concat (:source-files closure) (:runtime-files closure) (:config-files closure))]
    (let [path (.toPath (io/file (:path input)))
          _ (h/ensure! (and (.isAbsolute (io/file (:path input)))
                            (not (java.nio.file.Files/isSymbolicLink path))) :absolute-regular-closure-input)
          data (java.nio.file.Files/readAllBytes path)]
      (h/ensure! (and (= (:bytes input) (alength data)) (= (:sha256 input) (h/sha data))) :consumed-input-drift))))

(defn exercise!
  "Caller supplies reviewed, frozen inputs and already-loaded normal owner Vars.
  Returns actual products and reports; injected owner controls are not acceptance."
  [owner {:keys [source-path source-text reader-source-path invocation inputs] :as prepared}]
  (h/ensure! (and (:root prepared) (get-in prepared [:source :head])
                  (get-in prepared [:source :tree])
                  (= #{:fixture :reader :syntax} (set (keys inputs)))
                  (= source-path (get-in inputs [:fixture :path]))
                  (= source-text (get-in inputs [:fixture :text]))
                  (= reader-source-path (get-in inputs [:reader :path]))
                  (= (get-in inputs [:syntax :path])
                     (get-in invocation [:syntax-source-selection :source-path]))
                  (= 192 (get-in inputs [:fixture :bytes]))
                  (= h/fixture-sha (get-in inputs [:fixture :sha256]))) :prepared-C3-input-contract)
  (recheck-closure! prepared)
  (try
    (let [context ((owner 'reader-project-context-for-source) source-path)
          artifact ((owner 'compiler-c3-syntax-source-artifact)
                    source-path source-text context nil nil reader-source-path invocation)
          boundary (:gravity-syntax-boundary artifact)
          binding (:plan-binding boundary)
          report (fn [a] ((owner 'c3-syntax-verification-report)
                           (:syntax-object-stream a) (:syntax-serialization-fixture a)
                           (:c2-reader-artifact a) (:gravity-syntax-boundary a) invocation))
          positive (report artifact)]
      (h/ensure! (and (= :gravity/stage0-c3-syntax-object-artifact (:kind artifact))
                      (= :complete (get-in artifact [:c3-syntax-results :status]))
                      (= :gravity/sh04-development-syntax-plan-binding-v1 (:artifact binding))
                      (= :development-unadmitted (:admission-status binding))
                      (= (get-in inputs [:syntax :path]) (:source-path binding))
                      (= (str "sha256:" (get-in inputs [:syntax :sha256])) (:source-content-hash binding))
                      (= invocation (:operation-context boundary))
                      (= (:artifact-id artifact) ((owner 'c3-artifact-id) artifact))
                      (= :passed (:status positive))
                      (true? (:reader-products-authentic? positive))) :actual-selected-C3)
      (h/ensure! (= (str "sha256:" (get-in inputs [:reader :sha256]))
                     (get-in artifact [:c2-reader-artifact :sh03-reader-authentication
                                       :reader-source-revision :source-content-hash])) :actual-Reader-revision)
      (h/ensure! (= (str "sha256:" (get-in inputs [:fixture :sha256]))
                     (get-in artifact [:c2-reader-artifact :source-unit-record :bytes-hash])) :actual-fixture-source)
      (let [descriptor (:authenticated-envelope-descriptor boundary)
            changed (h/semantic-mutation descriptor :binding-id)
            options (:authenticated-envelope-options invocation)
            envelope ((owner 'p15-s23-stage2-sh02-descriptor-envelope)
                      :c3-syntax :gravity/sh04-syntax-products changed source-path options)
            envelope-report ((owner 'p15-s23-stage2-sh02-descriptor-envelope-verify!)
                             envelope :c3-syntax :gravity/sh04-syntax-products changed source-path options)
            _ (h/ensure! (and (= :accepted (:status envelope))
                               (= :passed (:status envelope-report))) :actual-negative-envelope)
            copy (-> artifact
                     (assoc-in [:gravity-syntax-boundary :authenticated-envelope-descriptor] changed)
                     (assoc-in [:gravity-syntax-boundary :authenticated-envelope] envelope))
            copy (assoc copy :artifact-id ((owner 'c3-artifact-id) copy))
            binding-report (report copy)
            physical (h/replace-existing artifact [:gravity-syntax-boundary :plan-binding :source-path]
                                         reader-source-path)
            physical-report (report physical)
            revision-path [:c2-reader-artifact :sh03-reader-authentication :reader-source-revision :source-content-hash]
            revision-copy (h/replace-existing artifact revision-path
                           (h/different-id (get-in artifact revision-path)))
            revision-report (report revision-copy)]
        (h/ensure! (not= (:artifact-id artifact) (:artifact-id copy)) :negative-identity-propagation)
        (h/ensure! (= (:artifact-id artifact) ((owner 'c3-artifact-id) physical)) :physical-semantic-neutrality)
        (doseq [r [binding-report physical-report revision-report]]
          (h/ensure! (h/c3-rejected? r) :actual-substitution-refusal))
        {:selector selector :positive-artifact artifact :fresh-positive-report positive
         :negative-envelope envelope :negative-envelope-verification envelope-report
         :negative-artifact copy :binding-substitution-report binding-report
         :physical-source-substitution-artifact physical :physical-source-substitution-report physical-report
         :Reader-revision-substitution-artifact revision-copy :Reader-revision-substitution-report revision-report
         :source-bookends :caller-retains-complete-closure-receipts
         :scope :exact-selected-C2-to-C3-only :self-hosting false :release false}))
    (finally (recheck-closure! prepared))))
