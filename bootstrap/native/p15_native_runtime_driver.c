#include <CommonCrypto/CommonDigest.h>
#include <errno.h>
#include <inttypes.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if !defined(__aarch64__)
#error "p15 native runtime driver is pinned to arm64"
#endif

#define PACKET_LIMIT 65536u
#define INSTRUCTION_LIMIT 128u
#define STACK_LIMIT 128u
#define VALUE_LIMIT 1024u
#define OUTPUT_LIMIT 8192u
#define RULE_SHA256 "4f7ab2fc7762c537d3ce93a0ca1faab35331f00128c6decd9ff8215dd3668a19"

static char diagnostic_source[VALUE_LIMIT + 1u] = "packet:unknown";

typedef enum { VALUE_STRING, VALUE_INT, VALUE_BOOL, VALUE_NIL } value_kind;
typedef struct {
  value_kind kind;
  int64_t integer;
  bool boolean;
  size_t length;
  unsigned char bytes[VALUE_LIMIT + 1u];
} value;

static int diagnostic(const char *id, const char *message) {
  (void)fprintf(stderr,
                "{:diagnostic \"%s\" :severity :error "
                ":stage :p15-native-runtime-driver "
                ":message \"%s\" :source-path \"%s\" "
                ":profile :native :target :arm64-macos "
                ":runtime-provider :gravity.native/libsystem-stdio-v1 "
                ":remediation :repair_bounded_native_runtime_packet "
                ":public-command-route? false "
                ":clojure-seed-boundary? true :self-hosted? false}\n",
                id, message, diagnostic_source);
  return 125;
}

static void sha256_hex(const unsigned char *bytes, size_t length, char out[65]) {
  unsigned char digest[CC_SHA256_DIGEST_LENGTH];
  static const char alphabet[] = "0123456789abcdef";
  (void)CC_SHA256(bytes, (CC_LONG)length, digest);
  for (size_t index = 0; index < sizeof(digest); ++index) {
    out[index * 2u] = alphabet[digest[index] >> 4u];
    out[index * 2u + 1u] = alphabet[digest[index] & 15u];
  }
  out[64] = '\0';
}

static bool exact_sha(const char *text) {
  if (strlen(text) != 64u) return false;
  for (size_t i = 0; i < 64u; ++i) {
    if (!((text[i] >= '0' && text[i] <= '9') ||
          (text[i] >= 'a' && text[i] <= 'f'))) return false;
  }
  return true;
}

static bool exact_hex(const char *text) {
  size_t length = strlen(text);
  if ((length & 1u) != 0u || length > VALUE_LIMIT * 2u) return false;
  for (size_t i = 0; i < length; ++i) {
    if (!((text[i] >= '0' && text[i] <= '9') ||
          (text[i] >= 'a' && text[i] <= 'f'))) return false;
  }
  return true;
}

static bool valid_utf8(const unsigned char *bytes, size_t length) {
  size_t index = 0u;
  while (index < length) {
    unsigned char first = bytes[index++];
    if (first <= 0x7fu) continue;
    if (first >= 0xc2u && first <= 0xdfu) {
      if (index >= length || bytes[index] < 0x80u || bytes[index] > 0xbfu)
        return false;
      index += 1u;
      continue;
    }
    if (first >= 0xe0u && first <= 0xefu) {
      if (index + 1u >= length) return false;
      unsigned char second = bytes[index];
      unsigned char third = bytes[index + 1u];
      if (third < 0x80u || third > 0xbfu) return false;
      if ((first == 0xe0u && (second < 0xa0u || second > 0xbfu)) ||
          (first == 0xedu && (second < 0x80u || second > 0x9fu)) ||
          (first != 0xe0u && first != 0xedu &&
           (second < 0x80u || second > 0xbfu)))
        return false;
      index += 2u;
      continue;
    }
    if (first >= 0xf0u && first <= 0xf4u) {
      if (index + 2u >= length) return false;
      unsigned char second = bytes[index];
      unsigned char third = bytes[index + 1u];
      unsigned char fourth = bytes[index + 2u];
      if (third < 0x80u || third > 0xbfu ||
          fourth < 0x80u || fourth > 0xbfu)
        return false;
      if ((first == 0xf0u && (second < 0x90u || second > 0xbfu)) ||
          (first == 0xf4u && (second < 0x80u || second > 0x8fu)) ||
          (first != 0xf0u && first != 0xf4u &&
           (second < 0x80u || second > 0xbfu)))
        return false;
      index += 3u;
      continue;
    }
    return false;
  }
  return true;
}

static unsigned char hex_nibble(char c) {
  if (c >= '0' && c <= '9') return (unsigned char)(c - '0');
  return (unsigned char)(c - 'a' + 10);
}

