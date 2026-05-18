package com.company.ragknowledgebase.constant;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {}

    // API base paths
    public static final String API_V1             = "/api/v1";
    public static final String DOCUMENTS_PATH     = API_V1 + "/documents";
    public static final String CHAT_PATH          = API_V1 + "/chat";
    public static final String SEARCH_PATH        = API_V1 + "/search";

    // Document status
    public static final String STATUS_PROCESSING  = "PROCESSING";
    public static final String STATUS_INDEXED     = "INDEXED";
    public static final String STATUS_FAILED      = "FAILED";

    // Chat roles
    public static final String ROLE_USER          = "USER";
    public static final String ROLE_ASSISTANT     = "ASSISTANT";
    public static final String ROLE_SYSTEM        = "SYSTEM";

    // Defaults
    public static final int    DEFAULT_TOP_K      = 5;
    public static final double DEFAULT_THRESHOLD  = 0.7;
    public static final int    DEFAULT_PAGE_SIZE  = 20;
    public static final int    MAX_PAGE_SIZE      = 100;

    // Pagination param names
    public static final String PAGE_NO            = "pageNo";
    public static final String PAGE_SIZE          = "pageSize";
    public static final String SORT_BY            = "sortBy";
    public static final String SORT_DIR           = "sortDir";
    public static final String DEFAULT_SORT_BY    = "createdAt";
    public static final String DEFAULT_SORT_DIR   = "DESC";
}
