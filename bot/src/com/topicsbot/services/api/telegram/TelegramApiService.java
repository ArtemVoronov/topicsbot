package com.topicsbot.services.api.telegram;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topicsbot.model.ChannelType;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.api.telegram.daemons.*;
import com.topicsbot.services.api.telegram.handlers.KeyboardFactory;
import com.topicsbot.services.api.telegram.handlers.UpdateHandler;
import com.topicsbot.services.api.telegram.handlers.UpdateProcessor;
import com.topicsbot.services.api.telegram.handlers.UpdateType;
import com.topicsbot.services.api.telegram.handlers.admin.CountHandler;
import com.topicsbot.services.api.telegram.handlers.admin.GetJVMInfoHandler;
import com.topicsbot.services.api.telegram.handlers.user.*;
import com.topicsbot.services.api.telegram.model.*;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.db.dao.ChatDAO;
import com.topicsbot.services.db.dao.TopicDAO;
import com.topicsbot.services.db.dao.UserDAO;
import com.topicsbot.services.i18n.ResourceBundleService;
import com.topicsbot.services.messages.MessagesFactory;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Artem Voronov
 */
public class TelegramApiService implements TelegramApiProvider {
  private static final Logger logger = Logger.getLogger("TELEGRAM_API_SERVICE");
  private static final ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final TelegramApiClient client;
  private final String getUpdatesUrl;
  private final String sendMessageUrl;
  private final String getChatMembersCountUrl;
  private final String getChatUrl;
  private final String getChatMemberUrl;
  private final String answerInlineQueryUrl;

  private final Queue<Runnable> sendMessageRequestsQueue = new ConcurrentLinkedQueue<>();


  private final UpdateProcessor updateProcessor;
  private final ChatDAO chatDAO;

