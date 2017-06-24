package com.magic.assignbook;

public class GradePONumber {

	private String courseNumber;
	private String poNumber;
	private String tenantId;

	public GradePONumber(String courseNumber, String poNumber, String tenantId) {
		// TODO Auto-generated constructor stub
		super();
		this.courseNumber = courseNumber;
		this.poNumber = poNumber;
		this.tenantId = tenantId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

}
