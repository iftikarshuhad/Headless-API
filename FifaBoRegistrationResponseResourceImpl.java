package fifa.boregistrationheadless.internal.resource.v1_0;

import com.fifa.audit.model.Audit;
import com.fifa.audit.service.AuditLocalServiceUtil;
import com.fifa.bo.registration.data.model.FifaBoRegistration;
import com.fifa.bo.registration.data.service.FifaBoRegistrationLocalServiceUtil;
import com.fifa.common.api.util.FifaAPIUtil;
import com.fifa.exception.queue.service.ExceptionQueueLocalServiceUtil;
import com.fifa.system.configuration.RegistrationConfiguration;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;

import java.sql.Timestamp;
import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import fifa.boregistrationheadless.dto.v1_0.FifaBoRegistrationResponse;
import fifa.boregistrationheadless.resource.v1_0.FifaBoRegistrationResponseResource;

/**
 * @author Iftikar
 */
@Component(properties = "OSGI-INF/liferay/rest/v1_0/fifa-bo-registration-response.properties", scope = ServiceScope.PROTOTYPE, service = FifaBoRegistrationResponseResource.class)
public class FifaBoRegistrationResponseResourceImpl extends BaseFifaBoRegistrationResponseResourceImpl {
	private static Log log = LogFactoryUtil.getLog(FifaBoRegistrationResponseResourceImpl.class);
	public static final String SITENAME = PropsUtil.get("site-name");

