package com.sameneed.controller;

import com.sameneed.dao.jpa.ServiceCategoryJpaDao;
import com.sameneed.model.ServiceCategory;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {

    private final ServiceCategoryJpaDao categoryDao = new ServiceCategoryJpaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            List<ServiceCategory> categories = categoryDao.findAll();
            res.getWriter().write(JsonUtil.toJson(categories));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to load categories\"}");
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
            java.util.Map<String, Object> body = JsonUtil.fromJson(req.getReader(), java.util.Map.class);
            ServiceCategory cat = new ServiceCategory(
                    (String) body.get("name"),
                    (String) body.get("description"));
            categoryDao.insert(cat);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(cat));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to create category\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String role = (String) req.getSession().getAttribute("role");
            if (!"ADMIN".equals(role)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"Admin access required\"}");
                return;
            }
            String path = req.getPathInfo();
            int categoryId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());
            categoryDao.delete(categoryId);
            res.getWriter().write("{\"message\":\"Category deleted\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to delete category\"}");
        }
    }
}
