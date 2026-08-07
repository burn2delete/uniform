#!/usr/bin/env python3
"""Render and query the Gravity project-structure contract.

This tool is a read-only view over ``contracts/project-structure.json``.  It
imports :mod:`validate_project_structure` and refuses to render a manifest
until that validator accepts it.  The renderer does not write artifacts,
change ownership, grant authority, or promote a slice's status.  Its default
output is stable JSON on stdout.
"""

from __future__ import annotations

import argparse
import copy
import fnmatch
import json
from pathlib import Path
import re
import sys
from typing import Any, Iterable, Mapping, Sequence

try:
    # Script execution puts ``tools`` on sys.path.  Tests import this module
    # from the same directory, so keep the validator import direct and clear.
    import validate_project_structure as validator
except ImportError as exc:  # pragma: no cover - only reachable from bad setup
    raise RuntimeError("project-structure renderer requires tools/validate_project_structure.py") from exc


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = validator.DEFAULT_MANIFEST


class RenderError(ValueError):
    """Raised when a manifest cannot be rendered safely."""


def _sorted_unique(values: Iterable[str]) -> list[str]:
    return sorted(set(values))


def _normalise_changed_path(path: Any) -> str:
    if not isinstance(path, str):
        raise RenderError(f"changed path must be a string, got {type(path).__name__}")
    if not path:
        raise RenderError("changed path must not be empty")
    if "\x00" in path:
        raise RenderError("changed path must not contain NUL")
    if path.startswith("/") or re.match(r"^[A-Za-z]:", path):
        raise RenderError(f"changed path must be repository-relative: {path!r}")
    if "\\" in path:
        raise RenderError("changed path must use '/' separators")
    if any(operator in path for operator in "*?[]{}"):
        raise RenderError(f"changed path must be concrete, not a pattern: {path!r}")
    if "//" in path:
        raise RenderError(f"changed path must not contain repeated separators: {path!r}")
    parts = path.split("/")
    if any(part in ("", ".", "..") for part in parts):
        raise RenderError(
            f"changed path must be a normalized concrete repository-relative path: {path!r}"
        )
    return path


def _require_valid_manifest(manifest: Any) -> Mapping[str, Any]:
    """Validate a loaded manifest, failing closed on every validator error."""

    if not isinstance(manifest, Mapping):
        raise RenderError("project structure manifest must be a JSON object")
    errors = validator.validate_manifest(manifest)
    if errors:
        detail = "\n".join(f"- {error}" for error in errors)
        raise RenderError(f"project structure manifest validation failed:\n{detail}")
    # Queries never mutate the caller's object.  A shallow mapping contract is
    # enough for reads; report builders copy values before returning them.
    return manifest


def load_validated_manifest(path: str | Path = DEFAULT_MANIFEST) -> Mapping[str, Any]:
    """Load and validate a project-structure manifest before any rendering."""

    try:
        manifest = validator.load_manifest(Path(path))
    except validator.ManifestError as exc:
        raise RenderError(str(exc)) from exc
    return _require_valid_manifest(manifest)


def render_summary(manifest: Mapping[str, Any]) -> dict[str, Any]:
    """Return deterministic counts and identity metadata from a valid manifest."""

    manifest = _require_valid_manifest(manifest)
    passes = list(manifest.get("canonical_passes", ()))
    slices = list(manifest.get("slices", ()))
    artifacts = list(manifest.get("artifacts", ()))
    ownership = manifest.get("ownership", {})
    path_policy = manifest.get("path_policy", {})
    status_counts: dict[str, int] = {}
    for item in slices:
        status = item.get("status")
        if isinstance(status, str):
            status_counts[status] = status_counts.get(status, 0) + 1
    owner_category_counts: dict[str, int] = {}
    for item in ownership.get("owners", ()):
        category = item.get("category")
        if isinstance(category, str):
            owner_category_counts[category] = owner_category_counts.get(category, 0) + 1
    return {
        "manifest_id": manifest.get("manifest_id"),
        "schema_version": manifest.get("schema_version"),
        "canonical_pass_count": len(passes),
        "slice_count": len(slices),
        "artifact_count": len(artifacts),
        "owner_count": len(ownership.get("owners", ())),
        "path_policy_count": len(path_policy.get("policies", ())),
        "module_path_count": len(ownership.get("module_paths", {})),
        "slice_status_counts": dict(sorted(status_counts.items())),
        "owner_category_counts": dict(sorted(owner_category_counts.items())),
    }


