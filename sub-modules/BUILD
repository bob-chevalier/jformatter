genrule(
    name = "langtools",
    srcs = [ "openjdk8-langtools/dist/lib/classes.jar" ],
    outs = [ "openjdk8-langtools.jar" ],
    cmd = "cp $< $@",
    visibility = ["//src/main/java/com/staircaselabs/jformatter:__subpackages__"],
)
