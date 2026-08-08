#include <stdio.h>

int
main(void)
{
    if (fputs("child-exit-23\n", stderr) == EOF) {
        return 70;
    }
    return 23;
}
