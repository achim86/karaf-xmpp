package org.apache.karaf.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.xmpp.XmppComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
public class XMPPIntegrationTest extends CamelTestSupport {

    private static final String CAMEL_VERSION = "2.15.4";

    private static final MavenArtifactUrlReference KARAF_URL = maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .type("zip")
            .version("4.0.3");
    private static final MavenUrlReference CAMEL_FEATURE = maven()
            .groupId("org.apache.camel.karaf")
            .artifactId("apache-camel")
            .classifier("features")
            .type("xml")
            .version(CAMEL_VERSION);

    @EndpointInject(uri = "mock:endpoint")
    MockEndpoint mockEndpoint;

    @Override
    public RouteBuilder createRouteBuilder() throws Exception {
        context.addComponent("xmpp", new XmppComponent());
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test")
                        .to("log:org.apache.karaf.test?level=INFO")
                        .to("xmpp://localhost:5222/camel_consumer@camel.xmpp?user=camel_producer&password=secret&serviceName=camel.xmpp");

                from("xmpp://localhost:5222/?user=camel_consumer&password=secret&serviceName=camel.xmpp")
                        .to("log:org.apache.karaf.test?level=INFO")
                        .to(mockEndpoint);

            }
        };
    }

    @Configuration
    public Option[] config() {
        return options(
                karafDistributionConfiguration()
                        .frameworkUrl(KARAF_URL)
                        .karafVersion("4.0.3")
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.INFO),
                replaceConfigurationFile("etc/keystores/xmppServer.jks", new File("src/test/resources/etc/keystores/xmppServer.jks")),
                editConfigurationFilePut("etc/system.properties", "javax.net.ssl.trustStore", "${karaf.base}/etc/keystores/xmppServer.jks"),
                features(CAMEL_FEATURE, "camel-test", "camel-xmpp")
        );
    }

    @Test
    public void testXmpp() throws InterruptedException {
        mockEndpoint.setResultWaitTime(1000);
        mockEndpoint.expectedMessageCount(1);
        template.sendBody("direct:test", "Hello World!");
        mockEndpoint.assertIsSatisfied();
    }

}
