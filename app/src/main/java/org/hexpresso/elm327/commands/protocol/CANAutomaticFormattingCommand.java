package org.hexpresso.elm327.commands.protocol;

import org.hexpresso.elm327.commands.AbstractCommand;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-25.
 */
public class CANAutomaticFormattingCommand extends AbstractCommand {
    public CANAutomaticFormattingCommand(boolean isOn) {
        super("AT CAF" + (isOn ? "1" : "0"));
    }
}
