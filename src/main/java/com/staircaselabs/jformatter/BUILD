#genrule(
#    name = "langtools",
#    srcs = [ "../../openjdk8-langtools/dist/lib/classes.jar" ],
#    outs = [ "openjdk8-langtools.jar" ],
#    cmd = "cp $< $@",
#)

java_binary(
    name = "jformatter",
    srcs = glob([ "*.java" ]),
    main_class = "com.staircaselabs.jformatter.BatchFormatter",
    deps = [
        "@com_beust_jcommander//jar",
        "//src/main/java/com/staircaselabs/jformatter/core:core",
        "//src/main/java/com/staircaselabs/jformatter/formatters:formatters",
    ],
)

#java_library(
#    name = "test_lib",
#    srcs = glob( [ "src/test/java/com/staircaselabs/jformatter/*.java", ] ),
#    deps = [
#            "@org_junit_platform_engine//jar",
#            "@org_junit_platform_runner//jar",
#            "@org_junit_platform_suite_api//jar",
#            "@org_junit_jupiter_api//jar",
#            "@org_junit_jupiter_engine//jar",
#            "@org_junit_vintage//jar",
#            ":jformatter",
#    ],
#)

#java_test(
#    name = "all_tests",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.FormatterTestSuite",
#    runtime_deps = [ ":test_lib" ],
#)
#
#java_binary(
#    name = "tokentest",
#    srcs = glob( [
#            "src/main/java/com/staircaselabs/jformatter/*.java",
#    ] ),
#    main_class = "com.staircaselabs.jformatter.TokenTest",
#    deps = [
#            "@com_beust_jcommander//jar",
#            ":langtools",
#    ],
#)
