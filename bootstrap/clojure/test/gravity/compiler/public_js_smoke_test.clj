(ns gravity.compiler.public-js-smoke-test
  (:require [clojure.test :refer [deftest]]
            [gravity.compiler.public-js-acceptance :as public-js]))

(deftest public-js-compiler-smoke
  (public-js/run-smoke!))
