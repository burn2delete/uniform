

;; ---------------------------------------------------------------------------
;; Authenticated hosted-C17 toolchain Gate B (FL-P07-T02 bounded slice)
;; ---------------------------------------------------------------------------

;; Gate A above is intentionally pure with respect to the host toolchain.  The
;; lexical authority below is the only route from an authenticated Gate-A
;; carrier to filesystem or process effects.  None of the B3/LLVM authority or
;; command machinery is reused: its diagnostics, target contract, and six-file
;; publication inventory are deliberately backend-specific.

(declare p18-t04-experimental-verified-mir-c-route-record)