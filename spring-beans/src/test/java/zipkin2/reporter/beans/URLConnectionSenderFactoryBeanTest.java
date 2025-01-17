/*
 * Copyright 2016-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin2.reporter.beans;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import zipkin2.codec.Encoding;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import static org.assertj.core.api.Assertions.assertThat;

public class URLConnectionSenderFactoryBeanTest {
  XmlBeans context;

  @After public void close() {
    if (context != null) context.close();
  }

  @Test public void endpoint() throws MalformedURLException {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .extracting("endpoint")
        .containsExactly(URI.create("http://localhost:9411/api/v2/spans").toURL());
  }

  @Test public void connectTimeout() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "  <property name=\"connectTimeout\" value=\"0\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .isEqualToComparingFieldByField(URLConnectionSender.newBuilder()
            .endpoint("http://localhost:9411/api/v2/spans")
            .connectTimeout(0)
            .build());
  }

  @Test public void readTimeout() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "  <property name=\"readTimeout\" value=\"0\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .isEqualToComparingFieldByField(URLConnectionSender.newBuilder()
            .endpoint("http://localhost:9411/api/v2/spans")
            .readTimeout(0).build());
  }

  @Test public void compressionEnabled() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "  <property name=\"compressionEnabled\" value=\"false\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .extracting("compressionEnabled")
        .containsExactly(false);
  }

  @Test public void messageMaxBytes() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "  <property name=\"messageMaxBytes\" value=\"1024\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .extracting("messageMaxBytes")
        .containsExactly(1024);
  }

  @Test public void encoding() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "  <property name=\"encoding\" value=\"PROTO3\"/>\n"
        + "</bean>"
    );

    assertThat(context.getBean("sender", URLConnectionSender.class))
        .extracting("encoding")
        .containsExactly(Encoding.PROTO3);
  }

  @Test(expected = IllegalStateException.class) public void close_closesSender() {
    context = new XmlBeans(""
        + "<bean id=\"sender\" class=\"zipkin2.reporter.beans.URLConnectionSenderFactoryBean\">\n"
        + "  <property name=\"endpoint\" value=\"http://localhost:9411/api/v2/spans\"/>\n"
        + "</bean>"
    );

    URLConnectionSender sender = context.getBean("sender", URLConnectionSender.class);
    context.close();

    sender.sendSpans(Arrays.asList(new byte[] {'{', '}'}));
  }
}
