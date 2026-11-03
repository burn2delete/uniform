# Current-v2 fresh Core construction port

## Purpose and scope

This report records the smallest current-main port of useful PR603 behavior.
The current 00724a constructor already owns the selected v2
:development-current source binding, Reader and expression owner calls,
source bookends, sealed-result ordering, replay and plan-free metadata
projection. The port therefore adds only a focused current-v2 regression
harness and this evidence record.

Public C6 v3 construction remains refused by the existing generic
gravity.source-interface-header contract. This change does not enable v3,
renew the historical flat-bool source record, revive the old 00740 source
policy, or replace the current 00724a owner bridge.

## Historical PR603 accounting

The original PR603 was
d561fa9926ac15d25e3b85caafaada63db7056ae, tree
f731a643da86e351492eeff25c453a081cf342ce, over authored parent
cfe934c95a61241ad337f500a08222f79f047f61, tree
3de9da6f7b0700fd931bad9efcd9872e463de6bf. Its six authored paths were:

- 00723: targeted unused-private annotation removal;
- 00724: versioned constructor and public-header dispatch;
- 00740: duplicate Bool-family require removal;
- gravity/c6_c10/source_interface.clj: v2/v3 successor dispatch;
- the original versioned-construction report; and
- tools/c6_fresh_construction_test.clj: six synthetic tests / 61
  assertions.

Those production hunks were not replayed. Current main already contains the
v2 owner bridge and its actual C6/C11 composition. The old v3-positive
oracles and fixed historical source route would be incorrect on current main.
The original six-test result is retained as historical author evidence only,
not as current-port evidence.

## Current port behavior

The focused current-v2 harness covers:

- explicit v2 dispatch to the current source owner with
  :development-current, followed by the current Reader and expression
  owners;
- both public v2 entry shapes (validated context/header and
  path/text/target/header), including Reader-path and source-text forwarding;
- Reader source path/version and private-ingress argument forwarding;
- removal of private plans and function manifests from the public binding and
  expression projection;
- source mutation refusal before builder execution and after replay;
- current-v2 rejection of a wrong semantic Core family and a synthetic opaque
  expression callback at the final trusted-carrier seam;
- upstream Reader refusal before builder, sealing, replay and publication;
- sealed-result refusal before replay and public artifact publication;
- v3, native integer-version, legacy-header and foreign-interface refusal
  before the fresh constructor is called;
- positive legacy v1 context and path/text/target entrypoints without the
  current Reader/expression policy route; and
- restoration of the dynamic expression binding after successful and failed
  construction attempts, plus the source-policy receipt's interface,
  admission and self-hosting fields.

The test uses synthetic owner seams for products and replay ordering. It does
not substitute those seams for the existing current-main C6/C11 production
evidence. The callback refusal proves that the constructor reaches and does
not bypass the final carrier seam, but is not an independent authentication
result.

## Focused evidence

Command:

    clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.c6-fresh-construction-current-test

Observed: 8 tests, 71 assertions, 0 failures, 0 errors. git diff --check
also passes for the candidate.

The current-main C6/C11 source-authentication and genuine replay results
remain separately owned evidence. No new native operation, v3 product,
public report completion, PR552 completion, release, self-hosting or
seed-retirement claim follows from this harness.
