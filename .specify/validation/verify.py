#!/usr/bin/env python3
"""Lightweight Spec Kit smoke checks; no compiler, network or model dispatch."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--cli', type=Path, help='Optional evaluated specify executable')
    parser.add_argument('--output', type=Path, help='Write JSON observations to this path')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    observations = []

    def record(name, **details):
        observations.append({'check': name, 'status': 'passed', **details})

    pin = json.loads((root / '.specify/upstream.json').read_text())
    assert pin['commit'] == '5ea8b1cf0a38699340a38e00d02770308997d8c0'
    config = json.loads((root / '.specify/integration.json').read_text())
    assert config['installed_integrations'] == ['codex']
    assert config['default_integration'] == 'codex'
    assert config['integration_settings']['codex']['parsed_options']['events'] == 'false'
    assert json.loads((root / '.specify/workflows/workflow-registry.json').read_text())['workflows'] == {}
    assert not (root / '.specify/extensions.yml').exists()
    assert not (root / '.codex').exists()
    assert 'feature.json' in (root / '.specify/.gitignore').read_text()
    record('pin-and-inert-configuration', release=pin['release'], commit=pin['commit'])

    all_files = [p for base in ['.specify', '.agents', 'specs/001-paired-source-core']
                 for p in (root / base).rglob('*') if p.is_file() and p.suffix != '.pyc']
    for path in all_files:
        path.read_text(encoding='ascii')
        if path.suffix == '.json' or path.name == '.registry':
            json.loads(path.read_text())
    record('ascii-and-json', files=len(all_files))

    for path in (root / '.specify/local-preset').rglob('*'):
        if path.is_file():
            installed = root / '.specify/presets/uniform' / path.relative_to(root / '.specify/local-preset')
            assert installed.read_bytes() == path.read_bytes(), str(installed)
    for path in (root / '.specify/local-preset/commands').glob('*.md'):
        command = path.stem.removeprefix('speckit.')
        skill = root / f'.agents/skills/speckit-{command}/SKILL.md'
        body = path.read_text().split('---', 2)[2].strip()
        assert body in skill.read_text(), str(skill)
        assert f'name: speckit-{command}\n' in skill.read_text()
        assert 'source: preset:uniform' in skill.read_text()
        assert '__SPECKIT_COMMAND_' not in skill.read_text()
    record('installed-preset-and-generated-skills', commands=10)

    for base in ['.specify/README.md', '.specify/memory/constitution.md',
                 'specs/001-paired-source-core/spec.md', 'specs/001-paired-source-core/plan.md',
                 'specs/001-paired-source-core/tasks.md', 'specs/001-paired-source-core/evidence.md']:
        path = root / base
        for target in re.findall(r'\]\(([^)]+)\)', path.read_text()):
            if '://' in target or target.startswith('#'):
                continue
            assert (path.parent / target.split('#')[0]).exists(), (base, target)
    record('local-document-links')

    def run(command, cwd, env=None, expected=0):
        result = subprocess.run(command, cwd=cwd, env=env, text=True,
                                capture_output=True, timeout=30)
        assert result.returncode == expected, (command, result.returncode, result.stdout, result.stderr)
        return result

    for script in (root / '.specify/scripts/bash').glob('*.sh'):
        run(['bash', '-n', str(script)], root)
    record('bash-syntax', scripts=6)

    with tempfile.TemporaryDirectory(prefix='uniform-speckit-smoke-') as tmp:
        scratch = Path(tmp)
        shutil.copytree(root / '.specify', scratch / '.specify',
                        ignore=shutil.ignore_patterns('__pycache__', 'feature.json'))
        shutil.copytree(root / '.agents', scratch / '.agents')
        env = dict(os.environ, SPECIFY_INIT_DIR=str(scratch), SPECIFY_FEATURE_NO_PERSIST='1')
        env.pop('SPECIFY_FEATURE', None)
        env.pop('SPECIFY_FEATURE_DIRECTORY', None)
        script_dir = scratch / '.specify/scripts/bash'

        def helper(name, *options, expected=0):
            return run(['bash', str(script_dir / name), *options], scratch, env, expected)

        create_args = ['--json', '--number', '1', '--short-name', 'smoke-core', 'Smoke source Core work artifacts']
        dry = json.loads(helper('create-new-feature.sh', '--dry-run', *create_args).stdout)
        assert dry['DRY_RUN'] and not (scratch / 'specs').exists()
        created = json.loads(helper('create-new-feature.sh', *create_args).stdout)
        feature = Path(created['SPEC_FILE']).parent
        assert (feature / 'spec.md').read_bytes() == (root / '.specify/templates/overrides/spec-template.md').read_bytes()
        assert not (scratch / '.specify/feature.json').exists()
        env['SPECIFY_FEATURE_DIRECTORY'] = str(feature)
        helper('setup-plan.sh', '--unknown-option', expected=1)
        helper('setup-plan.sh', '--json')
        assert (feature / 'plan.md').read_bytes() == (root / '.specify/templates/overrides/plan-template.md').read_bytes()
        (feature / 'plan.md').write_text('Reviewed existing plan\n')
        helper('setup-plan.sh', '--json')
        assert (feature / 'plan.md').read_text() == 'Reviewed existing plan\n'
        tasks = json.loads(helper('setup-tasks.sh', '--json').stdout)
        assert tasks['TASKS_TEMPLATE_CONTENT'] == (root / '.specify/templates/overrides/tasks-template.md').read_text()
        helper('check-prerequisites.sh', '--json', '--require-tasks', expected=1)
        (feature / 'tasks.md').write_text('- [ ] T001 Inspect existing Core evidence\n')
        helper('check-prerequisites.sh', '--json', '--require-tasks', '--include-tasks')
        (feature / 'plan.md').unlink()
        helper('setup-tasks.sh', '--json', expected=1)
        env['SPECIFY_FEATURE_DIRECTORY'] = str(scratch / 'specs/absent')
        helper('check-prerequisites.sh', '--json', expected=1)
        record('actual-helper-flow-and-rejections', creation='success', overrides='spec/plan/tasks',
               preserved_existing_plan=True, missing_tasks='rejected', missing_plan='rejected',
               missing_feature='rejected', unknown_option='rejected', pointer_persisted=False)

        # Compare source-only ASCII locale probe with the upstream byte sequence.
        probe = run(['bash', '-c', "printf '\\303\\251\\343\\200\\202'"], scratch)
        assert probe.stdout.encode() == bytes.fromhex('c3a9e38082')
        record('ascii-locale-probe-byte-equivalence')

        if args.cli:
            cli = str(args.cli.resolve())
            version = run([cli, 'version'], scratch).stdout
            assert '1.2.3' in version
            status = run([cli, 'integration', 'status'], scratch).stdout
            assert 'Missing managed files: 0' in status
            assert 'Invalid manifest paths: 0' in status
            assert 'Unchecked manifests: 0' in status
            presets = json.loads(run([cli, 'preset', 'list', '--json'], scratch).stdout)
            assert len(presets) == 1 and presets[0]['id'] == 'uniform' and presets[0]['enabled']
            assert presets[0]['provides']['commands'] == 10
            workflows = run([cli, 'workflow', 'list'], scratch).stdout
            extensions = run([cli, 'extension', 'list'], scratch).stdout
            assert 'No workflows installed' in workflows and 'No extensions installed' in extensions
            record('actual-cli-inventory', version='1.2.3', presets=['uniform'],
                   status=status.strip(), workflows=[], extensions=[])
            # Regenerate from reviewed source with official supported preset update,
            # in scratch only. Compare generated results, not timestamp metadata.
            before = {p.relative_to(scratch): p.read_bytes() for p in (scratch / '.agents').rglob('SKILL.md')}
            run([cli, 'preset', 'update', 'uniform', '--dev', '.specify/local-preset'], scratch)
            for path, content in before.items():
                assert (scratch / path).read_bytes() == content, str(path)
            record('official-preset-regeneration', skills_byte_identical=10)

    # Bind the main executable/source inputs; exclude this results file itself.
    inputs = {}
    for base in ['.agents', '.specify/scripts', '.specify/local-preset', '.specify/templates/overrides']:
        for path in sorted((root / base).rglob('*')):
            if path.is_file():
                inputs[str(path.relative_to(root))] = hashlib.sha256(path.read_bytes()).hexdigest()
    report = {'scope': 'Spec Kit scaffold only; no compiler or model execution',
              'upstream_commit': pin['commit'], 'observations': observations, 'input_sha256': inputs,
              'unrun': ['fresh Codex skill discovery/chat dispatch', 'compiler/JVM tests',
                        'PR356 raw log authentication', 'review/integration/merge']}
    if args.output:
        args.output.write_text(json.dumps(report, indent=2, ensure_ascii=True) + '\n')
    print(f'validation passed: {len(observations)} Spec Kit scaffold checks; compiler/model unrun')


if __name__ == '__main__':
    main()
