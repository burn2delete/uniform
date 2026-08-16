#include <stdio.h>

int
main(int argc, char **argv)
{
    int index;

    if (fputs("child-ok\n", stdout) == EOF) {
        return 70;
    }
    for (index = 1; index < argc; index += 1) {
        if (printf("arg[%d]=%s\n", index, argv[index]) < 0) {
            return 71;
        }
    }
    return 0;
}
