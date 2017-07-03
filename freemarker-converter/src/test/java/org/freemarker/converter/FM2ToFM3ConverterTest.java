/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.freemarker.converter;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.freemarker.converter.ConverterException;
import org.apache.freemarker.converter.FM2ToFM3Converter;
import org.freemarker.converter.test.ConverterTest;
import org.junit.Test;

import freemarker.template.Configuration;

public class FM2ToFM3ConverterTest extends ConverterTest {

    protected void createSourceFiles() throws IOException {
        //
    }

    @Test
    public void testMixed() throws IOException, ConverterException {
        assertConvertedSame("s1\n  <#if t>\n    ${var}\n  </#if>\ns2");
    }

    @Test
    public void testLiterals() throws IOException, ConverterException {
        assertConvertedSame("${''}");
        assertConvertedSame("${'s'}");
        assertConvertedSame("${\"\"}");
        assertConvertedSame("${\"s\"}");
        assertConvertedSame("${\"\\\"'\"}");
        assertConvertedSame("${'\"\\''}");
        assertConvertedSame("${'1${x + 1 + \\'s\\'}2'}");
        assertConvertedSame("${\"s ${'x $\\{\\\"y\\\"}'}\"}");
        assertConvertedSame("${'${1}${2}'}");

        assertConvertedSame("${r'${1}'}");

        assertConvertedSame("${1}");
        assertConvertedSame("${0.5}");
        assertConvertedSame("${-1.5}");

        assertConvertedSame("${true}");
        assertConvertedSame("${false}");

        assertConvertedSame("${f([])}");
        assertConvertedSame("${f([ <#-- C --> ])}");
        assertConvertedSame("${f([1])}");
        assertConvertedSame("${f([1, [x,y], 3])}");
        assertConvertedSame("${f([<#--1--> 1, <#--2--> 2, <#--3--> 3 <#--4-->])}");

        assertConvertedSame("${f({})}");
        assertConvertedSame("${f({k: v})}");
        assertConvertedSame("${f({k1: v1, k2: v2, 'k3': 33})}");
        assertConvertedSame("${f({ <#--1--> k1 <#--2--> : <#--3--> v1 <#--4-->,k2:v2 <#--5-->})}");

        assertConvertedSame("${f(1 .. 9)}");
        assertConvertedSame("${f(1 ..* 9)}");
        assertConvertedSame("${f(1 ..! 9)}");
        assertConvertedSame("${f(1 ..< 9)}");
        assertConvertedSame("${f(1 ..)}");
        assertConvertedSame("${f(1<#--1-->..\t<#--2-->9)}");
    }

    @Test
    public void testOtherExpressions() throws IOException, ConverterException {
        assertConvertedSame("${x + 1\r\n\t- y % 2 / 2 * +z / -1}");
        assertConvertedSame("${x * (y + z) * (\ty+z\n)}");

        assertConvertedSame("${f()}");
        assertConvertedSame("${f(1)}");
        assertConvertedSame("${f(1, 2)}");
        assertConvertedSame("${f<#--1-->(<#--2--> 1, 2 ,<#--3--> 3,<#--4-->4 <#--5-->)}");

        assertConvertedSame("${m[key]}");
        assertConvertedSame("${m['key']}");
        assertConvertedSame("${m <#--1--> [ <#--2--> key <#--3--> ]}");
        assertConvertedSame("${seq\\-foo[1]}");

        assertConvertedSame("${m.key}");
        assertConvertedSame("${m <#--1--> . <#--3--> key}");

        assertConvertedSame("${.outputFormat}");
        assertConvertedSame("${. <#-- C --> outputFormat}");
        assertConverted("${.outputFormat}","${.output_format}");

        assertConvertedSame("${a < b}${a <= b}${(a > b)}${(a >= b)}${a == b}${a != b}");
        assertConvertedSame("${a<#--1--><<#--2-->b}${a<#--3--><=<#--4-->b}"
                + "${(a<#--7-->><#--8-->b)}${(a<#--9-->>=<#--A-->b)}"
                + "${a<#--B-->==<#--C-->b}${a<#--D-->!=<#--E-->b}");
        // "Same" for now, will be different later.
        assertConvertedSame("${a = b}${a == b}");
        assertConvertedSame("${a &lt; b}${a lt b}${a \\lt b}");
        assertConvertedSame("${a &lt;= b}${a lte b}${a \\lte b}");
        assertConvertedSame("${a &gt; b}${a gt b}${a \\gt b}");
        assertConvertedSame("${a &gt;= b}${a gte b}${a \\gte b}");

        // [FM3] Add \and and &amp;&amp; tests when 2.3.27 is released
        assertConvertedSame("${a && b}${a & b}${a || b}${a | b}");
        assertConvertedSame("${a<#--1-->&&<#--2-->b}${a<#--3-->&<#--4-->b}"
                + "${a<#--5-->||<#--6-->b}${a<#--7-->|<#--8-->b}");

        assertConvertedSame("${!a}${! foo}${! <#--1--> bar}${!!c}");

        assertConvertedSame("${a!} ${a!0}");
        assertConvertedSame("${a <#--1--> !} ${a <#--2--> ! <#--3--> 0}");
        assertConvertedSame("${a!b.c(x!0, y!0)}");
        assertConvertedSame("${(a.b)!x}");
        // [FM3] Will be: a!(x+1)
        assertConvertedSame("${a!x+1}");

        assertConvertedSame("${a??} ${a <#--1--> ??}");
    }

