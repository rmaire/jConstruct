/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uprisesoft.jconstruct.executor.implementation.sync;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

public class EchoShellFactory implements ShellFactory {
    public static final EchoShellFactory INSTANCE = new EchoShellFactory();

    public EchoShellFactory() {
        super();
    }

    @Override
    public Command createShell(ChannelSession channel) {
        return new EchoShell();
    }
}