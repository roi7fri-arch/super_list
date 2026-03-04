# Branch Protection and Review Policy

## Required protections

- Protect the default branch from direct pushes.
- Require pull requests before merge.
- Require at least one approval from a code owner or designated reviewer.
- Require all quality workflows to pass:
  - API Quality
  - Android Quality
  - iOS Quality

## Required checks

- Lint checks must pass.
- Type/compile checks must pass.
- Contract and test checks are required in implementation phases.

## Exceptions

- Any temporary bypass requires maintainer approval, expiry date, and follow-up task.
