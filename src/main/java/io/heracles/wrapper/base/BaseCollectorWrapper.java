package io.heracles.wrapper.base;

import com.google.common.annotations.VisibleForTesting;
import io.heracles.label.extractor.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.heracles.label.Labels;
import io.heracles.util.CollectionUtils;
import io.prometheus.client.SimpleCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 带标签解析的采集器
 *
 * @author walter
 * @date 2021/01/07 22:33
 **/
public class BaseCollectorWrapper<Wrapper extends BaseCollectorWrapper<Wrapper, RealCollector>, RealCollector extends SimpleCollector<?>> {
    protected final RealCollector realCollector;

    /**
     * 目标类型 -> 标签解析
     */
    private final Map<Class<?>, LabelExtractor<?>> labelExtractorMap;
    /**
     * @see LabelMissingStrategy
     */
    private final LabelMissingStrategy labelMissingStrategy;
    /**
     * @see LabelNames
     */
    private final LabelNames labelNames;
    /**
     * Collector Wrapper线程安全
     */
    private final ThreadLocal<Labels> labelsThreadLocal = new ThreadLocal<>();

    protected BaseCollectorWrapper(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, RealCollector realCollector) {
        this.labelNames = labelNames;
        this.labelMissingStrategy = labelMissingStrategy;
        this.labelExtractorMap = labelExtractorMap;
        this.realCollector = realCollector;
    }

    /**
     * 从目标对象解析并填充标签
     *
     * @param object 目标对象
     * @param tClass 目标对象类型
     * @param <T>    目标对象类型泛型
     * @return 当前对象
     * @see LabelExtractor#extractLabels(Object)
     */
    public <T> Wrapper label(T object, Class<T> tClass) {
        if (tClass == null || CollectionUtils.isEmpty(this.labelExtractorMap)) {
            return (Wrapper) this;
        }

        LabelExtractor<T> labelExtractor = (LabelExtractor<T>) this.labelExtractorMap.get(tClass);
        if (labelExtractor == null) {
            return (Wrapper) this;
        }

        Labels labels = labelExtractor.extractLabels(object);
        getTempLabels().with(labels);

        return (Wrapper) this;
    }

    /**
     * 填充标签
     *
     * @param labelName  标签名
     * @param labelValue 标签值
     * @return 当前对象
     * @see Labels#label(String, Object)
     */
    public Wrapper label(String labelName, Object labelValue) {
        getTempLabels().label(labelName, labelValue);
        return (Wrapper) this;
    }

    @VisibleForTesting
    public String[] getCurrentLabels() {
        if (this.labelNames.isEmpty()) {
            return new String[0];
        }

        String[] labelNameArray = this.labelNames.toArray();
        List<String> labelValues = new ArrayList<>(labelNameArray.length);
        Labels labels = getTempLabels();
        for (String labelName : labelNameArray) {
            String labelValue = labels.getLabelValue(labelName);

            // 先看是否有默认值
            if (labelValue == null) {
                labelValue = this.labelNames.getDefaultValue(labelName);
            }

            // 默认值也为空 且 缺失策略为FILL_WITH_EMPTY_STRING时 填充空字符
            if (labelValue == null && LabelMissingStrategy.FILL_WITH_EMPTY_STRING.equals(this.labelMissingStrategy)) {
                labelValue = "";
            }

            labelValues.add(labelValue);
        }

        return labelValues.stream().filter(Objects::nonNull).toArray(String[]::new);
    }

    @VisibleForTesting
    public boolean shouldSkip() {
        if (!LabelMissingStrategy.SKIP.equals(this.labelMissingStrategy)
                || this.labelNames.isEmpty()) {
            return false;
        }

        Labels labels = getTempLabels();
        return Stream.of(this.labelNames.toArray())
                .anyMatch(labelName -> labels.getLabelValue(labelName) == null && this.labelNames.getDefaultValue(labelName) == null);
    }

    protected void cleanLabels() {
        this.labelsThreadLocal.remove();
    }

    private Labels getTempLabels() {
        Labels labels = this.labelsThreadLocal.get();
        if (labels == null) {
            labels = Labels.newInstance();
            this.labelsThreadLocal.set(labels);
        }
        return labels;
    }
}
