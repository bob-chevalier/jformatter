java_binary(
    name = "jformatter",
    main_class = "com.staircaselabs.jformatter.BatchFormatter",
    runtime_deps = [ ":jformatter-lib" ],
)

java_library(
    name = "jformatter-lib",
    srcs = glob([ "src/main/java/com/staircaselabs/jformatter/*.java" ]),
    deps = [
        "@com_beust_jcommander//jar",
        "//src/main/java/com/staircaselabs/jformatter/core:core",
        "//src/main/java/com/staircaselabs/jformatter/config:config",
        "//src/main/java/com/staircaselabs/jformatter/debug:debug",
        "//src/main/java/com/staircaselabs/jformatter/formatters:formatters",
    ],
)
