package com.evranger.elm327.commands.protocol;

import com.evranger.elm327.commands.AbstractCommand;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;
import com.evranger.elm327.commands.filters.RegularExpressionResponseFilter;

import java.util.List;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-26.
 */
public class ReadInputVoltageCommand extends AbstractCommand {

    private Double voltage;

    public ReadInputVoltageCommand() {
        super("AT RV");
        addResponseFilter(new RegularExpressionResponseFilter("(\\d+\\.?\\d+)V?"));
        withAutoProcessResponse(true);
        setTimeoutMs(3000);
    }

    public double getInputVoltage() {
        List<String> lines = getResponse().getLines();
        if (lines != null && !lines.isEmpty()) {
            voltage = new Double(lines.get(0));
        } else {
            voltage = 0.0;
        }
        return voltage;
    }
    public void doProcessResponse() {
        CurrentValuesSingleton.getInstance().set("ELM327.Voltage_V", new Double(getInputVoltage()));
    }
}
