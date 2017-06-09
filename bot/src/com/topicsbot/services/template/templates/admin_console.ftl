<html>
<head>
  <title>Topics Bot: Admin console</title>
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

  <style>
    table.table tr th {text-align: center; width: 20%}
    table.table tr td {text-align: center;}
  </style>
</head>
<body style="text-align: center; padding: 0, 25px, 25px, 25px">
<h1 style="text-align: center; margin-bottom: 25px">Welcome to Topics Bot!</h1>

<div class="row">
  <div class="col-xs-4 col-md-4">
    <div class="thumbnail">
      <div class="caption">Active chats: ${cacheInfo.activeChats}</div>
    </div>
  </div>
  <div class="col-xs-4 col-md-4">
    <div class="thumbnail">
      <div class="caption">Active users: ${cacheInfo.activeUsers}</div>
    </div>
  </div>
  <div class="col-xs-4 col-md-4">
    <div class="thumbnail">
      <div class="caption">Messages: ${cacheInfo.chatCounters['MESSAGES']}</div>
    </div>
  </div>
</div>
<div class="row">
  <div class="col-xs-3 col-md-3">
    <div class="thumbnail">
      <div class="caption">PRIVATE chats: ${cacheInfo.chatTypes['PRIVATE']}</div>
    </div>
  </div>
  <div class="col-xs-3 col-md-3">
    <div class="thumbnail">
      <div class="caption">GROUP chats: ${cacheInfo.chatTypes['GROUP']}</div>
    </div>
  </div>
  <div class="col-xs-3 col-md-3">
    <div class="thumbnail">
      <div class="caption">SUPER_GROUP chats: ${cacheInfo.chatTypes['SUPER_GROUP']}</div>
    </div>
  </div>
  <div class="col-xs-3 col-md-3">
    <div class="thumbnail">
      <div class="caption">CHANNEL chats: ${cacheInfo.chatTypes['CHANNEL']}</div>
    </div>
  </div>
</div>

<table class="table table-striped table-hover table-bordered" style="margin-bottom: 20px">
  <tr>
    <th>COUNTER</th>
    <th>PRIVATE</th>
    <th>GROUP</th>
    <th>SUPER_GROUP</th>
    <th>TOTAL</th>
  </tr>
<#list cacheInfo.chatCountersDetailed?keys as key>
  <#if key != "FLOOD" && key != "WORDS" && key != "MESSAGES" && key != "STATISTICS_COMMAND" && cacheInfo.chatCounters[key] != 0>
    <tr>
      <td>${key}</td>
      <td>${cacheInfo.chatCountersDetailed[key]['PRIVATE']}</td>
      <td>${cacheInfo.chatCountersDetailed[key]['GROUP']}</td>
      <td>${cacheInfo.chatCountersDetailed[key]['SUPER_GROUP']}</td>
      <td>${cacheInfo.chatCounters[key]}</td>
    </tr>
  </#if>
</#list>
</table>

<table class="table table-striped table-hover table-bordered" style="margin-bottom: 20px">
  <tr>
    <th>LANG</th>
    <th>PRIVATE</th>
    <th>GROUP</th>
    <th>SUPER_GROUP</th>
    <th>TOTAL</th>
  </tr>
<#list cacheInfo.chatLanguagesDetailed?keys as key>
  <#if cacheInfo.chatLanguages[key] != 0>
  <tr>
    <td>${key}</td>
    <td>${cacheInfo.chatLanguagesDetailed[key]['PRIVATE']}</td>
    <td>${cacheInfo.chatLanguagesDetailed[key]['GROUP']}</td>
    <td>${cacheInfo.chatLanguagesDetailed[key]['SUPER_GROUP']}</td>
    <td>${cacheInfo.chatLanguages[key]}</td>
  </tr>
  </#if>
</#list>
</table>

<table class="table table-striped table-hover table-bordered" style="margin-bottom: 20px">
  <tr>
    <th>TIMEZONE</th>
    <th>PRIVATE</th>
    <th>GROUP</th>
    <th>SUPER_GROUP</th>
    <th>TOTAL</th>
  </tr>
<#list cacheInfo.chatTimeZonesDetailed?keys as key>
  <#if cacheInfo.chatTimeZones[key] != 0>
  <tr>
    <td>${key}</td>
    <td>${cacheInfo.chatTimeZonesDetailed[key]['PRIVATE']}</td>
    <td>${cacheInfo.chatTimeZonesDetailed[key]['GROUP']}</td>
    <td>${cacheInfo.chatTimeZonesDetailed[key]['SUPER_GROUP']}</td>
    <td>${cacheInfo.chatTimeZones[key]}</td>
  </tr>
  </#if>
</#list>
</table>

</body>
</html>

<#-- @ftlvariable name="cacheInfo" type="com.topicsbot.services.cache.CacheInfo" -->