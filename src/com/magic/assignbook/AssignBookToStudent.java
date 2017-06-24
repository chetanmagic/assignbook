package com.magic.assignbook;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.magic.utility.CvsUtility;
import com.magic.utility.DataService;

public class AssignBookToStudent {

	public static void main(String[] args) {

		Console c = System.console();
		System.out.println("Enter properties file path: ");
		String n = c.readLine();

		Properties properties = loadproperties(n);

		System.setProperty("log", properties.getProperty("outputlog"));

		Logger logger = Logger.getLogger(AssignBookToStudent.class);

		String fileName = properties.getProperty("po_detail_file");
		logger.info(fileName);
		List<GradePONumber> gradePONumberList = CvsUtility.readCsvFile(fileName);

		String waittimeString = properties.getProperty("waittime");

		Integer waittime = Integer.valueOf(waittimeString);

		ResultSet resultSet = null;
		ResultSet resultSetUserDetails = null;
		ResultSet resultSetSchoolAdmin = null;
		ResultSet resultSetTeacherDetails = null;
		ResultSet resultSetLicenseDetails = null;

		try {
			Connection con = DataService.loadDriver(properties);
			for (GradePONumber gradePONumber : gradePONumberList) {

				resultSet = DataService.getResultSet(
						"select USER_GROUP_ID,GROUP_NAME,SCHOOL_ID,OTHER_SYSTEM_ID from sch_usr_group where COURSE_NUMBER='"
								+ gradePONumber.getCourseNumber() + "' and tenant_id=" + gradePONumber.getTenantId(),
						con);

				if (resultSet != null) {

					while (resultSet.next()) {
						int userGroupId = resultSet.getInt("USER_GROUP_ID");
						String groupName = resultSet.getString("GROUP_NAME");
						int schoolId = resultSet.getInt("SCHOOL_ID");
						String cleverId = resultSet.getString("OTHER_SYSTEM_ID");

						logger.info(
								"Class details: " + userGroupId + "\t" + groupName + "\t" + schoolId + "\t" + cleverId);

						resultSetUserDetails = DataService
								.getResultSet("select  DISTINCT USER_GUID, FIRST_NAME, LAST_NAME,"
										+ " USER_NAME,  usersbyuse3_.OTHER_SYSTEM_ID from sch_usr_group_member this_"
										+ " inner join sch_usr_group schusrgrou1_"
										+ "	on this_.USER_GROUP_ID=schusrgrou1_.USER_GROUP_ID inner join"
										+ "	school_user schooluser2_ on this_.SCHOOL_USER_ID=schooluser2_.SCHOOL_USER_ID"
										+ "	inner join users usersbyuse3_ on schooluser2_.USER_ID=usersbyuse3_.USER_GUID"
										+ "	inner join user_type usertypeal4_"
										+ "	on usersbyuse3_.USER_TYPE_ID=usertypeal4_.USER_TYPE_ID where "
										+ gradePONumber.getTenantId() + "=this_.TENANT_ID"
										+ "	and usersbyuse3_.USER_STATUS='ACT' and schusrgrou1_.USER_GROUP_ID="
										+ userGroupId, con);

						if (resultSetUserDetails != null) {
							while (resultSetUserDetails.next()) {
								String userId = resultSetUserDetails.getString("USER_GUID");
								String firstName = resultSetUserDetails.getString("FIRST_NAME");
								String lastName = resultSetUserDetails.getString("LAST_NAME");
								String userName = resultSetUserDetails.getString("USER_NAME");
								String userCleverId = resultSetUserDetails.getString("usersbyuse3_.OTHER_SYSTEM_ID");
								logger.info("user Details: " + userId + "\t" + firstName + "\t" + lastName + "\t"
										+ userName + "\t" + userCleverId);

								ObjectMapper mapper = new ObjectMapper();
								AssignPODistrictServiceRequest assignPODistrictServiceRequest = new AssignPODistrictServiceRequest();
								assignPODistrictServiceRequest.setUserName(userName);
								assignPODistrictServiceRequest.setPonumber(gradePONumber.getPoNumber());

								try {

									HttpResponse response = Request.Post(properties.getProperty("apiservice"))
											.addHeader("Content-Type", "application/json")
											.body(new StringEntity(
													mapper.writeValueAsString(assignPODistrictServiceRequest), "UTF-8"))
											.execute().returnResponse();

									String apiserver = EntityUtils.toString(response.getEntity());
									JSONObject meUserStubObject = new JSONObject(apiserver);
									logger.info("User response status: " + userName + ": "
											+ meUserStubObject.getJSONObject("response").get("responseCode") + ": "
											+ meUserStubObject.getJSONObject("response").get("message"));

									Thread.sleep(waittime);

								} catch (JsonGenerationException e) {
									logger.error(e);
								} catch (JsonMappingException e) {
									logger.error(e);
								} catch (ClientProtocolException e) {
									logger.error(e);
								} catch (UnsupportedEncodingException e) {
									logger.error(e);
								} catch (IOException e) {
									logger.error(e);
								} catch (JSONException e) {
									logger.error(e);
								} catch (InterruptedException e) {
									logger.error(e);
								}
							}
							DbUtils.closeQuietly(resultSetUserDetails);
						}

						/**
						 * School Teacher License assignation
						 * 
						 */
						resultSetSchoolAdmin = DataService.getResultSet("select usersbyuse1_.USER_NAME as y6_ from"
								+ " school_user this_ inner join users usersbyuse1_"
								+ " on this_.USER_ID=usersbyuse1_.USER_GUID   inner join"
								+ " user_type usertypeal2_ on usersbyuse1_.USER_TYPE_ID=usertypeal2_.USER_TYPE_ID"
								+ " where '" + gradePONumber.getTenantId() + "'=this_.TENANT_ID"
								+ " and this_.SCHOOL_ID='" + schoolId + "' and this_.USER_TYPE='SCHOOL_ADMIN'"
								+ " and usersbyuse1_.USER_STATUS='ACT' ", con);

						if (resultSetSchoolAdmin != null) {
							while (resultSetSchoolAdmin.next()) {
								resultSetTeacherDetails = DataService
										.getResultSet("select usersbyuse4_.USER_NAME as y4_ from"
												+ "	school_class_teacher this_	inner join"
												+ "	school_classes schoolclas1_"
												+ "	on this_.SCHOOL_CLASS_ID=schoolclas1_.SCHOOL_CLASS_ID"
												+ "	inner join	sch_usr_group schusrgrou2_"
												+ "	on schoolclas1_.GROUP_ID=schusrgrou2_.USER_GROUP_ID"
												+ "	inner join	school_user schooluser3_"
												+ "	on this_.SCHOOL_USER_ID=schooluser3_.SCHOOL_USER_ID"
												+ "	inner join	 users usersbyuse4_"
												+ "	on schooluser3_.USER_ID=usersbyuse4_.USER_GUID inner join"
												+ "	user_type usertypeal5_"
												+ "	on usersbyuse4_.USER_TYPE_ID=usertypeal5_.USER_TYPE_ID where '"
												+ gradePONumber.getTenantId()
												+ "'=this_.TENANT_ID and usersbyuse4_.USER_STATUS='ACT'"
												+ "	and schusrgrou2_.USER_GROUP_ID='" + userGroupId + "'", con);

								List<String> teacherList = new ArrayList<String>();

								while (resultSetTeacherDetails.next()) {
									teacherList.add(resultSetTeacherDetails.getString(1));
								}

								resultSetLicenseDetails = DataService.getResultSet(
										"select LICENSE_COUNT   from      access_code this_"
												+ "    inner join       license_type licensetyp4_"
												+ "            on this_.ACCESS_CODE_LICENSE_TYPE_ID=licensetyp4_.ID"
												+ "    inner join        po_access_code poaccessco1_"
												+ "            on this_.ACCESS_CODE_ID=poaccessco1_.ACCESS_CODE_ID"
												+ "    inner join       purchase_order purchaseor2_"
												+ "            on poaccessco1_.PO_ID=purchaseor2_.PURCHASE_ORDER_ID"
												+ "    where      purchaseor2_.PURCHASE_ORDER_NUMBER='"
												+ gradePONumber.getPoNumber() + "'       and this_.TENANT_ID='"
												+ gradePONumber.getTenantId() + "'        and this_.SCHOOL_ID='"
												+ schoolId + "' and this_.STATUS='ACT' and this_.EXPIRY_DATE>=now()",
										con);

								Integer licenseCount = 0;
								if (resultSetLicenseDetails != null) {
									if (resultSetLicenseDetails.next()) {
										licenseCount = resultSetLicenseDetails.getInt(1);
									}
								}

								logger.info("licenseCount: " + licenseCount);

								DbUtils.closeQuietly(resultSetLicenseDetails);

								if (!isEmptyObject(licenseCount) || licenseCount > 0) {
									ObjectMapper mapper = new ObjectMapper();
									AssignPODistrictServiceRequest assignPODistrictServiceRequest = new AssignPODistrictServiceRequest();
									assignPODistrictServiceRequest.setUserName(resultSetSchoolAdmin.getString(1));
									assignPODistrictServiceRequest.setPonumber(gradePONumber.getPoNumber());
									assignPODistrictServiceRequest.setAssignedteacherUserNames(teacherList);
									assignPODistrictServiceRequest.setLicenseCount(licenseCount);

									HttpResponse response = Request.Post(properties.getProperty("apiservice"))
											.addHeader("Content-Type", "application/json")
											.body(new StringEntity(
													mapper.writeValueAsString(assignPODistrictServiceRequest), "UTF-8"))
											.execute().returnResponse();

									String apiserver = EntityUtils.toString(response.getEntity());
									JSONObject meUserStubObject = new JSONObject(apiserver);
									logger.info("Teacher Assignation response status: "
											+ meUserStubObject.getJSONObject("response").get("responseCode") + ": "
											+ meUserStubObject.getJSONObject("response").get("message"));

									Thread.sleep(waittime);
								} else {
									logger.info("For School id: " + schoolId + " there is no License count avaiable.");
								}
							}
							DbUtils.closeQuietly(resultSetSchoolAdmin);
						} else {
							logger.info("For School id: " + schoolId + " there is no school admin created.");
						}

					}
					DbUtils.closeQuietly(resultSet);
				}
			}
		} catch (SQLException | JSONException e) {
			e.printStackTrace();
			logger.error(e);
			logger.error(e.getMessage());
			logger.error(e.getCause());
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(resultSetUserDetails);
			DbUtils.closeQuietly(resultSetSchoolAdmin);
			DbUtils.closeQuietly(resultSetLicenseDetails);
			DbUtils.closeQuietly(resultSetTeacherDetails);
		}
	}

	public static Properties loadproperties(String propPath) {
		InputStream input = null;
		try {
			Properties prop = new Properties();
			input = new FileInputStream(propPath);
			prop.load(input);
			input.close();
			return prop;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean isEmptyObject(Object value) {
		return value == null || value.toString().trim().length() == 0;
	}
}
