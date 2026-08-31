

(def provider-specs
  {:io/stdout {:provider 'gravity.io/stdout-host
               :version "fixture-1"
               :profiles #{:hosted :native :ai}}
   :memory/allocator {:provider 'gravity.memory/gc-host
                      :version "fixture-1"
                      :profiles #{:hosted :native}}
   :resource/file {:provider 'gravity.resource/file-host
                   :version "fixture-1"
                   :profiles #{:hosted :native}}
   :scheduler/task {:provider 'gravity.scheduler/structured-host
                    :version "fixture-1"
                    :profiles #{:hosted :native :distributed :ai}}
   :memory/raw {:provider 'gravity.memory/raw-unsafe
                :version "fixture-1"
                :profiles #{:native :kernel}}
   :hardware/mmio {:provider 'gravity.hardware/mmio-audited
                   :version "fixture-1"
                   :profiles #{:native :kernel :firmware :hardware}}
   :hardware/interrupt {:provider 'gravity.hardware/interrupt-audited
                        :version "fixture-1"
                        :profiles #{:native :kernel :firmware :hardware}}
   :time/read {:provider 'gravity.time/runtime-clock
               :version "fixture-1"
               :profiles #{:hosted :native :distributed :ai}}
   :filesystem/read {:provider 'gravity.fs/read-scoped
                     :version "fixture-1"
                     :profiles #{:hosted :native :distributed :ai}}
   :filesystem/write {:provider 'gravity.fs/write-scoped
                      :version "fixture-1"
                      :profiles #{:hosted :native :distributed :ai}}
   :network/client {:provider 'gravity.net/http-client
                    :version "fixture-1"
                    :profiles #{:hosted :native :distributed :ai}}
   :network/listener {:provider 'gravity.net/listener
                      :version "fixture-1"
                      :profiles #{:hosted :native}}
   :database/read {:provider 'gravity.db/read-scoped
                   :version "fixture-1"
                   :profiles #{:hosted :native :distributed :ai}}
   :database/write {:provider 'gravity.db/write-scoped
                    :version "fixture-1"
                    :profiles #{:hosted :native :distributed :ai}}
   :random/read {:provider 'gravity.random/secure
                 :version "fixture-1"
                 :profiles #{:hosted :native :distributed :ai}}
   :ffi/call {:provider 'gravity.ffi/audited
              :version "fixture-1"
              :profiles #{:native :hosted}}
   :reflection/use {:provider 'gravity.host/reflection
                    :version "fixture-1"
                    :profiles #{:hosted :meta}}
   :compiler/ir {:provider 'gravity.compiler/ir-store
                 :version "fixture-1"
                 :profiles #{:meta :hosted}}
   :compiler/plugin {:provider 'gravity.compiler/plugin-host
                     :version "fixture-1"
                     :profiles #{:meta :hosted}}
   :secrets/read {:provider 'gravity.secrets/scoped
                  :version "fixture-1"
                  :profiles #{:hosted :native :distributed :ai}}
   :shell/exec {:provider 'gravity.shell/audited
                :version "fixture-1"
                :profiles #{:hosted :native}}
   :workflow/event {:provider 'gravity.workflow/events
                    :version "fixture-1"
                    :profiles #{:distributed :ai :hosted}}
   :workflow/replay {:provider 'gravity.workflow/replay
                     :version "fixture-1"
                     :profiles #{:distributed :ai :hosted}}
   :ai/model {:provider 'gravity.ai/model-provider
              :version "fixture-1"
              :profiles #{:ai :hosted}}
   :ai/tool {:provider 'gravity.ai/tool-provider
             :version "fixture-1"
             :profiles #{:ai :hosted}}
   :ai/memory {:provider 'gravity.ai/memory-store
               :version "fixture-1"
               :profiles #{:ai :hosted}}
   :ai/eval {:provider 'gravity.ai/eval-runner
             :version "fixture-1"
             :profiles #{:ai :hosted}}
   :ai/human-review {:provider 'gravity.ai/human-review
                     :version "fixture-1"
                     :profiles #{:ai :hosted}}
   :test/fixture {:provider 'gravity.test/effect-fixture
                  :version "fixture-1"
                  :profiles #{:hosted :ai :distributed}}})

(def effect-registry
  {:pure {:family :pure :kind :pure :requires-capability false :profiles known-source-profiles}
   :memory/allocate {:family :memory-allocation :kind :memory :requires-capability true :capability :memory/allocator :profiles #{:hosted :native :ai}}
   :memory/free {:family :memory-allocation :kind :memory :requires-capability true :capability :memory/allocator :profiles #{:native :hosted}}
   :memory/raw {:family :raw-memory :kind :unsafe-island :requires-capability true :capability :memory/raw :profiles #{:native :kernel}}
   :memory/mmio {:family :mmio :kind :unsafe-island :requires-capability true :capability :hardware/mmio :profiles #{:native :kernel :firmware :hardware}}
   :interrupt/register {:family :interrupt :kind :platform :requires-capability true :capability :hardware/interrupt :profiles #{:kernel :firmware :hardware :native}}
   :resource/open {:family :resource :kind :external :requires-capability true :capability :resource/file :profiles #{:hosted :native}}
   :resource/close {:family :resource :kind :external :requires-capability true :capability :resource/file :profiles #{:hosted :native}}
   :io/read {:family :io :kind :external :requires-capability true :profiles #{:hosted :native}}
   :io/write {:family :io :kind :external :requires-capability true :capability :io/stdout :profiles #{:hosted :native}}
   :filesystem/read {:family :filesystem :kind :external :requires-capability true :capability :filesystem/read :nondeterministic true :replay-record true :profiles #{:hosted :native :distributed :ai}}
   :filesystem/write {:family :filesystem :kind :external :requires-capability true :capability :filesystem/write :profiles #{:hosted :native :distributed :ai}}
   :network/http {:family :network :kind :external :requires-capability true :capability :network/client :nondeterministic true :replay-record true :profiles #{:hosted :native :distributed :ai}}
   :network/listen {:family :network :kind :external :requires-capability true :capability :network/listener :profiles #{:hosted :native}}
   :database/read {:family :database :kind :external :requires-capability true :capability :database/read :nondeterministic true :replay-record true :profiles #{:hosted :native :distributed :ai}}
   :database/write {:family :database :kind :external :requires-capability true :capability :database/write :profiles #{:hosted :native :distributed :ai}}
   :time/read {:family :time :kind :nondeterministic :requires-capability true :capability :time/read :nondeterministic true :replay-record true :profiles #{:hosted :native :distributed :ai}}
   :time/schedule {:family :time :kind :runtime :requires-capability true :capability :scheduler/task :nondeterministic true :profiles #{:hosted :native :distributed :ai}}
   :random/read {:family :random :kind :nondeterministic :requires-capability true :capability :random/read :nondeterministic true :replay-record true :profiles #{:hosted :native :distributed :ai}}
   :async/suspend {:family :async :kind :control :requires-capability true :capability :scheduler/task :profiles #{:hosted :native :distributed :ai}}
   :async/await {:family :async :kind :control :requires-capability true :capability :scheduler/task :profiles #{:hosted :native :distributed :ai}}
   :generator/yield {:family :generator :kind :control :requires-capability false :profiles #{:hosted :native :distributed :ai}}
   :thread/spawn {:family :thread :kind :concurrency :requires-capability true :capability :scheduler/task :profiles #{:hosted :native :distributed :ai}}
   :sync/block {:family :sync :kind :concurrency :requires-capability false :profiles #{:hosted :native}}
   :error/throw {:family :error :kind :control :requires-capability false :profiles known-source-profiles}
   :error/resume {:family :resumable-error :kind :control :requires-capability false :profiles #{:hosted :native :distributed :ai}}
   :panic/fail {:family :panic :kind :control :requires-capability false :profiles #{:hosted :native :kernel :firmware :distributed :ai}}
   :safety/check-failure {:family :safety-check :kind :runtime :requires-capability false :profiles known-source-profiles}
   :host/error {:family :host-error :kind :host :requires-capability false :profiles #{:hosted :ai}}
   :ffi/error {:family :ffi-error :kind :external :requires-capability true :capability :ffi/call :profiles #{:native :hosted}}
   :workflow/failure {:family :workflow-error :kind :workflow :requires-capability true :capability :workflow/event :profiles #{:distributed :ai :hosted}}
   :ai/error {:family :ai-error :kind :ai :requires-capability true :capability :ai/tool :profiles #{:ai :hosted}}
   :ffi/call {:family :ffi :kind :unsafe-island :requires-capability true :capability :ffi/call :profiles #{:native :hosted}}
   :reflection/use {:family :reflection :kind :host :requires-capability true :capability :reflection/use :profiles #{:hosted :meta}}
   :dynamic/eval {:family :dynamic :kind :host :requires-capability false :profiles #{:hosted :meta :ai}}
   :compiler/read-ir {:family :compiler :kind :meta :requires-capability true :capability :compiler/ir :profiles #{:meta :hosted}}
   :compiler/write-ir {:family :compiler :kind :meta :requires-capability true :capability :compiler/ir :profiles #{:meta :hosted}}
   :compiler/plugin {:family :compiler :kind :meta :requires-capability true :capability :compiler/plugin :profiles #{:meta :hosted}}
   :build/read-file {:family :build :kind :build :requires-build-grant true}
   :build/write-artifact {:family :build :kind :build :requires-build-grant true}
   :build/env {:family :build :kind :build :requires-build-grant true}
   :build/network {:family :build :kind :build :requires-build-grant true}
   :build/exec {:family :build :kind :build :requires-build-grant true}
   :build/time {:family :build :kind :build :requires-build-grant true}
   :build/random {:family :build :kind :build :requires-build-grant true}
   :build/model-call {:family :build :kind :build :requires-build-grant true}
   :build/tool-call {:family :build :kind :build :requires-build-grant true}
   :build/target-probe {:family :build :kind :build :requires-build-grant true}
   :build/package-index {:family :build :kind :build :requires-build-grant true}
   :secrets/read {:family :secrets :kind :external :requires-capability true :capability :secrets/read :profiles #{:hosted :native :distributed :ai}}
   :shell/exec {:family :shell :kind :external :requires-capability true :capability :shell/exec :profiles #{:hosted :native}}
   :workflow/event {:family :workflow :kind :workflow :requires-capability true :capability :workflow/event :nondeterministic true :replay-record true :profiles #{:distributed :ai :hosted}}
   :workflow/replay {:family :workflow :kind :workflow :requires-capability true :capability :workflow/replay :replay-record true :profiles #{:distributed :ai :hosted}}
   :ai/model-call {:family :ai :kind :ai :requires-capability true :capability :ai/model :nondeterministic true :replay-record true :profiles #{:ai :hosted}}
   :ai/tool-call {:family :ai :kind :ai :requires-capability true :capability :ai/tool :nondeterministic true :replay-record true :profiles #{:ai :hosted}}
   :ai/embedding {:family :ai :kind :ai :requires-capability true :capability :ai/model :nondeterministic true :replay-record true :profiles #{:ai :hosted}}
   :ai/memory-read {:family :ai :kind :ai :requires-capability true :capability :ai/memory :profiles #{:ai :hosted}}
   :ai/memory-write {:family :ai :kind :ai :requires-capability true :capability :ai/memory :profiles #{:ai :hosted}}
   :ai/prompt-render {:family :ai :kind :ai :requires-capability false :profiles #{:ai :hosted}}
   :ai/output-validate {:family :ai :kind :ai :requires-capability false :profiles #{:ai :hosted}}
   :ai/eval-run {:family :ai :kind :ai :requires-capability true :capability :ai/eval :profiles #{:ai :hosted}}
   :ai/human-review {:family :ai :kind :ai :requires-capability true :capability :ai/human-review :nondeterministic true :replay-record true :profiles #{:ai :hosted}}
   :control/recur {:family :control :kind :control :requires-capability false :profiles known-source-profiles}
   :state/write {:family :state :kind :state :requires-capability false :profiles #{:hosted :native :meta}}})