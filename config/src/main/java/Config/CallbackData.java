package Config;

/**
 * @author liketechnik
 * @version 1.0
 * @date 25 of March 2017
 */
public final class CallbackData {

    public enum SET_LANGUAGE {
        set_language_english, set_language_german, set_language_default
    }

    public enum DELETE_MEDIA {
        delete_media_, delete_media_delete_, delete_media_first, delete_media_middle, delete_media_last
    }

    public enum CONFIRM_DELETE_MEDIA {
        confirm_delete_media_yes_, confirm_delete_media_back_, confirm_delete_media_cancel
    }
}
