

(defn b6-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b6-document-override-diagnostics fail-kind)]
      (b6-document-fail!
       id source-path
       {:stage :b6-js-ts-backend-document-coverage
        :artifact-id (str "b6-document-" (name fail-kind))
        :missing-effect-capability-schema-fact fail-kind
        :host-symbol (name fail-kind)}
       {:missing-fields [fail-kind]}))))

(def b6-document-js-source
  (str "export class GravityPanic extends Error {\n"
       "  constructor(message, cause) {\n"
       "    super(message);\n"
       "    this.name = \"GravityPanic\";\n"
       "    this.cause = cause;\n"
       "  }\n"
       "}\n\n"
       "export function gravityEntry(x) {\n"
       "  if (typeof x !== \"bigint\") {\n"
       "    throw new GravityPanic(\"expected Gravity I64 BigInt boundary\");\n"
       "  }\n"
       "  return x;\n"
       "}\n\n"
       "export function optionFromNullish(value) {\n"
       "  if (value === null || value === undefined) {\n"
       "    return Object.freeze({ tag: \"none\" });\n"
       "  }\n"
       "  return Object.freeze({ tag: \"some\", value });\n"
       "}\n\n"
       "export async function translatePromise(thunk) {\n"
       "  try {\n"
       "    return Object.freeze({ tag: \"ok\", value: await thunk() });\n"
       "  } catch (error) {\n"
       "    return Object.freeze({ tag: \"error\", error: normalizeHostError(error) });\n"
       "  }\n"
       "}\n\n"
       "export function checkedNumber(value) {\n"
       "  if (typeof value !== \"number\" || !Number.isFinite(value)) {\n"
       "    throw new GravityPanic(\"invalid Gravity F64 Number boundary\");\n"
       "  }\n"
       "  return value;\n"
       "}\n\n"
       "export function packI32(values) {\n"
       "  return Int32Array.from(values);\n"
       "}\n\n"
       "function normalizeHostError(error) {\n"
       "  if (error instanceof Error) {\n"
       "    return { name: error.name, message: error.message };\n"
       "  }\n"
       "  return { name: \"HostError\", message: String(error) };\n"
       "}\n\n"
       "export const numericPolicy = Object.freeze({\n"
       "  exactInteger: \"BigInt\",\n"
       "  float: \"Number\",\n"
       "  packed: \"Int32Array\",\n"
       "  jsonBoundary: \"schema-checked\"\n"
       "});\n\n"
       "export const capabilityManifest = Object.freeze({\n"
       "  globals: [{ symbol: \"globalThis\", effects: [], capabilities: [], schema: \"opaque-host-global\" }],\n"
       "  imports: []\n"
       "});\n\n"
       "export const componentMetadata = Object.freeze({\n"
       "  enabled: false,\n"
       "  sourceMap: \"gravity-stage0.mjs.map\",\n"
       "  capabilities: []\n"
       "});\n\n"
       "export default Object.freeze({\n"
       "  gravityEntry,\n"
       "  optionFromNullish,\n"
       "  translatePromise,\n"
       "  checkedNumber,\n"
       "  packI32,\n"
       "  numericPolicy,\n"
       "  capabilityManifest,\n"
       "  componentMetadata\n"
       "});\n"
       "//# sourceMappingURL=gravity-stage0.mjs.map\n"))

(def b6-document-ts-declarations
  (str "export type GravityOption<T> =\n"
       "  | Readonly<{ tag: \"none\" }>\n"
       "  | Readonly<{ tag: \"some\"; value: T }>;\n\n"
       "export type GravityResult<T, E> =\n"
       "  | Readonly<{ tag: \"ok\"; value: T }>\n"
       "  | Readonly<{ tag: \"error\"; error: E }>;\n\n"
       "export interface GravityHostError {\n"
       "  readonly name: string;\n"
       "  readonly message: string;\n"
       "}\n\n"
       "export declare class GravityPanic extends Error {\n"
       "  constructor(message: string, cause?: unknown);\n"
       "}\n\n"
       "export declare function gravityEntry(x: bigint): bigint;\n"
       "export declare function optionFromNullish<T>(value: T | null | undefined): GravityOption<T>;\n"
       "export declare function translatePromise<T>(thunk: () => Promise<T> | T): Promise<GravityResult<T, GravityHostError>>;\n"
       "export declare function checkedNumber(value: number): number;\n"
       "export declare function packI32(values: Iterable<number>): Int32Array;\n"
       "export declare const numericPolicy: Readonly<{ exactInteger: \"BigInt\"; float: \"Number\"; packed: \"Int32Array\"; jsonBoundary: \"schema-checked\" }>;\n"
       "export declare const capabilityManifest: Readonly<{ globals: readonly Readonly<{ symbol: \"globalThis\"; effects: readonly []; capabilities: readonly []; schema: \"opaque-host-global\" }>[]; imports: readonly [] }>;\n"
       "export declare const componentMetadata: Readonly<{ enabled: false; sourceMap: \"gravity-stage0.mjs.map\"; capabilities: readonly [] }>;\n"))

