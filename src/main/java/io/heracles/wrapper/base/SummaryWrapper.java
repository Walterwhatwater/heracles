package io.heracles.wrapper.base;

import io.heracles.label.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.prometheus.client.Summary;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 带标签解析的Summary
 *
 * @author walter
 * @date 2021/01/08 00:09
 **/
public class SummaryWrapper extends BaseCollectorWrapper<SummaryWrapper, Summary> {
    public static class Builder extends BaseWrapperBuilder<SummaryWrapper, Builder, Summary, Summary.Builder> {
        protected Builder() {
            super(Summary.build());
        }

        public Builder quantile(double quantile, double error) {
            realBuilder.quantile(quantile, error);
            return this;
        }

        public Builder maxAgeSeconds(long maxAgeSeconds) {
            realBuilder.maxAgeSeconds(maxAgeSeconds);
            return this;
        }

        public Builder ageBuckets(int ageBuckets) {
            realBuilder.ageBuckets(ageBuckets);
            return this;
        }

        @Override
        protected SummaryWrapper create(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Summary summary) {
            return new SummaryWrapper(labelNames, labelMissingStrategy, labelExtractorMap, summary);
        }
    }

    private static final Summary.Child FAKE_CHILD = Summary.build()
            .name("fake_summary").help("fake").quantile(0.5, 0.1)
            .labelNames("fake")
            .create()
            .labels("fake");

    public static Builder build() {
        return new Builder();
    }

    protected SummaryWrapper(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Summary summary) {
        super(labelNames, labelMissingStrategy, labelExtractorMap, summary);
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

    public Summary.Timer startTimer() {
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

    public Summary.Child.Value get() {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.get();
            }

            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.get();
            }

            return realCollector.labels(labels).get();
        } finally {
            cleanLabels();
        }
    }
}