def canonical_pass_table(manifest: Mapping[str, Any]) -> list[dict[str, Any]]:
    """Return the canonical D1 pass table in manifest order.

    The table keeps pass-contract fields useful for coordination while
    retaining the source values exactly.  It is a view, not an authority
    decision and not a replacement for the manifest validator.
    """

    manifest = _require_valid_manifest(manifest)
    fields = (
        "id",
        "name",
        "order",
        "depends_on",
        "input_artifacts",
        "output_artifacts",
        "requires",
        "preserves",
        "invalidates",
        "emits",
        "rejects",
        "owner",
        "cost",
        "authority",
    )
    rows: list[dict[str, Any]] = []
    for item in manifest.get("canonical_passes", ()):
        rows.append({field: copy.deepcopy(item[field]) for field in fields})
    return rows


def slice_topological_waves(
    manifest: Mapping[str, Any], slice_ids: Iterable[str] | None = None
) -> list[dict[str, Any]]:
    """Return deterministic Kahn waves for the selected or complete slice DAG.

    A wave contains every currently-ready slice, sorted by id.  Dependencies
    are interpreted in the manifest's declared direction (``depends_on``),
    and a cycle raises rather than producing a partial or misleading view.
    """

    manifest = _require_valid_manifest(manifest)
    slices = {
        item["id"]: item for item in manifest.get("slices", ()) if isinstance(item, Mapping)
    }
    if slice_ids is None:
        selected = set(slices)
    else:
        if isinstance(slice_ids, str):
            slice_ids = [slice_ids]
        selected = _sorted_unique(slice_ids)
        unknown = sorted(set(selected).difference(slices))
        if unknown:
            raise RenderError("unknown slice ids: " + ", ".join(unknown))
        selected = set(selected)

    # Include dependencies needed to make a selected slice's ordering
    # meaningful, then report all selected/dependency nodes in stable waves.
    closure = set(selected)
    pending = list(selected)
    while pending:
        current = pending.pop()
        for dependency in slices[current].get("depends_on", ()):
            if dependency not in slices:
                raise RenderError(f"slice {current!r} depends on unknown slice {dependency!r}")
            if dependency not in closure:
                closure.add(dependency)
                pending.append(dependency)

    remaining = set(closure)
    completed: set[str] = set()
    waves: list[dict[str, Any]] = []
    while remaining:
        ready = sorted(
            item_id
            for item_id in remaining
            if set(slices[item_id].get("depends_on", ())).issubset(completed)
        )
        if not ready:
            raise RenderError("slice dependency graph contains a cycle")
        waves.append({"wave": len(waves), "slices": ready})
        completed.update(ready)
        remaining.difference_update(ready)
    return waves


def _path_pattern_matches(pattern: str, path: str) -> bool:
    """Match a validated manifest pattern without validator-private APIs.

    Exact paths compare literally, a trailing slash reserves a directory, and
    the manifest's glob subset uses case-sensitive matching within each path
    segment.  Wildcards cannot consume ``/``; explicit directory reservations
    still include concrete descendants beneath the matched directory.
    """

    brace = re.search(r"\{([^{}]+)\}", pattern)
    if brace is not None:
        alternatives = brace.group(1).split(",")
        if len(alternatives) > 1:
            return any(
                _path_pattern_matches(
                    pattern[: brace.start()] + alternative + pattern[brace.end() :], path
                )
                for alternative in alternatives
            )
    directory_reservation = pattern.endswith("/")
    pattern_segments = pattern.rstrip("/").split("/")
    path_segments = path.split("/")
    if directory_reservation:
        # A reservation names descendants, not the directory marker itself.
        if len(path_segments) <= len(pattern_segments):
            return False
        path_segments = path_segments[: len(pattern_segments)]
    elif len(path_segments) != len(pattern_segments):
        return False
    return all(
        fnmatch.fnmatchcase(path_segment, pattern_segment)
        for pattern_segment, path_segment in zip(pattern_segments, path_segments)
    )