  public TelegramApiService(DBService dbService, ScheduledExecutorService scheduledExecutorService,
                            ResourceBundleService resourceBundleService, AnalysisProvider analysisProvider,
                            CacheService cacheService,
                            int connectTimeout, int requestTimeout,
                            String botToken, String botUserName) {
    this.client = new TelegramApiClient(connectTimeout, requestTimeout);

    final String apiTelegramUrl = "https://api.telegram.org/bot"+botToken;
    this.getUpdatesUrl = apiTelegramUrl + "/getUpdates";
    this.sendMessageUrl = apiTelegramUrl + "/sendMessage";
    this.getChatMembersCountUrl = apiTelegramUrl + "/getChatMembersCount";
    this.getChatUrl = apiTelegramUrl + "/getChat";
    this.getChatMemberUrl = apiTelegramUrl + "/getChatMember";
    this.answerInlineQueryUrl = apiTelegramUrl + "/answerInlineQuery";

    scheduledExecutorService.scheduleAtFixedRate(new SendMessageDaemon(sendMessageRequestsQueue, scheduledExecutorService), 10000L, 34L, TimeUnit.MILLISECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new RebirthChatDaemon(dbService), calculateRebirthChatDaemonInitDelay(), 3600L, TimeUnit.SECONDS);
    scheduledExecutorService.scheduleWithFixedDelay(new UpdateChatInfoDaemon(dbService, this), 180L, 3600L*24, TimeUnit.SECONDS);
    scheduledExecutorService.schedule(new UpdateUserInfoDaemon(dbService, this), 360L, TimeUnit.SECONDS);

    this.chatDAO = new ChatDAO(dbService);
    final TopicDAO topicDAO = new TopicDAO(dbService);
    final UserDAO userDAO = new UserDAO(dbService);
    final KeyboardFactory keyboardFactory = new KeyboardFactory(resourceBundleService);
    final MessagesFactory messagesFactory = new MessagesFactory(resourceBundleService, analysisProvider, cacheService);
    Map<UpdateType, UpdateHandler> handlers = new HashMap<>(UpdateType.values().length);
    handlers.put(UpdateType.START, new StartCommandUserHandler(this, resourceBundleService, cacheService, userDAO, chatDAO));
    handlers.put(UpdateType.HELP, new HelpCommandUserHandler(this, resourceBundleService, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.DONATE, new DonateCommandUserHandler(this, resourceBundleService, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.RATE, new RateCommandUserHandler(this, resourceBundleService, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.RANK, new RankCommandUserHandler(this, resourceBundleService, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.CANCEL, new CancelUserHandler(chatDAO, userDAO, this, resourceBundleService, cacheService));
    handlers.put(UpdateType.TO_STATISTICS, new ToStatisticsUserHandler(analysisProvider, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.TOPICS, new GetTopicsUserHandler(analysisProvider, this, chatDAO, topicDAO, resourceBundleService, cacheService, userDAO));
    handlers.put(UpdateType.ADD, new AddTopicUserHandler(botUserName, chatDAO, topicDAO, userDAO, this, resourceBundleService, cacheService));
    handlers.put(UpdateType.WORLD_TOPICS, new GetWorldTopicsUserHandler(this, messagesFactory, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.INLINE_QUERY, new AnswerInlineQueryHandler(this, messagesFactory));
    handlers.put(UpdateType.STATISTICS, new GetStatisticsUserHandler(this, messagesFactory, cacheService, chatDAO, userDAO));
    handlers.put(UpdateType.SETTINGS, new ShowSettingsKeyboardUserHandler(this, messagesFactory, chatDAO, keyboardFactory, cacheService, userDAO));
    handlers.put(UpdateType.CLOSE_SETTINGS, new HideSettingsKeyboardHandler(this, messagesFactory, chatDAO));
    handlers.put(UpdateType.LANGUAGE_KEYBOARD, new ShowLanguagesKeyboardHandler(this, chatDAO, resourceBundleService, keyboardFactory));
    handlers.put(UpdateType.TIMEZONE_KEYBOARD, new ShowTimezonesKeyboardHandler(this, chatDAO, resourceBundleService, keyboardFactory));
    handlers.put(UpdateType.LANGUAGE, new ChangeLanguageHandler(chatDAO, this, resourceBundleService));
    handlers.put(UpdateType.TIMEZONE, new ChangeTimezoneHandler(chatDAO, this, resourceBundleService));

    handlers.put(UpdateType.JVM, new GetJVMInfoHandler(this, cacheService));
    handlers.put(UpdateType.COUNT, new CountHandler(this, cacheService));
    this.updateProcessor = new UpdateProcessor(botUserName, handlers, cacheService);
  }

  private static long calculateRebirthChatDaemonInitDelay() {
    LocalTime now = LocalTime.now();
    int minutes = now.getMinute();
    int seconds = now.getSecond();
    long daemonInitDelaySeconds = (60L - minutes) * 60 + 60L - seconds;

    if (logger.isDebugEnabled())
      logger.debug("rebirth daemon init delay: " + daemonInitDelaySeconds + " seconds (" + (60 - minutes) + " min)");

    return daemonInitDelaySeconds;
  }

  @Override
  public void sendMessage(Chat chat, String text) {
    final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\"}";
    sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
  }

  @Override
  public void replyToMessage(Chat chat, String text, Message replyMessage) {
    final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_to_message_id\":" + replyMessage.getId() + "}";
    sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
  }

  @Override
  public int getChatMembersCount(String chatExternalId) {
    final String jsonParams = "{\"chat_id\":\"" + chatExternalId + "\"}";
    ChatMembersCount result = client.makeRequest(getChatMembersCountUrl, jsonParams, ChatMembersCount.class);
    return result.getCount();
  }

  @Override
  public Chat getChat(String chatExternalId) {
    final String jsonParams = "{\"chat_id\":\"" + chatExternalId + "\"}";
    ChatInfo result = client.makeRequest(getChatUrl, jsonParams, ChatInfo.class);
    return result.getChat();
  }

  @Override
  public User getChatMember(String chatExternalId, String userExternalId) {
    final String jsonParams = "{\"chat_id\":" + chatExternalId + ", \"user_id\":" + userExternalId + "}";
    ChatMemberInfo result = client.makeRequest(getChatMemberUrl, jsonParams, ChatMemberInfo.class);
    ChatMember chatMember = result.getResult();
    if (chatMember == null)
      return null;

    return chatMember.getUser();
  }

  @Override
  public Updates getUpdates(Integer lastUpdateId) {
    String jsonParams = lastUpdateId == null ? "{}" : "{\"offset\":" + lastUpdateId + "}";
    return client.makeRequest(getUpdatesUrl, jsonParams, Updates.class);
  }

  @Override
  public void sendReplyKeyboard(Chat chat, String text, Message replyMessage, ReplyKeyboardMarkup keyboard) {
    try {
      final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_to_message_id\":" + replyMessage.getId() + ",\"reply_markup\":" + MAPPER.writeValueAsString(keyboard) + "}";
      sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void hideKeyboard(Chat chat, String text, Message replyMessage, ReplyKeyboardRemove replyKeyboardRemove) {
    try {
      final String jsonParams = "{\"chat_id\":\"" + chat.getId() + "\",\"text\":\"" + text + "\",\"reply_to_message_id\":" + replyMessage.getId() + ",\"reply_markup\":" + MAPPER.writeValueAsString(replyKeyboardRemove) + "}";
      sendMessageRequestsQueue.add(() -> client.makeRequest(sendMessageUrl, jsonParams, Message.class));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void answerInlineQuery(String inlineQueryId, List<InlineQueryResult> inlineQueryResults) {
    try {
      final String jsonParams = "{\"inline_query_id\":\"" + inlineQueryId + "\",\"results\":" + MAPPER.writeValueAsString(inlineQueryResults) + "}";
      sendMessageRequestsQueue.add(() -> client.makeRequest(answerInlineQueryUrl, jsonParams, Message.class));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void processUpdate(Update next) {
    try {
      if (next == null)
        return;

      if (isDeprecated(next))
        return;

      checkOrRegisterChat(next);
      updateProcessor.process(next);

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  private boolean isDeprecated(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return false;

    long message_time = message.getDate() * 1000;
    long system_time = System.currentTimeMillis();
    long diff = system_time - message_time;
    return diff > 120_000L; //if older than 2 min -> ignore message
  }

  private void checkOrRegisterChat(Update update) {
    Message message = update.getMessage();

    if (message == null)
      return;

    String externalId = message.getChatId();
    synchronized (chatDAO) {
      com.topicsbot.model.chat.Chat modelChat = chatDAO.find(externalId);

      if (modelChat == null) {
        Chat apiChat = message.getChat();
        com.topicsbot.model.chat.ChatType type = Converter.convert(apiChat.getType());
        int size = type == com.topicsbot.model.chat.ChatType.PRIVATE ? 1 : getChatMembersCount(externalId);
        ZoneId UTC = TimeZone.getTimeZone("Etc/GMT0").toZoneId();
        chatDAO.create(externalId, apiChat.getTitle(), ChannelType.TELEGRAM, type, ChatLanguage.EN, size, UTC, LocalDate.now(UTC));
      }
    }
  }
}