static bool decode_source_path(const char *hex) {
  size_t hex_length = strlen(hex);
  if (hex_length == 0u || !exact_hex(hex)) return false;
  size_t length = hex_length / 2u;
  for (size_t i = 0u; i < length; ++i) {
    unsigned char byte = (unsigned char)((hex_nibble(hex[i * 2u]) << 4u) |
                                         hex_nibble(hex[i * 2u + 1u]));
    if (!((byte >= 'a' && byte <= 'z') || (byte >= 'A' && byte <= 'Z') ||
          (byte >= '0' && byte <= '9') || byte == '/' || byte == '.' ||
          byte == '_' || byte == '-')) return false;
    diagnostic_source[i] = (char)byte;
  }
  diagnostic_source[length] = '\0';
  return (length > 8u && strcmp(diagnostic_source + length - 8u, ".gravity") == 0) ||
         (length > 4u && strcmp(diagnostic_source + length - 4u, ".qst") == 0);
}

static bool parse_size(const char *text, size_t maximum, size_t *result) {
  size_t parsed = 0u;
  size_t index = 0u;
  if (text[0] == '\0' ||
      (text[0] == '0' && text[1] != '\0') ||
      (text[0] != '0' && (text[0] < '1' || text[0] > '9')))
    return false;
  while (text[index] != '\0') {
    unsigned int digit;
    if (text[index] < '0' || text[index] > '9') return false;
    digit = (unsigned int)(text[index] - '0');
    if ((size_t)digit > maximum || parsed > (maximum - digit) / 10u)
      return false;
    parsed = parsed * 10u + digit;
    index += 1u;
  }
  if (parsed > maximum) return false;
  *result = parsed;
  return true;
}

static bool parse_i64(const char *text, int64_t *result) {
  bool negative = text[0] == '-';
  const char *digits = negative ? text + 1 : text;
  uint64_t maximum = negative ? UINT64_C(9223372036854775808)
                              : UINT64_C(9223372036854775807);
  uint64_t parsed = 0u;
  size_t index = 0u;
  if (digits[0] == '\0' ||
      (digits[0] == '0' && (negative || digits[1] != '\0')) ||
      (digits[0] != '0' && (digits[0] < '1' || digits[0] > '9')))
    return false;
  while (digits[index] != '\0') {
    unsigned int digit;
    if (digits[index] < '0' || digits[index] > '9') return false;
    digit = (unsigned int)(digits[index] - '0');
    if (parsed > (maximum - digit) / UINT64_C(10)) return false;
    parsed = parsed * UINT64_C(10) + digit;
    index += 1u;
  }
  if (negative && parsed == UINT64_C(9223372036854775808))
    *result = INT64_MIN;
  else if (negative)
    *result = -(int64_t)parsed;
  else
    *result = (int64_t)parsed;
  return true;
}

static bool falsey(const value *item) {
  return item->kind == VALUE_NIL ||
         (item->kind == VALUE_BOOL && !item->boolean);
}

static bool render(const value *item, unsigned char out[VALUE_LIMIT + 1u],
                   size_t *length) {
  int count;
  switch (item->kind) {
    case VALUE_STRING:
      memcpy(out, item->bytes, item->length);
      *length = item->length;
      return true;
    case VALUE_INT:
      count = snprintf((char *)out, VALUE_LIMIT + 1u, "%" PRId64, item->integer);
      if (count < 0 || (size_t)count > VALUE_LIMIT) return false;
      *length = (size_t)count;
      return true;
    case VALUE_BOOL:
      memcpy(out, item->boolean ? "true" : "false", item->boolean ? 4u : 5u);
      *length = item->boolean ? 4u : 5u;
      return true;
    case VALUE_NIL:
      memcpy(out, "nil", 3u);
      *length = 3u;
      return true;
  }
  return false;
}

