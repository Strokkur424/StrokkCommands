module StrokkCommands {
    exports net.strokkur.commands.annotations;
    exports net.strokkur.commands.annotations.arguments;
    exports net.strokkur.commands.objects.arguments;
    
    requires com.google.auto.service;
    requires java.compiler;
    requires org.jspecify;
    requires org.jetbrains.annotations;
    requires jdk.jfr;
}