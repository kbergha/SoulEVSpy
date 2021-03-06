package com.evranger.elm327;

import android.util.Pair;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import com.evranger.elm327.commands.AbstractCommand;
import com.evranger.elm327.commands.Response;
import com.evranger.elm327.commands.ResponseFilter;
import com.evranger.elm327.commands.general.VehicleIdentifierNumberCommand;
import com.evranger.elm327.commands.protocol.PrintVersionIdCommand;
import com.evranger.elm327.commands.protocol.ReadInputVoltageCommand;
import com.evranger.elm327.exceptions.MisunderstoodCommandException;
import com.evranger.soulevspy.Responder;
import com.evranger.soulevspy.obd.commands.BatteryManagementSystemCommand;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;
import com.evranger.soulevspy.util.ClientSharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-24.
 */
@RunWith(AndroidJUnit4.class)
public class CommandTest {

    ByteArrayInputStream input = null;
    ByteArrayOutputStream output = null;

    final String msgOk = "OK \r" +
                         ">";

    final String msg2101 = "7EC 10 3D 61 01 FF FF FF FF \r" +
//                           "7EA 10 0E 61 01 F0 00 00 00 \r" +
                           "7EC 21 15 23 28 1E C8 03 00 \r" +
//                           "7EA 21 ED 05 02 03 00 00 00 \r" +
                           "7EC 22 1E 0C DD 0E 0D 0E 0D \r" +
//                           "7EA 22 00 00 00 00 00 00 00 \r" +
                           "7EC 23 0D 0D 0C 00 0F AB 34 \r" +
                           "7EC 24 AB 43 00 00 84 00 00 \r" +
                           "7EC 25 44 D4 00 00 49 F8 00 \r" +
                           "7EC 26 00 19 B3 00 00 1A EA \r" +
                           "7EC 27 00 09 EC 96 45 01 45 \r" +
                           "7EC 28 00 00 00 00 03 E8 00 \r" +
                           ">";

    final String msg2102 = "7EC 10 26 61 02 FF FF FF FF \r" +
                            "7EC 21 CA CA CA CA CA CA CA \r" +
                            "7EC 22 CA C9 C9 C9 C9 C9 C9 \r" +
                            "7EC 23 CA C9 C9 C9 C9 C9 C9 \r" +
                            "7EC 24 C9 C9 C9 CA C9 C9 C9 \r" +
                            "7EC 25 C9 C9 C9 C9 00 00 00 \r" +
                            ">";

    final String msg2103 = "7EC 10 26 61 03 FF FF FF FF \r" +
                            "7EC 21 C9 C9 CA C9 C9 C9 C9 \r" +
                            "7EC 22 C9 C9 CA CA CA CA CA \r" +
                            "7EC 23 CA CA CA CA CA CA CA \r" +
                            "7EC 24 CA CA CA CA CA CA C9 \r" +
                            "7EC 25 C9 C9 C9 C9 00 00 00 \r" +
                            ">";

    final String msg2104 = "7EC 10 26 61 04 FF FF FF FF \r" +
                            "7EC 21 C9 C9 C9 CA C9 C9 C9 \r" +
                            "7EC 22 C9 C9 C9 C9 CA C9 C9 \r" +
                            "7EC 23 C9 C9 C9 C9 CA C9 C9 \r" +
                            "7EC 24 C9 C9 C9 C9 CA CA CA \r" +
                            "7EC 25 CA CA CA CA 00 00 00 \r" +
                            ">";

    final String msg2105 = "7EC 10 2C 61 05 FF FF FF FF \r" +
                            "7EC 21 00 00 00 00 00 10 0F \r" +
                            "7EC 22 10 00 00 00 00 1B CC \r" +
                            "7EC 23 23 28 00 01 55 10 10 \r" +
                            "7EC 24 00 49 01 00 3F 12 B4 \r" +
                            "7EC 25 00 00 00 00 00 00 00 \r" +
                            "7EC 26 00 00 00 00 00 00 00 \r" +
                            ">";

