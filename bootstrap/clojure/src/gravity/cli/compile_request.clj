(ns gravity.cli.compile-request
  "Shared structural parser for the public compile command."
  (:require [clojure.string :as str]))

(def expected-forms
  [["compile" "<file.qst|file.gravity>"]
   ["compile" "<file.qst|file.gravity>" "-o" "<executable>"]
   ["compile" "<file.qst|file.gravity>" "--target" "c" "-o" "<executable>"]
   ["compile" "<file.qst|file.gravity>" "--target" "c"
    "--lowering" "runtime-derived" "-o" "<executable>"]])

(defn path-segments
  [path]
  (vec (remove str/blank? (str/split (str path) #"/+"))))

(defn output-path-allowed?
  [path]
  (let [file (java.io.File. (str path))
        segments (path-segments path)]
    (and (string? path)
         (not (str/blank? path))
         (not (.isAbsolute file))
         (not-any? #{".."} segments)
         (or (str/starts-with? path "target/")
             (= 1 (count segments))))))

(defn parse-shape!
  "Parse command shape and return raw option values.

  The caller supplies the existing diagnostic owner. Backend and lowering
  interpretation are intentionally left to the bootstrap parser."
  [args fail!]
  (let [[_ source-path & options] args]
    (when-not (and (string? source-path)
                   (not (str/blank? source-path))
                   (not (str/starts-with? source-path "-")))
      (fail! {:source "bin/gravity"
              :command args
              :missing-fields [:source-path]}))
    (loop [remaining (vec options)
           target nil
           output-path nil
           lowering-mode nil
           output-option nil]
      (if (empty? remaining)
        {:source-path source-path
         :target-argument target
         :output-path output-path
         :output-option output-option
         :lowering-argument lowering-mode}
        (let [option (first remaining)
              rest-options (subvec remaining 1)]
          (cond
            (#{"-o" "--output"} option)
            (do
              (when (or (empty? rest-options) output-path)
                (fail! {:source source-path
                        :command args
                        :option option
                        :missing-fields (if (empty? rest-options)
                                          [:output-path]
                                          [:duplicate-output-option])}))
              (let [candidate (first rest-options)]
                (when (or (str/blank? (str candidate))
                          (str/starts-with? (str candidate) "-"))
                  (fail! {:source source-path
                          :command args
                          :option option
                          :missing-fields [:output-path]}))
                (when-not (output-path-allowed? candidate)
                  (fail! {:source source-path
                          :command args
                          :option option
                          :output-path candidate
                          :allowed-output-roots ["target/"
                                                  "<current-directory>"]}))
                (recur (subvec rest-options 1) target candidate
                       lowering-mode option)))

            (= "--target" option)
            (do
              (when (or (empty? rest-options) target)
                (fail! {:source source-path
                        :command args
                        :option option
                        :missing-fields (if (empty? rest-options)
                                          [:target]
                                          [:duplicate-target-option])}))
              (let [candidate (first rest-options)]
                (when (or (str/blank? (str candidate))
                          (str/starts-with? (str candidate) "-"))
                  (fail! {:source source-path
                          :command args
                          :option option
                          :missing-fields [:target]}))
                (recur (subvec rest-options 1) candidate output-path
                       lowering-mode output-option)))

            (= "--lowering" option)
            (do
              (when (or (empty? rest-options) lowering-mode)
                (fail! {:source source-path
                        :command args
                        :option option
                        :missing-fields (if (empty? rest-options)
                                          [:lowering-mode]
                                          [:duplicate-lowering-option])}))
              (let [candidate (first rest-options)]
                (when (or (str/blank? (str candidate))
                          (str/starts-with? (str candidate) "-"))
                  (fail! {:source source-path
                          :command args
                          :option option
                          :missing-fields [:lowering-mode]}))
                (recur (subvec rest-options 1) target output-path
                       candidate output-option)))

            :else
            (fail! {:source source-path
                    :command args
                    :unsupported-option option
                    :expected-forms expected-forms})))))))

(defn validate!
  "Validate the structural compile command without loading the bootstrap."
  [args]
  (parse-shape!
   args
   (fn [facts]
     (let [message "compile executable output usage is invalid"]
       (throw
        (ex-info message
                 (merge {:id "P18T04002"
                         :message message
                         :bootstrap-stage :stage0
                         :source-span {:source (or (:source facts)
                                                   "bin/gravity")}
                         :phase "P18-T04"
                         :diagnostic-family
                         :p18-t04-executable-command-contract}
                        facts))))))
  true)
