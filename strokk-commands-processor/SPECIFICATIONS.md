# Annotation Processor Specifications (inner classes)
*This document is meant as a reference for how the annotation processors processes
methods into a Brigadier source tree, with focus on inner classes.*

## Different types of nodes
Crudely said, there are three types of ways to add nested nodes:

### Distinct methods
A method has two parts:
- The `@Executes` annotation, which allows for defining subliterals under which the executor should be placed:
  `@Executes("one two")` would be placed under `<root> one two`.
- Parameters inside the method itself. These can be literals or arguments. A method with the following
  signature...
  ```java
  @Executes
  void run(CommandSender sender, @Literal String run, Player target);
  ```
  ...would result in a path `<root> run <target>`.

### Nested classes
Since v1.3.0, command classes can be nested to reduce literal complexity. These can be combined with
distinct executes methods to achieve the same effect as putting in parameters to the executes method:
```java
@Command("one two")
class oneTwoSub {
    
    @Executes("three")
    void runThree(CommandSender sender);
    
    @Executse("four")
    void runFour(CommandSender sender);
}
```

This would result in the following paths:
- `<root> one two three`
- `<root> one two four`

### Nested record classes
Similarly to nested classes, records can be used instead. These allow for not only declaring literals to be
prepended, but also argument types.
```java
@Command("player")
record PlayerSub(Player target) {
    
    @Executes("heal")
    void heal(CommandSender sender);
    
    @Executes("setlvl")
    void setLvl(CommandSender sender, int level);
}
```
Would result in the following paths:
- `<root> <target> heal`
- `<root> <target> setlvl <level>`

These nodes can be nested indefinitely.

## Combining nodes
It is very well possible to "merge" paths together. If you define the following executes methods:
```java
@Executes("some key")
void key(CommandSender sender);

@Executes("some value")
void value(CommandSender sender);
```

They should result in those two paths being valid:
- `<root> some key`
- `<root> some value`

### Name-type mismatches
As this also applies to argument types, which can have different types (as example (a.e. in short): `int` and `String`)
but the same name (a.e.: `value`), the implementation should throw a compile-time exception, telling the user that
they cannot do this and should rename one of the parameters. This is only true if the **type of the parameter**
differs. If it doesn't, the paths should be merged together.

## Processing annotations into an intermediate, internal tree
The annotation processor should run once per **top-level class annotated with `@Command`**.
It then should work **depth-first** to parse inner classes with the `@Command` annotation.

### Resolving command paths
The first step is to generate a bunch of paths without worrying about merging them.
We can visualize this on the following example command:
```java
@Command("visualize")
class VisualizeCommand {
    
    @Executes("run")
    void run(CommandSender sender);
    
    @Command("fly")
    class FlySub {
        
        @Executes("on")
        void on(CommandSender sender);
        
        @Executes("off")
        void off(CommandSender sender);
        
        @Command("player")
        record PlayerSub(Player target) {
            
            @Executes("toggle")
            void toggle(CommandSender sender);
        }
    }
}
```

This would turn into the following paths:
- `visualize run`
- `<root> fly on`
- `<root> fly off`
- `<root> player <target> toggle`

We deliberately avoid parsing the root just yet. As these paths are taken from the most inner classes first,
they have no knowledge of any (or how many!) nested classes there might be.

### Combining paths into a tree
The next step is to put the paths together into a single tree. For this, we work from **the top level** down,
filling in any `<root>` tags as we go, resulting in the following possible paths:
- `visualize run`
- `visualize fly on`
- `visualize fly off`
- `visualize fly player <target> toggle`

Which can be combined cleanly into a single command tree.

## Code generation
This is the most complicated step considering the support for nested classes/records. Here it is important
to know the distinction between static and non-static classes. Static refers to the state of being able
to access variables from the enclosing class. Static classes can be instantiated directly, without
an instance of the enclosing class. **Record classes are always static**, as per design.

### Static vs. non-static classes
Encountering a static class provides no issues whatsoever. You can simply construct them with `new Enclosing.Inner()`.

Non-static classes are more complicated. They require an instance of the enclosing class in order to be instantiated
this way, resulting in a `new enclosing.Inner()` structure. This is particularly problematic with n-times
nested, non-static classes. And it becomes even worse if both of these are mixed:
```java
class Root {
    static class StaticClass {
        class NonStaticClass {
            static class AnotherStaticClass { }
        }
    }
}
```

