package com.wyjson.router.document;

import androidx.arch.core.util.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.wyjson.router.core.CardMeta;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.core.RouteHashMap;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorTreeMap;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.service.ServiceHashMap;
import com.wyjson.router.service.ServiceMeta;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class DocumentUtils {

    private static Gson gson;

    /**
     * 生成JSON格式文档
     * ***只有添加进路由的路径、服务、拦截器才会出现在文档里***
     *
     * @param documentModel
     * @param tagFunction   不处理返回默认int类型tag,实现方法可自定义返回tag,示例[LOGIN, AUTHENTICATION]
     * @return JSON格式文档
     */
    public static String generate(DocumentModel documentModel, Function<Integer, String> tagFunction) {
        if (!GoRouter.logger.isShowLog()) {
            throw new RouterException("Call GoRouter.openLog(); Log enabled after use!");
        }
        String json;
        gson = new GsonBuilder()
                .registerTypeAdapter(Class.class, new ClassDataSerializer())
                .registerTypeAdapter(DocumentModel.class, new DocumentModelTypeAdapter())
                .registerTypeAdapter(ServiceHashMap.class, new ServiceHashMapTypeAdapter())
                .registerTypeAdapter(RouteHashMap.class, new RouteHashMapTypeAdapter())
                .registerTypeAdapter(ServiceMeta.class, new ServiceMetaTypeAdapter())
                .registerTypeAdapter(InterceptorTreeMap.class, new InterceptorTreeMapTypeAdapter())
                .registerTypeAdapter(CardMeta.class, new CardMetaTypeAdapter(tagFunction))
                .create();
        json = gson.toJson(documentModel);
        return json;
    }

    static class DocumentModelTypeAdapter extends TypeAdapter<DocumentModel> {
        @Override
        public void write(JsonWriter out, DocumentModel value) throws IOException {
            out.beginObject();

            if (MapUtils.isNotEmpty(value.getServices())) {
                out.name("services").jsonValue(gson.toJson(value.getServices()));
            } else {
                out.name("services").beginObject().endObject();
            }

            if (MapUtils.isNotEmpty(value.getInterceptors())) {
                out.name("interceptors").jsonValue(gson.toJson(value.getInterceptors()));
            } else {
                out.name("interceptors").beginObject().endObject();
            }

            if (MapUtils.isNotEmpty(value.getRoutes())) {
                out.name("routes").jsonValue(gson.toJson(value.getRoutes()));
            } else {
                out.name("routes").beginObject().endObject();
            }

            out.endObject();
        }

        @Override
        public DocumentModel read(JsonReader in) throws IOException {
            return null;
        }
    }

    static class ServiceHashMapTypeAdapter<K, V> extends TypeAdapter<ServiceHashMap<K, V>> {

        @Override
        public void write(JsonWriter out, ServiceHashMap<K, V> map) throws IOException {
            if (MapUtils.isEmpty(map)) {
                out.nullValue();
                return;
            }

            out.beginObject();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                out.name(((Class<? extends IService>) entry.getKey()).getSimpleName()).jsonValue(gson.toJson(entry.getValue()));
            }
            out.endObject();
        }

        @Override
        public ServiceHashMap<K, V> read(JsonReader in) throws IOException {
            return null;
        }

    }

    static class RouteHashMapTypeAdapter<K, V> extends TypeAdapter<RouteHashMap<K, V>> {

        @Override
        public void write(JsonWriter out, RouteHashMap<K, V> map) throws IOException {
            if (MapUtils.isEmpty(map)) {
                out.nullValue();
                return;
            }

            out.beginArray();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                out.jsonValue(gson.toJson(entry.getValue()));
            }
            out.endArray();
        }

        @Override
        public RouteHashMap<K, V> read(JsonReader in) throws IOException {
            return null;
        }

    }

    static class InterceptorTreeMapTypeAdapter<K, V> extends TypeAdapter<InterceptorTreeMap<K, V>> {

        @Override
        public void write(JsonWriter out, InterceptorTreeMap<K, V> map) throws IOException {
            if (MapUtils.isEmpty(map)) {
                out.nullValue();
                return;
            }

            out.beginObject();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                out.name(entry.getKey().toString()).value(entry.getValue().getClass().getName());
            }
            out.endObject();
        }

        @Override
        public InterceptorTreeMap<K, V> read(JsonReader in) throws IOException {
            return null;
        }

    }

    static class ServiceMetaTypeAdapter extends TypeAdapter<ServiceMeta> {
        @Override
        public void write(JsonWriter out, ServiceMeta value) throws IOException {
            out.value(value.getServiceClass().getName());
        }

        @Override
        public ServiceMeta read(JsonReader in) throws IOException {
            return null;
        }
    }

    static class CardMetaTypeAdapter extends TypeAdapter<CardMeta> {

        Function<Integer, String> tagFunction;

        public CardMetaTypeAdapter(Function<Integer, String> tagFunction) {
            this.tagFunction = tagFunction;
        }

        @Override
        public void write(JsonWriter out, CardMeta value) throws IOException {
            out.beginObject();
            out.name("path").value(value.getPath());
            out.name("remark").value(value.getRemark());
            out.name("type").value(value.getType().toString());
            out.name("pathClass").value(value.getPathClass().getName());
            if (value.getTag() != 0) {
                if (tagFunction != null) {
                    String tagValue = tagFunction.apply(value.getTag());
                    if (!TextUtils.isEmpty(tagValue)) {
                        out.name("tag").value(tagValue);
                    } else {
                        out.name("tag").value(value.getTag());
                    }
                } else {
                    out.name("tag").value(value.getTag());
                }
            }
            if (MapUtils.isNotEmpty(value.getParamsType())) {
                out.name("paramsType").jsonValue(gson.toJson(value.getParamsType()));
            }
            out.endObject();
        }

        @Override
        public CardMeta read(JsonReader in) throws IOException {
            return null;
        }
    }

    static class ClassDataSerializer implements JsonSerializer<Class<?>> {
        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

}
