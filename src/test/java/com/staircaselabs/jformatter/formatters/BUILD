java_library(
    name = "formatters",
    srcs = glob([ "*.java" ]),
    deps = [
        "@junit4//jar",
        "@org_junit_platform_commons//jar",
        "@org_junit_jupiter_api//jar",
        "@com_google_truth//jar",
        "@com_google_guava//jar",
        "//src/main/java/com/staircaselabs/jformatter/core:core",
        "//src/main/java/com/staircaselabs/jformatter/formatters:formatters",
    ],
)

test_suite(
    name = "formatters-tests",
#    tags = [ "-flaky" ],
)

#java_test(
#    name = "TestBraceInserter",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.formatters.TestBraceInserter",
#    runtime_deps = [
#        "@org_opentest4j//jar",
#        ":formatters",
#    ],
#    data = [ "//src/test/data/brace_inserter:test-inputs", ],
#)

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

java_test(
    name = "TestImportsSorter",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestImportsSorter",
    runtime_deps = [
        "@org_opentest4j//jar",
        ":formatters",
    ],
    data = [ "//src/test/data/imports_sorter:test-inputs", ],
)

#java_test(
#    name = "TestLeftBraceCuddler",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.formatters.TestLeftBraceCuddler",
#    runtime_deps = [
#        "@org_opentest4j//jar",
#        ":formatters",
#    ],
#    data = [ "//src/test/data/cuddle_left:test-inputs", ],
#)

#java_test(
#    name = "TestPaddingFormatter",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.formatters.TestPaddingFormatter",
#    runtime_deps = [
#        "@org_opentest4j//jar",
#        ":formatters",
#    ],
#    data = [ "//src/test/data/padding:test-inputs", ],
#)

#java_test(
#    name = "TestRightBraceCuddler",
#    size = "small",
#    test_class = "com.staircaselabs.jformatter.formatters.TestRightBraceCuddler",
#    runtime_deps = [
#        "@org_opentest4j//jar",
#        ":formatters",
#    ],
#    data = [ "//src/test/data/cuddle_right:test-inputs", ],
#)

java_test(
    name = "TestLayoutFormatter",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestLayoutFormatter",
    runtime_deps = [
        "@org_opentest4j//jar",
        ":formatters",
    ],
    data = [ "//src/test/data/layout:test-inputs", ],
)

java_test(
    name = "TestTrailingWhitespaceRemover",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestTrailingWhitespaceRemover",
    runtime_deps = [
        "@org_opentest4j//jar",
        ":formatters",
    ],
    data = [ "//src/test/data/trailing_whitespace:test-inputs", ],
)

java_test(
    name = "TestUnusedImportsRemover",
    size = "small",
    test_class = "com.staircaselabs.jformatter.formatters.TestUnusedImportsRemover",
    runtime_deps = [
        "@org_opentest4j//jar",
        ":formatters",
    ],
    data = [ "//src/test/data/unused_imports:test-inputs", ],
)
