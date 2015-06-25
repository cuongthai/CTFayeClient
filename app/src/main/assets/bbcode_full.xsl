<?xml version="1.0" encoding="utf-8"?>
<!--
  ~ Copyright (C) 2014 ChatWing
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!--
    TODO: check whether we need some parts (url scrope, table code, etc.) or not.
    If we don't need them, clean them up.
-->

<configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns="http://kefirsf.org/kefirbb/schema"
               xsi:schemaLocation="http://kefirsf.org/kefirbb/schema http://kefirsf.org/kefirbb/schema/kefirbb-1.0.xsd">
    <!-- XML escape symbols -->
    <scope name="escapeXml">
        <code priority="100">
            <pattern>&amp;</pattern>
            <template>&amp;amp;</template>
        </code>
        <code priority="100">
            <pattern>&apos;</pattern>
            <template>&amp;apos;</template>
        </code>
        <code priority="100">
            <pattern>&lt;</pattern>
            <template>&amp;lt;</template>
        </code>
        <code priority="100">
            <pattern>&gt;</pattern>
            <template>&amp;gt;</template>
        </code>
        <code priority="100">
            <pattern>&quot;</pattern>
            <template>&amp;quot;</template>
        </code>
    </scope>

    <!-- Scope for escaping bb spec chars -->
    <scope name="escapeBb" parent="escapeXml">
        <!--
            Escape bb-code symbols
            double slash to slash
            slash + square bracket to square bracket
         -->
        <code name="slash" priority="10">
            <pattern>\\</pattern>
            <template>\</template>
        </code>
        <code name="left_square_bracket" priority="9">
            <pattern>\[</pattern>
            <template>[</template>
        </code>
        <code name="right_square_bracket" priority="9">
            <pattern>\]</pattern>
            <template>]</template>
        </code>
    </scope>

    <!-- Escape basic HTML char sequences -->
    <scope name="basic" parent="escapeBb">
        <!-- line feed characters -->
        <code name="br1" priority="3">
            <pattern>&#x0A;&#x0D;</pattern>
            <template>&lt;br/&gt;</template>
        </code>
        <code name="br2" priority="2">
            <pattern>&#x0D;&#x0A;</pattern>
            <template>&lt;br/&gt;</template>
        </code>
        <code name="br3" priority="1">
            <pattern>&#x0A;</pattern>
            <template>&lt;br/&gt;</template>
        </code>
        <code name="br4" priority="0">
            <pattern>&#x0D;</pattern>
            <template>&lt;br/&gt;</template>
        </code>

        <!-- Special html symbols -->
        <code name="symbol">
            <pattern ignoreCase="true">[symbol=<var scope="escapeXml"/>/]</pattern>
            <template>&amp;<var/>;</template>
        </code>

        <!-- angle quotes -->
        <code name="aquote">
            <pattern ignoreCase="true">[aquote]<var inherit="true"/>[/aquote]</pattern>
            <template>&amp;laquo;<var/>&amp;raquo;</template>
        </code>
    </scope>

    <!-- Root scope. This scope uses when processor started work and by default, if not set other scope -->
    <scope name="ROOT">
        <!-- Formatting -->
        <coderef name="bold"/>
        <coderef name="u"/>
        <coderef name="s"/>
        <coderef name="i"/>
        <coderef name="color"/>
        <coderef name="bg_color"/>
        <coderef name="size"/>

        <!-- Image -->
        <coderef name="img"/>

        <!-- Video -->
        <coderef name="video"/>

        <!-- links -->
        <coderef name="url1"/>
        <coderef name="url2"/>
        <coderef name="url3"/>
        <coderef name="url4"/>
        <coderef name="url5"/>
        <coderef name="url6"/>
        <coderef name="url7"/>
    </scope>

    <!-- Simple formatting -->
    <code name="bold">
        <pattern ignoreCase="true">[b]<var inherit="true"/>[/b]</pattern>
        <template>&lt;b&gt;<var/>&lt;/b&gt;</template>
    </code>
    <code name="u">
        <pattern ignoreCase="true">[u]<var inherit="true"/>[/u]</pattern>
        <template>&lt;u&gt;<var/>&lt;/u&gt;</template>
    </code>
    <code name="s">
        <pattern ignoreCase="true">[s]<var inherit="true"/>[/s]</pattern>
        <template>&lt;strike&gt;<var/>&lt;/strike&gt;</template>
    </code>
    <code name="i">
        <pattern ignoreCase="true">[i]<var inherit="true"/>[/i]</pattern>
        <template>&lt;i&gt;<var/>&lt;/i&gt;</template>
    </code>

    <!-- Font color -->
    <code name="color">
        <pattern ignoreCase="true">[color=<var name="color" scope="escapeXml"/>]<var name="text" inherit="true"/>[/color]</pattern>
        <template>&lt;font color=&quot;<var name="color"/>&quot;&gt;<var name="text"/>&lt;/font&gt;</template>
    </code>

    <!-- Font background color -->
    <code name="bg_color">
        <pattern ignoreCase="true">[bgcolor=<var name="color" scope="escapeXml"/>]<var name="text" inherit="true"/>[/bgcolor]</pattern>
        <template>&lt;bgcolor=&quot;<var name="color"/>&quot;&gt;<var name="text"/>&lt;/bgcolor&gt;</template>
    </code>

    <!-- Font size -->
    <code name="size">
        <pattern ignoreCase="true">[size=<var name="size" scope="escapeXml"/>]<var name="text" inherit="true"/>[/size]</pattern>
        <template>&lt;span style=&quot;font-size:<var name="size"/>;&quot;&gt;<var name="text"/>&lt;/span&gt;</template>
    </code>

    <!-- Insert image -->
    <code name="img" priority="1">
        <pattern ignoreCase="true">[img]<var name="addr" scope="escapeXml"/>[/img]</pattern>
        <template>&lt;img src=&quot;<var name="addr"/>&quot;/&gt;</template>
    </code>

    <!-- Video -->
    <code name="video" priority="2">
        <pattern ignoreCase="true">[video]<var name="addr" scope="escapeXml"/>[/video]</pattern>
        <template>&lt;video&gt;<var name="addr"/>&lt;/video&gt;</template>
    </code>

    <!-- Links. http, https, malto protocols -->
    <scope name="url" parent="basic">
        <coderef name="bold"/>
        <coderef name="u"/>
        <coderef name="s"/>
        <coderef name="i"/>
        <coderef name="color"/>
        <coderef name="bg_color"/>
        <coderef name="size"/>
        <coderef name="img"/>
        <coderef name="video"/>
    </scope>

    <!-- HTTP -->
    <code name="url1" priority="2">
        <pattern ignoreCase="true">[url=<var name="protocol" regex="(\s*(ht|f)tps?:|\.{1,2})?"/>/<var name="url" scope="escapeXml"/>]<var
                name="text" scope="url"/>[/url]</pattern>
        <template>&lt;a href=&quot;<var name="protocol"/>/<var name="url"/>&quot;&gt;<var name="text"/>&lt;/a&gt;</template>
    </code>
    <code name="url2" priority="2">
        <pattern ignoreCase="true">[url]<var name="protocol" regex="(\s*(ht|f)tps?:|\.{1,2})?"/>/<var name="url" scope="escapeXml"/>[/url]</pattern>
        <template>&lt;a href=&quot;<var name="protocol"/>/<var name="url"/>&quot;&gt;<var name="protocol"/>/<var
                name="url"/>&lt;/a&gt;</template>
    </code>
    <code name="url3" priority="1">
        <pattern ignoreCase="true">[url=<var name="url" scope="escapeXml"/>]<var name="text" scope="url"/>[/url]</pattern>
        <template>&lt;a href=&quot;http://<var name="url"/>&quot;&gt;<var name="text"/>&lt;/a&gt;</template>
    </code>
    <code name="url4" priority="1">
        <pattern ignoreCase="true">[url]<var name="url" scope="escapeXml"/>[/url]</pattern>
        <template>&lt;a href=&quot;http://<var name="url"/>&quot;&gt;<var name="url"/>&lt;/a&gt;</template>
    </code>

    <!-- MAILTO -->
    <code name="url5" priority="2">
        <pattern ignoreCase="true">[url=mailto:<var name="url" scope="escapeXml"/>]<var name="text" scope="url"/>[/url]</pattern>
        <template>&lt;a href=&quot;mailto:<var name="url"/>&quot;&gt;<var name="text"/>&lt;/a&gt;</template>
    </code>
    <code name="url6" priority="2">
        <pattern ignoreCase="true">[url]mailto:<var name="url" scope="escapeXml"/>[/url]</pattern>
        <template>&lt;a href=&quot;mailto:<var name="url"/>&quot;&gt;mailto:<var name="url"/>&lt;/a&gt;</template>
    </code>
    <code name="url7" priority="2">
        <pattern ignoreCase="true">[email]<var name="email" scope="escapeXml"/>[/email]</pattern>
        <template>&lt;a href=&quot;mailto:<var name="email"/>&quot;&gt;<var name="email"/>&lt;/a&gt;</template>
    </code>
</configuration>