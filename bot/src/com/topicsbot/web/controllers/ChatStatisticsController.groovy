package com.topicsbot.web.controllers

import com.topicsbot.model.chat.Chat
import com.topicsbot.services.cache.CacheService
import com.topicsbot.services.db.DBService
import com.topicsbot.services.db.query.ChatQuery
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.hibernate.Session

import javax.annotation.PostConstruct
import javax.enterprise.context.RequestScoped
import javax.faces.context.FacesContext
import javax.inject.Inject
import javax.inject.Named
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.Path

/**
 * author: Artem Voronov
 */
@Named
@Path("/chatStatistics")
@CompileStatic
@RequestScoped
@Log4j("logger")
class ChatStatisticsController {

  @Inject
  private CacheService cache

  @Inject
  private DBService db

  String chatId
  String chatTitle

  boolean error

  @PostConstruct
  void init() {
    try {
      HttpServletRequest request = FacesContext.currentInstance?.externalContext?.request as HttpServletRequest

      if (!request)
        return

      chatId = request.getAttribute('chatId')

      if (!chatId)
        return

      db.vtx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat
        chatTitle = chat.title ? chat.title : "Topics Bot"
        //TODO
//        ChatDayStatistics chatDayStatistics = cache.getChatStatistics(chat)
//        Map<String, UserDayStatistics> flooders = cache.getFlooders(chat)

      }


    } catch (Exception ignored) {
      error = true
    }
  }

}
