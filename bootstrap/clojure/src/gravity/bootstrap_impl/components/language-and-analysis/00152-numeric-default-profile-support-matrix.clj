

(def numeric-default-profile-support-matrix
  [{:profile :core :target :portable :family :fixed-integer
    :available? true :allocation :none}
   {:profile :core :target :portable :family :symbolic
    :available? true :allocation :none}
   {:profile :hosted :target :jvm :family :bigint
    :available? true :allocation :declared}
   {:profile :hosted :target :jvm :family :ratio
    :available? true :allocation :declared}
   {:profile :hosted :target :jvm :family :float
    :available? true :allocation :none}
   {:profile :native :target :llvm-x86-64-linux :family :fixed-integer
    :available? true :allocation :none}
   {:profile :native :target :llvm-x86-64-linux :family :float
    :available? true :allocation :none}
   {:profile :native :target :llvm-x86-64-linux :family :complex
    :available? true :allocation :none}
   {:profile :native :target :llvm-x86-64-linux :family :interval
    :available? true :allocation :bounded-region}
   {:profile :native :target :llvm-x86-64-linux :family :symbolic
    :available? true :allocation :none}
   {:profile :native :target :llvm-x86-64-linux :family :quantity
    :available? true :allocation :none}
   {:profile :firmware :target :armv7m :family :fixed-integer
    :available? true :allocation :none}
   {:profile :firmware :target :armv7m :family :bigint
    :available? false :allocation :forbidden}
   {:profile :kernel :target :linux-kernel :family :ratio
    :available? false :allocation :forbidden}
   {:profile :gpu :target :cuda-sm90 :family :float
    :available? true :allocation :device}
   {:profile :formal :target :proof :family :real
    :available? true :allocation :proof-only}
   {:profile :formal :target :proof :family :interval
    :available? true :allocation :proof-only}
   {:profile :formal :target :proof :family :symbolic
    :available? true :allocation :proof-only}])