package io.heracles.wrapper;

import io.heracles.label.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.heracles.wrapper.base.BaseCollectorWrapper;
import io.heracles.wrapper.base.BaseWrapperBuilder;
import io.prometheus.client.Counter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

/**
 * 带标签解析的Counter
 *
 * @author walter
 * @date 2021/01/07 23:45
 **/
public class CounterWrapper extends BaseCollectorWrapper<CounterWrapper, Counter> {
    public static class Builder extends BaseWrapperBuilder<CounterWrapper, Builder, Counter, Counter.Builder> {
        protected Builder() {
            super(Counter.build());
        }

        @Override
        protected CounterWrapper create(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Counter counter) {
            return new CounterWrapper(labelNames, labelMissingStrategy, labelExtractorMap, counter);
        }
    }

    private static final double DEFAULT_AMT = 1d;

    public static Builder builder() {
        return new Builder();
    }

    protected CounterWrapper(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Counter counter) {
        super(labelNames, labelMissingStrategy, labelExtractorMap, counter);
    }

    public void inc() {
        inc(DEFAULT_AMT);
    }

    public void inc(double amt) {
        try {
            if (shouldSkip()) {
                return;
            }

            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                realCollector.inc(amt);
                return;
            }

            realCollector.labels(labels).inc(amt);
        } finally {
            cleanLabels();
        }
    }
}
