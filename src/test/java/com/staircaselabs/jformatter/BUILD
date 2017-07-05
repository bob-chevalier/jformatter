#java_library(
#    name = "jformatter",
#    srcs = glob([ "*.java" ]),
#    deps = [
#            "@org_junit_platform_commons//jar",
#            "@org_junit_platform_engine//jar",
#            "@org_junit_platform_runner//jar",
#            "@org_junit_platform_suite_api//jar",
#            "@org_junit_jupiter_api//jar",
#            "@org_junit_jupiter_engine//jar",
#            "@org_junit_vintage//jar",
#            "@junit4//jar",
#            "//src/test/java/com/staircaselabs/jformatter/core:core",
#    ],
#    runtime_deps = [
#            "@org_junit_platform_launcher//jar",
#            "@org_opentest4j//jar",
#    ],
#)
#
#java_test(
#    name = "AllTests",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.AllTests",
#    runtime_deps = [
#            "@org_junit_platform_commons//jar",
#            "@org_junit_platform_engine//jar",
#            "@org_junit_platform_runner//jar",
#            "@org_junit_platform_suite_api//jar",
#            "@org_junit_jupiter_api//jar",
#            "@org_junit_jupiter_engine//jar",
#            "@org_junit_vintage//jar",
#            "@org_opentest4j//jar",
#            ":jformatter",
#            "//src/test/java/com/staircaselabs/jformatter/core:core",
#    ],
#)

test_suite(
    name = "all-tests",
#    tags = [ "-flaky" ],
)
