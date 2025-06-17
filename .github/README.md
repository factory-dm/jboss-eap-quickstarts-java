# GitHub Actions Workflows for *jboss-eap-quickstarts-java*

This repository ships with a set of GitHub Actions workflows that automate the most common CI tasks—building, testing, linting and auditing code.  
Use this document as a quick reference for what each workflow does, when it runs and how you can tweak it for your own needs.

---

## 1. Workflow Overview

| Workflow file | Job name (shown on GitHub)          | Purpose |
|---------------|-------------------------------------|---------|
| `.github/workflows/reduce_readme.yml` | **Update READMEs** | Flattens every `README-source.adoc` into a single `README.adoc` for nicer GitHub display. |
| `.github/workflows/build.yml` | **Build and Test** | Compiles all Maven modules and executes the unit tests on Java 11 & 17. |
| `.github/workflows/code-quality.yml` | **Code Quality** | Runs Checkstyle and SpotBugs, uploads the XML reports as artifacts. |
| `.github/workflows/dependency-check.yml` | **Dependency Check** | Executes OWASP Dependency-Check and publishes the HTML / XML / JSON reports. |
| `.github/workflows/yaml-lint.yml` | **YAML Lint** | Lints all YAML/YML files with `yamllint` using the repo-provided config. |
| `.github/workflows/pull-request.yml` | **Pull Request Checks** | Adds labels, performs a quick compile and posts a status comment on every PR. |

---

## 2. Detailed Workflow Descriptions

### 2.1 Update READMEs (`reduce_readme.yml`)
* **What it does** – Uses `asciidoctor-reducer` to generate flattened `README.adoc` files for every quickstart, then commits the results back to the branch.  
* **Triggers** – `push` to the `8.0.x` branch only.  
* **Notes** – Concurrency is enabled so only the newest run stays active.

### 2.2 Build and Test (`build.yml`)
* **What it does**
  * Checks out the code.
  * Sets up a Java matrix (`11`, `17`) with Maven cache.
  * Runs `mvn clean install -DskipTests` followed by `mvn test`.
  * Uploads all produced WAR/JAR artefacts.
* **Triggers**
  * `push` and `pull_request` on `main`, `master`, or version branches such as `8.0.x`.
  * `workflow_dispatch` for manual runs.
* **Manual trigger** – Click the **Actions → Build and Test → Run workflow** button; optionally change the branch or Java version matrix.

### 2.3 Code Quality (`code-quality.yml`)
* **What it does**
  * Executes Checkstyle (`mvn checkstyle:check`) and SpotBugs.
  * Uploads Checkstyle & SpotBugs XML reports for download.
* **Triggers** – Same set as Build and Test (`push`, `pull_request`, `workflow_dispatch`).
* **Manual trigger** – Same as above.

### 2.4 Dependency Check (`dependency-check.yml`)
* **What it does**
  * Runs OWASP Dependency-Check in aggregate mode against every module.
  * Uses `.github/dependency-check-suppressions.xml` to filter false positives.
  * Publishes HTML / XML / JSON reports as artifacts.
  * Emits a warning if **High/Critical** vulnerabilities are detected.
* **Triggers**
  * **Scheduled**: every Sunday 00:00 UTC (`cron: '0 0 * * 0'`).
  * `workflow_dispatch` on demand.
* **Manual trigger** – Same pattern; useful after adding new dependencies.

### 2.5 YAML Lint (`yaml-lint.yml`)
* **What it does** – Installs `yamllint`, then lints all `.yml`/`.yaml` files with the project's config.  
* **Triggers**
  * `push` and `pull_request` that touch any YAML file or workflow file.
  * `workflow_dispatch` available.

### 2.6 Pull Request Checks (`pull-request.yml`)
* **What it does**
  * Automatically labels the PR using `.github/labeler.yml`.
  * Performs a fast Maven compile (`mvn clean compile -DskipTests`) to catch obvious issues.
  * Performs heuristic file checks (e.g. source changes without tests) and posts a summarising comment.
* **Triggers** – For every PR event (`opened`, `synchronize`, `reopened`) targeting `main`, `master`, or version branches.

---

## 3. How Workflows Are Triggered

| Event type | Workflows |
|------------|-----------|
| `push` (branch) | Build & Test, Code Quality, YAML Lint (YAML-only), Update READMEs (8.0.x only) |
| `pull_request` | Build & Test, Code Quality, YAML Lint, Pull Request Checks |
| `schedule` | Dependency Check (weekly) |
| `workflow_dispatch` | All workflows provide **Run workflow** button for manual execution |

---

## 4. Manually Triggering a Workflow

1. Go to the **Actions** tab.  
2. Select the workflow you want to run.  
3. Click **Run workflow**.  
4. Pick a branch (and, in some cases, tweak inputs), then hit **Run workflow**.  

This is handy when:
- You changed workflow logic and want to verify it.
- You need an immediate dependency audit instead of waiting for the weekly schedule.

---

## 5. Customising the Workflows

| What to customise | How |
|-------------------|-----|
| **Branches** | Edit the `branches:` filter in each workflow's `on:` block. |
| **Java versions** | Modify the `matrix.java` list in `build.yml`. |
| **Checkstyle rules** | Tweak the `<configuration>` in the root `pom.xml`. |
| **SpotBugs thresholds** | Adjust `-Dspotbugs.threshold` in `code-quality.yml`. |
| **Dependency Check frequency** | Change the `cron` expression in `dependency-check.yml`. |
| **YAML lint rules** | Edit `.github/yamllint.config`. |
| **PR label patterns** | Modify `.github/labeler.yml` to suit your directory structure. |
| **Artifact retention** | Update the `retention-days` field in any `upload-artifact` step. |

---

## 6. Adding New Workflows

1. Create a new file under `.github/workflows/your-workflow.yml`.  
2. Follow GitHub-Actions [workflow syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions).  
3. Use `actions/setup-java@v3` for Java builds and enable the `cache: 'maven'` option.  
4. Commit & push – GitHub automatically recognises and starts using the new workflow.

---

Enjoy automated builds and safer dependencies! If you have questions or suggestions, open an issue or start a discussion in the repository.  
Happy coding 🎉
