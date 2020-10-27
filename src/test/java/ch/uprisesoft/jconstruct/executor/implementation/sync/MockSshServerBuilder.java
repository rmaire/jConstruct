/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import java.io.IOException;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;

class MockSshServerBuilder {

    public static SshServer buildShellServer() throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setPort(3333);
        server.setHost("127.0.0.1");

        server.setKeyboardInteractiveAuthenticator(null);
        server.setGSSAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        server.setPublickeyAuthenticator(null);

        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String u, String p, ServerSession s) {
                if(u.equals("admin") && p.equals("password")) {
                    return true;
                }
                return false;
            }
        });

        server.setShellFactory(null);

        server.setCommandFactory(new ProcessShellCommandFactory());

        return server;
    }
    
    public static SshServer buildFileServer() throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setPort(3333);
        server.setHost("127.0.0.1");

        server.setKeyboardInteractiveAuthenticator(null);
        server.setGSSAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        server.setPublickeyAuthenticator(null);

        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String u, String p, ServerSession s) {
                if(u.equals("admin") && p.equals("password")) {
                    return true;
                }
                return false;
            }
        });

        server.setShellFactory(null);

        server.setCommandFactory(new ProcessShellCommandFactory());

        return server;
    }
}
