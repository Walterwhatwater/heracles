package io.heracles.label;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 标签组合
 *
 * @author walter
 * @date 2021/01/07 22:37
 **/
public class Labels {
    /**
     * 标签名 -> 标签值
     */
    private final Map<String, String> labelMap = new HashMap<>();

    private Labels() {
    }

    public static Labels newInstance() {
        return new Labels();
    }

    /**
     * 设置标签名对应值
     *
     * @param labelName  标签名
     * @param labelValue 标签值，null会被忽略
     * @return 当前对象
     */
    public Labels label(String labelName, Object labelValue) {
        if (StringUtils.isEmpty(labelName) || labelValue == null) {
            return this;
        }

        this.labelMap.put(labelName, labelValue.toString());
        return this;
    }

    public Labels with(Labels another) {
        if (another == null) {
            return this;
        }

        for (Map.Entry<String, String> entry : another.getCurrentLabels().entrySet()) {
            label(entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * 获取当前标签值组合
     *
     * @return 标签值组合
     */
    public Map<String, String> getCurrentLabels() {
        return ImmutableMap.copyOf(this.labelMap);
    }

    /**
     * 获取标签名对应值
     *
     * @param labelName 标签名
     * @return 标签值
     */
    public String getLabelValue(String labelName) {
        return this.labelMap.get(labelName);
    }
}
