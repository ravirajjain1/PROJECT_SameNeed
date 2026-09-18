package com.sameneed.controller;

import com.sameneed.dao.jpa.ServiceJpaDao;
import com.sameneed.model.Service;
import com.sameneed.model.ServiceCategory;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/services/*")
public class ServiceServlet extends HttpServlet {

    private final ServiceJpaDao serviceDao = new ServiceJpaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String catIdStr = req.getParameter("categoryId");
            List<Service> services;
            if (catIdStr != null) {
                services = serviceDao.findByCategory(Integer.parseInt(catIdStr));
            } else {
                services = serviceDao.findAll();
            }
            res.getWriter().write(JsonUtil.toJson(services));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to load services\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String role = (String) req.getSession().getAttribute("role");
            if (!"ADMIN".equals(role)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"Admin access required\"}");
                return;
            }
            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            int categoryId = ((Number) body.get("categoryId")).intValue();
            ServiceCategory cat = new ServiceCategory();
            cat.setCategoryId(categoryId);
            Service svc = new Service(cat, (String) body.get("name"), (String) body.get("description"));
            serviceDao.insert(svc);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(svc));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to create service\"}");
        }
    }
}