    @Test
    public void testInterpolations() throws IOException, ConverterException {
        assertConvertedSame("${n}");
        assertConvertedSame("${  n\n}");

        assertConverted("${n}", "#{n}");
        assertConverted("${n?string('0.00')}", "#{n; m2}");
        assertConverted("${n?string('0.###')}", "#{n; M3}");
        assertConverted("${n?string('0.00###')}", "#{n; m2M5}");
        assertConverted("${n + 1}", "#{n + 1}");
        assertConverted("${(n + 1)?string('0.00')}", "#{n + 1; m2}");
        assertConverted("${(n * m)?string('0.00')}", "#{n * m; m2}");
        assertConverted("${(-n)?string('0.00')}", "#{-n; m2}");
        assertConverted("${a.b?string('0.00')}", "#{a.b; m2}");
        assertConverted("${f()?string('0.00')}", "#{f(); m2}");
        assertConverted("${m[k]?string('0.00')}", "#{m[k]; m2}");
        assertConverted("${n?abs?string('0.00')}", "#{n?abs; m2}");

        assertConverted("${  n  }", "#{  n  }");
        assertConverted("${  n?string('0.00')}", "#{  n ; m2}");
    }

    @Test
    public void testCoreDirectives() throws IOException, ConverterException {
        assertConvertedSame("<#if foo>1</#if>");
        assertConvertedSame("<#if\n  foo\n>\n123\n</#if\n>");

        assertConverted("<#if foo>1<#elseIf bar>2<#else>3</#if>", "<#if foo>1<#elseif bar>2<#else>3</#if>");
        assertConvertedSame("<#if  foo >1<#elseIf  bar >2<#else >3</#if >");

        assertConvertedSame("<#macro m>body</#macro>");
        assertConvertedSame("<#macro <#--1--> m <#--2-->></#macro >");
        assertConvertedSame("<#macro m()></#macro>");
        assertConvertedSame("<#macro m <#--1--> ( <#--2--> ) <#--3--> ></#macro>");
        assertConvertedSame("<#macro m p1></#macro>");
        assertConvertedSame("<#macro m(p1)></#macro>");
        assertConvertedSame("<#macro m p1 p2 p3></#macro>");
        assertConvertedSame("<#macro m p1 <#--1--> p2 <#--2--> p3 <#--3-->></#macro>");
        assertConvertedSame("<#macro m(p1<#--1-->,<#--2--> p2<#--3-->,<#--4-->"
                + " p5<#--5-->)<#--6-->></#macro>");
        assertConvertedSame("<#macro m p1=11 p2=foo p3=a+b></#macro>");
        assertConvertedSame("<#macro m(p1=11, p2=foo, p3=a+b)></#macro>");
        assertConvertedSame("<#macro m p1<#--1-->=<#--2-->11<#--3-->,<#--4-->p2=22></#macro>");
        assertConvertedSame("<#macro m others...></#macro>");
        assertConvertedSame("<#macro m p1 others...></#macro>");
        assertConvertedSame("<#macro m p1 p2=22 others...></#macro>");
        assertConvertedSame("<#macro m(others...)></#macro>");
        assertConvertedSame("<#macro m(others <#--1--> ... <#--2--> )></#macro>");
        assertConvertedSame("<#function m x y>foo</#function>");
        assertConvertedSame("<#macro m\\-1 p\\-1></#macro>");

        assertConvertedSame("<#assign x = 1>");
        assertConvertedSame("<#global x = 1>");
        assertConvertedSame("<#macro m><#local x = 1></#macro>");
        assertConvertedSame("<#assign x = 1 in someNs>");
        assertConvertedSame("<#assign <#--1--> x <#--2--> = <#--3--> 1 <#--4--> in <#--5--> someNs <#--6-->>");
        assertConvertedSame("<#assign x=1 y=2,z=3>");
        assertConvertedSame("<#assign x += 1>");
        assertConvertedSame("<#assign x *= 2>");
        assertConvertedSame("<#assign x-->");
        assertConvertedSame("<#global x = 1, y++, z /= 2>");
        assertConvertedSame("<#assign x = 1 y++ z /= 2>");
        assertConvertedSame("<#assign <#--0-->x = 1<#--1-->,<#--2-->y++<#--3-->z/=2<#--4-->>");

        assertConvertedSame("<#attempt>a<#recover>r</#attempt>");
        assertConvertedSame("<#attempt >a<#recover  >r</#attempt   >");
        assertConverted("<#attempt>a<#recover>r</#attempt>", "<#attempt>a<#recover>r</#recover>");
        assertConverted("<#attempt >a<#recover  >r</#attempt   >", "<#attempt >a<#recover  >r</#recover   >");

        assertConvertedSame("<#ftl>");
        assertConvertedSame("<#ftl>x");
        assertConvertedSame("<#ftl>x${x}");
        assertConvertedSame("<#ftl>\nx${x}");
        assertConvertedSame("\n\n  <#ftl>\n\nx");
        assertConverted("<#ftl outputFormat='HTML'>x", "<#ftl output_format='HTML'>x");
    }

