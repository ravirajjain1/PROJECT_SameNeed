package com.sameneed.controller;

import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/meta")
public class MetaServlet extends HttpServlet {

    private static final Map<String, String> KNOWN_ENTITIES = new HashMap<>();

    static {
        KNOWN_ENTITIES.put("ServiceRequest", "com.sameneed.model.ServiceRequest");
        KNOWN_ENTITIES.put("Offer", "com.sameneed.model.Offer");
        KNOWN_ENTITIES.put("Booking", "com.sameneed.model.Booking");
        KNOWN_ENTITIES.put("RequestMember", "com.sameneed.model.RequestMember");
        KNOWN_ENTITIES.put("Review", "com.sameneed.model.Review");
        KNOWN_ENTITIES.put("Notification", "com.sameneed.model.Notification");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");

        String entityName = req.getParameter("entity");

        if (entityName == null || !KNOWN_ENTITIES.containsKey(entityName)) {
            res.getWriter().write(JsonUtil.toJson(KNOWN_ENTITIES.keySet()));
            return;
        }

        String className = KNOWN_ENTITIES.get(entityName);
        try {
            Class<?> clazz = Class.forName(className);
            Field[] fields = clazz.getDeclaredFields();
            List<Map<String, String>> fieldInfo = new ArrayList<>();
            for (Field field : fields) {
                Map<String, String> info = new HashMap<>();
                info.put("name", field.getName());
                info.put("type", field.getType().getSimpleName());
                info.put("modifier", java.lang.reflect.Modifier.toString(field.getModifiers()));
                fieldInfo.add(info);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("entity", entityName);
            result.put("className", className);
            result.put("superClass", clazz.getSuperclass() != null ? clazz.getSuperclass().getSimpleName() : "none");
            result.put("fields", fieldInfo);
            res.getWriter().write(JsonUtil.toJson(result));
        } catch (ClassNotFoundException e) {
            res.setStatus(404);
            res.getWriter().write("{\"error\":\"Entity class not found\"}");
        }
    }
}
