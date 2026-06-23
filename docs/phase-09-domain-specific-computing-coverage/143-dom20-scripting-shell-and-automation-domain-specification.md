# DOM20 - Scripting, Shell and Automation Domain Specification

Sequence: 143
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover scripting, shell, and automation
slices normally written in Bash, Python, Ruby, PowerShell, JavaScript, Make,
YAML workflows, or CI/CD task DSLs.

The replacement scope is typed scripts, CLI wrappers, argument schemas, command
schemas, file transforms, deployment automation, repo maintenance, data import,
dry-run plans, audit logs, and hermetic build actions.

## Requirements

- Scripts must declare arguments, environment inputs, filesystem roots, process
  execution, shell commands, network effects, secrets, and deployment authority.
- Shell/process execution requires command schemas, quoting/escaping policy,
  taint validation, timeout, exit-code mapping, and human-review when required.
- String-built commands using tainted input are rejected.
- Build automation must declare hermeticity or record non-hermetic inputs.
- File operations must be scoped to granted roots and path schemas.
- Dry-run and audit modes must record planned writes, deletes, deployments, and
  shell commands.

## Dependencies

- `P4`, `P3`, `SAFE10`, `SAFE11`, `R9`, `R11`, and `R12` define hosted/meta
  execution, capabilities, taint, interactive runtime, authority, and
  observability.
- Package/build phases define hermeticity and deployment policy.

## Outputs and Artifacts

- Scripting domain manifest.
- Script package.
- Argument schema.
- Command schema.
- Capability policy.
- Dry-run plan.
- Task/audit log.
- Hermeticity record.
- Shell human-review record when required.
- Scripting diagnostics.

## Domain Manifest

```clojure
{:domain :scripting-shell-automation
 :profiles #{:hosted :meta}
 :backends #{:javascript-typescript :jvm :wasm}
 :artifacts #{:script-package :argument-schema :command-schema
              :capability-policy :task-log}
 :examples #{:file-transform :deploy-script :repo-maintenance :data-import}
 :rejects #{:shell-injection :ambient-filesystem :unhermetic-build-action
            :destructive-command-without-human-review}}
```

## Replacement Scope

Gravity should replace:

- shell scripts,
- CI tasks,
- deployment scripts,
- file transforms,
- repo maintenance automation,
- import/export tasks,
- typed CLI tools,
- dry-run and audit wrappers.

External commands remain process provider boundaries with schemas.

## Minimum End-to-End Slice

The first complete slice is a file transform script:

- Gravity source declares args, input/output roots, file schema, and write
  capability.
- Runtime capability system grants only the declared roots.
- Dry-run artifact records planned writes.
- Negative fixture rejects tainted shell command construction.
- Audit log records actual file changes and source spans.

## Diagnostics

Scripting diagnostics use `DOM20` identifiers:

- `DOM20-ARGS` for missing argument schemas.
- `DOM20-FILESYSTEM` for file access outside granted roots.
- `DOM20-SHELL` for shell/process execution without command schema or grant.
- `DOM20-TAINT` for tainted input in commands, paths, or trusted sinks.
- `DOM20-DESTRUCTIVE` for delete/deploy/write actions without human-review policy.
- `DOM20-HERMETICITY` for unrecorded env/network/filesystem build inputs.
- `DOM20-SECRET` for unsafe secret use or logging.
- `DOM20-AUDIT` for missing dry-run or task log evidence.

Diagnostics must include script id, source span, argument/path/command id,
effect, capability, taint category, human-review requirement, and remediation.

## Rejected Designs

Gravity rejects ambient shell authority.

Gravity rejects string-built commands with tainted input.

Gravity rejects filesystem access outside declared roots.

Gravity rejects unhermetic build automation without records.

Gravity rejects destructive automation without human-review policy when required.

## Conformance Criteria

A conforming scripting slice must demonstrate:

- file transform, repo maintenance, and deployment examples,
- argument, path, and command schemas,
- capability enforcement for filesystem/process/network/secrets,
- taint rejection for shell injection,
- dry-run and audit logs,
- hermeticity records,
- human-review fixtures for destructive actions.
