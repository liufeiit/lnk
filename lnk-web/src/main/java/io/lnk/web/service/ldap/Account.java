package io.lnk.web.service.ldap;

import java.io.Serializable;

public class Account implements Serializable{
	private static final long serialVersionUID = -3565243085088338686L;
	private String sAMAccountName;
    private String whenCreated;
    private String objectCategory;
    private String badPwdCount;
    private String mDBUseDefaults;
    private String codePage;
    private String mail;
    private String objectGUID;
    private String msExchUserAccountControl;
    private String msExchMailboxSecurityDescriptor;
    private String memberOf;
    private String msExchMailboxGuid;
    private String instanceType;
    private String msExchPoliciesIncluded;
    private String objectSid;
    private String badPasswordTime;
    private String proxyAddresses;
    private String dSCorePropagationData;
    private String objectClass;
    private String msExchWhenMailboxCreated;
    private String name;
    private String description;
    private String sn;
    private String userAccountControl;
    private String primaryGroupID;
    private String lastLogon;
    private String accountExpires;
    private String uSNChanged;
    private String msExchRBACPolicyLink;
    private String cn;
    private String msExchVersion;
    private String msExchTextMessagingState;
    private String logonCount;
    private String msExchHomeServerName;
    private String homeMTA;
    private String sAMAccountType;
    private String msExchRecipientTypeDetails;
    private String legacyExchangeDN;
    private String givenName;
    private String uSNCreated;
    private String displayName;
    private String msExchShadowMailNickname;
    private String pwdLastSet;
    private String userPrincipalName;
    private String whenChanged;
    private String lastLogonTimestamp;
    private String countryCode;
    private String mailNickname;
    private String msExchShadowProxyAddresses;
    private String distinguishedName;
    private String homeMDB;
    private String msExchRecipientDisplayType;
    private String msExchUMDtmfMap;
    private String showInAddressBook;
    private String msExchUserCulture;

	public String getsAMAccountName() {
		return sAMAccountName;
	}

	public void setsAMAccountName(String sAMAccountName) {
		this.sAMAccountName = sAMAccountName;
	}

	public String getWhenCreated() {
		return whenCreated;
	}

	public void setWhenCreated(String whenCreated) {
		this.whenCreated = whenCreated;
	}

	public String getObjectCategory() {
		return objectCategory;
	}

	public void setObjectCategory(String objectCategory) {
		this.objectCategory = objectCategory;
	}

	public String getBadPwdCount() {
		return badPwdCount;
	}

	public void setBadPwdCount(String badPwdCount) {
		this.badPwdCount = badPwdCount;
	}

	public String getmDBUseDefaults() {
		return mDBUseDefaults;
	}

	public void setmDBUseDefaults(String mDBUseDefaults) {
		this.mDBUseDefaults = mDBUseDefaults;
	}

	public String getCodePage() {
		return codePage;
	}

