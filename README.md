# StrokkCommands

**StrokkCommands** is a very simple and lightweight compile-time library for generating Brigadier command trees
from annotation! Have **you** ever though: "Oh boy, my command is far too hard to read to maintain"? Well,
**this** is the **solution**!!!

## Documentation
You can read the documentation at https://commands.strokkur.net!

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

Tiny, isn't it?

### Star History

<picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date&theme=dark" />
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date" />
    <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=Strokkur424/StrokkCommands&type=Date" />
</picture>