package com.magic.assignbook;

import java.util.List;

public class AssignPODistrictServiceRequest {

	private String ponumber;

	private String userName;

	private boolean emailToUser;

	private List<String> assignedteacherUserNames;

	private List<String> unAssignedTeacherUserNames;

	private Integer licenseCount;

	private String userguid;

	private String usertype;

	private String firstName;

	private List<String> productcode;

	public String getPonumber() {
		return ponumber;
	}

	public void setPonumber(String ponumber) {
		this.ponumber = ponumber;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isEmailToUser() {
		return emailToUser;
	}

	public void setEmailToUser(boolean emailToUser) {
		this.emailToUser = emailToUser;
	}

	public List<String> getAssignedteacherUserNames() {
		return assignedteacherUserNames;
	}

	public void setAssignedteacherUserNames(List<String> assignedteacherUserNames) {
		this.assignedteacherUserNames = assignedteacherUserNames;
	}

	public List<String> getUnAssignedTeacherUserNames() {
		return unAssignedTeacherUserNames;
	}

	public void setUnAssignedTeacherUserNames(List<String> unAssignedTeacherUserNames) {
		this.unAssignedTeacherUserNames = unAssignedTeacherUserNames;
	}

	public Integer getLicenseCount() {
		return licenseCount;
	}

	public void setLicenseCount(Integer licenseCount) {
		this.licenseCount = licenseCount;
	}

	public String getUserguid() {
		return userguid;
	}

	public void setUserguid(String userguid) {
		this.userguid = userguid;
	}

	public String getUsertype() {
		return usertype;
	}

	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public List<String> getProductcode() {
		return productcode;
	}

	public void setProductcode(List<String> productcode) {
		this.productcode = productcode;
	}

}
