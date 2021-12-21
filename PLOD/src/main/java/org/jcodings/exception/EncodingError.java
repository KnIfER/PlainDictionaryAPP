package org.jcodings.exception;

import org.jcodings.util.IntHash;

public enum EncodingError {
    ERR_TYPE_BUG(ErrorMessages.ERR_TYPE_BUG, ErrorCodes.ERR_TYPE_BUG),

    ERR_TOO_BIG_WIDE_CHAR_VALUE(ErrorMessages.ERR_TOO_BIG_WIDE_CHAR_VALUE, ErrorCodes.ERR_TOO_BIG_WIDE_CHAR_VALUE),
    ERR_TOO_LONG_WIDE_CHAR_VALUE(ErrorMessages.ERR_TOO_LONG_WIDE_CHAR_VALUE, ErrorCodes.ERR_TOO_LONG_WIDE_CHAR_VALUE),

    ERR_INVALID_CHAR_PROPERTY_NAME(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME, ErrorCodes.ERR_INVALID_CHAR_PROPERTY_NAME),
    ERR_INVALID_CODE_POINT_VALUE(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE, ErrorCodes.ERR_INVALID_CODE_POINT_VALUE),

    ERR_ENCODING_CLASS_DEF_NOT_FOUND(ErrorMessages.ERR_ENCODING_CLASS_DEF_NOT_FOUND, ErrorCodes.ERR_ENCODING_CLASS_DEF_NOT_FOUND),
    ERR_ENCODING_LOAD_ERROR(ErrorMessages.ERR_ENCODING_LOAD_ERROR, ErrorCodes.ERR_ENCODING_LOAD_ERROR),

    ERR_ENCODING_ALREADY_REGISTERED(ErrorMessages.ERR_ENCODING_ALREADY_REGISTERED, ErrorCodes.ERR_ENCODING_ALREADY_REGISTERED),
    ERR_ENCODING_ALIAS_ALREADY_REGISTERED(ErrorMessages.ERR_ENCODING_ALIAS_ALREADY_REGISTERED, ErrorCodes.ERR_ENCODING_ALIAS_ALREADY_REGISTERED),
    ERR_ENCODING_REPLICA_ALREADY_REGISTERED(ErrorMessages.ERR_ENCODING_REPLICA_ALREADY_REGISTERED, ErrorCodes.ERR_ENCODING_REPLICA_ALREADY_REGISTERED),
    ERR_NO_SUCH_ENCODNG(ErrorMessages.ERR_NO_SUCH_ENCODNG, ErrorCodes.ERR_NO_SUCH_ENCODNG),
    ERR_COULD_NOT_REPLICATE(ErrorMessages.ERR_COULD_NOT_REPLICATE, ErrorCodes.ERR_COULD_NOT_REPLICATE),

    // transcoder messages
    ERR_TRANSCODER_ALREADY_REGISTERED(ErrorMessages.ERR_TRANSCODER_ALREADY_REGISTERED, ErrorCodes.ERR_TRANSCODER_ALREADY_REGISTERED),
    ERR_TRANSCODER_CLASS_DEF_NOT_FOUND(ErrorMessages.ERR_TRANSCODER_CLASS_DEF_NOT_FOUND, ErrorCodes.ERR_TRANSCODER_CLASS_DEF_NOT_FOUND),
    ERR_TRANSCODER_LOAD_ERROR(ErrorMessages.ERR_TRANSCODER_LOAD_ERROR, ErrorCodes.ERR_TRANSCODER_LOAD_ERROR);

    EncodingError(String message, int code) {
        this.message = message;
        this.code = code;
    }

    private final String message;
    private final int code;

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public static EncodingError fromCode(int code) {
        return CODE_TO_ERROR.get(code);
    }

    private static final IntHash<EncodingError> CODE_TO_ERROR = new IntHash<EncodingError>();
    static {
        for (EncodingError error : EncodingError.values()) {
            CODE_TO_ERROR.put(error.getCode(), error);
        }
    }
}