	public void setCodePage(String codePage) {
		this.codePage = codePage;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getObjectGUID() {
		return objectGUID;
	}

	public void setObjectGUID(String objectGUID) {
		this.objectGUID = objectGUID;
	}

	public String getMsExchUserAccountControl() {
		return msExchUserAccountControl;
	}

	public void setMsExchUserAccountControl(String msExchUserAccountControl) {
		this.msExchUserAccountControl = msExchUserAccountControl;
	}

	public String getMsExchMailboxSecurityDescriptor() {
		return msExchMailboxSecurityDescriptor;
	}

	public void setMsExchMailboxSecurityDescriptor(String msExchMailboxSecurityDescriptor) {
		this.msExchMailboxSecurityDescriptor = msExchMailboxSecurityDescriptor;
	}

	public String getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(String memberOf) {
		this.memberOf = memberOf;
	}

	public String getMsExchMailboxGuid() {
		return msExchMailboxGuid;
	}

	public void setMsExchMailboxGuid(String msExchMailboxGuid) {
		this.msExchMailboxGuid = msExchMailboxGuid;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getMsExchPoliciesIncluded() {
		return msExchPoliciesIncluded;
	}

	public void setMsExchPoliciesIncluded(String msExchPoliciesIncluded) {
		this.msExchPoliciesIncluded = msExchPoliciesIncluded;
	}

	public String getObjectSid() {
		return objectSid;
	}

	public void setObjectSid(String objectSid) {
		this.objectSid = objectSid;
	}

	public String getBadPasswordTime() {
		return badPasswordTime;
	}

	public void setBadPasswordTime(String badPasswordTime) {
		this.badPasswordTime = badPasswordTime;
	}

	public String getProxyAddresses() {
		return proxyAddresses;
	}

	public void setProxyAddresses(String proxyAddresses) {
		this.proxyAddresses = proxyAddresses;
	}

	public String getdSCorePropagationData() {
		return dSCorePropagationData;
	}

	public void setdSCorePropagationData(String dSCorePropagationData) {
		this.dSCorePropagationData = dSCorePropagationData;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public String getMsExchWhenMailboxCreated() {
		return msExchWhenMailboxCreated;
	}

	public void setMsExchWhenMailboxCreated(String msExchWhenMailboxCreated) {
		this.msExchWhenMailboxCreated = msExchWhenMailboxCreated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getUserAccountControl() {
		return userAccountControl;
	}

	public void setUserAccountControl(String userAccountControl) {
		this.userAccountControl = userAccountControl;
	}

	public String getPrimaryGroupID() {
		return primaryGroupID;
	}

	public void setPrimaryGroupID(String primaryGroupID) {
		this.primaryGroupID = primaryGroupID;
	}

	public String getLastLogon() {
		return lastLogon;
	}

	public void setLastLogon(String lastLogon) {
		this.lastLogon = lastLogon;
	}

	public String getAccountExpires() {
		return accountExpires;
	}

	public void setAccountExpires(String accountExpires) {
		this.accountExpires = accountExpires;
	}

	public String getuSNChanged() {
		return uSNChanged;
	}

	public void setuSNChanged(String uSNChanged) {
		this.uSNChanged = uSNChanged;
	}

	public String getMsExchRBACPolicyLink() {
		return msExchRBACPolicyLink;
	}

	public void setMsExchRBACPolicyLink(String msExchRBACPolicyLink) {
		this.msExchRBACPolicyLink = msExchRBACPolicyLink;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getMsExchVersion() {
		return msExchVersion;
	}

	public void setMsExchVersion(String msExchVersion) {
		this.msExchVersion = msExchVersion;
	}

	public String getMsExchTextMessagingState() {
		return msExchTextMessagingState;
	}

	public void setMsExchTextMessagingState(String msExchTextMessagingState) {
		this.msExchTextMessagingState = msExchTextMessagingState;
	}

	public String getLogonCount() {
		return logonCount;
	}

	public void setLogonCount(String logonCount) {
		this.logonCount = logonCount;
	}

	public String getMsExchHomeServerName() {
		return msExchHomeServerName;
	}

	public void setMsExchHomeServerName(String msExchHomeServerName) {
		this.msExchHomeServerName = msExchHomeServerName;
	}

	public String getHomeMTA() {
		return homeMTA;
	}

	public void setHomeMTA(String homeMTA) {
		this.homeMTA = homeMTA;
	}

	public String getsAMAccountType() {
		return sAMAccountType;
	}

	public void setsAMAccountType(String sAMAccountType) {
		this.sAMAccountType = sAMAccountType;
	}

	public String getMsExchRecipientTypeDetails() {
		return msExchRecipientTypeDetails;
	}

	public void setMsExchRecipientTypeDetails(String msExchRecipientTypeDetails) {
		this.msExchRecipientTypeDetails = msExchRecipientTypeDetails;
	}

	public String getLegacyExchangeDN() {
		return legacyExchangeDN;
	}

	public void setLegacyExchangeDN(String legacyExchangeDN) {
		this.legacyExchangeDN = legacyExchangeDN;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getuSNCreated() {
		return uSNCreated;
	}

	public void setuSNCreated(String uSNCreated) {
		this.uSNCreated = uSNCreated;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getMsExchShadowMailNickname() {
		return msExchShadowMailNickname;
	}

	public void setMsExchShadowMailNickname(String msExchShadowMailNickname) {
		this.msExchShadowMailNickname = msExchShadowMailNickname;
	}

	public String getPwdLastSet() {
		return pwdLastSet;
	}

	public void setPwdLastSet(String pwdLastSet) {
		this.pwdLastSet = pwdLastSet;
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	public String getWhenChanged() {
		return whenChanged;
	}

	public void setWhenChanged(String whenChanged) {
		this.whenChanged = whenChanged;
	}

	public String getLastLogonTimestamp() {
		return lastLogonTimestamp;
	}

	public void setLastLogonTimestamp(String lastLogonTimestamp) {
		this.lastLogonTimestamp = lastLogonTimestamp;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getMailNickname() {
		return mailNickname;
	}

	public void setMailNickname(String mailNickname) {
		this.mailNickname = mailNickname;
	}

	public String getMsExchShadowProxyAddresses() {
		return msExchShadowProxyAddresses;
	}

	public void setMsExchShadowProxyAddresses(String msExchShadowProxyAddresses) {
		this.msExchShadowProxyAddresses = msExchShadowProxyAddresses;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public String getHomeMDB() {
		return homeMDB;
	}

	public void setHomeMDB(String homeMDB) {
		this.homeMDB = homeMDB;
	}

	public String getMsExchRecipientDisplayType() {
		return msExchRecipientDisplayType;
	}

	public void setMsExchRecipientDisplayType(String msExchRecipientDisplayType) {
		this.msExchRecipientDisplayType = msExchRecipientDisplayType;
	}

	public String getMsExchUMDtmfMap() {
		return msExchUMDtmfMap;
	}

	public void setMsExchUMDtmfMap(String msExchUMDtmfMap) {
		this.msExchUMDtmfMap = msExchUMDtmfMap;
	}

	public String getShowInAddressBook() {
		return showInAddressBook;
	}

	public void setShowInAddressBook(String showInAddressBook) {
		this.showInAddressBook = showInAddressBook;
	}

	public String getMsExchUserCulture() {
		return msExchUserCulture;
	}

	public void setMsExchUserCulture(String msExchUserCulture) {
		this.msExchUserCulture = msExchUserCulture;
	}
    
    
}
