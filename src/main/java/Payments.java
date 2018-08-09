import static spark.Spark.*;
import spark.utils.IOUtils;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Payments {
    public static void main(String[] args) {
        port(getHerokuAssignedPort());

        get("/checkout", (request, response) -> {

            // Get the checkout Id.
            URL url = new URL("https://test.acaptureservices.com/v1/checkouts");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            String postParams = "authentication.userId=8a829417572279ad015732d66d0327ba&authentication.password=Ena74K3gzS&authentication.entityId=8a829417572279ad015732d66cb427b6";

            for (String key: request.queryParams()) {
                postParams = postParams.concat("&"+key+"="+request.queryParams(key));
            }

            System.out.print(postParams);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            InputStream is;

            if (responseCode >= 400) is = conn.getErrorStream();
            else is = conn.getInputStream();

            return IOUtils.toString(is);
        });
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 7070; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
