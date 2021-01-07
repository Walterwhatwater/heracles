# Heracles - A Prometheus Extension
## 扩展了什么
1. 原生的Prometheus SDK在进行打点时需要注意标签声明的顺序和打点时填充的顺序一致，并且数量也要一致，比较危险   
Heracles中通过label name + label value的方式填充标签，由SDK托管标签顺序，以及处理标签缺少的情况   
2. 实际使用中，我们也经常会遇到一个场景是从当前请求对象或者上下文对象种解析出一些标签   
Heracles中通过LabelExtractor接口实现这个能力
## 自定义标签解析的Collector Wrapper
### 1. 实现一个标签解析器
LabelExtractor接口定义了标签解析器，实现一个标签解析器需要实现：   
- 从某个对象种解析出标签组合的逻辑
- 定义这个解析器能解析哪些标签名
```java
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
```
这里举一个例子，从SomeClass类型对象种解析出"label1"和"label2"两个标签
```java
class SomeClass {
    private String label1;
    private String label2;

    public String getLabel1() {
        return label1;
    }

    public String getLabel2() {
        return label2;
    }

    public void setLabel1(String label1) {
        this.label1 = label1;
    }

    public void setLabel2(String label2) {
        this.label2 = label2;
    }
}

public class SomeLabelExtractor implements LabelExtractor<SomeClass> {
    @Override
    public Labels extractLabels(SomeClass object) {
        if (object == null) {
            return Labels.newInstance();
        }
        
        return Labels.newInstance()
                .label("label1", object.getLabel1())
                .label("label2", object.getLabel2());
    }

    @Override
    public LabelNames getTargetLabelNames() {
        return LabelNames.newInstance().names("label1", "label2");
    }
}
```
### 2. 定义一个Collector Wrapper
所有类型的Wrapper以及Wrapper Builder都对外暴露和原生Prometheus SDK相同的接口，用法和含义也相同   
这里以Counter类型对应的CounterWrapper为例   
通过labelExtractor方法将上面这个SomeLabelExtractor注册到采集器中，这样打点的时候就能通过SomeClass的对象解析label1和label2了   
也可以通过labelNames方法添加其他标签   
可以通过调用多次labelExtractor添加多个解析器   
```java
CounterWrapper counterWrapper = CounterWrapper.builder()
            .name("some_metric_total")
            .help("blah blah blah")
            .labelExtractor(new SomeLabelExtractor(), SomeClass.class)
            .labelNames("other_label1", "other_label2")
            .wrap();
```
### 3. 打点
```java
SomeClass someObject = new SomeClass();
someObject.setLabel1("value1");
someObject.setLabel2("value2");
counterWrapper.label(someObject, SomeClass.class)
            .label("other_label1", "other_value1")
            .label("other_label2", "other_value2")
            .inc();
```
### 4. 增强能力
除了可以通过对象解析标签的能力，还有一些其他能力的增强
#### 4.1 自定义标签解析范围
实际使用中，我们可以预定义一些常用的LabelExtractor供打点时选择，但是不一定每个打点都需要用到这个Extractor的全部标签   
例如我们有一个SomeLabelExtractor，能够解析出A,B,C,D四个标签，打点时我们只希望用到A,B两个标签，可以这么定义采集器
```java
CounterWrapper counterWrapper = CounterWrapper.builder()
            .name("some_metric_total")
            .help("blah blah blah")
            .labelExtractor(new SomeLabelExtractor(), SomeClass.class, "A", "B")
            .wrap();
```
#### 4.2 标签默认值 
有些标签的值是固定的，或者我们希望它有一个默认值，这样我们不需要每次打点时都去填充它   
可以在定义采集器时这样写，让other_label1有一个默认值default_value1
```java
CounterWrapper counterWrapper = CounterWrapper.builder()
            .name("some_metric_total")
            .help("blah blah blah")
            .labelName("other_label1", "default_value1")
            .wrap();
```
在实现标签解析时也可以定义默认值
```java
public class SomeLabelExtractor implements LabelExtractor<SomeClass> {
    @Override
    public Labels extractLabels(SomeClass object) {
        if (object == null) {
            return Labels.newInstance();
        }
        
        return Labels.newInstance()
                .label("label1", object.getLabel1())
                .label("label2", object.getLabel2());
    }

    @Override
    public LabelNames getTargetLabelNames() {
        return LabelNames.newInstance()
                .name("label1", "default_value1")
                .name("label2", "default_value2");
    }
}
```
#### 4.3 标签缺失时的处理策略
原生SDK中如果少了填标签会产生RuntimeException，非常危险   
可以在定义采集器时指定标签缺失时的策略
```java
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
```
```java
CounterWrapper counterWrapper = CounterWrapper.builder()
            .name("some_metric_total")
            .help("blah blah blah")
            .labelMissingStrategy(LabelMissingStrategy.FILL_WITH_EMPTY_STRING)
            .labelName("other_label1", "default_value1")
            .wrap();
```
