package launcher.error

object Errors {
    const val notAClass = "fields may only be contained in classes."
    const val privateClass = "fields may not be contained in private classes."
    const val notSupportedType = "fields must extend from Serializable, Parcelable or be of type String, int, float, double, char or boolean."
    const val inaccessibleField = "Inaccessable element."
    const val notBasicTypeInReceiver = "On BroadcastReceiver only basic types are supported."
    const val wrongClassType = "Is in wrong type. It needs to be Activity, Fragment, Service or BroadcastReceiver."
    const val noSetter = "No setter found."
}
