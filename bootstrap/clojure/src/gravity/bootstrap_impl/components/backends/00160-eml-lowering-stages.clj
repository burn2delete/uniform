

(def eml-lowering-stages
  [:efir-read :basis-introduce :domain-transport :branch-transport
   :algebraic-normalize :complex-simplify :candidate-emit :trace-check])