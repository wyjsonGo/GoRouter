package com.wyjson.router.compiler;

import com.google.auto.service.AutoService;
import com.wyjson.router.annotation.Service;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.wyjson.router.annotation.Service")
public class ServiceProcessor extends AbstractProcessor {

    public static final String ACTIVITY = "android.app.Activity";
    //Filer 就是文件流输出路径，当我们用AbstractProcess生成一个java类的时候，我们需要保存在Filer指定的目录下
    Filer mFiler;
    //类型 相关的工具类。当process执行的时候，由于并没有加载类信息，所以java文件中的类信息都是用element来代替了。
    //类型相关的都被转化成了一个叫TypeMirror，其getKind方法返回类型信息，其中包含了基础类型以及引用类型。
    Types types;
    //Elements 获取元素信息的工具，比如说一些类信息继承关系等。
    Elements elementUtils;
    //来报告错误、警告以及提示信息
    //用来写一些信息给使用此注解库的第三方开发者的
    Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.size() == 0){
            return false;
        }
        //获取所有包含 @Service 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Service.class);

        //每个Processor的独自的逻辑，其他的写法一般都是固定的

        return true;
    }

}
