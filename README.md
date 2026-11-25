# Hard Client v7

![Hard Client logo](https://img.wimods.net/github.com/Hard-Imperium/Hard7?to=https://hard.wiki/_media/logo/hard_758x192.webp)

- **Downloads:** [https://www.hardclient.net/download/](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fwww.hardclient.net%2Fdownload%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo)

- **Installation guide:** [https://www.hardclient.net/tutorials/how-to-install/](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fwww.hardclient.net%2Ftutorials%2Fhow-to-install%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo)

- **Feature list:** [https://www.hardclient.net/](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fwww.hardclient.net%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo)

- **Wiki:** [https://hard.wiki/](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fhard.wiki%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo)

- **Forum:** [https://hardforum.net/](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fhardforum.net%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo)	

- **Twitter/X:** [https://x.com/Hard_Imperium](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https://x.com/Hard_Imperium)

- **YouTube:** [https://www.youtube.com/@Alexander01998](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https://www.youtube.com/@Alexander01998)

- **Donations/Perks:** [https://ko-fi.com/hard](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https://ko-fi.com/hard)

## Installation

Hard 7 can be installed just like any other Fabric mod. Here are the basic installation steps:

1. Run the Fabric installer.
2. Add the Hard Client and Fabric API to your mods folder.

Please refer to the [full Hard 7 installation guide](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fwww.hardclient.net%2Ftutorials%2Fhow-to-install%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo) if you need more detailed instructions or run into any problems.

Also, this should be obvious, but you do need to have a licensed copy of Minecraft Java Edition in order to use Hard. Hard is a cheat client, not a pirate client.

## Development Setup

> [!IMPORTANT]
> Make sure you have [Java Development Kit 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot) installed. It won't work with other versions.

### Development using Eclipse

1. Clone the repository:

   ```pwsh
   git clone https://github.com/Hard-Imperium/Hard7.git
   cd Hard7
   ```

2. Generate the sources:

   ```pwsh
   ./gradlew genSources eclipse
   ```

3. In Eclipse, go to `Import...` > `Existing Projects into Workspace` and select this project.

4. **Optional:** Right-click on the project and select `Properties` > `Java Code Style`. Then under `Clean Up`, `Code Templates`, `Formatter`, import the respective files in the `codestyle` folder.

### Development using VSCode / Cursor

> [!TIP]
> You'll probably want to install the [Extension Pack for Java](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fmarketplace.visualstudio.com%2Fitems%3FitemName%3Dvscjava.vscode-java-pack) to make development easier.

1. Clone the repository:

   ```pwsh
   git clone https://github.com/Hard-Imperium/Hard7.git
   cd Hard7
   ```

2. Generate the sources:

   ```pwsh
   ./gradlew genSources vscode
```

3. Open the `Hard7` folder in VSCode / Cursor.

4. **Optional:** In the VSCode settings, set `java.format.settings.url` to `https://raw.githubusercontent.com/Hard-Imperium/Hard7/master/codestyle/formatter.xml` and `java.format.settings.profile` to `Hard-Imperium`.

### Development using IntelliJ IDEA

I don't use or recommend IntelliJ, but the commands to run would be:

```pwsh
git clone https://github.com/Hard-Imperium/Hard7.git
cd Hard7
./gradlew genSources idea --no-configuration-cache
```

**Note:** IntelliJ IDEA is [not yet compatible](https://github.com/FabricMC/fabric-loom/issues/1349) with Gradle's configuration cache. You will run into issues.

Possible workarounds:
- Turn off args files ([this setting](https://i.imgur.com/zHqIOYg.png)). Won't work for some users because of a command length limit.
- Add `--no-configuration-cache` to all of your Gradle commands.

## Contributing

Please always [contact me](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https%3A%2F%2Fwww.hardclient.net%2Fcontact%2F%3Futm_source%3DGitHub%26utm_medium%3DHard7%2Brepo) before opening a Pull Request. Any method works. That way we can discuss your ideas early and avoid wasting your time working on unwanted features or having to make lots of changes later.

We also have [contributing guidelines](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https://github.com/Hard-Imperium/Hard7/blob/master/CONTRIBUTING.md) to help you get started.

## Translations

To enable translations in-game, go to Hard Options > Translations > ON.

The preferred way to submit translations is through a Pull Request here on GitHub. The translation files are located in [this folder](https://go.wimods.net/from/github.com/Hard-Imperium/Hard7?to=https://github.com/Hard-Imperium/Hard7/tree/master/src/main/resources/assets/hard/translations).

Names of features (hacks/commands/etc.) should always be kept in English. This ensures that everyone can use the same commands, keybinds, etc. regardless of their language setting. It also makes it easier to communicate with someone who uses Hard in a different language.

## License

This code is licensed under the GNU General Public License v3. **You can only use this code in open-source clients that you release under the same license! Using it in closed-source/proprietary clients is not allowed!**
