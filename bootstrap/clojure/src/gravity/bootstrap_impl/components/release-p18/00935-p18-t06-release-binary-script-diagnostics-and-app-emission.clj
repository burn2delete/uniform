(defn- __gravity_bootstrap_p18_t06_diagnostics_and_app_emission []
  (str
    "emit_diagnostic() {\n"
    "  local diag=\"$1\"\n"
    "  printf '{:id \"%s\", :message \"final release command rejected source\", :phase \"P18-T06\", :release-binary-path \"%s\"}\\n' \"$diag\" \"$release_binary_path\" >&2\n"
    "  exit 1\n"
    "}\n"
    "\n"
    "source_extension_is_supported() {\n"
    "  case \"${1:-}\" in\n"
    "    *.gravity|*.qst) return 0 ;;\n"
    "    *) emit_diagnostic 'P18T06004' ;;\n"
    "  esac\n"
    "}\n"
    "\n"))
