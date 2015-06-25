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

<!-- Simplified version that covers text-formatting codes only. -->
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
</configuration>