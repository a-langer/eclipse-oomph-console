# Console Oomph Installer

[![license](https://img.shields.io/badge/license-EPL2-brightgreen.svg)](https://github.com/a-langer/eclipse-oomph-console/blob/main/LICENSE "License of source code")
[![p2](https://img.shields.io/badge/p2-latest-blue?style=flat)](https://a-langer.github.io/eclipse-oomph-console-site/ "P2 plugin repository")
[![marketplace](https://img.shields.io/badge/marketplace-latest-blue?style=flat)](https://marketplace.eclipse.org/content/console-oomph-installer "Eclipse marketplace")
[![JitPack](https://jitpack.io/v/a-langer/eclipse-oomph-console.svg)](https://jitpack.io/#a-langer/eclipse-oomph-console "Maven repository for builds from source code")
[![Maven](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/org.eclipse.oomph.console.product/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/org.eclipse.oomph.console.product "Maven repository for stable builds")

Console version of Oomph installer (also called [Eclipse installer](https://wiki.eclipse.org/Eclipse_Installer)). Implements a headless installation mode from command line. This project base on patch [66353][1] of Mikael Karlback.

Available solutions:

* [Standalone Oomph console installer](https://search.maven.org/search?q=a:org.eclipse.oomph.console.product) (plain Oomph with console plugin installed).
* [Oomph console plugin for Eclipse](https://marketplace.eclipse.org/content/console-oomph-installer) (can be installed on any Eclipse product from [p2 repository](https://a-langer.github.io/eclipse-oomph-console-site/)).

Typical use cases:

* Installation Eclipse with an exact set of features/plugins and with certain preferences.
* Automation the process of installing and updating Eclipse (using CI\CD).

## Supported features and examples of usage

* Use a console version of Oomph installer; you can download it either from [Standalone Oomph console installer](https://search.maven.org/search?q=a:org.eclipse.oomph.console.product) (choose the appropriate download for your target platform) or with mvn command. This eclipse installer is provided with `org.eclipse.oomph.console.application` application to handle command line installations:

  ```bash
  # Check Oomph console version
  # Linux
  mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:unpack -Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:LATEST:tar.gz:linux.gtk.x86_64 -DoutputDirectory=./ -Dproject.basedir=./
  # MacOS x86_64
  mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:unpack -Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:LATEST:tar.gz:macosx.cocoa.x86_64 -DoutputDirectory=./ -Dproject.basedir=./
  # Windows (powershell)
  mvn "org.apache.maven.plugins:maven-dependency-plugin:3.3.0:unpack" "-Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:LATEST:zip:win32.win32.x86_64" "-DoutputDirectory=./" "-Dproject.basedir=./"

  # Linux/MacOS binary
  ./eclipse-installer/eclipse-inst
  # Windows binary
  ./eclipse-instc.exe
  # Following examples may suit a default unix shell; adapt command and argument quoting as needed by your platform/shell
  ```

* Install Eclipse [product][6] such as "Eclipse IDE for Java Developers" or "Eclipse IDE for PHP Developers", ex.:

  ```bash
  # Install "Eclipse IDE for Java Developers"
  ./eclipse-inst -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java"

  # Install "Eclipse IDE for PHP Developers"
  ./eclipse-inst -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.php"
  ```

* Install Eclipse [project][7], such as "SWTBot testing tool" or "Model Workflow Engine" (see [list of projects](https://projects.eclipse.org/)), ex.:

  ```bash
  # Install "SWTBot testing tool" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="swtbot" \
    -Doomph.workspace.location="$PWD/workspace"

  # Install "Model Workflow Engine" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="MWE" \
    -Doomph.workspace.location="$PWD/workspace"
  ```

* Install multiple projects over product, ex.:

  ```bash
  # Install "SWTBot testing tool", "Business Intelligence Reporting Tool" and "Model Workflow Engine" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="swtbot,birt,MWE" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installer.layout="text"
  ```

* Install product and project from [custom setup model](./org.eclipse.oomph.console.product/setups), ex.:

  ```bash
  # Install "Bash Editor" over "Eclipse IDE for Java Developers" from setup model located in directory "$PWD/setups/"
  ./eclipse-inst -vmargs \
    -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/" \
    -Doomph.redirection.setupsDir="index:/->$PWD/setups/" \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="bash.editor" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installer.verbose=true
  ```

* Install product and project from Oomph [Configuration][4], ex.:

  ```bash
  # Install "Business Intelligence Reporting Tool" over "Eclipse IDE for Eclipse Committers" from Configuration
  ./eclipse-inst -vmargs \
    -Doomph.configuration.setups="https://raw.githubusercontent.com/eclipse/birt/master/build/org.eclipse.birt.releng/BIRTConfiguration.setup" \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installer.verbose=true
  ```

* TODO Update Eclipse product and projects (not implemented yet, see [1](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.p2/src/org/eclipse/oomph/setup/p2/impl/P2TaskImpl.java), [2](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.ui/src/org/eclipse/oomph/setup/ui/SetupUIPlugin.java), [3](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.doc/src/org/eclipse/oomph/setup/doc/user/wizard/DocUpdateWizard.java)).

## Settings

The Console Oomph Installer settings (see also [Eclipse runtime options](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html)):

* `oomph.product.id` (string, default `null`) - identifier of product in setup model, ex.:

  ```bash
  # For product with latest version
  -Doomph.product.id="epp.package.java"
  # Or set the specific version for product
  -Doomph.product.id="epp.package.java:latest"
  ```

* `oomph.product.version` (string, default `latest`) - version of product in setup model, if not defined in `oomph.product.id`, ex.:

  ```bash
  -Doomph.product.version="latest"
  ```

* `oomph.installation.location` (string, default `null`) - location of product installation, ex.:

  ```bash
  -Doomph.installation.location="$PWD/ide"
  ```

* `oomph.installation.id` (string, default specific for each product) - directory in `oomph.installation.location`, ex.:

  ```bash
  -Doomph.installation.id="my-inst-dir"
  ```

* `oomph.project.id` (string, default `null`) - identifier of project in setup model, ex.:

  ```bash
  # For single project with default stream
  -Doomph.project.id="swtbot"
  # Or set the specific stream for project
  -Doomph.project.id="swtbot:master"

  # For multiple projects with default streams
  -Doomph.project.id="swtbot,birt,MWE"
  # Or set the specific streams for multiple projects
  -Doomph.project.id="swtbot:master,birt:master,MWE:master"
  ```

* `oomph.project.stream` (string, default `master`) - stream of all projects in setup model, if not defined in `oomph.project.id` ex.:

  ```bash
  -Doomph.project.stream="master"
  ```

* `oomph.installer.layout` (**since 1.0.3**, string, default `progress`) - output layout mode, can be "`progress`" or "`text`", ex.:

  ```bash
  -Doomph.installer.layout=text
  ```

* `oomph.installer.verbose` (boolean, default `false`) - enable verbose output instead of progress bar, ex.:

  ```bash
  -Doomph.installer.verbose=true
  ```

* `oomph.installer.ssl.insecure` (**since 1.0.2**, boolean, default `false`) - disable check of public key certificates (e.g. self-signed ones), ex.:

  ```bash
  -Doomph.installer.ssl.insecure=true
  ```

* `oomph.workspace.location` (string, default `oomph.installation.location`) - project workspace location, ex.:

  ```bash
  -Doomph.workspace.location="$PWD/my-workspace"
  ```

* `oomph.redirection.setups` - location of basic setup model (contain org.eclipse.setup file), will be added to eclipse.ini as "oomph.redirection.index.redirection", ex.:

  ```bash
  # Setup model from URL
  -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/"
  # Setup model from directory
  -Doomph.redirection.setups="index:/->$PWD/my-custom-setups/"
  ```

* `oomph.redirection.<some_id>` overrides - location of additional setup model (also contain org.eclipse.setup file), uses for override basic setup model location, ex.:

  ```bash
  # Basic setup model location
  -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/"
  # Additional setup model location (overrides product and project catalog)
  -Doomph.redirection.setupsDir="index:/->$PWD/my-custom-setups/"
  ```

* `oomph.redirection.<some_id>` additions - location of additional setup model (without org.eclipse.setup file), uses to provides you own setup without altering eclipse Oomph base setup (redirect empty redirectable.products.setup and/or redirectable.projects.setup from default config to you own definitions):

  ```bash
  # Basic setup model location
  -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/"
  # Additional products setup models location
  -Doomph.redirection.productsCatalog="index:/redirectable.products.setup->$PWD/my-custom-products/products.setup"
  # Additional projects setup models location
  -Doomph.redirection.projectsCatalog="index:/redirectable.projects.setup->$PWD/my-custom-projects/projects.setup"
  ```

* `oomph.configuration.setups` (**since 1.0.3**, string, default `null`) - location of Oomph [Configuration][4], ex.:

  ```bash
  # From file
  -Doomph.configuration.setups="$PWD/my-custom-setups/Configuration.setup"
  # From URL
  -Doomph.configuration.setups="https://<hostname>/setups/Configuration.setup"
  ```

  > **_NOTE:_** When combined with `oomph.redirection.<some_id>` product\projects must be from the same setups model as specified in Configuration for the `productVersion` or `stream`.

* `setup.p2.agent` (string, default `null`) - directory location of shared pool for features/plugins, ex.:

  ```bash
  # From command line
  -Dsetup.p2.agent="$HOME/.p2"
  # From eclipse.ini
  -Dsetup.p2.agent="@user.home/.p2"
  ```

* `eclipse.p2.mirrors` (boolean default `false`) - use repository mirrors.
* `oomph.setup.offline` (boolean default `true`) - use local cache.
* `user.home` and `oomph.setup.user.home.redirect` - change user home directory location (for testing), ex.:

  ```bash
  -Duser.home="$PWD/new-home" -Doomph.setup.user.home.redirect=true
  ```

* `<Any jvm options>` - any jvm options will be bind to setup model variables, ex.:

  ```bash
  -Dmy.project.option1="my-value1"
  -Dmy.project.option2="my-value2"
  -Dmy.project.option3="my-value3"
  ```

## Related links

* Oomph source code: [git][2], [patch headless mode][1].
* Oomph docs: [Eclipse Oomph Authoring][4] and [Oomph Targlets][5].
* Example of setups models: [default products][6], [default projects][7], [eclipsesource product][8], [oomph playground][9], [Arlo's project catalog][10].
* Tutorials: [Oomph basic tutorial][3], [Customized Eclipse IDE provisioning with Oomph][11], [Maven Tycho for building Eclipse plug-ins][12], [How to deploy to a Maven repository][13].

## Development environment

* VS Code - need install extension [Eclipse PDE support](https://marketplace.visualstudio.com/items?itemName=yaozheng.vscode-pde).
* Eclipse - need install [Plugin Development Environment](https://marketplace.eclipse.org/content/eclipse-pde-plug-development-environment).

[1]: https://git.eclipse.org/r/c/oomph/org.eclipse.oomph/+/66353
[2]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/
[3]: https://eclipsesource.com/blogs/tutorials/oomph-basic-tutorial/
[4]: https://wiki.eclipse.org/Eclipse_Oomph_Authoring
[5]: https://wiki.eclipse.org/Oomph_Targlets
[6]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/org.eclipse.products.setup
[7]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/org.eclipse.projects.setup
[8]: https://github.com/eclipsesource/oomph/blob/master/EclipseSource.setup
[9]: https://github.com/nittka/oomph-playground
[10]: https://github.com/ArloL/eclipse-projects
[11]: https://www.vogella.com/tutorials/Oomph/article.html
[12]: https://www.vogella.com/tutorials/EclipseTycho/article.html
[13]: https://wiki.eclipse.org/Tycho:How_to_deploy_to_a_Maven_repository
