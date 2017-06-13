$(function($) {
  "use strict";

  //For fullPage
  $("#fullpage").fullpage({
    navigation: true,
    navigationTooltips: ["Today", "Yesterday", "This week", "This month"],
    responsiveWidth: 991,
    onLeave: function(index, nextIndex, direction){
      if (nextIndex == 1) {
        ga('send', 'pageview', '/today');
      } else if (nextIndex == 2) {
        ga('send', 'pageview', '/yesterday');
      } else if (nextIndex == 3) {
        ga('send', 'pageview', '/week');
      } else if (nextIndex == 4) {
        ga('send', 'pageview', '/month');
      }
    }
  });

  // For Small Screens - We use normal scrooling instead of fullpage.js on small devices
  var width = $(window).width();
  if (width < 991) {
    $(window).on("scroll resize", function() {
      if ($(window).scrollTop() >= 75) {
        $("body").addClass("fixed-header");
      }
      else {
        return $("body").removeClass("fixed-header");
      }
    });
  }

  // For page transitions
  $(".animsition").animsition();

});
