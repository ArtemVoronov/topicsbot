$(function($) {
  "use strict";

  //For fullPage
  $("#fullpage").fullpage({
    navigation: true,
    navigationTooltips: ["Home", "Features", "Contact Us", "Help to the project"],
    responsiveWidth: 991,
    onLeave: function(index, nextIndex, direction){
      if (nextIndex == 1) {
        ga('send', 'pageview', '/home');
      } else if (nextIndex == 2) {
        ga('send', 'pageview', '/features');
      } else if (nextIndex == 3) {
        ga('send', 'pageview', '/contacts');
      } else if (nextIndex == 4) {
        ga('send', 'pageview', '/donations');
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
