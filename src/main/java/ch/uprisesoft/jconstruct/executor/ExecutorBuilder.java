/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor;

import ch.uprisesoft.jconstruct.target.Target;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Uprise Software <uprisesoft@gmail.com>
 */
public interface ExecutorBuilder {

    public Executor build();

    public ExecutorBuilder withCommand(String command);

    public ExecutorBuilder withTarget(Target target);

    public ExecutorBuilder withUpload(File file, String path);

    public ExecutorBuilder withUpload(String file, String path);

    public ExecutorBuilder withUpload(List<String> file, String path);

    public ExecutorBuilder withEnvironment(Map<String, String> environment);
    
    public ExecutorBuilder withTimeout(int timeout);

    public ExecutorBuilder register(OutputObserver observer);
    
}
