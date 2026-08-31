

(def type-keywords
  {:Bottom "Bottom"
   :Never "Never"
   :Nil "Nil"
   :Unit "Unit"
   :Boolean "Boolean"
   :I8 "I8"
   :I16 "I16"
   :I32 "I32"
   :I64 "I64"
   :U8 "U8"
   :U16 "U16"
   :U32 "U32"
   :U64 "U64"
   :Int "Int"
   :Integer "Integer"
   :BigInt "BigInt"
   :F32 "F32"
   :F64 "F64"
   :Exact "Exact"
   :Symbol "Symbol"
   :Keyword "Keyword"
   :String "String"
   :Text "Text"
   :List "List"
   :Vector "Vector"
   :Map "Map"
   :Set "Set"
   :Tuple "Tuple"
   :Struct "Struct[Vec3]"
   :Record "Record[Fixture]"
   :Enum "Enum[Color.Red]"
   :TaggedUnion "Union[Result.Ok]"
   :Function "Fn[1]->String"
   :Protocol "Protocol[Displayable]"
   :Interface "Interface[Renderable]"
   :Existential "Exists[Displayable]"
   :Generic "Generic[T]"
   :Effect "Effect[:io/write]"
   :Region "Region[r,Buffer]"
   :Owned "Owned[Buffer]"
   :Borrow "Borrow[Buffer]"
   :BorrowMut "BorrowMut[Buffer]"
   :Ptr "Ptr[U8]"
   :RawPtr "RawPtr[U8]"
   :MMIO "MMIO[U32]"
   :Uninit "Uninit[Buffer]"
   :Init "Init[Buffer]"
   :Linear "Linear[FileHandle]"
   :Tainted "Tainted[String]"
   :CompileTime "Comptime[Integer]"
   :ConstGeneric "ConstGeneric[N]"
   :Schema "Schema[User]"
   :Syntax "SyntaxObject"
   :IR "IR[GravityMIR]"
   :ArtifactRef "ArtifactRef[Schema]"
   :GeneratedForm "GeneratedFormArtifact"
   :Dynamic "Dynamic"})

(def l5-required-type-categories
  [:bottom :never :nil :unit :boolean :signed-integer :unsigned-integer
   :machine-integer :big-integer :floating :exact-numeric :symbol :keyword
   :string :text-view :list :vector :map :set :tuple :struct :record :enum
   :tagged-union :function :protocol :interface :existential :generic
   :effect :region :owned :borrow :borrow-mut :pointer :raw-pointer :mmio
   :uninit :init :linear :tainted :compile-time :const-generic
   :schema-derived :syntax :ir :artifact-reference :dynamic])

(def exact-type-categories
  {"Bottom" :bottom
   "Never" :never
   "Nil" :nil
   "Unit" :unit
   "Boolean" :boolean
   "I8" :signed-integer
   "I16" :signed-integer
   "I32" :signed-integer
   "I64" :signed-integer
   "U8" :unsigned-integer
   "U16" :unsigned-integer
   "U32" :unsigned-integer
   "U64" :unsigned-integer
   "Int" :machine-integer
   "Integer" :machine-integer
   "BigInt" :big-integer
   "F32" :floating
   "F64" :floating
   "Exact" :exact-numeric
   "ExactRatio" :exact-numeric
   "Symbol" :symbol
   "Keyword" :keyword
   "String" :string
   "Text" :text-view
   "List" :list
   "Vector" :vector
   "Map" :map
   "Set" :set
   "Tuple" :tuple
   "Dynamic" :dynamic
   "SyntaxObject" :syntax})

(defn type-category
  [type-name]
  (or (get exact-type-categories type-name)
      (cond
        (str/starts-with? type-name "Struct[") :struct
        (str/starts-with? type-name "Record[") :record
        (str/starts-with? type-name "Enum[") :enum
        (str/starts-with? type-name "Union[") :tagged-union
        (str/starts-with? type-name "Fn[") :function
        (str/starts-with? type-name "Protocol[") :protocol
        (str/starts-with? type-name "Interface[") :interface
        (str/starts-with? type-name "Exists[") :existential
        (str/starts-with? type-name "Generic[") :generic
        (str/starts-with? type-name "Effect[") :effect
        (str/starts-with? type-name "Region[") :region
        (str/starts-with? type-name "Owned[") :owned
        (str/starts-with? type-name "Borrow[") :borrow
        (str/starts-with? type-name "BorrowMut[") :borrow-mut
        (str/starts-with? type-name "Ptr[") :pointer
        (str/starts-with? type-name "RawPtr[") :raw-pointer
        (str/starts-with? type-name "MMIO[") :mmio
        (str/starts-with? type-name "Uninit[") :uninit
        (str/starts-with? type-name "Init[") :init
        (str/starts-with? type-name "Linear[") :linear
        (str/starts-with? type-name "Tainted[") :tainted
        (str/starts-with? type-name "Comptime[") :compile-time
        (str/starts-with? type-name "ConstGeneric[") :const-generic
        (str/starts-with? type-name "Schema[") :schema-derived
        (str/starts-with? type-name "Validated[") :schema-derived
        (str/starts-with? type-name "IR[") :ir
        (str/starts-with? type-name "ArtifactRef[") :artifact-reference
        :else :unknown)))

(defn type-form-name
  [form]
  (cond
    (keyword? form) (get type-keywords form (name form))
    (symbol? form) (name form)
    (and (seq? form) (= 'Linear (first form))) (str "Linear[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'Owned (first form))) (str "Owned[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'Borrow (first form))) (str "Borrow[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'BorrowMut (first form))) (str "BorrowMut[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'Ptr (first form))) (str "Ptr[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'RawPtr (first form))) (str "RawPtr[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'MMIO (first form))) (str "MMIO[" (type-form-name (second form)) "]")
    (and (seq? form) (= 'Fn (first form))) (str form)
    :else (str form)))