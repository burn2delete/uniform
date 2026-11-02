(defn- __gravity_bootstrap_p18_t06_compiler_fixture_dispatch []
  (str
    "bootstrap_check_source() {\n"
    "  local source=\"$1\"\n"
    "  case \"$source\" in\n"
    "    *.gravity|*.qst) ;;\n"
    "    *) emit_diagnostic 'P18T06004' ;;\n"
    "  esac\n"
    "\n"))
