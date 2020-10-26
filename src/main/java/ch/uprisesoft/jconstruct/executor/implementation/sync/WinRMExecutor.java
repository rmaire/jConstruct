package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.executor.ExecutorException;
import ch.uprisesoft.jconstruct.executor.OutputEntry;
import ch.uprisesoft.jconstruct.executor.OutputEntryType;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class WinRMExecutor implements Executor {

    private final static String UPLOAD_COMMAND = "If ((!(Test-Path %s)) -or ((Get-Item '%s').length -eq %s)) {Add-Content -Encoding Byte -path %s -value ([System.Convert]::FromBase64String(\"%s\"))}";
    private final static String REMOVE_COMMAND = "If (Test-Path %s){ Remove-Item %s }";

    private final WinRmTool tool;
    private final WinRmClientContext context;
    private final List<String> commands;
    private final List<OutputObserver> observers;
    private final List<Pair<String, String>> uploads;
    private final Set<String> tags = new HashSet<>();

    protected WinRMExecutor(WinRmTool tool, WinRmClientContext context, List<String> commands, List<OutputObserver> observers, List<Pair<String, String>> uploads, int timeout) {
        this.tool = tool;
        this.context = context;
        this.commands = commands;
        this.observers = observers;
        this.uploads = uploads;
        this.tool.setOperationTimeout(new Long(timeout) );
        
    }

    private WinRMExecutor(Builder builder) {
        this.commands = builder.commands;
        this.context = builder.context;
        this.tool = builder.tool;
        this.uploads = builder.uploads;
        this.observers = builder.observers;
        this.tool.setOperationTimeout(new Long(builder.timeout) );
    }

    @Override
    public void runCommands() {

        for (Pair<String, String> file : uploads) {
            String content = file.getLeft();
            String path = file.getRight();

            // Delete if exists
            WinRmToolResponse response;

            try {
                response = tool.executePs(String.format(REMOVE_COMMAND, path, path));
            } catch (ClassCastException ex) {
                  throw new ExecutorException("Connection already closed", ex);
            }

            publishMessages(response);

            // Create file
            String commandWithFileContent = String.format(UPLOAD_COMMAND, path, path, content.length(), path, new String(Base64.getEncoder().encode(content.getBytes())));

            try {
                response = tool.executePs(commandWithFileContent);
            } catch (ClassCastException ex) {
                  throw new ExecutorException("Connection already closed", ex);
            }

            publishMessages(response);
        }

        for (String command : commands) {
            final WinRmToolResponse response;

            try {
//                response = tool.executeCommand(command);
                response = tool.executePs(command);
            } catch (ClassCastException ex) {
                  throw new ExecutorException("Connection already closed", ex);
            }

            publishMessages(response);
        }

        context.shutdown();
    }

    private void notifyObservers(OutputEntry entry) {
        for (OutputObserver o : observers) {
            o.inform(entry);
        }
    }

    private void publishMessages(WinRmToolResponse response) {
        OutputEntry entry;
        if (response.getStatusCode() == 0) {
            entry = createInfoEntry(response.getStdOut(), tags);
        } else {
            entry = createErrorEntry(response.getStdErr(), tags);
        }

        notifyObservers(entry);
    }

    private OutputEntry createInfoEntry(String entry, Set<String> tags) {
        return new OutputEntry(entry, OutputEntryType.INFO, tags);
    }

    private OutputEntry createErrorEntry(String entry, Set<String> tags) {
        return new OutputEntry(entry, OutputEntryType.ERROR, tags);
    }

    public static class Builder extends AbstractExecutorBuilder {

        private final Integer DEFAULT_PORT = 5985;

        private WinRmClientContext context;
        private WinRmTool.Builder builder;
        private WinRmTool tool;
        private Boolean useHttps = false;

        public Builder() {
            this.context = context = WinRmClientContext.newInstance();
            this.commands = new ArrayList<>();
            this.uploads = new ArrayList<>();
        }

        public Builder useHttps() {
            this.useHttps = true;
            return this;
        }

        
        /**
         * winrm set winrm/config/service/Auth '@{Basic="true"}'
         * winrm set winrm/config/service '@{AllowUnencrypted="true"}'
         * 
         */
        @Override
        public Executor build() {

            if (commands.isEmpty() && uploads.isEmpty()) {
                  throw new ExecutorException("No command given");
            }

            if (!target.getHost().isPresent()) {
                  throw new ExecutorException("Host must be set for Target");
            }

            if (!target.getUsername().isPresent()) {
                  throw new ExecutorException("Username must be set for Target");
            }

            if (!target.getPassword().isPresent()) {
                  throw new ExecutorException("Password must be set for Target");
            }

            this.builder = WinRmTool.Builder.builder(target.getHost().get(), target.getUsername().get(), target.getPassword().get());
//            builder.setAuthenticationScheme(AuthSchemes.NTLM);
            builder.port(target.getPort().orElse(DEFAULT_PORT));
            builder.useHttps(this.useHttps);
            builder.context(this.context);
            if (environment.isPresent()) {
                builder.environment(environment.get());
            }
            this.tool = builder.build();
            return new WinRMExecutor(this);
        }

        @Override
        protected String platformDependendStringJoin(List<String> file) {
            return String.join("\r\n", file);
        }
    }

}
