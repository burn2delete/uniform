(defn- __gravity_bootstrap_p18_t06_compiler_fixture_dispatch []
  (str
    "bootstrap_check_source() {\n"
    "  local source=\"$1\"\n"
    "  case \"$source\" in\n"
    "    *.gravity|*.qst) ;;\n"
    "    *) emit_diagnostic 'P18T06004' ;;\n"
    "  esac\n"
    "  case \"$source\" in\n"
    "    examples/*.gravity|examples/*.qst|\"$root_dir\"/examples/*.gravity|\"$root_dir\"/examples/*.qst)\n"
    "      (cd \"$root_dir\" && bin/gravity check \"$source\")\n"
    "      return ;;\n"
    "  esac\n"
    "\n"))
