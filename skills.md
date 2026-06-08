# Skills: Merge Upstream Tag into Branch & Raise PR (DCO-Safe)

A step-by-step guide to merge an upstream release tag into your fork branch and raise a PR without DCO failures.

---

## Prerequisites

- Your fork is set up with `origin` (your fork) and `upstream` (the original repo) remotes.
- Your git user config is set correctly:
  ```bash
  git config user.name "YourName"
  git config user.email "youremail@example.com"
  ```

---

## Step 1 — Verify Remotes

```bash
git remote -v
```

Expected:
```
origin    https://github.com/<your-username>/<repo>.git (fetch/push)
upstream  https://github.com/<org>/<repo>.git (fetch/push)
```

If `upstream` is missing:
```bash
git remote add upstream https://github.com/<org>/<repo>.git
```

---

## Step 2 — Fetch Upstream and the Tag

```bash
git fetch upstream
```

This fetches all branches and tags from upstream. Verify the tag exists:
```bash
git log --oneline <tag-name> -5
# Example: git log --oneline v1.3.1-rc.1 -5
```

---

## Step 3 — Check Out Your Branch

```bash
git checkout <your-branch>
# Example: git checkout develop-#1830
```

---

## Step 4 — Start the Merge

```bash
git merge <tag-name> --no-ff
# Example: git merge v1.3.1-rc.1 --no-ff
```

> `--no-ff` forces a merge commit so history is clear about what was merged.

---

## Step 5 — Resolve Conflicts (if any)

List conflicted files:
```bash
git diff --name-only --diff-filter=U
```

For each conflict, decide:
- **Take upstream version** (recommended when bringing in a release tag):
  ```bash
  git checkout --theirs <file>
  git add <file>
  ```
- **Take your version:**
  ```bash
  git checkout --ours <file>
  git add <file>
  ```
- **Manually edit** the file to combine both sides, then:
  ```bash
  git add <file>
  ```

Common conflict patterns when merging a release tag:
| File | Typical Resolution |
|------|-------------------|
| `pom.xml` | Take upstream (release version over SNAPSHOT) |
| `README.md` | Take upstream |
| `helm/*/values.yaml` | Take upstream |
| Test files (add/add) | Take upstream |
| Custom source files | Manual merge |

---

## Step 6 — Squash into ONE DCO-Safe Commit

> **Important:** Do NOT commit the merge normally if the upstream has contributors with missing/wrong DCO signoffs. Squash everything into a single commit signed by you instead.

Find the merge base with the target branch (e.g., `develop`):
```bash
git merge-base HEAD upstream/develop
# Note the commit SHA
```

Soft-reset to that SHA (keeps all changes staged):
```bash
git reset --soft <merge-base-sha>
```

Create a single squashed commit with DCO sign-off (`-s` adds `Signed-off-by` automatically):
```bash
git commit -s -m "Merge all changes from upstream <repo> <tag-name>

Brief description of what this tag contains.

Signed-off-by: Your Name <youremail@example.com>"
```

---

## Step 7 — Push and Raise PR

Push your branch:
```bash
git push origin <your-branch>
# If re-pushing after a squash:
git push --force-with-lease origin <your-branch>
```

Raise the PR via GitHub UI:
```
https://github.com/<org>/<repo>/compare/<base-branch>...<your-username>:<your-branch>
```

Or via `gh` CLI (if installed):
```bash
gh pr create \
  --repo <org>/<repo> \
  --head <your-username>:<your-branch> \
  --base <base-branch> \
  --title "Merge upstream <tag-name> changes into <base-branch>" \
  --body "Merges all changes from <tag-name>. Squashed into a single signed commit for DCO compliance."
```

---

## DCO Troubleshooting

### Why DCO fails on upstream commits
Upstream contributors may have signed with wrong emails, GitHub no-reply addresses, or no signoff at all. The DCO bot checks **every commit** in the PR.

### Fix: squash into one commit (recommended)
Covered in Step 6 above. Only your single commit needs DCO sign-off.

### Fix: rebase with signoff (alternative — rewrites history)
```bash
git rebase HEAD~<N> --signoff    # N = number of commits in PR
git push --force-with-lease origin <your-branch>
```
> Use this only if you are the sole author on the branch and nobody else is working from it.

### Verify your commit has sign-off
```bash
git log -1 --format="%B"
# Should contain: Signed-off-by: Your Name <youremail@example.com>
```

---

## Quick Reference Checklist

- [ ] `upstream` remote is configured
- [ ] `git fetch upstream` run
- [ ] Tag exists: `git log --oneline <tag> -5`
- [ ] On the correct branch
- [ ] `git merge <tag> --no-ff`
- [ ] All conflicts resolved and staged
- [ ] Found merge base: `git merge-base HEAD upstream/<base>`
- [ ] `git reset --soft <merge-base-sha>`
- [ ] `git commit -s -m "..."` (single squashed, signed commit)
- [ ] `git push --force-with-lease origin <branch>`
- [ ] PR raised targeting the correct base branch
