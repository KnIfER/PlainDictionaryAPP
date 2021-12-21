package org.jcodings.exception;

public interface ErrorCodes {
    public static final int NORMAL = 0;
    public static final int MISMATCH = -1;
    public static final int NO_SUPPORT_CONFIG = -2;

    /* internal error */
    public static final int ERR_MEMORY = -5;
    public static final int ERR_TYPE_BUG = -6;
    public static final int ERR_PARSER_BUG = -11;
    public static final int ERR_STACK_BUG = -12;
    public static final int ERR_UNDEFINED_BYTECODE = -13;
    public static final int ERR_UNEXPECTED_BYTECODE = -14;
    public static final int ERR_MATCH_STACK_LIMIT_OVER = -15;
    public static final int ERR_DEFAULT_ENCODING_IS_NOT_SET = -21;
    public static final int ERR_SPECIFIED_ENCODING_CANT_CONVERT_TO_WIDE_CHAR = -22;
    /* general error */
    public static final int ERR_INVALID_ARGUMENT = -30;
    /* syntax error */
    public static final int ERR_END_PATTERN_AT_LEFT_BRACE = -100;
    public static final int ERR_END_PATTERN_AT_LEFT_BRACKET = -101;
    public static final int ERR_EMPTY_CHAR_CLASS = -102;
    public static final int ERR_PREMATURE_END_OF_CHAR_CLASS = -103;
    public static final int ERR_END_PATTERN_AT_ESCAPE = -104;
    public static final int ERR_END_PATTERN_AT_META = -105;
    public static final int ERR_END_PATTERN_AT_CONTROL = -106;
    public static final int ERR_META_CODE_SYNTAX = -108;
    public static final int ERR_CONTROL_CODE_SYNTAX = -109;
    public static final int ERR_CHAR_CLASS_VALUE_AT_END_OF_RANGE = -110;
    public static final int ERR_CHAR_CLASS_VALUE_AT_START_OF_RANGE = -111;
    public static final int ERR_UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS = -112;
    public static final int ERR_TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED = -113;
    public static final int ERR_TARGET_OF_REPEAT_OPERATOR_INVALID = -114;
    public static final int ERR_NESTED_REPEAT_OPERATOR = -115;
    public static final int ERR_UNMATCHED_CLOSE_PARENTHESIS = -116;
    public static final int ERR_END_PATTERN_WITH_UNMATCHED_PARENTHESIS = -117;
    public static final int ERR_END_PATTERN_IN_GROUP = -118;
    public static final int ERR_UNDEFINED_GROUP_OPTION = -119;
    public static final int ERR_INVALID_POSIX_BRACKET_TYPE = -121;
    public static final int ERR_INVALID_LOOK_BEHIND_PATTERN = -122;
    public static final int ERR_INVALID_REPEAT_RANGE_PATTERN = -123;
    public static final int ERR_INVALID_CONDITION_PATTERN = -124;
    /* values error (syntax error) */
    public static final int ERR_TOO_BIG_NUMBER = -200;
    public static final int ERR_TOO_BIG_NUMBER_FOR_REPEAT_RANGE = -201;
    public static final int ERR_UPPER_SMALLER_THAN_LOWER_IN_REPEAT_RANGE = -202;
    public static final int ERR_EMPTY_RANGE_IN_CHAR_CLASS = -203;
    public static final int ERR_MISMATCH_CODE_LENGTH_IN_CLASS_RANGE = -204;
    public static final int ERR_TOO_MANY_MULTI_BYTE_RANGES = -205;
    public static final int ERR_TOO_SHORT_MULTI_BYTE_STRING = -206;
    public static final int ERR_TOO_BIG_BACKREF_NUMBER = -207;
    public static final int ERR_INVALID_BACKREF = -208;
    public static final int ERR_NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED = -209;
    public static final int ERR_TOO_SHORT_DIGITS = -210;
    public static final int ERR_TOO_LONG_WIDE_CHAR_VALUE = -212;
    public static final int ERR_EMPTY_GROUP_NAME = -214;
    public static final int ERR_INVALID_GROUP_NAME = -215;
    public static final int ERR_INVALID_CHAR_IN_GROUP_NAME = -216;
    public static final int ERR_UNDEFINED_NAME_REFERENCE = -217;
    public static final int ERR_UNDEFINED_GROUP_REFERENCE = -218;
    public static final int ERR_MULTIPLEX_DEFINED_NAME = -219;
    public static final int ERR_MULTIPLEX_DEFINITION_NAME_CALL = -220;
    public static final int ERR_NEVER_ENDING_RECURSION = -221;
    public static final int ERR_GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY = -222;
    public static final int ERR_INVALID_CHAR_PROPERTY_NAME = -223;
    public static final int ERR_TOO_MANY_CAPTURE_GROUPS = -224;
    public static final int ERR_INVALID_CODE_POINT_VALUE = -400;
    public static final int ERR_INVALID_WIDE_CHAR_VALUE = -400;
    public static final int ERR_TOO_BIG_WIDE_CHAR_VALUE = -401;
    public static final int ERR_NOT_SUPPORTED_ENCODING_COMBINATION = -402;
    public static final int ERR_INVALID_COMBINATION_OF_OPTIONS = -403;

    // specific to jcodings
    int ERR_ENCODING_CLASS_DEF_NOT_FOUND = -1000;
    int ERR_ENCODING_LOAD_ERROR = -1001;
    int ERR_ENCODING_ALREADY_REGISTERED = -1002;
    int ERR_ENCODING_ALIAS_ALREADY_REGISTERED = -1003;
    int ERR_ENCODING_REPLICA_ALREADY_REGISTERED = -1004;
    int ERR_NO_SUCH_ENCODNG = -1005;
    int ERR_COULD_NOT_REPLICATE = -1006;
    int ERR_TRANSCODER_ALREADY_REGISTERED = -1007;
    int ERR_TRANSCODER_CLASS_DEF_NOT_FOUND = -1008;
    int ERR_TRANSCODER_LOAD_ERROR = -1009;
}
