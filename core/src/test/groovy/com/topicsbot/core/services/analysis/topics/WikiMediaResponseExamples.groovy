package com.topicsbot.core.services.analysis.topics

/**
 * Author: Artem Voronov
 */
class WikiMediaResponseExamples {
  static final String THREE_TOPICS_XML = """<?xml version="1.0"?>
  <api batchcomplete="">
      <continue sroffset="3" continue="-||" />
      <query>
          <searchinfo totalhits="715" />
          <search>
              <p ns="0" title="Africanized bee" pageid="161804" size="47202" wordcount="5620" snippet="The Africanized &lt;span class=&quot;searchmatch&quot;&gt;bee&lt;/span&gt;, also known as the Africanised &lt;span class=&quot;searchmatch&quot;&gt;honey&lt;/span&gt; &lt;span class=&quot;searchmatch&quot;&gt;bee&lt;/span&gt;, and known colloquially as &amp;quot;killer &lt;span class=&quot;searchmatch&quot;&gt;bee&lt;/span&gt;&amp;quot;, is a hybrid of the Western &lt;span class=&quot;searchmatch&quot;&gt;honey&lt;/span&gt; &lt;span class=&quot;searchmatch&quot;&gt;bee&lt;/span&gt; species (Apis" timestamp="2018-05-01T17:48:16Z" />
              <p ns="0" title="Honeypot ant" pageid="321940" size="9228" wordcount="918" snippet="Honeypot &lt;span class=&quot;searchmatch&quot;&gt;ants&lt;/span&gt;, also called &lt;span class=&quot;searchmatch&quot;&gt;honey&lt;/span&gt; &lt;span class=&quot;searchmatch&quot;&gt;ants&lt;/span&gt;, are &lt;span class=&quot;searchmatch&quot;&gt;ants&lt;/span&gt; which have specialized workers (repletes, plerergates, or rotunds) that are gorged with food by workers" timestamp="2018-04-24T18:04:49Z" />
              <p ns="0" title="Ant-Bee" pageid="12557141" size="3378" wordcount="212" snippet="&lt;span class=&quot;searchmatch&quot;&gt;Ant&lt;/span&gt;-&lt;span class=&quot;searchmatch&quot;&gt;Bee&lt;/span&gt; (stage-name for Billy James) is an American experimental musician and writer. In his musical work, he is a psychedelic era revivalist, working" timestamp="2018-02-01T00:01:36Z" />
          </search>
      </query>
  </api>"""

  static final String TWO_TOPICS_XML = """<?xml version="1.0"?>
  <api batchcomplete="">
      <continue sroffset="2" continue="-||" />
      <query>
          <searchinfo totalhits="90" />
          <search>
              <p ns="0" title="David W. Carter High School" pageid="11701842" size="12455" wordcount="1019" snippet="David W. Carter Cowboys compete in the following sports: &lt;span class=&quot;searchmatch&quot;&gt;Baseball&lt;/span&gt; Basketball Cross Country &lt;span class=&quot;searchmatch&quot;&gt;Football&lt;/span&gt; Golf Soccer Softball Swimming and Diving Tennis Track" timestamp="2018-04-17T21:36:10Z" />
              <p ns="0" title="List of The Eric Andre Show episodes" pageid="40718091" size="38726" wordcount="1148" snippet="(2016-09-12). &amp;quot;&lt;span class=&quot;searchmatch&quot;&gt;Friday&lt;/span&gt; cable ratings: College &lt;span class=&quot;searchmatch&quot;&gt;football&lt;/span&gt; wins the night&amp;quot;. Tvbythenumbers. Retrieved 2016-09-13.   Alex Welch (2016-09-19). &amp;quot;&lt;span class=&quot;searchmatch&quot;&gt;Friday&lt;/span&gt; cable ratings:" timestamp="2018-03-30T17:21:50Z" />
          </search>
      </query>
  </api>"""
}
