package com.wyjson.router.route;

import androidx.annotation.Nullable;

import com.wyjson.router.core.Card;
import com.wyjson.router.core.CardMeta;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.utils.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class RouteHelper {

    private RouteHelper() {
    }

    private static class InstanceHolder {
        private static final RouteHelper mInstance = new RouteHelper();
    }

    public static RouteHelper getInstance() {
        return InstanceHolder.mInstance;
    }

    private static final Map<String, CardMeta> routes = new HashMap<>();

    public Map<String, CardMeta> getRoutes() {
        return routes;
    }

    @Nullable
    public CardMeta getCardMeta(Card card) {
        CardMeta cardMeta = RouteHelper.getInstance().getRoutes().get(card.getPath());
        if (cardMeta != null) {
            GoRouter.logger.info(null, "[getCardMeta] " + cardMeta);
        } else {
            GoRouter.logger.warning(null, "[getCardMeta] null");
        }
        return cardMeta;
    }

    public void addCardMeta(CardMeta cardMeta) {
        if (TextUtils.isEmpty(cardMeta.getPath())) {
            throw new RouterException("path Parameter is invalid!");
        }
        // 检查路由是否有重复提交的情况
        if (GoRouter.isDebug()) {
            for (Map.Entry<String, CardMeta> cardMetaEntry : routes.entrySet()) {
                if (TextUtils.equals(cardMetaEntry.getKey(), cardMeta.getPath())) {
                    GoRouter.logger.error(null, "[addCardMeta] Path duplicate commit!!! path[" + cardMetaEntry.getValue().getPath() + "]");
                    break;
                } else if (cardMetaEntry.getValue().getPathClass() == cardMeta.getPathClass()) {
                    GoRouter.logger.error(null, "[addCardMeta] PathClass duplicate commit!!! pathClass[" + cardMetaEntry.getValue().getPathClass() + "]");
                    break;
                }
            }
        }
        routes.put(cardMeta.getPath(), cardMeta);
        GoRouter.logger.debug(null, "[addCardMeta] size:" + routes.size() + ", commit:" + cardMeta);
    }

}