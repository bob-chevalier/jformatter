java_library(
    name = "formatters",
    srcs = glob([ "*.java" ]),
    deps = [
        "@junit4//jar",
        "@org_junit_platform_commons//jar",
        "@org_junit_jupiter_api//jar",
        "//src/main/java/com/staircaselabs/jformatter/core:core",
        "//src/main/java/com/staircaselabs/jformatter/formatters:formatters",
    ],
)

test_suite(
    name = "formatters-tests",
#    tags = [ "-flaky" ],
)

java_test(
    name = "TestHeaderFormatter",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestHeaderFormatter",
    runtime_deps = [
        "@org_opentest4j//jar",
        ":formatters",
    ],
    data = [ "//src/test/data/header_formatter:test-inputs", ],
)
