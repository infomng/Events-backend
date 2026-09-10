<configuration>

    <appender name="CONSOLE"
              class="ch.qos.logback.core.ConsoleAppender">

        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS}
                %cyan([METHOD])
                %msg%n
            </pattern>
        </encoder>

    </appender>


    <logger name="METHOD_TRACE"
            level="INFO"
            additivity="false">

        <appender-ref ref="CONSOLE"/>

    </logger>


    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>

</configuration>