    final String ray2101 = "7EC 10 3D 61 01 FF FF FF FF\r" +
                            "7EC 21 4E 18 38 18 38 03 00\r" +
                            "7EC 22 2F 0D 18 1E 1D 1D 1D\r" +
                            "7EC 23 1D 1D 1D 00 1F BE 0C\r" +
                            "7EC 24 BD 27 00 00 8B 00 05\r" +
                            "7EC 25 64 1D 00 05 6B 21 00\r" +
                            "7EC 26 01 DE C8 00 01 C8 2C\r" +
                            "7EC 27 00 B2 7F A3 45 01 54\r" +
                            "7EC 28 00 00 00 00 15 EB 00\r" +
                            ">";

    final String ray2102 = "7EC 10 26 61 02 FF FF FF FF\r" +
                            "7EC 21 BE BE BE BE BE BE BE\r" +
                            "7EC 22 BE BE BE BE BE BE BE\r" +
                            "7EC 23 BE BE BE BE BE BE BE\r" +
                            "7EC 24 BE BE BE BE BE BE BE\r" +
                            "7EC 25 BE BE BE BE 00 00 00\r" +
                            ">";

    final String ray2103 = "7EC 10 26 61 03 FF FF FF FF\r" +
                            "7EC 21 BE BE BE BE BE BE BD\r" +
                            "7EC 22 BE BE BE BE BE BE BE\r" +
                            "7EC 23 BE BE BE BE BE BE BE\r" +
                            "7EC 24 BE BE BE BE BE BE BE\r" +
                            "7EC 25 BE BE BE BE 00 00 00\r" +
                            ">";

    final String ray2104 = "7EC 10 1E 61 04 FF FF FF FF\r" +
                            "7EC 21 BE BE BE BE BE BE BE\r" +
                            "7EC 22 BE BE BE BE BE BE BE\r" +
                            "7EC 23 BE BE BE BE BE BE BE\r" +
                            "7EC 24 BE BE BE 00 00 00 00\r" +
                            ">";

    final String ray2105 = "7EC 10 18 61 05 FF FF FF FF\r" +
                            "7EC 21 01 2C 00 01 2C 1E 1D\r" +
                            "7EC 22 1D 1D 1D 1D 1D 18 38\r" +
                            "7EC 23 18 38 00 01 00 00 00\r" +
                            ">";

    class ElmCommand extends AbstractCommand implements ResponseFilter {

        public ElmCommand() {
            super("Command");
            addResponseFilter(this);
        }
        public void doProcessResponse() {}

        @Override
        public void onResponseReceived(Response response) {
            response.getLines().clear();
        }
    }

    private CurrentValuesSingleton vals;

    @Before
    public void init() {
        vals = CurrentValuesSingleton.reset();
        ClientSharedPreferences prefs = new ClientSharedPreferences(InstrumentationRegistry.getTargetContext());
        vals.setPreferences(prefs);

        output = new ByteArrayOutputStream();
    }

    /**
     *
     */
    @Test
    public void testBasicCommand() {
        final String response = "ABCD>";
        input = new ByteArrayInputStream(response.getBytes());

        ElmCommand cmd = (ElmCommand) new ElmCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(input, output);
        }
        catch(Exception e)
        {
            // ...
        }

