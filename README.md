# StrokkCommands

**StrokkCommands** is a very simple and lightweight compile-time library for generating Brigadier command trees from
annotation! It is written for the Paper server software and fully supports all arguments exposed in the API.

## Documentation

> [!NOTE]
> The documentation (`/docs`) has moved to its own repository. You can find it
under https://github.com/Strokkur424/strokkur.net.

You can read the documentation at https://commands.strokkur.net!

## Discord

For questions, update announcements, and general chatter, make sure to join the Discord
server: https://discord.strokkur.net.

## Example

Here is a pretty typical Brigadier command:

```java
public static LiteralCommandNode<CommandSourceStack> create() {
  return Commands.literal("fillblock")
    .then(Commands.argument("pos1", ArgumentTypes.blockPosition())
      .then(Commands.argument("pos2", ArgumentTypes.blockPosition())
        .then(Commands.argument("state", ArgumentTypes.blockState())
          .executes(ctx -> runLogic(ctx, 1000))
          .then(Commands.argument("perTick", IntegerArgumentType.integer())
            .executes(ctx -> runLogic(ctx, IntegerArgumentType.getInteger(ctx, "perTick")))
          )
        )
      )
    )
    .build();
}

private static int runLogic(CommandContext<CommandSourceStack> ctx, int perTickValue) {
  BlockPosition pos1 = ctx.getArgument("pos1", BlockPositionResolver.class).resolve(ctx.getSource());
  BlockPosition pos2 = ctx.getArgument("pos2", BlockPositionResolver.class).resolve(ctx.getSource());
  BlockState state = ctx.getArgument("state", BlockState.class);

  // fill blocks with either specified or default perTick value
  return Command.SINGLE_SUCCESS;
}
```

This is really hard to read. In order to combat this, I've created **StrokkCommands**. This is the same command, but
declared using annotations (and the abuse of some Java language features):

```java
@Command("fillblock")
record FillBlockCommand(BlockPosition pos1, BlockPosition pos2, BlockState state) {

  @Executes
  void execute(OptionalInt perTick) {
    int perTickValue = perTick.orElse(1000);
    // fill blocks with either specified or default perTick value
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
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&theme=dark&legend=top-left&sealed_token=JeY6m5vfbSm6wKNQ3Pr557lUTO9qoDC1SKkwJ5o9yhVBvKbIk0d8FTL5NYVvK499ZAdl5BOePYbKKeKxmcH2IjYLO4MfG28WCiwkD98FjgWW17kgoPY8Zw" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&legend=top-left&sealed_token=JeY6m5vfbSm6wKNQ3Pr557lUTO9qoDC1SKkwJ5o9yhVBvKbIk0d8FTL5NYVvK499ZAdl5BOePYbKKeKxmcH2IjYLO4MfG28WCiwkD98FjgWW17kgoPY8Zw" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=Strokkur424/StrokkCommands&type=date&legend=top-left&sealed_token=JeY6m5vfbSm6wKNQ3Pr557lUTO9qoDC1SKkwJ5o9yhVBvKbIk0d8FTL5NYVvK499ZAdl5BOePYbKKeKxmcH2IjYLO4MfG28WCiwkD98FjgWW17kgoPY8Zw" />
 </picture>
</a>