Constructing a `NonStaticClass` in the shortest way possible would look like this:
```java
var nonStaticClass = new Root.StaticClass().new NonStaticClass();
```

In an attempt to simplify code generation, one can create an instance of all of these top-down:
```java
var root = new Root();
var staticClass = new Root.StaticClass();
var nonStaticClass = staticClass.new NonStaticClass();
var anotherStaticClass = new Root.StaticClass.NonStaticClass.AnotherStaticClass();
```

This way, all instances are created for us in advance.

### Handling records
Due to records holding argument values, they cannot be instantiated in advance, meaning any and all non-static
inner classes must be created inside the generated `.executes` method.

### An example generation for the `visualize` code example
As a reference example, the `visualize` command shown a few lines up should generate like this:
```java
public static LiteralArgumentBuilder<CommandSourceStack> construct() {
    VisualizeCommand VISUALIZE_COMMAND = new VisualizeCommand();
    VisualizeCommand.FlySub VISUALIZE_COMMAND_FLY_SUB = VISUALIZE_COMMAND.new FlySub();

    return Commands.literal("visualize")
        .executes(ctx -> {
            VISUALIZE_COMMAND.run(ctx.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        })
        .then(Commands.literal("fly")
            .then(Commands.literal("on")
                .executes(ctx -> {
                    VISUALIZE_COMMAND_FLY_SUB.on(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("off")
                .executes(ctx -> {
                    VISUALIZE_COMMAND_FLY_SUB.off(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("player")
                .then(Commands.arguments("player", ArgumentTypes.player())
                    .executes(ctx -> {
                        VisualizeCommand.FlySub.PlayerSub executorClass = new VisualizeCommand.FlySub.PlayerSub(
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).stream().toList().getFirst()
                        );
                        executorClass.toggle(ctx.getSource().getSender());
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        );
}
```

## Records with nested classes
Whilst you cannot nest records correctly (considering they are always static, meaning they wouldn't allow for
arguments to be passed around as one would expect), you can have nested, non-static classes inside a record.
This case should also be handled.

If we were to expand on the `fly player` subcommand of the `visualize` command to add a `yeet` subcommand
with a nested class, all inner classes would have to be constructed inside the `.executes` method.

```java
@Command("player")
record PlayerSub(Player target) {
    @Command("yeet")
    class YeetSub {
        /**
         * Note that this example is not a valid use case for a nested class. You could achieve the
         * same path by putting the {@code yeet} method right inside the record and adding a literal
         * path into the {@link Executes} annotation. This, however, is fine for the sake of specs.
         */
        @Executes
        void yeet(CommandSender sender);
    }
}
```

It would have to be constructed and called like this:
```java
.then(Commands.literal("yeet")
    .executes(ctx -> {
        VisualizeCommand.FlySub.PlayerSub recordClass = new VisualizeCommand.FlySub.PlayerSub(
            ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).stream().toList().getFirst()
        );
        VisualizeCommand.FlySub.PlayerSub.YeetSub yeetSub = recordClass.new YeetSub();
        yeetSub.yeet(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    })
)
```

## Internal processes
In order to achieve such levels of advanced, nested command structures the processor needs to keep
track of a magnitude of variables for each path:
- Has a record class been used?
  - If yes, keep track of the arguments relevant to construct a record instance. 
  - If yes, keep track of its inner classes relevant for the path, as they will need to be constructed live.
- Method parameters of the `@Executes` annotated method.

For the source file printer, a command tree has to be constructed in greatly simplify the Brigadier tree printing.
Instead of keeping track of nodes in a traditional sense, we can put the paths into the tree directly.

For our `visualize` command structure, the tree might look like this:

<p align="center">

![](./public/diagram-3.svg)

</p>

The horizontal rectangle nodes here describe non-executable paths and ruby-shaped
nodes describe executable paths.

There is only one problem remaining: What if there are two nodes which start with the same
path, but finally divert? A.e.:
- `<root> run <target>`
- `<root> run <target> stop`

As could be declared by these methods:
```java
@Executes
void run(CommandSender sender, Player target);

@Executes
void runStop(CommandSender sender, Player target, @Literal String stop);
```

The answer here is quite simple: We have to keep two information:
1. We need the argument information for the method.
2. We split/combine paths in order to cleanly fit into the tree.