(def b6-document-source-map
  (str "{\"version\":3,"
       "\"file\":\"gravity-stage0.mjs\","
       "\"sources\":[\"bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity\"],"
       "\"sourcesContent\":[],"
       "\"names\":[\"GravityPanic\",\"gravityEntry\",\"optionFromNullish\",\"translatePromise\",\"checkedNumber\",\"packI32\"],"
       "\"mappings\":\"\","
       "\"x_gravity_generated_origin\":[\"mir\",\"c14-target-lowering\",\"b1-interface\",\"b6-js-ts-backend\"]}\n"))

(def b6-document-package-json
  (str "{\"name\":\"@gravity/stage0-js-ts\","
       "\"version\":\"0.0.0-stage0\","
       "\"type\":\"module\","
       "\"sideEffects\":false,"
       "\"exports\":{\".\":{\"types\":\"./gravity-stage0.d.ts\","
       "\"import\":\"./gravity-stage0.mjs\"}},"
       "\"files\":[\"gravity-stage0.mjs\","
       "\"gravity-stage0.d.ts\","
       "\"gravity-stage0.mjs.map\"]}\n"))

(defn b6-document-js-structurally-valid?
  [source]
  (and (str/includes? source "export function gravityEntry")
       (str/includes? source "export async function translatePromise")
       (str/includes? source "typeof x !== \"bigint\"")
       (str/includes? source "Int32Array.from")
       (str/includes? source "Object.freeze")
       (str/includes? source "sourceMappingURL=gravity-stage0.mjs.map")
       (not (str/includes? source "eval("))
       (not (str/includes? source "new Function"))))

(defn b6-document-ts-structurally-valid?
  [source]
  (and (str/includes? source "export declare function gravityEntry")
       (str/includes? source "bigint")
       (str/includes? source "GravityOption")
       (str/includes? source "Promise<GravityResult")
       (str/includes? source "Int32Array")
       (str/includes? source "Readonly")))

(defn b6-document-source-map-structurally-valid?
  [source]
  (and (str/includes? source "\"version\":3")
       (str/includes? source "\"file\":\"gravity-stage0.mjs\"")
       (str/includes? source "\"sources\"")
       (str/includes? source "\"x_gravity_generated_origin\"")))

(defn b6-document-package-structurally-valid?
  [source]
  (and (str/includes? source "\"type\":\"module\"")
       (str/includes? source "\"sideEffects\":false")
       (str/includes? source "\"types\":\"./gravity-stage0.d.ts\"")
       (str/includes? source "\"import\":\"./gravity-stage0.mjs\"")))

(defn b6-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b6-js-ts-backend-diagnostic-stream
   :stage :b6-js-ts-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b6-js-ts-backend-document-coverage
            :backend :gravity.backend/js-ts
            :message-key (keyword "backend-js-ts" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b6-document-syntax-" index)
                      :artifact input-id}
            :mir-op (case id
                      "B6-GLOBAL" :host-global-access
                      "B6-IMPORT" :package-import
                      "B6-NULLISH" :host-nullish-boundary
                      "B6-EXCEPTION" :host-exception-boundary
                      "B6-NUMERIC" :numeric-lowering
                      "B6-ASYNC" :async-host-boundary
                      :host-call)
            :domain-anchor (when (= id "B6-UI") :ui-component)
            :runtime :browser
            :module-format :esm
            :ecmascript :es2022
            :host-symbol (b6-document-host-symbol id)
            :package-id (when (= id "B6-IMPORT") "@gravity/stage0")
            :missing-effect-capability-schema-fact
            (b6-document-missing-fact id)
            :selected-adapter-or-rejection (b6-document-selected-adapter id)
            :fallback-status :rejected
            :facts {:ambient-global-policy :reject-without-capability
                    :nullish-policy :option-result-checked-or-opaque
                    :exception-policy :translate
                    :dynamic-code-policy :denied
                    :numeric-policy :manifested}
            :remediation [{:kind :pin-js-runtime-target}
                          {:kind :declare-host-global-or-import}
                          {:kind :emit-type-declaration-source-map-and-boundary-map}]
            :redactions []
            :ordering-key [id :b6-js-ts-backend-document-coverage
                           :browser-esm]})
         b6-document-diagnostic-ids
         (range))
   :status :complete})