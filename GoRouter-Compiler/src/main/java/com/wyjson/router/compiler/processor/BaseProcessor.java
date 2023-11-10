package com.wyjson.router.compiler.processor;

import static com.wyjson.router.compiler.utils.Constants.KEY_GENERATE_DOC_NAME;
import static com.wyjson.router.compiler.utils.Constants.KEY_MODULE_NAME;
import static com.wyjson.router.compiler.utils.Constants.NO_MODULE_NAME_TIPS;
import static com.wyjson.router.compiler.utils.Constants.PREFIX_OF_LOGGER;
import static com.wyjson.router.compiler.utils.Constants.VALUE_ENABLE;

import com.wyjson.router.compiler.utils.Logger;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class BaseProcessor extends AbstractProcessor {

    Filer mFiler;
    Logger logger;
    Types types;
    Elements elementUtils;
    // Module name, maybe its 'app' or others
    String moduleName = null;
    String generateClassName;
    boolean isGenerateDoc;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        logger = new Logger(processingEnv.getMessager());

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
            isGenerateDoc = VALUE_ENABLE.equals(options.get(KEY_GENERATE_DOC_NAME));
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            generateClassName = strToClassName(moduleName);

            logger.info(moduleName + " The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error(NO_MODULE_NAME_TIPS);
            throw new RuntimeException(PREFIX_OF_LOGGER + "No module name, for more information, look at gradle log.");
        }
    }

    private String strToClassName(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        // 去除首字母是0-9和_的情况
        str = str.replaceAll("^[0-9_]", "");
        // 首字母大写
        str = str.substring(0, 1).toUpperCase() + str.substring(1);
        // 处理下划线
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '_') {
                if (++i < len) {
                    sb.append(Character.toUpperCase(str.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
            this.add(KEY_GENERATE_DOC_NAME);
        }};
    }
}
