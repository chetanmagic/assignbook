package com.magic.assignbook;

public class GradePONumber {

	private String grade;
	private String poNumber;
	private String tenantId;

	public GradePONumber(String grade, String poNumber, String tenantId) {
		// TODO Auto-generated constructor stub
		super();
		this.grade = grade;
		this.poNumber = poNumber;
		this.tenantId = tenantId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

}
