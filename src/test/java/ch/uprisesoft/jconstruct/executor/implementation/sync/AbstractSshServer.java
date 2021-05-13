/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

/**
 *
 * @author rmaire
 * 
 * https://stackoverflow.com/questions/62692515/how-to-upload-download-files-using-apache-sshd-scpclient
 */
public abstract class AbstractSshServer {
    protected SshServer server;
    
    protected int port;
    protected String user;
    protected String password;
    protected String host;
    protected Path testFolder;

    public AbstractSshServer() throws IOException {
        this(3333, "user", "password", "127.0.0.1");
//        this.testFolder = Files.createTempDirectory("MockSsh");
    }

    public AbstractSshServer(int port, String user, String password, String host) throws IOException {
        this.testFolder = Files.createTempDirectory("MockSsh");
        this.port = port;
        this.user = user;
        this.password = password;
        this.host = host;
        
        this.server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setHost(host);

        server.setKeyboardInteractiveAuthenticator(null);
        server.setGSSAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        server.setPublickeyAuthenticator(null);

        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        
        server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String u, String p, ServerSession s) {
                if(u.equals(user) && p.equals(password)) {
                    return true;
                }
                return false;
            }
        });
        
        setActualCommandFactory();

        server.setShellFactory(null);
        
        
        
    }
    
    public void start() throws IOException{
        server.start();
        server.open();
    }
    
    public void stop() throws IOException{
        server.stop();
        testFolder.toFile().delete();
    }
    
    public boolean isStarted() {
        return server.isStarted();
    }
    
    protected abstract void setActualCommandFactory();
    
}
