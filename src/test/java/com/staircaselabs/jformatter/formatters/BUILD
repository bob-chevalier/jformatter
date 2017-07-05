java_library(
    name = "formatters",
    srcs = glob([ "*.java" ]),
    deps = [
        "@junit4//jar",
        "@org_junit_platform_commons//jar",
        "@org_junit_jupiter_api//jar",
        "//src/main/java/com/staircaselabs/jformatter/formatters:formatters",
    ],
#    resources = [
#            "//src/test/resources:test-src-files",
#    ]
#    visibility = [
#        "//src/test/java/com/staircaselabs/jformatter:__pkg__"
#    ],
)

test_suite(
    name = "formatters-tests",
#    tags = [ "-flaky" ],
)

java_test(
    name = "TestHeader",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestHeader",
    runtime_deps = [ ":formatters" ],
    data = [ "//src/test/resources:test-src-files", ],
)
