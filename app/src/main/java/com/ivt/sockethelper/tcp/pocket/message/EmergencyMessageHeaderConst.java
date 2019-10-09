package com.ivt.sockethelper.tcp.pocket.message;

public interface EmergencyMessageHeaderConst {
	//包头 head
	public final String FIELD_IDENTIFIER     = "emergency.header.field.identifier";
	//除head之外的长度
	public final String FIELD_PACKAGE_LENGTH = "emergency.header.field.package_length";
	//版本号
	public final String FIELD_VERSION        = "emergency.header.field.version";
	//备用字节
	public final String FIELD_UNUSED         = "emergency.header.field.unused ";

	//设备唯一标识 MIME码 
	public final String FIELD_DEVICETOKEN        = "emergency.header.field.deviceToken";
	public final String FIELD_DEVICETOKEN_LENGTH = "emergency.header.field.deviceToken_length";
	//医生ID 唯一
	public final String FIELD_DOCID              = "emergency.header.field.docid";
	public final String FIELD_DOCID_LENGTH       = "emergency.header.field.docid_length";
	//消息类型
	public final String FIELD_PACKET_TYPE        = "emergency.header.field.packet_type";
	//包的 ID
	public final String FIELD_PACKET_ID          = "emergency.header.field.packet_id";

	/**用户登录**/
	//用户名
	public final String FIELD_USERNAME_LENGTH            = "emergency.header.field.username_length";
	public final String FIELD_USERNAME                   = "emergency.header.field.username";
	//密码
	public final String FIELD_PASSWORD_MD5_DIGEST_LENGTH = "emergency.header.field.password_MD5_digest_length";
	public final String FIELD_PASSWORD_MD5_DIGEST        = "emergency.header.field.password_md5_digest";

	/**响应消息头**/
	public final String FIELD_ERROR_CODE = "emergency.header.field.error_code";

	public final String FIELD_ERROR_DESCRIPTION_LENGTH = "emergency.header.field.error_description_length";

	public final String FIELD_ERROR_DESCRIPTION = "emergency.header.field.error_description";

	/**XML**/
	public final String FIELD_XML_DATA_LENGTH = "emergency.header.field.xml_data_length";
	public final String FIELD_XML_DATA        = "emergency.header.field.xml_data";

	public final String FIELD_DATA_LENGTH = "emergency.header.field.data_length";
	public final String FIELD_DATA        = "emergency.header.field.data";


	public final String HEADER_SEND_HEARTBEAT = "emergency.header.header_send_heartbeat";
	public final String HEADER_SEND_DATA      = "emergency.header.header_send_data";
	public final String HEADER_USER_LOGIN     = "emergency.header.header_user_login";

	public final String HEADER_RESPONSE_HEARTBEAT        = "emergency.header.header_response_heartbeat";
	public final String HEADER_RESPONSE_DATA             = "emergency.header.header_response_data";
	public final String HEADER_RESPONSE_USER_LOGIN       = "emergency.header.header_response_user_login";
	public final String HEADER_RESPONSE_SERVICE          = "emergency.header.header_response_service";
	public final String HEADER_SERVICE_PUSH_NOTIFICATION = "emergency.header.header_response_service_push_notification";

}