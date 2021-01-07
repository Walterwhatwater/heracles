package io.heracles.wrapper;

import io.heracles.label.extractor.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.heracles.wrapper.base.BaseCollectorWrapper;
import io.heracles.wrapper.base.BaseWrapperBuilder;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 带标签解析的Histogram
 *
 * @author walter
 * @date 2021/01/08 00:00
 **/
public class HistogramWrapper extends BaseCollectorWrapper<HistogramWrapper, Histogram> {
    public static class Builder extends BaseWrapperBuilder<HistogramWrapper, Builder, Histogram, Histogram.Builder> {
        protected Builder() {
            super(Histogram.build());
        }

        public Builder buckets(double... buckets) {
            realBuilder.buckets(buckets);
            return this;
        }

        public Builder linearBuckets(double start, double width, int count) {
            realBuilder.linearBuckets(start, width, count);
            return this;
        }

        public Builder exponentialBuckets(double start, double factor, int count) {
            realBuilder.exponentialBuckets(start, factor, count);
            return this;
        }

        @Override
        protected HistogramWrapper create(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Histogram histogram) {
            return new HistogramWrapper(labelNames, labelMissingStrategy, labelExtractorMap, histogram);
        }
    }

    public static Builder build() {
        return new Builder();
    }

    private static final Histogram.Child FAKE_CHILD = Histogram.build()
            .name("fake_histogram").help("fake")
            .labelNames("fake").buckets(10, 20)
            .create()
            .labels("fake");

    protected HistogramWrapper(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Histogram histogram) {
        super(labelNames, labelMissingStrategy, labelExtractorMap, histogram);
    }


    public void observe(double amt) {
        try {
            if (shouldSkip()) {
                return;
            }

            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                realCollector.observe(amt);
                return;
            }

            realCollector.labels(labels).observe(amt);
        } finally {
            cleanLabels();
        }
    }

    public Histogram.Timer startTimer() {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.startTimer();
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.startTimer();
            }

            return realCollector.labels(labels).startTimer();
        } finally {
            cleanLabels();
        }
    }

    public double time(Runnable timeable) {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.time(timeable);
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.time(timeable);
            }

            return realCollector.labels(labels).time(timeable);
        } finally {
            cleanLabels();
        }
    }


    public <E> E time(Callable<E> timeable) {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.time(timeable);
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.time(timeable);
            }

            return realCollector.labels(labels).time(timeable);
        } finally {
            cleanLabels();
        }
    }
}