	/**
	 * fifa Bo registration api is used to save the essential details which are
	 * required to display digital fan id
	 */
	@Override
	public FifaBoRegistrationResponse postFifaBoRegistration(
			fifa.boregistrationheadless.dto.v1_0.FifaBoRegistration fifaBoRegistration) throws Exception {

		Timestamp inputTimestamp = new Timestamp(System.currentTimeMillis());

		if (log.isDebugEnabled()) {
			log.debug("FifaBoRegistration api is called..");
		}
		FifaBoRegistrationResponse boRegistrationResponse = new FifaBoRegistrationResponse();
		String fanIdNo = fifaBoRegistration.getFanIdNo();
		long companyId = CompanyThreadLocal.getCompanyId();
		String userId = fifaBoRegistration.getLiferayUserId();
		String[] arabicCountries = StringPool.EMPTY_ARRAY;

		if (Validator.isNull(userId)) {
			userId = "0";
		}

		// Intialized the Audit. Document Number or user fanidno should assigned
		Audit audit = AuditLocalServiceUtil.initiateAudit(0, "Save details from fifa Bo registration API",
				"postFifaBoRegistration", null, this.getClass().getName(), fanIdNo);
		String description = audit.getActionDescription();

		if (Validator.isNotNull(getRegistrationConfiguration())) {
			arabicCountries = getRegistrationConfiguration().getArabicCountries();
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("Started postFifaBoRegistration where arabic country is true");
			}
			description = updateDescription(description, "Started postFifaBoRegistration method");
			FifaBoRegistration fanRegistration = FifaBoRegistrationLocalServiceUtil.fetchRegistrationByFanId(fanIdNo);
			description = updateDescription(description, "Created Instance Sucessfully with fanidno :" + fanIdNo);
			Group group = groupLocalService.getGroup(companyId, SITENAME);
			if (Validator.isNotNull(fanRegistration)) {
				fanRegistration.setGroupId(group.getGroupId());
				fanRegistration.setCompanyId(companyId);
				fanRegistration.setUserId(Long.parseLong(userId));
				fanRegistration.setModifiedDate(new Date());

				description = updateDescription(description, "Filled liferay essential Data");

				fanRegistration.setFanIdNo(fifaBoRegistration.getFanIdNo());
				fanRegistration.setDocumentNumber(fifaBoRegistration.getDocumentNumber());
				fanRegistration.setNationality(fifaBoRegistration.getNationality());
				fanRegistration.setFirstName(fifaBoRegistration.getFirstEnName());
				fanRegistration.setLastName(fifaBoRegistration.getLastEnName());
				fanRegistration.setApplicationStatus(fifaBoRegistration.getApplicationStatus());
				fanRegistration.setArabicCountry(
						isArabicCountry(arabicCountries, getCountryName(fifaBoRegistration.getNationality())));
				fanRegistration.setFirstArabicName(fifaBoRegistration.getFirstArabicName());
				fanRegistration.setFifthArabicName(fifaBoRegistration.getFifthArabicName());
				fanRegistration.setIsChildApplication(fifaBoRegistration.getIsChildApplication());
				fanRegistration.setDocumentType(fifaBoRegistration.getDocumentType());
				fanRegistration.setProfilePicDLfileEntryId(fifaBoRegistration.getProfilePicDLfileEntryId());

				fanRegistration = FifaBoRegistrationLocalServiceUtil.updateFifaBoRegistration(fanRegistration);
				description = updateDescription(description,
						"successfully added or updated the data into DB with Document Number:"
								+ fifaBoRegistration.getDocumentNumber());
				if (log.isDebugEnabled()) {
					log.debug("successfully added or updated the data into DB with Document Number:"
							+ fifaBoRegistration.getDocumentNumber());
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Started postFifaBoRegistration where arabic country is false");
				}
				description = updateDescription(description, "Started postFifaBoRegistration method");
				long registrationID = CounterLocalServiceUtil.increment(FifaBoRegistration.class.getName());

				fanRegistration = FifaBoRegistrationLocalServiceUtil.createFifaBoRegistration(registrationID);

				description = updateDescription(description,
						"Created Instance Sucessfully with registrationID :" + registrationID);

				fanRegistration.setFanRegistrationId(registrationID);

				fanRegistration.setGroupId(group.getGroupId());
				fanRegistration.setCompanyId(companyId);
				fanRegistration.setUserId(Long.parseLong(userId));
				fanRegistration.setModifiedDate(new Date());

				description = updateDescription(description, "Filled liferay essential Data");

				fanRegistration.setFanIdNo(fifaBoRegistration.getFanIdNo());
				fanRegistration.setDocumentNumber(fifaBoRegistration.getDocumentNumber());
				fanRegistration.setNationality(fifaBoRegistration.getNationality());
				fanRegistration.setFirstName(fifaBoRegistration.getFirstEnName());
				fanRegistration.setLastName(fifaBoRegistration.getLastEnName());
				fanRegistration.setApplicationStatus(fifaBoRegistration.getApplicationStatus());
				fanRegistration.setArabicCountry(
						isArabicCountry(arabicCountries, getCountryName(fifaBoRegistration.getNationality())));
				fanRegistration.setFirstArabicName(fifaBoRegistration.getFirstArabicName());
				fanRegistration.setFifthArabicName(fifaBoRegistration.getFifthArabicName());
				fanRegistration.setIsChildApplication(fifaBoRegistration.getIsChildApplication());
				fanRegistration.setDocumentType(fifaBoRegistration.getDocumentType());
				fanRegistration.setProfilePicDLfileEntryId(fifaBoRegistration.getProfilePicDLfileEntryId());

				fanRegistration = FifaBoRegistrationLocalServiceUtil.addFifaBoRegistration(fanRegistration);
				fanRegistration = FifaBoRegistrationLocalServiceUtil.updateFifaBoRegistration(fanRegistration);
				description = updateDescription(description,
						"successfully added or updated the data into DB with Document Number:"
								+ fifaBoRegistration.getDocumentNumber());
				if (log.isDebugEnabled()) {
					log.debug("successfully added or updated the data into DB with Document Number:"
							+ fifaBoRegistration.getDocumentNumber());
				}
			}

			if (Validator.isNotNull(fanRegistration)) {
				boRegistrationResponse.setApplicationStatus("Success");

				description = updateDescription(description,
						"Stored fifa Bo Registration details successfully and  Application status response :SUCCESS");
			} else {
				boRegistrationResponse.setApplicationStatus("Failed");

				description = updateDescription(description,
						"Stored fifa Bo Registration details failed and Application status response :FAILED");
			}

		} catch (Exception e) {
			boRegistrationResponse.setApplicationStatus("Failed");

			description = updateDescription(description,
					"Error Occured at postFifaBoRegistration in headless fifaboregistration :" + e.getMessage());

			if (log.isErrorEnabled()) {
				e.printStackTrace();
			}

			// Log the exception into exception queue
			String actionName = "FifaBoRegistrationAPI - Exception: " + e.getMessage();

			String exceptionDescription = FifaAPIUtil.updateResponseLogs(StringPool.BLANK,
					prepareExceptionMessage(Long.parseLong(userId), fanIdNo, e.getMessage()), "Fifa Bo registration",
					"Get Data form Bo");

			ExceptionQueueLocalServiceUtil.createExceptionQueue(StringPool.BLANK, StringPool.BLANK, actionName,
					exceptionDescription, FifaBoRegistrationResponseResourceImpl.class.getName(), fanIdNo);

		}

