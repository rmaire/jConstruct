/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.target.Target;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.After;

public class SshExecutorTest {

    SshServer server;
    Target target;
    Executor executor;

    public SshExecutorTest() {
    }

    @Before
    public void setUp() throws IOException {

        /*server = new MockSshServerBuilder(2223)
                .usePasswordAuthentication("tester", "testing")
                .build();*/
//        server = SshServer.setUpDefaultServer();
//        server.setPort(2223);
//        server.setHost("127.0.0.1");
////        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
//        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hostkey.ser").toPath()));
//        server.setPasswordAuthenticator(new PasswordAuthenticator() {
//            @Override
//            public boolean authenticate(String u, String p, ServerSession s) {
//                return true;
//            }
//        });
//
//        server.setShellFactory(new ProcessShellFactory(new String[]{"bash"}));
//        server.start();
    }

    @After
    public void tearDown() throws IOException {
//        server.stop();
    }

    //@Test
    public void testConnect() throws IOException {
        
        Logger minaLogger = Logger.getLogger("org.apache.mina");

        minaLogger.setLevel(Level.DEBUG);


        target = new Target.Builder()
                .withHost("127.0.0.1")
                .withUsername("tester")
                .withPassword("testing")
                .withPort(2223)
                .build();

        executor = new SshExecutor.Builder()
                .withTarget(target)
                .withCommand("ls")
                .build();
        executor.runCommands();

    }

}