def _policy_matches_path(policy: Mapping[str, Any], path: str) -> bool:
    return any(
        _path_pattern_matches(pattern, path)
        for pattern in policy.get("patterns", ())
        if isinstance(pattern, str)
    )


def owner_path_view(
    manifest: Mapping[str, Any], paths: Iterable[str] | None = None
) -> dict[str, Any]:
    """Return owners, path policies, module paths, and explicit unowned paths."""

    manifest = _require_valid_manifest(manifest)
    ownership = manifest.get("ownership", {})
    policies = {
        item["id"]: item
        for item in manifest.get("path_policy", {}).get("policies", ())
        if isinstance(item, Mapping) and isinstance(item.get("id"), str)
    }
    module_paths = ownership.get("module_paths", {})
    if isinstance(paths, str):
        paths = [paths]
    path_values = [] if paths is None else [_normalise_changed_path(path) for path in paths]
    path_values = _sorted_unique(path_values)

    policy_rows: list[dict[str, Any]] = []
    for policy_id in sorted(policies):
        policy = policies[policy_id]
        policy_rows.append(
            {
                "id": policy_id,
                "kind": policy.get("kind"),
                "owner": policy.get("owner"),
                "patterns": sorted(policy.get("patterns", ())),
                "editable": policy.get("editable"),
                "review_required": policy.get("review_required"),
            }
        )

    owner_rows: list[dict[str, Any]] = []
    for owner in sorted(ownership.get("owners", ()), key=lambda item: item.get("id", "")):
        owner_id = owner.get("id")
        owner_policy_ids = sorted(
            policy_id
            for policy_id, policy in policies.items()
            if policy.get("owner") == owner_id
        )
        owner_module_paths = sorted(
            path for path, path_owner in module_paths.items() if path_owner == owner_id
        )
        owner_rows.append(
            {
                "id": owner_id,
                "category": owner.get("category"),
                "exclusive": owner.get("exclusive"),
                "path_policy_ids": owner_policy_ids,
                "module_paths": owner_module_paths,
            }
        )

    path_matches: list[dict[str, Any]] = []
    unowned_paths: list[str] = []
    for path in path_values:
        matching_policy_ids = sorted(
            policy_id for policy_id, policy in policies.items() if _policy_matches_path(policy, path)
        )
        matching_owners = sorted(
            {
                policies[policy_id].get("owner")
                for policy_id in matching_policy_ids
                if isinstance(policies[policy_id].get("owner"), str)
            }
        )
        module_owner = module_paths.get(path)
        if isinstance(module_owner, str) and module_owner not in matching_owners:
            matching_owners.append(module_owner)
            matching_owners.sort()
        if not matching_policy_ids and not isinstance(module_owner, str):
            unowned_paths.append(path)
        path_matches.append(
            {
                "path": path,
                "policy_ids": matching_policy_ids,
                "owners": matching_owners,
                "unowned": path in unowned_paths,
            }
        )

    return {
        "owners": owner_rows,
        "policies": policy_rows,
        "module_paths": [
            {"path": path, "owner": module_paths[path]} for path in sorted(module_paths)
        ],
        "paths": path_matches,
        "unowned_paths": sorted(unowned_paths),
    }


