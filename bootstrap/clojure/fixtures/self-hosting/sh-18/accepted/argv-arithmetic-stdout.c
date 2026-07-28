#include <errno.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv) {
  char *left_end = NULL;
  char *right_end = NULL;
  long left = 0;
  long right = 0;

  if (argc != 3) {
    fputs("expected two integer arguments\n", stderr);
    return 64;
  }

  errno = 0;
  left = strtol(argv[1], &left_end, 10);
  if (errno != 0 || left_end == argv[1] || *left_end != '\0') {
    fputs("invalid left integer\n", stderr);
    return 65;
  }

  errno = 0;
  right = strtol(argv[2], &right_end, 10);
  if (errno != 0 || right_end == argv[2] || *right_end != '\0') {
    fputs("invalid right integer\n", stderr);
    return 66;
  }

  if ((right > 0 && left > LONG_MAX - right) ||
      (right < 0 && left < LONG_MIN - right)) {
    fputs("integer addition overflow\n", stderr);
    return 67;
  }

  printf("sum=%ld\n", left + right);
  printf("argc=%d\n", argc);
  return 0;
}
