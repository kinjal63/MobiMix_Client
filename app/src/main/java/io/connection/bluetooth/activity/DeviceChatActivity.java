package io.connection.bluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.R;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 26/08/16.
 */
public class DeviceChatActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "DeviceChatActivity";
    static Context context;
    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private static Button mSendButton;
    ConnectedThread connectedThread;
    static BluetoothDevice device;
    static ChatAdapter chatAdapter;
    private static TextView chatUserName;
    private static TextView connectionStatus;


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private static List<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_chat_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setDivider(null);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        chatUserName = (TextView) findViewById(R.id.chat_user);
        connectionStatus = (TextView) findViewById(R.id.connection_status);
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setEnabled(false);
        context = this;


        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }


        Intent intent = getIntent();
        connectedThread = DeviceListActivityChat.getCurrentThread();
        device = intent.getParcelableExtra("device");

        chatUserName.setText(ChatDataConversation.getUserName(device.getAddress()));
        connectionStatus.setText("Connecting ...");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            setupChat();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mConversationArrayAdapter = new ArrayList<>();

        //mConversationView.setAdapter(mConversationArrayAdapter);

        List<String> stringList = ChatDataConversation.getChatConversation(device.getAddress());
        if (stringList != null && stringList.size() > 0) {
            Log.d(TAG, "setupChat:  Value of " + stringList.size());
            mConversationArrayAdapter.addAll(stringList);
            // mConversationArrayAdapter.notifyDataSetChanged();
        }

        chatAdapter = new ChatAdapter(context, mConversationArrayAdapter);
        mConversationView.setAdapter(chatAdapter);
        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                if (null != v) {
                    TextView textView = (TextView) findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    public static void enableSendButton() {
        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendButton.setEnabled(true);
                connectionStatus.setText("Connected");
            }
        });

    }

    public static void setConnectionStatus() {

        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText("Not Connected Try Again later");
            }
        });

    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            if (!message.equals("NOWweArECloSing")) {
                mConversationArrayAdapter.add("Me:  " + message);
                ChatDataConversation.putChatConversation(device.getAddress(), "Me:  " + message);
                chatAdapter.notifyDataSetChanged();
            }
            byte[] send = message.getBytes();
            connectedThread.sendMessage(send);


            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }


    public static void disconnectedChat(final String deviceAddress) {
        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConversationArrayAdapter.size() >= 0) {
                    mSendButton.setEnabled(false);
                    connectionStatus.setText("Disconnected");
                    ChatDataConversation.removeLastMessage(deviceAddress);
                    mConversationArrayAdapter.remove(mConversationArrayAdapter.size() - 1);
                    chatAdapter.notifyDataSetChanged();

                }

                Toast.makeText(context,"Go To device list and try again",Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendMessage("NOWweArECloSing");
        connectedThread.cancel();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: List View ");
        sendMessage("NOWweArECloSing");
        connectedThread.cancel();
    }

    public static void readMessagae(final BluetoothDevice deviceRemote) {

        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> listofStrings = ChatDataConversation.getChatConversation(deviceRemote.getAddress());
                if (mConversationArrayAdapter != null) {
                    if (deviceRemote.getAddress().equals(device.getAddress())) {
                        mConversationArrayAdapter.clear();
                        mConversationArrayAdapter.addAll(listofStrings);
                        chatAdapter.notifyDataSetChanged();
                    }

                }
            }
        });


    }


    static class ChatAdapter extends BaseAdapter {
        private static LayoutInflater inflater = null;


        List<String> listMessages = new ArrayList<>();
        Context context;


        public ChatAdapter(Context context, List<String> listMessages) {
            this.listMessages = listMessages;
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return listMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String chatMessage = listMessages.get(position).trim();
            Log.d(TAG, "getView: " + chatMessage + "   " + chatMessage.length());
            View vi = convertView;
            if (convertView == null) {
                vi = inflater.inflate(R.layout.chat_message, null);
            }
            TextView textView = (TextView) vi.findViewById(R.id.chat_messages);
            if (chatMessage.startsWith("Me:  ")) {
                chatMessage = chatMessage.split(":  ", 2)[1];
                textView.setText(chatMessage);
                //textView.setGravity(View.TEXT_ALIGNMENT_VIEW_START);
                textView.setBackgroundResource(R.drawable.background_chat);
                vi.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);


            } else {
                chatMessage = chatMessage.split(":  ", 2)[1];
                textView.setText(chatMessage);
                //textView.setGravity(View.TEXT_ALIGNMENT_VIEW_END);
                textView.setBackgroundResource(R.drawable.background_chat_left);
                vi.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            }

            return vi;

        }

    }


}
