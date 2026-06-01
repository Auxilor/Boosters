---
title: "API"
sidebar_position: 6
---

Boosters is fully open-source, and you can build against it from your own plugin. This page shows how to add it as a dependency.

## Source code

The full source is on [GitHub](https://github.com/Auxilor/Boosters).

## Adding the dependency

1. Add the Auxilor repository and the Boosters dependency to your `build.gradle.kts`:

   ```kotlin
   repositories {
       maven("https://repo.auxilor.io/repository/maven-public/")
   }

   dependencies {
       compileOnly("com.willfp:Boosters:<version>")
   }
   ```

The latest version available on the repo can be found [here](https://github.com/Auxilor/Boosters/tags)

<hr/>

## Where to go next

- **The framework:** Boosters is built on [eco](https://github.com/Auxilor/eco), where most shared APIs live.
- **Build a booster from config:** the [How to Make a Booster](how-to-make-a-custom-booster) guide.