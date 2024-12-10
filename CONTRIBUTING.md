# Contributing Guidelines

We love improvements to our tools! There are a few key ways you can help us improve our projects:

### Submitting Feedback, Requests, and Bugs

Our process for submitting feedback, feature requests, and reporting bugs usually begins by discussion on [our chat](http://wiki.kiwix.org/wiki/Communication#Chat) and, after initial clarification, through [GitHub issues](https://help.github.com/articles/about-issues/). Each project repository generally maintains its own set of issues:

        https://github.com/kiwix/<repository-name>/issues

Some projects have additional templates or sets of questions for each issue, which you will be prompted to fill out when creating one.

Issues that span multiple projects or are about coordinating how we work overall are in the [Overview Issue Tracker](https://github.com/kiwix/overview/issues).

### Submitting Code and Documentation Changes

We still do not have [project guidelines](./guidelines/project_guidelines.md) for all of the projects hosted in our [GitHub Organization](https://github.com/kiwix), which new repositories should follow during their creation.

Our process for accepting changes operates by [Pull Request (PR)](https://help.github.com/articles/about-pull-requests/) and has a few steps:

1.  If you haven't submitted anything before, and you aren't (yet!) a member of our organization, **fork and clone** the repo:

        $ git clone git@github.com:<your-username>/<repository-name>.git

    Organization members should clone the upstream repo, instead of working from a personal fork:

        $ git clone git@github.com:kiwix/<repository-name>.git

1.  Create a **new branch** for the changes you want to work on. Choose a topic for your branch name that reflects the change:

        $ git checkout -b <branch-name>

1.  **Create or modify the files** with your changes. If you want to show other people work that isn't ready to merge in, commit your changes then create a pull request (PR) with _WIP_ or _Work In Progress_ in the title.

        https://github.com/kiwix/<repository-name>/pull/new/main

1.  Once your changes are ready for final review, commit your changes then modify or **create your pull request (PR)**, assign as a reviewer or ping (using "`@<username>`") a Lieutenant (someone able to merge in PRs) active on the project (all Lieutenants can be pinged via `@kiwix/lieutenants`)

1.  Allow others sufficient **time for review and comments** before merging. We make use of GitHub's review feature to comment in-line on PRs when possible. There may be some fixes or adjustments you'll have to make based on feedback.

1.  Once you have integrated comments, or waited for feedback, a Lieutenant should merge your changes in!

### Branching

Our branching strategy is based on [this article](https://nvie.com/posts/a-successful-git-branching-model/) which we suggest you read.

+ **main**  the actively worked on next release of the app, what we branch off of while working on new features and what we merge into upon feature completion
+ **feature/** or feature/\<username\>/ any branch under this directory is an actively developed feature, `feature` branches culminate in a PR, are merged and deleted.
 Typically a `feature` branch is off of `main` but in rare scenarios, if there is an issue in production a branch may be made off `main` to fix this issue, this type of `feature` branch must be merged to `main` before being deleted.
Branch names should be in the format **\<issue-number\>-kebab-case-title**

All branches should have distinct history and should be visually easy to follow, for this reason only perform merge commits when merging code either by PR or when synchronising.

If you wish to rebase you should be following the [Golden Rule](https://www.atlassian.com/git/tutorials/merging-vs-rebasing#the-golden-rule-of-rebasing) and adhere to the advice in the heading [Aside: Rebase as cleanup is awesome in the coding lifecycle](https://www.atlassian.com/git/articles/git-team-workflows-merge-or-rebase).

### Committing

For writing commit messages please read the
[COMMITSTYLE](docs/commitstyle.md) carefully. Kindly adhere to the
guidelines. Pull requests not matching the style will be rejected.

### Design and style

For an overview of how to make design changes to Kiwix Android, check out [DESIGN.md](https://github.com/kiwix/kiwix-android/blob/main/DESIGN.md).

### Building

The Kiwix app is split into 3 modules
1. `core` - the "core" functionality of the app shared between different clients
1. `app` - the main app Kiwix, Wikipedia Offline
1. `custom` - this is for building custom applications that supply only 1 ZIM file and a custom skin

The default build is `debug`, with this variant you can use a debugger while developing. To install the application click the `run` button in Android Studio with the `app` configuration selected while you have a device connected. The `release` build is what gets uploaded to the Google Play store and can be built locally with the dummy credentials/keystore provided.

By default we fetch kiwix-lib, the key component for interacting with ZIM files from maven, but if you wish to use your own locally compiled version for testing purposes, then you can create the directory `app/libs` and place your .aar file inside it to have it used instead.

### Git hooks

We've implemented [Git hooks](https://www.atlassian.com/git/tutorials/git-hooks) to ensure code quality and minimize potential errors in the Kiwix project. The [pre-commit](https://github.com/kiwix/kiwix-android/blob/main/team-props/git-hooks/pre-commit.sh) Git hook, automatically runs `detekt`, `ktlint`, and `lint` with every commit.
If this hook identifies any potential errors or code quality issues, it will fail the commit and provide the reason for the failure. You need to fix that error and recommit your code.

However, if there's a valid reason to skip linting for a particular commit, you can use:
```bash
git commit --no-verify
```

### Linting

PR should be linted properly locally. The linter is a git hooks and always run automatically.

If you have a good reason to ignore it, please run:
```bash
git commit --no-verify
```

### Testing

Unit tests are located in `[module]/src/test` and to run them locally you
can use the gradle command:

        $ gradlew testDebugUnitTest

or the abbreviated:

        $ gradlew tDUT

Automated tests that require a connected device (UI related tests) are located in `[module]/src/androidTest` & `app/src/androidTestKiwix`, to run them locally you can use the gradle command:

        $ gradlew connectedDebugAndroidTest

or the abbreviated:


        $ gradlew cDAT

All local test results can be seen under `[module]/build/reports/`

### Code coverage

To generate coverage reports for your unit tests run:

        $ gradlew jacocoTestReport

To generate coverage reports for your automated tests run:

        $ gradlew jacocoInstrumentationTestReport

Code coverage results can be seen under `[module]/build/reports/`

### Continuous Integration

All PRs will have all these tests run and a combined coverage report will be attached, if coverage is to go down the PR will be marked failed. On Travis CI the automated tests are run on an emulator. To
learn more about the commands run on the CI please refer to [.github/workflows](https://github.com/kiwix/kiwix-android/tree/main/.github/workflows).


_These guidelines are based on [Tools for Government Data Archiving](https://github.com/edgi-govdata-archiving/overview/blob/master/CONTRIBUTING.md)'s._

## Code Style

For contributions please read the [CODESTYLE](docs/codestyle.md)
carefully. Pull requests that do not match the style will be rejected.

### Localization

Kiwix Android is available in multiple languages. Translations of
string resources are managed by
[Translatewiki.net](https://translatewiki.net).

When adding new strings to the code base (menu entries, warnings,
dialog, ...), developers are required to do it using a string
resource.

When adding a string resource to the code base, developers
are required to provide additional context on how and where the new
string will be used. This helps translators to understand the context
and translate accurately.

[More information about the Kiwix project in
Translatewiki.net](https://translatewiki.net/wiki/Translating:Kiwix).

#### String resources

Each user-visible string in Kiwix Android should be listed in
`strings.xml ` files to allow proper translation, replacement and
modification.

Newly added string resources are imported by Translatewiki.net every
few days and and then manually translated by a community of
volunteers.

Every few days, Translatewiki.net pushes automatically the new
translations in Kiwix Android git repository via a PR.

##### Howto by example

Start by adding your new string resource `new_string` to
`values/strings.xml` in English. That is:
```
...
<string name="new_string">New String</string>
...

```

You will now have to describe the string in `values-qq/strings.xml`
with where and how the new string is used. E.x. for the string
`<string name="on">On</string>`: `values-qq/strings.xml:`

```
...
<string name="on">This is used in the settings screen to turn on the dark mode.</string>
...
```

It's important to notice that:

- The values in `values/strings.xml` are the strings that are going to
  be displayed in the Kiwix application to the user.

- The values in `values-qq/strings.xml` are only visible to the
  translator and are only there to help them make a correct
  translation.
