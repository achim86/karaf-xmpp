package org.apache.karaf.test;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.server.XMPPServer;

import java.io.InputStream;

import static org.apache.vysper.xmpp.addressing.EntityImpl.parseUnchecked;

public final class XMPPTestServer {

    public static void main(String[] args) throws Exception {
        XMPPServer xmppServer = new XMPPServer("camel.xmpp");

        StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        AccountManagement accountManagement = (AccountManagement)providerRegistry.retrieve(AccountManagement.class);

        accountManagement.addUser(parseUnchecked("camel_consumer@camel.xmpp"), "secret");
        accountManagement.addUser(parseUnchecked("camel_producer@camel.xmpp"), "secret");

        xmppServer.setStorageProviderRegistry(providerRegistry);

        TCPEndpoint endpoint = new TCPEndpoint();
        endpoint.setPort(5222);

        xmppServer.addEndpoint(endpoint);

        InputStream stream = XMPPTestServer.class.getClassLoader().getResourceAsStream("etc/keystores/xmppServer.jks");
        xmppServer.setTLSCertificateInfo(stream, "secret");

        xmppServer.start();
    }

}
