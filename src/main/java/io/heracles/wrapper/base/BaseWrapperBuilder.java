package io.heracles.wrapper.base;

import io.heracles.label.LabelExtractor;
import io.heracles.label.LabelMissingStrategy;
import io.heracles.label.LabelNames;
import io.prometheus.client.SimpleCollector;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 构造带标签解析的采集器
 *
 * @author walter
 * @date 2021/01/07 22:32
 **/
public abstract class BaseWrapperBuilder<Wrapper extends BaseCollectorWrapper<Wrapper, RealCollector>, Builder extends BaseWrapperBuilder<Wrapper, Builder, RealCollector, RealBuilder>, RealCollector extends SimpleCollector<?>, RealBuilder extends SimpleCollector.Builder<RealBuilder, RealCollector>> {
    protected final RealBuilder realBuilder;

    /**
     * 目标Class -> 标签解析
     */
    private final Map<Class<?>, LabelExtractor<?>> labelExtractorMap = new HashMap<>();
    /**
     * @see LabelNames
     */
    private final LabelNames labelNames = LabelNames.newInstance();
    /**
     * @see LabelMissingStrategy
     */
    private LabelMissingStrategy labelMissingStrategy;

    protected BaseWrapperBuilder(RealBuilder realBuilder) {
        this.realBuilder = realBuilder;
    }

    /**
     * 设置标签缺失处理策略
     *
     * @param labelMissingStrategy 标签缺失处理策略
     * @return 当前对象
     * @see LabelMissingStrategy
     */
    public Builder labelMissingStrategy(LabelMissingStrategy labelMissingStrategy) {
        this.labelMissingStrategy = labelMissingStrategy;
        return (Builder) this;
    }

    /**
     * 添加不带默认值的标签名
     *
     * @param labelNames 标签名
     * @return 当前对象
     * @see LabelNames#names(String...)
     */
    public Builder labelNames(String... labelNames) {
        if (ArrayUtils.isEmpty(labelNames)) {
            return (Builder) this;
        }

        this.labelNames.names(labelNames);
        return (Builder) this;
    }

    /**
     * 添加标签名和对应默认值
     *
     * @param labelName  标签名
     * @param labelValue 默认值
     * @return 当前对象
     * @see LabelNames#name(String, String)
     */
    public Builder labelName(String labelName, String labelValue) {
        this.labelNames.name(labelName, labelValue);
        return (Builder) this;
    }

    /**
     * 注册标签解析逻辑，并指导解析范围
     *
     * @param extractor  标签解析
     * @param tClass     解析目标类型
     * @param labelNames 解析标签名范围
     * @param <T>        对应目标类型泛型
     * @return 当前对象
     */
    public <T> Builder labelExtractor(LabelExtractor<T> extractor, Class<T> tClass, String... labelNames) {
        if (extractor == null || tClass == null) {
            return (Builder) this;
        }

        final LabelNames targetLabelNames = extractor.getTargetLabelNames();
        if (targetLabelNames == null) {
            return (Builder) this;
        }

        if (ArrayUtils.isEmpty(labelNames)) {
            labelNames = targetLabelNames.toArray();
        }

        Stream.of(labelNames).filter(targetLabelNames::contains)
                .forEach(l -> this.labelNames.name(l, targetLabelNames.getDefaultValue(l)));

        this.labelExtractorMap.put(tClass, extractor);

        return (Builder) this;
    }

    /**
     * 注册标签解析逻辑，以解析器的标签解析范围作为默认范围
     *
     * @param extractor 标签解析
     * @param tClass    解析目标类型
     * @param <T>       对应目标类型泛型
     * @return 当前对象
     */
    public <T> Builder labelExtractor(LabelExtractor<T> extractor, Class<T> tClass) {
        return labelExtractor(extractor, tClass, extractor.getTargetLabelNames().toArray());
    }

    /**
     * @see SimpleCollector.Builder#name(String)
     */
    public Builder name(String name) {
        realBuilder.name(name);
        return (Builder) this;
    }

    /**
     * @see SimpleCollector.Builder#subsystem(String)
     */
    public Builder subsystem(String subsystem) {
        realBuilder.subsystem(subsystem);
        return (Builder) this;
    }

    /**
     * @see SimpleCollector.Builder#namespace(String)
     */
    public Builder namespace(String namespace) {
        realBuilder.namespace(namespace);
        return (Builder) this;
    }

    /**
     * @see SimpleCollector.Builder#help(String)
     */
    public Builder help(String help) {
        realBuilder.help(help);
        return (Builder) this;
    }

    /**
     * 调用实际构造器register方法生成实际采集器，并包装成Collector Wrapper
     *
     * @return Collector Wrapper
     * @see SimpleCollector.Builder#register()
     */
    public Wrapper wrap() {
        RealCollector realCollector = realBuilder.labelNames(this.labelNames.toArray()).register();
        return create(this.labelNames, this.labelMissingStrategy, this.labelExtractorMap, realCollector);
    }

    /**
     * 创建Collector Wrapper
     *
     * @param labelNames           标签名
     * @param labelMissingStrategy 标签缺失处理逻辑
     * @param labelExtractorMap    标签解析
     * @param realCollector        对应原生SDK Collector
     * @return Collector Wrapper
     */
    protected abstract Wrapper create(LabelNames labelNames, LabelMissingStrategy labelMissingStrategy, Map<Class<?>, LabelExtractor<?>> labelExtractorMap, RealCollector realCollector);
}
