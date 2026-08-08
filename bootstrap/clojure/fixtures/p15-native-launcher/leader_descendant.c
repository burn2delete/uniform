#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

static int
write_text(const char *path, const char *text)
{
    FILE *file = fopen(path, "w");

    if (file == NULL) {
        return -1;
    }
    if (fputs(text, file) == EOF) {
        (void)fclose(file);
        return -1;
    }
    return fclose(file);
}

int
main(int argc, char **argv)
{
    int pipe_fds[2];
    pid_t child;
    char pid_text[64];
    char ready_byte;

    if (argc != 3) {
        return 64;
    }
    if (pipe(pipe_fds) != 0) {
        return 65;
    }
    child = fork();
    if (child < 0) {
        return 66;
    }
    if (child == 0) {
        (void)close(pipe_fds[0]);
        (void)snprintf(pid_text, sizeof(pid_text), "%ld\n", (long)getpid());
        if (write_text(argv[2], pid_text) != 0) {
            _exit(67);
        }
        if (write(pipe_fds[1], "R", 1) != 1) {
            _exit(68);
        }
        (void)close(pipe_fds[1]);
        for (;;) {
            (void)pause();
        }
    }

    (void)close(pipe_fds[1]);
    if (read(pipe_fds[0], &ready_byte, 1) != 1) {
        (void)close(pipe_fds[0]);
        return 69;
    }
    (void)close(pipe_fds[0]);
    if (write_text(argv[1], "leader-exited\n") != 0) {
        return 70;
    }
    return 0;
}
