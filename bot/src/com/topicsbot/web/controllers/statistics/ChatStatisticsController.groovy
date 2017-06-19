package com.topicsbot.web.controllers.statistics

import com.topicsbot.model.chat.Chat
import com.topicsbot.model.statistics.ChatDayStatistics
import com.topicsbot.model.statistics.UserDayStatistics
import com.topicsbot.services.analysis.AnalysisProvider
import com.topicsbot.services.cache.CacheService
import com.topicsbot.services.db.DBService
import com.topicsbot.services.db.query.ChatQuery
import com.topicsbot.services.db.query.ChatStatisticsQuery
import com.topicsbot.services.db.query.UserStatisticsQuery
import com.topicsbot.web.controllers.statistics.model.ChatStatistics
import com.topicsbot.web.controllers.statistics.model.KeywordStatistics
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors

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
  private AnalysisProvider analysisProvider

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
  ChatStatistics today(@QueryParam("chatId") String chatId, @Context SecurityContext ctx) {
    try{
      return db.tx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat

        if (!chat) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("CHAT_NOT_FOUND").build())
        }

        ChatDayStatistics chatDayStatistics = cache.getChatStatistics(chat.externalId, chat.rebirthDate)
        Map<String, UserDayStatistics> flooders = cache.getFlooders(chat.externalId, chat.rebirthDate)

        if (chatDayStatistics == null || flooders == null)
          return new ChatStatistics()

        Map<String, Long> keywords = analysisProvider.getChatKeywordsExtended(chat.externalId, chat.rebirthDate)

        return convert(chatDayStatistics, flooders, keywords)
      }
    } catch (Exception ignored) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).type(MediaType.TEXT_PLAIN_TYPE).entity("UNKNOWN").build())
    }
  }

  private static ChatStatistics convert(ChatDayStatistics chatDayStatistics, Map<String, UserDayStatistics> flooders, Map<String, Long> keywords) {
    ChatStatistics result = new ChatStatistics()

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
    List<KeywordStatistics> keywordStatistics = keywords.entrySet().stream()
      .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
      .limit(10)
      .map{Map.Entry<String, Long> it -> new KeywordStatistics(word: it.key, count: it.value)}
      .collect(Collectors.toList())

    result.keywords = keywordStatistics

    return result
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/yesterday")
  ChatStatistics yesterday(@QueryParam("chatId") String chatId, @Context SecurityContext ctx) {
    try{
      return db.tx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat

        if (!chat) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("CHAT_NOT_FOUND").build())
        }

        LocalDate yesterday = chat.rebirthDate.minusDays(1)

        ChatDayStatistics chatDayStatistics = cache.getChatStatistics(chat.externalId, yesterday)
        Map<String, UserDayStatistics> flooders = cache.getFlooders(chat.externalId, yesterday)

        if (chatDayStatistics == null)
          chatDayStatistics = ChatStatisticsQuery.byChat(chat, yesterday, session).uniqueResult() as ChatDayStatistics

        if (flooders == null) {
          List<UserDayStatistics> floodersFromDb = UserStatisticsQuery.byChat(chat, yesterday, session).list()
          if (floodersFromDb != null && !floodersFromDb.isEmpty()) {
            flooders = new HashMap<String, UserDayStatistics>(floodersFromDb.size())
            for (UserDayStatistics loaded : floodersFromDb) {
              flooders.put(loaded.user.externalId, loaded)
            }
          }
        }

        if (chatDayStatistics == null || flooders == null)
          return new ChatStatistics()

        Map<String, Long> keywords = analysisProvider.getChatKeywordsExtended(chat.externalId, yesterday)

        return convert(chatDayStatistics, flooders, keywords)
      }
    } catch (Exception ignored) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).type(MediaType.TEXT_PLAIN_TYPE).entity("UNKNOWN").build())
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/week")
  ChatStatistics week(@QueryParam("chatId") String chatId, @Context SecurityContext ctx) {
    try{
      return db.tx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat

        if (!chat) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("CHAT_NOT_FOUND").build())
        }

        LocalDate monday = chat.rebirthDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        LocalDate sunday = chat.rebirthDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        ChatDayStatistics chatDayStatistics = getChatStatisticsByPeriod(chat, monday, sunday, session)
        Map<String, UserDayStatistics> flooders = getUserStatisticsByPeriod(chat, monday, sunday, session)

        if (chatDayStatistics == null || flooders == null || flooders.isEmpty())
          return new ChatStatistics()

        Map<String, Long> keywords = analysisProvider.getChatKeywordsExtended(chat.externalId, monday, sunday)

        return convert(chatDayStatistics, flooders, keywords)
      }
    } catch (Exception ignored) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).type(MediaType.TEXT_PLAIN_TYPE).entity("UNKNOWN").build())
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/month")
  ChatStatistics month(@QueryParam("chatId") String chatId, @Context SecurityContext ctx) {
    try{
      return db.tx { Session session ->
        Chat chat = ChatQuery.byTelegramExternalId(chatId, session).uniqueResult() as Chat

        if (!chat) {
          throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("CHAT_NOT_FOUND").build())
        }

        LocalDate startOfMonth = chat.rebirthDate.withDayOfMonth(1)
        LocalDate endOfMonth = chat.rebirthDate.withDayOfMonth(chat.rebirthDate.lengthOfMonth())

        ChatDayStatistics chatDayStatistics = getChatStatisticsByPeriod(chat, startOfMonth, endOfMonth, session)
        Map<String, UserDayStatistics> flooders = getUserStatisticsByPeriod(chat, startOfMonth, endOfMonth, session)

        if (chatDayStatistics == null || flooders == null || flooders.isEmpty())
          return new ChatStatistics()

        Map<String, Long> keywords = analysisProvider.getChatKeywordsExtended(chat.externalId, startOfMonth, endOfMonth)

        return convert(chatDayStatistics, flooders, keywords)
      }
    } catch (Exception ignored) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).type(MediaType.TEXT_PLAIN_TYPE).entity("UNKNOWN").build())
    }
  }

  private ChatDayStatistics getChatStatisticsByPeriod(Chat chat, LocalDate from, LocalDate till, Session session) {
    List<ChatDayStatistics> chatDayStatisticsList = ChatStatisticsQuery.byChat(chat, from, till, session).list()
    Set<LocalDate> loadedDates = chatDayStatisticsList.stream().map{it.createDate}.collect(Collectors.toSet())

    for (LocalDate dateIterator = from; !dateIterator.isAfter(till); dateIterator = dateIterator.plusDays(1)) {
      if (!loadedDates.contains(dateIterator)) {
        ChatDayStatistics chatDayStatistics = cache.getChatStatistics(chat.externalId, dateIterator)
        if (chatDayStatistics != null)
          chatDayStatisticsList.add(chatDayStatistics)
      }
    }

    if (chatDayStatisticsList == null || chatDayStatisticsList.isEmpty())
      return null

    ChatDayStatistics result = new ChatDayStatistics(chat: chat, floodSize: 0, messageCounter: 0, wordCounter: 0)
    for (ChatDayStatistics stat : chatDayStatisticsList) {
      result.floodSize += stat.floodSize
      result.messageCounter += stat.messageCounter
      result.wordCounter += stat.wordCounter
    }
    return result
  }

  private Map<String, UserDayStatistics> getUserStatisticsByPeriod(Chat chat, LocalDate from, LocalDate till, Session session) {
    List<UserDayStatistics> flooders = UserStatisticsQuery.byChat(chat, from, till, session).list()
    Set<LocalDate> loadedDates = flooders.stream().map{it.createDate}.collect(Collectors.toSet())

    for (LocalDate dateIterator = from; !dateIterator.isAfter(till); dateIterator = dateIterator.plusDays(1)) {
      if (!loadedDates.contains(dateIterator)) {
        Map<String, UserDayStatistics> fromCache = cache.getFlooders(chat.externalId, dateIterator)
        if (fromCache != null && !fromCache.isEmpty()) {
          flooders.addAll(fromCache.values())
        }
      }
    }

    Map<String, UserDayStatistics> result = new HashMap<>()

    if (flooders != null && !flooders.isEmpty()) {
      for (UserDayStatistics loaded : flooders) {
        UserDayStatistics stat = result.get(loaded.user.externalId)

        if (stat == null)  {
          stat = new UserDayStatistics(user: loaded.user, chat: loaded.chat, floodSize: 0, messageCounter: 0, wordCounter: 0)
          result.put(loaded.user.externalId, stat)
        }

        stat.floodSize += loaded.floodSize
        stat.messageCounter += loaded.messageCounter
        stat.wordCounter += loaded.wordCounter
      }
    }
    return result
  }

}
