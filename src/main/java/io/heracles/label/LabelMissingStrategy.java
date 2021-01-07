package io.heracles.label;

/**
 * 标签缺失时处理策略
 *
 * @author walter
 * @date 2021/01/07 22:59
 **/
public enum LabelMissingStrategy {
    /**
     * 默认处理逻辑，交由原生Prometheus SDK处理
     */
    DEFAULT,
    /**
     * 用空字符串填充
     */
    FILL_WITH_EMPTY_STRING,
    /**
     * 跳过当前打点
     */
    SKIP;
}
