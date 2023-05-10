-optimizations !code/simplification/variable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment

-keepclassmembers class * implements android.os.Parcelable {
    static *** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
