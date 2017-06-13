package com.topicsbot.web.controllers.statistics

import com.topicsbot.model.chat.Chat
import com.topicsbot.model.statistics.ChatDayStatistics
import com.topicsbot.model.statistics.UserDayStatistics
import com.topicsbot.services.cache.CacheService
import com.topicsbot.services.db.DBService
import com.topicsbot.services.db.query.ChatQuery
import com.topicsbot.web.controllers.statistics.model.ChatStatistics
import com.topicsbot.web.controllers.statistics.model.UserStatistics
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.hibernate.Session

import javax.annotation.PostConstruct
import javax.enterprise.context.RequestScoped
import javax.faces.context.FacesContext
import javax.inject.Inject
import javax.inject.Named
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 * author: Artem Voronov
 */
@Named
@Path("/chat_statistics")
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

      }


    } catch (Exception ignored) {
      error = true
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/today")
  ChatStatistics statistics(@QueryParam("chatId") String chatId, @Context SecurityContext ctx) {
    try{
      return db.tx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat

        if (!chat) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("CHAT_NOT_FOUND").build())
        }

        ChatDayStatistics chatDayStatistics = cache.getChatStatistics(chat)
        Map<String, UserDayStatistics> flooders = cache.getFlooders(chat)
        ChatStatistics result = new ChatStatistics()

        if (chatDayStatistics == null || flooders == null)
          return result

        result.users = flooders.size()
        result.flood = chatDayStatistics.floodSize
        result.messages = chatDayStatistics.messageCounter
        result.words = chatDayStatistics.wordCounter

        List<UserStatistics> userStatisticsList = new ArrayList<>(flooders.size())

        Collection<UserDayStatistics> sorted = flooders.values().sort({a,b-> b.floodSize<=>a.floodSize})
        for (UserDayStatistics stat : sorted) {
          UserStatistics rec = new UserStatistics()
          rec.messages = stat.messageCounter
          rec.words = stat.wordCounter
          float floodPercentNotFormatted = (float) stat.floodSize /  chatDayStatistics.floodSize * 100
          if (Float.isNaN(floodPercentNotFormatted)) {
            floodPercentNotFormatted = 0
          }
          rec.floodPercent = String.format("%.2f", floodPercentNotFormatted)
          rec.name = stat.user.name
          userStatisticsList << rec
        }

        result.userStatistics = userStatisticsList

        return result
      }
    } catch (Exception ignored) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).type(MediaType.TEXT_PLAIN_TYPE).entity("UNKNOWN").build())
    }
  }

}
