import java.util.Objects;

/**
 * Created by diez on 5/1/16.
 */
public class VkUser {
    public Long id;
    public String firstName;
    public String lastName;
    public String city;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof VkUser))return false;
        VkUser otherMyClass = (VkUser) other;
        if (!Objects.equals(id, otherMyClass.id)) return false;
        if (!Objects.equals(firstName, otherMyClass.firstName)) return false;
        if (!Objects.equals(lastName, otherMyClass.lastName)) return false;
        if (!Objects.equals(city, otherMyClass.city)) return false;
        return true;
    }
}
