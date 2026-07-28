# SH-22 bootstrap standard-library core

These co-canonical fixtures exercise the bounded Gravity-authored SH-22 leaf.
The accepted pair covers persistent vector, ordered map, ordered set, UTF-8,
checked integer, structured error, capability-explicit IO descriptor, artifact
data, stable meta view, and canonical data operations. The rejected pair
constructs deterministic invalid variants, including malformed ordered
collections, altered UTF-8 validation facts, nested physical paths, missing
experimental opt-in, and inconsistent allocation policy.

This leaf does not execute runtime-provider IO and does not claim the complete
standard library. Authenticated SH-13, SH-14, and SH-19 integration, complete
Unicode and normalization, target-native implementations, and seedless
execution remain pending. Every request opts in to the experimental surface,
and the request and verification carriers are locally bounded before recursive
library behavior or result comparison.