static int execute_payload(char *payload, size_t declared_count) {
  char *instructions[INSTRUCTION_LIMIT];
  size_t instruction_count = 0u;
  char *cursor = payload;
  while (*cursor != '\0') {
    char *newline = strchr(cursor, '\n');
    if (newline == NULL) return diagnostic("P15NR003", "payload is not canonical newline-terminated text");
    *newline = '\0';
    if (*cursor == '\0' || instruction_count == INSTRUCTION_LIMIT)
      return diagnostic("P15NR002", "instruction count exceeds the bounded packet contract");
    instructions[instruction_count++] = cursor;
    cursor = newline + 1;
  }
  if (instruction_count != declared_count)
    return diagnostic("P15NR003", "declared instruction count does not match the payload");

  value stack[STACK_LIMIT];
  unsigned char output[OUTPUT_LIMIT];
  size_t depth = 0u;
  size_t emitted = 0u;
  bool halted = false;
  for (size_t pc = 0u; pc < instruction_count; ++pc) {
    char *line = instructions[pc];
    char *operand = strchr(line, ' ');
    if (operand != NULL) *operand++ = '\0';

    if (strcmp(line, "push-string") == 0) {
      if (operand == NULL || !exact_hex(operand))
        return diagnostic("P15NR007", "push-string requires bounded lowercase hexadecimal UTF-8 bytes");
      if (depth == STACK_LIMIT) return diagnostic("P15NR008", "value stack overflow");
      value *item = &stack[depth++];
      item->kind = VALUE_STRING;
      item->length = strlen(operand) / 2u;
      for (size_t i = 0; i < item->length; ++i)
        item->bytes[i] = (unsigned char)((hex_nibble(operand[i * 2u]) << 4u) |
                                         hex_nibble(operand[i * 2u + 1u]));
      if (!valid_utf8(item->bytes, item->length))
        return diagnostic("P15NR007", "push-string bytes are not canonical UTF-8");
    } else if (strcmp(line, "push-int") == 0) {
      int64_t parsed;
      if (operand == NULL || !parse_i64(operand, &parsed))
        return diagnostic("P15NR007", "push-int requires a canonical signed 64-bit integer");
      if (depth == STACK_LIMIT) return diagnostic("P15NR008", "value stack overflow");
      stack[depth++] = (value){.kind = VALUE_INT, .integer = parsed};
    } else if (strcmp(line, "push-bool") == 0) {
      if (operand == NULL || (strcmp(operand, "true") != 0 && strcmp(operand, "false") != 0))
        return diagnostic("P15NR007", "push-bool requires true or false");
      if (depth == STACK_LIMIT) return diagnostic("P15NR008", "value stack overflow");
      stack[depth++] = (value){.kind = VALUE_BOOL, .boolean = strcmp(operand, "true") == 0};
    } else if (strcmp(line, "push-nil") == 0) {
      if (operand != NULL) return diagnostic("P15NR007", "push-nil accepts no operand");
      if (depth == STACK_LIMIT) return diagnostic("P15NR008", "value stack overflow");
      stack[depth++] = (value){.kind = VALUE_NIL};
    } else if (strcmp(line, "str") == 0) {
      size_t arity;
      if (operand == NULL || !parse_size(operand, 2u, &arity) || arity < 1u)
        return diagnostic("P15NR007", "str requires arity one or two");
      if (depth < arity) return diagnostic("P15NR008", "str value stack underflow");
      unsigned char left[VALUE_LIMIT + 1u], right[VALUE_LIMIT + 1u];
      size_t left_length = 0u, right_length = 0u;
      if (!render(&stack[depth - arity], left, &left_length) ||
          (arity == 2u && !render(&stack[depth - 1u], right, &right_length)) ||
          left_length + right_length > VALUE_LIMIT)
        return diagnostic("P15NR009", "str result exceeds the bounded value size");
      depth -= arity;
      value *result = &stack[depth++];
      result->kind = VALUE_STRING;
      memcpy(result->bytes, left, left_length);
      if (arity == 2u) memcpy(result->bytes + left_length, right, right_length);
      result->length = left_length + right_length;
    } else if (strcmp(line, "println") == 0) {
      size_t arity;
      if (operand == NULL || !parse_size(operand, 2u, &arity) || arity < 1u)
        return diagnostic("P15NR007", "println requires arity one or two");
      if (depth < arity) return diagnostic("P15NR008", "println value stack underflow");
      unsigned char rendered[VALUE_LIMIT + 1u];
      for (size_t index = 0u; index < arity; ++index) {
        size_t rendered_length = 0u;
        if (!render(&stack[depth - arity + index], rendered, &rendered_length) ||
            emitted + rendered_length + (index == 0u ? 0u : 1u) + 1u > OUTPUT_LIMIT)
          return diagnostic("P15NR009", "application output exceeds the bounded runtime contract");
        if (index != 0u) output[emitted++] = (unsigned char)' ';
        memcpy(output + emitted, rendered, rendered_length);
        emitted += rendered_length;
      }
      output[emitted++] = (unsigned char)'\n';
      depth -= arity;
      stack[depth++] = (value){.kind = VALUE_NIL};
    } else if (strcmp(line, "jump") == 0 || strcmp(line, "jump-if-false") == 0) {
      size_t target;
      if (operand == NULL || !parse_size(operand, instruction_count - 1u, &target) || target <= pc)
        return diagnostic("P15NR007", "jumps must target a later bounded instruction index");
      if (strcmp(line, "jump-if-false") == 0) {
        if (depth == 0u) return diagnostic("P15NR008", "jump-if-false value stack underflow");
        bool take = falsey(&stack[--depth]);
        if (take) pc = target - 1u;
      } else {
        pc = target - 1u;
      }
    } else if (strcmp(line, "halt") == 0) {
      if (operand != NULL || pc + 1u != instruction_count)
        return diagnostic("P15NR010", "halt must be the final instruction and accepts no operand");
      halted = true;
    } else {
      return diagnostic("P15NR006", "packet contains an unsupported runtime instruction");
    }
  }
  if (!halted) return diagnostic("P15NR010", "packet is missing its final halt instruction");
  if (fwrite(output, 1u, emitted, stdout) != emitted)
    return diagnostic("P15NR009", "application stdout write failed");
  if (fflush(stdout) != 0) return diagnostic("P15NR009", "application stdout flush failed");
  return 0;
}

