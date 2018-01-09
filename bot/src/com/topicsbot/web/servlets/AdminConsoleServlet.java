package com.topicsbot.web.servlets;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author: Artem Voronov
 */
public class AdminConsoleServlet extends HttpServlet {

  private static final String DESTINATION_PARAM = "destination";
  private String destination;

  @Override
  public void init(ServletConfig config) throws ServletException {
    destination = config.getInitParameter(DESTINATION_PARAM);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RequestDispatcher rd = request.getRequestDispatcher(destination);
    rd.forward(request, response);
  }
}
