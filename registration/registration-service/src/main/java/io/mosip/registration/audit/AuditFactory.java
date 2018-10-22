package io.mosip.registration.audit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;

import io.mosip.kernel.auditmanager.builder.AuditRequestBuilder;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.core.spi.auditmanager.AuditHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.stereotype.Service;

import io.mosip.registration.constants.AppModuleEnum;
import io.mosip.registration.constants.AuditEventEnum;
import io.mosip.registration.constants.RegConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.context.SessionContext.UserContext;

/**
 * Class to Audit the events of Registration.
 * <p>
 * This class creates a wrapper around {@link AuditRequest} class. This class
 * creates a {@link AuditRequest} object for each audit event and persists the
 * same.
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 *
 */
@Service
public class AuditFactory {

	@Autowired
	private AuditHandler<AuditRequestDto> auditHandler;
	@Autowired
	private Environment environment;

	/**
	 * Static method to audit the events across Registration Processor Module.
	 * <p>
	 * This method takes {@code AuditEventEnum}, {@link AppModuleEnum}, audit
	 * description, refId and refIdType as inputs values from Session Context object
	 * namely createdBy, sessionUserId and sessionUserName to build the
	 * {@link AuditRequest} object. This {@link AuditRequest} object will be passed
	 * to the {@link AuditingHandler} which will persist the audit event in
	 * database.
	 * 
	 * @param auditEventEnum
	 *            this {@code Enum} contains the event details namely eventId,
	 *            eventType and eventName
	 * @param appModuleEnum
	 *            this {@code Enum} contains the application module details namely
	 *            moduleId and moduleName
	 * @param auditDescription
	 *            the description of the audit event
	 * @param refId
	 *            the ref id of the audit event
	 * @param refIdType
	 *            the ref id type of the audit event
	 */
	public void audit(AuditEventEnum auditEventEnum, AppModuleEnum appModuleEnum, String auditDescription, String refId,
			String refIdType) {

		// Get UserContext Object from SessionContext
		UserContext userContext = SessionContext.getInstance().getUserContext();
		String userId = userContext.getUserId() == null ? "NA" : userContext.getUserId();
		String userName = userContext.getName() == null ? "NA" : userContext.getName(); 

		// Getting Host IP Address and Name
		String hostIP = null;
		String hostName = null;
		try {
			InetAddress hostInetAddress = InetAddress.getLocalHost();
			hostIP = hostInetAddress.getHostAddress();
			hostName = hostInetAddress.getHostName();
		} catch (UnknownHostException unknownHostException) {
			hostIP = environment.getProperty(RegConstants.HOST_IP);
			hostName = environment.getProperty(RegConstants.HOST_NAME);
		}

		AuditRequestBuilder auditRequestBuilder = new AuditRequestBuilder();
		auditRequestBuilder.setActionTimeStamp(OffsetDateTime.now())
				.setApplicationId(environment.getProperty(RegConstants.AUDIT_APPLICATION_ID))
				.setApplicationName(environment.getProperty(RegConstants.AUDIT_APPLICATION_NAME))
				.setCreatedBy(userName).setDescription(auditDescription).setEventId(auditEventEnum.getId())
				.setEventName(auditEventEnum.getName()).setEventType(auditEventEnum.getType()).setHostIp(hostIP)
				.setHostName(hostName).setId(refId).setIdType(refIdType).setModuleId(appModuleEnum.getId())
				.setModuleName(appModuleEnum.getName()).setSessionUserId(userId)
				.setSessionUserName(userName);
		auditHandler.writeAudit(auditRequestBuilder.build());
	}
}
