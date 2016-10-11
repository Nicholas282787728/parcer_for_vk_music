import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by diez on 5/1/16.
 */
public class VkApi {

    private void fakeSleep()
    {
        Random rnd = new Random(System.currentTimeMillis());
        try {
            Thread.sleep(rnd.nextInt(100000) % 4000 + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public VkApi(String token) {
        this.token = token;
    }

    public HashSet<VkUser> getAndFilterGroupMembers(String groupName) throws IOException, JSONException {
        HashSet<VkUser> retval = new HashSet<>();
        int i = 0;
         while (true) {
             String request = "https://api.vk.com/method/" +
                     "groups.getMembers?" +
                     "offset=" + i + "&" +
                     "group_id=" + groupName + "&" +
                     "fields=bdate,city,country,can_see_audio,can_write_private_message,last_seen&" +
                     "v=5.52&" +
                     "access_token=" + token;
             JSONObject json = readJsonFromUrl(request);
             JSONArray items = json.getJSONObject("response").getJSONArray("items");
             if (items.length() == 0) break;
             fakeSleep();
             System.out.println(
                     "processing " + items.length() + " users for page " + groupName + ". " +
                             "Offset: " + i
             );
             i += 1000;
             for (int j = 0; j < items.length(); j++) {
                 JSONObject item = (JSONObject)items.get(j);
                 if (!item.has("can_see_audio"))
                     continue;
                 if (!item.has("can_write_private_message"))
                     continue;
                 if (!item.has("last_seen"))
                     continue;
                 if (item.getInt("can_see_audio") != 1)
                     continue;
                 if (item.getInt("can_write_private_message") != 1)
                     continue;
                 if (item.getJSONObject("last_seen").getLong("time") < System.currentTimeMillis() / 1000 - 259200)
                     continue;
                 if (!item.has("country"))
                     continue;
                 if (item.getJSONObject("country").getInt("id") != 9)
                     continue;
                 VkUser user = new VkUser();
                 user.id = item.getLong("id");
                 user.firstName = item.getString("first_name");
                 user.lastName = item.getString("last_name");
                 if (item.has("city"))
                     user.city = item.getJSONObject("city").getString("title");

                 if (!retval.contains(user))
                     retval.add(user);
             }
         }


        return retval;
    }

    public HashSet<Song> getUserSongs(Long userId) throws Exception {
        HashSet<Song> retval = new HashSet<>();
        if (userId == 153415512)
            return retval;
        String request = "https://api.vk.com/method/" +
                "audio.get?" +
                "owner_id=" + userId + "&" +
                "v=5.52&" +
                "access_token=" + token;
        JSONObject json = readJsonFromUrl(request);
        JSONArray items;
        try {
            items = json.getJSONObject("response").getJSONArray("items");
        } catch (JSONException e) {
            System.out.println(request);
            System.out.println(json);
            throw new Exception("capcha?");

        }
//        try {
//            items = json.getJSONObject("response").getJSONArray("items");
//        }
//        catch (Exception e) {
//        }
        fakeSleep();
        for (int j = 0; j < items.length(); j++) {
            JSONObject item = (JSONObject)items.get(j);
            Song song = new Song(
                    item.getString("artist"),
                    item.getString("title")
            );

            if (!retval.contains(song))
                retval.add(song);
        }


        return retval;
    }


    private String token;
}
