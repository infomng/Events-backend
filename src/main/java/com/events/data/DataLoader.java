package com.events.data;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-loader.enabled", havingValue = "true")
public class DataLoader {
//        implements CommandLineRunner {

//    private final EventDataGenerator eventDataGenerator;
//
//    @Value("${app.data-loader.event-count:100}") // Default to 100 if not specified
//    private int numberOfEventsToGenerate;

    //    @Override
    //    public void run(String... args) throws Exception {
    //        eventDataGenerator.generate(numberOfEventsToGenerate);
    //    }
}
