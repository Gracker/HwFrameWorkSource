package com.android.server.wifi.hotspot2;

import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.Credential.CertificateCredential;
import android.net.wifi.hotspot2.pps.Credential.SimCredential;
import android.net.wifi.hotspot2.pps.Credential.UserCredential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.net.wifi.hotspot2.pps.Policy;
import android.net.wifi.hotspot2.pps.Policy.RoamingPartner;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import com.android.internal.util.XmlUtils;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PasspointXmlUtils {
    private static final String XML_TAG_ABLE_TO_SHARE = "AbleToShare";
    private static final String XML_TAG_CERT_SHA256_FINGERPRINT = "CertSHA256Fingerprint";
    private static final String XML_TAG_CERT_TYPE = "CertType";
    private static final String XML_TAG_CHECK_AAA_SERVER_CERT_STATUS = "CheckAAAServerCertStatus";
    private static final String XML_TAG_COUNTRIES = "Countries";
    private static final String XML_TAG_CREATION_TIME = "CreationTime";
    private static final String XML_TAG_CREDENTIAL_PRIORITY = "CredentialPriority";
    private static final String XML_TAG_EAP_TYPE = "EAPType";
    private static final String XML_TAG_EXCLUDED_SSID_LIST = "ExcludedSSIDList";
    private static final String XML_TAG_EXPIRATION_TIME = "ExpirationTime";
    private static final String XML_TAG_FQDN = "FQDN";
    private static final String XML_TAG_FQDN_EXACT_MATCH = "FQDNExactMatch";
    private static final String XML_TAG_FRIENDLY_NAME = "FriendlyName";
    private static final String XML_TAG_HOME_NETWORK_IDS = "HomeNetworkIDs";
    private static final String XML_TAG_ICON_URL = "IconURL";
    private static final String XML_TAG_IMSI = "IMSI";
    private static final String XML_TAG_MACHINE_MANAGED = "MachineManaged";
    private static final String XML_TAG_MATCH_ALL_OIS = "MatchAllOIs";
    private static final String XML_TAG_MATCH_ANY_OIS = "MatchAnyOIs";
    private static final String XML_TAG_MAXIMUM_BSS_LOAD_VALUE = "MaximumBSSLoadValue";
    private static final String XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH = "MinHomeDownlinkBandwidth";
    private static final String XML_TAG_MIN_HOME_UPLINK_BANDWIDTH = "MinHomeUplinkBandwidth";
    private static final String XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH = "MinRoamingDownlinkBandwidth";
    private static final String XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH = "MinRoamingUplinkBandwidth";
    private static final String XML_TAG_NON_EAP_INNER_METHOD = "NonEAPInnerMethod";
    private static final String XML_TAG_OTHER_HOME_PARTNERS = "OtherHomePartners";
    private static final String XML_TAG_PASSWORD = "Password";
    private static final String XML_TAG_PORTS = "Ports";
    private static final String XML_TAG_PRIORITY = "Priority";
    private static final String XML_TAG_PROTO = "Proto";
    private static final String XML_TAG_REALM = "Realm";
    private static final String XML_TAG_RESTRICTION = "Restriction";
    private static final String XML_TAG_ROAMING_CONSORTIUM_OIS = "RoamingConsortiumOIs";
    private static final String XML_TAG_SECTION_HEADER_CERT_CREDENTIAL = "CertCredential";
    private static final String XML_TAG_SECTION_HEADER_CREDENTIAL = "Credential";
    private static final String XML_TAG_SECTION_HEADER_HOMESP = "HomeSP";
    private static final String XML_TAG_SECTION_HEADER_POLICY = "Policy";
    private static final String XML_TAG_SECTION_HEADER_POLICY_UPDATE = "PolicyUpdate";
    private static final String XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST = "RoamingPartnerList";
    private static final String XML_TAG_SECTION_HEADER_PROTO_PORT = "ProtoPort";
    private static final String XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP = "RequiredProtoPortMap";
    private static final String XML_TAG_SECTION_HEADER_ROAMING_PARTNER = "RoamingPartner";
    private static final String XML_TAG_SECTION_HEADER_SIM_CREDENTIAL = "SimCredential";
    private static final String XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE = "SubscriptionUpdate";
    private static final String XML_TAG_SECTION_HEADER_USER_CREDENTIAL = "UserCredential";
    private static final String XML_TAG_SERVER_URI = "ServerURI";
    private static final String XML_TAG_SOFT_TOKEN_APP = "SoftTokenApp";
    private static final String XML_TAG_SUBSCRIPTION_CREATION_TIME = "SubscriptionCreationTime";
    private static final String XML_TAG_SUBSCRIPTION_EXPIRATION_TIME = "SubscriptionExpirationTime";
    private static final String XML_TAG_SUBSCRIPTION_TYPE = "SubscriptionType";
    private static final String XML_TAG_TRUST_ROOT_CERT_LIST = "TrustRootCertList";
    private static final String XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT = "TrustRootCertSHA256Fingerprint";
    private static final String XML_TAG_TRUST_ROOT_CERT_URL = "TrustRootCertURL";
    private static final String XML_TAG_UPDATE_IDENTIFIER = "UpdateIdentifier";
    private static final String XML_TAG_UPDATE_INTERVAL = "UpdateInterval";
    private static final String XML_TAG_UPDATE_METHOD = "UpdateMethod";
    private static final String XML_TAG_USAGE_LIMIT_DATA_LIMIT = "UsageLimitDataLimit";
    private static final String XML_TAG_USAGE_LIMIT_START_TIME = "UsageLimitStartTime";
    private static final String XML_TAG_USAGE_LIMIT_TIME_LIMIT = "UsageLimitTimeLimit";
    private static final String XML_TAG_USAGE_LIMIT_TIME_PERIOD = "UsageLimitTimePeriod";
    private static final String XML_TAG_USERNAME = "Username";

    public static void serializePasspointConfiguration(XmlSerializer out, PasspointConfiguration config) throws XmlPullParserException, IOException {
        XmlUtil.writeNextValue(out, XML_TAG_UPDATE_IDENTIFIER, Integer.valueOf(config.getUpdateIdentifier()));
        XmlUtil.writeNextValue(out, XML_TAG_CREDENTIAL_PRIORITY, Integer.valueOf(config.getCredentialPriority()));
        XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_LIST, config.getTrustRootCertList());
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_CREATION_TIME, Long.valueOf(config.getSubscriptionCreationTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_EXPIRATION_TIME, Long.valueOf(config.getSubscriptionExpirationTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_SUBSCRIPTION_TYPE, config.getSubscriptionType());
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_TIME_PERIOD, Long.valueOf(config.getUsageLimitUsageTimePeriodInMinutes()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_START_TIME, Long.valueOf(config.getUsageLimitStartTimeInMillis()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_DATA_LIMIT, Long.valueOf(config.getUsageLimitDataLimit()));
        XmlUtil.writeNextValue(out, XML_TAG_USAGE_LIMIT_TIME_LIMIT, Long.valueOf(config.getUsageLimitTimeLimitInMinutes()));
        serializeHomeSp(out, config.getHomeSp());
        serializeCredential(out, config.getCredential());
        serializePolicy(out, config.getPolicy());
        serializeUpdateParameter(out, XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE, config.getSubscriptionUpdate());
    }

    /* JADX WARNING: Removed duplicated region for block: B:82:0x015c A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x015c A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x015c A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0177  */
    /* JADX WARNING: Missing block: B:26:0x006b, code:
            if (r8.equals(XML_TAG_SUBSCRIPTION_CREATION_TIME) != false) goto L_0x008e;
     */
    /* JADX WARNING: Missing block: B:63:0x0141, code:
            if (r1.equals(XML_TAG_SECTION_HEADER_SUBSCRIPTION_UPDATE) != false) goto L_0x0159;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PasspointConfiguration deserializePasspointConfiguration(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        PasspointConfiguration config = new PasspointConfiguration();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            int i = 3;
            if (!isValueElement(in)) {
                String name = in.getName();
                int hashCode = name.hashCode();
                if (hashCode == -2127810660) {
                    if (name.equals(XML_TAG_SECTION_HEADER_HOMESP)) {
                        i = 0;
                        switch (i) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            default:
                                break;
                        }
                    }
                } else if (hashCode == -1898802862) {
                    if (name.equals(XML_TAG_SECTION_HEADER_POLICY)) {
                        i = 2;
                        switch (i) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            default:
                                break;
                        }
                    }
                } else if (hashCode != 162345062) {
                    if (hashCode == 1310049399 && name.equals(XML_TAG_SECTION_HEADER_CREDENTIAL)) {
                        i = 1;
                        switch (i) {
                            case 0:
                                config.setHomeSp(deserializeHomeSP(in, outerTagDepth + 1));
                                break;
                            case 1:
                                config.setCredential(deserializeCredential(in, outerTagDepth + 1));
                                break;
                            case 2:
                                config.setPolicy(deserializePolicy(in, outerTagDepth + 1));
                                break;
                            case 3:
                                config.setSubscriptionUpdate(deserializeUpdateParameter(in, outerTagDepth + 1));
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown section under PasspointConfiguration: ");
                                stringBuilder.append(in.getName());
                                throw new XmlPullParserException(stringBuilder.toString());
                        }
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
            }
            String[] name2 = new String[1];
            Object value = XmlUtil.readCurrentValue(in, name2);
            String str = name2[0];
            switch (str.hashCode()) {
                case -1662505799:
                    if (str.equals(XML_TAG_SUBSCRIPTION_EXPIRATION_TIME)) {
                        i = 4;
                        break;
                    }
                case -1463836521:
                    if (str.equals(XML_TAG_USAGE_LIMIT_DATA_LIMIT)) {
                        i = 8;
                        break;
                    }
                case -1358509545:
                    if (str.equals(XML_TAG_SUBSCRIPTION_TYPE)) {
                        i = 5;
                        break;
                    }
                case -1114457047:
                    break;
                case -1063797932:
                    if (str.equals(XML_TAG_USAGE_LIMIT_TIME_LIMIT)) {
                        i = 9;
                        break;
                    }
                case 550067826:
                    if (str.equals(XML_TAG_UPDATE_IDENTIFIER)) {
                        i = 0;
                        break;
                    }
                case 1083081909:
                    if (str.equals(XML_TAG_USAGE_LIMIT_START_TIME)) {
                        i = 7;
                        break;
                    }
                case 1492973896:
                    if (str.equals(XML_TAG_USAGE_LIMIT_TIME_PERIOD)) {
                        i = 6;
                        break;
                    }
                case 2017737531:
                    if (str.equals(XML_TAG_CREDENTIAL_PRIORITY)) {
                        i = 1;
                        break;
                    }
                case 2125983292:
                    if (str.equals(XML_TAG_TRUST_ROOT_CERT_LIST)) {
                        i = 2;
                        break;
                    }
                default:
                    i = -1;
                    break;
            }
            switch (i) {
                case 0:
                    config.setUpdateIdentifier(((Integer) value).intValue());
                    break;
                case 1:
                    config.setCredentialPriority(((Integer) value).intValue());
                    break;
                case 2:
                    config.setTrustRootCertList((Map) value);
                    break;
                case 3:
                    config.setSubscriptionCreationTimeInMillis(((Long) value).longValue());
                    break;
                case 4:
                    config.setSubscriptionExpirationTimeInMillis(((Long) value).longValue());
                    break;
                case 5:
                    config.setSubscriptionType((String) value);
                    break;
                case 6:
                    config.setUsageLimitUsageTimePeriodInMinutes(((Long) value).longValue());
                    break;
                case 7:
                    config.setUsageLimitStartTimeInMillis(((Long) value).longValue());
                    break;
                case 8:
                    config.setUsageLimitDataLimit(((Long) value).longValue());
                    break;
                case 9:
                    config.setUsageLimitTimeLimitInMinutes(((Long) value).longValue());
                    break;
                default:
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Unknown value under PasspointConfiguration: ");
                    stringBuilder2.append(in.getName());
                    throw new XmlPullParserException(stringBuilder2.toString());
            }
        }
        return config;
    }

    private static void serializeHomeSp(XmlSerializer out, HomeSp homeSp) throws XmlPullParserException, IOException {
        if (homeSp != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_HOMESP);
            XmlUtil.writeNextValue(out, "FQDN", homeSp.getFqdn());
            XmlUtil.writeNextValue(out, XML_TAG_FRIENDLY_NAME, homeSp.getFriendlyName());
            XmlUtil.writeNextValue(out, XML_TAG_ICON_URL, homeSp.getIconUrl());
            XmlUtil.writeNextValue(out, XML_TAG_HOME_NETWORK_IDS, homeSp.getHomeNetworkIds());
            XmlUtil.writeNextValue(out, XML_TAG_MATCH_ALL_OIS, homeSp.getMatchAllOis());
            XmlUtil.writeNextValue(out, XML_TAG_MATCH_ANY_OIS, homeSp.getMatchAnyOis());
            XmlUtil.writeNextValue(out, XML_TAG_OTHER_HOME_PARTNERS, homeSp.getOtherHomePartners());
            XmlUtil.writeNextValue(out, "RoamingConsortiumOIs", homeSp.getRoamingConsortiumOis());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_HOMESP);
        }
    }

    private static void serializeCredential(XmlSerializer out, Credential credential) throws XmlPullParserException, IOException {
        if (credential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_CREDENTIAL);
            XmlUtil.writeNextValue(out, "CreationTime", Long.valueOf(credential.getCreationTimeInMillis()));
            XmlUtil.writeNextValue(out, XML_TAG_EXPIRATION_TIME, Long.valueOf(credential.getExpirationTimeInMillis()));
            XmlUtil.writeNextValue(out, "Realm", credential.getRealm());
            XmlUtil.writeNextValue(out, XML_TAG_CHECK_AAA_SERVER_CERT_STATUS, Boolean.valueOf(credential.getCheckAaaServerCertStatus()));
            serializeUserCredential(out, credential.getUserCredential());
            serializeCertCredential(out, credential.getCertCredential());
            serializeSimCredential(out, credential.getSimCredential());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_CREDENTIAL);
        }
    }

    private static void serializePolicy(XmlSerializer out, Policy policy) throws XmlPullParserException, IOException {
        if (policy != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_POLICY);
            XmlUtil.writeNextValue(out, XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH, Long.valueOf(policy.getMinHomeDownlinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_HOME_UPLINK_BANDWIDTH, Long.valueOf(policy.getMinHomeUplinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH, Long.valueOf(policy.getMinRoamingDownlinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH, Long.valueOf(policy.getMinRoamingUplinkBandwidth()));
            XmlUtil.writeNextValue(out, XML_TAG_EXCLUDED_SSID_LIST, policy.getExcludedSsidList());
            XmlUtil.writeNextValue(out, XML_TAG_MAXIMUM_BSS_LOAD_VALUE, Integer.valueOf(policy.getMaximumBssLoadValue()));
            serializeProtoPortMap(out, policy.getRequiredProtoPortMap());
            serializeUpdateParameter(out, XML_TAG_SECTION_HEADER_POLICY_UPDATE, policy.getPolicyUpdate());
            serializePreferredRoamingPartnerList(out, policy.getPreferredRoamingPartnerList());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_POLICY);
        }
    }

    private static void serializeUserCredential(XmlSerializer out, UserCredential userCredential) throws XmlPullParserException, IOException {
        if (userCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_USER_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_USERNAME, userCredential.getUsername());
            XmlUtil.writeNextValue(out, "Password", userCredential.getPassword());
            XmlUtil.writeNextValue(out, XML_TAG_MACHINE_MANAGED, Boolean.valueOf(userCredential.getMachineManaged()));
            XmlUtil.writeNextValue(out, XML_TAG_SOFT_TOKEN_APP, userCredential.getSoftTokenApp());
            XmlUtil.writeNextValue(out, XML_TAG_ABLE_TO_SHARE, Boolean.valueOf(userCredential.getAbleToShare()));
            XmlUtil.writeNextValue(out, XML_TAG_EAP_TYPE, Integer.valueOf(userCredential.getEapType()));
            XmlUtil.writeNextValue(out, XML_TAG_NON_EAP_INNER_METHOD, userCredential.getNonEapInnerMethod());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_USER_CREDENTIAL);
        }
    }

    private static void serializeCertCredential(XmlSerializer out, CertificateCredential certCredential) throws XmlPullParserException, IOException {
        if (certCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_CERT_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_CERT_TYPE, certCredential.getCertType());
            XmlUtil.writeNextValue(out, XML_TAG_CERT_SHA256_FINGERPRINT, certCredential.getCertSha256Fingerprint());
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_CERT_CREDENTIAL);
        }
    }

    private static void serializeSimCredential(XmlSerializer out, SimCredential simCredential) throws XmlPullParserException, IOException {
        if (simCredential != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_SIM_CREDENTIAL);
            XmlUtil.writeNextValue(out, XML_TAG_IMSI, simCredential.getImsi());
            XmlUtil.writeNextValue(out, XML_TAG_EAP_TYPE, Integer.valueOf(simCredential.getEapType()));
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_SIM_CREDENTIAL);
        }
    }

    private static void serializePreferredRoamingPartnerList(XmlSerializer out, List<RoamingPartner> preferredRoamingPartnerList) throws XmlPullParserException, IOException {
        if (preferredRoamingPartnerList != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST);
            for (RoamingPartner partner : preferredRoamingPartnerList) {
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_ROAMING_PARTNER);
                XmlUtil.writeNextValue(out, "FQDN", partner.getFqdn());
                XmlUtil.writeNextValue(out, XML_TAG_FQDN_EXACT_MATCH, Boolean.valueOf(partner.getFqdnExactMatch()));
                XmlUtil.writeNextValue(out, "Priority", Integer.valueOf(partner.getPriority()));
                XmlUtil.writeNextValue(out, XML_TAG_COUNTRIES, partner.getCountries());
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_ROAMING_PARTNER);
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST);
        }
    }

    private static void serializeUpdateParameter(XmlSerializer out, String type, UpdateParameter param) throws XmlPullParserException, IOException {
        if (param != null) {
            XmlUtil.writeNextSectionStart(out, type);
            XmlUtil.writeNextValue(out, XML_TAG_UPDATE_INTERVAL, Long.valueOf(param.getUpdateIntervalInMinutes()));
            XmlUtil.writeNextValue(out, XML_TAG_UPDATE_METHOD, param.getUpdateMethod());
            XmlUtil.writeNextValue(out, XML_TAG_RESTRICTION, param.getRestriction());
            XmlUtil.writeNextValue(out, XML_TAG_SERVER_URI, param.getServerUri());
            XmlUtil.writeNextValue(out, XML_TAG_USERNAME, param.getUsername());
            XmlUtil.writeNextValue(out, "Password", param.getBase64EncodedPassword());
            XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_URL, param.getTrustRootCertUrl());
            XmlUtil.writeNextValue(out, XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT, param.getTrustRootCertSha256Fingerprint());
            XmlUtil.writeNextSectionEnd(out, type);
        }
    }

    private static void serializeProtoPortMap(XmlSerializer out, Map<Integer, String> protoPortMap) throws XmlPullParserException, IOException {
        if (protoPortMap != null) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP);
            for (Entry<Integer, String> entry : protoPortMap.entrySet()) {
                XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_PROTO_PORT);
                XmlUtil.writeNextValue(out, XML_TAG_PROTO, entry.getKey());
                XmlUtil.writeNextValue(out, XML_TAG_PORTS, entry.getValue());
                XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_PROTO_PORT);
            }
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP);
        }
    }

    /* JADX WARNING: Missing block: B:14:0x003c, code:
            if (r5.equals(XML_TAG_FRIENDLY_NAME) != false) goto L_0x0072;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HomeSp deserializeHomeSP(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        HomeSp homeSp = new HomeSp();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                switch (str.hashCode()) {
                    case -1504997731:
                        if (str.equals(XML_TAG_MATCH_ALL_OIS)) {
                            i = 4;
                            break;
                        }
                    case -1502763406:
                        if (str.equals(XML_TAG_MATCH_ANY_OIS)) {
                            i = 5;
                            break;
                        }
                    case -991549930:
                        if (str.equals(XML_TAG_ICON_URL)) {
                            i = 2;
                            break;
                        }
                    case -346924001:
                        if (str.equals("RoamingConsortiumOIs")) {
                            i = 6;
                            break;
                        }
                    case 2165397:
                        if (str.equals("FQDN")) {
                            i = 0;
                            break;
                        }
                    case 626253302:
                        break;
                    case 1348385833:
                        if (str.equals(XML_TAG_HOME_NETWORK_IDS)) {
                            i = 3;
                            break;
                        }
                    case 1956561338:
                        if (str.equals(XML_TAG_OTHER_HOME_PARTNERS)) {
                            i = 7;
                            break;
                        }
                    default:
                        i = -1;
                        break;
                }
                switch (i) {
                    case 0:
                        homeSp.setFqdn((String) value);
                        break;
                    case 1:
                        homeSp.setFriendlyName((String) value);
                        break;
                    case 2:
                        homeSp.setIconUrl((String) value);
                        break;
                    case 3:
                        homeSp.setHomeNetworkIds((Map) value);
                        break;
                    case 4:
                        homeSp.setMatchAllOis((long[]) value);
                        break;
                    case 5:
                        homeSp.setMatchAnyOis((long[]) value);
                        break;
                    case 6:
                        homeSp.setRoamingConsortiumOis((long[]) value);
                        break;
                    case 7:
                        homeSp.setOtherHomePartners((String[]) value);
                        break;
                    default:
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Unknown data under HomeSP: ");
                        stringBuilder.append(valueName[0]);
                        throw new XmlPullParserException(stringBuilder.toString());
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return homeSp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0061 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0061 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0061 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00dd A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00dd A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f8  */
    /* JADX WARNING: Missing block: B:20:0x0050, code:
            if (r7.equals("Realm") != false) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:43:0x00cc, code:
            if (r1.equals(XML_TAG_SECTION_HEADER_SIM_CREDENTIAL) != false) goto L_0x00da;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Credential deserializeCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Credential credential = new Credential();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            int i = 2;
            if (!isValueElement(in)) {
                String name = in.getName();
                int hashCode = name.hashCode();
                if (hashCode == -930907486) {
                    if (name.equals(XML_TAG_SECTION_HEADER_USER_CREDENTIAL)) {
                        i = 0;
                        switch (i) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            default:
                                break;
                        }
                    }
                } else if (hashCode != 802017390) {
                    if (hashCode == 1771027899 && name.equals(XML_TAG_SECTION_HEADER_CERT_CREDENTIAL)) {
                        i = 1;
                        switch (i) {
                            case 0:
                                credential.setUserCredential(deserializeUserCredential(in, outerTagDepth + 1));
                                break;
                            case 1:
                                credential.setCertCredential(deserializeCertCredential(in, outerTagDepth + 1));
                                break;
                            case 2:
                                credential.setSimCredential(deserializeSimCredential(in, outerTagDepth + 1));
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown section under Credential: ");
                                stringBuilder.append(in.getName());
                                throw new XmlPullParserException(stringBuilder.toString());
                        }
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
            String[] name2 = new String[1];
            Object value = XmlUtil.readCurrentValue(in, name2);
            String str = name2[0];
            int hashCode2 = str.hashCode();
            if (hashCode2 == -1670320580) {
                if (str.equals(XML_TAG_EXPIRATION_TIME)) {
                    i = 1;
                    switch (i) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        default:
                            break;
                    }
                }
            } else if (hashCode2 != 78834287) {
                if (hashCode2 == 646045490) {
                    if (str.equals(XML_TAG_CHECK_AAA_SERVER_CERT_STATUS)) {
                        i = 3;
                        switch (i) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            default:
                                break;
                        }
                    }
                } else if (hashCode2 == 1750336108 && str.equals("CreationTime")) {
                    i = 0;
                    switch (i) {
                        case 0:
                            credential.setCreationTimeInMillis(((Long) value).longValue());
                            break;
                        case 1:
                            credential.setExpirationTimeInMillis(((Long) value).longValue());
                            break;
                        case 2:
                            credential.setRealm((String) value);
                            break;
                        case 3:
                            credential.setCheckAaaServerCertStatus(((Boolean) value).booleanValue());
                            break;
                        default:
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Unknown value under Credential: ");
                            stringBuilder2.append(name2[0]);
                            throw new XmlPullParserException(stringBuilder2.toString());
                    }
                }
            }
            i = -1;
            switch (i) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:
                    break;
            }
        }
        return credential;
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x00de A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00de A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f9  */
    /* JADX WARNING: Missing block: B:11:0x0035, code:
            if (r7.equals(XML_TAG_MIN_ROAMING_DOWNLINK_BANDWIDTH) != false) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:46:0x00d7, code:
            if (r1.equals(XML_TAG_SECTION_HEADER_PREFERRED_ROAMING_PARTNER_LIST) != false) goto L_0x00db;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Policy deserializePolicy(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Policy policy = new Policy();
        while (XmlUtils.nextElementWithin(in, outerTagDepth)) {
            int i = 2;
            if (!isValueElement(in)) {
                String name = in.getName();
                int hashCode = name.hashCode();
                if (hashCode != -2125460531) {
                    if (hashCode == -1710886725) {
                        if (name.equals(XML_TAG_SECTION_HEADER_POLICY_UPDATE)) {
                            i = 1;
                            switch (i) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (hashCode == -285225102 && name.equals(XML_TAG_SECTION_HEADER_REQUIRED_PROTO_PORT_MAP)) {
                        i = 0;
                        switch (i) {
                            case 0:
                                policy.setRequiredProtoPortMap(deserializeProtoPortMap(in, outerTagDepth + 1));
                                break;
                            case 1:
                                policy.setPolicyUpdate(deserializeUpdateParameter(in, outerTagDepth + 1));
                                break;
                            case 2:
                                policy.setPreferredRoamingPartnerList(deserializePreferredRoamingPartnerList(in, outerTagDepth + 1));
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown section under Policy: ");
                                stringBuilder.append(in.getName());
                                throw new XmlPullParserException(stringBuilder.toString());
                        }
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
            String[] name2 = new String[1];
            Object value = XmlUtil.readCurrentValue(in, name2);
            String str = name2[0];
            switch (str.hashCode()) {
                case -166875607:
                    if (str.equals(XML_TAG_MAXIMUM_BSS_LOAD_VALUE)) {
                        i = 5;
                        break;
                    }
                case 228300356:
                    if (str.equals(XML_TAG_MIN_HOME_DOWNLINK_BANDWIDTH)) {
                        i = 0;
                        break;
                    }
                case 399544491:
                    if (str.equals(XML_TAG_MIN_HOME_UPLINK_BANDWIDTH)) {
                        i = 1;
                        break;
                    }
                case 831300259:
                    if (str.equals(XML_TAG_EXCLUDED_SSID_LIST)) {
                        i = 4;
                        break;
                    }
                case 1768378798:
                    break;
                case 1934106261:
                    if (str.equals(XML_TAG_MIN_ROAMING_UPLINK_BANDWIDTH)) {
                        i = 3;
                        break;
                    }
                default:
                    i = -1;
                    break;
            }
            switch (i) {
                case 0:
                    policy.setMinHomeDownlinkBandwidth(((Long) value).longValue());
                    break;
                case 1:
                    policy.setMinHomeUplinkBandwidth(((Long) value).longValue());
                    break;
                case 2:
                    policy.setMinRoamingDownlinkBandwidth(((Long) value).longValue());
                    break;
                case 3:
                    policy.setMinRoamingUplinkBandwidth(((Long) value).longValue());
                    break;
                case 4:
                    policy.setExcludedSsidList((String[]) value);
                    break;
                case 5:
                    policy.setMaximumBssLoadValue(((Integer) value).intValue());
                    break;
            }
        }
        return policy;
    }

    /* JADX WARNING: Missing block: B:11:0x0032, code:
            if (r5.equals("Password") != false) goto L_0x0068;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static UserCredential deserializeUserCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        UserCredential userCredential = new UserCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                switch (str.hashCode()) {
                    case -1249356658:
                        if (str.equals(XML_TAG_EAP_TYPE)) {
                            i = 5;
                            break;
                        }
                    case -201069322:
                        if (str.equals(XML_TAG_USERNAME)) {
                            i = 0;
                            break;
                        }
                    case -123996342:
                        if (str.equals(XML_TAG_ABLE_TO_SHARE)) {
                            i = 4;
                            break;
                        }
                    case 193808112:
                        if (str.equals(XML_TAG_NON_EAP_INNER_METHOD)) {
                            i = 6;
                            break;
                        }
                    case 1045832056:
                        if (str.equals(XML_TAG_MACHINE_MANAGED)) {
                            i = 2;
                            break;
                        }
                    case 1281629883:
                        break;
                    case 1410776018:
                        if (str.equals(XML_TAG_SOFT_TOKEN_APP)) {
                            i = 3;
                            break;
                        }
                    default:
                        i = -1;
                        break;
                }
                switch (i) {
                    case 0:
                        userCredential.setUsername((String) value);
                        break;
                    case 1:
                        userCredential.setPassword((String) value);
                        break;
                    case 2:
                        userCredential.setMachineManaged(((Boolean) value).booleanValue());
                        break;
                    case 3:
                        userCredential.setSoftTokenApp((String) value);
                        break;
                    case 4:
                        userCredential.setAbleToShare(((Boolean) value).booleanValue());
                        break;
                    case 5:
                        userCredential.setEapType(((Integer) value).intValue());
                        break;
                    case 6:
                        userCredential.setNonEapInnerMethod((String) value);
                        break;
                    default:
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Unknown value under UserCredential: ");
                        stringBuilder.append(valueName[0]);
                        throw new XmlPullParserException(stringBuilder.toString());
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return userCredential;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0040 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Missing block: B:10:0x002f, code:
            if (r5.equals(XML_TAG_CERT_SHA256_FINGERPRINT) == false) goto L_0x003c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static CertificateCredential deserializeCertCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        CertificateCredential certCredential = new CertificateCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                int hashCode = str.hashCode();
                if (hashCode != -673759330) {
                    if (hashCode == -285451687) {
                    }
                } else if (str.equals(XML_TAG_CERT_TYPE)) {
                    i = 0;
                    switch (i) {
                        case 0:
                            certCredential.setCertType((String) value);
                            break;
                        case 1:
                            certCredential.setCertSha256Fingerprint((byte[]) value);
                            break;
                        default:
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Unknown value under CertCredential: ");
                            stringBuilder.append(valueName[0]);
                            throw new XmlPullParserException(stringBuilder.toString());
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return certCredential;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0040 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Missing block: B:13:0x0039, code:
            if (r5.equals(XML_TAG_EAP_TYPE) != false) goto L_0x003d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static SimCredential deserializeSimCredential(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        SimCredential simCredential = new SimCredential();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                int hashCode = str.hashCode();
                if (hashCode != -1249356658) {
                    if (hashCode == 2251386 && str.equals(XML_TAG_IMSI)) {
                        i = 0;
                        switch (i) {
                            case 0:
                                simCredential.setImsi((String) value);
                                break;
                            case 1:
                                simCredential.setEapType(((Integer) value).intValue());
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown value under CertCredential: ");
                                stringBuilder.append(valueName[0]);
                                throw new XmlPullParserException(stringBuilder.toString());
                        }
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return simCredential;
    }

    private static List<RoamingPartner> deserializePreferredRoamingPartnerList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<RoamingPartner> roamingPartnerList = new ArrayList();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_ROAMING_PARTNER, outerTagDepth)) {
            roamingPartnerList.add(deserializeRoamingPartner(in, outerTagDepth + 1));
        }
        return roamingPartnerList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x005e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x005e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x005e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0077  */
    /* JADX WARNING: Missing block: B:23:0x0057, code:
            if (r5.equals(XML_TAG_FQDN_EXACT_MATCH) != false) goto L_0x005b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static RoamingPartner deserializeRoamingPartner(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        RoamingPartner partner = new RoamingPartner();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                int hashCode = str.hashCode();
                if (hashCode != -1768941957) {
                    if (hashCode == -1100816956) {
                        if (str.equals("Priority")) {
                            i = 2;
                            switch (i) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (hashCode == -938362220) {
                        if (str.equals(XML_TAG_COUNTRIES)) {
                            i = 3;
                            switch (i) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (hashCode == 2165397 && str.equals("FQDN")) {
                        i = 0;
                        switch (i) {
                            case 0:
                                partner.setFqdn((String) value);
                                break;
                            case 1:
                                partner.setFqdnExactMatch(((Boolean) value).booleanValue());
                                break;
                            case 2:
                                partner.setPriority(((Integer) value).intValue());
                                break;
                            case 3:
                                partner.setCountries((String) value);
                                break;
                            default:
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Unknown value under RoamingPartner: ");
                                stringBuilder.append(valueName[0]);
                                throw new XmlPullParserException(stringBuilder.toString());
                        }
                    }
                }
                i = -1;
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return partner;
    }

    /* JADX WARNING: Missing block: B:29:0x006e, code:
            if (r5.equals(XML_TAG_UPDATE_METHOD) != false) goto L_0x0072;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static UpdateParameter deserializeUpdateParameter(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        UpdateParameter param = new UpdateParameter();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            int i = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                switch (str.hashCode()) {
                    case -961491158:
                        break;
                    case -287007905:
                        if (str.equals(XML_TAG_TRUST_ROOT_CERT_SHA256_FINGERPRINT)) {
                            i = 7;
                            break;
                        }
                    case -201069322:
                        if (str.equals(XML_TAG_USERNAME)) {
                            i = 4;
                            break;
                        }
                    case 106806188:
                        if (str.equals(XML_TAG_RESTRICTION)) {
                            i = 2;
                            break;
                        }
                    case 438596814:
                        if (str.equals(XML_TAG_UPDATE_INTERVAL)) {
                            i = 0;
                            break;
                        }
                    case 1281629883:
                        if (str.equals("Password")) {
                            i = 5;
                            break;
                        }
                    case 1731155985:
                        if (str.equals(XML_TAG_TRUST_ROOT_CERT_URL)) {
                            i = 6;
                            break;
                        }
                    case 1806520073:
                        if (str.equals(XML_TAG_SERVER_URI)) {
                            i = 3;
                            break;
                        }
                    default:
                        i = -1;
                        break;
                }
                switch (i) {
                    case 0:
                        param.setUpdateIntervalInMinutes(((Long) value).longValue());
                        break;
                    case 1:
                        param.setUpdateMethod((String) value);
                        break;
                    case 2:
                        param.setRestriction((String) value);
                        break;
                    case 3:
                        param.setServerUri((String) value);
                        break;
                    case 4:
                        param.setUsername((String) value);
                        break;
                    case 5:
                        param.setBase64EncodedPassword((String) value);
                        break;
                    case 6:
                        param.setTrustRootCertUrl((String) value);
                        break;
                    case 7:
                        param.setTrustRootCertSha256Fingerprint((byte[]) value);
                        break;
                    default:
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Unknown value under UpdateParameter: ");
                        stringBuilder.append(valueName[0]);
                        throw new XmlPullParserException(stringBuilder.toString());
                }
            }
            throw new XmlPullParserException("Missing value name");
        }
        return param;
    }

    private static Map<Integer, String> deserializeProtoPortMap(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        Map<Integer, String> protoPortMap = new HashMap();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_PROTO_PORT, outerTagDepth)) {
            String ports = (String) XmlUtil.readNextValueWithName(in, XML_TAG_PORTS);
            protoPortMap.put(Integer.valueOf(((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_PROTO)).intValue()), ports);
        }
        return protoPortMap;
    }

    private static boolean isValueElement(XmlPullParser in) {
        return in.getAttributeValue(null, "name") != null;
    }
}
