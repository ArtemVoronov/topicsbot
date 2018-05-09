package com.topicsbot.ui.telegram.web.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Author: Artem Voronov
 */

public class WebHookServlet extends HttpServlet {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    handleRequest(request, response);
  }

  private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    final String uri = request.getRequestURI();

//    if (uri == null || !uri.startsWith("/" + BotContext.getToken()))//TODO: inject editable config
//      return;
//
//    response.setCharacterEncoding("UTF-8");
//    response.setStatus(HttpServletResponse.SC_OK);
//
//    StringBuilder jb = new StringBuilder();
//    String line = null;
//    try (final BufferedReader reader = request.getReader()) { //TODO: better choice: String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//      while ((line = reader.readLine()) != null)
//        jb.append(line);
//    }
//
//    //TODO: add api client inject
//    Update update = MAPPER.readValue(jb.toString(), Update.class);
//    TelegramApiProvider provider = BotContext.getServices().getTelegramApiProvider();
//    provider.processUpdate(update);
  }
}