static int run_packet(void) {
  static unsigned char packet[PACKET_LIMIT + 1u];
  size_t length = fread(packet, 1u, PACKET_LIMIT + 1u, stdin);
  if (ferror(stdin) || length == 0u || length > PACKET_LIMIT)
    return diagnostic("P15NR002", "packet input is empty, unreadable, or over bound");
  if (memchr(packet, '\0', length) != NULL)
    return diagnostic("P15NR003", "packet contains an embedded NUL byte");
  packet[length] = '\0';
  char *separator = strstr((char *)packet, "\n--\n");
  if (separator == NULL) return diagnostic("P15NR003", "packet header delimiter is missing");
  char *payload = separator + 4;
  size_t payload_length = length - (size_t)(payload - (char *)packet);
  *separator = '\0';

  char *lines[6];
  size_t line_count = 0u;
  char *cursor = (char *)packet;
  while (line_count < 6u) {
    lines[line_count++] = cursor;
    char *newline = strchr(cursor, '\n');
    if (newline == NULL) break;
    *newline = '\0';
    cursor = newline + 1;
  }
  if (line_count != 6u || strchr(cursor, '\n') != NULL ||
      strcmp(lines[0], "gravity-native-runtime-v1") != 0)
    return diagnostic("P15NR003", "packet header schema is not exact");

  const char *prefixes[5] = {"rule-sha256 ", "source-path-hex ", "source-sha256 ",
                             "payload-sha256 ", "instruction-count "};
  char *values[5];
  for (size_t i = 0u; i < 5u; ++i) {
    size_t prefix_length = strlen(prefixes[i]);
    if (strncmp(lines[i + 1u], prefixes[i], prefix_length) != 0)
      return diagnostic("P15NR003", "packet header keys or ordering are invalid");
    values[i] = lines[i + 1u] + prefix_length;
  }
  if (!decode_source_path(values[1]) || !exact_sha(values[2]) || !exact_sha(values[3]))
    return diagnostic("P15NR003", "packet provenance or content hashes are malformed");
  if (!exact_sha(values[0]) || strcmp(values[0], RULE_SHA256) != 0)
    return diagnostic("P15NR004", "packet runtime rule hash does not match the pinned Gravity source");
  char observed_hash[65];
  sha256_hex((const unsigned char *)payload, payload_length, observed_hash);
  if (strcmp(values[3], observed_hash) != 0)
    return diagnostic("P15NR005", "packet payload content hash does not match its bytes");
  size_t instruction_count;
  if (!parse_size(values[4], INSTRUCTION_LIMIT, &instruction_count) || instruction_count == 0u)
    return diagnostic("P15NR002", "declared instruction count is outside the bounded contract");
  return execute_payload(payload, instruction_count);
}

int main(int argc, char **argv) {
  if (argc == 2 && strcmp(argv[1], "--rule-hash") == 0) {
    (void)puts(RULE_SHA256);
    return 0;
  }
  if (argc == 2 && strcmp(argv[1], "--boundary") == 0) {
    (void)printf("{:artifact :gravity/p15-s23-native-runtime-boundary "
                 ":runtime-rule-sha256 \"%s\" "
                 ":source-sha256-declared? true "
                 ":source-content-hash-verified-by-provider? false "
                 ":selected-runtime-clojure-seed-boundary? false "
                 ":compiler-clojure-seed-boundary? true "
                 ":verifier-clojure-seed-boundary? true "
                 ":artifact-construction-clojure-seed-boundary? true "
                 ":process-and-file-io-clojure-seed-boundary? true "
                 ":public-wrapper-clojure-seed-boundary? true "
                 ":clojure-seed-boundary? true :public-command-route? false "
                 ":whole-language? false :formal-language-complete? false "
                 ":self-hosted? false :release-ready? false :seedless-release? false}\n",
                 RULE_SHA256);
    return 0;
  }
  if (argc != 1) return diagnostic("P15NR001", "usage: p15-native-runtime-driver [--rule-hash|--boundary]");
  return run_packet();
}
