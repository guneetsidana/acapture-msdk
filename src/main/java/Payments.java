import static spark.Spark.*;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import spark.utils.IOUtils;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class Payments {
    public static void main(String[] args) {
        // Assign port.
        port(getHerokuAssignedPort());

        get("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("heading", "Acapture Payments API");

            return new ModelAndView(model, "index.mustache"); // resources/templates directory
        }, new MustacheTemplateEngine());

        get("/apple-pay", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("heading", "Apple Payments Demo");
            model.put("transaction_id", request.queryParams("checkout_id"));

            return new ModelAndView(model, "apple_pay.mustache"); // resources/templates directory
        }, new MustacheTemplateEngine());

        get("/checkout", (request, response) -> {
            URL url = new URL("https://test.acaptureservices.com/v1/checkouts");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            String postParams = "authentication.userId=" + System.getenv().get("AUTHENTICATION_USERID") +
                    "&authentication.password=" + System.getenv().get("AUTHENTICATION_PASSWORD") +
                    "&authentication.entityId=" + System.getenv().get("AUTHENTICATION_ENTITY_ID");

            for (String key: request.queryParams()) {
                postParams = postParams.concat("&"+key+"="+request.queryParams(key));
            }

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

        get("/payment-status", (request, response) -> {
            URL url = new URL("https://test.acaptureservices.com" + request.queryParams("resourcePath") +
                    "?authentication.userId=" + System.getenv().get("AUTHENTICATION_USERID") +
                    "&authentication.password=" + System.getenv().get("AUTHENTICATION_PASSWORD") +
                    "&authentication.entityId=" + System.getenv().get("AUTHENTICATION_ENTITY_ID"));

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
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