        assertEquals("Command\r", output.toString());
        assertTrue(cmd.getResponse().getLines().isEmpty());
    }

    /**
     *
     */
    @Test
    public void testBmsCommand() throws IOException {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7DF", msgOk),
                new Pair<String, String>("AT CRA 7EC", msgOk),
                new Pair<String, String>("21 01", msg2101),
                new Pair<String, String>("21 02", msg2102),
                new Pair<String, String>("21 03", msg2103),
                new Pair<String, String>("21 04", msg2104),
                new Pair<String, String>("21 05", msg2105)
                );
        Responder responder = new Responder(reqres);

        BatteryManagementSystemCommand cmd = (BatteryManagementSystemCommand) new BatteryManagementSystemCommand(true);

        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertEquals("", responder.getMessages());
        assertEquals(10.5, vals.get("battery.SOC_pct"));
    }

    @Test
    public void testRayEvBmsCommand() {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7DF", msgOk),
                new Pair<String, String>("AT CRA 7EC", msgOk),
                new Pair<String, String>("21 01", ray2101),
                new Pair<String, String>("21 02", ray2102),
                new Pair<String, String>("21 03", ray2103),
                new Pair<String, String>("21 04", ray2104),
                new Pair<String, String>("21 05", ray2105)
        );
        Responder responder = new Responder(reqres);

        BatteryManagementSystemCommand cmd = (BatteryManagementSystemCommand) new BatteryManagementSystemCommand(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertEquals(39.0, vals.get("battery.SOC_pct"));
    }

    @Test
    public void testReadInputVoltage() {
        final String response = "12.5V\r>";
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT RV", response)
        );
        Responder responder = new Responder(reqres);

        ReadInputVoltageCommand cmd = (ReadInputVoltageCommand) new ReadInputVoltageCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertEquals(12.5, cmd.getInputVoltage(), 0.00001);
    }

    @Test
    public void testNoResponseReadInputVoltage() {
        final String response = "?\r>";
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT.?RV", response)
        );
        Responder responder = new Responder(reqres);

        boolean caught = false;
        ReadInputVoltageCommand cmd = (ReadInputVoltageCommand) new ReadInputVoltageCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
        }
        catch (MisunderstoodCommandException m)
        {
            caught = true;
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertTrue(caught);
        assertEquals(0.0, cmd.getInputVoltage(), 0.0001);
    }

    @Test
    public void testVersion() {
        final String response = "ELM327 v1.5\r>";
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT I", response)
        );
        Responder responder = new Responder(reqres);

        PrintVersionIdCommand cmd = (PrintVersionIdCommand) new PrintVersionIdCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertEquals("ELM327 v1.5", cmd.getVersion());
    }

    @Test
    public void testVehicleIdentificationNumber() {
        final String vinSoulEv = "7EA 10 14 49 02 01 4B 4E 44 \r" +
                           "7EA 21 4A 58 33 41 45 31 47 \r" +
                           "7EA 22 37 31 32 33 34 35 36\r" +
                           ">";

        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("09 02", vinSoulEv)
        );
        Responder responder = new Responder(reqres);

        VehicleIdentifierNumberCommand cmd = (VehicleIdentifierNumberCommand) new VehicleIdentifierNumberCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertEquals("KNDJX3AE1G7123456", cmd.getValue());
    }

    @Test
    public void testRayEvVehicleIdentificationNumber() {
        final String vinRayEv = "7EB 03 7F 09 11\r" +
                                "7EA 03 7F 09 12\r" +
                                ">";

        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("09 02", vinRayEv)
        );
        Responder responder = new Responder(reqres);

        VehicleIdentifierNumberCommand cmd = (VehicleIdentifierNumberCommand) new VehicleIdentifierNumberCommand().withAutoProcessResponse(true);
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
        }
        catch(Exception e)
        {
            assertEquals("", e.toString());
        }

        assertTrue(cmd.getValue().startsWith("error"));
    }

    @Test
    public void testUnableToConnect() {
        final String vin = "...\rUNABLE TO CONNECT\r\r";

        input = new ByteArrayInputStream(vin.getBytes());

        VehicleIdentifierNumberCommand cmd = (VehicleIdentifierNumberCommand) new VehicleIdentifierNumberCommand().withAutoProcessResponse(true);

        boolean caughtIt = false;
        try {
            cmd.execute(input, output);
        }
        catch(Exception e)
        {
            assert(e.getMessage() == "UNABLE TO CONNECT");
            caughtIt = true;
        }
        assert(caughtIt);
    }
}
