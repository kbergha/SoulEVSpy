package org.hexpresso.elm327.commands.protocol;

import org.hexpresso.elm327.commands.AbstractCommand;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-25.
 */
public class EchoCommand extends AbstractCommand {

    public EchoCommand(boolean isOn) {
        super("AT E" + (isOn ? "1" : "0"));
    }
}
