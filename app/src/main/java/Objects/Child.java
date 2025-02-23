package Objects;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Child implements Parcelable {

    private String ID;
    private String FullName;
    private int Age;
    private String Hobby;
    private List<String> classs;
    private String GartenName;
    private List<Note> notes;


    public Child() {
    }

    public Child(String id, String fullName, Integer age, String hobby, List<String> classs, String gartenName) {
        ID = id;
        FullName = fullName;
        Age = age;
        Hobby = hobby;
        this.classs = classs;
        this.GartenName = gartenName;
    }

    protected Child(Parcel in) {
        ID = in.readString();
        FullName = in.readString();
        Age = in.readInt();
        Hobby = in.readString();
        classs = in.createStringArrayList();
        GartenName = in.readString();
    }

    public static final Creator<Child> CREATOR = new Creator<Child>() {
        @Override
        public Child createFromParcel(Parcel in) {
            return new Child(in);
        }

        @Override
        public Child[] newArray(int size) {
            return new Child[size];
        }
    };

    public String getID() {
        return ID;
    }

    public void setID(String id) {
        ID = id;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public Integer getAge() {
        return Age;
    }

    public void setAge(Integer age) {
        Age = age;
    }

    public String getHobby() {
        return Hobby;
    }

    public void setHobby(String hobby) {
        Hobby = hobby;
    }

    public List<String> getHobbies() {
        return classs;
    }

    public void setHobbies(List<String> hobbies) {
        this.classs = hobbies;
    }

    public String getGartenName() {
        return GartenName;
    }

    public void setGartenName(String gartenName) {
        this.GartenName = gartenName;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(FullName);
        dest.writeInt(Age);
        dest.writeString(Hobby);
        dest.writeStringList(classs);
        dest.writeString(GartenName);
    }
}
