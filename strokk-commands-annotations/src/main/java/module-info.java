import org.jspecify.annotations.NullMarked;

@NullMarked
module strokkcommands.annotations {
    exports net.strokkur.commands;
    exports net.strokkur.commands.annotations;
    exports net.strokkur.commands.annotations.arguments;
    
    requires org.jspecify;
}