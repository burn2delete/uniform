#define _DARWIN_C_SOURCE 1

/*
 * Bounded Darwin launcher prerequisite for P15-S23.
 *
 * This is an identity-bound pathname admission primitive, not descriptor-
 * relative execution.  It proves only that the suspended child's executable
 * mapping names the vnode opened before spawn and that no live, non-zombie
 * member remains in the child's process group before return.  It does not
 * resist same-euid in-place mutation or external SIGCONT, contain setsid/
 * double-fork escapees, verify code signatures, own a public Gravity route,
 * retire a seed boundary, or prove whole-process-tree reaping.
 */

#include <errno.h>
#include <fcntl.h>
#include <libproc.h>
#include <limits.h>
#include <mach-o/fat.h>
#include <mach-o/loader.h>
#include <mach/vm_prot.h>
#include <signal.h>
#include <spawn.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/proc_info.h>
#include <sys/proc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

#ifndef O_NOFOLLOW_ANY
#error "p15 native launcher requires Darwin O_NOFOLLOW_ANY"
#endif
#ifndef O_UNIQUE
#error "p15 native launcher requires Darwin O_UNIQUE"
#endif
#ifndef POSIX_SPAWN_START_SUSPENDED
#error "p15 native launcher requires POSIX_SPAWN_START_SUSPENDED"
#endif
#ifndef POSIX_SPAWN_CLOEXEC_DEFAULT
#error "p15 native launcher requires POSIX_SPAWN_CLOEXEC_DEFAULT"
#endif

extern char **environ;

static volatile sig_atomic_t termination_signal = 0;

enum {
    MAX_TIMEOUT_MS = 600000,
    MAX_REGION_COUNT = 4096,
    GROUP_REAP_WAIT_MS = 2000
};

static int
fail(const char *id, const char *message)
{
    (void)fprintf(stderr, "%s: %s\n", id, message);
    return 125;
}

static void
record_termination_signal(int signal_number)
{
    termination_signal = signal_number;
}

static int
install_termination_handlers(void)
{
    struct sigaction action;

    memset(&action, 0, sizeof(action));
    action.sa_handler = record_termination_signal;
    if (sigemptyset(&action.sa_mask) != 0) {
        return -1;
    }
    if (sigaction(SIGTERM, &action, NULL) != 0 ||
        sigaction(SIGINT, &action, NULL) != 0 ||
        sigaction(SIGHUP, &action, NULL) != 0) {
        return -1;
    }
    return 0;
}

static int64_t
monotonic_millis(void)
{
    struct timespec now;

    if (clock_gettime(CLOCK_MONOTONIC, &now) != 0) {
        return -1;
    }
    return ((int64_t)now.tv_sec * 1000) + (now.tv_nsec / 1000000);
}

static int
sleep_millis(long milliseconds)
{
    struct timespec duration;

    duration.tv_sec = milliseconds / 1000;
    duration.tv_nsec = (milliseconds % 1000) * 1000000L;
    while (nanosleep(&duration, &duration) != 0) {
        if (errno != EINTR) {
            return -1;
        }
    }
    return 0;
}

static int
parse_timeout(const char *text, int *timeout_ms)
{
    char *end = NULL;
    long parsed;

    errno = 0;
    parsed = strtol(text, &end, 10);
    if (errno != 0 || end == text || *end != '\0' || parsed < 1 ||
        parsed > MAX_TIMEOUT_MS) {
        return -1;
    }
    *timeout_ms = (int)parsed;
    return 0;
}

static int
mach_o_magic_valid(int descriptor)
{
    uint32_t magic = 0;
    ssize_t count = pread(descriptor, &magic, sizeof(magic), 0);

    if (count != (ssize_t)sizeof(magic)) {
        return 0;
    }
    return magic == MH_MAGIC || magic == MH_CIGAM ||
           magic == MH_MAGIC_64 || magic == MH_CIGAM_64 ||
           magic == FAT_MAGIC || magic == FAT_CIGAM ||
           magic == FAT_MAGIC_64 || magic == FAT_CIGAM_64;
}

static int
canonicalize_nonsymlink(const char *path, char canonical[PATH_MAX])
{
    struct stat path_status;

    if (path == NULL || path[0] != '/') {
        errno = EINVAL;
        return -2;
    }
    if (lstat(path, &path_status) != 0 || S_ISLNK(path_status.st_mode)) {
        errno = ELOOP;
        return -1;
    }
    if (realpath(path, canonical) == NULL || canonical[0] != '/') {
        return -1;
    }
    return 0;
}