def changed_path_impact_closure(
    manifest: Mapping[str, Any], changed_paths: Iterable[str]
) -> dict[str, Any]:
    """Compute a conservative, read-only impact closure for changed paths.

    A changed path first selects matching policy/module owners.  Slices that
    name a selected policy or owner are seeds; all reverse dependents are then
    included.  Their artifacts seed canonical passes, and pass dependencies
    flow forward to include downstream artifacts/passes.  Unowned paths and
    owners with no slice/policy bridge are classified as unresolved and mark
    the result incomplete instead of silently yielding an empty impact.
    """

    manifest = _require_valid_manifest(manifest)
    if isinstance(changed_paths, str):
        changed_paths = [changed_paths]
    normalised_paths = _sorted_unique(_normalise_changed_path(path) for path in changed_paths)
    view = owner_path_view(manifest, normalised_paths)
    matching_policy_ids = {
        policy_id for row in view["paths"] for policy_id in row["policy_ids"]
    }
    impacted_owners = {
        owner for row in view["paths"] for owner in row["owners"] if isinstance(owner, str)
    }
    slices = [
        item for item in manifest.get("slices", ()) if isinstance(item, Mapping)
    ]
    direct_slice_items = [
        item
        for item in slices
        if (
            item.get("owner") in impacted_owners
            or matching_policy_ids.intersection(item.get("path_policy_ids", ()))
        )
    ]
    slice_owner_ids = {
        item.get("owner") for item in slices if isinstance(item.get("owner"), str)
    }
    slice_policy_ids = {
        policy_id
        for item in slices
        for policy_id in item.get("path_policy_ids", ())
        if isinstance(policy_id, str)
    }
    policies_by_id = {
        item["id"]: item
        for item in manifest.get("path_policy", {}).get("policies", ())
        if isinstance(item, Mapping) and isinstance(item.get("id"), str)
    }
    unresolved_owners: set[str] = set()
    unresolved_paths: set[str] = set(view["unowned_paths"])
    for row in view["paths"]:
        row_unresolved_owners: set[str] = set()
        for owner in row["owners"]:
            owner_matching_policies = {
                policy_id
                for policy_id in row["policy_ids"]
                if policies_by_id[policy_id].get("owner") == owner
            }
            if (
                owner not in slice_owner_ids
                and not owner_matching_policies.intersection(slice_policy_ids)
            ):
                row_unresolved_owners.add(owner)
        if row_unresolved_owners:
            unresolved_owners.update(row_unresolved_owners)
            unresolved_paths.add(row["path"])
    # A reservation policy may be owned by the coordinator while the slices
    # using that reservation are leaf-owned.  Report both policy owners and
    # owners of directly affected slices so the closure remains conservative.
    impacted_owners.update(
        item.get("owner")
        for item in direct_slice_items
        if isinstance(item.get("owner"), str)
    )
    impacted_slices_by_id = {
        item["id"]
        for item in direct_slice_items
    }

    # Reverse dependency closure: a changed prerequisite can affect every
    # downstream slice, even when the downstream slice does not share a path.
    slices_by_id = {
        item["id"]: item for item in manifest.get("slices", ()) if isinstance(item, Mapping)
    }
    changed = True
    while changed:
        changed = False
        for item_id, item in slices_by_id.items():
            if item_id in impacted_slices_by_id:
                continue
            if set(item.get("depends_on", ())).intersection(impacted_slices_by_id):
                impacted_slices_by_id.add(item_id)
                changed = True

    direct_slices = sorted(item["id"] for item in direct_slice_items)
    impacted_slices = sorted(impacted_slices_by_id)
    impacted_artifacts: set[str] = set()
    for item_id in impacted_slices:
        item = slices_by_id[item_id]
        impacted_artifacts.update(item.get("artifact_inputs", ()))
        impacted_artifacts.update(item.get("artifact_outputs", ()))

    passes = [item for item in manifest.get("canonical_passes", ()) if isinstance(item, Mapping)]
    pass_by_id = {item["id"]: item for item in passes}
    impacted_pass_ids = {
        item["id"]
        for item in passes
        if set(item.get("input_artifacts", ())).intersection(impacted_artifacts)
        or set(item.get("output_artifacts", ())).intersection(impacted_artifacts)
    }
    # Once a pass is impacted, downstream pass boundaries and artifacts are
    # conservatively included.  This uses declared pass dependencies only.
    changed = True
    while changed:
        changed = False
        for item in passes:
            pass_id = item["id"]
            if pass_id in impacted_pass_ids:
                continue
            if set(item.get("depends_on", ())).intersection(impacted_pass_ids):
                impacted_pass_ids.add(pass_id)
                changed = True
        for pass_id in sorted(impacted_pass_ids):
            impacted_artifacts.update(pass_by_id[pass_id].get("output_artifacts", ()))

    return {
        "changed_paths": normalised_paths,
        "path_matches": view["paths"],
        "unowned_paths": view["unowned_paths"],
        "unresolved_owners": sorted(unresolved_owners),
        "unresolved_paths": sorted(unresolved_paths),
        "impact_status": "incomplete" if unresolved_paths else "complete",
        "impact_complete": not unresolved_paths,
        "blocking": bool(unresolved_paths),
        "impacted_policy_ids": sorted(matching_policy_ids),
        "impacted_owners": sorted(impacted_owners),
        "direct_slices": direct_slices,
        "impacted_slices": impacted_slices,
        "impacted_artifacts": sorted(impacted_artifacts),
        "impacted_passes": [
            item["id"] for item in passes if item["id"] in impacted_pass_ids
        ],
    }


