package org.example.surveyplanet.pojo.entity.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Options {
    private final ArrayList<Option> options = new ArrayList<>();

    public Options(String content) {
        int index = 0;
        assert content.charAt(0) == '[' && content.charAt(content.length() - 1) == ']';
        AtomicInteger id = new AtomicInteger(1);
        Arrays.stream(content.substring(1, content.length() - 1).split(", ")).forEach(option -> {
            options.add(new Option(option, id.getAndIncrement()));
        });
    }

    public ArrayList<Option> getOptions() {
        return options;
    }
}