static int
open_validated_executable(const char *path, struct stat *identity)
{
    int descriptor;

    descriptor = open(path, O_RDONLY | O_CLOEXEC | O_NOFOLLOW_ANY | O_UNIQUE);
    if (descriptor < 0) {
        return -1;
    }
    if (fstat(descriptor, identity) != 0) {
        (void)close(descriptor);
        return -1;
    }
    if (!S_ISREG(identity->st_mode) ||
        (identity->st_mode & (S_IXUSR | S_IXGRP | S_IXOTH)) == 0) {
        (void)close(descriptor);
        errno = EACCES;
        return -3;
    }
    if (identity->st_uid != geteuid() || identity->st_nlink != 1) {
        (void)close(descriptor);
        errno = EPERM;
        return -4;
    }
    if (!mach_o_magic_valid(descriptor)) {
        (void)close(descriptor);
        errno = ENOEXEC;
        return -3;
    }
    return descriptor;
}

#ifdef GRAVITY_NATIVE_LAUNCHER_TESTING
static int
apply_replacement_hook(const char *target)
{
    const char *replacement = getenv("GRAVITY_NATIVE_LAUNCHER_TEST_REPLACEMENT");
    char canonical_replacement[PATH_MAX];
    struct stat ignored;
    int descriptor;

    if (replacement == NULL || replacement[0] == '\0') {
        return 0;
    }
    if (canonicalize_nonsymlink(replacement, canonical_replacement) != 0) {
        return -1;
    }
    descriptor = open_validated_executable(canonical_replacement, &ignored);
    if (descriptor < 0) {
        return -1;
    }
    if (close(descriptor) != 0 || rename(canonical_replacement, target) != 0) {
        return -1;
    }
    return 0;
}
#endif

static int
same_vnode(const struct stat *opened, const struct vinfo_stat *mapped)
{
    return (uint64_t)opened->st_dev == (uint64_t)mapped->vst_dev &&
           (uint64_t)opened->st_ino == mapped->vst_ino &&
           opened->st_uid == mapped->vst_uid &&
           opened->st_nlink == mapped->vst_nlink &&
           opened->st_size == mapped->vst_size &&
           (uint32_t)opened->st_gen == mapped->vst_gen &&
           ((opened->st_mode & S_IFMT) == (mapped->vst_mode & S_IFMT));
}

/* Returns 1 for an exact executable mapping, 0 for mismatch, -1 for no proof. */
static int
mapped_main_identity(pid_t child, const struct stat *opened)
{
    uint64_t address = 0;
    size_t region_count;
    int executable_candidates = 0;
    int exact_candidates = 0;

    for (region_count = 0; region_count < MAX_REGION_COUNT; region_count += 1) {
        struct proc_regionwithpathinfo region;
        uint64_t next;
        int count;

        memset(&region, 0, sizeof(region));
        count = proc_pidinfo(child, PROC_PIDREGIONPATHINFO, address,
                             &region, (int)sizeof(region));
        if (count == 0) {
            break;
        }
        if (count != (int)sizeof(region) || region.prp_prinfo.pri_size == 0) {
            return -1;
        }
        if ((region.prp_prinfo.pri_protection & VM_PROT_EXECUTE) != 0 &&
            region.prp_prinfo.pri_offset == 0 &&
            S_ISREG(region.prp_vip.vip_vi.vi_stat.vst_mode)) {
            executable_candidates += 1;
            if (same_vnode(opened, &region.prp_vip.vip_vi.vi_stat)) {
                exact_candidates += 1;
            }
        }
        next = region.prp_prinfo.pri_address + region.prp_prinfo.pri_size;
        if (next <= address) {
            return -1;
        }
        address = next;
    }
    if (region_count == MAX_REGION_COUNT || executable_candidates == 0) {
        return -1;
    }
    return exact_candidates == 1 ? 1 : 0;
}

static int
group_has_live_members(pid_t group, pid_t ignored_root)
{
    pid_t members[256];
    int member_count;
    size_t index;
    size_t count;

    memset(members, 0, sizeof(members));
    member_count = proc_listpgrppids(group, members, (int)sizeof(members));
    if (member_count < 0 ||
        member_count >= (int)(sizeof(members) / sizeof(members[0]))) {
        return -1;
    }
    count = (size_t)member_count;
    for (index = 0; index < count; index += 1) {
        struct proc_bsdinfo information;
        int result;

        if (members[index] <= 0 || members[index] == ignored_root) {
            continue;
        }
        memset(&information, 0, sizeof(information));
        result = proc_pidinfo(members[index], PROC_PIDTBSDINFO, 0,
                              &information, (int)sizeof(information));
        if (result != (int)sizeof(information)) {
            return -1;
        }
        if (information.pbi_status != SZOMB) {
            return 1;
        }
    }
    return 0;
}

