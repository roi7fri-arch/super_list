# Regression Test Policy

Every production defect fix MUST include:

1. A failing automated test that reproduces the defect.
2. A code change that makes the new test pass.
3. Verification that all related regression suites remain green.

## Required mapping

- Link defect/ticket ID in the test name or test comment.
- Keep reproduction fixtures deterministic.
- Add cross-platform coverage when behavior affects both iOS and Android.

## Minimum evidence

- CI link with failing test before fix (or equivalent local captured output).
- CI link with passing test after fix.
- Updated release notes when user-facing behavior changed.
