#include <stdio.h>

int
main(int argc, char **argv)
{
    FILE *marker;

    if (argc != 2) {
        return 64;
    }
    marker = fopen(argv[1], "w");
    if (marker == NULL) {
        return 65;
    }
    if (fputs("child-ran\n", marker) == EOF) {
        (void)fclose(marker);
        return 66;
    }
    if (fclose(marker) != 0) {
        return 67;
    }
    return 0;
}