static int
wait_group_absent(pid_t group, int wait_ms)
{
    int64_t deadline = monotonic_millis();

    if (deadline < 0) {
        return -1;
    }
    deadline += wait_ms;
    for (;;) {
        int present = group_has_live_members(group, group);
        int64_t now;

        if (present == 0) {
            return 0;
        }
        if (present > 0 && kill(-group, SIGKILL) != 0 && errno != ESRCH) {
            return -1;
        }
        now = monotonic_millis();
        if (now < 0 || now >= deadline) {
            return -1;
        }
        if (sleep_millis(10) != 0) {
            return -1;
        }
    }
}

static int
wait_root(pid_t child, int *status, int options)
{
    pid_t result;

    do {
        result = waitpid(child, status, options);
    } while (result < 0 && errno == EINTR);
    return result == child ? 1 : (result == 0 ? 0 : -1);
}

static int
observe_root_exit(pid_t child, siginfo_t *information)
{
    int result;

    memset(information, 0, sizeof(*information));
    do {
        result = waitid(P_PID, (id_t)child, information,
                        WEXITED | WNOHANG | WNOWAIT);
    } while (result != 0 && errno == EINTR);
    if (result != 0) {
        return -1;
    }
    return information->si_pid == child ? 1 : 0;
}

static int
kill_and_reap_group(pid_t child)
{
    int status = 0;
    int group_result;
    int wait_result;

    if (kill(-child, SIGKILL) != 0) {
        if (errno != ESRCH) {
            return -1;
        }
        /* The dedicated-pgroup invariant may have failed before validation. */
        if (kill(child, SIGKILL) != 0 && errno != ESRCH) {
            return -1;
        }
    }
    group_result = wait_group_absent(child, GROUP_REAP_WAIT_MS);
    if (group_result != 0) {
        return -1;
    }
    wait_result = wait_root(child, &status, 0);
    if (wait_result < 0 && errno != ECHILD) {
        return -1;
    }
    return 0;
}

static int
spawn_suspended(char *const child_argv[], pid_t *child)
{
    posix_spawnattr_t attributes;
    posix_spawn_file_actions_t actions;
    short flags = POSIX_SPAWN_CLOEXEC_DEFAULT |
                  POSIX_SPAWN_START_SUSPENDED |
                  POSIX_SPAWN_SETPGROUP;
    int result;

    result = posix_spawnattr_init(&attributes);
    if (result != 0) {
        return result;
    }
    result = posix_spawn_file_actions_init(&actions);
    if (result != 0) {
        (void)posix_spawnattr_destroy(&attributes);
        return result;
    }
    if ((result = posix_spawnattr_setflags(&attributes, flags)) == 0 &&
        (result = posix_spawnattr_setpgroup(&attributes, 0)) == 0 &&
        (result = posix_spawn_file_actions_addinherit_np(&actions, STDIN_FILENO)) == 0 &&
        (result = posix_spawn_file_actions_addinherit_np(&actions, STDOUT_FILENO)) == 0 &&
        (result = posix_spawn_file_actions_addinherit_np(&actions, STDERR_FILENO)) == 0) {
        result = posix_spawn(child, child_argv[0], &actions, &attributes,
                             child_argv, environ);
    }
    (void)posix_spawn_file_actions_destroy(&actions);
    (void)posix_spawnattr_destroy(&attributes);
    return result;
}

static int
run_child(pid_t child, int timeout_ms)
{
    int64_t deadline = monotonic_millis();

    if (deadline < 0) {
        (void)kill_and_reap_group(child);
        return fail("P15NL015", "monotonic clock unavailable");
    }
    deadline += timeout_ms;
    for (;;) {
        siginfo_t exit_information;
        if (termination_signal != 0) {
            if (kill_and_reap_group(child) != 0) {
                return fail("P15NL013", "interrupted child process group was not removed");
            }
            return fail("P15NL015", "launcher interrupted; child group removed");
        }
        int waited = observe_root_exit(child, &exit_information);
        int64_t now;

        if (waited < 0) {
            (void)kill_and_reap_group(child);
            return fail("P15NL014", "waitpid failed");
        }
        if (waited == 1) {
            int present = group_has_live_members(child, child);
            if (present < 0) {
                if (kill_and_reap_group(child) != 0) {
                    (void)kill(child, SIGKILL);
                    (void)wait_root(child, &present, 0);
                }
                return fail("P15NL013", "post-exit process group census was unverified");
            }
            if (present > 0) {
                if (kill_and_reap_group(child) != 0) {
                    return fail("P15NL013", "surviving process group was not removed");
                }
                return fail("P15NL012", "leader exited while its process group survived");
            }
            if (wait_root(child, &present, 0) < 0) {
                return fail("P15NL014", "observed child exit could not be reaped");
            }
            if (exit_information.si_code == CLD_EXITED) {
                return exit_information.si_status;
            }
            if (exit_information.si_code == CLD_KILLED ||
                exit_information.si_code == CLD_DUMPED) {
                return 128 + exit_information.si_status;
            }
            return fail("P15NL014", "unsupported child wait status");
        }
        now = monotonic_millis();
        if (now < 0) {
            (void)kill_and_reap_group(child);
            return fail("P15NL015", "monotonic clock unavailable");
        }
        if (now >= deadline) {
            if (kill_and_reap_group(child) != 0) {
                return fail("P15NL013", "timed-out process group was not removed");
            }
            return fail("P15NL011", "child exceeded launcher timeout");
        }
        if (sleep_millis(10) != 0) {
            (void)kill_and_reap_group(child);
            return fail("P15NL015", "launcher polling failed");
        }
    }
}

