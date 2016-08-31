appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}
root(WARN, ["STDOUT"])   // change log level here to TRACE, DEBUG, INFO, WARN
