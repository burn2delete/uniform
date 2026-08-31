(defn p18-t06-release-binary-script []
  (str
    (__gravity_bootstrap_p18_t06_prologue_and_source_identity)
    (__gravity_bootstrap_p18_t06_compiler_fixture_dispatch)
    (__gravity_bootstrap_p18_t06_backend_fixture_dispatch)
    (__gravity_bootstrap_p18_t06_runtime_fixture_dispatch)
    (__gravity_bootstrap_p18_t06_diagnostics_and_app_emission)
    (__gravity_bootstrap_p18_t06_command_dispatch)))
