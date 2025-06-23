# StrokkCommands
[Shortcut: Adding the Dependency](#adding-the-dependency).

**StrokkCommands** is a very simple and lightweight compile-time library for generating Brigadier command trees
from annotation! Have **you** ever though: "Oh boy, my command is far too hard to read to maintain"? Well,
**this** is the **solution**!!!

## Example
Talking doesn't cut it, so why don't I show you? Here is a Brigadier command:
```java
public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("adventure")
        .then(Commands.literal("send")
            .then(Commands.literal("message")
                .then(Commands.argument("message", StringArgumentType.string())
                    .then(Commands.literal("with")
                        .then(Commands.literal("color")
                            .then(Commands.argument("color", ArgumentTypes.namedColor())
                                .executes(ctx -> {
                                    // Your command logic
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                )
            )
        ).build();
}
```

Ugly, isn't it? This denotes the command `/adventure send message <message> with color <color>`. Why does this
take up so much space? **I don't know**! And that is why I created **StrokkCommands**! Let's take a look:

```java
@Command("adventure")
class AdventureArgumentsCommand {

    @Executes("send message")
    void executes(CommandSender sender,
                  @StringArg(STRING) String message,
                  @Literal("with") String $with,
                  @Literal("color") String $color,
                  NamedTextColor color) {
        // Your command logic
    }
}
```

Whoah! That's so tiny! Where did the pesky logic go? Well, it has to be somewhere. And you'd be right! But I will
explain how this all works [✨ later ✨](#how-does-it-all-work).


## Adding the dependency
In order to use the annotation preprocessor, you have to add the following lines to your `build.gradle.kts`:
```kts
repositories {
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly("net.strokkur", "strokk-commands-annotations", "1.2.3-SNAPSHOT")
    annotationProcessor("net.strokkur", "strokk-commands-processor", "1.2.3-SNAPSHOT")
}
```

And that's all! Really. **No** shading, **no** relocating. Absolutely **nothing**!

## How to use the library
*More detailed documentation __will__ be readable at https://commands.strokkur.net, at least once I get around to setup a website for this*.

At its core stands the `@Command` annotation. With it, you can annotate your command class. It also describes what the
command is called. So `@Command("skittles")` would result in `/skittles`. You get it.

There are a few more annotations that you can add to the class. Those being `@Description("...")` for the command
description in Bukkit's `/help skittles`, and `@Aliases({"..."})` for the command aliases. Pretty self-explanatory.

Next up, `@Executes`. This one declares a method which declares the command. Each method annotated with `@Executes` **has**
to have its first parameter as a `CommandSender`. You can then, optionally, declare an executor (either a `Player`
or an `Entity`) with `@Executor`. The difference? Well, let's explain it with a section from my Paper docs:

> For the target of a command, you should use getExecutor(), which is relevant if the command was run via
> /execute as <entity> run <our_command>. It is not necessarily required, but is seen as good practice.

Hope that clears things up.
It's just good practice instead of casting a `CommandSender` to a `Player`.
Anyway, the rest of the parameters for our method are just the arguments for our command.
A command
`/helpme <message>` might look like this:
```java
@Command("helpme")
@Description("Ask for help.")
@Aliases({"helpop", "plshelp"})
class HelpMeCommand {
    
    @Executes
    @Permission("plugin.commands.helpme")
    void execute(CommandSender sender, @Executor Player player, @StringArg(StringArgType.GREEDY) String message) {
        // command logic
    }
}
```

Here you can see two annotations you haven't seen yet: `@Permission` and `@StringArg`.
A quick rundown:
- `@Permission`: This one just means that the command sender needs to have the declared permission if they want to run the command.
  You can also annotate the class directly; that will result in all execute methods requiring the permission.
- `@StringArg`: In Brigadier, there are three types of String arguments: `word`, `string` and `greedyString` types. If you
  leave out this annotation, StrokkCommands defaults to using a `word` type. But you can explicitly declare it using this
  annotation.

You can also use `@RequiresOp` to annotate that a command/execute method requires operator status to run.

### Existing arguments
A rundown of basic arguments can be found here:
- https://docs.papermc.io/paper/dev/command-api/basics/arguments-and-literals/

You can also have more complicated arguments, which are noted down here:
- https://docs.papermc.io/paper/dev/command-api/arguments/minecraft/

> [!NOTE]
> Most arguments come pre-resolved.
> Everything that has "resolver" in the name only requires that you enter the resolved type as a parameter.
> A.e: for a `BlockPosition`, just use `BlockPosition` (NOT `BlockPositionResolver`),
> like this:
> ```java
> @Executes
> void execute(CommandSender sender, BlockPosition position) { /* ... */ }
> ```

> [!NOTE]
> All the [registry arguments](https://docs.papermc.io/paper/dev/command-api/arguments/registry/) also come pre-resolved.
> You can use the finished type as your method parameter. (Or, if you want to get the `TypedKey` for whatever reason,
> just use that as your parameter type).

All the notes and information from the official Paper docs (did I mention that I wrote them? *hehe*) also apply
here. So it does make sense to give them a read, even if you might not understand all of it.

### Registering commands
This step works 1:1 the same as written in the [Paper docs about command registration](https://docs.papermc.io/paper/dev/command-api/basics/registration/).
But what do you register? After you build your command and are happy with it, **before** you can register it, you
have to compile the project first. This allows the annotation preprocessor to run and generate a file.
The class name of this file will be &lt;YourCommandClass&gt;Brigadier. You can then register it
using the static `#register(Commands)` method.

As example:
```java
// YourCommand.java
@Command("yourcommand")
class YourCommand {
    // ...
}

// YourPlugin.java
@Override
public void onLoad() {
    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
        Commands commands = commands.registrar();
        YourCommandBrigadier.register(commands); // <-- Here
    });
}
```

Where did the file come from? Don't question it, just accept it. (Or if you *do* question it, feel free to give
[#how-does-it-all-work](#how-does-it-all-work) a read. It's not particularly interesting though).

And that's it! You are completely done. You don't have to see one of those pesky Brigadier trees ever again.
Make sure to give the project a **star**, I would really appreciate it!

### Star History

<picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date" />
</picture>

## How does it all work?
// I will explain this later, at the time of writing, I have been coding on this for 14 hours straight, so please be patient ^-^
