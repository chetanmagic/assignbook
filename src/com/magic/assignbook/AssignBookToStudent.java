package com.magic.assignbook;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.http.HttpResponse;
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

		ResultSet resultSet = null;
		ResultSet resultSetUserDetails = null;
		try {
			Connection con = DataService.loadDriver(properties);
			for (GradePONumber gradePONumber : gradePONumberList) {

				resultSet = DataService
						.getResultSet(
								"select USER_GROUP_ID,GROUP_NAME,SCHOOL_ID,OTHER_SYSTEM_ID from sch_usr_group where GRADE_NAME='"
										+ gradePONumber.getGrade() + "' and tenant_id=" + gradePONumber.getTenantId(),
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
								}
							}
							DbUtils.closeQuietly(resultSetUserDetails);
						}
					}
					DbUtils.closeQuietly(resultSet);
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(resultSetUserDetails);
			DbUtils.closeQuietly(resultSet);
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
}
