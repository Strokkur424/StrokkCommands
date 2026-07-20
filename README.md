# StrokkCommands

**StrokkCommands** is a very simple and lightweight compile-time library for generating Brigadier command trees
from annotation! It is written for the Paper server software and fully supports all arguments exposed in the API.

## Documentation

> [!NOTE]
> The documentation (`/docs`) has moved to its own repository. You can find it under https://github.com/Strokkur424/strokkur.net.

You can read the documentation at https://commands.strokkur.net!

## Discord

For questions, update announcements, and general chatter, make sure to join the Discord server: https://discord.strokkur.net.

## Example

Here is a pretty typical Brigadier command:

```java
public static LiteralCommandNode<CommandSourceStack> create() {
    return Commands.literal("fillblock")
        .then(Commands.argument("pos1", ArgumentTypes.blockPosition())
            .then(Commands.argument("pos2", ArgumentTypes.blockPosition())
                .then(Commands.argument("state", ArgumentTypes.blockState())
                    .executes(ctx -> {
                        // fill blocks with default perTick value
                        return Command.SINGLE_SUCCESS;
                    })
    
                    .then(Commands.argument("perTick", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            // fill blocks with specified perTick value
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        )
        .build();
}
```

This is really hard to read. In order to combat this, I've created **StrokkCommands**. This is the same command,
but declared using annotations (and the abuse of some Java language features):

```java
@Command("fillblock")
record FillBlockCommand(BlockPosition pos1, BlockPosition pos2, BlockState state) {

    @Executes
    void execute(CommandSender sender) {
        execute(sender, 1000);
    }

    @Executes
    void execute(CommandSender sender, int perTick) {
        // fill blocks with specified perTick value
    }
}
```

Whoah, that's so much shorter! I hope this makes the use case of this library very clear, so jump right in by
[adding the dependency](https://commands.strokkur.net/docs/dependency/) and creating your own tiny commands.

## What is the difference to other command frameworks (like cloud)?
StrokkCommands and cloud both solve completely different problems: Whilst cloud primarily markets itself as a full-blown
command framework, StrokkCommands simply allows a developer to generate Brigadier trees. It does not handle any complex
logic expected from a conventional framework. It does **not extend** the feature set of Brigadier in any way, only
providing **quality of life** improvements to the existing system.

The primary reason for StrokkCommands existence is the lack of an **ultra-lightweight** Brigadier command library which
simply takes away the complexity from Brigadier. For many developers, the feature set of most command frameworks is just
not required and just increases your plugin jar's file size for very little reasons.

## Star History

<a href="https://www.star-history.com/?repos=Strokkur424%2FStrokkCommands&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&theme=dark&legend=top-left&sealed_token=kg0OpP777wufPXFbdOXkD4PYbvxgGT-FwTropivkgG0yAwHIjzEKVS2KgqbBBMLW5IurxFmZVlHvR2QjHMd4Ol1OB3Z2eSM5P-Rcuu0oFn3OHT2yz8_XLk1yXeO-XeRqoo3Zmc4TAVTyseWlPRhhJameKgz6YtAVrBmR7vBD9fswYgp01NyIEmcIu365" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&legend=top-left&sealed_token=kg0OpP777wufPXFbdOXkD4PYbvxgGT-FwTropivkgG0yAwHIjzEKVS2KgqbBBMLW5IurxFmZVlHvR2QjHMd4Ol1OB3Z2eSM5P-Rcuu0oFn3OHT2yz8_XLk1yXeO-XeRqoo3Zmc4TAVTyseWlPRhhJameKgz6YtAVrBmR7vBD9fswYgp01NyIEmcIu365" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&legend=top-left&sealed_token=kg0OpP777wufPXFbdOXkD4PYbvxgGT-FwTropivkgG0yAwHIjzEKVS2KgqbBBMLW5IurxFmZVlHvR2QjHMd4Ol1OB3Z2eSM5P-Rcuu0oFn3OHT2yz8_XLk1yXeO-XeRqoo3Zmc4TAVTyseWlPRhhJameKgz6YtAVrBmR7vBD9fswYgp01NyIEmcIu365" />
 </picture>
</a>

<img width="1832" height="1404" alt="star-history-2026721" src="https://github.com/user-attachments/assets/053dd234-ca72-4e35-ae66-8601b3edb2c5" />