    @Test
    public void testUserDirectives() throws IOException, ConverterException {
        assertConvertedSame("<@foo/>");
        assertConvertedSame("<@foo />");
        assertConvertedSame("<@foo\\-bar />");
        assertConvertedSame("<@foo></@foo>");
        assertConvertedSame("<@foo\\-bar >t</@foo\\-bar>");
        assertConvertedSame("<@foo\\-bar >t</@>");
        assertConvertedSame("<@foo x=1 y=2 />");
        assertConvertedSame("<@foo x\\-y=1 />");
        assertConvertedSame("<@foo\n\tx = 1\n\ty = 2\n/>");
        assertConvertedSame("<@foo 1 2 />");
        assertConvertedSame("<@foo <#--1--> 1 <#--2--> 2 <#--3--> />");
        assertConvertedSame("<@foo 1, 2 />");
        assertConvertedSame("<@foo <#--1--> 1 <#--2-->, <#--3--> 2 <#--4--> />");
        assertConvertedSame("<@foo x=1; i, j></@>");
        assertConvertedSame("<@foo 1; i, j></@>");
        assertConvertedSame("<@foo 1 2; i\\-2, j></@>");
        assertConvertedSame("<@foo x=1 y=2; i></@>");
        assertConvertedSame("<@foo x=1 ;\n    i <#-- C0 --> , <#--1-->\n\t<!-- C2 --> j <#--3-->\n></@>");
    }

    @Test
    public void testBuiltInExpressions() throws IOException, ConverterException {
        assertConverted("${s?upperCase} ${s?leftPad(123)}", "${s?upper_case} ${s?left_pad(123)}");
        assertConverted("${s?html}", "${s?web_safe}");
        assertConvertedSame("${s  ?   upperCase\t?\t\tleftPad(5)}");
    }

