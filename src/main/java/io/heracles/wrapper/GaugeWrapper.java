package io.heracles.wrapper;

import io.heracles.label.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.heracles.wrapper.base.BaseCollectorWrapper;
import io.heracles.wrapper.base.BaseWrapperBuilder;
import io.prometheus.client.Gauge;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 带标签解析的Gauge
 *
 * @author walter
 * @date 2021/01/07 23:51
 **/
public class GaugeWrapper extends BaseCollectorWrapper<GaugeWrapper, Gauge> {
    public static class Builder extends BaseWrapperBuilder<GaugeWrapper, Builder, Gauge, Gauge.Builder> {
        protected Builder() {
            super(Gauge.build());
        }

        @Override
        protected GaugeWrapper create(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Gauge gauge) {
            return new GaugeWrapper(labelNames, labelMissingStrategy, labelExtractorMap, gauge);
        }
    }

    private static final Gauge.Child FAKE_CHILD = new Gauge.Child();
    private static final double DEFAULT_AMT = 1d;

    public static Builder build() {
        return new Builder();
    }

    protected GaugeWrapper(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, Gauge gauge) {
        super(labelNames, labelMissingStrategy, labelExtractorMap, gauge);
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


    public void dec() {
        dec(DEFAULT_AMT);
    }

    public void dec(double amt) {
        try {
            if (shouldSkip()) {
                return;
            }

            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                realCollector.dec(amt);
                return;
            }

            realCollector.labels(labels).dec(amt);
        } finally {
            cleanLabels();
        }
    }

    public void set(double val) {
        try {
            if (shouldSkip()) {
                return;
            }

            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                realCollector.set(val);
                return;
            }

            realCollector.labels(labels).set(val);
        } finally {
            cleanLabels();
        }
    }

    public void setToCurrentTime() {
        try {
            if (shouldSkip()) {
                return;
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                realCollector.setToCurrentTime();
                return;
            }

            realCollector.labels(labels).setToCurrentTime();
        } finally {
            cleanLabels();
        }
    }

    public Gauge.Timer startTimer() {
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

    public double setToTime(Runnable timeable) {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.setToTime(timeable);
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.setToTime(timeable);
            }

            return realCollector.labels(labels).setToTime(timeable);
        } finally {
            cleanLabels();
        }
    }

    public <E> E setToTime(Callable<E> timeable) {
        try {
            if (shouldSkip()) {
                return FAKE_CHILD.setToTime(timeable);
            }
            String[] labels = getCurrentLabels();
            if (ArrayUtils.isEmpty(labels)) {
                return realCollector.setToTime(timeable);
            }

            return realCollector.labels(labels).setToTime(timeable);
        } finally {
            cleanLabels();
        }
    }

    public double get() {
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
