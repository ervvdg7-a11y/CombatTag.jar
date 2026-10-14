# CombatTag (GitHub Actions)

Paper 26.3 combat-tag plugin project.

## Automatic build
- Every push to `main` or `master`, pull request, or manual workflow run builds the plugin.
- The resulting `target/CombatTag.jar` is uploaded as a GitHub Actions artifact named `CombatTag`.
- Pushing a tag such as `v1.0.0` also creates a GitHub Release with `CombatTag.jar` attached.

## Add this project to GitHub Desktop
1. Download and extract this ZIP.
2. In GitHub Desktop, choose **File → Add Local Repository** if this folder is already a repository, or clone your GitHub repository and copy these files into the cloned folder.
3. Ensure `.github/workflows/build.yml` is included. On Windows, folders beginning with a dot may be hidden.
4. Commit all files and click **Push origin**.
5. On GitHub, open **Actions** and select **Build CombatTag**. Wait for the run to finish green.
6. Open the completed run and download the `CombatTag` artifact. Extract it to get `CombatTag.jar`.

## Create a GitHub Release
In GitHub Desktop, create a tag named `v1.0.0`, then push the tag to origin. The workflow will build the JAR and attach it to the release. You can also create/push a tag in GitHub's web interface.

## Requirements and note
The workflow uses Java 25 and Maven on GitHub-hosted runners. Paper's current Maven version range for 26.3 is configured in `pom.xml`.
Test the plugin on a backup/test server before using it on a live server, especially if other plugins alter death or inventory drops.
