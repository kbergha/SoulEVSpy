package org.hexpresso.elm327.io;

import org.hexpresso.elm327.commands.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-27.
 */
public class Protocol {
    //
    private LinkedBlockingQueue<Message> mMessageInputQueue = new LinkedBlockingQueue<>();
    private Thread mExecutionThread = null;

    //
    private LinkedBlockingQueue<Message> mMessageOutputQueue = new LinkedBlockingQueue<>();
    private Thread mProcessingThread = null;
    private List<MessageReceivedListener> mMessageReceivedListeners = new ArrayList<>();

    // Input/output streams
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;

    public Protocol() {

        // Thread used to execute ELM327 commands
        mExecutionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                executeMessages();
            }
        });

        // Thread used to process the received messages
        mProcessingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processReceivedMessages();
            }
        });
    }

    /**
     * Adds the specified Command to the message queue and execute it
     * @param command Command to execute
     * @return True if the command was successfully added, false otherwise
     */
    public boolean addCommand(Command command) {
        // Create the message wrapper for the queue
        Message message = new Message(command);

        try {
            mMessageInputQueue.put(message);
        } catch (InterruptedException e) {
            // An error occurred while adding the message to the execution queue, flag the error
            message.setState(Message.State.ERROR_QUEUE);
            addMessageToProcessingQueue(message);
            return false;
        }

        return true;
    }

    /**
     * Adds the specified Message to the processing queue.
     * @param message Message to add
     */
    private void addMessageToProcessingQueue(Message message) {
        try {
            mMessageOutputQueue.put(message);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Command execution thread loop
     * Takes a Message from the message queue and executes it
     */
    private void executeMessages() {
        while (!mExecutionThread.isInterrupted()) {
            Message message = null;

            try {
                message = mMessageInputQueue.take();

                message.setState(Message.State.EXECUTING);

                // TODO PEM : check for errors
                message.getCommand().execute(mInputStream, mOutputStream);

                message.setState(Message.State.FINISHED);
            } catch (InterruptedException e) {
                mExecutionThread.interrupt();
            } catch (IOException e) {

            }

            if(message != null) {
                // Add the Messabe object to the processing queue
                addMessageToProcessingQueue(message);
            }
        }
    }

    /**
     * Process the received ELM327 messages, then dispatches the messages to the registered
     * MessageReceivedListener objects.
     */
    private void processReceivedMessages() {
        while (!mProcessingThread.isInterrupted())
        {
            Message message = null;

            try {
                message = mMessageOutputQueue.take();

                // TODO Call ProcessResponse method (add it to the Command class).

                // Notify registered MessageReceivedListener objects
                for (Iterator<MessageReceivedListener> i=mMessageReceivedListeners.iterator(); i.hasNext(); ) {
                    MessageReceivedListener listener = i.next();
                    try {
                        listener.onMessageReceived(message);
                    }
                    catch (RuntimeException e) {
                        // An error occurred, remove the listener
                        i.remove();
                    }
                }
            } catch (InterruptedException e) {
                mProcessingThread.interrupt();
            }
        }
    }

    /**
     * Starts the protocol
     * @param in Input stream
     * @param out Output stream
     */
    public synchronized void start(InputStream in, OutputStream out) {
        mInputStream = in;
        mOutputStream = out;

        mExecutionThread.start();
        mProcessingThread.start();
    }

    /**
     * Stops the protocol
     */
    public synchronized void stop() {
        // Stops the execution
        mExecutionThread.interrupt();
        mProcessingThread.interrupt();

        // TODO : Cancel the pending messages and transfer to the received queue (flagged as queue error or something)
        mMessageInputQueue.clear();
        mMessageOutputQueue.clear();

        mInputStream = null;
        mOutputStream = null;
    }

    /**
     * A received Message listener.
     */
    public interface MessageReceivedListener {

        /**
         * Receives notification that an ELM327 message response has been received.
         * @param message The Message object.
         */
        void onMessageReceived(Message message);
    }

    /**
     * Registers an event listener that listens for received ELM327 Message.
     * @param listener A MessageReceivedListener object
     * @return True if the listener was successfully registered, false otherwise.
     */
    public boolean registerOnMessageReceivedListener(MessageReceivedListener listener) {
        return mMessageReceivedListeners.add(listener);
    }

    /**
     * Unregisters an event listener.
     * @param listener A MessageReceivedListener object
     * @return True if the listener was successfully unregistered, false otherwise.
     */
    public boolean unregisterOnMessageReceivedListener(MessageReceivedListener listener) {
        return mMessageReceivedListeners.remove(listener);
    }
}