		audit.setActionPerformedEndDate(new Date());
		audit.setActionDescription(description);
		audit.setActionPerformedBy(String.valueOf(userId));
		AuditLocalServiceUtil.updateAudit(audit);
		Timestamp outputTimestamp = new Timestamp(System.currentTimeMillis());
		long diff = outputTimestamp.getTime() - inputTimestamp.getTime();

		description = updateDescription(description,
				"fifa bo registration ends and time taken by API is : " + diff + "ms");

		if (log.isDebugEnabled()) {
			log.debug("postFifaBoRegistration ends, time: " + diff + "ms");
		}
		return boRegistrationResponse;
	}

	/**
	 * This method holds the logic for getting faq configuration from the system
	 * settings
	 * 
	 * @param companyId
	 * @return
	 */
	private static RegistrationConfiguration getRegistrationConfiguration() {
		RegistrationConfiguration registrationConfiguration = null;
		try {

			if (log.isDebugEnabled()) {
				log.debug("Called getRegistrationConfiguration method to get logic for Configuration from system");
			}

			registrationConfiguration = ConfigurationProviderUtil
					.getSystemConfiguration(RegistrationConfiguration.class);

			if (log.isDebugEnabled()) {
				log.debug("got registration Configuration");
			}

			return registrationConfiguration;
		} catch (Exception ex) {
			if (log.isErrorEnabled()) {
				log.error("Error while getting faq configuration : " + ex);
			}
		}
		return registrationConfiguration;
	}

	/**
	 * This method is used to get boolean value wheather the country is arabic
	 * country or not.
	 * 
	 * @param arabicCountries
	 * @param countryName
	 * @return
	 */
	private static boolean isArabicCountry(String[] arabicCountries, String countryName) {
		if (log.isDebugEnabled()) {
			log.debug("Called is Arabic Country");
		}
		for (String aCountry : arabicCountries) {
			if (aCountry.toLowerCase().equalsIgnoreCase(countryName.toLowerCase())) {
				if (log.isDebugEnabled()) {
					log.debug("is Arabic country is true");
				}
				return true;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("is Arabic country is false");
		}
		return false;
	}

	/**
	 * This method is used to get country name with respect to nationality code.
	 * 
	 * @param nationality
	 * @return
	 */
	private static String getCountryName(String nationality) {
		if (log.isDebugEnabled()) {
			log.debug("getCountryName");
		}
		String languageId = "1";
		JSONObject metaDataJson = FifaAPIUtil.getMetaDataJson(languageId);
		JSONArray nationalities = JSONFactoryUtil.createJSONArray();
		String resulCodeType = metaDataJson.getString("resulCodeType");
		if (Validator.isNotNull(resulCodeType) && resulCodeType.equalsIgnoreCase("Success")) {
			nationalities = metaDataJson.getJSONObject("data").getJSONArray("nationality");
		}
		if (log.isDebugEnabled()) {
			log.debug(
					"getCountryName returned :" + FifaAPIUtil.getJsonValue(nationalities, "code", "name", nationality));
		}
		return FifaAPIUtil.getJsonValue(nationalities, "code", "name", nationality);
	}

	/**
	 * This method is used to concatenate the description in the audit.
	 * 
	 * @param oldDescription
	 * @param newDescription
	 * @return (concatenated String)
	 */
	private String updateDescription(String oldDescription, String newDescription) {

		return oldDescription + " ::: " + newDescription;

	}

	public static String prepareExceptionMessage(long userId, String fanId, String exceptionMessage) {

		StringBuilder sb = new StringBuilder();

		sb.append("userId : " + userId + StringPool.COMMA_AND_SPACE);
		sb.append("FANID : " + fanId + StringPool.COMMA_AND_SPACE);
		sb.append("Exception Message : " + exceptionMessage + StringPool.COMMA_AND_SPACE);

		return sb.toString();

	}

}