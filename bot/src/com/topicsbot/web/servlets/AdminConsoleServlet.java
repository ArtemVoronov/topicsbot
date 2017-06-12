package com.topicsbot.web.servlets;

import com.topicsbot.BotContext;
import com.topicsbot.services.cache.CacheInfo;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.template.TemplateService;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

/**
 * Author: Artem Voronov
 */
public class AdminConsoleServlet extends HttpServlet {

  private static final Logger logger = Logger.getLogger("ADMIN_CONSOLE_SERVLET");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);

      CacheService cache = BotContext.getServices().getCacheService();
      TemplateService templateService = BotContext.getServices().getTemplateService();

      CacheInfo cacheInfo = cache.getCacheInfo(LocalDate.now());
      String info = templateService.getAdminConsolePage(cacheInfo);

      try (PrintWriter out = response.getWriter()) {
        out.write(info);
      }
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }

  }
}
