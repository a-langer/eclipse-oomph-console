# Console Oomph Installer

[![license](https://img.shields.io/badge/license-EPL2-brightgreen.svg)](https://github.com/a-langer/eclipse-oomph-console/blob/main/LICENSE "License of source code")
[![p2](https://img.shields.io/badge/p2-latest-blue?style=flat)](https://alanger.jfrog.io/artifactory/eclipse-oomph-console/ "P2 plugin repository")
[![marketplace](https://img.shields.io/badge/marketplace-latest-blue?style=flat)](https://marketplace.eclipse.org/content/console-oomph-installer "Eclipse marketplace")
[![JitPack](https://jitpack.io/v/a-langer/eclipse-oomph-console.svg)](https://jitpack.io/#a-langer/eclipse-oomph-console "Maven repository for builds from source code")
[![Maven](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/org.eclipse.oomph.console.product/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/org.eclipse.oomph.console.product "Maven repository for stable builds")

Console version of Oomph installer (also called [Eclipse installer](https://wiki.eclipse.org/Eclipse_Installer)). Implements a headless installation mode from command line. This project base on patch [66353][1] of Mikael Karlback.

Available solutions:

* [Standalone Oomph console installer](https://search.maven.org/search?q=a:org.eclipse.oomph.console.product) (plain Oomph with console plugin installed).
* [Oomph console plugin for Eclipse](https://marketplace.eclipse.org/content/console-oomph-installer) (can be installed on any Eclipse product from [p2 repository](https://alanger.jfrog.io/artifactory/eclipse-oomph-console/)).

Typical use cases:

* Installation Eclipse with an exact set of features/plugins and with certain preferences.
* Automation the process of installing and updating Eclipse (using CI\CD).

## Supported features and examples of usage

* Use a console version of oomph installer; you can download it either from [Standalone Oomph console installer](https://search.maven.org/search?q=a:org.eclipse.oomph.console.product) (choose the appropriate download for your target platform) or with mvn command. This eclipse installer is provided with `org.eclipse.oomph.console.application` application to handle command line installations:

  ```bash
  # Check oomph console version
  # Linux
  mvn dependency:copy -Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:1.0.1:tar.gz:linux.gtk.x86_64 -DoutputDirectory=.
  # MacOS x86_64
  #mvn dependency:copy -Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:1.0.1:tar.gz:macosx.cocoa.x86_64 -DoutputDirectory=.
  # Windows
  #mvn dependency:copy -Dartifact=com.github.a-langer:org.eclipse.oomph.console.product:1.0.1:tar.gz:win32.win32.x86_64 -DoutputDirectory=.
  
  tar -xzf org.eclipse.oomph.console.product-*.tar.gz
  cd eclipse-installer/
  ```

* Install Eclipse [product][6] such as "Eclipse IDE for Java Developers" or "Eclipse IDE for PHP Developers", ex.:

  ```bash
  # Install "Eclipse IDE for Java Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java"
  
  # Install "Eclipse IDE for PHP Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.php"
  ```

* Install Eclipse [project][7], such as "SWTBot testing tool" or "Model Workflow Engine" (see [list of projects](https://projects.eclipse.org/)), ex.:

  ```bash
  # Install "SWTBot testing tool" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="swtbot" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installation.id="eclipse-with-swtbot" \
    -Dsetup.p2.agent="$HOME/.p2"
  
  # Install "Model Workflow Engine" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="MWE" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installation.id="eclipse-with-mwe" \
    -Dsetup.p2.agent="$HOME/.p2"
  ```

* Install multiple products and projects, ex.:

  ```bash
  # Install "SWTBot testing tool", "Business Intelligence Reporting Tool" and "Model Workflow Engine" over "Eclipse IDE for Java Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="swtbot,birt,MWE" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installation.id="eclipse-with-swtbot-birt-mwe" \
    -Dsetup.p2.agent="$HOME/.p2"
  
  # Install "SWTBot", "BIRT", "MWE" over "Eclipse IDE for Java Developers" and "BIRT", "MWE" over "Eclipse IDE for PHP Developers"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java,epp.package.php" \
    -Doomph.project.id.epp.package.java="swtbot,birt,MWE" \
    -Doomph.project.id.epp.package.php="birt,MWE" \
    -Doomph.workspace.location.epp.package.java="$PWD/workspace-java" \
    -Doomph.workspace.location.epp.package.php="$PWD/workspace-php" \
    -Doomph.installation.id.epp.package.java="java-with-swtbot-birt-mwe" \
    -Doomph.installation.id.epp.package.php="php-with-birt-mwe" \
    -Dsetup.p2.agent="$HOME/.p2"
  ```

* Install product and project from [custom setup model](./org.eclipse.oomph.console.product/setups), ex.:

  ```bash
  # Install "Bash Editor" over "Eclipse IDE for Java Developers" from setup model located in directory "$PWD/setups/"
  ./eclipse-inst -nosplash -application org.eclipse.oomph.console.application -vmargs \
    -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/" \
    -Doomph.redirection.setupsDir="index:/->$PWD/setups/" \
    -Doomph.installation.location="$PWD/ide" \
    -Doomph.product.id="epp.package.java" \
    -Doomph.project.id="bash.editor" \
    -Doomph.workspace.location="$PWD/workspace" \
    -Doomph.installation.id="eclipse-with-basheditor" \
    -Dsetup.p2.agent="$HOME/.p2" \
    -Doomph.installer.verbose=true
  ```

* TODO Update Eclipse product and projects (not implemented yet, see [1](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.p2/src/org/eclipse/oomph/setup/p2/impl/P2TaskImpl.java), [2](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.ui/src/org/eclipse/oomph/setup/ui/SetupUIPlugin.java), [3](https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/plugins/org.eclipse.oomph.setup.doc/src/org/eclipse/oomph/setup/doc/user/wizard/DocUpdateWizard.java)).

## Settings

The Console Oomph Installer settings (see also [Eclipse runtime options](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html)):

* `oomph.product.id` (string, default `null`) - identifier of product in setup model, ex.:

  ```bash
  # For single product
  -Doomph.product.id="epp.package.java"
  # For multiple products
  -Doomph.product.id="epp.package.java,epp.package.php"
  ```

* `oomph.product.version` (string, default `latest`) - version of product in setup model, ex.:

  ```bash
  # For all products
  -Doomph.product.version="latest"
  # Or set the version for specific product
  -Doomph.product.id="epp.package.java:latest"
  # Or set the version for multiple specific products
  -Doomph.product.id="epp.package.java:latest,epp.package.php:latest"
  ```

* `oomph.installation.location` (string, default `null`) - location of product installation, ex.:

  ```bash
  # For all products
  -Doomph.installation.location="$PWD/ide"
  # Or set the location for specific product, ex. "epp.package.java"
  -Doomph.installation.location.epp.package.java="$PWD/ide-java"
  ```

* `oomph.installation.id` (string, default specific for each product) - directory in `oomph.installation.location`, ex.:

  ```bash
  # For all products
  -Doomph.installation.id="my-inst-dir"
  # Or set the directory fro specific product, ex. "epp.package.java"
  -Doomph.installation.id.epp.package.java="java-inst-dir"
  ```

* `oomph.project.id` (string, default `null`) - identifier of project in setup model, ex.:

  ```bash
  # For single project
  -Doomph.project.id="swtbot"
  # For multiple projects
  -Doomph.project.id="swtbot,birt,MWE"
  ```

* `oomph.project.stream` (string, default `master`) - stream of project in setup model, ex.:

  ```bash
  # For all projects
  -Doomph.project.stream="master"
  # Or set the stream for specific project
  -Doomph.project.id="swtbot:master"
  # Or set the stream for multiple specific projects
  -Doomph.project.id="swtbot:master,birt:master,MWE:master"
  ```

* `oomph.installer.verbose` (boolean, default `false`) - enable verbose output instead of progress bar, ex.:

  ```bash
  -Doomph.installer.verbose=true
  ```

* `oomph.workspace.location` (string, default `oomph.installation.location`) - project workspace location, ex.:

  ```bash
  # For all projects
  -Doomph.workspace.location="$PWD/my-workspace"
  # Or set the workspace fro specific project, ex. "birt"
  -Doomph.workspace.location.birt="$PWD/birt-workspace"
  ```

* `oomph.redirection.setups` - location of basic setup model (contain org.eclipse.setup file), will be added to eclipse.ini as "oomph.redirection.index.redirection", ex.:

  ```bash
  # Setup model from URL
  -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/"
  # Setup model from directory
  -Doomph.redirection.setups="index:/->$PWD/my-custom-setups/"
  ```

* `oomph.redirection.*` - location of additional setup model (also contain org.eclipse.setup file), uses for override basic setup model location, ex.:

  ```bash
  # Basic setup model location
  -Doomph.redirection.setups="index:/->http://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/"
  # Additional setup model location (overrides product and project catalog)
  -Doomph.redirection.setupsDir="index:/->$PWD/my-custom-setups/"
  ```

* `setup.p2.agent` (string, default null) - directory location of shared pool for features/plugins, ex.:

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
* Example of setups models: [default products][6], [default projects][7], [eclipsesource product][8], [oomph playground][9].
* Tutorials: [Oomph basic tutorial][3], [Customized Eclipse IDE provisioning with Oomph][10], [Maven Tycho for building Eclipse plug-ins][11], [How to deploy to a Maven repository][12].

## Development environment

* VS Code - need install extension [Eclipse PDE support](https://marketplace.visualstudio.com/items?itemName=yaozheng.vscode-pde).
* Eclipse - need install [Plugin Development Environment](https://marketplace.eclipse.org/content/eclipse-pde-plug-development-environment).

[1]: https://git.eclipse.org/r/c/oomph/org.eclipse.oomph/+/66353
[2]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/tree/
[3]: https://eclipsesource.com/blogs/tutorials/oomph-basic-tutorial/
[4]: https://wiki.eclipse.org/Eclipse_Oomph_Authoring#Creating_a_Configuration
[5]: https://wiki.eclipse.org/Oomph_Targlets
[6]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/org.eclipse.products.setup
[7]: https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/org.eclipse.projects.setup
[8]: https://github.com/eclipsesource/oomph/blob/master/EclipseSource.setup
[9]: https://github.com/nittka/oomph-playground
[10]: https://www.vogella.com/tutorials/Oomph/article.html
[11]: https://www.vogella.com/tutorials/EclipseTycho/article.html
[12]: https://wiki.eclipse.org/Tycho:How_to_deploy_to_a_Maven_repository
