package afterwork.millionaire.trading.dto;

import lombok.Data;

@Data
public class BaseInput {
    public static final String contentType = "application/json";
    public static String authorization = "Bearer ";
    public static final String appkey = "";
    public static final String appsecret = "";

    public static final String CANO = "";
    public static final String ACNT_PRDT_CD = "";

    // 국내
    public static final String CO233740 = "233740";
    public static final String COQ520057 = "Q520057";
    public static String dAmount = "";
    public static String dStatus = "";

    // 해외
    public static String EXCD = "NAS";
    public static String SYMB = "";
    public static String OVRS_ORD_UNPR = "";
    public static String ORD_QTY = "";
    public static String OVRS_EXCG_CD = "NASD";
    public static String oStatus = "";
    public static String oAmount = "";
    public static final String COTQQQ = "TQQQ";
    public static final String COSQQQ = "SQQQ";

}
