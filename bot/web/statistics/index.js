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

  $(document).ready(function() {
    var $todayStatistics = $("#today_statistics");
    var chatId = $("#chat_id").val();

    $.get("http://localhost:8080/rest/chat_statistics/today?chatId=" + chatId, function(result) {
      $todayStatistics.fadeIn(500);

      var source = $todayStatistics.find("script").html();
      var $destination = $todayStatistics.find(".hbs-output").first();
      var template = Handlebars.compile(source);
      $destination.html(template(result));
      var disablePagination = result.userStatistics.length > 10;
      $('#today_user_table_statistics').dataTable({
        "ordering": false,
        "lengthChange": false,
        "searching": false,
        "paging": disablePagination
      });

    });

  });
};
