package ch.uprisesoft.jconstruct;

import ch.uprisesoft.jconstruct.executor.ExecutorBuilder;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import ch.uprisesoft.jconstruct.executor.implementation.sync.SshExecutor;
import ch.uprisesoft.jconstruct.executor.implementation.sync.WinRMExecutor;
import ch.uprisesoft.jconstruct.target.Target;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public class Command {

    private final Target target;
    private final OsType os;
    private final List<OutputObserver> observers = new ArrayList<>();
    private Map<String, String> environment = new HashMap<>();
    private ExecutorBuilder localExecutorBuilder = null;

    public Command(Target target, OsType os) {
        this.target = target;
        this.os = os;
    }

    private Command(Target target, OsType os, ExecutorBuilder locaExecutorBuilder) {
        this.target = target;
        this.os = os;
        this.localExecutorBuilder = locaExecutorBuilder;
    }

    public void upload(File file, String path) {
        getExecutorBuilder()
                .withUpload(file, path)
                .build()
                .runCommands();
    }

    public void makeExecutable(String target) {
        getExecutorBuilder()
                .withCommand("chmod +x " + target)
                .build()
                .runCommands();
    }

    public void callScript(String script) {
        getExecutorBuilder()
                .withCommand("source " + script)
                .withEnvironment(environment)
                .build()
                .runCommands();
    }

    public void register(OutputObserver observer) {
        this.observers.add(observer);
    }

    public void addEnvironmentVariable(String name, String value) {
        environment.put(name, value);
    }

    public void runCommand(String command) {
        getExecutorBuilder()
                .withCommand(command)
                .withEnvironment(environment)
                .build()
                .runCommands();
    }

    private ExecutorBuilder getExecutorBuilder() {

        ExecutorBuilder builder = null;
        if (localExecutorBuilder != null) {
            builder = localExecutorBuilder;
        } else if (os == OsType.LINUX) {
            builder = new SshExecutor.Builder();
        } else if (os == OsType.WINDOWS) {
            builder = new WinRMExecutor.Builder();
        }

        builder.withTarget(target);
        for (OutputObserver observer : observers) {
            builder.register(observer);
        }

        return builder;
    }
}