int
main(int argc, char **argv)
{
    char canonical_target[PATH_MAX];
    struct stat opened_identity;
    pid_t child = -1;
    int timeout_ms;
    int descriptor;
    int spawn_result;
    int mapping_result;

    if (argc < 5 || strcmp(argv[1], "--timeout-ms") != 0 ||
        strcmp(argv[3], "--") != 0 || argv[4][0] == '\0') {
        return fail("P15NL001", "usage: launcher --timeout-ms N -- /absolute/executable [args...]");
    }
    if (parse_timeout(argv[2], &timeout_ms) != 0) {
        return fail("P15NL002", "timeout must be an integer from 1 through 600000 milliseconds");
    }
    if (install_termination_handlers() != 0) {
        return fail("P15NL015", "launcher signal handlers could not be installed");
    }
    descriptor = canonicalize_nonsymlink(argv[4], canonical_target);
    if (descriptor == -2) {
        return fail("P15NL003", "target executable path must be absolute");
    }
    if (descriptor != 0) {
        return fail("P15NL004", "target executable path canonicalization/no-follow admission failed");
    }
    descriptor = open_validated_executable(canonical_target, &opened_identity);
    if (descriptor == -1) {
        return fail("P15NL004", "target executable open/stat/no-follow admission failed");
    }
    if (descriptor == -3) {
        return fail("P15NL005", "target must be a regular executable Mach-O file");
    }
    if (descriptor == -4) {
        return fail("P15NL006", "target must be owned by the current euid with one link");
    }
#ifdef GRAVITY_NATIVE_LAUNCHER_TESTING
    if (apply_replacement_hook(canonical_target) != 0) {
        (void)close(descriptor);
        return fail("P15NL015", "test-only replacement hook failed");
    }
#endif
    if (termination_signal != 0) {
        (void)close(descriptor);
        return fail("P15NL015", "launcher interrupted before child creation");
    }
    argv[4] = canonical_target;
    spawn_result = spawn_suspended(&argv[4], &child);
    if (spawn_result != 0) {
        (void)close(descriptor);
        errno = spawn_result;
        return fail("P15NL007", "posix_spawn failed");
    }
    if (termination_signal != 0) {
        (void)close(descriptor);
        if (kill_and_reap_group(child) != 0) {
            return fail("P15NL013", "interrupted suspended child group was not removed");
        }
        return fail("P15NL015", "launcher interrupted during child creation; child group removed");
    }
    if (getpgid(child) != child) {
        if (kill_and_reap_group(child) != 0) {
            (void)kill(child, SIGKILL);
            (void)wait_root(child, &spawn_result, 0);
        }
        (void)close(descriptor);
        return fail("P15NL013", "spawned child did not enter its dedicated process group");
    }
    mapping_result = mapped_main_identity(child, &opened_identity);
    (void)close(descriptor);
    if (termination_signal != 0) {
        if (kill_and_reap_group(child) != 0) {
            return fail("P15NL013", "interrupted unverified child group was not removed");
        }
        return fail("P15NL015", "launcher interrupted during identity verification; child group removed");
    }
    if (mapping_result < 0) {
        if (kill_and_reap_group(child) != 0) {
            return fail("P15NL013", "unverified suspended child group was not removed");
        }
        return fail("P15NL008", "suspended child main executable mapping could not be verified");
    }
    if (mapping_result == 0) {
        if (kill_and_reap_group(child) != 0) {
            return fail("P15NL013", "mismatched suspended child group was not removed");
        }
        return fail("P15NL009", "suspended child executable vnode does not match opened target");
    }
    if (kill(child, SIGCONT) != 0) {
        if (kill_and_reap_group(child) != 0) {
            return fail("P15NL013", "unreleased child group was not removed");
        }
        return fail("P15NL010", "suspended child could not be released");
    }
    return run_child(child, timeout_ms);
}
