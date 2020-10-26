package ch.uprisesoft.jconstruct.target;

import java.io.File;
import java.util.Optional;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class Target {

    private final Optional<String> host;
    private final Optional<String> username;
    private final Optional<String> password;
    private final Optional<String> key;
    private final Optional<File> keyFile;
    private final Optional<Integer> port;

    public Optional<String> getHost() {
        return host;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getPassword() {
        return password;
    }

    public Optional<String> getKey() {
        return key;
    }
    
    public Optional<File> getKeyFile() {
        return keyFile;
    }

    public Optional<Integer> getPort() {
        return port;
    }

    private Target(Builder builder) {
        this.host = builder.host;
        this.key = builder.key;
        this.keyFile = builder.keyFile;
        this.password = builder.password;
        this.username = builder.username;
        this.port = builder.port;
    }

    public static class Builder {

        private Optional<String> host = Optional.empty();
        private Optional<String> username = Optional.empty();
        private Optional<String> password = Optional.empty();
        private Optional<String> key = Optional.empty();
        private Optional<File> keyFile = Optional.empty();
        private Optional<Integer> port = Optional.empty();

        public Builder withHost(String host) {
            this.host = Optional.of(host);
            return this;
        }
        
        public Builder withUsername(String username) {
            this.username = Optional.of(username);
            return this;
        }
        
        public Builder withPassword(String password) {
            this.password = Optional.of(password);
            return this;
        }
        
        public Builder withKey(String key) {
            this.key = Optional.of(key);
            return this;
        }
        
        public Builder withKeyFile(File key) {
            this.keyFile = Optional.of(key);
            return this;
        }
        
        public Builder withPort(Integer port) {
            this.port = Optional.of(port);
            return this;
        }
        
        public Target build() {
            return new Target(this);
        }

    }
}
