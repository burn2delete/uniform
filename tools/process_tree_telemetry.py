"""Best-effort, non-authoritative local process-tree telemetry."""

from __future__ import annotations

import subprocess


def process_tree_metrics(root_pid: int) -> dict[str, object]:
    """Sample aggregate RSS, process count, and CPU for ``root_pid``'s tree.

    The sample is observational only.  Failure is represented explicitly and
    never converted into a zero measurement.
    """

    try:
        result = subprocess.run(
            ["ps", "-axo", "pid=,ppid=,rss=,%cpu="],
            check=True,
            capture_output=True,
            text=True,
            timeout=0.5,
        )
        rows: dict[int, tuple[int, int, float]] = {}
        for line in result.stdout.splitlines():
            fields = line.split()
            if len(fields) != 4:
                continue
            pid, ppid, rss_kib = map(int, fields[:3])
            rows[pid] = (ppid, rss_kib, float(fields[3]))
        descendants = {root_pid}
        changed = True
        while changed:
            changed = False
            for pid, (ppid, _, _) in rows.items():
                if pid not in descendants and ppid in descendants:
                    descendants.add(pid)
                    changed = True
        present = [rows[pid] for pid in descendants if pid in rows]
        if not present:
            return {
                "process_count": None,
                "rss_bytes": None,
                "cpu_percent": None,
                "telemetry_available": False,
                "telemetry_error": "process tree was not present in the process-table sample",
            }
        return {
            "process_count": len(present),
            "rss_bytes": sum(row[1] for row in present) * 1024,
            "cpu_percent": round(sum(row[2] for row in present), 2),
            "telemetry_available": True,
            "telemetry_error": None,
        }
    except (OSError, subprocess.SubprocessError, ValueError) as error:
        return {
            "process_count": None,
            "rss_bytes": None,
            "cpu_percent": None,
            "telemetry_available": False,
            "telemetry_error": f"{type(error).__name__}: {error}",
        }
