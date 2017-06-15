stat = {};

Handlebars.registerHelper('eq', function(a, b) {
  "use strict";
  return a === b;
});

/**
 * @constructor
 * @returns {!stat.ChatStatistics}
 */
stat.ChatStatistics = function() {

  function enableDataTables(userStatisticsTableId, keywordsTableId, chatStatistics) {
    var disablePagination = chatStatistics.userStatistics.length > 10;
    $('#'+userStatisticsTableId).dataTable({
      "ordering": false,
      "lengthChange": false,
      "searching": false,
      "info": disablePagination,
      "paging": disablePagination
    });
    $('#'+keywordsTableId).dataTable({
      "ordering": false,
      "lengthChange": false,
      "searching": false,
      "paging": false,
      "info": false
    });
  }


  $(document).ready(function() {
    var $overlay = $("#overlay");
    var $canvas = $("#canvas");

    var $todayStatistics = $("#today_statistics");
    var $yesterdayStatistics = $("#yesterday_statistics");
    var chatId = $("#chat_id").val();
    var deployUrl = $("#deploy_url").val();

    var res1 = $.get("http://" + deployUrl + "/rest/chat_statistics/today?chatId=" + chatId, function(result) {
      var source = $todayStatistics.find("script").html();
      var $destination = $todayStatistics.find(".hbs-output").first();
      var template = Handlebars.compile(source);
      $destination.html(template(result));
      if (result.userStatistics)
        enableDataTables ('today_user_statistics_table', 'today_keywords_table', result)

    });

    //yesterday statistics
    var res2 = $.get("http://" + deployUrl + "/rest/chat_statistics/yesterday?chatId=" + chatId, function(result) {
      var source = $yesterdayStatistics.find("script").html();
      var $destination = $yesterdayStatistics.find(".hbs-output").first();
      var template = Handlebars.compile(source);
      $destination.html(template(result));
      if (result.userStatistics)
        enableDataTables ('yesterday_user_statistics_table', 'yesterday_keywords_table', result);
    });

    $.when( res1, res2 ).done(function () {
      $overlay.css("visibility", "hidden");
      $canvas.css("visibility", "hidden");
    })

  });
};
