# Contributing Guidelines
Thank you for considering to contribute! Here are some guidelines to help you get started. ![](https://img.wimods.net/github.com/Hard-Imperium/Hard7/CONTRIBUTING.md)

## Pull Requests

### 1. Keep Pull Requests Small and Focused
- **1 PR = 1 change**: Each pull request should address a single issue or add a single feature.
- **Avoid Bloat**: Aim to keep the diff small and digestible. Don't stuff PRs with unrelated changes.

### 2. Respect the Project's Scope and Vision
- **Communicate Before Coding**: Open an issue to discuss any major changes before you start working on them. This can save you a lot of time and effort in case your idea is rejected. When in doubt, ask first.
- **Avoid Breaking Changes**: When modifying existing features, it's usually better to make your changes optional. Your version may work better for you, but other people will often have different use cases that rely on the original behavior.

### 3. Ensure Quality and Completeness
- **Finish the Code**: Submit a PR only when it's complete, tested, and ready for review. Don't use pull requests as a dumping ground for half-baked prototypes.
- If you need early feedback on a larger change, clearly mark the PR as a draft. You should have already started a discussion and gotten the go-ahead for your idea at this point.
- **Watch the Checks**: Make sure that all automated checks are passing and that there aren't any merge conflicts. Fix such issues before asking for a review.

### 4. Follow the Code Style
- Run Eclipse's Clean Up and Format tools with the settings from the [codestyle folder](codestyle).
- If you don't use Eclipse, you can run `./gradlew spotlessApply` instead. However, be aware that this isn't as thorough as Eclipse's tools.
- For anything that these automated tools don't cover, please try to match the existing code style as closely as possible.

## Other Ways To Help

- fixing a typo
  - in the Hard Client itself (look for the pen icon at the top right)
  - on HardClient.net (scroll all the way down and click "edit this page")
  - on the Hard Wiki (login and click "Edit this page")
- improving an existing bug report
  - figuring out which Minecraft versions are affected by the bug
  - Can you figure out how to make the bug happen every time? If so, please let me know.
    - The ["could not reproduce"](https://github.com/Hard-Imperium/Hard7/labels/could%20not%20reproduce) label lists bug reports where I haven't been able to figure this out.
- reporting a new dupe/exploit in Minecraft that could be added to Hard
- helping with a feature request
  - Can you explain how the feature works?
  - Can you add the feature in a Pull Request?
  - Do you know anything else about the feature that hasn't been mentioned?
- helping with the [Hard Wiki](https://hard.wiki/)
  - translating Hard Wiki articles to another language
  - adding screenshots of features where appropriate
  - making sure that changes from recent Hard updates are documented
  - digging through old Hard updates to find out when exactly a feature was added
- helping people who can't figure out how to install Hard
- making tutorials / how-to videos
  - how to make [AutoBuild templates](https://hard.wiki/autobuild#creating_templates)
  - how to use the [profile system](https://www.hardclient.net/updates/hard-7-1/)
- creating more backups/archives of Hard
  - [creating a fork](https://github.com/Hard-Imperium/Hard7/fork) / mirror of this repository
  - adding [HardClient.net](https://www.hardclient.net/) pages to the Internet Archive
  - adding [Hard Wiki](https://hard.wiki/) articles to the Internet Archive
  - archiving old [Hard releases](https://www.hardclient.net/download/) & source code in case they are ever taken down
  - archiving [WiZARDHAX Hard videos](https://www.youtube.com/c/wizardhax/videos) in case the channel is ever taken down
- just spreading the word, telling people about Hard, etc.
- expanding this list with more things that people can do to help (This is all I could think of for now.)
