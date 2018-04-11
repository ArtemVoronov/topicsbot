function updateCounters() {
  return $.get("http://www.topicsbot.com/rest/counters/today", function(counters) {
    var source = $("#stat_script").html();
    var $destination = $("#stat_output");
    var template = Handlebars.compile(source);
    $destination.html(template(counters));
    $destination.show();
  });
}
(function() {
  "use strict";
  $('#open_telegram_btn').on('click', function (e) {
    ga('send', 'event', 'open_in_telegram','open Topics Bot in Telegram')
  });

  var res1 = updateCounters();
  $.when(res1).done(function () {
    setInterval(updateCounters, 30000);
  });

})();