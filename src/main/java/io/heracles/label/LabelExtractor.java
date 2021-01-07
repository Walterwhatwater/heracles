package io.heracles.label;

/**
 * 标签解析
 *
 * @author walter
 * @date 2021/01/07 22:37
 **/
public interface LabelExtractor<T> {
    /**
     * 从对象中解析出标签组合
     *
     * @param object 目标对象
     * @return 标签组合
     */
    Labels extractLabels(T object);

    /**
     * 规定解析器的目标标签名范围
     *
     * @return 目标标签名列表
     */
    LabelNames getTargetLabelNames();
}
