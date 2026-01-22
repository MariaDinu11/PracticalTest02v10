package ro.pub.cs.systems.eim.practicaltest02v10;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
public class PracticalTest02v10MainActivity extends AppCompatActivity {
    private EditText port, name, type, ability, url;
    Button connectBtn;

//    private static EditText serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_practical_test02v10_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        port = findViewById(R.id.port);
        name = findViewById(R.id.name);
        type = findViewById(R.id.type);
        ability = findViewById(R.id.ability);
        url = findViewById(R.id.url);

        connectBtn = findViewById(R.id.connect);
//        connectBtn.setOnClickListener(v -> {
//            Server server = new Server(Integer.parseInt(port.getText().toString()));
//            server.start();
//        });
        String address = "localhost";

//        Button sendReq = findViewById(R.id.sendReqBtn);
        connectBtn.setOnClickListener(v -> {

            Server server = new Server(Integer.parseInt(port.getText().toString()));
            server.start();
            Client client = new Client(address, Integer.parseInt(port.getText().toString()), name.getText().toString());
            client.start();
        });
    }
    static class Server {
        private HashMap<String, String> cache = new HashMap<>();
        Integer port;
        public Server(Integer port) {
            this.port = port;
        }
        public void start() {
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    Log.d("[SERVER]", "Connected: " + serverSocket.getInetAddress() + " " +
                            serverSocket.getLocalPort());
                    while (true) {
                        Socket socket = serverSocket.accept();
                        new Thread(() -> handleClient(socket)).start();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        private void handleClient(Socket socket) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String request = in.readLine();
                Log.d("[SERVER]", request);
                String response;
//                if (cache.containsKey(request)) {
//                    response = cache.get(request);
//                } else {
//                    response = callAPI(request);
//                    cache.put(request, response);
//                }
//                String filter = in.readLine();

                response = callAPI(request);

//                String name = in.readLine();
                String type = new JSONObject(response).getJSONArray("types").getJSONObject(0).getJSONObject("type").getString("name");
                String ability = new JSONObject(response).getJSONArray("abilities").getJSONObject(0).getString("ability");
                String url = new JSONObject(response).getJSONObject("sprites").getString("front_default");
//                out.println(type);
//                out.println(ability);
                out.println(url);

                socket.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private String callAPI(String request) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + request);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                connection.disconnect();
                return stringBuilder.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    class Client {
        private String addr;
        private Integer port;
        private String option;
        public Client(String addr, Integer port, String option) {
            this.addr = addr;
            this.port = port;
            this.option = option;
        }
        public void start() {
            new Thread(() -> {
                try {
                    Socket socket = new Socket(addr, port);
                    Log.d("[CLIENT]", "Connected to " + socket.getInetAddress() + " " +
                            socket.getLocalPort() + " " + socket.getPort());
                    Log.d("[CLIENT]", "Filter is: " + option);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
// out.println(cityName);
                    out.println(option);
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(socket.getInputStream()));
                    String response = reader.readLine();
                    Log.d("[CLIENT]", response);
//                    PracticalTest02v10MainActivity.this.runOnUiThread(() -> {
//                        serverResponse.setText(response);
//                    });
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}