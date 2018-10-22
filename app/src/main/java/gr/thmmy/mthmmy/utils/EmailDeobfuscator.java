package gr.thmmy.mthmmy.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

//Deobfuscates Cloudflare-obfuscated emails
public class EmailDeobfuscator {
    public static Response deobfuscate(Response response) throws IOException {
        String responseBody = response.body().string();
        Document document = Jsoup.parse(responseBody);
        Elements obfuscatedEmails = document.select("span.__cf_email__");
        for (Element obfuscatedEmail : obfuscatedEmails) {
            String email = deobfuscateEmail(obfuscatedEmail.attr("data-cfemail"));
            Element parent = obfuscatedEmail.parent();
            if (parent.is("a")&&parent.attr("href").contains("email-protection"))
                parent.attr("href", "mailto:"+email);
            obfuscatedEmail.replaceWith(new TextNode(email, ""));
        }

        MediaType contentType = response.body().contentType();
        ResponseBody body = ResponseBody.create(contentType, document.toString());
        return response.newBuilder().body(body).build();
    }


    private static String deobfuscateEmail(final String encodedString) {
        final StringBuilder email = new StringBuilder();
        final int r = Integer.parseInt(encodedString.substring(0, 2), 16);
        for (int n = 2; n < encodedString.length(); n += 2) {
            final int i = Integer.parseInt(encodedString.substring(n, n+2), 16) ^ r;
            email.append(Character.toString ((char) i));
        }
        return email.toString();
    }
}
