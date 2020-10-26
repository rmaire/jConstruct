package ch.uprisesoft.jconstruct.executor.implementation.sync;

import ch.uprisesoft.jconstruct.executor.Executor;
import ch.uprisesoft.jconstruct.executor.ExecutorBuilder;
import ch.uprisesoft.jconstruct.executor.OutputObserver;
import ch.uprisesoft.jconstruct.target.Target;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public abstract class AbstractExecutorBuilder implements ExecutorBuilder {

    protected Optional<Map<String, String>> environment = Optional.empty();
    protected List<OutputObserver> observers = new ArrayList<>();
    protected List<Pair<String, String>> uploads = new ArrayList<>();
    protected List<String> commands = new ArrayList<>();
    protected Target target;
    protected int timeout = 120;

    @Override
    public abstract Executor build();

    @Override
    public ExecutorBuilder withCommand(String command) {
        this.commands.add(command);
        return this;
    }

    @Override
    public AbstractExecutorBuilder withTarget(Target target) {
        this.target = target;
        return this;
    }

    @Override
    public AbstractExecutorBuilder withUpload(File file, String path) {
        String fileContent = "";
        try {
            fileContent = platformDependendStringJoin(Files.readAllLines(file.toPath()));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read file", ex);
        }
        this.uploads.add(Pair.of(fileContent, path));
        return this;
    }

    @Override
    public AbstractExecutorBuilder withUpload(String file, String path) {
        this.uploads.add(Pair.of(file, path));
        return this;
    }

    @Override
    public AbstractExecutorBuilder withUpload(List<String> file, String path) {
        this.uploads.add(Pair.of(platformDependendStringJoin(file), path));
        return this;
    }

    @Override
    public ExecutorBuilder withEnvironment(Map<String, String> environment) {
        this.environment = Optional.of(environment);
        return this;
    }

    @Override
    public ExecutorBuilder withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public ExecutorBuilder register(OutputObserver observer) {
        this.observers.add(observer);
        return this;
    }

    protected abstract String platformDependendStringJoin(List<String> file);

}
