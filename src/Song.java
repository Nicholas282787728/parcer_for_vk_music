/**
 * Created by diez on 5/1/16.
 */
public class Song {
    String artist;

    public Song(String artist, String name) {
        this.artist = artist;
        this.title = name;
    }

    @Override
    public int hashCode() {
        return (artist + title).hashCode();
    }
    String title;
}