def render_structure(
    manifest: Mapping[str, Any], changed_paths: Iterable[str] = ()
) -> dict[str, Any]:
    """Build the complete deterministic JSON-ready report."""

    manifest = _require_valid_manifest(manifest)
    return {
        "manifest": {
            "manifest_id": manifest.get("manifest_id"),
            "schema_version": manifest.get("schema_version"),
            "description": manifest.get("description"),
        },
        "summary": render_summary(manifest),
        "canonical_pass_table": canonical_pass_table(manifest),
        "slice_topological_waves": slice_topological_waves(manifest),
        "owner_path_view": owner_path_view(manifest),
        "changed_path_impact": changed_path_impact_closure(manifest, changed_paths),
    }


# Short aliases make the query API easy to discover without changing the
# explicit names used by the CLI or contract-oriented tests.
summary = render_summary
slice_waves = slice_topological_waves
impact_closure = changed_path_impact_closure


def stable_json(value: Any) -> str:
    """Serialize a report with stable key and collection formatting."""

    return json.dumps(
        value,
        ensure_ascii=True,
        allow_nan=False,
        sort_keys=True,
        separators=(",", ":"),
    )


def _selected_report(report: Mapping[str, Any], sections: Sequence[str]) -> Mapping[str, Any]:
    if not sections or "all" in sections:
        return report
    selected: dict[str, Any] = {"manifest": report["manifest"]}
    section_keys = {
        "summary": "summary",
        "passes": "canonical_pass_table",
        "canonical-passes": "canonical_pass_table",
        "slices": "slice_topological_waves",
        "slice-waves": "slice_topological_waves",
        "owners": "owner_path_view",
        "owner-path": "owner_path_view",
        "impact": "changed_path_impact",
    }
    for section in sections:
        key = section_keys.get(section)
        if key is not None:
            selected[key] = report[key]
    return selected


def main(argv: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "manifest",
        nargs="?",
        type=Path,
        default=DEFAULT_MANIFEST,
        help="project structure JSON manifest (default: contracts/project-structure.json)",
    )
    parser.add_argument(
        "--section",
        action="append",
        choices=("all", "summary", "passes", "canonical-passes", "slices", "slice-waves", "owners", "owner-path", "impact"),
        default=[],
        help="restrict output to one or more report sections",
    )
    parser.add_argument(
        "--summary", dest="summary_flag", action="store_true", help="shortcut for --section summary"
    )
    parser.add_argument(
        "--passes", dest="passes_flag", action="store_true", help="shortcut for --section passes"
    )
    parser.add_argument(
        "--slices", dest="slices_flag", action="store_true", help="shortcut for --section slices"
    )
    parser.add_argument(
        "--owners", dest="owners_flag", action="store_true", help="shortcut for --section owners"
    )
    parser.add_argument(
        "--changed-path",
        "--path",
        dest="changed_paths",
        action="append",
        default=[],
        help="changed repository-relative path (repeatable; unmatched paths are explicit)",
    )
    args = parser.parse_args(argv)
    sections = list(args.section)
    if args.summary_flag:
        sections.append("summary")
    if args.passes_flag:
        sections.append("passes")
    if args.slices_flag:
        sections.append("slices")
    if args.owners_flag:
        sections.append("owners")
    if args.changed_paths and "impact" not in sections:
        sections.append("impact")
    try:
        manifest = load_validated_manifest(args.manifest)
        full_report = render_structure(manifest, args.changed_paths)
        impact = full_report["changed_path_impact"]
        report = _selected_report(full_report, sections)
        output = stable_json(report) + "\n"
        # stdout is the only output channel; no output path or artifact write is
        # accepted by this read-only tool.
        sys.stdout.write(output)
        if args.changed_paths and impact.get("blocking"):
            print(
                "project structure impact query is incomplete; "
                "unresolved or unowned changed paths require coordinator review",
                file=sys.stderr,
            )
            return 2
        return 0
    except (RenderError, OSError, TypeError, ValueError) as exc:
        print(f"project structure rendering failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
