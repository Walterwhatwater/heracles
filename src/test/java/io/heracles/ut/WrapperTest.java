package io.heracles.ut;

import io.heracles.wrapper.CounterWrapper;

/**
 * @author walter
 * @date 2021/01/08 00:54
 **/
public class WrapperTest {
    CounterWrapper counterWrapper = CounterWrapper.builder()
            .name("some_metric_total")
            .help("blah blah blah")
            .labelExtractor(new SomeLabelExtractor(), SomeClass.class)
            .labelNames("other_label1", "other_label2")
            .labelName()
            .wrap();

    public void test() {
        SomeClass someObject = new SomeClass();
        someObject.setLabel1("value1");
        someObject.setLabel2("value2");
        counterWrapper.label(someObject, SomeClass.class)
                .label("other_label1", "other_value1")
                .label("other_label2", "other_value2")
                .inc();

    }
}
