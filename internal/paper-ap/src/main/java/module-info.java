import org.jspecify.annotations.NullMarked;

@NullMarked
module net.strokkur.internal.paper.api {
  requires org.jspecify;
  requires java.compiler;
  requires net.strokkur.jap.source;
}
