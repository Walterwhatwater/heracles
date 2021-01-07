package io.heracles.label;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 标签名及其对应默认值列表
 *
 * @author walter
 * @date 2021/01/07 22:37
 **/
public class LabelNames {
    /**
     * 标签名 -> 默认值（null代表无默认值）
     */
    private final Map<String, String> labelNamesWithDefaultValues = new LinkedHashMap<>();

    private LabelNames() {
    }

    public static LabelNames newInstance() {
        return new LabelNames();
    }

    /**
     * 添加标签名，不含默认值
     *
     * @param labelNames 标签名列表
     * @return 当前对象
     */
    public LabelNames names(String... labelNames) {
        if (ArrayUtils.isEmpty(labelNames)) {
            return this;
        }

        Stream.of(labelNames)
                .filter(l -> !StringUtils.isEmpty(l))
                .forEach(l -> this.labelNamesWithDefaultValues.put(l, null));

        return this;
    }

    /**
     * 添加标签名和对应默认值
     *
     * @param labelName    标签名
     * @param defaultValue 默认值
     * @return 当前对象
     */
    public LabelNames name(String labelName, String defaultValue) {
        if (StringUtils.isEmpty(labelName)) {
            return this;
        }

        this.labelNamesWithDefaultValues.put(labelName, defaultValue);
        return this;
    }

    /**
     * 获取标签名对应默认值
     *
     * @param labelName 标签名
     * @return 默认值，无默认值返回null
     */
    public String getDefaultValue(String labelName) {
        if (StringUtils.isEmpty(labelName)) {
            return null;
        }

        return this.labelNamesWithDefaultValues.get(labelName);
    }

    /**
     * 是否包含某个标签名
     *
     * @param labelName 标签名
     * @return true=包含，false=不包含
     */
    public boolean contains(String labelName) {
        if (StringUtils.isEmpty(labelName)) {
            return false;
        }

        return this.labelNamesWithDefaultValues.containsKey(labelName);
    }

    public boolean isEmpty() {
        return this.labelNamesWithDefaultValues.isEmpty();
    }

    /**
     * 获取标签名数组
     *
     * @return 标签名数组
     */
    public String[] toArray() {
        return this.labelNamesWithDefaultValues.keySet().stream()
                .filter(k -> !StringUtils.isEmpty(k))
                .toArray(String[]::new);
    }
}
