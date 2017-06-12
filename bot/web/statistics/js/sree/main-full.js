$(function($) {
  "use strict";

  //For fullPage
  $("#fullpage").fullpage({
    navigation: false,
    responsiveWidth: 991
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