    @Test
    public void testComments() throws IOException, ConverterException {
        assertConvertedSame("\n<#--\n  c\n\t-->\n");
        assertConvertedSame("${1 + <#--1-->\r\n2 +[#-- C2 --]3 +<!--\tC3\t-->4 +[!-- C4 --] 5 + -<!-- -->1}");
    }

    @Test
    public void testSquareBracketTagSyntax() throws IOException, ConverterException {
        assertConvertedSame("[#if true <#-- c -->[#-- c --]]${v}[/#if]", true);
    }

    @Test
    public void testFileExtensions() throws IOException, ConverterException {
        FileUtils.write(new File(srcDir, "t1"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t2.foo"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t3.ftl"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t4.ftlh"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t5.ftlx"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t6.ftl"), "[#ftl]", UTF_8);
        FileUtils.write(new File(srcDir, "t7.ftlh"), "[#ftl]", UTF_8);
        FileUtils.write(new File(srcDir, "t8.ftlx"), "[#ftl]", UTF_8);
        FileUtils.write(new File(srcDir, "t9.Ftl"), "x", UTF_8);
        FileUtils.write(new File(srcDir, "t10.Foo3"), "x", UTF_8);

        FM2ToFM3Converter converter = new FM2ToFM3Converter();
        converter.setSource(srcDir);
        converter.setDestinationDirectory(dstDir);
        Properties properties = new Properties();
        properties.setProperty(Configuration.DEFAULT_ENCODING_KEY, UTF_8.name());
        converter.setFreeMarker2Settings(properties);

        converter.execute();

        assertTrue(new File(dstDir, "t1").exists());
        assertTrue(new File(dstDir, "t2.foo").exists());
        assertTrue(new File(dstDir, "t3.fm3").exists());
        assertTrue(new File(dstDir, "t4.fm3h").exists());
        assertTrue(new File(dstDir, "t5.fm3x").exists());
        assertTrue(new File(dstDir, "t6.fm3s").exists());
        assertTrue(new File(dstDir, "t7.fm3sh").exists());
        assertTrue(new File(dstDir, "t8.fm3sx").exists());
        assertTrue(new File(dstDir, "t9.fm3").exists());
        assertTrue(new File(dstDir, "t10.Foo3").exists());
    }

    private void assertConvertedSame(String ftl2) throws IOException, ConverterException {
        assertConverted(ftl2, ftl2);
    }

    private void assertConverted(String ftl3, String ftl2) throws IOException, ConverterException {
        assertEquals(ftl3, convert(ftl2));
    }

    private void assertConvertedSame(String ftl2, boolean squareBracketTagSyntax)
            throws IOException, ConverterException {
        assertConverted(ftl2, ftl2, squareBracketTagSyntax);
    }

    private void assertConverted(String ftl3, String ftl2, boolean squareBracketTagSyntax)
            throws IOException, ConverterException {
        assertEquals(ftl3, convert(ftl2, squareBracketTagSyntax));
    }

    private String convert(String ftl2) throws IOException, ConverterException {
        return convert(ftl2, false);
    }

    private String convert(String ftl2, boolean squareBracketTagSyntax) throws IOException, ConverterException {
        File srcFile = new File(srcDir, "t");
        FileUtils.write(srcFile, ftl2, UTF_8);

        FM2ToFM3Converter converter = new FM2ToFM3Converter();
        converter.setSource(srcFile);
        converter.setDestinationDirectory(dstDir);
        Properties properties = new Properties();
        properties.setProperty(Configuration.DEFAULT_ENCODING_KEY, UTF_8.name());
        if (squareBracketTagSyntax) {
            properties.setProperty(Configuration.TAG_SYNTAX_KEY, "squareBracket");
        }
        converter.setFreeMarker2Settings(properties);

        converter.execute();

        File outputFile = new File(dstDir, "t");
        String output = FileUtils.readFileToString(outputFile, UTF_8);
        if (!outputFile.delete()) {
            throw new IOException("Couldn't delete file: " + outputFile);
        }

        return output;
    }

}